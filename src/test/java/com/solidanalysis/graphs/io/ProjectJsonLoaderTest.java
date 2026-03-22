package com.solidanalysis.graphs.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.graphs.model.AstArtifact;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectJsonLoaderTest {

    @Test
    void diretorioSemAstRetornaListaVazia(@TempDir Path tmp) throws Exception {
        List<AstArtifact> list = ProjectJsonLoader.loadArtifacts(tmp);
        assertTrue(list.isEmpty());
    }

    @Test
    void astVazioRetornaListaVazia(@TempDir Path tmp) throws Exception {
        Files.createDirectories(ProjectOutputLayout.astDirectory(tmp));
        List<AstArtifact> list = ProjectJsonLoader.loadArtifacts(tmp);
        assertTrue(list.isEmpty());
    }

    @Test
    void carregaMultiplosArtefactosEmAst(@TempDir Path tmp) throws Exception {
        Path ast = ProjectOutputLayout.astDirectory(tmp);
        Files.createDirectories(ast);
        String j1 =
                "{\"sourceFile\":\"/a.java\",\"primaryType\":{\"kind\":\"class\",\"name\":\"A\","
                        + "\"abstract\":false,\"superclass\":null,\"implementedInterfaces\":[],"
                        + "\"fields\":[],\"methods\":[]}}";
        String j2 =
                "{\"sourceFile\":\"/b.java\",\"primaryType\":{\"kind\":\"interface\",\"name\":\"B\","
                        + "\"abstract\":false,\"superclass\":null,\"implementedInterfaces\":[],"
                        + "\"fields\":[],\"methods\":[]}}";
        Files.writeString(ast.resolve("a.json"), j1, StandardCharsets.UTF_8);
        Files.writeString(ast.resolve("b.json"), j2, StandardCharsets.UTF_8);
        List<AstArtifact> list = ProjectJsonLoader.loadArtifacts(tmp);
        assertEquals(2, list.size());
    }

    @Test
    void carregaJsonEmSubpastasAstEIgnoraAlgorithmsNaRaiz(@TempDir Path tmp) throws Exception {
        Path ast = ProjectOutputLayout.astDirectory(tmp);
        String artifact =
                "{\"sourceFile\":\"/x.java\",\"primaryType\":{\"kind\":\"class\",\"name\":\"X\","
                        + "\"abstract\":false,\"superclass\":null,\"implementedInterfaces\":[],"
                        + "\"fields\":[],\"methods\":[]}}";
        Path nested = ast.resolve("src").resolve("main").resolve("X.json");
        Files.createDirectories(nested.getParent());
        Files.writeString(nested, artifact, StandardCharsets.UTF_8);
        Path alg = tmp.resolve("algorithms").resolve("g1_algorithms.json");
        Files.createDirectories(alg.getParent());
        Files.writeString(alg, "{}", StandardCharsets.UTF_8);

        List<AstArtifact> list = ProjectJsonLoader.loadArtifacts(tmp);
        assertEquals(1, list.size());
        assertEquals("X", list.get(0).primaryType().name());
    }
}
