// This software is released into the Public Domain. See copying.txt for details.
package gama.dependencies.osmosis;

/**
 * Provides the definition of a class receiving tags.
 *
 * @author Brett Henderson
 */
public interface TagListener {

	/**
	 * Processes the tag.
	 *
	 * @param tag
	 *            The tag.
	 */
	void processTag(Tag tag);
}
