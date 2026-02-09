/*******************************************************************************************************
 *
 * CSVSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.save;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.data.csv.AbstractCSVManipulator;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.objects.IList;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.api.utils.files.BufferingUtils;
import gama.api.utils.files.SaveOptions;
import gama.core.util.matrix.GamaMatrix;
import gama.gaml.statements.SaveStatement;

/**
 * The Class CSVSaver.
 */
public class CSVSaver extends AbstractSaver {

	// TODO Attributes not used for the moment ?

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param item
	 *            the item
	 * @param os
	 *            the os
	 * @param header
	 *            the header
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	// public void save(final IScope scope, final IExpression item, final OutputStream os, final boolean header)
	// throws GamaRuntimeException {
	// if (os == null) return;
	// save(scope, new OutputStreamWriter(os), header, item);
	// }

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param item
	 *            the item
	 * @param f
	 *            the f
	 * @param header
	 *            the header
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void save(final IScope scope, final IExpression item, final File file, final SaveOptions saveOptions)
			throws GamaRuntimeException, IOException {

		StringBuilder sb = new StringBuilder();
		final IType itemType = item.getGamlType();
		final ISpeciesDescription sd;
		if (itemType.isAgentType()) {
			sd = itemType.getSpecies();
		} else if (itemType.getContentType().isAgentType()) {
			sd = itemType.getContentType().getSpecies();
		} else {
			sd = null;
		}
		final Object value = item.value(scope);
		final IList values = itemType.isContainer() ? GamaListFactory.toList(scope, value)
				: GamaListFactory.create(scope, itemType, value);
		if (values.isEmpty()) return;
		char del = AbstractCSVManipulator.getDefaultDelimiter();
		if (sd != null) {
			final Collection<String> attributeNames = sd.getAttributeNames();
			attributeNames.removeAll(SaveStatement.NON_SAVEABLE_ATTRIBUTE_NAMES);
			if (saveOptions.addHeader) {
				sb.append("cycle" + del + "name;location.x" + del + "location.y" + del + "location.z");
				for (final String v : attributeNames) { sb.append(del + v); }
				sb.append(StringUtils.LN);
			}
			for (final Object obj : values) {
				if (obj instanceof IAgent) {
					final IAgent ag = Cast.asAgent(scope, obj);
					sb.append(scope.getClock().getCycle() + del + ag.getName().replace(';', ',') + del
							+ ag.getLocation().getX() + del + ag.getLocation().getY() + del + ag.getLocation().getZ());
					for (final String v : attributeNames) {
						String val = StringUtils.toGaml(ag.getDirectVarValue(scope, v), false).replace(';', ',');
						if (val.startsWith("'") && val.endsWith("'") || val.startsWith("\"") && val.endsWith("\"")) {
							val = val.substring(1, val.length() - 1);
						}
						sb.append(del + val);
					}
					sb.append(StringUtils.LN);
				}
			}
		} else {
			if (saveOptions.addHeader) {
				sb.append(item.serializeToGaml(true).replace("]", "").replace("[", "").replace(',', del));
				sb.append(StringUtils.LN);
			}
			if (itemType.id() == IType.MATRIX) {
				GamaMatrix<?> matrix = (GamaMatrix<?>) value;
				matrix.rowByRow(scope, v -> sb.append(toCleanString(v)), () -> sb.append(del),
						() -> sb.append(StringUtils.LN));
			} else {
				final int size = values.size();
				for (int i = 0; i < size; i++) {
					if (i > 0) { sb.append(del); }
					sb.append(toCleanString(values.get(i)));
				}
			}
			sb.append(StringUtils.LN);
		}
		BufferingUtils.getInstance().askWriteFile(file.getAbsolutePath(), scope, sb, saveOptions);
	}

	/**
	 * To clean string.
	 *
	 * @param o
	 *            the o
	 * @return the string
	 */
	private String toCleanString(final Object o) {
		// Verify this (shouldn't we use AbstractCSVManipulator.getDefaultDelimiter() ?)
		String val = StringUtils.toGaml(o, false).replace(';', ',');
		if (val.startsWith("'") && val.endsWith("'") || val.startsWith("\"") && val.endsWith("\"")) {
			val = val.substring(1, val.length() - 1);
		}

		if (o instanceof String) {
			val = val.replace("\\'", "'");
			val = val.replace("\\\"", "\"");

		}
		return val;
	}

	@Override
	protected Set<String> computeFileTypes() {
		return Set.of("csv");
	}

}
