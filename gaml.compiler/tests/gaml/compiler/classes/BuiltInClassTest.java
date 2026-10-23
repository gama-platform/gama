/*******************************************************************************************************
 *
 * BuiltInClassTest.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.classes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.osgi.launch.EquinoxFactory;

import gama.annotations.action;
import gama.annotations.doc;
import gama.annotations.gaml_class;
import gama.annotations.getter;
import gama.annotations.listener;
import gama.annotations.operator;
import gama.annotations.no_test;
import gama.annotations.setter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.additions.GamaBundleLoader;
import gama.api.GAMA;
import gama.api.additions.IGamlAdditions;
import gama.api.compilation.GamlCompilationError;
import gama.api.gaml.GAML;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gama.api.kernel.GamaMetaModel;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IConstructor;
import gama.api.kernel.object.IObject;
import gama.api.kernel.object.IObjectConstructor;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.ExecutionScope;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.IWorkspaceManager;
import gama.core.object.GamlObject;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.descriptions.DescriptionFactory;
import gaml.compiler.factories.ExpressionDescriptionFactory;
import gaml.compiler.expressions.GamlExpressionFactory;
import gaml.compiler.factories.ClassFactory;
import gaml.compiler.factories.ExperimentFactory;
import gaml.compiler.factories.ModelFactory;
import gaml.compiler.factories.PlatformFactory;
import gaml.compiler.factories.SkillFactory;
import gaml.compiler.factories.SpeciesFactory;
import gaml.compiler.factories.StatementFactory;
import gaml.compiler.factories.VariableFactory;
import gaml.compiler.prototypes.ArtefactFactory;
import gaml.compiler.validation.GamlModelBuilder;

/**
 * Standalone integration test. Compile with GamaProcessor, then pass the generated additions class to main.
 * Run with --add-modules jdk.incubator.vector and the compiler/core runtime classpath.
 */
public final class BuiltInClassTest {

	@gaml_class (name = "native_counter", doc = @doc ("A Java-backed counter used by integration tests."))
	@vars ({ @variable (name = "value", type = IType.INT, init = "7", doc = @doc ("Counter value.")),
			@variable (name = "seed", type = IType.INT, doc = @doc ("A Java-initialized value.")),
			@variable (name = "notifications", type = IType.INT, constant = true,
					doc = @doc ("Number of notified changes.")) })
	public static class Counter extends GamlObject {
		private int value = -1;
		private int notifications;
		private int seed = 23;

		public Counter(final IScope scope, final IClass clazz) { super(clazz); }

		@getter ("value")
		public int getValue() { return value; }

		@setter ("value")
		public void setValue(final int value) { this.value = value; }

		@getter (value = "seed", initializer = true)
		public int getSeed() { return seed; }

		@setter ("seed")
		public void setSeed(final int value) { seed = value; }

		@getter ("notifications")
		public int getNotifications() { return notifications; }

		@listener ("value")
		public void changed(final IScope scope, final Integer value) { notifications++; }

		@action (name = "increment", doc = @doc ("Increments this object, not the calling agent."))
		public Integer increment(final IScope scope) { return ++value; }
	}

	@gaml_class (name = "native_child", doc = @doc ("A native subclass of the counter."))
	@vars (@variable (name = "value", type = IType.INT, init = "9", doc = @doc ("Overridden default.")))
	public static class Child extends Counter {
		public Child(final IScope scope, final IClass clazz) { super(scope, clazz); }

		@Override
		@action (name = "increment", doc = @doc ("Adds two instead of one."))
		public Integer increment(final IScope scope) {
			setValue(getValue() + 2);
			return getValue();
		}
	}

	@gaml_class (name = "native_abstract", doc = @doc ("An abstract native class."))
	public abstract static class AbstractCounter extends Counter {
		public AbstractCounter(final IScope scope, final IClass clazz) { super(scope, clazz); }
	}

	@operator (value = "native_identity", category = "System")
	@doc ("Returns the exact same native object.")
	@no_test
	public static Counter identity(final Counter counter) { return counter; }

	private static int checks;

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
		checks++;
	}

	private static void bootstrap() throws Exception {
		final var root = Proxy.newProxyInstance(org.eclipse.core.resources.IWorkspaceRoot.class.getClassLoader(),
				new Class<?>[] { org.eclipse.core.resources.IWorkspaceRoot.class }, (p, m, a) -> switch (m.getName()) {
					case "findContainersForLocation" -> new org.eclipse.core.resources.IContainer[0];
					case "findFilesForLocation" -> new org.eclipse.core.resources.IFile[0];
					default -> throw new UnsupportedOperationException(m.getName());
				});
		final var workspace = Proxy.newProxyInstance(org.eclipse.core.resources.IWorkspace.class.getClassLoader(),
				new Class<?>[] { org.eclipse.core.resources.IWorkspace.class }, (p, m, a) -> {
					if (m.getName().equals("addResourceChangeListener")) return null;
					throw new UnsupportedOperationException(m.getName());
				});
		GAMA.setWorkspaceManager((IWorkspaceManager) Proxy.newProxyInstance(IWorkspaceManager.class.getClassLoader(),
				new Class<?>[] { IWorkspaceManager.class }, (p, m, a) -> switch (m.getName()) {
					case "getWorkspace" -> workspace;
					case "getRoot" -> root;
					default -> throw new UnsupportedOperationException(m.getName());
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
		GamaBundleLoader.CURRENT_PLUGIN_NAME = "gama.api";
		new gaml.additions.api.GamlAdditions().initialize();
		GamaBundleLoader.CURRENT_PLUGIN_NAME = "gama.core";
		new gaml.additions.core.GamlAdditions().initialize();
	}

	private static Object evaluate(final String source, final IModelSpecies model, final IScope scope) {
		final var expression = GAML.getExpressionFactory().createExpr(source, model.getDescription());
		check(expression != null, "Expression compiles: " + source);
		return expression.value(scope);
	}

	public static void main(final String[] args) {
		try {
			runWithPlatform(args[0]);
		} catch (final Throwable failure) {
			failure.printStackTrace();
			System.exit(1);
		}
		// GAML's background services are process-wide, so this standalone runner owns the JVM lifecycle.
		System.exit(0);
	}

	private static void runWithPlatform(final String additions) throws Exception {
		final var storage = Files.createTempDirectory("native-class-platform-");
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
			if (org.eclipse.core.runtime.RegistryFactory.getRegistry() == null) {
				final Object token = new Object();
				final var registry = org.eclipse.core.runtime.RegistryFactory.createRegistry(null, token, new Object());
				registry.addContribution(new ByteArrayInputStream("""
						<plugin>
						  <extension-point id="filesystems" name="File systems"/>
						  <extension point="org.eclipse.core.filesystem.filesystems">
						    <filesystem scheme="file">
						      <run class="org.eclipse.core.internal.filesystem.local.LocalFileSystem"/>
						    </filesystem>
						  </extension>
						</plugin>
						""".getBytes()), org.eclipse.core.runtime.ContributorFactorySimple.createContributor(
								"org.eclipse.core.filesystem"), false, null, null, token);
				org.eclipse.core.runtime.RegistryFactory.setDefaultRegistryProvider(() -> registry);
			}
			runChecks(additions);
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

	private static void runChecks(final String additions) throws Exception {
		final var injector = GamlStandaloneSetup.doSetup();
		bootstrap();
		GamaBundleLoader.CURRENT_PLUGIN_NAME = "nativeclasses";
		((IGamlAdditions) Class.forName(additions).getConstructor().newInstance()).initialize();
		check(Types.get(Counter.class).getName().equals("native_counter"),
				"Java type registered before operator signatures are resolved");
		GamaMetaModel.build();
		Types.init();
		GamlStandaloneSetup.initializeAfterPlatformReady(injector);
		final var description = GamaMetaModel.getClassDescription("native_counter");
		final var agentConstructor = GamaMetaModel.getSpeciesDescription("agent").getConstructor();
		check(agentConstructor != null, "Species expose their agent factory through the shared accessor");
		final IPopulation<?> population = (IPopulation<?>) Proxy.newProxyInstance(IPopulation.class.getClassLoader(),
				new Class<?>[] { IPopulation.class }, (p, m, a) -> {
					if (m.getName().equals("hashCode")) return System.identityHashCode(p);
					throw new UnsupportedOperationException(m.getName());
				});
		final IConstructor<IAgent, IPopulation<?>, Integer> sharedAgentConstructor = agentConstructor;
		final IAgent agent = sharedAgentConstructor.create(population, 17);
		check(agent.getPopulation() == population && agent.getIndex() == 17,
				"Shared factory preserves agent population and index");
		final IAgent directAgent = agentConstructor.createOneAgent(population, 18);
		check(directAgent.getPopulation() == population && directAgent.getIndex() == 18,
				"Primitive-index agent construction remains available");
		check(description.getConstructor() != null, "Java-backed classes provide an object constructor");
		check(description.isBuiltIn() && !description.isAbstract(), "Concrete built-in description");
		check(description.getJavaBase() == Counter.class, "Java implementation preserved");
		check(description.getDefiningPlugin().equals("nativeclasses"), "Defining plugin preserved");
		check(description.hasAttribute("value") && description.getAction("increment") != null,
				"Annotated attributes and actions copied");
		check(GamaMetaModel.getClassDescription("native_child").getParent() == description,
				"Nearest annotated Java superclass is the GAML parent");
		check(GamaMetaModel.getClassDescription("native_abstract").isAbstract(), "Java abstract class stays abstract");

		final var file = Files.createTempFile("native-classes-", ".gaml");
		try {
			Files.writeString(file, """
					model native_class_test
					global {
						native_counter c <- native_counter();
						native_counter fluent <- native_identity(native_counter());
						context shell <- command("echo hi", context());
					}
					class local_record {
						int value <- 2;
						int increment() { value <- value + 1; return value; }
					}
					class derived parent: native_counter { int extra <- 3; }
					class abstract_derived parent: native_abstract {}
					experiment main type: gui {}
					""");
			final var errors = new ArrayList<GamlCompilationError>();
			final IModelSpecies model = GamlModelBuilder.getInstance().compile(
					org.eclipse.emf.common.util.URI.createFileURI(file.toString()), errors);
			check(errors.stream().noneMatch(GamlCompilationError::isError), "Model compiles without errors: " + errors);
			check(model != null, "Model produces runtime species");
			final IScope scope = new ExecutionScope(null) {
				@Override
				public IModelSpecies getModel() { return model; }

				@Override
				public ITypesManager getTypes() { return model.getDescription().getTypesManager(); }
			};
			check(model.getClass("native_counter") == GamaMetaModel.getClass("native_counter"),
					"Model resolves the shared built-in class");
			check(evaluate("native_counter().value", model, scope).equals(7), "Zero-argument constructor and getter");
			check(evaluate("native_counter().seed", model, scope).equals(23),
					"Initializer getter sees fully constructed Java object");
			check(evaluate("native_counter(value: 12).value", model, scope).equals(12),
					"Named constructor arguments survive Java field initialization");
			check(evaluate("native_identity(native_counter()).value", model, scope).equals(7),
					"Native operator arguments and return type");
			check(evaluate("native_counter().increment()", model, scope).equals(8), "Native action targets its object");
			check(evaluate("native_child().value", model, scope).equals(9), "Native subclass overrides attributes");
			check(evaluate("native_child().increment()", model, scope).equals(11), "Native subclass overrides actions");
			check(evaluate("derived().extra", model, scope).equals(3), "GAML subclass initializes own attributes");
			check(evaluate("derived().increment()", model, scope).equals(8), "GAML subclass inherits native actions");
			check(evaluate("local_record().value", model, scope).equals(2), "Existing model-defined class unchanged");
			check(evaluate("local_record().increment()", model, scope).equals(3),
					"Model-defined methods still access their object's variables");
			checkContext(model, scope, file.getParent().toFile());
			final Counter counter = (Counter) model.getClass("native_counter").createInstance(scope, Map.of());
			if (!(description.getConstructor() instanceof IObjectConstructor objectConstructor))
				throw new AssertionError("Class descriptions expose an object constructor");
			final IConstructor<IObject, IScope, IClass> sharedObjectConstructor = objectConstructor;
			final IObject constructed = sharedObjectConstructor.create(scope, model.getClass("native_counter"));
			check(constructed instanceof Counter && constructed.getSpecies() == model.getClass("native_counter"),
					"Shared factory preserves object implementation and class");
			check(model.getDescription().getClassDescription("derived").getConstructor() == description.getConstructor(),
					"Model-defined classes inherit the shared constructor");
			counter.setDirectVarValue(scope, "value", 42);
			check(counter.getValue() == 42 && counter.getNotifications() == 1, "Native setter and listener");
			check(counter.get(scope, "value").equals(42), "String indexing uses native getter");
			final IObject copy = counter.copy(scope);
			check(copy instanceof Counter c && c.getValue() == 42 && copy != counter, "Copy preserves native class");
			final IObject derived = model.getClass("derived").createInstance(scope, Map.of("value", 11));
			check(derived instanceof Counter c && c.getValue() == 11, "GAML subclass uses inherited Java factory");
			check(derived.isInstanceOf(model.getClass("native_counter"), false), "Native parent instance check");
			check(Types.get(Counter.class).getName().equals("native_counter"),
					"Model-defined subclasses do not overwrite Java-to-native-type correspondence");
			check(Types.get(Counter.class).isAssignableFrom(Types.get(Child.class)), "Native type inheritance");
			check(model.getDescription().getClassDescription("abstract_derived").isAbstract(),
					"GAML subclass cannot instantiate an abstract Java implementation");
			model.dispose();
			check(GamaMetaModel.getClass("native_counter").getVar("value") != null,
					"Disposing a model does not dispose shared native classes");
			for (final String invalid : new String[] {
					"global { native_abstract c <- native_abstract(); }",
					"class native_counter {}" }) {
				Files.writeString(file, "model invalid\n" + invalid);
				errors.clear();
				GamlModelBuilder.getInstance().compile(
						org.eclipse.emf.common.util.URI.createFileURI(file.toString()), errors);
				check(errors.stream().anyMatch(GamlCompilationError::isError), "Rejects invalid model: " + invalid);
			}
		} finally {
			Files.deleteIfExists(file);
		}
		checkInvalidImplementation("public class Invalid {}",
				"classes extending or implementing gama.api.kernel.object.IObject");
		checkInvalidImplementation("""
				public class Invalid extends gama.core.object.GamlObject {
					public Invalid(gama.api.kernel.object.IClass clazz) { super(clazz); }
				}
				""", "requires a public constructor (IScope scope, IClass clazz)");
		System.out.println("Built-in class integration: " + checks + " checks passed");
	}

	private static void checkContext(final IModelSpecies model, final IScope scope, final java.io.File folder)
			throws Exception {
		check(evaluate("context().directory.path", model, scope).equals(folder.getAbsolutePath()),
				"Context starts in the model directory");
		check(evaluate("command('printf hello', context()).result", model, scope).equals("hello"),
				"Context returns the output without a trailing separator");
		check(evaluate("command('echo hello', context()).result", model, scope).equals("hello\n"),
				"Context keeps line separators");
		check(evaluate("command('echo $X', command('X=42', context())).result", model, scope).equals("42\n"),
				"Context keeps shell variables");
		check(evaluate("command('pwd', command('cd ..', context())).directory.path", model, scope)
				.equals(folder.getCanonicalFile().getParentFile().getAbsolutePath()), "Context keeps directory changes");
		check(evaluate("command('false', context()).status", model, scope).equals(1),
				"Failing commands set status without errors");
		check(evaluate("command('echo b', command('echo a', context())).commands", model, scope)
				.equals(java.util.List.of("echo a", "echo b")), "Context records commands");
		check(evaluate("command('echo b', command('echo a', context())).results", model, scope)
				.equals(java.util.List.of("a\n", "b\n")), "Context records results");
		final var context = (gama.core.util.CommandContext) evaluate("context()", model, scope);
		final var child = Files.createTempDirectory(folder.toPath(), "context-");
		try {
			context.execute(scope, "Y=1");
			context.setDirectory(scope, new gama.api.types.file.GamaFolderFile(scope, child.toString()));
			check(context.execute(scope, "pwd -P; echo $Y").getResult().equals(
					child.toFile().getCanonicalPath() + "\n1\n"), "Assigned directory applies to the live shell");
			final long start = System.nanoTime();
			check(context.execute(scope, "sleep 2 &").getResult().isEmpty() && System.nanoTime() - start < 1_000_000_000L,
					"Background commands do not block");
			context.execute(scope, "exit 3");
			check(context.getStatus() == 3 && context.execute(scope, "echo $Y").getResult().equals("\n")
					&& context.getDirectory(scope).getFile(scope).getCanonicalPath().equals(child.toFile().getCanonicalPath()),
					"Exited shell restarts in the same directory with fresh state");
			check(context.close(scope) && !context.close(scope), "Close terminates the shell");
		} finally {
			context.close(scope);
			Files.deleteIfExists(child);
		}
	}

	private static void checkInvalidImplementation(final String declaration, final String diagnostic) throws Exception {
		final var output = Files.createTempDirectory("nativeclass");
		try {
			final var source = new SimpleJavaFileObject(java.net.URI.create("string:///Invalid.java"),
					JavaFileObject.Kind.SOURCE) {
				@Override
				public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
					return "@gama.annotations.gaml_class(name=\"invalid\")\n" + declaration;
				}
			};
			final var diagnostics = new DiagnosticCollector<JavaFileObject>();
			try (final var manager = ToolProvider.getSystemJavaCompiler().getStandardFileManager(diagnostics, null, null)) {
				final boolean passed = ToolProvider.getSystemJavaCompiler().getTask(null, manager, diagnostics,
						java.util.List.of("-proc:only", "-processor", "gama.processor.GamaProcessor",
								"-classpath", System.getProperty("java.class.path"),
								"-d", output.toString(), "-s", Files.createDirectory(output.resolve("target")).toString()),
						null, java.util.List.of(source)).call();
				check(!passed && diagnostics.getDiagnostics().stream()
						.anyMatch(d -> d.getMessage(null).contains(diagnostic)), "Processor rejects invalid class: " + diagnostic);
			}
		} finally {
			try (var paths = Files.walk(output)) {
				for (final var path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) { Files.delete(path); }
			}
		}
	}
}
