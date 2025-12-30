/*******************************************************************************************************
 *
 * SVGInfo.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.metadata;

import gama.gaml.interfaces.IGamlDescription.Doc;
import gama.gaml.interfaces.IGamlDescription.RegularDoc;
import gama.gaml.operators.Strings;

/**
 * The Class SVGInfo.
 */
public class SVGInfo extends GamaFileMetaData {

	/** The width. */
	private final float width;

	/** The height. */
	private final float height;

	/** The number of groups. */
	private final int numberOfGroups;

	/**
	 * Instantiates a new SVG info.
	 *
	 * @param modificationStamp
	 *            the modification stamp
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param numberOfGroups
	 *            the number of groups
	 */
	public SVGInfo(final long modificationStamp, final float width, final float height, final int numberOfGroups) {
		super(modificationStamp);
		this.width = width;
		this.height = height;
		this.numberOfGroups = numberOfGroups;
	}

	/**
	 * Instantiates a new SVG info.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public SVGInfo(final String propertyString) {
		super(propertyString);
		final String[] segments = split(propertyString);
		width = Float.parseFloat(segments[1]);
		height = Float.parseFloat(segments[2]);
		if (segments.length > 3) {
			numberOfGroups = Integer.parseInt(segments[3]);
		} else {
			numberOfGroups = 0;
		}
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		if (numberOfGroups > 0) {
			sb.append(numberOfGroups).append(" group");
			if (numberOfGroups > 1) { sb.append("s"); }
			sb.append(SUFFIX_DEL);
		}
		sb.append(String.format("%.0f", width)).append("x").append(String.format("%.0f", height)).append(SUFFIX_DEL)
				.append("SVG");
	}

	@Override
	public Doc getDocumentation() {
		final RegularDoc sb = new RegularDoc();
		sb.append("SVG File").append(Strings.LN);
		sb.append("Dimensions: ").append(width + " x " + height).append(Strings.LN);
		if (numberOfGroups > 0) { sb.append("Groups: " + numberOfGroups).append(Strings.LN); }
		return sb;
	}

	@Override
	public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + width + DELIMITER + height + DELIMITER + numberOfGroups;
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public float getWidth() { return width; }

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public float getHeight() { return height; }

	/**
	 * Gets the number of groups.
	 *
	 * @return the number of groups
	 */
	public int getNumberOfGroups() { return numberOfGroups; }

}
