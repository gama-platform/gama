/*******************************************************************************************************
 *
 * UnitConstantExpression.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;

/**
 * Class UnitConstantExpression.
 *
 * @author drogoul
 * @since 22 avr. 2014
 *
 */
public class UnitConstantExpression extends ConstantExpression implements IExpression.Unit {

	/** The name. */
	String name;

	/** The documentation. */
	IGamlDocumentation documentation;

	/** The alternate names. */
	final java.util.List<String> alternateNames;

	/** The is deprecated. */
	private boolean isDeprecated;

	/**
	 * Instantiates a new unit constant expression.
	 *
	 * @param val
	 *            the val
	 * @param t
	 *            the t
	 * @param name
	 *            the name
	 * @param doc
	 *            the doc
	 * @param names
	 *            the names
	 */
	public UnitConstantExpression(final Object val, final IType<?> t, final String name, final String doc,
			final String[] names) {
		super(val, t);
		this.name = name;
		documentation = new GamlConstantDocumentation(doc);
		alternateNames = new ArrayList<>();
		alternateNames.add(name);
		if (names != null) { alternateNames.addAll(Arrays.asList(names)); }
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "#" + name;
	}

	@Override
	public IGamlDocumentation getDocumentation() { return documentation; }

	@Override
	public String getName() { return name; }

	@Override
	public void setName(final String n) { this.name = n; }

	@Override
	public String getTitle() {
		String prefix;
		if (type.equals(Types.COLOR)) {
			prefix = "Constant color ";
		} else if (getClass().equals(UnitConstantExpression.class)) {
			prefix = "Constant ";
		} else {
			prefix = "Mutable value ";
		}
		StringBuilder s = new StringBuilder().append(prefix).append(serializeToGaml(false));
		if (alternateNames.size() > 1) { s.append(" (").append(alternateNames).append(")"); }
		return s.toString();
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		meta.put(GamlProperties.CONSTANTS, name);
	}

	@Override
	public void setExpression(final IExpression expr) {}

	@Override
	public IExpression compile(final IDescription context) {
		return getExpression();
	}

	@Override
	public IExpression getExpression() { return this; }

	@Override
	public IExpressionDescription compileAsLabel() {
		return GAML.getExpressionDescriptionFactory().createLabel(name);
	}

	@Override
	public boolean equalsString(final String o) {
		return name.equals(o);
	}

	@Override
	public EObject getTarget() { return null; }

	@Override
	public void setTarget(final EObject target) {}

	@Override
	public Set<String> getStrings(final IDescription context, final boolean skills) {
		return Collections.EMPTY_SET;
	}

	@Override
	public IExpressionDescription cleanCopy() {
		return this;
	}

	@Override
	public IType<?> getDenotedType(final IDescription context) {
		return Types.NO_TYPE;
	}

	/**
	 * Sets the deprecated.
	 *
	 * @param deprecated
	 *            the new deprecated
	 */
	public void setDeprecated(final String deprecated) {
		isDeprecated = true;
		documentation.prepend("Deprecated: " + deprecated + ". ");
	}

	/**
	 * Checks if is deprecated.
	 *
	 * @return true, if is deprecated
	 */
	@Override
	public boolean isDeprecated() { return isDeprecated; }

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return getExpression();
	}

}
