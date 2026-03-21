package com.solidanalysis.graphs.internal;

/** Helpers for Java type strings as they appear in AST JSON. */
public final class TypeNames {

    private TypeNames() {}

    /** Removes generic arguments {@code <...>} from a type reference string. */
    public static String stripGenerics(String type) {
        if (type == null || type.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                if (depth > 0) {
                    depth--;
                }
            } else if (depth == 0) {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    /** Simple name of a possibly qualified type (after stripping generics). */
    public static String simpleName(String type) {
        String t = stripGenerics(type);
        if (t.isEmpty()) {
            return "";
        }
        int dot = t.lastIndexOf('.');
        return dot >= 0 ? t.substring(dot + 1) : t;
    }
}
