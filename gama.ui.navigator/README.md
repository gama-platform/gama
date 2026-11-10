# Navigator

## Dependencies

**Show dependencies** on a model or standalone experiment file opens the
**Dependencies** view (`gama.ui.navigator.view.Dependencies`). The **Sync with editor**
toggle uses the navigator's sync icon. Off by default, it keeps the graph pinned;
when enabled, it immediately follows the active workspace model or standalone
experiment and switches when the active editor changes. Other editor types and
navigator selection leave the graph unchanged. The view does not require a running
experiment.

The GAML editor's options menu also provides **Show dependencies** / **Hide
dependencies**, immediately after **Show versions** / **Hide versions**.
Showing the view chooses that editor's saved workspace model.

The navigator view is titled **Navigator**. **Views > Show...** groups Navigator,
the three consoles, Outline / Versions / Minimap / Dependencies, and
Validation / Tests, in that order with separators between groups. **Other...**
remains available at the end for additional installed views.

**Back** and **Forward** revisit models chosen in this view, including models
followed through editor sync. Their left/right arrow buttons sit together at the
far right of the toolbar and are disabled when no corresponding entry exists;
tooltips name the destination.
History navigation turns editor sync off to keep the revisited model pinned.
Refreshes do not add entries, and choosing a new model after going back replaces
the forward history. History lasts while the view is open; reopening starts with
the restored model.

The highlighted file and all its reachable imported models are arranged
left-to-right using Jungrapht's Sugiyama layout with rounded, gap-routed connections.
An arrow **A -> B** means A directly imports B. Shared imports
appear once; no extra arrows are added for indirect dependencies. Imports outside
the selected file's project are included. Plugins are not shown.
The view reads saved files without semantic compilation: unsaved editor changes
are not included. Saving or changing any workspace file in the graph refreshes
it automatically, whether editor sync is on or off. Updates are debounced and
retain the current manual pan, zoom, and focused card when possible, or refit
the graph while Fit is active. **Refresh** remains
available for an immediate reload or files outside the workspace. External edits
must first be detected by Eclipse's workspace refresh.

Click a card to open its file; drag to pan and scroll to zoom around the pointer.
The toolbar provides zoom and fit actions. Arrow keys focus files, Enter opens
the focused file, `+`/`-` zoom, and `F` or `0` fits the graph. Fit is active on
initial opening and continues adjusting the graph when the view is resized.
Manual zooming, dragging, or keyboard panning leaves Fit mode; selecting Fit
enables it again. Hovering highlights
immediate connections and shows full paths, import aliases, and diagnostics.
Compact cards use the navigator's green (no known problems), yellow (warning),
and red (error) status dots, with details only in tooltips. Model cards are
180-280 units wide with tighter padding and column spacing; long parent paths
are abbreviated rather than widening the cards, with full paths in tooltips. Status combines
saved-file syntax diagnostics and existing workspace validation markers; changes
to problem markers also refresh the view, without triggering validation. Missing/inaccessible
files remain connected as pale, dashed ghost cards. Cyclic imports are normal and
are neither flagged nor highlighted as problems. The view's paperclip icon and
small circular-arrow **Refresh** icon match the GAMA modeling-view style, with
light/dark variants and no model-file icon on the left. Initial display fits the
graph; zoom in or pan to explore larger graphs.
An isolated model is shown as one card.

**Show resources used by each model** toggles compact icon-and-filename labels
beneath each model, connected by teal lines rather than the import arrows.
The toggle is off by default and restored with the view. Labels use the navigator's
file icons and form one or two tidy columns, with space reserved by the layout.
Shared resources appear beside each model that uses them; the status bar counts
distinct resources. Click or use the arrow keys and Enter to open local resources;
hover for their full path and owning model. Missing resources remain as dashed
ghost labels and may represent files generated at runtime.

Resource detection shares the navigator's **Uses** rules: quoted literal paths
with a registered GAMA file extension, excluding comments and model imports.
Paths are resolved relative to the declaring model, or as absolute local paths.
This is not semantic analysis: dynamically constructed paths are not inferred,
and literal output filenames may also appear. Remote references are displayed
but never fetched or checked; clicking them explains this limitation.
The view neither reads resource contents nor creates workspace links.
Visible workspace resources participate in automatic refresh, including creation
and deletion of missing targets. Hiding them returns to the model-only graph.

Loading and layout run in the background, without a Stop toolbar button; changing
the input or closing the view cancels obsolete work. A refresh keeps the
previous snapshot visible until its replacement is ready; cancellation or failure
does not present that previous snapshot as refreshed. The chosen file is restored
when reopening the workbench, along with the sync and resources toggles. Linking chooses the
active model; automatic refresh tracks that model and all its reachable imports,
including creation/deletion of missing targets and project open/close changes.
Unrelated workspace files do not trigger a reload. Graph geometry is transient
and is rebuilt.

## Versions

For a workspace file, **Versions...** in the navigator opens
`gama.ui.navigator.view.PreviousVersions`. GAML editors also expose
**Show versions** / **Hide versions** immediately after the
minimap entry in their options menu. Files without saved history can still open
the view.

The view follows the selected workspace file or active editor, groups versions
by date, and compares a version with the current file when opened. It has no
toolbar, repeated filename, column heading, or configurable view modes. Date
groups and revisions use theme-aware GAMA calendar and version icons.
History is fetched at opening, whenever a
workspace editor is activated or brought to the top, and when the file changes
(including saves). Opening a revision or comparison editor does not change the
workspace file being followed.

Its revision context menu provides **Open in text editor**, **Compare with
current**, and **Replace current**. Replacement uses Eclipse's overwrite
confirmation and preserves the replaced contents in local history; the current
revision cannot replace itself.

The application default disables Eclipse's local-history retention policy, so
versions are not discarded because of age, count, or file size. Explicit
workspace retention preferences still take precedence. Unlimited history can
increase workspace disk usage and cannot recover versions already discarded.

## Contributing navigator folders

The `gama.ui.navigator.topLevelFolders` extension point declares both the navigator's
own workspace folders and independent top-level folders with their complete virtual
hierarchies. Downstream declarations do not register GAML extensions, add
model-library categories, clone repositories, or create workspace projects.

The navigator uses this extension itself: its `plugin.xml` declares Data, Demos,
Library, Recipes, Tutorials, Tests and User with the same factory interface and
registry as external folders. `WorkspaceFolderProvider` reads their `category`,
`name` and `order` attributes. The seven `category` values and their lowercase IDs
are reserved for navigator-owned declarations, preventing optional plug-ins from
changing local-project classification. Built-in getters remain compatibility
accessors into the registry, not an alternative hard-coded construction path.

## Declaration and placement

```xml
<extension point="gama.ui.navigator.topLevelFolders">
  <folder
      id="gama.ui.shared.models.github"
      class="gama.ui.shared.models.navigator.GitHubFolderProvider"
      before="user"
      order="0"/>
</extension>
```

`id` is globally unique and stable; it is independent of the displayed folder name.
Use a plug-in-qualified identifier. `class` implements the exported
`gama.ui.navigator.view.contents.ITopLevelFolderProvider` interface and has a public
no-argument constructor.

`before` defaults to `user`. Supported anchors, in display order, are `data`,
`demos`, `library`, `recipes`, `tutorials`, `tests`, and `user`. Their IDs are
reserved. Contributions precede their anchor even when that built-in folder is
hidden because it is empty. Within one anchor, ascending `order` (default `0`),
then lexicographic contribution ID, determines the order. Anchoring to other
contributions is deliberately unsupported: no cycles or installation-order
dependencies are possible.

## Downstream visibility

Add `gama.ui.navigator` to `Require-Bundle` in the UI plug-in's manifest. The
`gama.ui.navigator.view.contents` package, including the factory, `NavigatorRoot`,
`TopLevelFolder`, `VirtualContent` and `ResourceManager`, is exported without
internal/friend restrictions. The navigator re-exports its API-facing workspace,
runtime, workbench, JFace, SWT and shared-UI bundles, so public parameter and return
types are visible downstream. GAMA's navigator targets JavaSE-25; downstream code
must compile against a compatible GAMA target.

No separate Eclipse navigator content or label-provider contribution is necessary.
The existing providers accept subclasses of `VirtualContent` throughout the tree.
The shared context menu accepts standard Eclipse menu and action contributions,
as described below; no additional GAMA-specific menu extension point is needed.

## Shared context menu

The popup ID is `navigator.menu`, and platform contributions are enabled. Its
insertion groups are `group.new`, `group.open`, `group.openWith`, `group.copy`,
`group.refresh`, and `additions`. Prefer `additions` for plug-in-specific commands,
or a semantic group such as `group.open` for "Open on GitHub".

Independent hierarchies can override `VirtualContent.supportsWorkspaceActions()`
to return `false` on their top-level folder. Children inherit that policy.
The standard **Import...**, resource **Properties**, **Copy**, **Paste**, **Delete**
and refactoring entries are then omitted, including for mixed selections containing
such nodes. Contributed commands remain available and can supply their own
properties or import action. Declarative contributions can check the same policy
with the `gama.navigator.supportsWorkspaceActions` property tester.
Keep the navigator's existing **Refresh/F5** action rather than adding another:
it calls `TopLevelFolder.refresh()` for independent folders and already has the
standard refresh icon and keyboard binding.

### Commands and handlers (recommended)

Declare a command/handler and contribute it through `org.eclipse.ui.menus`:

```xml
<extension point="org.eclipse.ui.commands">
  <command id="gama.ui.shared.models.import"
      name="Import into workspace"
      defaultHandler="gama.ui.shared.models.navigator.ImportHandler"/>
</extension>
<extension point="org.eclipse.ui.menus">
  <menuContribution locationURI="popup:navigator.menu?after=additions">
    <command commandId="gama.ui.shared.models.import"
        label="Import into workspace..." style="push">
      <visibleWhen checkEnabled="false">
        <with variable="selection">
          <count value="1"/>
          <iterate ifEmpty="false" operator="and">
            <instanceof value="gama.ui.shared.models.navigator.RemoteProject"/>
          </iterate>
        </with>
      </visibleWhen>
    </command>
  </menuContribution>
</extension>
```

`ImportHandler` extends `AbstractHandler` and obtains the current selection with
`HandlerUtil.getCurrentSelection(event)`. Check it is an `IStructuredSelection`
whose first element is the expected remote node before acting. Handler
`enabledWhen` expressions or a property tester can gate state-dependent actions
(offline, already imported, etc.); avoid network requests in these expressions.
Use `visibleWhen` to avoid showing remote actions on local workspace nodes. This
path requires no additional navigator viewer binding.

### Programmatic action providers

For a dynamic set of actions, subclass Eclipse `CommonActionProvider`, declare it
through `org.eclipse.ui.navigator.navigatorContent`, and explicitly bind its ID
to this viewer. Merely declaring an action provider is not sufficient: the
navigator's existing binding only includes its built-in providers.

```xml
<extension point="org.eclipse.ui.navigator.navigatorContent">
  <actionProvider id="gama.ui.shared.models.actions"
      class="gama.ui.shared.models.navigator.RemoteActions">
    <enablement>
      <with variable="selection">
        <iterate ifEmpty="false" operator="and">
          <instanceof value="gama.ui.shared.models.navigator.RemoteProject"/>
        </iterate>
      </with>
    </enablement>
  </actionProvider>
</extension>
<extension point="org.eclipse.ui.navigator.viewer">
  <viewerActionBinding viewerId="gama.ui.application.view.GamaNavigator">
    <includes>
      <actionExtension pattern="gama.ui.shared.models.actions"/>
    </includes>
  </viewerActionBinding>
</extension>
```

In `fillContextMenu(IMenuManager menu)`, obtain the selection from
`getContext().getSelection()` and call `menu.appendToGroup("additions", action)`.
The extension site supplies the viewer and view shell. Keep action construction
fast; do asynchronous work only when an action is invoked. Clean up owned
resources in the provider's `dispose()`. `org.eclipse.ui.navigator` is re-exported
by `gama.ui.navigator`, making these provider interfaces visible downstream.

Existing Refresh/F5 already delegates to an independent folder's
`refresh(IProgressMonitor)`, including when a descendant is selected. Its
provider does not expose workspace repair/test actions for an independent tree.
Local copy/delete/rename actions remain resource-based; remote nodes should not
adapt to `IResource` merely to gain menu items. Implement their own import/open
commands instead.

## Folder and hierarchy contract

The factory returns a subclass of `TopLevelFolder` using its protected
non-workspace constructor:

```java
public final class GitHubFolderProvider implements ITopLevelFolderProvider {
    @Override
    public TopLevelFolder createFolder(final NavigatorRoot root) {
        return new GitHubFolder(root);
    }
}

public final class GitHubFolder extends TopLevelFolder {
    private VirtualContent<?>[] nodes = new VirtualContent<?>[0];

    public GitHubFolder(final NavigatorRoot root) {
        super(root, "GitHub", null, "Browse shared models", IGamaColors.BLUE);
    }

    @Override
    public Object[] getNavigatorChildren() { return nodes; }

    @Override
    public Object[] getCachedChildren() { return nodes; }

    @Override
    public boolean hasChildren() { return nodes.length > 0; }
}
```

The example is intentionally an empty folder, not a network implementation. Pass an
`ImageDescriptor` from your own plug-in instead of `null` to supply an icon.
Contributed roots are visible even when empty; override `isVisibleWhenEmpty()` if
necessary. The registry assigns their stable IDs from the declaration.

Each descendant extends `VirtualContent<ParentType>` and receives its actual parent
in its constructor. Supply children, icon, suffix, overlay and problem severity;
use `NO_PROBLEM` when no diagnostic is available. Children must be non-null arrays
of `VirtualContent` nodes, not raw API response objects. Use `FOLDER`, `PROJECT` or
`FILE` node types for title-searchable entries. Nodes need not implement `IAdaptable`
or have an `IResource`. Do not reuse the cached workspace `WrappedProject` instances
as remote entries: those belong to the built-in project categories.

Override `getId()` on descendants when labels can change or sibling labels are not
unique. IDs must be nonempty and stable within their parent; repository paths or
immutable API IDs are preferable to translated labels. The viewer comparer uses
node class, ancestor identities and ID, allowing freshly published wrappers to
retain their expansion state.

## Loading, refresh and disposal

- Factory creation must be fast, without network requests or filesystem scans.
  Do not call `root.getFolders()` from the factory: the registry is still loading.
- Viewer callbacks execute on the UI thread. `getNavigatorChildren()` may schedule
  an asynchronous load, but must return immediately with a cached snapshot or
  loading/error placeholder. Coalesce repeated requests and honor cancellation.
  `hasChildren()` must not perform I/O; lazy folders can return `true` before their
  first load.
- `getCachedChildren()` must return only the currently available snapshot, without
  fetching or scheduling a load. Override it wherever `getNavigatorChildren()` is
  lazy. Title search examines only cached content; model-only and source searches
  remain restricted to actual workspace model files. Search-driven expansion uses
  cached children too. Global Expand All traverses only workspace categories;
  Expand All on a selected contributed node opens just one level.
- Publish new snapshots on the UI thread, then call
  `getManager().refreshContent(this)` on the affected node. This schedules a viewer
  refresh without waiting for a workspace resource-change event. Retain the owning
  manager for asynchronous callbacks; do not publish after disposal.
- Override `TopLevelFolder.refresh(IProgressMonitor)` for explicit Refresh/F5.
  It runs off the UI thread, outside the local workspace modification lock;
  honor the monitor, throw `CoreException` on failure, and marshal UI-owned changes
  to the UI thread. Selecting any descendant refreshes its owning contribution.
  Refresh with no selection refreshes workspace resources and all contributions.
  Ordinary project additions/group changes/library refreshes rebuild only built-in
  workspace categories, retaining contributed roots and caches.
- Override `TopLevelFolder.dispose()` to cancel jobs, unregister listeners and
  release owned images. It runs when the navigator content provider is disposed.
  Reopening creates new contributions. Installing/removing plug-ins requires a
  restart; the registry is not a dynamic plug-in-unloading mechanism.

Expansion paths for contributions use encoded stable IDs, separately from legacy
workspace paths and category names. When state retention is enabled, saved paths
are replayed as asynchronous child snapshots arrive through `refreshContent`.
Only cached children are inspected, but expanding saved ancestors may trigger their
normal asynchronous loading. Collapsing an ancestor cancels its pending restoration.

Independent folders have `isWorkspaceFolder() == false` and `getCategory() == null`.
They do not classify workspace projects, display local-project counts, or accept
the built-in resource drop operation. Existing local project/group operations
remain attached to the built-in categories. Contributions implement their own
import/download commands and error/offline UI; listing remote entries does not
require importing anything.

Malformed factories, duplicate/reserved IDs and invalid placement values are
logged to the Eclipse error log and skipped without preventing other roots from
loading. Disposal failures are also logged. Existing built-in folder ordering,
empty-folder visibility and legacy workspace expansion preferences are preserved.
