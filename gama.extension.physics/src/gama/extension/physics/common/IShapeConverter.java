/*******************************************************************************************************
 *
 * IShapeConverter.java, in gama.extension.physics, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.common;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.grid.IGrid;
import gama.core.runtime.IScope;
import gama.core.util.matrix.IField;
import gama.extension.physics.gaml.PhysicalSimulationAgent;

/**
 * The Interface IShapeConverter.
 *
 * @param <ShapeType>
 *            the generic type
 * @param <VectorType>
 *            the generic type
 */
public interface IShapeConverter<ShapeType, VectorType> extends IPhysicalEntity<VectorType> {

	/**
	 * To floats.
	 *
	 * @param array
	 *            the array
	 * @return the float[]
	 */
	default float[] toFloats(final double[] array) {
		float[] result = new float[array.length];
		for (int i = 0; i < array.length; ++i) { result[i] = (float) array[i]; }
		return result;
	}

	/**
	 * Compute depth.
	 *
	 * @param agent
	 *            the agent
	 * @return the float
	 */
	default float computeDepth(final IAgent agent) {
		if (agent.getSpecies().isGrid())
			return (float) ((IGrid) agent.getPopulation().getTopology().getPlaces()).getValue(agent.getIndex());
		Double d = agent.getDepth();
		float result = d == null ? 0f : d.floatValue();
		// Depth cannot be negative as it is used for the half-extents of shapes
		return result < 0 ? 0f : result;
	}

	/**
	 * Compute type.
	 *
	 * @param agent
	 *            the agent
	 * @return the i shape. type
	 */
	default IShape.Type computeType(final IAgent agent) {
		if (agent.getSpecies().isGrid()) return IShape.Type.BOX;
		return agent.getGeometricalType();
	}

	/**
	 * Convert and translate.
	 *
	 * @param agent
	 *            the agent
	 * @param aabbTranslation
	 *            the aabb translation
	 * @param visualTranslation
	 *            the visual translation
	 * @return the shape type
	 */
	default ShapeType convertAndTranslate(final IAgent agent, final VectorType aabbTranslation,
			final VectorType visualTranslation) {
		IShape.Type type = computeType(agent);
		float depth = computeDepth(agent);
		computeTranslation(agent, type, depth, aabbTranslation, visualTranslation);
		if (agent instanceof PhysicalSimulationAgent) {
			IField terrain = ((PhysicalSimulationAgent) agent).getTerrain();
			if (terrain != null)
				return convertTerrain(agent.getScope(), terrain, agent.getWidth(), agent.getHeight(), depth);
		}
		return convertShape(agent.getGeometry(), type, depth);

	}

	/**
	 * Compute translation.
	 *
	 * @param agent
	 *            the agent
	 * @param type
	 *            the type
	 * @param depth
	 *            the depth
	 * @param aabbTranslation
	 *            the aabb translation
	 * @param visualTranslation
	 *            the visual translation
	 */
	void computeTranslation(final IAgent agent, final IShape.Type type, final float depth,
			final VectorType aabbTranslation, final VectorType visualTranslation);

	/**
	 * Convert shape.
	 *
	 * @param shape
	 *            the shape
	 * @param type
	 *            the type
	 * @param depth
	 *            the depth
	 * @return the shape type
	 */
	ShapeType convertShape(final IShape shape, final IShape.Type type, final float depth);

	/**
	 * Convert terrain.
	 *
	 * @param scope
	 *            the scope
	 * @param field
	 *            the field
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param depth
	 *            the depth
	 * @return the shape type
	 */
	ShapeType convertTerrain(final IScope scope, final IField field, final Double width, final Double height,
			final float depth);

}
