/*******************************************************************************************************
 *
 * GamaProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

import gama.annotations.tests;
import gama.processor.tests.TestProcessor;

/**
 * The GamaProcessor is the main annotation processor for the GAMA modeling and simulation platform.
 *
 * <p>
 * This processor serves as the central coordinator for all GAMA annotation processing during compilation. It implements
 * the standard Java annotation processing API and orchestrates the processing of various GAMA-specific annotations to
 * generate runtime registration code and test files.
 *
 * <p>
 * The processor operates during the compilation phase and:
 * <ul>
 * <li><strong>Discovers Annotations:</strong> Scans source code for GAMA-specific annotations</li>
 * <li><strong>Coordinates Processing:</strong> Delegates to specialized processors for each annotation type</li>
 * <li><strong>Generates Code:</strong> Produces GamlAdditions classes that register GAMA elements at runtime</li>
 * <li><strong>Creates Tests:</strong> Generates test files from documentation examples</li>
 * <li><strong>Manages Lifecycle:</strong> Handles multi-round processing and resource cleanup</li>
 * </ul>
 *
 * <h3>Architecture:</h3>
 * <p>
 * The processor follows a delegating architecture where:
 * <ol>
 * <li>The main processor coordinates the overall process</li>
 * <li>Specialized processors (ActionProcessor, OperatorProcessor, etc.) handle specific annotation types</li>
 * <li>A ProcessorContext provides shared utilities and state management</li>
 * <li>Generated code is collected and written to appropriate output files</li>
 * </ol>
 *
 * <h3>Processing Flow:</h3>
 * <p>
 * The annotation processing occurs in multiple rounds:
 * <ol>
 * <li><strong>Initialization:</strong> Set up processing environment and context</li>
 * <li><strong>Discovery:</strong> Find all annotated elements in the current compilation unit</li>
 * <li><strong>Processing:</strong> Delegate to specialized processors for code generation</li>
 * <li><strong>Generation:</strong> Write generated code to GamlAdditions classes</li>
 * <li><strong>Testing:</strong> Generate test files from documentation examples</li>
 * </ol>
 *
 * <h3>Generated Output:</h3>
 * <p>
 * The processor generates several types of output:
 * <ul>
 * <li><strong>GamlAdditions Classes:</strong> Runtime registration code for GAMA elements</li>
 * <li><strong>Test Files:</strong> Automated tests derived from documentation examples</li>
 * <li><strong>Diagnostic Messages:</strong> Compilation errors, warnings, and progress information</li>
 * </ul>
 *
 * <h3>Configuration:</h3>
 * <p>
 * The processor is configured to:
 * <ul>
 * <li>Support all annotation types ("*")</li>
 * <li>Target Java 21 language features</li>
 * <li>Process incrementally across multiple compilation rounds</li>
 * </ul>
 *
 * <h3>Performance Monitoring:</h3>
 * <p>
 * The processor includes timing measurements to track processing performance and reports processing times for different
 * phases to help with build optimization.
 *
 * @author GAMA Development Team
 * @since 1.0
 * @see AbstractProcessor
 * @see ProcessorContext
 * @see Constants
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
@SupportedAnnotationTypes ({ "*" })
@SupportedSourceVersion (SourceVersion.RELEASE_21)
public class GamaProcessor extends AbstractProcessor implements Constants {

	/**
	 * The processing context that provides shared utilities, type information, and state management across all
	 * processors.
	 */
	private ProcessorContext context;

	/**
	 * Counter for tracking processing operations (currently unused).
	 */
	int count;

	/**
	 * Timestamp marking the beginning of a processing phase. Used for performance measurement and reporting.
	 */
	long begin = 0;

	/**
	 * Timestamp marking the start of complete processing for a plugin. Used to measure total processing time across all
	 * phases.
	 */
	long complete = 0;

	/**
	 * Initializes the annotation processor with the processing environment.
	 *
	 * <p>
	 * This method is called once by the compiler before processing begins. It sets up the processing context that will
	 * be used throughout the annotation processing lifecycle.
	 *
	 * @param pe
	 *            the processing environment provided by the compiler
	 */
	@Override
	public synchronized void init(final ProcessingEnvironment pe) {
		super.init(pe);
		context = new ProcessorContext(pe);
	}

	/**
	 * Processes annotations in the current compilation round.
	 *
	 * <p>
	 * This is the core method of the annotation processor that is called by the compiler for each round of processing.
	 * The method:
	 * <ol>
	 * <li>Sets up timing measurements for performance tracking</li>
	 * <li>Updates the processing context with the current round environment</li>
	 * <li>Delegates to specialized processors if there are source roots to process</li>
	 * <li>Generates output files when processing is complete</li>
	 * <li>Reports timing information for different processing phases</li>
	 * </ol>
	 *
	 * <p>
	 * The method handles exceptions during processing by emitting warnings and rethrowing them to ensure compilation
	 * fails if critical errors occur.
	 *
	 * <p>
	 * Processing completion is determined by the context, typically after all source files have been processed. At
	 * completion, the method:
	 * <ul>
	 * <li>Generates Java source files containing GAMA element registrations</li>
	 * <li>Creates test files from documentation examples</li>
	 * <li>Reports performance metrics for build optimization</li>
	 * </ul>
	 *
	 * @param annotations
	 *            the set of annotation types found in the current round
	 * @param env
	 *            the round environment providing access to annotated elements
	 * @return {@code true} to indicate that annotations were processed (standard practice)
	 */
	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment env) {
		if (complete == 0) { complete = System.currentTimeMillis(); }
		context.setRoundEnvironment(env);
		// context.emit(Kind.NOTE, "GAML Processor: Entering the production of sources for " + context.currentPlugin,
		// (Element) null);

		if (context.getRoots().size() > 0) {
			try {
				begin = System.currentTimeMillis();
				processors.forEach((s, p) -> p.process(context));
				// After processing all elements, discover plugin-specific packages for dynamic imports -- not yet
				// functional
				// context.discoverPluginPackages();
			} catch (final Exception e) {
				context.emitWarning("An exception occured in the parsing of GAML annotations: ", e);
				throw e;
			}
		}
		if (context.processingOver()) {
			final FileObject file = context.createSource();
			generateJavaSource(file);
			context.emit(Kind.NOTE, "GAML Processor: Java sources produced for " + context.currentPlugin + " in "
					+ (System.currentTimeMillis() - begin) + "ms", (Element) null);
			begin = System.currentTimeMillis();
			generateTests();
			context.emit(Kind.NOTE, "GAML Processor: GAMA tests produced for " + context.currentPlugin + " in "
					+ (System.currentTimeMillis() - begin) + "ms", (Element) null);
			context.emit(Kind.NOTE, "GAML Processor: Complete processing of " + context.currentPlugin + " in "
					+ (System.currentTimeMillis() - complete) + "ms", (Element) null);
			complete = 0;
		}
		return true;
	}

	/**
	 * Generates test files from documentation examples and test annotations.
	 *
	 * <p>
	 * This method extracts test cases from GAMA documentation and creates executable test files that can be run to
	 * verify the correctness of documented examples. The process involves:
	 * <ul>
	 * <li>Collecting test elements from the TestProcessor</li>
	 * <li>Creating a test writer for output generation</li>
	 * <li>Writing test code to appropriate test files</li>
	 * <li>Handling IO exceptions during file creation</li>
	 * </ul>
	 *
	 * <p>
	 * Tests are generated only if the TestProcessor has found elements to process. Any exceptions during test
	 * generation are reported as warnings but do not fail the compilation process.
	 */
	public void generateTests() {
		final TestProcessor tp = (TestProcessor) processors.get(tests.class);
		if (tp.hasElements()) {
			try (Writer source = context.createTestWriter()) {
				tp.writeTests(source);
			} catch (final IOException e) {
				context.emitWarning("An exception occured in the generation of test files: ", e);
			}
		}
		// We pass the current document of the documentation processor to avoir re-reading it
		// final DocProcessor dp = (DocProcessor) processors.get(doc.class);
		// ExamplesToTests.createTests(context, dp.document);
	}

	/**
	 * Generates Java source files containing GAMA element registration code.
	 *
	 * <p>
	 * This method creates the GamlAdditions classes that contain all the runtime registration code generated by the
	 * annotation processors. The generated code enables GAMA to discover and utilize annotated elements at runtime.
	 *
	 * <p>
	 * The process involves:
	 * <ul>
	 * <li>Creating a source writer for the output file</li>
	 * <li>Writing the complete Java class body with all registrations</li>
	 * <li>Handling IO exceptions during file creation</li>
	 * <li>Ensuring proper resource cleanup</li>
	 * </ul>
	 *
	 * <p>
	 * Any exceptions during generation are reported and may cause compilation failure to ensure the build process fails
	 * if critical registration code cannot be created.
	 *
	 * @param file
	 *            the file object where the generated source should be written
	 */
	public void generateJavaSource(final FileObject file) {
		try (Writer source = context.createSourceWriter(file)) {
			if (source != null) { source.append(writeJavaBody()); }
		} catch (final IOException io) {
			context.emitWarning("An IO exception occured in the generation of Java files: ", io);
		} catch (final Exception e) {
			context.emitWarning("An exception occured in the generation of Java files: ", e);
			throw e;
		}
	}

	/**
	 * Writes the immutable header portion of the generated GamlAdditions class.
	 *
	 * <p>
	 * This method generates the static portions of the GamlAdditions class including:
	 * <ul>
	 * <li>Package declaration and imports (static and regular)</li>
	 * <li>Class declaration extending AbstractGamlAdditions</li>
	 * <li>Suppression of common warnings for generated code</li>
	 * <li>Beginning of the initialize() method</li>
	 * </ul>
	 *
	 * <p>
	 * The imports now include both the standard GAMA imports and any plugin-specific imports discovered during
	 * annotation processing.
	 *
	 * @param sb
	 *            the StringBuilder to append the header code to
	 */
	protected void writeImmutableHeader(final StringBuilder sb) {
		// Use dynamic imports that include plugin-specific packages
		for (final String element : context.getAllStaticCollectiveImports()) {
			sb.append(ln).append("import static ").append(element).append("*;");
		}
		for (final String element : context.getAllCollectiveImports()) {
			sb.append(ln).append("import ").append(element).append("*;");
		}
		for (final String element : INDIVIDUAL_IMPORTS) { sb.append(ln).append("import ").append(element).append(";"); }
		sb.append(ln).append("@SuppressWarnings({ \"rawtypes\", \"unchecked\", \"unused\" })");
		sb.append(ln).append(ln).append("public class GamlAdditions extends gama.api.additions.AbstractGamlAdditions")
				.append(" {");
		sb.append(ln).append(tab);
		sb.append("public void initialize() throws SecurityException, NoSuchMethodException {");
	}

	/**
	 * Writes the mutable header portion of the generated GamlAdditions class.
	 *
	 * <p>
	 * This method generates the variable portions of the GamlAdditions class that depend on which processors have
	 * elements to contribute. It:
	 * <ul>
	 * <li>Iterates through all registered processors</li>
	 * <li>Checks if each processor has elements to contribute and outputs to Java</li>
	 * <li>Adds method calls to each processor's initialization method</li>
	 * <li>Closes the initialize() method</li>
	 * </ul>
	 *
	 * <p>
	 * The "mutable" designation refers to the fact that this portion varies based on what annotations are found in the
	 * current compilation unit.
	 *
	 * @param sb
	 *            the StringBuilder to append the header code to
	 */
	protected void writeMutableHeader(final StringBuilder sb) {
		processors.values().forEach(p -> {
			if (p.outputToJava() && p.hasElements()) {
				final String method = p.getInitializationMethodName();
				if (method != null) { sb.append(ln).append(tab).append(method).append("();"); }
			}
		});

		sb.append(ln).append('}');
	}

	/**
	 * Constructs the complete Java source code for the GamlAdditions class.
	 *
	 * <p>
	 * This method assembles all the components needed for the generated GamlAdditions class that will contain runtime
	 * registration code for all GAMA elements discovered during annotation processing.
	 *
	 * <p>
	 * The generated class structure includes:
	 * <ol>
	 * <li>Package declaration based on the current plugin</li>
	 * <li>Standard imports required for GAMA registration</li>
	 * <li>Class declaration and method headers</li>
	 * <li>Method calls to initialize different element types</li>
	 * <li>Registration code from all active processors</li>
	 * <li>Proper class closing</li>
	 * </ol>
	 *
	 * <p>
	 * Only processors that have elements to contribute and generate Java output are included in the final generated
	 * code.
	 *
	 * @return a StringBuilder containing the complete GamlAdditions class source code
	 */
	public StringBuilder writeJavaBody() {
		final StringBuilder sb = new StringBuilder();
		sb.append("package ").append(PACKAGE_NAME).append(".").append(context.shortcut).append(';');
		sb.append(ln);
		writeImmutableHeader(sb);
		writeMutableHeader(sb);
		processors.values().forEach(p -> { if (p.outputToJava() && p.hasElements()) { p.writeJavaBody(sb); } });

		sb.append(ln).append('}');
		return sb;
	}

	/**
	 * Retrieves the TypeMirror for a given qualified class name.
	 *
	 * <p>
	 * This method delegates to the processing context to obtain type information for the specified class. It is used by
	 * the annotation processing system to resolve type references during code generation.
	 *
	 * @param classQualifiedName
	 *            the fully qualified name of the class to resolve
	 * @return the TypeMirror representing the specified class, or null if not found
	 */
	@Override
	public TypeMirror getType(final String classQualifiedName) {
		return context.getType(classQualifiedName);
	}

}
