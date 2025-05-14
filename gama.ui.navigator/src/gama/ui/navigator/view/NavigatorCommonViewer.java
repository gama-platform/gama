/*******************************************************************************************************
 *
 * NavigatorCommonViewer.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view;

import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.navigator.CommonViewer;

import gama.core.common.IStatusMessage;
import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.ui.navigator.view.contents.NavigatorRoot;
import gama.ui.navigator.view.contents.TopLevelFolder;
import gama.ui.navigator.view.contents.VirtualContent.VirtualContentType;

/**
 * The Class NavigatorCommonViewer.
 */
public class NavigatorCommonViewer extends CommonViewer {

	static {
		DEBUG.OFF();
	}

	/**
	 * Instantiates a new navigator common viewer.
	 *
	 * @param aViewerId
	 *            the a viewer id
	 * @param aParent
	 *            the a parent
	 * @param aStyle
	 *            the a style
	 */
	public NavigatorCommonViewer(final String aViewerId, final Composite aParent, final int aStyle) {
		super(aViewerId, aParent, aStyle);
	}

	@Override
	public void expandAll() {
		getControl().setRedraw(false);
		NavigatorContentProvider.FILE_CHILDREN_ENABLED = false;
		final IStructuredSelection currentSelection = (IStructuredSelection) getSelection();
		if (currentSelection == null || currentSelection.isEmpty()) {
			NavigatorRoot.getInstance().accept(content -> {
				if (content.getType() == VirtualContentType.PROJECT) {
					GAMA.getGui().getStatus().informStatus("Expanding projects", IStatusMessage.COMPILE_ICON);
					Widget w = internalExpand(content, true);
					if (w != null) { internalExpandToLevel(w, 3); }
				}
				return true;
			});

		} else {
			final Iterator<?> it = currentSelection.iterator();
			while (it.hasNext()) {
				final Object o = it.next();
				if (o instanceof TopLevelFolder || o instanceof IContainer) {
					expandToLevel(o, AbstractTreeViewer.ALL_LEVELS); // 2
				}
			}

		}
		NavigatorContentProvider.FILE_CHILDREN_ENABLED = true;
		this.refresh(false);
		getControl().setRedraw(true);

	}

}
