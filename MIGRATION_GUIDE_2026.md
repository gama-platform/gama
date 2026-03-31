# GAMA Version 2026: Innovations and Comprehensive Migration Guide

This document is divided into two major sections. The **first part** details the profound innovations, technological leaps, and grammatical evolutions introduced in GAMA Version 2026 (branch `Upgrade-to-Eclipse-2025-12-and-JDK-25`). The **second part** serves as an exhaustive, technical migration reference guide for developers and modelers transitioning from previous versions.

---

# Part 1: Innovations in GAMA and GAML (Version 2026)

GAMA 2026 represents one of the most significant architectural overhauls in the history of the platform. More than just an incremental update, it is a foundational leap designed to secure GAMA’s future, enhance the modeler’s experience, and provide a robust, modern API for plugin developers. The modifications span across over 10,000 files, touching every aspect of the engine, the language, and the user interface.

## 1. Technological Leap: Embracing JDK 25 and Eclipse 2025-12

At its core, GAMA 2026 abandons legacy Java constraints by upgrading directly to **Java Development Kit (JDK) 25** and the **Eclipse 2025-12** Rich Client Platform.

### What this brings to the platform:
*   **Performance and Memory Management:** JDK 25 introduces cutting-edge garbage collection optimizations, improved thread management, and underlying enhancements that directly translate to faster simulation execution times and a reduced memory footprint for massive agent-based models.
*   **Modern Java Features:** For plugin developers, this means the ability to use modern Java paradigms. The codebase has been extensively refactored to utilize features like **Pattern Matching for `instanceof`** (e.g., `if (obj instanceof MyClass myVar) { ... }`), Records, and Switch Expressions. This drastically reduces boilerplate code, minimizes casting errors, and improves code readability across the entire platform.
*   **Future-Proofing the IDE:** By aligning with the upcoming Eclipse 2025-12 release, GAMA ensures continued support for the latest OS updates, high-DPI displays, and modern UI rendering engines.

## 2. The Language Evolution: A Cleaner, Modern GAML

The GAML language (GAMA Modeling Language) has been refined to be more intuitive, concise, and aligned with modern programming aesthetics. The goal was to remove unnecessary verbosity and "boilerplate" syntax that hindered readability without adding semantic value.

### Key Grammatical Innovations:
*   **Streamlined Parameter Passing (`with:`):** The legacy syntax for passing arguments to actions or creating agents involved clunky brackets and double colons (e.g., `with: [a::1, b::2]`). GAMA 2026 introduces a much cleaner, JSON-like syntax using parentheses and single colons: **`with: (a:1, b:2)`**. This makes model code significantly easier to read and write.
*   **Flattening of Arrow Functions (`->`):** Previously, defining inline actions or evaluations required surrounding the body in curly braces: `action my_action -> { do something; };`. The parser now intelligently handles the arrow operator without requiring the outermost braces: **`action my_action -> do something;`**. This functional programming style allows for highly expressive, one-line declarations.
*   **Removal of Redundant Keywords:**
    *   The `diffuse` statement no longer requires the `var:` keyword. `diffuse var: heat` is now simply **`diffuse heat`**.
    *   The `transition` statement inside state machines no longer uses `to:`. `transition to: next_state` is now just **`transition next_state`**.
*   **Separating Identifiers from Titles:** In previous versions, displays and experiments could be named using a string (e.g., `display "My Chart"`), which internally acted as an awkward identifier. GAMA 2026 enforces a clear separation between the programmatic identifier and the human-readable UI title: **`display My_Chart title: "My Chart"`**. This eliminates internal parsing ambiguities and bugs related to special characters in names.
*   **Contextual Clarity (`image` vs. `picture`):** To avoid keyword collisions and confusion, the display layer used for rendering images has been renamed from `image` to **`picture`**.

## 3. The Architectural Revolution: The `gama.api` Module

For years, GAMA extension developers struggled with a monolithic `gama.core` module where public interfaces were entangled with internal implementations.

GAMA 2026 introduces **`gama.api`**, a completely new, isolated module that serves as the definitive public contract for the platform.

### Why this is a game-changer:
*   **True Encapsulation:** Implementation details are now hidden. Plugin developers rely solely on interfaces (`IPoint`, `IAgent`, `IList`, `IGraph`) located in `gama.api`. This guarantees that internal optimizations in `gama.core` will not break third-party plugins in the future.
*   **Interface-Driven Design:** Methods across the platform now strictly return interfaces rather than concrete classes. For instance, `agent.getLocation()` returns an `IPoint` rather than a `GamaPoint`. This allows the engine to swap out underlying implementations (e.g., swapping a standard point for a highly optimized, memory-mapped spatial coordinate) without the modeler or plugin developer ever noticing.

## 4. Reinventing the Core: `IAgent`, `IPopulation`, and Factories

Under the hood, the fundamental building blocks of a simulation have been redesigned for safety, speed, and consistency.

*   **The `IAgent` Interface Redefined:** The agent interface has been stripped of direct shape inheritance and mutable container properties. It now extends `IDelegatingShape`, delegating geometry handling to specialized, swappable components. Furthermore, direct attribute mutation via the container interface is deprecated; attributes must be accessed via robust GAML variables, preventing untracked state changes.
*   **Population Centralization:** The logic that manages collections of agents has been centralized into an `AbstractPopulation` base class. This DRY (Don't Repeat Yourself) approach eliminates inconsistencies between grid populations, standard agent populations, and graph-based populations, making the engine much easier to maintain.
*   **The Factory Pattern Generalization:** To support the `gama.api` encapsulation, the direct instantiation of core types using `new` (e.g., `new GamaList()`) is strictly prohibited. Everything is now created via Factories (`GamaListFactory.create()`, `GamaShapeFactory.buildCircle()`). This is a crucial innovation: it gives the GAMA kernel absolute control over memory allocation, allowing it to pool objects, reuse memory, and optimize data structures transparently based on the simulation's needs.

## 5. Cleaning the Utilities and Annotations

*   **Flattening Annotations:** The GAML annotation framework (used by developers to define `@operator`, `@species`, `@action`) was previously nested inside a massive `GamlAnnotations` class. In 2026, these are now elegant, top-level interfaces directly in the `gama.annotations` package, greatly cleaning up Java imports and class definitions.
*   **The Fall of the Monolithic `Cast` Class:** Type casting in Java plugins used to be routed through a single, gigantic `Cast` utility class. This created a massive bottleneck and architectural dependency nightmare. In 2026, casting logic has been decentralized. Each `IType` implementation now knows how to cast objects to itself (e.g., `Types.POINT.cast(...)`), adhering to solid object-oriented design principles and improving extensibility.

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
