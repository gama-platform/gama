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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import gama.core.common.interfaces.ISerialisationConstants;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;

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
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions options)
			throws GamaRuntimeException {
		String toSave = Cast.asString(scope, item.value(scope));
		char id = toSave.charAt(0);
		Charset ch = id == ISerialisationConstants.GAMA_AGENT_IDENTIFIER
				|| id == ISerialisationConstants.GAMA_OBJECT_IDENTIFIER
						? ISerialisationConstants.STRING_BYTE_ARRAY_CHARSET : StandardCharsets.UTF_8;
		options.setCharSet(ch);
		
		try  {
			GAMA.getBufferingController().askWriteFile(file.getAbsolutePath(), scope, toSave, options);
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
