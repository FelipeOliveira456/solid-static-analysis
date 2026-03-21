package com.solidanalysis.graphs.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.graphs.model.FieldAccessSummary;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TypeRelevanceFilterTest {

    private final TypeRelevanceFilter filter = new TypeRelevanceFilter();

    @Test
    void excludesPrimitivesAndString() {
        assertTrue(filter.isExcludedTypeReference("int"));
        assertTrue(filter.isExcludedTypeReference("void"));
        assertTrue(filter.isExcludedTypeReference("java.lang.String"));
        assertTrue(filter.isExcludedTypeReference("String"));
    }

    @Test
    void projectTypeRelevantWhenInSet() {
        Set<String> p = Set.of("Foo");
        assertTrue(filter.isRelevantProjectType("Foo", p));
        assertFalse(filter.isRelevantProjectType("String", p));
        assertFalse(filter.isRelevantProjectType("java.util.List", p));
    }

    @Test
    void detectsSystemOut() {
        assertTrue(
                filter.isSystemOutAccess(
                        new FieldAccessSummary("out", "java.lang.System", "read")));
        assertFalse(
                filter.isSystemOutAccess(
                        new FieldAccessSummary("saldo", "ContaBancaria", "read")));
    }
}
