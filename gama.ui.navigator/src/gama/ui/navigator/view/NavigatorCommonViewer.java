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
import java.util.Objects;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;

import gama.api.GAMA;
import gama.api.ui.IStatusMessage;
import gama.dev.DEBUG;
import gama.ui.navigator.view.contents.LinkedFile;
import gama.ui.navigator.view.contents.NavigatorRoot;
import gama.ui.navigator.view.contents.TopLevelFolder;
import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.navigator.view.contents.VirtualContent;
import gama.ui.navigator.view.contents.VirtualContent.VirtualContentType;
import gama.ui.navigator.view.contents.WrappedResource;
import gama.ui.navigator.view.contents.WrappedSyntacticContent;
import gama.ui.shared.resources.IGamaColors;

/**
 * The Class NavigatorCommonViewer.
 */
public class NavigatorCommonViewer extends CommonViewer {

	private record ContentKey(Class<?> type, Object parent, Object identifier) {}

	/** Refreshes recreate outline and metadata nodes, so widget identity must not depend on their wrappers. */
	private static final IElementComparer CONTENT_COMPARER = new IElementComparer() {

		@Override
		public boolean equals(final Object a, final Object b) {
			return a == b || Objects.equals(key(a), key(b));
		}

		@Override
		public int hashCode(final Object element) {
			return Objects.hashCode(key(element));
		}

		private Object key(final Object element) {
			if (element instanceof WrappedResource<?, ?> resource) return resource.getResource();
			if (element instanceof VirtualContent<?> content) {
				Object identifier = content.getName();
				if (content instanceof WrappedSyntacticContent syntax && syntax.getURI() != null) {
					identifier = syntax.getURI();
				} else if (content instanceof LinkedFile link && link.getTarget() != null) {
					identifier = link.getTarget().getResource();
				}
				return new ContentKey(content.getClass(), key(content.getParent()), identifier);
			}
			return element;
		}
	};

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
	protected void init() {
		setComparer(CONTENT_COMPARER);
		setUseHashlookup(true);
		setContentProvider(getNavigatorContentService().createCommonContentProvider());
		setLabelProvider(new NavigatorDecoratingLabelProvider(
				getNavigatorContentService().createCommonLabelProvider()) {
			private final StyledString.Styler decorationStyle = new StyledString.Styler() {
				@Override
				public void applyStyles(final TextStyle textStyle) {
					textStyle.foreground = IGamaColors.NICE.color();
				}
			};

			@Override
			protected StyledString.Styler getDecorationStyle(final Object element) {
				return decorationStyle;
			}
		});
		initDragAndDrop();
	}

	/**
	 * Scrolls after SWT has laid out an unfolded branch, keeping its parent visible for large branches.
	 */
	public void revealExpandedChildren(final Object element) {
		getControl().getDisplay().asyncExec(() -> {
			if (getControl().isDisposed()) return;
			if (!(findItem(element) instanceof TreeItem item) || !item.getExpanded() || item.getItemCount() == 0)
				return;
			TreeItem last = item.getItem(item.getItemCount() - 1);
			while (last.getExpanded() && last.getItemCount() > 0) {
				last = last.getItem(last.getItemCount() - 1);
			}
			final var area = getTree().getClientArea();
			// Leave the following branch visible too, if it fits together with the unfolded parent.
			for (TreeItem current = item; current != null; current = current.getParentItem()) {
				final TreeItem parent = current.getParentItem();
				final int index = parent == null ? getTree().indexOf(current) : parent.indexOf(current);
				final int count = parent == null ? getTree().getItemCount() : parent.getItemCount();
				if (index + 1 >= count) continue;
				final TreeItem next = parent == null ? getTree().getItem(index + 1) : parent.getItem(index + 1);
				final var bounds = next.getBounds();
				if (bounds.y + bounds.height - item.getBounds().y <= area.height) { last = next; }
				break;
			}
			final int bottom = last.getBounds().y + last.getBounds().height;
			if (bottom <= area.y + area.height) return;
			if (bottom - item.getBounds().y > area.height) {
				getTree().setTopItem(item);
			} else {
				getTree().showItem(last);
			}
		});
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
				if (o instanceof TopLevelFolder || o instanceof UserGroupFolder || o instanceof IContainer) {
					expandToLevel(o, AbstractTreeViewer.ALL_LEVELS); // 2
				}
			}

		}
		NavigatorContentProvider.FILE_CHILDREN_ENABLED = true;
		this.refresh(false);
		getControl().setRedraw(true);

	}

}
