/*******************************************************************************************************
 *
 * SpatialTransformations.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators.spatial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ITypeProvider;
import gama.annotations.precompiler.Reason;
import gama.core.common.geometry.AxisAngle;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.geometry.GeometryUtils;
import gama.core.common.geometry.Rotation3D;
import gama.core.common.geometry.Scaling3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.grid.GamaSpatialMatrix;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaPair;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.graph.IGraph;
import gama.core.util.matrix.IMatrix;
import gama.gaml.operators.Containers;
import gama.gaml.operators.Graphs;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Transformations.
 */
public class SpatialTransformations {

	/**
	 * Convex hull.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the i shape
	 */
	@operator (
			value = "convex_hull",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A geometry corresponding to the convex hull of the operand.",
			examples = { @example (
					value = "convex_hull(self)",
					equals = "the convex hull of the geometry of the agent applying the operator",
					test = false) })
	@no_test
	public static IShape convex_hull(final IScope scope, final IShape g) {
		return GamaShapeFactory.createFrom(g.getInnerGeometry().convexHull()).withAttributesOf(g);
	}

	/**
	 * Scaled by.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param coefficient
	 *            the coefficient
	 * @return the i shape
	 */
	@operator (
			value = { IKeyword.MULTIPLY, "scaled_by" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			usages = { @usage (
					value = "if the left-hand operand is a geometry and the right-hand operand a float, returns a geometry corresponding to the left-hand operand (geometry, agent, point) scaled by the right-hand operand coefficient",
					examples = {
							// @example(value = "shape * 2",
							// equals = "a geometry corresponding to the geometry of
							// the agent applying the operator scaled by a
							// coefficient of 2",
							@example (
									value = "circle(10) * 2",
									equals = "circle(20)",
									test = false),
							@example (
									value = "(circle(10) * 2).location with_precision 9",
									equals = "(circle(20)).location with_precision 9"),
							@example (
									value = "(circle(10) * 2).height with_precision 9",
									equals = "(circle(20)).height with_precision 9",
									returnType = "float") }) })
	public static IShape scaled_by(final IScope scope, final IShape g, final Double coefficient) {
		return GamaShapeFactory.createFrom(g).withScaling(coefficient);
	}

	/**
	 * Scaled by.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param coefficients
	 *            the coefficients
	 * @return the i shape
	 */
	@operator (
			value = { IKeyword.MULTIPLY, "scaled_by" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if the left-hand operand is a geometry and the right-hand operand a point, returns a geometry corresponding to the left-hand operand (geometry, agent, point) scaled by the right-hand operand coefficients in the 3 dimensions",
					examples = { @example (
							value = "shape * {0.5,0.5,2}",
							equals = "a geometry corresponding to the geometry of the agent applying the operator scaled by a coefficient of 0.5 in x, 0.5 in y and 2 in z",
							test = false) }) })
	@test ("""
			geometry g <- cube (2);\
			float v1 <- g.area * g.height; \
			g <- g * {5, 5, 5};\
			float v2 <- g.area * g.height;  \
			v1 < v2""")
	public static IShape scaled_by(final IScope scope, final IShape g, final GamaPoint coefficients) {
		return GamaShapeFactory.createFrom(g).withScaling(Scaling3D.of(coefficients), false);
	}

	/**
	 * Scaled to.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param bounds
	 *            the bounds
	 * @return the i shape
	 */
	@operator (
			value = { "scaled_to" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "allows to restrict the size of a geometry so that it fits in the envelope {width, height, depth} defined by the second operand",
			examples = { @example (
					value = "shape scaled_to {10,10}",
					equals = "a geometry corresponding to the geometry of the agent applying the operator scaled so that it fits a square of 10x10",
					test = false) })
	@test ("""
			geometry g <- cube (2);\
			float v1 <- g.area * g.height; \
			g <- g scaled_to {20,20};\
			float v2 <- g.area * g.height;  \
			v1 < v2""")
	public static IShape scaled_to(final IScope scope, final IShape g, final GamaPoint bounds) {
		return GamaShapeFactory.createFrom(g).withScaling(Scaling3D.of(bounds), true);
	}

	/**
	 * Enlarged by.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param size
	 *            the size
	 * @param numberOfSegments
	 *            the number of segments
	 * @return the i shape
	 */
	@operator (
			value = { IKeyword.PLUS, "buffer", "enlarged_by" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if the left-hand operand is a geometry and the right-hand operands a float and an integer, returns a geometry corresponding to the left-hand operand (geometry, agent, point) enlarged by the first right-hand operand (distance), using a number of segments equal to the second right-hand operand",
					examples = { @example (
							value = "circle(5) + (5,32)",
							equals = "circle(10)",
							test = false) }) })
	@test ("(circle(5) + (5,32)).height with_precision 5 = 20.0")
	public static IShape enlarged_by(final IScope scope, final IShape g, final Double size,
			final Integer numberOfSegments) {
		if (g == null) return null;
		final Geometry gg = g.getInnerGeometry().buffer(size, numberOfSegments);
		if (gg != null && !gg.isEmpty()) return GamaShapeFactory.createFrom(gg).withAttributesOf(g);
		return null;
	}

	/**
	 * Enlarged by.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param size
	 *            the size
	 * @param numberOfSegments
	 *            the number of segments
	 * @param endCap
	 *            the end cap
	 * @return the i shape
	 */
	@operator (
			value = { IKeyword.PLUS, "buffer", "enlarged_by" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if the left-hand operand is a geometry and the right-hand operands a float, an integer and one of #round, #square or #flat, returns a geometry corresponding to the left-hand operand (geometry, agent, point) enlarged by the first right-hand operand (distance), using a number of segments equal to the second right-hand operand and a flat, square or round end cap style",
					examples = { @example (
							value = "circle(5) + (5,32,#round)",
							equals = "circle(10)",
							test = false) }) })
	@test ("(circle(5) + (5,32,#round)).height with_precision 5 = 20.0")
	public static IShape enlarged_by(final IScope scope, final IShape g, final Double size,
			final Integer numberOfSegments, final Integer endCap) {
		if (g == null) return null;
		final Geometry gg = g.getInnerGeometry().buffer(size, numberOfSegments, endCap);
		if (gg != null && !gg.isEmpty()) return GamaShapeFactory.createFrom(gg).withAttributesOf(g);
		return null;
	}

	/**
	 * Enlarged by.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param size
	 *            the size
	 * @param numberOfSegments
	 *            the number of segments
	 * @param endCap
	 *            the end cap
	 * @param isSingleSided
	 *            the is single sided
	 * @return the i shape
	 */
	@operator (
			value = { IKeyword.PLUS, "buffer", "enlarged_by" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if the left-hand operand is a geometry and the right-hand operands a float, an integer, one of #round, #square or #flat and a boolean, returns a geometry corresponding to the left-hand operand (geometry, agent, point) enlarged by the first right-hand operand (distance), using a number of segments equal to the second right-hand operand and a flat, square or round end cap style and single sided is the boolean is true",
					examples = { @example (
							value = "line([{10,10}, {50,50}]) + (5,32,#round, true)",
							equals = "A ploygon corresponding to the buffer generated",
							test = false) }) })
	@test ("(line([{10,10}, {50,50}]) + (5,32,#round, true)).area with_precision 1 = 282.8")
	public static IShape enlarged_by(final IScope scope, final IShape g, final Double size,
			final Integer numberOfSegments, final Integer endCap, final Boolean isSingleSided) {
		if (g == null) return null;
		BufferParameters param = new BufferParameters(numberOfSegments, endCap);
		param.setSingleSided(isSingleSided);
		Geometry gg = BufferOp.bufferOp(g.getInnerGeometry(), size, param);
		if (gg != null && !gg.isEmpty()) return GamaShapeFactory.createFrom(gg).withAttributesOf(g);
		return null;
	}

	/**
	 * Enlarged by.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param size
	 *            the size
	 * @param isSingleSided
	 *            the is single sided
	 * @return the i shape
	 */
	@operator (
			value = { IKeyword.PLUS, "buffer", "enlarged_by" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if the left-hand operand is a geometry and the right-hand operands a float and a boolean, returns a geometry corresponding to the left-hand operand (geometry, agent, point) enlarged by the first right-hand operand (distance), single sided is the boolean is true",
					examples = { @example (
							value = "line([{10,10}, {50,50}]) + (5, true)",
							equals = "A ploygon corresponding to the buffer generated",
							test = false) }) })
	@test ("(line([{10,10}, {50,50}]) + (5, true)).area with_precision 1 = 282.8")
	public static IShape enlarged_by(final IScope scope, final IShape g, final Double size,
			final Boolean isSingleSided) {
		if (g == null) return null;
		BufferParameters param = new BufferParameters();
		param.setSingleSided(isSingleSided);
		Geometry gg = BufferOp.bufferOp(g.getInnerGeometry(), size, param);
		if (gg != null && !gg.isEmpty()) return GamaShapeFactory.createFrom(gg).withAttributesOf(g);
		return null;
	}

	/**
	 * Enlarged by.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param size
	 *            the size
	 * @return the i shape
	 */
	@operator (
			value = { IKeyword.PLUS, "buffer", "enlarged_by" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = { @usage (
					value = "if the left-hand operand is a geometry and the right-hand operand a float, returns a geometry corresponding to the left-hand operand (geometry, agent, point) enlarged by the right-hand operand distance. The number of segments used by default is 8 and the end cap style is #round",
					examples = { @example (
							value = "circle(5) + 5",
							equals = "circle(10)",
							test = false) }) })
	@test ("(circle(5) + 5).height with_precision 1 = 20.0")
	@test ("(circle(5) + 5).location with_precision 9 = (circle(10)).location with_precision 9")
	public static IShape enlarged_by(final IScope scope, final IShape g, final Double size) {
		if (g == null) return null;
		final Geometry gg = g.getInnerGeometry().buffer(size);
		if (gg != null && !gg.isEmpty()) return GamaShapeFactory.createFrom(gg).withAttributesOf(g);
		return null;
	}

	/**
	 * Reduced by.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param size
	 *            the size
	 * @return the i shape
	 */
	@operator (
			value = { "-", "reduced_by" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			usages = { @usage (
					value = "if the left-hand operand is a geometry and the right-hand operand a float, returns a geometry corresponding to the left-hand operand (geometry, agent, point) reduced by the right-hand operand distance",
					examples = { @example (
							value = "shape - 5",
							equals = "a geometry corresponding to the geometry of the agent applying the operator reduced by a distance of 5",
							test = false) }) })
	@test ("(square(20) - 5).area = 100.0")
	public static IShape reduced_by(final IScope scope, final IShape g, final Double size) {
		if (g == null) return null;
		return enlarged_by(scope, g, -size);
	}

	/**
	 * @throws GamaRuntimeException
	 *             Apply a rotation (of a given angle) to the agent geometry
	 *
	 * @param args
	 *            : angle --: double, degree
	 *
	 */
	@operator (
			value = "rotated_by",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A geometry resulting from the application of a rotation by the right-hand operand angle (degree) to the left-hand operand (geometry, agent, point)",
			masterDoc = true,
			examples = { @example (
					value = "self rotated_by 45",
					equals = "the geometry resulting from a 45 degrees rotation to the geometry of the agent applying the operator.",
					test = false) },
			see = { "transformed_by", "translated_by" })
	@test ("(( square(5) rotated_by 45).width with_precision 2 = 7.07)")
	public static IShape rotated_by(final IScope scope, final IShape g1, final Double angle) {
		if (g1 == null) return null;
		return GamaShapeFactory.createFrom(g1).withRotation(new AxisAngle(angle));
	}

	/**
	 * Inverse rotation.
	 *
	 * @param scope
	 *            the scope
	 * @param rotation
	 *            the rotation
	 * @return the gama pair
	 */
	@operator (
			value = "inverse_rotation",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "The inverse rotation. It is a rotation around the same axis with the opposite angle.",
			masterDoc = true,
			examples = { @example (
					value = "inverse_rotation(38.0::{1,1,1})",
					equals = "-38.0::{1,1,1}",
					test = false) },
			see = { "rotation_composition, normalized_rotation" })
	@test ("inverse_rotation(38.0::{1,1,1}) = (-38.0::{1,1,1})")
	public static GamaPair<Double, GamaPoint> inverse_rotation(final IScope scope,
			final GamaPair<Double, GamaPoint> rotation) {
		return new GamaPair(-rotation.key, rotation.value, Types.FLOAT, Types.POINT);
	}

	/**
	 * Normalized rotation.
	 *
	 * @param scope
	 *            the scope
	 * @param rotation
	 *            the rotation
	 * @return the gama pair
	 */
	@operator (
			value = "normalized_rotation",
			content_type = IType.POINT,
			index_type = IType.FLOAT,
			type = IType.PAIR,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "The rotation normalized according to Euler formalism with a positive angle, such that each rotation has a unique set of parameters (positive angle, normalize axis rotation).",
			masterDoc = true,
			examples = { @example (
					value = "normalized_rotation(-38.0::{1,1,1})",
					equals = "38.0::{-0.5773502691896258,-0.5773502691896258,-0.5773502691896258}",
					test = false) },
			see = { "rotation_composition, inverse_rotation" })
	@test ("normalized_rotation(-38::{1,1,1})=(38.0::{-0.5773502691896258,-0.5773502691896258,-0.5773502691896258})")
	public static GamaPair<Double, GamaPoint> normalized_rotation(final IScope scope, final GamaPair rotation) {
		final GamaPair<Double, GamaPoint> rot = (GamaPair<Double, GamaPoint>) GamaType
				.from(Types.PAIR, Types.FLOAT, Types.POINT).cast(scope, rotation, null, false);
		final GamaPoint axis = rot.getValue();
		final double norm = Math.sqrt(axis.x * axis.x + axis.y * axis.y + axis.z * axis.z);
		axis.x = Math.signum(rot.getKey()) * axis.x / norm;
		axis.y = Math.signum(rot.getKey()) * axis.y / norm;
		axis.z = Math.signum(rot.getKey()) * axis.z / norm;
		return new GamaPair(Math.signum(rot.getKey()) * rot.getKey(), axis, Types.FLOAT, Types.POINT);
	}

	/**
	 * Rotation composition.
	 *
	 * @param scope
	 *            the scope
	 * @param rotation_list
	 *            the rotation list
	 * @return the gama pair
	 */
	@operator (
			value = "rotation_composition",
			content_type = IType.POINT,
			index_type = IType.FLOAT,
			type = IType.PAIR,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "The rotation resulting from the composition of the rotations in the list, from left to right. Angles are in degrees.",
			masterDoc = true,
			examples = { @example (
					value = "rotation_composition([38.0::{1,1,1},90.0::{1,0,0}])",
					equals = "115.22128507898108::{0.9491582126366207,0.31479943993669307,-0.0}",
					test = false) },
			see = { "inverse_rotation" })
	// public static GamaPair<Double, GamaPoint> rotation_composition(final IScope scope,
	// final GamaList<GamaPair<Double, GamaPoint>> rotation_list) {
	// Rotation3D rotation = new Rotation3D(new GamaPoint(1, 0, 0), 0.0);
	// for (GamaPair<Double, GamaPoint> rot : rotation_list) {
	// rotation = rotation.applyTo(new Rotation3D(rot.value, 2 * Math.PI / 360 * rot.key));
	// }
	// return new GamaPair(180 / Math.PI * rotation.getAngle(), rotation.getAxis(), Types.FLOAT, Types.POINT);
	// }
	@test ("normalized_rotation(rotation_composition(38.0::{1,1,1},90.0::{1,0,0}))=normalized_rotation(115.22128507898108::{0.9491582126366207,0.31479943993669307,-0.0})")
	public static GamaPair<Double, GamaPoint> rotation_composition(final IScope scope,
			final IList<GamaPair> rotation_list) {
		Rotation3D rotation = new Rotation3D(new GamaPoint(1, 0, 0), 0.0);
		for (final GamaPair element : rotation_list) {
			final GamaPair<Double, GamaPoint> rot = (GamaPair<Double, GamaPoint>) GamaType
					.from(Types.PAIR, Types.FLOAT, Types.POINT).cast(scope, element, null, false);
			rotation = rotation.applyTo(new Rotation3D(rot.value, 2 * Math.PI / 360 * rot.key));
		}
		return new GamaPair(180 / Math.PI * rotation.getAngle(), rotation.getAxis(), Types.FLOAT, Types.POINT);
	}

	/**
	 * Rotated by.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param rotation
	 *            the rotation
	 * @param vector
	 *            the vector
	 * @return the i shape
	 */
	@operator (
			value = "rotated_by",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A geometry resulting from the application of a rotation by the operand angles (degree)"
					+ " along the operand axis (last operand) to the left-hand operand (geometry, agent, point)",
			masterDoc = true,
			examples = { @example (
					value = "rotated_by(pyramid(10),45.0, {1,0,0})",
					equals = "the geometry resulting from a 45 degrees rotation along the {1,0,0} vector to the geometry of "
							+ "the agent applying the operator.",
					test = false) },
			see = { "transformed_by", "translated_by" })
	@no_test
	public static IShape rotated_by(final IScope scope, final IShape g1, final Double rotation,
			final GamaPoint vector) {
		if (g1 == null) return null;
		if (vector.x == 0d && vector.y == 0d && vector.z == 0d) return g1;
		return GamaShapeFactory.createFrom(g1).withRotation(new AxisAngle(vector, rotation))
				.withLocation(g1.getLocation());
	}

	/**
	 * Rotated by.
	 *
	 * @param scope
	 *            the scope
	 * @param p1
	 *            the p 1
	 * @param rotation
	 *            the rotation
	 * @return the gama point
	 */
	@operator (
			value = "rotated_by",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = {
					@usage ("When used  with a  point and  a pair angle::point, it returns a point resulting from the application of the right-hand rotation operand (angles in degree)"
							+ " to the left-hand operand point") })
	@no_test
	public static GamaPoint rotated_by(final IScope scope, final GamaPoint p1, final GamaPair rotation) {
		if (p1 == null) return null;
		final GamaPair<Double, GamaPoint> rot = (GamaPair<Double, GamaPoint>) GamaType
				.from(Types.PAIR, Types.FLOAT, Types.POINT).cast(scope, rotation, null, false);
		final GamaPoint p2 = new GamaPoint(p1);
		new Rotation3D(rot.getValue(), 2 * Math.PI / 360 * rot.getKey()).applyTo(p2);
		return p2;
	}

	/**
	 * Rotated by.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param rotation
	 *            the rotation
	 * @return the i shape
	 */
	@operator (
			value = "rotated_by",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			examples = { @example (
					value = "rotated_by(pyramid(10),45.0::{1,0,0})",
					equals = "the geometry resulting from a 45 degrees rotation along the {1,0,0} vector to the geometry of "
							+ "the agent applying the operator.",
					test = false) },
			see = { "transformed_by", "translated_by" })
	@no_test
	public static IShape rotated_by(final IScope scope, final IShape g1, final GamaPair rotation) {
		final GamaPair<Double, GamaPoint> rot = (GamaPair<Double, GamaPoint>) GamaType
				.from(Types.PAIR, Types.FLOAT, Types.POINT).cast(scope, rotation, null, false);
		if (g1 == null || rot == null) return null;
		return GamaShapeFactory.createFrom(g1).withRotation(new AxisAngle(rot.getValue(), rot.getKey()))
				.withLocation(g1.getLocation());
	}

	/**
	 * Rotated by.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param angle
	 *            the angle
	 * @return the i shape
	 */
	@operator (
			value = "rotated_by",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = { @usage ("the right-hand operand representing  the angle can be a float or an integer") })
	@no_test
	public static IShape rotated_by(final IScope scope, final IShape g1, final Integer angle) {
		if (g1 == null) return null;
		if (angle == null) return g1.copy(scope);
		// if ( g1.isPoint() ) { return g1.copy(scope); }
		return GamaShapeFactory.createFrom(g1).withRotation(new AxisAngle(angle.doubleValue()));

	}

	/**
	 * @throws GamaRuntimeException
	 *             Apply a affinite operation (of a given coefficient and angle)to the agent geometry. Angle is given by
	 *             the point.x ; Coefficient by the point.y
	 *
	 * @param args
	 *            : coefficient --: double; angle --: double, rad
	 *
	 */
	@operator (
			value = "transformed_by",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A geometry resulting from the application of a rotation and a scaling (right-operand : "
					+ "point {angle(degree), scale factor} of the left-hand operand (geometry, agent, point)",
			examples = { @example (
					value = "self transformed_by {45, 0.5}",
					equals = "the geometry resulting from 45 degrees rotation and 50% scaling of the geometry "
							+ "of the agent applying the operator.",
					test = false) },
			see = { "rotated_by", "translated_by" })
	@no_test
	public static IShape transformed_by(final IScope scope, final IShape g, final GamaPoint p) {
		if (g == null) return null;
		return scaled_by(scope, rotated_by(scope, g, p.x), p.y);
	}

	/**
	 * @throws GamaRuntimeException
	 *             Apply a translation operation (vector (dx, dy)) to the agent geometry
	 *
	 * @param args
	 *            : dx --: double; dy --: double
	 *
	 */
	@operator (
			value = "translated_by",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A geometry resulting from the application of a translation by the right-hand operand distance to the left-hand operand (geometry, agent, point)",
			examples = { @example (
					value = "self translated_by {10,10,10}",
					equals = "the geometry resulting from applying the translation to the left-hand geometry (or agent).",
					test = false) },
			see = { "rotated_by", "transformed_by" })
	@no_test
	public static IShape translated_by(final IScope scope, final IShape g, final GamaPoint p)
			throws GamaRuntimeException {
		if (g == null) return null;
		return at_location(scope, g, gama.gaml.operators.Points.add(g.getLocation(), p));
	}

	/**
	 * At location.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param p
	 *            the p
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { "at_location", "translated_to" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A geometry resulting from the tran of a translation to the right-hand operand point of the left-hand operand (geometry, agent, point)",
			examples = { @example (
					value = "self at_location {10, 20}",
					equals = "the geometry resulting from a translation to the location {10, 20} of the left-hand geometry (or agent).",
					test = false),
					@example (
							value = " (box({10, 10 , 5}) at_location point(50,50,0)).location.x",
							equals = "50.0",
							returnType = "float") })
	public static IShape at_location(final IScope scope, final IShape g, final GamaPoint p)
			throws GamaRuntimeException {
		if (g == null) return null;
		return GamaShapeFactory.createFrom(g).withLocation(p);
	}

	/**
	 * Without holes.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the i shape
	 */
	@operator (
			value = { "without_holes", "solid" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.AGENT_LOCATION })
	@doc (
			value = "A geometry corresponding to the operand geometry (geometry, agent, point) without its holes",
			examples = { @example (
					value = "solid(self)",
					equals = "the geometry corresponding to the geometry of the agent applying the operator without "
							+ "its holes.",
					test = false),
					@example (
							value = "without_holes(polygon([{0,50}, {0,0}, {50,0}, {50,50}, {0,50}]) - square(10) at_location {10,10}).area",
							equals = "2500.0",
							returnType = "float") })
	public static IShape without_holes(final IScope scope, final IShape g) {
		if (g == null) return null;
		final Geometry geom = g.getInnerGeometry();
		Geometry result = geom;
		if (geom instanceof Polygon) {
			result = GeometryUtils.GEOMETRY_FACTORY.createPolygon(GeometryUtils.GEOMETRY_FACTORY
					.createLinearRing(((Polygon) geom).getExteriorRing().getCoordinates()), null);
		} else if (geom instanceof MultiPolygon mp) {
			final Polygon[] polys = new Polygon[mp.getNumGeometries()];
			for (int i = 0; i < mp.getNumGeometries(); i++) {
				final Polygon p = (Polygon) mp.getGeometryN(i);
				polys[i] = GeometryUtils.GEOMETRY_FACTORY.createPolygon(
						GeometryUtils.GEOMETRY_FACTORY.createLinearRing(p.getExteriorRing().getCoordinates()), null);
			}
			result = GeometryUtils.GEOMETRY_FACTORY.createMultiPolygon(polys);
		}
		return GamaShapeFactory.createFrom(result).withAttributesOf(g);
	}

	/**
	 * Skeletonize.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param clippingTolerance
	 *            the clipping tolerance
	 * @param triangulationTolerance
	 *            the triangulation tolerance
	 * @return the i list
	 */
	@operator (
			value = "skeletonize",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			usages = {
					@usage ("It can be used with 2 additional float operands: the tolerances for the clipping and for the triangulation") })
	@no_test
	public static IList<IShape> skeletonize(final IScope scope, final IShape g, final Double clippingTolerance,
			final Double triangulationTolerance) {
		final List<LineString> netw = GeometryUtils.squeletisation(scope, g.getInnerGeometry(), triangulationTolerance,
				clippingTolerance, false);
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		for (final LineString ls : netw) { geoms.add(GamaShapeFactory.createFrom(ls)); }
		return geoms;
	}

	/**
	 * Skeletonize.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param clippingTolerance
	 *            the clipping tolerance
	 * @param triangulationTolerance
	 *            the triangulation tolerance
	 * @param approxiClipping
	 *            the approxi clipping
	 * @return the i list
	 */
	@operator (
			value = "skeletonize",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			usages = {
					@usage ("It can be used with 3 additional float operands: the tolerance for the clipping, the  tolerance for the triangulation, and the approximation for the clipping.") })
	@no_test
	public static IList<IShape> skeletonize(final IScope scope, final IShape g, final Double clippingTolerance,
			final Double triangulationTolerance, final boolean approxiClipping) {
		final List<LineString> netw = GeometryUtils.squeletisation(scope, g.getInnerGeometry(), triangulationTolerance,
				clippingTolerance, approxiClipping);
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		for (final LineString ls : netw) { geoms.add(GamaShapeFactory.createFrom(ls)); }
		return geoms;
	}

	/**
	 * Skeletonize.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param clippingTolerance
	 *            the clipping tolerance
	 * @return the i list
	 */
	@operator (
			value = "skeletonize",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			usages = { @usage ("It can be used with 1 additional float operand: the tolerance for the clipping.") })
	@no_test
	public static IList<IShape> skeletonize(final IScope scope, final IShape g, final Double clippingTolerance) {
		final List<LineString> netw =
				GeometryUtils.squeletisation(scope, g.getInnerGeometry(), 0.0, clippingTolerance, false);
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		for (final LineString ls : netw) { geoms.add(GamaShapeFactory.createFrom(ls)); }
		return geoms;
	}

	/**
	 * Skeletonize.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the i list
	 */
	@operator (
			value = "skeletonize",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries (polylines) corresponding to the skeleton of the operand geometry (geometry, agent)",
			masterDoc = true,
			examples = { @example (
					value = "skeletonize(self)",
					equals = "the list of geometries corresponding to the skeleton of the geometry of the agent applying the operator.",
					test = false) })
	@test (" // applies only to a square \n " + "length(skeletonize(square(5))) = 1")
	public static IList<IShape> skeletonize(final IScope scope, final IShape g) {
		final List<LineString> netw = GeometryUtils.squeletisation(scope, g.getInnerGeometry(), 0.0, 0.0, false);
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		for (final LineString ls : netw) { geoms.add(GamaShapeFactory.createFrom(ls)); }
		return geoms;
	}

	/**
	 * Triangulate.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the i list
	 */
	@operator (
			value = { "triangulate", "to_triangles" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries (triangles) corresponding to the Delaunay triangulation of the operand geometry (geometry, agent, point)",
			masterDoc = true,
			examples = { @example (
					value = "triangulate(self)",
					equals = "the list of geometries (triangles) corresponding to the Delaunay triangulation of the geometry of the agent applying the operator.",
					test = false) })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList<IShape> triangulate(final IScope scope, final IShape g) {
		if (g == null) return null;
		return GeometryUtils.triangulation(scope, g.getInnerGeometry(), 0.0, 0.0, false);
	}

	/**
	 * Triangulate.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param clipTolerance
	 *            the clip tolerance
	 * @return the i list
	 */
	@operator (
			value = { "triangulate", "to_triangles" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries (triangles) corresponding to the Delaunay triangulation of the operand geometry (geometry, agent, point) with the given tolerance for the clipping",
			masterDoc = true,
			examples = { @example (
					value = "triangulate(self, 0.1)",
					equals = "the list of geometries (triangles) corresponding to the Delaunay triangulation of the geometry of the agent applying the operator.",
					test = false) })
	@no_test
	public static IList<IShape> triangulate(final IScope scope, final IShape g, final Double clipTolerance) {
		if (g == null) return null;
		return GeometryUtils.triangulation(scope, g.getInnerGeometry(), 0.0, clipTolerance, false);
	}

	/**
	 * Triangulate.
	 *
	 * @param scope
	 *            the scope
	 * @param gs
	 *            the gs
	 * @return the i list
	 */
	@operator (
			value = { "triangulate", "to_triangles" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries (triangles) corresponding to the Delaunay triangulation computed from the list of polylines",
			masterDoc = true,
			examples = { @example (
					value = "triangulate([line([{0,50},{100,50}]), line([{50,0},{50,100}]))",
					equals = "the list of geometries (triangles) corresponding to the Delaunay triangulation of the geometry of the agent applying the operator.",
					test = false) })
	@no_test
	public static IList<IShape> triangulate(final IScope scope, final IList<IShape> gs) {
		if (gs == null || gs.isEmpty()) return null;
		return GeometryUtils.triangulation(scope, gs, 0.0);
	}

	/**
	 * Triangulate.
	 *
	 * @param scope
	 *            the scope
	 * @param gs
	 *            the gs
	 * @param tol
	 *            the tol
	 * @return the i list
	 */
	@operator (
			value = { "triangulate", "to_triangles" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries (triangles) corresponding to the Delaunay triangulation computed from the list of polylines with the given tolerance for the triangulation",
			masterDoc = true,
			examples = { @example (
					value = "triangulate([line([{0,50},{100,50}]), line([{50,0},{50,100}], 0.01))",
					equals = "the list of geometries (triangles) corresponding to the Delaunay triangulation of the geometry of the agent applying the operator with the a tolerance of 0.01 for the triangulation.",
					test = false) })
	@no_test
	public static IList<IShape> triangulate(final IScope scope, final IList<IShape> gs, final double tol) {
		if (gs == null || gs.isEmpty()) return null;
		return GeometryUtils.triangulation(scope, gs, tol);
	}

	/**
	 * Triangulate.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param clipTolerance
	 *            the clip tolerance
	 * @param triangulationTolerance
	 *            the triangulation tolerance
	 * @return the i list
	 */
	@operator (
			value = { "triangulate", "to_triangles" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries (triangles) corresponding to the Delaunay triangulation of the operand geometry (geometry, agent, point) with the given tolerance for the clipping and for the triangulation",
			masterDoc = true,
			examples = { @example (
					value = "triangulate(self,0.1, 1.0)",
					equals = "the list of geometries (triangles) corresponding to the Delaunay triangulation of the geometry of the agent applying the operator.",
					test = false) })
	@no_test
	public static IList<IShape> triangulate(final IScope scope, final IShape g, final Double clipTolerance,
			final Double triangulationTolerance) {
		if (g == null) return null;
		return GeometryUtils.triangulation(scope, g.getInnerGeometry(), triangulationTolerance, clipTolerance, false);
	}

	/**
	 * Triangulate.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param clipTolerance
	 *            the clip tolerance
	 * @param triangulationTolerance
	 *            the triangulation tolerance
	 * @param approxClip
	 *            the approx clip
	 * @return the i list
	 */
	@operator (
			value = { "triangulate", "to_triangles" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries (triangles) corresponding to the Delaunay triangulation of the operand geometry (geometry, agent, point, use_approx_clipping) with the given tolerance for the clipping and for the triangulation with using an approximate clipping is the last operand is true",
			masterDoc = true,
			examples = { @example (
					value = "triangulate(self,0.1, 1.0, true)",
					equals = "the list of geometries (triangles) corresponding to the Delaunay triangulation of the geometry of the agent applying the operator.",
					test = false) })
	@no_test
	public static IList<IShape> triangulate(final IScope scope, final IShape g, final Double clipTolerance,
			final Double triangulationTolerance, final boolean approxClip) {
		if (g == null) return null;
		return GeometryUtils.triangulation(scope, g.getInnerGeometry(), triangulationTolerance, clipTolerance,
				approxClip);
	}

	/**
	 * Vornoi.
	 *
	 * @param scope
	 *            the scope
	 * @param pts
	 *            the pts
	 * @return the i list
	 */
	@operator (
			value = "voronoi",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries corresponding to the Voronoi diagram built from the list of points (with eventually a given  clip).",
			masterDoc = true,
			examples = { @example (
					value = "voronoi([{10,10},{50,50},{90,90},{10,90},{90,10}])",
					equals = "the list of geometries corresponding to the Voronoi Diagram built from the list of points.",
					test = false) })
	@no_test
	public static IList<IShape> vornoi(final IScope scope, final IList<GamaPoint> pts) {
		if (pts == null) return null;
		return GeometryUtils.voronoi(scope, pts);
	}

	/**
	 * Vornoi.
	 *
	 * @param scope
	 *            the scope
	 * @param pts
	 *            the pts
	 * @param clip
	 *            the clip
	 * @return the i list
	 */
	@operator (
			value = "voronoi",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			value = "A list of geometries corresponding to the Voronoi diagram built from the list of points according to the given clip",
			examples = { @example (
					value = "voronoi([{10,10},{50,50},{90,90},{10,90},{90,10}], square(300))",
					equals = "the list of geometries corresponding to the Voronoi Diagram built from the list of points with a square of 300m side size as clip.",
					test = false) })
	@no_test
	public static IList<IShape> vornoi(final IScope scope, final IList<GamaPoint> pts, final IShape clip) {
		if (pts == null) return null;
		return GeometryUtils.voronoi(scope, pts, clip);
	}

	/**
	 * Smooth.
	 *
	 * @param scope
	 *            the scope
	 * @param geometry
	 *            the geometry
	 * @param fit
	 *            the fit
	 * @return the i shape
	 */
	@operator (
			value = "smooth",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "Returns a 'smoothed' geometry, where straight lines are replaces by polynomial (bicubic) curves. The first parameter is the original geometry, the second is the 'fit' parameter which can be in the range 0 (loose fit) to 1 (tightest fit).",
			masterDoc = true,
			examples = { @example (
					value = "smooth(square(10), 0.0)",
					equals = "a 'rounded' square",
					test = false) })
	@no_test
	public static IShape smooth(final IScope scope, final IShape geometry, final Double fit) {
		if (geometry == null) return null;
		final double param = fit == null ? 0d : fit < 0 ? 0d : fit > 1 ? 1d : fit;
		return GeometryUtils.smooth(geometry.getInnerGeometry(), param);
	}

	/**
	 * To squares.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param dimension
	 *            the dimension
	 * @param overlaps
	 *            the overlaps
	 * @return the i list
	 */
	@operator (
			value = "to_squares",
			type = IType.LIST,
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of squares of the size corresponding to the given size that result from the decomposition of the geometry into squares (geometry, size, overlaps), if overlaps = true, add the squares that overlap the border of the geometry",
			examples = { @example (
					value = "to_squares(self, 10.0, true)",
					equals = "the list of squares of side size 10.0 corresponding to the discretization into squares of the geometry of the agent applying the operator. The squares overlapping the border of the geometry are kept",
					test = false) })
	@no_test
	public static IList<IShape> toSquares(final IScope scope, final IShape geom, final Double dimension,
			final boolean overlaps) {
		if (geom == null || geom.getInnerGeometry().getArea() <= 0) return GamaListFactory.create(Types.GEOMETRY);
		return GeometryUtils.discretization(geom.getInnerGeometry(), dimension, dimension, overlaps);
	}

	/**
	 * To squares.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param nbSquares
	 *            the nb squares
	 * @param overlaps
	 *            the overlaps
	 * @return the i list
	 */
	@operator (
			value = "to_squares",
			type = IType.LIST,
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			value = "A list of a given number of squares from the decomposition of the geometry into squares (geometry, nb_square, overlaps), if overlaps = true, add the squares that overlap the border of the geometry",
			examples = { @example (
					value = "to_squares(self, 10, true)",
					equals = "the list of 10 squares corresponding to the discretization into squares of the geometry of the agent applying the operator. The squares overlapping the border of the geometry are kept",
					test = false) })
	@no_test
	public static IList<IShape> toSquares(final IScope scope, final IShape geom, final Integer nbSquares,
			final boolean overlaps) {
		if (geom == null || geom.getInnerGeometry().getArea() <= 0) return GamaListFactory.create(Types.GEOMETRY);
		return GeometryUtils.squareDiscretization(geom.getInnerGeometry(), nbSquares, overlaps, 0.99);
	}

	/**
	 * Square discretization.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param nbSquares
	 *            the nb squares
	 * @param overlaps
	 *            the overlaps
	 * @param precision
	 *            the precision
	 * @return the i list
	 */
	@operator (
			value = "to_squares",
			type = IType.LIST,
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			value = "A list of a given number of squares from the decomposition of the geometry into squares (geometry, nb_square, overlaps, precision_coefficient), if overlaps = true, add the squares that overlap the border of the geometry, coefficient_precision should be close to 1.0",
			examples = { @example (
					value = "to_squares(self, 10, true, 0.99)",
					equals = "the list of 10 squares corresponding to the discretization into squares of the geometry of the agent applying the operator. The squares overlapping the border of the geometry are kept",
					test = false) })
	@no_test
	public static IList<IShape> squareDiscretization(final IScope scope, final IShape geom, final Integer nbSquares,
			final boolean overlaps, final double precision) {
		if (geom == null || geom.getInnerGeometry().getArea() <= 0) return GamaListFactory.create(Types.GEOMETRY);
		return GeometryUtils.squareDiscretization(geom.getInnerGeometry(), nbSquares, overlaps, precision);
	}

	/**
	 * To rectangle.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param dimension
	 *            the dimension
	 * @param overlaps
	 *            the overlaps
	 * @return the i list
	 */
	@operator (
			value = "to_rectangles",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of rectangles of the size corresponding to the given dimension that result from the decomposition of the geometry into rectangles (geometry, dimension, overlaps), if overlaps = true, add the rectangles that overlap the border of the geometry",
			masterDoc = true,
			examples = { @example (
					value = "to_rectangles(self, {10.0, 15.0}, true)",
					equals = "the list of rectangles of size {10.0, 15.0} corresponding to the discretization into rectangles of the geometry of the agent applying the operator. The rectangles overlapping the border of the geometry are kept",
					test = false) })
	@no_test
	public static IList<IShape> to_rectangle(final IScope scope, final IShape geom, final GamaPoint dimension,
			final boolean overlaps) {
		if (geom == null || geom.getInnerGeometry().getArea() <= 0) return GamaListFactory.create(Types.GEOMETRY);
		return GeometryUtils.discretization(geom.getInnerGeometry(), dimension.x, dimension.y, overlaps);
	}

	/**
	 * To rectangle.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param nbCols
	 *            the nb cols
	 * @param nbRows
	 *            the nb rows
	 * @param overlaps
	 *            the overlaps
	 * @return the i list
	 */
	@operator (
			value = "to_rectangles",
			type = IType.LIST,
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			value = "A list of rectangles corresponding to the given dimension that result from the decomposition of the geometry into rectangles (geometry, nb_cols, nb_rows, overlaps) by a grid composed of the given number of columns and rows, if overlaps = true, add the rectangles that overlap the border of the geometry",
			examples = { @example (
					value = "to_rectangles(self, 5, 20, true)",
					equals = "the list of rectangles corresponding to the discretization by a grid of 5 columns and 20 rows into rectangles of the geometry of the agent applying the operator. The rectangles overlapping the border of the geometry are kept",
					test = false) })
	@no_test
	public static IList<IShape> to_rectangle(final IScope scope, final IShape geom, final int nbCols, final int nbRows,
			final boolean overlaps) {
		if (geom == null || geom.getInnerGeometry().getArea() <= 0) return GamaListFactory.create(Types.GEOMETRY);
		final Envelope3D envelope = geom.getEnvelope();
		final double x_size = envelope.getWidth() / nbCols;
		final double y_size = envelope.getHeight() / nbRows;

		return GeometryUtils.discretization(geom.getInnerGeometry(), x_size, y_size, overlaps);
	}

	/**
	 * To squares.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param dimension
	 *            the dimension
	 * @return the i list
	 */
	@operator (
			value = { "split_geometry", "to_squares" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries that result from the decomposition of the geometry by square cells of the given side size (geometry, size). It can be used to split in rectangles by giving a point or 2 integer values as operand.",
			masterDoc = true,
			examples = { @example (
					value = "to_squares(self, 10.0)",
					equals = "the list of the geometries corresponding to the decomposition of the geometry by squares of side size 10.0",
					test = false) })
	@test ("length(square(10.0) split_geometry(3)) = 16")
	public static IList<IShape> toSquares(final IScope scope, final IShape geom, final Double dimension) {
		if (geom == null || geom.getInnerGeometry().getArea() <= 0) return GamaListFactory.create(Types.GEOMETRY);
		return GeometryUtils.geometryDecomposition(geom, dimension, dimension);
	}

	/**
	 * To rectangle.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param dimension
	 *            the dimension
	 * @return the i list
	 */
	@operator (
			value = { "split_geometry", "to_rectangles" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries that result from the decomposition of the geometry by rectangle cells of the given dimension (geometry, {size_x, size_y})",
			examples = { @example (
					value = "to_rectangles(self, {10.0, 15.0})",
					equals = "the list of the geometries corresponding to the decomposition of the geometry by rectangles of size 10.0, 15.0",
					test = false) })
	@test ("length(square(10.0) split_geometry({2,3})) = 20")
	public static IList<IShape> toRectangle(final IScope scope, final IShape geom, final GamaPoint dimension) {
		if (geom == null || geom.getInnerGeometry().getArea() <= 0) return GamaListFactory.create(Types.GEOMETRY);
		return GeometryUtils.geometryDecomposition(geom, dimension.x, dimension.y);
	}

	/**
	 * To rectangle.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param nbCols
	 *            the nb cols
	 * @param nbRows
	 *            the nb rows
	 * @return the i list
	 */
	@operator (
			value = { "split_geometry", "to_rectangles" },
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			value = "A list of geometries that result from the decomposition of the geometry according to a grid with the given number of rows and columns (geometry, nb_cols, nb_rows)",
			examples = { @example (
					value = "to_rectangles(self, 10,20)",
					equals = "the list of the geometries corresponding to the decomposition of the geometry of the agent applying the operator",
					test = false) })
	@test ("length(square(10.0) split_geometry(2,2)) = 4")
	public static IList<IShape> to_rectangle(final IScope scope, final IShape geom, final int nbCols,
			final int nbRows) {
		if (geom == null || geom.getInnerGeometry().getArea() <= 0) return GamaListFactory.create(Types.GEOMETRY);
		final Envelope3D envelope = geom.getEnvelope();
		final double x_size = envelope.getWidth() / nbCols;
		final double y_size = envelope.getHeight() / nbRows;

		return GeometryUtils.geometryDecomposition(geom, x_size, y_size);
	}

	/**
	 * To segments.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @return the i list
	 */
	@operator (
			value = "to_segments",
			type = IType.LIST,
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			value = "A list of a segments resulting from the decomposition of the geometry (or its contours for polygons) into sgements",
			examples = { @example (
					value = "to_segments(line([{10,10},{80,10},{80,80}]))",
					equals = "[line([{10,10},{80,10}]), line([{80,10},{80,80}])]",
					test = false) })
	@no_test
	public static IList<IShape> toSegments(final IScope scope, final IShape geom) {
		if (geom == null) return GamaListFactory.create(Types.GEOMETRY);
		final IList<IShape> segments = GamaListFactory.create(Types.GEOMETRY);
		if (geom.isMultiple()) {
			for (final IShape g : geom.getGeometries()) { segments.addAll(toSegments(scope, g)); }
		} else if (geom.isPoint()) {
			segments.add(GamaShapeFactory.createFrom(geom));
		} else {
			for (int i = 1; i < geom.getPoints().size(); i++) {
				final IList<IShape> points = GamaListFactory.create(Types.POINT);
				points.add(geom.getPoints().get(i - 1));
				points.add(geom.getPoints().get(i));
				segments.add(SpatialCreation.line(scope, points));
			}
		}

		return segments;
	}

	/**
	 * As hexagonal grid.
	 *
	 * @param ls
	 *            the ls
	 * @param param
	 *            the param
	 * @return the i list
	 */
	@operator (
			value = "as_hexagonal_grid",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS, IOperatorCategory.GRID },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.GRID })
	@doc (
			value = "A list of geometries (hexagonal) corresponding to the hexagonal tesselation of the first operand geometry",
			examples = { @example (
					value = "self as_hexagonal_grid {10, 5}",
					equals = "list of geometries (hexagonal) corresponding to the hexagonal tesselation of the first operand geometry",
					test = false) },
			see = { "as_4_grid", "as_grid" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList<IShape> as_hexagonal_grid(final IShape ls, final GamaPoint param) {
		return GeometryUtils.hexagonalGridFromGeom(ls, (int) param.x, (int) param.y);
	}

	/**
	 * As grid.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param dim
	 *            the dim
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "as_grid",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS, IOperatorCategory.GRID },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.GRID })
	@doc (
			value = "A matrix of square geometries (grid with 8-neighborhood) with dimension given by the right-hand operand ({nb_cols, nb_lines}) corresponding to the square tessellation of the left-hand operand geometry (geometry, agent)",
			examples = { @example (
					value = "self as_grid {10, 5}",
					equals = "a matrix of square geometries (grid with 8-neighborhood) with 10 columns and 5 lines corresponding to the square tessellation of the geometry of the agent applying the operator.",
					test = false) },
			see = { "as_4_grid", "as_hexagonal_grid" })
	@no_test
	public static IMatrix as_grid(final IScope scope, final IShape g, final GamaPoint dim) throws GamaRuntimeException {
		// cols, rows
		return new GamaSpatialMatrix(scope, g, (int) dim.x, (int) dim.y, false, false, false, false, "");
	}

	/**
	 * As 4 grid.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param dim
	 *            the dim
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "as_4_grid",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS, IOperatorCategory.GRID },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.GRID })
	@doc (
			value = "A matrix of square geometries (grid with 4-neighborhood) with dimension given by the right-hand operand ({nb_cols, nb_lines}) corresponding to the square tessellation of the left-hand operand geometry (geometry, agent)",
			examples = { @example (
					value = "self as_4_grid {10, 5}",
					equals = "the matrix of square geometries (grid with 4-neighborhood) with 10 columns and 5 lines corresponding to the square tessellation of the geometry of the agent applying the operator.",
					test = false) },
			see = { "as_grid", "as_hexagonal_grid" })
	@no_test
	public static IMatrix as_4_grid(final IScope scope, final IShape g, final GamaPoint dim)
			throws GamaRuntimeException {
		// cols, rows
		return new GamaSpatialMatrix(scope, g, (int) dim.x, (int) dim.y, false, true, false, false, "");
	}

	/**
	 * Split geometries.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param rates
	 *            the rates
	 * @return the i list
	 */
	@operator (
			value = "to_sub_geometries",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries resulting after spliting the geometry into sub-geometries.",
			masterDoc = true,
			examples = { @example (
					value = "to_sub_geometries(rectangle(10, 50), [0.1, 0.5, 0.4])",
					equals = "a list of three geometries corresponding to 3 sub-geometries",
					test = false) })
	@test ("length(to_sub_geometries(rectangle(10, 50), [0.1, 0.5, 0.4])) = 3")
	public static IList<IShape> splitGeometries(final IScope scope, final IShape geom, final IList<Double> rates) {
		if (geom == null) return GamaListFactory.create(Types.GEOMETRY);

		final double dimension = geom.getArea() / 2000.0;
		return splitGeometries(scope, geom, rates, dimension);
	}

	/**
	 * Split geometries.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param rates
	 *            the rates
	 * @param dimension
	 *            the dimension
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "to_sub_geometries",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries resulting after spliting the geometry into sub-geometries.",
			examples = { @example (
					value = "to_sub_geometries(rectangle(10, 50), [0.1, 0.5, 0.4], 1.0)",
					equals = "a list of three geometries corresponding to 3 sub-geometries using cubes of 1m size",
					test = false) })
	@no_test
	public static IList<IShape> splitGeometries(final IScope scope, final IShape geom, final IList<Double> rates,
			final Double dimension) throws GamaRuntimeException {
		if (geom == null || rates == null || rates.isEmpty()) return GamaListFactory.create(Types.GEOMETRY);
		final IList<IShape> nwGeoms = GamaListFactory.create(Types.GEOMETRY);
		if (geom.isPoint()) {
			nwGeoms.add(geom.copy(scope));
		} else if (geom.isLine()) {
			final IList<Double> translatedRates = GamaListFactory.create(Types.FLOAT);
			final Double sum = (Double) Containers.sum(scope, rates);
			double accu = 0;
			for (int i = 0; i < rates.size() - 1; i++) {
				accu += rates.get(i);
				translatedRates.add(accu / sum);
			}
			final IList<GamaPoint> pts = SpatialPunctal.points_along(geom, translatedRates);
			IShape g = geom.copy(scope);
			for (final GamaPoint pt : pts) {
				final IList<IShape> shapes = SpatialOperators.split_at(g, pt);
				nwGeoms.add(shapes.get(0));
				g = shapes.get(1);
			}
			nwGeoms.add(g);
		} else if (geom.getArea() > 0) {
			Comparator<IShape> comp;
			if (geom.getWidth() > geom.getHeight()) {
				comp = (o1, o2) -> Double.compare(o1.getLocation().getX(), o2.getLocation().getX());
			} else {
				comp = (o1, o2) -> Double.compare(o1.getLocation().getY(), o2.getLocation().getY());
			}
			ArrayList<IShape> listSq = new ArrayList(toSquares(scope, geom, dimension).stream().sorted(comp).toList());
			final Double sum = (Double) Containers.sum(scope, rates);
			final int totalNumber = listSq.size();
			for (final Double rate : rates) {
				final int number = Math.min((int) (rate / sum * totalNumber + 0.5), totalNumber);
				final IList<IShape> squares = GamaListFactory.create(Types.GEOMETRY);
				for (int i = 0; i < number; i++) { squares.add(listSq.remove(0)); }
				if (!squares.isEmpty()) {
					final IShape unionG = SpatialTransformations.clean(scope, SpatialOperators.union(scope, squares));
					nwGeoms.add(unionG);

				}
			}

		}
		return nwGeoms;
	}

	/**
	 * Split lines.
	 *
	 * @param scope
	 *            the scope
	 * @param geoms
	 *            the geoms
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "split_lines",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries resulting after cutting the lines at their intersections.",
			examples = { @example (
					value = "split_lines([line([{0,10}, {20,10}]), line([{0,10}, {20,10}])])",
					equals = "a list of four polylines: line([{0,10}, {10,10}]), line([{10,10}, {20,10}]), line([{10,0}, {10,10}]) and line([{10,10}, {10,20}])",
					test = false) })
	@test ("split_lines([line([{0,10}, {20,10}]), line([{10,0}, {10,20}])]) = [line([{0,10}, {10,10}]), line([{10,10}, {20,10}]), line([{10,0}, {10,10}]) , line([{10,10}, {10,20}])]")
	public static IList<IShape> split_lines(final IScope scope, final IContainer<?, IShape> geoms)
			throws GamaRuntimeException {
		if (geoms.isEmpty(scope)) return GamaListFactory.create(Types.GEOMETRY);
		final IShape line = SpatialOperators.union(scope, geoms);
		final Geometry nodedLineStrings = line.getInnerGeometry();
		final IList<IShape> nwGeoms = GamaListFactory.create(Types.GEOMETRY);

		for (int i = 0, n = nodedLineStrings.getNumGeometries(); i < n; i++) {
			final Geometry g = nodedLineStrings.getGeometryN(i);
			if (g instanceof LineString) { nwGeoms.add(GamaShapeFactory.createFrom(g)); }
		}
		return nwGeoms;
	}

	/**
	 * Split lines.
	 *
	 * @param scope
	 *            the scope
	 * @param geoms
	 *            the geoms
	 * @param readAttributes
	 *            the read attributes
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "split_lines",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A list of geometries resulting after cutting the lines at their intersections. if the last boolean operand is set to true, the split lines will import the attributes of the initial lines",
			examples = { @example (
					value = "split_lines([line([{0,10}, {20,10}]), line([{0,10}, {20,10}])])",
					equals = "a list of four polylines: line([{0,10}, {10,10}]), line([{10,10}, {20,10}]), line([{10,0}, {10,10}]) and line([{10,10}, {10,20}])",
					test = false) })
	@test ("split_lines([line({10,10}, {20,20}), line({10.0,20.0,0.0},{15.0,15.0,0.0})]) = [line ({10.0,10.0,0.0},{15.0,15.0,0.0}), line({15.0,15.0,0.0},{20.0,20.0,0.0}), line({10.0,20.0,0.0},{15.0,15.0,0.0})]")
	@test ("length(split_lines([line({10,10}, {20,20}), line({10.0,20.0,0.0},{15.0,15.0,0.0})])) = 3")
	public static IList<IShape> split_lines(final IScope scope, final IContainer<?, IShape> geoms,
			final boolean readAttributes) throws GamaRuntimeException {
		if (geoms.isEmpty(scope)) return GamaListFactory.create(Types.GEOMETRY);
		if (!readAttributes) return split_lines(scope, geoms);
		boolean change = true;
		IList<IShape> lines = GamaListFactory.create(Types.GEOMETRY);
		lines.addAll((Collection<? extends IShape>) geoms);
		final IList<IShape> split_lines = GamaListFactory.create(Types.GEOMETRY);
		while (change) {
			change = false;
			final IList<IShape> lines2 = GamaListFactory.createWithoutCasting(Types.GEOMETRY, lines);
			for (final IShape l : lines) {
				lines2.remove(l);
				if (!l.getInnerGeometry().isSimple()) {
					final IList<IShape> segments = GamaListFactory.create(Types.GEOMETRY);
					for (int i = 0; i < l.getPoints().size() - 1; i++) {
						final IList<IShape> points = GamaListFactory.create(Types.POINT);
						points.add(l.getPoints().get(i));
						points.add(l.getPoints().get(i + 1));
						segments.add(SpatialCreation.line(scope, points));
					}
					final IShape line = SpatialOperators.union(scope, segments);
					final Geometry nodedLineStrings = line.getInnerGeometry();

					for (int i = 0, n = nodedLineStrings.getNumGeometries(); i < n; i++) {
						final Geometry g = nodedLineStrings.getGeometryN(i);
						if (g instanceof LineString) {
							final IShape gS = GamaShapeFactory.createFrom(g);
							gS.copyAttributesOf(l);
							lines2.add(GamaShapeFactory.createFrom(g));
						}
					}
					change = true;

					lines = lines2;
					break;
				}
				final IShape gg =
						SpatialTransformations.enlarged_by(scope, l, Math.min(0.001, l.getPerimeter() / 1000.0), 10);

				final List<IShape> ls = gg == null ? GamaListFactory.create()
						: (List<IShape>) SpatialQueries.overlapping(scope, lines2, gg);
				if (!ls.isEmpty()) {
					final GamaPoint pto = l.getPoints().firstValue(scope);
					final GamaPoint ptd = l.getPoints().lastValue(scope);
					@SuppressWarnings ("null") final PreparedGeometry pg =
							PreparedGeometryFactory.prepare(gg.getInnerGeometry());
					for (final IShape l2 : ls) {
						if (pg.covers(l2.getInnerGeometry()) || pg.coveredBy(l2.getInnerGeometry())) { continue; }
						final IShape it = SpatialOperators.inter(scope, l, l2);

						if (it == null || it.getPerimeter() > 0.0) { continue; }
						if (!it.getLocation().equals(pto) || !it.getLocation().equals(ptd)) {
							final GamaPoint pt = it.getPoints().firstValue(scope);
							final IList<IShape> res1 = SpatialOperators.split_at(l2, pt);
							res1.removeIf(a -> a.getPerimeter() == 0.0);
							final IList<IShape> res2 = SpatialOperators.split_at(l, pt);
							res2.removeIf(a -> a.getPerimeter() == 0.0);
							if (res1.size() > 1 || res2.size() > 1) {
								change = true;
								lines2.addAll(res1);
								lines2.addAll(res2);
								lines2.remove(l2);
								break;
							}
						}
					}
					if (change) {
						lines = lines2;
						break;
					}
				}
				split_lines.add(l);
			}

		}

		return split_lines;
	}

	/**
	 * Clean.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the i shape
	 */
	@operator (
			value = "clean",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A geometry corresponding to the cleaning of the operand (geometry, agent, point)",
			comment = "The cleaning corresponds to a buffer with a distance of 0.0",
			examples = { @example (
					value = "clean(self)",
					equals = "returns the geometry resulting from the cleaning of the geometry of the agent applying the operator.",
					test = false) })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IShape clean(final IScope scope, final IShape g) {

		if (g == null || g.getInnerGeometry() == null) return g;
		if (g.getInnerGeometry() instanceof Polygon)
			return GamaShapeFactory.createFrom(GeometryUtils.cleanGeometry(g.getInnerGeometry())).withAttributesOf(g);
		if (g.getInnerGeometry() instanceof MultiPolygon) {
			final MultiPolygon mp = (MultiPolygon) g.getInnerGeometry();
			final int nb = mp.getNumGeometries();
			final Polygon[] polys = new Polygon[nb];
			for (int i = 0; i < nb; i++) { polys[i] = (Polygon) GeometryUtils.cleanGeometry(mp.getGeometryN(i)); }
			return GamaShapeFactory.createFrom(GeometryUtils.GEOMETRY_FACTORY.createMultiPolygon(polys))
					.withAttributesOf(g);
		}
		return g.copy(scope);
	}

	/**
	 * Clean.
	 *
	 * @param scope
	 *            the scope
	 * @param polylines
	 *            the polylines
	 * @param tolerance
	 *            the tolerance
	 * @param splitlines
	 *            the splitlines
	 * @param keepMainGraph
	 *            the keep main graph
	 * @return the i list
	 */
	@operator (
			value = "clean_network",
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = """
					A list of polylines corresponding to the cleaning of the first operand (list of polyline geometry or agents), considering the tolerance distance given \
					by the second operand; the third operator is used to define if the operator should as well split the lines at their intersections(true to split the lines); the last operand\
					is used to specify if the operator should as well keep only the main connected component of the network. \
					Usage: clean_network(lines:list of geometries or agents, tolerance: float, split_lines: bool, keepMainConnectedComponent: bool)""",
			comment = "The cleaned set of polylines",
			examples = { @example (
					value = "clean_network(my_road_shapefile.contents, 1.0, true, false)",
					equals = "returns the list of polulines resulting from the cleaning of the geometry of the agent applying the operator with a tolerance of 1m, and splitting the lines at their intersections.",
					isExecutable = false),
					@example (
							value = "clean_network([line({10,10}, {20,20}), line({10,20},{20,10})],3.0,true,false)",
							equals = "[line({10.0,20.0,0.0},{15.0,15.0,0.0}),line({15.0,15.0,0.0},{20.0,10.0,0.0}), line({10.0,10.0,0.0},{15.0,15.0,0.0}), line({15.0,15.0,0.0},{20.0,20.0,0.0})]") })
	@test ("length(clean_network([line({10,10}, {20,20}), line({10,20},{20,10})],3.0,true,false)) = 4")
	public static IList<IShape> clean(final IScope scope, final IList<IShape> polylines, final double tolerance,
			final boolean splitlines, final boolean keepMainGraph) {
		if (polylines == null || polylines.isEmpty()) return polylines;
		final IList<IShape> geoms = polylines.copy(scope);
		geoms.removeIf(a -> !a.getGeometry().isLine());
		if (geoms.isEmpty()) return GamaListFactory.EMPTY_LIST;

		IList<IShape> results = GamaListFactory.create();

		IList<IShape> geomsTmp = geoms.copy(scope);
		boolean modif = true;
		if (tolerance > 0) {

			while (modif) {
				for (final IShape geom : geomsTmp) {
					final GamaPoint ptF = geom.getPoints().firstValue(scope);
					modif = connectLine(scope, ptF, geom, true, geoms, results, tolerance);
					if (modif) {
						geomsTmp = GamaListFactory.create();
						geomsTmp.addAll(geoms);
						break;
					}
					final GamaPoint ptL = geom.getPoints().lastValue(scope);
					modif = connectLine(scope, ptL, geom, false, geoms, results, tolerance);
					if (modif) {
						geomsTmp = GamaListFactory.create();
						geomsTmp.addAll(geoms);
						break;
					}
					results.add(geom);
					geoms.remove(geom);
				}
			}
		} else {
			results = geomsTmp;
		}
		results.removeIf(
				a -> a.getPerimeter() == 0 || !a.getInnerGeometry().isValid() || a.getInnerGeometry().isEmpty());

		if (splitlines) {
			results = SpatialTransformations.split_lines(scope, results, true);
			results.removeIf(
					a -> !a.getInnerGeometry().isValid() || a.getInnerGeometry().isEmpty() || a.getPerimeter() == 0);
		}
		if (keepMainGraph) {
			IGraph graph = Graphs.spatialFromEdges(scope, results);
			graph = Graphs.reduceToMainconnectedComponentOf(scope, graph);
			return graph.getEdges();
		}
		return results;
	}

	/**
	 * Connect line.
	 *
	 * @param scope
	 *            the scope
	 * @param pt
	 *            the pt
	 * @param shape
	 *            the shape
	 * @param first
	 *            the first
	 * @param geoms
	 *            the geoms
	 * @param results
	 *            the results
	 * @param tolerance
	 *            the tolerance
	 * @return true, if successful
	 */
	private static boolean connectLine(final IScope scope, final GamaPoint pt, final IShape shape, final boolean first,
			final IList<IShape> geoms, final IList<IShape> results, final double tolerance) {
		final IList<IShape> tot = geoms.copy(scope);
		tot.addAll(results);
		tot.remove(shape);
		final IShape closest = SpatialQueries.closest_to(scope, tot, pt);
		if (closest == null || closest.intersects(shape)) return false;
		if (closest.euclidianDistanceTo(pt) <= tolerance) {
			final GamaPoint fp = closest.getPoints().firstValue(scope);
			if (pt.equals3D(fp)) return false;
			final GamaPoint lp = closest.getPoints().lastValue(scope);
			if (pt.equals3D(lp)) return false;
			if (pt.euclidianDistanceTo(fp) <= tolerance) {
				modifyPoint(scope, shape, fp, first);
				return false;
			}
			if (pt.euclidianDistanceTo(lp) > tolerance) {
				final GamaPoint ptS = SpatialPunctal.closest_points_with(pt, closest).get(1);
				modifyPoint(scope, shape, ptS, first);
				final IList<IShape> spliL = SpatialOperators.split_at(closest, ptS);
				if (results.contains(closest)) {
					results.remove(closest);
					results.addAll(spliL);
				} else {
					geoms.remove(closest);
					geoms.addAll(spliL);
				}
				return true;
			}
			modifyPoint(scope, shape, lp, first);
		}
		return false;
	}

	/**
	 * Modify point.
	 *
	 * @param scope
	 *            the scope
	 * @param shape
	 *            the shape
	 * @param pt
	 *            the pt
	 * @param first
	 *            the first
	 */
	/*
	 * if (first) {g <- line([pt] + (g.points - first(g.points)));} else {g <- line((g.points - last(g.points)) +
	 * [pt]);} return g;
	 */
	private static void modifyPoint(final IScope scope, final IShape shape, final GamaPoint pt, final boolean first) {
		if (first) {
			shape.getInnerGeometry().getCoordinates()[0] = pt;
		} else {
			shape.getInnerGeometry().getCoordinates()[shape.getInnerGeometry().getCoordinates().length - 1] = pt;
		}
		shape.getInnerGeometry().geometryChanged();
	}

	/**
	 * Simplification of a geometry (Douglas-Peuker algorithm)
	 */

	@operator (
			value = "simplification",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A geometry corresponding to the simplification of the operand (geometry, agent, point) considering a tolerance distance.",
			comment = "The algorithm used for the simplification is Douglas-Peucker",
			examples = { @example (
					value = "self simplification 0.1",
					equals = "the geometry resulting from the application of the Douglas-Peuker algorithm on the geometry of the agent applying the operator with a tolerance distance of 0.1.",
					test = false) })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IShape simplification(final IScope scope, final IShape g1, final Double distanceTolerance) {
		if (g1 == null || g1.getInnerGeometry() == null) return g1;
		if (g1.isPoint()) return g1.copy(scope);
		final Geometry geomSimp = DouglasPeuckerSimplifier.simplify(g1.getInnerGeometry(), distanceTolerance);
		if (geomSimp != null && !geomSimp.isEmpty() && geomSimp.isSimple())
			return GamaShapeFactory.createFrom(geomSimp).withAttributesOf(g1);
		return g1.copy(scope);
	}

	/**
	 * With precision.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param precision
	 *            the precision
	 * @return the i shape
	 */
	@operator (
			value = "with_precision",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION })
	@doc (
			value = "A geometry corresponding to the rounding of points of the operand considering a given precison.",
			examples = { @example (
					value = "self with_precision 2",
					equals = "the geometry resulting from the rounding of points of the geometry with a precision of 0.1.",
					test = false) })
	@no_test
	public static IShape withPrecision(final IScope scope, final IShape g1, final Integer precision) {
		if (g1 == null || g1.getInnerGeometry() == null) return g1;
		final double scale = Math.pow(10, precision);
		final PrecisionModel pm = new PrecisionModel(scale);
		return GamaShapeFactory.createFrom(GeometryPrecisionReducer.reduce(g1.getInnerGeometry(), pm));
	}

}
