/*******************************************************************************************************
 *
 * OperatorProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import gama.annotations.operator;

/**
 * The OperatorProcessor is responsible for processing {@code @operator} annotations during the annotation processing phase.
 * 
 * <p>Operators in GAMA represent functions and operations that can be used in GAML expressions.
 * They provide the computational building blocks for model logic, from basic arithmetic and logical
 * operations to complex spatial, statistical, and domain-specific functions. The operator processor
 * handles the registration of these operations with the GAMA runtime system.
 * 
 * <p>This processor handles:
 * <ul>
 * <li><strong>Operator Registration:</strong> Registering operators with the GAMA expression system</li>
 * <li><strong>Method Signature Analysis:</strong> Processing method parameters and return types</li>
 * <li><strong>Static/Instance Detection:</strong> Handling both static and instance method operators</li>
 * <li><strong>Type Safety:</strong> Ensuring proper type checking and conversion</li>
 * <li><strong>Performance Optimization:</strong> Generating efficient invocation code</li>
 * <li><strong>Documentation Validation:</strong> Verifying examples and test coverage</li>
 * </ul>
 * 
 * <h3>Operator Types:</h3>
 * <p>Operators can follow different patterns:
 * <ul>
 * <li><strong>Static:</strong> {@code static Result operation(IScope scope, Type1 arg1, Type2 arg2)}</li>
 * <li><strong>Instance:</strong> {@code Result operation(IScope scope, Type1 arg1)} on instance methods</li>
 * <li><strong>No-scope:</strong> Methods without IScope parameter for pure functions</li>
 * <li><strong>Multiple names:</strong> Operators can have multiple aliases</li>
 * </ul>
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * @operator(
 *     value = {"distance", "dist"},
 *     can_be_const = true,
 *     type = IType.FLOAT,
 *     expected_content_type = IType.POINT
 * )
 * public static Double distance(IScope scope, ILocation a, ILocation b) {
 *     return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
 * }
 * }</pre>
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see operator
 * @see ElementProcessor
 */
public class OperatorProcessor extends ElementProcessor<operator> {

	/**
	 * Helper class to encapsulate parameter processing information
	 */
	private static record ParameterInfo(String[] args, boolean hasScope, int actualArgsNumber) {}

	@Override
	public void createElement(final StringBuilder sb, final Element method, final operator op) {
		// Cache frequently accessed values
		final String[] names = op.value();
		if (names == null) {
			context.emitError("GAML operators need to have at least one name", method);
			return;
		}

		final String methodName = method.getSimpleName().toString();
		final String operatorName = names.length == 0 ? methodName : names[0];

		// Early verification
		verifyDoc(method, "operator " + operatorName, op);
		verifyTests(method, op);

		final ExecutableElement executableMethod = (ExecutableElement) method;
		final String declClass = rawNameOf(method.getEnclosingElement().asType());

		// Register the package of the declaring class for dynamic import
		// This allows generated code to use simple class names instead of fully qualified names
		String packageName = extractPackageFromClassName(declClass);
		if (packageName != null) {
			registerPackageForImport(packageName);
		}

		// Process method parameters
		final ParameterInfo paramInfo = processParameters(executableMethod);
		if (paramInfo == null) return; // Error occurred during processing

		// Validate method signature
		final Set<Modifier> modifiers = method.getModifiers();
		final boolean isStatic = modifiers.contains(Modifier.STATIC);
		if (!validateMethodSignature(paramInfo, isStatic, method)) return;

		// Build class array for method call
		final String[] classes = buildClassArray(paramInfo, declClass, isStatic);

		// Validate and process return type
		final String returnType = processReturnType(executableMethod, method);
		if (returnType == null) return; // Error occurred during processing

		// Generate method call
		final String methodCall = isStatic ? declClass + "." + methodName : methodName;
		generateOperatorCode(sb, names, classes, methodCall, returnType, isStatic, paramInfo.hasScope, op);
	}

	/**
	 * Builds the helper call.
	 *
	 * @param sb
	 *            the sb
	 * @param hasScope
	 *            the has scope
	 * @param isStatic
	 *            the is static
	 * @param classes
	 *            the classes
	 * @param methodCall
	 *            the method call
	 */
	private void buildHelperCall(final StringBuilder sb, final boolean hasScope, final boolean isStatic,
			final String[] classes, final String methodCall) {
		sb.append(',').append("(s,o)->");
		final int start = isStatic ? 0 : 1;
		final String firstArg = hasScope ? "s" : "";
		if (isStatic) {
			// For static calls, extract the class name and track it
			String staticClassName = extractClass(methodCall, classes[0], true);
			context.trackClassUsage(staticClassName);
			sb.append(getClassName(staticClassName)).append('.').append(extractMethod(methodCall, true))
				.append('(').append(firstArg);
		} else {
			final String methodName = extractMethod(methodCall, false);
			sb.append("((").append(getClassName(classes[0])).append(")o[0]).").append(methodName)
				.append('(').append(firstArg);
		}
		if (start < classes.length) {
			if (hasScope) { sb.append(','); }
			for (int i = start; i < classes.length; i++) {
				param(sb, classes[i], "o[" + i + "]");
				sb.append(i != classes.length - 1 ? "," : "");
			}
		}
		sb.append(")");
	}

	/**
	 * Verify class type compatibility.
	 *
	 * @param context
	 *            the context
	 * @param string
	 *            the string
	 * @param ve
	 *            the ve
	 */
	public void verifyClassTypeCompatibility(final String string, final Element ve) {
		String warning = null;
		switch (string) {
			case "Map":
				warning = "it is safer to use the IMap type";
				break;
			case "ArrayList":
			case "List":
				warning = "it is safer to use the IList type";
				break;
			case "short":
			case "long":
			case "Long":
			case "Short":
				warning = "it is safer to use the Integer type";
				break;
			case "float":
			case "Float":
				warning = "it is safer to use the Double type";
				break;
			case "Color":
				warning = "it is safer to use the GamaColor type";
				break;
		}
		if (warning != null) { context.emitWarning(warning, ve); }

	}

	/**
	 * Verify tests.
	 *
	 * @param method
	 *            the method
	 * @param op
	 *            the op
	 */
	private void verifyTests(final Element method, final operator op) {
		if (!hasTests(method, op)) {
			final String[] names = op.value();
			final String operatorName = names.length == 0 ? method.getSimpleName().toString() : names[0];
			context.emitWarning("operator '" + operatorName + "' is not tested", method);
		}
	}

	@Override
	protected Class<operator> getAnnotationClass() { return operator.class; }

	@Override
	public String getExceptions() { return "throws SecurityException, NoSuchMethodException"; }

	/**
	 * Extract method.
	 *
	 * @param s
	 *            the s
	 * @param stat
	 *            the stat
	 * @return the string
	 */
	protected static String extractMethod(final String s, final boolean stat) {
		if (!stat) return s;
		return s.substring(s.lastIndexOf('.') + 1);
	}

	/**
	 * Extract class.
	 *
	 * @param name
	 *            the name
	 * @param string
	 *            the string
	 * @param stat
	 *            the stat
	 * @return the string
	 */
	protected static String extractClass(final String name, final String string, final boolean stat) {
		if (stat) return name.substring(0, name.lastIndexOf('.'));
		return string;
	}

	/**
	 * Builds the method call.
	 *
	 * @param sb
	 *            the sb
	 * @param classes
	 *            the classes
	 * @param name
	 *            the name
	 * @param stat
	 *            the stat
	 * @param scope
	 *            the scope
	 * @return the string builder
	 */
	protected static StringBuilder buildMethodCall(final StringBuilder sb, final String[] classes, final String name,
			final boolean stat, final boolean scope) {
		final int start = stat ? 0 : 1;
		sb.append(toClassObjectStatic(extractClass(name, classes[0], stat)));
		sb.append(".getMethod(").append(toJavaString(extractMethod(name, stat))).append(',');
		if (scope) { sb.append(toClassObjectStatic(ISCOPE)).append(','); }
		for (int i = start; i < classes.length; i++) {
			sb.append(toClassObjectStatic(classes[i]));
			sb.append(',');
		}
		sb.setLength(sb.length() - 1);
		sb.append(')');
		return sb;
	}

	/**
	 * To array of classes.
	 *
	 * @param sb
	 *            the sb
	 * @param segments
	 *            the segments
	 * @return the string builder
	 */
	protected final static StringBuilder toArrayOfClasses(final StringBuilder sb, final String[] segments) {
		if (segments == null || segments.length == 0) {
			sb.append("{}");
			return sb;
		}
		sb.append("C(");
		for (int i = 0; i < segments.length; i++) {
			if (i > 0) { sb.append(','); }
			sb.append(toClassObjectStatic(segments[i]));
		}
		sb.append(")");
		return sb;
	}

	/**
	 * Validate element.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	@Override
	protected boolean validateElement(final Element e) {

		// TODO: move all other warnings and errors here
		return assertElementIsPublic(true, e);
	}

	/**
	 * Process method parameters and extract parameter information
	 */
	private ParameterInfo processParameters(final ExecutableElement executableMethod) {
		final List<? extends VariableElement> argParams = executableMethod.getParameters();
		final String[] args = new String[argParams.size()];

		for (int i = 0; i < args.length; i++) {
			final VariableElement ve = argParams.get(i);
			switch (ve.asType().getKind()) {
				case ARRAY:
					context.emitError("arrays should be wrapped in a GAML container (IList or IMatrix) ", ve);
					return null;
				case CHAR:
				case BYTE:
				case SHORT:
					context.emitWarning("this argument will be casted to int", ve);
					break;
				default:
			}
			args[i] = rawNameOf(argParams.get(i).asType());
			verifyClassTypeCompatibility(args[i], ve);
		}

		final boolean hasScope = args.length > 0 && args[0].contains("IScope");
		final int actualArgsNumber = args.length + (hasScope ? -1 : 0);

		return new ParameterInfo(args, hasScope, actualArgsNumber);
	}

	/**
	 * Validate method signature constraints
	 */
	private boolean validateMethodSignature(final ParameterInfo paramInfo, final boolean isStatic,
			final Element method) {
		final int n = paramInfo.args.length;
		if (isStatic && (n == 0 || paramInfo.hasScope && n == 1)) {
			context.emitError("an operator needs to have at least one operand", method);
			return false;
		}
		return true;
	}

	/**
	 * Build the class array for method calls
	 */
	private String[] buildClassArray(final ParameterInfo paramInfo, final String declClass, final boolean isStatic) {
		final int totalArgsNumber = paramInfo.actualArgsNumber + (!isStatic ? 1 : 0);
		final String[] classes = new String[totalArgsNumber];

		int begin = 0;
		if (!isStatic) {
			classes[0] = declClass;
			begin = 1;
		}

		final int shift = paramInfo.hasScope ? 1 : 0;
		try {
			for (int i = 0; i < paramInfo.actualArgsNumber; i++) { classes[begin + i] = paramInfo.args[i + shift]; }
		} catch (final Exception e1) {
			context.emitError("an exception occurred in the processing of operators: ", e1, null);
			return null;
		}

		return classes;
	}

	/**
	 * Process and validate return type
	 */
	private String processReturnType(final ExecutableElement executableMethod, final Element method) {
		final String ret = rawNameOf(executableMethod.getReturnType());
		verifyClassTypeCompatibility(ret, method);

		switch (executableMethod.getReturnType().getKind()) {
			case ARRAY:
				context.emitError("wrap the returned array in a GAML container (IList or IMatrix) ", method);
				return null;
			case VOID:
			case NULL:
			case NONE:
			case ERROR:
				context.emitError("operators need to return a value.", method);
				return null;
			case CHAR:
			case BYTE:
			case SHORT:
				context.emitWarning("the return type will be casted to integer", method);
				break;
			case EXECUTABLE:
				context.emitError("operators cannot return Java executables", method);
				return null;
			default:
		}
		if ("void".equals(ret)) {
			context.emitError("operators need to return a value", method);
			return null;
		}
		return ret;
	}

	/**
	 * Generate the operator code output
	 */
	private void generateOperatorCode(final StringBuilder sb, final String[] names, final String[] classes,
			final String methodCall, final String returnType, final boolean isStatic, final boolean hasScope,
			final operator op) {
		sb.append(in).append("_operator(");
		toArrayOfStrings(names, sb).append(',');
		buildMethodCall(sb, classes, methodCall, isStatic, hasScope).append(",null,"); /* doc */
		toArrayOfInts(op.expected_content_type(), sb).append(',').append(toClassObject(returnType)).append(',')
				.append(toBoolean(op.can_be_const())).append(',').append(op.type()).append(',')
				.append(op.content_type()).append(',').append(op.index_type()).append(',')
				.append(op.content_type_content_type());

		buildHelperCall(sb, hasScope, isStatic, classes, methodCall);
		sb.append(',').append(toBoolean(op.iterator()));
		sb.append(");");
	}

	/**
	 * Extracts the package name from a fully qualified class name.
	 * 
	 * @param className the fully qualified class name
	 * @return the package name, or null if no package can be extracted
	 */
	private String extractPackageFromClassName(final String className) {
		if (className == null) return null;
		int lastDotIndex = className.lastIndexOf('.');
		if (lastDotIndex > 0) {
			return className.substring(0, lastDotIndex);
		}
		return null;
	}

}