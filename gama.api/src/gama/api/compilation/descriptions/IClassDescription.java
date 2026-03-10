/**
 *
 */
package gama.api.compilation.descriptions;

import gama.api.compilation.documentation.IGamlDocumentation;

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

}
