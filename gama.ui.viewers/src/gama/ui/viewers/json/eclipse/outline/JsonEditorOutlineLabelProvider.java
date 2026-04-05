/*******************************************************************************************************
 *
 * JsonEditorOutlineLabelProvider.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.outline;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.viewers.json.SimpleStringUtils;
import gama.ui.viewers.json.eclipse.ColorManager;
import gama.ui.viewers.json.eclipse.JsonEditorActivator;
import gama.ui.viewers.json.eclipse.JsonEditorColorConstants;
import gama.ui.viewers.json.outline.Item;
import gama.ui.viewers.json.outline.ItemType;
import gama.ui.viewers.json.outline.ItemVariant;

/**
 * The Class JsonEditorOutlineLabelProvider.
 */
public class JsonEditorOutlineLabelProvider extends BaseLabelProvider implements IStyledLabelProvider, IColorProvider {

	/** The Constant JSON_OBJECT. */
	private static final String JSON_OBJECT = "gaml/_reflex";

	/** The Constant ICON_JSON_ARRAY. */
	private static final String ICON_JSON_ARRAY = "gaml/_list";

	/** The Constant ICON_JSON_VALUE. */
	private static final String ICON_JSON_VALUE = "gaml/_string";

	/** The Constant ICON_JSONNODE. */
	private static final String ICON_JSONNODE = "gaml/_reflex";

	/** The Constant ICON_ERROR. */
	private static final String ICON_ERROR = "gaml/_abort";

	/** The Constant ICON_INFO. */
	private static final String ICON_INFO = "gaml/_file";

	/** The outline item type styler. */
	private final Styler outlineItemTypeStyler = new Styler() {

		@Override
		public void applyStyles(final TextStyle textStyle) {
			textStyle.foreground = getColorManager().getColor(JsonEditorColorConstants.OUTLINE_ITEM__TYPE);
		}
	};

	@Override
	public Color getBackground(final Object element) {
		return null;
	}

	@Override
	public Color getForeground(final Object element) {
		return null;
	}

	@Override
	public Image getImage(final Object element) {
		if (element == null) return null;
		if (element instanceof Item item) {
			ItemType type = item.getItemType();

			if (type == null) return null;

			ItemVariant itemVariant = item.getItemVariant();
			switch (type) {
				case JSON_NODE:
					switch (itemVariant) {
						case VALUE:
							return getOutlineImage(ICON_JSON_VALUE);
						case ARRAY:
							return getOutlineImage(ICON_JSON_ARRAY);
						case OBJECT:
							return getOutlineImage(JSON_OBJECT);
						case null:
						default:
							break;
					}
					return getOutlineImage(ICON_JSONNODE);
				case META_ERROR:
					return getOutlineImage(ICON_ERROR);
				case META_INFO:
					return getOutlineImage(ICON_INFO);
				default:
					return null;
			}
		}
		return null;
	}

	@Override
	public StyledString getStyledText(final Object element) {
		StyledString styled = new StyledString();
		if (element == null) return styled.append("null");
		if (!(element instanceof Item item)) return styled.append(element.toString());
		String name = item.getName();
		if (name != null) {
			styled.append(name);// +" { ... }");
			styled.append(" ");
		}
		ItemType itemType = item.getItemType();
		if (itemType == ItemType.JSON_NODE) {
			if (item.itemVariant == ItemVariant.VALUE) {
				StyledString typeString = new StyledString(SimpleStringUtils.shortString(item.getContent(), 20) + " ",
						outlineItemTypeStyler);
				styled.append(typeString);
			}
		} else if (itemType == ItemType.META_DEBUG) {
			StyledString typeString = new StyledString(item.getOffset() + ": ", outlineItemTypeStyler);
			styled.append(typeString);
		}

		return styled;
	}

	/**
	 * Gets the color manager.
	 *
	 * @return the color manager
	 */
	public ColorManager getColorManager() {
		JsonEditorActivator editorActivator = JsonEditorActivator.getDefault();
		if (editorActivator == null) return ColorManager.getStandalone();
		return editorActivator.getColorManager();
	}

	/**
	 * Gets the outline image.
	 *
	 * @param name
	 *            the name
	 * @return the outline image
	 */
	private Image getOutlineImage(final String name) {
		return GamaIcon.named(name).image();
	}

}
