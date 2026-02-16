/*******************************************************************************************************
 *
 * IShapeFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.geometry;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IEnvelope;

/**
 *
 */
public interface IShapeFactory {

	/**
	 * A predefined angle (approx 0.423 radians) often used for arrow head calculations.
	 */
	double theta = Math.tan(0.423d);

	/**
	 * Creates a new {@link IShape} from a JTS {@link Geometry}.
	 *
	 * @param geom
	 *            the source JTS geometry.
	 * @return the new {@link IShape}.
	 */
	IShape createFrom(Geometry geom);

	/**
	 * Creates an empty {@link IShape}.
	 *
	 * @return a new empty {@link IShape}.
	 */
	IShape create();

	/**
	 * Creates a new {@link IShape} representing the bounding box of a {@link IEnvelope}.
	 *
	 * @param env
	 *            the envelope.
	 * @return the new {@link IShape}.
	 */
	IShape createFrom(IEnvelope env);

	/**
	 * Creates a new {@link IShape} as a copy of another {@link IShape}. The inner geometry is copied, and attributes
	 * are typically preserved.
	 *
	 * @param source
	 *            the source shape.
	 * @return the new {@link IShape}.
	 */
	IShape createFrom(IShape source);

	/**
	 * Builds a polygon from a list of points. Ensures the ring is closed and valid.
	 *
	 * @param points
	 *            the list of vertices.
	 * @return the created polygon shape.
	 */
	IShape buildPolygon(List<? extends IShape> points);

	/**
	 * Builds a MultiPolygon from a list of list of points (each list forming a polygon).
	 *
	 * @param lpoints
	 *            the list of polygon vertices.
	 * @return the created multi-polygon shape.
	 */
	// A.G 28/05/2015 ADDED for gamanalyser
	IShape buildMultiPolygon(List<List<IShape>> lpoints);

	/**
	 * Builds a triangle with specified base, height, and location.
	 *
	 * @param base
	 *            the length of the base.
	 * @param height
	 *            the height of the triangle.
	 * @param location
	 *            the center location of the triangle.
	 * @return the triangle shape.
	 */
	IShape buildTriangle(double base, double height, IPoint location);

	/**
	 * Builds an equilateral triangle defined by side length and location.
	 *
	 * @param side_size
	 *            the length of a side.
	 * @param location
	 *            the center location.
	 * @return the triangle shape.
	 */
	IShape buildTriangle(double side_size, IPoint location);

	/**
	 * Builds a rectangle.
	 *
	 * @param width
	 *            the width of the rectangle.
	 * @param height
	 *            the height of the rectangle.
	 * @param location
	 *            the center location.
	 * @return the rectangle shape.
	 */
	IShape buildRectangle(double width, double height, IPoint location);

	/**
	 * Builds a polyhedron (3D polygon) with a specified depth.
	 *
	 * @param points
	 *            the base polygon vertices.
	 * @param depth
	 *            the extrusion depth.
	 * @return the polyhedron shape.
	 */
	IShape buildPolyhedron(List<IShape> points, Double depth);

	/**
	 * Builds a line segment from the origin to a location.
	 *
	 * @param location2
	 *            the end point.
	 * @return the line shape.
	 */
	IShape buildLine(IShape location2);

	/**
	 * Builds a line segment between two locations.
	 *
	 * @param location1
	 *            the start point.
	 * @param location2
	 *            the end point.
	 * @return the line shape.
	 */
	IShape buildLine(IShape location1, IShape location2);

	/**
	 * Builds a cylinder following a line segment (like a pipe).
	 *
	 * @param location1
	 *            the start point.
	 * @param location2
	 *            the end point.
	 * @param radius
	 *            the radius of the cylinder.
	 * @return the line cylinder shape.
	 */
	IShape buildLineCylinder(IShape location1, IShape location2, double radius);

	/**
	 * Builds a planar shape from a line defined by two points, extruded by depth.
	 *
	 * @param location1
	 *            the start point.
	 * @param location2
	 *            the end point.
	 * @param depth
	 *            the extrusion depth (height of the plane).
	 * @return the plan shape.
	 */
	IShape buildPlan(IShape location1, IShape location2, Double depth);

	/**
	 * Builds a polyline from a list of points.
	 *
	 * @param points
	 *            the vertices of the polyline.
	 * @return the polyline shape.
	 */
	IShape buildPolyline(List<? extends IShape> points);

	/**
	 * Builds a cylinder following a polyline.
	 *
	 * @param points
	 *            the vertices of the polyline.
	 * @param radius
	 *            the radius of the cylinder.
	 * @return the polyline cylinder shape.
	 */
	IShape buildPolylineCylinder(List<IShape> points, double radius);

	/**
	 * Builds a multi-segment plan (polyplan) from a list of points, extruded by depth.
	 *
	 * @param points
	 *            the vertices of the base polyline.
	 * @param depth
	 *            the extrusion depth (height).
	 * @return the polyplan shape.
	 */
	IShape buildPolyplan(List<IShape> points, Double depth);

	/**
	 * Builds a square.
	 *
	 * @param side_size
	 *            the side length.
	 * @param location
	 *            the center location.
	 * @return the square shape.
	 */
	IShape buildSquare(double side_size, IPoint location);

	/**
	 * Builds a cube.
	 *
	 * @param side_size
	 *            the side length of the cube.
	 * @param location
	 *            the center location.
	 * @return the cube shape.
	 */
	IShape buildCube(double side_size, IPoint location);

	/**
	 * Builds a 3D box.
	 *
	 * @param width
	 *            the width.
	 * @param height
	 *            the height.
	 * @param depth
	 *            the depth.
	 * @param location
	 *            the center location.
	 * @return the box shape.
	 */
	IShape buildBox(double width, double height, double depth, IPoint location);

	/**
	 * Builds a regular hexagon.
	 *
	 * @param size
	 *            the size (width/height approximation).
	 * @param x
	 *            the X center coordinate.
	 * @param y
	 *            the Y center coordinate.
	 * @return the hexagon shape.
	 */
	IShape buildHexagon(double size, double x, double y);

	/**
	 * Builds a regular hexagon.
	 *
	 * @param size
	 *            the size.
	 * @param location
	 *            the center location.
	 * @return the hexagon shape.
	 */
	IShape buildHexagon(double size, IPoint location);

	/**
	 * Builds a hexagon with specified width and height.
	 *
	 * @param sizeX
	 *            the width.
	 * @param sizeY
	 *            the height.
	 * @param location
	 *            the center location.
	 * @return the hexagon shape.
	 */
	IShape buildHexagon(double sizeX, double sizeY, IPoint location);

	/**
	 * Builds a circle.
	 *
	 * @param radius
	 *            the radius.
	 * @param location
	 *            the center location.
	 * @return the circle shape.
	 */
	IShape buildCircle(double radius, IPoint location);

	/**
	 * Builds an ellipse.
	 *
	 * @param xRadius
	 *            the horizontal radius (half-width).
	 * @param yRadius
	 *            the vertical radius (half-height).
	 * @param location
	 *            the center location.
	 * @return the ellipse shape.
	 */
	IShape buildEllipse(double xRadius, double yRadius, IPoint location);

	/**
	 * Builds a squircle (superellipse).
	 *
	 * @param xRadius
	 *            the radius/size.
	 * @param power
	 *            the exponent controlling corner roundness.
	 * @param location
	 *            the center location.
	 * @return the squircle shape.
	 */
	IShape buildSquircle(double xRadius, double power, IPoint location);

	/**
	 * Builds an arc using JTS GeometricShapeFactory.
	 *
	 * @param xRadius
	 *            the radius.
	 * @param heading
	 *            the start angle in decimal degrees.
	 * @param amplitude
	 *            the angular extent in decimal degrees.
	 * @param filled
	 *            whether to create a sector (polygon) or just an arc (linestring).
	 * @param location
	 *            the center location.
	 * @return the arc shape.
	 */
	IShape buildArc(double xRadius, double heading, double amplitude, boolean filled, IPoint location);

	/**
	 * Builds a cylinder (vertical).
	 *
	 * @param radius
	 *            the radius.
	 * @param depth
	 *            the height/depth.
	 * @param location
	 *            the center location.
	 * @return the cylinder shape.
	 */
	IShape buildCylinder(double radius, double depth, IPoint location);

	/**
	 * Builds a sphere.
	 *
	 * @param radius
	 *            the radius.
	 * @param location
	 *            the center location.
	 * @return the sphere shape.
	 */
	// FIXME: Be sure that a buffer on a sphere returns a sphere.
	IShape buildSphere(double radius, IPoint location);

	/**
	 * Builds a 3D cone.
	 *
	 * @param radius
	 *            the base radius.
	 * @param depth
	 *            the height.
	 * @param location
	 *            the base center location.
	 * @return the cone shape.
	 */
	IShape buildCone3D(double radius, double depth, IPoint location);

	/**
	 * Builds a teapot (useful for testing/visualization).
	 *
	 * @param size
	 *            the size scaling factor.
	 * @param location
	 *            the location.
	 * @return the teapot shape.
	 */
	IShape buildTeapot(double size, IPoint location);

	/**
	 * Builds a pyramid based on a square base.
	 *
	 * @param side_size
	 *            the base side length.
	 * @param location
	 *            the center location.
	 * @return the pyramid shape.
	 */
	IShape buildPyramid(double side_size, IPoint location);

	/**
	 * Builds an arrow shape starting from (0,0,0) to target.
	 *
	 * @param head
	 *            the target point (tip of the arrow).
	 * @param size
	 *            parameter influencing arrow width/head size.
	 * @return the arrow shape.
	 */
	IShape buildArrow(IPoint head, double size);

	/**
	 * Builds an arrow shape from tail to head.
	 *
	 * @param tail
	 *            the start point.
	 * @param head
	 *            the end point.
	 * @param arrowWidth
	 *            width of the arrow.
	 * @param arrowLength
	 *            length of the arrow head.
	 * @param closed
	 *            whether the arrow is a closed polygon or open lines.
	 * @return the arrow shape.
	 */
	IShape buildArrow(IPoint tail, IPoint head, double arrowWidth, double arrowLength, boolean closed);

	/**
	 * Builds a link (edge) between two shapes as a dynamic geometry.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param source
	 *            the source agent/shape.
	 * @param target
	 *            the target agent/shape.
	 * @return the dynamic link shape.
	 */
	IShape buildLink(IScope scope, IShape source, IShape target);

	/**
	 * Builds a MultiGeometry (collection) from a list of shapes.
	 *
	 * @param shapes
	 *            the list of shapes to combine.
	 * @return the combined multi-geometry shape.
	 */
	IShape buildMultiGeometry(List<IShape> shapes);

	/**
	 * Builds a MultiGeometry from variable arguments.
	 *
	 * @param shapes
	 *            the shapes to combine.
	 * @return the combined multi-geometry shape.
	 */
	IShape buildMultiGeometry(IShape... shapes);

	/**
	 * Builds a cross shape.
	 *
	 * @param xRadius
	 *            the radius (half-size of the cross arms).
	 * @param width
	 *            the thickness of the arms.
	 * @param location
	 *            the center location.
	 * @return the cross shape.
	 */
	IShape buildCross(Double xRadius, Double width, IPoint location);

}