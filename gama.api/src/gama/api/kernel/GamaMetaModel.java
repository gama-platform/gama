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
import static gama.annotations.constants.IKeyword.AGENT;
import static gama.annotations.constants.IKeyword.EXPERIMENT;
import static gama.annotations.constants.IKeyword.MODEL;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import gama.annotations.constants.IKeyword;
import gama.api.additions.GamaBundleLoader;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.descriptions.IDescriptionFactory;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.gaml.GAML;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.object.IClass;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.ISpecies;

/**
 * The GAMA metamodel that defines and manages all built-in species in the platform.
 *
 * <p>
 * This singleton class is responsible for:
 * <ul>
 * <li>Registering built-in species during platform initialization</li>
 * <li>Building the species hierarchy (agent -> model, experiment, etc.)</li>
 * <li>Providing access to built-in species descriptions and instances</li>
 * <li>Managing species-skill associations</li>
 * </ul>
 *
 * <h2>Built-in Species Hierarchy</h2>
 *
 * <pre>
 * agent (root of all species)
 *   ├── model (simulation-level agent)
 *   ├── experiment (experiment controller)
 *   ├── platform (GAMA platform agent)
 *   └── [other built-in species]
 * </pre>
 *
 * <h2>Initialization Process</h2>
 * <ol>
 * <li>Species are registered via {@link #addSpecies} during plugin loading</li>
 * <li>{@link #build()} is called to construct the hierarchy</li>
 * <li>First, 'agent' is built as the root</li>
 * <li>Then 'model' is built as a child of 'agent'</li>
 * <li>The loop is closed by putting 'agent' inside 'model'</li>
 * <li>'experiment' and 'platform' are built</li>
 * <li>All other built-in species are built and attached to 'model'</li>
 * <li>Types are built and descriptions finalized</li>
 * </ol>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Get a built-in species description
 * ISpeciesDescription agentDesc = GamaMetaModel.getSpeciesDescription("agent");
 *
 * // Get a compiled built-in species
 * ISpecies agentSpecies = GamaMetaModel.getSpecies("agent");
 *
 * // Register a new built-in species (during plugin initialization)
 * GamaMetaModel.addSpecies("my_species", MyAgentClass.class, MyAgentClass::new, new String[] { "skill1", "skill2" });
 * }</pre>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMetaModel {

	/** The Constant INSTANCE. */
	private final static GamaMetaModel INSTANCE = new GamaMetaModel();

	/** The temp species. */
	private final Map<String, SpeciesRecord> tempSpecies = new HashMap();

	/** The abstract object class. */
	private IClass abstractObjectClass;

	/** The object. */
	private IClassDescription abstractObjectClassDescription;

	/** The species skills. */
	private final Multimap<String, String> speciesSkills = HashMultimap.create();

	/** The built in species. */
	private final Map<String, ISpecies> builtInSpecies = new HashMap<>();

	/** The built in species descriptions. */
	private final Map<String, ISpeciesDescription> builtInSpeciesDescriptions = new HashMap<>();

	/** The is initialized. */
	public volatile boolean isInitialized;

	/**
	 * Internal record holding species registration information during initialization.
	 *
	 * @param name
	 *            the species name
	 * @param plugin
	 *            the plugin that contributed this species
	 * @param clazz
	 *            the Java class implementing the species
	 * @param helper
	 *            the agent constructor for creating instances
	 * @param skills
	 *            the initial skills assigned to this species
	 */
	private record SpeciesRecord(String name, String plugin, Class clazz, IAgentConstructor helper, String[] skills) {}

	/**
	 * Private constructor enforcing singleton pattern.
	 */
	private GamaMetaModel() {}

	/**
	 * Gets the object class description.
	 *
	 * @return the object class description
	 */
	public static IClassDescription getObjectClassDescription() {
		if (INSTANCE.abstractObjectClassDescription == null) {
			INSTANCE.abstractObjectClassDescription =
					GAML.getDescriptionFactory().createBuiltInClassDescription(GamaBundleLoader.CURRENT_PLUGIN_NAME);
			INSTANCE.abstractObjectClassDescription.validate();
		}
		return INSTANCE.abstractObjectClassDescription;
	}

	/**
	 * Gets the abstract object class.
	 *
	 * @return the abstract object class
	 */
	public static IClass getAbstractObjectClass() {
		IClassDescription desc = getObjectClassDescription();
		if (INSTANCE.abstractObjectClass == null) { INSTANCE.abstractObjectClass = desc.compileAsBuiltIn(); }
		return INSTANCE.abstractObjectClass;
	}

	/**
	 * Registers a built-in species to be compiled during platform initialization.
	 *
	 * <p>
	 * This method should be called during plugin loading, typically from a plugin's activator or initialization code.
	 * Species are not compiled immediately but stored for later processing during {@link #build()}.
	 * </p>
	 *
	 * @param name
	 *            the unique name of the species
	 * @param clazz
	 *            the Java class implementing this species (must implement IAgent)
	 * @param helper
	 *            the constructor function for creating agent instances
	 * @param skills
	 *            array of skill names to attach to this species
	 *
	 * @see #build()
	 */
	public static void addSpecies(final String name, final Class clazz, final IAgentConstructor helper,
			final String[] skills) {
		INSTANCE.tempSpecies.put(name,
				new SpeciesRecord(name, GamaBundleLoader.CURRENT_PLUGIN_NAME, clazz, helper, skills));
	}

	/**
	 * Retrieves the description of a built-in species by name.
	 *
	 * @param name
	 *            the name of the built-in species (e.g., "agent", "model", "experiment")
	 * @return the species description, or null if not found
	 *
	 * @see ISpeciesDescription
	 */
	public static ISpeciesDescription getSpeciesDescription(final String name) {
		return INSTANCE.builtInSpeciesDescriptions.get(name);
	}

	/**
	 * Retrieves a compiled instance of a built-in species.
	 *
	 * <p>
	 * Species are lazily compiled on first access. The compiled species is cached for subsequent calls.
	 * </p>
	 *
	 * @param name
	 *            the name of the built-in species
	 * @return the compiled species instance, or null if not found
	 *
	 * @see ISpecies
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
	 * Builds the GAMA metamodel by compiling all registered built-in species.
	 *
	 * <p>
	 * This method is called once during platform initialization. It constructs the fundamental species hierarchy in the
	 * following order:
	 * <ol>
	 * <li>Build 'agent' as the root of all species</li>
	 * <li>Build 'model' as a child of 'agent'</li>
	 * <li>Close the circular reference by adding 'agent' to 'model'</li>
	 * <li>Build 'experiment' as a child of 'agent'</li>
	 * <li>Build 'platform' as a child of 'agent'</li>
	 * <li>Build all other built-in species and attach them to 'model'</li>
	 * <li>Build types and finalize descriptions</li>
	 * </ol>
	 *
	 * <p>
	 * After this method completes, {@link #isInitialized} is set to true and the metamodel is ready for use.
	 * </p>
	 *
	 * @see #addSpecies(String, Class, IAgentConstructor, String[])
	 */
	public static void build() {
		// We first build "agent" as the root of all other species (incl.
		// "model")
		final SpeciesRecord ap = INSTANCE.tempSpecies.remove(AGENT);
		// "agent" has no super-species yet
		ISpeciesDescription agent = INSTANCE.buildSpecies(ap, null, null);

		// We then build "model", sub-species of "agent"
		final SpeciesRecord wp = INSTANCE.tempSpecies.remove(MODEL);
		IModelDescription model = (IModelDescription) INSTANCE.buildSpecies(wp, null, null);
		// We close the first loop by putting agent "inside" model
		agent.setEnclosingDescription(model);
		model.addChild(agent);

		// We create "experiment" as the root of all experiments, sub-species of "agent"
		final SpeciesRecord ep = INSTANCE.tempSpecies.remove(EXPERIMENT);
		IExperimentDescription experiment = (IExperimentDescription) INSTANCE.buildSpecies(ep, null, agent);
		experiment.finalizeDescription();

		// We now can attach "experiment" as a child of "model"
		model.addChild(experiment);

		// We create "platform" as the root of all platforms, sub-species of "agent"
		final SpeciesRecord pp = INSTANCE.tempSpecies.remove(IKeyword.PLATFORM);
		ISpeciesDescription.Platform platform = (ISpeciesDescription.Platform) INSTANCE.buildSpecies(pp, null, agent);
		// Necessary to be able to use 'gama' in models
		platform.finalizeDescription();
		model.addChild(platform);

		// We then create all other built-in species and attach them to "model"
		for (final SpeciesRecord proto : INSTANCE.tempSpecies.values()) {
			ISpeciesDescription desc = INSTANCE.buildSpecies(proto, model, agent);
			if (!(desc instanceof IModelDescription)) { model.addChild(desc); }
		}
		INSTANCE.tempSpecies.clear();
		model.buildTypes();
		model.finalizeDescription();
		INSTANCE.isInitialized = true;
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
	public static void addSpeciesSkill(final String spec, final String name) {
		INSTANCE.speciesSkills.put(spec, name);
	}

	/**
	 * @return
	 */
	public static Collection<ISpeciesDescription> getAllSpeciesDescriptions() {
		return INSTANCE.builtInSpeciesDescriptions.values();
	}

}
