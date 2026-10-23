/**
 *
 */
package gama.api.compilation.descriptions;

import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.kernel.object.IObjectConstructor;

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

	@Override
	Class<? extends IObject> getJavaBase();

	/**
	 * Returns the Java instance factory, inherited by model-defined subclasses when applicable.
	 */
	IObjectConstructor getObjectConstructor();
}
