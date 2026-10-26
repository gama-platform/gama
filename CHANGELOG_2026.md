# GAMA 2026 Changelog

This document lists the major technical modifications and commits introduced in the GAMA Version 2026 update (branch `Upgrade-to-Eclipse-2025-12-and-JDK-25`).

All commits listed below point to specific changes that drive the massive architectural evolution of this release.

## [2026.0.0] - Unreleased

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
A brand-new, blazing-fast native display plugin relying solely on OpenGL 4.1 Core Profile logic. Legacy APIs have been removed.

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
