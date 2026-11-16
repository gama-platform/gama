package gama.ui.navigator.view.dependencies;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.navigator.view.dependencies.DependencyGraphLayout.Bounds;
import gama.ui.navigator.view.dependencies.DependencyGraphLayout.Geometry;
import gama.ui.navigator.view.dependencies.DependencyGraphLayout.Measurements;
import gama.ui.navigator.view.dependencies.DependencyGraphLayout.Size;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.Node;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.Resource;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.State;
import gama.ui.navigator.view.contents.VirtualContent;

/** Native graph rendering; mouse and keyboard operations never trigger model loading or layout. */
public final class DependencyGraphCanvas extends Canvas {

	private record Item(URI uri, URI owner) {
		static Item model(final URI uri) { return new Item(uri, null); }
		static Item resource(final Resource resource) { return new Item(resource.file().uri(), resource.owner()); }
	}

	private final Consumer<URI> openFile;
	private final Font titleFont;
	private final Font detailFont;
	private final ThemeHelper.IThemeListener themeListener;
	private final ArrayList<Color> colors = new ArrayList<>();
	private Color background, cardColor, foreground, muted, border, accent, highlight, shadow, resourceLink;
	private Image modelIcon, okIcon, warningIcon, errorIcon;
	private final LocalResourceManager imageResources;
	private final Map<URI, Image> resourceIcons = new LinkedHashMap<>();
	private final Map<Item, Bounds> items = new LinkedHashMap<>();
	private final Map<Item, Node> nodes = new LinkedHashMap<>();
	private Geometry geometry;
	private Item hovered, focused, pressed;
	private double zoom = 1, panX, panY;
	private int downX, downY;
	private double downPanX, downPanY;
	private boolean dragging, moved, fitToView = true;
	private String message = "Choose a model with Show dependencies or enable Sync with editor.";

	public DependencyGraphCanvas(final Composite parent, final Consumer<URI> openFile) {
		super(parent, SWT.DOUBLE_BUFFERED);
		this.openFile = openFile;
		imageResources = new LocalResourceManager(JFaceResources.getResources(), this);
		final var titleData = getFont().getFontData();
		for (final var data : titleData) data.setStyle(SWT.BOLD);
		titleFont = new Font(getDisplay(), titleData);
		final var detailData = getFont().getFontData();
		for (final var data : detailData) data.setHeight(Math.max(8, data.getHeight() - 1));
		detailFont = new Font(getDisplay(), detailData);
		updateColors();
		final var display = getDisplay();
		themeListener = light -> display.asyncExec(() -> {
			if (!isDisposed()) {
				updateColors();
				if (geometry != null) loadResourceIcons(geometry.graph());
				redraw();
			}
		});
		ThemeHelper.addListener(themeListener);
		addPaintListener(event -> paint(event.gc));
		addListener(SWT.Resize, event -> {
			if (fitToView) fit();
		});
		addListener(SWT.MouseDown, this::mouseDown);
		addListener(SWT.MouseMove, this::mouseMove);
		addListener(SWT.MouseUp, this::mouseUp);
		addListener(SWT.MouseExit, event -> {
			hovered = null;
			setToolTipText(null);
			redraw();
		});
		addListener(SWT.MouseWheel, event -> {
			zoomAt(Math.pow(1.12, event.count), event.x, event.y);
			event.doit = false;
		});
		addListener(SWT.KeyDown, this::keyDown);
		addListener(SWT.FocusIn, event -> redraw());
		addListener(SWT.FocusOut, event -> redraw());
		getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(final AccessibleEvent event) {
				event.result = geometry == null ? message : "Model dependencies. "
						+ (focused == null ? "" : tooltip(focused)) + ". Arrow keys move between files; Enter opens a file.";
			}
		});
		addDisposeListener(event -> {
			ThemeHelper.removeListener(themeListener);
			titleFont.dispose();
			detailFont.dispose();
			colors.forEach(Color::dispose);
		});
	}

	public Measurements measure(final ModelDependencyGraph graph) {
		final var result = new LinkedHashMap<URI, Size>();
		final var resources = new LinkedHashMap<Resource, Size>();
		final GC gc = new GC(this);
		try {
			for (final var node : graph.nodes().values()) {
				gc.setFont(titleFont);
				final int titleWidth = gc.textExtent(node.name()).x + modelIcon.getBounds().width + 40;
				final int titleHeight = gc.textExtent(node.name()).y;
				gc.setFont(detailFont);
				final int pathWidth = Math.min(220, gc.textExtent(subtitle(node.path())).x + 20);
				final int detailHeight = gc.textExtent("Model").y;
				result.put(node.uri(), new Size(Math.max(180, Math.min(280, Math.max(titleWidth, pathWidth))),
						Math.max(56, Math.max(titleHeight, modelIcon.getBounds().height) + detailHeight + 20)));
			}
			gc.setFont(detailFont);
			for (final var resource : graph.resources()) {
				final var text = gc.textExtent(resource.file().name());
				resources.put(resource, new Size(Math.max(100, Math.min(220, text.x + 36)), Math.max(26, text.y + 10)));
			}
		} finally {
			gc.dispose();
		}
		return new Measurements(result, resources);
	}

	private void loadResourceIcons(final ModelDependencyGraph graph) {
		resourceIcons.clear();
		for (final var resource : graph.resources()) {
			final var file = resource.file();
			if (resourceIcons.containsKey(file.uri())) continue;
			final var workspaceFile = ModelDependencyService.workspaceFile(file.uri());
			final Image image;
			if (workspaceFile != null && workspaceFile.exists()) {
				image = VirtualContent.DEFAULT_LABEL_PROVIDER.getImage(workspaceFile);
			} else {
				final var descriptor = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(file.name());
				image = imageResources.createImageWithDefault(descriptor);
			}
			resourceIcons.put(file.uri(), image == null ? GamaIcon.named(IGamaIcons.FILE_TEXT).image() : image);
		}
	}

	public void setGeometry(final Geometry next, final boolean fit) {
		geometry = next;
		items.clear();
		nodes.clear();
		next.cards().forEach((uri, bounds) -> {
			items.put(Item.model(uri), bounds);
			nodes.put(Item.model(uri), next.graph().nodes().get(uri));
		});
		next.resourceCards().forEach((resource, bounds) -> {
			items.put(Item.resource(resource), bounds);
			nodes.put(Item.resource(resource), resource.file());
		});
		loadResourceIcons(next.graph());
		focused = !fit && items.containsKey(focused) ? focused : Item.model(next.graph().focus());
		hovered = null;
		pressed = null;
		setToolTipText(null);
		if (fit || fitToView) fit();
		redraw();
	}

	public void setMessage(final String text) {
		message = text;
		redraw();
	}

	public Geometry geometry() { return geometry; }

	public void fit() {
		fitToView = true;
		if (geometry == null) return;
		final var area = getClientArea();
		if (area.width <= 0 || area.height <= 0) return;
		zoom = Math.max(0.08, Math.min(1, Math.min(area.width / geometry.width(), area.height / geometry.height())));
		panX = (area.width - geometry.width() * zoom) / 2;
		panY = (area.height - geometry.height() * zoom) / 2;
		redraw();
	}

	public void zoom(final double factor) {
		final var area = getClientArea();
		zoomAt(factor, area.width / 2, area.height / 2);
	}

	private void zoomAt(final double factor, final int x, final int y) {
		fitToView = false;
		final double next = Math.max(0.08, Math.min(2.5, zoom * factor));
		panX = x - (x - panX) * next / zoom;
		panY = y - (y - panY) * next / zoom;
		zoom = next;
		redraw();
	}

	private void mouseDown(final Event event) {
		if (event.button != 1) return;
		setFocus();
		downX = event.x;
		downY = event.y;
		downPanX = panX;
		downPanY = panY;
		pressed = hit(event.x, event.y);
		dragging = true;
		moved = false;
		setCapture(true);
	}

	private void mouseMove(final Event event) {
		if (dragging) {
			moved |= Math.hypot(event.x - downX, event.y - downY) > 5;
			if (moved) {
				fitToView = false;
				panX = downPanX + event.x - downX;
				panY = downPanY + event.y - downY;
				redraw();
			}
		} else {
			final Item next = hit(event.x, event.y);
			if (!java.util.Objects.equals(next, hovered)) {
				hovered = next;
				setToolTipText(next == null ? null : tooltip(next));
				setCursor(getDisplay().getSystemCursor(next == null ? SWT.CURSOR_ARROW : SWT.CURSOR_HAND));
				redraw();
			}
		}
	}

	private void mouseUp(final Event event) {
		if (event.button != 1 || !dragging) return;
		dragging = false;
		setCapture(false);
		if (!moved && pressed != null && pressed.equals(hit(event.x, event.y))) {
			focused = pressed;
			openFile.accept(focused.uri());
		}
		pressed = null;
		redraw();
	}

	private void keyDown(final Event event) {
		if (event.character == '+' || event.character == '=') zoom(1.2);
		else if (event.character == '-') zoom(1 / 1.2);
		else if (event.character == '0' || event.character == 'f') fit();
		else if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {
			if (focused != null) openFile.accept(focused.uri());
		} else if (geometry != null && (event.keyCode == SWT.ARROW_LEFT || event.keyCode == SWT.ARROW_RIGHT
				|| event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)) {
			final var origin = items.get(focused == null ? Item.model(geometry.graph().focus()) : focused).center();
			Item next = null;
			double best = Double.POSITIVE_INFINITY;
			for (final var entry : items.entrySet()) {
				final var center = entry.getValue().center();
				final double dx = center.x() - origin.x(), dy = center.y() - origin.y();
				final double along = switch (event.keyCode) {
					case SWT.ARROW_LEFT -> -dx;
					case SWT.ARROW_RIGHT -> dx;
					case SWT.ARROW_UP -> -dy;
					default -> dy;
				};
				final double across = event.keyCode == SWT.ARROW_LEFT || event.keyCode == SWT.ARROW_RIGHT ? dy : dx;
				final double score = Math.hypot(dx, dy) + Math.abs(across) * 2;
				if (along > 0 && score < best) {
					best = score;
					next = entry.getKey();
				}
			}
			if (next != null) {
				focused = next;
				hovered = null;
				final var center = items.get(next).center();
				final var area = getClientArea();
				if (center.x() * zoom + panX < 20 || center.x() * zoom + panX > area.width - 20
						|| center.y() * zoom + panY < 20 || center.y() * zoom + panY > area.height - 20) {
					fitToView = false;
					panX = area.width / 2.0 - center.x() * zoom;
					panY = area.height / 2.0 - center.y() * zoom;
				}
				setToolTipText(tooltip(next));
				redraw();
			}
			event.doit = false;
		}
	}

	private Item hit(final int x, final int y) {
		if (geometry == null) return null;
		for (final var entry : items.entrySet()) {
			if (entry.getValue().contains((x - panX) / zoom, (y - panY) / zoom)) return entry.getKey();
		}
		return null;
	}

	private String tooltip(final Item item) {
		final var node = nodes.get(item);
		final var text = new StringBuilder(node.name()).append('\n').append(node.path());
		text.append('\n').append(switch (node.state()) {
			case AVAILABLE -> item.owner() == null ? "No known problems" : "Resource reference";
			case WARNING -> "Warning";
			case ERROR -> "Error";
			case MISSING -> "Missing or inaccessible file";
		});
		for (final String diagnostic : node.diagnostics()) text.append('\n').append(diagnostic);
		if (item.owner() != null) {
			text.append("\nUsed by ").append(geometry.graph().nodes().get(item.owner()).name());
			return text.toString();
		}
		for (final var edge : geometry.graph().edges()) {
			if (edge.source().equals(item.uri())) {
				text.append("\nImports ").append(geometry.graph().nodes().get(edge.target()).name());
				if (edge.alias() != null) text.append(" as ").append(edge.alias());
				text.append(" (line ").append(edge.line()).append(')');
			}
		}
		return text.toString();
	}

	private void paint(final GC gc) {
		gc.setAntialias(SWT.ON);
		gc.setTextAntialias(SWT.ON);
		if (geometry == null) {
			gc.setForeground(muted);
			gc.drawText(message, 24, 30, true);
			return;
		}
		final var transform = new Transform(getDisplay());
		try {
			transform.translate((float) panX, (float) panY);
			transform.scale((float) zoom, (float) zoom);
			gc.setTransform(transform);
			final Item active = hovered != null ? hovered : isFocusControl() ? focused : null;
			final Set<Item> neighbors = new LinkedHashSet<>();
			if (active != null) neighbors.add(active);
			gc.setLineJoin(SWT.JOIN_ROUND);
			for (final var entry : geometry.routes().entrySet()) {
				final var edge = entry.getKey();
				final boolean selected = Item.model(edge.source()).equals(active) || Item.model(edge.target()).equals(active);
				if (selected) {
					neighbors.add(Item.model(edge.source()));
					neighbors.add(Item.model(edge.target()));
				}
				gc.setAlpha(active == null || selected ? 255 : 90);
				gc.setForeground(selected ? accent : border);
				gc.setLineWidth(selected ? 2 : 1);
				final var points = entry.getValue();
				drawRoute(gc, points);
				final var tip = points.get(points.size() - 1);
				final var previous = points.get(points.size() - 2);
				final double angle = Math.atan2(tip.y() - previous.y(), tip.x() - previous.x());
				gc.setBackground(selected ? accent : border);
				gc.fillPolygon(new int[] { (int) tip.x(), (int) tip.y(),
						(int) (tip.x() - 10 * Math.cos(angle - 0.45)), (int) (tip.y() - 10 * Math.sin(angle - 0.45)),
						(int) (tip.x() - 10 * Math.cos(angle + 0.45)), (int) (tip.y() - 10 * Math.sin(angle + 0.45)) });
			}
			for (final var entry : geometry.resourceRoutes().entrySet()) {
				final var resource = entry.getKey();
				final var item = Item.resource(resource);
				final var owner = Item.model(resource.owner());
				final boolean selected = item.equals(active) || owner.equals(active);
				if (selected) {
					neighbors.add(item);
					neighbors.add(owner);
				}
				gc.setAlpha(active == null || selected ? 210 : 70);
				gc.setForeground(resourceLink);
				gc.setLineWidth(selected ? 2 : 1);
				drawRoute(gc, entry.getValue());
			}
			gc.setAlpha(255);
			for (final var entry : geometry.cards().entrySet()) drawCard(gc, entry.getKey(), entry.getValue(), active, neighbors);
			for (final var entry : geometry.resourceCards().entrySet())
				drawResource(gc, entry.getKey(), entry.getValue(), active, neighbors);
			gc.setTransform(null);
		} finally {
			transform.dispose();
		}
	}

	private void drawCard(final GC gc, final URI uri, final Bounds b, final Item active, final Set<Item> neighbors) {
		final var node = geometry.graph().nodes().get(uri);
		final var item = Item.model(uri);
		final int x = (int) b.x(), y = (int) b.y(), width = (int) b.width(), height = (int) b.height();
		final boolean focus = uri.equals(geometry.graph().focus());
		final boolean ghost = node.state() == State.MISSING;
		final int alpha = active == null || neighbors.contains(item) ? 255 : 165;
		gc.setAlpha(ghost ? alpha * 2 / 3 : alpha);
		if (!ghost) {
			gc.setBackground(shadow);
			gc.fillRoundRectangle(x + 1, y + 3, width, height, 14, 14);
		}
		gc.setBackground(ghost ? background : item.equals(active) ? highlight : cardColor);
		gc.fillRoundRectangle(x, y, width, height, 14, 14);
		gc.setForeground(focus || item.equals(active) ? accent : border);
		gc.setLineWidth(focus || item.equals(active) ? 2 : 1);
		gc.setLineStyle(ghost ? SWT.LINE_DASH : SWT.LINE_SOLID);
		gc.drawRoundRectangle(x, y, width, height, 14, 14);
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.drawImage(modelIcon, x + 10, y + 7);
		gc.setFont(titleFont);
		final int titleHeight = Math.max(gc.textExtent(node.name()).y, modelIcon.getBounds().height);
		gc.setForeground(ghost ? muted : foreground);
		gc.drawText(ellipsize(gc, node.name(), width - modelIcon.getBounds().width - 40),
				x + modelIcon.getBounds().width + 16, y + 7, true);
		gc.setFont(detailFont);
		gc.setForeground(muted);
		gc.drawText(ellipsize(gc, subtitle(node.path()), width - 20), x + 10, y + titleHeight + 11, true);
		final Image statusIcon = switch (node.state()) {
			case AVAILABLE -> okIcon;
			case WARNING -> warningIcon;
			case ERROR, MISSING -> errorIcon;
		};
		gc.setAlpha(alpha);
		gc.drawImage(statusIcon, x + width - 18, y + 7 + (titleHeight - statusIcon.getBounds().height) / 2);
		if (isFocusControl() && item.equals(focused)) gc.drawFocus(x + 5, y + 5, width - 10, height - 10);
		gc.setAlpha(255);
	}

	private void drawResource(final GC gc, final Resource resource, final Bounds bounds, final Item active,
			final Set<Item> neighbors) {
		final var item = Item.resource(resource);
		final var file = resource.file();
		final int x = (int) bounds.x(), y = (int) bounds.y(), width = (int) bounds.width(), height = (int) bounds.height();
		final boolean ghost = file.state() == State.MISSING;
		final int alpha = active == null || neighbors.contains(item) ? 255 : 150;
		gc.setAlpha(ghost ? alpha * 2 / 3 : alpha);
		gc.setBackground(ghost ? background : item.equals(active) ? highlight : cardColor);
		gc.fillRoundRectangle(x, y, width, height, 8, 8);
		gc.setForeground(resourceLink);
		gc.setLineWidth(item.equals(active) ? 2 : 1);
		gc.setLineStyle(ghost ? SWT.LINE_DASH : SWT.LINE_SOLID);
		gc.drawRoundRectangle(x, y, width, height, 8, 8);
		gc.setLineStyle(SWT.LINE_SOLID);
		final var image = resourceIcons.get(file.uri());
		final var imageBounds = image.getBounds();
		final int iconWidth = Math.min(16, imageBounds.width), iconHeight = Math.min(16, imageBounds.height);
		gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height,
				x + 6, y + (height - iconHeight) / 2, iconWidth, iconHeight);
		gc.setFont(detailFont);
		gc.setForeground(ghost ? muted : foreground);
		gc.drawText(ellipsize(gc, file.name(), width - 34), x + 28,
				y + (height - gc.textExtent(file.name()).y) / 2, true);
		if (isFocusControl() && item.equals(focused)) gc.drawFocus(x + 3, y + 3, width - 6, height - 6);
		gc.setAlpha(255);
	}

	private void drawRoute(final GC gc, final java.util.List<DependencyGraphLayout.Point> points) {
		final var path = new Path(getDisplay());
		try {
			path.moveTo((float) points.getFirst().x(), (float) points.getFirst().y());
			for (int i = 1; i < points.size() - 1; i++) {
				final var previous = points.get(i - 1);
				final var corner = points.get(i);
				final var next = points.get(i + 1);
				final double before = Math.hypot(corner.x() - previous.x(), corner.y() - previous.y());
				final double after = Math.hypot(next.x() - corner.x(), next.y() - corner.y());
				if (before == 0 || after == 0) {
					path.lineTo((float) corner.x(), (float) corner.y());
					continue;
				}
				final double radius = Math.min(10, Math.min(before, after) / 2);
				path.lineTo((float) (corner.x() + (previous.x() - corner.x()) * radius / before),
						(float) (corner.y() + (previous.y() - corner.y()) * radius / before));
				path.quadTo((float) corner.x(), (float) corner.y(),
						(float) (corner.x() + (next.x() - corner.x()) * radius / after),
						(float) (corner.y() + (next.y() - corner.y()) * radius / after));
			}
			path.lineTo((float) points.getLast().x(), (float) points.getLast().y());
			gc.drawPath(path);
		} finally {
			path.dispose();
		}
	}

	private static String subtitle(final String path) {
		final int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		return slash < 0 ? path : path.substring(0, slash);
	}

	private static String ellipsize(final GC gc, final String text, final int width) {
		if (gc.textExtent(text).x <= width) return text;
		int end = text.length();
		while (end > 0 && gc.textExtent(text.substring(0, end) + "...").x > width) end--;
		return text.substring(0, end) + "...";
	}

	private Color color(final int r, final int g, final int b) {
		final var color = new Color(getDisplay(), r, g, b);
		colors.add(color);
		return color;
	}

	private void updateColors() {
		colors.forEach(Color::dispose);
		colors.clear();
		final boolean dark = ThemeHelper.isDark();
		background = dark ? color(30, 33, 39) : color(245, 247, 250);
		cardColor = dark ? color(43, 48, 57) : color(255, 255, 255);
		foreground = dark ? color(232, 237, 244) : color(37, 48, 64);
		muted = dark ? color(165, 177, 193) : color(102, 117, 136);
		border = dark ? color(99, 113, 133) : color(167, 181, 198);
		accent = dark ? color(105, 179, 247) : color(49, 126, 193);
		highlight = dark ? color(48, 65, 83) : color(234, 245, 255);
		shadow = dark ? color(23, 26, 31) : color(225, 230, 238);
		resourceLink = dark ? color(93, 196, 177) : color(33, 143, 125);
		modelIcon = GamaIcon.named(IGamaIcons.FILE_ICON).image();
		okIcon = GamaIcon.named(IGamaIcons.OVERLAY_OK).image();
		warningIcon = GamaIcon.named(IGamaIcons.OVERLAY_WARNING).image();
		errorIcon = GamaIcon.named(IGamaIcons.OVERLAY_ERROR).image();
		GamaColors.setBackground(background, this);
		setForeground(foreground);
	}
}
