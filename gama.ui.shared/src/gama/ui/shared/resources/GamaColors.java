/*******************************************************************************************************
 *
 * GamaColors.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.resources;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import gama.core.util.GamaColor;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * Class GamaIcons.
 *
 * @author drogoul
 * @since 12 sept. 2013
 *
 */
public class GamaColors {

	static {
		DEBUG.OFF();
	}

	/**
	 * The Class GamaUIColor.
	 */
	public static class GamaUIColor {

		/** The reverse. */
		Color active, inactive, darker, gray, reverse;

		/** The lighter colors. */
		Map<Float, Color> lighterColors;

		/**
		 * Instantiates a new gama UI color.
		 *
		 * @param c
		 *            the c
		 */
		public GamaUIColor(final Color c) {
			active = c;
		}

		/**
		 * Validate.
		 *
		 * @return the gama UI color
		 */
		public GamaUIColor validate() {
			return this;
		}

		@Override
		public String toString() {
			return active.getRed() + ", " + active.getGreen() + ", " + active.getBlue();
		}

		/**
		 * Checks if is dark.
		 *
		 * @return true, if is dark
		 */
		public boolean isDark() { return GamaColors.isDark(active); }

		/**
		 * Instantiates a new gama UI color.
		 *
		 * @param c
		 *            the c
		 * @param i
		 *            the i
		 */
		public GamaUIColor(final Color c, final Color i) {
			active = c;
			inactive = i;
		}

		/**
		 * Color.
		 *
		 * @return the color
		 */
		public Color color() {
			return active;
		}

		/**
		 * Inactive.
		 *
		 * @return the color
		 */
		public Color inactive() {
			if (inactive == null) { inactive = computeInactive(active); }
			return inactive;
		}

		/**
		 * Darker.
		 *
		 * @return the color
		 */
		public Color darker() {
			if (darker == null) { darker = computeDarker(active); }
			return darker;
		}

		/**
		 * Lighter.
		 *
		 * @return the color
		 */
		public Color lighter() {
			return lighter(0.2f);
		}

		/**
		 * Lighter.
		 *
		 * @param percentage
		 *            the percentage
		 * @return the color
		 */
		public Color lighter(final float percentage) {
			if (lighterColors == null) { lighterColors = new HashMap<>(); }
			Color lighter = lighterColors.get(percentage);
			if (lighter != null) return lighter;
			DEBUG.OUT("Computing new lighter color with " + percentage);
			lighter = computeLighter(active, percentage);
			lighterColors.put(percentage, lighter);
			return lighter;
		}

		/**
		 * Gets the rgb.
		 *
		 * @return the rgb
		 */
		public RGB getRGB() { return active.getRGB(); }

		/**
		 * Gama color.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the gama color
		 * @date 20 août 2023
		 */
		public GamaColor gamaColor() {
			return GamaColor.get(active.getRed(), active.getGreen(), active.getBlue(), active.getAlpha());
		}
	}

	/** The colors. */
	static HashMap<RGB, GamaUIColor> colors = new HashMap<>();

	/**
	 * Compute inactive.
	 *
	 * @param c
	 *            the c
	 * @return the color
	 */
	static Color computeInactive(final Color c) {
		final var data = c.getRGB();
		final var hsb = data.getHSB();
		final var newHsb = new float[3];
		newHsb[0] = hsb[0];
		newHsb[1] = hsb[1] / 2;
		newHsb[2] = Math.min(1.0f, hsb[2] + 0.2f);
		final var newData = new RGB(newHsb[0], newHsb[1], newHsb[2]);
		return getColor(newData.red, newData.green, newData.blue);
	}

	/**
	 * Compute darker.
	 *
	 * @param c
	 *            the c
	 * @return the color
	 */
	static Color computeDarker(final Color c) {
		final var data = c.getRGB();
		final var hsb = data.getHSB();
		final var newHsb = new float[3];
		newHsb[0] = hsb[0];
		newHsb[1] = hsb[1];
		newHsb[2] = Math.max(0.0f, hsb[2] - 0.1f);
		final var newData = new RGB(newHsb[0], newHsb[1], newHsb[2]);
		return getColor(newData.red, newData.green, newData.blue);
	}

	/**
	 * Compute reverse.
	 *
	 * @param c
	 *            the c
	 * @return the color
	 */
	static Color computeReverse(final Color c) {
		final var data = c.getRGB();
		return getColor(255 - data.red, 255 - data.green, 255 - data.blue);
	}

	/**
	 * Compute lighter.
	 *
	 * @param c
	 *            the c
	 * @return the color
	 */

	static Color computeLighter(final Color c, final float percentage) {
		final var data = c.getRGB();
		final var hsb = data.getHSB();
		final var newHsb = new float[3];
		newHsb[0] = hsb[0];
		newHsb[1] = hsb[1];
		newHsb[2] = Math.min(1f, hsb[2] + percentage);
		final var newData = new RGB(newHsb[0], newHsb[1], newHsb[2]);
		return getColor(newData.red, newData.green, newData.blue);
	}

	/**
	 * Compute gray.
	 *
	 * @param c
	 *            the c
	 * @return the color
	 */
	static Color computeGray(final Color c) {
		final var data = c.getRGB();
		final var hsb = data.getHSB();
		final var newHsb = new float[3];
		newHsb[0] = hsb[0];
		newHsb[1] = 0.0f;
		newHsb[2] = hsb[2];
		final var newData = new RGB(newHsb[0], newHsb[1], newHsb[2]);
		return getColor(newData.red, newData.green, newData.blue);
	}

	/**
	 * Gets the color.
	 *
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 * @return the color
	 */
	private static Color getColor(final int r, final int g, final int b) {
		return new Color(WorkbenchHelper.getDisplay(), r, g, b);
	}

	/**
	 * Gets the.
	 *
	 * @param color
	 *            the color
	 * @return the gama UI color
	 */
	public static GamaUIColor get(final java.awt.Color color) {
		if (color == null) return null;
		return get(color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * Gets the.
	 *
	 * @param rgb
	 *            the rgb
	 * @return the gama UI color
	 */
	public static GamaUIColor get(final RGB rgb) {
		if (rgb == null) return null;
		var c = colors.get(rgb);
		if (c == null) {
			final var cc = getColor(rgb.red, rgb.green, rgb.blue);
			c = new GamaUIColor(cc);
			colors.put(rgb, c);
		}
		return c;
	}

	/**
	 * Gets the.
	 *
	 * @param color
	 *            the color
	 * @return the gama UI color
	 */
	public static GamaUIColor get(final Color color) {
		if (color == null) return null;
		return get(color.getRGB());
	}

	/**
	 * Gets the.
	 *
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 * @return the gama UI color
	 */
	public static GamaUIColor get(final int r, final int g, final int b) {
		final var r1 = r < 0 ? 0 : r > 255 ? 255 : r;
		final var g1 = g < 0 ? 0 : g > 255 ? 255 : g;
		final var b1 = b < 0 ? 0 : b > 255 ? 255 : b;
		final var rgb = new RGB(r1, g1, b1);
		return get(rgb);
	}

	/**
	 * System.
	 *
	 * @param c
	 *            the c
	 * @return the color
	 */
	public static Color system(final int c) {
		return WorkbenchHelper.getDisplay().getSystemColor(c);
	}

	/**
	 * Gets the.
	 *
	 * @param c
	 *            the c
	 * @return the gama UI color
	 */
	public static GamaUIColor get(final int... c) {
		if (c.length >= 3) return get(c[0], c[1], c[2]);
		final var rgb = c[0];
		final var red = rgb >> 16 & 0xFF;
		final var green = rgb >> 8 & 0xFF;
		final var blue = rgb & 0xFF;
		return get(red, green, blue);
	}

	/**
	 * Get the color of the icon passed in parameter (supposing it's mono-colored)
	 *
	 * @param create
	 * @return
	 */
	public static GamaUIColor get(final GamaIcon icon) {
		final var image = icon.image();
		final var data = image.getImageData();
		final var palette = data.palette;
		final var pixelValue = data.getPixel(0, 0);
		return get(palette.getRGB(pixelValue));
	}

	/**
	 * @param background
	 * @return
	 */
	public static boolean isDark(final Color color) {
		return luminanceOf(color) < 130;
	}

	/**
	 * Luminance of.
	 *
	 * @param color
	 *            the color
	 * @return the int
	 */
	public static int luminanceOf(final Color color) {
		return (int) (0.299 * color.getRed() * color.getRed() / 255 + 0.587 * color.getGreen() * color.getGreen() / 255
				+ 0.114 * color.getBlue() * color.getBlue() / 255); // http://alienryderflex.com/hsp.html
	}

	/**
	 * To awt color.
	 *
	 * @param color
	 *            the color
	 * @return the java.awt. color
	 */
	public static java.awt.Color toAwtColor(final Color color) {
		return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * To swt color.
	 *
	 * @param color
	 *            the color
	 * @return the color
	 */
	public static Color toSwtColor(final java.awt.Color color) {
		if (color == null) return toSwtColor(java.awt.Color.BLACK);
		return new Color(color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * To gama color.
	 *
	 * @param color
	 *            the color
	 * @return the gama color
	 */
	public static GamaColor toGamaColor(final Color color) {
		return GamaColor.get(color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * To gama color.
	 *
	 * @param color
	 *            the color
	 * @return the gama color
	 */
	public static GamaColor toGamaColor(final RGB color) {
		if (color == null) return GamaColor.get(0);
		return GamaColor.get(color.red, color.green, color.blue);
	}

	/**
	 * @param background
	 * @return
	 */
	public static GamaUIColor getTextColorForBackground(final Color background) {
		return isDark(background) ? IGamaColors.WHITE : IGamaColors.BLACK;
	}

	/**
	 * Gets the text color for background.
	 *
	 * @param background
	 *            the background
	 * @return the text color for background
	 */
	public static GamaUIColor getTextColorForBackground(final GamaUIColor background) {
		return getTextColorForBackground(background.color());
	}

	/**
	 * Gets the background CSS property.
	 *
	 * @param c
	 *            the c
	 * @return the background CSS property
	 */
	public static String getCSSProperty(final String prop, final GamaUIColor c) {
		return ThemeHelper.getCSSProperty(prop, c.color());
	}

	/**
	 * Sets the background.
	 *
	 * @param w
	 *            the w
	 * @param c
	 *            the c
	 */
	public static void setBackground(final Color c, final Control... controls) {
		String prop = ThemeHelper.getCSSProperty("background-color", c);
		for (Control w : controls) {
			if (w == null) { continue; }
			w.setBackground(c);
			w.setData("style", c == null ? null : prop);
		}
	}

	/**
	 * Sets the foreground.
	 *
	 * @param w
	 *            the w
	 * @param c
	 *            the c
	 */
	public static void setForeground(final Color c, final Control... controls) {
		String prop = ThemeHelper.getCSSProperty("color", c);
		for (Control w : controls) {
			if (w == null) { continue; }
			w.setForeground(c);
			w.setData("style", c == null ? null : prop);
		}
	}

	/**
	 * Sets the back and foreground.
	 *
	 * @param w
	 *            the w
	 * @param b
	 *            the b
	 * @param f
	 *            the f
	 */
	public static void setBackAndForeground(final Color b, final Color f, final Control... controls) {
		for (Control w : controls) {
			if (w == null) { continue; }
			if (b == null) {
				if (f == null) return;
				setForeground(f, w);
			} else if (f == null) {
				setBackground(b, w);
			} else {
				w.setBackground(b);
				w.setForeground(f);
				w.setData("style",
						ThemeHelper.getCSSProperty("background-color", b) + ThemeHelper.getCSSProperty("color", f));
			}
		}
	}

}
