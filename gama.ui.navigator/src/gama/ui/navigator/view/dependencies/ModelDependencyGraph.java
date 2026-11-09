package gama.ui.navigator.view.dependencies;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;

/** A saved-file snapshot, independent of compiler resources and layout state. */
public record ModelDependencyGraph(URI focus, Map<URI, Node> nodes, List<Edge> edges, List<Resource> resources) {

	public enum State { AVAILABLE, WARNING, ERROR, MISSING }

	public record Node(URI uri, String name, String path, State state, List<String> diagnostics) {
		public Node {
			diagnostics = List.copyOf(diagnostics);
		}
	}

	public record Edge(URI source, URI target, String alias, int line) {}

	/** One occurrence per owner keeps shared resources close to each model that uses them. */
	public record Resource(URI owner, Node file) {}

	public ModelDependencyGraph(final URI focus, final Map<URI, Node> nodes, final List<Edge> edges) {
		this(focus, nodes, edges, List.of());
	}

	public ModelDependencyGraph {
		nodes = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(nodes));
		edges = List.copyOf(edges);
		resources = List.copyOf(resources);
	}

	public boolean incomplete() {
		return nodes.values().stream().anyMatch(node -> node.state() != State.AVAILABLE);
	}
}
