/*******************************************************************************************************
 *
 * HiDPIExpression.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import gama.api.GAMA;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IGamaView;
import gama.api.ui.IOutput;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;

/**
 * The Class ZoomUnitExpression.
 */
public class HiDPIExpression extends UnitConstantExpression {

	/**
	 * Instantiates a new zoom unit expression.
	 *
	 * @param name
	 *            the name
	 * @param doc
	 *            the doc
	 */
	public HiDPIExpression(final String name, final String doc) {
		super(1.0, Types.BOOL, name, doc, null);
	}

	@Override
	public Boolean _value(final IScope scope) {
		if (!scope.isGraphics()) return GAMA.getGui().isHiDPI();
		final IGraphics g = ((IGraphicsScope) scope).getGraphics();
		if (g == null) return false;
		IDisplaySurface surface = g.getSurface();
		if (surface == null) return false;
		IOutput.Display output = surface.getOutput();
		if (output == null) return false;
		IGamaView.Display view = output.getView();
		if (view == null) return false;
		return view.isHiDPI();
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public boolean isContextIndependant() { return false; }

}
