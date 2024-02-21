/*******************************************************************************************************
 *
 * IMultiListener.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2.0.0).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama2 for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.camera;

/**
 * The listener interface for receiving keyboard and mouse events from either SWT or NEWT
 *
 *
 */
public interface IMultiListener extends
		/*
		 * org.eclipse.swt.events.KeyListener, MouseListener, MouseMoveListener, MouseTrackListener, MouseWheelListener,
		 */ com.jogamp.newt.event.MouseListener, com.jogamp.newt.event.KeyListener {

	// /**
	// * Method mouseEnter()
	// *
	// * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
	// */
	// @Override
	// default void mouseEnter(final org.eclipse.swt.events.MouseEvent e) {}

	/**
	 * Mouse entered.
	 *
	 * @param e
	 *            the e
	 */
	@Override
	default void mouseEntered(final com.jogamp.newt.event.MouseEvent e) {}

	// /**
	// * Method mouseExit()
	// *
	// * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
	// */
	// @Override
	// default void mouseExit(final org.eclipse.swt.events.MouseEvent e) {}

	/**
	 * Mouse exited.
	 *
	 * @param e
	 *            the e
	 */
	@Override
	default void mouseExited(final com.jogamp.newt.event.MouseEvent e) {}

	// /**
	// * Method mouseHover()
	// *
	// * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
	// */
	// @Override
	// default void mouseHover(final org.eclipse.swt.events.MouseEvent e) {}

}
