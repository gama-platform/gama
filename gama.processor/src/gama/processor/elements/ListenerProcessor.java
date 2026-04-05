/*******************************************************************************************************
 *
 * ListenerProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import gama.annotations.listener;

/**
 * The ListenerProcessor is responsible for processing {@code @listener} annotations during the annotation processing phase.
 * 
 * <p>Listeners in GAMA provide a mechanism for responding to changes in variable values or other events
 * within the simulation. They enable reactive programming patterns where methods are automatically
 * triggered when specific variables are modified, allowing for dynamic model behavior and monitoring.
 * 
 * <p>This processor handles:
 * <ul>
 * <li><strong>Listener Registration:</strong> Registering listener methods with the GAMA runtime</li>
 * <li><strong>Parameter Validation:</strong> Ensuring listener methods have proper signatures</li>
 * <li><strong>Helper Generation:</strong> Creating helper code for listener invocation</li>
 * <li><strong>Dynamic/Static Detection:</strong> Handling both dynamic and static listener patterns</li>
 * <li><strong>Scope Management:</strong> Processing scope parameters when required</li>
 * </ul>
 * 
 * <h3>Listener Patterns:</h3>
 * <p>Listeners can follow different patterns:
 * <ul>
 * <li><strong>Simple:</strong> {@code listener(Object newValue)}</li>
 * <li><strong>With Scope:</strong> {@code listener(IScope scope, Object newValue)}</li>
 * <li><strong>Dynamic:</strong> {@code listener(IAgent agent, Object newValue)} or {@code listener(IScope scope, IAgent agent, Object newValue)}</li>
 * </ul>
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * @listener("speed")
 * public void onSpeedChange(IScope scope, IAgent agent, Double newSpeed) {
 *     // React to speed changes
 *     if (newSpeed > maxSpeed) {
 *         agent.setAttribute("speed", maxSpeed);
 *     }
 * }
 * }</pre>
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see listener
 * @see ElementProcessor
 */
public class ListenerProcessor extends ElementProcessor<listener> {

	@Override
	public void createElement(final StringBuilder sb, final Element e, final listener node) {

		final String clazz = rawNameOf(e.getEnclosingElement().asType());
		final String clazzObject = toClassObject(clazz);
		ExecutableElement ex = (ExecutableElement) e;
		final List<? extends VariableElement> argParams = ex.getParameters();
		final int n = argParams.size();
		if (n == 0) {
			context.emitError(
					"listeners must declare at least one argument corresponding to the new value of the variable (or 2 if the scope is passed)",
					ex);
			return;
		}
		final String[] args = new String[n];
		for (int i = 0; i < args.length; i++) { args[i] = rawNameOf(argParams.get(i).asType()); }

		final boolean scope = n > 0 && args[0].contains("IScope");
		final String method = ex.getSimpleName().toString();
		final boolean isDynamic = scope ? n == 3 : n == 2;
		final String param_class = checkPrim(isDynamic ? args[!scope ? 1 : 2] : args[!scope ? 0 : 1]);

		String listenerHelper = concat("(s,a,t,v)->{if (t != null) ((", clazz, ") t).", method, "(", scope ? "s," : "",
				isDynamic ? "a, " : "", "(" + param_class + ") v); return null; }");

		sb.append("_listener(").append(toJavaString(node.value())).append(',').append(clazzObject).append(',')
				.append(listenerHelper).append(");");

	}

	@Override
	protected Class<listener> getAnnotationClass() { return listener.class; }

}
