/**
* Name: citycomodel
* Experiment file for city model. Used by "Urbanization And Traffic Comodel.gaml"
* Author: Lucas Grosjean, HUYNH Quang Nghi
* Tags: comodel, experiment
*/

experiment citycomodel model: '../../../Procedural City/models/Procedural City.gaml' mode:gui actions_context: simulation {
	Building get_building_at (geometry p)
	{
		return Building closest_to p;
	}
	
	list<Building> get_building (){
		return list(Building);
	}
}
