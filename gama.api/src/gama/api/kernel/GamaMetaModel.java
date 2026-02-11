/*******************************************************************************************************
 *
 * GamaMetaModel.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel;

import static gama.api.constants.IKeyword.AGENT;
import static gama.api.constants.IKeyword.EXPERIMENT;
import static gama.api.constants.IKeyword.MODEL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import gama.api.additions.GamaBundleLoader;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ISpeciesDescription.Platform;
import gama.api.constants.IKeyword;
import gama.api.gaml.GAML;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.ISpecies;

/**
 * The Class GamaMetaModel.
 */

/**
 * The Class GamaMetaModel.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 janv. 2024
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMetaModel {

	/** The Constant INSTANCE. */
	public final static GamaMetaModel INSTANCE = new GamaMetaModel();

	/** The temp species. */
	private final Map<String, SpeciesProto> tempSpecies = new HashMap();

	/** The species skills. */
	private final Multimap<String, String> speciesSkills = HashMultimap.create();

	/** The abstract model species. */
	private ISpecies abstractModelSpecies;

	/** The abstract agent species. */
	private ISpecies abstractAgentSpecies;

	/** The is initialized. */
	public volatile boolean isInitialized;

	/** The experiment. */
	private ISpeciesDescription agent;

	/** The model. */
	private IModelDescription model;

	/** The experiment. */
	private IExperimentDescription experiment;

	/** The platform. */
	private ISpeciesDescription.Platform platform;

	/**
	 * Gets the agent species description.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the agent species description
	 * @date 15 janv. 2024
	 */
	public static ISpeciesDescription getAgentSpeciesDescription() { return INSTANCE.agent; }

	/**
	 * Gets the model description.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the model description
	 * @date 15 janv. 2024
	 */
	public static IModelDescription getModelDescription() { return INSTANCE.model; }

	/**
	 * Gets the experiment description.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the experiment description
	 * @date 15 janv. 2024
	 */
	public static IExperimentDescription getExperimentDescription() { return INSTANCE.experiment; }

	/**
	 * Gets the platform species description.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the platform species description
	 * @date 15 janv. 2024
	 */
	public static ISpeciesDescription.Platform getPlatformSpeciesDescription() { return INSTANCE.platform; }

	/**
	 * The Class SpeciesProto.
	 */
	private static class SpeciesProto {

		/** The name. */
		final String name;

		/** The plugin. */
		final String plugin;

		/** The clazz. */
		final Class clazz;

		/** The helper. */
		final IAgentConstructor helper;

		/** The skills. */
		final String[] skills;

		/**
		 * Instantiates a new species proto.
		 *
		 * @param name
		 *            the name
		 * @param clazz
		 *            the clazz
		 * @param helper
		 *            the helper
		 * @param skills
		 *            the skills
		 */
		public SpeciesProto(final String name, final Class clazz, final IAgentConstructor helper,
				final String[] skills) {
			plugin = GamaBundleLoader.CURRENT_PLUGIN_NAME;
			this.name = name;
			this.clazz = clazz;
			this.helper = helper;
			this.skills = skills;
		}
	}

	/**
	 * Instantiates a new gama meta model.
	 */
	private GamaMetaModel() {}

	/**
	 * Adds the species.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 */
	public void addSpecies(final String name, final Class clazz, final IAgentConstructor helper,
			final String[] skills) {
		final SpeciesProto proto = new SpeciesProto(name, clazz, helper, skills);
		tempSpecies.put(name, proto);
	}

	/**
	 * Builds the.
	 */
	public void build() {

		// We first build "agent" as the root of all other species (incl.
		// "model")
		final SpeciesProto ap = tempSpecies.remove(AGENT);
		// "agent" has no super-species yet
		agent = buildSpecies(ap, null, null, false, false);

		// We then build "model", sub-species of "agent"
		final SpeciesProto wp = tempSpecies.remove(MODEL);
		model = (IModelDescription) buildSpecies(wp, null, agent, true, false);

		// We close the first loop by putting agent "inside" model
		agent.setEnclosingDescription(model);
		model.addChild(agent);

		// We create "experiment" as the root of all experiments, sub-species of
		// "agent"
		final SpeciesProto ep = tempSpecies.remove(EXPERIMENT);
		experiment = (IExperimentDescription) buildSpecies(ep, null, agent, false, true);
		experiment.finalizeDescription();
		// Types.builtInTypes.addSpeciesType(experiment);

		// We now can attach "model" as a micro-species of "experiment"
		// model.setEnclosingDescription(experiment);
		model.addChild(experiment);

		// We then create all other built-in species and attach them to "model"
		for (final SpeciesProto proto : tempSpecies.values()) {
			model.addChild(
					buildSpecies(proto, model, agent, ISimulationAgent.class.isAssignableFrom(proto.clazz), false));
		}
		tempSpecies.clear();
		model.buildTypes();
		model.finalizeDescription();
		isInitialized = true;
	}

	/**
	 * Builds the species.
	 *
	 * @param proto
	 *            the proto
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param isModel
	 *            the is model
	 * @param isExperiment
	 *            the is experiment
	 * @return the species description
	 */
	public ISpeciesDescription buildSpecies(final SpeciesProto proto, final ISpeciesDescription macro,
			final ISpeciesDescription parent, final boolean isModel, final boolean isExperiment) {
		final Class clazz = proto.clazz;
		final String name = proto.name;
		final IAgentConstructor helper = proto.helper;
		final String[] skills = proto.skills;
		final String plugin = proto.plugin;
		final Set<String> allSkills = new HashSet(Arrays.asList(skills));
		allSkills.addAll(speciesSkills.get(name));
		ISpeciesDescription desc;
		if (IKeyword.PLATFORM.equals(name)) {
			desc = GAML.getDescriptionFactory().createPlatformSpeciesDescription(name, clazz, macro, parent, helper,
					allSkills, plugin);
			platform = (Platform) desc;
		} else if (!isModel) {
			if (isExperiment) {
				desc = GAML.getDescriptionFactory().createBuiltInExperimentDescription(name, clazz, macro, parent,
						helper, allSkills, plugin);
			} else {
				desc = GAML.getDescriptionFactory().createBuiltInSpeciesDescription(name, clazz, macro, parent, helper,
						allSkills, plugin);
			}
		} else {
			// if it is a ModelDescription, then the macro represents the parent (except for root)
			desc = GAML.getDescriptionFactory().createRootModelDescription(name, clazz, macro, parent, helper,
					allSkills, plugin);
		}
		desc.copyJavaAdditions();
		desc.inheritFromParent();
		return desc;
	}

	/**
	 * Adds the species skill.
	 *
	 * @param spec
	 *            the spec
	 * @param name
	 *            the name
	 */
	public void addSpeciesSkill(final String spec, final String name) {
		speciesSkills.put(spec, name);
	}

	/**
	 * Gets the abstract model species.
	 *
	 * @return the abstract model species
	 */
	public ISpecies getAbstractModelSpecies() {
		if (abstractModelSpecies == null) { abstractModelSpecies = (ISpecies) getModelDescription().compile(); }
		return abstractModelSpecies;
	}

	/**
	 * Gets the abstract agent species.
	 *
	 * @return the abstract agent species
	 */
	public ISpecies getAbstractAgentSpecies() {
		if (abstractAgentSpecies == null) { abstractAgentSpecies = (ISpecies) getAgentSpeciesDescription().compile(); }
		return abstractAgentSpecies;
	}

}
