/*******************************************************************************************************
 *
 * MultipleTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.continuous;

import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;
import gama.api.types.misc.IContainer;
import gama.api.types.topology.ITopology;

/**
 * The class GamaMultipleTopology.
 *
 * @author drogoul
 * @since 30 nov. 2011
 *
 */
public class MultipleTopology extends ContinuousTopology {

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 * @param environment
	 */
	public MultipleTopology(final IScope scope, final IContainer<?, IShape> places) throws GamaRuntimeException {
		// For the moment, use the geometric envelope in order to simplify the "environment".
		super(scope, GamaShapeFactory.geometriesToGeometry(scope, places).getGeometricEnvelope());
		this.places = places;
	}

	@Override
	protected boolean canCreateAgents() {
		return true;
	}

	/**
	 * @see gama.api.types.misc.interfaces.IValue#stringValue()
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "Multiple topology in " + environment.toString();
	}

	/**
	 * @see gama.environment.AbstractTopology#_toGaml()
	 */
	@Override
	protected String _toGaml(final boolean includingBuiltIn) {
		return IKeyword.TOPOLOGY + "(" + places.serializeToGaml(includingBuiltIn) + ")";
	}

	/**
	 * @see gama.environment.AbstractTopology#_copy()
	 */
	@Override
	protected ITopology _copy(final IScope scope) {
		return new MultipleTopology(scope, places/* , isTorus */);
	}

}
