/*******************************************************************************************************
 *
 * AspectDrawer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import java.awt.geom.Rectangle2D;

import gama.core.common.interfaces.IDrawDelegate;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.statements.IExecutable;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class AspectDrawer.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 10 déc. 2023
 */
public class AspectDrawer implements IDrawDelegate {

	/**
	 * Instantiates a new aspect drawer.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 10 déc. 2023
	 */
	public AspectDrawer() {}

	@Override
	public Rectangle2D executeOn(final IGraphicsScope scope, final DrawingData data, final IExpression... items)
			throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		if (agent == null) return null;
		String aspectName = items[0].literalValue();
		IExecutable aspect = agent.getSpecies().getAspect(aspectName);
		if (aspect == null) return null;
		return (Rectangle2D) aspect.executeOn(scope);
	}

	@Override
	public IType<?> typeDrawn() {
		return Types.ACTION;
	}

	@Override
	public void validate(final IDescription currentDrawStatement, final IExpression item) {
		String name = item.literalValue();
		IDescription aspect = currentDrawStatement.getParentWithKeyword(IKeyword.ASPECT);
		if (aspect == null) return;
		if (aspect.getName().equals(name)) { currentDrawStatement.error("An aspect cannot draw itself recursively"); }
	}

}
