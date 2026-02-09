/*******************************************************************************************************
 *
 * StatementSerializer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.serialization;

import com.google.common.collect.Iterables;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.utils.StringUtils;

/**
 * The Class StatementSerializer.
 */
public class StatementSerializer implements ISymbolSerializer {

	/**
	 * Serialize facets.
	 *
	 * @param s
	 *            the s
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	@Override
	public void serializeFacets(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
		ISymbolSerializer.super.serializeFacets(s, sb, includingBuiltIn);
		serializeArgs(s, sb, includingBuiltIn);

	}

	/**
	 * Serialize args.
	 *
	 * @param s
	 *            the s
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	protected void serializeArgs(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
		final IStatementDescription desc = (IStatementDescription) s;

		final Iterable<IDescription> formalArgs = desc.getFormalArgs();
		if (!Iterables.isEmpty(formalArgs)) {
			sb.append("(");
			for (final IDescription arg : formalArgs) {
				serializeArg(desc, arg, sb, includingBuiltIn);
				sb.append(", ");
			}
		} else {
			final Arguments passedArgs = desc.getPassedArgs();
			if (passedArgs.isEmpty()) return;
			sb.append("(");
			passedArgs.forEachFacet((name, value) -> {
				if (StringUtils.isGamaNumber(name)) {
					sb.append(value.serializeToGaml(includingBuiltIn));
				} else {
					sb.append(name).append(":").append(value.serializeToGaml(includingBuiltIn));
				}
				sb.append(", ");
				return true;
			});

		}
		sb.setLength(sb.length() - 2);
		sb.append(")");
	}

	/**
	 * Serialize arg.
	 *
	 * @param desc
	 *            the desc
	 * @param arg
	 *            the arg
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	protected void serializeArg(final IDescription desc, final IDescription arg, final StringBuilder sb,
			final boolean includingBuiltIn) {
		// normally never called as it is redefined for action, do and
		// create
	}

}