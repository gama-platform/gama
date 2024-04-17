/*******************************************************************************************************
 *
 * Signature.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;

import gama.core.runtime.IScope;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;

/**
 * The Class Signature.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })

public record Signature(IType[] list) implements Iterable<IType> {


	/** The empty types. */
	static IType[] EMPTY_TYPES = {};

	/**
	 * Var arg from.
	 *
	 * @param sig
	 *            the sig
	 * @return the signature
	 */
	public static Signature varArgFrom(final Signature sig) {
		return new Signature(Types.LIST.of(GamaType.findCommonType(sig.list)));
	}

	/**
	 * Creates the simplified.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param args
	 *            the args
	 * @return the signature
	 * @date 9 janv. 2024
	 */
	public static Signature createSimplified(final IExpression... args) {
		IType[] copy = new IType[args.length];
		for (int i = 0; i < args.length; i++) { copy[i] = args[i].getGamlType().getGamlType(); }
		return new Signature(copy);
	}

	/**
	 * Instantiates a new signature.
	 *
	 * @param method
	 *            the method
	 */
	public Signature(final Executable method) {
		this(extractTypesFrom(method));
	}

	/**
	 * Instantiates a new signature.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param t
	 *            the t
	 * @date 8 nov. 2023
	 */
	public Signature(final IType t) {
		this(new IType[] { t });
	}

	/**
	 * Extract types from.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param method
	 *            the method
	 * @return the i type[]
	 * @date 8 nov. 2023
	 */
	private static IType[] extractTypesFrom(final Executable method) {
		if (method == null) return EMPTY_TYPES;
		List<IType<?>> types = new ArrayList();
		Class[] classes = method.getParameterTypes();
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		boolean isConstructor = method instanceof Constructor;
		if (!isStatic && !isConstructor) { types.add(Types.get(method.getDeclaringClass())); }
		for (Class c : classes) { if (c != IScope.class) { types.add(Types.get(c)); } }
		return types.toArray(new IType[types.size()]);
	}

	/**
	 * Instantiates a new signature.
	 *
	 * @param types
	 *            the types
	 */
	public Signature(final int[] types) {
		this(new IType[types.length]);
		for (int i = 0; i < list.length; i++) { list[i] = Types.get(types[i]); }
	}

	/**
	 * Instantiates a new signature.
	 *
	 * @param objects
	 *            the objects
	 */
	public Signature(final IExpression... objects) {
		this(new IType[objects.length]);
		for (int i = 0; i < list.length; i++) {
			final IExpression o = objects[i];
			list[i] = o == null ? Types.NO_TYPE : o.getGamlType();
		}
	}

	/**
	 * Instantiates a new signature.
	 *
	 * @param objects
	 *            the objects
	 */
	public Signature(final List<IExpression> objects) {
		this(new IType[objects.size()]);
		for (int i = 0; i < list.length; i++) {
			final IExpression o = objects.get(i);
			list[i] = o == null ? Types.NO_TYPE : o.getGamlType();
		}
	}

	/**
	 * Instantiates a new signature.
	 *
	 * @param classes
	 *            the classes
	 */
	public Signature(final Class... classes) {
		this(new IType[classes.length]);
		for (int i = 0; i < list.length; i++) { list[i] = Types.get(classes[i]); }

	}

	/**
	 * Simplified.
	 *
	 * @return the signature
	 */
	public Signature simplified() {
		// returns a signature that does not contain any parametric types
		final IType[] copy = Arrays.copyOf(list, list.length);
		for (int i = 0; i < copy.length; i++) { copy[i] = copy[i].getGamlType(); }
		return new Signature(copy);
	}

	/**
	 * Matches desired signature.
	 *
	 * @param types
	 *            the types
	 * @return true, if successful
	 */

	/**
	 * Matches desired signature.
	 *
	 * @param types
	 *            the types
	 * @return true, if successful
	 */
	public boolean matchesDesiredSignature(final Signature types) {
		return matchesDesiredSignature(types.list);
	}

	/**
	 * Distance to.
	 *
	 * @param types
	 *            the types
	 * @return the int
	 */
	public int distanceTo(final IType... types) {
		if (types.length != list.length) return Integer.MAX_VALUE;
		int max = 0;
		int min = Integer.MAX_VALUE;
		// We now take into account the min and the max (see #2266 and the case where [unknown, geometry, geometry] was
		// preffered to [topology, geometry, geometry] for an input of [topology, a_species, a_species])
		for (int i = 0; i < list.length; i++) {
			final int d = types[i].distanceTo(list[i]);
			if (max < d) { max = d; }
			if (min > d) { min = d; }
		}
		return min + max;
	}

	/**
	 * Matches desired signature.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param types
	 *            the types
	 * @return true, if successful
	 * @date 12 janv. 2024
	 */
	public boolean matchesDesiredSignature(final IType... types) {
		if (types.length != list.length) return false;
		for (int i = 0; i < list.length; i++) {
			final IType ownType = list[i];
			final IType desiredType = types[i];
			if (Types.intFloatCase(ownType, desiredType)
					|| desiredType.isAssignableFrom(ownType)  || !desiredType.isNumber() && ownType == Types.NO_TYPE) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Distance to.
	 *
	 * @param types
	 *            the types
	 * @return the int
	 */
	public int distanceTo(final Signature types) {
		return distanceTo(types.list);
	}

	/**
	 * Equals.
	 *
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	public boolean equals(final Signature p) {
		if (p.list.length != list.length) return false;
		for (int i = 0; i < list.length; i++) { if (p.list[i] != list[i]) return false; }
		return true;
	}

	@Override
	public boolean equals(final Object p) {
		if (!(p instanceof Signature)) return false;
		return equals((Signature) p);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(list);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder().append(list.length < 2 ? "type " : "types [");
		for (int i = 0; i < list.length; i++) {
			s.append(list[i]);
			if (i != list.length - 1) { s.append(", "); }
		}
		if (list.length >= 2) { s.append("]"); }
		return s.toString();
	}

	/**
	 * Gets the.
	 *
	 * @param i
	 *            the i
	 * @return the i type
	 */
	public IType get(final int i) {
		return list[i];
	}

	/**
	 * Coerce.
	 *
	 * @param originalSignature
	 *            the original signature
	 * @param context
	 *            the context
	 * @return the i type[]
	 */
	public IType[] coerce(final Signature originalSignature, final IDescription context) {
		final IType[] result = new IType[list.length];
		for (int i = 0; i < list.length; i++) { result[i] = list[i].coerce(originalSignature.get(i), context); }
		return result;
	}

	/**
	 * @return
	 */
	public boolean isUnary() { return list.length == 1; }

	/**
	 * @return
	 */
	public int size() {
		return list.length;
	}

	/**
	 * @return
	 */
	public String asPattern(final boolean withVariables) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.length; i++) {
			sb.append(withVariables ? list[i].asPattern() : list[i].serializeToGaml(true));
			if (i < list.length - 1) { sb.append(','); }
		}
		return sb.toString();
	}

	/**
	 * Checks for int or float. Assuming both signatures have the same size
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if successful
	 * @date 27 déc. 2023
	 */
	public boolean mightNeedCoercionWith(final Signature other) {
		for (int i = 0; i < list.length; i++)
			if (Types.intFloatCase(list[i], other.list[i])) return true;
		return false;
	}

	@Override
	public Iterator<IType> iterator() {
		return Iterators.forArray(list);
	}

}