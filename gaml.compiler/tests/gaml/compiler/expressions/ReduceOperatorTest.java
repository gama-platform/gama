package gaml.compiler.expressions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.osgi.launch.EquinoxFactory;
import org.eclipse.xtext.parser.IParser;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.types.Types;
import gama.api.kernel.GamaMetaModel;
import gama.api.runtime.IWorkspaceManager;
import gama.api.runtime.scope.ExecutionScope;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.gaml.operators.Containers;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.descriptions.DescriptionFactory;
import gaml.compiler.factories.ClassFactory;
import gaml.compiler.factories.ExperimentFactory;
import gaml.compiler.factories.ExpressionDescriptionFactory;
import gaml.compiler.factories.ModelFactory;
import gaml.compiler.factories.PlatformFactory;
import gaml.compiler.factories.SkillFactory;
import gaml.compiler.factories.SpeciesFactory;
import gaml.compiler.factories.StatementFactory;
import gaml.compiler.factories.VariableFactory;
import gaml.compiler.prototypes.ArtefactFactory;

/** Standalone language regression checks. Run with --add-modules jdk.incubator.vector and the compiler/core classpath. */
public final class ReduceOperatorTest {

	private static int checks;

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
		checks++;
	}

	private static void bootstrap() throws Exception {
		final var workspace = Proxy.newProxyInstance(org.eclipse.core.resources.IWorkspace.class.getClassLoader(),
				new Class<?>[] { org.eclipse.core.resources.IWorkspace.class }, (p, m, a) -> {
					if (m.getName().equals("addResourceChangeListener")) return null;
					throw new UnsupportedOperationException(m.getName());
				});
		GAMA.setWorkspaceManager((IWorkspaceManager) Proxy.newProxyInstance(IWorkspaceManager.class.getClassLoader(),
				new Class<?>[] { IWorkspaceManager.class }, (p, m, a) -> {
					if (m.getName().equals("getWorkspace")) return workspace;
					throw new UnsupportedOperationException(m.getName());
				}));
		GAML.registerArtefactProtoFactory(ArtefactFactory.getInstance());
		GAML.registerDescriptionFactory(DescriptionFactory.getInstance());
		GAML.registerSymbolFactory(ExperimentFactory.getInstance());
		GAML.registerSymbolFactory(ModelFactory.getInstance());
		GAML.registerSymbolFactory(PlatformFactory.getInstance());
		GAML.registerSymbolFactory(SpeciesFactory.getInstance());
		GAML.registerSymbolFactory(StatementFactory.getInstance());
		GAML.registerSymbolFactory(VariableFactory.getInstance());
		GAML.registerSymbolFactory(SkillFactory.getInstance());
		GAML.registerSymbolFactory(ClassFactory.getInstance());
		GAML.registerExpressionFactory(GamlExpressionFactory.getInstance());
		GAML.registerExpressionDescriptionFactory(ExpressionDescriptionFactory.getInstance());
		gama.api.types.geometry.GamaShapeFactory.setBuilder(new gama.core.geometry.InternalGamaShapeFactory());
		gama.api.types.matrix.GamaMatrixFactory.setBuilder(new gama.core.util.matrix.InternalGamaMatrixFactory());
		GamaBundleLoader.CURRENT_PLUGIN_NAME = "gama.api";
		new gaml.additions.api.GamlAdditions().initialize();
		GamaBundleLoader.CURRENT_PLUGIN_NAME = "gama.core";
		new gaml.additions.core.GamlAdditions().initialize();
		GamaMetaModel.build();
		Types.init();
	}

	private static void expect(final String source, final Object expected, final IDescription context,
			final IScope scope) {
		final IExpression expression = GAML.getExpressionFactory().createExpr(source, context);
		check(expression != null, "Expression compiles: " + source);
		final Object actual = expression.value(scope);
		check(Objects.equals(expected, actual), "Result of " + source + " should be " + expected + ", got " + actual);
		final String serialized = expression.serializeToGaml(false);
		final IExpression restored = GAML.getExpressionFactory().createExpr(serialized, context);
		check(restored != null, "Serialized expression compiles: " + serialized);
		check(Objects.equals(expected, restored.value(scope)), "Serialization preserves the result: " + serialized);
		check(expression.getGamlType().equals(restored.getGamlType()), "Serialization preserves the type: " + source);
	}

	private static void reject(final String source, final String diagnostic, final IDescription context) {
		try {
			check(GAML.getExpressionFactory().createExpr(source, context) == null,
					"Invalid expression rejected: " + source);
		} catch (final GamaRuntimeException expected) {
			check(expected.getMessage().contains(diagnostic), "Explicit validation error: " + source);
		}
	}

	public static void main(final String[] args) {
		try {
			runWithPlatform();
			System.out.println("ReduceOperatorTest: " + checks + " checks passed");
		} catch (final Throwable failure) {
			failure.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

	private static void runWithPlatform() throws Exception {
		final var storage = Files.createTempDirectory("reduce-test-platform-");
		final var framework = new EquinoxFactory().newFramework(Map.of(
				"org.osgi.framework.storage", storage.toString(), "org.osgi.framework.bundle.parent", "app"));
		try {
			framework.start();
			for (final String name : new String[] { "gama.api", "gama.core", "gama.library" }) {
				final var manifest = new Manifest();
				final var attributes = manifest.getMainAttributes();
				attributes.putValue("Manifest-Version", "1.0");
				attributes.putValue("Bundle-ManifestVersion", "2");
				attributes.putValue("Bundle-SymbolicName", name);
				attributes.putValue("Bundle-Version", "1.0.0");
				final var bytes = new ByteArrayOutputStream();
				try (var jar = new JarOutputStream(bytes, manifest)) {}
				framework.getBundleContext().installBundle(name, new ByteArrayInputStream(bytes.toByteArray())).start();
			}
			InternalPlatform.getDefault().start(framework.getBundleContext());
			final var injector = GamlStandaloneSetup.doSetup();
			bootstrap();
			GamlStandaloneSetup.initializeAfterPlatformReady(injector);
			final var parsed = injector.getInstance(IParser.class).parse(new StringReader("""
					@no_warning
					model reduce_test
					global {
						int acc <- 7;
						int total <- [1,2,3] reduce (@ + each);
						list<int> totals <- [1,2,3] scan (0, @ + each);
					}
					experiment main type: gui {}
					"""));
			check(!parsed.hasSyntaxErrors(), "Pragmas and accumulator expressions coexist in a model");
			final var context = GamaMetaModel.getSpeciesDescription("model");
			try (final IScope scope = new ExecutionScope(null)) {
				runChecks(context, scope);
				runScanChecks(context, scope);
			}
		} finally {
			framework.stop();
			framework.waitForStop(10000);
			try (var paths = Files.walk(storage)) {
				for (final var path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
					Files.delete(path);
				}
			}
		}
	}

	private static void runChecks(final IDescription context, final IScope scope) {
		check(IArtefact.Operator.isAccumulator("reduce") && IArtefact.Operator.isAccumulator("scan"),
				"Reduce and scan are accumulator names");
		check(!IArtefact.Operator.isAccumulator("collect") && !IArtefact.Operator.isAccumulator(null),
				"Ordinary iterators and null are not accumulator names");
		for (final String source : new String[] { "[1,2] reduce (@ + each)", "[1,2] scan (0, @ + each)",
				"[1,2] collect (each + 1)" }) {
			final var expression = (IOperator) GAML.getExpressionFactory().createExpr(source, context);
			final var prototype = (IArtefact.Operator) expression.getPrototype();
			check(prototype.isIterator(), "Accumulators remain iterators: " + source);
			check(prototype.isAccumulator() == !source.contains("collect"),
					"Operator prototypes distinguish accumulators from ordinary iterators: " + source);
		}
		expect("[1,2,3,4] reduce (@ + each)", 10, context, scope);
		expect("[1,2,3,4] reduce (@ * each)", 24, context, scope);
		expect("[10,3,2] reduce (@ - each)", 5, context, scope);
		expect("[10,3,2] reduce (0, @ - each)", -15, context, scope);
		expect("[1,2,3] reduce (10, @ + each)", 16, context, scope);
		expect("[] reduce (0, @ + each)", 0, context, scope);
		expect("[7] reduce (@ + each)", 7, context, scope);
		expect("[nil] reduce (@)", null, context, scope);
		expect("[7] reduce (each div 0)", 7, context, scope);
		expect("[] reduce (0, each div 0)", 0, context, scope);
		expect("['a','b','c'] reduce (@ + ',' + each)", "a,b,c", context, scope);
		expect("['a','bb','ccc'] reduce (0, @ + length(each))", 6, context, scope);
		expect("['a'::1,'b'::2,'c'::3] reduce (@ + each)", 6, context, scope);
		expect("matrix([[1,2],[3,4]]) reduce (@ + each)", 10, context, scope);
		expect("reduce([1,2,3], @ + each)", 6, context, scope);
		expect("reduce([1,2,3], 10, @ + each)", 16, context, scope);
		expect("[1,2] reduce (0.0, @ + each)", 3d, context, scope);
		expect("[1,2] reduce (0.0, 1)", 1d, context, scope);
		expect("[] reduce (0.0, @ + each)", 0d, context, scope);
		expect("[1,2.0] reduce (@ + each)", 3d, context, scope);
		expect("[[1,2],[3,4]] reduce (0, @ + (each reduce (@ + each)))", 10, context, scope);
		expect("[[1,2],[3,4]] reduce (0, (each reduce (@, @ + each)) + @)", 13, context, scope);
		expect("[1,2] collect (x: [x,3] reduce (@ + each))", List.of(4, 5), context, scope);
		expect("[1,2] collect (acc: [acc,3] reduce (@ + each))", List.of(4, 5), context, scope);
		expect("[1,2] reduce (0, @ + sum([each,3] collect (each * 2)))", 18, context, scope);
		expect("[1,2] reduce (0, sum([each] collect (@ + each)))", 3, context, scope);
		expect("([1,2] reduce (@ + each)) + ([3,4] reduce (@ + each))", 10, context, scope);
		expect("[[1],[2,3]] reduce (@ + each)", List.of(1, 2, 3), context, scope);
		expect("[[1],[2,3]] reduce (list<int>([]), @ + each)", List.of(1, 2, 3), context, scope);
		expect("[1,2,3] reduce (list<int>([]), @ + each)", List.of(1, 2, 3), context, scope);

		check(GAML.getExpressionFactory().createExpr("[1,2] reduce (0.0, @ + each)", context)
				.getGamlType().equals(Types.FLOAT), "Seed determines result type");
		check(GAML.getExpressionFactory().createExpr("[[1],[2]] reduce (@ + each)", context)
				.getGamlType().equals(Types.LIST.of(Types.INT)), "Unseeded result retains content parameters");
		check(GAML.getExpressionFactory().createExpr("agent reduce (0, @ + 1)", context)
				.getGamlType().equals(Types.INT), "Species support seeded reduction");

		for (final String source : new String[] { "[1,2] reduce (@ + each)", "[1,2] reduce (0, @ + each)" }) {
			final var expression = (IOperator) GAML.getExpressionFactory().createExpr(source, context);
			final var prototype = (IArtefact.Operator) expression.getPrototype();
			check(prototype.getPattern(true).contains(" reduce (") && prototype.getPattern(true).contains("@ + each"),
					"Editor templates expose the user syntax rather than internal iterator arguments");
			check(!prototype.documentSignature().contains("string"), "Hover signature hides the iterator name");
		}

		reject("@", "'@' is only accessible", context);
		reject("[1,2] reduce (@, @ + each)", "'@' is only accessible", context);
		reject("[1,2] reduce (0, 'not an integer')", "body must return a value compatible with int", context);
		reject("1 reduce (0, @ + each)", "expects a container", context);
		reject("[1,2] reduce (0, @ + each, 1)", "expects a body", context);
		reject("reduce([1,2])", "expects a container, an optional initial value, and a body", context);
		reject("reduce([1,2], 0, @ + each, 1)", "expects a container, an optional initial value, and a body", context);

		scope.setEach("each", "outer element");
		scope.setEach("@", "outer accumulator");
		expect("[1,2,3] reduce (@ + each)", 6, context, scope);
		check(scope.getEach("each").equals("outer element") && scope.getEach("@").equals("outer accumulator"),
				"Successful reduction restores outer bindings");
		for (final String source : new String[] { "[] reduce (@ + each)", "[1,2] reduce (@ div 0)" }) {
			try {
				GAML.getExpressionFactory().createExpr(source, context).value(scope);
				throw new AssertionError("Expected runtime failure: " + source);
			} catch (final GamaRuntimeException expected) {
				check(scope.getEach("each").equals("outer element") && scope.getEach("@").equals("outer accumulator"),
						"Failed reduction restores outer bindings: " + source);
			}
		}

		final var input = GamaListFactory.wrap(Types.INT, 1, 2, 3);
		final var original = new ArrayList<>(input);
		final AtomicInteger calls = new AtomicInteger();
		final IExpression body = GAML.getExpressionFactory().createExpr(sc -> {
			calls.incrementAndGet();
			return (Integer) sc.getEach("@") + (Integer) sc.getEach("each");
		}, Types.INT);
		check(Containers.reduce(scope, "each", input, 10, body).equals(16), "Seeded runtime result");
		check(calls.get() == 3, "Seeded body evaluates once per element");
		calls.set(0);
		check(Containers.reduce(scope, "each", input, body).equals(6), "Unseeded runtime result");
		check(calls.get() == 2, "Unseeded body skips the first element");
		check(original.equals(input), "Reduction does not modify the source container");

		final AtomicInteger seedCalls = new AtomicInteger();
		final IExpression seed = GAML.getExpressionFactory().createExpr(sc -> {
			seedCalls.incrementAndGet();
			check(sc.getEach("@").equals("outer accumulator") && sc.getEach("each").equals("outer element"),
					"Seed evaluates outside the reduction bindings");
			return 10;
		}, Types.INT);
		final var template = (IOperator) GAML.getExpressionFactory().createExpr("[1,2,3] reduce (10, @ + each)", context);
		final IExpression seeded = GAML.getExpressionFactory().createOperator("reduce", context, null,
				template.arg(0), template.arg(1), seed, template.arg(3));
		check(seeded.value(scope).equals(16) && seedCalls.get() == 1, "Seed evaluates exactly once");
		final IExpression emptySeeded = GAML.getExpressionFactory().createOperator("reduce", context, null,
				template.arg(0), GAML.getExpressionFactory().createExpr("[]", context), seed, template.arg(3));
		check(emptySeeded.value(scope).equals(10) && seedCalls.get() == 2, "Empty input evaluates its seed exactly once");
	}

	private static void runScanChecks(final IDescription context, final IScope scope) {
		expect("[1,2,3,4] scan (@ + each)", List.of(1, 3, 6, 10), context, scope);
		expect("[1,2,3,4] scan (@ * each)", List.of(1, 2, 6, 24), context, scope);
		expect("[10,3,2] scan (@ - each)", List.of(10, 7, 5), context, scope);
		expect("[10,3,2] scan (0, @ - each)", List.of(0, -10, -13, -15), context, scope);
		expect("[1,2,3] scan (10, @ + each)", List.of(10, 11, 13, 16), context, scope);
		expect("[] scan (0, @ + each)", List.of(0), context, scope);
		expect("[] scan (@)", List.of(), context, scope);
		expect("list<int>([]) scan (@ + each)", List.of(), context, scope);
		expect("[7] scan (each div 0)", List.of(7), context, scope);
		expect("[] scan (0, each div 0)", List.of(0), context, scope);
		expect("[nil] scan (@)", java.util.Collections.singletonList(null), context, scope);
		expect("[1,2] scan (nil, each)", java.util.Arrays.asList(null, 1, 2), context, scope);
		expect("[] scan (nil, each)", java.util.Collections.singletonList(null), context, scope);
		expect("['a','b','c'] scan (@ + ',' + each)", List.of("a", "a,b", "a,b,c"), context, scope);
		expect("['a','bb','ccc'] scan (0, @ + length(each))", List.of(0, 1, 3, 6), context, scope);
		expect("['a'::1,'b'::2,'c'::3] scan (@ + each)", List.of(1, 3, 6), context, scope);
		expect("matrix([[1,2],[3,4]]) scan (@ + each)", List.of(1, 4, 6, 10), context, scope);
		expect("scan([1,2,3], @ + each)", List.of(1, 3, 6), context, scope);
		expect("scan([1,2,3], 10, @ + each)", List.of(10, 11, 13, 16), context, scope);
		expect("[1,2] scan (0.0, @ + each)", List.of(0d, 1d, 3d), context, scope);
		expect("[1,2] scan (0.0, 1)", List.of(0d, 1d, 1d), context, scope);
		expect("[] scan (0.0, @ + each)", List.of(0d), context, scope);
		expect("[1,2.0] scan (@ + each)", List.of(1d, 3d), context, scope);
		expect("[[1,2],[3,4]] scan (0, @ + (each reduce (@ + each)))", List.of(0, 3, 10), context, scope);
		expect("[[1,2],[3,4]] scan (0, last(each scan (@, @ + each)) + @)", List.of(0, 3, 13), context, scope);
		expect("[[1,2],[3,4]] reduce (0, @ + last(each scan (@ + each)))", 10, context, scope);
		expect("[1,2] collect (acc: [acc,3] scan (@ + each))",
				List.of(List.of(1, 4), List.of(2, 5)), context, scope);
		expect("[1,2] scan (0, sum([each] collect (@ + each)))", List.of(0, 1, 3), context, scope);
		expect("[[1],[2,3]] scan (@ + each)", List.of(List.of(1), List.of(1, 2, 3)), context, scope);
		expect("[1,2,3] scan (list<int>([]), @ + each)",
				List.of(List.of(), List.of(1), List.of(1, 2), List.of(1, 2, 3)), context, scope);
		expect("[] scan (list<int>([]), @ + each)", List.of(List.of()), context, scope);

		for (final String source : new String[] { "[1,2] scan (0.0, @ + each)", "[] scan (0.0, @ + each)",
				"[1,2.0] scan (@ + each)" }) {
			final var expression = GAML.getExpressionFactory().createExpr(source, context);
			check(expression.getGamlType().equals(Types.LIST.of(Types.FLOAT)), "Scan result type: " + source);
			check(expression.value(scope) instanceof IList<?> list && list.getGamlType().equals(expression.getGamlType()),
					"Scan runtime type: " + source);
		}
		final var unknownSeed = GAML.getExpressionFactory().createExpr("[1,2] scan (nil, each)", context);
		final var unknownValues = (IList<?>) unknownSeed.value(scope);
		check(unknownValues.getGamlType().getContentType().equals(unknownSeed.getGamlType().getContentType()),
				"Scan retains the seed's declared content type even when the body has a more specific type");
		check(GAML.getExpressionFactory().createExpr("[[1],[2]] scan (@ + each)", context)
				.getGamlType().equals(Types.LIST.of(Types.LIST.of(Types.INT))), "Scan retains nested content parameters");
		check(GAML.getExpressionFactory().createExpr("agent scan (0, @ + 1)", context)
				.getGamlType().equals(Types.LIST.of(Types.INT)), "Species support seeded scan");
		for (final String source : new String[] { "[1,2] scan (@ + each)", "[1,2] scan (0, @ + each)" }) {
			final var expression = (IOperator) GAML.getExpressionFactory().createExpr(source, context);
			final var prototype = (IArtefact.Operator) expression.getPrototype();
			check(prototype.getPattern(true).contains(" scan (") && prototype.getPattern(true).contains("@ + each"),
					"Scan editor template exposes accumulator syntax");
			check(!prototype.documentSignature().contains("string"), "Scan hover hides the iterator name");
		}

		reject("[1,2] scan (@, @ + each)", "'@' is only accessible", context);
		reject("[1,2] scan (0, 'not an integer')", "The scan body must return a value compatible with int", context);
		reject("1 scan (0, @ + each)", "scan expects a container", context);
		reject("[1,2] scan (0, @ + each, 1)", "scan expects a body", context);
		reject("scan([1,2])", "scan expects a container, an optional initial value, and a body", context);
		reject("scan([1,2], 0, @ + each, 1)", "scan expects a container, an optional initial value, and a body", context);

		scope.setEach("each", "outer element");
		scope.setEach("@", "outer accumulator");
		expect("[1,2,3] scan (@ + each)", List.of(1, 3, 6), context, scope);
		check(scope.getEach("each").equals("outer element") && scope.getEach("@").equals("outer accumulator"),
				"Successful scan restores outer bindings");
		try {
			GAML.getExpressionFactory().createExpr("[1,2] scan (@ div 0)", context).value(scope);
			throw new AssertionError("Expected scan runtime failure");
		} catch (final GamaRuntimeException expected) {
			check(scope.getEach("each").equals("outer element") && scope.getEach("@").equals("outer accumulator"),
					"Failed scan restores outer bindings");
		}

		final var input = GamaListFactory.wrap(Types.INT, 1, 2, 3);
		final var original = new ArrayList<>(input);
		final AtomicInteger calls = new AtomicInteger();
		final IExpression body = GAML.getExpressionFactory().createExpr(sc -> {
			calls.incrementAndGet();
			return (Integer) sc.getEach("@") + (Integer) sc.getEach("each");
		}, Types.INT);
		check(List.of(10, 11, 13, 16).equals(Containers.scan(scope, "each", input,
				GAML.getExpressionFactory().createConst(10, Types.INT), body)),
				"Seeded scan runtime result");
		check(calls.get() == 3, "Seeded scan evaluates its body once per element");
		calls.set(0);
		check(List.of(1, 3, 6).equals(Containers.scan(scope, "each", input, body)), "Unseeded scan runtime result");
		check(calls.get() == 2, "Unseeded scan skips the first element");
		check(original.equals(input), "Scan does not modify the source container");
		check(List.of().equals(Containers.scan(scope, "each", GamaListFactory.create(Types.INT), body))
				&& calls.get() == 2, "Empty unseeded scan does not evaluate the body");

		final AtomicInteger seedCalls = new AtomicInteger();
		final IExpression seed = GAML.getExpressionFactory().createExpr(sc -> {
			seedCalls.incrementAndGet();
			check(sc.getEach("@").equals("outer accumulator") && sc.getEach("each").equals("outer element"),
					"Scan seed evaluates outside its bindings");
			return 10;
		}, Types.INT);
		final var template = (IOperator) GAML.getExpressionFactory().createExpr("[1,2,3] scan (10, @ + each)", context);
		final IExpression seeded = GAML.getExpressionFactory().createOperator("scan", context, null,
				template.arg(0), template.arg(1), seed, template.arg(3));
		check(List.of(10, 11, 13, 16).equals(seeded.value(scope)) && seedCalls.get() == 1,
				"Scan evaluates its seed exactly once and includes it as the first result");
		final IExpression emptySeeded = GAML.getExpressionFactory().createOperator("scan", context, null,
				template.arg(0), GAML.getExpressionFactory().createExpr("[]", context), seed, template.arg(3));
		check(List.of(10).equals(emptySeeded.value(scope)) && seedCalls.get() == 2,
				"Empty seeded scan evaluates its seed exactly once");
	}
}
