# GAMA Developer Tools (`gama.ui.devtools`)

An Eclipse plugin for the developers of GAMA. It adds a **GAMA** menu to the menu bar and a **GAMA** sub-menu to the context menu of Java projects.

## Installation

Install it in the Eclipse IDE used to develop GAMA, not in GAMA itself. This Eclipse needs JDT and PDE and must run on Java 21 or newer. Eclipse can use its bundled JustJ Java 21 runtime; configure a separate JDK 25 under *Preferences > Java > Installed JREs* for compiling and running GAMA.

1. In Eclipse, choose *Help > Install New Software...*
2. Add the update site `https://gama-platform.org/gama/devtools/`.
3. Select **GAMA Developer Tools**, then restart Eclipse.

Updates are then found by *Help > Check for Updates*.

The update site is built from `gama.p2site.devtools`, which contains only `gama.feature.devtools`. The workflow `.github/workflows/publish-devtools.yaml` publishes it on every change of the developer tools on `main`. The plugin depends only on Eclipse bundles; the jgrapht jars it uses are embedded in `libs/`. To try local changes without installing, launch a runtime Eclipse from the workspace.

## Commands

| Command | What it does |
|---|---|
| **Find GAML Keyword Implementation...** (`Ctrl/Cmd+Shift+Alt+K`) | Finds the Java code behind a GAML keyword (see below). |
| **Clean and Rebuild All GAMA Plugins** (`Ctrl/Cmd+Shift+Alt+B`) | Saves the editors and repairs the configuration (optional). Disables the annotation processor, deletes the `gaml/` folders and re-enables the processor. Then runs a clean build and a full build of the workspace, or only of the GAMA plugins (preference), and checks that every `GamlAdditions.java` was regenerated. No dialog is shown and the job can be cancelled. |
| **Clean and Rebuild Selected Plugins** | The same, limited to the GAMA plugins in the selection. |
| **Check GAMA Plugins Configuration** | Checks every GAMA plugin (see below), adds problem markers with quick fixes (`Ctrl/Cmd+1` in the Problems view) and offers to repair everything at once. |
| **Rebuild the Annotation Processor Jar** | Regenerates `gama.processor/gama.processor.jar` from `Generate Processor.jardesc`. Use it after modifying `gama.processor` or `gama.annotations`. |
| **Update Java Headers...** | Sets the GAMA version and copyright end year, updates existing headers and adds missing ones, with a mandatory per-file preview and undo. |
| **GAML Documentation and Tests Report** | Builds a Markdown report from the warnings of the processor. It lists the undocumented GAML elements and the untested operators, per plugin. |
| **Generate GAML Test Stubs for Untested Operators** | Writes `models/Tests/Untested Operators.gaml` in each selected plugin (or all of them). It contains one `test` stub per operator reported as not tested, with its Java signature. Stubs already in the file are kept. |
| **Package Dependency Graph...** | Runs `jdeps` on the compiled bundles and produces an interactive HTML graph of the package dependencies. This needs a JDK. |
| **New > GAMA Plugin / Skill / Operators / Action / Type / GAML Test Model** | The wizards. They are also available in *File > New > Other... > GAMA Development*. |

## Updating Java headers

**GAMA > Update Java Headers...** asks for a GAMA version and copyright end year. By default it processes all open GAMA plugins; the scope can be restricted to the selected GAMA projects. The next page previews the changes and lets you exclude individual files before applying them.

Every Java compilation unit in the projects' Java source folders is considered, including `package-info.java` and files created without a GAMA wizard. Missing GAMA headers are added, and existing ones have their filename, plugin name, version and GAMA copyright year updated. Existing copyright holders, author annotations and other license notices are preserved; a new GAMA header is prepended rather than replacing an unrelated notice. Files outside Java source folders (such as vendored files under `libs/`), generated `gaml/` sources, and derived resources are excluded.

Line endings, encoding and the rest of the file are preserved. Changes use Eclipse's refactoring preview and undo support, including its handling of open editor buffers and save state. The command is independent of Clean & Rebuild and never runs automatically. After applying it, the version and year are remembered and used by the Java wizards as well. This command does not change files in closed or non-GAMA projects.

The text transformer has a standalone regression test that needs only a JDK:
```
javac -d /tmp/gama-header-tests gama.ui.devtools/src/gama/ui/devtools/util/JavaHeaderText.java gama.ui.devtools/tests/JavaHeaderTextTest.java
java -cp /tmp/gama-header-tests JavaHeaderTextTest
```

## Finding the Java implementation of a GAML keyword

**GAMA > Find GAML Keyword Implementation...** opens a dialog like *Open Type*. It lists the GAML keywords declared in the Java sources of the open projects, with the Java element that implements each one:

- operators, statements, skills, species, types, file, experiment and display types, classes and constants;
- actions and their arguments;
- attributes, with their getters and setters;
- facets.

The names are read from the annotations (`@operator`, `@symbol`, `@variable`, `@facet`, `@arg`...). Names given as constants, such as `IKeyword.SPEED` or a local `VELOCITY`, are resolved to their GAML value.

- The pattern matches the start of the names and is case-insensitive. `*` and `?` are wildcards when the pattern also contains a letter (`*_path`); on their own they find the operators `*`, `?`...
- To restrict the search to one kind, prefix the pattern with the kind: `facet:speed`, `op:+`, `action:go`, `attr:location`, `statement:draw`.
- The dialog opens with the selected text or the word under the caret, in any editor, including the GAML editor. For example, put the caret on `draw` in a model and press `Ctrl/Cmd+Shift+Alt+K`.
- Choosing one or several entries opens the Java files and selects the name in the annotation.

The index needs no build. The first search parses the annotated files (about one second for the whole GAMA code base), and each later search only parses the files that changed.

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
- Library declarations are left unchanged: repairs never register JARs found under `libs/` automatically. Add runtime dependencies explicitly; attach source and Javadoc archives to their libraries instead of adding them to the runtime classpath.

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
