/**
* Name: Boids Adapter
* Author: Huynh Quang Nghi
* Description: Comodeling adapter (coupling class) for the Boids 3D Motion model. This file is NOT intended
*   to be launched directly — it serves as the interface between the City Boids comodel and the Boids
*   sub-model, exposing the Boids agents and their behavior to the parent comodel environment.
* Tags: comodel, adapter, boids, coupling
*/
model boids_adapter

import "../../../../Toy Models/Boids/models/Boids 3D Motion.gaml"


experiment B_Adapter title:"Adapter of Boids" type:gui  {
	
	list<boids_goal> get_boids_goal(){
		return list(boids_goal);
	}
	
	list<boids> get_boids(){
		return list(boids);
	}
	
	output{
	}
}