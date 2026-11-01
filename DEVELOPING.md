# Developing GAMA

GAMA is an agent-based modeling and simulation platform built from Eclipse/OSGi bundles. Its models use
the GAML language; the desktop application, headless runtime, and extensions share the same core.
This guide covers a development workspace and the main places to start exploring the code.

## Set up the development environment

1. Install [Eclipse IDE for Java and DSL Developers 2026-03](https://www.eclipse.org/downloads/packages/release/2026-03/r/eclipse-ide-java-and-dsl-developers).
   Eclipse and the annotation processor run on Java 21; Eclipse can use its bundled Java 21 runtime.
   The IDE needs Java development tools, Xtext, and the Plug-in Development Environment (PDE).
   If PDE is missing, install it from the
   [Eclipse 2026-03 update site](https://download.eclipse.org/releases/2026-03/).
2. Install a separate full **JDK 25**, such as [Eclipse Temurin](https://adoptium.net/temurin/releases/).
   GAMA compilation and launches require JDK 25, not just a JRE.
3. In Eclipse, add JDK 25 under **Preferences > Java > Installed JREs** and select it as the default.
   Under **Installed JREs > Execution Environments**, associate it with `JavaSE-25` and `JavaSE-21`.
   The latter is used by `gama.annotations`, which retains a Java 21 compatibility target; JDK 25
   can compile for both environments. This does not change the Java runtime used by Eclipse itself
   or the annotation processor. Keep the projects' checked-in compiler settings.
4. Choose **Help > Install New Software...**, add the update site
   `https://gama-platform.org/gama.devtools/`, select **GAMA Developer Tools**, and restart Eclipse.
   Install the tools in the Eclipse IDE, not in a packaged GAMA application.

## Install and run the sources

1. Choose **GAMA > Install GAMA sources > Install core plugins...**. Select a checkout directory,
   repository URL, and branch; the defaults use this repository's `main` branch. The wizard can clone
   the repository or update an existing checkout, so a separate Git installation is not required.
   Existing checkouts must be clean and fast-forwardable: the wizard does not stash, reset, or overwrite
   local changes.
2. Review the project selection. Core projects listed in `gama.parent/pom.xml` are preselected unless
   already imported. Also select **`gama.annotations`** and **`gama.processor`** if they are not already
   in the workspace; they are needed for annotation processing and are not part of the parent reactor.
   Select **`gaml.grammar`** as well if you plan to regenerate the GAML grammar.
3. Choose the optional working sets and workspace settings. Importing the bundled preferences is opt-in
   and can change workspace compiler and formatting settings. The wizard recommends closing
   `gama.parent` to disable its Eclipse Maven compilation; this does not remove it from the checkout.
   Finish to import the projects in place and schedule a workspace refresh, clean, and full build.
4. After the build, use **GAMA > Check workspace...** to identify missing projects, target-platform
   bundles, plugin configuration issues, or a missing JDK 25. It can offer repairs for supported issues.
   The installer does not configure the JDK or resolve the PDE target platform automatically; review
   **Preferences > Plug-in Development > Target Platform** if dependencies are missing.
5. Choose **GAMA > Run GAMA > Run UI**. On first use, select an existing launch configuration or
   generate a default and choose the registered JDK 25. The same menu offers **Debug UI**, headless
   launches, and a test launch. The tools refresh the workspace before launching.

The **Install GAMA sources** menu also offers the graphical editor and community plugins. For details,
including repository and branch choices, see the
[GAMA Developer Tools guide](https://github.com/gama-platform/gama.devtools/blob/main/gama.ui.devtools/README.md).

## Find your way around

| Area | Projects |
|---|---|
| Language | [`gaml.grammar`](gaml.grammar/) defines the Xtext grammar; [`gaml.compiler`](gaml.compiler/) implements GAML compilation. |
| Shared contracts and runtime | [`gama.api`](gama.api/) provides shared interfaces and types; [`gama.core`](gama.core/) implements simulation, agents, spatial operations, and outputs. [`gama.workspace`](gama.workspace/) integrates workspace resources. |
| Generated language extensions | [`gama.annotations`](gama.annotations/) and [`gama.processor`](gama.processor/) define and process annotations used to register GAML elements. Generated `gaml/` sources should be regenerated rather than edited. |
| Desktop | `gama.ui.*` contains the Eclipse application, editor, navigator, experiment views, and Java2D/OpenGL displays. |
| Extensions and models | `gama.extension.*` adds domain capabilities; [`gama.library`](gama.library/) contains example and tutorial models. |
| Headless and distribution | [`gama.headless`](gama.headless/) runs experiments without the desktop; `gama.feature.*`, [`gama.product`](gama.product/), and [`gama.parent`](gama.parent/) assemble and build the distribution. |

Useful developer tools include **GAMA > Search GAML keyword...** to find Java implementations,
**GAMA > Create GAMA artefacts** for extension wizards, and **GAMA > Clean GAMA plugins** to repair
plugin settings and rebuild generated additions. Cleaning deletes the generated `gaml/` folders and
performs a workspace clean and full build. Use **GAMA > Repair GAMA plugins > Rebuild annotation processor**
after changing the processor or annotations.

## Build a local release

For a runnable build of the current checkout, choose **GAMA > Run GAMA > Build local release...**.
The tools download and manage Maven on first use, build for the current platform, and include JDK 25
in the resulting release. Internet access is needed for the initial download and uncached dependencies.
See the [tools guide](https://github.com/gama-platform/gama.devtools/blob/main/gama.ui.devtools/README.md#building-a-local-gama-release)
for supported platforms and build details.

Alternatively, install Maven and clone this repository yourself. Set `JAVA_HOME` to JDK 25 and check that
`mvn -version` reports Java 25. From the repository root, build the prerequisite projects before the parent:

```sh
mvn -f gama.annotations/pom.xml clean install
mvn -f gama.processor/pom.xml clean install
mvn -f gama.parent/pom.xml clean install
```

The resulting distribution is in `gama.product/target`. See the
[contribution guidelines](.github/CONTRIBUTING.md) before submitting changes.
