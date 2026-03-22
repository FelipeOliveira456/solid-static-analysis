# Research: ETAPA 4 - Scoring SOLID

## Decision 1: Keep scoring in a dedicated package and CLI mode

- **Decision**: Implement the scorer in `com.solidanalysis.scoring` and expose
  it through a new `--score` branch in `SolidAnalysisCli`.
- **Rationale**: Stage 4 has its own inputs, outputs, and failure behavior. A
  dedicated package keeps the code isolated from graph generation and algorithm
  analysis while reusing the same shaded JAR entrypoint.
- **Alternatives considered**: Extending `GraphAlgorithmsRunner` or folding the
  logic into existing algorithm classes. Those options would blur stage
  boundaries and make failure handling harder to reason about.

## Decision 2: Reuse existing JSON models and Jackson-based loading

- **Decision**: Load algorithm JSON and AST JSON with Jackson, reusing the
  existing model classes where the schema already matches the fixture data.
- **Rationale**: The repository already standardizes JSON handling through
  Jackson. Reusing those models minimizes duplication and keeps fixture-based
  tests straightforward.
- **Alternatives considered**: Custom parsing with raw maps or new DTOs for
  every artifact. Those approaches would add boilerplate without improving the
  feature.

## Decision 3: Load thresholds from `analysis.properties` with code defaults

- **Decision**: Read fixed-threshold overrides from `analysis.properties` in
  the project root and fall back to built-in defaults when the file or a key is
  missing.
- **Rationale**: The spec requires configurable thresholds but also requires
  the scorer to work out of the box.
- **Alternatives considered**: Hard-coding the thresholds or exposing them only
  as CLI arguments. Hard-coding would remove configurability, and CLI-only
  tuning would make repeated runs verbose and error-prone.

## Decision 4: Use a scoring-specific DOT reader for labeled edges

- **Decision**: Add a scorer-side graph reader for the DOT files that need edge
  labels or node attributes, especially G7 switch nodes and instantiation
  counts.
- **Rationale**: The existing DOT loader is optimized for topology, but stage 4
  needs attribute-aware extraction. Keeping that logic local avoids changing the
  graph generation pipeline.
- **Alternatives considered**: Expanding the existing `GraphDotLoader` to cover
  all scoring cases or relying solely on algorithm JSON. Both options either
  couple unrelated stages or miss the attribute data needed by the scorer.

## Decision 5: Use worst-case aggregation and deterministic ranking

- **Decision**: Score each principle by taking the worst indicator result, then
  compute the overall class score from the worst principle. Rank classes by
  overall score and break ties by class name.
- **Rationale**: This matches the feature specification and gives stable,
  explainable results that are easy to verify in tests.
- **Alternatives considered**: Average-based or weighted scores. Those would
  make the output less sensitive to the most serious issue and would diverge
  from the spec.

## Decision 6: Treat project size as the selector for classification strategy

- **Decision**: Use fixed thresholds for projects with fewer than 10 classes
  and z-score classification for projects with 10 or more classes.
- **Rationale**: The spec provides this hybrid rule and it balances simplicity
  for small projects with normalization for larger ones.
- **Alternatives considered**: A single universal strategy for all projects or
  a user-selected strategy. The former would ignore the size-based rule in the
  spec, and the latter would add unnecessary configuration.
