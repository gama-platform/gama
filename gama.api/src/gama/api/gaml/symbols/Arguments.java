/*******************************************************************************************************
 *
 * Arguments.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.symbols;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.object.IObject;
import gama.api.runtime.scope.IScope;
import gama.api.utils.interfaces.BiConsumerWithPruning;

/**
 * Argument list for GAML statements and expressions, extending Facets with caller context and ordering. Maintains
 * insertion order of arguments via a key list and associates them with a caller agent for evaluation.
 *
 * @author drogoul
 */
public class Arguments extends Facets {

	/** A shared null instance representing no arguments. */
	public static final Arguments NULL = new Arguments();

	/** Preserves insertion order of argument keys (used by getExpr(int index)). */
	private List<String> keys;

	/**
	 * The caller represents the agent in the context of which the arguments need to be evaluated. Uses ThreadLocal to
	 * support multi-threaded evaluation contexts.
	 */
	private final ThreadLocal<IObject> caller = new ThreadLocal<>();

	/**
	 * Instantiates a new arguments map from another arguments object. Copies all argument mappings and the caller
	 * context.
	 *
	 * @param args
	 *            the source arguments (may be null)
	 */
	public Arguments(final Arguments args) {
		if (args != null) {
			putAll(args);
			setCaller(args.getCaller());
		}
	}

	/**
	 * Instantiates a new arguments map from a key-value map. Values are automatically wrapped in constant expression
	 * descriptions.
	 *
	 * @param myArgs
	 *            the source map
	 */
	public Arguments(final Map<String, Object> myArgs) {
		this();
		if (myArgs != null) {
			myArgs.forEach((k, v) -> put(k, GAML.getExpressionDescriptionFactory().createConstant(v)));
		}
	}

	/**
	 * Instantiates a new arguments map with a caller context. Values from the map are automatically wrapped in constant
	 * expression descriptions.
	 *
	 * @param caller
	 *            the agent context for evaluation
	 * @param args
	 *            the source map
	 */
	public Arguments(final IAgent caller, final Map<String, Object> args) {
		this(args);
		setCaller(caller);
	}

	/**
	 * Instantiates an empty arguments map.
	 */
	public Arguments() {}

	/**
	 * Creates a deep copy of this arguments map including the caller context.
	 *
	 * @return a new arguments map with copied entries and caller reference
	 */
	@Override
	public Arguments cleanCopy() {
		final Arguments result = new Arguments();
		result.setCaller(caller.get());
		forEach((s, e) -> result.put(s, e.cleanCopy()));
		return result;
	}

	/**
	 * Resolves all argument expressions against a given scope.
	 *
	 * @param scope
	 *            the evaluation scope
	 * @return a new arguments map with resolved expressions
	 */
	public Arguments resolveAgainst(final IScope scope) {
		final Arguments result = new Arguments();
		result.setCaller(caller.get());
		forEach((s, e) -> {
			final IExpression exp = getExpr(s);
			if (exp != null) { result.putExpression(s, exp.resolveAgainst(scope)); }
		});
		return result;
	}

	/**
	 * Puts an argument and maintains key insertion order.
	 *
	 * @param s
	 *            the argument name
	 * @param e
	 *            the expression description
	 * @return the previous expression, or null if none
	 */
	@Override
	public IExpressionDescription put(final String s, final IExpressionDescription e) {
		if (keys == null) { keys = new ArrayList<>(); }
		if (!keys.contains(s)) { keys.add(s); }
		return super.put(s, e);
	}

	/**
	 * Removes an argument and its key from the ordering list.
	 *
	 * @param s
	 *            the argument name
	 * @return the removed expression description
	 */
	@Override
	public IExpressionDescription remove(final String s) {
		if (keys != null) { keys.remove(s); }
		return super.remove(s);
	}

	/**
	 * Sets the caller agent context for argument evaluation.
	 *
	 * @param caller
	 *            the agent context
	 */
	public void setCaller(final IObject caller) {
		this.caller.set(caller);
	}

	/**
	 * Gets the caller agent context.
	 *
	 * @return the caller, or null if not set
	 */
	public IObject getCaller() { return caller.get(); }

	/**
	 * Clears all arguments and resets the caller context.
	 */
	@Override
	public void dispose() {
		super.dispose();
		caller.set(null);
	}

	/**
	 * Gets the expression for an argument by positional index.
	 *
	 * @param index
	 *            the position (0-based)
	 * @return the expression at that position, or null if out of bounds or key undefined
	 */
	public IExpression getExpr(final int index) {
		if (index < 0 || index >= size() || keys == null || index >= keys.size()) return null;
		final String key = keys.get(index);
		final IExpressionDescription desc = get(key);
		return desc == null ? null : desc.getExpression();
	}

	/**
	 * Iterates over all arguments with a visitor callback.
	 *
	 * @param visitor
	 *            the visitor callback
	 * @return true if all arguments were processed, false if visitor returned false
	 */
	public boolean forEachArgument(final BiConsumerWithPruning<String, IExpressionDescription> visitor) {
		return forEachFacet(visitor);
	}

	/**
	 * Adds arguments from another Arguments map without replacing existing keys.
	 *
	 * @param newFacets
	 *            the arguments to merge
	 */
	public void complementWith(final Arguments newFacets) {
		if (newFacets != null) {
			newFacets.forEachArgument((s, v) -> {
				if (!containsKey(s)) { put(s, v); }
				return true;
			});
		}
	}

}
