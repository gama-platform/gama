package gama.extension.physics.common;

import gama.api.types.geometry.IPoint;

/**
 * Interface representing a generic joint definition for physics engines.
 */
public interface IJointDefinition {

    /**
     * Enum for the type of joint.
     */
    enum JointType { HINGE, SLIDER, BALL_AND_SOCKET, FIXED, DISTANCE, ROPE, CONE_TWIST, WHEEL }

    /**
     * Gets the type of the joint.
     *
     * @return the joint type
     */
    JointType getJointType();

    /**
     * Gets the first body connected by the joint.
     *
     * @return the first body
     */
    Object getBodyA();

    /**
     * Gets the second body connected by the joint.
     *
     * @return the second body
     */
    Object getBodyB();

    /**
     * Gets the anchor point for the joint in world coordinates.
     *
     * @return the anchor point
     */
    IPoint getAnchorPoint();

    /**
     * Gets the joint axis in world coordinates.
     *
     * @return the joint axis
     */
    IPoint getAxis();

    /**
     * Gets the lower limit of the joint, if applicable.
     *
     * @return the lower limit
     */
    double getLowerLimit();

    /**
     * Gets the upper limit of the joint, if applicable.
     *
     * @return the upper limit
     */
    double getUpperLimit();

    /**
     * Whether this joint has lower and upper limits.
     *
     * @return true when limits are enabled
     */
    boolean hasLimits();

    /**
     * Gets the motor speed for the joint, if applicable.
     *
     * @return the motor speed
     */
    double getMotorSpeed();

    /**
     * Gets the maximum motor force for the joint, if applicable.
     *
     * @return the maximum motor force
     */
    double getMaxMotorForce();

    /**
     * Gets the anchor on the second body (world coordinates). Only distinct from the first anchor for distance and
     * rope joints.
     *
     * @return the second anchor point
     */
    default IPoint getSecondAnchorPoint() { return getAnchorPoint(); }

    /**
     * Gets the spring frequency in Hz (distance, wheel and fixed joints). A value of 0 means a rigid joint.
     *
     * @return the frequency
     */
    default double getFrequency() { return 0; }

    /**
     * Gets the spring damping ratio (distance, wheel and fixed joints).
     *
     * @return the damping ratio
     */
    default double getDamping() { return 0; }

    /**
     * Gets the cone-twist swing limit (half-angle of the cone, in radians).
     *
     * @return the swing limit
     */
    default double getSwingLimit() { return 0; }

    /**
     * Gets the cone-twist twist limit (in radians).
     *
     * @return the twist limit
     */
    default double getTwistLimit() { return 0; }
}