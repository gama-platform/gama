/*******************************************************************************************************
 *
 * TypeFieldExpression.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;

/**
 * The Class TypeFieldExpression.
 */
public class TypeFieldExpression extends UnaryOperator {

	/**
	 * Instantiates a new type field expression.
	 *
	 * @param artefact
	 *            the artefact
	 * @param context
	 *            the context
	 * @param expr
	 *            the expr
	 */
	public TypeFieldExpression(final IArtefact.Operator proto, final IDescription context,
			final IExpression expr) {
		super(proto, context, expr);
	}

	@Override
	public TypeFieldExpression resolveAgainst(final IScope scope) {
		return new TypeFieldExpression(prototype, null, child.resolveAgainst(scope));
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		parenthesize(sb, child);
		sb.append(".").append(getName());
		return sb.toString();
	}

	@Override
	public String toString() {
		if (child == null) return prototype.getSignature().toString() + "." + getName();
		return child.serializeToGaml(false) + "." + getName();
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		final StringBuilder sb = new StringBuilder(200);
		if (child != null) { sb.append("Defined on objects of type " + child.getGamlType().getName()); }
		final vars annot = prototype.getJavaBase().getAnnotation(vars.class);
		if (annot != null) {
			final variable[] allVars = annot.value();
			for (final variable v : allVars) {
				if (v.name().equals(getName()) && v.doc().length > 0) {
					sb.append("<br/>");
					sb.append(v.doc()[0].value());
				}
			}
		}
		return new GamlRegularDocumentation(sb);
	}

	@Override
	public String getTitle() { return "field <b>" + getName() + "</b> of type " + getGamlType().getName(); }

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		meta.put(GamlProperties.ATTRIBUTES, getName());
	}

}
