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

import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.List;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.ui.layers.IDrawingAttributes;
import gama.api.ui.layers.IDrawingAttributes.DrawerType;
import gama.ui.display.opengl.scene.AbstractObject;

/**
 * The Class StringObject.
 */
public class StringObject extends AbstractObject<String, IDrawingAttributes> {

	private final boolean collected;
	private List<StringObject> lines;
	private String[] text;

	/**
	 * Instantiates a new string object.
	 *
	 * @param string
	 *            the string
	 * @param attributes
	 *            the attributes
	 */
	public StringObject(final String string, final IDrawingAttributes attributes) {
		this(string, attributes, false);
	}

	public static StringObject collected(final String string, final IDrawingAttributes attributes) {
		return new StringObject(string, attributes, true);
	}

	private StringObject(final String string, final IDrawingAttributes attributes, final boolean collected) {
		super(string, attributes, DrawerType.STRING);
		this.collected = collected;
	}

	public int numberOfLines() {
		if (!collected) return 1;
		int count = 0;
		for (final String line : lineText()) { if (!line.isEmpty()) { count++; } }
		return count;
	}

	private String[] lineText() {
		if (text == null) { text = getObject().contains("\n") ? getObject().split("\\r?\\n", -1)
				: new String[] { getObject() }; }
		return text;
	}

	/** Layout is evaluated once by the renderer; collecting text does not measure fonts or split strings. */
	public List<StringObject> realizeLines(final double pixelsPerModelUnit) {
		if (!collected) return List.of(this);
		if (lines == null) {
			final String[] text = lineText();
			final double shift = text.length == 1 ? 0 : getAttributes().getFont().getAwtFont()
					.getLineMetrics("Ag", new FontRenderContext(null, true, true)).getHeight()
							/ pixelsPerModelUnit;
			final List<StringObject> result = new ArrayList<>(text.length);
			for (int i = 0; i < text.length; i++) {
				if (text[i].isEmpty()) continue;
				final IDrawingAttributes attributes = getAttributes().copyTranslatedBy(
						GamaPointFactory.create(0, shift * i));
				attributes.getLocation().negateY();
				result.add(new StringObject(text[i], attributes, false));
			}
			lines = result;
		}
		return lines;
	}

}
