/*******************************************************************************************************
 *
 * IDrawDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import java.awt.geom.Rectangle2D;

import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.statements.draw.DrawingData;
import gama.gaml.types.IType;

/**
 * Class ICreateDelegate. Allows to create agents from other sources than the ones used in the tradition 'create'
 * statement
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
public interface IDrawDelegate {

	/**
	 * Fills the list of maps with the initial values read from the source. Returns true if all the inits have been
	 * correctly filled
	 *
	 * @param scope
	 * @param inits
	 * @param max
	 *            can be null (in that case, the maximum number of agents to create is ignored)
	 * @param source
	 * @return
	 */

	Rectangle2D executeOn(IGraphicsScope agent, DrawingData data, IExpression... items) throws GamaRuntimeException;

	/**
	 * Returns the type expected in the default facet of the 'draw' statement. Should not be null and should be
	 * different from IType.NO_TYPE (in order to be able to check the validity of draw statements at compile time). The
	 * type should also be considered as 'drawable' (see {@link IType#isDrawable()}).
	 *
	 * @return a GAML type representing the type of the objects this draw delegate expects
	 */
	IType<?> typeDrawn();

	/**
	 * Validate. Called by the draw statement when it is validated. Can perform checks depending on the type of object
	 * to draw
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param currentDrawStatement
	 *            the current draw statement
	 * @param item
	 * @date 12 déc. 2023
	 */
	default void validate(final IDescription currentDrawStatement, final IExpression item) {}

}
