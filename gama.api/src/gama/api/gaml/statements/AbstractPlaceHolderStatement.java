/*******************************************************************************************************
 *
 * AbstractPlaceHolderStatement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.gaml.statements;

import gama.api.compilation.descriptions.IDescription;
import gama.api.runtime.scope.IScope;

/**
 * Abstract base class for placeholder statements that perform no execution.
 * 
 * <p>
 * Placeholder statements are used during compilation to represent syntactic constructs that don't have runtime
 * behavior but provide metadata or structure. The primary example is the {@link ArgStatement}, which declares
 * parameters but doesn't execute any code.
 * </p>
 * 
 * <h2>Usage</h2>
 * <p>
 * This class is used for statements that:
 * </p>
 * <ul>
 * <li>Declare structure or metadata (like argument definitions)</li>
 * <li>Are processed entirely at compile time</li>
 * <li>Have no runtime execution logic</li>
 * </ul>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see ArgStatement
 * @see AbstractStatement
 */
public abstract class AbstractPlaceHolderStatement extends AbstractStatement {

	/**
	 * Constructs a new placeholder statement.
	 *
	 * @param desc
	 *            the statement description
	 */
	public AbstractPlaceHolderStatement(final IDescription desc) {
		super(desc);
	}

	/**
	 * Executes nothing and returns null.
	 * 
	 * <p>
	 * Placeholder statements have no runtime behavior.
	 * </p>
	 *
	 * @param stack
	 *            the execution scope (unused)
	 * @return always null
	 */
	@Override
	protected Object privateExecuteIn(final IScope stack) {
		return null;
	}

}
