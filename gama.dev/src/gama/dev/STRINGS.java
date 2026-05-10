/*******************************************************************************************************
 *
 * STRINGS.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dev;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Utility methods used to obtain readable string representations and padded strings.
 */
public class STRINGS {

	/**
	 * Stringifiers dedicated to primitive arrays, keyed by their component type.
	 */
	static final ConcurrentHashMap<Class<?>, Function<Object, String>> TO_STRING = new ConcurrentHashMap<>();

	static {
		TO_STRING.put(int.class, o -> Arrays.toString((int[]) o));
		TO_STRING.put(double.class, o -> Arrays.toString((double[]) o));
		TO_STRING.put(float.class, o -> Arrays.toString((float[]) o));
		TO_STRING.put(byte.class, o -> Arrays.toString((byte[]) o));
		TO_STRING.put(boolean.class, o -> Arrays.toString((boolean[]) o));
		TO_STRING.put(long.class, o -> Arrays.toString((long[]) o));
		TO_STRING.put(short.class, o -> Arrays.toString((short[]) o));
		TO_STRING.put(char.class, o -> Arrays.toString((char[]) o));
	}

	/**
	 * Tries to obtain a correct string representation of the object, including when it is an array (or an array of
	 * arrays), an {@link Iterable}, or an {@link Enumeration}. Made public to be used outside the debug sessions.
	 *
	 * @param object
	 *            any object
	 * @return its string representation
	 */
	public static String TO_STRING(final Object object) {
		return TO_STRING(object, new IdentityHashMap<>());
	}

	/**
	 * Builds a readable string representation of the specified object while keeping track of already visited container
	 * instances to avoid infinite recursion.
	 *
	 * @param object
	 *            the object to stringify
	 * @param visited
	 *            the container instances already being stringified
	 * @return a readable string representation of the object
	 */
	private static String TO_STRING(final Object object, final IdentityHashMap<Object, Boolean> visited) {
		if (object == null) return "null";
		if (object.getClass().isArray()) {
			if (visited.put(object, Boolean.TRUE) != null) return "[...]";
			final Class<?> clazz = object.getClass().getComponentType();
			try {
				if (clazz.isPrimitive()) return TO_STRING.get(clazz).apply(object);
				return ARRAY_TO_STRING((Object[]) object, visited);
			} finally {
				visited.remove(object);
			}
		}
		if (object instanceof Iterable<?> iterable) {
			if (visited.put(object, Boolean.TRUE) != null) return "[...]";
			try {
				return ITERATOR_TO_STRING(iterable.iterator(), visited);
			} finally {
				visited.remove(object);
			}
		}
		if (object instanceof Enumeration<?> enumeration) {
			if (visited.put(object, Boolean.TRUE) != null) return "[...]";
			try {
				return ENUMERATION_TO_STRING(enumeration, visited);
			} finally {
				visited.remove(object);
			}
		}

		return object.toString();
	}

	/**
	 * Builds a string representation of an object array and recursively stringifies its elements.
	 *
	 * @param array
	 *            the array to stringify
	 * @param visited
	 *            the container instances already being stringified
	 * @return the string representation of the array
	 */
	private static String ARRAY_TO_STRING(final Object[] array, final IdentityHashMap<Object, Boolean> visited) {
		final StringBuilder sb = new StringBuilder().append('[');
		for (int i = 0; i < array.length; i++) {
			if (i > 0) { sb.append(", "); }
			sb.append(TO_STRING(array[i], visited));
		}
		return sb.append(']').toString();
	}

	/**
	 * Builds a string representation of an {@link Enumeration} and recursively stringifies its elements.
	 *
	 * @param enumeration
	 *            the enumeration to stringify
	 * @param visited
	 *            the container instances already being stringified
	 * @return the string representation of the enumeration
	 */
	private static String ENUMERATION_TO_STRING(final Enumeration<?> enumeration,
			final IdentityHashMap<Object, Boolean> visited) {
		final StringBuilder sb = new StringBuilder().append('[');
		boolean first = true;
		while (enumeration.hasMoreElements()) {
			if (!first) { sb.append(", "); }
			sb.append(TO_STRING(enumeration.nextElement(), visited));
			first = false;
		}
		return sb.append(']').toString();
	}

	/**
	 * Builds a string representation of an {@link Iterator} and recursively stringifies its elements.
	 *
	 * @param iterator
	 *            the iterator to stringify
	 * @param visited
	 *            the container instances already being stringified
	 * @return the string representation of the iterator
	 */
	private static String ITERATOR_TO_STRING(final Iterator<?> iterator, final IdentityHashMap<Object, Boolean> visited) {
		final StringBuilder sb = new StringBuilder().append('[');
		boolean first = true;
		while (iterator.hasNext()) {
			if (!first) { sb.append(", "); }
			sb.append(TO_STRING(iterator.next(), visited));
			first = false;
		}
		return sb.append(']').toString();
	}

	/**
	 * A utility method for padding a string with spaces in order to obtain a length of "minLength"
	 *
	 * @param string
	 *            the string to pad
	 * @param minLength
	 *            the minimum length to reach (if the string is longer, it will be return as is)
	 * @return a string of minimum length minLength
	 */
	public static String PAD(final String string, final int minLength) {
		return PAD(string, minLength, ' ');
	}

	/**
	 * A utility method for padding a string with any character in order to obtain a length of "minLength"
	 *
	 * @param string
	 *            the string to pad
	 * @param minLength
	 *            the minimum length to reach (if the string is longer, it will be return as is)
	 * @return a string of minimum length minLength
	 */

	public static String PAD(final String string, final int minLength, final char c) {
		if (string.length() >= minLength) return string;
		final StringBuilder sb = new StringBuilder(minLength);
		sb.append(string);
		for (int i = string.length(); i < minLength; i++) { sb.append(c); }
		return sb.toString();
	}

}
