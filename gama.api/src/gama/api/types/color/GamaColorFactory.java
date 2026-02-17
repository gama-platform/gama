/*******************************************************************************************************
 *
 * GamaColorFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.color;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.annotations.constants.ColorCSS;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IContainer;

/**
 * A static factory for creating and managing {@link IColor} instances. This class serves as a central point for
 * accessing color creation logic, predefined colors, and color conversion utilities. It delegates the actual creation
 * to an {@link IColorFactory} implementation.
 */
public class GamaColorFactory {

	/**
	 * A map storing named colors accessible by their lowercase names.
	 */
	public static Map<String, IColor> NAME_REGISTRY = new HashMap<>();

	/**
	 * A thread-safe map storing colors indexed by their integer RGB value.
	 */
	static Map<Integer, IColor> INT_REGISTRY = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Common color constants used throughout the platform.
	 */
	public static IColor BLACK, WHITE, RED, GREEN, BLUE, YELLOW, LIGHT_GRAY, GRAY;

	/**
	 * The Class NamedGamaColor.
	 */
	public static class NamedGamaColor extends GamaColor {

		/**
		 * Instantiates a new named gama color.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param name
		 *            the name.
		 * @param rgba
		 *            the rgba
		 * @date 20 août 2023
		 */
		NamedGamaColor(final String name, final int... rgba) {
			super(rgba[0], rgba[1], rgba[2], rgba[3]);
			this.name = name;
		}

		/** The name. */
		final String name;

		@Override
		public String toString() {
			return "color[" + name + "]";
		}

		@Override
		public String serializeToGaml(final boolean includingBuiltIn) {
			return "#" + name;
		}

		@Override
		public String stringValue(final IScope scope) {
			return name;
		}

	}

	/**
	 * Updates the internal factory builder and initializes default colors.
	 *
	 * @param builder
	 *            the {@link IColorFactory} to be used as the internal builder.
	 */
	static {
		for (int i = 0; i < ColorCSS.array.length; i += 2) {
			String name = (String) ColorCSS.array[i];
			int[] rgba = (int[]) ColorCSS.array[i + 1];
			register(name, rgba[0], rgba[1], rgba[2], rgba[3]);
		}
		GRAY = get("gray");
		LIGHT_GRAY = get("lightgray");
		YELLOW = get("yellow");
		BLUE = get("blue");
		GREEN = get("green");
		RED = get("red");
		WHITE = get("white");
		BLACK = get("black");
	}

	/**
	 * Retrieves a color from its integer RGB encoding.
	 *
	 * @param rgb
	 *            the integer representation of the color (including alpha).
	 * @return the corresponding {@link IColor} instance.
	 */

	public static IColor get(final int rgba) {
		IColor result = INT_REGISTRY.get(rgba);
		if (result == null) {
			result = new GamaColor(rgba);
			INT_REGISTRY.put(rgba, result);
		}
		return result;
	}

	/**
	 * Creates a color from an RGB value and a specific alpha (transparency).
	 *
	 * @param rgb
	 *            the integer representation of the color (ignoring its alpha).
	 * @param alpha
	 *            the alpha component (0-255).
	 * @return the new {@link IColor} instance.
	 */
	public static IColor createWithAlpha(final int rgb, final int alpha) {
		IColor c = get(rgb);
		return createWithRGBA(c.red(), c.green(), c.blue(), alpha);
	}

	/**
	 * Creates a color from a CSS color name and a specific alpha (transparency).
	 *
	 * @param c
	 *            the CSS color name.
	 * @param alpha
	 *            the alpha component (0-255).
	 * @return the new {@link IColor} instance.
	 */
	public static IColor createWithAlpha(final String c, final int alpha) {
		IColor color = get(c);
		if (color == null) { color = BLACK; }
		return color.withAlpha(alpha / 255d);
	}

	/**
	 * Retrieves or creates a color from red, green, and blue components.
	 *
	 * @param r
	 *            the red component (0-255).
	 * @param g
	 *            the green component (0-255).
	 * @param b
	 *            the blue component (0-255).
	 * @return the corresponding {@link IColor} instance.
	 */
	public static IColor get(final int r, final int g, final int b) {
		return createWithRGBA(r, g, b, 0);
	}

	/**
	 * Retrieves or creates a color from red, green, blue, and alpha components.
	 *
	 * @param r
	 *            the red component (0-255).
	 * @param g
	 *            the green component (0-255).
	 * @param b
	 *            the blue component (0-255).
	 * @param a
	 *            the alpha component (0-255).
	 * @return the corresponding {@link IColor} instance.
	 */
	public static IColor createWithRGBA(final int r, final int g, final int b, final int a) {
		// rgb in 3 components + alpha
		return get((normalize(a) & 0xFF) << 24 | (normalize(r) & 0xFF) << 16 | (normalize(g) & 0xFF) << 8
				| (normalize(b) & 0xFF) << 0);
	}

	/**
	 * Retrieves or creates a color from integer RGB components and a double alpha value.
	 *
	 * @param r
	 *            the red component (0-255).
	 * @param g
	 *            the green component (0-255).
	 * @param b
	 *            the blue component (0-255).
	 * @param t
	 *            the alpha component as a double (0.0 usually means transparent, 1.0 opaque). The exact behavior
	 *            depends on implementation interpretation (often normalized).
	 * @return the corresponding {@link IColor} instance.
	 */
	public static IColor createWithDoubleAlpha(final int r, final int g, final int b, final double t) {
		return createWithRGBA(r, g, b, normalize(t));
	}

	/**
	 * Retrieves or creates a color using double precision values for all components.
	 *
	 * @param r
	 *            the red component (0.0-1.0 or implementation specific range).
	 * @param g
	 *            the green component.
	 * @param b
	 *            the blue component.
	 * @param t
	 *            the alpha component.
	 * @return the corresponding {@link IColor} instance.
	 */
	public static IColor getWithDoubles(final double r, final double g, final double b, final double t) {
		return createWithRGBA(normalize(r), normalize(g), normalize(b), normalize(t));
	}

	/**
	 * Creates a new color from an existing AWT {@link Color} and a double alpha value.
	 *
	 * @param c
	 *            the source AWT color.
	 * @param t
	 *            the new alpha value as a double.
	 * @return the new {@link IColor} instance.
	 */
	public static IColor createWithAlpha(final Color c, final double t) {
		return createWithRGBA(c.getRed(), c.getGreen(), c.getBlue(), normalize(t));
	}

	/**
	 * Creates a new color from an existing AWT {@link Color} and an integer alpha value.
	 *
	 * @param c
	 *            the source AWT color.
	 * @param t
	 *            the new alpha value (0-255).
	 * @return the new {@link IColor} instance.
	 */
	public static IColor createWithAlpha(final Color c, final int t) {
		return createWithRGBA(c.getRed(), c.getGreen(), c.getBlue(), t);
	}

	/**
	 * Creates a new {@link IColor} based on an existing one but with a modified alpha value.
	 *
	 * @param c
	 *            the source color.
	 * @param t
	 *            the new alpha value (0-255).
	 * @return a new {@link IColor} instance with the specified transparency.
	 */
	public static IColor createWithAlpha(final IColor c, final int t) {
		return c.withAlpha(t / 255d);
	}

	/**
	 * Creates a new {@link IColor} based on an existing one but with a modified alpha value.
	 *
	 * @param c
	 *            the source color.
	 * @param t
	 *            the new alpha value as a double.
	 * @return a new {@link IColor} instance with the specified transparency.
	 */
	public static IColor createWithAlpha(final IColor c, final double t) {
		return c.withAlpha(t);
	}

	/**
	 * Retrieves or creates an {@link IColor} from an AWT {@link Color}.
	 *
	 * @param c
	 *            the AWT color to wrap or copy.
	 * @return the corresponding {@link IColor} instance.
	 */
	public static IColor createFromAWTColor(final Color c) {
		return get(c.getRGB());
	}

	/**
	 * Retrieves a named color. Returns an IColor corresponding to a named color or a string representation of a color.
	 * Examples: 'red', '#FF0000', 'rgb(255,0,0)'.
	 *
	 * @param rgb
	 *            the name of the color (e.g., "red", "blue").
	 * @return the corresponding {@link IColor} instance, or null if not found.
	 */

	public static IColor get(final String rgb) {
		return NAME_REGISTRY.get(rgb);
	}

	/**
	 * Register or retrieve a color with a given name and optional component values.
	 *
	 * @param name
	 *            the name of the color.
	 * @param t
	 *            optional integer components (e.g. R, G, B, A).
	 * @return the {@link IColor} instance.
	 */
	private static IColor register(final String name, final int r, final int g, final int b, final int a) {
		IColor c = NAME_REGISTRY.get(name);
		if (c == null) {
			c = new NamedGamaColor(name, normalize(r), normalize(g), normalize(b), normalize(a));
			NAME_REGISTRY.put(name, c);
			INT_REGISTRY.put(c.getRGB(), c);
		}
		return c;
	}

	/**
	 * Converts an arbitrary object into an {@link IColor}.
	 *
	 * @param scope
	 *            the current execution scope.
	 * @param obj
	 *            the object to convert (can be a Color, List, Map, String, etc.).
	 * @param param
	 *            an optional parameter (e.g., for alpha value modification).
	 * @param copy
	 *            whether to create a copy if the object is already a color.
	 * @return the resulting {@link IColor} instance, or null if conversion fails.
	 * @throws GamaRuntimeException
	 *             if the object cannot be converted to a valid color.
	 */
	public static IColor castToColor(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		// param can contain the alpha value
		switch (obj) {
			case null -> {
				return null;
			}
			case IColor col -> {
				if (param instanceof Integer a) return createWithRGBA(col.red(), col.green(), col.blue(), a);
				if (param instanceof Double a) return createWithDoubleAlpha(col.red(), col.green(), col.blue(), a);
				return (IColor) obj;
			}
			case List l -> {
				final int size = l.size();
				return switch (size) {
					case 0 -> BLACK;
					case 1, 2 -> castToColor(scope, ((List) obj).get(0), param, copy);
					case 3 -> createWithRGBA(Cast.asInt(scope, l.get(0)), Cast.asInt(scope, l.get(1)),
							Cast.asInt(scope, l.get(2)), 255);
					default -> createWithRGBA(Cast.asInt(scope, l.get(0)), Cast.asInt(scope, l.get(1)),
							Cast.asInt(scope, l.get(2)), Cast.asInt(scope, l.get(3)));
				};
			}
			case Map m -> {
				return createWithRGBA(Cast.asInt(scope, m.get("red")), Cast.asInt(scope, m.get("green")),
						Cast.asInt(scope, m.get("blue")), Cast.asInt(scope, m.get("alpha")));
			}
			case IContainer<?, ?> c -> {
				return castToColor(scope, c.listValue(scope, Types.NO_TYPE, false), param, copy);
			}
			case String str -> {
				final String s = str.toLowerCase();
				IColor c = NAME_REGISTRY.get(s);
				if (c == null) {
					try {
						c = createFromAWTColor(Color.decode(s));
					} catch (final NumberFormatException e) {
						c = null;
						if (s != null && s.contains("rgb")) {
							String sClean = s.replace(" ", "").replace("rgb", "").replace("(", "").replace(")", "");
							String[] sval = sClean.split(",");
							if (sval.length >= 3) {
								Integer r = Integer.valueOf(sval[0]);
								Integer g = Integer.valueOf(sval[1]);
								Integer b = Integer.valueOf(sval[2]);
								Integer alpha = sval.length == 4 ? Integer.valueOf(sval[3]) : null;
								if (r != null && b != null && g != null) {
									c = createWithRGBA(r, g, b, alpha == null ? 255 : alpha);
								}
							}
						}
						if (c == null) throw GamaRuntimeException.error("'" + s + "' is not a valid color name", scope);
					}
					NAME_REGISTRY.put(s, c);
				}
				switch (param) {
					case Integer i -> {
						return createWithAlpha(c, i);
					}
					case Double d -> {
						return createWithAlpha(c, d);
					}
					case null, default -> {
						return c;
					}
				}

			}
			case Boolean cond -> {
				return cond ? createFromAWTColor(Color.black) : createFromAWTColor(Color.white);
			}
			default -> {
				final int i = Cast.asInt(scope, obj);
				if (param instanceof Integer in) return createWithAlpha(i, in);
				if (param instanceof Double d) return createWithAlpha(i, Double.valueOf(d * 255).intValue());
				return get(i);
			}
		}
	}

	/**
	 * Convenience method to create a color from an object within a scope.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param value
	 *            the value to convert to a color.
	 * @return the created {@link IColor} instance.
	 */
	public static IColor castToColor(final IScope scope, final Object value) {
		return castToColor(scope, value, null, false);
	}

	/**
	 * Creates a new GamaColor object.
	 *
	 * @param c1
	 *            the c 1
	 * @param c2
	 *            the c 2
	 * @return the i color
	 */
	public static IColor createByMerging(final IColor c1, final IColor c2) {
		int r = (c1.red() + c2.red()) / 2;
		int g = (c1.green() + c2.green()) / 2;
		int b = (c1.blue() + c2.blue()) / 2;
		int a = (c1.alpha() + c2.alpha()) / 2;
		return createWithRGBA(r, g, b, a);
	}

	/**
	 * Normalize.
	 *
	 * @param number
	 *            the transp
	 * @return the int
	 */
	// returns a value between 0 and 255 from a double between 0 and 1
	private static int normalize(final double number) {
		return (int) (number < 0 ? 0 : number > 1 ? 255 : 255 * number);
	}

	/**
	 * Normalize.
	 *
	 * @param number
	 *            the rgb comp
	 * @return the int
	 */
	private static int normalize(final int number) {
		return number < 0 ? 0 : number > 255 ? 255 : number;
	}

}