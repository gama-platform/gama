/*******************************************************************************************************
 *
 * RestoreStatement.java, in gama.serialize, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.gaml;

import static gama.annotations.precompiler.ISymbolKind.SEQUENCE_STATEMENT;
import static gama.core.common.interfaces.IKeyword.FROM;
import static gama.core.common.interfaces.IKeyword.RESTORE;
import static gama.core.common.interfaces.IKeyword.TARGET;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.IGamaFile;
import gama.extension.serialize.binary.BinarySerialisation;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;

/**
 * This command is used to restore agents from a file or a string in which they have been saved/serialized
 */

/**
 * The Class CreateStatement.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 août 2023
 */
@symbol (
		name = IKeyword.RESTORE,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		with_args = false,
		breakable = false,
		concept = { IConcept.SPECIES, IConcept.SERIALIZE },
		remote_context = false)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
				name = TARGET,
				type = { IType.AGENT },
				optional = false,
				doc = @doc ("The agent to restore. Its attributes will be replaced by the ones stored in the file or string. No verification is done regarding the compatibility ")),
				@facet (
						name = FROM,
						type = { IType.STRING, IType.FILE },
						optional = false,
						doc = @doc ("The file or the string from which to restore the agent")) },
		omissible = IKeyword.TARGET)
@doc ("Allows to restore any agent that has been previously serialised or saved to a file, e.g. `string s <- serialize(a);` ...  `restore a from: s;`"
	+ " or `save simulation to: 'sim.gsim' format: 'binary';` ... `restore simulation from: file('sim.gsim');`")
public class RestoreStatement extends AbstractStatement {

	/** The header. */
	private final IExpression from, target;

	/**
	 * Instantiates a new statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public RestoreStatement(final IDescription desc) {
		super(desc);
		from = getFacet(FROM);
		target = getFacet(TARGET);
		setName(RESTORE);
	}

	/**
	 * Private execute in.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list<? extends I agent>
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public IAgent privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		IAgent agent = Cast.asAgent(scope, target.value(scope));
		if (agent == null) return null;
		if (from == null) return agent;
		Object source = from.value(scope);
		if (source instanceof String string) {
			BinarySerialisation.restoreFromString(agent, string);
		} else if (source instanceof IGamaFile file) {
			BinarySerialisation.restoreFromFile(agent, file.getPath(scope));
		}
		return agent;
	}

}