/*******************************************************************************************************
 *
 * UserStatusMessage.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.common;

import gama.core.common.interfaces.IGui;
import gama.core.util.GamaColor;

/**
 * Class UserStatusMessage.
 *
 * @author drogoul
 * @since 11 mars 2015
 *
 */
public class UserStatusMessage extends StatusMessage {

	/** The color. */
	GamaColor color;

	/**
	 * @param msg
	 * @param color
	 */
	public UserStatusMessage(final String msg, final GamaColor color, final String icon) {
		super(msg, IGui.USER);
		this.color = color;
		this.icon = icon;
	}

	@Override
	public GamaColor getColor() {
		return color;
	}

	@Override
	public String getIcon() {
		return icon;
	}

}
