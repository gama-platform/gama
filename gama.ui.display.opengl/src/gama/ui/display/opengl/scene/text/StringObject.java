/*******************************************************************************************************
 *
 * StringObject.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.text;

import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IDrawingAttributes.DrawerType;
import gama.ui.display.opengl.scene.AbstractObject;

/**
 * The Class StringObject.
 */
public class StringObject extends AbstractObject<String, IDrawingAttributes> {

	/**
	 * Instantiates a new string object.
	 *
	 * @param string
	 *            the string
	 * @param attributes
	 *            the attributes
	 */
	public StringObject(final String string, final IDrawingAttributes attributes) {
		super(string, attributes, DrawerType.STRING);
	}

}
