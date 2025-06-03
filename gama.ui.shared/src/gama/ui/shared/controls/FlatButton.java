/*******************************************************************************************************
 *
 * FlatButton.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.views.toolbar.Selector;

/**
 * The Class FlatButton.
 */
public class FlatButton extends Canvas implements PaintListener, Listener {

	static {
		DEBUG.OFF();
	}

	/** The menu image. */
	static final Image MENU_IMAGE = GamaIcon.named(IGamaIcons.SMALL_DROPDOWN).image();

	/** The Constant MENU_BOUNDS. */
	static final Rectangle MENU_BOUNDS = MENU_IMAGE.getBounds();

	/** The Constant INNER_MARGIN. */
	private static final int INNER_MARGIN = 4, IMAGE_PADDING = 4, MINIMAL_HEIGHT = MENU_BOUNDS.height + IMAGE_PADDING;

	/** The Constant ELLIPSIS. */
	private static final char ELLIPSIS = '…';

	/** The Constant NULL_EXTENT. */
	private static final Point NULL_EXTENT = new Point(0, 0);

	/** The selection listener. */
	private Selector selectionListener;

	/** The image. */
	private Image image;

	/** The text. */
	private String text;

	/** The color code. */
	private RGB colorCode;

	/** Dimensions. */
	private int preferredHeight = SWT.DEFAULT, preferredWidth = SWT.DEFAULT, forcedWidth = SWT.DEFAULT,
			minimalHeight = MINIMAL_HEIGHT;

	/** States */
	private boolean enabled = true, hovered = false, down = false, border = false, menu = false, computeSize = true;

	/**
	 * Creates the.
	 *
	 * @param comp
	 *            the comp
	 * @param style
	 *            the style
	 * @return the flat button
	 */
	public static FlatButton create(final Composite comp, final int style) {
		return new FlatButton(comp, style);
	}

	/**
	 * Label.
	 *
	 * @param comp
	 *            the comp
	 * @param color
	 *            the color
	 * @param text
	 *            the text
	 * @param forcedWidth
	 *            the forced width
	 * @return the flat button
	 */
	public static FlatButton label(final Composite comp, final GamaUIColor color, final String text,
			final int forcedWidth) {
		return create(comp, SWT.None).withFixedWidth(forcedWidth).setText(text).setColor(color);
	}

	/**
	 * Button.
	 *
	 * @param comp
	 *            the comp
	 * @param color
	 *            the color
	 * @param text
	 *            the text
	 * @return the flat button
	 */
	public static FlatButton button(final Composite comp, final GamaUIColor color, final String text) {
		return create(comp, SWT.None).setText(text).setColor(color);
	}

	/**
	 * Button.
	 *
	 * @param comp
	 *            the comp
	 * @param color
	 *            the color
	 * @param text
	 *            the text
	 * @param image
	 *            the image
	 * @return the flat button
	 */
	public static FlatButton button(final Composite comp, final GamaUIColor color, final String text,
			final Image image) {
		return button(comp, color, text).setImage(image);
	}

	/**
	 * Menu.
	 *
	 * @param comp
	 *            the comp
	 * @param color
	 *            the color
	 * @param text
	 *            the text
	 * @return the flat button
	 */
	public static FlatButton menu(final Composite comp, final GamaUIColor color, final String text) {
		return button(comp, color, text).addMenuSign();
	}

	/**
	 * Instantiates a new flat button.
	 *
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 */
	private FlatButton(final Composite parent, final int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		addPaintListener(this);
		addListener(SWT.MouseDown, this);
		addListener(SWT.MouseExit, this);
		addListener(SWT.MouseEnter, this);
		addListener(SWT.MouseHover, this);
		addListener(SWT.MouseUp, this);
		addListener(SWT.MouseMove, this);
	}

	@Override
	public void handleEvent(final Event e) {
		if (!enabled) { return; }
		switch (e.type) {
			case SWT.MouseExit:
				doHover(false);
				break;
			case SWT.MouseMove:
				break;
			case SWT.MouseEnter:
			case SWT.MouseHover:
				doHover(true);
				e.doit = true;
				break;
			case SWT.MouseUp:
				if (e.button == 1 && getClientArea().contains(e.x, e.y)) { doButtonUp(e); }
				break;
			case SWT.MouseDown:
				if (e.button == 1 && getClientArea().contains(e.x, e.y)) { doButtonDown(); }
				break;
			default:
				;
		}
	}

	/**
	 * SelectionListeners are notified when the button is clicked
	 *
	 * @param listener
	 */
	public void setSelectionListener(final Selector listener) { selectionListener = listener; }

	/**
	 * Do button down.
	 */
	private void doButtonDown() {
		if (!enabled) { return; }
		down = true;
		if (!isDisposed()) { redraw(); }
	}

	/**
	 * Do button up.
	 */
	private void doButtonUp(final Event e) {
		if (selectionListener != null) { selectionListener.widgetSelected(new SelectionEvent(e)); }
		down = false;
		if (!isDisposed()) { redraw(); }
	}

	/**
	 * Do hover.
	 *
	 * @param hover
	 *            the hover
	 */
	private void doHover(final boolean hover) {
		hovered = hover;
		if (!hover) { down = false; }
		if (!isDisposed()) { redraw(); }
	}

	@Override
	public void paintControl(final PaintEvent e) {
		final GC gc = e.gc;
		gc.setAntialias(SWT.ON);
		Font f = getFont();
		gc.setFont(f);

		if (computeSize) { computePreferredSize(gc); }
		// DEBUG.OUT("Painting. Preferred size of '" + getText() + "' is " + preferredWidth + " x " + preferredHeight);

		float v_inset;
		if (preferredHeight < getBounds().height) {
			v_inset = (getBounds().height - preferredHeight) / 2f;
		} else {
			v_inset = 0;
		}
		final Rectangle rect = new Rectangle(0, Math.round(v_inset), preferredWidth, preferredHeight);
		setBackground(getParent().getBackground());
		GamaUIColor color = GamaColors.get(colorCode);
		Color background = color == null ? getParent().getBackground() : hovered ? color.lighter() : color.color();
		final Color foreground = GamaColors.getTextColorForBackground(background).color();

		gc.setBackground(background);
		if (down) {
			gc.fillRoundRectangle(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2, 8, 8);
			if (border) {
				final Color borderColor = ThemeHelper.isDark() ? GamaColors.get(background).lighter()
						: GamaColors.get(background).darker();
				gc.setForeground(borderColor);
				gc.drawRoundRectangle(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2, 8, 8);
			}
		} else {
			gc.fillRoundRectangle(rect.x, rect.y, rect.width, rect.height, 8, 8);
			if (border) {
				final Color borderColor = ThemeHelper.isDark() ? GamaColors.get(background).lighter()
						: GamaColors.get(background).darker();
				gc.setForeground(borderColor);
				gc.drawRoundRectangle(rect.x, rect.y, rect.width, rect.height, 8, 8);
			}
		}
		gc.setForeground(foreground);

		float x = INNER_MARGIN;
		final Image im = getImage();
		float y_text = -1;
		final String contents = newText(gc);
		if (contents != null) { y_text += (getBounds().height - gc.textExtent(contents).y) / 2f; }

		if (im != null) {
			float y_image = (getBounds().height - im.getBounds().height) / 2f;
			x = drawImage(im, gc, Math.round(x), Math.round(y_image));
		}
		gc.drawString(contents, Math.round(x), Math.round(y_text));
		if (menu) {
			float y_image = (getBounds().height - MENU_BOUNDS.height) / 2f;
			x = INNER_MARGIN;
			x = rect.width - x - MENU_BOUNDS.width;
			drawImage(MENU_IMAGE, gc, Math.round(x), Math.round(y_image));
		}
	}

	/**
	 * Draw image.
	 *
	 * @param gc
	 *            the gc
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the int
	 */
	private int drawImage(final Image image, final GC gc, final int x, final int y) {
		if (image == null) { return x; }
		gc.drawImage(image, x, y);
		return x + image.getBounds().width + IMAGE_PADDING;
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		// DEBUG.OUT("Computing size of '" + text + "' called with " + wHint + " x " + hHint);
		// DEBUG.STACK();
		if (computeSize) { computePreferredSize(); }
		int width = wHint != SWT.DEFAULT ? wHint : preferredWidth;
		int height = hHint != SWT.DEFAULT ? hHint : preferredHeight;
		return super.computeSize(width, height, changed);

		// return new Point(width, height);
	}

	/**
	 * New text. Does not dispose of the gc !
	 *
	 * @return the string
	 */
	public String newText(final GC gc) {
		if (text == null) { return null; }
		final float width = computeWidthAvailableForText();
		final float textWidth = computeExtentOfText(gc, text).x;
		// DEBUG.OUT("Text size. Size of '" + text + "' is " + textWidth + " ; available size is " + width);
		if (textWidth > width) {
			for (int i = text.length() - 1; i > 0; i--) {
				text = text.substring(0, i) + ELLIPSIS;
				if (computeExtentOfText(gc, text).x < width) { break; }
			}
		}
		return text;
	}

	/**
	 * Compute width available for text.
	 *
	 * @return the int
	 */
	private int computeWidthAvailableForText() {
		final Image im = getImage();
		int r = 0;
		if (im != null || menu) {
			if (im != null) { r += im.getBounds().width + IMAGE_PADDING * 2; }
			if (menu) { r += MENU_BOUNDS.x + IMAGE_PADDING * 2; }
		}
		return preferredWidth - r;
	}

	/**
	 * This is an image that will be displayed to the side of the text inside the button (if any). By default the image
	 * will be to the left of the text; however, setImageStyle can be used to specify that it's either to the right or
	 * left. If there is no text, the image will be centered inside the button.
	 *
	 * @param image
	 */
	public FlatButton setImage(final Image image) {
		if (image == null && this.image == null) {
			computeSize = true;
			redraw();
			return this;
		}
		if (this.image == image) { return this; }
		this.image = image;
		computeSize = true;
		redraw();
		return this;
	}

	/**
	 * Sets the image without recomputing size.
	 *
	 * @param image
	 *            the image
	 * @return the flat button
	 */
	public FlatButton setImageWithoutRecomputingSize(final Image image) {
		if (image == null && this.image == null) {
			redraw();
			return this;
		}
		if (this.image == image) { return this; }
		this.image = image;
		redraw();
		return this;
	}

	/**
	 * Show menu sign.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the flat button
	 * @date 12 août 2023
	 */
	public FlatButton addMenuSign() {
		menu = true;
		computeSize = true;
		return this;
	}

	/**
	 * Removes the menu sign.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the flat button
	 * @date 25 août 2023
	 */
	public FlatButton removeMenuSign() {
		menu = false;
		computeSize = true;
		return this;
	}

	/**
	 * Compute width of text.Does not dispose of the gc !
	 *
	 * @return the int
	 */
	private Point computeExtentOfText(final GC gc, final String text) {
		if (text != null) { return gc.textExtent(text); }
		return NULL_EXTENT;
	}

	/**
	 * Compute preferred size.
	 */
	public void computePreferredSize() {
		GC gc = new GC(this);
		try {
			computePreferredSize(gc);
		} finally {
			gc.dispose();
		}
	}

	/**
	 * Compute preferred size.
	 *
	 * @param gc
	 *            the gc
	 */
	public void computePreferredSize(final GC gc) {
		if (!computeSize) { return; }
		try {
			// DEBUG.OUT("Computing. Preferred size of '" + getText() + "'. ", false);
			preferredWidth = 0;
			preferredHeight = 0;
			final Image im = getImage();
			if (im != null) {
				Rectangle imb = im.getBounds();
				preferredWidth += imb.width + IMAGE_PADDING * 2;
				preferredHeight += imb.height;
				// DEBUG.OUT("image " + (imb.width + IMAGE_PADDING * 2) + " x " + imb.height + "; ", false);
			}
			if (menu) {
				preferredWidth += MENU_BOUNDS.width + IMAGE_PADDING * 2;
				preferredHeight = Math.max(preferredHeight, MENU_BOUNDS.height);
				// DEBUG.OUT("menu " + (MENU_BOUNDS.width + IMAGE_PADDING * 2) + " x " + MENU_BOUNDS.height + "; ",
				// false);
			}
			if (text != null) {
				Point extent = computeExtentOfText(gc, text);
				preferredWidth += extent.x + INNER_MARGIN * 2;
				preferredHeight = Math.max(preferredHeight, extent.y + INNER_MARGIN);
				// DEBUG.OUT("text " + (extent.x + INNER_MARGIN * 2) + " x " + (extent.y + INNER_MARGIN) + "; ", false);
			}
			if (forcedWidth != SWT.DEFAULT) { preferredWidth = forcedWidth; }
			if (minimalHeight >= preferredHeight) { preferredHeight = minimalHeight; }
		} finally {
			// DEBUG.OUT("==> " + preferredWidth + " x " + preferredHeight);
			computeSize = false;
		}

	}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText() { return text; }

	/**
	 * Sets the text.
	 *
	 * @param text
	 *            the text
	 * @return the flat button
	 */
	public FlatButton setText(final String text) {
		if (text == null || text.equals(this.text)) { return this; }
		this.text = text;
		computeSize = true;
		redraw();
		return this;
	}

	/**
	 * Sets the text without recomputing size.
	 *
	 * @param text
	 *            the text
	 * @return the flat button
	 */
	public FlatButton setTextWithoutRecomputingSize(final String text) {
		if (text == null || text.equals(this.text)) { return this; }
		this.text = text;
		redraw();
		return this;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
		redraw();
	}

	/**
	 * Disabled.
	 *
	 * @return the flat button
	 */
	public FlatButton disabled() {
		setEnabled(false);
		return this;
	}

	/**
	 * Sets the color.
	 *
	 * @param c
	 *            the c
	 * @return the flat button
	 */
	public FlatButton setColor(final GamaUIColor c) {
		if (c == null) { return this; }
		final RGB oldColorCode = colorCode;
		final RGB newColorCode = c.getRGB();
		if (newColorCode.equals(oldColorCode)) { return this; }
		colorCode = c.getRGB();
		redraw();
		return this;
	}

	/**
	 * Sets the width.
	 *
	 * @param width
	 *            the width
	 * @return the flat button
	 */
	public FlatButton withFixedWidth(final int width) {
		forcedWidth = width;
		return this;
	}

	/**
	 * Sets the fixed width.
	 */
	public FlatButton withFixedWidth() {
		return withFixedWidth(computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
	}

	/**
	 * With height.
	 *
	 * @param height
	 *            the height
	 * @return the flat button
	 */
	public FlatButton withHeight(final int height) {
		minimalHeight = height;
		return this;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	public GamaUIColor getColor() { return GamaColors.get(colorCode); }

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	private Image getImage() { return image; }

	/**
	 * With border.
	 *
	 * @return the flat button
	 */
	public FlatButton withBorder() {
		border = true;
		return this;
	}

	/**
	 *
	 */
	public void disposeImage() {
		if (image == null) { return; }
		image.dispose();
		image = null;
	}

	/**
	 *
	 */
	public void click(final Event e) {
		SelectionEvent event = new SelectionEvent(e);
		event.widget = this;
		this.selectionListener.widgetSelected(event);
	}

}