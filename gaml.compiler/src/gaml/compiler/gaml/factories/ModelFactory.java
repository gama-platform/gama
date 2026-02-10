/*******************************************************************************************************
 *
 * ModelFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.factories;

import static com.google.common.collect.Iterables.get;
import static gama.api.compilation.descriptions.IModelDescription.ROOT;
import static gama.api.constants.IKeyword.FREQUENCY;
import static gama.api.constants.IKeyword.GLOBAL;
import static gama.api.constants.IKeyword.NAME;
import static gama.api.constants.IKeyword.PARENT;
import static gama.api.constants.IKeyword.SCHEDULES;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.ast.ISyntacticElement.SyntacticVisitor;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescription.DescriptionVisitor;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.gaml.GAML;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.symbols.IModelFactory;
import gama.api.gaml.types.Types;
import gama.api.kernel.GamaMetaModel;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gaml.compiler.gaml.ast.SyntacticFactory;
import gaml.compiler.gaml.descriptions.ModelDescription;
import gaml.compiler.gaml.validation.ValidationContext;

/**
 * Factory class responsible for creating and assembling GAMA model descriptions from syntactic elements.
 * 
 * <p>
 * This factory is the central component in the GAMA compilation process that transforms parsed syntactic elements
 * (from GAML files) into fully-fledged model descriptions with properly structured hierarchies of species,
 * experiments, and their associated elements (actions, reflexes, aspects, etc.).
 * </p>
 * 
 * <h3>Key Responsibilities:</h3>
 * <ul>
 * <li>Creating built-in species descriptions (including the root model)</li>
 * <li>Assembling composite models from multiple imported GAML files</li>
 * <li>Building the species hierarchy and establishing parent-child relationships</li>
 * <li>Creating and configuring experiment descriptions</li>
 * <li>Managing the inheritance chain for species and experiments</li>
 * <li>Validating and finalizing model descriptions</li>
 * </ul>
 * 
 * <h3>Model Assembly Process:</h3>
 * <ol>
 * <li>Apply pragmas (no_info, no_warning, requires, etc.)</li>
 * <li>Extract and assemble elements from all imported models</li>
 * <li>Build the primary model with global facets</li>
 * <li>Add micro-species and experiments to the model</li>
 * <li>Establish parent-child relationships</li>
 * <li>Build the type hierarchy</li>
 * <li>Complement species with their members (variables, actions, aspects)</li>
 * <li>Process inheritance chain</li>
 * <li>Finalize all descriptions</li>
 * </ol>
 * 
 * <p>
 * This class follows the Singleton pattern to ensure only one instance exists throughout the compilation process.
 * </p>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see IModelFactory
 * @see IModelDescription
 * @see ISyntacticElement
 */
public class ModelFactory implements IModelFactory {

	/** The singleton instance of ModelFactory. */
	private static ModelFactory INSTANCE;

	/**
	 * Private constructor to enforce singleton pattern.
	 */
	private ModelFactory() {}

	/**
	 * Returns the singleton instance of ModelFactory. Creates the instance lazily on first access.
	 * 
	 * <p>
	 * This method is thread-safe for practical purposes in the GAMA compilation context where model factories
	 * are typically accessed from a single compilation thread.
	 * </p>
	 *
	 * @return the singleton instance of ModelFactory
	 */
	public static ModelFactory getInstance() {
		if (INSTANCE == null) { INSTANCE = new ModelFactory(); }
		return INSTANCE;
	}

	static {
		DEBUG.OFF();
	}

	/**
	 * Creates a built-in species description, including the root model and other built-in model species.
	 * 
	 * <p>
	 * This method is responsible for creating fundamental species that are built into the GAMA platform.
	 * When the name is "model", it creates the root model description that serves as the base for all models.
	 * Otherwise, it creates a built-in model species with the root as its parent.
	 * </p>
	 *
	 * @param name the name of the built-in species (e.g., "model", "agent")
	 * @param clazz the Java class implementing the species behavior
	 * @param macro the macro species description (may be null)
	 * @param parent the parent species description (may be null)
	 * @param helper the agent constructor for creating instances of this species
	 * @param skills the set of skill names associated with this species
	 * @param plugin the plugin name where this species is defined
	 * @return the newly created built-in model description
	 */
	@Override
	@SuppressWarnings ("rawtypes")
	public IModelDescription createBuiltInSpeciesDescription(final String name, final Class clazz,
			final ISpeciesDescription macro, final ISpeciesDescription parent, final IAgentConstructor helper,
			final Set<String> skills, final String plugin) {
		if (IKeyword.MODEL.equals(name)) {
			IModelDescription.ROOT[0] = new ModelDescription(name, clazz, "", "", null, macro, parent, null, null,
					ValidationContext.NULL, Collections.emptySet(), helper);
			return IModelDescription.ROOT[0];
		}
		// We are creating a built-in model species
		// For the moment we suppose its parent is the root (macro)
		final IModelDescription model = new ModelDescription(name, clazz, "", "", null, null, IModelDescription.ROOT[0],
				null, null, ValidationContext.NULL, Collections.emptySet(), helper, skills);
		IModelDescription.BUILT_IN_MODELS.put(name, model);
		return model;
	}

	/**
	 * Builds a description from the given parameters. This method is part of the IModelFactory interface
	 * but is not actually called in the model factory implementation.
	 *
	 * @param keyword the GAML keyword for the description
	 * @param facets the facets (attributes) of the description
	 * @param element the EObject source element
	 * @param children the child descriptions
	 * @param enclosing the enclosing description context
	 * @param proto the symbol prototype
	 * @return always returns null as this method is not used in model assembly
	 */
	@Override
	public IDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription enclosing, final IArtefactProto.Symbol proto) {
		// This method is actually never called.
		return null;
	}

	/**
	 * Creates a complete model description from syntactic elements, assembling all components into a coherent model.
	 * 
	 * <p>
	 * This is the main entry point for model compilation. It orchestrates the entire model assembly process,
	 * from applying pragmas to finalizing the complete model hierarchy with all species, experiments, and their members.
	 * </p>
	 * 
	 * <h3>Assembly Process:</h3>
	 * <ol>
	 * <li>Apply pragmas from source model</li>
	 * <li>Extract and merge elements from all imported models</li>
	 * <li>Build primary model with global facets</li>
	 * <li>Add micro-models as children if present</li>
	 * <li>Add all species and experiments</li>
	 * <li>Establish parent-child relationships</li>
	 * <li>Build type hierarchy</li>
	 * <li>Complement all species with their members</li>
	 * <li>Process inheritance chain</li>
	 * <li>Finalize descriptions</li>
	 * </ol>
	 *
	 * @param projectPath the absolute path to the project containing the model
	 * @param modelPath the path to the model file relative to the project
	 * @param models the collection of syntactic elements representing the model and its imports
	 * @param collector the validation context for collecting errors and warnings
	 * @param mm the map of micro-models (sub-models) indexed by name, may be null
	 * @return the fully assembled and validated model description, or null if compilation fails
	 */
	@Override
	public IModelDescription createModelDescription(final String projectPath, final String modelPath,
			final Iterable<ISyntacticElement> models, final IValidationContext collector,
			final Map<String, IModelDescription> mm) {
		// DEBUG.OUT("ModelAssembler running in thread " + Thread.currentThread().getName());
		// DEBUG.OUT("All models passed to ModelAssembler: "
		// + Iterables.transform(allModels, @Nullable ISyntacticElement::getName));

		/** The species nodes. */
		final LinkedHashMap<String, ISyntacticElement> speciesNodes = new LinkedHashMap<>();

		/** The experiment nodes. */
		final LinkedHashMap<String, ISyntacticElement> experimentNodes = new LinkedHashMap<>();

		/** The temp species cache. */
		final LinkedHashMap<String, ISpeciesDescription> tempSpeciesCache = new LinkedHashMap<>();

		final ISyntacticElement source = get(models, 0);

		if (!applyPragmas(collector, source)) return null;

		Facets globalFacets = null;
		ISyntacticElement globalNodes = SyntacticFactory.getInstance().create(GLOBAL, (EObject) null, true);
		for (int i = Iterables.size(models); i-- > 0;) {
			globalFacets = extractAndAssembleElementsOf(collector, globalFacets, get(models, i), globalNodes,
					speciesNodes, experimentNodes);
		}

		final String modelName = buildModelName(source.getName());

		// We build a list of working paths from which the composite model will
		// be able to look for resources. These working paths come from the
		// imported models

		// DEBUG.OUT("In building " + modelName);
		Set<String> absoluteAlternatePathAsStrings = buildWorkingPaths(mm, models);

		final IModelDescription model = buildPrimaryModel(projectPath, modelPath, collector, models, source,
				globalFacets, modelName, absoluteAlternatePathAsStrings);

		// hqnghi add micro-models
		if (mm != null) {
			// model.setMicroModels(mm);
			model.addChildren(mm.values());
		}
		// end-hqnghi
		// recursively add user-defined species to world and down on to the
		// hierarchy
		addSpeciesAndExperiments(model, speciesNodes, experimentNodes, tempSpeciesCache);

		// Parent the species and the experiments of the model (all are now
		// known).
		parentSpeciesAndExperiments(model, speciesNodes, experimentNodes, tempSpeciesCache);

		// Initialize the hierarchy of types
		model.buildTypes();
		// hqnghi build micro-models as types
		if (mm != null) {
			mm.forEach((k, v) -> model.getTypesManager().alias(v.getName(), k));
			// end-hqnghi
		}

		// Make species and experiments recursively create their attributes,
		// actions....
		complementSpecies(model, globalNodes);

		complementSpeciesAndExperiments(model, speciesNodes, experimentNodes);

		// Complement recursively the different species (incl. the world). The
		// recursion is hierarchical

		model.inheritFromParent();

		for (final ISpeciesDescription sd : getSpeciesInHierarchicalOrder(model)) {
			sd.inheritFromParent();
			if (sd.isExperiment() && !sd.finalizeDescription()) return null;
		}

		// Issue #1708 (put before the finalization)
		if (model.hasFacet(SCHEDULES) || model.hasFacet(FREQUENCY)) { createSchedulerSpecies(model); }

		if (!model.finalizeDescription()) return null;
		return model;

	}

	/**
	 * Complements all species and experiments in the model by adding their children (variables, actions, aspects, etc.).
	 * 
	 * <p>
	 * This method iterates through all species and experiment nodes, delegating to {@link #complementSpecies}
	 * to recursively add member elements (attributes, behaviors, aspects) to each species and experiment description.
	 * </p>
	 *
	 * @param model the model description containing the species and experiments
	 * @param speciesNodes the map of species names to their syntactic elements
	 * @param experimentNodes the map of experiment names to their syntactic elements
	 */
	private void complementSpeciesAndExperiments(final IModelDescription model,
			final Map<String, ISyntacticElement> speciesNodes, final Map<String, ISyntacticElement> experimentNodes) {
		speciesNodes.forEach((s, speciesNode) -> {
			complementSpecies(model.getMicroSpecies(speciesNode.getName()), speciesNode);
		});
		experimentNodes.forEach((s, experimentNode) -> {
			complementSpecies(model.getExperiment(experimentNode.getName()), experimentNode);
		});
	}

	/**
	 * Adds species and experiments to the model from syntactic nodes.
	 * 
	 * <p>
	 * This method processes the collected species and experiment syntactic elements, creating their initial
	 * descriptions and adding them to the model. Micro-species are added as children of the model, and
	 * experiments are registered in the model's experiment collection.
	 * </p>
	 *
	 * @param model the model description to add species and experiments to
	 * @param speciesNodes the map of species names to their syntactic elements
	 * @param experimentNodes the map of experiment names to their syntactic elements
	 * @param tempSpeciesCache the temporary cache for storing species descriptions during construction
	 */
	private void addSpeciesAndExperiments(final IModelDescription model,
			final Map<String, ISyntacticElement> speciesNodes, final Map<String, ISyntacticElement> experimentNodes,
			final Map<String, ISpeciesDescription> tempSpeciesCache) {
		speciesNodes.forEach((s, speciesNode) -> { addMicroSpecies(model, speciesNode, tempSpeciesCache); });
		experimentNodes.forEach((s, experimentNode) -> { addExperiment(s, model, experimentNode, tempSpeciesCache); });
	}

	/**
	 * Establishes parent-child relationships for all species and experiments in the model.
	 * 
	 * <p>
	 * This method processes the species and experiment nodes to set up their inheritance hierarchy.
	 * Species are linked to their parent species (or "agent" by default), and experiments are linked
	 * to their parent experiment (or the built-in "experiment" by default).
	 * </p>
	 *
	 * @param model the model description containing the species and experiments
	 * @param speciesNodes the map of species names to their syntactic elements
	 * @param experimentNodes the map of experiment names to their syntactic elements
	 * @param tempSpeciesCache the temporary cache for looking up species descriptions
	 */
	private void parentSpeciesAndExperiments(final IModelDescription model,
			final Map<String, ISyntacticElement> speciesNodes, final Map<String, ISyntacticElement> experimentNodes,
			final Map<String, ISpeciesDescription> tempSpeciesCache) {
		speciesNodes.forEach((s, speciesNode) -> { parentSpecies(model, speciesNode, model, tempSpeciesCache); });
		experimentNodes.forEach((s, experimentNode) -> { parentExperiment(model, experimentNode); });
	}

	/**
	 * Builds the primary model description from collected information.
	 * 
	 * <p>
	 * This method creates the main model description with all global facets, determines the parent model
	 * (either ROOT or a built-in model if specified), and sets up the model's working paths for resource lookup.
	 * </p>
	 *
	 * @param projectPath the absolute path to the project
	 * @param modelPath the relative path to the model file
	 * @param collector the validation context for error collection
	 * @param models the collection of all syntactic elements (main model and imports)
	 * @param source the main source syntactic element
	 * @param globalFacets the merged global facets from all models
	 * @param modelName the computed name for the model
	 * @param absoluteAlternatePathAsStrings the set of alternate working paths
	 * @return the newly created primary model description
	 */
	private IModelDescription buildPrimaryModel(final String projectPath, final String modelPath,
			final IValidationContext collector, final Iterable<ISyntacticElement> models,
			final ISyntacticElement source, final Facets globalFacets, final String modelName,
			final Set<String> absoluteAlternatePathAsStrings) {
		IModelDescription parent = ROOT[0];
		if (globalFacets != null && globalFacets.containsKey(PARENT)) {
			String parentModel = globalFacets.getLabel(PARENT);
			IModelDescription parentBuiltInModels = IModelDescription.BUILT_IN_MODELS.get(parentModel);
			if (parentBuiltInModels != null) { parent = parentBuiltInModels; }
		}
		final IModelDescription model =
				new ModelDescription(modelName, null, projectPath, modelPath, source.getElement(), null, parent, null,
						globalFacets, collector, absoluteAlternatePathAsStrings, parent.getAgentConstructor());
		final Collection<String> allModelNames = Iterables.size(models) == 1 ? null : ImmutableSet
				.copyOf(Iterables.transform(Iterables.skip(models, 1), each -> buildModelName(each.getName())));
		model.setImportedModelNames(allModelNames);
		return model;
	}

	/**
	 * Builds the set of working paths for resource lookup from imported models and micro-models.
	 * 
	 * <p>
	 * Aggregates file paths from all syntactic model elements and their micro-models to create
	 * a comprehensive set of directories where the model can search for resources (data files, images, etc.).
	 * </p>
	 *
	 * @param mm the map of micro-models (may be null)
	 * @param models the collection of syntactic elements representing imported models
	 * @return a set of absolute path strings that serve as alternate resource lookup paths
	 */
	private Set<String> buildWorkingPaths(final Map<String, IModelDescription> mm,
			final Iterable<ISyntacticElement> models) {
		final LinkedHashSet<String> workingPaths = new LinkedHashSet<>();
		// Add paths from all model elements (in reverse order to maintain priority)
		for (int i = Iterables.size(models); i-- > 0;) {
			workingPaths.add(get(models, i).getPath());
		}
		// Add alternate paths from micro-models if present
		if (mm != null) {
			for (final IModelDescription microModel : mm.values()) {
				workingPaths.addAll(microModel.getAlternatePaths());
			}
		}
		return workingPaths;
	}

	/**
	 * Extracts and assembles elements from a syntactic model element.
	 * 
	 * <p>
	 * This method processes a single model element, extracting its facets, global nodes, species, grids, and experiments.
	 * It merges the facets with existing global facets and categorizes elements into the appropriate node collections.
	 * Species and grids are processed in order, with grids always placed after regular species to support proper
	 * diffusion statement handling.
	 * </p>
	 *
	 * @param collector the validation context for error collection
	 * @param globalFacets the accumulated global facets (may be null initially)
	 * @param cm the current syntactic model element to process
	 * @param globalNodes the collection of global nodes to add children to
	 * @param speciesNodes the map collecting all species nodes
	 * @param experimentNodes the map collecting all experiment nodes
	 * @return the updated global facets after merging with the current model's facets
	 */
	private Facets extractAndAssembleElementsOf(final IValidationContext collector, Facets globalFacets,
			final ISyntacticElement cm, final ISyntacticElement globalNodes,
			final Map<String, ISyntacticElement> speciesNodes, final Map<String, ISyntacticElement> experimentNodes) {
		if (cm == null) return globalFacets;
		
		// Merge facets from current model
		if (cm.hasFacets()) {
			final Facets currentFacets = cm.copyFacets(null);
			if (globalFacets == null) {
				globalFacets = currentFacets;
			} else {
				globalFacets.putAll(currentFacets);
			}
		}
		
		// Add children with origin information
		cm.visitChildren(element -> {
			element.setFacet(IKeyword.ORIGIN,
					GAML.getExpressionDescriptionFactory().createConstant(cm.getName()));
			globalNodes.addChild(element);
		});
		
		// Visit species and grids (grids last to support DiffusionStatement)
		final SyntacticVisitor speciesVisitor = element -> addSpeciesNode(element, collector, speciesNodes);
		cm.visitSpecies(speciesVisitor);
		cm.visitGrids(speciesVisitor);
		
		// Visit experiments
		cm.visitExperiments(element -> addExperimentNode(element, cm.getName(), collector, experimentNodes));
		
		return globalFacets;
	}

	/**
	 * Applies pragmas from the source syntactic element to the validation context.
	 * 
	 * <p>
	 * Pragmas are special directives that control compilation behavior, such as disabling info/warning messages,
	 * requiring specific plugins, or disabling experiments. This method processes these pragmas and configures
	 * the validation context accordingly.
	 * </p>
	 * 
	 * <h4>Supported Pragmas:</h4>
	 * <ul>
	 * <li><code>no_info</code> - Suppresses informational messages</li>
	 * <li><code>no_warning</code> - Suppresses warning messages</li>
	 * <li><code>no_experiment</code> - Disables experiment validation</li>
	 * <li><code>requires</code> - Specifies required plugins for the model</li>
	 * </ul>
	 *
	 * @param collector the validation context to configure
	 * @param source the syntactic element containing pragma declarations
	 * @return true if pragmas were applied successfully (including plugin requirements), false if required plugins are missing
	 */
	private boolean applyPragmas(final IValidationContext collector, final ISyntacticElement source) {
		final Map<String, List<String>> pragmas = source.getPragmas();
		collector.resetInfoAndWarning();
		
		if (pragmas == null) return true;
		
		// Apply boolean pragmas
		if (pragmas.containsKey(IKeyword.PRAGMA_NO_INFO)) { collector.setNoInfo(); }
		if (pragmas.containsKey(IKeyword.PRAGMA_NO_WARNING)) { collector.setNoWarning(); }
		if (pragmas.containsKey(IKeyword.PRAGMA_NO_EXPERIMENT)) { collector.setNoExperiment(); }
		
		// Verify required plugins if enabled
		final List<String> requiresList = pragmas.get(IKeyword.PRAGMA_REQUIRES);
		if (GamaPreferences.Experimental.REQUIRED_PLUGINS.getValue() && requiresList != null) {
			return collector.verifyPlugins(requiresList);
		}
		
		return true;
	}

	/**
	 * Returns species in hierarchical order, from parent to child, using topological sorting.
	 * 
	 * <p>
	 * This method constructs a directed acyclic graph (DAG) representing the species hierarchy
	 * and returns an iterator that traverses species in dependency order (parents before children).
	 * This ordering ensures that parent species are fully processed before their children.
	 * </p>
	 * 
	 * <p>
	 * If a cycle is detected in the hierarchy, an error is reported and the affected species
	 * is excluded from the ordering.
	 * </p>
	 *
	 * @param model the model containing the species hierarchy
	 * @return an iterable of species descriptions in topological (hierarchical) order
	 */
	private Iterable<ISpeciesDescription> getSpeciesInHierarchicalOrder(final IModelDescription model) {
		/** The hierarchy. */
		final DirectedAcyclicGraph<ISpeciesDescription, Object> hierarchy = new DirectedAcyclicGraph<>(Object.class);

		/** The hierarchy builder. */
		final DescriptionVisitor<ISpeciesDescription> hierarchyBuilder = desc -> {
			if (desc instanceof IModelDescription) return true;
			final ISpeciesDescription sd = desc.getParent();
			if (sd == null || sd == desc) return false;
			hierarchy.addVertex(desc);
			if (!sd.isBuiltIn()) {
				hierarchy.addVertex(sd);
				try {
					hierarchy.addEdge(sd, desc);
				} catch (
				/** The e. */
				IllegalArgumentException e) {
					// denotes the presence of a cycle in the hierarchy
					desc.error("The hierarchy of " + desc.getName() + " is inconsistent.", IGamlIssue.WRONG_PARENT);
					return false;
				}
			}
			return true;
		};
		model.visitAllSpecies(hierarchyBuilder);
		return () -> hierarchy.iterator();
	}

	/**
	 * Creates an internal scheduler species when the model has schedule or frequency facets.
	 * 
	 * <p>
	 * When a model declares <code>schedules</code> or <code>frequency</code> facets at the global level,
	 * this method creates a special internal species to handle the scheduling logic. The facets are
	 * transferred from the model to this internal species to maintain proper scheduling behavior.
	 * </p>
	 *
	 * @param model the model that requires a scheduler species
	 */
	private void createSchedulerSpecies(final IModelDescription model) {
		final ISpeciesDescription sd = (ISpeciesDescription) GAML.getDescriptionFactory().create(IKeyword.SPECIES,
				model, NAME, "_internal_global_scheduler");
		sd.finalizeDescription();
		if (model.hasFacet(SCHEDULES)) {
			// remove the warning as GAMA integrates a working workaround to use this facet at the global level
			// model.warning(
			// "'schedules' is deprecated in global. Define a dedicated species instead and add the facet to it",
			// IGamlIssue.DEPRECATED, NAME);
			sd.setFacetExprDescription(SCHEDULES, model.getFacet(SCHEDULES));
			model.removeFacets(SCHEDULES);
		}
		if (model.hasFacet(FREQUENCY)) {
			// model.warning(
			// "'frequency' is deprecated in global. Define a dedicated species instead and add the facet to it",
			// IGamlIssue.DEPRECATED, NAME);
			sd.setFacetExprDescription(FREQUENCY, model.getFacet(FREQUENCY));
			model.removeFacets(FREQUENCY);
		}
		model.addChild(sd);
	}

	/**
	 * Adds an experiment description to the model.
	 * 
	 * <p>
	 * Creates an experiment description from the syntactic element and registers it with the model.
	 * The experiment is cached for later parent relationship establishment and origin information is set.
	 * </p>
	 *
	 * @param origin the name of the model where the experiment originates
	 * @param model the model description to add the experiment to
	 * @param experiment the syntactic element defining the experiment
	 * @param cache the temporary cache for storing experiment descriptions
	 */
	void addExperiment(final String origin, final IModelDescription model, final ISyntacticElement experiment,
			final Map<String, ISpeciesDescription> cache) {
		// Create the experiment description
		final IDescription desc = GAML.getDescriptionFactory().create(experiment, model, Collections.emptyList());
		final IExperimentDescription eDesc = (IExperimentDescription) desc;
		cache.put(eDesc.getName(), eDesc);
		desc.resetOriginName();
		desc.setOriginName(buildModelName(origin));
		model.addChild(desc);
	}

	/**
	 * Adds an experiment node to the collection, validating for duplicates.
	 * 
	 * <p>
	 * Checks if an experiment with the same name has already been declared and reports appropriate
	 * errors or informational messages. If the experiment is being superseded by one from another file,
	 * an info message is generated. If it's a duplicate within the same file, an error is reported.
	 * </p>
	 *
	 * @param element the syntactic element representing the experiment
	 * @param modelName the name of the model containing the experiment
	 * @param collector the validation context for error reporting
	 * @param experimentNodes the map collecting all experiment nodes
	 */
	void addExperimentNode(final ISyntacticElement element, final String modelName, final IValidationContext collector,
			final Map<String, ISyntacticElement> experimentNodes) {
		final String experimentName = element.getName();
		final ISyntacticElement existingExperiment = experimentNodes.get(experimentName);
		
		// Check for duplicate experiment declarations
		if (existingExperiment != null) {
			final EObject existingObject = existingExperiment.getElement();
			if (existingObject != null && existingObject.eResource() != null) {
				final URI existingURI = existingObject.eResource().getURI();
				final URI currentURI = collector.getURI();
				
				if (existingURI.equals(currentURI)) {
					// Same file - this is an error
					collector.add(new GamlCompilationError("Experiment " + experimentName + " is declared twice",
							IGamlIssue.DUPLICATE_DEFINITION, element.getElement(), GamlCompilationError.Type.Error));
				} else {
					// Different file - this is informational (experiment supersedes the previous one)
					collector.add(new GamlCompilationError(
							"Experiment " + experimentName + " supersedes the one declared in " + existingURI.lastSegment(),
							IGamlIssue.DUPLICATE_DEFINITION, element.getElement(), GamlCompilationError.Type.Info));
				}
			}
		}
		
		experimentNodes.put(experimentName, element);
	}

	/**
	 * Adds a micro-species to a macro-species (container species).
	 * 
	 * <p>
	 * Creates a species description from the syntactic element and registers it as a child of the macro-species.
	 * This method is called recursively to handle nested species hierarchies, processing both regular species
	 * and experiments as micro-species of their container.
	 * </p>
	 *
	 * @param macro the macro-species (container) to add the micro-species to
	 * @param micro the syntactic element defining the micro-species
	 * @param cache the temporary cache for storing species descriptions during construction
	 */
	void addMicroSpecies(final ISpeciesDescription macro, final ISyntacticElement micro,
			final Map<String, ISpeciesDescription> cache) {
		// Create the species description without any children. Passing
		// explicitly an empty list and not null;
		final ISpeciesDescription mDesc =
				(ISpeciesDescription) GAML.getDescriptionFactory().create(micro, macro, Collections.emptyList());
		cache.put(mDesc.getName(), mDesc);
		// Add it to its macro-species
		macro.addChild(mDesc);
		// Recursively create each micro-species of the newly added micro-species
		final SyntacticVisitor visitor = element -> addMicroSpecies(mDesc, element, cache);
		micro.visitSpecies(visitor);
		micro.visitExperiments(visitor);
	}

	/**
	 * Adds a species node to the collection, validating for duplicates.
	 * 
	 * <p>
	 * Checks if a species with the same name has already been declared and reports errors for both
	 * occurrences if duplicates are found. Only species elements (not experiments or other types) are processed.
	 * </p>
	 *
	 * @param sse the syntactic element representing the species
	 * @param collector the validation context for error reporting
	 * @param speciesNodes the map collecting all species nodes
	 */
	void addSpeciesNode(final ISyntacticElement sse, final IValidationContext collector,
			final Map<String, ISyntacticElement> speciesNodes) {
		if (!sse.isSpecies()) return;
		
		final String name = sse.getName();
		final ISyntacticElement existingNode = speciesNodes.get(name);
		
		// Report duplicate species declarations on both occurrences
		if (existingNode != null) {
			final GamlCompilationError error = new GamlCompilationError("Species " + name + " is declared twice",
					IGamlIssue.DUPLICATE_DEFINITION, sse.getElement(), GamlCompilationError.Type.Error);
			collector.add(error);
			collector.add(new GamlCompilationError("Species " + name + " is declared twice",
					IGamlIssue.DUPLICATE_DEFINITION, existingNode.getElement(), GamlCompilationError.Type.Error));
		}
		
		speciesNodes.put(name, sse);
	}

	/**
	 * Recursively complements a species and its micro-species with all member elements.
	 * 
	 * <p>
	 * This method populates a species description with its complete content including variables, behaviors
	 * (actions, reflexes, tasks, states), aspects, and other elements defined in the syntactic structure.
	 * It processes both the species itself and all nested micro-species recursively.
	 * </p>
	 * 
	 * <h4>Elements Added:</h4>
	 * <ul>
	 * <li>Variables and attributes</li>
	 * <li>Actions and behaviors</li>
	 * <li>Reflexes and tasks</li>
	 * <li>States and state machines</li>
	 * <li>Aspects (graphical representations)</li>
	 * <li>Java additions from annotations</li>
	 * </ul>
	 *
	 * @param species the species description to complement
	 * @param node the syntactic element containing the species structure and children
	 */
	void complementSpecies(final ISpeciesDescription species, final ISyntacticElement node) {
		if (species == null) return;
		species.copyJavaAdditions();
		node.visitChildren(element -> {
			final IDescription childDesc = GAML.getDescriptionFactory().create(element, species, null);
			if (childDesc != null) { species.addChild(childDesc); }
		});
		// recursively complement micro-species
		node.visitSpecies(element -> {
			final ISpeciesDescription sd = species.getMicroSpecies(element.getName());
			if (sd != null) { complementSpecies(sd, element); }
		});

	}

	/**
	 * Establishes the parent relationship for an experiment.
	 * 
	 * <p>
	 * Links an experiment to its parent experiment based on the "parent" facet declared in the syntactic element.
	 * If no parent is specified, the built-in "experiment" description is used as the default parent.
	 * </p>
	 *
	 * @param model the model containing the experiments
	 * @param micro the syntactic element defining the experiment whose parent needs to be set
	 */
	void parentExperiment(final IModelDescription model, final ISyntacticElement micro) {
		// Gather the previously created experiment
		final ISpeciesDescription experimentDesc = model.getExperiment(micro.getName());
		if (experimentDesc == null) return;
		
		// Get parent name from facet, default to built-in "experiment" if not specified
		final String parentName = experimentDesc.getLitteral(IKeyword.PARENT);
		ISpeciesDescription parent = model.getExperiment(parentName);
		if (parent == null) { 
			parent = GamaMetaModel.getExperimentDescription(); 
		}
		
		experimentDesc.setParent(parent);
	}

	/**
	 * Establishes the parent relationship for a species and recursively for its micro-species.
	 * 
	 * <p>
	 * Links a species to its parent species based on the "parent" facet declared in the syntactic element.
	 * If no parent is specified, "agent" is used as the default parent. The method recursively processes
	 * all nested micro-species to establish the complete species hierarchy.
	 * </p>
	 *
	 * @param macro the macro-species containing this species
	 * @param micro the syntactic element defining the species whose parent needs to be set
	 * @param model the model containing all species descriptions
	 * @param cache the temporary cache for species lookups during construction
	 */
	void parentSpecies(final ISpeciesDescription macro, final ISyntacticElement micro, final IModelDescription model,
			final Map<String, ISpeciesDescription> cache) {
		// Gather the previously created species
		final ISpeciesDescription speciesDesc = cache.get(micro.getName());
		if (speciesDesc == null || speciesDesc.isExperiment()) return;
		
		// Get parent name from facet, default to "agent" if not specified
		String parentName = speciesDesc.getLitteral(IKeyword.PARENT);
		if (parentName == null) { 
			parentName = IKeyword.AGENT; 
		}
		
		// Look up parent in cache first, then in model
		ISpeciesDescription parent = lookupSpecies(parentName, cache);
		if (parent == null) { 
			parent = model.getSpeciesDescription(parentName); 
		}
		
		speciesDesc.setParent(parent);
		
		// Recursively process micro-species
		micro.visitSpecies(element -> parentSpecies(speciesDesc, element, model, cache));
	}

	/**
	 * Looks up a species description by name, searching first in the cache and then in built-in species.
	 * 
	 * <p>
	 * This method provides a two-tier lookup: first checking the temporary cache of species being constructed,
	 * then falling back to the registry of built-in species (agent, world, model, etc.).
	 * </p>
	 *
	 * @param name the name of the species to look up
	 * @param cache the temporary cache of species descriptions
	 * @return the species description if found, null otherwise
	 */
	ISpeciesDescription lookupSpecies(final String name, final Map<String, ISpeciesDescription> cache) {
		ISpeciesDescription result = cache.get(name);
		if (result == null) { result = Types.getBuiltInSpecies().get(name); }
		return result;
	}

	/**
	 * Builds a normalized model name by replacing spaces with underscores and appending the model suffix.
	 * 
	 * <p>
	 * Ensures model names follow GAMA's naming convention by converting spaces to underscores
	 * and adding the standard model suffix defined in {@link IModelDescription#MODEL_SUFFIX}.
	 * </p>
	 *
	 * @param source the source string (usually a file name without extension)
	 * @return the normalized model name with suffix
	 */
	protected String buildModelName(final String source) {
		return source.replace(' ', '_') + IModelDescription.MODEL_SUFFIX;
	}

	/**
	 * Returns the kinds of descriptions this factory can create.
	 * 
	 * <p>
	 * Implements {@link IModelFactory#getKinds()} to indicate that this factory
	 * is responsible for creating MODEL-level descriptions.
	 * </p>
	 *
	 * @return an array containing the MODEL kind constant
	 */
	@Override
	public int[] getKinds() { return new int[] { MODEL }; }

}
