/*******************************************************************************************************
 *
 * ConstantProcessor.java, in gama.processor, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.processor;

import javax.lang.model.element.Element;

import gama.annotations.precompiler.GamlAnnotations.constant;

/**
 * The Class ConstantProcessor.
 */
public class ConstantProcessor extends ElementProcessor<constant> {

	@Override
	protected Class<constant> getAnnotationClass() {
		return constant.class;
	}

	@Override
	public void createElement(final StringBuilder sb, final ProcessorContext context, final Element e,
			final constant constant) {
		verifyDoc(context, e, "constant " + constant.value(), constant);
	}

	@Override
	public boolean outputToJava() {
		return false;
	}

}
