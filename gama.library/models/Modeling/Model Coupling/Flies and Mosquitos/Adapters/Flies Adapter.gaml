/**
* Name: Flies Adapter
* Author: Huynh Quang Nghi
* Description: Comodeling adapter (coupling class) for the Flies model. This file is NOT intended to be
*   launched directly — it serves as the interface between the Flies and Mosquitos comodeling examples and
*   the Flies sub-model, exposing fly agents to the parent comodel environment.
* Tags: comodel, adapter, flies, coupling
*/
model flies_coupling
import "../Models/Flies.gaml"

global
{
}

experiment Simple type: gui
{
	list<Fly> get_flies(){
		return list(Fly);
	}
	
	//if we redefine the output, i.e, a blank output, the displays in parent experiement dont show.
	output
	{
	}

}


