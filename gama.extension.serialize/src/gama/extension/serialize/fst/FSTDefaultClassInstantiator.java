/*******************************************************************************************************
 *
 * FSTDefaultClassInstantiator.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fst;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

import gama.dev.DEBUG;
import gama.extension.serialize.fst.util.FSTUtil;
import sun.reflect.ReflectionFactory;

/**
 * Created by ruedi on 12.12.14.
 *
 * Valid for common x86 JDK's (not android)
 *
 */
public class FSTDefaultClassInstantiator implements FSTClassInstantiator {

	/**
	 * reduce number of generated classes. Can be cleared riskless in case.
	 */
	public static ConcurrentHashMap<Class, Constructor> constructorMap = new ConcurrentHashMap<>();

	@Override
	public Object newInstance(final Class clazz, final Constructor cons, final boolean doesRequireInit) {
		try {
			if (cons == null) return null;
			return cons.newInstance();
		} catch (Throwable ignored) {
			DEBUG.ERR("Failed to construct new instance", ignored);
			return null;
		}
	}

	@Override
	public Constructor findConstructorForExternalize(final Class clazz) {
		try {
			Constructor c = clazz.getDeclaredConstructor((Class[]) null);
			if (c == null) return null;
			c.setAccessible(true);
			if ((c.getModifiers() & Modifier.PUBLIC) != 0) return c;
			return null;
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}

	@Override
	public Constructor findConstructorForSerializable(final Class clazz) {
		if (!Serializable.class.isAssignableFrom(clazz)) {
			// In forceSerializable mode the class is (de)serialised field by field. Prefer a real public no-arg
			// constructor when the class exposes one
			Constructor ext = findConstructorForExternalize(clazz);
			if (ext != null) return ext;
			// otherwise allocate the instance without running any of the class' own constructors
			// This is a fallback to avoid runtime crashes, but a proper no-arg constructor is recommended
			return newSerializationConstructor(clazz, Object.class);
		}
		if (FSTClazzInfo.BufferConstructorMeta) {
			Constructor constructor = constructorMap.get(clazz);
			if (constructor != null) return constructor;
		}
		Class<?> curCl = clazz;
		while (Serializable.class.isAssignableFrom(curCl)) { if ((curCl = curCl.getSuperclass()) == null) return null; }
		try {
			Constructor c = curCl.getDeclaredConstructor((Class[]) null);
			int mods = c.getModifiers();
			if ((mods & Modifier.PRIVATE) != 0
					|| (mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 && !FSTUtil.isPackEq(clazz, curCl))
				return null;
			c = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, c);
			c.setAccessible(true);

			if (FSTClazzInfo.BufferConstructorMeta) { constructorMap.put(clazz, c); }
			return c;
		} catch (NoClassDefFoundError | NoSuchMethodException ex) {
			return null;
		}
	}

	/**
	 * Creates a constructor that allocates an instance of {@code clazz} without running any of its own constructors,
	 * delegating to the no-arg constructor of {@code baseClass} (a superclass of {@code clazz}) through
	 * {@link ReflectionFactory}. This mirrors how the JDK instantiates objects during deserialisation and is used in
	 * forceSerializable mode for classes that do not expose a no-arg constructor.
	 *
	 * @param clazz
	 *            the class to instantiate
	 * @param baseClass
	 *            a superclass of {@code clazz} whose no-arg constructor will be invoked (typically {@link Object})
	 * @return a serialisation constructor, or {@code null} if one cannot be created
	 */
	private static Constructor newSerializationConstructor(final Class clazz, final Class baseClass) {
		try {
			Constructor base = baseClass.getDeclaredConstructor();
			Constructor c = ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, base);
			if (c != null) { c.setAccessible(true); }
			return c;
		} catch (NoClassDefFoundError | NoSuchMethodException | SecurityException | RuntimeException ex) {
			return null;
		}
	}

}
