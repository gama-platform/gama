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
import gama.api.data.factories.GamaMapFactory;
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

/**
 * The Class ModelDescription.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 12 janv. 2024
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
		super(IKeyword.MODEL, clazz, macro == null ? GamaMetaModel.getExperimentDescription() : macro, parent, children,
				source, facets, skills);
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

		if (child instanceof ExperimentDescription) {
			final String s = child.getName();
			if (experiments == null) { experiments = GamaMapFactory.createOrdered(); }
			experiments.put(s, (ExperimentDescription) child);
		} else {
			super.addChild(child);
		}

		return child;
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
		if (experiments == null) return false;
		if (experiments.containsKey(nameOrTitle)) return true;
		for (final ExperimentDescription exp : experiments.values()) {
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
		if (hasMicroSpecies()) return getMicroSpecies().get(spec);
		return null;
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
	public Set<String> getExperimentNames() {
		if (experiments == null) return Collections.EMPTY_SET;
		return new LinkedHashSet(experiments.keySet());
	}

	/**
	 * Gets the experiment titles.
	 *
	 * @return the experiment titles
	 */
	public Set<String> getExperimentTitles() {
		final Set<String> strings = new LinkedHashSet();
		if (experiments != null) {
			experiments.forEachPair((a, b) -> {
				if (b.getOriginName().equals(getName())) { strings.add(b.getExperimentTitleFacet()); }
				return true;
			});
		}
		return strings;
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
		if (experiments == null) return null;
		final ExperimentDescription desc = experiments.get(name);
		if (desc == null) {
			for (final ExperimentDescription ed : experiments.values()) {
				final String title = ed.getExperimentTitleFacet();
				if (title != null && title.equals(name)) return ed;
			}
		}
		return desc;
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		boolean result = super.visitChildren(visitor);
		if (result && experiments != null) { result &= experiments.forEachValue(visitor); }
		return result;
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		if (!super.visitOwnChildren(visitor) || experiments != null && !experiments.forEachValue(visitor)) return false;
		return true;
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		final DescriptionVisitor<IDescription> recursiveVisitor = each -> {
			if (!visitor.process(each)) return false;
			return each.visitOwnChildrenRecursively(visitor);
		};
		if (!super.visitOwnChildrenRecursively(visitor)
				|| experiments != null && !experiments.forEachValue(recursiveVisitor))
			return false;
		return true;
	}

	@Override
	public boolean finalizeDescription() {
		if (!super.finalizeDescription()) return false;
		if (actions != null) {
			for (final IActionDescription action : actions.values()) {
				if (action.isAbstract() && (action.getUnderlyingElement() == null
						|| !action.getUnderlyingElement().eResource().equals(getUnderlyingElement().eResource()))) {
					this.error("Abstract action '" + action.getName() + "', defined in " + action.getOriginName()
							+ ", should be redefined.", IGamlIssue.MISSING_ACTION);
					return false;
				}
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
	public Collection<? extends ExperimentDescription> getExperiments() {
		if (experiments == null) return Collections.EMPTY_LIST;
		return experiments.values();
	}

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
		if (experiments != null) { experiments.forEachValue(visitor); }
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
