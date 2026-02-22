/*******************************************************************************************************
 *
 * IItemList.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import gama.api.types.color.IColor;

/**
 * Interface for managing lists of displayable items in GAMA views.
 * 
 * <p>This interface provides a contract for views that display collections of items
 * (such as simulations, parameters, or agents) with support for adding, removing,
 * pausing, resuming, and focusing items. It also handles visual presentation aspects
 * like colors, names, visibility, and context menus.</p>
 * 
 * <h2>Main Responsibilities:</h2>
 * <ul>
 *   <li>Manage item collection (add, remove)</li>
 *   <li>Control item state (pause, resume, focus)</li>
 *   <li>Provide item display attributes (name, color, visibility)</li>
 *   <li>Handle item context menus</li>
 *   <li>Support item value updates</li>
 * </ul>
 * 
 * <h2>Item Display Attributes:</h2>
 * <p>Items can have custom display properties:</p>
 * <ul>
 *   <li><strong>Name:</strong> Text label shown in the list</li>
 *   <li><strong>Color:</strong> Visual indicator for item status or type</li>
 *   <li><strong>Visibility:</strong> Whether the item should be shown or hidden</li>
 * </ul>
 * 
 * <h2>Special Characters:</h2>
 * <p>The interface defines special Unicode characters for visual indicators:</p>
 * <ul>
 *   <li>{@link #ERROR_CODE} - Indicates an error state (÷)</li>
 *   <li>{@link #INFO_CODE} - Indicates informational content (ø)</li>
 *   <li>{@link #WARNING_CODE} - Indicates a warning state (þ)</li>
 *   <li>{@link #SEPARATION_CODE} - Used for visual separation (ÿ)</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * IItemList<ISimulationAgent> simList = new SimulationList();
 * simList.addItem(simulation);
 * simList.pauseItem(simulation);
 * String displayName = simList.getItemDisplayName(simulation, "Simulation");
 * IColor color = simList.getItemDisplayColor(simulation);
 * }</pre>
 *
 * @param <T> the type of items managed by this list
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since 13 mai 2011
 */
public interface IItemList<T> {

	/**
	 * Special character code indicating an error state.
	 * 
	 * <p>This Unicode character (÷, U+00F7) can be used in item names or labels
	 * to visually indicate an error condition.</p>
	 */
	Character ERROR_CODE = '\u00F7';

	/**
	 * Special character code indicating informational content.
	 * 
	 * <p>This Unicode character (ø, U+00F8) can be used in item names or labels
	 * to visually indicate informational status.</p>
	 */
	Character INFO_CODE = '\u00F8';

	/**
	 * Special character code indicating a warning state.
	 * 
	 * <p>This Unicode character (þ, U+00FE) can be used in item names or labels
	 * to visually indicate a warning condition.</p>
	 */
	Character WARNING_CODE = '\u00FE';

	/**
	 * Special character code for visual separation.
	 * 
	 * <p>This Unicode character (ÿ, U+00FF) can be used to create visual
	 * separators between items or sections in lists.</p>
	 */
	Character SEPARATION_CODE = '\u00FF';

	/**
	 * Adds an item to this list.
	 * 
	 * <p>The default implementation returns false, indicating the item was not added.
	 * Concrete implementations should override this to actually add items.</p>
	 *
	 * @param obj the item to add
	 * @return true if the item was successfully added, false otherwise
	 */
	default boolean addItem(final T obj) {
		return false;
	}

	/**
	 * Removes an item from this list.
	 * 
	 * <p>The default implementation does nothing. Concrete implementations should
	 * override this to actually remove items and clean up any associated resources.</p>
	 *
	 * @param obj the item to remove
	 */
	default void removeItem(final T obj) {}

	/**
	 * Pauses an item in this list.
	 * 
	 * <p>For items that represent active processes (like simulations), this method
	 * pauses their execution. The default implementation does nothing.</p>
	 *
	 * @param obj the item to pause
	 */
	default void pauseItem(final T obj) {}

	/**
	 * Resumes a paused item in this list.
	 * 
	 * <p>For items that represent active processes (like simulations), this method
	 * resumes their execution after being paused. The default implementation does nothing.</p>
	 *
	 * @param obj the item to resume
	 */
	default void resumeItem(final T obj) {}

	/**
	 * Focuses on an item in this list.
	 * 
	 * <p>This method brings the item into view and/or gives it focus in the UI.
	 * The default implementation does nothing.</p>
	 *
	 * @param obj the item to focus on
	 */
	default void focusItem(final T obj) {}

	/**
	 * Gets all items in this list.
	 * 
	 * <p>The default implementation returns an empty list. Concrete implementations
	 * should override this to return the actual collection of items.</p>
	 *
	 * @return the list of items, or an empty list if no items are present
	 */
	default List<T> getItems() { return Collections.emptyList(); }

	/**
	 * Gets the display name for an item.
	 * 
	 * <p>This method allows customizing how an item's name appears in the UI.
	 * It can modify the name based on item state, add prefixes/suffixes, or
	 * apply other transformations. The default implementation returns the
	 * previous name unchanged.</p>
	 *
	 * @param obj the item whose name to get
	 * @param previousName the item's current/default name
	 * @return the display name to show in the UI
	 */
	default String getItemDisplayName(final T obj, final String previousName) {
		return previousName;
	}

	/**
	 * Updates the values displayed for all items.
	 * 
	 * <p>This method refreshes the UI to show current values. It can run
	 * synchronously or asynchronously and can optionally retrieve fresh
	 * values from the underlying data.</p>
	 * 
	 * <p>The default implementation does nothing.</p>
	 *
	 * @param synchronously if true, update runs on the calling thread; if false,
	 *                      may run asynchronously
	 * @param retrieveValues if true, retrieves updated values from data sources;
	 *                       if false, uses cached values
	 */
	default void updateItemValues(final boolean synchronously, final boolean retrieveValues) {}

	/**
	 * Checks if an item should be visible in the list.
	 * 
	 * <p>This method allows filtering items based on various criteria. Items
	 * that return false will be hidden from the UI. The default implementation
	 * returns true (all items visible).</p>
	 *
	 * @param obj the item to check
	 * @return true if the item should be visible, false if it should be hidden
	 */
	default boolean isItemVisible(final T obj) {
		return true;
	}

	/**
	 * Gets the display color for an item.
	 * 
	 * <p>This method allows items to be color-coded based on their state, type,
	 * or other attributes. The default implementation returns null (use default color).</p>
	 *
	 * @param data the item whose color to get
	 * @return the color to use for displaying the item, or null for default color
	 */
	default IColor getItemDisplayColor(final T data) {
		return null;
	}

	/**
	 * Handles the context menu for an item.
	 * 
	 * <p>This method is called when the user right-clicks or otherwise requests
	 * a context menu for an item. It returns a map of menu item labels to the
	 * actions that should execute when those items are selected.</p>
	 * 
	 * <p>The default implementation returns null (no context menu).</p>
	 *
	 * @param data the item whose context menu is requested
	 * @param x the x coordinate where the menu was requested
	 * @param y the y coordinate where the menu was requested
	 * @return a map of menu labels to actions, or null if no menu should be shown
	 */
	default Map<String, Runnable> handleMenu(final T data, final int x, final int y) {
		return null;
	}

}
