/*******************************************************************************************************
 *
 * TraversalMode.java, in gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

package com.bulletphysics.collision.shapes;

/**
 * Traversal mode for {@link OptimizedBvh}.
 * 
 * @author jezek2
 */
public enum TraversalMode {
	
	/** The stackless. */
	STACKLESS,
	
	/** The stackless cache friendly. */
	STACKLESS_CACHE_FRIENDLY,
	
	/** The recursive. */
	RECURSIVE
}
