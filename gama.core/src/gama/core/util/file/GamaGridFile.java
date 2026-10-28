/*******************************************************************************************************
 *
 * GamaGridFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static gama.core.topology.gis.ProjectionFactory.getTargetCRSOrDefault;
import static org.geotools.util.factory.Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.geotools.api.coverage.grid.GridCoverageWriter;
import org.geotools.api.geometry.Position;
import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.crs.ProjectedCRS;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataDecoder;
import org.geotools.data.PrjFileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.Envelope2DArchived;
import org.geotools.geometry.GeneralBounds;
import org.geotools.geometry.Position2D;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.support.IConcept;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IField;
import gama.api.types.matrix.IMatrix;
import gama.api.ui.IStatusMessage;
import gama.api.ui.TaskProgress;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.interfaces.IFieldMatrixProvider;
import gama.core.topology.gis.GamaCRS;
import gama.core.topology.gis.ProjectionFactory;
import gama.core.util.matrix.GamaFloatMatrix;

/**
 * The Class GamaGridFile.
 */
@file (
		name = "grid",
		extensions = { "asc", "tif" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.GRID, IConcept.ASC, IConcept.TIF, IConcept.FILE },
		doc = @doc ("Represents .asc or .tif files that contain grid descriptions. ASC files may use CELLSIZE or separate DX and DY values, with spaces or tabs as separators. NODATA_value is optional."))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaGridFile extends GamaGisFile implements IFieldMatrixProvider {

	/**
	 * The Class Records.
	 */
	static class Records {

		/** The bands. */
		final List<double[]> bands = new ArrayList<>();

		/**
		 * Fill.
		 *
		 * @param i
		 *            the i
		 * @param bands2
		 *            the bands 2
		 */
		public void fill(final int i, final IList<Double> bands2) {
			for (double[] tab : bands) { bands2.add(tab[i]); }
		}
	}

	/** The coverage. */
	transient GridCoverage2D coverage;

	/** The asc data. */
	GamaFloatMatrix ascData;

	/** The asc info. */
	Double[] ascInfo;

	/** The num cols. */
	public int nbBands, numRows, numCols;

	/** The geom. */
	IShape geom;

	/** The no data. */
	Number noData = IField.NO_NO_DATA;

	/** The genv. */
	GeneralBounds genv;

	/** The records. */
	Records records;

	/**
	 * Instantiates a new gama grid file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a asc file or a tif (geotif) file",
			examples = { @example (
					value = "file f <- grid_file(\"file.asc\");",
					isExecutable = false) })

	public GamaGridFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null);
	}

	/**
	 * Instantiates a new gama grid file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param asMatrix
	 *            the as matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a asc file or a tif (geotif) file, but without converting it into shapes. Only a matrix of float values is created",
			examples = { @example (
					value = "file f <- grid_file(\"file.asc\", false);",
					isExecutable = false) })

	public GamaGridFile(final IScope scope, final String pathName, final boolean asMatrix) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null);
	}

	/**
	 * Instantiates a new gama grid file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a asc file or a tif (geotif) file specifying the coordinates system code, as an int (epsg code)",
			examples = { @example (
					value = "file f <- grid_file(\"file.asc\", 32648);",
					isExecutable = false) })
	public GamaGridFile(final IScope scope, final String pathName, final Integer code) throws GamaRuntimeException {
		super(scope, pathName, code);
	}

	/**
	 * Instantiates a new gama grid file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 */
	@doc (
			value = "This file constructor allows to read a asc file or a tif (geotif) file specifying the coordinates system code (epg,...,), as a string ",
			examples = { @example (
					value = "file f <- grid_file(\"file.asc\",\"EPSG:32648\");",
					isExecutable = false) })
	public GamaGridFile(final IScope scope, final String pathName, final String code) {
		super(scope, pathName, code);
	}

	/**
	 * Instantiates a new gama grid file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param field
	 *            the field
	 */
	@doc (
			value = "This allows to build a writable grid file from the values of a field",
			examples = { @example (
					value = "file f <- grid_file(\"file.tif\",my_field); save f;",
					isExecutable = false) })
	public GamaGridFile(final IScope scope, final String pathName, final IField field) {
		super(scope, pathName, false);
		setWritable(scope, true);
		createCoverage(scope, field);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// No attributes
		return GamaListFactory.getEmptyList();
	}

	/**
	 * Creates the coverage.
	 *
	 * @param scope
	 *            the scope
	 */
	private void createCoverage(final IScope scope) {
		if (coverage == null && ascData == null) {
			final File gridFile = getFile(scope);
			gridFile.setReadable(true);
			InputStream fis = null;
			try {
				fis = Files.newInputStream(gridFile.toPath());
			} catch (IOException e1) {}
			try {
				privateCreateCoverage(scope, fis);
			} catch (final Exception e) {
				if (isTiff(scope)) throw GamaRuntimeException
						.error("The format of " + getName(scope) + " seems incorrect: " + e.getMessage(), scope);
				// A problem appeared, likely related to the wrong format of the file (see Issue 412)
				// reportError(scope, warning("Format of " + name + " seems incorrect. Trying to read it anyway.",
				// scope),
				// false);

				customAscReader(scope);
				/*
				 * try { fis = fixFileHeader(scope); } catch (UnsupportedEncodingException e2) { e2.printStackTrace(); }
				 * try { privateCreateCoverage(scope, fis); } catch (IOException e1) { e1.printStackTrace(); }
				 */
			}
		}
	}

	/**
	 * Double val.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param line
	 *            the line
	 * @return the double
	 * @date 31 août 2023
	 */
	private Double doubleVal(final String line) {
		String[] l = line.trim().split("\\s+");
		if (l.length > 1) return Double.valueOf(l[l.length - 1]);
		return null;
	}

	/**
	 * Int val.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param line
	 *            the line
	 * @return the integer
	 * @date 31 août 2023
	 */
	private Integer intVal(final String line) {

		String[] l = line.trim().split("\\s+");
		if (l.length > 1) return Integer.valueOf(l[l.length - 1]);
		return null;
	}

	/**
	 * Custom asc reader.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @date 31 août 2023
	 */
	private void customAscReader(final IScope scope) {
		int lineNumber = 0;
		try (Scanner scanner = new Scanner(getFile(scope))) {
			boolean headingComplete = false;
			Integer nbCols = null;
			Integer nbRows = null;
			Double xCorner = null;
			Double yCorner = null;
			Double xCenter = null;
			Double yCenter = null;
			Double dX = null;
			Double dY = null;
			Double noDataD = null;
			ascInfo = new Double[4];
			int j = 0;
			while (scanner.hasNextLine()) {
				lineNumber++;
				String line = scanner.nextLine().trim();
				if (line.isEmpty()) { continue; }
				final String header = line.toLowerCase(java.util.Locale.ROOT);
				if (!headingComplete) {
					if (dX == null && header.startsWith("dx")) {
						dX = doubleVal(line);
						ascInfo[0] = dX;
					} else if (dY == null && header.startsWith("dy")) {
						dY = doubleVal(line);
						ascInfo[1] = dY;
					} else if ((dX == null || dY == null) && header.startsWith("cellsize")) {
						Double cellSize = doubleVal(line);
						if (dX == null) {
							dX = cellSize;
							ascInfo[0] = dX;
						}
						if (dY == null) {
							dY = cellSize;
							ascInfo[1] = dY;
						}
					} else if (nbCols == null && header.startsWith("ncols")) {
						nbCols = intVal(line);
					} else if (nbRows == null && header.startsWith("nrows")) {
						nbRows = intVal(line);
					} else if (noDataD == null && header.startsWith("nodata")) {
						noDataD = header.contains("nan") ? Double.NaN : doubleVal(line);
					} else if (xCorner == null && xCenter == null && header.startsWith("xllcorner")) {
						xCorner = doubleVal(line);
						ascInfo[2] = xCorner;
					} else if (yCorner == null && yCenter == null && header.startsWith("yllcorner")) {
						yCorner = doubleVal(line);
					} else if (xCorner == null && xCenter == null && header.startsWith("xllcenter")) {
						xCenter = doubleVal(line);
					} else if (yCorner == null && yCenter == null && header.startsWith("yllcenter")) {
						yCenter = doubleVal(line);
					} else {
						if (nbCols == null || nbCols <= 0 || nbRows == null || nbRows <= 0)
							throw GamaRuntimeException.error("The format of " + getName(scope)
									+ " is not correct. Error: NCOLS and NROWS must be positive", scope);
						if (dX == null || dY == null || !Double.isFinite(dX) || !Double.isFinite(dY)
								|| dX <= 0 || dY <= 0)
							throw GamaRuntimeException.error("The format of " + getName(scope)
									+ " is not correct. Error: positive CELLSIZE or DX and DY must be defined", scope);
						if (xCenter != null) {
							xCorner = xCenter - dX / 2.0;
							ascInfo[2] = xCorner;
						}
						if (yCenter != null) { yCorner = yCenter - dY / 2.0; }

						ascData = (GamaFloatMatrix) GamaMatrixFactory.createFloatMatrix(nbCols, nbRows);
						this.noData = noDataD == null ? IField.NO_NO_DATA : noDataD;
						double xC = xCorner == null ? 0 : xCorner;
						double yC = yCorner == null ? 0 : yCorner;
						ascInfo[2] = xC;
						ascInfo[3] = yC + nbRows * dY;
						final IEnvelope env =
								GamaEnvelopeFactory.of(xC, xC + nbCols * dX, yC, yC + nbRows * dY, 0, 0);
						computeProjection(scope, env);
						numRows = nbRows;
						numCols = nbCols;

						headingComplete = true;
					}
				}
				if (headingComplete) {
					String[] l = line.split("\\s+");
					if (j >= nbRows || l.length != nbCols)
						throw GamaRuntimeException.error("The format of " + getName(scope)
								+ " is not correct. Error: raster data does not match NCOLS and NROWS", scope);
					for (int i = 0; i < l.length; i++) {
						ascData.set(scope, i, j, "nan".equalsIgnoreCase(l[i]) ? Double.NaN : Double.valueOf(l[i]));
					}
					j++;
				}
			}
			if (!headingComplete || j != nbRows)
				throw GamaRuntimeException.error("The format of " + getName(scope)
						+ " is not correct. Error: incomplete raster data", scope);
		} catch (final FileNotFoundException e2) {
			throw GamaRuntimeException
					.error("The format of " + getName(scope) + " is not correct. Error: " + e2.getMessage(), scope);
		} catch (final RuntimeException e) {
			ascData = null;
			final GamaRuntimeException error = e instanceof NumberFormatException
					? GamaRuntimeException.error("The format of " + getName(scope)
							+ " is not correct. Error: invalid numeric value: " + e.getMessage(), scope)
					: GamaRuntimeException.create(e, scope);
			error.addContext("Reading " + getName(scope) + " at ASC line " + lineNumber);
			throw error;
		}

	}

	/**
	 * Creates the coverage.
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 */
	private void createCoverage(final IScope scope, final IField field) {
		// temporary fixes #3128 - the code comes from the save statement... maybe we can do better

		// old code
		/*
		 * double[] data = field.getMatrix();
		 *
		 * DataBuffer buffer = new DataBufferDouble(data, data.length); SampleModel sample = new
		 * BandedSampleModel(DataBuffer.TYPE_DOUBLE, field.numCols, field.numRows, field.getBandsNumber(scope));
		 * WritableRaster raster = Raster.createWritableRaster(sample, buffer, null); Envelope2D envelope = new
		 * Envelope2D(getCRS(scope), 0, 0, scope.getSimulation().getWidth(), scope.getSimulation().getHeight());
		 * GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null); GridCoverage2D cov =
		 * factory.create(getName(scope), raster, envelope); coverage = cov;
		 */
		final boolean nullProjection = scope.getSimulation().getProjectionFactory().getWorld() == null;

		final int cols = field.getCols(scope);
		final int rows = field.getRows(scope);
		double x = nullProjection ? 0
				: scope.getSimulation().getProjectionFactory().getWorld().getProjectedEnvelope().getMinX();
		double y = nullProjection ? 0
				: scope.getSimulation().getProjectionFactory().getWorld().getProjectedEnvelope().getMinY();

		final float[][] imagePixelData = new float[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) { imagePixelData[row][col] = field.get(scope, col, row).floatValue(); }

		}
		final double width = scope.getSimulation().getEnvelope().getWidth();
		final double height = scope.getSimulation().getEnvelope().getHeight();

		Envelope2DArchived refEnvelope =
				new Envelope2DArchived(getTargetCRSOrDefault(scope).getCRS(), x, y, width, height);

		coverage = new GridCoverageFactory().create("data", imagePixelData, refEnvelope);

	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		setWritable(scope, true);
		ensureCoverage(scope);
		if (coverage == null) return;

		GridCoverageWriter writer = null;
		try {
			final File file = prepareOutputFile(scope);
			writer = createGridWriter(scope, file);
			writer.write(coverage, (GeneralParameterValue[]) null);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			disposeWriter(writer);
			ProjectionFactory.saveTargetCRSAsPRJFile(scope, getFile(scope).getAbsolutePath());
		}
	}

	/**
	 * Ensure coverage.
	 *
	 * @param scope
	 *            the scope
	 */
	private void ensureCoverage(final IScope scope) {
		if (coverage != null) return;
		createCoverage(scope);
		if (coverage == null && hasFallbackData()) { createCoverage(scope, getField(scope)); }
	}

	/**
	 * Checks for fallback data.
	 *
	 * @return true, if successful
	 */
	private boolean hasFallbackData() {
		return ascData != null || getBuffer() != null;
	}

	/**
	 * Prepare output file.
	 *
	 * @param scope
	 *            the scope
	 * @return the file
	 */
	private File prepareOutputFile(final IScope scope) {
		final File file = getFile(scope);
		file.setWritable(true);
		return file;
	}

	/**
	 * Creates the grid writer.
	 *
	 * @param scope
	 *            the scope
	 * @param file
	 *            the file
	 * @return the grid coverage writer
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private GridCoverageWriter createGridWriter(final IScope scope, final File file) throws IOException {
		if (isTiff(scope)) return new GeoTiffFormat().getWriter(file);
		return new ArcGridWriter(file);
	}

	/**
	 * Dispose writer.
	 *
	 * @param writer
	 *            the writer
	 */
	private void disposeWriter(final GridCoverageWriter writer) {
		if (writer == null) return;
		try {
			writer.dispose();
		} catch (final Exception ignored) {}
	}

	/**
	 * Private create coverage.
	 *
	 * @param scope
	 *            the scope
	 * @param fis
	 *            the fis
	 * @throws DataSourceException
	 *             the data source exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void privateCreateCoverage(final IScope scope, final InputStream fis) {
		AbstractGridCoverage2DReader store = null;
		scope.getGui().getStatus().beginTask("Opening file " + getName(scope), IStatusMessage.DOWNLOAD_ICON);
		try {
			final ICoordinateReferenceSystem crs = getExistingCRS(scope);
			CoordinateReferenceSystem geoToolsCrs = crs == null ? null : crs.getCRS();
			Hints hints = new Hints();
			hints.put(Hints.SKIP_EXTERNAL_OVERVIEWS, Boolean.TRUE);

			if (geoToolsCrs != null) { hints.put(DEFAULT_COORDINATE_REFERENCE_SYSTEM, geoToolsCrs); }

			if (isTiff(scope)) {
				// If no CRS resolved yet, try extracting GeoKeys EPSG directly
				if (geoToolsCrs == null) {
					Integer epsgCode = extractEPSGCode(getFile(scope));
					if (epsgCode != null) {
						try {
							geoToolsCrs = CRS.decode("EPSG:" + epsgCode);
							hints.put(DEFAULT_COORDINATE_REFERENCE_SYSTEM, geoToolsCrs);
						} catch (Throwable ignored) {}
					}
				}

				try {
					store = new GeoTiffReader(getFile(scope), hints);
				} catch (Throwable e) {
					// Hard fallback
					Integer epsgCode = extractEPSGCode(getFile(scope));
					if (epsgCode == null) throw e;
					try {
						hints.put(DEFAULT_COORDINATE_REFERENCE_SYSTEM, CRS.decode("EPSG:" + epsgCode));
					} catch (Throwable ignored) {
						// The code found in the file cannot be decoded: report the original error
						throw e;
					}
					store = new GeoTiffReader(getFile(scope), hints);
				}
				noData = ((GeoTiffReader) store).getMetadata().getNoData();
			} else {
				store = new ArcGridReader(fis, hints);
			}
			genv = store.getOriginalEnvelope();
			final IEnvelope env = GamaEnvelopeFactory.of(genv.getMinimum(0), genv.getMaximum(0), genv.getMinimum(1),
					genv.getMaximum(1), 0, 0);
			computeProjection(scope, env);
			numRows = store.getOriginalGridRange().getHigh(1) + 1;
			numCols = store.getOriginalGridRange().getHigh(0) + 1;
			coverage = store.read(null);
		} catch (Throwable e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			if (store != null) { store.dispose(); }
			scope.getGui().getStatus().endTask("Opening file " + getName(scope), IStatusMessage.DOWNLOAD_ICON);
		}
	}

	/**
	 * Gets the value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param locX
	 *            the loc X
	 * @param locY
	 *            the loc Y
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @return the value
	 * @date 31 août 2023
	 */
	private double[] getValue(final IScope scope, final Double locX, final Double locY, final int i, final int j) {
		if (coverage != null) return coverage.evaluate((Position) new Position2D(locX, locY), (double[]) null);
		double[] v = new double[1];
		v[0] = ascData.get(scope, i, j);
		return v;
	}

	/**
	 * Read.
	 *
	 * @param scope
	 *            the scope
	 * @param readAll
	 *            the read all
	 * @param createGeometries
	 *            the create geometries
	 */
	void read(final IScope scope, final boolean readAll, final boolean createGeometries) {
		try {
			final IEnvelope envP = gis == null ? scope.getSimulation().getEnvelope() : gis.getProjectedEnvelope();
			if (gis != null && !(gis.getInitialCRS(scope).getCRS() instanceof ProjectedCRS)) {
				GAMA.reportError(scope, GamaRuntimeException.warning("Try to project a grid -" + this.originalPath
						+ "-  that is not projected. Projection of grids can lead to errors in the cell coordinates. ",
						scope), false);
			}
			final double cellHeight = envP.getHeight() / numRows;
			final double cellWidth = envP.getWidth() / numCols;
			final IList<IShape> shapes = GamaListFactory.create(Types.GEOMETRY);
			final double originX = envP.getMinX();
			final double originY = envP.getMinY();
			final double maxY = envP.getMaxY();
			final double maxX = envP.getMaxX();
			shapes.add(GamaPointFactory.create(originX, originY));
			shapes.add(GamaPointFactory.create(maxX, originY));
			shapes.add(GamaPointFactory.create(maxX, maxY));
			shapes.add(GamaPointFactory.create(originX, maxY));
			shapes.add(shapes.get(0));
			geom = GamaShapeFactory.buildPolygon(shapes);
			if (!readAll) return;

			final double cmx = cellWidth / 2;
			final double cmy = cellHeight / 2;
			double cellHeightP;
			double cellWidthP;
			double originXP;
			double maxYP;
			if (genv != null) {
				cellHeightP = genv.getSpan(1) / numRows;
				cellWidthP = genv.getSpan(0) / numCols;
				originXP = genv.getMinimum(0);
				maxYP = genv.getMaximum(1);

			} else {
				cellHeightP = ascInfo[1];
				cellWidthP = ascInfo[0];
				originXP = ascInfo[2];
				maxYP = ascInfo[3];
			}
			final double cmxP = cellWidthP / 2;
			final double cmyP = cellHeightP / 2;

			if (records == null) {
				final long size = (long) numRows * numCols;
				if (size > Integer.MAX_VALUE - 8) throw GamaRuntimeException.error("The grid " + getName(scope)
						+ " contains too many cells (" + numCols + "x" + numRows + ") to be loaded in memory", scope);
				final Records r = new Records();
				try (TaskProgress progress = TaskProgress.begin(scope, "Reading file " + getName(scope))) {
					if (coverage == null || !readCoverageByTiles(progress, r)) {
						r.bands.clear();
						r.bands.add(new double[(int) size]); // data
						for (int i = 0, n = (int) size; i < n; i++) {
							if (i % numCols == 0) { progress.report(i, n); }
							final int yy = i / numCols;
							final int xx = i - yy * numCols;
							double[] vd = getValue(scope, originXP + xx * cellWidthP + cmxP,
									maxYP - (yy * cellHeightP + cmyP), xx, yy);
							nbBands = vd.length;
							if (i == 0 && vd.length > 1) {
								for (int j = 0; j < vd.length - 1; j++) { r.bands.add(new double[(int) size]); }
							}
							for (int j = 0; j < vd.length; j++) { r.bands.get(j)[i] = vd[j]; }
						}
					}
				}
				records = r;
			}
			if (getBuffer() == null && createGeometries) {
				// Building geometries
				try (TaskProgress progress = TaskProgress.begin(scope, "Building the cells of " + getName(scope))) {
					setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
					for (int i = 0, n = numRows * numCols; i < n; i++) {
						if (i % numCols == 0) { progress.report(i, n); }
						final int yy = i / numCols;
						final int xx = i - yy * numCols;
						final IPoint p = GamaPointFactory.create(originX + xx * cellWidth + cmx,
								maxY - (yy * cellHeight + cmy));
						IShape rect = GamaShapeFactory.buildRectangle(cellWidth, cellHeight, p);
						if (gis == null) {
							rect = GamaShapeFactory.createFrom(rect.getInnerGeometry());
						} else {
							rect = GamaShapeFactory.createFrom(gis.transform(rect.getInnerGeometry()));
						}
						IList<Double> bands = GamaListFactory.create(scope, Types.FLOAT);
						records.fill(i, bands);
						rect.setAttribute("grid_value", bands.get(0));
						rect.setAttribute("bands", bands);
						getBuffer().add(rect);
					}
				}
			}
		} catch (final Exception e) {
			throw GamaRuntimeException
					.error("The format of " + getName(scope) + " is not correct. Error: " + e.getMessage(), scope);
		}

	}

	/**
	 * Reads all the values of the coverage directly from its image, tile by tile, instead of evaluating the coverage
	 * at the center of every cell (which is much slower and requires much more memory for large rasters). Only
	 * possible when the image has the same dimensions as the grid and is not rotated; otherwise, returns false so that
	 * the cell-by-cell evaluation is used instead.
	 *
	 * @param progress
	 *            used to report the progress of the reading
	 * @param r
	 *            the records to fill
	 * @return true if the values have been read
	 */
	private boolean readCoverageByTiles(final TaskProgress progress, final Records r) {
		final RenderedImage image = coverage.getRenderedImage();
		if (image == null || image.getWidth() != numCols || image.getHeight() != numRows) return false;
		if (!(coverage.getGridGeometry() instanceof GridGeometry2D gg)
				|| !(gg.getGridToCRS2D() instanceof AffineTransform at) || at.getShearX() != 0 || at.getShearY() != 0)
			return false;
		// GAMA's cells are ordered from the north-west corner: flip the image if its axes are not oriented this way
		final boolean flipX = at.getScaleX() < 0;
		final boolean flipY = at.getScaleY() > 0;
		final int bands = image.getSampleModel().getNumBands();
		final int size = numRows * numCols;
		r.bands.clear();
		for (int b = 0; b < bands; b++) { r.bands.add(new double[size]); }
		nbBands = bands;
		final int minX = image.getMinX(), minY = image.getMinY();
		final Rectangle bounds = new Rectangle(minX, minY, numCols, numRows);
		final int tilesX = image.getNumXTiles(), tilesY = image.getNumYTiles();
		final double nbTiles = (double) tilesX * tilesY;
		double[] samples = null;
		for (int ty = 0; ty < tilesY; ty++) {
			for (int tx = 0; tx < tilesX; tx++) {
				progress.report((ty * tilesX + tx) / nbTiles);
				final Raster tile = image.getTile(image.getMinTileX() + tx, image.getMinTileY() + ty);
				final Rectangle area = tile.getBounds().intersection(bounds);
				if (area.isEmpty()) { continue; }
				final int w = area.width, h = area.height;
				if (samples == null || samples.length < w * h) { samples = new double[w * h]; }
				for (int b = 0; b < bands; b++) {
					tile.getSamples(area.x, area.y, w, h, b, samples);
					final double[] target = r.bands.get(b);
					for (int y = 0; y < h; y++) {
						final int row = area.y - minY + y;
						final int yy = flipY ? numRows - 1 - row : row;
						for (int x = 0; x < w; x++) {
							final int col = area.x - minX + x;
							final int xx = flipX ? numCols - 1 - col : col;
							target[yy * numCols + xx] = samples[y * w + x];
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		if (gis == null) { createCoverage(scope); }
		return gis.getProjectedEnvelope();
		// OLD : see what it changes to not do it
		// fillBuffer(scope);
		// return gis.getProjectedEnvelope();
	}

	@Override
	protected void fillBuffer(final IScope scope) {
		if (getBuffer() != null) return;
		createCoverage(scope);
		read(scope, true, true);
	}

	/**
	 * Gets the nb rows.
	 *
	 * @param scope
	 *            the scope
	 * @return the nb rows
	 */
	public int getNbRows(final IScope scope) {
		createCoverage(scope);
		return numRows;
	}

	/**
	 * Checks if is tiff.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is tiff
	 */
	public boolean isTiff(final IScope scope) {
		return getExtension(scope).startsWith("tif");
	}

	@Override
	public IShape getGeometry(final IScope scope) {
		createCoverage(scope);
		read(scope, false, false);
		return geom;
	}

	@Override
	protected ICoordinateReferenceSystem getOwnCRS(final IScope scope) {
		final File source = getFile(scope);
		final String sourceAsString = source.getAbsolutePath();
		final int index = sourceAsString.lastIndexOf('.');
		final StringBuilder prjFileName;
		if (index == -1) {
			prjFileName = new StringBuilder(sourceAsString);
		} else {
			prjFileName = new StringBuilder(sourceAsString.substring(0, index));
		}
		prjFileName.append(".prj");

		// does it exist?
		final File prjFile = new File(prjFileName.toString());
		if (prjFile.exists()) {
			// it exists then we have to read it
			try (FileInputStream fip = new FileInputStream(prjFile);
					final FileChannel channel = fip.getChannel();
					PrjFileReader projReader = new PrjFileReader(channel);) {
				return new GamaCRS(projReader.getCoordinateReferenceSystem());
			} catch (final IOException | FactoryException e) {
				// warn about the error but proceed, it is not fatal
				// we have at least the default crs to use
				return null;
			}
		}
		if (isTiff(scope)) {
			// 1. Extract top-level EPSG code directly via GeoKeys first to bypass component queries
			Integer epsgCode = extractEPSGCode(getFile(scope));
			Hints hints = new Hints();
			hints.put(Hints.SKIP_EXTERNAL_OVERVIEWS, Boolean.TRUE);

			if (epsgCode != null) {
				try {
					CoordinateReferenceSystem crs = CRS.decode("EPSG:" + epsgCode);
					hints.put(DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs);
					return new GamaCRS(crs);
				} catch (Throwable ignored) {}
			}

			// 2. Fallback to standard reader with SKIP_EXTERNAL_OVERVIEWS hint
			try {
				final GeoTiffReader store = new GeoTiffReader(getFile(scope), hints);
				CoordinateReferenceSystem crs = store.getCoordinateReferenceSystem();
				store.dispose();
				return new GamaCRS(crs);
			} catch (final Throwable e) {
				GAMA.reportError(scope,
						GamaRuntimeException.warning(
								"Problem when reading the CRS of the " + this.getOriginalPath() + " file", scope),
						false);
			}
		}

		return null;
	}

	/**
	 * Extracts the top-level Projected or Geographic EPSG code from GeoTIFF GeoKeys metadata. Bypasses component-level
	 * database queries when running in database-free environments.
	 */
	/**
	 * Extracts the top-level Projected or Geographic EPSG code from GeoTIFF GeoKeys metadata. Bypasses component-level
	 * database queries when running in database-free environments.
	 */
	private Integer extractEPSGCode(final File file) {
		ImageInputStream in = null;
		ImageReader reader = null;
		try {
			in = ImageIO.createImageInputStream(file);
			if (in == null) return null;

			// Use the explicit GeoTools TIFF ImageReader SPI
			org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataDecoder metadata;
			it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi spi =
					new it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi();

			reader = spi.createReaderInstance();
			reader.setInput(in);

			javax.imageio.metadata.IIOMetadata iioMetadata = reader.getImageMetadata(0);
			metadata = new GeoTiffIIOMetadataDecoder(iioMetadata);

			if (metadata.hasGeoKey()) {
				// ProjectedCSTypeGeoKey = 3072 (1024 is GTModelTypeGeoKey, which only tells the type of model)
				String projStr = metadata.getGeoKey(3072);
				if (projStr != null) {
					try {
						int projectedCode = Integer.parseInt(projStr.trim());
						if (projectedCode != 32767 && projectedCode != 0) return projectedCode;
					} catch (Exception ignored) {}
				}

				// GeographicTypeGeoKey = 2048
				String geoStr = metadata.getGeoKey(2048);
				if (geoStr != null) {
					try {
						int geographicCode = Integer.parseInt(geoStr.trim());
						if (geographicCode != 32767 && geographicCode != 0) return geographicCode;
					} catch (Exception ignored) {}
				}
			}
		} catch (Exception ignored) {} finally {
			if (reader != null) {
				try {
					reader.dispose();
				} catch (Exception ignored) {}
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception ignored) {}
			}
		}
		return null;
	}

	@Override
	public void invalidateContents() {
		super.invalidateContents();
		if (coverage != null) { coverage.dispose(true); }
		coverage = null;
		ascData = null;
		ascInfo = null;
	}

	/**
	 * Value of.
	 *
	 * @param scope
	 *            the scope
	 * @param loc
	 *            the loc
	 * @return the double
	 */
	public Double valueOf(final IScope scope, final IPoint loc) {
		return valueOf(scope, loc.getX(), loc.getY());
	}

	/**
	 * Value of.
	 *
	 * @param scope
	 *            the scope
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the double
	 */
	public Double valueOf(final IScope scope, final double x, final double y) {
		if (getBuffer() == null) { fillBuffer(scope); }
		Object vals = null;
		try {
			vals = coverage.evaluate(new Position2D(x, y));
		} catch (final Exception e) {
			vals = noData.doubleValue();
		}
		final boolean doubleValues = vals instanceof double[];
		final boolean intValues = vals instanceof int[];
		final boolean byteValues = vals instanceof byte[];
		final boolean longValues = vals instanceof long[];
		final boolean floatValues = vals instanceof float[];
		Double val = null;
		if (doubleValues) {
			final double[] vd = (double[]) vals;
			val = vd[0];
		} else if (intValues) {
			final int[] vi = (int[]) vals;
			val = (double) vi[0];
		} else if (longValues) {
			final long[] vi = (long[]) vals;
			val = (double) vi[0];
		} else if (floatValues) {
			final float[] vi = (float[]) vals;
			val = (double) vi[0];
		} else if (byteValues) {
			final byte[] bv = (byte[]) vals;
			if (bv.length == 3) {
				final int red = bv[0] < 0 ? 256 + bv[0] : bv[0];
				final int green = bv[0] < 0 ? 256 + bv[1] : bv[1];
				final int blue = bv[0] < 0 ? 256 + bv[2] : bv[2];
				val = (red + green + blue) / 3.0;
			} else {
				val = (double) ((byte[]) vals)[0];
			}
		}
		return val;
	}

	@Override
	public int length(final IScope scope) {
		createCoverage(scope);
		return numRows * numCols;
	}

	@Override
	protected SimpleFeatureCollection getFeatureCollection(final IScope scope) {
		return null;
	}

	@Override
	public double getNoData(final IScope scope) {
		return noData == null ? IField.NO_NO_DATA : noData.doubleValue();
	}

	@Override
	public int getRows(final IScope scope) {
		createCoverage(scope);
		return numRows;
	}

	@Override
	public int getCols(final IScope scope) {
		createCoverage(scope);
		return numCols;
	}

	@Override
	public int getBandsNumber(final IScope scope) {
		createCoverage(scope);
		return nbBands;
	}

	@Override
	public double[] getBand(final IScope scope, final int index) {
		createCoverage(scope);
		read(scope, true, false);
		return Arrays.copyOf(records.bands.get(index), length(scope));
	}

	@Override
	protected IMatrix _matrixValue(final IScope scope, final IType contentsType, final IPoint preferredSize,
			final boolean copy) throws GamaRuntimeException {
		getContents(scope);
		return getField(scope);
	}

	@Override
	public void save(final IScope scope, final Facets parameters) {
		setWritable(scope, true);
		super.save(scope, parameters == null ? new Facets() : parameters);
	}

}
