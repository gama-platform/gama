/*******************************************************************************************************
 *
 * FrequencyController.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;

import gama.core.outputs.IOutput;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.IToolbarDecoratedView.StateListener;

/**
 * The class FrequencyController.
 *
 * @author drogoul
 * @since 19 janv. 2012
 *
 */
public class FrequencyController implements StateListener {

	/** The view. */
	final IToolbarDecoratedView.Pausable view;

	/** The pause item. */
	ToolItem pauseItem;

	/** The internal change. */
	boolean internalChange;

	/**
	 * Instantiates a new frequency controller.
	 *
	 * @param view
	 *            the view
	 */
	public FrequencyController(final IToolbarDecoratedView.Pausable view) {
		this.view = view;
		view.addStateListener(this);
	}

	/**
	 * Toggle pause.
	 *
	 * @param item
	 *            the item
	 * @param out
	 *            the out
	 */
	void togglePause(final ToolItem item, final IOutput out) {
		if (out != null) { item.setToolTipText((out.isPaused() ? "Resume " : "Pause ") + out.getName()); }
		view.pauseChanged();
	}

	/**
	 * @param tb
	 */
	public void install(final GamaToolbar2 tb) {

		createPauseItem(tb);
		tb.sep(GamaToolbarFactory.TOOLBAR_SEP, SWT.RIGHT);
	}

	/**
	 * @param tb
	 */
	private void createPauseItem(final GamaToolbar2 tb) {

		pauseItem = tb.check(IGamaIcons.DISPLAY_TOOLBAR_PAUSE, "Pause", "Pause or resume the current view", e -> {
			final IOutput output = view.getOutput();
			if (!internalChange && output != null) {
				if (output.isPaused()) {
					output.setPaused(false);
				} else {
					output.setPaused(true);
				}
			}
			togglePause((ToolItem) e.widget, output);
		}, SWT.RIGHT);

	}

	@Override
	public void updateToReflectState() {
		if (view == null) return;
		final IOutput output = view.getOutput();
		if (output == null) return;

		WorkbenchHelper.run(() -> {
			internalChange = true;
			if (pauseItem != null && !pauseItem.isDisposed()) {
				((GamaToolbar2) pauseItem.getParent().getParent()).setSelection(pauseItem, output.isPaused());
			}
			internalChange = false;
		});

	}

}
