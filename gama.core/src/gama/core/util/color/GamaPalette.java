/*******************************************************************************************************
 *
 * GamaPalette.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.color;

import gama.api.data.objects.IColor;
import gama.api.data.objects.IList;
import gama.api.gaml.types.Types;
import gama.core.util.list.GamaList;

/**
 * The Class GamaPalette.
 */
public class GamaPalette extends GamaList<IColor> {

	/**
	 * Instantiates a new gama palette.
	 *
	 * @param colors
	 *            the colors
	 */
	public GamaPalette(final IList<IColor> colors) {
		super(100, Types.COLOR);
		addAll(colors);
	}
}