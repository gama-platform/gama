/*******************************************************************************************************
 *
 * CompletionStatusMessage.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common;

import gama.core.util.GamaColor;

/**
 *
 */
public record CompletionStatusMessage(String message, Double completion, long timeStamp) implements IStatusMessage {

	@Override
	public StatusType type() {
		return StatusType.REGULAR;
	}

	@Override
	public GamaColor color() {
		return WAIT_AND_TASK_COLOR;
	}

	@Override
	public String icon() {
		return DOWNLOAD_ICON;
	}

}
