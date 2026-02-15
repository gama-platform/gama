/*******************************************************************************************************
 *
 * JsonArray.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import gama.api.data.json.IJson;
import gama.api.data.json.IJsonArray;
import gama.api.data.json.IJsonValue;
import gama.api.data.objects.IList;
import gama.api.runtime.scope.IScope;
import gama.api.utils.list.GamaListFactory;

/**
 * Represents a JSON array, an ordered collection of JSON values.
 * <p>
 * Elements can be added using the <code>add(...)</code> methods which accept instances of {@link JsonValue}, strings,
 * primitive numbers, and boolean values. To replace an element of an array, use the <code>set(int, ...)</code> methods.
 * </p>
 * <p>
 * Elements can be accessed by their index using {@link #get(int)}. This class also supports iterating over the elements
 * in document order using an {@link #iterator()} or an enhanced for loop:
 * </p>
 *
 * <pre>
 * for (JsonValue value : jsonArray) {
 *   ...
 * }
 * </pre>
 * <p>
 * An equivalent {@link List} can be obtained from the method {@link #values()}.
 * </p>
 * <p>
 * Note that this class is <strong>not thread-safe</strong>. If multiple threads access a <code>JsonArray</code>
 * instance concurrently, while at least one of these threads modifies the contents of this array, access to the
 * instance must be synchronized externally. Failure to do so may lead to an inconsistent state.
 * </p>
 * <p>
 * This class is <strong>not supposed to be extended</strong> by clients.
 * </p>
 */
@SuppressWarnings ("serial") // use default serial UID
public class JsonArray extends JsonValue implements IJsonArray {

	/** The values. */
	private final List<IJsonValue> values;

	/** The json. */
	private final IJson json;

	/**
	 * Creates a new empty JsonArray.
	 */
	JsonArray(final IJson json) {
		this.json = json;
		values = new ArrayList<>();
	}

	/**
	 * Appends the JSON representation of the specified <code>int</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	@Override
	public IJsonArray add(final int value) {
		values.add(json.valueOf(value));
		return this;
	}

	/**
	 * Appends the JSON representation of the specified <code>long</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	@Override
	public IJsonArray add(final long value) {
		values.add(json.valueOf(value));
		return this;
	}

	/**
	 * Appends the JSON representation of the specified <code>float</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	@Override
	public IJsonArray add(final float value) {
		values.add(json.valueOf(value));
		return this;
	}

	/**
	 * Appends the JSON representation of the specified <code>double</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	@Override
	public IJsonArray add(final double value) {
		values.add(json.valueOf(value));
		return this;
	}

	/**
	 * Appends the JSON representation of the specified <code>boolean</code> value to the end of this array.
	 *
	 * @param value
	 *            the value to add to the array
	 * @return the array itself, to enable method chaining
	 */
	@Override
	public IJsonArray add(final boolean value) {
		values.add(json.valueOf(value));
		return this;
	}

	/**
	 * Appends the JSON representation of the specified string to the end of this array.
	 *
	 * @param value
	 *            the string to add to the array
	 * @return the array itself, to enable method chaining
	 */
	@Override
	public IJsonArray add(final String value) {
		values.add(json.valueOf(value));
		return this;
	}

	/**
	 * Appends the specified JSON value to the end of this array.
	 *
	 * @param value
	 *            the JsonValue to add to the array, must not be <code>null</code>
	 * @return the array itself, to enable method chaining
	 */
	@Override
	public IJsonArray add(final IJsonValue value) {
		if (!(value instanceof JsonValue jv)) throw new RuntimeException("Wrong value type");
		values.add(jv);
		return this;
	}

	/**
	 * Adds the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @return the json array
	 * @date 29 oct. 2023
	 */
	@Override
	public IJsonArray add(final Object object) {
		values.add(json.valueOf(object));
		return this;
	}

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
	@Override
	public IJsonArray set(final int index, final int value) {
		values.set(index, json.valueOf(value));
		return this;
	}

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
	@Override
	public IJsonArray set(final int index, final long value) {
		values.set(index, json.valueOf(value));
		return this;
	}

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
	@Override
	public IJsonArray set(final int index, final float value) {
		values.set(index, json.valueOf(value));
		return this;
	}

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
	@Override
	public IJsonArray set(final int index, final double value) {
		values.set(index, json.valueOf(value));
		return this;
	}

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
	@Override
	public IJsonArray set(final int index, final boolean value) {
		values.set(index, json.valueOf(value));
		return this;
	}

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
	@Override
	public IJsonArray set(final int index, final String value) {
		values.set(index, json.valueOf(value));
		return this;
	}

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
	@Override
	public IJsonArray set(final int index, final IJsonValue value) {
		if (!(value instanceof JsonValue jv)) throw new RuntimeException("Wrong value type");
		values.set(index, jv);
		return this;
	}

	/**
	 * Removes the element at the specified index from this array.
	 *
	 * @param index
	 *            the index of the element to remove
	 * @return the array itself, to enable method chaining
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	@Override
	public IJsonArray remove(final int index) {
		values.remove(index);
		return this;
	}

	/**
	 * Returns the number of elements in this array.
	 *
	 * @return the number of elements in this array
	 */
	@Override
	public int size() {
		return values.size();
	}

	/**
	 * Returns <code>true</code> if this array contains no elements.
	 *
	 * @return <code>true</code> if this array contains no elements
	 */
	@Override
	public boolean isEmpty() { return values.isEmpty(); }

	/**
	 * Returns the value of the element at the specified position in this array.
	 *
	 * @param index
	 *            the index of the array element to return
	 * @return the value of the element at the specified position
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range, i.e. <code>index &lt; 0</code> or <code>index &gt;= size</code>
	 */
	@Override
	public IJsonValue get(final int index) {
		return values.get(index);
	}

	/**
	 * Returns a list of the values in this array in document order. The returned list is backed by this array and will
	 * reflect subsequent changes. It cannot be used to modify this array. Attempts to modify the returned list will
	 * result in an exception.
	 *
	 * @return a list of the values in this array
	 */
	@Override
	public List<IJsonValue> values() {
		return Collections.unmodifiableList(values);
	}

	/**
	 * Returns an iterator over the values of this array in document order. The returned iterator cannot be used to
	 * modify this array.
	 *
	 * @return an iterator over the values of this array
	 */
	@Override
	public Iterator<IJsonValue> iterator() {
		final Iterator<? extends IJsonValue> iterator = values.iterator();
		return new Iterator<>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public IJsonValue next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Write.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param writer
	 *            the writer
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 29 oct. 2023
	 */
	@Override
	public void write(final JsonWriter writer) throws IOException {
		writer.writeArrayOpen();
		Iterator<? extends IJsonValue> iterator = iterator();
		if (iterator.hasNext()) {
			JsonValue first = (JsonValue) iterator.next();
			first.write(writer);
			while (iterator.hasNext()) {
				writer.writeArraySeparator();
				((JsonValue) iterator.next()).write(writer);
			}
		}
		writer.writeArrayClose();
	}

	/**
	 * Checks if is array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is array
	 * @date 29 oct. 2023
	 */
	@Override
	public boolean isArray() { return true; }

	/**
	 * As array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the json array
	 * @date 29 oct. 2023
	 */
	@Override
	public IJsonArray asArray() {
		return this;
	}

	/**
	 * Hash code.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the int
	 * @date 29 oct. 2023
	 */
	@Override
	public int hashCode() {
		return values.hashCode();
	}

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
	public boolean equals(final Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		JsonArray other = (JsonArray) object;
		return values.equals(other.values);
	}

	@Override
	public IList toGamlValue(final IScope scope) {
		IList<Object> result = GamaListFactory.create();
		for (IJsonValue v : values) { result.add(v.toGamlValue(scope)); }
		return result;
	}

}
