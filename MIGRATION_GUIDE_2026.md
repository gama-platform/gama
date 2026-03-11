# Migration Guide: GAMA Version 2026 (Eclipse 2025-12 and JDK 25)

This document describes the major API and architectural changes introduced between the previous version of GAMA (on the `main` branch) and the 2026 version (on the `Upgrade-to-Eclipse-2025-12-and-JDK-25` branch).

The goal of this massive update (over 10,000 files modified) is to modernize GAMA, improve compatibility with modern standards (JDK 25, Eclipse 2025-12), and cleanly separate implementation details from the public API by introducing a brand-new module: **`gama.api`**.

---

## 1. Module Changes and Imports (The `gama.api` Module)

The most impactful change for GAMA extension developers is the creation of the `gama.api` module. Previously, public interfaces, types, and utilities were mixed with implementation code inside `gama.core` and `gama.annotations`.

**Required Action:** Most of your imports pointing to `gama.core.*` or `gama.annotations.*` will need to be systematically updated to `gama.api.*`.

### Package Correspondences: Core Types and Utilities

The basic GAML types and their managers have moved:
*   `gama.core.common.interfaces.ITyped` $\rightarrow$ `gama.api.gaml.types.ITyped`
*   `gama.gaml.types.IType` $\rightarrow$ `gama.api.gaml.types.IType`
*   `gama.gaml.types.ITypesManager` $\rightarrow$ `gama.api.gaml.types.ITypesManager`

*All the concrete GAML type definitions have been migrated.* For example:
*   `gama.gaml.types.GamaPointType` $\rightarrow$ `gama.api.gaml.types.GamaPointType`
*   `gama.gaml.types.GamaListType` $\rightarrow$ `gama.api.gaml.types.GamaListType`
*   `gama.gaml.types.GamaMatrixType` $\rightarrow$ `gama.api.gaml.types.GamaMatrixType`
*   *And similarly for GamaActionType, GamaAgentType, GamaBoolType, GamaColorType, GamaFloatType, GamaGeometryType, GamaStringType, etc.*

### Package Correspondences: Data Structures
The actual Java interfaces and classes representing GAMA structures have been grouped under `gama.api.types.*`:
*   `gama.core.util.IList` $\rightarrow$ `gama.api.types.list.IList`
*   `gama.core.util.GamaList` $\rightarrow$ `gama.api.types.list.GamaList`
*   `gama.core.util.GamaMap` $\rightarrow$ `gama.api.types.map.GamaMap`
*   `gama.core.util.graph.IGraph` $\rightarrow$ `gama.api.types.graph.IGraph`
*   `gama.core.metamodel.topology.ITopology` $\rightarrow$ `gama.api.types.topology.ITopology`
*   `gama.core.metamodel.shape.GamaPoint` $\rightarrow$ `gama.api.types.geometry.GamaPoint`

---

## 2. Redesign of the `IAgent` Interface

The root interface for agents, `IAgent`, has undergone profound structural modifications to enhance encapsulation.

1.  **Package Change:**
    *   *Before:* `gama.core.metamodel.agent.IAgent`
    *   *After:* `gama.api.kernel.agent.IAgent`
2.  **Inheritance and Geometry:**
    *   `IAgent` no longer directly extends `IShape` and `IAttributed`.
    *   It now extends **`IDelegatingShape`**, allowing a cleaner separation of concerns regarding the agent's geometry.
3.  **Using Geometric Interfaces instead of Concrete Classes:**
    *   Methods like `getLocation()` and `setLocation(...)` now return and accept the `IPoint` interface rather than the concrete `GamaPoint` class.
    *   **Systematic Migration Example:**
        ```java
        // BEFORE (GAMA 1.9 / main)
        GamaPoint location = agent.getLocation();
        agent.setLocation(new GamaPoint(10, 20));
        IShape geometry = agent.getGeometry();

        // AFTER (GAMA 2026)
        IPoint location = agent.getLocation();
        agent.setLocation(GamaPointFactory.create(10, 20));
        IShape geometry = agent.getGeometry(); // IShape interface is now in gama.api.types.geometry.IShape
        ```
4.  **Attribute Management and Containers:**
    *   `IAgent` no longer extends `IContainer.Addressable<String, Object>`.
    *   It now extends `IContainer.ToGet<String, Object>`. Direct attribute modification via the container interface is no longer supported; developers must use GAML variable mechanisms (`IVariable`) or `IVarAndActionSupport`.
5.  **The `peers` Property:**
    *   The list of peer agents (`getPeers()`) is now conceptually **read-only**.
    *   The default implementation of the `setPeers(IList<IAgent> peers)` method in `IAgent` is now empty. Do not rely on this method to modify an agent's neighborhood.

---

## 3. Generalization of Factories for Instantiation

A major architectural shift in GAMA 2026 is the **generalization of the Factory pattern** for creating core data structures and geometries.
You should no longer use direct constructors (e.g., `new GamaList(...)`, `new GamaMap(...)`, `new GamaPoint(...)`) to create instances. This hides the internal implementations behind the `gama.api` and allows GAMA to inject optimized underlying structures.

**Systematic Migration Examples:**

*   **Creating Lists (`IList`)**
    ```java
    // BEFORE
    IList<String> list = new GamaList<>();
    IList<IAgent> listFromSet = new GamaList<>(mySet);

    // AFTER
    IList<String> list = GamaListFactory.create(Types.STRING);
    IList<IAgent> listFromSet = GamaListFactory.create(scope, Types.AGENT, mySet);
    ```

*   **Creating Maps (`IMap`)**
    ```java
    // BEFORE
    IMap<String, Object> map = new GamaMap<>();

    // AFTER
    IMap<String, Object> map = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
    ```

*   **Creating Geometry and Points (`IShape`, `IPoint`)**
    ```java
    // BEFORE
    GamaPoint pt = new GamaPoint(1.0, 2.0);
    IShape circle = GamaGeometryType.buildCircle(10, pt);

    // AFTER
    IPoint pt = GamaPointFactory.create(1.0, 2.0);
    IShape circle = GamaShapeFactory.buildCircle(10, pt);
    ```

*   **Creating Graphs (`IGraph`)**
    *   Use `GamaGraphFactory` to instantiate spatial graphs, generic graphs, etc.

*   **Creating Colors (`IColor`)**
    *   Use `GamaColorFactory` instead of `new GamaColor()`.

---

## 4. Redesign of Populations (`IPopulation` and `AbstractPopulation`)

The internal management of agent populations has been refactored to reduce code duplication and clarify the API.

1.  **Introduction of `AbstractPopulation`:**
    *   Common logic (internal agent container, mirror management, topology, `init`, and `step`) has been extracted into a new abstract base class: `gama.core.metamodel.population.AbstractPopulation`.
    *   `GamaPopulation` has been refactored to extend this abstract class.
2.  **Changes in the `IPopulation` API:**
    *   **Removed:** The nested `IsLiving` predicate has been removed.
    *   **Renamed:** The `createAgentAt(...)` method is renamed to `createAgentAtIndex(...)`. *All existing calls must be updated.*
    *   **Added:** A default `createOneAgent(...)` method has been added.
    *   **Behavior Change:** The `fireAgentsAdded` method is no longer a default method in the interface; its implementation is delegated to concrete classes.
    *   **Systematic Migration Example:**
        ```java
        // BEFORE
        myPopulation.createAgentAt(scope, index, initialValues, isRestored);

        // AFTER
        myPopulation.createAgentAtIndex(scope, index, initialValues, isRestored);
        ```

---

## 5. GAML Compiler and Parser (`gaml.compiler`)

Major changes occurred in the GAML Xtext compiler.

1.  **Strictly Typed AST (Abstract Syntax Tree):**
    *   Introduction of formal syntactic elements such as `SyntacticAttributeElement`, `SyntacticSpeciesElement`, `SyntacticModelElement`, etc., replacing generic AST node structures.
2.  **Descriptions and Expressions:**
    *   Factories (`DescriptionFactory`) and description objects (like `SpeciesDescription`, `ModelDescription`, `StatementDescription`) have been significantly overhauled.
    *   *Expression Interfaces moved:*
        *   `gama.gaml.expressions.IExpression` $\rightarrow$ `gama.api.gaml.expressions.IExpression`
        *   `gama.gaml.statements.IStatement` $\rightarrow$ `gama.api.gaml.statements.IStatement`
3.  **Resources and GLSL Shaders:**
    *   GLSL shaders (used in the `gama.ui.display.opengl4` plugin) are no longer stored as String literals within the Java code.
    *   They must be placed in dedicated `.vert` and `.frag` files within a `glsl` subfolder of the plugin and loaded dynamically using `getClass().getResourceAsStream()`.

---

## 6. Utilities and I/O (`gama.api.utils.*`)

File management and utility libraries have been streamlined and isolated within `gama.api.utils`.

*   **Refactoring `FileUtils`:** I/O methods have been centralized. The use of scattered legacy utilities must be replaced by the centralized API in `gama.api.utils.files.FileUtils`.
*   **General Tools:** Many utilities (`GamaPreferences`, `StringUtils`, `CSV` and `JSON` manipulators, and random generators like `GamaRNG`) have been moved to `gama.api.utils.*`.
*   **Java Pattern Matching:** The use of Java's pattern matching for `instanceof` is now the standard across the platform (introduced with the JDK migration), which greatly simplifies type checking in implementations (`if (obj instanceof MyClass myVar) { ... }`).

---

**Note for CI/CD developers:**
Ensure that your local build environments (Maven Tycho) and CI pipelines use **JDK 25** (or higher) and the Eclipse 2025-12 target platform to successfully compile this new version of GAMA.

## 7. Evolution of Casting Mechanisms (The `Cast` class)

The central `Cast` class, which was previously a massive utility class in `gama.core.gaml.operators.Cast` handling almost all type conversions in Java, has been refactored.

1.  **Moved to API:** The class is now located at `gama.api.gaml.types.Cast`.
2.  **Decreased Role:** Its role has been significantly decreased. Many static cast methods specific to data structures (e.g., `asPoint`, `asList`, `asMap`, `asMatrix`, `asGeometry`) have been **removed** from the `Cast` class.
3.  **Delegation to Types and Factories:** Type casting and conversions are now handled directly by the static methods within the target `IType` implementations or via the corresponding `Factory`.

**Systematic Migration Examples for Casting:**

*   **Casting to a Point (`IPoint`)**
    ```java
    // BEFORE
    import gama.gaml.operators.Cast;
    GamaPoint p = Cast.asPoint(scope, someObject);

    // AFTER
    import gama.api.gaml.types.Types;
    IPoint p = Types.POINT.cast(scope, someObject, null, false);
    ```

*   **Casting to a List (`IList`)**
    ```java
    // BEFORE
    IList list = Cast.asList(scope, someObject);

    // AFTER
    IList list = Types.LIST.cast(scope, someObject, null, false);
    ```

*   **Casting to a Geometry (`IShape`)**
    ```java
    // BEFORE
    IShape shape = Cast.asGeometry(scope, someObject);

    // AFTER
    IShape shape = Types.GEOMETRY.cast(scope, someObject, null, false);
    ```

*Note: Some very generic casts or base type conversions (like `asInt`, `asFloat`, `asString`, `asAgent`) are still present in the new `gama.api.gaml.types.Cast` utility, but for structured types, always prefer using `Types.YOUR_TYPE.cast(...)`.*


## 8. Flattening of GAML Annotations (`gama.annotations`)

A significant syntactic change affects all GAMA extension and plugin developers: the global `GamlAnnotations` class (which previously contained all the `@interface` definitions as inner classes) has been flattened.

Annotations are now top-level interfaces in the `gama.annotations` package.

**Required Action:** You must update your imports and the way you declare annotations on your Java classes and methods.

**Systematic Migration Examples for Annotations:**

*   **Operator Definition**
    ```java
    // BEFORE
    import gama.annotations.precompiler.GamlAnnotations.operator;
    import gama.annotations.precompiler.GamlAnnotations.doc;

    @operator(value = "my_operator", can_be_const = true)
    @doc("Returns something")
    public Object myOperator(...) { ... }

    // AFTER
    import gama.annotations.operator;
    import gama.annotations.doc;

    @operator(value = "my_operator", can_be_const = true)
    @doc("Returns something")
    public Object myOperator(...) { ... }
    ```

*   **Action Definition**
    ```java
    // BEFORE
    import gama.annotations.precompiler.GamlAnnotations.action;

    @action(name = "my_action")
    public Object myAction(...) { ... }

    // AFTER
    import gama.annotations.action;

    @action(name = "my_action")
    public Object myAction(...) { ... }
    ```

*This applies systematically to all annotations: `@species`, `@skill`, `@getter`, `@setter`, `@variable`, `@vars`, `@type`, `@symbol`, `@display`, `@experiment`, etc.*

---

## 9. UI and Display Interfaces Extraction (`gama.api.ui`)

In alignment with the core architecture extraction, the graphical and user interface APIs have been separated from their Eclipse RCP, SWT, Java2D, or OpenGL implementations. The core interfaces now reside in the `gama.api.ui` package.

If your plugin interacts with the UI, dialogs, or display surfaces, you need to update the following references:

*   **General UI and Dialogs:**
    *   `gama.core.common.interfaces.IGui` $\rightarrow$ `gama.api.ui.IGui`
    *   `gama.api.ui.IDialogFactory` (new factory for user dialogs, file dialogs, etc.)
*   **Displays and Graphics:**
    *   `gama.core.outputs.display.IDisplaySurface` $\rightarrow$ `gama.api.ui.displays.IDisplaySurface`
    *   `gama.core.common.interfaces.IGraphics` $\rightarrow$ `gama.api.ui.displays.IGraphics`
    *   `gama.core.outputs.LayeredDisplayData` $\rightarrow$ `gama.api.ui.displays.IDisplayData`
*   **Layers:**
    *   `gama.core.outputs.layers.ILayer` $\rightarrow$ `gama.api.ui.layers.ILayer`
    *   `gama.api.ui.layers.IDrawingAttributes` (used to encapsulate styling when drawing shapes).

---
