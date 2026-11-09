/*******************************************************************************************************
 *
 * ResourceObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.resources;

import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IDrawingAttributes.DrawerType;
import gama.core.util.file.GamaGeometryFile;
import gama.ui.display.opengl.scene.AbstractObject;

/**
 * The Class ResourceObject.
 */
public class ResourceObject extends AbstractObject<GamaGeometryFile, IDrawingAttributes> {

	private final String cacheKey;
	private final gama.api.utils.geometry.AxisAngle initialRotation;
	private final boolean twoD;
	/**
	 * Instantiates a new resource object.
	 *
	 * @param file
	 *            the file
	 * @param attributes
	 *            the attributes
	 */
	public ResourceObject(final GamaGeometryFile file, final IDrawingAttributes attributes) {
		this(file, attributes, null);
	}

	public ResourceObject(final GamaGeometryFile file, final IDrawingAttributes attributes, final String cacheKey) {
		super(file, attributes, DrawerType.RESOURCE);
		this.cacheKey = cacheKey;
		final var rotation = file.getInitRotation();
		initialRotation = rotation == null ? null
				: new gama.api.utils.geometry.AxisAngle(rotation.getAxis(), rotation.getAngle());
		twoD = file.is2D();
	}

	public String getCacheKey() { return cacheKey; }

	public gama.api.utils.geometry.AxisAngle getInitialRotation() { return initialRotation; }

	public boolean is2D() { return twoD; }

	@Override
	public gama.api.utils.geometry.IEnvelope getEnvelope(final gama.ui.display.opengl.IOpenGL gl) {
		return cacheKey == null ? super.getEnvelope(gl) : gl.getEnvelopeFor(getObject(), cacheKey);
	}

}
