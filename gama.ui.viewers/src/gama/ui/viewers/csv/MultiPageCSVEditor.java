/*******************************************************************************************************
 *
 * MultiPageCSVEditor.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.csv;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import gama.core.util.file.csv.AbstractCSVManipulator.Letters;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;
import gama.ui.viewers.csv.model.CSVModel;
import gama.ui.viewers.csv.model.CSVRow;
import gama.ui.viewers.csv.model.ICsvFileModelListener;
import gama.ui.viewers.csv.text.CSVTableFilter;
import gama.ui.viewers.csv.text.CSVTextEditor;

/**
 *
 * @author fhenri
 *
 */
public class MultiPageCSVEditor extends MultiPageEditorPart
		implements IResourceChangeListener, IToolbarDecoratedView.Sizable {

	/** The is page modified. */
	private boolean isPageModified;

	/** index of the source page */
	public static final int indexSRC = 1;
	/** index of the table page */
	public static final int indexTBL = 0;

	/** The text editor used in page 0. */
	protected CSVTextEditor editor;

	/** The table viewer used in page 1. */
	protected TableViewer tableViewer;

	/** The table sorter. */
	final CSVTableSorter tableSorter;

	/** The table filter. */
	final CSVTableFilter tableFilter;

	/** The model. */
	CSVModel model;

	/** The csv file listener. */
	private final ICsvFileModelListener csvFileListener = (row, rowIndex) -> tableModified();

	/**
	 * Creates a multi-page editor example.
	 */
	public MultiPageCSVEditor() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		tableFilter = new CSVTableFilter();
		tableSorter = new CSVTableSorter();
		// model = createCSVFile();
	}

	@Override
	public Control getSizableFontControl() {
		if (tableViewer == null) { return null; }
		return tableViewer.getTable();
	}

	/**
	 * Creates the pages of the multi-page editor.
	 *
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
	 */
	@Override
	protected void createPages() {
		try {
			model = new CSVModel(getFileFor(getEditorInput()));
			createTablePage();
			createSourcePage();
			updateTitle();
			populateTablePage();
			setActivePage(0);
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Gets the file for.
	 *
	 * @param input
	 *            the input
	 * @return the file for
	 */
	private static IFile getFileFor(final IEditorInput input) {
		if (input instanceof IFileEditorInput) { return ((IFileEditorInput) input).getFile(); }
		if (input instanceof IStorageEditorInput) {
			try {
				final IStorage storage = ((IStorageEditorInput) input).getStorage();
				if (storage instanceof IFile) { return (IFile) storage; }
			} catch (final CoreException ignore) {
				// intentionally blank
			}
		}
		return null;
	}

	/**
	 * Creates page 0 of the multi-page editor, which contains a text editor.
	 */
	private void createSourcePage() {
		try {
			editor = new CSVTextEditor(model.getCustomDelimiter());
			addPage(editor, getEditorInput());
			setPageText(indexSRC, "Text");
		} catch (final PartInitException e) {
			// ErrorDialog.openError(getSite().getShell(), "Error creating
			// nested text editor", null, e.getStatus());
		}
	}

	/**
	 *
	 */
	private void createTablePage() {
		final Composite parent = getContainer();
		final Composite intermediate = new Composite(parent, SWT.NONE);
		final Composite composite = GamaToolbarFactory.createToolbars(this, intermediate);
		tableViewer =
				new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.setUseHashlookup(true);
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// set the sorter for the table

		tableViewer.setComparator(tableSorter);
		// set a table filter
		tableViewer.addFilter(tableFilter);

		addPage(intermediate);
		setPageText(indexTBL, "Table");
	}

	/**
	 * Set Name of the file to the tab
	 */
	private void updateTitle() {
		final IEditorInput input = getEditorInput();
		setPartName(input.getName());
		setTitleToolTip(input.getToolTipText());
	}

	/**
	 * @throws Exception
	 */
	private void populateTablePage() {
		tableViewer.setContentProvider(new CSVContentProvider());
		// make the selection available
		getSite().setSelectionProvider(tableViewer);
		tableViewer.getTable().getDisplay().asyncExec(this::updateTableFromTextEditor);
	}

	/**
	 *
	 */
	public void tableModified() {
		tableViewer.refresh();
		final boolean wasPageModified = isPageModified;
		isPageModified = true;
		if (!wasPageModified) {
			firePropertyChange(IEditorPart.PROP_DIRTY);
			editor.validateEditorInputState(); // will invoke:
			// FileModificationValidator.validateEdit()
			// (expected by some repository
			// providers)
		}
	}

	/**
	 *
	 */
	void updateTableFromTextEditor() {
		model.removeModelListener(csvFileListener);
		model.setInput(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get());
		final TableColumn[] columns = tableViewer.getTable().getColumns();
		for (final TableColumn c : columns) { c.dispose(); }
		for (int i = 0; i < model.getHeader().size(); i++) {
			final TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
			final int index = i;
			column.getColumn().setText(model.getHeader().get(i));
			column.getColumn().setWidth(100);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			column.setLabelProvider(new CSVLabelProvider());
			addMenuItemToColumn(column.getColumn(), index);
		}
		tableViewer.setInput(model);
		model.addModelListener(csvFileListener);
		defineCellEditing();
	}

	/**
	 *
	 */
	void defineCellEditing() {
		final String[] columnProperties = new String[model.getColumnCount()];
		final CellEditor[] cellEditors = new CellEditor[model.getColumnCount()];

		for (int i = 0; i < model.getColumnCount(); i++) {
			columnProperties[i] = Integer.toString(i);
			cellEditors[i] = new TextCellEditor(tableViewer.getTable());
		}

		tableViewer.setColumnProperties(columnProperties);

		// XXX can be replaced by tableViewer.setEditingSupport()
		tableViewer.setCellEditors(cellEditors);
		tableViewer.setCellModifier(new CSVEditorCellModifier());

	}

	/**
	 * Find a column in the Table by its name
	 *
	 * @param columnName
	 * @return the index of the Column indicated by its name
	 */
	int findColumnForName(final String columnName) {
		final int index = -1;
		final TableColumn[] tableColumns = tableViewer.getTable().getColumns();
		for (int i = 0; i < tableColumns.length; i++) {
			final TableColumn column = tableColumns[i];
			if (columnName.equalsIgnoreCase(column.getText())) { return i; }
		}
		return index;
	}

	/**
	 * @param column
	 * @param index
	 */
	void addMenuItemToColumn(final TableColumn column, final int index) {
		// Setting the right sorter
		column.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				int dir = tableViewer.getTable().getSortDirection();
				switch (dir) {
					case SWT.UP:
						dir = SWT.DOWN;
						break;
					case SWT.DOWN:
						dir = SWT.NONE;
						break;
					case SWT.NONE:
						dir = SWT.UP;
						break;
				}
				tableSorter.setColumn(index, dir);
				tableViewer.getTable().setSortDirection(dir);
				if (dir == SWT.NONE) {
					tableViewer.getTable().setSortColumn(null);
				} else {
					tableViewer.getTable().setSortColumn(column);
				}
				tableViewer.refresh();
			}
		});

	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this <code>IWorkbenchPart</code> method disposes all
	 * nested editors. This method is automatically called when the editor is closed and marks the end of the editor's
	 * life cycle. It cleans up any platform resources, such as images, clipboard, and so on, which were created by this
	 * class.
	 *
	 * @see org.eclipse.ui.part.MultiPageEditorPart#dispose()
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document. If the save is successful, the part should fire a property changed event
	 * (PROP_DIRTY property), reflecting the new dirty state. If the save is canceled via user action, or for any other
	 * reason, the part should invoke setCanceled on the IProgressMonitor to inform the caller
	 *
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(final IProgressMonitor monitor) {
		if (getActivePage() == indexTBL && isPageModified) {
			updateTextEditorFromTable();
		} else {
			updateTableFromTextEditor();
		}
		isPageModified = false;
		editor.doSave(monitor);
		model.saveMetaData();
	}

	/**
	 * Returns whether the "Save As" operation is supported by this part.
	 *
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() { return true; }

	/**
	 * Saves the multi-page editor's document as another file. Also updates the text for page 0's tab, and updates this
	 * multi-page editor's input to correspond to the nested editor's.
	 *
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		if (getActivePage() == indexTBL && isPageModified) {
			updateTextEditorFromTable();
		} else {
			updateTableFromTextEditor();
		}
		isPageModified = false;
		editor.doSaveAs();
		setInput(editor.getEditorInput());
		updateTitle();
	}

	/**
	 * @see org.eclipse.ui.part.MultiPageEditorPart#handlePropertyChange(int)
	 */
	@Override
	protected void handlePropertyChange(final int propertyId) {
		if (propertyId == IEditorPart.PROP_DIRTY) { isPageModified = isDirty(); }
		super.handlePropertyChange(propertyId);
	}

	/**
	 * @see org.eclipse.ui.part.MultiPageEditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() { return isPageModified || super.isDirty(); }

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 *
	 * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
	 */
	@Override
	protected void pageChange(final int newPageIndex) {
		switch (newPageIndex) {
			case indexSRC:
				if (isDirty()) { updateTextEditorFromTable(); }
				break;
			case indexTBL:
				if (isDirty()) { updateTableFromTextEditor(); }
				break;
		}
		isPageModified = false;
		super.pageChange(newPageIndex);
	}

	/**
	 *
	 */
	private void updateTextEditorFromTable() {
		editor.getDocumentProvider().getDocument(editor.getEditorInput())
				.set(((CSVModel) tableViewer.getInput()).getTextRepresentation());
	}

	/**
	 * When the focus shifts to the editor, this method is called; it must then redirect focus to the appropriate editor
	 * based on which page is currently selected.
	 *
	 * @see org.eclipse.ui.part.MultiPageEditorPart#setFocus()
	 */
	@Override
	public void setFocus() {
		switch (getActivePage()) {
			case indexSRC:
				editor.setFocus();
				break;
			case indexTBL:
				tableViewer.getTable().setFocus();
				break;
		}
	}

	/**
	 * Closes all project files on project close.
	 *
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
			WorkbenchHelper.asyncRun(() -> {
				final IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
				for (final IWorkbenchPage page : pages) {
					if (((FileEditorInput) editor.getEditorInput()).getFile().getProject()
							.equals(event.getResource())) {
						final IEditorPart editorPart = page.findEditor(editor.getEditorInput());
						page.closeEditor(editorPart, true);
					}
				}
			});
		} else {

			final IResourceDelta delta = event.getDelta().findMember(getFileFor(getEditorInput()).getFullPath());
			if (delta != null) {
				// file deleted -- close the editor
				if (delta.getKind() == IResourceDelta.REMOVED) {
					final Runnable r = () -> getSite().getPage().closeEditor(MultiPageCSVEditor.this, false);
					getSite().getShell().getDisplay().asyncExec(r);
				}
				// file changed -- reload
				else if (delta.getKind() == IResourceDelta.CHANGED) {
					final int flags = delta.getFlags();
					if ((flags & IResourceDelta.CONTENT) != 0 || (flags & IResourceDelta.LOCAL_CHANGED) != 0) {
						WorkbenchHelper.asyncRun(MultiPageCSVEditor.this::updateTableFromTextEditor);
					}
				}
			}

		}
	}

	/**
	 * Refresh with delimiter.
	 *
	 * @param c
	 *            the c
	 */
	void refreshWithDelimiter(final Character c) {
		if (c != null) {
			model.setCustomDelimiter(c);
			editor.setDelimiter(c);
		}
		updateTableFromTextEditor();
		updateTextEditorFromTable();
	}

	/**
	 * Method createToolItem()
	 *
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView#createToolItem(int,
	 *      gama.ui.shared.views.toolbar.GamaToolbar2)
	 */
	@Override
	public void createToolItems(final GamaToolbar2 tb) {

		// add the filtering and coloring when searching specific elements.
		final Text searchText =
				new Text(tb.getToolbar(SWT.LEFT), SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		tb.control(searchText, 150, SWT.LEFT);
		searchText.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent ke) {
				tableFilter.setSearchText(searchText.getText());
				final String filterText = searchText.getText();
				for (int i = 0; i < tableViewer.getColumnProperties().length; i++) {
					final CellLabelProvider labelProvider = tableViewer.getLabelProvider(i);
					if (labelProvider != null) { ((CSVLabelProvider) labelProvider).setSearchText(filterText); }
				}
				tableViewer.refresh();
			}
		});

		GamaToolbarSimple tbs = tb.getToolbar(SWT.RIGHT);
		tbs.button("editor/local.menu", "More...", "More options", e -> {

			final GamaMenu menu = new GamaMenu() {

				@Override
				protected void fillMenu() {
					Menu sub = GamaMenu.sub(mainMenu, "Choose separator",
							"Determine which character should be used as delimiter of fields",
							(IGamaIcons.SET_DELIMITER));
					GamaMenu.action(sub, ", (comma)", e1 -> refreshWithDelimiter(Letters.COMMA));
					GamaMenu.action(sub, "; (semicolon)", e1 -> refreshWithDelimiter(Letters.SEMICOLUMN));
					GamaMenu.action(sub, "  (space)", e1 -> refreshWithDelimiter(Letters.SPACE));
					GamaMenu.action(sub, "  (tab)", e1 -> refreshWithDelimiter(Letters.TAB));
					GamaMenu.action(sub, ": (colon)", e1 -> refreshWithDelimiter(Letters.COLUMN));
					GamaMenu.action(sub, "| (pipe)", e1 -> refreshWithDelimiter(Letters.PIPE));

					GamaCommand.build(IGamaIcons.SET_HEADER, "First line is header", "First line is header", e -> {
						final ToolItem t1 = (ToolItem) e.widget;
						model.setFirstLineHeader(t1.getSelection());
						refreshWithDelimiter(null);
					}).toCheckItem(mainMenu).setSelection(model.isFirstLineHeader());
					GamaMenu.separate(mainMenu);
					GamaCommand.build(IGamaIcons.ADD_ROW, "Add row",
							"Insert a new row before the currently selected one or at the end of the file if none is selected",
							e -> {
								final CSVRow row =
										(CSVRow) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
								if (row != null) {
									model.addRowAfterElement(row);
								} else {
									model.addRow();
								}
								tableModified();
							}).toItem(mainMenu);
					GamaCommand.build(IGamaIcons.DELETE_ROW, "Delete row", "Delete currently selected rows", e -> {

						CSVRow row = (CSVRow) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

						while (row != null) {
							row = (CSVRow) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
							if (row != null) {
								model.removeRow(row);
								tableModified();
							}
						}
					}).toItem(mainMenu);
					if (model.isFirstLineHeader()) {
						GamaCommand.build(IGamaIcons.ADD_COLUMN, "Add column", "Add new column", arg0 -> {
							// call insert/add column page
							final InsertColumnPage acPage =
									new InsertColumnPage(getSite().getShell(), model.getArrayHeader());
							if (acPage.open() == Window.OK) {
								final String colToInsert = acPage.getColumnNewName();
								model.addColumn(colToInsert);
								tableViewer.setInput(model);
								final TableColumn column = new TableColumn(tableViewer.getTable(), SWT.LEFT);
								column.setText(colToInsert);
								column.setWidth(100);
								column.setResizable(true);
								column.setMoveable(true);
								addMenuItemToColumn(column, model.getColumnCount() - 1);
								defineCellEditing();
								tableModified();

							}
						}).toItem(mainMenu);

					}
					if (model.isFirstLineHeader()) {
						GamaCommand.build(IGamaIcons.DELETE_COLUMN, "Delete column", "Delete one or several column(s)",
								e -> {

									// call delete column page
									final DeleteColumnPage dcPage =
											new DeleteColumnPage(getSite().getShell(), model.getArrayHeader());
									if (dcPage.open() == Window.OK) {
										final String[] colToDelete = dcPage.getColumnSelected();
										for (final String column : colToDelete) {
											final int colIndex = findColumnForName(column);
											tableViewer.getTable().getColumn(colIndex).dispose();
											// tableHeaderMenu.getItem(colIndex).dispose();
											model.removeColumn(column);
										}
										tableModified();
									}

								}).toItem(mainMenu);
					}
					GamaMenu.separate(mainMenu);
					GamaCommand.build(IGamaIcons.SAVE_AS, "Save as...", "Save as...", e -> doSaveAs()).toItem(mainMenu);
				}

			};
			menu.open(tbs, e, tbs.getSize().y, 0);
		});

	}

}
