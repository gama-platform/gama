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

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.metamodel.shape.GamaPoint;
import gama.extension.physics.common.IJointDefinition;
import gama.gaml.types.IType;

/**
 * A wrapper class for joints created in the physical world.
 */
@vars ({ @variable (
		name = "type",
		type = IType.STRING,
		doc = @doc ("The type of the joint (e.g., hinge, slider, etc.).")),
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
				name = "lowerLimit",
				type = IType.FLOAT,
				doc = @doc ("The lower limit of the joint, if applicable.")),
		@variable (
				name = "upperLimit",
				type = IType.FLOAT,
				doc = @doc ("The upper limit of the joint, if applicable.")),
		@variable (
				name = "motorSpeed",
				type = IType.FLOAT,
				doc = @doc ("The motor speed of the joint, if applicable.")),
		@variable (
				name = "maxMotorForce",
				type = IType.FLOAT,
				doc = @doc ("The maximum motor force of the joint, if applicable.")) })
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
	private final GamaPoint anchor;

	/** The lower limit. */
	private final double lowerLimit;

	/** The upper limit. */
	private final double upperLimit;

	/** The motor speed. */
	private final double motorSpeed;

	/** The max motor force. */
	private final double maxMotorForce;

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
	 * @param lowerLimit
	 *            the lower limit of the joint, if applicable
	 * @param upperLimit
	 *            the upper limit of the joint, if applicable
	 * @param motorSpeed
	 *            the motor speed of the joint, if applicable
	 * @param maxMotorForce
	 *            the maximum motor force of the joint, if applicable
	 */
	public GamaJoint(final Object joint, final JointType type, final Object bodyA, final Object bodyB,
			final GamaPoint anchor, final double lowerLimit, final double upperLimit, final double motorSpeed,
			final double maxMotorForce) {
		this.joint = joint;
		this.type = type;
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		this.anchor = anchor;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.motorSpeed = motorSpeed;
		this.maxMotorForce = maxMotorForce;
	}

	@getter ("type")
	@Override
	public JointType getJointType() { return type; }

	@getter ("bodyA")
	@Override
	public Object getBodyA() { return bodyA; }

	@getter ("bodyB")
	@Override
	public Object getBodyB() { return bodyB; }

	@getter ("anchor")
	@Override
	public GamaPoint getAnchorPoint() { return anchor; }

	@getter ("lowerLimit")
	@Override
	public double getLowerLimit() { return lowerLimit; }

	@getter ("upperLimit")
	@Override
	public double getUpperLimit() { return upperLimit; }

	@getter ("motorSpeed")
	@Override
	public double getMotorSpeed() { return motorSpeed; }

	@getter ("maxMotorForce")
	@Override
	public double getMaxMotorForce() { return maxMotorForce; }

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
				+ ", lowerLimit=" + lowerLimit + ", upperLimit=" + upperLimit + ", motorSpeed=" + motorSpeed
				+ ", maxMotorForce=" + maxMotorForce + '}';
	}
}