/*******************************************************************************************************
 *
 * GamaPoint.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import static gama.api.utils.MathUtils.round;
import static java.lang.Math.sqrt;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.NumberUtil;

import gama.api.data.factories.GamaEnvelopeFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonValue;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.list.GamaListFactory;
import gama.api.utils.prefs.GamaPreferences;

/**
 * A mutable point in 3D, deriving from JTS Coordinate, that serves muliple purposes (location of agents, of geometries
 * -- through GamaCoordinateSequence --, vectors -- see Rotation3D and AxisAngle, etc.)
 *
 * @author drogoul 11 oct. 07
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })

public class GamaPoint extends Coordinate implements IPoint {

	/** The tolerance. */
	public static double TOLERANCE = GamaPreferences.Experimental.TOLERANCE_POINTS.getValue();
	static {
		GamaPreferences.Experimental.TOLERANCE_POINTS.onChange(v -> TOLERANCE = v);
	}

	/**
	 * Instantiates a new gama point.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 */
	GamaPoint(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Smaller than.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	@Override
	public boolean smallerThan(final IPoint other) {
		if (other == null) return false;
		return x < other.getX() && y < other.getY();// || z < other.getZ();
	}

	/**
	 * Smaller than or equal to.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	@Override
	public boolean smallerThanOrEqualTo(final IPoint other) {
		if (other == null) return false;
		return x <= other.getX() && y <= other.getY(); // && z <= other.getZ();
	}

	/**
	 * Bigger than.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	@Override
	public boolean biggerThan(final IPoint other) {
		if (other == null) return false;
		return x > other.getX() && y > other.getY(); // && z > other.getZ();
	}

	/**
	 * Bigger than or equal to.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	@Override
	public boolean biggerThanOrEqualTo(final IPoint other) {
		if (other == null) return false;
		return x >= other.getX() && y >= other.getY(); // && z >= other.getZ();
	}

	/**
	 * Sets the location.
	 *
	 * @param al
	 *            the al
	 * @return the i point
	 */
	@Override
	public IPoint setLocation(final IPoint al) {
		if (al == this) return this;
		return setLocation(al.getX(), al.getY(), al.getZ());
	}

	/**
	 * Sets the location.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @return the gama point
	 */
	@Override
	public IPoint setLocation(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		setZ(z);
		return this;
	}

	@Override
	public void setCoordinate(final Coordinate c) {
		setLocation(c.x, c.y, c.z);
	}

	@Override
	public void setOrdinate(final int i, final double v) {
		switch (i) {
			case X:
				setX(v);
				break;
			case Y:
				setY(v);
				break;
			case Z:
				setZ(v);
				break;
		}
	}

	@Override
	public void setX(final double xx) { x = xx; }

	@Override
	public void setY(final double yy) { y = yy; }

	@Override
	public void setZ(final double zz) { z = Double.isNaN(zz) ? 0.0d : zz; }

	@Override
	public double getX() { return x; }

	@Override
	public double getY() { return y; }

	@Override
	public double getZ() { return z; }

	@Override
	public boolean isPoint() { return true; }

	@Override
	public boolean isLine() { return false; }

	@Override
	public String toString() {
		return "{" + x + "," + y + "," + z + "}";
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "{" + x + "," + y + "," + z + "}";
	}

	@Override
	public IPoint getLocation() { return this; }

	@Override
	public String stringValue(final IScope scope) {
		return "{" + x + "," + y + "," + z + "}";
	}

	/**
	 * Adds the.
	 *
	 * @param loc
	 *            the loc
	 * @return the gama point
	 */
	@Override
	public IPoint add(final IPoint loc) {
		x += loc.getX();
		y += loc.getY();
		setZ(z + loc.getZ());
		return this;
	}

	/**
	 * Adds the.
	 *
	 * @param ax
	 *            the ax
	 * @param ay
	 *            the ay
	 * @param az
	 *            the az
	 * @return the gama point
	 */
	@Override
	public IPoint add(final double ax, final double ay, final double az) {
		x += ax;
		y += ay;
		setZ(z + az);
		return this;
	}

	/**
	 * Subtract.
	 *
	 * @param loc
	 *            the loc
	 * @return the gama point
	 */
	@Override
	public IPoint subtract(final IPoint loc) {
		x -= loc.getX();
		y -= loc.getY();
		setZ(z - loc.getZ());
		return this;
	}

	/**
	 * Multiply by.
	 *
	 * @param value
	 *            the value
	 * @return the gama point
	 */
	@Override
	public IPoint multiplyBy(final double value) {
		x *= value;
		y *= value;
		setZ(z * value);
		return this;
	}

	/**
	 * Divide by.
	 *
	 * @param value
	 *            the value
	 * @return the gama point
	 */
	@Override
	public IPoint divideBy(final double value) {
		x /= value;
		y /= value;
		setZ(z / value);
		return this;
	}

	@Override
	public IPoint copy(final IScope scope) {
		return GamaPointFactory.create(x, y, z);
	}

	@Override
	public IShape getGeometry() { return GamaShapeFactory.buildPoint(this); }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.interfaces.IGeometry#setGeometry(gama.core.util.GamaGeometry)
	 */
	@Override
	public void setGeometry(final IShape g) {
		setLocation(g.getLocation());
	}

	/**
	 * @see gama.interfaces.IGeometry#getInnerGeometry()
	 */
	@Override
	public Geometry getInnerGeometry() { return GeometryUtils.getGeometryFactory().createPoint(this); }

	/**
	 * @see gama.interfaces.IGeometry#getEnvelope()
	 */
	@Override
	public IEnvelope getEnvelope() { return GamaEnvelopeFactory.of(this.toCoordinate()); }

	/**
	 * Returns the envelope considering this point as bounds
	 */
	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return GamaEnvelopeFactory.of(0, x, 0, y, 0, z);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof IPoint) {
			if (TOLERANCE > 0.0) return equalsWithTolerance((IPoint) o, TOLERANCE);
			return equals3D((IPoint) o);
		}
		return super.equals(o);
	}

	/**
	 * Equals with tolerance.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @param tolerance
	 *            the tolerance
	 * @return true, if successful
	 * @date 17 sept. 2023
	 */
	@Override
	public boolean equalsWithTolerance(final IPoint c, final double tolerance) {
		if (tolerance == 0.0) return equals3D(c);
		if (!NumberUtil.equalsWithTolerance(this.x, c.getX(), tolerance)
				|| !NumberUtil.equalsWithTolerance(this.y, c.getY(), tolerance))
			return false;
		if (!Double.isNaN(z) && !Double.isNaN(c.getZ()) && !NumberUtil.equalsWithTolerance(this.z, c.getZ(), tolerance))
			return false;

		return true;
	}

	@Override
	public boolean covers(final IShape g) {
		if (g.isPoint()) return this.equals(g.getLocation());
		return false;
	}

	@Override
	public double euclidianDistanceTo(final IShape g) {
		if (g.isPoint()) return euclidianDistanceTo(g.getLocation());
		return g.euclidianDistanceTo(this);
	}

	/**
	 * Euclidian distance to.
	 *
	 * @param p
	 *            the p
	 * @return the double
	 */
	@Override
	public double euclidianDistanceTo(final IPoint p) {
		return distance3D(p);
	}

	@Override
	public boolean intersects(final IShape g) {
		if (g.isPoint()) return this.equals(g.getLocation());
		return g.intersects(this);
	}

	@Override
	public boolean touches(final IShape g) {
		if (g.isPoint()) return false;
		return g.touches(this);
	}

	@Override
	public boolean partiallyOverlaps(final IShape g) {
		if (g.isPoint()) return false;
		return g.partiallyOverlaps(this);
	}

	@Override
	public boolean crosses(final IShape g) {
		if (g.isPoint()) return false;
		return g.crosses(this);
	}

	/**
	 * @see gama.interfaces.IGeometry#getAgent()
	 */
	@Override
	public IAgent getAgent() { return null; }

	@Override
	public void setAgent(final IAgent agent) {}

	/**
	 * @see gama.core.common.interfaces.IGeometry#setInnerGeometry(org.locationtech.jts.geom.Geometry)
	 */
	@Override
	public void setInnerGeometry(final Geometry point) {
		final Coordinate p = point.getCoordinate();
		setLocation(p.x, p.y, p.z);
	}

	/**
	 * @see gama.core.common.interfaces.IGeometry#dispose()
	 */
	@Override
	public void dispose() {}

	@Override
	public IMap getAttributes(final boolean createIfNeeded) {
		return null;
	}

	/**
	 * Do nothing
	 */
	@Override
	public void setAttribute(final String key, final Object value) {}

	/**
	 * Method getGeometricalType()
	 *
	 * @see gama.api.data.objects.IShape#getGeometricalType()
	 */
	@Override
	public Type getGeometricalType() { return Type.POINT; }

	/**
	 * Times.
	 *
	 * @param d
	 *            the d
	 * @return the gama point
	 */
	@Override
	public IPoint times(final double d) {
		return GamaPointFactory.create(x * d, y * d, z * d);
	}

	/**
	 * Divided by.
	 *
	 * @param d
	 *            the d
	 * @return the gama point
	 */
	@Override
	public IPoint dividedBy(final double d) {
		return GamaPointFactory.create(x / d, y / d, z / d);
	}

	/**
	 * Minus.
	 *
	 * @param other
	 *            the other
	 * @return the gama point
	 */
	@Override
	public IPoint minus(final IPoint other) {
		return GamaPointFactory.create(x - other.getX(), y - other.getY(), z - other.getZ());
	}

	/**
	 * Minus.
	 *
	 * @param ax
	 *            the ax
	 * @param ay
	 *            the ay
	 * @param az
	 *            the az
	 * @return the gama point
	 */
	@Override
	public IPoint minus(final double ax, final double ay, final double az) {
		return GamaPointFactory.create(x - ax, y - ay, z - az);
	}

	/**
	 * Plus.
	 *
	 * @param other
	 *            the other
	 * @return the gama point
	 */
	@Override
	public IPoint plus(final IPoint other) {
		return GamaPointFactory.create(x + other.getX(), y + other.getY(), z + other.getZ());
	}

	/**
	 * Plus.
	 *
	 * @param ax
	 *            the ax
	 * @param ay
	 *            the ay
	 * @param az
	 *            the az
	 * @return the gama point
	 */
	@Override
	public IPoint plus(final double ax, final double ay, final double az) {
		return GamaPointFactory.create(x + ax, y + ay, z + az);
	}

	/**
	 * Norm.
	 *
	 * @return the double
	 */
	@Override
	public double norm() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + hashCode(x);
		result = 370 * result + hashCode(y);
		// Keep consistent with `equals3D` and `setZ`
		final double hz = Double.isNaN(z) ? 0.0d : z;
		return 3700 * result + hashCode(hz);
	}

	/**
	 * Normalized.
	 *
	 * @return the gama point
	 */
	@Override
	public IPoint normalized() {
		final double r = this.norm();
		if (r == 0d) return GamaPointFactory.create(0, 0, 0);
		return GamaPointFactory.create(this.x / r, this.y / r, this.z / r);
	}

	/**
	 * Normalize.
	 *
	 * @return the gama point
	 */
	@Override
	public IPoint normalize() {
		final double r = this.norm();
		if (r == 0d) return this;
		x = x / r;
		y = y / r;
		z = z / r;
		return this;
	}

	/**
	 * Negated.
	 *
	 * @return the gama point
	 */
	@Override
	public IPoint negated() {
		return GamaPointFactory.create(-x, -y, -z);
	}

	/**
	 * Negate.
	 */
	@Override
	public void negate() {
		x = -x;
		y = -y;
		z = -z;
	}

	/**
	 * Dot product.
	 *
	 * @param v1
	 *            the v 1
	 * @param v2
	 *            the v 2
	 * @return the double
	 */
	public final static double dotProduct(final IPoint v1, final IPoint v2) {
		return v1.dotProductWith(v2);
	}

	/**
	 * Dot product with.
	 *
	 * @param v2
	 *            the v 2
	 */
	@Override
	public final double dotProductWith(final IPoint v2) {
		return x * v2.getX() + y * v2.getY() + z * v2.getZ();
	}

	/**
	 * Cross.
	 *
	 * @param v1
	 *            the v 1
	 * @param v2
	 *            the v 2
	 * @return the gama point
	 */
	public final static IPoint crossProduct(final IPoint v1, final IPoint v2) {
		return v1.crossProductWith(v2);
	}

	/**
	 * Cross product with.
	 *
	 * @param v2
	 *            the other
	 * @return the gama point
	 */
	@Override
	public final IPoint crossProductWith(final IPoint v2) {
		return GamaPointFactory.create(y * v2.getZ() - z * v2.getY(), v2.getX() * z - v2.getZ() * x,
				x * v2.getY() - y * v2.getX());

	}

	/**
	 * Method getPoints()
	 *
	 * @see gama.api.data.objects.IShape#getPoints()
	 */
	@Override
	public IList<IPoint> getPoints() {
		final IList result = GamaListFactory.create(Types.POINT);
		result.add(clone());
		return result;
	}

	/**
	 * @return the point with y negated (for OpenGL, for example), without side effect on the point.
	 */
	@Override
	public IPoint yNegated() {
		return GamaPointFactory.create(x, -y, z);
	}

	@Override
	public void setDepth(final double depth) {}

	/**
	 * Method getType()
	 *
	 * @see gama.api.gaml.types.ITyped#getGamlType()
	 */
	@Override
	public IType getGamlType() { return Types.POINT; }

	/**
	 * Method getArea()
	 *
	 * @see gama.api.data.objects.IShape#getArea()
	 */
	@Override
	public Double getArea() { return 0d; }

	/**
	 * Method getVolume()
	 *
	 * @see gama.api.data.objects.IShape#getVolume()
	 */
	@Override
	public Double getVolume() { return 0d; }

	/**
	 * Method getPerimeter()
	 *
	 * @see gama.api.data.objects.IShape#getPerimeter()
	 */
	@Override
	public double getPerimeter() { return 0; }

	/**
	 * Method getHoles()
	 *
	 * @see gama.api.data.objects.IShape#getHoles()
	 */
	@Override
	public IList<IShape> getHoles() { return GamaListFactory.getEmptyList(); }

	/**
	 * Method getCentroid()
	 *
	 * @see gama.api.data.objects.IShape#getCentroid()
	 */
	@Override
	public IPoint getCentroid() { return this; }

	/**
	 * Method getExteriorRing()
	 *
	 * @see gama.api.data.objects.IShape#getExteriorRing()
	 */
	@Override
	public IShape getExteriorRing(final IScope scope) {
		return GamaShapeFactory.createFrom(this);
	}

	/**
	 * Method getWidth()
	 *
	 * @see gama.api.data.objects.IShape#getWidth()
	 */
	@Override
	public Double getWidth() { return 0d; }

	/**
	 * Method getHeight()
	 *
	 * @see gama.api.data.objects.IShape#getHeight()
	 */
	@Override
	public Double getHeight() { return 0d; }

	/**
	 * Method getDepth()
	 *
	 * @see gama.api.data.objects.IShape#getDepth()
	 */
	@Override
	public Double getDepth() { return null; }

	/**
	 * Method getGeometricEnvelope()
	 *
	 * @see gama.api.data.objects.IShape#getGeometricEnvelope()
	 */
	@Override
	public IShape getGeometricEnvelope() { return GamaShapeFactory.createFrom(this); }

	/**
	 * Method getGeometries()
	 *
	 * @see gama.api.data.objects.IShape#getGeometries()
	 */
	@Override
	public IList<? extends IShape> getGeometries() {
		return GamaListFactory.wrap(Types.GEOMETRY, GamaShapeFactory.createFrom(this));
	}

	/**
	 * Method isMultiple()
	 *
	 * @see gama.api.data.objects.IShape#isMultiple()
	 */
	@Override
	public boolean isMultiple() { return false; }

	@Override
	public double getOrdinate(final int i) {
		switch (i) {
			case 0:
				return x;
			case 1:
				return y;
			case 2:
				return z;
		}
		return 0d;
	}

	@Override
	public void copyShapeAttributesFrom(final IShape other) {}

	/**
	 * Orthogonal.
	 *
	 * @return the gama point
	 */
	@Override
	public IPoint orthogonal() {
		final double threshold = 0.6 * norm();
		if (threshold == 0) return this;
		if (Math.abs(x) <= threshold) {
			final double inverse = 1 / sqrt(y * y + z * z);
			return GamaPointFactory.create(0, inverse * z, -inverse * y);
		}
		if (Math.abs(y) <= threshold) {
			final double inverse = 1 / sqrt(x * x + z * z);
			return GamaPointFactory.create(-inverse * z, 0, inverse * x);
		}
		final double inverse = 1 / sqrt(x * x + y * y);
		return GamaPointFactory.create(inverse * y, -inverse * x, 0);
	}

	/**
	 * With precision.
	 *
	 * @param i
	 *            the i
	 * @return the gama point
	 */
	@Override
	public IPoint withPrecision(final int i) {
		return GamaPointFactory.create(round(x, i), round(y, i), round(z, i));
	}

	@Override
	public void setGeometricalType(final Type t) {}

	// Necessary to keep GamaPoint here.
	@Override
	public GamaPoint clone() {
		return new GamaPoint(x, y, z);
	}

	@Override
	public boolean intersects(final Envelope env) {
		return env.intersects(this);
	}

	@Override
	public boolean intersects(final IEnvelope env) {
		return env.intersects(this.toCoordinate());
	}

	@Override
	public boolean intersects(final Coordinate env) {
		return this.equals3D(env);
	}

	/**
	 * Rounded.
	 *
	 * @return the gamap point
	 */
	@Override
	public IPoint rounded() {
		return GamaPointFactory.create(Math.round(x), Math.round(y), Math.round(z));
	}

	/**
	 * Checks if is null.
	 *
	 * @return true, if is null
	 */
	@Override
	public boolean isNull() { return x == 0d && y == 0d && z == 0d; }

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "x", x, "y", y, "z", z);
	}

	/**
	 * Translated to.
	 *
	 * @param scope
	 *            the scope
	 * @param absoluteLocation
	 *            the absolute location
	 * @return the i shape
	 */
	@Override
	public IShape translatedTo(final IScope scope, final IPoint absoluteLocation) {
		this.setLocation(absoluteLocation);
		return this;
	}

	@Override
	public boolean equals3D(final IPoint other) {
		if (other == null) return false;
		final double oz = other.getZ();
		final double z1 = Double.isNaN(this.z) ? 0.0d : this.z;
		final double z2 = Double.isNaN(oz) ? 0.0d : oz;
		return this.x == other.getX() && this.y == other.getY() && z1 == z2;
	}

	/**
	 * Distance 3 D.
	 *
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	@Override
	public double distance3D(final IPoint p) {
		return distance3D(p.toCoordinate());
	}

	@Override
	public Coordinate toCoordinate() {
		return this;
	}

	@Override
	public int compareTo(final IPoint p) {
		double px = p.getX();
		if (x < px) return -1;
		if (x > px) return 1;
		double py = p.getY();
		if (y < py) return -1;
		if (y > py) return 1;
		double pz = p.getZ();
		if (z < pz) return -1;
		if (z > pz) return 1;
		return 0;
	}

	@Override
	public int compareTo(final Coordinate p) {
		double px = p.getX();
		if (x < px) return -1;
		if (x > px) return 1;
		double py = p.getY();
		if (y < py) return -1;
		if (y > py) return 1;
		double pz = p.getZ();
		if (z < pz) return -1;
		if (z > pz) return 1;
		return 0;
	}

	@Override
	public double distance(final IPoint pt2) {
		double dx = x - pt2.getX();
		double dy = y - pt2.getY();
		return Math.hypot(dx, dy);
	}

	@Override
	public boolean equals2D(final IPoint current, final double tolerance) {
		if (!NumberUtil.equalsWithTolerance(this.x, current.getX(), tolerance)
				|| !NumberUtil.equalsWithTolerance(this.y, current.getY(), tolerance))
			return false;
		return true;
	}

}
