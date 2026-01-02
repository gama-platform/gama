/*******************************************************************************************************
 *
 * AbstractStatementSequenceWithArgs.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.statements.IStatement.WithArgs;

/**
 * Class AbstractStatementSequenceWithArgs.
 *
 * @author drogoul
 * @since 11 mai 2014
 *
 */
public class AbstractStatementSequenceWithArgs extends AbstractStatementSequence implements WithArgs {

	/** The actual args. */
	final ThreadLocal<IArguments> actualArgs = new ThreadLocal<>();

	/**
	 * @param desc
	 */
	public AbstractStatementSequenceWithArgs(final IDescription desc) {
		super(desc);
	}

	/**
	 * Method setFormalArgs()
	 *
	 * @see gama.gaml.statements.IStatement.WithArgs#setFormalArgs(gama.gaml.statements.Arguments)
	 */
	@Override
	public void setFormalArgs(final IArguments args) {}

	/**
	 * Method setRuntimeArgs()
	 *
	 * @see gama.gaml.statements.IStatement.WithArgs#setRuntimeArgs(gama.gaml.statements.Arguments)
	 */
	@Override
	public void setRuntimeArgs(final IScope scope, final IArguments args) {
		// TODO Verify that this copy of the arguments is required or not.
		actualArgs.set(new Arguments(args));
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.stackArguments(actualArgs.get());
		return super.privateExecuteIn(scope);
	}

	@Override
	public void dispose() {
		IArguments args = actualArgs.get();
		if (args != null) { args.dispose(); }
		actualArgs.set(null);
		super.dispose();
	}

}
