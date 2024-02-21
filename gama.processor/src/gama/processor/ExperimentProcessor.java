/*******************************************************************************************************
 *
 * ExperimentProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import gama.annotations.precompiler.GamlAnnotations.experiment;

/**
 * The Class ExperimentProcessor.
 */
public class ExperimentProcessor extends ElementProcessor<experiment> {

	@Override
	protected Class<experiment> getAnnotationClass() { return experiment.class; }

	@Override
	public void createElement(final StringBuilder sb, final ProcessorContext context, final Element e,
			final experiment exp) {
		verifyDoc(context, e, "experiment " + exp.value(), exp);
		String clazz = e.asType().toString();
		sb.append(in).append("_experiment(").append(toJavaString(exp.value())).append(",(p, i)->new ")
				.append(rawNameOf(context, e.asType())).append("(p, i),").append(clazz).append(".class);");
	}

	@Override
	protected boolean validateElement(final ProcessorContext context, final Element e) {
		boolean result = assertClassExtends(context, true, (TypeElement) e,
				context.getType("gama.core.kernel.experiment.IExperimentAgent"));
		return result;
	}
}
