package gama.ui.navigator.view.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;
import org.jgrapht.graph.DirectedPseudograph;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Rectangle;

import gama.ui.navigator.view.dependencies.ModelDependencyGraph.Edge;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.Resource;

/** The only adapter to Jungrapht; published geometry contains no library or SWT objects. */
public final class DependencyGraphLayout {

	public record Size(double width, double height) {}

	public record Measurements(Map<URI, Size> models, Map<Resource, Size> resources) {
		public Measurements {
			models = Map.copyOf(models);
			resources = Map.copyOf(resources);
		}
	}

	private record Group(Size size, Map<Resource, Bounds> resources) {}

	private record Column(double left, double right) {}

	public record Point(double x, double y) {}

	public record Bounds(double x, double y, double width, double height) {
		public Point center() { return new Point(x + width / 2, y + height / 2); }
		public boolean contains(final double px, final double py) {
			return px >= x && px <= x + width && py >= y && py <= y + height;
		}
	}

	public record Geometry(ModelDependencyGraph graph, Map<URI, Bounds> cards, Map<Edge, List<Point>> routes,
			Map<Resource, Bounds> resourceCards, Map<Resource, List<Point>> resourceRoutes, double width, double height) {
		public Geometry {
			cards = Collections.unmodifiableMap(new LinkedHashMap<>(cards));
			final var copied = new LinkedHashMap<Edge, List<Point>>();
			routes.forEach((edge, points) -> copied.put(edge, List.copyOf(points)));
			routes = Collections.unmodifiableMap(copied);
			resourceCards = Collections.unmodifiableMap(new LinkedHashMap<>(resourceCards));
			final var copiedResources = new LinkedHashMap<Resource, List<Point>>();
			resourceRoutes.forEach((resource, points) -> copiedResources.put(resource, List.copyOf(points)));
			resourceRoutes = Collections.unmodifiableMap(copiedResources);
		}
	}

	private DependencyGraphLayout() {}

	public static Geometry compute(final ModelDependencyGraph snapshot, final Map<URI, Size> sizes,
			final IProgressMonitor monitor) {
		return compute(snapshot, new Measurements(sizes, Map.of()), monitor);
	}

	public static Geometry compute(final ModelDependencyGraph snapshot, final Measurements measurements,
			final IProgressMonitor monitor) {
		final var groups = new LinkedHashMap<URI, Group>();
		final var sizes = new LinkedHashMap<URI, Size>();
		final var resourcesByOwner = new LinkedHashMap<URI, List<Resource>>();
		for (final var resource : snapshot.resources()) {
			resourcesByOwner.computeIfAbsent(resource.owner(), key -> new ArrayList<>()).add(resource);
		}
		for (final URI uri : snapshot.nodes().keySet()) {
			checkCanceled(monitor);
			final var group = group(measurements.models().get(uri), resourcesByOwner.getOrDefault(uri, List.of()),
					measurements.resources());
			groups.put(uri, group);
			sizes.put(uri, group.size());
		}
		final var graph = new DirectedPseudograph<URI, Edge>(null, null, false);
		snapshot.nodes().keySet().stream().sorted(Comparator.comparing(URI::toString)).forEach(graph::addVertex);
		snapshot.edges().forEach(edge -> graph.addEdge(edge.source(), edge.target(), edge));
		// Self-imports stay visible but must not enter the layered algorithm.
		final var loops = new ArrayList<Edge>();
		for (final var edge : snapshot.edges()) {
			if (edge.source().equals(edge.target())) {
				loops.add(edge);
				graph.removeEdge(edge);
			}
		}
		checkCanceled(monitor);
		final var model = LayoutModel.<URI>builder().graph(graph).size(1000, 1000).build();
		final var algorithm = SugiyamaLayoutAlgorithm.<URI, Edge>edgeAwareBuilder()
				.threaded(false).separateComponents(false).expandLayout(false).maxLevelCross(8)
				.postStraighten(false)
				.vertexBoundsFunction(uri -> {
					checkCanceled(monitor);
					// Jungrapht represents routing-only synthetic vertices with a null model vertex.
					if (uri == null) return Rectangle.IDENTITY;
					final var size = sizes.get(uri);
					// Layout is top-down; swap dimensions before rotating its geometry to left-to-right.
					return Rectangle.of(-(size.height() + 28) / 2, -(size.width() + 40) / 2,
							size.height() + 28, size.width() + 40);
				})
				.edgeComparator((a, b) -> {
					checkCanceled(monitor);
					final int source = a.source().toString().compareTo(b.source().toString());
					return source == 0 ? a.target().toString().compareTo(b.target().toString()) : source;
				}).build();
		if (graph.vertexSet().size() == 1) {
			model.set(graph.vertexSet().iterator().next(), 0, 0);
		} else {
			algorithm.visit(model);
		}
		checkCanceled(monitor);
		final var layoutRoutes = new LinkedHashMap<Edge, List<Point>>();
		final var layerWidths = new TreeMap<Double, Double>();
		final var layerCards = new TreeMap<Double, List<URI>>();
		for (final URI uri : graph.vertexSet()) {
			final var position = model.get(uri);
			layerWidths.merge(position.y, sizes.get(uri).width(), Math::max);
			layerCards.computeIfAbsent(position.y, key -> new ArrayList<>()).add(uri);
		}
		for (final var edge : graph.edgeSet()) {
			final var points = algorithm.getEdgeArticulationFunction().apply(edge).stream()
					.map(p -> new Point(p.y, p.x)).toList();
			layoutRoutes.put(edge, points);
			for (final var point : points) layerWidths.putIfAbsent(point.x(), 0.0);
		}
		// Jungrapht stretches coordinates into a square independently of card dimensions.
		// Repack its layers and compress their cross-axis spacing, preserving its ordering and routes.
		final var layerPositions = new LinkedHashMap<Double, Double>();
		double nextX = 0;
		for (final var layer : layerWidths.entrySet()) {
			final double width = Math.max(12, layer.getValue());
			layerPositions.put(layer.getKey(), nextX + width / 2);
			nextX += width + 56;
		}
		double verticalScale = 0;
		for (final var vertices : layerCards.values()) {
			vertices.sort(Comparator.comparingDouble(uri -> model.get(uri).x));
			for (int i = 1; i < vertices.size(); i++) {
				final URI before = vertices.get(i - 1), after = vertices.get(i);
				final double distance = model.get(after).x - model.get(before).x;
				if (distance > 0) verticalScale = Math.max(verticalScale,
						(sizes.get(before).height() / 2 + sizes.get(after).height() / 2 + 24) / distance);
			}
		}
		// Long and backward imports have routing-only vertices that also need to clear resource clusters.
		if (!snapshot.resources().isEmpty()) {
			for (final var points : layoutRoutes.values()) {
				for (final var point : points) {
					for (final URI uri : layerCards.getOrDefault(point.x(), List.of())) {
						final double distance = Math.abs(point.y() - model.get(uri).x);
						if (distance > 1e-8) verticalScale = Math.max(verticalScale,
								(sizes.get(uri).height() / 2 + 12) / distance);
					}
				}
			}
		}
		final double crossScale = verticalScale == 0 ? 0.25 : verticalScale;
		final var cards = new LinkedHashMap<URI, Bounds>();
		final var resourceCards = new LinkedHashMap<Resource, Bounds>();
		final var columns = new LinkedHashMap<Double, Column>();
		double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
		for (final URI uri : graph.vertexSet()) {
			final var point = model.get(uri);
			final var size = sizes.get(uri);
			final var cluster = new Bounds(layerPositions.get(point.y) - size.width() / 2,
					point.x * crossScale - size.height() / 2,
					size.width(), size.height());
			final var modelSize = measurements.models().get(uri);
			final var card = new Bounds(cluster.center().x() - modelSize.width() / 2, cluster.y(),
					modelSize.width(), modelSize.height());
			cards.put(uri, card);
			groups.get(uri).resources().forEach((resource, b) -> resourceCards.put(resource,
					new Bounds(cluster.x() + b.x(), cluster.y() + b.y(), b.width(), b.height())));
			columns.merge(cluster.center().x(), new Column(cluster.x(), cluster.x() + cluster.width()),
					(a, b) -> new Column(Math.min(a.left(), b.left()), Math.max(a.right(), b.right())));
			minX = Math.min(minX, cluster.x());
			minY = Math.min(minY, cluster.y());
		}
		final var routes = new LinkedHashMap<Edge, List<Point>>();
		for (final var edge : snapshot.edges()) {
			checkCanceled(monitor);
			final var source = cards.get(edge.source());
			final var target = cards.get(edge.target());
			final var points = new ArrayList<Point>();
			if (loops.contains(edge)) {
				final double top = source.y() - (snapshot.resources().isEmpty() ? 25 : 12);
				points.add(new Point(source.x() + source.width(), source.y() + source.height() / 2));
				points.add(new Point(source.x() + source.width() + 35, source.y() + source.height() / 2));
				points.add(new Point(source.x() + source.width() + 35, top));
				points.add(new Point(source.x() + source.width() / 2, top));
				points.add(new Point(source.x() + source.width() / 2, source.y()));
			} else {
				for (final var p : layoutRoutes.get(edge)) {
					points.add(new Point(layerPositions.get(p.x()), p.y() * crossScale));
				}
				if (points.size() < 2) {
					points.clear();
					points.add(source.center());
					points.add(target.center());
				}
				if (distance(points.getFirst(), source.center()) > distance(points.getLast(), source.center())) {
					Collections.reverse(points);
				}
				points.set(0, source.center());
				points.set(points.size() - 1, target.center());
				final var routed = routeThroughColumns(points, source, target, columns);
				points.clear();
				points.addAll(routed);
			}
			for (final var p : points) {
				minX = Math.min(minX, p.x());
				minY = Math.min(minY, p.y());
			}
			routes.put(edge, points);
		}
		final double dx = 36 - minX, dy = 36 - minY;
		cards.replaceAll((uri, b) -> new Bounds(b.x() + dx, b.y() + dy, b.width(), b.height()));
		resourceCards.replaceAll((resource, b) -> new Bounds(b.x() + dx, b.y() + dy, b.width(), b.height()));
		routes.replaceAll((edge, points) -> points.stream().map(p -> new Point(p.x() + dx, p.y() + dy)).toList());
		final var resourceRoutes = new LinkedHashMap<Resource, List<Point>>();
		for (final var entry : resourceCards.entrySet()) {
			checkCanceled(monitor);
			final var owner = cards.get(entry.getKey().owner());
			final var target = entry.getValue();
			final double busY = owner.y() + owner.height() + 8;
			resourceRoutes.put(entry.getKey(), List.of(new Point(owner.center().x(), owner.y() + owner.height()),
					new Point(owner.center().x(), busY), new Point(target.x() - 8, busY),
					new Point(target.x() - 8, target.center().y()), new Point(target.x(), target.center().y())));
		}
		double width = 0, height = 0;
		for (final var card : cards.values()) {
			width = Math.max(width, card.x() + card.width());
			height = Math.max(height, card.y() + card.height());
		}
		for (final var card : resourceCards.values()) {
			width = Math.max(width, card.x() + card.width());
			height = Math.max(height, card.y() + card.height());
		}
		for (final var points : routes.values()) {
			for (final var p : points) {
				if (!Double.isFinite(p.x()) || !Double.isFinite(p.y())) throw new IllegalStateException("Invalid edge layout");
				width = Math.max(width, p.x());
				height = Math.max(height, p.y());
			}
		}
		for (final var card : cards.values()) {
			if (!Double.isFinite(card.x()) || !Double.isFinite(card.y())) throw new IllegalStateException("Invalid node layout");
		}
		return new Geometry(snapshot, cards, routes, resourceCards, resourceRoutes, width + 36, height + 36);
	}

	private static Group group(final Size model, final List<Resource> resources, final Map<Resource, Size> sizes) {
		if (resources.isEmpty()) return new Group(model, Map.of());
		final int columns = resources.size() > 4 ? 2 : 1;
		final double[] widths = new double[columns];
		double rowHeight = 0;
		for (int i = 0; i < resources.size(); i++) {
			final var size = sizes.get(resources.get(i));
			widths[i % columns] = Math.max(widths[i % columns], size.width());
			rowHeight = Math.max(rowHeight, size.height());
		}
		final double width = Math.max(model.width(), 24 + widths[0] + (columns == 2 ? widths[1] + 20 : 0));
		final var cards = new LinkedHashMap<Resource, Bounds>();
		for (int i = 0; i < resources.size(); i++) {
			final var resource = resources.get(i);
			final var size = sizes.get(resource);
			cards.put(resource, new Bounds(24 + (i % columns == 0 ? 0 : widths[0] + 20),
					model.height() + 20 + i / columns * (rowHeight + 8), size.width(), rowHeight));
		}
		final int rows = (resources.size() + columns - 1) / columns;
		return new Group(new Size(width, model.height() + 20 + rows * (rowHeight + 8) - 8), cards);
	}

	private static void checkCanceled(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) throw new OperationCanceledException();
	}

	private static double distance(final Point a, final Point b) {
		return Math.hypot(a.x() - b.x(), a.y() - b.y());
	}

	private static List<Point> routeThroughColumns(final List<Point> articulations, final Bounds source,
			final Bounds target, final Map<Double, Column> columns) {
		final var result = new ArrayList<Point>();
		for (int i = 1; i < articulations.size(); i++) {
			final var from = articulations.get(i - 1);
			final var to = articulations.get(i);
			final boolean sameColumn = from.x() == to.x();
			final boolean forward = from.x() <= to.x();
			final var fromColumn = columns.getOrDefault(from.x(), new Column(from.x(), from.x()));
			final var toColumn = columns.getOrDefault(to.x(), new Column(to.x(), to.x()));
			final double channel = sameColumn ? fromColumn.right() + 35
					: forward ? (fromColumn.right() + toColumn.left()) / 2
					: (fromColumn.left() + toColumn.right()) / 2;
			final var start = i == 1 ? new Point(forward ? source.x() + source.width() : source.x(), from.y()) : from;
			final var end = i == articulations.size() - 1
					? new Point(sameColumn || !forward ? target.x() + target.width() : target.x(), to.y()) : to;
			addPoint(result, start);
			addPoint(result, new Point(channel, from.y()));
			addPoint(result, new Point(channel, to.y()));
			addPoint(result, end);
		}
		return result;
	}

	private static void addPoint(final List<Point> points, final Point point) {
		if (points.isEmpty() || !points.getLast().equals(point)) points.add(point);
	}
}
