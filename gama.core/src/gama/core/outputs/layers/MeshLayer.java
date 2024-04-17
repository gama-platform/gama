/*******************************************************************************************************
 *
 * MeshLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.util.Collections;

import gama.core.common.geometry.Scaling3D;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IImageProvider;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.util.matrix.IField;
import gama.gaml.statements.draw.MeshDrawingAttributes;

/**
 * The Class MeshLayer.
 */
public class MeshLayer extends AbstractLayer {

	/**
	 * Instantiates a new mesh layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public MeshLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	protected ILayerData createData() {
		return new MeshLayerData(definition);
	}

	@Override
	public MeshLayerData getData() { return (MeshLayerData) super.getData(); }

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics dg) {

		final MeshLayerData data = getData();
		final IField values = data.getElevationMatrix(scope);
		final IImageProvider textureFile = data.textureFile();
		final MeshDrawingAttributes attributes = new MeshDrawingAttributes("", false);
		attributes.setGrayscaled(data.isGrayScaled());
		attributes.setEmpty(data.isWireframe());
		attributes.setBorder(data.drawLines() ? data.getLineColor() : null);
		if (textureFile != null) { attributes.setTextures(Collections.singletonList(textureFile)); }
		attributes.setLocation(data.getPosition());
		attributes.setTriangulated(data.isTriangulated());
		attributes.setWithText(data.isShowText());
		attributes.setXYDimension(data.getDimension());
		attributes.setSize(Scaling3D.of(data.getSize()));
		attributes.setScale(data.getScale());
		attributes.setColors(data.getColor());
		attributes.setSmooth(data.getSmooth());
		attributes.setNoData(data.getNoDataValue());
		attributes.setAbove(data.getAbove());
		dg.drawField(values, attributes);

	}

	@Override
	public String getType() { return "Field layer"; }

}
