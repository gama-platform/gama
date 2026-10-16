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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import gama.api.compilation.descriptions.IVarDescriptionProvider;
import gama.api.compilation.documentation.GamlIdiomsProvider;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.PlatformAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IExecutionContext;
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

/**
 * The Class InteractiveConsoleView.
 */
public class InteractiveConsoleView extends GamaViewPart implements IToolbarDecoratedView.Sizable,
		IToolbarDecoratedView.LogExportable, IExecutionContext, IVarDescriptionProvider, IGamaView.Interactive {

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

	/** The temps. */
	private final Map<String, Object> temps = new LinkedHashMap<>();

	/**
	 * Tracks the declared type of each persistent console variable. Populated the first time {@link #getVarExpr} is
	 * called for a variable and kept even if the runtime value later becomes {@code nil}, so that the original
	 * declaration type is preserved across commands.
	 */
	private final Map<String, IType<?>> tempTypes = new LinkedHashMap<>();

	/**
	 * A persistent execution context for the interactive console. All variable declarations and assignments performed
	 * in the console are stored directly in the {@link #temps} map so they survive across commands (similar to a Python
	 * REPL session). The context is self-referential ({@code createChildContext} / {@code createCopy} / {@code
	 * getOuterContext} all return {@code this}) so that it is never replaced when the underlying scope pushes and pops
	 * statement contexts. Its lifecycle still clears the console bindings when the context is disposed or when the
	 * active top-level agent changes, preventing variables from leaking across experiments/sessions.
	 */
	private final IExecutionContext consoleContext = new IExecutionContext() {

		private ITopLevelAgent lastTopLevelAgent;

		/**
		 * Intentionally a no-op: {@link ExecutionScope#pop(ISymbol)} calls {@code previous.dispose()} on the context
		 * after every statement execution. Because {@link #getOuterContext()} returns {@code this}, {@code previous} IS
		 * the console context, so a normal {@code dispose()} implementation would wipe {@code temps} after every
		 * command — preventing variables declared in one command from being visible in the next. Explicit cleanup is
		 * handled by {@link InteractiveConsoleView#topLevelAgentChanged} and {@link InteractiveConsoleView#reset}.
		 */
		@Override
		public void dispose() { /* no-op: persist variables across commands */ }

		@Override
		public IScope getScope() {
			final ITopLevelAgent a = GAMA.getCurrentTopLevelAgent();
			if (a != lastTopLevelAgent) {
				clearLocalVars();
				lastTopLevelAgent = a;
			}
			return a != null ? a.getScope() : GAMA.getRuntimeScope();
		}

		@Override
		public IExecutionContext getOuterContext() { return this; }

		@Override
		public IExecutionContext createCopy(final ISymbol command) { return this; }

		@Override
		public IExecutionContext createChildContext(final ISymbol command) { return this; }

		/** Always returns 0 — avoids infinite recursion from the default {@code depth()} implementation. */
		@Override
		public int depth() { return 0; }

		@Override
		public void setTempVar(final String name, final Object value) { temps.put(name, value); }

		@Override
		public Object getTempVar(final String name) { return temps.get(name); }

		@Override
		public Map<? extends String, ? extends Object> getLocalVars() { return temps; }

		@Override
		public void clearLocalVars() {
			temps.clear();
			tempTypes.clear();
		}

		@Override
		public void putLocalVar(final String varName, final Object val) { temps.put(varName, val); }

		@Override
		public Object getLocalVar(final String string) { return temps.get(string); }

		@Override
		public boolean hasLocalVar(final String name) { return temps.containsKey(name); }

		/**
		 * Intentionally a no-op: REPL variables declared in one command must persist into subsequent commands.
		 * Removing a local var at block-scope exit would silently erase the user's declaration from the console
		 * context. Use {@link #clearLocalVars()} (or {@link IExecutionContext#dispose()}) to wipe all bindings.
		 */
		@Override
		public void removeLocalVar(final String name) { /* no-op: persist across commands */ }

		@Override
		public ISymbol getCurrentSymbol() { return null; }

		@Override
		public void setCurrentSymbol(final ISymbol statement) {}
	};

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
		controlToDisplayInFullScreen = new Composite(composite, SWT.BORDER);
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
	public IScope getScope() { return GAMA.getCurrentTopLevelAgent().getScope(); }

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
			showPrompt();
		}, true);

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
					// Output (incl. the prompt) is appended asynchronously by the console: place the caret once the
					// prompt actually lands in the document
					moveCaretToEnd();
				}
			}

			@Override
			public void documentAboutToBeChanged(final DocumentEvent event) {

			}
		});

		// Prepended so that it runs before the viewer's own key handling
		viewer.prependVerifyKeyListener(e -> {
			final StyledText text = viewer.getTextWidget();
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
		try {
			final int lineStart = doc.getLineOffset(doc.getNumberOfLines() - 1);
			final String line = doc.get(lineStart, doc.getLength() - lineStart);
			return line.startsWith(PROMPT) ? lineStart + PROMPT.length() : lineStart;
		} catch (final org.eclipse.jface.text.BadLocationException e) {
			return doc.getLength();
		}
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
		if (msgConsole != null) { msgConsole.clearConsole(); }
		topLevelAgentChanged(null);
		showPrompt();
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
		// Clear persistent console variables when the top-level agent changes so that
		// bindings from a previous experiment/model do not leak into the new session.
		temps.clear();
		tempTypes.clear();
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
			if (!(agent instanceof PlatformAgent)) {
				agent.getSpecies().getDescription().attachAlternateVarDescriptionProvider(this);
			}
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
		if (agent == null || agent.dead()) {
			topLevelAgentChanged(null);
			append("> No active experiment. Start an experiment before evaluating a command.", true, true);
		} else {
			String result = null;
			var error = false;
			if (entered.startsWith("?")) {
				result = GamlIdiomsProvider.getDocumentationOn(entered.substring(1));
			} else {
				IScope scope = agent.getScope().copy("In interactive console");
				// Install the persistent console context so that variable declarations
				// (e.g. "int i <- 0") survive across commands and are accessible in later
				// commands (e.g. "i <- 1" or just "i").
				scope.setExecutionContext(consoleContext);
				try {
					final IExpression expr = GAML.compileExpression(s, agent, this, false);
					if (expr != null) { result = StringUtils.toGaml(scope.evaluate(expr, agent).getValue(), true); }
				} catch (final Exception e) {
					error = true;
					result = "> Error: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
				} finally {
					agent.getSpecies().removeTemporaryAction();
					GAMA.releaseScope(scope);
				}
			}
			if (result == null) { result = "nil"; }
			append(result, error, true);
			if (!error && GAMA.getExperiment() != null) { GAMA.getExperiment().refreshAllOutputs(); }
		}

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
	public void setTempVar(final String name, final Object value) {
		temps.put(name, value);

	}

	@Override
	public Object getTempVar(final String name) {
		return temps.get(name);
	}

	@Override
	public Map<? extends String, ? extends Object> getLocalVars() { return temps; }

	@Override
	public void clearLocalVars() {
		temps.clear();
		tempTypes.clear();
	}

	@Override
	public void putLocalVar(final String name, final Object val) {
		temps.put(name, val);

	}

	@Override
	public Object getLocalVar(final String name) {
		return temps.get(name);
	}

	@Override
	public boolean hasLocalVar(final String name) {
		return temps.containsKey(name);
	}

	/**
	 * Intentionally a no-op: REPL variables must persist across commands. Removing a local var at block-scope exit
	 * would silently erase the user's declaration from the persistent console context. All bindings are cleared at
	 * once by {@link #clearLocalVars()} when the experiment/session ends.
	 */
	@Override
	public void removeLocalVar(final String name) { /* no-op: persist across commands */ }

	@Override
	public IExecutionContext getOuterContext() { return this; }

	@Override
	public IExecutionContext createCopy(final ISymbol command) {
		return this;
	}

	@Override
	public IExecutionContext createChildContext(final ISymbol command) {
		return this;
	}

	@Override
	public IExpression getVarExpr(final String name, final boolean asField) {
		if (temps.containsKey(name)) {
			// Look up the cached type first (set at declaration time or on first non-nil access).
			IType<?> t = tempTypes.get(name);
			if (t == null) {
				// No cached type yet: derive from the current runtime value.
				t = GamaType.of(temps.get(name));
				// Only cache meaningful types; if the value is currently nil the derived type
				// is NO_TYPE / unknown, and we leave tempTypes empty so the next call can try
				// again once the value is non-nil.
				if (t != null && t != Types.NO_TYPE) { tempTypes.put(name, t); }
			}
			return GAML.getExpressionFactory().createVar(name, t, false, IVarExpression.Category.TEMP, null);
		}
		return null;
	}

	@Override
	public boolean hasAttribute(final String name) {
		return temps.containsKey(name);
	}

	@Override
	public String getContents() { return viewer.getDocument().get(); }

	@Override
	public ISymbol getCurrentSymbol() { return null; }

	@Override
	public void setCurrentSymbol(final ISymbol statement) {}

	@Override
	public void setFocus() {
		Control c = getSizableFontControl();
		if (c != null && !c.isDisposed() && !c.isFocusControl()) {
			c.setFocus(); // Necessary ?
		}
	}

}
