package com.solidanalysis.graphs.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidanalysis.graphs.GraphObjectMapper;
import org.junit.jupiter.api.Test;

class GraphModelJsonTest {

    @Test
    void desserializaAstArtifactMinimoComCamposOpcionaisOmitidos() throws Exception {
        ObjectMapper om = GraphObjectMapper.create();
        String json =
                "{\"sourceFile\":\"/tmp/X.java\",\"primaryType\":{\"kind\":\"class\",\"name\":\"X\","
                        + "\"abstract\":false,\"superclass\":null,\"implementedInterfaces\":[],"
                        + "\"fields\":[],\"methods\":[]}}";
        AstArtifact a = om.readValue(json, AstArtifact.class);
        assertEquals("/tmp/X.java", a.sourceFile());
        assertEquals("X", a.primaryType().name());
        assertTrue(a.primaryType().methods().isEmpty());
    }
}
