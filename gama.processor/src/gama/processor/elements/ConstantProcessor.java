/*******************************************************************************************************
 *
 * ConstantProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import javax.lang.model.element.Element;

import gama.annotations.constant;

/**
 * The ConstantProcessor is responsible for processing {@code @constant} annotations during the annotation processing phase.
 * 
 * <p>This processor handles constant declarations in GAMA, validating their documentation and ensuring proper
 * annotation usage. Unlike other processors, the ConstantProcessor does not generate Java code output, but rather
 * focuses on validation and documentation verification.
 * 
 * <p>Constants in GAMA represent named values that can be used throughout the modeling language. They are typically
 * used to define mathematical constants, configuration values, or other immutable values that models can reference.
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * @constant(value = "pi", doc = "The mathematical constant π (3.14159...)")
 * public static final double PI = Math.PI;
 * }</pre>
 * 
 * <p>The processor validates that all constants have proper documentation through the {@code @doc} annotation
 * or embedded documentation in the {@code @constant} annotation itself.
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see constant
 * @see ElementProcessor
 */
public class ConstantProcessor extends ElementProcessor<constant> {

	/**
	 * Returns the annotation class that this processor handles.
	 * 
	 * @return the {@link constant} annotation class
	 */
	@Override
	protected Class<constant> getAnnotationClass() { return constant.class; }

	/**
	 * Creates the element for a constant annotation.
	 * 
	 * <p>This method processes a constant declaration by validating its documentation.
	 * Unlike other processors, this method does not generate runtime registration code
	 * as constants are handled differently in the GAMA system.
	 * 
	 * <p>The validation ensures that the constant has proper documentation through
	 * either embedded doc attributes in the annotation or separate {@code @doc} annotations.
	 * 
	 * @param sb the StringBuilder (not used for constants as no code is generated)
	 * @param e the element annotated with @constant (typically a field)
	 * @param constant the constant annotation containing the constant's metadata
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final constant constant) {
		verifyDoc(e, "constant " + constant.value(), constant);
	}

	/**
	 * Indicates whether this processor generates Java code output.
	 * 
	 * <p>Constants do not require runtime registration code generation, so this processor
	 * returns {@code false}. The constant values are handled directly by the GAMA
	 * language processing system rather than through generated helper code.
	 * 
	 * @return {@code false} as constants do not generate Java output code
	 */
	@Override
	public boolean outputToJava() {
		return false;
	}

}
