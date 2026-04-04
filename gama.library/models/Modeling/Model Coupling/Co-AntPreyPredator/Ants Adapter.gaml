/**
* Name: Ants Adapter
* Author: Huynh Quang Nghi
* Description: Comodeling adapter (coupling class) for the Ant Foraging model. This file is NOT intended
*   to be launched directly — it serves as the interface between the Co-AntPreyPredator comodel and the
*   Ant Foraging sub-model, exposing ant agents and their foraging behavior to the parent comodel.
* Tags: comodel, adapter, ants, foraging, coupling
*/
model ants_coupling

import "../../../Toy Models/Ants (Foraging and Sorting)/models/Ant Foraging.gaml"

//this is the experiment that supposed to uses
experiment Base type: gui 
{
	list<ant> get_ants()
	{
		return list(ant);
	}

	list<ant_grid> get_ant_grid()
	{
		return list(ant_grid);
	}
	//if we redefine the output, i.e, a blank output, the displays in parent experiment don't show.
	output
	{
	}

} 