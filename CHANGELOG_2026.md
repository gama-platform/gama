# GAMA 2026 Changelog

This document lists the major technical modifications and commits introduced in the GAMA Version 2026 update (branch `Upgrade-to-Eclipse-2025-12-and-JDK-25`).

All commits listed below point to specific changes that drive the massive architectural evolution of this release.

## [2026.0.0] - Unreleased

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
