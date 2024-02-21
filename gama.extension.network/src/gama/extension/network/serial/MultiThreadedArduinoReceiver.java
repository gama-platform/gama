/*******************************************************************************************************
 *
 * MultiThreadedArduinoReceiver.java, in gama.network, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.network.serial;

import gama.core.messaging.GamaMailbox;
import gama.core.messaging.GamaMessage;
import gama.core.messaging.MessagingSkill;
import gama.core.metamodel.agent.IAgent;
import gama.dev.DEBUG;
import gama.dev.THREADS;

/**
 * The Class MultiThreadedArduinoReceiver.
 */
public class MultiThreadedArduinoReceiver extends Thread {
	static {
		DEBUG.ON();
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
		System.out.println("START OF THE THREAD");

		// Successfully created Server Socket. Now wait for connections.
		while (!closed) {
			System.out.println("enter while");

			try {
				if (myAgent.dead()) { this.interrupt(); }
				// System.out.println("not dead");

				final String sentence = arduino.serialRead(1);

				@SuppressWarnings ("unchecked") GamaMailbox<GamaMessage> mailbox =
						(GamaMailbox<GamaMessage>) myAgent.getAttribute(MessagingSkill.MAILBOX_ATTRIBUTE);
				if (mailbox == null) {
					mailbox = new GamaMailbox<>();
					myAgent.setAttribute(MessagingSkill.MAILBOX_ATTRIBUTE, mailbox);
				}

				// IList<ConnectorMessage> msgs = (IList<ConnectorMessage>) myAgent.getAttribute("messages" + myAgent);
				// if (msgs == null) {
				// msgs = GamaListFactory.create(ConnectorMessage.class);
				// }
				if (myAgent.dead()) { this.interrupt(); }

				// System.out.println("sentence = " + sentence);

				GamaMessage msg = new GamaMessage(myAgent.getScope(), "Arduino", myAgent.getName(), sentence);

				// final NetworkMessage msg = MessageFactory.buildNetworkMessage("Arduino", sentence);
				mailbox.add(msg);

				// msgs.addValue(myAgent.getScope(), msg);
				// System.out.println("sentence = " + msg.getPlainContents());

				// myAgent.setAttribute("messages" + myAgent, msgs);
				// System.out.println("not dead");

			} catch (final Exception ioe) {
				closed = true;
				this.interrupt();
				ioe.printStackTrace();
			}
			// System.out.println("avt wait");
			THREADS.WAIT(timer);
			// System.out.println("WAIT off!");
		}
		// System.out.println("stop stop off!");

	}
}
