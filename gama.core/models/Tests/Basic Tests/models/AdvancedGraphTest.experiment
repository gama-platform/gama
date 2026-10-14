/**
* Name: AdvancedGraphTest
* Author: Antigravity
* Description: Tests advanced graph algorithms (components, paths, centralities)
* Tags: graph, algorithm, test
*/

model AdvancedGraphTestModel

global {
	graph g;
	
	init {
		// A triangle and a disconnected component
		g <- as_edge_graph([{0,0}::{1,1}, {1,1}::{2,2}, {2,2}::{0,0}, {10,10}::{11,11}]);
	}
}

experiment AdvancedGraphTest type: test autorun: true {
	
	test "Connected Components" {
		list<list> components <- connected_components_of(g);
		
		// Two components: {a, b, c} and {d, e}
		assert length(components) = 2;
		
		// Sort by length to reliably check
		list<list> sorted_comp <- components sort_by (length(each));
		assert length(sorted_comp[0]) = 2; // {d, e}
		assert length(sorted_comp[1]) = 3; // {a, b, c}
	}
	
	test "Graph Metrics" {
		assert length(g.vertices) = 5;
		assert length(g.edges) = 4;
		
		// with v = 5 vertices, e = 4 edges and p = 2 connected components:
		// one independent cycle (the triangle): e - v + p
		assert nb_cycles(g) = 1;
		// alpha = cycles / (2v - 5)
		assert alpha_index(g) = 0.2;
		// beta = e / v
		assert beta_index(g) = 0.8;
		// gamma = e / (2v - 5)
		assert gamma_index(g) = 0.8;
		// connectivity = (v - p) / e
		assert connectivity_index(g) = 0.75;
	}
	
	test "Graph Parsing" {
		graph g2 <- as_edge_graph([{0,0}::{1,1}, {1,1}::{2,2}, {2,2}::{3,3}]);
		path p <- path_between(g2, {0,0}, {3,3});
		
		assert p != nil;
		assert length(p.edges) = 3;
	}
}
