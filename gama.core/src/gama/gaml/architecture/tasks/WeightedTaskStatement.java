/*******************************************************************************************************
 *
 * WeightedTaskStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.tasks;

import java.util.Arrays;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.validation.Assert;
import gama.api.compilation.validation.ValidNameValidator;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.gaml.architecture.tasks.WeightedTaskStatement.TaskValidator;

/**
 * The Class WeightedTaskCommand. A simple definition of a task (set of commands) with a weight that can be computed
 * dynamically. Depending on the architecture in which the tasks are defined, this weight can be used to choose the
 * active task, or to define the order in which they are executed each step.
 *
 * @author drogoul
 */

@symbol (
		name = WeightedTaskStatement.TASK,
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		concept = { IConcept.BEHAVIOR, IConcept.SCHEDULER, IConcept.TASK_BASED, IConcept.ARCHITECTURE })
@inside (
		symbols = { WeightedTasksArchitecture.WT, SortedTasksArchitecture.ST, ProbabilisticTasksArchitecture.PT },
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = WeightedTaskStatement.WEIGHT,
				type = IType.FLOAT,
				optional = false,
				doc = @doc ("the priority level of the task")),
				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = false,
						doc = @doc ("the identifier of the task")) },
		omissible = IKeyword.NAME)
@validator (TaskValidator.class)
@doc ("As reflex, a task is a sequence of statements that can be executed, at each time step, by the agent. If an agent owns several tasks, the scheduler chooses a task to execute based on its current priority weight value.")
public class WeightedTaskStatement extends AbstractStatementSequence {

	/** The Allowed architectures. */
	final static List<String> ALLOWED_ARCHITECTURES =
			Arrays.asList(SortedTasksArchitecture.ST, WeightedTasksArchitecture.WT, ProbabilisticTasksArchitecture.PT);

	/**
	 * The Class TaskValidator.
	 */
	public static class TaskValidator extends ValidNameValidator {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription description) {
			if (!Assert.nameIsValid(description)) return;
			// Verify that the task is inside a species with task-based control
			final ITypeDescription species = description.getTypeContext();
			if (!species.isSpecies()) {
				description.error("A " + description.getKeyword() + " can only be defined in a species",
						IGamlIssue.WRONG_CONTEXT);
			}
			final ISkillDescription control = ((ISpeciesDescription) species).getControl();
			if (!WeightedTasksArchitecture.class.isAssignableFrom(control.getJavaBase())) {
				description.error("A " + description.getKeyword()
						+ " can only be defined in a task-controlled species  (one of" + ALLOWED_ARCHITECTURES + ")",
						IGamlIssue.WRONG_CONTEXT);
			}
		}
	}

	/** The Constant WEIGHT. */
	protected static final String WEIGHT = "weight";

	/** The Constant TASK. */
	protected static final String TASK = "task";

	/** The weight. */
	protected IExpression weight;

	/**
	 * Instantiates a new weighted task statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public WeightedTaskStatement(final IDescription desc) {
		super(desc);
		setName(desc.getName());
		weight = getFacet(WEIGHT);
	}

	/**
	 * Compute weight.
	 *
	 * @param scope
	 *            the scope
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public Double computeWeight(final IScope scope) throws GamaRuntimeException {
		return Cast.asFloat(scope, weight.value(scope));
	}

}
