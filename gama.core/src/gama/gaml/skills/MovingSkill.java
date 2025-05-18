/*******************************************************************************************************
 *
 * MovingSkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import static gama.core.common.geometry.GeometryUtils.getFirstPointOf;
import static gama.core.common.geometry.GeometryUtils.getLastPointOf;
import static gama.core.common.geometry.GeometryUtils.getPointsOf;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.ArrayUtils.indexOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.metamodel.topology.filter.In;
import gama.core.metamodel.topology.graph.GamaSpatialGraph;
import gama.core.metamodel.topology.graph.GraphTopology;
import gama.core.metamodel.topology.grid.GamaSpatialMatrix;
import gama.core.metamodel.topology.grid.GridTopology;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.graph.IGraph;
import gama.core.util.path.GamaPath;
import gama.core.util.path.GamaSpatialPath;
import gama.core.util.path.IPath;
import gama.core.util.path.PathFactory;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Maths;
import gama.gaml.operators.Random;
import gama.gaml.operators.spatial.SpatialCreation;
import gama.gaml.operators.spatial.SpatialPunctal;
import gama.gaml.operators.spatial.SpatialRelations;
import gama.gaml.species.ISpecies;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * MovingSkill : This class is intended to define the minimal set of behaviours
 * required from an agent that is able to move. Each member that has a meaning
 * in GAML is annotated with the respective tags (vars, getter, setter, init,
 * action & args)
 *
 * @author drogoul 4 juil. 07
 */

@doc("The moving skill is intended to define the minimal set of behaviours required for agents that are able to move on different topologies")
@vars({ @variable(name = IKeyword.LOCATION, type = IType.POINT, depends_on = IKeyword.SHAPE, doc = @doc("Represents the current position of the agent")),
	@variable(name = IKeyword.SPEED, type = IType.FLOAT, init = "1.0", doc = @doc("Represents the speed of the agent (in meter/second)")),
	@variable(name = IKeyword.HEADING, type = IType.FLOAT, init = "rnd(360.0)", doc = @doc("Represents the absolute heading of the agent in degrees.")),
	@variable(name = "current_path", type = IType.PATH, init = "nil", doc = @doc("Represents the path on which the agent is moving on (goto action on a graph)")),
	@variable(name = "current_edge", type = IType.GEOMETRY, init = "nil", doc = @doc("Represents the agent/geometry on which the agent is located (only used with a graph)")),
	@variable(name = IKeyword.REAL_SPEED, type = IType.FLOAT, init = "0.0", doc = @doc("Represents the actual speed of the agent (in meter/second)")), })
@skill(name = IKeyword.MOVING_SKILL, concept = { IConcept.SKILL, IConcept.AGENT_MOVEMENT })
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MovingSkill extends Skill {

    /**
     * Gets the heading.
     *
     * @param agent
     *            the agent
     * @return the heading
     */
    @getter(IKeyword.HEADING)
    public Double getHeading(final IAgent agent) {
	Double h = (Double) agent.getAttribute(IKeyword.HEADING);
	if (h == null) {
	    h = agent.getScope().getRandom().next() * 360;
	    setHeading(agent, h);
	}
	return Maths.checkHeading(h);
    }

    /**
     * Sets the heading.
     *
     * @param agent
     *            the agent
     * @param heading
     *            the heading
     */
    @setter(IKeyword.HEADING)
    public void setHeading(final IAgent agent, final double heading) {
	if (agent == null) {
	    return;
	}
	final double headingValue = heading % 360;
	final Double oldValue = (Double) agent.getAttribute(IKeyword.HEADING);
	if (oldValue == null || oldValue.doubleValue() != headingValue) {
	    agent.setAttribute(IKeyword.HEADING, headingValue);
	    agent.notifyVarValueChange(IKeyword.HEADING, headingValue);
	}
    }

    /**
     * Gets the destination.
     *
     * @param agent
     *            the agent
     * @return the destination
     */
    @getter(IKeyword.DESTINATION)
    public GamaPoint getDestination(final IAgent agent) {
	if (agent == null) {
	    return null;
	}
	final GamaPoint actualLocation = agent.getLocation();
	final double dist = computeDistance(agent.getScope(), agent);
	final ITopology topology = getTopology(agent);
	return topology.getDestination(agent.getScope(), actualLocation, getHeading(agent), dist, false);
    }

    /**
     * Sets the destination.
     *
     * @param agent
     *            the agent
     * @param p
     *            the p
     */
    @setter(IKeyword.DESTINATION)
    public void setDestination(final IAgent agent, final GamaPoint p) {
	// READ_ONLY
    }

    /**
     * Gets the speed.
     *
     * @param agent
     *            the agent
     * @return the speed
     */
    @getter(IKeyword.SPEED)
    public static Double getSpeed(final IAgent agent) {
	if (agent == null) {
	    return 0.0;
	}
	return (Double) agent.getAttribute(IKeyword.SPEED);
    }

    /**
     * Gets the real speed.
     *
     * @param agent
     *            the agent
     * @return the real speed
     */
    @getter(IKeyword.REAL_SPEED)
    public static Double getRealSpeed(final IAgent agent) {
	if (agent == null) {
	    return 0.0;
	}
	return (Double) agent.getAttribute(IKeyword.REAL_SPEED);
    }

    /**
     * Sets the speed.
     *
     * @param agent
     *            the agent
     * @param s
     *            the s
     */
    @setter(IKeyword.SPEED)
    public static void setSpeed(final IAgent agent, final Double s) {
	if (agent == null) {
	    return;
	}
	final Double oldValue = (Double) agent.getAttribute(IKeyword.SPEED);
	if (oldValue == null || oldValue.doubleValue() != s) {
	    agent.setAttribute(IKeyword.SPEED, s);
	    agent.notifyVarValueChange(IKeyword.SPEED, s);
	}
    }

    /**
     * Sets the real speed.
     *
     * @param agent
     *            the agent
     * @param s
     *            the s
     */
    @setter(IKeyword.REAL_SPEED)
    public static void setRealSpeed(final IAgent agent, final Double s) {
	if (agent == null) {
	    return;
	}
	agent.setAttribute(IKeyword.REAL_SPEED, s);
    }

    /**
     * Gets the location.
     *
     * @param agent
     *            the agent
     * @return the location
     */
    @getter(value = IKeyword.LOCATION, initializer = true)
    public GamaPoint getLocation(final IAgent agent) {
	if (agent == null) {
	    return null;
	}
	return agent.getLocation();
    }

    /**
     * Sets the location.
     *
     * @param agent
     *            the agent
     * @param p
     *            the p
     */
    @setter(IKeyword.LOCATION)
    // Correctly manages the heading
    public void setLocation(final IAgent agent, final GamaPoint p) {
	if (agent == null) {
	    return;
	}
	final ITopology topology = getTopology(agent);
	final GamaPoint oldLocation = agent.getLocation();
	Coordinate oldCoords = new Coordinate(oldLocation.getX(), oldLocation.getY(), oldLocation.getZ());
	if (!topology.isTorus() && p != null && !p.equalsWithTolerance(oldCoords, 0.01)) {
	    final Double newHeading = topology.directionInDegreesTo(agent.getScope(), oldLocation, p);
	    if (newHeading != null) {
		setHeading(agent, newHeading);
	    }
	}
	agent.setLocation(p);
    }

    /**
     * Sets the current path.
     *
     * @param agent
     *            the agent
     * @param p
     *            the p
     */
    @setter("current_path")
    public static void setCurrentPath(final IAgent agent, final IPath p) {
	// READ_ONLY
    }

    /**
     * Gets the current path.
     *
     * @param agent
     *            the agent
     * @return the current path
     */
    @getter(value = "current_path")
    public static IPath getCurrentPath(final IAgent agent) {
	if (agent == null) {
	    return null;
	}
	return (IPath) agent.getAttribute("current_path");
    }

    /**
     * Sets the current edge.
     *
     * @param agent
     *            the agent
     * @param g
     *            the g
     */
    @setter("current_edge")
    public void setCurrentEdge(final IAgent agent, final IShape g) {
	// READ_ONLY
    }

    /**
     * Gets the current edge.
     *
     * @param agent
     *            the agent
     * @return the current edge
     */
    @getter(value = "current_edge")
    public IShape getCurrentEdge(final IAgent agent) {
	if (agent == null) {
	    return null;
	}
	return (IShape) agent.getAttribute("current_edge");
    }

    /**
     * Sets the current edge.
     *
     * @param agent
     *            the agent
     * @param path
     *            the path
     */
    public void setCurrentEdge(final IAgent agent, final IPath path) {
	if (path != null) {
	    final Integer index = (Integer) agent.getAttribute("index_on_path");
	    if (index < path.getEdgeList().size()) {
		agent.setAttribute("current_edge", path.getEdgeList().get(index));
	    }
	}
    }

    /**
     * Sets the current edge.
     *
     * @param agent
     *            the agent
     * @param graph
     *            the graph
     */
    public void setCurrentEdge(final IAgent agent, final IGraph graph) {
	if (graph != null) {
	    final Integer index = (Integer) agent.getAttribute("index_on_path");
	    if (index < graph.getEdges().size()) {
		agent.setAttribute("current_edge", graph.getEdges().get(index));
	    }
	}
    }

    /**
     * @throws GamaRuntimeException
     * @throws GamaRuntimeException
     *             Prim: move randomly. Has to be redefined for every class that
     *             implements this interface.
     *
     * @param args
     *            the args speed (meter/sec) : the speed with which the agent
     *            wants to move distance (meter) : the distance the agent want
     *            to cover in one step amplitude (in degrees) : 360 or 0 means
     *            completely random move, while other values, combined with the
     *            heading of the agent, define the angle in which the agent will
     *            choose a new place. A bounds (geometry, agent, list of agents,
     *            list of geometries, species) can be specified
     * @return the path followed
     */

    protected double computeHeadingFromAmplitude(final IScope scope, final IAgent agent) throws GamaRuntimeException {
	final double ampl = scope.hasArg("amplitude") ? scope.getFloatArg("amplitude") : 359;
	setHeading(agent, getHeading(agent) + scope.getRandom().between(-ampl / 2.0, ampl / 2.0));
	return getHeading(agent);
    }

    /**
     * Compute heading.
     *
     * @param scope
     *            the scope
     * @param agent
     *            the agent
     * @return the double
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    protected double computeHeading(final IScope scope, final IAgent agent) throws GamaRuntimeException {
	final Double heading = scope.hasArg(IKeyword.HEADING) ? scope.getFloatArg(IKeyword.HEADING) : null;
	if (heading != null) {
	    setHeading(agent, heading);
	}
	return getHeading(agent);
    }

    /**
     * Compute distance.
     *
     * @param scope
     *            the scope
     * @param agent
     *            the agent
     * @return the double
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    protected Double computeDistance(final IScope scope, final IAgent agent) throws GamaRuntimeException {
	// We do not change the speed of the agent anymore. Only the current
	// primitive is affected
	final Double s = scope.hasArg(IKeyword.SPEED) ? scope.getFloatArg(IKeyword.SPEED) : getSpeed(agent);
	// 20/1/2012 Change : The speed of the agent is multiplied by the
	// timestep in order to
	// obtain the maximal distance it can cover in one step.
	return s * scope.getClock().getStepInSeconds();
    }

    /**
     * Compute target.
     *
     * @param scope
     *            the scope
     * @param agent
     *            the agent
     * @return the i shape
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    protected IShape computeTarget(final IScope scope, final IAgent agent) throws GamaRuntimeException {
	final Object target = scope.getArg("target", IType.NONE);
	IShape result = null;
	if (target instanceof IShape) {
	    result = (IShape) target;// ((ILocated) target).getLocation();
	}
	// if ( result == null ) {
	// scope.setStatus(ExecutionStatus.failure);
	// }
	return result;
    }

    /**
     * Compute topology.
     *
     * @param scope
     *            the scope
     * @param agent
     *            the agent
     * @return the i topology
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    protected ITopology computeTopology(final IScope scope, final IAgent agent) throws GamaRuntimeException {
	final Object on = scope.getArg("on", IType.NONE);
	final ITopology topo = Cast.asTopology(scope, on);
	if (topo == null) {
	    return scope.getTopology();
	}
	return topo;
    }

    /**
     * Compute move weights.
     *
     * @param scope
     *            the scope
     * @return the map
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    protected Map computeMoveWeights(final IScope scope) throws GamaRuntimeException {
	return scope.hasArg("move_weights") ? (Map) scope.getArg("move_weights", IType.MAP) : null;
    }

    /**
     * Prim move randomly.
     *
     * @param scope
     *            the scope
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    @action(name = "wander", args = {
	    @arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
	    @arg(name = "amplitude", type = IType.FLOAT, optional = true, doc = @doc("a restriction placed on the random heading choice. The new heading is chosen in the range (heading - amplitude/2, heading+amplitude/2)")),
	    @arg(name = IKeyword.BOUNDS, type = IType.GEOMETRY, optional = true, doc = @doc("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry)")),
	    @arg(name = IKeyword.ON, type = IType.GRAPH, optional = true, doc = @doc("the graph that restrains this move (the agent moves on the graph")),
	    @arg(name = "proba_edges", type = IType.MAP, optional = true, doc = @doc("When the agent moves on a graph, the probability to choose another edge. If not defined, each edge has the same probability to be chosen")) }, doc = @doc(examples = {
		    @example("do wander speed: speed - 10 amplitude: 120 bounds: agentA;") }, value = "Moves the agent towards a random location at the maximum distance (with respect to its speed). The heading of the agent is chosen randomly if no amplitude is specified. This action changes the value of heading."))
    public boolean primMoveRandomly(final IScope scope) throws GamaRuntimeException {
	final IAgent agent = getCurrentAgent(scope);
	final GamaPoint location = agent.getLocation();
	final double heading = computeHeadingFromAmplitude(scope, agent);
	final double dist = computeDistance(scope, agent);

	GamaPoint loc = scope.getTopology().getDestination(scope, location, heading, dist, true);
	if (loc == null) {
	    setHeading(agent, heading - 180);
	    // pathFollowed = null;
	} else {
	    final Object on = scope.getArg(IKeyword.ON, IType.GRAPH);
	    Double newHeading = null;
	    if (on instanceof GamaSpatialGraph graph) {
		IMap<IShape, Double> probaDeplacement = null;
		if (scope.hasArg("proba_edges")) {
		    probaDeplacement = (IMap<IShape, Double>) scope.getVarValue("proba_edges");
		}
		moveToNextLocAlongPathSimplified(scope, agent, graph, dist, probaDeplacement);
		return true;
	    }
	    final Object bounds = scope.getArg(IKeyword.BOUNDS, IType.NONE);
	    if (bounds != null) {
		IShape geom = GamaGeometryType.staticCast(scope, bounds, null, false);

		if (geom.getGeometries().size() > 1) {
		    for (final IShape g : geom.getGeometries()) {
			if (g.euclidianDistanceTo(location) < 0.01) {
			    geom = g;
			    break;
			}
		    }
		}
		if (geom.getInnerGeometry() != null) {
		    final GamaPoint loc2 = computeLocationForward(scope, dist, loc, geom);
		    if (!loc2.equals(loc)) {
			newHeading = heading - 180;
			loc = loc2;
		    }
		}
	    }

	    // Enable to use wander in 3D space. An agent will wander in the
	    // plan define by its z value.
	    loc.z = agent.getLocation().getZ();
	    agent.setAttribute(IKeyword.REAL_SPEED,
		    loc.euclidianDistanceTo(location) / scope.getClock().getStepInSeconds());

	    setLocation(agent, loc);
	    if (newHeading != null) {
		setHeading(agent, newHeading);

	    }
	}
	return true;
    }

    /**
     * Prim move forward.
     *
     * @param scope
     *            the scope
     * @return the i path
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    @action(name = "move", args = {
	    @arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
	    @arg(name = IKeyword.HEADING, type = IType.FLOAT, optional = true, doc = @doc("the angle (in degree) of the target direction.")),
	    @arg(name = IKeyword.BOUNDS, type = IType.GEOMETRY, optional = true, doc = @doc("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry"))

    }, doc = @doc(examples = {
	    @example("do move speed: speed - 10 heading: heading + rnd (30) bounds: agentA;") }, value = "moves the agent forward, the distance being computed with respect to its speed and heading. The value of the corresponding variables are used unless arguments are passed."))

    public IPath primMoveForward(final IScope scope) throws GamaRuntimeException {
	final IAgent agent = getCurrentAgent(scope);
	final GamaPoint location = agent.getLocation();
	final Double dist = computeDistance(scope, agent);
	final Double heading = computeHeading(scope, agent);

	GamaPoint loc = scope.getTopology().getDestination(scope, location, heading, dist, true);
	if (loc == null) {
	    setHeading(agent, heading - 180);
	} else {
	    final Object bounds = scope.getArg(IKeyword.BOUNDS, IType.NONE);
	    if (bounds != null) {
		final IShape geom = GamaGeometryType.staticCast(scope, bounds, null, false);
		if (geom != null && geom.getInnerGeometry() != null) {
		    loc = computeLocationForward(scope, dist, loc, geom);
		}
	    }
	    setLocation(agent, loc);
	}
	if (loc != null) {
	    agent.setAttribute(IKeyword.REAL_SPEED,
		    loc.euclidianDistanceTo(location) / scope.getClock().getStepInSeconds());
	} else {
	    agent.setAttribute(IKeyword.REAL_SPEED, 0.0);
	}
	return null;
    }

    /**
     * Prim follow.
     *
     * @param scope
     *            the scope
     * @return the i path
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    @action(name = "follow", args = {
	    @arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
	    @arg(name = "path", type = IType.PATH, optional = false, doc = @doc("a path to be followed.")),
	    @arg(name = "move_weights", type = IType.MAP, optional = true, doc = @doc("Weights used for the moving.")),
	    @arg(name = "return_path", type = IType.BOOL, optional = true, doc = @doc("if true, return the path followed (by default: false)")) }, doc = @doc(value = "moves the agent along a given path passed in the arguments.", returns = "optional: the path followed by the agent.", examples = {
		    @example("do follow speed: speed * 2 path: road_path;") }))
    public IPath primFollow(final IScope scope) throws GamaRuntimeException {
	final IAgent agent = getCurrentAgent(scope);
	final double dist = computeDistance(scope, agent);
	final Boolean returnPath = scope.getBoolArg("return_path");
	final IMap weigths = (IMap) computeMoveWeights(scope);
	final GamaPath path = scope.hasArg("path") ? (GamaPath) scope.getArg("path", IType.PATH) : null;
	if (path != null && !path.getEdgeList().isEmpty()) {
	    if (returnPath != null && returnPath) {
		final IPath pathFollowed = moveToNextLocAlongPath(scope, agent, path, dist, weigths);
		if (pathFollowed == null) {
		    notMoving(agent);

		    return null;
		}
		return pathFollowed;
	    }
	    moveToNextLocAlongPathSimplified(scope, agent, path, dist, weigths);
	    return null;
	}
	notMoving(agent);
	return null;
    }

    /**
     * Prim goto.
     *
     * @param scope
     *            the scope
     * @return the i path
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    @action(name = "goto", args = {
	    @arg(name = "target", type = IType.GEOMETRY, optional = false, doc = @doc("the location or entity towards which to move.")),
	    @arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
	    @arg(name = "on", type = IType.NONE, optional = true, doc = @doc("graph, topology, list of geometries or map of geometries that restrain this move")),
	    @arg(name = "recompute_path", type = IType.BOOL, optional = true, doc = @doc("if false, the path is not recompute even if the graph is modified (by default: true)")),
	    @arg(name = "return_path", type = IType.BOOL, optional = true, doc = @doc("if true, return the path followed (by default: false)")),
	    @arg(name = "move_weights", type = IType.MAP, optional = true, doc = @doc("Weights used for the moving.")) }, doc = @doc(value = "moves the agent towards the target passed in the arguments.", returns = "optional: the path followed by the agent.", examples = {
		    @example("do goto target: (one_of road).location speed: speed * 2 on: road_network;") }))
    public IPath primGoto(final IScope scope) throws GamaRuntimeException {
	final IAgent agent = getCurrentAgent(scope);
	final GamaPoint source = agent.getLocation().copy(scope);
	final double maxDist = computeDistance(scope, agent);
	IShape goal = computeTarget(scope, agent);
	final Boolean returnPath = scope.hasArg("return_path") ? (Boolean) scope.getArg("return_path", IType.NONE)
		: false;
	IContainer on = null;

	Object onV = scope.getArg("on", IType.NONE);
	Object rt;
	if (onV instanceof IShape && ((IShape) onV).isLine()) {
	    rt = onV;
	} else {
	    if (onV instanceof ISpecies) {
		on = ((ISpecies) onV).listValue(scope, Types.AGENT, false);
	    } else if (onV instanceof IList) {

		on = GamaListFactory.create(Types.AGENT);
		final IList ags = (IList) onV;
		if (!ags.isEmpty() && ags.get(0) instanceof IAgent) {
		    ((IList) on).addAll(ags);
		    onV = ((IAgent) ags.get(0)).getSpecies();
		}
	    } else if (onV instanceof IMap) {
		on = GamaMapFactory.wrap(Types.AGENT, Types.NO_TYPE, (IMap) onV);
		onV = ((IAgent) ((IMap) onV).getKeys().get(scope, 0)).getSpecies();
	    }
	    rt = Cast.asTopology(scope, onV instanceof IMap ? ((IMap) onV).keySet() : onV);
	}

	if (on != null && on.isEmpty(scope)) {
	    on = null;
	}
	final IShape edge = rt instanceof IShape ? (IShape) rt : null;
	final ITopology topo = rt instanceof ITopology ? (ITopology) rt : scope.getTopology();
	if (goal == null || topo == null) {
	    notMoving(agent);
	    if (returnPath) {
		return PathFactory.newInstance(scope, topo, source, source, GamaListFactory.EMPTY_LIST, false);
	    }
	    return null;
	}
	if (topo instanceof GridTopology) {
	    // source =
	    // ((GamaSpatialMatrix)topo.getPlaces()).getAgentAt(source).getLocation();
	    goal = ((GamaSpatialMatrix) topo.getPlaces()).getAgentAt(goal.getLocation()).getLocation();
	}
	if (source.equals(goal.getLocation())) {
	    notMoving(agent);
	    if (returnPath) {
		return PathFactory.newInstance(scope, topo, source, source, GamaListFactory.EMPTY_LIST, false);
	    }

	    return null;
	}

	Boolean recomputePath = (Boolean) scope.getArg("recompute_path", IType.NONE);
	if (recomputePath == null) {
	    recomputePath = true;
	}
	IPath path = (GamaPath) agent.getAttribute("current_path");
	if (recomputePath && topo instanceof GridTopology) {
	    agent.setAttribute("current_path", null);
	    path = null;
	}
	if (path == null || path.getTopology(scope) != null && !path.getTopology(scope).equals(topo)
		|| !((IShape) path.getEndVertex()).getLocation().equals(goal.getLocation())
		|| !((IShape) path.getStartVertex()).getLocation().equals(source.getLocation())) {

	    if (edge != null) {
		final IList<IShape> edges = GamaListFactory.create(Types.GEOMETRY);
		edges.add(edge);
		path = new GamaSpatialPath(source.getGeometry(), goal, edges, true);
	    } else if (topo instanceof GridTopology) {
		if (on instanceof IList) {
		    path = ((GridTopology) topo).pathBetween(scope, source, goal, (IList) on);
		} else if (on instanceof IMap) {
		    path = ((GridTopology) topo).pathBetween(scope, source, goal, (IMap) on);
		}

	    } else {
		path = topo.pathBetween(scope, agent, goal);
	    }
	} else if (topo instanceof GraphTopology
		&& (((GraphTopology) topo).getPlaces() != path.getGraph() || recomputePath && ((GraphTopology) topo)
			.getPlaces().getPathComputer().getVersion() != path.getGraphVersion())) {
	    path = topo.pathBetween(scope, agent, goal);
	}
	if (path == null) {
	    notMoving(agent);
	    if (returnPath) {
		return PathFactory.newInstance(scope, topo, source, source,
			GamaListFactory.<IShape> create(Types.GEOMETRY), false);
	    }
	    return null;
	}

	final IMap weigths = (IMap) computeMoveWeights(scope);
	if (returnPath) {
	    final IPath pathFollowed = moveToNextLocAlongPath(scope, agent, path, maxDist, weigths);
	    if (pathFollowed == null) {
		return PathFactory.newInstance(scope, topo, source, source,
			GamaListFactory.<IShape> create(Types.GEOMETRY), false);
	    }
	    return pathFollowed;
	}
	moveToNextLocAlongPathSimplified(scope, agent, path, maxDist, weigths);
	return null;
    }

    /**
     * Not moving.
     *
     * @param agent
     *            the agent
     */
    private void notMoving(final IAgent agent) {
	setRealSpeed(agent, 0.0);
	agent.setAttribute("current_edge", null);
	agent.setAttribute("current_path", null);
    }

    /**
     * @throws GamaRuntimeException
     *             Return the next location toward a target on a line
     *
     * @param coords
     *            coordinates of the line
     * @param source
     *            current location
     * @param target
     *            location to reach
     * @param distance
     *            max displacement distance
     * @return the next location
     */

    protected IList initMoveAlongPath3D(final IAgent agent, final IPath path, final GamaPoint cl) {
	GamaPoint currentLocation = cl.copy(GAMA.getRuntimeScope());
	final IList initVals = GamaListFactory.create();

	Integer index = 0;
	Integer indexSegment = 1;
	Integer endIndexSegment = 1;
	GamaPoint falseTarget = null;
	final IList<IShape> edges = path.getEdgeGeometry();
	if (path.isVisitor(agent)) {
	    index = path.indexOf(agent);
	    indexSegment = path.indexSegmentOf(agent);

	} else {
	    if (edges.isEmpty()) {
		return null;
	    }
	    path.acceptVisitor(agent);

	    double dist = Double.MAX_VALUE;
	    int i = 0;
	    for (final IShape e : edges) {
		final GamaPoint[] points = getPointsOf(e);
		int j = 0;
		for (final GamaPoint pt : points) {
		    final double d = pt.euclidianDistanceTo(cl);
		    if (d < dist) {
			currentLocation = pt;
			dist = d;
			index = i;
			indexSegment = j + 1;
			if (dist == 0.0) {
			    break;
			}
		    }
		    j++;
		}

		if (dist == 0.0) {
		    break;
		}
		i++;
	    }
	}
	final GamaPoint[] points = getPointsOf(edges.lastValue(GAMA.getRuntimeScope()));
	int j = 0;
	double dist = Double.MAX_VALUE;
	final GamaPoint end = ((IShape) path.getEndVertex()).getLocation();
	for (final GamaPoint pt : points) {
	    final double d = pt.euclidianDistanceTo(end);
	    if (d < dist) {
		dist = d;
		endIndexSegment = j;
		falseTarget = pt;
		if (dist == 0.0) {
		    break;
		}

	    }
	    j++;
	}
	initVals.add(index);
	initVals.add(indexSegment);
	initVals.add(endIndexSegment);
	initVals.add(currentLocation);
	initVals.add(falseTarget);
	return initVals;
    }

    /**
     * Inits the move along path.
     *
     * @param agent
     *            the agent
     * @param path
     *            the path
     * @param cl
     *            the cl
     * @return the i list
     */
    protected IList initMoveAlongPath(final IAgent agent, final IPath path, final GamaPoint cl) {
	GamaPoint currentLocation = cl;
	try (final Collector.AsList initVals = Collector.getList()) {
	    Integer index = 0;
	    Integer indexSegment = 1;
	    Integer endIndexSegment = 0;
	    GamaPoint falseTarget = null;
	    final IList<IShape> edges = path.getEdgeGeometry();
	    if (edges.isEmpty()) {
		return null;
	    }
	    final int nb = edges.size();
	    if (path.getGraph() == null && nb == 1 && edges.get(0).getInnerGeometry().getNumPoints() == 2) {
		index = 0;
		indexSegment = 0;
		endIndexSegment = 0;
		falseTarget = ((IShape) path.getEndVertex()).getLocation();
		path.acceptVisitor(agent);

	    } else {
		if (path.isVisitor(agent)) {
		    index = path.indexOf(agent);
		    indexSegment = path.indexSegmentOf(agent);

		} else {
		    path.acceptVisitor(agent);
		    double distanceS = Double.MAX_VALUE;
		    IShape line = null;
		    for (int i = 0; i < nb; i++) {
			line = edges.get(i);
			final double distS = Distance.pointToSegment(currentLocation, getFirstPointOf(line),
				getLastPointOf(line));
			if (distS < distanceS) {
			    distanceS = distS;
			    index = i;
			}
		    }
		    line = edges.get(index);
		    final GamaPoint[] points = getPointsOf(line);
		    if (contains(points, currentLocation)) {
			currentLocation = new GamaPoint(currentLocation);
			indexSegment = indexOf(points, currentLocation) + 1;
		    } else {
			currentLocation = SpatialPunctal._closest_point_to(currentLocation, line);
			if (points.length >= 3) {
			    distanceS = Double.MAX_VALUE;
			    final int nbSp = points.length;
			    for (int i = 0; i < nbSp - 1; i++) {
				final double distS = Distance.pointToSegment(currentLocation, points[i], points[i + 1]);
				if (distS < distanceS) {
				    distanceS = distS;
				    indexSegment = i + 1;
				    currentLocation.z = points[i].z + (points[i + 1].z - points[i].z)
					    * currentLocation.distance(points[i]) / points[i].distance(points[i + 1]);
				}
			    }
			} else if (points.length >= 2) {
			    final GamaPoint c0 = points[0];
			    final GamaPoint c1 = points[1];
			    currentLocation.z = c0.getZ()
				    + (c1.z - c0.z) * currentLocation.distance(c0) / line.getPerimeter();
			} else {
			    currentLocation.z = points[0].z;
			}
		    }
		}
		final IShape lineEnd = edges.get(nb - 1);
		final GamaPoint end = ((IShape) path.getEndVertex()).getLocation();
		final GamaPoint[] points = getPointsOf(lineEnd);
		if (contains(points, end)) {
		    falseTarget = new GamaPoint(end);
		    endIndexSegment = indexOf(points, end) + 1;
		} else {
		    falseTarget = SpatialPunctal._closest_point_to(end, lineEnd);
		    endIndexSegment = 1;
		    if (points.length >= 3) {
			double distanceT = Double.MAX_VALUE;
			for (int i = 0; i < points.length - 1; i++) {
			    final double distT = Distance.pointToSegment(falseTarget, points[i], points[i + 1]);// segment.distance(pointGeom);
			    if (distT < distanceT) {
				distanceT = distT;
				endIndexSegment = i + 1;
				falseTarget.z = points[i].z + (points[i + 1].z - points[i].z)
					* falseTarget.distance3D(points[i]) / points[i].distance3D(points[i + 1]);
			    }
			}
		    } else {
			final GamaPoint c0 = points[0];
			final GamaPoint c1 = points[1];
			falseTarget.z = c0.getZ()
				+ (c1.getZ() - c0.getZ()) * falseTarget.distance3D(c0) / lineEnd.getPerimeter();
		    }
		}
	    }
	    initVals.add(index);
	    initVals.add(indexSegment);
	    initVals.add(endIndexSegment);
	    initVals.add(currentLocation);
	    initVals.add(falseTarget);
	    return initVals.items();
	}
    }

    /**
     * Inits the move along path.
     *
     * @param scope
     *            the scope
     * @param agent
     *            the agent
     * @param graph
     *            the graph
     * @param currentLoc
     *            the current loc
     * @return the i list
     */
    @SuppressWarnings("null")
    protected IList initMoveAlongPath(final IScope scope, final IAgent agent, final GamaSpatialGraph graph,
	    final GamaPoint currentLoc) {
	GamaPoint currentLocation = currentLoc;
	try (final Collector.AsList initVals = Collector.getList()) {
	    Integer index = 0;
	    Integer indexSegment = 1;
	    Integer reverse = 0;
	    final IList<IShape> edges = graph.getEdges();
	    if (edges.isEmpty()) {
		return null;
	    }
	    final int nb = edges.size();
	    if (nb == 1 && edges.get(0).getInnerGeometry().getNumPoints() == 2) {
		index = 0;
		indexSegment = 1;
	    } else {
		IShape line = null;
		index = (Integer) agent.getAttribute("index_on_path");
		indexSegment = (Integer) agent.getAttribute("index_on_path_segment");
		reverse = (Integer) agent.getAttribute("reverse");
		if (index == null || indexSegment == null) {
		    reverse = scope.getRandom().between(0, 1);
		    final boolean optimization = graph.edgeSet().size() > 1000;
		    final double dist = optimization
			    ? Math.sqrt(scope.getSimulation().getArea()) / graph.edgeSet().size() * 100
			    : -1;
		    if (graph.isAgentEdge()) {
			final IAgentFilter filter = In.edgesOf(graph);
			if (optimization) {
			    final Collection<IAgent> ags = scope.getSimulation().getTopology().getNeighborsOf(scope,
				    currentLocation, dist, filter);
			    if (!ags.isEmpty()) {
				double distMin = Double.MAX_VALUE;
				for (final IAgent e : ags) {
				    final double d = currentLocation.euclidianDistanceTo(e);
				    if (d < distMin) {
					line = e;
					distMin = d;
				    }
				}
			    }
			}
			if (line == null) {
			    line = scope.getSimulation().getTopology().getAgentClosestTo(scope, currentLocation,
				    filter);
			}
			index = edges.indexOf(line);
		    } else {
			double distanceS = Double.MAX_VALUE;
			for (int i = 0; i < nb; i++) {
			    line = edges.get(i);
			    final double distS = line.euclidianDistanceTo(currentLocation);
			    if (distS < distanceS) {
				distanceS = distS;
				index = i;
			    }
			}
			line = edges.get(index);
		    }
		    final GamaPoint[] points = getPointsOf(line);
		    if (contains(points, currentLocation)) {
			currentLocation = new GamaPoint(currentLocation);
			indexSegment = indexOf(points, currentLocation) + 1;
		    } else {
			currentLocation = SpatialPunctal._closest_point_to(currentLocation, line);
			if (points.length >= 3) {
			    Double distanceS = Double.MAX_VALUE;
			    for (int i = 0; i < points.length - 1; i++) {
				final double distS = Distance.pointToSegment(currentLocation, points[i], points[i + 1]); // segment.distance(pointGeom);
				if (distS < distanceS) {
				    distanceS = distS;
				    indexSegment = i + 1;
				    currentLocation.z = points[i].z
					    + (points[i + 1].z - points[i].z) * currentLocation.distance3D(points[i])
						    / points[i].distance3D(points[i + 1]);
				}
			    }
			} else {
			    indexSegment = 1;
			    currentLocation.z = points[0].getZ() + (points[1].z - points[0].z)
				    * currentLocation.distance3D(points[0]) / line.getPerimeter();
			}
		    }
		}
	    }

	    initVals.add(index);
	    initVals.add(indexSegment);
	    initVals.add(reverse);
	    return initVals.items();
	}
    }

    /**
     * Move to next loc along path simplified.
     *
     * @param scope
     *            the scope
     * @param agent
     *            the agent
     * @param graph
     *            the graph
     * @param d
     *            the d
     * @param probaEdge
     *            the proba edge
     */
    public void moveToNextLocAlongPathSimplified(final IScope scope, final IAgent agent, final GamaSpatialGraph graph,
	    final double d, final IMap probaEdge) {
	GamaPoint currentLocation = agent.getLocation().copy(scope);
	final IList indexVals = initMoveAlongPath(scope, agent, graph, currentLocation);
	if (indexVals == null) {
	    return;
	}
	int index = (Integer) indexVals.get(0);
	int indexSegment = (Integer) indexVals.get(1);
	int inverse = (Integer) indexVals.get(2);
	IShape edge = (IShape) graph.getEdges().get(index);
	double distance = d;
	double travelledDist = 0.0;
	double computedHeading = 0.0;
	while (true) {
	    Coordinate coords[] = edge.getInnerGeometry().getCoordinates();
	    if (!graph.isDirected() && inverse == 1) {
		final int si = coords.length;
		final Coordinate coords2[] = new Coordinate[si];
		for (int i = 0; i < coords.length; i++) {
		    coords2[i] = coords[si - 1 - i];
		}
		coords = coords2;
	    }

	    final double weight = graph.getEdgeWeight(edge) / edge.getGeometry().getPerimeter();
	    for (int j = indexSegment; j < coords.length; j++) {
		final GamaPoint pt = new GamaPoint(coords[j]);
		final double dis = pt.distance3D(currentLocation);
		final double dist = weight * dis;
		computedHeading = SpatialRelations.towards(scope, currentLocation, pt);

		if (distance < dist) {
		    final double ratio = distance / dist;
		    travelledDist += dis * ratio;
		    final double newX = currentLocation.x + ratio * (pt.x - currentLocation.x);
		    final double newY = currentLocation.y + ratio * (pt.y - currentLocation.y);
		    final double newZ = currentLocation.z + ratio * (pt.z - currentLocation.z);
		    currentLocation.setLocation(newX, newY, newZ);
		    distance = 0;
		    break;
		}
		if (distance <= dist) {
		    currentLocation = pt;
		    travelledDist += dis;
		    distance = 0;
		    if (indexSegment < coords.length - 1) {
			indexSegment++;
		    } else {
			indexSegment = 1;
		    }
		    break;
		}
		currentLocation = pt;
		travelledDist += dis;

		distance = distance - dist;
		indexSegment++;
		if (j == coords.length - 1) {
		    IShape node = (IShape) graph.getEdgeTarget(edge);
		    if (!graph.isDirected() && !node.getLocation().equals(currentLocation)) {
			node = (IShape) graph.getEdgeSource(edge);
		    }
		    final List<IShape> nextRoads = new ArrayList<IShape>(
			    graph.isDirected() ? graph.outgoingEdgesOf(node) : graph.edgesOf(node));
		    if (nextRoads.isEmpty()) {
			distance = 0;
			break;
		    }
		    if (nextRoads.size() == 1) {
			edge = nextRoads.get(0);
		    }
		    if (nextRoads.size() > 1) {
			if (probaEdge == null || probaEdge.isEmpty()) {
			    edge = nextRoads.get(scope.getRandom().between(0, nextRoads.size() - 1));
			} else {
			    final IList<Double> distribution = GamaListFactory.create(Types.FLOAT);
			    for (final IShape r : nextRoads) {
				final Double val = (Double) probaEdge.get(r);
				distribution.add(val == null ? 0.0 : val);
			    }
			    edge = nextRoads.get(Random.opRndChoice(scope, distribution));
			}
		    }
		    index = graph.getEdges().indexOf(edge);
		    if (!graph.isDirected()) {
			if (currentLocation.equals(graph.getEdgeSource(edge))) {
			    inverse = 0;
			} else {
			    inverse = 1;
			}
		    }
		    indexSegment = 0;
		}
	    }
	    if (distance == 0) {
		break;
	    }
	    indexSegment = 1;
	}
	agent.setAttribute(IKeyword.REAL_SPEED, travelledDist / scope.getClock().getStepInSeconds());

	agent.setAttribute("index_on_path", index);
	setCurrentEdge(agent, graph);
	agent.setAttribute("index_on_path_segment", indexSegment);
	agent.setAttribute("reverse", inverse);
	setLocation(agent, currentLocation);

	setHeading(agent, computedHeading);
    }

    /**
     * Move to next loc along path simplified.
     *
     * @param scope
     *            the scope
     * @param agent
     *            the agent
     * @param path
     *            the path
     * @param d
     *            the d
     * @param weigths
     *            the weigths
     */
    private void moveToNextLocAlongPathSimplified(final IScope scope, final IAgent agent, final IPath path,
	    final double d, final IMap weigths) {
	GamaPoint currentLocation = agent.getLocation().copy(scope);
	final IList indexVals = ((GamaSpatialPath) path).isThreeD() ? initMoveAlongPath3D(agent, path, currentLocation)
		: initMoveAlongPath(agent, path, currentLocation);
	if (indexVals == null) {
	    return;
	}
	int index = (Integer) indexVals.get(0);
	int indexSegment = (Integer) indexVals.get(1);
	final int endIndexSegment = (Integer) indexVals.get(2);
	currentLocation = (GamaPoint) indexVals.get(3);
	final GamaPoint falseTarget = (GamaPoint) indexVals.get(4);
	final IList<IShape> edges = path.getEdgeGeometry();
	double computedHeading = 0.0;
	final int nb = edges.size();
	double distance = d;
	final GamaSpatialGraph graph = (GamaSpatialGraph) path.getGraph();
	double travelledDist = 0.0;
	for (int i = index; i < nb; i++) {
	    final IShape line = edges.get(i);
	    final GamaPoint[] coords = getPointsOf(line);

	    double weight;
	    if (weigths == null) {
		weight = computeWeigth(graph, path, line);
	    } else {
		final IShape realShape = path.getRealObject(line);
		final Double w = realShape == null ? null
			: (Double) weigths.get(realShape) / realShape.getGeometry().getPerimeter();
		weight = w == null ? computeWeigth(graph, path, line) : w;
	    }
	    for (int j = indexSegment; j < coords.length; j++) {
		GamaPoint pt = null;
		if (i == nb - 1 && j == endIndexSegment) {
		    pt = falseTarget;
		} else {
		    pt = new GamaPoint(coords[j]);
		}
		final double dis = pt.distance3D(currentLocation);
		final double dist = weight * dis;
		computedHeading = SpatialRelations.towards(scope, currentLocation, pt);

		if (distance < dist) {
		    final double ratio = distance / dist;
		    final double newX = currentLocation.x + ratio * (pt.x - currentLocation.x);
		    final double newY = currentLocation.y + ratio * (pt.y - currentLocation.y);
		    final double newZ = currentLocation.z + ratio * (pt.z - currentLocation.z);
		    travelledDist += dis * ratio;
		    currentLocation.setLocation(newX, newY, newZ);
		    distance = 0;
		    break;
		}
		if (distance <= dist) {
		    currentLocation = pt;
		    distance = 0;
		    travelledDist += dis;
		    if (indexSegment < coords.length - 1) {
			indexSegment++;
		    } else {
			if (index < nb - 1) {
			    index++;
			}
			indexSegment = 1;
		    }
		    break;
		}
		currentLocation = pt;
		travelledDist += dis;
		distance = distance - dist;
		if (i == nb - 1 && j == endIndexSegment) {
		    break;
		}
		indexSegment++;
	    }
	    if (distance == 0) {
		break;
	    }
	    indexSegment = 1;
	    if (index < nb - 1) {
		index++;
	    }
	}
	if (currentLocation.equals(falseTarget)) {

	    currentLocation = Cast.asPoint(scope, path.getEndVertex());
	    index++;
	}
	path.setIndexSegementOf(agent, indexSegment);
	path.setIndexOf(agent, index);
	agent.setAttribute(IKeyword.REAL_SPEED, travelledDist / scope.getClock().getStepInSeconds());

	setCurrentEdge(agent, path);
	setLocation(agent, currentLocation);
	setHeading(agent, computedHeading);
	path.setSource(agent.getLocation()/* .copy(scope) */);

    }

    /**
     * Compute weigth.
     *
     * @param graph
     *            the graph
     * @param path
     *            the path
     * @param line
     *            the line
     * @return the double
     */
    protected double computeWeigth(final IGraph graph, final IPath path, final IShape line) {
	if (graph == null) {
	    return 1.0;
	}
	final IShape realShape = path.getRealObject(line);
	return realShape == null ? 1 : graph.getEdgeWeight(realShape) / realShape.getGeometry().getPerimeter();
    }

    /**
     * Move to next loc along path.
     *
     * @param scope
     *            the scope
     * @param agent
     *            the agent
     * @param path
     *            the path
     * @param d
     *            the d
     * @param weigths
     *            the weigths
     * @return the i path
     */
    private IPath moveToNextLocAlongPath(final IScope scope, final IAgent agent, final IPath path, final double d,
	    final IMap weigths) {
	final GamaPoint startLocation = agent.getLocation().copy(scope);

	GamaPoint currentLocation = agent.getLocation().copy(scope);
	final IList indexVals = ((GamaSpatialPath) path).isThreeD() ? initMoveAlongPath3D(agent, path, currentLocation)
		: initMoveAlongPath(agent, path, currentLocation);
	if (indexVals == null) {
	    return null;
	}
	final IList<IShape> segments = GamaListFactory.create(Types.GEOMETRY);
	final IMap agents = GamaMapFactory.createUnordered();

	int index = (Integer) indexVals.get(0);
	int indexSegment = (Integer) indexVals.get(1);
	final int endIndexSegment = (Integer) indexVals.get(2);
	currentLocation = (GamaPoint) indexVals.get(3);
	final GamaPoint falseTarget = (GamaPoint) indexVals.get(4);
	final IList<IShape> edges = path.getEdgeGeometry();
	final int nb = edges.size();
	double distance = d;
	double travelledDist = 0.0;
	double computedHeading = 0.0;
	final GamaSpatialGraph graph = (GamaSpatialGraph) path.getGraph();
	for (int i = index; i < nb; i++) {
	    final IShape line = edges.get(i);
	    final Coordinate coords[] = line.getInnerGeometry().getCoordinates();
	    double weight;
	    if (weigths == null) {
		weight = computeWeigth(graph, path, line);
	    } else {
		final IShape realShape = path.getRealObject(line);
		final Double w = realShape == null ? null
			: (Double) weigths.get(realShape) / realShape.getGeometry().getPerimeter();
		weight = w == null ? computeWeigth(graph, path, line) : w;
	    }

	    for (int j = indexSegment; j < coords.length; j++) {
		GamaPoint pt = null;
		if (i == nb - 1 && j == endIndexSegment) {
		    pt = falseTarget;
		} else {
		    pt = new GamaPoint(coords[j]);
		}
		final double dis = pt.distance3D(currentLocation);
		final double dist = weight * dis;
		computedHeading = SpatialRelations.towards(scope, currentLocation, pt);

		if (distance < dist) {
		    final GamaPoint pto = currentLocation.copy(scope);

		    final double ratio = distance / dist;
		    travelledDist += dis * ratio;

		    final double newX = currentLocation.x + ratio * (pt.x - currentLocation.x);
		    final double newY = currentLocation.y + ratio * (pt.y - currentLocation.y);
		    final double newZ = currentLocation.z + ratio * (pt.z - currentLocation.z);
		    currentLocation.setLocation(newX, newY, newZ);
		    distance = 0;

		    final IShape gl = GamaGeometryType.buildLine(pto, currentLocation);
		    final IShape sh = path.getRealObject(line);
		    if (sh != null) {
			final IAgent a = sh.getAgent();
			if (a != null) {
			    agents.put(gl, a);
			}
		    }
		    segments.add(gl);

		    break;
		}
		if (distance <= dist) {
		    travelledDist += dis;
		    final IShape gl = GamaGeometryType.buildLine(currentLocation, pt);
		    if (path.getRealObject(line) != null) {
			final IAgent a = path.getRealObject(line).getAgent();

			if (a != null) {
			    agents.put(gl, a);
			}
		    }

		    segments.add(gl);
		    currentLocation = pt;
		    distance = 0;
		    if (indexSegment < coords.length - 1) {
			indexSegment++;
		    } else {
			if (index < nb - 1) {
			    index++;
			}
			indexSegment = 1;
		    }
		    break;
		}
		travelledDist += dis;
		final IShape gl = GamaGeometryType.buildLine(currentLocation, pt);
		final IShape sh = path.getRealObject(line);
		if (sh != null) {
		    final IAgent a = sh.getAgent();
		    if (a != null) {
			agents.put(gl, a);
		    }
		}
		segments.add(gl);

		currentLocation = pt;
		distance = distance - dist;
		if (i == nb - 1 && j == endIndexSegment) {
		    break;
		}
		indexSegment++;
	    }
	    if (distance == 0) {
		break;
	    }
	    indexSegment = 1;
	    if (index < nb - 1) {
		index++;
	    }
	}
	if (currentLocation.equals(falseTarget)) {

	    currentLocation = Cast.asPoint(scope, path.getEndVertex());
	    index++;
	}
	path.setIndexSegementOf(agent, indexSegment);
	path.setIndexOf(agent, index);
	setCurrentEdge(agent, path);
	setLocation(agent, currentLocation);
	path.setSource(currentLocation.copy(scope));
	agent.setAttribute(IKeyword.REAL_SPEED, travelledDist / scope.getClock().getStepInSeconds());

	if (segments.isEmpty()) {
	    return null;
	}
	final IPath followedPath = PathFactory.newInstance(scope, agent.getTopology(), startLocation, currentLocation,
		segments, false);
	followedPath.setRealObjects(agents);

	setHeading(agent, computedHeading);
	return followedPath;
    }

    /**
     * Compute location forward.
     *
     * @param scope
     *            the scope
     * @param dist
     *            the dist
     * @param loc
     *            the loc
     * @param geom
     *            the geom
     * @return the gama point
     */
    protected GamaPoint computeLocationForward(final IScope scope, final double dist, final GamaPoint loc,
	    final IShape geom) {
	final IList pts = GamaListFactory.create(Types.POINT);
	pts.add(scope.getAgent().getLocation(scope));
	pts.add(loc);
	final IShape line = SpatialCreation.line(scope, pts);
	// line = Spatial.Operators.inter(scope, line, geom);

	if (line == null) {
	    return getCurrentAgent(scope).getLocation(scope);
	}
	if (geom.covers(line)) {
	    return loc;
	}

	// final GamaPoint computedPt = line.getPoints().lastValue(scope);

	final GamaPoint computedPt = SpatialPunctal.closest_points_with(line, geom.getExteriorRing(scope)).get(0);
	if (computedPt != null && computedPt.intersects(geom)) {
	    return computedPt;
	}
	return getCurrentAgent(scope).getLocation(scope);
    }
}
