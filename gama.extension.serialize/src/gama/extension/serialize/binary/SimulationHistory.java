/*******************************************************************************************************
 *
 * SimulationHistory.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import gama.core.util.ByteArrayZipper;
import gama.dev.DEBUG;
import gama.extension.serialize.binary.SimulationHistory.SimulationHistoryNode;

/**
 * The Class SimulationHistory.
 */
public class SimulationHistory extends LinkedList<SimulationHistoryNode> {

	/**
	 * The Class SimulationHistoryNode.
	 */
	static class SimulationHistoryNode {

		/** The bytes. */
		byte[] bytes;

		/** The cycle. */
		long cycle;

		/**
		 * Instantiates a new history node.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param state
		 *            the state
		 * @date 22 oct. 2023
		 */
		public SimulationHistoryNode(final byte[] state, final long cycle) {
			bytes = state;
			this.cycle = cycle;
		}

	}

	static {
		DEBUG.ON();
	}

	/** The executor. */
	final ExecutorService executor = Executors.newCachedThreadPool();

	/** The delta. Part of https://github.com/mantlik/xdeltaencoder/tree/master to compute diffs */
	// Delta delta = new Delta();

	/** The diffs. */
	LinkedList<SimulationHistoryNode> diffs = new LinkedList<>();

	@Override
	public void push(final SimulationHistoryNode node) {
		// SimulationHistoryNode old = peek();
		asyncProcess(node, System.nanoTime());
		super.push(node);

	}

	// /**
	// * Async zip.
	// *
	// * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	// * @param newFrame
	// * the node
	// * @date 8 août 2023
	// */
	// protected void asyncProcess(final SimulationHistoryNode keyFrame, final SimulationHistoryNode newFrame,
	// final long startTime) {
	// if (keyFrame == null) return;
	// executor.execute(() -> {
	// try {
	// byte[] diff = delta.compute(keyFrame.bytes, newFrame.bytes);
	// SimulationHistoryNode diffNode = new SimulationHistoryNode(diff, newFrame.cycle);
	// diffs.push(diffNode);
	// diffNode.bytes = ByteArrayZipper.zip(diff);
	// DEBUG.OUT("Serialised and compressed to " + diffNode.bytes.length / 1000000d + "Mb in "
	// + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + "ms");
	// } catch (IOException e) {}
	//
	// });
	// }

	/**
	 * Async process.
	 *
	 * @param newFrame
	 *            the new frame
	 * @param startTime
	 *            the start time
	 */
	protected void asyncProcess(final SimulationHistoryNode newFrame, final long startTime) {
		executor.execute(() -> {
			newFrame.bytes = ByteArrayZipper.zip(newFrame.bytes);
			DEBUG.OUT("Serialised and compressed to " + newFrame.bytes.length / 1000000d + "Mb in "
					+ TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + "ms");

		});
	}

}
