/*******************************************************************************************************
 *
 * Symbol.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.symbols;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.dev.COUNTER;

/**
 * Base abstract implementation of {@link ISymbol} providing common functionality for GAML symbols.
 * 
 * <p>
 * This class serves as the foundation for all concrete symbol implementations in GAMA. It provides default
 * implementations for core symbol operations including facet access, description management, serialization, and
 * lifecycle management.
 * </p>
 * 
 * <h2>Key Responsibilities</h2>
 * <ul>
 * <li><strong>Description Management</strong> - Maintains reference to the compile-time description</li>
 * <li><strong>Facet Access</strong> - Provides convenient methods for reading facet values</li>
 * <li><strong>Ordering</strong> - Manages declaration order for initialization sequencing</li>
 * <li><strong>Serialization</strong> - Supports conversion back to GAML source code</li>
 * <li><strong>Tracing</strong> - Enables runtime debugging and error reporting</li>
 * </ul>
 * 
 * <h2>Subclassing</h2>
 * <p>
 * Concrete symbol implementations should extend this class and override methods as needed:
 * </p>
 * <ul>
 * <li>{@link #setChildren(Iterable)} - To establish parent-child relationships</li>
 * <li>{@link #setEnclosing(ISymbol)} - To track the containing symbol</li>
 * <li>{@link #dispose()} - To perform cleanup when the symbol is no longer needed</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * This class is not thread-safe. Symbols are typically created during compilation (single-threaded) and then shared
 * read-only during execution. Modifications during runtime should be avoided.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see ISymbol
 * @see IDescription
 */
public abstract class Symbol implements ISymbol {

	/** The name of this symbol (typically from the 'name' facet). */
	protected String name;

	/** The compile-time description containing facets and metadata. */
	protected final IDescription description;

	/** The declaration order of this symbol relative to siblings. */
	protected int order;

	/**
	 * Gets the compile-time description associated with this symbol.
	 *
	 * @return the description, or null if not available
	 */
	@Override
	public IDescription getDescription() { return description; }

	/**
	 * Gets the URI identifying the source location of this symbol.
	 * 
	 * <p>
	 * Extracts the URI from the underlying EMF element in the description, if available.
	 * </p>
	 *
	 * @return the URI, or null if description or underlying element is not available
	 */
	@Override
	public URI getURI() {
		if (description == null) return null;
		final EObject object = description.getUnderlyingElement();
		return object == null ? null : EcoreUtil.getURI(object);
	}

	@Override
	public int getOrder() { return order; }

	@Override
	public void setOrder(final int i) { order = i; }

	/**
	 * Constructs a new symbol with the given description.
	 * 
	 * <p>
	 * The constructor assigns a unique order number to this symbol for sequencing purposes. The order is obtained from
	 * a global counter to ensure uniqueness across the model.
	 * </p>
	 *
	 * @param desc
	 *            the compile-time description (may be null for special cases)
	 */
	public Symbol(final IDescription desc) {
		description = desc;
		// if (desc != null) {
		// order = desc.getOrder();
		// } else {
		order = COUNTER.GET_UNIQUE();
		// DEBUG.LOG("Order of " + desc.getName() + " = " + order);
		// }
	}

	/**
	 * Serializes this symbol back to GAML source code.
	 * 
	 * <p>
	 * Delegates to the description's serialization if available. This allows symbols to be written back to files or
	 * displayed in debugging interfaces.
	 * </p>
	 *
	 * @param includingBuiltIn
	 *            whether to include built-in/internal symbols in the output
	 * @return the GAML source code representation, or empty string if no description
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		if (description == null) return "";
		return description.serializeToGaml(includingBuiltIn);
	}

	/**
	 * Gets the GAML keyword that defines this symbol type.
	 *
	 * @return the keyword (e.g., "species", "variable", "action"), or null if no description
	 */
	@Override
	public String getKeyword() {
		if (description == null) return null;
		return description.getKeyword();
	}

	/**
	 * Gets the expression for the first matching facet among the provided keys.
	 * 
	 * <p>
	 * This is a convenience method that searches for the first facet that exists and returns its compiled expression.
	 * Useful for facets that may have multiple valid names (aliases).
	 * </p>
	 *
	 * @param keys
	 *            one or more facet names to search for
	 * @return the expression of the first matching facet, or null if none found or no description
	 */
	@Override
	public final IExpression getFacet(final String... keys) {
		if (description == null) return null;
		return description.getFacetExpr(keys);
	}

	/**
	 * Gets the value of a facet, evaluated in the provided scope.
	 * 
	 * <p>
	 * Convenience method equivalent to calling {@link #getFacetValue(IScope, String, Object)} with a null default.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param key
	 *            the facet name
	 * @return the evaluated facet value, or null if facet doesn't exist
	 * @throws GamaRuntimeException
	 *             if evaluation fails
	 */
	public Object getFacetValue(final IScope scope, final String key) throws GamaRuntimeException {
		return getFacetValue(scope, key, null);
	}

	/**
	 * Gets the value of a facet, evaluated in the provided scope, with a default fallback.
	 * 
	 * <p>
	 * This method retrieves the facet expression, evaluates it in the given scope, and returns the result. If the facet
	 * doesn't exist, the default value is returned instead.
	 * </p>
	 *
	 * @param <T>
	 *            the expected return type
	 * @param scope
	 *            the execution scope
	 * @param key
	 *            the facet name
	 * @param defaultValue
	 *            the value to return if the facet doesn't exist
	 * @return the evaluated facet value, or the default if facet is not present
	 * @throws GamaRuntimeException
	 *             if evaluation fails
	 */
	@Override
	@SuppressWarnings ("unchecked")
	public final <T> T getFacetValue(final IScope scope, final String key, final T defaultValue)
			throws GamaRuntimeException {
		final IExpression exp = getFacet(key);
		return (T) (exp == null ? defaultValue : exp.value(scope));
	}

	/**
	 * Gets the literal (uncompiled) string value of a facet.
	 * 
	 * <p>
	 * Returns the facet's literal representation without evaluation. Useful for getting constant string values or
	 * displaying the source text.
	 * </p>
	 *
	 * @param key
	 *            the facet name
	 * @return the literal value, or null if facet doesn't exist
	 */
	public String getLiteral(final String key) {
		return getLiteral(key, null);
	}

	/**
	 * Gets the literal (uncompiled) string value of a facet with a default fallback.
	 *
	 * @param key
	 *            the facet name
	 * @param defaultValue
	 *            the value to return if the facet doesn't exist
	 * @return the literal value, or the default if facet is not present
	 */
	public String getLiteral(final String key, final String defaultValue) {
		final IExpression exp = getFacet(key);
		return exp == null ? defaultValue : exp.literalValue();
	}

	/**
	 * Sets or updates a facet's expression description.
	 * 
	 * <p>
	 * This method modifies the compile-time description of the symbol. Should be used with caution, typically only
	 * during compilation or special symbol transformations.
	 * </p>
	 *
	 * @param key
	 *            the facet name
	 * @param expr
	 *            the new expression description
	 */
	protected void setFacet(final String key, final IExpressionDescription expr) {
		if (description == null) return;
		description.setFacetExprDescription(key, expr);
	}

	/**
	 * Checks if this symbol has a facet with the specified key.
	 *
	 * @param s
	 *            the facet name
	 * @return true if the facet exists, false otherwise or if no description
	 */
	@Override
	public boolean hasFacet(final String s) {
		return description != null && description.hasFacet(s);
	}

	/**
	 * Sets the name of this symbol.
	 *
	 * @param n
	 *            the new name
	 */
	@Override
	public void setName(final String n) { name = n; }

	/**
	 * Gets the name of this symbol.
	 *
	 * @return the name
	 */
	@Override
	public String getName() { return name; }

	/**
	 * Disposes of this symbol and releases any resources.
	 * 
	 * <p>
	 * Default implementation does nothing. Subclasses should override to perform cleanup such as removing listeners,
	 * disposing child symbols, or releasing external resources.
	 * </p>
	 */
	@Override
	public void dispose() {}

	/**
	 * Gets a detailed trace of this symbol's current state for debugging.
	 * 
	 * <p>
	 * The trace includes the symbol's keyword, facet names, their source expressions, and their evaluated values in the
	 * provided scope. Internal symbols (names starting with INTERNAL) are excluded from the trace.
	 * </p>
	 *
	 * @param scope
	 *            the scope in which to evaluate facet values
	 * @return a formatted string showing the symbol's state
	 */
	@Override
	public String getTrace(final IScope scope) {
		final StringBuilder sb = new StringBuilder(100);
		sb.append(getKeyword()).append(' ');
		if (getDescription() != null) {
			getDescription().visitFacets((name, ed) -> {
				if (IKeyword.NAME.equals(name)) {
					final String n = getFacet(IKeyword.NAME).literalValue();
					if (n.startsWith(IKeyword.INTERNAL)) return true;
				}
				IExpression expr = null;
				if (ed != null) { expr = ed.getExpression(); }
				final String exprString = expr == null ? "N/A" : expr.serializeToGaml(false);
				final String exprValue = expr == null ? "nil" : StringUtils.toGaml(expr.value(scope), false);
				sb.append(name).append(": [ ").append(exprString).append(" ] ").append(exprValue).append(" ");
				return true;
			});
		}

		return sb.toString();

	}

}
