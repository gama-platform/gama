/*******************************************************************************************************
 *
 * ClassProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import gama.annotations.gaml_class;

/**
 * Generates registration and instance factories for Java-backed GAML classes.
 */
public class ClassProcessor extends ElementProcessor<gaml_class> {

	@Override
	protected Class<gaml_class> getAnnotationClass() { return gaml_class.class; }

	@Override
	public void createElement(final StringBuilder sb, final Element e, final gaml_class annotation) {
		final String clazz = rawNameOf(e.asType());
		verifyDoc(e, "class " + annotation.name(), annotation);
		sb.append(in).append("_class(").append(toJavaString(annotation.name())).append(",")
				.append(toClassObject(clazz)).append(",");
		if (e.getModifiers().contains(Modifier.ABSTRACT)) {
			sb.append("null");
		} else {
			sb.append("(s,c)->new ").append(clazz).append("(s,c)");
		}
		sb.append(");");
	}

	@Override
	protected boolean validateElement(final Element e) {
		if (!(e instanceof TypeElement type) || e.getKind() != ElementKind.CLASS) {
			context.emitError("@gaml_class requires a Java class", e);
			return false;
		}
		if (!assertClassExtends(true, type, context.getType("gama.api.kernel.object.IObject"))) return false;
		if (context.getTypeUtils().isAssignable(e.asType(), context.getIAgent())) {
			context.emitError("Agents must be registered with @species, not @gaml_class", e);
			return false;
		}
		if (!e.getModifiers().contains(Modifier.PUBLIC)
				|| e.getEnclosingElement() instanceof TypeElement && !e.getModifiers().contains(Modifier.STATIC)) {
			context.emitError("@gaml_class requires a public top-level or static nested class", e);
			return false;
		}
		if (e.getAnnotation(gaml_class.class).name().isBlank()) {
			context.emitError("A built-in GAML class needs a non-empty name", e);
			return false;
		}
		if (e.getModifiers().contains(Modifier.ABSTRACT)) return true;
		for (final Element child : e.getEnclosedElements()) {
			if (child.getKind() != ElementKind.CONSTRUCTOR || !child.getModifiers().contains(Modifier.PUBLIC))
				continue;
			final var params = ((ExecutableElement) child).getParameters();
			if (params.size() == 2
					&& context.getTypeUtils().isSameType(params.get(0).asType(), context.getIScope())
					&& context.getTypeUtils().isSameType(params.get(1).asType(),
							context.getType("gama.api.kernel.object.IClass")))
				return true;
		}
		context.emitError("@gaml_class requires a public constructor (IScope scope, IClass clazz)", e);
		return false;
	}
}
