/*******************************************************************************************************
 *
 * NativeBulletBodyWrapper.java, in gama.extension.physics, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.native_version;

import static java.lang.Math.max;

import java.util.logging.Level;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.NativePhysicsObject;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.pair.GamaPairFactory;
import gama.extension.physics.common.AbstractBodyWrapper;
import gama.extension.physics.common.IBody;
import gama.extension.physics.common.IShapeConverter;

/**
 * The Class NativeBulletBodyWrapper.
 */
/*
 * A rigid body "wrapper" dedicated to GAMA agents. Allows to translate information from/to the agents and their bodies,
 * to reconstruct shapes (from JTS geometries and GAMA 3D additions, but also from AABB envelopes) and to pass commands
 * to the bodies (velocity, forces, location...)
 *
 * @author Alexis Drogoul 2021
 */
public class NativeBulletBodyWrapper
		extends AbstractBodyWrapper<PhysicsSpace, PhysicsRigidBody, CollisionShape, Vector3f>
		implements INativeBulletPhysicalEntity {

	static {
		CollisionShape.logger.setLevel(Level.OFF);
		NativePhysicsObject.loggerN.setLevel(Level.OFF);
		PhysicsSpace.logger.setLevel(Level.OFF);
		PhysicsCollisionObject.logger.setLevel(Level.OFF);
		PhysicsRigidBody.logger2.setLevel(Level.OFF);
	}

	/**
	 * The quaternion used to transfer between GAMA coordinates and JBullet coordinates. Some discrepancies exist (esp.
	 * on spheres, for instance)
	 **/
	Quaternion quatTransfer = new Quaternion();

	/**
	 * Pre-allocated temporary Vector3f instances used to avoid per-call heap allocations in hot-path methods
	 * ({@link #getLinearVelocity}, {@link #getAngularVelocity}, {@link #setCCD}, {@link #getAABB}).
	 */
	private final Vector3f vtemp1 = new Vector3f();

	/** Pre-allocated second temporary vector for methods that need two vectors simultaneously (e.g. AABB). */
	private final Vector3f vtemp2 = new Vector3f();

	/** Pre-allocated bounding box reused across {@link #setCCD} and {@link #getAABB} calls. */
	private final BoundingBox bbTemp = new BoundingBox();

	/**
	 * Pre-allocated vector for {@link #transferLocationAndRotationToAgent()} to avoid allocating a new Vector3f on
	 * every simulation step.
	 */
	private final Vector3f locationTransfer = new Vector3f();

	/**
	 * Instantiates a new native bullet body wrapper.
	 *
	 * @param agent
	 *            the agent
	 * @param gateway
	 *            the gateway
	 */
	public NativeBulletBodyWrapper(final IAgent agent, final NativeBulletPhysicalWorld gateway) {
		super(agent, gateway);
		setLocation(agent.getLocation());
		// We add the wrapper to both the body and the agent to enable their inter-communication
		agent.setAttribute(BODY, this);
		body.setUserObject(this);
	}

	@Override
	public PhysicsRigidBody createAndInitializeBody(final CollisionShape shape, final PhysicsSpace world) {
		PhysicsRigidBody body = new PhysicsRigidBody(shape);
		IBody previous = (IBody) agent.getAttribute(BODY);
		if (previous != null) {
			body.setMass(previous.getMass());
			body.setFriction(previous.getFriction());
			body.setRestitution(previous.getRestitution());
			body.setAngularDamping(previous.getAngularDamping());
			body.setContactDamping(previous.getContactDamping());
			body.setLinearDamping(previous.getLinearDamping());
			IPoint pointTransfer = GamaPointFactory.create();
			body.setLinearVelocity(toVector(previous.getLinearVelocity(pointTransfer)));
			body.setAngularVelocity(toVector(previous.getAngularVelocity(pointTransfer)));
		}
		body.setEnableSleep(false);
		return body;

	}

	@Override
	public void setCCD(final boolean v) {
		if (v) {
			body.boundingBox(bbTemp);
			bbTemp.getMax(vtemp1);
			bbTemp.getMin(vtemp2);
			float dx = vtemp1.x - vtemp2.x;
			float dy = vtemp1.y - vtemp2.y;
			float dz = vtemp1.z - vtemp2.z;
			float ccd = max(max(dx, dy), dz);
			body.setCcdSweptSphereRadius(ccd / 2);
			body.setCcdMotionThreshold(ccd / 4);
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
		body.setLinearDamping(clamp(damping));
	}

	@Override
	public void setAngularDamping(final Double damping) {
		body.setAngularDamping(clamp(damping));
	}

	@Override
	public void setContactDamping(final Double damping) {
		body.setContactDamping(clamp(damping));
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
		body.setPhysicsLocation(toVector(loc).addLocal(aabbTranslation));
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
	public void setMass(final Double mass) {
		body.setMass(mass.floatValue());
	}

	// ====================================================
	// Transfer functions from the body to the GAMA agent
	// ====================================================

	@Override
	public void transferLocationAndRotationToAgent() {
		body.getPhysicsLocation(locationTransfer);
		agent.setLocation(GamaPointFactory.create(locationTransfer.x, locationTransfer.y,
				locationTransfer.z - aabbTranslation.z));
		body.getPhysicsRotation(quatTransfer);
		float qx = quatTransfer.getX();
		float qy = quatTransfer.getY();
		float qz = quatTransfer.getZ();
		double mag = qx * qx + qy * qy + qz * qz;
		if (mag > EPS) {
			mag = Math.sqrt(mag);
			double invMag = 1.0 / mag;
			agent.setAttribute(ROTATION,
					GamaPairFactory.createWith(Math.toDegrees(2.0 * Math.atan2(mag, quatTransfer.getW())),
							GamaPointFactory.create(qx * invMag, qy * invMag, qz * invMag), Types.FLOAT, Types.POINT));
		} else {
			agent.setAttribute(ROTATION,
					GamaPairFactory.createWith(0d, GamaPointFactory.create(0, 0, 1), Types.FLOAT, Types.POINT));
		}
	}

	@Override
	public IShape getAABB() {
		body.boundingBox(bbTemp);
		bbTemp.getMax(vtemp1);
		bbTemp.getMin(vtemp2);
		return GamaShapeFactory.buildBox(vtemp1.x - vtemp2.x, vtemp1.y - vtemp2.y, vtemp1.z - vtemp2.z,
				GamaPointFactory.create(vtemp2.x + (vtemp1.x - vtemp2.x) / 2, vtemp2.y + (vtemp1.y - vtemp2.y) / 2,
						vtemp2.z + (vtemp1.z - vtemp2.z) / 2 + visualTranslation.z));
	}

	/**
	 * Gets the translation.
	 *
	 * @return the translation
	 */
	public Vector3f getTranslation() { return aabbTranslation; }

	@Override
	public float getMass() { return body.getMass(); }

	@Override
	public float getFriction() { return body.getFriction(); }

	@Override
	public float getRestitution() { return body.getRestitution(); }

	@Override
	public float getLinearDamping() { return body.getLinearDamping(); }

	@Override
	public float getAngularDamping() { return body.getAngularDamping(); }

	@Override
	public IPoint getAngularVelocity(final IPoint v) {
		body.getAngularVelocity(vtemp1);
		return toGamaPoint(vtemp1, v);
	}

	@Override
	public IPoint getLinearVelocity(final IPoint v) {
		IPoint result = v == null ? GamaPointFactory.create() : v;
		body.getLinearVelocity(vtemp1);
		result.setLocation(vtemp1.x, vtemp1.y, vtemp1.z);
		return result;
	}

	@Override
	public void clearForces() {
		body.clearForces();
	}

	/**
	 * Update shape.
	 *
	 * @param converter
	 *            the converter
	 */
	public void updateShape(final IShapeConverter<CollisionShape, Vector3f> converter) {
		CollisionShape shape = converter.convertAndTranslate(agent, aabbTranslation, visualTranslation);
		body.setCollisionShape(shape);
	}

	@Override
	public float getContactDamping() { return body.getContactDamping(); }

}
