/*******************************************************************************************************
 *
 * CSVSaver.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements.save;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import gama.core.common.util.StringUtils;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.file.csv.AbstractCSVManipulator;
import gama.core.util.matrix.GamaMatrix;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Strings;
import gama.gaml.statements.SaveStatement;
import gama.gaml.types.IType;

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
//	public void save(final IScope scope, final IExpression item, final OutputStream os, final boolean header)
//			throws GamaRuntimeException {
//		if (os == null) return;
//		save(scope, new OutputStreamWriter(os), header, item);
//	}

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
		final SpeciesDescription sd;
		if (itemType.isAgentType()) {
			sd = itemType.getSpecies();
		} else if (itemType.getContentType().isAgentType()) {
			sd = itemType.getContentType().getSpecies();
		} else {
			sd = null;
		}
		final Object value = item.value(scope);
		final IList values =
				itemType.isContainer() ? Cast.asList(scope, value) : GamaListFactory.create(scope, itemType, value);
		if (values.isEmpty()) return;
		char del = AbstractCSVManipulator.getDefaultDelimiter();
		if (sd != null) {
			final Collection<String> attributeNames = sd.getAttributeNames();
			attributeNames.removeAll(SaveStatement.NON_SAVEABLE_ATTRIBUTE_NAMES);
			if (saveOptions.addHeader) {
				sb.append("cycle" + del + "name;location.x" + del + "location.y" + del + "location.z");
				for (final String v : attributeNames) { sb.append(del + v); }
				sb.append(Strings.LN);
			}
			for (final Object obj : values) {
				if (obj instanceof IAgent) {
					final IAgent ag = Cast.asAgent(scope, obj);
					sb.append(scope.getClock().getCycle() + del + ag.getName().replace(';', ',') + del
							+ ag.getLocation().getX() + del + ag.getLocation().getY() + del
							+ ag.getLocation().getZ());
					for (final String v : attributeNames) {
						String val = StringUtils.toGaml(ag.getDirectVarValue(scope, v), false).replace(';', ',');
						if (val.startsWith("'") && val.endsWith("'")
								|| val.startsWith("\"") && val.endsWith("\"")) {
							val = val.substring(1, val.length() - 1);
						}
						sb.append(del + val);
					}
					sb.append(Strings.LN);
				}
			}
		} else {
			if (saveOptions.addHeader) {
				sb.append(item.serializeToGaml(true).replace("]", "").replace("[", "").replace(',', del));
				sb.append(Strings.LN);
			}
			if (itemType.id() == IType.MATRIX) {
				GamaMatrix<?> matrix = (GamaMatrix<?>) value;
				matrix.rowByRow(scope, v -> sb.append(toCleanString(v)), () -> sb.append(del),
						() -> sb.append(Strings.LN));
			} else {
				final int size = values.size();
				for (int i = 0; i < size; i++) {
					if (i > 0) { sb.append(del); }
					sb.append(toCleanString(values.get(i)));
				}
			}
			sb.append(Strings.LN);
		}
		GAMA.getBufferingController().askWriteFile(file.getAbsolutePath(), scope, sb, saveOptions);
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
