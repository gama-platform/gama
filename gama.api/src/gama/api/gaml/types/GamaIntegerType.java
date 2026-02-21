/*******************************************************************************************************
 *
 * GamaIntegerType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IValue;

/**
 * Represents the GAML integer type.
 * <p>
 * This type wraps Java Integer/int/Long values, representing whole numbers in GAML.
 * Integer supports conversion from various types:
 * <ul>
 * <li>null → 0</li>
 * <li>Integer → itself</li>
 * <li>Any Number → intValue()</li>
 * <li>String → parsed (supports hex with # prefix, custom radix via param)</li>
 * <li>Boolean → 1 for true, 0 for false</li>
 * <li>IValue → intValue()</li>
 * <li>Other → 0</li>
 * </ul>
 * Integers can be coerced to floats when necessary.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.INT,
		id = IType.INT,
		wraps = { Integer.class, int.class, Long.class },
		kind = ISymbolKind.Variable.NUMBER,
		concept = { IConcept.TYPE },
		doc = @doc ("Type of integer numbers"))
public class GamaIntegerType extends GamaType<Integer> {

	/**
	 * Constructs a new GamaIntegerType.
	 * 
	 * @param typesManager the types manager for type resolution
	 */
	public GamaIntegerType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc ("""
			Returns the parameter casted to an int value. If it is an integer, returns it. A float, returns its integer value. \
			A color, its rgb value. An agent, its index. A string, attempts to parse it (with a radix of 16 if it begins with '#'). \
			A bool, return 1 if true. A font, return its size.  Otherwise return 0""")
	public Integer cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	/**
	 * Performs static casting to integer for various object types.
	 * <p>
	 * This method handles conversion from multiple types using pattern matching.
	 * </p>
	 *
	 * @param scope the execution scope (may be null for some operations)
	 * @param obj the object to cast to integer
	 * @param param optional radix parameter for string parsing (default 10)
	 * @param copy whether to copy the result (not applicable for primitives)
	 * @return the integer value resulting from the cast
	 */
	public static Integer staticCast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		return switch (obj) {
			case null -> 0;
			case Integer i -> i;
			case Number n -> n.intValue();
			case String s -> castFromString(s, param);
			case Boolean b -> b ? 1 : 0;
			case IValue v -> v.intValue(scope);
			default -> 0;
		};
	}

	/**
	 * Casts a string to an integer.
	 * <p>
	 * Handles multiple formats:
	 * <ul>
	 * <li>Strings starting with '#' are parsed as hexadecimal</li>
	 * <li>Strings with whitespace have it removed before parsing</li>
	 * <li>Can use custom radix if provided via param</li>
	 * <li>Falls back to parsing as double then truncating if integer parsing fails</li>
	 * <li>Returns 0 if all parsing fails</li>
	 * </ul>
	 * </p>
	 *
	 * @param s the string to parse
	 * @param param optional radix parameter (Integer)
	 * @return the parsed integer value, or 0 if parsing fails
	 */
	private static Integer castFromString(final String s, final Object param) {
		String n = s.replaceAll("\\p{Zs}", "");
		try {
			if (n.startsWith("#")) return Integer.parseInt(n.substring(1), 16);
			int radix = 10;
			if (param instanceof Integer) { radix = (Integer) param; }
			return Integer.parseInt(n, radix);
		} catch (final NumberFormatException e) {
			Double d = 0d;
			try {
				d = Double.parseDouble(n);
			} catch (final NumberFormatException e1) {
				return 0;
			}
			return d.intValue();
		}
	}

	/**
	 * Returns the default value for the integer type.
	 * 
	 * @return 0, the default integer value
	 */
	@Override
	public Integer getDefault() { return 0; }

	/**
	 * Checks if this type can be translated into another type.
	 * <p>
	 * Integer can be translated to any numeric type or NO_TYPE.
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
	 * Integer coerces to itself for same-type comparisons.
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
	 * For numeric types:
	 * <ul>
	 * <li>int + int = int</li>
	 * <li>int + float = float</li>
	 * <li>int + other = NO_TYPE</li>
	 * </ul>
	 * </p>
	 * 
	 * @param type the other type
	 * @return this type if the other is also int, float if the other is float, NO_TYPE otherwise
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	public IType computeFindCommonSupertypeWith(final IType<?> type) {
		return type == this ? this : type.id() == IType.FLOAT ? type : Types.NO_TYPE;
	}

	/**
	 * Indicates whether integer values can be cast to constants.
	 * 
	 * @return true, as integer values can be compile-time constants
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Indicates whether this is a numeric type.
	 * 
	 * @return true, as integer is a numeric type
	 */
	@Override
	public boolean isNumber() { return true; }

}
