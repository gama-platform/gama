package gama.ui.display.chart;

import java.awt.geom.Rectangle2D;

import gama.core.common.interfaces.IDrawDelegate;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.expressions.IExpression;
import gama.gaml.statements.draw.DrawingData;
import gama.gaml.types.IType;

public class TrialDraw implements IDrawDelegate {

	public TrialDraw() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle2D executeOn(IGraphicsScope agent, DrawingData data, IExpression... items)
			throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IType<?> typeDrawn() {
		// TODO Auto-generated method stub
		return null;
	}

}
