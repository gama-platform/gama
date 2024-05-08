/*******************************************************************************************************
 *
 * TestView.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views;

import static gama.core.common.preferences.GamaPreferences.Runtime.FAILED_TESTS;
import static gama.core.common.preferences.GamaPreferences.Runtime.TESTS_SORTED;
import static gama.ui.shared.resources.IGamaIcons.TEST_FILTER;
import static gama.ui.shared.resources.IGamaIcons.TEST_SORT;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.google.common.primitives.Ints;

import gama.core.common.interfaces.IGamaView;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.ItemList;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.util.FileUtils;
import gama.core.runtime.GAMA;
import gama.core.util.GamaColor;
import gama.gaml.statements.test.AbstractSummary;
import gama.gaml.statements.test.CompoundSummary;
import gama.gaml.statements.test.TestExperimentSummary;
import gama.gaml.statements.test.TestState;
import gama.ui.shared.controls.ParameterExpandItem;
import gama.ui.shared.parameters.AssertEditor;
import gama.ui.shared.parameters.EditorsGroup;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.ViewsHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaToolbar2;

/**
 * The Class TestView.
 */
public class TestView extends ExpandableItemsView<AbstractSummary<?>> implements IGamaView.Test {

	/** The Constant BY_ORDER. */
	static final Comparator<AbstractSummary<?>> BY_ORDER = (o1, o2) -> Ints.compare(o1.getIndex(), o2.getIndex());

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
		experiments.clear();
		WorkbenchHelper.run(() -> {
			if (toolbar != null) {
				toolbar.status(null, "Run experiment to see the tests results",
						e -> { GAMA.startFrontmostExperiment(false); }, IGamaColors.BLUE, SWT.LEFT);
			}
		});
		super.reset();
	}

	@Override
	public void finishTestSequence() {
		super.reset();
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
			if (state != TestState.FAILED && state != TestState.ABORTED) return false;
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
			if (state != TestState.FAILED && state != TestState.ABORTED) return;
		}
		final AssertEditor ed = new AssertEditor(GAMA.getRuntimeScope(), subTest);
		ed.createControls(compo);
	}

	@SuppressWarnings ("synthetic-access")
	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		super.createToolItems(tb);
		TESTS_SORTED.removeChangeListeners();
		FAILED_TESTS.removeChangeListeners();
		final ToolItem t = tb.check(TEST_SORT, "Sort by severity",
				"When checked, sort the tests by their decreasing state severity (i.e. errored > failed > warning > passed > not run). Otherwise they are sorted by their order of execution.",
				e -> {
					TESTS_SORTED.set(!TESTS_SORTED.getValue());
					TestView.super.reset();
					reset();
				}, SWT.RIGHT);
		t.setSelection(TESTS_SORTED.getValue());
		TESTS_SORTED.onChange(v -> t.setSelection(v));

		final ToolItem t2 = tb.check(TEST_FILTER, "Filter tests",
				"When checked, show only errored and failed tests and assertions", e -> {
					FAILED_TESTS.set(!FAILED_TESTS.getValue());
					TestView.super.reset();
					reset();
				}, SWT.RIGHT);
		t2.setSelection(FAILED_TESTS.getValue());
		FAILED_TESTS.onChange(v -> t2.setSelection(v));
		final ToolItem save =
				tb.button(IGamaIcons.SAVE_AS, "Save tests", "Save the current tests as a text file", e -> {
					this.saveTests();
				}, SWT.RIGHT);
	}

	/**
	 * Save tests.
	 */
	public void saveTests() {
		final DirectoryDialog dialog = new DirectoryDialog(WorkbenchHelper.getShell(), SWT.NULL);
		dialog.setFilterPath(GAMA.getModel() == null
				? ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() : GAMA.getModel().getFilePath());
		dialog.setMessage("Choose a folder for saving the tests");
		final String path = dialog.open();
		if (path == null) return;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String file = path + "/" + "tests_" + timestamp.toString() + ".txt";
		file = FileUtils.constructAbsoluteFilePath(GAMA.getRuntimeScope(), file, false);
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
		sb.append(obj.getState()).append(ItemList.SEPARATION_CODE).append(name).append(' ');
		return sb.toString();
	}

	@Override
	protected boolean shouldBeClosedWhenNoExperiments() {
		return !runningAllTests;
	}

	@Override
	public GamaColor getItemDisplayColor(final AbstractSummary<?> t) {
		return t.getColor(null);
	}

	@Override
	public void focusItem(final AbstractSummary<?> data) {}

	@Override
	public List<AbstractSummary<?>> getItems() { return experiments; }

	@Override
	public void updateItemValues(final boolean synchronously) {}

	@Override
	public void reset() {
		WorkbenchHelper.run(() -> {
			if (!getParentComposite().isDisposed()) {
				resortTests();
				displayItems();
				getParentComposite().layout(true, false);
				if (toolbar != null) {
					toolbar.status(null, new CompoundSummary<>(experiments).getStringSummary(), null, IGamaColors.BLUE,
							SWT.LEFT);
				}
				ViewsHelper.bringToFront(this);
			}
		});

	}

	/**
	 * Method handleMenu()
	 *
	 * @see gama.core.common.interfaces.ItemList#handleMenu(java.lang.Object)
	 */
	@Override
	public Map<String, Runnable> handleMenu(final AbstractSummary<?> item, final int x, final int y) {
		final Map<String, Runnable> result = new HashMap<>();
		result.put("Copy summary to clipboard", () -> { WorkbenchHelper.copy(item.toString()); });
		result.put("Show in editor", () -> GAMA.getGui().editModel(item.getURI()));
		return result;
	}

	@Override
	protected boolean needsOutput() {
		return false;
	}

	@Override
	public void displayProgress(final int number, final int total) {
		WorkbenchHelper.asyncRun(() -> {
			if (toolbar != null) {
				toolbar.status(null, "Executing test models: " + number + " on " + total, null, IGamaColors.NEUTRAL,
						SWT.LEFT);
			}
		});

	}

}
