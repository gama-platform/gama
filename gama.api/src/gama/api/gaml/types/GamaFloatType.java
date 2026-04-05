/*******************************************************************************************************
 *
 * GamaFloatType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IValue;

/**
 * Represents the GAML float type.
 * <p>
 * This type wraps Java Double/double values, representing floating-point numbers in GAML.
 * Float is the primary numeric type for decimal values and supports automatic conversion
 * from various types:
 * <ul>
 * <li>null → 0.0</li>
 * <li>Double → itself</li>
 * <li>Any Number → doubleValue()</li>
 * <li>String → parsed as double (handles "Infinity" and "NaN")</li>
 * <li>Boolean → 1.0 for true, 0.0 for false</li>
 * <li>IValue → floatValue()</li>
 * <li>Other → 0.0</li>
 * </ul>
 * Float is the common supertype of all numeric types in GAML.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.FLOAT,
		id = IType.FLOAT,
		wraps = { Double.class, double.class },
		kind = ISymbolKind.NUMBER,
		doc = { @doc ("Represents floating point numbers (equivalent to Double in Java)") },
		concept = { IConcept.TYPE })
public class GamaFloatType extends GamaType<Double> {

	/**
	 * Constructs a new GamaFloatType.
	 * 
	 * @param typesManager the types manager for type resolution
	 */
	public GamaFloatType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc ("Cast the argument into a float number. If the argument is a float, returns it; if it is an int, returns it as a float; if it is a string, tries to extract a double from it; if it is a bool, return 1.0 if true and 0.0 if false; if it is a shape (or an agent) returns its area; if it is a font, returns its size; if it is a date, returns the number of milliseconds since the starting date and time of the simulation ")
	public Double cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	/**
	 * Performs static casting to double for various object types.
	 * <p>
	 * This method handles conversion from multiple types using pattern matching,
	 * providing sensible defaults for unsupported types.
	 * </p>
	 *
	 * @param scope the execution scope (may be null for some operations)
	 * @param obj the object to cast to double
	 * @param param optional casting parameter (currently unused)
	 * @param copy whether to copy the result (not applicable for primitives)
	 * @return the double value resulting from the cast
	 */
	public static Double staticCast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		return switch (obj) {
			case null -> 0d;
			case Double d -> d;
			case Number n -> n.doubleValue();
			case String s -> castFromString(s);
			case Boolean b -> b ? 1d : 0d;
			case IValue v -> v.floatValue(scope);
			default -> 0d;
		};
	}

	/**
	 * Casts a string to a double.
	 * <p>
	 * Handles special cases including quoted strings and parsing errors.
	 * Returns 0.0 if parsing fails.
	 * </p>
	 *
	 * @param s the string to parse
	 * @return the parsed double value, or 0.0 if parsing fails
	 */
	private static Double castFromString(final String s) {
		try {
			// Remove surrounding double quotes for "Infinity" and "NaN" cases
			String cleaned = s;
			if (s.startsWith("\"") && s.endsWith("\"")) { cleaned = s.substring(1, s.length() - 1); }
			return Double.parseDouble(cleaned);
		} catch (final NumberFormatException e) {
			return 0d;
		}
	}

	/**
	 * Returns the default value for the float type.
	 * 
	 * @return 0.0, the default float value
	 */
	@Override
	public Double getDefault() { return 0d; }

	/**
	 * Checks if this type can be translated into another type.
	 * <p>
	 * Float can be translated to any numeric type or NO_TYPE.
	 * </p>
	 * 
	 * @param type the target type
	 * @return true if translation is possible, false otherwise
	 */
	@Override
	public boolean computeIsTranslatableInto(final IType<?> type) {
		return type.isNumber() || type == Types.NO_TYPE;
	}

	/**
	 * Coerces this type with another type during type checking.
	 * <p>
	 * Float is the dominant numeric type and coerces other types to itself.
	 * </p>
	 * 
	 * @param type the other type to coerce with
	 * @param context the compilation context
	 * @return this type if coercion is needed, null if types are identical
	 */
	@Override
	public IType<?> coerce(final IType<?> type, final IDescription context) {
		if (type == this) return null;
		return this;
	}

	/**
	 * Finds the common supertype between this type and another type.
	 * <p>
	 * For numeric types, float is the common supertype. For non-numeric types, returns NO_TYPE.
	 * </p>
	 * 
	 * @param type the other type
	 * @return this type if the other is numeric, NO_TYPE otherwise
	 */
	@Override
	public IType<? super Double> computeFindCommonSupertypeWith(final IType<?> type) {
		return type.isNumber() ? this : Types.NO_TYPE;
	}

	/**
	 * Indicates whether float values can be cast to constants.
	 * 
	 * @return true, as float values can be compile-time constants
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Indicates whether this is a numeric type.
	 * 
	 * @return true, as float is a numeric type
	 */
	@Override
	public boolean isNumber() { return true; }
}
