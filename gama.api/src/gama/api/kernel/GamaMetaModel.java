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

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Sets.newHashSet;
import static gama.api.constants.IKeyword.AGENT;
import static gama.api.constants.IKeyword.EXPERIMENT;
import static gama.api.constants.IKeyword.MODEL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import gama.api.additions.GamaBundleLoader;
import gama.api.compilation.descriptions.IDescriptionFactory;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.constants.IKeyword;
import gama.api.gaml.GAML;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.simulation.IExperimentAgent;
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

/**
 * The Class GamaMetaModel.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMetaModel {

	/** The Constant INSTANCE. */
	public final static GamaMetaModel INSTANCE = new GamaMetaModel();

	/** The temp species. */
	private final Map<String, SpeciesRecord> tempSpecies = new HashMap();

	/** The species skills. */
	private final Multimap<String, String> speciesSkills = HashMultimap.create();

	/** The built in species. */
	private final Map<String, ISpecies> builtInSpecies = new HashMap<>();

	/** The built in species descriptions. */
	private final Map<String, ISpeciesDescription> builtInSpeciesDescriptions = new HashMap<>();

	/** The is initialized. */
	public volatile boolean isInitialized;

	/**
	 * The Class SpeciesRecord.
	 */
	private record SpeciesRecord(String name, String plugin, Class clazz, IAgentConstructor helper, String[] skills) {}

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
		tempSpecies.put(name, new SpeciesRecord(name, GamaBundleLoader.CURRENT_PLUGIN_NAME, clazz, helper, skills));
	}

	/**
	 * Gets the species description.
	 *
	 * @param name
	 *            the name
	 * @return the species description
	 */
	public static ISpeciesDescription getSpeciesDescription(final String name) {
		return INSTANCE.builtInSpeciesDescriptions.get(name);
	}

	/**
	 * Gets the species.
	 *
	 * @param name
	 *            the name
	 * @return the species
	 */
	public static ISpecies getSpecies(final String name) {
		ISpecies s = INSTANCE.builtInSpecies.get(name);
		if (s == null) {
			ISpeciesDescription sd = getSpeciesDescription(name);
			if (sd == null) return null;
			s = sd.compileAsBuiltIn();
			INSTANCE.builtInSpecies.put(name, s);
		}
		return s;
	}

	/**
	 * Builds the.
	 */
	public void build() {
		// We first build "agent" as the root of all other species (incl.
		// "model")
		final SpeciesRecord ap = tempSpecies.remove(AGENT);
		// "agent" has no super-species yet
		ISpeciesDescription agent = buildSpecies(ap, null, null);

		// We then build "model", sub-species of "agent"
		final SpeciesRecord wp = tempSpecies.remove(MODEL);
		IModelDescription model = (IModelDescription) buildSpecies(wp, null, null);
		// We close the first loop by putting agent "inside" model
		agent.setEnclosingDescription(model);
		model.addChild(agent);

		// We create "experiment" as the root of all experiments, sub-species of
		// "agent"
		final SpeciesRecord ep = tempSpecies.remove(EXPERIMENT);
		IExperimentDescription experiment = (IExperimentDescription) buildSpecies(ep, null, agent);
		experiment.finalizeDescription();

		// We now can attach "experiment" as a child of "model"
		model.addChild(experiment);

		// We then create all other built-in species and attach them to "model"
		for (final SpeciesRecord proto : tempSpecies.values()) { model.addChild(buildSpecies(proto, model, agent)); }
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
	private ISpeciesDescription buildSpecies(final SpeciesRecord proto, final ISpeciesDescription macro,
			final ISpeciesDescription parent) {
		Set<String> skills = newHashSet(concat(Arrays.asList(proto.skills), speciesSkills.get(proto.name)));
		ISpeciesDescription desc = create(macro, parent, proto.clazz, proto.name, proto.helper, proto.plugin, skills);
		desc.copyJavaAdditions();
		desc.inheritFromParent();
		builtInSpeciesDescriptions.put(proto.name, desc);
		return desc;
	}

	/**
	 * Builds the species.
	 *
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param clazz
	 *            the clazz
	 * @param isModel
	 *            the is model
	 * @param isExperiment
	 *            the is experiment
	 * @param name
	 *            the name
	 * @param helper
	 *            the helper
	 * @param plugin
	 *            the plugin
	 * @param skills
	 *            the skills
	 * @return the i species description
	 */
	private ISpeciesDescription create(final ISpeciesDescription macro, final ISpeciesDescription parent,
			final Class clazz, final String name, final IAgentConstructor helper, final String plugin,
			final Set<String> skills) {
		IDescriptionFactory factory = GAML.getDescriptionFactory();
		if (IKeyword.PLATFORM.equals(name))
			return factory.createPlatformSpeciesDescription(name, clazz, macro, parent, helper, skills, plugin);
		if (ISimulationAgent.class.isAssignableFrom(clazz)) // macro represents the parent here (except for root)
			return factory.createRootModelDescription(name, clazz, macro, parent, helper, skills, plugin);
		if (IExperimentAgent.class.isAssignableFrom(clazz))
			return factory.createBuiltInExperimentDescription(name, clazz, macro, parent, helper, skills, plugin);
		return factory.createBuiltInSpeciesDescription(name, clazz, macro, parent, helper, skills, plugin);

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

}
