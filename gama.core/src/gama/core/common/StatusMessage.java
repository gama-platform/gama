/*******************************************************************************************************
 *
 * StatusMessage.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common;

import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;

/**
 * The Class StatusMessage.
 */
public record StatusMessage(String message, StatusType type, String icon, GamaColor color, Boolean begin,
		Double completion, GamaRuntimeException exception, long time) implements IStatusMessage {

	/**
	 * @param msg
	 * @param user
	 * @param icon2
	 * @param color2
	 * @param object
	 * @param object2
	 * @param object3
	 */
	public StatusMessage(final String message, final StatusType type, final String icon, final GamaColor color,
			final Boolean begin, final Double completion, final GamaRuntimeException exception) {
		this(message, type, icon, color, begin, completion, exception, System.currentTimeMillis());
	}

	/**
	 * Inform.
	 *
	 * @param msg
	 *            the msg
	 * @return the status message
	 */
	public static StatusMessage INFORM(final String msg) {
		return new StatusMessage(msg, StatusType.REGULAR, null, INFORM_COLOR, true, null, null);
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
	public static StatusMessage ERROR(final GamaRuntimeException e) {
		return new StatusMessage("Error in experiment", StatusType.ERROR, ERROR_ICON, ERROR_COLOR, null, null, e);
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
	public static StatusMessage COMPLETION(final String msg, final Double completion, final String icon) {
		return new StatusMessage(msg, StatusType.REGULAR, icon, WAIT_AND_TASK_COLOR, false, completion, null);
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
	 * @return the status message
	 */
	public static StatusMessage CREATE(final String msg, final StatusType s, final String icon) {
		return CUSTOM(msg, s, icon, NEUTRAL_COLOR);
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
	public static StatusMessage CUSTOM(final String msg, final StatusType s, final String icon, final GamaColor color) {
		return new StatusMessage(msg, s, icon, color, null, null, null);
	}

	/** The experiment. */
	private static StatusMessage EXPERIMENT_UPDATE =
			new StatusMessage(null, StatusType.EXPERIMENT, null, null, null, null, null);

	/** The idle. */
	private static StatusMessage IDLE = new StatusMessage("Idle", StatusType.NONE, null, null, null, null, null);

	/**
	 * Experiment.
	 *
	 * @return the status message
	 */
	public static StatusMessage EXPERIMENT() {
		return EXPERIMENT_UPDATE;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public StatusType getType() { return type(); }

	/**
	 * @return
	 */
	public static StatusMessage IDLE() {
		return IDLE;
	}

}