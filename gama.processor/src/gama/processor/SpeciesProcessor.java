/*******************************************************************************************************
 *
 * SpeciesProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
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

import gama.annotations.precompiler.GamlAnnotations.species;

/**
 * The Class SpeciesProcessor.
 */
public class SpeciesProcessor extends ElementProcessor<species> {

	@Override
	protected Class<species> getAnnotationClass() { return species.class; }

	@Override
	public void createElement(final StringBuilder sb, final ProcessorContext context, final Element e,
			final species spec) {
		final String clazz = rawNameOf(context, e.asType());
		verifyDoc(context, e, "species " + spec.name(), spec);
		sb.append(in).append("_species(").append(toJavaString(spec.name())).append(",").append(toClassObject(clazz))
				.append(",(p, i)->").append("new ").append(clazz).append("(p, i),");
		toArrayOfStrings(spec.skills(), sb).append(");");
	}

	@Override
	protected boolean validateElement(final ProcessorContext context, final Element e) {
		boolean result =
				assertClassExtends(context, true, (TypeElement) e, context.getType("gama.core.metamodel.agent.IAgent"));
		return result;
	}

}
