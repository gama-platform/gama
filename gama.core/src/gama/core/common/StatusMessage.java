/*******************************************************************************************************
 *
 * StatusMessage.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.common;

import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IStatusMessage;
import gama.core.util.GamaColor;

/**
 * The Class StatusMessage.
 */
public class StatusMessage implements IStatusMessage {

	/** The message. */
	String message = "";
	
	/** The code. */
	protected int code = IGui.INFORM;
	
	/** The icon. */
	protected String icon;

	/**
	 * Instantiates a new status message.
	 *
	 * @param msg the msg
	 * @param s the s
	 */
	public StatusMessage(final String msg, final int s) {
		message = msg;
		code = s;
	}

	/**
	 * Instantiates a new status message.
	 *
	 * @param msg the msg
	 * @param s the s
	 * @param icon the icon
	 */
	public StatusMessage(final String msg, final int s, final String icon) {
		message = msg;
		this.icon = icon;
		code = s;
	}

	@Override
	public String getText() {
		return message;
	}

	@Override
	public int getCode() {
		return code;
	}

	/**
	 * Method getColor()
	 * 
	 * @see gama.core.common.interfaces.IStatusMessage#getColor()
	 */
	@Override
	public GamaColor getColor() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gama.core.common.IStatusMessage#getIcon()
	 */
	@Override
	public String getIcon() {
		return icon;
	}

}