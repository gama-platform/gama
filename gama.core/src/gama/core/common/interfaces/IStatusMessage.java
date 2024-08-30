/*******************************************************************************************************
 *
 * IStatusMessage.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import java.awt.Color;

/**
 * Class IStatusMessage.
 *
 * @author drogoul
 * @since 5 nov. 2014
 *
 */
public interface IStatusMessage extends IUpdaterMessage {

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	String getText();

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	int getCode();

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	default Color getColor() { return null; }

	/**
	 * Gets the icon.
	 *
	 * @return the icon
	 */
	String getIcon();
}
