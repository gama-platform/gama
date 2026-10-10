/*******************************************************************************************************
 *
 * RDSSaver.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats.rds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import org.renjin.sexp.SEXP;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.SaveOptions;
import gama.gaml.statements.save.AbstractSaver;
import se.alipsa.rdatautils.RDataUtil;

/**
 * Implementation of ISaveDelegate for saving GAMA objects (matrices, dataframes, lists, maps, agents) into R RDS files.
 */
public class RDSSaver extends AbstractSaver {

	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions saveOptions)
			throws GamaRuntimeException, IOException {
		if (item == null) return;
		Object val = item.value(scope);
		if (val == null) return;

		SEXP sexp = RdsGamaConverter.toSexp(scope, val);
		try (OutputStream os = new FileOutputStream(file)) {
			RDataUtil.write(sexp, os);
		}
	}

	@Override
	protected Set<String> computeFileTypes() {
		return Set.of("rds");
	}
}
