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
import java.util.concurrent.Semaphore;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PersistentManifolds;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.dev.DEBUG;
import gama.extension.physics.common.AbstractPhysicalWorld;
import gama.extension.physics.common.IBody;
import gama.extension.physics.common.IShapeConverter;
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
	volatile Semaphore semaphore = new Semaphore(1);

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
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

}
