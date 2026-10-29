/*******************************************************************************************************
 *
 * ErrorView.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.IRuntimeExceptionHandler;
import gama.api.ui.IGamaView;
import gama.api.ui.IGui;
import gama.api.ui.IItemList;
import gama.api.utils.prefs.GamaPreferences;
import gama.ui.shared.controls.ParameterExpandItem;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.utils.WebHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.ExpandableItemsView;

/**
 * The Class ErrorView.
 */
public class ErrorView extends ExpandableItemsView<GamaRuntimeException> implements IGamaView.Error {

	/** The id. */
	public static final String ID = IGui.ERROR_VIEW_ID;

	/** The number of displayed errors. */
	int numberOfDisplayedErrors = GamaPreferences.Runtime.CORE_ERRORS_NUMBER.getValue();

	/** The most recent first. */
	boolean mostRecentFirst = GamaPreferences.Runtime.CORE_RECENT.getValue();

	@Override
	protected boolean areItemsClosable() {
		return true;
	}

	@Override
	public boolean addItem(final GamaRuntimeException e) {
		createItem(getParentComposite(), e, false, null);
		return true;
	}

	/**
	 * Display errors.
	 *
	 * @param reset
	 *            the reset
	 */
	@Override
	public void displayErrors(final boolean reset) {
		if (reset) {
			reset();
		} else {
			WorkbenchHelper.run(() -> {
				displayItems();
				if (getParentComposite() != null && !getParentComposite().isDisposed()) {
					getParentComposite().layout(true, true);
				}
			});
		}

	}

	@Override
	public void ownCreatePartControl(final Composite view) {}

	// Experimental: creates a deferred item
	@Override
	protected ParameterExpandItem createItem(final Composite parent, final GamaRuntimeException data,
			final boolean expanded, final GamaUIColor color) {
		createViewer(parent);
		if (getViewer() == null) return null;
		getViewer().setSpacing(5);
		final Composite control = createItemContentsFor(data);
		ParameterExpandItem item = createItem(parent, data, control, false, color);
		item.onExpand(() -> createStackTrace(control, data));
		if (expanded) { item.setExpanded(true); }
		return item;
	}

	@Override
	protected Composite createItemContentsFor(final GamaRuntimeException exception) {
		final Composite compo = new Composite(getViewer(), SWT.NONE);
		compo.setBackground(getViewer().getBackground());
		compo.setBackgroundMode(SWT.INHERIT_FORCE);
		final GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 12;
		layout.marginHeight = 4;
		layout.verticalSpacing = 0;
		compo.setLayout(layout);
		return compo;
	}

	/**
	 * Creates the stack trace.
	 *
	 * @param compo
	 *            the compo
	 * @param exception
	 *            the exception
	 */
	private void createStackTrace(final Composite compo, final GamaRuntimeException exception) {
		final List<String> agents = exception.getAgentsNames();
		if (!agents.isEmpty()) {
			createDetail(compo, exception,
					(agents.size() == 1 ? "Agent: " : "Agents: ") + String.join(", ", agents));
		}
		for (String string : exception.getContextAsList()) {
			// The full agent list is already displayed above, without the context's truncation.
			if (string.isBlank() || string.startsWith("in agent ") || string.startsWith("in agents ")) { continue; }
			createDetail(compo, exception, string);
		}
		compo.requestLayout();
	}

	private void createDetail(final Composite parent, final GamaRuntimeException exception, final String text) {
		final Label label = new Label(parent, SWT.WRAP);
		label.setText(text.replace("&", "&&"));
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		label.setToolTipText(text);
		label.addListener(SWT.MouseUp, event -> {
			if (event.button == 1) {
				GAMA.getGui().getModelsManager().editModel(exception.getEditorContext());
			}
		});
	}

	@Override
	public void setFocus() {

	}

	/**
	 * Gets the exception handler.
	 *
	 * @return the exception handler
	 */
	private IRuntimeExceptionHandler getExceptionHandler() {
		return WorkbenchHelper.getService(IRuntimeExceptionHandler.class);
	}

	@Override
	public void removeItem(final GamaRuntimeException obj) {
		getExceptionHandler().remove(obj);
	}

	@Override
	public void pauseItem(final GamaRuntimeException obj) {}

	@Override
	public void resumeItem(final GamaRuntimeException obj) {}

	@Override
	public String getItemDisplayName(final GamaRuntimeException obj, final String previousName) {
		final StringBuilder sb = new StringBuilder(300);
		sb.append("Cycle ").append(obj.getCycle()).append(IItemList.SEPARATION_CODE)
				.append(obj.isWarning() ? IItemList.WARNING_CODE : IItemList.ERROR_CODE).append(obj.getMessage());
		return sb.toString();
	}

	@Override
	public void focusItem(final GamaRuntimeException data) {
		// gotoEditor(data);
	}

	@Override
	public List<GamaRuntimeException> getItems() {
		final List<GamaRuntimeException> errors = new ArrayList<>();
		final List<GamaRuntimeException> exceptions = getExceptionHandler().getCleanExceptions();
		final int size = exceptions.size();
		if (size == 0) return errors;
		final int end = size;
		int begin = end - numberOfDisplayedErrors;
		begin = begin < 0 ? 0 : begin;
		final List<GamaRuntimeException> except = new ArrayList<>(exceptions);
		errors.addAll(except.subList(begin, end));
		if (mostRecentFirst) { Collections.reverse(errors); }
		return errors;
	}

	@Override
	public void updateItemValues(final boolean synchronously, final boolean retrieveValues) {
		this.getViewer().updateItemNames();
	}

	@Override
	public void reset() {
		WorkbenchHelper.run(() -> {
			ErrorView.super.reset();
			displayItems();
			if (getParentComposite() != null && !getParentComposite().isDisposed()) {
				getParentComposite().layout(true, true);
			}
		});

	}

	/**
	 * Method handleMenu()
	 *
	 * @see gama.api.ui.IItemList#handleMenu(java.lang.Object)
	 */
	@Override
	public Map<String, Runnable> handleMenu(final GamaRuntimeException item, final int x, final int y) {
		final Map<String, Runnable> result = new HashMap<>();
		result.put("Copy error to clipboard", () -> { WorkbenchHelper.copy(item.getAllText()); });
		result.put("Show in editor", () -> GAMA.getGui().getModelsManager().editModel(item.getEditorContext()));
		result.put("Report issue on GitHub", () -> this.reportError(item));
		return result;
	}

	/**
	 * Report error.
	 *
	 * @param item
	 *            the item
	 */
	private void reportError(final GamaRuntimeException item) {
		// final String data = item.getAllText();
		WebHelper.openPage("https://github.com/gama-platform/gama/issues/new");
	}

	@Override
	protected boolean needsOutput() {
		return false;
	}

	/**
	 * The ErrorView must never be auto-closed by {@code closeSimulationViews()} — its content is relevant precisely
	 * when there is no running simulation (e.g. after an init failure). Returning {@code false} also prevents
	 * {@link gama.ui.shared.views.GamaViewPart#init} from closing it when no experiment is running.
	 */
	@Override
	protected boolean shouldBeClosedWhenNoExperiments() {
		return false;
	}

	/**
	 * No-op: the ErrorView is intentionally never closed by the simulation lifecycle machinery. It can only be closed
	 * manually by the user (its items are closable). Overrides {@link GamaViewPart#close} which would otherwise hide
	 * this view every time {@code closeSimulationViews()} is called.
	 */
	@Override
	public void close(final gama.api.runtime.scope.IScope scope) {
		// deliberate no-op — see Javadoc above
	}

}
