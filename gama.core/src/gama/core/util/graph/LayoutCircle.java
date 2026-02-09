/*******************************************************************************************************
 *
 * LayoutCircle.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.graph;

import java.util.Collections;
import java.util.List;

import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IGraph;
import gama.api.data.objects.IShape;
import gama.api.runtime.scope.IScope;
import gama.gaml.operators.MathOperators;
import gama.gaml.operators.spatial.SpatialPunctal;

/**
 * The Class LayoutCircle.
 */
public class LayoutCircle {

	/** The graph. */
	private final IGraph<IShape, IShape> graph;

	/** The envelope geometry. */
	private final IShape envelopeGeometry;

	/**
	 * Instantiates a new layout circle.
	 *
	 * @param graph
	 *            the graph
	 * @param envelopeGeometry
	 *            the envelope geometry
	 */
	public LayoutCircle(final IGraph<IShape, IShape> graph, final IShape envelopeGeometry) {
		this.graph = graph;
		this.envelopeGeometry = envelopeGeometry;
	}

	/**
	 * Apply layout.
	 *
	 * @param scope
	 *            the scope
	 * @param shuffle
	 *            the shuffle
	 */
	public void applyLayout(final IScope scope, final boolean shuffle) {

		final double radius = envelopeGeometry.getCentroid().euclidianDistanceTo(SpatialPunctal
				._closest_point_to(envelopeGeometry.getCentroid(), envelopeGeometry.getExteriorRing(scope)));

		// Optimize node ordering
		final List<IShape> orderedNodes = this.minimizeEdgeLength(graph, shuffle);

		int i = 0;
		for (final IShape v : orderedNodes) {
			final double angle = 360 * i++ / (double) graph.vertexSet().size();
			final double x = MathOperators.cos(angle) * radius + envelopeGeometry.getCentroid().getX();
			final double y = MathOperators.sin(angle) * radius + envelopeGeometry.getCentroid().getY();
			v.setLocation(GamaPointFactory.create(x, y));
		}

	}

	/**
	 * Minimize edge length.
	 *
	 * @param graph
	 *            the graph
	 * @param shuffle
	 *            the shuffle
	 * @return the list
	 */
	private List<IShape> minimizeEdgeLength(final IGraph<IShape, IShape> graph, final boolean shuffle) {
		/*
		 * List<IShape> orderedNode = graph.vertexSet().stream().sorted((v1,v2) -> graph.degreeOf(v1) <
		 * graph.degreeOf(v2) ? 1 : (v1.getAgent().getIndex() < v2.getAgent().getIndex() ? -1 : 1)) .toList();
		 */

		// Not find a simple to implement algorithm

		final List<IShape> nodes = graph.getVertices();
		if (shuffle) { Collections.shuffle(nodes); }
		return nodes;
	}

}
