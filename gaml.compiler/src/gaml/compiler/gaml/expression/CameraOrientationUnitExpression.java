/*******************************************************************************************************
 *
 * CameraOrientationUnitExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;

/**
 *
 * @author Drogoul
 * @revision Now provides the camera_orientation even if the code is not run within a graphics context but the current
 *           experiment only one OpenGL display
 */
public class CameraOrientationUnitExpression extends UnitConstantExpression {

	/**
	 * Instantiates a new camera orientation unit expression.
	 *
	 * @param doc
	 *            the doc
	 */
	public CameraOrientationUnitExpression(final String doc) {
		super(GamaPointFactory.create(), Types.POINT, "camera_orientation", doc, null);
	}

	@Override
	public IPoint _value(final IScope sc) {
		if (sc == null || !sc.isGraphics()) return null;
		IGraphicsScope scope = (IGraphicsScope) sc;
		final IGraphics g = scope.getGraphics();
		if (g.is2D()) return null;
		return ((IGraphics.ThreeD) g).getCameraOrientation().yNegated();
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public boolean isAllowedInParameters() { return false; }

}
