/*******************************************************************************************************
 *
 * CompactTestView.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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
import gama.api.utils.tests.AbstractSummary;
import gama.api.utils.tests.CompoundSummary;
import gama.api.utils.tests.TestExperimentSummary;
import gama.api.utils.tests.TestState;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.SwtGui;
import gama.ui.shared.utils.WorkbenchHelper;

/** A virtual native tree holds rows, not per-assertion editor composites. */
public class CompactTestView extends ViewPart implements IGamaView, IGamaView.Test {

	private final List<AbstractSummary<?>> experiments = new ArrayList<>();
	private final Map<AbstractSummary<?>, List<AbstractSummary<?>>> children = new IdentityHashMap<>();
	private List<AbstractSummary<?>> roots = List.of();
	private final Map<TestState, Image> icons = new EnumMap<>(TestState.class);
	private org.eclipse.swt.widgets.Display display;
	private Composite root;
	private Tree tree;
	private Text details;
	private Label counters, status;
	private ProgressBar progress;
	private ToolItem stop, expand, collapse, filter, sort, export;
	private ToolBar tools, treeTools;
	private Composite toolbar;
	private SashForm sash;
	private boolean problemsOnly, severity, running, report;
	private volatile String currentName = "";
	private volatile int currentModel, totalModels;
	private volatile int completedTests, totalTests = -1;
	private volatile boolean progressPending;
	private volatile long generation;

	@Override
	public void createPartControl(final Composite parent) {
		root = new Composite(parent, SWT.NONE);
		display = parent.getDisplay();
		root.setLayout(new GridLayout(1, false));
		toolbar = new Composite(root, SWT.NONE);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final var toolbarLayout = new GridLayout(2, false);
		toolbarLayout.marginWidth = toolbarLayout.marginHeight = 0;
		toolbar.setLayout(toolbarLayout);
		tools = new ToolBar(toolbar, SWT.FLAT);
		tools.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		treeTools = new ToolBar(toolbar, SWT.FLAT);
		treeTools.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		counters = new Label(root, SWT.WRAP);
		counters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		status = new Label(root, SWT.NONE);
		status.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
		if (problemsOnly) { result.removeIf(s -> !s.getState().isFailure()); }
		if (severity) { result.sort(Comparator.comparing(AbstractSummary::getState)); }
		return result;
	}

	private void populate(final TreeItem item) {
		final TreeItem parent = item.getParentItem();
		final var siblings = rows(parent == null ? null : (AbstractSummary<?>) parent.getData());
		final int index = parent == null ? tree.indexOf(item) : parent.indexOf(item);
		if (index < 0 || index >= siblings.size()) return;
		final var summary = siblings.get(index);
		item.setData(summary);
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
		children.clear();
		roots = filtered(new ArrayList<>(experiments));
		tree.setRedraw(false);
		try {
			tree.removeAll();
			tree.setItemCount(rows(null).size());
			details.setText("");
			sash.setMaximizedControl(tree);
		} finally { tree.setRedraw(true); }
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
		updateControls();
		root.layout(true, true);
	}

	private void expand(final TreeItem[] items) {
		for (final var item : items) {
			item.getText();
			item.setExpanded(true);
			expand(item.getItems());
		}
	}

	private void updateControls() {
		// Keep the filter available even when it hides every result.
		final boolean resultsAvailable = !running && report && !experiments.isEmpty();
		if (resultsAvailable && (filter == null || filter.isDisposed())) {
			filter = tool(tools, SWT.CHECK, "Problems only", IGamaIcons.TEST_FILTER, () -> {
				problemsOnly = filter.getSelection();
				refresh();
			});
			filter.setSelection(problemsOnly);
			sort = tool(tools, SWT.CHECK, "Sort by severity", IGamaIcons.TEST_SORT, () -> {
				severity = sort.getSelection();
				refresh();
			});
			sort.setSelection(severity);
			export = tool(tools, SWT.PUSH, "Export results...", IGamaIcons.SAVE_AS, this::export);
		} else if (!resultsAvailable && filter != null && !filter.isDisposed()) {
			filter.dispose();
			sort.dispose();
			export.dispose();
		}
		if (!running && stop != null && !stop.isDisposed()) { stop.dispose(); }
		if (expand != null && !expand.isDisposed()) { expand.dispose(); }
		if (collapse != null && !collapse.isDisposed()) { collapse.dispose(); }
		if (running && (stop == null || stop.isDisposed())) {
			stop = new ToolItem(tools, SWT.PUSH, 0);
			stop.setImage(GamaIcon.named(IGamaIcons.EXPERIMENT_STOP).image());
			stop.setToolTipText("Stop tests");
			stop.addListener(SWT.Selection, e -> {
				if (GAMA.getRegularGui() instanceof SwtGui gui) { gui.stopTests(); }
				status.setText("Stopping tests...");
				stop.setEnabled(false);
			});
		} else if (!running && report && roots.stream().anyMatch(s -> !rows(s).isEmpty())) {
			expand = new ToolItem(treeTools, SWT.PUSH);
			expand.setImage(GamaIcon.named(IGamaIcons.TREE_EXPAND).image());
			expand.setToolTipText("Expand all");
			expand.addListener(SWT.Selection, e -> expand(tree.getItems()));
			collapse = new ToolItem(treeTools, SWT.PUSH);
			collapse.setImage(GamaIcon.named(IGamaIcons.TREE_COLLAPSE).image());
			collapse.setToolTipText("Collapse all");
			collapse.addListener(SWT.Selection, e -> {
				for (final var item : tree.getItems()) { item.setExpanded(false); }
			});
		}
		treeTools.setVisible(!running && expand != null && !expand.isDisposed());
		((GridData) treeTools.getLayoutData()).exclude = !treeTools.getVisible();
		progress.setVisible(running);
		((GridData) progress.getLayoutData()).exclude = !running;
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
			running = true;
			report = false;
			currentName = "Preparing tests...";
			currentModel = totalModels = 0;
			completedTests = 0;
			totalTests = -1;
			experiments.clear();
			refresh();
			status.setText(currentName);
			progress.setSelection(0);
		});
	}

	@Override
	public void addTestResult(final CompoundSummary<?, ?> summary) {
		ui(() -> {
			if (summary instanceof TestExperimentSummary) { experiments.add(summary); }
			else { experiments.addAll(summary.getSummaries().values()); }
			refresh();
		});
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
				progressPending = false;
				if (root.isDisposed() || !running || sequence != generation) return;
				counters.setText(testCountsText());
				status.setText((totalModels > 0 ? "Model " + currentModel + " of " + totalModels + " - " : "")
						+ currentName);
				progress.setMaximum(Math.max(1, totalTests >= 0 ? totalTests : totalModels));
				progress.setSelection(Math.max(0, totalTests >= 0 ? completedTests : currentModel - 1));
			});
		});
	}

	@Override
	public void finishTestSequence() {
		ui(() -> {
			running = false;
			report = true;
			final boolean stopped = GAMA.getRegularGui().isTestStopRequested();
			status.setText(stopped ? "Stopped - partial results" : "Finished");
			refresh();
		});
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
		roots = List.of();
		icons.values().forEach(Image::dispose);
		icons.clear();
		super.dispose();
	}
}
