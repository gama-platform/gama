/**
* Name: SIR Comodel Switch
* Author: Huynh Quang Nghi
* Description: A comodel that implements the hybrid ABM/EBM SIR switching strategy using two legacy sub-models.
*   When the population density is high, the Equation-Based Model (EBM / ODE) is used for efficiency; when
*   density drops below a threshold, it switches to the Agent-Based Model (ABM) for accuracy. Two adapter
*   models (SIR_ABM_coupling and SIR_EBM_coupling) provide the comodel interface to the respective legacy
*   models. This example demonstrates dynamic model selection and legacy model reuse in a comodeling context.
* Tags: comodel, SIR, math, equation, epidemiology, ABM, EBM, hybrid, switch, legacy
*/
model Comodel_SIR_Switch

import "Legacy_models/EBM Adapter.gaml" as SIR_1
import "Legacy_models/ABM Adapter.gaml" as SIR_2

global
{
	geometry shape <- envelope(square(100));
	int switch_threshold <- 120; // threshold for switching models
	int threshold_to_IBM <- 220; // threshold under which the model swith to IBM
	int threshold_to_Maths <- 20;
	init
	{
		create SIR_1."Adapter";
		create SIR_2."AdapterAbm";
		create Switch;
	} 

}

species Switch
{
	int S <- 495;
	int I <- 50;
	int R <- 0;
	reflex request_from_micro_model
	{
		//if the size of S population and I population are bigger than a threshold, use the EBM
		if (S > threshold_to_Maths and I > threshold_to_Maths)
		{
				if(first(SIR_1."Adapter")!=nil){					
					unknown call;
					call <- first(SIR_1."Adapter").set_num_S_I_R(S, I, R);
					ask first(SIR_1."Adapter").simulation
					{
						loop times: 1
						{
							do _step_();
						}
	
					}
	
					S <- first(SIR_1."Adapter").get_num_S();
					I <- first(SIR_1."Adapter").get_num_I();
					R <- first(SIR_1."Adapter").get_num_R();
				}
		}
		else
		//if the size of S population or  I population are smaller  than a threshold, use the ABM
		if (I < threshold_to_IBM or S < threshold_to_IBM)
		{
				unknown call;
				call <- first(SIR_2."AdapterAbm").set_num_S_I_R(S, I, R);
				ask first(SIR_2."AdapterAbm").simulation
				{
					loop times: 10
					{
						do _step_();
					}

				}

				S <- first(SIR_2."AdapterAbm").get_num_S();
				I <- first(SIR_2."AdapterAbm").get_num_I();
				R <- first(SIR_2."AdapterAbm").get_num_R();
		}

	}

	aspect base
	{
		draw square(100);
	}

}

experiment Simple_exp type: gui
{
	output
	{
	 	layout horizontal([0::5000,vertical([1::5000,2::5000])::5000]) tabs:true editors: false;
		display "Switch_SIR chart" type: 2d 
		{
			chart "SIR_agent" type: series background: # white
			{
				data 'S' value: first(Switch).S color: # green;
				data 'I' value: first(Switch).I color: # red;
				data 'R' value: first(Switch).R color: # blue;
			}

		}
		display "EBM Disp"  type: 2d {			
			chart "SIR_agent" type: series background: #white {
				data 'S' value: first(first(SIR_1."Adapter").simulation.agent_with_SIR_dynamic).S color: #green ;				
				data 'I' value: first(first(SIR_1."Adapter").simulation.agent_with_SIR_dynamic).I color: #red ;
				data 'R' value: first(first(SIR_1."Adapter").simulation.agent_with_SIR_dynamic).R color: #blue ;
			}
		}
		display "ABM Disp" type:2d{			
			agents "Host" value:first(SIR_2."AdapterAbm").simulation.Host aspect:base;
		}

	}

}
