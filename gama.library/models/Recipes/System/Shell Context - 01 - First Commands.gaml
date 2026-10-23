/**
* Name: Shell Context - 01 - First Commands
* Author: Alexis Drogoul
* Description: First in a short series showing how to run system commands in a persistent shell. A 'context' is a
*   built-in object holding a live shell. The 'command(string, context)' operator runs a command in it and returns
*   the same context, so its result can be read immediately: 'result' is the output of the last command, 'status'
*   its exit status, and 'commands' and 'results' keep the history of everything that has been run.
* Tags: system, command, shell, context, class, object
*/
model ShellContextFirstCommands

global {
	// The commands differ slightly between cmd.exe (Windows) and bash (macOS and Linux)
	bool windows <- gama.platform contains "Windows";

	init {
		// A context is created with a zero-argument constructor. Its shell starts with the first command
		context c <- context();

		// command() returns the context, which can be reassigned, avoiding to store each output separately
		c <- command("echo Hello from the shell", c);
		write "Output: " + trim(c.result);
		write "Exit status: " + c.status;

		c <- command(windows ? "ver" : "uname -s", c);
		write "System: " + trim(c.result);

		// Calls can be nested: the inner command runs first
		c <- command("echo second", command("echo first", c));

		// The history records every command and its output, in order
		loop i from: 0 to: length(c.commands) - 1 {
			write c.commands[i] + " -> " + trim(c.results[i]);
		}

		// Terminates the shell (it would otherwise be terminated with the agent that started it)
		c.close();
	}
}

experiment "Run" type: gui;
