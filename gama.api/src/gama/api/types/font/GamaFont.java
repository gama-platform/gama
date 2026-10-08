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

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * Standard implementation of {@link IFont} that wraps an AWT {@link Font} instance.
 *
 * <p>
 * This class extends {@link java.awt.Font} directly, allowing it to be used interchangeably with AWT fonts while also
 * implementing the GAMA {@link IFont} interface for type system integration, serialization, and operator support.
 * </p>
 *
 * <p>
 * GamaFont instances are effectively immutable value objects. While they extend the mutable Font class, all
 * modification operations in GAMA return new instances rather than modifying existing ones.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 * <li>Serialization to GAML syntax for model persistence</li>
 * <li>JSON serialization for data exchange</li>
 * <li>Integration with GAMA's type system via {@link IFont}</li>
 * <li>Immutable value semantics for thread-safe sharing</li>
 * </ul>
 *
 * <h2>Construction</h2>
 * <p>
 * GamaFont instances should be created using {@link GamaFontFactory} rather than constructing directly:
 * </p>
 *
 * <pre>
 * IFont font = GamaFontFactory.createFont("Helvetica", 12, Font.BOLD);
 * IFont fromString = GamaFontFactory.createFontFrom("Arial-ITALIC-14");
 * IFont defaultFont = GamaFontFactory.getDefaultFont();
 * </pre>
 *
 * <h2>Serialization Formats</h2>
 * <ul>
 * <li><b>GAML:</b> {@code font('Helvetica',12,#bold)} - Parseable GAML syntax</li>
 * <li><b>String:</b> {@code Helvetica-bold-12} - AWT Font.toString() format</li>
 * <li><b>JSON:</b> Structured object with name, style, and size fields</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * GamaFont instances are thread-safe for reading. No synchronization is needed when accessing font properties or using
 * fonts for rendering, as they are effectively immutable after construction.
 * </p>
 *
 * @author drogoul
 * @see IFont
 * @see GamaFontFactory
 * @see java.awt.Font
 * @since GAMA 1.7 (March 22, 2015)
 */
/**
 * The underlying AWT {@link Font} instance that holds the font's name, style, and size.
 *
 * <p>
 * This component is the canonical representation of the font data and is used by all accessors and
 * serialization methods. It is set once at construction time and never replaced, ensuring the immutability
 * of {@code GamaFont} instances.
 * </p>
 *
 * @see java.awt.Font
 */
record GamaFont(Font internal) implements IFont {

	/**
	 * Returns the logical name of this font.
	 *
	 * <p>
	 * The logical name is the name that was used to create the font, which may be either a physical font
	 * family name (e.g., {@code "Helvetica Neue"}, {@code "Arial"}) or a platform-independent logical name
	 * (e.g., {@code "Dialog"}, {@code "SansSerif"}, {@code "Serif"}, {@code "Monospaced"}).
	 * </p>
	 *
	 * <p>
	 * To obtain the full font face name including style information, use {@link #getFontName()} instead.
	 * </p>
	 *
	 * @return the logical name of this font, never {@code null}
	 * @see #getFontName()
	 * @see java.awt.Font#getName()
	 */
	@Override
	public String getName() { return internal.getName(); }

	/**
	 * Returns the point size of this font, rounded to an integer.
	 *
	 * <p>
	 * The size is expressed in typographic points (pt). On screen, one point typically corresponds to one
	 * device-independent pixel at 72 dpi. Common display sizes in GAMA models range from 8 to 72 points.
	 * </p>
	 *
	 * <p>
	 * In GAML, when a font is cast to an integer, this value is returned:
	 * </p>
	 *
	 * <pre>
	 * int sz &lt;- int(font("Arial", 14, #bold)); // sz = 14
	 * </pre>
	 *
	 * @return the size of this font in points, always a positive integer
	 * @see java.awt.Font#getSize()
	 * @see #intValue(IScope)
	 */
	@Override
	public int getSize() { return internal.getSize(); }

	/**
	 * Returns the style of this font as a bitfield integer.
	 *
	 * <p>
	 * The style is a bitwise combination of the following constants from {@link java.awt.Font}:
	 * </p>
	 * <ul>
	 * <li>{@link java.awt.Font#PLAIN} ({@code 0}) - Normal weight and upright</li>
	 * <li>{@link java.awt.Font#BOLD} ({@code 1}) - Bold weight</li>
	 * <li>{@link java.awt.Font#ITALIC} ({@code 2}) - Italic/oblique style</li>
	 * <li>{@code Font.BOLD | Font.ITALIC} ({@code 3}) - Both bold and italic</li>
	 * </ul>
	 *
	 * <p>
	 * These correspond to the GAML constants {@code #plain}, {@code #bold}, {@code #italic}, and
	 * {@code #bold + #italic} respectively.
	 * </p>
	 *
	 * @return the style bitfield of this font; one of {@code 0} (plain), {@code 1} (bold), {@code 2} (italic),
	 *         or {@code 3} (bold+italic)
	 * @see java.awt.Font#getStyle()
	 * @see java.awt.Font#PLAIN
	 * @see java.awt.Font#BOLD
	 * @see java.awt.Font#ITALIC
	 */
	@Override
	public int getStyle() { return internal.getStyle(); }

	/**
	 * Serializes this font to valid GAML syntax.
	 *
	 * <p>
	 * Returns a string in the format {@code font('name',size,#style)} that can be parsed back into a font object in
	 * GAML code. For example: {@code font('Helvetica',12,#bold+#italic)}
	 * </p>
	 *
	 * <p>
	 * The style constants used are:
	 * </p>
	 * <ul>
	 * <li>{@code #plain} - normal weight and upright</li>
	 * <li>{@code #bold} - bold weight</li>
	 * <li>{@code #italic} - italic style</li>
	 * <li>{@code #bold + #italic} - combination of bold and italic</li>
	 * </ul>
	 *
	 * @param includingBuiltIn
	 *            currently unused for fonts
	 * @return GAML syntax string representing this font
	 * @see IFont#serializeToGaml(boolean)
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		String strStyle;
		if (internal.isBold()) {
			strStyle = internal.isItalic() ? "#bold + #italic" : "#bold";
		} else {
			strStyle = internal.isItalic() ? "#italic" : "#plain";
		}
		return "font('" + internal.getName() + "'," + internal.getSize() + "," + strStyle + ")";
	}

	/**
	 * Returns the GAMA type for fonts.
	 *
	 * <p>
	 * All GamaFont instances have the type {@link gama.api.gaml.types.Types#FONT}.
	 * </p>
	 *
	 * @return the FONT type from the GAMA type system
	 * @see gama.api.gaml.types.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.FONT; }

	/**
	 * Returns a string representation of this font in a format compatible with {@link Font#decode(String)}.
	 *
	 * <p>
	 * The format is {@code name-style-size}, for example: {@code Helvetica-bold-12} or {@code Arial-italic-14}. This
	 * format can be parsed back by {@link Font#decode(String)}.
	 * </p>
	 *
	 * @param scope
	 *            the current scope (unused for fonts)
	 * @return string representation in Font.decode format
	 * @throws GamaRuntimeException
	 *             if an error occurs during conversion
	 * @see java.awt.Font#decode(String)
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return toString();
	}

	/**
	 * Returns a string representation of this font in AWT format.
	 *
	 * <p>
	 * The format is {@code name-style-size}, where style is one of: plain, bold, italic, or bolditalic. For example:
	 * {@code Helvetica-bold-12}
	 * </p>
	 *
	 * @return string representation in AWT format
	 */
	@Override
	public String toString() {
		String strStyle;
		if (internal.isBold()) {
			strStyle = internal.isItalic() ? "bolditalic" : "bold";
		} else {
			strStyle = internal.isItalic() ? "italic" : "plain";
		}
		return internal.getName() + "-" + strStyle + "-" + internal.getSize();
	}

	/**
	 * Creates a copy of this font.
	 *
	 * <p>
	 * Returns a new GamaFont instance with identical name, style, and size. Since fonts are immutable value objects,
	 * this creates a true independent copy.
	 * </p>
	 *
	 * @param scope
	 *            the current scope (unused for fonts)
	 * @return a new font instance with the same properties
	 * @throws GamaRuntimeException
	 *             if an error occurs during copying
	 */
	@Override
	public IFont copy(final IScope scope) throws GamaRuntimeException {
		return GamaFontFactory.createFontFrom(internal);
	}

	/**
	 * Returns the font size as an integer value.
	 *
	 * <p>
	 * When a font is cast to an integer in GAML expressions, this returns the font size in points.
	 * </p>
	 *
	 * @param scope
	 *            the current scope (unused for fonts)
	 * @return the font size in points
	 */
	@Override
	public int intValue(final IScope scope) {
		return getSize();
	}

	/**
	 * Serializes this font to JSON format.
	 *
	 * <p>
	 * Creates a JSON object with the FONT type tag and three properties: name, style, and size. This format is suitable
	 * for data exchange and persistence in JSON-based storage.
	 * </p>
	 *
	 * <p>
	 * Example output: {@code {"type":"font","name":"Helvetica","style":1,"size":12}}
	 * </p>
	 *
	 * @param json
	 *            the JSON serializer
	 * @return a JSON value representing this font
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), IKeyword.NAME, getName(), IKeyword.STYLE, getStyle(), IKeyword.SIZE,
				getSize());
	}

	/**
	 * Returns the underlying AWT {@link Font} instance for this GAMA font.
	 *
	 * <p>
	 * This method provides interoperability with AWT/Swing rendering code that requires a {@link java.awt.Font}
	 * directly. The returned instance is the same object stored in the {@link #internal} record component.
	 * </p>
	 *
	 * @return the AWT {@link Font} wrapped by this instance, never {@code null}
	 * @see java.awt.Font
	 * @see IFont#getAwtFont()
	 */
	@Override
	public Font getAwtFont() { return internal; }

	/**
	 * Returns the full font face name of this font.
	 *
	 * <p>
	 * Unlike {@link #getName()}, which returns the logical name used to construct the font, this method
	 * returns the actual face name of the underlying typeface as known to the platform's font subsystem.
	 * For example, a font constructed with the logical name {@code "Helvetica"} and style {@code Font.BOLD}
	 * may return {@code "Helvetica-Bold"} here, whereas {@link #getName()} would return {@code "Helvetica"}.
	 * </p>
	 *
	 * <p>
	 * If the requested font was substituted by the platform (e.g., because the family is not installed),
	 * the face name of the substitute font is returned.
	 * </p>
	 *
	 * @return the full font face name as reported by the platform, never {@code null}
	 * @see java.awt.Font#getFontName()
	 * @see #getName()
	 */
	@Override
	public String getFontName() { return internal.getFontName(); }

}
