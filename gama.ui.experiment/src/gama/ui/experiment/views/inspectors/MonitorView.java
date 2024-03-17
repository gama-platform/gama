/*******************************************************************************************************
 *
 * MonitorView.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.views.inspectors;

import static gama.ui.shared.resources.GamaColors.get;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import gama.core.common.interfaces.IValue;
import gama.core.common.interfaces.ItemList;
import gama.core.metamodel.agent.IAgent;
import gama.core.outputs.IOutput;
import gama.core.outputs.MonitorOutput;
import gama.core.outputs.ValuedDisplayOutputFactory;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.dev.COUNTER;
import gama.gaml.compilation.GAML;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.ui.shared.parameters.EditorFactory;
import gama.ui.shared.parameters.EditorsGroup;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.ExpandableItemsView;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;

/**
 * @author Alexis Drogoul
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class MonitorView extends ExpandableItemsView<MonitorOutput> implements IToolbarDecoratedView.Pausable {

	@Override
	public void ownCreatePartControl(final Composite parent) {
		displayItems();
	}

	@Override
	protected boolean needsOutput() {
		return false;
	}

	@Override
	public void addOutput(final IOutput output) {
		super.addOutput(output);
		addItem((MonitorOutput) output);
	}

	@Override
	public boolean addItem(final MonitorOutput output) {
		if (output != null) {
			createItem(getParentComposite(), output, output.getValue() == null,
					output.getColor(null) == null ? null : get(output.getColor(null)));
			return true;
		}
		return false;

	}

	@Override
	protected Composite createItemContentsFor(final MonitorOutput output) {
		final EditorsGroup compo = new EditorsGroup(getViewer(), SWT.NONE);
		final Text titleEditor =
				(Text) EditorFactory.create(output.getScope(), compo, "Title:", output.getName(), true, newValue -> {
					output.setName(newValue);
					update(output);
				}).getEditor();

		IExpression expr;
		try {
			expr = GAML.compileExpression(output.getExpressionText(), output.getScope().getSimulation(), true);
		} catch (GamaRuntimeException e1) {
			// The expression is maybe dedicated to experiments (and not simulations) ?
			expr = GAML.compileExpression(output.getExpressionText(), output.getScope().getExperiment(), true);
		}

		final Text c = (Text) EditorFactory.createExpression(output.getScope(), compo, "Expression:",
				output.getValue() == null ? IExpressionFactory.NIL_EXPR : expr, newValue -> {
					output.setNewExpression((IExpression) newValue);
					update(output);
				}, Types.NO_TYPE).getEditor();

		c.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				getViewer().collapseItemWithData(output);
			}

		});
		titleEditor.addModifyListener(evt -> {
			output.setName(titleEditor.getText());
			update(output);
		});
		// outputs.add(output);
		// update(output);
		return compo;
	}

	@Override
	public void removeItem(final MonitorOutput o) {
		o.close();
		removeOutput(o);
	}

	@Override
	public void resumeItem(final MonitorOutput o) {
		if (o.isPaused()) { o.setPaused(false); }
		update(o);
	}

	@Override
	public void pauseItem(final MonitorOutput o) {
		o.setPaused(true);
		update(o);
	}

	@Override
	public String getItemDisplayName(final MonitorOutput o, final String previousName) {
		final StringBuilder sb = new StringBuilder(100);
		sb.setLength(0);
		sb.append(o.getName()).append(ItemList.SEPARATION_CODE).append(getValueAsString(o));
		if (o.isPaused()) { sb.append(" (paused)"); }
		return sb.toString();

	}

	/**
	 * Gets the value as string.
	 *
	 * @param o
	 *            the o
	 * @return the value as string
	 */
	public String getValueAsString(final MonitorOutput o) {
		final Object v = o.getLastValue();
		return v == null ? "nil" : v instanceof IValue ? ((IValue) v).serializeToGaml(true) : v.toString();
	}

	@Override
	public GamaColor getItemDisplayColor(final MonitorOutput o) {
		return o.getColor(null);
	}

	/**
	 * Creates the new monitor.
	 *
	 * @param scope
	 *            the scope
	 */
	@SuppressWarnings ("unused")
	public static void createNewMonitor(final IScope scope) {
		// TODO ADD the possibility to do it in several simulations
		new MonitorOutput(scope, "monitor" + COUNTER.COUNT(), "");
	}

	@Override
	public void reset() {
		disposeViewer();
		outputs.clear();
	}

	@Override
	public void focusItem(final MonitorOutput data) {
		outputs.remove(data);
		outputs.add(0, data);
	}

	@Override
	protected boolean areItemsClosable() {
		return true;
	}

	@Override
	protected boolean areItemsPausable() {
		return true;
	}

	@Override
	public List getItems() { return outputs; }

	@Override
	public void updateItemValues(final boolean synchronously) {}

	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		super.createToolItems(tb);
		tb.sep(GamaToolbarFactory.TOOLBAR_SEP, SWT.RIGHT);
		tb.button(IGamaIcons.MENU_ADD_MONITOR, "Add new monitor", "Add new monitor",
				e -> createNewMonitor(getOutput().getScope()), SWT.RIGHT);
	}

	// @Override
	// public void outputReloaded(final IDisplayOutput output) {
	//
	// }

	/**
	 * Method pauseChanged()
	 *
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView.Pausable#pauseChanged()
	 */
	@Override
	public void pauseChanged() {}

	/**
	 * Method handleMenu()
	 *
	 * @see gama.core.common.interfaces.ItemList#handleMenu(java.lang.Object, int, int)
	 */
	@Override
	public Map<String, Runnable> handleMenu(final MonitorOutput data, final int x, final int y) {
		final Map<String, Runnable> menu = new HashMap();
		final IExpression exp = data.getValue();
		if (exp == null) return null;
		final IType<?> type = exp.getGamlType();
		menu.put("Copy to clipboard", () -> { WorkbenchHelper.copy(getValueAsString(data)); });
		if (type.isNumber() || type.isContainer() && type.getContentType().isNumber()) {
			// menu.put("Open chart", () -> {});
			menu.put("Save as CSV", () -> { data.saveHistory(); });
		} else if (type.isAgentType()) {
			menu.put("Inspect", () -> { data.getScope().getGui().setSelectedAgent((IAgent) data.getLastValue()); });
		} else if (type.isContainer() && type.getContentType().isAgentType()) {
			menu.put("Browse",
					() -> { ValuedDisplayOutputFactory.browse((Collection<? extends IAgent>) data.getLastValue()); });
		}
		return menu;
	}

}
