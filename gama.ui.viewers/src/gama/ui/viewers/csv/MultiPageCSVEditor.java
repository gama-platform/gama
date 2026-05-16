/*******************************************************************************************************
 *
 * MultiPageCSVEditor.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.csv;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import gama.api.GAMA;
import gama.api.utils.StringUtils;
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

/**
 *
 * @author fhenri
 *
 */
public class MultiPageCSVEditor extends EditorPart implements IResourceChangeListener, IToolbarDecoratedView.Sizable {

	/** Row-count threshold above which the table switches to fast virtual browsing. */
	private static final int LARGE_FILE_ROW_THRESHOLD = 10_000;

	/** File-size threshold above which the table switches to fast virtual browsing without querying metadata. */
	private static final long LARGE_FILE_SIZE_THRESHOLD = 2_000_000L;

	/** Maximum number of columns displayed at once in fast browsing mode. */
	private static final int FAST_VISIBLE_COLUMN_COUNT = 50;

	/** The is page modified. */
	private boolean isPageModified;

	/** Whether the current table uses the virtual browsing mode. */
	private boolean virtualTable;

	/** Monotonic identifier used to ignore stale background table loads. */
	private int tableLoadGeneration;

	/** First model column currently displayed in fast browsing mode. */
	private int visibleColumnStart;

	/** Toolbar button moving to the previous column window. */
	private ToolItem previousColumnsButton;

	/** Toolbar button moving to the next column window. */
	private ToolItem nextColumnsButton;

	/** The table viewer used in page 1. */
	protected TableViewer tableViewer;

	/** The table sorter. */
	final CSVTableSorter tableSorter;

	/** The table filter. */
	final CSVTableFilter tableFilter;

	/** The model. */
	CSVModel model;

	/** The csv file listener. */
	private final ICsvFileModelListener csvFileListener = (row, rowIndex) -> updateEditedRow(row);

	/**
	 * Creates a multi-page editor example.
	 */
	public MultiPageCSVEditor() {
		GAMA.getWorkspaceManager().getWorkspace().addResourceChangeListener(this);
		tableFilter = new CSVTableFilter();
		tableSorter = new CSVTableSorter();
		// model = createCSVFile();
	}

	@Override
	public Control getSizableFontControl() {
		if (tableViewer == null) return null;
		return tableViewer.getTable();
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		updateTitle();
	}

	@Override
	public void createPartControl(final Composite parent) {
		try {
			model = new CSVModel(getFileFor(getEditorInput()));
			virtualTable = useVirtualTable();
			createTablePage(parent);
			populateTablePage();
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
		if (input instanceof IFileEditorInput) return ((IFileEditorInput) input).getFile();
		if (input instanceof IStorageEditorInput) {
			try {
				final IStorage storage = ((IStorageEditorInput) input).getStorage();
				if (storage instanceof IFile) return (IFile) storage;
			} catch (final CoreException ignore) {
				// intentionally blank
			}
		}
		return null;
	}

	/**
	 *
	 */
	private void createTablePage(final Composite parent) {
		final Composite intermediate = new Composite(parent, SWT.NONE);
		final Composite composite = GamaToolbarFactory.createToolbars(this, intermediate);
		final int tableStyle = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER
				| (virtualTable ? SWT.VIRTUAL : SWT.NONE);
		tableViewer = new TableViewer(composite, tableStyle);
		tableViewer.setUseHashlookup(true);
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// set the sorter for the table

		if (!virtualTable) {
			tableViewer.setComparator(tableSorter);
			// set a table filter
			tableViewer.addFilter(tableFilter);
		}
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
		tableViewer.setContentProvider(virtualTable ? new CSVLazyContentProvider() : new CSVContentProvider());
		// make the selection available
		getSite().setSelectionProvider(tableViewer);
		tableViewer.getTable().getDisplay().asyncExec(this::scheduleTableLoadFromFile);
	}

	/**
	 * Loads the current CSV file in a background job and applies the resulting model on the UI thread.
	 */
	private void scheduleTableLoadFromFile() {
		final IFile file = getFileFor(getEditorInput());
		if (file == null) return;
		if (model != null) { model.removeModelListener(csvFileListener); }
		final int generation = ++tableLoadGeneration;
		tableViewer.getTable().removeAll();
		tableViewer.setInput(null);
		tableViewer.setItemCount(0);
		final Job job = new Job("Load CSV table") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				final CSVModel loadedModel = new CSVModel(file);
				loadedModel.reloadFromFile();
				WorkbenchHelper.asyncRun(() -> {
					if (tableViewer == null || tableViewer.getTable().isDisposed()) return;
					if (generation != tableLoadGeneration) return;
					model = loadedModel;
					applyModelToTable();
				});
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/**
	 *
	 */
	public void tableModified() {
		tableViewer.setItemCount(model.getDataRowCount());
		tableViewer.refresh();
		markDirty();
	}

	/**
	 * Updates the edited row without reloading the whole table whenever possible.
	 *
	 * @param row
	 *            the row that has just been modified
	 */
	private void updateEditedRow(final CSVRow row) {
		markDirty();
		final int rowIndex = model.findDataRow(row);
		if (rowIndex >= 0) {
			tableViewer.update(row, null);
			if (virtualTable) { tableViewer.replace(row, rowIndex); }
			return;
		}
		tableModified();
	}

	/**
	 * Marks the editor dirty and validates the underlying file state.
	 */
	private void markDirty() {
		final boolean wasPageModified = isPageModified;
		isPageModified = true;
		if (!wasPageModified) { firePropertyChange(IEditorPart.PROP_DIRTY); }
	}

	/**
	 *
	 */
	void updateTableFromModel() {
		tableLoadGeneration++;
		model.removeModelListener(csvFileListener);
		applyModelToTable();
	}

	/**
	 * Applies the currently loaded model to the table viewer.
	 */
	private void applyModelToTable() {
		rebuildVisibleColumns();
		tableViewer.setInput(model);
		if (virtualTable) { tableViewer.setItemCount(model.getDataRowCount()); }
		tableViewer.refresh();
		model.addModelListener(csvFileListener);
		defineCellEditing();
	}

	/**
	 * Rebuilds the visible SWT columns according to the current windowed column range.
	 */
	private void rebuildVisibleColumns() {
		clampVisibleColumnStart();
		final TableColumn[] columns = tableViewer.getTable().getColumns();
		for (final TableColumn c : columns) { c.dispose(); }
		for (int i = getFirstVisibleColumn(); i < getLastVisibleColumn(); i++) {
			final TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
			column.getColumn().setText(model.getHeader().get(i));
			column.getColumn().setWidth(100);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(true);
			column.setLabelProvider(new CSVLabelProvider(i));
			addMenuItemToColumn(column.getColumn(), i);
		}
		updateColumnWindowButtons();
	}

	/**
	 * Returns whether fast mode currently displays only a subset of columns.
	 *
	 * @return {@code true} if the editor uses a windowed column range in fast mode
	 */
	private boolean usesColumnWindow() {
		return virtualTable && model != null && model.getColumnCount() > FAST_VISIBLE_COLUMN_COUNT;
	}

	/**
	 * Gets the first visible model column.
	 *
	 * @return the inclusive start index of the displayed column window
	 */
	private int getFirstVisibleColumn() {
		return usesColumnWindow() ? visibleColumnStart : 0;
	}

	/**
	 * Gets the exclusive end index of the displayed column window.
	 *
	 * @return the exclusive end index of the displayed column window
	 */
	private int getLastVisibleColumn() {
		if (model == null) return 0;
		return usesColumnWindow() ? Math.min(model.getColumnCount(), visibleColumnStart + FAST_VISIBLE_COLUMN_COUNT)
				: model.getColumnCount();
	}

	/**
	 * Ensures that the first visible column stays within the valid range.
	 */
	private void clampVisibleColumnStart() {
		if (!usesColumnWindow()) {
			visibleColumnStart = 0;
			return;
		}
		final int maxStart = Math.max(0, model.getColumnCount() - FAST_VISIBLE_COLUMN_COUNT);
		if (visibleColumnStart < 0) {
			visibleColumnStart = 0;
		} else if (visibleColumnStart > maxStart) {
			visibleColumnStart = maxStart;
		}
	}

	/**
	 * Moves the visible column window by the given delta.
	 *
	 * @param delta
	 *            the number of model columns to shift by
	 */
	private void shiftVisibleColumns(final int delta) {
		if (!usesColumnWindow()) return;
		visibleColumnStart += delta;
		clampVisibleColumnStart();
		rebuildVisibleColumns();
		tableViewer.refresh();
	}

	/**
	 * Updates the enabled state and tooltip of the column window navigation buttons.
	 */
	private void updateColumnWindowButtons() {
		if (previousColumnsButton == null || previousColumnsButton.isDisposed() || nextColumnsButton == null
				|| nextColumnsButton.isDisposed())
			return;
		final boolean enabled = usesColumnWindow();
		previousColumnsButton.setEnabled(enabled && getFirstVisibleColumn() > 0);
		nextColumnsButton.setEnabled(enabled && getLastVisibleColumn() < model.getColumnCount());
		if (!enabled) {
			previousColumnsButton.setToolTipText("All columns are currently visible");
			nextColumnsButton.setToolTipText("All columns are currently visible");
			return;
		}
		final String range = (getFirstVisibleColumn() + 1) + "-" + getLastVisibleColumn() + " / "
				+ model.getColumnCount();
			previousColumnsButton.setToolTipText("Show previous columns (currently " + range + ")");
			nextColumnsButton.setToolTipText("Show next columns (currently " + range + ")");
	}

	/**
	 * Determines whether the current CSV file should use the virtual browsing mode.
	 *
	 * @return {@code true} for large CSV files, {@code false} otherwise
	 */
	private boolean useVirtualTable() {
		final IFile file = getFileFor(getEditorInput());
		if (file != null && file.getLocation() != null && file.getLocation().toFile().length() >= LARGE_FILE_SIZE_THRESHOLD)
			return true;
		return model != null && model.getInfo().rows >= LARGE_FILE_ROW_THRESHOLD;
	}

	/**
	 *
	 */
	void defineCellEditing() {
		if (virtualTable) {
			tableViewer.setColumnProperties(new String[0]);
			tableViewer.setCellEditors(null);
			tableViewer.setCellModifier(null);
			return;
		}
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
			if (columnName.equalsIgnoreCase(column.getText())) return i;
		}
		return index;
	}

	/**
	 * @param column
	 * @param index
	 */
	void addMenuItemToColumn(final TableColumn column, final int index) {
		if (virtualTable) return;
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
		GAMA.getWorkspaceManager().getWorkspace().removeResourceChangeListener(this);
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
		saveModelToFile(getFileFor(getEditorInput()), monitor);
		isPageModified = false;
		firePropertyChange(IEditorPart.PROP_DIRTY);
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
		final SaveAsDialog dialog = new SaveAsDialog(getSite().getShell());
		dialog.setOriginalFile(getFileFor(getEditorInput()));
		if (dialog.open() != Window.OK || dialog.getResult() == null) return;
		final IFile destination = ResourcesPlugin.getWorkspace().getRoot().getFile(dialog.getResult());
		saveModelToFile(destination, new NullProgressMonitor());
		final char delimiter = model.getCustomDelimiter();
		final boolean firstLineHeader = model.isFirstLineHeader();
		final String text = model.getTextRepresentation();
		setInput(new FileEditorInput(destination));
		model = new CSVModel(destination);
		model.setCustomDelimiter(delimiter);
		model.setFirstLineHeader(firstLineHeader);
		model.setInput(text);
		applyModelToTable();
		isPageModified = false;
		firePropertyChange(IEditorPart.PROP_DIRTY);
		updateTitle();
	}

	/**
	 * Saves the current table model into the given workspace file.
	 *
	 * @param file
	 *            the destination file
	 * @param monitor
	 *            the progress monitor to use
	 */
	private void saveModelToFile(final IFile file, final IProgressMonitor monitor) {
		if (file == null) return;
		final IProgressMonitor progress = monitor == null ? new NullProgressMonitor() : monitor;
		final Charset charset = getCharset(file);
		final byte[] bytes = model.getTextRepresentation().getBytes(charset);
		try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
			if (file.exists()) {
				file.setContents(stream, true, true, progress);
			} else {
				file.create(stream, true, progress);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Resolves the charset used to persist the given file.
	 *
	 * @param file
	 *            the target file
	 * @return the charset to use for saving the CSV contents
	 */
	private Charset getCharset(final IFile file) {
		try {
			return Charset.forName(file.getCharset());
		} catch (final Exception e) {
			return StandardCharsets.UTF_8;
		}
	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() { return isPageModified; }

	/**
	 * When the focus shifts to the editor, this method is called; it must then redirect focus to the appropriate editor
	 * based on which page is currently selected.
	 *
	 * @see org.eclipse.ui.part.MultiPageEditorPart#setFocus()
	 */
	@Override
	public void setFocus() {
		tableViewer.getTable().setFocus();
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
				final IEditorInput currentInput = getEditorInput();
				final IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
				for (final IWorkbenchPage page : pages) {
					if (((FileEditorInput) currentInput).getFile().getProject()
							.equals(event.getResource())) {
						final IEditorPart editorPart = page.findEditor(currentInput);
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
						WorkbenchHelper.asyncRun(() -> {
							if (!isDirty()) {
								scheduleTableLoadFromFile();
							}
						});
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
		final String text = model.getTextRepresentation();
		if (c != null) {
			model.setCustomDelimiter(c);
		}
		model.setInput(text);
		updateTableFromModel();
	}

	/**
	 * Method createToolItem()
	 *
	 * @see gama.ui.shared.views.toolbar.IToolbarDecoratedView#createToolItem(int,
	 *      gama.ui.shared.views.toolbar.GamaToolbar2)
	 */
	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		previousColumnsButton = tb.button(IGamaIcons.BROWSER_BACK, "Prev columns",
				"Show previous columns in fast browse mode", e -> shiftVisibleColumns(-FAST_VISIBLE_COLUMN_COUNT),
				SWT.RIGHT);
		nextColumnsButton = tb.button(IGamaIcons.BROWSER_FORWARD, "Next columns",
				"Show next columns in fast browse mode", e -> shiftVisibleColumns(FAST_VISIBLE_COLUMN_COUNT), SWT.RIGHT);
		updateColumnWindowButtons();

		// add the filtering and coloring when searching specific elements.
		final Text searchText =
				new Text(tb.getToolbar(SWT.LEFT), SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		tb.control(searchText, 150, SWT.LEFT);
		searchText.setEnabled(!virtualTable);
		if (virtualTable) { searchText.setMessage("Search disabled in fast browse mode"); }
		searchText.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent ke) {
				if (virtualTable) return;
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
							IGamaIcons.SET_DELIMITER);
					GamaMenu.action(sub, ", (comma)", e1 -> refreshWithDelimiter(StringUtils.Letters.COMMA));
					GamaMenu.action(sub, "; (semicolon)", e1 -> refreshWithDelimiter(StringUtils.Letters.SEMICOLUMN));
					GamaMenu.action(sub, "  (space)", e1 -> refreshWithDelimiter(StringUtils.Letters.SPACE));
					GamaMenu.action(sub, "  (tab)", e1 -> refreshWithDelimiter(StringUtils.Letters.TAB));
					GamaMenu.action(sub, ": (colon)", e1 -> refreshWithDelimiter(StringUtils.Letters.COLUMN));
					GamaMenu.action(sub, "| (pipe)", e1 -> refreshWithDelimiter(StringUtils.Letters.PIPE));

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
								updateTableFromModel();
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
											model.removeColumn(column);
										}
																		updateTableFromModel();
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
