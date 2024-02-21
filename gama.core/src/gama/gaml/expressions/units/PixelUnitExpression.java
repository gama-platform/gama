/*******************************************************************************************************
 *
 * PixelUnitExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.units;

import gama.core.common.interfaces.IGraphics;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.gaml.types.Types;

/**
 * The Class PixelUnitExpression.
 */
public class PixelUnitExpression extends UnitConstantExpression {

	/**
	 * Instantiates a new pixel unit expression.
	 *
	 * @param name
	 *            the name
	 * @param doc
	 *            the doc
	 */
	public PixelUnitExpression(final String name, final String doc) {
		super(1.0, Types.FLOAT, name, doc, new String[] { "pixels", "px" });
	}

	@Override
	public Double _value(final IScope sc) {
		if (!(sc instanceof IGraphicsScope scope)) return 1d;
		final IGraphics g = scope.getGraphics();
		if (g == null) return 1d;
		return 1d / g.getAbsoluteRatioBetweenPixelsAndModelsUnits();
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public boolean isContextIndependant() { return false; }

	@Override
	public boolean isAllowedInParameters() { return false; }
}
