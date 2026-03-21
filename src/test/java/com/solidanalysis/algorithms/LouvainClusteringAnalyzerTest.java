package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.algorithms.algorithms.LouvainClusteringAnalyzer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.junit.jupiter.api.Test;

class LouvainClusteringAnalyzerTest {

    @Test
    void separatesTwoDenseCliquesWithSingleBridge() {
        Multigraph<String, DefaultEdge> g = new Multigraph<>(DefaultEdge.class);
        for (String v : new String[] {"a1", "a2", "a3", "b1", "b2", "b3"}) {
            g.addVertex(v);
        }
        g.addEdge("a1", "a2");
        g.addEdge("a2", "a3");
        g.addEdge("a3", "a1");
        g.addEdge("b1", "b2");
        g.addEdge("b2", "b3");
        g.addEdge("b3", "b1");
        g.addEdge("a1", "b1");
        var w =
                LouvainClusteringAnalyzer.undirectedWeightsFromUndirected(
                        g.vertexSet(), g.edgeSet(), g::getEdgeSource, g::getEdgeTarget);
        List<List<String>> c =
                LouvainClusteringAnalyzer.cluster(w, LouvainClusteringAnalyzer.DEFAULT_CLUSTERING_SEED);
        assertEquals(2, c.size());
        Set<String> all = new HashSet<>();
        for (List<String> cluster : c) {
            assertTrue(cluster.size() == 3, cluster::toString);
            all.addAll(cluster);
        }
        assertEquals(Set.of("a1", "a2", "a3", "b1", "b2", "b3"), all);
    }

    @Test
    void sameInputAndSeedYieldsIdenticalPartition() {
        Multigraph<String, DefaultEdge> g = new Multigraph<>(DefaultEdge.class);
        for (String v : new String[] {"a1", "a2", "a3", "b1", "b2", "b3"}) {
            g.addVertex(v);
        }
        g.addEdge("a1", "a2");
        g.addEdge("a2", "a3");
        g.addEdge("a3", "a1");
        g.addEdge("b1", "b2");
        g.addEdge("b2", "b3");
        g.addEdge("b3", "b1");
        g.addEdge("a1", "b1");
        var w =
                LouvainClusteringAnalyzer.undirectedWeightsFromUndirected(
                        g.vertexSet(), g.edgeSet(), g::getEdgeSource, g::getEdgeTarget);
        long seed = LouvainClusteringAnalyzer.DEFAULT_CLUSTERING_SEED;
        List<List<String>> c1 = LouvainClusteringAnalyzer.cluster(w, seed);
        List<List<String>> c2 = LouvainClusteringAnalyzer.cluster(w, seed);
        assertEquals(c1, c2);
    }

    @Test
    void includesIsolatedVerticesAsSingletonCommunities() {
        Multigraph<String, DefaultEdge> g = new Multigraph<>(DefaultEdge.class);
        for (String v : new String[] {"a1", "a2", "iso1", "iso2"}) {
            g.addVertex(v);
        }
        g.addEdge("a1", "a2");
        var w =
                LouvainClusteringAnalyzer.undirectedWeightsFromUndirected(
                        g.vertexSet(), g.edgeSet(), g::getEdgeSource, g::getEdgeTarget);
        List<List<String>> clusters =
                LouvainClusteringAnalyzer.cluster(w, LouvainClusteringAnalyzer.DEFAULT_CLUSTERING_SEED);
        Set<String> all = new HashSet<>();
        boolean hasIso1Singleton = false;
        boolean hasIso2Singleton = false;
        for (List<String> cluster : clusters) {
            all.addAll(cluster);
            if (cluster.size() == 1 && "iso1".equals(cluster.get(0))) {
                hasIso1Singleton = true;
            }
            if (cluster.size() == 1 && "iso2".equals(cluster.get(0))) {
                hasIso2Singleton = true;
            }
        }
        assertEquals(Set.of("a1", "a2", "iso1", "iso2"), all);
        assertTrue(hasIso1Singleton);
        assertTrue(hasIso2Singleton);
    }
}
