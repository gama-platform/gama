/*******************************************************************************************************
 *
 * LayeredDisplayDecorator.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.views.displays;

import static gama.ui.shared.bindings.GamaKeyBindings.COMMAND;
import static gama.ui.shared.bindings.GamaKeyBindings.format;
import static gama.ui.shared.resources.IGamaIcons.DISPLAY_FULLSCREEN_ENTER;
import static gama.ui.shared.resources.IGamaIcons.DISPLAY_FULLSCREEN_EXIT;
import static gama.ui.shared.resources.IGamaIcons.DISPLAY_TOOLBAR_SNAPSHOT;
import static gama.ui.shared.resources.IGamaIcons.EXPERIMENT_RUN;
import static gama.ui.shared.resources.IGamaIcons.TOGGLE_ANTIALIAS;
import static gama.ui.shared.resources.IGamaIcons.TOGGLE_OVERLAY;

import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveListener;

import gama.api.GAMA;
import gama.api.kernel.simulation.IExperimentStateListener;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.ui.displays.IDisplayData.Changes;
import gama.api.ui.displays.IDisplayData.DisplayDataListener;
import gama.api.utils.interfaces.IDisposable;
import gama.dev.DEBUG;
import gama.dev.STRINGS;
import gama.ui.experiment.controls.SimulationSpeedContributionItem;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.menus.GamaColorMenu;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.ViewsHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;
import gama.ui.shared.views.toolbar.Selector;

/**
 * The Class LayeredDisplayDecorator.
 */
public class LayeredDisplayDecorator implements DisplayDataListener, IExperimentStateListener {

	static {
		DEBUG.ON();
	}

	/** The key and mouse listener. */
	protected IDisposable keyAndMouseListener;

	/** The menu manager. */
	protected DisplaySurfaceMenu menuManager;

	/** The view. */
	public final LayeredDisplayView view;

	/** The fs. */
	ToolItem fs = null;

	/** The normal parent of full screen control. */
	protected Composite normalParentOfFullScreenControl, normalParentOfToolbar;

	/** The full screen shell. */
	protected Shell fullScreenShell;

	/**
	 * Guard against re-entrant or race-condition calls to {@link #toggleFullScreen()}. Set to {@code true} at the start
	 * of a transition and cleared at the end, so that any second call arriving while the transition is in progress
	 * (e.g. a UIJob queued by an E4 part-activation event) is silently ignored.
	 */
	private volatile boolean inFullScreenTransition;

	/**
	 * Timestamp (ms) recorded the last time fullscreen was <em>entered</em>. Used by
	 * {@link #fullScreenEnteredRecently()} to debounce synthetic ESC {@code SWT.KeyDown} events that macOS injects when
	 * a new {@code ON_TOP} shell becomes visible — these arrive after {@code inFullScreenTransition} is already
	 * {@code false} and would otherwise immediately exit fullscreen.
	 */
	private volatile long lastFullScreenEnterTime = 0;

	/** The overlay. */
	public DisplayOverlay overlay;

	/** The toolbar. */
	public GamaToolbar2 toolbar;

	/** The interactive console visible. */
	private volatile boolean isOverlayTemporaryVisible;

	/** The perspective listener. */
	private final IPerspectiveListener perspectiveListener = new LayeredDisplayPerspectiveListener(this);

	/** The overlay listener. */
	private final IPartListener2 overlayListener = new LayeredDisplayPartListener(this);

	/** The relaunch experiment. */
	GamaCommand toggleOverlay, takeSnapshot, antiAlias, toggleFullScreen, runExperiment, stepExperiment,
			closeExperiment, relaunchExperiment;

	/**
	 * Instantiates a new layered display decorator.
	 *
	 * @param view
	 *            the view
	 */
	LayeredDisplayDecorator(final LayeredDisplayView view) {
		this.view = view;
		createCommands();
	}

	/**
	 * Creates the commands.
	 *
	 * @param view
	 *            the view
	 */
	private void createCommands() {
		int pad = 25;
		toggleOverlay = new GamaCommand(TOGGLE_OVERLAY, STRINGS.PAD("Toggle overlay", pad) + format(COMMAND, 'O'),
				e -> toggleOverlay());
		takeSnapshot = new GamaCommand(DISPLAY_TOOLBAR_SNAPSHOT, STRINGS.PAD("Take a snapshot", pad),
				e -> view.takeSnapshot(null));
		antiAlias = new GamaCommand(TOGGLE_ANTIALIAS, STRINGS.PAD("Turn antialias on/off", pad), e -> {
			view.getOutput().getData().setAntialias(!view.getOutput().getData().isAntialias());
			view.update(view.getOutput());
		});
		toggleFullScreen =
				new GamaCommand(DISPLAY_FULLSCREEN_ENTER, STRINGS.PAD("Toggle fullscreen", pad) + "ESC", e -> {
					toggleFullScreen();
				});
		runExperiment = new GamaCommand(EXPERIMENT_RUN,
				STRINGS.PAD("Run or pause experiment", pad) + GamaKeyBindings.PLAY_STRING, e -> {

					final Item item = (Item) e.widget;
					if (!GAMA.isPaused()) {
						item.setImage(GamaIcon.named(EXPERIMENT_RUN).image());
					} else {
						item.setImage(GamaIcon.named(IGamaIcons.MENU_PAUSE_ACTION).image());
					}
					GAMA.startPauseFrontmostExperiment(false);

				});
		stepExperiment = new GamaCommand(IGamaIcons.EXPERIMENT_STEP,
				STRINGS.PAD("Step experiment", pad) + GamaKeyBindings.STEP_STRING,
				e -> GAMA.stepFrontmostExperiment(false));
		closeExperiment = new GamaCommand(IGamaIcons.EXPERIMENT_STOP,
				STRINGS.PAD("Closes experiment", pad) + GamaKeyBindings.QUIT_STRING,
				e -> new Thread(() -> GAMA.closeAllExperiments(true, false)).start());
		relaunchExperiment = new GamaCommand(IGamaIcons.EXPERIMENT_RELOAD,
				STRINGS.PAD("Reload experiment", pad) + GamaKeyBindings.RELOAD_STRING,
				e -> GAMA.reloadFrontmostExperiment(false));
	}

	/** The enter full screen. */
	public final GamaCommand enterFullScreen =
			new GamaCommand(DISPLAY_FULLSCREEN_ENTER, STRINGS.PAD("Enter fullscreen", 25) + "ESC", e -> {
				toggleFullScreen();
			});

	/** The exit full screen. */
	public final GamaCommand exitFullScreen =
			new GamaCommand(DISPLAY_FULLSCREEN_EXIT, STRINGS.PAD("Exit fullscreen", 25) + "ESC", e -> {
				toggleFullScreen();
			});

	/**
	 * Toggle full screen.
	 */
	public void toggleFullScreen() {
		if (inFullScreenTransition) return;
		inFullScreenTransition = true;
		try {
			if (isFullScreen()) {
				DEBUG.OUT("Is already full screen: exiting");
				fs.setImage(GamaIcon.named(DISPLAY_FULLSCREEN_ENTER).image());
				fs.setToolTipText(STRINGS.PAD("Enter fullscreen", 25) + "ESC");
				toggleFullScreen = enterFullScreen;
				// Toolbar
				if (!toolbar.isDisposed()) {
					toolbar.wipe(SWT.LEFT, true);
					toolbar.setParent(normalParentOfToolbar);
					normalParentOfToolbar.requestLayout();
				}
				runExperimentItem = null;
				view.getCentralPanel().setParent(normalParentOfFullScreenControl);
				createOverlay();
				normalParentOfFullScreenControl.requestLayout();
				destroyFullScreenShell();
			} else {
				DEBUG.OUT("Is not full screen: entering");
				fullScreenShell = createFullScreenShell();
				if (fullScreenShell == null) return;
				// Activate AFTER setting fullScreenShell so that ok() sees isFullScreen()=true
				// and the overlayListener.partActivated UIJob is not queued. Previously, activate()
				// was called before createFullScreenShell(), meaning isFullScreen() was still false
				// when partActivated fired — causing an extra showCanvas UIJob that could race with
				// (and undo) the fullscreen transition when called during decorateDisplays().
				ViewsHelper.activate(view);
				fs.setImage(GamaIcon.named(DISPLAY_FULLSCREEN_EXIT).image());
				fs.setToolTipText(STRINGS.PAD("Exit fullscreen", 25) + "ESC");
				toggleFullScreen = exitFullScreen;
				normalParentOfFullScreenControl = view.getCentralPanel().getParent();
				view.getCentralPanel().setParent(fullScreenShell);
				fullScreenShell.layout(true, true);
				fullScreenShell.setVisible(true);
				lastFullScreenEnterTime = System.currentTimeMillis();
				createOverlay();
				// Toolbar
				if (!toolbar.isDisposed()) {
					toolbar.wipe(SWT.LEFT, true);
					addFullscreenToolbarCommands();
					normalParentOfToolbar = toolbar.getParent();
					toolbar.setParent(fullScreenShell);
				}
			}
			if (!toolbar.isDisposed()) {
				toolbar.wipe(SWT.RIGHT, true);
				GamaToolbarFactory.buildToolbar(view, toolbar);
				toolbar.requestLayout();
			}
			if (overlay.isVisible()) {
				WorkbenchHelper.runInUI("Display overlay", 50, m -> {
					toggleOverlay();
					toggleOverlay();
				});
			}
			view.focusCanvas();
		} finally {
			inFullScreenTransition = false;
		}
	}

	/**
	 * Toggle toolbar.
	 */
	public void toggleToolbar() {
		// If in fullscreen the hierarchy is simplified
		if (isFullScreen()) {
			boolean visible = toolbar.isVisible();
			toolbar.setVisible(!visible);
			((GridData) toolbar.getLayoutData()).exclude = visible;
		} else if (toolbar.isVisible()) {
			toolbar.hide();
		} else {
			toolbar.show();
		}
		toolbar.getParent().requestLayout();
	}

	/** The run experiment item. */
	ToolItem runExperimentItem = null;

	/**
	 * Adds the fullscreen toolbar commands.
	 */
	public void addFullscreenToolbarCommands() {
		toolbar.button(toggleOverlay, SWT.LEFT);
		toolbar.sep(GamaToolbarFactory.TOOLBAR_SEP, SWT.LEFT);
		runExperimentItem = toolbar.button(runExperiment, SWT.LEFT);
		if (GAMA.isPaused()) {
			runExperimentItem.setImage(GamaIcon.named(IGamaIcons.EXPERIMENT_RUN).image());
		} else {
			runExperimentItem.setImage(GamaIcon.named(IGamaIcons.MENU_PAUSE_ACTION).image());
		}
		toolbar.button(stepExperiment, SWT.LEFT);
		toolbar.control(SimulationSpeedContributionItem.create(toolbar.getToolbar(SWT.LEFT)),
				SimulationSpeedContributionItem.totalWidth(), SWT.LEFT);
		toolbar.button(relaunchExperiment, SWT.LEFT);
		toolbar.button(closeExperiment, SWT.LEFT);
	}

	/**
	 * Creates the overlay.
	 */
	public void createOverlay() {
		boolean wasVisible = false;
		if (overlay != null) {
			wasVisible = overlay.isVisible();
			overlay.dispose();
		}
		overlay = new DisplayOverlay(view, view.surfaceComposite);
		if (wasVisible) { overlay.setVisible(true); }

		if (overlay.isVisible()) {
			overlay.relocate();
			overlay.update();
		}
	}

	/**
	 * Creates the decorations.
	 *
	 * @param form
	 *            the form
	 */
	public void createDecorations() {
		createOverlay();
		addListeners();
		keyAndMouseListener = view.getMultiListener();
		menuManager = new DisplaySurfaceMenu(view.getDisplaySurface(), view.getParentComposite(), presentationMenu());
		// Run synchronously — createDecorations() is always called on the UI thread from ownCreatePartControl.
		// Scheduling a UIJob here used to fire an extra per-display layout pass after the main layout job,
		// causing visible toolbar-area reflows.
		if (view.getOutput().getData().isToolbarVisible()) {
			toolbar.show();
		} else {
			toolbar.hide();
		}
	}

	/**
	 *
	 */
	private void addListeners() {
		GAMA.addExperimentStateListener(this);
		WorkbenchHelper.getWindow().addPerspectiveListener(perspectiveListener);
		WorkbenchHelper.getPage().addPartListener(overlayListener);
	}

	/**
	 * Removes the listeners.
	 */
	private void removeListeners() {
		GAMA.removeExperimentStateListener(this);
		WorkbenchHelper.getWindow().removePerspectiveListener(perspectiveListener);
		WorkbenchHelper.getPage().removePartListener(overlayListener);
	}

	/**
	 * Checks if is full screen.
	 *
	 * @return true, if is full screen
	 */
	public boolean isFullScreen() { return fullScreenShell != null; }

	/**
	 * Returns {@code true} if fullscreen was entered within the last 500 ms. Used by
	 * {@link gama.ui.shared.utils.ViewsHelper#toggleFullScreenMode(IGamaView.Display)} to suppress the synthetic
	 * {@code SWT.KeyDown / ESC} event that macOS injects when a new {@code ON_TOP} shell becomes visible — this
	 * synthetic event arrives after {@link #inFullScreenTransition} has already been cleared and would otherwise
	 * immediately exit fullscreen.
	 *
	 * @return {@code true} if less than 500 ms have elapsed since fullscreen was last entered
	 */
	public boolean fullScreenEnteredRecently() {
		return System.currentTimeMillis() - lastFullScreenEnterTime < 500;
	}

	/**
	 * Creates the full screen shell.
	 *
	 * @return the shell
	 */
	private Shell createFullScreenShell() {
		final int monitorId = view.getOutput().getData().fullScreen();
		final Monitor[] monitors = WorkbenchHelper.getMonitors();
		int monitorId1 = Math.min(monitors.length - 1, Math.max(0, monitorId));
		final Rectangle bounds = monitors[monitorId1].getBounds();
		if (ViewsHelper.registerFullScreenView(monitorId1, view)) {
			final Shell shell = new Shell(WorkbenchHelper.getDisplay(), SWT.NO_TRIM | SWT.ON_TOP);
			shell.setBounds(bounds);
			// For DEBUG purposes only:
			// fullScreenShell.setBounds(new Rectangle(0, 0, bounds.width / 2, bounds.height / 2));
			shell.setLayout(shellLayout());
			return shell;
		}
		return null;
	}

	/**
	 * Destroy full screen shell.
	 */
	private void destroyFullScreenShell() {
		if (fullScreenShell == null || fullScreenShell.isDisposed()) return;
		DEBUG.OUT("Destroying full screen shell");
		WorkbenchHelper.asyncRun(() -> {
			if (!fullScreenShell.isDisposed()) {
				fullScreenShell.close();
				fullScreenShell.dispose();
				fullScreenShell = null;
			}
			ViewsHelper.unregisterFullScreenView(view);
			ViewsHelper.activate(view);
		});

	}

	/**
	 * Shell layout.
	 *
	 * @return the layout used for the contents of the shell
	 */
	private GridLayout shellLayout() {
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		final int margin = 0; // REDUCED_VIEW_TOOLBAR_HEIGHT.getValue() ? -1 : 0;
		layout.marginTop = margin;
		layout.marginBottom = margin;
		layout.marginHeight = margin;
		return layout;
	}

	/** The display overlay. */
	protected Runnable displayOverlay = () -> {
		if (overlay == null) return;
		updateOverlay();
	};

	/**
	 * Update overlay.
	 */
	protected void updateOverlay() {
		if (overlay == null) return;
		if (view.forceOverlayVisibility()) {
			if (!overlay.isVisible()) {
				isOverlayTemporaryVisible = true;
				overlay.setVisible(true);
			}
		} else if (isOverlayTemporaryVisible) {
			isOverlayTemporaryVisible = false;
			overlay.setVisible(false);
		}
		if (overlay.isVisible()) { overlay.update(); }

	}

	/**
	 * Toggle overlay.
	 */
	public void toggleOverlay() {
		overlay.setVisible(!overlay.isVisible());
	}

	/**
	 * Presentation menu.
	 *
	 * @return the menu manager
	 */
	private Function<Menu, Menu> presentationMenu() {

		return parentMenu -> {
			Menu sub = GamaMenu.sub(parentMenu, "Presentation", "", IGamaIcons.PRESENTATION_MENU);
			if (!isFullScreen() && WorkbenchHelper.getNumberOfMonitors() > 1) {
				Menu mon = GamaMenu.sub(sub, "Enter fullscreen", "", DISPLAY_FULLSCREEN_ENTER);
				Monitor[] mm = WorkbenchHelper.getMonitors();
				for (int i = 0; i < mm.length; i++) {
					Monitor monitor = mm[i];
					Rectangle bounds = monitor.getBounds();
					String text = "Monitor " + i + " (" + bounds.width + "x" + bounds.height + ")";
					final int monitorId = i;
					GamaMenu.action(mon, text, e -> {
						view.getOutput().getData().setFullScreen(monitorId);
						toggleFullScreen();
					});
				}
			} else {
				toggleFullScreen.toItem(sub);
			}
			toggleOverlay.toItem(sub);
			GamaMenu.action(sub,
					STRINGS.PAD("Toggle toolbar ", 25) + GamaKeyBindings.format(GamaKeyBindings.COMMAND, 'T'),
					t -> toggleToolbar(),
					this.isFullScreen() ? "display/toolbar.fullscreen" : "display/toolbar.regular");
			GamaColorMenu.addColorSubmenuTo(sub, STRINGS.PAD("Background", 25), c -> {
				view.getDisplaySurface().getData().setBackgroundColor(c);
				view.getDisplaySurface().updateDisplay(true);
			});
			return sub;
		};

	}

	/**
	 * Creates the tool items.
	 *
	 * @param tb
	 *            the tb
	 */
	public void createToolItems(final GamaToolbar2 tb) {
		toolbar = tb;
		tb.setBackgroundColor(GamaColors.get(view.getOutput().getData().getToolbarColor()).color());
		tb.sep(GamaToolbarFactory.TOOLBAR_SEP, SWT.RIGHT);
		tb.button(takeSnapshot, SWT.RIGHT);
		ToolItem item = tb.check(antiAlias, SWT.RIGHT);
		tb.setSelection(item, view.getOutput().getData().isAntialias());
		if (!isFullScreen() && WorkbenchHelper.getNumberOfMonitors() > 1) {
			fs = tb.menuItem(DISPLAY_FULLSCREEN_ENTER, "", "Enter fullscreen", e -> {
				final GamaMenu menu = new GamaMenu() {

					@Override
					protected void fillMenu() {
						Monitor[] monitors = WorkbenchHelper.getMonitors();
						for (int i = 0; i < monitors.length; i++) {
							Rectangle bounds = monitors[i].getBounds();
							String text = "Enter fullscreen on monitor " + i + " (" + bounds.width + "x" + bounds.height
									+ ")";
							final int monitorId = i;

							action(text, (Selector) e -> {
								view.getOutput().getData().setFullScreen(monitorId);
								toggleFullScreen();
							});
						}
					}

				};
				menu.open(toolbar.getToolbar(SWT.RIGHT), e);
			}, SWT.RIGHT);

		} else {
			fs = tb.button(toggleFullScreen, SWT.RIGHT);
		}
		tb.sep(GamaToolbarFactory.TOOLBAR_SEP, SWT.RIGHT);
		tb.menuItem(IGamaIcons.LAYERS_MENU, "Browse displayed agents by layers", "Properties and contents of layers",
				trigger -> menuManager.buildToolbarMenu(trigger, (ToolItem) trigger.widget), SWT.RIGHT);

	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		try {
			removeListeners();
		} catch (final Exception e) {}
		if (keyAndMouseListener != null) {
			keyAndMouseListener.dispose();
			keyAndMouseListener = null;
		}
		if (overlay != null) {
			overlay.close();
			overlay = null;
		}

		if (menuManager != null) {
			menuManager.disposeMenu();
			menuManager = null;
		}
		if (toolbar != null && !toolbar.isDisposed()) {
			toolbar.dispose();
			toolbar = null;
		}

		fs = null;
		normalParentOfToolbar = null;
		normalParentOfFullScreenControl = null;
		if (fullScreenShell != null && !fullScreenShell.isDisposed()) {
			fullScreenShell.dispose();
			fullScreenShell = null;
		}

	}

	/**
	 * Changed.
	 *
	 * @param changes
	 *            the changes
	 * @param value
	 *            the value
	 */
	@Override
	public void changed(final Changes changes, final Object value) {
		switch (changes) {
			case ZOOM:
				WorkbenchHelper.asyncRun(this::updateOverlay);
				break;
			default:
				break;
		}

	}

	/**
	 * Update state to.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param experiment
	 *            the experiment
	 * @param state
	 *            the state
	 * @date 26 oct. 2023
	 */
	@Override
	public void updateStateTo(final IExperimentSpecies experiment, final State state) {
		if (!isFullScreen() || toolbar == null || !toolbar.isVisible()) return;

		if (IExperimentStateListener.State.PAUSED.name().equals(state.name())) {
			WorkbenchHelper.asyncRun(() -> {
				runExperimentItem.setImage(GamaIcon.named(IGamaIcons.EXPERIMENT_RUN).image());
				toolbar.update();
			});
		} else if (IExperimentStateListener.State.RUNNING.name().equals(state.name())) {
			WorkbenchHelper.asyncRun(() -> {
				runExperimentItem.setImage(GamaIcon.named(IGamaIcons.MENU_PAUSE_ACTION).image());
				toolbar.update();
			});
		}
	}

}
