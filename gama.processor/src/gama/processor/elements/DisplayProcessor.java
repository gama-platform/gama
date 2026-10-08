/*******************************************************************************************************
 *
 * DisplayProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import gama.annotations.display;
import gama.processor.Constants;

/**
 * The DisplayProcessor is responsible for processing {@code @display} annotations during the annotation processing
 * phase.
 *
 * <p>
 * Display processors handle the registration of custom display surface implementations that can be used to visualize
 * simulation data in GAMA. Display surfaces define how simulation outputs are rendered and presented to users,
 * including 2D displays, 3D displays, charts, and other visualization components.
 *
 * <p>
 * This processor handles:
 * <ul>
 * <li><strong>Display Registration:</strong> Registering display surface types with the GAMA runtime</li>
 * <li><strong>Factory Generation:</strong> Creating factory methods for display surface instantiation</li>
 * <li><strong>Validation:</strong> Ensuring display classes implement the required interfaces</li>
 * <li><strong>Documentation Validation:</strong> Verifying proper documentation for display types</li>
 * </ul>
 *
 * <h3>Display Structure:</h3>
 * <p>
 * A display definition includes:
 * <ul>
 * <li>One or more names for the display type</li>
 * <li>The implementing Java class that handles the visualization</li>
 * <li>Constructor logic for creating display instances</li>
 * <li>Proper inheritance from IDisplaySurface</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 *
 * <pre>{@code
 * @display ({ "java2D", "2d" })
 * public class Java2DDisplaySurface implements IDisplaySurface {
 * 
 * 	public Java2DDisplaySurface(Object... args) {
 * 		// Display surface implementation
 * 	}
 * 
 * 	// IDisplaySurface implementation methods
 * }
 * }</pre>
 *
 * @author GAMA Development Team
 * @since 1.0
 * @see display
 * @see ElementProcessor
 */
public class DisplayProcessor extends ElementProcessor<display> {

	/**
	 * Returns the annotation class that this processor handles.
	 *
	 * @return the {@link display} annotation class
	 */
	@Override
	protected Class<display> getAnnotationClass() { return display.class; }

	/**
	 * Creates the element code for a display annotation.
	 *
	 * <p>
	 * This method generates the runtime registration code for display surface types. It processes each display name
	 * defined in the annotation and creates the necessary registration code for the GAMA display system.
	 *
	 * <p>
	 * The generated code includes:
	 * <ul>
	 * <li>Display name registration</li>
	 * <li>Class reference for the display implementation</li>
	 * <li>Factory lambda for creating display instances</li>
	 * </ul>
	 *
	 * @param sb
	 *            the StringBuilder to append the generated registration code to
	 * @param e
	 *            the class element annotated with @display
	 * @param d
	 *            the display annotation containing display metadata
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final display d) {
		String[] names = d.value();
		if (names == null) return;
		for (String name : names) {
			verifyDoc(e, "display " + name, d);
			String clazz = rawNameOf(e.asType());
			sb.append(in).append("_display(").append(toJavaString(name)).append(",").append(clazz)
					.append(".class,(s, o)->new ").append(clazz).append("(s, o));");
		}

	}

	/**
	 * Validates that a display element meets the requirements for display processing.
	 *
	 * <p>
	 * This method ensures that the display class properly implements the IDisplaySurface interface, which is required
	 * for all display surface implementations in GAMA. The IDisplaySurface interface provides the contract for:
	 * <ul>
	 * <li>Rendering simulation outputs</li>
	 * <li>Handling user interactions</li>
	 * <li>Managing display lifecycle</li>
	 * <li>Integration with the GAMA display system</li>
	 * </ul>
	 *
	 * @param e
	 *            the element to validate (should be a class annotated with @display)
	 * @return {@code true} if the element implements IDisplaySurface, {@code false} otherwise
	 */
	@Override
	protected boolean validateElement(final Element e) {
		return assertClassExtends(true, (TypeElement) e, context.getType(Constants.IDisplaySurfaceClassName));
	}

}
