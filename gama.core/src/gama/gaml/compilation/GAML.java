/*******************************************************************************************************
 *
 * GAML.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import static com.google.common.collect.Iterables.addAll;
import static gama.core.common.util.JavaUtils.collectImplementationClasses;
import static gama.core.util.GamaMapFactory.create;
import static gama.gaml.factories.DescriptionFactory.getStatementProto;
import static gama.gaml.factories.DescriptionFactory.getStatementProtoNames;
import static gama.gaml.types.Types.getBuiltInSpecies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ISkill;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IExecutionContext;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.GamaMapFactory;
import gama.core.util.ICollector;
import gama.core.util.IContainer;
import gama.core.util.IMap;
import gama.core.util.file.GamlFileInfo;
import gama.core.util.file.IGamlResourceInfoProvider;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.gaml.compilation.kernel.GamaSkillRegistry;
import gama.gaml.constants.IConstantAcceptor;
import gama.gaml.descriptions.ExperimentDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IDescription.DescriptionVisitor;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.descriptions.OperatorProto;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.expressions.GamlExpressionFactory;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.expressions.units.UnitConstantExpression;
import gama.gaml.factories.ModelFactory;
import gama.gaml.types.IType;
import gama.gaml.types.Signature;
import gama.gaml.types.Types;

/**
 * Class GAML. Static support for various GAML constructs and functions
 *
 * @author drogoul
 * @since 16 mai 2013
 *
 */
public class GAML {

	/** The operators. */
	@SuppressWarnings ("unchecked") public static final Map<String, IMap<Signature, OperatorProto>> OPERATORS =
			Collections.synchronizedMap(GamaMapFactory.createUnordered());

	/** The iterators. */
	public static final Set<String> ITERATORS = Collections.synchronizedSet(new HashSet<>());

	/** The Constant CONSTANTS. */
	public static final Set<String> CONSTANTS = Collections.synchronizedSet(new HashSet<>());

	/** The Constant ADDITIONS. */
	public final static Multimap<Class, IDescription> ADDITIONS = Multimaps.synchronizedMultimap(HashMultimap.create());

	/** The Constant FIELDS. */
	public final static Multimap<Class, OperatorProto> FIELDS = Multimaps.synchronizedMultimap(HashMultimap.create());

	/** The units. */
	public static final Map<String, UnitConstantExpression> UNITS = Collections.synchronizedMap(new HashMap<>());

	/** The Constant VARTYPE2KEYWORDS. */
	public final static Multimap<Integer, String> VARTYPE2KEYWORDS =
			Multimaps.synchronizedMultimap(HashMultimap.create());

	/** The Constant LISTENERS_BY_CLASS. */
	public final static SetMultimap<Class, GamaHelper> LISTENERS_BY_CLASS =
			Multimaps.synchronizedSetMultimap(HashMultimap.create());

	/** The Constant LISTENERS_BY_NAME. */
	public final static SetMultimap<String, Class> LISTENERS_BY_NAME =
			Multimaps.synchronizedSetMultimap(HashMultimap.create());

	/** The expression factory. */
	public static volatile IExpressionFactory expressionFactory = null;

	/** The model factory. */
	// public static volatile ModelFactory modelFactory = null;

	/** The info provider. */
	private static IGamlResourceInfoProvider infoProvider = null;

	/** The gaml ecore utils. */
	private static IGamlEcoreUtils gamlEcoreUtils = null;

	/** The gaml model builer. */
	private static IGamlModelBuilder gamlModelBuilder = null;

	/** The gaml text validator. */
	private static IGamlTextValidator gamlTextValidator = null;

	/**
	 * Not null.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 * @return the t
	 */
	public static <T> T notNull(final IScope scope, final T object) {
		return notNull(scope, object, "Error: nil value detected");
	}

	/**
	 * Not null.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 * @param error
	 *            the error
	 * @return the t
	 */
	public static <T> T notNull(final IScope scope, final T object, final String error) {
		if (object == null) throw GamaRuntimeException.error(error, scope);
		return object;
	}

	/**
	 * Empty check.
	 *
	 * @param <T>
	 *            the generic type
	 * @param scope
	 *            the scope
	 * @param container
	 *            the container
	 * @return the t
	 */
	@SuppressWarnings ("rawtypes")
	public static <T extends IContainer> T emptyCheck(final IScope scope, final T container) {
		if (notNull(scope, container).isEmpty(scope))
			throw GamaRuntimeException.error("Error: the container is empty", scope);
		return container;
	}

	/**
	 *
	 * Parsing and compiling GAML utilities
	 *
	 */

	public static ModelFactory getModelFactory() {
		return new ModelFactory();
		// Returning a new instance eliminates a lot of ConcurrentModificationException
		// if (modelFactory == null) { modelFactory = DescriptionFactory.getModelFactory(); }
		// return modelFactory;
	}

	/**
	 * Gets the expression factory.
	 *
	 * @return the expression factory
	 */
	public static IExpressionFactory getExpressionFactory() {
		if (expressionFactory == null) { expressionFactory = new GamlExpressionFactory(); }
		return expressionFactory;
	}

	/**
	 * Evaluate expression.
	 *
	 * @param expression
	 *            the expression
	 * @param a
	 *            the a
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static Object evaluateExpression(final String expression, final IAgent a) throws GamaRuntimeException {
		if (a == null) return null;
		if (expression == null || expression.isEmpty())
			throw GamaRuntimeException.error("Enter a valid expression", a.getScope());
		final IExpression expr = compileExpression(expression, a, true);
		if (expr == null) return null;
		final IScope scope = a.getScope().copy("in temporary expression evaluator");
		final Object o = scope.evaluate(expr, a).getValue();
		GAMA.releaseScope(scope);
		return o;
	}

	/**
	 * Compile expression.
	 *
	 * @param expression
	 *            the expression
	 * @param agent
	 *            the agent
	 * @param onlyExpression
	 *            the only expression
	 * @return the i expression
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IExpression compileExpression(final String expression, final IAgent agent,
			final boolean onlyExpression) throws GamaRuntimeException {
		if (agent == null) throw GamaRuntimeException.error("Agent is nil", GAMA.getRuntimeScope());
		final IExecutionContext tempContext = agent.getScope().getExecutionContext();
		return compileExpression(expression, agent, tempContext, onlyExpression);
	}

	/**
	 * Compile expression.
	 *
	 * @param expression
	 *            the expression
	 * @param agent
	 *            the agent
	 * @param tempContext
	 *            the temp context
	 * @param onlyExpression
	 *            the only expression
	 * @return the i expression
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IExpression compileExpression(final String expression, final IAgent agent,
			final IExecutionContext tempContext, final boolean onlyExpression) throws GamaRuntimeException {
		if (agent == null) throw GamaRuntimeException.error("Agent is nil", tempContext.getScope());
		final IDescription context = agent.getSpecies().getDescription();
		try {
			return getExpressionFactory().createExpr(expression, context, tempContext);
		} catch (final Throwable e) {
			// Maybe it is a statement instead ?
			if (onlyExpression) throw GamaRuntimeException.create(e, tempContext.getScope());
			try {
				return getExpressionFactory().createTemporaryActionForAgent(agent, expression, tempContext);
			} catch (final Throwable e2) {
				// Not a statement.
				throw GamaRuntimeException.error(e.getMessage() + "\n" + e2.getMessage(), tempContext.getScope());
			}

		}
	}

	/**
	 * Gets the model context.
	 *
	 * @return the model context
	 */
	public static ModelDescription getModelContext() {
		IExperimentPlan experiment = GAMA.getExperiment();
		if (experiment == null) return null;
		return experiment.getModel().getDescription();
	}

	/**
	 * Gets the experiment context.
	 *
	 * @param a
	 *            the a
	 * @return the experiment context
	 */
	public static ExperimentDescription getExperimentContext(final IAgent a) {
		if (a == null) return null;
		final IScope scope = a.getScope();
		final ITopLevelAgent agent = scope.getExperiment();
		if (agent == null) return null;
		return (ExperimentDescription) agent.getSpecies().getDescription();
	}

	/**
	 * Register info provider.
	 *
	 * @param info
	 *            the info
	 */
	public static void registerInfoProvider(final IGamlResourceInfoProvider info) {
		infoProvider = info;
	}

	/**
	 * Register gaml ecore utils.
	 *
	 * @param utils
	 *            the utils
	 */
	public static void registerGamlEcoreUtils(final IGamlEcoreUtils utils) {
		gamlEcoreUtils = utils;
	}

	/**
	 * Register gaml model builder.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param defaultInstance
	 *            the default instance
	 * @date 15 oct. 2023
	 */
	public static void registerGamlModelBuilder(final IGamlModelBuilder builder) {
		gamlModelBuilder = builder;
	}

	/**
	 * Register gaml text validator.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param validator
	 *            the validator
	 * @date 11 janv. 2024
	 */
	public static void registerGamlTextValidator(final IGamlTextValidator validator) {
		gamlTextValidator = validator;
	}

	/**
	 * Gets the ecore utils.
	 *
	 * @return the ecore utils
	 */
	public static IGamlEcoreUtils getEcoreUtils() { return gamlEcoreUtils; }

	/**
	 * Gets the info.
	 *
	 * @param uri
	 *            the uri
	 * @param stamp
	 *            the stamp
	 * @return the info
	 */
	public static GamlFileInfo getInfo(final URI uri, final long stamp) {
		return infoProvider.getInfo(uri, stamp);
	}

	/**
	 * Gets the info.
	 *
	 * @param uri
	 *            the uri
	 * @return the info
	 */
	public static GamlFileInfo getInfo(final URI uri) {
		return infoProvider.getInfo(uri);
	}

	/**
	 * Gets the contents.
	 *
	 * @param uri
	 *            the uri
	 * @return the contents
	 */
	public static ISyntacticElement getContents(final URI uri) {
		return infoProvider.getContents(uri);
	}

	/**
	 * Gets the all fields.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the all fields
	 */
	public static Map<String, OperatorProto> getAllFields(final Class clazz) {
		final List<Class> classes = collectImplementationClasses(clazz, Collections.EMPTY_SET, FIELDS.keySet());
		final Map<String, OperatorProto> fieldsMap = create();
		for (final Class c : classes) {
			for (final OperatorProto desc : FIELDS.get(c)) { fieldsMap.put(desc.getName(), desc); }
		}
		return fieldsMap;
	}

	/**
	 * Gets the all children of.
	 *
	 * @param base
	 *            the base
	 * @param skills
	 *            the skills
	 * @return the all children of
	 */
	public static Iterable<IDescription> getAllChildrenOf(final Class base,
			final Iterable<Class<? extends ISkill>> skills) {
		final List<Class> classes = collectImplementationClasses(base, skills, ADDITIONS.keySet());
		try (ICollector<IDescription> list = Collector.getList()) {
			for (Class c : classes) { list.addAll(ADDITIONS.get(c)); }
			return list;
		}
	}

	/**
	 * Gets the all fields.
	 *
	 * @return the all fields
	 */
	public static Collection<OperatorProto> getAllFields() { return FIELDS.values(); }

	/**
	 * Gets the all vars.
	 *
	 * @return the all vars
	 */
	public static Collection<IDescription> getAllVars() {
		final HashSet<IDescription> result = new HashSet<>();

		final DescriptionVisitor<IDescription> varVisitor = desc -> {
			result.add(desc);
			return true;
		};

		final DescriptionVisitor<IDescription> actionVisitor = desc -> {
			addAll(result, ((StatementDescription) desc).getFormalArgs());
			return true;
		};

		for (final TypeDescription desc : Types.getBuiltInSpecies().values()) {
			desc.visitOwnAttributes(varVisitor);
			desc.visitOwnActions(actionVisitor);

		}
		GamaSkillRegistry.INSTANCE.visitSkills(desc -> {
			((TypeDescription) desc).visitOwnAttributes(varVisitor);
			((TypeDescription) desc).visitOwnActions(actionVisitor);
			return true;
		});

		return result;
	}

	/**
	 * Gets the statements for skill.
	 *
	 * @param s
	 *            the s
	 * @return the statements for skill
	 */
	public static Collection<SymbolProto> getStatementsForSkill(final String s) {
		final Set<SymbolProto> result = new LinkedHashSet<>();
		for (final String p : getStatementProtoNames()) {
			final SymbolProto proto = getStatementProto(p);
			if (proto != null && proto.shouldBeDefinedIn(s)) { result.add(proto); }
		}
		return result;
	}

	/**
	 * Gets the all actions.
	 *
	 * @return the all actions
	 */
	public static Collection<IDescription> getAllActions() {
		SetMultimap<String, IDescription> result = MultimapBuilder.hashKeys().linkedHashSetValues().build();

		final DescriptionVisitor<IDescription> visitor = desc -> {
			result.put(desc.getName(), desc);
			return true;
		};

		for (final TypeDescription s : getBuiltInSpecies().values()) { s.visitOwnActions(visitor); }
		GamaSkillRegistry.INSTANCE.visitSkills(desc -> {
			((SkillDescription) desc).visitOwnActions(visitor);
			return true;
		});
		return result.values();
	}

	/**
	 * @param name
	 * @return
	 */
	public static boolean isUnaryOperator(final String name) {
		if (!OPERATORS.containsKey(name)) return false;
		final Map<Signature, OperatorProto> map = OPERATORS.get(name);
		for (final Signature s : map.keySet()) { if (s.isUnary()) return true; }
		return false;
	}

	/**
	 * Gets the constant acceptor.
	 *
	 * @return the constant acceptor
	 */
	public static IConstantAcceptor getConstantAcceptor() {
		return (name, value, doc, deprec, isTime, names) -> {
			if (UNITS.containsKey(name)) return false;
			// DEBUG.LOG("Added constant " + name);
			final IType t = Types.get(value.getClass());
			final UnitConstantExpression exp =
					getExpressionFactory().createUnit(value, t, name, doc, deprec, isTime, names);
			UNITS.put(name, exp);
			if (names != null) { for (final String s : names) { UNITS.put(s, exp); } }
			return true;
		};
	}

	/**
	 * Gets the model builder.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the model builder
	 * @date 15 oct. 2023
	 */
	public static IGamlModelBuilder getModelBuilder() { return gamlModelBuilder; }

	/**
	 * Validate. Based on a best guess regarding the contents provided (model, species, statements or expression).
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param entered
	 *            the entered
	 * @param b
	 *            the b
	 * @return the list
	 * @date 11 janv. 2024
	 */
	public static List<GamlCompilationError> validate(final String entered, final boolean syntaxOnly) {
		List<GamlCompilationError> errors = new ArrayList<>();
		if (entered.startsWith(IKeyword.MODEL)) {
			gamlTextValidator.validateModel(entered, errors, syntaxOnly);
		} else if (entered.startsWith(IKeyword.SPECIES) || entered.startsWith(IKeyword.GRID)) {
			gamlTextValidator.validateSpecies(entered, errors, syntaxOnly);
		} else if (entered.lines().count() > 1) {
			gamlTextValidator.validateStatements(entered, errors, syntaxOnly);
		} else {
			gamlTextValidator.validateExpression(entered, errors, syntaxOnly);
		}
		return errors;
	}

}
