/*******************************************************************************************************
 *
 * GamaFont.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.font;

import java.awt.Font;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * Standard implementation of {@link IFont} that wraps an AWT {@link Font} instance.
 * 
 * <p>This class extends {@link java.awt.Font} directly, allowing it to be used interchangeably with AWT fonts
 * while also implementing the GAMA {@link IFont} interface for type system integration, serialization, and
 * operator support.</p>
 * 
 * <p>GamaFont instances are effectively immutable value objects. While they extend the mutable Font class,
 * all modification operations in GAMA return new instances rather than modifying existing ones.</p>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Full compatibility with AWT {@link Font} - can be used directly in Swing/AWT rendering</li>
 *   <li>Serialization to GAML syntax for model persistence</li>
 *   <li>JSON serialization for data exchange</li>
 *   <li>Integration with GAMA's type system via {@link IFont}</li>
 *   <li>Immutable value semantics for thread-safe sharing</li>
 * </ul>
 * 
 * <h2>Construction</h2>
 * <p>GamaFont instances should be created using {@link GamaFontFactory} rather than constructing directly:</p>
 * <pre>
 * IFont font = GamaFontFactory.createFont("Helvetica", 12, Font.BOLD);
 * IFont fromString = GamaFontFactory.createFontFrom("Arial-ITALIC-14");
 * IFont defaultFont = GamaFontFactory.getDefaultFont();
 * </pre>
 * 
 * <h2>Serialization Formats</h2>
 * <ul>
 *   <li><b>GAML:</b> {@code font('Helvetica',12,#bold)} - Parseable GAML syntax</li>
 *   <li><b>String:</b> {@code Helvetica-bold-12} - AWT Font.toString() format</li>
 *   <li><b>JSON:</b> Structured object with name, style, and size fields</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>GamaFont instances are thread-safe for reading. No synchronization is needed when accessing font properties
 * or using fonts for rendering, as they are effectively immutable after construction.</p>
 * 
 * @author drogoul
 * @see IFont
 * @see GamaFontFactory
 * @see java.awt.Font
 * @since GAMA 1.7 (March 22, 2015)
 */
class GamaFont extends Font implements IFont {

	/**
	 * Constructs a new GamaFont with the specified name, style, and size.
	 * 
	 * <p>This constructor is package-private. Use {@link GamaFontFactory} to create font instances.</p>
	 *
	 * @param name the font name (family name or logical name)
	 * @param style the font style (0=plain, 1=bold, 2=italic, 3=bold+italic)
	 * @param size the font size in points
	 * @see GamaFontFactory#createFont(String, Integer, Integer)
	 */
	public GamaFont(final String name, final int style, final int size) {
		super(name, style, size);
	}

	/**
	 * Constructs a new GamaFont from an existing AWT Font.
	 * 
	 * <p>This constructor wraps an existing Font object, copying its attributes. This is package-private;
	 * use {@link GamaFontFactory#createFontFrom(Font)} instead.</p>
	 *
	 * @param font the AWT font to wrap
	 * @see GamaFontFactory#createFontFrom(Font)
	 */
	public GamaFont(final Font font) {
		super(font);
	}

	@Override
	public String getName() { return name; }

	@Override

	public int getSize() { return size; }

	@Override

	public int getStyle() { return style; }

	/**
	 * Serializes this font to valid GAML syntax.
	 * 
	 * <p>Returns a string in the format {@code font('name',size,#style)} that can be parsed back into
	 * a font object in GAML code. For example: {@code font('Helvetica',12,#bold+#italic)}</p>
	 * 
	 * <p>The style constants used are:</p>
	 * <ul>
	 *   <li>{@code #plain} - normal weight and upright</li>
	 *   <li>{@code #bold} - bold weight</li>
	 *   <li>{@code #italic} - italic style</li>
	 *   <li>{@code #bold + #italic} - combination of bold and italic</li>
	 * </ul>
	 *
	 * @param includingBuiltIn currently unused for fonts
	 * @return GAML syntax string representing this font
	 * @see IFont#serializeToGaml(boolean)
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		String strStyle;

		if (isBold()) {
			strStyle = isItalic() ? "#bold + #italic" : "#bold";
		} else {
			strStyle = isItalic() ? "#italic" : "#plain";
		}

		return "font('" + name + "'," + pointSize + "," + strStyle + ")";
	}

	/**
	 * Returns the GAMA type for fonts.
	 * 
	 * <p>All GamaFont instances have the type {@link gama.api.gaml.types.Types#FONT}.</p>
	 *
	 * @return the FONT type from the GAMA type system
	 * @see gama.api.gaml.types.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.FONT; }

	/**
	 * Returns a string representation of this font in a format compatible with {@link Font#decode(String)}.
	 * 
	 * <p>The format is {@code name-style-size}, for example: {@code Helvetica-bold-12} or
	 * {@code Arial-italic-14}. This format can be parsed back by {@link Font#decode(String)}.</p>
	 *
	 * @param scope the current scope (unused for fonts)
	 * @return string representation in Font.decode format
	 * @throws GamaRuntimeException if an error occurs during conversion
	 * @see java.awt.Font#decode(String)
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return toString();
	}

	/**
	 * Returns a string representation of this font in AWT format.
	 * 
	 * <p>The format is {@code name-style-size}, where style is one of: plain, bold, italic, or bolditalic.
	 * For example: {@code Helvetica-bold-12}</p>
	 *
	 * @return string representation in AWT format
	 */
	@Override
	public String toString() {
		String strStyle;
		if (isBold()) {
			strStyle = isItalic() ? "bolditalic" : "bold";
		} else {
			strStyle = isItalic() ? "italic" : "plain";
		}
		return name + "-" + strStyle + "-" + size;
	}

	/**
	 * Creates a copy of this font.
	 * 
	 * <p>Returns a new GamaFont instance with identical name, style, and size. Since fonts are immutable
	 * value objects, this creates a true independent copy.</p>
	 *
	 * @param scope the current scope (unused for fonts)
	 * @return a new font instance with the same properties
	 * @throws GamaRuntimeException if an error occurs during copying
	 */
	@Override
	public IFont copy(final IScope scope) throws GamaRuntimeException {
		return GamaFontFactory.createFont(name, style, size);
	}

	/**
	 * Returns the font size as an integer value.
	 * 
	 * <p>When a font is cast to an integer in GAML expressions, this returns the font size in points.</p>
	 *
	 * @param scope the current scope (unused for fonts)
	 * @return the font size in points
	 */
	@Override
	public int intValue(final IScope scope) {
		return getSize();
	}

	/**
	 * Serializes this font to JSON format.
	 * 
	 * <p>Creates a JSON object with the FONT type tag and three properties: name, style, and size.
	 * This format is suitable for data exchange and persistence in JSON-based storage.</p>
	 * 
	 * <p>Example output: {@code {"type":"font","name":"Helvetica","style":1,"size":12}}</p>
	 *
	 * @param json the JSON serializer
	 * @return a JSON value representing this font
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "name", this.name, "style", this.style, "size", this.size);
	}

	/**
	 * Returns this font as an AWT Font.
	 * 
	 * <p>Since GamaFont extends Font, this simply returns {@code this}.</p>
	 *
	 * @return this font instance
	 */
	@Override
	public Font getAwtFont() { return this; }

}
