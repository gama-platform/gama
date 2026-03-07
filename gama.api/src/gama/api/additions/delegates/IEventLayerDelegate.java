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
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IOperatorCategory;
import gama.api.gaml.statements.IStatement;
import gama.api.runtime.scope.IScope;
import gama.api.ui.layers.ILayerStatement;

/**
 * Delegate interface for extending GAMA's event layer system with custom event sources.
 *
 * <p>
 * This interface allows plugins to provide custom event sources beyond the standard keyboard and mouse events. Event
 * layer delegates can handle events from external devices, network sources, or other input mechanisms, and trigger GAML
 * code in response.
 * </p>
 *
 * <h2>Event Layer Mechanism</h2>
 * <p>
 * In GAMA, event layers are special display layers that respond to user or system events. When an event layer is
 * defined in a GAML model, the platform:
 * </p>
 * <ol>
 * <li>Queries registered delegates via {@link #getEvents()} to find which events they handle</li>
 * <li>When an event occurs, calls {@link #acceptSource(IScope, Object)} to identify the appropriate delegate</li>
 * <li>Calls {@link #createFrom(IScope, Object, IStatement.Event)} to process the event</li>
 * </ol>
 *
 * <h2>Standard Event Constants</h2>
 * <p>
 * This interface defines standard mouse and keyboard event constants that are used throughout the GAMA platform. Custom
 * delegates can define their own event constants for specialized input types.
 * </p>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * {
 * 	&#64;code
 * 	public class GamepadEventDelegate implements IEventLayerDelegate {
 * 		private static final Set<String> GAMEPAD_EVENTS = Set.of("gamepad_button", "gamepad_axis");
 * 
 * 		&#64;Override
 * 		public Set<String> getEvents() { return GAMEPAD_EVENTS; }
 * 
 * 		&#64;Override
 * 		public boolean acceptSource(IScope scope, Object source) {
 * 			return source instanceof GamepadEvent;
 * 		}
 * 
 * 		@Override
 * 		public boolean createFrom(IScope scope, Object source, IStatement.Event statement) {
 * 			GamepadEvent event = (GamepadEvent) source;
 * 			// Process the event and execute the statement
 * 			return statement.executeOn(scope);
 * 		}
 * 	}
 * }
 * </pre>
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 * @see ICreateDelegate
 * @see IDrawDelegate
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
	 * Returns the set of event names that this delegate can handle.
	 *
	 * <p>
	 * This method is called during event layer initialization to determine which events this delegate is responsible
	 * for. The returned set should contain string identifiers for all events that this delegate can process.
	 * </p>
	 *
	 * <p>
	 * Event names should be unique across all delegates to avoid conflicts. Standard mouse and keyboard events are
	 * predefined as constants in this interface.
	 * </p>
	 *
	 * @return the set of event name strings handled by this delegate
	 */
	Set<String> getEvents();

	/**
	 * Determines whether this delegate can handle events from the specified source.
	 *
	 * <p>
	 * This method is called when an event occurs to find the appropriate delegate to process it. The first registered
	 * delegate that returns true will be selected.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param source
	 *            the event source object (e.g., MouseEvent, KeyEvent, or custom event type)
	 * @return true if this delegate can handle events from this source, false otherwise
	 */
	boolean acceptSource(IScope scope, Object source);

	/**
	 * Processes an event and executes the associated event layer statement.
	 *
	 * <p>
	 * This method is called when an event handled by this delegate occurs. It should extract relevant information from
	 * the source, potentially update the scope with event-specific variables, and trigger the execution of the event
	 * statement.
	 * </p>
	 *
	 * <p>
	 * Common tasks include:
	 * </p>
	 * <ul>
	 * <li>Extracting event data (coordinates, key codes, etc.)</li>
	 * <li>Setting temporary variables in the scope for access in GAML code</li>
	 * <li>Executing the event statement's body</li>
	 * </ul>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param source
	 *            the event source object containing event details
	 * @param statement
	 *            the event statement to execute in response to this event
	 * @return true if the event was successfully processed, false otherwise
	 */
	boolean createFrom(IScope scope, Object source, ILayerStatement.Event statement);

}
