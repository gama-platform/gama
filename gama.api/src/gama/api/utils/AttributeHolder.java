/*******************************************************************************************************
 *
 * AttributeHolder.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;

/**
 * A class that facilitates the development of classes holding attributes declared in symbols' facets
 *
 * @author drogoul
 *
 */
public abstract class AttributeHolder {

	// static {
	// DEBUG.OFF();
	// }

	/** The attributes. */
	final Map<String, Attribute<?>> attributes = new HashMap<>(10);

	/**
	 * Cached array of attribute entries for fast iteration during refresh(). Built lazily on the first refresh() call
	 * and rebuilt whenever new attributes are added via create(). Eliminates per-frame HashMap traversal and lambda
	 * allocation in the hot drawing path.
	 */
	private Attribute<?>[] attributeArray;

	/** The symbol. */
	protected final ISymbol symbol;

	/**
	 * The Interface Attribute.
	 *
	 * @param <V>
	 *            the value type
	 */
	public interface Attribute<V> extends IExpression {

		/**
		 * Refresh.
		 *
		 * @param scope
		 *            the scope
		 */
		void refresh(final String name, final IScope scope);

		/**
		 * Gets the.
		 *
		 * @return the v
		 */
		V get();

		/**
		 * Changed.
		 *
		 * @return true, if successful
		 */
		boolean changed();

		/**
		 * Gets the gaml type.
		 *
		 * @return the gaml type
		 */
		@Override
		default IType<V> getGamlType() { return Types.NO_TYPE; }

	}

	/**
	 * The Interface IExpressionWrapper.
	 *
	 * @param <V>
	 *            the value type
	 */
	public interface IExpressionWrapper<V> {

		/**
		 * Value.
		 *
		 * @param scope
		 *            the scope
		 * @param facet
		 *            the facet
		 * @return the v
		 */
		V value(IScope scope, IExpression facet);
	}

	/**
	 * The Class ConstantAttribute.
	 *
	 * @param <V>
	 *            the value type
	 */
	public static class ConstantAttribute<V> implements Attribute<V> {

		/** The value. */
		private final V value;

		/**
		 * Instantiates a new constant attribute.
		 *
		 * @param value
		 *            the value
		 */
		public ConstantAttribute(final V value) {
			this.value = value;
		}

		/**
		 * Refresh.
		 *
		 * @param scope
		 *            the scope
		 */
		@Override
		public void refresh(final String name, final IScope scope) {}

		@Override
		public V value(final IScope scope) {
			return value;
		}

		@Override
		public V get() {
			return value;
		}

		@Override
		public boolean changed() {
			return false;
		}

	}

	/**
	 * The Class ExpressionAttribute.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 */
	static class ExpressionAttribute<T extends IType<V>, V> implements Attribute<V> {

		/** The expression. */
		final IExpression expression;

		/** The return type. */
		final T returnType;

		/** The value. */
		private volatile V value;

		/** The changed. */
		private volatile boolean changed;

		/**
		 * Instantiates a new expression attribute.
		 *
		 * @param type
		 *            the type
		 * @param ev
		 *            the ev
		 * @param init
		 *            the init
		 */
		public ExpressionAttribute(final T type, final IExpression ev, final V init) {
			expression = ev;
			returnType = type;
			value = init;
		}

		@Override
		public V value(final IScope scope) {
			return returnType.cast(scope, expression.value(scope), null, false);
		}

		/**
		 * Refresh.
		 *
		 * @param scope
		 *            the scope
		 */
		@Override
		public void refresh(final String name, final IScope scope) {
			changed = false;
			V old = value;
			value = value(scope);
			changed = !Objects.equals(old, value);
		}

		@Override
		public V get() {
			return value;
		}

		@Override
		public boolean changed() {
			return changed;
		}

	}

	/**
	 * The Class ExpressionEvaluator.
	 *
	 * @param <V>
	 *            the value type
	 */
	static class ExpressionEvaluator<V> implements Attribute<V> {

		/** The evaluator. */
		final IExpressionWrapper<V> evaluator;

		/** The facet. */
		final IExpression facet;

		/** The value. */
		private V value;

		/** The changed. */
		boolean changed;

		/**
		 * Instantiates a new expression evaluator.
		 *
		 * @param ev
		 *            the ev
		 * @param expression
		 *            the expression
		 */
		public ExpressionEvaluator(final IExpressionWrapper<V> ev, final IExpression expression) {
			evaluator = ev;
			facet = expression;
		}

		@Override
		public V value(final IScope scope) {
			return evaluator.value(scope, facet);
		}

		/**
		 * Refresh.
		 *
		 * @param scope
		 *            the scope
		 */
		@Override
		public void refresh(final String name, final IScope scope) {
			changed = false;
			V old = value;
			value = value(scope);
			changed = !Objects.equals(old, value);
		}

		@Override
		public V get() {
			return value;
		}

		@Override
		public boolean changed() {
			return changed;
		}

	}

	/**
	 * Refresh.
	 *
	 * @param scope
	 *            the scope
	 * @return the attribute holder
	 */
	@SuppressWarnings ("unchecked")
	public void refresh(final IScope scope) {
		// Build the array cache on first call (or after a new attribute was added)
		if (attributeArray == null) { attributeArray = attributes.values().toArray(new Attribute[0]); }
		final Attribute<?>[] arr = attributeArray;
		final int n = arr.length;
		for (int i = 0; i < n; i++) { arr[i].refresh(null, scope); }
	}

	/**
	 * Instantiates a new attribute holder.
	 *
	 * @param symbol
	 *            the symbol
	 */
	public AttributeHolder(final ISymbol symbol) {
		this.symbol = symbol;
	}

	/**
	 * Registers an attribute and invalidates the iteration cache so that the next refresh() rebuilds it.
	 */
	private <V> void putAttribute(final String facet, final Attribute<V> attr) {
		attributes.put(facet, attr);
		attributeArray = null; // invalidate cache
	}

	// ...existing code...

	/**
	 * Creates the.
	 *
	 * @param <V>
	 *            the value type
	 * @param facet
	 *            the facet
	 * @param def
	 *            the def
	 * @return the attribute
	 */
	protected <V> Attribute<V> create(final String facet, final V def) {
		final Attribute<V> result = new ConstantAttribute<>(def);
		putAttribute(facet, result);
		return result;
	}

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 * @param facet
	 *            the facet
	 * @param type
	 *            the type
	 * @param def
	 *            the def
	 * @return the attribute
	 */
	protected <T extends IType<V>, V> Attribute<V> create(final String facet, final T type, final V def) {
		final IExpression exp = symbol.getFacet(facet);
		return create(facet, exp, type, def);
	}

	// ...existing code...

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 * @param facet
	 *            the facet
	 * @param exp
	 *            the exp
	 * @param type
	 *            the type
	 * @param def
	 *            the def
	 * @return the attribute
	 */
	protected <T extends IType<V>, V> Attribute<V> create(final String facet, final IExpression exp, final T type,
			final V def) {
		Attribute<V> result;
		if (exp == null || exp.isConst() && exp.isContextIndependant() && type != Types.BOOL) {
			result = new ConstantAttribute<>(exp == null ? def : type.cast(null, exp.getConstValue(), null, true));
		} else {
			result = new ExpressionAttribute<>(type, exp, def);
		}
		putAttribute(facet, result);
		return result;
	}

	// ...existing code...

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <V>
	 *            the value type
	 * @param facet
	 *            the facet
	 * @param ev
	 *            the ev
	 * @param type
	 *            the type
	 * @param def
	 *            the def
	 * @return the attribute
	 */
	protected <T extends IType<V>, V> Attribute<V> create(final String facet, final IExpressionWrapper<V> ev,
			final T type, final V def) {
		final IExpression exp = symbol.getFacet(facet);
		Attribute<V> result;
		if (exp == null) {
			result = new ConstantAttribute<>(def);
		} else {
			result = new ExpressionEvaluator<>(ev, exp);
		}
		putAttribute(facet, result);
		return result;
	}

}
