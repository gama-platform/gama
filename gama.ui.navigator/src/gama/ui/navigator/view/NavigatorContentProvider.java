/*******************************************************************************************************
 *
 * NavigatorContentProvider.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view;

import static gama.ui.navigator.view.contents.NavigatorRoot.getInstance;
import static org.eclipse.core.resources.IResourceChangeEvent.POST_BUILD;
import static org.eclipse.core.resources.IResourceChangeEvent.POST_CHANGE;
import static org.eclipse.core.resources.IResourceChangeEvent.PRE_DELETE;
import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.navigator.CommonViewer;

import gama.ui.navigator.view.contents.ResourceManager;
import gama.ui.navigator.view.contents.VirtualContent;

/**
 * The Class NavigatorContentProvider.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class NavigatorContentProvider extends WorkbenchContentProvider implements ITreePathContentProvider {

	/** The file children enabled. */
	public volatile static boolean FILE_CHILDREN_ENABLED = true;

	@Override
	public Object getParent(final Object element) {
		if (element instanceof VirtualContent) return ((VirtualContent) element).getParent();
		return super.getParent(element);
	}

	@Override
	public Object[] getChildren(final Object p) {
		if (p instanceof VirtualContent) return ((VirtualContent) p).getNavigatorChildren();
		return super.getChildren(p);
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof VirtualContent) return ((VirtualContent) element).hasChildren();
		return super.hasChildren(element);
	}

	@Override
	public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
		final CommonViewer viewer = (CommonViewer) v;
		final ResourceManager mapper = new ResourceManager(this, viewer);
		getInstance().resetVirtualFolders(mapper);
		getWorkspace().addResourceChangeListener(mapper, POST_CHANGE | PRE_DELETE | POST_BUILD);
		super.inputChanged(viewer, oldInput, newInput);
	}

	@Override
	public Object[] getChildren(final TreePath parentPath) {
		return getChildren(parentPath.getLastSegment());
	}

	@Override
	public boolean hasChildren(final TreePath path) {
		return hasChildren(path.getLastSegment());
	}

	@Override
	public TreePath[] getParents(final Object element) {
		final ArrayList segments = new ArrayList();
		Object parent = element;
		do {
			parent = getParent(parent);
			if (parent != null && parent != getInstance()) { segments.add(0, parent); }
		} while (parent != null && parent != getInstance());
		if (!segments.isEmpty()) return new TreePath[] { new TreePath(segments.toArray()) };
		return new TreePath[0];
	}

}
