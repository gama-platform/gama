/**
 *
 */
package gaml.compiler.gaml.preprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GamlResourceOffsetMap {

	/**
	 * The Class Shift.
	 */
	private static class Shift {

		/** The index in tagged text. */
		int indexInTaggedText;

		/** The delta. */
		int delta; // Positive if text was added, negative if removed

		/**
		 * Instantiates a new shift.
		 *
		 * @param index
		 *            the index
		 * @param delta
		 *            the delta
		 */
		Shift(final int index, final int delta) {
			this.indexInTaggedText = index;
			this.delta = delta;
		}
	}

	/** The shifts. */
	private final List<Shift> shifts = new ArrayList<>();

	/**
	 * Record insertion. Call this whenever you inject a synthetic tag
	 *
	 * @param indexInTaggedText
	 *            the index in tagged text
	 * @param lengthAdded
	 *            the length added
	 */
	public void recordInsertion(final int indexInTaggedText, final int lengthAdded) {
		shifts.add(new Shift(indexInTaggedText, lengthAdded));
	}

	/**
	 * Record deletion. Call this if you ever replace a long word with a short word
	 *
	 * @param indexInTaggedText
	 *            the index in tagged text
	 * @param lengthRemoved
	 *            the length removed
	 */

	public void recordDeletion(final int indexInTaggedText, final int lengthRemoved) {
		shifts.add(new Shift(indexInTaggedText, -lengthRemoved));
	}

	/**
	 * Translates an offset from the parsed AST back to the original text.
	 */
	public int getOriginalOffset(final int taggedOffset) {
		int originalOffset = taggedOffset;
		for (Shift shift : shifts) { if (taggedOffset > shift.indexInTaggedText) { originalOffset -= shift.delta; } }
		return originalOffset;
	}

	/**
	 * Clear.
	 */
	public void clear() {
		shifts.clear();
	}

}
