/*******************************************************************************************************
 *
 * SwtGui.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.utils;

import static gama.ui.shared.utils.ViewsHelper.hideView;
import static gama.ui.shared.utils.WorkbenchHelper.getClipboard;

import java.awt.EventQueue;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.ICommandService;

import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.compilation.IModelsManager;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.IParameter;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.IRuntimeExceptionHandler;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.font.IFont;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.ui.IConsoleListener;
import gama.api.ui.IDialogFactory;
import gama.api.ui.IGamaView;
import gama.api.ui.IGamaView.Console;
import gama.api.ui.IGamaView.Parameters;
import gama.api.ui.IGamaView.Test;
import gama.api.ui.IGamaView.User;
import gama.api.ui.IGui;
import gama.api.ui.IOutput;
import gama.api.ui.IProgressIndicator;
import gama.api.ui.IStatusDisplayer;
import gama.api.ui.displays.IDisplayCreator;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.server.CommandExecutor;
import gama.api.utils.server.ISocketCommand;
import gama.api.utils.tests.CompoundSummary;
import gama.core.outputs.InspectDisplayOutput;
import gama.core.outputs.display.AbstractDisplayGraphics;
import gama.dev.DEBUG;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.application.workbench.SimulationPerspectiveDescriptor;
import gama.ui.shared.interfaces.IDisplayLayoutManager;
import gama.ui.shared.interfaces.IRefreshHandler;
import gama.ui.shared.interfaces.ISpeedDisplayer;
import gama.ui.shared.parameters.EditorsDialog;
import gama.ui.shared.parameters.GamaWizard;
import gama.ui.shared.parameters.GamaWizardDialog;
import gama.ui.shared.parameters.GamaWizardPage;
import gama.ui.shared.resources.GamaColors;
import gama.workspace.console.CompositeConsoleListener;
import gama.workspace.status.ProgressIndicator;

/**
 * Written by drogoul Modified on 6 mai 2011
 *
 * @todo Description
 *
 */
public class SwtGui implements IGui {

	static {
		DEBUG.OFF();
	}

	/** The all tests running. */
	public volatile static boolean ALL_TESTS_RUNNING;

	/** The highlighted agent. */
	private IAgent highlightedAgent;

	/** The mouse location in model. */
	private IPoint mouseLocationInModel, mouseLocationInDisplay;

	/** The parameters view. */
	private final IGamaView.Parameters[] parametersView = new IGamaView.Parameters[1];

	/** The console. */
	IConsoleListener console;

	/** The dialog factory. */
	IDialogFactory dialogFactory;

	/**
	 * Holds the work submitted by {@link #arrangeExperimentViews} so it can be executed inside the same
	 * {@code WorkbenchHelper.run()} syncExec as the display-view openings in {@link #openAndApplyLayout}. Set by
	 * {@code arrangeExperimentViews} and drained (read-then-null) by {@code openAndApplyLayout}.
	 */
	private volatile Runnable pendingArrange;

	static {
		PreferencesHelper.initialize();
		AbstractDisplayGraphics.getCachedGC();
	}

	/**
	 * Instantiates a new swt gui.
	 */
	public SwtGui() {}

	@Override
	public boolean confirmClose(final IExperimentSpecies exp) {
		if (exp == null || !GamaPreferences.Runtime.CORE_ASK_CLOSING.getValue()) return true;
		PerspectiveHelper.switchToSimulationPerspective();
		return getDialogFactory().modalQuestion("Closing experiment", "Do you want to close experiment '"
				+ exp.getName() + "' of model '" + exp.getModel().getName() + "' ?");
	}

	@Override
	public void runtimeError(final IScope scope, final GamaRuntimeException g) {
		if (g.isReported() || GAMA.getFrontmostController() != null && GAMA.getFrontmostController().isDisposing())
			return;
		final IRuntimeExceptionHandler handler = getRuntimeExceptionHandler();
		if (!handler.isRunning()) { handler.start(); }
		handler.offer(g);
		g.setReported();
	}

	/**
	 * Display errors.
	 *
	 * @param scope
	 *            the scope
	 * @param exceptions
	 *            the exceptions
	 * @param reset
	 *            the reset
	 */
	@Override
	public void displayErrors(final IScope scope, final List<GamaRuntimeException> exceptions, final boolean reset) {
		if (exceptions == null) {
			hideView(ERROR_VIEW_ID);
		} else {
			// Run on the UI thread to avoid race conditions. If there are real errors and the
			// view is not yet open, open it first so that it is populated immediately. This
			// replaces the previous openErrorView() + displayErrors() two-step in
			// RuntimeExceptionHandler.updateUI() which suffered from a timing race: the view
			// was opened asynchronously while displayErrors() ran immediately on the background
			// thread, finding no view and therefore displaying nothing.
			WorkbenchHelper.run(() -> {
				IGamaView.Error v = (IGamaView.Error) ViewsHelper.findView(ERROR_VIEW_ID, null, false);
				if (v == null && !exceptions.isEmpty()) {
					v = (IGamaView.Error) showView(null, ERROR_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
				}
				if (v != null) { v.displayErrors(reset); }
			});
		}
	}

	@Override
	public IGamaView.Test openTestView(final IScope scope, final boolean allTests) {
		ALL_TESTS_RUNNING = allTests;
		final IGamaView.Test v = (Test) showView(scope, TEST_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
		if (v != null) { v.startNewTestSequence(allTests); }
		return v;
	}

	/**
	 * Display tests progress.
	 *
	 * @param scope
	 *            the scope
	 */
	@Override
	public void displayTestsProgress(final IScope scope, final int number, final int total) {
		final IGamaView.Test v = (Test) WorkbenchHelper.getPage().findView(TEST_VIEW_ID);
		if (v != null) { v.displayProgress(number, total); }
	}

	@Override
	public void displayTestsResults(final IScope scope, final CompoundSummary<?, ?> summary) {
		IGamaView.Test v = (Test) WorkbenchHelper.getPage().findView(TEST_VIEW_ID);
		if (v != null) { v.addTestResult(summary); }
	}

	@Override
	public void endTestDisplay() {
		final IGamaView.Test v = (Test) WorkbenchHelper.getPage().findView(TEST_VIEW_ID);
		if (v != null) { v.finishTestSequence(); }
		WorkbenchHelper.refreshNavigator();
	}

	@Override
	public void clearErrors(final IScope scope) {
		final IRuntimeExceptionHandler handler = getRuntimeExceptionHandler();
		handler.clearErrors();
	}

	/**
	 * Internal show view.
	 *
	 * @param viewId
	 *            the view id
	 * @param secondaryId
	 *            the secondary id
	 * @param code
	 *            the code
	 * @return the object
	 */
	private Object internalShowView(final String viewId, final String secondaryId, final int code) {
		if (GAMA.getFrontmostController() != null && GAMA.getFrontmostController().isDisposing()) return null;
		final Object[] result = new Object[1];
		WorkbenchHelper.run(() -> {
			try {
				final IWorkbenchPage page = WorkbenchHelper.getPage();
				if (page != null) {
					// page.zoomOut();
					final String second = secondaryId == null ? null
							: secondaryId + "@@@" + String.valueOf(System.currentTimeMillis());
					// The goal here is to address #2441 by randomizing the ids of views.
					// DEBUG.LOG("Opening view " + viewId + " " + second);
					result[0] = page.showView(viewId, second, code);
				}
			} catch (final Exception e) {
				result[0] = e;
			}
		});
		return result[0];
	}

	/** The transfers. */
	static Transfer[] transfers = { TextTransfer.getInstance() };

	@Override
	public boolean copyToClipboard(final String text) {
		if (getClipboard() == null || text == null) return false;
		WorkbenchHelper.asyncRun(() -> { getClipboard().setContents(new String[] { text }, transfers); });
		return true;
	}

	@Override
	public String copyTextFromClipboard() {
		if (getClipboard() == null) return null;
		return (String) WorkbenchHelper.run(() -> getClipboard().getContents(TextTransfer.getInstance()));
	}

	/**
	 * Open welcome page.
	 */
	@Override
	public void openWebDocumentationPage() {
		WebHelper.openWelcomePage();
	}

	@Override
	public IGamaView showView(final IScope scope, final String viewId, final String secondaryId, final int code) {

		Object o = internalShowView(viewId, secondaryId, code);
		if (o instanceof IWorkbenchPart) {
			if (o instanceof IGamaView) return (IGamaView) o;
			o = GamaRuntimeException.error("Impossible to open view " + viewId, scope);
		}
		if (o instanceof Throwable) {
			GAMA.reportError(scope, GamaRuntimeException.create((Exception) o, scope), false);
		}
		return null;
	}

	@Override
	public boolean openSimulationPerspective(final IModelSpecies model, final String experimentName) {
		if (model == null) return false;
		return PerspectiveHelper.openSimulationPerspective(model, experimentName);
	}

	/**
	 * Creates the display surface for.
	 *
	 * @param output
	 *            the output
	 * @param args
	 *            the args
	 * @return the i display surface
	 */
	@Override
	public IDisplaySurface createDisplaySurfaceFor(final IOutput.Display output, final Object uiComponent) {
		final String keyword = output.getData().getDisplayType();
		final IDisplayCreator creator = GamaAdditionRegistry.getDisplay(keyword);
		if (creator == null)
			throw GamaRuntimeException.error("Display " + keyword + " is not defined anywhere.", output.getScope());
		IDisplaySurface surface = creator.create(output, uiComponent);
		surface.outputReloaded();
		return surface;
	}

	/**
	 * Gets the frontmost display surface.
	 *
	 * @return the frontmost display surface
	 */
	@Override
	public IDisplaySurface getFrontmostDisplaySurface() { return ViewsHelper.frontmostDisplaySurface(); }

	@Override
	public Map<String, Object> openUserInputDialog(final IScope scope, final String title,
			final List<IParameter> parameters, final IFont font, final IColor color, final Boolean showTitle) {
		final IMap<String, Object> result = GamaMapFactory.createUnordered();
		for (final IParameter p : parameters) { result.put(p.getName(), p.getInitialValue(scope)); }
		WorkbenchHelper.run(() -> {
			final EditorsDialog dialog = new EditorsDialog(scope, null, parameters, title, font, color, showTitle);
			if (dialog.open() == Window.OK) { result.putAll(dialog.getValues()); }
		});
		return result;
	}

	@Override
	public IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final IActionDescription finish, final IList<IMap<String, Object>> pages) {
		final IMap<String, IMap<String, Object>> result = GamaMapFactory.create();
		final IList<GamaWizardPage> wizardPages = GamaListFactory.create();
		for (IMap<String, Object> l : pages) {
			// GamaFont f = (GamaFont) l.get(IKeyword.FONT);
			String t = (String) l.get(IKeyword.TITLE);
			String d = (String) l.get(IKeyword.DESCRIPTION);
			@SuppressWarnings ("unchecked") List<IParameter> ps = (List<IParameter>) l.get(IKeyword.PARAMETERS);
			wizardPages.add(new GamaWizardPage(scope, ps, t, d));

		}

		WorkbenchHelper.run(() -> {
			final GamaWizard wizard = new GamaWizard(title, finish, wizardPages);
			GamaWizardDialog wizardDialog = new GamaWizardDialog(WorkbenchHelper.getShell(), wizard);

			if (wizardDialog.open() == Window.OK) { result.putAll(wizardDialog.getValues()); }
		});
		return result;
	}

	@Override
	public void openUserControlPanel(final IScope scope, final IStatement panel) {
		WorkbenchHelper.run(() -> {
			IGamaView.User part = (User) showView(scope, USER_CONTROL_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
			if (part != null) { part.initFor(scope, panel); }
			scope.setOnUserHold(true);
			try {
				WorkbenchHelper.getPage().showView(USER_CONTROL_VIEW_ID);
			} catch (final PartInitException e) {
				e.printStackTrace();
			}
		});

	}

	@Override
	public void closeDialogs(final IScope scope) {

		WorkbenchHelper.run(() -> {
			getDialogFactory().closeUserDialog();
			hideView(USER_CONTROL_VIEW_ID);

		});

	}

	@Override
	public IAgent getHighlightedAgent() { return highlightedAgent; }

	@Override
	public void setHighlightedAgent(final IAgent a) { highlightedAgent = a; }

	/**
	 * Gets the model runner.
	 *
	 * @return the model runner
	 */
	@Override
	public IModelsManager getModelsManager() { return WorkbenchHelper.getService(IModelsManager.class); }

	/**
	 * Update parameters.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 17 août 2023
	 */
	@Override
	public void updateParameters(final boolean retrieveValues) {
		// Use asyncRun to avoid blocking the command thread with a syncExec during experiment launch.
		// The parameters view update is purely cosmetic at launch time and does not need to complete
		// before the simulation starts.
		WorkbenchHelper.asyncRun(() -> {
			boolean showIt = GAMA.getExperiment() != null && GAMA.getExperiment().hasParametersOrUserCommands()
					&& !PerspectiveHelper.isModelingPerspective() && PerspectiveHelper.showParameters();
			if (showIt) {
				parametersView[0] = (Parameters) showView(GAMA.getRuntimeScope(), PARAMETER_VIEW_ID, null,
						IWorkbenchPage.VIEW_ACTIVATE);
			} else {
				parametersView[0] = (Parameters) ViewsHelper.findView(PARAMETER_VIEW_ID, null, false);
			}
			if (parametersView[0] != null) {
				parametersView[0].topLevelAgentChanged(GAMA.getCurrentTopLevelAgent());
				parametersView[0].updateItemValues(false, retrieveValues);
			}
		});
	}

	/**
	 * Regular update for the monitors
	 *
	 * @param scope
	 */
	@Override
	public void updateParameterView(final IScope scope) {
		if (parametersView[0] instanceof IGamaView gv) { gv.update(null); }
	}

	/**
	 * Method setSelectedAgent()
	 *
	 * @see gama.api.ui.IGui#setSelectedAgent(gama.api.kernel.agent.IAgent)
	 */
	@Override
	public void setSelectedAgent(final IAgent a) {
		WorkbenchHelper.asyncRun(() -> {
			if (WorkbenchHelper.getPage() == null || a == null) return;
			try {
				final InspectDisplayOutput output = InspectDisplayOutput.inspect(a, null);
				output.launch(a.getScope().copy("in Inspector"));
			} catch (final GamaRuntimeException g) {
				g.addContext("In opening the agent inspector");
				GAMA.reportError(GAMA.getRuntimeScope(), g, false);
			}
			final IViewReference r = WorkbenchHelper.getPage().findViewReference(IGui.AGENT_VIEW_ID, "");
			if (r != null) { WorkbenchHelper.getPage().bringToTop(r.getPart(true)); }
		});
	}

	/**
	 * Arrange experiment views.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param exp
	 *            the exp
	 * @param keepTabs
	 *            the keep tabs
	 * @param keepToolbars
	 *            the keep toolbars
	 * @param showConsoles
	 *            the show consoles
	 * @param showParameters
	 *            the show parameters
	 * @param showNavigator
	 *            the show navigator
	 * @param showControls
	 *            the show controls
	 * @param keepTray
	 *            the keep tray
	 * @param color
	 *            the color
	 * @param showEditors
	 *            the show editors
	 * @date 14 août 2023
	 */
	@Override
	public void arrangeExperimentViews(final IScope scope, final IExperimentSpecies exp, final Boolean keepTabs,
			final Boolean keepToolbars, final Boolean showConsoles, final Boolean showParameters,
			final Boolean showNavigator, final Boolean showControls, final Boolean keepTray,
			final Supplier<IColor> color, final boolean showEditors) {

		WorkbenchHelper.setWorkbenchWindowTitle(exp.getName() + " - " + exp.getModel().getFilePath());
		// Capture all view-arrangement work as a Runnable rather than scheduling it as a UIJob.
		// openAndApplyLayout() will drain and execute this inside its single setRedraw(false/true)
		// syncExec, so console, navigator, toolbar and display views all appear in one paint.
		pendingArrange = () -> {
			WorkbenchHelper.getWindow().updateActionBars();
			// To solve issue #3697
			ICommandService hs = WorkbenchHelper.getService(ICommandService.class);
			hs.refreshElements("gama.ui.application.commands.SynchronizeExperiment", null);
			WorkbenchHelper.getPage().setEditorAreaVisible(showEditors);
			getConsole().toggleConsoleViews(exp.getAgent(), showConsoles == null || showConsoles);
			if (showNavigator != null && !showNavigator) { hideView(IGui.NAVIGATOR_VIEW_ID); }
			if (showControls != null) { WorkbenchHelper.getWindow().setCoolBarVisible(showControls); }
			if (keepTray != null) { PerspectiveHelper.showBottomTray(WorkbenchHelper.getWindow(), keepTray); }
			final SimulationPerspectiveDescriptor sd = PerspectiveHelper.getActiveSimulationPerspective();
			if (sd != null) {
				sd.showParameters(showParameters == null || showParameters);
				sd.showConsoles(showConsoles == null || showConsoles);
				sd.keepTabs(keepTabs);
				sd.keepToolbars(keepToolbars);
				sd.keepControls(showControls);
				sd.keepTray(keepTray);
				sd.setBackground(() -> {
					IColor c = color.get();
					return c == null ? null : GamaColors.toSwtColor(c);
				});
			}
		};
	}

	/**
	 * The overlay shown between the perspective switch and the display layout. {@code null} when no experiment is being
	 * launched.
	 */
	private volatile LaunchingOverlay launchingOverlay;

	@Override
	public void showLaunchingOverlay(final String perspectiveId) {
		final Shell parent = WorkbenchHelper.getWindow() != null ? WorkbenchHelper.getWindow().getShell() : null;
		if (parent == null || parent.isDisposed()) return;
		// Extract model and experiment names from the perspective id:
		// format is PERSPECTIVE_SIMULATION_FRAGMENT + ":" + modelName + ":" + experimentName
		final String[] parts = perspectiveId == null ? new String[0] : perspectiveId.split(":", 3);
		final String modelName = parts.length > 1 ? parts[1] : "";
		final String expName = parts.length > 2 ? parts[2] : "";
		launchingOverlay = new LaunchingOverlay(parent, modelName, expName, getConsole(), getStatus(), null);
		launchingOverlay.show();
	}

	@Override
	public void openErrorView() {
		// Kept for backward compatibility / external callers. The main error-display
		// flow now goes through displayErrors(), which opens the view atomically on the
		// UI thread when there are exceptions to show.
		WorkbenchHelper.asyncRun(() -> showView(null, ERROR_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE));
	}

	@Override
	public void displayLatestErrors() {
		final IRuntimeExceptionHandler handler = getRuntimeExceptionHandler();
		if (handler == null) return;
		// Open (or bring to front) the ErrorView so the user sees it immediately,
		// then flush any pending exceptions from the handler's incoming queue into
		// cleanExceptions via handler.displayLatestErrors() → updateUI() → displayErrors(),
		// which will also refresh the view contents on the UI thread.
		WorkbenchHelper.run(() -> {
			final IGamaView.Error v =
					(IGamaView.Error) showView(null, ERROR_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
			if (v != null) { v.displayErrors(true); }
		});
		handler.displayLatestErrors();
	}

	@Override
	public void hideLaunchingOverlay() {
		final LaunchingOverlay overlay = launchingOverlay;
		launchingOverlay = null;
		if (overlay != null) { overlay.hide(); }
	}

	/**
	 * Method cleanAfterExperiment()
	 */
	@Override
	public void cleanAfterExperiment() {
		pendingArrange = null;
		WorkbenchHelper.asyncRun(this::hideLaunchingOverlay);
		hideParameters();
		final IGamaView m = (IGamaView) ViewsHelper.findView(MONITOR_VIEW_ID, null, false);
		if (m != null) {
			m.reset();
			hideView(MONITOR_VIEW_ID);
		}
		if (!GamaPreferences.Interface.CORE_CONSOLE_KEEP.getValue()) { getConsole().eraseConsole(true); }
		final IGamaView icv = (IGamaView) ViewsHelper.findView(INTERACTIVE_CONSOLE_VIEW_ID, null, false);
		if (icv != null) { icv.reset(); }
		// Do NOT call handler.stop() here. The handler auto-stops after remainingTime
		// (5 s idle) via its own run() loop. Stopping it here races with
		// displayLatestErrors() on the init-failure path and wipes cleanExceptions
		// before the Errors view has been populated.
	}

	@Override
	public void hideParameters() {
		hideView(PARAMETER_VIEW_ID);
		parametersView[0] = null;
	}

	/**
	 * Gets the runtime exception handler.
	 *
	 * @return the runtime exception handler
	 */
	private IRuntimeExceptionHandler getRuntimeExceptionHandler() {
		return WorkbenchHelper.getService(IRuntimeExceptionHandler.class);
	}

	/**
	 * Method updateSpeedDisplay()
	 *
	 * @see gama.api.ui.IGui#updateSpeedDisplay(java.lang.Double)
	 */
	@Override
	public void updateSpeedDisplay(final IScope scope, final Double minimumCycleDuration,
			final Double maximumCycleDuration, final boolean notify) {
		final ISpeedDisplayer speedStatus = WorkbenchHelper.getService(ISpeedDisplayer.class);
		if (speedStatus != null) {
			WorkbenchHelper.asyncRun(() -> {
				speedStatus.setMaximum(maximumCycleDuration);
				speedStatus.setInit(minimumCycleDuration, notify);
			});

		}
	}

	@Override
	public void closeSimulationViews(final IScope scope, final boolean openModelingPerspective,
			final boolean immediately) {
		WorkbenchHelper.run(() -> {
			final IWorkbenchPage page = WorkbenchHelper.getPage();
			final IViewReference[] views = page.getViewReferences();

			for (final IViewReference view : views) {
				final IViewPart part = view.getView(false);
				if (part instanceof IGamaView gv) {
					if (part instanceof Console && GamaPreferences.Interface.CORE_CONSOLE_KEEP.getValue()) { continue; }
					gv.close(scope);
				}
			}
			if (openModelingPerspective) {
				DEBUG.OUT("Deleting simulation perspective and opening immediately the modeling perspective = "
						+ immediately);
				PerspectiveHelper.deleteCurrentSimulationPerspective();
				PerspectiveHelper.openModelingPerspective(immediately, false);
			}

			// getStatus().neutralStatus("No simulation running");
		});

	}

	@Override
	public void updateViewTitle(final IOutput out, final ISimulationAgent agent) {
		// Use asyncRun instead of run() to avoid blocking the command thread with a syncExec
		// while the UI thread is busy processing UIJobs (display openers, layout).
		WorkbenchHelper.asyncRun(() -> {
			final IViewPart part = ViewsHelper.findView(out.getViewId(), out.isUnique() ? null : out.getName(), true);
			if (part instanceof IGamaView) { ((IGamaView) part).changePartNameWithSimulation(agent); }
		});

	}

	@Override
	public IStatusDisplayer getStatus() { return WorkbenchHelper.getService(IStatusDisplayer.class); }

	@Override
	public IConsoleListener getConsole() {
		if (console == null) {
			console = new CompositeConsoleListener();
			console.addConsoleListener(WorkbenchHelper.getService(IConsoleListener.class));
		}
		return console;
	}

	/**
	 * Gets the progress indicator.
	 *
	 * @param scope
	 *            the scope
	 * @param taskName
	 *            the task name
	 * @return the progress indicator
	 */
	@Override
	public IProgressIndicator getProgressIndicator(final IScope scope, final String taskName) {
		return new ProgressIndicator(scope, taskName);
	}

	/**
	 * Gets the dialog factory.
	 *
	 * @return the dialog factory
	 */
	@Override
	public IDialogFactory getDialogFactory() {
		if (dialogFactory == null) { dialogFactory = WorkbenchHelper.getService(IDialogFactory.class); }
		return dialogFactory;
	}

	@Override
	public void run(final String taskName, final Runnable r, final boolean asynchronous) {
		if (asynchronous) {
			WorkbenchHelper.runInUI(taskName, 0, m -> r.run());
		} else {
			WorkbenchHelper.run(r);
		}
	}

	@Override
	public void setFocusOn(final IShape shape) {
		if (shape == null) return;
		for (final IDisplaySurface surface : ViewsHelper.allDisplaySurfaces()) {
			if (shape instanceof ITopLevelAgent) {
				surface.zoomFit();
			} else {
				surface.focusOn(shape);
			}
		}
		GAMA.getExperiment().refreshAllOutputs();
	}

	@Override
	public void applyLayout(final IScope scope, final Object layout) {
		final IDisplayLayoutManager manager = WorkbenchHelper.getService(IDisplayLayoutManager.class);
		if (manager != null) { manager.applyLayout(layout); }
	}

	/**
	 * Opens all display outputs and applies the layout in a single synchronous UI call under
	 * {@code shell.setRedraw(false)}, eliminating every intermediate paint that would otherwise occur between
	 * individual view-opener UIJobs.
	 *
	 * <p>
	 * The command thread calls this method via a {@code syncExec}: the UI thread is frozen for the entire duration of
	 * {@code openAll.run()} + layout application, so the user sees only the final state.
	 * </p>
	 *
	 * @param scope
	 *            the current scope
	 * @param openAll
	 *            runnable that calls {@link gama.core.outputs.AbstractOutput#open()} for every display output
	 * @param layout
	 *            the layout object forwarded to {@link IDisplayLayoutManager#applyLayout(Object)}
	 */
	@Override
	public void openAndApplyLayout(final IScope scope, final Runnable openAll, final Object layout) {
		final IDisplayLayoutManager manager = WorkbenchHelper.getService(IDisplayLayoutManager.class);
		if (manager == null) {
			// No layout manager (headless): fall back to simple sequential open + layout.
			pendingArrange = null;
			openAll.run();
			applyLayout(scope, layout);
			return;
		}
		// Run arrange + open + layout in one syncExec so no OS paint events occur between them.
		// applyLayoutNow() → ArrangeDisplayViews.execute() wraps the E4 mutations in setRedraw(false/true).
		final Runnable arrange = pendingArrange;
		pendingArrange = null;
		WorkbenchHelper.run(() -> {
			if (arrange != null) { arrange.run(); }
			openAll.run();
			manager.applyLayoutNow(layout);
			// Defer overlay close to AFTER this syncExec returns.
			// Closing an ON_TOP Shell on macOS re-enters the Cocoa event loop synchronously,
			// dispatching any queued OS events (including the synthetic ESC KeyDown that macOS
			// injects when the fullscreen ON_TOP shell becomes visible). If we close the overlay
			// here, that synthetic ESC fires while we are still inside the syncExec and calls
			// toggleFullScreen() a second time — exiting fullscreen immediately after entering it.
			WorkbenchHelper.asyncRun(this::hideLaunchingOverlay);
		});
	}

	@Override
	public IPoint getMouseLocationInModel() { return mouseLocationInModel; }

	@Override
	public IPoint getMouseLocationInDisplay() { return mouseLocationInDisplay; }

	@Override
	public void setMouseLocationInModel(final IPoint location) { mouseLocationInModel = location; }

	@Override
	public void setMouseLocationInDisplay(final IPoint location) { mouseLocationInDisplay = location; }

	@Override
	public void exit() {
		WorkbenchHelper.close();
	}

	@Override
	public void refreshNavigator() {
		final IRefreshHandler refresh = WorkbenchHelper.getService(IRefreshHandler.class);
		if (refresh != null) { refresh.completeRefresh(null); }

	}

	@Override
	public boolean isInDisplayThread() { return EventQueue.isDispatchThread() || Display.getCurrent() != null; }

	@Override
	public boolean isHiDPI() {
		int zoom = WorkbenchHelper.run(() -> WorkbenchHelper.getDisplay().getPrimaryMonitor().getZoom());
		return zoom > 100;
	}

	@Override
	public void openFile(final URI uri) {
		FileOpener.openFile(uri);
	}

	@Override
	public Map<String, ISocketCommand> getServerCommands() { return CommandExecutor.getDefaultCommands(); }

}
