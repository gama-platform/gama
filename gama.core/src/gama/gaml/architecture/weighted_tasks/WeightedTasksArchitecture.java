/*******************************************************************************************************
 *
 * WeightedTasksArchitecture.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.architecture.weighted_tasks;

import java.util.ArrayList;
import java.util.List;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.architecture.reflex.ReflexArchitecture;
import gama.gaml.statements.IStatement;

/**
 * The class WeightedTasksArchitecture. A simple architecture of competing tasks, where one can be active at a time.
 * Weights of the tasks are computed every step and the chosen task is simply the one with the maximal weight.
 * 
 * task t1 weight: a_float { ... } task t2 weight: another_float {...}
 * 
 * @author drogoul
 * @since 21 dec. 2011
 * 
 */
@skill (
		name = WeightedTasksArchitecture.WT,
		concept = { IConcept.ARCHITECTURE, IConcept.BEHAVIOR, IConcept.TASK_BASED })
@doc ("The class WeightedTasksArchitecture. A simple architecture of competing tasks, where one can be active at a time. Weights of the tasks are computed every step and the chosen task is simply the one with the maximal weight")
public class WeightedTasksArchitecture extends ReflexArchitecture {

	/** The Constant WT. */
	public static final String WT = "weighted_tasks";
	
	/** The tasks. */
	List<WeightedTaskStatement> tasks = new ArrayList<>();

	@Override
	protected void clearBehaviors() {
		super.clearBehaviors();
		tasks.clear();
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		// We let inits, reflexes run
		super.executeOn(scope);
		final WeightedTaskStatement active = chooseTask(scope);
		if (active != null) { return scope.execute(active).getValue(); }
		return null;
	}

	/**
	 * Choose task.
	 *
	 * @param scope the scope
	 * @return the weighted task statement
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	protected WeightedTaskStatement chooseTask(final IScope scope) throws GamaRuntimeException {
		Double max = Double.MIN_VALUE;
		WeightedTaskStatement active = null;
		for (final WeightedTaskStatement c : tasks) {
			final Double weight = c.computeWeight(scope);
			if (weight > max) {
				active = c;
				max = weight;
			}
		}
		return active;
	}

	@Override
	public void addBehavior(final IStatement c) {
		if (c instanceof WeightedTaskStatement) {
			tasks.add((WeightedTaskStatement) c);
		} else {
			super.addBehavior(c);
		}
	}

}
