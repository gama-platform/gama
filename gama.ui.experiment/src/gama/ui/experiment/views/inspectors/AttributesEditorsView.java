/*******************************************************************************************************
 *
 * AttributesEditorsView.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.views.inspectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import gama.ui.experiment.parameters.EditorsList;
import gama.ui.shared.interfaces.IParameterEditor;
import gama.ui.shared.parameters.AbstractEditor;
import gama.ui.shared.parameters.EditorsGroup;
import gama.ui.shared.views.ExpandableItemsView;

/**
 * The Class AttributesEditorsView.
 *
 * @param <T>
 *            the generic type
 */
public abstract class AttributesEditorsView<T> extends ExpandableItemsView<T> {

	/** The editors. */
	protected EditorsList<T> editors;

	@Override
	public String getItemDisplayName(final T obj, final String previousName) {
		if (editors == null) return "";
		return editors.getItemDisplayName(obj, previousName);
	}

	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	protected Composite createItemContentsFor(final T data) {
		final EditorsGroup compo = new EditorsGroup(getViewer());
		if (editors != null) {
			final Map<String, IParameterEditor<?>> parameters = editors.getSections().get(data);
			if (parameters != null) {
				final List<AbstractEditor> list = new ArrayList(parameters.values());
				Collections.sort(list);
				for (final AbstractEditor<?> gpParam : list) {
					gpParam.createControls(compo);
					if (!editors.isEnabled(gpParam)) { gpParam.setActive(false); }
				}
			}
		}
		return compo;
	}

	@Override
	public void reset() {
		super.reset();
		editors = null;
	}

	@Override
	public void removeItem(final T obj) {
		if (editors == null) return;
		editors.removeItem(obj);
	}

	@Override
	public void pauseItem(final T obj) {
		if (editors == null) return;
		editors.pauseItem(obj);
	}

	@Override
	public void resumeItem(final T obj) {
		if (editors == null) return;
		editors.resumeItem(obj);
	}

	@Override
	public void focusItem(final T obj) {
		if (editors == null) return;
		editors.focusItem(obj);
	}

	@Override
	public List<T> getItems() {
		if (editors == null) return Collections.EMPTY_LIST;
		return editors.getItems();
	}

	@Override
	public void updateItemValues(final boolean synchronously) {
		if (editors != null) { editors.updateItemValues(synchronously); }
	}

}
