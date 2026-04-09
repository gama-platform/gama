/**
* Name: GraphTest
* Author: Minh Thu
* Description: Tests common graph operations
* Tags: graph, test
*/

model classicgraphgeneration

global {
	graph the_graph ;
	
	init {
		
	int i<-10;
	create node_agent number:7
	{
		location<-{i,i + ( (i/10) mod 2)*10};
		i<-i+10;
	}
	
		
		the_graph <- directed(as_distance_graph(node_agent, 30.0));
		
		
	}
	
	
}

species edge_agent {
	aspect default {	
		draw shape color: #black;
	}
}

species node_agent {
	aspect default {	
		draw circle(1) color: #red;
draw string(self as int) color: #black font: font("Helvetica", 14 , #bold) at: location - {1, 1, -0.5};			loop neigh over: the_graph neighbors_of self {
			draw line([self.location, agent(neigh).location]) color: #black;
		}

	}
}

experiment loadgraph type: gui {
	
	output {
		display map type: 3d{
			species edge_agent ;
			species node_agent ;
		}
	}
}

experiment GraphTest type: test {
	
			
	setup {
		/** Insert any initialization to run before each of the tests */
	}
	
	test "My Graph Test" {
		
		// Directed graph test
		assert the_graph in_degree_of (node_agent[2])=2;
		assert the_graph out_degree_of (node_agent[2])=2;
		assert the_graph degree_of (node_agent[2]) = 4;
		
		// out_edges_of
		assert length(the_graph out_edges_of (node_agent[2]))=2;
		
		// neighbors of node
		assert length(the_graph neighbors_of(node_agent[2]))=4;
		assert length(remove_duplicates(the_graph neighbors_of(node_agent[2])))=4;
		assert length(the_graph predecessors_of(node_agent[2]))=2;
		assert length(the_graph successors_of(node_agent[2]))=2;
	
		// Contains edges
		point node8 <- {50.0,50.0};
		assert the_graph contains_vertex(node_agent[2]);
		assert the_graph contains_edge(link(node_agent[2],node_agent[5])) = false;
		assert the_graph contains_edge(link(node_agent[2],node_agent[3]));	
		assert the_graph source_of(link(node_agent[2],node_agent[3]))=node_agent[2];
		assert the_graph target_of(link(node_agent[2],node_agent[3]))=node_agent[3];
		
	}
	
}
