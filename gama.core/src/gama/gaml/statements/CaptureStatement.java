/*******************************************************************************************************
 *
 * CaptureStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.ArrayList;
import java.util.List;

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
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.FlowStatus;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.CaptureStatement.CaptureValidator;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class CaptureStatement.
 */
@symbol (
		name = IKeyword.CAPTURE,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		breakable = true,
		continuable = true,
		concept = { IConcept.MULTI_LEVEL },
		remote_context = true)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
				doc = @doc ("an expression that is evaluated as an agent or a list of the agent to be captured"),
				name = IKeyword.TARGET,
				type = { IType.AGENT, IType.CONTAINER },
				of = IType.AGENT,
				optional = false),
				@facet (
						name = IKeyword.AS,
						type = IType.SPECIES,
						optional = true,
						doc = @doc ("the species that the captured agent(s) will become, this is a micro-species of the calling agent's species")),
				@facet (
						name = IKeyword.RETURNS,
						type = IType.NEW_TEMP_ID,
						optional = true,
						doc = @doc ("a list of the newly captured agent(s)")) },
		omissible = IKeyword.TARGET)
@doc (
		value = "Allows an agent to capture other agent(s) as its micro-agent(s).",
		usages = {

				@usage (
						value = "The preliminary for an agent A to capture an agent B as its micro-agent is that the A's species must defined a micro-species which is a sub-species of B's species (cf. [Species161#Nesting_species Nesting species]).",
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
										value = "...",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "To capture all \"A\" agents as \"C\" agents, we can ask an \"B\" agent to execute the following statement:",
						examples = { @example (
								value = "capture list(B) as: C;",
								isExecutable = false) }),
				@usage (
						value = "Deprecated writing:",
						examples = { @example (
								value = "capture target: list (B) as: C;",
								isExecutable = false) }) },
		see = { "release" })
@validator (CaptureValidator.class)
public class CaptureStatement extends AbstractStatementSequence {

	/**
	 * The Class CaptureValidator.
	 */
	public static class CaptureValidator implements IDescriptionValidator<StatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final StatementDescription cd) {
			final String microSpeciesName = cd.getLitteral(AS);
			if (microSpeciesName != null) {
				final SpeciesDescription macroSpecies = cd.getSpeciesContext();
				final TypeDescription microSpecies = macroSpecies.getMicroSpecies(microSpeciesName);
				if (microSpecies == null) {
					cd.error(macroSpecies.getName() + " species doesn't contain " + microSpeciesName
							+ " as micro-species", IGamlIssue.UNKNOWN_SPECIES, AS, microSpeciesName);
				}
			}

		}
	}

	/** The target. */
	private IExpression target;

	/** The return string. */
	private final String returnString;

	/** The micro species name. */
	private String microSpeciesName = null;

	/** The sequence. */
	private RemoteSequence sequence = null;

	/**
	 * Instantiates a new capture statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public CaptureStatement(final IDescription desc) {
		super(desc);
		target = getFacet(IKeyword.TARGET);
		microSpeciesName = getLiteral(IKeyword.AS);
		returnString = getLiteral(IKeyword.RETURNS);
		if (hasFacet(IKeyword.TARGET)) { setName(IKeyword.CAPTURE + " " + getFacet(IKeyword.TARGET).serializeToGaml(false)); }
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
		final IList<IAgent> microAgents = GamaListFactory.create(Types.AGENT);
		final IMacroAgent macroAgent = (IMacroAgent) scope.getAgent();
		final ISpecies macroSpecies = macroAgent.getSpecies();

		final Object t = target.value(scope);

		if (t == null) return null;

		if (t instanceof IContainer) {
			for (final Object o : ((IContainer<?, ?>) t).iterable(scope)) {
				if (o instanceof IAgent) { microAgents.add((IAgent) o); }
			}
		} else if (t instanceof IAgent) { microAgents.add((IAgent) t); }

		final List<IAgent> removedComponents = GamaListFactory.create(Types.AGENT);
		List<IAgent> capturedAgents = GamaListFactory.create();

		if (!microAgents.isEmpty()) {
			if (microSpeciesName != null) { // micro-species name is specified
											// in the "as" facet.
				final ISpecies microSpecies = macroSpecies.getMicroSpecies(microSpeciesName);

				if (microSpecies == null) throw GamaRuntimeException.error(this.name
						+ " can't capture other agents as members of " + microSpeciesName + " population because the "
						+ microSpeciesName + " population is not visible or doesn't exist.", scope);

				final IPopulation<? extends IAgent> microPopulation = macroAgent.getPopulationFor(microSpecies);
				if (microPopulation == null) throw GamaRuntimeException.error(this.name
						+ " can't capture other agents as members of " + microSpeciesName + " population because the "
						+ microSpeciesName + " population is not visible or doesn't exist.", scope);

				for (final IAgent c : microAgents) {
					if (!macroAgent.canCapture(c, microSpecies)) { removedComponents.add(c); }
				}

				if (!removedComponents.isEmpty()) { microAgents.removeAll(removedComponents); }

				if (!microAgents.isEmpty()) {
					capturedAgents = macroAgent.captureMicroAgents(scope, microSpecies, microAgents);
					microAgents.clear();

					// scope.addVarWithValue(IKeyword.MYSELF, macroAgent);
					if (!capturedAgents.isEmpty() && sequence != null && !sequence.isEmpty()) {
						for (final IAgent capturedA : capturedAgents) {
							if (!scope.execute(sequence, capturedA, null).passed()) { break; }
						}
					}
				}
			} else { // micro-species name is not specified in the "as" facet.
				ISpecies microSpecies;
				IAgent capturedAgent;
				// scope.addVarWithValue(IKeyword.MYSELF, macroAgent);
				for (final IAgent c : microAgents) {
					microSpecies = macroSpecies.getMicroSpecies(c.getSpeciesName());

					if (microSpecies != null) {
						capturedAgent = macroAgent.captureMicroAgent(scope, microSpecies, c);

						if (sequence != null && !sequence.isEmpty()) {
							scope.execute(sequence, capturedAgent, null);
							if (scope.interrupted() || scope.getAndClearBreakStatus() == FlowStatus.BREAK) { break; }
						}

						capturedAgents.add(capturedAgent);
					} else {
						removedComponents.add(c);
					}
				}
			}
		}

		if (returnString != null) { scope.setVarValue(returnString, capturedAgents); }

		// throw GamaRuntimeException if necessary
		if (!removedComponents.isEmpty()) {
			final List<String> raStr = new ArrayList<>();
			for (final IAgent ra : removedComponents) {
				raStr.add(ra.getName());
				raStr.add(", ");
			}
			raStr.remove(raStr.size() - 1);

			// final StringBuilder raB = new StringBuilder();
			// for (final String s : raStr) { raB.append(s); }

			if (microSpeciesName != null) throw GamaRuntimeException.error(
					macroAgent.getName() + " can't capture " + raStr.toString() + " as " + microSpeciesName + " agent",
					scope);
			throw GamaRuntimeException.error(macroAgent.getName() + " can't capture " + raStr.toString()
					+ " as micro-agents because no appropriate micro-population is found to welcome these agents.",
					scope);
		}

		return capturedAgents;
	}

	@Override
	public void dispose() {
		target = null;
		microSpeciesName = null;
		super.dispose();
	}
}