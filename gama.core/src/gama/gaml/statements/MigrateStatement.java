/*******************************************************************************************************
 *
 * MigrateStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.gaml.statements.MigrateStatement.MigrateValidator;

/**
 * This command permits agents to migrate from one population/species to another population/species and stay in the same
 * host after the migration.
 *
 * It has two mandatory parameters: + source: can be an agent, a list of agents, a agent's population to be migrated +
 * target: target species/population that source agent(s) migrate to.
 *
 * Species of source agents and target species respect the following constraints: + they are "peer" species (sharing the
 * same direct macro-species) + they have sub-species vs. parent-species relationship.
 */
@symbol (
		name = { IKeyword.MIGRATE },
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		remote_context = true,
		concept = { IConcept.MULTI_LEVEL })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
				name = IKeyword.SOURCE,
				type = { IType.AGENT, IType.SPECIES, IType.CONTAINER, IType.ID },
				of = IType.AGENT,
				optional = false,
				doc = @doc ("An agent, a species, or a container of agents to migrate. Source agents must be living micro-agents of the executing agent.")),
				@facet (
						name = IKeyword.TARGET,
						type = IType.SPECIES,
						optional = false,
						doc = @doc ("target species/population that source agent(s) migrate to.")),
				@facet (
						name = IKeyword.RETURNS,
						type = IType.NEW_TEMP_ID,
						optional = true,
						doc = @doc ("the list of returned agents in a new local variable")) },
		omissible = IKeyword.SOURCE)
@validator (MigrateValidator.class)
@doc (
		value = "This command permits agents to migrate from one population/species to another population/species and stay in the same host after the migration. Species of source agents and target species respect the following constraints: (i) they are \"peer\" species (sharing the same direct macro-species), (ii) they have sub-species vs. parent-species relationship.",
		usages = { @usage (
				value = "It can be used in a 3-levels model, in case where individual agents can be captured into group meso agents and groups into clouds macro agents. migrate is used to allows agents captured by groups to migrate into clouds. See the model 'Balls, Groups and Clouds.gaml' in the library.",
				examples = { @example (
						value = "migrate ball_in_group target: ball_in_cloud;",
						isExecutable = false) }) },
		see = { IKeyword.CAPTURE, IKeyword.RELEASE })
public class MigrateStatement extends AbstractStatementSequence {

	/**
	 * The Class MigrateValidator.
	 */
	public static class MigrateValidator implements IDescriptionValidator<IStatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IStatementDescription cd) {
			final String microSpeciesName = cd.getLitteral(TARGET);
			if (microSpeciesName != null) {
				final ITypeDescription macroSpecies = cd.getTypeContext();
				if (!macroSpecies.isSpecies()) {
					cd.error("A migrate statement can only be defined in a species", IGamlIssue.WRONG_CONTEXT);
					return;
				}
				final ITypeDescription microSpecies =
						((ISpeciesDescription) macroSpecies).getMicroSpecies(microSpeciesName);
				if (microSpecies == null) {
					cd.error(macroSpecies.getName() + " species doesn't contain " + microSpeciesName
							+ " as micro-species", IGamlIssue.UNKNOWN_SPECIES, TARGET, microSpeciesName);
				}
			}

		}
	}

	/** The source. */
	private final IExpression source;

	/** The target. */
	private final String target;

	/** The return string. */
	private final String returnString;

	/**
	 * The sequence of statements to execute on each migrated agent.
	 *
	 * <p><b>Thread-safety:</b> declared {@code volatile} so that the single write performed by
	 * {@link #setChildren(Iterable)} during construction is guaranteed to be visible to all threads
	 * that subsequently call {@link #privateExecuteIn(IScope)}, even when those threads belong to
	 * different parallel simulations sharing this statement instance.</p>
	 */
	private volatile RemoteSequence sequence = null;

	/**
	 * Instantiates a new migrate statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public MigrateStatement(final IDescription desc) {
		super(desc);

		source = getFacet(IKeyword.SOURCE);

		target = getLiteral(IKeyword.TARGET);

		returnString = getLiteral(IKeyword.RETURNS);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> com) {
		sequence = new RemoteSequence(description);
		sequence.setName("commands of " + getName());
		sequence.setChildren(com);
	}

	@Override
	public Iterable<IStatement> getSubStatements() {
		return sequence == null ? java.util.List.of() : sequence.getSubStatements();
	}

	@Override
	public Object privateExecuteIn(final IScope stack) throws GamaRuntimeException {
		// TODO Verify it is a macro agent
		final IMacroAgent executor = (IMacroAgent) stack.getAgent();
		final IList<IAgent> immigrants = GamaListFactory.create(Types.AGENT);

		final ISpecies targetMicroSpecies = executor.getSpecies().getMicroSpecies(target);
		if (targetMicroSpecies == null) throw GamaRuntimeException.error(
				"Cannot migrate agents: " + target + " is not a micro-species of " + executor.getSpeciesName(), stack);

		final Object candidates = source.value(stack);
		if (candidates instanceof ISpecies sourceMicroSpecies) {
			if (executor.getSpecies().getMicroSpecies(sourceMicroSpecies.getName()) != sourceMicroSpecies)
				throw GamaRuntimeException.error("Cannot migrate agents: the source must be a micro-species of "
						+ executor.getSpeciesName(), stack);
			immigrants.addAll(executor.migrateMicroAgents(stack, sourceMicroSpecies, targetMicroSpecies));
		} else {
			final IList<IAgent> microAgents = GamaListFactory.create(Types.AGENT);
			if (candidates instanceof IAgent agent) {
				microAgents.add(agent);
			} else if (candidates instanceof IContainer<?, ?> container) {
				for (final Object candidate : container.iterable(stack)) {
					if (!(candidate instanceof IAgent agent)) throw GamaRuntimeException
							.error("Cannot migrate agents: the source container must contain only agents", stack);
					microAgents.add(agent);
				}
			} else {
				throw GamaRuntimeException.error(
						"Cannot migrate agents: the source must be an agent, a species or a container of agents", stack);
			}
			for (final IAgent agent : microAgents) {
				if (agent.dead() || agent.getHost() != executor) throw GamaRuntimeException
						.error("Cannot migrate agents: source agents must be living micro-agents of the executing agent",
								stack);
			}
			immigrants.addAll(executor.migrateMicroAgents(stack, microAgents, targetMicroSpecies));
		}

		if (returnString != null) { stack.setVarValue(returnString, immigrants); }

		return null;
	}

	@Override
	public void dispose() {
		if (sequence != null) { sequence.dispose(); }
		sequence = null;
		super.dispose();
	}
}