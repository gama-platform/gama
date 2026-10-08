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

import gama.annotations.constants.IKeyword;
import gama.api.additions.delegates.IDrawDelegate;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.IExecutable;
import gama.api.ui.displays.DrawingData;
import gama.api.ui.displays.IGraphicsScope;

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
