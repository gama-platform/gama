/*******************************************************************************************************
 *
 * InteractiveConsoleSession.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IVarDescriptionProvider;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.PlatformAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IExecutionContext;
import gama.api.runtime.scope.IScope;

/** Persistent execution and variable state for an interactive console session. */
final class InteractiveConsoleSession implements IExecutionContext, IVarDescriptionProvider {

	private final Map<String, Object> variables = new LinkedHashMap<>();
	private final Map<String, IType<?>> variableTypes = new LinkedHashMap<>();
	private final ThreadLocal<ITopLevelAgent> evaluationAgent = new ThreadLocal<>();
	private final List<IDescription> deferredProviderDetaches = new ArrayList<>();

	private ITopLevelAgent activeAgent;
	private IDescription alternateVarDescription;
	private boolean evaluationActive;
	private boolean clearVariablesWhenEvaluationEnds;

	synchronized void setAgent(final ITopLevelAgent agent) {
		final IDescription previousDescription = alternateVarDescription;
		alternateVarDescription = null;
		if (previousDescription != null && previousDescription.getAlternateVarProvider() == this) {
			if (evaluationActive) {
				if (!deferredProviderDetaches.contains(previousDescription)) {
					deferredProviderDetaches.add(previousDescription);
				}
			} else {
				previousDescription.attachAlternateVarDescriptionProvider(null);
			}
		}

		activeAgent = agent;
		if (agent != null && !(agent instanceof PlatformAgent)) {
			alternateVarDescription = agent.getSpecies().getDescription();
			alternateVarDescription.attachAlternateVarDescriptionProvider(this);
		}
		if (evaluationActive) {
			clearVariablesWhenEvaluationEnds = true;
		} else {
			clearLocalVars();
			clearVariablesWhenEvaluationEnds = false;
		}
	}

	synchronized void beginEvaluation(final ITopLevelAgent agent) {
		evaluationActive = true;
		evaluationAgent.set(agent);
	}

	synchronized void endEvaluation() {
		evaluationAgent.remove();
		evaluationActive = false;
		if (clearVariablesWhenEvaluationEnds) {
			clearLocalVars();
			clearVariablesWhenEvaluationEnds = false;
		}
		for (final IDescription description : deferredProviderDetaches) {
			if (description != alternateVarDescription && description.getAlternateVarProvider() == this) {
				description.attachAlternateVarDescriptionProvider(null);
			}
		}
		deferredProviderDetaches.clear();
	}

	synchronized void close() {
		setAgent(null);
	}

	@Override
	public IScope getScope() {
		ITopLevelAgent agent = evaluationAgent.get();
		if (agent == null) {
			agent = GAMA.getCurrentTopLevelAgent();
			synchronized (this) {
				if (agent != activeAgent) { setAgent(agent); }
			}
		}
		return agent == null ? GAMA.getRuntimeScope() : agent.getScope();
	}

	@Override
	public void dispose() { /* Console bindings persist between statement contexts. */ }

	@Override
	public IExecutionContext getOuterContext() { return this; }

	@Override
	public IExecutionContext createCopy(final ISymbol command) { return this; }

	@Override
	public IExecutionContext createChildContext(final ISymbol command) { return this; }

	@Override
	public int depth() { return 0; }

	@Override
	public void setTempVar(final String name, final Object value) { variables.put(name, value); }

	@Override
	public Object getTempVar(final String name) { return variables.get(name); }

	@Override
	public Map<? extends String, ? extends Object> getLocalVars() { return variables; }

	@Override
	public void clearLocalVars() {
		variables.clear();
		variableTypes.clear();
	}

	@Override
	public void putLocalVar(final String name, final Object value) { variables.put(name, value); }

	@Override
	public Object getLocalVar(final String name) { return variables.get(name); }

	@Override
	public boolean hasLocalVar(final String name) { return variables.containsKey(name); }

	@Override
	public void removeLocalVar(final String name) { /* REPL variables persist until the session is cleared. */ }

	@Override
	public ISymbol getCurrentSymbol() { return null; }

	@Override
	public void setCurrentSymbol(final ISymbol statement) {}

	@Override
	public IExpression getVarExpr(final String name, final boolean asField) {
		if (!variables.containsKey(name)) return null;
		IType<?> type = variableTypes.get(name);
		if (type == null) {
			type = GamaType.of(variables.get(name));
			if (type != null && type != Types.NO_TYPE) { variableTypes.put(name, type); }
		}
		return GAML.getExpressionFactory().createVar(name, type, false, IVarExpression.Category.TEMP, null);
	}

	@Override
	public boolean hasAttribute(final String name) { return variables.containsKey(name); }
}
