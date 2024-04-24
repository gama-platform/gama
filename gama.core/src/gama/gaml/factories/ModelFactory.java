/*******************************************************************************************************
 *
 * ModelFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.factories;

import static com.google.common.collect.Iterables.get;
import static gama.core.common.interfaces.IKeyword.FREQUENCY;
import static gama.core.common.interfaces.IKeyword.GLOBAL;
import static gama.core.common.interfaces.IKeyword.NAME;
import static gama.core.common.interfaces.IKeyword.PARENT;
import static gama.core.common.interfaces.IKeyword.SCHEDULES;
import static gama.core.common.interfaces.IKeyword.SPECIES;
import static gama.gaml.descriptions.ModelDescription.BUILT_IN_MODELS;
import static gama.gaml.descriptions.ModelDescription.ROOT;

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

import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.compilation.IAgentConstructor;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.gaml.compilation.ast.ISyntacticElement.SyntacticVisitor;
import gama.gaml.compilation.ast.SyntacticFactory;
import gama.gaml.compilation.ast.SyntacticModelElement;
import gama.gaml.compilation.ast.SyntacticSpeciesElement;
import gama.gaml.compilation.kernel.GamaMetaModel;
import gama.gaml.descriptions.ConstantExpressionDescription;
import gama.gaml.descriptions.ExperimentDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IDescription.DescriptionVisitor;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.descriptions.ValidationContext;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.Facets;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 27 oct. 2009
 *
 * @todo Description
 */
public class ModelFactory extends SymbolFactory {

	static {
		DEBUG.ON();
	}

	/** The hierarchy. */
	DirectedAcyclicGraph<SpeciesDescription, Object> hierarchy = new DirectedAcyclicGraph<>(Object.class);

	/** The hierarchy builder. */
	final DescriptionVisitor<SpeciesDescription> hierarchyBuilder = desc -> {
		if (desc instanceof ModelDescription) return true;
		final SpeciesDescription sd = desc.getParent();
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

	/**
	 * Creates a new Model object.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 * @param plugin
	 *            the plugin
	 * @return the model description
	 */
	@SuppressWarnings ("rawtypes")
	public static ModelDescription createRootModel(final String name, final Class clazz, final SpeciesDescription macro,
			final SpeciesDescription parent, final IAgentConstructor helper, final Set<String> skills,
			final String plugin) {
		if (IKeyword.MODEL.equals(name)) {
			ModelDescription.ROOT = new ModelDescription(name, clazz, "", "", null, macro, parent, null, null,
					ValidationContext.NULL, Collections.EMPTY_SET, helper);
			return ModelDescription.ROOT;
		}
		// we are with a built-in model species
		// for the moment we suppose its parent is the root (macro)
		ModelDescription model = new ModelDescription(name, clazz, "", "", null, null, ModelDescription.ROOT, null,
				null, ValidationContext.NULL, Collections.EMPTY_SET, helper, skills);
		ModelDescription.BUILT_IN_MODELS.put(name, model);
		return model;

	}

	@Override
	protected IDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription enclosing, final SymbolProto proto) {
		// This method is actually never called.
		return null;
	}

	/**
	 * Assemble.
	 *
	 * @param projectPath
	 *            the project path
	 * @param modelPath
	 *            the model path
	 * @param allModels
	 *            the all models
	 * @param collector
	 *            the collector
	 * @param document
	 *            the document
	 * @param mm
	 *            the mm
	 * @return the model description
	 */
	public ModelDescription createModelDescription(final String projectPath, final String modelPath,
			final Iterable<ISyntacticElement> models, final ValidationContext collector,
			final Map<String, ModelDescription> mm) {
		// DEBUG.OUT("ModelAssembler running in thread " + Thread.currentThread().getName());
		// DEBUG.OUT("All models passed to ModelAssembler: "
		// + Iterables.transform(allModels, @Nullable ISyntacticElement::getName));

		/** The species nodes. */
		final LinkedHashMap<String, ISyntacticElement> speciesNodes = new LinkedHashMap<>();

		/** The experiment nodes. */
		final LinkedHashMap<String, ISyntacticElement> experimentNodes = new LinkedHashMap<>();

		/** The temp species cache. */
		final LinkedHashMap<String, SpeciesDescription> tempSpeciesCache = new LinkedHashMap<>();

		try {

			final ISyntacticElement source = get(models, 0);

			if (!applyPragmas(collector, source)) return null;

			Facets globalFacets = null;
			ISyntacticElement globalNodes = SyntacticFactory.create(GLOBAL, (EObject) null, true);
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

			final ModelDescription model = buildPrimaryModel(projectPath, modelPath, collector, models, source,
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

			for (final SpeciesDescription sd : getSpeciesInHierarchicalOrder(model)) {
				sd.inheritFromParent();
				if (sd.isExperiment() && !sd.finalizeDescription()) return null;
			}

			// Issue #1708 (put before the finalization)
			if (model.hasFacet(SCHEDULES) || model.hasFacet(FREQUENCY)) { createSchedulerSpecies(model); }

			if (!model.finalizeDescription()) return null;
			return model;
		} finally {
			hierarchy = new DirectedAcyclicGraph<>(Object.class);
		}

	}

	/**
	 * Complement species and experiments.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param speciesNodes
	 *            the species nodes
	 * @param experimentNodes
	 *            the experiment nodes
	 * @param model
	 *            the model
	 * @date 26 déc. 2023
	 */
	private void complementSpeciesAndExperiments(final ModelDescription model,
			final Map<String, ISyntacticElement> speciesNodes, final Map<String, ISyntacticElement> experimentNodes) {
		speciesNodes.forEach((s, speciesNode) -> {
			complementSpecies(model.getMicroSpecies(speciesNode.getName()), speciesNode);
		});
		experimentNodes.forEach((s, experimentNode) -> {
			complementSpecies(model.getExperiment(experimentNode.getName()), experimentNode);
		});
	}

	/**
	 * Adds the species and experiments.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param speciesNodes
	 *            the species nodes
	 * @param experimentNodes
	 *            the experiment nodes
	 * @param tempSpeciesCache
	 *            the temp species cache
	 * @param model
	 *            the model
	 * @date 26 déc. 2023
	 */
	private void addSpeciesAndExperiments(final ModelDescription model,
			final Map<String, ISyntacticElement> speciesNodes, final Map<String, ISyntacticElement> experimentNodes,
			final Map<String, SpeciesDescription> tempSpeciesCache) {
		speciesNodes.forEach((s, speciesNode) -> { addMicroSpecies(model, speciesNode, tempSpeciesCache); });
		experimentNodes.forEach((s, experimentNode) -> { addExperiment(s, model, experimentNode, tempSpeciesCache); });
	}

	/**
	 * Parent species and experiments.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param speciesNodes
	 *            the species nodes
	 * @param experimentNodes
	 *            the experiment nodes
	 * @param tempSpeciesCache
	 *            the temp species cache
	 * @param model
	 *            the model
	 * @date 26 déc. 2023
	 */
	private void parentSpeciesAndExperiments(final ModelDescription model,
			final Map<String, ISyntacticElement> speciesNodes, final Map<String, ISyntacticElement> experimentNodes,
			final Map<String, SpeciesDescription> tempSpeciesCache) {
		speciesNodes.forEach((s, speciesNode) -> { parentSpecies(model, speciesNode, model, tempSpeciesCache); });
		experimentNodes.forEach((s, experimentNode) -> { parentExperiment(model, experimentNode); });
	}

	/**
	 * Builds the primary model.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param projectPath
	 *            the project path
	 * @param modelPath
	 *            the model path
	 * @param collector
	 *            the collector
	 * @param document
	 *            the document
	 * @param models
	 *            the models
	 * @param source
	 *            the source
	 * @param globalFacets
	 *            the global facets
	 * @param modelName
	 *            the model name
	 * @param absoluteAlternatePathAsStrings
	 *            the absolute alternate path as strings
	 * @return the model description
	 * @date 26 déc. 2023
	 */
	private ModelDescription buildPrimaryModel(final String projectPath, final String modelPath,
			final ValidationContext collector, final Iterable<ISyntacticElement> models, final ISyntacticElement source,
			final Facets globalFacets, final String modelName, final Set<String> absoluteAlternatePathAsStrings) {
		ModelDescription parent = ROOT;
		if (globalFacets != null && globalFacets.containsKey(PARENT)) {
			String parentModel = globalFacets.getLabel(PARENT);
			ModelDescription parentBuiltInModels = BUILT_IN_MODELS.get(parentModel);
			if (parentBuiltInModels != null){ 
				parent = parentBuiltInModels;
			}
		}
		final ModelDescription model =
				new ModelDescription(modelName, null, projectPath, modelPath, source.getElement(), null, parent, null,
						globalFacets, collector, absoluteAlternatePathAsStrings, parent.getAgentConstructor());
		final Collection<String> allModelNames = Iterables.size(models) == 1 ? null : ImmutableSet
				.copyOf(Iterables.transform(Iterables.skip(models, 1), each -> buildModelName(each.getName())));
		model.setImportedModelNames(allModelNames);
		return model;
	}

	/**
	 * Builds the working paths.
	 *
	 * @param mm
	 *            the mm
	 * @param models
	 *            the models
	 * @return the sets the
	 */
	private Set<String> buildWorkingPaths(final Map<String, ModelDescription> mm,
			final Iterable<ISyntacticElement> models) {
		LinkedHashSet<String> absoluteAlternatePathAsStrings = new LinkedHashSet<>();
		for (int i = Iterables.size(models); i-- > 0;) {
			// DEBUG.OUT("Adding " + ((SyntacticModelElement) get(models, i)).getPath() + " to the paths");
			absoluteAlternatePathAsStrings.add(((SyntacticModelElement) get(models, i)).getPath());
		}
		if (mm != null) {
			for (final ModelDescription m1 : mm.values()) {
				absoluteAlternatePathAsStrings.addAll(m1.getAlternatePaths());
			}
		}
		// DEBUG.OUT("Alternate paths " + absoluteAlternatePathAsStrings);
		return absoluteAlternatePathAsStrings;
	}

	/**
	 * Extract and assemble elements of.
	 *
	 * @param collector
	 *            the collector
	 * @param speciesNodes
	 *            the species nodes
	 * @param experimentNodes
	 *            the experiment nodes
	 * @param globalNodes
	 *            the global nodes
	 * @param globalFacets
	 *            the global facets
	 * @param cm
	 *            the cm
	 * @return the facets
	 */
	private Facets extractAndAssembleElementsOf(final ValidationContext collector, Facets globalFacets,
			final ISyntacticElement cm, final ISyntacticElement globalNodes,
			final Map<String, ISyntacticElement> speciesNodes, final Map<String, ISyntacticElement> experimentNodes) {
		final SyntacticModelElement currentModel = (SyntacticModelElement) cm;
		if (currentModel != null) {
			if (currentModel.hasFacets()) {
				if (globalFacets == null) {
					globalFacets = currentModel.copyFacets(null);
				} else {
					globalFacets.putAll(currentModel.copyFacets(null));
				}
			}
			currentModel.visitChildren(element -> {
				element.setFacet(IKeyword.ORIGIN, ConstantExpressionDescription.create(currentModel.getName()));
				globalNodes.addChild(element);
			});
			SyntacticVisitor visitor = element -> addSpeciesNode(element, collector, speciesNodes);
			currentModel.visitSpecies(visitor);

			// We input the species so that grids are always the last ones
			// (see DiffusionStatement)
			currentModel.visitGrids(visitor);
			visitor = element -> {
				addExperimentNode(element, currentModel.getName(), collector, experimentNodes);

			};
			currentModel.visitExperiments(visitor);

		}
		return globalFacets;
	}

	/**
	 * Apply pragmas.
	 *
	 * @param collector
	 *            the collector
	 * @param source
	 *            the source
	 * @return true, if successful
	 */
	private boolean applyPragmas(final ValidationContext collector, final ISyntacticElement source) {
		final Map<String, List<String>> pragmas = source.getPragmas();
		collector.resetInfoAndWarning();
		if (pragmas == null) {
			return true;
		}
		List<String> requiresList = pragmas.get(IKeyword.PRAGMA_REQUIRES);
		if (pragmas.containsKey(IKeyword.PRAGMA_NO_INFO)) { collector.setNoInfo(); }
		if (pragmas.containsKey(IKeyword.PRAGMA_NO_WARNING)) { collector.setNoWarning(); }
		if (pragmas.containsKey(IKeyword.PRAGMA_NO_EXPERIMENT)) { collector.setNoExperiment(); }
		if (GamaPreferences.Experimental.REQUIRED_PLUGINS.getValue()
				&& requiresList != null
				&& !collector.verifyPlugins(requiresList)) {
			return false;			
		}
		return true;
	}

	/**
	 * Gets the species in hierarchical order.
	 *
	 * @param model
	 *            the model
	 * @return the species in hierarchical order
	 */
	private Iterable<SpeciesDescription> getSpeciesInHierarchicalOrder(final ModelDescription model) {
		model.visitAllSpecies(hierarchyBuilder);
		return () -> hierarchy.iterator();
	}

	/**
	 * Creates the scheduler species.
	 *
	 * @param model
	 *            the model
	 */
	private void createSchedulerSpecies(final ModelDescription model) {
		final SpeciesDescription sd =
				(SpeciesDescription) DescriptionFactory.create(SPECIES, model, NAME, "_internal_global_scheduler");
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
	 * Adds the experiment.
	 *
	 * @param origin
	 *            the origin
	 * @param model
	 *            the model
	 * @param experiment
	 *            the experiment
	 * @param cache
	 *            the cache
	 */
	void addExperiment(final String origin, final ModelDescription model, final ISyntacticElement experiment,
			final Map<String, SpeciesDescription> cache) {
		// Create the experiment description
		final IDescription desc = DescriptionFactory.create(experiment, model, Collections.EMPTY_LIST);
		final ExperimentDescription eDesc = (ExperimentDescription) desc;
		cache.put(eDesc.getName(), eDesc);
		((SymbolDescription) desc).resetOriginName();
		desc.setOriginName(buildModelName(origin));
		model.addChild(desc);
	}

	/**
	 * Adds the experiment node.
	 *
	 * @param element
	 *            the element
	 * @param modelName
	 *            the model name
	 * @param experimentNodes
	 *            the experiment nodes
	 * @param collector
	 *            the collector
	 */
	void addExperimentNode(final ISyntacticElement element, final String modelName, final ValidationContext collector,
			final Map<String, ISyntacticElement> experimentNodes) {
		// First we verify that this experiment has not been declared previously
		final String experimentName = element.getName();
		ISyntacticElement elm = experimentNodes.get(experimentName); 
		if (elm != null) {
			EObject object = elm.getElement();
			if (object != null && object.eResource() != null) {
				URI other = object.eResource().getURI();
				URI myself = collector.getURI();
				if (other.equals(myself)) {
					collector.add(new GamlCompilationError("Experiment " + element.getName() + " is declared twice",
							IGamlIssue.DUPLICATE_DEFINITION, element.getElement(), false, false));
				} else {
					collector.add(new GamlCompilationError(
							"Experiment " + experimentName + " supersedes the one declared in " + other.lastSegment(),
							IGamlIssue.DUPLICATE_DEFINITION, element.getElement(), false, true));
				}
			}
		}
		experimentNodes.put(experimentName, element);
	}

	/**
	 * Adds the micro species.
	 *
	 * @param macro
	 *            the macro
	 * @param micro
	 *            the micro
	 * @param cache
	 *            the cache
	 */
	void addMicroSpecies(final SpeciesDescription macro, final ISyntacticElement micro,
			final Map<String, SpeciesDescription> cache) {
		// Create the species description without any children. Passing
		// explicitly an empty list and not null;
		final SpeciesDescription mDesc =
				(SpeciesDescription) DescriptionFactory.create(micro, macro, Collections.EMPTY_LIST);
		cache.put(mDesc.getName(), mDesc);
		// Add it to its macro-species
		macro.addChild(mDesc);
		// Recursively create each micro-species of the newly added
		// micro-species
		final SyntacticVisitor visitor = element -> addMicroSpecies(mDesc, element, cache);
		micro.visitSpecies(visitor);
		micro.visitExperiments(visitor);
	}

	/**
	 * Adds the species node.
	 *
	 * @param element
	 *            the element
	 * @param speciesNodes
	 *            the species nodes
	 * @param collector
	 *            the collector
	 */
	void addSpeciesNode(final ISyntacticElement sse, final ValidationContext collector,
			final Map<String, ISyntacticElement> speciesNodes) {
		if (!(sse instanceof SyntacticSpeciesElement element)) return;
		final String name = element.getName();
		ISyntacticElement node = speciesNodes.get(name);
		if (node != null) {
			collector.add(new GamlCompilationError("Species " + name + " is declared twice",
					IGamlIssue.DUPLICATE_DEFINITION, element.getElement(), false, false));
			collector.add(new GamlCompilationError("Species " + name + " is declared twice",
					IGamlIssue.DUPLICATE_DEFINITION, node.getElement(), false, false));
		}
		speciesNodes.put(name, element);
	}

	/**
	 * Recursively complements a species and its micro-species. Add variables, behaviors (actions, reflex, task, states,
	 * ...), aspects to species.
	 *
	 * @param macro
	 *            the macro-species
	 * @param micro
	 *            the structure of micro-species
	 */
	void complementSpecies(final SpeciesDescription species, final ISyntacticElement node) {
		if (species == null) return;
		species.copyJavaAdditions();
		node.visitChildren(element -> {
			final IDescription childDesc = DescriptionFactory.create(element, species, null);
			if (childDesc != null) { species.addChild(childDesc); }
		});
		// recursively complement micro-species
		node.visitSpecies(element -> {
			final SpeciesDescription sd = species.getMicroSpecies(element.getName());
			if (sd != null) { complementSpecies(sd, element); }
		});

	}

	/**
	 * Parent experiment.
	 *
	 * @param model
	 *            the model
	 * @param micro
	 *            the micro
	 */
	void parentExperiment(final ModelDescription model, final ISyntacticElement micro) {
		// Gather the previously created species
		final SpeciesDescription mDesc = model.getExperiment(micro.getName());
		if (mDesc == null) return;
		final String p = mDesc.getLitteral(IKeyword.PARENT);
		// If no parent is defined, we assume it is "experiment"
		// No cache needed for experiments ??
		SpeciesDescription parent = model.getExperiment(p);
		if (parent == null) { parent = GamaMetaModel.getExperimentDescription(); }
		mDesc.setParent(parent);
	}

	/**
	 * Parent species.
	 *
	 * @param macro
	 *            the macro
	 * @param micro
	 *            the micro
	 * @param model
	 *            the model
	 * @param cache
	 *            the cache
	 */
	void parentSpecies(final SpeciesDescription macro, final ISyntacticElement micro, final ModelDescription model,
			final Map<String, SpeciesDescription> cache) {
		// Gather the previously created species
		final SpeciesDescription mDesc = cache.get(micro.getName());
		if (mDesc == null || mDesc.isExperiment()) return;
		String p = mDesc.getLitteral(IKeyword.PARENT);
		// If no parent is defined, we assume it is "agent"
		if (p == null) { p = IKeyword.AGENT; }
		SpeciesDescription parent = lookupSpecies(p, cache);
		if (parent == null) { parent = model.getSpeciesDescription(p); }
		mDesc.setParent(parent);
		micro.visitSpecies(element -> parentSpecies(mDesc, element, model, cache));

	}

	/**
	 * Lookup first in the cache passed in argument, then in the built-in species
	 *
	 * @param cache
	 * @return
	 */
	SpeciesDescription lookupSpecies(final String name, final Map<String, SpeciesDescription> cache) {
		SpeciesDescription result = cache.get(name);
		if (result == null) { result = Types.getBuiltInSpecies().get(name); }
		return result;
	}

	/**
	 * Builds the model name.
	 *
	 * @param source
	 *            the source
	 * @return the string
	 */
	protected String buildModelName(final String source) {
		return source.replace(' ', '_') + ModelDescription.MODEL_SUFFIX;
	}

}
