/*******************************************************************************************************
 *
 * GamaFloatType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.FLOAT,
		id = IType.FLOAT,
		wraps = { Double.class, double.class },
		kind = ISymbolKind.Variable.NUMBER,
		doc = { @doc ("Represents floating point numbers (equivalent to Double in Java)") },
		concept = { IConcept.TYPE })
public class GamaFloatType extends GamaType<Double> {

	@Override
	@doc ("Cast the argument into a float number. If the argument is a float, returns it; if it is an int, returns it as a float; if it is a string, tries to extract a double from it; if it is a bool, return 1.0 if true and 0.0 if false; if it is a shape (or an agent) returns its area; if it is a font, returns its size; if it is a date, returns the number of milliseconds since the starting date and time of the simulation ")
	public Double cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the double
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
	 * Cast from string.
	 *
	 * @param s
	 *            the s
	 * @return the double
	 */
	private static Double castFromString(final String s) {
		try {
			return Double.parseDouble(s);
		} catch (final NumberFormatException e) {
			return 0d;
		}
	}

	@Override
	public Double getDefault() { return 0d; }

	@Override
	public boolean isTranslatableInto(final IType<?> type) {
		return type.isNumber() || type == Types.NO_TYPE;
	}

	@Override
	public IType<?> coerce(final IType<?> type, final IDescription context) {
		if (type == this) return null;
		return this;
	}

	@Override
	public IType<? super Double> findCommonSupertypeWith(final IType<?> type) {
		return type.isNumber() ? this : Types.NO_TYPE;
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isNumber() { return true; }
}
