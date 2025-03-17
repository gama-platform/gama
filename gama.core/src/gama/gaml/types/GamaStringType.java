/*******************************************************************************************************
 *
 * GamaStringType.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.2025-03).
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
import gama.gaml.interfaces.INamed;

/**
 *
 *
 *
 * Written by drogoul Modified on 3 juin 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.STRING,
		id = IType.STRING,
		wraps = { String.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.STRING },
		doc = @doc ("Strings are ordered list of characters"))
public class GamaStringType extends GamaType<String> {

	@Override
	@doc ("Transforms the parameter into a string")
	public String cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param copy
	 *            the copy
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static String staticCast(final IScope scope, final Object obj, final boolean copy)
			throws GamaRuntimeException {
		return switch (obj) {
			case null -> null;
			case String s -> s;
			case IValue i -> i.stringValue(scope);
			case INamed n -> n.getName();
			default -> obj.toString();
		};
	}

	@Override
	public String getDefault() { return null; }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isDrawable() { return true; }

}
