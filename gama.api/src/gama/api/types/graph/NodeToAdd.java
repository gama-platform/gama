/**
 * 
 */
package gama.api.types.graph;

/**
 * The Class NodeToAdd.
 */
public class NodeToAdd implements GraphObjectToAdd {

	/** The object. */
	public Object object;

	/** The weight. */
	public Double weight;

	/**
	 * Instantiates a new node to add.
	 *
	 * @param object
	 *            the object
	 * @param weight
	 *            the weight
	 */
	public NodeToAdd(final Object object, final Double weight) {
		this.object = object;
		this.weight = weight;
	}

	/**
	 * @param cast
	 */
	public NodeToAdd(final Object o) {
		object = o;
	}

	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	@Override
	public Object getObject() { return object; }

}