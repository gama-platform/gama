/*******************************************************************************************************
 *
 * EditorsCategory.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import gama.api.types.color.IColor;

/**
 * The EditorsCategory.
 */
public record EditorsCategory(String name, IColor color, Boolean expanded) {

}
