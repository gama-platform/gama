/*******************************************************************************************************
 *
 * ErrorStatusMessage.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
 *
 */
public record ErrorStatusMessage(GamaRuntimeException exception, long timeStamp) implements IStatusMessage {

	@Override
	public StatusType type() {
		return StatusType.ERROR;
	}

	@Override
	public GamaColor color() {
		return ERROR_COLOR;
	}

	@Override
	public String icon() {
		return ERROR_ICON;
	}

	@Override
	public String message() {
		return "Error in experiment: " + exception.getMessage();
	}

	@Override
	public boolean isError() { return true; }
}
