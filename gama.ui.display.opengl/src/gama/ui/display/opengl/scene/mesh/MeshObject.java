/*******************************************************************************************************
 *
 * MeshObject.java, in gama.ui.display.opengl, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.mesh;

import gama.core.util.matrix.IField;
import gama.gaml.statements.draw.MeshDrawingAttributes;
import gama.gaml.statements.draw.DrawingAttributes.DrawerType;
import gama.ui.display.opengl.scene.AbstractObject;

/**
 * The Class MeshObject.
 */
public class MeshObject extends AbstractObject<IField, MeshDrawingAttributes> {

	/**
	 * Instantiates a new mesh object.
	 *
	 * @param dem the dem
	 * @param attributes the attributes
	 */
	public MeshObject(final IField dem, final MeshDrawingAttributes attributes) {
		super(dem, attributes, DrawerType.MESH);
	}

}
