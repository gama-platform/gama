/*******************************************************************************************************
 *
 * IPositionChangeListener.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.controls;

/**
 * Listener interface for position change events of CoolSlider
 * 
 */
public interface IPositionChangeListener {
	/**
	 * Puts the position of the thumb of the slider after a change has occurred. The position has range from min to max
	 * and represents a integer that is a multiple of the incrementValue.<br>
	 * <br>
	 * 
	 * @param position
	 */
	public void positionChanged(SimpleSlider slider, double position);
}
