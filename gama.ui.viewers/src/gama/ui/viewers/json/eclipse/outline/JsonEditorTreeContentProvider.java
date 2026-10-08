/*******************************************************************************************************
 *
 * JsonEditorTreeContentProvider.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import gama.ui.viewers.json.outline.Item;
import gama.ui.viewers.json.outline.ItemType;
import gama.ui.viewers.json.script.JsonModel;

/**
 * The Class JsonEditorTreeContentProvider.
 */
public class JsonEditorTreeContentProvider implements ITreeContentProvider {

	/** The Constant JSON_MODEL_EMPTY_OR_INVALID. */
	private static final String JSON_MODEL_EMPTY_OR_INVALID = "Empty JSON or invalid";

	/** The Constant JSON_MODEL_DISABLED. */
	private static final String JSON_MODEL_DISABLED = "Outline disabled - must be enabled by menu or toolbar";

	/** The Constant RESULT_WHEN_EMPTY. */
	private static final Object[] RESULT_WHEN_EMPTY = { JSON_MODEL_EMPTY_OR_INVALID };

	/** The items. */
	private Object[] items;

	/** The monitor. */
	private final Object monitor = new Object();

	/** The outline enabled. */
	boolean outlineEnabled;

	/** The model. */
	private JsonModel model;

	/**
	 * Instantiates a new highspeed JSON editor tree content provider.
	 */
	JsonEditorTreeContentProvider() {
		items = RESULT_WHEN_EMPTY;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		synchronized (monitor) {
			if (inputElement != null && !(inputElement instanceof JsonModel))
				return new Object[] { "Unsupported input element:" + inputElement };
			if (items != null && items.length > 0) return items;
		}
		return RESULT_WHEN_EMPTY;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (!(parentElement instanceof Item item)) return null;
		return item.children.toArray();
	}

	@Override
	public Object getParent(final Object element) {
		if (!(element instanceof Item item)) return null;
		return item.parent;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (!(element instanceof Item item)) return false;
		return !item.children.isEmpty();

	}

	/**
	 * Rebuild tree.
	 *
	 * @param model
	 *            the model
	 */
	public void rebuildTree(final JsonModel model) {
		synchronized (monitor) {
			this.model = model;
			Item rootItem = model != null ? model.getRootItem() : null;
			Item item = null;

			if (outlineEnabled && rootItem != null) {
				item = rootItem;
			} else {
				item = new Item();
				if (!outlineEnabled) {
					item.name = JSON_MODEL_DISABLED;
				} else {
					item.name = JSON_MODEL_EMPTY_OR_INVALID;
				}
			}
			items = new Item[] { item };
		}
	}

	/**
	 * Creates the full path.
	 *
	 * @param lastItem
	 *            the last item
	 * @return the string
	 */
	public String createFullPath(final Item lastItem) {

		StringBuilder sb = new StringBuilder();
		List<Item> itemList = new ArrayList<>();
		Item render = lastItem;
		while (render != null) {
			if (!render.isRoot()) { itemList.add(render); }
			render = render.getParent();
		}
		Collections.reverse(itemList);

		int count = 0;
		for (Item item : itemList) {
			count++;
			if (ItemType.VIRTUAL_ARRAY_SEGMENT_NODE.equals(item.getType())) { continue; }
			if (count > 1) {
				boolean renderDot = true;// !item.getItemVariant().equals(ItemVariant.ARRAY);
				if (renderDot) { sb.append('.'); }
			}
			String name = item.getOriginName();
			if (item.isPartOfArray()) {
				sb.append('[');
				sb.append(item.getArrayIndex());
				sb.append(']');
			} else {
				sb.append(name);
			}

		}
		String path = sb.toString();
		path = path.replace("..", ".");
		path = path.replace(".[", "[");
		return path;

	}

	/**
	 * Try to find by offset.
	 *
	 * @param offset
	 *            the offset
	 * @return the item
	 */
	public Item tryToFindByOffset(final int offset) {
		synchronized (monitor) {
			if ((model == null) || model.hasErrors()) return null;
			Item item = null;
			for (int i = 0; i < 50 && item == null; i++) {
				int offset2 = offset + i;
				item = model.getItemOffsetMap().get(offset2);

			}
			return item;

		}
	}

}
