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
package gama.workspace.status;

import gama.core.common.interfaces.IStatusMessage;
import gama.core.common.interfaces.IStatusMessage.StatusType;
import gama.core.util.IColor;

/**
 * The Class StatusMessage.
 */
public record StatusMessage(String message, StatusType type, String icon, IColor color, Object data, long timeStamp)
		implements IStatusMessage {

}