/*******************************************************************************************************
 *
 * ImageDrawer.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image.display;

import java.awt.geom.Rectangle2D;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.ui.displays.DrawingData;
import gama.api.ui.displays.IGraphicsScope;
import gama.extension.image.GamaImageType;
import gama.gaml.statements.draw.AssetDrawer;

/**
 * The Class ImageDrawer.
 */
public class ImageDrawer extends AssetDrawer {

	/**
	 * Execute on.
	 *
	 * @param scope
	 *            the scope
	 * @param data
	 *            the data
	 * @param items
	 *            the items
	 * @return the rectangle 2 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public Rectangle2D executeOn(final IGraphicsScope scope, final DrawingData data, final IExpression... items)
			throws GamaRuntimeException {
		return super.executeOn(scope, data, items);
	}

	/**
	 * Type drawn.
	 *
	 * @return the i type
	 */
	@Override
	public IType<?> typeDrawn() {
		return Types.get(GamaImageType.ID);
	}

}
