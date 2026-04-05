/*******************************************************************************************************
 *
 * SVGInfo.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image.svg;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;

import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.StringUtils;
import gama.api.utils.files.AbstractFileMetaData;
import gama.dev.DEBUG;

/**
 * The Class SVGInfo.
 */
public class SVGInfo extends AbstractFileMetaData {

	/** The width. */
	private float width;

	/** The height. */
	private float height;

	/** The number of groups. */
	private int numberOfGroups;

	/**
	 * Instantiates a new SVG info.
	 *
	 * @param file
	 *            the file
	 */
	public SVGInfo(final IFile file) {
		super(file);
		int groups = 0;
		try {
			SVGLoader loader = new SVGLoader();
			SVGDocument doc = loader.load(file.getLocationURI().toURL());
			if (doc != null) {
				width = doc.size().width;
				height = doc.size().height;
			}
			try (InputStream is = file.getContents()) {
				String content = new String(is.readAllBytes());
				groups = content.split("<g").length - 1;
			}
		} catch (Exception e) {
			DEBUG.ERR("Error reading SVG metadata for " + file.getName() + ": " + e.getMessage());
		}
		this.numberOfGroups = groups;
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
	public IGamlDocumentation getDocumentation() {
		final GamlRegularDocumentation sb = new GamlRegularDocumentation();
		sb.append("SVG File").append(StringUtils.LN);
		sb.append("Dimensions: ").append(width + " x " + height).append(StringUtils.LN);
		if (numberOfGroups > 0) { sb.append("Groups: " + numberOfGroups).append(StringUtils.LN); }
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
