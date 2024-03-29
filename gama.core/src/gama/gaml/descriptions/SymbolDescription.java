/*******************************************************************************************************
 *
 * SymbolDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import gama.annotations.precompiler.GamlProperties;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.compilation.ISymbol;
import gama.gaml.expressions.IExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.interfaces.IGamlDescription;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.Facets;
import gama.gaml.statements.IStatement;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 16 mars 2010
 *
 * @todo Description
 *
 */
public abstract class SymbolDescription implements IDescription {

	static {
		DEBUG.OFF();
	}

	/** The type provider facets. */
	protected static Set<String> typeProviderFacets = ImmutableSet
			.copyOf(Arrays.asList(VALUE, TYPE, AS, SPECIES, OF, OVER, FROM, INDEX, FUNCTION, UPDATE, INIT, DEFAULT));

	/** The state. */
	private final EnumSet<Flag> state = EnumSet.noneOf(Flag.class);

	/** The order. */
	// private final int order = COUNTER.GET_UNIQUE();

	/** The facets. */
	private Facets facets;

	/** The element. */
	protected final EObject element;

	/** The enclosing. */
	private IDescription enclosingDescription;

	/** The model description. */
	private ModelDescription modelDescription;

	/** The origin name. */
	protected String originName;

	/** The name. */
	protected String name;

	/** The keyword. */
	protected final String keyword;

	/** The type. */
	private IType<?> type;

	/** The proto. */
	final SymbolProto proto;

	/**
	 * Instantiates a new symbol description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 */
	public SymbolDescription(final String keyword, final IDescription superDesc, final EObject source,
			final Facets facets) {
		this.keyword = keyword;
		this.facets = facets;
		element = source;
		setIf(Flag.BuiltIn, element == null);
		if (facets != null && facets.containsKey(ORIGIN)) {
			originName = facets.getLabel(ORIGIN);
			facets.remove(ORIGIN);
		} else if (superDesc != null) { originName = superDesc.getName(); }
		setEnclosingDescription(superDesc);
		proto = DescriptionFactory.getProto(getKeyword(), getSpeciesContext());

	}

	// ---- State management

	/**
	 * Sets the.
	 *
	 * @param flag
	 *            the flag
	 */
	protected void set(final Flag flag) {
		state.add(flag);
	}

	/**
	 * Sets the if.
	 *
	 * @param flag
	 *            the flag
	 * @param condition
	 *            the condition
	 */
	protected void setIf(final Flag flag, final boolean condition) {
		if (condition) {
			set(flag);
		} else {
			unSet(flag);
		}
	}

	/**
	 * Un set.
	 *
	 * @param flag
	 *            the flag
	 */
	protected void unSet(final Flag flag) {
		state.remove(flag);
	}

	/**
	 * Checks if is sets the.
	 *
	 * @param flag
	 *            the flag
	 * @return true, if is sets the
	 */
	protected boolean isSet(final Flag flag) {
		return state.contains(flag);
	}

	/**
	 * Gets the order.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the order
	 * @date 28 déc. 2023
	 */
	// @Override
	// public int getOrder() { return order; }

	/**
	 * Checks for facets.
	 *
	 * @return true, if successful
	 */
	protected boolean hasFacets() {
		return facets != null;
	}

	/**
	 * Checks for facets not in.
	 *
	 * @param others
	 *            the others
	 * @return true, if successful
	 */
	protected boolean hasFacetsNotIn(final Set<String> others) {
		if (facets == null) return false;
		return !visitFacets((facetName, exp) -> others.contains(facetName));
	}

	@Override
	public final SymbolSerializer<? extends SymbolDescription> getSerializer() {
		final SymbolProto p = getMeta();
		SymbolSerializer<? extends SymbolDescription> d = p.getSerializer();
		if (d == null) {
			d = createSerializer();
			p.setSerializer(d);
		}
		return d;
	}

	@Override
	public IExpressionDescription getFacet(final String string) {
		return !hasFacets() ? null : facets.get(string);
	}

	@Override
	public IExpression getFacetExpr(final String... strings) {
		return !hasFacets() ? null : facets.getExpr(strings);
	}

	@Override
	public IExpressionDescription getFacet(final String... strings) {
		return !hasFacets() ? null : facets.getDescr(strings);
	}

	@Override
	public boolean hasFacet(final String string) {
		return hasFacets() && facets.containsKey(string);
	}

	@Override
	public String getLitteral(final String string) {
		return !hasFacets() ? null : facets.getLabel(string);
	}

	@Override
	public void setFacetExprDescription(final String name, final IExpressionDescription desc) {
		if (!hasFacets()) { facets = new Facets(); }
		facets.put(name, desc);
	}

	@Override
	public void setFacet(final String string, final IExpression exp) {
		if (!hasFacets()) { facets = new Facets(); }
		facets.putExpression(string, exp);
	}

	@Override
	public void removeFacets(final String... strings) {
		if (!hasFacets()) return;
		for (final String s : strings) { facets.remove(s); }
		if (facets.isEmpty()) { facets = null; }
	}

	@Override
	public final boolean visitFacets(final Set<String> names, final IFacetVisitor visitor) {
		if (!hasFacets()) return true;
		return facets.forEachFacetIn(names, visitor);
	}

	@Override
	public final boolean visitFacets(final IFacetVisitor visitor) {
		if (!hasFacets()) return true;
		return facets.forEachFacet(visitor);
	}

	/**
	 * Gets the type denoted by facet.
	 *
	 * @param s
	 *            the s
	 * @return the type denoted by facet
	 */
	public IType<?> getTypeDenotedByFacet(final String... s) {
		if (!hasFacets()) return Types.NO_TYPE;
		return getTypeDenotedByFacet(facets.getFirstExistingAmong(s), Types.NO_TYPE);
	}

	@Override
	public String firstFacetFoundAmong(final String... strings) {
		if (!hasFacets()) return null;
		return facets.getFirstExistingAmong(strings);
	}

	/**
	 * Gets the type denoted by facet.
	 *
	 * @param s
	 *            the s
	 * @param defaultType
	 *            the default type
	 * @return the type denoted by facet
	 */
	public IType<?> getTypeDenotedByFacet(final String s, final IType<?> defaultType) {
		if (!hasFacets()) return defaultType;
		return facets.getTypeDenotedBy(s, this, defaultType);
	}

	/**
	 * Gets the facets copy.
	 *
	 * @return the facets copy
	 */
	public Facets getFacetsCopy() { return !hasFacets() ? null : facets.cleanCopy(); }

	/**
	 * @return
	 */
	protected SymbolSerializer<? extends SymbolDescription> createSerializer() {
		return SYMBOL_SERIALIZER;
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getSerializer().serialize(this, includingBuiltIn);
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		getSerializer().collectMetaInformation(this, meta);
	}

	@Override
	public int getKind() { return getMeta().getKind(); }

	/**
	 * Compile type provider facets.
	 */
	protected void compileTypeProviderFacets() {
		visitFacets((facetName, exp) -> {
			if (typeProviderFacets.contains(facetName)) { exp.compile(SymbolDescription.this); }
			return true;
		});

	}

	/**
	 * Compile type provider facets.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param names
	 *            the names
	 * @date 3 oct. 2023
	 */
	protected void compileTypeProviderFacets(final String... names) {
		for (String s : names) {
			IExpressionDescription exp = getFacet(s);
			if (exp != null) { exp.compile(this); }
		}
	}

	@Override
	public final SymbolProto getMeta() { return proto; }

	/**
	 * Flag error.
	 *
	 * @param s
	 *            the s
	 * @param code
	 *            the code
	 * @param warning
	 *            the warning
	 * @param info
	 *            the info
	 * @param source
	 *            the source
	 * @param data
	 *            the data
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected void flagError(final String s, final String code, final boolean warning, final boolean info,
			final EObject source, final String... data) throws GamaRuntimeException {

		if (warning && !info && !GamaPreferences.Modeling.WARNINGS_ENABLED.getValue()) return;
		if (info && !GamaPreferences.Modeling.INFO_ENABLED.getValue()) return;

		IDescription desc = this;
		EObject e = source;
		if (e == null) { e = getUnderlyingElement(); }
		while (e == null && desc != null) {
			desc = desc.getEnclosingDescription();
			if (desc != null) { e = desc.getUnderlyingElement(); }
		}
		// throws a runtime exception if there is no way to signal the error in
		// the source
		// (i.e. we are probably in a runtime scenario)
		if (e == null || e.eResource() == null || e.eResource().getURI().path().contains(SYNTHETIC_RESOURCES_PREFIX)) {
			if (!warning && !info) throw GamaRuntimeException.error(s, gama.core.runtime.GAMA.getRuntimeScope());
			return;

		}
		final ValidationContext c = getValidationContext();
		if (c == null) {
			DEBUG.ERR((warning ? "Warning" : "Error") + ": " + s);
			return;
		}
		c.add(new GamlCompilationError(s, code, e, warning, info, data));
	}

	/**
	 * Documents an expression within this context
	 */
	@Override
	public void document(final EObject e, final IGamlDescription desc) {
		final ValidationContext c = getValidationContext();
		if (c != null) { c.setGamlDocumentation(e, desc); }
	}

	@Override
	public void error(final String message) {
		error(message, IGamlIssue.GENERAL);
	}

	/** The Constant EMPTY_DATA. */
	final static String[] EMPTY_DATA = {};

	@Override
	public void error(final String message, final String code) {
		flagError(message, code, false, false, getUnderlyingElement(), EMPTY_DATA);
	}

	@Override
	public void error(final String s, final String code, final EObject facet, final String... data) {
		flagError(s, code, false, false, facet, data);
	}

	@Override
	public void error(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, false, false, this.getUnderlyingElement(facet, IGamlIssue.UNKNOWN_FACET.equals(code)),
				data == null || data.length == 0 ? new String[] { facet } : data);
	}

	@Override
	public void info(final String message, final String code) {
		flagError(message, code, false, true, getUnderlyingElement(), EMPTY_DATA);
	}

	@Override
	public void info(final String s, final String code, final EObject facet, final String... data) {
		flagError(s, code, false, true, facet, data);
	}

	@Override
	public void info(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, false, true, this.getUnderlyingElement(facet, false),
				data == null || data.length == 0 ? new String[] { facet } : data);
	}

	@Override
	public void warning(final String message, final String code) {
		flagError(message, code, true, false, null, EMPTY_DATA);
	}

	@Override
	public void warning(final String s, final String code, final EObject object, final String... data) {
		flagError(s, code, true, false, object, data);
	}

	@Override
	public void warning(final String s, final String code, final String facet, final String... data) {
		flagError(s, code, true, false, this.getUnderlyingElement(facet, IGamlIssue.UNKNOWN_FACET.equals(code)), data);
	}

	@Override
	public String getKeyword() { return keyword; }

	@Override
	public String getName() {
		if (name == null) { name = getLitteral(NAME); }
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
		if (getMeta().getPossibleFacets().containsKey(NAME)) {
			setFacetExprDescription(NAME, LabelExpressionDescription.create(name));
		}
	}

	@Override
	public void dispose() {
		// DEBUG.LOG("Disposing " + getKeyword() + " " + getName());
		if (isBuiltIn()) return;
		visitOwnChildren(DISPOSING_VISITOR);
		if (hasFacets()) { facets.dispose(); }
		facets = null;
		enclosingDescription = null;
		modelDescription = null;
		setType(null);
	}

	@Override
	public ModelDescription getModelDescription() { return modelDescription; }

	// To add children from outside
	/**
	 * Adds the children.
	 *
	 * @param originalChildren
	 *            the original children
	 */
	// @Override
	public final void addChildren(final Iterable<? extends IDescription> originalChildren) {
		if (originalChildren == null) return;
		for (final IDescription c : originalChildren) { addChild(c); }
	}

	/**
	 * Adds the child.
	 *
	 * @param child
	 *            the child
	 * @return the i description
	 */
	// @Override
	public IDescription addChild(final IDescription child) {
		if (child == null) return null;
		child.setEnclosingDescription(this);
		return child;
	}

	@Override
	public void setEnclosingDescription(final IDescription desc) {
		enclosingDescription = desc;
		if (enclosingDescription == null) return;
		modelDescription = enclosingDescription.getModelDescription();
		if (modelDescription != null && modelDescription.isBuiltIn() && !this.isBuiltIn()) { modelDescription = null; }
	}

	@Override
	public EObject getUnderlyingElement(final Object facet, final boolean returnFacet) {
		if (facet == null) return element;
		if (facet instanceof EObject e) return e;
		if (facet instanceof IExpressionDescription f) {
			final EObject result = f.getTarget();
			if (result != null) return result;
		}
		if (facet instanceof String) {
			if (getMeta() != null && !returnFacet && facet.equals(getMeta().getOmissible())) {
				final EObject o = GAML.getEcoreUtils().getExprOf(element);
				if (o != null) return o;
			}
			if (returnFacet) {
				final EObject facetObject = GAML.getEcoreUtils().getFacetsMapOf(element).get(facet);
				if (facetObject != null) return facetObject;
			}
			final IExpressionDescription f = getFacet((String) facet);
			if (f != null) {
				final EObject result = f.getTarget();
				if (result != null) return result;
				final EObject facetObject = GAML.getEcoreUtils().getFacetsMapOf(element).get(facet);
				if (facetObject != null) return facetObject;
			}
			// Last chance if the expression is a constant (no information on EObjects), see Issue #2760)
			final EObject facetExpr = GAML.getEcoreUtils().getExpressionAtKey(element, (String) facet);
			if (facetExpr != null) return facetExpr;
		}
		return null;
	}

	@Override
	public IDescription copy(final IDescription into) {
		return this;
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	@Override
	public IDescription getEnclosingDescription() { return enclosingDescription; }

	@Override
	public boolean hasAttribute(final String aName) {
		return false;
	}

	@Override
	public boolean manipulatesVar(final String aName) {
		return false;
	}

	/**
	 * Checks for action.
	 *
	 * @param aName
	 *            the a name
	 * @param superInvocation
	 *            the super invocation
	 * @return true, if successful
	 */
	protected boolean hasAction(final String aName, final boolean superInvocation) {
		return false;
	}

	@Override
	public IVarDescriptionProvider getDescriptionDeclaringVar(final String aName) {
		IDescription enc = getEnclosingDescription();
		return enc == null ? null : enc.getDescriptionDeclaringVar(aName);
	}

	@Override
	public IDescription getDescriptionDeclaringAction(final String aName, final boolean superInvocation) {
		IDescription enc = getEnclosingDescription();
		return enc == null ? null : enc.getDescriptionDeclaringAction(aName, superInvocation);
	}

	@Override
	public IExpression getVarExpr(final String aName, final boolean asField) {
		return null;
	}

	@Override
	public IType<?> getTypeNamed(final String s) {
		final ModelDescription m = getModelDescription();
		if (m == null) return Types.get(s);
		return m.getTypeNamed(s);
	}

	@Override
	public IType<?> getGamlType() {
		if (type == null) { setType(computeType()); }
		return type;
	}

	/** The Constant staticTypeProviders. */
	// 13/02/20: Addition of VALUE (see #2932)
	static final String[] staticTypeProviders = { DATA, TYPE, SPECIES, AS, TARGET /* , ON */ };

	/** The Constant dynamicTypeProviders. */
	static final String[] dynamicTypeProviders = { INIT, VALUE, UPDATE, FUNCTION, DEFAULT };

	/**
	 * Compute type.
	 *
	 * @return the i type
	 */
	protected IType<?> computeType() {
		// Adapter ca pour prendre en compte les ITypeProvider
		IType<?> tt = getTypeDenotedByFacet(staticTypeProviders);
		if (tt == Types.NO_TYPE) {
			IExpressionDescription ed = getFacet(dynamicTypeProviders);
			if (ed != null) {
				IExpression expr = ed.compile(this);
				if (expr != null) { tt = expr.getGamlType(); }
			}
		}
		IType<?> kt = getTypeDenotedByFacet(INDEX, tt.getKeyType());
		IType<?> ct = getTypeDenotedByFacet(OF, tt.getContentType());
		final boolean isContainerWithNoContentsType = tt.isContainer() && ct == Types.NO_TYPE;
		final boolean isContainerWithNoKeyType = tt.isContainer() && kt == Types.NO_TYPE;
		if (isContainerWithNoContentsType || isContainerWithNoKeyType) {
			IExpressionDescription ed = getFacet(dynamicTypeProviders);
			if (ed != null) {
				IExpression expr = ed.compile(this);
				final IType<?> exprType = expr == null ? Types.NO_TYPE : expr.getGamlType();
				if (tt.isAssignableFrom(exprType)) {
					tt = exprType;
				} else {
					if (isContainerWithNoKeyType) { kt = exprType.getKeyType(); }
					if (isContainerWithNoContentsType /* || isSpeciesWithAgentType */) {
						ct = exprType.getContentType();
					}
				}
			}
		}

		return GamaType.from(tt, kt, ct);
	}

	@Override
	public SpeciesDescription getSpeciesContext() {
		IDescription desc = getEnclosingDescription();
		if (desc == null) return null;
		return desc.getSpeciesContext();
	}

	/**
	 * @see gama.core.common.interfaces.IDescription#getSpeciesDescription(java.lang.String)
	 */
	@Override
	public SpeciesDescription getSpeciesDescription(final String actualSpecies) {
		final ModelDescription model = getModelDescription();
		if (model == null) return null;
		return model.getSpeciesDescription(actualSpecies);
	}

	/**
	 * @see gama.core.common.interfaces.IDescription#getAction(java.lang.String)
	 */
	@Override
	public ActionDescription getAction(final String aName) {
		return null;
	}

	@Override
	public String getTitle() { return "Statement " + getKeyword(); }

	@Override
	public Doc getDocumentation() { return getMeta().getDocumentation(); }

	@Override
	public String getDefiningPlugin() { return getMeta().getDefiningPlugin(); }

	@Override
	public void setDefiningPlugin(final String plugin) {
		// Nothing to do here
	}

	@Override
	public ValidationContext getValidationContext() {
		final ModelDescription model = getModelDescription();
		if (model == null) return null;
		return model.getValidationContext();
	}

	@Override
	public boolean isBuiltIn() { return state.contains(Flag.BuiltIn); }

	/**
	 * Checks if is synthetic.
	 *
	 * @return true, if is synthetic
	 */
	protected boolean isSynthetic() { return state.contains(Flag.Synthetic); }

	@Override
	public String getOriginName() { return originName; }

	@Override
	public void setOriginName(final String name) {
		if (originName == null) { originName = name; }
	}

	@Override
	public void resetOriginName() {
		originName = null;
	}

	@Override
	public IDescription validate() {
		if (isSet(Flag.Validated)) return this;
		set(Flag.Validated);

		if (isBuiltIn()) {
			// We simply make sure that the facets are correctly compiled
			validateFacets();
			return this;
		}
		final IDescription enclosing = getEnclosingDescription();
		if (enclosing != null) {
			String kw = getKeyword();
			String ekw = enclosing.getKeyword();
			// We first verify that the description is at the right place
			if (!proto.canBeDefinedIn(enclosing)) {
				error(kw + " cannot be defined in " + ekw, IGamlIssue.WRONG_CONTEXT);
				return null;
			}
			// If it is supposed to be unique, we verify this
			if (proto.isUniqueInContext()) {
				final boolean hasError = !enclosing.visitOwnChildren(child -> {
					if (child != SymbolDescription.this && child.getKeyword().equals(kw)) {
						final String error = kw + " is defined twice. Only one definition is allowed in " + ekw;
						child.error(error, IGamlIssue.DUPLICATE_KEYWORD, child.getUnderlyingElement(), kw);
						error(error, IGamlIssue.DUPLICATE_KEYWORD, getUnderlyingElement(), kw);
						return false;
					}
					return true;

				});
				if (hasError) return null;
			}
		}

		// We then validate its facets and children
		if (!validateFacets() || !validateChildren()) return null;
		if (proto.getDeprecated() != null) {
			warning("'" + getKeyword() + "' is deprecated. " + proto.getDeprecated(), IGamlIssue.DEPRECATED);
		}

		// If a custom validator has been defined, run it
		if (!proto.getValidator().validate(this, element)) return null;
		return this;
	}

	/**
	 * Validate facets.
	 *
	 * @return true, if successful
	 */
	private final boolean validateFacets() {
		// Special case for "do", which can accept (at parsing time) any facet
		final boolean isDo = isSet(Flag.IsInvocation);
		final boolean isBuiltIn = isBuiltIn();
		List<String> mandatory = proto.getMandatoryFacets();
		if (mandatory != null) {
			for (String facet : mandatory) {
				if (!facets.containsKey(facet)) {
					error("Missing facet " + facet, IGamlIssue.MISSING_FACET, getUnderlyingElement(), facet, "nil");
					return false;
				}
			}
		}

		return visitFacets((facet, expr) -> {
			final FacetProto fp = proto.getFacet(facet);
			if (fp == null) return processUnknowFacet(isDo, facet);
			if (fp.getDeprecated() != null) {
				warning("Facet '" + facet + "' is deprecated: " + fp.getDeprecated(), IGamlIssue.DEPRECATED, facet);
			}
			if (fp.values != null) {
				if (!processMultiValuedFacet(facet, expr, fp)) return false;
			} else {
				// Some expresssions might not be compiled
				IExpression exp = compileExpression(facet, expr, fp);
				if (exp != null && !isBuiltIn) {
					final IType<?> actualType = exp.getGamlType();
					// Special case for init. Temporary solution before we can pass ITypeProvider.OWNER_TYPE to the init
					// facet. Concerned types are point and date, which belong to "NumberVariable" and can accept nil,
					// while int and float cannot
					if (INIT.equals(fp.name)) {
						IType<?> requestedType = SymbolDescription.this.getGamlType();
						if ((Types.POINT == requestedType || Types.DATE == requestedType)
								&& actualType == Types.NO_TYPE)
							return true;
					}
					final IType<?> contentType = fp.contentType;
					final IType<?> keyType = fp.keyType;
					boolean compatible = verifyFacetTypesCompatibility(fp, exp, actualType, contentType, keyType);
					if (!compatible) {
						emitFacetTypesIncompatibilityWarning(facet, fp, actualType, contentType, keyType);
					}
				}
			}
			return true;
		});

	}

	/**
	 * Compile expression.
	 *
	 * @param facet
	 *            the facet
	 * @param expr
	 *            the expr
	 * @param fp
	 *            the fp
	 * @return the i expression
	 */
	private IExpression compileExpression(final String facet, final IExpressionDescription expr, final FacetProto fp) {
		IExpression exp;
		if (fp.isNewTemp) {
			exp = createVarWithTypes(facet);
			// DEBUG.OUT("Type of IExpressionDescription is " + expr.getClass().getSimpleName());
			expr.setExpression(exp);
		} else if (!fp.isLabel()) {
			if (fp.isRemote && this instanceof StatementRemoteWithChildrenDescription srwc) {
				IDescription previousEnclosingDescription = srwc.pushRemoteContext();
				exp = expr.compile(SymbolDescription.this);
				srwc.popRemoteContext(previousEnclosingDescription);
			} else {
				exp = expr.compile(SymbolDescription.this);
			}
		} else {
			exp = expr.getExpression();
		}
		return exp;
	}

	/**
	 * Emit facet types incompatibility warning.
	 *
	 * @param facet
	 *            the facet
	 * @param fp
	 *            the fp
	 * @param actualType
	 *            the actual type
	 * @param contentType
	 *            the content type
	 * @param keyType
	 *            the key type
	 */
	private void emitFacetTypesIncompatibilityWarning(final String facet, final FacetProto fp,
			final IType<?> actualType, final IType<?> contentType, final IType<?> keyType) {
		final String[] strings = new String[fp.types.length];
		for (int i = 0; i < fp.types.length; i++) {
			IType<?> requestedType2 = fp.types[i];
			if (requestedType2.isContainer()) { requestedType2 = GamaType.from(requestedType2, keyType, contentType); }
			strings[i] = requestedType2.toString();
		}

		warning("Facet '" + facet + "' is expecting " + Arrays.toString(strings) + " instead of " + actualType,
				IGamlIssue.SHOULD_CAST, facet, fp.types[0].toString());
	}

	/**
	 * Verify facet types compatibility.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param fp
	 *            the fp
	 * @param exp
	 *            the exp
	 * @param actualType
	 *            the actual type
	 * @param contentType
	 *            the content type
	 * @param keyType
	 *            the key type
	 * @return true, if successful
	 * @date 10 janv. 2024
	 */
	private boolean verifyFacetTypesCompatibility(final FacetProto fp, final IExpression exp, final IType<?> actualType,
			final IType<?> contentType, final IType<?> keyType) {
		boolean compatible = false;
		for (final IType<?> definedType : fp.types) {
			if (definedType == Types.NO_TYPE) return true;
			boolean isNone = actualType == Types.NO_TYPE;

			if (definedType.isContainer()) {
				compatible = actualType.equals(definedType) && actualType.getKeyType().equals(keyType)
						&& actualType.getContentType().equals(contentType)
						|| !isNone && actualType.isTranslatableInto(definedType)
								&& actualType.getKeyType().isTranslatableInto(keyType)
								&& actualType.getContentType().isTranslatableInto(contentType);
			} else {
				compatible = actualType.equals(definedType) || !isNone && actualType.isTranslatableInto(definedType);

			}
			compatible |= Types.isEmptyContainerCase(definedType, exp);
			if (compatible) { break; }
		}

		return compatible;
	}

	/**
	 * Process multi valued facet.
	 *
	 * @param facet
	 *            the facet
	 * @param expr
	 *            the expr
	 * @param fp
	 *            the fp
	 * @return true, if successful
	 */
	private boolean processMultiValuedFacet(final String facet, final IExpressionDescription expr,
			final FacetProto fp) {
		final String val = expr.getExpression().literalValue();
		// We have a multi-valued facet
		if (!fp.values.contains(val)) {
			error("Facet '" + facet + "' is expecting a value among " + fp.values + " instead of " + val, facet);
			return false;
		}
		return true;
	}

	/**
	 * Process unknow facet.
	 *
	 * @param isDo
	 *            the is do
	 * @param facet
	 *            the facet
	 * @return true, if successful
	 */
	private boolean processUnknowFacet(final boolean isDo, final String facet) {
		if (facet.contains(IGamlIssue.DOUBLED_CODE)) {
			final String correct = facet.replace(IGamlIssue.DOUBLED_CODE, "");
			final String error = "Facet " + correct + " is declared twice. Please correct.";
			error(error, IGamlIssue.DUPLICATE_DEFINITION, facet, "1");
			error(error, IGamlIssue.DUPLICATE_DEFINITION, correct, "2");
			return false;
		}
		if (!isDo) {
			error("Unknown facet " + facet, IGamlIssue.UNKNOWN_FACET, facet);
			return false;
		}
		return true;
	}

	/**
	 * Creates the var with types.
	 *
	 * @param tag
	 *            the tag
	 * @return the i expression
	 */
	// Nothing to do here
	protected IExpression createVarWithTypes(final String tag) {
		return null;
	}

	/**
	 * Validate children.
	 *
	 * @return true, if successful
	 */
	protected boolean validateChildren() {
		return visitOwnChildren(VALIDATING_VISITOR);
	}

	@Override
	public final ISymbol compile() {
		validate();
		final ISymbol cs = proto.create(this);
		if (cs == null) return null;
		if (proto.hasArgs()) {
			((IStatement.WithArgs) cs).setFormalArgs(((StatementDescription) this).createCompiledArgs());
		}
		if (proto.hasSequence() && !proto.isPrimitive()) { cs.setChildren(compileChildren()); }
		return cs;

	}

	/**
	 * Method compileChildren()
	 *
	 * @see gama.gaml.descriptions.IDescription#compileChildren()
	 */
	protected Iterable<? extends ISymbol> compileChildren() {

		final List<ISymbol> lce = new ArrayList<>();
		visitChildren(desc -> {
			final ISymbol s = desc.compile();
			if (s != null) { lce.add(s); }
			return true;
		});
		return lce;
	}

	@Override
	public Iterable<IDescription> getChildrenWithKeyword(final String aKeyword) {
		return Iterables.filter(getOwnChildren(), each -> each.getKeyword().equals(aKeyword));
	}

	@Override
	public IDescription getChildWithKeyword(final String aKeyword) {
		return Iterables.find(getOwnChildren(), each -> each.getKeyword().equals(aKeyword), null);
	}
	//
	// @Override
	// public void computeStats(final FacetVisitor proc, final int[] facetNumber, final int[] descWithNoFacets,
	// final int[] descNumber) {
	// visitFacets(proc);
	// final int facetSize = facets == null ? 0 : facets.size();
	// facetNumber[0] += facetSize;
	// descNumber[0]++;
	// if (facetSize == 1)
	// descWithNoFacets[0]++;
	//
	// visitChildren(new DescriptionVisitor<IDescription>() {
	//
	// @Override
	// public boolean visit(final IDescription desc) {
	// desc.computeStats(proc, facetNumber, descWithNoFacets, descNumber);
	// return true;
	// }
	// });
	//
	// }

	/**
	 * Convenience method to access facets from other structures. However, this method should be (when possible)
	 * replaced by the usage of the visitor pattern through visitFacets()
	 */
	@Override
	public Facets getFacets() { return facets == null ? Facets.NULL : facets; }

	@Override
	public void attachAlternateVarDescriptionProvider(final IVarDescriptionProvider vp) {}

	/**
	 * Gets the similar child.
	 *
	 * @param container
	 *            the container
	 * @param desc
	 *            the desc
	 * @return the similar child
	 */
	public static IDescription getSimilarChild(final IDescription container, final IDescription desc) {
		final IDescription[] found = new IDescription[1];
		container.visitChildren(d -> {
			if (d != null && d.getKeyword().equals(desc.getKeyword()) && d.getName().equals(desc.getName())) {
				found[0] = d;
				return false;
			}
			return true;
		});
		return found[0];
	}

	@Override
	public void replaceChildrenWith(final Iterable<IDescription> array) {}

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	private void setType(final IType<?> type) { this.type = type; }

	@Override
	public boolean isInvocation() { return isSet(Flag.IsInvocation); }

}
