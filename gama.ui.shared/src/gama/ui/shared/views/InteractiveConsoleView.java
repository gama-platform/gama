/*******************************************************************************************************
 *
 * InteractiveConsoleView.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views;

import static gama.ui.shared.resources.IGamaIcons.ACTION_CLEAR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.internal.console.IOConsoleViewer;

import gama.api.GAMA;
import gama.api.compilation.documentation.GamlIdiomsProvider;
import gama.api.kernel.PlatformAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IGamaView;
import gama.api.utils.StringUtils;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.access.ConsoleContextMenuHelper;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.ViewsHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView;
import gama.ui.shared.views.toolbar.TextSearchItem;

/**
 * The Class InteractiveConsoleView.
 */
public class InteractiveConsoleView extends GamaViewPart implements IToolbarDecoratedView.Sizable,
		IToolbarDecoratedView.LogExportable, IGamaView.Interactive {

	static {
		DEBUG.OFF();
	}

	/** The msg console. */
	private IOConsole msgConsole;

	/** The viewer. */
	IOConsoleViewer viewer;

	/** The error writer. */
	private OutputStreamWriter resultWriter, errorWriter;

	/** The reader. */
	BufferedReader reader;

	/** The agent. */
	// private ITopLevelAgent agent;

	private final InteractiveConsoleSession consoleSession = new InteractiveConsoleSession();

	/** Invalidates queued commands when the active top-level agent changes. */
	private long evaluationGeneration;

	/** Whether a command is waiting for or performing simulation-safe evaluation. */
	private boolean evaluationPending;

	/** Whether the action has started and may still be using the persistent bindings. */
	private boolean evaluationRunning;

	/** Suppresses stale result output when the console is explicitly reset. */
	private boolean resettingConsole;

	private long outputGeneration;

	private int inputStartOffset;

	/** The history. */
	private final List<String> history = new ArrayList<>();

	/** The index in history. */
	private int indexInHistory = 0;

	/** The unfinished command being edited while navigating history. */
	private String historyDraft = "";

	/** The control to display in full screen. */
	private Composite controlToDisplayInFullScreen;

	/** The parent of control to display full screen. */
	private Composite parentOfControlToDisplayFullScreen;

	/**
	 * Instantiates a new interactive console view.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 14 août 2023
	 */
	public InteractiveConsoleView() {
		DEBUG.OUT("InteractiveConsoleView created");
		GAMA.registerTopLevelAgentChangeListener(this);
	}

	@Override
	public void createPartControl(final Composite composite) {
		setParentOfControlToDisplayFullScreen(composite);
		final var layout = new GridLayout(1, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		getParentOfControlToDisplayFullScreen().setLayout(layout);
		controlToDisplayInFullScreen = new Composite(composite, SWT.NONE);
		controlToDisplayInFullScreen.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		super.createPartControl(controlToDisplayInFullScreen);

	}

	@Override
	public void init(final IViewSite site) throws PartInitException {
		super.init(site);
		// Issue #2816
		this.setTitleImage(GamaIcon.named(IGamaIcons.VIEW_INTERACTIVE).image());
	}

	@Override
	public void ownCreatePartControl(final Composite p) {
		msgConsole = new IOConsole("GAMA Console", null);
		reader = new BufferedReader(new InputStreamReader(msgConsole.getInputStream()));
		var stream = msgConsole.newOutputStream();
		stream.setColor(ThemeHelper.isDark() ? IGamaColors.NEUTRAL.lighter() : IGamaColors.NEUTRAL.color());
		stream.setActivateOnWrite(false);
		resultWriter = new OutputStreamWriter(stream);
		stream = msgConsole.newOutputStream();
		stream.setColor(IGamaColors.ERROR.color());
		stream.setActivateOnWrite(false);
		errorWriter = new OutputStreamWriter(stream);
		viewer = new IOConsoleViewer(p, msgConsole);

		viewer.setWordWrap(true);
		viewer.getTextWidget().setToolTipText(
				"Enter a GAML expression and press Enter. Type ?keyword for documentation. Use Up/Down to browse history.");
		ConsoleContextMenuHelper.install(viewer.getTextWidget(), () -> {
			msgConsole.clearConsole();
			inputStartOffset = 0;
			showPrompt();
		}, true);
		TextSearchItem.bind(this, viewer);

		msgConsole.getDocument().addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(final DocumentEvent event) {
				if (StringUtils.LN.equals(event.getText())) {
					var textEntered = "";
					try {
						textEntered = reader.readLine();
					} catch (final IOException e) {
						append("> Error reading command: " + e.getMessage(), true, true);
						return;
					}
					processInput(textEntered);
				} else if (endsWithPrompt(event.getDocument())) {
					inputStartOffset = event.getDocument().getLength();
					// Output (incl. the prompt) is appended asynchronously by the console: place the caret once the
					// prompt actually lands in the document
					moveCaretToEnd();
					if (!isEvaluationPending()) { viewer.getTextWidget().setEditable(true); }
				}
			}

			@Override
			public void documentAboutToBeChanged(final DocumentEvent event) {

			}
		});

		// Prepended so that it runs before the viewer's own key handling
		viewer.prependVerifyKeyListener(e -> {
			final StyledText text = viewer.getTextWidget();
			if (!text.getEditable()) return;
			final int caret = text.getCaretOffset();
			final int start = getInputStartOffset();
			if (caret < start) return;
			final boolean noSelection = text.getSelectionCount() == 0;
			if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
				e.doit = false;
				insertHistory(e.keyCode == SWT.ARROW_UP);
			} else if (e.keyCode == SWT.HOME
					|| e.keyCode == SWT.ARROW_LEFT && (e.stateMask & SWT.MOD1) != 0 && SWT.getPlatform().equals("cocoa")) {
				e.doit = false;
				if ((e.stateMask & SWT.SHIFT) != 0) {
					text.setSelection(caret, start);
				} else {
					text.setCaretOffset(start);
				}
			} else if ((e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.BS) && caret <= start && noSelection) {
				e.doit = false;
			}
		});
		p.layout(true, true);
		showPrompt();

	}

	/**
	 * Gets the control to display in full screen.
	 *
	 * @return the control to display in full screen
	 */
	public Composite getControlToDisplayInFullScreen() { return controlToDisplayInFullScreen; }

	/** The Constant PROMPT. */
	public static final String PROMPT = "gaml> ";

	/**
	 * Show prompt.
	 */
	private void showPrompt() {
		WorkbenchHelper.asyncRun(() -> {
			if (viewer == null || viewer.getTextWidget() == null || viewer.getTextWidget().isDisposed()) return;
			writeToConsole(StringUtils.LN + PROMPT, false);
			moveCaretToEnd();
		});
	}

	/**
	 * Insert history.
	 *
	 * @param back
	 *            the back
	 */
	private void insertHistory(final boolean back) {
		if (history.isEmpty()) {
			ViewsHelper.requestUserAttention(this, "No history");
			return;
		}
		final var doc = viewer.getDocument();
		final int start = getInputStartOffset();
		final int length = doc.getLength() - start;
		try {
			if (indexInHistory == history.size()) { historyDraft = doc.get(start, length); }
			final int newIndex = back ? indexInHistory - 1 : indexInHistory + 1;
			if (newIndex < 0 || newIndex > history.size()) return;
			final String command = newIndex == history.size() ? historyDraft : history.get(newIndex);
			doc.replace(start, length, command);
			indexInHistory = newIndex;
			viewer.getTextWidget().setCaretOffset(doc.getLength());
			viewer.getTextWidget().showSelection();
		} catch (final org.eclipse.jface.text.BadLocationException e) {
			DEBUG.ERR("Unable to navigate interactive console history", e);
		}
	}

	/**
	 * Returns the document offset where user input begins, i.e. just after the prompt on the last line.
	 *
	 * @return the input start offset
	 */
	private int getInputStartOffset() {
		final var doc = viewer.getDocument();
		return Math.min(inputStartOffset, doc.getLength());
	}

	/**
	 * Append the text to the console.
	 *
	 * @param text
	 *            to display in the console
	 */
	public void append(final String text, final boolean error, final boolean showPrompt) {

		WorkbenchHelper.asyncRun(() -> {
			if (viewer == null || viewer.getTextWidget() == null || viewer.getTextWidget().isDisposed()) return;
			writeToConsole(text, error);
			if (showPrompt) {
				writeToConsole(StringUtils.LN + PROMPT, false);
				moveCaretToEnd();
			}

		});

	}

	private void writeToConsole(final String text, final boolean error) {
		@SuppressWarnings ("resource") final var writer = error ? errorWriter : resultWriter;
		try {
			writer.append(text);
			writer.flush();
		} catch (final IOException e) {
			DEBUG.ERR("Unable to write to interactive console", e);
		}
	}

	private static boolean endsWithPrompt(final org.eclipse.jface.text.IDocument doc) {
		final int length = doc.getLength();
		if (length < PROMPT.length()) return false;
		try {
			return PROMPT.equals(doc.get(length - PROMPT.length(), PROMPT.length()));
		} catch (final org.eclipse.jface.text.BadLocationException e) {
			return false;
		}
	}

	private void moveCaretToEnd() {
		if (viewer == null || viewer.getTextWidget() == null || viewer.getTextWidget().isDisposed()) return;
		viewer.getTextWidget().getDisplay().asyncExec(() -> {
			if (viewer != null && viewer.getTextWidget() != null && !viewer.getTextWidget().isDisposed()) {
				viewer.getTextWidget().setCaretOffset(viewer.getTextWidget().getCharCount());
				viewer.getTextWidget().showSelection();
			}
		});
	}

	@Override
	public void widgetDisposed(final DisposeEvent e) {
		reset();
		super.widgetDisposed(e);
	}

	@Override
	public void dispose() {
		GAMA.removeTopLevelAgentChangeListener(this);
		consoleSession.close();
		DEBUG.OUT("Console removed from GAMA listeners");
		super.dispose();
	}

	@Override
	public void close(final IScope scope) {
		reset();
		super.close(scope);
	}

	@Override
	public void reset() {
		synchronized (this) {
			outputGeneration++;
			resettingConsole = true;
		}
		if (msgConsole != null) { msgConsole.clearConsole(); }
		inputStartOffset = 0;
		topLevelAgentChanged(null);
		showPrompt();
		synchronized (this) { resettingConsole = false; }
	}

	@Override
	public Control getSizableFontControl() {
		if (viewer == null) return null;
		return viewer.getTextWidget();
	}

	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		super.createToolItems(tb);
		GamaToolbarSimple tbs = toolbar.getToolbar(SWT.RIGHT);
		TextSearchItem.install(tbs, () -> TextSearchItem.open(this));
		tbs.button("editor/local.menu", "More...", "More options", e -> {

			final GamaMenu menu = new GamaMenu() {

				@Override
				protected void fillMenu() {
					GamaCommand.build(ACTION_CLEAR, "Clear", "Clear the console", e -> {
						msgConsole.clearConsole();
						showPrompt();
					}).toItem(mainMenu);
					GamaCommand.build(null, "Console help", "Show interactive console shortcuts and help",
							e -> append("Enter a GAML expression and press Enter to evaluate it." + StringUtils.LN
									+ "Type ?keyword to display documentation for a GAML keyword." + StringUtils.LN
									+ "Use Up/Down to browse command history; Down restores the current input."
									+ StringUtils.LN, false, false)).toItem(mainMenu);
					GamaMenu.separate(mainMenu);
					GamaCommand.build(IGamaIcons.DISPLAY_TOOLBAR_CSVEXPORT, "Export to log file", "Export to log file",
							e -> saveAsLog()).toItem(mainMenu);
				}

			};
			menu.open(tbs, e, tbs.getSize().y, 0);
		});

		this.topLevelAgentChanged(GAMA.getCurrentTopLevelAgent());
	}

	@Override
	protected ViewUpdateUIJob createUpdateJob() {
		return null;
	}

	/**
	 * As ConsoleView is automatically opened by moving to the simulation perspective, the automatic closing can cause
	 * problems. So the view is stated as accepting an "experiment-less" mode. See Issue #1361 Method
	 * shouldBeClosedWhenNoExperiments()
	 *
	 * @see ummisco.gama.ui.views.GamaViewPart#shouldBeClosedWhenNoExperiments()
	 */
	@Override
	protected boolean shouldBeClosedWhenNoExperiments() {
		return false;
	}

	@Override
	protected boolean needsOutput() {
		return false;
	}

	// @Override
	// public void append(final String text, final ITopLevelAgent agent, final GamaColor color) {
	// setExecutorAgent(agent);
	// if (text != null) { append(text, false, true); }
	// }

	/**
	 * Sets the executor agent.
	 *
	 * @param agent
	 *            the new executor agent
	 */
	@Override
	public void topLevelAgentChanged(final ITopLevelAgent agent) {
		boolean cancelledPendingEvaluation = false;
		boolean suppressCancellationOutput;
		synchronized (this) {
			evaluationGeneration++;
			if (!(evaluationPending && evaluationRunning)) {
				if (evaluationPending) {
					evaluationPending = false;
					cancelledPendingEvaluation = true;
				}
			}
			suppressCancellationOutput = resettingConsole;
		}
		consoleSession.setAgent(agent);
		if (cancelledPendingEvaluation && !suppressCancellationOutput) {
			publishResult("> Command cancelled because the active experiment changed.", true, null, -1);
		}
		if (agent == null) {
			WorkbenchHelper.asyncRun(() -> {
				if (toolbar != null && !toolbar.isDisposed()) {
					toolbar.wipe(SWT.LEFT, true);
					// toolbar.setBackgroundColor(null);
					toolbar.update();
					toolbar.requestLayout();
				}
			});
		} else {
			DEBUG.OUT("Changing top level agent to " + agent.getName());
			WorkbenchHelper.asyncRun(() -> {
				if (toolbar != null) {
					toolbar.status(GamaIcon.ofColor(agent.getColor()).getCode(),
							"Interacting with " + agent.getFamilyName() + " " + agent.getName());
					// toolbar.setBackgroundColor(GamaColors.toSwtColor(agent.getColor()));
					toolbar.update();
					toolbar.requestLayout();
				}
			});
		}
		// this.agent = agent;
	}

	/**
	 * Process input.
	 *
	 * @param s
	 *            the s
	 */
	protected void processInput(final String s) {
		final var entered = s == null ? "" : s.trim();
		if (!entered.isEmpty()) { history.add(entered); }
		indexInHistory = history.size();
		historyDraft = "";
		final var agent = GAMA.getCurrentTopLevelAgent();
		if (entered.startsWith("?")) {
			final String result = GamlIdiomsProvider.getDocumentationOn(entered.substring(1));
			append(result == null ? "nil" : result, false, true);
		} else if (agent == null || agent instanceof PlatformAgent || agent.dead()) {
			topLevelAgentChanged(null);
			append("> No active experiment. Start an experiment before evaluating a command.", true, true);
		} else {
			final long generation;
			final long currentOutputGeneration;
			synchronized (this) {
				evaluationPending = true;
				evaluationRunning = false;
				generation = evaluationGeneration;
				currentOutputGeneration = outputGeneration;
			}
			viewer.getTextWidget().setEditable(false);
			try {
				CompletableFuture.runAsync(
						() -> scheduleEvaluation(agent, entered, generation, currentOutputGeneration));
			} catch (final RuntimeException e) {
				finishEvaluation(generation, currentOutputGeneration, agent, "> Error: " + errorMessage(e), true);
			}
		}

	}

	private void scheduleEvaluation(final ITopLevelAgent agent, final String input, final long generation,
			final long currentOutputGeneration) {
		try {
			agent.executeAction(scope -> {
				if (!beginEvaluation(generation, agent)) return null;
				try {
					if (agent.dead()) {
						finishEvaluation(generation, currentOutputGeneration, agent,
								"> The active experiment is no longer available.", true);
					} else {
						final InteractiveConsoleEvaluator.Result result =
								InteractiveConsoleEvaluator.evaluate(input, agent, scope, consoleSession, consoleSession);
						finishEvaluation(generation, currentOutputGeneration, agent, result.output(), result.error());
					}
				} catch (final RuntimeException e) {
					finishEvaluation(generation, currentOutputGeneration, agent, "> Error: " + errorMessage(e), true);
				} finally {
					consoleSession.endEvaluation();
				}
				return null;
			});
		} catch (final RuntimeException e) {
			finishEvaluation(generation, currentOutputGeneration, agent, "> Error: " + errorMessage(e), true);
		}
	}

	private boolean beginEvaluation(final long generation, final ITopLevelAgent agent) {
		synchronized (this) {
			if (!evaluationPending || generation != evaluationGeneration) return false;
			consoleSession.beginEvaluation(agent);
			evaluationRunning = true;
			return true;
		}
	}

	private void finishEvaluation(final long generation, final long currentOutputGeneration,
			final ITopLevelAgent agent, String result, boolean error) {
		boolean outputStillCurrent;
		synchronized (this) {
			if (!evaluationPending) return;
			evaluationPending = false;
			evaluationRunning = false;
			if (generation != evaluationGeneration) {
				result = "> Command cancelled because the active experiment changed.";
				error = true;
			}
			outputStillCurrent = currentOutputGeneration == outputGeneration;
		}
		if (outputStillCurrent) {
			publishResult(result, error, agent, generation, currentOutputGeneration);
		} else {
			WorkbenchHelper.asyncRun(() -> {
				if (viewer != null && viewer.getTextWidget() != null && !viewer.getTextWidget().isDisposed()) {
					viewer.getTextWidget().setEditable(true);
				}
			});
		}
	}

	private void publishResult(final String result, final boolean error, final ITopLevelAgent agent,
			final long generation) {
		final long currentOutputGeneration;
		synchronized (this) { currentOutputGeneration = outputGeneration; }
		publishResult(result, error, agent, generation, currentOutputGeneration);
	}

	private void publishResult(final String result, final boolean error, final ITopLevelAgent agent,
			final long generation, final long currentOutputGeneration) {
		WorkbenchHelper.asyncRun(() -> {
			if (viewer == null || viewer.getTextWidget() == null || viewer.getTextWidget().isDisposed()) return;
			if (!isCurrentOutputGeneration(currentOutputGeneration)) return;
			append(result == null ? "nil" : result, error, true);
			if (!error && isCurrentGeneration(generation) && agent == GAMA.getCurrentTopLevelAgent()
					&& GAMA.getExperiment() != null) {
				GAMA.getExperiment().refreshAllOutputs();
			}
		});
	}

	private synchronized boolean isCurrentGeneration(final long generation) {
		return generation >= 0 && generation == evaluationGeneration;
	}

	private synchronized boolean isCurrentOutputGeneration(final long generation) {
		return generation == outputGeneration;
	}

	private synchronized boolean isEvaluationPending() { return evaluationPending; }

	private static String errorMessage(final Exception e) {
		return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
	}

	/**
	 * Gets the parent of control to display full screen.
	 *
	 * @return the parent of control to display full screen
	 */
	public Composite getParentOfControlToDisplayFullScreen() { return parentOfControlToDisplayFullScreen; }

	/**
	 * Sets the parent of control to display full screen.
	 *
	 * @param parentOfControlToDisplayFullScreen
	 *            the new parent of control to display full screen
	 */
	public void setParentOfControlToDisplayFullScreen(final Composite parentOfControlToDisplayFullScreen) {
		this.parentOfControlToDisplayFullScreen = parentOfControlToDisplayFullScreen;
	}

	@Override
	public String getContents() { return viewer.getDocument().get(); }

	@Override
	public void setFocus() {
		Control c = getSizableFontControl();
		if (c != null && !c.isDisposed() && !c.isFocusControl()) {
			c.setFocus(); // Necessary ?
		}
	}

}
