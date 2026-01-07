/*******************************************************************************************************
 *
 * CameraTargetUnitExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.units;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGraphics;
import gama.core.metamodel.shape.GamaPointFactory;
import gama.core.metamodel.shape.IPoint;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.gaml.types.Types;

/**
 * The Class CameraTargetUnitExpression.
 */
public class CameraTargetUnitExpression extends UnitConstantExpression {

	/**
	 * Instantiates a new camera target unit expression.
	 *
	 * @param doc
	 *            the doc
	 */
	public CameraTargetUnitExpression(final String doc) {
		super(GamaPointFactory.create(), Types.POINT, "camera_target", doc, null);
	}

	@Override
	public IPoint _value(final IScope sc) {
		if (sc == null || !sc.isGraphics()) {
			IDisplaySurface surface = GAMA.getGui().getFrontmostDisplaySurface();
			if (surface != null) return surface.getData().getCameraTarget().yNegated();
			return null;
		}
		IGraphicsScope scope = (IGraphicsScope) sc;
		final IGraphics g = scope.getGraphics();
		if (g.is2D()) return null;
		return ((IGraphics.ThreeD) g).getCameraTarget().yNegated();
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public boolean isAllowedInParameters() { return false; }

}
