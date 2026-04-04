# GAMA 2026 — Migration Guide

> **Who is this for?** Plugin developers and contributors upgrading existing code to GAMA 2026.  
> **What's covered?** Every breaking change in Java APIs, GAML syntax, and architecture — with before/after examples throughout.

---

## Quick Navigation

| Section | Topic |
|---|---|
| [Part 0](#part-0-changelog-at-a-glance) | Changelog at a glance |
| [Part 1](#part-1-whats-new-and-why-it-matters) | What's new and why it matters |
| [Part 2](#part-2-migration-reference) | Step-by-step migration reference |

---

# Part 0: Changelog at a Glance

A high-level summary of every area that changed. Use this to quickly assess what affects your plugin.

## Core Technology & Architecture

| Change | Impact |
|---|---|
| Upgraded to **JDK 25** and **Eclipse 2026-03** | Rebuild required; modern Java syntax now available |
| New **`gama.api`** module | Public interfaces decoupled from `gama.core` implementation |
| **Factory pattern** enforced for all core types | `new GamaList()`, `new GamaPoint()` etc. are gone |
| **`Cast` utility class** reduced | Casting delegated to individual `IType` implementations |
| **`AbstractPopulation`** centralises population logic | `createAgentAt` renamed to `createAgentAtIndex` |
| **`IAgent`** now extends `IDelegatingShape` | Attribute mutation via containers is restricted |

## GAML Language

| Change | Impact |
|---|---|
| New **`class`** and **`object`** keywords | New OOP constructs available in models |
| Rewritten Xtext parser & AST | Faster compilation; some syntax constructs changed |
| Arrow braces removed | `-> { ... }` becomes `-> ...` |
| `with:` argument list syntax changed | `[a::1]` becomes `(a:1)` |
| `diffuse var:` and `transition to:` removed | Use `diffuse` and `transition` directly |
| `image` layer renamed to `picture` | Update display blocks |
| Displays/experiments need explicit `title:` | Identifier and UI label are now separate |

## Engine, UI, and Graphics

| Change | Impact |
|---|---|
| New **OpenGL 4.1** display plugin (`gama.ui.display.opengl4`) | Modern shader-based rendering; legacy OpenGL 2 pipeline removed |
| New **launching overlay** | Visual improvement only, no code changes needed |
| Improved **concurrency** support | Internal threading improved; some scheduling APIs changed |

---

# Part 1: What's New and Why It Matters

## 1. JDK 25 & Eclipse 2025-12

GAMA 2026 moves entirely to **Java 25** and **Eclipse 2025-12 RCP**. This unlocks modern language features that are now used throughout the engine:

- **Pattern matching for `instanceof`** — eliminates redundant casts:
  ```java
  // Old
  if (obj instanceof MyClass) { MyClass myVar = (MyClass) obj; ... }
  // New
  if (obj instanceof MyClass myVar) { ... }
  ```
- **Records, sealed classes, switch expressions** — used internally for cleaner data modelling.
- **Superior concurrency** — virtual threads and structured concurrency improve parallel simulation scheduling.

> **Action needed:** Recompile all plugins against JDK 25. No source changes are required solely for this upgrade, but you will benefit from updating your own code too.

---

## 2. New OOP Constructs in GAML: `class` and `object`

GAML now supports **`class`** (a lightweight, purely data-oriented structure with no lifecycle or spatial footprint) alongside the existing **`species`** (full agents with location, shape, and scheduling).

**When to use which:**

| Construct | Use when… |
|---|---|
| `species` | Agents need a location, shape, or are scheduled |
| `class` | You only need structured data (like a Java POJO) |
| `object` | You need a single instance of a `class` |

This is the first time GAML supports structured data types that are not agents — no migration is required unless you want to take advantage of this.

---

## 3. Cleaner, Faster GAML Syntax

The Xtext grammar (`Gaml.xtext`) has been extracted into its own dedicated plugin (`gaml.grammar`) and the AST has been formalised. The result is significantly faster model compilation.

Several syntax shortcuts have also been cleaned up. See [Part 2, Section 1](#1-gaml-language-syntax-changes) for the full before/after reference.

---

## 4. New OpenGL 4.1 Display Plugin

The new **`gama.ui.display.opengl4`** plugin is a ground-up rewrite of the rendering engine, built on the **OpenGL 4.1 Core Profile**:

- All rendering now uses **shaders** (`.vert`/`.frag` files), **VAOs**, and **VBOs**.
- The legacy OpenGL 2 fixed-function pipeline (`glBegin`/`glEnd`, `glMatrix*`) is gone.
- The **JOML** library replaces the legacy matrix stack.
- Result: higher frame rates, better polygon throughput, lower GPU memory usage.

> **Action needed for display plugin authors:** Rendering code using the legacy OpenGL 2 API must be rewritten against the new shader-based pipeline.

---

## 5. New Architecture: the `gama.api` Module

The single biggest architectural change is the introduction of **`gama.api`** — a dedicated module containing only public interfaces and contracts, with zero implementation code.

**Before (GAMA 2025 and earlier):**
- Public interfaces and their implementations lived side by side in `gama.core`.
- Plugin developers ended up importing implementation classes directly.

**After (GAMA 2026):**
- `gama.api` contains only interfaces (`IAgent`, `IPoint`, `IList`, …).
- `gama.core` contains the implementations.
- Plugin developers **only depend on `gama.api`**.

This makes the platform significantly more stable: implementation classes can change freely without breaking plugin code.

See [Part 2, Section 5](#5-package-mapping-gamaapi-module) for the full import mapping.

---

# Part 2: Migration Reference

Work through each section that applies to your plugin. Each section is self-contained.

---

## 1. GAML Language Syntax Changes

These changes affect `.gaml` model files. Most can be applied with a global find-and-replace.

### 1.1 Arrow (`->`) expressions — braces removed

The outer curly braces around the body of `->` expressions are no longer valid.

```gaml
// Before
action my_action -> { do something; };

// After
action my_action -> do something;
```

### 1.2 `diffuse var:` and `transition to:` — keyword removed

The `var:` and `to:` facets were redundant and have been removed.

```gaml
// Before
diffuse var: heat;
transition to: idle;

// After
diffuse heat;
transition idle;
```

### 1.3 `image` display layer → `picture`

The `image` keyword inside `display` blocks has been renamed to `picture` to better reflect its purpose and avoid confusion with the `image` type.

```gaml
// Before
display my_disp {
    image "background.jpg";
}

// After
display my_disp {
    picture "background.jpg";
}
```

### 1.4 Display and experiment identifiers — explicit `title:` required

Displays and experiments now enforce a strict separation between the **programmatic identifier** (used in code) and the **UI label** (shown to the user).

```gaml
// Before
display "3 Simulations" { ... }

// After
display _3_Simulations title: "3 Simulations" { ... }
```

> **Rule of thumb:** The name immediately after `display` or `experiment` must now be a valid identifier (no spaces, no special characters). Use `title:` for the human-readable label.

### 1.5 `with:` argument list syntax

The map-literal syntax for argument passing has been replaced by a cleaner parenthesised syntax.

```gaml
// Before
do my_action with: [agents::ag, values::[1, 2, 3]];

// After
do my_action with: (agents: ag, values: [1, 2, 3]);
```

**Key differences:**
- Outer `[...]` → `(...)`
- Double-colon `::` → single colon `:`

---

## 2. Java Annotations — Import Path Changed

All GAML-related Java annotations have moved to **top-level** interfaces in `gama.annotations`. Update your imports accordingly.

```java
// Before
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.skill;

// After
import gama.annotations.operator;
import gama.annotations.skill;
```

**Complete list of affected annotations:**

`@action`, `@arg`, `@constant`, `@display`, `@doc`, `@example`, `@experiment`, `@facet`, `@facets`, `@file`, `@getter`, `@inside`, `@listener`, `@no_test`, `@operator`, `@setter`, `@skill`, `@species`, `@symbol`, `@test`, `@type`, `@usage`, `@variable`, `@vars`

**Also note:** `IKeyword` has moved:

```java
// Before
import gama.core.common.interfaces.IKeyword;

// After
import gama.annotations.constants.IKeyword;
```

---

## 3. `IAgent` Interface Changes

`IAgent` has been redesigned and moved to the `gama.api` module.

### What changed

| Aspect | Before | After |
|---|---|---|
| Package | `gama.core.metamodel.agent.IAgent` | `gama.api.kernel.agent.IAgent` |
| Extends | `IShape`, `IAttributed` | `IDelegatingShape` |
| Container type | `IContainer.Addressable<String, Object>` | `IContainer.ToGet<String, Object>` |
| `getLocation()` return type | `GamaPoint` | `IPoint` |
| `getGeometry()` return type | `GamaShape` | `IShape` |
| `getPeers()` | Mutable list | Read-only list |
| `setPeers()` | Concrete implementation | Empty default method |

### Practical impact

- **Do not mutate agent attributes through the container interface.** Use proper variable evaluation via `scope.evaluate(expression, agent)`.
- **Geometry access is now interface-typed.** If you need a concrete `GamaPoint`, cast explicitly — but prefer working with `IPoint`.

---

## 4. Population API Changes (`AbstractPopulation`)

### New base class

All population implementations should now extend `gama.core.metamodel.population.AbstractPopulation`, which centralises common logic that was previously duplicated.

### `IPopulation` API changes

| Member | Change |
|---|---|
| `IsLiving` (nested predicate class) | **Removed** — inline the predicate in your own code |
| `createAgentAt(...)` | **Renamed** to `createAgentAtIndex(...)` |
| `createOneAgent(...)` | **Added** as a default method |
| `fireAgentsAdded(...)` | No longer a default method — must be implemented explicitly |

---

## 5. Package Mapping: `gama.api` Module

> **General rule:** Replace `gama.core` with `gama.api` in your import statements, then fix any remaining errors using the detail tables below.

### 5.1 GAML Types → `gama.api.gaml.types.*`

`IType`, `ITyped`, `ITypesManager`, `Signature`, `GamaActionType`, `GamaAgentType`, `GamaBoolType`, `GamaColorType`, `GamaContainerType`, `GamaDateType`, `GamaDirectoryType`, `GamaFieldType`, `GamaFileType`, `GamaFloatType`, `GamaFontType`, and all other `Gama*Type` classes.

### 5.2 Java Data Structures → `gama.api.types.*`

| Category | Interfaces & Classes |
|---|---|
| Colors | `IColor`, `GamaColor`, `GamaColorFactory` |
| Dates | `IDate`, `GamaDate`, `GamaDateInterval`, `GamaDateFactory` |
| Files | `IGamaFile`, `GamaFile`, `GenericFile` |
| Fonts | `IFont`, `GamaFont`, `GamaFontFactory` |
| Geometry | `IShape`, `IPoint`, `IDelegatingShape`, `GamaPoint`, `GamaPointFactory`, `GamaShapeFactory`, `IShapeFactory` |
| Graphs | `IGraph`, `IPath`, `GraphObject`, `GamaGraphFactory`, `GamaPathFactory` |
| Lists | `IList`, `GamaList`, `GamaPairList`, `GamaListFactory` |
| Maps | `IMap`, `GamaMap`, `GamaMapFactory` |
| Matrices | `IMatrix`, `IField`, `GamaMatrixFactory` |
| Messages | `IMessage`, `GamaMessageFactory` |
| Pairs | `IPair`, `GamaPairFactory` |
| Topology | `ITopology`, `AmorphousTopology`, `GamaTopologyFactory` |

### 5.3 Expressions and AST → `gama.api.gaml.*` & `gama.api.compilation.*`

`IExpression`, `IStatement`, `IDescription`, `ISymbolDescriptionFactory`, `IExpressionFactory`, `ISyntacticFactory`

### 5.4 UI and Displays → `gama.api.ui.*`

`IGui`, `IDialogFactory`, `IStatusDisplayer`, `IConsoleListener`, `IDisplaySurface`, `IGraphics`, `IDisplayData`, `IChart`, `ILayer`, `ICameraDefinition`, `IDrawingAttributes`

### 5.5 Core Utilities → `gama.api.utils.*`

`FileUtils`, `GamaPreferences`, `StringUtils`, `JavaUtils`, `MathUtils`, `CsvReader`, `CsvWriter`, `IJson`, `IJsonObject`, `GamaRNG`, `RandomUtils`

---

## 6. Factory Pattern — Replace Direct Constructors

Direct instantiation of core GAMA types is **no longer allowed**. You must use the provided factory classes.

> **Why?** Factories enforce correct type metadata and scope-awareness that constructors cannot guarantee.

| Type | Before | After |
|---|---|---|
| `IList` | `new GamaList<>()` | `GamaListFactory.create(Types.STRING)` |
| `IMap` | `new GamaMap<>()` | `GamaMapFactory.create(Types.STRING, Types.INT)` |
| `IPoint` | `new GamaPoint(1.0, 2.0)` | `GamaPointFactory.create(1.0, 2.0)` |
| `IShape` (circle) | `GamaGeometryType.buildCircle(10, pt)` | `GamaShapeFactory.buildCircle(10, pt)` |
| `IColor` | `new GamaColor(255, 0, 0)` | `GamaColorFactory.create(255, 0, 0)` |
| `IGraph` | `new GamaSpatialGraph(...)` | `GamaGraphFactory.createSpatialGraph(...)` |

**Note:** When creating typed collections, always provide the element type(s) using constants from the `Types` class (e.g. `Types.STRING`, `Types.INT`, `Types.AGENT`).

---

## 7. Casting — From `Cast` to `IType`

The static `Cast` utility class has been significantly reduced. Type-specific casting methods have been removed and are now handled by the `IType` implementations directly.

> **What remains in `Cast`:** Primitive conversions — `asInt`, `asFloat`, `asString`, `asAgent` — are still available at `gama.api.gaml.types.Cast`.

| Type | Before | After |
|---|---|---|
| `IPoint` | `Cast.asPoint(scope, obj)` | `Types.POINT.cast(scope, obj, null, false)` |
| `IList` | `Cast.asList(scope, obj)` | `Types.LIST.cast(scope, obj, null, false)` |
| `IMap` | `Cast.asMap(scope, obj, copy)` | `Types.MAP.cast(scope, obj, null, copy)` |
| `IShape` | `Cast.asGeometry(scope, obj)` | `Types.GEOMETRY.cast(scope, obj, null, false)` |
| `IMatrix` | `Cast.asMatrix(scope, obj)` | `Types.MATRIX.cast(scope, obj, null, false)` |

**Parameter guide for `IType.cast(scope, obj, param, copy)`:**

| Parameter | Meaning |
|---|---|
| `scope` | The current execution scope |
| `obj` | The object to cast |
| `param` | An optional type parameter (usually `null`) |
| `copy` | `true` to return a copy, `false` to allow returning the original |
