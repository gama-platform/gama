/*******************************************************************************************************
 *
 * AbstractSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.save;

import java.util.Set;

import gama.core.common.interfaces.ISaveDelegate;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class AbstractSaver.
 */
public abstract class AbstractSaver implements ISaveDelegate {

	/** The file types. */
	Set<String> fileTypes = computeFileTypes();

	@Override
	public Set<String> getFileTypes() { return fileTypes; }

	/**
	 * Compute file types.
	 *
	 * @return the string[]
	 */
	protected abstract Set<String> computeFileTypes();

	@Override
	public IType getDataType() { return Types.NO_TYPE; }

}
