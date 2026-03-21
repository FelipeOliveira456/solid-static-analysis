package com.solidanalysis.algorithms.model;

import java.util.List;

public class G4ProjectionAlgorithmsDocument {

    public List<List<String>> connectedComponents;
    public List<String> isolatedNodes;
    /** Louvain communities (empty when clustering is disabled). */
    public List<List<String>> clusters;
}
