# GAMA 2026 Changelog

All notable changes to the GAMA platform for the 2026 version (JDK 25 / Eclipse 2025-12 upgrade) will be documented in this file.

## [2026.0.0] - Unreleased

### Added
- **GAML Language:** Native support for `class` and `object` constructs, allowing for lightweight, object-oriented data structures without the overhead of full simulation agents.
- **Graphics:** A brand new, highly optimized native display plugin (`gama.ui.display.opengl4`) built strictly on the OpenGL 4.1 Core Profile, utilizing Shaders, VAOs/VBOs, and the JOML math library.
- **UI:** A new, responsive launching overlay that provides clear feedback during model compilation, agent initialization, and setup progress.
- **Architecture (`gama.api`):** A brand new, isolated `gama.api` module acting as the definitive public contract for the platform, completely decoupling public interfaces from internal implementations.
- **Architecture (Factories):** Generalization of the Factory pattern for core data structures (e.g., `GamaListFactory`, `GamaShapeFactory`, `GamaMapFactory`).

### Changed
- **Platform:** Upgraded the underlying execution environment to Java Development Kit (JDK) 25 and the Eclipse 2025-12 Rich Client Platform.
- **Performance:** Massive improvements to concurrency support (both internal engine multi-threading and GAML parallel operations) and significantly faster model compilation times due to AST restructuring.
- **GAML Syntax (Arguments):** The syntax for parameter passing (`with:`) has been modernized from bracket/double-colon `[a::1]` to parenthesis/single-colon `(a:1)`.
- **GAML Syntax (Arrow Functions):** The outermost curly braces surrounding the body of `->` (arrow) expressions are no longer required by the parser (`action my_action -> do something;`).
- **GAML Syntax (Displays):** The layer previously known as `image` is renamed to `picture` to avoid keyword collisions. Displays and experiments now enforce a separation between programmatic identifiers and UI titles (`display My_Chart title: "My Chart"`).
- **Core API (`IAgent`):** The `IAgent` interface has been profoundly redesigned for safety. It now extends `IDelegatingShape`, deprecates direct attribute mutation via container methods, and ensures `getPeers()` is strictly read-only.
- **Core API (`IPopulation`):** Population logic is centralized into a new `AbstractPopulation` base class. Methods have been renamed for clarity (e.g., `createAgentAt` to `createAgentAtIndex`).
- **Java Annotations:** The monolithic `GamlAnnotations` class has been flattened. All GAML annotations (`@operator`, `@species`, etc.) are now top-level interfaces in the `gama.annotations` package.
- **Type Casting:** The central `Cast` utility class has been significantly reduced. Type conversions are now intelligently delegated directly to `IType` implementations.
- **Utilities:** Core utilities and I/O methods (e.g., `FileUtils`, `GamaPreferences`, `StringUtils`) have been centralized and moved to the `gama.api.utils.*` package.

### Removed / Deprecated
- **GAML Syntax:** The redundant keywords `var:` (in `diffuse` statements) and `to:` (in `transition` statements) have been deprecated and removed.
- **Legacy Graphics:** Legacy OpenGL 2 fixed-function pipeline features (`glBegin`/`glEnd`, `glMatrixMode`) are no longer used by the primary OpenGL4 rendering engine.
- **Direct Instantiation:** Direct constructor calls for core types (e.g., `new GamaList()`, `new GamaPoint()`) are strictly prohibited in plugin development, replaced entirely by Factories.
