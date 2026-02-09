/*******************************************************************************************************
 *
 * CreateAgentsFromSerialisedStringDelegate.java, in gama.extension.serialize, is part of the source code of the GAMA
 * modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.extension.serialize.gaml;

import java.util.List;
import java.util.Map;

import gama.api.additions.delegates.ICreateDelegate;
import gama.api.constants.ISerialisationConstants;
import gama.api.data.objects.IList;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import gama.extension.serialize.binary.BinarySerialisation;

/**
 * Class CreateFromSavecSimulationDelegate.
 *
 * @author bgaudou
 * @since 18 July 2018
 *
 */

@SuppressWarnings ({ "unchecked", "rawtypes" })
public class CreateAgentsFromSerialisedStringDelegate implements ICreateDelegate, ISerialisationConstants {

	@Override
	public boolean handlesCreation() {
		return true;
	}

	@Override
	public IList<? extends IAgent> createAgents(final IScope scope, final IPopulation<? extends IAgent> pop,
			final List<Map<String, Object>> inits, final IStatement.Create statement, final IStatement sequence) {
		IList<? extends IAgent> agents = pop.createAgents(scope, 1, inits, false, true, null);
		IAgent agent = agents.get(0);
		String path = (String) inits.get(0).get(SERIALISATION_STRING);
		BinarySerialisation.restoreFromString(agent, path);
		// The sequence is executed only after the restoration
		scope.execute(sequence, agent, null);
		return agents;
	}

	/**
	 * Method acceptSource()
	 *
	 * @see gama.api.additions.delegates.ICreateDelegate#acceptSource(IScope, java.lang.Object)
	 */
	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return source instanceof String s && !s.isBlank() && s.getBytes()[0] == GAMA_AGENT_IDENTIFIER;
	}

	/**
	 * @see gama.api.additions.delegates.ICreateDelegate#createFrom(gama.api.runtime.scope.IScope, java.util.List, int,
	 *      java.lang.Object)
	 */
	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object source, final Arguments init, final IStatement.Create statement) {
		inits.add(Map.of(SERIALISATION_STRING, source));
		return true;
	}

	/**
	 * Method fromFacetType()
	 *
	 * @see gama.api.additions.delegates.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType fromFacetType() {
		return Types.STRING;
	}
}
