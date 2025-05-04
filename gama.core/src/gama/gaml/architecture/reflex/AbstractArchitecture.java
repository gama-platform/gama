/*******************************************************************************************************
 *
 * AbstractArchitecture.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.reflex;

import gama.core.runtime.IScope;
import gama.gaml.architecture.IArchitecture;
import gama.gaml.descriptions.IDescription;
import gama.gaml.skills.Skill;
import gama.gaml.species.ISpecies;

/**
 * The Class AbstractArchitecture.
 */
public abstract class AbstractArchitecture extends Skill implements IArchitecture {

	/**
	 * Instantiates a new abstract architecture.
	 */
	public AbstractArchitecture(final IDescription desc) {
		super(desc);
	}

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
	public boolean hasFacet(final String key) {
		return false;
	}

	@Override
	public void verifyBehaviors(final ISpecies context) {}

	@Override
	public void dispose() {}

}