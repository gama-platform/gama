/*******************************************************************************************************
 *
 * InScope.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

/**
 * The Interface InScope.
 *
 * @param <T>
 *            the generic type
 */
public interface InScope<T> {

	/**
	 * The Class Void.
	 */
	public abstract static class Void implements InScope<Object> {

		@Override
		public Object run(final IScope scope) {
			process(scope);
			return null;
		}

		/**
		 * Process.
		 *
		 * @param scope
		 *            the scope
		 */
		public abstract void process(IScope scope);
	}

	/**
	 * Run.
	 *
	 * @param scope
	 *            the scope
	 * @return the t
	 */
	T run(IScope scope);
}