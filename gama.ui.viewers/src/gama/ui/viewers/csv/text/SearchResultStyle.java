/*******************************************************************************************************
 *
 * SearchResultStyle.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.csv.text;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fhenri
 *
 */
public class SearchResultStyle {

	/**
	 * @param searchTerm
	 * @param content
	 * @return
	 */
	public static int[] getSearchTermOccurrences(final String searchTerm, final String content) {
		List<Integer> ranges = new ArrayList<>(); // reset the ranges-array

		if ("".equals(searchTerm)) return new int[] {};

		// determine all occurrences of the searchText and write the beginning
		// and length of each occurrence into an array
		for (int i = 0; i < content.length(); i++) {
			if (i + searchTerm.length() <= content.length()
					&& content.substring(i, i + searchTerm.length()).equalsIgnoreCase(searchTerm)) {
				// ranges format: n->start of the range, n+1->length of the
				// range
				ranges.add(i);
				ranges.add(searchTerm.length());
			}
		}
		// convert the list into an int[] and make sure that overlapping
		// search term occurrences are are merged
		final int[] intRanges = new int[ranges.size()];
		int arrayIndexCounter = 0;
		for (int listIndexCounter = 0; listIndexCounter < ranges.size(); listIndexCounter++) {
			if (listIndexCounter % 2 == 0 && searchTerm.length() > 1 && listIndexCounter != 0
					&& ranges.get(listIndexCounter - 2) + ranges.get(listIndexCounter - 1) >= ranges
							.get(listIndexCounter)) {
				intRanges[arrayIndexCounter - 1] = 0 - ranges.get(listIndexCounter - 2) + ranges.get(listIndexCounter)
						+ ranges.get(++listIndexCounter);
			} else {
				intRanges[arrayIndexCounter++] = ranges.get(listIndexCounter);
			}
		}
		// if there have been any overlappings we need to reduce the size of
		// the array to avoid conflicts in the setStyleRanges method
		final int[] intRangesCorrectSize = new int[arrayIndexCounter];
		System.arraycopy(intRanges, 0, intRangesCorrectSize, 0, arrayIndexCounter);

		return intRangesCorrectSize;
	}
}
