
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

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.IType;
import gama.api.utils.StringUtils;
import gama.api.utils.interfaces.BiConsumerWithPruning;
import gama.api.utils.interfaces.IDisposable;
import gama.api.utils.interfaces.IGamlable;
import gama.dev.DEBUG;

/**
 * Represents a map of named facet expressions in GAML. Provides convenient access to expression descriptions with type
 * denotation, copying, and disposal support. Facets are typically used to represent optional or named parameters in
 * GAML statements and expressions.
 *
 * @author drogoul
 */
public class Facets extends HashMap<String, IExpressionDescription> implements IGamlable, IDisposable {
	static {
		DEBUG.OFF();
	}

	/** The Constant NULL. */
	public static final Facets NULL = new Facets();

	/**
	 * Checks if this facets map is non-empty.
	 *
	 * @return true if facets exist
	 */
	public boolean exists() {
		return !isEmpty();
	}

	/**
	 * Instantiates an empty facets map with default capacity.
	 */
	public Facets() {
		this(5);
	}

	/**
	 * Instantiates a facets map with a specific initial capacity.
	 *
	 * @param size
	 *            the initial capacity
	 */
	protected Facets(final int size) {
		super(size, 0.8f);
	}

	/**
	 * Instantiates a facets map from alternating key-value string pairs. Pairs are converted to constant expressions
	 * automatically.
	 *
	 * @param strings
	 *            alternating keys and values (e.g., "key1", "value1", "key2", "value2")
	 */
	public Facets(final String... strings) {
		this(strings == null ? 0 : (strings.length + 1) / 2);
		if (strings != null && strings.length > 0) {
			for (int i = 0; i + 1 < strings.length; i += 2) {
				put(strings[i], GAML.getExpressionDescriptionFactory().createStringBased(strings[i + 1]));
			}
		}
	}

	/**
	 * Instantiates a facets map from another facets map (shallow copy).
	 *
	 * @param other
	 *            the source facets map
	 */
	public Facets(final Facets other) {
		super(other == null ? Collections.emptyMap() : other);
	}

	/**
	 * Complement with.
	 *
	 * @param newFacets
	 *            the new facets to add (without replacing existing values)
	 */
	public void complementWith(final Facets newFacets) {
		if (newFacets != null) { newFacets.forEach(this::putIfAbsent); }
	}

	/**
	 * Gets the first matching expression description among the given keys.
	 *
	 * @param keys
	 *            the keys to search
	 * @return the first found expression description, or null
	 */
	public IExpressionDescription getDescr(final String... keys) {
		if (keys == null || keys.length == 0) return null;
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
	 * @return the label as a Java string, or null
	 */
	public String getLabel(final String key) {
		final IExpressionDescription f = get(key);
		return f == null ? null : StringUtils.toJavaString(f.toString());
	}

	/**
	 * Gets the expression for a given key.
	 *
	 * @param key
	 *            the key
	 * @return the expression, or null if not found
	 */
	public IExpression getExpr(final String key) {
		return getExpr(key, null);
	}

	/**
	 * Gets the first existing expression among the given keys.
	 *
	 * @param keys
	 *            the keys to search
	 * @return the first found expression, or null
	 */
	public IExpression getExpr(final String... keys) {
		if (keys == null || keys.length == 0) return null;
		for (final String s : keys) {
			final IExpression expr = getExpr(s);
			if (expr != null) return expr;
		}
		return null;
	}

	/**
	 * Gets the expression for a given key with a fallback.
	 *
	 * @param key
	 *            the key
	 * @param ifAbsent
	 *            the fallback expression
	 * @return the expression or fallback
	 */
	public IExpression getExpr(final String key, final IExpression ifAbsent) {
		final IExpressionDescription f = get(key);
		return f == null ? ifAbsent : f.getExpression();
	}

	/**
	 * Put as label.
	 *
	 * @param key
	 *            the key
	 * @param desc
	 *            the label description
	 */
	public void putAsLabel(final String key, final String desc) {
		put(key, GAML.getExpressionDescriptionFactory().createLabel(desc));
	}

	/**
	 * Put expression.
	 *
	 * @param key
	 *            the key
	 * @param expr
	 *            the expression
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
	 * Put expression description.
	 *
	 * @param key
	 *            the key
	 * @param expr
	 *            the expression description
	 * @return the previous expression description, or null
	 */
	@Override
	public IExpressionDescription put(final String key, final IExpressionDescription expr) {
		final IExpressionDescription existing = get(key);
		return existing != null ? replace(key, expr) : super.put(key, expr);
	}

	/**
	 * Checks if a facet equals a string value.
	 *
	 * @param key
	 *            the key
	 * @param o
	 *            the string to compare
	 * @return true if the facet's string value matches
	 */
	public boolean equals(final String key, final String o) {
		final IExpressionDescription f = get(key);
		return f == null ? o == null : f.equalsString(o);
	}

	/**
	 * Creates a deep copy of this facets map.
	 *
	 * @return a new facets map with all entries deep-copied
	 */
	public Facets cleanCopy() {
		final Facets result = new Facets(size());
		forEach((s, e) -> result.put(s, e.cleanCopy()));
		return result;
	}

	/**
	 * Disposes all expression descriptions in this map.
	 */
	@Override
	public void dispose() {
		forEach((s, e) -> { if (e != null) { e.dispose(); } });
	}

	/**
	 * For each facet.
	 *
	 * @param visitor
	 *            the visitor callback
	 * @return true if all facets were processed, false if visitor returned false
	 */
	public boolean forEachFacet(final BiConsumerWithPruning<String, IExpressionDescription> visitor) {
		if (visitor == null) return true;
		for (Map.Entry<String, IExpressionDescription> entry : entrySet()) {
			if (!visitor.process(entry.getKey(), entry.getValue())) return false;
		}
		return true;
	}

	/**
	 * For each facet in a restricted set of names.
	 *
	 * @param names
	 *            the names to process (or null for all)
	 * @param visitor
	 *            the visitor callback
	 * @return true if all matching facets were processed, false if visitor returned false
	 */
	public boolean forEachFacetIn(final Set<String> names,
			final BiConsumerWithPruning<String, IExpressionDescription> visitor) {
		if (visitor == null) return true;
		if (names == null) return forEachFacet(visitor);
		for (Map.Entry<String, IExpressionDescription> entry : entrySet()) {
			String key = entry.getKey();
			if (names.contains(key) && !visitor.process(key, entry.getValue())) return false;
		}
		return true;
	}

	/**
	 * Gets the first existing key among the given strings.
	 *
	 * @param strings
	 *            the keys to check
	 * @return the first found key, or null
	 */
	public String getFirstExistingAmong(final String... strings) {
		if (strings == null || strings.length == 0) return null;
		for (final String s : strings) { if (containsKey(s)) return s; }
		return null;
	}

	/**
	 * Gets the type denoted by a facet.
	 *
	 * @param key
	 *            the facet key
	 * @param context
	 *            the description context for type resolution
	 * @param noType
	 *            the fallback type if not found
	 * @return the denoted type or fallback
	 */
	public IType<?> getTypeDenotedBy(final String key, final IDescription context, final IType<?> noType) {
		final IExpressionDescription f = get(key);
		return f == null ? noType : f.getDenotedType(context);
	}

	/**
	 * Contains key.
	 *
	 * @param key
	 *            the key
	 * @return true if the key exists
	 */
	public boolean containsKey(final String key) {
		return super.containsKey(key);
	}

	/**
	 * Removes the facet.
	 *
	 * @param key
	 *            the key
	 * @return the removed expression description
	 */
	public IExpressionDescription remove(final String key) {
		return super.remove(key);
	}

}