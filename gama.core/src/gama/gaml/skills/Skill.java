/*******************************************************************************************************
 *
 * Skill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import org.apache.commons.lang3.NotImplementedException;

import gama.core.common.interfaces.ISkill;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SkillDescription;


/**
 * The Class Skill.
 */
public class Skill extends Symbol implements ISkill {

	/** The description. */
	protected SkillDescription description;

	/**
	 * Instantiates a new skill.
	 */
	protected Skill() { super(null); } // TODO: check
	
	public Skill(IDescription desc) {
		super(desc);
	}

	@Override
	public void setName(final String newName) {}

	/**
	 * Sets the description.
	 *
	 * @param desc
	 *            the new description
	 */
	public void setDescription(final SkillDescription desc) { description = desc; }

	@Override
	public Doc getDocumentation() { return description.getDocumentation(); }

	@Override
	public SkillDescription getDescription() { return description; }

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
	public void setChildren(Iterable<? extends ISymbol> children) {
		throw new NotImplementedException("setChildren not implemented yet for Skill");
	}
	
	@Override
	public String getName() { return description.getName(); }

}
