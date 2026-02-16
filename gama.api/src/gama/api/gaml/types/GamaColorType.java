/*******************************************************************************************************
 *
 * GamaColorType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.map.IMap;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@type (
		name = IKeyword.RGB,
		id = IType.COLOR,
		wraps = { IColor.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.COLOR },
		doc = @doc ("The type rgb represents colors in GAML, with their three red, green, blue components and, optionally, a fourth alpha component "))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaColorType extends GamaType<IColor> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaColorType(final ITypesManager typesManager) {
		super(typesManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	@doc ("Transforms the parameter into a rgb color. A second parameter can be used to express the transparency of the color, either an int (between 0 and 255) or a float (between 0 and 1)")
	public IColor cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaColorFactory.castToColor(scope, obj, param, copy);
	}

	@Override
	public IColor getDefault() {
		return null; // new GamaColor(Color.black);
	}

	@Override
	public IType getContentType() { return Types.get(INT); }

	@Override
	public IType getKeyType() { return Types.get(INT); }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isCompoundType() { return true; }

	@Override
	public IColor deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return cast(scope, map2, null, false);
	}

}
