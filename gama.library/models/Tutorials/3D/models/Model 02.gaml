/**
* Name: 3D Tutorial - Model 02 - Moving Cells
* Author: Arnaud Grignard
* Description: Second step of the 3D tutorial. Adds movement to the 3D agents: cells move randomly within a
*   cubic environment. Introduces the concept of a cubic bounding box for the world geometry and demonstrates
*   random 3D movement using the 'moving' skill. Agents bounce off the walls to stay within bounds.
* Tags: grid, agent_movement, 3d, tutorial, skill, moving, cube
*/
model Tuto3D   

global {
	int nb_cells <- 100;
	int environment_size <- 100;
	geometry shape <- cube(environment_size);

	init {
		create cell number: nb_cells {
			location <- {rnd(environment_size), rnd(environment_size), rnd(environment_size)};
		}
	}
}

species cell skills: [moving3D] {
	reflex move {
		do move;
	}

	aspect default {
		draw sphere(environment_size * 0.01) color: #blue;
	}
}

experiment Tuto3D type: gui {
	parameter "Initial number of cells: " var: nb_cells min: 1 max: 1000 category: "Cells";
	output {
		display View1 type: 3d {
			camera 'default' location: {-89.9704,145.5689,125.2091} target: {117.2908,13.529,0.0};
			graphics "env" {
				draw cube(environment_size) color: #black wireframe: true;
			}
			species cell;
		}
	}
}