/*******************************************************************************************************
 *
 * GamaNoType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.runtime.scope.IScope;

/**
 * Type representing the absence of type information in GAML - the root of the type hierarchy.
 * <p>
 * GamaNoType (also known as "unknown" type) serves as a special type representing values without precise type
 * information. It acts as the universal supertype in GAMA's type hierarchy - all other types are subtypes of NoType.
 * This type is used during compilation when type information cannot be determined, and at runtime for untyped values.
 * </p>
 *
 * <h2>Key Characteristics:</h2>
 * <ul>
 * <li>Universal supertype - all types are subtypes of NoType</li>
 * <li>Represents absence of type information</li>
 * <li>Used for untyped or dynamically typed values</li>
 * <li>Minimal type restrictions on operations</li>
 * <li>Default null value</li>
 * <li>Can be cast to constant</li>
 * </ul>
 *
 * <h2>Type Hierarchy Role:</h2>
 * <p>
 * In GAMA's type system:
 * <ul>
 * <li>NoType is the top of the type hierarchy</li>
 * <li>All types are assignable to NoType</li>
 * <li>NoType is compatible with any other type</li>
 * <li>Common supertype resolution falls back to NoType when no specific common type exists</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Scenarios:</h2>
 * <ul>
 * <li>Variables declared without explicit type</li>
 * <li>Generic containers with heterogeneous contents</li>
 * <li>Dynamic programming where type is determined at runtime</li>
 * <li>Fallback type when type inference fails</li>
 * <li>Placeholder during compilation</li>
 * </ul>
 *
 * <h2>Examples:</h2>
 *
 * <pre>
 * {@code
 * // Implicitly uses NoType (though GAMA usually infers specific types)
 * unknown my_value <- "hello";
 * my_value <- 42;  // Can change to different type
 * my_value <- {10, 20};  // And again to another type
 *
 * // Generic/heterogeneous collections
 * list mixed_list <- ["text", 42, {10, 20}, self];
 *
 * // Dynamic operations where type is unknown at compile time
 * unknown result <- some_plugin_operation();
 * }
 * </pre>
 *
 * <h2>Type Translation Limitations:</h2>
 * <p>
 * While NoType is compatible with most types, it cannot be directly translated to certain primitive types at compile
 * time:
 * <ul>
 * <li>bool - boolean conversion requires known value</li>
 * <li>int - integer conversion requires known value</li>
 * <li>float - float conversion requires known value</li>
 * </ul>
 * This prevents incorrect compile-time assumptions about unknown values.
 * </p>
 *
 * @author GAMA Development Team
 * @see GamaType
 * @since GAMA 1.0
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.UNKNOWN,
		id = IType.NONE,
		wraps = { Object.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE },
		doc = @doc ("A type, root of all other types, that represents values without a precise type"))
public class GamaNoType extends GamaType<Object> {

	/**
	 * Constructs a new NoType.
	 *
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaNoType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to NoType.
	 * <p>
	 * Since NoType accepts any value, this simply returns the object itself without transformation. The copy parameter
	 * is ignored as there's no type-specific copying needed.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast (returned as-is)
	 * @param param
	 *            optional parameter (not used)
	 * @param copy
	 *            whether to create a copy (ignored for NoType)
	 * @return the object itself
	 */
	@Override
	@doc ("Returns the parameter itself")
	public Object cast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		// WARNING: Should we obey the "copy" parameter in this case ?
		return obj;
	}

	/**
	 * Returns the default value for NoType.
	 * <p>
	 * The default value is null, representing absence of value.
	 * </p>
	 *
	 * @return null
	 */
	@Override
	public Object getDefault() { return null; }

	/**
	 * Checks if this type is a supertype of another type.
	 * <p>
	 * As the universal supertype, NoType is always a supertype of any other type.
	 * </p>
	 *
	 * @param type
	 *            the type to check
	 * @return true always, as NoType is the supertype of all types
	 */
	@Override
	public boolean isSuperTypeOf(final IType<?> type) {
		return true;
	}

	/**
	 * Computes the common supertype between this type and another.
	 * <p>
	 * As the universal supertype, NoType is the common supertype for any type combination.
	 * </p>
	 *
	 * @param iType
	 *            the other type
	 * @return this (NoType), as it's the common supertype of all types
	 */
	@Override
	public IType<Object> computeFindCommonSupertypeWith(final IType<?> iType) {
		// By default, this is the supertype common to all subtypes
		return this;
	}

	/**
	 * Indicates whether NoType values can be cast to constant.
	 * <p>
	 * NoType can be constant since it places no restrictions on values.
	 * </p>
	 *
	 * @return true, NoType values can be constant
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Determines if NoType values can be translated into another type at compile time.
	 * <p>
	 * An unknown value (at compilation time) cannot be reliably translated into primitive numeric or boolean types, as
	 * this would require knowing the actual value. However, it can be translated to other types.
	 * </p>
	 *
	 * @param t
	 *            the target type
	 * @return false for bool, int, and float types; true for all other types
	 */
	@Override
	public boolean computeIsTranslatableInto(final IType<?> t) {
		return switch (t.id()) {
			case IType.BOOL, IType.INT, IType.FLOAT -> false;
			default -> true;
		};
	}

}
