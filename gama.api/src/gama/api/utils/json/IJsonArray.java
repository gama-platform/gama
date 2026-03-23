/*******************************************************************************************************
 *
 * IJsonArray.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.json;

import java.util.Iterator;
import java.util.List;

import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;

/**
 *
 */

public interface IJsonArray extends IJsonValue, Iterable<IJsonValue> {

	/**
	 * Appends the JSON representation of the specified <code>int</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	IJsonArray add(int value);

	/**
	 * Appends the JSON representation of the specified <code>long</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	IJsonArray add(long value);

	/**
	 * Appends the JSON representation of the specified <code>float</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	IJsonArray add(float value);

	/**
	 * Appends the JSON representation of the specified <code>double</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	IJsonArray add(double value);

	/**
	 * Appends the JSON representation of the specified <code>boolean</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	IJsonArray add(boolean value);

	/**
	 * Appends the JSON representation of the specified string to the end of this array.
	 *
	 * @param value
	 *            the string to add to the array
	 * @return the array itself, to enable method chaining
	 */
	IJsonArray add(String value);

	/**
	 * Appends the specified JSON value to the end of this array.
	 *
	 * @param value
	 *            the JsonValue to add to the array, must not be <code>null</code>
	 * @return the array itself, to enable method chaining
	 */
	IJsonArray add(IJsonValue value);

	/**
	 * Adds the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @return the json array
	 * @date 29 oct. 2023
	 */
	IJsonArray add(Object object);

	/**
	 * Replaces the element at the specified position in this array with the JSON representation of the specified
	 * <code>int</code> value.
	 *
	 * @param index
	 *            the index of the array element to replace
	 * @param value
	 *            the value to be stored at the specified array position
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonArray set(int index, int value);

	/**
	 * Replaces the element at the specified position in this array with the JSON representation of the specified
	 * <code>long</code> value.
	 *
	 * @param index
	 *            the index of the array element to replace
	 * @param value
	 *            the value to be stored at the specified array position
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonArray set(int index, long value);

	/**
	 * Replaces the element at the specified position in this array with the JSON representation of the specified
	 * <code>float</code> value.
	 *
	 * @param index
	 *            the index of the array element to replace
	 * @param value
	 *            the value to be stored at the specified array position
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonArray set(int index, float value);

	/**
	 * Replaces the element at the specified position in this array with the JSON representation of the specified
	 * <code>double</code> value.
	 *
	 * @param index
	 *            the index of the array element to replace
	 * @param value
	 *            the value to be stored at the specified array position
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonArray set(int index, double value);

	/**
	 * Replaces the element at the specified position in this array with the JSON representation of the specified
	 * <code>boolean</code> value.
	 *
	 * @param index
	 *            the index of the array element to replace
	 * @param value
	 *            the value to be stored at the specified array position
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonArray set(int index, boolean value);

	/**
	 * Replaces the element at the specified position in this array with the JSON representation of the specified
	 * string.
	 *
	 * @param index
	 *            the index of the array element to replace
	 * @param value
	 *            the string to be stored at the specified array position
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonArray set(int index, String value);

	/**
	 * Replaces the element at the specified position in this array with the specified JSON value.
	 *
	 * @param index
	 *            the index of the array element to replace
	 * @param value
	 *            the value to be stored at the specified array position, must not be <code>null</code>
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonArray set(int index, IJsonValue value);

	/**
	 * Removes the element at the specified index from this array.
	 *
	 * @param index
	 *            the index of the element to remove
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonArray remove(int index);

	/**
	 * Returns the number of elements in this array.
	 *
	 * @return the number of elements in this array
	 */
	int size();

	/**
	 * Returns <code>true</code> if this array contains no elements.
	 *
	 * @return <code>true</code> if this array contains no elements
	 */
	boolean isEmpty();

	/**
	 * Returns the value of the element at the specified position in this array.
	 *
	 * @param index
	 *            the index of the array element to return
	 * @return the value of the element at the specified position
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	IJsonValue get(int index);

	/**
	 * Returns a list of the values in this array in document order. The returned list is backed by this array and will
	 * reflect subsequent changes. It cannot be used to modify this array. Attempts to modify the returned list will
	 * result in an exception.
	 *
	 * @return a list of the values in this array
	 */
	List<IJsonValue> values();

	/**
	 * Returns an iterator over the values of this array in document order. The returned iterator cannot be used to
	 * modify this array.
	 *
	 * @return an iterator over the values of this array
	 */
	@Override
	Iterator<IJsonValue> iterator();

	/**
	 * Checks if is array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is array
	 * @date 29 oct. 2023
	 */
	@Override
	boolean isArray();

	/**
	 * As array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the json array
	 * @date 29 oct. 2023
	 */
	@Override
	IJsonArray asArray();

	/**
	 * Hash code.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the int
	 * @date 29 oct. 2023
	 */
	@Override
	int hashCode();

	/**
	 * Indicates whether a given object is "equal to" this JsonArray. An object is considered equal if it is also a
	 * <code>JsonArray</code> and both arrays contain the same list of values.
	 * <p>
	 * If two JsonArrays are equal, they will also produce the same JSON output.
	 * </p>
	 *
	 * @param object
	 *            the object to be compared with this JsonArray
	 * @return <tt>true</tt> if the specified object is equal to this JsonArray, <code>false</code> otherwise
	 */
	@Override
	boolean equals(Object object);

	/**
	 * To gaml value.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 */
	@Override
	IList toGamlValue(IScope scope);

}