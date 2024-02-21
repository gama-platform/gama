/*******************************************************************************************************
 *
 * KmlOp.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.core.common.util.FileUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaDate;
import gama.gaml.types.GamaKmlExport;

/**
 * The Class KmlOp.
 */
public class KmlOp {

	
	/**
	 * Adds the shape.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param shape the shape
	 * @param lineWidth the line width
	 * @param lineColor the line color
	 * @param fillColor the fill color
	 * @param begin the begin
	 * @param end the end
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Define the kml export manager with new geometry",
			see = { "add_3Dmodel", "add_icon", "add_label"},
			masterDoc = true)
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			double lineWidth, GamaColor lineColor, GamaColor fillColor, GamaDate begin, GamaDate end) throws GamaRuntimeException {
		if (kml == null || shape == null) return kml;
		String styleName = shape.stringValue(scope) + ":" + begin.toString(); 
		kml.defStyle(styleName, lineWidth, lineColor,fillColor);
		kml.addGeometry(scope, shape.toString(), begin, end, shape, styleName, shape.getDepth() == null ? 0.0 : shape.getDepth());
		return kml;
	}
	
	/**
	 * Adds the shape.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param shape the shape
	 * @param lineWidth the line width
	 * @param lineColor the line color
	 * @param fillColor the fill color
	 * @param end the end
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new geometry: take the following argument: (kml, geometry,linewidth, linecolor,fillcolor, end date)",
			see = { "add_3Dmodel", "add_icon", "add_label"})
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			double lineWidth, GamaColor lineColor, GamaColor fillColor, GamaDate end) throws GamaRuntimeException {
		GamaDate begin = scope.getClock().getCurrentDate();
		return addShape(scope,kml,shape,lineWidth,lineColor,fillColor,begin,end) ;
	}
	
	
	/**
	 * Adds the shape.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param shape the shape
	 * @param lineWidth the line width
	 * @param lineColor the line color
	 * @param fillColor the fill color
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new geometry: take the following argument: (kml, geometry,linewidth, linecolor,fillcolor)",
			see = { "add_3Dmodel", "add_icon", "add_label"})
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			double lineWidth, GamaColor lineColor, GamaColor fillColor) throws GamaRuntimeException {
		GamaDate begin = scope.getClock().getCurrentDate();
		GamaDate end = Dates.plusDuration(scope, begin, scope.getClock().getStepInSeconds());
		return addShape(scope,kml,shape,lineWidth,lineColor,fillColor,begin,end) ;
	}
	
	/**
	 * Adds the shape.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param shape the shape
	 * @param lineColor the line color
	 * @param fillColor the fill color
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new geometry: take the following argument: (kml, geometry, linecolor,fillcolor)",
			see = { "add_3Dmodel", "add_icon", "add_label"})
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,
			GamaColor lineColor, GamaColor fillColor) throws GamaRuntimeException {
		return addShape(scope,kml,shape,1.0,lineColor,fillColor) ;
	}
	

	/**
	 * Adds the shape.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param shape the shape
	 * @param lineWidth the line width
	 * @param color the color
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_geometry",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new geometry: take the following argument: (kml, geometry,linewidth, color)",
			see = { "add_3Dmodel", "add_icon", "add_label"})
	@no_test
	public static GamaKmlExport addShape(final IScope scope, final GamaKmlExport kml, final IShape shape,double lineWidth,
			GamaColor color) throws GamaRuntimeException {
		return addShape(scope,kml,shape,lineWidth,color,color) ;
	}
	
	/**
	 * Adds the 3 D model.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param loc the loc
	 * @param scale the scale
	 * @param orientation the orientation
	 * @param file the file
	 * @param begin the begin
	 * @param end the end
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_3Dmodel",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new 3D model: specify the 3D model (collada) to add to the kml",
			see = { "add_geometry", "add_icon", "add_label"},
			masterDoc = true)
	@no_test
	public static GamaKmlExport add3DModel(final IScope scope, final GamaKmlExport kml, final GamaPoint loc,
			double scale , double orientation, String file, GamaDate begin, GamaDate end) throws GamaRuntimeException {
		if (kml == null || loc == null || file == null || file.isEmpty()) return kml;
		 kml.add3DModel(scope, loc, orientation, scale, begin, end, file);
		return kml;
	}
	
	/**
	 * Adds the 3 D model.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param loc the loc
	 * @param scale the scale
	 * @param orientation the orientation
	 * @param file the file
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_3Dmodel",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Kml export with a 3D model",
			see = { "add_geometry", "add_icon", "add_label"})
	@no_test
	public static GamaKmlExport add3DModel(final IScope scope, final GamaKmlExport kml, final GamaPoint loc,
			double scale , double orientation, String file) throws GamaRuntimeException {
		GamaDate currentDate = scope.getClock().getCurrentDate();
		GamaDate endDate = Dates.plusDuration(scope, currentDate, scope.getClock().getStepInSeconds());
		return add3DModel( scope, kml,loc,scale , orientation,file, currentDate,endDate);
	}
	
	/**
	 * Adds the icon.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param loc the loc
	 * @param scale the scale
	 * @param orientation the orientation
	 * @param file the file
	 * @param begin the begin
	 * @param end the end
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_icon",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "Define the kml export manager with new icons",
			see = { "add_geometry", "add_icon"},
			masterDoc = true)
	@no_test
	public static GamaKmlExport addIcon(final IScope scope, final GamaKmlExport kml, final GamaPoint loc,
			double scale, double orientation , String file, GamaDate begin, GamaDate end) throws GamaRuntimeException {
		if (kml == null || loc == null || file == null || file.isEmpty()) return kml;
		String styleName = loc.stringValue(scope) + ":" + begin.toString(); 
		kml.defIconStyle(styleName,FileUtils.constructAbsoluteFilePath(scope, file, true),scale, orientation);  
		kml.addLabel(scope, loc, begin, end, "", "", styleName);
		return kml;
	}
	
	/**
	 * Adds the icon.
	 *
	 * @param scope the scope
	 * @param kml the kml
	 * @param loc the loc
	 * @param scale the scale
	 * @param orientation the orientation
	 * @param file the file
	 * @return the gama kml export
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	@operator (
			value = "add_icon",
			category = IOperatorCategory.LOGIC,
			concept = { IConcept.LOGICAL })
	@doc (
			value = "the kml export manager with new icons: take the following argument: (kml, location (point),orientation (float), scale (float), file_path (string))",
			see = { "add_geometry", "add_icon"})
	@no_test
	public static GamaKmlExport addIcon(final IScope scope, final GamaKmlExport kml, final GamaPoint loc,
			double scale, double orientation , String file) throws GamaRuntimeException {
		GamaDate currentDate = scope.getClock().getCurrentDate();
		GamaDate endDate = Dates.plusDuration(scope, currentDate, scope.getClock().getStepInSeconds());
		return addIcon(scope,kml, loc,scale,orientation ,file, currentDate,endDate);
	}
}
