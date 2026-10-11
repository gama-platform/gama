/**
* Name: Mosquitos Adapter
* Author: Huynh Quang Nghi
* Description: Comodeling adapter (coupling class) for the Mosquitos model. This file is NOT intended to
*   be launched directly — it serves as the interface between the Flies and Mosquitos comodeling examples
*   and the Mosquitos sub-model, exposing mosquito agents to the parent comodel environment.
* Tags: comodel, adapter, mosquitos, coupling
*/
model mosquitos_coupling
import "../Models/Mosquitos.gaml"

global
{
}

experiment Generic type: gui
{
	list<Mosquito> get_mosquitos (){
		return list(Mosquito);
	}
	
	//if we redefine the output, i.e, a blank output, the displays in parent experiement dont show.
	output
	{
	}

}


