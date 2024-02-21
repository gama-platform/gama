/*******************************************************************************************************
 *
 * IPhysicalEntity.java, in gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.physics.common;

import gama.core.metamodel.shape.GamaPoint;

/**
 * The Interface IPhysicalEntity.
 *
 * @param <VectorType> the generic type
 */
public interface IPhysicalEntity<VectorType> extends IPhysicalConstants {

	/**
	 * To vector.
	 *
	 * @param v the v
	 * @return the vector type
	 */
	VectorType toVector(final GamaPoint v);

	/**
	 * To gama point.
	 *
	 * @param v the v
	 * @return the gama point
	 */
	GamaPoint toGamaPoint(VectorType v);

}
