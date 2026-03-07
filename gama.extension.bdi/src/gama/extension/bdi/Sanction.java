/*******************************************************************************************************
 *
 * Sanction.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.bdi;

import java.util.Objects;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IValue;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Class Sanction.
 */
@vars ({ @variable (
		name = IKeyword.NAME,
		type = IType.STRING,
		doc = @doc ("The name of this sanction")),

})
public class Sanction implements IValue {

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), IKeyword.NAME, getName());
	}

	/** The sanction statement. */
	private SanctionStatement sanctionStatement;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@getter (IKeyword.NAME)
	public String getName() { return this.sanctionStatement.getName(); }

	/**
	 * Gets the sanction statement.
	 *
	 * @return the sanction statement
	 */
	public SanctionStatement getSanctionStatement() { return this.sanctionStatement; }

	/**
	 * Instantiates a new sanction.
	 */
	public Sanction() {}

	/**
	 * Instantiates a new sanction.
	 *
	 * @param statement
	 *            the statement
	 */
	public Sanction(final SanctionStatement statement) {
		this.sanctionStatement = statement;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Sanction other = (Sanction) obj;
		if (!Objects.equals(sanctionStatement, other.sanctionStatement)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sanctionStatement);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "Sanction( " + sanctionStatement + ")";
	}

	@Override
	public IType<?> getGamlType() {
		return Types.get(SanctionType.id);
		// return null;
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "Sanction( " + sanctionStatement + ")";
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		return new Sanction(sanctionStatement);
	}

}
