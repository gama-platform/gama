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
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaFieldType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.utils.interfaces.IDiffusionTarget;
import gama.api.utils.interfaces.IImageProvider;

/**
 * A matrix of doubles with additionnal attributes that can serve as a lightweight replacement for grids (holding only
 * one value, but covering the whole environment and accessible by agents using their location).
 *
 * @author drogoul
 *
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

	/** The no no data. */
	double NO_NO_DATA = Double.MAX_VALUE;

	/**
	 * Gets the field.
	 *
	 * @param scope
	 *            the scope
	 * @return the field
	 */
	@Override
	default IField getField(final IScope scope) {
		return this;
	}

	/**
	 * Returns the values present in this field **This array should better not modified directly **
	 *
	 * @return the direct double array.
	 */
	double[] getMatrix();

	/**
	 * Returns the value that represent the "absence" of data.
	 *
	 * @return the value to consider
	 */
	@Override
	@getter ("no_data")
	double getNoData(IScope scope);

	/**
	 * Sets the value that is bound to represent the "absence" of data
	 *
	 * @param noData
	 *            the value to consider
	 */
	@setter ("no_data")
	void setNoData(IScope scope, double noData);

	/**
	 * Gets the min max.
	 *
	 * @return the min max
	 */
	double[] getMinMax();

	/**
	 * Returns the bands registered for this field.
	 *
	 * @return a list of fields, never null as the first band is this field itself.
	 */
	@getter ("bands")
	IList<? extends IField> getBands(IScope scope);

	/**
	 * Sets the bands.
	 *
	 * @param scope
	 *            the scope
	 * @param bands
	 *            the bands
	 */
	@setter ("bands")
	default void setBands(final IScope scope, final IList<IField> bands) {
		// Nothing to do by default as this value is supposed to be read-only
	}

	/**
	 * Gets the cell size.
	 *
	 * @param scope
	 *            the scope
	 * @return the cell size
	 */
	@getter ("cell_size")
	IPoint getCellSize(IScope scope);

	/**
	 * Sets the cell size.
	 *
	 * @param scope
	 *            the scope
	 * @param size
	 *            the size
	 */
	@setter ("cell_size")
	default void setCellSize(final IScope scope, final IPoint size) {
		// Nothing to do by default as this value is supposed to be read-only
	}

	/**
	 * Returns the 'cell' (a rectangle shape) that represents the cell at this location
	 *
	 * @param loc
	 *            a world location (location of an agent, for instance)
	 * @return A list of values at this location. Never null nor empty (as there is at least one band).
	 */
	IShape getCellShapeAt(IScope scope, IPoint loc);

	/**
	 * Gets the cell shape at.
	 *
	 * @param scope
	 *            the scope
	 * @param columns
	 *            the columns
	 * @param rows
	 *            the rows
	 * @return the cell shape at
	 */
	IShape getCellShapeAt(IScope scope, int columns, int rows);

	/**
	 * Returns a list of all the values present in the bands at this world location
	 *
	 * @param loc
	 *            a world location (location of an agent, for instance)
	 * @return A list of values at this location. Never null nor empty (as there is at least one band).
	 */
	IList<Double> getValuesIntersecting(IScope scope, IShape shape);

	/**
	 * Returns a list of the 'cells' (rectangle shapes) that intersect the geometry passed in parameter
	 *
	 * @param scope
	 * @param shape
	 * @return
	 */
	IList<IShape> getCellsIntersecting(IScope scope, IShape shape);

	/**
	 * Returns a list of the 'cells' (rectangle shapes) that overlap the geometry passed in parameter
	 *
	 * @param scope
	 * @param shape
	 * @return
	 */
	IList<IShape> getCellsOverlapping(IScope scope, IShape shape);

	/**
	 * Gets the locations intersecting.
	 *
	 * @param scope
	 *            the scope
	 * @param shape
	 *            the shape
	 * @return the locations intersecting
	 */
	IList<IPoint> getLocationsIntersecting(final IScope scope, final IShape shape);

	/**
	 * Gets the neighbors of.
	 *
	 * @param scope
	 *            the scope
	 * @param point
	 *            the point
	 * @return the neighbors of
	 */
	IList<IPoint> getNeighborsOf(IScope scope, IPoint point);

	/**
	 * Gets the gaml type.
	 *
	 * @return the gaml type
	 */
	@Override
	default GamaFieldType getGamlType() { return Types.FIELD; }

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
	 * @param scope
	 * @param colorProvider
	 * @return
	 */
	IField flatten(IScope scope, Object computer);

}
