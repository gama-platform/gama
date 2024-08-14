/*******************************************************************************************************
 *
 * IConstantCategory.java, in gama.annotations, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.annotations.precompiler;

/**
 * Written by gaudou Modified on 1 mars 2014
 * 
 * Description: all the possible categories for operators
 * 
 */
public interface IConstantCategory {

	/** The Constant PREFIX_CONSTANT. */
	public static final String PREFIX_CONSTANT = "#";	
	
	
	/** The Constant LENGTH. */
	// Units
	public static final String LENGTH		= "Length units";
	
	/** The Constant TIME. */
	public static final String TIME			= "Time units";
	
	/** The Constant VOLUME. */
	public static final String VOLUME		= "Volume units";	
	
	/** The Constant WEIGHT. */
	public static final String WEIGHT		= "Weight units";
	
	/** The Constant SURFACE. */
	public static final String SURFACE		= "Surface units";
	
	/** The Constant GRAPHIC. */
	public static final String GRAPHIC		= "Graphics units";	
	
	/** The Constant CONSTANT. */
	// Constants
	public static final String CONSTANT		= "Constants";

	/** The Constant MATH. */
	// Math
	public static final String MATH			= "Math constants";
	
	/** The Constant COLOR_CSS. */
	public static final String COLOR_CSS 	= "Colors";
	
}
