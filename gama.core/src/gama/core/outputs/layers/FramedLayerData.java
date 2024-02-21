/*******************************************************************************************************
 *
 * FramedLayerData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.Color;

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.types.Types;

/**
 * The Class OverlayLayerData.
 */
public class FramedLayerData extends LayerData {

	/** The border. */
	final Attribute<GamaColor> border;

	/** The background. */
	final Attribute<GamaColor> background;

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
				def instanceof OverlayStatement ? GamaColor.get(Color.black) : null);
	}

	/**
	 * Gets the background color.
	 *
	 * @param scope
	 *            the scope
	 * @return the background color
	 */
	public Color getBackgroundColor(final IScope scope) {
		Color c = background.get();
		return c == null ? null
				: new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) ((1 - getTransparency(scope)) * 255));
	}

	/**
	 * Gets the border color.
	 *
	 * @return the border color
	 */
	public Color getBorderColor() { return border.get(); }

}
