/*******************************************************************************************************
 *
 * IAsset.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.annotations.precompiler.OkForAPI;

/**
 * The Interface IAsset. A mostly tagging interface for files, icons, images... everything that can be considered as a
 * graphical "asset"
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface IAsset {

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	String getId();

}
