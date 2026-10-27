/*******************************************************************************************************
 *
 * Invocation.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime.scope;

import gama.api.gaml.symbols.Arguments;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.object.IObject;
import gama.api.runtime.IExecutable;

/**
 * The data of one call of an executable (actual arguments, caller and "myself"), handed by the scope executing it to
 * the executable through {@link IScope#setInvocation(Invocation)} and {@link IScope#takeInvocation(IExecutable)}.
 *
 * <p>
 * As the executables of a model (actions, statements...) are shared by all its simulations (and by the threads
 * executing agents in parallel), they cannot hold this data themselves: it belongs to the call, and therefore lives in
 * the scope performing it, which is never used by two threads at the same time.
 * </p>
 *
 * @param executable
 *            the executable called (only it can take this invocation)
 * @param args
 *            the actual arguments of the call (may be null)
 * @param caller
 *            the object in the context of which the arguments are evaluated (may be null)
 * @param myself
 *            the agent calling the executable, i.e. the value of <code>myself</code> in remote sequences (may be null)
 */
public record Invocation(IExecutable executable, Arguments args, IObject caller, IAgent myself) {}
