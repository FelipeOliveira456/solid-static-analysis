package com.solidanalysis.scoring;

import com.solidanalysis.graphs.model.ControlFlowStatementSummary;
import com.solidanalysis.graphs.model.MethodSummary;
import com.solidanalysis.graphs.model.TypeSummary;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AST-based heuristics for Open/Closed “variant dispatch” (if/else-if chains vs polymorphism). Two
 * signals vote into one risk band: 0 → BAIXO (no indicator), 1 → MEDIO, 2 → ALTO.
 *
 * <p>Parameters come from {@code analysis.properties} via {@link DispatchAstParams} (see {@link
 * ThresholdConfiguration}).
 *
 * <ul>
 *   <li><b>H1 — homogeneity:</b> for a chain of {@code if}/{@code else if}, {@code n} = number of
 *       conditions, {@code d} = number of <em>distinct</em> discriminants from {@code ==}, {@code
 *       .equals}, or {@code Objects.equals}. {@code H = 1 - d/n}. H1 fires when {@code H >
 *       scoring.dispatchAst.h1.homogeneityThreshold} and at least two branches yield a
 *       discriminant. One dominant “left side” across many ifs → high {@code H}; e.g. {@code A == …}
 *       and {@code C == …} → larger {@code d} → lower {@code H}.
 *   <li><b>H2 — branch volume:</b> (a) a single {@code if}/{@code else if} chain with at least {@code
 *       scoring.dispatchAst.h2.minChainIfs} conditions, or (b) at least {@code
 *       scoring.dispatchAst.h2.minConsecutiveTopIfs} consecutive top-level {@code if} entries in the
 *       method’s control-flow list (sibling {@code if}s, not {@code else if}).
 * </ul>
 */
public final class DispatchAstHeuristicAnalyzer {

    /**
     * Thresholds for {@link #evaluate(TypeSummary, DispatchAstParams)} from {@code
     * analysis.properties}.
     */
    public record DispatchAstParams(
            double h1HomogeneityThreshold, int h2MinChainIfs, int h2MinConsecutiveTopIfs) {}

    private static final Pattern EQUALS_METHOD =
            Pattern.compile("^(.+?)\\.\\s*equals\\s*\\(", Pattern.DOTALL);
    private static final Pattern OBJECTS_EQUALS =
            Pattern.compile("^Objects\\.\\s*equals\\s*\\(\\s*(.+?)\\s*,", Pattern.DOTALL);

    private DispatchAstHeuristicAnalyzer() {}

    /** Per-class result: {@link #votes()} is 0–2 for MEDIO/ALTO voting. */
    public record Flags(boolean h1, boolean h2) {
        public int votes() {
            return (h1 ? 1 : 0) + (h2 ? 1 : 0);
        }
    }

    public static Flags evaluate(TypeSummary primaryType, DispatchAstParams params) {
        boolean anyH1 = false;
        boolean anyH2 = false;
        if (primaryType != null && primaryType.methods() != null) {
            for (MethodSummary m : primaryType.methods()) {
                if (m == null) {
                    continue;
                }
                Flags mf = methodFlags(m, params);
                anyH1 |= mf.h1;
                anyH2 |= mf.h2;
            }
        }
        return new Flags(anyH1, anyH2);
    }

    static Flags methodFlags(MethodSummary m, DispatchAstParams p) {
        List<ControlFlowStatementSummary> list = m.controlFlowStatements();
        if (list == null || list.isEmpty()) {
            return new Flags(false, false);
        }
        boolean h1 = false;
        boolean h2 = maxConsecutiveTopLevelIfs(list) >= p.h2MinConsecutiveTopIfs();
        for (ControlFlowStatementSummary st : list) {
            if (st == null || !"if".equals(st.kind())) {
                continue;
            }
            List<String> chain = flattenIfChainConditions(st);
            if (heuristic1OnChain(chain, p.h1HomogeneityThreshold())) {
                h1 = true;
            }
            if (chain.size() >= p.h2MinChainIfs()) {
                h2 = true;
            }
        }
        return new Flags(h1, h2);
    }

    static List<String> flattenIfChainConditions(ControlFlowStatementSummary head) {
        List<String> out = new ArrayList<>();
        ControlFlowStatementSummary cur = head;
        while (cur != null && "if".equals(cur.kind())) {
            if (cur.condition() != null && !cur.condition().isBlank()) {
                out.add(cur.condition());
            }
            cur = cur.chainedElseIf();
        }
        return out;
    }

    /**
     * H1: {@code H = 1 - d/n}; {@code d} = distinct discriminants among extractable branches; {@code
     * n} = chain length. Requires ≥2 extractable discriminants.
     */
    static boolean heuristic1OnChain(List<String> conditions, double homogeneityThreshold) {
        if (conditions.size() < 2) {
            return false;
        }
        Set<String> distinct = new LinkedHashSet<>();
        int extracted = 0;
        for (String c : conditions) {
            Optional<String> disc = extractDiscriminant(c);
            if (disc.isPresent()) {
                distinct.add(disc.get());
                extracted++;
            }
        }
        if (extracted < 2) {
            return false;
        }
        int n = conditions.size();
        int d = distinct.size();
        double homogeneity = 1.0 - (double) d / (double) n;
        return homogeneity > homogeneityThreshold;
    }

    static int maxConsecutiveTopLevelIfs(List<ControlFlowStatementSummary> list) {
        int run = 0;
        int max = 0;
        for (ControlFlowStatementSummary st : list) {
            if (st != null && "if".equals(st.kind())) {
                run++;
                max = Math.max(max, run);
            } else {
                run = 0;
            }
        }
        return max;
    }

    static Optional<String> extractDiscriminant(String condition) {
        if (condition == null) {
            return Optional.empty();
        }
        String c = stripOuterParens(condition.trim());
        if (c.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> eq = extractEqualsDiscriminant(c);
        if (eq.isPresent()) {
            return eq;
        }
        Matcher em = EQUALS_METHOD.matcher(c);
        if (em.find()) {
            return Optional.of(normalizeDiscriminant(em.group(1)));
        }
        Matcher om = OBJECTS_EQUALS.matcher(c);
        if (om.find()) {
            return Optional.of(normalizeDiscriminant(om.group(1)));
        }
        return Optional.empty();
    }

    private static Optional<String> extractEqualsDiscriminant(String c) {
        int idx = indexOfTopLevelDoubleEquals(c);
        if (idx < 0) {
            return Optional.empty();
        }
        String lhs = c.substring(0, idx).trim();
        if (lhs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(normalizeDiscriminant(lhs));
    }

    static int indexOfTopLevelDoubleEquals(String s) {
        int depth = 0;
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < s.length() - 1; i++) {
            char ch = s.charAt(i);
            if (!inDouble && ch == '\'' && !inSingle) {
                inSingle = true;
                continue;
            }
            if (inSingle) {
                if (ch == '\'' && (i == 0 || s.charAt(i - 1) != '\\')) {
                    inSingle = false;
                }
                continue;
            }
            if (ch == '"') {
                inDouble = !inDouble;
                continue;
            }
            if (inDouble) {
                continue;
            }
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth = Math.max(0, depth - 1);
            } else if (depth == 0 && ch == '=' && s.charAt(i + 1) == '=') {
                if (i > 0 && s.charAt(i - 1) == '=') {
                    continue;
                }
                return i;
            }
        }
        return -1;
    }

    static String stripOuterParens(String s) {
        String t = s.trim();
        while (hasRedundantOuterParens(t)) {
            t = t.substring(1, t.length() - 1).trim();
        }
        return t;
    }

    static boolean hasRedundantOuterParens(String t) {
        if (t.length() < 2 || t.charAt(0) != '(' || t.charAt(t.length() - 1) != ')') {
            return false;
        }
        int depth = 0;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            }
            if (depth == 0 && i < t.length() - 1) {
                return false;
            }
        }
        return depth == 0;
    }

    static String normalizeDiscriminant(String s) {
        return stripOuterParens(s).replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
    }

    static String formatDetail(int votes, boolean h1, boolean h2, DispatchAstParams p) {
        if (votes <= 0) {
            return "";
        }
        String h1s =
                h1
                        ? "H1: homogeneidade 1−d/n > "
                                + formatPct(p.h1HomogeneityThreshold())
                                + " (scoring.dispatchAst.h1.homogeneityThreshold)"
                        : null;
        String h2s =
                h2
                        ? "H2: cadeia if/else if com ≥"
                                + p.h2MinChainIfs()
                                + " condições (scoring.dispatchAst.h2.minChainIfs) ou ≥"
                                + p.h2MinConsecutiveTopIfs()
                                + " if consecutivos ao mesmo nível (scoring.dispatchAst.h2.minConsecutiveTopIfs)"
                        : null;
        String level =
                votes >= 2
                        ? "duas heurísticas — risco ALTO para extensão sem alterar código existente"
                        : "uma heurística — risco MÉDIO";
        String core =
                h1s != null && h2s != null
                        ? h1s + "; " + h2s + " — " + level
                        : Objects.requireNonNullElse(h1s, h2s) + " — " + level;
        return "DISPATCH_AST_HEURISTICS (if/else-if no AST) — " + core;
    }

    private static String formatPct(double fraction) {
        return String.format(Locale.US, "%.0f%%", 100.0 * fraction);
    }
}
