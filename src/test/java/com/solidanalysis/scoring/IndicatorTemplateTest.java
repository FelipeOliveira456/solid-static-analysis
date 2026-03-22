package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IndicatorTemplateTest {

    @Test
    void formatLcomValueContainsParts() {
        String s = IndicatorTemplate.formatLcomValue(0.42, 33);
        assertTrue(s.contains("LCOM"));
        assertTrue(s.contains("33%"));
    }

    @Test
    void formatSwitchCasesMentionsMethod() {
        String s = IndicatorTemplate.formatSwitchCases(5, "foo()");
        assertTrue(s.contains("switch"));
        assertTrue(s.contains("foo()"));
    }

    @Test
    void formatInstantiationCountListsClassesSorted() {
        String s = IndicatorTemplate.formatInstantiationCount(2, List.of("B", "A"));
        assertTrue(s.contains("A, B"));
        assertTrue(s.contains("new"));
    }

    @Test
    void formatG1OutCentrality() {
        String s = IndicatorTemplate.formatG1OutCentrality(0.75);
        assertTrue(s.contains("out-centrality"));
    }
}
