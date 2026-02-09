/*******************************************************************************************************
 *
 * MeshObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.mesh;

import gama.api.data.objects.IField;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IDrawingAttributes.DrawerType;
import gama.ui.display.opengl.scene.AbstractObject;

/**
 * The Class MeshObject.
 */
public class MeshObject extends AbstractObject<IField, IDrawingAttributes> {

	/**
	 * Instantiates a new mesh object.
	 *
	 * @param dem
	 *            the dem
	 * @param attributes
	 *            the attributes
	 */
	public MeshObject(final IField dem, final IDrawingAttributes attributes) {
		super(dem, attributes, DrawerType.MESH);
	}

}
