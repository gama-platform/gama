# GAMA 2026 Changelog

This document lists the major technical modifications and commits grouped by themes introduced in the GAMA Version 2026 update (branch `Upgrade-to-Eclipse-2025-12-and-JDK-25`).

## [2026.0.0] - Unreleased

### Core Technology & Modern Java (JDK 25)
*   **Platform Upgrade:** Rebased the entire project to require JDK 25 and target the Eclipse 2025-12 RCP environment.
*   **Record adoption:** Converted heavily used classes (e.g., `GamlCompilationError`) into immutable Java records to improve safety and reduce memory overhead.
*   **Pattern Matching:** Extensive refactoring to use Java pattern matching for `instanceof` (e.g., `SpeciesDescription` checks), greatly improving code readability and reducing explicit casts.

### Architecture & API Separation (`gama.api`)
*   **Module extraction:** Created the isolated `gama.api` module. Hundreds of core interfaces (`IAgent`, `IPoint`, `IList`, `ITopology`) were moved here from `gama.core` and `gama.annotations` to guarantee stable public contracts for plugin developers.
*   **Factory Generalization:** Centralized object creation via Factories (`GamaListFactory`, `GamaShapeFactory`, `GamaMapFactory`). `PoolUtils` was rewritten using concurrent queues and `AtomicLong` counters to safely pool instances like `AgentExecutionContext` and geometry factories.
*   **Agent Encapsulation:** `IAgent` interface profoundly redesigned to extend `IDelegatingShape`, delegating geometry implementations securely. Modification of agent properties via the container interface is strictly blocked in favor of standard variable updates.
*   **Population Centralization:** Extracted common population management logic (internal containers, grid handling, topology handling) into the `AbstractPopulation` base class to DRY the code. `IPopulation.createAgentAt()` renamed to `createAgentAtIndex()`.
*   **Cast Evolution:** The massive `Cast` utility class was decentralized. Type conversion logic is now implemented by the respective `IType` classes. `GamaColorFactory.createFrom` renamed to `castToColor` to unify color coercion across layers and displays.

### GAML Language, Compiler, & AST
*   **Grammar Plugin Extraction:** The Xtext grammar definition (`Gaml.xtext`) has been extracted from the compiler into its own dedicated plugin (`gaml.grammar`), alongside optimization of parsing rules and code generators.
*   **Object-Oriented GAML:** Native support introduced for `class` and `object` constructs, bridging the gap between agent-based modeling and classical software engineering for data modeling.
*   **Stateless Compilation:** Re-engineered the expression compiler to be entirely stateless via `ExpressionCompilationContext` and `ExpressionCompilationSwitch`. Thread-local states were removed, significantly improving thread-safety and parallel compilation stability.
*   **Validation Contexts:** Refactored `ValidationContext` and `IDocumentationContext` to separately track validations and documentation, reducing lock contention. `GamlResourceServices` rewritten to use thread-safe maps and atomic operations.
*   **Cleaner Syntax:** Deprecated and removed noisy grammar keywords (`diffuse var:` becomes `diffuse`, `transition to:` becomes `transition`).
*   **Syntax Enhancements:** Parameter lists (`with:`) now use parentheses and colons `(a:1)` instead of brackets and double-colons. Arrow expressions `->` no longer require curly braces around single statements.

### Concurrency & Parallel Execution
*   **Multi-threading Optimizations:** Overhauled internal multi-threading mechanisms. Implemented a `ConcurrentHashMap` cache for parallel execution evaluation.
*   **ForkJoin executor:** Upgraded the `AGENT_PARALLEL_EXECUTOR`. Switched the internal pool to async mode, customized the `ForkJoinWorkerThreadFactory`, and improved robustness against interrupts and timeouts during engine shutdown.
*   **Parallel Steppers:** Switched agent result tracking in `ParallelAgentExecuter` and `ParallelAgentStepper` from `Boolean[]` arrays to high-performance `AtomicBoolean` implementations.

### UI, Displays, & OpenGL
*   **OpenGL 4.1 Native Engine:** Complete release of `gama.ui.display.opengl4`, the first pure OpenGL 4.1 Core Profile display plugin. Replaced legacy OpenGL 2 fixed pipelines with modern Shaders, VAOs, VBOs, and the JOML matrix math library.
*   **New Launching Overlay:** Replaced the legacy loading screen with a modern, responsive launching overlay when loading and compiling experiments, preventing UI unresponsiveness.
*   **Display Syntax:** The `image` layer inside display blocks was renamed to `picture`. Displays now strictly separate logical identifiers from user-visible string titles.
