/*******************************************************************************************************
 *
 * GridLineObject.java, in gama.ui.display.opengl, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.scene.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import gama.api.ui.layers.IDrawingAttributes;
import gama.api.utils.geometry.GeometryUtils;

/** A grid border command, realized once on the drawing thread. */
public final class GridLineObject extends GeometryObject {

	private final int cols, rows;
	private final double width, height;
	private Geometry lines;

	public GridLineObject(final int cols, final int rows, final double width, final double height,
			final IDrawingAttributes attributes) {
		super(attributes);
		this.cols = cols;
		this.rows = rows;
		this.width = width;
		this.height = height;
	}

	@Override
	public Geometry getObject() {
		if (lines == null) {
			final var factory = GeometryUtils.getGeometryFactory();
			final LineString[] parts = new LineString[cols + rows + 2];
			for (int i = 0; i <= cols; i++) {
				final double x = i * width / cols;
				parts[i] = factory.createLineString(new Coordinate[] {
						new Coordinate(x, 0, 0), new Coordinate(x, height, 0) });
			}
			for (int i = 0; i <= rows; i++) {
				final double y = i * height / rows;
				parts[cols + 1 + i] = factory.createLineString(new Coordinate[] {
						new Coordinate(0, y, 0), new Coordinate(width, y, 0) });
			}
			lines = factory.createMultiLineString(parts);
		}
		return lines;
	}
}
