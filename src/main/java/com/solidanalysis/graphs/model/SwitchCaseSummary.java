package com.solidanalysis.graphs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One arm of a {@code switch} in control-flow metadata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwitchCaseSummary(String label, Integer line) {}
