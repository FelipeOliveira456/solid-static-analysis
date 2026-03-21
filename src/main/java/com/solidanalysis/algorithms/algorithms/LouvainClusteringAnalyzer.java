package com.solidanalysis.algorithms.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Weighted undirected Louvain community detection (Blondel et al.) with deterministic shuffles
 * derived from a fixed seed (FR-012). Returns one partition as {@code clusters} in JSON (automatic
 * {@code k}).
 */
public final class LouvainClusteringAnalyzer {

    /** Default seed when callers use a shared constant. */
    public static final long DEFAULT_CLUSTERING_SEED = 0x6C6F757661696EL; // "louvain"

    private LouvainClusteringAnalyzer() {}

    /**
     * @param undirectedWeighted symmetric {@code u -> (v -> weight)}; positive weights only; parallel
     *     arcs may be merged by summing
     * @param seed fixes {@link Collections#shuffle} of node visit order each sweep
     */
    public static List<List<String>> cluster(Map<String, Map<String, Double>> undirectedWeighted, long seed) {
        if (undirectedWeighted == null || undirectedWeighted.isEmpty()) {
            return List.of();
        }
        Graph g = Graph.from(undirectedWeighted);
        if (g.n == 0) {
            return List.of();
        }
        int[] com = recurse(g, new Random(seed), 0);
        return toSortedClusters(g.ids, com);
    }

    /**
     * Collapses directed arcs into a single lower/upper undirected weight per vertex pair (opposite
     * arcs sum).
     */
    public static Map<String, Map<String, Double>> undirectedWeightsFromDirected(
            Iterable<String> vertices,
            Iterable<org.jgrapht.graph.DefaultEdge> edges,
            java.util.function.Function<org.jgrapht.graph.DefaultEdge, String> source,
            java.util.function.Function<org.jgrapht.graph.DefaultEdge, String> target) {
        Map<String, Map<String, Double>> w = initializeVertexBuckets(vertices);
        for (org.jgrapht.graph.DefaultEdge e : edges) {
            String u = source.apply(e);
            String v = target.apply(e);
            if (u.equals(v)) {
                continue;
            }
            String a = u.compareTo(v) < 0 ? u : v;
            String b = u.compareTo(v) < 0 ? v : u;
            w.computeIfAbsent(a, k -> new TreeMap<>()).merge(b, 1.0, Double::sum);
        }
        return w;
    }

    /** One undirected weight {@code 1.0} per edge (canonical lower/upper string pair). */
    public static Map<String, Map<String, Double>> undirectedWeightsFromUndirected(
            Iterable<String> vertices,
            Iterable<org.jgrapht.graph.DefaultEdge> edges,
            java.util.function.Function<org.jgrapht.graph.DefaultEdge, String> source,
            java.util.function.Function<org.jgrapht.graph.DefaultEdge, String> target) {
        return undirectedWeightsFromDirected(vertices, edges, source, target);
    }

    private static Map<String, Map<String, Double>> initializeVertexBuckets(Iterable<String> vertices) {
        Map<String, Map<String, Double>> w = new TreeMap<>();
        for (String v : vertices) {
            w.putIfAbsent(v, new TreeMap<>());
        }
        return w;
    }

    private static List<List<String>> toSortedClusters(List<String> ids, int[] com) {
        Map<Integer, List<String>> by = new TreeMap<>();
        for (int i = 0; i < ids.size(); i++) {
            by.computeIfAbsent(com[i], k -> new ArrayList<>()).add(ids.get(i));
        }
        for (List<String> list : by.values()) {
            Collections.sort(list);
        }
        List<List<String>> out = new ArrayList<>(by.values());
        out.sort(Comparator.comparing(l -> l.isEmpty() ? "" : l.get(0)));
        return out;
    }

    /** Recursive Louvain: local moving, aggregate, recurse, lift. */
    private static int[] recurse(Graph g, Random rng, int sweepBase) {
        int[] com = singletonCommunities(g.n);
        phase1LocalMoving(g, com, rng, sweepBase);
        compressCommLabels(com);
        recomputeAggregates(g, com);
        Graph agg = aggregateGraph(g, com);
        if (agg == null || agg.n >= g.n) {
            return com;
        }
        int[] superCom = recurse(agg, rng, sweepBase + g.n);
        int[] lifted = new int[g.n];
        for (int i = 0; i < g.n; i++) {
            int s = com[i];
            lifted[i] = superCom[s];
        }
        return lifted;
    }

    /** Renumber community ids to {@code 0 .. K-1} so they match aggregate super-node indices. */
    private static void compressCommLabels(int[] com) {
        Set<Integer> uniq = new TreeSet<>();
        for (int c : com) {
            uniq.add(c);
        }
        Map<Integer, Integer> map = new LinkedHashMap<>();
        int k = 0;
        for (int c : uniq) {
            map.put(c, k++);
        }
        for (int i = 0; i < com.length; i++) {
            com[i] = map.get(com[i]);
        }
    }

    /** Rebuild {@link Graph#internalSum} / {@link Graph#totSum} from {@code com} (after relabel). */
    private static void recomputeAggregates(Graph g, int[] com) {
        g.internalSum.clear();
        g.totSum.clear();
        for (int i = 0; i < g.n; i++) {
            g.totSum.merge(com[i], g.deg[i], Double::sum);
        }
        for (int i = 0; i < g.n; i++) {
            for (Map.Entry<Integer, Double> e : g.neigh.get(i).entrySet()) {
                int j = e.getKey();
                if (com[i] != com[j]) {
                    continue;
                }
                double w = e.getValue();
                if (i < j) {
                    g.internalSum.merge(com[i], w, Double::sum);
                } else if (i == j) {
                    g.internalSum.merge(com[i], w, Double::sum);
                }
            }
        }
    }

    private static int[] singletonCommunities(int n) {
        int[] c = new int[n];
        for (int i = 0; i < n; i++) {
            c[i] = i;
        }
        return c;
    }

    private static void phase1LocalMoving(Graph g, int[] com, Random rng, int sweepBase) {
        if (g.m2 <= 1e-15) {
            return;
        }
        boolean moved = true;
        int sweep = 0;
        while (moved) {
            moved = false;
            List<Integer> order = new ArrayList<>(g.n);
            for (int i = 0; i < g.n; i++) {
                order.add(i);
            }
            long mix = rng.nextLong() ^ ((long) (sweepBase + sweep++) * 0x9E3779B97F4A7C15L);
            Collections.shuffle(order, new Random(mix));
            for (int i : order) {
                int oldC = com[i];
                removeNode(g, com, i, oldC);
                Set<Integer> candidates = new TreeSet<>();
                for (Map.Entry<Integer, Double> e : g.neigh.get(i).entrySet()) {
                    candidates.add(com[e.getKey()]);
                }
                candidates.add(oldC);
                int best = oldC;
                double bestGain = 0.0;
                for (int cand : candidates) {
                    double dq = deltaAdd(g, com, i, cand);
                    if (dq > bestGain + 1e-15
                            || (Math.abs(dq - bestGain) <= 1e-15 && cand < best)) {
                        bestGain = dq;
                        best = cand;
                    }
                }
                if (bestGain > 1e-15 || !g.totSum.containsKey(oldC)) {
                    addNode(g, com, i, best);
                    if (best != oldC) {
                        moved = true;
                    }
                } else {
                    addNode(g, com, i, oldC);
                }
            }
        }
    }

    private static double deltaAdd(Graph g, int[] com, int i, int target) {
        double kiC = 0.0;
        for (Map.Entry<Integer, Double> e : g.neigh.get(i).entrySet()) {
            if (com[e.getKey()] == target) {
                kiC += e.getValue();
            }
        }
        double inC = g.internalSum.getOrDefault(target, 0.0);
        double totC = g.totSum.getOrDefault(target, 0.0);
        double m = g.m2 / 2.0;
        if (m <= 0.0) {
            return 0.0;
        }
        double termNew = (inC + kiC) / m - Math.pow((totC + g.deg[i]) / g.m2, 2);
        double termOld = inC / m - Math.pow(totC / g.m2, 2);
        return termNew - termOld;
    }

    private static void removeNode(Graph g, int[] com, int i, int c) {
        double kiIn = 0.0;
        for (Map.Entry<Integer, Double> e : g.neigh.get(i).entrySet()) {
            int j = e.getKey();
            if (com[j] == c) {
                kiIn += e.getValue();
            }
        }
        g.internalSum.put(c, g.internalSum.getOrDefault(c, 0.0) - kiIn);
        g.totSum.put(c, g.totSum.getOrDefault(c, 0.0) - g.deg[i]);
        if (g.totSum.get(c) <= 1e-12) {
            g.internalSum.remove(c);
            g.totSum.remove(c);
        }
        com[i] = -1;
    }

    private static void addNode(Graph g, int[] com, int i, int c) {
        double kiIn = 0.0;
        for (Map.Entry<Integer, Double> e : g.neigh.get(i).entrySet()) {
            int j = e.getKey();
            if (com[j] == c) {
                kiIn += e.getValue();
            }
        }
        g.internalSum.put(c, g.internalSum.getOrDefault(c, 0.0) + kiIn);
        g.totSum.put(c, g.totSum.getOrDefault(c, 0.0) + g.deg[i]);
        com[i] = c;
    }

    /** Build aggregated graph: one super-node per distinct community id in {@code com}. */
    private static Graph aggregateGraph(Graph g, int[] com) {
        Map<Integer, Set<Integer>> members = new TreeMap<>();
        for (int i = 0; i < g.n; i++) {
            members.computeIfAbsent(com[i], k -> new TreeSet<>()).add(i);
        }
        List<Integer> commIds = new ArrayList<>(members.keySet());
        if (commIds.size() >= g.n) {
            return null;
        }
        Map<Integer, Integer> idx = new LinkedHashMap<>();
        for (int i = 0; i < commIds.size(); i++) {
            idx.put(commIds.get(i), i);
        }
        int n2 = commIds.size();
        double[][] w = new double[n2][n2];
        // Count each undirected edge once (i < j) so internal weights are not doubled.
        for (int i = 0; i < g.n; i++) {
            for (Map.Entry<Integer, Double> e : g.neigh.get(i).entrySet()) {
                int j = e.getKey();
                if (j <= i) {
                    continue;
                }
                int ci = idx.get(com[i]);
                int cj = idx.get(com[j]);
                double wt = e.getValue();
                if (ci == cj) {
                    w[ci][ci] += wt;
                } else {
                    int a = Math.min(ci, cj);
                    int b = Math.max(ci, cj);
                    w[a][b] += wt;
                }
            }
        }
        List<String> newIds = new ArrayList<>(n2);
        List<Map<Integer, Double>> newNeigh = new ArrayList<>(n2);
        double sumTwice = 0.0;
        for (int a = 0; a < n2; a++) {
            Set<Integer> grp = members.get(commIds.get(a));
            List<String> parts = new ArrayList<>();
            for (int x : grp) {
                parts.add(g.ids.get(x));
            }
            Collections.sort(parts);
            newIds.add(String.join("|", parts));
            newNeigh.add(new LinkedHashMap<>());
        }
        for (int a = 0; a < n2; a++) {
            for (int b = a + 1; b < n2; b++) {
                double wt = w[a][b];
                if (wt > 1e-15) {
                    newNeigh.get(a).put(b, wt);
                    newNeigh.get(b).put(a, wt);
                    sumTwice += 2.0 * wt;
                }
            }
            double loop = w[a][a];
            if (loop > 1e-15) {
                newNeigh.get(a).put(a, loop);
                // Self-loop of weight L contributes 2L to total degree.
                sumTwice += 2.0 * loop;
            }
        }
        double[] newDeg = new double[n2];
        for (int a = 0; a < n2; a++) {
            double s = 0.0;
            for (Map.Entry<Integer, Double> e : newNeigh.get(a).entrySet()) {
                int t = e.getKey();
                double x = e.getValue();
                s += (t == a) ? 2.0 * x : x;
            }
            newDeg[a] = s;
        }
        if (sumTwice <= 1e-15) {
            return null;
        }
        return new Graph(newIds, newNeigh, newDeg, sumTwice);
    }

    private static final class Graph {
        final List<String> ids;
        final int n;
        final List<Map<Integer, Double>> neigh;
        final double[] deg;
        final double m2;
        final Map<Integer, Double> internalSum = new LinkedHashMap<>();
        final Map<Integer, Double> totSum = new LinkedHashMap<>();

        Graph(List<String> ids, List<Map<Integer, Double>> neigh, double[] deg, double m2) {
            this.ids = ids;
            this.n = ids.size();
            this.neigh = neigh;
            this.deg = deg;
            this.m2 = m2;
            for (int i = 0; i < n; i++) {
                internalSum.put(i, 0.0);
                totSum.put(i, deg[i]);
            }
        }

        static Graph from(Map<String, Map<String, Double>> w) {
            Set<String> vs = new TreeSet<>();
            for (Map.Entry<String, Map<String, Double>> e : w.entrySet()) {
                vs.add(e.getKey());
                if (e.getValue() != null) {
                    vs.addAll(e.getValue().keySet());
                }
            }
            List<String> id = new ArrayList<>(vs);
            Map<String, Integer> ix = new LinkedHashMap<>();
            for (int i = 0; i < id.size(); i++) {
                ix.put(id.get(i), i);
            }
            int n = id.size();
            List<Map<Integer, Double>> neigh = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                neigh.add(new LinkedHashMap<>());
            }
            for (Map.Entry<String, Map<String, Double>> e : w.entrySet()) {
                String u = e.getKey();
                Integer iu = ix.get(u);
                if (iu == null || e.getValue() == null) {
                    continue;
                }
                for (Map.Entry<String, Double> f : e.getValue().entrySet()) {
                    String v = f.getKey();
                    Integer iv = ix.get(v);
                    if (iv == null || iu.equals(iv)) {
                        continue;
                    }
                    double wt = f.getValue();
                    if (wt <= 0.0 || Double.isNaN(wt)) {
                        continue;
                    }
                    int a = Math.min(iu, iv);
                    int b = Math.max(iu, iv);
                    neigh.get(a).merge(b, wt, Double::sum);
                }
            }
            double sumTwice = 0.0;
            for (int i = 0; i < n; i++) {
                for (Map.Entry<Integer, Double> e : neigh.get(i).entrySet()) {
                    int j = e.getKey();
                    if (i >= j) {
                        continue;
                    }
                    double wt = e.getValue();
                    neigh.get(j).put(i, wt);
                    sumTwice += 2.0 * wt;
                }
            }
            double[] deg = new double[n];
            for (int i = 0; i < n; i++) {
                double s = 0.0;
                for (double x : neigh.get(i).values()) {
                    s += x;
                }
                deg[i] = s;
            }
            return new Graph(id, neigh, deg, sumTwice);
        }
    }
}
