# GAMA 2026 Changelog

This document covers changes since **Version 2025-06**, whose stable release tag is
[`2025.06.4`](https://github.com/gama-platform/gama/releases/tag/2025.06.4) (published July 3, 2025).

The historical audit covers `2025.06.4..b11559966`, through October 27, 2026:
1,970 commits (1,665 non-merge commits and 305 merges), including changes brought in through
merged branches and a check of merge-resolution deltas
([full comparison](https://github.com/gama-platform/gama/compare/2025.06.4...b11559966)).
Entries group related changes rather than reproducing every commit. Intermediate implementations,
reverted changes, generated files, formatting-only edits and routine release-version bumps are not
listed separately. References identify the implementation or follow-up fixing the described behavior.
Unmerged branches are outside the historical audit. Changes in companion repositories
([gama.devtools](https://github.com/gama-platform/gama.devtools), [gama.clients](https://github.com/gama-platform/gama.clients),
[gama.copilot](https://github.com/gama-platform/gama.copilot), [gama.shared.models](https://github.com/gama-platform/gama.shared.models),
[gama.experimental](https://github.com/gama-platform/gama.experimental) and the [gama-platform-plugins](https://github.com/gama-platform-plugins)
graphical modelling and LLM plugins) are summarised in the relevant topics from their own histories since July 2025.
Separately identified draft follow-ups below document development work, not released or merged behavior.

For migration-sensitive additions, start with the [GAML syntax overhaul and translator](#breaking-change-gaml-syntax-overhaul-and-model-translator),
then see [compiler diagnostics](#compiler-diagnostics-dates-and-expression-semantics),
[SQL/dataframes](#dataframes-and-database-migration), [numeric/container behavior](#gaml-string-container-and-numeric-correctness)
and [random draws/sampling](#random-draws-and-statistical-correctness), in addition to the Fory and nil-container notes below.

## [2026.0.0] - Unreleased

This release is organised in five topics:

- [1. Modelling and exploring models](#1-modelling-and-exploring-models)
- [2. Experimenting and visualising](#2-experimenting-and-visualising)
- [3. Sharing and deploying models](#3-sharing-and-deploying-models)
- [4. Developing GAMA](#4-developing-gama)
- [5. Other changes](#5-other-changes)

### 1. Modelling and exploring models

Everything that happens before an experiment is launched: the GAML language and its evolution
(classes and objects, functional creation, new syntax), the editor (validation, formatter, online
help), previous versions of files, wizards, file viewers, the navigator and workspace, the Tests view,
and the discovery and installation of optional extensions, plugins and models.

- **GAML language evolution:** [BREAKING CHANGE: GAML syntax overhaul and model translator](#breaking-change-gaml-syntax-overhaul-and-model-translator) · [GAML language and compiler: classes and objects](#gaml-language-and-compiler-classes-and-objects) · [Compiler diagnostics, dates and expression semantics](#compiler-diagnostics-dates-and-expression-semantics) · [BREAKING CHANGE: explicit container types for built-in action arguments](#breaking-change-explicit-container-types-for-built-in-action-arguments) · [Lists and maps: explicit nil is distinct from an empty container](#lists-and-maps-explicit-nil-is-distinct-from-an-empty-container) · [Nested map content types](#nested-map-content-types) · [GAML: percentage literals](#gaml-percentage-literals) · [GAML: reduce and scan iterators](#gaml-reduce-and-scan-iterators) · [GAML: string, container and numeric correctness](#gaml-string-container-and-numeric-correctness) · [Mesh source name resolution](#mesh-source-name-resolution) · [Co-model imports: preserve aliases while editing](#co-model-imports-preserve-aliases-while-editing) · [Benchmark statement: individual durations and optional console output](#benchmark-statement-individual-durations-and-optional-console-output)
- **Editor, validation and online help:** [Editor: formatter, online help and code minings](#editor-formatter-online-help-and-code-minings) · [Editor validation error reporting](#editor-validation-error-reporting) · [Editor: per-file automatic validation control](#editor-per-file-automatic-validation-control) · [Adjustable editor delays for large models](#adjustable-editor-delays-for-large-models) · [Editor Find/Replace](#editor-findreplace) · [GAML outline icons](#gaml-outline-icons)
- **History, wizards and views of the modeling perspective:** [Major addition: previous versions for model editing](#major-addition-previous-versions-for-model-editing) · [GAML file creation](#gaml-file-creation) · [Wizards, refactoring and syntax conversion](#wizards-refactoring-and-syntax-conversion) · [File viewers and editors: CSV, JSON/GeoJSON, SVG and GML](#file-viewers-and-editors-csv-jsongeojson-svg-and-gml)
- **Navigator and workspace stability:** [Workspace models organisation and navigator](#workspace-models-organisation-and-navigator) · [Navigator and workspace stability](#navigator-and-workspace-stability) · [Navigator metadata during workspace recovery](#navigator-metadata-during-workspace-recovery)
- **Tests:** [Tests view and test execution](#tests-view-and-test-execution)
- **Installing and discovering extensions, plugins and models:** [GitHub Copilot: independent optional integration](#github-copilot-independent-optional-integration) · [Optional installations and memory indicator](#optional-installations-and-memory-indicator) · [GitHub Copilot integration (gama.copilot)](#github-copilot-integration-gamacopilot) · [LLM plugin (gama.plugin.llm)](#llm-plugin-gamapluginllm) · [Graphical modelling (diagram editor)](#graphical-modelling-diagram-editor) · [Bundled library models](#bundled-library-models) · [Shared models (gama.shared.models)](#shared-models-gamasharedmodels) · [Experimental plugins (gama.experimental)](#experimental-plugins-gamaexperimental) · [Icons and resources (gama.resources)](#icons-and-resources-gamaresources)

#### BREAKING CHANGE: GAML syntax overhaul and model translator

**GAML 2026 is not source-compatible with all GAMA 2025-06 models.** The rewritten grammar
standardizes declarations, action signatures/calls and named arguments, removes several legacy
forms, and separates display/experiment identifiers from their visible titles. This is a
model-source migration, not just an internal compiler refactoring: older models may fail to
parse or compile until converted.

The main changes and the older forms normalized by the translator are:

| Area | Older syntax | GAML 2026 syntax |
|---|---|---|
| Action declaration, including no arguments | `action move { ... }` | `action move() { ... }` |
| Typed action declaration | `int compute { return 1; }` | `int compute() { return 1; }` |
| Action arguments and defaults | `action move { arg distance type: float default: 1.0; ... }` | `action move(float distance <- 1.0) { ... }` |
| No-argument action call | `do move;` or `do action: move;` | `do move();` |
| Named argument lists | `do move with: [distance::2.0];` | `do move with: (distance: 2.0);` (or `do move(distance: 2.0);`) |
| Typed local declaration | `let n type: int value: 5;` | `int n <- 5;` |
| Assignment | `set n value: 5;` | `n <- 5;` |
| List content type | `list of: int` | `list<int>` |
| Computed expression after `->` | `float doubled -> {value * 2};` | `float doubled -> value * 2;` |
| Diffusion target | `diffuse var: heat ...;` | `diffuse heat ...;` |
| State-transition target | `transition to: idle ...;` | `transition idle ...;` |
| Image display layer | `image "background.png";` inside a display | `picture "background.png";` |
| Display/experiment name and label | `display "3 Simulations" { ... }` | `display _3_Simulations title: "3 Simulations" { ... }` (same separation for `experiment`) |

Parentheses are required on action **declarations**, even when empty; former body-level `arg`
declarations are compilation errors. Write typed arguments and optional `<-` defaults in the
signature. The translator also normalizes older call/declaration spellings; **not every old
form in the table is rejected**: some legacy calls/facets remain accepted, and
`create ... with:` is deprecated rather than removed. Functional creation is also available,
for example `create person(age: 20) number: 10;`.

The `with:` change concerns **argument passing**, not ordinary maps/pairs: map literals still
use `::`. Removing braces after `->` concerns the outer expression wrapper, not normal statement
blocks or point literals. Use `title:` when a human-readable label differs from the identifier;
it is not necessary to add a title to every display or experiment.

**Migrating existing models:** back up or commit your model sources, select the relevant
projects, folders or files in the navigator, then choose **Convert to GAMA 2026 syntax...**.
The bundled Java translator applies the conversions above to `.gaml` and `.experiment` files,
recursively for selected directories. It requests confirmation and saves open editors before
rewriting files **in place**; the operation cannot be undone through editor Undo.

Review the diff, validate the converted models and run their experiments afterward. The translator
automates supported syntax rewrites, not arbitrary semantic/API migration: review generated
identifiers and references to renamed experiments/displays (including server clients), and handle
changes such as SQL/dataframe results, Fory saves and explicit `nil` separately. Conversion is
not a guarantee that every older model will run unchanged.

See the [detailed migration reference](MIGRATION_GUIDE_2026.md#1-gaml-language-syntax-changes),
the [registered translator passes](gaml.compiler/src/gaml/compiler/transition/GamlFileProcessor.java),
and the [navigator conversion command](gama.ui.navigator/src/gama/ui/navigator/commands/ConvertToGama2026Handler.java)
([`ac8b13c`](https://github.com/gama-platform/gama/commit/ac8b13c),
[`6b7f2d040`](https://github.com/gama-platform/gama/commit/6b7f2d040),
[`d8be6dbcf`](https://github.com/gama-platform/gama/commit/d8be6dbcf), #799).

#### GAML language and compiler: classes and objects

The GAML compiler has been completely rewritten into a stateless architecture, ensuring faster compilation. Furthermore, GAML now natively supports Object-Oriented concepts (`class` and `object`).

*   `[ca3cded]` Add IClass and IObject interfaces
*   `[e033a1c]` Add CLASS support and AST updates
*   `[95e7f7e]` Add ClassDescription and refactor IClass/IObject
*   `[5105a81]` Add class support and refactor constructors/actions
*   `[99a015e]` Add gaml.grammar project and refactoring tools (Xtext Extraction)
*   `[7c85472]` Make expression compilation stateless
*   `[ee3b660]` Add documentation context and refactor validation
*   `[fcf7321]` Reduce synchronization in import indexing
*   `[a61ece7]` Make GamlResourceServices thread-safe
*   `[b3fd49f]` Replace 'image' keyword with 'picture'

#### Compiler diagnostics, dates and expression semantics

- Functional creation supports `create species_name(attribute: value, ...) number: ...;`;
  legacy `create ... with:` is deprecated. **Migration:** the former `set variable value: ...`
  assignment syntax is removed; use `variable <- ...`. The
  [syntax migration reference](MIGRATION_GUIDE_2026.md#1-gaml-language-syntax-changes)
  also documents argument-list, arrow-block, `diffuse` / `transition`, `picture` and
  display/experiment-title changes
  ([`4d21b93da`](https://github.com/gama-platform/gama/commit/4d21b93da),
  [`f793a45f6`](https://github.com/gama-platform/gama/commit/f793a45f6)).
- **Migration:** legacy `arg` declarations are compilation errors. Declare arguments in the
  action signature, e.g. `action a(int x, float y <- 1.0)`. Compilation also rejects duplicate
  default facets, an aspect sharing an attribute's name, a species inheriting from a class,
  and lifecycle/spatial declarations (`reflex`, `init`, `aspect`) inside classes
  ([`d8be6dbcf`](https://github.com/gama-platform/gama/commit/d8be6dbcf), #799;
  [`c2a01032c`](https://github.com/gama-platform/gama/commit/c2a01032c), #1118;
  [`a88d263b8`](https://github.com/gama-platform/gama/commit/a88d263b8), #1177;
  [`2c6f4c781`](https://github.com/gama-platform/gama/commit/2c6f4c781), #1162;
  [`8049eec13`](https://github.com/gama-platform/gama/commit/8049eec13), #1165).
- Ambiguous operator resolution produces a warning; implicit var-argument list wrapping produces
  an informational diagnostic. Test experiments warn about experiment-level seed assignments
  ([`882340649`](https://github.com/gama-platform/gama/commit/882340649),
  [`9bed69341`](https://github.com/gama-platform/gama/commit/9bed69341), #1195;
  [`2637ba18b`](https://github.com/gama-platform/gama/commit/2637ba18b), #178).
- Actions can be invoked on container/access expressions. Arguments evaluate in the caller's
  scope and declaration order; temporary variables resolve in remote contexts, nested iterators
  restore `each`, and failing action calls in display events no longer fail silently
  ([`0543caf9c`](https://github.com/gama-platform/gama/commit/0543caf9c), #1164;
  [`cea9bf892`](https://github.com/gama-platform/gama/commit/cea9bf892),
  [`0ff7b4c96`](https://github.com/gama-platform/gama/commit/0ff7b4c96),
  [`331fde817`](https://github.com/gama-platform/gama/commit/331fde817), #779;
  [`b07d7e3a7`](https://github.com/gama-platform/gama/commit/b07d7e3a7), #1172).
- Class attribute reads and object scope context are corrected. Indexed assignments validate
  index types and accept float indices. `pref_cast_container_contents` exposes container-content
  casting behavior, enabled by default
  ([`998d1f420`](https://github.com/gama-platform/gama/commit/998d1f420), #1152;
  [`bd246da8e`](https://github.com/gama-platform/gama/commit/bd246da8e),
  [`6d06ce9e0`](https://github.com/gama-platform/gama/commit/6d06ce9e0), #1104;
  [`c4669cd4d`](https://github.com/gama-platform/gama/commit/c4669cd4d)).
- Pasted Unicode whitespace parses correctly. Expressions compiled from strings use the active
  scope context; `do` can resolve operator expressions as well as action calls. Parent action
  dispatch distinguishes species from classes, and unknown arguments can match nonnumeric
  overloads
  ([`50cc3704e`](https://github.com/gama-platform/gama/commit/50cc3704e),
  [`f613d8618`](https://github.com/gama-platform/gama/commit/f613d8618),
  [`d0a514be2`](https://github.com/gama-platform/gama/commit/d0a514be2), #1016;
  [`2635687aa`](https://github.com/gama-platform/gama/commit/2635687aa),
  [`d4c1364c1`](https://github.com/gama-platform/gama/commit/d4c1364c1)).
- Date parsing, formatting and arithmetic support negative/antiquity years consistently;
  formatter year conversion preserves quoted literals. Date hashing and calendar-based
  `every` with `#month` / `#year` are corrected
  ([`7e0858909`](https://github.com/gama-platform/gama/commit/7e0858909),
  [`9480c85d4`](https://github.com/gama-platform/gama/commit/9480c85d4),
  [`e5681752d`](https://github.com/gama-platform/gama/commit/e5681752d),
  [`b408c1616`](https://github.com/gama-platform/gama/commit/b408c1616),
  [`907d7bff1`](https://github.com/gama-platform/gama/commit/907d7bff1)).
- Date casts accept `[text, pattern]` and `[text, pattern, locale]` in addition to numeric
  calendar-component lists
  ([`4e4f44b08`](https://github.com/gama-platform/gama/commit/4e4f44b08)).
- Non-constant parameter initializers can populate experiment choices; batch `sample:` /
  `levels:` facets accept expressions. Generic-file field getters and platform-specific
  `project_path` separators are corrected
  ([`0db0f6696`](https://github.com/gama-platform/gama/commit/0db0f6696), #786;
  [`3935dabae`](https://github.com/gama-platform/gama/commit/3935dabae), #873;
  [`1b53532e2`](https://github.com/gama-platform/gama/commit/1b53532e2), #713;
  [`e51ded5b6`](https://github.com/gama-platform/gama/commit/e51ded5b6), #805).

#### BREAKING CHANGE: explicit container types for built-in action arguments

Built-in actions now expose and check the content and key types declared by their Java
`@arg` annotations (`of` and `index`). Models that previously passed an unparameterized
`graph` to spatial movement, driving or pedestrian actions may now report an argument-type
error. Declare these networks as `graph<geometry, geometry>`, not just `graph`; the two
parameters describe vertices and edges, respectively. Road and intersection agents are
compatible with geometry types.

Update globals, attributes, local variables and action parameters through which these
networks are passed. Assigning a spatial graph to a variable declared only as `graph`
does not refine that variable's declared type. Other container arguments may similarly
require explicit list element or map key/value types. This does not require non-spatial
graphs to use geometry types.

See the [migration example](MIGRATION_GUIDE_2026.md#explicit-container-types-for-built-in-action-arguments)
for the declaration change.

#### Lists and maps: explicit nil is distinct from an empty container

Lists and maps now preserve explicit `nil` initialization, assignment and casts (#765).
The rule is the same for local variables, global and species attributes, and typed or untyped containers:

| GAML | Result |
|---|---|
| `list l;` / `map m;` | A new empty list / map |
| `list<int> l;` / `map<string,int> m;` | A new empty container with the declared types |
| `list l <- nil;` / `map m <- nil;` | No container: the value is `nil` |
| `l <- nil;` / `m <- nil;` | The variable becomes `nil` |
| `list(nil)` / `map(nil)` / `to_list(nil)` | `nil` |
| `[] = nil` / `map([]) = nil` | `false` |

Each omitted initializer creates its own mutable container, including on repeated execution of a local
declaration and for separate agents. A `nil` container is not an empty container: test for absence with
`value = nil` before using operators that require an actual container. Operator-specific nil handling
still applies; for example, indexing a nil container reports a runtime error.
Explicit nil values supplied through `create ... with:` or object constructor arguments are also preserved.

**Migration:** assigning or casting `nil` no longer creates an empty list or map. Use `[]` or `map([])`
explicitly when an empty container is intended. The bundled **Lists** and **Maps** recipes illustrate
this distinction.

#### Nested map content types

- Converting nested maps and lists now propagates declared container types even when the inferred
  types are assignment-compatible. A `map<string, map<string, unknown>>` initialized with float-only
  inner maps accepts later string values without coercing them to floats (#751). Explicitly typed
  maps still cast inserted values when container-content casting is enabled.

#### GAML: percentage literals

Numeric values can now use `%` as a postfix percentage suffix: `100%` is equivalent to
`100 #percent` and evaluates to `1.0`. This also works with decimal values and in larger
expressions, for example `2.5%` or `100% + 2%`.

#### GAML: reduce and scan iterators

The new `reduce` operator (#464) combines container elements into one result; `scan` returns a
list of successive accumulated values. Both evaluate sequentially in container iteration order,
with `@` representing the accumulator and `each` the current element. They form an accumulator
subcategory of iterators, currently identified by operator name without a separate annotation:

| GAML | Result |
|---|---|
| `[1,2,3,4] reduce (@ + each)` | `10` |
| `[1,2,3] reduce (10, @ + each)` | `16` |
| `[1,2,3,4] scan (@ + each)` | `[1,3,6,10]` |
| `[1,2,3,4] scan (0, @ + each)` | `[0,1,3,6,10]` |
| `["a","bb","ccc"] scan (0, @ + length(each))` | `[0,1,3,6]` |

Without a seed, the first element initializes the accumulator and the body runs from the second
element. With a seed, it is evaluated once before the body, which then runs for every element;
`scan` includes the seed as its first result. The accumulator type comes from the seed, or from the
container's element type when no seed is supplied, and the body must return a compatible value.

An empty seeded `reduce` returns its seed; an empty seeded `scan` returns `[seed]`. Without a seed,
an empty `reduce` raises a runtime error, whereas an empty `scan` returns `[]`. Maps iterate over
values. Both operators support function-call syntax, container-valued accumulators and nesting;
inner bodies temporarily bind their own `@` and `each`. Scan results retain accumulated values
without deep copying them.

Editor templates and accumulator hover documentation accompany dedicated
[Reduce](gama.library/models/Recipes/Loop%20And%20Iterations/Reduce.gaml) and
[Scan](gama.library/models/Recipes/Loop%20And%20Iterations/Scan.gaml) recipes.

#### GAML: string, container and numeric correctness

- Lists, matrices and fields gain `slice` with **inclusive** endpoints, negative indices and
  optional forward/reverse steps. `sublist` selects ordered explicit indices, including repeated
  indices; `submatrix` supports range or explicit column/row selections. This differs from
  `copy_between`, whose upper bound remains exclusive. `as_list` flattens matrices/fields
  in row-major order
  ([`8b2254bb8`](https://github.com/gama-platform/gama/commit/8b2254bb8),
  [`31cc4f0fb`](https://github.com/gama-platform/gama/commit/31cc4f0fb),
  [`c6be4a87b`](https://github.com/gama-platform/gama/commit/c6be4a87b),
  [`42f67fc76`](https://github.com/gama-platform/gama/commit/42f67fc76)).
- **Migration:** `ceil(float)` returns an integer, not a float; `ceiling` is an additional spelling
  ([`41261004e`](https://github.com/gama-platform/gama/commit/41261004e), #766).
- New string operators include `starts_with`, `ends_with`, `trim`, `tokenize_regex`,
  `whitespace`, `format`, `replace_first`, `count`, `string_with`, character-class predicates
  (`is_alphanum`, `is_alpha`, `is_ascii`, `is_decimal`, `is_digit`, `is_upper`, `is_lower`),
  and the aliases `join` / `split`. Repeated regular expressions use a bounded cache
  ([`2964f6618`](https://github.com/gama-platform/gama/commit/2964f6618), #760;
  [`1a6d8929e`](https://github.com/gama-platform/gama/commit/1a6d8929e)).
- `enumerate` returns index/element pairs, `atan2_rad` supplies a radian-based two-argument
  arctangent, and `from_hsb` aliases `hsb`. Integer exponentiation no longer uses a
  floating-point intermediate that rounds large results
  ([`f399fb111`](https://github.com/gama-platform/gama/commit/f399fb111), #1166;
  [`9daa5f2cc`](https://github.com/gama-platform/gama/commit/9daa5f2cc),
  [`5dd1f5f4d`](https://github.com/gama-platform/gama/commit/5dd1f5f4d)).
- Species expose their skill names through the read-only `skills` attribute; files expose their
  complete textual contents through `text`. Simulations provide `sleep(duration: ...)`
  ([`e83635357`](https://github.com/gama-platform/gama/commit/e83635357),
  [`f875d3167`](https://github.com/gama-platform/gama/commit/f875d3167), #763;
  [`3a28dcc1c`](https://github.com/gama-platform/gama/commit/3a28dcc1c), #1062).
- String indexing and slicing now count Unicode code points rather than UTF-16 code units.
  `length`, `index_of`, `last_index_of`, `copy_between`, `at`, `first`, `last` and `capitalize`
  handle supplementary characters consistently. **Compatibility:** indices and lengths can change
  for strings containing emoji or other supplementary characters
  ([`a1798aa97`](https://github.com/gama-platform/gama/commit/a1798aa97)).
- `compress` / `uncompress` preserve non-ASCII text using UTF-8 in both directions. `is_number`
  rejects strings with a trailing nonnumeric character, such as `"12A"`
  ([`0cd697012`](https://github.com/gama-platform/gama/commit/0cd697012),
  [`cbac0fd93`](https://github.com/gama-platform/gama/commit/cbac0fd93)).
- `to_gaml` / `from_gaml` correctly round-trip strings containing quotes and backslashes;
  compiling a quoted expression no longer mistakes the entire expression for one literal.
  Explicit `unknown` content types in casts, such as `map<string, unknown>`, are preserved
  ([`99ca7a4a4`](https://github.com/gama-platform/gama/commit/99ca7a4a4),
  [`e402f02b7`](https://github.com/gama-platform/gama/commit/e402f02b7),
  [`62c660fe8`](https://github.com/gama-platform/gama/commit/62c660fe8)).
- Searches with `index_of`, `last_index_of` and `all_indexes_of` can find `nil` elements in
  lists, maps and matrices. Primitive-array-to-list conversion no longer loses elements when
  no element cast is required
  ([`c926f1dc5`](https://github.com/gama-platform/gama/commit/c926f1dc5),
  [`99eb1dfc6`](https://github.com/gama-platform/gama/commit/99eb1dfc6)).
- `append_horizontally` now appends columns, not rows. Matrix concatenation supports mixed
  integer/float operands and object matrices, promotes the result appropriately, and rejects
  incompatible dimensions instead of silently returning an unchanged operand
  ([`abdc4a98d`](https://github.com/gama-platform/gama/commit/abdc4a98d),
  [`6be4ba49a`](https://github.com/gama-platform/gama/commit/6be4ba49a)).
- Numeric matrices compare by dimensions and values: equivalent integer and float matrices
  are equal. A zero width or height is normalized to an empty `0 x 0` matrix, including fields,
  and empty initializers retain the requested element type
  ([`9c91dae54`](https://github.com/gama-platform/gama/commit/9c91dae54),
  [`5c0ea5a90`](https://github.com/gama-platform/gama/commit/5c0ea5a90), #758).
- Equal-bound numeric loops execute their body once. Floating-point equality recognizes
  infinities of the same sign as equal
  ([`6ca485193`](https://github.com/gama-platform/gama/commit/6ca485193), #1109;
  [`92079fccf`](https://github.com/gama-platform/gama/commit/92079fccf)).
- Float comparison consistently handles paired NaNs and nil operands; float-matrix membership
  and removal use the same comparison. Integer modulo by zero reports a GAML error.
  `regex_matches` declares string content for its result list
  ([`14bda6b29`](https://github.com/gama-platform/gama/commit/14bda6b29),
  [`5ba015b68`](https://github.com/gama-platform/gama/commit/5ba015b68), #813;
  [`28758e79b`](https://github.com/gama-platform/gama/commit/28758e79b),
  [`952c58252`](https://github.com/gama-platform/gama/commit/952c58252)).
- `migrate source:` accepts a species, a single micro-agent or a container of micro-agents,
  rather than only a literal species name. Invalid source types and agents not owned by the
  executor are rejected explicitly
  ([`b2c8af089`](https://github.com/gama-platform/gama/commit/b2c8af089)).
- Invalid list/population indices, ragged matrices, malformed replacement regular expressions,
  invalid dates, nil mathematical operands and invalid precision requests now produce contextual
  GAML errors. Color/font/geometry operators also validate non-finite inputs and zero divisors;
  three-digit hexadecimal colors are accepted, blend ratios are clamped, and font sizes are bounded
  ([`b279dfe3d`](https://github.com/gama-platform/gama/commit/b279dfe3d),
  [`67795c92e`](https://github.com/gama-platform/gama/commit/67795c92e),
  [`b91c5a0af`](https://github.com/gama-platform/gama/commit/b91c5a0af),
  [`671cde3b7`](https://github.com/gama-platform/gama/commit/671cde3b7),
  [`12ec2b60c`](https://github.com/gama-platform/gama/commit/12ec2b60c)).
- Graph-path computation supports K-shortest paths in non-spatial graphs, and `beta_index`
  returns zero for a graph without vertices. Graph/container operator return types have also
  been corrected, improving type checking of graph indices and neighborhood results
  ([`b279dfe3d`](https://github.com/gama-platform/gama/commit/b279dfe3d),
  [`83ead77e9`](https://github.com/gama-platform/gama/commit/83ead77e9),
  [`c0e53e478`](https://github.com/gama-platform/gama/commit/c0e53e478),
  [`a4d032490`](https://github.com/gama-platform/gama/commit/a4d032490)).

#### Mesh source name resolution

Bare identifiers in statements such as `mesh terrain` now use normal name resolution,
including when the parser initially recognizes the statement as a declaration.
Declared fields resolve as mesh sources, and unknown names produce an undefined-variable
error instead of a misleading warning that the source has type `skill`.

#### Co-model imports: preserve aliases while editing

Adding, renaming, or removing `as` on an existing import now preserves the import's dependency-graph entry
(#774). Aliased imports support spaces in both filenames and directories, without requiring files to be renamed.

#### Benchmark statement: individual durations and optional console output

`benchmark` now accepts `returns:` to declare a temporary `list<float>` containing one
duration per execution, in milliseconds and execution order (#329). The variable remains
available in the enclosing scope after the block, without predeclaration or casts.
`console: false` suppresses the summary while retaining timing and result collection;
output produced inside the block is unaffected. By default, the block runs once and the
console summary is unchanged. `repeat:` must be positive; zero and negative values raise
a runtime error. Repetitions do not reset model state.

```gaml
benchmark "List construction" repeat: 5 returns: durations console: false {
    list<int> values <- [1, 2, 3];
}
float first_duration <- durations[0];
write "Average duration: " + mean(durations) + " ms";
```

This is separate from experiment-wide profiling via `experiment ... benchmark: true`.
See the [Benchmarking recipe](gama.library/models/Recipes/System/Benchmarking.gaml).

#### Editor: formatter, online help and code minings

- A syntax-aware formatter handles the rewritten grammar's operators, calls, facets, blocks
  and comments. Editor reconciliation/documentation rejects obsolete work, and launching a model
  no longer leaves ghost diagnostics from temporary build/import validation
  ([`9e5d06045`](https://github.com/gama-platform/gama/commit/9e5d06045),
  [`be7b93581`](https://github.com/gama-platform/gama/commit/be7b93581),
  [`a1eba1d0c`](https://github.com/gama-platform/gama/commit/a1eba1d0c),
  [`5343d881f`](https://github.com/gama-platform/gama/commit/5343d881f),
  [`0ad0fd2bc`](https://github.com/gama-platform/gama/commit/0ad0fd2bc)).
- Context menus can search a word in the global GAML reference from editors and consoles.
  Hover documentation works for uncompiled models; reference popups, editor readability and
  theme transitions have been improved. The console gains a search field
  ([`791fcc165`](https://github.com/gama-platform/gama/commit/791fcc165),
  [`8e4a0dfdd`](https://github.com/gama-platform/gama/commit/8e4a0dfdd),
  [`f12d7b5eb`](https://github.com/gama-platform/gama/commit/f12d7b5eb),
  [`196873acb`](https://github.com/gama-platform/gama/commit/196873acb),
  [`b80e1d7b7`](https://github.com/gama-platform/gama/commit/b80e1d7b7)).
- Editor code minings are grouped, deduplicated and truncated; operator hovers show parameter
  names. Folding an `if` leaves its `else` visible, and end-of-file template insertion is corrected
  ([`9ed49020c`](https://github.com/gama-platform/gama/commit/9ed49020c), #548;
  [`34477accf`](https://github.com/gama-platform/gama/commit/34477accf), #759;
  [`28c0d6da6`](https://github.com/gama-platform/gama/commit/28c0d6da6),
  [`513662253`](https://github.com/gama-platform/gama/commit/513662253)).

#### Editor validation error reporting

Validation failures are reported as editor diagnostics rather than escaping the Xtext validation
job and opening an interrupting error dialog. Detached expressions fall back to an enclosing
source location when available. Expected compilation errors retain their original message instead
of accumulating an "internal error" prefix at every nested expression; unexpected validation
failures are also logged, and cancellation remains separate from error reporting.
This also applies to argument compilation and both modern and legacy action calls.
An aborted validation also publishes a failed experiment-control state, replacing stale green
launch buttons with the error indicator instead of leaving the model apparently runnable.

#### Editor: per-file automatic validation control

The GAML editor's top-right **Editor options** menu now ends with **Automatic validation** and
**Validate now** (#802). Automatic validation is enabled by default; disabling it pauses semantic validation
and documentation rebuilding for that file, while parsing and syntax colouring remain active. The setting
is stored in workspace metadata and survives saving, closing and reopening the file.

A grey-circle overlay on navigator and editor-tab icons identifies files with automatic validation disabled;
their diagnostics may be outdated. **Validate now** is available only while automatic validation is disabled
and refreshes diagnostics and documentation for the current editor content without changing the setting.
Re-enabling **Automatic validation** immediately performs the same refresh, then resumes automatic updates.
Both actions update error annotations without requiring an edit or save. Running an experiment still compiles
the model, and stale error markers do not prevent launching a file whose automatic validation is disabled.

#### Adjustable editor delays for large models

**Preferences > Editors > Edition** now offers two settings to reduce background
work while typing, especially useful for large models:

- **Wait before rereading code and updating the model (ms)** controls how long the
  editor waits after the last edit before updating its understanding of the code.
- **Additional wait before checking for errors (ms)** adds a further pause before
  checking the model and refreshing diagnostics, including syntax errors.

Both settings accept **0 to 3000 ms** and default to **0** for immediate feedback.
Each new edit restarts the wait; the two delays add up before error checking.
For example, 500 ms followed by 1000 ms allows a 1.5-second typing pause before
checking starts. Increasing these values can make editing large models more
responsive and avoid displaying errors for unfinished statements, at the cost of
later feedback. Existing diagnostics can remain visible until the next check
finishes. **Validate now** bypasses the delays.

#### Editor Find/Replace

The GAML editor toolbar now has a Find/Replace icon that opens the same search UI as
Cmd-F (macOS) or Ctrl-F (Windows/Linux), replacing the custom inline search field.
The history arrows are grouped together to the left of the matching circular search
icon and remain dedicated to edit locations. Cmd/Ctrl-G is no longer
intercepted by the removed field. The Find overlay uses GAMA-styled up/down arrows
and a double magnifier for selecting all matches, in both light and dark themes.

#### GAML outline icons

The editor outline uses the reflex blue from the GAMA 2026 syntax palette,
adapted for light and dark themes. Distinct shapes and badges differentiate
items instead of multiple category colors. Group folders and display icons
use thin outlines without overlapping borders.

Imports, experiment setup, user interaction, BDI behaviors, batch methods and
additional data types now have matching icons. Generic types use their base type;
model-defined agent, class and skill types and future extension types receive
semantic family icons. Missing artwork has its own dashed icon, separate from
the question mark for the GAML `unknown` type.

Attributes, Parameters and Actions have folder-style group icons. In species and
`global`, behaviors (reflex, init, state, perceive, rule, plan...) and aspects are
also grouped in Behaviors and Aspects folders, and `test` blocks get their own Tests
folder (also in experiments). Groups appear first, in this order, followed by the
other elements. The navigator's model outline uses the same groups and icons. Attribute icons
retain their type and add a function, update or constant badge; typed actions
show their return type with an action arrow, and parameters show their referenced
attribute type with a slider badge. Typed actions are grouped under Actions,
not Attributes. Standalone experiments without an explicit type use the GUI icon,
matching the compiler default.

#### Major addition: previous versions for model editing

Model editing now includes a dedicated **Previous versions** view for browsing, comparing and restoring
saved versions of workspace files without leaving GAMA. Open it with **Previous versions...** in a file's
navigator context menu, or use **Show previous versions** / **Hide previous versions** immediately after
the minimap entry in the GAML editor's top-right options menu.

The view stays synchronized with the selected workspace file or active editor, automatically groups versions
by date, and opens a selected version in comparison with the current file. It refreshes when opened, whenever
a workspace editor is activated or brought to the top, and when the file changes, including saves. Revision
and comparison editors do not change the workspace file being followed. The streamlined presentation removes
the toolbar, repeated filename and column heading, and uses theme-aware GAMA clock, calendar and version icons.
Files without saved history can still open the view.

The revision context menu provides **Open in text editor**, **Compare with current**, and **Replace current**.
Replacement asks for overwrite confirmation and preserves the replaced contents in local history; the current
revision cannot replace itself. This makes it easier to inspect earlier modeling choices, compare edits and
recover an earlier version without manually copying files.

**Defaults and compatibility:** the navigator's former **Local history...** item is renamed **Previous versions...**.
The application default disables Eclipse's local-history retention policy, removing age, version-count and
file-size limits unless explicit workspace preferences enable them. Unlimited history can increase workspace
disk usage. Only versions retained in this workspace are available: this is not Git history or a backup, and
versions already discarded cannot be recovered. No GAML syntax migration is required.

#### GAML file creation

The global and navigator **New** menus now offer one **GAML file** wizard instead of separate model and
experiment entries. Its **Model** tab opens by default; a **Standalone experiment** tab creates an
experiment-only `.gaml` file, with optional model import and GUI or headless type selection. The separate
test-experiment wizard remains available.

#### Wizards, refactoring and syntax conversion

- Creation/editing wizards cover model/global headers, species, grids, classes, actions,
  reflexes, experiments and displays. Forms provide contextual validation and source-preserving,
  undoable edits. The display editor includes transactional layer creation, duplication, deletion
  and reordering; unsupported declarations and comments are preserved
  ([`3bfceb32c`](https://github.com/gama-platform/gama/commit/3bfceb32c),
  [`b9cb8c4dc`](https://github.com/gama-platform/gama/commit/b9cb8c4dc),
  [`e56d05953`](https://github.com/gama-platform/gama/commit/e56d05953),
  [`6f5493436`](https://github.com/gama-platform/gama/commit/6f5493436),
  [`1742a74c8`](https://github.com/gama-platform/gama/commit/1742a74c8)).
- Action extraction uses the action wizard's **Include selection** control, validates complete
  statements and caller context, and applies the declaration and call as one undoable edit
  ([`1317dfece`](https://github.com/gama-platform/gama/commit/1317dfece), #266).
- **Convert to GAMA 2026 syntax...** runs the syntax converter on selected projects, folders or
  model files from the navigator. See the [syntax-overhaul overview and migration instructions](#breaking-change-gaml-syntax-overhaul-and-model-translator)
  ([`6b7f2d040`](https://github.com/gama-platform/gama/commit/6b7f2d040)).

#### File viewers and editors: CSV, JSON/GeoJSON, SVG and GML

- Large CSVs use a virtual, lazy-loading viewer. Spatial-file viewing supports QGIS `.qmd`
  styles, and external file changes invalidate cached metadata
  ([`97a856dab`](https://github.com/gama-platform/gama/commit/97a856dab), #681;
  [`2ad18442d`](https://github.com/gama-platform/gama/commit/2ad18442d),
  [`0ef823b50`](https://github.com/gama-platform/gama/commit/0ef823b50), #194, #1127).
- A JSON/GeoJSON source editor provides syntax presentation, validation, outline and formatting.
  SVG files open in the image viewer with dimensions/metadata; GML files gain a GIS viewer
  and navigator metadata/associated-file support
  ([`ca8e6f166`](https://github.com/gama-platform/gama/commit/ca8e6f166),
  [`63dea6d05`](https://github.com/gama-platform/gama/commit/63dea6d05),
  [`30a69403e`](https://github.com/gama-platform/gama/commit/30a69403e),
  [`bdb3d869a`](https://github.com/gama-platform/gama/commit/bdb3d869a)).

#### Workspace models organisation and navigator

Plugins now contribute models through `models/{Data,Demos,Library,Recipes,Tutorials,Tests}/<GAMA projects>`; the `gama.models` extension point is removed. Projects are classified by location (not by natures), the contributing plugin is stored in the `.project` comment, and the GAMA nature is now `gama.workspace.nature` (the former ids are still recognised and migrated). The navigator gains a "Clean and update workspace..." command, hides empty top-level folders, and supports user *group folders* (plain directories of the workspace containing several projects, see #678) that can be created, renamed, deleted, filled by drag and drop, paste, and the New/Import project wizards.

The navigator's **Search...** field now offers **Title | Body** search modes. Title (the default) searches all filenames, folder, group and project names, and model tags, case-insensitively. Matching folders show their contents; ancestor folders remain visible for matching files. Body searches saved model source, including comments, case-insensitively. Body scans run in the background after a short typing delay, cancel when the query or mode changes, and use a bounded cache checked against resource modification stamps and charset. Unsaved editor changes are not searched. Clearing the field or pressing Escape restores the previous tree expansion.

The navigator's top-right menu includes **Modification date** and, when enabled, **Most recent first**. These saved settings replace the toolbar's date-sorting button. Dates can be displayed while retaining alphabetical order; hiding dates restores alphabetical order without losing the preferred date-sorting setting.

In the **User** folder and its groups, **New...** offers **Group...** (a name dialog) and **Project...** (the project wizard, prefilled with the selected group). Editable groups have icon-bearing **Rename...** and **Delete...** actions separated from the other options.

#### Navigator and workspace stability

- Navigator title search adds an **All | Models** restriction. External models can be copied
  into the workspace instead of linked, and linked files have a distinct visual indication.
  Workspace recovery/update dialogs offer **Don't ask again**, with preferences to change
  the decision later
  ([`7b13ddc10`](https://github.com/gama-platform/gama/commit/7b13ddc10),
  [`94697f629`](https://github.com/gama-platform/gama/commit/94697f629), #203;
  [`8667751a4`](https://github.com/gama-platform/gama/commit/8667751a4)).
- Workspace selection omits deleted workspaces; library loading in new workspaces and unexpected
  projects under User models are corrected. Preferences open correctly after configuring a
  startup file. Linux startup warns about Wayland limitations with an option not to show it again
  ([`f7019a1af`](https://github.com/gama-platform/gama/commit/f7019a1af),
  [`a14cda7e9`](https://github.com/gama-platform/gama/commit/a14cda7e9), #1181;
  [`a11ab331a`](https://github.com/gama-platform/gama/commit/a11ab331a), #1211;
  [`439892a81`](https://github.com/gama-platform/gama/commit/439892a81), #1081;
  [`2805df209`](https://github.com/gama-platform/gama/commit/2805df209)).
- Startup detects stale workspace locks/corrupt resource metadata and offers recovery.
  The workspace chooser is resizable, scalable SVG workbench icons support themes/disabled
  states, and narrow toolbars wrap or move experiment buttons into a menu
  ([`2497b55a1`](https://github.com/gama-platform/gama/commit/2497b55a1),
  [`463743818`](https://github.com/gama-platform/gama/commit/463743818),
  [`e2fb35357`](https://github.com/gama-platform/gama/commit/e2fb35357),
  [`7c298956e`](https://github.com/gama-platform/gama/commit/7c298956e),
  [`d09c3c568`](https://github.com/gama-platform/gama/commit/d09c3c568)).

#### Navigator metadata during workspace recovery

Generating file metadata no longer synchronously refreshes workspace resources.
This removes an uncancellable wait for the build's workspace lock when navigator
labels need uncached metadata, notably after a crash. Metadata invalidation still
uses file-system modification times; background and explicit workspace refreshes
remain enabled.

#### Tests view and test execution

The test view has a **Stop tests** button and displays the currently executing model, experiment and test name. By default, GUI tests execute in the workbench JVM, using scope cancellation and thread interruption. **Stop tests** interrupts the current experiment and ends the suite, preserving completed results and marking the active test as **interrupted**. **Execution > Tests > Test experiment timeout (seconds)** defaults to 10 seconds: a timeout requests interruption, marks the active test as **timeout**, and continues with the next experiment once execution actually exits. The runner never abandons a live test thread. Unstarted tests and assertions remain **not reached**.

**Advanced > Tests > Run tests in a separate headless process** enables stronger isolation for Java operators that ignore cooperative cancellation. An isolated sequence reuses an initialized headless worker, replacing it after a timeout or worker failure and terminating it when the sequence ends. In this mode Stop and timeout can forcibly terminate unresponsive computations. The timeout covers experiment initialization and execution, not model compilation or worker startup. Isolated tests require saved local model files; default same-JVM execution can use the model compiled from the editor.

The test-view toolbar shows **Stop tests** only during execution and **Run the same tests again** only after execution ends (including after Stop). Rerun repeats the previous selected test, experiment, or batch of model files, recompiling models rather than keeping the previous experiment alive. **Expand/Collapse** appears only when the resulting summary is displayed. The left toolbar uses round, single-color outline icons: a red funnel for problems-only filtering, descending bars and an arrow for severity sorting, and a download arrow into a tray for export; Stop and Rerun reuse the server console icons, with Stop now red in both views. Summary counters and the dynamically updated model/test status are now inside the toolbar.

The compact **Tests** view is now the only test results view; the legacy view and the **Use the compact test results view** preference have been removed. The view uses a virtual native tree (experiment, test, assertion), small status indicators, summary counters, and one shared details pane instead of per-assertion editor controls. It provides problems-only filtering, severity sorting, source navigation, copy/export, and coalesced progress updates. Problems-only filtering and severity sorting retain their **Execution > Tests** preferences.

The compact view places Expand/Collapse on the right and hides them for empty or flat reports. Its details pane appears only when a test or assertion is selected, shows the source filename rather than its path, and includes complete error stacks (including causes and GAML context) in both execution modes.

Individual test rows provide **Run this test only** in their context menu after execution ends. This recompiles the model and reruns only that declaration in its original experiment, in either execution mode. Its result and assertions are replaced in place, updating aggregate states and counters while retaining the other results, expanded rows, selection and scroll position (unless filtering hides the updated row). The toolbar rerun action still repeats the original run selection.

Summaries and the compact view label unexecuted tests and assertions **not reached**. These tests remain included in the summary counters, but **Problems only** hides not-reached entries.

Before running the test suite, a discovery pass counts individual tests across its test experiments. The compact view shows “Discovering tests...” during this pass, then a live completed/total counter in both execution modes, retaining partial counts after Stop or timeout.

- Test experiments work with `pref_parallel_simulations_all` enabled. Headless `-test` extracts
  the complete test tree and extension bundles ship their test suites
  ([`77e223678`](https://github.com/gama-platform/gama/commit/77e223678), #904;
  [`ed19a52b4`](https://github.com/gama-platform/gama/commit/ed19a52b4),
  [`a978b0013`](https://github.com/gama-platform/gama/commit/a978b0013)).
- **Run this test** executes only the GAML test under the caret, in either test execution mode.
  Failed assertions display evaluated operands; the first non-warning failure stops the test,
  while warning assertions allow it to continue. Test output can optionally be routed to the
  normal UI console, including from isolated workers
  ([`dfe167e9e`](https://github.com/gama-platform/gama/commit/dfe167e9e),
  [`63d08501f`](https://github.com/gama-platform/gama/commit/63d08501f),
  [`b2f39e9c0`](https://github.com/gama-platform/gama/commit/b2f39e9c0)).

#### GitHub Copilot: independent optional integration

Copilot packaging and GAMA-specific integration move to
[gama.copilot](https://github.com/gama-platform/gama.copilot), with a standalone
Tycho build, dependency baseline, GitHub Actions publishing and dedicated p2 site
at <https://gama-platform.org/gama.copilot/>. Copilot is still optional and is not
included in the default GAMA product.
The site is thin: it publishes only GAMA's wrapper and integration plug-in.
p2 resolves compatible upstream Copilot components from Microsoft and platform
dependencies from the fixed Eclipse 2026-03 repository, without mirroring them
or moving GAMA to a newer Eclipse release.

**Support > Install GAMA extensions... > Install Copilot extension** now uses that site rather than the running GAMA
release's update repository; subsequent updates use **Support > Check for updates...**.
License acceptance, trust checks, GitHub sign-in and entitlement requirements remain.
The installer checks for the GAMA wrapper feature, so an upstream-only Copilot
installation can also acquire the GAMA integration.

The main Maven reactor no longer builds or publishes `gama.feature.copilot` and
does not consult the upstream Copilot repository. Copilot-specific startup preferences
and UI adaptations belong to the standalone integration; generic GAML editor
extension support stays in GAMA. A new `gama.ui.application.startup` extension
point runs optional `Runnable` contributions before workspace selection and workbench
creation. See the [migration notes](MIGRATION_GUIDE_2026.md#optional-copilot-integration-and-pre-workbench-initialization)
and [installation documentation](.github/SUPPORT.md#installing-gama-extensions).

#### Optional installations and memory indicator

- The memory control shows **MEM xx%** beside garbage collection and refreshes the displayed
  usage after collection. Copilot and diagramming support are optional Support-menu installations;
  Copilot is no longer bundled into the default product
  ([`e069b5f52`](https://github.com/gama-platform/gama/commit/e069b5f52), #1254;
  [`755acd4d6`](https://github.com/gama-platform/gama/commit/755acd4d6),
  [`b815e0c8f`](https://github.com/gama-platform/gama/commit/b815e0c8f)).

#### GitHub Copilot integration (gama.copilot)

The optional Copilot integration now lives in its own repository,
[gama-platform/gama.copilot](https://github.com/gama-platform/gama.copilot), created in
November 2026. It packages the **unmodified**
[GitHub Copilot for Eclipse](https://github.com/microsoft/copilot-for-eclipse) for GAMA; it is
not a fork and adds no GAML-specific chat tools, MCP servers, agents or prompts of its own.
Copilot chat, code completion and authentication come from Microsoft's plug-in. Generic GAML
editor hooks stay in GAMA (see *GitHub Copilot: independent optional integration* above).

- **Installation.** Use **Support > Install GAMA extensions... > Install Copilot extension**, or
  add `https://gama-platform.org/gama.copilot/` in **Help > Install New Software...** and
  select **GitHub Copilot for GAMA** (`gama.feature.copilot`, the same feature ID as before).
  Restart GAMA and sign in with GitHub. You need a Copilot entitlement and network access.
  Updates use the normal **Check for updates** mechanism. The integration never updates
  Copilot agents silently.
- **Thin update site.** The GAMA site publishes only a small wrapper feature and the
  `gama.ui.copilot` adapter. Copilot core, UI, language server and native agents download
  directly from Microsoft's repository, and platform dependencies come from Eclipse 2026-03.
  The installer must be allowed to contact all update sites. Each GAMA build is kept under
  `releases/<run-id>-<attempt>/` behind a composite repository, so earlier wrapper releases
  remain available. Microsoft's site can change, however, so offline reinstallation of older
  releases is not guaranteed.
- **Compatibility.** The wrapper accepts Copilot for Eclipse `[0.21.0, 1.0.0)` and was
  initially validated with `0.21.0.202608060743`. Upstream 0.x updates therefore do not need
  a new GAMA build. The Eclipse baseline stays pinned to 2026-03 (Java 21+): if Copilot ever
  needs a newer platform, resolution fails instead of upgrading the platform. Supported
  platforms are Linux GTK x86_64, Windows x86_64 and macOS (Intel and Apple Silicon).
- **GAMA-specific adaptations.** Through GAMA's new `gama.ui.application.startup` extension
  point, the adapter runs before the workbench starts. It disables the Copilot quick-start
  page and automatic "What's New" display only when the user has not set these preferences.
  It does not start the Copilot UI early. A disabled activity hides Copilot's own status-bar
  item and menu, because GAMA already provides Copilot through its own menus and chat view.
  The adapter has no dependency on GAMA's Java API.
- **Fixes and infrastructure.** The wrapper now requires the Eclipse Terminal feature
  (`[1.0.200, 2.0.0)`). Copilot checks for this feature only at runtime, so without the
  requirement it could fail after installation. Builds use Tycho 5.0.2. GitHub Actions
  validate pull requests, publish to GitHub Pages and generate the p2 category from
  `pom.xml`. Third-party notices are explicit. Installation problems should be reported to
  gama.copilot, while chat, completion and sign-in bugs go to Microsoft's repository.

#### LLM plugin (gama.plugin.llm)

The LLM plugin (repository
[gama-platform-plugins/gama.plugin.llm](https://github.com/gama-platform-plugins/gama.plugin.llm),
branch `GAMA_2026-06`) is built on LangChain4j. It moved from the experimental plug-ins to the
regular GAMA plug-ins, now targets GAMA 2026-06 and is published on the plug-in update site in
the AI category.

- **Renamed from MCP to LLM.** The former `gama.plugin.mcp` is now the `gama.plugin.llm`
  bundle with the `gama.plugin.feature.llm` feature. Its example library moved from `MCP` to
  **LLM skill**.
- **The `llm` skill.** Agents with this skill get the `llm`, `chat_bot` and `chat_memory`
  attributes and can:
  - build chat models with `create_chat_model` (`model_type: "openai"` or `"ollama"`), or with
    `create_openai_chat_model` and `create_ollama_chat_model`. Models accept tuning options
    such as temperature, seed, top-p/top-k, token limits, retries, timeout and JSON or text
    response format;
  - build conversation memory (`create_chat_memory`) and use it with `fetch_memory` and
    `add_to_memory`;
  - send prompts with `send_to_llm` and `send_to_llm_without_memory`;
  - define GAML tool executors that the LLM can call (`create_tool_executor`,
    `add_tool_executor` and their `_from_json` variants);
  - connect to external MCP servers through `create_mcp_transport` and `create_mcp_client`.
    `create_client_executor` exposes their tools to the LLM;
  - create retrieval-augmented generation (RAG) content retrievers from a directory of
    documents (`create_rag`);
  - combine a model, memory, tools and RAG in an assistant (`create_assistant`,
    `send_to_assistant`).

  The plugin defines GAML types for these objects: chat models, memories, tool providers,
  MCP transports and clients, content retrievers and assistants.
- **Asynchronous requests (new).** `send_to_llm_async`, `send_to_llm_async_without_memory`
  and `send_to_assistant_async` return a request identifier immediately, so a simulation can
  keep running while waiting for the model. Use `has_llm_result`, `get_llm_result` (which
  returns `nil` while the request is still pending) and `cancel_llm_request` to manage these
  requests.
- **Robustness.** RAG works again after missing libraries were added. The release also fixes
  GAML errors and compilation warnings in the models and code. Memory operations are now
  thread-safe, error handling in tool providers is better, and request logging has been
  removed.
- **Models.** The library includes these examples: *Simple LLM syntax*, *Simple LLM
  Conversation*, *Use of Tools*, *Use of MCP Services*, *Use of Rag*, *Argumentation*, *Pop
  Synth & Mobility*, and the *SimpleLLMAgents_Fire* examples (calls without memory and
  multilingual agents). The quick start uses a local [Ollama](https://ollama.com/) server,
  for example with `ollama run llama3.2`.
- **Experimental (not yet distributed).** The repository also contains two prototypes that
  are not part of the published feature. `gama.extension.mcp` runs GAMA as an MCP server over
  WebSocket, implementing `tools/list` and `tools/call`, and includes a sample Python LLM
  client. `gama.ui.display.webrtc` provides a `webrtc` display type and *WebRTC Display* view
  for streaming displays.
- **Build.** Maven builds now run on multiple threads and cache dependencies in CI. The p2
  repository is deployed only from the default branch, and p2 categories are generated from
  each plugin's `pom.xml`. Feature metadata now includes GPL v3 headers and a license URL for
  each repository.

#### Graphical modelling (diagram editor)

The graphical modelling plugin ([gama-platform-plugins/gama.graphical.modeling](https://github.com/gama-platform-plugins/gama.graphical.modeling), bundle `gama.ui.diagram`) has moved to the new GAMA plugins organisation and been largely rewritten for GAMA 2026.

##### Packaging and compatibility
- Now distributed as a separate plugin (feature `gama.ui.feature.diagram`) from the GAMA plugins update site, listed in its own category. It is no longer bundled with the core platform.
- Updated for the GAMA 2026-06 API and the new GAML syntax. Diagrams are now imported from the compiler's model descriptions instead of executable models, so source bodies, explicit facets, inheritance and documentation are preserved.
- Automatic layout needs a GAMA installation whose `gama.dependencies` bundle exports Jungrapht. This is the case for GAMA 2026.

##### Live diagrams from GAML
- **Switch to diagram** now opens a **live, read-only view** of the current GAML editor, including unsaved edits. It no longer generates a `.gadl` file in a `diagrams` folder. Running the command again focuses the existing view.
- The diagram refreshes shortly after each edit. Positions, sizes, colours and connection bends are kept for declarations that keep the same name and location. While the model has errors, the last valid diagram stays visible with a status message.
- **Save** and the dirty indicator apply to the GAML source. Validation and experiment launching use the original GAML file. Closing the source also closes its live view.
- **Editable snapshots**: the toolbar **Edit** icon or **Save As** saves the current diagram as an independent `.gadl` file. You can then edit this file with the full palette, but changes to it do not update the GAML source.

##### Diagram editing
- Creating and editing species, grids, the model, actions, behaviours (reflexes, `init`, `abort`), experiments (including `test` experiments), displays and layers now uses **the same wizards as the GAML editor**. These wizards preview changes and validate them in context, and all changes can be undone.
- **GAML classes** are supported, with their own palette tools and styling. Inheritance between classes is checked to prevent cycles. Virtual actions are preserved.
- Import and generation now keep equation systems (differential equations and comments), display-layer bodies, raw source and trailing comments.
- The palette has been reorganised: sections have coloured headers that match the node colours, tool labels are consistent, and there is a single entry for creating experiments.

##### Appearance and navigation
- Updated look: rounded boxes with pastel fills, readable text colours, thinner colour-coded relations, multiline attributes and new SVG icons. Diagrams from older versions are converted to the new look when opened, and grid snapping is disabled.
- **Layout** uses GAMA's Jungrapht Sugiyama hierarchical algorithm, from top to bottom. Boxes are first fitted to their contents. Connections use orthogonal routes with filled arrowheads and routing lanes that avoid crossing boxes. Opening an existing diagram keeps its saved positions.
- New toolbar controls for zooming in and out and for fitting the diagram to the view. Trackpad pinch zooming is supported, and dragging the background pans the canvas. Diagrams fit the view when first opened.

#### Bundled library models

- Library additions include **Game of Life with time** and **1D Cellular Automaton Generator**,
  alongside dataframe, physics and matrix-computation examples
  ([`b06758d31`](https://github.com/gama-platform/gama/commit/b06758d31),
  [`951060883`](https://github.com/gama-platform/gama/commit/951060883)).
- New **SIR Analysis** tutorials cover Latin hypercube, Morris, Sobol, stochasticity and
  beta-distribution exploration; **Particle Life** demonstrates emergent particle interactions.
  Pedestrian-network examples improve free-space connectivity; traffic, grid paths, raster
  import, driving and color-brewer demonstrations are repaired. The broken satellite-image
  download is removed from the OSM example
  ([`e45234051`](https://github.com/gama-platform/gama/commit/e45234051),
  [`cb8be0089`](https://github.com/gama-platform/gama/commit/cb8be0089),
  [`44b66f048`](https://github.com/gama-platform/gama/commit/44b66f048),
  [`4d99bcf52`](https://github.com/gama-platform/gama/commit/4d99bcf52), #815;
  [`aec780cfa`](https://github.com/gama-platform/gama/commit/aec780cfa),
  [`4b23d9304`](https://github.com/gama-platform/gama/commit/4b23d9304),
  [`2954e8a44`](https://github.com/gama-platform/gama/commit/2954e8a44),
  [`f08628d68`](https://github.com/gama-platform/gama/commit/f08628d68),
  [`f337ef343`](https://github.com/gama-platform/gama/commit/f337ef343),
  [`4b81346f3`](https://github.com/gama-platform/gama/commit/4b81346f3)).

#### Shared models (gama.shared.models)

- **New "GAMA Shared Models" extension.** This new installable tool (feature `gama.feature.shared.models`, listed in the *Tool* category) adds a **Community** folder above **User** in the GAMA navigator. Use it to browse community projects from [gama-platform/gama.models](https://github.com/gama-platform/gama.models) and import them into your workspace.
- **Two catalogues.** *Published models* reads a public JSON registry with version, description, author, license and GAMA compatibility for each model. *Repository projects* lists the project folders in the repository, grouped by category (e.g. Generic, Thematic). A project appears as "remote project, N models". Expanding it shows its metadata (author, description, version, license, repository), not its files. Browsing never downloads archives or creates workspace projects.
- **Import into workspace.** The context menu of a remote project offers **Import into workspace**, **Properties**, **Open repository** and **Refresh** (F5). Importing asks for a local project name, then copies the whole project into **User** as an independent local copy. The copy records its source and version or commit as project properties, and existing projects are never overwritten. Imports are verified: SHA-256 checksums are checked, size and entry counts are limited, unsafe paths are rejected, and legacy GAMA project natures are normalized. Network operations run as cancellable background jobs. If the network is unavailable, cached catalogues are used with an explicit offline/stale notice. No GitHub account is needed.
- **Installation.** The tool is published as a versioned p2 update site at `https://gama-platform.org/gama.shared.models/`. GAMA 2026 already lists this site in **Install GAMA extensions…**, so the tool can be installed there with the other extensions. It is built on the new navigator API (`gama.ui.navigator.topLevelFolders`) that lets plug-ins add top-level folders to the navigator. Current limitations: only ZIP projects can be imported, and only their latest version. Version selection, `.gamp`/`.gamx` import and automatic updates are planned.

#### Experimental plugins (gama.experimental)

- **Repository migration.** The `gama.experimental` repository is now archived as a legacy source. Plugins for GAMA 2026 are maintained in the [gama-platform-plugins](https://github.com/gama-platform-plugins/) organization.
- **New flooding plugin (`gama.experimental.feature.flooding`).** A new `SpreadingSkill` simulates water spreading over a terrain. It supports rain functions, the construction and removal of dykes, and file-path parameters, and its spreading algorithm has been optimized. It includes a *Spreading Example* model based on Hanoi water polygons.
- **Argumentation plugin.** The plugin now has its own feature and can be installed from GAMA's extension installer. Its JDK configuration and p2 metadata were also fixed.
- **MCP / LLM plugin (`gama.experimental.mcp`).** The `LLMSkill` gains skill variables, and its action argument names are fixed. Tools can now be defined from a JSON string. The example models (LLM conversation, RAG, tools, MCP services, population synthesis) were updated to match.
- **MPI plugin.** The plugin was updated for the new GAMA release. It is built again and comes with new example models (`send_recv`, `mpi_scatter`, `rank_size`) and `startHeadless` / `startMpiModel` launch scripts.
- **Build and distribution.** The p2 update site was moved to GAMA 2025.6.4 and later updated to meet the SSH minimal server requirements. The CI now downloads Maven automatically. The *Fuzzy logic* and *MATLAB* plugins are disabled because they no longer compile with GAMA 2025-06.

#### Icons and resources (gama.resources)

- `gama.resources` received a new SVG icon generator (`GamaSVGIconsProducer`, used alongside the renamed `GamaPNGIconsProducer`) and new or redrawn light and dark SVG icons (event layer, navigator folders, view tabs, modeling perspective). The final GAMA Days 2024 website was archived, and a style kit for GAMA Days 2026 was added.

### 2. Experimenting and visualising

Everything that happens once an experiment runs: experiments, simulations and batch execution,
parallelism, parameters and UI controls, consoles, displays and layers (with clarified layer and
overlay coordinates), the OpenGL and new OpenGL4 renderers, charts, keyboard and mouse listeners,
the GAMA Server infrastructure and simulation performance.

- **Experiments, simulations and batch:** [Development follow-up: explicit experiment and simulation contexts](#development-follow-up-explicit-experiment-and-simulation-contexts) · [Experiment execution controls and pause-state migration](#experiment-execution-controls-and-pause-state-migration) · [Platform-agent operations](#platform-agent-operations) · [Simulation concurrency defaults](#simulation-concurrency-defaults) · [Simulation lifecycle, scheduling and batch execution](#simulation-lifecycle-scheduling-and-batch-execution) · [Concurrency and parallel execution](#concurrency-and-parallel-execution) · [Automatic spatial-index synchronization](#automatic-spatial-index-synchronization) · [Passive populations and agent access](#passive-populations-and-agent-access) · [Runtime errors: persistent, clickable status](#runtime-errors-persistent-clickable-status)
- **Parameters, consoles and UI controls:** [Parameters: display units (`unit:` facet)](#parameters-display-units-unit-facet) · [Parameters, monitors and launch controls](#parameters-monitors-and-launch-controls) · [Consoles: persistent contents, interactive console and history](#consoles-persistent-contents-interactive-console-and-history) · [Swiss-inspired simulation and editor palettes](#swiss-inspired-simulation-and-editor-palettes)
- **Displays, layers and coordinates:** [Display viewport dimensions and mouse-coordinate aliases](#display-viewport-dimensions-and-mouse-coordinate-aliases) · [Overlays and display reliability](#overlays-and-display-reliability) · [Draft follow-up: persistent output presentation locals](#draft-follow-up-persistent-output-presentation-locals) · [Static layer visibility](#static-layer-visibility) · [CPU-side display preparation](#cpu-side-display-preparation) · [Java2D display responsiveness](#java2d-display-responsiveness) · [Snapshots and image saving: faster, optionally asynchronous](#snapshots-and-image-saving-faster-optionally-asynchronous) · [Additional display fixes](#additional-display-fixes)
- **OpenGL and OpenGL4:** [OpenGL4: native OpenGL 4.1 core-profile renderer](#opengl4-native-opengl-41-core-profile-renderer) · [OpenGL4 geometry reuse](#opengl4-geometry-reuse) · [OpenGL4 chart textures](#opengl4-chart-textures) · [OpenGL4 text contours](#opengl4-text-contours) · [OpenGL4 fixed-pipeline lighting](#opengl4-fixed-pipeline-lighting) · [OpenGL4 raster uploads](#opengl4-raster-uploads) · [OBJ loading and rendering](#obj-loading-and-rendering) · [Improved OpenGL rendering for maps and map-like environments](#improved-opengl-rendering-for-maps-and-map-like-environments) · [OpenGL text and polygon drawing reuse](#opengl-text-and-polygon-drawing-reuse) · [OpenGL FPS overlay shutdown](#opengl-fps-overlay-shutdown) · [Optional OpenGL rendering diagnostics](#optional-opengl-rendering-diagnostics)
- **Charts:** [Charts: dynamic bodies, distinct names and stable colors](#charts-dynamic-bodies-distinct-names-and-stable-colors) · [Chart legends, bounds and default properties](#chart-legends-bounds-and-default-properties) · [Independent series-chart rendering](#independent-series-chart-rendering) · [Chart image sizing in OpenGL](#chart-image-sizing-in-opengl)
- **Keyboard and mouse listeners:** [BIG CHANGE: keyboard combinations in display events](#big-change-keyboard-combinations-in-display-events) · [Generic keyboard and mouse event listeners](#generic-keyboard-and-mouse-event-listeners) · [BEHAVIOR CHANGE: macOS 3D camera controls use CMD, not CTRL](#behavior-change-macos-3d-camera-controls-use-cmd-not-ctrl)
- **GAMA Server:** [GAMA Server: full exchange logs](#gama-server-full-exchange-logs) · [GAMA Server: desktop server disabled by default](#gama-server-desktop-server-disabled-by-default) · [GAMA Server, headless and TCP follow-ups](#gama-server-headless-and-tcp-follow-ups)
- **Simulation performance:** [Spatial routing and movement performance](#spatial-routing-and-movement-performance) · [Quadtree query and update speed](#quadtree-query-and-update-speed) · [Octree query and update speed](#octree-query-and-update-speed) · [Movement along paths and graphs](#movement-along-paths-and-graphs) · [Routing cache and weight-update speed](#routing-cache-and-weight-update-speed) · [Endpoint route candidate construction](#endpoint-route-candidate-construction) · [Shared container type descriptions](#shared-container-type-descriptions) · [Two-argument numeric minimum and maximum](#two-argument-numeric-minimum-and-maximum) · [Fixed-argument operator invocation](#fixed-argument-operator-invocation) · [Runtime arguments and population queries](#runtime-arguments-and-population-queries)

#### Development follow-up: explicit experiment and simulation contexts

Experiment code no longer implicitly borrows the current simulation's ordinary species
populations. Select a simulation explicitly for creation and population access. Creation of
directly imported comodel experiments remains supported.
Direct population operations on a statically known model species in experiment context now
produce compilation errors; species values remain usable, and dynamic accesses are guarded at runtime.

Experiments can opt their directly declared actions into the current simulation
with `actions_context: simulation`, reducing adapter boilerplate without changing
action ownership or implicitly selecting all simulations. Imported model simulations
remain valid simulation-creation targets in an experiment.

An experiment's `user_command` now selects its owner with `of: simulation` (the default) or
`of: experiment`, like parameters. Compilation, action lookup and execution follow that owner.
The former `action:` fallback that ran a global action on every simulation is removed; a
simulation-owned command targets one simulation. Commands in global, species and user panels
keep their existing context; monitors are unchanged.

The experiment's `until:` and `record:` and batch method `minimize:`/`maximize:` compile in the
model context and evaluate in the relevant simulation. Exploration `outputs:` names simulation
attributes; other method configuration facets use experiment context. Optimization configuration
is initialized in the experiment scope. The new per-facet `@facet(context = ...)` metadata
separates static name resolution from runtime scope selection without changing structural ancestry.

These are development changes, not a released compatibility promise. See the
[model migration](MIGRATION_GUIDE_2026.md#experiment-and-simulation-ownership),
[plugin guidance](MIGRATION_GUIDE_2026.md#facet-compilation-contexts-for-plugin-developers) and
[experiment guide](EXPERIMENT_AGENT.md#experiment-and-simulation-contexts).

#### Experiment execution controls and pause-state migration

Experiment agents expose `play`, `pause`, `step_once`, `step_back`, `synchronize`, `desynchronize` and `reload`.
Execution commands target the receiving experiment's controller and all its scheduled simulations, using the
existing GUI/server execution paths. Stepping, rewinding and reload requests are deferred to the current
experiment-cycle boundary and processed asynchronously. Rewinding requires recording (`record: true`) and an
available recorder. Reload replaces the experiment and simulation agents; reacquire references afterwards.
`die` remains the experiment-close action; it does not quit GAMA. Non-interactive headless controllers reject
unsupported execution controls with a runtime error. Display synchronization remains platform-wide.

Read-only `experiment.status` uses the server status query (`RUNNING`, `PAUSED`, `NONE`).
`experiment.is_paused` is equivalent to `status = "PAUSED"`; `experiment.is_synchronized` reports the shared
rendering synchronization flag. Read-only `simulation.is_on_user_hold` reports only the simulation's
user-interaction hold, independently of the controller pause state. A held user panel can coexist with
`RUNNING`; `play` does not release that hold.

**Migration:** simulation/model-species `pause` and `resume` are deprecated delegates, retaining their `nil`
return. Replace them with `ask experiment { do pause(); }` and `ask experiment { do play(); }`.
Simulation `paused` is deprecated and now read-only; its combined getter remains available. Read
`experiment.is_paused` for scheduler pause, `simulation.is_on_user_hold` for user hold, or their logical OR
for the combined state with an active controller. The former `paused` setter did nothing; assignments now fail
validation. Replace assignments intended to pause/resume execution with experiment actions, not assignments
to the new read-only flags. See the [experiment-agent guide](https://github.com/gama-platform/gama/wiki/ExperimentAgent)
for all controls, existing maintenance actions, limitations and examples.

#### Platform-agent operations

The unique `gama` platform agent now offers GAML actions to inspect exposed preferences, update or reset them in memory
or persistently, open a model experiment, run a model's test experiments, validate models in the workspace, validate
GAML source snippets, and launch a playable `.gamx` package in its isolated player. Opening an experiment asks before
replacing an active one by default; `confirm: false` is available for intentional scripted chains. Preference changes
are temporary by default and restore on experiment close; `persist: true` keeps the updated value in the preference
store. Model and test operations accept file paths or workspace-relative paths; GUI experiment opening and `.gamx`
playback require the workbench. See the [platform-agent guide](https://github.com/gama-platform/gama/wiki/PlatformAgent)
for examples and limitations.

**Migration:** no existing GAML syntax or Java API needs to change. These are additive `gama` actions. Scripts that
use `confirm: false` can close the active experiment without prompting, and `persist: true` changes a user-wide
preference beyond the current experiment.

#### Simulation concurrency defaults

The global preference for parallel simulation scheduling has been removed. Experiments without a `parallel:` facet
now step their simulations sequentially, including batch experiments. Set `parallel: true` on the experiment to use
the configured thread count, or `parallel: n` to specify the maximum number of concurrent simulations. Headless
non-batch experiments remain sequential. Each simulation still has its own thread regardless of the scheduling limit.
Existing models and scripts that relied on `gama.pref_parallel_simulations` must use the experiment facet instead.

Batch experiments now default to grouping simulations from different parameter sets in both GUI and headless modes.
Use `group_parameter_sets: false` to process one parameter set at a time. This choice changes when experiment
behaviors and outputs are stepped and can affect optimization behavior; it does not set the concurrency limit.
The former `gama.pref_parallel_simulations_all` preference has been removed. GUI batches that relied on its default
of `false` should use `group_parameter_sets: false` to preserve their evaluation strategy.

#### Simulation lifecycle, scheduling and batch execution

- Batch experiments resolve a default exploration when no method is declared. Default float
  sweeps include the minimum and booleans can be explored without artificial bounds.
  Exploration plans accept variable references for outputs and explicit parameter sets
  ([`9677cbd46`](https://github.com/gama-platform/gama/commit/9677cbd46),
  [`7ea745b2a`](https://github.com/gama-platform/gama/commit/7ea745b2a),
  [`508f96f57`](https://github.com/gama-platform/gama/commit/508f96f57),
  [`0d2a66924`](https://github.com/gama-platform/gama/commit/0d2a66924)).
- Exploration panels report the plan and estimated sample count, handle strings and honor
  `among:` choices. CSV plans resolve relative to the model; omitted output paths default to
  `<experiment>_results.csv`. `results:` controls the raw-results path and `outputs:` selects
  tracked variables; simulation time can be captured as an output
  ([`8095b1ba6`](https://github.com/gama-platform/gama/commit/8095b1ba6),
  [`06898167d`](https://github.com/gama-platform/gama/commit/06898167d), #833;
  [`fb8a15779`](https://github.com/gama-platform/gama/commit/fb8a15779),
  [`552b0985a`](https://github.com/gama-platform/gama/commit/552b0985a),
  [`43694316a`](https://github.com/gama-platform/gama/commit/43694316a)).
- Simulation initialization, stepping, recording and restoration run through dedicated
  per-simulation execution threads. Execution scopes isolate invocation arguments, callers,
  topology overrides and parallel tasks, preventing scope leaks and cross-simulation races.
  The `thread` skill uses virtual threads
  ([`0df6b1f5c`](https://github.com/gama-platform/gama/commit/0df6b1f5c),
  [`1bcfcb397`](https://github.com/gama-platform/gama/commit/1bcfcb397),
  [`408a129c1`](https://github.com/gama-platform/gama/commit/408a129c1),
  [`f26348be7`](https://github.com/gama-platform/gama/commit/f26348be7)).
- Batch simulations run autonomously, honor their own `until` conditions, and are collected
  and replaced as they finish. Runner threads terminate, status/reload handling is corrected,
  and simulations that die during initialization or execution no longer leave the batch waiting
  forever. Failed explicit explorations report their runtime error and terminate the failed run
  ([`add34b1b5`](https://github.com/gama-platform/gama/commit/add34b1b5),
  [`3b15a4b23`](https://github.com/gama-platform/gama/commit/3b15a4b23),
  [`d142ceefb`](https://github.com/gama-platform/gama/commit/d142ceefb);
  #198, #199, #282, #325, #358, #418).
- `using` restores the previous topology override rather than pinning an agent's topology
  into subsequent operations. Model-level `schedules:` / `frequency:` declarations survive
  compilation instead of being lost during built-in species handling
  ([`f51cb0688`](https://github.com/gama-platform/gama/commit/f51cb0688),
  [`2783493fe`](https://github.com/gama-platform/gama/commit/2783493fe), #918).
- Restoring a recorded simulation retains the restored snapshot in history, so subsequent
  backward navigation does not skip it
  ([`e98780140`](https://github.com/gama-platform/gama/commit/e98780140), #829).
- Excessive nested action calls report a GAML stack-overflow error while retaining the original
  cause for diagnostics, rather than exposing only an unhandled Java `StackOverflowError`
  ([`7e640862f`](https://github.com/gama-platform/gama/commit/7e640862f)).
- Experiment-parameter initialization now rejects action calls with an explanation: defaults
  are evaluated before a simulation exists. Use a simulation-independent expression or an
  experiment-level parameter declaration. Imports retain declaration order
  ([`3106d98aa`](https://github.com/gama-platform/gama/commit/3106d98aa), #1167;
  [`73a0a1c6c`](https://github.com/gama-platform/gama/commit/73a0a1c6c), #838).
- Experiment agents expose `is_gui`. Co-modeling repairs aliased-model type resolution and
  experiment/simulation population lookup; updated examples use standalone experiment
  adapters instead of requiring coupling-specific experiments in base models
  ([`c32f8820a`](https://github.com/gama-platform/gama/commit/c32f8820a),
  [`0a8fe4f0c`](https://github.com/gama-platform/gama/commit/0a8fe4f0c),
  [`a5ff1a3c5`](https://github.com/gama-platform/gama/commit/a5ff1a3c5),
  [`55eaa76cd`](https://github.com/gama-platform/gama/commit/55eaa76cd)).

#### Concurrency and parallel execution

Massive performance upgrades taking advantage of JDK 25 multithreading enhancements for parallel agent execution.

*   `[65a958e]` Improve ForkJoin executor and parallelism cache
*   `[5e637b9]` Improve thread-safety and performance in statements
*   `[dd3af06]` (via `a61ece7`) Thread-safe registry handling and Atomic ops

#### Automatic spatial-index synchronization

Spatial-index synchronization is now decided automatically for each simulation before its indices are created.
The decision covers species and grid scheduling configured through model-level `parallel:` facets, parallel `ask`
statements in nested or inherited behaviors, and species using the `thread` skill.
Interactive experiments are synchronized because display event handlers, rendering, and agent inspectors can
access simulation state from other threads. Displays and event layers also enable synchronization in headless
experiments. The selected experiment's behaviors and outputs are considered; other experiments are excluded.

Nonconstant `parallel:` expressions conservatively enable synchronization even if their initial value is `false`
or `0`, because they can enable concurrency later. Constant `false` or `0` does not request synchronization.
The decision considers potential concurrency rather than the current population size, so an initially small
population remains protected if it later exceeds its parallel-execution threshold.

Concurrent simulations synchronize complete quadtree/octree operations and compound-index operations, including
lazy population registration and species caches. Headless simulations with no concurrent execution or display
retain the unsynchronized fast path.
Synchronization is retained across environment rebuilds and topology merges; an environment rebuild can enable
it when needed, but does not disable it once enabled. The decision does not modify or persist any user preference.
Running independent batch simulations in parallel does not, by itself, require synchronized spatial indices.

Synchronization can reduce throughput in models with many concurrent spatial queries or moving agents.
It protects index operations, not arbitrary shared agent state, and does not make parallel execution reproducible.

Display event callbacks are scheduled on the simulation thread between steps instead of executing on the display
input thread; if there is no simulation, they are scheduled on the experiment. Mouse positions are captured when
the event arrives, including for specific mouse events, so delayed callbacks use the originating position.
Callbacks may run later when the simulation is busy, and pending callbacks are discarded if their display closes
or their simulation dies or changes. Independently running `thread` skill actions can still access agents concurrently.
For specific `#mouse_move` and `#mouse_drag` layers, consecutive pending motions from the same layer are coalesced
to the latest position, normally at most once every 33 milliseconds. An intervening event flushes the pending
motion first. Mouse buttons, keys, other layers, and generic `#mouse_listener` events are not coalesced.

OpenGL animation now renders on its animator thread rather than waiting for the SWT event queue. Its optional
frame-rate limit uses the duration of each frame, and ordinary mouse movement no longer reads GPU depth pixels
for every event. Both OpenGL backends let SWT size and embed native windows before showing them, avoid an extra
native fullscreen round-trip on their first appearance unless their host changed shells, and suppress background
erasure in the native host. Hidden launch canvases only attach their native window when revealed.
The SWT placeholder matches the display background. A cleared background is presented during startup, and
subsequent buffer swaps require a complete rendered scene, avoiding flicker while the first scene is prepared.

**Migration:** the spatial-index synchronization preference and its GAML variable
`gama.pref_synchronize_quadtree` have been removed. Delete reads, assignments, and save/restore code referring to
that variable; no replacement setting is needed. Previously saved values no longer affect index creation.
The bundled Boids and Preferences examples have been updated accordingly.

#### Passive populations and agent access

Standard populations with the default reflex architecture skip automatic scheduling when they have no reflexes,
variable updates, overridden step or micro-species. Initialization, explicit actions and explicit agent steps remain
unchanged. Custom agents, architectures and populations, mirror species and explicit scheduling/frequency/parallel facets
retain their normal scheduling path.

Stored attributes on standard agents are read with one lookup; missing attributes still use their initialization fallback,
while explicitly stored nil values remain nil. Standard population updates reuse their resolved variable declarations,
preserving coercion, bounds and change notifications. Execution scopes reuse their most recent immutable agent
frame for an identical object/outer-context pair, without recycling mutable execution contexts, allocating caches
in short-lived scope copies or sharing mutable cache state between scope copies.

#### Runtime errors: persistent, clickable status

Runtime errors, including experiment launch failures (#286), remain visible as red text on the normal top-right status background
until clicked, rather than being replaced by background activity or the idle timer. Click the error text to
open its source location in the editor; hover to read the full error, or right-click to open an error-details
dialog. If no source location is available, clicking opens the details instead. The dropdown arrow still
opens status history, where errors also provide their full text on hover and link to the editor.

#### Parameters: display units (`unit:` facet)

The `unit:` facet of `parameter` (#197) now controls how numeric values are displayed in the
parameters view. It affects display only: stored values, `min:`, `max:` and `step:` are unchanged.

| GAML | Display |
|---|---|
| `parameter "Duration" var: d min: 1#h max: 10#h unit: #h;` | `9000.0` (s) is shown as `2.5 h` |
| `parameter "Speed" var: v unit: #km/#h;` | `10.0` (m/s) is shown as `36 km/h` |
| `parameter "Density" var: dens min: 0.0 max: 1.0 unit: #percent;` | `0.7` is shown as `70 %` |
| `parameter "Speed" var: v unit: "#km/#h";` | Same conversion: the string is a numeric expression |
| `parameter "Flood speed" var: f unit: "m/min";` | Label only, no conversion |
| `parameter "Opening hour" var: h min: 6 max: 12 unit: "h";` | Int parameters accept labels only |

- Numeric units (constants, compound expressions or strings that compile to them) convert float values;
  the label is taken from the expression as written (`#km/#h` is shown as `km/h`, `#percent` as `%`).
  Use `#percent` for fractions in [0,1], and the label `"%"` for values that are already percentages.
- Sliders show the converted value and its unit; float and int text editors keep the stored value
  editable and show the conversion (`= 36 km/h`) or the label, in gray, beside it.
- Slider values and unit annotations are left-aligned in a fixed column, so editors no longer shift
  while a slider is moved, and adding a unit no longer changes the displayed precision.
- Non-constant numeric units, non-positive factors and numeric units on non-float parameters are
  rejected at compilation. Calendar-dependent units (`#month`, `#year`) are not supported.
- Library models now use units where relevant (Road Traffic, Moussaid, Incremental Model, SIR,
  Schelling, Sugarscape, Clock, Life, Ants, Boids, City Escape, Vote, etc.), and the
  **Declaration of Parameters** recipe illustrates each form.

#### Parameters, monitors and launch controls

- Numeric parameter editors hide increment/decrement buttons with `among:`, accept inclusive
  bounds and handle nil initial values. Monitors appear in the Parameters view when enabled,
  and the Synchronize button reflects the state after reload
  ([`d1695b059`](https://github.com/gama-platform/gama/commit/d1695b059), #787;
  [`7df44fef2`](https://github.com/gama-platform/gama/commit/7df44fef2),
  [`bf928c256`](https://github.com/gama-platform/gama/commit/bf928c256), #1175;
  [`d1f7b92cd`](https://github.com/gama-platform/gama/commit/d1f7b92cd),
  [`23de20086`](https://github.com/gama-platform/gama/commit/23de20086), #506).
- The launching overlay avoids duplicate messages, remains above native OpenGL canvases,
  coordinates toolbar/perspective transitions and exits cleanly on startup failure
  ([`b10bf7cfe`](https://github.com/gama-platform/gama/commit/b10bf7cfe), #1056;
  [`59f044fd9`](https://github.com/gama-platform/gama/commit/59f044fd9),
  [`59a6a3659`](https://github.com/gama-platform/gama/commit/59a6a3659), #1055;
  [`2cb94bf56`](https://github.com/gama-platform/gama/commit/2cb94bf56)).
  It also provides cancellation, model/experiment identification and scrollable startup output;
  display views are opened and laid out as a batch to reduce flashing
  ([`53c71df23`](https://github.com/gama-platform/gama/commit/53c71df23),
  [`7d2f9ba66`](https://github.com/gama-platform/gama/commit/7d2f9ba66), #1018).
- Simulation-speed sliders use normalized positions; a failed list-editor parse preserves
  the original value rather than replacing it with an empty list
  ([`21f9f7f7e`](https://github.com/gama-platform/gama/commit/21f9f7f7e),
  [`bc2d1af4d`](https://github.com/gama-platform/gama/commit/bc2d1af4d)).

#### Consoles: persistent contents, interactive console and history

- The normal console is available in the Modeling perspective and can retain output across
  experiments with **Keep contents** (`pref_keep_console`, enabled by default); word wrapping
  can be changed directly in the view. Initialization errors survive partial-simulation cleanup
  and the Errors view remains available instead of closing with simulation views
  ([`d2e2501db`](https://github.com/gama-platform/gama/commit/d2e2501db),
  [`1b271714c`](https://github.com/gama-platform/gama/commit/1b271714c),
  [`dd899e6bd`](https://github.com/gama-platform/gama/commit/dd899e6bd),
  [`0baff3fcc`](https://github.com/gama-platform/gama/commit/0baff3fcc)).
- The interactive console retains declared variables across commands and permits later
  reassignment. Assigning model variables works again, including through server expressions
  ([`a99907f2a`](https://github.com/gama-platform/gama/commit/a99907f2a), #202;
  [`70026eb8d`](https://github.com/gama-platform/gama/commit/70026eb8d), #1140).
- Interactive-console history navigation preserves draft input and respects prompt/editing
  boundaries, with startup help and clearer errors
  ([`e3c873782`](https://github.com/gama-platform/gama/commit/e3c873782)).

#### Swiss-inspired simulation and editor palettes

Swiss, Warm Amber and Mineral add eight-color categorical palettes alongside
the existing simulation/chart schemes, also available through
`vintage_colors(name)` in GAML. Swiss becomes the default for unset simulation
and chart preferences; default agents use muted blue and highlights burnt orange.
Saved preferences and explicit model colors are preserved.
Box2D's cream paper (`#f8f6f0`) is used by the splash and default simulation/chart
backgrounds. Theme > UI offers an opt-in custom-background switch (off by default)
and separate light/dark UI colors, all requiring restart. System mode restores
the original Eclipse/SWT backgrounds.

The same families provide optional light/dark editor configurations alongside
GAMA 2026 and GAMA 2025. Each configuration includes independently editable
light/dark editor backgrounds, applied on Save and included in version 2 exports;
version 1 imports remain supported. Game of Life (grid agents), the basic agent-based SIR
and simple Lotka-Volterra demos receive a paper-and-ink visual treatment.
See [palette documentation and examples](PALETTES.md).

#### Display viewport dimensions and mouse-coordinate aliases

`#display_width` and `#display_height` now always return the visible viewport dimensions in logical pixels,
independent of zoom and layer referential. In graphical scopes, including `event` callbacks, they use the
originating display; outside graphics they use the frontmost display, or return `0` if none exists.
They no longer return the zoomed world's pixel extent in world contexts and never denote overlay-local size.
`#px` remains contextual: one logical pixel in screen contexts, a pixel-to-model-unit conversion in world contexts.

`#mouse_location_in_world` joins `#user_location` and `#user_location_in_world` as a world-coordinate synonym.
`#mouse_location_in_display` is a synonym of `#user_location_in_display`, measured from the viewport's top-left
in logical pixels. The old `#mouse_world_location` and `#mouse_screen_location` names remain compatibility aliases.
All aliases retain the originating mouse-event snapshot behavior.
Both OpenGL backends convert native mouse-event pixels to logical viewport pixels using the actual native
surface scale, fixing undersized overlay hit areas on HiDPI displays. Camera picking retains physical pixels.
World-layer layout uses non-negative fitted viewport dimensions scaled by zoom instead of a camera-projected
point, preventing valid layer sizes from becoming negative during camera movement. Camera-matrix reads used
for projected world coordinates are synchronized with updates in both OpenGL backends.

**Migration:** review dimension expressions that relied on zoom-dependent world extents. Use viewport mouse
coordinates with viewport dimensions, and world mouse coordinates with world geometry; point arithmetic does
not convert between referentials. Multiplying viewport dimensions by `#zoom` is not a general replacement for
the former extent because aspect-ratio fitting and projection also affect it.
See the [migration reference](MIGRATION_GUIDE_2026.md#display-dimensions-and-mouse-coordinates)
and [display-coordinate guide](gama.documentation/files/input/markdown/DisplayCoordinates.md).

#### Overlays and display reliability

- Overlay position/size interpretation is shared by Java2D, OpenGL, OpenGL4 and image exports.
  Coordinates independently accept view pixels, view fractions or model units; negative positions
  anchor from the right/bottom. Small pixel sizes remain pixels, borders/corners agree across
  renderers, and resizing/fullscreen transitions refresh geometry. **Migration:** overlay contents
  needing pixel sizing should use `#px`; see the [overlay migration reference](MIGRATION_GUIDE_2026.md)
  ([`fce05cad0`](https://github.com/gama-platform/gama/commit/fce05cad0),
  [`bd7b8e276`](https://github.com/gama-platform/gama/commit/bd7b8e276), #354, #589, #656, #997).
- Fractional image dimensions are retained, preventing tile gaps and inaccurate rotation/scaling.
  OBJ render-cache keys distinguish material variants, so differently textured instances no
  longer incorrectly share cached geometry
  ([`c3bd024bd`](https://github.com/gama-platform/gama/commit/c3bd024bd), #668;
  [`7d8708dff`](https://github.com/gama-platform/gama/commit/7d8708dff), #644).
- Java2D grid colors remain aligned with cell borders and agent aspects when zooming or panning
  with antialiasing disabled, including in the Tools Panel example (#927).
- Traces isolate their transformation matrices, preventing layer elevation from accumulating
  across frames. Autorun displays with `refresh: false` retain their initial textures, and
  skipped OpenGL refreshes are retried even after the simulation pauses
  ([`5df2104df`](https://github.com/gama-platform/gama/commit/5df2104df), #1098;
  [`3e6a69aa0`](https://github.com/gama-platform/gama/commit/3e6a69aa0),
  [`685229a36`](https://github.com/gama-platform/gama/commit/685229a36), #715).
- Grid borders are rendered as one multilinestring instead of a rectangle per cell, reducing
  scene overhead and preventing disappearing fills/borders during camera movement on large grids
  ([`f337031c9`](https://github.com/gama-platform/gama/commit/f337031c9), #1156).
- Fullscreen defaults to the monitor containing GAMA's main window when no monitor is specified.
  Returning from an initially fullscreen display no longer leaves a blank macOS canvas.
  OpenGL refreshes its native surface scale across mixed-DPI monitors and fullscreen transitions
  ([`80daed784`](https://github.com/gama-platform/gama/commit/80daed784), #810;
  [`04f1d704a`](https://github.com/gama-platform/gama/commit/04f1d704a), #1214;
  [`3095ffa7c`](https://github.com/gama-platform/gama/commit/3095ffa7c)).
- Switching inspected agents refreshes monitor values instead of retaining the previous agent's
  value. Matrix parameter editing restores row controls and validates on OK, leaving the dialog
  open on invalid input rather than failing during disposal
  ([`a138bbf79`](https://github.com/gama-platform/gama/commit/a138bbf79), #383;
  [`da4739414`](https://github.com/gama-platform/gama/commit/da4739414), #936).

#### Draft follow-up: persistent output presentation locals

**Status:** implemented on the branch of draft
[PR #1313](https://github.com/gama-platform/gama/pull/1313), including
[`828cff24c`](https://github.com/gama-platform/gama/commit/828cff24c50f24b1e173f9d1c3418ac09a2c13cf),
not yet merged or released. This entry is separate from the historical audit above.

Typed declarations directly inside `output` and `permanent` now define persistent
lexical locals. Model authors can move presentation-only fonts, colors, positions and other
styling out of experiment attributes without turning them into simulation globals.
Output locals are available to child outputs, including display facets, layers, inline
callbacks and monitors. They are not agent attributes. Declarations directly inside
`display` are not supported; place them in the enclosing output block instead. In particular,
`image name;` inside a display always denotes an image layer, not a variable declaration.

The output owner controls initialization and lifetime: omitted `of:` and `of: simulation`
create independent storage per simulation; `of: experiment` and legacy `permanent` keep
experiment-owned storage across simulations. Declarations initialize once, in order,
before facets, and remain live across refreshes and reopenings of the same instance.
Experiment output locals are prepared before early layout evaluation, after experiment
attributes but before its GAML `init` behavior.

Remote inline layers and event/control callbacks preserve lexical bindings while `self`
denotes their target. Closest lexical declarations take precedence over homonymous target
attributes; explicit receiver access remains available. Overlay controls can bind persistent
locals, but still reject transient variables. Detached graphics/callback scopes share dedicated
local storage without retaining transient caller execution frames or snapshotting live bindings.

Experiment inheritance copies declarations and rebinds their expressions to each child's
storage. Same-name child output declarations override inherited declarations; missing ones
are copied in the existing merge order. Explicit display `parent:` inheritance copies display
facets and layers, not a separate set of local variables. Inherited layers retain their
declaration-site bindings. Neither form copies a parent's live local values or promises dependency sorting.
Inherited declaration labels are copied independently so compiling a child cannot replace
the parent's binding.

Display bodies contain layers and display configuration, not persistent declarations.
Typed container defaults, explicit nil, and ordinary temporary-variable behavior inside
graphics blocks and event handlers are retained. Attribute `update:`/`function:` semantics
are not introduced for presentation locals; dynamic state should remain in attributes or
be evaluated/assigned explicitly.

Related owner follow-ups stop experiment attribute initialization from incorrectly reading
simulation-parameter configuration while an experiment target is active, and prevent
experiment-owned species layers from silently using the current simulation's population.
Species-layer initialization, drawing and population menus use the actual output root;
`of:` still does not select an individual simulation.

**Migration:** keep controller/model state and parameter targets as attributes. Move only
presentation-only values into output locals, or explicitly qualify retained experiment
attributes from simulation outputs. Review initializer timing, lexical visibility and inherited
overrides. See the [detailed before/after migration guide](MIGRATION_GUIDE_2026.md#presentation-variables-in-experiments-migrate-to-output-locals).

#### Static layer visibility

In both OpenGL backends, `refresh: false` layers can be hidden and shown
without losing their static behavior. A layer is built when first visible and
rebuilt once when shown again after being hidden, using the current model state;
it remains cached between those transitions. See the
[display-layer guide](gama.documentation/files/input/markdown/DisplayCoordinates.md#static-layers-and-visibility)
for an example and details.

Both OpenGL backends now wait for a visible, initialized viewport before building
the first scene, rather than rebuilding all static layers immediately afterward.
Random expressions in static aspects are therefore not re-evaluated by that
startup redraw.

#### CPU-side display preparation

Evaluated drawing commands now move winding/arrow realization, image support
rectangles, grid borders and multiline layout to OpenGL scene playback. Native
spheres/cubes use scalar dimensions rather than rediscovering them from polygons.
Java2D applies ordinary object transforms during AWT shape conversion, while
preserving renderer-side arrow/depth and invalid-polygon handling.

Coordinates, mutable drawing attributes, grid pixels and ordinary field arrays
are captured before asynchronous rendering. Layer/trace state and file drawing
keys are captured once; transparency is evaluated on layer refresh, not again
during background rendering. Asset loaders requiring contextual services retain
a detached-scope compatibility path. Existing GAML aspect execution and graphics
plugin adapters are preserved.

Both OpenGL backends prepare eligible direct circle, sphere, square and cube
draws from immutable unit geometry instead of rebuilding JTS polygons per agent.
Default appearances use the same path. Dynamic facets, agent-relative centers,
transforms, appearance and picking remain live; general geometry expressions,
textured/arrows draws and Java2D retain their existing preparation path.

Drawing facets reuse invariant conversions without freezing pixel or viewport
dependencies, and their mutable evaluation data is local to each display.
Draw-only aspects avoid redundant child-result wrappers and empty draw scopes.
Shapes without arrows skip arrow-coordinate extraction, including unnecessary
convex-hull work for multipart geometries.

Dynamic scene/list storage and untraced grid-elevation arrays can be reused only
after retiring their previous scene. Static drawings, traces, arbitrary aspect
side effects and grid expression evaluation timing are preserved.

Display preparation diagnostics now report CPU-side layer/aspect/draw timings
and synchronization waits separately from rendering. See the
[display-layer guide](gama.documentation/files/input/markdown/DisplayCoordinates.md#cpu-side-display-preparation)
for eligibility, fallbacks and profiling instructions.

#### Java2D display responsiveness

Java2D render requests are coalesced and painted directly on the AWT event thread
instead of deferring another repaint behind mouse events. Synchronized updates
are acknowledged after the paint attempt, including when a display is hidden or
being closed. Mouse-driven display overlays refresh at most 30 times per second
and no longer force a layout on every movement.

Mouse and keyboard event actions that refresh outputs no longer make the AWT
event thread wait for rendering on itself, or consume the simulation's render
acknowledgment. This affects interactive event-layer models such as Moving Agents.

The macOS desktop product disables the Java2D Metal pipeline to reduce remaining mouse-movement stalls.
See the [startup workaround](README.md#macos-java2d-mouse-stutter) for its limitations and configuration.

#### Snapshots and image saving: faster, optionally asynchronous

Display snapshots and image saves are faster (#740). The saved expression is evaluated only once,
PNG files use fast lossless compression, OpenGL pixel readback and conversion are cheaper, and
Java2D snapshots wait for the paint notification instead of polling.

`save` accepts a new `async` facet (`false` by default) for image values, including snapshots.
With `async: true`, the image is still computed and copied synchronously, but encoding and
writing run on a background thread, so the simulation can continue during disk I/O:

```gaml
reflex capture {
	save snapshot("my_display") to: "frames/frame_" + string(cycle) + ".png" format: "png" async: true;
}
```

- Files are written in the order of the `save` statements, by a single background writer.
- At most two images wait in the queue. If disk writing cannot keep up, the next asynchronous
  `save` waits, which bounds memory use.
- When the statement returns, the file may not exist yet. Do not read it back immediately
  after an asynchronous save.
- Write errors are reported to the runtime when they occur, not by the `save` statement itself.
- `async: true` requires a `to:` destination and an image value. Fields, grids and other
  file formats raise an error rather than being saved synchronously.

GUI snapshots remain more expensive than headless ones. In headless mode every display is
rendered offscreen as an `image` display, at 500×500 pixels by default. In the GUI, a snapshot
waits for the on-screen display to render and then reads pixels back at native, possibly
high-DPI, resolution. To reduce this cost, request a smaller image with
`snapshot(agent, "my_display", {width, height})` and save it with `async: true`.

#### Additional display fixes

- Windows OpenGL embedding and native window coordinates respect DPI scaling; reloaded layouts
  remove stale containers that left black regions. Multi-display Java2D reload avoids serial UI
  waits. macOS fullscreen ignores a synthetic Escape event, and right-click menus restore
  display focus/mouse tracking
  ([`a99b4dbfc`](https://github.com/gama-platform/gama/commit/a99b4dbfc),
  [`f0a47db41`](https://github.com/gama-platform/gama/commit/f0a47db41),
  [`1e8b3b09c`](https://github.com/gama-platform/gama/commit/1e8b3b09c),
  [`21e5e259f`](https://github.com/gama-platform/gama/commit/21e5e259f),
  [`c80c916e0`](https://github.com/gama-platform/gama/commit/c80c916e0),
  [`3308f9668`](https://github.com/gama-platform/gama/commit/3308f9668)).
- Mesh palette/gradient handling and OpenGL4 font/extruded-text rendering are corrected.
  In-place image changes refresh GPU textures; the legacy renderer uses VBO-backed meshes.
  `camera_orientation` reads orientation instead of position
  ([`a7b62b5fe`](https://github.com/gama-platform/gama/commit/a7b62b5fe),
  [`d117a43e7`](https://github.com/gama-platform/gama/commit/d117a43e7),
  [`2703b1c75`](https://github.com/gama-platform/gama/commit/2703b1c75),
  [`27201142f`](https://github.com/gama-platform/gama/commit/27201142f),
  [`4c8e19b4f`](https://github.com/gama-platform/gama/commit/4c8e19b4f),
  [`2c95d9674`](https://github.com/gama-platform/gama/commit/2c95d9674)).
- Permanent outputs accept `synchronized:` / `autosave:`; synchronization in one experiment
  no longer slows independent experiments. `#camera_up`, `#camera_right` and `#mouse_click`
  extend display controls/events
  ([`d90eed6e9`](https://github.com/gama-platform/gama/commit/d90eed6e9), #736;
  [`751905f08`](https://github.com/gama-platform/gama/commit/751905f08), #1158;
  [`e22c98471`](https://github.com/gama-platform/gama/commit/e22c98471), #919;
  [`dc458c99d`](https://github.com/gama-platform/gama/commit/dc458c99d)).
- Display menus represent event layers with dedicated entries/icons; Linux models receive
  guidance about unreliable `mouse_exit` delivery
  ([`a5577c5ae`](https://github.com/gama-platform/gama/commit/a5577c5ae), #798;
  [`db3d4e528`](https://github.com/gama-platform/gama/commit/db3d4e528), #922).
- Java2D display creation is staggered on macOS to avoid hangs, and focus changes no longer
  cause flickering. OpenGL snapshots correct signed-byte colors; keystone reset, freezing and
  HiDPI sizing are fixed, rotated text stays stable on right-click, and antialiasing is retained
  after the first step
  ([`2e2497829`](https://github.com/gama-platform/gama/commit/2e2497829), #934;
  [`5bd001a5d`](https://github.com/gama-platform/gama/commit/5bd001a5d), #965;
  [`20fb49daa`](https://github.com/gama-platform/gama/commit/20fb49daa), #1051;
  [`ee7d6f0fe`](https://github.com/gama-platform/gama/commit/ee7d6f0fe), #948;
  [`272fd107d`](https://github.com/gama-platform/gama/commit/272fd107d),
  [`1fb0bfa83`](https://github.com/gama-platform/gama/commit/1fb0bfa83),
  [`e1ad312bf`](https://github.com/gama-platform/gama/commit/e1ad312bf)).

#### OpenGL4: native OpenGL 4.1 core-profile renderer

A new `opengl4` display renderer uses OpenGL 4.1 Core Profile instead of fixed-function APIs.
The existing `opengl` renderer remains available; the core-profile migration applies to the new renderer,
not the removal of all legacy displays.

OpenGL4 lighting now uses consistent eye-space coordinates for surfaces and lights, supports up to eight active diffuse lights with directional, point and spotlight behavior (including attenuation), and leaves only ambient illumination when all diffuse lights are disabled. Two-sided lighting, material-color clamping before texture modulation, and the absence of unsolicited specular highlights match the legacy renderer, including textured OBJ models such as Moving 3D Object.

OpenGL4 also matches legacy draw rotations and primitive orientations, correcting reversed sprites and axis arrows. Textured draws premultiply by texel alpha and blend like legacy OpenGL. This removes bright fringes around transparent icons and keeps translucent textured layers, such as the water in Moving 3D Object, at full brightness.

The shared default light intensity for OpenGL and OpenGL4 is increased from 160 to 180 for slightly brighter scenes. Explicit model intensities and saved lighting preferences are unchanged.

Both 3D renderers now limit the effective `z_near` to `z_far / 100000`. Very small values no longer exhaust depth-buffer precision, which made back faces show through and objects appear lit from behind (#973).

Empty 3D displays now show their axes; previously they were hidden as if the display only contained charts (#809).

*   `[47f55ee]` Migrate renderer to OpenGL 4 core profile
*   `[7614445]` Migrate OpenGL4 renderer away from fixed-function
*   `[973f54c]` Add FastTriangulation and use for polygons
*   `[117e307]` Remove bundled JOGL and shaders from OpenGL4
*   `[2322a83]` Introduce output title API and UI fixes (New overlay handling)

#### OpenGL4 geometry reuse

OpenGL4 keystone compositing now uses a core-profile vertex array and restores
rendering state. Large meshes in its fallback path are streamed in bounded
triangle batches, and general drawing buffers grow when necessary.

Large dynamic layers stream the untextured, unbordered sides of each extruded
polygon in ordered triangle batches instead of submitting each face separately.
Textured, outlined and wireframe geometry keeps its existing rendering path.

OpenGL4 now retains sphere, cylinder, cone-side and disk geometry in renderer-local GPU buffers.
Repeated instances and frames reuse positions, normals and texture coordinates instead of
recomputing and uploading every strip. Compatible strips are consolidated into one indexed
draw per primitive pass, preserving triangle winding and texture seams. Transforms, colors, transparency, lighting, wireframes
and picking remain per draw. A bounded cache distinguishes geometry parameters and textured
variants and releases GPU resources on eviction or display disposal.

Complex polygons now use the same bounded CPU tessellation cache as legacy OpenGL, including
holes and coordinate changes. Diagnostics report these cache hits and misses and retained-buffer
uploads.

Repeated constant-color geometry can also reuse GPU buffers: the first occurrence streams
normally, and a second encounter admits it to a bounded cache. Geometry changes invalidate reuse;
colors, transforms, lighting and picking remain dynamic. Mixed-color geometry keeps its streaming
path. Distinct fields no longer compete for one mesh drawer merely because their dimensions match.

Outline text reuses cached glyph tessellation and extrusion geometry. The basic shader computes
normal matrices on the CPU instead of inverting a matrix per vertex, and skips unchanged matrix
and rendering-flag uniform uploads. These optimizations require no model changes or new options.
Outside eligible map batches, static-layer traversal remains live, but geometry objects retain ordered transform,
color, texture-selection, outline-offset and GPU draw commands. Replays skip geometry
construction and cache-key snapshots without freezing the camera or layer transforms.
Picking, adaptive text, meshes and resource callbacks keep their existing paths.
The command cache is bounded and pins its GPU buffers until eviction or layer disposal.
Layers over 4,096 objects skip per-object command retention when they cannot use a
whole-layer batch, avoiding repeated GPU allocation and eviction for large mixed layers.
Refreshing layers over 256 objects also bypass per-object, coordinate-drawing and
extrusion-side retention, avoiding repeated allocation and disposal as drawings are replaced.
Static layers retain their existing caches.
OpenGL4 also automatically instances consecutive compatible sphere and point draws
in layers consisting entirely of opaque, filled, untextured, unbordered spheres and
points. Their order, transforms, colors and lighting remain live. Layers with other
shapes, unsupported properties, transparency or wireframes, and picking use the
ordinary path. Diagnostics report instanced calls and instances; actual gains depend
on the scene and hardware.

OpenGL4 uses the same native input handling and non-stealing view focus behavior as
legacy OpenGL, including conversion of mouse positions to logical window coordinates
on high-density displays.

Opaque, untextured layers containing only roads and/or flat unbordered polygon fills
can now retain ordered multi-draw batches. Compatible consecutive draws share packed
GPU buffers; separate ranges preserve road boundaries, polygon holes and traversal order.
Later frames skip object traversal and uploads while keeping camera and layer transforms live.
Color, lighting, line-width and depth-offset changes invalidate the batches. Transparent,
textured, bordered or extruded layers, picking and unsupported geometry keep their existing paths.
Diagnostics distinguish native multi-draw calls from the constituent drawings.

Eligible refreshed map layers over 256 objects and animated maps now stream ordered
multi-draw batches through bounded, reusable CPU and GPU buffers, without retaining
per-object geometry. Opaque, untextured lines and flat unbordered fills keep their
colors, lighting, transforms and drawing-depth offsets live; unsupported draws
flush pending batches before using the ordinary path. Static maps with native-width
lines retain the existing whole-layer cache.

Eligible map lines wider than one pixel use screen-space triangle ribbons instead
of driver-limited native wide lines. These maps also use the streaming path, even
when static. Widths are measured in framebuffer pixels, like native OpenGL widths;
segments have butt ends and no added miter or round joins. Picking, transparency,
textures, outlines and extrusions retain their ordinary paths and native-width
limits. Map shader attributes are separate from sphere/point instancing attributes.

Forced layer redraws requested after picking now wait for the next scene update
under its lock instead of invalidating an in-progress scene.

Mesh fills and outlines now keep separate VAO layouts: unchanged meshes no longer redefine
attribute pointers or toggle attribute arrays every frame. Streaming uniform-color geometry
uses one constant color attribute rather than uploading an RGBA array; mixed-color geometry
retains per-vertex colors. Geometry and resource objects keep the basic shader bound across
their component draws, restoring it at the object boundary so text and other shader paths
remain isolated. List traversals also share the basic shader across object boundaries;
foreign text and mesh paths suspend that binding before taking control.

Refreshed textured, grayscale and constant-color fields can reuse an unchanged mesh
after an exact comparison of sampled elevation values and appearance settings, avoiding
repeated smoothing, vertex reconstruction and GPU uploads. Mutable band and palette
color providers retain their rebuilding behavior. Environment-size changes rebuild the mesh.

Static image textures now remain owned by the display until disposal, matching the
lifetime of texture IDs held by scene objects. This avoids expiring still-referenced
allocations and losing the ability to release them. Successfully preloaded images
leave the preload queue instead of being looked up again on every frame.

#### OpenGL4 chart textures

Smooth chart and image textures now explicitly generate mipmap levels in GL4 core:
JOGL's automatic compatibility-profile path left these textures incomplete and black.
BufferedImage-backed scene objects resolve their texture through the backend cache on
each use, avoiding stale IDs after chart refreshes and texture-pool reuse. Recycled
image data regenerates its mipmaps when needed. Texture anisotropy uses the valid
minimum of one when display antialiasing is disabled.

Untextured GL4 draws keep a complete, renderer-owned white texture bound instead
of texture zero, avoiding macOS's "texture unloadable" sampler warning. Texture
binding explicitly selects unit zero so foreign shader paths cannot redirect
ordinary image bindings to another unit.

The GL4 line-width cache starts from the context's actual width, not zero, so
restoring an object's previous width cannot submit the invalid `glLineWidth(0)`.
Requested widths outside the native driver's supported range are constrained to
that range, with one explicit warning per display. In particular, macOS forward-compatible
GL4 contexts can reject widths greater than one with `GL_INVALID_VALUE`.
Rendering diagnostics include the first three frames' scene readiness, viewport,
framebuffer, swap mode and shader-link state. Basic-shader link failures now raise
an explicit error with the driver's program log instead of continuing with a blank display.

#### OpenGL4 text contours

Graph-font glyph contours are normalized before tessellation when their winding is
opposite to JOGL's convention. This fixes filled holes and broken curved edges in
affected glyphs, notably Poppins zeros, without changing fonts or replacing GPU text
rendering with raster labels. Already-correct contours and intentional zero dots are preserved.

Constant-color geometry also avoids populating a CPU color array. If a draw changes color or
alpha between vertices, the earlier vertex colors are materialized and subsequent colors remain
per vertex. Changes to color after the last vertex do not recolor the already submitted geometry.

Unbordered filled polygon extrusions and elevated lines now retain their side walls as one
indexed mesh instead of reconstructing and drawing every quad separately. Cache keys snapshot
coordinates, extrusion vectors and texture orientation; changing geometry or depth rebuilds the
mesh. Bordered and wireframe sides keep their original per-face traversal and outline ordering.
The side cache is bounded and releases GPU resources on eviction or display disposal.

OpenGL4 no longer drains the GPU with an unconditional `glFinish` at the end of every frame.
It flushes submitted commands instead; buffer presentation and explicit readbacks provide
their own synchronization. Picking keeps its dedicated readback synchronization. This allows
CPU submission and GPU execution to overlap.

Complex polygon fills now retain GPU drawings directly from their cached tessellation,
avoiding repeated per-vertex callback replay and attribute-buffer reconstruction. Coordinate
changes and face-normal changes produce new drawings. Textured polygons include orientation
and mapping bounds in their GPU cache keys, preserving mapping changes without rebuilding
attributes on every unchanged draw. Texture image contents and texture IDs remain per draw.
Retained geometry with one uniform face normal uses a constant normal attribute instead of
allocating and uploading a repeated normal buffer; curved surfaces retain per-vertex normals.

Simple shapes, lines, outlines and textured rectangles now look up retained drawings before
rebuilding vertex attributes. Cache keys include ordered coordinates, normals and texture-mapping
state, but not colors or object transforms. A second occurrence admits a drawing; changing
coordinates or texture orientation invalidates reuse. Compatible captured triangles, strips and
fans are consolidated into one indexed draw without connecting separate contours.

#### OpenGL4 fixed-pipeline lighting

OpenGL4 now computes two-sided ambient/diffuse lighting per vertex, matching the fixed OpenGL
pipeline, rather than running the light loop for every covered pixel. This is particularly useful
for large lit raster rectangles and polygon surfaces. Vertex material colors are clamped before
interpolation and texture modulation, as in legacy OpenGL. Positional attenuation, spotlights,
transparency and picking are preserved. Lighting on coarse geometry now follows legacy vertex
interpolation rather than the previous per-pixel shading. Unlit draws skip normal-matrix
calculation and upload; picking skips vertex lighting. No new model option is required.

#### OpenGL4 raster uploads

Raster uploads now use packed ARGB pixels directly, removing the per-pixel RGBA byte conversion.
Contiguous straight-ARGB images use their existing pixel array; other image layouts are normalized
without creating an intermediate image. Reusable upload buffers reduce allocation pressure.
Repeated lookups of the same `BufferedImage` share a texture within a simulation update, with
separate mipmapped variants. Scene updates invalidate this cache so in-place image changes remain
visible. These changes preserve alpha and image orientation and require no model changes.

Non-mipmapped raster textures also reuse GPU storage across simulation updates. A renderer-local
pool (at most 16 textures and 64 MiB) retains retired texture allocations; matching dimensions
receive fresh pixels through a subimage upload. Pixel contents are still refreshed each update,
and pooled storage is released on eviction or display disposal. Mipmapped images retain their
full rebuild path.

#### OBJ loading and rendering

OBJ bounds are computed from the parsed vertex bounds without first constructing a geometry for
every face. When an OBJ is used as GAML geometry, its buffer now contains one polygon per face,
rather than additional incomplete polygons for each intermediate vertex. Repeated buffer access
reuses the existing contents, and invalidating the file discards its parsed data and bounds.
Face parsing avoids per-corner tokenizer and character-array allocations and resolves negative
OBJ indices relative to the vertices, texture coordinates and normals already read.
OBJ parsing is serialized per file, so several displays rendering the same OBJ concurrently
no longer interleave its parsed lists and fail with index errors.

Both renderers batch consecutive compatible OBJ triangles and quads without changing material or
face order. The fixed pipeline compiles these batches into display lists. OpenGL4 retains triangle
and filled-quad batches in GPU buffers, reusing them across instances and frames while keeping
per-instance transforms, colors, lighting and picking. Wireframe quads and less common polygon or
mixed-attribute faces retain their individual drawing path. Cached GPU buffers are released on
cache expiration or display disposal and rebuilt when their source OBJ is invalidated.
Both backends avoid constructing temporary face coordinates when all normals are supplied, and
leave cached material textures under the texture cache's ownership.

#### Improved OpenGL rendering for maps and map-like environments

Both OpenGL backends now support **map ordering**, an opt-in rendering mode for flat maps and
map-like environments where overlapping backgrounds, grids, roads, polygons, agents and labels
should follow their existing drawing order. Instead of letting the depth buffer decide which
coplanar fragments are visible, it composites world layers and drawings in traversal order:
later fragments cover or blend over earlier ones. This provides a more reliable way to avoid
z-fighting between overlapping map elements than small depth offsets alone.

Enable it per display with `map_order: true`, or enable **Displays > OpenGL Rendering Properties >
In OpenGL, draw world layers in map order instead of using 3D depth**. The preference defaults to
`false`; an explicit display facet overrides it. Java2D is unaffected. Selected library map displays
have been updated to demonstrate the option.

Map ordering disables world depth testing and writes, including within individual drawings, so it
is not a general solution for 3D scenes: elevated surfaces, meshes and overlapping faces may no
longer appear in spatial depth order. Camera clipping still applies, screen layers keep their
existing ordering, and picking has backend-specific limitations. Legacy orthographic OpenGL also
now uses the same camera-depth direction as perspective and OpenGL4, so overlapping geometry in
existing depth-based displays can look different even when map ordering is disabled.

**Migration:** existing displays do not opt in automatically. Add the facet to suitable displays
or enable the preference; use `map_order: false` to retain depth-based rendering for a particular
display when the preference is enabled. See [map-order migration](MIGRATION_GUIDE_2026.md#map-order-for-opengl-displays)
and the [display-coordinate guide](gama.documentation/files/input/markdown/DisplayCoordinates.md#map-ordering-in-opengl).

#### OpenGL text and polygon drawing reuse

The legacy OpenGL renderer reuses prepared text outlines and polygon tessellations in bounded, renderer-local
caches instead of rebuilding them on every redraw. Text keys include the content, font, precision, depth,
wireframe and texture mode; polygon keys snapshot all contour coordinates, including holes and elevations,
so in-place geometry changes cannot reuse stale triangles. Colors, textures, placement, lighting and picking
remain applied at draw time. Each cache has a 16 MiB budget and is cleared when its renderer is disposed.

#### OpenGL FPS overlay shutdown

Both OpenGL renderers read FPS through the canvas using a single animator-reference
snapshot. This prevents an intermittent null-pointer exception when native-peer
shutdown detaches the animator while its final frame is drawing the FPS overlay.

Native OpenGL canvases start their animator independently of launch-overlay closure;
unrealized native windows are not drawn. Hiding still pauses rendering, and showing
resumes it. This avoids depending on post-launch view callbacks to start rendering
while a synchronized simulation waits for its first frame. Disposed native peers
cannot be restarted.

#### Optional OpenGL rendering diagnostics

The **Displays > OpenGL Rendering Properties > Log OpenGL rendering diagnostics** preference
enables periodic per-display reports in the application process log. It is off by default.
Both backends report CPU elapsed time for rendering, the visible scene and picking; OpenGL4 also
reports draw calls and buffer uploads for its generic and mesh paths, while legacy OpenGL reports
polygon-tessellation cache hits and misses. These are scoped counters, not totals for every GPU
operation. The measurements do not force GPU completion and are not GPU execution times.

#### Charts: dynamic bodies, distinct names and stable colors

- The body of a `chart` (its `data` and `datalist` statements and any `if`, `loop`… around them) is now re-evaluated at every step (#403). Series can appear conditionally, be created in loops, or get a new legend (which creates a new series). A series that is no longer declared is frozen (kept, but no longer updated) in charts showing a history of values (`series`, `xy`, `scatter`, `heatmap`) and removed in charts showing a snapshot (`pie`, `histogram`, `radar`, `box_whisker`). The other facets of `chart` keep their previous behaviour.
- Series are identified internally, independently of their legend (#785): several `data` statements can share the same legend, and pie labels, radar legends, histogram bar labels, tooltips and saved histories display legends rather than internal ids. Categories (`x_serie_labels`, `datalist` categories) are identified by position, so duplicated labels such as `["a", "b", "a"]` give three distinct categories.
- Histograms, radars and box-whisker charts keep their colors when the number of values of a `datalist` changes (#801).
- `y2_tick_values_visible` hides the tick values of the second y axis (#783).
- `include_init: false` skips the initial chart sample; the default remains `true`
  ([`60e8b408d`](https://github.com/gama-platform/gama/commit/60e8b408d), #1210).

Manual regression models are available in `Tests/Chart Tests/models` (`Chart Varying Datalist Regression`, `Chart Duplicate Names Regression`, `Chart Dynamic Body Regression`).

#### Chart legends, bounds and default properties

- Chart legends support left/right/top/bottom placement, hidden (`none`) and anchored
  on-chart placement with a transparent background
  ([`06b68b691`](https://github.com/gama-platform/gama/commit/06b68b691), #780;
  [`f1d2f96f9`](https://github.com/gama-platform/gama/commit/f1d2f96f9), #784).
- Charts accept fixed `x_min`, `x_max`, `y_min` and `y_max` bounds and `legend_orientation`.
  `data` / `datalist` legends accept booleans, `nil`, strings or lists with blank entries;
  blank labels can be hidden. Histogram on-chart text respects its text color
  ([`eb03fbcc5`](https://github.com/gama-platform/gama/commit/eb03fbcc5),
  [`9ae7d3a43`](https://github.com/gama-platform/gama/commit/9ae7d3a43),
  [`fcceeea66`](https://github.com/gama-platform/gama/commit/fcceeea66), #1186, #1187;
  [`959a256ae`](https://github.com/gama-platform/gama/commit/959a256ae)).
- **Default chart properties** preferences configure fonts, background/text/grid colors,
  gridlines, thickness, palette, pivot color, markers and series style.
  `pref_chart_continue_drawing` allows hidden charts or charts outside the Simulation perspective
  to continue updating; histogram series default to bars
  ([`1e106c6fa`](https://github.com/gama-platform/gama/commit/1e106c6fa),
  [`d9d8de11c`](https://github.com/gama-platform/gama/commit/d9d8de11c),
  [`2815627ea`](https://github.com/gama-platform/gama/commit/2815627ea),
  [`aa992d5d4`](https://github.com/gama-platform/gama/commit/aa992d5d4), #511;
  [`db08de9fa`](https://github.com/gama-platform/gama/commit/db08de9fa)).

#### Independent series-chart rendering

Series, XY and scatter charts capture independent rendering snapshots under the live-data lock, then paint without
holding that lock. Snapshots include interval datasets, axes, renderers, marker sizes, category labels and legends.
Unchanged charts reuse their last completed image; data, style, image-size and antialiasing changes trigger a redraw.
Every chart update and recorded/exported value is retained, and synchronized display acknowledgments are unchanged.
Other chart types retain their existing live-data rendering lock.

#### Chart image sizing in OpenGL

Chart raster sizes use the magnitude of signed OpenGL layer dimensions, preventing negative image sizes when
camera projection produces negative screen coordinates. Zero-size layers are not drawn, and chart-quality scaling
keeps nonempty OpenGL chart images at least one pixel wide. Other layer positioning and scaling are unchanged.

#### BIG CHANGE: keyboard combinations in display events

The `event` statement now supports **explicit keyboard combinations** in Java2D, OpenGL and OpenGL4 displays
(#332). Models can bind several modifiers to a character or named key without replacing existing single-key events:

```gaml
event "ctrl+shift+a" { write "CTRL+SHIFT+A"; }
event "alt+arrow_up" { write "ALT+UP"; }
event #ctrl + "a" { write "CTRL+A"; }
event #cmd + "+" + #enter { write "Command/Windows+Enter"; }
```

Use `ctrl`, `shift`, `alt`, and `cmd`, followed by one character or a named keyboard key. Modifier order and
capitalization do not matter, but the modifier set must match **exactly**: `ctrl+a` does not match `ctrl+shift+a`.
Letters are case-insensitive inside combinations; declare `shift` explicitly. `space` names the space key, and
`ctrl++` names CTRL with the plus character (add `shift` if producing `+` requires it on your keyboard).
Modifier-only combinations, duplicate modifiers, unknown names and mouse-event combinations are rejected.

Existing `#ctrl`, `#cmd`, `#shift`, and `#alt` constants retain their string values and standalone-event behavior.
They can also be concatenated directly: `#ctrl + "a"` and `#ctrl + #shift + "a"` are accepted compact spellings
of `ctrl+a` and `ctrl+shift+a`. The explicit separator form, e.g. `#ctrl + "+a"`, is recommended for readability.
`control` aliases `ctrl`; `command`, `meta`, `win`, and `windows` alias `cmd`. CMD means Command on macOS and
Windows/Meta elsewhere; it is **not** a portable alias for CTRL.

Combinations fire on key press, including native auto-repeat, when the display has keyboard focus. Their blocks
execute in the experiment context; use `ask simulation` to change simulation state. Legacy key events continue
to be dispatched as before. Built-in camera, display and workbench shortcuts remain active rather than being
overridden. Validation warns specifically about CTRL-based OpenGL events on Windows and known shortcut conflicts; OS or
workbench interception can still prevent delivery.

See the runnable [Keyboard Combinations](gama.library/models/Recipes/User%20Interaction/models/Keyboard%20Combinations.gaml)
recipe for string syntax, constant concatenation, multiple modifiers, named keys and unchanged single-key events.

#### Generic keyboard and mouse event listeners

An event layer can observe all keyboard presses or all supported mouse actions without declaring a
separate layer for each one. For example, inside a display:

```gaml
event #key_listener {
    write "Key: " + #key_pressed;
}
event #mouse_listener {
    write #mouse_action + " / " + #mouse_button + " at " + #user_location;
}
```

`#key_pressed` is the character produced by the key, preserving case; the space bar produces a literal
space (`" "`). Non-character keys use names such as `arrow_up`, `enter`, `backspace`, `ctrl` and `f1`.
If a modifier produces a control character, the unmodified key is used when known. Unknown keys are
reported as `key_<native code>`, whose code is platform-dependent. Modifiers can produce their own
press events, and holding a key can trigger repeats.

`#mouse_action` is one of `#mouse_down`, `#mouse_up`, `#mouse_click`, `#mouse_move`, `#mouse_drag`,
`#mouse_enter`, `#mouse_exit` or `#mouse_menu`. `#mouse_button` is `"left"`, `"middle"`, `"right"`,
`"button_N"` for an additional button, or `"none"` when the event has no associated button. A drag
reports the first pressed button known to the backend; click and menu detection depend on the platform.
Within a mouse callback, `#user_location` and `#user_location_in_display` are snapshots from the display
that received the event.

The three new input values are scoped to their corresponding callback, including nested `ask` blocks
and called actions; outside it they are empty strings, not the last event. Store a value in a model
variable to use it later. Blocks run in the experiment context, so use `ask simulation` to change global
simulation state. Listeners observe rather than consume input: existing event layers, display navigation,
context menus and shortcuts remain active. Mouse wheel and key-release events are not included, and the
operating system or workbench may intercept input before it reaches the display.

The [Keyboard Fireworks](gama.library/models/Recipes/User%20Interaction/models/Keyboard%20Fireworks.gaml)
and [Mouse Garden](gama.library/models/Recipes/User%20Interaction/models/Mouse%20Garden.gaml) recipes
demonstrate both listeners in Java2D and OpenGL displays.

#### BEHAVIOR CHANGE: macOS 3D camera controls use CMD, not CTRL

In **both OpenGL and OpenGL4**, the camera modifier is now **CMD only on macOS**. Previously, both CTRL and CMD
could activate modified camera navigation. macOS users who held CTRL while rotating or moving the camera must
now hold **CMD** instead. CTRL is no longer a camera modifier and can be used for model-defined keyboard events
without also activating that modifier's camera behavior.

**Windows and Linux keep CTRL as their camera modifier**; the Windows/Meta key does not substitute for it.
The platform-specific selection applies to mouse and keyboard camera interaction and to the camera helper's
display shortcuts. Unmodified navigation and SHIFT-based controls are unchanged. On macOS, **CTRL-click still
opens the contextual menu**; this change does not override operating-system or workbench shortcuts.

#### GAMA Server: full exchange logs

GUI and headless servers can now record all WebSocket exchanges in a separate JSON Lines file per run under `~/.gama/server-logs/` (#1269). Logging is **off by default**; enable it in **Network > Server mode** or with headless `-server-log`. Headless `-server-log-directory <path>` and `-no-server-log` override settings for one launch without changing saved preferences. Records include timestamps, connection IDs, directions and full payloads, including broadcasts, asynchronous events and binary/control frames. See [exchange logging configuration, format and privacy precautions](gama.headless/README.md#server-exchange-logs).

Clients can also send `{"type":"server_log","enabled":true}` (or `false`) to toggle logging immediately for the whole running GUI/headless server without changing saved settings. Re-enabling appends to the same run's log.

The GUI logging preference also takes effect immediately, without restarting GAMA or disconnecting clients. The log-directory preference still applies to the next server run.

#### GAMA Server: desktop server disabled by default

The `pref_enable_server` preference now defaults to `false`, so GAMA does not start the desktop WebSocket server automatically for a new preference store. Enable **Enable GAMA server mode** in **Preferences > Network > Server mode**, or set `-Dpref_enable_server=true` in `Gama.ini`. Existing saved preference values and explicit JVM system properties retain their precedence.

#### GAMA Server, headless and TCP follow-ups

- Headless startup initializes the workspace/core before model validation and creates built-in
  scopes only after platform contributions are ready; GUI server startup waits for UI services
  ([`de9f2d1b0`](https://github.com/gama-platform/gama/commit/de9f2d1b0),
  [`a83b34f3e`](https://github.com/gama-platform/gama/commit/a83b34f3e),
  [`968944933`](https://github.com/gama-platform/gama/commit/968944933),
  [`9c592c8a5`](https://github.com/gama-platform/gama/commit/9c592c8a5)).
- Command processing has bounded queuing (`pref_server_command_queue_size`, default 50)
  and explicit failure responses. Runtime errors during load/reload/step are returned to clients;
  asynchronous multi-step requests no longer stop after ten steps. Downloaded files are not
  given an extra trailing newline, and controllers are cleaned up on reload/disconnect
  ([`a09f7def6`](https://github.com/gama-platform/gama/commit/a09f7def6),
  [`030be4929`](https://github.com/gama-platform/gama/commit/030be4929),
  [`8031a80a7`](https://github.com/gama-platform/gama/commit/8031a80a7),
  [`54b088716`](https://github.com/gama-platform/gama/commit/54b088716),
  [`62eeda93d`](https://github.com/gama-platform/gama/commit/62eeda93d), #1065;
  [`0bb5b4993`](https://github.com/gama-platform/gama/commit/0bb5b4993),
  [`9f0a0f000`](https://github.com/gama-platform/gama/commit/9f0a0f000)).
- Model queries/actions are synchronized with simulation cycles, including when paused, rather
  than reading geometry concurrently with updates; controller operations remain asynchronous.
  `ask` validates action names, existing agent references and JSON arguments without creating
  missing agents. `stepBack` / `back` explicitly reject non-record experiments
  ([`dc775dc5d`](https://github.com/gama-platform/gama/commit/dc775dc5d), #1264;
  [`ef49d74f8`](https://github.com/gama-platform/gama/commit/ef49d74f8),
  [`53bea7ec4`](https://github.com/gama-platform/gama/commit/53bea7ec4), #990;
  [`b4fd69ad3`](https://github.com/gama-platform/gama/commit/b4fd69ad3), #828).
- GUI Stop reports lifecycle state `NONE` during disposal and cannot be followed by a stale
  `PAUSED` / `RUNNING` notification from a finishing cycle. State notifications track both the
  experiment and destination connection; the bundled browser connector waits for lifecycle
  replies before issuing another Load/Reload/Stop
  ([`e24024707`](https://github.com/gama-platform/gama/commit/e24024707), #988).
- Headless batch `play` runs all parameter sets/repetitions, interpreting `until` per simulation,
  emits one final `SimulationEnded`, and defers `sync: true` completion until that event.
  Requests to play/step a completed batch are rejected
  ([`d5658ee1d`](https://github.com/gama-platform/gama/commit/d5658ee1d), #297).
- Headless model paths resolve consistently with GUI paths, including cross-bundle library
  imports. `-validate` / `-test` initialize a common workspace; models that fail compilation
  are reported and counted as errors rather than disappearing from test results. Unknown
  legacy experiment names list available choices and return a nonzero exit status. Launchers
  improve quoting, Windows behavior and local command-line help
  ([`dfeeb5930`](https://github.com/gama-platform/gama/commit/dfeeb5930),
  [`1575edfcc`](https://github.com/gama-platform/gama/commit/1575edfcc),
  [`50355b0fe`](https://github.com/gama-platform/gama/commit/50355b0fe),
  [`ac4c93c5a`](https://github.com/gama-platform/gama/commit/ac4c93c5a), #752;
  [`a324c18ba`](https://github.com/gama-platform/gama/commit/a324c18ba)).
- Validation collects diagnostics without losing source locations when resources are disposed,
  maps snippet errors back to the submitted input, includes semantic errors for text validation,
  and encodes errors safely in JSON
  ([`942bb4aa3`](https://github.com/gama-platform/gama/commit/942bb4aa3),
  [`0fa605fb1`](https://github.com/gama-platform/gama/commit/0fa605fb1), #617, #733, #1084).
- TCP connections reuse UTF-8 streams, preserve read-ahead messages, escape rich-message
  payloads losslessly and serialize concurrent writes. EOF closes cleanly and send failures
  are reported; raw payloads remain unescaped
  ([`e943cfb62`](https://github.com/gama-platform/gama/commit/e943cfb62), #11).
- Network disconnect closes the agent's registered servers, removes registrations and closes
  TCP sockets before readers, avoiding shutdown hangs and leaked connections
  ([`8afa0128d`](https://github.com/gama-platform/gama/commit/8afa0128d), #483).

#### Spatial routing and movement performance

Routing on agent-typed edge networks now uses nearby spatial-index candidates instead of scanning every edge for each
endpoint. It still considers all equally nearest overlapping edges, including edges at different elevations, and
selects the least-weight reachable route without trying farther attachments. Geometry-edge networks and the
experimental location-based distance setting retain exhaustive lookup, as do graphs with mixed agent/geometry edges.
Routing candidate queries do not consume simulation random numbers.

Point-agent movement no longer allocates a temporary identity set for each spatial-index removal. Non-point
removal retains deferred, identity-deduplicated disposal of shared spatial keys.

#### Quadtree query and update speed

Single-nearest queries now visit the closest quadtree partitions first, skipping partitions that cannot improve the
result instead of repeatedly scanning expanding search areas. Equal-distance candidates remain randomly selected;
the number and order of random draws change, so seeded runs can differ from earlier versions. Searches across
populations compare each population's nearest candidate.

Distance queries use tighter search envelopes. Leaf splits reuse stored spatial keys rather than rebuilding
agent envelopes, ordinary insertions avoid the bounds-expansion lock, and point agents staying in the same leaf
update their existing entry without removal and reinsertion. Boundary crossings and geometry changes retain
the general update path. Exact shape distances, including point elevations, are preserved.

#### Octree query and update speed

The octree now applies the same optimizations in all three dimensions: direct single-nearest traversal,
tighter search boxes, same-leaf point updates, reuse of stored keys during splits, and a bounds-expansion
lock only when needed. Its distance rules remain unchanged: exact 3D distances between points and 3D
bounding-box approximations for other shapes. Equal-distance neighbours remain randomly selected, but
the number and order of random draws change, so seeded runs can differ from earlier versions.

#### Movement along paths and graphs

Graph movement now uses lazily indexed edges instead of walking the graph's edge set for each edge lookup or
intersection crossing. The index follows the existing edge order and is rebuilt after edges are added or removed,
not when weights change. Both 2D and 3D moving skills reuse the final edge directly.

Path following reuses the destination projection while checking for changes to the target or the final road's
coordinates. Movement reads line coordinates directly, traverses reversed roads without reversing arrays, computes
only the final heading, and avoids repeated road-length evaluations and temporary initialization lists on the hot
path. Dynamic weights, intersection choices and optional returned travel paths retain their existing behavior.

#### Routing cache and weight-update speed

Applying a map or list of graph weights now skips unchanged values and invalidates routing data once per batch,
instead of once per edge. Reapplying identical weights preserves cached routes. Actual changes also reset
precomputed routing algorithms, including Floyd-Warshall; list weights follow the existing edge iteration order
without repeated indexed lookups into the edge set.

Reading a graph's vertex set no longer clears its routing cache. The set remains live, and removing vertices through
it also removes their incident edges and invalidates routing data. Adding vertices or edges invalidates cached
routing data at the mutation rather than relying on a later read.

Cached routes and their intermediate-to-destination suffixes share one private route snapshot, replacing repeated
suffix copying with linear-time cache construction. Returned routes remain independently mutable; modifying an
exposed cached suffix copies only that suffix and cannot alter the others.

#### Endpoint route candidate construction

When several equally nearest edges can attach a source or destination to a network, routing now compares their
edge lists before constructing spatial paths. Only the winning candidate gets its oriented and trimmed path
geometries. Candidate weights, iteration-order tie breaking, and directed/undirected route selection remain
unchanged; single-candidate queries keep their existing construction path.

#### Shared container type descriptions

Repeated construction of the same container type reuses its type description and type expression through a
thread-safe, bounded cache owned by the relevant type manager. Cache keys use exact type identities, keeping
model-specific types distinct; disposing a manager clears its entries. Compiled list expressions pass their
existing list type directly to each fresh list, avoiding redundant metadata construction and lookup.
Container contents remain independent.

#### Two-argument numeric minimum and maximum

`min(a, b)` and `max(a, b)` compare numeric operands directly instead of constructing a temporary list.
All four int/float combinations are supported: two ints return an int, and mixed or float operands return a float.
The existing ordered-comparison behavior for NaN and signed zeros is preserved. Container operands and calls
with more than two arguments retain their existing behavior.

#### Fixed-argument operator invocation

Generated unary and binary operator helpers accept operands directly instead of allocating an argument array on each
evaluation. Operand order, lazy evaluation, conversions and error reporting are unchanged. Existing array-based
helpers remain supported through a compatibility fallback; operators with three or more operands retain their
existing invocation path. Regenerating operator registrations enables the specialized helpers.

#### Runtime arguments and population queries

Runtime action arguments bypass the compiler's value-based constant cache, avoiding full map/list hashing and
preserving each supplied object's identity even when two argument values compare equal. Constant descriptions
also avoid eagerly converting their values to an unused name string. Ordinary population
emptiness checks stop at the first living agent, and size queries count living agents without building a
temporary filtered list. Both queries retain the population's synchronization and exclude null/dead entries;
grid population behavior is unchanged.

### 3. Sharing and deploying models

Distributing models to other users and to other programs: the new `.gamp` (editable project
archive) and `.gamx` (playable model package) formats, the headless commands that produce, validate
and run them, and the new GAMA Server commands available to client applications and libraries.

- **Project archives and playable models:** [Model and package file formats](#model-and-package-file-formats) · [Headless commands for playable models and project archives](#headless-commands-for-playable-models-and-project-archives)
- **Server commands for clients:** [GAMA Server: configurable GUI loading](#gama-server-configurable-gui-loading) · [GAMA Server: read and configure platform preferences](#gama-server-read-and-configure-platform-preferences) · [GAMA Server: compact parameters and status subscriptions](#gama-server-compact-parameters-and-status-subscriptions) · [GAMA Server client libraries (gama.clients)](#gama-server-client-libraries-gamaclients)

#### Model and package file formats

Standalone experiment files, bundled library examples and generated test models now use the `.gaml`
extension. A `.gaml` file can contain a model with experiments or a standalone experiment; the legacy
`.experiment` extension remains supported for existing files and compatibility.

The archive extensions now distinguish two different workflows: `.gamp` is an editable project archive
that can be imported into a modeling workspace, while `.gamx` is a playable model package that opens in
GAMA's isolated player. Both are ZIP-based, but opening them has different effects. Playable packages
previously exported with the `.gamp` suffix must be renamed to `.gamx`; the playable archive contents
remain compatible. Legacy project ZIP import remains supported.

#### Headless commands for playable models and project archives

`gama-headless` can now generate, validate and run `.gamx` playable models and `.gamp` project archives, with the
same exporters and checks as the GUI. `-package <source> <output.gamx|output.gamp>` writes an archive from a project
directory or a model; `-validate-package <archive>` checks its layout, manifest, plugins, host compatibility and models;
`-run-package <archive>` runs one experiment until cycle N (`-until-cycle N`), or until its `until:` condition is true. Archive
plugins and `-plugin <file.jar>` plugins are installed for the duration of the command only. A run exits with a
nonzero status when the simulation raises a runtime error. The CI checks these commands on Linux and macOS.

#### GAMA Server: configurable GUI loading

WebSocket `load` now honors `parameters`, `until`, `console`, `status`, `dialog` and `runtime` in desktop GUI mode, not only headless mode (#394). Parameter values are applied before `global init`. Forwarding options use the same defaults in both modes and do not suppress local GUI output. The loaded experiment retains its requesting socket even when another client connects. A GUI experiment pauses and sends `SimulationEnded` when its stop condition is reached; reload it before running again. See the [GUI loading guide and runnable client example](gama.headless/README.md).

#### GAMA Server: read and configure platform preferences

GUI and headless clients can now read and change GAML-accessible platform preferences through
`get_preferences` and `set_preference`, **before loading an experiment** (#812).
For example, read the experiment-close confirmation setting, then disable it to allow subsequent GUI
`load` commands to replace the current experiment without a confirmation dialog:

```json
{"type":"get_preferences","names":["pref_experiment_ask_closing"]}
{"type":"set_preference","name":"pref_experiment_ask_closing","value":false}
```

Send these as separate WebSocket messages and wait for the successful `set_preference` response before
sending `load`. Omit `names` to discover all exposed preferences and their current values, types,
descriptions, allowed values/ranges and restart requirements.

Changes are runtime-only by default; add `"persist":true` to `set_preference` to save the value for
future launches. Values are validated and normal preference listeners are notified; rejected changes
and persistence failures return explicit errors. Preferences marked as requiring a restart are not
necessarily applied immediately.

**Preferences affect the whole GAMA instance**, including other clients and the desktop user, and are
not restored when a connection closes. Only trusted clients should access the server. See the
[preference command guide](gama.headless/README.md#platform-preferences-over-websocket) for JSON value
formats, response fields and error behavior.

#### GAMA Server: compact parameters and status subscriptions

- `load` / `reload` accept compact parameter objects (`{"var1":42}`) as well as legacy arrays,
  with shared validation and optional array-entry types. Seed parameters are applied before
  simulations are recreated on reload
  ([`73be977dc`](https://github.com/gama-platform/gama/commit/73be977dc),
  [`0fa605fb1`](https://github.com/gama-platform/gama/commit/0fa605fb1), #484).
- `simulation_status_subscribe`, `simulation_status_unsubscribe` and
  `simulation_status_request` let the owning client change status-bar forwarding or query
  lifecycle state without reloading. See the
  [status command reference](gama.headless/README.md#runtime-status-subscription-and-status-requests)
  ([`5066560a1`](https://github.com/gama-platform/gama/commit/5066560a1), #15).

#### GAMA Server client libraries (gama.clients)

The official client libraries for GAMA Server were substantially reworked. The repository now ships two maintained clients, **Python** (`gama_client` on PyPI, 2.0.x) and **TypeScript/JavaScript** (`@gama-platform/gama-client` on npm, 3.0.0). The old standalone `javascript` folder was removed and replaced by the TypeScript library. The whole repository is now MIT-licensed.

##### Python client (`gama_client` 2.0)

Version 2.0.0 overhauls the API. It contains **breaking changes**:

- `GamaBaseClient` was renamed **`GamaAsyncClient`**, and its command methods now use the `_async` suffix (`load_async`, `step_async`, …). These methods are fire-and-forget: answers go to a message handler and can be routed using `additional_data`.
- In `GamaSyncClient`, the plain method names (`load`, `play`, `step`, `expression`, …) now block until they return the server's answer. Each command also has an **`_awaitable`** variant, such as `load_awaitable` or `connect_awaitable`, which returns the exact answer to that command inside an `asyncio` program. The awaitable, blocking and fire-and-forget styles can be mixed on the same client.
- **Timeouts**: the client accepts a `default_timeout`, and every synchronous or awaitable command accepts a per-call `timeout`. This fixes long-standing hangs when the server does not answer.
- **Full command coverage** in all three modes: `load`, `play`, `pause`, `step`, `step_back`, `stop`, `reload`, `expression`, `ask`, `describe`, `validate`, `upload`, `download` and `exit`. `ask`, `upload`, `download` and `exit` were added in this release.
- **`seed` parameter for `load` and `reload`**: it sets the simulation's random seed before initialization. This maps to the new server-side `seed` option, so a run can be reproduced exactly.
- Blocking methods now run on an event loop owned by the client, so the `nest_asyncio` dependency was dropped. In a notebook or any other running event loop, use the awaitable API.
- Robustness and performance: a connection error no longer terminates the host program, the maximum WebSocket message size is configurable, and the server's "ready" confirmation is handled correctly. Command IDs come from a lightweight counter, which also fixed a memory leak. The connection handshake was moved out of the main listening loop.
- Requires Python ≥ 3.10. New sync and async examples (`ex01_connect_close` … `ex12_validate`, `complete_example`), a benchmarking script with baseline comparison, and a rewritten README are included.

##### TypeScript / JavaScript client (`@gama-platform/gama-client`)

- The library was rewritten in TypeScript and published on **npm** and GitHub Packages. Releases now use npm trusted publishing (OIDC). The package ships ESM and CJS builds with type declarations through a single entry point. `GamaClient` is available both as the default export and as a named export, and the public types (`GamaMessage`, `GamaParameter`, `GamaState`, `ExperimentState`) are now exported. Constants such as `MessageTypes` and `WS_READY_STATE` are exported as well.
- The client works in both **Node.js (≥ 20)** and the **browser**. When the library runs in a web page, it falls back to the browser's native `WebSocket`.
- Commands: `loadExperiment` (with `parameters`, `until`, and console/status/dialog/runtime redirection), `play`, `pause`, `step`, `stepBack`, `reload`, `stop`, `expression`, `ask`, `describe`, `validate`, `upload`, `download` and `exit`. `stepback` is deprecated in favour of `stepBack`.
- All commands now resolve with the typed server answer (`GamaMessage`) instead of a string. Each answer is matched to the command that produced it, and status broadcasts from other sessions are ignored. `ask()` validates agent references and arguments.
- New lifecycle helpers:
  - `closeConnection()` waits for the server's acknowledgement, with a timeout, and replaces `disconnectGama`.
  - `disposeSync()` performs a clean shutdown in `beforeunload`.
  - `wasInterrupted()` distinguishes a runtime-error interruption from a deliberate pause.
  - `stop()` resolves only once the experiment is gone.
  - Getters are available for the connection and experiment state.
- Logging now goes through LogTape, which the host application configures.
- **Breaking changes:** `disconnectGama` was removed. Command answers are now objects instead of strings.
- Browser demos with no bundler are included: a **control panel** (connect, load, play, pause, step, reload, evaluate) and a **Mapbox display** that renders agents with `to_geojson`. The SaReine web demo was also restored.

##### Testing and CI

- The Python and TypeScript clients share one suite of GAML test models (`tests/gaml`).
- GitHub Actions run both suites against the latest GAMA release and the latest pre-release, on several Python versions and on the current Node.js LTS versions. A failing combination no longer cancels the others.
- Workflows automate version bumps and publication to PyPI, npm and GitHub Releases.

##### Server commands available from the clients

Both clients expose the commands described above. Several commands added to GAMA Server in this release are **not yet wrapped** by either client:

- `get_preferences` and `set_preference`, which read and write platform preferences.
- `simulation_status_subscribe`, `simulation_status_unsubscribe` and `simulation_status_request`, which handle runtime status notifications.
- `server_log`, which turns exchange logging on or off.
- The `evaluate` and `back` synonyms.

These commands can still be sent as raw JSON messages through the clients' sockets. Playable model (`.gamx`) and project archive (`.gamp`) packaging is provided as headless command-line options (`-package`, `-validate-package`, `-run-package`). These are not WebSocket commands, so the clients do not expose them.

### 4. Developing GAMA

For developers of GAMA and of its extensions: the new GAMA developer tools, the clarified
architecture (`gama.api` separated from `gama.core`, stateless compiler) and its refactorings,
extension APIs, and the platform, build and packaging changes.

- **GAMA developer tools:** [GAMA Developer Tools (`gama.devtools`)](#gama-developer-tools-gamadevtools)
- **Architecture and refactoring:** [Architecture and core API (`gama.api`)](#architecture-and-core-api-gamaapi) · [Refactoring and code quality](#refactoring-and-code-quality)
- **Extension APIs:** [Extension API: temporary declarations separated from facet types](#extension-api-temporary-declarations-separated-from-facet-types) · [Extension API: asynchronous save delegates](#extension-api-asynchronous-save-delegates) · [Extension API: Java-backed GAML classes and generated regression tests](#extension-api-java-backed-gaml-classes-and-generated-regression-tests)
- **Platform, build and packaging:** [Platform, build and extension packaging](#platform-build-and-extension-packaging)

#### GAMA Developer Tools (`gama.devtools`)

GAMA now comes with a dedicated Eclipse plugin for **developers of GAMA and of GAMA plugins**: [gama-platform/gama.devtools](https://github.com/gama-platform/gama.devtools). It gathers in a single **GAMA** menu, toolbar and project context menu everything needed to set up a development workspace, install the sources, clean and rebuild the GAMA plugins (annotation processor, GAML additions, Xtext grammar), launch/debug GAMA from the workspace, build a local release, create new GAMA artefacts and inspect the code base. It replaces the old, mostly unmaintained `gama.ui.devtools` that lived in the main repository (removed from `gama` in October 2026); the tools now have their own repository, build and update site.

##### Installation and launch

- Requirements: **Eclipse IDE for Java and DSL Developers 2026-03 or newer** (JDT, PDE, Xtext/MWE2) running on Java 21+, plus a separate **full JDK 25** to compile and run GAMA (the setup wizard helps find and register it).
- In Eclipse, *Help > Install New Software...*, add the update site **`https://gama-platform.org/gama.devtools/`**, select **GAMA Developer Tools** and restart. Installing the tools does not install GAMA itself into Eclipse.
- After restart, a workspace without a `gama.core` project offers to open **GAMA > Get started...**, a five-page guided setup: Eclipse checks → JDK 25 detection/registration → source installation → clean build → Run/Debug GAMA. A manual setup path is also documented in the [plugin README](https://github.com/gama-platform/gama.devtools/blob/main/gama.ui.devtools/README.md#getting-started).
- The tools check for updates in the background (hourly) and update themselves through **GAMA > Update GAMA tools...**.
- The update site is a composite p2 repository published by GitHub Actions on every change to `main`, keeping previous builds available. The tools can also be built locally with `mvn -DskipTests package` (JDK 21+, Maven 3.9).

##### Available commands

All commands are in the **GAMA** menu (also reachable from the GAMA logo on the **GAMA toolbar**). Some are also in the **GAMA** context menu of bundle projects.

**Setup and maintenance of the tools**
- **Get started...** – guided setup of a complete GAMA development environment (Eclipse/JDK checks, sources, build, first launch). Can be disabled at startup in *Preferences > GAMA Developer Tools*.
- **Install GAMA sources > Install core plugins...** – clones/updates (via JGit, no Git executable needed) and imports projects from `gama`, `gama.shared.models`, `gama.graphical.modeling` and `gama.copilot`, grouped by GAMA category. Options: per-repository branch and directory, stashing local changes on request, working sets from `Project sets for Gama.psf`, opt-in import of GAMA Eclipse preferences, cleanup and formatter profiles, closing `gama.parent`, and adding optional source features to GAMA launches and Maven/release builds.
- **Install GAMA sources > Install community plugins...** – the same for the repositories of [gama-platform-plugins](https://github.com/gama-platform-plugins) (branch `GAMA_2026-06`), discovered through the GitHub API and grouped by the `gama.category` declared in their POMs.
- **Update GAMA tools...** – checks the update site and installs the latest devtools directly. The menu shows "(update available)" when a newer version is published.
- **Report an issue...** – opens a new issue on [gama.devtools](https://github.com/gama-platform/gama.devtools/issues/new) in the browser.

**Building**
- **Clean GAMA plugins** (`Cmd+B` / `Ctrl+B`, toolbar) – refreshes the workspace, removes stale project registrations, rebuilds `gama.annotations`/`gama.processor` and exports `gama.processor.jar`, repairs plugin configuration, resets annotation processing and its cached classloaders, deletes `gaml/` folders, then runs a clean and full build and checks that every `GamlAdditions.java` was regenerated. It also sets the workspace compiler level to Java 25. The context-menu **Clean...** limits this to the selected plugins.
- **Automatic rebuild on Git branch switch** – once a branch switch in a repository containing GAMA plugins has settled, the same clean/rebuild runs automatically.
- **Repair GAMA plugins > Rebuild annotation processor** – re-exports `gama.processor.jar` from its `.jardesc`, then cleans the plugins. Use it after changing `gama.processor` or `gama.annotations`.
- **Repair GAMA plugins > Regenerate Xtext grammar** – runs the MWE2 workflow `GenerateGaml` from `gaml.grammar`, then cleans the plugins.
- **Repair GAMA plugins > Update file headers** – sets the GAMA version and copyright year in Java headers and adds missing headers, with a per-file refactoring preview and undo. Also available from the context menu as **Update headers...**.
- **Repair GAMA plugins > Generate missing tests** – writes `models/Tests/Untested Operators.gaml` with one `test` stub per untested operator.

**Running and releasing**
- **Run GAMA > Run UI / Run headless / Run tests / Debug UI / Debug headless** (Run/Debug UI also on the toolbar) – refreshes the workspace, completes the launch configuration from `gama.product`/`gama.headless.product` (missing features, plug-ins and startup bundles), and launches workspace GAMA. Headless asks for arguments (XML or `-batch`), tests use `-test`. Missing or closed core projects open the source installer instead.
- **Run GAMA > Configure...** – associates each flavour (GUI, headless, tests) with an existing Eclipse Application configuration, or generates defaults (`GAMA - GUI`, `GAMA - Headless`, `GAMA - Tests`) with a registered JDK 25. Also lets you edit a configuration or restore the defaults.
- **Run GAMA > Configure GAMA build...** – chooses which features and plugins go into local releases. Choices are saved per checkout and never modify the sources.
- **Run GAMA > Build local release...** (toolbar "Export GAMA release...") – builds a runnable, self-contained GAMA (`Gama.app`, or a `Gama` folder with launcher and bundled JDK 25) for the current OS/architecture from the workspace checkout. It uses a privately downloaded and checksum-verified Maven 3.9.16 (no system Maven needed), parallel builds (`-T 2C`) and reuses already verified processor builds. It offers to run the result.
- **Add to GAMA build...** (bundle context menu) – registers a plugin in the Maven reactor (`gama.parent`) and makes its feature reachable from `gama.product`, reusing a feature or creating a dedicated one.
- **Crash recovery** – when a GAMA UI launch exits with status 42 (fatal GAML initialization failure), a dialog offers **Clean and Relaunch**, **Clean Only** or **Ignore**.

**Inspecting and reporting**
- **Check workspace...** – diagnoses GAMA plugin configuration, required workspace projects, target-platform bundles of the GUI product and JDK 25 availability, and offers repairs or the source installer.
- **Search GAML keyword...** (`Ctrl/Cmd+Shift+Alt+K`, toolbar) – an *Open Type*-like dialog that finds the Java implementation of any GAML operator, statement, skill, species, type, action, argument, attribute or facet. Supports kind prefixes (`op:`, `facet:`, `action:`, `attr:`, `statement:`) and wildcards, and pre-fills with the word under the caret, including in the GAML editor.
- **Report > Missing annotations** (context menu **Report missing annotations...**) – Markdown report of undocumented GAML elements and untested operators, per plugin, built from the annotation processor's warnings.
- **Report > Native library dependencies** – runs `jdeps` on compiled bundles and libraries and produces an interactive HTML dependency explorer and a DOT export. It is also usable standalone through `generate_dependencies_graph.sh`. See [Static GAMA Dependencies Analysis](https://github.com/gama-platform/gama.devtools/blob/main/gama.ui.devtools/README_DEPENDENCIES_GRAPH.md).
- **Quick fixes on processor warnings** (`Ctrl/Cmd+1`, also in bulk from the Problems view) – add a `@doc` or `@test` skeleton, mark an operator `@no_test(...)`, or generate a GAML test stub. Plugin configuration problems found by the checker (APT, factory path, `gaml` source folder, `gaml.extension`, singleton, UTF-8, unique short names...) also get quick fixes.

**Creating artefacts** (*Create GAMA artefacts* submenu, also under *File > New > Other... > GAMA Development*)
- **New GAMA Plugin...** – creates a complete GAML plugin (sources, `gaml/`, `models/`, manifest, POM with a `gama.category`, APT settings). It validates the plugin id, can register the plugin in `gama.parent` and in a feature or new dedicated feature, then cleans and builds it.
- **New GAMA Skill... / New GAMA Operator... / New GAMA Action... / New GAMA Type...** – generate annotated Java skeletons for these GAML extensions.
- **New GAML Test Model...** – creates a GAML test model in a plugin.
- **Import GAMA project into plugin...** – copies a GAMA model project into a plugin's `models/<Category>/` library (Data, Demos, Library, Recipes, Tutorials or Tests), fixes its natures and exports `models/`.

##### Notable evolutions since July 2025

- **Dependency analysis** (July–October 2026, [#1099](https://github.com/gama-platform/gama/issues/1099), [PR #1142](https://github.com/gama-platform/gama/pull/1142)): generation of the static dependency graph of GAMA bundles with an HTML web UI. Later rewritten to use only the JDK (no more JGraphT).
- **Revival of the developer tools** (October 2026, [PR #1270](https://github.com/gama-platform/gama/pull/1270)): new GAMA menu, Clean & Rebuild, configuration checker with quick fixes, updated wizards, GAML keyword search, processor-warning quick fixes and Java header maintenance, all published on an update site.
- **Standalone repository** (end of October 2026): the tools moved from `gama` to [gama.devtools](https://github.com/gama-platform/gama.devtools), with their own CI publication, self-update and hourly update check.
- **Workspace launches, local release builds** (no Maven install required) and a **GAMA toolbar**, followed by configurable release contents (November 2026).
- **Guided onboarding**: the *Get started* wizard, workspace diagnostics, core/community source installers organised by GAMA categories, and recovery after fatal UI startup failures.
- **Workflow automation**: automatic clean after processor or grammar regeneration, automatic rebuild after Git branch switches, progress dialogs, and refreshes of open Explorers.
- **Reusable GitHub Action** [`p2-categories`](https://github.com/gama-platform/gama.devtools/tree/main/.github/actions/p2-categories) (November 2026): regenerates a plugin update site's `category.xml` from the `gama.category` properties declared in plugin POMs, using the shared `categories.xml`. Plugin repositories, including community plugins, can use it.

#### Architecture and core API (`gama.api`)

The new `gama.api` module has been created to decouple public interfaces from internal implementations (`gama.core`). Core types have been extensively relocated, and heavily used classes have been modernized to use Java Records.

*   `[79be879]` Create IDelegatingShape.java (Agent shape delegation)
*   `[8e84ad3]` Use IDelegatingShape and remove IShape defaults
*   `[cfa0b36]` Introduce AbstractPopulation and refactor populations
*   `[33fff95]` Use record for GamlCompilationError and update callers
*   `[0c26602]` Refactor envelope classes to utils.geometry
*   `[8940dcc]` Restructure packages: move types & interfaces to gama.api
*   `[13b2eea]` Add built-in platform and global species lookup
*   `[f1c2e72]` Refactor color API, pooling and movement helpers (Cast to Color unification)
*   `[7085b17]` Move GamaGeometryFactory; optimize GamaListFactory (Factory Generalization)

#### Refactoring and code quality

The codebase makes extensive use of new Java features like pattern matching, resulting in cleaner and more maintainable backend code.

*   `[775b94c]` Refactor TypePair equals and descriptions (Pattern matching)
*   `[6ff70b5]` Refactor annotations, replace literals with constants
*   `[8440794]` Move IKeyword to annotations package
*   `[c138702]` Add package-info and improve API Javadocs
*   `[ac8b13c]` Migrate transition transformers from Python to Java

#### Extension API: temporary declarations separated from facet types

`IType.NEW_TEMP_ID` has been removed. Temporary-declaring facets use
`declares_new_temp = true` with ordinary `type`, `of`, and `index` metadata.
`ITypeProvider.STATEMENT_TYPE` describes the statement's computed type;
`INFERRED_TYPE` uses its declaration/value/iteration inference, preserving nested container
types. Built-in declarations and generated documentation have been migrated. Extensions
must migrate affected annotations and regenerate their additions; see the
[migration guide](MIGRATION_GUIDE_2026.md#temporary-declaring-facets-replacing-new_temp_id).

#### Extension API: asynchronous save delegates

`ISaveDelegate` implementations can opt in to the `async` facet by overriding
`supportsAsync()` to return `true` and reading `SaveOptions.async()` in `save(...)`. The default
is `false`, and `save` rejects `async: true` for delegates that do not support it.
`SaveOptions` gains a trailing `async` component and `withAsync(boolean)`; its former
canonical constructor remains available and keeps saves synchronous.

This is also an opportunity to speed up other file formats, such as CSV, shapefiles,
GeoPackage or JSON. Delegates that write large files can move encoding and I/O off the
simulation thread. An asynchronous implementation must:

- read the agents, expressions and mutable values it needs synchronously, while `save` runs, and
  copy them into data owned by the background task;
- never use the `IScope` from the background task;
- preserve the order of writes to the same file, for example with one writer per destination;
- bound queued work, for example with a small queue that blocks when full;
- report failures explicitly, because the `save` statement has already returned.

`gama.extension.image.AsyncImageWriter` is the reference implementation. It copies pixels
before queuing them and uses a single-thread executor with a two-item queue. A full queue blocks
the caller, and errors are reported through `GAMA.reportError` with the current runtime scope.

#### Extension API: Java-backed GAML classes and generated regression tests

- Java plugins can register non-agent GAML classes with `@gaml_class`, including constructors,
  inherited attributes and actions. Object and agent construction share the `getConstructor()`
  API; see the [Java-backed class migration guide](MIGRATION_GUIDE_2026.md#java-backed-built-in-classes)
  ([`86dd66fd1`](https://github.com/gama-platform/gama/commit/86dd66fd1),
  [`31db829c3`](https://github.com/gama-platform/gama/commit/31db829c3)).
- Annotation-driven regression coverage is expanded across operators, constants, units and types;
  the processor generates separate tests per annotation and operator/cast fuzz tests. Robustness
  suites cover Unicode, nil, invalid input and numerical edge cases
  ([`633fe117e`](https://github.com/gama-platform/gama/commit/633fe117e),
  [`13d049068`](https://github.com/gama-platform/gama/commit/13d049068),
  [`7809b87ad`](https://github.com/gama-platform/gama/commit/7809b87ad),
  [`7640eb14f`](https://github.com/gama-platform/gama/commit/7640eb14f)).

#### Platform, build and extension packaging

- The platform moves to **Eclipse 2026-03**, **Java 25** and **GeoTools 33.4**. Packaged startup
  raises the initial heap from 2 GB to 4 GB (`-Xms`, not a new maximum) and uses an
  XWayland-compatible SWT path under Wayland. Sound playback dependencies are modernized
  and the desktop product includes the sound/statistics features
  ([`7090a85f2`](https://github.com/gama-platform/gama/commit/7090a85f2),
  [`41f3a5272`](https://github.com/gama-platform/gama/commit/41f3a5272),
  [`810bfbbe0`](https://github.com/gama-platform/gama/commit/810bfbbe0),
  [`e6ff14a16`](https://github.com/gama-platform/gama/commit/e6ff14a16),
  [`00f6e41c2`](https://github.com/gama-platform/gama/commit/00f6e41c2),
  [`b7f5f8812`](https://github.com/gama-platform/gama/commit/b7f5f8812),
  [`c8db99638`](https://github.com/gama-platform/gama/commit/c8db99638), #860).
- Builds and headless launchers target JDK 25; macOS archives distinguish `MacOS_Intel`
  and `MacOS_Silicon`. Log4j is updated to 2.26.0
  ([`cd50a1fc0`](https://github.com/gama-platform/gama/commit/cd50a1fc0),
  [`2c9cad4b5`](https://github.com/gama-platform/gama/commit/2c9cad4b5),
  [`148a58caf`](https://github.com/gama-platform/gama/commit/148a58caf), #734;
  [`907a83822`](https://github.com/gama-platform/gama/commit/907a83822)).
- OSM, KML, GML and dataframe support are separate extension bundles; distribution operators
  and MOEA dependencies move into the statistics extension without changing their GAML names.
  Plugins can contribute display types with `@display(viewId=..., is3D=...)` and server
  commands through `gama.server_command`
  ([`246ec0f38`](https://github.com/gama-platform/gama/commit/246ec0f38),
  [`8a3846a55`](https://github.com/gama-platform/gama/commit/8a3846a55),
  [`927826832`](https://github.com/gama-platform/gama/commit/927826832),
  [`66b06221f`](https://github.com/gama-platform/gama/commit/66b06221f),
  [`d8ef830cb`](https://github.com/gama-platform/gama/commit/d8ef830cb),
  [`817299b1d`](https://github.com/gama-platform/gama/commit/817299b1d), #1124;
  [`aac6be26c`](https://github.com/gama-platform/gama/commit/aac6be26c)).
- Distribution/build tooling repairs generated p2 deployment, validates embedded-JDK download
  checksums and signs nested macOS libraries with spaced paths. Release publication runs code
  tests; documentation generation publishes an updated Prism GAML syntax definition
  ([`2884a856d`](https://github.com/gama-platform/gama/commit/2884a856d),
  [`fe4ca2a05`](https://github.com/gama-platform/gama/commit/fe4ca2a05),
  [`477d6237b`](https://github.com/gama-platform/gama/commit/477d6237b),
  [`ea731b652`](https://github.com/gama-platform/gama/commit/ea731b652),
  [`05eacbb76`](https://github.com/gama-platform/gama/commit/05eacbb76)).

### 5. Other changes

New and improved file formats and file caching, dataframes and databases, matrix computation
with the Java Vector API, statistics and random draws, persistent terminal commands, physics
(joints and Box2D), pedestrian and driving skills, geometry, serialisation and BDI.

- **File formats, file caching and data:** [GeoPackage, raster and file interoperability](#geopackage-raster-and-file-interoperability) · [XML files: structured contents and saving](#xml-files-structured-contents-and-saving) · [RDS files: import and export R serialized data](#rds-files-import-and-export-r-serialized-data) · [Dataframes and database migration](#dataframes-and-database-migration)
- **Computation and statistics:** [Matrix computation and grid integration](#matrix-computation-and-grid-integration) · [Random draws and statistical correctness](#random-draws-and-statistical-correctness)
- **Terminal commands:** [System commands: persistent contexts and process cleanup](#system-commands-persistent-contexts-and-process-cleanup)
- **Physics, pedestrians and driving:** [Physics: joints and Box2D corrections](#physics-joints-and-box2d-corrections) · [Pedestrian, driving and Bullet corrections](#pedestrian-driving-and-bullet-corrections)
- **Geometry and spatial operations:** [Geometry and spatial operations](#geometry-and-spatial-operations)
- **Serialisation:** [Recording: shared Fory dependency](#recording-shared-fory-dependency)
- **BDI:** [BDI: shared internal mental-state operations](#bdi-shared-internal-mental-state-operations) · [BDI: configurable thought-history capacity](#bdi-configurable-thought-history-capacity) · [BDI: shared statement execution](#bdi-shared-statement-execution) · [BDI: numeric contracts and emotion calculations](#bdi-numeric-contracts-and-emotion-calculations)

#### GeoPackage, raster and file interoperability

- OSM PBF import works with modern protobuf dependencies through a reader handling dense nodes,
  ways, relations, bounds and raw/zlib blobs. Relation members retain relation tags and
  `osm_relation_id`; multipolygon-hole subtraction preserves valid working geometry
  ([`d19006a45`](https://github.com/gama-platform/gama/commit/d19006a45),
  [`a9b254fea`](https://github.com/gama-platform/gama/commit/a9b254fea), #726;
  [`a9343e296`](https://github.com/gama-platform/gama/commit/a9343e296)).
- The new GeoPackage extension reads `.gpkg` feature layers with `geopackage_file(path)` or
  `geopackage_file(path, layer_name)`. Without a layer name it selects the first feature layer;
  unknown layers and files without feature layers produce explicit errors. Navigator metadata
  includes layer information, feature count, CRS and extent; bundled examples demonstrate layer
  selection. This is feature-layer import, not a claim of raster-tile or export support
  ([`3b2a5e3df`](https://github.com/gama-platform/gama/commit/3b2a5e3df),
  [`f2d2224d5`](https://github.com/gama-platform/gama/commit/f2d2224d5),
  [`436776c91`](https://github.com/gama-platform/gama/commit/436776c91)).
- Large GeoTIFFs are read in tiles with progress feedback. CRS-key handling and multiband
  mesh colors are corrected. Large raster meshes use reduced display detail and reusable buffers;
  oversized agent grids are rejected before allocation with memory guidance, rather than
  attempting an allocation that cannot fit. This does not remove the memory cost of creating
  millions of agents; use fields when individual cell agents are unnecessary
  ([`918e5733d`](https://github.com/gama-platform/gama/commit/918e5733d), #647, #652, #655).
- ASC import accepts whitespace-separated headers, optional NODATA values and independent
  `DX` / `DY` spacing. Cell-center coordinates and bounds are corrected, cached contents are
  cleared on invalidation, and parse failures identify the file and line
  ([`654da9ccc`](https://github.com/gama-platform/gama/commit/654da9ccc), #737).
- Shapefile features remain available after their datastore is disposed, including when
  memory mapping is disabled. Read failures include contextual GAML errors
  ([`682a652bc`](https://github.com/gama-platform/gama/commit/682a652bc), #309;
  [`8c7c5e3b0`](https://github.com/gama-platform/gama/commit/8c7c5e3b0)).
- CSV import strips byte-order marks, handles empty input and preserves the first data row
  when headers are explicitly disabled, without overriding an explicit content type.
  JSON output preserves full floating-point precision; malformed JSON reports a GAML error,
  and serializer thread-local state is cleared after use
  ([`8c7c5e3b0`](https://github.com/gama-platform/gama/commit/8c7c5e3b0),
  [`671cde3b7`](https://github.com/gama-platform/gama/commit/671cde3b7),
  [`01efaccf4`](https://github.com/gama-platform/gama/commit/01efaccf4)).
- File paths are cached per experiment, CSV-to-matrix parsing is corrected, and saving an
  empty string no longer fails with an out-of-bounds exception
  ([`62d137fe6`](https://github.com/gama-platform/gama/commit/62d137fe6), #294;
  [`438827049`](https://github.com/gama-platform/gama/commit/438827049), #1127;
  [`8bf1a8274`](https://github.com/gama-platform/gama/commit/8bf1a8274)).
- File-extension dispatch is case-insensitive at runtime. `save` checks actual disk existence
  when buffering is disabled, avoiding incorrect header/overwrite decisions based on pending
  buffered writes
  ([`16f9ce1f7`](https://github.com/gama-platform/gama/commit/16f9ce1f7),
  [`c2300fd44`](https://github.com/gama-platform/gama/commit/c2300fd44)).
- Saving `nil` writes nothing. GIS saving validates attribute names and reports nonexistent
  agent attributes; MySQL geometry construction uses `ST_GeomFromText`
  ([`e352396bb`](https://github.com/gama-platform/gama/commit/e352396bb),
  [`af1df79a7`](https://github.com/gama-platform/gama/commit/af1df79a7), #795;
  [`dac5f0133`](https://github.com/gama-platform/gama/commit/dac5f0133), #964).
- JSON object members convert to actual GAML values, and JSON writing correctly separates
  members, including empty typed objects. Binary persistence supports spatial graph structure/
  weights, spatial paths and mailbox messages through the current serializer adapters;
  this does **not** restore compatibility with FST-produced files
  ([`04d7af843`](https://github.com/gama-platform/gama/commit/04d7af843),
  [`61fd0a059`](https://github.com/gama-platform/gama/commit/61fd0a059),
  [`b594a8451`](https://github.com/gama-platform/gama/commit/b594a8451),
  [`0b3cb85cb`](https://github.com/gama-platform/gama/commit/0b3cb85cb)).
- The database skill is now named **`sql`**, replacing `SQLSKILL`; MySQL, SQLite and PostGIS
  examples use the new spelling
  ([`185d46cb1`](https://github.com/gama-platform/gama/commit/185d46cb1)).

- Raster export to TIF/ASC uses GeoTools; GeoTIFF saving accepts `no_data:`.
  Projection factories initialize asynchronously to reduce model-loading delays
  ([`de9b9206d`](https://github.com/gama-platform/gama/commit/de9b9206d),
  [`7946bd916`](https://github.com/gama-platform/gama/commit/7946bd916), #7;
  [`8ac6f4f0d`](https://github.com/gama-platform/gama/commit/8ac6f4f0d), #1074).

#### XML files: structured contents and saving

`xml_file` now reads XML documents into nested GAML maps instead of a map of raw file lines
(#472). The file's `contents` represents the root element as a `map<string, unknown>` with
four keys: `name` (tag name), `attributes` (`map<string, string>`), `text` (trimmed direct text)
and `children` (an ordered list of element maps). Repeated child tags remain separate elements.
The file's `attributes` returns the names of the root element's XML attributes.

The new `xml_file(path, root_map)` constructor supports creating or modifying XML documents;
`save xml_file(path, root_map);` writes the structure as UTF-8 XML. This representation is
intended for structured data, not lossless XML editing: comments and processing instructions
are omitted, surrounding text whitespace is trimmed, and mixed text/element ordering is not
preserved. Reading rejects DTDs and external entities.

The library includes [XML File Import](gama.library/models/Data/Data%20Importation/models/XML%20File%20Import.gaml),
with an XML ecosystem configuration, and
[Save to XML](gama.library/models/Data/Data%20Exportation/models/Save%20to%20XML.gaml),
which saves agent data and reads it back.

#### RDS files: import and export R serialized data

The new `gama.extension.rds` plugin adds **RDS serialization version 2** support (#191),
independently of the statistics extension and without requiring an installed R runtime.
It is included in the standard desktop and headless distributions.

Use `rds_file("data.rds")` to load an object and `save value to: "output.rds" format: "rds";`
to export one. Supported values include typed vectors/lists, string-keyed maps, two-dimensional
matrices and dataframes. Missing values are preserved as `nil`; factors are imported as labels.
Agent collections can be exported as dataframe snapshots, including selected or computed attributes.

Input may be gzip-compressed or uncompressed; exports use gzip-compressed XDR version 2 and can
be read with R's `readRDS`. **Version 3, R workspace files (`.RData`), other compression formats,
and unsupported object types are rejected explicitly.** Export compatible input from R with
`saveRDS(object, "data.rds", version = 2)`. Each save writes one object; appending is not supported,
and conversion failures do not replace an existing destination.

The navigator identifies RDS files with a violet **R** document icon and displays serialization
version and compression. Metadata inspection reads only the stream header, without deserializing
the object or adding background computation. See the [RDS guide](gama.extension.rds/README.md)
for conversion details and limitations, and the bundled
[RDS Files examples](gama.extension.rds/models/Data/RDS%20Files) for usage.

#### Dataframes and database migration

The `dataframe` type provides named, heterogeneous columns, `rows` / `columns` / `keys`
properties, conversions from collections/agents/matrices, and behaves as a container of row maps:
ordinary iteration, `where`, `collect` and `first` operate on rows, while column access yields lists.
It is supplied by `gama.extension.dataframe`, with loading/saving examples and a usage recipe
([`a03469892`](https://github.com/gama-platform/gama/commit/a03469892),
[`3e333607c`](https://github.com/gama-platform/gama/commit/3e333607c),
[`8ad942f0e`](https://github.com/gama-platform/gama/commit/8ad942f0e),
[`0b27e1e18`](https://github.com/gama-platform/gama/commit/0b27e1e18)).

- `dataframe_file` imports tabular data, including CSV, JSON, XLSX, Parquet and Avro.
  `save` exports these formats, supports `encoding:`, and can write a map of dataframes
  as separate Excel sheets
  ([`c0a523512`](https://github.com/gama-platform/gama/commit/c0a523512),
  [`f06987e69`](https://github.com/gama-platform/gama/commit/f06987e69),
  [`1ab0c68bb`](https://github.com/gama-platform/gama/commit/1ab0c68bb),
  [`ef2fe1075`](https://github.com/gama-platform/gama/commit/ef2fe1075)).
- Operators include `dataframe_with`, `row_at`, `column_at`, `rows_list`, `columns_list`,
  `iloc`, `cell`, `add_column`, `select_columns`, `remove_empty`, `filter`, `pivot`,
  `pretty_print`, keyed `join` (inner/left/right/full), and `+` concatenation.
  JDBC helpers include `load_table`, `load_sql` and `save_table`. Intermediate `df_*`
  names are not the current API
  ([`022f9ed00`](https://github.com/gama-platform/gama/commit/022f9ed00),
  [`95445cd03`](https://github.com/gama-platform/gama/commit/95445cd03),
  [`f7e96e6e0`](https://github.com/gama-platform/gama/commit/f7e96e6e0)).
- **Migration:** SQL `select` returns a dataframe instead of a list of lists, including
  `AgentDB` results; `create ... from:` uses column names. SQL `insert` replaces
  `columns:` / `values:` with a single `data:` argument accepting a positional list,
  named map or dataframe for batch insertion. Update result indexing and insertion calls
  ([`7fa901903`](https://github.com/gama-platform/gama/commit/7fa901903)).
- New `sparql_query(query, endpoint[, timeout_ms])` executes SPARQL SELECT queries through
  Jena with a configurable timeout. Its current result is a dataframe, including absent
  OPTIONAL bindings
  ([`c15e5c91a`](https://github.com/gama-platform/gama/commit/c15e5c91a),
  [`8f763eb4c`](https://github.com/gama-platform/gama/commit/8f763eb4c),
  [`54eb8374d`](https://github.com/gama-platform/gama/commit/54eb8374d)).

#### Matrix computation and grid integration

GAML's matrix toolkit expands with element-wise matrix/scalar arithmetic, powers, convolution,
scalar comparisons producing masks, `shift`, matrix-valued `ifelse`, and element-wise mathematical
functions. `cell[attribute]` reads a complete grid species attribute as a typed spatial matrix,
with optimized extraction. `cell[attribute] <- values;` writes a matrix back using a
compile-time-checked attribute reference. Values retain their declared types and normal
attribute setter behavior. Matrix dimensions are checked before writing.
Float attribute snapshots are internally fields, so `field(cell[attribute])` reuses the
snapshot without a conversion copy while bracket expressions remain typed as `matrix<float>`.
The same brackets work on ordinary species: `animal[energy]` returns a typed list in population order,
and `animal[energy] <- values;` assigns a compatible list with exactly one element per agent.
Inherited attributes and typed species aliases are supported; list length is checked before writing.
`cell collect expression` also preserves spatial structure and infers the result content type.
**Migration:** the string-based grid operators `matrix_with(grid, "attribute")` and `set_values` are removed;
the dimensions-based `{columns, rows} matrix_with expression` remains available.
Grid `collect` now returns a matrix, so use `list(cell) collect each.attribute` where a flat
list is required. Lists of cells and filtered grids still collect to lists.
Vectorized matrix/field/diffusion paths use the Java Vector API,
and bundled Life and Ant Foraging examples demonstrate matrix-based computation
([`025ac5462`](https://github.com/gama-platform/gama/commit/025ac5462),
[`b16056b3d`](https://github.com/gama-platform/gama/commit/b16056b3d),
[`fc76dcd12`](https://github.com/gama-platform/gama/commit/fc76dcd12),
[`6a5db774e`](https://github.com/gama-platform/gama/commit/6a5db774e),
[`bfd1200e5`](https://github.com/gama-platform/gama/commit/bfd1200e5)).

#### Random draws and statistical correctness

- New statistical tools include one-way `anova`, two-factor `multi_anova` with interaction,
  coefficient standard errors/p-values on regression results, `spearman_correlation`, normalized
  `hsic` and permutation-based `hsic_p_value`. **Limitation:** the `glm` entry point currently
  delegates to ordinary least squares; it does not implement arbitrary GLM families/link functions
  ([`92bb0a650`](https://github.com/gama-platform/gama/commit/92bb0a650),
  [`209453f59`](https://github.com/gama-platform/gama/commit/209453f59),
  [`172fef77d`](https://github.com/gama-platform/gama/commit/172fef77d)).
- `rolling_vc`, `rolling_se` and `power_test` expose incremental replication/sample-size
  diagnostics. `morris_analysis`, `sobol_analysis` and `stochanalysis` generate sensitivity
  reports from existing CSV data, column maps or matrices without a new batch run
  ([`123a747c0`](https://github.com/gama-platform/gama/commit/123a747c0),
  [`9ac3c7aef`](https://github.com/gama-platform/gama/commit/9ac3c7aef),
  [`54a25c012`](https://github.com/gama-platform/gama/commit/54a25c012)).
- Latin-hypercube sampling corrects sample counts and uses ESE-inspired maximin optimization.
  `outer_iter` / `inner_iter` configure effort (defaults 50 / 100). **Reproducibility:** changed
  design algorithms need not reproduce older experimental plans from the same seed
  ([`f62bda9fd`](https://github.com/gama-platform/gama/commit/f62bda9fd), #773;
  [`d37aa9fdf`](https://github.com/gama-platform/gama/commit/d37aa9fdf),
  [`bb1289dc6`](https://github.com/gama-platform/gama/commit/bb1289dc6)).
- `binomial(n, 1)` terminates immediately rather than looping; weighted `rnd_choice` accepts
  integer as well as float weights. Hierarchical spatial clustering retains nested merges and
  updates distances correctly
  ([`b6030ce2c`](https://github.com/gama-platform/gama/commit/b6030ce2c), #743;
  [`b58308d3a`](https://github.com/gama-platform/gama/commit/b58308d3a), #976;
  [`ff4b54662`](https://github.com/gama-platform/gama/commit/ff4b54662), #928).
- New opt-in integer-sampling forms `rnd(max, true)` and `rnd(min, max, true)` support
  inclusive integer bounds without relying on float-based sampling. Existing `rnd` calls
  retain their legacy seeded sequences; the new forms deliberately produce different sequences.
  Assigning `rng_usage` restores an absolute raw-draw position by resetting and advancing the RNG,
  rather than advancing relative to its current position. This restores the raw stream, not
  cached Gaussian values; the threaded generator cannot be restored this way
  ([`5146893c6`](https://github.com/gama-platform/gama/commit/5146893c6)).
- Stepped `rnd` draws validate finite, nonzero steps and their direction, including descending
  and equal-bound cases. `exp_rnd` and `exp_density` require a finite, positive scale; their
  documentation clarifies that scale is the mean, not the rate
  ([`85af4a720`](https://github.com/gama-platform/gama/commit/85af4a720)).
- Pearson `correlation` no longer mixes sample covariance with population standard deviations,
  which could produce values outside `[-1, 1]`. HSIC p-values are bounded and interrupted
  computations are rejected instead of returning misleading results
  ([`0df7b48b5`](https://github.com/gama-platform/gama/commit/0df7b48b5),
  [`fcac916c5`](https://github.com/gama-platform/gama/commit/fcac916c5)).
- Sobol, Morris and stochasticity-analysis fixes improve sampling/reporting and robustness
  ([`ddcaebf5c`](https://github.com/gama-platform/gama/commit/ddcaebf5c),
  [`d300525fd`](https://github.com/gama-platform/gama/commit/d300525fd),
  [`9e1db9406`](https://github.com/gama-platform/gama/commit/9e1db9406),
  [`e084bbdb2`](https://github.com/gama-platform/gama/commit/e084bbdb2),
  [`746717490`](https://github.com/gama-platform/gama/commit/746717490)).

- Statistical operators now report GAML errors for malformed `kmeans` points, invalid `power_test` probabilities or samples, out-of-range `quantile` probabilities, impossible `split_in` bin counts, and missing or undersized `t_test` samples instead of leaking Java exceptions. `split_in` also honors its strict/non-strict boundary argument.

#### System commands: persistent contexts and process cleanup

The built-in `context` class and `command(string, context)` overload preserve shell state,
including the working directory and variables, between commands. Contexts expose command/output
history, the latest result and exit status, an assignable directory, and `close()`. Unlike the
string-returning command overloads, a nonzero exit status is available for inspection rather than
automatically raising an error. See the [migration guide](MIGRATION_GUIDE_2026.md#persistent-shell-contexts)
([`b224d7265`](https://github.com/gama-platform/gama/commit/b224d7265), #444).

Agent-owned command processes and subprocesses are terminated when their owner is disposed,
including managed background commands
([`e8e377ac7`](https://github.com/gama-platform/gama/commit/e8e377ac7), #450).

#### Physics: joints and Box2D corrections

The physics extension adds the `joint` type and operators to create, inspect, update and destroy
physical constraints. Supported forms include hinge, slider, ball-and-socket and fixed joints,
plus Box2D distance/rope/wheel joints and Bullet cone-twist joints. `with_motor_speed` and
`with_max_motor_force` update live motors; constraints are removed before their bodies are
removed or rebuilt. Backend-specific capabilities and units are documented on the operators
([`f70eb86bd`](https://github.com/gama-platform/gama/commit/f70eb86bd),
[`f30436856`](https://github.com/gama-platform/gama/commit/f30436856),
[`174553c1e`](https://github.com/gama-platform/gama/commit/174553c1e),
[`86813e10c`](https://github.com/gama-platform/gama/commit/86813e10c),
[`a510fd328`](https://github.com/gama-platform/gama/commit/a510fd328)).

Box2D coordinate/orientation and engine-consistency fixes accompany articulated-arm, jointed-snake,
wheel-car, cone-twist-tentacle and pedestrian-flow/evacuation examples
([`3735b08ab`](https://github.com/gama-platform/gama/commit/3735b08ab),
[`9d83d5e85`](https://github.com/gama-platform/gama/commit/9d83d5e85),
[`7e3011ff1`](https://github.com/gama-platform/gama/commit/7e3011ff1),
[`017986532`](https://github.com/gama-platform/gama/commit/017986532), #895).

#### Pedestrian, driving and Bullet corrections

- Pedestrian `walk` obstacle avoidance is corrected; `path_deviation` and
  `path_deviation_radius` add noise to followed paths
  ([`b29a87a7f`](https://github.com/gama-platform/gama/commit/b29a87a7f), PR #1161).
- Bullet tracks dynamic bodies and reuses temporary math objects; collision-detection bounds
  use min/max extents. Driving avoids redundant location updates and corrects headings
  near segment goals
  ([`6153550b6`](https://github.com/gama-platform/gama/commit/6153550b6),
  [`b2d7cd1d7`](https://github.com/gama-platform/gama/commit/b2d7cd1d7)).

#### Geometry and spatial operations

- Worlds with depth use an octree for three-dimensional spatial indexing; flat worlds retain
  a quadtree. Envelope operations distinguish projected and full-3D intersections, while point
  math treats missing Z coordinates as zero for valid 2D operations. Generated 3D arcs interpolate
  elevation, and quaternion axis/angle extraction and application are corrected
  ([`1a39dd302`](https://github.com/gama-platform/gama/commit/1a39dd302),
  [`1b415e423`](https://github.com/gama-platform/gama/commit/1b415e423),
  [`b6a18d0cb`](https://github.com/gama-platform/gama/commit/b6a18d0cb), #769;
  [`956735a33`](https://github.com/gama-platform/gama/commit/956735a33), #1003).
- Geometries gain `horizontal_flip`, `vertical_flip`, matrix-based `transformed_by`,
  `rotation_matrix` and `scale_matrix`. `envelope(file, depth)` supports georeferenced 3D worlds.
  Geometry-envelope calculations use projected geometry consistently; the geometry `envelope`
  attribute is deprecated in favor of the operator
  ([`19ffef1d0`](https://github.com/gama-platform/gama/commit/19ffef1d0), #657;
  [`804128e7e`](https://github.com/gama-platform/gama/commit/804128e7e), #347;
  [`93b456e1f`](https://github.com/gama-platform/gama/commit/93b456e1f), #135).
- Invalid non-finite agent coordinates now raise errors instead of triggering unbounded
  spatial-index subdivision. Quadtree bounds checks use the 2D coordinates appropriately;
  `square` accepts negative sizes like `rectangle`, using their absolute size
  ([`625c6329d`](https://github.com/gama-platform/gama/commit/625c6329d), #1248, #1249;
  [`a63ae2115`](https://github.com/gama-platform/gama/commit/a63ae2115)).
- Difficult triangulations retry after geometry cleanup and precision/tolerance adjustment.
  Network endpoint snapping edits actual coordinates, and `split_lines(..., true)` retains
  attributes while using the normal splitting algorithm
  ([`591c12f10`](https://github.com/gama-platform/gama/commit/591c12f10),
  [`8920dbe44`](https://github.com/gama-platform/gama/commit/8920dbe44), #984;
  [`1d0aaf5d2`](https://github.com/gama-platform/gama/commit/1d0aaf5d2), #1001).
- Hex-grid lookup uses cell locations; combining diffusion operations avoids modifying the
  list being iterated. Graph edge-agent populations avoid node-only topologies and guard dead
  endpoints. Indexed Fruchterman-Reingold layout validates theta in `[0,1]`
  ([`270df531b`](https://github.com/gama-platform/gama/commit/270df531b),
  [`1b5863bee`](https://github.com/gama-platform/gama/commit/1b5863bee),
  [`2aeecfeb2`](https://github.com/gama-platform/gama/commit/2aeecfeb2),
  [`7c2796a93`](https://github.com/gama-platform/gama/commit/7c2796a93), #986).
- Spatial indices expand beyond the original world bounds so queries find out-of-world agents.
  Non-point `at_distance` queries and path-finder end-segment selection are corrected
  ([`3bef77563`](https://github.com/gama-platform/gama/commit/3bef77563), #1174;
  [`dece2d97f`](https://github.com/gama-platform/gama/commit/dece2d97f), #1220;
  [`bbe91257d`](https://github.com/gama-platform/gama/commit/bbe91257d), #1117).

#### Recording: shared Fory dependency

Apache Fory is now supplied and exported by `gama.dependencies`, rather than embedded privately
in the serialization extension. This lets generated serializers access Fory from model bundles,
fixing record-experiment failures involving image files, OBJ files and grids while retaining JIT compilation.

Fory is the sole binary serializer. The embedded FST implementation, backend-selection preference
(`pref_binary_serializer`) and `gama.binary.serializer` system property have been removed.
Binary saves produced with FST are not supported; regenerate saved simulations and serialized objects
with GAMA 2026. There is no legacy reader or automatic conversion.

#### BDI: shared internal mental-state operations

The BDI extension now centralises its built-in modality/base mapping, mental-state construction, and common
predicate and name lookups (#161). Public string-based modalities (including custom names), GAML actions and facets,
base-argument overrides, and modality-specific insertion/removal effects remain unchanged. Beliefs still perform
their specialised contradiction and intention handling; emotions retain their separate replacement policy.

#### BDI: configurable thought-history capacity

Agents using `simple_bdi` or `parallel_bdi` can set `thinking_capacity` to control the size of their `thinking`
history (#161). The default remains 5 and thoughts remain newest-first. Setting the capacity to 0 clears and
disables automatic history; negative values raise an error. Reducing the capacity immediately discards the oldest
excess entries. History updates still replace the list rather than mutating previously returned lists.

#### BDI: shared statement execution

The `rule`, `coping`, and `law` statements now share internal condition checks, mental-state preparation, and
list-effect operations (#161), without removing or merging their GAML syntax. Statement-specific guards,
`all` expansion, scalar versus indexed strength/lifetime evaluation, specialised base updates, and the order of
effects remain unchanged. Coping bodies retain their existing execution and return-value behaviour; laws retain
their obedience threshold and plan/norm invalidation. This refactoring does not change legacy matching policies.

#### BDI: numeric contracts and emotion calculations

BDI variable and operator documentation now distinguishes conventional ranges from existing validation, and describes
numeric defaults, unspecified intensity, and decay units (#161). Repeated desire-appraisal and compound-emotion
intensity calculations and personality-based decay share internal helpers. This is a compatibility-preserving
refactoring: the decay coefficient remains `0.00028`, multiplication order and negative decay are unchanged,
and no new clamping or rejection of previously accepted numeric values is introduced. Existing presence flags and
operator-specific range checks (including `with_dominance` excluding 1) remain unchanged.
