/*******************************************************************************************************
 *
 * SystemOfEquationsStatement.java, in gaml.extensions.maths, is part of the source code of the GAMA modeling
 * and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.maths.ode.statements;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import com.google.common.collect.Lists;

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
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.GamaMapFactory;
import gama.core.util.GamaPair;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IVarExpression;
import gama.gaml.expressions.operators.UnaryOperator;
import gama.gaml.expressions.variables.AgentVariableExpression;
import gama.gaml.operators.Cast;
import gama.gaml.species.GamlSpecies;
import gama.gaml.statements.AbstractStatementSequence;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The class SystemOfEquationsStatement. This class represents a system of equations (SingleEquationStatement) that
 * implements the interface FirstOrderDifferentialEquations and can be integrated by any of the integrators available in
 * the Apache Commons Library.
 *
 * @author drogoul
 * @since 26 janv. 2013
 *
 */
@symbol (
		name = IKeyword.EQUATION,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		unique_name = true,
		concept = { IConcept.EQUATION })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID /* CHANGE */,
				optional = false,
				doc = @doc ("the equation identifier")),
				// @facet (
				// name = IKeyword.TYPE,
				// type = IType.ID /* CHANGE */,
				// optional = true,
				// values = { "SI", "SIS", "SIR", "SIRS", "SEIR", "LV" },
				// doc = @doc (
				// value = "the choice of one among classical models (SI, SIS, SIR, SIRS, SEIR, LV)")),
				@facet (
						name = IKeyword.VARS,
						type = IType.LIST,
						optional = true,
						doc = @doc ("the list of variables used in predefined equation systems")),
				@facet (
						name = IKeyword.PARAMS,
						type = IType.LIST,
						optional = true,
						doc = @doc ("the list of parameters used in predefined equation systems")),
				@facet (
						name = IKeyword.SIMULTANEOUSLY,
						type = IType.LIST,
						of = IType.SPECIES,
						optional = true,
						doc = @doc ("a list of species containing a system of equations (all systems will be solved simultaneously)")) },
		omissible = IKeyword.NAME)
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.MODEL })
@doc (
		value = "The equation statement is used to create an equation system from several single equations.",
		usages = { @usage (
				value = "The basic syntax to define an equation system is:",
				examples = { @example (
						value = "float t;",
						isExecutable = false),
						@example (
								value = "float S;",
								isExecutable = false),
						@example (
								value = "float I;",
								isExecutable = false),
						@example (
								value = "equation SI { ",
								isExecutable = false),
						@example (
								value = "   diff(S,t) = (- 0.3 * S * I / 100);",
								isExecutable = false),
						@example (
								value = "   diff(I,t) = (0.3 * S * I / 100);",
								isExecutable = false),
						@example (
								value = "} ",
								isExecutable = false) }),
				@usage (
						value = "If the type: facet is used, a predefined equation system is defined using variables vars: and parameters params: in the right order. All possible predefined equation systems are the following ones (see [EquationPresentation161 EquationPresentation161] for precise definition of each classical equation system): ",
						examples = { @example (
								value = "equation eqSI type: SI vars: [S,I,t] params: [N,beta];",
								isExecutable = false),
								@example (
										value = "equation eqSIS type: SIS vars: [S,I,t] params: [N,beta,gamma];",
										isExecutable = false),
								@example (
										value = "equation eqSIR type:SIR vars:[S,I,R,t] params:[N,beta,gamma];",
										isExecutable = false),
								@example (
										value = "equation eqSIRS type: SIRS vars: [S,I,R,t] params: [N,beta,gamma,omega,mu];",
										isExecutable = false),
								@example (
										value = "equation eqSEIR type: SEIR vars: [S,E,I,R,t] params: [N,beta,gamma,sigma,mu];",
										isExecutable = false),
								@example (
										value = "equation eqLV type: LV vars: [x,y,t] params: [alpha,beta,delta,gamma];",
										isExecutable = false) }),
				@usage (
						value = "If the simultaneously: facet is used, system of all the agents will be solved simultaneously.") },
		see = { "=", IKeyword.SOLVE })
// @validator (SystemOfEquationsValidator.class)

@SuppressWarnings ({ "rawtypes", "unchecked" })
public class SystemOfEquationsStatement extends AbstractStatementSequence implements FirstOrderDifferentialEquations {

	/**
	 * The Class SystemOfEquationsValidator.
	 */
	// public static class SystemOfEquationsValidator implements IDescriptionValidator<IDescription> {
	//
	// // /** The Constant CLASSICAL_NAMES. */
	// // final static Set<String> CLASSICAL_NAMES =
	// // new HashSet<>(Arrays.asList("SIR", "SI", "SIS", "SIRS", "SEIR", "LV"));
	//
	// @Override
	// public void validate(final IDescription description) {
	// // final String type = description.getLitteral(TYPE);
	// // if (type == null) return;
	// // if (!CLASSICAL_NAMES.contains(type)) {
	// // description.error(type + " is not a recognized classical equation name", IGamlIssue.WRONG_TYPE, TYPE);
	// // }
	// }
	// }

	/** The Constant ___equations. */
	public static final String ___equations = "___equations";

	/** The Constant ___variables_diff. */
	public static final String ___variables_diff = "___variables_diff";

	/** The equations. */
	private final IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> equations = GamaMapFactory.create();

	/** The variables diff. */
	private final IMap<Integer, GamaPair<IAgent, IExpression>> variables_diff = GamaMapFactory.create();

	/** The variable time. */
	public IExpression variable_time = null;

	/** The current scope. */
	private IScope currentScope;

	/** The simultan. */
	IExpression simultan = null;

	/**
	 * Instantiates a new system of equations statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public SystemOfEquationsStatement(final IDescription desc) {
		super(desc);
		setName(desc.getName());
		simultan = getFacet(IKeyword.SIMULTANEOUSLY);

	}

	/**
	 * This method separates regular statements and equations.
	 *
	 * @see gama.gaml.statements.AbstractStatementSequence#setChildren(java.util.List)
	 */
	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		List<? extends ISymbol> cmd = Lists.newArrayList(commands);
		// if (getFacet(IKeyword.TYPE) != null) {
		// final String type = getFacet(IKeyword.TYPE).literalValue();
		// final ListExpression vars = (ListExpression) getFacet(IKeyword.VARS);
		// final ListExpression params = (ListExpression) getFacet(IKeyword.PARAMS);
		// switch (type) {
		// case "SIR":
		// cmd.clear();
		// cmd = new ClassicalSIREquations(getDescription()).SIR(vars, params);
		// break;
		// case "SI":
		// cmd.clear();
		// cmd = new ClassicalSIEquations(getDescription()).SI(vars, params);
		// break;
		// case "SIS":
		// cmd.clear();
		// cmd = new ClassicalSISEquations(getDescription()).SIS(vars, params);
		// break;
		// case "SIRS":
		// cmd.clear();
		// cmd = new ClassicalSIRSEquations(getDescription()).SIRS(vars, params);
		// break;
		// case "SEIR":
		// cmd.clear();
		// cmd = new ClassicalSEIREquations(getDescription()).SEIR(vars, params);
		// break;
		// case "LV":
		// cmd.clear();
		// cmd = new ClassicalLVEquations(getDescription()).LV(vars, params);
		// }
		// }
		try (final Collector.AsList<ISymbol> others = Collector.getList()) {
			for (final ISymbol s : cmd) {
				if (s instanceof SingleEquationStatement s2) {
					s2.establishVar();
					for (int i = 0; i < s2.getVars().size(); i++) {
						final IExpression v = s2.getVar(i);
						if (s2.getOrder() > 0) {
							final GamaPair p1 =
									new GamaPair<IAgent, SingleEquationStatement>(null, s2, Types.AGENT, Types.NO_TYPE);
							equations.put(equations.size(), p1);
							final GamaPair p2 = new GamaPair<IAgent, IExpression>(null, v, Types.AGENT, Types.NO_TYPE);
							variables_diff.put(variables_diff.size(), p2);
						}
					}
					variable_time = s2.getVarTime();
				} else {
					others.add(s);
				}
			}
			super.setChildren(others.items());
		}
	}

	// static {
	// DEBUG.OFF();
	// }

	/**
	 * Assign value.
	 *
	 * @param scope
	 *            the scope
	 * @param time
	 *            the time
	 * @param y
	 *            the y
	 */
	public void assignValue(final IScope scope, final double time, final double[] y) {
		// final List<SingleEquationStatement> equationValues = new
		// ArrayList<SingleEquationStatement>(equations.values());
		// final List<IExpression> variableValues = new
		// ArrayList<IExpression>(variables_diff.values());
		final IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> myEQ = getEquations(currentScope.getAgent());
		final IMap<Integer, GamaPair<IAgent, IExpression>> myVar = getVariableDiff(scope.getAgent());
		for (int i = 0, n = myEQ.size(); i < n; i++) {

			final GamaPair<IAgent, SingleEquationStatement> s = myEQ.get(i);
			if (s.getValue().getOrder() == 0) { continue; }

			final IAgent remoteAgent = s.getKey();// getEquationAgents(scope).get(i);
			boolean pushed = false;
			if (null != remoteAgent && !remoteAgent.dead()) {
				pushed = scope.push(remoteAgent);
				try {
					if (s.getValue().getVarTime() instanceof IVarExpression) {
						((IVarExpression) s.getValue().getVarTime()).setVal(scope, time, false);
					}
					if (myVar.get(i).getValue() instanceof IVarExpression) {
						// if (OWN_DEBUG)
						// DEBUG.LOG(remoteAgent + " assign " + myVar.get(i).getValue() + " " + y[i]);
						((IVarExpression) myVar.get(i).getValue()).setVal(scope, y[i], false);
					}
				} catch (final Throwable ex1) {
					GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex1, scope), true);
				} finally {
					if (pushed) { scope.pop(remoteAgent); }
				}
			}

		}

		for (int i = 0, n = myEQ.size(); i < n; i++) {
			final GamaPair<IAgent, SingleEquationStatement> s = myEQ.get(i);
			if (s.getValue().getOrder() == 0) {
				final IExpression tmp = ((UnaryOperator) s.getValue().getFunction()).arg(0);
				final Object v = s.getValue().getExpression().value(currentScope);
				if (tmp instanceof AgentVariableExpression) {
					((AgentVariableExpression) tmp).setVal(currentScope, v, false);
				}
			}

		}
		setEquations(scope.getAgent(), myEQ);
		setVariableDiff(scope.getAgent(), myVar);
	}

	/**
	 * This method is bound to be called by the integrator of the equations system (instantiated in SolveStatement).
	 *
	 * @see org.apache.commons.math3.ode.FirstOrderDifferentialEquations#computeDerivatives(double, double[], double[])
	 */

	@Override
	public void computeDerivatives(final double time, final double[] y, final double[] ydot)
			throws MaxCountExceededException, DimensionMismatchException {
		/*
		 * the y value is calculated automatically inside integrator's algorithm just get y, and assign value to
		 * Variables in GAMA, which is use by GAMA modeler
		 */

		final IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> myEQ = getEquations(currentScope.getAgent());
		assignValue(currentScope, time, y);

		/*
		 * with variables assigned, calculate new value of expression in function loop through equations (internal and
		 * external) to get SingleEquation values
		 */

		// final List<SingleEquationStatement> equationValues = new
		// ArrayList<SingleEquationStatement>(equations.values());
		final Map<Integer, IAgent> equaAgents = getEquationAgents(currentScope);
		for (int i = 0, n = getDimension(); i < n; i++) {
			if (myEQ.get(i) == null) { continue; }
			try {
				final ExecutionResult result = currentScope.execute(myEQ.get(i).getValue(), equaAgents.get(i), null);
				ydot[i] = Cast.asFloat(currentScope, result.getValue());
			} catch (final Throwable e2) {
				GAMA.reportAndThrowIfNeeded(currentScope, GamaRuntimeException.create(e2, currentScope), true);
			}
		}
		setEquations(currentScope.getAgent(), myEQ);

	}

	/**
	 * The dimension of the equations system is simply, here, the number of equations.
	 *
	 * @see org.apache.commons.math3.ode.FirstOrderDifferentialEquations#getDimension()
	 */
	@Override
	public int getDimension() {
		int count = 0;
		final IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> myEQ = getEquations(currentScope.getAgent());
		for (final GamaPair<IAgent, SingleEquationStatement> s : myEQ.values()) {
			if (s.getValue().getOrder() > 0) { count++; }
		}

		return count;
	}

	/**
	 * Sets the current scope.
	 *
	 * @param currentScope
	 *            the new current scope
	 */
	private void setCurrentScope(final IScope currentScope) { this.currentScope = currentScope; }

	/**
	 * Gets the external agents.
	 *
	 * @param scope
	 *            the scope
	 * @return the external agents
	 */
	private Set<IAgent> getExternalAgents(final IScope scope) {
		if (scope.getAgent() == null) return Collections.EMPTY_SET;
		Set<IAgent> result = (Set<IAgent>) scope.getAgent().getAttribute("__externalAgents");
		if (result == null) {
			result = new LinkedHashSet<>();
			scope.getAgent().setAttribute("__externalAgents", result);
		}
		return result;
	}

	/**
	 * Gets the equations.
	 *
	 * @param agt
	 *            the agt
	 * @return the equations
	 */
	public IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> getEquations(final IAgent agt) {
		if (agt == null) return GamaMapFactory.create();
		IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> result =
				(IMap<Integer, GamaPair<IAgent, SingleEquationStatement>>) agt.getAttribute(___equations);
		if (result == null) {
			result = GamaMapFactory.create();
			agt.setAttribute(___equations, result);
		}
		return result;
	}

	/**
	 * Sets the equations.
	 *
	 * @param agt
	 *            the agt
	 * @param eqs
	 *            the eqs
	 */
	public void setEquations(final IAgent agt, final IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> eqs) {
		if (agt == null) return;
		agt.setAttribute(___equations, eqs);
	}

	/**
	 * Gets the variable diff.
	 *
	 * @param agt
	 *            the agt
	 * @return the variable diff
	 */
	public IMap<Integer, GamaPair<IAgent, IExpression>> getVariableDiff(final IAgent agt) {
		if (agt == null) return GamaMapFactory.create();
		IMap<Integer, GamaPair<IAgent, IExpression>> result =
				(IMap<Integer, GamaPair<IAgent, IExpression>>) agt.getAttribute(___variables_diff);
		if (result == null) {
			result = GamaMapFactory.create();
			currentScope.getAgent().setAttribute(___variables_diff, result);
		}
		return result;
	}

	/**
	 * Sets the variable diff.
	 *
	 * @param agt
	 *            the agt
	 * @param var
	 *            the var
	 */
	public void setVariableDiff(final IAgent agt, final IMap<Integer, GamaPair<IAgent, IExpression>> var) {
		if (agt == null) return;
		agt.setAttribute(___variables_diff, var);
	}

	/**
	 * Gets the equation agents.
	 *
	 * @param scope
	 *            the scope
	 * @return the equation agents
	 */
	public Map<Integer, IAgent> getEquationAgents(final IScope scope) {
		if (scope.getAgent() == null) return new HashMap();
		Map<Integer, IAgent> result = (Map<Integer, IAgent>) scope.getAgent().getAttribute("__equationAgents");
		if (result == null) {
			result = new HashMap();
			scope.getAgent().setAttribute("__equationAgents", result);
		}
		return result;

	}

	/**
	 * Begin with scope.
	 *
	 * @param scope
	 *            the scope
	 */
	private void beginWithScope(final IScope scope) {
		setCurrentScope(scope);
		addInternalAgents(scope);
		addInternalEquations(scope);
		addExternalAgents(scope);
		addExternalEquations(scope);
	}

	/**
	 * Finish with scope.
	 *
	 * @param scope
	 *            the scope
	 */
	private void finishWithScope(final IScope scope) {
		removeExternalEquations(scope);
		getEquationAgents(scope).clear();
		getExternalAgents(scope).clear();
		getEquations(scope.getAgent()).clear();
		getVariableDiff(scope.getAgent()).clear();
		setEquations(scope.getAgent(), null);
		setVariableDiff(scope.getAgent(), null);
		setCurrentScope(null);
	}

	/**
	 * Execute in scope.
	 *
	 * @param scope
	 *            the scope
	 * @param criticalSection
	 *            the critical section
	 */
	public synchronized void executeInScope(final IScope scope, final Runnable criticalSection) {
		beginWithScope(scope);
		criticalSection.run();
		finishWithScope(scope);
	}

	/**
	 * Adds the internal agents.
	 *
	 * @param scope
	 *            the scope
	 */
	private void addInternalAgents(final IScope scope) {
		for (int i = 0; i < equations.size(); i++) { getEquationAgents(scope).put(i, scope.getAgent()); }
	}

	/**
	 * Adds the internal equations.
	 *
	 * @param scope
	 *            the scope
	 */
	private void addInternalEquations(final IScope scope) {
		for (int i = 0; i < equations.size(); i++) { addEquationsOf(scope.getAgent()); }
	}

	/**
	 * Adds the external agents.
	 *
	 * @param scope
	 *            the scope
	 */
	private void addExternalAgents(final IScope scope) {
		addExternalAgents(scope, simultan, getExternalAgents(scope));
	}

	/**
	 * Adds the external equations.
	 *
	 * @param scope
	 *            the scope
	 */
	private void addExternalEquations(final IScope scope) {
		for (final IAgent remoteAgent : getExternalAgents(scope)) {
			if (!remoteAgent.dead()) { addEquationsOf(remoteAgent); }
		}
	}

	/**
	 * Removes the external equations.
	 *
	 * @param scope
	 *            the scope
	 */
	private void removeExternalEquations(final IScope scope) {
		if (scope.getAgent() == null) return;
		final IMap<String, IList<Double>> result =
				(IMap<String, IList<Double>>) scope.getAgent().getAttribute("__integrated_values");
		for (final IAgent remoteAgent : getExternalAgents(scope)) {
			if (!remoteAgent.dead()) {
				remoteAgent.setAttribute("__integrated_values", result);
				removeEquationsOf(remoteAgent);
			}
		}
	}

	/**
	 * Adds the equations of.
	 *
	 * @param remoteAgent
	 *            the remote agent
	 */
	private void addEquationsOf(final IAgent remoteAgent) {
		final IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> myEQ = getEquations(currentScope.getAgent());
		final IMap<Integer, GamaPair<IAgent, IExpression>> myVar = getVariableDiff(currentScope.getAgent());
		// if (remoteAgent.equals(currentScope.getAgent())) {
		for (final GamaPair<IAgent, SingleEquationStatement> e : myEQ.values()) {
			if (e.getKey().equals(remoteAgent)) return;
		}
		// if (OWN_DEBUG)
		// DEBUG.LOG(currentScope.getAgent() + " addEquationsOf " + remoteAgent);
		final SystemOfEquationsStatement ses =
				remoteAgent.getSpecies().getStatement(SystemOfEquationsStatement.class, getName());
		if (ses != null) {
			if (!remoteAgent.equals(currentScope.getAgent())) {
				for (int i = 0, n = ses.equations.size(); i < n; i++) {
					getEquationAgents(currentScope).put(getEquationAgents(currentScope).size(), remoteAgent);
				}
			}
			for (final GamaPair<IAgent, SingleEquationStatement> s : ses.equations.values()) {
				// final String name = remoteAgent.getName() + s.getKey();
				final SingleEquationStatement eq = s.getValue();
				// if (OWN_DEBUG)
				// DEBUG.LOG("eq " + eq);
				final GamaPair p1 = new GamaPair<>(remoteAgent, eq, Types.AGENT, Types.NO_TYPE);
				myEQ.put(myEQ.size(), p1);
			}
			for (final GamaPair<IAgent, IExpression> s : ses.variables_diff.values()) {
				// final String name = remoteAgent.getName() + s.getKey();
				final IExpression v = s.getValue();
				// if (OWN_DEBUG)
				// DEBUG.LOG("v " + v);
				final GamaPair p1 = new GamaPair<>(remoteAgent, v, Types.AGENT, Types.NO_TYPE);
				myVar.put(myVar.size(), p1);
			}
		}

		// }else {
		// if (OWN_DEBUG) DEBUG.LOG("addEquationsOf "+remoteAgent);
		// GamaMap<Integer, GamaPair<IAgent, SingleEquationStatement>> eqs = getEquations(remoteAgent);
		// GamaMap<Integer, GamaPair<IAgent, IExpression>> sesVar = getVariableDiff(remoteAgent);
		// if (eqs != null) {
		// for (int i = 0, n = eqs.size(); i < n; i++) {
		// getEquationAgents(currentScope).put(getEquationAgents(currentScope).size(), remoteAgent);
		// }
		// for (final GamaPair<IAgent, SingleEquationStatement> s : eqs.values()) {
		// // final String name = remoteAgent.getName() + s.getKey();
		//// if(s.getKey()!=null) {
		// final SingleEquationStatement eq = s.getValue();
		// if (OWN_DEBUG) DEBUG.LOG("eq "+eq);
		// GamaPair p1 = new GamaPair<IAgent, SingleEquationStatement>(remoteAgent, eq, Types.AGENT,
		// Types.NO_TYPE);
		// myEQ.put(myEQ.size(), p1);
		//// }
		// }
		// for (final GamaPair<IAgent, IExpression> s : sesVar.values()) {
		// // final String name = remoteAgent.getName() + s.getKey();
		// final IExpression v = s.getValue();
		// DEBUG.LOG("v "+v);
		// GamaPair p1 = new GamaPair<IAgent, IExpression>(remoteAgent, v, Types.AGENT, Types.NO_TYPE);
		// myVar.put(myVar.size(), p1);
		// }
		// }
		// }

		setEquations(currentScope.getAgent(), myEQ);
		setVariableDiff(currentScope.getAgent(), myVar);

	}

	/**
	 * Removes the equations of.
	 *
	 * @param remoteAgent
	 *            the remote agent
	 */
	private void removeEquationsOf(final IAgent remoteAgent) {
		final IMap<Integer, GamaPair<IAgent, SingleEquationStatement>> myEQ = getEquations(currentScope.getAgent());
		final IMap<Integer, GamaPair<IAgent, IExpression>> myVar = getVariableDiff(currentScope.getAgent());

		// final SystemOfEquationsStatement ses =
		// remoteAgent.getSpecies().getStatement(SystemOfEquationsStatement.class,
		// getName());

		// DEBUG.LOG("removeEquationsOf " + remoteAgent);
		// GamaMap<Integer, GamaPair<IAgent, SingleEquationStatement>> ses = getEquations(remoteAgent);
		// GamaMap<Integer, GamaPair<IAgent, IExpression>> sesVar = getVariableDiff(remoteAgent);
		if (myEQ != null) {
			// final int n = equations.size();
			// for (final GamaPair<IAgent, SingleEquationStatement> e : myEQ.values()) {
			// if (e.getKey().equals(remoteAgent)) {
			// // myVar.values().remove(remoteAgent);
			// // for (final IExpression eV : e.getValue().getVars()) {
			// // DEBUG.OUT(eV);
			// // GamaPair p1 = new GamaPair<IAgent, IExpression>(remoteAgent, eV, Types.AGENT, Types.NO_TYPE);
			// // myVar.values().remove(p1);
			// // }
			// }
			// }
			myEQ.values().removeIf(e -> e.getKey().equals(remoteAgent));
			// for (final Integer s : ses.equations.keySet()) {
			// equations.remove(n - s - 1);
			// variables_diff.remove(n - s - 1);
			// }
		}
		setEquations(currentScope.getAgent(), myEQ);
		setVariableDiff(currentScope.getAgent(), myVar);
	}

	/**
	 * Adds the external agents.
	 *
	 * @param scope
	 *            the scope
	 * @param toAdd
	 *            the to add
	 * @param externalAgents
	 *            the external agents
	 */
	private void addExternalAgents(final IScope scope, final Object toAdd, final Set<IAgent> externalAgents) {
		if (toAdd instanceof IExpression) {
			addExternalAgents(scope, ((IExpression) toAdd).value(scope), externalAgents);
		} else if (toAdd instanceof IAgent && !toAdd.equals(scope.getAgent()) && !((IAgent) toAdd).dead()) {
			externalAgents.add((IAgent) toAdd);
		} else if (toAdd instanceof GamlSpecies) {
			addExternalAgents(scope, ((GamlSpecies) toAdd).getPopulation(scope), externalAgents);
		} else if (toAdd instanceof IList) {
			for (final Object o : ((IList) toAdd).iterable(scope)) { addExternalAgents(scope, o, externalAgents); }
		}
	}

}
