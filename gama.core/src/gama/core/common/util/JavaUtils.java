/*******************************************************************************************************
 *
 * JavaUtils.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.util;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.UnmodifiableIterator;

import gama.gaml.skills.ISkill;

/**
 * Written by drogoul Modified on 28 d�c. 2010
 *
 * Provides some utilities for dealing with reflection.
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class JavaUtils {


	/** The Constant IMPLEMENTATION_CLASSES. */
	public final static Map<Integer, List<Class>> IMPLEMENTATION_CLASSES = new HashMap();

	/** The interfaces. */
	private static Multimap<Class, Class> INTERFACES = HashMultimap.<Class, Class> create();

	/** The superclasses. */
	private static Multimap<Class, Class> SUPERCLASSES = HashMultimap.<Class, Class> create();

	/**
	 * Key of.
	 *
	 * @param base
	 *            the base
	 * @param others
	 *            the others
	 * @return the int
	 */
	private static int keyOf(final Class base, final Iterable<Class<? extends ISkill>> others) {
		int result = base.hashCode();
		for (final Class other : others) { result += other.hashCode(); }
		return result;
	}

	/**
	 * All interfaces of.
	 *
	 * @param c
	 *            the c
	 * @param in
	 *            the in
	 * @return the sets the
	 */
	private static final Set<Class> allInterfacesOf(final Class c, final Set<Class> in) {
		if (c == null) return Collections.EMPTY_SET;
		if (!INTERFACES.containsKey(c)) {
			final Class[] interfaces = c.getInterfaces();
			for (final Class c1 : interfaces) {
				if (in.contains(c1)) {
					INTERFACES.put(c, c1);
					INTERFACES.putAll(c, allInterfacesOf(c1, in));
				}
			}
			INTERFACES.putAll(c, allInterfacesOf(c.getSuperclass(), in));
		}
		return (Set<Class>) INTERFACES.get(c);
	}

	/**
	 * All superclasses of.
	 *
	 * @param c
	 *            the c
	 * @param in
	 *            the in
	 * @return the sets the
	 */
	private static final Set<Class> allSuperclassesOf(final Class c, final Set<Class> in) {
		if (c == null) return null;
		if (!SUPERCLASSES.containsKey(c)) {
			Class c2 = c.getSuperclass();
			while (c2 != null) {
				if (in.contains(c2)) { SUPERCLASSES.put(c, c2); }
				c2 = c2.getSuperclass();
			}
		}
		return (Set<Class>) SUPERCLASSES.get(c);
	}

	/**
	 * Collect implementation classes.
	 *
	 * @param baseClass
	 *            the base class
	 * @param skillClasses
	 *            the skill classes
	 * @param in
	 *            the in
	 * @return the list
	 */
	public static List<Class> collectImplementationClasses(final Class baseClass,
			final Iterable<Class<? extends ISkill>> skillClasses, final Set<Class> in) {
		final int key = keyOf(baseClass, skillClasses);
		if (!IMPLEMENTATION_CLASSES.containsKey(key)) {
			final Iterable<Class> basis = concat(singleton(baseClass), skillClasses,
					concat(transform(skillClasses, each -> allInterfacesOf(each, in))), allInterfacesOf(baseClass, in));
			final Iterable<Class> extensions = concat(transform(basis, each -> allSuperclassesOf(each, in)));
			final Set<Class> classes = newHashSet(concat(basis, extensions));
			final ArrayList<Class> classes2 = new ArrayList(classes);
			Collections.sort(classes2, (o1, o2) -> {
				if (o1 == o2) return 0;
				if (o1.isAssignableFrom(o2)) return -1;
				if (o2.isAssignableFrom(o1)) return 1;
				if (o1.isInterface() && !o2.isInterface()) return -1;
				if (o2.isInterface() && !o1.isInterface()) {}
				return 1;
			});

			IMPLEMENTATION_CLASSES.put(key, classes2);
		}
		return IMPLEMENTATION_CLASSES.get(key);

	}

}
