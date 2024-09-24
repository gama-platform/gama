/*******************************************************************************************************
 *
 * GamlAccessEntry.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.ui.shared.access;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import gama.gaml.compilation.GamlIdiomsProvider;
import gama.gaml.interfaces.IGamlDescription;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class GamlAccessEntry.
 */
public class GamlAccessEntry {

	/** The first in category. */
	boolean firstInCategory;

	/** The last in category. */
	boolean lastInCategory;

	/** The element. */
	IGamlDescription element;

	/** The provider. */
	GamlIdiomsProvider<?> provider;

	/** The element match regions. */
	int[][] elementMatchRegions;

	/** The provider match regions. */
	int[][] providerMatchRegions;

	/**
	 * Provides a rough indicator of how good of a match this entry was to its filter. Lower values indicate better
	 * match quality. A value of 0 indicates the filter string was an exact match to the label or that there is no
	 * filter being applied.
	 */
	private final int matchQuality;

	static TextStyle boldStyle, categoryStyle;

	static TextStyle getBoldStyle() {
		if (boldStyle == null) {
			boldStyle = new TextStyle(WorkbenchHelper.getDisplay().getSystemFont(), null,
					ThemeHelper.isDark() ? IGamaColors.BLUE.color() : IGamaColors.TOOLTIP.color());
		}
		return boldStyle;
	}

	static TextStyle getCategoryStyle() {
		if (categoryStyle == null) {
			categoryStyle = new TextStyle(WorkbenchHelper.getDisplay().getSystemFont(), null,
					ThemeHelper.isDark() ? IGamaColors.BLUE.color() : IGamaColors.TOOLTIP.color());
		}
		return categoryStyle;
	}

	/**
	 * Indicates the filter string was a perfect match to the label or there is no filter applied
	 *
	 * @see #getMatchQuality()
	 */
	public static final int MATCH_PERFECT = 0;

	/**
	 * Indicates this entry is very relevant for the filter string. Recommended value for when the filter was found at
	 * the start of the element's label or a complete case sensitive camel case match.
	 *
	 * @see #getMatchQuality()
	 */
	public static final int MATCH_EXCELLENT = 5;

	/**
	 * Indicates this entry is relevant for the filter string. Recommended value for when the complete filter was found
	 * somewhere inside the element's label or provider.
	 *
	 * @see #getMatchQuality()
	 */
	public static final int MATCH_GOOD = 10;

	/**
	 * Indicates only part of the filter string matches to the element's label.
	 *
	 * @see #getMatchQuality()
	 */
	public static final int MATCH_PARTIAL = 15;

	/**
	 * Creates a new quick access entry from the given element and provider. If no filter was used to match this entry
	 * the element/provider match regions may be empty and the match quality should be {@link #MATCH_PERFECT}
	 *
	 * @param element
	 *            the element this entry will represent
	 * @param provider
	 *            the provider that owns this entry
	 * @param elementMatchRegions
	 *            list of text regions the filter string matched in the element label, possibly empty
	 * @param providerMatchRegions
	 *            list of text regions the filter string matches in the provider label, possible empty
	 * @param matchQuality
	 *            a rough indication of how closely the filter matched, lower values indicate a better match. It is
	 *            recommended to use the constants available on this class: {@link #MATCH_PERFECT},
	 *            {@link #MATCH_EXCELLENT}, {@link #MATCH_GOOD}, {@link #MATCH_PARTIAL}
	 */
	GamlAccessEntry(final IGamlDescription element, final GamlIdiomsProvider<?> provider,
			final int[][] elementMatchRegions, final int[][] providerMatchRegions, final int matchQuality) {
		this.element = element;
		this.provider = provider;
		this.elementMatchRegions = elementMatchRegions;
		this.providerMatchRegions = providerMatchRegions;
		this.matchQuality = matchQuality;
	}

	// Image getImage(final ResourceManager resourceManager) {
	// final Image image = findOrCreateImage(provider.getImageDescriptor(), resourceManager);
	// // if (image == null) {
	// // image = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJ_ELEMENT);
	// // }
	// return image;
	// }

	// private Image findOrCreateImage(final ImageDescriptor imageDescriptor, final ResourceManager resourceManager) {
	// if (imageDescriptor == null) { return null; }
	// Image image = (Image) resourceManager.find(imageDescriptor);
	// if (image == null) {
	// try {
	// image = resourceManager.createImage(imageDescriptor);
	// } catch (final DeviceResourceException e) {
	// // WorkbenchPlugin.log(e);
	// }
	// }
	// return image;
	// }

	/**
	 * Gets the search category.
	 *
	 * @return the search category
	 */
	public String getSearchCategory() { return provider.getSearchCategory(); }

	/**
	 * Measure.
	 *
	 * @param event
	 *            the event
	 * @param textLayout
	 *            the text layout
	 */
	public void measure(final Event event, final TextLayout textLayout) {
		// final Table table = ((TableItem) event.item).getParent();

		event.width = 0;
		switch (event.index) {
			case 0:
				// textLayout.setFont(GamaFonts.categoryHelpFont);
				if (firstInCategory || providerMatchRegions.length > 0) {
					textLayout.setText(provider.name);
					for (final int[] matchRegion : providerMatchRegions) {
						textLayout.setStyle(getCategoryStyle(), matchRegion[0], matchRegion[1]);
					}
				} else {
					textLayout.setText(""); //$NON-NLS-1$
				}
				break;
			case 1:
				textLayout.setText(element.getTitle());
				for (final int[] matchRegion : elementMatchRegions) {
					textLayout.setStyle(getBoldStyle(), matchRegion[0], matchRegion[1]);
				}

				break;
		}
		final Rectangle rect = textLayout.getBounds();
		event.width += rect.width + 4;
		event.height = Math.max(event.height, rect.height + 2);
	}

	/**
	 * Paint.
	 *
	 * @param event
	 *            the event
	 * @param textLayout
	 *            the text layout
	 */
	public void paint(final Event event, final TextLayout textLayout) {
		final Table table = ((TableItem) event.item).getParent();

		switch (event.index) {
			case 0:
				textLayout.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
				if (firstInCategory || providerMatchRegions.length > 0) {
					// textLayout.setFont(GamaFonts.categoryHelpFont);

					textLayout.setText(provider.name);

					for (final int[] matchRegion : providerMatchRegions) {
						textLayout.setStyle(getCategoryStyle(), matchRegion[0], matchRegion[1]);
					}

					if (providerMatchRegions.length > 0 && !firstInCategory) {
						event.gc.setForeground(IGamaColors.GRAY_LABEL.color());
					}
					final Rectangle availableBounds = ((TableItem) event.item).getTextBounds(event.index);
					final Rectangle requiredBounds = textLayout.getBounds();
					textLayout.draw(event.gc, availableBounds.x + 1,
							availableBounds.y + (availableBounds.height - requiredBounds.height) / 2);
				}
				break;
			case 1:
				textLayout.setFont(table.getFont());
				final String label = element.getTitle();
				textLayout.setText(label);
				for (final int[] matchRegion : elementMatchRegions) {
					textLayout.setStyle(getBoldStyle(), matchRegion[0], matchRegion[1]);
				}

				final Rectangle availableBounds = ((TableItem) event.item).getTextBounds(event.index);
				final Rectangle requiredBounds = textLayout.getBounds();
				textLayout.draw(event.gc, availableBounds.x + 1 /* + image.getBounds().width */,
						availableBounds.y + (availableBounds.height - requiredBounds.height) / 2);
				break;
		}
		if (lastInCategory) {
			event.gc.setForeground(IGamaColors.GRAY_LABEL.color());
			final Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
			event.gc.drawLine(Math.max(0, bounds.x - 1), bounds.y + bounds.height - 1, bounds.x + bounds.width,
					bounds.y + bounds.height - 1);
		}
	}

	/**
	 * @param event
	 */
	public void erase(final Event event) {
		// We are only custom drawing the foreground.
		event.detail &= ~SWT.FOREGROUND;
	}

	/**
	 * Provides a rough indicator of how good of a match this entry was to its filter. Lower values indicate better
	 * match quality. A value of {@link #MATCH_PERFECT} indicates the filter string was an exact match to the label or
	 * that there is no filter being applied.
	 *
	 * @return Returns the match quality
	 */
	public int getMatchQuality() { return matchQuality; }

}