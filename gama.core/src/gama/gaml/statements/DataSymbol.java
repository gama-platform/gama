/*******************************************************************************************************
 *
 * DataSymbol.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.types.IType;

/**
 * The Class DataSymbol.
 */
@symbol (
		name = IKeyword.DATA_TYPE,
		kind = ISymbolKind.DATA,
		with_sequence = true,
		doc = @doc ("Allows to define a custom data type that can be composed of various elements"))
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = false,
				doc = @doc ("the identifier of the species")) },
		omissible = "name")
public class DataSymbol extends Symbol {

	/**
	 * Instantiates a new data symbol.
	 *
	 * @param desc
	 *            the desc
	 */
	public DataSymbol(final IDescription desc) {
		super(desc);
		setName(description.getName());
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

}