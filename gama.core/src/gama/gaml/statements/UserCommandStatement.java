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
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.FluentIterable;

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
import gama.api.compilation.IInternalFacets;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.ValidNameValidator;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.Invocation;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.gaml.statements.UserCommandStatement.UserCommandValidator;

/**
 * Written by drogoul Modified on 7 févr. 2010
 *
 * <p>
 * <b>Thread-safety:</b> this class is safe for concurrent use by multiple threads (e.g. parallel simulations). The
 * shared mutable state is protected as follows:
 * <ul>
 * <li>{@link #args} – the formal {@link Arguments} set once by {@link #setFormalArgs(Arguments)} is held in an
 * {@link AtomicReference} so that concurrent reads inside {@link #privateExecuteIn(IScope)} always observe a consistent
 * reference.</li>
 * <li>the runtime arguments are never stored in the statement: they are handed to each call by the scope performing it
 * (see {@link Invocation}).</li>
 * <li>{@link #category} – lazily initialised; declared {@code volatile} so the initialised value is immediately visible
 * to all threads.</li>
 * </ul>
 * </p>
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
						name = IKeyword.OF,
						type = IType.LABEL,
						values = { IKeyword.EXPERIMENT, IKeyword.SIMULATION },
						optional = true,
						doc = @doc ("Only for a command directly declared in an experiment. Selects its execution context: the current simulation (default) or the experiment. Names, actions, arguments, the block and dynamic facets use that owner; there is no implicit fallback.")),
				@facet (
						name = IKeyword.ACTION,
						type = IType.LABEL,
						optional = true,
						doc = @doc ("the identifier of an action accessible in the selected execution context")),
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
		value = "A user_command can call an action or execute a block. When directly declared in an experiment, of: simulation (the default) runs it in the current simulation, while of: experiment runs it in the experiment. Its expressions, action and block use the selected context, without fallback to the other owner. A simulation command runs on one simulation, not all of them, and reports an error if no simulation is available. Use explicit experiment references or ask to cross contexts. Commands declared in global, a species or a user_panel retain their declaring agent's context.",
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
			if (action != null && action.contains(IInternalFacets.SYNTHETIC)) {
				description.warning(
						"This use of 'action' is deprecated. Move the sequence to execute at the end of the 'user_command' statement instead.",
						IGamlIssue.DEPRECATED, ACTION, (String[]) null);
			}
			final IDescription enclosing = description.getEnclosingDescription();
			if (description.hasFacet(IKeyword.OF) && !(enclosing instanceof IExperimentDescription)) {
				description.error("The 'of' facet is only available for commands declared in an experiment.",
						IGamlIssue.WRONG_CONTEXT, IKeyword.OF);
			}
			final IDescription target = enclosing instanceof IExperimentDescription
					&& !IExperimentDescription.isExperimentContext(description) ? enclosing.getModelDescription() : enclosing;
			if (action != null && target.getAction(action) == null) {
				final String targetName = target instanceof IModelDescription ? "global" : target.getName();
				description.error("Action " + action + " does not exist in " + targetName,
						IGamlIssue.UNKNOWN_ACTION, ACTION);
			}
		}
	}

	/**
	 * The formal args. Uses {@link AtomicReference} so that {@link #setFormalArgs(Arguments)} and the reads inside
	 * {@link #privateExecuteIn(IScope)} are safe when called from multiple threads.
	 */
	final AtomicReference<Arguments> args = new AtomicReference<>();

	/** The action name. */
	final String actionName;

	private final boolean declaredInExperiment;
	private final boolean experimentOwner;

	/**
	 * The category. Declared {@code volatile} because it can be lazily initialised by {@link #getCategory()} after
	 * construction.
	 */
	volatile String category;

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
		declaredInExperiment = desc.getEnclosingDescription() instanceof IExperimentDescription;
		experimentOwner = declaredInExperiment && IExperimentDescription.isExperimentContext(desc);
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
	public void setFormalArgs(final Arguments args) {
		this.args.set(args);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		for (final ISymbol c : children) { if (c instanceof UserInputStatement ip) { inputs.add(ip); } }
		super.setChildren(FluentIterable.from(children).filter(each -> !inputs.contains(each)));
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final Invocation invocation = scope.takeInvocation(this);
		final Arguments currentRuntimeArgs = invocation == null ? null : invocation.args();
		if (declaredInExperiment) {
			final ITopLevelAgent target = getExecutionTarget(scope);
			if (scope.getRoot() != target || scope.getAgent() != target) {
				Arguments forwarded = currentRuntimeArgs;
				if (forwarded != null) {
					final var caller = invocation.caller();
					final boolean pushed = caller != null && scope.push(caller);
					try {
						forwarded = forwarded.evaluateAgainst(scope);
					} finally {
						if (pushed) { scope.pop(caller); }
					}
				}
				return scope.execute(this, target, true, forwarded).getValue();
			}
		}
		if (isEnabled(scope)) {
			if (actionName == null) {
				if (currentRuntimeArgs != null) { scope.stackArguments(currentRuntimeArgs, invocation.caller()); }
				for (final IStatement s : inputs) { if (!scope.execute(s).passed()) return null; }
				return super.privateExecuteIn(scope);
			}
			final ISpecies context = scope.getAgent().getSpecies();
			final IStatement.WithArgs executer = context.getAction(actionName);
			if (executer == null) throw GamaRuntimeException.error("Unknown action: " + actionName, scope);
			final Arguments tempArgs = new Arguments(args.get());
			if (currentRuntimeArgs != null) { tempArgs.complementWith(currentRuntimeArgs); }
			return scope.execute(executer, tempArgs).getValue();
		}
		return null;
	}

	private ITopLevelAgent owner(final IScope scope) {
		if (!declaredInExperiment) return scope.getRoot();
		final IExperimentAgent experiment = scope.getExperiment();
		if (experiment == null) return null;
		if (experimentOwner) return experiment;
		if (scope.getRoot() instanceof ISimulationAgent simulation && simulation.getExperiment() == experiment)
			return simulation;
		return experiment.getSimulation();
	}

	/** Returns the selected owner for a command declared in an experiment. */
	public ITopLevelAgent getExecutionTarget(final IScope scope) {
		final ITopLevelAgent target = owner(scope);
		if (target == null || target.dead())
			throw GamaRuntimeException.error("No " + (experimentOwner ? "experiment" : "simulation")
					+ " is available for user_command " + getName(), scope);
		return target;
	}

	private Object evaluateFacet(final IScope scope, final IExpression expression) {
		if (!declaredInExperiment || expression.isConst()) return expression.value(scope);
		final ITopLevelAgent target = getExecutionTarget(scope);
		if (scope.getRoot() == target && scope.getAgent() == target) return expression.value(scope);
		try (IScope evaluation = target.getScope().detachedCopy("in user_command " + getName())) {
			return expression.value(evaluation);
		}
	}

	/**
	 * Checks if is enabled.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is enabled
	 */
	public boolean isEnabled(final IScope scope) {
		if (declaredInExperiment) {
			final ITopLevelAgent target = owner(scope);
			if (target == null || target.dead()) return false;
		}
		return when == null || Cast.asBool(scope, evaluateFacet(scope, when));
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
		if (declaredInExperiment && !exp.isConst()) {
			final ITopLevelAgent target = owner(scope);
			if (target == null || target.dead()) return null;
		}
		return GamaColorFactory.castToColor(scope, evaluateFacet(scope, exp));
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
		if (declaredInExperiment && !exp.isConst()) {
			final ITopLevelAgent target = owner(scope);
			if (target == null || target.dead()) return false;
		}
		return Cast.asBool(scope, evaluateFacet(scope, exp));
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
	public boolean isDefinedInExperiment() { return experimentOwner; }

}