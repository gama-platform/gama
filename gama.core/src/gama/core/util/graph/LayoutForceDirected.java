/*******************************************************************************************************
 *
 * LayoutForceDirected.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.graph;

import java.util.IdentityHashMap;
import java.util.Map;

import org.jgrapht.Graph;

import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.gaml.operators.ContainerOperators;
import gama.gaml.operators.spatial.SpatialPunctal;

/**
 * The Class LayoutForceDirected.
 */
public class LayoutForceDirected {

	/** The graph. */
	private final Graph<IShape, IShape> graph;

	/** The equi. */
	private final boolean equi;

	/** The criterion. */
	private final double criterion;

	/** The cooling rate. */
	private final double coolingRate;

	/** The maxit. */
	private final int maxit;

	/** The coeff force. */
	private final double coeffForce;

	/** The bounds. */
	IShape bounds;

	/** The iteration. */
	private int iteration = 0;

	/** The area. */
	private double area;

	/** The k. */
	private double k;

	/** The t. */
	private double t;

	/** The equilibrium reached. */
	private boolean equilibriumReached = false;

	/** The disp. */
	private final Map<IShape, IPoint> disp;

	/** The loc. */
	private final Map<IShape, IPoint> loc;

	/**
	 * Creates a new Simulation.
	 *
	 * @param graph
	 * @param p
	 * @throws ParseException
	 */
	public LayoutForceDirected(final Graph<IShape, IShape> graph, final IShape bounds, final double coeffForce,
			final double coolingRate, final int maxit, final boolean isEquilibriumCriterion, final double criterion) {
		this.graph = graph;
		this.bounds = bounds;
		this.equi = isEquilibriumCriterion;
		this.criterion = criterion;
		this.coolingRate = coolingRate;
		this.maxit = maxit;
		this.coeffForce = coeffForce;
		this.disp = new IdentityHashMap<>();
		this.loc = new IdentityHashMap<>();

	}

	/**
	 * Starts the simulation.
	 *
	 * @return number of iterations used until criterion is met
	 */
	public int startSimulation(final IScope scope) {

		iteration = 0;
		equilibriumReached = false;

		area = Math.min(bounds.getWidth() * bounds.getWidth(), bounds.getHeight() * bounds.getHeight());
		k = coeffForce * Math.sqrt(area / graph.vertexSet().size());
		t = bounds.getWidth() / 10;

		for (final IShape v : graph.vertexSet()) {
			disp.put(v, GamaPointFactory.create());
			loc.put(v, v.getCentroid().copy(scope));
		}

		if (equi) {
			// simulate until mechanical equilibrium
			while (!equilibriumReached && iteration < maxit) { simulateStep(scope); }
		} else {
			// simulate maxit-steps
			for (int i = 0; i < maxit; i++) { simulateStep(scope); }
		}
		for (final IShape v : graph.vertexSet()) { v.setLocation(loc.get(v)); }
		return iteration;
	}

	/**
	 * Simulates a single step.
	 */
	private void simulateStep(final IScope scope) {
		final double toleranceCenter = Math.sqrt(area) / 10.0;
		final double distanceMinCenter = Math.sqrt(area) / 3.0;
		// calculate repulsive forces (from every vertex to every other)
		for (final IShape v : graph.vertexSet()) {
			// reset displacement vector for new calculation
			final IPoint vDisp = disp.get(v);
			vDisp.setLocation(0, 0, 0);
			for (final IShape u : graph.vertexSet()) {
				if (!v.equals(u)) {
					// normalized difference position vector of v and u
					IPoint deltaPos = loc.get(v).minus(loc.get(u));
					final double length = deltaPos.norm();

					if (length != 0) { deltaPos = deltaPos.times(forceRepulsive(length, k) / length); }

					vDisp.add(deltaPos);

				}
			}
		}

		// calculate attractive forces (only between neighbors)
		for (final IShape e : graph.edgeSet()) {
			final IShape u = graph.getEdgeSource(e);
			final IShape v = graph.getEdgeTarget(e);
			// normalized difference position vector of v and u
			IPoint deltaPos = loc.get(v).minus(loc.get(u));
			final double length = deltaPos.norm();
			if (length != 0) { deltaPos.multiplyBy(forceAttractive(length, k) / length); }

			disp.get(v).minus(deltaPos);
			disp.get(u).add(deltaPos);

		}

		// assume equilibrium
		equilibriumReached = true;

		for (final IShape v : graph.vertexSet()) {

			IPoint d = GamaPointFactory.create(disp.get(v));
			final double length = d.norm();

			// no equilibrium if one vertex has too high net force
			if (length > criterion) { equilibriumReached = false; }
			// limit maximum displacement by temperature t
			if (length != 0) { d.multiplyBy(Math.min(length, t) / length); }
			final IPoint l = loc.get(v);
			l.add(d);
			if (!bounds.intersects(l)) { loc.put(v, SpatialPunctal._closest_point_to(l, bounds)); }

		}
		final IPoint center = (IPoint) ContainerOperators.opMean(scope, GamaListFactory.wrap(Types.POINT, loc.values()));
		if (center.distance3D(bounds.getCentroid()) > toleranceCenter) {
			final IPoint d = bounds.getCentroid().minus(center);
			d.multiplyBy(0.5);
			for (final IShape v : graph.vertexSet()) {
				final IPoint l = loc.get(v);
				l.add(d);
				if (!bounds.intersects(l)) { loc.put(v, SpatialPunctal._closest_point_to(l, bounds)); }
			}
		}
		double maxDist = graph.vertexSet().stream().mapToDouble(v -> v.euclidianDistanceTo(center)).max().getAsDouble();
		if (maxDist < distanceMinCenter) {
			maxDist = distanceMinCenter - maxDist;
			for (final IShape v : graph.vertexSet()) {
				final IPoint l = loc.get(v);
				final IPoint d = l.minus(center);
				final double len = d.norm();
				if (len > 0) { d.multiplyBy(maxDist / d.norm()); }
				l.add(d);
				if (!bounds.intersects(l)) { loc.put(v, SpatialPunctal._closest_point_to(l, bounds)); }
			}
		}

		t = Math.max(t * (1 - coolingRate), 1);

		iteration++;
	}

	/**
	 * Calculates the amount of the attractive force between vertices using the expression entered by the user.
	 *
	 * @param d
	 *            the distance between the two vertices
	 * @param k
	 * @return amount of force
	 */
	private double forceAttractive(final double d, final double k) {
		return k == 0 ? 1 : d * d / k;
	}

	/**
	 * Calculates the amount of the repulsive force between vertices using the expression entered by the user.
	 *
	 * @param d
	 *            the distance between the two vertices
	 * @param k
	 * @return amount of force
	 */
	private double forceRepulsive(final double d, final double k) {
		return d == 0 ? 1 : k * k / d;
	}

}
