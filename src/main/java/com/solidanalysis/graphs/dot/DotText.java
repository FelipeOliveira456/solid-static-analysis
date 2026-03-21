package com.solidanalysis.graphs.dot;

import java.util.regex.Pattern;

/** DOT string escaping and identifier sanitization. */
public final class DotText {

    private static final Pattern ID_SAFE = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private DotText() {}

    /** Wraps a label in double quotes with minimal escaping for Graphviz. */
    public static String escapeLabel(String raw) {
        if (raw == null) {
            return "\"\"";
        }
        String escaped = raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
        return "\"" + escaped + "\"";
    }

    /**
     * @return a DOT node/graph id; non-safe characters become underscores
     */
    public static String sanitizeId(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "n";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
            } else if (c == '.' || c == '$' || c == '<' || c == '>' || c == '(' || c == ')') {
                sb.append('_');
            } else {
                sb.append('_');
            }
        }
        String s = sb.toString();
        if (s.isEmpty() || !Character.isJavaIdentifierStart(s.charAt(0))) {
            s = "n_" + s;
        }
        if (ID_SAFE.matcher(s).matches()) {
            return s;
        }
        return "n_" + Math.abs(raw.hashCode());
    }
}
