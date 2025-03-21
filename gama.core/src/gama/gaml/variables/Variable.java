/*******************************************************************************************************
 *
 * Variable.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ISkill;
import gama.core.common.interfaces.IVarAndActionSupport;
import gama.core.common.util.JavaUtils;
import gama.core.common.util.StringUtils;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.benchmark.StopWatch;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.dev.DEBUG;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.GamaHelper;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.IGamaHelper;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.ConstantExpressionDescription;
import gama.gaml.descriptions.ExperimentDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.data.ListExpression;
import gama.gaml.expressions.units.TimeUnitConstantExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.operators.Cast;
import gama.gaml.species.AbstractSpecies;
import gama.gaml.statements.IExecutable;
import gama.gaml.types.GamaListType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Var.
 *
 *
 * FIXME FOR THE MOMENT SPECIES_WIDE CONSTANTS ARE NOT CONSIDERED (TOO MANY THINGS TO CONSIDER AND POSSIBILITIES TO MAKE
 * FALSE POSITIVE)
 */

/**
 * The Class Variable.
 */

/**
 * The Class Variable.
 */
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.NEW_VAR_ID,
				optional = false,
				doc = @doc ("The name of the attribute")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.TYPE_ID,
						optional = true,
						doc = { @doc ("The type of this attribute. Can be combined with facets 'of' and 'index' to describe container types") }),
				@facet (
						name = IKeyword.OF,
						type = IType.TYPE_ID,
						optional = true,
						doc = { @doc ("The type of the elements contained in the type of this attribute if it is a container type") }),
				@facet (
						name = IKeyword.INDEX,
						type = IType.TYPE_ID,
						optional = true,
						doc = { @doc ("The type of the index used to retrieve elements if the type of the attribute is a container type") }),
				@facet (
						name = IKeyword.INIT,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = IType.NONE,
						optional = true,
						doc = @doc ("The initial value of the attribute. Same as <- ")),
				@facet (
						name = "<-",
						internal = true,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = IType.NONE,
						optional = true,
						doc = @doc ("The initial value of the attribute. Same as init:")),
				@facet (
						name = IKeyword.UPDATE,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = IType.NONE,
						optional = true,
						doc = @doc ("An expression that will be evaluated each cycle to compute a new value for the attribute")),
				@facet (
						name = IKeyword.ON_CHANGE,
						type = IType.NONE,
						optional = true,
						doc = @doc (
								value = "Provides a block of statements that will be executed whenever the value of the attribute changes")),

				@facet (
						name = IKeyword.FUNCTION,
						// AD 02/16 TODO Allow to declare ITypeProvider.OWNER_TYPE here
						type = IType.NONE,
						optional = true,
						doc = @doc ("Used to specify an expression that will be evaluated each time the attribute is accessed. This facet is incompatible with both 'init:', 'update:' and 'on_change:' (or the equivalent final block)")),
				@facet (
						name = "->",
						internal = true,
						type = { IType.INT, IType.FLOAT, IType.POINT, IType.DATE },
						optional = true,
						doc = @doc ("Used to specify an expression that will be evaluated each time the attribute is accessed. Equivalent to 'function:'. This facet is incompatible with both 'init:' and 'update:' and 'on_change:' (or the equivalent final block)")),

				@facet (
						name = IKeyword.CONST,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Indicates whether this attribute can be subsequently modified or not")),
				@facet (
						name = IKeyword.AMONG,
						type = IType.LIST,
						optional = true,
						doc = @doc ("A list of constant values among which the attribute can take its value")) },
		omissible = IKeyword.NAME)
@symbol (
		kind = ISymbolKind.Variable.REGULAR,
		with_sequence = false,
		concept = { IConcept.ATTRIBUTE })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@doc ("Declaration of an attribute of a species or an experiment")
@validator (gama.gaml.variables.Variable.VarValidator.class)
@SuppressWarnings ({ "rawtypes" })
public class Variable extends Symbol implements IVariable {

	static {
		DEBUG.OFF();
	}

	/**
	 * The Class VarValidator.
	 */
	public static class VarValidator implements IDescriptionValidator {

		/** The assignment facets. */
		public static final List<String> assignmentFacets = Arrays.asList(VALUE, INIT, FUNCTION, UPDATE, MIN, MAX);

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription vd) {
			final VariableDescription cd = (VariableDescription) vd;
			final boolean isParameter = cd.isExperimentParameter();
			final String name = cd.getName();
			// Verifying that the name is not null
			if (name == null) {
				cd.error("The attribute name is missing", IGamlIssue.MISSING_NAME);
				return;
			}

			if (!isParameter) {
				// Verifying that the name is not a type
				final IType t = cd.getEnclosingDescription().getTypeNamed(name);
				if (t != Types.NO_TYPE && !t.isAgentType()) {
					cd.error(name + " is a type name. It cannot be used as an attribute name", IGamlIssue.IS_A_TYPE,
							NAME, name);
					return;
				}
				// Verifying that the name is not reserved
				if (RESERVED.contains(name)) {
					cd.error(name + " is a reserved keyword. It cannot be used as an attribute name",
							IGamlIssue.IS_RESERVED, NAME, name);
					return;
				}
				// if the step is defined with simply an init, we copy the init
				// expression to the update facet as well, so that it is
				// recomputed every time it changes (necessary for
				// time-dependent units. Should be done, actually, for any
				// variable that manipulates time-dependent units
				// May 2019: a warning is emitted instead (see why in #2574)
				if (STEP.equals(name) && cd.hasFacet(INIT) && !cd.hasFacet(UPDATE)) {
					final IExpression expr = cd.getFacetExpr(INIT);
					if (expr.findAny(e -> e instanceof TimeUnitConstantExpression tu && !tu.isConst())) {
						cd.warning(
								"Time dependent constants used to define the step at initialization are computed once based on the current_date. "
										+ "The resulting durations may be irrelevant after a few cycles. "
										+ "An 'update:' facet should be defined with the same expression to recompute the step every cycle",
								IGamlIssue.CONFLICTING_FACETS, INIT);
					}
				}
			}
			// The name is ok. Now verifying the logic of facets
			// Verifying that 'function' is not used in conjunction with other
			// "value" facets
			if (cd.hasFacet(FUNCTION) && (cd.hasFacet(INIT) || cd.hasFacet(UPDATE) || cd.hasFacet(ON_CHANGE))) {
				cd.error("A function cannot have an 'init', 'on_change' or 'update' facet", IGamlIssue.REMOVE_VALUE,
						FUNCTION);
				return;
			}

			// Verifying that a constant has not 'update' or 'function' facet
			// and is not a parameter
			if (TRUE.equals(cd.getLitteral(CONST))) {
				if (cd.hasFacet(UPDATE)) {
					cd.warning("A constant attribute cannot have an update value (use init or <- instead)",
							IGamlIssue.REMOVE_CONST, UPDATE);
				} else if (cd.hasFacet(FUNCTION)) {
					cd.error("A function cannot be constant (use init or <- instead)", IGamlIssue.REMOVE_CONST,
							FUNCTION);
					return;
				} else if (cd.isParameter()) {
					cd.error("Parameter '" + cd.getParameterName() + "'  cannot be declared as constant ",
							IGamlIssue.REMOVE_CONST);
					return;
				} else if (cd.hasFacet(ON_CHANGE)) {
					cd.warning("A constant attribute cannot declare an 'on_change' facet", IGamlIssue.REMOVE_CONST,
							ON_CHANGE);
				}
			}
			if (cd.isParameter()) {
				assertCanBeParameter(cd);
			} else {
				assertValueFacetsTypes(cd, cd.getGamlType());
			}
			assertAssignmentFacetsTypes(cd);
			assertAmongValues(cd);
		}

		/**
		 * Assert among values.
		 *
		 * @param vd
		 *            the vd
		 */
		public void assertAmongValues(final VariableDescription vd) {
			// if (vd.isParameter() && vd.getSpeciesContext().isExperiment()
			// && ((ExperimentDescription) vd.getSpeciesContext()).isBatch())
			// return;
			final IExpression amongExpression = vd.getFacetExpr(AMONG);
			final IExpression initExpression = vd.getFacetExpr(INIT);
			if (initExpression == null || !initExpression.isConst()) return;
			if (amongExpression instanceof ListExpression list) {
				final Object init = initExpression.getConstValue();
				if (!list.containsValue(init)) {
					if (list.getElements().length == 0) {
						vd.error("No value of " + vd.getName() + " can be chosen.", IGamlIssue.NOT_AMONG, AMONG);
					} else {
						vd.warning(
								"The initial value of " + vd.getName()
										+ " does not belong to the list of possible values. It will be initialized to "
										+ list.getElements()[0].serializeToGaml(true) + " instead.",
								IGamlIssue.WRONG_VALUE, INIT, String.valueOf(list.getElements()[0].getConstValue()));
					}
				}
			}

		}

		/**
		 * Assert assignment facets types.
		 *
		 * @param vd
		 *            the vd
		 */
		public void assertAssignmentFacetsTypes(final VariableDescription vd) {
			for (final String s : assignmentFacets) {
				Assert.typesAreCompatibleForAssignment(s, vd, vd.getName(), vd.getGamlType(), /* vd.getContentType(), */
						vd.getFacet(s));
			}
		}

		/**
		 * Assert value facets types.
		 *
		 * @param vd
		 *            the vd
		 * @param vType
		 *            the v type
		 */
		public void assertValueFacetsTypes(final VariableDescription vd, final IType<?> vType) {

			// final IType type = null;
			// final String firstValueFacet = null;
			final IExpression amongExpression = vd.getFacetExpr(AMONG);
			if (amongExpression != null && !vType.isAssignableFrom(amongExpression.getGamlType().getContentType())) {
				vd.error("Variable " + vd.getName() + " of type " + vType + " cannot be chosen among "
						+ amongExpression.serializeToGaml(false), IGamlIssue.NOT_AMONG, AMONG);
			}
			// AD 6/2/22 Restriction removed:
			// if (!amongExpression.isContextIndependant()) {
			// vd.warning(
			// "Facet 'among:' should only be provided with a literal constant list for its definition. Proceed at your
			// own risk with this variable",
			// IGamlIssue.NOT_CONST, AMONG);
			// }
		}

		/**
		 * Assert can be parameter.
		 *
		 * @param cd
		 *            the cd
		 */
		public void assertCanBeParameter(final VariableDescription cd) {
			if (PARAMETER.equals(cd.getKeyword()) /* facets.equals(KEYWORD, PARAMETER) */) {
				final String varName = cd.getLitteral(VAR);
				VariableDescription targetedVar = cd.getModelDescription().getAttribute(varName);

				if (targetedVar == null) {
					// AD 07/21 : Adds the possibility for experiment variables to become parameters
					// We keep on looking after looking in the model so as to make sure that built-in parameters (like
					// seed, for instance) can be correctly retrieved
					ExperimentDescription ed = (ExperimentDescription) cd.getEnclosingDescription();
					targetedVar = ed.getAttribute(varName);
					if (targetedVar == null) {
						final String p = "Parameter '" + cd.getParameterName() + "' ";
						cd.error(p + "cannot refer to the non-global variable " + varName, IGamlIssue.UNKNOWN_VAR,
								IKeyword.VAR);
						return;
					}
					if (ed.isBatch()) {
						final String p = "Parameter '" + cd.getParameterName() + "' ";
						cd.warning(p
								+ "refers to an experiment variable, which cannot be explored during batch experiments. Move "
								+ varName + " to the global section if it makes sense.", IGamlIssue.WRONG_CONTEXT,
								IKeyword.VAR);
					}
				}
				if (cd.getGamlType().equals(Types.NO_TYPE)) {
					cd.error("Impossible to determine the type of the parameter " + varName, IGamlIssue.UNMATCHED_TYPES,
							IKeyword.TYPE);
					return;
				}
				if (cd.getGamlType().id() != targetedVar.getGamlType().id()) {
					final String p = "Parameter '" + cd.getParameterName() + "' ";
					cd.error(p + "type must be the same as that of " + varName, IGamlIssue.UNMATCHED_TYPES,
							IKeyword.TYPE);
					return;
				}
				assertValueFacetsTypes(cd, targetedVar.getGamlType());
				if (cd.isNotModifiable() || targetedVar.isNotModifiable()) {
					final String p = "Parameter '" + cd.getParameterName() + "' ";
					cd.info(p + "Since the variable is declared as const, this parameter will be read-only.",
							IGamlIssue.REMOVE_CONST);
				}
			}

			/**
			 * Assert value facets types.
			 */
			assertValueFacetsTypes(cd, cd.getGamlType());
			// AD 6/2/22 Restriction removed: min and max facets are not supposed to be constants anymore in parameters.
			// Their value can only be updated through the 'updates' facet of another parameter, though
			// final IExpression min = cd.getFacetExpr(MIN);
			// if (min != null && !min.isConst()) {
			// final String p = "Parameter '" + cd.getParameterName() + "' ";
			// cd.error(p + " min value must be constant", IGamlIssue.NOT_CONST, MIN);
			// return;
			// }
			// final IExpression max = cd.getFacetExpr(MAX);
			// if (max != null && !max.isConst()) {
			// final String p = "Parameter '" + cd.getParameterName() + "' ";
			// cd.error(p + " max value must be constant", IGamlIssue.NOT_CONST, MAX);
			// return;
			// }

			// Cf. #3574
			IExpression foundInit;

			if (cd.getFacetExpr(INIT) != null) {
				foundInit = cd.getFacetExpr(INIT);
			} else {
				// Check if steps increment or not and init with corresponding limit range

				IExpression step = cd.getFacetExpr(STEP);
				if (step != null && step.isContextIndependant()) {
					Double stepValue = Cast.asFloat(null, step.getConstValue());
					if (stepValue < 0) {
						foundInit = cd.getFacetExpr(MAX);
					} else {
						foundInit = cd.getFacetExpr(MIN);
					}
				} else {
					foundInit = cd.getFacetExpr(MIN); // By default, we assume step is positive
				}

			}

			final IExpression init = foundInit;

			if (init == null) {
				final String p = "Parameter '" + cd.getParameterName() + "' ";
				cd.error(p + " must have an initial or minimal value.", IGamlIssue.NO_INIT, cd.getUnderlyingElement(),
						StringUtils.toGaml(cd.getGamlType().getDefault(), false));
				return;
			}
			// Cf. #3493 && #3622
			for (String f : VariableDescription.INIT_DEPENDENCIES_FACETS) {
				IExpression initExpr = cd.getFacetExpr(f);
				if (initExpr != null && !initExpr.isAllowedInParameters()) {
					cd.error(initExpr.serializeToGaml(true)
							+ " cannot be used in the context of experiments. Please use a constant expression or redeclare this parameter in the experiments",
							IGamlIssue.WRONG_CONTEXT, f);
				}
			}

			// IDescription oneExperiment = Iterables.getFirst(cd.getModelDescription().getExperiments(), null);
			// if (oneExperiment != null) {
			// for (String f : VariableDescription.INIT_DEPENDENCIES_FACETS) {
			// IExpressionDescription initExpr = cd.getFacet(f);
			// if (initExpr != null) {
			// IExpressionDescription ed = initExpr.cleanCopy();
			// if (GAML.getExpressionFactory().getParser().compile(ed, oneExperiment) == null) {
			// // COMPLETEMENT FAUX == RENVOIE NULL dans le Cas de Month car ce n'est pas une constante,
			// // mais c'est tout. Du coup 1#month passe sans problème ..car..car
			// cd.error(
			// "This expression cannot be used in the context of experiments. Please use a constant expression or
			// redeclare this parameter in the experiments",
			// IGamlIssue.WRONG_CONTEXT, f);
			// }
			// }
			// }
			// }

			// AD 6/2/22 Restriction removed: non-boolean vars can "enable" or "disable" others based on the cast of
			// their value to bool
			// if (cd.hasFacet(ENABLES) && !cd.getGamlType().equals(Types.BOOL)) {
			// cd.warning("The 'enables' facet has no meaning for non-boolean parameters",
			// IGamlIssue.CONFLICTING_FACETS, ENABLES);
			// }
			// if (cd.hasFacet(DISABLES) && !cd.getGamlType().equals(Types.BOOL)) {
			// cd.warning("The 'disables' facet has no meaning for non-boolean parameters",
			// IGamlIssue.CONFLICTING_FACETS, DISABLES);
			// }
			// AD 15/04/14: special case for files
			// AD 17/06/16 The restriction is temporarily removed
			// if (!init.isConst() && init.getType().getType().id() !=
			// IType.FILE) {
			// final String p = "Parameter '" + cd.getParameterName() + "' ";
			// cd.error(p + "initial value must be constant",
			// IGamlIssue.NOT_CONST, INIT);
			// return;
			// }
			if (cd.hasFacet(UPDATE) || cd.isFunction()) {
				final String p = "Parameter '" + cd.getParameterName() + "' ";
				cd.error(p + "cannot have an 'update' or 'function' facet", IGamlIssue.REMOVE_VALUE);
			}

		}

	}

	/** The init expression. */
	protected IExpression initExpression;

	/** The on change expression. */
	protected final IExpression updateExpression, amongExpression, functionExpression, onChangeExpression;

	/** The type. */
	protected IType type;

	/** The is not modifiable. */
	protected final boolean isNotModifiable;

	/** The setter. */
	public IGamaHelper getter, initer, setter;

	/** The listeners. */
	public Map<GamaHelper, IVarAndActionSupport> listeners;

	/** The s skill. */
	protected ISkill gSkill, sSkill;

	/** The on changer. */
	private IExecutable on_changer;

	/** The category. */
	protected String parameter, category;

	/** The must notify of changes. */
	protected boolean mustNotifyOfChanges;
	// private Object speciesWideValue;

	/**
	 * Instantiates a new variable.
	 *
	 * @param sd
	 *            the sd
	 */
	public Variable(final IDescription sd) {
		super(sd);
		final VariableDescription desc = (VariableDescription) sd;
		setName(sd.getName());
		parameter = desc.getParameterName();
		category = getLiteral(IKeyword.CATEGORY, null);
		updateExpression = getFacet(IKeyword.UPDATE);
		functionExpression = getFacet(IKeyword.FUNCTION);
		initExpression = getFacet(IKeyword.INIT);
		amongExpression = getFacet(IKeyword.AMONG);
		onChangeExpression = getFacet(IKeyword.ON_CHANGE);
		isNotModifiable = desc.isNotModifiable();
		type = desc.getGamlType();
	}

	/**
	 * Builds the helpers.
	 *
	 * @param species
	 *            the species
	 */
	private void buildHelpers(final AbstractSpecies species) {
		getter = getDescription().getGetter();
		if (getter != null) { gSkill = species.getSkillInstanceFor(getter.getSkillClass()); }
		initer = getDescription().getIniter();
		setter = getDescription().getSetter();
		if (setter != null) { sSkill = species.getSkillInstanceFor(setter.getSkillClass()); }
		addListeners(species);
		mustNotifyOfChanges =
				listeners != null && listeners.size() > 0 || onChangeExpression != null || on_changer != null;
	}

	/**
	 * // AD 2021: addition of the listeners
	 */
	private void addListeners(final AbstractSpecies species) {
		// if (IKeyword.LOCATION.equals(getName())) {

		// DEBUG.OUT("Adding listeners to " + this.getName());

		// }

		SpeciesDescription sp = species.getDescription();
		Class base = sp.getJavaBase();
		if (base == null) return;
		List<GamaHelper> helpers = new ArrayList<>();
		Iterable<Class<? extends ISkill>> skillClasses = Iterables.transform(sp.getSkills(), IDescription.TO_CLASS);
		if (GAML.LISTENERS_BY_NAME.containsKey(getName())) {
			DEBUG.OUT("Listeners found for " + getName());
			List<Class> classes =
					JavaUtils.collectImplementationClasses(base, skillClasses, GAML.LISTENERS_BY_NAME.get(getName()));
			if (!classes.isEmpty()) {
				for (Class c : classes) {
					Set<GamaHelper> set = GAML.LISTENERS_BY_CLASS.get(c);
					for (GamaHelper h : set) {
						if (h.getName().equals(getName())) {
							DEBUG.OUT("--> Adding listener found in " + c.getSimpleName());
							helpers.add(h);
						}
					}
				}
			}

		}

		if (!helpers.isEmpty()) {
			listeners = new HashMap<>();
			for (GamaHelper helper : helpers) {
				listeners.put(helper, species.getSkillInstanceFor(helper.getSkillClass()));
			}

		}

	}

	/**
	 * Coerce.
	 *
	 * @param agent
	 *            the agent
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected Object coerce(final IAgent agent, final IScope scope, final Object v) throws GamaRuntimeException {
		return type.cast(scope, v, null, false);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder().append(isNotModifiable() ? IKeyword.CONST : IKeyword.VAR);
		result.append(" ").append(type).append("[").append(getName()).append("]");
		return result.toString();
	}

	@Override
	public void setValue(final IScope scope, final Object initial) {
		final IExpressionDescription desc = ConstantExpressionDescription.create(initial);
		initExpression = desc.getExpression();
		setFacet(IKeyword.INIT, desc);
		// computeSpeciesConst();
	}

	@Override
	public void dispose() {
		super.dispose();
		initer = null;
		getter = null;
		setter = null;
		sSkill = null;
		gSkill = null;
	}

	@Override
	public boolean isParameter() { return getDescription().isParameter(); }

	@Override
	public VariableDescription getDescription() { return (VariableDescription) description; }

	@Override
	public boolean isUpdatable() { return updateExpression != null && !isNotModifiable; }

	@Override
	public boolean isFunction() { return functionExpression != null; }

	@Override
	public IType getType() { return type; }

	@Override
	public void initializeWith(final IScope scope, final IAgent a, final Object v) throws GamaRuntimeException {
		try (StopWatch w = GAMA.benchmark(scope, this)) {
			scope.setCurrentSymbol(this);
			if (v != null) {
				_setVal(a, scope, v);
			} else if (initExpression != null) {
				_setVal(a, scope, scope.evaluate(initExpression, a).getValue());
			} else if (initer != null) {
				final Object val = initer.run(scope, a, gSkill == null ? a : gSkill);
				_setVal(a, scope, val);
			} else {
				_setVal(a, scope, getType().getDefault());
			}
		} catch (final GamaRuntimeException e) {
			e.addContext("in initializing attribute " + getName());
			throw e;
		} finally {
			scope.setCurrentSymbol(null);
		}
	}

	@Override
	public String getTitle() { return parameter; }

	@Override
	public String getCategory() {
		if (category == null) { category = IVariable.super.getCategory(); }
		return category;
	}

	// @Override
	// public Integer getDefinitionOrder() {
	// return definitionOrder;
	// }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		// Not yet ready to behave like parameter (with 'on_change' moved at the end of the statement) because of the
		// possible usages of the block for other tasks (like fuction:)
		// final List<IStatement> statements = new ArrayList<>();
		// for (final ISymbol c : commands) {
		// if (c instanceof IStatement) { statements.add((IStatement) c); }
		// }
		// if (!statements.isEmpty()) {
		// final IDescription d =
		// DescriptionFactory.create(IKeyword.ACTION, getDescription(), IKeyword.NAME, "inline");
		// ActionStatement action = new ActionStatement(d);
		// action.setChildren(statements);
		// on_changer = action;
		// }
	}

	@Override
	public String getName() { return name; }

	@Override
	public void setName(final String name) { this.name = name; }

	@Override
	public final void setVal(final IScope scope, final IAgent agent, final Object v) throws GamaRuntimeException {
		if (isNotModifiable) return;
		final Object oldValue = !mustNotifyOfChanges ? null : value(scope, agent);
		_setVal(agent, scope, v);
		if (mustNotifyOfChanges && !Objects.equal(oldValue, v)) {
			internalNotifyOfValueChange(scope, agent, oldValue, v);
		}
	}

	/**
	 * Internal notify of value change.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	private void internalNotifyOfValueChange(final IScope scope, final IAgent agent, final Object oldValue,
			final Object newValue) {
		if (onChangeExpression != null) {
			if (on_changer == null) {
				on_changer = agent.getSpecies().getAction(Cast.asString(scope, onChangeExpression.value(scope)));
			}
			scope.execute(on_changer, agent, null);
		}

		if (listeners != null) {
			listeners.forEach(
					(listener, skill) -> { listener.run(scope, agent, skill == null ? agent : skill, newValue); });
		}
	}

	/**
	 * Public method supposed to be only called from outside (e.g. in setLocation() for 'location') to trigger
	 * listeners. Notifies listeners declared in GAML (facet 'on_change:') and in Java (annotation 'listener'). Change
	 * the value of 'mustNotifyOfChanges' to false in order to avoid double notifications
	 *
	 * @param scope
	 * @param agent
	 * @param oldValue
	 * @param newValue
	 */
	@Override
	public final void notifyOfValueChange(final IScope scope, final IAgent agent, final Object oldValue,
			final Object newValue) {
		// so as to block internal notifications, since notifications are produced somewhere else in GAMA.
		mustNotifyOfChanges = false;
		internalNotifyOfValueChange(scope, agent, oldValue, newValue);
	}

	/**
	 * Sets the val.
	 *
	 * @param agent
	 *            the agent
	 * @param scope
	 *            the scope
	 * @param v
	 *            the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected void _setVal(final IAgent agent, final IScope scope, final Object v) throws GamaRuntimeException {
		Object val;
		val = coerce(agent, scope, v);
		val = checkAmong(agent, scope, val);
		if (setter != null) {
			setter.run(scope, agent, sSkill == null ? agent : sSkill, val);
		} else {
			agent.setAttribute(name, val);
		}
		// if (isSpeciesConst) {
		// speciesWideValue = val;
		// }
	}

	/**
	 * Check among.
	 *
	 * @param agent
	 *            the agent
	 * @param scope
	 *            the scope
	 * @param val
	 *            the val
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected Object checkAmong(final IAgent agent, final IScope scope, final Object val) throws GamaRuntimeException {
		if (amongExpression == null) return val;
		final List among = Cast.asList(scope, scope.evaluate(amongExpression, agent).getValue());
		if (among == null || among.contains(val)) return val;
		if (among.isEmpty()) return null;
		throw GamaRuntimeException.error(
				"Value " + val + " is not included in the possible values [" + among + "] of variable " + name, scope);
	}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		return value(scope, scope.getAgent());
	}

	@Override
	public Object value(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		// if (isSpeciesConst) { return speciesWideValue; }
		if (getter != null) return getter.run(scope, agent, gSkill == null ? agent : gSkill);
		if (functionExpression != null) return scope.evaluate(functionExpression, agent).getValue();
		// Var not yet initialized. May happen when asking for its value while initializing an editor
		// See Issue #2781 + Issue #3920
		if (!agent.hasAttribute(name) && (isNotModifiable || initExpression != null && initExpression.isConst())
				&& !description.isBuiltIn())
			return getInitialValue(scope);
		return agent.getAttribute(name);
	}

	@Override
	public Object getUpdatedValue(final IScope scope) {
		return updateExpression.value(scope);
	}

	@Override
	public Comparable getMinValue(final IScope scope) {
		return null;
	}

	@Override
	public Comparable getMaxValue(final IScope scope) {
		return null;
	}

	@Override
	public Comparable getStepValue(final IScope scope) {
		return null;
	}

	@Override
	public List getAmongValue(final IScope scope) {
		if (amongExpression == null) return null;
		try {
			return GamaListType.staticCast(scope, amongExpression.value(scope), getType(), false);
		} catch (final GamaRuntimeException e) {
			return null;
		}
	}

	@Override
	public Object getInitialValue(final IScope scope) {
		if (initExpression != null) {
			try {
				return initExpression.value(scope);
			} catch (final GamaRuntimeException e) {
				return null;
			}
		}
		return value(scope);
	}

	@Override
	public String getUnitLabel(final IScope scope) {
		return null;
	}

	@Override
	public void setUnitLabel(final String label) {}

	@Override
	public boolean isEditable() { return !isNotModifiable; }

	/**
	 * Method isDefined()
	 *
	 * @see gama.core.kernel.experiment.IParameter#isDefined()
	 */
	@Override
	public boolean isDefined() { return true; }

	/**
	 * Method setDefined()
	 *
	 * @see gama.core.kernel.experiment.IParameter#setDefined(boolean)
	 */
	@Override
	public void setDefined(final boolean b) {}

	@Override
	public boolean acceptsSlider(final IScope scope) {
		// No facets are available to describe whether or not a slider should be
		// defined. AD change: if we are int or float and max, min and step are defined, we accept it for number
		// variables;

		return false;
	}

	@Override
	public void setEnclosing(final ISymbol enclosing) {
		if (enclosing instanceof AbstractSpecies) { buildHelpers((AbstractSpecies) enclosing); }
	}

	@Override
	public boolean isMicroPopulation() {
		final VariableDescription desc = getDescription();
		if (desc == null) return false;
		return desc.isSyntheticSpeciesContainer();
	}

	@Override
	public List<GamaColor> getColors(final IScope scope) {
		// No facet available to describe a potential color
		return null;
	}

	@Override
	public GamaColor getColor(final IScope scope) {
		return null;
	}

	@Override
	public boolean isNotModifiable() { return isNotModifiable; }

	@Override
	public boolean isDefinedInExperiment() { return getDescription().isDefinedInExperiment(); }

	@Override
	public void setValueNoCheckNoNotification(final Object value) {
		// Do nothing for the moment ? Vars are not supposed to be changed that way
	}

}
