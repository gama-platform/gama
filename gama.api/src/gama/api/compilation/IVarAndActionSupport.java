/*******************************************************************************************************
 *
 * IVarAndActionSupport.java, in gama.annotations, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.compilation;

/**
 * Marker interface indicating that a class supports both variables and actions.
 * 
 * <p>
 * This interface serves as a common type marker for classes that can contain both variable declarations
 * and action definitions. It provides a unified abstraction for entities in the GAMA platform that
 * support these two fundamental features.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * The interface enables polymorphic handling of both agents ({@code IAgent}) and skills ({@code ISkill}),
 * which share the capability of having:
 * </p>
 * <ul>
 *   <li><strong>Variables:</strong> Attributes that store state</li>
 *   <li><strong>Actions:</strong> Behaviors that can be executed</li>
 * </ul>
 * 
 * <h2>Implementation Hierarchy</h2>
 * 
 * <p>
 * This interface is typically implemented by:
 * </p>
 * <ul>
 *   <li><strong>IAgent:</strong> Individual simulation agents that have attributes and behaviors</li>
 *   <li><strong>ISkill:</strong> Skill modules that augment agents with additional attributes and behaviors</li>
 * </ul>
 * 
 * <h2>Usage Context</h2>
 * 
 * <p>
 * This interface is used in contexts where code needs to work with entities that support variables and
 * actions without caring whether they are agents or skills. Examples include:
 * </p>
 * <ul>
 *   <li>Compilation and description building processes</li>
 *   <li>Generic variable and action lookup mechanisms</li>
 *   <li>Reflection and introspection utilities</li>
 *   <li>Documentation generation</li>
 * </ul>
 * 
 * <h2>Design Pattern</h2>
 * 
 * <p>
 * This is a marker interface (no methods) following the Marker Interface Pattern. It provides
 * type information and allows for polymorphic behavior without requiring any specific method
 * implementations.
 * </p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * public void processVarAndActionSupport(IVarAndActionSupport entity) {
 *     // This method can work with both IAgent and ISkill instances
 *     if (entity instanceof IAgent) {
 *         IAgent agent = (IAgent) entity;
 *         // Process agent-specific features
 *     } else if (entity instanceof ISkill) {
 *         ISkill skill = (ISkill) entity;
 *         // Process skill-specific features
 *     }
 * }
 * }</pre>
 * 
 * @author drogoul
 * @since 6 janv. 2016
 * @version 2025-03
 * 
 * @see gama.core.runtime.IAgent
 * @see gama.core.common.interfaces.ISkill
 * @see gama.api.compilation.descriptions.IVarDescriptionProvider
 * @see gama.api.compilation.descriptions.IVarDescriptionUser
 */
public interface IVarAndActionSupport {

}
