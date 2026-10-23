/**
 *
 */
package gama.api.compilation.descriptions;

import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.kernel.object.IConstructor;

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
	 *
	 * @return the object or agent factory, or {@code null} when no Java factory applies
	 */
	IConstructor<? extends IObject, ?, ?> getConstructor();
}
