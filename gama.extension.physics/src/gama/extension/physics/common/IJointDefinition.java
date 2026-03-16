package gama.extension.physics.common;

import gama.core.metamodel.shape.GamaPoint;

/**
 * Interface representing a generic joint definition for physics engines.
 */
public interface IJointDefinition {

    /**
     * Enum for the type of joint.
     */
    enum JointType {
        HINGE,
        SLIDER,
        DISTANCE,
        FIXED,
        BALL_AND_SOCKET
    }

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
    GamaPoint getAnchorPoint();

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
}