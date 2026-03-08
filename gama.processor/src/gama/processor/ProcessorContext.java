/*******************************************************************************************************
 *
 * ProcessorContext.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor;

import static java.util.Collections.sort;

// import static gama.processor.annotations.GamlProperties.GAML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The ProcessorContext serves as the central context and utility provider for GAMA annotation processing.
 *
 * <p>
 * This class acts as a bridge between the standard Java annotation processing API and the GAMA-specific processing
 * requirements. It implements both {@link ProcessingEnvironment} and {@link RoundEnvironment} interfaces, providing a
 * unified access point for all processing utilities while adding GAMA-specific functionality.
 *
 * <h3>Core Responsibilities:</h3>
 * <ul>
 * <li><strong>Environment Delegation:</strong> Proxies access to the standard annotation processing environment</li>
 * <li><strong>Element Organization:</strong> Groups and sorts annotated elements for efficient processing</li>
 * <li><strong>File Management:</strong> Handles creation of source files, test files, and resource files</li>
 * <li><strong>Error Reporting:</strong> Provides consistent error and warning reporting across all processors</li>
 * <li><strong>Type Resolution:</strong> Manages type lookups and validation for annotation processing</li>
 * <li><strong>Plugin Detection:</strong> Automatically detects the current plugin being processed</li>
 * <li><strong>Project Setup:</strong> Creates test projects and folder structures as needed</li>
 * </ul>
 *
 * <h3>Element Processing Features:</h3>
 * <p>
 * The context provides sophisticated element handling capabilities:
 * <ul>
 * <li>Automatic grouping of annotated elements by their root classes</li>
 * <li>Consistent sorting of elements to ensure deterministic processing order</li>
 * <li>Efficient filtering of annotations relevant to GAMA processing</li>
 * <li>Root element tracking for incremental processing scenarios</li>
 * </ul>
 *
 * <h3>File Generation Support:</h3>
 * <p>
 * The context manages various types of file generation:
 * <ul>
 * <li><strong>Source Files:</strong> Generated GamlAdditions classes containing registration code</li>
 * <li><strong>Test Files:</strong> Executable test files derived from documentation examples</li>
 * <li><strong>Project Files:</strong> Eclipse project configurations for test projects</li>
 * <li><strong>Resource Files:</strong> Additional resources needed during processing</li>
 * </ul>
 *
 * <h3>Plugin Integration:</h3>
 * <p>
 * The context automatically detects the current GAMA plugin being processed by analyzing file paths and URIs. This
 * enables:
 * <ul>
 * <li>Plugin-specific package naming for generated classes</li>
 * <li>Appropriate test folder organization</li>
 * <li>Proper resource placement within the plugin structure</li>
 * </ul>
 *
 * <h3>Error Handling:</h3>
 * <p>
 * Comprehensive error reporting includes:
 * <ul>
 * <li>Contextual error messages with source location information</li>
 * <li>Exception stack trace capture and formatting</li>
 * <li>Configurable warning and error emission</li>
 * <li>Graceful degradation when file operations fail</li>
 * </ul>
 *
 * <h3>Thread Safety:</h3>
 * <p>
 * The context uses volatile fields for plugin information that may be accessed across different processing rounds,
 * ensuring thread-safe access to shared state.
 *
 * @author GAMA Development Team
 * @since 1.0
 * @see ProcessingEnvironment
 * @see RoundEnvironment
 * @see Constants
 */
public class ProcessorContext implements ProcessingEnvironment, RoundEnvironment, Constants {

	/**
	 * Standard location for generated source output files. Points to the SOURCE_OUTPUT location defined by the
	 * annotation processing API.
	 */
	public static final StandardLocation OUT = StandardLocation.SOURCE_OUTPUT;

	/**
	 * The underlying processing environment delegate that provides access to the standard annotation processing
	 * utilities (messager, filer, type utils, etc.).
	 */
	private final ProcessingEnvironment delegate;

	/**
	 * The current round environment containing information about annotated elements and processing state for the
	 * current compilation round.
	 */
	private RoundEnvironment round;

	/**
	 * The name of the current GAMA plugin being processed. Volatile to ensure thread-safe access across processing
	 * rounds. Automatically detected from source file paths.
	 */
	public volatile String currentPlugin;

	/**
	 * Short name/identifier for the current plugin (last part after dots). Volatile to ensure thread-safe access across
	 * processing rounds. Used for generating unique package and class names.
	 */
	public volatile String shortcut;

	/**
	 * List of root element names from the current processing round. Used for incremental processing and cache
	 * management.
	 */
	public List<String> roots;

	/**
	 * Reference to the parent GamaProcessor instance. Used to record import usage in the persistent cache.
	 * This enables tracking which imports are actually used across compilation rounds.
	 */
	private GamaProcessor processor;
	
	/**
	 * Counter for tracking how many imports are recorded (for debugging).
	 */
	private int importTrackingCount = 0;

	/**
	 * Set of plugin-specific packages discovered during annotation processing. These packages will be added to the
	 * collective imports for the current plugin.
	 */
	private final Set<String> dynamicCollectiveImports = new LinkedHashSet<>();

	/**
	 * Set of plugin-specific static imports discovered during annotation processing. These static imports will be added
	 * to the static collective imports for the current plugin.
	 */
	private final Set<String> dynamicStaticImports = new LinkedHashSet<>();

	/**
	 * Shared XML document builder for parsing XML resources during processing. Initialized once during class loading
	 * for performance efficiency.
	 */
	public static final DocumentBuilder xmlBuilder;

	static {
		DocumentBuilder temp = null;
		try {
			temp = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (final ParserConfigurationException e) {}
		xmlBuilder = temp;
	}

	/**
	 * Constructs a new ProcessorContext wrapping the given processing environment.
	 *
	 * <p>
	 * This constructor initializes the context with the standard annotation processing environment, which provides
	 * access to the compiler's utilities for file creation, type resolution, and message emission.
	 *
	 * @param pe
	 *            the processing environment to wrap and delegate to
	 */
	public ProcessorContext(final ProcessingEnvironment pe) {
		delegate = pe;
	}

	/**
	 * Gets the shared XML document builder for parsing XML resources.
	 *
	 * <p>
	 * This builder is initialized once during class loading and can be safely used across all processing operations. It
	 * is primarily used for parsing XML configuration files and resources during annotation processing.
	 *
	 * @return the shared DocumentBuilder instance, or null if initialization failed
	 */
	public DocumentBuilder getBuilder() { return xmlBuilder; }

	/**
	 * Gets the fully qualified name of a type element, handling nested classes properly.
	 *
	 * <p>
	 * This method correctly constructs the qualified name for both top-level and nested type elements. For top-level
	 * classes, it returns the qualified name directly. For nested classes, it recursively builds the name by traversing
	 * the enclosing element hierarchy.
	 *
	 * <p>
	 * This is necessary because the standard {@code getQualifiedName()} method doesn't work correctly for nested
	 * classes in all annotation processing contexts.
	 *
	 * @param e
	 *            the type element to get the name for
	 * @return the fully qualified name including proper nested class notation
	 */
	public String nameOf(final TypeElement e) {
		if (e.getNestingKind() == NestingKind.TOP_LEVEL) return e.getQualifiedName().toString();
		return nameOf((TypeElement) e.getEnclosingElement()) + "." + e.getSimpleName().toString();
	}

	/**
	 * Sorts elements annotated with the given annotation class to ensure deterministic processing.
	 *
	 * <p>
	 * This method was introduced to handle issue #1671, which required consistent ordering of processed elements across
	 * different compilation runs. The sorting is based on the string representation of elements, providing a stable
	 * ordering that doesn't depend on internal hash codes or memory addresses.
	 *
	 * <p>
	 * Deterministic ordering is crucial for:
	 * <ul>
	 * <li>Reproducible builds across different machines and compiler versions</li>
	 * <li>Consistent generated code output for version control</li>
	 * <li>Reliable testing and debugging of annotation processing</li>
	 * </ul>
	 *
	 * @param annotationClass
	 *            the annotation class to find elements for
	 * @return a sorted list of elements annotated with the given annotation
	 */
	public List<Element> sortElements(final Class<? extends Annotation> annotationClass) {
		final Set<? extends Element> elements = getElementsAnnotatedWith(annotationClass);
		final List<Element> result = new ArrayList<>(elements);
		sort(result, Comparator.comparing(Element::toString));
		return result;
	}

	/**
	 * Groups annotated elements by their root class for organized processing.
	 *
	 * <p>
	 * This method collects all elements annotated with the specified annotation and organizes them into groups based on
	 * their root containing class. This grouping enables processors to handle related elements together and generate
	 * appropriate output organized by class.
	 *
	 * <p>
	 * The root class is determined by traversing the element hierarchy upward until reaching a top-level class (one
	 * that is not enclosed by another class). This ensures that nested classes, methods, and fields are grouped with
	 * their ultimate containing class.
	 *
	 * @param annotationClass
	 *            the annotation class to find elements for
	 * @return a map where keys are root class names and values are lists of elements in those classes
	 */
	public final Map<String, List<Element>> groupElements(final Class<? extends Annotation> annotationClass) {

		// result.forEach((s, l) -> sort(l, (o1, o2) -> o1.toString().compareTo(o2.toString())));
		return getElementsAnnotatedWith(annotationClass).stream().collect(Collectors.groupingBy(this::getRootClassOf));
	}

	/**
	 * Gets the root class of an element by traversing up the enclosing element hierarchy.
	 *
	 * <p>
	 * This method recursively finds the top-level class that contains the given element. It traverses upward through
	 * the enclosing element chain until it finds a class or interface that is not enclosed by another class or
	 * interface.
	 *
	 * <p>
	 * This is used for grouping related elements (methods, fields, nested classes) under their ultimate containing
	 * class for organized processing.
	 *
	 * @param e
	 *            the element to find the root class for
	 * @return the string representation of the root class containing this element
	 */
	private String getRootClassOf(final Element e) {
		final ElementKind kind = e.getKind();
		final Element enclosing = e.getEnclosingElement();
		final ElementKind enclosingKind = enclosing.getKind();
		if ((kind == ElementKind.CLASS || kind == ElementKind.INTERFACE) && enclosingKind != ElementKind.CLASS
				&& enclosingKind != ElementKind.INTERFACE)
			return e.toString();
		return getRootClassOf(enclosing);
	}

	/**
	 * Gets the TypeMirror for a given qualified class name.
	 *
	 * <p>
	 * This method resolves a fully qualified class name to its corresponding TypeMirror representation used in
	 * annotation processing. It delegates to the element utilities to perform the type lookup.
	 *
	 * @param qualifiedName
	 *            the fully qualified name of the class to resolve
	 * @return the TypeMirror for the specified class, or null if not found
	 */
	@Override
	public TypeMirror getType(final String qualifiedName) {
		TypeElement e = delegate.getElementUtils().getTypeElement(qualifiedName);
		if (e == null) return null;
		return e.asType();
	}

	@Override
	public Map<String, String> getOptions() { return delegate.getOptions(); }

	@Override
	public Messager getMessager() { return delegate.getMessager(); }

	@Override
	public Filer getFiler() { return delegate.getFiler(); }

	@Override
	public Elements getElementUtils() { return delegate.getElementUtils(); }

	@Override
	public Types getTypeUtils() { return delegate.getTypeUtils(); }

	@Override
	public SourceVersion getSourceVersion() { return delegate.getSourceVersion(); }

	@Override
	public Locale getLocale() { return delegate.getLocale(); }

	/**
	 * Emits a warning message without a specific source location.
	 *
	 * <p>
	 * This is a convenience method for general warning messages that are not associated with a specific element in the
	 * source code.
	 *
	 * @param s
	 *            the warning message to emit
	 */
	public void emitWarning(final String s) {
		emitWarning(s, (Element) null);
	}

	/**
	 * Emits an error message without a specific source location.
	 *
	 * <p>
	 * This is a convenience method for general error messages that are not associated with a specific element in the
	 * source code.
	 *
	 * @param s
	 *            the error message to emit
	 */
	public void emitError(final String s) {
		emitError(s, (Element) null);
	}

	/**
	 * Emits a warning message associated with a specific source element.
	 *
	 * <p>
	 * This method provides source location context for the warning, helping developers identify the exact location of
	 * the issue in their code.
	 *
	 * @param s
	 *            the warning message to emit
	 * @param e
	 *            the source element associated with the warning, or null for general warnings
	 */
	public void emitWarning(final String s, final Element e) {
		emit(Kind.WARNING, s, e);
	}

	/**
	 * Emits an error message associated with a specific source element.
	 *
	 * <p>
	 * This method provides source location context for the error, helping developers identify the exact location of the
	 * issue in their code. Errors typically cause compilation to fail.
	 *
	 * @param s
	 *            the error message to emit
	 * @param e
	 *            the source element associated with the error, or null for general errors
	 */
	public void emitError(final String s, final Element e) {
		emit(Kind.ERROR, s, e);
	}

	/**
	 * Emits a diagnostic message with the specified severity level.
	 *
	 * <p>
	 * This is the core message emission method that handles both warnings and errors. Messages are prefixed with
	 * "GAML:" to identify them as coming from the GAMA annotation processor. Message emission can be disabled through
	 * configuration.
	 *
	 * @param kind
	 *            the severity level of the message (WARNING, ERROR, NOTE, etc.)
	 * @param s
	 *            the message text to emit
	 * @param e
	 *            the source element associated with the message, or null for general messages
	 */
	public void emit(final Kind kind, final String s, final Element e) {
		if (!PRODUCES_WARNING) return;
		if (e == null) {
			getMessager().printMessage(kind, "GAML: " + s);
		} else {
			getMessager().printMessage(kind, "GAML: " + s, e);
		}
	}

	/**
	 * Emits an error message with exception details but without a specific source element.
	 *
	 * <p>
	 * This method formats and emits an error message that includes the exception message and stack trace for diagnostic
	 * purposes. It is used when an error occurs during processing but is not associated with a specific source element.
	 *
	 * @param s
	 *            the base error message to emit
	 * @param e1
	 *            the exception that occurred
	 */
	public void emitError(final String s, final Exception e1) {
		emit(Kind.ERROR, s, e1, null);
	}

	/**
	 * Emits a warning message with exception details but without a specific source element.
	 *
	 * <p>
	 * This method formats and emits a warning message that includes the exception message and stack trace for
	 * diagnostic purposes. It is used when a non-fatal error occurs during processing but is not associated with a
	 * specific source element.
	 *
	 * @param s
	 *            the base warning message to emit
	 * @param e1
	 *            the exception that occurred
	 */
	public void emitWarning(final String s, final Exception e1) {
		emit(Kind.WARNING, s, e1, null);
	}

	/**
	 * Emits an error message with exception details associated with a specific source element.
	 *
	 * <p>
	 * This method provides the most comprehensive error reporting by including the base message, exception details,
	 * stack trace, and source location information. This helps developers diagnose both what went wrong and where it
	 * occurred in their code.
	 *
	 * @param s
	 *            the base error message to emit
	 * @param e1
	 *            the exception that occurred
	 * @param element
	 *            the source element associated with the error
	 */
	public void emitError(final String s, final Exception e1, final Element element) {
		emit(Kind.ERROR, s, e1, element);
	}

	/**
	 * Emits a warning message with exception details associated with a specific source element.
	 *
	 * <p>
	 * This method provides comprehensive warning reporting by including the base message, exception details, stack
	 * trace, and source location information. This helps developers diagnose both what went wrong and where it occurred
	 * in their code.
	 *
	 * @param s
	 *            the base warning message to emit
	 * @param e1
	 *            the exception that occurred
	 * @param element
	 *            the source element associated with the warning
	 */
	public void emitWarning(final String s, final Exception e1, final Element element) {
		emit(Kind.WARNING, s, e1, element);
	}

	/**
	 * Emits a diagnostic message with exception details and optional source element association.
	 *
	 * <p>
	 * This is the core method for exception-based diagnostic reporting. It constructs a comprehensive message that
	 * includes:
	 * <ul>
	 * <li>The base message provided by the caller</li>
	 * <li>The exception's message</li>
	 * <li>The complete stack trace for debugging</li>
	 * <li>Source location information if an element is provided</li>
	 * </ul>
	 *
	 * <p>
	 * The resulting message provides developers with complete context for diagnosing and fixing issues that occur
	 * during annotation processing.
	 *
	 * @param kind
	 *            the severity level of the message (WARNING, ERROR, NOTE, etc.)
	 * @param s
	 *            the base message text to include
	 * @param e1
	 *            the exception that occurred
	 * @param element
	 *            the source element associated with the message, or null
	 */
	public void emit(final Kind kind, final String s, final Exception e1, final Element element) {
		final StringBuilder sb = new StringBuilder();
		sb.append(s);
		sb.append(e1.getMessage());
		for (final StackTraceElement t : e1.getStackTrace()) {
			sb.append("\n");
			sb.append(t);
		}
		emit(kind, sb.toString(), element);
	}

	/**
	 * Sets the round environment for the current processing round.
	 *
	 * <p>
	 * This method updates the context with information about the current compilation round, including which elements
	 * are being processed. It also extracts and stores the root element names for use in incremental processing
	 * scenarios.
	 *
	 * <p>
	 * The root elements represent the top-level elements (typically classes) that are being compiled in this round,
	 * which is important for cache management and determining when processing is complete.
	 *
	 * @param env
	 *            the round environment for the current compilation round
	 */
	public void setRoundEnvironment(final RoundEnvironment env) {
		round = env;
		roots = round.getRootElements().stream().map(Element::toString).toList();
	}

	@Override
	public boolean processingOver() {
		return round.processingOver();
	}

	@Override
	public boolean errorRaised() {
		return round.errorRaised();
	}

	@Override
	public Set<? extends Element> getRootElements() { return round.getRootElements(); }

	@Override
	public Set<? extends Element> getElementsAnnotatedWith(final TypeElement a) {
		return round.getElementsAnnotatedWith(a);
	}

	@Override
	public Set<? extends Element> getElementsAnnotatedWith(final Class<? extends Annotation> a) {
		return round.getElementsAnnotatedWith(a);
	}

	/**
	 * Creates a writer for a resource file with the specified name.
	 *
	 * <p>
	 * This method creates a writer that can be used to generate resource files during annotation processing. The file
	 * is created in the standard output location and can be used for any type of resource generation.
	 *
	 * @param s
	 *            the name of the resource file to create
	 * @return a Writer for the created file, or null if creation failed
	 */
	public Writer createWriter(final String s) {
		try {
			final OutputStream output = getFiler().createResource(OUT, "", s, (Element[]) null).openOutputStream();
			return new OutputStreamWriter(output, CHARSET);
		} catch (final Exception e) {
			emitWarning("", e);
		}
		return null;
	}

	/**
	 * Initializes the current plugin information by analyzing file paths.
	 *
	 * <p>
	 * This method determines the current GAMA plugin being processed by creating a temporary source file and examining
	 * its URI path. The plugin name is extracted from the path structure, which enables proper package naming and file
	 * organization for generated code.
	 *
	 * <p>
	 * The method sets both the full plugin name and a shortcut identifier that is used for generating unique class and
	 * package names within the plugin.
	 *
	 * <p>
	 * If plugin detection fails, a warning is emitted but processing can continue with default naming schemes.
	 */
	void initCurrentPlugin() {
		// Only initialize if not already done
		if (shortcut != null) {
			return;
		}
		
		try {
			final FileObject temp = getFiler().createSourceFile("gaml.additions.package-info", (Element[]) null);
			emit(Kind.NOTE, "GAML Processor: creating " + temp.toUri(), (Element) null);
			final String plugin2 = temp.toUri().toASCIIString().replace("/target/gaml/additions/package-info.java", "")
					.replace("/gaml/gaml/additions/package-info.java", "");
			currentPlugin = plugin2.substring(plugin2.lastIndexOf('/') + 1);
			shortcut = currentPlugin.substring(currentPlugin.lastIndexOf('.') + 1);
			emit(Kind.NOTE, "GAML Processor: Plugin identified as '" + currentPlugin + "' (shortcut: '" + shortcut + "')", (Element) null);
		} catch (IOException e) {
			emitWarning("Exception raised while reading the current plugin name " + e.getMessage(), e);
		}
	}

	/**
	 * Creates a source file for the GamlAdditions class and returns the file object.
	 *
	 * <p>
	 * This method is responsible for creating the main generated source file that will contain all the GAMA element
	 * registration code. It first initializes the current plugin information and then creates the appropriately named
	 * source file.
	 *
	 * @return the FileObject for the created source file, or null if creation failed
	 */
	public FileObject createSource() {
		initCurrentPlugin();
		try {

			return getFiler().createSourceFile(ADDITIONS_PACKAGE_BASE + "." + shortcut + "." + ADDITIONS_CLASS_NAME,
					(Element[]) null);
		} catch (final Exception e) {
			emitWarning("Exception raised while creating the source file: " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Creates a test writer using the default test file name.
	 *
	 * <p>
	 * This convenience method creates a writer for the default test file for the current plugin. The file name is
	 * automatically generated based on the plugin name.
	 *
	 * @return a Writer for the test file
	 */
	public Writer createTestWriter() {
		return createTestWriter(getTestFileName());
	}

	/**
	 * Creates a test writer for a specific test file name.
	 *
	 * <p>
	 * This method creates a writer for generating test files in the appropriate test folder structure. It ensures the
	 * test folder exists and creates the file in the proper location for the GAMA test framework to discover and
	 * execute.
	 *
	 * @param fileName
	 *            the name of the test file to create
	 * @return a Writer for the test file, or null if creation failed
	 */
	public Writer createTestWriter(final String fileName) {
		createTestsFolder();
		try {
			final OutputStream output =
					getFiler().createResource(OUT, getTestFolderName() + ".models", fileName, (Element[]) null)
							.openOutputStream();
			return new OutputStreamWriter(output, CHARSET);
		} catch (final Exception e) {
			e.printStackTrace();
			emitWarning("Impossible to create test file " + fileName + ": ", e);
		}
		return null;
	}

	/**
	 * Gets the test file name for the current plugin.
	 *
	 * <p>
	 * Generates a test file name based on the current plugin name, following the convention of "{PluginName}
	 * Tests.experiment".
	 *
	 * @return the generated test file name
	 */
	private String getTestFileName() {
		final String title = currentPlugin.substring(currentPlugin.lastIndexOf('.') + 1);
		return Constants.capitalizeFirstLetter(title) + " Tests.experiment";
	}

	/**
	 * Gets the test folder name.
	 *
	 * @return the test folder name
	 */
	private String getTestFolderName() {
		final String title = currentPlugin.substring(currentPlugin.lastIndexOf('.') + 1);
		return "tests.Generated From " + Constants.capitalizeFirstLetter(title);
	}

	/**
	 * Creates the tests folder and associated project configuration.
	 *
	 * <p>
	 * This method creates a complete Eclipse project structure for test files including:
	 * <ul>
	 * <li>Project folder structure</li>
	 * <li>.project file with proper natures and build commands</li>
	 * <li>GAMA and Xtext configuration for test execution</li>
	 * </ul>
	 *
	 * <p>
	 * The created project enables the GAMA test framework to discover and execute the generated test files
	 * automatically.
	 */
	public void createTestsFolder() {
		FileObject obj = null;
		try {
			obj = getFiler().createResource(OUT, getTestFolderName(), ".project", (Element[]) null);
		} catch (final FilerException e) {
			// Already exists. Simply return
			return;
		} catch (final IOException e) {
			// More serious problem
			emitWarning("Cannot create tests folder: ", e);
			return;
		}
		try (final OutputStream output = obj.openOutputStream();
				final Writer writer = new OutputStreamWriter(output, CHARSET);) {
			writer.append("""
					<?xml version="1.0" encoding="UTF-8"?>
					<projectDescription>
						<name>Generated tests in %s</name>
						<comment>%s</comment>
						<projects></projects>
						<buildSpec>
							<buildCommand>
								<name>org.eclipse.xtext.ui.shared.xtextBuilder</name>
								<arguments></arguments>
							</buildCommand>
						</buildSpec>
						<natures>
							<nature>org.eclipse.xtext.ui.shared.xtextNature</nature>
							<nature>gama.workspace.gamaNature</nature>
							<nature>gama.workspace.testNature</nature>
						</natures>
					</projectDescription>
					""".formatted(currentPlugin, currentPlugin));

		} catch (final IOException t) {
			emitWarning("", t);
		}
	}

	/**
	 * Creates the source writer.
	 *
	 * @param file
	 *            the file
	 * @return the writer
	 */
	public Writer createSourceWriter(final FileObject file) {
		try {
			final OutputStream output = file.openOutputStream();
			return new OutputStreamWriter(output, CHARSET);
		} catch (final Exception e) {
			emitWarning("Error in creating source writer", e);
		}
		return null;
	}

	/**
	 * Should produce doc.
	 *
	 * @return true, if successful
	 */
	public boolean shouldProduceDoc() {
		return "true".equals(getOptions().get("doc")) || PRODUCES_DOC;
	}

	/**
	 * Gets the input stream.
	 *
	 * @param string
	 *            the string
	 * @return the input stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public InputStream getInputStream(final String string) throws IOException {
		return getFiler().getResource(ProcessorContext.OUT, "", string).openInputStream();
	}

	/**
	 * Gets the useful annotations on.
	 *
	 * @param e
	 *            the e
	 * @return the useful annotations on
	 */
	public List<Annotation> getUsefulAnnotationsOn(final Element e) {
		final List<Annotation> result = new ArrayList<>();
		for (final Class<? extends Annotation> clazz : processors.keySet()) {
			final Annotation a = e.getAnnotation(clazz);
			if (a != null) { result.add(a); }
		}
		return result;
	}

	/**
	 * Gets the roots.
	 *
	 * @return the roots
	 */
	public List<String> getRoots() { return roots; }

	/**
	 * Contains import.
	 *
	 * @param path
	 *            the path
	 * @return true, if successful
	 */
	public boolean containsImport(final String path) {
		return COLLECTIVE_IMPORTS.contains(path);
	}

	/**
	 * Checks if is i type.
	 *
	 * @param type
	 *            the type
	 * @return true, if is i type
	 */
	public boolean isIType(final TypeMirror type) {
		return delegate.getTypeUtils().isSubtype(type, getIType());
	}

	/**
	 * Sets the reference to the parent GamaProcessor.
	 * This allows the context to record import usage in the persistent cache.
	 *
	 * @param processor the GamaProcessor instance
	 */
	public void setProcessor(final GamaProcessor processor) {
		this.processor = processor;
	}

	/**
	 * Records that a class is being used in the generated code.
	 * This method updates the persistent cache in GamaProcessor, ensuring that
	 * imports are preserved across incremental compilation rounds.
	 * 
	 * This now tracks the EXACT class name (not just the package) for precise imports.
	 * 
	 * Note: Static imports are NOT tracked - they're always included as they're
	 * fundamental to the generated code (IKeyword constants, Cast methods).
	 *
	 * @param fullyQualifiedClassName the fully qualified class name (e.g., "gama.api.kernel.agent.IAgent")
	 */
	public void recordImportUsage(final String fullyQualifiedClassName) {
		if (processor != null && fullyQualifiedClassName != null && !fullyQualifiedClassName.isEmpty()) {
			processor.getUsedImports().add(fullyQualifiedClassName);
			importTrackingCount++;
			// Debug: log first few imports
			if (importTrackingCount <= 5) {
				emit(javax.tools.Diagnostic.Kind.NOTE, "Tracking import #" + importTrackingCount + 
					" for plugin '" + (shortcut != null ? shortcut : "NULL") + "': " + fullyQualifiedClassName, (Element) null);
			}
		}
	}
	
	/**
	 * Gets the import tracking count for debugging purposes.
	 *
	 * @return the number of times recordImportUsage was called
	 */
	public int getImportTrackingCount() {
		return importTrackingCount;
	}

	/**
	 * Adds a package to the dynamic collective imports for the current plugin.
	 *
	 * <p>
	 * This method allows processors to register packages that should be imported in the generated GamlAdditions class.
	 * The package will be added with a wildcard import (e.g., "com.example.package.*").
	 *
	 * @param packageName
	 *            the package name to add to imports (without trailing dot or asterisk)
	 */
	public void addDynamicCollectiveImport(final String packageName) {
		if (packageName != null && !packageName.isEmpty()) {
			// Normalize package name and add dot for wildcard import
			String normalizedPackage = packageName.endsWith(".") ? packageName : packageName + ".";
			dynamicCollectiveImports.add(normalizedPackage);
		}
	}

	/**
	 * Adds a static import to the dynamic static imports for the current plugin.
	 *
	 * <p>
	 * This method allows processors to register static imports that should be imported in the generated GamlAdditions
	 * class. The import will be added as a static wildcard import (e.g., "static com.example.Class.*").
	 *
	 * @param className
	 *            the fully qualified class name to add as static import (without trailing dot or asterisk)
	 */
	public void addDynamicStaticImport(final String className) {
		if (className != null && !className.isEmpty()) {
			// Normalize class name and add dot for wildcard import
			String normalizedClass = className.endsWith(".") ? className : className + ".";
			dynamicStaticImports.add(normalizedClass);
		}
	}

	/**
	 * Gets all collective imports for the current plugin, including both static base imports and dynamically discovered
	 * plugin-specific imports.
	 *
	 * <p>
	 * The returned set includes:
	 * <ul>
	 * <li>All standard GAMA collective imports from Constants</li>
	 * <li>Any plugin-specific packages discovered during processing</li>
	 * </ul>
	 *
	 * @return a combined set of all collective imports for this plugin
	 */
	public Set<String> getAllCollectiveImports() {
		return COLLECTIVE_IMPORTS;
		// Set<String> allImports = new LinkedHashSet<>(COLLECTIVE_IMPORTS);
		// allImports.addAll(dynamicCollectiveImports);
		// return allImports;
	}

	/**
	 * Gets all static collective imports for the current plugin, including both static base imports and dynamically
	 * discovered plugin-specific static imports.
	 *
	 * <p>
	 * The returned set includes:
	 * <ul>
	 * <li>All standard GAMA static collective imports from Constants</li>
	 * <li>Any plugin-specific static classes discovered during processing</li>
	 * </ul>
	 *
	 * @return a combined set of all static collective imports for this plugin
	 */
	public Set<String> getAllStaticCollectiveImports() {
		return STATIC_COLLECTIVE_IMPORTS;
		// Set<String> allStaticImports = new LinkedHashSet<>(STATIC_COLLECTIVE_IMPORTS);
		// allStaticImports.addAll(dynamicStaticImports);
		// return allStaticImports;
	}

	// /**
	// * Discovers and adds packages from annotated elements found during processing.
	// *
	// * <p>
	// * This method analyzes all elements being processed and automatically discovers packages that should be imported.
	// * It examines:
	// * <ul>
	// * <li>The packages of all annotated classes</li>
	// * <li>Return types of methods</li>
	// * <li>Parameter types</li>
	// * <li>Field types</li>
	// * </ul>
	// *
	// * <p>
	// * Packages are only added if they are not already covered by the standard GAMA imports and are not from the
	// * java.lang package.
	// */
	// public void discoverPluginPackages() {
	// // Get all annotated elements
	// for (Class<? extends Annotation> annotationClass : processors.keySet()) {
	// List<Element> elements = sortElements(annotationClass);
	// for (Element element : elements) { discoverPackagesFromElement(element); }
	// }
	// }
	//
	// /**
	// * Discovers packages from a specific element and adds them to dynamic imports.
	// *
	// * @param element
	// * the element to analyze for package discovery
	// */
	// private void discoverPackagesFromElement(final Element element) {
	// // Get package of the element itself
	// String elementPackage = extractPackageFromElement(element);
	// if (shouldAddPackage(elementPackage)) { addDynamicCollectiveImport(elementPackage); }
	//
	// // For methods, also examine parameter and return types
	// if (element.getKind() == ElementKind.METHOD) {
	// ExecutableElement method = (ExecutableElement) element;
	//
	// // Check return type
	// String returnTypePackage = extractPackageFromTypeMirror(method.getReturnType());
	// if (shouldAddPackage(returnTypePackage)) { addDynamicCollectiveImport(returnTypePackage); }
	//
	// // Check parameter types
	// for (VariableElement param : method.getParameters()) {
	// String paramPackage = extractPackageFromTypeMirror(param.asType());
	// if (shouldAddPackage(paramPackage)) { addDynamicCollectiveImport(paramPackage); }
	// }
	// }
	//
	// // For fields, examine the field type
	// if (element.getKind() == ElementKind.FIELD) {
	// VariableElement field = (VariableElement) element;
	// String fieldTypePackage = extractPackageFromTypeMirror(field.asType());
	// if (shouldAddPackage(fieldTypePackage)) { addDynamicCollectiveImport(fieldTypePackage); }
	// }
	// }
	//
	// /**
	// * Extracts the package name from an element.
	// *
	// * @param element
	// * the element to extract package from
	// * @return the package name, or null if not extractable
	// */
	// private String extractPackageFromElement(final Element element) {
	// Element topLevel = element;
	// while (topLevel.getEnclosingElement() != null
	// && topLevel.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
	// topLevel = topLevel.getEnclosingElement();
	// }
	//
	// if (topLevel.getEnclosingElement() != null && topLevel.getEnclosingElement().getKind() == ElementKind.PACKAGE)
	// return topLevel.getEnclosingElement().toString();
	// return null;
	// }
	//
	// /**
	// * Extracts the package name from a TypeMirror.
	// *
	// * @param typeMirror
	// * the type to extract package from
	// * @return the package name, or null if not extractable
	// */
	// private String extractPackageFromTypeMirror(final TypeMirror typeMirror) {
	// String typeName = typeMirror.toString();
	//
	// // Remove generics
	// int genericIndex = typeName.indexOf('<');
	// if (genericIndex != -1) { typeName = typeName.substring(0, genericIndex); }
	//
	// // Remove arrays
	// while (typeName.endsWith("[]")) { typeName = typeName.substring(0, typeName.length() - 2); }
	//
	// // Extract package
	// int lastDotIndex = typeName.lastIndexOf('.');
	// if (lastDotIndex > 0) return typeName.substring(0, lastDotIndex);
	// return null;
	// }
	//
	// /**
	// * Determines if a package should be added to dynamic imports.
	// *
	// * @param packageName
	// * the package name to check
	// * @return true if the package should be added, false otherwise
	// */
	// private boolean shouldAddPackage(final String packageName) {
	// // Don't add java.lang or primitive packages
	// if (packageName == null || packageName.isEmpty() || "java.lang".equals(packageName) ||
	// packageName.startsWith("java.lang.")) return false;
	//
	// // Don't add packages already covered by existing imports
	// String packageWithDot = packageName + ".";
	// if (COLLECTIVE_IMPORTS.contains(packageWithDot)) return false;
	//
	// // Check if any existing import covers this package
	// for (String existingImport : COLLECTIVE_IMPORTS) {
	// if (packageName.startsWith(existingImport.substring(0, existingImport.length() - 1))) return false;
	// }
	//
	// return true;
	// }

}