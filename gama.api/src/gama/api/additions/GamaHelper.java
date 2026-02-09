/*******************************************************************************************************
 *
 * GamaHelper.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

import gama.api.compilation.IVarAndActionSupport;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * Written by drogoul Modified on 14 ao�t 2010. Modified on 23 Apr. 2013. A general purpose helper that can be
 * subclassed like a Runnable.
 *
 * @todo Description
 *
 */
@SuppressWarnings ({ "rawtypes" })
public class GamaHelper<T> implements IGamaHelper<T> {

	/** The name. */
	final String name;

	/** The skill class. */
	final Class skillClass;

	/** The delegate. */
	final IGamaHelper<T> delegate;

	/**
	 * Instantiates a new gama helper.
	 *
	 * @param clazz
	 *            the clazz
	 * @param delegate
	 *            the delegate
	 */
	public GamaHelper(final Class clazz, final IGamaHelper<T> delegate) {
		this(null, clazz, delegate);
	}

	/**
	 * Instantiates a new gama helper.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param delegate
	 *            the delegate
	 */
	public GamaHelper(final String name, final Class clazz, final IGamaHelper<T> delegate) {
		this.name = name;
		skillClass = clazz;
		this.delegate = delegate;
	}

	@Override
	public Class getSkillClass() { return skillClass; }

	@Override
	public String getName() { return name; }

	@Override
	public T run(final IScope scope, final IAgent agent, final IVarAndActionSupport skill, final Object values)
			throws GamaRuntimeException {
		if (delegate == null) return null;
		return delegate.run(scope, agent, skill, values);
	}

}