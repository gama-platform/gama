/*******************************************************************************************************
 *
 * CreateAgentsFromSerialisedStringDelegate.java, in gama.serialize, is part of the source code of the GAMA
 * modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.extension.serialize.gaml;

import java.util.List;
import java.util.Map;

import gama.core.common.interfaces.ICreateDelegate;
import gama.core.common.interfaces.ISerialisationConstants;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.util.IList;
import gama.extension.serialize.binary.BinarySerialisation;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.CreateStatement;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
			final List<Map<String, Object>> inits, final CreateStatement statement, final RemoteSequence sequence) {
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
	 * @see gama.core.common.interfaces.ICreateDelegate#acceptSource(IScope, java.lang.Object)
	 */
	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return source instanceof String s && !s.isBlank() && s.getBytes()[0] == GAMA_AGENT_IDENTIFIER;
	}

	/**
	 * @see gama.core.common.interfaces.ICreateDelegate#createFrom(gama.core.runtime.IScope, java.util.List, int,
	 *      java.lang.Object)
	 */
	@Override
	public boolean createFrom(final IScope scope, final List<Map<String, Object>> inits, final Integer max,
			final Object source, final Arguments init, final CreateStatement statement) {
		inits.add(Map.of(SERIALISATION_STRING, source));
		return true;
	}

	/**
	 * Method fromFacetType()
	 *
	 * @see gama.core.common.interfaces.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType fromFacetType() {
		return Types.STRING;
	}
}
