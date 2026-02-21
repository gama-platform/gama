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
 * The ast package provides Abstract Syntax Tree (AST) representations for GAML source code.
 * 
 * <p>This package contains interfaces and classes for representing the syntactic structure of GAML
 * models as they are parsed from source files. The AST preserves source location information and
 * provides the foundation for building semantic model descriptions.</p>
 * 
 * <h2>Core Concepts</h2>
 * 
 * <h3>Syntactic Elements:</h3>
 * <p>The AST is composed of syntactic elements that represent GAML constructs:</p>
 * <ul>
 *   <li>{@link gama.api.compilation.ast.ISyntacticElement} - Base interface for all syntactic elements</li>
 *   <li>Model, species, action, statement, and expression elements</li>
 *   <li>Preserve keyword, facet, and child element information</li>
 *   <li>Maintain source location for error reporting</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * 
 * <p>AST elements are typically created during parsing and then transformed into semantic
 * descriptions during the compilation process. They serve as an intermediate representation
 * between raw source text and executable model descriptions.</p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.compilation.ast.ISyntacticElement
 * @see gama.api.compilation.descriptions
 */
package gama.api.compilation.ast;
