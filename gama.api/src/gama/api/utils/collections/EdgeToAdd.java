/**
 * 
 */
package gama.api.utils.collections;

/**
 * The Class EdgeToAdd.
 */
public class EdgeToAdd implements GraphObjectToAdd {

	/** The target. */
	public Object source, target;

	/** The object. */
	public Object object;

	/** The weight. */
	public Double weight;

	/**
	 * Instantiates a new edge to add.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param object
	 *            the object
	 * @param weight
	 *            the weight
	 */
	public EdgeToAdd(final Object source, final Object target, final Object object, final Double weight) {
		this.object = object;
		this.weight = weight;
		this.source = source;
		this.target = target;
	}

	/**
	 * Instantiates a new edge to add.
	 *
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param object
	 *            the object
	 * @param weight
	 *            the weight
	 */
	public EdgeToAdd(final Object source, final Object target, final Object object, final Integer weight) {
		this.object = object;
		this.weight = weight == null ? null : weight.doubleValue();
		this.source = source;
		this.target = target;
	}

	@Override
	public Object getObject() { return object; }

	/**
	 * @param cast
	 */
	public EdgeToAdd(final Object o) {
		this.object = o;
	}
}