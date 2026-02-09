/*******************************************************************************************************
 *
 * ElementProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import gama.annotations.arg;
import gama.annotations.constant;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.file;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.skill;
import gama.annotations.species;
import gama.annotations.symbol;
import gama.annotations.test;
import gama.annotations.tests;
import gama.annotations.type;
import gama.annotations.usage;
import gama.annotations.variable;
import gama.processor.Constants;
import gama.processor.IProcessor;
import gama.processor.ProcessorContext;

/**
 * The ElementProcessor is the abstract base class for all annotation processors in the GAMA processor system.
 * 
 * <p>This class provides the fundamental infrastructure for processing annotations during the compilation phase
 * and generating the necessary runtime registration code. Each concrete processor extends this class to handle
 * a specific type of annotation (e.g., {@code @action}, {@code @operator}, {@code @symbol}, etc.).
 * 
 * <p>The processor system works by:
 * <ol>
 * <li>Scanning source code for elements annotated with specific annotations</li>
 * <li>Validating the annotated elements and their annotations</li>
 * <li>Generating helper code that registers these elements with the GAMA runtime</li>
 * <li>Collecting and organizing the generated code by source root</li>
 * </ol>
 * 
 * <p>Key features provided by this base class:
 * <ul>
 * <li><strong>Element Processing:</strong> Core logic for finding and processing annotated elements</li>
 * <li><strong>Code Generation:</strong> Utilities for generating Java code strings and type conversions</li>
 * <li><strong>Validation:</strong> Documentation validation, test verification, and type checking</li>
 * <li><strong>Caching:</strong> Name caching and efficient string building for performance</li>
 * <li><strong>Error Handling:</strong> Comprehensive error reporting with source location information</li>
 * </ul>
 * 
 * <h3>Implementing a New Processor:</h3>
 * <p>To create a new annotation processor, extend this class and implement the abstract methods:
 * <pre>{@code
 * public class MyProcessor extends ElementProcessor<MyAnnotation> {
 *     @Override
 *     protected Class<MyAnnotation> getAnnotationClass() {
 *         return MyAnnotation.class;
 *     }
 *     
 *     @Override
 *     public void createElement(StringBuilder sb, Element e, MyAnnotation annotation) {
 *         // Generate registration code
 *         sb.append("// Generated code for ").append(e.getSimpleName());
 *     }
 * }
 * }</pre>
 * 
 * <h3>Code Generation Utilities:</h3>
 * <p>The class provides numerous utility methods for code generation:
 * <ul>
 * <li>{@link #toJavaString(String)} - Converts strings to Java string literals</li>
 * <li>{@link #toClassObject(String)} - Generates Class&lt;?&gt; references</li>
 * <li>{@link #rawNameOf(TypeMirror)} - Extracts raw type names</li>
 * <li>{@link #checkPrim(String)} - Handles primitive type mapping</li>
 * </ul>
 * 
 * @param <T> the type of annotation this processor handles
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see IProcessor
 * @see ProcessorContext
 */
public abstract class ElementProcessor<T extends Annotation> implements IProcessor<T>, Constants {

	/**
	 * Cache for frequently used type names to improve performance during code generation.
	 * Maps full qualified class names to their processed forms.
	 */
	protected static final Map<String, String> NAME_CACHE = new HashMap<>();

	/**
	 * Reusable StringBuilder for string concatenation operations.
	 * Used by the {@link #concat(String...)} method to avoid creating multiple temporary objects.
	 */
	static final StringBuilder CONCAT = new StringBuilder();

	/**
	 * Map containing the serialized code elements organized by source root.
	 * Each entry represents generated code for a specific source root in the compilation.
	 */
	protected final SortedMap<String, StringBuilder> serializedElements = new TreeMap<>();

	/**
	 * Pattern for matching class parameter declarations (e.g., "&lt;String, Integer&gt;").
	 * Used to clean generic type information from class names during code generation.
	 */
	static final Pattern CLASS_PARAM = Pattern.compile("<.*?>");

	/**
	 * Pattern for matching double quotes in strings.
	 * Used for escaping quotes in generated Java string literals.
	 */
	static final Pattern SINGLE_QUOTE = Pattern.compile("\"");

	/**
	 * Replacement string for escaped quotes in Java string literals.
	 * Used with {@link #SINGLE_QUOTE} pattern to properly escape strings.
	 */
	static final String QUOTE_MATCHER = Matcher.quoteReplacement("\\\"");

	/**
	 * The name of the initialization method to be generated.
	 * Set during processing and used in the final code generation phase.
	 */
	protected String initializationMethodName;

	/**
	 * The current processing context providing access to type utilities and error reporting.
	 * Set during the {@link #process(ProcessorContext)} method execution.
	 */
	protected ProcessorContext context;

	/**
	 * Efficiently concatenates an array of strings using a shared StringBuilder.
	 * 
	 * <p>This method provides a performance-optimized way to concatenate multiple strings
	 * by reusing a static StringBuilder instance. The buffer is cleared after each use
	 * to ensure no memory leaks.
	 * 
	 * <p>This is preferred over String concatenation or String.join() for performance
	 * reasons in the code generation context where many string operations are performed.
	 * 
	 * @param array the array of strings to concatenate
	 * @return the concatenated string
	 */
	protected final static String concat(final String... array) {
		for (final String element : array) { CONCAT.append(element); }
		final String result = CONCAT.toString();
		CONCAT.setLength(0);
		return result;
	}

	/**
	 * Instantiates a new element processor.
	 */
	public ElementProcessor() {}

	/**
	 * Clean.
	 *
	 * @param context
	 *            the context
	 * @param map
	 *            the map
	 */
	protected void clean(final ProcessorContext context, final Map<String, StringBuilder> map) {
		for (final String k : context.getRoots()) { map.remove(k); }
	}

	@Override
	public TypeMirror getType(final String classQualifiedName) {
		return context.getType(classQualifiedName);
	}

	@Override
	public boolean hasElements() {
		return serializedElements.size() > 0;
	}

	/**
	 * Processes all elements annotated with this processor's annotation type.
	 * 
	 * <p>This is the main entry point for annotation processing. The method:
	 * <ol>
	 * <li>Sets up the processing context</li>
	 * <li>Cleans up any previous processing results for the current roots</li>
	 * <li>Groups elements by their source root</li>
	 * <li>Processes each element by calling {@link #createElement(StringBuilder, Element, Annotation)}</li>
	 * <li>Collects the generated code in {@link #serializedElements}</li>
	 * </ol>
	 * 
	 * <p>Error handling is built-in: if any element processing fails, an error is reported
	 * through the context but processing continues for other elements.
	 * 
	 * @param context the processing context providing type information and error reporting
	 * @throws ProcessingException if critical processing errors occur
	 */
	@Override
	public void process(final ProcessorContext context) {
		this.context = context;
		final Class<T> a = getAnnotationClass();
		clean(context, serializedElements);
		for (final Map.Entry<String, List<Element>> entry : context.groupElements(a).entrySet()) {
			final List<Element> elements = entry.getValue();
			if (elements.size() == 0) { continue; }
			final StringBuilder sb = new StringBuilder();
			for (final Element e : elements) {
				try {
					if (validateElement(e)) { createElement(sb, e, e.getAnnotation(a)); }
				} catch (final Exception exception) {
					context.emitError("Exception in processor: " + exception.getMessage(), exception, e);
				}
			}
			if (sb.length() > 0) { serializedElements.put(entry.getKey(), sb); }
		}
	}

	/** The Constant NULL_DOCS. */
	static final doc[] NULL_DOCS = {};

	/**
	 * Checks if is internal.
	 *
	 * @param main
	 *            the main
	 * @param a
	 *            the a
	 * @return true, if is internal
	 */
	protected boolean isInternal(final Element main, final Annotation a) {
		return switch (a) {
			case species s -> s.internal();
			case symbol s -> s.internal();
			case operator s -> s.internal();
			case skill s -> s.internal();
			case facet s -> s.internal();
			case type s -> s.internal();
			case variable s -> s.internal();
			case null, default -> false;
		};
	}

	/**
	 * Gets the doc annotation.
	 *
	 * @param main
	 *            the main
	 * @param a
	 *            the a
	 * @return the doc annotation
	 */
	protected doc getDocAnnotation(final Element main, final Annotation a) {
		doc[] docs = switch (a) {
			case species s -> s.doc();
			case symbol s -> s.doc();
			case arg s -> s.doc();
			case constant s -> s.doc();
			case operator s -> s.doc();
			case skill s -> s.doc();
			case facet s -> s.doc();
			case type s -> s.doc();
			case file s -> s.doc();
			case variable s -> s.doc();
			case null, default -> null;
		};
		return docs == null || docs.length == 0 ? main.getAnnotation(doc.class) : docs[0];
	}

	/**
	 * Checks if is deprecated.
	 *
	 * @param e
	 *            the e
	 * @param a
	 *            the a
	 * @return true, if is deprecated
	 */
	protected boolean isDeprecated(final Element e, final Annotation a) {
		final doc d = getDocAnnotation(e, a);
		if (d == null) return false;
		return d.deprecated().length() > 0;
	}

	/**
	 * Checks for tests.
	 *
	 * @param examples
	 *            the examples
	 * @return true, if successful
	 */
	public boolean hasTests(final example[] examples) {
		for (final example ex : examples) { if (ex.isTestOnly() || ex.isExecutable() && ex.test()) return true; }
		return false;
	}

	/**
	 * Checks for tests.
	 *
	 * @param e
	 *            the e
	 * @param a
	 *            the a
	 * @return true, if successful
	 */
	public boolean hasTests(final Element e, final Annotation a) {
		// if the artifact is internal, skip the verification
		if (isInternal(e, a)) return true;
		final no_test no = e.getAnnotation(no_test.class);
		// if no tests are necessary, skip the verification
		if (no != null) return true;
		final tests tests = e.getAnnotation(tests.class);
		if (tests != null) return true;
		final test test = e.getAnnotation(test.class);
		if (test != null) return true;
		final doc doc = getDocAnnotation(e, a);
		if (doc == null) return false;
		if (hasTests(doc.examples())) return true;
		for (final usage us : doc.usages()) { if (hasTests(us.examples())) return true; }
		return doc.deprecated().length() > 0;
	}

	/**
	 * Verify doc.
	 *
	 * @param context
	 *            the context
	 * @param e
	 *            the e
	 * @param displayedName
	 *            the displayed name
	 * @param a
	 *            the a
	 */
	protected void verifyDoc(final Element e, final String displayedName, final Annotation a) {
		if (isInternal(e, a)) return;
		final doc d = getDocAnnotation(e, a);
		boolean docMissing = d == null;
		if (d != null) {
			if (d.value().length() == 0 && d.deprecated().length() == 0 && d.usages().length == 0
					&& d.special_cases().length == 0 && d.examples().length == 0) {
				docMissing = true;
			}
		}
		if (docMissing) { context.emitWarning("documentation missing for " + displayedName, e); }

	}

	/**
	 * Serialize.
	 *
	 * @param elements
	 *            the elements
	 * @param sb
	 *            the sb
	 */
	@Override
	public void serialize(final Collection<StringBuilder> elements, final StringBuilder sb) {
		elements.forEach(builder -> { if (builder != null) { sb.append(builder); } });
	}

	/**
	 * Write method.
	 *
	 * @param method
	 *            the method
	 * @param followingMethod
	 *            the following method
	 * @param elements
	 *            the elements
	 * @param sb
	 *            the sb
	 * @param context
	 *            the context
	 */
	private void writeMethod(final String method, final String followingMethod,
			final Collection<StringBuilder> elements, final StringBuilder sb) {
		sb.append("public void ").append(method).append("() ").append(getExceptions()).append(" {");
		serialize(elements, sb);
		if (followingMethod != null) { sb.append(ln).append(followingMethod).append("(); "); }
		sb.append(ln).append("}");
	}

	/**
	 * Write java body.
	 *
	 * @param sb
	 *            the sb
	 */
	@Override
	public void writeJavaBody(final StringBuilder sb) {
		final String method = getInitializationMethodName();
		if (method == null) return;
		final int size = sizeOf(serializedElements);
		if (size > 20000) {
			writeMethod(method, method + "2", halfOf(serializedElements, true), sb);
			writeMethod(method + "2", null, halfOf(serializedElements, false), sb);
		} else {
			writeMethod(method, null, serializedElements.values(), sb);
		}
	}

	/**
	 * Size of.
	 *
	 * @param elements
	 *            the elements
	 * @return the int
	 */
	private int sizeOf(final Map<String, StringBuilder> elements) {
		return elements.values().stream().filter(e -> e != null).mapToInt(StringBuilder::length).sum();
	}

	/**
	 * Half of.
	 *
	 * @param elements
	 *            the elements
	 * @param firstHalf
	 *            the first half
	 * @return the list
	 */
	private List<StringBuilder> halfOf(final Map<String, StringBuilder> elements, final boolean firstHalf) {
		final int size = elements.size();
		final List<StringBuilder> result = new ArrayList<>(elements.values());
		return firstHalf ? result.subList(0, size / 2) : result.subList(size / 2, size);

	}

	/**
	 * Creates the element.
	 *
	 * @param sb
	 *            the sb
	 * @param context
	 *            the context
	 * @param e
	 *            the e
	 * @param annotation
	 *            the annotation
	 */
	public abstract void createElement(StringBuilder sb, Element e, T annotation);

	/**
	 * Gets the annotation class.
	 *
	 * @return the annotation class
	 */
	protected abstract Class<T> getAnnotationClass();

	@Override
	public final String getInitializationMethodName() {
		if (initializationMethodName == null) {
			initializationMethodName =
					"initialize" + Constants.capitalizeFirstLetter(getAnnotationClass().getSimpleName());
		}
		return initializationMethodName;
	}

	/**
	 * To java string.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	protected static String toJavaString(final String s) {
		if (s == null || s.isEmpty()) return "(String)null";
		final int i = ss1.indexOf(s);
		return i == -1 ? "\"" + s + "\"" : ss2.get(i);
	}

	/**
	 * To class object.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	protected final String toClassObject(final String s) {
		// Check if we have a predefined short form
		final String result = CLASS_NAMES.get(s);
		if (result != null) return result;
		
		// Track that this class is being used (important for minimal imports)
		if (context != null) {
			context.trackClassUsage(s);
		}
		
		// Use context to get the appropriate class name (simple if imported, qualified if not)
		String className = context != null ? context.getClassNameForGeneration(s) : s;
		return className + ".class";
	}

	/**
	 * Static version of toClassObject for use in static methods.
	 * This version always uses fully qualified names since it doesn't have access to context.
	 *
	 * @param s the class name
	 * @return the class object string
	 */
	protected final static String toClassObjectStatic(final String s) {
		final String result = CLASS_NAMES.get(s);
		return result == null ? s + ".class" : result;
	}

	/**
	 * Gets the class name for direct use in generated code (without .class suffix).
	 * Uses simple names when packages are imported.
	 *
	 * @param fullyQualifiedName the fully qualified class name
	 * @return the class name to use in generated code
	 */
	protected final String getClassName(final String fullyQualifiedName) {
		// Track that this class is being used (important for minimal imports)
		if (context != null) {
			context.trackClassUsage(fullyQualifiedName);
		}
		return context != null ? context.getClassNameForGeneration(fullyQualifiedName) : fullyQualifiedName;
	}

	/**
	 * Registers the package of a class for import and returns the appropriate class name to use.
	 * This is a convenience method that combines package registration with class name generation.
	 * 
	 * @param fullyQualifiedClassName the fully qualified class name
	 * @return the class name to use in generated code (simple name after registering package)
	 */
	protected final String registerAndGetClassName(final String fullyQualifiedClassName) {
		if (fullyQualifiedClassName == null) return null;
		
		// Extract and register the package
		int lastDotIndex = fullyQualifiedClassName.lastIndexOf('.');
		if (lastDotIndex > 0) {
			String packageName = fullyQualifiedClassName.substring(0, lastDotIndex);
			registerPackageForImport(packageName);
		}
		
		return getClassName(fullyQualifiedClassName);
	}

	/**
	 * Generates a static method call, using simple syntax when static imports are available.
	 * 
	 * @param className the fully qualified class name containing the static method
	 * @param methodName the static method name
	 * @param args the method arguments (as a string)
	 * @return the appropriate method call syntax
	 */
	protected final String generateStaticMethodCall(final String className, final String methodName, final String args) {
		if (context != null) {
			// Track that we're using this static class
			context.trackStaticClassUsage(className);
			
			// Check if we can use simple syntax due to static imports
			if (context.canUseStaticMethodDirectly(className, methodName)) {
				return methodName + "(" + args + ")";
			}
		}
		
		// Use qualified syntax
		String simpleClassName = getClassName(className);
		return simpleClassName + "." + methodName + "(" + args + ")";
	}

	/**
	 * To array of strings.
	 *
	 * @param segments
	 *            the segments
	 * @param sb
	 *            the sb
	 * @return the string builder
	 */
	protected final static StringBuilder toArrayOfStrings(final String[] segments, final StringBuilder sb) {
		if (segments == null || segments.length == 0) {
			sb.append("AS");
			return sb;
		}
		sb.append("S(");
		for (int i = 0; i < segments.length; i++) {
			if (i > 0) { sb.append(','); }
			sb.append(toJavaString(segments[i]));
		}
		sb.append(')');
		return sb;
	}

	/**
	 * Check prim.
	 *
	 * @param c
	 *            the c
	 * @return the string
	 */
	protected static String checkPrim(final String c) {
		return CHECK_PRIM.getOrDefault(c, c);
	}

	/**
	 * Return when null.
	 *
	 * @param returnClass
	 *            the return class
	 * @return the string
	 */
	protected static String returnWhenNull(final String returnClass) {
		return RETURN_WHEN_NULL.getOrDefault(returnClass, " null");
	}

	/**
	 * Param.
	 *
	 * @param sb
	 *            the sb
	 * @param c
	 *            the c
	 * @param par
	 *            the par
	 */
	protected static void param(final StringBuilder sb, final String c, final String par) {
		final String jc = checkPrim(c);
		switch (jc) {
			case DOUBLE -> sb.append("asFloat(s,").append(par).append(')');
			case INTEGER -> sb.append("asInt(s,").append(par).append(')');
			case BOOLEAN -> sb.append("asBool(s,").append(par).append(')');
			case OBJECT -> sb.append(par);
			default -> sb.append("((").append(jc).append(")").append(par).append(')');
		}

	}

	/**
	 * Escape double quotes.
	 *
	 * @param input
	 *            the input
	 * @return the string
	 */
	protected static String escapeDoubleQuotes(final String input) {
		if (input == null) return "";
		return SINGLE_QUOTE.matcher(input).replaceAll(QUOTE_MATCHER);
	}

	/**
	 * To array of ints.
	 *
	 * @param array
	 *            the array
	 * @param sb
	 *            the sb
	 * @return the string builder
	 */
	public static StringBuilder toArrayOfInts(final int[] array, final StringBuilder sb) {
		if (array == null || array.length == 0) {
			sb.append("AI");
			return sb;
		}
		sb.append("I(");
		for (final int i : array) { sb.append(i).append(","); }
		sb.setLength(sb.length() - 1);
		sb.append(")");
		return sb;
	}

	/**
	 * Returns the "raw" name of a type (qualified name w/o the type parameters). Additionally compares the resulting
	 * name to the elements in GamaProcessor.IMPORTS_SET and removes the path if it is imported.
	 *
	 * @param context
	 *            the context
	 * @param t
	 *            the t
	 * @return the string
	 */
	String rawNameOf(final TypeMirror t) {
		if (TypeKind.VOID.equals(t.getKind())) return "void";
		final String key = t.toString();
		String cachedName = NAME_CACHE.get(key);
		if (cachedName != null) {
			// Track usage even for cached names
			if (context != null) {
				context.trackClassUsage(cachedName);
			}
			return cachedName;
		}
		String type = context.getTypeUtils().erasure(t).toString();
		// As a workaround for ECJ/javac discrepancies regarding erasure
		type = CLASS_PARAM.matcher(type).replaceAll("");
		
		// Use the new dynamic import system to get the appropriate class name
		String result = context.getClassNameForGeneration(type);
		NAME_CACHE.put(key, result);
		return result;
	}

	/**
	 * To boolean.
	 *
	 * @param b
	 *            the b
	 * @return the string
	 */
	protected String toBoolean(final boolean b) {
		return b ? "T" : "F";
	}

	// -------------- VALIDATION METHODS

	/**
	 * A method that allows to verify that the element on which annotations are processed is valid. Should return true
	 * if it is the case, false otherwise. And errors should be produced in this case
	 *
	 * @param context
	 *            the current processor context (which gives access to various utilities)
	 * @param e
	 *            the current element to be processed
	 */
	protected boolean validateElement(final Element e) {
		return true;
	}

	/**
	 * Assert contains scope.
	 *
	 * @param context
	 *            the context
	 * @param throwsError
	 *            the throws error
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	protected boolean assertContainsScope(final boolean throwsError, final ExecutableElement e) {
		for (VariableElement v : e.getParameters()) { if (v.asType().toString().endsWith("IScope")) return true; }
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING, "IScope must be passed as a parameter to this method", e);
		return !throwsError;
	}

	/**
	 * Assert arguments size.
	 *
	 * @param context
	 *            the context
	 * @param throwsError
	 *            the throws error
	 * @param e
	 *            the e
	 * @param i
	 *            the i
	 * @return true, if successful
	 */
	protected boolean assertArgumentsSize(final boolean throwsError, final ExecutableElement e, final int i) {
		List<? extends VariableElement> parameters = e.getParameters();
		if (parameters.size() == 1) return true;
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING, "The size of parameters should be equal to " + i, e);
		return !throwsError;
	}

	/**
	 * Assert not void.
	 *
	 * @param context
	 *            the context
	 * @param throwsError
	 *            the throws error
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	protected boolean assertNotVoid(final boolean throwsError, final ExecutableElement e) {
		if (!TypeKind.VOID.equals(e.getReturnType().getKind())) return true;
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING,
				"The method should return a result to be annotated with " + getAnnotationClass().getSimpleName(), e);
		return !throwsError;
	}

	/**
	 * Assert class is agent or skill.
	 *
	 * @param context
	 *            the context
	 * @param throwsError
	 *            the throws error
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	protected boolean assertClassIsAgentOrSkill(final boolean throwsError, final TypeElement e) {
		TypeMirror t = e.asType();
		if (context.getTypeUtils().isAssignable(t, context.getIAgent())
				|| context.getTypeUtils().isAssignable(t, context.getISkill()))
			return true;
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING,
				getAnnotationClass().getSimpleName()
						+ " annotations do not make sense outside an IAgent or ISkill subclass: " + t.toString()
						+ " is neither of them.",
				e);
		return !throwsError;
	}

	/**
	 * Assert class extends.
	 *
	 * @param context
	 *            the context
	 * @param throwsError
	 *            the throws error
	 * @param e
	 *            the e
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	protected boolean assertClassExtends(final boolean throwsError, final TypeElement e, final TypeMirror type) {
		if (context.getTypeUtils().isAssignable(e.asType(), type)) return true;
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING, getAnnotationClass().getSimpleName()
				+ " annotations shoud be placed on classes extending or implementing " + type.toString(), e);
		return !throwsError;
	}

	/**
	 * Assert one scope and string constructor.
	 *
	 * @param context
	 *            the context
	 * @param throwsError
	 *            the throws error
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	protected boolean assertOneScopeAndStringConstructor(final boolean throwsError, final TypeElement e) {
		for (Element ee : e.getEnclosedElements()) {
			if (ee.getKind() == ElementKind.CONSTRUCTOR) {
				ExecutableElement constr = (ExecutableElement) ee;
				List<? extends VariableElement> param = constr.getParameters();
				if (param.size() == 2 && param.get(0).asType().equals(context.getIScope())
						&& param.get(1).asType().equals(context.getString()))
					return true;
			}
		}
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING, getAnnotationClass().getSimpleName() + " " + e.toString()
				+ " should declare at least one constructor with the signature (IScope scope, String fileName) to be usable in GAML",
				e);
		return !throwsError;

	}

	/**
	 * Assert annotation present.
	 *
	 * @param context
	 *            the context
	 * @param throwsError
	 *            the throws error
	 * @param e
	 *            the e
	 * @param anno
	 *            the anno
	 * @return true, if successful
	 */
	protected boolean assertAnnotationPresent(final boolean throwsError, final Element e,
			final Class<? extends Annotation> anno) {
		if (e.getAnnotation(anno) != null) return true;
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING,
				"A @" + anno.getSimpleName() + " annotation should be present on this element", e);
		return !throwsError;

	}

	/**
	 * Assert element is public.
	 *
	 * @param context
	 *            the context
	 * @param throwsError
	 *            the throws error
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	protected boolean assertElementIsPublic(final boolean throwsError, final Element e) {
		final Set<Modifier> modifiers = e.getModifiers();
		if (modifiers.contains(Modifier.PUBLIC)) return true;
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING,
				getAnnotationClass().getSimpleName() + "s can only be implemented by public elements", e);
		return !throwsError;
	}

	/**
	 * Registers a package for dynamic import in the generated GamlAdditions class.
	 * 
	 * <p>This utility method allows processors to register packages that should be imported
	 * in the generated code, enabling the use of simple class names instead of fully
	 * qualified names. The package will be added as a wildcard import.
	 * 
	 * <p>Example usage:
	 * <pre>{@code
	 * // Register a plugin-specific package for import
	 * registerPackageForImport("com.example.myplugin.operators");
	 * 
	 * // Now the generated code can use simple class names:
	 * // MyOperator.doSomething() instead of com.example.myplugin.operators.MyOperator.doSomething()
	 * }</pre>
	 * 
	 * @param packageName the package name to register for import (without trailing dot or asterisk)
	 */
	protected final void registerPackageForImport(final String packageName) {
		if (context != null) {
			context.addDynamicCollectiveImport(packageName);
		}
	}

	/**
	 * Registers a static import for dynamic import in the generated GamlAdditions class.
	 * 
	 * <p>This utility method allows processors to register static imports that should be imported
	 * in the generated code, enabling the use of static method names without class qualification.
	 * The class will be added as a static wildcard import.
	 * 
	 * <p>Example usage:
	 * <pre>{@code
	 * // Register a utility class for static import
	 * registerStaticImport("com.example.myplugin.utils.HelperClass");
	 * 
	 * // Now the generated code can use static methods directly:
	 * // helperMethod() instead of HelperClass.helperMethod()
	 * }</pre>
	 * 
	 * @param className the fully qualified class name to register for static import
	 */
	protected final void registerStaticImport(final String className) {
		if (context != null) {
			context.addDynamicStaticImport(className);
		}
	}

}
