/*******************************************************************************************************
 *
 * MouseEventLayerDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.GamlAnnotations.constant;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.core.common.interfaces.IEventLayerDelegate;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;

/**
 * The Class MouseEventLayerDelegate.
 */
public class MouseEventLayerDelegate implements IEventLayerDelegate {

	/** The Constant MOUSE_DOWN. */
	@constant (
			value = IKeyword.MOUSE_DOWN,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses the mouse button")) final static public String MOUSE_DOWN =
					IKeyword.MOUSE_DOWN;

	/** The Constant MOUSE_UP. */
	@constant (
			value = IKeyword.MOUSE_UP,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user releases the mouse button")) final static public String MOUSE_UP =
					IKeyword.MOUSE_UP;

	/** The Constant MOUSE_MOVED. */
	@constant (
			value = IKeyword.MOUSE_MOVE,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user moves the mouse")) final static public String MOUSE_MOVED =
					IKeyword.MOUSE_MOVE;

	/** The Constant MOUSE_CLICKED. */
	@constant (
			value = IKeyword.MOUSE_CLICK,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user presses and releases the mouse button immediately")) final static public String MOUSE_CLICKED =
					IKeyword.MOUSE_CLICK;

	/** The Constant MOUSE_ENTERED. */
	@constant (
			value = IKeyword.MOUSE_ENTER,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the mouse enters the display")) final static public String MOUSE_ENTERED =
					IKeyword.MOUSE_ENTER;

	/** The Constant MOUSE_EXITED. */
	@constant (
			value = IKeyword.MOUSE_EXIT,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the mouse leaves the display")) final static public String MOUSE_EXITED =
					IKeyword.MOUSE_EXIT;

	/** The Constant MOUSE_MENU. */
	@constant (
			value = IKeyword.MOUSE_MENU,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user invokes the contextual menu")) final static public String MOUSE_MENU =
					IKeyword.MOUSE_MENU;

	/** The Constant MOUSE_DRAGGED. */
	@constant (
			value = IKeyword.MOUSE_DRAG,
			category = IOperatorCategory.USER_CONTROL,
			doc = @doc ("Represents an event emitted when the user drags the mouse, i.e. when he moves it with a button pressed.")) final static public String MOUSE_DRAGGED =
					IKeyword.MOUSE_DRAG;

	/** The Constant EVENTS. */
	public static final Set<String> EVENTS =
			new HashSet<>(Arrays.asList(MOUSE_UP, MOUSE_DOWN, MOUSE_MOVED, MOUSE_ENTERED, MOUSE_EXITED, MOUSE_MENU, MOUSE_DRAGGED));

	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		return Objects.equals(source, IKeyword.DEFAULT);
	}

	@Override
	public boolean createFrom(final IScope scope, final Object source, final EventLayerStatement statement) {
		return true;
	}

	@Override
	public Set<String> getEvents() { return EVENTS; }

}
