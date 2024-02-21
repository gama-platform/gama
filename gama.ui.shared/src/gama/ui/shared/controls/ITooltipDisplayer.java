/*******************************************************************************************************
 *
 * ITooltipDisplayer.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.controls;

import gama.ui.shared.resources.GamaColors.GamaUIColor;

/**
 * The class ITooltipDisplayer. 
 *
 * @author drogoul
 * @since 8 déc. 2014
 *
 */
public interface ITooltipDisplayer {

	/**
	 * Stop displaying tooltips.
	 */
	public abstract void stopDisplayingTooltips();

	/**
	 * Display tooltip.
	 *
	 * @param text the text
	 * @param color the color
	 */
	public abstract void displayTooltip(String text, GamaUIColor color);

}