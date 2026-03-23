/**
 *
 */
package gama.api.compilation.descriptions;

import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.kernel.object.IClass;

/**
 *
 */
public interface IClassDescription extends ITypeDescription {

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	@Override
	IClassDescription getParent();

	/**
	 * @param result
	 */
	void documentThis(IGamlDocumentation result);

	/**
	 * @return
	 */
	IClass compileAsBuiltIn();

}
