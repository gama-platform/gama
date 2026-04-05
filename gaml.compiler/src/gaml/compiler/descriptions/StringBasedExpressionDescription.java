/*******************************************************************************************************
 *
 * StringBasedExpressionDescription.java, in gama.core, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.descriptions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;

/**
 * The class StringBasedExpressionDescription.
 *
 * @author drogoul
 * @since 31 mars 2012
 *
 */
@SuppressWarnings ({ "rawtypes" })
public class StringBasedExpressionDescription extends BasicExpressionDescription {

	/** The string. */
	String string;

	/**
	 * Instantiates a new string based expression description.
	 *
	 * @param s
	 *            the s
	 */
	public StringBasedExpressionDescription(final String s) {
		super((EObject) null);
		string = s;
	}

	@Override
	public String toOwnString() {
		return string;
	}

	@Override
	public IExpressionDescription compileAsLabel() {
		return new LabelExpressionDescription(string);
	}

	@Override
	public Set<String> getStrings(final IDescription context, final boolean skills) {
		// Assuming of the form [aaa, bbb]
		final Set<String> result = new HashSet<>();
		final StringBuilder b = new StringBuilder();
		for (final char c : string.toCharArray()) {
			switch (c) {
				case '[':
				case ' ':
					break;
				case ']':
				case ',': {
					result.add(b.toString());
					b.setLength(0);
					break;
				}
				default:
					b.append(c);
			}
		}
		return result;
	}

	@Override
	public IExpressionDescription cleanCopy() {
		final IExpressionDescription copy = new StringBasedExpressionDescription(string);
		copy.setTarget(target);
		return copy;
	}

	@Override
	public IType<?> getDenotedType(final IDescription context) {
		if ("0".equals(string)) return Types.NO_TYPE;
		IType type = context.getTypeNamed(string);
		if (type == Types.NO_TYPE) { type = super.getDenotedType(context); }
		return type;
	}

}
