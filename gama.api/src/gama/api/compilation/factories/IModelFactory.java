/*******************************************************************************************************
 *
 * IModelFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.factories;

import java.util.Map;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.validation.IDocumentationContext;
import gama.api.compilation.validation.IValidationContext;

/**
 * Factory interface for creating GAML model descriptions from parsed syntactic elements.
 * 
 * <p>
 * The IModelFactory is responsible for transforming the abstract syntax tree (AST) of parsed GAML model files into
 * compiled model descriptions. It extends {@link ISymbolDescriptionFactory.Species} because the model is essentially a
 * special type of species (the top-level container).
 * </p>
 * 
 * <h2>Compilation Process</h2>
 * <p>
 * The factory processes models through these stages:
 * </p>
 * <ol>
 * <li><strong>Parsing</strong> - GAML source files are parsed into syntactic elements</li>
 * <li><strong>Description Creation</strong> - The factory creates model descriptions from the AST</li>
 * <li><strong>Validation</strong> - Errors and warnings are collected in the validation context</li>
 * <li><strong>Documentation</strong> - Documentation metadata is extracted for help generation</li>
 * <li><strong>Dependency Resolution</strong> - Import statements are resolved and sub-models linked</li>
 * </ol>
 * 
 * <h2>Model Structure</h2>
 * <p>
 * A GAML model typically consists of:
 * </p>
 * <ul>
 * <li><strong>model</strong> declaration - The top-level container</li>
 * <li><strong>global</strong> section - Global variables, initialization, and behaviors</li>
 * <li><strong>species</strong> - Agent type definitions</li>
 * <li><strong>experiment</strong> - Simulation experiments</li>
 * <li><strong>import</strong> statements - Dependencies on other model files</li>
 * </ul>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see IModelDescription
 * @see ISyntacticElement
 */
public interface IModelFactory extends ISymbolDescriptionFactory.Species {

	/**
	 * Creates a model description from a set of syntactic elements (parsed model files).
	 * 
	 * <p>
	 * This method is the main entry point for model compilation. It processes the parsed AST and produces a complete
	 * model description that can be used to instantiate and run simulations.
	 * </p>
	 * 
	 * <p>
	 * The method handles:
	 * </p>
	 * <ul>
	 * <li>Merging multiple model files (imports)</li>
	 * <li>Resolving cross-references between model elements</li>
	 * <li>Validating model structure and semantics</li>
	 * <li>Building the species hierarchy</li>
	 * <li>Collecting documentation metadata</li>
	 * </ul>
	 *
	 * @param projectPath
	 *            the absolute path of the project containing the model (workspace-relative)
	 * @param modelPath
	 *            the absolute path of the main model file being compiled
	 * @param models
	 *            the parsed syntactic elements representing the model(s) and any imported files
	 * @param collector
	 *            the validation context for collecting and reporting errors, warnings, and info messages
	 * @param doc
	 *            the documentation context for extracting help information (may be null)
	 * @param mm
	 *            a map of model paths to their descriptions, used for resolving imports and avoiding recompilation
	 * @return the created IModelDescription, or null if compilation failed
	 */
	IModelDescription createModelDescription(final String projectPath, final String modelPath,
			final Iterable<ISyntacticElement> models, final IValidationContext collector, IDocumentationContext doc,
			final Map<String, IModelDescription> mm);

}
