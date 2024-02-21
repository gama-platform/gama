/*******************************************************************************************************
 *
 * ISpeedDisplayer.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.interfaces;

/**
 * Class ISpeedDisplayer.
 *
 * @author drogoul
 * @since 29 mars 2014
 *
 */
public interface ISpeedDisplayer {

	/**
	 * Sets the init.
	 *
	 * @param i
	 *            the i
	 * @param notify
	 *            the notify
	 */
	void setInit(final double i, boolean notify);

	/**
	 * Sets the maximum.
	 *
	 * @param maximumCycleDuration
	 *            the new maximum
	 */
	void setMaximum(Double maximumCycleDuration);

}
