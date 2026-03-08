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
 * The symbols package provides structural symbol definitions for GAML.
 * 
 * <p>This package contains interfaces for GAML structural symbols, which are the primary
 * building blocks of model structure including model, global, species, experiment, and other
 * top-level constructs.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.gaml.symbols.ISymbol} - Base interface for all symbols</li>
 *   <li>{@link gama.api.gaml.symbols.IVariable} - Interface for variables</li>
 *   <li>{@link gama.api.gaml.symbols.IParameter} - Interface for parameters</li>
 * </ul>
 * 
 * <h2>Symbol Types</h2>
 * 
 * <h3>Structural Symbols:</h3>
 * <ul>
 *   <li><strong>model:</strong> Top-level model definition</li>
 *   <li><strong>global:</strong> Global section definition</li>
 *   <li><strong>species:</strong> Agent type definition</li>
 *   <li><strong>experiment:</strong> Experiment definition</li>
 *   <li><strong>grid:</strong> Grid species definition</li>
 * </ul>
 * 
 * <h3>Member Symbols:</h3>
 * <ul>
 *   <li><strong>var/parameter:</strong> Variable and parameter definitions</li>
 *   <li><strong>action:</strong> Action/behavior definitions</li>
 *   <li><strong>reflex:</strong> Automatic behavior definitions</li>
 *   <li><strong>aspect:</strong> Visual representation definitions</li>
 * </ul>
 * 
 * <h3>Output Symbols:</h3>
 * <ul>
 *   <li><strong>display:</strong> Visualization output</li>
 *   <li><strong>monitor:</strong> Value monitoring output</li>
 *   <li><strong>inspect:</strong> Agent inspection output</li>
 * </ul>
 * 
 * <h2>Symbol Lifecycle</h2>
 * 
 * <p>Symbols have a compilation lifecycle:</p>
 * <ol>
 *   <li>Parse from source</li>
 *   <li>Create description</li>
 *   <li>Validate facets and children</li>
 *   <li>Compile to runtime representation</li>
 * </ol>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.symbols.ISymbol
 * @see gama.api.compilation.descriptions
 */
package gama.api.gaml.symbols;
