/*******************************************************************************************************
 *
 * IJsonObject.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.json;

import java.util.Iterator;
import java.util.List;

import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.runtime.scope.IScope;

/**
 *
 */

public interface IJsonObject extends IJsonValue, Iterable<IJsonObject.Member> {

	/**
	 * The Interface Agent.
	 */
	interface Agent extends IJsonObject {

		/**
		 * To gaml value.
		 *
		 * @param scope
		 *            the scope
		 * @return the i serialised agent
		 */
		@Override
		ISerialisedAgent toGamlValue(IScope scope);
	}

	/**
	 * The Interface Reference.
	 */
	interface Reference extends IJsonObject {

	}

	/**
	 * The Interface Member.
	 */
	interface Member {
		/**
		 * Name.
		 *
		 * @return the string
		 */
		String name();

		/**
		 * Value.
		 *
		 * @return the object
		 */
		IJsonValue value();
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
	IJsonObject add(String name, Object object);

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
	IJsonObject add(String name, int value);

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
	IJsonObject add(String name, long value);

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
	IJsonObject add(String name, float value);

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
	IJsonObject add(String name, double value);

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
	IJsonObject add(String name, boolean value);

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
	IJsonObject add(String name, String value);

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
	IJsonObject add(String name, IJsonValue value);

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
	IJsonObject set(String name, IJsonValue value);

	/**
	 * Removes a member with the specified name from this object. If this object contains multiple members with the
	 * given name, only the last one is removed. If this object does not contain a member with the specified name, the
	 * object is not modified.
	 *
	 * @param name
	 *            the name of the member to remove
	 * @return the value that has been removed or null if the name was not present
	 */
	IJsonValue remove(String name);

	/**
	 * Checks if a specified member is present as a child of this object. This will not test if this object contains the
	 * literal <code>null</code>, should be used for this purpose.
	 *
	 * @param name
	 *            the name of the member to check for
	 * @return whether or not the member is present
	 */
	boolean contains(String name);

	/**
	 * Returns the value of the member with the specified name in this object. If this object contains multiple members
	 * with the given name, this method will return the last one.
	 *
	 * @param name
	 *            the name of the member whose value is to be returned
	 * @return the value of the last member with the specified name, or <code>null</code> if this object does not
	 *         contain a member with that name
	 */
	IJsonValue get(String name);

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
	int getInt(String name, int defaultValue);

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
	long getLong(String name, long defaultValue);

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
	float getFloat(String name, float defaultValue);

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
	double getDouble(String name, double defaultValue);

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
	boolean getBoolean(String name, boolean defaultValue);

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
	String getString(String name, String defaultValue);

	/**
	 * Returns the number of members (name/value pairs) in this object.
	 *
	 * @return the number of members in this object
	 */
	int size();

	/**
	 * Returns <code>true</code> if this object contains no members.
	 *
	 * @return <code>true</code> if this object contains no members
	 */
	boolean isEmpty();

	/**
	 * Returns a list of the names in this object in document order. The returned list is backed by this object and will
	 * reflect subsequent changes. It cannot be used to modify this object. Attempts to modify the returned list will
	 * result in an exception.
	 *
	 * @return a list of the names in this object
	 */
	List<String> names();

	/**
	 * Returns an iterator over the members of this object in document order. The returned iterator cannot be used to
	 * modify this object.
	 *
	 * @return an iterator over the members of this object
	 */
	@Override
	Iterator<IJsonObject.Member> iterator();

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
	 * Equals.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 * @date 29 oct. 2023
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * To gaml value.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@Override
	Object toGamlValue(IScope scope);

}