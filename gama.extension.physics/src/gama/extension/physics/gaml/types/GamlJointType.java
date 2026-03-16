/*******************************************************************************************************
 *
 * GamlJointType.java, in gama.extension.physics, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.gaml.types;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.core.runtime.IScope;
import gama.extension.physics.gaml.GamaJoint;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

/**
 * A GAML type that wraps the GamaJoint class, exposing its properties and methods to GAML.
 */

/**
 * The Class GamlJointType.
 */
@type (
		name = "joint",
		id = IType.TYPE + 1234,
		wraps = { GamaJoint.class },
		doc = @doc ("A type representing a physical joint in the simulation."))
public class GamlJointType extends GamaType<GamaJoint> {

	/**
	 * Can cast to const.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

	/**
	 * Cast.
	 *
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param scope
	 *            the scope
	 * @return the gama joint
	 */
	@Override
	public GamaJoint cast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		if (obj instanceof GamaJoint) return (GamaJoint) obj;
		throw new IllegalArgumentException("Cannot cast object to GamaJoint.");
	}

	/**
	 * Gets the default.
	 *
	 * @return the default
	 */
	@Override
	public GamaJoint getDefault() { return null; }
}