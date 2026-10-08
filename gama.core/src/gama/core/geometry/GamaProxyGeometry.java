/*******************************************************************************************************
 *
 * GamaProxyGeometry.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.geometry;

import static gama.api.utils.geometry.GeometryUtils.translate;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.geometry.GeometryUtils;
import gama.api.utils.geometry.IEnvelope;

/**
 * Class GamaProxyGeometry. A geometry that represents a wrapper to a reference geometry and a translation. All the
 * operations are transmitted to the reference geometry, taking this translation into account. The inner geometry of
 * each instance is computed dynamically every time.
 *
 * This class does not allow any other transformation to its geometry than translation (no scaling, no rotation, etc.).
 * TODO This might come later when rotatedBy() and scaledBy() are redefined outside GamaShape.
 *
 * Abstract methods to override: getReferenceGeometry()
 *
 * Caching of the resulting innner geometry can be achieved by redefining getInnerGeometry() and implementing the policy
 * there. However, the purpose of this class is principally to save memory (see. GamaSpatialMatrix).
 *
 *
 * AD: Changed in 2016 to create attributes due to the abandon of attributes in agents. These geometries have attributes
 * now.
 *
 * The geometries dont have individual attributes. Instead, they read from / write to the attributes of the reference
 * geometry. This can be a simple way to implement properties common to a set of geometries. Subclasses that wish to
 * implement individual attributes can do so by overriding the corresponding methods.
 *
 *
 * @author drogoul
 * @since 18 mai 2013
 *
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public abstract class GamaProxyGeometry implements IShape, Cloneable {

	/** The absolute location. */
	IPoint absoluteLocation;
	// Property map to add all kinds of information (e.g to specify if the
	// geometry is a sphere, a
	// cube, etc...). Can be reused by subclasses (for example to store GIS
	/** The attributes. */
	// information)
	// protected IMap<String, Object> attributes;

	/**
	 * Instantiates a new gama proxy geometry.
	 *
	 * @param loc
	 *            the loc
	 */
	public GamaProxyGeometry(final IPoint loc) {
		setLocation(loc);
	}

	@Override
	public IType getGamlType() { return Types.GEOMETRY; }

	/**
	 * Method setLocation()
	 *
	 * @see gama.api.utils.interfaces.ILocated#setLocation(gama.core.metamodel.shape.GamaPoint)
	 */
	@Override
	public IPoint setLocation(final IPoint loc) {
		absoluteLocation = loc;
		return loc;
	}

	/**
	 * Method getLocation()
	 *
	 * @see gama.api.utils.interfaces.ILocated#getLocation()
	 */
	@Override
	public IPoint getLocation() { return absoluteLocation; }

	/**
	 * Method stringValue()
	 *
	 * @see gama.api.types.misc.IValue#stringValue(gama.api.runtime.scope.IScope)
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return SHAPE_WRITER.write(getInnerGeometry());
	}

	/**
	 * @return The geometry wrapped by this proxy. This geometry can be static or dynamic (all translations are computed
	 *         dynamically). No caching being made in the basic implementation, it can also change during the lifetime
	 *         of the proxy.
	 */
	protected abstract IShape getReferenceGeometry();

	/**
	 * Method copy()
	 *
	 * @see gama.api.types.misc.IValue#copy(gama.api.runtime.scope.IScope)
	 */
	@Override
	public IShape copy(final IScope scope) throws GamaRuntimeException {
		return GamaShapeFactory.createFrom(this);
	}

	/**
	 * Method toGaml()
	 *
	 * @see gama.api.utils.interfaces.IGamlable#toGaml()
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getReferenceGeometry().serializeToGaml(includingBuiltIn) + " at_location "
				+ absoluteLocation.serializeToGaml(includingBuiltIn);
	}

	/**
	 * Method getAttributes(). Maintain a map of attributes in each translated shape, which is costly.
	 *
	 * @see gama.api.utils.interfaces.IAttributed#getAttributes()
	 */
	@Override
	public IMap<String, Object> getAttributes(final boolean createIfNeeded) {
		return null;
		// if (attributes == null && createIfNeeded) { attributes = GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		// }
		// return attributes;
	}

	/**
	 * Method getAgent()
	 *
	 * @see gama.api.types.geometry.IShape#getAgent()
	 */
	@Override
	public IAgent getAgent() {
		// This method is intended to be subclassed if necessary
		return null;
	}

	/**
	 * Method setAgent()
	 *
	 * @see gama.api.types.geometry.IShape#setAgent(gama.api.kernel.agent.IAgent)
	 */
	@Override
	public void setAgent(final IAgent agent) {
		// This method is intended to be subclassed if necessary
	}

	/**
	 * Method getGeometry()
	 *
	 * @see gama.api.types.geometry.IShape#getGeometry()
	 */
	@Override
	public IShape getGeometry() {
		return this; // TODO or the translated geometry ??
	}

	/**
	 * Method setGeometry()
	 *
	 * @see gama.api.types.geometry.IShape#setGeometry(gama.api.types.geometry.IShape)
	 */
	@Override
	public void setGeometry(final IShape g) {
		// Not allowed. The reference geometry is final
	}

	/**
	 * Method isPoint()
	 *
	 * @see gama.api.types.geometry.IShape#isPoint()
	 */
	@Override
	public boolean isPoint() { return getReferenceGeometry().isPoint(); }

	@Override
	public boolean isLine() { return getReferenceGeometry().isLine(); }

	/**
	 * Method getInnerGeometry()
	 *
	 * @see gama.api.types.geometry.IShape#getInnerGeometry()
	 */
	@Override
	public Geometry getInnerGeometry() {
		final Geometry copy = getReferenceGeometry().getInnerGeometry().copy();
		translate(copy, getReferenceGeometry().getLocation(), getLocation());
		return copy;
	}

	/**
	 * Method getEnvelope(). Computed dynamically. A subclass may choose to cache this (often used) information by
	 * redefining this method
	 *
	 * @see gama.api.types.geometry.IShape#getEnvelope()
	 */
	@Override
	public IEnvelope getEnvelope() {
		final IEnvelope copy = getReferenceGeometry().getEnvelope();
		final IPoint loc = getLocation();
		final IPoint loc2 = getReferenceGeometry().getLocation();
		final double dx = loc.getX() - loc2.getX();
		final double dy = loc.getY() - loc2.getY();
		final double dz = loc.getZ() - loc2.getZ();
		copy.translate(dx, dy, dz);
		return copy;
	}

	/**
	 * Method covers()
	 *
	 * @see gama.api.types.geometry.IShape#covers(gama.api.types.geometry.IShape)
	 */
	@Override
	public boolean covers(final IShape g) {
		// TODO Use prepared geometries like in GamaShape ?
		return getInnerGeometry().covers(g.getInnerGeometry());
	}

	/**
	 * Method crosses()
	 *
	 * @see gama.api.types.geometry.IShape#crosses(gama.api.types.geometry.IShape)
	 */
	@Override
	public boolean crosses(final IShape g) {
		return getInnerGeometry().crosses(g.getInnerGeometry());
	}

	/**
	 * Method euclidianDistanceTo()
	 *
	 * @see gama.api.types.geometry.IShape#euclidianDistanceTo(gama.api.types.geometry.IShape)
	 */
	@Override
	public double euclidianDistanceTo(final IShape g) {
		if (isPoint() && g.isPoint()) return g.getLocation().euclidianDistanceTo(getLocation());
		return getInnerGeometry().distance(g.getInnerGeometry());
	}

	/**
	 * Method euclidianDistanceTo()
	 *
	 * @see gama.api.types.geometry.IShape#euclidianDistanceTo(gama.core.metamodel.shape.GamaPoint)
	 */
	@Override
	public double euclidianDistanceTo(final IPoint g) {
		if (isPoint()) return g.euclidianDistanceTo(getLocation());
		return getInnerGeometry().distance(g.getInnerGeometry());
	}

	/**
	 * Method intersects()
	 *
	 * @see gama.api.types.geometry.IShape#intersects(gama.api.types.geometry.IShape)
	 */
	@Override
	public boolean intersects(final IShape g) {
		return getInnerGeometry().intersects(g.getInnerGeometry());
	}

	@Override
	public boolean touches(final IShape g) {
		return getInnerGeometry().touches(g.getInnerGeometry());
	}

	@Override
	public boolean partiallyOverlaps(final IShape g) {
		return getInnerGeometry().overlaps(g.getInnerGeometry());
	}

	/**
	 * Method getPerimeter()
	 *
	 * @see gama.api.types.geometry.IShape#getPerimeter()
	 */
	@Override
	public double getPerimeter() { return getReferenceGeometry().getPerimeter(); }

	/**
	 * Method setInnerGeometry()
	 *
	 * @see gama.api.types.geometry.IShape#setInnerGeometry(org.locationtech.jts.geom.Geometry)
	 */
	@Override
	public void setInnerGeometry(final Geometry intersection) {}

	/**
	 * Method dispose()
	 *
	 * @see gama.api.types.geometry.IShape#dispose()
	 */
	@Override
	public void dispose() {
		// if (attributes != null) { attributes.clear(); }
		// attributes = null;
	}

	@Override
	public Type getGeometricalType() { return getReferenceGeometry().getGeometricalType(); }

	/**
	 * Method getPoints()
	 *
	 * @see gama.api.types.geometry.IShape#getPoints()
	 */
	@Override
	public IList<IPoint> getPoints() {
		final IList<IPoint> result = GamaListFactory.create(Types.POINT);
		final Coordinate[] points = getInnerGeometry().getCoordinates();
		for (final Coordinate c : points) { result.add(GamaPointFactory.create(c)); }
		return result;
	}

	/**
	 * Method setDepth()
	 *
	 * @see gama.api.types.geometry.IShape#setDepth(double)
	 */
	@Override
	public void setDepth(final double depth) {
		// this.setAttribute(IShape.DEPTH_ATTRIBUTE, depth);
	}

	/**
	 * Method getArea()
	 *
	 * @see gama.api.types.geometry.IShape#getArea()
	 */
	@Override
	public Double getArea() { return getReferenceGeometry().getArea(); }

	/**
	 * Method getVolume()
	 *
	 * @see gama.api.types.geometry.IShape#getVolume()
	 */
	@Override
	public Double getVolume() { return getReferenceGeometry().getVolume(); }

	/**
	 * Method getHoles()
	 *
	 * @see gama.api.types.geometry.IShape#getHoles()
	 */
	@Override
	public IList<IShape> getHoles() {
		final IList<IShape> holes = GamaListFactory.create(Types.GEOMETRY);
		final Geometry g = getInnerGeometry();
		if (g instanceof Polygon p) {
			final int n = p.getNumInteriorRing();
			for (int i = 0; i < n; i++) {
				holes.add(GamaShapeFactory.createFrom(
						GeometryUtils.getGeometryFactory().createPolygon(p.getInteriorRingN(i).getCoordinates())));
			}
		}
		return holes;
	}

	/**
	 * Method getCentroid()
	 *
	 * @see gama.api.types.geometry.IShape#getCentroid()
	 */
	@Override
	public IPoint getCentroid() { return absoluteLocation; }

	/**
	 * Method getExteriorRing()
	 *
	 * @see gama.api.types.geometry.IShape#getExteriorRing()
	 */
	@Override
	public IShape getExteriorRing(final IScope scope) {
		return getReferenceGeometry().getExteriorRing(scope).translatedTo(scope, absoluteLocation);
	}

	/**
	 * Method getWidth()
	 *
	 * @see gama.api.types.geometry.IShape#getWidth()
	 */
	@Override
	public Double getWidth() { return getReferenceGeometry().getWidth(); }

	/**
	 * Method getHeight()
	 *
	 * @see gama.api.types.geometry.IShape#getHeight()
	 */
	@Override
	public Double getHeight() { return getReferenceGeometry().getHeight(); }

	/**
	 * Method getDepth()
	 *
	 * @see gama.api.types.geometry.IShape#getDepth()
	 */
	@Override
	public Double getDepth() { return getReferenceGeometry().getDepth(); }

	/**
	 * Method getGeometricEnvelope()
	 *
	 * @see gama.api.types.geometry.IShape#getGeometricEnvelope()
	 */
	@Override
	public IShape getGeometricEnvelope() { return GamaShapeFactory.createFrom(getEnvelope().toGeometry()); }

	@Override
	public IShape translatedTo(final IScope scope, final IPoint absoluteLocation) {
		this.setLocation(absoluteLocation);
		return this;
	}

}
