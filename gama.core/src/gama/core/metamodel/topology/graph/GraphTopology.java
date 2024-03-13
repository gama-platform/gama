/*******************************************************************************************************
 *
 * GraphTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Ordering;

import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.AbstractTopology;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.metamodel.topology.filter.In;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.GamaListFactory;
import gama.core.util.ICollector;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.path.GamaPath;
import gama.core.util.path.GamaSpatialPath;
import gama.core.util.path.IPath;
import gama.core.util.path.PathFactory;
import gama.gaml.types.Types;

/**
 * The class GraphTopology.
 *
 * @author drogoul
 * @since 27 nov. 2011
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GraphTopology extends AbstractTopology {

	/**
	 * @param scope
	 * @param env
	 * @param torus
	 */
	public GraphTopology(final IScope scope, final IShape env, final GamaSpatialGraph graph) {
		super(scope, env, null);
		places = graph;
	}

	/**
	 * Instantiates a new graph topology.
	 *
	 * @param scope
	 *            the scope
	 * @param graph
	 *            the graph
	 */
	// The default topologies for graphs.
	public GraphTopology(final IScope scope, final GamaSpatialGraph graph) {
		this(scope, scope.getSimulation().getGeometry(), graph);
	}

	@Override
	protected boolean canCreateAgents() {
		return true;
	}

	@Override
	public boolean isContinuous() { return false; }

	/**
	 * Optimized closest to.
	 *
	 * @param source
	 *            the source
	 * @param candidates
	 *            the candidates
	 * @return the i shape
	 */
	private IShape optimizedClosestTo(final IShape source, final List<IShape> candidates) {
		IShape result = null;
		final GamaPoint loc = source.getLocation();
		double distMin = Double.MAX_VALUE;
		for (final IShape c : candidates) {
			final double dist = loc.euclidianDistanceTo(c.getLocation());
			if (dist < distMin) {
				distMin = dist;
				result = c;
			}
		}
		return result;
	}

	/**
	 * TODO : doc
	 *
	 * @throws GamaRuntimeException
	 * @throws GamaRuntimeException
	 */
	@Override
	public GamaSpatialPath pathBetween(final IScope scope, final IShape source, final IShape target) {
		// final GamaPoint source = sourceShape.getLocation();
		final GamaSpatialGraph graph = (GamaSpatialGraph) getPlaces();
		IShape sourceN = source;
		IShape targetN = target;
		boolean targetNode = graph.getVertexMap().containsKey(target);
		final boolean isAgentVertex = graph.isEmpty(scope) ? false : graph.getVertices().get(0) instanceof IAgent;
		final boolean targetNSame = isAgentVertex == target instanceof IAgent;
		final boolean sourceNSame = isAgentVertex == source instanceof IAgent;
		boolean sourceNode = graph.getVertexMap().containsKey(source);
		final boolean optimizedClosestTo = GamaPreferences.External.PATH_COMPUTATION_OPTIMIZATION.getValue();

		if (sourceNode && GamaPreferences.External.TOLERANCE_POINTS.getValue() > 0.0) {
			for (final IShape v : graph.getVertexMap().keySet()) {
				if (v.equals(source)) {
					sourceN = v;
					break;
				}
			}
		}
		if (targetNode && GamaPreferences.External.TOLERANCE_POINTS.getValue() > 0.0) {
			for (final IShape v : graph.getVertexMap().keySet()) {
				if (v.equals(target)) {
					targetN = v;
					break;
				}
			}
		}
		if (!sourceNSame && !sourceNode || !targetNSame && !targetNode) {
			for (final Object ed : graph.getVertices()) {
				if (((IShape) ed).getLocation().equals(source.getLocation())) {
					sourceN = (IShape) ed;
					sourceNode = true;
				}
				if (((IShape) ed).getLocation().equals(target.getLocation())) {
					targetN = (IShape) ed;
					targetNode = true;
				}
				if (sourceNode && targetNode) { break; }
			}

		}
		if (sourceNode && targetNode)
			return (GamaSpatialPath) graph.getPathComputer().computeShortestPathBetween(scope, sourceN, targetN);

		IShape edgeS = null, edgeT = null;
		final boolean optimization = graph.edgeSet().size() > 1000;

		final double dist =
				optimization ? Math.sqrt(scope.getSimulation().getArea()) / graph.edgeSet().size() * 100 : -1;

		if (graph.isAgentEdge()) {
			final IAgentFilter filter = In.edgesOf(getPlaces());

			if (!sourceNode) {
				edgeS = getPathEdge(scope, source);
				if (edgeS == null) {
					if (optimizedClosestTo) {
						edgeS = optimizedClosestTo(source, getPlaces().getEdges());
					} else if (optimization) {
						final Collection<IAgent> ags = scope.getSimulation().getAgent().getTopology()
								.getNeighborsOf(scope, source, dist, filter);
						if (!ags.isEmpty()) {
							double distMin = Double.MAX_VALUE;
							for (final IAgent e : ags) {
								final double d = source.euclidianDistanceTo(e);
								if (d < distMin) {
									edgeS = e;
									distMin = d;
								}
							}
						}
					}
					if (edgeS == null) {
						edgeS = scope.getSimulation().getAgent().getTopology().getAgentClosestTo(scope, source, filter);
					}
				}
				// We avoid computing the target if we cannot find any source.
				if (edgeS == null) return null;
			}
			if (!targetNode) {
				// edgeT = getPathEdge(scope, target);

				if (optimizedClosestTo) {
					edgeT = optimizedClosestTo(target, getPlaces().getEdges());
				} else if (optimization) {
					final Collection<IAgent> ags =
							scope.getSimulation().getAgent().getTopology().getNeighborsOf(scope, target, dist, filter);
					if (!ags.isEmpty()) {
						double distMin = Double.MAX_VALUE;
						for (final IAgent e : ags) {
							final double d = target.euclidianDistanceTo(e);
							if (d < distMin) {
								edgeT = e;
								distMin = d;
							}
						}
					}
				}
				if (edgeT == null) {
					edgeT = scope.getSimulation().getAgent().getTopology().getAgentClosestTo(scope, target, filter);
				}

				if (edgeT == null) return null;
			}
		} else {
			double distSMin = Double.MAX_VALUE;
			double distTMin = Double.MAX_VALUE;
			edgeS = getPathEdge(scope, source);
			if (edgeS != null) { distSMin = 0; }
			/*
			 * edgeT = getPathEdge(scope, target); if (edgeT != null) distTMin = 0;
			 */
			if (distSMin > 0 && !sourceNode || distTMin > 0 && !targetNode) {

				for (final Object e : graph.getEdges()) {
					final IShape edge = (IShape) e;
					if (!sourceNode && distSMin > 0) {
						final double distS =
								optimizedClosestTo ? edge.getLocation().euclidianDistanceTo(source.getLocation())
										: edge.euclidianDistanceTo(source);
						if (distS < distSMin) {
							distSMin = distS;
							edgeS = edge;
						}
					}
					if (!targetNode && distTMin > 0) {
						final double distT =
								optimizedClosestTo ? edge.getLocation().euclidianDistanceTo(target.getLocation())
										: edge.euclidianDistanceTo(target);
						if (distT < distTMin) {
							distTMin = distT;
							edgeT = edge;
						}
					}
				}
			}
			if (!sourceNode && edgeS == null || !targetNode && edgeT == null) return null;
		}

		if (getPlaces().isDirected()) {
			final List<IShape> startingEdgesList = new ArrayList<>();
			final List<IShape> targetEdgesList = new ArrayList<>();
			startingEdgesList.add(edgeS);
			if (!sourceNode) {
				final IShape edgeRev = (IShape) graph.getEdge(graph.getEdgeTarget(edgeS), graph.getEdgeSource(edgeS));
				if (edgeRev != null && edgeS != null
						&& edgeRev.euclidianDistanceTo(source) <= edgeS.euclidianDistanceTo(source)) {
					startingEdgesList.add(edgeRev);
				}
			}
			targetEdgesList.add(edgeT);
			if (!targetNode) {
				final IShape edgeRev = (IShape) graph.getEdge(graph.getEdgeTarget(edgeT), graph.getEdgeSource(edgeT));
				if (edgeRev != null && target != null && edgeS != null
						&& edgeRev.euclidianDistanceTo(target) <= edgeS.euclidianDistanceTo(target)) {
					targetEdgesList.add(edgeRev);
				}
			}
			return pathBetweenCommonDirected(scope, startingEdgesList, targetEdgesList, sourceN, targetN, sourceNode,
					targetNode);
		}

		return pathBetweenCommon(scope, graph, edgeS, edgeT, sourceN, targetN, sourceNode, targetNode);
	}

	/**
	 * Gets the path edge.
	 *
	 * @param scope
	 *            the scope
	 * @param ref
	 *            the ref
	 * @return the path edge
	 */
	public IShape getPathEdge(final IScope scope, final IShape ref) {
		if (ref.getAgent() != null) {
			final IShape edge = (IShape) ref.getAgent().getAttribute("current_edge");
			if (edge != null && this.getPlaces().containsEdge(edge)
					&& ref.getLocation().euclidianDistanceTo(edge) < 0.1)
				return edge;
			final IPath path = (GamaPath) ref.getAgent().getAttribute("current_path");
			if (path != null && path.getTopology(scope) != null && this.equals(path.getTopology(scope))
					&& ((IShape) path.getStartVertex()).getLocation().equals(ref.getLocation())) {
				final int index = path.indexOf(ref.getAgent());
				if (index >= path.getEdgeList().size())
					return (IShape) path.getEdgeList().get(path.getEdgeList().size() - 1);
				return (IShape) path.getEdgeList().get(index);
			}
		}
		return null;
	}

	/**
	 * Path between common.
	 *
	 * @param scope
	 *            the scope
	 * @param graph
	 *            the graph
	 * @param edgeS
	 *            the edge S
	 * @param edgeT
	 *            the edge T
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param sourceNode
	 *            the source node
	 * @param targetNode
	 *            the target node
	 * @return the gama spatial path
	 */
	public GamaSpatialPath pathBetweenCommon(final IScope scope, final GamaSpatialGraph graph, final IShape edgeS,
			final IShape edgeT, final IShape source, final IShape target, final boolean sourceNode,
			final boolean targetNode) {
		IList<IShape> edges = GamaListFactory.create(Types.GEOMETRY);
		if (sourceNode && !targetNode) {
			final IShape nodeT1 = (IShape) graph.getEdgeSource(edgeT);
			final IShape nodeT2 = (IShape) graph.getEdgeTarget(edgeT);
			double l1 = 0;
			boolean computeOther = true;
			if (nodeT1 == source) {
				l1 = lengthEdge(edgeT, target, nodeT2, nodeT1);
			} else {
				edges = getPlaces().getPathComputer().computeBestRouteBetween(scope, source, nodeT1);
				final boolean isEmpty = edges.isEmpty();
				l1 = isEmpty ? Double.MAX_VALUE : pathlengthEdges(edges) + lengthEdge(edgeT, target, nodeT2, nodeT1);
				if (!isEmpty) {
					final IShape el = edges.get(edges.size() - 1);
					if (graph.getEdgeSource(el) == nodeT2 || graph.getEdgeTarget(el) == nodeT2) {
						// edges.remove(edges.size()-1);
						computeOther = false;
					}
				}
			}
			if (computeOther) {
				double l2 = 0;
				IList<IShape> edges2 = GamaListFactory.create(Types.GEOMETRY);
				if (nodeT2 == source) {
					l2 = lengthEdge(edgeT, target, nodeT1, nodeT2);
				} else {
					edges2 = getPlaces().getPathComputer().computeBestRouteBetween(scope, source, nodeT2);
					l2 = edges2.isEmpty() ? Double.MAX_VALUE
							: pathlengthEdges(edges2) + lengthEdge(edgeT, target, nodeT1, nodeT2);
				}
				if (l2 < l1) {
					edges = edges2;
					l1 = l2;
				}
			}
			if (l1 == Double.MAX_VALUE) return null;
			if (edges.isEmpty() || edges.get(edges.size() - 1) != edgeT) { edges.add(edgeT); }
		} else if (!sourceNode && targetNode) {
			final IShape nodeS1 = (IShape) graph.getEdgeSource(edgeS);
			final IShape nodeS2 = (IShape) graph.getEdgeTarget(edgeS);
			double l1 = 0;
			boolean computeOther = true;
			if (nodeS1 == target) {
				l1 = lengthEdge(edgeS, source, nodeS2, nodeS1);
			} else {
				edges = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeS1, target);
				final boolean isEmpty = edges.isEmpty();
				l1 = isEmpty ? Double.MAX_VALUE : pathlengthEdges(edges) + lengthEdge(edgeS, source, nodeS2, nodeS1);
				if (!isEmpty) {
					final IShape e0 = edges.get(0);
					if (graph.getEdgeSource(e0) == nodeS2 || graph.getEdgeTarget(e0) == nodeS2) {
						// edges.remove(0);
						computeOther = false;
					}
				}
			}
			if (computeOther) {
				double l2 = 0;
				IList<IShape> edges2 = GamaListFactory.create(Types.GEOMETRY);
				if (nodeS2 == target) {
					l2 = lengthEdge(edgeS, source, nodeS1, nodeS2);
				} else {
					edges2 = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeS2, target);
					l2 = edges2.isEmpty() ? Double.MAX_VALUE
							: pathlengthEdges(edges2) + lengthEdge(edgeS, source, nodeS1, nodeS2);
				}
				if (l2 < l1) {
					edges = edges2;
					l1 = l2;
				}
			}
			if (l1 == Double.MAX_VALUE) return null;
			if (edges.isEmpty() || edges.get(0) != edgeS) { edges.add(0, edgeS); }
		} else {
			final IShape nodeS1 = (IShape) graph.getEdgeSource(edgeS);
			final IShape nodeS2 = (IShape) graph.getEdgeTarget(edgeS);
			final IShape nodeT1 = (IShape) graph.getEdgeSource(edgeT);
			final IShape nodeT2 = (IShape) graph.getEdgeTarget(edgeT);

			double lmin = Double.MAX_VALUE;

			boolean computeS1T2 = true;
			boolean computeS2T1 = true;
			boolean computeS2T2 = true;
			if (nodeS1 == nodeT1) {
				lmin = lengthEdge(edgeS, source, nodeS2, nodeS1) + lengthEdge(edgeT, target, nodeT2, nodeT1);
			} else {
				edges = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeS1, nodeT1);
				final boolean isEmpty = edges.isEmpty();

				final double els = lengthEdge(edgeS, source, nodeS2, nodeS1);
				final double elt = lengthEdge(edgeT, target, nodeT2, nodeT1);
				lmin = isEmpty ? Double.MAX_VALUE : pathlengthEdges(edges) + els + elt;

				if (!isEmpty) {
					final IShape e0 = edges.get(0);
					final IShape el = edges.get(edges.size() - 1);
					if (e0 != el) {
						final boolean ts1 = graph.getEdgeSource(e0) == nodeS2 || graph.getEdgeTarget(e0) == nodeS2;
						final boolean ts2 = graph.getEdgeSource(el) == nodeT2 || graph.getEdgeTarget(el) == nodeT2;
						double valmin = lmin;
						if (ts1) {
							computeS2T1 = false;
							final double val = lmin - els - getPlaces().getEdgeWeight(e0)
									+ lengthEdge(edgeS, source, nodeS1, nodeS2);
							if (valmin > val) { valmin = val; }
						}
						if (ts2) {
							computeS1T2 = false;
							final double val = lmin - elt - getPlaces().getEdgeWeight(el)
									+ lengthEdge(edgeT, target, nodeT1, nodeT2);
							if (valmin > val) { valmin = val; }
						}
						if (ts1 && ts2) {
							computeS2T2 = false;
							final double val = lmin - els - getPlaces().getEdgeWeight(e0) - elt
									- getPlaces().getEdgeWeight(el) + lengthEdge(edgeS, source, nodeS1, nodeS2)
									+ lengthEdge(edgeT, target, nodeT1, nodeT2);
							if (valmin > val) { valmin = val; }
						}
						lmin = valmin;
					}
				}
			}
			if (computeS2T1) {
				double l2 = 0;
				IList<IShape> edges2 = GamaListFactory.create(Types.GEOMETRY);
				if (nodeS2 == nodeT1) {
					l2 = lengthEdge(edgeS, source, nodeS1, nodeS2) + lengthEdge(edgeT, target, nodeT2, nodeT1);
				} else {
					edges2 = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeS2, nodeT1);
					final boolean isEmpty = edges2.isEmpty();
					final double els = lengthEdge(edgeS, source, nodeS1, nodeS2);
					final double elt = lengthEdge(edgeT, target, nodeT2, nodeT1);

					l2 = isEmpty ? Double.MAX_VALUE : pathlengthEdges(edges2) + els + elt;
					if (!isEmpty) {
						final IShape e0 = edges2.get(0);
						final IShape el = edges2.get(edges2.size() - 1);
						if (e0 != el) {
							final boolean ts1 = graph.getEdgeSource(e0) == nodeS1 || graph.getEdgeTarget(e0) == nodeS1;

							final boolean ts2 = graph.getEdgeSource(el) == nodeT2 || graph.getEdgeTarget(el) == nodeT2;
							double valmin = l2;
							if (ts2) {
								computeS2T2 = false;
								final double val = l2 - elt - getPlaces().getEdgeWeight(el)
										+ lengthEdge(edgeT, target, nodeT1, nodeT2);
								if (valmin > val) { valmin = val; }
							}
							if (ts1 && ts2) {
								computeS1T2 = false;
								final double val = l2 - els - getPlaces().getEdgeWeight(e0) - elt
										- getPlaces().getEdgeWeight(el) + lengthEdge(edgeS, source, nodeS2, nodeS1)
										+ lengthEdge(edgeT, target, nodeT1, nodeT2);
								if (valmin > val) { valmin = val; }
							}
							l2 = valmin;
						}

					}
				}
				if (l2 < lmin) {
					edges = edges2;
					lmin = l2;
				}
			}
			if (computeS1T2) {

				double l2 = 0;
				IList<IShape> edges2 = GamaListFactory.create(Types.GEOMETRY);
				if (nodeS1 == nodeT2) {
					l2 = lengthEdge(edgeS, source, nodeS2, nodeS1) + lengthEdge(edgeT, target, nodeT1, nodeT2);
				} else {
					edges2 = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeS1, nodeT2);
					final boolean isEmpty = edges2.isEmpty();
					final double els = lengthEdge(edgeS, source, nodeS2, nodeS1);
					final double elt = lengthEdge(edgeT, target, nodeT1, nodeT2);
					l2 = isEmpty ? Double.MAX_VALUE : pathlengthEdges(edges2) + els + elt;
					if (!isEmpty) {
						final IShape e0 = edges2.get(0);
						final boolean ts1 = graph.getEdgeSource(e0) == nodeS2 || graph.getEdgeTarget(e0) == nodeS2;
						if (ts1) {
							computeS2T2 = false;
							final double val = l2 - els - getPlaces().getEdgeWeight(e0)
									+ lengthEdge(edgeS, source, nodeS1, nodeS2);
							if (l2 > val) { l2 = val; }
						}

					}
				}
				if (l2 < lmin) {
					edges = edges2;
					lmin = l2;
				}
			}
			if (computeS2T2) {
				double l2 = 0;
				IList<IShape> edges2 = GamaListFactory.create(Types.GEOMETRY);
				if (nodeS2 == nodeT2) {
					l2 = lengthEdge(edgeS, source, nodeS1, nodeS2) + lengthEdge(edgeT, target, nodeT1, nodeT2);
				} else {
					edges2 = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeS2, nodeT2);
					l2 = edges2.isEmpty() ? Double.MAX_VALUE : pathlengthEdges(edges2)
							+ lengthEdge(edgeS, source, nodeS1, nodeS2) + lengthEdge(edgeT, target, nodeT1, nodeT2);
				}

				if (l2 < lmin) {
					edges = edges2;
					lmin = l2;
				}
			}
			if (lmin == Double.MAX_VALUE) return null;
			if (edges.isEmpty() || edges.get(0) != edgeS) { edges.add(0, edgeS); }

			if (edges.get(edges.size() - 1) != edgeT) { edges.add(edgeT); }

		}
		return PathFactory.newInstance(scope, this, source, target, edges);
	}

	/**
	 * Path from edges undirected.
	 *
	 * @param scope
	 *            the scope
	 * @param listOfEdges
	 *            the list of edges
	 * @param edgeS
	 *            the edge S
	 * @param edgeT
	 *            the edge T
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param sourceNode
	 *            the source node
	 * @param targetNode
	 *            the target node
	 * @param nodeS
	 *            the node S
	 * @param nodeSbis
	 *            the node sbis
	 * @param nodeT
	 *            the node T
	 * @param computeOther
	 *            the compute other
	 * @return the gama spatial path
	 */
	GamaSpatialPath pathFromEdgesUndirected(final IScope scope, final IList<IShape> listOfEdges, final IShape edgeS,
			final IShape edgeT, final IShape source, final IShape target, final boolean sourceNode,
			final boolean targetNode, final IShape nodeS, final IShape nodeSbis, final IShape nodeT,
			final boolean computeOther) {
		IList<IShape> edges = listOfEdges;
		if (edges.isEmpty() || edges.get(0) == null) return null;
		if (!sourceNode) {
			Set edgesSetInit = new LinkedHashSet(Arrays.asList(edges.get(0).getInnerGeometry().getCoordinates()));
			final Set edgesSetS = new LinkedHashSet(Arrays.asList(edgeS.getInnerGeometry().getCoordinates()));
			if (!edgesSetS.equals(edgesSetInit)) {
				double l1 = 0;
				double l2 = 1;
				IList<IShape> edgesbis = null;
				if (computeOther) {
					l1 = pathlengthEdges(edges) + lengthEdge(edgeS, source, nodeSbis, nodeS);
					edgesbis = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeSbis, nodeT);
					l2 = pathlengthEdges(edgesbis) + lengthEdge(edgeS, source, nodeS, nodeSbis);
				}
				if (l1 < l2 || edgesbis == null || edgesbis.isEmpty() || edgesbis.get(0) == null) {
					edges.add(0, edgeS);
				} else {
					edges = edgesbis;
					edgesSetInit = new LinkedHashSet(Arrays.asList(edges.get(0).getInnerGeometry().getCoordinates()));
					if (!edgesSetS.equals(edgesSetInit)) { edges.add(0, edgeS); }
				}

			}
		}
		if (!targetNode) {
			final Set edgesSetEnd =
					new LinkedHashSet(Arrays.asList(edges.get(edges.size() - 1).getInnerGeometry().getCoordinates()));
			final Set edgesSetT = new LinkedHashSet(Arrays.asList(edgeT.getInnerGeometry().getCoordinates()));

			if (!edgesSetT.equals(edgesSetEnd)) { edges.add(edgeT); }
		}

		// return new GamaPath(this, source, target, edges);
		return PathFactory.newInstance(scope, this, source, target, edges);
	}

	/**
	 * Pathlength edges.
	 *
	 * @param edges
	 *            the edges
	 * @return the double
	 */
	public double pathlengthEdges(final IList<IShape> edges) {
		double length = 0;
		for (final IShape sp : edges) { length += getPlaces().getWeightOf(sp); }
		return length;
	}

	/**
	 * Length edge.
	 *
	 * @param edge
	 *            the edge
	 * @param location
	 *            the location
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return the double
	 */
	public double lengthEdge(final IShape edge, final IShape location, final IShape source, final IShape target) {
		final double dist = source.getLocation().euclidianDistanceTo(target.getLocation());
		return dist == 0 ? 0
				: getPlaces().getWeightOf(edge) * location.euclidianDistanceTo(target.getLocation()) / dist;
	}

	/**
	 * Path between common directed.
	 *
	 * @param scope
	 *            the scope
	 * @param edgeS
	 *            the edge S
	 * @param edgeT
	 *            the edge T
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param sourceNode
	 *            the source node
	 * @param targetNode
	 *            the target node
	 * @return the gama spatial path
	 */
	public GamaSpatialPath pathBetweenCommonDirected(final IScope scope, final List<IShape> edgeS,
			final List<IShape> edgeT, final IShape source, final IShape target, final boolean sourceNode,
			final boolean targetNode) {
		if (edgeS.size() == 1 && edgeT.size() == 1)
			return pathBetweenCommonDirected(scope, edgeS.get(0), edgeT.get(0), source, target, sourceNode, targetNode);
		double wMin = Double.MAX_VALUE;
		GamaSpatialPath shortestPath = null;
		for (final IShape eS : edgeS) {
			for (final IShape eT : edgeT) {
				final GamaSpatialPath path =
						pathBetweenCommonDirected(scope, eS, eT, source, target, sourceNode, targetNode);
				if (path == null) { continue; }
				final double weight = path.getWeight();
				if (weight < wMin) {
					wMin = weight;
					shortestPath = path;
				}
			}
		}
		return shortestPath;

	}

	/**
	 * Path between common directed.
	 *
	 * @param scope
	 *            the scope
	 * @param edgeS
	 *            the edge S
	 * @param edgeT
	 *            the edge T
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param sourceNode
	 *            the source node
	 * @param targetNode
	 *            the target node
	 * @return the gama spatial path
	 */
	public GamaSpatialPath pathBetweenCommonDirected(final IScope scope, final IShape edgeS, final IShape edgeT,
			final IShape source, final IShape target, final boolean sourceNode, final boolean targetNode) {
		IList<IShape> edges;

		if (!sourceNode && !targetNode && edgeS.equals(edgeT)) {
			final GamaPoint ptS = new GamaPoint(edgeS.getInnerGeometry().getCoordinates()[0]);
			if (source.euclidianDistanceTo(ptS) < target.euclidianDistanceTo(ptS)) {
				edges = GamaListFactory.create(Types.GEOMETRY);
				edges.add(edgeS);
				return PathFactory.newInstance(scope, this, source, target, edges);
			}
		}
		final IShape nodeS = sourceNode ? source : getPlaces().getEdgeTarget(edgeS);
		final IShape nodeT = targetNode ? target : getPlaces().getEdgeSource(edgeT);

		if (nodeS.equals(nodeT)) {
			edges = GamaListFactory.create(Types.GEOMETRY);
			if (edgeS != null) { edges.add(edgeS); }
			if (edgeT != null) { edges.add(edgeT); }
			return PathFactory.newInstance(scope, this, source, target, edges);
		}
		edges = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeS, nodeT);
		if (edges.isEmpty() || edges.get(0) == null) return null;

		if (!sourceNode) { edges.add(0, edgeS); }
		if (!targetNode) { edges.add(edges.size(), edgeT); }

		// return new GamaPath(this, source, target, edges);
		return PathFactory.newInstance(scope, this, source, target, edges);
	}

	/**
	 * Path between.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param existingEdge
	 *            the existing edge
	 * @return the gama spatial path
	 */
	public GamaSpatialPath pathBetween(final IScope scope, final IShape source, final IShape target,
			final IShape existingEdge) {
		IList<IShape> edges;
		if (source.equals(target)) {
			edges = GamaListFactory.create(Types.GEOMETRY);
			if (existingEdge != null) { edges.add(existingEdge); }
			return PathFactory.newInstance(scope, this, source, target, edges);
		}
		edges = getPlaces().getPathComputer().computeBestRouteBetween(scope, source, target);
		if (existingEdge != null) {
			edges = edges.listValue(scope, Types.NO_TYPE, true);
			edges.addValueAtIndex(scope, 0, existingEdge);
		}
		if (edges.isEmpty() || edges.get(0) == null) return null;

		return PathFactory.newInstance(scope, this, source, target, edges);
	}

	@Override
	public GamaSpatialPath pathBetween(final IScope scope, final GamaPoint source, final GamaPoint target) {
		return pathBetween(scope, (IShape) source, (IShape) target);
	}

	/**
	 * @see gama.interfaces.IValue#stringValue()
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "GraphTopology";
	}

	/**
	 * @see gama.environment.AbstractTopology#_toGaml()
	 */
	@Override
	protected String _toGaml(final boolean includingBuiltIn) {
		return "GraphTopology";
	}

	/**
	 * @see gama.environment.AbstractTopology#_copy()
	 */
	@Override
	protected ITopology _copy(final IScope scope) {
		return new GraphTopology(scope, environment, (GamaSpatialGraph) places);
	}

	/**
	 * @see gama.environment.AbstractTopology#getRandomPlace()
	 */

	@Override
	public ISpatialGraph getPlaces() { return (GamaSpatialGraph) super.getPlaces(); }

	@Override
	public boolean isValidLocation(final IScope scope, final GamaPoint p) {
		return isValidGeometry(scope, p.getGeometry());
	}

	@Override
	public boolean isValidGeometry(final IScope scope, final IShape g) {
		// Geometry g2 = g.getInnerGeometry();
		for (final IShape g1 : places.iterable(scope)) { if (g1.intersects(g)) return true; }
		return false;
	}

	@Override
	public Double distanceBetween(final IScope scope, final IShape source, final IShape target) {
		final GamaSpatialPath path = this.pathBetween(scope, source, target);
		if (path == null) return Double.MAX_VALUE;
		if (path.getEdgeList().isEmpty()) return 0.0;

		// Patrick: no idea of the goal of the following code (in commentary)
		/*
		 * final Coordinate[] coordsSource = path.getEdgeList().get(0).getInnerGeometry().getCoordinates(); final
		 * Coordinate[] coordsTarget = path.getEdgeList().get(path.getEdgeList().size() -
		 * 1).getInnerGeometry().getCoordinates(); if (coordsSource.length == 0 || coordsTarget.length == 0) { return
		 * Double.MAX_VALUE; } final GamaPoint sourceEdges = new GamaPoint(coordsSource[0]); final GamaPoint targetEdges
		 * = new GamaPoint(coordsTarget[coordsTarget.length - 1]); path.setSource(sourceEdges);
		 * path.setTarget(targetEdges);
		 */
		return path.getDistance(scope);
	}

	@Override
	public Double distanceBetween(final IScope scope, final GamaPoint source, final GamaPoint target) {
		final GamaSpatialPath path = this.pathBetween(scope, source, target);
		if (path == null) return Double.MAX_VALUE;
		if (path.getEdgeList().isEmpty()) return 0.0;
		// Patrick: no idea of the goal of the following code (in commentary)
		/*
		 * final Coordinate[] coordsSource = path.getEdgeList().get(0).getInnerGeometry().getCoordinates(); final
		 * Coordinate[] coordsTarget = path.getEdgeList().get(path.getEdgeList().size() -
		 * 1).getInnerGeometry().getCoordinates(); if (coordsSource.length == 0 || coordsTarget.length == 0) { return
		 * Double.MAX_VALUE; } final GamaPoint sourceEdges = new GamaPoint(coordsSource[0]); final GamaPoint targetEdges
		 * = new GamaPoint(coordsTarget[coordsTarget.length - 1]); path.setSource(sourceEdges);
		 * path.setTarget(targetEdges);
		 */
		return path.getDistance(scope);
	}

	@Override
	public Double directionInDegreesTo(final IScope scope, final IShape source, final IShape target) {
		// WARNING As it is computed every time the location of an agent is set,
		// and as the source and target in that
		// case do not correspond to existing nodes, it may be safer (and
		// faster) to call the root topology
		return root.directionInDegreesTo(scope, source, target);
		// final GamaSpatialPath path = this.pathBetween(scope, source, target);
		// if ( path == null ) { return null; }
		// // LineString ls = (LineString)
		// path.getEdgeList().first().getInnerGeometry();
		// // TODO Check this
		// final double dx = target.getLocation().getX() -
		// source.getLocation().getX();
		// final double dy = target.getLocation().getY() -
		// source.getLocation().getY();
		// final double result = Maths.atan2Opt(dy, dx);
		// return Maths.checkHeading((int) result);
	}

	@Override
	public Collection<IAgent> getAgentsIn(final IScope scope, final IShape source, final IAgentFilter f,
			final SpatialRelation relation) {
		final Collection<IAgent> result = super.getAgentsIn(scope, source, f, relation);
		result.removeIf(each -> each.dead() || !isValidGeometry(scope, each));
		return result;
	}

	@Override
	public boolean isTorus() {
		// TODO Why is it the case ?
		return false;
	}

	@Override
	public IList kPathsBetween(final IScope scope, final IShape source, final IShape target, final int k) {
		final ISpatialGraph graph = getPlaces();
		if (source == target) return GamaListFactory.create();
		final boolean sourceNode = graph.containsVertex(source);
		final boolean targetNode = graph.containsVertex(target);

		if (sourceNode && targetNode)
			return graph.getPathComputer().computeKShortestPathsBetween(scope, source, target, k);

		IShape edgeS = null, edgeT = null;

		final IAgentFilter filter = In.edgesOf(getPlaces());

		if (!sourceNode) {
			if (getPlaces().getEdgeSpecies() != null) {
				edgeS = getAgentClosestTo(scope, source, filter);
			} else {
				edgeS = shapeClosest(this.getPlaces().getEdges(), source);
			}

			// We avoid computing the target if we cannot find any source.
			if (edgeS == null) return null;
		}
		if (!targetNode) {
			if (getPlaces().getEdgeSpecies() != null) {
				edgeT = getAgentClosestTo(scope, target, filter);
			} else {
				edgeT = shapeClosest(this.getPlaces().getEdges(), target);
			}
			if (edgeT == null) return null;
		}

		if (getPlaces().isDirected())
			return KpathsBetweenCommonDirected(scope, edgeS, edgeT, source, target, sourceNode, targetNode, k);

		return KpathsBetweenCommon(scope, edgeS, edgeT, source, target, sourceNode, targetNode, k);
	}

	/**
	 * Shape closest.
	 *
	 * @param shapes
	 *            the shapes
	 * @param geom
	 *            the geom
	 * @return the i shape
	 */
	public IShape shapeClosest(final List<IShape> shapes, final IShape geom) {
		IShape cp = null;
		double distMin = Double.MAX_VALUE;
		for (final IShape shp : shapes) {
			final double dist = shp.euclidianDistanceTo(geom);
			if (dist < distMin) {
				distMin = dist;
				cp = shp;
			}
		}
		return cp;
	}

	@Override
	public IList<GamaSpatialPath> kPathsBetween(final IScope scope, final GamaPoint source, final GamaPoint target,
			final int k) {
		return kPathsBetween(scope, source.getGeometry(), target.getGeometry(), k);
	}

	/**
	 * Kpaths between common.
	 *
	 * @param scope
	 *            the scope
	 * @param edgeS
	 *            the edge S
	 * @param edgeT
	 *            the edge T
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param sourceNode
	 *            the source node
	 * @param targetNode
	 *            the target node
	 * @param k
	 *            the k
	 * @return the i list
	 */
	public IList KpathsBetweenCommon(final IScope scope, final IShape edgeS, final IShape edgeT, final IShape source,
			final IShape target, final boolean sourceNode, final boolean targetNode, final int k) {
		IShape nodeS = source;
		IShape nodeSbis = source;
		IShape nodeT = target;

		if (!targetNode) {
			IShape t1 = null;
			IShape t2 = null;
			t1 = getPlaces().getEdgeSource(edgeT);
			t2 = getPlaces().getEdgeTarget(edgeT);
			if (t1 == null || t2 == null) return null;
			nodeT = t1;
			if (t1.getLocation().euclidianDistanceTo(target.getLocation()) > t2.getLocation()
					.euclidianDistanceTo(target.getLocation())) {
				nodeT = t2;
			}
		}
		if (!sourceNode) {
			IShape s1 = null;
			IShape s2 = null;
			s1 = getPlaces().getEdgeSource(edgeS);
			s2 = getPlaces().getEdgeTarget(edgeS);
			if (s1 == null || s2 == null) return null;
			nodeS = s1;
			nodeSbis = s2;
			if (s1.equals(nodeT) || !s2.equals(nodeT) && s1.getLocation().euclidianDistanceTo(source.getLocation()) > s2
					.getLocation().euclidianDistanceTo(source.getLocation())) {
				nodeS = s2;
				nodeSbis = s1;
			}
		}
		final List<IList<IShape>> edgesList =
				getPlaces().getPathComputer().computeKBestRoutesBetween(scope, nodeS, nodeT, k);
		final IList results = GamaListFactory.create(Types.PATH);
		for (final IList<IShape> edges : edgesList) {
			final GamaSpatialPath pp = pathFromEdgesUndirected(scope, edges, edgeS, edgeT, source, target, sourceNode,
					targetNode, nodeS, nodeSbis, nodeT, false);
			if (pp != null) { results.add(pp); }

		}
		Collections.sort(results);
		return results;
	}

	/**
	 * Kpaths between common directed.
	 *
	 * @param scope
	 *            the scope
	 * @param edgeS
	 *            the edge S
	 * @param edgeT
	 *            the edge T
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param sourceNode
	 *            the source node
	 * @param targetNode
	 *            the target node
	 * @param k
	 *            the k
	 * @return the i list
	 */
	public IList KpathsBetweenCommonDirected(final IScope scope, final IShape edgeS, final IShape edgeT,
			final IShape source, final IShape target, final boolean sourceNode, final boolean targetNode, final int k) {
		final IList results = GamaListFactory.create(Types.PATH);
		if (edgeS.equals(edgeT)) {
			final GamaPoint ptS = new GamaPoint(edgeS.getInnerGeometry().getCoordinates()[0]);
			if (source.euclidianDistanceTo(ptS) < target.euclidianDistanceTo(ptS)) {
				final IList<IShape> edges = GamaListFactory.create(Types.GEOMETRY);
				edges.add(edgeS);
				results.add(PathFactory.newInstance(scope, this, source, target, edges));
				return results;
			}
		}
		final IShape nodeS = sourceNode ? source : getPlaces().getEdgeTarget(edgeS);
		final IShape nodeT = targetNode ? target : getPlaces().getEdgeSource(edgeT);

		if (nodeS.equals(nodeT)) {
			final IList<IShape> edges = GamaListFactory.create(Types.GEOMETRY);
			edges.add(edgeS);
			edges.add(edgeT);
			results.add(PathFactory.newInstance(scope, this, source, target, edges));
			return results;
		}
		final List<IList<IShape>> edgesList =
				getPlaces().getPathComputer().computeKBestRoutesBetween(scope, nodeS, nodeT, k);
		for (IList<IShape> edges : edgesList) {
			/**
			 * TODO AD: PROBLEM HERE. Why is edges recomputed immediately ?
			 */
			edges = getPlaces().getPathComputer().computeBestRouteBetween(scope, nodeS, nodeT);
			if (edges.isEmpty() || edges.get(0) == null) { continue; }

			if (!sourceNode) { edges.add(0, edgeS); }
			if (!targetNode) { edges.add(edges.size(), edgeT); }

			// return new GamaPath(this, source, target, edges);
			final GamaSpatialPath pp = PathFactory.newInstance(scope, this, source, target, edges);
			results.add(pp);
		}
		Collections.sort(results);
		return results;

	}

	@Override
	public Collection<IAgent> getNeighborsOf(final IScope scope, final IShape source, final Double distance,
			final IAgentFilter filter) throws GamaRuntimeException {
		final ISpatialGraph graph = this.getPlaces();
		boolean searchEdges = false;
		boolean searchVertices = false;
		try (ICollector<IAgent> agents = Collector.getOrderedSet()) {
			IShape realS = null;
			IShape tcr = null;
			if (graph.containsEdge(source)) {
				realS = source;
			} else {
				double minDist = Double.POSITIVE_INFINITY;
				for (final IShape e : graph.getEdges()) {
					final double d = e.euclidianDistanceTo(source);
					if (d < minDist) {
						minDist = d;
						tcr = e;
					}
				}
				if (source.euclidianDistanceTo(graph.getEdgeSource(tcr)) < source
						.euclidianDistanceTo(graph.getEdgeTarget(tcr))) {
					realS = graph.getEdgeSource(tcr);
				} else {
					realS = graph.getEdgeTarget(tcr);
				}
			}
			if (filter.getSpecies() != null) {
				searchEdges = filter.getSpecies() == graph.getEdgeSpecies();
				searchVertices = filter.getSpecies() == graph.getVertexSpecies();
			}
			if (searchEdges) {
				final Set<IShape> edgs = getNeighborsOfRec(scope, realS, true, distance, graph, new LinkedHashSet<>());
				for (final IShape ed : edgs) { agents.add(ed.getAgent()); }
				return agents.items();
			}
			if (searchVertices) {
				final Set<IShape> nds = getNeighborsOfRec(scope, realS, false, distance, graph, new LinkedHashSet<>());
				for (final IShape nd : nds) { agents.add(nd.getAgent()); }
				return agents.items();
			}
			IContainer agentsTotest = null;
			if (filter.getSpecies() != null) {
				agentsTotest = filter.getSpecies().getAgents(scope);
			} else {
				agentsTotest = scope.getSimulation().getAgents(scope);
			}
			final Set<IShape> edges = getNeighborsOfRec(scope, realS, true, distance, graph, new LinkedHashSet<>());
			for (final Object ob : agentsTotest.iterable(scope)) {
				final IShape ag = (IShape) ob;
				if (filter.accept(scope, source, ag)) {
					IShape rd = null;
					if (graph.getEdges().contains(ag)) {
						rd = ag;
					} else {
						double minDist = Double.POSITIVE_INFINITY;
						for (final IShape e : graph.getEdges()) {
							final double d = e.euclidianDistanceTo(ag);
							if (d < minDist) {
								minDist = d;
								rd = e;
							}
						}
					}

					if (edges.contains(rd) && this.distanceBetween(scope, source, ag) <= distance) {
						agents.add(ag.getAgent());
					}

				}
			}

			return agents.items();
		}

	}

	/**
	 * Gets the neighbors of rec.
	 *
	 * @param scope
	 *            the scope
	 * @param currentSource
	 *            the current source
	 * @param edge
	 *            the edge
	 * @param currentDist
	 *            the current dist
	 * @param graph
	 *            the graph
	 * @param alr
	 *            the alr
	 * @return the neighbors of rec
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public Set<IShape> getNeighborsOfRec(final IScope scope, final IShape currentSource, final boolean edge,
			final double currentDist, final ISpatialGraph graph, final Set<IShape> alr) throws GamaRuntimeException {
		try (Collector.AsOrderedSet<IShape> edges = Collector.getOrderedSet()) {
			final Set<IShape> eds =
					graph.isDirected() ? graph.outgoingEdgesOf(currentSource) : graph.edgesOf(currentSource);
			if (!edge) { edges.add(currentSource.getAgent()); }
			for (final IShape ed : eds) {
				if (alr.contains(ed)) { continue; }
				alr.add(ed);
				final double dist = getPlaces().getEdgeWeight(ed);
				if (edge) { edges.add(ed); }
				if (currentDist - dist > 0) {
					IShape nextNode = null;
					if (graph.isDirected()) {
						nextNode = graph.getEdgeTarget(ed);
					} else {
						nextNode = currentSource == graph.getEdgeTarget(ed) ? graph.getEdgeSource(ed)
								: graph.getEdgeTarget(ed);
					}
					edges.addAll(getNeighborsOfRec(scope, nextNode, edge, currentDist - dist, graph, alr));
				}
			}
			return edges.items();
		}
	}

	@Override
	public IAgent getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter) {
		// A better solution is required !!! this solution is just here to
		// ensure the consistency of the closest operator on graph !
		final List<IAgent> listAgents = new ArrayList();
		listAgents.addAll(filter.getAgents(scope).listValue(scope, Types.AGENT, false));
		listAgents.remove(source);
		IAgent closest = null;
		double minDist = Double.POSITIVE_INFINITY;
		for (final IAgent ag : listAgents) {
			final Double dist = this.distanceBetween(scope, source, ag);
			if (dist < minDist) {
				closest = ag;
				minDist = dist;
				if (dist == 0) { break; }
			}

		}
		return closest;
	}

	@Override
	public Collection<IAgent> getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter,
			final int number) {
		// A better solution is required !!! this solution is just here to
		// ensure the consistency of the closest operator on graph !

		final List<IAgent> listAgents = new ArrayList();
		listAgents.addAll(filter.getAgents(scope).listValue(scope, Types.AGENT, false));
		listAgents.remove(source);
		scope.getRandom().shuffleInPlace(listAgents);
		final Ordering<IAgent> ordering = Ordering.natural().onResultOf(input -> distanceBetween(scope, source, input));
		if (listAgents.size() <= number) {
			Collections.sort(listAgents, ordering);
			return GamaListFactory.wrap(Types.AGENT, listAgents);
		}

		return GamaListFactory.wrap(Types.AGENT, ordering.leastOf(listAgents, number));

	}
}
