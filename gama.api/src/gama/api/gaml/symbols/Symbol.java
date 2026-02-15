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
 * Written by drogoul Modified on 13 mai 2010 A simple class to serve as the root of all Gaml Symbols
 *
 * @todo Description
 *
 */
public abstract class Symbol implements ISymbol {

	/** The name. */
	protected String name;

	/** The description. */
	protected final IDescription description;

	/** The order. */
	protected int order;

	@Override
	public IDescription getDescription() { return description; }

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
	 * Instantiates a new symbol.
	 *
	 * @param desc
	 *            the desc
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

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		if (description == null) return "";
		return description.serializeToGaml(includingBuiltIn);
	}

	@Override
	public String getKeyword() {
		if (description == null) return null;
		return description.getKeyword();
	}

	@Override
	public final IExpression getFacet(final String... keys) {
		if (description == null) return null;
		return description.getFacetExpr(keys);
	}

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @return the facet value
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public Object getFacetValue(final IScope scope, final String key) throws GamaRuntimeException {
		return getFacetValue(scope, key, null);
	}

	/**
	 * Gets the facet value.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the facet value
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	@SuppressWarnings ("unchecked")
	public final <T> T getFacetValue(final IScope scope, final String key, final T defaultValue)
			throws GamaRuntimeException {
		final IExpression exp = getFacet(key);
		return (T) (exp == null ? defaultValue : exp.value(scope));
	}

	/**
	 * Gets the literal.
	 *
	 * @param key
	 *            the key
	 * @return the literal
	 */
	public String getLiteral(final String key) {
		return getLiteral(key, null);
	}

	/**
	 * Gets the literal.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the literal
	 */
	public String getLiteral(final String key, final String defaultValue) {
		final IExpression exp = getFacet(key);
		return exp == null ? defaultValue : exp.literalValue();
	}

	/**
	 * Sets the facet.
	 *
	 * @param key
	 *            the key
	 * @param expr
	 *            the expr
	 */
	protected void setFacet(final String key, final IExpressionDescription expr) {
		if (description == null) return;
		description.setFacetExprDescription(key, expr);
	}

	@Override
	public boolean hasFacet(final String s) {
		return description != null && description.hasFacet(s);
	}

	@Override
	public void setName(final String n) { name = n; }

	@Override
	public String getName() { return name; }

	@Override
	public void dispose() {}

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
