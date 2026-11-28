# GAMA Agent Guide

GAMA is an Eclipse 2026-03 RCP/OSGi and Xtext application built with Maven and
Tycho. See [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md), especially its
guidance on runtime errors and warnings.

## Working practices

- Work in the existing checkout. Do not create worktrees, change branches,
  commit or push unless explicitly requested. Preserve unrelated local changes.
- When asked only to investigate, explain or propose, do not implement changes
  until requested.
- Do not leave newly added test files unless explicitly requested. Use focused
  existing non-UI checks where appropriate; leave UI testing to the user.
- Write code, identifiers, comments and documentation in English. Reply in the
  user's language.

## Repository map

- `gama.annotations` defines GAML annotations; `gama.processor` generates
  registration code and tests from annotations.
- `gama.api` contains shared contracts and runtime services; `gama.core`
  implements the simulation engine and built-in GAML constructs.
- `gaml.grammar` contains the Xtext grammar and generator; `gaml.compiler`
  implements the compiler; `gama.ui.editor` provides editor features.
- `gama.ui.*` contains the RCP UI and display renderers; `gama.workspace`
  manages workspace models and projects.
- `gama.extension.*` contains optional extensions, `gama.library` contains
  models, `gama.headless` provides headless and server execution, and
  `gama.dev` provides development utilities.

Use current package and bundle names; do not use legacy `msi.gama.*` or
`ummisco.gama.*` package or bundle names for new code.

## Builds, generation and OSGi

- The platform builds and runs on JDK 25. `gama.annotations` and
  `gama.processor` must remain Java 21-compatible because the Eclipse annotation
  processor can run on Java 21. Use modern Java features where appropriate
  without introducing newer language features or APIs into those two bundles.
- The main reactor is `gama.parent/pom.xml`. It does not include
  `gama.annotations` or `gama.processor`: when these need rebuilding or
  installing, build them separately in that order, as in `travis/build.sh`.
  Eclipse's factory paths for `gama.api` and `gama.core` use
  `gama.processor/gama.processor.jar`; rebuild that jar when the processor
  changes.
- For a focused compile, select affected bundles and any modified upstream
  bundles explicitly: Tycho can otherwise resolve installed bundles instead of
  changed sources. Disable the Maven build cache to ensure a fresh compile. For
  example, after changes to both API and core:

  ```sh
  mvn -f gama.parent/pom.xml -pl ../gama.api,../gama.core \
    -am -DskipTests -Dmaven.build.cache.enabled=false compile
  ```

  Add `-o` only when the necessary dependencies are already cached.
- Regenerate `GamlAdditions` for bundles whose GAML annotations change; do not
  hand-edit generated registration code. Missing or incompatible API/core
  contributions can cause startup to exit with code 42.
- Declare bundle dependencies and package visibility in the relevant
  `META-INF/MANIFEST.MF`, and ensure third-party dependencies are available in
  the target platform. In particular, OpenGL4 receives JOGL through
  `gama.ui.display.opengl` exports: export any newly used `com.jogamp` package
  there as well.

## GAML and runtime

- Inspect existing `gama.api` and `gama.core` examples before adding annotated
  constructs. Operators are `public static`; when context is needed, put
  `final IScope scope` first and use `scope.getRandom()` for simulation
  randomness. Document new operators with `@doc` and an illustrative
  `@example`; an example alone does not guarantee that a test runs.
- Skills extend `gama.api.kernel.skill.Skill`. Declare variables with `@vars`
  and `@variable`, using shared attribute-name constants in `@getter` and, for
  mutable variables, `@setter`. Actions use `@action`, accept
  `final IScope scope` and retrieve arguments from that scope.
- At GAML runtime, throw `GamaRuntimeException.error(...)` when an operation
  cannot safely continue. For recoverable conditions, follow the reporting and
  explicit fallback guidance in [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md);
  constructing an exception does not itself report it.
- Keep experiment and simulation ownership distinct, including in
  multi-simulation and co-model contexts. Use `getEnclosingContext()` for
  expression lookup and typing, and `getEnclosingDescription()` for structural
  ancestry. Preserve caller locals, target `self` and caller `myself` in remote
  contexts; action arguments are evaluated in the caller's scope.
- Scope copies and forks can retain and synchronize execution frames. Clear
  short-lived copies/forks after their work and child tasks finish, using
  try-with-resources or `GAMA.releaseScope()`. Use `detachedCopy()` for
  long-lived scopes, or `copyForGraphics()` for graphics. Preserve persistent
  output locals and their live, description-bound storage across scope copies.
- Avoid unnecessary allocations and stream pipelines in per-agent or
  per-simulation-step hot paths; prefer primitive collections or GAMA
  `IList`/`IMap` factories where appropriate.

## Editor, UI and documentation

- Editor completion and hover must work on incomplete or uncompiled source; do
  not assume a compiled model is available. If the grammar changes, use the
  existing Xtext generator in `gaml.grammar` and review its generated artifacts.
- Update SWT widgets on the UI thread and follow existing resource lifecycle
  patterns. For backgrounds that must survive Eclipse CSS theming, use
  `GamaColors.setBackground(...)`.
- For display-coordinate changes, document defaults, units, reference frames
  and compatibility. Check the relevant renderers and keep
  `gama.documentation/files/input/markdown/DisplayCoordinates.md` and the
  display-construction wiki aligned where applicable.
- For user-visible language or behavior changes, update the relevant
  documentation and, where appropriate, `CHANGELOG_2026.md` and
  `MIGRATION_GUIDE_2026.md`. Keep migration converters consistent with the
  guide; flag intent-dependent migrations for modeler review rather than
  guessing.
