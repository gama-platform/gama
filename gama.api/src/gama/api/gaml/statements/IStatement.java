/*******************************************************************************************************
 *
 * IStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import java.util.Map;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.ISymbol;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IExperimentDisplayable;
import gama.api.utils.tests.IndividualTestSummary;
import gama.api.utils.tests.WithTestSummary;

/**
 * Written by drogoul Feb. 2009
 *
 *
 *
 */
public interface IStatement extends ISymbol, IExecutable {

	/**
		 *
		 */
	public interface Remote extends IStatement {

	}

	/**
		 *
		 */
	public interface UserCommand extends IStatement, IExperimentDisplayable {

	}

	/**
		 *
		 */
	public interface Aspect extends IStatement {

	}

	/**
		 *
		 */
	public interface Action extends IStatement.WithArgs {

	}

	/**
	 * The Interface WithArgs.
	 */
	public interface WithArgs extends IStatement {

		/**
		 * Sets the formal args.
		 *
		 * @param args
		 *            the new formal args
		 */
		void setFormalArgs(Arguments args);

		/**
		 * Sets the runtime args.
		 *
		 * @param scope
		 *            the scope
		 * @param args
		 *            the args
		 */
		@Override
		default void setRuntimeArgs(final IScope scope, final Arguments args) {}

	}

	/**
	 * The Interface Breakable.
	 */
	public interface Breakable extends IStatement {
		// Unused tagging interface (for the moment)
	}

	/**
	 * The Interface Test.
	 */
	public interface Test extends IStatement, WithTestSummary<IndividualTestSummary> {

	}

	/**
	 * The Interface Create.
	 */
	public interface Create extends IStatement.WithArgs {

		/**
		 * @param scope
		 * @param map
		 */
		void fillWithUserInit(IScope scope, Map map);

		/**
		 * @return
		 */
		IExpression getHeader();

	}

	/**
	 * The Interface Save.
	 */
	public interface Save extends IStatement {

	}

	/**
	 * The Interface Draw.
	 */
	public interface Draw extends IStatement {

	}

	/**
	 * The Interface Event.
	 */
	public interface Event extends ISymbol {

	}

	/**
	 * @return
	 */
	default boolean isEmpty() { return true; }

}
