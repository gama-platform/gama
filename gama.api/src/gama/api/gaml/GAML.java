/*******************************************************************************************************
 *
 * GAML.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml;

import static gama.api.utils.JavaUtils.collectImplementationClasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import gama.api.GAMA;
import gama.api.additions.IConstantAcceptor;
import gama.api.additions.IGamaHelper;
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionFactory;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.factories.IExpressionDescriptionFactory;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.compilation.prototypes.IArtefactProtoFactory;
import gama.api.compilation.validation.IGamlModelBuilder;
import gama.api.compilation.validation.IGamlTextValidator;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.IExecutionContext;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.IGamlFileInfo;

/**
 * Class GAML. Static support for various GAML constructs and functions
 *
 * @author drogoul
 * @since 16 mai 2013
 *
 */
public class GAML {

	/** The operators. */
	@SuppressWarnings ("unchecked") public static final Map<String, Map<Signature, IArtefactProto.Operator>> OPERATORS =
			Collections.synchronizedMap(new HashMap<>());

	/** The iterators. */
	public static final Set<String> ITERATORS = Collections.synchronizedSet(new HashSet<>());

	/** The Constant CONSTANTS. */
	public static final Set<String> CONSTANTS = Collections.synchronizedSet(new HashSet<>());

	/** The Constant ADDITIONS. */
	public final static Multimap<Class, IDescription> ADDITIONS = Multimaps.synchronizedMultimap(HashMultimap.create());

	/** The Constant FIELDS. */
	public final static Multimap<Class, IArtefactProto> FIELDS = Multimaps.synchronizedMultimap(HashMultimap.create());

	/** The units. */
	public static final Map<String, IExpression.Unit> UNITS = Collections.synchronizedMap(new HashMap<>());

	/** The Constant LISTENERS_BY_CLASS. */
	public final static SetMultimap<Class, IGamaHelper> LISTENERS_BY_CLASS =
			Multimaps.synchronizedSetMultimap(HashMultimap.create());

	/** The Constant LISTENERS_BY_NAME. */
	public final static SetMultimap<String, Class> LISTENERS_BY_NAME =
			Multimaps.synchronizedSetMultimap(HashMultimap.create());

	/** The factories. */
	public final static Map<Integer, ISymbolDescriptionFactory> DESCRIPTION_FACTORIES = new HashMap<>();

	/** The description factory. */
	private static volatile IDescriptionFactory descriptionFactory = null;

	/** The artefact proto factory. */
	private static volatile IArtefactProtoFactory artefactProtoFactory = null;

	/** The expression factory. */
	public static volatile IExpressionFactory expressionFactory = null;

	/** The expression description factory. */
	public static volatile IExpressionDescriptionFactory expressionDescriptionFactory = null;

	/** The info provider. */
	private static Function<URI, ISyntacticElement> infoProvider = null;

	/** The gaml model builer. */
	private static IGamlModelBuilder gamlModelBuilder = null;

	/** The gaml text validator. */
	private static IGamlTextValidator gamlTextValidator = null;

	/**
	 * Sets the expression factory.
	 *
	 * @param factory
	 *            the new expression factory
	 */
	public static void registerExpressionFactory(final IExpressionFactory factory) {
		expressionFactory = factory;
	}

	/**
	 * Register artefact proto factory.
	 *
	 * @param factory
	 *            the factory
	 */
	public static void registerArtefactProtoFactory(final IArtefactProtoFactory factory) {
		artefactProtoFactory = factory;
	}

	/**
	 * Adds the factory.
	 *
	 * @param factory
	 *            the factory
	 */
	public static void registerSymbolFactory(final ISymbolDescriptionFactory factory) {
		int[] handles = factory.getKinds();
		for (int i : handles) { DESCRIPTION_FACTORIES.put(i, factory); }
	}

	/**
	 * Register description factory.
	 *
	 * @param factory
	 *            the factory
	 */
	public static void registerDescriptionFactory(final IDescriptionFactory factory) {
		descriptionFactory = factory;
	}

	/**
	 * Register expression description factory.
	 *
	 * @param factory
	 *            the factory
	 */
	public static void registerExpressionDescriptionFactory(final IExpressionDescriptionFactory factory) {
		expressionDescriptionFactory = factory;
	}

	/**
	 * Register info provider.
	 *
	 * @param info
	 *            the info
	 */
	public static void registerGamlContentProvider(final Function<URI, ISyntacticElement> info) {
		infoProvider = info;
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
	 * Gets the description factory.
	 *
	 * @return the description factory
	 */
	public static IDescriptionFactory getDescriptionFactory() { return descriptionFactory; }

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
			final IExpression.Unit exp = getExpressionFactory().createUnit(value, t, name, doc, deprec, isTime, names);
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
	 * Gets the expression factory.
	 *
	 * @return the expression factory
	 */
	public static IExpressionFactory getExpressionFactory() { return expressionFactory; }

	/**
	 * Gets the artefact proto factory.
	 *
	 * @return the artefact proto factory
	 */
	public static IArtefactProtoFactory getArtefactProtoFactory() { return artefactProtoFactory; }

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
	public static IModelDescription getModelContext() {
		IExperimentSpecies experiment = GAMA.getExperiment();
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
	public static IExperimentDescription getExperimentContext(final IAgent a) {
		if (a == null) return null;
		final IScope scope = a.getScope();
		final ITopLevelAgent agent = scope.getExperiment();
		if (agent == null) return null;
		return (IExperimentDescription) agent.getSpecies().getDescription();
	}

	/**
	 * Gets the expression description factory.
	 *
	 * @return the expression description factory
	 */
	public static IExpressionDescriptionFactory getExpressionDescriptionFactory() {
		return expressionDescriptionFactory;
	}

	/**
	 * Gets the info.
	 *
	 * @param uri
	 *            the uri
	 * @return the info
	 */
	public static IGamlFileInfo getInfo(final URI uri) {
		return (IGamlFileInfo) GAMA.getMetadataProvider().getMetaData(uri, true, true);
	}

	/**
	 * Gets the contents.
	 *
	 * @param uri
	 *            the uri
	 * @return the contents
	 */
	public static ISyntacticElement getContents(final URI uri) {
		return infoProvider.apply(uri);
	}

	/**
	 * Gets the all fields.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the all fields
	 */
	public static Map<String, IArtefactProto> getAllFields(final Class clazz) {
		final List<Class> classes = collectImplementationClasses(clazz, Collections.EMPTY_SET, FIELDS.keySet());
		final Map<String, IArtefactProto> fieldsMap = new HashMap<>();
		for (final Class c : classes) {
			for (final IArtefactProto desc : FIELDS.get(c)) { fieldsMap.put(desc.getName(), desc); }
		}
		return fieldsMap;
	}

	/**
	 * Gets the statements for skill.
	 *
	 * @param s
	 *            the s
	 * @return the statements for skill
	 */
	public static Collection<IArtefactProto.Symbol> getStatementsForSkill(final String s) {
		final Set<IArtefactProto.Symbol> result = new LinkedHashSet<>();
		for (final String p : ArtefactProtoRegistry.getStatementProtoNames()) {
			final IArtefactProto.Symbol proto = ArtefactProtoRegistry.getStatementProto(p);
			if (proto != null && proto.shouldBeDefinedIn(s)) { result.add(proto); }
		}
		return result;
	}

	/**
	 * @param name
	 * @return
	 */
	public static boolean isUnaryOperator(final String name) {
		if (!OPERATORS.containsKey(name)) return false;
		final Map<Signature, IArtefactProto.Operator> map = OPERATORS.get(name);
		for (final Signature s : map.keySet()) { if (s.isUnary()) return true; }
		return false;
	}

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

	/**
	 * @param source
	 * @return
	 */
	public static int[] getLocationInFileInfo(final EObject source) {
		return gamlTextValidator.getStartLineAndOffsetInFileInfo(source);
	}

}
