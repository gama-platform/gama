/*******************************************************************************************************
 *
 * JsonEditorColorConstants.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import org.eclipse.swt.graphics.RGB;

/**
 * The Interface JsonEditorColorConstants.
 */
public interface JsonEditorColorConstants {

	/** The Constant GRAY_JAVA. */
	RGB GRAY_JAVA = rgb(192, 192, 192);

	/** The Constant GREEN_JAVA. */
	RGB GREEN_JAVA = rgb(63, 127, 95);

	/** The Constant LINK_DEFAULT_BLUE. */
	RGB LINK_DEFAULT_BLUE = rgb(63, 63, 191);

	/** The Constant KEYWORD_DEFAULT_PURPLE. */
	RGB KEYWORD_DEFAULT_PURPLE = rgb(127, 0, 85);

	/** The Constant STRING_DEFAULT_BLUE. */
	/*
	 * same as java default string in eclipse
	 */
	RGB STRING_DEFAULT_BLUE = rgb(42, 0, 255);

	/** The Constant ROYALBLUE. */
	/* royal blue - http://www.rapidtables.com/web/color/blue-color.htm */
	RGB ROYALBLUE = rgb(65, 105, 225);

	/** The Constant STEELBLUE. */
	/* steel blue - http://www.rapidtables.com/web/color/blue-color.htm */
	RGB STEELBLUE = rgb(70, 130, 180);

	/** The Constant CADET_BLUE. */
	/* cadetblue - http://www.rapidtables.com/web/color/blue-color.htm */
	RGB CADET_BLUE = rgb(95, 158, 160);

	/** The Constant OUTLINE_ITEM__TYPE. */
	RGB OUTLINE_ITEM__TYPE = rgb(149, 125, 71); // same
												// as
												// java
												// outline
	/** The Constant MIDDLE_GRAY. */
	// string
	RGB MIDDLE_GRAY = rgb(128, 128, 128);

	/** The Constant MIDDLE_GREEN. */
	RGB MIDDLE_GREEN = rgb(0, 128, 0);

	/** The Constant MIDDLE_BROWN. */
	RGB MIDDLE_BROWN = rgb(128, 128, 0);

	/** The Constant MIDDLE_RED. */
	RGB MIDDLE_RED = rgb(128, 0, 0);

	/** The Constant MIDDLE_ORANGE. */
	RGB MIDDLE_ORANGE = rgb(255, 128, 64);

	/** The Constant DARK_GREEN. */
	RGB DARK_GREEN = rgb(0, 64, 0);

	/** The Constant TASK_DEFAULT_RED. */
	RGB TASK_DEFAULT_RED = rgb(128, 0, 0);

	/** The Constant BLACK. */
	RGB BLACK = rgb(0, 0, 0);

	/** The Constant RED. */
	RGB RED = rgb(170, 0, 0);

	/** The Constant GREEN. */
	RGB GREEN = rgb(0, 170, 0);

	/** The Constant BROWN. */
	RGB BROWN = rgb(170, 85, 0);

	/** The Constant BLUE. */
	RGB BLUE = rgb(0, 0, 170);

	/** The Constant MAGENTA. */
	RGB MAGENTA = rgb(170, 0, 170);

	/** The Constant CYANN. */
	RGB CYANN = rgb(0, 170, 170);

	/** The Constant GRAY. */
	RGB GRAY = rgb(170, 170, 170);

	/** The Constant DARK_THEME_GRAY. */
	RGB DARK_THEME_GRAY = rgb(97, 97, 97);

	/** The Constant DARK_GRAY. */
	RGB DARK_GRAY = rgb(85, 85, 85);

	/** The Constant BRIGHT_RED. */
	RGB BRIGHT_RED = rgb(255, 85, 85);

	/** The Constant BRIGHT_GREEN. */
	RGB BRIGHT_GREEN = rgb(85, 255, 85);

	/** The Constant YELLOW. */
	RGB YELLOW = rgb(255, 255, 85);

	/** The Constant ORANGE. */
	RGB ORANGE = rgb(255, 165, 0); // http://www.rapidtables.com/web/color/orange-color.htm

	/** The Constant BRIGHT_BLUE. */
	RGB BRIGHT_BLUE = rgb(85, 85, 255);

	/** The Constant MEDIUM_CYAN. */
	RGB MEDIUM_CYAN = rgb(0, 128, 192);

	/** The Constant DARK_BLUE. */
	RGB DARK_BLUE = rgb(0, 64, 128);

	/** The Constant BRIGHT_MAGENTA. */
	RGB BRIGHT_MAGENTA = rgb(255, 85, 255);

	/** The Constant BRIGHT_CYAN. */
	RGB BRIGHT_CYAN = rgb(85, 255, 255);

	/** The Constant WHITE. */
	RGB WHITE = rgb(255, 255, 255);

	/** The Constant TASK_CYAN. */
	RGB TASK_CYAN = rgb(0, 128, 128);

	/**
	 * A special dark cyan color for echo outputs on dark themes
	 */
	RGB DARK_THEME_ECHO_OUTPUT = rgb(49, 98, 98);

	/** The Constant DARK_THEME_LIGHT_BLUE. */
	RGB DARK_THEME_LIGHT_BLUE = rgb(114, 159, 207);

	/** The Constant DARK_THEME_LIGHT_ORANGE. */
	RGB DARK_THEME_LIGHT_ORANGE = rgb(233, 185, 110);

	/**
	 * Rgb.
	 *
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 * @return the rgb
	 */
	static RGB rgb(final int r, final int g, final int b) {
		return new RGB(r, g, b);
	}
}
