/*******************************************************************************************************
 *
 * ExperimentStatusMessage.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common;

/**
 *
 */
public record ExperimentStatusMessage() implements IStatusMessage {

	@Override
	public StatusType type() {
		return StatusType.EXPERIMENT;
	}

	@Override
	public String message() {
		return "Experiment update";
	}

}
