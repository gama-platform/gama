/*******************************************************************************************************
 *
 * AssetDrawingAttributes.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.draw;

import gama.api.kernel.agent.IAgent;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.Scaling3D;

/**
 * The Class FileDrawingAttributes.
 */
public class AssetDrawingAttributes extends DrawingAttributes {

	/** The agent identifier. */
	public final IAgent agentIdentifier;

	/**
	 * Instantiates a new file drawing attributes.
	 *
	 * @param size
	 *            the size
	 * @param rotation
	 *            the rotation
	 * @param location
	 *            the location
	 * @param color
	 *            the color
	 * @param border
	 *            the border
	 * @param agent
	 *            the agent
	 * @param lineWidth
	 *            the line width
	 * @param isImage
	 *            the is image
	 * @param lighting
	 *            the lighting
	 */
	public AssetDrawingAttributes(final Scaling3D size, final AxisAngle rotation, final IPoint location,
			final IColor color, final IColor border, final IAgent agent, final Double lineWidth, final boolean isImage,
			final Boolean lighting) {
		super(size, rotation, location, color, border, lighting);
		this.agentIdentifier = agent;
		setLineWidth(lineWidth);
		setType(isImage ? IShape.Type.POLYGON : IShape.Type.THREED_FILE);
		setUseCache(true); // by default
	}

	/**
	 * Instantiates a new file drawing attributes.
	 *
	 * @param location
	 *            the location
	 * @param isImage
	 *            the is image
	 */
	public AssetDrawingAttributes(final IPoint location, final boolean isImage) {
		super(null, null, location, null, null, null);
		agentIdentifier = null;
		setType(isImage ? IShape.Type.POLYGON : IShape.Type.THREED_FILE);
		setUseCache(true); // by default
	}

	@Override
	public IAgent getAgentIdentifier() { return agentIdentifier; }

}