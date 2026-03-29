/*******************************************************************************************************
 *
 * ExperimentDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.descriptions;

import static gama.annotations.constants.IKeyword.NAME;
import static gama.annotations.constants.IKeyword.PARAMETER;
import static gama.api.constants.IGamlIssue.DUPLICATE_DEFINITION;
import static gama.api.constants.IGamlIssue.REDEFINES;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.collect.Iterables;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.agent.IAgentConstructor;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;

/**
 * The Class ExperimentDescription.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })

public class ExperimentDescription extends SpeciesDescription implements IExperimentDescription {

	/** The parameters. */
	private IMap<String, IVariableDescription> parameters;

	/** The output. */
	private IStatementDescription output;

	/** The permanent. */
	private IStatementDescription permanent;

	/**
	 * Instantiates a new experiment description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param enclosing
	 *            the enclosing
	 * @param cp
	 *            the cp
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 */
	public ExperimentDescription(final String keyword, final ISpeciesDescription enclosing,
			final Iterable<IDescription> cp, final EObject source, final Facets facets) {
		super(keyword, null, enclosing, null, cp, source, facets);
		String type = getLitteral(IKeyword.TYPE);
		setIf(Flag.isBatch, IKeyword.BATCH.equals(type));
		setIf(Flag.isMemorize, facets.containsKey(IKeyword.RECORD));
	}

	/**
	 * Instantiates a new experiment description.
	 *
	 * @param name
	 *            the name
	 * @param clazz
	 *            the clazz
	 * @param superDesc
	 *            the super desc
	 * @param parent
	 *            the parent
	 * @param helper
	 *            the helper
	 * @param skills2
	 *            the skills 2
	 * @param ff
	 *            the ff
	 * @param plugin
	 *            the plugin
	 */
	public ExperimentDescription(final String name, final Class<?> clazz, final ISpeciesDescription superDesc,
			final ISpeciesDescription parent, final IAgentConstructor helper, final Set<String> skills2,
			final Facets ff, final String plugin) {
		super(name, clazz, superDesc, parent, helper, skills2, ff, plugin);
	}

	/**
	 * Adds the parameter.
	 *
	 * @param var
	 *            the var
	 */
	private void addParameter(final IVariableDescription var) {
		if (parameters == null) { parameters = GamaMapFactory.create(); }
		String vName = var.getName();
		IVariableDescription existing = parameters.get(vName);
		if (existing != null) {
			existing.warning("'" + vName + "' is overwritten in this experiment and will not be used.",
					DUPLICATE_DEFINITION, NAME);
			var.warning("'" + vName + "' overwrites a previous definition.", DUPLICATE_DEFINITION, NAME);
		}
		IModelDescription md = this.getModelDescription();
		md.visitAllAttributes(d -> {
			VariableDescription vd = (VariableDescription) d;
			if (vName.equals(vd.getParameterName())) {
				// Possibly different resources
				final Resource newResource =
						var.getUnderlyingElement() == null ? null : var.getUnderlyingElement().eResource();
				final Resource existingResource =
						vd.getUnderlyingElement() == null ? null : vd.getUnderlyingElement().eResource();
				if (Objects.equals(newResource, existingResource)) {
					var.info("'" + vName + "' supersedes the parameter declaration in " + vd.getOriginName(), REDEFINES,
							NAME);
					vd.info("Parameter '" + vName + "' is redefined in experiment "
							+ var.getEnclosingDescription().getName(), DUPLICATE_DEFINITION, NAME);
				} else if (existingResource != null) {
					var.info("Redefinition of '" + vName + "' imported from " + existingResource.getURI().lastSegment(),
							REDEFINES, NAME);
				}
			}
			return true;
		}

		);
		parameters.put(var.getName(), var);
	}

	/**
	 * Checks for parameter.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean hasParameter(final String name) {
		if (parameters == null) return false;
		return parameters.containsKey(name);
	}

	/**
	 * Gets the parameter.
	 *
	 * @param name
	 *            the name
	 * @return the parameter
	 */
	public IVariableDescription getParameter(final String name) {
		if (parameters == null) return null;
		return parameters.get(name);
	}

	/**
	 * Inherit parameters from.
	 *
	 * @param p
	 *            the p
	 */
	public void inheritParametersFrom(final ExperimentDescription p) {
		if (p.parameters != null) {
			for (final IVariableDescription v : p.parameters.values()) { addInheritedParameter(v); }
		}
	}

	/**
	 * Adds the inherited parameter.
	 *
	 * @param vd
	 *            the vd
	 */
	public void addInheritedParameter(final IVariableDescription vd) {

		final String inheritedVarName = vd.getName();

		// If no previous definition is found, just add the parameter
		if (!hasParameter(inheritedVarName)) {
			addParameter(vd.copy(this));
			return;
		}
		// A redefinition has been found
		final IVariableDescription existing = getParameter(inheritedVarName);
		if (assertAttributesAreCompatible(vd, existing)) {
			if (!existing.isBuiltIn()) { markAttributeRedefinition(vd, existing); }
			existing.copyFrom(vd);
		}
	}

	/**
	 * Adds the inherited attribute.
	 *
	 * @param var
	 *            the var
	 */
	@Override
	public void addInheritedAttribute(final IVariableDescription var) {
		if (PARAMETER.equals(var.getKeyword())) {
			addParameter(var);
		} else {
			super.addInheritedAttribute(var);
		}
	}

	/**
	 * Adds the own attribute.
	 *
	 * @param var
	 *            the var
	 */
	@Override
	public void addOwnAttribute(final IVariableDescription var) {
		if (!PARAMETER.equals(var.getKeyword())) {
			super.addOwnAttribute(var);
		} else {
			addParameter(var);
		}
	}

	@Override
	public String getTitle() { return "experiment " + getName(); }

	/**
	 * Gets the experiment title facet.
	 *
	 * @return the experiment title facet
	 */
	@Override
	public String getExperimentTitleFacet() { return getLitteral(IKeyword.TITLE); }

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		if (!super.visitOwnChildren(visitor) || parameters != null && !parameters.forEachValue(visitor)) return false;
		if (output != null && !visitor.process(output) || permanent != null && !visitor.process(permanent))
			return false;
		return true;
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		final DescriptionVisitor<IDescription> recursiveVisitor = each -> {
			if (!visitor.process(each)) return false;
			return each.visitOwnChildrenRecursively(visitor);
		};
		if (!super.visitOwnChildrenRecursively(visitor)
				|| parameters != null && !parameters.forEachValue(recursiveVisitor))
			return false;
		if (output != null && !recursiveVisitor.process(output)
				|| permanent != null && !recursiveVisitor.process(permanent))
			return false;
		return true;
	}

	@Override
	public Iterable<IDescription> getOwnChildren() {
		return Iterables.concat(super.getOwnChildren(),
				parameters == null ? Collections.EMPTY_LIST : parameters.values(),
				output == null ? Collections.EMPTY_LIST : Collections.singleton(output),
				permanent == null ? Collections.EMPTY_LIST : Collections.singleton(permanent));
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		boolean result = super.visitChildren(visitor);
		if (!result) return false;
		if (parameters != null) { result &= parameters.forEachValue(visitor); }
		if (!result) return false;
		if (output != null) { result &= visitor.process(output); }
		if (!result) return false;
		if (permanent != null) { result &= visitor.process(permanent); }
		return result;
	}

	@Override
	public Class<? extends IExperimentAgent> getJavaBase() {
		String type = getLitteral(IKeyword.TYPE);
		return IExperimentDescription.getJavaBaseFor(type);
		// return IKeyword.BATCH.equals(type) ? BatchAgent.class : ExperimentAgent.class;
	}

	@Override
	public void inheritFromParent() {
		// Takes care of invalid species (see Issue 711)
		if (parent != null && parent != this && !parent.isBuiltIn()) {
			super.inheritFromParent();
			inheritParametersFrom((ExperimentDescription) parent);
			inheritOutputsFrom((ExperimentDescription) parent);
		}
	}

	/**
	 * Inherit outputs from.
	 *
	 * @param parent
	 *            the parent
	 */
	private void inheritOutputsFrom(final ExperimentDescription parent) {
		if (parent.output != null) {
			if (output == null) {
				output = (IStatementDescription) parent.output.copy(this);
			} else {
				mergeOutputs(parent.output, output);
			}
		}
		if (parent.permanent != null) {
			if (permanent == null) {
				permanent = (IStatementDescription) parent.permanent.copy(this);
			} else {
				mergeOutputs(parent.permanent, permanent);
			}
		}
	}

	/**
	 * Merge outputs.
	 *
	 * @param inherited
	 *            the inherited
	 * @param defined
	 *            the defined
	 */
	private void mergeOutputs(final IStatementDescription inherited, final IStatementDescription defined) {
		inherited.visitChildren(in -> {
			final IDescription redefined = getSimilarChild(defined, in);
			if (redefined == null) {
				defined.addChild(in.copy(defined));
			} else {
				redefined.info("Redefinition of " + redefined.getName() + " from " + in.getTypeContext().getName(),
						IGamlIssue.REDEFINES, NAME);
			}
			return true;
		});

	}

	@Override
	protected void addBehavior(final IStatementDescription r) {
		if (IKeyword.OUTPUT.equals(r.getKeyword())) {
			output = r;
		} else if (IKeyword.PERMANENT.equals(r.getKeyword())) {
			permanent = r;
		} else {
			super.addBehavior(r);
		}
	}

	@Override
	protected boolean parentIsVisible() {
		if (!getParent().isExperiment()) return false;
		if (parent.isBuiltIn()) return true;
		final ModelDescription host = (ModelDescription) getMacroSpecies();
		if (host != null && host.getExperiment(parent.getName()) != null) return true;
		return false;
	}

	/**
	 * Visit micro species.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean visitMicroSpecies(final DescriptionVisitor<ISpeciesDescription> visitor) {
		return true;
	}

	/**
	 * Checks if is class.
	 *
	 * @return true, if is class
	 */
	@Override
	public boolean isClass() { return false; }

	/**
	 * Checks if is species.
	 *
	 * @return true, if is species
	 */
	@Override
	public boolean isSpecies() { return false; }

	/**
	 * Checks if is experiment.
	 *
	 * @return true, if is experiment
	 */
	@Override
	public boolean isExperiment() { return true; }

	/**
	 * Checks if is model.
	 *
	 * @return true, if is model
	 */
	@Override
	public boolean isModel() { return false; }

	/**
	 * Checks if is skill.
	 *
	 * @return true, if is skill
	 */
	@Override
	public boolean isSkill() { return false; }

}
