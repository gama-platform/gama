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
import gama.api.compilation.prototypes.IArtefactProto.Operator;
import gama.api.compilation.prototypes.IArtefactProtoFactory;
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
	private static final Map<String, Map<Signature, IArtefactProto.Operator>> OPERATORS = new ConcurrentHashMap<>();

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
	 * Structure: {@code Map<Class, Set<IArtefactProto>>}
	 * </p>
	 */
	private final static Map<Class, Set<IArtefactProto>> FIELDS = new ConcurrentHashMap<>();

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
	private static volatile IArtefactProtoFactory artefactProtoFactory = null;

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

	/** The gaml model builer. */
	private static IGamlModelBuilder gamlModelBuilder = null;

	/** The gaml text validator. */
	private static IGamlTextValidator gamlTextValidator = null;

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
	public static void registerArtefactProtoFactory(final IArtefactProtoFactory factory) {
		artefactProtoFactory = factory;
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
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param builder
	 *            the GAML model builder to register, must not be null
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
	 * Gets the constant acceptor for registering constants and units in GAML.
	 *
	 * <p>
	 * Returns an implementation of IConstantAcceptor that registers constants and units into the UNITS registry. The
	 * acceptor validates that names don't conflict with existing units and creates appropriate unit expressions.
	 * </p>
	 *
	 * @return the constant acceptor implementation
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
	 * Gets the all fields for a class, including inherited fields from superclasses and interfaces.
	 *
	 * <p>
	 * This method efficiently collects all field prototypes associated with a class and its hierarchy, avoiding
	 * duplicate lookups and minimizing object allocations.
	 * </p>
	 *
	 * @param clazz
	 *            the class to get fields for
	 * @return a map of field names to their prototypes
	 */
	public static Map<String, IArtefactProto> getAllFields(final Class clazz) {
		final List<Class> classes = collectImplementationClasses(clazz, Collections.EMPTY_SET, FIELDS.keySet());
		// Pre-size the map to avoid rehashing
		final Map<String, IArtefactProto> fieldsMap = new HashMap<>(classes.size() * 4);
		for (final Class c : classes) {
			final Set<IArtefactProto> fields = FIELDS.get(c);
			if (fields != null) {
				for (final IArtefactProto desc : fields) { fieldsMap.putIfAbsent(desc.getName(), desc); }
			}
		}
		return fieldsMap;
	}

	/**
	 * Gets the statement prototypes that should be defined in a given skill.
	 *
	 * <p>
	 * This method collects all statement prototypes that are designated to be defined within the specified skill
	 * context.
	 * </p>
	 *
	 * @param s
	 *            the skill name to get statements for
	 * @return a collection of statement prototypes for the skill
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
	 * Checks if the given operator name corresponds to a unary operator.
	 *
	 * @param name
	 *            the operator name to check
	 * @return true if the operator has at least one unary signature, false otherwise
	 */
	public static boolean isUnaryOperator(final String name) {
		final Map<Signature, IArtefactProto.Operator> map = OPERATORS.get(name);
		if (map == null) return false;
		for (final Signature s : map.keySet()) { if (s.isUnary()) return true; }
		return false;
	}

	/**
	 * Validates GAML code based on a best guess regarding the contents provided.
	 *
	 * <p>
	 * This method automatically determines the type of GAML code (model, species, statements, or expression) and
	 * applies the appropriate validation strategy.
	 * </p>
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param entered
	 *            the GAML code string to validate
	 * @param syntaxOnly
	 *            if true, only syntax validation is performed; if false, semantic validation is also done
	 * @return the list of compilation errors found, empty if no errors
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

	// ==================================================================================
	// Utility methods for working with concurrent multimap-like structures
	// ==================================================================================

	/**
	 * Adds a value to the ADDITIONS multimap in a thread-safe manner. Creates the set if it doesn't exist using
	 * computeIfAbsent.
	 *
	 * @param key
	 *            the class key
	 * @param value
	 *            the description to add
	 * @return true if the value was added, false if it was already present
	 */
	public static boolean addAddition(final Class key, final IDescription value) {
		return ADDITIONS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
	}

	/**
	 * Adds a field to the FIELDS multimap in a thread-safe manner. Creates the set if it doesn't exist using
	 * computeIfAbsent.
	 *
	 * @param key
	 *            the class key
	 * @param value
	 *            the artefact proto to add
	 * @return true if the field was added, false if it was already present
	 */
	public static boolean addField(final Class key, final IArtefactProto value) {
		return FIELDS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
	}

	/**
	 * Gets all additions for a class. Returns an empty set if none exist.
	 *
	 * @param key
	 *            the class key
	 * @return the set of descriptions, never null
	 */
	public static Set<IDescription> getAdditions(final Class key) {
		return ADDITIONS.getOrDefault(key, Collections.emptySet());
	}

	/**
	 * Gets all fields for a class. Returns an empty set if none exist.
	 *
	 * @param key
	 *            the class key
	 * @return the set of artefact protos, never null
	 */
	public static Set<IArtefactProto> getFields(final Class key) {
		return FIELDS.getOrDefault(key, Collections.emptySet());
	}

	/**
	 * Adds the iterators.
	 *
	 * @param iterators
	 *            the iterators
	 */
	public static void addIterators(final String... iterators) {
		Collections.addAll(ITERATORS, iterators);
	}

	/**
	 * Adds the constants.
	 *
	 * @param constants
	 *            the constants
	 */
	public static void addConstants(final String... constants) {
		Collections.addAll(CONSTANTS, constants);
	}

	/**
	 * Gets the addition classes.
	 *
	 * @return the addition classes
	 */
	public static Set<Class> getAdditionClasses() { return Collections.unmodifiableSet(ADDITIONS.keySet()); }

	/**
	 * Gets the constants.
	 *
	 * @return the constants
	 */
	public static Collection<String> getConstants() { return Collections.unmodifiableCollection(CONSTANTS); }

	/**
	 * Gets the fields.
	 *
	 * @return the fields
	 */
	public static Collection<IArtefactProto> getFields() {
		return FIELDS.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
	}

	/**
	 * Gets the units.
	 *
	 * @return the units
	 */
	public static Map<String, IExpression.Unit> getUnits() { return Collections.unmodifiableMap(UNITS); }

	/**
	 * Gets the unit.
	 *
	 * @param name
	 *            the name
	 * @return the unit
	 */
	public static IExpression.Unit getUnit(final String name) {
		return UNITS.get(name);
	}

	/**
	 * Checks if the given operator name corresponds to an iterator.
	 *
	 * @param name
	 *            the operator name to check
	 * @return true if the operator is an iterator, false otherwise
	 */
	public static boolean isIterator(final String name) {
		return ITERATORS.contains(name);
	}

	/**
	 * Returns false if the signature is already registered for this keyword
	 *
	 * @param kw
	 *            the operator keyword to check
	 * @param signature
	 *            the signature to check for registration
	 * @return true if the operator can be registered, false if already registered
	 */
	public static boolean canRegisterOperator(final String kw, final Signature signature) {
		return !getOperatorsRegistryFor(kw).containsKey(signature);
	}

	/**
	 * Gets the operators registry for.
	 *
	 * @param kw
	 *            the kw
	 * @return the operators registry for
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
	 * Registers an operator prototype in the OPERATORS registry.
	 *
	 * <p>
	 * Associates the operator with its signature for later retrieval and compilation.
	 * </p>
	 *
	 * @param proto
	 *            the operator prototype to register
	 */
	public static void registerOperator(final Operator proto) {
		getOperatorsRegistryFor(proto.getName()).put(proto.getSignature(), proto);
	}

	/**
	 * Gets the names of all registered operators.
	 *
	 * @return collection of all operator names
	 */
	public static Collection<String> getOperatorsNames() { return OPERATORS.keySet(); }

	/**
	 * Gets all operator implementations for a given operator name.
	 *
	 * @param name
	 *            the operator name
	 * @return map of signatures to operator implementations, or null if operator doesn't exist
	 */
	public static Map<Signature, Operator> getOperatorsNamed(final String name) {
		return OPERATORS.get(name);
	}

	/**
	 * Checks if an operator with the given name is registered.
	 *
	 * @param op
	 *            the operator name to check
	 * @return true if the operator exists, false otherwise
	 */
	public static boolean containsOperatorNamed(final String op) {
		return OPERATORS.containsKey(op);
	}

}
