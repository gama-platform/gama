/*******************************************************************************************************
 *
 * ImageInfo.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.core.resources.IFile;

import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.StringUtils;
import gama.api.utils.files.AbstractFileMetaData;
import gama.dev.DEBUG;

/**
 * The Class ImageInfo.
 */
public class ImageInfo extends AbstractFileMetaData {

	/** The Constant FORMATS. */
	public final static List<String> FORMATS = List.of("BMP", "WPMB", "GIF", "JPEG", "JPG", "PNG", "PNM", "ICO", "TIFF",
			"TIF", "ASCII ARCINFO", "PBM", "PGM", "PPM", "JP2");

	/** The type. */
	private int type;

	/** The width. */
	private int width;

	/** The height. */
	private int height;

	/**
	 * Instantiates a new image info.
	 *
	 * @param file
	 *            the file
	 */
	public ImageInfo(final IFile file) {
		super(file);
		String type = "Unknown Format";
		int width = -1, height = -1;
		try (ImageInputStream iis = ImageIO.createImageInputStream(file.getLocationURI().toURL().openStream())) {
			// DEBUG.LOG("Reading image metadata for " + file.getName());
			if (iis == null) {
				createFrom(file.getModificationStamp(), type, width, height);
			} else {
				final var readers = ImageIO.getImageReaders(iis);
				if (readers.hasNext()) {
					ImageReader reader;
					try {
						reader = readers.next();
					} catch (Exception e) {
						DEBUG.ERR("Error reading image metadata for " + file.getName() + ": " + e.getMessage());
						reader = null;
					}
					if (reader != null) {
						try {
							reader.setInput(iis);
							width = reader.getWidth(0);
							height = reader.getHeight(0);
							type = reader.getFormatName();
						} catch (Exception e) {
							DEBUG.ERR("Error reading image metadata for " + file.getName() + ": " + e.getMessage());
						} finally {
							reader.dispose();
						}
					}
				}
			}
		} catch (final Exception e) {}
		createFrom(file.getModificationStamp(), type, width, height);

	}

	/**
	 * Instantiates a new image info.
	 *
	 * @param modificationStamp
	 *            the modification stamp
	 * @param origType
	 *            the orig type
	 * @param origWidth
	 *            the orig width
	 * @param origHeight
	 *            the orig height
	 */
	public void createFrom(final long modificationStamp, /* final Object thumbnail, */final String origType,
			final int origWidth, final int origHeight) {
		this.type = FORMATS.indexOf(origType.toUpperCase());
		if (type == -1) { DEBUG.LOG("Unknown image format: " + origType); }
		this.width = origWidth;
		this.height = origHeight;
	}

	/**
	 * Instantiates a new image info.
	 *
	 * @param modificationStamp
	 *            the modification stamp
	 * @param origType
	 *            the orig type
	 * @param origWidth
	 *            the orig width
	 * @param origHeight
	 *            the orig height
	 */
	public void createFrom(final long modificationStamp, /* final Object thumbnail, */final int origType,
			final int origWidth, final int origHeight) {
		this.type = origType;
		this.width = origWidth;
		this.height = origHeight;
	}

	/**
	 * Instantiates a new image info.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public ImageInfo(final String propertyString) {
		super(propertyString);
		final String[] segments = split(propertyString);
		type = Integer.parseInt(segments[1]);
		width = Integer.parseInt(segments[2]);
		height = Integer.parseInt(segments[3]);
	}

	/**
	 * Gets the short label.
	 *
	 * @param type
	 *            the type
	 * @return the short label
	 */
	public String getShortLabel(final int type) {
		return type >= 0 && type < FORMATS.size() ? FORMATS.get(type) : "Unknown Format";
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		sb.append(width).append("x").append(height).append(SUFFIX_DEL).append(getShortLabel(type));
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		final GamlRegularDocumentation sb = new GamlRegularDocumentation();
		sb.append(getShortLabel(type)).append(" Image File").append(StringUtils.LN);
		sb.append("Dimensions: ").append(width + " pixels x " + height + " pixels").append(StringUtils.LN);
		return sb;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public int getType() { return type; }

	@Override
	public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + type + DELIMITER + width + DELIMITER + height;
	}
}