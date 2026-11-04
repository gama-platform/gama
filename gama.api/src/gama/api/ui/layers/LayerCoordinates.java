/*******************************************************************************************************
 *
 * LayerCoordinates.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

/**
 * XY placement modes. Overlays and charts default to SCREEN; other layers default to WORLD.
 */
public enum LayerCoordinates {
	WORLD, SCREEN;

	/** Shared defaults for runtime layer data and compile-time validation. */
	public static LayerCoordinates defaultFor(final String keyword) {
		return "overlay".equals(keyword) || "chart".equals(keyword) ? SCREEN : WORLD;
	}

	/** Converts an untyped screen coordinate; exactly 1 denotes one logical pixel, not the full extent. */
	public static double screenLength(final double value, final double extent) {
		return Math.abs(value) < 1 ? value * extent : value;
	}

	/** Negative screen positions are measured from the opposite edge. */
	public static double screenPosition(final double value, final double extent) {
		return screenLength(value, extent) + (value < 0 ? extent : 0);
	}
}
