/*******************************************************************************************************
 *
 * TestView.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views;

import static gama.api.utils.prefs.GamaPreferences.Runtime.FAILED_TESTS;
import static gama.api.utils.prefs.GamaPreferences.Runtime.TESTS_SORTED;
import static gama.ui.shared.resources.IGamaIcons.TEST_FILTER;
import static gama.ui.shared.resources.IGamaIcons.TEST_SORT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.google.common.primitives.Longs;

import gama.api.GAMA;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.IGamaView;
import gama.api.ui.IGui;
import gama.api.ui.IItemList;
import gama.api.ui.IStatusMessage;
import gama.api.utils.files.FileUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.tests.AbstractSummary;
import gama.api.utils.tests.CompoundSummary;
import gama.api.utils.tests.TestExperimentSummary;
import gama.api.utils.tests.TestState;
import gama.ui.shared.controls.ParameterExpandItem;
import gama.ui.shared.controls.StatusIconProvider;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.parameters.AssertEditor;
import gama.ui.shared.parameters.EditorsGroup;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.ViewsHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.utils.SwtGui;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 * The Class TestView.
 */
public class TestView extends ExpandableItemsView<AbstractSummary<?>> implements IGamaView.Test {

	/** The scope. */
	IScope scope = GAMA.getRuntimeScope();

	/** The Constant BY_ORDER. */
	static final Comparator<AbstractSummary<?>> BY_ORDER = (o1, o2) -> Longs.compare(o1.getIndex(), o2.getIndex());

	/** The Constant BY_SEVERITY. */
	static final Comparator<AbstractSummary<?>> BY_SEVERITY = (o1, o2) -> {
		final TestState s1 = o1.getState();
		final TestState s2 = o2.getState();
		if (s1 == s2) return BY_ORDER.compare(o1, o2);
		return s1.compareTo(s2);
	};

	/** The experiments. */
	public final List<AbstractSummary<?>> experiments = new ArrayList<>();

	/** The running all tests. */
	private boolean runningAllTests;

	private volatile boolean sequenceRunning;

	private boolean stopped;

	private ToolItem stopItem;

	private ToolItem expandItem, collapseItem;

	private boolean summaryDisplayed;

	private String currentTest = "";

	private int currentModel, totalModels;

	/** The icon provider. */
	private final StatusIconProvider iconProvider = new StatusIconProvider();

	/** The id. */
	public static String ID = IGui.TEST_VIEW_ID;

	@Override
	public void init(final IViewSite site) throws PartInitException {
		super.init(site);
		experiments.clear();
		super.reset();
	}

	@Override
	protected boolean areItemsClosable() {
		return false;
	}

	/**
	 * Resort tests.
	 */
	protected void resortTests() {
		final Comparator<AbstractSummary<?>> comp = TESTS_SORTED.getValue() ? BY_SEVERITY : BY_ORDER;
		experiments.sort(comp);
	}

	@Override
	public void startNewTestSequence(final boolean all) {
		runningAllTests = all;
		sequenceRunning = true;
		experiments.clear();
		WorkbenchHelper.run(() -> {
			stopped = false;
			summaryDisplayed = false;
			currentTest = "";
			currentModel = totalModels = 0;
			updateSequenceToolItems();
			if (toolbar != null) {
				toolbar.status("Preparing tests...");
			}
		});
		super.reset();
	}

	@Override
	public void finishTestSequence() {
		sequenceRunning = false;
		WorkbenchHelper.run(() -> {
			summaryDisplayed = true;
			updateSequenceToolItems();
		});
		super.reset();
		updateIcon = false;
		reset();
	}

	@Override
	public void addTestResult(final CompoundSummary<?, ?> summary) {
		if (summary instanceof TestExperimentSummary) {
			if (!experiments.contains(summary)) { experiments.add(summary); }
		} else {
			for (final AbstractSummary<?> s : summary.getSummaries().values()) {
				if (!experiments.contains(s)) { experiments.add(s); }
			}
		}
	}

	@Override
	public boolean addItem(final AbstractSummary<?> experiment) {
		final boolean onlyFailed = GamaPreferences.Runtime.FAILED_TESTS.getValue();
		ParameterExpandItem item = getViewer() == null ? null : getViewer().getItem(experiment);
		if (item != null) { item.dispose(); }
		if (onlyFailed) {
			final TestState state = experiment.getState();
			if (!state.isFailure()) return false;
		}
		item = createItem(getParentComposite(), experiment, !runningAllTests,
				GamaColors.get(getItemDisplayColor(experiment)));
		return true;
	}

	@Override
	public void ownCreatePartControl(final Composite view) {

	}

	// Experimental: creates a deferred item
	@Override
	protected ParameterExpandItem createItem(final Composite parent, final AbstractSummary<?> data,
			final boolean expanded, final GamaUIColor color) {
		createViewer(parent);
		if (getViewer() == null) return null;
		final EditorsGroup control = createItemContentsFor(data);
		ParameterExpandItem item;
		if (expanded) {
			createEditors(control, data);
			item = createItem(parent, data, control, expanded, color);
		} else {
			item = createItem(parent, data, control, expanded, color);
			item.onExpand(() -> createEditors(control, data));
		}
		return item;
	}

	@Override
	protected EditorsGroup createItemContentsFor(final AbstractSummary<?> experiment) {
		final EditorsGroup compo = new EditorsGroup(getViewer());
		compo.setBackground(getViewer().getBackground());
		return compo;
	}

	/**
	 * Creates the editors.
	 *
	 * @param compo
	 *            the compo
	 * @param test
	 *            the test
	 */
	public void createEditors(final EditorsGroup compo, final AbstractSummary<?> test) {
		createErrorDetails(compo, test);
		Map<String, ? extends AbstractSummary<?>> assertions = test.getSummaries();
		for (final Map.Entry<String, ? extends AbstractSummary<?>> assertion : assertions.entrySet()) {
			final AbstractSummary<?> summary = assertion.getValue();
			final String name = assertion.getKey();
			createEditor(compo, test, summary, name);
			if (summary instanceof CompoundSummary) {
				assertions = summary.getSummaries();
				for (final Map.Entry<String, ? extends AbstractSummary<?>> aa : assertions.entrySet()) {
					createEditor(compo, test, aa.getValue(), aa.getKey());
				}
			}
		}
	}

	/**
	 * Creates the editor.
	 *
	 * @param compo
	 *            the compo
	 * @param globalTest
	 *            the global test
	 * @param subTest
	 *            the sub test
	 * @param name
	 *            the name
	 */
	public void createEditor(final EditorsGroup compo, final AbstractSummary<?> globalTest,
			final AbstractSummary<?> subTest, final String name) {
		if (GamaPreferences.Runtime.FAILED_TESTS.getValue()) {
			final TestState state = subTest.getState();
			if (!state.isFailure()) return;
		}
		final AssertEditor ed = new AssertEditor(scope, subTest);
		ed.createControls(compo);
		createErrorDetails(compo, subTest);
	}

	private void createErrorDetails(final EditorsGroup compo, final AbstractSummary<?> summary) {
		final String error = summary.getErrorDetails();
		if (error == null || error.isBlank()) return;
		final Text details = new Text(compo, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		details.setText(error);
		details.setToolTipText("Error details for " + summary.getTitle());
		final GridData data = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1);
		data.widthHint = 0;
		data.heightHint = details.getLineHeight() * Math.min(8, Math.max(2, details.getLineCount()));
		details.setLayoutData(data);
	}

	@SuppressWarnings ("synthetic-access")
	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		super.createToolItems(tb);
		TESTS_SORTED.removeChangeListeners();
		FAILED_TESTS.removeChangeListeners();
		GamaToolbarSimple tbs = toolbar.getToolbar(SWT.RIGHT);
		updateSequenceToolItems();
		tbs.button("editor/local.menu", "More...", "More options", e -> {

			final GamaMenu menu = new GamaMenu() {

				@Override
				protected void fillMenu() {
					GamaCommand.build(TEST_SORT, "Sort by severity",
							"When checked, sort by severity, including interrupted and timed-out tests. Otherwise sort by order of execution.",
							e -> {
								TESTS_SORTED.set(!TESTS_SORTED.getValue());
								TestView.this.reset();
								reset();
							}).toCheckItem(mainMenu).setSelection(TESTS_SORTED.getValue());
					GamaCommand.build(TEST_FILTER, "Filter tests",
							"When checked, show only failed, errored, interrupted and timed-out tests and assertions", e -> {
								FAILED_TESTS.set(!FAILED_TESTS.getValue());
								TestView.this.reset();
								reset();
							}).toCheckItem(mainMenu).setSelection(FAILED_TESTS.getValue());
					GamaMenu.separate(mainMenu);
					GamaCommand.build(IGamaIcons.SAVE_AS, "Save tests", "Save the current tests as a text file", e -> {
						TestView.this.saveTests();
					}).toItem(mainMenu);
				}

			};
			menu.open(tbs, e, tbs.getSize().y, 0);
		});

	}

	@Override
	public void createExpandItems(final GamaToolbar2 tb) {}

	private void updateSequenceToolItems() {
		if (toolbar == null || toolbar.isDisposed()) return;
		if (stopItem != null && !stopItem.isDisposed()) { stopItem.dispose(); }
		if (expandItem != null && !expandItem.isDisposed()) { expandItem.dispose(); }
		if (collapseItem != null && !collapseItem.isDisposed()) { collapseItem.dispose(); }
		stopItem = expandItem = collapseItem = null;
		if (sequenceRunning) {
			stopItem = toolbar.button(new GamaCommand(IGamaIcons.EXPERIMENT_STOP, "Stop tests",
					"Stop the current test sequence and keep its results", e -> {
					if (!sequenceRunning || stopped) return;
					stopped = true;
					stopItem.setEnabled(false);
					if (GAMA.getRegularGui() instanceof SwtGui gui) { gui.stopTests(); }
					toolbar.status("Stopping tests...");
					}), SWT.RIGHT);
			stopItem.setEnabled(!stopped);
		} else if (summaryDisplayed) {
			expandItem = toolbar.button(IGamaIcons.TREE_EXPAND, "Expand all items", "Expand all items",
					e -> expandAll(), SWT.RIGHT);
			collapseItem = toolbar.button(IGamaIcons.TREE_COLLAPSE, "Collapse all items", "Collapse all items",
					e -> collapseAll(), SWT.RIGHT);
		}
		toolbar.requestLayout();
	}

	/**
	 * Save tests.
	 */
	public void saveTests() {
		final DirectoryDialog dialog = new DirectoryDialog(WorkbenchHelper.getShell(), SWT.NULL);
		dialog.setFilterPath(GAMA.getModel() == null ? GAMA.getWorkspaceManager().getWorkspaceLocation()
				: GAMA.getModel().getFilePath());
		dialog.setMessage("Choose a folder for saving the tests");
		final String path = dialog.open();
		if (path == null) return;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String file = path + File.separator + "tests_" + new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(timestamp)
				+ ".txt";
		file = FileUtils.constructAbsoluteFilePath(scope, file, false);
		try (PrintWriter out = new PrintWriter(file)) {
			for (AbstractSummary summary : experiments) {
				out.println(summary.toString());
				out.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setFocus() {}

	@Override
	public void removeItem(final AbstractSummary<?> obj) {}

	@Override
	public void pauseItem(final AbstractSummary<?> obj) {}

	@Override
	public void resumeItem(final AbstractSummary<?> obj) {}

	@Override
	public String getItemDisplayName(final AbstractSummary<?> obj, final String previousName) {
		final StringBuilder sb = new StringBuilder(300);
		final String name = obj.getTitle();
		sb.append(obj.getState()).append(IItemList.SEPARATION_CODE).append(name).append(' ');
		if (obj.getError() != null) { sb.append("[").append(obj.getError()).append("]"); }
		return sb.toString();
	}

	@Override
	protected boolean shouldBeClosedWhenNoExperiments() {
		return false;
		// return !runningAllTests;
	}

	/**
	 * The TestView must never be auto-closed by {@code closeSimulationViews()} — its results are intentionally kept
	 * visible until the user decides to close it manually. This matches the ErrorView behavior and prevents losing the
	 * current unit-test results when a model experiment is launched.
	 */
	@Override
	public void close(final IScope scope) {
		// deliberate no-op — see Javadoc above
	}

	@Override
	public IColor getItemDisplayColor(final AbstractSummary<?> t) {
		return t.getColor(null);
	}

	@Override
	public void focusItem(final AbstractSummary<?> data) {}

	@Override
	public List<AbstractSummary<?>> getItems() { return experiments; }

	@Override
	public void updateItemValues(final boolean synchronously, final boolean retrieveValues) {}

	@Override
	public void reset() {
		WorkbenchHelper.run(() -> {
			if (!getParentComposite().isDisposed()) {
				resortTests();
				displayItems();
				getParentComposite().layout(true, false);
				if (toolbar != null) {
					toolbar.status((stopped ? "Stopped - " : "") + new CompoundSummary<>(experiments).getStringSummary());
				}
				ViewsHelper.bringToFront(this);
			}
		});

	}

	/**
	 * Method handleMenu()
	 *
	 * @see gama.api.ui.IItemList#handleMenu(java.lang.Object)
	 */
	@Override
	public Map<String, Runnable> handleMenu(final AbstractSummary<?> item, final int x, final int y) {
		final Map<String, Runnable> result = new HashMap<>();
		result.put("Copy summary to clipboard", () -> { WorkbenchHelper.copy(item.toString()); });
		result.put("Show in editor", () -> GAMA.getGui().getModelsManager().editModel(item.getURI()));
		return result;
	}

	@Override
	protected boolean needsOutput() {
		return false;
	}

	/** The update icon. */
	volatile boolean updateIcon = false;

	@Override
	public void displayProgress(final int number, final int total) {
		startProgressAnimation();
		WorkbenchHelper.asyncRun(() -> {
			if (!sequenceRunning || stopped || getParentComposite().isDisposed()) return;
			currentModel = number;
			totalModels = total;
			showRunningTest();
		});

	}

	private void startProgressAnimation() {
		if (!updateIcon) {
			updateIcon = true;
			Callable<Boolean> stop = () -> !updateIcon;
			WorkbenchHelper.asyncRun(() -> {
				if (toolbar != null) {
					toolbar.updateStatusImage(iconProvider.getIcon());
					GAMA.getGui().getStatus().informStatus("Running tests", IStatusMessage.SIMULATION_ICON);
				}
			}, 100, stop);
		}
	}

	@Override
	public void displayRunningTest(final String name) {
		WorkbenchHelper.asyncRun(() -> {
			if (!sequenceRunning || stopped || getParentComposite().isDisposed()) return;
			startProgressAnimation();
			currentTest = name;
			showRunningTest();
		});
	}

	private void showRunningTest() {
		if (toolbar != null) {
			final String progress = totalModels > 0 ? "Model " + currentModel + " of " + totalModels + " - " : "";
			toolbar.status(iconProvider.getIcon(), progress + currentTest, null);
		}
	}

}
