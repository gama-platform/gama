/**
* Name: CommandContextTest
* Author: Alexis Drogoul
* Description: Tests the 'context' built-in class and the 'command(string, context)' operator, which run commands in
*   a persistent shell whose state (directory, variables) is kept between commands.
* Tags: core, system, command, shell, context, test
*/

model CommandContextTestModel

global {
	bool windows <- gama.platform contains "Windows";

	string echo_variable(string variable) {
		return "echo " + (windows ? "%" + variable + "%" : "$" + variable);
	}

	string set_variable(string variable, string value) {
		return (windows ? "set " : "") + variable + "=" + value;
	}

	// Compares folders by their contents, as their paths may differ in their form (relative, canonical, symbolic links...)
	bool same_folder(directory f1, directory f2) {
		return (f1.contents sort_by each) = (f2.contents sort_by each);
	}

	string print_directory() {
		return windows ? "cd" : "pwd";
	}
}

experiment CommandContextTest type: test autorun: true {

	test "A new context is empty and starts in the model directory" {
		context c <- context();
		assert c.result = "";
		assert c.status = 0;
		assert empty(c.commands);
		assert empty(c.results);
		assert world.same_folder(c.directory, folder("."));
		assert c.directory.is_folder;
	}

	test "command returns the same context" {
		context c <- context();
		context returned <- command("echo test", c);
		assert returned = c;
		c.close();
	}

	test "The output and the history are recorded" {
		context c <- command("echo second", command("echo first", context()));
		assert trim(c.result) = "second";
		assert c.status = 0;
		assert c.commands = ["echo first", "echo second"];
		assert length(c.results) = 2;
		assert trim(c.results[0]) = "first";
		assert trim(c.results[1]) = "second";
		c.close();
	}

	test "Shell variables persist between commands" {
		context c <- command(world.set_variable("GAMA_CONTEXT_VALUE", "42"), context());
		c <- command(world.echo_variable("GAMA_CONTEXT_VALUE"), c);
		assert trim(c.result) = "42";
		c.close();
	}

	test "Changing directory in the shell updates the directory attribute" {
		context c <- command("cd ..", context());
		assert world.same_folder(c.directory, folder(".."));
		c <- command(world.print_directory(), c);
		assert world.same_folder(folder(trim(c.result)), folder(".."));
		c.close();
	}

	test "Assigning the directory moves the live shell without losing its state" {
		context c <- command(world.set_variable("GAMA_CONTEXT_STATE", "kept"), context());
		c.directory <- folder("..");
		c <- command(world.print_directory(), c);
		assert world.same_folder(folder(trim(c.result)), folder(".."));
		c <- command(world.echo_variable("GAMA_CONTEXT_STATE"), c);
		assert trim(c.result) = "kept";
		c.close();
	}

	test "The directory cannot be nil or a missing folder" {
		context c <- context();
		bool nil_failed <- false;
		try { c.directory <- nil; } catch { nil_failed <- true; }
		assert nil_failed;
		bool missing_failed <- false;
		try { c.directory <- folder("this_folder_does_not_exist"); } catch { missing_failed <- true; }
		assert missing_failed;
	}

	test "A failing command sets the status without raising an error" {
		context c <- context();
		assert !is_error(command("exit_code_test_command_that_does_not_exist", c));
		assert c.status != 0;
		c <- command("echo recovered", c);
		assert c.status = 0;
		assert trim(c.result) = "recovered";
		c.close();
	}

	test "A background command returns immediately with an empty result" {
		context c <- context();
		float start <- gama.machine_time;
		c <- command((windows ? "ping -n 3 127.0.0.1" : "sleep 2") + " &", c);
		assert gama.machine_time - start < 1000;
		assert c.result = "";
		assert last(c.commands) contains "&";
		c.close();
	}

	test "The exit status of an exiting shell is kept and a fresh shell is restarted" {
		context c <- command(world.set_variable("GAMA_CONTEXT_LOST", "value"), context());
		c <- command("cd ..", c);
		c <- command("exit 3", c);
		assert c.status = 3;
		c <- command(world.echo_variable("GAMA_CONTEXT_LOST"), c);
		assert !(c.result contains "value");
		assert world.same_folder(c.directory, folder(".."));
		c.close();
	}

	test "close terminates the shell but keeps the history" {
		context c <- command("echo closed", context());
		assert c.close();
		assert !c.close();
		assert c.commands = ["echo closed"];
		c <- command("echo reopened", c);
		assert trim(c.result) = "reopened";
		assert length(c.commands) = 2;
		c.close();
	}

	test "A nil context raises an error" {
		context c <- nil;
		assert is_error(command("echo test", c));
	}

	test "Contexts are independent" {
		context first <- command(world.set_variable("GAMA_CONTEXT_OWNER", "first"), context());
		context second <- command(world.set_variable("GAMA_CONTEXT_OWNER", "second"), context());
		first <- command(world.echo_variable("GAMA_CONTEXT_OWNER"), first);
		second <- command(world.echo_variable("GAMA_CONTEXT_OWNER"), second);
		assert trim(first.result) = "first";
		assert trim(second.result) = "second";
		first.close();
		second.close();
	}

	test "The string forms of command are unchanged" {
		assert trim(command("echo legacy")) = "legacy";
		assert is_error(command("exit 2"));
	}
}
