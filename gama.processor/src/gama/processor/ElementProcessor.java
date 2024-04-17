/*******************************************************************************************************
 *
 * ElementProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor;

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

import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.constant;
import gama.annotations.precompiler.GamlAnnotations.display;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.experiment;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.tests;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.GamlAnnotations.variable;

/**
 * The Class ElementProcessor.
 *
 * @param <T>
 *            the generic type
 */
public abstract class ElementProcessor<T extends Annotation> implements IProcessor<T>, Constants {

	/** The Constant NAME_CACHE. */
	protected static final Map<String, String> NAME_CACHE = new HashMap<>();

	/** The Constant CONCAT. */
	static final StringBuilder CONCAT = new StringBuilder();

	/** The serialized elements. */
	protected final SortedMap<String, StringBuilder> serializedElements = new TreeMap<>();

	/** The Constant CLASS_PARAM. */
	static final Pattern CLASS_PARAM = Pattern.compile("<.*?>");

	/** The Constant SINGLE_QUOTE. */
	static final Pattern SINGLE_QUOTE = Pattern.compile("\"");

	/** The Constant QUOTE_MATCHER. */
	static final String QUOTE_MATCHER = Matcher.quoteReplacement("\\\"");

	/** The initialization method name. */
	protected String initializationMethodName;

	/**
	 * Concat.
	 *
	 * @param array
	 *            the array
	 * @return the string
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
	public boolean hasElements() {
		return serializedElements.size() > 0;
	}

	@Override
	public void process(final ProcessorContext context) {
		final Class<T> a = getAnnotationClass();
		clean(context, serializedElements);
		for (final Map.Entry<String, List<Element>> entry : context.groupElements(a).entrySet()) {
			final List<Element> elements = entry.getValue();
			if (elements.size() == 0) { continue; }
			final StringBuilder sb = new StringBuilder();
			for (final Element e : elements) {
				try {
					if (validateElement(context, e)) { createElement(sb, context, e, e.getAnnotation(a)); }
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
		boolean internal = false;
		if (a instanceof species) {
			internal = ((species) a).internal();
		} else if (a instanceof symbol) {
			internal = ((symbol) a).internal();
		} else if (a instanceof operator) {
			internal = ((operator) a).internal();
		} else if (a instanceof skill) {
			internal = ((skill) a).internal();
		} else if (a instanceof facet) {
			internal = ((facet) a).internal();
		} else if (a instanceof type) {
			internal = ((type) a).internal();
		} else if (a instanceof variable) { internal = ((variable) a).internal(); }
		return internal;
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
		doc[] docs = NULL_DOCS;
		if (a instanceof species) {
			docs = ((species) a).doc();
		} else if (a instanceof symbol) {
			docs = ((symbol) a).doc();
		} else if (a instanceof arg) {
			docs = ((arg) a).doc();
		} else if (a instanceof display || a instanceof experiment) {
			// nothing
		} else if (a instanceof constant) {
			docs = ((constant) a).doc();
		} else if (a instanceof operator) {
			docs = ((operator) a).doc();
		} else if (a instanceof skill) {
			docs = ((skill) a).doc();
		} else if (a instanceof facet) {
			docs = ((facet) a).doc();
		} else if (a instanceof type) {
			docs = ((type) a).doc();
		} else if (a instanceof file) {
			docs = ((file) a).doc();
		} else if (a instanceof variable) { docs = ((variable) a).doc(); }
		doc d = null;
		if (docs.length == 0) {
			d = main.getAnnotation(doc.class);
		} else {
			d = docs[0];
		}
		return d;
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
	protected void verifyDoc(final ProcessorContext context, final Element e, final String displayedName,
			final Annotation a) {
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

	@Override
	public void serialize(final ProcessorContext context, final Collection<StringBuilder> elements,
			final StringBuilder sb) {
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
			final Collection<StringBuilder> elements, final StringBuilder sb, final ProcessorContext context) {
		sb.append("public void ").append(method).append("() ").append(getExceptions()).append(" {");
		serialize(context, elements, sb);
		if (followingMethod != null) { sb.append(ln).append(followingMethod).append("(); "); }
		sb.append(ln).append("}");
	}

	@Override
	public void writeJavaBody(final StringBuilder sb, final ProcessorContext context) {
		final String method = getInitializationMethodName();
		if (method == null) return;
		final int size = sizeOf(serializedElements);
		if (size > 20000) {
			writeMethod(method, method + "2", halfOf(serializedElements, true), sb, context);
			writeMethod(method + "2", null, halfOf(serializedElements, false), sb, context);
		} else {
			writeMethod(method, null, serializedElements.values(), sb, context);
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
	public abstract void createElement(StringBuilder sb, ProcessorContext context, Element e, T annotation);

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
	protected final static String toClassObject(final String s) {
		final String result = CLASS_NAMES.get(s);
		return result == null ? s + ".class" : result;
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
			case DOUBLE:
				sb.append("asFloat(s,").append(par).append(')');
				break;
			case INTEGER:
				sb.append("asInt(s,").append(par).append(')');
				break;
			case BOOLEAN:
				sb.append("asBool(s,").append(par).append(')');
				break;
			case OBJECT:
				sb.append(par);
				break;
			default:
				sb.append("((").append(jc).append(")").append(par).append(')');

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
	static String rawNameOf(final ProcessorContext context, final TypeMirror t) {
		if (TypeKind.VOID.equals(t.getKind())) return "void";
		final String key = t.toString();
		String cachedName = NAME_CACHE.get(key);
		if (cachedName != null) return cachedName;
		String type = context.getTypeUtils().erasure(t).toString();
		// As a workaround for ECJ/javac discrepancies regarding erasure
		type = CLASS_PARAM.matcher(type).replaceAll("");
		// Reduction by considering the imports written in the header
		int lastDot = type.lastIndexOf('.') + 1;
		String path = type.substring(0, lastDot);
		if (context.containsImport(path)) { type = type.substring(lastDot); }
		NAME_CACHE.put(key, type);
		return type;
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
	protected boolean validateElement(final ProcessorContext context, final Element e) {
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
	protected boolean assertContainsScope(final ProcessorContext context, final boolean throwsError,
			final ExecutableElement e) {
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
	protected boolean assertArgumentsSize(final ProcessorContext context, final boolean throwsError,
			final ExecutableElement e, final int i) {
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
	protected boolean assertNotVoid(final ProcessorContext context, final boolean throwsError,
			final ExecutableElement e) {
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
	protected boolean assertClassIsAgentOrSkill(final ProcessorContext context, final boolean throwsError,
			final TypeElement e) {
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
	protected boolean assertClassExtends(final ProcessorContext context, final boolean throwsError, final TypeElement e,
			final TypeMirror type) {
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
	protected boolean assertOneScopeAndStringConstructor(final ProcessorContext context, final boolean throwsError,
			final TypeElement e) {
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
	protected boolean assertAnnotationPresent(final ProcessorContext context, final boolean throwsError,
			final Element e, final Class<? extends Annotation> anno) {
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
	protected boolean assertElementIsPublic(final ProcessorContext context, final boolean throwsError,
			final Element e) {
		final Set<Modifier> modifiers = e.getModifiers();
		if (modifiers.contains(Modifier.PUBLIC)) return true;
		context.emit(throwsError ? Kind.ERROR : Kind.WARNING,
				getAnnotationClass().getSimpleName() + "s can only be implemented by public elements", e);
		return !throwsError;
	}

}
