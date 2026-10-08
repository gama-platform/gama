/*******************************************************************************************************
 *
 * Box2DPhysicalWorld.java, in gama.extension.physics, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.box2d_version;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.RopeJointDef;
import org.jbox2d.dynamics.joints.WeldJointDef;
import org.jbox2d.dynamics.joints.WheelJointDef;

import gama.api.kernel.agent.IAgent;
import gama.api.types.geometry.IPoint;
import gama.extension.physics.common.AbstractPhysicalWorld;
import gama.extension.physics.common.IBody;
import gama.extension.physics.common.IJointDefinition;
import gama.extension.physics.common.IShapeConverter;
import gama.extension.physics.gaml.PhysicalSimulationAgent;

/**
 * The Class Box2DPhysicalWorld.
 */
public class Box2DPhysicalWorld extends AbstractPhysicalWorld<World, Shape, Vec2> implements IBox2DPhysicalEntity {

	/** The scale. */
	float scale;

	/** The target. */
	static float TARGET = 10;

	private static final int VELOCITY_ITERATIONS = 8;

	private static final int POSITION_ITERATIONS = 3;

	/**
	 * Instantiates a new box 2 D physical world.
	 *
	 * @param sim
	 *            the physical simulation agent
	 */
	public Box2DPhysicalWorld(final PhysicalSimulationAgent sim) {
		super(sim);
		double w = sim.getWidth();
		double h = sim.getHeight();
		scale = (float) (TARGET / Math.max(w, h));
	}

	@SuppressWarnings ("unused")
	@Override
	public void registerAgent(final IAgent agent) {
		new Box2DBodyWrapper(agent, this);
	}

	@Override
	public void unregisterAgent(final IAgent agent) {
		Body body = ((Box2DBodyWrapper) agent.getAttribute(BODY)).body;
		getWorld().destroyBody(body);

	}

	@Override
	public void setCCD(final boolean ccd) {}

	@Override
	public void setGravity(final IPoint gravity) {
		if (world != null) { world.setGravity(toVector(gravity)); }
	}

	@Override
	public void dispose() {
		if (world != null) {
			Body b = world.getBodyList();
			while (b != null) {
				world.destroyBody(b);
				b = b.getNext();
			}
			world = null;
		}
	}

	@Override
	public void updatePositionsAndRotations() {
		if (world == null) return;
		Body b = world.getBodyList();
		while (b != null) {
			IBody body = (IBody) b.getUserData();
			if (b.isActive()) { body.transferLocationAndRotationToAgent(); }
			b = b.getNext();
		}
	}

	@Override
	protected World createWorld() {
		IPoint p = simulation.getGravity(simulation.getScope());
		World result = new World(toVector(p));
		result.setAutoClearForces(true);
		result.setContactListener(contactListener);
		return result;
	}

	@Override
	protected IShapeConverter<Shape, Vec2> createShapeConverter() {
		return new Box2DShapeConverter(scale);
	}

	@Override
	protected void updateAgentsShape() {
		// We update the agents
		for (IAgent a : updatableAgents) { unregisterAgent(a); }
		for (IAgent a : updatableAgents) { registerAgent(a); }
		updatableAgents.clear();
	}

	@Override
	protected void updateEngine(final Double timeStep, final int maxSubSteps) {
		int steps = Math.max(1, maxSubSteps);
		float subStep = timeStep.floatValue() / steps;
		for (int i = 0; i < steps; i++) {
			getWorld().step(subStep, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
		}
	}

	@Override
	public float getScale() { return scale; }

	/**
	 * Adds a joint to the Box2D world.
	 *
	 * @param jointDef
	 *            the joint definition
	 * @return the created joint
	 */
	public Joint addJoint(final JointDef jointDef) {
		return world.createJoint(jointDef);
	}

	/**
	 * Creates the joint.
	 *
	 * @param jointDefinition
	 *            the joint definition
	 * @return the object
	 */
	@Override
	public Object createJoint(final IJointDefinition jointDefinition) {
		JointDef jointDef = convertToBox2DJointDef(jointDefinition);
		return getWorld().createJoint(jointDef);
	}

	@Override
	public void destroyJoint(final Object joint) {
		if (joint instanceof Joint j && world != null) { world.destroyJoint(j); }
	}

	/**
	 * Convert to box 2 D joint def.
	 *
	 * @param jointDefinition
	 *            the joint definition
	 * @return the joint def
	 */
	private JointDef convertToBox2DJointDef(final IJointDefinition jointDefinition) {
		if (!(jointDefinition.getBodyA() instanceof IAgent agentA)
				|| !(jointDefinition.getBodyB() instanceof IAgent agentB)) {
			throw new IllegalArgumentException("Joint bodies must be agents with physical body skills");
		}
		Object bodyA = agentA.getAttribute(BODY);
		Object bodyB = agentB.getAttribute(BODY);
		if (!(bodyA instanceof Box2DBodyWrapper wrapperA) || !(bodyB instanceof Box2DBodyWrapper wrapperB)) {
			throw new IllegalArgumentException("Both joint bodies must be registered in the physical world");
		}
		Body first = wrapperA.getBody();
		Body second = wrapperB.getBody();
		Vec2 anchor = toVector(jointDefinition.getAnchorPoint());
		switch (jointDefinition.getJointType()) {
			case HINGE, BALL_AND_SOCKET -> {
				RevoluteJointDef definition = new RevoluteJointDef();
				definition.initialize(first, second, anchor);
				if (jointDefinition.getJointType() == IJointDefinition.JointType.HINGE) {
					IPoint axis = jointDefinition.getAxis();
					if (axis == null || Math.abs(axis.getX()) > 1e-6 || Math.abs(axis.getY()) > 1e-6
							|| axis.getZ() <= 0) {
						throw new IllegalArgumentException("Box2D hinge axes must point along positive Z");
					}
					if (jointDefinition.hasLimits()) {
						definition.enableLimit = true;
						definition.lowerAngle = (float) jointDefinition.getLowerLimit();
						definition.upperAngle = (float) jointDefinition.getUpperLimit();
					}
					if (jointDefinition.getMaxMotorForce() > 0) {
						definition.enableMotor = true;
						definition.motorSpeed = (float) jointDefinition.getMotorSpeed();
						definition.maxMotorTorque = (float) jointDefinition.getMaxMotorForce();
					}
				}
				return definition;
			}
			case SLIDER -> {
				IPoint gamaAxis = jointDefinition.getAxis();
				if (gamaAxis == null || gamaAxis.getX() == 0 && gamaAxis.getY() == 0
						|| Math.abs(gamaAxis.getZ()) > 1e-6) {
					throw new IllegalArgumentException("Box2D slider axes must be non-zero and lie in the XY plane");
				}
				Vec2 axis = toVector(gamaAxis);
				axis.normalize();
				PrismaticJointDef definition = new PrismaticJointDef();
				definition.initialize(first, second, anchor, axis);
				if (jointDefinition.hasLimits()) {
					definition.enableLimit = true;
					definition.lowerTranslation = toBox2D(jointDefinition.getLowerLimit());
					definition.upperTranslation = toBox2D(jointDefinition.getUpperLimit());
				}
				if (jointDefinition.getMaxMotorForce() > 0) {
					definition.enableMotor = true;
					definition.motorSpeed = toBox2D(jointDefinition.getMotorSpeed());
					definition.maxMotorForce = (float) jointDefinition.getMaxMotorForce();
				}
				return definition;
			}
			case FIXED -> {
				WeldJointDef definition = new WeldJointDef();
				definition.initialize(first, second, anchor);
				definition.frequencyHz = (float) jointDefinition.getFrequency();
				definition.dampingRatio = (float) jointDefinition.getDamping();
				return definition;
			}
			case DISTANCE -> {
				Vec2 anchorB = toVector(jointDefinition.getSecondAnchorPoint());
				if (anchor.sub(anchorB).length() < 0.005f) {
					throw new IllegalArgumentException("The two anchors of a distance joint must be distinct");
				}
				DistanceJointDef definition = new DistanceJointDef();
				definition.initialize(first, second, anchor, anchorB);
				definition.frequencyHz = (float) jointDefinition.getFrequency();
				definition.dampingRatio = (float) jointDefinition.getDamping();
				return definition;
			}
			case ROPE -> {
				if (jointDefinition.getUpperLimit() <= 0) {
					throw new IllegalArgumentException("The maximum length of a rope joint must be positive");
				}
				RopeJointDef definition = new RopeJointDef();
				definition.bodyA = first;
				definition.bodyB = second;
				definition.localAnchorA.set(first.getLocalPoint(anchor));
				definition.localAnchorB.set(second.getLocalPoint(toVector(jointDefinition.getSecondAnchorPoint())));
				definition.maxLength = toBox2D(jointDefinition.getUpperLimit());
				return definition;
			}
			case WHEEL -> {
				IPoint gamaAxis = jointDefinition.getAxis();
				if (gamaAxis == null || gamaAxis.getX() == 0 && gamaAxis.getY() == 0
						|| Math.abs(gamaAxis.getZ()) > 1e-6) {
					throw new IllegalArgumentException("Box2D wheel axes must be non-zero and lie in the XY plane");
				}
				Vec2 axis = toVector(gamaAxis);
				axis.normalize();
				WheelJointDef definition = new WheelJointDef();
				definition.initialize(first, second, anchor, axis);
				definition.frequencyHz = (float) jointDefinition.getFrequency();
				definition.dampingRatio = (float) jointDefinition.getDamping();
				if (jointDefinition.getMaxMotorForce() > 0) {
					definition.enableMotor = true;
					definition.motorSpeed = (float) jointDefinition.getMotorSpeed();
					definition.maxMotorTorque = (float) jointDefinition.getMaxMotorForce();
				}
				return definition;
			}
			default -> throw new IllegalArgumentException(
					"Joint type " + jointDefinition.getJointType() + " is not supported by the Box2D library");
		}
	}
}