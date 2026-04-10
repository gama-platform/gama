/*******************************************************************************************************
 *
 * KmlOp.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.date.IDate;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.utils.files.FileUtils;
import gama.core.util.file.GamaKmlExport;
import gama.annotations.doc;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;

/**
 * Provides GAML operators for building KML (Keyhole Markup Language) exports for Google Earth
 * and compatible GIS tool visualisation. KML is an XML-based geographic data format originally
 * developed by Google that describes points, lines, polygons, 3D models, and styled placemarks.
 *
 * <p>A {@link GamaKmlExport} object must be created first and passed as the first operand of
 * every operator in this class. The operators modify the export object in place and return it,
 * allowing fluent chaining of multiple additions.</p>
 *
 * <p>The following operators are provided:</p>
 * <ul>
 *   <li><strong>{@code add_geometry}</strong> – adds a geographic shape (polygon, line, or
 *       point) with a defined visual style (line width, line colour, fill colour) and an
 *       optional time span. Several overloads are available to omit begin/end dates or line
 *       width.</li>
 *   <li><strong>{@code add_3Dmodel}</strong> – adds a reference to a Collada 3D model
 *       ({@code .dae}) at a given location with a specified scale, orientation, and optional
 *       time span.</li>
 *   <li><strong>{@code add_icon}</strong> – adds an icon/placemark at a given location using
 *       an image file, with a specified scale, orientation, and optional time span.</li>
 * </ul>
 *
 * <p>All operators are annotated {@code @no_test} because they require a
 * {@link GamaKmlExport} context and produce file output that cannot be verified in standard
 * unit tests. The resulting KML files can be opened in Google Earth or any compatible GIS
 * application.</p>
 *
 * @author GAMA Development Team
 * @see GamaKmlExport
 */
public class KmlOp {

	/**
	 * Adds the shape.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param shape
	 *            the shape
	 * @param lineWidth
	 *            the line width
	 * @param lineColor
	 *            the line color
	 * @param fillColor
	 *            the fill color
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Define the kml export manager with new geometry",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If the shape is nil, returns the kml object unchanged." },
			see = { "add_3Dmodel", "add_icon", "add_label" },
			masterDoc = true)
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			final double lineWidth, final IColor lineColor, final IColor fillColor, final IDate begin,
			final IDate end) throws GamaRuntimeException {
		if (kml == null || shape == null) return kml;
		String styleName = shape.stringValue(scope) + ":" + begin.toString();
		kml.defStyle(styleName, lineWidth, lineColor, fillColor);
		kml.addGeometry(scope, shape.toString(), begin, end, shape, styleName,
				shape.getDepth() == null ? 0.0 : shape.getDepth());
		return kml;
	}

	/**
	 * Adds the shape.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param shape
	 *            the shape
	 * @param lineWidth
	 *            the line width
	 * @param lineColor
	 *            the line color
	 * @param fillColor
	 *            the fill color
	 * @param end
	 *            the end
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new geometry: take the following argument: (kml, geometry,linewidth, linecolor,fillcolor, end date)",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If the shape is nil, returns the kml object unchanged.",
				"Uses the current simulation clock time as the begin date." },
			see = { "add_3Dmodel", "add_icon", "add_label" })
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			final double lineWidth, final IColor lineColor, final IColor fillColor, final IDate end)
			throws GamaRuntimeException {
		IDate begin = scope.getClock().getCurrentDate();
		return addShape(scope, kml, shape, lineWidth, lineColor, fillColor, begin, end);
	}

	/**
	 * Adds the shape.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param shape
	 *            the shape
	 * @param lineWidth
	 *            the line width
	 * @param lineColor
	 *            the line color
	 * @param fillColor
	 *            the fill color
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new geometry: take the following argument: (kml, geometry,linewidth, linecolor,fillcolor)",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If the shape is nil, returns the kml object unchanged.",
				"Uses the current simulation clock time as begin date and adds one step as end date." },
			see = { "add_3Dmodel", "add_icon", "add_label" })
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			final double lineWidth, final IColor lineColor, final IColor fillColor) throws GamaRuntimeException {
		IDate begin = scope.getClock().getCurrentDate();
		IDate end = Dates.plusDuration(scope, begin, scope.getClock().getStepInSeconds());
		return addShape(scope, kml, shape, lineWidth, lineColor, fillColor, begin, end);
	}

	/**
	 * Adds the shape.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param shape
	 *            the shape
	 * @param lineColor
	 *            the line color
	 * @param fillColor
	 *            the fill color
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new geometry: take the following argument: (kml, geometry, linecolor,fillcolor)",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If the shape is nil, returns the kml object unchanged.",
				"Uses a default line width of 1.0." },
			see = { "add_3Dmodel", "add_icon", "add_label" })
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			final IColor lineColor, final IColor fillColor) throws GamaRuntimeException {
		return addShape(scope, kml, shape, 1.0, lineColor, fillColor);
	}

	/**
	 * Adds the shape.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param shape
	 *            the shape
	 * @param lineWidth
	 *            the line width
	 * @param color
	 *            the color
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new geometry: take the following argument: (kml, geometry,linewidth, color)",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If the shape is nil, returns the kml object unchanged.",
				"The single color is used for both line and fill." },
			see = { "add_3Dmodel", "add_icon", "add_label" })
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			final double lineWidth, final IColor color) throws GamaRuntimeException {
		return addShape(scope, kml, shape, lineWidth, color, color);
	}

	/**
	 * Adds the 3 D model.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param loc
	 *            the loc
	 * @param scale
	 *            the scale
	 * @param orientation
	 *            the orientation
	 * @param file
	 *            the file
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_3Dmodel",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new 3D model: specify the 3D model (collada) to add to the kml",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If loc is nil, or file is nil or empty, returns the kml object unchanged." },
			see = { "add_geometry", "add_icon", "add_label" },
			masterDoc = true)
	@no_test
	public static GamaKmlExport add3DModel(final IScope scope, final GamaKmlExport kml, final IPoint loc,
			final double scale, final double orientation, final String file, final IDate begin, final IDate end)
			throws GamaRuntimeException {
		if (kml == null || loc == null || file == null || file.isEmpty()) return kml;
		kml.add3DModel(scope, loc, orientation, scale, begin, end, file);
		return kml;
	}

	/**
	 * Adds the 3 D model.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param loc
	 *            the loc
	 * @param scale
	 *            the scale
	 * @param orientation
	 *            the orientation
	 * @param file
	 *            the file
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_3Dmodel",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Kml export with a 3D model",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If loc is nil, or file is nil or empty, returns the kml object unchanged.",
				"Uses the current simulation clock time as begin date and adds one step as end date." },
			see = { "add_geometry", "add_icon", "add_label" })
	@no_test
	public static GamaKmlExport add3DModel(final IScope scope, final GamaKmlExport kml, final IPoint loc,
			final double scale, final double orientation, final String file) throws GamaRuntimeException {
		IDate currentDate = scope.getClock().getCurrentDate();
		IDate endDate = Dates.plusDuration(scope, currentDate, scope.getClock().getStepInSeconds());
		return add3DModel(scope, kml, loc, scale, orientation, file, currentDate, endDate);
	}

	/**
	 * Adds the icon.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param loc
	 *            the loc
	 * @param scale
	 *            the scale
	 * @param orientation
	 *            the orientation
	 * @param file
	 *            the file
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_icon",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Define the kml export manager with new icons",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If loc is nil, or file is nil or empty, returns the kml object unchanged." },
			see = { "add_geometry", "add_icon" },
			masterDoc = true)
	@no_test
	public static GamaKmlExport addIcon(final IScope scope, final GamaKmlExport kml, final IPoint loc,
			final double scale, final double orientation, final String file, final IDate begin, final IDate end)
			throws GamaRuntimeException {
		if (kml == null || loc == null || file == null || file.isEmpty()) return kml;
		String styleName = loc.stringValue(scope) + ":" + begin.toString();
		kml.defIconStyle(styleName, FileUtils.constructAbsoluteFilePath(scope, file, true), scale, orientation);
		kml.addLabel(scope, loc, begin, end, "", "", styleName);
		return kml;
	}

	/**
	 * Adds the icon.
	 *
	 * @param scope
	 *            the scope
	 * @param kml
	 *            the kml
	 * @param loc
	 *            the loc
	 * @param scale
	 *            the scale
	 * @param orientation
	 *            the orientation
	 * @param file
	 *            the file
	 * @return the gama kml export
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "add_icon",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new icons: take the following argument: (kml, location (point),orientation (float), scale (float), file_path (string))",
			returns = "the updated {@code kml} export object (modified in place).",
			special_cases = {
				"If the kml argument is nil, returns nil without modification.",
				"If loc is nil, or file is nil or empty, returns the kml object unchanged.",
				"Uses the current simulation clock time as begin date and adds one step as end date." },
			see = { "add_geometry", "add_icon" })
	@no_test
	public static GamaKmlExport addIcon(final IScope scope, final GamaKmlExport kml, final IPoint loc,
			final double scale, final double orientation, final String file) throws GamaRuntimeException {
		IDate currentDate = scope.getClock().getCurrentDate();
		IDate endDate = Dates.plusDuration(scope, currentDate, scope.getClock().getStepInSeconds());
		return addIcon(scope, kml, loc, scale, orientation, file, currentDate, endDate);
	}
}
