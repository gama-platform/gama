/*******************************************************************************************************
 *
 * GisLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.util.List;

import gama.api.constants.IKeyword;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.IGraphics;
import gama.api.ui.displays.IGraphicsScope;
import gama.api.ui.layers.ILayerStatement;
import gama.api.utils.color.GamaColorFactory;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.util.file.GamaShapeFile;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.ShapeDrawingAttributes;

/**
 * The Class GisLayer.
 */
public class GisLayer extends AbstractLayer {

	/** The color expression. */
	IExpression gisExpression, colorExpression;

	/**
	 * Instantiates a new gis layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public GisLayer(final ILayerStatement layer) {
		super(layer);
		gisExpression = layer.getFacet(IKeyword.GIS);
		colorExpression = layer.getFacet(IKeyword.COLOR);
	}

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics g) {
		final IColor color =
				colorExpression == null ? GamaColorFactory.get(GamaPreferences.Displays.CORE_COLOR.getValue().getRGB())
						: GamaColorFactory.createFrom(scope, colorExpression.value(scope));
		final List<IShape> shapes = buildGisLayer(scope);
		if (shapes != null) {
			for (final IShape geom : shapes) {
				if (geom != null) {
					final DrawingAttributes attributes =
							new ShapeDrawingAttributes(geom, (IAgent) null, color, GamaColorFactory.BLACK);
					g.drawShape(geom.getInnerGeometry(), attributes);
				}
			}
		}
	}

	/**
	 * Builds the gis layer.
	 *
	 * @param scope
	 *            the scope
	 * @return the list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public List<IShape> buildGisLayer(final IScope scope) throws GamaRuntimeException {
		final GamaShapeFile file = getShapeFile(scope);
		if (file == null) return null;
		return file.getContents(scope);
	}

	/**
	 * Gets the shape file.
	 *
	 * @param scope
	 *            the scope
	 * @return the shape file
	 */
	private GamaShapeFile getShapeFile(final IScope scope) {
		if (gisExpression == null) return null;
		if (gisExpression.getGamlType().id() == IType.STRING) {
			final String fileName = Cast.asString(scope, gisExpression.value(scope));
			return new GamaShapeFile(scope, fileName);
		}
		final Object o = gisExpression.value(scope);
		if (o instanceof GamaShapeFile) return (GamaShapeFile) o;
		return null;
	}

	@Override
	public String getType() { return "Gis layer"; }

}
