/**
* Name: SIR Comodel Switch
* Author: Huynh Quang Nghi, Lucas Grosjean
* Description: A comodel that implements the hybrid ABM/EBM SIR switching strategy using two legacy sub-models.
*   When the population density is high, the Equation-Based Model (EBM / ODE) is used for efficiency; when
*   density drops below a threshold, it switches to the Agent-Based Model (ABM) for accuracy. Two adapter
*   models (SIR_ABM_coupling and SIR_EBM_coupling) provide the comodel interface to the respective legacy
*   models. This example demonstrates dynamic model selection and legacy model reuse in a comodeling context.
* Tags: comodel, SIR, math, equation, epidemiology, ABM, EBM, hybrid
*/
model Comodel_SIR_Switch

import "Experiment_comodel/SIR_ebm_comodel.experiment" as SIR_EBM
import "Experiment_comodel/SIR_abm_comodel.experiment" as SIR_ABM

global
{
	geometry shape <- envelope(square(100));
	
	int switch_threshold <- 120; // threshold for switching models
	int threshold_to_IBM <- 220; // threshold under which the model swith to IBM
	int threshold_to_Maths <- 20;
	
	int S <- 495;
	int I <- 50;
	int R <- 0;
	
	init
	{
		create SIR_EBM.EBMcomodel; 
		create SIR_ABM.ABMcomodel;
	} 
	reflex request_from_micro_model
	{
		//if the size of S population and I population are bigger than a threshold, use the EBM
		if (S > threshold_to_Maths and I > threshold_to_Maths)
		{			
			first(SIR_EBM.EBMcomodel).set_num_S_I_R(S, I, R);
			ask SIR_EBM.EBMcomodel[0].simulation
			{
				self._step_();
			}

			S <- SIR_EBM.EBMcomodel[0].get_num_S();
			I <- SIR_EBM.EBMcomodel[0].get_num_I();
			R <- SIR_EBM.EBMcomodel[0].get_num_R();
				
		}
		else
		//if the size of S population or  I population are smaller  than a threshold, use the ABM
		if (I < threshold_to_IBM or S < threshold_to_IBM)
		{
				first(SIR_ABM.ABMcomodel).set_num_S_I_R(S, I, R);
				ask first(SIR_ABM.ABMcomodel).simulation
				{
					loop times: 10
					{
						self._step_();
					}
				}
				S <- first(SIR_ABM.ABMcomodel).get_num_S();
				I <- first(SIR_ABM.ABMcomodel).get_num_I();
				R <- first(SIR_ABM.ABMcomodel).get_num_R();
		}
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
				data 'S' value: S color: # green;
				data 'I' value: I color: # red;
				data 'R' value: R color: # blue;
			}

		}
		display "EBM Disp"  type: 2d {			
			chart "SIR_agent" type: series background: #white {
				data 'S' value: first(first(SIR_EBM.EBMcomodel).simulation.agent_with_SIR_dynamic).S color: #green ;				
				data 'I' value: first(first(SIR_EBM.EBMcomodel).simulation.agent_with_SIR_dynamic).I color: #red ;
				data 'R' value: first(first(SIR_EBM.EBMcomodel).simulation.agent_with_SIR_dynamic).R color: #blue ;
			}
		}
		display "ABM Disp" type:2d{			
			agents "Host" value:first(SIR_ABM.ABMcomodel).simulation.Host aspect:base;
		}
	}
}
