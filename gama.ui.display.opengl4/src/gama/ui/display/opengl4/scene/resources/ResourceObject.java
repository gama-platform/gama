/*******************************************************************************************************
 *
 * ResourceObject.java, in gama.ui.display.opengl4, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl4.scene.resources;

import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IDrawingAttributes.DrawerType;
import gama.core.util.file.GamaGeometryFile;
import gama.ui.display.opengl4.scene.AbstractObject;

/**
 * The Class ResourceObject.
 */
public class ResourceObject extends AbstractObject<GamaGeometryFile, IDrawingAttributes> {

	/**
	 * Instantiates a new resource object.
	 *
	 * @param file
	 *            the file
	 * @param attributes
	 *            the attributes
	 */
	public ResourceObject(final GamaGeometryFile file, final IDrawingAttributes attributes) {
		super(file, attributes, DrawerType.RESOURCE);
	}

}
