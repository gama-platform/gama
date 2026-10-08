/*******************************************************************************************************
 *
 * JsonObjectMember.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import java.util.Objects;

import gama.api.utils.json.IJsonObject;
import gama.api.utils.json.IJsonValue;

/**
 * Represents a member of a JSON object, a pair of a name and a value.
 */
public record JsonObjectMember(String name, IJsonValue value) implements IJsonObject.Member {

	/**
	 * Indicates whether a given object is "equal to" this JsonObject. An object is considered equal if it is also a
	 * <code>JsonObject</code> and both objects contain the same members <em>in the same order</em>.
	 * <p>
	 * If two JsonObjects are equal, they will also produce the same JSON output.
	 * </p>
	 *
	 * @param object
	 *            the object to be compared with this JsonObject
	 * @return <tt>true</tt> if the specified object is equal to this JsonObject, <code>false</code> otherwise
	 */
	@Override
	public boolean equals(final Object object) {
		if (this == object) return true;
		return object instanceof JsonObjectMember jom && name.equals(jom.name) && Objects.equals(value, jom.value);
	}

}