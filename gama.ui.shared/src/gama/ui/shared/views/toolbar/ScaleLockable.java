/*******************************************************************************************************
 *
 * ScaleLockable.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

/** A display view whose rendered scale can be locked while its viewport is resized. */
public interface ScaleLockable {

	boolean isScaleLocked();

	void toggleScaleLock();

}
