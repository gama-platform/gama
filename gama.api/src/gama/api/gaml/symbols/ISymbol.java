/*******************************************************************************************************
 *
 * ISymbol.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.symbols;

import org.eclipse.emf.common.util.URI;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.utils.interfaces.IDisposable;
import gama.api.utils.interfaces.INamed;

/**
 * The base interface for all GAML symbols in the GAMA modeling and simulation platform.
 * 
 * <p>
 * Symbols represent the structural and behavioral elements of GAML models, including species, variables, actions,
 * reflexes, aspects, experiments, and other language constructs. Each symbol has an associated description that
 * contains its metadata and configuration (facets).
 * </p>
 * 
 * <p>
 * Symbols form a hierarchical structure where symbols can contain child symbols (e.g., a species contains variables
 * and actions). They are ordered to maintain declaration order and support proper initialization sequences.
 * </p>
 * 
 * <h2>Key Responsibilities</h2>
 * <ul>
 * <li>Maintain reference to compile-time description ({@link IDescription})</li>
 * <li>Provide access to facet values (named parameters/attributes)</li>
 * <li>Support hierarchical symbol structures with parent-child relationships</li>
 * <li>Enable serialization back to GAML source code</li>
 * <li>Support runtime evaluation and tracing</li>
 * </ul>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see IDescription
 * @see Symbol
 */
public interface ISymbol extends INamed, IDisposable {

	/**
	 * Gets the declaration order of this symbol relative to other symbols at the same level.
	 * 
	 * <p>
	 * The order is typically set during compilation and reflects the sequence in which symbols were declared in the
	 * source code. This ordering is important for initialization and execution sequences.
	 * </p>
	 *
	 * @return the declaration order (lower values indicate earlier declaration)
	 */
	int getOrder();

	/**
	 * Sets the declaration order of this symbol.
	 * 
	 * <p>
	 * This is typically called during compilation to establish the relative ordering of symbols.
	 * </p>
	 *
	 * @param o
	 *            the new order value
	 */
	void setOrder(int o);

	/**
	 * Gets the compile-time description associated with this symbol.
	 * 
	 * <p>
	 * The description contains metadata about the symbol including its facets (named parameters), documentation,
	 * validation information, and references to the source code elements.
	 * </p>
	 *
	 * @return the symbol's description, or null if not available
	 */
	IDescription getDescription();

	/**
	 * Gets the URI identifying the source location of this symbol.
	 * 
	 * <p>
	 * The URI typically points to the model file and location where this symbol was declared. This is used for error
	 * reporting, navigation, and debugging.
	 * </p>
	 *
	 * @return the URI of the source element, or null if not available
	 */
	URI getURI();

	/**
	 * Returns the expression located at the first facet matching one of the provided keys.
	 * 
	 * <p>
	 * Facets are named parameters or attributes of a symbol (e.g., 'name', 'type', 'value', 'init'). This method
	 * searches for the first key that exists and returns its associated expression.
	 * </p>
	 *
	 * @param keys
	 *            one or more facet names to search for
	 * @return the expression associated with the first matching facet, or null if none found
	 */
	IExpression getFacet(String... keys);

	/**
	 * Checks whether this symbol has a facet with the specified key.
	 * 
	 * <p>
	 * This is useful for conditional logic based on the presence of optional facets.
	 * </p>
	 *
	 * @param key
	 *            the facet name to check
	 * @return true if the facet exists, false otherwise
	 */
	boolean hasFacet(String key);

	/**
	 * Sets the child symbols contained within this symbol.
	 * 
	 * <p>
	 * For example, a species symbol contains variable, action, and reflex symbols as children. This method establishes
	 * the parent-child relationship in the symbol hierarchy.
	 * </p>
	 *
	 * @param children
	 *            the child symbols to set
	 */
	void setChildren(Iterable<? extends ISymbol> children);

	/**
	 * Gets a human-readable trace of this symbol's current state for debugging.
	 * 
	 * <p>
	 * The trace typically includes the symbol's keyword, facet names and values evaluated in the provided scope. This
	 * is useful for runtime debugging and error reporting.
	 * </p>
	 *
	 * @param abstractScope
	 *            the scope in which to evaluate facet values
	 * @return a formatted string representation of the symbol's state
	 */
	String getTrace(IScope abstractScope);

	/**
	 * Gets the GAML keyword that defines this symbol type.
	 * 
	 * <p>
	 * Examples include "species", "variable", "action", "reflex", "experiment", etc. The keyword identifies what kind
	 * of symbol this is.
	 * </p>
	 *
	 * @return the defining keyword, or null if not available
	 */
	String getKeyword();

	/**
	 * Sets the enclosing (parent) symbol that contains this symbol.
	 * 
	 * <p>
	 * For example, a variable symbol's enclosing symbol would be the species that declares it. This method is called
	 * during compilation to establish the containment hierarchy.
	 * </p>
	 * 
	 * <p>
	 * Default implementation does nothing; subclasses may override to maintain parent references.
	 * </p>
	 *
	 * @param enclosing
	 *            the parent symbol
	 */
	default void setEnclosing(final ISymbol enclosing) {}

	/**
	 * Gets the value of a facet, evaluated in the provided scope, with a default fallback.
	 * 
	 * <p>
	 * This is a convenience method that combines {@link #getFacet(String...)} with runtime evaluation. If the facet
	 * doesn't exist, the provided default value is returned.
	 * </p>
	 *
	 * @param <T>
	 *            the expected return type
	 * @param scope
	 *            the scope in which to evaluate the facet expression
	 * @param string
	 *            the facet name
	 * @param object
	 *            the default value to return if the facet doesn't exist
	 * @return the evaluated facet value, or the default if facet is not present
	 */
	<T> T getFacetValue(IScope scope, String string, T object);

}
