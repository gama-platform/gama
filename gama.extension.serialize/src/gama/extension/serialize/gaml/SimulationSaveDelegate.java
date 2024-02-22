/*******************************************************************************************************
 *
 * SimulationSaveDelegate.java, in gama.serialize, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.gaml;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import gama.core.common.interfaces.ISaveDelegate;
import gama.core.common.interfaces.ISerialisationConstants;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.extension.serialize.binary.BinarySerialisation;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class SimulationSaveDelegate.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 8 août 2023
 */
public class SimulationSaveDelegate implements ISaveDelegate, ISerialisationConstants {

	@Override
	public void save(final IScope scope, final IExpression item, final File file, final String code,
			final boolean addHeader, final String type, final Object attributesToSave) throws IOException {
		Object toSave = item.value(scope);
		if (toSave instanceof IAgent sa) {
			BinarySerialisation.saveToFile(scope, sa, file.getPath(), type, true, true);
		}
	}

	@Override
	public Set<String> getFileTypes() { return Sets.union(FILE_FORMATS, FILE_TYPES); }

	@Override
	public IType getDataType() { return Types.AGENT; }

	@Override
	public boolean handlesDataType(final IType request) {
		return request.isAgentType();
	}

}
