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
	static final Image menuImage = GamaIcon.named(IGamaIcons.SMALL_DROPDOWN).image();

	/** The Constant menuImageBounds. */
	static final Rectangle menuImageBounds = menuImage.getBounds();

	/** The Constant innerMargin. */
	private static final int innerMargin = 5, imagePadding = 5;

	/** The Constant nullExtent. */
	private static final Point nullExtent = new Point(0, 0);

	/** The Constant MINIMAL_HEIGHT. */
	private static final int MINIMAL_HEIGHT = 24;

	/** The selection listener. */
	private Selector selectionListener;

	/** The image. */
	private Image image;

	/** The text. */
	private String text;

	/** The color code. */
	private RGB colorCode;

	/** The preferred height. */
	private int preferredHeight = SWT.DEFAULT, preferredWidth = SWT.DEFAULT, forcedWidth = SWT.DEFAULT;

	// minimalHeight = SWT.DEFAULT,

	/** States */
	private boolean enabled = true, hovered = false, down = false, border = false, menu = false;

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
		return create(comp, SWT.None).withWidth(forcedWidth).setText(text).setColor(color);
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
		if (!enabled) return;
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
		if (!enabled) return;
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
		float v_inset;
		// DEBUG.OUT("Button '" + getText() + "' preferred width = " + preferredWidth + " bounds width = "
		// + getBounds().width);
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

		float x = innerMargin;
		final Image im = getImage();
		float y_text = 0;
		final String contents = newText();
		if (contents != null) { y_text += (getBounds().height - gc.textExtent(contents).y) / 2f; }

		if (im != null) {
			float y_image = (getBounds().height - im.getBounds().height) / 2f;
			x = drawImage(im, gc, Math.round(x), Math.round(y_image));
		}
		gc.drawString(contents, Math.round(x), Math.round(y_text));
		if (menu) {
			float y_image = (getBounds().height - menuImageBounds.height) / 2f;
			x = innerMargin;
			x = rect.width - x - menuImageBounds.width;
			drawImage(menuImage, gc, Math.round(x), Math.round(y_image));
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
		if (image == null) return x;
		gc.drawImage(image, x, y);
		return x + image.getBounds().width + imagePadding;
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		// DEBUG.OUT("Computing size of '" + text + "' called from ");
		// DEBUG.STACK();
		int width = wHint != SWT.DEFAULT ? wHint : preferredWidth;
		int height = hHint != SWT.DEFAULT ? hHint : preferredHeight;
		return super.computeSize(width, height, changed);

		// return new Point(width, height);
	}

	/**
	 * New text.
	 *
	 * @return the string
	 */
	public String newText() {
		if (text == null) return null;
		// final float parentWidth = getParent().getBounds().width;
		final float width = preferredWidth;
		final float textWidth = computeExtentOfText().x;
		if (textWidth > width) {
			float imageWidth = 0;
			final Image im = getImage();
			if (im != null || menu) {
				if (im != null) { imageWidth = im.getBounds().width + imagePadding * 2; }
				if (menu) { imageWidth += menuImageBounds.width + imagePadding * 2; }
			}

			// if (parentWidth < width) {
			// r = (parentWidth - imageWidth) / width;
			// } else {
			float r = (width - imageWidth) / textWidth;
			// }
			final int nbChars = text.length();
			final int newNbChars = Math.max(0, (int) (nbChars * r));
			return text.substring(0, newNbChars / 2) + "..." + text.substring(nbChars - newNbChars / 2, nbChars);
		}
		return text;
	}

	/**
	 * This is an image that will be displayed to the side of the text inside the button (if any). By default the image
	 * will be to the left of the text; however, setImageStyle can be used to specify that it's either to the right or
	 * left. If there is no text, the image will be centered inside the button.
	 *
	 * @param image
	 */
	public FlatButton setImage(final Image image) {
		if (this.image == image) return this;
		this.image = image;
		computePreferredSize();
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
		if (this.image == image) return this;
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
		computePreferredSize();
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
		computePreferredSize();
		return this;
	}

	/**
	 * Compute width of text.
	 *
	 * @return the int
	 */
	public Point computeExtentOfText() {
		if (text != null) {
			final GC gc = new GC(this);
			gc.setFont(getFont());
			final Point extent = gc.textExtent(text);
			gc.dispose();
			return extent;
		}
		return nullExtent;
	}

	/**
	 * Compute preferred size.
	 */
	public void computePreferredSize() {
		Rectangle bounds = new Rectangle(0, 0, 0, MINIMAL_HEIGHT);
		final Image im = getImage();
		if (im != null) {
			Rectangle imBounds = im.getBounds();
			bounds.height = Math.max(imBounds.height, bounds.height);
			bounds.width += imBounds.width + imagePadding * 2;
		}
		if (menu) {
			bounds.width += menuImageBounds.width + imagePadding * 2;
			bounds.height = Math.max(bounds.height, menuImageBounds.height);
		}
		if (text != null) {
			Point extent = computeExtentOfText();
			bounds.width += extent.x + innerMargin * 2;
			bounds.height = Math.max(bounds.height, extent.y + innerMargin);
		}
		if (forcedWidth != SWT.DEFAULT) { bounds.width = forcedWidth; }
		preferredWidth = bounds.width;
		preferredHeight = bounds.height;
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
		if (text == null || text.equals(this.text)) return this;
		this.text = text;
		computePreferredSize();
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
		if (text == null || text.equals(this.text)) return this;
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
		if (c == null) return this;
		final RGB oldColorCode = colorCode;
		final RGB newColorCode = c.getRGB();
		if (newColorCode.equals(oldColorCode)) return this;
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
	public FlatButton withWidth(final int width) {
		forcedWidth = width;
		return this;
	}

	/**
	 * With minimal height.
	 *
	 * @param height
	 *            the height
	 * @return the flat button
	 */
	// public FlatButton withMinimalHeight(final int height) {
	// minimalHeight = height;
	// return this;
	// }

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
	 * Sent by the layout
	 */
	// @Override
	// public void setBounds(final int x, final int y, final int width, final
	// int height) {
	// // withWidth(width);
	// super.setBounds(x, y, width, height);
	// }

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
		if (image == null) return;
		image.dispose();
		image = null;
	}

}