/*******************************************************************************************************
 *
 * IField.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.matrix;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.operator;
import gama.annotations.setter;
import gama.annotations.test;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.utils.interfaces.IDiffusionTarget;
import gama.api.utils.interfaces.IFieldMatrixProvider;
import gama.api.utils.interfaces.IImageProvider;

/**
 * A specialized matrix of doubles designed as a lightweight replacement for grids in GAMA.
 *
 * <p>
 * Fields are two-dimensional continuous data structures that hold a single double value per cell, covering the entire
 * environment. They provide spatial awareness, allowing agents to query field values using their world location. Fields
 * are particularly useful for representing environmental variables (elevation, temperature, pollution, etc.) and
 * support diffusion operations.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 * <li><strong>Double precision storage:</strong> Each cell stores a double value</li>
 * <li><strong>No-data value support:</strong> Ability to define a special value representing missing or invalid
 * data</li>
 * <li><strong>Cell size awareness:</strong> Each cell has a defined spatial dimension</li>
 * <li><strong>Multi-band support:</strong> Can contain multiple layers (bands) of data, similar to multi-band raster
 * images</li>
 * <li><strong>Spatial queries:</strong> Agents can query values based on their location, get neighboring cells, find
 * intersecting cells</li>
 * <li><strong>Diffusion support:</strong> Implements {@link IDiffusionTarget} for simulating diffusion processes</li>
 * <li><strong>Image provider:</strong> Can be visualized as images through {@link IImageProvider}</li>
 * </ul>
 *
 * <h2>Available Variables</h2>
 * <ul>
 * <li><strong>no_data</strong> (float) - The value indicating absence of data (default: {@link #NO_NO_DATA}). Setting
 * this only changes interpretation, not actual values</li>
 * <li><strong>cell_size</strong> (point) - The dimension of individual cells as {width, height}. Setting this only
 * changes interpretation</li>
 * <li><strong>bands</strong> (list&lt;field&gt;) - Optional list of bands present in the field. The first band is the
 * field itself</li>
 * </ul>
 *
 * <h2>Spatial Operations</h2>
 * <ul>
 * <li>{@link #getCellShapeAt(IScope, IPoint)} - Get the rectangle shape representing a cell at a location</li>
 * <li>{@link #getValuesIntersecting(IScope, IShape)} - Get all values at cells intersecting a geometry</li>
 * <li>{@link #getCellsIntersecting(IScope, IShape)} - Get all cell shapes intersecting a geometry</li>
 * <li>{@link #getCellsOverlapping(IScope, IShape)} - Get all cell shapes overlapping a geometry</li>
 * <li>{@link #getNeighborsOf(IScope, IPoint)} - Get neighboring cell locations</li>
 * </ul>
 *
 * <h2>Mathematical Operations</h2>
 * <p>
 * Fields support element-wise arithmetic operations with other fields or scalar values:
 * </p>
 * <ul>
 * <li>Addition: field + field, field + double, field + int</li>
 * <li>Subtraction: field - field, field - double, field - int</li>
 * <li>Multiplication: field * field, field * double, field * int</li>
 * <li>Division: field / field, field / double, field / int</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>
 * // Create a field
 * IField elevation = GamaMatrixFactory.createField(scope, 100, 100);
 * elevation.setNoData(scope, -9999.0);
 *
 * // Query field at agent location
 * IPoint agentLocation = agent.getLocation();
 * double value = elevation.get(scope, agentLocation);
 *
 * // Get cells intersecting a geometry
 * IList&lt;IShape&gt; cells = elevation.getCellsIntersecting(scope, someShape);
 *
 * // Perform arithmetic
 * IField combined = elevation.plus(scope, temperature.times(0.5));
 * </pre>
 *
 * @author drogoul
 * @since GAMA 1.0
 */
@vars ({ @variable (
		name = "no_data",
		type = IType.FLOAT,
		doc = @doc ("""
				Represents the value that indicates the absence of data. \
				Setting it will only change the interpretation made by the field \
				of the values it contains, but not the values themselves""")),
		@variable (
				name = "cell_size",
				type = IType.POINT,
				doc = @doc ("""
						Represents the dimension of an individual cell as a point (width, height)\
						Setting it will only change the interpretation made by the field \
						of the values it contains, but not the values themselves""")),
		@variable (
				name = "bands",
				type = IType.LIST,
				of = IType.FIELD,
				doc = @doc ("The list of bands that are optionnaly present in the field. The first band is the primary field itself, and each of these bands is a field w/o bands ")) })
public interface IField extends IMatrix<Double>, IDiffusionTarget, IImageProvider {

	/**
	 * Constant representing the absence of a "no-data" value. When a field's no-data value equals this constant, it
	 * means all values in the field are considered valid data.
	 */
	double NO_NO_DATA = Double.MAX_VALUE;

	/**
	 * Returns the field itself (identity operation).
	 *
	 * <p>
	 * This method is part of the {@link IFieldMatrixProvider} interface and simply returns this field instance.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return this field instance
	 */
	@Override
	default IField getField(final IScope scope) {
		return this;
	}

	/**
	 * Returns the direct internal double array containing all field values.
	 *
	 * <p>
	 * <strong>Warning:</strong> This array should preferably not be modified directly as it may bypass internal
	 * consistency checks. The array is stored in row-major order.
	 * </p>
	 *
	 * @return the internal double array containing all field values
	 */
	double[] getMatrix();

	/**
	 * Gets the no-data value for this field.
	 *
	 * <p>
	 * The no-data value is a special double value that represents the absence or invalidity of data. It is commonly
	 * used in raster datasets to indicate cells where no measurement was taken or data is not applicable.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return the no-data value, or {@link #NO_NO_DATA} if no special value is set
	 */
	@Override
	@getter ("no_data")
	double getNoData(IScope scope);

	/**
	 * Sets the no-data value for this field.
	 *
	 * <p>
	 * Setting the no-data value only changes how the field interprets its values; it does not modify the actual cell
	 * values. Cells containing this value will be treated as having no valid data.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param noData
	 *            the value to consider as representing absent data
	 */
	@setter ("no_data")
	void setNoData(IScope scope, double noData);

	/**
	 * Gets the minimum and maximum values present in this field.
	 *
	 * <p>
	 * This method computes or retrieves the range of values in the field, excluding the no-data value if one is set.
	 * </p>
	 *
	 * @return a double array of length 2: [minimum_value, maximum_value]
	 */
	double[] getMinMax();

	/**
	 * Gets the list of bands associated with this field.
	 *
	 * <p>
	 * Multi-band fields are similar to multi-band raster images (like RGB images or multi-spectral satellite imagery).
	 * The first band in the list is always this field itself, and subsequent bands are independent fields without their
	 * own bands.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return a list of fields representing the bands, never null (at minimum contains this field)
	 */
	@getter ("bands")
	IList<? extends IField> getBands(IScope scope);

	/**
	 * Sets the bands for this field.
	 *
	 * <p>
	 * By default, this method does nothing as bands are typically read-only. Implementations may override this to
	 * support mutable band collections.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param bands
	 *            the list of fields to set as bands
	 */
	@setter ("bands")
	default void setBands(final IScope scope, final IList<IField> bands) {
		// Nothing to do by default as this value is supposed to be read-only
	}

	/**
	 * Gets the spatial size of individual cells in world coordinates.
	 *
	 * <p>
	 * The cell size determines how field indices map to world coordinates and affects spatial queries.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @return a point representing cell dimensions as {width, height}
	 */
	@getter ("cell_size")
	IPoint getCellSize(IScope scope);

	/**
	 * Sets the spatial size of individual cells.
	 *
	 * <p>
	 * Setting the cell size only changes the interpretation of field coordinates; it does not modify cell values. By
	 * default, this method does nothing as cell size is typically read-only.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param size
	 *            the new cell size as {width, height}
	 */
	@setter ("cell_size")
	default void setCellSize(final IScope scope, final IPoint size) {
		// Nothing to do by default as this value is supposed to be read-only
	}

	/**
	 * Gets the shape (rectangle) representing the cell at a given world location.
	 *
	 * <p>
	 * This method converts a world coordinate to the corresponding cell and returns a rectangular geometry representing
	 * that cell's spatial extent.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param loc
	 *            a world location (e.g., an agent's location)
	 * @return a rectangular shape representing the cell at that location
	 */
	IShape getCellShapeAt(IScope scope, IPoint loc);

	/**
	 * Gets the shape (rectangle) representing the cell at specific column and row indices.
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param columns
	 *            the column index
	 * @param rows
	 *            the row index
	 * @return a rectangular shape representing the cell at those indices
	 */
	IShape getCellShapeAt(IScope scope, int columns, int rows);

	/**
	 * Gets all field values (from all bands) that intersect with a given geometry.
	 *
	 * <p>
	 * This method finds all cells whose spatial extent intersects the provided shape and returns their values from all
	 * bands.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param shape
	 *            the geometry to test for intersection
	 * @return a list of double values from cells intersecting the shape (never null or empty as there is at least one
	 *         band)
	 */
	IList<Double> getValuesIntersecting(IScope scope, IShape shape);

	/**
	 * Gets all cell shapes (rectangles) that intersect with a given geometry.
	 *
	 * <p>
	 * Returns the spatial rectangles of all cells whose extent intersects the provided shape.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param shape
	 *            the geometry to test for intersection
	 * @return a list of cell shapes (rectangles) intersecting the shape
	 */
	IList<IShape> getCellsIntersecting(IScope scope, IShape shape);

	/**
	 * Gets all cell shapes (rectangles) that overlap with a given geometry.
	 *
	 * <p>
	 * Similar to {@link #getCellsIntersecting(IScope, IShape)} but may use different overlap semantics depending on
	 * implementation.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param shape
	 *            the geometry to test for overlap
	 * @return a list of cell shapes (rectangles) overlapping the shape
	 */
	IList<IShape> getCellsOverlapping(IScope scope, IShape shape);

	/**
	 * Gets the grid locations (as points) of all cells intersecting a given geometry.
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param shape
	 *            the geometry to test for intersection
	 * @return a list of points representing cell indices that intersect the shape
	 */
	IList<IPoint> getLocationsIntersecting(final IScope scope, final IShape shape);

	/**
	 * Gets the neighboring cell locations of a given point.
	 *
	 * <p>
	 * Returns the grid locations of cells adjacent to the specified point, typically using 4-connectivity or
	 * 8-connectivity depending on implementation.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param point
	 *            the reference point (grid location)
	 * @return a list of neighboring cell locations
	 */
	IList<IPoint> getNeighborsOf(IScope scope, IPoint point);

	/**
	 * Plus.
	 *
	 * @param scope
	 *            the scope
	 * @param other
	 *            the other
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc ("Returns a field containing the addition of  the elements of two fields in argument "))
	@test ("field([[1,2],[3,4]]) + field([[1,2],[3,4]]) = field([[2,4],[6,8]])")
	IField plus(IScope scope, IField other) throws GamaRuntimeException;

	/**
	 * Times.
	 *
	 * @param scope
	 *            the scope
	 * @param other
	 *            the other
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc ("Multiplies the two field operands"))
	@test ("field([[1,2],[3,4]]) * field([[1,2],[3,4]]) = field([[1,4],[9,16]]) ")
	IField times(IScope scope, IField other) throws GamaRuntimeException;

	/**
	 * Divides.
	 *
	 * @param scope
	 *            the scope
	 * @param other
	 *            the other
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			content_type = IType.FLOAT,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc ("Divides the two field operands"))
	@test ("field([[1,2],[3,4]]) / field([[1,2],[3,4]]) = field([[1,1],[1,1]])")
	IField divides(IScope scope, IField other) throws GamaRuntimeException;

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param other
	 *            the other
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Performs a subtraction between the two field operands"))
	@test ("field([[1,2],[3,4]]) - field([[1,2],[3,4]]) = field([[0,0],[0,0]])")
	IField minus(IScope scope, IField other) throws GamaRuntimeException;

	/**
	 * Times.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Performs a multiplication between the field operand and the float operand"))
	@test ("field([[1,2],[3,4]]) * 2.5 = field([[2.5,5.0],[7.5,10]])")
	IField times(Double val) throws GamaRuntimeException;

	/**
	 * Times.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = { IConcept.MATRIX },
			doc = @doc ("Performs a multiplication between the two field operands"))
	@test ("field([[1,2],[3,4]]) * 2 = field([[2,4],[6,8]])")
	IField times(Integer val) throws GamaRuntimeException;

	/**
	 * Divides.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Divides all the elements of the field operand by the float operand"))
	@test ("field([[1,2],[3,4]]) / 2.5 = field([[0.4,0.8],[1.2,1.6]])")
	IField divides(Double val) throws GamaRuntimeException;

	/**
	 * Divides.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Divides all the elements of the field operand by the integer operand"))
	@test ("field([[1,2],[3,4]]) / 2 = field([[0.5,1],[1.5,2]])")
	IField divides(Integer val) throws GamaRuntimeException;

	/**
	 * Plus.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Adds the float operand to all the elements in the field"))
	@test ("field([[1,2],[3,4]]) + 22.5 = field([[23.5,24.5],[25.5,26.5]])")
	IField plus(Double val) throws GamaRuntimeException;

	/**
	 * Plus.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Adds the int operand to all the elements in the field"))
	@test ("field([[1,2],[3,4]]) + 2 = field([[3,4],[5,6]])")
	IField plus(Integer val) throws GamaRuntimeException;

	/**
	 * Minus.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Subtracts the float operand from all the elements in the field"))
	@test ("field([[1,2],[3,4]]) - 1.5 = field([[-0.5,0.5],[1.5,2.5]])")
	IField minus(Double val) throws GamaRuntimeException;

	/**
	 * Minus.
	 *
	 * @param val
	 *            the val
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MATRIX },
			concept = {},
			doc = @doc ("Subtracts the int operand from all the elements in the field"))
	@test ("field([[1,2],[3,4]]) - 1 = field([[0,1],[2,3]])")
	IField minus(Integer val) throws GamaRuntimeException;

	/**
	 * Flattens or processes the field using a custom computer/provider.
	 *
	 * <p>
	 * This method applies a computation or transformation to the field, potentially reducing multi-band data or
	 * applying a custom color mapping. The exact behavior depends on the computer implementation provided.
	 * </p>
	 *
	 * @param scope
	 *            the current GAMA execution scope
	 * @param computer
	 *            the computation provider to apply to the field
	 * @return a new processed field
	 */
	IField flatten(IScope scope, Object computer);

}
