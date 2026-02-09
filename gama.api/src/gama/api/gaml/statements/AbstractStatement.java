/*******************************************************************************************************
 *
 * AbstractStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.Symbol;
import gama.api.runtime.scope.IScope;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 */

public abstract class AbstractStatement extends Symbol implements IStatement {

	/**
	 * Instantiates a new abstract statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public AbstractStatement(final IDescription desc) {
		super(desc);
		if (desc != null) {
			final String k = getKeyword();
			final String n = desc.getName();
			setName(k == null ? "" : k + " " + (n == null ? "" : n));
		}
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		try {
			scope.setCurrentSymbol(this);
			return privateExecuteIn(scope);
		} catch (final GamaRuntimeException e) {
			e.addContext(this);
			GAMA.reportAndThrowIfNeeded(scope, e, true);
			return null;
		} finally {
			scope.setCurrentSymbol(null);
		}
	}

	/**
	 * Private execute in.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected abstract Object privateExecuteIn(IScope scope) throws GamaRuntimeException;

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {}

	@Override
	public String toString() {
		return description.serializeToGaml(true);
	}

	@Override
	public IStatementDescription getDescription() { return (IStatementDescription) super.getDescription(); }

}
