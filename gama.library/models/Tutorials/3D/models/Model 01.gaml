/**
* Name: 3D Tutorial - Model 01 - Basic 3D Model
* Author: Arnaud Grignard
* Description: First step of the 3D tutorial. Introduces the basic setup of a 3D GAMA simulation: creating
*   agents with random 3D positions, assigning them a simple sphere geometry, and displaying them in a 3D
*   OpenGL environment. This model is the starting point for learning how to build 3D simulations in GAMA.
* Tags: 3d, display, tutorial, opengl, cell, basic
*/

model Tuto3D

global {
  int nb_cells <- 100;	
  init { 
    create cell number: nb_cells { 
      location <- {rnd(100), rnd(100), rnd(100)};       
    } 
  }  
} 
  
species cell {                      
  aspect default {
    draw sphere(1) color: #blue;   
  }
}

experiment Tuto3D  type: gui {
  parameter "Initial number of cells: " var: nb_cells min: 1 max: 1000 category: "Cells" ;	
  output {
    display View1 type: 3d {
	camera 'default' location: {-89.9704,145.5689,125.2091} target: {117.2908,13.529,0.0};
      species cell;
    }
  }
}