/*******************************************************************************************************
 *
 * FSTUtil.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fst.util;

import java.io.InputStream;
import java.io.ObjectStreamField;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: ruedi Date: 29.11.12 Time: 20:38 To change this template use File | Settings | File
 * Templates.
 */
public class FSTUtil {

	/** The Empty obj array. */
	static Object[] EmptyObjArray = new Object[10000];

	/** The no fields. */
	static ObjectStreamField[] NO_FIELDS = {};

	/**
	 * Clear.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param arr
	 *            the arr
	 * @date 1 nov. 2023
	 */
	public static void clear(final int[] arr) {
		Arrays.fill(arr, 0);
	}

	/**
	 * Clear.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param arr
	 *            the arr
	 * @date 1 nov. 2023
	 */
	public static void clear(final Object[] arr) {
		final int arrlen = arr.length;
		clear(arr, arrlen);
	}

	/**
	 * Clear.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param arr
	 *            the arr
	 * @param arrlen
	 *            the arrlen
	 * @date 1 nov. 2023
	 */
	public static void clear(final Object[] arr, final int arrlen) {
		int count = 0;
		final int length = EmptyObjArray.length;
		while (arrlen - count > length) {
			System.arraycopy(EmptyObjArray, 0, arr, count, length);
			count += length;
		}
		System.arraycopy(EmptyObjArray, 0, arr, count, arrlen - count);
	}

	/**
	 * To string.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param th
	 *            the th
	 * @return the string
	 * @date 1 nov. 2023
	 */
	public static String toString(final Throwable th) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		th.printStackTrace(pw);
		return th.getClass().getSimpleName() + ":" + th.getMessage() + "\n" + sw.toString();
	}

	/**
	 * Rethrow.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param <T>
	 *            the generic type
	 * @param exception
	 *            the exception
	 * @throws T
	 *             the t
	 * @date 1 nov. 2023
	 */
	@SuppressWarnings ("unchecked")
	public static <T extends Throwable> void rethrow(final Throwable exception) throws T {
		throw (T) exception;
	}

	/**
	 * Gets the package.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param clazz
	 *            the clazz
	 * @return the package
	 * @date 1 nov. 2023
	 */
	// obsolete
	public static String getPackage(final Class<?> clazz) {
		String s = clazz.getName();
		int i = s.lastIndexOf('[');
		if (i >= 0) { s = s.substring(i + 2); }
		i = s.lastIndexOf('.');
		if (i >= 0) return s.substring(0, i);
		return "";
	}

	/**
	 * Checks if is pack eq.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param clazz1
	 *            the clazz 1
	 * @param clazz2
	 *            the clazz 2
	 * @return true, if is pack eq
	 * @date 1 nov. 2023
	 */
	public static boolean isPackEq(final Class<?> clazz1, final Class<?> clazz2) {
		return getPackage(clazz1).equals(getPackage(clazz2));
	}

	/**
	 * Find private method.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param clazz
	 *            the clazz
	 * @param methName
	 *            the meth name
	 * @param clazzArgs
	 *            the clazz args
	 * @param retClazz
	 *            the ret clazz
	 * @return the method
	 * @date 1 nov. 2023
	 */
	public static Method findPrivateMethod(final Class<?> clazz, final String methName, final Class[] clazzArgs,
			final Class<?> retClazz) {
		try {
			Method m = clazz.getDeclaredMethod(methName, clazzArgs);
			int modif = m.getModifiers();
			if (m.getReturnType() == retClazz && (modif & Modifier.PRIVATE) != 0 && (modif & Modifier.STATIC) == 0) {
				m.setAccessible(true);
				return m;
			}
			return null;
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}

	/**
	 * Find derived method.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param clazz
	 *            the clazz
	 * @param metnam
	 *            the metnam
	 * @param argClzz
	 *            the arg clzz
	 * @param retClz
	 *            the ret clz
	 * @return the method
	 * @date 1 nov. 2023
	 */
	public static Method findDerivedMethod(final Class<?> clazz, final String metnam, final Class[] argClzz,
			final Class<?> retClz) {
		Method m = null;
		Class<?> defCl = clazz;
		while (defCl != null) {
			try {
				m = defCl.getDeclaredMethod(metnam, argClzz);
				break;
			} catch (NoSuchMethodException ex) {
				defCl = defCl.getSuperclass();
			}
		}
		if (m == null || m.getReturnType() != retClz) return null;
		int mods = m.getModifiers();
		if ((mods & (Modifier.STATIC | Modifier.ABSTRACT)) != 0) return null;
		if ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
			m.setAccessible(true);
			return m;
		}
		if ((mods & Modifier.PRIVATE) != 0) {
			m.setAccessible(true);
			if (clazz == defCl) return m;
		} else {
			m.setAccessible(true);
			if (isPackEq(clazz, defCl)) return m;
		}
		return null;
	}

	/**
	 * Prints the ex.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param e
	 *            the e
	 * @date 1 nov. 2023
	 */
	public static void printEx(Throwable e) {
		while (e.getCause() != null && e.getCause() != e) { e = e.getCause(); }
		e.printStackTrace();
	}

	/**
	 * Checks if is primitive array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @return true, if is primitive array
	 * @date 1 nov. 2023
	 */
	public static boolean isPrimitiveArray(final Class<?> c) {
		Class<?> componentType = c.getComponentType();
		if (componentType == null) return c.isPrimitive();
		return isPrimitiveArray(c.getComponentType());
	}

	/**
	 * Write signed var int.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param value
	 *            the value
	 * @param out
	 *            the out
	 * @param index
	 *            the index
	 * @return the int
	 * @date 1 nov. 2023
	 */
	public static int writeSignedVarInt(final int value, final byte out[], final int index) {
		return writeUnsignedVarInt(value << 1 ^ value >> 31, out, index);
	}

	/**
	 * Write unsigned var int.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param value
	 *            the value
	 * @param out
	 *            the out
	 * @param index
	 *            the index
	 * @return the int
	 * @date 1 nov. 2023
	 */
	public static int writeUnsignedVarInt(int value, final byte[] out, int index) {
		while ((value & 0xFFFFFF80) != 0L) {
			out[index++] = (byte) (value & 0x7F | 0x80);
			value >>>= 7;
		}
		out[index++] = (byte) (value & 0x7F);
		return index;
	}

	/**
	 * Gets the all fields.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param fields
	 *            the fields
	 * @param type
	 *            the type
	 * @return the all fields
	 * @date 1 nov. 2023
	 */
	public static List<Field> getAllFields(List<Field> fields, final Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));
		if (type.getSuperclass() != null) { fields = getAllFields(fields, type.getSuperclass()); }
		return fields;
	}

	/**
	 * Read all.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param is
	 *            the is
	 * @return the byte[]
	 * @throws Exception
	 *             the exception
	 * @date 1 nov. 2023
	 */
	public static byte[] readAll(final InputStream is) throws Exception {
		int pos = 0;
		byte[] buffer = new byte[1024];
		while (true) {
			int toRead;
			if (pos >= buffer.length) {
				toRead = buffer.length * 2;
				if (buffer.length < pos + toRead) { buffer = Arrays.copyOf(buffer, pos + toRead); }
			} else {
				toRead = buffer.length - pos;
			}
			int byt = is.read(buffer, pos, toRead);
			if (byt < 0) {
				if (pos != buffer.length) { buffer = Arrays.copyOf(buffer, pos); }
				break;
			}
			pos += byt;
		}
		return buffer;
	}

	/**
	 * Next pow 2.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param num
	 *            the num
	 * @return the int
	 * @date 1 nov. 2023
	 */
	public static int nextPow2(final int num) {
		return 1 << (num == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(num - 1));
	}

	/**
	 * Gets the real enum class.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param enumClass
	 *            the enum class
	 * @return the real enum class
	 * @date 1 nov. 2023
	 */
	public static Class<?> getRealEnumClass(final Class<?> enumClass) {
		if (enumClass.isAnonymousClass()) return enumClass.getSuperclass();
		return enumClass;
	}
}
