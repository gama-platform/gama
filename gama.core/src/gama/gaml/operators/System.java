/*******************************************************************************************************
 *
 * System.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.eclipse.core.runtime.Platform;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.IValue;
import gama.core.common.util.FileUtils;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.InputParameter;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IObject;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaFont;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 10 d�c. 2010
 *
 * @todo Description
 *
 */
public class System {

	/**
	 * Op dead.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @return the boolean
	 */

	/**
	 * Op dead.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @return the boolean
	 */
	@operator (
			value = "dead",
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.SPECIES })
	@doc (
			value = "true if the agent is dead (or null), false otherwise.",
			examples = @example (
					value = "dead(agent_A)",
					equals = "true or false",
					isExecutable = false))
	@test ("dead(simulation) = false")
	public static Boolean opDead(final IScope scope, final IAgent a) {
		return a == null || a.dead();
	}

	/**
	 * Checks if is error.
	 *
	 * @param scope
	 *            the scope
	 * @param expr
	 *            the expr
	 * @return the boolean
	 */
	@operator (
			value = "is_error",
			can_be_const = true,
			concept = IConcept.TEST)
	@doc ("Returns whether or not the argument raises an error when evaluated")
	@test ("is_error(1.0 = 1) = false")
	public static Boolean is_error(final IScope scope, final IExpression expr) {
		try {
			expr.value(scope);
		} catch (final GamaRuntimeException e) {
			return !e.isWarning();
		} catch (final Exception e1) {}
		return false;
	}

	/**
	 * Checks if is warning.
	 *
	 * @param scope
	 *            the scope
	 * @param expr
	 *            the expr
	 * @return the boolean
	 */
	@operator (
			value = "is_warning",
			can_be_const = true,
			concept = IConcept.TEST)
	@doc ("Returns whether or not the argument raises a warning when evaluated")
	@test ("is_warning(1.0 = 1) = false")
	public static Boolean is_warning(final IScope scope, final IExpression expr) {
		try {
			expr.value(scope);
		} catch (final GamaRuntimeException e) {
			return e.isWarning();
		} catch (final Exception e1) {}
		return false;
	}

	/**
	 * Checks if the url is reachable.
	 *
	 * @param scope
	 *            the scope
	 * @param expr
	 *            the expr
	 * @return the boolean
	 */

	/**
	 * Checks if is reachable.
	 *
	 * @param scope
	 *            the scope
	 * @param address
	 *            the address
	 * @param openPort
	 *            the open port
	 * @param timeout
	 *            the timeout
	 * @return the boolean
	 */
	@operator (
			value = "is_reachable",
			can_be_const = true,
			concept = IConcept.TEST)
	@doc (
			value = "Returns whether or not the given web address is reachable or not before a time_out time in milliseconds",
			examples = { @example (
					value = "write sample(is_reachable(\"www.google.com\", 200));",
					isExecutable = false) })
	@no_test
	public static Boolean is_reachable(final IScope scope, final String address, final int openPort,
			final int timeout) {
		// Any Open port on other machine
		// openPort = 22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
		try (Socket soc = new Socket()) {
			soc.connect(new InetSocketAddress(address, openPort), timeout);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	/**
	 * Play sound.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the boolean
	 */

	/**
	 * Play sound.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @return the boolean
	 */
	@operator (
			value = "play_sound",
			can_be_const = true,
			concept = IConcept.SOUND)
	@doc (
			value = "Play a wave file",
			examples = { @example (
					value = "bool sound_ok <- play_sound('beep.wav');",
					isExecutable = false) })
	@no_test
	public static Boolean playSound(final IScope scope, final String source) {
		try {
			final String soundFilePath = FileUtils.constructAbsoluteFilePath(scope, source, true);
			File f = new File(soundFilePath);
			if (!f.exists()) return false;
			Clip clip = AudioSystem.getClip();
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(f);
			clip.open(inputStream);
			clip.start();

		} catch (Exception e) {
			throw GamaRuntimeException.error(e.toString(), scope);
		}
		return true;
	}

	/**
	 * Checks if the url is reachable.
	 *
	 * @param scope
	 *            the scope
	 * @param expr
	 *            the expr
	 * @return the boolean
	 */

	/**
	 * Checks if is reachable.
	 *
	 * @param scope
	 *            the scope
	 * @param address
	 *            the address
	 * @param timeout
	 *            the timeout
	 * @return the boolean
	 */
	@operator (
			value = "is_reachable",
			concept = IConcept.TEST)
	@doc (
			value = "Returns whether or not the given web address is reachable or not before a time_out time in milliseconds",
			examples = { @example (
					value = "write sample(is_reachable(\"www.google.com\", 200));",
					isExecutable = false) })
	@no_test
	public static Boolean is_reachable(final IScope scope, final String address, final int timeout) {
		return is_reachable(scope, address, 80, timeout);
	}

	/**
	 * Console.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @return the string
	 */

	/**
	 * Console.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @return the string
	 */
	@operator (
			value = "command",
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.COMMUNICATION })
	@doc (
			value = "command allows GAMA to issue a system command using the system terminal or shell and to receive a string containing the outcome of the command or script executed. By default, commands are blocking the agent calling them, unless the sequence ' &' is used at the end. In this case, the result of the operator is an empty string. The basic form with only one string in argument uses the directory of the model and does not set any environment variables. Two other forms (with a directory and a map<string, string> of environment variables) are available.",
			masterDoc = true)
	@no_test
	public static String console(final IScope scope, final String s) {
		return console(scope, s, scope.getSimulation().getExperiment().getWorkingPath());
	}

	/**
	 * Console.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param directory
	 *            the directory
	 * @return the string
	 */

	/**
	 * Console.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param directory
	 *            the directory
	 * @return the string
	 */
	@operator (
			value = "command",
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.COMMUNICATION })
	@doc ("command allows GAMA to issue a system command using the system terminal or shell and to receive a string containing the outcome of the command or script executed. By default, commands are blocking the agent calling them, unless the sequence ' &' is used at the end. In this case, the result of the operator is an empty string. The basic form with only one string in argument uses the directory of the model and does not set any environment variables. Two other forms (with a directory and a map<string, string> of environment variables) are available.")
	@no_test
	public static String console(final IScope scope, final String s, final String directory) {
		return console(scope, s, directory, GamaMapFactory.create());
	}

	/**
	 * Console.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param directory
	 *            the directory
	 * @param environment
	 *            the environment
	 * @return the string
	 */

	/**
	 * Console.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param directory
	 *            the directory
	 * @param environment
	 *            the environment
	 * @return the string
	 */
	@operator (
			value = "command",
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.COMMUNICATION })
	@doc ("command allows GAMA to issue a system command using the system terminal or shell and to receive a string containing the outcome of the command or script executed. By default, commands are blocking the agent calling them, unless the sequence ' &' is used at the end. In this case, the result of the operator is an empty string. A map<string, string> containing environment variables values can be passed, replacing, only for this command, the values of existing variables.")
	@no_test
	public static String console(final IScope scope, final String s, final String directory,
			final IMap<String, String> environment) {
		if (s == null || s.isEmpty()) return "";
		final StringBuilder output = new StringBuilder();
		final List<String> commands = new ArrayList<>();
		commands.add(Platform.OS_WIN32.equals(Platform.getOS()) ? "cmd.exe" : "/bin/bash");
		commands.add(Platform.OS_WIN32.equals(Platform.getOS()) ? "/C" : "-c");
		commands.add(s.trim());
		// commands.addAll(Arrays.asList(s.split(" ")));
		final boolean nonBlocking = commands.get(commands.size() - 1).endsWith("&");
		if (nonBlocking) {
			// commands.(commands.size() - 1);
		}
		final ProcessBuilder b = new ProcessBuilder(commands);
		b.redirectErrorStream(true);
		b.directory(new File(directory));
		b.environment().putAll(environment);
		try {
			final Process p = b.start();
			if (nonBlocking) return "";
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				final int returnValue = p.waitFor();
				String line = "";
				while ((line = reader.readLine()) != null) { output.append(line + Strings.LN); }

				if (returnValue != 0)
					throw GamaRuntimeException.error("Error in console command." + output.toString(), scope);
			}
		} catch (final IOException | InterruptedException e) {
			throw GamaRuntimeException.error("Error in console command. " + e.getMessage(), scope);
		}
		return output.toString();

	}

	/**
	 * Op get value.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param s
	 *            the s
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Op get value.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param s
	 *            the s
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword._DOT, IKeyword.OF },
			type = ITypeProvider.TYPE_AT_INDEX + 2,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			index_type = ITypeProvider.KEY_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.ATTRIBUTE })
	@doc (
			value = "It has two different uses: it can be the dot product between 2 matrices or return an evaluation of the expression (right-hand operand) in the scope the given agent.",
			masterDoc = true,
			special_cases = "if the agent is nil or dead, throws an exception",
			usages = @usage (
					value = "if the left operand is an agent, it evaluates of the expression (right-hand operand) in the scope the given agent",
					examples = { @example (
							value = "agent1.location",
							equals = "the location of the agent agent1",
							isExecutable = false),
					// @example (value = "map(nil).keys", raises = "exception", isTestOnly = false)
					}))
	@no_test
	public static Object opGetValue(final IScope scope, final IAgent a, final IExpression s)
			throws GamaRuntimeException {
		if (a == null) {
			if (!scope.interrupted()) throw GamaRuntimeException
					.warning("Cannot evaluate " + s.serializeToGaml(false) + " as the target agent is nil", scope);
			return null;
		}
		if (a.dead()) {
			// scope.getGui().debug("System.opGetValue");
			if (!scope.interrupted()) // scope.getGui().debug("System.opGetValue error");
				throw GamaRuntimeException
						.warning("Cannot evaluate " + s.serializeToGaml(false) + " as the target agent is dead", scope);
			return null;
		}
		return scope.evaluate(s, a).getValue();
	}

	/**
	 * Op get value.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param s
	 *            the s
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = { IKeyword._DOT, IKeyword.OF },
			type = ITypeProvider.TYPE_AT_INDEX + 2,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			index_type = ITypeProvider.KEY_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.ATTRIBUTE })
	@doc (
			value = "It has two different uses: it can be the dot product between 2 matrices or return an evaluation of the expression (right-hand operand) in the scope the given agent.",
			masterDoc = true,
			special_cases = "if the agent is nil or dead, throws an exception",
			usages = @usage (
					value = "if the left operand is an agent, it evaluates of the expression (right-hand operand) in the scope the given agent",
					examples = { @example (
							value = "agent1.location",
							equals = "the location of the agent agent1",
							isExecutable = false),
					// @example (value = "map(nil).keys", raises = "exception", isTestOnly = false)
					}))
	@no_test
	public static Object opGetValue(final IScope scope, final IObject a, final IExpression s)
			throws GamaRuntimeException {
		if (a == null) {
			if (!scope.interrupted()) throw GamaRuntimeException
					.warning("Cannot evaluate " + s.serializeToGaml(false) + " as the target object is nil", scope);
			return null;
		}
		return scope.evaluate(s, a).getValue();
	}

	/**
	 * Op copy.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "copy",
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM })
	@doc (
			value = "returns a copy of the operand.")
	@no_test
	public static Object opCopy(final IScope scope, final Object o) throws GamaRuntimeException {
		if (o instanceof IValue) return ((IValue) o).copy(scope);
		return o;
	}

	/**
	 * User input dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param parameters
	 *            the parameters
	 * @return the i map
	 */
	@SuppressWarnings ("unchecked")
	@operator (
			value = IKeyword.USER_INPUT_DIALOG,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = {})
	@doc (
			value = "Asks the user for some values and returns a map containing these values. Takes a string and a list of calls to the `enter()` or `choose()` operators as arguments. The string is used to specify the message of the dialog box. The list is to specify the parameters the user can enter",
			examples = {
					@example ("map<string,unknown> values2 <- user_input_dialog('Enter number of agents and locations',[enter('Number',100), enter('Location',point, {10, 10})]);"),
					@example (
							value = "create bug number: int(values2 at \"Number\") with: [location:: (point(values2 at \"Location\"))];",
							isExecutable = false) })
	@no_test
	public static IMap<String, Object> userInputDialog(final IScope scope, final String title, final IList parameters) {
		parameters.removeIf(p -> !(p instanceof IParameter));
		return userInputDialog(scope, title, parameters, null, null);
	}

	/**
	 * User input dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param parameters
	 *            the parameters
	 * @return the i map
	 */
	@operator (
			value = IKeyword.USER_INPUT_DIALOG,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = {})
	@doc (
			value = "Asks the user for some values and returns a map containing these values. Takes a string and a list of calls to the `enter()` or `choose()` operators as arguments. The string is used to specify the message of the dialog box. The list is to specify the parameters the user can enter",
			examples = {
					@example ("map<string,unknown> values_no_title <- user_input_dialog([enter('Number',100), enter('Location',point, {10, 10})]);"),
					@example (
							value = "create bug number: int(values2 at \"Number\") with: [location:: (point(values2 at \"Location\"))];",
							isExecutable = false) })
	@no_test
	public static IMap<String, Object> userInputDialog(final IScope scope, final IList parameters) {
		final IAgent agent = scope.getAgent();
		return userInputDialog(scope, agent.getSpeciesName() + " #" + agent.getIndex() + " request", parameters);
	}

	/**
	 * User input dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param parameters
	 *            the parameters
	 * @param font
	 *            the font
	 * @return the i map
	 */
	@SuppressWarnings ("unchecked")
	@operator (
			value = IKeyword.USER_INPUT_DIALOG,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = {})
	@doc (
			value = "Asks the user for some values and returns a map containing these values. Takes a string and a list of calls to the `enter()` or `choose()` operators as arguments. The string is used to specify the message of the dialog box. The list is used to specify the parameters the user can enter. Finally, the font of the title can be specified",
			examples = {
					@example ("map<string,unknown> values2 <- user_input_dialog('Enter number of agents and locations',[enter('Number',100), enter('Location',point, {10, 10})], font('Helvetica', 18));"),
					@example (
							value = "create bug number: int(values2 at \"Number\") with: [location:: (point(values2 at \"Location\"))];",
							isExecutable = false) })
	@no_test
	public static IMap<String, Object> userInputDialog(final IScope scope, final String title, final IList parameters,
			final GamaFont font) {
		parameters.removeIf(p -> !(p instanceof IParameter));
		return GamaMapFactory.createWithoutCasting(Types.STRING, Types.NO_TYPE,
				scope.getGui().openUserInputDialog(scope, title, parameters, font, null, true));
	}

	/**
	 * User input dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param parameters
	 *            the parameters
	 * @param font
	 *            the font
	 * @param color
	 *            the color
	 * @return the i map
	 */
	@operator (
			value = IKeyword.USER_INPUT_DIALOG,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = {})
	@doc (
			value = "Asks the user for some values and returns a map containing these values. Takes a string and a list of calls to the `enter()` or `choose()` operators as arguments. The string is used to specify the message of the dialog box. The list is used to specify the parameters the user can enter. Finally, the font of the title can be specified as well as the background color",
			examples = {
					@example ("map<string,unknown> values2 <- user_input_dialog('Enter number of agents and locations',[enter('Number',100), enter('Location',point, {10, 10})], font('Helvetica', 18));"),
					@example (
							value = "create bug number: int(values2 at \"Number\") with: [location:: (point(values2 at \"Location\"))];",
							isExecutable = false) })
	@no_test
	public static IMap<String, Object> userInputDialog(final IScope scope, final String title, final IList parameters,
			final GamaFont font, final GamaColor color) {
		return userInputDialog(scope, title, parameters, font, color, true);
	}

	/**
	 * User input dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param parameters
	 *            the parameters
	 * @param font
	 *            the font
	 * @param color
	 *            the color
	 * @param showTitle
	 *            the show title
	 * @return the i map
	 */
	@SuppressWarnings ("unchecked")
	@operator (
			value = IKeyword.USER_INPUT_DIALOG,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = {})
	@doc (
			value = "Asks the user for some values and returns a map containing these values. Takes a string and a list of calls to the `enter()` or `choose()` operators as arguments. The string is used to specify the message of the dialog box. The list is used to specify the parameters the user can enter. Finally, the font of the title can be specified, as well as the background color and whether the title and close button of the dialog should be displayed or not",
			masterDoc = true,
			examples = {
					@example ("map<string,unknown> values2 <- user_input_dialog('Enter number of agents and locations',[enter('Number',100), enter('Location',point, {10, 10})], font('Helvetica', 18), #blue, true);"),
					@example (
							value = "create bug number: int(values2 at \"Number\") with: [location:: (point(values2 at \"Location\"))];",
							isExecutable = false) })
	@no_test
	public static IMap<String, Object> userInputDialog(final IScope scope, final String title, final IList parameters,
			final GamaFont font, final GamaColor color, final Boolean showTitle) {
		parameters.removeIf(p -> !(p instanceof IParameter));
		return GamaMapFactory.createWithoutCasting(Types.STRING, Types.NO_TYPE,
				scope.getGui().openUserInputDialog(scope, title, parameters, font, color, showTitle));
	}

	/**
	 * Open wizard.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param finish
	 *            the finish
	 * @param pages
	 *            the pages
	 * @return the i map
	 */
	@operator (
			value = IKeyword.WIZARD,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = { IConcept.SYSTEM, IConcept.GUI })
	@doc (
			value = "Build a wizard and return the values enter by the user as a map of map [\"title page 1\"::[\"var1\"::1,\"var2\"::2]]. Takes a string, an action and a list of calls to the `wizard_page()` operator. The first string is used to specify the title. The action to describe when the wizard is supposed to be finished. A classic way of defining the action is "
					+ "bool eval_finish(map<string,map> input_map) {return input_map[\"page1\"][\"file\"] != nil;}. The list is to specify the wizard pages.",
			examples = { @example (
					value = "map results <-  wizard(\"My wizard\",eval_finish, [wizard_page(\"page1\",\"enter info\" ,[enter(\"var1\",string)], font(\"Arial\", 10))]);",
					isExecutable = false) })
	@no_test

	public static IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final ActionDescription finish, final IList<IMap<String, Object>> pages) {
		return scope.getGui().openWizard(scope, title, finish, pages);
	}

	/**
	 * Open wizard.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param pages
	 *            the pages
	 * @return the i map
	 */
	@operator (
			value = IKeyword.WIZARD,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = { IConcept.SYSTEM, IConcept.GUI })
	@doc (
			value = "Build a wizard and return the values enter by the user as a map of map [\"title page 1\"::[\"var1\"::1,\"var2\"::2]]. Takes a string, a list of calls to the `wizard_page()` operator. The first string is used to specify the title. The list is to specify the wizard pages.",
			examples = { @example (
					value = "map results <-  wizard(\"My wizard\",[wizard_page(\"page1\",\"enter info\" ,[enter(\"var1\",string)], font(\"Arial\", 10))]);",
					isExecutable = false) })
	@no_test
	public static IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final IList<IMap<String, Object>> pages) {
		return scope.getGui().openWizard(scope, title, null, pages);
	}

	/**
	 * Wizard page.
	 *
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 * @param parameters
	 *            the parameters
	 * @param font
	 *            the font
	 * @return the i map
	 */
	@operator (
			value = IKeyword.WIZARD_PAGE,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = { IConcept.SYSTEM, IConcept.GUI })
	@doc (
			value = "Build a wizard page. Takes two strings, a list of calls to the `enter()` or `choose()` operators and a font as arguments. The first string is used to specify the title, the second the description of the dialog box. The list is to specify the parameters the user can enter. The font is used to specify the font",
			examples = { @example (
					value = "map results <-  wizard(\"My wizard\",[wizard_page(\"page1\",\"enter info\" ,[enter(\"var1\",string)], font(\"Arial\", 10))]);",
					isExecutable = false) })
	@no_test
	public static IMap<String, Object> wizardPage(final String title, final String description, final IList parameters,
			final GamaFont font) {
		IMap<String, Object> results = GamaMapFactory.create();
		results.put(IKeyword.TITLE, title);
		results.put(IKeyword.DESCRIPTION, description);
		results.put(IKeyword.PARAMETERS, parameters);
		results.put(IKeyword.FONT, font);
		return results;
	}

	/**
	 * Wizard page.
	 *
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 * @param parameters
	 *            the parameters
	 * @return the i map
	 */
	@operator (
			value = IKeyword.WIZARD_PAGE,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = { IConcept.SYSTEM, IConcept.GUI })
	@doc (
			value = "Build a wizard page. Takes two strings and a list of calls to the `enter()` or `choose()` operators. The first string is used to specify the title, the second the description of the dialog box. The list is to specify the parameters the user can enter",
			examples = { @example (
					value = "map results <-  wizard(\"My wizard\",[wizard_page(\"page1\",\"enter info\" ,[enter(\"var1\",string)])]);",
					isExecutable = false) })
	@no_test
	public static IMap<String, Object> wizardPage(final String title, final String description,
			final IList parameters) {
		IMap<String, Object> results = GamaMapFactory.create();
		results.put(IKeyword.TITLE, title);
		results.put(IKeyword.DESCRIPTION, description);
		results.put(IKeyword.PARAMETERS, parameters);
		return results;
	}

	/**
	 * User confirm dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @return the boolean
	 */
	@operator (
			value = IKeyword.USER_CONFIRM,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = {})
	@doc (
			value = "Asks the user to confirm a choice. The two string are used to specify the title and the message of the dialog box. ",
			examples =

			{ @example ("bool confirm <- user_confirm(\"Confirm\",\"Please confirm\");") })
	@no_test
	public static Boolean userConfirmDialog(final IScope scope, final String title, final String message) {
		return scope.getGui().openUserInputDialogConfirm(scope, title, message);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param type
	 *            the type
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter a value by specifying a title and a type")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final IType type) {
		return enterValue(scope, title, type, type.getDefault());
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter an int by specifying a title and an initial value")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final Integer init) {
		return enterValue(scope, title, Types.INT, init);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @return the i parameter
	 */

	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter an int by specifying a title, an initial value, a min and a max value. "
					+ "The initial value is clamped if it is lower than min or higher than max.")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final Integer init, final Integer min,
			final Integer max) {
		return new InputParameter(title, init, min, max);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param step
	 *            the step
	 * @return the i parameter
	 */

	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter an int by specifying a title, an initial value, a min, a max and a step value. "
					+ "The initial value is clamped if it is lower than min or higher than max.",
			usages = { @usage (
					value = "The GUI is then a slider when an init value, a min (int or float), a max (int or float) (and eventually a  step (int or float) ) operands.",
					examples = { @example (
							value = "map resMinMax <- user_input([enter(\"Title\",5,0)])",
							test = false),
							@example (
									value = "map resMinMax <- user_input([enter(\"Title\",5,0,10)])",
									test = false),
							@example (
									value = "map resMMStepFF <- user_input([enter(\"Title\",5,0.1,10.1,0.5)]);",
									test = false) }) })
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final Integer init, final Integer min,
			final Integer max, final Integer step) {
		return new InputParameter(title, init, min, max, step);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter an int by specifying a title, an initial value, a min and a max value"
					+ "The initial value is clamped if it is lower than min or higher than max.")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final Double init, final Double min,
			final Double max) {
		return new InputParameter(title, init, min, max);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param step
	 *            the step
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter a float by specifying a title, an initial value, a min, a max and a step value. "
					+ "The initial value is clamped if it is lower than min or higher than max.")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final Double init, final Double min,
			final Double max, final Double step) {
		return new InputParameter(title, init, min, max, step);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter a float by specifying a title and an initial value")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final Double init) {
		return enterValue(scope, title, Types.FLOAT, init);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter a boolean value by specifying a title and an initial value",
			usages = { @usage (
					value = "When the second operand is the boolean type or a boolean value, the GUI is then a switch",
					examples = { @example (
							value = "map<string,unknown> m <- user_input(enter(\"Title\",true));",
							test = false),
							@example (
									value = "map<string,unknown> m2 <- user_input(enter(\"Title\",bool));",
									test = false) }) })
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final Boolean init) {
		return enterValue(scope, title, Types.BOOL, init);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter a string by specifying a title and an initial value",
			masterDoc = true)
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final String init) {
		return enterValue(scope, title, Types.STRING, init);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param type
	 *            the type
	 * @param init
	 *            the init
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter a value by specifying a title, a type, and an initial value")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final IType type, final Object init) {
		return new InputParameter(title, init, type);
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param init
	 *            the init
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.ENTER)
	@doc (
			value = "Allows the user to enter a value by specifying a title and an initial value. The type will be deduced from the value")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final Object init) {
		return new InputParameter(title, init, GamaType.of(init));
	}

	/**
	 * Enter value.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param type
	 *            the type
	 * @param init
	 *            the init
	 * @param among
	 *            the among
	 * @return the i parameter
	 */
	@operator (
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			value = IKeyword.CHOOSE)
	@doc (
			value = "Allows the user to choose a value by specifying a title, a type, and a list of possible values")
	@no_test
	public static IParameter enterValue(final IScope scope, final String title, final IType type, final Object init,
			final IList among) {
		return new InputParameter(title, init, type, among);
	}

	/**
	 * Copy to clipboard.
	 *
	 * @param scope
	 *            the scope
	 * @param text
	 *            the text
	 * @return the boolean
	 */

	/**
	 * Copy to clipboard.
	 *
	 * @param scope
	 *            the scope
	 * @param text
	 *            the text
	 * @return the boolean
	 */
	@operator (
			value = "copy_to_clipboard",
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM })
	@doc (
			examples = @example ("bool copied  <- copy_to_clipboard('text to copy');"),
			value = "Tries to copy the text in parameter to the clipboard and returns whether it has been correctly copied or not (for instance it might be impossible in a headless environment)")
	@no_test ()
	public static Boolean copyToClipboard(final IScope scope, final String text) {
		return scope.getGui().copyToClipboard(text);
	}

	/**
	 * Copy from clipboard.
	 *
	 * @param scope
	 *            the scope
	 * @param type
	 *            the type
	 * @return the boolean
	 */
	@operator (
			value = "copy_from_clipboard",
			can_be_const = false,
			type = ITypeProvider.DENOTED_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM })
	@doc (
			examples = @example ("string copied  <- copy_from_clipboard(string);"),
			value = "Tries to copy data from the clipboard by passing its expected type. Returns nil if it has not been correctly retrieved, or not retrievable using the given type or if GAMA is in a headless environment")
	@no_test ()
	public static Object copyFromClipboard(final IScope scope, final IType type) {
		return type.copyFromClipboard(scope);
	}

}
