/*******************************************************************************************************
 *
 * InteractiveConsoleEvaluator.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views;

import gama.api.GAMA;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IExecutionContext;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;

/** Evaluates one GAML console command against the supplied agent execution context. */
final class InteractiveConsoleEvaluator {

	private InteractiveConsoleEvaluator() {}

	static Result evaluate(final String input, final ITopLevelAgent agent, final IScope agentScope,
			final IExecutionContext executionContext, final IExecutionContext compilationContext) {
		IScope scope = null;
		try {
			scope = agentScope.copy("In interactive console");
			scope.setExecutionContext(executionContext);
			final IExpression expression = GAML.compileExpression(input, agent, compilationContext, false);
			final String output = expression == null ? "nil"
					: StringUtils.toGaml(scope.evaluate(expression, agent).getValue(), true);
			return new Result(output, false);
		} catch (final Exception e) {
			return new Result("> Error: " + errorMessage(e), true);
		} finally {
			try {
				agent.getSpecies().removeTemporaryAction();
			} finally {
				if (scope != null) { GAMA.releaseScope(scope); }
			}
		}
	}

	private static String errorMessage(final Exception e) {
		return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
	}

	record Result(String output, boolean error) {}
}
