/*******************************************************************************************************
 *
 * ParametersSet.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.experiment;

import java.util.Collection;
import java.util.Map;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMap;
import gama.core.util.file.GamaFile;
import gama.gaml.types.Types;

/**
 * The Class ParametersSet.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class ParametersSet extends GamaMap<String, Object> {

	/** The empty. */
	public final static ParametersSet EMPTY = new ParametersSet();

	/**
	 * Instantiates a new parameters set. A GamaMap with some specialized constructors and a specialisation of put(..)
	 */
	public ParametersSet() {
		super(10, Types.STRING, Types.NO_TYPE);
	}

	/**
	 * Instantiates a new parameters set.
	 *
	 * @param solution
	 *            the solution
	 */
	public ParametersSet(final ParametersSet solution) {
		this();
		putAll(solution);
	}

	/**
	 * Instantiates a new parameters set.
	 *
	 * @param scope
	 *            the scope
	 * @param variables
	 *            the variables
	 * @param reinit
	 *            the reinit
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public ParametersSet(final IScope scope, final Map<String, IParameter> variables, final boolean reinit)
			throws GamaRuntimeException {
		this();
		for (final String var : variables.keySet()) {
			final IParameter varBat = variables.get(var);
			if (varBat instanceof ExperimentParameter ep) { scope.setCurrentSymbol(ep); }
			if (reinit && varBat instanceof IParameter.Batch) { ((IParameter.Batch) varBat).reinitRandomly(scope); }
			put(var, varBat.value(scope));
		}

	}

	/**
	 * Instantiates a new parameters set.
	 *
	 * @param scope
	 *            the scope
	 * @param parameters
	 *            the parameters
	 * @param reinit
	 *            the reinit
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public ParametersSet(final IScope scope, final Collection<? extends IParameter> parameters, final boolean reinit)
			throws GamaRuntimeException {
		this();
		for (final IParameter p : parameters) {
			if (p instanceof ExperimentParameter ep) { scope.setCurrentSymbol(ep); }
			if (reinit && p instanceof IParameter.Batch) { ((IParameter.Batch) p).reinitRandomly(scope); }
			put(p.getName(), p.value(scope));
		}
	}

	@Override
	public Object put(final String s, final Object o) {
		// Special case for files as they are not invariant. Their contents must
		// be invalidated before they are loaded
		// again in a simulation. See Issue 812.
		if (o instanceof GamaFile) { ((GamaFile) o).invalidateContents(); }
		return super.put(s, o);
	}

}
