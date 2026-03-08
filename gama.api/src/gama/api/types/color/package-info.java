/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
/**
 * Provides color management and manipulation utilities for the GAMA modeling and simulation platform.
 * <p>
 * This package contains the core interfaces and implementations for working with colors in GAMA. It provides:
 * </p>
 * <ul>
 * <li><strong>Color Interface:</strong> {@link gama.api.types.color.IColor} defines the contract for color objects,
 * including RGB component access, alpha channel manipulation, color comparison methods, and color transformation
 * operations (brighter/darker).</li>
 * <li><strong>Color Implementation:</strong> {@link gama.api.types.color.GamaColor} is the internal implementation of
 * the {@link gama.api.types.color.IColor} interface, wrapping Java AWT Color objects and providing GAML-specific
 * serialization and comparison capabilities.</li>
 * <li><strong>Color Factory:</strong> {@link gama.api.types.color.GamaColorFactory} serves as the central factory for
 * creating and managing color instances. It maintains registries of named colors (CSS color names) and integer-encoded
 * colors for efficient reuse, and provides comprehensive conversion methods from various types (strings, lists, maps,
 * integers, AWT colors) to {@link gama.api.types.color.IColor} instances.</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><strong>Named Colors:</strong> Support for CSS color names (e.g., "red", "blue", "lightgray") through a
 * centralized registry.</li>
 * <li><strong>Color Caching:</strong> Efficient color instance reuse through internal registries indexed by RGB
 * values.</li>
 * <li><strong>Flexible Creation:</strong> Multiple factory methods supporting various input formats (RGB components,
 * RGB with alpha, double precision values, AWT Color objects, CSS names, hex strings, etc.).</li>
 * <li><strong>Color Comparison:</strong> Multiple comparison strategies including RGB comparison, luminescence, brightness, and luma.</li>
 * <li><strong>Alpha Channel Support:</strong> Full support for transparency with methods to create colors with specific
 * alpha values or modify existing colors' transparency.</li>
 * <li><strong>Color Transformations:</strong> Built-in support for creating brighter and darker variants of colors.</li>
 * <li><strong>GAML Integration:</strong> Seamless integration with GAML type system, including serialization to GAML
 * syntax and JSON.</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Create colors using factory methods
 * IColor red = GamaColorFactory.get("red");
 * IColor customColor = GamaColorFactory.get(120, 50, 200); // RGB
 * IColor transparent = GamaColorFactory.createWithRGBA(255, 0, 0, 128); // RGB + Alpha
 * 
 * // Access color components
 * int redValue = customColor.red();
 * int alpha = customColor.alpha();
 * 
 * // Transform colors
 * IColor lighter = customColor.brighter();
 * IColor darker = customColor.darker();
 * IColor semiTransparent = customColor.withAlpha(0.5);
 * 
 * // Compare colors
 * int comparison = color1.compareLuminescenceTo(color2);
 * </pre>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * The color registry (INT_REGISTRY) in {@link gama.api.types.color.GamaColorFactory} is thread-safe. Individual color
 * instances are immutable after creation, making them safe to use across multiple threads.
 * </p>
 * 
 * @author GAMA Development Team
 * @version 2025-03
 * @since 1.0
 */
package gama.api.types.color;
