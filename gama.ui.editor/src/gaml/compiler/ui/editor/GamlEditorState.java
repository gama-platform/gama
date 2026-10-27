/*******************************************************************************************************
 *
 * GamlEditorState.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.validation.IValidationContext;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.IGamaColors;

/**
 * A validation snapshot for experiment controls. Descriptions and validation contexts are not retained, so this
 * object can be built during validation and passed to the UI after the descriptions have been disposed.
 */
public class GamlEditorState {

	/** The has internal errors. */
	final boolean hasInternalErrors;

	/** The has imported errors. */
	final boolean hasImportedErrors;

	/** The has experiments. */
	final boolean hasExperiments;

	/** The has null status. */
	final boolean hasNullStatus;

	/** The show experiments. */
	final boolean showExperiments;

	/** The experiments. */
	public final List<String> experiments;

	/** The abbreviations. */
	public final List<String> abbreviations;

	/** The types. */
	final List<String> types;

	/** The imported errors. */
	final Map<String, URI> importedErrors;

	/**
	 * Instantiates a new gaml editor state.
	 *
	 * @param status
	 *            the status
	 * @param descriptions
	 *            the descriptions
	 */
	public GamlEditorState(final IValidationContext status, final Iterable<? extends IDescription> descriptions) {

		if (status != null) {
			hasImportedErrors = status.hasImportedErrors();
			importedErrors = hasImportedErrors
					? Collections.unmodifiableMap(new LinkedHashMap<>(status.getImportedErrorsAsStrings()))
					: Collections.emptyMap();
			hasInternalErrors = status.hasInternalErrors() || status.hasInternalSyntaxErrors();
			hasNullStatus = false;
			showExperiments = !status.getNoExperiment();
		} else {
			hasNullStatus = true;
			hasInternalErrors = false;
			hasImportedErrors = false;
			importedErrors = Collections.emptyMap();
			showExperiments = true;
		}
		final List<String> names = new ArrayList<>();
		final List<String> experimentTypes = new ArrayList<>();
		if (descriptions != null) {
			for (final IDescription ep : descriptions) {
				final String name = ep.getName();
				names.add(name);
				IExperimentDescription r = (IExperimentDescription) ep;
				experimentTypes.add(IKeyword.BATCH.equals(r.getLitteral(IKeyword.TYPE)) ? IKeyword.BATCH
						: r.isMemorize() ? IKeyword.RECORD : IKeyword.GUI_);
			}
		}
		experiments = Collections.unmodifiableList(names);
		abbreviations = experiments;
		types = Collections.unmodifiableList(experimentTypes);
		hasExperiments = !experiments.isEmpty();
	}

	/** @return whether validation allows experiment controls to be shown */
	public boolean isShowingExperiments() { return showExperiments; }

	/** @return whether an imported-error menu should be shown */
	public boolean hasImportedErrors() { return hasImportedErrors; }

	/** @return experiment types, in the same order as {@link #experiments} */
	public List<String> getExperimentTypes() { return types; }

	/**
	 * Equals 2.
	 *
	 * @param old
	 *            the old
	 * @return true, if successful
	 */
	public boolean equals2(final Object old) {
		if (!(old instanceof final GamlEditorState state)) return false;
		return equals(state);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(experiments, hasImportedErrors, hasInternalErrors, hasNullStatus, showExperiments, types,
				importedErrors);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final GamlEditorState other = (GamlEditorState) obj;
		if (!Objects.equals(experiments, other.experiments) || hasImportedErrors != other.hasImportedErrors
				|| hasInternalErrors != other.hasInternalErrors || hasNullStatus != other.hasNullStatus)
			return false;
		if (showExperiments != other.showExperiments || !Objects.equals(types, other.types)
				|| !Objects.equals(importedErrors, other.importedErrors)) return false;
		return true;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	public GamaUIColor getColor() {
		if (hasImportedErrors || hasInternalErrors) return IGamaColors.ERROR;
		if (!hasExperiments) return IGamaColors.WARNING;
		return IGamaColors.OK;
	}

	/** The Constant NO_EXP_DEFINED. */
	public final static String NO_EXP_DEFINED = "No experiments defined";

	/** The Constant ERRORS_DETECTED. */
	public final static String ERRORS_DETECTED = "Error(s) detected";

	/** The Constant IN_IMPORTED_FILES. */
	public final static String IN_IMPORTED_FILES = "Error(s) in imported files";

	/** The Constant IMPOSSIBLE_TO_RUN. */
	public final static String IMPOSSIBLE_TO_RUN = "Impossible to run any experiment";

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		String msg = null;
		if (hasInternalErrors) {
			msg = ERRORS_DETECTED;
			if (hasImportedErrors) { msg = IN_IMPORTED_FILES; }
		} else if (hasImportedErrors) {
			msg = IN_IMPORTED_FILES;
		} else if (!hasExperiments)
			return NO_EXP_DEFINED;
		else
			return null;
		if (hasExperiments) { msg += ". " + IMPOSSIBLE_TO_RUN; }
		return msg;
	}

	/**
	 * Gets the imported errors.
	 *
	 * @return the imported errors
	 */
	public Map<String, URI> getImportedErrors() { return importedErrors; }
}