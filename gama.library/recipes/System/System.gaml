/**
* Name: System
* Author: Alexis Drogoul
* Description: Demonstrates how to access system-level properties of the GAMA platform through the built-in 'gama'
*   pseudo-agent. Available properties include: the GAMA version string, the list of installed plugins, the current
*   machine time (milliseconds since epoch), the current workspace path, and the available memory. These can be used
*   to ensure minimum platform requirements are met (e.g., minimum GAMA version, minimum available memory) or to
*   log diagnostic information about the execution environment.
* Tags: system, platform, gama, version, plugin, memory, machine_time, workspace
*/
model System


global
{
	init
	{
		// The version of the current GAMA installation
		write sample(gama.version);
		// The list of plugins loaded in the current GAMA installation
		write gama.plugins;
		// The current time since epoch day (i.e. UNIX time)
		write sample(gama.machine_time) + " milliseconds since epoch day";
		// The current path to the workspace
		write gama.workspace_path;
		// The memory still available to be allocated to GAMA
		write sample(gama.free_memory) + " bytes" ;
		// The maximum amount of memory GAMA can be allocated
		write sample(gama.max_memory) + " bytes";
		// gama.info could be used to get a summary of the current computer configuration (hardware and software)
		write gama.info;
		// gama.platform could be used to get the platform on which GAMA is currently running
		write gama.platform;
	}

}

experiment Run;