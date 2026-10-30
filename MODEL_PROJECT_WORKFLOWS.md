# Models, projects, packages and file workflows

Source audit dated 2026-10-30, updated after the editable-project archive implementation.
This includes `.gamx` playable models and the new `.gamp` editable-project archive implementation,
standard-plugin classification and dependency packaging. Those changes are described as observed in the source,
not as features verified in an already published release.

This is a code-path audit, not a cross-platform execution report. Inherited
Eclipse dialogs are identified as such; OS associations depend on the installed
product and the user's selected default application. This document distinguishes implemented
working-tree behavior from remaining proposals; it is not an installed-product acceptance report.

## Summary

GAMA does **not** currently have one unified open/import/export pipeline.
There are three main systems:

1. Eclipse resource/editor operations for editing and transferring files.
2. `WorkspaceModelsManager` for opening models supplied by the OS or arguments.
3. Simulation export and package startup for distributing runnable experiments.

There is useful sharing inside the third system: package and application export
share model discovery, experiment selection, presentation configuration and some
path rewriting. Package opening also shares validation and player preparation.
Editable project archives now reuse package path validation and plugin dependency discovery,
but keep their own project-only manifest and workspace-import transaction. Ordinary file transfers
and loose-model opening remain separate.

**`.gamx` (GAMA executable model) is the runnable simulation package.
`.gamp` is the editable project archive, with a default preview/import action.**
A `.gamx` contains source projects, but its supported Open action
starts a player rather than importing those projects into the modeling workspace.
The missing distinction is not ZIP versus another compression format: `.gamx`
itself is ZIP-based. It is the document contract, metadata and opening action.

Previously exported playable `.gamp` archives can be renamed to `.gamx`: the
internal version-1 archive format is unchanged. Current export dialogs, document
routing and registrations distinguish playback (`.gamx`) from project import (`.gamp`);
there is no automatic legacy-suffix playback alias.

### Which operation should I use?

| What you want | Use | What happens |
|---|---|---|
| Keep edits to your current model | Save / Save All | Writes current resources; linked or externally registered resources can modify originals outside the workspace. |
| Share a complete editable project | Export > Project (`.gamp`) | Saves the whole project, with optional linked-resource targets and non-standard plugins. |
| Bring an editable project into your workspace | Import GAMA project (`.gamp` or project ZIP) | Previews contents, chooses a group, resolves name conflicts, and copies projects into the workspace. |
| Share experiments for playback | Export as playable model (`.gamx`) | Builds an isolated-player package rather than a normal workspace import. |
| Run a received playable model | Open `.gamx` / navigator Play | Starts the player; does not install its plugins into the modeler. |
| Work directly on an existing external project folder | Existing project folder (advanced) | Can register the original location instead of copying it; saves may modify those original files. |
| Transfer individual files | Import from disk/archive, or resource copy/link | Transfers resources without collecting a model's dependencies. |
| Recover generated simulation output | Save prompt when closing the player | Saves new or modified workspace files as ZIP, not the complete project or a resumable checkpoint. |

**Implementation status:** the affected archive and UI bundles compile, and product
descriptors have been checked. No tests were created or run. Live import/export
dialogs, plugin activation after restart and native OS double-click behavior have
not been exercised. OS icons and associations require a rebuilt/installed product;
editing these sources does not update the current installation's registrations.

## 1. Current operations

| Entry point | Actual operation | Dependencies and ownership |
|---|---|---|
| Save / Save All | Standard workbench/editor saving. GAML Save optionally formats the document first. | Saves editor resources, not a portable project or dependency manifest. Saving a linked resource writes its external target. |
| Save As | GAML calls the inherited Xtext Save As implementation after the same optional formatting. | A document operation, not a project clone, dependency relocation or package export. Destination UI is inherited from Xtext/Eclipse. |
| Open/double-click a workspace model | Navigator uses `IDE.openEditor` and the editor registry. | Edits the existing workspace resource. A workspace `.experiment` opens in the editor; this is different from the argument-opening route below. |
| Open through OS events or GUI application arguments | Shared dispatch sends `.gamx` to playback, `.gamp`/project ZIPs to import, and model paths to `WorkspaceModelsManager`. | The model route may register a containing project or copy/link one file. `#experimentName` requests execution; `.experiment` without a selector requests experiment `"0"`. |
| `FileOpen` handler class | Contains a `.gaml` / `.experiment` / All Files picker and calls the same model manager. | All Files also allows the recognized `.model` / `.species` extensions; unrelated extensions are ignored. No registration/reference for this class was found in the inspected UI wiring; do not assume there is a supported File > Open model dialog just because this class exists. |
| Import > Project / File > Import GAMA project | Shared `.gamp`/legacy ZIP importer: preview, group, collision plan, validated staging, workspace copy. | `.gamp` validates its manifest and payload paths, without hash or exact-size verification. Extraction limits remain enforced. Plugin installation is opt-in. Advanced project-folder import retains copy/register-in-place behavior. |
| Import > From disk | Thin subclass of Eclipse's filesystem import wizard. | Resource transfer into a project/container, not model dependency discovery. |
| Import > From archive | Thin subclass of Eclipse's ZIP import wizard. | Extracts selected resources; does not establish a runnable package environment. |
| Export > Project | Compact whole-project save dialog, default `[Project Name].gamp`. | Optional resolved links and required non-standard plugins; no file tree or experiment requirement. Saves dirty editors with consent first. |
| External files to archive | Thin subclass of Eclipse's ZIP export wizard. | Advanced selected-resource export; unlike `.gamp`, it does not provide project metadata or dependency handling. |
| Export as playable model | Shared simulation-export handler/dialog, defaulting to `.gamx`. | Compiles models, discovers projects/data/plugins, selects experiments, stages a portable payload. Does not include GAMA or a JDK. |
| Export application instead | Switches the same dialog to the full application exporter. | Builds an application distribution with runtime and optional JDK/executable options. Uses a different archive-building implementation. |
| Open playable model / navigator `.gamx` double-click | Validates and prepares an isolated player. | Does not import into the modeling workspace or install plugins into the running modeler. |
| Save data when closing the player | Native Save dialog with a model-title/date ZIP name; saves new or modified files across the player workspace, excluding internal metadata. | Output recovery, not Save Project, Save Package or a complete simulation checkpoint. Canceling keeps the player open. |
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

The advanced directory-project import wizard offers a destination group and asks for another project
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
| Navigator root | Project archives (`.gamp`/ZIP) enter the shared importer. A recognized project directory is copied into a project named after the directory, excluding the source `.project`; other inputs retain the existing copy behavior. |
| Existing navigator project/folder | `NavigatorResourceDropAssistant` uses Eclipse copy/link operations according to the requested drop operation. |
| Paste filesystem directories into a user group | `UserGroups.importDirectories` copies recognized project directories, asks about name conflicts and registers the copies. Non-project inputs are skipped by this helper. |
| GAML editor text area | External files are reused if already registered, otherwise linked under the editor project's `external/` folder. Model resources generate import statements; other file resources generate file declarations. This edits the current model rather than opening/importing a project. |

Existing project/folder and editor drops remain resource transfers. In particular,
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
                                     bundles (no per-file inventory)
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
session-history/recovery UI. On closing, the player detects content changes across
its workspace and offers a native Save dialog to export them as ZIP. Files outside
the workspace are not collected.

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
viewer document, and `.gamp` as an editable-project import document. Windows registers Open With handlers; Linux's Debian packaging
installs a desktop handler and MIME descriptions. User-selected defaults still
apply. These registrations require an assembled/installed product; an Eclipse
development launch or an old installation does not acquire them from source edits.
Package launching through the UI explicitly requires an installed native launcher.
The current UI labels are **Open playable model...**, **Export as playable
model...** and **Properties > Playable model**. Navigator archives additionally offer
**Play** (`.gamx`) or **Import** (`.gamp`), cached content summaries and properties.
Old playable archives need the `.gamx` suffix.

Argument parsing is only partly unified:

- The pre-Equinox launcher and `PackageDocuments` each parse document arguments.
  They have different option-value lists.
- Normal product startup forwards every classified document to the modeler's
  document queue when the request is not exclusively playable models. The advisor
  does not process those arguments a second time. Direct development launches
  that bypass the document-router application still retain the advisor's legacy
  last-argument fallback.
- `model.gaml#experimentName` is handled by the model manager. Its `#` parsing is
  not a structured document-request representation.
- `.gamr` retains its launcher-delegate behavior, also through shared document dispatch.
  The inspected serialization implementation uses saved
  agent/simulation formats, including `.gsim`, rather than treating `.gamr` as
  a general project archive.
- Headless startup has its own XML/batch/`-gaml`/server argument processing. It
  does not expose the GUI package-opening service as a headless `.gamx` runner.

Installation `dropins/` is yet another concept, **not drag-and-drop import**.
The regular product includes the p2 drop-ins reconciler and plugin-install
commands. This is installation-level provisioning. A package carries `plugins/`
and builds an explicit private bundle inventory; player preparation disables
drop-ins reconciliation instead of copying package plugins into the installation.
Project-archive import can, with explicit consent, stage included missing plugins
as a complete Dropins contribution and offer to restart. It does not activate them
in the current framework, overwrite installed conflicting bundles, or install
anything just by inspecting an archive.

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
| Partly addressed | Multiple file arguments and type routing differ by entry point. | Product startup now queues mixed documents once; direct development launches retain the legacy fallback, and generic resource transfers deliberately do not open/import documents. |
| Implemented | Dependency-aware editable-project archives. | `.gamp` exports whole projects, optional links/plugins and a manifest without a per-file inventory, and uses the same collision/group import workflow as legacy ZIPs. See below for limits. |
| Next | Export does not always snapshot every dirty participating editor. | Editor-triggered export saves the active editor, but saving all others depends on `EDITOR_SAVE`. Navigator-triggered export has only that optional Save All. Dependency discovery and staging can therefore use disk contents older than visible edits. [Source][simulation-export] |
| Later | Runtime packaging still has two staging/dependency implementations. | `.gamx` uses `PackageProjectStager` and package dependency closure; application export uses `GamaZipBuilder` and `BundleDependencyAnalyzer`. Shared discovery does not imply identical portability guarantees. |
| Implemented | Shutdown-only generated data saving. | New or modified workspace files are offered as a ZIP export on closing; internal metadata is ignored. This is not resumable simulation state. |

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

## 8. Implemented: editable `.gamp` project archives

### Export and layout

Navigator export actions are selection-based: a single project offers project
archive and playable-model export; a model or experiment offers playable-model
export. Empty selections, roots, groups and ordinary folders do not show Export.
The generic workbench Export wizard retains its project chooser for global export.

**Export > Project** opens a compact options dialog, with **Choose destination...**
instead of OK and no destination field, Browse button or file-selection tree.
It is a plain dialog without a wizard-style title banner. Dependency discovery
uses the workbench's delayed progress display, avoiding a progress-window flash
when preparation completes quickly.
That button opens the native save picker with the default `[Project Name].gamp`.
Cancelling the picker returns to the options dialog without exporting.
It exports the whole project. Both options default unchecked:

- **Resolve and export linked resources** materializes linked files/folders and
  removes their external link definitions from the exported copy. Otherwise the
  definitions remain and targets are omitted, with a portability warning.
- **Export required GAMA plugins** appears only for required non-standard plugins.
  Standard plugins remain host requirements; plugin dependency closure shares
  the playable exporter. Optional plugins must be installed as distributable JARs.

```text
Project.gamp                        ZIP container
  gama-project.properties          format.version=1, archive.kind=project,
                                    GAMA version, projects, links, requirements,
                                    warnings (no per-file inventory)
  workspace/Project/.project
  workspace/Project/...            whole project resources, including empty folders
  plugins/extra.plugin_version.jar optional non-standard plugins
```

The exporter writes one selected project; the format and importer support multiple
projects. Models need no experiments and need not compile to preserve project
files. Failed compilation makes dependency discovery explicitly incomplete and
blocks plugin embedding; it is not reported as a complete dependency inventory.
Referenced projects are reported but not recursively exported. Dynamic external
data, filesystem symlinks, directory-form plugins and arbitrary external projects
are not automatically collected. Workspace `.metadata` is always excluded.
Whole-project resources can include generated outputs or local files: authors
must review their project content before sharing it.
For externally registered projects, the archive uses the workspace project name
consistently in its manifest, project folder and staged `.project` description,
even when the original on-disk description uses another name. The original
description is not modified.

### Import, replace and rename

Use **File > Import GAMA project**, navigator **Import**, or open/double-click
`.gamp`. OS associations are packaged for macOS, Windows and Linux and take effect
only in rebuilt/installed products, subject to default-application preferences.
The same import dialog accepts old project ZIPs containing `.project` descriptions;
they have no dependency manifest. Generic **Import from archive**
still transfers selected files rather than importing a project.
The archive option in the advanced project-import wizard also forwards `.gamp`
and ZIP files to this shared importer; directory and other legacy archive routes
retain their existing behavior.

Import previews content, link portability and plugin requirements, then asks for
an optional navigator group. Existing project names offer **Cancel / Replace
existing model / Rename and import**. An occupied but unregistered directory can
only be preserved by cancellation or renaming, not implicitly overwritten.
Replacement is limited to workspace-owned user projects: built-in library
projects and externally registered projects must instead be renamed/copied.
Originals are retained under `.metadata/gama-project-backups/<id>/`.

Extraction validates bounded, portable paths and enforces extraction byte limits before any
workspace copy. The importer validates the complete plan first and rolls back
failed project registration/replacement. Renaming adjusts `.project` references
and static cross-project model/data literals while preserving comments and source
encodings; dynamically constructed paths, scripts and arbitrary data formats are
not migrated. Choosing a different group does not collect or repair references
to projects that are outside the archive.

### Optional plugin installation

Importing is not consent to execute additional Java code. An unchecked install
option appears for included missing plugins, with a trust warning. If selected,
the importer checks plugin identity/version, installed/pending conflicts and
Dropins permissions, and stages the contribution before changing projects.
After import, it publishes the complete contribution and offers a restart.
Installation failures are reported explicitly; no incompatible installed bundle
is overwritten. Read-only installations require normal plugin provisioning.
Declining installation still imports editable sources, which may not compile
until requirements are installed. Checksums prove consistency, not authenticity.

`.gamx` still opens the isolated player, not this importer. Old playable `.gamp`
files must be renamed to `.gamx`. Editable import of playable packages remains a
separate future feature.

## 9. Remaining delivery priorities

1. Fix external-opening correctness first: non-destructive reopen, canonical
   identity checks, preserved fallback results, meaningful errors and real cancel.
2. Unify document parsing/dispatch and copy/link/conflict policies. Process all
   document arguments once; distinguish Edit from Run for `.experiment`.
3. Add validated **Import source projects** for existing runnable `.gamx` files,
   with an explicit plugin strategy. Preserve existing player opening.
4. Extend `.gamp` dependency declarations for broken projects and explicit
   multi-project export where needed; retain the simple whole-project save flow.
5. Consolidate portable staging/dependency planning where application and package
   export can actually share it; define dirty-editor snapshot policy and clearer
   result-export/recovery behavior.

Acceptance scenarios should cover fresh/existing workspaces, open/closed and
same-name projects, directory/description name mismatch, copy/link/cancel,
multiple/mixed arguments, model/data relative paths, missing/conflicting plugins,
project-only archives, dirty editors, old playable `.gamp` files renamed to
`.gamx`, reserved-suffix routing, and cold/running GAMA on each supported OS.

The practical rule is: **Save for edits, `.gamp` for editable project exchange
(legacy ZIP import remains supported), `.gamx` for runnable distribution, and
the shutdown save prompt for generated player outputs.
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
[project-export]: gama.ui.export/src/gama/ui/export/commands/ExportProject.java
[project-import]: gama.ui.export/src/gama/ui/export/commands/ImportProject.java
[project-archive]: gama.export/src/gama/export/ProjectArchive.java
[project-import-transaction]: gama.ui.export/src/gama/ui/export/ProjectArchiveImport.java
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
