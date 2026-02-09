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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;

/**
 * A record representing a type signature for methods, operators, or other callable constructs in the GAMA platform.
 * 
 * <p>This class encapsulates an ordered list of {@link IType} objects that define the parameter types
 * for a particular signature. It provides functionality for signature matching, type coercion,
 * distance calculation between signatures, and various utility operations.</p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Signature matching for method resolution</li>
 * <li>Type distance calculation for best match selection</li>
 * <li>Type coercion support</li>
 * <li>Simplified signature generation</li>
 * <li>Pattern generation for documentation</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Create signature from expressions
 * Signature sig = new Signature(expr1, expr2, expr3);
 * 
 * // Check if it matches desired types
 * boolean matches = sig.matchesDesiredSignature(Types.INT, Types.STRING, Types.FLOAT);
 * 
 * // Calculate distance to another signature
 * int distance = Signature.distanceBetween(sig1, sig2);
 * }</pre>
 * 
 * @author GAMA Development Team
 * @since GAMA 2025-03
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public record Signature(IType<?>[] list) implements Iterable<IType<?>> {

	/**
	 * Empty array constant to avoid repeated object creation.
	 * Used as a default when no types are specified.
	 */
	static IType<?>[] EMPTY_TYPES = {};

	/**
	 * Creates a variadic signature from an existing signature by wrapping all types
	 * in a common container type (list).
	 * 
	 * <p>This method finds the most general common type among all types in the signature
	 * and creates a new signature containing a single list type of that common type.</p>
	 *
	 * @param sig the original signature to convert to variadic form
	 * @return a new Signature containing a single list type with the common type as element type
	 * @throws NullPointerException if sig is null
	 */
	public static Signature varArgFrom(final Signature sig) {
		if (sig == null) throw new NullPointerException("Signature cannot be null");
		return new Signature(Types.LIST.of(GamaType.findCommonType(sig.list)));
	}

	/**
	 * Creates a simplified signature from expressions by extracting their GAML types.
	 * 
	 * <p>This method creates a signature where each type is simplified to its base GAML type,
	 * removing any parametric type information. This is useful for signature comparison
	 * where type parameters should be ignored.</p>
	 *
	 * @param args the expressions to extract types from
	 * @return a new Signature with simplified types from the expressions
	 */
	public static Signature createSimplified(final IExpression... args) {
		IType<?>[] copy = new IType[args.length];
		for (int i = 0; i < args.length; i++) { 
			copy[i] = args[i].getGamlType().getGamlType(); 
		}
		return new Signature(copy);
	}

	/**
	 * Creates a signature from an executable (method or constructor).
	 * 
	 * <p>This constructor extracts type information from the method's parameters,
	 * automatically handling static methods, constructors, and instance methods.
	 * {@link IScope} parameters are filtered out as they are framework-specific.</p>
	 *
	 * @param method the executable (method or constructor) to extract signature from
	 */
	public Signature(final Executable method) {
		this(extractTypesFrom(method));
	}

	/**
	 * Creates a signature with a single type.
	 *
	 * @param t the single type for this signature
	 */
	public Signature(final IType<?> t) {
		this(new IType[] { t });
	}

	/**
	 * Extracts type information from an executable (method or constructor).
	 * 
	 * <p>This method handles both static and instance methods, automatically including
	 * the declaring class as the first parameter for instance methods. {@link IScope}
	 * parameters are filtered out as they are framework-specific.</p>
	 *
	 * @param method the executable to extract types from
	 * @return an array of types representing the method signature
	 */
	private static IType<?>[] extractTypesFrom(final Executable method) {
		if (method == null) return EMPTY_TYPES;
		
		List<IType<?>> types = new ArrayList<>();
		Class<?>[] parameterTypes = method.getParameterTypes();
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		boolean isConstructor = method instanceof Constructor;
		
		// Add declaring class type for non-static, non-constructor methods
		if (!isStatic && !isConstructor) { 
			types.add(Types.get(method.getDeclaringClass())); 
		}
		
		// Add parameter types (excluding IScope)
		for (Class<?> paramType : parameterTypes) { 
			if (paramType != IScope.class) { 
				types.add(Types.get(paramType)); 
			} 
		}
		
		return types.toArray(new IType[0]);
	}

	/**
	 * Creates a signature from an array of type identifiers.
	 *
	 * @param types the array of type identifiers
	 */
	public Signature(final int[] types) {
		this(new IType[types.length]);
		for (int i = 0; i < list.length; i++) { list[i] = Types.get(types[i]); }
	}

	/**
	 * Creates a signature from expressions.
	 *
	 * @param objects the expressions to extract types from
	 */
	public Signature(final IExpression... objects) {
		this(new IType[objects.length]);
		for (int i = 0; i < list.length; i++) {
			final IExpression o = objects[i];
			list[i] = o == null ? Types.NO_TYPE : o.getGamlType();
		}
	}

	/**
	 * Creates a signature from a list of expressions.
	 *
	 * @param objects the list of expressions to extract types from
	 */
	public Signature(final List<IExpression> objects) {
		this(new IType[objects.size()]);
		for (int i = 0; i < list.length; i++) {
			final IExpression o = objects.get(i);
			list[i] = o == null ? Types.NO_TYPE : o.getGamlType();
		}
	}

	/**
	 * Creates a signature from Java classes.
	 *
	 * @param classes the Java classes to convert to GAMA types
	 */
	public Signature(final Class<?>... classes) {
		this(new IType[classes.length]);
		for (int i = 0; i < list.length; i++) { list[i] = Types.get(classes[i]); }
	}

	/**
	 * Returns a simplified signature without parametric type information.
	 * 
	 * <p>Creates a new signature where each type is reduced to its base GAMA type,
	 * effectively removing any generic type parameters. This is useful for signature
	 * comparison where type parameters should be ignored.</p>
	 *
	 * @return a new Signature with simplified types
	 */
	public Signature simplified() {
		// returns a signature that does not contain any parametric types
		final IType[] copy = Arrays.copyOf(list, list.length);
		for (int i = 0; i < copy.length; i++) { copy[i] = copy[i].getGamlType(); }
		return new Signature(copy);
	}

	/**
	 * Checks if this signature matches the desired signature for type compatibility.
	 * 
	 * <p>A signature matches if all corresponding types are compatible through assignment,
	 * number coercion (int/float), or if the desired type is NO_TYPE for non-numeric types.</p>
	 *
	 * @param types the signature to match against
	 * @return true if signatures are compatible
	 */
	public boolean matchesDesiredSignature(final Signature types) {
		if (types.list.length != list.length) return false;
		for (int i = 0; i < list.length; i++) {
			final IType localType = list[i];
			final IType requestedType = types.get(i);
			if (Types.intFloatCase(localType, requestedType) || requestedType.isAssignableFrom(localType)
					|| !localType.isNumber() && requestedType == Types.NO_TYPE) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Checks if this signature matches the desired signature for type compatibility using varargs.
	 * 
	 * <p>A signature matches if all corresponding types are compatible through assignment,
	 * number coercion (int/float), or if the desired type is NO_TYPE for non-numeric types.</p>
	 *
	 * @param types the desired types to match against
	 * @return true if signatures are compatible
	 */
	public boolean matchesDesiredSignature(final IType... types) {
		if (types.length != list.length) return false;
		for (int i = 0; i < list.length; i++) {
			final IType ownType = list[i];
			final IType desiredType = types[i];
			if (Types.intFloatCase(ownType, desiredType) || desiredType.isAssignableFrom(ownType)
					|| !ownType.isNumber() && desiredType == Types.NO_TYPE) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Calculates the type distance between two signatures for overload resolution.
	 * 
	 * <p>The distance represents how well the passed signature matches the formal signature.
	 * A lower distance indicates a better match. Returns {@code Integer.MAX_VALUE} if the
	 * signatures have different lengths.</p>
	 *
	 * @param formalSignature the target signature to match against
	 * @param passedSignature the signature being tested
	 * @return the sum of type distances between corresponding types, or Integer.MAX_VALUE if lengths differ
	 */
	public static int distanceBetween(final Signature formalSignature, final Signature passedSignature) {
		IType<?>[] formalTypes = formalSignature.list;
		IType<?>[] passedTypes = passedSignature.list;
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
	 * Compares this signature with another signature for equality.
	 *
	 * @param p the signature to compare with
	 * @return true if signatures are equal (same types in same order)
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
	 * Gets the type at the specified index.
	 *
	 * @param i the index of the type to retrieve
	 * @return the type at the given index
	 * @throws ArrayIndexOutOfBoundsException if the index is out of bounds
	 */
	public IType<?> get(final int i) {
		return list[i];
	}

	/**
	 * Performs type coercion on this signature based on the original signature.
	 * 
	 * <p>This method creates a new array of types where each type in this signature
	 * is coerced to be compatible with the corresponding type in the original signature.</p>
	 *
	 * @param originalSignature the signature to coerce against
	 * @param context the description context for coercion
	 * @return an array of coerced types
	 */
	public IType<?>[] coerce(final Signature originalSignature, final IDescription context) {
		final IType<?>[] result = new IType[list.length];
		for (int i = 0; i < list.length; i++) { result[i] = list[i].coerce(originalSignature.get(i), context); }
		return result;
	}

	/**
	 * Checks if this signature has exactly one type.
	 *
	 * @return true if this signature contains exactly one type
	 */
	public boolean isUnary() { 
		return list.length == 1; 
	}

	/**
	 * Returns the number of types in this signature.
	 *
	 * @return the size of this signature
	 */
	public int size() {
		return list.length;
	}

	/**
	 * Generates a pattern representation of this signature for documentation purposes.
	 * 
	 * <p>This method creates a string representation that can be used for documentation
	 * or pattern matching, with optional variable names for each type.</p>
	 *
	 * @param withVariables if true, includes variable patterns for each type
	 * @return a comma-separated string representation of the signature
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
	 * Checks if this signature might need type coercion when compared to another signature.
	 * 
	 * <p>This method determines if any corresponding types between this signature and
	 * another signature are in the int/float coercion case, which would require
	 * type conversion during method resolution.</p>
	 *
	 * @param other the signature to compare against (must have the same size)
	 * @return true if any type pairs might need int/float coercion
	 */
	public boolean mightNeedCoercionWith(final Signature other) {
		for (int i = 0; i < list.length; i++)
			if (Types.intFloatCase(list[i], other.list[i])) return true;
		return false;
	}

	@Override
	public Iterator<IType<?>> iterator() {
		return Iterators.forArray(list);
	}

}