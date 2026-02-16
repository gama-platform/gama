/**
 * 
 */
package gama.api.types.graph;

/**
 * Placeholders for fake expressions used to build complex items (like edges and nodes). These expressions are never
 * evaluated, and return special graph objects (node, edge, nodes and edges)
 */

public interface GraphObjectToAdd {

	/**
	 * Gets the object.
	 *
	 * @return the object
	 */
	Object getObject();
}