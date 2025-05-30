/*******************************************************************************************************
 *
 * GlobalToolbarItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolItem;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public abstract class GlobalToolbarItem {

	/** The Constant SPACER. */
	static final String SPACER = "spacer";

	/** The item. */
	protected final ToolItem item;

	/** The saved tooltip. */
	String savedTooltip;

	/** The saved image. */
	String savedImage;

	/**
	 * Instantiates a new global toolbar item.
	 *
	 * @param toolbar
	 *            the toolbar
	 */
	GlobalToolbarItem(final GamaToolbarSimple toolbar) {
		item = createItem(toolbar);
	}

	/**
	 * @param toolbar
	 * @return
	 */
	protected abstract ToolItem createItem(GamaToolbarSimple toolbar);

	/**
	 * Update.
	 */
	public abstract void update();

	/**
	 * Reinit. A new simulation / experiment is launching. Changes any state.
	 */
	public abstract void reinit();

	/**
	 * @param item
	 * @param string
	 */
	protected void setTooltip(final String string) {
		if (!item.isDisposed() && string != null) { item.setToolTipText(string); }
	}

	/**
	 * @param item
	 * @param string
	 */
	protected void setImage(final String string) {
		if (!SPACER.equals(string)) { savedImage = string; }
		Image im = GamaIcon.named(string).image();
		if (!item.isDisposed()) {
			item.setImage(im);
			item.setDisabledImage(GamaIcon.named(string).disabled());
		}
	}

	/**
	 * Hide.
	 */
	protected void hide() {
		if (!item.isDisposed()) {
			setImage(SPACER);
			item.setEnabled(false);
			savedTooltip = item.getToolTipText();
		}
	}

	/**
	 * Show.
	 */
	protected void show() {
		if (!item.isDisposed()) {
			if (savedImage != null) { setImage(savedImage); }
			item.setEnabled(true);
			setTooltip(savedTooltip);
		}
	}

	/**
	 * @return
	 */
	public int getDefaultWidth() { return 25; }

	/**
	 * Sets the width.
	 *
	 * @param i
	 *            the new width
	 */
	public void setWidth(final int i) {
		item.setWidth(i);
	}

}
