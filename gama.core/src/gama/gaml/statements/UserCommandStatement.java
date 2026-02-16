/*******************************************************************************************************
 *
 * UserCommandStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.FluentIterable;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.ValidNameValidator;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.gaml.statements.UserCommandStatement.UserCommandValidator;

/**
 * Written by drogoul Modified on 7 févr. 2010
 *
 * @todo Description
 *
 */
@symbol (
		name = { IKeyword.USER_COMMAND },
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		with_args = true,
		concept = { IConcept.GUI })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL },
		symbols = IKeyword.USER_PANEL)
@facets (
		value = { @facet (
				name = IKeyword.CONTINUE,
				type = IType.BOOL,
				optional = true,
				doc = @doc ("Whether or not the button, when clicked, should dismiss the user panel it is defined in. Has no effect in other contexts (menu, parameters, inspectors)")),
				@facet (
						name = IKeyword.COLOR,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("The color of the button to display")),
				@facet (
						name = IKeyword.CATEGORY,
						type = IType.STRING,
						optional = true,
						doc = @doc ("a category label, used to group parameters in the interface")),
				@facet (
						name = IKeyword.ACTION,
						type = IType.ACTION,
						optional = true,
						doc = @doc ("the identifier of the action to be executed. This action should be accessible in the context in which the user_command is defined (an experiment, the global section or a species). A special case is allowed to maintain the compatibility with older versions of GAMA, when the user_command is declared in an experiment and the action is declared in 'global'. In that case, all the simulations managed by the experiment will run the action in response to the user executing the command")),
				@facet (
						name = IKeyword.NAME,
						type = IType.LABEL,
						optional = false,
						doc = @doc ("the identifier of the user_command")),
				@facet (
						name = IKeyword.WHEN,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("the condition that should be fulfilled (in addition to the user clicking it) in order to execute this action")),
				@facet (
						name = IKeyword.WITH,
						type = IType.MAP,
						optional = true,
						doc = @doc ("the map of the parameters::values required by the action")) },
		omissible = IKeyword.NAME)
@doc (
		value = "Anywhere in the global block, in a species or in an (GUI) experiment, user_command statements allows to either call directly an existing action (with or without arguments) or to be followed by a block that describes what to do when this command is run.",
		usages = { @usage (
				value = "The general syntax is for example:",
				examples = @example (
						value = "user_command kill_myself action: some_action with: [arg1::val1, arg2::val2, ...];",
						isExecutable = false)) },
		see = { IKeyword.USER_INIT, IKeyword.USER_PANEL, IKeyword.USER_INPUT })
@validator (UserCommandValidator.class)

public class UserCommandStatement extends AbstractStatementSequence implements IStatement.UserCommand {

	/**
	 * The Class UserCommandValidator.
	 */
	public static class UserCommandValidator extends ValidNameValidator {

		/*
		 * (non-Javadoc)
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml. descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription description) {
			super.validate(description);
			final String action = description.getLitteral(ACTION);
			if (action != null && action.contains(SYNTHETIC)) {
				description.warning(
						"This use of 'action' is deprecated. Move the sequence to execute at the end of the 'user_command' statement instead.",
						IGamlIssue.DEPRECATED, ACTION, (String[]) null);
			}
			final IDescription enclosing = description.getEnclosingDescription();
			if (action != null && enclosing.getAction(action) == null) {
				// 2 cases: we are in a simulation or in a "normal" species and
				// we emit an error, or we are in an experiment, in which case
				// we try to see if the simulations can run it. In that case we
				// emit a warning (see Issue #1595)
				if (enclosing instanceof IExperimentDescription) {
					final IModelDescription model = enclosing.getModelDescription();
					if (model.hasAction(action, false)) {
						description.warning("Action " + action
								+ " should be defined in the experiment, not in global. To maintain the compatibility with GAMA 1.6.1, the command will execute it on all the simulations managed by this experiment",
								IGamlIssue.WRONG_CONTEXT, ACTION);
					} else {
						description.error("Action " + action + " does not exist in this experiment",
								IGamlIssue.UNKNOWN_ACTION, ACTION);
					}
				} else {
					final String enclosingName =
							enclosing instanceof IModelDescription ? "global" : enclosing.getName();
					description.error("Action " + action + " does not exist in " + enclosingName,
							IGamlIssue.UNKNOWN_ACTION, ACTION);
				}
			}
		}
	}

	/** The args. */
	Arguments args;

	/** The runtime args. */
	Arguments runtimeArgs;

	/** The action name. */
	final String actionName;

	/** The category. */
	String category;

	/** The when. */
	final IExpression when;

	/** The inputs. */
	List<UserInputStatement> inputs = new ArrayList<>();

	/**
	 * Instantiates a new user command statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public UserCommandStatement(final IDescription desc) {
		super(desc);
		setName(desc.getName());
		actionName = getLiteral(IKeyword.ACTION);
		category = desc.getLitteral(IKeyword.CATEGORY);
		when = getFacet(IKeyword.WHEN);
	}

	/**
	 * Gets the inputs.
	 *
	 * @return the inputs
	 */
	public List<UserInputStatement> getInputs() { return inputs; }

	/**
	 * Sets the formal args.
	 *
	 * @param args
	 *            the new formal args
	 */
	@Override
	public void setFormalArgs(final Arguments args) { this.args = args; }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		for (final ISymbol c : children) { if (c instanceof UserInputStatement ip) { inputs.add(ip); } }
		super.setChildren(FluentIterable.from(children).filter(each -> !inputs.contains(each)));
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		if (isEnabled(scope)) {
			// Addition of a simplification to the definition of this statement.
			if (actionName == null) {
				if (runtimeArgs != null) { scope.stackArguments(runtimeArgs); }
				// AD 2/1/16 : Addition of this to address Issue #1339
				for (final IStatement s : inputs) { if (!scope.execute(s).passed()) return null; }
				final Object result = super.privateExecuteIn(scope);
				runtimeArgs = null;
				return result;
			}
			ISpecies context = scope.getAgent().getSpecies();
			IStatement.WithArgs executer = context.getAction(actionName);
			boolean isWorkaroundForIssue1595 = false;
			if (executer == null) {
				// See Issue #1595
				if (!(context instanceof IExperimentSpecies))
					throw GamaRuntimeException.error("Unknown action: " + actionName, scope);
				context = ((IExperimentSpecies) context).getModel();
				executer = context.getAction(actionName);
				isWorkaroundForIssue1595 = true;
			}
			final Arguments tempArgs = new Arguments(args);
			if (runtimeArgs != null) { tempArgs.complementWith(runtimeArgs); }
			if (!isWorkaroundForIssue1595) {
				final Object result = scope.execute(executer, tempArgs).getValue();
				runtimeArgs = null;
				return result;
			}
			final IPopulation<ISimulationAgent> simulations = scope.getExperiment().getSimulationPopulation();
			for (final ISimulationAgent sim : simulations.iterable(scope)) { scope.execute(executer, sim, tempArgs); }
		}
		return null;
	}

	@Override
	public void setRuntimeArgs(final IScope scope, final Arguments args) {
		this.runtimeArgs = args;
	}

	/**
	 * Checks if is enabled.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is enabled
	 */
	public boolean isEnabled(final IScope scope) {
		return when == null || Cast.asBool(scope, when.value(scope));
	}

	/**
	 * Gets the color.
	 *
	 * @param scope
	 *            the scope
	 * @return the color
	 */
	@Override
	public IColor getColor(final IScope scope) {
		final IExpression exp = getFacet(IKeyword.COLOR);
		if (exp == null) return null;
		return GamaColorFactory.createFrom(scope, exp.value(scope));
	}

	/**
	 * Checks if is continue.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is continue
	 */
	public boolean isContinue(final IScope scope) {
		final IExpression exp = getFacet(IKeyword.CONTINUE);
		if (exp == null) return false;
		return Cast.asBool(scope, exp.value(scope));
	}

	@Override
	public String getCategory() {
		if (category == null) { category = UserCommand.super.getCategory(); }
		return category;
	}

	@Override
	public String getTitle() { return getName(); }

	@Override
	public String getUnitLabel(final IScope scope) {
		return "";
	}

	@Override
	public void setUnitLabel(final String label) {}

	@Override
	public boolean isDefinedInExperiment() {
		// By default. Maybe it should be computed (looking at the action in experiment or in model ?
		return false;
	}

}
