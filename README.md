# Official repository of the GAMA modeling platform
[![Continuous project validation](https://github.com/gama-platform/new.gama/actions/workflows/trigger-compilation.yaml/badge.svg)](https://github.com/gama-platform/gama/actions/workflows/trigger-compilation.yaml)
[![Language](https://img.shields.io/badge/language-java-brightgreen.svg)](https://www.java.com/)
[![GitHub issues](https://img.shields.io/github/issues/gama-platform/gama.svg)](https://github.com/gama-platform/gama/issues)
[![Github Releases](https://img.shields.io/github/release/gama-platform/gama.svg)](https://github.com/gama-platform/gama/releases)
[![Documentation](https://img.shields.io/badge/documentation-web-brightgreen.svg)](https://gama-platform.github.io)


[![CodeScene general](https://codescene.io/images/analyzed-by-codescene-badge.svg)](https://codescene.io/projects/51964)
[![CodeScene Average Code Health](https://codescene.io/projects/51964/status-badges/average-code-health)](https://codescene.io/projects/51964)


This is the official repository of the GAMA platform, an open-source modeling and simulation environment for creating spatially explicit agent-based simulations. 

**See the changelog on the [website](https://gama-platform.org/wiki/next/Changelog) for a detailed list of changes since version 1.9.0**  

## Installing GAMA

### Install a release
There's a detailed tutorial that covers all cases in the [documentation](https://gama-platform.org/wiki/Installation), but for most user you can just go straight to the [**releases**](https://github.com/gama-platform/gama/releases) section of this repository and download the version that corresponds to your needs, we recommend you take a version bundled with a JDK.

### Run it from the source code

To run it from the source code you can either use `maven` to build the program yourself, or run it from `eclipse` in case you want to inspect the code and/or modify it. In both cases you will first need to **clone this repository** and to get the [Temurin distribution of JDK21](https://adoptium.net).

#### Using eclipse

There is a more detailled explanation on the [website](https://gama-platform.org/wiki/InstallingGitVersion) about how to do so, here we will only give a fast and easy approach.

The _highly recommended configuration_ for working on this branch is [Eclipse for Java and DSL 2025-03](https://www.eclipse.org/downloads/packages/release/2025-03/r/eclipse-ide-java-and-dsl-developers), using a different version may expose you to some bugs.

Once you have imported the code base into your workspace, simply open the `gama.product` project and open either `gama.headless.product` or `gama.product` depending if you want to run gama with or without GUI.
In the `Overview` pane of the file, click on `Synchronize` and then `Launch an Eclipse application` and GAMA should start.

#### Using maven

First install [maven](https://maven.apache.org/) on your computer, then open a terminal at the root of project and follow those steps:
1. go to the project `gama.annotations`
2. run the command:
```bash
mvn clean install
```
3. go to the project `gama.processor`
4. run the same mvn command
5. go to the project `gama.parent`
6. run the same mvn command one last time

The produced release should be stored in the project `gama.product` under the `target` folder.

## Controlling GAMA from a WebSocket client

GAMA Server works in both the desktop GUI and headless mode. A `load` request can set experiment parameters **before initialization**, choose which console/status/dialog/runtime-error messages are sent to the client, and supply an `until` condition to stop execution. In GUI mode, displays and local output remain available.

Server exchange logging can be enabled for troubleshooting and is off by default. See the [GAMA Server guide](gama.headless/README.md) for [log locations and configuration](gama.headless/README.md#server-exchange-logs), enabling the server, all load options and defaults, and a complete model/client example.

## Pausing automatic editor validation

For large models, uncheck **Automatic validation** in the GAML editor's top-right **Editor options** menu to
pause semantic validation and documentation rebuilding. The setting is remembered per file in workspace metadata,
including after saving or reopening the file; parsing and syntax colouring remain active.
A grey circle on the navigator and editor-tab icons indicates that automatic validation is disabled and diagnostics
may be outdated. The two validation items are at the end of the menu. **Validate now**, available only while
automatic validation is disabled, checks the current editor content and refreshes its error annotations without
re-enabling automatic validation.
Running an experiment still compiles the model. Rechecking **Automatic validation** immediately validates and
rebuilds documentation, then resumes automatic updates.

## Exported models

The export bundle now provides a runtime-independent `.gamp` archive API, separate from the existing standalone
application exporter. `SimulationPackage.write(stagingDirectory, destination, manifest)` packages staged
`workspace/<project>/...` files, optional `plugins/*.jar`, and the existing
`configuration/gama-launcher.properties` / `configuration/gama-launcher-image` presentation assets.
Use `LauncherConfiguration.serialize()` to produce the launcher settings; package experiment selections use
`experimentName@Project/models/model.gaml`. Experiment names and model paths cannot contain the player's
reserved `@` and `#` separators in this initial format.

The UTF-8 `manifest.properties` declares format version 1, package identity/version, a minimum GAMA runtime version,
experiments, exact bundle requirements, and a generated SHA-256/size inventory of every payload file.
An empty bundle artifact denotes a dependency supplied by the host at the recorded version or newer. Embedded
plugin payloads still require an exact identity/version match. Eclipse `.metadata` and source symbolic links
are excluded by rejecting such files, rather than copying author-specific workspace state.
`SimulationPackage.inspect()` checks structure and declared limits without activating GAMA.
`SimulationPackage.extract()` checks runtime compatibility and payload integrity into a fresh private directory
under an application-owned session directory. The caller owns successful sessions and their eventual cleanup;
failed extraction cleans up only its newly created directory.
`SimulationLaunchContext.fromPackage()` supplies explicit content/workspace paths and experiment choices to the
player, without changing installation files or global preferences.

Authors use the existing **File > Export as standalone model...** command from a model editor or the corresponding
navigator export commands. These open the model export wizard by default; there is no separate package export
menu item. **Export application instead...** switches the same wizard to the full application exporter (including
its JDK/executable options), without losing the experiment selection or presentation settings. **Export package
instead...** switches back. The package dialog reuses the standalone export's experiment
selection, presentation image, title/description, Home option and default experiment, with a `.gamp` destination.
Package identity and version are internal metadata generated automatically; users do not need to enter them.
The default title omits the model's `.gaml` extension. Home and the launch overlay label experiments as
"Experiment Classic", for example, without showing internal project/model paths.
The player window uses the presentation title followed by " powered by GAMA", including while an experiment runs.
It does not include GAMA or a JDK.

The exporter copies the participating projects and statically discovered external data, materializes Eclipse linked
resources, and excludes direct `save ... to:` destinations from required input data when the same path is not also
referenced elsewhere as input. Output files do not need to exist before export; missing input data still causes an error.
It removes machine-specific link/filter metadata, preserves model text encoding through UTF-8 conversion and
explicit resource settings, and rewrites static file references without modifying the author's resources.
Shapefile companions and external directory trees are included. Files referenced only by dynamically constructed paths
must already be included in the exported projects. Empty external data directories and filesystem symbolic links are
not supported. Choose a destination outside the projects, linked resource folders, model data and GAMA installation.
Cancellation or an export failure leaves an existing destination archive unchanged.

Dependency controls are hidden for models that only use standard GAMA plugins. When extra plugins are needed,
**Review dependencies...** lists those extra dependencies and exact versions, including transitive OSGi
bundle and package wiring. Discovery errors remain visible. Required non-standard Java plugin JARs and their non-standard dependencies are included
automatically under `plugins/`, even when the exporter API is called without manually selected plugins. Only the required
dependency closure is included, not every optional plugin installed by the author. Standard GAMA plugins remain host
requirements. Standard membership comes from a build-time copy of the product's feature graph, not the author's
potentially extended installation; stock transitive libraries are explicitly declared in the standard dependency features.
Maven generates this metadata when building `gama.export`, so build that bundle before using package export in a development launch.
The review distinguishes standard requirements, automatically packaged plugins, and unsupported extras. Required
non-standard plugins that are directories, fragments, contain native code or nested JARs currently block package
export with an explicit reason, rather than silently requiring the recipient to install them. Build/install Java plugin
JARs or use the application exporter in those cases. A package cannot replace an already installed plugin, even with
the same version. Dependency information survives switching export formats; the application exporter remains available
when package dependency resolution fails.

In an installed product, opening a single `.gamp` while GAMA is closed prepares its private runtime **before
Equinox starts**, then runs the player in the original native launcher's JVM/process. The startup JAR delegates to
the standard Eclipse launcher after preparation; it does not fork another Java process or modify the installed app.
On macOS, a small Cocoa bridge collects Finder's initial open-document event before framework startup.
The package's existing title, description and optional illustration generate a personalized splash, with a
title-only presentation when there is no illustration, plus "Powered by GAMA". No additional export fields are required.
Player splashes do not draw Eclipse's startup progress bar or "Starting GAMA..." message over the artwork;
normal modeling startup retains its standard splash progress display.
The usual GAMA splash is not shown first. Home and automatic experiment startup retain the package's existing settings.

When GAMA is already running, **File > Open standalone model...**, application arguments and open-document events
prepare one additional isolated player; the running modeler is not converted into a player.
The same archive validation and preparation run without a confirmation dialog. Multiple/mixed startup documents and
document events received after the early collection window retain the pre-core router as a safe fallback.
Ordinary launches still use the modeling application; a late ordinary document is forwarded without reopening an
already handled package.
The macOS product declares the `.gamp` document type. The Windows installer registers an Open With entry and the
Linux Debian package ships a desktop handler and shared MIME definition, without overriding user-selected defaults.
These associations require a newly packaged installation; changing the source does not register an existing app.
Product assembly installs `gama-launcher.jar` and its build-generated bundle locations into GUI products only.
Local macOS assembly builds the universal Cocoa bridge with Clang. When products are assembled on Linux, the macOS
release workflow builds it before signing, so the resulting library is signed with the rest of GAMA. The installed
application's OS identity and icon remain GAMA's; package-specific assets are generated only in the private session.

The player uses a private Equinox configuration, bundle inventory, preference store, and workspace under
`~/.gama/package-sessions/`. Installed bundle paths are referenced without modifying the application. Required host
bundles must meet the declared minimum versions; newer GAMA releases and newer host bundles are assumed compatible.
Actual OSGi resolution and model compilation still report genuine incompatibilities. Package plugins cannot replace installed bundles. Additional Java-only
plugins are checked against their JAR manifests and resolved by the package bootstrap before GAMA core initialization.
Plugins contributing Eclipse extensions must declare `singleton:=true`. Fragments, native-code declarations,
native-library filenames, and nested JARs are rejected in this initial implementation.
The player's WebSocket server is disabled at startup so it does not compete with a running modeler's listening port.
Home and the player's launch overlay share one presentation component: title, experiment selector, description and
illustration retain the same styling and position when launching. Play becomes Cancel and launch output appears below
the presentation. Illustrations are scaled down to fit a 560 by 240 pixel area without changing their aspect ratio.
Normal modeling launches keep their existing launch overlay.
Player startup explicitly selects the Home perspective and opens its view before reporting readiness; an inherited
modeling layout cannot be mistaken for an already active Home screen based on cached perspective state.
Home hides the entire global toolbar strip, including modeler navigation, status, search and heap controls.
Leaving Home restores the global controls according to the experiment's controls setting; returning Home hides them again.

Private runtime folders, including `player.log`, are retained internally to avoid losing generated results.
There is no package history or session-management screen: users export archives wherever they choose and open them
directly. The player retains its existing **Save Simulation Data** action. Running players are protected by a lifetime
file lease and process identity; internal cleanup never follows symbolic links or removes unrelated/legacy unregistered
directories. Existing results are not deleted automatically.
Launching from an Eclipse development configuration without a native launcher is not supported.
Open packages only from authors you trust: models/plugins execute with the user's permissions as soon as the package
is opened, without a separate trust prompt. Checksums are integrity checks, not signatures or a sandbox.
These checks do not prove that plugin code is safe or portable. The existing standalone export remains
available.

The JDK-only regression entry points are `SimulationPackageTest`, `SimulationPackageLauncherTest`, `PackageProjectStagerTest`,
`PackageDocumentsTest`, `SimulationPackageSessionsTest`, `StandardDistributionTest`, `PackageAssociationTest`, and `PackageRegistrationTest`
(the last two run from the repository root). `SimulationPackageSmokeTest` is an opt-in GUI
check taking an installed Eclipse directory, native GAMA launcher, and scratch parent directory. An optional fourth
argument names this source repository and overlays its compiled application/shared-UI/export bundles into the scratch area,
without changing the installed product. The smoke check verifies early dependency resolution, a packaged GAML
language contribution, player workbench startup, model compilation, and experiment initialization.
Set the smoke runner's `-Dgama.smoke.home=true` to check Home-first startup without auto-running; also set
`-Dgama.smoke.export=true` to verify the actual active Home perspective, visible Home view and hidden editor area.
Set `-Dgama.smoke.unresolved=true` to check rejection of unresolved plugin dependencies before GAML initialization.
Set `-Dgama.smoke.export=true` to discover and export a model inside the running workbench, including linked resources,
external data and automatically embedded plugins (including an Import-Package provider), and reopen the resulting package
in another isolated player whose stock installation lacks those plugins. Unsupported extra dependencies must fail
without changing an existing destination archive. `StandardDistributionTest` optionally takes the repository root and
a stock installed Eclipse directory to check that its bundle inventory contains no undeclared standard dependencies.
`PackageCompatibilityTest` covers minimum runtime/host versions, numeric version ordering and exact embedded payload identities.
Also set `-Dgama.smoke.ui=true` after compiling `LauncherHomeTest` and `LaunchPresentationTest` onto the smoke runner's
classpath to exercise the wizard's format switch and shared Home/launch geometry, typography, images and cancellation
inside the real workbench, together with the dependency dialog. Test classes are injected only
into the scratch overlays. `ColdStartSmokeTest` takes the same four arguments and checks package arguments, native
document-event routing without confirmation, invalid archives, normal startup and a late ordinary-document handoff.
Its native event delivery is simulated in SWT; Finder, Windows and Linux integration, signed distribution launches,
and cross-platform execution still require checks on the actual packaged products.
`PackageLauncherTest` covers pre-framework argument parsing and Windows/Linux splash rendering using only the JDK
and Eclipse launcher JAR. `SingleProcessSmokeTest` takes the same four arguments and assembles an isolated native
macOS test app, verifying that the original PID reaches the player, with embedded plugins resolved before GAML,
and no child JVM. `-Dgama.smoke.finder=true` uses a real Launch Services open-document event instead of command-line
arguments; `-Dgama.smoke.home=true` also checks the actual Home perspective, view and hidden editor.
The package plugin checks the native splash window handle and selected image path, not just the generated file.
`-Dgama.smoke.image=true` exercises the illustration. `-Dgama.smoke.prepared=true` checks a preprepared player;
`-Dgama.smoke.invalid=true` checks rejection before framework/GAMA startup.
With `-Dgama.smoke.finder=true -Dgama.smoke.normal=true -Dgama.smoke.live=true`, it runs the real modeler workbench
(bypassing the interactive workspace picker in the test app only), opens a package through Launch Services, and
checks that the modeler stays active while one isolated player opens with its own splash.
Compile `LauncherInstaller`, `SimulationPackageSmokeTest`, `SingleProcessSmokeTest`, `NormalStartupProbe` and
`NativeModelerProbe` on the smoke runner's classpath. Neither the installed product nor the user's sessions are changed.

## Reporting problems, bugs and issues

If you spot a bug or want to suggest an improvement to GAMA, please do so by posting an issue here: [https://github.com/gama-platform/gama/issues](https://github.com/gama-platform/gama/issues). 

If you noticed something wrong/not up-to-date or a lack of information on something on the website and the documentation you could create an issue on the website's dedicated repository here: [https://github.com/gama-platform/gama-platform.github.io/issues](https://github.com/gama-platform/gama-platform.github.io/issues)

For general questions about modelling/development on GAMA you can either ask them in the [discussions](https://github.com/gama-platform/gama/discussions) of this repository or on the [mailing list](https://groups.google.com/g/gama-platform)

## Contributing

There are a few tutorials explaining how to create new functionalities in gama that you can find in the [developing extensions](https://gama-platform.org/wiki/DevelopingExtensions) section of the website, and one general explanation of the architecture of the software and important types used in the section [Introduction to GAMA Java API](https://gama-platform.org/wiki/Introduction-To-Gama-Java-API).

To contribute to the code you just have to submit a pull-request to this repository, if you encounter any problem to do so, feel free to ask about it either on the [github discussions](https://github.com/gama-platform/gama/discussions) or the [mailing list](https://groups.google.com/g/gama-platform).

 ## Acknowledgments

 ![YourKit logo](https://www.yourkit.com/images/yk_logo.svg) GAMA is actively supported by YourKit LLC, with its full-featured <a href="https://www.yourkit.com/java/profiler/index.jsp">Java Profiler</a>.


 
 

  
  
  
