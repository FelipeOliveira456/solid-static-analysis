package com.solidanalysis.algorithms.runners;

/**
 * Options for {@link GraphAlgorithmsRunner}. Louvain clustering is optional (CLI {@code
 * --clustering}); the boolean name is kept for binary compatibility with {@code
 * SolidAnalysisCli}.
 */
public final class GraphAnalyzeOptions {

    private final boolean girvanNewmanClustering;
    /** Legacy no-op; retained for two-arg constructor calls (second value ignored). */
    @SuppressWarnings("unused")
    private final int girvanKUpperBoundCap;

    public GraphAnalyzeOptions(boolean girvanNewmanClustering) {
        this(girvanNewmanClustering, Integer.MAX_VALUE);
    }

    public GraphAnalyzeOptions(boolean girvanNewmanClustering, int girvanKUpperBoundCap) {
        this.girvanNewmanClustering = girvanNewmanClustering;
        this.girvanKUpperBoundCap = girvanKUpperBoundCap;
    }

    /** Default: metrics only, no Louvain clustering. */
    public static GraphAnalyzeOptions defaults() {
        return new GraphAnalyzeOptions(false);
    }

    /** @return whether to run Louvain on G1, G3, and G4 projection */
    public boolean isGirvanNewmanClustering() {
        return girvanNewmanClustering;
    }

    /** @deprecated Louvain discovers {@code k}; this value is ignored. */
    @Deprecated
    public int getGirvanKUpperBoundCap() {
        return girvanKUpperBoundCap;
    }
}
