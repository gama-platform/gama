/*******************************************************************************************************
 *
 * FramedLayerData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.ui.layers.ILayerStatement;

/**
 * The Class OverlayLayerData.
 */
public class FramedLayerData extends LayerData {

	/** The border. */
	final Attribute<IColor> border;

	/** The background. */
	final Attribute<IColor> background;

	/**
	 * Instantiates a new overlay layer data.
	 *
	 * @param def
	 *            the def
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public FramedLayerData(final ILayerStatement def) throws GamaRuntimeException {
		super(def);
		border = create(IKeyword.BORDER, Types.COLOR, null);
		background = create(IKeyword.BACKGROUND, Types.COLOR,
				def instanceof OverlayStatement ? GamaColorFactory.BLACK : null);
	}

	/**
	 * Gets the background color.
	 *
	 * @param scope
	 *            the scope
	 * @return the background color
	 */
	public IColor getBackgroundColor(final IScope scope) {
		IColor c = background.get();
		return c == null ? null
				: GamaColorFactory.createWithRGBA(c.red(), c.green(), c.blue(), (int) ((1 - getTransparency(scope)) * 255));
	}

	/**
	 * Gets the border color.
	 *
	 * @return the border color
	 */
	public IColor getBorderColor() { return border.get(); }

}
