package com.solidanalysis.scoring;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes human-readable scoring reports under {@code results/} next to {@code scoring/} JSON
 * outputs (Etapa 5).
 */
public final class HumanReadableResultsWriter {

    /** Separator line length matches contract examples. */
    static final String SEPARATOR = "=".repeat(80);

    /** Class report: principle blocks in this order. */
    private static final List<String> CLASS_REPORT_PRINCIPLE_ORDER = List.of("O", "S", "L", "I", "D");

    /** Project summary: distribution lines in this order (contract). */
    private static final List<String> SUMMARY_DISTRIBUTION_ORDER = List.of("S", "O", "L", "I", "D");

    public HumanReadableResultsWriter() {}

    /**
     * Maps aggregate principle risk to the symbol used in indicator lines and legend.
     *
     * @param level principle risk level
     * @return {@code [!]}, {@code [~]}, or {@code [ok]}
     */
    public static String riskSymbol(ScoreLevel level) {
        return switch (level) {
            case ALTO -> "[!]";
            case MEDIO -> "[~]";
            case BAIXO -> "[ok]";
        };
    }

    /**
     * Full title line for a principle block in a per-class report, e.g. {@code O — Open/Closed:
     * BAIXO}.
     */
    public static String principleSectionHeader(String letter, ScoreLevel level) {
        return letter + " — " + principleDisplayName(letter) + ": " + level.name();
    }

    /**
     * Writes {@code results/<className>.txt} for each {@link ClassScore}.
     *
     * @param resultsDir project output {@code results/} directory (created if missing)
     * @param classes scores produced for this project
     */
    public void writeClassReports(Path resultsDir, List<ClassScore> classes) throws IOException {
        Files.createDirectories(resultsDir);
        for (ClassScore cs : classes) {
            writeOneClassReport(resultsDir, cs);
        }
    }

    /**
     * Writes {@code results/project_summary.txt} from the consolidated JSON model.
     *
     * @param resultsDir project output {@code results/} directory (created if missing)
     * @param summary value returned by {@link ProjectSummaryBuilder#build}
     */
    public void writeProjectSummary(Path resultsDir, ProjectSummary summary) throws IOException {
        Files.createDirectories(resultsDir);
        Path out = resultsDir.resolve("project_summary.txt");
        try (PrintWriter pw =
                new PrintWriter(Files.newBufferedWriter(out, StandardCharsets.UTF_8))) {
            pw.println(SEPARATOR);
            pw.println("RESUMO DO PROJETO: " + summary.projectPath());
            pw.println("ESTRATÉGIA DE CLASSIFICAÇÃO: " + summary.classificationStrategy().name());
            pw.print("PRINCÍPIO MAIS VIOLADO: ");
            String mv = summary.mostViolatedPrinciple();
            if (mv == null) {
                pw.println("nenhum (sem ALTO por princípio)");
            } else {
                pw.println(mv + " (" + principleDisplayName(mv) + ")");
            }
            pw.println(SEPARATOR);
            pw.println();
            pw.println("DISTRIBUIÇÃO POR PRINCÍPIO:");
            int labelWidth = maxDistributionLabelWidth();
            for (String letter : SUMMARY_DISTRIBUTION_ORDER) {
                Map<String, Integer> row = summary.principleDistribution().get(letter);
                String label = letter + " — " + principleDisplayName(letter) + ":";
                pw.printf(
                        Locale.US,
                        "  %-" + labelWidth + "s  ALTO: %d  MEDIO: %d  BAIXO: %d%n",
                        label,
                        row.get("ALTO"),
                        row.get("MEDIO"),
                        row.get("BAIXO"));
            }
            pw.println();
            pw.println("RANKING DE CLASSES (pior para melhor):");
            int i = 1;
            for (ProjectSummary.RankingEntry e : summary.ranking()) {
                String worstPart = formatWorstPart(e.worst());
                pw.printf(
                        Locale.US,
                        "  %d. %-28s %-6s %s%n",
                        i++,
                        e.displayLabel(),
                        e.overall().name(),
                        worstPart);
            }
        }
    }

    private static void writeOneClassReport(Path resultsDir, ClassScore cs) throws IOException {
        Path dir = resultsDir;
        if (!cs.relativePath().isEmpty()) {
            dir = resultsDir.resolve(cs.relativePath());
        }
        Files.createDirectories(dir);
        Path out = dir.resolve(cs.className() + ".txt");
        try (PrintWriter pw =
                new PrintWriter(Files.newBufferedWriter(out, StandardCharsets.UTF_8))) {
            pw.println(SEPARATOR);
            pw.println("CLASSE: " + cs.displayClassLabel());
            pw.println("PROJETO: " + cs.projectPath());
            pw.println("SCORE GERAL: " + cs.overall().name());
            pw.println(formatStrategyLine(cs.classificationStrategy(), cs.relaxFactor()));
            pw.println(SEPARATOR);
            pw.println();
            for (String letter : CLASS_REPORT_PRINCIPLE_ORDER) {
                PrincipleScore ps = cs.scores().get(letter);
                if (ps == null) {
                    continue;
                }
                pw.println(principleSectionHeader(letter, ps.score()));
                String sym = riskSymbol(ps.score());
                if (ps.indicators().isEmpty()) {
                    pw.println("  " + sym + " sem indicadores listados");
                } else {
                    for (IndicatorResult ir : ps.indicators()) {
                        pw.println("  " + sym + " " + ir.detail());
                    }
                }
                pw.println();
            }
            writeLegend(pw);
        }
    }

    static String formatStrategyLine(ScoringStrategy strategy, double relaxFactor) {
        if (strategy == ScoringStrategy.FIXED_THRESHOLD_RELAXED) {
            return String.format(
                    Locale.US,
                    "ESTRATÉGIA: %s (fator de relaxamento: %.4f)",
                    strategy.name(),
                    relaxFactor);
        }
        return "ESTRATÉGIA: " + strategy.name();
    }

    private static void writeLegend(PrintWriter pw) {
        pw.println("Legenda de símbolos:");
        pw.println("[!]  ALTO  — risco elevado, requer atenção");
        pw.println("[~]  MEDIO — risco moderado, avaliar");
        pw.println("[ok] BAIXO — sem risco identificado");
    }

    static String principleDisplayName(String letter) {
        return switch (letter) {
            case "S" -> "Single Responsibility";
            case "O" -> "Open/Closed";
            case "L" -> "Liskov Substitution";
            case "I" -> "Interface Segregation";
            case "D" -> "Dependency Inversion";
            default -> letter;
        };
    }

    private static int maxDistributionLabelWidth() {
        int w = 0;
        for (String letter : SUMMARY_DISTRIBUTION_ORDER) {
            String label = letter + " — " + principleDisplayName(letter) + ":";
            w = Math.max(w, label.length());
        }
        return w;
    }

    private static String formatWorstPart(String worstLetter) {
        if (worstLetter == null) {
            return "(pior: —)";
        }
        return "(pior: " + worstLetter + " — " + principleDisplayName(worstLetter) + ")";
    }
}
