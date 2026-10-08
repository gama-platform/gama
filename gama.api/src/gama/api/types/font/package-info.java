/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
/**
 * Provides types and factory methods for working with fonts in GAMA.
 * 
 * <p>This package contains the core abstractions for representing typefaces used in GAMA displays and outputs.
 * Fonts are value objects that wrap AWT {@link java.awt.Font} instances with GAMA-specific type information,
 * serialization support, and operator integration.</p>
 * 
 * <h2>Core Types</h2>
 * <ul>
 *   <li>{@link gama.api.types.font.IFont} - The primary interface for font values in GAMA</li>
 *   <li>{@link gama.api.types.font.GamaFont} - The standard immutable implementation wrapping AWT fonts</li>
 *   <li>{@link gama.api.types.font.GamaFontFactory} - Factory and operators for creating font instances</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Creating fonts in GAML</h3>
 * <pre>
 * // Create a font with name and size (plain style)
 * font my_font &lt;- font("Helvetica", 14);
 * 
 * // Create a font with name, size, and style
 * font bold_italic &lt;- font("Arial", 16, #bold + #italic);
 * 
 * // Modify an existing font
 * font larger &lt;- my_font with_size 20;
 * font italic_version &lt;- my_font with_style #italic;
 * </pre>
 * 
 * <h3>Using fonts in Java</h3>
 * <pre>
 * // Create fonts programmatically
 * IFont defaultFont = GamaFontFactory.getDefaultFont();
 * IFont customFont = GamaFontFactory.createFont("Times New Roman", 12, Font.BOLD);
 * 
 * // Access font properties
 * String name = customFont.getName();
 * int size = customFont.getSize();
 * int style = customFont.getStyle();
 * 
 * // Get the underlying AWT font for rendering
 * Font awtFont = customFont.getAwtFont();
 * </pre>
 * 
 * <h2>Font Styles</h2>
 * <p>Font styles are represented as integer constants following AWT conventions:</p>
 * <ul>
 *   <li><b>0 (PLAIN)</b> - Normal weight and upright style (GAML: {@code #plain})</li>
 *   <li><b>1 (BOLD)</b> - Bold weight (GAML: {@code #bold})</li>
 *   <li><b>2 (ITALIC)</b> - Italic/oblique style (GAML: {@code #italic})</li>
 *   <li><b>3 (BOLD+ITALIC)</b> - Combination of bold and italic (GAML: {@code #bold + #italic})</li>
 * </ul>
 * 
 * <h2>Font Names</h2>
 * <p>Font names can be either:</p>
 * <ul>
 *   <li><b>Physical font names</b> - Actual font family names like "Helvetica Neue", "Arial", "Times New Roman"</li>
 *   <li><b>Logical font names</b> - Platform-independent names like "Dialog", "SansSerif", "Serif", "Monospaced"</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>{@link gama.api.types.font.GamaFont} instances are effectively immutable once created. All modification operations
 * ({@code with_size}, {@code with_style}) return new instances rather than modifying existing ones, making them safe
 * to share across threads.</p>
 * 
 * <h2>Serialization</h2>
 * <p>Fonts can be serialized to GAML using {@link gama.api.types.font.IFont#serializeToGaml(boolean)} which produces
 * valid GAML syntax that can be parsed back into a font object:</p>
 * <pre>
 * font('Helvetica',12,#bold)
 * </pre>
 * 
 * @see gama.api.types.font.IFont
 * @see gama.api.types.font.GamaFontFactory
 * @see java.awt.Font
 * @since GAMA 1.7
 */
package gama.api.types.font;
