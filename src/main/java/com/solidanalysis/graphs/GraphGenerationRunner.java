package com.solidanalysis.graphs;

import com.solidanalysis.graphs.io.ProjectJsonLoader;
import com.solidanalysis.graphs.model.ParsedProject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads JSON artifacts and writes all graph DOT files next to them. */
public final class GraphGenerationRunner {

    /**
     * Writes {@code graphs/*.dot} under {@code projectJsonDirectory}.
     *
     * @param projectJsonDirectory directory containing {@code *.json} from the scanner
     */
    public void run(Path projectJsonDirectory) throws IOException {
        ParsedProject project = ProjectJsonLoader.loadParsed(projectJsonDirectory);
        Path graphs = projectJsonDirectory.resolve("graphs");
        Files.createDirectories(graphs);
        Path g7 = graphs.resolve("g7_cfg");
        Files.createDirectories(g7);

        new G1DependencyGraphGenerator().write(graphs.resolve("g1_dependency.dot"), project);
        new G2InheritanceGraphGenerator().write(graphs.resolve("g2_inheritance.dot"), project);
        Path g3 = graphs.resolve("g3_method_calls");
        Files.createDirectories(g3);
        new G3MethodCallsGraphGenerator().writePerClass(g3, project);
        G4FieldUsageGraphGenerator g4 = new G4FieldUsageGraphGenerator();
        Path g4Field = graphs.resolve("g4_field_usage");
        Path g4Proj = graphs.resolve("g4_method_projection");
        Files.createDirectories(g4Field);
        Files.createDirectories(g4Proj);
        g4.writeFieldUsagePerClass(g4Field, project);
        g4.writeMethodProjectionPerClass(g4Proj, project);
        new G5InterfaceImplGraphGenerator().write(graphs.resolve("g5_interface_impl.dot"), project);
        new G6InterfaceUsageGraphGenerator().write(graphs.resolve("g6_interface_usage.dot"), project);
        new G7CfgGraphGenerator().writeAll(g7, project);
    }
}
