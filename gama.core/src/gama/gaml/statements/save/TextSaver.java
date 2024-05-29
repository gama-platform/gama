/*******************************************************************************************************
 *
 * TextSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.save;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import gama.core.common.interfaces.ISerialisationConstants;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.WriteController.BufferingStrategies;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Strings;

/**
 * The Class TextSaver.
 */
public class TextSaver extends AbstractSaver {

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param item
	 *            the item
	 * @param file
	 *            the file
	 * @param code
	 *            the code
	 * @param addHeader
	 *            the add header
	 * @param type
	 *            the type
	 * @param attributesToSave
	 *            the attributes to save
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void save(final IScope scope, final IExpression item, final File file, final String code,
			final boolean addHeader, final String type, final Object attributesToSave, final BufferingStrategies bufferingStrategy)
			throws GamaRuntimeException {
		String toSave = Cast.asString(scope, item.value(scope));
		char id = toSave.charAt(0);
		Charset ch = id == ISerialisationConstants.GAMA_AGENT_IDENTIFIER
				|| id == ISerialisationConstants.GAMA_OBJECT_IDENTIFIER
						? ISerialisationConstants.STRING_BYTE_ARRAY_CHARSET : StandardCharsets.UTF_8;
		try  {
			GAMA.askWriteFile(scope.getSimulation(), file, toSave, bufferingStrategy);
		} catch (final GamaRuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	@Override
	protected Set<String> computeFileTypes() {
		return Set.of("text", "txt");
	}

	@Override
	public BiMap<String, String> getSynonyms() { return ImmutableBiMap.of("txt", "text"); }

}
