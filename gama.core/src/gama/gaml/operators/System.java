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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IObject;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.gaml.expressions.IExpression;
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
	 * True if the agent is dead (or null), false otherwise.
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
	public static Boolean dead(final IScope scope, final IAgent a) {
		return a == null || a.dead();
	}

	/**
	 * Instantiate.
	 *
	 * @param scope
	 *            the scope
	 * @param type
	 *            the type
	 * @param args
	 *            the args
	 * @return the i object
	 */
	@operator (
			value = IKeyword.INSTANTIATE,
			can_be_const = false,
			type = ITypeProvider.DENOTED_TYPE_AT_INDEX + 1,
			concept = IConcept.SYSTEM)
	@doc ("Allow to create an object of a given type, with given arguments. The type can be either an agent type or an object type. The arguments are given as a map of name/value pairs, where the names are the names of the atrributes of the species or class.")
	public static IObject instantiate(final IScope scope, final IType type, final IMap<String, Object> args) {
		if (type == null || type == Types.NO_TYPE) return null;
		if (type.isAgentType()) return scope.getModel().getSpecies(type.getSpeciesName()).createInstance(scope, args);
		if (type.isObjectType()) return scope.getModel().getClass(type.getSpeciesName()).createInstance(scope, args);
		throw GamaRuntimeException.error("Cannot instantiate an object of type " + type.getName(), scope);
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
	 * @param environment
	 *            the environment
	 * @return the string
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
	public static Object copy(final IScope scope, final Object o) throws GamaRuntimeException {
		if (o instanceof IValue) return ((IValue) o).copy(scope);
		return o;
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
	 * @param environment
	 *            the environment
	 * @return the string
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
	public static Object dot(final IScope scope, final IAgent a, final IExpression s)
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
	public static Object dot(final IScope scope, final IObject a, final IExpression s)
			throws GamaRuntimeException {
		if (a == null) {
			if (!scope.interrupted()) throw GamaRuntimeException
					.warning("Cannot evaluate " + s.serializeToGaml(false) + " as the target object is nil", scope);
			return null;
		}
		return scope.evaluate(s, a).getValue();
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

}
