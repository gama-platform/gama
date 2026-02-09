/*******************************************************************************************************
 *
 * SanctionStatement.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * The Class SanctionStatement.
 */
@symbol (
		name = { SanctionStatement.SANCTION },
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		concept = { IConcept.BDI })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = true) },
		omissible = IKeyword.NAME)
@doc ("declare the actions an agent execute when enforcing norms of others during a perception")
public class SanctionStatement extends AbstractStatementSequence {

	/** The Constant SANCTION. */
	public static final String SANCTION = "sanction";

	/**
	 * Instantiates a new sanction statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public SanctionStatement(final IDescription desc) {
		super(desc);
		setName(desc.getName());
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		// if (_when == null || Cast.asBool(scope, _when.value(scope))) {
		return super.privateExecuteIn(scope);
		// }
		// return null;
	}

}
