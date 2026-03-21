package com.solidanalysis.algorithms.model;

import java.util.List;
import java.util.Map;

public class G1AlgorithmsDocument {

    public List<List<String>> stronglyConnectedComponents;
    public Map<String, Integer> inDegree;
    public Map<String, Integer> outDegree;
    public Map<String, Double> degreeCentrality;
    public Map<String, Double> inCentrality;
    public Map<String, Double> outCentrality;
    /** Louvain communities (empty when clustering is disabled). */
    public List<List<String>> clusters;
}
