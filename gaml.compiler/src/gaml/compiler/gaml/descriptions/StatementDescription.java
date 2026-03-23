/*******************************************************************************************************
 *
 * StatementDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import static gama.annotations.constants.IKeyword.ARG;
import static gama.annotations.constants.IKeyword.AS;
import static gama.annotations.constants.IKeyword.FROM;
import static gama.annotations.constants.IKeyword.OVER;
import static gama.annotations.constants.IKeyword.SPECIES;
import static gama.annotations.constants.IKeyword.VALUE;
import static gama.annotations.constants.IKeyword.WITH;
import static gama.api.constants.IGamlIssue.GENERAL;
import static gama.api.constants.IGamlIssue.MISSING_NAME;
import static gama.api.constants.IGamlIssue.SHOULD_CAST;
import static gama.api.constants.IGamlIssue.UNKNOWN_ARGUMENT;
import static gama.api.gaml.types.Types.NO_TYPE;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.dev.COUNTER;
import gama.dev.DEBUG;

/**
 * Written by drogoul Modified on 10 févr. 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ({ "rawtypes" })
public class StatementDescription extends SymbolDescription implements IStatementDescription {

	static {
		DEBUG.OFF();
	}

	/** The passed args. */
	// Corresponds to the "with" facet
	protected final Arguments passedArgs;

	/**
	 * Instantiates a new statement description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param hasArgs
	 *            the has args
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 * @param alreadyComputedArgs
	 *            the already computed args
	 */
	public StatementDescription(final String keyword, final IDescription superDesc, final boolean hasArgs,
			final EObject source, final Facets facets, final Arguments alreadyComputedArgs) {
		super(keyword, superDesc, source, /* children, */ facets);
		setIf(Flag.IsCreate, IKeyword.CREATE.equals(keyword) || IKeyword.RESTORE.equals(keyword));
		passedArgs = alreadyComputedArgs != null ? alreadyComputedArgs : hasArgs ? createArgs() : null;
	}

	@Override
	protected ISymbolSerializer createSerializer() {
		return STATEMENT_SERIALIZER;
	}

	@Override
	public void dispose() {
		if (isBuiltIn()) return;
		super.dispose();
		if (passedArgs != null) { passedArgs.dispose(); }
	}

	/**
	 * Creates the args.
	 *
	 * @return the arguments
	 */
	protected Arguments createArgs() {
		if (hasFacet(WITH)) {
			try {
				return GAML.getExpressionFactory().createArgumentMap(null, getFacet(WITH), this);
			} finally {
				removeFacets(WITH);
			}
		}
		return null;
	}

	@Override
	public StatementDescription copy(final IDescription into) {
		final StatementDescription desc = new StatementDescription(getKeyword(), into, false, /* null, */ element,
				getFacetsCopy(), passedArgs == null ? null : passedArgs.cleanCopy());
		desc.originName = getOriginName();
		return desc;
	}

	@Override
	public boolean manipulatesVar(final String nm) {
		if (IKeyword.EQUATION.equals(getKeyword())) {
			final Iterable<IDescription> equations = getChildrenWithKeyword(IKeyword.EQUALS);
			for (final IDescription equation : equations) {
				final IExpressionDescription desc = equation.getFacet(IKeyword.EQUATION_LEFT);
				desc.compile(equation);
				final IExpression exp = desc.getExpression();
				if (exp instanceof IOperator op
						&& (op.arg(0).getName().equals(nm) || op.arg(1) != null && op.arg(1).getName().equals(nm)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Gets the formal args.
	 *
	 * @return the formal args
	 */
	@Override
	public Iterable<IDescription> getFormalArgs() { return getChildrenWithKeyword(ARG); }

	/**
	 * Gets the passed args.
	 *
	 * @return the passed args
	 */
	@Override
	public Arguments getPassedArgs() { return passedArgs == null ? Arguments.NULL : passedArgs; }

	@Override
	public String getName() {
		String s = super.getName();
		if (s == null) {
			// Special case for aspects
			if (IKeyword.ASPECT.equals(getKeyword())) {
				s = IKeyword.DEFAULT;
			} else {
				if (IKeyword.REFLEX.equals(getKeyword())) {
					warning("Reflexes should be named", MISSING_NAME, getUnderlyingElement());
				}
				s = IKeyword.INTERNAL + getKeyword() + String.valueOf(COUNTER.GET_UNIQUE());
			}
			setName(s);
		}
		return s;
	}

	@Override
	public String toString() {
		return getKeyword() + " " + getName();
	}

	@Override
	public String getTitle() {
		String kw = getKeyword();
		if (IKeyword.LET.equals(kw)) {
			kw = "Declaration of temporary variable ";
		} else {
			kw = StringUtils.capitalize(kw);
		}
		String nm = getName();
		if (nm.contains(IKeyword.INTERNAL)) {
			nm = getLitteral(IKeyword.ACTION);
			if (nm == null) { nm = "statement"; }
		}
		String in = "";
		if (getArtefact().isTopLevel()) {
			final IDescription d = getEnclosingDescription();
			if (d == null) {
				in = " defined in " + getOriginName();
			} else {
				in = " of " + d.getTitle();
			}
		}
		return kw + " " + nm + " " + in;
	}

	@Override
	public IDescription validate() {
		if (isValidated()) return this;
		final IDescription result = super.validate();
		validatePassedArgs();
		return result;
	}

	/**
	 * Validate passed args.
	 *
	 * @return the arguments
	 */
	protected Arguments validatePassedArgs() {
		final IDescription superDesc = getEnclosingDescription();
		if (passedArgs != null) {
			passedArgs.forEachFacet((nm, exp) -> {
				if (exp != null) { exp.compile(superDesc); }
				return true;
			});
		}
		if (isCreate()) { verifyInits(passedArgs); }
		return passedArgs;
	}

	/**
	 * Verify inits.
	 *
	 * @param ca
	 *            the ca
	 */
	private void verifyInits(final Arguments ca) {
		if (ca == null) return;
		final ITypeDescription denotedSpecies = getGamlType().getDenotedSpecies();
		if (denotedSpecies == null) {
			if (!ca.isEmpty()) {
				warning("Impossible to verify the validity of the arguments. Use them at your own risk.",
						UNKNOWN_ARGUMENT);
			}
			return;
		}
		ca.forEachFacet((nm, exp) -> {
			// hqnghi check attribute is not exist in both main model and
			// micro-model
			if (!denotedSpecies.hasAttribute(nm) && denotedSpecies instanceof ExperimentDescription
					&& !denotedSpecies.getModelDescription().hasAttribute(nm)) {
				// end-hqnghi
				error("Attribute " + nm + " does not exist in species " + denotedSpecies.getName(), UNKNOWN_ARGUMENT,
						exp.getTarget(), (String[]) null);
				return false;
			}
			IType<?> initType = NO_TYPE;
			IType<?> varType = NO_TYPE;
			final IVariableDescription vd = denotedSpecies.getAttribute(nm);
			if (vd != null) { varType = vd.getGamlType(); }
			if (exp != null) {
				final IExpression expr = exp.getExpression();
				if (expr != null) { initType = expr.getGamlType(); }
				if (varType != NO_TYPE && !initType.isTranslatableInto(varType)) {
					final boolean isDB = getFacet(FROM) != null
							&& getFacet(FROM).getExpression().getGamlType().isAssignableFrom(Types.LIST);
					if (isDB && initType.equals(Types.STRING)) return true;
					warning("The type of attribute " + nm + " should be " + varType, SHOULD_CAST, exp.getTarget(),
							varType.toString());
				}
			}

			return true;
		});

	}

	@Override
	protected IExpression createVarWithTypes(final String tag) {
		compileTypeProviderFacets();
		IType t = getGamlType();
		final String kw = getKeyword();
		IType ct = t.getContentType();
		if (isCreate() || IKeyword.CAPTURE.equals(kw) || IKeyword.RELEASE.equals(kw)) {
			ct = t;
			t = Types.LIST;
		} else if (t == NO_TYPE && !isSet(Flag.NoTypeInference)) { t = inferType(); }
		IType kt = t.getKeyType();
		// Definition of the content type and key type
		if (hasFacet(AS)) {
			ct = getTypeDenotedByFacet(AS);
		} else if (hasFacet(SPECIES)) {
			final IExpression expr = getFacetExpr(SPECIES);
			if (expr != null) {
				ct = expr.getGamlType().getContentType();
				kt = expr.getGamlType().getKeyType();
			}
		}
		return addNewTempIfNecessary(tag, GamaType.from(t, kt, ct));

	}

	/**
	 * Infer type.
	 *
	 * @param t
	 *            the t
	 * @return the i type
	 */
	private IType inferType() {
		IType t = NO_TYPE;
		// If the type is not defined, we try to infer it from the facets only if the flag is not set (see #385)
		if (hasFacet(VALUE)) {
			final IExpression value = getFacetExpr(VALUE);
			if (value != null) { t = value.getGamlType(); }
		} else if (hasFacet(OVER)) {
			final IExpression expr = getFacetExpr(OVER);
			if (expr != null) {
				// If of type pair, find the common supertype of key and contents
				if (Types.PAIR.isAssignableFrom(expr.getGamlType())) {
					t = GamaType.findCommonType(expr.getGamlType().getContentType(), expr.getGamlType().getKeyType());
				} else {
					t = expr.getGamlType().getContentType();
				}
			}
		} else if (hasFacet(FROM) && hasFacet(IKeyword.TO)) {
			t = GamaType.findCommonType(getFacetExpr(FROM), getFacetExpr(IKeyword.TO), getFacetExpr(IKeyword.STEP));
		}
		return t;
	}

	/**
	 * Adds the new temp if necessary.
	 *
	 * @param facetName
	 *            the facet name
	 * @param type
	 *            the type
	 * @return the i var expression
	 */
	public IVarExpression addNewTempIfNecessary(final String facetName, final IType type) {
		final String varName = getLitteral(facetName);
		final IDescription sup = getEnclosingDescription();
		if (!(sup instanceof StatementWithChildrenDescription sc)) {
			error("Impossible to return " + varName, GENERAL);
			return null;
		}
		return (IVarExpression) sc.addTemp(this, facetName, varName, type);
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	@Override
	public Iterable<IDescription> getOwnChildren() { return Collections.emptyList(); }

	/**
	 * Creates the compiled args.
	 *
	 * @return the arguments
	 */
	public Arguments createCompiledArgs() {
		return passedArgs;
	}

}
