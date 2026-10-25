# GAMA Developer Tools (`gama.ui.devtools`)

An Eclipse plugin for the developers of GAMA. It adds a **GAMA** menu to the menu bar and a **GAMA** sub-menu to the context menu of Java projects.

## Installation

Install it in the Eclipse IDE used to develop GAMA, not in GAMA itself. This Eclipse needs JDT and PDE and must run on Java 25.

1. In Eclipse, choose *Help > Install New Software...*
2. Add the update site `https://gama-platform.org/gama/devtools/`.
3. Select **GAMA Developer Tools**, then restart Eclipse.

Updates are then found by *Help > Check for Updates*.

The update site is built from `gama.p2site.devtools`, which contains only `gama.feature.devtools`. The workflow `.github/workflows/publish-devtools.yaml` publishes it on every change of the developer tools on `main`. The plugin depends only on Eclipse bundles; the jgrapht jars it uses are embedded in `libs/`. To try local changes without installing, launch a runtime Eclipse from the workspace.

## Commands

| Command | What it does |
|---|---|
| **Clean and Rebuild All GAMA Plugins** (`Ctrl/Cmd+Shift+Alt+B`) | Saves the editors and repairs the configuration (optional). Disables the annotation processor, deletes the `gaml/` folders and re-enables the processor. Then runs a clean build and a full build of the workspace, or only of the GAMA plugins (preference), and checks that every `GamlAdditions.java` was regenerated. No dialog is shown and the job can be cancelled. |
| **Clean and Rebuild Selected Plugins** | The same, limited to the GAMA plugins in the selection. |
| **Check GAMA Plugins Configuration** | Checks every GAMA plugin (see below), adds problem markers with quick fixes (`Ctrl/Cmd+1` in the Problems view) and offers to repair everything at once. |
| **Rebuild the Annotation Processor Jar** | Regenerates `gama.processor/gama.processor.jar` from `Generate Processor.jardesc`. Use it after modifying `gama.processor` or `gama.annotations`. |
| **GAML Documentation and Tests Report** | Builds a Markdown report from the warnings of the processor. It lists the undocumented GAML elements and the untested operators, per plugin. |
| **Generate GAML Test Stubs for Untested Operators** | Writes `models/Tests/Untested Operators.gaml` in each selected plugin (or all of them). It contains one `test` stub per operator reported as not tested, with its Java signature. Stubs already in the file are kept. |
| **Package Dependency Graph...** | Runs `jdeps` on the compiled bundles and produces an interactive HTML graph of the package dependencies. This needs a JDK. |
| **New > GAMA Plugin / Skill / Operators / Action / Type / GAML Test Model** | The wizards. They are also available in *File > New > Other... > GAMA Development*. |

## Quick fixes on the processor warnings

The warnings of the annotation processor (`GAML: documentation missing for ...` and `GAML: operator '...' is not tested`) get quick fixes, in the Java editor and in the Problems view (`Ctrl/Cmd+1`). In the Problems view, a fix can be applied to all the selected warnings at once.

| Warning | Quick fixes |
|---|---|
| Documentation missing | **Add a @doc skeleton.** Adds `@doc("TODO: ...")` to the element, or fills its empty `@doc` or `doc = {}`. For attributes, arguments and facets, the documentation goes in the matching `@variable`, `@arg` or `@facet` annotation. The name can be a literal or a constant such as `IKeyword.SPEED`. |
| Operator not tested | **Add a @test skeleton.** Adds a deliberately failing `@test("false /* TODO ... */")` to replace with a real assertion. <br> **Mark as @no_test(Reason.ALREADY_TESTED)** or **@no_test(Reason.IMPOSSIBLE_TO_TEST)**. <br> **Generate a GAML test stub** in `models/Tests/Untested Operators.gaml`. |

The fixes only modify the Java sources. The warnings disappear at the next build.

## Configuration checks

A GAMA plugin is a Java project whose `.factorypath` references `gama.processor`, or whose `plugin.xml` declares the `gaml.extension` extension point. The checker verifies the following.

**Annotation processing**
- APT is enabled.
- The generated source directory is `gaml`.
- Processing during reconcile is disabled.
- The factory path contains `/gama.processor/gama.processor.jar`.
- The processor jar exists and is not older than the processor sources.
- The generated additions exist.

**Build path and packaging**
- `gaml` is a source folder of `.classpath` and of `build.properties`.
- `plugin.xml` declares `gaml.extension`, and the bundle is a singleton.
- `models/` is exported.
- The jars of `libs/` are declared in `Bundle-ClassPath`, `bin.includes` and `.classpath`.

**Naming and encoding**
- The encoding is UTF-8.
- The bundle name matches the project name.
- No two plugins share the same last segment. That segment names the generated package `gaml.additions.<shortcut>`.

## New GAMA plugin

The wizard creates a plugin with the following layout:

```
gama.extension.foo/
  src/<base package>/     Java sources (and an example operator)
  gaml/                   generated by the annotation processor (git-ignored)
  libs/                   third-party jars (optional)
  models/Tests/ ...       GAML models (Tests, Recipes, Library models, Data)
  META-INF/MANIFEST.MF  build.properties  plugin.xml  pom.xml  README.md
  .classpath  .factorypath  .settings/ (APT, JDT, encoding)
```

By default the project goes next to the other GAMA projects, in the git repository. The wizard can also do the following:
- register the module in `gama.parent/pom.xml`;
- add the plugin to `gama.feature.extensions`, or create a dedicated `gama.feature.extension.<shortcut>` feature and reference it in `gama.p2site/category.xml`.

A **Clean and Rebuild** of the new plugin is then started.

## Preferences

*Preferences > GAMA Developer Tools*: the default vendor and plugin id prefix of the wizards, and the options of **Clean and Rebuild**:
- repair before cleaning;
- rebuild the whole workspace or only the GAMA plugins;
- check after rebuilding.
