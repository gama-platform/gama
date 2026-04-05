/*******************************************************************************************************
 *
 * AbstractTreeViewerFilter.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import gama.ui.viewers.json.Matcher;

/**
 * The Class AbstractTreeViewerFilter.
 *
 * @param <T>
 *            the generic type
 */
public abstract class AbstractTreeViewerFilter<T> extends ViewerFilter {

	/** The matcher. */
	private Matcher<T> matcher;

	/**
	 * Instantiates a new abstract tree viewer filter.
	 */
	public AbstractTreeViewerFilter() {}

	/**
	 * Sets the matcher.
	 *
	 * @param matcher
	 *            the new matcher
	 */
	public void setMatcher(final Matcher<T> matcher) { this.matcher = matcher; }

	@Override
	public Object[] filter(final Viewer viewer, final TreePath parentPath, final Object[] elements) {
		int size = elements.length;
		ArrayList<Object> out = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			Object element = elements[i];
			if (selectTreePath(viewer, parentPath, element)) { out.add(element); }
		}
		return out.toArray();
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		return selectTreePath(viewer, new TreePath(new Object[] { parentElement }), element);
	}

	/**
	 * Select tree path.
	 *
	 * @param viewer
	 *            the viewer
	 * @param parentPath
	 *            the parent path
	 * @param element
	 *            the element
	 * @return true, if successful
	 */
	private boolean selectTreePath(final Viewer viewer, final TreePath parentPath, final Object element) {
		// Cut off children of elements that are shown repeatedly.
		for (int i = 0; i < parentPath.getSegmentCount() - 1; i++) {
			if (element.equals(parentPath.getSegment(i))) return false;
		}

		if (!(viewer instanceof TreeViewer treeViewer) || matcher == null) return true;
		Boolean matchingResult = isMatchingOrNull(element);
		if (matchingResult != null) return matchingResult;
		return hasUnfilteredChild(treeViewer, parentPath, element);
	}

	/**
	 * Checks if is matching or null.
	 *
	 * @param element
	 *            the element
	 * @return the boolean
	 */
	@SuppressWarnings ("unchecked")
	Boolean isMatchingOrNull(final Object element) {
		T item = null;
		try {
			item = (T) element;
		} catch (ClassCastException e) {
			return Boolean.FALSE;
		}
		if (matcher.matches(item)) return Boolean.TRUE;
		/* maybe children are matching */
		return null;
	}

	/**
	 * Checks for unfiltered child.
	 *
	 * @param viewer
	 *            the viewer
	 * @param parentPath
	 *            the parent path
	 * @param element
	 *            the element
	 * @return true, if successful
	 */
	private boolean hasUnfilteredChild(final TreeViewer viewer, final TreePath parentPath, final Object element) {
		TreePath elementPath = parentPath.createChildPath(element);
		IContentProvider contentProvider = viewer.getContentProvider();
		Object[] children = contentProvider instanceof ITreePathContentProvider i ? i.getChildren(elementPath)
				: ((ITreeContentProvider) contentProvider).getChildren(element);

		/* avoid NPE + guard close */
		if (children == null || children.length == 0) return false;
		for (Object child : children) { if (selectTreePath(viewer, elementPath, child)) return true; }
		return false;
	}

}