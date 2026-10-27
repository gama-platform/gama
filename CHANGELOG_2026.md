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
Unmerged branches and changes in other repositories are outside this scope.

For migration-sensitive additions, start with the [GAML syntax overhaul and translator](#breaking-change-gaml-syntax-overhaul-and-model-translator),
then see [compiler diagnostics](#compiler-diagnostics-dates-and-expression-semantics),
[SQL/dataframes](#dataframes-and-database-migration), [numeric/container behavior](#gaml-string-container-and-numeric-correctness)
and [random draws/sampling](#random-draws-and-statistical-correctness), in addition to the Fory and nil-container notes below.

## [2026.0.0] - Unreleased

### Nested map content types

- Converting nested maps and lists now propagates declared container types even when the inferred
  types are assignment-compatible. A `map<string, map<string, unknown>>` initialized with float-only
  inner maps accepts later string values without coercing them to floats (#751). Explicitly typed
  maps still cast inserted values when container-content casting is enabled.

### BREAKING CHANGE: GAML syntax overhaul and model translator

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

### GAML: percentage literals

Numeric values can now use `%` as a postfix percentage suffix: `100%` is equivalent to
`100 #percent` and evaluates to `1.0`. This also works with decimal values and in larger
expressions, for example `2.5%` or `100% + 2%`.

### GAML: reduce and scan iterators

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

### Parameters: display units (`unit:` facet)

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

### Recording: shared Fory dependency

Apache Fory is now supplied and exported by `gama.dependencies`, rather than embedded privately
in the serialization extension. This lets generated serializers access Fory from model bundles,
fixing record-experiment failures involving image files, OBJ files and grids while retaining JIT compilation.

Fory is the sole binary serializer. The embedded FST implementation, backend-selection preference
(`pref_binary_serializer`) and `gama.binary.serializer` system property have been removed.
Binary saves produced with FST are not supported; regenerate saved simulations and serialized objects
with GAMA 2026. There is no legacy reader or automatic conversion.

### Runtime errors: persistent, clickable status

Runtime errors, including experiment launch failures (#286), remain visible as red text on the normal top-right status background
until clicked, rather than being replaced by background activity or the idle timer. Click the error text to
open its source location in the editor; hover to read the full error, or right-click to open an error-details
dialog. If no source location is available, clicking opens the details instead. The dropdown arrow still
opens status history, where errors also provide their full text on hover and link to the editor.

### Lists and maps: explicit nil is distinct from an empty container

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

### Editor: per-file automatic validation control

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

### Co-model imports: preserve aliases while editing

Adding, renaming, or removing `as` on an existing import now preserves the import's dependency-graph entry
(#774). Aliased imports support spaces in both filenames and directories, without requiring files to be renamed.

### RDS files: import and export R serialized data

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

### BIG CHANGE: keyboard combinations in display events

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

### BEHAVIOR CHANGE: macOS 3D camera controls use CMD, not CTRL

In **both OpenGL and OpenGL4**, the camera modifier is now **CMD only on macOS**. Previously, both CTRL and CMD
could activate modified camera navigation. macOS users who held CTRL while rotating or moving the camera must
now hold **CMD** instead. CTRL is no longer a camera modifier and can be used for model-defined keyboard events
without also activating that modifier's camera behavior.

**Windows and Linux keep CTRL as their camera modifier**; the Windows/Meta key does not substitute for it.
The platform-specific selection applies to mouse and keyboard camera interaction and to the camera helper's
display shortcuts. Unmodified navigation and SHIFT-based controls are unchanged. On macOS, **CTRL-click still
opens the contextual menu**; this change does not override operating-system or workbench shortcuts.

### BDI: shared internal mental-state operations

The BDI extension now centralises its built-in modality/base mapping, mental-state construction, and common
predicate and name lookups (#161). Public string-based modalities (including custom names), GAML actions and facets,
base-argument overrides, and modality-specific insertion/removal effects remain unchanged. Beliefs still perform
their specialised contradiction and intention handling; emotions retain their separate replacement policy.

### BDI: configurable thought-history capacity

Agents using `simple_bdi` or `parallel_bdi` can set `thinking_capacity` to control the size of their `thinking`
history (#161). The default remains 5 and thoughts remain newest-first. Setting the capacity to 0 clears and
disables automatic history; negative values raise an error. Reducing the capacity immediately discards the oldest
excess entries. History updates still replace the list rather than mutating previously returned lists.

### BDI: shared statement execution

The `rule`, `coping`, and `law` statements now share internal condition checks, mental-state preparation, and
list-effect operations (#161), without removing or merging their GAML syntax. Statement-specific guards,
`all` expansion, scalar versus indexed strength/lifetime evaluation, specialised base updates, and the order of
effects remain unchanged. Coping bodies retain their existing execution and return-value behaviour; laws retain
their obedience threshold and plan/norm invalidation. This refactoring does not change legacy matching policies.

### BDI: numeric contracts and emotion calculations

BDI variable and operator documentation now distinguishes conventional ranges from existing validation, and describes
numeric defaults, unspecified intensity, and decay units (#161). Repeated desire-appraisal and compound-emotion
intensity calculations and personality-based decay share internal helpers. This is a compatibility-preserving
refactoring: the decay coefficient remains `0.00028`, multiplication order and negative decay are unchanged,
and no new clamping or rejection of previously accepted numeric values is introduced. Existing presence flags and
operator-specific range checks (including `with_dominance` excluding 1) remain unchanged.

### GAMA Server: full exchange logs

GUI and headless servers can now record all WebSocket exchanges in a separate JSON Lines file per run under `~/.gama/server-logs/` (#1269). Logging is **off by default**; enable it in **Network > Server mode** or with headless `-server-log`. Headless `-server-log-directory <path>` and `-no-server-log` override settings for one launch without changing saved preferences. Records include timestamps, connection IDs, directions and full payloads, including broadcasts, asynchronous events and binary/control frames. See [exchange logging configuration, format and privacy precautions](gama.headless/README.md#server-exchange-logs).

Clients can also send `{"type":"server_log","enabled":true}` (or `false`) to toggle logging immediately for the whole running GUI/headless server without changing saved settings. Re-enabling appends to the same run's log.

The GUI logging preference also takes effect immediately, without restarting GAMA or disconnecting clients. The log-directory preference still applies to the next server run.

### GAMA Server: configurable GUI loading

WebSocket `load` now honors `parameters`, `until`, `console`, `status`, `dialog` and `runtime` in desktop GUI mode, not only headless mode (#394). Parameter values are applied before `global init`. Forwarding options use the same defaults in both modes and do not suppress local GUI output. The loaded experiment retains its requesting socket even when another client connects. A GUI experiment pauses and sends `SimulationEnded` when its stop condition is reached; reload it before running again. See the [GUI loading guide and runnable client example](gama.headless/README.md).

### GAMA Server: read and configure platform preferences

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

### 🏗️ Architecture & Core API (`gama.api`)
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

### 📁 Workspace Models Organisation
Plugins now contribute models through `models/{Data,Demos,Library,Recipes,Tutorials,Tests}/<GAMA projects>`; the `gama.models` extension point is removed. Projects are classified by location (not by natures), the contributing plugin is stored in the `.project` comment, and the GAMA nature is now `gama.workspace.nature` (the former ids are still recognised and migrated). The navigator gains a "Clean and update workspace..." command, hides empty top-level folders, and supports user *group folders* (plain directories of the workspace containing several projects, see #678) that can be created, renamed, deleted, filled by drag and drop, paste, and the New/Import project wizards.

The navigator's **Search...** field now offers **Title | Body** search modes. Title (the default) searches all filenames, folder, group and project names, and model tags, case-insensitively. Matching folders show their contents; ancestor folders remain visible for matching files. Body searches saved model source, including comments, case-insensitively. Body scans run in the background after a short typing delay, cancel when the query or mode changes, and use a bounded cache checked against resource modification stamps and charset. Unsaved editor changes are not searched. Clearing the field or pressing Escape restores the previous tree expansion.

The navigator's top-right menu includes **Modification date** and, when enabled, **Most recent first**. These saved settings replace the toolbar's date-sorting button. Dates can be displayed while retaining alphabetical order; hiding dates restores alphabetical order without losing the preferred date-sorting setting.

In the **User** folder and its groups, **New...** offers **Group...** (a name dialog) and **Project...** (the project wizard, prefilled with the selected group). Editable groups have icon-bearing **Rename...** and **Delete...** actions separated from the other options.

### 🚀 GAML Language & Compiler (AST)
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

### ⚡ Concurrency & Parallel Execution
Massive performance upgrades taking advantage of JDK 25 multithreading enhancements for parallel agent execution.

*   `[65a958e]` Improve ForkJoin executor and parallelism cache
*   `[5e637b9]` Improve thread-safety and performance in statements
*   `[dd3af06]` (via `a61ece7`) Thread-safe registry handling and Atomic ops

### 🖥️ UI, Displays & OpenGL 4.1 Native
A new `opengl4` display renderer uses OpenGL 4.1 Core Profile instead of fixed-function APIs.
The existing `opengl` renderer remains available; the core-profile migration applies to the new renderer,
not the removal of all legacy displays.

OpenGL4 lighting now uses consistent eye-space coordinates for surfaces and lights, supports up to eight active diffuse lights with directional, point and spotlight behavior (including attenuation), and leaves only ambient illumination when all diffuse lights are disabled. Two-sided lighting, material-color clamping before texture modulation, and the absence of unsolicited specular highlights match the legacy renderer, including textured OBJ models such as Moving 3D Object.

OpenGL4 also matches legacy draw rotations and primitive orientations, correcting reversed sprites and axis arrows. Textured draws premultiply by texel alpha and blend like legacy OpenGL. This removes bright fringes around transparent icons and keeps translucent textured layers, such as the water in Moving 3D Object, at full brightness.

The shared default light intensity for OpenGL and OpenGL4 is increased from 160 to 180 for slightly brighter scenes. Explicit model intensities and saved lighting preferences are unchanged.

Both 3D renderers now limit the effective `z_near` to `z_far / 100000`. Very small values no longer exhaust depth-buffer precision, which made back faces show through and objects appear lit from behind (#973).

Empty 3D displays now show their axes; previously they were hidden as if the display only contained charts (#809).

### Charts: dynamic bodies, distinct names and stable colors

- The body of a `chart` (its `data` and `datalist` statements and any `if`, `loop`… around them) is now re-evaluated at every step (#403). Series can appear conditionally, be created in loops, or get a new legend (which creates a new series). A series that is no longer declared is frozen (kept, but no longer updated) in charts showing a history of values (`series`, `xy`, `scatter`, `heatmap`) and removed in charts showing a snapshot (`pie`, `histogram`, `radar`, `box_whisker`). The other facets of `chart` keep their previous behaviour.
- Series are identified internally, independently of their legend (#785): several `data` statements can share the same legend, and pie labels, radar legends, histogram bar labels, tooltips and saved histories display legends rather than internal ids. Categories (`x_serie_labels`, `datalist` categories) are identified by position, so duplicated labels such as `["a", "b", "a"]` give three distinct categories.
- Histograms, radars and box-whisker charts keep their colors when the number of values of a `datalist` changes (#801).
- `y2_tick_values_visible` hides the tick values of the second y axis (#783).
- `include_init: false` skips the initial chart sample; the default remains `true`
  ([`60e8b408d`](https://github.com/gama-platform/gama/commit/60e8b408d), #1210).

Manual regression models are available in `Tests/Chart Tests/models` (`Chart Varying Datalist Regression`, `Chart Duplicate Names Regression`, `Chart Dynamic Body Regression`).

The test view has a **Stop tests** button and displays the currently executing model, experiment and test name. By default, GUI tests execute in the workbench JVM, using scope cancellation and thread interruption. **Stop tests** interrupts the current experiment and ends the suite, preserving completed results and marking the active test as **interrupted**. **Execution > Tests > Test experiment timeout (seconds)** defaults to 10 seconds: a timeout requests interruption, marks the active test as **timeout**, and continues with the next experiment once execution actually exits. The runner never abandons a live test thread. Unstarted tests and assertions remain **not reached**.

**Advanced > Tests > Run tests in a separate headless process** enables stronger isolation for Java operators that ignore cooperative cancellation. An isolated sequence reuses an initialized headless worker, replacing it after a timeout or worker failure and terminating it when the sequence ends. In this mode Stop and timeout can forcibly terminate unresponsive computations. The timeout covers experiment initialization and execution, not model compilation or worker startup. Isolated tests require saved local model files; default same-JVM execution can use the model compiled from the editor.

The test-view toolbar shows **Stop tests** only during execution and **Expand/Collapse** only when the resulting summary is displayed.

The compact **Tests** view is now the default. **Advanced > Tests > Use the compact test results view** can be disabled to use the legacy view for the next test run; changing the preference does not move an active run or its report. The compact view uses a virtual native tree (experiment, test, assertion), small status indicators, summary counters, and one shared details pane instead of per-assertion editor controls. It provides problems-only filtering, severity sorting, source navigation, copy/export, and coalesced progress updates.

The compact view places Expand/Collapse on the right and hides them for empty or flat reports. Its details pane appears only when a test or assertion is selected, shows the source filename rather than its path, and includes complete error stacks (including causes and GAML context) in both execution modes.

Summaries and the compact view label unexecuted tests and assertions **not reached**. These tests remain included in the summary counters, but **Problems only** hides not-reached entries.

Before running the test suite, a discovery pass counts individual tests across its test experiments. The compact view shows “Discovering tests...” during this pass, then a live completed/total counter in both execution modes, retaining partial counts after Stop or timeout.

Statistical operators now report GAML errors for malformed `kmeans` points, invalid `power_test` probabilities or samples, out-of-range `quantile` probabilities, impossible `split_in` bin counts, and missing or undersized `t_test` samples instead of leaking Java exceptions. `split_in` also honors its strict/non-strict boundary argument.

*   `[47f55ee]` Migrate renderer to OpenGL 4 core profile
*   `[7614445]` Migrate OpenGL4 renderer away from fixed-function
*   `[973f54c]` Add FastTriangulation and use for polygons
*   `[117e307]` Remove bundled JOGL and shaders from OpenGL4
*   `[2322a83]` Introduce output title API and UI fixes (New overlay handling)

### 🧹 Refactoring & Code Quality
The codebase makes extensive use of new Java features like pattern matching, resulting in cleaner and more maintainable backend code.

*   `[775b94c]` Refactor TypePair equals and descriptions (Pattern matching)
*   `[6ff70b5]` Refactor annotations, replace literals with constants
*   `[8440794]` Move IKeyword to annotations package
*   `[c138702]` Add package-info and improve API Javadocs
*   `[ac8b13c]` Migrate transition transformers from Python to Java

### GAML: string, container and numeric correctness

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

### Matrix computation and grid integration

GAML's matrix toolkit expands with element-wise matrix/scalar arithmetic, powers, convolution,
scalar comparisons producing masks, `shift`, matrix-valued `ifelse`, and element-wise mathematical
functions. `matrix_with(grid, "attribute")` reads a grid attribute as a matrix; `set_values`
writes matrix values back to the grid. Vectorized matrix/field/diffusion paths use the Java Vector API,
and bundled Life and Ant Foraging examples demonstrate matrix-based computation
([`025ac5462`](https://github.com/gama-platform/gama/commit/025ac5462),
[`b16056b3d`](https://github.com/gama-platform/gama/commit/b16056b3d),
[`fc76dcd12`](https://github.com/gama-platform/gama/commit/fc76dcd12),
[`6a5db774e`](https://github.com/gama-platform/gama/commit/6a5db774e),
[`bfd1200e5`](https://github.com/gama-platform/gama/commit/bfd1200e5)).

### Compiler diagnostics, dates and expression semantics

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

### Dataframes and database migration

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

### Random draws and statistical correctness

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

### Simulation lifecycle, scheduling and batch execution

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
  experiment/simulation population lookup; updated examples use standalone `.experiment`
  adapters instead of requiring coupling-specific experiments in base models
  ([`c32f8820a`](https://github.com/gama-platform/gama/commit/c32f8820a),
  [`0a8fe4f0c`](https://github.com/gama-platform/gama/commit/0a8fe4f0c),
  [`a5ff1a3c5`](https://github.com/gama-platform/gama/commit/a5ff1a3c5),
  [`55eaa76cd`](https://github.com/gama-platform/gama/commit/55eaa76cd)).

### System commands: persistent contexts and process cleanup

The built-in `context` class and `command(string, context)` overload preserve shell state,
including the working directory and variables, between commands. Contexts expose command/output
history, the latest result and exit status, an assignable directory, and `close()`. Unlike the
string-returning command overloads, a nonzero exit status is available for inspection rather than
automatically raising an error. See the [migration guide](MIGRATION_GUIDE_2026.md#persistent-shell-contexts)
([`b224d7265`](https://github.com/gama-platform/gama/commit/b224d7265), #444).

Agent-owned command processes and subprocesses are terminated when their owner is disposed,
including managed background commands
([`e8e377ac7`](https://github.com/gama-platform/gama/commit/e8e377ac7), #450).

### GeoPackage, raster and file interoperability

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

### Physics: joints and Box2D corrections

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

### Geometry, overlays and display reliability

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

### Editor, consoles and optional integrations

- A syntax-aware formatter handles the rewritten grammar's operators, calls, facets, blocks
  and comments. Editor reconciliation/documentation rejects obsolete work, and launching a model
  no longer leaves ghost diagnostics from temporary build/import validation
  ([`9e5d06045`](https://github.com/gama-platform/gama/commit/9e5d06045),
  [`be7b93581`](https://github.com/gama-platform/gama/commit/be7b93581),
  [`a1eba1d0c`](https://github.com/gama-platform/gama/commit/a1eba1d0c),
  [`5343d881f`](https://github.com/gama-platform/gama/commit/5343d881f),
  [`0ad0fd2bc`](https://github.com/gama-platform/gama/commit/0ad0fd2bc)).
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
- Context menus can search a word in the global GAML reference from editors and consoles.
  Hover documentation works for uncompiled models; reference popups, editor readability and
  theme transitions have been improved. The console gains a search field
  ([`791fcc165`](https://github.com/gama-platform/gama/commit/791fcc165),
  [`8e4a0dfdd`](https://github.com/gama-platform/gama/commit/8e4a0dfdd),
  [`f12d7b5eb`](https://github.com/gama-platform/gama/commit/f12d7b5eb),
  [`196873acb`](https://github.com/gama-platform/gama/commit/196873acb),
  [`b80e1d7b7`](https://github.com/gama-platform/gama/commit/b80e1d7b7)).
- Interactive-console history navigation preserves draft input and respects prompt/editing
  boundaries, with startup help and clearer errors
  ([`e3c873782`](https://github.com/gama-platform/gama/commit/e3c873782)).
- Editor code minings are grouped, deduplicated and truncated; operator hovers show parameter
  names. Folding an `if` leaves its `else` visible, and end-of-file template insertion is corrected
  ([`9ed49020c`](https://github.com/gama-platform/gama/commit/9ed49020c), #548;
  [`34477accf`](https://github.com/gama-platform/gama/commit/34477accf), #759;
  [`28c0d6da6`](https://github.com/gama-platform/gama/commit/28c0d6da6),
  [`513662253`](https://github.com/gama-platform/gama/commit/513662253)).
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
- Simulation-speed sliders use normalized positions; a failed list-editor parse preserves
  the original value rather than replacing it with an empty list
  ([`21f9f7f7e`](https://github.com/gama-platform/gama/commit/21f9f7f7e),
  [`bc2d1af4d`](https://github.com/gama-platform/gama/commit/bc2d1af4d)).
- The memory control shows **MEM xx%** beside garbage collection and refreshes the displayed
  usage after collection. Copilot and diagramming support are optional Support-menu installations;
  Copilot is no longer bundled into the default product
  ([`e069b5f52`](https://github.com/gama-platform/gama/commit/e069b5f52), #1254;
  [`755acd4d6`](https://github.com/gama-platform/gama/commit/755acd4d6),
  [`b815e0c8f`](https://github.com/gama-platform/gama/commit/b815e0c8f)).

### GAMA Server, headless and TCP follow-ups

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

### Tests and extension-development tooling

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
- Annotation-driven regression coverage is expanded across operators, constants, units and types;
  the processor generates separate tests per annotation and operator/cast fuzz tests. Robustness
  suites cover Unicode, nil, invalid input and numerical edge cases
  ([`633fe117e`](https://github.com/gama-platform/gama/commit/633fe117e),
  [`13d049068`](https://github.com/gama-platform/gama/commit/13d049068),
  [`7809b87ad`](https://github.com/gama-platform/gama/commit/7809b87ad),
  [`7640eb14f`](https://github.com/gama-platform/gama/commit/7640eb14f)).
- Java plugins can register non-agent GAML classes with `@gaml_class`, including constructors,
  inherited attributes and actions. Object and agent construction share the `getConstructor()`
  API; see the [Java-backed class migration guide](MIGRATION_GUIDE_2026.md#java-backed-built-in-classes)
  ([`86dd66fd1`](https://github.com/gama-platform/gama/commit/86dd66fd1),
  [`31db829c3`](https://github.com/gama-platform/gama/commit/31db829c3)).
- **GAMA Developer Tools** is installable into Eclipse from
  `https://gama-platform.org/gama/devtools/`. It supplies plugin/skill/operator/action/type/test
  wizards, configuration diagnostics and quick fixes, annotation-processor repair/rebuild,
  test-stub and documentation reports, GAML-keyword implementation lookup, package dependency
  graphs, previewable Java-header maintenance, and GUI/headless/test run/debug configurations.
  Eclipse itself can run on Java 21+, while GAMA launches use JDK 25. See the
  [developer-tools guide](gama.ui.devtools/README.md)
  ([`c7cc2e9f3`](https://github.com/gama-platform/gama/commit/c7cc2e9f3),
  [`cb7406e28`](https://github.com/gama-platform/gama/commit/cb7406e28),
  [`81b210fb7`](https://github.com/gama-platform/gama/commit/81b210fb7),
  [`1118b29d5`](https://github.com/gama-platform/gama/commit/1118b29d5)).

### Additional chart and experiment controls

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

### Additional spatial, pedestrian and display changes

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
- Spatial indices expand beyond the original world bounds so queries find out-of-world agents.
  Non-point `at_distance` queries and path-finder end-segment selection are corrected
  ([`3bef77563`](https://github.com/gama-platform/gama/commit/3bef77563), #1174;
  [`dece2d97f`](https://github.com/gama-platform/gama/commit/dece2d97f), #1220;
  [`bbe91257d`](https://github.com/gama-platform/gama/commit/bbe91257d), #1117).
- Raster export to TIF/ASC uses GeoTools; GeoTIFF saving accepts `no_data:`.
  Projection factories initialize asynchronously to reduce model-loading delays
  ([`de9b9206d`](https://github.com/gama-platform/gama/commit/de9b9206d),
  [`7946bd916`](https://github.com/gama-platform/gama/commit/7946bd916), #7;
  [`8ac6f4f0d`](https://github.com/gama-platform/gama/commit/8ac6f4f0d), #1074).
- Pedestrian `walk` obstacle avoidance is corrected; `path_deviation` and
  `path_deviation_radius` add noise to followed paths
  ([`b29a87a7f`](https://github.com/gama-platform/gama/commit/b29a87a7f), PR #1161).
- Bullet tracks dynamic bodies and reuses temporary math objects; collision-detection bounds
  use min/max extents. Driving avoids redundant location updates and corrects headings
  near segment goals
  ([`6153550b6`](https://github.com/gama-platform/gama/commit/6153550b6),
  [`b2d7cd1d7`](https://github.com/gama-platform/gama/commit/b2d7cd1d7)).
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

### Platform, extension packaging and bundled models

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
- Library additions include **Game of Life with time** and **1D Cellular Automaton Generator**,
  alongside dataframe, physics and matrix-computation examples
  ([`b06758d31`](https://github.com/gama-platform/gama/commit/b06758d31),
  [`951060883`](https://github.com/gama-platform/gama/commit/951060883)).
- New **SIRAnalysis** tutorials cover Latin hypercube, Morris, Sobol, stochasticity and
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
- Distribution/build tooling repairs generated p2 deployment, validates embedded-JDK download
  checksums and signs nested macOS libraries with spaced paths. Release publication runs code
  tests; documentation generation publishes an updated Prism GAML syntax definition
  ([`2884a856d`](https://github.com/gama-platform/gama/commit/2884a856d),
  [`fe4ca2a05`](https://github.com/gama-platform/gama/commit/fe4ca2a05),
  [`477d6237b`](https://github.com/gama-platform/gama/commit/477d6237b),
  [`ea731b652`](https://github.com/gama-platform/gama/commit/ea731b652),
  [`05eacbb76`](https://github.com/gama-platform/gama/commit/05eacbb76)).
