/*******************************************************************************************************
 *
 * ExperimentToolbar.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.toolbar;

import static gama.api.utils.prefs.GamaPreferences.Modeling.EDITOR_COLLAPSE_BUTTONS;
import static gama.api.utils.prefs.GamaPreferences.Modeling.EDITOR_EXPERIMENT_MENU;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;

import gama.annotations.constants.IKeyword;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceAfterChangeListener;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.views.toolbar.GamaToolbar2;
import gaml.compiler.ui.editor.GamlEditorState;

/**
 * Shared experiment controls for text and graphical editors, independent of any editor implementation.
 * <p>
 * Construct and call this component on the SWT UI thread. Build {@link GamlEditorState} during validation,
 * before disposing descriptions, then dispatch {@link #update(GamlEditorState)} to the UI thread.
 * Callbacks run on the UI thread; launching receives the full experiment name, never its display label.
 * The host owns the toolbar and any leading items or right-hand actions. Disposing the toolbar removes preference
 * listeners and all owned controls; no separate disposal is required.
 */
public final class ExperimentToolbar {

	private static final int BUTTON_HEIGHT = 20;

	private final GamaToolbar2 toolbar;
	private final Consumer<String> launchExperiment;
	private final Runnable revalidate;
	private final Consumer<URI> openImportedError;
	private final FlatButton measuringButton;
	private final Display display;
	private final IPreferenceAfterChangeListener<Boolean> preferenceListener = value -> scheduleRefresh();
	private GamlEditorState state;
	private int leadingItemCount;
	private int renderedWidth = -1;
	private final AtomicBoolean refreshPending = new AtomicBoolean();

	/**
	 * Attach experiment controls to an existing toolbar. The launch callback owns saving and compilation;
	 * validation updates only provide snapshots and never trigger a launch.
	 */
	public ExperimentToolbar(final GamaToolbar2 toolbar, final Consumer<String> launchExperiment,
			final Runnable revalidate, final Consumer<URI> openImportedError) {
		this.toolbar = Objects.requireNonNull(toolbar);
		display = toolbar.getDisplay();
		this.launchExperiment = Objects.requireNonNull(launchExperiment);
		this.revalidate = Objects.requireNonNull(revalidate);
		this.openImportedError = Objects.requireNonNull(openImportedError);
		measuringButton = FlatButton.button(toolbar, IGamaColors.OK, "").withHeight(BUTTON_HEIGHT);
		measuringButton.setVisible(false);
		toolbar.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				if (toolbar.getAvailableLeftWidth() != renderedWidth) { scheduleRefresh(); }
			}
		});
		EDITOR_EXPERIMENT_MENU.addChangeListener(preferenceListener);
		EDITOR_COLLAPSE_BUTTONS.addChangeListener(preferenceListener);
		toolbar.addDisposeListener(e -> {
			EDITOR_EXPERIMENT_MENU.removeChangeListener(preferenceListener);
			EDITOR_COLLAPSE_BUTTONS.removeChangeListener(preferenceListener);
		});
	}

	/** Preserve this many host-owned items at the beginning of the left toolbar (default: zero). */
	public void setLeadingItemCount(final int count) {
		if (count < 0) throw new IllegalArgumentException("Negative leading item count");
		leadingItemCount = count;
	}

	/** Render a detached validation snapshot. The host must discard stale validation results before calling this. */
	public void update(final GamlEditorState snapshot) {
		state = Objects.requireNonNull(snapshot);
		refresh();
	}

	/** Re-render the current snapshot, for example after host-owned actions change width. */
	public void refresh() {
		if (toolbar.isDisposed() || state == null) return;
		toolbar.wipe(SWT.LEFT, leadingItemCount);
		renderedWidth = toolbar.getAvailableLeftWidth();
		final String message = state.getStatus();
		if (message != null && !GamlEditorState.NO_EXP_DEFINED.equals(message)) {
			final String icon = state.hasImportedErrors() ? IGamaIcons.SMALL_DROPDOWN : IGamaIcons.MARKER_ERROR;
			final ToolItem item = toolbar.button(state.getColor(), message, GamaIcon.named(icon).image(),
					e -> revalidate.run(), BUTTON_HEIGHT, SWT.LEFT);
			if (state.hasImportedErrors()) { bindImportedErrors((FlatButton) item.getControl(), state); }
		} else if (state.isShowingExperiments() && !state.experiments.isEmpty()) {
			if (EDITOR_EXPERIMENT_MENU.getValue()
					|| EDITOR_COLLAPSE_BUTTONS.getValue() && buttonsOverflow()) {
				displayExperimentMenu();
			} else {
				displayExperimentButtons();
			}
		}
		toolbar.normalizeToolbars();
		toolbar.requestLayout();
	}

	private void scheduleRefresh() {
		if (display.isDisposed() || !refreshPending.compareAndSet(false, true)) return;
		display.asyncExec(() -> {
			refreshPending.set(false);
			refresh();
		});
	}

	private boolean buttonsOverflow() {
		if (renderedWidth < 0) return false;
		int width = 0;
		final ToolItem[] items = toolbar.getToolbar(SWT.LEFT).getItems();
		for (int i = 0; i < Math.min(leadingItemCount, items.length); i++) {
			width += items[i].getWidth();
		}
		for (int i = 0; i < state.abbreviations.size(); i++) {
			measuringButton.setText(state.abbreviations.get(i));
			measuringButton.setImage(GamaIcon.named(buttonIcon(state.getExperimentTypes().get(i))).image());
			width += measuringButton.computeSize(SWT.DEFAULT, BUTTON_HEIGHT).x + 4;
		}
		return width > renderedWidth;
	}

	private void displayExperimentButtons() {
		for (int i = 0; i < state.experiments.size(); i++) {
			final String name = state.experiments.get(i);
			final String text = state.abbreviations.get(i);
			final String type = state.getExperimentTypes().get(i);
			final ToolItem item = toolbar.button(IGamaColors.OK, text, GamaIcon.named(buttonIcon(type)).image(),
					e -> launchExperiment.accept(name), BUTTON_HEIGHT, SWT.LEFT);
			final String label = IKeyword.BATCH.equals(type) ? IKeyword.BATCH
					: IKeyword.RECORD.equals(type) ? IKeyword.RECORD : "regular";
			item.setToolTipText("Executes the " + label + " experiment named '" + name + "'");
			item.setData("index", i + 1);
			item.getControl().setData("exp", name);
		}
	}

	private void displayExperimentMenu() {
		final GamlEditorState snapshot = state;
		final ToolItem item = toolbar.menuButton(IGamaColors.OK, "Run Experiment...", SWT.LEFT);
		final FlatButton button = ((FlatButton) item.getControl()).withHeight(BUTTON_HEIGHT);
		final Menu menu = new Menu(toolbar.getShell(), SWT.POP_UP);
		for (int i = 0; i < snapshot.experiments.size(); i++) {
			final String name = snapshot.experiments.get(i);
			GamaMenu.action(menu, snapshot.abbreviations.get(i), e -> launchExperiment.accept(name),
					menuIcon(snapshot.getExperimentTypes().get(i))).setData("exp", name);
		}
		button.addDisposeListener(e -> menu.dispose());
		button.setSelectionListener(e -> {
			final Point location = button.toDisplay(0, button.getSize().y);
			menu.setLocation(location);
			menu.setVisible(true);
		});
	}

	private void bindImportedErrors(final FlatButton button, final GamlEditorState snapshot) {
		final Menu menu = new Menu(toolbar.getShell(), SWT.POP_UP);
		snapshot.getImportedErrors().forEach((message, uri) ->
				GamaMenu.action(menu, message, e -> openImportedError.accept(uri), null));
		button.addDisposeListener(e -> menu.dispose());
		button.setSelectionListener(e -> {
			menu.setLocation(button.toDisplay(0, button.getSize().y));
			menu.setVisible(true);
		});
	}

	private static String buttonIcon(final String type) {
		return IKeyword.BATCH.equals(type) ? IGamaIcons.BUTTON_BATCH
				: IKeyword.RECORD.equals(type) ? IGamaIcons.BUTTON_BACK : IGamaIcons.BUTTON_GUI;
	}

	private static String menuIcon(final String type) {
		if (ThemeHelper.isDark()) return buttonIcon(type);
		return IKeyword.BATCH.equals(type) ? IGamaIcons.MENU_BATCH
				: IKeyword.RECORD.equals(type) ? IGamaIcons.MENU_BACK : IGamaIcons.MENU_GUI;
	}
}
