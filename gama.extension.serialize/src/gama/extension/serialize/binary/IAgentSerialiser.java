/*******************************************************************************************************
 *
 * IAgentSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.kernel.agent.AgentReference;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.runtime.scope.IScope;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link IAgent} instances. Uses a nesting-depth strategy tracked via the owning serialiser's
 * {@code inAgent} flag: the outermost agent is written as a full {@link SerialisedAgent}, while any nested agent
 * encountered during that serialisation is written as a lightweight {@link AgentReference}. On deserialisation, the
 * boolean flag distinguishes the two cases. Objects deserialised by this serialiser are not registered for
 * back-reference tracking.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IAgentSerialiser extends FSTIndividualSerialiser<IAgent> {

	/**
	 * Returns {@code false}: agents are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises an agent. If the serialiser is already inside an agent serialisation (i.e. {@code serialiser.inAgent}
	 * is {@code true}), the agent is written as an {@link AgentReference} (a boolean {@code true} followed by the
	 * reference). Otherwise, it is written as a full {@link SerialisedAgent} (a boolean {@code false} followed by the
	 * agent data), and the {@code inAgent} flag is set for the duration to detect further nesting.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the agent to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final IAgent o) throws Exception {
		if (serialiser.inAgent) {
			out.writeBoolean(true); // isRef
			out.writeObject(AgentReference.of(o));
		} else {
			serialiser.inAgent = true;
			out.writeBoolean(false); // isRef
			out.writeObject(SerialisedAgent.of(o, true));
			serialiser.inAgent = false;
		}
	}

	/**
	 * Deserialises an agent. Reads the boolean flag: if {@code true}, reads an {@link AgentReference} and resolves it
	 * to a live agent via the scope; if {@code false}, reads a full {@link SerialisedAgent} and recreates the agent in
	 * the simulation.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IAgent}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public IAgent deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		boolean isRef = in.readBoolean();
		if (isRef) {
			AgentReference ref = (AgentReference) in.readObject(AgentReference.class);
			return ref.getReferencedAgent(scope);
		}
		SerialisedAgent sa = (SerialisedAgent) in.readObject(SerialisedAgent.class);
		return sa.recreateIn(scope);
	}

}
