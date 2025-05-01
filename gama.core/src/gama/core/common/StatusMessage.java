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

import gama.core.util.GamaColor;

/**
 * The Class StatusMessage.
 */
public record StatusMessage(String message, StatusType type, String icon, GamaColor color, Object data, long timeStamp)
		implements IStatusMessage {

}