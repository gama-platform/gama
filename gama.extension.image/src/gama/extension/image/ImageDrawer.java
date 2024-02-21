/*******************************************************************************************************
 *
 * ImageDrawer.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import java.awt.geom.Rectangle2D;

import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.expressions.IExpression;
import gama.gaml.statements.draw.AssetDrawer;
import gama.gaml.statements.draw.DrawingData;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
