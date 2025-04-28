/*******************************************************************************************************
 *
 * StatusIconProvider.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls;

import org.eclipse.swt.graphics.Image;

import gama.core.common.IStatusMessage;
import gama.ui.shared.resources.GamaIcon;

/**
 * A simple class that returns a different progress icon every time getIcon() or getImage() is called
 */
public class StatusIconProvider {

	/** The progress. */
	private double progress = 1d;

	/**
	 * Gets the icon.
	 *
	 * @param icon
	 *            the icon
	 * @return the icon
	 */
	public String getIcon() {
		if (progress > 6) { progress = 1; }
		progress += 0.3;
		return IStatusMessage.PROGRESS_ICON + Math.round(progress);
	}

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public Image getImage() { return GamaIcon.named(getIcon()).image(); }

}
