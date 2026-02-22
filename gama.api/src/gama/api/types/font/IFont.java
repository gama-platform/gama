/*******************************************************************************************************
 *
 * IFont.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.font;

import java.awt.Font;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.constants.IKeyword;
import gama.api.gaml.types.IType;
import gama.api.types.misc.IValue;

/**
 * Interface for font values in GAMA representing typefaces used in displays, outputs, and visualizations.
 * 
 * <p>An {@code IFont} encapsulates font information including the font family name, size in points, and style
 * (plain, bold, italic, or combinations). Fonts are value objects that can be serialized to GAML, compared,
 * and manipulated through various operators.</p>
 * 
 * <p>This interface extends {@link IValue} to integrate with GAMA's type system, providing serialization,
 * type information, and scope-aware operations.</p>
 * 
 * <h2>Font Properties</h2>
 * <p>Fonts have three primary properties accessible via getters and GAML attributes:</p>
 * <ul>
 *   <li><b>name</b> - The font family name (e.g., "Helvetica", "Arial", "SansSerif")</li>
 *   <li><b>size</b> - The font size in points (e.g., 12, 14, 18)</li>
 *   <li><b>style</b> - The font style as an integer: 0=plain, 1=bold, 2=italic, 3=bold+italic</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>GAML Usage</h3>
 * <pre>
 * // Creating fonts
 * font my_font &lt;- font("Helvetica", 14);
 * font bold_font &lt;- font("Arial", 16, #bold);
 * 
 * // Accessing properties
 * string font_name &lt;- my_font.name;
 * int font_size &lt;- my_font.size;
 * int font_style &lt;- my_font.style;
 * 
 * // Using in displays
 * draw "Text" font: my_font at: {10, 10};
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * <pre>
 * IFont font = GamaFontFactory.createFont("Times New Roman", 12, Font.BOLD);
 * String name = font.getName();
 * int size = font.getSize();
 * Font awtFont = font.getAwtFont();  // Get underlying AWT font for rendering
 * </pre>
 * 
 * <h2>Immutability</h2>
 * <p>Font instances are effectively immutable. Operations that appear to modify a font (like {@code with_size} or
 * {@code with_style}) actually create and return new font instances.</p>
 * 
 * <h2>Thread Safety</h2>
 * <p>Due to immutability, {@code IFont} instances are inherently thread-safe and can be freely shared across
 * threads without synchronization.</p>
 * 
 * @author drogoul
 * @see gama.api.types.font.GamaFont
 * @see gama.api.types.font.GamaFontFactory
 * @see java.awt.Font
 * @since GAMA 1.7
 */
@vars ({ @variable (
		name = IKeyword.NAME,
		type = IType.STRING,
		doc = { @doc ("Returns the name of this font") }),
		@variable (
				name = IKeyword.SIZE,
				type = IType.INT,
				doc = { @doc ("Returns the size (in points) of this font") }),
		@variable (
				name = IKeyword.STYLE,
				type = IType.INT,
				doc = { @doc ("Returns the style of this font (0 for plain, 1 for bold, 2 for italic, 3 for bold+italic)") }) })

public interface IFont extends IValue {

	/**
	 * Returns the font family name.
	 * 
	 * <p>This can be either a physical font name (e.g., "Helvetica Neue", "Arial Black") or a logical
	 * font name (e.g., "Dialog", "SansSerif", "Serif", "Monospaced").</p>
	 *
	 * @return the font family name as a string
	 * @see java.awt.Font#getName()
	 */
	@getter (IKeyword.NAME)
	String getName();

	/**
	 * Returns the font size in points.
	 * 
	 * <p>The point size determines the height of the font characters. Common sizes include 10, 12, 14, 16, etc.
	 * The actual pixel size depends on screen resolution (typically 72 or 96 DPI).</p>
	 *
	 * @return the font size in points (always positive)
	 * @see java.awt.Font#getSize()
	 */
	@getter (IKeyword.SIZE)
	int getSize();

	/**
	 * Returns the font style as an integer code.
	 * 
	 * <p>The style is represented using the same constants as {@link java.awt.Font}:</p>
	 * <ul>
	 *   <li>0 ({@link java.awt.Font#PLAIN}) - Normal weight, upright</li>
	 *   <li>1 ({@link java.awt.Font#BOLD}) - Bold weight</li>
	 *   <li>2 ({@link java.awt.Font#ITALIC}) - Italic/oblique style</li>
	 *   <li>3 ({@link java.awt.Font#BOLD} + {@link java.awt.Font#ITALIC}) - Bold and italic</li>
	 * </ul>
	 *
	 * @return the style code (0-3)
	 * @see java.awt.Font#getStyle()
	 */
	@getter (IKeyword.STYLE)
	int getStyle();

	/**
	 * Returns the underlying AWT {@link Font} object for rendering.
	 * 
	 * <p>This method provides access to the native Java font object which can be used directly with
	 * AWT/Swing graphics contexts for text rendering.</p>
	 * 
	 * <p><b>Note:</b> Modifications to the returned Font object (if mutable) will not affect this IFont
	 * instance, as IFont instances are immutable.</p>
	 *
	 * @return the AWT Font object
	 * @see java.awt.Font
	 */
	Font getAwtFont();

	/**
	 * Returns the platform-specific font name.
	 * 
	 * <p>This may return a more specific font name than {@link #getName()}, including the font's actual
	 * file name or the specific variant being used (e.g., "HelveticaNeue-Bold" instead of "Helvetica Neue").</p>
	 * 
	 * <p>This is useful for font debugging and for ensuring the exact font variant is identified.</p>
	 *
	 * @return the platform-specific font name
	 * @see java.awt.Font#getFontName()
	 */
	String getFontName();

}