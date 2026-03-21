package com.solidanalysis.graphs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Shared Jackson configuration for AST JSON artifacts. */
public final class GraphObjectMapper {

    private GraphObjectMapper() {}

    /**
     * @return mapper tolerant to unknown properties and null collections where applicable
     */
    public static ObjectMapper create() {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        m.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return m;
    }
}
