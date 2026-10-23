/**
* Name: Shell Context - 03 - Errors and Background Commands
* Author: Alexis Drogoul
* Description: Third in a short series about persistent shells. With a 'context', a failing command does not raise a
*   GAML error: its exit status is available in 'status' (0 meaning success) and its error output in 'result'. A
*   command ending with ' &' runs in the background without blocking the model, its output being discarded. If the
*   shell itself ends (e.g. with 'exit') or is closed, the next command starts a new shell in the same directory,
*   but its previous variables are lost.
* Tags: system, command, shell, context, error, background, process
*/
model ShellContextErrorsAndBackground

global {
	bool windows <- gama.platform contains "Windows";

	init {
		context c <- context();

		// Errors are reported by the status and the output, not by an exception
		c <- command(windows ? "dir this_file_does_not_exist" : "ls this_file_does_not_exist", c);
		if (c.status != 0) {
			write "The command failed with status " + c.status + ": " + trim(c.result);
		}

		// The shell is still usable after a failure
		c <- command("echo still alive", c);
		write trim(c.result) + " (status " + c.status + ")";

		// A trailing '&' starts the command in the background: the call returns at once with an empty result
		float start <- gama.machine_time;
		c <- command((windows ? "ping -n 3 127.0.0.1" : "sleep 2") + " &", c);
		write "Background command started in " + (gama.machine_time - start) + " ms, result: '" + c.result + "'";

		// Ending the shell keeps the history and the directory, but the shell state is lost
		c <- command(windows ? "set NAME=GAMA" : "NAME=GAMA", c);
		c <- command("exit 4", c);
		write "The shell exited with status " + c.status;
		c <- command(windows ? "echo [%NAME%]" : "echo [$NAME]", c);
		write "A new shell has been started: " + trim(c.result);

		// 'close' terminates the shell and the processes it started. It returns false if no shell was running
		write "Closed: " + c.close() + ", closed again: " + c.close();
		write "History of " + length(c.commands) + " commands: " + c.commands;
	}
}

experiment "Run" type: gui;
