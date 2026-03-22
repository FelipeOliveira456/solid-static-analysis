package com.solidanalysis.graphs;

import com.solidanalysis.graphs.io.ProjectJsonLoader;
import com.solidanalysis.graphs.model.ParsedProject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads AST JSON from {@code ast/} and writes graph DOT files under {@code graphs/}. */
public final class GraphGenerationRunner {

    /**
     * Writes {@code graphs/*.dot} under {@code projectJsonDirectory}.
     *
     * @param projectJsonDirectory project output root containing {@code ast/*.json} from the scanner
     */
    public void run(Path projectJsonDirectory) throws IOException {
        ParsedProject project = ProjectJsonLoader.loadParsed(projectJsonDirectory);
        Path graphs = projectJsonDirectory.resolve("graphs");
        Files.createDirectories(graphs);

        new G1DependencyGraphGenerator().write(graphs.resolve("g1_dependency.dot"), project);
        new G2InheritanceGraphGenerator().write(graphs.resolve("g2_inheritance.dot"), project);
        new G3MethodCallsGraphGenerator().writePerClass(graphs, project);
        G4FieldUsageGraphGenerator g4 = new G4FieldUsageGraphGenerator();
        g4.writeFieldUsagePerClass(graphs, project);
        g4.writeMethodProjectionPerClass(graphs, project);
        new G5InterfaceImplGraphGenerator().write(graphs.resolve("g5_interface_impl.dot"), project);
        new G6InterfaceUsageGraphGenerator().write(graphs.resolve("g6_interface_usage.dot"), project);
        new G7CfgGraphGenerator().writeAll(graphs, project);
    }
}
