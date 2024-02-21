/*******************************************************************************************************
 *
 * ExpressionDescriptionBuilder.java, in gaml.compiler.gaml, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import gama.core.common.interfaces.IKeyword;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.gaml.descriptions.ConstantExpressionDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.resource.GamlResourceServices;
import gaml.compiler.gaml.BooleanLiteral;
import gaml.compiler.gaml.DoubleLiteral;
import gaml.compiler.gaml.IntLiteral;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.Unary;
import gaml.compiler.gaml.UnitName;
import gaml.compiler.gaml.util.GamlSwitch;

/**
 * The Class ExpressionDescriptionBuilder.
 */
public class ExpressionDescriptionBuilder extends GamlSwitch<IExpressionDescription> {

	@Override
	public ConstantExpressionDescription caseIntLiteral(final IntLiteral object) {
		ConstantExpressionDescription ed = null;
		try {
			ed = ConstantExpressionDescription.create(Integer.parseInt(object.getOp()));
		} catch (final NumberFormatException e) {
			ed = ConstantExpressionDescription.create(0);
		}
		Resource r = object.eResource();
		if (r != null) { GamlResourceServices.getResourceDocumenter().setGamlDocumentation(r.getURI(), object, ed); }
		return ed;
	}

	@Override
	public ConstantExpressionDescription caseDoubleLiteral(final DoubleLiteral object) {
		ConstantExpressionDescription ed = null;
		try {
			ed = ConstantExpressionDescription.create(Double.parseDouble(object.getOp()));
		} catch (final NumberFormatException e) {
			ed = ConstantExpressionDescription.create(0d);
		}
		Resource r = object.eResource();
		if (r != null) {
			GamlResourceServices.getResourceDocumenter().setGamlDocumentation(r.getURI(), object, ed.getExpression());
		}
		return ed;
	}

	@Override
	public ConstantExpressionDescription caseStringLiteral(final StringLiteral object) {
		final ConstantExpressionDescription ed = ConstantExpressionDescription.create(object.getOp());
		Resource r = object.eResource();
		if (r != null) {
			GamlResourceServices.getResourceDocumenter().setGamlDocumentation(r.getURI(), object, ed.getExpression());
		}
		return ed;
	}

	@Override
	public ConstantExpressionDescription caseBooleanLiteral(final BooleanLiteral object) {
		final ConstantExpressionDescription ed =
				ConstantExpressionDescription.create(IKeyword.TRUE.equals(object.getOp()));
		Resource r = object.eResource();
		if (r != null) {
			GamlResourceServices.getResourceDocumenter().setGamlDocumentation(r.getURI(), object, ed.getExpression());
		}
		return ed;
	}

	@Override
	public IExpressionDescription caseUnitName(final UnitName object) {
		final String s = EGaml.getInstance().getKeyOf(object);
		if (GAML.UNITS.containsKey(s)) return GAML.UNITS.get(s);
		return null;
	}

	@Override
	public IExpressionDescription caseUnary(final Unary object) {
		final String op = EGaml.getInstance().getKeyOf(object);
		if ("#".equals(op)) return doSwitch(object.getRight());
		return null;
	}

	@Override
	public IExpressionDescription defaultCase(final EObject object) {
		return new EcoreBasedExpressionDescription(object);
	}

	/**
	 * Creates the.
	 *
	 * @param e
	 *            the e
	 * @param errors
	 *            the errors
	 * @return the i expression description
	 */
	public IExpressionDescription createBlock(final ISyntacticElement e) {
		return new BlockExpressionDescription(e);
	}

	/**
	 * Creates the.
	 *
	 * @param expr
	 *            the expr
	 * @return the i expression description
	 */
	public IExpressionDescription create(final EObject expr) {
		final IExpressionDescription result = doSwitch(expr);
		result.setTarget(expr);
		return result;
	}

}