/*******************************************************************************************************
 *
 * GamaFontFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.font;

import static gama.api.utils.prefs.GamaPreferences.Displays.DEFAULT_DISPLAY_FONT;

import java.awt.Font;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.runtime.scope.IScope;

/**
 * A static factory for creating {@link IFont} instances (representing typefaces used in displays).
 * 
 * <p>This class provides the primary API for font creation in GAMA, offering multiple creation methods and
 * operator definitions that are accessible from GAML code. It abstracts the font creation process and provides
 * convenient operators for font manipulation.</p>
 * 
 * <h2>Factory Methods</h2>
 * <ul>
 *   <li>{@link #getDefaultFont()} - Returns the system default display font</li>
 *   <li>{@link #createFont(String, Integer)} - Creates a plain font with name and size</li>
 *   <li>{@link #createFont(String, Integer, Integer)} - Creates a font with name, size, and style</li>
 *   <li>{@link #createFontFrom(String)} - Creates a font from a font descriptor string</li>
 *   <li>{@link #createFontFrom(Font)} - Wraps an existing AWT Font</li>
 *   <li>{@link #cloneFont(IFont)} - Creates a copy of an existing font</li>
 *   <li>{@link #castToFont(IScope, Object, boolean)} - Type conversion to font</li>
 * </ul>
 * 
 * <h2>GAML Operators</h2>
 * <p>This class defines the following GAML operators accessible in models:</p>
 * 
 * <h3>font operator</h3>
 * <pre>
 * font my_font &lt;- font("Helvetica", 14);
 * font styled_font &lt;- font("Arial", 16, #bold + #italic);
 * </pre>
 * 
 * <h3>with_size operator</h3>
 * <pre>
 * font larger &lt;- my_font with_size 20;
 * </pre>
 * 
 * <h3>with_style operator</h3>
 * <pre>
 * font bold &lt;- my_font with_style #bold;
 * font italic &lt;- my_font with_style #italic;
 * font plain &lt;- my_font with_style #plain;
 * </pre>
 * 
 * <h2>Font Styles</h2>
 * <p>Font styles are specified using GAML constants or integer codes:</p>
 * <ul>
 *   <li>{@code #plain} or 0 - Normal weight, upright</li>
 *   <li>{@code #bold} or 1 - Bold weight</li>
 *   <li>{@code #italic} or 2 - Italic/oblique style</li>
 *   <li>{@code #bold + #italic} or 3 - Bold and italic combined</li>
 * </ul>
 * 
 * <h2>Font Names</h2>
 * <p>Font names can be:</p>
 * <ul>
 *   <li><b>Physical names:</b> Actual font family names like "Helvetica Neue", "Arial", "Times New Roman"</li>
 *   <li><b>Logical names:</b> Platform-independent names like "Dialog", "SansSerif", "Serif", "Monospaced"</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>All methods in this class are thread-safe. Font instances created are immutable and can be safely
 * shared across threads.</p>
 * 
 * <h2>Example Usage</h2>
 * <pre>
 * // Get default font
 * IFont defaultFont = GamaFontFactory.getDefaultFont();
 * 
 * // Create custom fonts
 * IFont plainFont = GamaFontFactory.createFont("Helvetica", 12);
 * IFont boldFont = GamaFontFactory.createFont("Arial", 14, Font.BOLD);
 * 
 * // Parse from string
 * IFont fromString = GamaFontFactory.createFontFrom("Courier-BOLD-10");
 * 
 * // Modify existing fonts
 * IFont larger = GamaFontFactory.cloneWithSize(plainFont, 18);
 * IFont italic = GamaFontFactory.cloneWithStyle(plainFont, Font.ITALIC);
 * </pre>
 * 
 * @author drogoul
 * @see IFont
 * @see GamaFont
 * @see java.awt.Font
 * @since GAMA 1.7
 */
public class GamaFontFactory {

	/**
	 * Returns the default font used in GAMA displays.
	 * 
	 * <p>The default font is configured through GAMA preferences and typically represents a readable
	 * sans-serif font suitable for UI displays.</p>
	 *
	 * @return the default display font from preferences
	 * @see gama.api.utils.prefs.GamaPreferences.Displays#DEFAULT_DISPLAY_FONT
	 */
	public static IFont getDefaultFont() { return DEFAULT_DISPLAY_FONT.getValue(); }

	/**
	 * Creates a copy of an existing {@link IFont}.
	 * 
	 * <p>Creates a new font instance with identical properties (name, style, size) to the source font.
	 * This is equivalent to calling {@code copy(scope)} on the font itself.</p>
	 *
	 * @param value the source font to copy
	 * @return a new {@link IFont} instance identical to the source
	 * @see IFont#copy(IScope)
	 */
	public static IFont cloneFont(final IFont value) {
		return new GamaFont(value.getName(), value.getStyle(), value.getSize());
	}

	/**
	 * Creates a new font from a string definition.
	 * 
	 * <p>Parses a font descriptor string in the format used by {@link Font#decode(String)}. The string
	 * format is {@code name-style-size}, for example:</p>
	 * <ul>
	 *   <li>{@code "Helvetica-BOLD-12"}</li>
	 *   <li>{@code "Arial-ITALIC-14"}</li>
	 *   <li>{@code "Courier-PLAIN-10"}</li>
	 * </ul>
	 * 
	 * <p>If the string doesn't match this format, a default font will be created.</p>
	 *
	 * @param def the font definition string (e.g., "Helvetica-BOLD-12")
	 * @return the corresponding {@link IFont} instance
	 * @see Font#decode(String)
	 */
	public static IFont createFontFrom(final String def) {
		return createFontFrom(Font.decode(def));
	}

	/**
	 * Wraps an existing AWT {@link Font} as a GAMA {@link IFont}.
	 * 
	 * <p>This method is useful for integrating with AWT/Swing code that produces Font objects. It extracts
	 * the font name, style, and size and creates a GamaFont instance.</p>
	 *
	 * @param awtFont the AWT font to wrap
	 * @return a new {@link IFont} wrapping the AWT font
	 */
	public static IFont createFontFrom(final Font awtFont) {
		return new GamaFont(awtFont.getFontName(), awtFont.getStyle(), awtFont.getSize());
	}

	/**
	 * Converts an object to a font, with optional copying.
	 * 
	 * <p>This method implements GAMA's type casting logic for fonts. It handles conversion from:</p>
	 * <ul>
	 *   <li><b>Number:</b> Creates a default font with the number as the size</li>
	 *   <li><b>IFont:</b> Returns the font directly or creates a copy if requested</li>
	 *   <li><b>String:</b> Parses the string as a font descriptor</li>
	 *   <li><b>null/other:</b> Returns the default font</li>
	 * </ul>
	 * 
	 * <p>This method is used internally by GAMA's type system when casting values to the font type.</p>
	 *
	 * @param scope the current scope
	 * @param obj the object to convert to a font
	 * @param copy whether to copy font instances (true) or return them directly (false)
	 * @return the resulting font
	 */
	public static IFont castToFont(final IScope scope, final Object obj, final boolean copy) {
		return switch (obj) {
			case Number size -> cloneWithSize(getDefaultFont(), size.intValue());
			case IFont f -> copy ? cloneFont(f) : f;
			case String s -> createFontFrom(s);
			case null, default -> getDefaultFont();
		};
	}

	/**
	 * With size.
	 *
	 * @param font
	 *            the font
	 * @param size
	 *            the size
	 * @return the gama font
	 */
	@operator (
			value = "with_size",
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.TEXT, IConcept.DISPLAY },
			can_be_const = true)
	@doc (
			value = "Creates a new font from an existing font, with a new size in points",
			masterDoc = true,
			examples = @example (
					value = "font ('Helvetica Neue',12, #bold + #italic) with_size 24",
					equals = "a bold and italic face of the Helvetica Neue family with a size of 24 points",
					test = false))
	@no_test
	public static IFont cloneWithSize(final IFont font, final Integer size) {
		return new GamaFont(font.getName(), font.getStyle(), size);
	}

	/**
	 * With size.
	 *
	 * @param font
	 *            the font
	 * @param size
	 *            the size
	 * @return the gama font
	 */
	@operator (
			value = "with_style",
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.TEXT, IConcept.DISPLAY },
			can_be_const = true)
	@doc (
			value = "Creates a new font from an existing font, with a new style: either #bold, #italic or #plain or a combination (addition) of them.",
			masterDoc = true,
			examples = @example (
					value = "font ('Helvetica Neue',12, #bold + #italic) with_style #plain",
					equals = "a plain face of the Helvetica Neue family with a size of 12 points",
					test = false))
	@no_test
	public static IFont cloneWithStyle(final IFont font, final Integer style) {
		return new GamaFont(font.getName(), style, font.getSize());
	}

	/**
	 * Font.
	 *
	 * @param name
	 *            the name
	 * @param size
	 *            the size
	 * @return the gama font
	 */
	@operator (
			value = IKeyword.FONT,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.TEXT, IConcept.DISPLAY },
			can_be_const = true)
	@doc (
			value = "Creates a new font, by specifying its name (either a font face name like 'Lucida Grande Bold' or 'Helvetica', or a logical name like 'Dialog', 'SansSerif', 'Serif', etc.) and a size in points. No style is attached to this font")
	@no_test
	public static IFont createFont(final String name, final Integer size) {
		return new GamaFont(name, Font.PLAIN, size);
	}

	/**
	 * Font.
	 *
	 * @param name
	 *            the name
	 * @param size
	 *            the size
	 * @param style
	 *            the style
	 * @return the gama font
	 */
	@operator (
			value = IKeyword.FONT,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.TEXT, IConcept.DISPLAY },
			can_be_const = true)
	@doc (
			value = "Creates a new font, by specifying its name (either a font face name like 'Lucida Grande Bold' or 'Helvetica', or a logical name like 'Dialog', 'SansSerif', 'Serif', etc.), a size in points and a style, either #bold, #italic or #plain or a combination (addition) of them.",
			masterDoc = true,
			examples = @example (
					value = "font ('Helvetica Neue',12, #bold + #italic)",
					equals = "a bold and italic face of the Helvetica Neue family",
					test = false))
	@no_test
	public static IFont createFont(final String name, final Integer size, final Integer style) {
		return new GamaFont(name, style, size);
	}

}