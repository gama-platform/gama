/*******************************************************************************************************
 *
 * IExperimentDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gama.annotations.constants.IKeyword;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentAgentCreator;
import gama.api.kernel.simulation.IExperimentAgentCreator.ExperimentAgentDescription;

/**
 *
 */
public interface IExperimentDescription extends ISpeciesDescription {

	/** The experiment creators. */
	Map<String, IExperimentAgentCreator> CREATORS = new HashMap<>();

	/**
	 * Creates the experiment agent.
	 *
	 * @param name
	 *            the name
	 * @param pop
	 *            the pop
	 * @param index
	 *            the index
	 * @return the experiment agent
	 */
	static IExperimentAgent createExperimentAgent(final String name, final IPopulation pop, final int index) {
		return CREATORS.get(name).create(pop, index);
	}

	/**
	 * Adds the experiment agent creator.
	 *
	 * @param key
	 *            the key
	 * @param creator
	 *            the creator
	 */
	static void addExperimentAgentCreator(final String key, final IExperimentAgentCreator creator) {
		CREATORS.put(key, creator);
	}

	/**
	 * Gets the experiment types.
	 *
	 * @return the experiment types
	 */
	static Set<String> getExperimentTypes() { return CREATORS.keySet(); }

	/**
	 * Gets the java base for.
	 *
	 * @param type
	 *            the type
	 * @return the java base for
	 */
	static Class<? extends IExperimentAgent> getJavaBaseFor(final String type) {
		IExperimentAgentCreator creator = CREATORS.get(type == null ? IKeyword.GUI_ : type);
		if (!(creator instanceof ExperimentAgentDescription desc)) return IExperimentAgent.class;
		return (Class<? extends IExperimentAgent>) desc.getJavaBase();
	}

	/**
	 * Gets the experiment creator.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param type
	 *            the type
	 * @return the experiment creator
	 * @date 2 janv. 2024
	 */
	static IExperimentAgentCreator getExperimentCreator(final String type) {
		return CREATORS.get(type);
	}

	/**
	 * @return
	 */
	boolean isBatch();

	/**
	 * @return
	 */
	boolean isMemorize();

	/**
	 * @return
	 */
	String getExperimentTitleFacet();

	/**
	 * @return
	 */
	boolean isTest();

	/** Whether a declaration explicitly belongs to the experiment, including the legacy permanent output block. */
	static boolean isExperimentContext(final IDescription description) {
		return IKeyword.PERMANENT.equals(description.getKeyword())
				|| IKeyword.EXPERIMENT.equals(description.getLitteral(IKeyword.OF));
	}

	/** Keeps experiment parameters separate from simulation parameters with the same target name. */
	static String parameterKey(final String name, final boolean experiment) {
		return experiment ? "(Experiment) " + name : name;
	}

	/** An omitted owner always means simulation, without an implicit experiment fallback. */
	default IVariableDescription getParameterTarget(final String name) {
		return getParameterTarget(name, false);
	}

	default IVariableDescription getParameterTarget(final String name, final boolean experiment) {
		if (experiment) return getAttribute(name);
		final IModelDescription model = getModelDescription();
		return model == null ? null : model.getAttribute(name);
	}

}
