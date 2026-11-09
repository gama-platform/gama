package gama.ui.navigator.view.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;

import gama.api.GAMA;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.FileOpener;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.State;
import gama.ui.navigator.view.dependencies.DependencyGraphLayout.Measurements;

/** Saved-file dependency view with optional editor linking, independent of simulations and navigator selection. */
public final class DependenciesView extends ViewPart implements IToolbarDecoratedView {

	public static final String ID = "gama.ui.navigator.view.Dependencies";
	private static final String PLUGIN = "gama.ui.navigator";
	private DependencyGraphCanvas canvas;
	private Label status;
	private GamaToolbar2 toolbar;
	private ToolItem backItem, forwardItem, syncItem, refreshItem, resourcesItem;
	private final ArrayList<URI> history = new ArrayList<>();
	private int historyIndex = -1;
	private URI input;
	private Job job;
	private long request;
	private boolean followEditor;
	private boolean showResources;
	private volatile boolean disposed;
	private volatile Set<IPath> watchedFiles = Set.of();

	private final UIJob automaticRefreshJob = new UIJob("Refreshing model dependencies") {
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			if (!monitor.isCanceled()) loadGraph();
			return Status.OK_STATUS;
		}
	};

	private final IResourceChangeListener resourceListener = event -> {
		final var delta = event.getDelta();
		if (!disposed && delta != null && affectsWatchedFiles(delta, watchedFiles)) {
			automaticRefreshJob.cancel();
			automaticRefreshJob.schedule(300);
		}
	};

	private final UIJob syncWithEditorJob = new UIJob("Synchronizing dependencies with editor") {
		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			syncWithActiveEditor();
			return Status.OK_STATUS;
		}
	};

	private final IPartListener2 editorListener = new IPartListener2() {
		@Override
		public void partActivated(final IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference) scheduleSyncWithEditor();
		}

		@Override
		public void partBroughtToTop(final IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference) scheduleSyncWithEditor();
		}

		@Override
		public void partInputChanged(final IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference) scheduleSyncWithEditor();
		}

		@Override
		public void partClosed(final IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference) scheduleSyncWithEditor();
		}

		@Override
		public void partVisible(final IWorkbenchPartReference ref) {
			if (ref instanceof IEditorReference || ref.getPart(false) == DependenciesView.this) scheduleSyncWithEditor();
		}
	};

	{
		syncWithEditorJob.setSystem(true);
		automaticRefreshJob.setSystem(true);
	}

	public static void show(final IFile file) {
		try {
			final var view = (DependenciesView) WorkbenchHelper.getPage().showView(ID);
			view.choose(URI.createPlatformResourceURI(file.getFullPath().toString(), true));
		} catch (final PartInitException e) {
			StatusManager.getManager().handle(new Status(IStatus.ERROR, PLUGIN, "Cannot open Dependencies", e),
					StatusManager.LOG | StatusManager.SHOW);
		}
	}

	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);
		setTitleImage(GamaIcon.named("views/tabs/view.dependencies").image());
		if (memento != null) {
			if (memento.getString("model") != null) input = URI.createURI(memento.getString("model"));
			followEditor = Boolean.TRUE.equals(memento.getBoolean("followEditor"));
			showResources = Boolean.TRUE.equals(memento.getBoolean("showResources"));
		}
		if (input != null) {
			history.add(input);
			historyIndex = 0;
		}
	}

	@Override
	public void createPartControl(final Composite parent) {
		final var content = GamaToolbarFactory.createToolbars(this, parent);
		final var layout = new GridLayout(1, false);
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		content.setLayout(layout);
		canvas = new DependencyGraphCanvas(content, this::open);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		status = new Label(content, SWT.WRAP);
		final var statusData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		statusData.horizontalIndent = 8;
		statusData.verticalIndent = 6;
		status.setLayoutData(statusData);
		status.setText("Choose a model or enable Sync with editor.");
		if (input != null) watchFiles(Set.of(input));
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener,
				IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.POST_BUILD);
		getSite().getPage().addPartListener(editorListener);
		syncWithActiveEditor();
		if (input != null && job == null) refresh();
	}

	@Override
	public void createToolItems(final GamaToolbar2 toolbar) {
		this.toolbar = toolbar;
		syncItem = toolbar.check(IGamaIcons.EDITOR_LINK, "", "Sync with editor: follow the active model when enabled",
				event -> {
					followEditor = syncItem.getSelection();
					if (followEditor) scheduleSyncWithEditor();
					else syncWithEditorJob.cancel();
				}, SWT.RIGHT);
		toolbar.setSelection(syncItem, followEditor);
		refreshItem = toolbar.button("toolbar/view.refresh", "", "Refresh",
				event -> refresh(), SWT.RIGHT);
		refreshItem.setEnabled(input != null);
		resourcesItem = toolbar.check("toolbar/view.resources", "", resourcesTooltip(),
				event -> {
					showResources = resourcesItem.getSelection();
					resourcesItem.setToolTipText(resourcesTooltip());
					refresh();
				}, SWT.RIGHT);
		toolbar.setSelection(resourcesItem, showResources);
		toolbar.sep(SWT.RIGHT);
		toolbar.button("toolbar/font.increase", "", "Zoom in (+)", event -> canvas.zoom(1.2), SWT.RIGHT);
		toolbar.button("toolbar/font.reset", "", "Fit graph (F)", event -> canvas.fit(), SWT.RIGHT);
		toolbar.button("toolbar/font.decrease", "", "Zoom out (-)", event -> canvas.zoom(1 / 1.2), SWT.RIGHT);
		toolbar.sep(SWT.RIGHT);
		backItem = toolbar.button("editor/command.lastedit", "", "Back",
				event -> navigateHistory(-1), SWT.RIGHT);
		forwardItem = toolbar.button("editor/command.nextedit", "", "Forward",
				event -> navigateHistory(1), SWT.RIGHT);
		updateHistoryItems();
	}

	private String resourcesTooltip() {
		return (showResources ? "Hide" : "Show") + " resources used by each model";
	}

	private void scheduleSyncWithEditor() {
		if (followEditor && !disposed) syncWithEditorJob.schedule(200);
	}

	private void syncWithActiveEditor() {
		if (!followEditor || disposed || canvas == null || canvas.isDisposed()) return;
		final var editor = getSite().getPage().getActiveEditor();
		final IFile file = editor == null ? null : ResourceUtil.getFile(editor.getEditorInput());
		if (!ModelDependencyService.isModel(file)) return;
		final URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
		if (!uri.equals(input)) choose(uri);
	}

	private void choose(final URI uri) {
		if (historyIndex < 0 || !uri.equals(history.get(historyIndex))) {
			history.subList(historyIndex + 1, history.size()).clear();
			history.add(uri);
			historyIndex = history.size() - 1;
		}
		displayInput(uri);
	}

	private void navigateHistory(final int direction) {
		final int next = historyIndex + direction;
		if (next < 0 || next >= history.size()) return;
		// Pin history navigation so pending editor events cannot immediately replace it.
		followEditor = false;
		syncWithEditorJob.cancel();
		toolbar.setSelection(syncItem, false);
		historyIndex = next;
		displayInput(history.get(historyIndex));
	}

	private void updateHistoryItems() {
		final boolean back = historyIndex > 0;
		final boolean forward = historyIndex >= 0 && historyIndex < history.size() - 1;
		backItem.setEnabled(back);
		forwardItem.setEnabled(forward);
		backItem.setToolTipText(back ? "Back to " + URI.decode(history.get(historyIndex - 1).lastSegment()) : "Back");
		forwardItem.setToolTipText(forward ? "Forward to " + URI.decode(history.get(historyIndex + 1).lastSegment())
				: "Forward");
	}

	private void displayInput(final URI uri) {
		if (!uri.equals(input)) watchFiles(Set.of(uri));
		input = uri;
		updateHistoryItems();
		refreshItem.setEnabled(true);
		refresh();
	}

	private void cancel() {
		request++;
		automaticRefreshJob.cancel();
		if (job != null) job.cancel();
		job = null;
		status.setText(canvas.geometry() == null ? "Loading canceled. Refresh to try again."
				: "Refresh canceled. Showing the previous saved-file snapshot.");
		canvas.setMessage("Loading canceled. Refresh to try again.");
		status.getParent().layout();
	}

	private void refresh() {
		automaticRefreshJob.cancel();
		loadGraph();
	}

	private void loadGraph() {
		if (input == null || disposed) return;
		if (job != null) job.cancel();
		final long current = ++request;
		final URI chosen = input;
		final boolean includeResources = showResources;
		final var display = canvas.getDisplay();
		final boolean fit = canvas.geometry() == null || !chosen.equals(canvas.geometry().graph().focus());
		status.setText("Reading saved dependencies of " + URI.decode(chosen.lastSegment()) + "..."
				+ (canvas.geometry() == null ? "" : " Showing the previous snapshot until ready."));
		status.getParent().layout();
		canvas.setMessage("Reading saved model dependencies...");
		job = new Job("Model dependencies") {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Reading model imports", IProgressMonitor.UNKNOWN);
				try {
					final var graph = ModelDependencyService.read(chosen, includeResources, monitor);
					final var sizes = new AtomicReference<Measurements>();
					if (monitor.isCanceled() || display.isDisposed()) throw new OperationCanceledException();
					display.syncExec(() -> {
						if (accepts(current)) {
							final var files = new LinkedHashSet<>(graph.nodes().keySet());
							graph.resources().forEach(resource -> files.add(resource.file().uri()));
							watchFiles(files);
							sizes.set(canvas.measure(graph));
						}
					});
					if (sizes.get() == null || monitor.isCanceled()) throw new OperationCanceledException();
					monitor.subTask("Arranging dependency graph");
					final var geometry = DependencyGraphLayout.compute(graph, sizes.get(), monitor);
					if (monitor.isCanceled()) throw new OperationCanceledException();
					post(display, () -> {
						if (!accepts(current)) return;
						canvas.setGeometry(geometry, fit);
						final long problems = graph.nodes().values().stream().filter(n -> n.state() != State.AVAILABLE).count();
						status.setText(graph.nodes().get(chosen).name() + "  |  " + graph.nodes().size() + " model files, "
								+ graph.edges().size() + " imports"
								+ (includeResources ? ", " + graph.resources().stream().map(r -> r.file().uri()).distinct().count()
										+ " resources" : "")
								+ "  |  Saved files; workspace changes refresh automatically."
								+ (problems == 0 ? "" : "  " + problems + " files with problems; hover for details."));
						status.getParent().layout();
						job = null;
					});
					return Status.OK_STATUS;
				} catch (final OperationCanceledException e) {
					post(display, () -> {
						if (accepts(current)) cancel();
					});
					return Status.CANCEL_STATUS;
				} catch (final RuntimeException e) {
					final var error = new Status(IStatus.ERROR, PLUGIN, "Cannot build model dependencies", e);
					StatusManager.getManager().handle(error, StatusManager.LOG);
					post(display, () -> {
						if (!accepts(current)) return;
						status.setText("Cannot refresh dependencies: " + e.getMessage()
								+ (canvas.geometry() == null ? "" : ". Showing the previous snapshot."));
						status.getParent().layout();
						canvas.setMessage("Cannot load dependencies. See the error log for details.");
						job = null;
					});
					return error;
				} finally {
					monitor.done();
				}
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	private void watchFiles(final Collection<URI> uris) {
		final var paths = new LinkedHashSet<IPath>();
		for (final URI uri : uris) {
			if (uri.isPlatformResource()) {
				paths.add(Path.fromPortableString(uri.toPlatformString(true)));
			} else if (uri.isFile()) {
				for (final var file : ResourcesPlugin.getWorkspace().getRoot()
						.findFilesForLocationURI(java.net.URI.create(uri.toString()))) {
					paths.add(file.getFullPath());
				}
			}
		}
		watchedFiles = Set.copyOf(paths);
	}

	private static boolean affectsWatchedFiles(final IResourceDelta delta, final Set<IPath> files) {
		for (final IPath file : files) {
			final var changed = delta.findMember(file);
			if (changed != null) {
				if ((changed.getKind() & (IResourceDelta.ADDED | IResourceDelta.REMOVED)) != 0
						|| (changed.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.ENCODING
								| IResourceDelta.LOCAL_CHANGED | IResourceDelta.TYPE | IResourceDelta.REPLACED)) != 0)
					return true;
				if ((changed.getFlags() & IResourceDelta.MARKERS) != 0) {
					for (final var marker : changed.getMarkerDeltas()) {
						if (marker.isSubtypeOf(IMarker.PROBLEM)) return true;
					}
				}
			}
			// Closed/deleted containers may have no delta for the watched file itself.
			for (IPath parent = file.removeLastSegments(1); parent.segmentCount() > 0;
					parent = parent.removeLastSegments(1)) {
				final var container = delta.findMember(parent);
				if (container != null && ((container.getKind() & (IResourceDelta.ADDED | IResourceDelta.REMOVED)) != 0
						|| (container.getFlags() & (IResourceDelta.OPEN | IResourceDelta.LOCAL_CHANGED
								| IResourceDelta.ENCODING | IResourceDelta.TYPE | IResourceDelta.REPLACED)) != 0))
					return true;
			}
		}
		return false;
	}

	private static void post(final Display display, final Runnable update) {
		try {
			display.asyncExec(update);
		} catch (final SWTException e) {
			if (e.code != SWT.ERROR_DEVICE_DISPOSED) throw e;
		}
	}

	private boolean accepts(final long current) {
		return !disposed && current == request && canvas != null && !canvas.isDisposed();
	}

	private void open(final URI uri) {
		final var graph = canvas.geometry().graph();
		var node = graph.nodes().get(uri);
		if (node == null) node = graph.resources().stream().map(ModelDependencyGraph.Resource::file)
				.filter(file -> file.uri().equals(uri)).findFirst().orElseThrow();
		if (node.state() == State.MISSING) {
			GAMA.getGui().getDialogFactory().warning(node.path() + "\n" + String.join("\n", node.diagnostics()));
			return;
		}
		if (!uri.isFile() && !uri.isPlatformResource()) {
			GAMA.getGui().getDialogFactory().warning("Remote resource (not downloaded by Dependencies):\n" + uri);
			return;
		}
		FileOpener.openFile(uri);
	}

	@Override
	public void saveState(final IMemento memento) {
		super.saveState(memento);
		if (input != null) memento.putString("model", input.toString());
		memento.putBoolean("followEditor", followEditor);
		memento.putBoolean("showResources", showResources);
	}

	@Override
	public void setFocus() {
		if (canvas != null && !canvas.isDisposed()) canvas.setFocus();
	}

	@Override
	public void dispose() {
		disposed = true;
		request++;
		automaticRefreshJob.cancel();
		syncWithEditorJob.cancel();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
		watchedFiles = Set.of();
		getSite().getPage().removePartListener(editorListener);
		if (job != null) job.cancel();
		super.dispose();
	}
}
