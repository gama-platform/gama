/*******************************************************************************************************
 *
 * AskStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.ExecutionResult;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.IStatement.Breakable;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

// A group of commands that can be executed on remote agents.

/**
 * The Class AskStatement.
 */
@symbol (
		name = IKeyword.ASK,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		breakable = true,
		continuable = true,
		remote_context = true,
		concept = { IConcept.SPECIES })
@facets (
		value = { @facet (
				name = IKeyword.PARALLEL,
				type = { IType.BOOL, IType.INT },
				optional = true,
				doc = @doc ("(experimental) setting this facet to 'true' will allow 'ask' to use concurrency when traversing the targets; setting it to an integer will set the threshold under which they will be run sequentially (the default is initially 20, but can be fixed in the preferences). This facet is false by default.")),
				@facet (
						name = IKeyword.TARGET,
						type = { IType.CONTAINER, IType.AGENT },
						of = IType.AGENT,
						optional = false,
						doc = @doc ("an expression that evaluates to an agent or a list of agents")),
				@facet (
						name = IKeyword.AS,
						type = { IType.SPECIES },
						optional = true,
						doc = @doc ("an expression that evaluates to a species")) },
		omissible = IKeyword.TARGET)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER, ISymbolKind.OUTPUT },
		symbols = IKeyword.CHART)
@doc (
		value = "Allows an agent, the sender agent (that can be the [Sections161#global world agent]), to ask another (or other) agent(s) to perform a set of statements. If the value of the target facet is nil or empty, the statement is ignored.",
		usages = { @usage (
				name = "Ask agents",
				value = "Ask  a set of receiver agents, stored in a container, to perform a block of statements. The block is evaluated in the context of the agents' species",
				examples = { @example (
						value = "ask ${receiver_agents} {",
						isExecutable = false,
						isPattern = true),
						@example (
								value = "     ${cursor}",
								isExecutable = false,
								isPattern = true),
						@example (
								value = "}",
								isExecutable = false,
								isPattern = true) }),
				@usage (
						name = "Ask one agent",
						menu = usage.CUSTOM,
						path = "Special",
						value = "Ask  one agent to perform a block of statements. The block is evaluated in the context of the agent's species",
						examples = { @example (
								value = "ask ${one_agent} {",
								isExecutable = false,
								isPattern = true),
								@example (
										value = "     ${cursor}",
										isExecutable = false,
										isPattern = true),
								@example (
										value = "}",
										isExecutable = false,
										isPattern = true) }),
				@usage (
						name = "Ask agents and force their species",
						value = "If the species of the receiver agent(s) cannot be determined, it is possible to force it using the `as` facet. An error is thrown if an agent is not a direct or undirect instance of this species",
						examples = { @example (
								value = "ask ${receiver_agent(s)} as: ${a_species_expression} {",
								isExecutable = false,
								isPattern = true),
								@example (
										value = "     ${cursor}",
										isExecutable = false,
										isPattern = true),
								@example (
										value = "}",
										isExecutable = false,
										isPattern = true) }),
				@usage (
						name = "Ask one agent casted to a species",
						value = "If the species of the receiver agent cannot be determined, it is possible to force it by casting the agent. Nothing happens if the agent cannot be casted to this species",
						pattern = "ask ${species_name}(${receiver_agent}) {\n\t ${cursor}\n}"),
				@usage (
						name = "Ask agents belonging to a species",
						value = "To ask a set of agents to do something only if they belong to a given species, the `of_species` operator can be used. If none of the agents belong to the species, nothing happens",
						examples = { @example (
								value = "ask ${receiver_agents} of_species ${species_name} {",
								isExecutable = false,
								isPattern = true),
								@example (
										value = "     ${cursor}",
										isExecutable = false,
										isPattern = true),
								@example (
										value = "}",
										isExecutable = false,
										isPattern = true) }),
				@usage (
						value = "Any statement can be declared in the block statements. All the statements will be evaluated in the context of the receiver agent(s), as if they were defined in their species, which means that an expression like `self` will represent the receiver agent and not the sender. If the sender needs to refer to itself, some of its own attributes (or temporary variables) within the block statements, it has to use the keyword `myself`.",
						examples = { @example (
								value = "species animal {",
								isExecutable = false),
								@example (
										value = "    float energy <- rnd (1000) min: 0.0;",
										isExecutable = false),
								@example (
										value = "    reflex when: energy > 500 { // executed when the energy is above the given threshold",
										isExecutable = false),
								@example (
										value = "         list<animal> others <- (animal at_distance 5); // find all the neighboring animals in a radius of 5 meters",
										isExecutable = false),
								@example (
										value = "         float shared_energy  <- (energy - 500) / length (others); // compute the amount of energy to share with each of them",
										isExecutable = false),
								@example (
										value = "         ask others { // no need to cast, since others has already been filtered to only include animals",
										isExecutable = false),
								@example (
										value = "              if (energy < 500) { // refers to the energy of each animal in others",
										isExecutable = false),
								@example (
										value = "                   energy <- energy + myself.shared_energy; // increases the energy of each animal",
										isExecutable = false),
								@example (
										value = "                   myself.energy <- myself.energy - myself.shared_energy; // decreases the energy of the sender",
										isExecutable = false),
								@example (
										value = "              }",
										isExecutable = false),
								@example (
										value = "         }",
										isExecutable = false),
								@example (
										value = "    }",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) })

public class AskStatement extends AbstractStatementSequence implements Breakable {

	/** The sequence. */
	private RemoteSequence sequence = null;

	/** The target. */
	private final IExpression target;

	/** The parallel. */
	private final IExpression parallel;

	/**
	 * Instantiates a new ask statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public AskStatement(final IDescription desc) {
		super(desc);
		target = getFacet(IKeyword.TARGET);
		parallel = getFacet("parallel");
		if (target != null) { setName("ask " + target.serializeToGaml(false)); }
		// targetsSimulations = targetsSimulations();
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> com) {
		sequence = new RemoteSequence(description);
		sequence.setName("commands of " + getName());
		sequence.setChildren(com);
	}

	@SuppressWarnings ({ "rawtypes", "unchecked" })
	@Override
	public Object privateExecuteIn(final IScope scope) {
		final Object t = target.value(scope);
		if (t instanceof IContainer ic) {
			GamaExecutorService.execute(scope, sequence,
					((IContainer<?, IAgent>) ic).listValue(scope, Types.AGENT, false), parallel);
			return this;
		}
		IAgent agent;
		if (t instanceof IAgent) {
			agent = (IAgent) t;
		} else {
			agent = Cast.asAgent(scope, t);
			if (agent == null) throw GamaRuntimeException.error("Can not execute ask on a nil agent", scope);
		}
		final ExecutionResult result = scope.execute(sequence, agent, null);
		return result.getValue();
	}

	@Override
	public void dispose() {
		sequence.dispose();
		super.dispose();
	}

}