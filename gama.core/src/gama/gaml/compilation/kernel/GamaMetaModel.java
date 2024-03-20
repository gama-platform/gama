/*******************************************************************************************************
 *
 * GamaMetaModel.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation.kernel;

import static gama.core.common.interfaces.IKeyword.AGENT;
import static gama.core.common.interfaces.IKeyword.EXPERIMENT;
import static gama.core.common.interfaces.IKeyword.MODEL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import gama.core.common.interfaces.IExperimentAgentCreator;
import gama.core.common.interfaces.IExperimentAgentCreator.ExperimentAgentDescription;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.model.GamlModelSpecies;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.population.IPopulation;
import gama.gaml.compilation.IAgentConstructor;
import gama.gaml.descriptions.ExperimentDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.PlatformSpeciesDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.species.ISpecies;
import gama.gaml.types.GamaGenericAgentType;
import gama.gaml.types.Types;

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

	/** The experiment creators. */
	private final Map<String, IExperimentAgentCreator> experimentCreators = new HashMap<>();

	/** The temp species. */
	private final Map<String, SpeciesProto> tempSpecies = new HashMap();

	/** The species skills. */
	private final Multimap<String, String> speciesSkills = HashMultimap.create();

	/** The abstract model species. */
	private GamlModelSpecies abstractModelSpecies;

	/** The abstract agent species. */
	private ISpecies abstractAgentSpecies;

	/** The is initialized. */
	public volatile boolean isInitialized;

	/** The experiment. */
	private SpeciesDescription agent;

	/** The model. */
	private ModelDescription model;

	/** The experiment. */
	private ExperimentDescription experiment;

	/** The platform. */
	private PlatformSpeciesDescription platform;

	/**
	 * Gets the agent species description.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the agent species description
	 * @date 15 janv. 2024
	 */
	public static SpeciesDescription getAgentSpeciesDescription() { return INSTANCE.agent; }

	/**
	 * Gets the model description.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the model description
	 * @date 15 janv. 2024
	 */
	public static SpeciesDescription getModelDescription() { return INSTANCE.model; }

	/**
	 * Gets the experiment description.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the experiment description
	 * @date 15 janv. 2024
	 */
	public static SpeciesDescription getExperimentDescription() { return INSTANCE.experiment; }

	/**
	 * Gets the platform species description.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the platform species description
	 * @date 15 janv. 2024
	 */
	public static PlatformSpeciesDescription getPlatformSpeciesDescription() { return INSTANCE.platform; }

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
	public ExperimentAgent createExperimentAgent(final String name, final IPopulation pop, final int index) {
		return (ExperimentAgent) experimentCreators.get(name).create(pop, index);
	}

	/**
	 * Adds the experiment agent creator.
	 *
	 * @param key
	 *            the key
	 * @param creator
	 *            the creator
	 */
	public void addExperimentAgentCreator(final String key, final IExperimentAgentCreator creator) {
		experimentCreators.put(key, creator);
	}

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
		((GamaGenericAgentType) Types.builtInTypes.get(IKeyword.AGENT)).setSpecies(agent);

		// We then build "model", sub-species of "agent"
		final SpeciesProto wp = tempSpecies.remove(MODEL);
		model = (ModelDescription) buildSpecies(wp, null, agent, true, false);

		// We close the first loop by putting agent "inside" model
		agent.setEnclosingDescription(model);
		model.addChild(agent);

		// We create "experiment" as the root of all experiments, sub-species of
		// "agent"
		final SpeciesProto ep = tempSpecies.remove(EXPERIMENT);
		experiment = (ExperimentDescription) buildSpecies(ep, null, agent, false, true);
		experiment.finalizeDescription();
		// Types.builtInTypes.addSpeciesType(experiment);

		// We now can attach "model" as a micro-species of "experiment"
		// model.setEnclosingDescription(experiment);
		model.addChild(experiment);

		// We then create all other built-in species and attach them to "model"
		for (final SpeciesProto proto : tempSpecies.values()) {
			model.addChild(
					buildSpecies(proto, model, agent, SimulationAgent.class.isAssignableFrom(proto.clazz), false));
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
	public SpeciesDescription buildSpecies(final SpeciesProto proto, final SpeciesDescription macro,
			final SpeciesDescription parent, final boolean isModel, final boolean isExperiment) {
		final Class clazz = proto.clazz;
		final String name = proto.name;
		final IAgentConstructor helper = proto.helper;
		final String[] skills = proto.skills;
		final String plugin = proto.plugin;
		final Set<String> allSkills = new HashSet(Arrays.asList(skills));
		allSkills.addAll(speciesSkills.get(name));
		SpeciesDescription desc;
		if (IKeyword.PLATFORM.equals(proto.name)) {
			platform = (PlatformSpeciesDescription) (desc = DescriptionFactory.createPlatformSpeciesDescription(name,
					clazz, macro, parent, helper, allSkills, plugin));
		} else if (!isModel) {
			if (isExperiment) {
				desc = DescriptionFactory.createBuiltInExperimentDescription(name, clazz, macro, parent, helper,
						allSkills, plugin);
			} else {
				desc = DescriptionFactory.createBuiltInSpeciesDescription(name, clazz, macro, parent, helper, allSkills,
						plugin);
			}
		} else {
			// if it is a ModelDescription, then the macro represents the parent (except for root)
			desc = DescriptionFactory.createRootModelDescription(name, clazz, macro, parent, helper, allSkills, plugin);
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
	public GamlModelSpecies getAbstractModelSpecies() {
		if (abstractModelSpecies == null) { abstractModelSpecies = (GamlModelSpecies) getModelDescription().compile(); }
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

	/**
	 * Gets the experiment types.
	 *
	 * @return the experiment types
	 */
	public Set<String> getExperimentTypes() { return experimentCreators.keySet(); }

	/**
	 * Gets the java base for.
	 *
	 * @param type
	 *            the type
	 * @return the java base for
	 */
	public Class<? extends IExperimentAgent> getJavaBaseFor(final String type) {
		IExperimentAgentCreator creator = experimentCreators.get(type);
		if (!(creator instanceof ExperimentAgentDescription desc)) return ExperimentAgent.class;
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
	public IExperimentAgentCreator getExperimentCreator(final String type) {
		return experimentCreators.get(type);
	}

}
