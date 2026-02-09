/*******************************************************************************************************
 *
 * AbstractArchitecture.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.skill;

import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * The Class AbstractArchitecture.
 */
public abstract class AbstractArchitecture extends Skill implements IArchitecture {

	/**
	 * Instantiates a new abstract architecture.
	 */
	public AbstractArchitecture() {}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	@Override
	public String getKeyword() { return getName(); }

	@Override
	public String getTrace(final IScope scope) {
		return "";
	}

	@Override
	public IExpression getFacet(final String... key) {
		return null;
	}

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @return the facet value
	 */

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the facet value
	 */

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the facet value
	 */

	/**
	 * Gets the facet value.
	 *
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the facet value
	 */
	@Override
	public <T> T getFacetValue(final IScope scope, final String key, final T defaultValue) {
		return null;
	}

	@Override
	public boolean hasFacet(final String key) {
		return false;
	}

	@Override
	public void verifyBehaviors(final ISpecies context) {}

	@Override
	public void dispose() {}

}