/**
* Name: EBMcomodel
* Experiment file for SIR_EBM. Used by "SIR Comodel.gaml"
* Author: Lucas Grosjean
* Tags: comodel, experiment
*/

experiment EBMcomodel model:'../Legacy_models/SIR_EBM.gaml' type:gui {
	int get_num_S()
	{
		return int(first(agent_with_SIR_dynamic).S);
	}

	int get_num_I()
	{
		return int(first(agent_with_SIR_dynamic).I);
	}

	int get_num_R()
	{
		return int(first(agent_with_SIR_dynamic).R);
	}

	action set_num_S_I_R (int numS, int numI, int numR)
	{
		self.set_num_S(numS);
		self.set_num_I(numI);
		self.set_num_R(numR);
	}

	action set_num_S (int num)
	{
		first(agent_with_SIR_dynamic).S <- float(num);
	}

	action set_num_I (int num)
	{
		first(agent_with_SIR_dynamic).I <- float(num);
	}

	action set_num_R (int num)
	{
		first(agent_with_SIR_dynamic).R <- float(num);
	}
}
