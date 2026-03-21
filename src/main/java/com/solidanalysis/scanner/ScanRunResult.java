package com.solidanalysis.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregated outcome of scanning a project directory (not serialized to per-file JSON).
 */
public final class ScanRunResult {

    private int successCount;
    private int failureCount;
    private final List<String> failureMessages = new ArrayList<>();

    /** @return number of source files written successfully */
    public int getSuccessCount() {
        return successCount;
    }

    /** Increments the successful parse/write counter. */
    public void incrementSuccess() {
        successCount++;
    }

    /** @return number of source files that failed to parse or write */
    public int getFailureCount() {
        return failureCount;
    }

    /** Increments the per-file failure counter. */
    public void incrementFailure() {
        failureCount++;
    }

    /**
     * Records a single-line message for stdout (typically path + error).
     *
     * @param message human-readable failure description
     */
    public void addFailureMessage(String message) {
        failureMessages.add(message);
    }

    /** @return immutable view of failure lines emitted during the scan */
    public List<String> getFailureMessages() {
        return Collections.unmodifiableList(failureMessages);
    }
}
