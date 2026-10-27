/**
* Name: Shell Context - 04 - One Shell per Agent
* Author: Alexis Drogoul
* Description: Last in a short series about persistent shells. Contexts are ordinary GAML values: they can be stored
*   in attributes, so that each agent has its own shell with its own state. Here, each worker builds a file in its
*   own folder, one line per cycle. The shells of the workers are terminated, with their subprocesses, when the
*   workers die.
* Tags: system, command, shell, context, agent, file, folder
*/
model ShellContextOneShellPerAgent

global {
	bool windows <- gama.platform contains "Windows";
	init {
		create worker number: 3;
	}

	reflex stop when: cycle = 5 {
		ask worker {
			write name + " wrote in " + shell.directory.path + ":\n" + trim(command(windows ? "type log.txt" : "cat log.txt", shell).result);
			do die();
		}
		do pause();
	}
}

species worker {
	context shell <- context();

	init {
		// Each worker moves its own shell to a dedicated folder. The other shells are not affected
		shell.directory <- new_folder("shells/" + name);
		shell <- command("echo started > log.txt", shell);
	}

	reflex work {
		shell <- command("echo cycle " + cycle + " >> log.txt", shell);
	}
}

experiment "Run" type: gui;
