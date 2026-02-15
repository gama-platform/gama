/*******************************************************************************************************
 *
 * ModelDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static gama.api.constants.IKeyword.NAME;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.compilation.validation.IValidationContext;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IMap;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gama.api.gaml.types.TypesManager;
import gama.api.kernel.GamaMetaModel;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.utils.ConsumerWithPruning;
import gama.api.utils.map.GamaMapFactory;

/**
 * Root description for complete GAML models, representing the top-level container of all model elements.
 *
 * <p>
 * ModelDescription extends {@link SpeciesDescription} as models can be thought of as the "world species" that contains
 * all other species, global variables, and experiments. It provides additional functionality for managing experiments,
 * type systems, file paths, and model imports.
 * </p>
 *
 * <p>
 * <strong>Architectural Position:</strong>
 * </p>
 *
 * <pre>
 * ModelDescription (root of description hierarchy)
 *   ├── Global Section (attributes, actions)
 *   ├── Species Definitions
 *   │   ├── Regular Species
 *   │   ├── Grid Species
 *   │   └── Micro-species (nested)
 *   ├── Experiment Definitions
 *   │   ├── GUI Experiments
 *   │   ├── Batch Experiments
 *   │   └── Test Experiments
 *   └── Type System (TypesManager)
 * </pre>
 *
 * <p>
 * <strong>Key Responsibilities:</strong>
 * </p>
 * <ul>
 * <li><strong>Model Container:</strong> Root element containing all species, experiments, and global definitions</li>
 * <li><strong>Experiment Management:</strong> Stores and manages experiment descriptions for this model</li>
 * <li><strong>Type System:</strong> Maintains the type manager for type resolution and validation</li>
 * <li><strong>Path Management:</strong> Tracks model file path and project path for resource loading</li>
 * <li><strong>Import System:</strong> Manages imported models (micro-models) and alternate search paths</li>
 * <li><strong>Validation Context:</strong> Provides context for validation (error/warning reporting)</li>
 * </ul>
 *
 * <p>
 * <strong>Example GAML Model Structure:</strong>
 * </p>
 *
 * <pre>{@code
 * model TrafficSimulation        // ModelDescription
 *
 * global {                        // Global section (attributes/actions)
 *   int nb_cars <- 100;
 *   init {
 *     create car number: nb_cars;
 *   }
 * }
 *
 * species car skills: [moving] {  // Species definitions
 *   float speed <- 50.0 #km/#h;
 *   reflex move {
 *     do wander;
 *   }
 * }
 *
 * grid road width: 100 height: 100 {  // Grid species
 *   rgb color <- #gray;
 * }
 *
 * experiment MyExperiment type: gui {  // Experiment definition
 *   parameter "Number of cars" var: nb_cars;
 *   output {
 *     display "Main" {
 *       species car;
 *       grid road;
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>
 * <strong>Type System Management:</strong>
 * </p>
 * <p>
 * Each model has its own {@link ITypesManager} that extends the built-in type system:
 * </p>
 * <ul>
 * <li><strong>Built-in Types:</strong> int, float, bool, string, agent, etc.</li>
 * <li><strong>Species Types:</strong> Each species becomes a type</li>
 * <li><strong>Custom Types:</strong> Type definitions in the model</li>
 * <li><strong>Type Hierarchy:</strong> Tracks parent-child relationships for type compatibility</li>
 * </ul>
 *
 * <p>
 * <strong>Micro-Model System:</strong>
 * </p>
 * <p>
 * Models can import other models as micro-models for composition and reuse:
 * </p>
 *
 * <pre>{@code
 * import "utilities.gaml" as utils;   // Import with alias
 *
 * model MainModel {
 *   // Can reference species from utils
 *   species myAgent parent: utils.baseAgent {
 *     // ...
 *   }
 * }
 * }</pre>
 *
 * <p>
 * <strong>Experiment Management:</strong>
 * </p>
 * <ul>
 * <li><strong>Multiple Experiments:</strong> One model can define multiple experiments</li>
 * <li><strong>Experiment Types:</strong> gui (interactive), batch (automated), test (unit testing)</li>
 * <li><strong>Parameter Spaces:</strong> Experiments define parameter ranges for exploration</li>
 * <li><strong>Output Definitions:</strong> Displays, monitors, and export configurations</li>
 * </ul>
 *
 * <p>
 * <strong>Path Management:</strong>
 * </p>
 * <ul>
 * <li><strong>modelFilePath:</strong> Absolute path to the model file (e.g., /path/to/model.gaml)</li>
 * <li><strong>modelProjectPath:</strong> Path to the containing project</li>
 * <li><strong>alternatePaths:</strong> Additional search paths for imports and resources</li>
 * </ul>
 *
 * <p>
 * <strong>Memory Optimization:</strong>
 * </p>
 * <ul>
 * <li>Experiments stored in lazy-initialized map</li>
 * <li>Type manager shared with parent model if in micro-model scenario</li>
 * <li>Alternate paths use Set to avoid duplicates</li>
 * <li>Validation context shared across model</li>
 * </ul>
 *
 * <p>
 * <strong>Performance Considerations:</strong>
 * </p>
 * <ul>
 * <li><strong>Type Resolution:</strong> Cached in TypesManager for fast lookups</li>
 * <li><strong>Experiment Access:</strong> O(1) lookup by name</li>
 * <li><strong>Species Access:</strong> Inherited from SpeciesDescription</li>
 * <li><strong>Validation:</strong> Can be expensive for large models (1000s of symbols)</li>
 * </ul>
 *
 * <p>
 * <strong>Optimization Opportunities:</strong>
 * </p>
 * <ol>
 * <li><strong>Experiment Lazy Loading:</strong> Load experiment descriptions on demand</li>
 * <li><strong>Type Cache:</strong> Aggressive caching of type lookups</li>
 * <li><strong>Path Normalization:</strong> Normalize paths once at construction</li>
 * <li><strong>Validation Caching:</strong> Cache validation results per file hash</li>
 * <li><strong>Resource Pooling:</strong> Share validation context across multiple validations</li>
 * </ol>
 *
 * <p>
 * <strong>Thread Safety:</strong>
 * </p>
 * <p>
 * NOT thread-safe during construction and validation. Thread-safe for read-only operations after validation completes
 * (experiment lookup, type resolution, etc.).
 * </p>
 *
 * <p>
 * <strong>Lifecycle:</strong>
 * </p>
 * <ol>
 * <li><strong>Creation:</strong> Factory creates from parsed AST</li>
 * <li><strong>Population:</strong> Species and experiments added</li>
 * <li><strong>Validation:</strong> Semantic validation performed</li>
 * <li><strong>Compilation:</strong> Compiled to runtime model</li>
 * <li><strong>Execution:</strong> Experiments run against compiled model</li>
 * <li><strong>Disposal:</strong> Clean up resources when model unloaded</li>
 * </ol>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since 12 janv. 2024
 * @see SpeciesDescription
 * @see IModelDescription
 * @see ExperimentDescription
 * @see ITypesManager
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class ModelDescription extends SpeciesDescription implements IModelDescription {

	/** The experiments. */
	private IMap<String, ExperimentDescription> experiments;

	/** The types. */
	final ITypesManager types;

	/** The model file path. */
	private String modelFilePath;

	/** The model project path. */
	private final String modelProjectPath;

	/** The alternate paths. */
	private final Set<String> alternatePaths;

	/** The validation context. */
	private final IValidationContext validationContext;

	/** The document. */
	// protected volatile boolean document;

	/** The micro models. */
	// hqnghi new attribute manipulate micro-models
	private IMap<String, IModelDescription> microModels;

	/** The alias. */
	private String alias = "";

	/** The is starting date defined. */
	// boolean isStartingDateDefined = false;

	/** The imported model names. */
	private Collection<String> importedModelNames;

	/**
	 * Gets the alternate paths.
	 *
	 * @return the alternate paths
	 */
	@Override
	public Collection<String> getAlternatePaths() {
		return alternatePaths == null ? Collections.emptyList() : alternatePaths;
	}

	/**
	 * Gets the micro model.
	 *
	 * @param name
	 *            the name
	 * @return the micro model
	 */
	@Override
	public IModelDescription getMicroModel(final String name) {
		if (microModels == null) return null;
		return microModels.get(name);
	}

	/**
	 * Sets the alias.
	 *
	 * @param as
	 *            the new alias
	 */
	@Override
	public void setAlias(final String as) { alias = as; }

	/**
	 * Gets the alias.
	 *
	 * @return the alias
	 */
	@Override
	public String getAlias() { return alias; }

	/**
	 * Checks if is micro model.
	 *
	 * @return true, if is micro model
	 */
	@Override
	public boolean isMicroModel() { return alias != null && !alias.isEmpty(); }

	@Override
	public boolean isModel() { return true; }

	// end-hqnghi

	/**
	 * Instantiates a new model description.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param projectPath
	 *            the project path
	 * @param modelPath
	 *            the model path
	 * @param source
	 *            the source
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @param validationContext
	 *            the validation context
	 * @param imports
	 *            the imports
	 * @param helper
	 *            the helper
	 * @param skills
	 *            the skills
	 */
	public ModelDescription(final String name, final Class clazz, final String projectPath, final String modelPath,
			final EObject source, final ISpeciesDescription macro, final ISpeciesDescription parent,
			final Iterable<? extends IDescription> children, final Facets facets,
			final IValidationContext validationContext, final Set<String> imports, final IAgentConstructor helper,
			final Set<String> skills) {
		super(IKeyword.MODEL, clazz, macro == null ? GamaMetaModel.getSpeciesDescription(IKeyword.EXPERIMENT) : macro,
				parent, children, source, facets, skills);
		setName(name);
		types = parent instanceof ModelDescription m ? new TypesManager(m.types) : Types.builtInTypes;
		modelFilePath = modelPath;
		modelProjectPath = projectPath;
		this.validationContext = validationContext;
		this.alternatePaths = imports;
		if (helper != null) { setAgentConstructor(helper); }

	}

	/**
	 * Instantiates a new model description.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param projectPath
	 *            the project path
	 * @param modelPath
	 *            the model path
	 * @param source
	 *            the source
	 * @param macro
	 *            the macro
	 * @param parent
	 *            the parent
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @param validationContext
	 *            the validation context
	 * @param imports
	 *            the imports
	 * @param helper
	 *            the helper
	 */
	public ModelDescription(final String name, final Class clazz, final String projectPath, final String modelPath,
			final EObject source, final ISpeciesDescription macro, final ISpeciesDescription parent,
			final Iterable<? extends IDescription> children, final Facets facets,
			final IValidationContext validationContext, final Set<String> imports, final IAgentConstructor helper) {
		this(name, clazz, projectPath, modelPath, source, macro, parent, children, facets, validationContext, imports,
				helper, Collections.EMPTY_SET);
	}

	@Override
	public ISymbolSerializer createSerializer() {
		return MODEL_SERIALIZER;
	}

	@Override
	public String getTitle() { return getName().replace(MODEL_SUFFIX, ""); }

	// hqnghi does it need to verify parent of micro-model??
	@Override
	protected boolean verifyParent() {
		if (parent == IModelDescription.ROOT[0]) return true;
		return super.verifyParent();
	}

	// end-hqnghi

	/**
	 * Mark attribute redefinition.
	 *
	 * @param existingVar
	 *            the existing var
	 * @param newVar
	 *            the new var
	 */
	@Override
	public void markAttributeRedefinition(final IVariableDescription existingVar, final IVariableDescription newVar) {
		if (newVar.isBuiltIn()) return;
		if (existingVar.isBuiltIn()) {
			newVar.info(
					"This definition of " + newVar.getName() + " supersedes the one in " + existingVar.getOriginName(),
					IGamlIssue.REDEFINES, NAME);
			return;
		}

		final EObject newResource = newVar.getUnderlyingElement().eContainer();
		final EObject existingResource = existingVar.getUnderlyingElement().eContainer();
		if (Objects.equals(newResource, existingResource)) {
			existingVar.error("Attribute " + newVar.getName() + " is defined twice", IGamlIssue.DUPLICATE_DEFINITION,
					NAME);
			newVar.error("Attribute " + newVar.getName() + " is defined twice", IGamlIssue.DUPLICATE_DEFINITION, NAME);
			return;
		}
		if (existingResource != null) {
			newVar.info("This definition of " + newVar.getName() + " supersedes the one in imported file "
					+ existingResource.eResource().getURI().lastSegment(), IGamlIssue.REDEFINES, NAME);
		}
	}

	@Override
	public void documentThis(final IGamlDocumentation sb) {
		final String parentName = getParent() == null ? "nil" : getParent().getName();
		sb.append("<b>Subspecies of:</b> ").append(parentName).append("<br>");
		final Iterable<String> skills = getSkillsNames();
		if (!Iterables.isEmpty(skills)) { sb.append("<b>Skills:</b> ").append(skills.toString()).append("<br>"); }
		documentAttributes(sb);
		documentActions(sb);
	}

	/**
	 * Relocates the working path. The last segment must not end with a "/"
	 *
	 * @param path
	 */
	public void setWorkingDirectory(final String path) {
		modelFilePath = path + File.separator + new File(modelFilePath).getName();
	}

	@Override
	public String toString() {
		if (modelFilePath == null || modelFilePath.isEmpty()) return "abstract model " + getName();
		return "description of " + modelFilePath.substring(modelFilePath.lastIndexOf(File.separator));
	}

	@Override
	public void dispose() {
		if (isBuiltIn()) return;
		super.dispose();
		experiments = null;
		types.dispose();

	}

	/**
	 * Gets the model file name.
	 *
	 * @return the model file name
	 */
	@Override
	public String getModelFilePath() { return modelFilePath; }

	/**
	 * Gets the model folder path.
	 *
	 * @return the model folder path
	 */
	@Override
	public String getModelFolderPath() { return new File(modelFilePath).getParent(); }

	/**
	 * Gets the model project path.
	 *
	 * @return the model project path
	 */
	@Override
	public String getModelProjectPath() { return modelProjectPath; }

	/**
	 * Create types from the species descriptions
	 */
	@Override
	public void buildTypes() {
		types.init(this);
	}

	@Override
	public IDescription addChild(final IDescription child) {
		if (child == null) return null;
		if (child instanceof ModelDescription md) {
			md.getTypesManager().setParent(getTypesManager());
			if (microModels == null) { microModels = GamaMapFactory.create(); }
			microModels.put(((ModelDescription) child).getAlias(), (ModelDescription) child);
		} // no else as models are also species, which should be added after.

		if (child instanceof ExperimentDescription exp) {
			getExperimentsMap().put(exp.getName(), exp);
		} else {
			super.addChild(child);
		}
		return child;
	}

	/**
	 * Gets the own experiments.
	 *
	 * @return the own experiments
	 */
	public IMap<String, ExperimentDescription> getOwnExperiments() {
		return experiments == null ? GamaMapFactory.EMPTY : experiments;
	}

	/**
	 * Gets the experiments map.
	 *
	 * @return the experiments map
	 */
	public IMap<String, ExperimentDescription> getExperimentsMap() {
		if (experiments == null) { experiments = GamaMapFactory.create(); }
		return experiments;
	}

	/**
	 * Adds the own attribute.
	 *
	 * @param vd
	 *            the vd
	 */
	@Override
	public void addOwnAttribute(final IVariableDescription vd) {
		setIf(Flag.StartingDateDefined, !vd.isBuiltIn() && ISimulationAgent.STARTING_DATE.equals(vd.getName()));
		super.addOwnAttribute(vd);
	}

	/**
	 * Checks if is starting date defined.
	 *
	 * @return true, if is starting date defined
	 */
	@Override
	public boolean isStartingDateDefined() { return isSet(Flag.StartingDateDefined); }

	/**
	 * Checks for experiment.
	 *
	 * @param nameOrTitle
	 *            the name or title
	 * @return true, if successful
	 */
	@Override
	public boolean hasExperiment(final String nameOrTitle) {
		if (getOwnExperiments().containsKey(nameOrTitle)) return true;
		for (final ExperimentDescription exp : getOwnExperiments().values()) {
			final String s = exp.getExperimentTitleFacet();
			if (s != null && s.equals(nameOrTitle)) return true;
		}
		return false;
	}

	@Override
	public ModelDescription getModelDescription() { return this; }

	@Override
	public ISpeciesDescription getSpeciesDescription(final String spec) {
		if (spec.equals(getName()) || importedModelNames != null && importedModelNames.contains(spec)) return this;
		if (IKeyword.EXPERIMENT.equals(spec) && gama.api.GAMA.getExperiment() != null)
			return gama.api.GAMA.getExperiment().getDescription();
		if (getTypesManager() != null) return getTypesManager().get(spec).getSpecies();
		return getOwnMicroSpecies().get(spec);
	}

	@Override
	public IType getTypeNamed(final String s) {
		if (types == null) return Types.NO_TYPE;
		return types.get(s);
	}

	/**
	 * Gets the types manager.
	 *
	 * @return the types manager
	 */
	@Override
	public ITypesManager getTypesManager() { return types; }

	@Override
	public SpeciesDescription getSpeciesContext() { return this; }

	/**
	 * Gets the experiment names.
	 *
	 * @return the experiment names
	 */
	@Override
	public Set<String> getExperimentNames() { return new LinkedHashSet(getOwnExperiments().keySet()); }

	/**
	 * Gets the experiment titles.
	 *
	 * @return the experiment titles
	 */
	public Set<String> getExperimentTitles() {
		return getOwnExperiments().values().stream().filter(b -> b.getOriginName().equals(getName()))
				.map(ExperimentDescription::getExperimentTitleFacet)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public IValidationContext getValidationContext() { return validationContext; }

	/**
	 * Gets the experiment.
	 *
	 * @param name
	 *            the name
	 * @return the experiment
	 */
	@Override
	public ExperimentDescription getExperiment(final String name) {
		final ExperimentDescription desc = getOwnExperiments().get(name);
		if (desc == null) {
			for (final ExperimentDescription ed : getOwnExperiments().values()) {
				final String title = ed.getExperimentTitleFacet();
				if (title != null && title.equals(name)) return ed;
			}
		}
		return desc;
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		return super.visitChildren(visitor) && getOwnExperiments().forEachValue(visitor);
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		return super.visitOwnChildren(visitor) && getOwnExperiments().forEachValue(visitor);
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		final DescriptionVisitor<IDescription> recursiveVisitor = each -> {
			if (!visitor.process(each)) return false;
			return each.visitOwnChildrenRecursively(visitor);
		};
		return super.visitOwnChildrenRecursively(visitor) && getOwnExperiments().forEachValue(recursiveVisitor);
	}

	@Override
	public boolean finalizeDescription() {
		if (!super.finalizeDescription()) return false;
		for (final IActionDescription action : getOwnActions().values()) {
			if (action.isAbstract() && (action.getUnderlyingElement() == null
					|| !action.getUnderlyingElement().eResource().equals(getUnderlyingElement().eResource()))) {
				this.error("Abstract action '" + action.getName() + "', defined in " + action.getOriginName()
						+ ", should be redefined.", IGamlIssue.MISSING_ACTION);
				return false;
			}
		}
		return true;
	}

	@Override
	public ModelDescription validate() {
		if (!isSet(Flag.Validated)) {
			super.validate();
			validationContext.doDocument(this);
		}
		return this;
	}

	/**
	 * @return
	 */
	@Override
	public Collection<? extends ExperimentDescription> getExperiments() { return getOwnExperiments().values(); }

	/**
	 * Sets the imported model names.
	 *
	 * @param allModelNames
	 *            the new imported model names
	 */
	@Override
	public void setImportedModelNames(final Collection<String> allModelNames) { importedModelNames = allModelNames; }

	/**
	 * Returns all the species including the model itself, all the micro-species and the experiments
	 *
	 * @return
	 */

	@Override
	public void visitAllSpecies(final ConsumerWithPruning<ISpeciesDescription> visitor) {
		visitor.process(this);
		if (!visitMicroSpecies(new DescriptionVisitor<ISpeciesDescription>() {

			@Override
			public boolean process(final ISpeciesDescription desc) {
				visitor.process(desc);
				return desc.visitMicroSpecies(this);
			}
		})) return;
		getOwnExperiments().forEachValue(visitor);
	}

	/**
	 * Gets the all species.
	 *
	 * @param accumulator
	 *            the accumulator
	 * @return the all species
	 */
	@Override
	public void getAllSpecies(final List<ISpeciesDescription> accumulator) {
		final DescriptionVisitor<ISpeciesDescription> visitor = desc -> {
			accumulator.add(desc);
			return true;
		};
		visitAllSpecies(visitor);
	}

	@Override
	protected boolean parentIsVisible() {
		if (!getParent().isModel()) return false;
		if (parent.isBuiltIn()) return true;

		return false;
	}

}
