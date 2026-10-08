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
