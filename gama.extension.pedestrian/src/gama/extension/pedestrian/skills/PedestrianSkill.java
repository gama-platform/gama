/*******************************************************************************************************
 *
 * PedestrianSkill.java, in gama.extension.pedestrian, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.pedestrian.skills;

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
import gama.core.metamodel.topology.graph.GraphTopology;
import gama.core.metamodel.topology.graph.ISpatialGraph;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaList;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.path.IPath;
import gama.gaml.descriptions.ConstantExpressionDescription;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Maths;
import gama.gaml.operators.Points;
import gama.gaml.operators.Random;
import gama.gaml.operators.spatial.SpatialCreation;
import gama.gaml.operators.spatial.SpatialOperators;
import gama.gaml.operators.spatial.SpatialProperties;
import gama.gaml.operators.spatial.SpatialPunctal;
import gama.gaml.operators.spatial.SpatialQueries;
import gama.gaml.skills.MovingSkill;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.IStatement;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class PedestrianSkill.
 */
@skill (
		name = "pedestrian",
		concept = { IConcept.TRANSPORT, IConcept.SKILL },
		doc = @doc ("A skill that provides agent with the ability to walk on continuous space while"
				+ " finding their way on a virtual network"))
@vars ({ @variable (
		name = "shoulder_length",
		type = IType.FLOAT,
		init = "0.45",
		doc = @doc ("The width of the pedestrian (in meters) - classic values: [0.39, 0.515]")),
		@variable (
				name = "minimal_distance",
				type = IType.FLOAT,
				init = "0.0",
				doc = @doc ("Minimal distance between pedestrians")),
		@variable (
				name = "pedestrian_consideration_distance",
				type = IType.FLOAT,
				init = "2.0",
				doc = @doc ("Distance of consideration of other pedestrians (to compute the nearby obstacles, used as distance, the max between this value and (step * speed) - classic value: 3.5m")),
		@variable (
				name = "obstacle_consideration_distance",
				type = IType.FLOAT,
				init = "2.0",
				doc = @doc ("Distance of consideration of obstacles (to compute the nearby obstacles, used as distance, the max between this value and (step * speed) - classic value: 3.5m")),
		@variable (
				name = "avoid_other",
				type = IType.BOOL,
				init = "true",
				doc = @doc ("has the pedestrian to avoid other pedestrians?")),
		@variable (
				name = "obstacle_species",
				type = IType.LIST,
				init = "[]",
				doc = @doc ("the list of species that are considered as obstacles")),
		@variable (
				name = "pedestrian_species",
				type = IType.LIST,
				init = "[]",
				doc = @doc ("the list of species that are considered as pedestrians")),
		@variable (
				name = "proba_detour",
				type = IType.FLOAT,
				init = "0.1",
				doc = @doc ("probability to accept to do a detour")),
		@variable (
				name = "A_pedestrians_SFM",
				type = IType.FLOAT,
				init = "4.5",
				doc = @doc ("Value of A in the SFM model for pedestrians - the force of repulsive interactions (classic values : mean = 4.5, std = 0.3)")),
		@variable (
				name = "A_obstacles_SFM",
				type = IType.FLOAT,
				init = "4.5",
				doc = @doc ("Value of A in the SFM model for obstacles - the force of repulsive interactions (classic values : mean = 4.5, std = 0.3)")),
		@variable (
				name = "B_pedestrians_SFM",
				type = IType.FLOAT,
				init = "2.0",
				doc = @doc ("Value of B in the SFM model for pedestrians - the range (in meters) of repulsive interactions")),
		@variable (
				name = "B_obstacles_SFM",
				type = IType.FLOAT,
				init = "2.0",
				doc = @doc ("Value of B in the SFM model for obstacles - the range (in meters) of repulsive interactions")),
		@variable (
				name = "k_SFM",
				type = IType.FLOAT,
				init = "200",
				doc = @doc ("Value of k in the SFM model: force counteracting body compression")),
		@variable (
				name = "kappa_SFM",
				type = IType.FLOAT,
				init = "400",
				doc = @doc ("Value of kappa in the SFM model: friction counteracting body compression")),
		@variable (
				name = "relaxion_SFM",
				type = IType.FLOAT,
				init = "0.54",
				doc = @doc ("Value of relaxion in the SFM model - the amount of delay time for an agent to adapt.(classic values : mean = 0.54, std = 0.05)")),
		@variable (
				name = "gama_SFM",
				type = IType.FLOAT,
				init = "0.35",
				doc = @doc ("Value of gama in the SFM model  the amount of normal social force added in tangential direction. between 0.0 and 1.0 (classic values : mean = 0.35, std = 0.01)")),
		@variable (
				name = "lambda_SFM",
				type = IType.FLOAT,
				init = "0.5",
				doc = @doc ("Value of lambda in the SFM model - the (an-)isotropy (between 0.0 and 1.0)")),
		@variable (
				name = "n_SFM",
				type = IType.FLOAT,
				init = "2.0",
				doc = @doc ("Value of n in the SFM model (classic values : mean = 2.0, std = 0.1)")),
		@variable (
				name = "n_prime_SFM",
				type = IType.FLOAT,
				init = "3.0",
				doc = @doc ("Value of n\' in the SFM model (classic values : mean = 3.0, std = 0.7)")),
		@variable (
				name = "pedestrian_model",
				type = IType.STRING,
				init = "'simple'",
				doc = @doc ("Model use for the movement of agents (Social Force Model). Can be either \"simple\" "
						+ "or \"advanced\" (default) for different versions of SFM Helbing model")),

		@variable (
				name = "velocity",
				type = IType.POINT,
				init = "{0,0,0}",
				doc = @doc ("The velocity of the pedestrian (in meters)")),
		@variable (
				name = "forces",
				type = IType.MAP,
				init = "[]",
				doc = @doc ("the map of forces")),
		@variable (
				name = "final_waypoint",
				type = IType.GEOMETRY,
				init = "nil",
				doc = @doc ("the final waypoint of the agent")),
		@variable (
				name = "current_waypoint",
				type = IType.GEOMETRY,
				init = "nil",
				doc = @doc ("the current waypoint of the agent")),
		@variable (
				name = "current_index",
				type = IType.INT,
				init = "0",
				doc = @doc ("the current index of the agent waypoint (according to the waypoint list)")),
		@variable (
				name = "waypoints",
				type = IType.LIST,
				of = IType.GEOMETRY,
				init = "[]",
				doc = @doc ("the current list of points/shape that the agent has to reach (path)")),
		@variable (
				name = "roads_waypoints",
				type = IType.MAP,
				init = "[]",
				doc = @doc ("for each waypoint, the associated road")),
		@variable (
				name = "use_geometry_waypoint",
				type = IType.BOOL,
				init = "false",
				doc = @doc ("use geometries as waypoint instead of points")),
		@variable (
				name = "tolerance_waypoint",
				type = IType.FLOAT,
				init = "1.0",
				doc = @doc ("distance to a waypoint (in meters) to consider that an agent is arrived at the waypoint"))

})
public class PedestrianSkill extends MovingSkill {

	// ---------- CONSTANTS -------------- //

	/** The Constant PEDESTRIAN_MODEL. */
	// General mode of walking
	public final static String PEDESTRIAN_MODEL = "pedestrian_model";

	/** The Constant SHOULDER_LENGTH. */
	public final static String SHOULDER_LENGTH = "shoulder_length";

	/** The Constant MINIMAL_DISTANCE. */
	public final static String MINIMAL_DISTANCE = "minimal_distance";

	/** The Constant CURRENT_TARGET. */
	public final static String CURRENT_TARGET = "current_waypoint";

	/** The Constant OBSTACLE_CONSIDERATION_DISTANCE. */
	public final static String OBSTACLE_CONSIDERATION_DISTANCE = "obstacle_consideration_distance";

	/** The Constant PEDESTRIAN_CONSIDERATION_DISTANCE. */
	public final static String PEDESTRIAN_CONSIDERATION_DISTANCE = "pedestrian_consideration_distance";

	/** The Constant PROBA_DETOUR. */
	public final static String PROBA_DETOUR = "proba_detour";

	/** The Constant AVOID_OTHER. */
	public final static String AVOID_OTHER = "avoid_other";

	/** The Constant OBSTACLE_SPECIES. */
	public final static String OBSTACLE_SPECIES = "obstacle_species";

	/** The Constant PEDESTRIAN_SPECIES. */
	public final static String PEDESTRIAN_SPECIES = "pedestrian_species";

	/** The Constant VELOCITY. */
	public final static String VELOCITY = "velocity";

	/** The Constant FORCES. */
	public final static String FORCES = "forces";

	/** The Constant A_PEDESTRIAN_SFM. */
	public final static String A_PEDESTRIAN_SFM = "A_pedestrians_SFM";

	/** The Constant A_OBSTACLES_SFM. */
	public final static String A_OBSTACLES_SFM = "A_obstacles_SFM";

	/** The Constant B_PEDESTRIAN_SFM. */
	public final static String B_PEDESTRIAN_SFM = "B_pedestrians_SFM";

	/** The Constant B_OBSTACLES_SFM. */
	public final static String B_OBSTACLES_SFM = "B_obstacles_SFM";

	/** The Constant K_SFM. */
	public final static String K_SFM = "k_SFM";

	/** The Constant KAPPA_SFM. */
	public final static String KAPPA_SFM = "kappa_SFM";

	/** The Constant RELAXION_SFM. */
	public final static String RELAXION_SFM = "relaxion_SFM";

	/** The Constant GAMA_SFM. */
	public final static String GAMA_SFM = "gama_SFM";

	/** The Constant lAMBDA_SFM. */
	public final static String lAMBDA_SFM = "lambda_SFM";

	/** The Constant N_SFM. */
	public final static String N_SFM = "n_SFM";

	/** The Constant N_PRIME_SFM. */
	public final static String N_PRIME_SFM = "n_prime_SFM";

	/** The Constant CURRENT_TARGET_GEOM. */
	public final static String CURRENT_TARGET_GEOM = "current_waypoint_geom";

	/** The Constant CURRENT_INDEX. */
	public final static String CURRENT_INDEX = "current_index";

	/** The Constant FINAL_TARGET. */
	public final static String FINAL_TARGET = "final_waypoint";

	/** The Constant CURRENT_PATH. */
	public final static String CURRENT_PATH = "current_path";

	/** The Constant PEDESTRIAN_GRAPH. */
	public final static String PEDESTRIAN_GRAPH = "pedestrian_graph";

	/** The Constant TOLERANCE_TARGET. */
	public final static String TOLERANCE_TARGET = "tolerance_waypoint";

	/** The Constant USE_GEOMETRY_TARGET. */
	public final static String USE_GEOMETRY_TARGET = "use_geometry_waypoint";

	/** The Constant COMPUTE_VIRTUAL_PATH. */
	// ACTION
	public final static String COMPUTE_VIRTUAL_PATH = "compute_virtual_path";

	/** The Constant WALK. */
	public final static String WALK = "walk";

	/** The Constant WALK_TO. */
	public final static String WALK_TO = "walk_to";
	// ---------- VARIABLES GETTER AND SETTER ------------- //

	/** The Constant TARGETS. */
	public final static String TARGETS = "waypoints";

	/** The Constant ROADS_TARGET. */
	public final static String ROADS_TARGET = "roads_waypoints";

	/**
	 * Gets the shoulder length.
	 *
	 * @param agent
	 *            the agent
	 * @return the shoulder length
	 */
	@getter (SHOULDER_LENGTH)
	public double getShoulderLength(final IAgent agent) {
		return (Double) agent.getAttribute(SHOULDER_LENGTH);
	}

	/**
	 * Gets the forces.
	 *
	 * @param agent
	 *            the agent
	 * @return the forces
	 */
	@SuppressWarnings ("unchecked")
	@getter (FORCES)
	public IMap<IShape, GamaPoint> getForces(final IAgent agent) {
		return (IMap<IShape, GamaPoint>) agent.getAttribute(FORCES);
	}

	/**
	 * Sets the shoulder length.
	 *
	 * @param agent
	 *            the agent
	 * @param s
	 *            the s
	 */
	@setter (SHOULDER_LENGTH)
	public void setShoulderLength(final IAgent agent, final double s) {
		agent.setAttribute(SHOULDER_LENGTH, s);
	}

	/**
	 * Gets the min dist.
	 *
	 * @param agent
	 *            the agent
	 * @return the min dist
	 */
	@getter (MINIMAL_DISTANCE)
	public double getMinDist(final IAgent agent) {
		return (Double) agent.getAttribute(MINIMAL_DISTANCE);
	}

	/**
	 * Sets the min dist.
	 *
	 * @param agent
	 *            the agent
	 * @param s
	 *            the s
	 */
	@setter (MINIMAL_DISTANCE)
	public void setMinDist(final IAgent agent, final double s) {
		agent.setAttribute(MINIMAL_DISTANCE, s);
	}

	/**
	 * Gets the ksfm.
	 *
	 * @param agent
	 *            the agent
	 * @return the ksfm
	 */
	@getter (K_SFM)
	public double getKSFM(final IAgent agent) {
		return (Double) agent.getAttribute(K_SFM);
	}

	/**
	 * Sets the KSFM.
	 *
	 * @param agent
	 *            the agent
	 * @param s
	 *            the s
	 */
	@setter (K_SFM)
	public void setKSFM(final IAgent agent, final double s) {
		agent.setAttribute(K_SFM, s);
	}

	/**
	 * Gets the kappa SFM.
	 *
	 * @param agent
	 *            the agent
	 * @return the kappa SFM
	 */
	@getter (KAPPA_SFM)
	public double getKappaSFM(final IAgent agent) {
		return (Double) agent.getAttribute(KAPPA_SFM);
	}

	/**
	 * Sets the kappa SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param s
	 *            the s
	 */
	@setter (KAPPA_SFM)
	public void setKappaSFM(final IAgent agent, final double s) {
		agent.setAttribute(KAPPA_SFM, s);
	}

	/**
	 * Sets the N PRIM E SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (N_PRIME_SFM)
	public void setN_PRIME_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(N_PRIME_SFM, val);
	}

	/**
	 * Gets the n prime sfm.
	 *
	 * @param agent
	 *            the agent
	 * @return the n prime sfm
	 */
	@getter (N_PRIME_SFM)
	public Double getN_PRIME_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(N_PRIME_SFM);
	}

	/**
	 * Sets the N SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (N_SFM)
	public void setN_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(N_SFM, val);
	}

	/**
	 * Gets the n sfm.
	 *
	 * @param agent
	 *            the agent
	 * @return the n sfm
	 */
	@getter (N_SFM)
	public Double getN_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(N_SFM);
	}

	/**
	 * Gets the obstacle species.
	 *
	 * @param agent
	 *            the agent
	 * @return the obstacle species
	 */
	@SuppressWarnings ("unchecked")
	@getter (OBSTACLE_SPECIES)
	public GamaList<ISpecies> getObstacleSpecies(final IAgent agent) {
		return (GamaList<ISpecies>) agent.getAttribute(OBSTACLE_SPECIES);
	}

	/**
	 * Sets the obstacle species.
	 *
	 * @param agent
	 *            the agent
	 * @param os
	 *            the os
	 */
	@setter (OBSTACLE_SPECIES)
	public void setObstacleSpecies(final IAgent agent, final GamaList<ISpecies> os) {
		agent.setAttribute(OBSTACLE_SPECIES, os);
	}

	/**
	 * Gets the pedestrian species.
	 *
	 * @param agent
	 *            the agent
	 * @return the pedestrian species
	 */
	@SuppressWarnings ("unchecked")
	@getter (PEDESTRIAN_SPECIES)
	public GamaList<ISpecies> getPedestrianSpecies(final IAgent agent) {
		return (GamaList<ISpecies>) agent.getAttribute(PEDESTRIAN_SPECIES);
	}

	/**
	 * Sets the pedestrian species.
	 *
	 * @param agent
	 *            the agent
	 * @param os
	 *            the os
	 */
	@setter (PEDESTRIAN_SPECIES)
	public void setPedestrianSpecies(final IAgent agent, final GamaList<ISpecies> os) {
		agent.setAttribute(PEDESTRIAN_SPECIES, os);
	}

	/**
	 * Gets the current target.
	 *
	 * @param agent
	 *            the agent
	 * @return the current target
	 */
	@getter (CURRENT_TARGET)
	public IShape getCurrentTarget(final IAgent agent) {
		return (IShape) agent.getAttribute(CURRENT_TARGET);
	}

	/**
	 * Sets the current target.
	 *
	 * @param agent
	 *            the agent
	 * @param point
	 *            the point
	 */
	@setter (CURRENT_TARGET)
	public void setCurrentTarget(final IAgent agent, final IShape point) {
		agent.setAttribute(CURRENT_TARGET, point);
	}

	/**
	 * Gets the obstacle consideration distance.
	 *
	 * @param agent
	 *            the agent
	 * @return the obstacle consideration distance
	 */
	@getter (OBSTACLE_CONSIDERATION_DISTANCE)
	public Double getObstacleConsiderationDistance(final IAgent agent) {
		return (Double) agent.getAttribute(OBSTACLE_CONSIDERATION_DISTANCE);
	}

	/**
	 * Sets the obstacle consideration distance.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (OBSTACLE_CONSIDERATION_DISTANCE)
	public void setObstacleConsiderationDistance(final IAgent agent, final Double val) {
		agent.setAttribute(OBSTACLE_CONSIDERATION_DISTANCE, val);
	}

	/**
	 * Gets the pedestrian consideration distance.
	 *
	 * @param agent
	 *            the agent
	 * @return the pedestrian consideration distance
	 */
	@getter (PEDESTRIAN_CONSIDERATION_DISTANCE)
	public Double getPedestrianConsiderationDistance(final IAgent agent) {
		return (Double) agent.getAttribute(PEDESTRIAN_CONSIDERATION_DISTANCE);
	}

	/**
	 * Sets the pedestrian consideration distance.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (PEDESTRIAN_CONSIDERATION_DISTANCE)
	public void setPedestrianConsiderationDistance(final IAgent agent, final Double val) {
		agent.setAttribute(PEDESTRIAN_CONSIDERATION_DISTANCE, val);
	}

	/**
	 * Gets the proba detour.
	 *
	 * @param agent
	 *            the agent
	 * @return the proba detour
	 */
	@getter (PROBA_DETOUR)
	public Double getProbaDetour(final IAgent agent) {
		return (Double) agent.getAttribute(PROBA_DETOUR);
	}

	/**
	 * Setl AMBD A SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (lAMBDA_SFM)
	public void setlAMBDA_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(lAMBDA_SFM, val);
	}

	/**
	 * Gets the l AMBD A SFM.
	 *
	 * @param agent
	 *            the agent
	 * @return the l AMBD A SFM
	 */
	@getter (lAMBDA_SFM)
	public Double getlAMBDA_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(lAMBDA_SFM);
	}

	/**
	 * Sets the GAM A SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (GAMA_SFM)
	public void setGAMA_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(GAMA_SFM, val);
	}

	/**
	 * Gets the gama sfm.
	 *
	 * @param agent
	 *            the agent
	 * @return the gama sfm
	 */
	@getter (GAMA_SFM)
	public Double getGAMA_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(GAMA_SFM);
	}

	/**
	 * Sets the proba detour.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (PROBA_DETOUR)
	public void setProbaDetour(final IAgent agent, final Double val) {
		agent.setAttribute(PROBA_DETOUR, val);
	}

	/**
	 * Gets the relaxion sfm.
	 *
	 * @param agent
	 *            the agent
	 * @return the relaxion sfm
	 */
	@getter (RELAXION_SFM)
	public Double getRELAXION_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(RELAXION_SFM);
	}

	/**
	 * Sets the RELAXIO N SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (RELAXION_SFM)
	public void setRELAXION_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(RELAXION_SFM, val);
	}

	/**
	 * Gets the a pedestrian SFM.
	 *
	 * @param agent
	 *            the agent
	 * @return the a pedestrian SFM
	 */
	@getter (A_PEDESTRIAN_SFM)
	public Double getAPedestrian_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(A_PEDESTRIAN_SFM);
	}

	/**
	 * Sets the A pedestrian SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (A_PEDESTRIAN_SFM)
	public void setAPedestrian_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(A_PEDESTRIAN_SFM, val);
	}

	/**
	 * Gets the a obst SFM.
	 *
	 * @param agent
	 *            the agent
	 * @return the a obst SFM
	 */
	@getter (A_OBSTACLES_SFM)
	public Double getAObstSFM(final IAgent agent) {
		return (Double) agent.getAttribute(A_OBSTACLES_SFM);
	}

	/**
	 * Sets the A obst SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (A_OBSTACLES_SFM)
	public void setAObstSFM(final IAgent agent, final Double val) {
		agent.setAttribute(A_OBSTACLES_SFM, val);
	}

	/**
	 * Gets the b sfm.
	 *
	 * @param agent
	 *            the agent
	 * @return the b sfm
	 */
	@getter (B_PEDESTRIAN_SFM)
	public Double getB_SFM(final IAgent agent) {
		return (Double) agent.getAttribute(B_PEDESTRIAN_SFM);
	}

	/**
	 * Sets the B SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (B_PEDESTRIAN_SFM)
	public void setB_SFM(final IAgent agent, final Double val) {
		agent.setAttribute(B_PEDESTRIAN_SFM, val);
	}

	/**
	 * Gets the b obst SFM.
	 *
	 * @param agent
	 *            the agent
	 * @return the b obst SFM
	 */
	@getter (B_OBSTACLES_SFM)
	public Double getBObstSFM(final IAgent agent) {
		return (Double) agent.getAttribute(B_OBSTACLES_SFM);
	}

	/**
	 * Sets the B obst SFM.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (B_OBSTACLES_SFM)
	public void setBObstSFM(final IAgent agent, final Double val) {
		agent.setAttribute(B_OBSTACLES_SFM, val);
	}

	/**
	 * Gets the avoid other.
	 *
	 * @param agent
	 *            the agent
	 * @return the avoid other
	 */
	@getter (AVOID_OTHER)
	public Boolean getAvoidOther(final IAgent agent) {
		return (Boolean) agent.getAttribute(AVOID_OTHER);
	}

	/**
	 * Sets the avoid other.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AVOID_OTHER)
	public void setAvoidOther(final IAgent agent, final Boolean val) {
		agent.setAttribute(AVOID_OTHER, val);
	}

	/**
	 * Sets the velocity.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (VELOCITY)
	public void setVelocity(final IAgent agent, final GamaPoint val) {
		agent.setAttribute(VELOCITY, val);
	}

	/**
	 * Gets the velocity.
	 *
	 * @param agent
	 *            the agent
	 * @return the velocity
	 */
	@getter (VELOCITY)
	public GamaPoint getVelocity(final IAgent agent) {
		return (GamaPoint) agent.getAttribute(VELOCITY);
	}

	/**
	 * Gets the tolerance target.
	 *
	 * @param agent
	 *            the agent
	 * @return the tolerance target
	 */
	@getter (TOLERANCE_TARGET)
	public double getToleranceTarget(final IAgent agent) {
		return (Double) agent.getAttribute(TOLERANCE_TARGET);
	}

	/**
	 * Sets the tolerance target.
	 *
	 * @param agent
	 *            the agent
	 * @param s
	 *            the s
	 */
	@setter (TOLERANCE_TARGET)
	public void setToleranceTarget(final IAgent agent, final double s) {
		agent.setAttribute(TOLERANCE_TARGET, s);
	}

	/**
	 * Gets the targets.
	 *
	 * @param agent
	 *            the agent
	 * @return the targets
	 */
	@SuppressWarnings ("unchecked")
	@getter (TARGETS)
	public IList<IShape> getTargets(final IAgent agent) {
		return (IList<IShape>) agent.getAttribute(TARGETS);
	}

	/**
	 * Gets the roads targets.
	 *
	 * @param agent
	 *            the agent
	 * @return the roads targets
	 */
	@getter (ROADS_TARGET)
	public IMap getRoadsTargets(final IAgent agent) {
		return (IMap) agent.getAttribute(ROADS_TARGET);
	}

	/**
	 * Sets the targets.
	 *
	 * @param agent
	 *            the agent
	 * @param points
	 *            the points
	 */
	@setter (TARGETS)
	public void setTargets(final IAgent agent, final IList<IShape> points) {
		agent.setAttribute(TARGETS, points);
	}

	/**
	 * Gets the final target.
	 *
	 * @param agent
	 *            the agent
	 * @return the final target
	 */
	@getter (FINAL_TARGET)
	public IShape getFinalTarget(final IAgent agent) {
		return (IShape) agent.getAttribute(FINAL_TARGET);
	}

	/**
	 * Sets the final target.
	 *
	 * @param agent
	 *            the agent
	 * @param point
	 *            the point
	 */
	@setter (FINAL_TARGET)
	public void setFinalTarget(final IAgent agent, final IShape point) {
		agent.setAttribute(FINAL_TARGET, point);
	}

	/**
	 * Gets the current index.
	 *
	 * @param agent
	 *            the agent
	 * @return the current index
	 */
	@getter (CURRENT_INDEX)
	public Integer getCurrentIndex(final IAgent agent) {
		return (Integer) agent.getAttribute(CURRENT_INDEX);
	}

	/**
	 * Sets the current index.
	 *
	 * @param agent
	 *            the agent
	 * @param index
	 *            the index
	 */
	@setter (CURRENT_INDEX)
	public void setCurrentIndex(final IAgent agent, final Integer index) {
		agent.setAttribute(CURRENT_INDEX, index);
	}

	/**
	 * Gets the use geometry target.
	 *
	 * @param agent
	 *            the agent
	 * @return the use geometry target
	 */
	@getter (USE_GEOMETRY_TARGET)
	public Boolean getUseGeometryTarget(final IAgent agent) {
		return (Boolean) agent.getAttribute(USE_GEOMETRY_TARGET);
	}

	/**
	 * Sets the use geometry target.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (USE_GEOMETRY_TARGET)
	public void setUseGeometryTarget(final IAgent agent, final Boolean val) {
		agent.setAttribute(USE_GEOMETRY_TARGET, val);
	}

	/**
	 * Gets the pedestrian model.
	 *
	 * @param agent
	 *            the agent
	 * @return the pedestrian model
	 */
	@getter (PEDESTRIAN_MODEL)
	public String getPedestrianModel(final IAgent agent) {
		return (String) agent.getAttribute(PEDESTRIAN_MODEL);
	}

	/**
	 * Sets the pedestrian model.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (PEDESTRIAN_MODEL)
	public void setPedestrianModel(final IAgent agent, final String val) {
		if (!"advanced".equals(val) && !"simple".equals(val)) throw GamaRuntimeException.error(
				"" + val + " is not a possible value for pedestrian model; possible values: ['simple', 'advanced']",
				agent.getScope());
		agent.setAttribute(PEDESTRIAN_MODEL, val);
	}

	// ----------------------------------- //

	/**
	 * Prim walk to.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = WALK_TO,
			args = { @arg (
					name = "target",
					type = IType.GEOMETRY,
					optional = false,
					doc = @doc ("Move toward the target using the SFM model")),
					@arg (
							name = IKeyword.BOUNDS,
							type = IType.GEOMETRY,
							optional = true,
							doc = @doc ("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry")),

			},

			doc = @doc (
					value = "action to walk toward a target",
					examples = { @example ("do walk_to {10,10};") }))
	public boolean primWalkTo(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		if (agent == null || agent.dead()) return false;
		IShape goal = computeTarget(scope, agent);
		if (goal == null) return false;
		IShape bounds = null;
		if (scope.hasArg(IKeyword.BOUNDS)) {
			final Object obj = scope.getArg(IKeyword.BOUNDS, IType.NONE);
			bounds = GamaGeometryType.staticCast(scope, obj, null, false);
		}
		IList<ISpecies> speciesList = getObstacleSpecies(agent);
		IContainer<Integer, IAgent> obstacles = null;
		if (speciesList.size() == 1) {
			obstacles = speciesList.get(0);
		} else {
			obstacles = GamaListFactory.create(Types.AGENT);
			for (ISpecies species : speciesList) {

				((IList<IAgent>) obstacles).addAll(Cast.asList(scope, species));
			}
		}

		speciesList = getPedestrianSpecies(agent);
		IContainer<Integer, IAgent> pedestrians = null;
		if (speciesList.size() == 1) {
			pedestrians = speciesList.get(0);
		} else {
			pedestrians = GamaListFactory.create(Types.AGENT);
			for (ISpecies species : speciesList) {

				((IList<IAgent>) pedestrians).addAll(Cast.asList(scope, species));
			}
		}

		GamaPoint currentTarget = goal.getLocation();
		double maxDist = computeDistance(scope, agent);
		double realSpeed = walkWithForceModel(scope, agent, currentTarget, getAvoidOther(agent), bounds, pedestrians,
				obstacles, maxDist);

		setRealSpeed(agent, realSpeed);
		return true;
	}

	/**
	 * General walking dynamic with force based avoidance
	 *
	 * @param scope
	 * @param agent
	 * @param currentTarget
	 * @param avoidOther
	 * @param bounds
	 * @param pedestrianList
	 * @param obstaclesList
	 * @param maxDist
	 * @return
	 */
	public double walkWithForceModel(final IScope scope, final IAgent agent, final IShape currentTarget,
			final boolean avoidOther, final IShape bounds, final IContainer<Integer, ?> pedestrianList,
			final IContainer<Integer, ?> obstaclesList, final double maxDist) {
		GamaPoint location = getLocation(agent).copy(scope);
		GamaPoint target = currentTarget.isPoint() ? currentTarget.getLocation()
				: SpatialPunctal._closest_point_to(location, currentTarget);
		target.setZ(target.z);
		double dist = location.distance(target);

		if (dist == 0.0) return 0.0;

		if (!currentTarget.isPoint() && bounds != null && getCurrentEdge(agent) != null) {
			IList<IShape> pts = GamaListFactory.create();
			pts.add(agent.getLocation());
			pts.add(target);
			IShape line = SpatialCreation.line(scope, pts);
			if (!bounds.covers(line)) {
				target = SpatialPunctal._closest_point_to(target, getCurrentEdge(agent));
				pts.clear();
				pts.add(agent.getLocation());
				pts.add(target);
				line = SpatialCreation.line(scope, pts);
				if (!bounds.covers(line)) { target = SpatialPunctal._closest_point_to(location, getCurrentEdge(agent)); }

			}
		}

		GamaPoint velocity = null;
		if (avoidOther) {
			double distPercep = Math.max(maxDist, getPedestrianConsiderationDistance(agent));
			double distPercepObst = Math.max(maxDist, getObstacleConsiderationDistance(agent));
			if ("simple".equals(getPedestrianModel(agent))) {
				velocity = avoidSFMSimple(scope, agent, location, target, distPercep, distPercepObst, pedestrianList,
						obstaclesList);

			} else {
				velocity = avoidSFM(scope, agent, location, target, distPercep, distPercepObst, pedestrianList,
						obstaclesList);

			}
			velocity = velocity.multiplyBy(dist);
		} else {
			velocity = target.copy(scope).minus(location);
		}

		GamaPoint tar = velocity.copy(scope).add(location);
		double distToTarget = location.euclidianDistanceTo(tar);
		if (distToTarget > 0.0) {
			double coeff = Math.min(maxDist / distToTarget, 1.0);
			if (coeff == 1.0) {
				location = tar;
			} else {
				velocity = velocity.multiplyBy(coeff);
				location = location.add(velocity);
			}
		}

		if (bounds != null && !SpatialProperties.overlaps(scope, location, bounds)) {
			location = SpatialPunctal.closest_points_with(location, bounds).get(1);
		}
		double realSpeed = 0.0;
		double proba_detour = getProbaDetour(agent);
		if (!Random.opFlip(scope, 1.0 - proba_detour)
				|| location.euclidianDistanceTo(target) <= agent.getLocation().euclidianDistanceTo(target)) {
			realSpeed = agent.euclidianDistanceTo(location) / scope.getSimulation().getTimeStep(scope);
			setVelocity(agent, location.copy(scope).minus(getLocation(agent)));
			setLocation(agent, location);
		} else {
			setVelocity(agent, new GamaPoint(0, 0, 0));
		}

		return realSpeed;
	}

	/**
	 * Classical implementation of the Social Force Model (Helbing and Molnar, 1998)
	 *
	 * @param scope
	 * @param agent
	 * @param location
	 * @param currentTarget
	 * @param distPercep
	 * @param obstaclesList
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	public GamaPoint avoidSFMSimple(final IScope scope, final IAgent agent, final GamaPoint location,
			final GamaPoint currentTarget, final double distPercepPedestrian, final double distPercepObstacle,
			final IContainer pedestriansList, final IContainer obstaclesList) {
		IMap<IShape, GamaPoint> forcesMap = GamaMapFactory.create();

		GamaPoint current_velocity = getVelocity(agent).copy(scope);
		GamaPoint fsoc = new GamaPoint(0, 0, 0);
		double dist = location.euclidianDistanceTo(currentTarget);
		IList<IAgent> obstacles = GamaListFactory.create(Types.AGENT);
		IList<IAgent> pedestrians = GamaListFactory.create(Types.AGENT);

		pedestrians.addAll(SpatialQueries.at_distance(scope, pedestriansList, distPercepPedestrian));
		obstacles.addAll(SpatialQueries.at_distance(scope, obstaclesList, distPercepObstacle));

		pedestrians.remove(agent);
		pedestrians.removeIf(IAgent::dead);
		double lambda = getlAMBDA_SFM(agent);
		double gama_ = getGAMA_SFM(agent);
		double A = getAPedestrian_SFM(agent);
		double n = getN_SFM(agent);
		double n_prime = getN_PRIME_SFM(agent);
		for (IAgent ag : pedestrians) {
			GamaPoint force = new GamaPoint(0, 0, 0);
			double distance = agent.euclidianDistanceTo(ag);
			GamaPoint itoj = Points.subtract(ag.getLocation(), agent.getLocation());
			itoj = itoj.divideBy(Maths.sqrt(scope, itoj.x * itoj.x + itoj.y * itoj.y + itoj.z * itoj.z));

			GamaPoint D = current_velocity.copy(scope).subtract(getVelocity(ag)).multiplyBy(lambda).add(itoj);
			double D_norm = Maths.sqrt(scope, D.x * D.x + D.y * D.y + D.z * D.z);
			double B = gama_ * D_norm;
			GamaPoint t_ = D.divideBy(D_norm);
			GamaPoint n_;
			if (t_.x == 0) {
				n_ = new GamaPoint(t_.y > 0 ? -1 : 1, 0, 0);
			} else if (t_.y == 0) {
				n_ = new GamaPoint(0, t_.x > 0 ? 1 : -1, 0);
			} else {
				double nx = -t_.y / t_.x;
				double norm = Math.sqrt(nx * nx + 1);
				n_ = t_.x > 0 ? new GamaPoint(-nx / norm, -1 / norm, 0) : new GamaPoint(nx / norm, 1 / norm, 0);
			}
			double t_xDotitoj = t_.x * itoj.x + t_.y * itoj.y + t_.z * itoj.z;
			t_xDotitoj = Math.max(Math.min(t_xDotitoj, 1.0), -1.0);
			double teta = Math.abs(Maths.acos(t_xDotitoj) * Math.PI / 180);
			if (teta <= Math.PI) {
				GamaPoint f_1 = t_.multiplyBy(Math.exp(-Math.pow(n_prime * B * teta, 2)));
				GamaPoint f_2 = n_.multiplyBy(Math.exp(-Math.pow(n * B * teta, 2)));
				force = f_1.add(f_2).multiplyBy(-A * Math.exp(-distance / B));
				fsoc = fsoc.add(force);
			}

			forcesMap.put(ag, force);
		}

		GamaPoint desiredVelo = currentTarget.copy(scope).minus(location)
				.divideBy(dist / Math.min(getSpeed(agent), dist / scope.getSimulation().getClock().getStepInSeconds()));
		GamaPoint fdest = desiredVelo.minus(current_velocity).dividedBy(getRELAXION_SFM(agent));

		forcesMap.put(agent, fdest);
		agent.setAttribute(FORCES, forcesMap);
		GamaPoint forces = fdest.add(fsoc);
		return current_velocity.add(forces).normalize();
	}

	/**
	 * Avoid SFM.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param location
	 *            the location
	 * @param currentTarget
	 *            the current target
	 * @param distPercepPedestrian
	 *            the dist percep pedestrian
	 * @param distPercepObstacle
	 *            the dist percep obstacle
	 * @param pedestriansList
	 *            the pedestrians list
	 * @param obstaclesList
	 *            the obstacles list
	 * @return the gama point
	 */
	@SuppressWarnings ("unchecked")
	public GamaPoint avoidSFM(final IScope scope, final IAgent agent, final GamaPoint location,
			final GamaPoint currentTarget, final double distPercepPedestrian, final double distPercepObstacle,
			final IContainer pedestriansList, final IContainer obstaclesList) {
		GamaPoint current_velocity = getVelocity(agent).copy(scope);
		double BWall = getBObstSFM(agent);
		double Bpedestrian = getB_SFM(agent);
		Double distMin = getMinDist(agent);
		double shoulderL = getShoulderLength(agent) / 2.0 + distMin;
		IMap<IShape, GamaPoint> forcesMap = GamaMapFactory.create();
		double dist = location.euclidianDistanceTo(currentTarget);
		if (dist == 0 || getSpeed(agent) <= 0.0) return new GamaPoint(0, 0, 0);
		IList<IAgent> obstacles = GamaListFactory.create(Types.AGENT);
		IList<IAgent> pedestrians = GamaListFactory.create(Types.AGENT);

		obstacles.addAll(SpatialQueries.at_distance(scope, obstaclesList, distPercepObstacle));
		pedestrians.addAll(SpatialQueries.at_distance(scope, pedestriansList, distPercepPedestrian));

		obstacles.remove(agent);
		obstacles.removeIf(IAgent::dead);
		double lambda = getlAMBDA_SFM(agent);
		double gama_ = getGAMA_SFM(agent);
		double APedes = getAPedestrian_SFM(agent);
		double AWall = getAObstSFM(agent);

		double k = getKSFM(agent);
		double kappa = getKappaSFM(agent);
		GamaPoint ei = current_velocity.copy(scope).normalize();

		GamaPoint desiredVelo = currentTarget.copy(scope).minus(location)
				.divideBy(dist / Math.min(getSpeed(agent), dist / scope.getSimulation().getClock().getStepInSeconds()));
		GamaPoint fdest = desiredVelo.minus(current_velocity).dividedBy(getRELAXION_SFM(agent));

		if (ei.equals(new GamaPoint())) { ei = fdest; }
		GamaPoint forcesPedestrian = new GamaPoint();
		for (IAgent ag : pedestrians) {
			double distance = agent.getLocation().euclidianDistanceTo(ag.getLocation());
			GamaPoint force = new GamaPoint();
			if (distance > 0) {
				double fact = APedes * Math.exp((shoulderL + getShoulderLength(ag) / 2.0 - distance) / Bpedestrian);

				GamaPoint nij = Points.subtract(agent.getLocation(), ag.getLocation());
				nij = nij.dividedBy(distance);
				double phi = SpatialPunctal.angleInDegreesBetween(scope, new GamaPoint(), ei, nij.copy(scope).multiplyBy(-1));
				GamaPoint fnorm = nij.multiplyBy(fact * (lambda + (1 - lambda) * (1 + Math.cos(phi)) / 2.0));

				GamaPoint tij = new GamaPoint(-1 * nij.y, nij.x);
				GamaPoint ej = getVelocity(ag).copy(scope).normalize();
				double phiij = GamaPoint.dotProduct(ei, ej);
				GamaPoint ftang = phiij <= 0 ? tij.multiplyBy(gama_ * fnorm.norm()) : new GamaPoint(0, 0, 0);
				GamaPoint fsoc = fnorm.add(ftang);
				force = fsoc.copy(scope);

				double omega = shoulderL + getShoulderLength(ag) / 2.0 - distance;

				if (omega > 0) {
					GamaPoint fphys = new GamaPoint();
					fphys = fphys.add(nij.copy(scope).multiplyBy(omega * k));
					double deltaSpeed = GamaPoint.dotProduct(getVelocity(ag).copy(scope).minus(current_velocity), tij);
					fphys = fphys.add(tij.copy(scope).multiplyBy(omega * kappa * deltaSpeed));
					force = force.add(fphys);
				}
			}
			forcesPedestrian = forcesPedestrian.add(force);
			forcesMap.put(ag, force);

		}

		GamaPoint forcesWall = new GamaPoint();
		for (IAgent ag : obstacles) {
			double distance = agent.euclidianDistanceTo(ag);
			GamaPoint closest_point = null;
			GamaPoint fwall = new GamaPoint();

			if (distance == 0) {
				closest_point = SpatialPunctal._closest_point_to(agent.getLocation(), ag.getGeometry().getExteriorRing(scope));
			} else {
				closest_point = SpatialPunctal._closest_point_to(agent.getLocation(), ag);
			}

			if (distance > 0) {
				double fact = AWall * Math.exp((shoulderL - distance) / BWall);
				double omega = shoulderL - distance;
				if (omega > 0) { fact += k * omega; }
				GamaPoint nij = Points.subtract(agent.getLocation(), closest_point.getLocation());
				nij = nij.normalize();
				fwall = nij.multiplyBy(fact);

				if (omega > 0) {
					GamaPoint tij = new GamaPoint(-1 * nij.y, nij.x);
					double product = GamaPoint.dotProduct(current_velocity, tij);

					fwall = fwall.minus(tij.multiplyBy(omega * kappa * product));
				}
			}

			forcesWall.add(fwall);
			forcesMap.put(ag, fwall);

		}

		forcesMap.put(agent, fdest);
		agent.setAttribute(FORCES, forcesMap);
		GamaPoint forces = fdest.add(forcesPedestrian).add(forcesWall);
		return current_velocity.add(forces).normalize();
	}

	/**
	 * Prim compute virtual path.
	 *
	 * @param scope
	 *            the scope
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = COMPUTE_VIRTUAL_PATH,
			args = { @arg (
					name = PEDESTRIAN_GRAPH,
					type = IType.GRAPH,
					optional = false,
					doc = @doc ("the graph on wich compute the path")),
					@arg (
							name = "target",
							type = IType.GEOMETRY,
							optional = false,
							doc = @doc ("the target to reach, can be any agent")) },

			doc = @doc (
					value = "action to compute a path to a location according to a given graph",
					returns = "the computed path, return nil if no path can be taken",
					examples = { @example ("do compute_virtual_path graph: pedestrian_network target: any_point;") }))
	public IPath primComputeVirtualPath(final IScope scope) throws GamaRuntimeException {
		IPath thePath = null;

		final ISpatialGraph graph = (ISpatialGraph) scope.getArg(PEDESTRIAN_GRAPH, IType.GRAPH);
		final IAgent agent = getCurrentAgent(scope);
		final boolean useGeometryTarget = getUseGeometryTarget(agent);
		IShape target = (IShape) scope.getArg("target", IType.GEOMETRY);
		IShape source = agent.getLocation();

		thePath = ((GraphTopology) graph.getTopology(scope)).pathBetween(scope, source, target);
		// If there is no path between source and target ...
		if (thePath == null) return thePath;
		IMap<IShape, IShape> roadTarget = GamaMapFactory.create();
		IList<IShape> targets = GamaListFactory.create();
		IList<IShape> segments = thePath.getEdgeGeometry();

		for (int i = 0; i < segments.size(); i++) {
			IShape cSeg = segments.get(i);
			IShape cRoad = thePath.getRealObject(cSeg);
			IMap<IAgent, IShape> map = PedestrianRoadSkill.getConnectedSegmentsIntersection((IAgent) cRoad);

			IShape geom = null, cRoadNext = null, geomNext = null;
			if (useGeometryTarget) {
				geom = PedestrianRoadSkill.getFreeSpace(cRoad.getAgent());
				if (i < segments.size() - 1) {
					cRoadNext = thePath.getRealObject(segments.get(i + 1));
					geomNext = PedestrianRoadSkill.getFreeSpace(cRoadNext.getAgent());
				}
			}

			IList<GamaPoint> points = cSeg.getPoints();
			for (int j = 1; j < points.size(); j++) {
				GamaPoint pt = points.get(j);
				IShape cTarget = null;
				if (PedestrianRoadSkill.getRoadStatus(scope, cRoad) == PedestrianRoadSkill.SIMPLE_STATUS) {
					cTarget = pt;
				} else {
					cTarget = pt;
					//if (cTarget == null) { cTarget = pt; } //TODO:why ?
				}
				if (useGeometryTarget) {
					cTarget = null;
					if (geomNext != null) {
						if (map != null && map.contains(scope, cRoadNext)) {

							cTarget = map.get(cRoadNext);
						} else {

							cTarget = SpatialOperators.inter(scope, geom, geomNext);
						}
					}
					if (cTarget == null) { cTarget = pt; }
				}
				targets.add(cTarget);

				roadTarget.put(cTarget, cRoad);
			}
		}
		IShape targ = targets.get(0);
		IAgent road = (IAgent) roadTarget.get(targ);
		if (road != null) { PedestrianRoadSkill.register(scope, road, agent); }

		if (!targets.get(0).getLocation().equals(agent.getLocation())) {
			if (road == null) {
				agent.setLocation(targets.get(0).getLocation());
			} else {
				IShape fS = PedestrianRoadSkill.getFreeSpace(road);
				if (!fS.intersects(agent.getLocation())) {
					agent.setLocation(SpatialPunctal._closest_point_to(agent.getLocation(), fS));
				}
			}
		}

		agent.setAttribute(ROADS_TARGET, roadTarget);
		setCurrentIndex(agent, 0);
		setTargets(agent, targets);

		setFinalTarget(agent, target);
		setCurrentTarget(agent, targ);

		agent.setAttribute(CURRENT_PATH, thePath);
		return thePath;
	}

	/**
	 * Prim arrived at destination.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "release_path",
			args = { @arg (
					name = "current_road",
					type = IType.AGENT,
					optional = true,
					doc = @doc ("current road on which the agent is located (can be nil)")), },
			doc = @doc (
					value = "clean all the interne state of the agent"))
	public boolean primArrivedAtDestination(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		IAgent road = (IAgent) scope.getArg("current_road", IType.AGENT);
		setCurrentIndex(agent, 0);
		setCurrentTarget(agent, null);
		setTargets(agent, GamaListFactory.create());
		setFinalTarget(agent, null);
		setCurrentPath(agent, null);
		setCurrentEdge(agent, (IShape) null);
		setRealSpeed(agent, 0.0);
		if (road != null) return PedestrianRoadSkill.unregister(scope, road, agent);
		return false;
	}

	/**
	 * Prim walk.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("unchecked")
	@action (
			name = WALK,
			doc = @doc (
					value = "action to walk toward the final target using the current_path (requires to use the "
							+ COMPUTE_VIRTUAL_PATH + " action before)",
					examples = { @example ("do walk;") }))
	public boolean primWalk(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		if (agent == null || agent.dead()) return false;
		final IShape finalTarget = getFinalTarget(agent);
		if (finalTarget == null) return false;

		final IList<IShape> targets = getTargets(agent);
		if (targets == null || targets.isEmpty()) return false;

		GamaPoint location = getLocation(agent).copy(scope);
		double maxDist = computeDistance(scope, agent);

		boolean movement = true;
		int maxIndex = targets.size() - 1;
		while (movement) {

			movement = false;
			int index = getCurrentIndex(agent);
			IShape currentTarget = getCurrentTarget(agent);
			IAgent road = (IAgent) getRoadsTargets(agent).get(currentTarget);

			IShape bounds = null;
			boolean avoidOther = getAvoidOther(agent);

			if (road != null) {

				avoidOther = PedestrianRoadSkill.getRoadStatus(scope, road) == PedestrianRoadSkill.SIMPLE_STATUS ? false
						: avoidOther;
				bounds = PedestrianRoadSkill.getFreeSpace(scope, road);

			}

			IContainer<Integer, ?> pedestrians =
					road == null ? GamaListFactory.create() : PedestrianRoadSkill.getCloseAgents(road);
			IList<ISpecies> speciesList = getObstacleSpecies(agent);
			IContainer obstacles = null;

			// AD : recreating these lists everytime is really terrible. Better use MetaPopulations
			if (speciesList.size() == 1) {
				obstacles = speciesList.get(0);
			} else {
				obstacles = GamaListFactory.create(Types.AGENT);
				for (ISpecies species : speciesList) {
					((IList<IAgent>) obstacles).addAll(Cast.asList(scope, species));
				}
			}

			GamaPoint prevLoc = location.copy(scope);
			walkWithForceModel(scope, agent, currentTarget, avoidOther, bounds, pedestrians, obstacles, maxDist);
			location = agent.getLocation();

			if (arrivedAtTarget(scope, location, currentTarget, getToleranceTarget(agent), index, maxIndex, targets)) {
				if (road != null) { PedestrianRoadSkill.unregister(scope, road, agent); }

				if (index < maxIndex) {
					index++;

					setCurrentIndex(agent, index);
					setCurrentTarget(agent, targets.get(index));
					road = (IAgent) getRoadsTargets(agent).get(getCurrentTarget(agent));

					if (road != null) { PedestrianRoadSkill.register(scope, road, agent); }

					maxDist -= location.distance(prevLoc);
					if (maxDist > 0) { movement = true; }
				} else {
					final ISpecies context = agent.getSpecies();
					final IStatement.WithArgs actionTNR = context.getAction("release_path");
					final Arguments argsTNR = new Arguments();
					argsTNR.put("current_road", ConstantExpressionDescription.create(road));
					actionTNR.setRuntimeArgs(scope, argsTNR);

					actionTNR.executeOn(scope);

				}
			}
		}
		return true;
	}

	/**
	 * Arrived at target.
	 *
	 * @param scope
	 *            the scope
	 * @param location
	 *            the location
	 * @param currentTarget
	 *            the current target
	 * @param size
	 *            the size
	 * @param index
	 *            the index
	 * @param maxIndex
	 *            the max index
	 * @param targets
	 *            the targets
	 * @return true, if successful
	 */
	boolean arrivedAtTarget(final IScope scope, final GamaPoint location, final IShape currentTarget, final double size,
			final int index, final int maxIndex, final IList<IShape> targets) {
		double dist = location.euclidianDistanceTo(currentTarget);
		if (dist <= size) return true;
		return false;
	}

}
