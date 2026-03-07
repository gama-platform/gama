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

import static gama.api.constants.IKeyword.MY;
import static gama.api.utils.JavaUtils.collectImplementationClasses;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.additions.IConstantAcceptor;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.GamlCompilationError;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.artefacts.IArtefact.Operator;
import gama.api.compilation.artefacts.IArtefactFactory;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionFactory;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.factories.IExpressionDescriptionFactory;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.compilation.validation.IGamlModelBuilder;
import gama.api.compilation.validation.IGamlTextValidator;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.scope.IExecutionContext;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.IGamlFileInfo;

/**
 * The GAML class provides the central registry and factory access point for the GAML language infrastructure.
 *
 * <p>
 * This class serves as a thread-safe static registry for all GAML language elements including:
 * <ul>
 * <li>Operators and their signatures</li>
 * <li>Iterators and constants</li>
 * <li>Units of measurement</li>
 * <li>Field and action additions to agent classes</li>
 * <li>Symbol description factories</li>
 * <li>Expression and artefact factories</li>
 * </ul>
 * </p>
 *
 * <p>
 * The class uses concurrent data structures (ConcurrentHashMap, ConcurrentHashSet) to ensure thread-safe registration
 * and access during GAMA platform initialization and runtime. This allows multiple threads to register and access GAML
 * elements without explicit synchronization.
 * </p>
 *
 * <p>
 * Key responsibilities:
 * <ul>
 * <li>Managing factory instances for creating GAML descriptions, expressions, and prototypes</li>
 * <li>Registering and retrieving operators, constants, units, and iterators</li>
 * <li>Providing expression compilation and evaluation services</li>
 * <li>Managing additions (fields and actions) that extend agent classes</li>
 * <li>Validating GAML code (models, species, statements, expressions)</li>
 * </ul>
 * </p>
 *
 * @author drogoul
 * @since 16 mai 2013
 *
 */
public class GAML {

	/**
	 * Thread-safe registry of all GAML operators indexed by name and signature.
	 *
	 * <p>
	 * Maps operator names to their signature-specific implementations. Each operator name can have multiple
	 * implementations with different signatures (overloading). Uses ConcurrentHashMap for thread-safe access without
	 * locking during platform initialization.
	 * </p>
	 *
	 * <p>
	 * Structure: {@code Map<OperatorName, Map<Signature, OperatorImplementation>>}
	 * </p>
	 */
	private static final Map<String, Map<Signature, IArtefact.Operator>> OPERATORS = new ConcurrentHashMap<>();

	static {
		// Trick to accept "my" as an operator
		OPERATORS.put(MY, Collections.emptyMap());
	}

	/**
	 * Thread-safe set of all registered iterator names in GAML.
	 *
	 * <p>
	 * Iterators are special operators that loop over collections (e.g., "collect", "where", "count"). Uses
	 * ConcurrentHashMap.newKeySet() for efficient thread-safe access during registration.
	 * </p>
	 */
	private static final Set<String> ITERATORS = ConcurrentHashMap.newKeySet();

	/**
	 * Thread-safe set of all registered constant names in GAML.
	 *
	 * <p>
	 * Constants are predefined values available in GAML expressions (e.g., "pi", "e", "true", "false"). Uses
	 * ConcurrentHashMap.newKeySet() for efficient thread-safe access during registration.
	 * </p>
	 */
	private static final Set<String> CONSTANTS = ConcurrentHashMap.newKeySet();

	/**
	 * Thread-safe registry of additional descriptions (fields and actions) for agent classes.
	 *
	 * <p>
	 * This multimap stores descriptions that extend agent classes at runtime. Uses ConcurrentHashMap with concurrent
	 * sets as values to provide better concurrent performance than synchronized Guava multimaps by using lock-free
	 * reads and fine-grained locking only on writes.
	 * </p>
	 *
	 * <p>
	 * Structure: {@code Map<Class, Set<IDescription>>}
	 * </p>
	 */
	private final static Map<Class, Set<IDescription>> ADDITIONS = new ConcurrentHashMap<>();

	/**
	 * Thread-safe registry of field prototypes for agent classes.
	 *
	 * <p>
	 * This multimap stores field prototypes that extend agent classes. Uses ConcurrentHashMap with concurrent sets as
	 * values for efficient concurrent access during registration and retrieval.
	 * </p>
	 *
	 * <p>
	 * Structure: {@code Map<Class, Set<IArtefact>>}
	 * </p>
	 */
	private final static Map<Class, Set<IArtefact>> FIELDS = new ConcurrentHashMap<>();

	/**
	 * Thread-safe registry of all unit expressions in GAML.
	 *
	 * <p>
	 * Maps unit names to their expression representations. Units represent physical quantities like distance, time,
	 * mass (e.g., "m", "km", "s", "ms"). Uses ConcurrentHashMap for thread-safe access during initialization and
	 * runtime.
	 * </p>
	 */
	private static final Map<String, IExpression.Unit> UNITS = new ConcurrentHashMap<>();

	/**
	 * Thread-safe registry of symbol description factories indexed by their kind.
	 *
	 * <p>
	 * Maps factory kinds (integer handles) to their corresponding factory implementations. Uses ConcurrentHashMap for
	 * better performance under concurrent access compared to synchronized wrapper.
	 * </p>
	 */
	public final static Map<ISymbolKind, ISymbolDescriptionFactory> DESCRIPTION_FACTORIES = new ConcurrentHashMap<>();

	/**
	 * The description factory used to create GAML descriptions.
	 *
	 * <p>
	 * Volatile to ensure thread-safe visibility of the factory instance across threads.
	 * </p>
	 */
	private static volatile IDescriptionFactory descriptionFactory = null;

	/**
	 * The artefact proto factory used to create artefact prototypes.
	 *
	 * <p>
	 * Volatile to ensure thread-safe visibility of the factory instance across threads.
	 * </p>
	 */
	private static volatile IArtefactFactory artefactFactory = null;

	/**
	 * The expression factory used to create GAML expressions.
	 *
	 * <p>
	 * Volatile to ensure thread-safe visibility of the factory instance across threads. Public to allow direct access
	 * from other GAML infrastructure components.
	 * </p>
	 */
	public static volatile IExpressionFactory expressionFactory = null;

	/**
	 * The expression description factory used to create expression descriptions.
	 *
	 * <p>
	 * Volatile to ensure thread-safe visibility of the factory instance across threads. Public to allow direct access
	 * from other GAML infrastructure components.
	 * </p>
	 */
	public static volatile IExpressionDescriptionFactory expressionDescriptionFactory = null;

	/**
	 * The GAML content provider that retrieves syntactic elements from file URIs.
	 *
	 * <p>
	 * This function maps URIs to their corresponding syntactic element structures, enabling access to the parsed
	 * representation of GAML files.
	 * </p>
	 */
	private static Function<URI, ISyntacticElement> infoProvider = null;

	/**
	 * The GAML model builder used to build model descriptions from source.
	 *
	 * <p>
	 * Responsible for constructing complete model descriptions from GAML source code, including validation and
	 * cross-referencing.
	 * </p>
	 */
	private static IGamlModelBuilder gamlModelBuilder = null;

	/**
	 * The GAML text validator used to validate GAML code strings.
	 *
	 * <p>
	 * Provides validation services for models, species, statements, and expressions supplied as raw strings.
	 * </p>
	 */
	private static IGamlTextValidator gamlTextValidator = null;

	// ==================================================================================
	// §1 — Factory registration: methods that install the platform factories during startup.
	// ==================================================================================

	/**
	 * Registers the expression factory used to create GAML expressions.
	 *
	 * <p>
	 * The expression factory is responsible for parsing and creating expression instances from GAML source code. This
	 * method should be called once during platform initialization.
	 * </p>
	 *
	 * @param factory
	 *            the expression factory to register, must not be null
	 */
	public static void registerExpressionFactory(final IExpressionFactory factory) {
		expressionFactory = factory;
	}

	/**
	 * Registers the expression description factory used to create expression descriptions.
	 *
	 * <p>
	 * The expression description factory creates descriptive metadata for GAML expressions. This method should be
	 * called once during platform initialization.
	 * </p>
	 *
	 * @param factory
	 *            the expression description factory to register, must not be null
	 */
	public static void registerExpressionDescriptionFactory(final IExpressionDescriptionFactory factory) {
		expressionDescriptionFactory = factory;
	}

	/**
	 * Registers the artefact proto factory used to create artefact prototypes.
	 *
	 * <p>
	 * The artefact proto factory creates prototypes for operators, actions, variables, and other GAML language
	 * elements. This method should be called once during platform initialization.
	 * </p>
	 *
	 * @param factory
	 *            the artefact proto factory to register, must not be null
	 */
	public static void registerArtefactProtoFactory(final IArtefactFactory factory) {
		artefactFactory = factory;
	}

	/**
	 * Registers the description factory used to create GAML descriptions.
	 *
	 * <p>
	 * The description factory creates description instances for GAML language constructs. This method should be called
	 * once during platform initialization.
	 * </p>
	 *
	 * @param factory
	 *            the description factory to register, must not be null
	 */
	public static void registerDescriptionFactory(final IDescriptionFactory factory) {
		descriptionFactory = factory;
	}

	/**
	 * Registers a symbol description factory for one or more symbol kinds.
	 *
	 * <p>
	 * Symbol factories are responsible for creating descriptions of GAML symbols (statements, species, etc.). Each
	 * factory can handle multiple kinds of symbols, specified by integer handles. This method registers the factory for
	 * all its supported kinds.
	 * </p>
	 *
	 * @param factory
	 *            the symbol description factory to register, must not be null
	 */
	public static void registerSymbolFactory(final ISymbolDescriptionFactory factory) {
		ISymbolKind[] handles = factory.getKinds();
		for (ISymbolKind kind : handles) { DESCRIPTION_FACTORIES.put(kind, factory); }
	}

	/**
	 * Registers the GAML content provider function.
	 *
	 * <p>
	 * The content provider function maps file URIs to their parsed syntactic element structures, enabling access to the
	 * abstract syntax tree of GAML files.
	 * </p>
	 *
	 * @param info
	 *            the function that maps URIs to syntactic elements, must not be null
	 */
	public static void registerGamlContentProvider(final Function<URI, ISyntacticElement> info) {
		infoProvider = info;
	}

	/**
	 * Registers the GAML model builder used to build model descriptions.
	 *
	 * <p>
	 * The model builder is responsible for constructing complete model descriptions from GAML source code, including
	 * validation and cross-referencing.
	 * </p>
	 *
	 * @param builder
	 *            the GAML model builder to register, must not be null
	 */
	public static void registerGamlModelBuilder(final IGamlModelBuilder builder) {
		gamlModelBuilder = builder;
	}

	/**
	 * Registers the GAML text validator used to validate raw GAML code strings.
	 *
	 * <p>
	 * The text validator provides syntax and semantic validation for models, species, statements, and expressions
	 * supplied as plain strings. This method should be called once during platform initialization.
	 * </p>
	 *
	 * @param validator
	 *            the GAML text validator to register, must not be null
	 */
	public static void registerGamlTextValidator(final IGamlTextValidator validator) {
		gamlTextValidator = validator;
	}

	// ==================================================================================
	// §2 — Factory access: getters for the registered factories and the constant/unit acceptor.
	// ==================================================================================

	/**
	 * Returns the description factory used to create GAML descriptions.
	 *
	 * @return the registered description factory, or null if not yet registered
	 */
	public static IDescriptionFactory getDescriptionFactory() { return descriptionFactory; }

	/**
	 * Returns the expression factory used to create GAML expressions.
	 *
	 * @return the registered expression factory, or null if not yet registered
	 */
	public static IExpressionFactory getExpressionFactory() { return expressionFactory; }

	/**
	 * Returns the expression description factory used to create expression descriptions.
	 *
	 * @return the registered expression description factory, or null if not yet registered
	 */
	public static IExpressionDescriptionFactory getExpressionDescriptionFactory() {
		return expressionDescriptionFactory;
	}

	/**
	 * Returns the artefact factory used to create artefacts.
	 *
	 * @return the registered artefact factory, or null if not yet registered
	 */
	public static IArtefactFactory getArtefactFactory() { return artefactFactory; }

	/**
	 * Returns the GAML model builder used to build model descriptions.
	 *
	 * @return the registered model builder, or null if not yet registered
	 */
	public static IGamlModelBuilder getModelBuilder() { return gamlModelBuilder; }

	/**
	 * Returns the constant acceptor used to register constants and units in GAML.
	 *
	 * <p>
	 * The returned {@link IConstantAcceptor} writes entries into the {@code UNITS} registry. It first checks that the
	 * name is not already taken, derives the GAML type from the value, creates a unit expression via the expression
	 * factory, and registers both the canonical name and any provided aliases.
	 * </p>
	 *
	 * @return an {@link IConstantAcceptor} implementation backed by the {@code UNITS} registry
	 */
	public static IConstantAcceptor getConstantAcceptor() {
		return (name, value, doc, deprec, isTime, names) -> {
			if (UNITS.containsKey(name)) return false;
			// DEBUG.LOG("Added constant " + name);
			final IType t = GamaType.of(value);
			final IExpression.Unit exp = getExpressionFactory().createUnit(value, t, name, doc, deprec, isTime, names);
			UNITS.put(name, exp);
			if (names != null) { for (final String s : names) { UNITS.put(s, exp); } }
			return true;
		};
	}

	// ==================================================================================
	// §3 — Expression compilation & evaluation: compiling and running GAML expressions at runtime.
	// ==================================================================================

	/**
	 * Evaluates a GAML expression in the context of the given agent.
	 *
	 * <p>
	 * This method compiles and evaluates the provided expression string within the agent's context. It creates a
	 * temporary scope for evaluation and releases it after completion.
	 * </p>
	 *
	 * @param expression
	 *            the GAML expression string to evaluate, must not be null or empty
	 * @param a
	 *            the agent providing the execution context, must not be null
	 * @return the result of evaluating the expression, or null if the agent is null or compilation fails
	 * @throws GamaRuntimeException
	 *             if the expression is invalid or evaluation fails
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
	 * Compiles a GAML expression string in the context of the given agent.
	 *
	 * <p>
	 * Derives the execution context from the agent's current scope. When {@code onlyExpression} is {@code false} and
	 * the string cannot be parsed as an expression, compilation is retried as a temporary action statement.
	 * </p>
	 *
	 * @param expression
	 *            the GAML source string to compile, must not be null
	 * @param agent
	 *            the agent providing the description context, must not be null
	 * @param onlyExpression
	 *            if {@code true}, only pure expressions are accepted; if {@code false}, statement blocks are also tried
	 * @return the compiled {@link IExpression}, never null on success
	 * @throws GamaRuntimeException
	 *             if the agent is null or the string cannot be compiled
	 */
	public static IExpression compileExpression(final String expression, final IAgent agent,
			final boolean onlyExpression) throws GamaRuntimeException {
		if (agent == null) throw GamaRuntimeException.error("Agent is nil", GAMA.getRuntimeScope());
		final IExecutionContext tempContext = agent.getScope().getExecutionContext();
		return compileExpression(expression, agent, tempContext, onlyExpression);
	}

	/**
	 * Compiles a GAML expression string in the context of the given agent and execution context.
	 *
	 * <p>
	 * Uses the supplied {@link IExecutionContext} rather than deriving one from the agent's scope, which is useful when
	 * local variable bindings must be preserved across calls. When {@code onlyExpression} is {@code false} and the
	 * string fails as an expression, compilation is retried as a temporary action statement. Both error messages are
	 * combined if the retry also fails.
	 * </p>
	 *
	 * @param expression
	 *            the GAML source string to compile, must not be null
	 * @param agent
	 *            the agent providing the description context, must not be null
	 * @param tempContext
	 *            the execution context providing local variable bindings, must not be null
	 * @param onlyExpression
	 *            if {@code true}, only pure expressions are accepted; if {@code false}, statement blocks are also tried
	 * @return the compiled {@link IExpression}, never null on success
	 * @throws GamaRuntimeException
	 *             if the agent is null or the string cannot be compiled as either an expression or a statement
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

	// ==================================================================================
	// §4 — Runtime contexts: retrieving the active model and experiment descriptions at runtime.
	// ==================================================================================

	/**
	 * Returns the description of the model currently loaded in the running experiment.
	 *
	 * <p>
	 * Retrieves the description by querying the currently active experiment from {@link GAMA}. Returns {@code null}
	 * when no experiment is running.
	 * </p>
	 *
	 * @return the {@link IModelDescription} of the current model, or {@code null} if no experiment is active
	 */
	public static IModelDescription getModelContext() {
		IExperimentSpecies experiment = GAMA.getExperiment();
		if (experiment == null) return null;
		return experiment.getModel().getDescription();
	}

	/**
	 * Returns the experiment description associated with the given agent's scope.
	 *
	 * <p>
	 * Walks up from the agent's scope to the top-level experiment agent, then returns its species description cast as
	 * an {@link IExperimentDescription}. Returns {@code null} if the agent or its enclosing experiment is {@code null}.
	 * </p>
	 *
	 * @param agent
	 *            the agent whose enclosing experiment is sought, may be null
	 * @return the {@link IExperimentDescription} of the enclosing experiment, or {@code null} if unavailable
	 */
	public static IExperimentDescription getExperimentContext(final IAgent agent) {
		if (agent == null) return null;
		final IScope scope = agent.getScope();
		final ITopLevelAgent topAgent = scope.getExperiment();
		if (topAgent == null) return null;
		return (IExperimentDescription) topAgent.getSpecies().getDescription();
	}

	// ==================================================================================
	// §5 — Operators: registering, querying and classifying GAML operators.
	// — Iterators: registering and querying GAML iterator operators.
	// ==================================================================================

	/**
	 * Registers an operator prototype in the {@code OPERATORS} registry.
	 *
	 * <p>
	 * Associates the operator with its signature so it can be resolved during expression compilation. If a prototype
	 * with the same signature already exists under the same name it is silently replaced.
	 * </p>
	 *
	 * @param proto
	 *            the operator prototype to register, must not be null
	 */
	public static void registerOperator(final Operator proto) {
		getOperatorsRegistryFor(proto.getName()).put(proto.getSignature(), proto);
	}

	/**
	 * Returns {@code true} if the given signature has not yet been registered for the given operator keyword.
	 *
	 * <p>
	 * Used during platform initialization to avoid silently overwriting an existing operator overload.
	 * </p>
	 *
	 * @param kw
	 *            the operator keyword to check
	 * @param signature
	 *            the signature to check for registration
	 * @return {@code true} if the signature is not yet registered, {@code false} if it is already present
	 */
	public static boolean canRegisterOperator(final String kw, final Signature signature) {
		return !getOperatorsRegistryFor(kw).containsKey(signature);
	}

	/**
	 * Returns {@code true} if any signature registered under the given name is unary.
	 *
	 * @param name
	 *            the operator name to check
	 * @return {@code true} if at least one registered signature for {@code name} is unary, {@code false} otherwise
	 */
	public static boolean isUnaryOperator(final String name) {
		final Map<Signature, IArtefact.Operator> map = OPERATORS.get(name);
		if (map == null) return false;
		for (final Signature s : map.keySet()) { if (s.isUnary()) return true; }
		return false;
	}

	/**
	 * Returns {@code true} if an operator with the given name is registered.
	 *
	 * @param op
	 *            the operator name to check
	 * @return {@code true} if the operator exists, {@code false} otherwise
	 */
	public static boolean containsOperatorNamed(final String op) {
		return OPERATORS.containsKey(op);
	}

	/**
	 * Returns the names of all registered operators.
	 *
	 * @return an unmodifiable view of all operator names currently in the registry
	 */
	public static Collection<String> getOperatorsNames() { return OPERATORS.keySet(); }

	/**
	 * Returns all operator implementations registered under the given name.
	 *
	 * @param name
	 *            the operator name whose overloads are sought
	 * @return a map of {@link Signature} to {@link Operator} implementations, or {@code null} if no operator with that
	 *         name exists
	 */
	public static Map<Signature, Operator> getOperatorsNamed(final String name) {
		return OPERATORS.get(name);
	}

	/**
	 * Returns (and lazily creates) the signature-to-operator map for the given keyword.
	 *
	 * <p>
	 * This internal helper is used by both {@link #registerOperator(Operator)} and
	 * {@link #canRegisterOperator(String, Signature)}. A new empty {@link HashMap} is inserted on first access for a
	 * given keyword.
	 * </p>
	 *
	 * @param kw
	 *            the operator keyword whose registry entry is sought
	 * @return the mutable map of signatures to operators for {@code kw}, never null
	 */
	private static Map<Signature, Operator> getOperatorsRegistryFor(final String kw) {
		Map<Signature, Operator> registry = OPERATORS.get(kw);
		if (registry == null) {
			registry = new HashMap<>();
			OPERATORS.put(kw, registry);
		}
		return registry;
	}

	/**
	 * Registers one or more iterator operator names in the {@code ITERATORS} set.
	 *
	 * <p>
	 * Iterators are special operators that iterate over collections (e.g., {@code collect}, {@code where},
	 * {@code count}). Registering a name here allows the compiler and validator to treat it accordingly.
	 * </p>
	 *
	 * @param iterators
	 *            the iterator names to register; duplicates are silently ignored
	 */
	public static void addIterators(final String... iterators) {
		Collections.addAll(ITERATORS, iterators);
	}

	/**
	 * Returns {@code true} if the given name is a registered GAML iterator.
	 *
	 * @param name
	 *            the operator name to check
	 * @return {@code true} if {@code name} is an iterator, {@code false} otherwise
	 */
	public static boolean isIterator(final String name) {
		return ITERATORS.contains(name);
	}

	// ==================================================================================
	// §6 - Statements & declarations: querying the status of keywords
	// ==================================================================================

	/**
	 * Checks if is a declaration.
	 *
	 * @param name
	 *            the name
	 * @return true, if is a declaration
	 */
	public static boolean isADeclaration(final String name) {
		return !isAStatement(name);
	}

	/**
	 * Checks if is a statement.
	 *
	 * @param name
	 *            the name
	 * @return true, if is a statement
	 */
	public static boolean isAStatement(final String name) {
		return ArtefactRegistry.isStatementArtefact(name);
	}

	// ==================================================================================
	// §7 — Constants & units: registering and querying named constants and measurement units.
	// ==================================================================================

	/**
	 * Registers one or more constant names in the {@code CONSTANTS} set.
	 *
	 * <p>
	 * Constants are predefined named values available in GAML expressions (e.g., {@code true}, {@code false},
	 * {@code #pi}). Registering a name here allows the compiler to recognise it as a constant reference rather than a
	 * variable. Duplicates are silently ignored.
	 * </p>
	 *
	 * @param constants
	 *            the constant names to register
	 */
	public static void addConstants(final String... constants) {
		Collections.addAll(CONSTANTS, constants);
	}

	/**
	 * Returns an unmodifiable view of all registered constant names.
	 *
	 * @return an unmodifiable collection of constant names; never null
	 */
	public static Collection<String> getConstants() { return Collections.unmodifiableCollection(CONSTANTS); }

	/**
	 * Returns an unmodifiable view of the entire units registry, mapping each unit name to its expression.
	 *
	 * @return an unmodifiable map of unit names to {@link IExpression.Unit} instances; never null
	 */
	public static Map<String, IExpression.Unit> getUnits() { return Collections.unmodifiableMap(UNITS); }

	/**
	 * Returns the unit expression registered under the given name.
	 *
	 * @param name
	 *            the unit name to look up (e.g., {@code "m"}, {@code "km"}, {@code "s"})
	 * @return the corresponding {@link IExpression.Unit}, or {@code null} if no such unit is registered
	 */
	public static IExpression.Unit getUnit(final String name) {
		return UNITS.get(name);
	}

	// ==================================================================================
	// §8 — Agent class additions & fields: registering and querying descriptions and field
	// prototypes that extend agent classes at runtime.
	// ==================================================================================

	/**
	 * Adds a description to the {@code ADDITIONS} multimap for the given agent class in a thread-safe manner.
	 *
	 * <p>
	 * Creates the backing set on first access via {@code computeIfAbsent}.
	 * </p>
	 *
	 * @param key
	 *            the agent class to which the description is added
	 * @param value
	 *            the description to add
	 * @return {@code true} if the description was added, {@code false} if it was already present
	 */
	public static boolean addAddition(final Class key, final IDescription value) {
		return ADDITIONS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
	}

	/**
	 * Adds a field prototype to the {@code FIELDS} multimap for the given agent class in a thread-safe manner.
	 *
	 * <p>
	 * Creates the backing set on first access via {@code computeIfAbsent}.
	 * </p>
	 *
	 * @param key
	 *            the agent class to which the field prototype is added
	 * @param value
	 *            the artefact prototype representing the field to add
	 * @return {@code true} if the field was added, {@code false} if it was already present
	 */
	public static boolean addField(final Class key, final IArtefact value) {
		return FIELDS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
	}

	/**
	 * Returns all description additions registered for the given agent class.
	 *
	 * @param key
	 *            the agent class whose additions are sought
	 * @return an unmodifiable set of descriptions; never null, empty if none are registered
	 */
	public static Set<IDescription> getAdditions(final Class key) {
		return ADDITIONS.getOrDefault(key, Collections.emptySet());
	}

	/**
	 * Returns all field prototypes registered for the given agent class.
	 *
	 * @param key
	 *            the agent class whose field prototypes are sought
	 * @return an unmodifiable set of artefact prototypes; never null, empty if none are registered
	 */
	public static Set<IArtefact> getFields(final Class key) {
		return FIELDS.getOrDefault(key, Collections.emptySet());
	}

	/**
	 * Returns the set of agent classes that have at least one registered addition.
	 *
	 * @return an unmodifiable view of the {@code ADDITIONS} key set; never null
	 */
	public static Set<Class> getAdditionClasses() { return Collections.unmodifiableSet(ADDITIONS.keySet()); }

	/**
	 * Returns a flat collection of all field prototypes registered across all agent classes.
	 *
	 * @return a set containing every registered {@link IArtefact} from all entries in {@code FIELDS}; never null
	 */
	public static Collection<IArtefact> getFields() {
		return FIELDS.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	}

	// ==================================================================================
	// §9 — Artefact prototypes & skills: querying the ArtefactRegistry and the
	// inherited field hierarchy for a given class or skill.
	// ==================================================================================

	/**
	 * Returns all field prototypes for the given class, traversing its full implementation hierarchy.
	 *
	 * <p>
	 * Collects field prototypes from the class itself and all its superclasses and implemented interfaces that appear
	 * in the {@code FIELDS} registry. When the same field name appears at multiple levels, the most-derived definition
	 * takes precedence ({@code putIfAbsent} ordering). The map is pre-sized to minimise rehashing.
	 * </p>
	 *
	 * @param clazz
	 *            the class whose full field hierarchy is to be collected
	 * @return a map of field names to their most-derived {@link IArtefact}; never null, may be empty
	 */
	public static Map<String, IArtefact> getAllFields(final Class clazz) {
		final List<Class> classes = collectImplementationClasses(clazz, Collections.EMPTY_SET, FIELDS.keySet());
		// Pre-size the map to avoid rehashing
		final Map<String, IArtefact> fieldsMap = new HashMap<>(classes.size() * 4);
		for (final Class c : classes) {
			final Set<IArtefact> fields = FIELDS.get(c);
			if (fields != null) { for (final IArtefact desc : fields) { fieldsMap.putIfAbsent(desc.getName(), desc); } }
		}
		return fieldsMap;
	}

	/**
	 * Returns all statement prototypes that are designated to be defined within the given skill.
	 *
	 * <p>
	 * Iterates over all registered statement prototype names in the {@link ArtefactRegistry} and retains those whose
	 * {@code shouldBeDefinedIn} predicate returns {@code true} for the given skill name. Insertion order is preserved
	 * via a {@link LinkedHashSet}.
	 * </p>
	 *
	 * @param skill
	 *            the skill name for which matching statement prototypes are sought
	 * @return an ordered collection of matching {@link IArtefact.Symbol} instances; never null, may be empty
	 */
	public static Collection<IArtefact.Symbol> getStatementsForSkill(final String skill) {
		final Set<IArtefact.Symbol> result = new LinkedHashSet<>();
		for (final String p : ArtefactRegistry.getStatementArtefactNames()) {
			final IArtefact.Symbol proto = ArtefactRegistry.getStatementArtefact(p);
			if (proto != null && proto.shouldBeDefinedIn(skill)) { result.add(proto); }
		}
		return result;
	}

	// ==================================================================================
	// §10 — File & content access: retrieving metadata and parsed content for GAML files.
	// ==================================================================================

	/**
	 * Returns the GAML file metadata for the given URI.
	 *
	 * <p>
	 * Delegates to the platform's metadata provider, requesting a fresh parse if necessary. The returned object
	 * contains information about the imports, experiments, and other top-level constructs of the file.
	 * </p>
	 *
	 * @param uri
	 *            the URI of the GAML file whose metadata is sought
	 * @return the {@link IGamlFileInfo} for the file, or {@code null} if the metadata cannot be retrieved
	 */
	public static IGamlFileInfo getInfo(final URI uri) {
		return (IGamlFileInfo) GAMA.getMetadataProvider().getMetaData(uri, true, true);
	}

	/**
	 * Gets the info.
	 *
	 * @param uri
	 *            the uri
	 * @return the info
	 */
	public static IGamlFileInfo getInfo(final File file) {
		return (IGamlFileInfo) GAMA.getMetadataProvider().getMetaData(file, true, true);
	}

	/**
	 * Returns the parsed syntactic element tree for the GAML file at the given URI.
	 *
	 * <p>
	 * Delegates to the registered content provider function. The returned element is the root of the abstract syntax
	 * tree produced by the GAML parser for the specified file.
	 * </p>
	 *
	 * @param uri
	 *            the URI of the GAML file to parse
	 * @return the root {@link ISyntacticElement} of the parsed file
	 */
	public static ISyntacticElement getContents(final URI uri) {
		return infoProvider.apply(uri);
	}

	// ==================================================================================
	// §11 — Validation: validating raw GAML code strings for syntax and semantic correctness.
	// ==================================================================================

	/**
	 * Validates a GAML code string, automatically detecting the kind of construct it represents.
	 *
	 * <p>
	 * The detection heuristic works as follows:
	 * <ul>
	 * <li>If the string starts with the {@code model} keyword, it is validated as a full model.</li>
	 * <li>If it starts with {@code species} or {@code grid}, it is validated as a species block.</li>
	 * <li>If it spans more than one line, it is validated as a sequence of statements.</li>
	 * <li>Otherwise it is validated as a single expression.</li>
	 * </ul>
	 * </p>
	 *
	 * @param entered
	 *            the raw GAML code string to validate, must not be null
	 * @param syntaxOnly
	 *            if {@code true}, only syntactic validation is performed; if {@code false}, semantic validation is also
	 *            applied
	 * @return the list of {@link GamlCompilationError} instances found; empty if the code is valid
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
	 * Returns the start line and character offset of the given EObject within its containing GAML file.
	 *
	 * <p>
	 * Delegates to the text validator to extract positional information from the node model. The result is used, for
	 * example, to map a validation error back to a specific location in the source file.
	 * </p>
	 *
	 * @param source
	 *            the EObject whose source location is sought, must not be null
	 * @return a two-element {@code int[]} where {@code [0]} is the 1-based start line and {@code [1]} is the 0-based
	 *         character offset within that line
	 */
	public static int[] getLocationInFileInfo(final EObject source) {
		return gamlTextValidator.getStartLineAndOffsetInFileInfo(source);
	}

}
