/*******************************************************************************************************
 *
 * IEventLayerDelegate.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.delegates;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import gama.annotations.constant;
import gama.annotations.doc;
import gama.annotations.support.IOperatorCategory;
import gama.api.constants.IKeyword;
import gama.api.gaml.statements.IStatement;
import gama.api.runtime.scope.IScope;

/**
 * Class IEventLayerDelegate. Represents a delegate to the EventLayers that accepts other inputs than keyboard inputs
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
public interface IEventLayerDelegate {

	/** The Constant MOUSE_DOWN. */
	@constant (
			value = IKeyword.MOUSE_DOWN,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the mouse button")) String MOUSE_DOWN =
					IKeyword.MOUSE_DOWN;

	/** The Constant MOUSE_UP. */
	@constant (
			value = IKeyword.MOUSE_UP,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user releases the mouse button")) String MOUSE_UP =
					IKeyword.MOUSE_UP;

	/** The Constant MOUSE_MOVED. */
	@constant (
			value = IKeyword.MOUSE_MOVE,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user moves the mouse")) String MOUSE_MOVED =
					IKeyword.MOUSE_MOVE;

	/** The Constant MOUSE_CLICKED. */
	@constant (
			value = IKeyword.MOUSE_CLICK,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses and releases the mouse button immediately")) String MOUSE_CLICKED =
					IKeyword.MOUSE_CLICK;

	/** The Constant MOUSE_ENTERED. */
	@constant (
			value = IKeyword.MOUSE_ENTER,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the mouse enters the display")) String MOUSE_ENTERED =
					IKeyword.MOUSE_ENTER;

	/** The Constant MOUSE_EXITED. */
	@constant (
			value = IKeyword.MOUSE_EXIT,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the mouse leaves the display")) String MOUSE_EXITED =
					IKeyword.MOUSE_EXIT;

	/** The Constant MOUSE_MENU. */
	@constant (
			value = IKeyword.MOUSE_MENU,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user invokes the contextual menu")) String MOUSE_MENU =
					IKeyword.MOUSE_MENU;

	/** The Constant MOUSE_DRAGGED. */
	@constant (
			value = IKeyword.MOUSE_DRAG,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user drags the mouse, i.e. when he moves it with a button pressed.")) String MOUSE_DRAGGED =
					IKeyword.MOUSE_DRAG;

	/** The Constant MOUSE_EVENTS. */
	Set<String> MOUSE_EVENTS = new HashSet<>(
			Arrays.asList(MOUSE_UP, MOUSE_DOWN, MOUSE_MOVED, MOUSE_ENTERED, MOUSE_EXITED, MOUSE_MENU, MOUSE_DRAGGED));

	/** The Constant SHIFT_MODIFIER. */
	@constant (
			value = "shift",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents the shift key modifier")) String SHIFT_MODIFIER = "shift";

	/** The Constant CONTROL_MODIFIER. */
	@constant (
			value = "ctrl",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents the control key modifier")) String CONTROL_MODIFIER = "ctrl";

	/** The Constant ALT_MODIFIER. */
	@constant (
			value = "alt",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents the alt key modifier")) String ALT_MODIFIER = "alt";

	/** The Constant CMD_MODIFIER. */
	@constant (
			value = "cmd",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents the command key modifier")) String CMD_MODIFIER = "cmd";
	/** The mouse press const. */
	@constant (
			value = "arrow_down",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the arrow down key. Defining an event layer with this event will deactivate the default navigation using arrow keys in the display. Use shift+arrow to override")) String ARROW_DOWN =
					"arrow_down";

	/** The mouse press const. */
	@constant (
			value = "arrow_up",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the arrow up key. Defining an event layer with this event will deactivate the default navigation using arrow keys in the display. Use shift+arrow to override")) String ARROW_UP =
					"arrow_up";

	/** The mouse press const. */
	@constant (
			value = "arrow_left",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the arrow left key. Defining an event layer with this event will deactivate the default navigation using arrow keys in the display. Use shift+arrow to override")) String ARROW_LEFT =
					"arrow_left";

	/** The mouse press const. */
	@constant (
			value = "arrow_right",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the arrow right key.Defining an event layer with this event will deactivate the default navigation using arrow keys in the display. Use shift+arrow to override")) String ARROW_RIGHT =
					"arrow_right";

	/** The mouse press const. */
	@constant (
			value = "escape",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the ESC key. Defining an event layer with this event will deactivate the fullscreen shortcut in the display. Use the toolbar/menu command to go fullscreen")) String KEY_ESC =
					"escape";

	/** The mouse press const. */
	@constant (
			value = "page_down",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the page down key")) String KEY_PAGE_DOWN =
					"page_down";

	/** The mouse press const. */
	@constant (
			value = "page_up",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the page up key")) String KEY_PAGE_UP =
					"page_up";

	/** The mouse press const. */
	@constant (
			value = "enter",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the enter/return key")) String KEY_ENTER =
					"enter";

	/** The mouse press const. */
	@constant (
			value = "tab",
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the tab key")) String KEY_TAB = "tab";

	/** The Constant KEYBOARD_EVENTS. */
	Set<String> KEYBOARD_EVENTS =
			new HashSet<>(Arrays.asList(KEY_ENTER, KEY_ESC, KEY_PAGE_DOWN, KEY_PAGE_UP, KEY_TAB, ARROW_DOWN, ARROW_LEFT,
					ARROW_RIGHT, ARROW_UP, ALT_MODIFIER, CONTROL_MODIFIER, CMD_MODIFIER, SHIFT_MODIFIER));

	/**
	 * Gets the events.
	 *
	 * @return the events
	 */
	Set<String> getEvents();

	/**
	 * Returns whether or not this delegate accepts the input source.
	 *
	 * @param scope
	 * @param source
	 *
	 * @return
	 */
	boolean acceptSource(IScope scope, Object source);

	/**
	 * Fills the list of maps with the initial values read from the source. Returns true if all the inits have been
	 * correctly filled
	 *
	 * @param scope
	 * @param source
	 * @return
	 */

	boolean createFrom(IScope scope, Object source, IStatement.Event statement);

}
