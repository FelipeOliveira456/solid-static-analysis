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
    void diretorioSemJsonRetornaListaVazia(@TempDir Path tmp) throws Exception {
        List<AstArtifact> list = ProjectJsonLoader.loadArtifacts(tmp);
        assertTrue(list.isEmpty());
    }

    @Test
    void carregaMultiplosArtefactos(@TempDir Path tmp) throws Exception {
        String j1 =
                "{\"sourceFile\":\"/a.java\",\"primaryType\":{\"kind\":\"class\",\"name\":\"A\","
                        + "\"abstract\":false,\"superclass\":null,\"implementedInterfaces\":[],"
                        + "\"fields\":[],\"methods\":[]}}";
        String j2 =
                "{\"sourceFile\":\"/b.java\",\"primaryType\":{\"kind\":\"interface\",\"name\":\"B\","
                        + "\"abstract\":false,\"superclass\":null,\"implementedInterfaces\":[],"
                        + "\"fields\":[],\"methods\":[]}}";
        Files.writeString(tmp.resolve("a.json"), j1, StandardCharsets.UTF_8);
        Files.writeString(tmp.resolve("b.json"), j2, StandardCharsets.UTF_8);
        List<AstArtifact> list = ProjectJsonLoader.loadArtifacts(tmp);
        assertEquals(2, list.size());
    }

    @Test
    void carregaJsonEmSubpastasEIgnoraAlgorithmsNaRaiz(@TempDir Path tmp) throws Exception {
        String artifact =
                "{\"sourceFile\":\"/x.java\",\"primaryType\":{\"kind\":\"class\",\"name\":\"X\","
                        + "\"abstract\":false,\"superclass\":null,\"implementedInterfaces\":[],"
                        + "\"fields\":[],\"methods\":[]}}";
        Path nested = tmp.resolve("src").resolve("main").resolve("X.json");
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
