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
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.api.types.map.IMap;

/**
 * Represents the GAML font type.
 * <p>
 * This type wraps {@link IFont} objects representing fonts that can be used in draw statements
 * and text layers. A font is characterized by:
 * <ul>
 * <li>Face name (e.g., 'Helvetica', 'Arial')</li>
 * <li>Size in points (e.g., 12)</li>
 * <li>Style (#bold, #italic, or a combination)</li>
 * </ul>
 * Fonts can be created from numbers (size), strings (face name), or explicit font specifications.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see IFont
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.FONT,
		id = IType.FONT,
		wraps = { IFont.class },
		kind = ISymbolKind.REGULAR,
		doc = { @doc ("Represents font objects that can be passed directly as arguments to draw statements and text layers. A font is identified by its face name (e.g. 'Helvetica'), its size in points (e.g. 12) and its style (i.e., #bold, #italic, or an addition of the 2") },
		concept = { IConcept.TYPE, IConcept.TEXT, IConcept.DISPLAY })
public class GamaFontType extends GamaType<IFont> {

	/**
	 * Constructs a new GamaFontType.
	 * 
	 * @param typesManager the types manager for type resolution
	 */
	public GamaFontType(final ITypesManager typesManager) {
		super(typesManager);
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
		return GamaFontFactory.castToFont(scope, obj, copy);
	}

	/**
	 * Returns the default font value.
	 * 
	 * @return the default system font
	 */
	@Override
	public IFont getDefault() { return GamaFontFactory.getDefaultFont(); }

	/**
	 * Indicates whether font values can be cast to constants.
	 * 
	 * @return true, as fonts can be compile-time constants
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Deserializes a font from a JSON map.
	 * <p>
	 * The map should contain "name" (face name), "style" (font style flags), and "size" (point size).
	 * </p>
	 * 
	 * @param scope the execution scope
	 * @param map2 the JSON map containing font data
	 * @return the deserialized font
	 */
	@Override
	public IFont deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaFontFactory.createFont(Cast.asString(scope, map2.get(IKeyword.NAME)), Cast.asInt(scope, map2.get("style")),
				Cast.asInt(scope, map2.get("size")));
	}

}
