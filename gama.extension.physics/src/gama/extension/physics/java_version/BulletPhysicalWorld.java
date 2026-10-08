/*******************************************************************************************************
 *
 * BulletPhysicalWorld.java, in gaml.extensions.physics, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.java_version;

import javax.vecmath.Vector3f;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SliderConstraint;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Multimap;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.extension.physics.common.AbstractPhysicalWorld;
import gama.extension.physics.common.IBody;
import gama.extension.physics.common.IJointDefinition;
import gama.extension.physics.common.IShapeConverter;
import gama.extension.physics.gaml.PhysicalSimulationAgent;

/**
 * The Class BulletPhysicalWorld.
 */
public class BulletPhysicalWorld extends AbstractPhysicalWorld<DiscreteDynamicsWorld, CollisionShape, Vector3f>
		implements IBulletPhysicalEntity {

	/** The config. */
	private final CollisionConfiguration config = new DefaultCollisionConfiguration();

	/** The dispatcher. */
	private final CollisionDispatcher dispatcher = new CollisionDispatcher(config);

	/**
	 * Instantiates a new bullet physical world.
	 *
	 * @param physicalSimulationAgent
	 *            the physical simulation agent
	 */
	public BulletPhysicalWorld(final PhysicalSimulationAgent physicalSimulationAgent) {
		super(physicalSimulationAgent);
	}

	@Override
	protected IShapeConverter<CollisionShape, Vector3f> createShapeConverter() {
		return new BulletShapeConverter();
	}

	@Override
	public DiscreteDynamicsWorld createWorld() {
		final BroadphaseInterface pairCache = new DbvtBroadphase();
		final SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		world = new DiscreteDynamicsWorld(dispatcher, pairCache, solver, config);
		setGravity(simulation.getGravity(simulation.getScope()));
		setCCD(simulation.getCCD(simulation.getScope()));
		// BulletGlobals.setContactAddedCallback(contactListener);
		return world;
	}

	@Override
	public void updateEngine(final Double timeStep, final int maxSubSteps) {
		getWorld().stepSimulation(timeStep.floatValue(), maxSubSteps);
	}

	@Override
	public void registerAgent(final IAgent agent) {
		BulletBodyWrapper body = new BulletBodyWrapper(agent, this);
		getWorld().addRigidBody(body.getBody());
		body.setCCD(simulation.getCCD(simulation.getScope()));
	}

	@Override
	public void unregisterAgent(final IAgent agent) {
		BulletBodyWrapper b = (BulletBodyWrapper) agent.getAttribute(BODY);
		getWorld().removeRigidBody(b.getBody());
	}

	@Override
	public void updateAgentsShape() {
		// We update the agents
		for (IAgent a : updatableAgents) { unregisterAgent(a); }
		for (IAgent a : updatableAgents) { registerAgent(a); }
		updatableAgents.clear();
	}

	@Override
	public void collectContacts(final Multimap<IBody, IBody> newContacts) {
		for (PersistentManifold pm : dispatcher.getInternalManifoldPointer()) {
			if (pm == null) { continue; }
			IBody b0 = (IBody) ((RigidBody) pm.getBody0()).getUserPointer();
			IBody b1 = (IBody) ((RigidBody) pm.getBody1()).getUserPointer();
			if (b0.isNoNotification() && b1.isNoNotification()) { continue; }
			int n = pm.getNumContacts();
			for (int i = 0; i < n; i++) {
				ManifoldPoint pt = pm.getContactPoint(i);
				if (pt.getDistance() < 0.1) {
					newContacts.put(b0, b1);
					break;
				}
			}
		}
	}

	@Override
	public void setCCD(final boolean ccd) {
		if (world != null) {
			world.getCollisionObjectArray().forEach(b -> {
				if (b.isStaticObject()) return;
				Object o = b.getUserPointer();
				if (o instanceof IBody) { ((IBody) o).setCCD(ccd); }
			});
		}
	}

	@Override
	public void setGravity(final GamaPoint g) {
		if (world != null) { world.setGravity(toVector(g)); }
	}

	@Override
	public void dispose() {
		if (world != null) {
			world.destroy();
			world = null;
		}
		BulletGlobals.cleanCurrentThread();
	}

	@Override
	public void updatePositionsAndRotations() {
		world.getCollisionObjectArray().forEach(b -> {
			RigidBody rb = (RigidBody) b;
			IBody bw = (IBody) rb.getUserPointer();
			if (rb.isActive() && !rb.isStaticObject()) { bw.transferLocationAndRotationToAgent(); }
		});
	}

	/**
	 * Adds a joint (constraint) to the jBullet world.
	 *
	 * @param constraint the joint/constraint to add
	 */
	public void addJoint(TypedConstraint constraint) {
		world.addConstraint(constraint);
	}

	@Override
	public Object createJoint(IJointDefinition jointDefinition) {
		TypedConstraint constraint = convertToBulletConstraint(jointDefinition);
		getWorld().addConstraint(constraint, true);
		return constraint;
	}

	private TypedConstraint convertToBulletConstraint(IJointDefinition jointDefinition) {
		if (!(jointDefinition.getBodyA() instanceof IAgent agentA)
				|| !(jointDefinition.getBodyB() instanceof IAgent agentB)) {
			throw new IllegalArgumentException("Joint bodies must be agents with physical body skills");
		}
		Object bodyA = agentA.getAttribute(BODY);
		Object bodyB = agentB.getAttribute(BODY);
		if (!(bodyA instanceof BulletBodyWrapper wrapperA) || !(bodyB instanceof BulletBodyWrapper wrapperB)) {
			throw new IllegalArgumentException("Both joint bodies must be registered in the physical world");
		}
		RigidBody first = wrapperA.getBody();
		RigidBody second = wrapperB.getBody();
		Vector3f anchor = toVector(jointDefinition.getAnchorPoint());
		Vector3f pivotA = toLocalPoint(first, anchor);
		Vector3f pivotB = toLocalPoint(second, anchor);
		TypedConstraint result;
		switch (jointDefinition.getJointType()) {
			case HINGE -> {
				Vector3f axis = toVector(jointDefinition.getAxis());
				if (axis.lengthSquared() == 0) throw new IllegalArgumentException("Joint axis must be non-zero");
				axis.normalize();
				Vector3f axisA = toLocalAxis(first, axis);
				Vector3f axisB = toLocalAxis(second, axis);
				HingeConstraint hinge = new HingeConstraint(first, second, pivotA, pivotB, axisA, axisB);
				if (jointDefinition.hasLimits()) {
					hinge.setLimit((float) jointDefinition.getLowerLimit(), (float) jointDefinition.getUpperLimit());
				}
				hinge.enableAngularMotor(jointDefinition.getMaxMotorForce() > 0,
						(float) jointDefinition.getMotorSpeed(), (float) jointDefinition.getMaxMotorForce());
				result = hinge;
			}
			case SLIDER -> {
				Vector3f axis = toVector(jointDefinition.getAxis());
				if (axis.lengthSquared() == 0) throw new IllegalArgumentException("Joint axis must be non-zero");
				axis.normalize();
				Transform frameA = sliderFrame(pivotA, toLocalAxis(first, axis));
				Transform frameB = sliderFrame(pivotB, toLocalAxis(second, axis));
				SliderConstraint slider = new SliderConstraint(first, second, frameA, frameB, true);
				if (jointDefinition.hasLimits()) {
					slider.setLowerLinLimit((float) jointDefinition.getLowerLimit());
					slider.setUpperLinLimit((float) jointDefinition.getUpperLimit());
				}
				slider.setPoweredLinMotor(jointDefinition.getMaxMotorForce() > 0);
				slider.setTargetLinMotorVelocity((float) jointDefinition.getMotorSpeed());
				slider.setMaxLinMotorForce((float) jointDefinition.getMaxMotorForce());
				result = slider;
			}
			case BALL_AND_SOCKET -> result = new Point2PointConstraint(first, second, pivotA, pivotB);
			default -> throw new IllegalArgumentException("Unsupported joint type: " + jointDefinition.getJointType());
		}
		return result;
	}

	private Vector3f toLocalPoint(final RigidBody body, final Vector3f worldPoint) {
		Transform transform = body.getCenterOfMassTransform(new Transform());
		transform.inverse();
		Vector3f result = new Vector3f(worldPoint);
		transform.transform(result);
		return result;
	}

	private Vector3f toLocalAxis(final RigidBody body, final Vector3f worldAxis) {
		Transform transform = body.getCenterOfMassTransform(new Transform());
		transform.basis.transpose();
		Vector3f result = new Vector3f(worldAxis);
		transform.basis.transform(result);
		result.normalize();
		return result;
	}

	private Transform sliderFrame(final Vector3f pivot, final Vector3f axis) {
		Vector3f reference = Math.abs(axis.z) < 0.9f ? new Vector3f(0, 0, 1) : new Vector3f(0, 1, 0);
		Vector3f second = new Vector3f();
		second.cross(reference, axis);
		second.normalize();
		Vector3f third = new Vector3f();
		third.cross(axis, second);
		third.normalize();
		Transform frame = new Transform();
		frame.setIdentity();
		frame.origin.set(pivot);
		frame.basis.setColumn(0, axis);
		frame.basis.setColumn(1, second);
		frame.basis.setColumn(2, third);
		return frame;
	}
}
