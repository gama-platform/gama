/*******************************************************************************************************
 *
 * AmorphousTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;

import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaPathFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPath;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IAgentFilter;
import gama.api.utils.geometry.GeometryUtils;

/**
 * The class AmorphousTopology.
 *
 * @author drogoul
 * @since 2 décembre 2011
 *
 */
public class AmorphousTopology implements ITopology {

	/** The expandable environment. */
	IShape expandableEnvironment = GamaShapeFactory.buildPoint(GamaPointFactory.create());

	/**
	 * @see gama.api.data.objects.interfaces.IValue#stringValue()
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "Expandable topology";
	}

	@Override
	public IType<?> getGamlType() { return Types.TOPOLOGY; }

	/**
	 * @see gama.api.data.objects.interfaces.IValue#toGaml()
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "topology({0,0})";
	}

	@Override
	public ITopology copy(final IScope scope) throws GamaRuntimeException {
		return new AmorphousTopology();
	}

	@Override
	public void initialize(final IScope scope, final IPopulation<? extends IAgent> pop) throws GamaRuntimeException {}

	@Override
	public void updateAgent(final IEnvelope previous, final IAgent agent) {
		Geometry geom = GeometryUtils.robustUnion(expandableEnvironment.getGeometry().getInnerGeometry(),
				agent.getGeometry().getInnerGeometry());
		expandableEnvironment.setGeometry(GamaShapeFactory.createFrom(geom.getEnvelope()));
	}

	@Override
	public void removeAgent(final IAgent agent) {}

	@Override
	public IList<IAgent> getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter,
			final int number) {
		return GamaListFactory.getEmptyList();
	}

	@Override
	public IAgent getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter) {
		return null;
	}

	@Override
	public IAgent getAgentFarthestTo(final IScope scope, final IShape source, final IAgentFilter filter) {
		return null;
	}

	@Override
	public Set<IAgent> getNeighborsOf(final IScope scope, final IShape source, final Double distance,
			final IAgentFilter filter) throws GamaRuntimeException {
		return Collections.EMPTY_SET;
	}

	@Override
	public Set<IAgent> getAgentsIn(final IScope scope, final IShape source, final IAgentFilter f,
			final SpatialRelation relation) {
		return Collections.EMPTY_SET;
	}

	@Override
	public Double distanceBetween(final IScope scope, final IShape source, final IShape target) {
		return source.euclidianDistanceTo(target);
	}

	@Override
	public Double distanceBetween(final IScope scope, final IPoint source, final IPoint target) {
		return source.euclidianDistanceTo(target);
	}

	@Override
	public IPath pathBetween(final IScope scope, final IShape source, final IShape target) throws GamaRuntimeException {
		// return new GamaPath(this, GamaList.with(source, target));
		return GamaPathFactory.createFrom(scope, this, GamaListFactory.wrap(Types.GEOMETRY, source, target), 0.0);
	}

	@Override
	public IPoint getDestination(final IScope scope, final IPoint source, final double direction, final double distance,
			final boolean nullIfOutside) {
		double d = Math.toDegrees(direction);
		final double cos = distance * Math.cos(d);
		final double sin = distance * Math.sin(d);
		return GamaPointFactory.create(source.getX() + cos, source.getY() + sin);

	}

	@Override
	public IPoint getDestination3D(final IScope scope, final IPoint source, final double heading, final double pitch,
			final double distance, final boolean nullIfOutside) {
		double p = Math.toDegrees(pitch);
		double h = Math.toDegrees(heading);
		final double x = distance * Math.cos(p) * Math.cos(h);
		final double y = distance * Math.cos(p) * Math.sin(h);
		final double z = distance * Math.sin(p);
		return GamaPointFactory.create(source.getX() + x, source.getY() + y, source.getZ() + z);
	}

	@Override
	public IPoint getRandomLocation(final IScope scope) {
		return GamaPointFactory.create(scope.getRandom().next(), scope.getRandom().next());
	}

	@Override
	public IContainer<?, IShape> getPlaces() {
		final IList<IShape> result = GamaListFactory.create(Types.GEOMETRY);
		result.add(expandableEnvironment);
		return result;
	}

	@Override
	public IShape getEnvironment() { return expandableEnvironment; }

	/**
	 * @see gama.api.kernel.topology.environment.ITopology#normalizeLocation(gama.core.util.GamaPoint, boolean)
	 */
	@Override
	public IPoint normalizeLocation(final IScope scope, final IPoint p, final boolean nullIfOutside) {
		return p;
	}

	// @Override
	// public void shapeChanged(final IPopulation pop) {}

	/**
	 * @see gama.api.kernel.topology.environment.ITopology#getWidth()
	 */
	@Override
	public double getWidth() { return expandableEnvironment.getEnvelope().getWidth(); }

	/**
	 * @see gama.api.kernel.topology.environment.ITopology#getHeight()
	 */
	@Override
	public double getHeight() { return expandableEnvironment.getEnvelope().getHeight(); }

	/**
	 * @see gama.api.kernel.topology.environment.ITopology#dispose()
	 */
	@Override
	public void dispose() {}

	/**
	 * @see gama.api.kernel.topology.environment.ITopology#isValidLocation(gama.core.util.GamaPoint)
	 */
	@Override
	public boolean isValidLocation(final IScope scope, final IPoint p) {
		return true;
	}

	@Override
	public boolean isValidGeometry(final IScope scope, final IShape g) {
		return true;
	}

	@Override
	public Double directionInDegreesTo(final IScope scope, final IShape g1, final IShape g2) {
		final IPoint source = g1.getLocation();
		final IPoint target = g2.getLocation();
		final double x2 = /* translateX(source.x, target.x); */target.getX();
		final double y2 = /* translateY(source.y, target.y); */target.getY();
		final double dx = x2 - source.getX();
		final double dy = y2 - source.getY();
		final double result = Math.atan2(dy, dx);
		return Math.toDegrees(result);
	}

	/**
	 * @see gama.api.kernel.topology.ITopology#pathBetween(gama.core.metamodel.shape.GamaPoint,
	 *      gama.core.metamodel.shape.GamaPoint)
	 */
	@Override
	public IPath pathBetween(final IScope scope, final IPoint source, final IPoint target) throws GamaRuntimeException {
		return GamaPathFactory.createFrom(scope, this, GamaListFactory.create(scope, Types.POINT, source, target), 0.0);
	}

	@Override
	public List<Geometry> listToroidalGeometries(final Geometry geom) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public boolean isTorus() { return false; }

	@Override
	public boolean isContinuous() { return true; }

	@Override
	public ISpatialIndex getSpatialIndex() { return ISpatialIndex.NULL_INDEX; }

	@Override
	public IList<IPath> kPathsBetween(final IScope scope, final IShape source, final IShape target, final int k) {
		final IList<IPath> paths = GamaListFactory.create(Types.PATH);
		paths.add(pathBetween(scope, source, target));
		return paths;
	}

	@Override
	public IList<IPath> kPathsBetween(final IScope scope, final IPoint source, final IPoint target, final int k) {
		final IList<IPath> paths = GamaListFactory.create(Types.PATH);
		paths.add(pathBetween(scope, source, target));
		return paths;
	}

	@Override
	public void setRoot(final IScope scope, final ITopology rt) {}
}
