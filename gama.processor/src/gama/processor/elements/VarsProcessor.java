/*******************************************************************************************************
 *
 * VarsProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import gama.annotations.getter;
import gama.annotations.setter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.processor.ProcessorContext;

/**
 * The VarsProcessor is responsible for processing {@code @vars} annotations during the annotation processing phase.
 *
 * <p>
 * Variables in GAMA represent attributes that can be accessed and modified in agent-based models. These variables can
 * be defined on species, skills, or other entities and provide the data storage and access mechanisms for simulation
 * state. The vars processor handles the registration of these variables with appropriate getter and setter mechanisms.
 *
 * <p>
 * This processor handles two types of variables:
 * <ul>
 * <li><strong>Agent Variables:</strong> Variables defined on agents/species that participate in the agent lifecycle and
 * can be accessed through the GAMA variable system</li>
 * <li><strong>Field Variables:</strong> Simple field variables that are accessed directly without going through the
 * agent variable system</li>
 * </ul>
 *
 * <p>
 * Key responsibilities include:
 * <ul>
 * <li><strong>Variable Registration:</strong> Registering variables with the GAMA runtime system</li>
 * <li><strong>Getter/Setter Processing:</strong> Building associations between variables and their accessor
 * methods</li>
 * <li><strong>Type Validation:</strong> Ensuring variables have proper types and metadata</li>
 * <li><strong>Documentation Validation:</strong> Verifying that all variables have proper documentation</li>
 * <li><strong>Facet Processing:</strong> Handling variable facets like constraints, dependencies, etc.</li>
 * </ul>
 *
 * <h3>Variable Definition Structure:</h3>
 * <p>
 * Variables are defined using the {@code @vars} annotation containing multiple {@code @variable} definitions:
 * <ul>
 * <li>Variable name and type information</li>
 * <li>Initial values and constraints</li>
 * <li>Access permissions and visibility</li>
 * <li>Documentation and usage examples</li>
 * <li>Associated getter and setter methods (via {@code @getter} and {@code @setter} annotations)</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 *
 * <pre>
 * {
 * 	&#64;code
 * 	&#64;vars ({ @variable (
 * 			name = "speed",
 * 			type = IType.FLOAT,
 * 			init = "1.0"),
 * 			@variable (
 * 					name = "location",
 * 					type = IType.POINT,
 * 					depends_on = { "x", "y" }) })
 * 	public class MovingAgent extends AbstractAgent {
 *
 * 		&#64;getter ("speed")
 * 		public double getSpeed(IAgent agent) {
 * 			return (Double) agent.getAttribute("speed");
 * 		}
 *
 * 		&#64;setter ("speed")
 * 		public void setSpeed(IAgent agent, Object value) {
 * 			agent.setAttribute("speed", value);
 * 		}
 * 	}
 * }
 * </pre>
 *
 * @author GAMA Development Team
 * @since 1.0
 * @see vars
 * @see variable
 * @see getter
 * @see setter
 * @see ElementProcessor
 */
public class VarsProcessor extends ElementProcessor<vars> {

	/**
	 * Map of setter methods organized by declaring class and variable name. Built during preprocessing to associate
	 * variables with their setter methods.
	 */
	Map<Element, Map<String, ExecutableElement>> setters = new HashMap<>();

	/**
	 * Map of getter methods organized by declaring class and variable name. Built during preprocessing to associate
	 * variables with their getter methods.
	 */
	Map<Element, Map<String, ExecutableElement>> getters = new HashMap<>();

	/**
	 * Processes all elements annotated with {@code @vars}.
	 *
	 * <p>
	 * This method extends the base processing by first building maps of all {@code @getter} and {@code @setter}
	 * annotated methods in the codebase, then proceeding with normal element processing. This preprocessing is
	 * necessary because variable processing needs to know about all available accessor methods before generating the
	 * variable registration code.
	 *
	 * @param context
	 *            the processing context providing access to annotated elements
	 */
	@Override
	public void process(final ProcessorContext context) {
		buildSettersAndGetters(context);
		super.process(context);
	}

	/**
	 * Builds the setters and getters.
	 *
	 * @param context
	 *            the context
	 */
	private void buildSettersAndGetters(final ProcessorContext context) {
		setters =
				context.getElementsAnnotatedWith(setter.class).stream().collect(groupingBy(Element::getEnclosingElement,
						toMap(f -> f.getAnnotation(setter.class).value(), f -> (ExecutableElement) f)));
		getters =
				context.getElementsAnnotatedWith(getter.class).stream().collect(groupingBy(Element::getEnclosingElement,
						toMap(f -> f.getAnnotation(getter.class).value(), f -> (ExecutableElement) f)));
	}

	@Override
	public void createElement(final StringBuilder sb, final Element e, final vars vars) {
		final TypeMirror typeClass = e.asType();
		// If the declaring class has nothing to do with IAgent or ISkill, then the variable is considered as a 'field'
		final boolean isField = !context.getTypeUtils().isAssignable(typeClass, context.getIVarAndActionSupport());

		// Use local set for temp to improve memory usage
		final Set<String> temp = new HashSet<>(vars.value().length);

		for (final variable node : vars.value()) {
			final String vName = node.name();
			if (!temp.add(vName)) {
				context.emitError("Attribute '" + vName + "' is declared twice in " + typeClass, e);
				continue;
			}
			verifyDoc(e, "attribute " + node.name(), node);
			final String clazz = rawNameOf(e.asType());
			final String clazzObject = toClassObject(clazz);

			sb.append(in).append(isField ? "_field(" : "_var(").append(clazzObject).append(',');

			if (isField) {
				writeFieldVariable(sb, node, clazz, clazzObject, e);
			} else {
				writeAgentVariable(sb, node, clazz, e);
			}

			sb.append(");");
		}
	}

	/**
	 * Write field variable details.
	 *
	 * @param sb
	 *            the StringBuilder
	 * @param node
	 *            the variable node
	 * @param clazz
	 *            the class name
	 * @param clazzObject
	 *            the class object string
	 * @param e
	 *            the element
	 */
	private void writeFieldVariable(final StringBuilder sb, final variable node, final String clazz,
			final String clazzObject, final Element e) {
		sb.append(toJavaString(node.name())).append(',');
		writeHelpers(sb, context, node, clazz, e, true, true);
		sb.append(',').append(node.type()).append(',').append(clazzObject).append(',').append(node.type()).append(',')
				.append(node.of()).append(',').append(node.index());
	}

	/**
	 * Write agent variable details.
	 *
	 * @param sb
	 *            the StringBuilder
	 * @param node
	 *            the variable node
	 * @param clazz
	 *            the class name
	 * @param e
	 *            the element
	 */
	private void writeAgentVariable(final StringBuilder sb, final variable node, final String clazz, final Element e) {
		sb.append("desc(").append(node.type()).append(',');
		writeFacets(sb, node);
		sb.append("),");
		writeHelpers(sb, context, node, clazz, e, false, false);
	}

	/**
	 * Write helpers.
	 *
	 * @param sb
	 *            the sb
	 * @param context
	 *            the context
	 * @param var
	 *            the var
	 * @param clazz
	 *            the clazz
	 * @param e
	 *            the e
	 * @param isField
	 *            the is field
	 * @param onlyGetter
	 *            the only getter
	 */
	private void writeHelpers(final StringBuilder sb, final ProcessorContext context, final variable var,
			final String clazz, final Element e, final boolean isField, final boolean onlyGetter) {
		final String name = var.name();
		final String setterHelper = onlyGetter ? null : processSetterHelper(context, var, clazz, e, name);
		final String[] getterInfo = processGetterHelper(var, clazz, e, name, isField);
		final String getterHelper = getterInfo[0];
		final String initerHelper = getterInfo[1];

		if (onlyGetter) {
			sb.append(getterHelper);
		} else {
			sb.append(getterHelper).append(',').append(initerHelper).append(',').append(setterHelper);
		}
	}

	/**
	 * Process setter helper for a variable.
	 *
	 * @param context
	 *            the processor context
	 * @param var
	 *            the variable
	 * @param clazz
	 *            the class name
	 * @param e
	 *            the element
	 * @param name
	 *            the variable name
	 * @return the setter helper string or null
	 */
	private String processSetterHelper(final ProcessorContext context, final variable var, final String clazz,
			final Element e, final String name) {
		final Map<String, ExecutableElement> elements = setters.get(e);
		if (elements == null) return null;

		final ExecutableElement ex = elements.get(name);
		if (ex == null) return null;

		final List<? extends VariableElement> argParams = ex.getParameters();
		final int n = argParams.size();
		if (n == 0) {
			context.emitError(
					"setters must declare at least one argument corresponding to the value of the variable (or 2 if the scope is passed)",
					ex);
			return null;
		}

		final String[] args = new String[n];
		for (int i = 0; i < args.length; i++) { args[i] = rawNameOf(argParams.get(i).asType()); }

		final boolean scope = n > 0 && args[0].contains("IScope");
		final String method = ex.getSimpleName().toString();
		final boolean isDynamic = scope ? n == 3 : n == 2;
		final String param_class = checkPrim(isDynamic ? args[!scope ? 1 : 2] : args[!scope ? 0 : 1]);

		return concat("(s,a,t,v)->{if (t != null) ((", clazz, ") t).", method, "(", scope ? "s," : "",
				isDynamic ? "a, " : "", "(" + param_class + ") v); return null; }");
	}

	/**
	 * Process getter helper for a variable.
	 *
	 * @param var
	 *            the variable
	 * @param clazz
	 *            the class name
	 * @param e
	 *            the element
	 * @param name
	 *            the variable name
	 * @param isField
	 *            whether this is a field
	 * @return array containing getter helper and initializer helper
	 */
	private String[] processGetterHelper(final variable var, final String clazz, final Element e, final String name,
			final boolean isField) {
		final Map<String, ExecutableElement> elements = getters.get(e);
		if (elements == null) return new String[] { null, null };

		final ExecutableElement ex = elements.get(name);
		if (ex == null) return new String[] { null, null };

		final List<? extends VariableElement> argParams = ex.getParameters();
		final String[] args = new String[argParams.size()];
		for (int i = 0; i < args.length; i++) { args[i] = rawNameOf(argParams.get(i).asType()); }

		final int n = args.length;
		final boolean scope = n > 0 && args[0].contains("IScope");
		final String method = ex.getSimpleName().toString();
		final String returns = rawNameOf(ex.getReturnType());
		final boolean dynamic = scope ? n > 1 : n > 0;

		final String getterHelper;
		if (isField) {
			getterHelper = concat("(s, o)->((", clazz, ")o[0]).", method, scope ? "(s)" : "()");
		} else {
			getterHelper = concat("(s,a,t,v)->t==null?", returnWhenNull(checkPrim(returns)), ":((", clazz, ")t).",
					method, "(", scope ? "s" : "", dynamic ? (scope ? "," : "") + "a)" : ")");
		}

		final String initerHelper = ex.getAnnotation(getter.class).initializer() ? getterHelper : null;
		return new String[] { getterHelper, initerHelper };
	}

	/**
	 * Write facets.
	 *
	 * @param sb
	 *            the StringBuilder
	 * @param s
	 *            the variable
	 */
	private void writeFacets(final StringBuilder sb, final variable s) {
		sb.append("S(\"type\"").append(',').append(toJavaString(String.valueOf(s.type()))).append(',')
				.append("\"name\"").append(',').append(toJavaString(s.name()));

		if (s.constant()) {
			sb.append(',').append("\"const\"").append(',').append(toJavaString(String.valueOf(s.constant())));
		}

		final String[] dependencies = s.depends_on();
		if (dependencies.length > 0) {
			sb.append(',').append("\"depends_on\"").append(',').append("\"[");
			for (int i = 0; i < dependencies.length; i++) {
				if (i > 0) { sb.append(','); }
				sb.append(dependencies[i]);
			}
			sb.append("]\"");
		}

		if (s.of() != 0) { sb.append(',').append("\"of\"").append(',').append(toJavaString(String.valueOf(s.of()))); }

		if (s.index() != 0) {
			sb.append(',').append("\"index\"").append(',').append(toJavaString(String.valueOf(s.index())));
		}

		final String init = s.init();
		if (!init.isEmpty()) { sb.append(',').append("\"init\"").append(',').append(toJavaString(init)); }

		sb.append(')');
	}

	@Override
	protected Class<vars> getAnnotationClass() { return vars.class; }

}
