/*******************************************************************************************************
 *
 * Item.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class Item.
 */
public class Item {

	/** The item variant. */
	public ItemVariant itemVariant;

	/** The type. */
	ItemType type;

	/** The name. */
	public String name;

	/** The variant. */
	String variant;

	/** The offset. */
	int offset;

	/** The length. */
	int length;

	/** The end offset. */
	int endOffset;

	/** The parent. */
	public Item parent;

	/** The children. */
	public List<Item> children = Collections.emptyList(); // initial only empty list

	/** The content. */
	private String content;

	/** The origin name. */
	private String originName;

	/** The array index. */
	private int arrayIndex = -1;

	/**
	 * Gets the item variant.
	 *
	 * @return the item variant
	 */
	public ItemVariant getItemVariant() { return itemVariant; }

	/**
	 * Sets the item variant.
	 *
	 * @param itemVariant
	 *            the new item variant
	 */
	public void setItemVariant(final ItemVariant itemVariant) {
		this.itemVariant = itemVariant;

		this.variant = itemVariant != null ? itemVariant.getText() : null;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public ItemType getType() { return type; }

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	public Item getParent() { return parent; }

	/**
	 * @return item type , or <code>null</code>
	 */
	public ItemType getItemType() { return type; }

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() { return name; }

	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	public int getOffset() { return offset; }

	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public int getLength() { return length; }

	/**
	 * Gets the end offset.
	 *
	 * @return the end offset
	 */
	public int getEndOffset() { return endOffset; }

	/**
	 * Gets the variant.
	 *
	 * @return the variant
	 */
	public String getVariant() { return variant; }

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(final ItemType type) { this.type = type; }

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(final String name) { this.name = name; }

	/**
	 * Sets the variant.
	 *
	 * @param variant
	 *            the new variant
	 */
	public void setVariant(final String variant) { this.variant = variant; }

	/**
	 * Sets the offset.
	 *
	 * @param offset
	 *            the new offset
	 */
	public void setOffset(final int offset) { this.offset = offset; }

	/**
	 * Sets the length.
	 *
	 * @param length
	 *            the new length
	 */
	public void setLength(final int length) { this.length = length; }

	/**
	 * Sets the end offset.
	 *
	 * @param endOffset
	 *            the new end offset
	 */
	public void setEndOffset(final int endOffset) { this.endOffset = endOffset; }

	/**
	 * Sets the parent.
	 *
	 * @param parent
	 *            the new parent
	 */
	public void setParent(final Item parent) { this.parent = parent; }

	/**
	 * Sets the children.
	 *
	 * @param children
	 *            the new children
	 */
	public void setChildren(final List<Item> children) { this.children = children; }

	/**
	 * Gets the children.
	 *
	 * @return the children
	 */
	public List<Item> getChildren() { return children; }

	/**
	 * Adds given item as a child and marks this as parent of child
	 *
	 * @param item
	 */
	public void addChild(final Item item) {
		if (!(children instanceof ArrayList)) { children = new ArrayList<>(); }
		item.setParent(this);
		children.add(item);
	}

	/**
	 * Sets the content.
	 *
	 * @param content
	 *            the new content
	 */
	public void setContent(final String content) { this.content = content; }

	/**
	 * Gets the content.
	 *
	 * @return the content
	 */
	public String getContent() { return content; }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Item:");
		sb.append("name:");
		sb.append(name);
		sb.append(",type:");
		sb.append(type);
		sb.append(",variant:");
		sb.append(variant);
		sb.append(",offset:");
		sb.append(offset);
		sb.append(",length:");
		sb.append(length);
		sb.append(",endOffset:");
		sb.append(endOffset);
		return sb.toString();
	}

	/**
	 * Builds the search string.
	 *
	 * @return the string
	 */
	public String buildSearchString() {
		return name;
	}

	/**
	 * Checks for children.
	 *
	 * @return true, if successful
	 */
	public boolean hasChildren() {
		return children.size() > 0;
	}

	/**
	 * Checks if is root.
	 *
	 * @return true, if is root
	 */
	public boolean isRoot() { return false; }

	/**
	 * Sets the origin name.
	 *
	 * @param name
	 *            the new origin name
	 */
	public void setOriginName(final String name) { this.originName = name; }

	/**
	 * Gets the origin name.
	 *
	 * @return the origin name
	 */
	public String getOriginName() { return originName; }

	/**
	 * Sets the array index.
	 *
	 * @param index
	 *            the new array index
	 */
	public void setArrayIndex(final int index) { this.arrayIndex = index; }

	/**
	 * Gets the array index.
	 *
	 * @return the array index
	 */
	public int getArrayIndex() { return arrayIndex; }

	/**
	 * Checks if is part of array.
	 *
	 * @return true, if is part of array
	 */
	public boolean isPartOfArray() { return arrayIndex != -1; }

}
