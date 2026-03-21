package com.solidanalysis.graphs.filter;

import com.solidanalysis.graphs.internal.TypeNames;
import com.solidanalysis.graphs.model.FieldAccessSummary;
import java.util.Set;

/**
 * Decides which type names and field accesses participate in project-only graphs (exclude JDK,
 * primitives, {@code System.out}, etc.).
 */
public final class TypeRelevanceFilter {

    private static final Set<String> PRIMITIVES_AND_VOID =
            Set.of("int", "long", "short", "byte", "char", "float", "double", "boolean", "void");

    /** Common {@code java.lang.*} simple names that appear unqualified in JSON. */
    private static final Set<String> JDK_SIMPLE_NAMES =
            Set.of(
                    "String",
                    "Object",
                    "Class",
                    "Integer",
                    "Long",
                    "Short",
                    "Byte",
                    "Character",
                    "Boolean",
                    "Float",
                    "Double",
                    "Void",
                    "Enum",
                    "Throwable",
                    "Exception",
                    "RuntimeException",
                    "Error",
                    "Cloneable",
                    "Serializable",
                    "AutoCloseable");

    /**
     * @return true if {@code rawType} should never appear as a graph dependency target/source for
     *     external/JDK noise
     */
    public boolean isExcludedTypeReference(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return true;
        }
        String simple = TypeNames.simpleName(rawType);
        if (simple.isEmpty()) {
            return true;
        }
        if (PRIMITIVES_AND_VOID.contains(simple)) {
            return true;
        }
        String stripped = TypeNames.stripGenerics(rawType);
        if (stripped.startsWith("java.") || stripped.startsWith("javax.")) {
            return true;
        }
        return JDK_SIMPLE_NAMES.contains(simple);
    }

    /**
     * @return true if {@code simpleName} is a type that exists in the analyzed project and is not
     *     excluded as JDK/primitive noise
     */
    public boolean isRelevantProjectType(String simpleName, Set<String> projectTypeNames) {
        if (simpleName == null || simpleName.isEmpty()) {
            return false;
        }
        if (isExcludedTypeReference(simpleName)) {
            return false;
        }
        return projectTypeNames.contains(simpleName);
    }

    /** Field access to {@code System.out} (or other standard streams) must be ignored in graphs. */
    public boolean isSystemOutAccess(FieldAccessSummary access) {
        if (access == null) {
            return false;
        }
        boolean system = "java.lang.System".equals(access.ownerClass());
        return system && "out".equals(access.fieldName());
    }
}
