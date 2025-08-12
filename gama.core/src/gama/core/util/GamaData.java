/*******************************************************************************************************
 *
 * GamaData.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;

/**
 * The Class GamaData.
 */
public class GamaData implements IValue {

	@Override
	public JsonValue serializeToJson(final Json json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets the field value.
	 *
	 * @param name
	 *            the name
	 * @return the field value
	 */
	public Object getFieldValue(final String name) {
		return null;
	}

}
