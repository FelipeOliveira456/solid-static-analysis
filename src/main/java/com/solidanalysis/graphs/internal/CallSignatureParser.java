package com.solidanalysis.graphs.internal;

import java.util.Objects;

/** Extracts callee method names from resolved call signatures in AST JSON. */
public final class CallSignatureParser {

    private CallSignatureParser() {}

    public static String methodNameFromSignature(String signature) {
        if (signature == null) {
            return null;
        }
        if (signature.contains("<init>")) {
            return "<init>";
        }
        int paren = signature.indexOf('(');
        String before = paren >= 0 ? signature.substring(0, paren) : signature;
        int dot = before.lastIndexOf('.');
        return dot >= 0 ? before.substring(dot + 1) : before;
    }

    /**
     * @return {@code <init>} for constructors, otherwise the simple method name from {@code
     *     signature}
     */
    public static String calleeMethodKey(String declaringType, String signature) {
        if (signature != null && signature.contains("<init>")) {
            return "<init>";
        }
        String m = methodNameFromSignature(signature);
        if (m == null) {
            return null;
        }
        String declSimple = TypeNames.simpleName(declaringType);
        if (declSimple != null && !declSimple.isEmpty() && Objects.equals(m, declSimple)) {
            return "<init>";
        }
        return m;
    }
}
