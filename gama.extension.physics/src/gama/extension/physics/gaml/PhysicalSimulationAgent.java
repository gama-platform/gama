/*******************************************************************************************************
 *
 * PhysicalSimulationAgent.java, in gama.extension.physics, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.gaml;

import java.util.Collection;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.operator;
import gama.annotations.setter;
import gama.annotations.species;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.IList;
import gama.api.types.matrix.IField;
import gama.api.utils.collections.Collector;
import gama.api.utils.collections.Collector.AsOrderedSet;
import gama.core.simulation.SimulationAgent;
import gama.extension.physics.PhysicsActivator;
import gama.extension.physics.box2d_version.Box2DPhysicalWorld;
import gama.extension.physics.common.IJointDefinition;
import gama.extension.physics.common.IPhysicalConstants;
import gama.extension.physics.common.IPhysicalWorld;
import gama.extension.physics.java_version.BulletPhysicalWorld;
import gama.extension.physics.native_version.NativeBulletPhysicalWorld;

/**
 * The PhysicalSimulationAgent class serves as the main entry point for managing physical simulations in the GAMA
 * platform. It provides capabilities to register and manage agents with physical properties, such as static or dynamic
 * bodies, and integrates with various physics engines (e.g., Box2D, jBullet, Native Bullet).
 *
 * <p>
 * Key Features:
 * </p>
 * <ul>
 * <li>Supports 2D and 3D physics simulations using different libraries.</li>
 * <li>Allows registration and management of agents with physical properties.</li>
 * <li>Provides actions and operators to create and manipulate physical joints (e.g., hinge, slider, distance).</li>
 * <li>Enables customization of simulation parameters such as gravity, collision detection, and substeps.</li>
 * </ul>
 *
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * // Example: Registering agents in the physical world
 * do register([agent1, agent2]);
 *
 * // Example: Creating a hinge joint
 * GamaJoint hinge = create_hinge_joint(bodyA, bodyB, anchor, lowerLimit, upperLimit, motorSpeed, maxMotorForce);
 * </pre>
 *
 * <p>
 * Attributes:
 * </p>
 * <ul>
 * <li><b>gravity</b>: Defines the gravity vector applied to the physical world.</li>
 * <li><b>automated_registration</b>: Determines whether agents are automatically registered.</li>
 * <li><b>max_substeps</b>: Specifies the maximum number of substeps for the simulation engine.</li>
 * <li><b>use_native</b>: Indicates whether the native Bullet library is used.</li>
 * <li><b>library_name</b>: Specifies the physics library to use (e.g., Bullet, Box2D).</li>
 * </ul>
 *
 * <p>
 * Supported Joints:
 * </p>
 * <ul>
 * <li><b>Hinge Joint</b>: Rotational joint with optional limits and motor.</li>
 * <li><b>Slider Joint</b>: Linear joint with optional limits.</li>
 * <li><b>Ball-and-socket Joint</b>: Keeps two bodies attached at a shared anchor while allowing rotation.</li>
 * </ul>
 *
 * @see IPhysicalWorld
 * @see GamaJoint
 * @see IJointDefinition
 */
@species (
		name = IPhysicalConstants.PHYSICAL_WORLD,
		skills = IPhysicalConstants.STATIC_BODY)
@doc ("The base species for models that act as a 3D physical world. Can register and manage agents provided with either the '"
		+ IPhysicalConstants.STATIC_BODY + "' or '" + IPhysicalConstants.DYNAMIC_BODY + "' skill. Inherits from '"
		+ IPhysicalConstants.STATIC_BODY + "', so it can also act as a physical body itself (with a '"
		+ IPhysicalConstants.MASS + "', '" + IPhysicalConstants.FRICTION + "', '" + IPhysicalConstants.GRAVITY
		+ "'), of course without motion -- in this case, it needs to register itself as a physical agent using the '"
		+ IPhysicalConstants.REGISTER + "' action")
@vars ({ @variable (
		name = IPhysicalConstants.GRAVITY,
		type = IType.POINT,
		init = "{0,0,-9.80665}",
		doc = @doc ("Defines the value of gravity in this world. The default value is set to -9.80665 on the z-axis, that is 9.80665 m/s2 towards the 'bottom' of the world. Can be set to any direction and intensity and applies to all the bodies present in the physical world")),
		@variable (
				name = IPhysicalConstants.AUTOMATED_REGISTRATION,
				type = IType.BOOL,
				init = "true",
				doc = @doc ("If set to true (the default), makes the world automatically register and unregister agents provided with either the '"
						+ IPhysicalConstants.STATIC_BODY + "' or '" + IPhysicalConstants.DYNAMIC_BODY
						+ "' skill. Otherwise, they must be registered using the '" + IPhysicalConstants.REGISTER
						+ "' action, which can be useful when only some agents need to be considered as 'physical agents'. Note that, in any case, the world needs to manually register itself if it is supposed to act as a physical body. ")),
		@variable (
				name = IPhysicalConstants.MAX_SUBSTEPS,
				type = IType.INT,
				init = "0",
				doc = @doc ("If equal to 0 (the default), makes the simulation engine be stepped alongside the simulation (no substeps allowed). Otherwise, sets the maximum number of physical simulation substeps that may occur within one GAMA simulation step")),
		@variable (
				name = IPhysicalConstants.TERRAIN,
				type = IType.FIELD,
				doc = { @doc ("This attribute is a matrix of float that can be used to represent a 3D terrain. The shape of the world, in that case, should be a box, where the"
						+ "dimension on the z-axis is used to scale the z-values of the DEM. The world needs to be register itself as a physical object") }),
		@variable (
				name = IPhysicalConstants.USE_NATIVE,
				type = IType.BOOL,
				doc = { @doc ("This attribute allows to manually switch between the Java version of the Bullet library (JBullet, a modified version of https://github.com/stephengold/jbullet, which corresponds to version 2.72 of the original library) and the native Bullet library (Libbulletjme, https://github.com/stephengold/Libbulletjme, which is kept up-to-date with the 3.x branch of the original library)."
						+ "The native version is the default one unless the libraries cannot be loaded, making JBullet the default") }),
		@variable (
				name = IPhysicalConstants.LIBRARY_NAME,
				type = IType.STRING,
				doc = { @doc ("This attribute allows to manually switch between two physics library, named 'bullet' and 'box2D'. The Bullet library, which comes in two flavors (see 'use_native') and the Box2D libray in its Java version (https://github.com/jbox2d/jbox2d). "
						+ "Bullet is the default library but models in 2D should better use Box2D") }),

		@variable (
				name = IPhysicalConstants.ACCURATE_COLLISION_DETECTION,
				type = IType.BOOL,
				init = "false",
				doc = @doc ("Enables or not a better (but slower) collision detection ")) })
public class PhysicalSimulationAgent extends SimulationAgent implements IPhysicalConstants {

	/** The population listener. */
	final BodyPopulationListener populationListener = new BodyPopulationListener();

	/** The ccd. */
	Boolean ccd = false;

	/** The automated registration. */
	Boolean automatedRegistration = true;

	/** The gravity. */
	final IPoint gravity = GamaPointFactory.create(0, 0, -9.81d);

	/** The terrain. */
	IField terrain;

	/** The gateway. */
	IPhysicalWorld gateway;

	/** The use native library. */
	Boolean useNativeLibrary = PhysicsActivator.NATIVE_BULLET_LIBRARY_LOADED;

	/** The library to use. */
	String libraryToUse = BULLET_LIBRARY_NAME;

	/** The registered agents. */
	private final AsOrderedSet<IAgent> registeredAgents = Collector.getOrderedSet();

	/** The max sub steps. */
	private int maxSubSteps;

	/**
	 * Instantiates a new physical simulation agent.
	 *
	 * @param s
	 *            the s
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public PhysicalSimulationAgent(final IPopulation<? extends IAgent> s, final int index) throws GamaRuntimeException {
		super(s, index);
	}

	/**
	 * Prim register.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@action (
			doc = @doc ("An action that allows to register agents in this physical world. Unregistered agents will not be governed by the physical laws of this world. If the world is to play a role in the physical world,"
					+ "then it needs to register itself (i.e. do register([self]);"),
			name = REGISTER,
			args = { @arg (
					doc = @doc ("the list or container of agents to register in this physical world"),
					name = BODIES,
					type = IType.CONTAINER) })

	public Object primRegister(final IScope scope) {
		IList<IAgent> agents = scope.getListArg(BODIES);
		if (agents == null) return null;
		for (IAgent agent : agents) { registerAgent(scope, agent); }
		return agents;
	}

	/**
	 * Register agent.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	private void registerAgent(final IScope scope, final IAgent agent) {
		if (registeredAgents.add(agent)) { getGateway().registerAgent(agent); }
	}

	/**
	 * Unregister agent.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 */
	private void unregisterAgent(final IScope scope, final IAgent agent) {
		if (registeredAgents.remove(agent)) { getGateway().unregisterAgent(agent); }
	}

	/**
	 * Called whenever an agent wants to update its body
	 *
	 * @param scope
	 * @param agent
	 */
	public void updateAgent(final IScope scope, final IAgent agent) {
		getGateway().updateAgentShape(agent);
	}

	/**
	 * Gets the terrain.
	 *
	 * @return the terrain
	 */
	@getter (IPhysicalConstants.TERRAIN)
	public IField getTerrain() { return terrain; }

	/**
	 * Sets the terrain.
	 *
	 * @param t
	 *            the new terrain
	 */
	@setter (IPhysicalConstants.TERRAIN)
	public void setTerrain(final IField t) { terrain = t; }

	/**
	 * Gets the ccd.
	 *
	 * @param scope
	 *            the scope
	 * @return the ccd
	 */
	@getter (
			value = ACCURATE_COLLISION_DETECTION,
			initializer = true)
	public Boolean getCCD(final IScope scope) {
		return ccd;
	}

	/**
	 * Sets the CCD.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 */
	@setter (ACCURATE_COLLISION_DETECTION)
	public void setCCD(final IScope scope, final Boolean v) {
		ccd = v;
		// Dont provoke the instantiation of the gateway yet if it is null
		if (gateway != null) { gateway.setCCD(v); }
	}

	/**
	 * Gets the automated registration.
	 *
	 * @param scope
	 *            the scope
	 * @return the automated registration
	 */
	@getter (
			value = AUTOMATED_REGISTRATION,
			initializer = true)
	public Boolean getAutomatedRegistration(final IScope scope) {
		return automatedRegistration;
	}

	/**
	 * Sets the automated registration.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 */
	@setter (AUTOMATED_REGISTRATION)
	public void setAutomatedRegistration(final IScope scope, final Boolean v) {
		automatedRegistration = v;
	}

	/**
	 * Uses native library.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = USE_NATIVE)
	public Boolean usesNativeLibrary(final IScope scope) {
		if (useNativeLibrary == null) { useNativeLibrary = PhysicsActivator.NATIVE_BULLET_LIBRARY_LOADED; }
		return useNativeLibrary;
	}

	/**
	 * Use native library.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 */
	@setter (USE_NATIVE)
	public void useNativeLibrary(final IScope scope, final Boolean v) {
		// If we have not successfully loaded the library, then the setting should remain false.
		useNativeLibrary = v;
	}

	/**
	 * Library to use.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@getter (
			value = LIBRARY_NAME,
			initializer = true)
	public String libraryToUse(final IScope scope) {
		return libraryToUse;
	}

	/**
	 * Library to use.
	 *
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 */
	@setter (LIBRARY_NAME)
	public void libraryToUse(final IScope scope, final String v) {
		libraryToUse = v;
	}

	/**
	 * Gets the max sub steps.
	 *
	 * @param scope
	 *            the scope
	 * @return the max sub steps
	 */
	@getter (
			value = MAX_SUBSTEPS,
			initializer = true)
	public int getMaxSubSteps(final IScope scope) {
		return maxSubSteps;
	}

	/**
	 * Sets the max sub steps.
	 *
	 * @param scope
	 *            the scope
	 * @param steps
	 *            the steps
	 */
	@setter (MAX_SUBSTEPS)
	public void setMaxSubSteps(final IScope scope, final int steps) {
		maxSubSteps = steps;
	}

	/**
	 * Gets the gravity.
	 *
	 * @param scope
	 *            the scope
	 * @return the gravity
	 */
	@getter (
			value = GRAVITY,
			initializer = true)
	public IPoint getGravity(final IScope scope) {
		return gravity;
	}

	/**
	 * Sets the gravity.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 */
	@setter (GRAVITY)
	public void setGravity(final IScope scope, final IPoint g) {
		this.gravity.setLocation(g);
		// Dont provoke the instantiation of the gateway yet if it is null
		if (gateway != null) { gateway.setGravity(g); }
	}

	/**
	 * Creates and adds a hinge joint.
	 *
	 * @param scope
	 *            the scope
	 * @param bodyA
	 *            the body A
	 * @param bodyB
	 *            the body B
	 * @param anchor
	 *            the anchor
	 * @param lowerLimit
	 *            the lower limit
	 * @param upperLimit
	 *            the upper limit
	 * @param motorSpeed
	 *            the motor speed
	 * @param maxMotorForce
	 *            the max motor force
	 * @return the gama joint
	 */
	@operator (
			doc = @doc ("Creates and adds a hinge joint around the world Z axis. Limits are angles in radians; motor speed is radians per second. A positive maximum motor force enables the motor."),
			value = "create_hinge_joint")
	public GamaJoint createHingeJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final Double lowerLimit, final Double upperLimit, final Double motorSpeed,
			final Double maxMotorForce) {
		return createJoint(scope, IJointDefinition.JointType.HINGE, bodyA, bodyB, anchor,
				GamaPointFactory.create(0, 0, 1),
				lowerLimit, upperLimit, motorSpeed, maxMotorForce);
	}

	@operator (
			doc = @doc ("Creates and adds a hinge joint around the supplied world-space axis. Limits are in radians and motor speed is radians per second."),
			value = "create_hinge_joint_with_axis")
	public GamaJoint createHingeJointWithAxis(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final IPoint axis, final Double lowerLimit, final Double upperLimit,
			final Double motorSpeed, final Double maxMotorForce) {
		return createJoint(scope, IJointDefinition.JointType.HINGE, bodyA, bodyB, anchor, axis, lowerLimit,
				upperLimit, motorSpeed, maxMotorForce);
	}

	/**
	 * Creates and adds a slider joint along the world-space x axis.
	 *
	 * @param scope
	 *            the scope
	 * @param bodyA
	 *            the body A
	 * @param bodyB
	 *            the body B
	 * @param anchor
	 *            the anchor
	 * @param lowerLimit
	 *            the lower limit
	 * @param upperLimit
	 *            the upper limit
	 * @return the gama joint
	 */
	@operator (
			doc = @doc ("Creates and adds a slider joint along the world-space x axis. Limits are distances."),
			value = "create_slider_joint")
	public GamaJoint createSliderJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final Double lowerLimit, final Double upperLimit) {
		return createJoint(scope, IJointDefinition.JointType.SLIDER, bodyA, bodyB, anchor,
				GamaPointFactory.create(1, 0, 0),
				lowerLimit, upperLimit, 0d, 0d);
	}

	@operator (
			doc = @doc ("Creates and adds a slider joint along the supplied world-space axis. Limits are distances."),
			value = "create_slider_joint_with_axis")
	public GamaJoint createSliderJointWithAxis(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final IPoint axis, final Double lowerLimit, final Double upperLimit,
			final Double motorSpeed, final Double maxMotorForce) {
		return createJoint(scope, IJointDefinition.JointType.SLIDER, bodyA, bodyB, anchor, axis, lowerLimit,
				upperLimit, motorSpeed, maxMotorForce);
	}

	/**
	 * Creates and adds a ball-and-socket joint.
	 *
	 * @param scope
	 *            the scope
	 * @param bodyA
	 *            the body A
	 * @param bodyB
	 *            the body B
	 * @param anchor
	 *            the anchor
	 * @return the gama joint
	 */
	@operator (
			doc = @doc ("Creates and adds a ball-and-socket joint at the supplied world-space anchor."),
			value = "create_ball_and_socket_joint")
	public GamaJoint createBallAndSocketJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor) {
		return createJoint(scope, IJointDefinition.JointType.BALL_AND_SOCKET, bodyA, bodyB, anchor,
				GamaPointFactory.create(0, 0, 1), null, null, 0d, 0d);
	}

	private GamaJoint createJoint(final IScope scope, final IJointDefinition.JointType type, final Object bodyA,
			final Object bodyB, final IPoint anchor, final IPoint axis, final Double lowerLimit,
			final Double upperLimit, final Double motorSpeed, final Double maxMotorForce) {
		if (anchor == null) throw GamaRuntimeException.error("A joint anchor is required", scope);
		if (!Double.isFinite(anchor.getX()) || !Double.isFinite(anchor.getY()) || !Double.isFinite(anchor.getZ())) {
			throw GamaRuntimeException.error("Joint anchors must have finite coordinates", scope);
		}
		if ((lowerLimit == null) != (upperLimit == null) || lowerLimit != null && lowerLimit > upperLimit) {
			throw GamaRuntimeException.error("Joint limits must be supplied as an ordered lower/upper pair", scope);
		}
		if (lowerLimit != null && (!Double.isFinite(lowerLimit) || !Double.isFinite(upperLimit))) {
			throw GamaRuntimeException.error("Joint limits must be finite", scope);
		}
		if (axis == null || !Double.isFinite(axis.norm()) || axis.norm() == 0) {
			throw GamaRuntimeException.error("Joint axis must be a non-zero point", scope);
		}
		if (motorSpeed != null && !Double.isFinite(motorSpeed)
				|| maxMotorForce != null && (!Double.isFinite(maxMotorForce) || maxMotorForce < 0)) {
			throw GamaRuntimeException.error("Joint motor speed and force must be finite; force cannot be negative",
					scope);
		}
		IAgent agentA = validateJointBody(scope, bodyA);
		IAgent agentB = validateJointBody(scope, bodyB);
		if (agentA == agentB) throw GamaRuntimeException.error("A joint must connect two different agents", scope);
		if (!registeredAgents.contains(agentA)) { registerAgent(scope, agentA); }
		if (!registeredAgents.contains(agentB)) { registerAgent(scope, agentB); }
		if (!(agentA.getAttribute(BODY) instanceof gama.extension.physics.common.IBody)
				|| !(agentB.getAttribute(BODY) instanceof gama.extension.physics.common.IBody)) {
			throw GamaRuntimeException.error("Joint bodies must be registered in the physical world", scope);
		}
		boolean hasLimits = lowerLimit != null;
		double lower = hasLimits ? lowerLimit : 0d;
		double upper = hasLimits ? upperLimit : 0d;
		double speed = motorSpeed == null ? 0d : motorSpeed;
		double force = maxMotorForce == null ? 0d : maxMotorForce;
		GamaJoint definition = new GamaJoint(null, type, agentA, agentB, anchor, axis, lower, upper, hasLimits, speed,
				force);
		try {
			Object engineJoint = getGateway().createJoint(definition);
			return new GamaJoint(engineJoint, type, agentA, agentB, anchor, axis, lower, upper, hasLimits, speed, force);
		} catch (IllegalArgumentException e) {
			throw GamaRuntimeException.error(e.getMessage(), scope);
		}
	}

	private IAgent validateJointBody(final IScope scope, final Object body) {
		if (!(body instanceof IAgent agent)
				|| !(agent.getSpecies().implementsSkill(DYNAMIC_BODY) || agent.getSpecies().implementsSkill(STATIC_BODY))) {
			throw GamaRuntimeException.error("Joint bodies must be agents with the dynamic_body or static_body skill",
					scope);
		}
		return agent;
	}

	@Override
	public void dispose() {
		getGateway().dispose();
		registeredAgents.clear();
		super.dispose();
	}

	@Override
	protected void registerMicropopulation(final IScope scope, final ISpecies species,
			final IPopulation<? extends IAgent> pop) {

		if (species.implementsSkill(DYNAMIC_BODY) || species.implementsSkill(STATIC_BODY)) {
			pop.addListener(populationListener);
		}
		super.registerMicropopulation(scope, species, pop);
	}

	@Override
	public boolean doStep(final IScope scope) {
		if (super.doStep(scope)) {
			final Double timeStep = getTimeStep(scope);
			getGateway().doStep(timeStep, maxSubSteps);
			return true;
		}
		return false;
	}

	/**
	 * Gets the gateway.
	 *
	 * @return the gateway
	 */
	IPhysicalWorld getGateway() {
		if (gateway == null) {
			boolean isBullet = BULLET_LIBRARY_NAME.equals(libraryToUse);
			if (isBullet) {
				if (useNativeLibrary) {
					gateway = new NativeBulletPhysicalWorld(this);
				} else {
					gateway = new BulletPhysicalWorld(this);
				}
			} else {
				gateway = new Box2DPhysicalWorld(this);
			}
		}
		return gateway;
	}

	/**
	 * The listener interface for receiving bodyPopulation events. The class that is interested in processing a
	 * bodyPopulation event implements this interface, and the object created with that class is registered with a
	 * component using the component's <code>addBodyPopulationListener<code> method. When the bodyPopulation event
	 * occurs, that object's appropriate method is invoked.
	 *
	 * @see BodyPopulationEvent
	 */
	class BodyPopulationListener implements IPopulation.Listener {

		@Override
		public void notifyAgentRemoved(final IScope scope, final IPopulation<? extends IAgent> pop,
				final IAgent agent) {
			unregisterAgent(scope, agent);
		}

		@Override
		public void notifyAgentAdded(final IScope scope, final IPopulation<? extends IAgent> pop, final IAgent agent) {
			if (automatedRegistration) { registerAgent(scope, agent); }
		}

		@Override
		public void notifyAgentsAdded(final IScope scope, final IPopulation<? extends IAgent> pop,
				final Collection<? extends IAgent> agents) {
			if (scope.interrupted()) return;

			if (automatedRegistration) { for (IAgent a : agents) { registerAgent(scope, a); } }
		}

		@Override
		public void notifyAgentsRemoved(final IScope scope, final IPopulation<? extends IAgent> pop,
				final Collection<? extends IAgent> agents) {
			if (scope.interrupted()) return;

			for (IAgent a : agents) { unregisterAgent(scope, a); }
		}

		@Override
		public void notifyPopulationCleared(final IScope scope, final IPopulation<? extends IAgent> pop) {
			if (scope.interrupted()) return;
			for (IAgent a : pop) { unregisterAgent(scope, a); }

		}

	}

}