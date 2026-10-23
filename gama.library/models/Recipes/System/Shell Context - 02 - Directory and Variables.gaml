/**
* Name: Shell Context - 02 - Directory and Variables
* Author: Alexis Drogoul
* Description: Second in a short series about persistent shells. Unlike the string forms of 'command', which start a
*   new shell for each call, a 'context' keeps its state between commands: a 'cd' changes the directory of the
*   following commands and a shell variable remains defined. The current directory is exposed as a 'folder' (the
*   'directory' attribute): it starts in the directory of the model, follows the 'cd' commands, and can also be
*   assigned from GAML.
* Tags: system, command, shell, context, directory, folder, file
*/
model ShellContextDirectoryAndVariables

global {
	bool windows <- gama.platform contains "Windows";

	init {
		context c <- context();
		write "Starting in: " + c.directory.path;

		// The working directory is kept between commands
		c <- command("cd ..", c);
		write "After 'cd ..': " + c.directory.path;

		// The directory is a folder, so its contents can be listed directly from GAML
		write "It contains: " + c.directory.contents;

		// Shell variables are also kept between commands
		c <- command(windows ? "set GREETING=hello" : "GREETING=hello", c);
		c <- command(windows ? "echo %GREETING% world" : "echo $GREETING world", c);
		write "Variable kept: " + trim(c.result);

		// Assigning the directory moves the shell before the next command, without losing its variables
		c.directory <- folder("includes");
		c <- command(windows ? "cd" : "pwd", c);
		write "Assigned directory: " + trim(c.result);
		c <- command(windows ? "echo %GREETING%" : "echo $GREETING", c);
		write "Variable still defined: " + trim(c.result);

		// A context created later is independent and starts again from the model directory
		context other <- command(windows ? "cd" : "pwd", context());
		write "Another context: " + trim(other.result);

		c.close();
		other.close();
	}
}

experiment "Run" type: gui;
