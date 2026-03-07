/*******************************************************************************************************
 *
 * VariableDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static gama.annotations.constants.IKeyword.CONST;
import static gama.annotations.constants.IKeyword.FALSE;
import static gama.annotations.constants.IKeyword.FUNCTION;
import static gama.annotations.constants.IKeyword.INIT;
import static gama.annotations.constants.IKeyword.MIRRORS;
import static gama.annotations.constants.IKeyword.OF;
import static gama.annotations.constants.IKeyword.PARAMETER;
import static gama.annotations.constants.IKeyword.TRUE;
import static gama.annotations.constants.IKeyword.TYPE;
import static gama.annotations.constants.IKeyword.UPDATE;
import static gama.annotations.constants.IKeyword.VALUE;
import static gama.api.compilation.descriptions.IDescription.Flag.Unmodifiable;
import static gama.api.compilation.descriptions.IDescription.Flag.Updatable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import gama.annotations.doc;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.ITypeProvider;
import gama.api.additions.GamaHelper;
import gama.api.additions.IGamaHelper;
import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.GamaIntegerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.utils.collections.Collector;
import gama.api.utils.collections.ICollector;

/**
 * Written by drogoul Modified on 16 mai 2010
 *
 * @todo Description
 *
 */
public class VariableDescription extends SymbolDescription implements IVariableDescription {

	/** The plugin. */
	private String plugin;

	/** The built in doc. */
	private Class<?> definitionClass;
	/** The set. */
	private IGamaHelper<?> get, init, set;

	/**
	 * Instantiates a new variable description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param source
	 *            the source
	 * @param iFacets
	 *            the facets
	 */
	public VariableDescription(final String keyword, final IDescription superDesc, final EObject source,
			final Facets iFacets) {
		super(keyword, superDesc, source, iFacets);
		if (iFacets != null) {
			setIf(Flag.isContextualType, computesContextualType());
			if (!hasFacet(TYPE) && !isExperimentParameter()) { iFacets.putAsLabel(TYPE, keyword); }
			setIf(Flag.isFunction, hasFacet(FUNCTION));
			setIf(Flag.IsParameter,
					isExperimentParameter() || hasFacet(PARAMETER) && !iFacets.equals(PARAMETER, FALSE));
			setIf(Flag.Global, superDesc instanceof ModelDescription);
			setIf(Unmodifiable, (iFacets.containsKey(FUNCTION) || iFacets.equals(CONST, TRUE)) && !isParameter());
			setIf(Updatable, !isSet(Unmodifiable) && (hasFacet(VALUE) || hasFacet(UPDATE)));
			if (isBuiltIn() && hasFacet("depends_on")) {
				final IExpressionDescription desc = getFacet("depends_on");
				final Collection<String> strings = desc.getStrings(this, false);
				dependencies.put(getName(), strings);
				removeFacets("depends_on");
			}
		}

	}

	/**
	 * Checks if is experiment parameter.
	 *
	 * @return true, if is experiment parameter
	 */
	@Override
	public boolean isExperimentParameter() { return PARAMETER.equals(keyword); }

	/**
	 * Sets the synthetic species container.
	 */
	public void setSyntheticSpeciesContainer() {
		set(Flag.Synthetic);
	}

	/**
	 * Checks if is synthetic species container.
	 *
	 * @return true, if is synthetic species container
	 */
	@Override
	public boolean isSyntheticSpeciesContainer() { return isSet(Flag.Synthetic); }

	/**
	 * Checks if is function.
	 *
	 * @return true, if is function
	 */
	@Override
	public boolean isFunction() { return isSet(Flag.isFunction); }

	/**
	 * Checks if is defined in experiment.
	 *
	 * @return true, if is defined in experiment
	 */
	@Override
	public boolean isDefinedInExperiment() { return getEnclosingDescription() instanceof ExperimentDescription; }

	@Override
	public void dispose() {
		if (isBuiltIn()) return;
		super.dispose();
	}

	/**
	 * Copy from.
	 *
	 * @param v2
	 *            the v 2
	 */
	@Override
	public void copyFrom(final IVariableDescription vd) {
		final VariableDescription v2 = (VariableDescription) vd;
		// Special cases for functions
		final boolean isFunction = hasFacet(FUNCTION);
		// We dont replace existing facets

		v2.visitFacets((facetName, exp) -> {
			if (isFunction && (INIT.equals(facetName) || UPDATE.equals(facetName) || VALUE.equals(facetName)))
				return true;
			if (!hasFacet(facetName)) { setFacetExprDescription(facetName, exp); }
			return true;
		});

		if (get == null) { get = v2.get; }
		if (set == null) { set = v2.set; }
		if (init == null) { init = v2.init; }
		definitionClass = v2.definitionClass;
		// if (listeners == null) { listeners = v2.listeners; }
	}

	@Override
	public IVariableDescription copy(final IDescription into) {
		final IVariableDescription vd = new VariableDescription(getKeyword(), into, element, getFacetsCopy());
		vd.addHelpers(get, init, set);
		vd.resetOriginName();
		vd.setOriginName(getOriginName());
		vd.setDefinitionClass(definitionClass);
		return vd;
	}

	@Override
	protected ISymbolSerializer createSerializer() {
		return VAR_SERIALIZER;
	}

	/**
	 * A variable is said to be contextual if its type or contents type depends on the species context. For example,
	 * 'simulation' in experiments. If so, it has to be copied in subspecies
	 *
	 * @return
	 */
	private boolean computesContextualType() {
		String type = getLitteral(TYPE);
		if (type != null && type.charAt(0) == '-') return true;
		type = getLitteral(OF);
		return type != null && type.charAt(0) == '-';
	}

	/**
	 * Returns the type denoted by this string. This is a contextual retrieval, as the string can contain the value of
	 * one of the ITypeProvider constants. Method getTypeNamed()
	 *
	 * @see gaml.compiler.gaml.descriptions.SymbolDescription#getTypeNamed(java.lang.String)
	 */
	@Override
	public IType<?> getTypeNamed(final String s) {
		final IType<?> result = super.getTypeNamed(s);
		if (result == Types.NO_TYPE) {
			final int provider = GamaIntegerType.staticCast(null, s, null, false);
			switch (provider) {
				case ITypeProvider.MACRO_TYPE:
					final IDescription species = this.getEnclosingDescription();
					final IDescription macro = species.getEnclosingDescription();
					if (macro == null) return Types.AGENT;
					return macro.getGamlType();
				case ITypeProvider.OWNER_TYPE: // This represents the type of the
												// agents of the enclosing species
					if (this.getEnclosingDescription() == null) return Types.AGENT;
					return this.getEnclosingDescription().getGamlType();
				case ITypeProvider.MODEL_TYPE: // This represents the type of the
												// model (used for simulations)
					final IModelDescription md = this.getModelDescription();
					if (md == null) return Types.get(IKeyword.MODEL);
					return md.getGamlType();
				case ITypeProvider.EXPERIMENT_TYPE:
					return Types.get(IKeyword.EXPERIMENT);
				case ITypeProvider.MIRROR_TYPE:
					if (getEnclosingDescription() == null) return null;
					final IExpression mirrors = getEnclosingDescription().getFacetExpr(MIRRORS);
					if (mirrors != null) {
						// We try to change the type of the 'target' variable if the
						// expression contains only agents from the
						// same species
						final IType<?> t = mirrors.getGamlType().getContentType();
						if (t.isAgentType() && t.id() != IType.AGENT) {
							getEnclosingDescription().info(
									"The 'target' attribute will be of type " + t.getSpeciesName(), IGamlIssue.GENERAL,
									MIRRORS);
						}
						return t;
					}
					getEnclosingDescription().info(
							"No common species detected in 'mirrors'. The 'target' variable will be of generic type 'agent'",
							IGamlIssue.WRONG_TYPE, MIRRORS);
			}
		}
		return result;
	}

	/**
	 * Gets the dependencies.
	 *
	 * @param facetsToVisit
	 *            the facets to visit
	 * @param includingThis
	 *            the including this
	 * @param includingSpecies
	 *            the including species
	 * @return the dependencies
	 */
	@Override
	public Collection<IVariableDescription> getDependencies(final Set<String> facetsToVisit,
			final boolean includingThis, final boolean includingSpecies) {

		try (final ICollector<IVarDescriptionUser> alreadyProcessed = Collector.getSet();
				final ICollector<IVariableDescription> result = Collector.getSet()) {
			final String varName = getName();
			final Collection<String> deps = dependencies.get(varName);
			if (deps != null) {
				final ISpeciesDescription speciesContext = getSpeciesContext();
				for (final String s : deps) {
					final IVariableDescription vd = speciesContext.getAttribute(s);
					if (vd != null) { result.add(vd); }
				}
			}

			final ISpeciesDescription speciesContext = getSpeciesContext();
			this.visitFacets(facetsToVisit, (fName, exp) -> {
				final IExpression expression = exp.getExpression();
				if (expression != null) { expression.collectUsedVarsOf(speciesContext, alreadyProcessed, result); }
				return true;
			});
			if (isSyntheticSpeciesContainer()) {
				final ISpeciesDescription mySpecies = (ISpeciesDescription) getEnclosingDescription();
				final ISpeciesDescription sd = mySpecies.getMicroSpecies(varName);
				sd.collectUsedVarsOf(mySpecies, alreadyProcessed, result);
			}
			if (!includingThis) { result.remove(this); }
			if (!includingSpecies) { result.removeIf(IVariableDescription::isSyntheticSpeciesContainer); }
			result.remove(null);
			return result.items();
		}
	}

	/**
	 * Checks if is updatable.
	 *
	 * @return true, if is updatable
	 */
	@Override
	public boolean isUpdatable() { return isSet(Flag.Updatable); }

	/**
	 * Checks if is not modifiable.
	 *
	 * @return true, if is not modifiable
	 */
	@Override
	public boolean isNotModifiable() { return isSet(Flag.Unmodifiable); }

	/**
	 * Checks if is parameter.
	 *
	 * @return true, if is parameter
	 */
	@Override
	public boolean isParameter() { return isSet(Flag.IsParameter); }

	/**
	 * Gets the var expr.
	 *
	 * @param asField
	 *            the as field
	 * @return the var expr
	 */
	// If asField is true, should not try to build a GlobalVarExpr
	@Override
	public IExpression getVarExpr(final boolean asField) {
		final boolean asGlobal = isGlobal() && !asField;

		return GAML.getExpressionFactory().createVar(getName(), getGamlType(), isNotModifiable(),
				asGlobal ? IVarExpression.GLOBAL : IVarExpression.AGENT, this.getEnclosingDescription());
	}

	@Override
	public String toString() {
		return getName() + " (description)";
	}

	/**
	 * Gets the parameter name.
	 *
	 * @return the parameter name
	 */
	@Override
	public String getParameterName() {
		final String pName = getLitteral(PARAMETER);
		if (pName == null || TRUE.equals(pName)) return getName();
		return pName;
	}

	@Override
	public String getTitle() {
		final String title = (isParameter() ? "Parameter " : isNotModifiable() ? "Constant " : "Attribute ") + getName()
				+ ", of type " + getGamlType().getName() + ", ";
		if (getEnclosingDescription() == null) return title;
		return title + "defined in " + this.getEnclosingDescription().getTitle() + "<br/>";
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		final String doc = getBuiltInDoc();
		if (isBuiltIn()) return new GamlConstantDocumentation(doc == null ? "Not yet documented" : doc);
		StringBuilder s = new StringBuilder();
		if (doc != null) { s.append(doc).append("<br/>"); }
		IGamlDocumentation result = new GamlRegularDocumentation(s).append(getArtefact().getDocumentation().toString());
		result.append("<hr/>").append("<b><p>").append(getGamlType().getTitle()).append("</p></b>").append("<br/>")
				.append(getGamlType().getDocumentation().toString());
		return result;
	}

	/**
	 * Gets the short documentation.
	 *
	 * @return the short documentation
	 */
	@Override
	public IGamlDocumentation getShortDocumentation() {
		IGamlDocumentation result = new GamlRegularDocumentation(
				isParameter() ? "parameter " : isNotModifiable() ? "constant " : "attribute ").append("of type ")
						.append(getGamlType().getName());
		final String doc = getBuiltInDoc();
		if (doc != null) { result.append(". ").append(doc).append("<br/>"); }
		return result;
	}

	/**
	 * Gets the built in doc.
	 *
	 * @return the built in doc
	 */
	@Override
	public String getBuiltInDoc() {
		// if (builtInDoc != null) return builtInDoc;

		final IVariableDescription builtIn = getBuiltInAncestor();
		if (builtIn == null) return null;
		String builtInDoc = "";
		if (definitionClass == null) return PREF_DEFINITIONS.get(getName());
		final vars vars = definitionClass.getAnnotationsByType(vars.class)[0];
		for (final gama.annotations.variable v : vars.value()) {
			if (v.name().equals(name)) {
				final doc[] docs = v.doc();
				if (docs.length > 0) { // documentation of fields is not used
					builtInDoc = docs[0].value();
				}
				break;
			}
		}
		return builtInDoc;

	}

	/**
	 * Gets the built in ancestor.
	 *
	 * @return the built in ancestor
	 */
	@Override
	public IVariableDescription getBuiltInAncestor() {
		if (getEnclosingDescription() instanceof TypeDescription) {
			final TypeDescription td = (TypeDescription) getEnclosingDescription();
			if (td.isBuiltIn()) return this;
			if (td.getParent() != null && td.getParent().hasAttribute(name))
				return td.getParent().getAttribute(name).getBuiltInAncestor();
		}
		return null;
	}

	/**
	 * Adds the helpers.
	 *
	 * @param get1
	 *            the get
	 * @param init1
	 *            the init
	 * @param set1
	 *            the set
	 */
	@Override
	public void addHelpers(final IGamaHelper<?> get1, final IGamaHelper<?> init1, final IGamaHelper<?> set1) {
		this.get = get1;
		this.set = set1;
		this.init = init1;
	}

	/**
	 * Adds the helpers.
	 *
	 * @param skill
	 *            the skill
	 * @param get1
	 *            the get
	 * @param init1
	 *            the init
	 * @param set1
	 *            the set
	 */
	@Override
	public void addHelpers(final Class<?> skill, final IGamaHelper<?> get1, final IGamaHelper<?> init1,
			final IGamaHelper<?> set1) {
		addHelpers(get1 != null ? new GamaHelper<>(name, skill, get1) : null,
				init1 != null ? new GamaHelper<>(name, skill, init1) : null,
				set1 != null ? new GamaHelper<>(name, skill, set1) : null);
	}

	// public void addListeners(final List<GamaHelper> listeners) {
	// this.listeners = listeners.toArray(new GamaHelper[listeners.size()]);
	// }
	//
	// public GamaHelper[] getListeners() {
	// return listeners;
	// }

	/**
	 * Gets the getter.
	 *
	 * @return the getter
	 */
	@Override
	public IGamaHelper<?> getGetter() { return get; }

	/**
	 * Gets the initer.
	 *
	 * @return the initer
	 */
	@Override
	public IGamaHelper<?> getIniter() { return init; }

	/**
	 * Gets the setter.
	 *
	 * @return the setter
	 */
	@Override
	public IGamaHelper<?> getSetter() { return set; }

	/**
	 * Checks if is global.
	 *
	 * @return true, if is global
	 */
	public boolean isGlobal() { return isSet(Flag.Global); }

	@Override
	public String getDefiningPlugin() { return plugin; }

	/**
	 * @param plugin
	 *            name
	 */
	@Override
	public void setDefiningPlugin(final String plugin) { this.plugin = plugin; }

	@Override
	public boolean visitChildren(final DescriptionVisitor visitor) {
		return true;
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor visitor) {
		return true;
	}

	@Override
	public Iterable<IDescription> getOwnChildren() { return Collections.EMPTY_LIST; }

	/**
	 * Checks if is contextual type.
	 *
	 * @return true, if is contextual type
	 */
	public boolean isContextualType() { return isSet(Flag.isContextualType); }

	/**
	 * Sets the built in doc.
	 *
	 * @param definitionClass
	 *            the new built in doc
	 */
	@Override
	public void setDefinitionClass(final Class definitionClass) { this.definitionClass = definitionClass; }

	@Override
	protected void flagError(final String s, final String code, final GamlCompilationError.Type type,
			final EObject source, final String... data) throws GamaRuntimeException {
		if (isExperimentParameter()) {
			EObject param = getUnderlyingElement();
			if (EcoreUtil.isAncestor(param, source)) {
				super.flagError(s, code, type, source, data);
			} else {
				super.flagError(s, code, type, param, data);
			}
		} else {
			super.flagError(s, code, type, source, data);
		}
	}

	/**
	 * Compute type. By default, variable declarations do not try to infer their type from the value facets. See #385
	 *
	 * @return the i type
	 */
	@Override
	protected IType<?> computeType() {
		return computeType(false);
	}

}
