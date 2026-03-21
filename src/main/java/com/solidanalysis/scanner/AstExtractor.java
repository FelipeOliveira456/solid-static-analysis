package com.solidanalysis.scanner;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.solidanalysis.scanner.model.AstArtifact;
import com.solidanalysis.scanner.model.ControlFlowStatementSummary;
import com.solidanalysis.scanner.model.FieldSummary;
import com.solidanalysis.scanner.model.MethodCallSummary;
import com.solidanalysis.scanner.model.MethodSummary;
import com.solidanalysis.scanner.model.ParameterSummary;
import com.solidanalysis.scanner.model.TypeSummary;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps a {@link CompilationUnit} to the {@link AstArtifact} export model.
 */
public class AstExtractor {

    private record FlowEntry(int line, int col, ControlFlowStatementSummary summary)
            implements Comparable<FlowEntry> {
        @Override
        public int compareTo(FlowEntry o) {
            int c = Integer.compare(line, o.line);
            if (c != 0) {
                return c;
            }
            return Integer.compare(col, o.col);
        }
    }

    /**
     * Extracts structured data for the first top-level class or interface in the compilation unit.
     *
     * @param cu parsed compilation unit (must have been parsed with symbol resolution configured)
     * @param sourceFile absolute path to the corresponding {@code .java} file
     */
    public AstArtifact extract(CompilationUnit cu, Path sourceFile) {
        ClassOrInterfaceDeclaration typeDecl =
                cu.findFirst(ClassOrInterfaceDeclaration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No class or interface declaration in " + sourceFile));

        TypeSummary summary = new TypeSummary();
        summary.setKind(typeDecl.isInterface() ? "interface" : "class");
        summary.setName(typeDecl.getNameAsString());
        summary.setAbstractType(typeDecl.isInterface() || typeDecl.isAbstract());

        if (!typeDecl.getExtendedTypes().isEmpty()) {
            summary.setSuperclass(typeDecl.getExtendedTypes().get(0).getNameAsString());
        } else {
            summary.setSuperclass(null);
        }

        List<String> interfaces = new ArrayList<>();
        typeDecl.getImplementedTypes().forEach(t -> interfaces.add(t.getNameAsString()));
        summary.setImplementedInterfaces(interfaces);

        List<FieldSummary> fields = new ArrayList<>();
        for (FieldDeclaration fd : typeDecl.getFields()) {
            fd.getVariables()
                    .forEach(
                            v ->
                                    fields.add(
                                            new FieldSummary(
                                                    v.getNameAsString(),
                                                    fd.getElementType().asString())));
        }
        summary.setFields(fields);

        List<MethodSummary> methods = new ArrayList<>();
        for (MethodDeclaration md : typeDecl.getMethods()) {
            MethodSummary ms = new MethodSummary();
            ms.setName(md.getNameAsString());
            ms.setReturnType(md.getType().asString());
            List<ParameterSummary> params = new ArrayList<>();
            for (Parameter p : md.getParameters()) {
                params.add(new ParameterSummary(p.getNameAsString(), p.getType().asString()));
            }
            ms.setParameters(params);

            List<MethodCallSummary> calls = new ArrayList<>();
            List<ControlFlowStatementSummary> flow = new ArrayList<>();
            md.getBody()
                    .ifPresent(
                            body -> {
                                for (MethodCallExpr call : body.findAll(MethodCallExpr.class)) {
                                    calls.add(summarizeCall(call));
                                }
                                collectControlFlow(body, flow);
                            });
            ms.setMethodCalls(calls);
            ms.setControlFlowStatements(flow);
            methods.add(ms);
        }
        summary.setMethods(methods);

        AstArtifact artifact = new AstArtifact();
        artifact.setSourceFile(sourceFile.toAbsolutePath().normalize().toString());
        artifact.setPrimaryType(summary);
        return artifact;
    }

    /**
     * Collects control-flow structures under {@code body}, ordered by source position (line, then
     * column).
     */
    static void collectControlFlow(BlockStmt body, List<ControlFlowStatementSummary> out) {
        List<FlowEntry> entries = new ArrayList<>();

        for (IfStmt s : body.findAll(IfStmt.class)) {
            entries.add(
                    entry(
                            s,
                            new ControlFlowStatementSummary(
                                    "if", s.getCondition().toString().trim(), lineOf(s))));
        }
        for (WhileStmt s : body.findAll(WhileStmt.class)) {
            entries.add(
                    entry(
                            s,
                            new ControlFlowStatementSummary(
                                    "while", s.getCondition().toString().trim(), lineOf(s))));
        }
        for (DoStmt s : body.findAll(DoStmt.class)) {
            entries.add(
                    entry(
                            s,
                            new ControlFlowStatementSummary(
                                    "doWhile", s.getCondition().toString().trim(), lineOf(s))));
        }
        for (ForStmt s : body.findAll(ForStmt.class)) {
            entries.add(
                    entry(
                            s,
                            new ControlFlowStatementSummary("for", forLoopCondition(s), lineOf(s))));
        }
        for (ForEachStmt s : body.findAll(ForEachStmt.class)) {
            String cond =
                    s.getVariable().toString().trim()
                            + " : "
                            + s.getIterable().toString().trim();
            entries.add(entry(s, new ControlFlowStatementSummary("foreach", cond, lineOf(s))));
        }
        for (SwitchStmt s : body.findAll(SwitchStmt.class)) {
            entries.add(
                    entry(
                            s,
                            new ControlFlowStatementSummary(
                                    "switch", s.getSelector().toString().trim(), lineOf(s))));
        }
        for (SynchronizedStmt s : body.findAll(SynchronizedStmt.class)) {
            entries.add(
                    entry(
                            s,
                            new ControlFlowStatementSummary(
                                    "synchronized", s.getExpression().toString().trim(), lineOf(s))));
        }
        for (TryStmt s : body.findAll(TryStmt.class)) {
            entries.add(entry(s, new ControlFlowStatementSummary("try", null, lineOf(s))));
        }
        for (CatchClause c : body.findAll(CatchClause.class)) {
            String cond = c.getParameter().getType().asString();
            entries.add(entry(c, new ControlFlowStatementSummary("catch", cond, lineOf(c))));
        }

        entries.sort(Comparator.naturalOrder());
        for (FlowEntry e : entries) {
            out.add(e.summary());
        }
    }

    private static FlowEntry entry(Node node, ControlFlowStatementSummary summary) {
        return new FlowEntry(lineOf(node), colOf(node), summary);
    }

    private static int lineOf(Node n) {
        return n.getBegin().map(p -> p.line).orElse(0);
    }

    private static int colOf(Node n) {
        return n.getBegin().map(p -> p.column).orElse(0);
    }

    private static String forLoopCondition(ForStmt f) {
        String init =
                f.getInitialization().stream().map(Object::toString).collect(Collectors.joining(", "));
        String compare = f.getCompare().map(Object::toString).orElse("");
        String update = f.getUpdate().stream().map(Object::toString).collect(Collectors.joining(", "));
        return String.join("; ", List.of(init, compare, update));
    }

    private static MethodCallSummary summarizeCall(MethodCallExpr call) {
        String expression = call.toString();
        try {
            ResolvedMethodDeclaration resolved = call.resolve();
            String signature = resolved.getQualifiedSignature();
            String declaring =
                    resolved.declaringType() != null
                            ? resolved.declaringType().getQualifiedName()
                            : null;
            return new MethodCallSummary(expression, true, declaring, signature);
        } catch (RuntimeException e) {
            return new MethodCallSummary(expression, false, null, null);
        }
    }
}
