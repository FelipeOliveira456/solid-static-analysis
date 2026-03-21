package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.scanner.model.AstArtifact;
import com.solidanalysis.scanner.model.TypeSummary;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArtifactJsonWriterTest {

    @Test
    void deveEscreverJsonUtf8NaoVazio(@TempDir Path temp) throws Exception {
        TypeSummary type = new TypeSummary();
        type.setKind("class");
        type.setName("X");
        type.setAbstractType(false);
        type.setSuperclass(null);
        AstArtifact artifact = new AstArtifact();
        artifact.setSourceFile("/tmp/X.java");
        artifact.setPrimaryType(type);

        Path out = temp.resolve("out.json");
        new ArtifactJsonWriter().write(artifact, out);

        byte[] bytes = Files.readAllBytes(out);
        String s = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(s.length() > 0);
        assertTrue(s.contains("X"));
    }
}
