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
 *
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
	 * Gets the name.
	 *
	 * @return the name
	 */
	@getter (IKeyword.NAME)
	String getName();

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	@getter (IKeyword.SIZE)
	int getSize();

	/**
	 * Gets the style.
	 *
	 * @return the style
	 */
	@getter (IKeyword.STYLE)
	int getStyle();

	/**
	 * @return
	 */
	Font getAwtFont();

	/**
	 * @return
	 */
	String getFontName();

}