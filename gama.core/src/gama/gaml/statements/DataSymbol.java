package gama.gaml.statements;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.gaml.descriptions.IDescription;


@symbol (
		name = IKeyword.DATA_TYPE,
		kind = ISymbolKind.DATA,
		with_sequence = true,
		doc = @doc ("Allows to define a custom data type that can be composed of various elements"))
public class DataSymbol extends AbstractStatement {

	/**
	 * Instantiates a new data symbol.
	 *
	 * @param desc the desc
	 */
	public DataSymbol(final IDescription desc) {
		super(desc);
	}

	@Override
	protected Object privateExecuteIn(final IScope scope) {
		// Data types are structural declarations, they don't need runtime execution
		return null;
	}

}