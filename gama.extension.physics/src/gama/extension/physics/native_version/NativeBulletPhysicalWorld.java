/*******************************************************************************************************
 *
 * NativeBulletPhysicalWorld.java, in gaml.extensions.physics, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.native_version;

import java.lang.Thread.State;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PersistentManifolds;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.Point2PointJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.joints.SliderJoint;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.bullet.joints.PhysicsJoint;

import gama.core.common.interfaces.GeneralSynchronizer;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.dev.DEBUG;
import gama.extension.physics.common.AbstractPhysicalWorld;
import gama.extension.physics.common.IBody;
import gama.extension.physics.common.IShapeConverter;
import gama.extension.physics.common.IJointDefinition;
import gama.extension.physics.gaml.PhysicalSimulationAgent;

/**
 * The Class NativeBulletPhysicalWorld.
 */
public class NativeBulletPhysicalWorld extends AbstractPhysicalWorld<PhysicsSpace, CollisionShape, Vector3f>
		implements INativeBulletPhysicalEntity, Runnable {

	/**
	 * The Class GamaPhysicsSpace.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 sept. 2023
	 */
	class GamaPhysicsSpace extends PhysicsSpace {

		/**
		 * Instantiates a new gama physics space.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param dbvt
		 *            the dbvt
		 * @date 25 sept. 2023
		 */
		public GamaPhysicsSpace(final BroadphaseType dbvt) {
			super(dbvt);
		}

		@Override
		public void onContactStarted(final long manifoldId) {
			int numPoints = PersistentManifolds.countPoints(manifoldId);
			if (numPoints == 0) return;
			long bodyAId = PersistentManifolds.getBodyAId(manifoldId);
			PhysicsCollisionObject pcoA = PhysicsCollisionObject.findInstance(bodyAId);
			long bodyBId = PersistentManifolds.getBodyBId(manifoldId);
			PhysicsCollisionObject pcoB = PhysicsCollisionObject.findInstance(bodyBId);
			for (int i = 0; i < numPoints; ++i) {
				long pointId = PersistentManifolds.getPointId(manifoldId, i);
				contactListener.onContactProcessed(pcoA, pcoB, pointId);
			}

		}

	}

	static {
		DEBUG.OFF();
	}

	/** The time step. */
	volatile Double timeStep = 1d;

	/** The max sub steps. */
	volatile int maxSubSteps;

	/** The do init. */
	volatile boolean doInit = true;

	/** The do update. */
	volatile boolean doUpdate = true;

	/** The continue step. */
	volatile boolean continueStep = false;

	/** The thread. */
	Thread thread = new Thread(this);

	/** The lock. */
	volatile GeneralSynchronizer semaphore = GeneralSynchronizer.withInitialAndMaxPermits(1, 1);

	@Override
	public void run() {
		if (doInit) {
			world = new GamaPhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
			world.setForceUpdateAllAabbs(false);
			world.useDeterministicDispatch(true);
			setGravity(simulation.getGravity(simulation.getScope()));
			setCCD(simulation.getCCD(simulation.getScope()));
			doInit = false;
			// DEBUG.OUT("Creating world in thread " + Thread.currentThread().getName());
		}
		while (doUpdate) {
			semaphore.acquire();
			PhysicsSpace world = getWorld();
			if (world != null) { world.update(timeStep.floatValue(), maxSubSteps, false, false, true); }
			// DEBUG.OUT("Actually updating world in thread " + Thread.currentThread().getName());
			continueStep = true;
		}
	}

	/**
	 * Instantiates a new native bullet physical world. $
	 *
	 * @param physicalSimulationAgent
	 *            the physical simulation agent
	 */
	public NativeBulletPhysicalWorld(final PhysicalSimulationAgent physicalSimulationAgent) {
		super(physicalSimulationAgent);
		semaphore.acquire();
	}

	@Override
	public void updateEngine(final Double timeStep, final int maxSubSteps) {
		this.timeStep = timeStep;
		this.maxSubSteps = maxSubSteps;
		// DEBUG.OUT("Asking to update the world in thread " + Thread.currentThread().getName());
		continueStep = false;
		semaphore.release();
		while (!continueStep) { Thread.yield(); }
	}

	@Override
	protected IShapeConverter<CollisionShape, Vector3f> createShapeConverter() {
		return new NativeBulletShapeConverter();
	}

	@Override
	public PhysicsSpace createWorld() {
		if (world != null) return world;
		if (thread.getState() == State.NEW) { thread.start(); }
		while (doInit) { Thread.yield(); }
		return world;
	}

	@Override
	public void registerAgent(final IAgent agent) {
		PhysicsSpace world = getWorld();
		if (world != null) {
			NativeBulletBodyWrapper b = new NativeBulletBodyWrapper(agent, this);
			world.addCollisionObject(b.getBody());
			b.setCCD(simulation.getCCD(simulation.getScope()));
		}
	}

	@Override
	public void unregisterAgent(final IAgent agent) {
		Object body = agent.getAttribute(BODY);
		PhysicsSpace world = getWorld();
		if (world != null && body instanceof NativeBulletBodyWrapper wrapper) { world.remove(wrapper.getBody()); }
	}

	@Override
	public void setCCD(final boolean ccd) {
		if (world != null) {
			world.getRigidBodyList().forEach(b -> {
				if (b.isStatic()) return;
				Object o = b.getUserObject();
				if (o instanceof IBody) { ((IBody) o).setCCD(ccd); }
			});
		}
	}

	@Override
	public void setGravity(final GamaPoint g) {
		PhysicsSpace world = getWorld();
		if (world != null) { world.setGravity(toVector(g)); }
	}

	@Override
	public void dispose() {
		if (world == null) return;
		// Doesnt seem to be necessary as the "CleanerThread" is running. See
		// https://hub.jmonkeyengine.org/t/solved-how-to-close-a-physics-space-to-free-up-ram/47684/9
		// world.destroy();
		// NativePhysicsObject.freeUnusedObjects();
		// CollisionSpace.physicsSpaceTL;
		doUpdate = false;
		semaphore.release();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		world = null;
		// The goal here is to get rid of bridge Java/C++ objects as soon as possible
		System.gc();

	}

	@Override
	public void updatePositionsAndRotations() {
		PhysicsSpace world = getWorld();
		if (world == null) return;
		for (PhysicsRigidBody b : world.getRigidBodyList()) {
			NativeBulletBodyWrapper bw = (NativeBulletBodyWrapper) b.getUserObject();
			if (b.isActive() && !b.isStatic()) { bw.transferLocationAndRotationToAgent(); }
		}
	}

	@Override
	protected void updateAgentsShape() {
		// We update the agents
		for (IAgent a : updatableAgents) {
			NativeBulletBodyWrapper body = (NativeBulletBodyWrapper) a.getAttribute(BODY);
			if (body == null) return;
			body.updateShape(getShapeConverter());
		}
		updatableAgents.clear();
	}

	/**
	 * Adds a joint to the native Bullet world.
	 *
	 * @param joint the joint to add
	 */
	public void addJoint(PhysicsJoint joint) {
		world.addJoint(joint);
	}

	@Override
	public Object createJoint(IJointDefinition jointDefinition) {
		PhysicsJoint joint = convertToNativeBulletJoint(jointDefinition);
		if (joint instanceof com.jme3.bullet.joints.Constraint constraint) {
			constraint.setCollisionBetweenLinkedBodies(false);
		}
		getWorld().addJoint(joint);
		return joint;
	}

	private PhysicsJoint convertToNativeBulletJoint(IJointDefinition jointDefinition) {
		if (!(jointDefinition.getBodyA() instanceof IAgent agentA)
				|| !(jointDefinition.getBodyB() instanceof IAgent agentB)) {
			throw new IllegalArgumentException("Joint bodies must be agents with physical body skills");
		}
		Object bodyA = agentA.getAttribute(BODY);
		Object bodyB = agentB.getAttribute(BODY);
		if (!(bodyA instanceof NativeBulletBodyWrapper wrapperA)
				|| !(bodyB instanceof NativeBulletBodyWrapper wrapperB)) {
			throw new IllegalArgumentException("Both joint bodies must be registered in the physical world");
		}
		PhysicsRigidBody first = wrapperA.getBody();
		PhysicsRigidBody second = wrapperB.getBody();
		Vector3f anchor = toVector(jointDefinition.getAnchorPoint());
		Vector3f pivotA = toLocalPoint(first, anchor);
		Vector3f pivotB = toLocalPoint(second, anchor);
		switch (jointDefinition.getJointType()) {
			case HINGE -> {
				Vector3f axis = toVector(jointDefinition.getAxis());
				if (axis.lengthSquared() == 0) throw new IllegalArgumentException("Joint axis must be non-zero");
				axis.normalizeLocal();
				HingeJoint hinge = new HingeJoint(first, second, pivotA, pivotB,
						toLocalAxis(first, axis), toLocalAxis(second, axis));
				if (jointDefinition.hasLimits()) {
					hinge.setLimit((float) jointDefinition.getLowerLimit(), (float) jointDefinition.getUpperLimit());
				}
				hinge.enableMotor(jointDefinition.getMaxMotorForce() > 0,
						(float) jointDefinition.getMotorSpeed(), (float) jointDefinition.getMaxMotorForce());
				return hinge;
			}
			case SLIDER -> {
				Vector3f axis = toVector(jointDefinition.getAxis());
				if (axis.lengthSquared() == 0) throw new IllegalArgumentException("Joint axis must be non-zero");
				axis.normalizeLocal();
				Vector3f axisA = toLocalAxis(first, axis);
				Vector3f axisB = toLocalAxis(second, axis);
				SliderJoint slider = new SliderJoint(first, second, pivotA, pivotB,
						sliderFrame(axisA), sliderFrame(axisB), true);
				if (jointDefinition.hasLimits()) {
					slider.setLowerLinLimit((float) jointDefinition.getLowerLimit());
					slider.setUpperLinLimit((float) jointDefinition.getUpperLimit());
				}
				slider.setPoweredLinMotor(jointDefinition.getMaxMotorForce() > 0);
				slider.setTargetLinMotorVelocity((float) jointDefinition.getMotorSpeed());
				slider.setMaxLinMotorForce((float) jointDefinition.getMaxMotorForce());
				return slider;
			}
			case BALL_AND_SOCKET -> {
				return new Point2PointJoint(first, second, pivotA, pivotB);
			}
			default -> throw new IllegalArgumentException("Unsupported joint type: " + jointDefinition.getJointType());
		}
	}

	private Vector3f toLocalPoint(final PhysicsRigidBody body, final Vector3f worldPoint) {
		Vector3f offset = worldPoint.subtract(body.getPhysicsLocation(new Vector3f()));
		return body.getPhysicsRotation(new Quaternion()).inverse().mult(offset);
	}

	private Vector3f toLocalAxis(final PhysicsRigidBody body, final Vector3f worldAxis) {
		Vector3f result = body.getPhysicsRotation(new Quaternion()).inverse().mult(worldAxis);
		return result.normalizeLocal();
	}

	private Matrix3f sliderFrame(final Vector3f axis) {
		Vector3f reference = Math.abs(axis.z) < 0.9f ? new Vector3f(0, 0, 1) : new Vector3f(0, 1, 0);
		Vector3f second = reference.cross(axis).normalizeLocal();
		Vector3f third = axis.cross(second).normalizeLocal();
		Matrix3f result = new Matrix3f();
		result.fromAxes(axis, second, third);
		return result;
	}
}
