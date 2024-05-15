/*******************************************************************************************************
 *
 * GridLayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Collections;
import java.util.Set;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.IImageProvider;
import gama.core.common.interfaces.ILayer.IGridLayer;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.util.Collector;
import gama.core.util.GamaColor;
import gama.core.util.IList;
import gama.core.util.matrix.GamaField;
import gama.core.util.matrix.IField;
import gama.dev.DEBUG;
import gama.gaml.statements.draw.MeshDrawingAttributes;

/**
 * The Class GridLayer.
 */
public class GridLayer extends AbstractLayer implements IGridLayer {

	static {
		DEBUG.OFF();
	}

	/**
	 * Instantiates a new grid layer.
	 *
	 * @param layer
	 *            the layer
	 */
	public GridLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public GridLayerData createData() {
		return new GridLayerData(definition);
	}

	@Override
	public GridLayerData getData() { return (GridLayerData) super.getData(); }

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		final IAgent a = geometry.getAgent();
		if (a == null || a.getSpecies() != getData().getGrid().getCellSpecies()) return null;
		return super.focusOn(a, s);
	}

	@Override
	public void reloadOn(final IDisplaySurface surface) {
		super.reloadOn(surface);
		getData().reset();
	}

	@Override
	public void privateDraw(final IGraphicsScope scope, final IGraphics dg) {
		GamaColor lineColor = null;
		final GridLayerData data = getData();
		if (data.drawLines()) { lineColor = data.getLineColor(); }
		final double[] gridValueMatrix = data.getElevationMatrix(scope);
		final IImageProvider textureFile = data.textureFile();
		final MeshDrawingAttributes attributes = new MeshDrawingAttributes(getName(), gridValueMatrix == null);
		attributes.setGrayscaled(data.isGrayScaled());
		attributes.setEmpty(data.isWireframe());
		final BufferedImage image = data.getImage();
		if (textureFile != null) {
			attributes.setTextures(Collections.singletonList(textureFile));
		} else if (image != null) {
			final int[] imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			// DEBUG.OUT("ImageData different from DisplayData ? "
			// + !Arrays.equals(imageData, data.getGrid().getDisplayData()));
			System.arraycopy(data.getGrid().getDisplayData(), 0, imageData, 0, imageData.length);
			attributes.setTextures(Collections.singletonList(image));
		}
		attributes.setTriangulated(data.isTriangulated());
		attributes.setWithText(data.isShowText());
		attributes.setBorder(lineColor);
		attributes.setXYDimension(data.getDimensions());
		attributes.setSmooth(data.isSmooth() ? 1 : 0);

		if (gridValueMatrix == null) {
			dg.drawImage(image, attributes);
		} else {
			dg.drawField(new GamaField(scope, (int) data.getDimensions().x, (int) data.getDimensions().y,
					gridValueMatrix, IField.NO_NO_DATA), attributes);
		}
	}

	@Override
	public Set<IAgent> collectAgentsAt(final int x, final int y, final IDisplaySurface g) {
		try (Collector.AsOrderedSet<IAgent> result = Collector.getOrderedSet()) {
			result.add(getData().getGrid().getAgentAt(getModelCoordinatesFrom(x, y, g)));
			return result.items();
		}
	}

	@Override
	public String getType() { return "Grid layer"; }

	@Override
	public IList<IAgent> getAgentsForMenu(final IScope scope) {
		return getData().getGrid().getAgents();
	}

}
