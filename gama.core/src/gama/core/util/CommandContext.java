/*******************************************************************************************************
 *
 * CommandContext.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.Cleaner;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.Platform;

import gama.annotations.action;
import gama.annotations.doc;
import gama.annotations.gaml_class;
import gama.annotations.getter;
import gama.annotations.setter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.object.IClass;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFolderFile;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.core.object.GamlObject;

/**
 * A persistent system shell used by the {@code command(string, context)} operator.
 */
@gaml_class (
		name = CommandContext.CONTEXT,
		concept = { IConcept.SYSTEM, IConcept.COMMUNICATION },
		doc = @doc ("A persistent system shell. Commands run with command(string, context) share its state (current directory, variables, functions...) and are recorded in its history."))
@vars ({ @variable (
		name = CommandContext.DIRECTORY,
		type = IType.DIRECTORY,
		doc = @doc ("The current directory of the shell. Defaults to the directory of the model. Assigning it changes the directory used by the next command.")),
		@variable (
				name = CommandContext.COMMANDS,
				type = IType.LIST,
				of = IType.STRING,
				constant = true,
				doc = @doc ("The commands run in this context, in order.")),
		@variable (
				name = CommandContext.RESULTS,
				type = IType.LIST,
				of = IType.STRING,
				constant = true,
				doc = @doc ("The outputs (standard output and error) of the commands run in this context, in order.")),
		@variable (
				name = CommandContext.RESULT,
				type = IType.STRING,
				constant = true,
				doc = @doc ("The output of the last command, or an empty string.")),
		@variable (
				name = CommandContext.STATUS,
				type = IType.INT,
				constant = true,
				doc = @doc ("The exit status of the last command (0 means success). Unlike the string forms of command, a failing command does not raise an error.")) })
public class CommandContext extends GamlObject {

	/** The GAML names. */
	public static final String CONTEXT = "context", DIRECTORY = "directory", COMMANDS = "commands",
			RESULTS = "results", RESULT = "result", STATUS = "status";

	/** Terminates shells of contexts that are no longer reachable. */
	private static final Cleaner CLEANER = Cleaner.create();

	/** Whether the platform uses cmd.exe. */
	private static final boolean WINDOWS = Platform.OS_WIN32.equals(Platform.getOS());

	/** The charset used to communicate with the shell. */
	private static final Charset CHARSET =
			Charset.forName(System.getProperty("native.encoding", Charset.defaultCharset().name()));

	/** The marker identifying the end of a command. */
	private final String marker = "__GAMA_CONTEXT_" + UUID.randomUUID().toString().replace("-", "") + "__";

	/** The history. */
	private final List<String> commands = new ArrayList<>(), results = new ArrayList<>();

	/** The last result. */
	private String result = "";

	/** The last status. */
	private int status;

	/** The absolute path of the current directory. */
	private String directory;

	/** Whether the directory has been assigned since the last command. */
	private boolean directoryChanged;

	/** The running shell, if any. */
	private Shell shell;

	/**
	 * Instantiates a new command context.
	 *
	 * @param scope
	 *            the scope
	 * @param clazz
	 *            the clazz
	 */
	public CommandContext(final IScope scope, final IClass clazz) {
		super(clazz);
	}

	/**
	 * Gets the directory.
	 *
	 * @param scope
	 *            the scope
	 * @return the directory
	 */
	@getter (
			value = DIRECTORY,
			initializer = true)
	public synchronized GamaFolderFile getDirectory(final IScope scope) {
		if (directory == null) {
			directory = new File(scope.getModel().getWorkingPath()).getAbsolutePath();
		}
		return new GamaFolderFile(scope, directory);
	}

	/**
	 * Sets the directory.
	 *
	 * @param scope
	 *            the scope
	 * @param folder
	 *            the folder
	 */
	@setter (DIRECTORY)
	public synchronized void setDirectory(final IScope scope, final GamaFolderFile folder) {
		if (folder == null) throw GamaRuntimeException.error("The directory of a context cannot be nil", scope);
		final File file = folder.getFile(scope);
		if (!file.isDirectory()) throw GamaRuntimeException.error(file.getAbsolutePath() + " is not a folder", scope);
		directory = file.getAbsolutePath();
		directoryChanged = true;
	}

	/**
	 * Gets the commands.
	 *
	 * @return the commands
	 */
	@getter (COMMANDS)
	public synchronized IList<String> getCommands() { return GamaListFactory.wrap(Types.STRING, new ArrayList<>(commands)); }

	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	@getter (RESULTS)
	public synchronized IList<String> getResults() { return GamaListFactory.wrap(Types.STRING, new ArrayList<>(results)); }

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	@getter (RESULT)
	public synchronized String getResult() { return result; }

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	@getter (STATUS)
	public synchronized int getStatus() { return status; }

	/**
	 * Closes the shell. Its state is lost, but the history and directory are kept and the next command starts a new
	 * shell.
	 *
	 * @param scope
	 *            the scope
	 * @return true if a shell was running
	 */
	@action (
			name = "close",
			doc = @doc ("Terminates the shell of this context and its subprocesses. The history and directory are kept; the next command starts a new shell. Returns true if a shell was running."))
	public synchronized Boolean close(final IScope scope) {
		if (shell == null) return false;
		shell.close();
		shell = null;
		return true;
	}

	/**
	 * Runs a command in this context. A trailing '&' runs it in the background, discarding its output.
	 *
	 * @param scope
	 *            the scope
	 * @param command
	 *            the command
	 * @return this context
	 */
	public synchronized CommandContext execute(final IScope scope, final String command) {
		if (command == null || command.isBlank()) return this;
		if (directory == null) { getDirectory(scope); }
		String text = command.trim();
		final boolean nonBlocking = text.endsWith("&") && !text.endsWith("&&");
		if (nonBlocking) { text = text.substring(0, text.length() - 1).stripTrailing(); }
		try {
			if (shell == null || !shell.process.isAlive()) {
				shell = new Shell(scope, directory);
				directoryChanged = false;
			}
			final StringBuilder script = new StringBuilder();
			if (directoryChanged) { script.append(changeDirectory()).append('\n'); }
			script.append(wrap(text, nonBlocking)).append('\n');
			script.append(WINDOWS ? "echo.\necho " + marker + " %ERRORLEVEL% %CD%\n"
					: "__gama_status=$?\nprintf '\\n%s %s %s\\n' '" + marker + "' \"$__gama_status\" \"$PWD\"\n");
			shell.input.write(script.toString());
			shell.input.flush();
			directoryChanged = false;
			read(nonBlocking);
		} catch (final IOException e) {
			if (shell != null) {
				shell.close();
				shell = null;
			}
			throw GamaRuntimeException.error("Error in console command. " + e.getMessage(), scope);
		}
		commands.add(command);
		results.add(result);
		return this;
	}

	/**
	 * Reads the output of the last command until the marker, updating the result, status and directory.
	 */
	private void read(final boolean nonBlocking) throws IOException {
		final StringBuilder output = new StringBuilder();
		String line;
		while ((line = shell.output.readLine()) != null) {
			if (WINDOWS && line.endsWith("\r")) { line = line.substring(0, line.length() - 1); }
			if (line.startsWith(marker + " ")) {
				final String[] parts = line.substring(marker.length() + 1).split(" ", 2);
				status = Integer.parseInt(parts[0].trim());
				if (parts.length > 1 && !parts[1].isBlank()) { directory = parts[1].trim(); }
				// Removes the line separator emitted before the marker
				if (!output.isEmpty()) { output.setLength(output.length() - 1); }
				result = nonBlocking ? "" : output.toString();
				return;
			}
			output.append(line).append('\n');
		}
		// The shell has exited (e.g. 'exit'): its state is lost
		try {
			status = shell.process.waitFor();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			status = -1;
		}
		shell.close();
		shell = null;
		result = output.toString();
	}

	/**
	 * Builds the shell instruction running a command without letting it consume the shell input.
	 */
	private static String wrap(final String command, final boolean nonBlocking) {
		if (WINDOWS) return nonBlocking ? "start \"\" /B cmd /C \"" + command + "\" > NUL 2>&1 < NUL"
				: "(" + command + "\n) < NUL";
		final String eval = "eval " + quote(command) + " < /dev/null";
		return nonBlocking ? eval + " > /dev/null 2>&1 &" : eval;
	}

	/**
	 * Builds the instruction changing the directory of the shell.
	 */
	private String changeDirectory() {
		return WINDOWS ? "cd /D \"" + directory + "\"" : "cd -- " + quote(directory);
	}

	/**
	 * Quotes a string for bash.
	 */
	private static String quote(final String s) {
		return "'" + s.replace("'", "'\\''") + "'";
	}

	/**
	 * A running shell process. Its process is terminated when the context becomes unreachable.
	 */
	private static final class Shell {

		/** The process. */
		final Process process;

		/** The input of the shell. */
		final Writer input;

		/** The merged standard output and error of the shell. */
		final BufferedReader output;

		/** The cleanable. */
		final Cleaner.Cleanable cleanable;

		/**
		 * Starts a shell.
		 */
		Shell(final IScope scope, final String directory) throws IOException {
			final ProcessBuilder builder = new ProcessBuilder(
					WINDOWS ? List.of("cmd.exe", "/Q", "/D", "/K") : List.of("/bin/bash"));
			builder.redirectErrorStream(true);
			builder.directory(new File(directory));
			process = builder.start();
			input = new OutputStreamWriter(process.getOutputStream(), CHARSET);
			output = new BufferedReader(new InputStreamReader(process.getInputStream(), CHARSET));
			final Process p = process;
			cleanable = CLEANER.register(this, () -> terminate(p));
			CommandProcessManager.register(scope.getAgent(), process);
		}

		/**
		 * Terminates the shell and its subprocesses.
		 */
		void close() {
			cleanable.clean();
		}

		/**
		 * Terminate.
		 */
		private static void terminate(final Process process) {
			final ProcessHandle handle = process.toHandle();
			handle.descendants().forEach(ProcessHandle::destroyForcibly);
			handle.destroyForcibly();
		}
	}

}
