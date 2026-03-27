# Comprehensive Migration Guide: GAMA Version 2026 (Eclipse 2025-12 and JDK 25)

This document provides a comprehensive and exhaustive reference of the API, architectural, and syntax changes introduced between the previous version of GAMA (on the `main` branch) and the 2026 version (on the `Upgrade-to-Eclipse-2025-12-and-JDK-25` branch).

The goal of this massive update is to modernize GAMA, improve compatibility with modern standards (JDK 25, Eclipse 2025-12), and cleanly separate implementation details from the public API by introducing a brand-new module: **`gama.api`**.

---

## 1. GAML Language Syntax Changes

The GAML grammar and parser have been significantly updated. Below is the exhaustive list of syntax modifications. Existing models may need to be updated.

### 1.1 Flattening of Arrow (`->`) Braces
The outermost curly braces surrounding the body of `->` (arrow) expressions are now removed by the parser.
*   **Before:** `action my_action -> { do something; };`
*   **After:** `action my_action -> do something;`

### 1.2 Deprecation of `diffuse var:` and `transition to:`
The verbose keywords `var:` and `to:` are no longer used in `diffuse` and `transition` statements.
*   **Before:** `diffuse var: heat`
*   **After:** `diffuse heat`
*   **Before:** `transition to: idle`
*   **After:** `transition idle`

### 1.3 Renaming `image` to `picture` in Displays
To avoid keyword collisions and improve clarity, the `image` layer inside `display` blocks is renamed to `picture`.
*   **Before:** `display my_disp { image "background.jpg"; }`
*   **After:** `display my_disp { picture "background.jpg"; }`

### 1.4 Explicit `title:` for Displays and Experiments
Display and experiment names declared only as strings are now rewritten to separate the identifier from the human-readable title.
*   **Before:** `display "3 Simulations"`
*   **After:** `display _3_Simulations title: "3 Simulations"`

### 1.5 Evolution of the `with:` argument list
The `with:` syntax for parameter passing has been modernized from bracket/double-colon `[::]` to parenthesis/single-colon `(:)`.
*   **Before:** `with: [agents::ag, values::[1,2,3]]`
*   **After:** `with: (agents:ag, values:[1,2,3])`

---

## 2. Java Annotations Flattening (`gama.annotations`)

The global `GamlAnnotations` class (which previously contained all the `@interface` definitions as inner classes) has been flattened. Annotations are now top-level interfaces in the `gama.annotations` package.

**Exhaustive list of migrated annotations:**
You must update your imports from `gama.annotations.precompiler.GamlAnnotations.*` to `gama.annotations.*` for the following:
`@action`, `@arg`, `@constant`, `@display`, `@doc`, `@example`, `@experiment`, `@facet`, `@facets`, `@file`, `@getter`, `@inside`, `@listener`, `@no_test`, `@operator`, `@setter`, `@skill`, `@species`, `@symbol`, `@test`, `@type`, `@usage`, `@variable`, `@vars`, `@factory`.

**Example:**
*   **Before:** `@gama.annotations.precompiler.GamlAnnotations.operator(value = "my_operator")`
*   **After:** `@gama.annotations.operator(value = "my_operator")`

Additionally, `IKeyword` has moved from `gama.core.common.interfaces.IKeyword` to `gama.annotations.constants.IKeyword`.

---

## 3. Redesign of the `IAgent` Interface

The root interface for agents, `IAgent`, has undergone profound structural modifications to enhance encapsulation.

*   **Location:** Moved from `gama.core.metamodel.agent.IAgent` to `gama.api.kernel.agent.IAgent`.
*   **Inheritance:** No longer extends `IShape` and `IAttributed`. It now extends `IDelegatingShape`.
*   **Container Interface:** Changed from `IContainer.Addressable<String, Object>` to `IContainer.ToGet<String, Object>`. Direct attribute mutation via the container is forbidden.
*   **Geometry:** Methods like `getLocation()` now return `IPoint` instead of `GamaPoint`. `getGeometry()` returns `IShape`.
*   **Peers:** The `getPeers()` method returns a conceptually read-only list. The `setPeers()` default implementation is empty.

---

## 4. Redesign of Populations (`AbstractPopulation`)

*   **Base Class:** Common population logic is now centralized in `gama.core.metamodel.population.AbstractPopulation`. `GamaPopulation` extends it.
*   **API Updates (`IPopulation`):**
    *   `IsLiving` nested predicate: **Removed**.
    *   `createAgentAt(...)`: **Renamed** to `createAgentAtIndex(...)`.
    *   `createOneAgent(...)`: **Added** as a default method.
    *   `fireAgentsAdded`: No longer a default method; delegated to implementations.

---

## 5. Exhaustive Package Mapping: The `gama.api` Module

The creation of the `gama.api` module required the relocation of hundreds of interfaces, types, and factories. Below is the exhaustive list of the most important structural migrations.

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

A major architectural shift in GAMA 2026 is the **generalization of the Factory pattern** for creating core data structures and geometries.
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

The central `Cast` class, previously located at `gama.core.gaml.operators.Cast`, handled almost all type conversions.

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

---

**Note for CI/CD developers:**
Ensure that your local build environments (Maven Tycho) and CI pipelines use **JDK 25** (or higher) and the Eclipse 2025-12 target platform to successfully compile this new version of GAMA.
