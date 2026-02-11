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
import gama.api.data.objects.IValue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
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
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
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
	 * @return the integer
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
	 * Cast from string.
	 *
	 * @param s
	 *            the s
	 * @param param
	 *            the param
	 * @return the integer
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

	@Override
	public Integer getDefault() { return 0; }

	@Override
	public boolean computeIsTranslatableInto(final IType<?> type) {
		return type.isNumber() || type == Types.NO_TYPE;
	}

	@Override
	public IType<?> coerce(final IType<?> type, final IDescription context) {
		if (type == this) return null;
		return this;
	}

	/**
	 * Generics information removed here to allow returning IType<Double>
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	public IType computeFindCommonSupertypeWith(final IType<?> type) {
		return type == this ? this : type.id() == IType.FLOAT ? type : Types.NO_TYPE;
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isNumber() { return true; }

}
