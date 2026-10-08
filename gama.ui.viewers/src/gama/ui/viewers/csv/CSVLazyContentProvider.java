/*******************************************************************************************************
 *
 * CSVLazyContentProvider.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.csv;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import gama.ui.viewers.csv.model.CSVModel;
import gama.ui.viewers.csv.model.CSVRow;

/**
 * Provides CSV rows to a virtual table viewer on demand.
 * <p>
 * This provider avoids creating an intermediate array of all data rows. Instead, it asks the {@link CSVModel} for the
 * requested displayed row and replaces the visible table item directly.
 * </p>
 *
 * @author fhenri
 */
public class CSVLazyContentProvider implements ILazyContentProvider {

	/** The viewer receiving row replacements in lazy mode. */
	private TableViewer tableViewer;

	/** The model currently used as viewer input. */
	private CSVModel model;

	/**
	 * Updates one visible element in the virtual table.
	 *
	 * @param index
	 *            the row index requested by the table viewer
	 */
	@Override
	public void updateElement(final int index) {
		if (tableViewer == null || model == null) return;
		final CSVRow row = model.getDataRowAt(index);
		if (row != null) { tableViewer.replace(row, index); }
	}

	/**
	 * Disposes this content provider.
	 *
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		tableViewer = null;
		model = null;
	}

	/**
	 * Notifies this content provider that the given viewer's input has changed.
	 *
	 * @param viewer
	 *            the viewer receiving the content
	 * @param oldInput
	 *            the previous input
	 * @param newInput
	 *            the new input
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		tableViewer = viewer instanceof TableViewer ? (TableViewer) viewer : null;
		model = newInput instanceof CSVModel ? (CSVModel) newInput : null;
		if (tableViewer != null && model != null) { tableViewer.setItemCount(model.getDataRowCount()); }
	}
}
