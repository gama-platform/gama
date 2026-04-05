/**
* Name: 3D Graph
* Author: Arnaud Grignard
* Description: A dynamic 3D graph visualization where node positions and edge connections are updated each
*   simulation step based on agent locations. Sphere agents represent nodes; arcs drawn between adjacent
*   spheres represent edges. Two experiments are offered: one where sphere size scales with the node degree
*   (number of connections), showing hub-and-spoke structures; one simpler experiment with uniform sphere
*   sizes. Demonstrates GAMA's ability to render graph structures in 3D with dynamic topology updates.
* Tags: graph, 3d, skill, dynamic, visualization, degree, node, edge
*/
  

model graph3D

global {
	int number_of_agents min: 1 <- 200;
	int width_and_height_of_environment min: 100 <- 500;
	
	//Distance to know if a sphere is adjacent or not with an other
	int distance min: 1 <- 100;
	
	
	int degreeMax <- 1;
	geometry shape <- cube(width_and_height_of_environment);
	
	
	graph my_graph;
	init {
		
		//creation of the node agent ie the spheres with a random location in the environment
		create node_agent number: number_of_agents {
			location <- { rnd(width_and_height_of_environment), rnd(width_and_height_of_environment), rnd(width_and_height_of_environment) };
		}
		
		do degreeMax_computation();
		
		ask node_agent {
			do compute_degree();
		}
	}
	
	reflex updateDegreeMax {
		do degreeMax_computation();
	}

	action degreeMax_computation() {
		my_graph <- node_agent as_distance_graph(distance);
		degreeMax <- 1;
		ask node_agent {
			if ((my_graph) degree_of (self) > degreeMax) {
				degreeMax <- (my_graph) degree_of (self);
			}
		}
	}
}


species node_agent skills: [moving3D] {
	int degree;
	float radius;
	rgb color ;
	float speed <- 5.0;
	reflex move {
		//make the agent move randomly
		do wander();
		//compute the degree of the agent
		do compute_degree();
	}
	
	
	action compute_degree() {
		degree <- my_graph = nil ? 0 : (my_graph) degree_of (self);
		radius <- ((((degree + 1) ^ 1.4) / (degreeMax))) * 5;
		color <- hsb(0.66,degree / (degreeMax + 1), 0.5);
	}

    aspect base {
		draw sphere(10) color:#black;
	}
	
	aspect dynamic {
		draw sphere(radius) color: color;
	}

}

experiment Display type: gui {
	 
	parameter 'Number of Agents' var:number_of_agents category: 'Initialization';
	parameter 'Dimensions' var:width_and_height_of_environment category: 'Initialization';
	parameter 'distance ' var:distance;
	
	output {
		display WanderingSphere type: 3d { 
			camera 'default' location: {-528.0266,911.2309,549.1574} target: {572.0892,-62.0693,0.0};			
			species node_agent aspect: dynamic;
			graphics "edges" {
				//Creation of the edges of adjacence
				if (my_graph != nil) {
					loop eg over: my_graph.edges {
						geometry edge_geom <- geometry(eg);
						float val <- 255 * edge_geom.perimeter / distance; 
						draw line(edge_geom.points, 0.5)  color: rgb(val,val,val);
					}
				}
				
			}
		}
	}
}


experiment SimpleDisplay type: gui {
	output {
		display WanderingSphere type: 3d { 
			camera 'default' location: {-528.0266,911.2309,549.1574} target: {572.0892,-62.0693,0.0};			
			species node_agent aspect: base;
			graphics "edges" {
				if (my_graph != nil) {
					loop eg over: my_graph.edges {
						geometry edge_geom <- geometry(eg);
						float val <- 255 * edge_geom.perimeter / distance; 
						draw line(edge_geom.points) color:#black;
					}
				}
				
			}
		}
	}
}
