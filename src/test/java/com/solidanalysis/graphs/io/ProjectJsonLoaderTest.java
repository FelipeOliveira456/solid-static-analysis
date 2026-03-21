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
}
