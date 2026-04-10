/*******************************************************************************************************
 *
 * Displays.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import gama.annotations.doc;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.collections.GamaNode;

/**
 * Provides GAML display layout operators for organizing multiple displays within the GAMA
 * modeling platform's user interface. These operators are used inside a GAML experiment's
 * {@code layout} facet to compose hierarchical arrangements of display panels.
 *
 * <p>Three layout operators are available:</p>
 * <ul>
 *   <li><strong>{@code horizontal}</strong> – places child elements side-by-side in a
 *       horizontal sash container, each element occupying space proportional to its
 *       integer weight.</li>
 *   <li><strong>{@code vertical}</strong> – stacks child elements top-to-bottom in a
 *       vertical sash container, using the same weight-based proportioning as
 *       {@code horizontal}.</li>
 *   <li><strong>{@code stack}</strong> – overlaps child displays in a tabbed container;
 *       only one display is visible at a time and weights are not taken into account.</li>
 * </ul>
 *
 * <p>Each layout element in a sash container is represented as a map entry where the
 * <em>key</em> is either a display index ({@code int}) or a nested layout
 * {@link GamaNode}, and the <em>value</em> is its relative integer weight within the
 * sash. All operators are annotated {@code @no_test} because they require an active
 * UI / display context that is not available during unit testing.</p>
 *
 * <p><strong>Example (GAML):</strong></p>
 * <pre>{@code
 * experiment "My Experiment" type: gui {
 *     // Horizontal split: display 0 takes 60%, display 1 takes 40%
 *     layout horizontal([0::60, 1::40]);
 * }
 * }</pre>
 *
 * @author GAMA Development Team
 * @see GamaNode
 */
public class Displays {

	/** The Constant HORIZONTAL. */
	public static final String HORIZONTAL = "horizontal";

	/** The Constant VERTICAL. */
	public static final String VERTICAL = "vertical";

	/** The Constant STACK. */
	public static final String STACK = "stack";

	/**
	 * Horizontal.
	 *
	 * @param scope
	 *            the scope
	 * @param nodes
	 *            the nodes
	 * @return the gama node
	 */
	@operator (
			value = HORIZONTAL,
			expected_content_type = IType.FLOAT,
			can_be_const = false)
	@doc (
			value = "Creates a horizontal layout node (a sash). Sashes can contain any number (> 1) of other elements: stacks, horizontal or vertical sashes, or display indices. Each element is represented by a pair in the map, where the key is the element and the value its weight within the sash",
			returns = "a {@code layout node} representing a horizontal sash container.",
			special_cases = {
				"A minimum of 2 child elements is required.",
				"If only 1 element is provided, behavior is undefined.",
				"Weight values determine relative proportions; negative or zero weights lead to undefined layout behavior." })
	@no_test
	public static GamaNode<String> horizontal(final IScope scope, final IMap<Object, Integer> nodes) {
		return buildSashFromMap(scope, HORIZONTAL, nodes);
	}

	/**
	 * Vertical.
	 *
	 * @param scope
	 *            the scope
	 * @param nodes
	 *            the nodes
	 * @return the gama node
	 */
	@operator (
			value = VERTICAL,
			expected_content_type = IType.FLOAT,
			can_be_const = false)
	@doc (
			value = "Creates a vertical layout node (a sash). Sashes can contain any number (> 1) of other elements: stacks, horizontal or vertical sashes, or display indices. Each element is represented by a pair in the map, where the key is the element and the value its weight within the sash",
			returns = "a {@code layout node} representing a vertical sash container.",
			special_cases = {
				"A minimum of 2 child elements is required.",
				"If only 1 element is provided, behavior is undefined.",
				"Weight values determine relative proportions; negative or zero weights lead to undefined layout behavior." })
	@no_test
	public static GamaNode<String> vertical(final IScope scope, final IMap<Object, Integer> nodes) {
		return buildSashFromMap(scope, VERTICAL, nodes);
	}

	/**
	 * Stack.
	 *
	 * @param scope
	 *            the scope
	 * @param nodes
	 *            the nodes
	 * @return the gama node
	 */
	@operator (
			value = STACK,
			can_be_const = false)
	@doc (
			value = "Creates a stack layout node. Stacks can only contain one or several indices of displays (without weight)",
			returns = "a {@code layout node} representing a stacked (tabbed) container.",
			special_cases = { "Each element in the stack is shown as a tab.",
					"An empty list produces an error; at least one display index must be provided." })
	@no_test
	public static GamaNode<String> stack(final IScope scope, final IList<Integer> nodes) {
		if (nodes == null) throw GamaRuntimeException.error("Nodes of a stack cannot be nil", scope);
		if (nodes.isEmpty())
			throw GamaRuntimeException.error("At least one display must be defined in the stack", scope);
		final GamaNode<String> node = new GamaNode<>(STACK);
		nodes.forEach(n -> node.addChild(String.valueOf(n)));
		return node;
	}

	/**
	 * Stack.
	 *
	 * @param scope
	 *            the scope
	 * @param nodes
	 *            the nodes
	 * @return the gama node
	 */
	@operator (
			value = STACK,
			can_be_const = false)
	@doc (
			value = "Creates a stack layout node. Accepts the same argument as `horizontal` or `vertical` (a map of display indices and weights) but the weights are not taken into account",
			returns = "a {@code layout node} representing a stacked (tabbed) container.",
			special_cases = { "Each element in the stack is shown as a tab.",
					"An empty map produces an error; at least one display entry must be provided." })
	@no_test
	public static GamaNode<String> stack(final IScope scope, final IMap<Object, Integer> nodes) {
		if (nodes == null) throw GamaRuntimeException.error("Nodes of a stack cannot be nil", scope);
		if (nodes.isEmpty())
			throw GamaRuntimeException.error("At least one display must be defined in the stack", scope);
		final GamaNode<String> node = new GamaNode<>(STACK);
		nodes.forEach((n, i) -> node.addChild(String.valueOf(n)));
		return node;
	}

	/**
	 * Builds the sash from map.
	 *
	 * @param scope
	 *            the scope
	 * @param orientation
	 *            the orientation
	 * @param nodes
	 *            the nodes
	 * @return the gama node
	 */
	@SuppressWarnings ("unchecked")
	private static GamaNode<String> buildSashFromMap(final IScope scope, final String orientation,
			final IMap<Object, Integer> nodes) {
		if (nodes == null)
			throw GamaRuntimeException.error("Nodes of a " + orientation + " layout cannot be nil", scope);
		if (nodes.size() < 2) throw GamaRuntimeException
				.error("At least two elements must be defined in this " + orientation + " layout", scope);
		final GamaNode<String> node = new GamaNode<>(orientation);
		nodes.forEach((key, value) -> {
			if (key instanceof GamaNode) {
				final GamaNode<String> n = (GamaNode<String>) key;
				n.setWeight(Cast.asInt(scope, value));
				n.attachTo(node);
			} else {
				final Integer index = Cast.asInt(scope, key);
				node.addChild(String.valueOf(index), Cast.asInt(scope, value));
			}
		});
		return node;
	}

}
