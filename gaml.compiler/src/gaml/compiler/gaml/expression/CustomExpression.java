/**
 *
 */
package gaml.compiler.gaml.expression;

import java.util.function.Function;

import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public class CustomExpression<T> extends UnitConstantExpression {

	/** The function. */
	final Function<IScope, T> function;

	/**
	 * @param val
	 * @param t
	 * @param name
	 * @param doc
	 * @param names
	 */
	public CustomExpression(final String name, final IType type, final T val, final String doc,
			final Function<IScope, T> function) {
		super(val, type == null ? GamaType.of(val) : type, name, doc, null);
		this.function = function;
	}

	@Override
	public T _value(final IScope sc) {
		return function.apply(sc);
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public boolean isContextIndependant() { return false; }

	@Override
	public boolean isAllowedInParameters() { return false; }

}
