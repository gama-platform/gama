/*******************************************************************************************************
 *
 * GeometryDrawing.java, in gama.api, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.util.GeometryEditor;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.GeometryUtils;

/**
 * An evaluated geometry command. Construction snapshots model coordinates; realization belongs to the consumer.
 * No expression, agent location or execution scope is retained. The owned geometry must never be mutated by callers.
 */
public final class GeometryDrawing {

	private final Geometry source;
	private final double beginArrow, endArrow;
	private final boolean filled;
	private Geometry realized;

	public GeometryDrawing(final Geometry source, final double beginArrow, final double endArrow, final boolean filled) {
		final GeometryEditor editor = new GeometryEditor(source.getFactory());
		this.source = editor.edit(source, new GeometryEditor.CoordinateSequenceOperation() {
			@Override
			public CoordinateSequence edit(final CoordinateSequence coordinates, final Geometry geometry) {
				final IPoint[] points = new IPoint[coordinates.size()];
				for (int i = 0; i < points.length; i++) {
					points[i] = GamaPointFactory.create(coordinates.getX(i), coordinates.getY(i), coordinates.getZ(i));
				}
				// The array is owned already: copying it again would also normalize winding during collection.
				return GamaCoordinateSequenceFactory.create(points, false);
			}
		});
		this.beginArrow = beginArrow;
		this.endArrow = endArrow;
		this.filled = filled;
	}

	public Geometry source() { return source; }

	public Geometry normalizedSource() {
		normalize(source);
		return source;
	}

	public boolean hasArrows() { return beginArrow > 0 || endArrow > 0; }

	/** Called by the rendering consumer, not while collecting commands. */
	public Geometry realize() {
		if (realized == null) {
			normalize(source);
			realized = addArrows(source);
		}
		return realized;
	}

	/** Java2D adds arrowheads after object transforms; OpenGL adds them before its playback transforms. */
	public Geometry addArrows(final Geometry geometry) {
		if (!hasArrows()) return geometry;
		final IPoint[] points = GeometryUtils.getPointsOf(geometry);
		if (points.length < 2) return geometry;
		final List<Geometry> parts = new ArrayList<>(3);
		parts.add(geometry);
		if (endArrow > 0) {
			parts.add(GamaShapeFactory.buildArrow(points[points.length - 2], points[points.length - 1],
					endArrow, endArrow + endArrow / 3, filled).getInnerGeometry());
		}
		if (beginArrow > 0) {
			parts.add(GamaShapeFactory.buildArrow(points[1], points[0], beginArrow,
					beginArrow + beginArrow / 3, filled).getInnerGeometry());
		}
		return GeometryUtils.getGeometryFactory().buildGeometry(parts);
	}

	private static void normalize(final Geometry geometry) {
		// Historically multipart drawings keep their constituent winding.
		if (!(geometry instanceof GeometryCollection)) {
			GamaCoordinateSequenceFactory.pointsOf(geometry).ensureClockwiseness();
		}
	}
}
