/*******************************************************************************************************
 *
 * SpatialProperties.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators.spatial;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;

/**
 * Provides GAML Boolean topological predicate operators implementing DE-9IM (Dimensionally Extended
 * 9-Intersection Model) spatial relationships. All predicates delegate to JTS (Java Topology Suite)
 * methods internally.
 * <p>
 * Operator families provided:
 * <ul>
 *   <li><b>Disjoint / Intersection</b>: {@code disjoint_from}, {@code intersects} — test whether
 *       two geometries share any point.</li>
 *   <li><b>Overlap</b>: {@code overlaps}, {@code partially_overlaps} — test whether interiors
 *       intersect or share space without one fully covering the other.</li>
 *   <li><b>Crossing / Touching</b>: {@code crosses}, {@code touches} (deprecated) — test
 *       dimensionally-specific boundary intersection patterns.</li>
 *   <li><b>Coverage / Equality</b>: {@code covers}, {@code equals} — test whether one geometry
 *       fully contains another or both geometries are geometrically identical.</li>
 * </ul>
 * <p>
 * These operators work on pure JTS {@code Geometry} instances and do <em>not</em> require an
 * active simulation topology, making most of them safe to unit-test with literal geometry values.
 * The {@code @no_test} annotation is therefore omitted for predicates that can be validated purely
 * with geometric constructors.
 *
 * @author Alexis Drogoul, Patrick Taillandier, Arnaud Grignard
 * @see gama.api.types.geometry.IShape
 * @see gama.api.types.geometry.IPoint
 */
public class SpatialProperties {

	/**
	 * Disjoint from.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param g2
	 *            the g 2
	 * @return the boolean
	 */
	@operator (
			value = { "disjoint_from" },
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION })
	@doc (
			value = "A boolean, equal to true if the left-geometry (or agent/point) is disjoints from the right-geometry (or agent/point).",
			usages = { @usage (
					value = "if one of the operand is null, returns true."),
					@usage (
							value = "if one operand is a point, returns false if the point is included in the geometry.") },
			examples = { @example (
					value = "polyline([{10,10},{20,20}]) disjoint_from polyline([{15,15},{25,25}])",
					equals = "false"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) disjoint_from polygon([{15,15},{15,25},{25,25},{25,15}])",
							equals = "false"),
					// @example (
					// value = "polygon([{10,10},{10,20},{20,20},{20,10}]) disjoint_from {15,15}",
					// equals = "false"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) disjoint_from {25,25}",
							equals = "true"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) disjoint_from polygon([{35,35},{35,45},{45,45},{45,35}])",
							equals = "true") },
			see = { "intersects", "crosses", "overlaps", "partially_overlaps", "touches" })
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) disjoint_from {15,15} = false")
	@test ("polygon([{0,0},{0,5},{5,5},{5,0}]) disjoint_from polygon([{10,10},{10,15},{15,15},{15,10}])")
	@test ("!(polygon([{0,0},{0,5},{5,5},{5,0}]) disjoint_from polygon([{3,3},{3,8},{8,8},{8,3}]))")
	public static Boolean disjoint_from(final IScope scope, final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null || g1.getInnerGeometry() == null || g2.getInnerGeometry() == null) return true;
		return !g1.intersects(g2);
	}

	/**
	 * Return true if the agent geometry overlaps the geometry of the localized entity passed in parameter
	 *
	 * @param args
	 *            : agent --: a localized entity
	 *
	 */

	@operator (
			value = "overlaps",
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION })
	@doc (
			value = "A boolean, equal to true if the left-geometry (or agent/point) overlaps the right-geometry (or agent/point).",
			usages = { @usage ("if one of the operand is null, returns false."),
					@usage ("if one operand is a point, returns true if the point is included in the geometry"),
					@usage ("Note: unlike the strict JTS {@code overlaps} predicate, this GAML operator uses intersection semantics (equivalent to {@code intersects}): two geometries that only touch at a boundary point or line will still return true.") },
			examples = { @example (
					value = "polyline([{10,10},{20,20}]) overlaps polyline([{15,15},{25,25}])",
					equals = "true"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps polygon([{15,15},{15,25},{25,25},{25,15}])",
							equals = "true"),
					// @example (
					// value = "polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps {25,25}",
					// equals = "false"),
					// @example (
					// value = "polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps
					// polygon([{35,35},{35,45},{45,45},{45,35}])",
					// equals = "false"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps polyline([{10,10},{20,20}])",
							equals = "true"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps {15,15}",
							equals = "true") },
			// @example (
			// value = "polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps polygon([{0,0},{0,30},{30,30},
			// {30,0}])",
			// equals = "true"),
			// @example (
			// value = "polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps
			// polygon([{15,15},{15,25},{25,25},{25,15}])",
			// equals = "true"),
			// @example (
			// value = "polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps
			// polygon([{10,20},{20,20},{20,30},{10,30}])",
			// equals = "true") },
			see = { "disjoint_from", "crosses", "intersects", "partially_overlaps", "touches" })
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps polygon([{0,0},{0,30},{30,30},{30,0}])")
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps polygon([{15,15},{15,25},{25,25},{25,15}])")
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps polygon([{10,20},{20,20},{20,30},{10,30}])")
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps polygon([{35,35},{35,45},{45,45},{45,35}]) =  false")
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) overlaps {25,25} = false")
	public static Boolean overlaps(final IScope scope, final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null) return false;
		return !disjoint_from(scope, g1, g2);
	}

	/**
	 * Return true if the agent geometry partially overlaps the geometry of the localized agent passed in parameter
	 *
	 * @param args
	 *            : agent --: a localized entity
	 *
	 */

	@operator (
			value = "partially_overlaps",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_PROPERTIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION })
	@doc (
			value = "A boolean, equal to true if the left-geometry (or agent/point) partially overlaps the right-geometry (or agent/point).",
			usages = { @usage ("if one of the operand is null, returns false.") },
			comment = "if one geometry operand fully covers the other geometry operand, returns false (contrarily to the overlaps operator).",
			examples = { @example (
					value = "polyline([{10,10},{20,20}]) partially_overlaps polyline([{15,15},{25,25}])",
					equals = "true"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) partially_overlaps polygon([{15,15},{15,25},{25,25},{25,15}])",
							equals = "true"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) partially_overlaps {25,25}",
							equals = "false"),
					@example (
							value = "polygon([{10,10},{10,20},{20,20},{20,10}]) partially_overlaps polyline([{10,10},{20,20}])",
							equals = "false") },
			see = { "disjoint_from", "crosses", "overlaps", "intersects", "touches" })
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) partially_overlaps polygon([{0,0},{0,30},{30,30}, {30,0}]) =  false")
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) partially_overlaps polygon([{15,15},{15,25},{25,25},{25,15}])")
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) partially_overlaps polygon([{10,20},{20,20},{20,30},{10,30}]) = false")
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) partially_overlaps {15,15} = false")
	@test ("polygon([{10,10},{10,20},{20,20},{20,10}]) partially_overlaps polygon([{35,35},{35,45},{45,45},{45,35}]) = false")
	public static Boolean partially_overlaps(final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null) return false;
		return g1.partiallyOverlaps(g2);
	}

	/**
	 * Return true if the agent geometry touches the geometry of the localized entity passed in parameter
	 *
	 * @param args
	 *            : agent --: a localized entity
	 *
	 */
	@operator (
			value = "touches",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_PROPERTIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION })
	@doc (
			deprecated = "It is not advised to use this operator as its semantic is not clear and has changed recently in the JTS library (see https://github.com/gama-platform/gama/pull/415). `intersects` should be preferred. ",
			value = "A boolean, equal to true if the left-geometry (or agent/point) touches the right-geometry (or agent/point), i.e. if they have at least one point in common, but their interiors do not intersect.",
			usages = { @usage ("if one of the operand is null, returns false.") },
			comment = "returns true when the left-operand only touches the right-operand. When one geometry covers partially (or fully) the other one, it returns false.",
			see = { "disjoint_from", "crosses", "overlaps", "partially_overlaps", "intersects" })
	@Deprecated
	public static Boolean touches(final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null) return false;
		return g1.touches(g2);
	}

	/**
	 * Return true if the agent geometry crosses the geometry of the localized entity passed in parameter
	 *
	 * @param args
	 *            : agent --: a localized entity
	 *
	 */

	@operator (
			value = "crosses",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_PROPERTIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION })
	@doc (
			value = "A boolean, equal to true if the left-geometry (or agent/point) crosses the right-geometry (or agent/point).",
			usages = { @usage ("if one of the operand is null, returns false."),
					@usage ("if one operand is a point, returns false.") },
			examples = { @example (
					value = "polyline([{10,10},{20,20}]) crosses polyline([{10,20},{20,10}])",
					equals = "true"),
					@example (
							value = "polyline([{10,10},{20,20}]) crosses {15,15}",
							equals = "true"),
					@example (
							value = "polyline([{0,0},{25,25}]) crosses polygon([{10,10},{10,20},{20,20},{20,10}])",
							equals = "true") },
			see = { "disjoint_from", "intersects", "overlaps", "partially_overlaps", "touches" })
	@test ("polyline([{10,10},{20,20}]) crosses polyline([{10,20},{20,10}])")
	public static Boolean crosses(final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null) return false;
		return g1.crosses(g2);
	}

	/**
	 * Intersects.
	 *
	 * @param g1
	 *            the g 1
	 * @param g2
	 *            the g 2
	 * @return the boolean
	 */
	@operator (
			value = "intersects",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_PROPERTIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION })
	@doc (
			value = "A boolean, equal to true if the left-geometry (or agent/point) intersects the right-geometry (or agent/point).",
			usages = { @usage ("if one of the operand is null, returns false.") },
			examples = { @example (
					value = "square(5) intersects {10,10}",
					equals = "false") },
			see = { "disjoint_from", "crosses", "overlaps", "partially_overlaps", "touches" })
	@test ("square(5) intersects square(2)")
	@test ("square(10) intersects square(10)")
	@test ("!(square(10) intersects polygon([{20,20},{20,25},{25,25},{25,20}]))")
	public static Boolean intersects(final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null) return false;
		return g1.intersects(g2);
	}

	/**
	 * Covers.
	 *
	 * @param g1
	 *            the g 1
	 * @param g2
	 *            the g 2
	 * @return the boolean
	 */
	@operator (
			value = "covers",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_PROPERTIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION })
	@doc (
			value = "A boolean, equal to true if the left-geometry (or agent/point) covers the right-geometry (or agent/point).",
			usages = { @usage ("if one of the operand is null, returns false.") },
			examples = { @example (
					value = "square(5) covers square(2)",
					equals = "true") },
			see = { "disjoint_from", "crosses", "overlaps", "partially_overlaps", "touches" })
	@test ("square(5) covers square(2)")
	public static Boolean covers(final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null) return false;
		return g1.covers(g2);
	}
	
	@operator (
			value = "equals",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_PROPERTIES },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION })
	@doc (
			value = "A boolean, equal to true if the left-geometry (or agent/point) equals the right-geometry (or agent/point).",
			usages = { @usage ("if one of the operand is null, returns false.") },
			examples = { @example (
					value = "square(5) equals (rectangle(10,5) rotated_by 90)",
					equals = "true") },
			see = { "disjoint_from", "crosses", "overlaps", "partially_overlaps", "touches", "covers" })
	@test ("rectangle(5,10) equals rectangle(5,10)")
	public static Boolean equalsGeometry(final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null || g1.getInnerGeometry() == null || g2.getInnerGeometry() == null) return false;
		return g1.getInnerGeometry().equals(g2.getInnerGeometry());
	}

}
