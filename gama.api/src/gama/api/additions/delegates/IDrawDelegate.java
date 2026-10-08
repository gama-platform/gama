/*******************************************************************************************************
 *
 * IDrawDelegate.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.delegates;

import java.awt.geom.Rectangle2D;

import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.ui.displays.DrawingData;
import gama.api.ui.displays.IGraphicsScope;

/**
 * Delegate interface for extending the GAMA 'draw' statement with custom drawable types.
 * 
 * <p>This interface allows plugins to extend the GAMA display system by providing custom
 * rendering logic for specific types of objects. When a 'draw' statement is executed,
 * the platform selects the appropriate IDrawDelegate based on the type of object being drawn.</p>
 * 
 * <h2>Delegate Selection Process</h2>
 * <p>During compilation and execution of a 'draw' statement:</p>
 * <ol>
 *   <li>The platform checks {@link #typeDrawn()} to find delegates that handle the object's type</li>
 *   <li>During compilation, {@link #validate(IDescription, IExpression)} is called to check the statement</li>
 *   <li>During execution, {@link #executeOn(IGraphicsScope, DrawingData, IExpression...)} is called to render</li>
 * </ol>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class ImageDrawDelegate implements IDrawDelegate {
 *     @Override
 *     public Rectangle2D executeOn(IGraphicsScope scope, DrawingData data, IExpression... items) {
 *         IImage image = (IImage) items[0].value(scope);
 *         // Custom rendering logic for images
 *         return scope.getGraphics().drawImage(image, data);
 *     }
 *     
 *     @Override
 *     public IType<?> typeDrawn() {
 *         return Types.IMAGE;
 *     }
 * }
 * }</pre>
 * 
 * @author drogoul
 * @since 27 mai 2015
 * 
 * @see ICreateDelegate
 * @see ISaveDelegate
 */
public interface IDrawDelegate {

	/**
	 * Executes the drawing operation for the specified items.
	 * 
	 * <p>This method is called during the execution of a 'draw' statement to render
	 * the drawable object. The method receives the graphics scope, drawing attributes
	 * (color, size, rotation, etc.), and the items to draw.</p>
	 * 
	 * <p>The returned Rectangle2D represents the bounding box of what was drawn, which
	 * can be used for display optimization and event handling.</p>
	 *
	 * @param scope the graphics scope providing access to the rendering context
	 * @param data the drawing data containing attributes like color, size, rotation, etc.
	 * @param items the expressions representing the items to draw (typically one expression)
	 * @return the bounding rectangle of the drawn content
	 * @throws GamaRuntimeException if an error occurs during rendering
	 */
	Rectangle2D executeOn(IGraphicsScope scope, DrawingData data, IExpression... items) throws GamaRuntimeException;

	/**
	 * Returns the GAML type that this delegate can draw.
	 * 
	 * <p>This type is used to select the appropriate delegate for a given draw statement.
	 * The type should not be null or {@link IType#NO_TYPE}, and should be considered
	 * drawable (see {@link IType#isDrawable()}).</p>
	 * 
	 * <p>Multiple delegates can handle the same type, in which case the platform may
	 * use additional criteria to select the most appropriate one.</p>
	 *
	 * @return the GAML type that this delegate can render (e.g., Types.GEOMETRY, Types.IMAGE)
	 */
	IType<?> typeDrawn();

	/**
	 * Validates a draw statement during compilation.
	 * 
	 * <p>This method is called by the compiler when validating a draw statement that
	 * uses this delegate. It can perform type checking, validate attribute combinations,
	 * or emit warnings/errors if the statement is incorrectly formed.</p>
	 * 
	 * <p>The default implementation does nothing, which is appropriate for simple cases.</p>
	 *
	 * @param currentDrawStatement the description of the draw statement being validated
	 * @param item the expression being drawn
	 */
	default void validate(final IDescription currentDrawStatement, final IExpression item) {}

}
