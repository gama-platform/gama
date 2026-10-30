# Models, projects, packages and file workflows

Source audit dated 2026-10-30, updated against `6313ef172` and the local working tree.
This includes the `.gamx` playable-model rename, standard-plugin classification
and automatic dependency packaging changes. Those changes are described as observed in the source,
not as features verified in an already published release.

This is a code-path audit, not a cross-platform execution report. Inherited
Eclipse dialogs are identified as such; OS associations depend on the installed
product and the user's selected default application. No runtime behavior is
changed by this document. Sections explicitly marked **Proposal** are not
implemented features.

## Summary

GAMA does **not** currently have one unified open/import/export pipeline.
There are three main systems:

1. Eclipse resource/editor operations for editing and transferring files.
2. `WorkspaceModelsManager` for opening models supplied by the OS or arguments.
3. Simulation export and package startup for distributing runnable experiments.

There is useful sharing inside the third system: package and application export
share model discovery, experiment selection, presentation configuration and some
path rewriting. Package opening also shares validation and player preparation.
That sharing does not extend to ordinary project import/export or navigator drops.

**`.gamx` (GAMA executable model) is the runnable simulation package.
`.gamp` is reserved for future editable project archives, not yet implemented.**
A `.gamx` contains source projects, but its supported Open action
starts a player rather than importing those projects into the modeling workspace.
The missing distinction is not ZIP versus another compression format: `.gamx`
itself is ZIP-based. It is the document contract, metadata and opening action.

Previously exported playable `.gamp` archives can be renamed to `.gamx`: the
internal version-1 archive format is unchanged. Current export dialogs, document
routing, navigator associations and OS registrations use `.gamx`, not `.gamp`;
there is no automatic legacy-suffix playback alias. Reserving `.gamp` does not
yet provide project export/import or double-click handling for that extension.

## 1. Current operations

| Entry point | Actual operation | Dependencies and ownership |
|---|---|---|
| Save / Save All | Standard workbench/editor saving. GAML Save optionally formats the document first. | Saves editor resources, not a portable project or dependency manifest. Saving a linked resource writes its external target. |
| Save As | GAML calls the inherited Xtext Save As implementation after the same optional formatting. | A document operation, not a project clone, dependency relocation or package export. Destination UI is inherited from Xtext/Eclipse. |
| Open/double-click a workspace model | Navigator uses `IDE.openEditor` and the editor registry. | Edits the existing workspace resource. A workspace `.experiment` opens in the editor; this is different from the argument-opening route below. |
| Open a model through OS events or GUI application arguments | `Application.openDocument` delegates non-`.gamx` paths to `WorkspaceModelsManager`. | May register a containing project, or import one file as a copy/link. `#experimentName` requests execution; `.experiment` without a selector requests experiment `"0"`. |
| `FileOpen` handler class | Contains a `.gaml` / `.experiment` / All Files picker and calls the same model manager. | All Files also allows the recognized `.model` / `.species` extensions; unrelated extensions are ignored. No registration/reference for this class was found in the inspected UI wiring; do not assume there is a supported File > Open model dialog just because this class exists. |
| Import > Project | Custom project-import wizard searches a directory or archive for `.project` descriptions. | Directory projects can be copied or registered at their existing location. The copy setting is remembered and initially false. Archive projects are extracted into the workspace. No package/plugin manifest processing. |
| Import > From disk | Thin subclass of Eclipse's filesystem import wizard. | Resource transfer into a project/container, not model dependency discovery. |
| Import > From archive | Thin subclass of Eclipse's ZIP import wizard. | Extracts selected resources; does not establish a runnable package environment. |
| Export > Project | GAMA wrapper around Eclipse's archive-resource export page. | An archive of selected resources, not a GAMA dependency-aware project format. Project completeness depends on selection; no automatic plugin closure. |
| External files to archive | Thin subclass of Eclipse's ZIP export wizard. | Generic archive export; substantially overlaps the project-export implementation. |
| Export as playable model | Shared simulation-export handler/dialog, defaulting to `.gamx`. | Compiles models, discovers projects/data/plugins, selects experiments, stages a portable payload. Does not include GAMA or a JDK. |
| Export application instead | Switches the same dialog to the full application exporter. | Builds an application distribution with runtime and optional JDK/executable options. Uses a different archive-building implementation. |
| Open playable model / navigator `.gamx` double-click | Validates and prepares an isolated player. | Does not import into the modeling workspace or install plugins into the running modeler. |
| Save Simulation Data | Copies recently modified files from the active simulation's project to a destination directory. | Output recovery, not Save Project, Save Package or a complete simulation checkpoint. |
| GAML `save` and serialized simulations | Runtime data writers and serialization delegates. | Different contracts again: output data or agent/simulation state, not portable source-project distribution. |

Primary sources: [editor saving][editor-save], [navigator opening][navigator-open],
[argument model opening][model-opening], [file picker][file-picker],
[wizard registrations][wizards], [project export][project-export],
[project import][project-import], [simulation export][simulation-export],
[output saving][artifacts], [serialization][serialization].

### Directory import, copy and link are different

Registering an external project means that its files remain outside the physical
workspace directory. Copying a project creates another set of files. Linking an
individual file creates a workspace resource referring to the original file.
These are not interchangeable, particularly when the original is later moved,
deleted or saved through GAMA.

The project-import wizard offers a destination group and asks for another project
name on conflicts. A renamed directory import is forced to copy rather than
rename the original. It is not a general cross-project-reference migration tool.
Project-name changes need special care when models use workspace-relative paths.

## 2. What happens when an external `.gaml` is double-clicked?

Assuming the OS sends the document to this GAMA installation:

1. GAMA starts the modeler if needed, or queues an OpenDocument event in an
   existing workbench. The non-package route reaches
   `WorkspaceModelsManager.openModelPassedAsArgument`.
2. GAMA first tries to resolve a workspace file from the path.
3. If not found, it walks up the filesystem parents looking for `.project`.
4. If a containing project is found, the code attempts to register/open that
   project and then locate the model as a workspace resource. This is not a
   deliberate copy-to-workspace workflow and does not ask the normal copy/link
   question.
5. If no containing project is found, GAMA informs the user, asks **copy or
   link**, then opens a project/folder selection dialog. If that selection returns
   null, it falls back to `Unclassified Models/models`; it does not cancel the
   entire operation.
6. Only the selected file is copied or linked. Imported GAML files, relative data
   directories and plugins are not collected with it.
7. GAMA touches the file and performs a full project build, then opens the model
   editor. A `#experimentName` selector runs the experiment instead.

Consequences:

- **A loose `.gaml` is not a self-contained deliverable.** Copying it can break
  imports and relative data paths. A link preserves the original file, but is
  still not a portable project or a guarantee that every relative dependency
  resolves correctly.
- **Save may change the original outside the workspace** when the resource is a
  link or belongs to a project registered at an external location.
- `.experiment` has inconsistent default intent: navigator Open uses its editor
  association, but argument/OS opening defaults to running experiment `"0"`.
- The external-project branch has collision and closed-project problems described
  below. It should not be documented as an unconditional, safe project import.

There is also a separate direct-filesystem opening path:
`FileOpener.openFileInFileSystem` uses Eclipse's file-store editor API, without
the above project-import interaction. Import hyperlinks and GUI edit requests can
reach `FileOpener`. Consequently, even internal requests to "open a file" do not
all have the same workspace-registration behavior. See [file-store opening][file-store].

## 3. Drag-and-drop and paste

The drop target changes the meaning of the operation:

| Target / route | Current meaning |
|---|---|
| Navigator root | `NavigatorDropAssistant` calls `PasteAction.handlePaste`. A recognized project directory is copied into a project named after the directory, excluding the source `.project`; other directories/files are copied into a selected/fallback container. |
| Existing navigator project/folder | `NavigatorResourceDropAssistant` uses Eclipse copy/link operations according to the requested drop operation. |
| Paste filesystem directories into a user group | `UserGroups.importDirectories` copies recognized project directories, asks about name conflicts and registers the copies. Non-project inputs are skipped by this helper. |
| GAML editor text area | External files are reused if already registered, otherwise linked under the editor project's `external/` folder. Model resources generate import statements; other file resources generate file declarations. This edits the current model rather than opening/importing a project. |

These routes do not implement a common archive classifier. In particular,
dropping a `.gamx` onto an existing project/folder transfers the archive as a
resource; it does not invoke the player or import its projects. Opening that
resource afterwards uses the `.gamx` player association.

The root-paste project helper also does not use the project-import wizard's
conflict-resolution policy: it calls `createOrUpdateProject` and copies content.
The precise operation available on virtual/group targets depends on their drop
assistant and adaptation; not every visually similar navigator area is an
equivalent drop target.

Sources: [root drop][root-drop], [root paste][paste], [resource drop][resource-drop],
[group import][group-import], [editor drop][editor-drop], [external links][external-links].

## 4. What `.gamx` already provides

The version-1 layout is:

```text
Example.gamx                         ZIP container
  manifest.properties                format, identity, runtime, experiments,
                                     bundles, payload sizes and SHA-256 hashes
  workspace/Project/.project
  workspace/Project/models/...
  workspace/Project/...              project resources and relocated data
  plugins/extra.plugin_version.jar   optional non-standard Java plugins
  configuration/gama-launcher.properties
  configuration/gama-launcher-image  optional presentation image
```

The exporter compiles selected roots, follows model imports and collects plugin
metadata and static data references. It includes participating projects, not just
the single selected model. The selected experiment list is a launcher list, not
an access-control mechanism over the included source.

The package stager materializes Eclipse linked resources, removes link/filter
metadata from `.project`, converts model text to UTF-8, and rewrites static file
references in the staged copies. It includes external data directory trees and
shapefile companions. Output-only direct `save ... to:` paths are not required
inputs. Dynamically computed paths cannot in general be discovered; their inputs
must already be present in the exported project resources. Filesystem symlinks
and empty external data directories are unsupported.

The codec validates layout, portable names and declared limits. Inspection does
not load package code; extraction additionally checks each payload's size and
hash. Hashes establish consistency, **not publisher authenticity or a sandbox**.
The default bounds are 10,000 payload files, 2 GiB per file, 8 GiB total and a
1 MiB manifest.

### Plugins

In the audited local tree, required non-standard plugin JARs and their non-standard
OSGi wiring dependencies are included automatically. Standard GAMA bundles are
host requirements, not embedded copies. Standard membership uses distribution
extension declarations plus the dependency feature inventories, rather than
treating every installed author plugin as standard.

There are important limits:

- Directory plugins, fragments, native code and nested JARs are rejected.
- A package cannot replace an already installed bundle, even at the same version.
- Host bundle requirements accept the recorded version or newer; embedded bundles
  require the exact declared identity/version. The runtime has a minimum-version
  check, not a guarantee of compatibility with every future release.
- Export requires compilable models and at least one experiment. The current
  manifest and codec also require experiments and launcher configuration.
  An incomplete source project or a reusable model library without experiments
  is therefore not representable as a general project-only package.

### Opening and persistence

An installed product can prepare a single cold-start package before Equinox and
run the player in the native launcher's original JVM. A running modeler opens a
separate player process. Multiple/mixed or late startup documents use the
document-router fallback rather than changing the running modeler's plugins.

Each package opening uses fresh content, workspace, preference and runtime
configuration directories under `~/.gama/package-sessions/`. The source archive
is not edited. Sessions and generated results are retained, but there is no
session-history/recovery UI. Save Simulation Data uses modification times and
only the active model's project; it does not collect every output from every
participating project or arbitrary external output path.

There is **no supported Import package for editing action**. A ZIP utility can
recover source projects, and a legacy project importer may recognize `.project`
entries if it accepts the archive, but that is not manifest-aware import:
package validation, plugin handling, runtime requirements and launcher intent are
not integrated into that route.

Sources: [model discovery][discovery], [package export][package-export],
[staging][staging], [manifest][manifest], [codec][codec], [player preparation][player],
[opening service][package-opening], [sessions][sessions], [output scope][output-scope].

## 5. OS associations, arguments and installation drop-ins

macOS declares `.gaml` and `.experiment` as editor documents and `.gamx` as a
viewer document. Windows registers Open With handlers; Linux's Debian packaging
installs a desktop handler and MIME descriptions. User-selected defaults still
apply. These registrations require an assembled/installed product; an Eclipse
development launch or an old installation does not acquire them from source edits.
Package launching through the UI explicitly requires an installed native launcher.
The current UI labels are **Open playable model...**, **Export as playable
model...** and **Properties > Playable model**. The reserved `.gamp` extension
has no playable-model registration; old playable archives need the `.gamx` suffix.

Argument parsing is only partly unified:

- The pre-Equinox launcher and `PackageDocuments` each parse document arguments.
  They have different option-value lists.
- `Application.openDocument` is a useful shared package/model dispatch point,
  but ordinary `ApplicationWorkbenchAdvisor.postStartup` still reads raw
  application arguments and opens only the last one.
- Thus `Gama A.gamx B.gaml` can reach the modeler and open only `B.gaml` on this
  route; the initially classified package list is not universally dispatched.
  Native document events, by contrast, are queued and processed individually.
- `model.gaml#experimentName` is handled by the model manager. Its `#` parsing is
  not a structured document-request representation.
- The advisor retains a special `.gamr` launcher-delegate branch. It is separate
  from package handling; the inspected serialization implementation uses saved
  agent/simulation formats, including `.gsim`, rather than treating `.gamr` as
  a general project archive.
- Headless startup has its own XML/batch/`-gaml`/server argument processing. It
  does not expose the GUI package-opening service as a headless `.gamx` runner.

Installation `dropins/` is yet another concept, **not drag-and-drop import**.
The regular product includes the p2 drop-ins reconciler and plugin-install
commands. This is installation-level provisioning. A package carries `plugins/`
and builds an explicit private bundle inventory; player preparation disables
drop-ins reconciliation instead of copying package plugins into the installation.

Sources: [native launcher][native-launcher], [document classification][documents],
[document router][router], [workbench arguments][arguments], [OS declarations][os-mac],
[Windows registration][os-windows], [Linux handler][os-linux], [headless][headless],
[product provisioning][product], [plugin installation][plugin-install].

## 6. Findings to address before presenting one coherent workflow

| Priority | Finding | Evidence / consequence |
|---|---|---|
| First | External opening has a destructive closed-project branch. | `WorkspaceModelsManager.findOutsideWorkspace` calls `proj.delete(true, null)` when the selected existing project is inaccessible, then recreates it. `true` requests deletion of project content; opening a document must never use this as a reopen strategy. [Source][external-project] |
| First | External project identity uses directory names and description names inconsistently. | The handle uses the parent directory's name; conflict checking separately uses `.project`'s name. Existing handles are not checked against the requested physical location before reuse. This can fail or select the wrong registered project. [Source][external-project] |
| First | The external-name-conflict fallback discards its returned resource. | It calls `createUnclassifiedModelsProjectAndAdd`, returns only from the workspace operation, then tries to locate the file in the original project handle. Import can therefore succeed without the requested file being opened. [Source][external-project] |
| First | Missing files/build failures can end without a useful user-facing error. | The model-opening route has null/debug-only exits; it does not share the package opener's explicit failure reporting. [Source][model-opening] |
| Next | Copy/link/cancel/collision policies differ by entry point. | Loose-file opening, project import, root paste, group import and resource drop each own policy. A cancelled container selector can fall back to Unclassified Models rather than cancel. |
| Next | Multiple file arguments and type routing differ by entry point. | Startup's last-argument handling, native event queues, package routing, file-picker filtering and navigator editor associations are not one contract. |
| Next | No dependency-aware editable-project archive workflow. | Legacy project ZIP export has no dependency closure; `.gamx` has no supported modeling import and requires runnable experiments. `.gamp` is reserved, not implemented. |
| Next | Export does not always snapshot every dirty participating editor. | Editor-triggered export saves the active editor, but saving all others depends on `EDITOR_SAVE`. Navigator-triggered export has only that optional Save All. Dependency discovery and staging can therefore use disk contents older than visible edits. [Source][simulation-export] |
| Later | Runtime packaging still has two staging/dependency implementations. | `.gamx` uses `PackageProjectStager` and package dependency closure; application export uses `GamaZipBuilder` and `BundleDependencyAnalyzer`. Shared discovery does not imply identical portability guarantees. |
| Later | "Save Simulation Data" sounds broader than its implementation. | Modification-time copying of the active project's files is neither a complete result inventory nor resumable simulation state. |

These are source findings, not claims that each scenario was reproduced through
all OS dialogs. The closed-project branch warrants a focused reproduction in a
disposable workspace before any further use of that path.

## 7. Proposal: unify contracts, not all implementations

### Keep four explicit user intentions

| Intention | Recommended UI wording | Contract |
|---|---|---|
| Edit existing content | Open model / Open project | Use existing resources. Ask explicitly before registering external content, and clearly identify edits to originals. |
| Acquire an editable copy | Import project | Copy into a chosen workspace/group, preview dependencies and resolve collisions before writing. |
| Distribute something runnable | Export as playable model | Current `.gamx` behavior; full application export remains an alternative. |
| Preserve generated results | Export simulation results | Output collection, separate from editor Save and simulation checkpoints. |

Retain generic Import files / Export files as advanced resource-transfer tools;
do not make them look equivalent to project-aware import/export.

### One document-dispatch contract

Introduce one shared classification/request layer for OS events, command-line
documents, Open dialogs and package-aware drop targets. It should resolve
canonical physical locations against existing workspace resources, then return
an explicit intent: edit, register in place, copy/import, run package or transfer
ordinary data.

Keep the pre-Equinox bootstrap small: classification and preparation must be usable
without activating the modeler. A shared parser/descriptor can be used there and
in the UI; the UI service itself must not be loaded before framework startup.
The editor-text drop remains intentionally different because it inserts imports
or declarations; it should use shared resource-resolution policy, not become an
Open action.

For loose models, offer an explicit choice of opening/registering the containing
project, importing a project copy, importing only the file with a dependency
warning, or cancelling. A temporary project for a truly loose file is a possible
convenience, not an implemented behavior. Never delete existing content to open
a document, and make every cancellation terminate the request.

### A standard editable project archive

**Recommendation: implement the reserved `.gamp` editable-project format using
the `.gamx` package infrastructure, rather than introduce another unrelated ZIP
importer. Keep the two document intents explicit.**

The v1 schema cannot simply be relabeled: it requires experiments and launcher
settings. Define a versioned project schema/profile that can represent a
project-only package, while continuing to read existing v1 `.gamx` packages as
runnable simulations:

| Profile | Default double-click action | Additional action |
|---|---|---|
| Playable model, `.gamx` including existing v1 archives | Run in isolated player | Import source projects for editing |
| Editable project, reserved `.gamp` | Preview and import/copy projects | Run an available experiment after import |

The extension split is now established: `.gamx` means play, `.gamp` means an
editable project archive once implemented. Reuse shared validation and archive
primitives, with explicit schema/profile checks, rather than duplicate the codec.
Do not register `.gamp` as a player alias. Existing `.gamx` double-click behavior
must remain unchanged; migration of old playable `.gamp` files is by renaming.

A project profile should support one or more complete source projects, including
non-compiling work in progress or model libraries without experiments. It needs
an explicit content-selection policy for generated outputs and author-local files.
Reuse portable path validation, payload inventory, safe staging, dependency
declarations and archive inspection; adapt experiment requirements and compilation-
based dependency discovery instead of imposing them on every project backup.
For uncompiled projects, dependencies must be declared or reported unresolved,
not presented as successfully inferred.

Import should validate into staging, preview projects and requirements, establish
a collision/rename plan, and only then copy/register projects. Renaming requires
consistent updates to project references, workspace-relative model imports and
entry-point paths; changing only `.project` names is insufficient. Archives
must never supply Eclipse `.metadata` or overwrite existing projects implicitly.

Package Java plugins must **not** be silently installed into a running modeling
session. Offer source-only import with explicit missing-dependency diagnostics,
deliberate installation through normal provisioning when compatible, or an
isolated authoring process using the package runtime. The last option is needed
when host and package plugins conflict; player isolation does not automatically
provide an authoring UI.

The early launcher currently recognizes `.gamx` as runnable and excludes `.gamp`
from package classification. Future `.gamp` handling must route to modeling import,
not the player, and validate the project schema/profile before modifying the
workspace. Add consistent cold-start, running-modeler, dialog and OS handlers
together; merely adding an Import wizard would leave double-click unsupported.

### Delivery order

1. Fix external-opening correctness first: non-destructive reopen, canonical
   identity checks, preserved fallback results, meaningful errors and real cancel.
2. Unify document parsing/dispatch and copy/link/conflict policies. Process all
   document arguments once; distinguish Edit from Run for `.experiment`.
3. Add validated **Import source projects** for existing runnable `.gamx` files,
   with an explicit plugin strategy. Preserve existing player opening.
4. Implement the reserved `.gamp` editable-project profile, versioned schema,
   export and default import action, then make it the primary project-export
   choice. Keep legacy ZIP import.
5. Consolidate portable staging/dependency planning where application and package
   export can actually share it; define dirty-editor snapshot policy and clearer
   result-export/recovery behavior.

Acceptance scenarios should cover fresh/existing workspaces, open/closed and
same-name projects, directory/description name mismatch, copy/link/cancel,
multiple/mixed arguments, model/data relative paths, missing/conflicting plugins,
project-only archives, dirty editors, old playable `.gamp` files renamed to
`.gamx`, reserved-suffix routing, and cold/running GAMA on each supported OS.

Until that work is implemented, the practical rule is: **Save for edits, legacy
project archives for editable exchange with separately managed dependencies,
`.gamx` for runnable distribution, and Save Simulation Data for selected player
outputs. `.gamp` remains reserved for future editable project archives.
Do not promise that opening, dropping and importing are synonyms.**

## Source index

Paths and line ranges refer to the audited working tree. They are implementation
anchors, not a substitute for the status/proposal distinction above.

[editor-save]: gama.ui.editor/src/gaml/compiler/ui/editor/GamlEditor.java#L838-L861
[navigator-open]: gama.ui.navigator/src/gama/ui/navigator/view/actions/OpenFileAction.java#L95-L138
[model-opening]: gama.workspace/src/gama/workspace/manager/WorkspaceModelsManager.java#L115-L306
[external-project]: gama.workspace/src/gama/workspace/manager/WorkspaceModelsManager.java#L247-L306
[file-picker]: gama.ui.navigator/src/gama/ui/navigator/commands/FileOpen.java#L27-L44
[file-store]: gama.ui.shared/src/gama/ui/shared/utils/FileOpener.java#L44-L108
[wizards]: gama.ui.navigator/plugin.xml#L861-L944
[project-export]: gama.ui.navigator/src/gama/ui/navigator/wizards/ExportProjectWizardPage.java#L109-L123
[project-import]: gama.ui.navigator/src/gama/ui/navigator/wizards/ImportProjectWizardPage.java#L1127-L1320
[simulation-export]: gama.ui.export/src/gama/ui/export/commands/ExportSimulation.java#L35-L145
[root-drop]: gama.ui.navigator/src/gama/ui/navigator/view/NavigatorDropAssistant.java#L37-L59
[paste]: gama.ui.navigator/src/gama/ui/navigator/view/actions/PasteAction.java#L289-L335
[resource-drop]: gama.ui.navigator/src/gama/ui/navigator/view/NavigatorResourceDropAssistant.java#L527-L546
[group-import]: gama.ui.navigator/src/gama/ui/navigator/view/contents/UserGroups.java#L235-L261
[editor-drop]: gama.ui.editor/src/gaml/compiler/ui/editor/GamlEditorDragAndDropHandler.java#L336-L382
[external-links]: gama.api/src/gama/api/utils/files/FileUtils.java#L390-L413
[discovery]: gama.export/src/gama/export/ExportModels.java#L103-L163
[package-export]: gama.export/src/gama/export/SimulationPackageExporter.java#L39-L224
[staging]: gama.export/src/gama/export/PackageProjectStager.java#L39-L215
[manifest]: gama.export/src/gama/export/SimulationPackageManifest.java#L9-L151
[codec]: gama.export/src/gama/export/SimulationPackage.java#L29-L284
[player]: gama.export/src/gama/export/SimulationPackageLauncher.java#L51-L129
[package-opening]: gama.export/src/gama/export/SimulationPackageOpening.java#L23-L63
[sessions]: gama.export/src/gama/export/SimulationPackageSessions.java#L20-L115
[artifacts]: gama.export/src/gama/export/SimulationArtifacts.java#L13-L53
[output-scope]: gama.ui.export/src/gama/ui/export/launcher/StartupModelHelper.java#L114-L138
[serialization]: gama.extension.serialize/src/gama/extension/serialize/gaml/SimulationSaveDelegate.java#L38-L55
[native-launcher]: gama.product/launcher/src/gama/launcher/PackageLauncher.java#L23-L104
[documents]: gama.export/src/gama/export/PackageDocuments.java#L12-L41
[router]: gama.export/src/gama/export/DocumentApplication.java#L28-L82
[arguments]: gama.ui.application/src/gama/ui/application/workbench/ApplicationWorkbenchAdvisor.java#L98-L145
[os-mac]: gama.product/extraresources/Info.plist#L10-L104
[os-windows]: gama.product/extraresources/installer/windows/windows_installer_script.iss#L90-L106
[os-linux]: gama.product/extraresources/installer/unix/gama-platform.desktop
[headless]: gama.headless/src/gama/headless/runtime/HeadlessApplication.java#L423-L482
[product]: gama.product/gama.product#L123-L129
[plugin-install]: gama.ui.shared/src/gama/ui/shared/commands/InstallHandler.java#L24-L32
