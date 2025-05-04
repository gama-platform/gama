/*******************************************************************************************************
 *
 * ReflexArchitecture.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.architecture.reflex;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.IConcept;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.ExecutionResult;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.statements.IStatement;

/**
 * Written by drogoul Modified on 12 sept. 2010
 *
 * @todo Description
 *
 */
@skill (
		name = IKeyword.REFLEX,
		concept = { IConcept.BEHAVIOR, IConcept.ARCHITECTURE })
@doc ("Represents the default behavioral architecture attached to species of agents if none is specified. This skills extends AbstractArchitecture and have all his actions and variables")
public class ReflexArchitecture extends AbstractArchitecture {

	/**
	 * @param desc
	 */
	public ReflexArchitecture(final IDescription desc) {
		super(desc);
	}

	/** The inits. */
	protected List<IStatement> _inits;

	/** The reflexes. */
	protected List<IStatement> _reflexes;

	/** The aborts. */
	protected List<IStatement> _aborts;

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		clearBehaviors();
		for (final ISymbol c : children) { addBehavior((IStatement) c); }
	}

	/**
	 * Clear behaviors.
	 */
	protected void clearBehaviors() {
		if (_inits != null) { _inits.clear(); }
		_inits = null;
		if (_aborts != null) { _aborts.clear(); }
		_aborts = null;
		if (_reflexes != null) { _reflexes.clear(); }
		_reflexes = null;
	}

	/**
	 * Adds the behavior.
	 *
	 * @param c
	 *            the c
	 */
	public void addBehavior(final IStatement c) {
		switch (c.getKeyword()) {
			case IKeyword.INIT:
				if (_inits == null) { _inits = new ArrayList<>(); }
				_inits.add(0, c);
				return;

			case IKeyword.ABORT:
				if (_aborts == null) { _aborts = new ArrayList<>(); }
				_aborts.add(0, c);
				return;
			case IKeyword.REFLEX:
			case IKeyword.TEST:
				if (_reflexes == null) { _reflexes = new ArrayList<>(); }
				_reflexes.add(c);
				break;
			default:
				;
		}

	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		return executeReflexes(scope);
	}

	/**
	 * Execute reflexes.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected final Object executeReflexes(final IScope scope) throws GamaRuntimeException {
		if (_reflexes == null) return null;
		Object result = null;
		for (final IStatement r : _reflexes) {
			final ExecutionResult er = scope.execute(r);
			if (!er.passed()) return result;
			result = er.getValue();
		}
		return result;
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		if (_inits == null) return true;
		for (final IStatement init : _inits) { if (!scope.execute(init).passed()) return false; }
		return true;
	}

	@Override
	public boolean abort(final IScope scope) throws GamaRuntimeException {
		if (_aborts == null) return true;
		for (final IStatement init : _aborts) { if (!scope.execute(init).passed()) return false; }
		return true;
	}

	@Override
	public void setEnclosing(final ISymbol enclosing) {
		// Nothing to do by default
	}

	@Override
	public void preStep(final IScope scope, final IPopulation<? extends IAgent> gamaPopulation) {}

	@Override
	public int getOrder() { return 0; }

	@Override
	public void setOrder(final int o) {}

	@Override
	public URI getURI() { return null; }

}
