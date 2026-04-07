/*******************************************************************************************************
 *
 * IModelDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gama.api.gaml.types.ITypesManager;
import gama.api.utils.interfaces.ConsumerWithPruning;

/**
 *
 */
public interface IModelDescription extends ISpeciesDescription {

	/** The built in models. */
	Map<String, IModelDescription> BUILT_IN_MODELS = new HashMap<>();

	/** The Constant MODEL_SUFFIX. */
	// TODO Move elsewhere
	String MODEL_SUFFIX = "_model";

	/**
	 * @return
	 */
	boolean isMicroModel();

	/**
	 * @return
	 */
	boolean isStartingDateDefined();

	/**
	 * @return
	 */
	String getMicroAlias();

	/**
	 * Visit all species.
	 *
	 * @param visitor
	 *            the visitor
	 */
	void visitAllSpecies(final ConsumerWithPruning<ISpeciesDescription> visitor);

	/**
	 * Visit all classes.
	 *
	 * @param visitor
	 *            the visitor
	 */
	void visitAllClasses(final ConsumerWithPruning<IClassDescription> visitor);

	/**
	 * @param alias
	 * @return
	 */
	IModelDescription getMicroModel(String alias);

	/**
	 * @return
	 */
	Set<String> getExperimentNames();

	/**
	 * @param result
	 */
	void getAllSpecies(List<ISpeciesDescription> result);

	/**
	 * @return
	 */
	ITypesManager getTypesManager();

	/**
	 * @param name
	 * @return
	 */
	boolean hasExperiment(String name);

	/**
	 * @return
	 */
	@Override
	Map<String, ISpeciesDescription> getOwnMicroSpecies();

	/**
	 * @param allModelNames
	 */
	void setImportedModelNames(Collection<String> allModelNames);

	/**
	 * @param name
	 * @return
	 */
	IExperimentDescription getExperiment(String name);

	/**
	 * @return
	 */
	Collection<String> getAlternatePaths();

	/**
	 *
	 */
	void buildTypes();

	/**
	 * @param aliasName
	 */
	void setMicroAlias(String aliasName);

	/**
	 * Validate.
	 *
	 * @return the i model description
	 */
	@Override
	IModelDescription validate();

	/**
	 * @return
	 */
	Collection<? extends IExperimentDescription> getExperiments();

	/**
	 * @return
	 */
	String getModelFilePath();

	/**
	 * @return
	 */
	String getModelProjectPath();

	/**
	 * @return
	 */
	String getModelFolderPath();

	/**
	 * @param p
	 * @return
	 */
	IClassDescription getClassDescription(String p);

}
