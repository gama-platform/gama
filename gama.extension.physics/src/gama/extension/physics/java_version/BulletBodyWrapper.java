/*******************************************************************************************************
 *
 * BulletBodyWrapper.java, in gama.extension.physics, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.java_version;

import static java.lang.Math.max;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.pair.GamaPairFactory;
import gama.api.types.pair.IPair;
import gama.extension.physics.common.AbstractBodyWrapper;
import gama.extension.physics.common.IBody;

/**
 * The Class BulletBodyWrapper.
 *
 * A rigid body "wrapper" dedicated to GAMA agents. Allows to translate information from/to the agents and their bodies,
 * to reconstruct shapes (from JTS geometries and GAMA 3D additions, but also from AABB envelopes) and to pass commands
 * to the bodies (velocity, forces, location...)
 *
 * @author Alexis Drogoul 2021
 */
public class BulletBodyWrapper extends AbstractBodyWrapper<DiscreteDynamicsWorld, RigidBody, CollisionShape, Vector3f>
		implements IBulletPhysicalEntity {

	/** The temp. */
	private final Transform temp = new Transform();

	/** The vtemp 2. */
	private final Vector3f vtemp = new Vector3f(), vtemp2 = new Vector3f();

	/** The axis angle transfer. */
	final AxisAngle4f axisAngleTransfer = new AxisAngle4f();

	/** The quat transfer. */
	Quat4f quatTransfer = new Quat4f();

	/**
	 * Instantiates a new bullet body wrapper.
	 *
	 * @param agent
	 *            the agent
	 * @param gateway
	 *            the gateway
	 */
	public BulletBodyWrapper(final IAgent agent, final BulletPhysicalWorld gateway) {
		super(agent, gateway);
		body.setUserPointer(this);
	}

	@Override
	public RigidBody createAndInitializeBody(final CollisionShape shape, final DiscreteDynamicsWorld world) {
		final Transform startTransform = new Transform();
		startTransform.setIdentity();
		IPoint p = agent.getLocation();
		startTransform.origin.set((float) p.getX(), (float) p.getY(), (float) p.getZ() + aabbTranslation.getZ());
		final MotionState state = new DefaultMotionState(startTransform);
		final RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(0f, state, shape);
		IBody previous = (IBody) agent.getAttribute(BODY);
		if (previous != null) {
			final float mass = previous.getMass();
			info.mass = mass;
			if (mass != 0f) { shape.calculateLocalInertia(mass, info.localInertia); }
			info.friction = previous.getFriction();
			info.restitution = previous.getRestitution();
			info.angularDamping = previous.getAngularDamping();
			info.linearDamping = previous.getLinearDamping();

		}
		RigidBody body1 = new RigidBody(info);
		if (!isStatic) { body1.setActivationState(CollisionObject.DISABLE_DEACTIVATION); }
		if (previous != null) {
			IPoint pointTransfer = GamaPointFactory.create();
			body1.setLinearVelocity(toVector(previous.getLinearVelocity(pointTransfer)));
			body1.setAngularVelocity(toVector(previous.getAngularVelocity(pointTransfer)));
		}
		body1.setCollisionFlags(CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
		return body1;
	}

	@Override
	public void setCCD(final boolean v) {
		if (v) {
			body.getAabb(vtemp, vtemp2);
			vtemp2.sub(vtemp);
			float ccd = max(max(vtemp2.x, vtemp2.y), vtemp2.z);
			body.setCcdMotionThreshold(ccd / 4);
			body.setCcdSweptSphereRadius(ccd / 2);
		} else {
			body.setCcdMotionThreshold(0f);
		}
	}

	// ====================================================
	// Transfer functions from the agent to the rigid body
	// ====================================================

	@Override
	public void setFriction(final Double friction) {
		body.setFriction(clamp(friction));
	}

	@Override
	public void setRestitution(final Double restitution) {
		body.setRestitution(clamp(restitution));
	}

	@Override
	public void setDamping(final Double damping) {
		body.setDamping(clamp(damping), getAngularDamping());
	}

	@Override
	public void setAngularDamping(final Double damping) {
		body.setDamping(getLinearDamping(), clamp(damping));
	}

	@Override
	public void setAngularVelocity(final IPoint angularVelocity) {
		body.setAngularVelocity(toVector(angularVelocity));
	}

	@Override
	public void setLinearVelocity(final IPoint linearVelocity) {
		body.setLinearVelocity(toVector(linearVelocity));
	}

	@Override
	public void setLocation(final IPoint loc) {
		// We synchronize both the world transform of the body (which holds the position at the end of last tick) and
		// the motion state.
		body.getWorldTransform(temp);
		temp.origin.set((float) loc.getX(), (float) loc.getY(), (float) loc.getZ() + aabbTranslation.z);
		body.setWorldTransform(temp);
	}

	@Override
	public void applyImpulse(final IPoint impulse) {
		body.applyCentralImpulse(toVector(impulse));

	}

	@Override
	public void applyTorque(final IPoint torque) {
		body.applyTorque(toVector(torque));

	}

	@Override
	public void applyForce(final IPoint force) {
		body.applyCentralForce(toVector(force));

	}

	@Override
	public float getMass() {
		float inverse = body.getInvMass();
		return inverse == 0f ? 0f : 1 / inverse;
	}

	@Override
	public void setMass(final Double mass) {
		body.getCollisionShape().calculateLocalInertia(mass.floatValue(), vtemp);
		body.setMassProps(mass.floatValue(), vtemp);
	}

	@Override
	public IPoint getAngularVelocity(final IPoint v) {
		body.getAngularVelocity(vtemp);
		return toGamaPoint(vtemp, v);
	}

	@Override
	public IPoint getLinearVelocity(final IPoint v) {
		body.getLinearVelocity(vtemp);
		return toGamaPoint(vtemp, v);
	}

	@Override
	public IShape getAABB() {
		body.getAabb(vtemp, vtemp2);
		return GamaShapeFactory.buildBox(vtemp2.x - vtemp.x, vtemp2.y - vtemp.y, vtemp2.z - vtemp.z,
				GamaPointFactory.create(vtemp.x + (vtemp2.x - vtemp.x) / 2, vtemp.y + (vtemp2.y - vtemp.y) / 2,
						vtemp.z + (vtemp2.z - vtemp.z) / 2 + visualTranslation.z));
	}

	@Override
	public float getContactDamping() { return 0; }

	@Override
	public void setContactDamping(final Double damping) {
		// Not available
	}

	@Override
	public float getFriction() { return body.getFriction(); }

	@Override
	public float getRestitution() {
		return body.getRestitution();

	}

	@Override
	public float getLinearDamping() { return body.getLinearDamping(); }

	@Override
	public float getAngularDamping() { return body.getAngularDamping(); }

	@Override
	public void clearForces() {
		body.clearForces();
	}

	@Override
	public void transferLocationAndRotationToAgent() {
		Vector3f vectorTransfer = body.getWorldTransform(temp).origin;
		agent.setLocation(
				GamaPointFactory.create(vectorTransfer.x, vectorTransfer.y, vectorTransfer.z - aabbTranslation.z));
		temp.getRotation(quatTransfer);
		axisAngleTransfer.set(quatTransfer);
		@SuppressWarnings ("unchecked") var rot = (IPair<Double, IPoint>) agent.getAttribute(ROTATION);
		if (rot == null) {
			rot = GamaPairFactory.createWith(0d, GamaPointFactory.create(0, 0, 1), Types.FLOAT, Types.POINT);
			agent.setAttribute(ROTATION, rot);
		}
		rot.setKey(Math.toDegrees(axisAngleTransfer.angle));
		rot.getValue().setLocation(axisAngleTransfer.x, axisAngleTransfer.y, axisAngleTransfer.z);
	}

}
