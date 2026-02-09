
/*******************************************************************************************************
 *
 * Facets.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.symbols;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.IType;
import gama.api.utils.BiConsumerWithPruning;
import gama.api.utils.IDisposable;
import gama.api.utils.IGamlable;
import gama.api.utils.StringUtils;
import gama.dev.DEBUG;

/**
 * Written by drogoul Modified on 27 ao�t 2010
 *
 * Represents a Map of Facet objects. From there, text, tokens and values of facets can be retrieved.
 *
 */
public class Facets extends HashMap<String, IExpressionDescription> implements IGamlable, IDisposable {
	static {
		DEBUG.OFF();
	}

	/** The clean copy. */
	static Function<IExpressionDescription, IExpressionDescription> cleanCopy = IExpressionDescription::cleanCopy;

	/** The Constant NULL. */
	public static final Facets NULL = new Facets();

	/**
	 * Exists.
	 *
	 * @return true, if successful
	 */
	public boolean exists() {
		return !isEmpty();
	}

	/**
	 * Instantiates a new facets 2.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 27 déc. 2023
	 */
	public Facets() {
		this(5);
	}

	/**
	 * Instantiates a new facets.
	 *
	 * @param size
	 *            the size
	 */
	protected Facets(final int size) {
		super(size, 0.8f);
	}

	/**
	 * Instantiates a new facets.
	 *
	 * @param strings
	 *            the strings
	 */
	public Facets(final String... strings) {
		this(strings == null ? 0 : strings.length % 2);
		if (strings != null) {
			int index = strings.length % 2 != 0 ? 1 : 0;
			for (; index < strings.length; index += 2) {
				put(strings[index], GAML.getExpressionDescriptionFactory().createStringBased(strings[index + 1]));
			}
		}
	}

	/**
	 * Instantiates a new facets.
	 *
	 * @param other
	 *            the other
	 */
	public Facets(final Facets other) {
		super(other == null ? Collections.EMPTY_MAP : other);
	}

	/**
	 * Complement with.
	 *
	 * @param newFacets
	 *            the new facets
	 */
	/*
	 * Same as putAll(), but without replacing the existing values
	 */
	public void complementWith(final Facets newFacets) {
		newFacets.forEach(this::putIfAbsent);
	}

	/**
	 * Gets the descr.
	 *
	 * @param keys
	 *            the keys
	 * @return the descr
	 */
	public IExpressionDescription getDescr(final String... keys) {
		for (final String key : keys) {
			final IExpressionDescription result = get(key);
			if (result != null) return result;
		}
		return null;

	}

	/**
	 * Gets the label.
	 *
	 * @param key
	 *            the key
	 * @return the label
	 */
	public String getLabel(final String key) {
		final IExpressionDescription f = get(key);
		if (f == null) return null;
		return StringUtils.toJavaString(f.toString());
	}

	/**
	 * Gets the expr.
	 *
	 * @param key
	 *            the key
	 * @return the expr
	 */
	public IExpression getExpr(final String key) {
		return getExpr(key, null);
	}

	/**
	 * Gets the expr.
	 *
	 * @param keys
	 *            the keys
	 * @return the expr
	 */
	public IExpression getExpr(final String... keys) {
		for (final String s : keys) {
			final IExpression expr = getExpr(s);
			if (expr != null) return expr;
		}
		return null;
	}

	/**
	 * Gets the expr.
	 *
	 * @param key
	 *            the key
	 * @param ifAbsent
	 *            the if absent
	 * @return the expr
	 */
	public IExpression getExpr(final String key, final IExpression ifAbsent) {
		final IExpressionDescription f = get(key);
		if (f == null) return ifAbsent;
		return f.getExpression();
	}

	/**
	 * Put as label.
	 *
	 * @param key
	 *            the key
	 * @param desc
	 *            the desc
	 */
	public void putAsLabel(final String key, final String desc) {
		put(key, GAML.getExpressionDescriptionFactory().createLabel(desc));
	}

	/**
	 * Put.
	 *
	 * @param key
	 *            the key
	 * @param expr
	 *            the expr
	 */
	public void putExpression(final String key, final IExpression expr) {
		final IExpressionDescription result = get(key);
		if (result != null) {
			result.setExpression(expr);
		} else {
			put(key, GAML.getExpressionDescriptionFactory().createBasic(expr));
		}
	}

	/**
	 * Put.
	 *
	 * @param key
	 *            the key
	 * @param expr
	 *            the expr
	 */
	@Override
	public IExpressionDescription put(final String key, final IExpressionDescription expr) {
		final IExpressionDescription existing = get(key);
		if (existing != null) return replace(key, expr);
		return super.put(key, expr);

	}

	/**
	 * Equals.
	 *
	 * @param key
	 *            the key
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	public boolean equals(final String key, final String o) {
		final IExpressionDescription f = get(key);
		return f == null ? o == null : f.equalsString(o);
	}

	/**
	 * @return
	 */
	public Facets cleanCopy() {
		final Facets result = new Facets(size());
		forEach((s, e) -> result.put(s, e.cleanCopy()));
		return result;
	}

	/**
	 *
	 */
	@Override
	public void dispose() {
		forEach((s, e) -> { if (e != null) { e.dispose(); } });
	}

	/**
	 * For each facet.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	public boolean forEachFacet(final BiConsumerWithPruning<String, IExpressionDescription> visitor) {
		for (Map.Entry<String, IExpressionDescription> entry : entrySet()) {
			if (!visitor.process(entry.getKey(), entry.getValue())) return false;
		}
		return true;
	}

	/**
	 * For each facet in.
	 *
	 * @param names
	 *            the names
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	public boolean forEachFacetIn(final Set<String> names,
			final BiConsumerWithPruning<String, IExpressionDescription> visitor) {
		if (names == null) return forEachFacet(visitor);
		for (Map.Entry<String, IExpressionDescription> entry : entrySet()) {
			String key = entry.getKey();
			if (names.contains(key) && !visitor.process(key, entry.getValue())) return false;
		}
		return true;
	}

	/**
	 * Gets the first existing among.
	 *
	 * @param strings
	 *            the strings
	 * @return the first existing among
	 */
	public String getFirstExistingAmong(final String... strings) {
		for (final String s : strings) { if (containsKey(s)) return s; }
		return null;
	}

	/**
	 * Gets the type denoted by.
	 *
	 * @param key
	 *            the key
	 * @param context
	 *            the context
	 * @param noType
	 *            the no type
	 * @return the type denoted by
	 */
	public IType<?> getTypeDenotedBy(final String key, final IDescription context, final IType<?> noType) {
		final IExpressionDescription f = get(key);
		if (f == null) return noType;
		// DEBUG.OUT("Looking for the type of facet " + key);
		return f.getDenotedType(context);
	}

	/**
	 * Contains key.
	 *
	 * @param key
	 *            the key
	 * @return true, if successful
	 */
	public boolean containsKey(final String key) {
		return super.containsKey(key);
	}

	/**
	 * Removes the.
	 *
	 * @param key
	 *            the key
	 * @return the i expression description
	 */
	public IExpressionDescription remove(final String key) {
		return super.remove(key);
	}

}