/**
* Name: Prey Predator Adapter
* Author: Huynh Quang Nghi
* Description: Comodeling adapter (coupling class) for the Prey Predator base model. This file is NOT
*   intended to be launched directly — it serves as the interface between comodel examples and the
*   Prey Predator sub-model, exposing its agent populations and behaviors to the parent comodel.
* Tags: comodel, adapter, predator_prey, coupling
*/
model prey_predator_coupling

import "Prey Predator.gaml"


global
{
}

experiment Simple type: gui
{
	geometry shape <- square(100);
	list<prey> get_prey()
	{
		return list(prey);
	}

	list<predator> get_predator()
	{
		return list(predator);
	}

	//if we redefine the output, i.e, a blank output, the displays in parent experiment don't show.
	output
	{
	}

}


