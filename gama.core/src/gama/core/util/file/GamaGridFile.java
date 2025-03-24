/*******************************************************************************************************
 *
 * GamaGridFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static gama.core.common.geometry.Envelope3D.of;
import static gama.core.metamodel.topology.projection.ProjectionFactory.getTargetCRSOrDefault;
import static org.geotools.util.factory.Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM;

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

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Envelope;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.IConcept;
import gama.core.common.geometry.Envelope3D;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.matrix.GamaField;
import gama.core.util.matrix.GamaFloatMatrix;
import gama.core.util.matrix.IField;
import gama.core.util.matrix.IMatrix;
import gama.gaml.statements.Facets;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
		doc = @doc ("Represents .asc or .tif files that contain grid descriptions"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaGridFile extends GamaGisFile implements IFieldMatrixProvider {

	/**
	 * The Class Records.
	 */
	static class Records {

		/** The x. */
		double x[];

		/** The y. */
		double y[];

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
	GeneralEnvelope genv;

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
	public GamaGridFile(final IScope scope, final String pathName, final GamaField field) {
		super(scope, pathName, false);
		setWritable(scope, true);
		createCoverage(scope, field);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// No attributes
		return GamaListFactory.EMPTY_LIST;
	}

	/**
	 * Creates the coverage.
	 *
	 * @param scope
	 *            the scope
	 */
	private void createCoverage(final IScope scope) {
		if (coverage == null) {
			final File gridFile = getFile(scope);
			gridFile.setReadable(true);
			InputStream fis = null;
			try {
				fis = Files.newInputStream(gridFile.toPath());
			} catch (IOException e1) {}
			try {
				privateCreateCoverage(scope, fis);
			} catch (final Exception e) {
				String name = getName(scope);
				if (isTiff(scope)) throw GamaRuntimeException
						.error("The format of " + name + " seems incorrect: " + e.getMessage(), scope);
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
		String[] l = line.split(" ");
		if (l.length == 1) { l = line.split("t"); }
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

		String[] l = line.split(" ");
		if (l.length == 1) { l = line.split("t"); }
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
				String line = scanner.nextLine();
				line = line.toLowerCase();
				if (!headingComplete) {
					if (dX == null && line.contains("dx")) {
						dX = doubleVal(line);
						ascInfo[0] = dX;
					} else if (dY == null && line.contains("dy")) {
						dY = doubleVal(line);
						ascInfo[1] = dY;
					} else if (nbCols == null && line.contains("ncols")) {
						nbCols = intVal(line);
					} else if (nbRows == null && line.contains("nrows")) {
						nbRows = intVal(line);
					} else if (noDataD == null && (line.contains("nodata") || line.contains("nodata_value"))) {
						noDataD = doubleVal(line);
					} else if (xCorner == null && xCenter == null && line.contains("xllcorner")) {
						xCorner = doubleVal(line);
						ascInfo[2] = xCorner;
					} else if (yCorner == null && yCenter == null && line.contains("yllcorner")) {
						yCorner = doubleVal(line);
						// TODO: very suspicious, probably xllcenter and yllcenter
					} else if (xCorner == null && xCenter == null && line.contains("xllcorner")) { // AD To verify: the
																									// conditions are
																									// the same as two
																									// lines above...
						xCenter = doubleVal(line);
						// ascInfo[2] = xCorner;
					} else if (yCorner == null && yCenter == null && line.contains("yllcorner")) { // AD To verify: the
																									// conditions are
																									// the same as two
																									// lines above...
						yCenter = doubleVal(line);
					} else if (line.replace(" ", "").length() > 0) {
						if (nbCols == null || nbCols == 0 || nbRows == null || nbRows == 0)
							throw GamaRuntimeException.error("The format of " + getName(scope)
									+ " is not correct. Error: NCOLS and NROWS have to be defined", scope);
						if (xCenter != null && dX != null) {
							xCorner = xCenter - nbCols * dX / 2.0;
							ascInfo[2] = xCorner;
						}
						if (yCenter != null && dY != null) { yCorner = yCenter - nbRows * dY / 2.0; }

						if (yCorner != null && dY != null) { ascInfo[3] = yCorner + nbRows * dY; }

						ascData = new GamaFloatMatrix(nbCols, nbRows);
						if (noData != null) { this.noData = noDataD; }
						double xC = xCorner == null ? 0 : xCorner;
						double yC = yCorner == null ? 0 : yCorner;
						final Envelope3D env = of(xC, yC, xC + nbCols * (dX == null ? 0 : dX), ascInfo[3], 0, 0);
						computeProjection(scope, env);
						numRows = nbRows;
						numCols = nbCols;

						headingComplete = true;
					}
				}
				if (headingComplete) {
					String[] l = line.split(" ");
					for (int i = 0; i < l.length; i++) {
						ascData.set(scope, i, j, Double.valueOf(l[i]));

					}
					j++;
				}
			}
		} catch (final FileNotFoundException e2) {
			throw GamaRuntimeException
					.error("The format of " + getName(scope) + " is not correct. Error: " + e2.getMessage(), scope);
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
	private void createCoverage(final IScope scope, final GamaField field) {
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

		final int cols = field.numCols;
		final int rows = field.numRows;
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

		Envelope2D refEnvelope;

		refEnvelope = new Envelope2D(getTargetCRSOrDefault(scope), x, y, width, height);

		coverage = new GridCoverageFactory().create("data", imagePixelData, refEnvelope);

	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		if (!writable || coverage == null) return;
		try {
			final File f = getFile(scope);
			f.setWritable(true);
			GridCoverageWriter writer;

			if (isTiff(scope)) {
				final GeoTiffFormat format = new GeoTiffFormat();
				writer = format.getWriter(f);
			} else {
				writer = new ArcGridWriter(f);
			}
			writer.write(coverage, (GeneralParameterValue[]) null);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
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
	private void privateCreateCoverage(final IScope scope, final InputStream fis)
			throws DataSourceException, IOException {
		AbstractGridCoverage2DReader store = null;
		try {
			// Necessary to compute it here, because it needs to be passed to the Hints
			final CoordinateReferenceSystem crs = getExistingCRS(scope);
			if (isTiff(scope)) {
				store = crs == null ? new GeoTiffReader(getFile(scope))
						: new GeoTiffReader(getFile(scope), new Hints(DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs));
				noData = ((GeoTiffReader) store).getMetadata().getNoData();
			} else if (crs == null) {
				store = new ArcGridReader(fis);
			} else {
				store = new ArcGridReader(fis, new Hints(DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs));
			}
			genv = store.getOriginalEnvelope();
			final Envelope3D env =
					of(genv.getMinimum(0), genv.getMaximum(0), genv.getMinimum(1), genv.getMaximum(1), 0, 0);
			computeProjection(scope, env);
			numRows = store.getOriginalGridRange().getHigh(1) + 1;
			numCols = store.getOriginalGridRange().getHigh(0) + 1;
			coverage = store.read(null);
		} finally {
			if (store != null) { store.dispose(); }
			scope.getGui().getStatus().endTask(scope, "Opening file " + getName(scope));
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
		if (coverage != null)
			return coverage.evaluate((DirectPosition) new DirectPosition2D(locX, locY), (double[]) null);
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
			scope.getGui().getStatus().beginTask(scope, "Reading file " + getName(scope));

			final Envelope envP = gis == null ? scope.getSimulation().getEnvelope() : gis.getProjectedEnvelope();
			if (gis != null && !(gis.getInitialCRS(scope) instanceof ProjectedCRS)) {
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
			shapes.add(new GamaPoint(originX, originY));
			shapes.add(new GamaPoint(maxX, originY));
			shapes.add(new GamaPoint(maxX, maxY));
			shapes.add(new GamaPoint(originX, maxY));
			shapes.add(shapes.get(0));
			geom = GamaGeometryType.buildPolygon(shapes);
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
				records = new Records();
				records.x = new double[numRows * numCols]; // x
				records.y = new double[numRows * numCols]; // y
				records.bands.add(new double[numRows * numCols]); // data
				for (int i = 0, n = numRows * numCols; i < n; i++) {
					scope.getGui().getStatus().setTaskCompletion(scope, i / (double) n);

					final int yy = i / numCols;
					final int xx = i - yy * numCols;

					records.x[i] = originX + xx * cellWidth + cmx;
					records.y[i] = maxY - (yy * cellHeight + cmy);

					double[] vd = getValue(scope, originXP + xx * cellWidthP + cmxP, maxYP - (yy * cellHeightP + cmyP),
							xx, yy);
					nbBands = vd.length;
					if (i == 0 && vd.length > 1) {
						for (int j = 0; j < vd.length - 1; j++) { records.bands.add(new double[numRows * numCols]); }
					}
					for (int j = 0; j < vd.length; j++) { records.bands.get(j)[i] = vd[j]; }

				}
				if (createGeometries) {
					// Building geometries
					for (int i = 0, n = numRows * numCols; i < n; i++) {

						setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
						final GamaPoint p = new GamaPoint(records.x[i], records.y[i]);
						GamaShape rect = (GamaShape) GamaGeometryType.buildRectangle(cellWidth, cellHeight, p);
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
		} finally {
			scope.getGui().getStatus().endTask(scope, "Reading file " + getName(scope));
		}

	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
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
	protected CoordinateReferenceSystem getOwnCRS(final IScope scope) {
		final File source = getFile(scope);
		// check to see if there is a projection file
		// getting name for the prj file
		final String sourceAsString;
		sourceAsString = source.getAbsolutePath();
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
				return projReader.getCoordinateReferenceSystem();
			} catch (final IOException | FactoryException e) {
				// warn about the error but proceed, it is not fatal
				// we have at least the default crs to use
				return null;
			}
		}
		if (isTiff(scope)) {
			try {
				final GeoTiffReader store = new GeoTiffReader(getFile(scope));
				return store.getCoordinateReferenceSystem();
			} catch (final DataSourceException e) {
				GAMA.reportError(scope,
						GamaRuntimeException.warning(
								"Problem when reading the CRS of the " + this.getOriginalPath() + " file", scope),
						false);
			}
		}

		return null;
	}

	@Override
	public void invalidateContents() {
		super.invalidateContents();
		if (coverage != null) { coverage.dispose(true); }
		coverage = null;
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
	public Double valueOf(final IScope scope, final GamaPoint loc) {
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
			vals = coverage.evaluate(new DirectPosition2D(x, y));
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
	protected IMatrix _matrixValue(final IScope scope, final IType contentsType, final GamaPoint preferredSize,
			final boolean copy) throws GamaRuntimeException {
		getContents(scope);
		return new GamaField(scope, this);
	}

}
