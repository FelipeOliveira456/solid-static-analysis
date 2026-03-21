package com.solidanalysis.scanner;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.solidanalysis.scanner.model.AstArtifact;
import com.solidanalysis.scanner.model.ControlFlowStatementSummary;
import com.solidanalysis.scanner.model.FieldAccessSummary;
import com.solidanalysis.scanner.model.FieldSummary;
import com.solidanalysis.scanner.model.InstantiationSummary;
import com.solidanalysis.scanner.model.MethodCallSummary;
import com.solidanalysis.scanner.model.MethodSummary;
import com.solidanalysis.scanner.model.ParameterSummary;
import com.solidanalysis.scanner.model.SwitchCaseSummary;
import com.solidanalysis.scanner.model.TypeSummary;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps a {@link CompilationUnit} to the {@link AstArtifact} export model.
 */
public class AstExtractor {

    private record FlowEntry(int line, int col, ControlFlowStatementSummary summary)
            implements Comparable<FlowEntry> {
        @Override
        public int compareTo(FlowEntry o) {
            int c = Integer.compare(line, o.line);
            if (c != 0) {
                return c;
            }
            return Integer.compare(col, o.col);
        }
    }

    private record FieldAccessPos(int line, int col, FieldAccessSummary summary)
            implements Comparable<FieldAccessPos> {
        @Override
        public int compareTo(FieldAccessPos o) {
            int c = Integer.compare(line, o.line);
            if (c != 0) {
                return c;
            }
            return Integer.compare(col, o.col);
        }
    }

    private record MethodLikePos(int line, int col, MethodCallSummary summary)
            implements Comparable<MethodLikePos> {
        @Override
        public int compareTo(MethodLikePos o) {
            int c = Integer.compare(line, o.line);
            if (c != 0) {
                return c;
            }
            return Integer.compare(col, o.col);
        }
    }

    private record InstantiationPos(int line, int col, InstantiationSummary summary)
            implements Comparable<InstantiationPos> {
        @Override
        public int compareTo(InstantiationPos o) {
            int c = Integer.compare(line, o.line);
            if (c != 0) {
                return c;
            }
            return Integer.compare(col, o.col);
        }
    }

    /**
     * Extracts structured data for the first top-level class or interface in the compilation unit.
     *
     * @param cu parsed compilation unit (must have been parsed with symbol resolution configured)
     * @param sourceFile absolute path to the corresponding {@code .java} file
     */
    public AstArtifact extract(CompilationUnit cu, Path sourceFile) {
        ClassOrInterfaceDeclaration typeDecl =
                cu.findFirst(ClassOrInterfaceDeclaration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No class or interface declaration in " + sourceFile));

        TypeSummary summary = new TypeSummary();
        summary.setKind(typeDecl.isInterface() ? "interface" : "class");
        summary.setName(typeDecl.getNameAsString());
        summary.setAbstractType(typeDecl.isInterface() || typeDecl.isAbstract());

        if (!typeDecl.getExtendedTypes().isEmpty()) {
            summary.setSuperclass(typeDecl.getExtendedTypes().get(0).getNameAsString());
        } else {
            summary.setSuperclass(null);
        }

        List<String> interfaces = new ArrayList<>();
        typeDecl.getImplementedTypes().forEach(t -> interfaces.add(t.getNameAsString()));
        summary.setImplementedInterfaces(interfaces);

        List<FieldSummary> fields = new ArrayList<>();
        for (FieldDeclaration fd : typeDecl.getFields()) {
            fd.getVariables()
                    .forEach(
                            v ->
                                    fields.add(
                                            new FieldSummary(
                                                    v.getNameAsString(),
                                                    fd.getElementType().asString())));
        }
        summary.setFields(fields);

        List<MethodSummary> methods = new ArrayList<>();
        for (MethodDeclaration md : typeDecl.getMethods()) {
            methods.add(extractExecutableSummary(md.getNameAsString(), md.getType().asString(), md.getParameters(), md.getBody().orElse(null)));
        }
        for (ConstructorDeclaration cd : typeDecl.getConstructors()) {
            methods.add(extractExecutableSummary(typeDecl.getNameAsString(), "<init>", cd.getParameters(), cd.getBody()));
        }
        summary.setMethods(methods);

        AstArtifact artifact = new AstArtifact();
        artifact.setSourceFile(sourceFile.toAbsolutePath().normalize().toString());
        artifact.setPrimaryType(summary);
        return artifact;
    }

    private static MethodSummary extractExecutableSummary(
            String name, String returnType, com.github.javaparser.ast.NodeList<Parameter> parameters, BlockStmt body) {
        MethodSummary ms = new MethodSummary();
        ms.setName(name);
        ms.setReturnType(returnType);

        List<ParameterSummary> params = new ArrayList<>();
        for (Parameter p : parameters) {
            params.add(new ParameterSummary(p.getNameAsString(), p.getType().asString()));
        }
        ms.setParameters(params);

        List<MethodCallSummary> calls = new ArrayList<>();
        List<ControlFlowStatementSummary> flow = new ArrayList<>();
        List<FieldAccessSummary> fieldAccesses = new ArrayList<>();
        List<InstantiationSummary> instantiations = new ArrayList<>();
        if (body != null) {
            collectMethodCallsAndConstructors(body, calls);
            collectControlFlow(body, flow);
            collectFieldAccesses(body, fieldAccesses);
            collectInstantiations(body, instantiations);
        }
        ms.setMethodCalls(calls);
        ms.setControlFlowStatements(flow);
        ms.setFieldAccesses(fieldAccesses);
        ms.setInstantiations(instantiations);
        return ms;
    }

    /**
     * Collects control-flow structures under {@code body}, ordered by source position (line, then
     * column).
     */
    static void collectControlFlow(BlockStmt body, List<ControlFlowStatementSummary> out) {
        List<FlowEntry> entries = new ArrayList<>();

        for (IfStmt s : body.findAll(IfStmt.class)) {
            if (isElseIfBranch(s)) {
                continue;
            }
            entries.add(entry(s, ifSummary(s)));
        }
        for (WhileStmt s : body.findAll(WhileStmt.class)) {
            entries.add(entry(s, loopLikeSummary("while", s.getCondition().toString().trim(), s)));
        }
        for (DoStmt s : body.findAll(DoStmt.class)) {
            entries.add(
                    entry(
                            s,
                            new ControlFlowStatementSummary(
                                    "doWhile", s.getCondition().toString().trim(), lineOf(s))));
        }
        for (ForStmt s : body.findAll(ForStmt.class)) {
            entries.add(entry(s, loopLikeSummary("for", forLoopCondition(s), s)));
        }
        for (ForEachStmt s : body.findAll(ForEachStmt.class)) {
            String cond =
                    s.getVariable().toString().trim()
                            + " : "
                            + s.getIterable().toString().trim();
            entries.add(entry(s, loopLikeSummary("foreach", cond, s)));
        }
        for (SwitchStmt s : body.findAll(SwitchStmt.class)) {
            entries.add(entry(s, switchSummary(s)));
        }
        for (SynchronizedStmt s : body.findAll(SynchronizedStmt.class)) {
            entries.add(
                    entry(
                            s,
                            new ControlFlowStatementSummary(
                                    "synchronized", s.getExpression().toString().trim(), lineOf(s))));
        }
        for (TryStmt s : body.findAll(TryStmt.class)) {
            entries.add(entry(s, new ControlFlowStatementSummary("try", null, lineOf(s))));
        }
        for (CatchClause c : body.findAll(CatchClause.class)) {
            String cond = c.getParameter().getType().asString();
            entries.add(entry(c, new ControlFlowStatementSummary("catch", cond, lineOf(c))));
        }

        entries.sort(Comparator.naturalOrder());
        for (FlowEntry e : entries) {
            out.add(e.summary());
        }
    }

    /**
     * {@code else if} is parsed as an {@code IfStmt} nested in the {@code else} branch; those inner
     * nodes are merged into {@link ControlFlowStatementSummary#getChainedElseIf()} and skipped as
     * separate entries in {@link #collectControlFlow}.
     */
    private static boolean isElseIfBranch(IfStmt stmt) {
        return stmt.getParentNode()
                .filter(IfStmt.class::isInstance)
                .map(IfStmt.class::cast)
                .filter(parent -> parent.getElseStmt().orElse(null) == stmt)
                .isPresent();
    }

    private static ControlFlowStatementSummary ifSummary(IfStmt s) {
        ControlFlowStatementSummary cfs =
                new ControlFlowStatementSummary("if", s.getCondition().toString().trim(), lineOf(s));
        cfs.setThenLine(lineOfNullable(s.getThenStmt()));
        Optional<Statement> elseOpt = s.getElseStmt();
        if (elseOpt.isEmpty()) {
            cfs.setEndLine(endLineOf(s));
            return cfs;
        }
        Statement elseSt = elseOpt.get();
        if (elseSt instanceof IfStmt inner) {
            cfs.setElseLine(lineOfNullable(inner));
            cfs.setChainedElseIf(ifSummary(inner));
        } else {
            cfs.setElseLine(lineOfNullable(elseSt));
        }
        cfs.setEndLine(endLineOfIfChain(s));
        return cfs;
    }

    /** End line of this {@code if} including {@code else} / {@code else if} chain. */
    private static Integer endLineOfIfChain(IfStmt s) {
        Optional<Statement> elseOpt = s.getElseStmt();
        if (elseOpt.isEmpty()) {
            return endLineOf(s);
        }
        Statement elseSt = elseOpt.get();
        if (elseSt instanceof IfStmt inner) {
            return endLineOfIfChain(inner);
        }
        return endLineOf(s);
    }

    private static ControlFlowStatementSummary loopLikeSummary(
            String kind, String condition, Statement loopStmt) {
        ControlFlowStatementSummary cfs = new ControlFlowStatementSummary(kind, condition, lineOf(loopStmt));
        if (loopStmt instanceof WhileStmt w) {
            cfs.setThenLine(lineOfNullable(w.getBody()));
            cfs.setElseLine(null);
            cfs.setEndLine(endLineOf(w));
        } else if (loopStmt instanceof ForStmt f) {
            cfs.setThenLine(lineOfNullable(f.getBody()));
            cfs.setElseLine(null);
            cfs.setEndLine(endLineOf(f));
        } else if (loopStmt instanceof ForEachStmt fe) {
            cfs.setThenLine(lineOfNullable(fe.getBody()));
            cfs.setElseLine(null);
            cfs.setEndLine(endLineOf(fe));
        }
        return cfs;
    }

    private static ControlFlowStatementSummary switchSummary(SwitchStmt s) {
        ControlFlowStatementSummary cfs =
                new ControlFlowStatementSummary("switch", s.getSelector().toString().trim(), lineOf(s));
        List<SwitchCaseSummary> cases = new ArrayList<>();
        for (SwitchEntry e : s.getEntries()) {
            String label;
            if (e.isDefault()) {
                label = "default";
            } else if (e.getLabels().isEmpty()) {
                label = "";
            } else {
                label =
                        e.getLabels().stream()
                                .map(Expression::toString)
                                .collect(Collectors.joining(", "));
            }
            cases.add(new SwitchCaseSummary(label, lineOfNullable(e)));
        }
        cfs.setCases(cases);
        cfs.setThenLine(null);
        cfs.setElseLine(null);
        cfs.setEndLine(endLineOf(s));
        return cfs;
    }

    /**
     * {@link MethodCallExpr} and {@link ObjectCreationExpr} ({@code new ...}), ordered by source
     * position. Constructor calls are stored in the same {@link MethodCallSummary} list as method
     * calls (expression text is the full {@code new Type(...)}).
     */
    static void collectMethodCallsAndConstructors(BlockStmt body, List<MethodCallSummary> out) {
        List<MethodLikePos> list = new ArrayList<>();
        for (MethodCallExpr call : body.findAll(MethodCallExpr.class)) {
            list.add(new MethodLikePos(lineOf(call), colOf(call), summarizeCall(call)));
        }
        for (ObjectCreationExpr ctor : body.findAll(ObjectCreationExpr.class)) {
            list.add(new MethodLikePos(lineOf(ctor), colOf(ctor), summarizeObjectCreation(ctor)));
        }
        list.sort(Comparator.naturalOrder());
        for (MethodLikePos p : list) {
            out.add(p.summary());
        }
    }

    static void collectInstantiations(BlockStmt body, List<InstantiationSummary> out) {
        List<InstantiationPos> list = new ArrayList<>();
        for (ObjectCreationExpr expr : body.findAll(ObjectCreationExpr.class)) {
            summarizeInstantiation(expr)
                    .ifPresent(summary -> list.add(new InstantiationPos(lineOf(expr), colOf(expr), summary)));
        }
        list.sort(Comparator.naturalOrder());
        for (InstantiationPos p : list) {
            out.add(p.summary());
        }
    }

    static void collectFieldAccesses(BlockStmt body, List<FieldAccessSummary> out) {
        Set<Expression> assignTargets = Collections.newSetFromMap(new IdentityHashMap<>());
        for (AssignExpr a : body.findAll(AssignExpr.class)) {
            assignTargets.add(a.getTarget());
        }
        List<FieldAccessPos> list = new ArrayList<>();
        for (FieldAccessExpr fae : body.findAll(FieldAccessExpr.class)) {
            tryFieldAccess(fae, assignTargets).ifPresent(list::add);
        }
        for (NameExpr ne : body.findAll(NameExpr.class)) {
            tryFieldAccess(ne, assignTargets).ifPresent(list::add);
        }
        list.sort(Comparator.naturalOrder());
        for (FieldAccessPos p : list) {
            out.add(p.summary());
        }
    }

    private static Optional<FieldAccessPos> tryFieldAccess(
            Expression expr, Set<Expression> assignTargets) {
        try {
            ResolvedValueDeclaration rv;
            if (expr instanceof FieldAccessExpr fae) {
                rv = fae.resolve();
            } else if (expr instanceof NameExpr ne) {
                rv = ne.resolve();
            } else {
                return Optional.empty();
            }
            if (!rv.isField()) {
                return Optional.empty();
            }
            ResolvedFieldDeclaration fd = rv.asField();
            String owner = fd.declaringType().getQualifiedName();
            String accessType = assignTargets.contains(expr) ? "write" : "read";
            FieldAccessSummary summary = new FieldAccessSummary(fd.getName(), owner, accessType);
            int line = expr.getBegin().map(p -> p.line).orElse(0);
            int col = expr.getBegin().map(p -> p.column).orElse(0);
            return Optional.of(new FieldAccessPos(line, col, summary));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static FlowEntry entry(Node node, ControlFlowStatementSummary summary) {
        return new FlowEntry(lineOf(node), colOf(node), summary);
    }

    private static int lineOf(Node n) {
        return n.getBegin().map(p -> p.line).orElse(0);
    }

    private static Integer lineOfNullable(Node n) {
        return n.getBegin().map(p -> p.line).orElse(null);
    }

    private static Integer endLineOf(Node n) {
        return n.getEnd().map(p -> p.line).orElse(null);
    }

    private static int colOf(Node n) {
        return n.getBegin().map(p -> p.column).orElse(0);
    }

    private static String forLoopCondition(ForStmt f) {
        String init =
                f.getInitialization().stream().map(Object::toString).collect(Collectors.joining(", "));
        String compare = f.getCompare().map(Object::toString).orElse("");
        String update = f.getUpdate().stream().map(Object::toString).collect(Collectors.joining(", "));
        return String.join("; ", List.of(init, compare, update));
    }

    private static MethodCallSummary summarizeCall(MethodCallExpr call) {
        String expression = call.toString();
        try {
            ResolvedMethodDeclaration resolved = call.resolve();
            String signature = resolved.getQualifiedSignature();
            String declaring =
                    resolved.declaringType() != null
                            ? resolved.declaringType().getQualifiedName()
                            : null;
            return new MethodCallSummary(expression, true, declaring, signature);
        } catch (RuntimeException e) {
            return new MethodCallSummary(expression, false, null, null);
        }
    }

    private static Optional<InstantiationSummary> summarizeInstantiation(ObjectCreationExpr expr) {
        Integer line = lineOfNullable(expr);
        try {
            ResolvedConstructorDeclaration resolved = expr.resolve();
            String qn = resolved.declaringType().getQualifiedName();
            if (qn != null && (qn.startsWith("java.") || qn.startsWith("javax."))) {
                return Optional.empty();
            }
            return Optional.of(new InstantiationSummary(resolved.declaringType().getClassName(), line));
        } catch (RuntimeException ignored) {
            // Fallback to AST type when resolution is unavailable.
            return Optional.of(new InstantiationSummary(expr.getType().getName().asString(), line));
        }
    }

    private static MethodCallSummary summarizeObjectCreation(ObjectCreationExpr expr) {
        String expression = expr.toString();
        try {
            ResolvedConstructorDeclaration resolved = expr.resolve();
            String signature = resolved.getQualifiedSignature();
            String declaring = resolved.declaringType().getQualifiedName();
            return new MethodCallSummary(expression, true, declaring, signature);
        } catch (RuntimeException e) {
            return new MethodCallSummary(expression, false, null, null);
        }
    }
}
