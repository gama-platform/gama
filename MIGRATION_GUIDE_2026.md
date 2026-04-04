# GAMA Version 2026: Innovations and Comprehensive Migration Guide

This document is divided into three major sections. **Part 0** offers a complete, summarized ChangeLog. **Part 1** details the profound innovations, technological leaps, and grammatical evolutions introduced in GAMA Version 2026 (branch `Upgrade-to-Eclipse-2025-12-and-JDK-25`). **Part 2** serves as an exhaustive, technical migration reference guide for developers and modelers transitioning from previous versions.

---

# Part 0: Executive Summary & ChangeLog

This high-level ChangeLog summarizes the entire scope of the GAMA Version 2026 update.

### Core Technology & Architecture
*   **JDK 25 & Eclipse 2025-12:** Platform entirely upgraded to the latest Java Development Kit (JDK 25) and Eclipse 2025-12 RCP.
*   **Module Split (`gama.api`):** Creation of the `gama.api` module. Complete decoupling of public interfaces (`IAgent`, `IPoint`, `IList`) from implementation details (`gama.core`).
*   **Factory Generalization:** Direct constructors (e.g., `new GamaList()`) are abolished in favor of Factory patterns (`GamaListFactory.create()`).
*   **Decentralized Casting:** The monolithic `Cast` utility class is reduced; casting is delegated to individual type implementations (`Types.LIST.cast()`).
*   **Population Refactoring:** Centralization of population logic into `AbstractPopulation`. API updates (`createAgentAt` renamed to `createAgentAtIndex`).
*   **Agent Encapsulation:** `IAgent` extends `IDelegatingShape`. Modifying attributes via standard container methods is restricted in favor of proper variable evaluation.

### Language Innovations (GAML)
*   **Object-Oriented Constructs:** Introduction of the **`class`** and **`object`** concepts into the GAML language, bringing robust object-oriented paradigms to model building.
*   **Faster Compilation:** The Xtext parser and AST (Abstract Syntax Tree) have been completely redesigned with formal syntactic elements, resulting in vastly faster compilation times.
*   **Cleaner Syntax:**
    *   Removal of Arrow Braces: `action my_action -> { do something; }` is now `action my_action -> do something;`.
    *   Argument Lists (`with:`): `[a::1]` replaced by `(a:1)`.
    *   Keyword Deprecation: `diffuse var: heat` is now `diffuse heat`; `transition to: x` is now `transition x`.
*   **Annotation Flattening:** Java Annotations used for GAML (`@operator`, `@action`, `@species`) are flattened into top-level interfaces in the `gama.annotations` package.

### Engine, UI, and Graphics
*   **OpenGL 4.1 Native Display Plugin:** Introduction of `gama.ui.display.opengl4`, the first display plugin completely based on the modern OpenGL 4.1 Core Profile architecture. It abandons legacy fixed-function pipelines in favor of Shaders, VAOs/VBOs, and the JOML library.
*   **New Launching Overlay:** A redesigned and highly responsive launching overlay appears when preparing and starting experiments.
*   **Enhanced Concurrency:** Massive improvements to concurrency support, both internally for the simulation engine and externally for parallel execution of GAML operations.

---

# Part 1: Innovations in GAMA and GAML (Version 2026)

GAMA 2026 represents one of the most significant architectural overhauls in the history of the platform. More than just an incremental update, it is a foundational leap designed to secure GAMA’s future, enhance the modeler’s experience, and provide a robust, modern API for plugin developers.

## 1. Technological Leap: JDK 25 and Concurrency

At its core, GAMA 2026 abandons legacy Java constraints by upgrading directly to **Java Development Kit (JDK) 25** and the **Eclipse 2025-12** Rich Client Platform.

### What this brings to the platform:
*   **Pattern Matching & Modern Java:** Code readability is massively improved. The engine relies heavily on Pattern Matching for `instanceof` (e.g., `if (obj instanceof MyClass myVar) { ... }`), Records, and Switch Expressions, stripping away thousands of lines of boilerplate casting.
*   **Much Better Concurrency Support:** The shift to modern Java has enabled the engine to implement vastly superior concurrency models. Internal threading mechanisms for the platform and the scheduler have been optimized. Furthermore, GAML parallel operations (e.g., parallel lists, parallel asks) now execute with significantly lower overhead and better thread pooling, leveraging JDK 25's advanced thread management.

## 2. Introducing Object-Oriented GAML: `class` and `object`

For the first time, GAMA introduces native support for **`class`** and **`object`** structures directly within the GAML language.

While GAMA has always been Agent-based (where species define agents with complex lifecycles, shapes, and locations in topologies), the introduction of `class` provides a lightweight, purely data-and-logic structure. Classes allow modelers to define complex, nested data structures and utility objects without incurring the overhead of a full simulation agent (no geometry, no automatic scheduling). This closes a major gap for developers writing complex, software-engineering-heavy models in GAML.

## 3. The Language Evolution: A Cleaner, Faster GAML

The Xtext grammar definition (`Gaml.xtext`) has been entirely extracted from the compiler into its own dedicated plugin (`gaml.grammar`). This separation of concerns allowed for massive optimization of the parsing rules and code generators.

Furthermore, the GAML syntax has been refined to be more intuitive, concise, and aligned with modern programming aesthetics. Furthermore, the underlying Xtext compiler has been rewritten with a strict AST.

*   **Faster Compilation:** The rigid formalization of the AST (`SyntacticAttributeElement`, `SyntacticClassElement`) and optimized parsing rules mean that large models compile and initialize significantly faster than in previous versions.
*   **Streamlined Parameter Passing (`with:`):** The legacy syntax `with: [a::1, b::2]` is replaced by a cleaner, JSON-like syntax: **`with: (a:1, b:2)`**.
*   **Flattening of Arrow Functions (`->`):** The parser now handles the arrow operator without requiring the outermost braces: **`action my_action -> do something;`**.
*   **Removal of Redundant Keywords:**
    *   `diffuse var: heat` is now simply **`diffuse heat`**.
    *   `transition to: next_state` is now just **`transition next_state`**.
*   **Separating Identifiers from Titles:** Displays and experiments enforce a clear separation between the programmatic identifier and the human-readable UI title: **`display My_Chart title: "My Chart"`**.
*   **Contextual Clarity:** The display layer used for rendering images has been renamed from `image` to **`picture`**.

## 4. Graphics Revolution: The OpenGL 4.1 Display Plugin

GAMA 2026 ships with a brand new, highly optimized display plugin: **`gama.ui.display.opengl4`**.

This is the first display plugin built from the ground up on the **OpenGL 4.1 Core Profile**. It completely abandons the legacy OpenGL 2 fixed-function pipeline (removing `glBegin`/`glEnd`, `glMatrixMode`, etc.).
*   **Modern Rendering:** All rendering is now based exclusively on Shaders (loaded from `.vert` and `.frag` files), Vertex Array Objects (VAOs), and Vertex Buffer Objects (VBOs).
*   **JOML Math Library:** The legacy matrix stack has been replaced by the high-performance Java OpenGL Math Library (JOML).
*   **Performance:** This architecture ensures extremely high execution speed, massive polygon throughput, and significantly lower GPU memory usage compared to previous renderers.

## 5. UI Improvements: The Launching Overlay

The user experience during model execution has been smoothed out with the introduction of a **new launching overlay**. When an experiment is launched, a highly responsive UI overlay takes over to display compilation, agent initialization, and setup progress. This prevents UI lock-ups and provides clear feedback to the modeler before the simulation officially begins stepping.

## 6. The Architectural Revolution: `gama.api` and Factories

GAMA 2026 introduces **`gama.api`**, an isolated module that serves as the definitive public contract for the platform.

*   **True Encapsulation:** Implementation details are hidden in `gama.core`. Plugin developers rely solely on interfaces (`IPoint`, `IAgent`, `IList`).
*   **The Factory Pattern Generalization:** Direct instantiation of core types (e.g., `new GamaList()`) is strictly prohibited. Everything is created via Factories (`GamaListFactory.create()`). This allows the GAMA kernel absolute control over memory allocation, object pooling, and optimization.
*   **Reinventing `IAgent`:** The agent interface delegates geometry handling to specialized components (`IDelegatingShape`) and deprecates direct attribute mutation via containers in favor of robust GAML variable access.
*   **Decentralizing `Cast`:** The monolithic `Cast` utility class has been broken down. Type casting is now handled intelligently by the target `IType` implementations.


***

# Part 2: Comprehensive Migration Guide

This section provides an exhaustive reference of the API, architectural, and syntax changes.

## 1. GAML Language Syntax Changes

Existing `.gaml` models will need to be updated to comply with the new grammar.

### 1.1 Flattening of Arrow (`->`) Braces
The outermost curly braces surrounding the body of `->` (arrow) expressions are removed.
*   **Before:** `action my_action -> { do something; };`
*   **After:** `action my_action -> do something;`

### 1.2 Deprecation of `diffuse var:` and `transition to:`
*   **Before:** `diffuse var: heat`
*   **After:** `diffuse heat`
*   **Before:** `transition to: idle`
*   **After:** `transition idle`

### 1.3 Renaming `image` to `picture` in Displays
*   **Before:** `display my_disp { image "background.jpg"; }`
*   **After:** `display my_disp { picture "background.jpg"; }`

### 1.4 Explicit `title:` for Displays and Experiments
*   **Before:** `display "3 Simulations"`
*   **After:** `display _3_Simulations title: "3 Simulations"`

### 1.5 Evolution of the `with:` argument list
*   **Before:** `with: [agents::ag, values::[1,2,3]]`
*   **After:** `with: (agents:ag, values:[1,2,3])`

---

## 2. Java Annotations Flattening (`gama.annotations`)

You must update your imports from `gama.annotations.precompiler.GamlAnnotations.*` to `gama.annotations.*` for all GAML annotations.

**Exhaustive list of migrated annotations:**
`@action`, `@arg`, `@constant`, `@display`, `@doc`, `@example`, `@experiment`, `@facet`, `@facets`, `@file`, `@getter`, `@inside`, `@listener`, `@no_test`, `@operator`, `@setter`, `@skill`, `@species`, `@symbol`, `@test`, `@type`, `@usage`, `@variable`, `@vars`, `@factory`.

**Example:**
*   **Before:** `@gama.annotations.precompiler.GamlAnnotations.operator(value = "my_operator")`
*   **After:** `@gama.annotations.operator(value = "my_operator")`

*Note: `IKeyword` has moved from `gama.core.common.interfaces.IKeyword` to `gama.annotations.constants.IKeyword`.*

---

## 3. Redesign of the `IAgent` Interface

*   **Location:** Moved from `gama.core.metamodel.agent.IAgent` to `gama.api.kernel.agent.IAgent`.
*   **Inheritance:** No longer extends `IShape` and `IAttributed`. It now extends `IDelegatingShape`.
*   **Container Interface:** Changed from `IContainer.Addressable<String, Object>` to `IContainer.ToGet<String, Object>`.
*   **Geometry:** `getLocation()` now returns `IPoint` instead of `GamaPoint`. `getGeometry()` returns `IShape`.
*   **Peers:** `getPeers()` returns a read-only list. The `setPeers()` default implementation is empty.

---

## 4. Redesign of Populations (`AbstractPopulation`)

*   **Base Class:** Common logic centralized in `gama.core.metamodel.population.AbstractPopulation`.
*   **API Updates (`IPopulation`):**
    *   `IsLiving` nested predicate: **Removed**.
    *   `createAgentAt(...)`: **Renamed** to `createAgentAtIndex(...)`.
    *   `createOneAgent(...)`: **Added** as a default method.
    *   `fireAgentsAdded`: No longer a default method.

---

## 5. Exhaustive Package Mapping: The `gama.api` Module

**Action:** Update imports systematically from `gama.core.*` to `gama.api.*`.

### 5.1 GAML Types (`gama.api.gaml.types.*`)
*   `IType`, `ITyped`, `ITypesManager`, `Signature`
*   `GamaActionType`, `GamaAgentType`, `GamaBoolType`, `GamaColorType`, `GamaContainerType`, `GamaDateType`, `GamaDirectoryType`, `GamaFieldType`, `GamaFileType`, `GamaFloatType`, `GamaFontType`, `GamaGenericAgentType`, `GamaGeometryType`, `GamaGraphType`, `GamaIntegerType`, `GamaListType`, `GamaMapType`, `GamaMatrixType`, `GamaMessageType`, `GamaMetaType`, `GamaNoType`, `GamaPairType`, `GamaPathType`, `GamaPointType`, `GamaSkillType`, `GamaSpeciesType`, `GamaStringType`, `GamaTopologyType`, `GamaType`.

### 5.2 Java Data Structures (`gama.api.types.*`)
*   **Colors:** `IColor`, `GamaColor`, `GamaColorFactory`
*   **Dates:** `IDate`, `GamaDate`, `GamaDateInterval`, `GamaDateFactory`
*   **Files:** `IGamaFile`, `GamaFile`, `GenericFile`
*   **Fonts:** `IFont`, `GamaFont`, `GamaFontFactory`
*   **Geometry:** `IShape`, `IPoint`, `IDelegatingShape`, `GamaPoint`, `GamaPointFactory`, `GamaShapeFactory`, `IShapeFactory`
*   **Graphs:** `IGraph`, `IPath`, `GraphObject`, `GamaGraphFactory`, `GamaPathFactory`
*   **Lists:** `IList`, `GamaList`, `GamaPairList`, `GamaListFactory`
*   **Maps:** `IMap`, `GamaMap`, `GamaMapFactory`
*   **Matrices:** `IMatrix`, `IField`, `GamaMatrixFactory`
*   **Messages:** `IMessage`, `GamaMessageFactory`
*   **Pairs:** `IPair`, `GamaPairFactory`
*   **Topology:** `ITopology`, `AmorphousTopology`, `GamaTopologyFactory`

### 5.3 Expressions and AST (`gama.api.gaml.*` & `gama.api.compilation.*`)
*   `IExpression`, `IStatement`
*   `IDescription`, `ISymbolDescriptionFactory`, `IExpressionFactory`, `ISyntacticFactory`

### 5.4 UI and Displays (`gama.api.ui.*`)
*   `IGui`, `IDialogFactory`, `IStatusDisplayer`, `IConsoleListener`
*   `IDisplaySurface`, `IGraphics`, `IDisplayData`, `IChart`
*   `ILayer`, `ICameraDefinition`, `IDrawingAttributes`

### 5.5 Core Utilities (`gama.api.utils.*`)
*   `FileUtils` (Centralized file management)
*   `GamaPreferences` (Preference store)
*   `StringUtils`, `JavaUtils`, `MathUtils`
*   `CsvReader`, `CsvWriter`, `IJson`, `IJsonObject`
*   `GamaRNG`, `RandomUtils`

---

## 6. Generalization of Factories for Instantiation

You must no longer use direct constructors (e.g., `new GamaList(...)`, `new GamaPoint(...)`).

**Exhaustive Migration Examples:**

*   **Lists (`IList`)**
    *   *Before:* `new GamaList<>()`
    *   *After:* `GamaListFactory.create(Types.STRING)`
*   **Maps (`IMap`)**
    *   *Before:* `new GamaMap<>()`
    *   *After:* `GamaMapFactory.create(Types.STRING, Types.INT)`
*   **Points (`IPoint`)**
    *   *Before:* `new GamaPoint(1.0, 2.0)`
    *   *After:* `GamaPointFactory.create(1.0, 2.0)`
*   **Geometries (`IShape`)**
    *   *Before:* `GamaGeometryType.buildCircle(10, pt)`
    *   *After:* `GamaShapeFactory.buildCircle(10, pt)`
*   **Colors (`IColor`)**
    *   *Before:* `new GamaColor(255, 0, 0)`
    *   *After:* `GamaColorFactory.create(255, 0, 0)`
*   **Graphs (`IGraph`)**
    *   *Before:* `new GamaSpatialGraph(...)`
    *   *After:* `GamaGraphFactory.createSpatialGraph(...)`

---

## 7. Evolution of Casting Mechanisms (The `Cast` class)

1.  **Moved to API:** Located at `gama.api.gaml.types.Cast`.
2.  **Decreased Role:** Static cast methods specific to data structures have been removed.
3.  **Delegation:** Type casting is now handled directly by the target `IType` implementations.

**Exhaustive Casting Migration Examples:**

*   **Point (`IPoint`)**
    *   *Before:* `Cast.asPoint(scope, obj)`
    *   *After:* `Types.POINT.cast(scope, obj, null, false)`
*   **List (`IList`)**
    *   *Before:* `Cast.asList(scope, obj)`
    *   *After:* `Types.LIST.cast(scope, obj, null, false)`
*   **Map (`IMap`)**
    *   *Before:* `Cast.asMap(scope, obj, copy)`
    *   *After:* `Types.MAP.cast(scope, obj, null, copy)`
*   **Geometry (`IShape`)**
    *   *Before:* `Cast.asGeometry(scope, obj)`
    *   *After:* `Types.GEOMETRY.cast(scope, obj, null, false)`
*   **Matrix (`IMatrix`)**
    *   *Before:* `Cast.asMatrix(scope, obj)`
    *   *After:* `Types.MATRIX.cast(scope, obj, null, false)`

*Note: Primitive conversions like `asInt`, `asFloat`, `asString`, and `asAgent` remain in `gama.api.gaml.types.Cast`.*
