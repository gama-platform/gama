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
 * Written by drogoul Modified on 13 mai 2011
 *
 * @todo Description
 *
 */
public interface IItemList<T> {

	/** The Constant ERROR_CODE. */
	Character ERROR_CODE = '\u00F7';

	/** The Constant INFO_CODE. */
	Character INFO_CODE = '\u00F8';

	/** The Constant WARNING_CODE. */
	Character WARNING_CODE = '\u00FE';

	/** The Constant SEPARATION_CODE. */
	Character SEPARATION_CODE = '\u00FF';

	/**
	 * Adds the item.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	default boolean addItem(final T obj) {
		return false;
	}

	/**
	 * Removes the item.
	 *
	 * @param obj
	 *            the obj
	 */
	default void removeItem(final T obj) {}

	/**
	 * Pause item.
	 *
	 * @param obj
	 *            the obj
	 */
	default void pauseItem(final T obj) {}

	/**
	 * Resume item.
	 *
	 * @param obj
	 *            the obj
	 */
	default void resumeItem(final T obj) {}

	/**
	 * Focus item.
	 *
	 * @param obj
	 *            the obj
	 */
	default void focusItem(final T obj) {}

	/**
	 * Gets the items.
	 *
	 * @return the items
	 */
	default List<T> getItems() { return Collections.emptyList(); }

	/**
	 * Gets the item display name.
	 *
	 * @param obj
	 *            the obj
	 * @param previousName
	 *            the previous name
	 * @return the item display name
	 */
	default String getItemDisplayName(final T obj, final String previousName) {
		return previousName;
	}

	/**
	 * Update item values.
	 */
	default void updateItemValues(final boolean synchronously, final boolean retrieveValues) {}

	/**
	 * Checks if is item visible.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if is item visible
	 */
	default boolean isItemVisible(final T obj) {
		return true;
	}

	/**
	 * Gets the item display color.
	 *
	 * @param data
	 *            the data
	 * @return the item display color
	 */
	default IColor getItemDisplayColor(final T data) {
		return null;
	}

	/**
	 * Handle menu.
	 *
	 * @param data
	 *            the data
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the map
	 */
	default Map<String, Runnable> handleMenu(final T data, final int x, final int y) {
		return null;
	}

}
