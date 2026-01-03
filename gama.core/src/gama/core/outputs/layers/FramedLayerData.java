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

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColorFactory;
import gama.core.util.IColor;
import gama.gaml.types.Types;

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
				: GamaColorFactory.get(c.red(), c.green(), c.blue(), (int) ((1 - getTransparency(scope)) * 255));
	}

	/**
	 * Gets the border color.
	 *
	 * @return the border color
	 */
	public IColor getBorderColor() { return border.get(); }

}
