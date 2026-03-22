package com.solidanalysis.graphs;

import com.solidanalysis.graphs.dot.DirectedDotGraph;
import com.solidanalysis.graphs.dot.DotText;
import com.solidanalysis.graphs.model.ControlFlowStatementSummary;
import com.solidanalysis.graphs.model.MethodSummary;
import com.solidanalysis.graphs.model.PlacedArtifact;
import com.solidanalysis.graphs.model.ParsedProject;
import com.solidanalysis.graphs.model.TypeSummary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** G7 — per-method simplified CFG as individual DOT files under {@code g7_cfg/}. */
public final class G7CfgGraphGenerator {

    public void writeAll(Path graphsRoot, ParsedProject project) throws IOException {
        for (PlacedArtifact pa : project.placedArtifacts()) {
            TypeSummary type = pa.artifact().primaryType();
            if (type == null) {
                continue;
            }
            String owner = type.name();
            Path g7Dir = graphsRoot.resolve(pa.relativeOutputDir()).resolve("g7_cfg");
            Files.createDirectories(g7Dir);
            for (MethodSummary m : type.methods()) {
                List<ControlFlowStatementSummary> cf = m.controlFlowStatements();
                if (cf == null || cf.isEmpty()) {
                    continue;
                }
                String fileBase = methodFileBase(owner, m);
                Path out = g7Dir.resolve(fileBase + ".dot");
                Files.writeString(out, buildMethodCfg(owner, m, cf), StandardCharsets.UTF_8);
            }
        }
    }

    private static String methodFileBase(String owner, MethodSummary m) {
        if ("<init>".equals(m.returnType())) {
            return owner + "_init";
        }
        return owner + "_" + sanitizeFileToken(m.name());
    }

    private static String sanitizeFileToken(String name) {
        if (name == null || name.isEmpty()) {
            return "method";
        }
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private String buildMethodCfg(String owner, MethodSummary m, List<ControlFlowStatementSummary> flat) {
        List<ControlFlowStatementSummary> list = new ArrayList<>(flat);
        list.sort(
                Comparator.comparingInt(
                        (ControlFlowStatementSummary s) ->
                                s.line() == null ? Integer.MAX_VALUE : s.line()));

        Map<Integer, List<Integer>> childrenByParent = new HashMap<>();
        List<Integer> rootIndices = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Integer p = findParentIndex(list, i);
            if (p == null) {
                rootIndices.add(i);
            } else {
                childrenByParent.computeIfAbsent(p, k -> new ArrayList<>()).add(i);
            }
        }
        for (List<Integer> ch : childrenByParent.values()) {
            ch.sort(
                    Comparator.comparingInt(
                            idx -> {
                                Integer L = list.get(idx).line();
                                return L == null ? Integer.MAX_VALUE : L;
                            }));
        }
        rootIndices.sort(
                Comparator.comparingInt(
                        idx -> {
                            Integer L = list.get(idx).line();
                            return L == null ? Integer.MAX_VALUE : L;
                        }));

        DirectedDotGraph g = new DirectedDotGraph("CFG_" + owner + "_" + m.name());
        String entry = DotText.sanitizeId("entry");
        g.addNode(entry, "entry", Map.of("shape", "point"));
        AtomicInteger seq = new AtomicInteger();
        for (int rootIdx : rootIndices) {
            emitSubtree(g, list, childrenByParent, rootIdx, entry, null, seq);
        }
        return g.toDot();
    }

    /**
     * Emits statement {@code idx} hanging from {@code attachFrom}. Nested statements (from the flat
     * list) attach to the parent's {@code then} or {@code else} region stub when line ranges
     * indicate nesting.
     */
    private void emitSubtree(
            DirectedDotGraph g,
            List<ControlFlowStatementSummary> list,
            Map<Integer, List<Integer>> childrenByParent,
            int idx,
            String attachFrom,
            String incomingLabel,
            AtomicInteger seq) {
        ControlFlowStatementSummary st = list.get(idx);
        String id = DotText.sanitizeId("s_" + seq.incrementAndGet());
        String label =
                st.condition() != null && !st.condition().isBlank()
                        ? st.kind() + ": " + st.condition()
                        : st.kind();
        g.addNode(id, label, Map.of());
        if (attachFrom != null) {
            g.addEdge(attachFrom, id, incomingLabel);
        }
        if (isLoopKind(st.kind())) {
            g.addEdge(id, id, "loop");
        }

        if ("switch".equals(st.kind()) && st.cases() != null) {
            for (var c : st.cases()) {
                String cid = DotText.sanitizeId("case_" + seq.incrementAndGet());
                String cl = c.label() != null ? c.label() : "default";
                g.addNode(cid, cl, Map.of("shape", "box"));
                g.addEdge(id, cid, cl);
            }
            emitNestedChildren(g, list, childrenByParent, idx, id, seq);
            return;
        }

        String thenStub = null;
        if (st.thenLine() != null) {
            thenStub = DotText.sanitizeId("then_" + st.thenLine() + "_" + seq.incrementAndGet());
            g.addNode(thenStub, "then", Map.of("shape", "box"));
            g.addEdge(id, thenStub, "then");
        }

        String elseStub = null;
        if (st.chainedElseIf() != null) {
            emit(g, st.chainedElseIf(), id, "else", seq);
        } else if (st.elseLine() != null) {
            elseStub = DotText.sanitizeId("else_" + st.elseLine() + "_" + seq.incrementAndGet());
            g.addNode(elseStub, "else", Map.of("shape", "box"));
            g.addEdge(id, elseStub, "else");
        }

        emitNestedChildren(g, list, childrenByParent, idx, st, thenStub, elseStub, id, seq);
    }

    private void emitNestedChildren(
            DirectedDotGraph g,
            List<ControlFlowStatementSummary> list,
            Map<Integer, List<Integer>> childrenByParent,
            int idx,
            ControlFlowStatementSummary st,
            String thenStub,
            String elseStub,
            String decisionId,
            AtomicInteger seq) {
        List<Integer> childIdx = childrenByParent.getOrDefault(idx, List.of());
        for (int c : childIdx) {
            ControlFlowStatementSummary ch = list.get(c);
            String attach = chooseAttachForChild(st, thenStub, elseStub, decisionId, ch);
            emitSubtree(g, list, childrenByParent, c, attach, null, seq);
        }
    }

    /** Switch: no then/else stubs; hang nested statements from the switch head. */
    private void emitNestedChildren(
            DirectedDotGraph g,
            List<ControlFlowStatementSummary> list,
            Map<Integer, List<Integer>> childrenByParent,
            int idx,
            String switchHeadId,
            AtomicInteger seq) {
        List<Integer> childIdx = childrenByParent.getOrDefault(idx, List.of());
        for (int c : childIdx) {
            emitSubtree(g, list, childrenByParent, c, switchHeadId, null, seq);
        }
    }

    /** Legacy single-statement emit (chained else-if chain). */
    private void emit(
            DirectedDotGraph g,
            ControlFlowStatementSummary st,
            String parentId,
            String incomingLabel,
            AtomicInteger seq) {
        String id = DotText.sanitizeId("s_" + seq.incrementAndGet());
        String label =
                st.condition() != null && !st.condition().isBlank()
                        ? st.kind() + ": " + st.condition()
                        : st.kind();
        g.addNode(id, label, Map.of());
        if (parentId != null) {
            g.addEdge(parentId, id, incomingLabel);
        }
        if (isLoopKind(st.kind())) {
            g.addEdge(id, id, "loop");
        }
        if ("switch".equals(st.kind()) && st.cases() != null) {
            for (var c : st.cases()) {
                String cid = DotText.sanitizeId("case_" + seq.incrementAndGet());
                String cl = c.label() != null ? c.label() : "default";
                g.addNode(cid, cl, Map.of("shape", "box"));
                g.addEdge(id, cid, cl);
            }
            return;
        }
        if (st.thenLine() != null) {
            String tid = DotText.sanitizeId("then_" + st.thenLine() + "_" + seq.incrementAndGet());
            g.addNode(tid, "then", Map.of("shape", "box"));
            g.addEdge(id, tid, "then");
        }
        if (st.chainedElseIf() != null) {
            emit(g, st.chainedElseIf(), id, "else", seq);
        } else if (st.elseLine() != null) {
            String eid = DotText.sanitizeId("else_" + st.elseLine() + "_" + seq.incrementAndGet());
            g.addNode(eid, "else", Map.of("shape", "box"));
            g.addEdge(id, eid, "else");
        }
    }

    private static String chooseAttachForChild(
            ControlFlowStatementSummary parent,
            String thenStub,
            String elseStub,
            String decisionId,
            ControlFlowStatementSummary child) {
        Integer cl = child.line();
        Integer el = parent.elseLine();
        if (cl != null && el != null && cl >= el && elseStub != null) {
            return elseStub;
        }
        if (thenStub != null) {
            return thenStub;
        }
        return decisionId;
    }

    /** Parent = minimal spanning interval that strictly contains the child's line range. */
    private static Integer findParentIndex(List<ControlFlowStatementSummary> list, int i) {
        int[] cr = lineRange(list.get(i));
        if (cr == null) {
            return null;
        }
        int best = -1;
        int bestSpan = Integer.MAX_VALUE;
        for (int j = 0; j < list.size(); j++) {
            if (j == i) {
                continue;
            }
            int[] pr = lineRange(list.get(j));
            if (pr == null) {
                continue;
            }
            if (!strictlyContains(pr, cr)) {
                continue;
            }
            int span = pr[1] - pr[0];
            if (span < bestSpan) {
                bestSpan = span;
                best = j;
            }
        }
        return best < 0 ? null : best;
    }

    /** Inclusive [lo, hi] from {@code thenLine}/{@code endLine}, falling back to {@code line}. */
    private static int[] lineRange(ControlFlowStatementSummary s) {
        Integer lo = s.thenLine() != null ? s.thenLine() : s.line();
        Integer hi = s.endLine() != null ? s.endLine() : s.line();
        if (lo == null || hi == null) {
            return null;
        }
        if (lo > hi) {
            int t = lo;
            lo = hi;
            hi = t;
        }
        return new int[] {lo, hi};
    }

    /** True if {@code outer} is a strict superset of {@code inner} on the line axis. */
    private static boolean strictlyContains(int[] outer, int[] inner) {
        if (outer[0] > inner[0] || inner[1] > outer[1]) {
            return false;
        }
        return outer[0] < inner[0] || inner[1] < outer[1];
    }

    private static boolean isLoopKind(String kind) {
        if (kind == null) {
            return false;
        }
        return switch (kind) {
            case "while", "for", "foreach", "doWhile" -> true;
            default -> false;
        };
    }
}
