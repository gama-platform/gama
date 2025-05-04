/*******************************************************************************************************
 *
 * Skill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import gama.core.common.interfaces.ISkill;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;

/**
 * The Class Skill.
 */
public class Skill extends Symbol implements ISkill {

	/**
	 * @param desc
	 */
	public Skill(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setName(final String newName) {}

	@Override
	public Doc getDocumentation() { return description.getDocumentation(); }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	/**
	 * Gets the current agent.
	 *
	 * @param scope
	 *            the scope
	 * @return the current agent
	 */
	protected IAgent getCurrentAgent(final IScope scope) {
		return scope.getAgent();
	}

	/**
	 * Gets the topology.
	 *
	 * @param agent
	 *            the agent
	 * @return the topology
	 */
	protected ITopology getTopology(final IAgent agent) {
		return agent.getTopology();
	}

	@Override
	public String getTitle() { return description.getTitle(); }

	@Override
	public String getDefiningPlugin() { return description.getDefiningPlugin(); }

	@Override
	public String getName() { return description.getName(); }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		// TODO Auto-generated method stub

	}

}
