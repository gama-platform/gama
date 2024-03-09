/*******************************************************************************************************
 *
 * ErrorView.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import gama.core.common.interfaces.IGamaView;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IRuntimeExceptionHandler;
import gama.core.common.interfaces.ItemList;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.ui.shared.controls.ParameterExpandItem;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.utils.PreferencesHelper;
import gama.ui.shared.utils.WebHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.ExpandableItemsView;

/**
 * The Class ErrorView.
 */
public class ErrorView extends ExpandableItemsView<GamaRuntimeException> implements IGamaView.Error {

	/** The id. */
	public static String ID = IGui.ERROR_VIEW_ID;

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
		final Composite control = createItemContentsFor(data);
		ParameterExpandItem item = createItem(parent, data, control, expanded, color);
		if (expanded) {
			createStackTrace(control, data);
		} else {
			item.onExpand(() -> createStackTrace(control, data));
		}
		return item;
	}

	@Override
	protected Composite createItemContentsFor(final GamaRuntimeException exception) {
		final Composite compo = new Composite(getViewer(), SWT.NONE);
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 5;
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
		final Table t = new Table(compo, SWT.H_SCROLL);
		t.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				GAMA.getGui().editModel(exception.getEditorContext());
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {}
		});
		final GridData firstColData = new GridData(SWT.FILL, SWT.FILL, true, true);
		t.setLayoutData(firstColData);
		final java.util.List<String> strings = exception.getContextAsList();
		t.setForeground(exception.isWarning() ? GamaColors.get(PreferencesHelper.WARNING_TEXT_COLOR.getValue()).color()
				: GamaColors.get(PreferencesHelper.ERROR_TEXT_COLOR.getValue()).color());
		final TableColumn c = new TableColumn(t, SWT.NONE);
		for (String string : strings) {
			if (string.isBlank()) { continue; }
			final TableItem item = new TableItem(t, SWT.NONE);

			item.setText(new String[] { string });
		}
		c.pack();
		t.setSize(t.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		t.requestLayout();
		compo.requestLayout();
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
		final String a = obj.getAgentSummary();
		if (a != null) { sb.append(a).append(" at "); }
		sb.append("cycle ").append(obj.getCycle()).append(ItemList.SEPARATION_CODE)
				.append(obj.isWarning() ? ItemList.WARNING_CODE : ItemList.ERROR_CODE).append(obj.getMessage());
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
	public void updateItemValues(final boolean synchronously) {
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
	 * @see gama.core.common.interfaces.ItemList#handleMenu(java.lang.Object)
	 */
	@Override
	public Map<String, Runnable> handleMenu(final GamaRuntimeException item, final int x, final int y) {
		final Map<String, Runnable> result = new HashMap<>();
		result.put("Copy error to clipboard", () -> { WorkbenchHelper.copy(item.getAllText()); });
		result.put("Show in editor", () -> GAMA.getGui().editModel(item.getEditorContext()));
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

}
