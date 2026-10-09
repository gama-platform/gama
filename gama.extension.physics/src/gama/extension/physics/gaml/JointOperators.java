/*******************************************************************************************************
 *
 * JointOperators.java, in gama.extension.physics, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.gaml;

import gama.annotations.doc;
import gama.annotations.operator;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;

/** GAML operators for creating and destroying joints in a physical simulation. */
public final class JointOperators {

	private JointOperators() {}

	private static PhysicalSimulationAgent getPhysicalSimulation(final IScope scope) {
		if (scope != null && scope.getSimulation() instanceof PhysicalSimulationAgent simulation) return simulation;
		throw GamaRuntimeException.error("Joint operators can only be used in a physical simulation", scope);
	}

	@operator (
			doc = @doc ("Creates and adds a hinge joint around the world Z axis. Limits are angles in radians; motor speed is radians per second. A positive maximum motor force enables the motor."),
			value = "create_hinge_joint")
	public static GamaJoint createHingeJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final Double lowerLimit, final Double upperLimit, final Double motorSpeed,
			final Double maxMotorForce) {
		return getPhysicalSimulation(scope).createHingeJoint(scope, bodyA, bodyB, anchor, lowerLimit, upperLimit,
				motorSpeed, maxMotorForce);
	}

	@operator (
			doc = @doc ("Creates and adds a hinge joint around the supplied world-space axis. Limits are in radians and motor speed is radians per second."),
			value = "create_hinge_joint_with_axis")
	public static GamaJoint createHingeJointWithAxis(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final IPoint axis, final Double lowerLimit, final Double upperLimit,
			final Double motorSpeed, final Double maxMotorForce) {
		return getPhysicalSimulation(scope).createHingeJointWithAxis(scope, bodyA, bodyB, anchor, axis, lowerLimit,
				upperLimit, motorSpeed, maxMotorForce);
	}

	@operator (
			doc = @doc ("Creates and adds a slider joint along the world-space x axis. Limits are distances."),
			value = "create_slider_joint")
	public static GamaJoint createSliderJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final Double lowerLimit, final Double upperLimit) {
		return getPhysicalSimulation(scope).createSliderJoint(scope, bodyA, bodyB, anchor, lowerLimit, upperLimit);
	}

	@operator (
			doc = @doc ("Creates and adds a slider joint along the supplied world-space axis. Limits are distances."),
			value = "create_slider_joint_with_axis")
	public static GamaJoint createSliderJointWithAxis(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final IPoint axis, final Double lowerLimit, final Double upperLimit,
			final Double motorSpeed, final Double maxMotorForce) {
		return getPhysicalSimulation(scope).createSliderJointWithAxis(scope, bodyA, bodyB, anchor, axis, lowerLimit,
				upperLimit, motorSpeed, maxMotorForce);
	}

	@operator (
			doc = @doc ("Creates and adds a ball-and-socket joint at the supplied world-space anchor."),
			value = "create_ball_and_socket_joint")
	public static GamaJoint createBallAndSocketJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor) {
		return getPhysicalSimulation(scope).createBallAndSocketJoint(scope, bodyA, bodyB, anchor);
	}

	@operator (
			doc = @doc ("Creates and adds a fixed (weld) joint at the supplied world-space anchor."),
			value = "create_fixed_joint")
	public static GamaJoint createFixedJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor) {
		return getPhysicalSimulation(scope).createFixedJoint(scope, bodyA, bodyB, anchor);
	}

	@operator (
			doc = @doc ("Creates and adds a distance (spring) joint between two world-space anchors, with a spring frequency (Hz, 0 = rigid) and damping ratio. Box2D only."),
			value = "create_distance_joint")
	public static GamaJoint createDistanceJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchorA, final IPoint anchorB, final double frequency, final double damping) {
		return getPhysicalSimulation(scope).createDistanceJoint(scope, bodyA, bodyB, anchorA, anchorB, frequency,
				damping);
	}

	@operator (
			doc = @doc ("Creates and adds a rope joint keeping two world-space anchors at most maxLength apart. Box2D only."),
			value = "create_rope_joint")
	public static GamaJoint createRopeJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchorA, final IPoint anchorB, final double maxLength) {
		return getPhysicalSimulation(scope).createRopeJoint(scope, bodyA, bodyB, anchorA, anchorB, maxLength);
	}

	@operator (
			doc = @doc ("Creates and adds a cone-twist joint (ragdoll shoulder) at the anchor, around the axis, with swing and twist limits in radians. Bullet engines only."),
			value = "create_cone_twist_joint")
	public static GamaJoint createConeTwistJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final IPoint axis, final double swingLimit, final double twistLimit) {
		return getPhysicalSimulation(scope).createConeTwistJoint(scope, bodyA, bodyB, anchor, axis, swingLimit,
				twistLimit);
	}

	@operator (
			doc = @doc ("Creates and adds a wheel joint (suspension + rotation) at the anchor along the axis, with suspension frequency/damping and a motor. Box2D only."),
			value = "create_wheel_joint")
	public static GamaJoint createWheelJoint(final IScope scope, final Object bodyA, final Object bodyB,
			final IPoint anchor, final IPoint axis, final double frequency, final double damping,
			final double motorSpeed, final double maxMotorTorque) {
		return getPhysicalSimulation(scope).createWheelJoint(scope, bodyA, bodyB, anchor, axis, frequency, damping,
				motorSpeed, maxMotorTorque);
	}

	@operator (
			doc = @doc ("Destroys a joint, removing it from the physical world."),
			value = "destroy_joint")
	public static boolean destroyJoint(final IScope scope, final GamaJoint joint) {
		return getPhysicalSimulation(scope).destroyJoint(joint);
	}

}
