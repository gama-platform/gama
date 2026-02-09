/*******************************************************************************************************
 *
 * MultiThreadedArduinoReceiver.java, in gama.extension.network, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.serial;

import gama.api.data.factories.GamaMessageFactory;
import gama.api.data.objects.IMessage;
import gama.api.kernel.agent.IAgent;
import gama.core.util.messaging.GamaMailbox;
import gama.core.util.messaging.MessagingSkill;
import gama.dev.DEBUG;
import gama.dev.THREADS;

/**
 * The Class MultiThreadedArduinoReceiver.
 */
public class MultiThreadedArduinoReceiver extends Thread {
	static {
		DEBUG.OFF();
	}

	/** The my agent. */
	private final IAgent myAgent;

	/** The closed. */
	private volatile boolean closed = false;

	/** The timer. */
	private int timer = 1000;

	/** The arduino. */
	private final MyArduino arduino;

	/**
	 * Instantiates a new multi threaded arduino receiver.
	 *
	 * @param a
	 *            the a
	 * @param _timer
	 *            the timer
	 * @param ard
	 *            the ard
	 */
	public MultiThreadedArduinoReceiver(final IAgent a, final int _timer, final MyArduino ard) {
		myAgent = a;
		arduino = ard;
		timer = _timer;
	}

	@Override
	public void run() {
		DEBUG.LOG("START OF THE THREAD");

		// Successfully created Server Socket. Now wait for connections.
		while (!closed) {
			DEBUG.LOG("enter while");

			try {
				if (myAgent.dead()) { this.interrupt(); }
				// DEBUG.LOG("not dead");

				final String sentence = arduino.serialRead(1);

				@SuppressWarnings ("unchecked") GamaMailbox<IMessage> mailbox =
						(GamaMailbox<IMessage>) myAgent.getAttribute(MessagingSkill.MAILBOX_ATTRIBUTE);
				if (mailbox == null) {
					mailbox = new GamaMailbox<>();
					myAgent.setAttribute(MessagingSkill.MAILBOX_ATTRIBUTE, mailbox);
				}

				// IList<ConnectorMessage> msgs = (IList<ConnectorMessage>) myAgent.getAttribute("messages" + myAgent);
				// if (msgs == null) {
				// msgs = GamaListFactory.create(ConnectorMessage.class);
				// }
				if (myAgent.dead()) { this.interrupt(); }

				// DEBUG.LOG("sentence = " + sentence);

				IMessage msg = GamaMessageFactory.create(myAgent.getScope(), "Arduino", myAgent.getName(), sentence);

				// final NetworkMessage msg = MessageFactory.buildNetworkMessage("Arduino", sentence);
				mailbox.add(msg);

				// msgs.addValue(myAgent.getScope(), msg);
				// DEBUG.LOG("sentence = " + msg.getPlainContents());

				// myAgent.setAttribute("messages" + myAgent, msgs);
				// DEBUG.LOG("not dead");

			} catch (final Exception ioe) {
				closed = true;
				this.interrupt();
				ioe.printStackTrace();
			}
			// DEBUG.LOG("avt wait");
			THREADS.WAIT(timer);
			// DEBUG.LOG("WAIT off!");
		}
		// DEBUG.LOG("stop stop off!");

	}
}
