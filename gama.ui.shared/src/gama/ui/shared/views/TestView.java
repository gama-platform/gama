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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import gama.api.GAMA;
import gama.api.ui.IGamaView;
import gama.api.ui.IOutput;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.tests.AbstractSummary;
import gama.api.utils.tests.CompoundSummary;
import gama.api.utils.tests.IndividualTestSummary;
import gama.api.utils.tests.TestExperimentSummary;
import gama.api.utils.tests.TestSelection;
import gama.api.utils.tests.TestState;
import gama.ui.shared.commands.TestsRunner;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.SwtGui;
import gama.ui.shared.utils.WorkbenchHelper;

/** A virtual native tree holds rows, not per-assertion editor composites. */
public class TestView extends ViewPart implements IGamaView, IGamaView.Test {

	private record ExperimentKey(String title, URI uri) {
		ExperimentKey(final TestExperimentSummary summary) { this(summary.getTitle(), summary.getURI()); }
	}

	private final List<AbstractSummary<?>> experiments = new ArrayList<>();
	private final Map<AbstractSummary<?>, List<AbstractSummary<?>>> children = new IdentityHashMap<>();
	private final Map<AbstractSummary<?>, TreeItem> items = new IdentityHashMap<>();
	private final Set<ExperimentKey> finalizedExperiments = new HashSet<>();
	private final Map<TestExperimentSummary, IndividualTestSummary> pendingSummaries = new IdentityHashMap<>();
	private List<AbstractSummary<?>> roots = List.of();
	private final Map<TestState, Image> icons = new EnumMap<>(TestState.class);
	private org.eclipse.swt.widgets.Display display;
	private Composite root;
	private Tree tree;
	private Text details;
	private Label counters, status;
	private ProgressBar progress;
	private ToolItem stop, restart, expand, collapse, filter, sort, export;
	private ToolBar tools, treeTools;
	private Composite toolbar;
	private SashForm sash;
	private boolean problemsOnly, severity, running, report;
	private volatile String currentName = "";
	private volatile int currentModel, totalModels;
	private volatile int completedTests, totalTests = -1;
	private volatile boolean progressPending;
	private volatile long generation;
	private record TestRerun(TestExperimentSummary experiment, IndividualTestSummary test,
			int completed, int total) {}
	private volatile TestRerun rerun;

	@Override
	public void createPartControl(final Composite parent) {
		root = new Composite(parent, SWT.NONE);
		display = parent.getDisplay();
		root.setLayout(new GridLayout(1, false));
		toolbar = new Composite(root, SWT.NONE);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final var toolbarLayout = new GridLayout(3, false);
		toolbarLayout.marginWidth = toolbarLayout.marginHeight = 0;
		toolbar.setLayout(toolbarLayout);
		tools = new ToolBar(toolbar, SWT.FLAT);
		tools.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		final Composite summaryArea = new Composite(toolbar, SWT.NONE);
		final var summaryData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		summaryData.widthHint = 0;
		summaryArea.setLayoutData(summaryData);
		final var summaryLayout = new GridLayout(1, false);
		summaryLayout.marginWidth = summaryLayout.marginHeight = 0;
		summaryLayout.verticalSpacing = 0;
		summaryArea.setLayout(summaryLayout);
		counters = new Label(summaryArea, SWT.WRAP);
		counters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		status = new Label(summaryArea, SWT.WRAP);
		status.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		treeTools = new ToolBar(toolbar, SWT.FLAT);
		treeTools.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		progress = new ProgressBar(root, SWT.SMOOTH);
		progress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		sash = new SashForm(root, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree = new Tree(sash, SWT.VIRTUAL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		details = new Text(sash, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		sash.setWeights(75, 25);
		sash.setMaximizedControl(tree);
		tree.addListener(SWT.SetData, e -> populate((TreeItem) e.item));
		tree.addListener(SWT.Selection, e -> showDetails());
		tree.addListener(SWT.DefaultSelection, e -> openSource());
		final MenuManager menu = new MenuManager();
		menu.setRemoveAllWhenShown(true);
		menu.addMenuListener(m -> {
			if (selected() instanceof IndividualTestSummary test && test.getURI() != null
					&& test.getURI().hasFragment()) {
				final var experiment = selectedExperiment();
				if (experiment != null && experiment.getURI() != null && experiment.getExperimentName() != null) {
					final Action rerunAction = action("Run this test only",
							() -> TestsRunner.rerun(this, experiment, test));
					rerunAction.setImageDescriptor(GamaIcon.named("toolbar/server.restart").descriptor());
					rerunAction.setEnabled(!running && !TestsRunner.isRunning());
					m.add(rerunAction);
				}
			}
			m.add(action("Show in editor", this::openSource));
			m.add(action("Copy selected summary", () -> {
				final var summary = selected();
				if (summary != null) {
					final var text = new StringBuilder();
					appendReport(text, summary, "");
					WorkbenchHelper.copy(text.toString());
				}
			}));
			m.add(action("Copy all results", () -> WorkbenchHelper.copy(textReport())));
			m.add(action("Export results...", this::export));
		});
		tree.setMenu(menu.createContextMenu(tree));
		tree.addDisposeListener(e -> menu.dispose());
		status.setText("No test results");
		counters.setText("0 tests");
		updateControls();
	}

	private static Action action(final String label, final Runnable runnable) {
		return new Action(label) { @Override public void run() { runnable.run(); } };
	}

	private ToolItem tool(final ToolBar bar, final int style, final String label, final String icon,
			final Runnable runnable) {
		final var item = new ToolItem(bar, style);
		item.setImage(GamaIcon.named(icon).image());
		item.setToolTipText(label);
		item.addListener(SWT.Selection, e -> runnable.run());
		return item;
	}

	private List<AbstractSummary<?>> rows(final AbstractSummary<?> parent) {
		if (parent == null) return roots;
		return children.computeIfAbsent(parent, summary -> filtered(new ArrayList<>(summary.getSummaries().values())));
	}

	private List<AbstractSummary<?>> filtered(final List<AbstractSummary<?>> result) {
		if (problemsOnly) { result.removeIf(s -> !hasProblem(s)); }
		if (severity) { result.sort(Comparator.comparing(AbstractSummary::getState)); }
		return result;
	}

	private boolean hasProblem(final AbstractSummary<?> summary) {
		final TestState state = summary.getState();
		if (state.isFailure()) return true;
		return summary.getSummaries().values().stream().anyMatch(this::hasProblem);
	}

	private void populate(final TreeItem item) {
		final TreeItem parent = item.getParentItem();
		final var siblings = rows(parent == null ? null : (AbstractSummary<?>) parent.getData());
		final int index = parent == null ? tree.indexOf(item) : parent.indexOf(item);
		if (index < 0 || index >= siblings.size()) return;
		final var summary = siblings.get(index);
		item.setData(summary);
		items.put(summary, item);
		updateItem(item, summary);
	}

	private void updateItem(final TreeItem item, final AbstractSummary<?> summary) {
		final String error = summary.getError();
		item.setText(summary.getTitle() + "  [" + summary.getState() + "]"
				+ (error == null || error.isBlank() ? "" : " - " + error.replace('\n', ' ').replace('\r', ' ')));
		item.setImage(statusIcon(summary.getState()));
		item.setItemCount(rows(summary).size());
	}

	private Image statusIcon(final TestState state) {
		return icons.computeIfAbsent(state, s -> {
			final var data = new ImageData(16, 16, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
			final int pixel = data.palette.getPixel(GamaColors.get(s.getColor(GAMA.getRuntimeScope())).color().getRGB());
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < 16; x++) {
					final double coverage = Math.max(0, Math.min(1, 4.5 - Math.hypot(x + 0.5 - 8, y + 0.5 - 8)));
					data.setPixel(x, y, pixel);
					data.setAlpha(x, y, (int) Math.round(255 * coverage));
				}
			}
			return new Image(display, data);
		});
	}

	private AbstractSummary<?> selected() {
		return tree.getSelectionCount() == 0 ? null : (AbstractSummary<?>) tree.getSelection()[0].getData();
	}

	private TestExperimentSummary selectedExperiment() {
		if (tree.getSelectionCount() == 0) return null;
		TreeItem item = tree.getSelection()[0];
		while (item.getParentItem() != null) { item = item.getParentItem(); }
		return item.getData() instanceof TestExperimentSummary experiment ? experiment : null;
	}

	/** Called on the SWT thread before the runner opens a new test sequence. */
	public void prepareTestRerun(final TestExperimentSummary experiment, final IndividualTestSummary test) {
		rerun = new TestRerun(experiment, test, completedTests, totalTests);
	}

	private void showDetails() {
		final var summary = selected();
		if (summary == null) {
			details.setText("");
			sash.setMaximizedControl(tree);
			return;
		}
		final var text = new StringBuilder(summary.getTitle()).append("\nStatus: ").append(summary.getState());
		if (summary.getURI() != null) {
			text.append("\nSource: ").append(org.eclipse.emf.common.util.URI.decode(summary.getURI().lastSegment()));
		}
		appendErrors(text, summary);
		details.setText(text.toString());
		sash.setMaximizedControl(null);
	}

	private void appendErrors(final StringBuilder text, final AbstractSummary<?> summary) {
		if (summary.getErrorDetails() != null) {
			text.append("\n\n").append(summary.getTitle()).append(":\n").append(summary.getErrorDetails());
		}
		for (final var child : summary.getSummaries().values()) { appendErrors(text, child); }
	}

	private void openSource() {
		final var summary = selected();
		if (summary != null && summary.getURI() != null) {
			GAMA.getGui().getModelsManager().editModel(summary.getURI());
		}
	}

	private void refresh() {
		problemsOnly = GamaPreferences.Runtime.FAILED_TESTS.getValue();
		severity = GamaPreferences.Runtime.TESTS_SORTED.getValue();
		children.clear();
		items.clear();
		roots = filtered(new ArrayList<>(experiments));
		tree.setRedraw(false);
		try {
			tree.removeAll();
			tree.setItemCount(rows(null).size());
			details.setText("");
			sash.setMaximizedControl(tree);
		} finally { tree.setRedraw(true); }
		updateCounters();
		updateControls();
		root.layout(true, true);
	}

	private void updateCounters() {
		final StringBuilder text = new StringBuilder();
		int total = 0;
		for (final TestState state : TestState.values()) {
			int count = 0;
			for (final var experiment : experiments) { count += experiment.countTestsWith(state); }
			total += count;
			if (count > 0) { text.append("   ").append(count).append(" ").append(state); }
		}
		counters.setText((running ? testCountsText() : totalTests >= 0
				? completedTests + " / " + totalTests + " tests completed" : total + " tests") + text);
	}

	private void expand(final TreeItem[] items) {
		for (final var item : items) {
			item.getText();
			item.setExpanded(true);
			expand(item.getItems());
		}
	}

	private void updateControls() {
		if (!running && report && TestsRunner.canRestart() && (restart == null || restart.isDisposed())) {
			restart = tool(tools, SWT.PUSH, "Run the same tests again", "toolbar/server.restart", TestsRunner::restart);
		} else if (running && restart != null && !restart.isDisposed()) {
			restart.dispose();
		}
		// Keep the filter available even when it hides every result.
		final boolean resultsAvailable = !running && report && !experiments.isEmpty();
		if (resultsAvailable && (filter == null || filter.isDisposed())) {
			filter = tool(treeTools, SWT.CHECK, "Problems only", "toolbar/test.filter", () -> {
				GamaPreferences.Runtime.FAILED_TESTS.set(filter.getSelection());
				refresh();
			});
			filter.setSelection(problemsOnly);
			sort = tool(treeTools, SWT.CHECK, "Sort by severity", "toolbar/test.sort", () -> {
				GamaPreferences.Runtime.TESTS_SORTED.set(sort.getSelection());
				refresh();
			});
			sort.setSelection(severity);
			export = tool(treeTools, SWT.PUSH, "Export results...", "toolbar/test.save", this::export);
		} else if (!resultsAvailable && filter != null && !filter.isDisposed()) {
			filter.dispose();
			sort.dispose();
			export.dispose();
		}
		if (resultsAvailable) {
			filter.setSelection(problemsOnly);
			sort.setSelection(severity);
		}
		if (!running && stop != null && !stop.isDisposed()) { stop.dispose(); }
		if (expand != null && !expand.isDisposed()) { expand.dispose(); }
		if (collapse != null && !collapse.isDisposed()) { collapse.dispose(); }
		if (running && (stop == null || stop.isDisposed())) {
			stop = new ToolItem(tools, SWT.PUSH, 0);
			stop.setImage(GamaIcon.named("toolbar/server.stop").image());
			stop.setToolTipText("Stop tests");
			stop.addListener(SWT.Selection, e -> {
				if (GAMA.getRegularGui() instanceof SwtGui gui) { gui.stopTests(); }
				status.setText("Stopping tests...");
				stop.setEnabled(false);
				root.layout(true, true);
			});
		} else if (!running && report && roots.stream().anyMatch(s -> !rows(s).isEmpty())) {
			expand = new ToolItem(treeTools, SWT.PUSH, treeTools.indexOf(export));
			expand.setImage(GamaIcon.named(IGamaIcons.TREE_EXPAND).image());
			expand.setToolTipText("Expand all");
			expand.addListener(SWT.Selection, e -> expand(tree.getItems()));
			collapse = new ToolItem(treeTools, SWT.PUSH, treeTools.indexOf(export));
			collapse.setImage(GamaIcon.named(IGamaIcons.TREE_COLLAPSE).image());
			collapse.setToolTipText("Collapse all");
			collapse.addListener(SWT.Selection, e -> {
				for (final var item : tree.getItems()) { item.setExpanded(false); }
			});
		}
		treeTools.setVisible(resultsAvailable);
		((GridData) treeTools.getLayoutData()).exclude = !treeTools.getVisible();
		progress.setVisible(running);
		((GridData) progress.getLayoutData()).exclude = !running;
		status.setVisible(running || !status.getText().isBlank());
		((GridData) status.getLayoutData()).exclude = !status.getVisible();
		root.layout(true, true);
	}

	private void ui(final Runnable runnable) {
		if (display == null || display.isDisposed()) return;
		final Runnable guarded = () -> { if (!root.isDisposed()) { runnable.run(); } };
		if (display.getThread() == Thread.currentThread()) { guarded.run(); }
		else { display.syncExec(guarded); }
	}

	@Override
	public void startNewTestSequence(final boolean all) {
		ui(() -> {
			generation++;
			synchronized (this) { pendingSummaries.clear(); }
			finalizedExperiments.clear();
			running = true;
			report = false;
			currentName = "Preparing tests...";
			currentModel = totalModels = 0;
			completedTests = 0;
			totalTests = -1;
			if (rerun == null) {
				experiments.clear();
				refresh();
			} else {
				updateCounters();
				updateControls();
			}
			status.setText(currentName);
			progress.setSelection(0);
		});
	}

	@Override
	public void addTestResult(final CompoundSummary<?, ?> summary) {
		ui(() -> {
			if (rerun != null) {
				if (summary instanceof TestExperimentSummary experiment) { replaceTestResult(experiment); }
				return;
			}
			if (summary instanceof TestExperimentSummary experiment) {
				upsertExperiment(experiment);
			} else {
				for (final var result : summary.getSummaries().values()) {
					if (result instanceof TestExperimentSummary experiment) { upsertExperiment(experiment); }
					else { experiments.add(result); }
				}
			}
			refresh();
		});
	}

	private void upsertExperiment(final TestExperimentSummary experiment) {
		final var key = new ExperimentKey(experiment);
		finalizedExperiments.add(key);
		synchronized (this) {
			pendingSummaries.keySet().removeIf(summary -> key.equals(new ExperimentKey(summary)));
		}
		final int index = experimentIndex(key);
		if (index < 0) { experiments.add(experiment); }
		else { experiments.set(index, experiment); }
	}

	private int experimentIndex(final ExperimentKey key) {
		for (int i = 0; i < experiments.size(); i++) {
			if (experiments.get(i) instanceof TestExperimentSummary test
					&& key.equals(new ExperimentKey(test))) return i;
		}
		return -1;
	}

	/** Queues live summaries for a coalesced SWT-thread refresh. */
	public void displayTestSummary(final TestExperimentSummary experiment, final IndividualTestSummary summary) {
		if (experiment == null || summary == null) return;
		if (rerun != null) return;
		synchronized (this) { pendingSummaries.put(experiment, summary); }
		queueProgress();
	}

	@Override
	public void displayRunningTest(final String name) {
		currentName = name;
		queueProgress();
	}

	@Override
	public void displayProgress(final int number, final int total) {
		currentModel = number;
		totalModels = total;
		queueProgress();
	}

	public void displayTestCounts(final int completed, final int total) {
		completedTests = completed;
		totalTests = total;
		queueProgress();
	}

	private String testCountsText() {
		return totalTests < 0 ? "Discovering tests..." : completedTests + " / " + totalTests + " tests completed";
	}

	private synchronized void queueProgress() {
		if (progressPending || root == null || root.isDisposed()) return;
		progressPending = true;
		final long sequence = generation;
		display.asyncExec(() -> {
			if (root.isDisposed()) { progressPending = false; return; }
			display.timerExec(100, () -> {
				final Map<TestExperimentSummary, IndividualTestSummary> summaries;
				synchronized (TestView.this) {
					progressPending = false;
					summaries = new IdentityHashMap<>(pendingSummaries);
					pendingSummaries.clear();
				}
				if (root.isDisposed() || !running || sequence != generation) return;
				refreshLiveSummaries(summaries);
				counters.setText(testCountsText());
				status.setText((totalModels > 0 ? "Model " + currentModel + " of " + totalModels + " - " : "")
						+ currentName);
				progress.setMaximum(Math.max(1, totalTests >= 0 ? totalTests : totalModels));
				progress.setSelection(Math.max(0, totalTests >= 0 ? completedTests : currentModel - 1));
				root.layout(true, true);
			});
		});
	}

	private void refreshLiveSummaries(final Map<TestExperimentSummary, IndividualTestSummary> summaries) {
		boolean rootsChanged = false;
		boolean replaced = false;
		for (final var experiment : summaries.keySet()) {
			final var key = new ExperimentKey(experiment);
			if (finalizedExperiments.contains(key)) continue;
			final int index = experimentIndex(key);
			if (index < 0) {
				experiments.add(experiment);
				rootsChanged = true;
			} else if (experiments.get(index) != experiment) {
				experiments.set(index, experiment);
				replaced = true;
			}
		}
		if (replaced) {
			refresh();
		} else if (rootsChanged) {
			children.clear();
			roots = filtered(new ArrayList<>(experiments));
			tree.setItemCount(roots.size());
		}
		tree.setRedraw(false);
		try {
			for (final var entry : summaries.entrySet()) {
				if (!finalizedExperiments.contains(new ExperimentKey(entry.getKey()))) {
					refreshLiveExperiment(entry.getKey(), entry.getValue());
				}
			}
		} finally {
			tree.setRedraw(true);
		}
		tree.redraw();
	}

	private void refreshLiveExperiment(final TestExperimentSummary experiment, final IndividualTestSummary test) {
		final var experimentItem = items.get(experiment);
		if (experimentItem == null || experimentItem.isDisposed()) return;
		children.remove(experiment);
		updateItem(experimentItem, experiment);
		final var summaries = new ArrayList<>(experiment.getSummaries().values());
		for (int i = 0; i < summaries.size(); i++) {
			if (summaries.get(i) == test && i < experimentItem.getItemCount()) {
				final TreeItem testItem = experimentItem.getItem(i);
				if (experimentItem.getExpanded() || testItem.getData() != null) { refreshLiveItem(testItem, test); }
				break;
			}
		}
	}

	private void refreshLiveItem(final TreeItem item, final AbstractSummary<?> summary) {
		final Object previous = item.getData();
		if (previous instanceof AbstractSummary<?> old && old != summary) {
			items.remove(old);
			children.remove(old);
		}
		item.setData(summary);
		items.put(summary, item);
		children.remove(summary);
		updateItem(item, summary);
		final var summaries = rows(summary);
		final TreeItem[] childItems = item.getItems();
		for (int i = 0; i < Math.min(summaries.size(), childItems.length); i++) {
			if (childItems[i].getData() != null) { refreshLiveItem(childItems[i], summaries.get(i)); }
		}
	}

	@Override
	public void finishTestSequence() {
		ui(() -> {
			synchronized (this) { pendingSummaries.clear(); }
			running = false;
			report = true;
			final boolean stopped = GAMA.getRegularGui().isTestStopRequested();
			status.setText(stopped ? "Stopped - partial results" : "");
			if (rerun == null) {
				refresh();
			} else {
				completedTests = rerun.completed();
				totalTests = rerun.total();
				rerun = null;
				updateCounters();
				updateControls();
			}
		});
	}

	private void replaceTestResult(final TestExperimentSummary result) {
		final var target = rerun;
		final var selection = TestSelection.of(target.test().getURI());
		final var matches = result.getSummaries().values().stream().filter(test -> selection.matches(test.getURI())).toList();
		if (matches.size() != 1) {
			GAMA.getRegularGui().getDialogFactory().error("Unable to update the selected test: "
					+ (result.getErrorDetails() == null ? "the rerun did not return this test" : result.getErrorDetails()));
			return;
		}
		final var replacement = matches.getFirst();
		if (target.test().getState() == TestState.NOT_RUN && replacement.getState() != TestState.NOT_RUN
				&& target.total() >= 0) {
			rerun = new TestRerun(target.experiment(), target.test(),
					Math.min(target.total(), target.completed() + 1), target.total());
		}
		target.experiment().summaries.replaceAll((key, test) -> test == target.test() ? replacement : test);
		target.experiment().setState(result.getState() == TestState.INTERRUPTED
				|| result.getState() == TestState.TIMED_OUT || result.aborted ? result.getState() : TestState.NOT_RUN);
		target.experiment().setError(result.getError());
		target.experiment().setErrorDetails(result.getErrorDetails());
		reconcileReport(target.test(), replacement);
	}

	private void reconcileReport(final IndividualTestSummary previous, final IndividualTestSummary replacement) {
		final Set<AbstractSummary<?>> expanded = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
		rememberExpanded(tree.getItems(), expanded);
		if (expanded.remove(previous)) { expanded.add(replacement); }
		final var selection = selected();
		final var selectedSummary = selection == previous ? replacement : selection;
		final var top = tree.getTopItem();
		final Object topSummary = top == null ? null : top.getData();
		children.clear();
		items.clear();
		roots = filtered(new ArrayList<>(experiments));
		tree.setRedraw(false);
		try {
			tree.setItemCount(roots.size());
			for (int i = 0; i < roots.size(); i++) { reconcileItem(tree.getItem(i), roots.get(i), expanded); }
			final var selectedItem = items.get(selectedSummary);
			if (selectedItem == null) { tree.deselectAll(); }
			else { tree.setSelection(selectedItem); }
			final var topItem = items.get(topSummary == previous ? replacement : topSummary);
			if (topItem != null) { tree.setTopItem(topItem); }
			showDetails();
		} finally { tree.setRedraw(true); }
		updateCounters();
	}

	private void rememberExpanded(final TreeItem[] entries, final Set<AbstractSummary<?>> expanded) {
		for (final var item : entries) {
			if (item.getExpanded() && item.getData() instanceof AbstractSummary<?> summary) {
				expanded.add(summary);
				rememberExpanded(item.getItems(), expanded);
			}
		}
	}

	private void reconcileItem(final TreeItem item, final AbstractSummary<?> summary,
			final Set<AbstractSummary<?>> expanded) {
		item.setData(summary);
		items.put(summary, item);
		updateItem(item, summary);
		final boolean open = expanded.contains(summary);
		item.setExpanded(open);
		if (open) {
			final var summaries = rows(summary);
			for (int i = 0; i < summaries.size(); i++) { reconcileItem(item.getItem(i), summaries.get(i), expanded); }
		} else {
			item.clearAll(true);
		}
	}

	private String textReport() {
		final StringBuilder text = new StringBuilder();
		for (final var summary : experiments) { appendReport(text, summary, ""); }
		return text.toString();
	}

	private void appendReport(final StringBuilder text, final AbstractSummary<?> summary, final String indent) {
		text.append(indent).append(summary.getState()).append(": ").append(summary.getTitle());
		if (summary.getErrorDetails() != null) { text.append(" [").append(summary.getErrorDetails()).append(']'); }
		text.append('\n');
		for (final var child : summary.getSummaries().values()) { appendReport(text, child, indent + "  "); }
	}

	private void export() {
		final var dialog = new FileDialog(root.getShell(), SWT.SAVE);
		dialog.setFileName("test-results.txt");
		dialog.setOverwrite(true);
		final String file = dialog.open();
		if (file == null) return;
		try {
			Files.writeString(Path.of(file), textReport(), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			GAMA.getRegularGui().getDialogFactory().error("Unable to export test results: " + e.getMessage());
		}
	}

	@Override
	public void setFocus() { if (tree != null && !tree.isDisposed()) { tree.setFocus(); } }

	@Override public void update(final IOutput output) {}
	@Override public void addOutput(final IOutput output) {}
	@Override public void removeOutput(final IOutput output) {}
	@Override public IOutput getOutput() { return null; }
	@Override public void close(final IScope scope) {}
	@Override public void changePartNameWithSimulation(final ISimulationAgent agent) {}
	@Override public void reset() { ui(this::refresh); }
	@Override public void setName(final String name) { ui(() -> setPartName(name)); }
	@Override public void updateToolbarState() {}
	@Override public boolean isVisible() { return root != null && !root.isDisposed() && root.isVisible(); }
	@Override public void showToolbar(final boolean show) {
		ui(() -> {
			toolbar.setVisible(show);
			((GridData) toolbar.getLayoutData()).exclude = !show;
			root.layout();
		});
	}

	@Override
	public void dispose() {
		experiments.clear();
		children.clear();
		items.clear();
		finalizedExperiments.clear();
		synchronized (this) { pendingSummaries.clear(); }
		roots = List.of();
		icons.values().forEach(Image::dispose);
		icons.clear();
		super.dispose();
	}
}
