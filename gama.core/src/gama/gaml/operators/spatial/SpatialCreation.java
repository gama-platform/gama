package gama.gaml.operators.spatial;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.geometry.GeometryUtils;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.compilation.annotations.depends_on;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Maths;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Creation.
 */
public class SpatialCreation {

	/**
	 * Circle.
	 *
	 * @param scope
	 *            the scope
	 * @param radius
	 *            the radius
	 * @return the i shape
	 */
	@operator (
			value = "circle",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A circle geometry which radius is equal to the operand.",
			masterDoc = true,
			usages = { @usage (
					value = "returns a point if the radius operand is lower or equal to 0.") },
			comment = "the center of the circle is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "circle(10)",
					equals = "a geometry as a circle of radius 10.",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle", "square",
					"triangle" })
	@no_test // (comment="See Creation.experiment in test models : {Circle tests with tolerance}")
	public static IShape circle(final IScope scope, final Double radius) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (radius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildCircle(radius, location);
	}

	/**
	 * Circle.
	 *
	 * @param scope
	 *            the scope
	 * @param radius
	 *            the radius
	 * @param position
	 *            the position
	 * @return the i shape
	 */
	@operator (
			value = "circle",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A circle geometry which radius is equal to the first operand, and the center has the location equal to the second operand.",
			usages = { @usage (
					value = "When circle is used with 2 operands, the second one is the center of the created circle.",
					examples = { @example (
							value = "circle(10,{80,30})",
							equals = "a geometry as a circle of radius 10, the center will be in the location {80,30}.",
							test = false) }) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle", "square",
					"triangle" })
	@no_test
	public static IShape circle(final IScope scope, final Double radius, final GamaPoint position) {
		GamaPoint location;
		location = position;
		if (radius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildCircle(radius, location);
	}

	/**
	 * Ellipse.
	 *
	 * @param scope
	 *            the scope
	 * @param xRadius
	 *            the x radius
	 * @param yRadius
	 *            the y radius
	 * @return the i shape
	 */
	@operator (
			value = "ellipse",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "An ellipse geometry which x-radius is equal to the first operand and y-radius is equal to the second operand",
			usages = { @usage (
					value = "returns a point if both operands are lower or equal to 0, a line if only one is.") },
			comment = "the center of the ellipse is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "ellipse(10, 10)",
					equals = "a geometry as an ellipse of width 10 and height 10.",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle", "square",
					"circle", "squircle", "triangle" })
	@no_test // (comment="See Creation.experiment in test models : {Ellipse tests}")
	public static IShape ellipse(final IScope scope, final Double xRadius, final Double yRadius) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (xRadius <= 0 && yRadius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildEllipse(xRadius, yRadius, location);
	}

	/**
	 * Squicle.
	 *
	 * @param scope
	 *            the scope
	 * @param xRadius
	 *            the x radius
	 * @param power
	 *            the power
	 * @return the i shape
	 */
	@operator (
			value = "squircle",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A mix of square and circle geometry (see : http://en.wikipedia.org/wiki/Squircle), which side size is equal to the first operand and power is equal to the second operand",
			usages = { @usage (
					value = "returns a point if the side operand is lower or equal to 0.") },
			comment = "the center of the ellipse is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "squircle(4,4)",
					equals = "a geometry as a squircle of side 4 with a power of 4.",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "super_ellipse",
					"rectangle", "square", "circle", "ellipse", "triangle" })
	@no_test // Because who cares "du cul"
	public static IShape squicle(final IScope scope, final Double xRadius, final Double power) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (xRadius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildSquircle(xRadius, power, location);
	}

	/**
	 * Arc.
	 *
	 * @param scope
	 *            the scope
	 * @param xRadius
	 *            the x radius
	 * @param heading
	 *            the heading
	 * @param amplitude
	 *            the amplitude
	 * @return the i shape
	 */
	@operator (
			value = "arc",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "An arc, which radius is equal to the first operand, heading to the second and amplitude the third",
			masterDoc = false,
			usages = { @usage (
					value = "returns a point if the radius operand is lower or equal to 0.") },
			comment = "the center of the arc is by default the location of the current agent in which has been called this operator. This operator returns a polygon by default.",
			examples = { @example (
					value = "arc(4,45,90)",
					equals = "a geometry as an arc of radius 4, in a direction of 45° and an amplitude of 90°",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "super_ellipse",
					"rectangle", "square", "circle", "ellipse", "triangle" })
	@no_test // (comment="See Creation.experiment in test models : {Arc tests}")
	public static IShape arc(final IScope scope, final Double xRadius, final Double heading,
			final Double amplitude) {
		return arc(scope, xRadius, heading, amplitude, true);
	}

	/**
	 * Arc.
	 *
	 * @param scope
	 *            the scope
	 * @param xRadius
	 *            the x radius
	 * @param heading
	 *            the heading
	 * @param amplitude
	 *            the amplitude
	 * @param filled
	 *            the filled
	 * @return the i shape
	 */
	@operator (
			value = "arc",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "An arc, which radius is equal to the first operand, heading to the second, amplitude to the third and a boolean indicating whether to return a linestring or a polygon to the fourth",
			masterDoc = true,
			usages = { @usage (
					value = "returns a point if the radius operand is lower or equal to 0.") },
			comment = "the center of the arc is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "arc(4,45,90, false)",
					equals = "a geometry as an arc of radius 4, in a direction of 45° and an amplitude of 90°, which only contains the points on the arc",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "super_ellipse",
					"rectangle", "square", "circle", "ellipse", "triangle" })
	@no_test // (comment="See Creation.experiment in test models : {Arc tests}")
	public static IShape arc(final IScope scope, final Double xRadius, final Double heading, final Double amplitude,
			final boolean filled) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (xRadius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildArc(xRadius * 2, heading, amplitude, filled, location);
	}

	/**
	 * Elliptical arc.
	 *
	 * @param scope
	 *            the scope
	 * @param pt1
	 *            the pt 1
	 * @param pt2
	 *            the pt 2
	 * @param h
	 *            the h
	 * @param nPts
	 *            the n pts
	 * @return the i shape
	 */
	@operator (
			value = "elliptical_arc",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "An elliptical arc from the first operand (point) to the second operand (point), which radius is equal to the third operand, and a int giving the number of points to use as a last operand",
			examples = { @example (
					value = "elliptical_arc({0,0},{10,10},5.0, 20)",
					equals = "a geometry from {0,0} to {10,10} considering a radius of 5.0 built using 20 points",
					test = false) },
			see = { "arc", "around", "cone", "line", "link", "norm", "point", "polygon", "polyline",
					"super_ellipse", "rectangle", "square", "circle", "ellipse", "triangle" })
	@no_test // (comment="See Creation.experiment in test models : {Arc tests}")

	public static IShape ellipticalArc(final IScope scope, final GamaPoint pt1, final GamaPoint pt2, final double h,
			final int nPts) {
		double xRadius = pt1.distance(pt2) / 2.0;
		double yRadius = h;

		Coordinate[] pts = new Coordinate[nPts];
		int iPt = 0;
		for (int i = 0; i < nPts; i++) {
			double ang = i * (Math.PI / nPts);
			double x = xRadius * Math.cos(ang);
			double y = yRadius * Math.sin(ang);
			// Interpolate z value between pt1.z and pt2.z based on position along the arc
			double t = (double) i / (nPts - 1); // Normalized position from 0 to 1
			double z = pt1.z + t * (pt2.z - pt1.z);
			pts[iPt++] = new Coordinate(x, y, z);
		}
		IShape shape = GamaShapeFactory.createFrom(GeometryUtils.GEOMETRY_FACTORY.createLineString(pts));
		shape = SpatialTransformations.rotated_by(scope, shape, SpatialRelations.towards(scope, pt2, pt1));
		return SpatialTransformations.translated_by(scope, shape, pt1.minus(shape.getPoints().firstValue(scope)));
	}

	/**
	 * Cross.
	 *
	 * @param scope
	 *            the scope
	 * @param xRadius
	 *            the x radius
	 * @param width
	 *            the width
	 * @return the i shape
	 */
	@operator (
			value = "cross",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cross, which radius is equal to the first operand (and eventually the width of the lines for the second)",
			masterDoc = true,
			examples = { @example (
					value = "cross(10,2)",
					equals = "a geometry as a cross of radius 10, and with a width of 2 for the lines ",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "super_ellipse",
					"rectangle", "square", "circle", "ellipse", "triangle" })
	@no_test
	public static IShape cross(final IScope scope, final Double xRadius, final Double width) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (xRadius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildCross(xRadius, width, location);
	}

	/**
	 * Cross.
	 *
	 * @param scope
	 *            the scope
	 * @param xRadius
	 *            the x radius
	 * @return the i shape
	 */
	@operator (
			value = "cross",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cross, which radius is equal to the first operand",
			examples = { @example (
					value = "cross(10)",
					equals = "a geometry as a cross of radius 10",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "super_ellipse",
					"rectangle", "square", "circle", "ellipse", "triangle" })
	@no_test
	public static IShape cross(final IScope scope, final Double xRadius) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (xRadius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildCross(xRadius, null, location);
	}

	/**
	 * Cylinder.
	 *
	 * @param scope
	 *            the scope
	 * @param radius
	 *            the radius
	 * @param depth
	 *            the depth
	 * @return the i shape
	 */
	@operator (
			value = "cylinder",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A cylinder geometry which radius is equal to the operand.",
			usages = { @usage (
					value = "returns a point if the operand is lower or equal to 0.") },
			comment = "the center of the cylinder is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "cylinder(10,10)",
					equals = "a geometry as a circle of radius 10.",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle", "square",
					"triangle" })
	@no_test // (comment="Dummy init in test models > Creation.experiment")
	public static IShape cylinder(final IScope scope, final Double radius, final Double depth) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (radius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildCylinder(radius, depth, location);
	}

	/**
	 * Sphere.
	 *
	 * @param scope
	 *            the scope
	 * @param radius
	 *            the radius
	 * @return the i shape
	 */
	@operator (
			value = "sphere",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.THREED })
	@doc (
			value = "A sphere geometry which radius is equal to the operand.",
			usages = { @usage (
					value = "returns a point if the operand is lower or equal to 0.") },
			comment = "the centre of the sphere is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "sphere(10)",
					equals = "a geometry as a circle of radius 10 but displays a sphere.",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle", "square",
					"triangle" })
	@no_test // (comment="Dummy init in test models > Creation.experiment")
	public static IShape sphere(final IScope scope, final Double radius) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (radius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildSphere(radius, location);
	}

	/**
	 * Teapot.
	 *
	 * @param scope
	 *            the scope
	 * @param size
	 *            the size
	 * @return the i shape
	 */
	@operator (
			value = "teapot",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.THREED })
	@doc (
			value = "A teapot geometry which radius is equal to the operand.",
			special_cases = { "returns a point if the operand is lower or equal to 0." },
			comment = "the centre of the teapot is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "teapot(10)",
					equals = "a geometry as a circle of radius 10 but displays a teapot.",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle", "square",
					"triangle" })
	@no_test // (comment="Dummy init in test models > Creation.experiment")
	public static IShape teapot(final IScope scope, final Double size) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (size <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildTeapot(size, location);
	}

	/**
	 * Cone.
	 *
	 * @param scope
	 *            the scope
	 * @param p1
	 *            the p 1
	 * @param p2
	 *            the p 2
	 * @return the i shape
	 */
	@operator (
			value = "cone",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A cone geometry which min and max angles are given by the operands.",
			masterDoc = true,
			special_cases = { "returns nil if the operand is nil." },
			comment = "the center of the cone is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "cone(0, 45)",
					equals = "a geometry as a cone with min angle is 0 and max angle is 45.",
					test = false) },
			see = { "around", "circle", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"square", "triangle" })
	@no_test // no idea how to test a cone
	@depends_on (IKeyword.SHAPE)
	public static IShape cone(final IScope scope, final Integer p1, final Integer p2) {
		if (p1 == null || p2 == null) return null;
		final Double min_angle = Maths.checkHeading(p1);
		final Double max_angle = Maths.checkHeading(p2);
		final IAgent a = scope.getAgent();
		final GamaPoint origin = a.getLocation() == null ? new GamaPoint(0, 0) : a.getLocation();
		final double originx = origin.getX();
		final double originy = origin.getY();
		final double worldWidth = scope.getTopology().getWidth();// -
																	// originx;
		final double worldHeight = scope.getTopology().getHeight();// -
																	// originy;
		final double max = Math.max(worldWidth, worldHeight);
		final double min_point_x = originx + Maths.cos(min_angle) * max;
		final double min_point_y = originy + Maths.sin(min_angle) * max;
		final GamaPoint minPoint = new GamaPoint(min_point_x, min_point_y);

		final double max_point_x = originx + Maths.cos(max_angle) * max;
		final double max_point_y = originy + Maths.sin(max_angle) * max;
		final GamaPoint maxPoint = new GamaPoint(max_point_x, max_point_y);

		return polygon(scope, GamaListFactory.wrap(Types.POINT, origin, minPoint, maxPoint));
	}

	/**
	 * Cone.
	 *
	 * @param scope
	 *            the scope
	 * @param p
	 *            the p
	 * @return the i shape
	 */
	@operator (
			value = "cone",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cone geometry which min and max angles are given by the operands.",
			examples = { @example (
					value = "cone({0, 45})",
					equals = "a geometry as a cone with min angle is 0 and max angle is 45.",
					test = false) },
			see = { "around", "circle", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"square", "triangle" })
	@no_test // no idea how to test a cone
	public static IShape cone(final IScope scope, final GamaPoint p) {
		if (p == null) return null;
		return cone(scope, (int) p.x, (int) p.y);
	}

	/**
	 * Cone 3 D.
	 *
	 * @param scope
	 *            the scope
	 * @param radius
	 *            the radius
	 * @param height
	 *            the height
	 * @return the i shape
	 */
	@operator (
			value = "cone3D",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.THREED })
	@doc (
			value = "A cone geometry which base radius size is equal to the first operand, and which the height is equal to the second operand.",
			special_cases = { "returns a point if the operand is lower or equal to 0." },
			comment = "the center of the cone is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "cone3D(10.0,5.0)",
					equals = "a geometry as a cone with a base circle of radius 10 and a height of 5.",
					test = false) },
			see = { "around", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle", "square",
					"triangle" })
	@no_test
	public static IShape cone3D(final IScope scope, final Double radius, final Double height) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (radius <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildCone3D(radius, height, location);
	}

	/**
	 * Square.
	 *
	 * @param scope
	 *            the scope
	 * @param side_size
	 *            the side size
	 * @return the i shape
	 */
	@operator (
			value = "square",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A square geometry which side size is equal to the operand.",
			usages = { @usage (
					value = "returns nil if the operand is nil.") },
			comment = "the centre of the square is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "square(10)",
					equals = "a geometry as a square of side size 10.",
					test = false),
					@example (
							value = "var0.area",
							equals = "100.0",
							returnType = "float") },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"triangle" })
	@test ("square(10).area = 100")
	public static IShape square(final IScope scope, final Double side_size) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (side_size <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildSquare(side_size, location);
	}

	/**
	 * Cube.
	 *
	 * @param scope
	 *            the scope
	 * @param side_size
	 *            the side size
	 * @return the i shape
	 */
	@operator (
			value = "cube",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.THREED })
	@doc (
			value = "A cube geometry which side size is equal to the operand.",
			usages = { @usage (
					value = "returns nil if the operand is nil.") },
			comment = "the center of the cube is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "cube(10)",
					equals = "a geometry as a square of side size 10.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"triangle" })
	@test ("cube(10).volume = 1000")
	public static IShape cube(final IScope scope, final Double side_size) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (side_size <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildCube(side_size, location);
	}

	/**
	 * Rectangle.
	 *
	 * @param scope
	 *            the scope
	 * @param p
	 *            the p
	 * @return the i shape
	 */
	@operator (
			value = "rectangle",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A rectangle geometry which side sizes are given by the operands (as a point).",
			comment = "the center of the rectangle is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "rectangle({10, 5})",
					equals = "a geometry as a rectangle with width = 10 and height = 5.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "square",
					"triangle" })
	@test ("rectangle({10, 5}).area = 50.0")
	public static IShape rectangle(final IScope scope, final GamaPoint p) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		return GamaGeometryType.buildRectangle(p.x, p.y, location);
	}

	/**
	 * Rectangle.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the i shape
	 */
	@operator (
			value = "rectangle",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A rectangle geometry, computed from the operands values (e.g. the 2 side sizes).",
			masterDoc = true,
			usages = { @usage (
					value = "returns nil if the operand is nil.") },
			comment = "the center of the rectangle is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "rectangle(10, 5)",
					equals = "a geometry as a rectangle with width = 10 and height = 5.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "square",
					"triangle" })
	@test ("rectangle(10, 5).area = 50.0")
	public static IShape rectangle(final IScope scope, final double x, final double y) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		return GamaGeometryType.buildRectangle(x, y, location);
	}

	/**
	 * Rectangle.
	 *
	 * @param scope
	 *            the scope
	 * @param upperLeftCorner
	 *            the upper left corner
	 * @param lowerRightCorner
	 *            the lower right corner
	 * @return the i shape
	 */
	@operator (
			value = "rectangle",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A rectangle geometry which upper-left and lower-right corners are defined as points.",
			examples = { @example (
					value = "rectangle({0.0,0.0}, {10.0,10.0})",
					equals = "a geometry as a rectangle with {1.0,1.0} as the upper-left corner, {10.0,10.0} as the lower-right corner.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "square",
					"triangle" })
	@test ("rectangle({0.0,0.0}, {10.0,10.0}).area = 100.0")
	public static IShape rectangle(final IScope scope, final GamaPoint upperLeftCorner,
			final GamaPoint lowerRightCorner) {
		GamaPoint location;
		final double width = Math.abs(upperLeftCorner.x - lowerRightCorner.x);
		final double height = Math.abs(upperLeftCorner.y - lowerRightCorner.y);
		final GamaPoint realTopLeftCorner = new GamaPoint(Math.min(upperLeftCorner.x, lowerRightCorner.x),
				Math.min(upperLeftCorner.y, lowerRightCorner.y));
		location = new GamaPoint(realTopLeftCorner.x + width / 2, realTopLeftCorner.y + height / 2);
		return GamaGeometryType.buildRectangle(width, height, location);
	}

	/**
	 * Box.
	 *
	 * @param scope
	 *            the scope
	 * @param p
	 *            the p
	 * @return the i shape
	 */
	@operator (
			value = "box",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.THREED })
	@doc (
			value = "A box geometry which side sizes are given by the operands.",
			masterDoc = true,
			usages = { @usage (
					value = "returns nil if the operand is nil.") },
			comment = "the center of the box is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "box({10, 5 , 5})",
					equals = "a geometry as a rectangle with width = 10, height = 5 depth= 5.",
					test = false),
					@example (
							value = " (box({10, 10 , 5}) at_location point(50,50,0)).location.y",
							equals = "50.0",
							returnType = "float") },
			see = { "around", "circle", "sphere", "cone", "line", "link", "norm", "point", "polygon", "polyline",
					"square", "cube", "triangle" })
	public static IShape box(final IScope scope, final GamaPoint p) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		return GamaGeometryType.buildBox(p.x, p.y, p.z, location);
	}

	/**
	 * Box.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @return the i shape
	 */
	@operator (
			value = "box",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = {})
	@doc (
			value = "A box geometry which side sizes are given by the operands.",
			usages = { @usage (
					value = "returns nil if the operand is nil.") },
			comment = "the center of the box is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "box(10, 5 , 5)",
					equals = "a geometry as a rectangle with width = 10, height = 5 depth= 5.",
					test = false) },
			see = { "around", "circle", "sphere", "cone", "line", "link", "norm", "point", "polygon", "polyline",
					"square", "cube", "triangle" })
	@test ("box(10,5,5).volume = 250")
	public static IShape box(final IScope scope, final double x, final double y, final double z) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		return GamaGeometryType.buildBox(x, y, z, location);
	}

	/**
	 * Triangle.
	 *
	 * @param scope
	 *            the scope
	 * @param side_size
	 *            the side size
	 * @return the i shape
	 */
	@operator (
			value = "triangle",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A triangle geometry which side size is given by the operand.",
			usages = { @usage ("returns nil if the operand is nil.") },
			comment = "the center of the triangle is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "triangle(5)",
					equals = "a geometry as a triangle with side_size = 5.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"square" })
	@no_test
	public static IShape triangle(final IScope scope, final Double side_size) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (side_size <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildTriangle(side_size, location);
	}

	/**
	 * Triangle.
	 *
	 * @param scope
	 *            the scope
	 * @param base
	 *            the base
	 * @param height
	 *            the height
	 * @return the i shape
	 */
	@operator (
			value = "triangle",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A triangle geometry which the base and height size are given by the operand.",
			usages = { @usage ("returns nil if one of the operand is nil.") },
			comment = "the center of the triangle is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "triangle(5, 10)",
					equals = "a geometry as a triangle with a base of 5m and a height of 10m.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"square" })
	@no_test
	public static IShape triangle(final IScope scope, final Double base, final Double height) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (base <= 0 || height <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildTriangle(base, height, location);
	}

	/**
	 * Pyramid.
	 *
	 * @param scope
	 *            the scope
	 * @param side_size
	 *            the side size
	 * @return the i shape
	 */
	@operator (
			value = "pyramid",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.THREED })
	@doc (
			value = "A square geometry which side size is given by the operand.",
			usages = { @usage (
					value = "returns nil if the operand is nil.") },
			comment = "the center of the pyramid is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "pyramid(5)",
					equals = "a geometry as a square with side_size = 5.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"square" })
	@no_test
	public static IShape pyramid(final IScope scope, final Double side_size) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (side_size <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildPyramid(side_size, location);
	}

	/**
	 * Hexagon.
	 *
	 * @param scope
	 *            the scope
	 * @param size
	 *            the size
	 * @return the i shape
	 */
	@operator (
			value = "hexagon",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY })
	@doc (
			value = "A hexagon geometry which the given with and height",
			masterDoc = true,
			usages = { @usage ("returns nil if the operand is nil.") },
			comment = "the center of the hexagon is by default the location of the current agent in which has been called this operator.",
			examples = { @example (
					value = "hexagon(10)",
					equals = "a geometry as a hexagon of width of 10 and height of 10.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"triangle" })
	@no_test
	public static IShape hexagon(final IScope scope, final Double size) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (size <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildHexagon(size, location);
	}

	/**
	 * Hexagon.
	 *
	 * @param scope
	 *            the scope
	 * @param size
	 *            the size
	 * @return the i shape
	 */
	@operator (
			value = "hexagon",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.THREED })
	@doc (
			value = "A hexagon geometry which the given width and height",
			examples = { @example (
					value = "hexagon({10,5})",
					equals = "a geometry as a hexagon of width of 10 and height of 5.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"triangle" })
	@no_test
	public static IShape hexagon(final IScope scope, final GamaPoint size) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		final Double width = size.x;
		final Double height = size.y;
		if (width <= 0 || height <= 0) return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildHexagon(width, height, location);
	}

	/**
	 * Hexagon.
	 *
	 * @param scope
	 *            the scope
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @return the i shape
	 */
	@operator (
			value = "hexagon",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE, IOperatorCategory.THREED },
			concept = { IConcept.THREED })
	@doc (
			value = "A hexagon geometry which the given width and height",
			examples = { @example (
					value = "hexagon(10,5)",
					equals = "a geometry as a hexagon of width of 10 and height of 5.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle",
					"triangle" })
	@no_test
	public static IShape hexagon(final IScope scope, final Double width, final Double height) {
		GamaPoint location;
		final IAgent a = scope.getAgent();
		location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		if (width == null || height == null || width <= 0 || height <= 0)
			return GamaShapeFactory.createFrom(location);
		return GamaGeometryType.buildHexagon(width, height, location);
	}

	/**
	 * Polygon.
	 *
	 * @param scope
	 *            the scope
	 * @param points
	 *            the points
	 * @return the i shape
	 */
	@operator (
			value = "polygon",
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.POINT })
	@doc (
			value = "A polygon geometry from the given list of points.",
			usages = { @usage (
					value = "if the operand is nil, returns the point geometry {0,0}"),
					@usage (
							value = "if the operand is composed of a single point, returns a point geometry"),
					@usage (
							value = "if the operand is composed of 2 points, returns a polyline geometry.") },
			examples = { @example (
					value = "polygon([{0,0}, {0,10}, {10,10}, {10,0}])",
					equals = "a polygon geometry composed of the 4 points.",
					test = false),
					@example (
							value = "polygon([{0,0}, {0,10}, {10,10}, {10,0}]).area",
							equals = "100.0",
							returnType = "float"),
					@example (
							value = "polygon([{0,0}, {0,10}, {10,10}, {10,0}]).location",
							equals = "point(5.0,5.0,0.0)",
							returnType = "point") },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polyline", "rectangle", "square",
					"triangle" })
	@no_test
	public static IShape polygon(final IScope scope, final IContainer<?, ? extends IShape> points) {
		if (points == null || points.isEmpty(scope)) return GamaShapeFactory.createFrom(new GamaPoint(0, 0));
		// final IList<IShape> shapes = points.listValue(scope); Now
		// replaced by a copy of the list (see Issue 740)
		final IList<IShape> shapes = GamaListFactory.create(scope, Types.GEOMETRY, points);
		final int size = shapes.length(scope);
		final IShape first = shapes.firstValue(scope);
		if (size == 1) return GamaGeometryType.createPoint(first);
		if (size == 2) return GamaGeometryType.buildLine(first, shapes.lastValue(scope));
		if (!first.equals(shapes.lastValue(scope))) { shapes.add(first); }
		return GamaGeometryType.buildPolygon(shapes);
	}

	/**
	 * Polyhedron.
	 *
	 * @param scope
	 *            the scope
	 * @param points
	 *            the points
	 * @param depth
	 *            the depth
	 * @return the i shape
	 */
	@operator (
			value = "polyhedron",
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.POINT })
	@doc (
			value = "A polyhedron geometry from the given list of points.",
			usages = { @usage (
					value = "if the operand is nil, returns the point geometry {0,0}"),
					@usage (
							value = "" + "if the operand is composed of a single point, returns a point geometry"),
					@usage (
							value = "if the operand is composed of 2 points, returns a polyline geometry.") },
			examples = { @example (
					value = "polyhedron([{0,0}, {0,10}, {10,10}, {10,0}],10)",
					equals = "a polygon geometry composed of the 4 points and of depth 10.",
					test = false) },
			see = { "around", "circle", "cone", "line", "link", "norm", "point", "polyline", "rectangle", "square",
					"triangle" })
	@no_test
	public static IShape polyhedron(final IScope scope, final IContainer<?, IShape> points, final Double depth) {
		if (points == null || points.isEmpty(scope)) return GamaShapeFactory.createFrom(new GamaPoint(0, 0));
		// final IList<IShape> shapes = points.listValue(scope); Now
		// replaced by a copy of the list (see Issue 740)
		final IList<IShape> shapes = GamaListFactory.create(scope, Types.POINT, points);
		final int size = shapes.length(scope);
		final IShape first = shapes.firstValue(scope);
		if (size == 1) return GamaGeometryType.createPoint(first);
		final IShape last = shapes.lastValue(scope);
		if (size == 2) return GamaGeometryType.buildLine(first, last);
		if (!first.equals(last)) { shapes.add(first); }
		return GamaGeometryType.buildPolyhedron(shapes, depth);
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.POINT })
	@doc (
			value = "The operator computes a Bezier curve geometry between the given operators, with 10 or a given number of points, and from left to rigth or right to left.",
			masterDoc = true,
			usages = { @usage (
					value = "if one  of the operand is nil, returns nil"),
					@usage (
							value = "When used with 3 points, it computes a quadratic Bezier curve geometry built from the three given points and composed of 10 points.",
							examples = { @example (
									value = "curve({0,0}, {0,10}, {10,10})",
									equals = "a quadratic Bezier curve geometry composed of 10 points from p0 to p2.",
									test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1,
			final GamaPoint p2) {
		if (p0 == null || p1 == null || p2 == null) return null;
		return GamaGeometryType.buildPolyline(quadraticBezierCurve(p0, p1, p2, 10));
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param nbPoints
	 *            the nb points
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A quadratic Bezier curve geometry built from the three given points composed of a given numnber of points.",
			usages = { @usage (
					value = "When used with 3 points and an integer, it  computes a quadratic Bezier curve geometry built from the three given points. If the last operand (number of points) is inferior to 2, returns nil",
					examples = { @example (
							value = "curve({0,0}, {0,10}, {10,10}, 20)",
							equals = "a quadratic Bezier curve geometry composed of 20 points from p0 to p2.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1, final GamaPoint p2,
			final int nbPoints) {
		if (p0 == null || p1 == null || p2 == null || nbPoints < 2) return null;
		return GamaGeometryType.buildPolyline(quadraticBezierCurve(p0, p1, p2, nbPoints));
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param p3
	 *            the p3
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the four given points composed of 10 points.",
			usages = { @usage (
					value = "When used with 4 points, it computes, it computes a cubic Bezier curve geometry built from the four given points and composed of 10 points. ",
					examples = { @example (
							value = "curve({0,0}, {0,10}, {10,10})",
							equals = "a cubic Bezier curve geometry composed of 10 points from p0 to p3.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1, final GamaPoint p2,
			final GamaPoint p3) {
		if (p0 == null || p1 == null || p2 == null || p3 == null) return null;
		return GamaGeometryType.buildPolyline(cubicBezierCurve(p0, p1, p2, p3, 10));
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param P0
	 *            the p0
	 * @param P1
	 *            the p1
	 * @param coefficient
	 *            the coefficient
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of 10 points.",
			usages = { @usage (
					value = "When used with 2 points and a float coefficient, it computes a cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of 10 points.",
					examples = { @example (
							value = "curve({0,0},{10,10}, 0.5)",
							equals = "a cubic Bezier curve geometry composed of 10 points from p0 to p1.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint P0, final GamaPoint P1,
			final Double coefficient) {
		return bezierCurve(scope, P0, P1, coefficient, true, 10, 0.5);
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param coefficient
	 *            the coefficient
	 * @param right
	 *            the right
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of 10 points - the last boolean is used to specified if it is the right side.",
			usages = { @usage (
					value = "When used with 2 points, a float coefficient and a boolean, it computes a cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of 10 points. The last boolean is used to specified if it is the right side.",
					examples = { @example (
							value = "curve({0,0},{10,10}, 0.5, false)",
							equals = "a cubic Bezier curve geometry composed of 10 points from p0 to p1 at the left side.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1,
			final Double coefficient, final boolean right) {
		return bezierCurve(scope, p0, p1, coefficient, right, 10, 0.5);
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param coefficient
	 *            the coefficient
	 * @param right
	 *            the right
	 * @param nbPoints
	 *            the nb points
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of the given number of points - the boolean is used to specified if it is the right side.",
			usages = { @usage (
					value = "When used with 2 points, a float coefficient, a boolean, and an integer number of points, it computes a cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of the given number of points - the boolean is used to specified if it is the right side.",
					examples = { @example (
							value = "curve({0,0},{10,10}, 0.5, false, 100)",
							equals = "a cubic Bezier curve geometry composed of 100 points from p0 to p1 at the right side.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1,
			final Double coefficient, final boolean right, final int nbPoints) {
		return bezierCurve(scope, p0, p1, coefficient, right, nbPoints, 0.5);
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param coefficient
	 *            the coefficient
	 * @param right
	 *            the right
	 * @param nbPoints
	 *            the nb points
	 * @param proportion
	 *            the proportion
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of the given number of points - the boolean is used to specified if it is the right side and the last value to indicate where is the inflection point (between 0.0 and 1.0 - default 0.5).",
			usages = { @usage (
					value = "When used with 2 points, a float coefficient, a boolean, an integer number of points, and a float proportion, it computes a cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of the given number of points - the boolean is used to specified if it is the right side and the last value to indicate where is the inflection point (between 0.0 and 1.0 - default 0.5).",
					examples = { @example (
							value = "curve({0,0},{10,10}, 0.5, false, 100, 0.8)",
							equals = "a cubic Bezier curve geometry composed of 100 points from p0 to p1 at the right side.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1,
			final Double coefficient, final boolean right, final int nbPoints, final double proportion) {
		if (p0 == null || p1 == null) return null;
		GamaPoint p01 = new GamaPoint(p0.x + (p1.x - p0.x) * proportion, p0.y + (p1.y - p0.y) * proportion,
				p0.z + (p1.z - p0.z) * proportion);
		final double val = coefficient * p0.euclidianDistanceTo(p1);
		final double heading = SpatialRelations.towards(scope, p0, p1);
		p01 = new GamaPoint(p01.x + Maths.cos(heading + 90 * (right ? 1.0 : -1.0)) * val,
				p01.y + Maths.sin(heading + 90 * (right ? 1.0 : -1.0)) * val, p01.z);
		return bezierCurve(scope, p0, p01, p1, nbPoints);
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param coefficient
	 *            the coefficient
	 * @param nbPoints
	 *            the nb points
	 * @param proportion
	 *            the proportion
	 * @param angle
	 *            the angle
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of the given number of points, considering the given inflection point (between 0.0 and 1.0 - default 0.5), and the given rotation angle (90 = along the z axis).",
			usages = { @usage (
					value = "When used with 2 points, a float coefficient, a boolean, an integer number of points, a float proportion, and a float angle, it computes a cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of the given number of points, considering the given inflection point (between 0.0 and 1.0 - default 0.5), and the given rotation angle (90 = along the z axis).",
					examples = { @example (
							value = "curve({0,0},{10,10}, 0.5, 100, 0.8, 90)",
							equals = "a cubic Bezier curve geometry composed of 100 points from p0 to p1 at the right side.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1,
			final Double coefficient, final int nbPoints, final double proportion, final double angle) {
		if (p0 == null || p1 == null) return null;
		IShape shape = bezierCurve(scope, p0, p1, coefficient, false, nbPoints, proportion);
		shape = SpatialTransformations.rotated_by(scope, shape, angle,
				new GamaPoint(p0.x - p1.x, p0.y - p1.y, p0.z - p1.z));
		if (shape == null) return null;
		final GamaPoint newPt0 = shape.getPoints().get(0);
		return SpatialTransformations.translated_by(scope, shape,
				new GamaPoint(p0.x - newPt0.x, p0.y - newPt0.y, p0.z - newPt0.z));
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param coefficient
	 *            the coefficient
	 * @param nbPoints
	 *            the nb points
	 * @param angle
	 *            the angle
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of the given number of points, considering the given rotation angle (90 = along the z axis).",
			usages = { @usage (
					value = "When used with 2 points, a float coefficient, a boolean, an integer number of points, and a float angle, it computes a cubic Bezier curve geometry built from the two given points with the given coefficient for the radius and composed of the given number of points, considering the given rotation angle (90 = along the z axis).",
					examples = { @example (
							value = "curve({0,0},{10,10}, 0.5, 100, 90)",
							equals = "a cubic Bezier curve geometry composed of 100 points from p0 to p1 at the right side.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1,
			final Double coefficient, final int nbPoints, final double angle) {
		return bezierCurve(scope, p0, p1, coefficient, nbPoints, 0.5, angle);
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param coefficient
	 *            the coefficient
	 * @param angle
	 *            the angle
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the two given points with the given coefficient for the radius considering the given rotation angle (90 = along the z axis).",
			usages = { @usage (
					value = "When used with 2 points, a float coefficient, and a float angle, it computes a cubic Bezier curve geometry built from the two given points with the given coefficient for the radius considering the given rotation angle (90 = along the z axis).",
					examples = { @example (
							value = "curve({0,0},{10,10}, 0.5, 90)",
							equals = "a cubic Bezier curve geometry composed of 100 points from p0 to p1 at the right side.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1,
			final Double coefficient, final double angle) {
		return bezierCurve(scope, p0, p1, coefficient, 10, 0.5, angle);
	}

	/**
	 * Bezier curve.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param p3
	 *            the p3
	 * @param nbPoints
	 *            the nb points
	 * @return the i shape
	 */
	@operator (
			value = { "curve" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = {})
	@doc (
			value = "A cubic Bezier curve geometry built from the four given points composed of a given number of points.",
			usages = { @usage (
					value = "When used with 4 points and an integer number of  points, it computes a cubic Bezier curve geometry built from the four given points composed of a given number of points. If the number of points is  lower than 2, it returns nil.",
					examples = { @example (
							value = "curve({0,0}, {0,10}, {10,10})",
							equals = "a cubic Bezier curve geometry composed of 10 points from p0 to p3.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape bezierCurve(final IScope scope, final GamaPoint p0, final GamaPoint p1, final GamaPoint p2,
			final GamaPoint p3, final int nbPoints) {
		if (p0 == null || p1 == null || p2 == null || p3 == null || nbPoints < 2) return null;
		return GamaGeometryType.buildPolyline(cubicBezierCurve(p0, p1, p2, p3, nbPoints));
	}

	/**
	 * Quadratic bezier curve.
	 *
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param nbPoints
	 *            the nb points
	 * @return the list
	 */
	private static List<IShape> quadraticBezierCurve(final GamaPoint p0, final GamaPoint p1, final GamaPoint p2,
			final int nbPoints) {
		final List<IShape> points = new ArrayList<>();
		for (int i = 0; i < nbPoints; i++) {
			final double x = quadraticBezier(p0.x, p1.x, p2.x, (double) i / (nbPoints - 1));
			final double y = quadraticBezier(p0.y, p1.y, p2.y, (double) i / (nbPoints - 1));
			final double z = quadraticBezier(p0.z, p1.z, p2.z, (double) i / (nbPoints - 1));
			points.add(new GamaPoint(x, y, z));
		}
		return points;
	}

	/**
	 * Cubic bezier curve.
	 *
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @param p2
	 *            the p2
	 * @param p3
	 *            the p3
	 * @param nbPoints
	 *            the nb points
	 * @return the list
	 */
	private static List<IShape> cubicBezierCurve(final GamaPoint p0, final GamaPoint p1, final GamaPoint p2,
			final GamaPoint p3, final int nbPoints) {
		final List<IShape> points = new ArrayList<>();
		for (int i = 0; i < nbPoints; i++) {
			final double x = cubicBezier(p0.x, p1.x, p2.x, p3.x, (double) i / (nbPoints - 1));
			final double y = cubicBezier(p0.y, p1.y, p2.y, p3.y, (double) i / (nbPoints - 1));
			final double z = cubicBezier(p0.z, p1.z, p2.z, p3.z, (double) i / (nbPoints - 1));
			points.add(new GamaPoint(x, y, z));
		}
		return points;
	}

	/**
	 * Quadratic bezier.
	 *
	 * @param v0
	 *            the v 0
	 * @param v1
	 *            the v 1
	 * @param v2
	 *            the v 2
	 * @param t
	 *            the t
	 * @return the double
	 */
	private static double quadraticBezier(final double v0, final double v1, final double v2, final double t) {
		return (1 - t) * ((1 - t) * v0 + t * v1) + t * ((1 - t) * v1 + t * v2);
	}

	/**
	 * Cubic bezier.
	 *
	 * @param v0
	 *            the v 0
	 * @param v1
	 *            the v 1
	 * @param v2
	 *            the v 2
	 * @param v3
	 *            the v 3
	 * @param t
	 *            the t
	 * @return the double
	 */
	private static double cubicBezier(final double v0, final double v1, final double v2, final double v3,
			final double t) {
		return Math.pow(1 - t, 3) * v0 + 3 * (1 - t) * (1 - t) * t * v1 + 3 * (1 - t) * t * t * v2
				+ Math.pow(t, 3) * v3;
	}

	/**
	 * Line.
	 *
	 * @param scope
	 *            the scope
	 * @param points
	 *            the points
	 * @return the i shape
	 */
	@operator (
			value = { "line", "polyline" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.GEOMETRY, IConcept.POINT })
	@doc (
			value = "A polyline geometry from the given list of points.",
			masterDoc = true,
			usages = { @usage (
					value = "if the points list operand is nil, returns the point geometry {0,0}"),
					@usage (
							value = "if the points list operand is composed of a single point, returns a point geometry.") },
			examples = { @example (
					value = "polyline([{0,0}, {0,10}, {10,10}])",
					equals = "a polyline geometry composed of the 3 points.",
					test = false),
					@example (
							value = "line([{10,10}, {10,0}])",
							equals = "a line from 2 points.",
							test = false),
					@example (
							value = "string(polyline([{0,0}, {0,10}, {10,10}])+line([{10,10}, {10,0}]))",
							equals = "\"MULTILINESTRING ((0 0, 0 10, 10 10), (10 10, 10 0))\"",
							returnType = "string"), },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle" })
	@test ("points_along(line({0,0},{0,10}),[0.5])[0] = point({0,5})")
	public static IShape line(final IScope scope, final IContainer<?, IShape> points) {
		if (points == null || points.isEmpty(scope)) return GamaShapeFactory.createFrom(new GamaPoint(0, 0));
		final IList<IShape> shapes = points.listValue(scope, Types.NO_TYPE, false);
		final int size = shapes.length(scope);
		final IShape first = shapes.firstValue(scope);
		if (size == 1) return GamaGeometryType.createPoint(first);
		if (size == 2) return GamaGeometryType.buildLine(first, points.lastValue(scope));
		return GamaGeometryType.buildPolyline(shapes);
	}

	/**
	 * Geometry collection.
	 *
	 * @param scope
	 *            the scope
	 * @param geometries
	 *            the geometries
	 * @return the i shape
	 */
	@operator (
			value = { "geometry_collection" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE })
	@doc (
			value = "A geometry collection (multi-geometry) composed of the given list of geometries.",
			usages = { @usage (
					value = "if the operand is nil, returns the point geometry {0,0}"),
					@usage (
							value = "if the operand is composed of a single geometry, returns a copy of the geometry.") },
			examples = { @example (
					value = "geometry_collection([{0,0}, {0,10}, {10,10}, {10,0}])",
					equals = "a geometry composed of the 4 points (multi-point).",
					test = false) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle", "line" })
	@no_test
	public static IShape geometryCollection(final IScope scope, final IContainer<?, IShape> geometries) {
		if (geometries == null || geometries.isEmpty(scope))
			return GamaShapeFactory.createFrom(new GamaPoint(0, 0));
		final IList<IShape> shapes = geometries.listValue(scope, Types.NO_TYPE, false);
		final int size = shapes.length(scope);
		final IShape first = shapes.firstValue(scope);
		if (size == 1) return first.copy(scope);

		return GamaGeometryType.buildMultiGeometry(shapes);
	}

	/**
	 * Line.
	 *
	 * @param scope
	 *            the scope
	 * @param points
	 *            the points
	 * @param radius
	 *            the radius
	 * @return the i shape
	 */
	@operator (
			value = { "line", "polyline" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE })
	@doc (
			value = "A polyline geometry from the given list of points represented as a cylinder of radius r.",
			usages = { @usage (
					value = "if a radius is added, the given list of points represented as a cylinder of radius r",
					examples = { @example (
							value = "polyline([{0,0}, {0,10}, {10,10}, {10,0}],0.2)",
							equals = "a polyline geometry composed of the 4 points.",
							test = false) }) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle" })
	@no_test
	public static IShape line(final IScope scope, final IContainer<?, IShape> points, final double radius) {
		if (points == null || points.isEmpty(scope)) return GamaShapeFactory.createFrom(new GamaPoint(0, 0));
		final IList<IShape> shapes = points.listValue(scope, Types.NO_TYPE, false);
		final int size = shapes.length(scope);
		final IShape first = shapes.firstValue(scope);
		if (size == 1) return GamaGeometryType.createPoint(first);
		if (size == 2) return GamaGeometryType.buildLineCylinder(first, points.lastValue(scope), radius);
		return GamaGeometryType.buildPolylineCylinder(shapes, radius);
	}

	/**
	 * Plan.
	 *
	 * @param scope
	 *            the scope
	 * @param points
	 *            the points
	 * @param depth
	 *            the depth
	 * @return the i shape
	 */
	@operator (
			value = { "plan", "polyplan" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE })
	@doc (
			value = "A polyline geometry from the given list of points.",
			usages = { @usage (
					value = "if the operand is nil, returns the point geometry {0,0}"),
					@usage (
							value = "if the operand is composed of a single point, returns a point geometry.") },
			examples = { @example (
					value = "polyplan([{0,0}, {0,10}, {10,10}, {10,0}],10)",
					equals = "a polyline geometry composed of the 4 points with a depth of 10.",
					test = false) },
			see = { "around", "circle", "cone", "link", "norm", "point", "polygone", "rectangle", "square",
					"triangle" })
	@no_test
	public static IShape plan(final IScope scope, final IContainer<?, IShape> points, final Double depth) {
		if (points == null || points.isEmpty(scope)) return GamaShapeFactory.createFrom(new GamaPoint(0, 0));
		final IList<IShape> shapes = points.listValue(scope, Types.NO_TYPE, false);
		final int size = shapes.length(scope);
		final IShape first = shapes.firstValue(scope);
		if (size == 1) return GamaGeometryType.createPoint(first);
		if (size == 2) return GamaGeometryType.buildPlan(first, shapes.lastValue(scope), depth);
		return GamaGeometryType.buildPolyplan(shapes, depth);
	}

	/**
	 * Link.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "link" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.SHAPE, IConcept.SPATIAL_COMPUTATION, IConcept.GEOMETRY })
	@doc (
			value = "A dynamic line geometry between the location of the two operands",
			usages = {
					@usage ("if one of the operands is nil, link returns a point geometry at the location of the other. If both are null, it returns a point geometry at {0,0}"), },
			comment = "The geometry of the link is a line between the locations of the two operands, which is built and maintained dynamically ",
			examples = { @example (
					value = "link (geom1,geom2)",
					equals = "a link geometry between geom1 and geom2.",
					isExecutable = false) },
			see = { "around", "circle", "cone", "line", "norm", "point", "polygon", "polyline", "rectangle",
					"square", "triangle" })
	@no_test
	public static IShape link(final IScope scope, final IShape source, final IShape target)
			throws GamaRuntimeException {
		if (source == null) {
			if (target == null) return new GamaPoint(0, 0);
			return GamaGeometryType.createPoint(target.getLocation());
		}
		if (target == null) return GamaGeometryType.createPoint(source.getLocation());
		return GamaGeometryType.buildLink(scope, source, target);
	}

	/**
	 * Around.
	 *
	 * @param scope
	 *            the scope
	 * @param width
	 *            the width
	 * @param toBeCastedIntoGeometry
	 *            the to be casted into geometry
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "around",
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION })
	@doc (
			value = "A geometry resulting from the difference between a buffer around the right-operand casted in geometry at a distance left-operand (right-operand buffer left-operand) and the right-operand casted as geometry.",
			usages = { @usage (
					value = "returns a circle geometry of radius right-operand if the left-operand is nil") },
			examples = { @example (
					value = "10 around circle(5)",
					equals = "the ring geometry between 5 and 10.",
					test = false) },
			see = { "circle", "cone", "line", "link", "norm", "point", "polygon", "polyline", "rectangle", "square",
					"triangle" })
	@no_test
	public static IShape around(final IScope scope, final Double width, final Object toBeCastedIntoGeometry)
			throws GamaRuntimeException {
		final IShape g = Cast.asGeometry(scope, toBeCastedIntoGeometry, false);
		if (g == null) return circle(scope, width);
		return SpatialOperators.minus(scope, SpatialTransformations.enlarged_by(scope, g, width), g);
	}

	/*
	 * @operator("envelope")
	 *
	 * @doc(value =
	 * "A rectangular 3D geometry that represents the rectangle that surrounds the geometries or the surface described by the arguments. More general than geometry(arguments).envelope, as it allows to pass int, double, point, image files, shape files, asc files, or any list combining these arguments, in which case the envelope will be correctly expanded. If an envelope cannot be determined from the arguments, a default one of dimensions (0,100, 0, 100, 0, 100) is returned"
	 * ) public static IShape envelope(final IScope scope, final Object obj) { Envelope3D env = new
	 * Envelope3D(GeometryUtils.computeEnvelopeFrom(scope, obj)); if ( env.isNull() ) { env = new Envelope3D(0, 100,
	 * 0, 100, 0, 100); } final IShape shape = GamaGeometryType.buildBox(env.getWidth(), env.getHeight(),
	 * env.getDepth(), new GamaPoint(env.centre())); return shape; }
	 */

	/**
	 * Envelope.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i shape
	 */
	@operator (
			value = "envelope",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SHAPE },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION })
	@doc (
			value = "A 3D geometry that represents the box that surrounds the geometries or the surface described by the arguments. More general than geometry(arguments).envelope, as it allows to pass int, double, point, image files, shape files, asc files, or any list combining these arguments, in which case the envelope will be correctly expanded. If an envelope cannot be determined from the arguments, a default one of dimensions (0,100, 0, 100, 0, 100) is returned",
			usages = { @usage (
					value = "This operator is often used to define the environment of simulation") },
			examples = { @example (
					value = "file road_shapefile <- file(\"../includes/roads.shp\");",
					isExecutable = false),
					@example (
							value = "geometry shape <- envelope(road_shapefile);",
							isExecutable = false),
					@example (
							value = "// shape is the system variable of  the environment",
							isExecutable = false),
					@example (
							value = "polygon([{0,0}, {20,0}, {10,10}, {10,0}])",
							equals = "create a polygon to get the envolpe",
							test = false),
					@example (
							value = "envelope(polygon([{0,0}, {20,0}, {10,10}, {10,0}])).area",
							equals = "200.0",
							returnType = "float") }

	)
	public static IShape envelope(final IScope scope, final Object obj) {
		Envelope3D env = Envelope3D.of(GeometryUtils.computeEnvelopeFrom(scope, obj));
		try {
			if (env.isNull()) { env = Envelope3D.of(0, 100, 0, 100, 0, 100); }
			return GamaGeometryType.buildBox(env.getWidth(), env.getHeight(), env.getDepth(), env.centre());
		} finally {
			env.dispose();
		}
	}
}
