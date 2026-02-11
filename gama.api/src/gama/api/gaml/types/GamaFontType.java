/*******************************************************************************************************
 *
 * GamaFontType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.type;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaFontFactory;
import gama.api.data.objects.IFont;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.FONT,
		id = IType.FONT,
		wraps = { IFont.class },
		kind = ISymbolKind.Variable.REGULAR,
		doc = { @doc ("Represents font objects that can be passed directly as arguments to draw statements and text layers. A font is identified by its face name (e.g. 'Helvetica'), its size in points (e.g. 12) and its style (i.e., #bold, #italic, or an addition of the 2") },
		concept = { IConcept.TYPE, IConcept.TEXT, IConcept.DISPLAY })
public class GamaFontType extends GamaType<IFont> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaFontType(final ITypesManager typesManager) {
		super(typesManager);
		// TODO Auto-generated constructor stub
	}

	@doc (
			value = "Cast any object as a font",
			usages = { @usage (
					value = "if the operand is a number, returns with the operand value as font size and the default display font style",
					examples = { @example ("font f <- font(12);") }),
					@usage (
							value = "if the operand is a string, returns a font with this font name",
							examples = { @example ("font f <- font('Arial');") }), })
	@Override
	public IFont cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaFontFactory.createFontFrom(scope, obj, copy);
	}

	@Override
	public IFont getDefault() { return GamaFontFactory.getDefaultFont(); }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public IFont deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaFontFactory.createFont(Cast.asString(scope, map2.get("name")), Cast.asInt(scope, map2.get("style")),
				Cast.asInt(scope, map2.get("size")));
	}

}
