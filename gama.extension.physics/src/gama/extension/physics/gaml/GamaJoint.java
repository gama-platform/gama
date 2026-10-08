/*******************************************************************************************************
 *
 * GamaJoint.java, in gama.extension.physics, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.gaml;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.setter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.gaml.types.IType;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.extension.physics.common.IJointDefinition;

/**
 * A wrapper class for joints created in the physical world.
 */
@vars ({ @variable (
		name = "type",
		type = IType.STRING,
		doc = @doc ("The type of the joint: hinge, slider, ball_and_socket, fixed, distance, rope, cone_twist or wheel.")),
		@variable (
				name = "bodyA",
				type = IType.NONE,
				doc = @doc ("The first body connected by the joint.")),
		@variable (
				name = "bodyB",
				type = IType.NONE,
				doc = @doc ("The second body connected by the joint.")),
		@variable (
				name = "anchor",
				type = IType.POINT,
				doc = @doc ("The anchor point of the joint in world coordinates.")),
		@variable (
				name = "axis",
				type = IType.POINT,
				doc = @doc ("The hinge or slider axis in world coordinates.")),
		@variable (
				name = "lowerLimit",
				type = IType.FLOAT,
				doc = @doc ("The lower limit of the joint, in radians for hinges and world units for sliders.")),
		@variable (
				name = "upperLimit",
				type = IType.FLOAT,
				doc = @doc ("The upper limit of the joint, in radians for hinges and world units for sliders.")),
		@variable (
				name = "motorSpeed",
				type = IType.FLOAT,
				doc = @doc ("The motor speed, in radians per second for hinges and world units per second for sliders. Can be changed while the simulation runs.")),
		@variable (
				name = "maxMotorForce",
				type = IType.FLOAT,
				doc = @doc ("The maximum motor force/torque (or motor impulse in Bullet). A positive value enables the motor.")),
		@variable (
				name = "frequency",
				type = IType.FLOAT,
				doc = @doc ("The spring frequency in Hz of distance, wheel and fixed joints (0 means rigid).")),
		@variable (
				name = "damping",
				type = IType.FLOAT,
				doc = @doc ("The spring damping ratio of distance, wheel and fixed joints.")),
		@variable (
				name = "swingLimit",
				type = IType.FLOAT,
				doc = @doc ("The swing limit (half-angle of the cone, in radians) of a cone_twist joint.")),
		@variable (
				name = "twistLimit",
				type = IType.FLOAT,
				doc = @doc ("The twist limit (in radians) of a cone_twist joint.")),
		@variable (
				name = "angle",
				type = IType.FLOAT,
				doc = @doc ("The current angle of the joint in radians (hinges). 0 for joints without a rotational degree of freedom.")),
		@variable (
				name = "translation",
				type = IType.FLOAT,
				doc = @doc ("The current translation of the joint along its axis, in world units (sliders and wheels). 0 for joints without a translational degree of freedom.")),
		@variable (
				name = "destroyed",
				type = IType.BOOL,
				doc = @doc ("Whether the joint has been removed from the physical world.")) })
public class GamaJoint implements IJointDefinition {

	/** The joint. */
	private final Object joint;

	/** The type. */
	private final JointType type;

	/** The body A. */
	private final Object bodyA;

	/** The body B. */
	private final Object bodyB;

	/** The anchor. */
	private final IPoint anchor;

	/** The axis. */
	private final IPoint axis;

	/** The lower limit. */
	private final double lowerLimit;

	/** The upper limit. */
	private final double upperLimit;

	/** Whether limits are enabled. */
	private final boolean hasLimits;

	/** The motor speed. */
	private double motorSpeed;

	/** The max motor force. */
	private double maxMotorForce;

	/**
	 * Constructs a GamaJoint wrapping the given joint object and its attributes.
	 *
	 * @param joint
	 *            the underlying joint object
	 * @param type
	 *            the type of the joint
	 * @param bodyA
	 *            the first body connected by the joint
	 * @param bodyB
	 *            the second body connected by the joint
	 * @param anchor
	 *            the anchor point of the joint in world coordinates
	 * @param axis
	 *            the hinge or slider axis in world coordinates
	 * @param lowerLimit
	 *            the lower limit of the joint, if applicable
	 * @param upperLimit
	 *            the upper limit of the joint, if applicable
	 * @param hasLimits
	 *            whether the lower and upper limits are enabled
	 * @param motorSpeed
	 *            the motor speed of the joint, if applicable
	 * @param maxMotorForce
	 *            the maximum motor force of the joint, if applicable
	 */
	public GamaJoint(final Object joint, final JointType type, final Object bodyA, final Object bodyB,
			final IPoint anchor, final IPoint axis, final double lowerLimit, final double upperLimit,
			final boolean hasLimits, final double motorSpeed, final double maxMotorForce) {
		this.joint = joint;
		this.type = type;
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		this.anchor = anchor == null ? GamaPointFactory.create() : GamaPointFactory.create(anchor);
		this.axis = axis == null ? GamaPointFactory.create(0, 0, 1) : GamaPointFactory.create(axis);
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.hasLimits = hasLimits;
		this.motorSpeed = motorSpeed;
		this.maxMotorForce = maxMotorForce;
	}

	/** The anchor on the second body (distance and rope joints). */
	private IPoint secondAnchor;

	/** The spring frequency (Hz). */
	private double frequency;

	/** The spring damping ratio. */
	private double damping;

	/** The cone-twist swing limit. */
	private double swingLimit;

	/** The cone-twist twist limit. */
	private double twistLimit;

	/** Whether the joint has been removed from the physical world. */
	private boolean destroyed;

	/**
	 * Sets the parameters that only apply to some joint types.
	 *
	 * @return this joint
	 */
	public GamaJoint withParameters(final IPoint secondAnchor, final double frequency, final double damping,
			final double swingLimit, final double twistLimit) {
		this.secondAnchor = secondAnchor == null ? null : GamaPointFactory.create(secondAnchor);
		this.frequency = frequency;
		this.damping = damping;
		this.swingLimit = swingLimit;
		this.twistLimit = twistLimit;
		return this;
	}

	@Override
	public IPoint getSecondAnchorPoint() { return secondAnchor == null ? anchor : secondAnchor; }

	@getter ("frequency")
	@Override
	public double getFrequency() { return frequency; }

	@getter ("damping")
	@Override
	public double getDamping() { return damping; }

	@getter ("swingLimit")
	@Override
	public double getSwingLimit() { return swingLimit; }

	@getter ("twistLimit")
	@Override
	public double getTwistLimit() { return twistLimit; }

	/** Marks this joint as removed from the physical world. */
	public void markDestroyed() {
		destroyed = true;
	}

	@getter ("destroyed")
	public boolean isDestroyed() { return destroyed; }

	/**
	 * Gets the current angle of the joint, in radians (hinge and revolute-like joints). Returns 0 for joints without a
	 * rotational degree of freedom.
	 */
	@getter ("angle")
	public double getAngle() {
		if (destroyed || joint == null) return 0d;
		if (joint instanceof org.jbox2d.dynamics.joints.RevoluteJoint revolute) return revolute.getJointAngle();
		if (joint instanceof com.bulletphysics.dynamics.constraintsolver.HingeConstraint hinge)
			return hinge.getHingeAngle();
		if (joint instanceof com.jme3.bullet.joints.HingeJoint hinge) return hinge.getHingeAngle();
		return 0d;
	}

	/**
	 * Gets the current translation of the joint along its axis, in world units (slider and wheel joints). Returns 0
	 * for joints without a translational degree of freedom.
	 */
	@getter ("translation")
	public double getTranslation() {
		if (destroyed || joint == null) return 0d;
		if (joint instanceof org.jbox2d.dynamics.joints.PrismaticJoint prismatic)
			return fromBox2D(prismatic.getBodyA(), prismatic.getJointTranslation());
		if (joint instanceof org.jbox2d.dynamics.joints.WheelJoint wheel)
			return fromBox2D(wheel.getBodyA(), wheel.getJointTranslation());
		if (joint instanceof com.bulletphysics.dynamics.constraintsolver.SliderConstraint slider)
			return slider.getLinearPos();
		if (joint instanceof com.jme3.bullet.joints.SliderJoint slider) return nativeSliderTranslation(slider);
		return 0d;
	}

	private static double fromBox2D(final org.jbox2d.dynamics.Body body, final double value) {
		return body.getUserData() instanceof gama.extension.physics.box2d_version.IBox2DPhysicalEntity box2d
				? box2d.toGama((float) value) : value;
	}

	private static double nativeSliderTranslation(final com.jme3.bullet.joints.SliderJoint slider) {
		if (!(slider.getBodyA() instanceof com.jme3.bullet.objects.PhysicsRigidBody a)
				|| !(slider.getBodyB() instanceof com.jme3.bullet.objects.PhysicsRigidBody b))
			return 0d;
		com.jme3.math.Transform frameA = slider.getFrameTransform(com.jme3.bullet.joints.JointEnd.A,
				new com.jme3.math.Transform());
		com.jme3.math.Transform frameB = slider.getFrameTransform(com.jme3.bullet.joints.JointEnd.B,
				new com.jme3.math.Transform());
		com.jme3.math.Quaternion rotA = a.getPhysicsRotation(new com.jme3.math.Quaternion());
		com.jme3.math.Quaternion rotB = b.getPhysicsRotation(new com.jme3.math.Quaternion());
		com.jme3.math.Vector3f pointA = a.getPhysicsLocation(new com.jme3.math.Vector3f())
				.add(rotA.mult(frameA.getTranslation()));
		com.jme3.math.Vector3f pointB = b.getPhysicsLocation(new com.jme3.math.Vector3f())
				.add(rotB.mult(frameB.getTranslation()));
		com.jme3.math.Vector3f axis = rotA.mult(frameA.getRotation()).mult(com.jme3.math.Vector3f.UNIT_X);
		return pointB.subtract(pointA).dot(axis);
	}

	@Override
	public JointType getJointType() { return type; }

	@getter ("type")
	public String getType() { return type.name().toLowerCase(java.util.Locale.ROOT); }

	@getter ("bodyA")
	@Override
	public Object getBodyA() { return bodyA; }

	@getter ("bodyB")
	@Override
	public Object getBodyB() { return bodyB; }

	@getter ("anchor")
	@Override
	public IPoint getAnchorPoint() { return anchor; }

	@getter ("axis")
	@Override
	public IPoint getAxis() { return axis; }

	@getter ("lowerLimit")
	@Override
	public double getLowerLimit() { return lowerLimit; }

	@getter ("upperLimit")
	@Override
	public double getUpperLimit() { return upperLimit; }

	@Override
	public boolean hasLimits() { return hasLimits; }

	@getter ("motorSpeed")
	@Override
	public double getMotorSpeed() { return motorSpeed; }

	@setter ("motorSpeed")
	public void setMotorSpeed(final Double speed) {
		if (speed != null && !Double.isFinite(speed)) {
			throw new IllegalArgumentException("Joint motor speed must be finite");
		}
		motorSpeed = speed == null ? 0d : speed;
		if (joint == null || destroyed) return;
		if (joint instanceof org.jbox2d.dynamics.joints.RevoluteJoint revolute) {
			revolute.enableMotor(maxMotorForce > 0);
			revolute.setMotorSpeed((float) motorSpeed);
			revolute.setMaxMotorTorque((float) maxMotorForce);
		} else if (joint instanceof org.jbox2d.dynamics.joints.PrismaticJoint prismatic) {
			prismatic.enableMotor(maxMotorForce > 0);
			Object wrapper = prismatic.getBodyA().getUserData();
			float motorTarget = wrapper instanceof gama.extension.physics.box2d_version.IBox2DPhysicalEntity box2d
					? box2d.toBox2D(motorSpeed) : (float) motorSpeed;
			prismatic.setMotorSpeed(motorTarget);
			prismatic.setMaxMotorForce((float) maxMotorForce);
		} else if (joint instanceof org.jbox2d.dynamics.joints.WheelJoint wheel) {
			wheel.enableMotor(maxMotorForce > 0);
			wheel.setMotorSpeed((float) motorSpeed);
			wheel.setMaxMotorTorque((float) maxMotorForce);
		} else if (joint instanceof com.bulletphysics.dynamics.constraintsolver.HingeConstraint hinge) {
			hinge.enableAngularMotor(maxMotorForce > 0, (float) motorSpeed, (float) maxMotorForce);
		} else if (joint instanceof com.bulletphysics.dynamics.constraintsolver.SliderConstraint slider) {
			slider.setPoweredLinMotor(maxMotorForce > 0);
			slider.setTargetLinMotorVelocity((float) motorSpeed);
			slider.setMaxLinMotorForce((float) maxMotorForce);
		} else if (joint instanceof com.jme3.bullet.joints.HingeJoint hinge) {
			hinge.enableMotor(maxMotorForce > 0, (float) motorSpeed, (float) maxMotorForce);
		} else if (joint instanceof com.jme3.bullet.joints.SliderJoint slider) {
			slider.setPoweredLinMotor(maxMotorForce > 0);
			slider.setTargetLinMotorVelocity((float) motorSpeed);
			slider.setMaxLinMotorForce((float) maxMotorForce);
		}
	}

	@getter ("maxMotorForce")
	@Override
	public double getMaxMotorForce() { return maxMotorForce; }

	@setter ("maxMotorForce")
	public void setMaxMotorForce(final Double force) {
		if (force != null && (!Double.isFinite(force) || force < 0)) {
			throw new IllegalArgumentException("Joint motor force must be finite and non-negative");
		}
		maxMotorForce = force == null ? 0d : Math.max(0, force);
		setMotorSpeed(motorSpeed);
	}

	/**
	 * Gets the underlying joint object.
	 *
	 * @return the joint object
	 */
	public Object getJoint() { return joint; }

	/**
	 * Returns a string representation of the joint.
	 *
	 * @return a string representation
	 */
	@Override
	public String toString() {
		return "GamaJoint{" + "type=" + type + ", bodyA=" + bodyA + ", bodyB=" + bodyB + ", anchor=" + anchor
				+ ", axis=" + axis
				+ ", lowerLimit=" + lowerLimit + ", upperLimit=" + upperLimit + ", motorSpeed=" + motorSpeed
				+ ", maxMotorForce=" + maxMotorForce + '}';
	}
}