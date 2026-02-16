/*******************************************************************************************************
 *
 * Box2DShapeConverter.java, in gama.extension.physics, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.box2d_version;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.locationtech.jts.geom.LineString;

import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.geometry.IShape.Type;
import gama.api.types.matrix.IField;
import gama.api.utils.geometry.GeometryUtils;
import gama.dev.DEBUG;
import gama.extension.physics.common.IShapeConverter;

/**
 * The Class Box2DShapeConverter.
 */
public class Box2DShapeConverter implements IShapeConverter<Shape, Vec2>, IBox2DPhysicalEntity {

	static {
		DEBUG.OFF();
	}

	/** The scale. */
	final float scale;

	/**
	 * Instantiates a new box 2 D shape converter.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scale
	 *            the scale
	 * @date 1 oct. 2023
	 */
	Box2DShapeConverter(final float scale) {
		this.scale = scale;
	}

	@Override
	public void computeTranslation(final IAgent agent, final Type type, final float depth, final Vec2 aabbTranslation,
			final Vec2 visualTranslation) {
		// Normally not applicable.
	}

	@Override
	public Shape convertShape(final IShape shape, final Type type, final float depth) {

		switch (type) {
			case BOX:
			case PLAN:
			case SQUARE:
			case CUBE:
			case CONE:
			case PYRAMID:
				PolygonShape p = new PolygonShape();
				p.setAsBox(toBox2D(shape.getWidth().floatValue() / 2), toBox2D(shape.getHeight().floatValue() / 2));
				return p;
			case LINECYLINDER:
				// oriented on the Y or on the X (default) axis
				LineString line = (LineString) shape.getInnerGeometry();
				EdgeShape e = new EdgeShape();
				e.set(toVector((IPoint) line.getCoordinateN(0)), toVector((IPoint) line.getCoordinateN(1)));
				return e;
			case SPHERE:
			case CIRCLE:
			case POINT:
			case CYLINDER:
				CircleShape cc = new CircleShape();
				double radius = shape.getWidth().floatValue() / 2;
				// DEBUG.OUT("Creating a circle with radius " + radius);
				cc.setRadius(toBox2D(radius));
				return cc;
			default:
				IPoint[] points = GeometryUtils.getPointsOf(shape);
				switch (points.length) {
					case 0:
						return null;
					case 1:
						return convertShape(shape, IShape.Type.POINT, depth);
					case 2:
						EdgeShape l = new EdgeShape();
						l.set(toVector(points[0]), toVector(points[1]));
						return l;
					default:
						PolygonShape ps = new PolygonShape();
						Vec2[] vertices = new Vec2[points.length];
						for (int i = 0; i < points.length; i++) { vertices[i] = toVector(points[i]); }
						ps.set(vertices, vertices.length);
						return ps;
				}

		}
	}

	@Override
	public Shape convertTerrain(final IScope scope, final IField field, final Double width, final Double height,
			final float depth) {
		// No way to support "depth" here to build the shape :)
		PolygonShape rectangle = new PolygonShape();
		rectangle.setAsBox(width.floatValue(), height.floatValue());
		return rectangle;
	}

	@Override
	public float getScale() { return scale; }

	@Override
	public IPoint toGamaPoint(final Vec2 v) {
		// TODO Auto-generated method stub
		return null;
	}

}
