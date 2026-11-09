/*******************************************************************************************************
 *
 * IDisplayControlLayer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

/** Pointer routing for controls drawn inside a display layer, in logical viewport pixels. */
public interface IDisplayControlLayer {

	boolean containsControl(int x, int y);

	boolean isControlCaptured();

	boolean controlMouseDown(int x, int y, int button);

	boolean controlMouseMove(int x, int y);

	boolean controlMouseUp(int x, int y, int button);

	void cancelControlInteraction();
}
