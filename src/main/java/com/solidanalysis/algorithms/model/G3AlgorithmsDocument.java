package com.solidanalysis.algorithms.model;

import java.util.List;
import java.util.Map;

public class G3AlgorithmsDocument {

    public List<List<String>> stronglyConnectedComponents;
    public Map<String, Integer> inDegree;
    public Map<String, Integer> outDegree;
    public List<String> isolatedNodes;
    /** Louvain communities (empty when clustering is disabled). */
    public List<List<String>> clusters;
}
