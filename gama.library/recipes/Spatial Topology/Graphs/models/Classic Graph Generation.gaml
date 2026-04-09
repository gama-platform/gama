/**
* Name: Classic Graph Generation
* Author: Patrick Taillandier
* Description: Demonstrates the generation of four classic synthetic graph topologies in GAMA: scale-free
*   (Barabasi-Albert preferential attachment), small-world (Watts-Strogatz), complete (every node connected
*   to every other), and random (Erdos-Renyi). The graph type is selectable as a parameter. Each topology
*   produces characteristic structural properties (degree distribution, clustering, path lengths) that are
*   relevant for modeling social networks, epidemic spreading, or infrastructure resilience.
* Tags: graph, scale_free, small_world, complete, random, generation, network, topology
*/

model classicgraphgeneration

global {
	graph the_graph ;
	string graph_type <- "scale-free" among: ["small-world", "scale-free","complete", "random" ];
	int nb_nodes <- 30;
	int nb_edges <- 100;
	float p <- 0.2;
	int k <- 4;
	int m <- 4;
	
	init {
		switch graph_type {
			match "random" {
				the_graph <- generate_random_graph(nb_nodes,nb_edges, true, node_agent, edge_agent);
			}
			match "scale-free" {
				the_graph <- generate_barabasi_albert(int(nb_nodes/2), 5, nb_nodes, true, node_agent, edge_agent);	
			}
			match "small-world" {
				the_graph <- generate_watts_strogatz(nb_nodes, p, k, true,node_agent, edge_agent);	
			}
			match "complete" {
				the_graph <- generate_complete_graph(nb_nodes, true, node_agent, edge_agent);
			}
				
		}
		write the_graph;
		write "Edges : "+length(the_graph.edges);
		write "Nodes : "+length(the_graph.vertices);
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
	}
}

experiment loadgraph type: gui {
	parameter "Graph type" var: graph_type;
	parameter "Number of nodes" var: nb_nodes min: 10 ;
	parameter "Probability to rewire an edge (beta)" var: p min: 0.0 max: 1.0 category: "small-world";
	parameter "Base degree of each node. k must be even" var: k min: 2 max: 10 category: "small-world";
	parameter "Number of edges added per novel node" var: m min: 1 max: 10 category: "scale-free";
	
	output {
		display map type: 2d{
			species edge_agent ;
			species node_agent ;
		}
	}
}
