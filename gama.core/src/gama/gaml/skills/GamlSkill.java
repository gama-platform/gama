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

import org.eclipse.emf.common.util.URI;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.IType;

/**
 * The Class Skill.
 */
@symbol (
		name = { IKeyword.SKILL },
		kind = ISymbolKind.SKILL,
		with_sequence = true,
		concept = { IConcept.SKILL })
@inside (
		kinds = { ISymbolKind.MODEL })
@facets (
		value = {

				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = false,
						doc = @doc ("the identifier of the skill, which must be unique in the model. It is used to refer to the skill in the model.")),
				@facet (
						name = IKeyword.PARENT,
						type = IType.SKILL,
						optional = true,
						doc = @doc ("the parent skill (inheritance)")) },
		omissible = IKeyword.NAME)
@doc (
		value = "The skill statement allows modelers to define new skills in the model. A skill is a definition of associated actions and attribures, which can be attached to species and define the attributes and actions of agents with similar 'skills' in the model. Skills can inherit from other skills")

public class GamlSkill implements ISkill {

	/** The description. */
	protected SkillDescription description;

	/**
	 * Instantiates a new skill.
	 *
	 * @param desc
	 *            the desc
	 */
	protected GamlSkill() {}

	/**
	 * Instantiates a new skill.
	 */
	public GamlSkill(final IDescription desc) {
		this();
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
	public String getName() { return description.getName(); }

	@Override
	public int getOrder() { return 0; }

	@Override
	public void setOrder(final int o) {}

	@Override
	public URI getURI() { // TODO Auto-generated method stub
		return null;
	}

	@Override
	public IExpression getFacet(final String... keys) {
		return null;
	}

	@Override
	public boolean hasFacet(final String key) {
		return false;
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

	@Override
	public String getTrace(final IScope abstractScope) {
		return "";
	}

	@Override
	public String getKeyword() { // TODO Auto-generated method stub
		return IKeyword.SKILL;
	}

	@Override
	public void setEnclosing(final ISymbol enclosing) {}

}
