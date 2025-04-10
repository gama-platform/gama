/*******************************************************************************************************
 *
 * SyntaxErrorsView.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.views;

import static gaml.compiler.gaml.validation.GamlResourceValidator.DURATION;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.views.markers.ConfigureContentsDialogHandler;
import org.eclipse.ui.internal.views.markers.MarkersTreeViewer;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.MarkerSupportView;

import gama.core.common.preferences.GamaPreferences;
import gama.core.common.preferences.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.dev.DEBUG;
import gama.gaml.descriptions.ValidationContext;
import gama.ui.shared.commands.TestsRunner;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;
import gaml.compiler.gaml.resource.GamlResourceServices;
import gaml.compiler.gaml.validation.GamlResourceValidator;

/**
 * The Class SyntaxErrorsView.
 */
public class SyntaxErrorsView extends MarkerSupportView implements IToolbarDecoratedView {

	static {
		DEBUG.ON();
	}

	/** The parent. */
	protected Composite parent;

	/** The toolbar. */
	protected GamaToolbar2 toolbar;

	/** The info action. */
	ToolItem warningAction, infoAction;

	/** The listener. */
	final BuildPreferenceChangeListener listener;

	/**
	 * Instantiates a new syntax errors view.
	 */
	public SyntaxErrorsView() {
		super("gaml.compiler.gaml.ui.error.generator");
		listener = new BuildPreferenceChangeListener(this);
		GamaPreferences.Modeling.WARNINGS_ENABLED.addChangeListener(listener);
		GamaPreferences.Modeling.INFO_ENABLED.addChangeListener(listener);
	}

	@Override
	public void createPartControl(final Composite compo) {
		this.parent = GamaToolbarFactory.createToolbars(this, compo);
		super.createPartControl(parent);
		MarkersTreeViewer viewer = this.getAdapter(MarkersTreeViewer.class);
		viewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
				if (element instanceof MarkerItem item) {
					IMarker marker = item.getMarker();
					if (marker == null) return true;
					String text = marker.getAttribute(IMarker.MESSAGE, "");
					if (text.contains(ValidationContext.IMPORTED_FROM)) return false;
				}
				return true;
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		GamaPreferences.Modeling.WARNINGS_ENABLED.removeChangeListener(listener);
		GamaPreferences.Modeling.INFO_ENABLED.removeChangeListener(listener);
	}

	/**
	 * The listener interface for receiving buildPreferenceChange events. The class that is interested in processing a
	 * buildPreferenceChange event implements this interface, and the object created with that class is registered with
	 * a component using the component's <code>addBuildPreferenceChangeListener<code> method. When the
	 * buildPreferenceChange event occurs, that object's appropriate method is invoked.
	 *
	 * @see BuildPreferenceChangeEvent
	 */
	public static class BuildPreferenceChangeListener implements IPreferenceAfterChangeListener<Boolean> {

		/** The view. */
		SyntaxErrorsView view;

		/**
		 * Instantiates a new builds the preference change listener.
		 *
		 * @param v
		 *            the v
		 */
		BuildPreferenceChangeListener(final SyntaxErrorsView v) {
			view = v;
		}

		/**
		 * @see gama.core.common.preferences.IPreferenceChangeListener#afterValueChange(java.lang.Object)
		 */
		@Override
		public void afterValueChange(final Boolean newValue) {
			build();
			view.checkActions();
		}
	}

	/**
	 * Check actions.
	 */
	void checkActions() {
		if (warningAction != null) { warningAction.setSelection(GamaPreferences.Modeling.WARNINGS_ENABLED.getValue()); }
		if (infoAction != null) { infoAction.setSelection(GamaPreferences.Modeling.INFO_ENABLED.getValue()); }
	}

	@Override
	protected void setContentDescription(final String description) {
		toolbar.status((Image) null, description, e -> openFilterDialog(), IGamaColors.BLUE, false, SWT.LEFT);
	}

	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		this.toolbar = tb;

		warningAction = tb.check(IGamaIcons.TOGGLE_WARNINGS, "", "Toggle display of warning markers", e -> {
			final boolean b = ((ToolItem) e.widget).getSelection();
			GamaPreferences.Modeling.WARNINGS_ENABLED.set(b).save();
		}, SWT.RIGHT);

		infoAction = tb.check(IGamaIcons.TOGGLE_INFOS, "", "Toggle display of information markers", e -> {
			final boolean b = ((ToolItem) e.widget).getSelection();
			GamaPreferences.Modeling.INFO_ENABLED.set(b).save();
		}, SWT.RIGHT);
		checkActions();
		tb.sep(GamaToolbarFactory.TOOLBAR_SEP, SWT.RIGHT);
		tb.button(IGamaIcons.BUILD_ALL, "", "Clean and validate all projects", e -> { build(); }, SWT.RIGHT);
		tb.button(IGamaIcons.TEST_RUN, "", "Run all tests", e -> TestsRunner.start(), SWT.RIGHT);
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
	static private void doBuild(final IProgressMonitor monitor) {
		GamlResourceServices.getResourceDocumenter().invalidateAll();
		GamlResourceIndexer.eraseIndex();

		try {
			DEBUG.BANNER("COMPIL", "Last compilation of all models", "in", String.valueOf(DURATION()));
			GamlResourceValidator.RESET();
			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Builds the.
	 */
	static void build() {
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
		dialog.setBlockOnOpen(false);
		dialog.setCancelable(true);
		dialog.setOpenOnRun(true);
		try {
			dialog.run(true, true, SyntaxErrorsView::doBuild);
		} catch (InvocationTargetException | InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
