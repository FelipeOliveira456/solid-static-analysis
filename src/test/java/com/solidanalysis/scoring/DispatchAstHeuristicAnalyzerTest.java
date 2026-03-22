package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.graphs.model.ControlFlowStatementSummary;
import com.solidanalysis.graphs.model.MethodSummary;
import com.solidanalysis.graphs.model.TypeSummary;
import java.util.List;
import org.junit.jupiter.api.Test;

class DispatchAstHeuristicAnalyzerTest {

    private static final DispatchAstHeuristicAnalyzer.DispatchAstParams DEFAULT_PARAMS =
            new DispatchAstHeuristicAnalyzer.DispatchAstParams(0.25, 3, 3);

    @Test
    void greeterLikeChain_bothHeuristics() {
        ControlFlowStatementSummary c3 =
                new ControlFlowStatementSummary(
                        "if", "this.formality == \"intimate\"", 13, null, 16, 18, null, null);
        ControlFlowStatementSummary c2 =
                new ControlFlowStatementSummary(
                        "if", "this.formality == \"casual\"", 10, null, 13, 18, null, c3);
        ControlFlowStatementSummary head =
                new ControlFlowStatementSummary(
                        "if", "this.formality == \"formal\"", 7, null, 10, 18, null, c2);
        MethodSummary greet =
                new MethodSummary(
                        "greet",
                        "String",
                        List.of(),
                        List.of(),
                        List.of(head),
                        List.of(),
                        List.of());
        TypeSummary type =
                new TypeSummary(
                        "class",
                        "Greeter",
                        false,
                        null,
                        List.of(),
                        List.of(),
                        List.of(greet));
        DispatchAstHeuristicAnalyzer.Flags f =
                DispatchAstHeuristicAnalyzer.evaluate(type, DEFAULT_PARAMS);
        assertEquals(2, f.votes());
        assertTrue(f.h1());
        assertTrue(f.h2());
    }

    @Test
    void twoIfsSameDiscriminant_onlyH1() {
        ControlFlowStatementSummary inner =
                new ControlFlowStatementSummary("if", "x == 2", 2, null, null, 3, null, null);
        ControlFlowStatementSummary head =
                new ControlFlowStatementSummary("if", "x == 1", 1, null, 2, 3, null, inner);
        MethodSummary m =
                new MethodSummary(
                        "m", "void", List.of(), List.of(), List.of(head), List.of(), List.of());
        TypeSummary type =
                new TypeSummary("class", "T", false, null, List.of(), List.of(), List.of(m));
        DispatchAstHeuristicAnalyzer.Flags f =
                DispatchAstHeuristicAnalyzer.evaluate(type, DEFAULT_PARAMS);
        assertEquals(1, f.votes());
        assertTrue(f.h1());
        assertFalse(f.h2());
    }

    @Test
    void twoIfsDifferentDiscriminants_noH1() {
        ControlFlowStatementSummary inner =
                new ControlFlowStatementSummary("if", "c == 2", 2, null, null, 3, null, null);
        ControlFlowStatementSummary head =
                new ControlFlowStatementSummary("if", "a == 1", 1, null, 2, 3, null, inner);
        MethodSummary m =
                new MethodSummary(
                        "m", "void", List.of(), List.of(), List.of(head), List.of(), List.of());
        TypeSummary type =
                new TypeSummary("class", "T", false, null, List.of(), List.of(), List.of(m));
        DispatchAstHeuristicAnalyzer.Flags f =
                DispatchAstHeuristicAnalyzer.evaluate(type, DEFAULT_PARAMS);
        assertFalse(f.h1());
    }

    @Test
    void threeConsecutiveTopLevelIfs_onlyH2() {
        MethodSummary m =
                new MethodSummary(
                        "m",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(
                                new ControlFlowStatementSummary(
                                        "if", "a > 0", 1, null, null, 2, null, null),
                                new ControlFlowStatementSummary(
                                        "if", "b > 0", 3, null, null, 4, null, null),
                                new ControlFlowStatementSummary(
                                        "if", "c > 0", 5, null, null, 6, null, null)),
                        List.of(),
                        List.of());
        TypeSummary type =
                new TypeSummary("class", "T", false, null, List.of(), List.of(), List.of(m));
        DispatchAstHeuristicAnalyzer.Flags f =
                DispatchAstHeuristicAnalyzer.evaluate(type, DEFAULT_PARAMS);
        assertEquals(1, f.votes());
        assertFalse(f.h1());
        assertTrue(f.h2());
    }

    @Test
    void consecutiveThreshold_twoIfsDoesNotTriggerH2WhenMinIs3() {
        MethodSummary m =
                new MethodSummary(
                        "m",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(
                                new ControlFlowStatementSummary(
                                        "if", "a > 0", 1, null, null, 2, null, null),
                                new ControlFlowStatementSummary(
                                        "if", "b > 0", 3, null, null, 4, null, null)),
                        List.of(),
                        List.of());
        TypeSummary type =
                new TypeSummary("class", "T", false, null, List.of(), List.of(), List.of(m));
        var p2 = new DispatchAstHeuristicAnalyzer.DispatchAstParams(0.25, 3, 2);
        assertTrue(DispatchAstHeuristicAnalyzer.evaluate(type, p2).h2());
        assertFalse(DispatchAstHeuristicAnalyzer.evaluate(type, DEFAULT_PARAMS).h2());
    }

    @Test
    void extractDiscriminant_equalsAndEq() {
        assertTrue(
                DispatchAstHeuristicAnalyzer.extractDiscriminant("foo.equals(\"a\")")
                        .isPresent());
        assertEquals(
                "this.formality",
                DispatchAstHeuristicAnalyzer.extractDiscriminant("this.formality == \"x\"")
                        .orElseThrow());
        assertTrue(
                DispatchAstHeuristicAnalyzer.extractDiscriminant("Objects.equals(a, b)")
                        .isPresent());
    }

    @Test
    void formatDetail_nonEmptyWhenVotes() {
        String d =
                DispatchAstHeuristicAnalyzer.formatDetail(
                        2, true, true, DEFAULT_PARAMS);
        assertTrue(d.contains("H1"));
        assertTrue(d.contains("H2"));
        assertTrue(d.contains("DISPATCH_AST_HEURISTICS"));
    }
}
