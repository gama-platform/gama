/*******************************************************************************************************
 *
 * DataFrameJsonSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.dataframe;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.dflib.json.Json;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.SaveOptions;
import gama.gaml.statements.save.JsonSaver;

/**
 * The Class DataFrameJsonSaver.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 4 nov. 2023
 */
public class DataFrameJsonSaver extends JsonSaver {

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param item
	 *            the item
	 * @param file
	 *            the file
	 * @param saveOptions
	 *            the save options
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions saveOptions)
			throws GamaRuntimeException {
		// Dataframes are written as a JSON array of objects through DFLib, so they can be reloaded with dataframe_file.
		// This is the only path that honors the 'encoding' facet (carried by saveOptions.writeCharset()).

		try (Writer w = new FileWriter(file, saveOptions.writeCharset(), false)) {
			Json.save(((IDataFrame) item.value(scope)).getInner(), w);
		} catch (final Exception e) {
			throw GamaRuntimeException.create(e, scope);
		}

	}

	/**
	 * Gets the data type.
	 *
	 * @return the data type
	 */
	@Override
	public IType getDataType() { return IDataframeConstants.TYPE; }

	@Override
	public boolean handlesEncoding() {
		return true;
	}

}
