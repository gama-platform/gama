/*******************************************************************************************************
 *
 * Signature.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;

/**
 * The Class Signature.
 * Immutable sequence of parameter types used to match operator/method signatures in GAML.
 * Provides helpers to build signatures from expressions, Java reflection or plain classes,
 * to compare desired/actual parameters, and to compute coercion/distance when selecting operators.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })

public record Signature(IType[] list) implements Iterable<IType> {

	/** The empty types. */
	static IType[] EMPTY_TYPES = {};

	/**
	 * Builds a var-arg signature by wrapping the common type of {@code sig} elements into a list type.
	 * Useful when an operator accepts an arbitrary number of homogeneous arguments.
	 *
	 * @param sig signature to convert to var-arg form
	 * @return a new signature containing a single list type
	 */
	public static Signature varArgFrom(final Signature sig) {
		return new Signature(Types.LIST.of(GamaType.findCommonType(sig.list)));
	}

	/**
	 * Creates a simplified signature from expressions by stripping any parametric information (keeps base GAML types).
	 *
	 * @param args expressions to infer types from
	 * @return a non-parametric signature mirroring {@code args} order
	 */
	public static Signature createSimplified(final IExpression... args) {
		IType[] copy = new IType[args.length];
		for (int i = 0; i < args.length; i++) { copy[i] = args[i].getGamlType().getGamlType(); }
		return new Signature(copy);
	}

	/**
	 * Builds a signature from a Java executable (method or constructor), ignoring {@link IScope} parameters
	 * and inserting the receiver type for instance methods.
	 *
	 * @param method Java executable to analyse
	 */
	public Signature(final Executable method) {
		this(extractTypesFrom(method));
	}

	/**
	 * Convenience ctor for unary signatures.
	 *
	 * @param t single type in the signature
	 */
	public Signature(final IType t) {
		this(new IType[] { t });
	}

	/**
	 * Extracts the sequence of GAML types corresponding to a Java executable parameters.
	 * Skips {@link IScope}, and prepends the declaring class when the method is non-static.
	 *
	 * @param method Java executable to inspect
	 * @return ordered array of argument types (may be empty)
	 */
	private static IType[] extractTypesFrom(final Executable method) {
		if (method == null) return EMPTY_TYPES;
		Class[] classes = method.getParameterTypes();
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		boolean isConstructor = method instanceof Constructor;
		int size = classes.length;
		if (!isStatic && !isConstructor) { size++; }
		for (Class c : classes) { if (c == IScope.class) { size--; } }
		IType[] result = new IType[size];
		int i = 0;
		if (!isStatic && !isConstructor) { result[i++] = Types.get(method.getDeclaringClass()); }
		for (Class c : classes) { if (c != IScope.class) { result[i++] = Types.get(c); } }
		return result;
	}

	/**
	 * Builds a signature from type ids.
	 *
	 * @param types array of IType ids
	 */
	public Signature(final int[] types) {
		this(new IType[types.length]);
		for (int i = 0; i < list.length; i++) { list[i] = Types.get(types[i]); }
	}

	/**
	 * Builds a signature from expressions, keeping their declared runtime type (or NO_TYPE when null).
	 *
	 * @param objects expressions that provide types
	 */
	public Signature(final IExpression... objects) {
		this(new IType[objects.length]);
		for (int i = 0; i < list.length; i++) {
			final IExpression o = objects[i];
			list[i] = o == null ? Types.NO_TYPE : o.getGamlType();
		}
	}

	/**
	 * Builds a signature from a list of expressions.
	 *
	 * @param objects expressions that provide types
	 */
	public Signature(final List<IExpression> objects) {
		this(new IType[objects.size()]);
		for (int i = 0; i < list.length; i++) {
			final IExpression o = objects.get(i);
			list[i] = o == null ? Types.NO_TYPE : o.getGamlType();
		}
	}

	/**
	 * Builds a signature from Java classes by resolving each to its GAML type.
	 *
	 * @param classes Java classes to convert
	 */
	public Signature(final Class... classes) {
		this(new IType[classes.length]);
		for (int i = 0; i < list.length; i++) { list[i] = Types.get(classes[i]); }

	}

	/**
	 * Returns a simplified version with every entry replaced by its base GAML type (no parametric details).
	 *
	 * @return this when already simplified, otherwise a new simplified signature
	 */
	public Signature simplified() {
		// returns a signature that does not contain any parametric types
		boolean changed = false;
		for (IType t : list) {
			if (t.getGamlType() != t) {
				changed = true;
				break;
			}
		}
		if (!changed) return this;

		final IType[] copy = new IType[list.length];
		for (int i = 0; i < copy.length; i++) { copy[i] = list[i].getGamlType(); }
		return new Signature(copy);
	}

	/**
	 * Checks assignability of this signature to a desired one (same arity), allowing int/float interchange and NO_TYPE
	 * as wildcard for non-number types.
	 *
	 * @param types desired signature
	 * @return true when each position is compatible
	 */
	public boolean matchesDesiredSignature(final Signature types) {
		if (types.list.length != list.length) return false;
		for (int i = 0; i < list.length; i++) {
			final IType localType = list[i];
			final IType requestedType = types.list[i];
			if (Types.intFloatCase(localType, requestedType) || requestedType.isAssignableFrom(localType)
					|| !localType.isNumber() && requestedType == Types.NO_TYPE) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Variant of {@link #matchesDesiredSignature(Signature)} accepting raw type array.
	 *
	 * @param types desired types
	 * @return true when each position is compatible
	 */
	public boolean matchesDesiredSignature(final IType... types) {
		if (types.length != list.length) return false;
		for (int i = 0; i < list.length; i++) {
			final IType ownType = list[i];
			final IType desiredType = types[i];
			if (Types.intFloatCase(ownType, desiredType) || desiredType.isAssignableFrom(ownType)
					|| !desiredType.isNumber() && ownType == Types.NO_TYPE) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Computes the summed distance between two signatures (same arity) using {@link IType#distanceTo(IType)}.
	 * Returns {@link Integer#MAX_VALUE} on arity mismatch.
	 *
	 * @param formalSignature reference signature
	 * @param passedSignature tested signature
	 * @return summed distance (0 means identical), or MAX_VALUE if lengths differ
	 */
	public static int distanceBetween(final Signature formalSignature, final Signature passedSignature) {
		IType[] formalTypes = formalSignature.list;
		IType[] passedTypes = passedSignature.list;
		if (passedTypes.length != formalTypes.length) return Integer.MAX_VALUE;
		// We now take into account the min and the max (see #2266 and the case where [unknown, geometry, geometry] was
		// preffered to [topology, geometry, geometry] for an input of [topology, a_species, a_species])
		// Modified again for the case where [string, matrix, unknown] and [string, container, unknown] return both 1
		// for an
		// input of [string,matrix, int] ...Now we sum the distances between types and return this.
		int totalDistance = 0;
		for (int i = 0; i < formalTypes.length; i++) { totalDistance += formalTypes[i].distanceTo(passedTypes[i]); }
		return totalDistance;
	}

	/**
	 * Equals.
	 *
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	public boolean equals(final Signature p) {
		if (p == this) return true;
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
	 * Returns the type at the given position.
	 *
	 * @param i index of the argument
	 * @return type at index
	 */
	public IType get(final int i) {
		return list[i];
	}

	/**
	 * Coerces each type of this signature so it can be used where {@code originalSignature} was expected.
	 *
	 * @param originalSignature target signature to adapt to
	 * @param context description context providing resolution hints
	 * @return array of coerced types (same length as this signature)
	 */
	public IType[] coerce(final Signature originalSignature, final IDescription context) {
		final IType[] result = new IType[list.length];
		for (int i = 0; i < list.length; i++) { result[i] = list[i].coerce(originalSignature.get(i), context); }
		return result;
	}

	/**
	 * Indicates whether numeric coercion (int/float) might be required between two signatures of same arity.
	 *
	 * @param other signature to compare
	 * @return true if any position mixes int and float
	 */
	public boolean mightNeedCoercionWith(final Signature other) {
		for (int i = 0; i < list.length; i++)
			if (Types.intFloatCase(list[i], other.list[i])) return true;
		return false;
	}

	/**
	 * Returns a CSV of types either as patterns (withVariables=true) or serialized names.
	 *
	 * @param withVariables whether to include variable placeholders via {@link IType#asPattern()}
	 * @return comma-separated textual representation
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
	 * Checks if this signature contains exactly one type.
	 *
	 * @return true when the signature contains exactly one type
	 */
	public boolean isUnary() { return list.length == 1; }

	/**
	 * Returns the number of types in this signature.
	 *
	 * @return number of parameters in the signature
	 */
	public int size() {
		return list.length;
	}
 
	@Override
	public Iterator<IType> iterator() { return Iterators.forArray(list); }

}