# GAMA Developer Tools (`gama.ui.devtools`)

An Eclipse plugin for the developers of GAMA. It adds a **GAMA** menu to the menu bar and a **GAMA** sub-menu to the context menu of Java projects.

## Installation

Install it in the Eclipse IDE used to develop GAMA, not in GAMA itself. This Eclipse needs JDT and PDE and must run on Java 21 or newer. Eclipse can use its bundled JustJ Java 21 runtime; configure a separate JDK 25 under *Preferences > Java > Installed JREs* for compiling and running GAMA.

1. In Eclipse, choose *Help > Install New Software...*
2. Add the update site `https://gama-platform.org/gama/devtools/`.
3. Select **GAMA Developer Tools**, then restart Eclipse.

Use **GAMA > Update GAMA tools** to check the developer tools update site and install the latest
update directly, without Eclipse's software selection wizard. It updates only the installed developer
tools feature, not the rest of Eclipse. If no update is available, a dialog says so. The check and
installation run as cancellable background jobs; network and installation errors are reported rather
than treated as "no update". Eclipse still handles trust/signature prompts and restarting after
installation; a license prompt is shown only for licenses not already accepted. Finish any other software
installation or update before using this command. Workspace launches and plugins copied into `dropins`
must first be installed as the feature from the update site to use this action.

The update site is built from `gama.p2site.devtools`, which contains only `gama.feature.devtools`. The workflow `.github/workflows/publish-devtools.yaml` publishes it on every change of the developer tools on `main`. Eclipse supplies the JDT/PDE, EGit/JGit and Gson dependencies; the jgrapht jars are embedded in `libs/`. To try local changes without installing, launch a runtime Eclipse from the workspace.

The public URL is a composite p2 repository. Each workflow run adds an immutable child at
`devtools/releases/<run-id>-<attempt>/`; previous builds are retained on the `devtools-site` branch and included
in subsequent Pages deployments. Eclipse can therefore finish an installation using cached metadata even
after a newer build is published. Do not delete that branch or prune old releases while clients may need them.
Publishing requires permission for the workflow token to push to that branch; branch rules must allow it.
On the first run of this publishing scheme, the latest successful main-branch Pages artifact is restored
if available. A previous flat repository is migrated into a child while its original artifact URLs remain
available. If that artifact has expired, the workflow fails rather than silently discarding the old site;
restore a copy of the published site to the `devtools-site` branch before retrying.

Local `target/repository` builds remain standalone repositories and are replaced by `clean`. For repeated
local installation, use a uniquely named ZIP for each build rather than a cached directory URL.

## Commands

The **GAMA** menu uses compact **Repair**, **Search** and **Create** submenus, without gray headings
or separators for those groups. **Run GAMA**, **Build GAMA** and **Install** retain their gray,
non-clickable section headings, with **Update GAMA tools** at the bottom.
The **Create** submenu lists all six creation wizards. They are also available in
*File > New > Other... > GAMA Development* and in the Java-project context menu.

The menu layout, disabled headings, launch parameters and updater registration have a standalone regression test:
```
java gama.ui.devtools/tests/DevToolsMenuTest.java .
```

| Command | What it does |
|---|---|
| **Keyword implementations...** (`Ctrl/Cmd+Shift+Alt+K`) | Finds the Java code behind a GAML keyword (see below). |
| **Run... / Debug... > GAMA GUI / GAMA Headless... / GAMA Tests** | Launches workspace GAMA through editable PDE Eclipse Application configurations. |
| **Run... > Configure Launches...** | Associates each flavor with an existing configuration or generates a default; provides editing and confirmed restoration of defaults. |
| **Clean and build all plugins** (`Cmd+B` on macOS, `Ctrl+B` on Windows/Linux) | Saves the editors, refreshes the whole workspace recursively from disk and repairs the configuration (optional). Disables the annotation processor, deletes the `gaml/` folders and re-enables the processor. Then runs a clean build and a full build of the workspace, or only of the GAMA plugins (preference), and checks that every `GamlAdditions.java` was regenerated. No dialog is shown and the job can be cancelled. |
| **Clean and build selected plugins** | The same, limited to the GAMA plugins in the selection; the initial refresh still covers the whole workspace. |
| **Repair plugins metadata** | Checks every GAMA plugin (see below), adds problem markers with quick fixes (`Ctrl/Cmd+1` in the Problems view) and offers to repair everything at once. |
| **Rebuild annotation processor** | Regenerates `gama.processor/gama.processor.jar` from `Generate Processor.jardesc`. Use it after modifying `gama.processor` or `gama.annotations`. |
| **Regenerate Xtext grammar** | Runs `gaml.grammar/src/gaml/grammar/generator/Generate_GAML_Artefacts.mwe2` to regenerate the GAML grammar artifacts. The `gaml.grammar`, `gaml.compiler` and `gama.ui.editor` projects must be open. |
| **Update file headers** | Sets the GAMA version and copyright end year, updates existing headers and adds missing ones, with a mandatory per-file preview and undo. |
| **Missing annotations...** | Builds a Markdown report from the warnings of the processor. It lists the undocumented GAML elements and the untested operators, per plugin. |
| **Generate missing tests** | Writes `models/Tests/Untested Operators.gaml` in each selected plugin (or all of them). It contains one `test` stub per operator reported as not tested, with its Java signature. Stubs already in the file are kept. |
| **Plugins dependencies...** | Runs `jdeps` on the compiled bundles and produces an interactive HTML graph of the package dependencies. This needs a JDK. |
| **Update GAMA tools** | Checks for and directly installs developer tools updates, or reports that no update is available. |
| **Create: New GAMA Plugin / Skill / Operators / Action / Type / GAML Test Model** | Creation wizards, also available in *File > New > Other... > GAMA Development*. |
| **Install core plugins... / Install graphical editor... / Install community plugins...** | Checks out source repositories, imports selected projects and optionally applies working sets and workspace preferences (see below). |

The Clean and Rebuild All shortcut takes precedence over Eclipse's standard Build All binding in the default
key scheme. It always targets all GAMA plugins, regardless of selection. Explicit user bindings and more
specific editor bindings can still override it; configure these in *Preferences > General > Keys*.

## Installing source projects

The **Install** section provides three variants of the same wizard:

- **Core plugins** uses `https://github.com/gama-platform/gama.git`, branch `main`. It lists top-level
  Eclipse projects, preselecting those listed in `gama.parent/pom.xml` plus `gama.parent`, including
  `gama.export`, `gama.ui.export` and `gama.feature.export`. Already imported projects are not preselected.
- **Graphical editor** uses `https://github.com/gama-platform-plugins/gama.graphical.modeling.git`,
  branch `GAMA_2026-06`, with all its Eclipse projects preselected.
- **Community plugins** discovers repositories in `https://github.com/gama-platform-plugins` and lists their
  Eclipse project paths on `GAMA_2026-06`, including nested projects. Load the list, then explicitly select
  projects; only repositories containing selected projects are checked out. Repositories without that branch
  or without Eclipse projects are listed as unavailable, not silently switched to another branch.

Branches, HTTPS repository URLs and checkout directories are editable. Community installations use one
subdirectory per repository; select a project row to edit its repository URL. Discovery uses GitHub's public
API (with pagination); rate-limit and network errors are reported. Clone/fetch uses JGit, so a separate Git
executable is not needed. Public HTTPS repositories work without authentication.

**Next** clones a missing repository or fetches, checks out the selected branch and fast-forwards an existing
clean checkout with a matching origin. It refuses dirty repositories, unfinished Git operations and branches
with local commits: commit/stash or reconcile those yourself, or choose another directory. It never resets,
stashes or overwrites local edits. Checkout happens before project selection and is retained on cancellation.
Projects are imported in place and shared with EGit, not copied into the workspace. Already imported projects
are shown but not selected, and duplicate project names across repositories must be resolved by selecting
only one.

The final page offers additive working sets based on `utils/Project sets for Gama.psf`. Choose which sets
to create/update. Core installation preselects the **==================== EXPORT** working set, containing
the three export projects, alongside the other core working sets. Existing members are preserved and missing
projects are ignored. Selected community projects
that exist in the workspace are added to **==================== COMMUNITY**; it is populated dynamically rather
than maintained as an obsolete fixed experimental-project list. An option displays working sets as roots in
both Project Explorer and Package Explorer.

Preference import is **opt-in**, with separate checkboxes for `Eclipse Preferences for Gama.epf`,
`Cleanup rules for Gama.xml` and `Code formatter rules for Gama.xml`. The named GAMA profiles are installed
and selected, preserving other profiles. The Eclipse export also contains cleanup/formatter settings and
**Java compiler defaults**; review that choice before importing it. Project-specific settings take
precedence. Importing preferences does not rewrite, clean up or format source files.

Closing **gama.parent** to disable its Maven compilation is recommended and selected by default. Finish saves
editors, imports projects, applies the chosen settings and schedules a recursive workspace refresh followed by
a clean and full workspace build. Configure a suitable JDK and resolve the GAMA target platform separately.
Cancellation/failure retains completed checkouts/imports and reports the failure; there is no destructive rollback.

Explorer layout changes and JDT profile import are isolated in `install/WorkspaceSetup.java`. Eclipse does
not expose public APIs for all of these operations, so that adapter uses JDT's internal profile/Package
Explorer APIs and must be checked when updating the supported Eclipse platform.

The project discovery/project-set regression test runs without Eclipse:
```
javac -d /tmp/gama-install-tests gama.ui.devtools/src/gama/ui/devtools/install/InstallModel.java gama.ui.devtools/tests/InstallModelTest.java
java -cp /tmp/gama-install-tests InstallModelTest .
```
`tests/RepositoryCheckoutTest.java` additionally exercises cloning, fast-forward updates, branch selection,
cancellation and refusal to overwrite local edits/commits, using only disposable local repositories. Compile
and run it with JGit and Eclipse Core Runtime (and their dependencies) on the classpath.

## Running and debugging workspace GAMA

The **GAMA > Run...** and **GAMA > Debug...** menus share three launch configurations. **GUI** opens GAMA,
**Headless...** asks for mode/arguments (for example, an XML experiment file and output directory, or
`-batch experimentName "/path/model.gaml"`), and **Tests** generates a headless configuration with `-test`.
Headless arguments are remembered; tests report to Eclipse's Console.

On first use, select an existing **Eclipse Application** configuration or generate a default with a registered
**JDK 25**. An exact Java 25 JDK is required because the current product VM arguments enable preview features;
Eclipse itself can still run on Java 21. Add the JDK in *Preferences > Java > Installed JREs*.

Default configurations, named `GAMA - GUI`, `GAMA - Headless` and `GAMA - Tests`, read the workspace
`gama.product/gama.product` or `gama.product/gama.headless.product`. Import the product project, the feature
projects listed by the product, and their GAMA plugins, and resolve the GAMA target platform. Creation checks
the required projects; PDE validates bundle dependencies at launch. No GAMA binaries are bundled in devtools.
Generated defaults use feature-based plugin selection, the product's common and OS-specific VM/program
arguments, startup levels, and configuration properties. `${build.id}` becomes `development` for these
workspace launches. The product's `-data` argument is replaced by a separate runtime workspace per flavor,
located beside the development workspace (`gama-runtime-gui`, `gama-runtime-headless`, `gama-runtime-tests`).
They do not clear runtime workspaces automatically.

**Configure Launches...** lets you select each flavor and change its associated configuration. **Edit
configuration...** opens Eclipse's standard launch properties, including JRE, memory, arguments and plugins.
Existing configurations and customizations are never automatically rewritten. **Restore generated defaults...**
asks for confirmation, then creates and selects a fresh configuration; the old configuration is retained.
New product settings are picked up when generating defaults, not silently applied to existing configurations.
These launch commands require the Eclipse Debug, Java launching and PDE launching bundles, installed as
devtools dependencies. They use public Eclipse APIs rather than version-specific PDE implementation classes.

The product-settings regression test runs without Eclipse, from the repository root:
```
javac -d /tmp/gama-launch-tests gama.ui.devtools/src/gama/ui/devtools/launch/ProductLaunchSpec.java gama.ui.devtools/tests/ProductLaunchSpecTest.java
java -cp /tmp/gama-launch-tests ProductLaunchSpecTest .
```

## Updating Java headers

**GAMA > Update file headers** asks for a GAMA version and copyright end year. By default it processes all open GAMA plugins; the scope can be restricted to the selected GAMA projects. The next page previews the changes and lets you exclude individual files before applying them.

Every Java compilation unit in the projects' Java source folders is considered, including `package-info.java` and files created without a GAMA wizard. Missing GAMA headers are added, and existing ones have their filename, plugin name, version and GAMA copyright year updated. Existing copyright holders, author annotations and other license notices are preserved; a new GAMA header is prepended rather than replacing an unrelated notice. Files outside Java source folders (such as vendored files under `libs/`), generated `gaml/` sources, and derived resources are excluded.

Line endings, encoding and the rest of the file are preserved. Changes use Eclipse's refactoring preview and undo support, including its handling of open editor buffers and save state. The command is independent of Clean & Rebuild and never runs automatically. After applying it, the version and year are remembered and used by the Java wizards as well. This command does not change files in closed or non-GAMA projects.

The text transformer has a standalone regression test that needs only a JDK:
```
javac -d /tmp/gama-header-tests gama.ui.devtools/src/gama/ui/devtools/util/JavaHeaderText.java gama.ui.devtools/tests/JavaHeaderTextTest.java
java -cp /tmp/gama-header-tests JavaHeaderTextTest
```

## Finding the Java implementation of a GAML keyword

**GAMA > Keyword implementations...** opens a dialog like *Open Type*. It lists the GAML keywords declared in the Java sources of the open projects, with the Java element that implements each one:

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
