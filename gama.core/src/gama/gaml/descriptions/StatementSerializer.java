/*******************************************************************************************************
 *
 * StatementSerializer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import com.google.common.collect.Iterables;

import gama.annotations.precompiler.GamlProperties;
import gama.gaml.operators.Strings;
import gama.gaml.statements.Arguments;

/**
 * The Class StatementSerializer.
 */
public class StatementSerializer extends SymbolSerializer {

	/**
	 * Collect meta information in facets.
	 *
	 * @param desc
	 *            the desc
	 * @param plugins
	 *            the plugins
	 */
	@Override
	protected void collectMetaInformationInFacets(final IDescription desc, final GamlProperties plugins) {
		super.collectMetaInformationInFacets(desc, plugins);
		// if (desc.formalArgs == null || desc.formalArgs.isEmpty()) {
		// return;
		// }
		// for (final StatementDescription arg : desc.formalArgs.values()) {
		// collectMetaInformation(arg, plugins);
		// }
	}

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
	protected void serializeFacets(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
		super.serializeFacets(s, sb, includingBuiltIn);
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
		final StatementDescription desc = (StatementDescription) s;

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
				if (Strings.isGamaNumber(name)) {
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