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
 * Represents the GAML color (rgb) type.
 * <p>
 * This type wraps {@link IColor} objects representing colors with red, green, blue, and optionally
 * alpha (transparency) components. Colors in GAMA can be created from various sources including
 * RGB values, hexadecimal strings, color names, and maps.
 * </p>
 * <p>
 * The type is compound (has components accessible by index), can be cast to constants, and supports
 * a second parameter for transparency during casting.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see IColor
 */
@type (
		name = IKeyword.RGB,
		id = IType.COLOR,
		wraps = { IColor.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE, IConcept.COLOR },
		doc = @doc ("The type rgb represents colors in GAML, with their three red, green, blue components and, optionally, a fourth alpha component "))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaColorType extends GamaType<IColor> {

	/**
	 * Constructs a new GamaColorType.
	 * 
	 * @param typesManager the types manager for type resolution
	 */
	public GamaColorType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc ("Transforms the parameter into a rgb color. A second parameter can be used to express the transparency of the color, either an int (between 0 and 255) or a float (between 0 and 1)")
	public IColor cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaColorFactory.castToColor(scope, obj, param, copy);
	}

	/**
	 * Returns the default value for the color type.
	 * 
	 * @return null (no default color)
	 */
	@Override
	public IColor getDefault() {
		return null; // new GamaColor(Color.black);
	}

	/**
	 * Gets the content type for this type (when treated as a container).
	 * <p>
	 * Colors can be indexed to access their RGB(A) components, which are integers.
	 * </p>
	 * 
	 * @return the INTEGER type
	 */
	@Override
	public IType getContentType() { return Types.get(INT); }

	/**
	 * Gets the key type for this type (when treated as a container).
	 * <p>
	 * Colors are indexed by integer positions (0=red, 1=green, 2=blue, 3=alpha).
	 * </p>
	 * 
	 * @return the INTEGER type
	 */
	@Override
	public IType getKeyType() { return Types.get(INT); }

	/**
	 * Indicates whether color values can be cast to constants.
	 * 
	 * @return true, as colors can be compile-time constants
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}

	/**
	 * Indicates whether this is a compound type.
	 * <p>
	 * Colors are compound types as they have accessible components (RGBA values).
	 * </p>
	 * 
	 * @return true, as colors have multiple components
	 */
	@Override
	public boolean isCompoundType() { return true; }

	/**
	 * Deserializes a color from a JSON map.
	 * <p>
	 * The map can contain color data in various formats (e.g., RGB values, hex string, etc.)
	 * which are processed by the standard cast method.
	 * </p>
	 * 
	 * @param scope the execution scope
	 * @param map2 the JSON map containing the color data
	 * @return the deserialized color
	 */
	@Override
	public IColor deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return cast(scope, map2, null, false);
	}

}
