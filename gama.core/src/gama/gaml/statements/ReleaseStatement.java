/*******************************************************************************************************
 *
 * ReleaseStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.agent.ISerialisedAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.FlowStatus;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.species.ISpecies;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class ReleaseStatement.
 */
@symbol (
		name = { IKeyword.RELEASE },
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		breakable = true,
		continuable = true,
		remote_context = true,
		concept = { IConcept.MULTI_LEVEL })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
				name = IKeyword.TARGET,
				type = { IType.AGENT, IType.LIST },
				of = IType.AGENT,
				optional = false,
				doc = @doc ("an expression that is evaluated as an agent/a list of the agents to be released")),
				@facet (
						name = IKeyword.AS,
						type = { IType.SPECIES },
						optional = true,
						doc = @doc ("an expression that is evaluated as a species in which the micro-agent will be released")),
				@facet (
						name = IKeyword.IN,
						type = { IType.AGENT },
						optional = true,
						doc = @doc ("an expression that is evaluated as an agent that will be the macro-agent in which micro-agent will be released, i.e. their new host")),
				@facet (
						name = IKeyword.RETURNS,
						type = IType.NEW_TEMP_ID,
						optional = true,
						doc = @doc ("a new variable containing a list of the newly released agent(s)")) },
		omissible = IKeyword.TARGET)
@doc (
		value = "Allows an agent to release its micro-agent(s). The preliminary for an agent to release its micro-agents is that species of these micro-agents are sub-species of other species (cf. [Species161#Nesting_species Nesting species]). The released agents won't be micro-agents of the calling agent anymore. Being released from a macro-agent, the micro-agents will change their species and host (macro-agent).",
		usages = { @usage (
				value = "We consider the following species. Agents of \"C\" species can be released from a \"B\" agent to become agents of \"A\" species. Agents of \"D\" species cannot be released from the \"A\" agent because species \"D\" has no parent species.",
				examples = { @example (
						value = "species A {",
						isExecutable = false),
						@example (
								value = "...",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false),
						@example (
								value = "species B {",
								isExecutable = false),
						@example (
								value = "...",
								isExecutable = false),
						@example (
								value = "   species C parent: A {",
								isExecutable = false),
						@example (
								value = "   ...",
								isExecutable = false),
						@example (
								value = "   }",
								isExecutable = false),
						@example (
								value = "   species D {",
								isExecutable = false),
						@example (
								value = "   ...",
								isExecutable = false),
						@example (
								value = "   }",
								isExecutable = false),
						@example (
								value = "...",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "To release all \"C\" agents from a \"B\" agent, agent \"C\" has to execute the following statement. The \"C\" agent will change to \"A\" agent. The won't consider \"B\" agent as their macro-agent (host) anymore. Their host (macro-agent) will the be the host (macro-agent) of the \"B\" agent.",
						examples = { @example (
								value = "release list(C);",
								isExecutable = false) }),
				@usage (
						value = "The modeler can specify the new host and the new species of the released agents:",
						examples = @example (
								value = "release list (C) as: new_species in: new host;",
								isExecutable = false)) },
		see = "capture")
@SuppressWarnings ({ "rawtypes" })
public class ReleaseStatement extends AbstractStatementSequence {

	/** The target. */
	private final IExpression target;

	/** The as expr. */
	private final IExpression asExpr;

	/** The in expr. */
	private final IExpression inExpr;

	/** The return string. */
	private final String returnString;

	/** The sequence. */
	private RemoteSequence sequence = null;

	/**
	 * Instantiates a new release statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public ReleaseStatement(final IDescription desc) {
		super(desc);
		target = getFacet(IKeyword.TARGET);
		asExpr = getFacet(IKeyword.AS);
		inExpr = getFacet(IKeyword.IN);
		returnString = getLiteral(IKeyword.RETURNS);
	}

	@Override
	public void enterScope(final IScope stack) {
		if (returnString != null) { stack.addVarWithValue(returnString, null); }
		super.enterScope(stack);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> com) {
		sequence = new RemoteSequence(description);
		sequence.setName("commands of " + getName());
		sequence.setChildren(com);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {

		final Object t = target.value(scope);
		List<IAgent> releasedMicroAgents;
		final IAgent macroAgent = scope.getAgent();
		if (t instanceof ISerialisedAgent ag) {
			releasedMicroAgents = releaseAgentPrototype(scope, macroAgent, ag);
		} else {
			final IList<IAgent> microAgents = GamaListFactory.create(Types.AGENT);
			if (t instanceof IContainer) {
				for (final Object o : ((IContainer) t).iterable(scope)) {
					if (o instanceof IAgent) { microAgents.add((IAgent) o); }
				}
			} else if (t instanceof IAgent) { microAgents.add((IAgent) t); }
			microAgents.removeIf(each -> !each.getHost().equals(macroAgent));
			releasedMicroAgents = releaseExistingAgents(scope, macroAgent, microAgents);
		}
		// scope.addVarWithValue(IKeyword.MYSELF, macroAgent);
		if (!releasedMicroAgents.isEmpty() && !sequence.isEmpty()) {
			for (final IAgent releasedA : releasedMicroAgents) {
				if (!scope.execute(sequence, releasedA, null).passed() || scope.interrupted()
						|| scope.getAndClearBreakStatus() == FlowStatus.BREAK) {
					break;
				}
			}
		}

		if (returnString != null) { scope.setVarValue(returnString, releasedMicroAgents); }

		return releasedMicroAgents;
	}

	/**
	 * Release agent prototype.
	 *
	 * @param scope
	 *            the scope
	 * @param macroAgent
	 *            the macro agent
	 * @param saved
	 *            the saved
	 * @return the list
	 */
	private List<IAgent> releaseAgentPrototype(final IScope scope, final IAgent macroAgent,
			final ISerialisedAgent saved) {
		if (asExpr == null)
			throw GamaRuntimeException.error("Cannot release agent as its destination species is not specified", scope);
		IMacroAgent targetAgent = null;
		ISpecies microSpecies = null;
		if (inExpr == null) {
			final String microSpeciesName = asExpr.literalValue();
			targetAgent = macroAgent.getHost();
			while (targetAgent != null) {
				microSpecies = targetAgent.getSpecies().getMicroSpecies(microSpeciesName);
				if (microSpecies != null) { break; }
				targetAgent = targetAgent.getHost();
			}
		} else {
			targetAgent = (IMacroAgent) inExpr.value(scope);
			microSpecies = (ISpecies) scope.evaluate(asExpr, targetAgent).getValue();
		}
		if (microSpecies == null) throw GamaRuntimeException.error(
				"Cannot release agent as " + asExpr + " cannot be interpreted as a destination population", scope);
		if (targetAgent == null) throw GamaRuntimeException
				.error("Cannot release agent as the host of its destination population is nil", scope);
		final IPopulation<? extends IAgent> microSpeciesPopulation = macroAgent.getPopulationFor(microSpecies);
		final IAgent released = saved.restoreInto(scope, microSpeciesPopulation);
		return GamaListFactory.create(scope, Types.AGENT, released);
	}

	/**
	 * Release existing agents.
	 *
	 * @param scope
	 *            the scope
	 * @param macroAgent
	 *            the macro agent
	 * @param microAgents
	 *            the micro agents
	 * @return the list
	 */
	public List<IAgent> releaseExistingAgents(final IScope scope, final IAgent macroAgent,
			final IList<IAgent> microAgents) {
		List<IAgent> releasedMicroAgents = GamaListFactory.create();
		IMacroAgent targetAgent;
		ISpecies microSpecies = null;

		if (asExpr != null && inExpr != null) {
			targetAgent = (IMacroAgent) inExpr.value(scope);
			if (targetAgent != null && !targetAgent.equals(macroAgent)) {
				microSpecies = (ISpecies) scope.evaluate(asExpr, targetAgent).getValue();
				releasedMicroAgents = targetAgent.captureMicroAgents(scope, microSpecies, microAgents);
			}
		} else if (asExpr != null && inExpr == null) {
			final String microSpeciesName = asExpr.literalValue();
			targetAgent = macroAgent.getHost();
			while (targetAgent != null) {
				microSpecies = targetAgent.getSpecies().getMicroSpecies(microSpeciesName);
				if (microSpecies != null) { break; }
				targetAgent = targetAgent.getHost();
			}
			if (microSpecies != null && targetAgent != null) {
				releasedMicroAgents = targetAgent.captureMicroAgents(scope, microSpecies, microAgents);
			}
		} else if (asExpr == null && inExpr != null) {
			targetAgent = (IMacroAgent) inExpr.value(scope);
			if (targetAgent != null && !targetAgent.equals(macroAgent)) {
				releasedMicroAgents = GamaListFactory.create(Types.AGENT);
				for (final IAgent m : microAgents) {
					microSpecies = targetAgent.getSpecies().getMicroSpecies(m.getSpeciesName());
					if (microSpecies != null) {
						releasedMicroAgents.add(targetAgent.captureMicroAgent(scope, microSpecies, m));
					}
				}
			}
		} else if (asExpr == null && inExpr == null) {
			ISpecies microAgentSpec;
			IMacroAgent macroOfMacro;
			releasedMicroAgents = GamaListFactory.create(Types.AGENT);

			for (final IAgent m : microAgents) {
				microAgentSpec = m.getSpecies();
				macroOfMacro = macroAgent.getHost();
				while (macroOfMacro != null) {
					microSpecies = macroOfMacro.getSpecies().getMicroSpecies(microAgentSpec.getName());
					if (microSpecies != null) { break; }

					macroOfMacro = macroOfMacro.getHost();
				}

				if (macroOfMacro != null && microSpecies != null) {
					releasedMicroAgents.add(macroOfMacro.captureMicroAgent(scope, microSpecies, m));
				} else {
					// TODO throw exception when target population not found to
					// release the agent
					// instead of silently failed lie this!!!
				}

			}
		}

		// TODO change the following code
		return releasedMicroAgents;
	}

	@Override
	public void dispose() {
		if (sequence != null) { sequence.dispose(); }
		sequence = null;
		super.dispose();
	}

}