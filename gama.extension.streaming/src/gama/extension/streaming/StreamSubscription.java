/*******************************************************************************************************
 *
 * StreamSubscription.java, in gama.extension.streaming, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.streaming;

/**
 * Describes a client request to stream one display of a running experiment: which display, at which resolution, and at
 * which decimation of the simulation cycles (1 = every cycle, n = one cycle out of n).
 */
public class StreamSubscription {

	/** The original name of the display to stream. */
	public final String display;

	/** The width in pixels of the rendered frames. */
	public final int width;

	/** The height in pixels of the rendered frames. */
	public final int height;

	/** The frame rate, expressed as a number of simulation cycles between two frames (>= 1). */
	public final int frameRate;

	/** The last simulation cycle for which a frame has been sent, to avoid sending the same cycle twice. */
	private volatile int lastSentCycle = -1;

	/**
	 * Instantiates a new stream subscription.
	 *
	 * @param display
	 *            the original name of the display
	 * @param width
	 *            the width in pixels
	 * @param height
	 *            the height in pixels
	 * @param frameRate
	 *            the number of cycles between two frames (clamped to a minimum of 1)
	 */
	public StreamSubscription(final String display, final int width, final int height, final int frameRate) {
		this.display = display;
		this.width = Math.max(1, width);
		this.height = Math.max(1, height);
		this.frameRate = Math.max(1, frameRate);
	}

	/**
	 * Returns whether a frame should be sent for the given simulation cycle: the cycle must be a new one (not already
	 * sent) and must fall on the configured decimation.
	 *
	 * @param cycle
	 *            the current simulation cycle
	 * @return true if a frame should be sent now
	 */
	public boolean shouldSend(final int cycle) {
		return cycle != lastSentCycle && cycle % frameRate == 0;
	}

	/**
	 * Records that a frame has been sent for the given cycle.
	 *
	 * @param cycle
	 *            the cycle that has just been streamed
	 */
	public void markSent(final int cycle) {
		lastSentCycle = cycle;
	}

}
