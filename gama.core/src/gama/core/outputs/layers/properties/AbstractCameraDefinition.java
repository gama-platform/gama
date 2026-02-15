/*******************************************************************************************************
 *
 * AbstractCameraDefinition.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.properties;

import gama.api.data.objects.IPoint;
import gama.api.gaml.symbols.ISymbol;
import gama.api.ui.layers.ICameraDefinition;
import gama.api.utils.geometry.GamaPointFactory;

/**
 *
 */
public abstract class AbstractCameraDefinition extends AbstractDefinition implements ICameraDefinition {

	/**
	 * @param symbol
	 */
	public AbstractCameraDefinition(final ISymbol symbol) {
		super(symbol);
	}

	@Override
	public IPoint computeLocation(final String pos, final IPoint target, final double maxX, final double maxY,
			final double maxZ) {

		double tx = target.getX();
		double ty = target.getY();
		return switch (pos) {
			case from_above -> GamaPointFactory.create(tx, ty, maxZ);
			case from_left -> GamaPointFactory.create(tx - maxX, ty, 0);
			case from_up_left -> GamaPointFactory.create(tx - maxX, ty, maxZ);
			case from_right -> GamaPointFactory.create(tx + maxX, ty - maxY / 1000, 0);
			case from_up_right -> GamaPointFactory.create(tx + maxX, ty - maxY / 1000, maxZ);
			case from_front -> GamaPointFactory.create(tx, ty - maxY, 0);
			case from_up_front -> GamaPointFactory.create(tx, ty - maxY, maxZ);
			case isometric -> GamaPointFactory.create(tx + maxZ, -maxZ + ty, maxZ / 1.2);
			default -> GamaPointFactory.create(tx, ty, maxZ); // FROM_ABOVE
		};

	}

}
