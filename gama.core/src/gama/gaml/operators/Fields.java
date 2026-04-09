/*******************************************************************************************************
 *
 * Fields.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import gama.annotations.doc;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.matrix.IField;

/**
 * Provides GAML field/grid operators for operating on {@link IField} (raster/field matrix)
 * objects within the GAMA modeling platform. An {@link IField} is a matrix-like raster data
 * structure used for grid-based spatial computations, where each cell stores a floating-point
 * value and is associated with a rectangular geographic area.
 *
 * <p>The following operators are provided:</p>
 * <ul>
 *   <li><strong>{@code cell_at}</strong> – returns the rectangular cell shape at a given
 *       world-coordinate location or (column, row) matrix index.</li>
 *   <li><strong>{@code cells_in}</strong> – returns the list of cell shapes whose centres
 *       are inside a given geometry (centre-point intersection).</li>
 *   <li><strong>{@code cells_overlapping}</strong> – returns the list of cell shapes that
 *       geometrically overlap a given geometry (more precise but slower than
 *       {@code cells_in}).</li>
 *   <li><strong>{@code values_in}</strong> – returns the list of field values for all cells
 *       whose centres intersect a given geometry.</li>
 *   <li><strong>{@code points_in}</strong> – returns the list of cell-centre points for all
 *       cells whose centres intersect a given geometry.</li>
 *   <li><strong>{@code neighbors_of}</strong> – returns the world-coordinate centres of the
 *       cells surrounding a given location.</li>
 * </ul>
 *
 * <p>All operators are annotated {@code @no_test} because they require an active simulation
 * with a properly initialised field that is not available during standalone unit testing.</p>
 *
 * @author GAMA Development Team
 * @see IField
 */
public class Fields {

	/**
	 * Builds the shape from field location.
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param location
	 *            the location
	 * @return the i shape
	 */
	@operator (
			value = "cell_at",
			can_be_const = false,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc (
				value = "Returns the rectangular shape that corresponds to the 'cell' in the field at this location. This cell has no attributes. A future version may load it with the value of the field at this attribute",
				returns = "the rectangular cell shape at the given world coordinate.",
				special_cases = {
					"Returns nil if the location is outside the field boundaries." }) })
	@no_test
	public static IShape buildShapeFromFieldLocation(final IScope scope, final IField field, final IPoint location) {
		return field.getCellShapeAt(scope, location);
	}

	/**
	 * Builds the shape from field location.
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param columns
	 *            the columns
	 * @param rows
	 *            the rows
	 * @return the i shape
	 */
	@operator (
			value = "cell_at",
			can_be_const = false,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc (
				value = "Returns the rectangular shape that corresponds to the 'cell' in the field at this location in the matrix (column, row). This cell has no attributes. A future version may load it with the value of the field at this attribute",
				returns = "the rectangular cell shape at the given (column, row) matrix index.",
				special_cases = {
					"Returns nil if the column or row index is outside the field dimensions." }) })
	@no_test
	public static IShape buildShapeFromFieldLocation(final IScope scope, final IField field, final int columns,
			final int rows) {
		return field.getCellShapeAt(scope, columns, rows);
	}

	/**
	 * Gets the shapes from geometry (cells with a point inside the geometry).
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param shape
	 *            the shape
	 * @return the shapes from geometry
	 */
	@operator (
			value = "cells_in",
			can_be_const = false,
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc (
				value = """
						Returns the list of 'cells' that 'intersect' with the geometry passed in argument. \
						(Intersection is understood as the cell center is insside the geometry; if the  geometry is a polyline or a point, results will not be accurate.\
						The cells are ordered by their x-, then y-coordinates""",
				returns = "a list of {@code geometry} shapes for all cells whose centre is inside the given geometry, ordered by x- then y-coordinate. Returns an empty list if no cells match.",
				special_cases = {
					"If the geometry is a polyline or a point, results may not be accurate because intersection is tested against cell centres.",
					"Returns an empty list if the geometry does not overlap the field." }) })
	@no_test
	public static IList<IShape> getShapesFromGeometry(final IScope scope, final IField field, final IShape shape) {
		return field.getCellsIntersecting(scope, shape);
	}

	/**
	 * Gets the shapes from geometry (cells overlapping the geometry).
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param shape
	 *            the shape
	 * @return the shapes from geometry
	 */
	@operator (
			value = "cells_overlapping",
			can_be_const = false,
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc (
				value = """
						Returns the list of 'cells' that 'overlap' the geometry passed in argument. \
						It is much less efficient than the cells_in operator, but is relevant is a polynie or a point. \
						The cells are ordered by their x-, then y-coordinates""",
				returns = "a list of {@code geometry} shapes for all cells that overlap the given geometry, ordered by x- then y-coordinate.",
				special_cases = {
					"Much less efficient than cells_in for polygon geometries; prefer cells_in when centre-based intersection is sufficient.",
					"Particularly useful when the query geometry is a polyline or a point.",
					"Returns an empty list if the geometry does not overlap the field." }) })
	@no_test
	public static IList<IShape> getShapesOverGeometry(final IScope scope, final IField field, final IShape shape) {
		return field.getCellsOverlapping(scope, shape);
	}

	/**
	 * Gets the values from geometry.
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param shape
	 *            the shape
	 * @return the values from geometry
	 */
	@operator (
			value = "values_in",
			can_be_const = false,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc (
					value = "Returns the list of values in the field whose 'cell' 'intersects' with the geometry passed in argument. The values are ordered by the x-, then y-coordinate, of their 'cell'",
					returns = "a list of {@code float} values from the cells whose centres fall inside the given geometry, ordered by x- then y-coordinate.",
					special_cases = { "Returns an empty list if no cells intersect the geometry." }) })
	@no_test
	public static IList<Double> getValuesFromGeometry(final IScope scope, final IField field, final IShape shape) {
		return field.getValuesIntersecting(scope, shape);
	}

	/**
	 * Gets the points from geometry.
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param shape
	 *            the shape
	 * @return the points from geometry
	 */
	@operator (
			value = "points_in",
			can_be_const = false,
			content_type = IType.POINT,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc (
					value = "Returns the list of values in the field whose 'cell' 'intersects' with the geometry passed in argument. The values are ordered by the x-, then y-coordinate, of their 'cell'",
					returns = "a list of {@code point} values representing the centres of cells whose centres fall inside the given geometry, ordered by x- then y-coordinate.",
					special_cases = { "Returns an empty list if no cells intersect the geometry." }) })
	@no_test
	public static IList<IPoint> getPointsFromGeometry(final IScope scope, final IField field, final IShape shape) {
		return field.getLocationsIntersecting(scope, shape);
	}

	/**
	 * Gets the neighbors of.
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param point
	 *            the point
	 * @return the neighbors of
	 */
	@operator (
			value = "neighbors_of",
			can_be_const = false,
			content_type = IType.POINT,
			category = { IOperatorCategory.GRID },
			concept = { IConcept.GRID },
			doc = { @doc (
				value = "Returns the list of the 'neighbors' of a given world coordinate point, which correspond to the world coordinates of the cells that surround the cell located at this point",
				returns = "a list of {@code point} values representing the world-coordinate centres of the cells surrounding the given location.",
				special_cases = {
					"Cells on the boundary of the field have fewer neighbours.",
					"Returns an empty list if the given point is outside the field boundaries." }) })
	@no_test
	public static IList<IPoint> getNeighborsOf(final IScope scope, final IField field, final IPoint point) {
		return field.getNeighborsOf(scope, point);
	}

}
