/**
* Name: hydrocomodel
* Experiment file for hydro model. Used by "Flood Evacuation Comodel.gaml"
* Author: Lucas Grosjean
* Tags: comodel, experiment
*/


experiment hydrocomodel model:'../../../Flood Simulation/models/Hydrological Model.gaml' type:gui {
	point newSize <- { 0.07, 0.07 };
	cell get_cell_at (agent p)
	{	 
		using topology(cell) {
			return first(cell overlapping p);
		} 
	}

	list<cell> get_cell()
	{
		return list(cell) where (each.grid_value > 8.0);
	}

	list<buildings> get_buildings()
	{
		return list(buildings);
	}

	list<dyke> get_dyke()
	{
		return list(dyke);
	}
}
