/*******************************************************************************************************
 *
 * DrivingGraph.java, in gaml.extensions.traffic, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.traffic.driving;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gama.core.common.util.StringUtils;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.graph.GamaSpatialGraph;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.core.util.IMap;
import gama.core.util.graph.GraphEvent;
import gama.core.util.graph._Edge;
import gama.core.util.graph.GraphEvent.GraphEventType;

/**
 * The Class DrivingGraph.
 */
public class DrivingGraph extends GamaSpatialGraph {

	/**
	 * Instantiates a new driving graph.
	 *
	 * @param edges
	 *            the edges
	 * @param vertices
	 *            the vertices
	 * @param scope
	 *            the scope
	 */
	public DrivingGraph(final IContainer edges, final IContainer vertices, final IScope scope) {
		super(scope, vertices.getGamlType().getContentType(), edges.getGamlType().getContentType());
		init(scope, edges, vertices);
	}

	@Override
	public boolean addEdgeWithNodes(final IScope scope, final IShape e, final IMap<GamaPoint, IShape> nodes) {
		if (containsEdge(e)) return false;
		final Coordinate[] coord = e.getInnerGeometry().getCoordinates();
		final IShape ptS = new GamaPoint(coord[0]);
		final IShape ptT = new GamaPoint(coord[coord.length - 1]);
		final IShape v1 = nodes.get(ptS);
		if (v1 == null) return false;
		final IShape v2 = nodes.get(ptT);
		if (v2 == null) return false;

		if (e instanceof IAgent && ((IAgent) e).getSpecies().implementsSkill("road_skill")) {
			final IAgent roadAgent = e.getAgent();
			final IAgent source = v1.getAgent();
			final IAgent target = v2.getAgent();
			final List<IAgent> v1ro = RoadNodeSkill.getRoadsOut(source);
			if (!v1ro.contains(roadAgent)) { v1ro.add(roadAgent); }
			final List<IAgent> v2ri = RoadNodeSkill.getRoadsIn(target);
			if (!v2ri.contains(roadAgent)) { v2ri.add(roadAgent); }
			RoadSkill.setSourceNode(roadAgent, source);
			RoadSkill.setTargetNode(roadAgent, target);
			RoadSkill.getVehicleOrdering(roadAgent).clear();
		}

		addVertex(v1);
		addVertex(v2);
		_Edge<IShape, IShape> edge;
		try {
			edge = newEdge(e, v1, v2);
		} catch (final GamaRuntimeException e1) {
			e1.addContext("Impossible to create edge from " + StringUtils.toGaml(e, false) + " in graph " + this);
			throw e1;
		}
		// if ( edge == null ) { return false; }
		edgeMap.put(e, edge);
		dispatchEvent(scope, new GraphEvent(scope, this, this, e, null, GraphEventType.EDGE_ADDED));
		return true;
	}
}
