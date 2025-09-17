/*******************************************************************************************************
 *
 * IPhysicalWorld.java, in gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.physics.common;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.extension.physics.gaml.PhysicalSimulationAgent;

/**
 * The link between a simulation agent and the physical world / space, as defined by Bullet.
 *
 * @author drogoul
 *
 */
public interface IPhysicalWorld<WorldType, ShapeType, VectorType> extends IPhysicalEntity<VectorType> {

	/**
	 * Gets the shape converter.
	 *
	 * @return the shape converter
	 */
	IShapeConverter<ShapeType, VectorType> getShapeConverter();

	/**
	 * Do step.
	 *
	 * @param timeStep the time step
	 * @param maxSubSteps the max sub steps
	 */
	void doStep(Double timeStep, int maxSubSteps);

	/**
	 * Register agent.
	 *
	 * @param agent the agent
	 */
	void registerAgent(IAgent agent);

	/**
	 * Unregister agent.
	 *
	 * @param agent the agent
	 */
	void unregisterAgent(IAgent agent);

	/**
	 * Update agent shape.
	 *
	 * @param agent the agent
	 */
	void updateAgentShape(IAgent agent);

	/**
	 * Sets the ccd.
	 *
	 * @param ccd the new ccd
	 */
	void setCCD(boolean ccd);

	/**
	 * Sets the gravity.
	 *
	 * @param gravity the new gravity
	 */
	void setGravity(GamaPoint gravity);

	/**
	 * Dispose.
	 */
	void dispose();

	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	WorldType getWorld();

	/**
	 * Gets the simulation.
	 *
	 * @return the simulation
	 */
	PhysicalSimulationAgent getSimulation();

	/**
	 * Update positions and rotations.
	 */
	void updatePositionsAndRotations();

	/**
	 * Creates a joint in the physical world.
	 *
	 * @param jointDefinition the joint definition containing the configuration
	 * @return the created joint object
	 */
	Object createJoint(IJointDefinition jointDefinition);
}