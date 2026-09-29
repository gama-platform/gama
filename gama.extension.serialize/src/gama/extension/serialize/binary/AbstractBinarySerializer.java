/**
 *
 */
package gama.extension.serialize.binary;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public abstract class AbstractBinarySerializer {

	/**
	 * The Class TransientSerializationContext.
	 */
	public static class TransientSerializationContext {
		/**
		 * Flag indicating whether the binarySerialiser is currently inside an agent serialisation. Used by
		 * {@link IAgentSerialiser} to detect nesting and write references instead of full agents.
		 */
		boolean inAgent;
		/**
		 * The current GAMA simulation scope, set before each (de)serialisation operation and cleared afterwards.
		 */
		protected IScope scope;

		/**
		 * Checks if is flag indicating whether the binarySerialiser is currently inside an agent serialisation.
		 *
		 * @return the flag indicating whether the binarySerialiser is currently inside an agent serialisation
		 */
		public boolean isInAgent() { return inAgent; }

		/**
		 * Sets the flag indicating whether the binarySerialiser is currently inside an agent serialisation.
		 *
		 * @param inAgent
		 *            the new flag indicating whether the binarySerialiser is currently inside an agent serialisation
		 */
		public void setInAgent(final boolean inAgent) { this.inAgent = inAgent; }

		/**
		 * Gets the current GAMA simulation scope, set before each (de)serialisation operation and cleared afterwards.
		 *
		 * @return the current GAMA simulation scope, set before each (de)serialisation operation and cleared afterwards
		 */
		public IScope getScope() { return scope; }

		/**
		 * Sets the current GAMA simulation scope, set before each (de)serialisation operation and cleared afterwards.
		 *
		 * @param scope
		 *            the new current GAMA simulation scope, set before each (de)serialisation operation and cleared
		 *            afterwards
		 */
		public void setScope(final IScope scope) { this.scope = scope; }
	}

	/** The context. */
	final TransientSerializationContext context = new TransientSerializationContext();

	/**
	 * Restores the state of a live agent from a previously serialised byte array. The agent's attributes and inner
	 * populations are replaced by the stored values.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the target agent whose state will be restored
	 * @param input
	 *            the byte array produced by a prior serialisation of this agent
	 * @date 8 août 2023
	 */
	public void restoreAgentFromBytes(final IAgent sim, final byte[] input) {
		context.setScope(sim.getScope());
		try {
			SerialisedAgent sa = (SerialisedAgent) fromByteArrayToObject(input);
			sa.restoreAs(context.getScope(), sim);
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, context.getScope());
		} finally {
			context.setScope(null);
		}
	}

	/**
	 * Serialises an object (or agent) to a byte array. If the object is an {@link IAgent}, it is first wrapped in a
	 * {@link SerialisedAgent}.
	 *
	 * @param newScope
	 *            the current GAMA simulation scope
	 * @param obj
	 *            the object to serialise
	 * @return the serialised byte array
	 */
	public byte[] saveObjectToBytes(final IScope newScope, final Object obj) {
		context.setInAgent(false);
		return fromObjectToByteArray(obj instanceof IAgent a ? SerialisedAgent.of(a, true) : obj);
	}

	/**
	 * Deserialises an object from a byte array. If the deserialised result is a {@link SerialisedAgent}, the
	 * corresponding live agent is recreated in the given scope.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param newScope
	 *            the current GAMA simulation scope
	 * @param input
	 *            the byte array to deserialise
	 * @return the deserialised object, or a recreated agent if the data represents a {@link SerialisedAgent}
	 * @date 29 sept. 2023
	 */
	public Object createObjectFromBytes(final IScope newScope, final byte[] input) {
		try {
			context.setScope(newScope);
			Object o = fromByteArrayToObject(input);
			if (o instanceof SerialisedAgent sa) return sa.recreateIn(newScope);
			return o;
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, newScope);
		} finally {
			context.setScope(null);
		}
	}

	/**
	 * From byte array to object.
	 *
	 * @param input
	 *            the input
	 * @return the object
	 */
	abstract Object fromByteArrayToObject(final byte[] input);

	/**
	 * From object to byte array.
	 *
	 * @param obj
	 *            the obj
	 * @return the byte[]
	 */
	abstract byte[] fromObjectToByteArray(Object obj);

	/**
	 * @return
	 */
	public TransientSerializationContext getContext() { return context; }

}