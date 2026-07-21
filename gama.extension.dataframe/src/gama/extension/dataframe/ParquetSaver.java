/*******************************************************************************************************
 *
 * ParquetSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.dataframe;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.dflib.parquet.Parquet;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.SaveOptions;
import gama.gaml.statements.save.AbstractSaver;

/**
 * Saves a dataframe to a Parquet file (.parquet) through the 'save' statement.
 *
 * <p>
 * Example: {@code save my_dataframe to: "../results/output.parquet" format: "parquet";}
 * </p>
 */
public class ParquetSaver extends AbstractSaver {

	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions saveOptions)
			throws IOException {
		final Object value = item.value(scope);
		if (!(value instanceof IDataFrame)) throw GamaRuntimeException
				.error("Saving to Parquet expects a dataframe, but got " + item.getGamlType(), scope);
		Parquet.save(((IDataFrame) value).getInner(), file);
	}

	@Override
	public boolean handlesDataType(final IType request) {
		return request != null && request.id() == IDataframeConstants.ID;
	}

	@Override
	protected Set<String> computeFileTypes() {
		return Set.of("parquet");
	}

}
