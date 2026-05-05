/*******************************************************************************************************
 *
 * GeoTiffSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.save;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.geotools.api.coverage.grid.GridCoverageWriter;
import org.geotools.api.parameter.GeneralParameterValue;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.Envelope2DArchived;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.kernel.topology.IProjection;
import gama.api.runtime.scope.IScope;
import gama.api.types.matrix.IField;
import gama.api.utils.files.SaveOptions;
import gama.core.topology.gis.ProjectionFactory;
import gama.core.topology.grid.GridPopulation;

/**
 * The Class GeoTiffSaver.
 */
public class GeoTiffSaver extends AbstractSaver {

	/** The Constant GEOTIFF. */
	private static final String GEOTIFF = "geotiff";

	/** The GeoTools coverage property used to persist a no-data value. */
	private static final String GC_NODATA = "GC_NODATA";

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param item
	 *            the item
	 * @param file
	 *            the file
	 * @param saveOptions
	 *            the options controlling the GeoTIFF export, including the optional no-data value
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions saveOptions)
			throws IOException {
		if (file == null) return;
		File f = file;
		// in case it already exists we delete it, if deletion fail we cancel the saving
		if (f.exists() && !f.delete()) return;

		try {
			Object v = item.value(scope);
			final Double noData = resolveNoData(scope, v, saveOptions);
			if (v instanceof IField gf) {
				saveField(scope, gf, f, noData);
			} else {
				final ISpecies species = Cast.asSpecies(scope, v);
				if (species == null || !species.isGrid()) return;
				saveGrid(scope, species, f, noData);
			}
		} finally {
			ProjectionFactory.saveTargetCRSAsPRJFile(scope, f.getAbsolutePath());
		}
	}

	/**
	 * Resolves the no-data value that should be written in the GeoTIFF metadata.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param value
	 *            the evaluated value to save
	 * @param saveOptions
	 *            the save options provided by the statement
	 * @return the explicit or inherited no-data value, or {@code null} when none should be written
	 */
	private Double resolveNoData(final IScope scope, final Object value, final SaveOptions saveOptions) {
		if (saveOptions.noData() != null) return saveOptions.noData();
		if (value instanceof IField field) {
			final double fieldNoData = field.getNoData(scope);
			if (fieldNoData != IField.NO_NO_DATA) return fieldNoData;
		}
		return null;
	}

	/**
	 * Save grid.
	 *
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @param file
	 *            the file
	 * @throws IllegalArgumentException
	 *             the illegal argument exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void saveGrid(final IScope scope, final ISpecies species, final File file, final Double noData)
			throws IllegalArgumentException, IOException {
		final GridPopulation gp = (GridPopulation) species.getPopulation(scope);
		final int cols = gp.getNbCols();
		final int rows = gp.getNbRows();
		IProjection worldProjection = scope.getSimulation().getProjectionFactory().getWorld();
		ICoordinateReferenceSystem crs = ProjectionFactory.getTargetCRSOrDefault(scope);
		double x = worldProjection == null ? 0 : worldProjection.getProjectedEnvelope().getMinX();
		double y = worldProjection == null ? 0 : worldProjection.getProjectedEnvelope().getMinY();

		final float[][] imagePixelData = new float[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) { imagePixelData[row][col] = gp.getGridValue(col, row).floatValue(); }

		}
		final double width = scope.getSimulation().getEnvelope().getWidth();
		final double height = scope.getSimulation().getEnvelope().getHeight();

		Envelope2DArchived refEnvelope = new Envelope2DArchived(crs == null ? null : crs.getCRS(), x, y, width, height);

		// In order to fix issue #2793, it seems that (before the GAMA 1.8 release), GAMA is only able,
		// to read GeoTiff files with Byte format data.
		// The use of the following create from org.geotools.coverage.grid.GridCoverageFactory, will produce a
		// dataset of floats.
		// This is perfectly possible for the GeoTiff, but as GAMA can only read Byte format GeoTiff files, we limit
		// the save to this
		// specific format of data.
		final GridCoverage2D coverage = createCoverage(imagePixelData, refEnvelope, noData);
		// final GridCoverage2D coverage = createCoverageByteFromFloat("data", imagePixelData, refEnvelope);

		final GeoTiffFormat format = new GeoTiffFormat();
		final GridCoverageWriter writer = format.getWriter(file);
		writer.write(coverage, (GeneralParameterValue[]) null);

	}

	/**
	 * Save field.
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param f
	 *            the f
	 * @throws IllegalArgumentException
	 *             the illegal argument exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void saveField(final IScope scope, final IField field, final File f, final Double noData)
			throws IllegalArgumentException, IOException {
		if (field.isEmpty(scope)) return;
		final int cols = field.getCols(scope);
		final int rows = field.getRows(scope);
		IProjection worldProjection = scope.getSimulation().getProjectionFactory().getWorld();
		double x = worldProjection == null ? 0 : worldProjection.getProjectedEnvelope().getMinX();
		double y = worldProjection == null ? 0 : worldProjection.getProjectedEnvelope().getMinY();
		ICoordinateReferenceSystem crs = ProjectionFactory.getTargetCRSOrDefault(scope);
		final float[][] imagePixelData = new float[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) { imagePixelData[row][col] = field.get(scope, col, row).floatValue(); }
		}
		final double width = scope.getSimulation().getEnvelope().getWidth();
		final double height = scope.getSimulation().getEnvelope().getHeight();
		Envelope2DArchived refEnvelope = new Envelope2DArchived(crs == null ? null : crs.getCRS(), x, y, width, height);

		// In order to fix issue #2793, it seems that (before the GAMA 1.8 release), GAMA is only able,
		// to read GeoTiff files with Byte format data.
		// The use of the following create from org.geotools.coverage.grid.GridCoverageFactory, will produce a
		// dataset of floats.
		// This is perfectly possible for the GeoTiff, but as GAMA can only read Byte format GeoTiff files, we limit
		// the save to this
		// specific format of data.
		final GridCoverage2D coverage = createCoverage(imagePixelData, refEnvelope, noData);
		final GeoTiffFormat format = new GeoTiffFormat();
		final GridCoverageWriter writer = format.getWriter(f);
		writer.write(coverage, (GeneralParameterValue[]) null);

	}

	/**
	 * Creates the grid coverage to write, optionally annotating it with a no-data value.
	 *
	 * @param imagePixelData
	 *            the raster values to save
	 * @param refEnvelope
	 *            the spatial envelope of the coverage
	 * @param noData
	 *            the optional no-data value to persist
	 * @return the GeoTIFF coverage ready to be written
	 */
	private GridCoverage2D createCoverage(final float[][] imagePixelData, final Envelope2DArchived refEnvelope,
			final Double noData) {
		final GridCoverageFactory factory = new GridCoverageFactory();
		final GridCoverage2D coverage = factory.create("data", imagePixelData, refEnvelope);
		if (noData == null) return coverage;
		return factory.create("data", coverage.getRenderedImage(), refEnvelope, coverage.getSampleDimensions(), null,
				Collections.singletonMap(GC_NODATA, noData));
	}

	@Override
	public Set<String> computeFileTypes() {
		return Set.of(GEOTIFF);
	}
}
