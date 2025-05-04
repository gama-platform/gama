/*******************************************************************************************************
 *
 * GamaPoint.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.shape;

import static gama.gaml.operators.Maths.round;
import static java.lang.Math.sqrt;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.NumberUtil;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.geometry.GeometryUtils;
import gama.core.common.geometry.IIntersectable;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMap;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * A mutable point in 3D, deriving from JTS Coordinate, that serves muliple purposes (location of agents, of geometries
 * -- through GamaCoordinateSequence --, vectors -- see Rotation3D and AxisAngle, etc.)
 *
 * @author drogoul 11 oct. 07
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
@vars ({ @variable (
		name = IKeyword.X,
		type = IType.FLOAT,
		doc = { @doc ("Returns the x ordinate of this point") }),
		@variable (
				name = IKeyword.Y,
				type = IType.FLOAT,
				doc = { @doc ("Returns the y ordinate of this point") }),
		@variable (
				name = IKeyword.Z,
				type = IType.FLOAT,
				doc = { @doc ("Returns the z ordinate of this point") }) })
public class GamaPoint extends Coordinate implements IShape, IIntersectable, Cloneable {

	/** The tolerance. */
	public static double TOLERANCE = GamaPreferences.Experimental.TOLERANCE_POINTS.getValue();
	static {
		GamaPreferences.Experimental.TOLERANCE_POINTS.onChange(v -> TOLERANCE = v);
	}

	/**
	 * The Class Immutable.
	 */
	public static class Immutable extends GamaPoint {

		/** The null point. */
		public static Immutable NULL_POINT = new GamaPoint.Immutable(0, 0, 0);

		/**
		 * Instantiates a new immutable.
		 */
		public Immutable() {}

		/**
		 * Instantiates a new immutable.
		 *
		 * @param coord
		 *            the coord
		 */
		public Immutable(final Coordinate coord) {
			x = coord.x;
			y = coord.y;
			z = coord.z;
		}

		/**
		 * Instantiates a new immutable.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param z
		 *            the z
		 */
		public Immutable(final double x, final double y, final double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		/**
		 * Instantiates a new immutable.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 */
		public Immutable(final double x, final double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public GamaPoint setLocation(final GamaPoint al) {
			return this;
		}

		@Override
		public GamaPoint setLocation(final double x, final double y, final double z) {
			return this;
		}

		@Override
		public void setCoordinate(final Coordinate c) {}

		@Override
		public void setOrdinate(final int i, final double v) {}

		@Override
		public void setX(final double xx) {}

		@Override
		public void setY(final double yy) {}

		@Override
		public void setZ(final double zz) {}

		@Override
		public GamaPoint add(final GamaPoint loc) {
			return this;
		}

		@Override
		public GamaPoint add(final double ax, final double ay, final double az) {
			return this;
		}

		@Override
		public GamaPoint subtract(final GamaPoint loc) {
			return this;
		}

		@Override
		public GamaPoint multiplyBy(final double value) {
			return this;
		}

		@Override
		public GamaPoint divideBy(final double value) {
			return this;
		}

		@Override
		public void setGeometry(final IShape g) {}

		@Override
		public void setInnerGeometry(final Geometry point) {

		}

		@Override
		public GamaPoint normalize() {
			return this;
		}

		@Override
		public void negate() {}

		@Override
		public void setDepth(final double depth) {}

	}

	/**
	 * Instantiates a new gama point.
	 */
	public GamaPoint() {
		x = 0.0d;
		y = 0.0d;
		z = 0.0d;
	}

	/**
	 * Instantiates a new gama point.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 */
	public GamaPoint(final double x, final double y) {
		setLocation(x, y, 0d);
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
	public GamaPoint(final double x, final double y, final double z) {
		setLocation(x, y, z);
	}

	/**
	 * Instantiates a new gama point.
	 *
	 * @param coord
	 *            the coord
	 */
	public GamaPoint(final Coordinate coord) {
		if (coord == null) {
			setLocation(0d, 0d, 0d);
		} else {
			setLocation(coord.x, coord.y, coord.z);
		}
	}

	/**
	 * Smaller than.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	public boolean smallerThan(final GamaPoint other) {
		return x < other.x || y < other.y || z < other.z;
	}

	/**
	 * Smaller than or equal to.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	public boolean smallerThanOrEqualTo(final GamaPoint other) {
		return x <= other.x || y <= other.y || z <= other.z;
	}

	/**
	 * Bigger than.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	public boolean biggerThan(final GamaPoint other) {
		return x > other.x || y > other.y || z > other.z;
	}

	/**
	 * Bigger than or equal to.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	public boolean biggerThanOrEqualTo(final GamaPoint other) {
		return x >= other.x || y >= other.y || z >= other.z;
	}

	@Override
	public GamaPoint setLocation(final GamaPoint al) {
		if (al == this) return this;
		return setLocation(al.x, al.y, al.z);
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
	public GamaPoint setLocation(final double x, final double y, final double z) {
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
	@getter (IKeyword.X)
	public double getX() { return x; }

	@Override
	@getter (IKeyword.Y)
	public double getY() { return y; }

	@Override
	@getter (IKeyword.Z)
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
	public GamaPoint getLocation() { return this; }

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
	public GamaPoint add(final GamaPoint loc) {
		x += loc.x;
		y += loc.y;
		setZ(z + loc.z);
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
	public GamaPoint add(final double ax, final double ay, final double az) {
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
	public GamaPoint subtract(final GamaPoint loc) {
		x -= loc.x;
		y -= loc.y;
		setZ(z - loc.z);
		return this;
	}

	/**
	 * Multiply by.
	 *
	 * @param value
	 *            the value
	 * @return the gama point
	 */
	public GamaPoint multiplyBy(final double value) {
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
	public GamaPoint divideBy(final double value) {
		x /= value;
		y /= value;
		setZ(z / value);
		return this;
	}

	@Override
	public GamaPoint copy(final IScope scope) {
		return new GamaPoint(x, y, z);
	}

	@Override
	public GamaShape getGeometry() { return GamaGeometryType.createPoint(this); }

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
	public Geometry getInnerGeometry() { return GeometryUtils.GEOMETRY_FACTORY.createPoint(this); }

	/**
	 * @see gama.interfaces.IGeometry#getEnvelope()
	 */
	@Override
	public Envelope3D getEnvelope() { return Envelope3D.of((Coordinate) this); }

	/**
	 * Returns the envelope considering this point as bounds
	 */
	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		return Envelope3D.of(0, x, 0, y, 0, z);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof GamaPoint) {
			if (TOLERANCE > 0.0) return equalsWithTolerance((GamaPoint) o, TOLERANCE);
			return equals3D((GamaPoint) o);
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
	public boolean equalsWithTolerance(final Coordinate c, final double tolerance) {
		if (tolerance == 0.0) return equals3D(c);
		if (!NumberUtil.equalsWithTolerance(this.x, c.x, tolerance)
				|| !NumberUtil.equalsWithTolerance(this.y, c.y, tolerance))
			return false;
		if (!Double.isNaN(z) && !Double.isNaN(c.z) && !NumberUtil.equalsWithTolerance(this.z, c.z, tolerance))
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

	@Override
	public double euclidianDistanceTo(final GamaPoint p) {
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
	public GamaMap getAttributes(final boolean createIfNeeded) {
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
	 * @see gama.core.metamodel.shape.IShape#getGeometricalType()
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
	public GamaPoint times(final double d) {
		return new GamaPoint(x * d, y * d, z * d);
	}

	/**
	 * Divided by.
	 *
	 * @param d
	 *            the d
	 * @return the gama point
	 */
	public GamaPoint dividedBy(final double d) {
		return new GamaPoint(x / d, y / d, z / d);
	}

	/**
	 * Minus.
	 *
	 * @param other
	 *            the other
	 * @return the gama point
	 */
	public GamaPoint minus(final GamaPoint other) {
		return new GamaPoint(x - other.x, y - other.y, z - other.z);
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
	public GamaPoint minus(final double ax, final double ay, final double az) {
		return new GamaPoint(x - ax, y - ay, z - az);
	}

	/**
	 * Plus.
	 *
	 * @param other
	 *            the other
	 * @return the gama point
	 */
	public GamaPoint plus(final GamaPoint other) {
		return new GamaPoint(x + other.x, y + other.y, z + other.z);
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
	public GamaPoint plus(final double ax, final double ay, final double az) {
		return new GamaPoint(x + ax, y + ay, z + az);
	}

	/**
	 * Norm.
	 *
	 * @return the double
	 */
	public double norm() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + hashCode(x);
		result = 370 * result + hashCode(y);
		return 3700 * result + hashCode(z);
	}

	/**
	 * Normalized.
	 *
	 * @return the gama point
	 */
	public GamaPoint normalized() {
		final double r = this.norm();
		if (r == 0d) return new GamaPoint(0, 0, 0);
		return new GamaPoint(this.x / r, this.y / r, this.z / r);
	}

	/**
	 * Normalize.
	 *
	 * @return the gama point
	 */
	public GamaPoint normalize() {
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
	public GamaPoint negated() {
		return new GamaPoint(-x, -y, -z);
	}

	/**
	 * Negate.
	 */
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
	public final static double dotProduct(final GamaPoint v1, final GamaPoint v2) {
		return v1.dotProductWith(v2);
	}

	/**
	 * Dot product with.
	 *
	 * @param v2
	 *            the v 2
	 */
	public final double dotProductWith(final GamaPoint v2) {
		return x * v2.x + y * v2.y + z * v2.z;
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
	public final static GamaPoint crossProduct(final GamaPoint v1, final GamaPoint v2) {
		return v1.crossProductWith(v2);
	}

	/**
	 * Cross product with.
	 *
	 * @param v2
	 *            the other
	 * @return the gama point
	 */
	public final GamaPoint crossProductWith(final GamaPoint v2) {
		return new GamaPoint(y * v2.z - z * v2.y, v2.x * z - v2.z * x, x * v2.y - y * v2.x);

	}

	/**
	 * Method getPoints()
	 *
	 * @see gama.core.metamodel.shape.IShape#getPoints()
	 */
	@Override
	public IList<GamaPoint> getPoints() {
		final IList result = GamaListFactory.create(Types.POINT);
		result.add(clone());
		return result;
	}

	/**
	 * @return the point with y negated (for OpenGL, for example), without side effect on the point.
	 */
	public GamaPoint yNegated() {
		return new GamaPoint(x, -y, z);
	}

	@Override
	public void setDepth(final double depth) {}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType getGamlType() { return Types.POINT; }

	/**
	 * Method getArea()
	 *
	 * @see gama.core.metamodel.shape.IShape#getArea()
	 */
	@Override
	public Double getArea() { return 0d; }

	/**
	 * Method getVolume()
	 *
	 * @see gama.core.metamodel.shape.IShape#getVolume()
	 */
	@Override
	public Double getVolume() { return 0d; }

	/**
	 * Method getPerimeter()
	 *
	 * @see gama.core.metamodel.shape.IShape#getPerimeter()
	 */
	@Override
	public double getPerimeter() { return 0; }

	/**
	 * Method getHoles()
	 *
	 * @see gama.core.metamodel.shape.IShape#getHoles()
	 */
	@Override
	public IList<GamaShape> getHoles() { return GamaListFactory.EMPTY_LIST; }

	/**
	 * Method getCentroid()
	 *
	 * @see gama.core.metamodel.shape.IShape#getCentroid()
	 */
	@Override
	public GamaPoint getCentroid() { return this; }

	/**
	 * Method getExteriorRing()
	 *
	 * @see gama.core.metamodel.shape.IShape#getExteriorRing()
	 */
	@Override
	public GamaShape getExteriorRing(final IScope scope) {
		return GamaShapeFactory.createFrom(this);
	}

	/**
	 * Method getWidth()
	 *
	 * @see gama.core.metamodel.shape.IShape#getWidth()
	 */
	@Override
	public Double getWidth() { return 0d; }

	/**
	 * Method getHeight()
	 *
	 * @see gama.core.metamodel.shape.IShape#getHeight()
	 */
	@Override
	public Double getHeight() { return 0d; }

	/**
	 * Method getDepth()
	 *
	 * @see gama.core.metamodel.shape.IShape#getDepth()
	 */
	@Override
	public Double getDepth() { return null; }

	/**
	 * Method getGeometricEnvelope()
	 *
	 * @see gama.core.metamodel.shape.IShape#getGeometricEnvelope()
	 */
	@Override
	public GamaShape getGeometricEnvelope() { return GamaShapeFactory.createFrom(this); }

	/**
	 * Method getGeometries()
	 *
	 * @see gama.core.metamodel.shape.IShape#getGeometries()
	 */
	@Override
	public IList<? extends IShape> getGeometries() {
		return GamaListFactory.wrap(Types.GEOMETRY, GamaShapeFactory.createFrom(this));
	}

	/**
	 * Method isMultiple()
	 *
	 * @see gama.core.metamodel.shape.IShape#isMultiple()
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
	public GamaPoint orthogonal() {
		final double threshold = 0.6 * norm();
		if (threshold == 0) return this;
		if (Math.abs(x) <= threshold) {
			final double inverse = 1 / sqrt(y * y + z * z);
			return new GamaPoint(0, inverse * z, -inverse * y);
		}
		if (Math.abs(y) <= threshold) {
			final double inverse = 1 / sqrt(x * x + z * z);
			return new GamaPoint(-inverse * z, 0, inverse * x);
		}
		final double inverse = 1 / sqrt(x * x + y * y);
		return new GamaPoint(inverse * y, -inverse * x, 0);
	}

	/**
	 * With precision.
	 *
	 * @param i
	 *            the i
	 * @return the gama point
	 */
	public GamaPoint withPrecision(final int i) {
		return new GamaPoint(round(x, i), round(y, i), round(z, i));
	}

	@Override
	public void setGeometricalType(final Type t) {}

	@Override
	public GamaPoint clone() {
		return new GamaPoint(x, y, z);
	}

	@Override
	public boolean intersects(final Envelope env) {
		return env.intersects(this);
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
	public GamaPoint rounded() {
		return new GamaPoint(Math.round(x), Math.round(y), Math.round(z));
	}

	/**
	 * Checks if is null.
	 *
	 * @return true, if is null
	 */
	public boolean isNull() { return x == 0d && y == 0d && z == 0d; }

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "x", x, "y", y, "z", z);
	}

}
