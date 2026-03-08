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
 * The skill package provides the skill system for reusable agent behaviors.
 * 
 * <p>This package contains interfaces and base classes for implementing skills, which are
 * modular, reusable sets of attributes and actions that can be attached to species to extend
 * their capabilities.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.kernel.skill.ISkill} - Base interface for all skills</li>
 * </ul>
 * 
 * <h2>Skill Capabilities</h2>
 * 
 * <p>Skills provide:</p>
 * <ul>
 *   <li><strong>Additional Attributes:</strong> New variables for agents</li>
 *   <li><strong>Additional Actions:</strong> New behaviors for agents</li>
 *   <li><strong>Lifecycle Hooks:</strong> Initialization and disposal logic</li>
 *   <li><strong>State Management:</strong> Internal state specific to the skill</li>
 * </ul>
 * 
 * <h2>Built-in Skills</h2>
 * 
 * <p>GAMA provides several built-in skills:</p>
 * <ul>
 *   <li><strong>moving:</strong> Movement and navigation capabilities</li>
 *   <li><strong>communicating:</strong> Agent communication protocols</li>
 *   <li><strong>perception:</strong> Sensing and perception abilities</li>
 *   <li><strong>grid:</strong> Grid-specific behaviors</li>
 * </ul>
 * 
 * <h2>Custom Skills</h2>
 * 
 * <p>Custom skills can be created by:</p>
 * <ul>
 *   <li>Implementing the ISkill interface</li>
 *   <li>Using @skill annotation</li>
 *   <li>Defining actions with @action annotation</li>
 *   <li>Defining variables with @variable annotation</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // In GAML model
 * species my_species skills: [moving, communicating] {
 *     // Species can now use actions from these skills
 *     reflex move {
 *         do wander;  // From moving skill
 *     }
 * }
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.kernel.skill.ISkill
 * @see gama.api.additions
 */
package gama.api.kernel.skill;
