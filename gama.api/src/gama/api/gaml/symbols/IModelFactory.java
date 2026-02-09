/*******************************************************************************************************
 *
 * IModelFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.symbols;

import java.util.Map;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.compilation.validation.IValidationContext;

/**
 *
 */
public interface IModelFactory extends ISymbolDescriptionFactory.Species {

	/**
	 * Creates a model description from a set of syntactic elements (parsed model files).
	 *
	 * @param projectPath
	 *            the absolute path of the project containing the model
	 * @param modelPath
	 *            the absolute path of the model file
	 * @param models
	 *            the parsed syntactic elements
	 * @param collector
	 *            the validation context for reporting errors/warnings
	 * @param mm
	 *            a map of dependencies or micro-models
	 * @return the created IModelDescription
	 */
	IModelDescription createModelDescription(final String projectPath, final String modelPath,
			final Iterable<ISyntacticElement> models, final IValidationContext collector,
			final Map<String, IModelDescription> mm);

}
