/*******************************************************************************************************
 *
 * SyntaxErrorsView.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package org.eclipse.ui.internal.views.markers;

import static gama.ui.shared.resources.IGamaIcons.TOGGLE_INFOS;
import static gama.ui.shared.resources.IGamaIcons.TOGGLE_WARNINGS;

import java.util.HashMap;
import java.util.function.Consumer;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISources;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.MarkerSupportView;

import gama.api.GAMA;
import gama.api.compilation.validation.IValidationContext;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.dev.DEBUG;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;
import gaml.compiler.indexer.GamlResourceIndexer;
import gaml.compiler.resource.GamlResourceServices;
import gaml.compiler.ui.decorators.GamlMarkerUpdater;
import gaml.compiler.validation.GamlResourceValidator;

/**
 * The Class SyntaxErrorsView.
 */
public class SyntaxErrorsView extends MarkerSupportView implements IToolbarDecoratedView.Expandable {

	static {
		DEBUG.OFF();
	}

	/** The parent. */
	protected Composite parent;

	/** The toolbar. */
	protected GamaToolbar2 toolbar;

	/** The listener. */
	final BuildPreferenceChangeListener listener;

	private ProgressIndicator progress;
	private ToolItem validationAction;
	private String description = "";
	private String displayedTask;
	private boolean displayingBuild, animated;
	private int displayedWork;
	private volatile String buildTask = "";
	private volatile int totalWork;
	private volatile double completedWork;
	private volatile boolean building, workspaceBuilding;
	private final Consumer<String> compilationListener = name -> buildTask = "Compiling " + name;
	private final IResourceChangeListener workspaceBuildListener = event -> {
		if (event.getType() == IResourceChangeEvent.PRE_BUILD) {
			buildTask = "Validating projects...";
			workspaceBuilding = true;
		} else {
			workspaceBuilding = false;
		}
	};

	private final WorkspaceJob buildJob = new WorkspaceJob("Validate all projects") {
		@Override
		public IStatus runInWorkspace(final IProgressMonitor monitor) {
			totalWork = 0;
			completedWork = 0;
			buildTask = "Validating all projects...";
			building = true;
			try {
				doBuild(new ProgressMonitorWrapper(monitor) {
					@Override
					public void beginTask(final String name, final int total) {
						totalWork = total;
						completedWork = 0;
						setTaskName(name);
						super.beginTask(name, total);
					}

					@Override
					public void setTaskName(final String name) {
						if (name != null && !name.isBlank()) { buildTask = name; }
						super.setTaskName(name);
					}

					@Override
					public void subTask(final String name) {
						if (name != null && !name.isBlank()) { buildTask = name; }
						super.subTask(name);
					}

					@Override
					public void internalWorked(final double work) {
						completedWork += work;
						super.internalWorked(work);
					}

					@Override
					public void worked(final int work) {
						completedWork += work;
						super.worked(work);
					}
				});
				return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
			} catch (final OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} catch (final CoreException e) {
				return e.getStatus();
			} finally {
				building = false;
			}
		}
	};

	/**
	 * Instantiates a new syntax errors view.
	 */
	public SyntaxErrorsView() {
		super("gaml.compiler.gaml.ui.error.generator");
		listener = new BuildPreferenceChangeListener();
		GamaPreferences.Modeling.WARNINGS_ENABLED.addChangeListener(listener);
		GamaPreferences.Modeling.INFO_ENABLED.addChangeListener(listener);
		buildJob.setRule(GAMA.getWorkspaceManager().getWorkspace().getRuleFactory().buildRule());
		buildJob.setUser(true);
	}

	@Override
	public void createPartControl(final Composite compo) {

		this.parent = GamaToolbarFactory.createToolbars(this, compo);
		super.createPartControl(parent);
		final IToolBarManager tb = getViewSite().getActionBars().getToolBarManager();
		tb.removeAll();
		tb.update(true);

		MarkersTreeViewer viewer = this.getAdapter(MarkersTreeViewer.class);

		viewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(final Viewer viewer1, final Object parentElement, final Object element) {
				if (element instanceof MarkerItem item) {
					IMarker marker = item.getMarker();
					if (marker == null) return true;
					String text = marker.getAttribute(IMarker.MESSAGE, "");
					if (text.contains(IValidationContext.IMPORTED_FROM)) return false;
				}
				return true;
			}
		});
		GamlMarkerUpdater.addCompilationListener(compilationListener);
		GAMA.getWorkspaceManager().getWorkspace().addResourceChangeListener(workspaceBuildListener,
				IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.POST_BUILD);
	}

	@Override
	public void dispose() {
		GamaPreferences.Modeling.WARNINGS_ENABLED.removeChangeListener(listener);
		GamaPreferences.Modeling.INFO_ENABLED.removeChangeListener(listener);
		GamlMarkerUpdater.removeCompilationListener(compilationListener);
		GAMA.getWorkspaceManager().getWorkspace().removeResourceChangeListener(workspaceBuildListener);
		buildJob.cancel();
		super.dispose();
	}

	/**
	 * The listener interface for receiving buildPreferenceChange events. The class that is interested in processing a
	 * buildPreferenceChange event implements this interface, and the object created with that class is registered with
	 * a component using the component's <code>addBuildPreferenceChangeListener<code> method. When the
	 * buildPreferenceChange event occurs, that object's appropriate method is invoked.
	 *
	 * @see BuildPreferenceChangeEvent
	 */
	public class BuildPreferenceChangeListener implements IPreferenceAfterChangeListener<Boolean> {

		/**
		 *
		 * /**
		 *
		 * @see gama.api.utils.prefs.IPreferenceChangeListener#afterValueChange(java.lang.Object)
		 */
		@Override
		public void afterValueChange(final Boolean newValue) {
			build();
		}
	}

	@Override
	protected void setContentDescription(final String description) {
		this.description = description == null ? "" : description;
		if (!building && !workspaceBuilding) {
			updateToolbarStatus(this.description, false);
			displayedTask = this.description;
		}
	}

	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		this.toolbar = tb;
		progress = new ProgressIndicator(toolbar.getParent());
		progress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GamaToolbarSimple tbs = toolbar.getToolbar(SWT.RIGHT);
		tbs.button("editor/local.menu", "More...", "More options", e -> {

			final GamaMenu menu = new GamaMenu() {

				@Override
				protected void fillMenu() {
					final boolean warnings = GamaPreferences.Modeling.WARNINGS_ENABLED.getValue();
					GamaCommand.build(TOGGLE_WARNINGS, warnings ? "Hide warnings" : "Show warnings", null, e1 -> {
						GamaPreferences.Modeling.WARNINGS_ENABLED.set(!warnings).save();
					}).toItem(mainMenu);
					final boolean info = GamaPreferences.Modeling.INFO_ENABLED.getValue();
					GamaCommand.build(TOGGLE_INFOS, info ? "Hide info markers" : "Show info markers", null, e1 -> {
						GamaPreferences.Modeling.INFO_ENABLED.set(!info).save();
					}).toItem(mainMenu);
				}

			};
			menu.open(tbs, e, tbs.getSize().y, 0);
		});
		refreshProgress();
	}

	private void updateToolbarStatus(final String text, final boolean active) {
		toolbar.status(null, text, active ? null : e -> openFilterDialog(), null);
		validationAction = new ToolItem(toolbar.getToolbar(SWT.LEFT), SWT.PUSH, 0);
		validationAction.addListener(SWT.Selection, e -> {
			if (buildJob.getState() != Job.NONE) {
				buildJob.cancel();
			} else if (!building && !workspaceBuilding) {
				build();
			}
			updateValidationAction();
		});
		updateValidationAction();
		toolbar.requestLayout();
	}

	private void updateValidationAction() {
		final boolean cancellable = buildJob.getState() != Job.NONE;
		final boolean active = cancellable || building || workspaceBuilding;
		final GamaIcon icon = GamaIcon.named(active ? "toolbar/server.stop" : "toolbar/validate.all");
		validationAction.setImage(icon.image());
		validationAction.setDisabledImage(icon.disabled());
		validationAction.setToolTipText(active ? "Cancel validation" : "Validate all projects");
		validationAction.setEnabled(!active || cancellable);
	}

	private void refreshProgress() {
		if (toolbar.isDisposed() || progress.isDisposed()) return;
		final boolean active = building || workspaceBuilding;
		final boolean indeterminate = !building || totalWork <= 0;
		final String text = active ? buildTask : description;
		if (active != displayingBuild || !text.equals(displayedTask)) {
			updateToolbarStatus(text, active);
			displayedTask = text;
		}
		updateValidationAction();
		if (active) {
			if (!displayingBuild || animated != indeterminate) {
				progress.done();
				if (indeterminate) {
					progress.beginAnimatedTask();
				} else {
					progress.beginTask(1000);
				}
				displayedWork = 0;
				animated = indeterminate;
			}
			if (!indeterminate) {
				final int work = Math.min(1000, (int) (1000 * completedWork / totalWork));
				if (work > displayedWork) {
					progress.worked(work - displayedWork);
					displayedWork = work;
				}
			}
		} else if (displayingBuild) {
			progress.done();
		}
		displayingBuild = active;
		toolbar.getDisplay().timerExec(100, this::refreshProgress);
	}

	/**
	 * Open filter dialog.
	 */
	void openFilterDialog() {
		final IEvaluationContext ec = new EvaluationContext(null, this);
		ec.addVariable(ISources.ACTIVE_PART_NAME, this);
		final ExecutionEvent ev = new ExecutionEvent(null, new HashMap<>(), this, ec);
		new ConfigureContentsDialogHandler().execute(ev);
	}

	/**
	 * Do build.
	 *
	 * @param monitor
	 *            the monitor
	 */
	static private void doBuild(final IProgressMonitor monitor) throws CoreException {
		GamlResourceServices.getResourceDocumenter().invalidateAll();
		GamlResourceIndexer.eraseIndex();

		GamlResourceValidator.RESET();
		final SubMonitor progress = SubMonitor.convert(monitor, "Validating all projects", 100);
		final var workspace = GAMA.getWorkspaceManager().getWorkspace();
		try {
			workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, progress.split(20));
			workspace.build(IncrementalProjectBuilder.FULL_BUILD, progress.split(80));
		} finally {
			progress.done();
		}
	}

	/**
	 * Builds the.
	 */
	void build() {
		buildJob.schedule();
	}

	/**
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Expandable#expandAll()
	 */
	@Override
	public void expandAll() {
		getAdapter(MarkersTreeViewer.class).expandAll();
	}

	/**
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Expandable#collapseAll()
	 */
	@Override
	public void collapseAll() {
		getAdapter(MarkersTreeViewer.class).collapseAll();
	}

	/**
	 * Update title image.
	 *
	 * @param counts
	 *            the counts
	 */
	@Override
	void updateTitleImage(final Integer[] counts) {
		Image image = GamaIcon.named("views/tabs/view.problems").image();
		if (counts[0].intValue() > 0) {
			image = GamaIcon.named(IGamaIcons.MARKER_ERROR).image();
		} else if (counts[1].intValue() > 0) {
			image = GamaIcon.named(IGamaIcons.MARKER_WARNING).image();
		} else if (counts[2].intValue() > 0) { image = GamaIcon.named(IGamaIcons.MARKER_INFO).image(); }
		setTitleImage(image);
	}

}
