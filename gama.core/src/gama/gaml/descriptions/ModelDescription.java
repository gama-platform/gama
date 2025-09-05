/*******************************************************************************************************
 *
 * ModelDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import gama.core.kernel.simulation.SimulationAgent;
import gama.core.util.ConsumerWithPruning;
import gama.core.util.GamaMapFactory;
import gama.core.util.IMap;
import gama.gaml.compilation.IAgentConstructor;
import gama.gaml.compilation.kernel.GamaMetaModel;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.Facets;
import gama.gaml.types.IType;
import gama.gaml.types.ITypesManager;
import gama.gaml.types.Types;
import gama.gaml.types.TypesManager;

/**
 * The Class ModelDescription.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 12 janv. 2024
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class ModelDescription extends SpeciesDescription {

	/** The Constant MODEL_SUFFIX. */
	// TODO Move elsewhere
	public static final String MODEL_SUFFIX = "_model";

	/** The root. */
	public static volatile ModelDescription ROOT;

	/** The built in models. */
	public static volatile Map<String, ModelDescription> BUILT_IN_MODELS = new HashMap();

	/** The experiments. */
	private IMap<String, ExperimentDescription> experiments;

	/** The classes. */
	private IMap<String, ClassDescription> classes;

	/** The types. */
	final ITypesManager types;

	/** The model file path. */
	private String modelFilePath;

	/** The model project path. */
	private final String modelProjectPath;

	/** The alternate paths. */
	private final Set<String> alternatePaths;

	/** The validation context. */
	private final ValidationContext validationContext;

	/** The document. */
	// protected volatile boolean document;

	/** The micro models. */
	// hqnghi new attribute manipulate micro-models
	private IMap<String, ModelDescription> microModels;

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
	public Collection<String> getAlternatePaths() {
		return alternatePaths == null ? Collections.EMPTY_LIST : alternatePaths;
	}

	/**
	 * Gets the micro model.
	 *
	 * @param name
	 *            the name
	 * @return the micro model
	 */
	public ModelDescription getMicroModel(final String name) {
		if (microModels == null) return null;
		return microModels.get(name);
	}

	/**
	 * Sets the alias.
	 *
	 * @param as
	 *            the new alias
	 */
	public void setAlias(final String as) { alias = as; }

	/**
	 * Gets the alias.
	 *
	 * @return the alias
	 */
	public String getAlias() { return alias; }

	/**
	 * Checks if is micro model.
	 *
	 * @return true, if is micro model
	 */
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
			final EObject source, final SpeciesDescription macro, final SpeciesDescription parent,
			final Iterable<? extends IDescription> children, final Facets facets,
			final ValidationContext validationContext, final Set<String> imports, final IAgentConstructor helper,
			final Set<String> skills) {
		super(MODEL, clazz, macro == null ? GamaMetaModel.getExperimentDescription() : macro, parent, children, source,
				facets, skills);
		setName(name);
		types = parent instanceof ModelDescription ? new TypesManager(((ModelDescription) parent).types)
				: Types.builtInTypes;
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
			final EObject source, final SpeciesDescription macro, final SpeciesDescription parent,
			final Iterable<? extends IDescription> children, final Facets facets,
			final ValidationContext validationContext, final Set<String> imports, final IAgentConstructor helper) {
		this(name, clazz, projectPath, modelPath, source, macro, parent, children, facets, validationContext, imports,
				helper, Collections.EMPTY_SET);
	}

	@Override
	public SymbolSerializer createSerializer() {
		return MODEL_SERIALIZER;
	}

	@Override
	public String getTitle() { return getName().replace(MODEL_SUFFIX, ""); }

	// hqnghi does it need to verify parent of micro-model??
	@Override
	protected boolean verifyParent() {
		if (parent == ModelDescription.ROOT) return true;
		return super.verifyParent();
	}

	// end-hqnghi

	@Override
	public void markAttributeRedefinition(final VariableDescription existingVar, final VariableDescription newVar) {
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
	public void documentThis(final Doc sb) {
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
	public String getModelFilePath() { return modelFilePath; }

	/**
	 * Gets the model folder path.
	 *
	 * @return the model folder path
	 */
	public String getModelFolderPath() { return new File(modelFilePath).getParent(); }

	/**
	 * Gets the model project path.
	 *
	 * @return the model project path
	 */
	public String getModelProjectPath() { return modelProjectPath; }

	/**
	 * Create types from the species descriptions
	 */
	public void buildTypes() {
		types.init(this);
	}

	@Override
	public IDescription addChild(final IDescription child) {
		if (child == null) return null;
		switch (child) {
			case ModelDescription md -> {
				md.getTypesManager().setParent(getTypesManager());
				if (microModels == null) { microModels = GamaMapFactory.create(); }
				microModels.put(((ModelDescription) child).getAlias(), (ModelDescription) child);
				// no return as the species md must also be added as a child
			}
			case ClassDescription cd -> {
				final String s = child.getName();
				if (classes == null) { classes = GamaMapFactory.createOrdered(); }
				classes.put(s, cd);
				return cd;
			}
			case ExperimentDescription ed -> {
				final String s = ed.getName();
				if (experiments == null) { experiments = GamaMapFactory.createOrdered(); }
				experiments.put(s, ed);
				return ed;
			}
			default -> {
			}
		}
		super.addChild(child);
		return child;
	}

	@Override
	public void addOwnAttribute(final VariableDescription vd) {
		setIf(Flag.StartingDateDefined, !vd.isBuiltIn() && SimulationAgent.STARTING_DATE.equals(vd.getName()));
		super.addOwnAttribute(vd);
	}

	/**
	 * Checks if is starting date defined.
	 *
	 * @return true, if is starting date defined
	 */
	public boolean isStartingDateDefined() { return isSet(Flag.StartingDateDefined); }

	/**
	 * Checks for experiment.
	 *
	 * @param nameOrTitle
	 *            the name or title
	 * @return true, if successful
	 */
	public boolean hasExperiment(final String nameOrTitle) {
		if (experiments == null) return false;
		if (experiments.containsKey(nameOrTitle)) return true;
		for (final ExperimentDescription exp : experiments.values()) {
			String s = exp.getExperimentTitleFacet();
			if (s != null && s.equals(nameOrTitle)) return true;
		}
		return false;
	}

	@Override
	public ModelDescription getModelDescription() { return this; }

	@Override
	public SpeciesDescription getSpeciesDescription(final String spec) {
		if (spec.equals(getName()) || importedModelNames != null && importedModelNames.contains(spec)) return this;
		if (EXPERIMENT.equals(spec) && gama.core.runtime.GAMA.getExperiment() != null)
			return gama.core.runtime.GAMA.getExperiment().getDescription();
		ITypesManager tm = getTypesManager();
		if (tm != null) {
			IType t = tm.get(spec);
			TypeDescription td = t.getSpecies();
			if (td instanceof SpeciesDescription sd) return sd;
		}
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
	public ITypesManager getTypesManager() { return types; }

	@Override
	public SpeciesDescription getSpeciesContext() { return this; }

	/**
	 * Gets the experiment names.
	 *
	 * @return the experiment names
	 */
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
	public ValidationContext getValidationContext() { return validationContext; }

	/**
	 * Gets the experiment.
	 *
	 * @param name
	 *            the name
	 * @return the experiment
	 */
	public ExperimentDescription getExperiment(final String name) {
		if (experiments == null) return null;
		final ExperimentDescription desc = experiments.get(name);
		if (desc == null) {
			for (final ExperimentDescription ed : experiments.values()) {
				if (ed.getExperimentTitleFacet().equals(name)) return ed;
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
			for (final ActionDescription action : actions.values()) {
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
	public void setImportedModelNames(final Collection<String> allModelNames) { importedModelNames = allModelNames; }

	/**
	 * Returns all the species including the model itself, all the micro-species and the experiments
	 *
	 * @return
	 */

	public void visitAllSpecies(final ConsumerWithPruning<SpeciesDescription> visitor) {
		visitor.process(this);
		if (!visitMicroSpecies(new DescriptionVisitor<SpeciesDescription>() {

			@Override
			public boolean process(final SpeciesDescription desc) {
				visitor.process(desc);
				return desc.visitMicroSpecies(this);
			}
		})) return;
		if (experiments != null) { experiments.forEachValue(visitor); }
	}

	/**
	 * Visit all classes.
	 *
	 * @param visitor
	 *            the visitor
	 */
	public void visitAllClasses(final ConsumerWithPruning<ClassDescription> visitor) {
		if (classes == null) return;
		classes.forEachValue(desc -> visitor.process(desc));
	}

	/**
	 * Gets the all species.
	 *
	 * @param accumulator
	 *            the accumulator
	 * @return the all species
	 */
	public void getAllSpecies(final List<SpeciesDescription> accumulator) {
		final DescriptionVisitor<SpeciesDescription> visitor = desc -> {
			accumulator.add(desc);
			return true;
		};
		visitAllSpecies(visitor);
	}

	/**
	 * Gets the all classes.
	 *
	 * @param accumulator
	 *            the accumulator
	 * @return the all classes
	 */
	public void getAllClasses(final List<ClassDescription> accumulator) {
		final DescriptionVisitor<ClassDescription> visitor = desc -> {
			accumulator.add(desc);
			return true;
		};
		visitAllClasses(visitor);
	}

	@Override
	protected boolean parentIsVisible() {
		if (!getParent().isModel()) return false;
		if (parent.isBuiltIn()) return true;

		return false;
	}

	/**
	 * @param p
	 * @return
	 */
	public ClassDescription getClassDescription(final String p) {
		if (classes == null) return null;
		return classes.get(p);
	}

}
