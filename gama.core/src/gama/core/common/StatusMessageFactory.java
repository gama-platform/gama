/*******************************************************************************************************
 *
 * StatusMessageFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common;

import gama.core.common.IStatusMessage.StatusType;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;

/**
 *
 */
public class StatusMessageFactory {

	/**
	 * Inform.
	 *
	 * @param msg
	 *            the msg
	 * @return the status message
	 */
	public static IStatusMessage INFORM(final String msg) {
		return new StatusMessage(msg, StatusType.REGULAR, null, IStatusMessage.INFORM_COLOR, null,
				System.currentTimeMillis());
	}

	/**
	 * Error.
	 *
	 * @param scope
	 *            the scope
	 * @param e
	 *            the e
	 * @return the status message
	 */
	public static IStatusMessage ERROR(final GamaRuntimeException e) {
		return new ErrorStatusMessage(e, System.currentTimeMillis());
	}

	/**
	 * Completion.
	 *
	 * @param msg
	 *            the msg
	 * @param completion
	 *            the completion
	 * @return the status message
	 */
	public static IStatusMessage COMPLETION(final String msg, final Double completion) {
		return new CompletionStatusMessage(msg, completion, System.currentTimeMillis());
	}

	/**
	 * Custom.
	 *
	 * @param msg
	 *            the msg
	 * @param s
	 *            the s
	 * @param icon
	 *            the icon
	 * @param color
	 *            the color
	 * @return the status message
	 */
	public static IStatusMessage CUSTOM(final String msg, final StatusType s, final String icon,
			final GamaColor color) {
		return new StatusMessage(msg, s, icon, color, null, System.currentTimeMillis());
	}

	/** The experiment. */
	private static IStatusMessage EXPERIMENT_UPDATE = new ExperimentStatusMessage();

	/** The idle. */
	private static IStatusMessage IDLE = new IStatusMessage() {
		@Override
		public String message() {
			return "Idle";
		}
	};

	/**
	 * Experiment.
	 *
	 * @return the status message
	 */
	public static IStatusMessage EXPERIMENT() {
		return EXPERIMENT_UPDATE;
	}

	/**
	 * @return
	 */
	public static IStatusMessage IDLE() {
		return IDLE;
	}

}
