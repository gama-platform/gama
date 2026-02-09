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
import gama.api.data.objects.IField;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 *
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
			doc = { @doc ("Returns the rectangular shape that corresponds to the 'cell' in the field at this location. This cell has no attributes. A future version may load it with the value of the field at this attribute") })
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
			doc = { @doc ("Returns the rectangular shape that corresponds to the 'cell' in the field at this location in the matrix (column, row). This cell has no attributes. A future version may load it with the value of the field at this attribute") })
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
			doc = { @doc ("""
					Returns the list of 'cells' that 'intersect' with the geometry passed in argument. \
					(Intersection is understood as the cell center is insside the geometry; if the  geometry is a polyline or a point, results will not be accurate.\
					The cells are ordered by their x-, then y-coordinates""") })
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
			doc = { @doc ("""
					Returns the list of 'cells' that 'overlap' the geometry passed in argument. \
					It is much less efficient than the cells_in operator, but is relevant is a polynie or a point. \
					The cells are ordered by their x-, then y-coordinates""") })
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
			doc = { @doc ("Returns the list of values in the field whose 'cell' 'intersects' with the geometry passed in argument. The values are ordered by the x-, then y-coordinate, of their 'cell'") })
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
			doc = { @doc ("Returns the list of values in the field whose 'cell' 'intersects' with the geometry passed in argument. The values are ordered by the x-, then y-coordinate, of their 'cell'") })
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
			doc = { @doc ("Returns the list of the 'neighbors' of a given world coordinate point, which correspond to the world coordinates of the cells that surround the cell located at this point") })
	@no_test
	public static IList<IPoint> getNeighborsOf(final IScope scope, final IField field, final IPoint point) {
		return field.getNeighborsOf(scope, point);
	}

}
