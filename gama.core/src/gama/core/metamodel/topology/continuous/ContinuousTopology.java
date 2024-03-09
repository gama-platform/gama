/*******************************************************************************************************
 *
 * ContinuousTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology.continuous;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.AbstractTopology;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.gaml.operators.Maths;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 4 juil. 2011
 *
 * @todo Description
 *
 */
public class ContinuousTopology extends AbstractTopology {

	/**
	 * Initializes inner environment for agents other than "world".
	 *
	 * @param directMacro
	 * @param torus
	 */
	public ContinuousTopology(final IScope scope, final IShape environment) {
		super(scope, environment, null);
		places = GamaListFactory.wrap(Types.GEOMETRY, environment);
	}

	/**
	 * @see gama.interfaces.IValue#stringValue()
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "Continuous topology in " + environment.toString();
	}

	/**
	 * @see gama.environment.AbstractTopology#_toGaml()
	 */
	@Override
	protected String _toGaml(final boolean includingBuiltIn) {
		return IKeyword.TOPOLOGY + "(" + environment.serializeToGaml(includingBuiltIn) + ")";
	}

	/**
	 * @see gama.environment.AbstractTopology#_copy()
	 */
	@Override
	protected ITopology _copy(final IScope scope) {
		return new ContinuousTopology(scope, environment);
	}

	/**
	 * @see gama.environment.ITopology#isValidLocation(gama.core.util.GamaPoint)
	 */
	@Override
	public boolean isValidLocation(final IScope scope, final GamaPoint p) {
		return environment.covers(p);
	}

	@Override
	public boolean isValidGeometry(final IScope scope, final IShape g) {
		return environment.intersects(g);
	}

	@Override
	public Double directionInDegreesTo(final IScope scope, final IShape g1, final IShape g2) {
		// TODO Attention : calcul fait uniquement sur les locations. Il
		// conviendrait plutot de
		// faire une DistanceOp().getNearestPoints()
		if (g1 == null || g2 == null) return null;
		GamaPoint source = g1.getLocation();
		GamaPoint target = g2.getLocation();
		if (isTorus()) {
			source = normalizeLocation(scope, source, false);
			target = normalizeLocation(scope, target, false);
		}

		final double x2 = /* translateX(source.x, target.x); */target.getX();
		final double y2 = /* translateY(source.y, target.y); */target.getY();
		final double dx = x2 - source.getX();
		final double dy = y2 - source.getY();
		// AD 21/03/15: Fixes two long-standing bugs (see Issue 1177) + problems
		// in MovingSkill.move().
		final double result = Maths.atan2(dy, dx);
		return Maths.checkHeading(result);
	}

	@Override
	public boolean isContinuous() { return true; }

	@Override
	public Double distanceBetween(final IScope scope, final IShape g1, final IShape g2) {
		if (g1 == g2) return 0d;
		if (isTorus()) return returnToroidalGeom(g1).distance(returnToroidalGeom(g2));
		return g1.euclidianDistanceTo(g2);
	}

	@Override
	public Double distanceBetween(final IScope scope, final GamaPoint g1, final GamaPoint g2) {
		if (g1 == g2) return 0d;
		if (isTorus()) return returnToroidalGeom(g1).distance(returnToroidalGeom(g2));
		return g1.euclidianDistanceTo(g2);
	}

}