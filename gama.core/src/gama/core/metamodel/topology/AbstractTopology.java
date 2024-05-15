/*******************************************************************************************************
 *
 * AbstractTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;

import com.google.common.collect.Ordering;

import gama.core.common.geometry.Envelope3D;
import gama.core.common.geometry.GeometryUtils;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.continuous.RootTopology;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.ICollector;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.path.GamaSpatialPath;
import gama.core.util.path.PathFactory;
import gama.gaml.operators.Maths;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class AbstractTopology.
 */
public abstract class AbstractTopology implements ITopology {

	@Override
	public IType<?> getGamlType() { return Types.TOPOLOGY; }

	/** The environment. */
	protected IShape environment;

	/** The root. */
	protected RootTopology root;

	/** The places. */
	protected IContainer<?, IShape> places;

	/** The adjusted XY vector. */
	// VARIABLES USED IN TORUS ENVIRONMENT
	private double[][] adjustedXYVector = null;

	/**
	 * Instantiates a new abstract topology.
	 *
	 * @param scope
	 *            the scope
	 * @param env
	 *            the env
	 * @param root
	 *            the root
	 */
	public AbstractTopology(final IScope scope, final IShape env, final RootTopology root) {
		setRoot(scope, root);
		// speciesInserted = new ArrayList<>();
		environment = env;
	}

	@Override
	public void setRoot(final IScope scope, final RootTopology root) {
		this.root = root == null ? scope.getSimulation().getTopology() : root;
	}

	@Override
	public List<Geometry> listToroidalGeometries(final Geometry geom) {
		final Geometry copy = geom.copy();
		final List<Geometry> geoms = new ArrayList<>();
		final AffineTransformation at = new AffineTransformation();
		geoms.add(copy);
		for (int cnt = 0; cnt < 8; cnt++) {
			at.setToTranslation(getAdjustedXYVector()[cnt][0], getAdjustedXYVector()[cnt][1]);
			geoms.add(at.transform(copy));
		}
		return geoms;
	}

	/**
	 * Return toroidal geom.
	 *
	 * @param loc
	 *            the loc
	 * @return the geometry
	 */
	public Geometry returnToroidalGeom(final GamaPoint loc) {
		final List<Geometry> geoms = new ArrayList<>();
		final Point pt = GeometryUtils.GEOMETRY_FACTORY.createPoint(loc);
		final AffineTransformation at = new AffineTransformation();
		geoms.add(pt);
		for (int cnt = 0; cnt < 8; cnt++) {
			at.setToTranslation(getAdjustedXYVector()[cnt][0], getAdjustedXYVector()[cnt][1]);
			geoms.add(at.transform(pt));
		}
		return GeometryUtils.GEOMETRY_FACTORY.buildGeometry(geoms);
	}

	/**
	 * Return toroidal geom.
	 *
	 * @param shape
	 *            the shape
	 * @return the geometry
	 */
	public Geometry returnToroidalGeom(final IShape shape) {
		if (shape.isPoint()) return returnToroidalGeom(shape.getLocation());
		return GeometryUtils.GEOMETRY_FACTORY.buildGeometry(listToroidalGeometries(shape.getInnerGeometry()));
	}

	/**
	 * Toroidal geoms.
	 *
	 * @param scope
	 *            the scope
	 * @param shps
	 *            the shps
	 * @return the map
	 */
	public Map<Geometry, IAgent> toroidalGeoms(final IScope scope, final IContainer<?, ? extends IShape> shps) {
		final Map<Geometry, IAgent> geoms = GamaMapFactory.create();
		for (final IShape ag : shps.iterable(scope)) {
			final IAgent agent = ag.getAgent();
			if (agent != null) {
				geoms.put(GeometryUtils.GEOMETRY_FACTORY
						.buildGeometry(listToroidalGeometries(agent.getGeometry().getInnerGeometry())), agent);
			}
		}
		return geoms;
	}

	/**
	 * Creates the virtual environments.
	 */
	protected void createVirtualEnvironments() {
		adjustedXYVector = new double[8][2];
		final Envelope environmentEnvelope = environment.getEnvelope();

		final double environmentWidth = environmentEnvelope.getWidth();
		final double environmentHeight = environmentEnvelope.getHeight();

		// NORTH virtual environment
		adjustedXYVector[0][0] = 0.0;
		adjustedXYVector[0][1] = environmentHeight;

		// NORTH-WEST virtual environment
		adjustedXYVector[1][0] = environmentWidth;
		adjustedXYVector[1][1] = environmentHeight;

		// WEST virtual environment
		adjustedXYVector[2][0] = environmentWidth;
		adjustedXYVector[2][1] = 0.0;

		// SOUTH-WEST virtual environment
		adjustedXYVector[3][0] = environmentWidth;
		adjustedXYVector[3][1] = -environmentHeight;

		// SOUTH virtual environment
		adjustedXYVector[4][0] = 0.0;
		adjustedXYVector[4][1] = -environmentHeight;

		// SOUTH-EAST virtual environment
		adjustedXYVector[5][0] = -environmentWidth;
		adjustedXYVector[5][1] = -environmentHeight;

		// EAST virtual environment
		adjustedXYVector[6][0] = -environmentWidth;
		adjustedXYVector[6][1] = 0.0;

		// NORTH-EAST virtual environment
		adjustedXYVector[7][0] = -environmentWidth;
		adjustedXYVector[7][1] = environmentHeight;

	}

	/**
	 * Can create agents.
	 *
	 * @return true, if successful
	 */
	protected boolean canCreateAgents() {
		return false;
	}

	@Override
	public void initialize(final IScope scope, final IPopulation<? extends IAgent> pop) throws GamaRuntimeException {
		// Create the population from the places of the topology
		if (!canCreateAgents()) return;
		pop.createAgents(scope, places);

	}

	@Override
	public void removeAgent(final IAgent agent) {
		getSpatialIndex().remove(agent.getEnvelope(), agent);
	}

	@Override
	public GamaSpatialPath pathBetween(final IScope scope, final IShape source, final IShape target)
			throws GamaRuntimeException {
		return PathFactory.newInstance(scope, this,
				GamaListFactory.create(scope, Types.POINT, source.getLocation(), target.getLocation()), 0.0);
	}

	@Override
	public GamaSpatialPath pathBetween(final IScope scope, final GamaPoint source, final GamaPoint target)
			throws GamaRuntimeException {
		return PathFactory.newInstance(scope, this, GamaListFactory.wrap(Types.POINT, source, target), 0.0);
	}

	@Override
	public IList<GamaSpatialPath> kPathsBetween(final IScope scope, final IShape source, final IShape target,
			final int k) {
		final IList<GamaSpatialPath> paths = GamaListFactory.create(Types.PATH);
		paths.add(pathBetween(scope, source, target));
		return paths;
	}

	@Override
	public IList<GamaSpatialPath> kPathsBetween(final IScope scope, final GamaPoint source, final GamaPoint target,
			final int k) {
		final IList<GamaSpatialPath> paths = GamaListFactory.create(Types.PATH);
		paths.add(pathBetween(scope, source, target));
		return paths;
	}

	@Override
	public void updateAgent(final Envelope3D previous, final IAgent agent) {
		// if (GamaPreferences.External.QUADTREE_OPTIMIZATION.getValue()) {
		// if (speciesInserted.contains(agent.getSpecies())) { updateAgentBase(previous, agent); }
		// } else {
		// updateAgentBase(previous, agent);
		// }
		// }
		//
		// public void updateAgentBase(final Envelope3D previous, final IAgent agent) {
		if (previous != null && !previous.isNull()) { getSpatialIndex().remove(previous, agent); }
		getSpatialIndex().insert(agent);
	}

	@Override
	public IShape getEnvironment() { return environment; }

	@Override
	public GamaPoint normalizeLocation(final IScope scope, final GamaPoint point, final boolean nullIfOutside) {

		boolean covers = environment.getGeometry().covers(point);
		if (covers) return point;

		if (!isTorus()) {
			if (nullIfOutside) return null;
			return point;
		}

		// final Point pt = GeometryUtils.GEOMETRY_FACTORY.createPoint(point);
		// final AffineTransformation at = new AffineTransformation();
		GamaPoint p = new GamaPoint();
		for (int cnt = 0; cnt < 8; cnt++) {
			// at.setToTranslation(getAdjustedXYVector()[cnt][0], getAdjustedXYVector()[cnt][1]);
			// final GamaPoint newPt = new GamaPoint(at.transform(pt).getCoordinate());
			p.setLocation(point).add(getAdjustedXYVector()[cnt][0], getAdjustedXYVector()[cnt][1], 0);
			if (environment.getGeometry().covers(p)) return p;
		}

		// See if rounding errors of double do not interfere with the
		// computation.
		// In which case, the use of Maths.approxEquals(value1, value2,
		// tolerance) could help.

		// if ( envWidth == 0.0 ) {
		// xx = xx != envMinX ? nullIfOutside ? nil : envMinX : xx;
		// } else if ( xx < envMinX /* && xx > hostMinX - precision */) {
		// xx = /* !isTorus ? */nullIfOutside ? nil : envMinX /* : xx % envWidth
		// + envWidth */;
		// } else if ( xx >= envMaxX /*- precision*/) {
		// xx = /* !isTorus ? */nullIfOutside ? nil : envMaxX /* : xx % envWidth
		// */;
		// }
		// if ( xx == nil ) { return null; }
		// if ( envHeight == 0.0 ) {
		// yy = yy != envMinY ? nullIfOutside ? nil : envMinY : yy;
		// } else if ( yy < envMinY/* && yy > hostMinY - precision */) {
		// yy = /* !isTorus ? */nullIfOutside ? nil : envMinY /* : yy %
		// envHeight + envHeight */;
		// } else if ( yy >= envMaxY /*- precision*/) {
		// yy = /* !isTorus ? */nullIfOutside ? nil : envMaxY /* : yy %
		// envHeight */;
		// }
		// if ( yy == nil ) { return null; }
		// point.setLocation(xx, yy, point.getZ());

		return nullIfOutside ? null : point;
	}

	@Override
	public GamaPoint getDestination(final IScope scope, final GamaPoint source, final double direction,
			final double distance, final boolean nullIfOutside) {
		final double cos = distance * Maths.cos(direction);
		final double sin = distance * Maths.sin(direction);
		final GamaPoint result = source.plus(cos, sin, 0);
		return normalizeLocation(scope, result, nullIfOutside);
	}

	@Override
	public GamaPoint getDestination3D(final IScope scope, final GamaPoint source, final double heading,
			final double pitch, final double distance, final boolean nullIfOutside) throws GamaRuntimeException {
		final double x = distance * Maths.cos(pitch) * Maths.cos(heading);
		final double y = distance * Maths.cos(pitch) * Maths.sin(heading);
		final double z = distance * Maths.sin(pitch);
		return normalizeLocation3D(scope, new GamaPoint(source.getX() + x, source.getY() + y, source.getZ() + z),
				nullIfOutside);
	}

	/**
	 * Normalize location 3 D.
	 *
	 * @param point
	 *            the point
	 * @param nullIfOutside
	 *            the null if outside
	 * @return the gama point
	 */
	public GamaPoint normalizeLocation3D(final IScope scope, final GamaPoint point, final boolean nullIfOutside)
			throws GamaRuntimeException {
		final GamaPoint p = normalizeLocation(scope, point, nullIfOutside);
		if (p == null) return null;
		final double z = p.getZ();
		if (z < 0) return null;
		if (((GamaShape) environment.getGeometry()).getDepth() != null) {
			if (z > ((GamaShape) environment.getGeometry()).getDepth()) return null;
			return point;
		}
		throw GamaRuntimeException.error("The environment must be a 3D environment (e.g shape <- cube(100)).", scope);

	}

	@Override
	public ITopology copy(final IScope scope) throws GamaRuntimeException {
		return _copy(scope);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return _toGaml(includingBuiltIn);
	}

	/**
	 * @return a gaml description of the construction of this topology.
	 */
	protected abstract String _toGaml(boolean includingBuiltIn);

	/**
	 * @throws GamaRuntimeException
	 * @return a copy of this topology
	 */
	protected abstract ITopology _copy(IScope scope) throws GamaRuntimeException;

	@Override
	public GamaPoint getRandomLocation(final IScope scope) {
		return GeometryUtils.pointInGeom(scope, environment);
	}

	@Override
	public IContainer<?, IShape> getPlaces() { return places; }

	// protected void insertSpecies(final IScope scope, final ISpecies species) {
	// if (!this.speciesInserted.contains(species)) {
	// this.speciesInserted.add(species);
	// for (final IAgent ag : species.getPopulation(scope)) {
	// getSpatialIndex().insert(ag);
	// }
	// }
	// }
	//
	// protected void insertAgents(final IScope scope, final IAgentFilter filter) {
	// if (GamaPreferences.External.QUADTREE_OPTIMIZATION.getValue()) {
	// if (filter.getSpecies() != null) {
	// insertSpecies(scope, filter.getSpecies());
	// } else {
	// final IPopulation<? extends IAgent> pop = filter.getPopulation(scope);
	// if (pop != null) { insertSpecies(scope, pop.getSpecies()); }
	// }
	// }
	// }

	@Override
	public Collection<IAgent> getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter,
			final int number) {
		// insertAgents(scope, filter);
		if (!isTorus()) {
			try (ICollector<IAgent> alreadyChosen = Collector.getList()) {
				return getSpatialIndex().firstAtDistance(scope, source, 0, filter, number, alreadyChosen);
			}
		}
		final Geometry g0 = returnToroidalGeom(source.getGeometry());
		final Map<Geometry, IAgent> agents = getTororoidalAgents(source, scope, filter);
		agents.remove(g0);
		if (agents.size() <= number) return agents.values();
		final List<Geometry> ggs = new ArrayList<>(agents.keySet());
		scope.getRandom().shuffleInPlace(ggs);
		final Ordering<Geometry> ordering = Ordering.natural().onResultOf(input -> g0.distance(input));
		final IList<IAgent> shapes = GamaListFactory.create(Types.AGENT);
		for (final Geometry g : ordering.leastOf(ggs, number)) { shapes.add(agents.get(g)); }
		return shapes;
	}

	@Override
	public IAgent getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter) {
		// insertAgents(scope, filter);
		if (!isTorus()) return getSpatialIndex().firstAtDistance(scope, source, 0, filter);
		IAgent result = null;
		final Geometry g0 = returnToroidalGeom(source.getGeometry());
		final Map<Geometry, IAgent> agents = getTororoidalAgents(source, scope, filter);

		double distMin = Double.MAX_VALUE;
		for (final Geometry g1 : agents.keySet()) {
			final IAgent ag = agents.get(g1);
			if (source.getAgent() != null && ag == source.getAgent()) { continue; }
			final double dist = g0.distance(g1);
			if (dist < distMin) {
				distMin = dist;
				result = ag;
			}
		}
		return result;
	}

	@Override
	public IAgent getAgentFarthestTo(final IScope scope, final IShape source, final IAgentFilter filter) {
		if (!isTorus()) {
			IAgent result = null;
			double distMax = Double.MIN_VALUE;
			final IContainer<?, ? extends IShape> agents = getFilteredAgents(source, scope, filter);
			for (final IShape s : agents.iterable(scope)) {
				if (s instanceof IAgent) {
					final double dist = this.distanceBetween(scope, source, s);
					if (dist > distMax) {
						result = (IAgent) s;
						distMax = dist;
					}
				}
			}
			return result;
		}
		IAgent result = null;
		final Geometry g0 = returnToroidalGeom(source);
		final Map<Geometry, IAgent> agents = getTororoidalAgents(source, scope, filter);
		double distMax = Double.MIN_VALUE;
		for (final Geometry g1 : agents.keySet()) {
			final IAgent ag = agents.get(g1);
			if (source.getAgent() != null && ag == source.getAgent()) { continue; }
			final double dist = g0.distance(g1);
			if (dist > distMax) {
				distMax = dist;
				result = ag;
			}
		}
		return result;
	}

	/**
	 * Gets the tororoidal agents.
	 *
	 * @param source
	 *            the source
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @return the tororoidal agents
	 */
	public Map<Geometry, IAgent> getTororoidalAgents(final IShape source, final IScope scope,
			final IAgentFilter filter) {
		return toroidalGeoms(scope, getFilteredAgents(source, scope, filter));
	}

	/**
	 * Gets the filtered agents.
	 *
	 * @param source
	 *            the source
	 * @param scope
	 *            the scope
	 * @param filter
	 *            the filter
	 * @return the filtered agents
	 */
	@SuppressWarnings ("unchecked")
	public static IContainer<?, ? extends IShape> getFilteredAgents(final IShape source, final IScope scope,
			final IAgentFilter filter) {
		IContainer<?, ? extends IShape> shps;
		if (filter != null) {
			if (filter.hasAgentList()) {
				shps = filter.getAgents(scope);
			} else {
				shps = scope.getSimulation().getAgents(scope);
				filter.filter(scope, source, (Collection<? extends IShape>) shps);
			}
		} else {
			shps = scope.getSimulation().getAgents(scope);
		}
		return shps;
	}

	@Override
	public Collection<IAgent> getNeighborsOf(final IScope scope, final IShape source, final Double distance,
			final IAgentFilter filter) throws GamaRuntimeException {
		// insertAgents(scope, filter);

		if (!isTorus()) return getSpatialIndex().allAtDistance(scope, source, distance, filter);

		// FOR TORUS ENVIRONMENTS ONLY

		final Geometry g0 = returnToroidalGeom(source.getGeometry());
		try (ICollector<IAgent> agents = Collector.getOrderedSet()) {
			final Map<Geometry, IAgent> agentsMap = getTororoidalAgents(source, scope, filter);
			final IAgent sourceAgent = source.getAgent();
			for (final Geometry g1 : agentsMap.keySet()) {
				final IAgent ag = agentsMap.get(g1);
				if (sourceAgent != null && ag == sourceAgent) { continue; }
				final double dist = g0.distance(g1);
				if (dist <= distance) { agents.add(ag); }
			}
			return agents.items();
		}

	}

	@Override
	public double getWidth() { return environment.getEnvelope().getWidth(); }

	@Override
	public double getHeight() { return environment.getEnvelope().getHeight(); }

	@Override
	public void dispose() {
		// host = null;
		// scope = null;
	}

	/** The pg fact. */
	private final PreparedGeometryFactory pgFact = new PreparedGeometryFactory();

	/**
	 * Accept.
	 *
	 * @param pg1
	 *            the pg 1
	 * @param g2
	 *            the g 2
	 * @param rel
	 *            the rel
	 * @return true, if successful
	 */
	public static final boolean accept(final PreparedGeometry pg1, final Geometry g2, final SpatialRelation rel) {
		if (rel == SpatialRelation.OVERLAP) return pg1.intersects(g2);
		if (rel == SpatialRelation.INSIDE) return pg1.covers(g2);
		if (rel == SpatialRelation.COVER) return pg1.coveredBy(g2);
		if (rel == SpatialRelation.CROSS) return pg1.crosses(g2);
		if (rel == SpatialRelation.PARTIALLY_OVERLAP) return pg1.overlaps(g2);
		return pg1.touches(g2);
	}

	@Override
	public Collection<IAgent> getAgentsIn(final IScope scope, final IShape source, final IAgentFilter f,
			final SpatialRelation relation) {
		if (source == null) return Collections.EMPTY_SET;
		boolean covered = relation == SpatialRelation.INSIDE;
		// insertAgents(scope, f);
		if (!isTorus()) {
			final Envelope3D envelope = source.getEnvelope().intersection(environment.getEnvelope());
			try {
				final Collection<IAgent> shapes = getSpatialIndex().allInEnvelope(scope, source, envelope, f, covered);
				final PreparedGeometry pg = pgFact.create(source.getInnerGeometry());
				shapes.removeIf(each -> {
					if (each.dead()) return true;
					final Geometry geom = each.getInnerGeometry();
					return !accept(pg, geom, relation);
				});
				return shapes;
			} finally {
				envelope.dispose();
			}
		}
		try (final ICollector<IAgent> result = Collector.getOrderedSet()) {

			for (final IShape sourceSub : source.getGeometries()) {
				final Geometry sourceTo = returnToroidalGeom(sourceSub);
				final PreparedGeometry pg = pgFact.create(sourceTo);
				final Map<Geometry, IAgent> agentsMap = getTororoidalAgents(source, scope, f);
				for (final Geometry sh : agentsMap.keySet()) {
					final IAgent ag = agentsMap.get(sh);
					if (ag != null && !ag.dead()) {
						if (source.getAgent() != null && ag == source.getAgent()) { continue; }
						final Geometry geom = ag.getInnerGeometry();

						if (accept(pg, geom, relation)) { result.add(ag); }
					}
				}
			}
			return result.items();
		}
	}

	@Override
	public ISpatialIndex getSpatialIndex() { return root.getSpatialIndex(); }

	@Override
	public boolean isTorus() { return root.isTorus(); }

	/**
	 * Gets the adjusted XY vector.
	 *
	 * @return the adjusted XY vector
	 */
	protected double[][] getAdjustedXYVector() {
		if (adjustedXYVector == null) { createVirtualEnvironments(); }
		return adjustedXYVector;
	}

}
