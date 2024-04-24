/*******************************************************************************************************
 *
 * GamaImageFile.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.geotools.data.PrjFileReader;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.FactoryException;

import com.google.common.io.Files;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.IConcept;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IImageProvider;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.topology.projection.IProjection;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.GamaFile;
import gama.core.util.file.GamaGridFile;
import gama.core.util.file.IFieldMatrixProvider;
import gama.core.util.file.IGamaFile;
import gama.core.util.matrix.GamaIntMatrix;
import gama.core.util.matrix.IField;
import gama.core.util.matrix.IMatrix;
import gama.gaml.operators.spatial.SpatialProjections;
import gama.gaml.statements.Facets;
import gama.gaml.types.GamaMatrixType;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class GamaImageFile.
 */
@file (
		name = "image",
		extensions = { "tiff", "jpg", "jpeg", "png", "pict", "bmp" },
		buffer_type = IType.MATRIX,
		buffer_content = IType.INT,
		buffer_index = IType.POINT,
		concept = { IConcept.IMAGE, IConcept.FILE },
		doc = @doc ("Image files can be of 6 different formats: tiff, jpeg, png, pict or bmp. Their internal representation is a matrix of colors"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaImageFile extends GamaFile<IMatrix<Integer>, Integer>
		implements IFieldMatrixProvider, IImageProvider, IGamaFile.Drawable {

	/**
	 * The Class GamaPgmFile.
	 */
	@file (
			name = "pgm",
			extensions = { "pgm" },
			buffer_type = IType.MATRIX,
			buffer_content = IType.INT,
			doc = @doc ("PGM files are special image files in 256 gray levels"))
	public static class GamaPgmFile extends GamaImageFile {

		/**
		 * @param scope
		 * @param pathName
		 * @throws GamaRuntimeException
		 */
		@doc (
				value = "This file constructor allows to read a pgm file",
				examples = { @example (
						value = "file f <-pgm_file(\"file.pgm\");",
						isExecutable = false) })

		public GamaPgmFile(final IScope scope, final String pathName) throws GamaRuntimeException {
			super(scope, pathName);
		}

		@Override
		protected boolean isPgmFile() { return true; }

		/*
		 * (non-Javadoc)
		 *
		 * @see gama.core.util.GamaFile#flushBuffer()
		 */
		@Override
		protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
			throw GamaRuntimeException
					.error("Saving is not yet impletemented for files of type " + this.getExtension(scope), scope);
		}

	}

	/** The is georeferenced. */
	// protected BufferedImage image;
	private boolean isGeoreferenced = false;

	/** The extension. */
	private String extension = null;

	/**
	 * Instantiates a new gama image file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read an image file (tiff, jpg, jpeg, png, pict, bmp)",
			examples = { @example (
					value = "file f <-image_file(\"file.png\");",
					isExecutable = false) })

	public GamaImageFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Instantiates a new gama image file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param pathName
	 *            the extension of the file
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read an image file (tiff, jpg, jpeg, png, pict, bmp) and to force the extension of the file (can be useful for images coming from URL)",
			examples = { @example (
					value = "file f <-image_file(\"http://my_url\", \"png\");",
					isExecutable = false) })

	public GamaImageFile(final IScope scope, final String pathName, final String extension)
			throws GamaRuntimeException {
		super(scope, pathName);
		this.extension = extension;
	}

	/**
	 * Instantiates a new gama image file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param image
	 *            the image
	 */
	@doc (
			value = "This file constructor allows to store a matrix in a image file (it does not save it - just store it in memory)",
			examples = { @example (
					value = "file f <-image_file(\"file.png\");",
					isExecutable = false) })
	public GamaImageFile(final IScope scope, final String pathName, final IMatrix<Integer> image) {
		super(scope, pathName, image);
		ImageCache.getInstance().clearCache(getPath(scope));
	}

	/**
	 * Instantiates a new gama image file.
	 *
	 * @param scope
	 *            the scope
	 * @param filename
	 *            the filename
	 * @param im
	 *            the im
	 */
	public GamaImageFile(final IScope scope, final String filename, final BufferedImage im) {
		super(scope, filename, null);
		ImageCache.getInstance().forceCacheImage(im, filename);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// No attributes
		return GamaListFactory.EMPTY_LIST;
	}

	@Override
	public IContainerType getGamlType() { return Types.FILE.of(Types.POINT, Types.INT); }

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		// Temporary workaround for pgm files, which can be read by ImageIO but
		// produce wrong results. See Issue 880.
		// TODO change this behavior
		setBuffer(isPgmFile() || "pgm".equals(getExtension(scope)) ? matrixValueFromPgm(scope, null)
				: matrixValueFromImage(scope, null));
	}

	/**
	 * Checks if is pgm file.
	 *
	 * @return true, if is pgm file
	 */
	protected boolean isPgmFile() { return false; }

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.util.GamaFile#flushBuffer()
	 */
	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		if (!writable || getBuffer() == null || getBuffer().isEmpty(scope)) return;
		try {
			final File f = getFile(scope);
			f.setWritable(true);
			ImageIO.write(GamaIntMatrix.constructBufferedImageFromMatrix(scope, getBuffer()), getExtension(scope), f);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	@Override
	protected IMatrix _matrixValue(final IScope scope, final IType contentsType, final GamaPoint preferredSize,
			final boolean copy) throws GamaRuntimeException {
		getContents(scope);
		if (preferredSize != null)
			return matrixValueFromImage(scope, preferredSize).matrixValue(scope, contentsType, copy);
		return getBuffer().matrixValue(scope, contentsType, copy);
	}

	/**
	 * Load image.
	 *
	 * @param scope
	 *            the scope
	 * @param useCache
	 *            the use cache
	 * @return the buffered image
	 */
	protected BufferedImage loadImage(final IScope scope, final boolean useCache) {
		// if (image == null) {
		final BufferedImage image;
		try {
			image = ImageCache.getInstance().getImageFromFile(scope, getPath(scope), useCache, null, extension);
			if (image == null) throw GamaRuntimeException.error("This image format (." + getExtension(scope)
					+ ") is not recognized. Please use a proper operator to read it (for example, pgm_file to read a .pgm format",
					scope);
		} catch (final Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(e, scope), true);
			return null;
		}
		// }
		return image;
	}

	/**
	 * Gets the image.
	 *
	 * @param scope
	 *            the scope
	 * @param useCache
	 *            the use cache
	 * @return the image
	 */
	@Override
	public BufferedImage getImage(final IScope scope, final boolean useCache) {
		return loadImage(scope, useCache);
	}

	/**
	 * Matrix value from image.
	 *
	 * @param scope
	 *            the scope
	 * @param preferredSize
	 *            the preferred size
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private IMatrix matrixValueFromImage(final IScope scope, final GamaPoint preferredSize)
			throws GamaRuntimeException {
		final BufferedImage image = loadImage(scope, true);
		return matrixValueFromImage(scope, image, preferredSize);
	}

	/**
	 * Matrix value from image.
	 *
	 * @param scope
	 *            the scope
	 * @param image
	 *            the image
	 * @param preferredSize
	 *            the preferred size
	 * @return the i matrix
	 */
	public static IMatrix matrixValueFromImage(final IScope scope, final BufferedImage image,
			final GamaPoint preferredSize) {
		int xSize, ySize;
		BufferedImage resultingImage = image;
		if (preferredSize == null) {
			xSize = image.getWidth();
			ySize = image.getHeight();
		} else {
			xSize = (int) preferredSize.getX();
			ySize = (int) preferredSize.getY();
			resultingImage = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
			final Graphics2D g = resultingImage.createGraphics();
			g.drawImage(image, 0, 0, xSize, ySize, null);
			g.dispose();
			// image = resultingImage;
		}
		final IMatrix matrix = new GamaIntMatrix(xSize, ySize);
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) { matrix.set(scope, i, j, resultingImage.getRGB(i, j)); }
		}
		return matrix;

	}

	/**
	 * Matrix value from pgm.
	 *
	 * @param scope
	 *            the scope
	 * @param preferredSize
	 *            the preferred size
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	private IMatrix matrixValueFromPgm(final IScope scope, final GamaPoint preferredSize) throws GamaRuntimeException {
		// TODO PreferredSize is not respected here
		try (BufferedReader in = new BufferedReader(new FileReader(getFile(scope)))) {
			StringTokenizer tok;
			String str = in.readLine();
			if (str != null && !"P2".equals(str))
				throw new UnsupportedEncodingException("File is not in PGM ascii format");
			str = in.readLine();
			if (str == null) return GamaMatrixType.with(scope, 0, preferredSize, Types.INT);
			tok = new StringTokenizer(str);
			final int xSize = Integer.parseInt(tok.nextToken());
			final int ySize = Integer.parseInt(tok.nextToken());
			in.readLine();
			final StringBuilder buf = new StringBuilder();
			String line = in.readLine();
			while (line != null) {
				buf.append(line);
				buf.append(' ');
				line = in.readLine();
			}
			// in.close();
			str = buf.toString();
			tok = new StringTokenizer(str);
			final IMatrix matrix = new GamaIntMatrix(xSize, ySize);
			for (int j = 0; j < ySize; j++) {
				for (int i = 0; i < xSize; i++) {
					final Integer val = Integer.valueOf(tok.nextToken());
					matrix.set(scope, i, j, val);
				}
			}
			return matrix;
		} catch (final Throwable ex) {
			throw GamaRuntimeException.create(ex, scope);
		}

	}

	/**
	 * Gets the geo data file.
	 *
	 * @param scope
	 *            the scope
	 * @return the geo data file
	 */
	public String getGeoDataFile(final IScope scope) {
		final String extension = getExtension(scope);
		String val = null;
		String geodataFile = getPath(scope).replaceAll(extension, "");
		if ("jpg".equals(extension)) {
			geodataFile = geodataFile + "jgw";
		} else if ("png".equals(extension)) {
			geodataFile = geodataFile + "pgw";
		} else if ("tiff".equals(extension) || "tif".equals(extension)) {
			geodataFile = geodataFile + "tfw";
			val = "";
		} else
			return null;
		final File infodata = new File(geodataFile);
		if (infodata.exists()) return geodataFile;
		return val;
	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		final String geodataFile = getGeoDataFile(scope);
		double cellSizeX = 1;
		double cellSizeY = 1;
		double xllcorner = 0;
		double yllcorner = 0;
		boolean xNeg = false;
		boolean yNeg = false;
		final String extension = getExtension(scope);
		if (geodataFile != null && !"".equals(geodataFile)) {
			try (	final InputStream ips = java.nio.file.Files.newInputStream(new File(geodataFile).toPath());
					final InputStreamReader ipsr = new InputStreamReader(ips);
					final BufferedReader in = new BufferedReader(ipsr);) {
				String line = in.readLine();
				if (line != null) {
					final String[] cellSizeXStr = line.split(" ");
					cellSizeX = Double.parseDouble(cellSizeXStr[cellSizeXStr.length - 1]);
				}
				xNeg = cellSizeX < 0;
				line = in.readLine();
				line = in.readLine();
				line = in.readLine();
				if (line != null) {
					final String[] cellSizeYStr = line.split(" ");
					cellSizeY = Double.parseDouble(cellSizeYStr[cellSizeYStr.length - 1]);
				}
				yNeg = cellSizeY < 0;
				line = in.readLine();
				if (line != null) {
					final String[] xllcornerStr = line.split(" ");
					xllcorner = Double.parseDouble(xllcornerStr[xllcornerStr.length - 1]);
				}
				line = in.readLine();
				if (line != null) {
					final String[] yllcornerStr = line.split(" ");
					yllcorner = Double.parseDouble(yllcornerStr[yllcornerStr.length - 1]);
				}
				isGeoreferenced = true;

			} catch (final Throwable e) {
				throw GamaRuntimeException.create(e, scope);
			}
		} else if ("tiff".equals(extension) || "tif".equals(extension)) {
			final GamaGridFile file = new GamaGridFile(null, this.getPath(scope));

			final Envelope e = file.computeEnvelope(scope);
			if (e != null) {
				GamaPoint minCorner = new GamaPoint(e.getMinX(), e.getMinY());
				GamaPoint maxCorner = new GamaPoint(e.getMaxX(), e.getMaxY());
				if (geodataFile != null) {
					IProjection pr;
					try {
						pr = scope.getSimulation().getProjectionFactory().forSavingWith(scope,
								file.gis.getTargetCRS(scope));
						minCorner =
								GamaShapeFactory.createFrom(pr.transform(minCorner.getInnerGeometry())).getLocation();
						maxCorner =
								GamaShapeFactory.createFrom(pr.transform(maxCorner.getInnerGeometry())).getLocation();
					} catch (final FactoryException e1) {
						e1.printStackTrace();
					}

				}
				isGeoreferenced = true;
				return Envelope3D.of(minCorner.x, maxCorner.x, minCorner.y, maxCorner.y, 0, 0);
			}

		}
		final int nbCols = getCols(scope);
		final int nbRows = getRows(scope);

		final double x1 = xllcorner;
		final double x2 = xllcorner + cellSizeX * nbCols;
		final double y1 = yllcorner;
		final double y2 = yllcorner + cellSizeY * nbRows;
		GamaPoint minCorner =
				new GamaPoint(xNeg ? Math.max(x1, x2) : Math.min(x1, x2), yNeg ? Math.max(y1, y2) : Math.min(y1, y2));
		GamaPoint maxCorner =
				new GamaPoint(xNeg ? Math.min(x1, x2) : Math.max(x1, x2), yNeg ? Math.min(y1, y2) : Math.max(y1, y2));
		if (geodataFile != null) {
			String fp = this.getPath(scope);
			String path = fp.replace(Files.getFileExtension(fp), "prj");
			File f = new File(path);
			String crs = "EPSG:3857";
			if (f.exists()) {
				FileChannel rdc = null;
				try {
					rdc = FileChannel.open(f.toPath(), StandardOpenOption.READ);
				} catch (IOException e) {}
				if (rdc != null) {
					try (PrjFileReader pfr = new PrjFileReader(rdc)) {
						if (pfr.getCoordinateReferenceSystem() != null) {
							IProjection gis = scope.getSimulation().getProjectionFactory().forSavingWith(scope,
									pfr.getCoordinateReferenceSystem());
							if (gis != null) {
								minCorner = GamaShapeFactory.createFrom(gis.transform(minCorner.getInnerGeometry()))
										.getLocation();
								maxCorner = GamaShapeFactory.createFrom(gis.transform(maxCorner.getInnerGeometry()))
										.getLocation();
								return Envelope3D.of(minCorner.x, maxCorner.x, minCorner.y, maxCorner.y, 0, 0);
							}
						}
					} catch (IOException | FactoryException e) {}

				}

			}

			minCorner = SpatialProjections.to_GAMA_CRS(scope, minCorner, crs).getLocation();
			maxCorner = SpatialProjections.to_GAMA_CRS(scope, maxCorner, crs).getLocation();
		}

		return Envelope3D.of(minCorner.x, maxCorner.x, minCorner.y, maxCorner.y, 0, 0);

	}

	/**
	 * Checks if is georeferenced.
	 *
	 * @return true, if is georeferenced
	 */
	public boolean isGeoreferenced() { return isGeoreferenced; }

	@Override
	public double getNoData(final IScope scope) {
		return IField.NO_NO_DATA;
	}

	@Override
	public int getRows(final IScope scope) {
		final BufferedImage image = loadImage(scope, true);
		if (image == null) return 0;
		return image.getHeight();
	}

	@Override
	public int getCols(final IScope scope) {
		final BufferedImage image = loadImage(scope, true);
		if (image == null) return 0;
		return image.getWidth();
	}

	@Override
	public int getBandsNumber(final IScope scope) {
		BufferedImage image = getImage(scope, true);
		return image.getColorModel().getNumComponents();
	}

	/**
	 * Gets the extension.
	 *
	 * @return the extension
	 */
	public String getExtension() { return extension; }

	@Override
	public double[] getBand(final IScope scope, final int index) {
		BufferedImage image = getImage(scope, true);
		final double[] values = new double[image.getWidth() * image.getHeight()];
		int[] pixels = new int[values.length];
		PixelGrabber pgb =
				new PixelGrabber(image, 0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		try {
			pgb.grabPixels();
		} catch (InterruptedException e) {}
		for (int i = 0; i < values.length; ++i) {
			// Verify this ... Especially if the number of color components does not correspond
			values[i] = pixels[i] & (index + 1) * 255;
		}
		return values;
	}

	/**
	 * Checks for geo data available.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @date 15 juil. 2023
	 */
	@Override
	public boolean hasGeoDataAvailable(final IScope scope) {
		return getGeoDataFile(scope) != null;
	}
}
