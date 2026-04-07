/*******************************************************************************************************
 *
 * JsonAbstractObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gama.api.runtime.scope.IScope;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.json.IJsonValue;

/**
 * Represents an abstract JSON object, a set of name/value pairs, where the names are strings and the values are JSON
 * values.
 * <p>
 * Members can be added using the <code>add(String, ...)</code> methods which accept instances of {@link IJsonValue},
 * strings, primitive numbers, and boolean values. To modify certain values of an object, use the
 * <code>set(String, ...)</code> methods. Please note that the <code>add</code> methods are faster than <code>set</code>
 * as they do not search for existing members. On the other hand, the <code>add</code> methods do not prevent adding
 * multiple members with the same name. Duplicate names are discouraged but not prohibited by JSON.
 * </p>
 * <p>
 * Members can be accessed by their name using {@link #get(String)}. A list of all names can be obtained from the method
 * {@link #names()}. This class also supports iterating over the members in document order using an {@link #iterator()}
 * or an enhanced for loop:
 * </p>
 *
 * <pre>
 * for (Member member : jsonObject) {
 *   String name = member.getName();
 *   JsonValue value = member.getValue();
 *   ...
 * }
 * </pre>
 * <p>
 * Even though JSON objects are unordered by definition, instances of this class preserve the order of members to allow
 * processing in document order and to guarantee a predictable output.
 * </p>
 * <p>
 * Note that this class is <strong>not thread-safe</strong>. If multiple threads access a <code>JsonObject</code>
 * instance concurrently, while at least one of these threads modifies the contents of this object, access to the
 * instance must be synchronized externally. Failure to do so may lead to an inconsistent state.
 * </p>
 *
 */
@SuppressWarnings ("serial") // use default serial UID
public abstract class JsonAbstractObject extends JsonValue implements IJsonObject {

	/** The map. */
	protected final LinkedHashMap<String, JsonValue> members = new LinkedHashMap<>();

	/** The json. */
	protected final IJson json;

	/**
	 * Creates a new empty JsonObject.
	 */
	JsonAbstractObject(final IJson json) {
		this.json = json;
	}

	/**
	 * Appends a new member to the end of this object, with the specified name and the JSON representation of the
	 * object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the name
	 * @param object
	 *            the object
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	@Override
	public IJsonObject add(final String name, final Object object) {
		add(name, json.valueOf(object));
		return this;
	}

	/**
	 * Appends a new member to the end of this object, with the specified name and the JSON representation of the
	 * specified <code>int</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Calling this method with a name that already
	 * exists in the object will append another member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong> <em>add</em> is much faster than
	 * <em>set</em></strong> (because it does not need to search for existing members). Therefore <em>add</em> should be
	 * preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	@Override
	public IJsonObject add(final String name, final int value) {
		add(name, json.valueOf(value));
		return this;
	}

	/**
	 * Appends a new member to the end of this object, with the specified name and the JSON representation of the
	 * specified <code>long</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Calling this method with a name that already
	 * exists in the object will append another member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong> <em>add</em> is much faster than
	 * <em>set</em></strong> (because it does not need to search for existing members). Therefore <em>add</em> should be
	 * preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	@Override
	public IJsonObject add(final String name, final long value) {
		add(name, json.valueOf(value));
		return this;
	}

	/**
	 * Appends a new member to the end of this object, with the specified name and the JSON representation of the
	 * specified <code>float</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Calling this method with a name that already
	 * exists in the object will append another member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong> <em>add</em> is much faster than
	 * <em>set</em></strong> (because it does not need to search for existing members). Therefore <em>add</em> should be
	 * preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	@Override
	public IJsonObject add(final String name, final float value) {
		add(name, json.valueOf(value));
		return this;
	}

	/**
	 * Appends a new member to the end of this object, with the specified name and the JSON representation of the
	 * specified <code>double</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Calling this method with a name that already
	 * exists in the object will append another member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong> <em>add</em> is much faster than
	 * <em>set</em></strong> (because it does not need to search for existing members). Therefore <em>add</em> should be
	 * preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	@Override
	public IJsonObject add(final String name, final double value) {
		add(name, json.valueOf(value));
		return this;
	}

	/**
	 * Appends a new member to the end of this object, with the specified name and the JSON representation of the
	 * specified <code>boolean</code> value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Calling this method with a name that already
	 * exists in the object will append another member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong> <em>add</em> is much faster than
	 * <em>set</em></strong> (because it does not need to search for existing members). Therefore <em>add</em> should be
	 * preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	@Override
	public IJsonObject add(final String name, final boolean value) {
		add(name, json.valueOf(value));
		return this;
	}

	/**
	 * Appends a new member to the end of this object, with the specified name and the JSON representation of the
	 * specified string.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Calling this method with a name that already
	 * exists in the object will append another member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong> <em>add</em> is much faster than
	 * <em>set</em></strong> (because it does not need to search for existing members). Therefore <em>add</em> should be
	 * preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add
	 * @return the object itself, to enable method chaining
	 */
	@Override
	public IJsonObject add(final String name, final String value) {
		add(name, json.valueOf(value));
		return this;
	}

	/**
	 * Appends a new member to the end of this object, with the specified name and the specified JSON value.
	 * <p>
	 * This method <strong>does not prevent duplicate names</strong>. Calling this method with a name that already
	 * exists in the object will append another member with the same name. In order to replace existing members, use the
	 * method <code>set(name, value)</code> instead. However, <strong> <em>add</em> is much faster than
	 * <em>set</em></strong> (because it does not need to search for existing members). Therefore <em>add</em> should be
	 * preferred when constructing new objects.
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add, must not be <code>null</code>
	 * @return the object itself, to enable method chaining
	 */
	@Override
	public IJsonObject add(final String name, final IJsonValue value) {
		if (name == null) throw new NullPointerException("name is null");
		if (!(value instanceof JsonValue jv)) throw new RuntimeException("value is not correct");
		members.put(name, jv);
		// table.add(name, names.size());
		// names.add(name);
		// values.add(value);
		return this;
	}

	/**
	 * Sets the value of the member with the specified name to the specified JSON value. If this object does not contain
	 * a member with this name, a new member is added at the end of the object. If this object contains multiple members
	 * with this name, only the last one is changed.
	 * <p>
	 * This method should <strong>only be used to modify existing objects</strong>. To fill a new object with members,
	 * the method <code>add(name, value)</code> should be preferred which is much faster (as it does not need to search
	 * for existing members).
	 * </p>
	 *
	 * @param name
	 *            the name of the member to add
	 * @param value
	 *            the value of the member to add, must not be <code>null</code>
	 * @return the object itself, to enable method chaining
	 */
	@Override
	public IJsonObject set(final String name, final IJsonValue value) {
		if (name == null) throw new NullPointerException("name is null");
		if (!(value instanceof JsonValue jv)) throw new RuntimeException("value is not correct");
		members.put(name, jv);
		return this;
	}

	/**
	 * Removes a member with the specified name from this object. If this object contains multiple members with the
	 * given name, only the last one is removed. If this object does not contain a member with the specified name, the
	 * object is not modified.
	 *
	 * @param name
	 *            the name of the member to remove
	 * @return the value that has been removed or null if the name was not present
	 */
	@Override
	public IJsonValue remove(final String name) {
		if (name == null) throw new NullPointerException("name is null");
		return members.remove(name);
	}

	/**
	 * Checks if a specified member is present as a child of this object. This will not test if this object contains the
	 * literal <code>null</code>, {@link JsonValue#isNull()} should be used for this purpose.
	 *
	 * @param name
	 *            the name of the member to check for
	 * @return whether or not the member is present
	 */
	@Override
	public boolean contains(final String name) {
		return members.containsKey(name);
	}

	/**
	 * Returns the value of the member with the specified name in this object. If this object contains multiple members
	 * with the given name, this method will return the last one.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @return the value of the last member with the specified name, or <code>null</code> if this object does not
	 *         contain a member with that name
	 */
	@Override
	public IJsonValue get(final String name) {
		if (name == null) throw new NullPointerException("name is null");
		return members.get(name);
		// int index = indexOf(name);
		// return index != -1 ? values.get(index) : null;
	}

	/**
	 * Returns the <code>int</code> value of the member with the specified name in this object. If this object does not
	 * contain a member with this name, the given default value is returned. If this object contains multiple members
	 * with the given name, the last one will be picked. If this member's value does not represent a JSON number or if
	 * it cannot be interpreted as Java <code>int</code>, an exception is thrown.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @param defaultValue
	 *            the value to be returned if the requested member is missing
	 * @return the value of the last member with the specified name, or the given default value if this object does not
	 *         contain a member with that name
	 */
	@Override
	public int getInt(final String name, final int defaultValue) {
		IJsonValue value = get(name);
		return value != null ? value.asInt() : defaultValue;
	}

	/**
	 * Returns the <code>long</code> value of the member with the specified name in this object. If this object does not
	 * contain a member with this name, the given default value is returned. If this object contains multiple members
	 * with the given name, the last one will be picked. If this member's value does not represent a JSON number or if
	 * it cannot be interpreted as Java <code>long</code>, an exception is thrown.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @param defaultValue
	 *            the value to be returned if the requested member is missing
	 * @return the value of the last member with the specified name, or the given default value if this object does not
	 *         contain a member with that name
	 */
	@Override
	public long getLong(final String name, final long defaultValue) {
		IJsonValue value = get(name);
		return value != null ? value.asLong() : defaultValue;
	}

	/**
	 * Returns the <code>float</code> value of the member with the specified name in this object. If this object does
	 * not contain a member with this name, the given default value is returned. If this object contains multiple
	 * members with the given name, the last one will be picked. If this member's value does not represent a JSON number
	 * or if it cannot be interpreted as Java <code>float</code>, an exception is thrown.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @param defaultValue
	 *            the value to be returned if the requested member is missing
	 * @return the value of the last member with the specified name, or the given default value if this object does not
	 *         contain a member with that name
	 */
	@Override
	public float getFloat(final String name, final float defaultValue) {
		IJsonValue value = get(name);
		return value != null ? value.asFloat() : defaultValue;
	}

	/**
	 * Returns the <code>double</code> value of the member with the specified name in this object. If this object does
	 * not contain a member with this name, the given default value is returned. If this object contains multiple
	 * members with the given name, the last one will be picked. If this member's value does not represent a JSON number
	 * or if it cannot be interpreted as Java <code>double</code>, an exception is thrown.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @param defaultValue
	 *            the value to be returned if the requested member is missing
	 * @return the value of the last member with the specified name, or the given default value if this object does not
	 *         contain a member with that name
	 */
	@Override
	public double getDouble(final String name, final double defaultValue) {
		IJsonValue value = get(name);
		return value != null ? value.asDouble() : defaultValue;
	}

	/**
	 * Returns the <code>boolean</code> value of the member with the specified name in this object. If this object does
	 * not contain a member with this name, the given default value is returned. If this object contains multiple
	 * members with the given name, the last one will be picked. If this member's value does not represent a JSON
	 * <code>true</code> or <code>false</code> value, an exception is thrown.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @param defaultValue
	 *            the value to be returned if the requested member is missing
	 * @return the value of the last member with the specified name, or the given default value if this object does not
	 *         contain a member with that name
	 */
	@Override
	public boolean getBoolean(final String name, final boolean defaultValue) {
		IJsonValue value = get(name);
		return value != null ? value.asBoolean() : defaultValue;
	}

	/**
	 * Returns the <code>String</code> value of the member with the specified name in this object. If this object does
	 * not contain a member with this name, the given default value is returned. If this object contains multiple
	 * members with the given name, the last one is picked. If this member's value does not represent a JSON string, an
	 * exception is thrown.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @param defaultValue
	 *            the value to be returned if the requested member is missing
	 * @return the value of the last member with the specified name, or the given default value if this object does not
	 *         contain a member with that name
	 */
	@Override
	public String getString(final String name, final String defaultValue) {
		IJsonValue value = get(name);
		return value != null ? value.asString() : defaultValue;
	}

	/**
	 * Returns the number of members (name/value pairs) in this object.
	 *
	 * @return the number of members in this object
	 */
	@Override
	public int size() {
		return members.size();
	}

	/**
	 * Returns <code>true</code> if this object contains no members.
	 *
	 * @return <code>true</code> if this object contains no members
	 */
	@Override
	public boolean isEmpty() { return members.isEmpty(); }

	/**
	 * Returns a list of the names in this object in document order. The returned list is backed by this object and will
	 * reflect subsequent changes. It cannot be used to modify this object. Attempts to modify the returned list will
	 * result in an exception.
	 *
	 * @return a list of the names in this object
	 */
	@Override
	public List<String> names() {
		return members.keySet().stream().toList();
	}

	/**
	 * Returns an iterator over the members of this object in document order. The returned iterator cannot be used to
	 * modify this object.
	 *
	 * @return an iterator over the members of this object
	 */
	@Override
	public Iterator<IJsonObject.Member> iterator() {
		final Iterator<Map.Entry<String, JsonValue>> it = members.entrySet().iterator();
		return new Iterator<>() {

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public IJsonObject.Member next() {
				Map.Entry<String, JsonValue> entry = it.next();
				return new JsonObjectMember(entry.getKey(), entry.getValue());
			}

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
		writer.writeObjectOpen();
		writeMembers(writer);
		writer.writeObjectClose();
	}

	/**
	 * Write members.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param writer
	 *            the writer
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 4 nov. 2023
	 */
	protected void writeMembers(final JsonWriter writer) throws IOException {
		Set<Entry<String, JsonValue>> entrySet = members.entrySet();
		boolean first = true;
		for (Entry<String, JsonValue> entry : entrySet) {
			if (!first) { writer.writeObjectSeparator(); }
			first = false;
			String name = entry.getKey();
			JsonValue value = entry.getValue();
			writer.writeMemberName(name);
			writer.writeMemberSeparator();
			value.write(writer);
		}
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
		return 31 + members.hashCode();
	}

	/**
	 * Equals.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 * @date 29 oct. 2023
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		JsonAbstractObject other = (JsonAbstractObject) obj;
		return members.equals(other.members);
	}

	@Override
	public abstract Object toGamlValue(final IScope scope);

	/**
	 * To map.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i map
	 * @date 4 nov. 2023
	 */
	protected IMap<String, ? extends Object> toMap(final IScope scope) {
		IMap<String, Object> result = GamaMapFactory.create();
		members.forEach((name, value) -> result.put(name, value.toGamlValue(scope)));
		return result;
	}

}
