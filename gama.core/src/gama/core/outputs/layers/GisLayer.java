/*******************************************************************************************************
 *
 * GisLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.Color;
import java.util.List;

import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.file.GamaShapeFile;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.draw.DrawingAttributes;
import gama.gaml.statements.draw.ShapeDrawingAttributes;
import gama.gaml.types.IType;

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
		final GamaColor color =
				colorExpression == null ? GamaColor.get(GamaPreferences.Displays.CORE_COLOR.getValue().getRGB())
						: Cast.asColor(scope, colorExpression.value(scope));
		final List<IShape> shapes = buildGisLayer(scope);
		if (shapes != null) {
			for (final IShape geom : shapes) {
				if (geom != null) {
					final DrawingAttributes attributes =
							new ShapeDrawingAttributes(geom, (IAgent) null, color, GamaColor.get(Color.black));
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
