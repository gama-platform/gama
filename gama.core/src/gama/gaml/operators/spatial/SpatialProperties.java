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

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;

/**
 * The Class Properties.
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
					@usage ("if one operand is a point, returns true if the point is included in the geometry") },
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
