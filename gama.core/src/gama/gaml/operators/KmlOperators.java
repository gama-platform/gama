/*******************************************************************************************************
 *
 * KmlOperators.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import gama.api.data.objects.IColor;
import gama.api.data.objects.IDate;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.FileUtils;
import gama.core.util.file.GamaKmlExport;
import gama.annotations.doc;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;

/**
 * The Class KmlOperators.
 */
public class KmlOperators {

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
			see = { "add_3Dmodel", "add_icon", "add_label" })
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			final double lineWidth, final IColor lineColor, final IColor fillColor) throws GamaRuntimeException {
		IDate begin = scope.getClock().getCurrentDate();
		IDate end = DateOperators.plusDuration(scope, begin, scope.getClock().getStepInSeconds());
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
			see = { "add_geometry", "add_icon", "add_label" })
	@no_test
	public static GamaKmlExport add3DModel(final IScope scope, final GamaKmlExport kml, final IPoint loc,
			final double scale, final double orientation, final String file) throws GamaRuntimeException {
		IDate currentDate = scope.getClock().getCurrentDate();
		IDate endDate = DateOperators.plusDuration(scope, currentDate, scope.getClock().getStepInSeconds());
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
			see = { "add_geometry", "add_icon" })
	@no_test
	public static GamaKmlExport addIcon(final IScope scope, final GamaKmlExport kml, final IPoint loc,
			final double scale, final double orientation, final String file) throws GamaRuntimeException {
		IDate currentDate = scope.getClock().getCurrentDate();
		IDate endDate = DateOperators.plusDuration(scope, currentDate, scope.getClock().getStepInSeconds());
		return addIcon(scope, kml, loc, scale, orientation, file, currentDate, endDate);
	}
}
