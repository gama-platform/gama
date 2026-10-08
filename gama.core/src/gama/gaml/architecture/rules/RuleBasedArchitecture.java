/*******************************************************************************************************
 *
 * RuleBasedArchitecture.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.rules;

import static one.util.streamex.StreamEx.of;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gama.annotations.doc;
import gama.annotations.skill;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.gaml.architecture.reflex.ReflexArchitecture;

/**
 * The class RuleBasedArchitecture. A simple architecture of competing rules. Conditions and priorities of the rules are
 * computed every step and the rules executed are the ones which fulfill their condition, in the order of their
 * priorities
 *
 * task t1 weight: a_float { ... } task t2 weight: another_float {...}
 *
 * @author drogoul
 * @since 21 dec. 2011
 *
 */
@skill (
		name = RuleBasedArchitecture.RULES,
		concept = { IConcept.ARCHITECTURE, IConcept.BEHAVIOR })
@doc ("A control architecture based on the concept of rules. Allows to declare simple rules with the keyword `do_rule` and to execute them given with respect to their conditions and priority")
public class RuleBasedArchitecture extends ReflexArchitecture {

	/** The Constant RULES. */
	public static final String RULES = "rules";

	/** The rules. */
	List<RuleStatement> rules = new ArrayList<>();

	@Override
	protected void clearBehaviors() {
		super.clearBehaviors();
		rules.clear();
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		// we let a chance to the reflexes, etc. to execute
		super.executeOn(scope);
		final Map<RuleStatement, Double> priorities = of(rules).toMap(r -> r.computePriority(scope));
		final List<RuleStatement> rulesToRun = of(rules).filter(r -> r.computeCondition(scope))
				.reverseSorted((o1, o2) -> priorities.get(o1).compareTo(priorities.get(o2))).toList();
		Object result = null;
		for (final RuleStatement rule : rulesToRun) {
			final IExecutionResult er = scope.execute(rule);
			if (!er.passed()) return result;
			result = er.getValue();
		}
		return result;
	}

	@Override
	public void addBehavior(final IStatement c) {
		if (c instanceof RuleStatement) {
			rules.add((RuleStatement) c);
		} else {
			super.addBehavior(c);
		}
	}

}
