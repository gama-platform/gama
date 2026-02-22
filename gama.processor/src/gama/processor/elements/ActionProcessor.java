/*******************************************************************************************************
 *
 * ActionProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import gama.annotations.action;
import gama.annotations.arg;

/**
 * The ActionProcessor is responsible for processing {@code @action} annotations during the annotation processing phase.
 *
 * <p>
 * This processor generates runtime helper code that enables GAMA to register and invoke actions defined in Java
 * classes. Actions in GAMA are methods that can be called from within simulation models, typically from species or
 * skills.
 *
 * <p>
 * The processor validates action declarations, processes their arguments, and generates the necessary runtime
 * registration code. It handles:
 * <ul>
 * <li>Action name resolution (using annotation name or method name as fallback)</li>
 * <li>Argument validation and processing</li>
 * <li>Return type processing</li>
 * <li>Generation of helper code for runtime action invocation</li>
 * <li>Validation of reserved facet names to prevent conflicts</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 *
 * <pre>
 * {@code
 * &#64;action(name = "move", args = {
 *     &#64;arg(name = "speed", type = IType.FLOAT),
 *     @arg(name = "heading", type = IType.INT, optional = true)
 * })
 * public void moveAction(IScope scope) {
 *     // Action implementation
 * }
 * }
 * </pre>
 *
 * @author GAMA Development Team
 * @since 1.0
 * @see action
 * @see ElementProcessor
 */
public class ActionProcessor extends ElementProcessor<action> {

	/**
	 * Set of reserved facet names that cannot be used as action argument names.
	 *
	 * <p>
	 * These names are reserved by GAMA's action system and using them as argument names will prevent the action from
	 * being called using the faceted syntax. The reserved names are:
	 * <ul>
	 * <li>{@code name} - used to specify the action name</li>
	 * <li>{@code keyword} - used for action keywords</li>
	 * <li>{@code returns} - used to specify return value handling</li>
	 * </ul>
	 */
	private static final Set<String> RESERVED_FACETS = Set.of("name", "keyword", "returns");

	/**
	 * Creates the element code for an action annotation.
	 *
	 * <p>
	 * This method generates the runtime registration code for a GAMA action. It processes the action annotation and the
	 * annotated method to produce code that will register the action with the GAMA runtime system.
	 *
	 * <p>
	 * The generated code includes:
	 * <ul>
	 * <li>Action name and class binding</li>
	 * <li>Method invocation helper with proper type handling</li>
	 * <li>Return type processing (handles void and non-void methods)</li>
	 * <li>Argument definitions and validation</li>
	 * <li>Virtual action flag processing</li>
	 * </ul>
	 *
	 * @param sb
	 *            the StringBuilder to append the generated code to
	 * @param e
	 *            the method element annotated with @action
	 * @param action
	 *            the action annotation containing metadata
	 *
	 * @throws IllegalArgumentException
	 *             if the method signature is invalid for an action
	 * @throws RuntimeException
	 *             if code generation fails due to type processing errors
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final action action) {
		final String method = e.getSimpleName().toString();
		final String clazz = rawNameOf(e.getEnclosingElement().asType());
		final String ret = checkPrim(getReturnType((ExecutableElement) e));
		final String actionName = action.name();
		final String clazzObject = toClassObject(clazz);
		final String retObject = toClassObject(ret);
		final boolean isVoid = "void".equals(ret);

		sb.append(in).append("_action(");
		sb.append("new " + rawNameOf("gama.api.additions.GamaHelper") + "(").append(toJavaString(actionName))
				.append(',').append(clazzObject).append(',').append("(s,a,t,v)->").append(isVoid ? "{" : "")
				.append("((").append(clazz).append(") t).").append(method).append("(s)")
				.append(isVoid ? ";return null;})," : "),");
		sb.append("desc(PRIM,");
		buildArgs(e, action.args(), sb).append(",NAME,").append(toJavaString(actionName)).append(",TYPE,Ti(")
				.append(retObject).append("),VIRTUAL,").append(toJavaString(String.valueOf(action.virtual())))
				.append(')').append(',').append(clazzObject).append(".getMethod(").append(toJavaString(method))
				.append(',').append(toClassObject(ISCOPE)).append("));");
	}

	/**
	 * Returns the annotation class that this processor handles.
	 *
	 * @return the {@link action} annotation class
	 */
	@Override
	protected Class<action> getAnnotationClass() { return action.class; }

	/**
	 * Builds the argument definitions for an action.
	 *
	 * <p>
	 * This method processes the arguments defined in an action annotation and generates the appropriate code for
	 * argument validation and handling at runtime. It performs several validations:
	 * <ul>
	 * <li>Checks for reserved facet names that could conflict with GAMA's built-in facets</li>
	 * <li>Ensures argument names are unique within the action</li>
	 * <li>Validates argument documentation</li>
	 * <li>Generates type-safe argument processing code</li>
	 * </ul>
	 *
	 * <p>
	 * Reserved facets that cannot be used as argument names include: "name", "keyword", and "returns". Using these
	 * names will generate a warning as it prevents the action from being called using the faceted syntax (e.g.,
	 * {@code do action arg1: val1 arg2: val2;}).
	 *
	 * @param e
	 *            the method element being processed (for error reporting)
	 * @param args
	 *            the array of argument annotations from the action
	 * @param sb
	 *            the StringBuilder to append the generated argument code to
	 * @return the same StringBuilder for method chaining
	 *
	 * @throws ProcessingException
	 *             if duplicate argument names are found
	 */
	private final StringBuilder buildArgs(final Element e, final arg[] args, final StringBuilder sb) {
		sb.append("new Children(");

		if (args.length == 0) {
			sb.append(")");
			return sb;
		}

		// Use local set for temp to improve memory usage
		final Set<String> temp = new HashSet<>(args.length);

		for (int i = 0; i < args.length; i++) {
			final arg arg = args[i];
			if (i > 0) { sb.append(','); }
			final String argName = arg.name();

			if (RESERVED_FACETS.contains(argName)) {
				context.emitWarning("Argument '" + argName
						+ "' prevents this action to be called using facets (e.g. 'do action arg1: val1 arg2: val2;'). Consider renaming it to a non-reserved facet keyword",
						e);
			}
			if (!temp.add(argName)) { context.emitError("Argument '" + argName + "' is declared twice", e); }

			verifyDoc(e, "argument " + argName, arg);
			sb.append("_arg(").append(toJavaString(argName)).append(',').append(arg.type()).append(',')
					.append(toBoolean(arg.optional())).append(')');
		}
		sb.append(")");

		return sb;
	}

	/**
	 * Gets the return type.
	 *
	 * @param context
	 *            the context
	 * @param ex
	 *            the ex
	 * @return the return type
	 */
	private String getReturnType(final ExecutableElement ex) {
		final TypeMirror tm = ex.getReturnType();
		if (TypeKind.VOID.equals(tm.getKind())) return "void";
		return rawNameOf(tm);
	}

	@Override
	public String getExceptions() { return "throws SecurityException, NoSuchMethodException"; }

	/**
	 * By construction, action can only annotate methods so no need to verify this
	 */
	@Override
	protected boolean validateElement(final Element e) {
		boolean result = assertNotVoid(false, (ExecutableElement) e);
		result &= assertArgumentsSize(true, (ExecutableElement) e, 1);
		result &= assertContainsScope(true, (ExecutableElement) e);
		result &= assertClassIsAgentOrSkill(true, (TypeElement) e.getEnclosingElement());
		return result;
	}

}
