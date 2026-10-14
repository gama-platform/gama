/**
* Name: DynamicGraphTest
* Author: Antigravity
* Description: Tests the dynamic manipulation of graphs at runtime
* Tags: graph, dynamic, test
*/

model DynamicGraphTestModel

species node_agent {}

experiment DynamicGraphTest type: test autorun: true {
	
	test "Dynamic Graph Modification" {
		graph g <- as_edge_graph([]); // Empty graph
		
		assert length(g.vertices) = 0;
		
		create node_agent number: 3 returns: nodes;
		node_agent n1 <- nodes[0];
		node_agent n2 <- nodes[1];
		node_agent n3 <- nodes[2];
		
		// Add nodes manually
		g <- g add_node n1;
		g <- g add_node n2;
		g <- g add_node n3;
		
		assert length(g.vertices) = 3;
		
		// Add edges manually
		g <- g add_edge (n1::n2);
		g <- g add_edge (n2::n3);
		
		assert length(g.edges) = 2;
	}
	
	test "Dynamic Edge and Node Removal" {
		create node_agent number: 4 returns: nodes;
		node_agent n1 <- nodes[0];
		node_agent n2 <- nodes[1];
		node_agent n3 <- nodes[2];
		node_agent n4 <- nodes[3];
		
		graph g <- as_edge_graph([n1::n2, n2::n3, n3::n4, n4::n1]);
		
		assert length(g.vertices) = 4;
		assert length(g.edges) = 4;
		
		g <- n3 remove_node_from g;
		assert length(g.vertices) = 3;
		assert length(g.edges) = 2; // Edges connected to n3 are also removed
	}
	
}
