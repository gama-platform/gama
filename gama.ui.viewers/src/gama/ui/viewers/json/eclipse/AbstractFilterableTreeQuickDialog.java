/*******************************************************************************************************
 *
 * AbstractFilterableTreeQuickDialog.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;

import gama.dev.DEBUG;
import gama.ui.viewers.json.FilterPatternMatcher;
import gama.ui.viewers.json.eclipse.outline.FallbackOutlineContentProvider;

/**
 * The Class AbstractFilterableTreeQuickDialog.
 *
 * @param <T>
 *            the generic type
 */
public abstract class AbstractFilterableTreeQuickDialog<T> extends AbstractQuickDialog implements IDoubleClickListener {

	/** The Constant DO_SHOW_DIALOG. */
	private static final boolean DO_SHOW_DIALOG = SHOW_DIALOG_MENU;

	/** The Constant DEFAULT_X. */
	private static final int DEFAULT_X = 600;

	/** The Constant DEFAULT_Y. */
	private static final int DEFAULT_Y = 400;

	/** The input. */
	private Object input;

	/** The monitor. */
	private final Object monitor = new Object();

	/** The text. */
	private Text text;

	/** The tree viewer. */
	private TreeViewer treeViewer;

	/** The current used filter text. */
	private String currentUsedFilterText;

	/** The content provider. */
	private ITreeContentProvider contentProvider;

	/** The text filter. */
	private AbstractTreeViewerFilter<T> textFilter;

	/** The matcher. */
	private FilterPatternMatcher<T> matcher;

	/** The min width. */
	private final int minWidth;

	/** The min height. */
	private final int minHeight;

	/**
	 * Creates a quick outline dialog containing a filterable tree
	 *
	 * @param adaptable
	 *            adaptable which can be used by child class implementations
	 * @param parent
	 *            shell to use is null the outline will have no content! If the gradle editor is null location setting
	 *            etc. will not work.
	 * @param title
	 *            title for dialog
	 * @param minWidth
	 * @param minHeight
	 * @param infoText
	 *            additional information to show at the bottom of dialogs
	 */
	public AbstractFilterableTreeQuickDialog(final IAdaptable adaptable, final Shell parent, final String title,
			final int minWidth, final int minHeight, final String infoText) {
		super(parent, PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE, GRAB_FOCUS, PERSIST_SIZE, PERSIST_BOUNDS, DO_SHOW_DIALOG,
				SHOW_PERSIST_ACTIONS, title, infoText);
		this.minWidth = minWidth;
		this.minHeight = minHeight;

		contentProvider = createTreeContentProvider(adaptable);

		if (contentProvider == null) { contentProvider = new FallbackOutlineContentProvider(); }
	}

	/**
	 * Creates the tree content provider.
	 *
	 * @param adaptable
	 *            the adaptable
	 * @return the i tree content provider
	 */
	protected abstract ITreeContentProvider createTreeContentProvider(IAdaptable adaptable);

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		openSelectionAndCloseDialog(selection);
	}

	/**
	 * Open selection and close dialog.
	 *
	 * @param selection
	 *            the selection
	 */
	private final void openSelectionAndCloseDialog(final ISelection selection) {
		openSelection(selection);
		close();
	}

	/**
	 * Open selection.
	 *
	 * @param selection
	 *            the selection
	 */
	private final void openSelection(final ISelection selection) {
		String filterText = null;
		if (text != null && !text.isDisposed()) { filterText = text.getText(); }
		openSelectionImpl(selection, filterText);
	}

	/**
	 * Open selection
	 *
	 *
	 * @param filterText
	 *            the filter as text or <code>null</code> if not filtered
	 * @param selected
	 *            selected
	 */
	protected abstract void openSelectionImpl(ISelection selection, String filterText);

	/**
	 * Set input to show
	 *
	 * @param input
	 */
	public final void setInput(final Object input) { this.input = input; }

	@Override
	protected final void beforeRunEventLoop() {
		treeViewer.setInput(input);

		text.setFocus();

		T item = getInitialSelectedItem();
		if (item == null) return;
		StructuredSelection startSelection = new StructuredSelection(item);
		treeViewer.setSelection(startSelection, true);
	}

	/**
	 * Gets the initial selected item.
	 *
	 * @return the initial selected item
	 */
	protected abstract T getInitialSelectedItem();

	@Override
	protected boolean canHandleShellCloseEvent() {
		return true;
	}

	@Override
	protected final Control createDialogArea(final Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		boolean isWin32 = Util.isWindows();
		GridLayoutFactory.fillDefaults().extendedMargins(isWin32 ? 0 : 3, 3, 2, 2).applyTo(composite);

		IBaseLabelProvider labelProvider = createLabelProvider();
		if (labelProvider == null) { labelProvider = new LabelProvider(); }
		int style = SWT.NONE;
		Tree tree = new Tree(composite, SWT.SINGLE | style & ~SWT.MULTI);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = tree.getItemHeight() * 12;

		gridData.verticalAlignment = SWT.TOP;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		tree.setLayoutData(gridData);

		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(contentProvider);

		/* filter */
		textFilter = createFilter();
		matcher = createItemMatcher();
		textFilter.setMatcher(matcher);
		treeViewer.setFilters(textFilter);

		tree.setLayoutData(gridData);

		treeViewer.setContentProvider(contentProvider);
		treeViewer.addDoubleClickListener(this);
		treeViewer.setLabelProvider(labelProvider);

		return composite;
	}

	/**
	 * Creates the item matcher.
	 *
	 * @return the filter pattern matcher
	 */
	protected abstract FilterPatternMatcher<T> createItemMatcher();

	/**
	 * Creates the label provider.
	 *
	 * @return the i base label provider
	 */
	protected abstract IBaseLabelProvider createLabelProvider();

	/**
	 * Creates the filter.
	 *
	 * @return the abstract tree viewer filter
	 */
	protected abstract AbstractTreeViewerFilter<T> createFilter();

	@Override
	protected Control createInfoTextArea(final Composite parent) {
		return super.createInfoTextArea(parent);
	}

	@Override
	protected Control createTitleControl(final Composite parent) {
		text = new Text(parent, SWT.NONE);

		GridData textLayoutData = new GridData();
		textLayoutData.horizontalAlignment = GridData.FILL;
		textLayoutData.verticalAlignment = GridData.FILL;
		textLayoutData.grabExcessHorizontalSpace = true;
		textLayoutData.grabExcessVerticalSpace = false;
		textLayoutData.horizontalSpan = 2;

		text.setLayoutData(textLayoutData);

		text.addKeyListener(new FilterKeyListener());

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(DO_SHOW_DIALOG ? 1 : 2, 1)
				.applyTo(text);

		return text;
	}

	@Override
	protected final IDialogSettings getDialogSettings() {
		AbstractUIPlugin activator = getUIPlugin();
		if (activator == null) return null;
		return activator.getDialogSettings();
	}

	/**
	 * Gets the UI plugin.
	 *
	 * @return the UI plugin
	 */
	protected abstract AbstractUIPlugin getUIPlugin();

	@Override
	protected Point getInitialLocation(final Point initialSize) {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings == null) /* no dialog settings available, so fall back to min settings */
			return new Point(DEFAULT_X, DEFAULT_Y);
		return super.getInitialLocation(initialSize);
	}

	@Override
	protected Point getInitialSize() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings == null) /* no dialog settings available, so fall back to min settings */
			return new Point(minWidth, minHeight);
		Point point = super.getInitialSize();
		if (point.x < minWidth) { point.x = minWidth; }
		if (point.y < minHeight) { point.y = minHeight; }
		return point;
	}

	@Override
	protected boolean hasInfoArea() {
		return super.hasInfoArea();
	}

	/**
	 * Rebuild filter text pattern.
	 */
	private void rebuildFilterTextPattern() {
		if (text == null || text.isDisposed()) return;
		String filterText = text.getText();
		if (filterText == null) {
			if (currentUsedFilterText == null) /* same as before */
				return;
		} else if (filterText.equals(currentUsedFilterText)) /* same as before */
			return;

		matcher.setFilterText(filterText);

		currentUsedFilterText = filterText;

	}

	/**
	 * The listener interface for receiving filterKey events. The class that is interested in processing a filterKey
	 * event implements this interface, and the object created with that class is registered with a component using the
	 * component's <code>addFilterKeyListener</code> method. When the filterKey event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see FilterKeyEvent
	 */
	private class FilterKeyListener extends KeyAdapter {

		/** The dirty. */
		private boolean dirty;

		@Override
		public void keyPressed(final KeyEvent event) {
			if (event.keyCode == SWT.ARROW_DOWN) {
				Tree tree = treeViewer.getTree();
				if (tree.isDisposed() || tree.isFocusControl()) return;
				tree.setFocus();
				return;
			}
			if (event.character == '\r') {
				ISelection selection = treeViewer.getSelection();
				openSelectionAndCloseDialog(selection);
				return;
			}
			boolean allowedChar = false;
			allowedChar = allowedChar || event.character == '*';
			allowedChar = allowedChar || event.character == '(';
			allowedChar = allowedChar || event.character == ')';
			allowedChar = allowedChar || Character.isJavaIdentifierPart(event.character);
			allowedChar = allowedChar || Character.isWhitespace(event.character);
			if (!allowedChar) {
				event.doit = false;
				return;
			}
			if (treeViewer == null) {}

		}

		@Override
		public void keyReleased(final KeyEvent e) {
			String filterText = text.getText();
			if (filterText != null && filterText.equals(currentUsedFilterText)) /*
																				 * same text, occurs when only cursor
																				 * keys used etc. avoid flickering
																				 */
				return;
			synchronized (monitor) {
				if (dirty) return;
				dirty = true;
			}

			UIJob job = new UIJob("Rebuild json editor quick outline") {

				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor) {
					try {
						rebuildFilterTextPattern();
						if (treeViewer.getControl().isDisposed()) return Status.CANCEL_STATUS;
						treeViewer.refresh();
						if (matcher.hasFilterPattern()) {
							/*
							 * something was entered into filter - so results must be expanded:
							 */
							treeViewer.expandAll();
							selectFirstMaching();
						}
					} catch (RuntimeException e) {
						DEBUG.ERR("quick dialog failure", e);
					}
					dirty = false;
					return Status.OK_STATUS;
				}
			};
			job.schedule(400);
		}

		/**
		 * Select first maching.
		 */
		protected void selectFirstMaching() {
			selectfirstMatching(getTreeContentProvider().getElements(null));
		}

		/**
		 * Selectfirst matching.
		 *
		 * @param elements
		 *            the elements
		 * @return true, if successful
		 */
		private boolean selectfirstMatching(final Object[] elements) {
			if (treeViewer == null || textFilter == null || elements == null) return false;
			for (Object element : elements) {
				if (Boolean.TRUE.equals(textFilter.isMatchingOrNull(element))) {
					StructuredSelection selection = new StructuredSelection(element);
					treeViewer.setSelection(selection, true);
					return true;
				}
				ITreeContentProvider contentProvider = getTreeContentProvider();
				Object[] children = contentProvider.getChildren(element);
				boolean selectionDone = selectfirstMatching(children);
				if (selectionDone) return true;

			}
			return false;
		}

		/**
		 * Gets the tree content provider.
		 *
		 * @return the tree content provider
		 */
		private ITreeContentProvider getTreeContentProvider() { return contentProvider; }
	}

}
