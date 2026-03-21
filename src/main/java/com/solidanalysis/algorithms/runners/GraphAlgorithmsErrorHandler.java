package com.solidanalysis.algorithms.runners;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Collects warnings for missing inputs or recoverable issues during graph analysis. */
public final class GraphAlgorithmsErrorHandler {

    private final List<String> warnings = new ArrayList<>();

    public void warn(String message) {
        warnings.add(message);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public void printWarnings(PrintStream err) {
        for (String w : warnings) {
            err.println(w);
        }
    }
}
