/*******************************************************************************************************
 *
 * CSVContentProvider.java, in gama.ui.shared.viewers, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.csv;

import org.eclipse.jface.viewers.*;

import gama.ui.viewers.csv.model.CSVModel;

/**
 *
 * @author fhenri
 *
 */
public class CSVContentProvider implements IStructuredContentProvider, ILazyContentProvider {

	private TableViewer viewer;

	/**
	 * Returns the elements to display in the table viewer
	 *
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(final Object element) {

		if(element instanceof CSVModel) {
			CSVModel model = (CSVModel) element;
			return model.getArrayRows(false);
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/**
	 * Notifies this content provider that the given viewer's input
	 * has been switched to a different element.
	 *
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		this.viewer = (TableViewer) viewer;
	}

	@Override
	public void updateElement(int index) {
		if (viewer != null && viewer.getInput() instanceof CSVModel) {
			CSVModel model = (CSVModel) viewer.getInput();
			Object[] rows = model.getArrayRows(false);

			rows = applyFiltersAndSorters(rows, model);

			if (index >= 0 && index < rows.length) {
				viewer.replace(rows[index], index);
			}
		}
	}

	private Object[] applyFiltersAndSorters(Object[] rows, CSVModel model) {
		if (viewer.getFilters() != null && viewer.getFilters().length > 0) {
			java.util.List<Object> filteredRows = new java.util.ArrayList<>();
			for (Object row : rows) {
				if (isRowSelected(model, row)) {
					filteredRows.add(row);
				}
			}
			rows = filteredRows.toArray();
		}

		if (viewer.getComparator() != null) {
			java.util.Arrays.sort(rows, new java.util.Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					return viewer.getComparator().compare(viewer, o1, o2);
				}
			});
		}

		return rows;
	}

	private boolean isRowSelected(CSVModel model, Object row) {
		for (ViewerFilter filter : viewer.getFilters()) {
			if (!filter.select(viewer, model, row)) {
				return false;
			}
		}
		return true;
	}
}
