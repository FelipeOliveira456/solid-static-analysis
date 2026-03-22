package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.scanner.model.AstArtifact;
import com.solidanalysis.scanner.model.ControlFlowStatementSummary;
import com.solidanalysis.scanner.model.MethodSummary;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integração com fontes de teste em {@code src/test/resources/java-fixtures/} (herança, switch,
 * while, foreach, else if, campos).
 *
 * <p>O teste {@link #geraArtefactosJsonNoOutputDosFixtures} materializa JSON num diretório
 * temporário (o diretório {@code java-fixtures/output} no repo fica vazio / só README).
 */
class JavaFixturesScannerTest {

    private Path fixtureRoot;

    @BeforeEach
    void loadFixtureRoot() throws Exception {
        URL marker =
                Objects.requireNonNull(
                        getClass().getClassLoader().getResource("java-fixtures/ContaBancaria.java"));
        fixtureRoot = Paths.get(marker.toURI()).getParent();
    }

    private AstArtifact extract(String fileName) throws Exception {
        Path f = fixtureRoot.resolve(fileName);
        JavaParserFacade facade = new JavaParserFacade(fixtureRoot);
        return new AstExtractor().extract(facade.parse(f), f);
    }

    private static MethodSummary method(AstArtifact a, String name) {
        return a.getPrimaryType().getMethods().stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("method not found: " + name));
    }

    @Test
    void geraArtefactosJsonNoOutputDosFixtures(@TempDir Path tempOut) throws Exception {
        Path outDir = tempOut.resolve("scanner-out");
        Files.createDirectories(outDir);
        JavaParserFacade facade = new JavaParserFacade(fixtureRoot);
        ProjectScanner scanner =
                new ProjectScanner(facade, new AstExtractor(), new ArtifactJsonWriter());
        PrintStream silent =
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        ScanRunResult r = scanner.scan(fixtureRoot, outDir, silent);
        assertEquals(0, r.getFailureCount(), r.getFailureMessages()::toString);
        Path scanRootAbs = fixtureRoot.toAbsolutePath().normalize();
        long javaCount;
        try (Stream<Path> walk = Files.walk(fixtureRoot)) {
            javaCount =
                    walk.filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(".java"))
                            .count();
        }
        assertEquals(
                javaCount,
                r.getSuccessCount(),
                "cada .java no diretório de fixtures deve gerar um JSON; atualize o teste se mudar o conjunto de fontes");
        try (Stream<Path> walk = Files.walk(fixtureRoot)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .forEach(
                            javaFile -> {
                                Path expected =
                                        OutputArtifactNamer.resolveOutputPath(
                                                outDir, scanRootAbs, javaFile);
                                assertTrue(
                                        Files.exists(expected),
                                        () -> "expected JSON for " + javaFile + ": " + expected);
                            });
        }
    }

    @Test
    void contaBancariaSacarElseIfEncadeadoEUmUnicoIfComFieldAccesses() throws Exception {
        AstArtifact a = extract("ContaBancaria.java");
        assertEquals("ContaBancaria", a.getPrimaryType().getName());
        MethodSummary sacar = method(a, "sacar");
        List<ControlFlowStatementSummary> flows = sacar.getControlFlowStatements();
        long ifCount = flows.stream().filter(s -> "if".equals(s.getKind())).count();
        assertEquals(1, ifCount, "sacar: um único nó if para o if/else if/else");
        ControlFlowStatementSummary root =
                flows.stream().filter(s -> "if".equals(s.getKind())).findFirst().orElseThrow();
        assertNotNull(root.getChainedElseIf(), "else if (valor > saldo)");
        assertNull(
                root.getChainedElseIf().getChainedElseIf(),
                "último ramo é else { saldo -= valor }, não outro else if");
        assertTrue(root.getCondition().contains("ativa"));
        assertTrue(
                sacar.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "saldo".equals(fa.getFieldName())
                                                && "write".equals(fa.getAccessType())));
        assertTrue(
                sacar.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "ativa".equals(fa.getFieldName())
                                                && "read".equals(fa.getAccessType())));

        MethodSummary depositar = method(a, "depositar");
        assertEquals(
                1,
                depositar.getControlFlowStatements().stream()
                        .filter(s -> "if".equals(s.getKind()))
                        .count());
        assertNull(
                depositar.getControlFlowStatements().stream()
                        .filter(s -> "if".equals(s.getKind()))
                        .findFirst()
                        .orElseThrow()
                        .getChainedElseIf());

        MethodSummary encerrar = method(a, "encerrar");
        assertTrue(
                encerrar.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "ativa".equals(fa.getFieldName())
                                                && "write".equals(fa.getAccessType())));
        assertTrue(
                encerrar.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "saldo".equals(fa.getFieldName())
                                                && "write".equals(fa.getAccessType())));
    }

    @Test
    void contaCorrenteSwitchForEachEIf() throws Exception {
        AstArtifact a = extract("ContaCorrente.java");
        assertEquals("ContaCorrente", a.getPrimaryType().getName());

        MethodSummary resolverTaxa = method(a, "resolverTaxa");
        ControlFlowStatementSummary sw =
                resolverTaxa.getControlFlowStatements().stream()
                        .filter(s -> "switch".equals(s.getKind()))
                        .findFirst()
                        .orElseThrow();
        assertNotNull(sw.getCases());
        List<String> labels =
                sw.getCases().stream().map(c -> c.getLabel()).collect(Collectors.toList());
        assertTrue(labels.stream().anyMatch(l -> l.contains("basico")), labels::toString);
        assertTrue(labels.stream().anyMatch(l -> l.contains("premium")), labels::toString);
        assertTrue(labels.stream().anyMatch(l -> l.contains("empresarial")), labels::toString);
        assertTrue(labels.contains("default"), labels::toString);
        assertNotNull(sw.getEndLine());

        MethodSummary registrar = method(a, "registrarTransacoes");
        assertTrue(
                registrar.getControlFlowStatements().stream()
                        .anyMatch(s -> "foreach".equals(s.getKind())));
        assertEquals(
                1,
                registrar.getControlFlowStatements().stream()
                        .filter(s -> "if".equals(s.getKind()))
                        .count());
        assertTrue(
                registrar.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "saldo".equals(fa.getFieldName())
                                                && "write".equals(fa.getAccessType())));
        assertTrue(
                registrar.getFieldAccesses().stream()
                        .anyMatch(fa -> "transacoesMes".equals(fa.getFieldName())),
                "transacoesMes++ é incremento unário, não AssignExpr; ainda assim deve aparecer leitura");

        MethodSummary cobrar = method(a, "cobrarTaxa");
        assertEquals(
                1,
                cobrar.getControlFlowStatements().stream()
                        .filter(s -> "if".equals(s.getKind()))
                        .count());
        assertTrue(
                cobrar.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "saldo".equals(fa.getFieldName())
                                                && "write".equals(fa.getAccessType())));
    }

    @Test
    void contaPoupancaWhileEIfElse() throws Exception {
        AstArtifact a = extract("ContaPoupanca.java");
        assertEquals("ContaPoupanca", a.getPrimaryType().getName());

        MethodSummary aplicar = method(a, "aplicarRendimentosMensais");
        ControlFlowStatementSummary wh =
                aplicar.getControlFlowStatements().stream()
                        .filter(s -> "while".equals(s.getKind()))
                        .findFirst()
                        .orElseThrow();
        assertNotNull(wh.getThenLine());
        assertNotNull(wh.getEndLine());
        assertTrue(wh.getEndLine() >= wh.getThenLine());
        assertTrue(
                aplicar.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "saldo".equals(fa.getFieldName())
                                                && "write".equals(fa.getAccessType())));
        assertTrue(
                aplicar.getFieldAccesses().stream()
                        .anyMatch(fa -> "mesesAplicados".equals(fa.getFieldName())),
                "mesesAplicados++ é incremento unário; exige pelo menos um acesso resolvido ao campo");

        MethodSummary imposto = method(a, "calcularImposto");
        assertEquals(
                1,
                imposto.getControlFlowStatements().stream()
                        .filter(s -> "if".equals(s.getKind()))
                        .count());
        ControlFlowStatementSummary ifImp =
                imposto.getControlFlowStatements().stream()
                        .filter(s -> "if".equals(s.getKind()))
                        .findFirst()
                        .orElseThrow();
        assertNotNull(ifImp.getThenLine());
        assertNotNull(ifImp.getElseLine());
        assertNull(ifImp.getChainedElseIf());
    }

    @Test
    void tributavelSoMetodosDeInterface() throws Exception {
        AstArtifact a = extract("Tributavel.java");
        assertEquals("interface", a.getPrimaryType().getKind());
        assertEquals("Tributavel", a.getPrimaryType().getName());
        assertTrue(a.getPrimaryType().getMethods().size() >= 2);
    }
}
