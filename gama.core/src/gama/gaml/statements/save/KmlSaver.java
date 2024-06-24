/*******************************************************************************************************
 *
 * KmlSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.save;

import java.io.File;
import java.util.Set;

import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.BufferingController.BufferingStrategies;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.GamaKmlExport;

/**
 * The Class KmlSaver.
 */
public class KmlSaver extends AbstractSaver {

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param item
	 *            the item
	 * @param fileToSave
	 *            the file to save
	 * @param type
	 *            the type
	 */
	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions options) {
		final Object kml = item.value(scope);
		String path = file.getAbsolutePath();
		if (!(kml instanceof GamaKmlExport export)) return;
		if ("kml".equals(options.type)) {
			export.saveAsKml(scope, path);
		} else {
			export.saveAsKmz(scope, path);
		}
	}

	@Override
	public Set<String> computeFileTypes() {
		return Set.of("kml", "kmz");
	}

}
