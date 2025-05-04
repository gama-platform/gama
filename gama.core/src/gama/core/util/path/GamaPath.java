/*******************************************************************************************************
 *
 * GamaPath.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.path;

import org.jgrapht.GraphPath;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.graph.GamaSpatialGraph;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.core.util.graph.IGraph;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

// If build from a list of points, creates the corresponding geometry
// If build from a spatial graph, creates a geometry from the edges
/**
 * The Class GamaPath.
 *
 * @param <V>
 *            the value type
 * @param <E>
 *            the element type
 * @param <G>
 *            the generic type
 */
// Si
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPath<V, E, G extends IGraph<V, E>> implements Comparable, GraphPath<V, E>, IPath<V, E, G> {

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "source", source, "target", target, "edges", json.array(edges), "weight",
				weight).add("graph", graph).add("graphVersion", graphVersion);
	}

	/** The target. */
	V source, target;

	/** The edges. */
	IList<E> edges;

	/** The weight. */
	double weight = 0.0;

	/** The graph. */
	// The graph attribute is override in GamaSpatialPath by a GamaSpatialGraph
	G graph;

	/** The graph version. */
	int graphVersion;

	/**
	 * Instantiates a new gama path.
	 */
	// FIXME virer le constructeur par d�faut... used for the inheritance...
	public GamaPath() {}

	@Override
	public IType getGamlType() { return Types.PATH; }

	/**
	 * Instantiates a new gama path.
	 *
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param _edges
	 *            the edges
	 */
	public GamaPath(final G g, final V start, final V target, final IList<? extends E> _edges) {
		init(g, start, target, _edges, true);
		this.graph = g;
	}

	/**
	 * Instantiates a new gama path.
	 *
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param _edges
	 *            the edges
	 * @param modify_edges
	 *            the modify edges
	 */
	public GamaPath(final G g, final V start, final V target, final IList<? extends E> _edges,
			final boolean modify_edges) {
		init(g, start, target, _edges, modify_edges);
		this.graph = g;
	}

	/**
	 * Instantiates a new gama path.
	 *
	 * @param nodes
	 *            the nodes
	 */
	public GamaPath(final IList<? extends V> nodes) {
		final IList<E> _edges = GamaListFactory.create();
		for (int i = 0; i < nodes.size() - 1; i++) {
			final E edge = createEdge(nodes.get(i), nodes.get(i + 1));
			if (edge != null) { _edges.add(edge); }
		}
		init(null, nodes.get(0), nodes.get(nodes.size() - 1), _edges, false);
		this.graph = null;
	}

	/**
	 * Creates the edge.
	 *
	 * @param v
	 *            the v
	 * @param v2
	 *            the v 2
	 * @return the e
	 */
	protected E createEdge(final V v, final V v2) {
		// TODO to define !
		return null;
	}

	/**
	 * Inits the.
	 *
	 * @param g
	 *            the g
	 * @param start
	 *            the start
	 * @param target
	 *            the target
	 * @param _edges
	 *            the edges
	 * @param modify_edges
	 *            the modify edges
	 */
	public void init(final G g, final V start, final V target, final IList<? extends E> _edges,
			final boolean modify_edges) {
		this.source = start;
		this.target = target;
		this.edges = GamaListFactory.create();
		graphVersion = 0;

		if (_edges != null && _edges.size() > 0) { edges.addAll(_edges); }
	}

	/**
	 * Instantiates a new gama path.
	 *
	 * @param g
	 *            the g
	 * @param nodes
	 *            the nodes
	 */
	public GamaPath(final G g, final IList<? extends V> nodes) {
		if (!(g instanceof GamaSpatialGraph) && nodes.isEmpty())
			throw new ClassCastException("We cannot create an empty path in a non-spatial graph");
		if (nodes.isEmpty()) {
			source = null;
			target = null;
		} else {
			source = nodes.get(0);
			target = nodes.get(nodes.size() - 1);
		}
		edges = GamaListFactory.create();

		for (int i = 0, n = nodes.size(); i < n - 1; i++) { edges.add(g.getEdge(nodes.get(i), nodes.get(i + 1))); }
		graph = g;
	}

	// /////////////////////////////////////////////////
	// Implements methods from GraphPath

	@Override
	public G getGraph() { return graph; }

	@Override
	public V getStartVertex() { return source; }

	@Override
	public V getEndVertex() { return target; }

	@Override
	public IList<E> getEdgeList() { return edges; }

	/**
	 * Sets the weight.
	 *
	 * @param weight
	 *            the new weight
	 */
	public void setWeight(final double weight) { this.weight = weight; }

	@Override
	public double getWeight() {
		final G graph = getGraph();
		if (graph == null) return weight;
		return graph.computeWeight(this);
	}

	// /////////////////////////////////////////////////
	// Implements methods from IValue

	@Override
	public String stringValue(final IScope scope) {
		return serializeToGaml(false);
	}

	@Override
	public GamaPath copy(final IScope scope) {
		return new GamaPath(graph, source, target, edges);
	}

	@Override
	public IList<V> getVertexList() {
		if (graph == null) return GamaListFactory.EMPTY_LIST;
		return GamaListFactory.<V> wrap(getGamlType().getKeyType(), GraphPath.super.getVertexList());
	}

	@Override
	public double getWeight(final IShape line) throws GamaRuntimeException {
		return line.getGeometry().getPerimeter(); // workaround for the moment
	}

	@Override
	public String toString() {
		return "path between " + getStartVertex().toString() + " and " + getEndVertex().toString();
	}

	@Override
	// FIXME
	public void acceptVisitor(final IAgent agent) {
		agent.setAttribute("current_path", this); // ???
	}

	@Override
	// FIXME
	public void forgetVisitor(final IAgent agent) {
		agent.setAttribute("current_path", null); // ???
	}

	@Override
	// FIXME
	public int indexOf(final IAgent a) {
		return Cast.asInt(null, a.getAttribute("index_on_path")); // ???
	}

	@Override
	// FIXME
	public int indexSegmentOf(final IAgent a) {
		return Cast.asInt(null, a.getAttribute("index_on_path_segment")); // ???
	}

	@Override
	// FIXME
	public boolean isVisitor(final IAgent a) {
		return a.getAttribute("current_path") == this;
	}

	@Override
	// FIXME
	public void setIndexOf(final IAgent a, final int index) {
		a.setAttribute("index_on_path", index);
	}

	@Override
	// FIXME
	public void setIndexSegementOf(final IAgent a, final int indexSegement) {
		a.setAttribute("index_on_path_segment", indexSegement);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "(" + getEdgeList().serializeToGaml(includingBuiltIn) + ") as path";
	}

	@Override
	public int getLength() { return edges.size(); }

	@Override
	public double getDistance(final IScope scope) {
		if (getEdgeList() == null || getEdgeList().isEmpty()) return 0;
		return getWeight();
	}

	@Override
	public ITopology getTopology(final IScope scope) {
		return graph instanceof GamaSpatialGraph ? ((GamaSpatialGraph) graph).getTopology(scope) : null;
	}

	@Override
	public void setRealObjects(final IMap<IShape, IShape> realObjects) {

	}

	@Override
	public IShape getRealObject(final Object obj) {
		return null;
	}

	@Override
	public void setSource(final V source) { this.source = source; }

	@Override
	public void setTarget(final V target) { this.target = target; }

	@Override
	public int getGraphVersion() { return graphVersion; }

	@Override
	public IList<IShape> getEdgeGeometry() { return null; }

	@Override
	public IShape getGeometry() { return null; }

	@Override
	public void setGraph(final G graph) {
		this.graph = graph;
		graphVersion = graph.getPathComputer().getVersion();

	}

	@Override
	public int compareTo(final Object o) {
		return (int) (this.getWeight() - ((GamaPath) o).getWeight());
	}

}
