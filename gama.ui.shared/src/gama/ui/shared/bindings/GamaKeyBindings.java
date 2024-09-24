/*******************************************************************************************************
 *
 * GamaKeyBindings.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.bindings;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import gama.core.runtime.GAMA;
import gama.core.runtime.PlatformHelper;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.shared.utils.ViewsHelper;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.IGamlEditor;

/**
 * The purpose of this class is to install global key bindings that can work in any of the contexts of GAMA (incl.
 * fullscreen)
 *
 * @author drogoul
 *
 */
public class GamaKeyBindings implements Listener {

	static {
		// DEBUG.ON();
	}

	/** The command. */
	public static final int COMMAND = PlatformHelper.isMac() ? SWT.COMMAND : SWT.CTRL;

	/** The search string. */
	public static final String SEARCH_STRING = format(COMMAND + SWT.SHIFT, 'H');

	/** The play string. */
	public static final String PLAY_STRING = format(COMMAND, 'P');

	/** The step string. */
	public static final String STEP_STRING = format(COMMAND + SWT.SHIFT, 'P');

	/** The reload string. */
	public static final String RELOAD_STRING = format(COMMAND, 'R');

	/** The quit string. */
	public static final String QUIT_STRING = format(COMMAND + SWT.SHIFT, 'X');

	/**
	 * The Class PluggableBinding.
	 */
	public static abstract class PluggableBinding implements Runnable {

		/** The key. */
		final KeyStroke key;

		/**
		 * Instantiates a new pluggable binding.
		 *
		 * @param modifiers
		 *            the modifiers
		 * @param keyCode
		 *            the key code
		 */
		public PluggableBinding(final int modifiers, final int keyCode) {
			this.key = KeyStroke.getInstance(modifiers, keyCode);
		}

	}

	/** The Constant bindings. */
	private static final Map<KeyStroke, PluggableBinding> bindings = new LinkedHashMap<>();

	/**
	 * Instantiates a new gama key bindings.
	 */
	GamaKeyBindings() {}

	@Override
	public void handleEvent(final Event event) {
		if (event.keyCode == SWT.ESC) {
			if (ViewsHelper.toggleFullScreenMode()) {
				// DEBUG.OUT("Toogle full screen");
				consume(event);
			}
			return;
		}
		if (event.stateMask == 0) return;

		switch (event.keyCode) {

			case 'h':
				if (ctrl(event) && shift(event)) {
					consume(event);
					IGamlEditor ed = WorkbenchHelper.getActiveEditor();
					if (ed != null) {
						ed.searchReference();
						// GamlSearchField.INSTANCE.search();
					}
				}
				break;
			// Handles START, PAUSE & STEP
			case 'p':
				if (ctrl(event) && shift(event)) {
					consume(event);
					GAMA.stepFrontmostExperiment(false);
				} else if (ctrl(event)) {
					consume(event);
					GAMA.startPauseFrontmostExperiment(false);
				}
				break;
			// Handles RELOAD & RELAUNCH
			case 'r': {
				if (PerspectiveHelper.isModelingPerspective()) {
					// See Issue #2741
					break;
				}
				if (ctrl(event) && shift(event)) {
					// DEBUG.OUT("SHIFT CONTROL R Pressed");
					consume(event);
					GAMA.relaunchFrontmostExperiment();
				} else if (ctrl(event)) {
					// DEBUG.OUT("CONTROL R Pressed");
					consume(event);
					GAMA.reloadFrontmostExperiment(false);
				}
				break;
			}
			// Handles CLOSE
			case 'x':
				if (ctrl(event) && shift(event)) {
					consume(event);
					GAMA.closeAllExperiments(true, false);
				}
				break;
			default:
				// DEBUG.LOG(" KEY CODE " + event.keyCode + " MODS " + event.stateMask);
				final PluggableBinding pb = bindings.get(KeyStroke.getInstance(event.stateMask, event.keyCode));
				if (pb != null) {
					consume(event);
					pb.run();
				}
		}

	}

	/**
	 * Consume.
	 *
	 * @param event
	 *            the event
	 */
	private void consume(final Event event) {
		event.doit = false;
		event.type = SWT.None;
	}

	/** The Constant BINDINGS. */
	private final static GamaKeyBindings BINDINGS = new GamaKeyBindings();

	/**
	 * Install.
	 */
	public static void install() {
		WorkbenchHelper.run(() -> WorkbenchHelper.getDisplay().addFilter(SWT.KeyDown, BINDINGS));
	}

	/**
	 * Ctrl.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	public static boolean ctrl(final Event e) {
		return (e.stateMask & COMMAND) != 0;
	}

	/**
	 * Ctrl.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	public static boolean ctrl(final KeyEvent e) {
		return (e.stateMask & COMMAND) != 0;
	}

	/**
	 * Ctrl.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	public static boolean ctrl(final MouseEvent e) {
		return (e.stateMask & COMMAND) != 0;
	}

	/**
	 * Shift.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */

	public static boolean shift(final KeyEvent e) {
		return (e.stateMask & SWT.SHIFT) != 0;
	}

	/**
	 * Shift.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	public static boolean shift(final Event e) {
		return (e.stateMask & SWT.SHIFT) != 0;
	}

	/**
	 * Shift.
	 *
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	public static boolean shift(final MouseEvent e) {
		return (e.stateMask & SWT.SHIFT) != 0;
	}

	/**
	 * Format.
	 *
	 * @param mod
	 *            the mod
	 * @param key
	 *            the key
	 * @return the string
	 */
	public static String format(final int mod, final int key) {

		return SWTKeySupport.getKeyFormatterForPlatform().format(KeyStroke.getInstance(mod, key));
	}

	/**
	 * Plug.
	 *
	 * @param newBinding
	 *            the new binding
	 */
	public static void plug(final PluggableBinding newBinding) {
		bindings.put(newBinding.key, newBinding);
		// DEBUG.LOG(
		// "INSTALLING KEY CODE " + newBinding.key.getNaturalKey() + " MODS " + newBinding.key.getModifierKeys());

	}

}
