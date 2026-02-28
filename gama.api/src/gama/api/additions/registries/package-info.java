/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The registries package provides registry implementations for managing GAML additions.
 * 
 * <p>This package contains the concrete registry classes that store and provide access to
 * various types of GAML additions including operators, types, skills, statements, and other
 * language extensions.</p>
 * 
 * <h2>Registry Types</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.additions.registries.GamaAdditionRegistry} - Main registry for all GAML additions</li>
 *   <li>{@link gama.api.additions.registries.GamaSkillRegistry} - Registry for agent skills</li>
 *   <li>{@link gama.api.additions.registries.ArtefactRegistry} - Registry for action/operator prototypes</li>
 * </ul>
 * 
 * <h2>Registry Functions</h2>
 * 
 * <p>Registries provide:</p>
 * <ul>
 *   <li>Registration of new language elements</li>
 *   <li>Lookup by name or type</li>
 *   <li>Iteration over registered elements</li>
 *   <li>Validation of additions</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>Registries are populated during platform initialization and are read-only during
 * normal operation, making them safe for concurrent access.</p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.additions.registries.GamaAdditionRegistry
 * @see gama.api.additions.AbstractGamlAdditions
 */
package gama.api.additions.registries;
