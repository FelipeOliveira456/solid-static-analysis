package com.solidanalysis.scoring;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Fixed indicator templates with interpolated {@code detail} strings for JSON output.
 */
public enum IndicatorTemplate {
    LCOM_VALUE,
    PROJECTION_CLUSTERS,
    ISOLATED_METHODS_RATIO,
    G3_SCC_CYCLE,
    SWITCH_CASES,
    EXTENDS_CONCRETE,
    G1_CYCLE,
    INHERITANCE_DEPTH,
    CONCRETE_SUBCLASS_COUNT,
    /** In-degree of a concrete class in the inheritance graph (G2). */
    CONCRETE_CLASS_INDEGREE,
    IMPLEMENTS_COUNT,
    INTERFACE_ZERO_INDEGREE_IMPL,
    INTERFACE_ZERO_INDEGREE_USAGE,
    INSTANTIATION_COUNT,
    OUT_DEGREE_NORMALIZED,
    CONCRETE_DEPENDENCY_RATIO,
    G1_OUT_CENTRALITY;

    public String jsonName() {
        return name();
    }

    public static String formatLcomValue(double lcom, int pct) {
        return String.format(Locale.US, "LCOM %s — %d%% dos pares de métodos sem atributo em comum", formatDouble(lcom), pct);
    }

    public static String formatProjectionClusters(int clusters) {
        if (clusters <= 1) {
            return clusters
                    + " "
                    + plural(clusters, "cluster", "clusters")
                    + " de métodos na projeção — baixa evidência de responsabilidades separadas";
        }
        return clusters
                + " clusters de métodos na projeção — possível divisão de responsabilidades";
    }

    public static String formatIsolatedMethodsRatio(int isolated, int total, int pct) {
        if (total == 0) {
            return "sem métodos analisáveis para medir isolamento";
        }
        return isolated
                + " de "
                + total
                + " métodos isolados ("
                + pct
                + "%) — sem colaboração interna";
    }

    public static String formatG3SccCycle(List<String> methods) {
        return "ciclo de chamadas entre métodos: "
                + String.join(", ", methods)
                + " — responsabilidades entrelaçadas";
    }

    public static String formatSwitchCases(int value, String method) {
        String where = method == null || method.isBlank() ? "método desconhecido" : method;
        String suffix =
                value <= 2
                        ? "baixa ramificação"
                        : (value <= 4
                                ? "ramificação moderada; monitorar evolução"
                                : "alta ramificação — candidato a polimorfismo");
        return "switch com " + value + " " + plural(value, "case", "cases") + " em " + where + " — " + suffix;
    }

    public static String formatExtendsConcrete(String superclass) {
        return "herda de classe concreta " + superclass + " — difícil estender sem modificar";
    }

    public static String formatG1Cycle(List<String> classes) {
        return "dependência circular com "
                + String.join(", ", classes)
                + " — mudança propagada em loop";
    }

    public static String formatInheritanceDepth(int value, String className) {
        return "profundidade de herança da classe " + className + ": " + value;
    }

    public static String formatConcreteSubclassCount(int value, String className) {
        return value
                + " subclasses de classe concreta "
                + className
                + " — risco de quebra de contrato";
    }

    public static String formatConcreteClassIndegree(int value) {
        String suffix =
                value <= 1
                        ? "baixo acoplamento por herança"
                        : (value <= 3 ? "acoplamento moderado por herança" : "acoplamento alto por herança");
        return value + " referência" + (value == 1 ? "" : "s") + " como superclasse concreta (G2) — " + suffix;
    }

    public static String formatImplementsCount(int value) {
        if (value <= 1) {
            return "implementa " + value + " interface" + (value == 1 ? "" : "s") + " — escopo enxuto";
        }
        if (value <= 3) {
            return "implementa " + value + " interfaces — atenção para coesão da abstração";
        }
        return "implementa " + value + " interfaces — possível violação de Interface Segregation";
    }

    public static String formatInterfaceZeroIndegreeImpl(String iface) {
        return "interface " + iface + " declarada mas nunca implementada";
    }

    public static String formatInterfaceZeroIndegreeUsage(String iface) {
        return "interface " + iface + " nunca usada como tipo";
    }

    public static String formatInstantiationCount(int value, List<String> classes) {
        if (value == 0) {
            return "não instancia classes concretas via new";
        }
        String list =
                classes.stream().distinct().sorted().collect(Collectors.joining(", "));
        if (list.isBlank()) {
            return "instancia diretamente " + value + " classes concretas via new";
        }
        return "instancia diretamente "
                + value
                + " classes concretas via new: "
                + list;
    }

    public static String formatOutDegreeNormalized(int value, int total, int pct) {
        if (total <= 0) {
            return "grafo sem nós suficientes para calcular dependências de saída";
        }
        String suffix =
                pct < 30
                        ? "baixo espalhamento de dependências"
                        : (pct < 60
                                ? "espalhamento moderado de dependências"
                                : "alto espalhamento de dependências");
        return "depende de "
                + value
                + " de "
                + total
                + " classes do projeto ("
                + pct
                + "%) — "
                + suffix;
    }

    public static String formatConcreteDependencyRatio(int pct, int concrete, int total) {
        if (total == 0) {
            return "sem dependências de saída para medir razão entre concreto e abstração";
        }
        String suffix =
                pct < 20
                        ? "predominam abstrações"
                        : (pct < 50
                                ? "mistura equilibrada entre concreto e abstração"
                                : "predominam dependências concretas");
        return pct
                + "% das dependências são classes concretas — "
                + concrete
                + " de "
                + total
                + " ("
                + suffix
                + ")";
    }

    public static String formatG1OutCentrality(double value) {
        String suffix =
                value < 0.5
                        ? "baixo alcance de dependências"
                        : (value < 0.85
                                ? "alcance moderado de dependências"
                                : "classe conhece grande parte do projeto");
        return String.format(Locale.US, "out-centrality %s — %s", formatDouble(value), suffix);
    }

    private static String plural(int n, String singular, String plural) {
        return n == 1 ? singular : plural;
    }

    private static String formatDouble(double d) {
        if (d == (long) d) {
            return String.format(Locale.US, "%d", (long) d);
        }
        return String.format(Locale.US, "%.4g", d);
    }
}
