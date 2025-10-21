/*******************************************************************************************************
 *
 * IModelSpecies.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import gama.core.kernel.experiment.IExperimentSpecies;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.species.IClass;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.test.TestStatement;

/**
 * Written by drogoul Modified on 29 d�c. 2010
 *
 * @todo Description
 *
 */
public interface IModelSpecies extends ISpecies {

	/**
	 * Gets the class or species.
	 *
	 * @param name
	 *            the name
	 * @return the class or species
	 */
	default IClass getClassOrSpecies(final String name) {
		IClass c = getClass(name);
		if (c == null) { c = getSpecies(name); }
		return c;
	}

	/**
	 * Gets the class or species.
	 *
	 * @param name
	 *            the name
	 * @param origin
	 *            the origin
	 * @return the class or species
	 */
	default IClass getClassOrSpecies(final String name, final String origin) {
		IClass c = getClass(name, origin);
		if (c == null) { c = getSpecies(name, origin); }
		return c;
	}

	/**
	 * Gets the class.
	 *
	 * @param name
	 *            the name
	 * @return the class
	 */
	IClass getClass(String name);

	/**
	 * Gets the class.
	 *
	 * @param name
	 *            the name
	 * @param origin
	 *            the origin
	 * @return the class
	 */
	IClass getClass(String name, String origin);

	/**
	 * Gets the species.
	 *
	 * @param speciesName
	 *            the species name
	 * @return the species
	 */
	ISpecies getSpecies(String speciesName);

	/**
	 * Gets the species.
	 *
	 * @param speciesName
	 *            the species name
	 * @param origin
	 *            the spec des
	 * @return the species
	 */
	ISpecies getSpecies(String speciesName, String origin);

	/**
	 * Gets the experiment.
	 *
	 * @param s
	 *            the s
	 * @return the experiment
	 */
	IExperimentSpecies getExperiment(final String s);

	/**
	 * Gets the experiments.
	 *
	 * @return the experiments
	 */
	Iterable<IExperimentSpecies> getExperiments();

	/**
	 * Gets the working path.
	 *
	 * @return the working path
	 */
	String getWorkingPath();

	/**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
	String getFilePath();

	/**
	 * Gets the project path.
	 *
	 * @return the project path
	 */
	String getProjectPath();

	/**
	 * Gets the all species.
	 *
	 * @return the all species
	 */
	Map<String, ISpecies> getAllSpecies();

	/**
	 * Gets the imported paths.
	 *
	 * @return the imported paths
	 */
	Collection<String> getImportedPaths();

	/**
	 * Gets the all tests.
	 *
	 * @return the all tests
	 */
	List<TestStatement> getAllTests();

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@Override
	ModelDescription getDescription();

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	@Override
	default URI getURI() {
		final ModelDescription md = getDescription();
		if (md == null) return null;
		final EObject o = md.getUnderlyingElement();
		if (o == null) return null;
		final Resource r = o.eResource();
		if (r == null) return null;
		return r.getURI();
	}

}