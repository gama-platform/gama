/*******************************************************************************************************
 *
 * IBoxEnabledEditor.java, in gama.ui.shared.modeling, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.ui.editbox;

/**
 * Class IBoxEnabledEditor.
 * 
 * @author drogoul
 * @since 16 nov. 2014
 * 
 */
public interface IBoxEnabledEditor {

	/**
	 * Gets the decorator.
	 *
	 * @return the decorator
	 */
	IBoxDecorator getDecorator();

	/**
	 * Checks if is decoration enabled.
	 *
	 * @return true, if is decoration enabled
	 */
	boolean isDecorationEnabled();

	/**
	 * Creates the decorator.
	 */
	void createDecorator();

	/**
	 * Decorate.
	 *
	 * @param doIt the do it
	 */
	void decorate(boolean doIt);

	/**
	 * Enable updates.
	 *
	 * @param visible the visible
	 */
	void enableUpdates(boolean visible);

}
