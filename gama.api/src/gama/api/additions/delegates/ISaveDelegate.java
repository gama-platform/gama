/*******************************************************************************************************
 *
 * ISaveDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.delegates;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.SaveOptions;

/**
 * Delegate interface for extending GAMA's 'save' statement with custom file format support.
 * 
 * <p>This interface allows plugins to add support for saving GAML data to various file formats.
 * When a 'save' statement is executed, the platform selects the appropriate ISaveDelegate based
 * on the file type and the type of data being saved.</p>
 * 
 * <h2>Delegate Selection Process</h2>
 * <p>During execution of a 'save' statement:</p>
 * <ol>
 *   <li>The file extension is extracted from the target file path</li>
 *   <li>The platform checks {@link #getFileTypes()} to find delegates that support this extension</li>
 *   <li>Among matching delegates, {@link #handlesDataType(IType)} is called to verify data type compatibility</li>
 *   <li>The delegate with the closest type match is selected (based on type distance)</li>
 *   <li>{@link #save(IScope, IExpression, File, SaveOptions)} is called to perform the save operation</li>
 * </ol>
 * 
 * <h2>Type-Based Selection</h2>
 * <p>Delegates can be generic (handling any data type for a file format) or specific
 * (handling only certain data types). Use {@link #getDataType()} to specify a required
 * data type, or return {@link Types#NO_TYPE} to handle any type.</p>
 * 
 * <h2>File Type Synonyms</h2>
 * <p>Some file formats have multiple common extensions (e.g., "txt" and "text", "jpg" and "jpeg").
 * Use {@link #getSynonyms()} to declare these equivalences.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class CSVSaveDelegate implements ISaveDelegate {
 *     @Override
 *     public void save(IScope scope, IExpression item, File file, SaveOptions options) 
 *             throws IOException {
 *         Object data = item.value(scope);
 *         if (data instanceof IMatrix) {
 *             saveMatrix((IMatrix) data, file, options);
 *         } else if (data instanceof IList) {
 *             saveList((IList) data, file, options);
 *         }
 *     }
 *     
 *     @Override
 *     public Set<String> getFileTypes() {
 *         return Set.of("csv");
 *     }
 *     
 *     @Override
 *     public IType getDataType() {
 *         return Types.NO_TYPE; // Handle any type
 *     }
 * }
 * }</pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see ICreateDelegate
 * @see IDrawDelegate
 */
public interface ISaveDelegate {

	/** Empty synonym map for delegates that don't define synonyms. */
	BiMap<String, String> EMPTY_SYNONYMS = ImmutableBiMap.of();

	/**
	 * Saves data to a file in the format supported by this delegate.
	 * 
	 * <p>This method is responsible for converting GAML data into the target file format
	 * and writing it to disk. The implementation should handle all aspects of the save
 * operation, including:</p>
	 * <ul>
	 *   <li>Data conversion and formatting</li>
	 *   <li>Header generation (if applicable and requested via options)</li>
	 *   <li>Attribute selection and ordering</li>
	 *   <li>Coordinate system transformation (if applicable)</li>
	 *   <li>Error handling and resource management</li>
	 * </ul>
	 *
	 * @param scope the runtime scope providing access to the simulation context
	 * @param item the expression representing the data to save
	 * @param file the target file to write to
	 * @param saveOptions options controlling the save operation (headers, attributes, CRS, etc.)
	 * @throws IOException if an error occurs during file writing
	 */
	void save(IScope scope, IExpression item, File file, SaveOptions saveOptions) throws IOException;

	/**
	 * Returns the GAML type required for data saved by this delegate.
	 * 
	 * <p>If a specific type is returned (not {@link Types#NO_TYPE}), this delegate will
	 * only be selected for save operations involving that type. This allows multiple
	 * delegates to coexist for the same file format but different data types.</p>
	 * 
	 * <p>The default implementation returns {@link Types#NO_TYPE}, indicating that
	 * this delegate can potentially handle any data type (subject to {@link #handlesDataType(IType)}
	 * verification).</p>
	 *
	 * @return the required GAML data type, or {@link Types#NO_TYPE} for no specific requirement
	 */
	default IType getDataType() { return Types.NO_TYPE; }

	/**
	 * Determines whether this delegate can handle the specified data type.
	 * 
	 * <p>This method provides fine-grained control over type compatibility. Even if
	 * {@link #getDataType()} returns {@link Types#NO_TYPE}, this method can restrict
	 * which types are actually supported.</p>
	 * 
	 * <p>The default implementation returns true, accepting all types. Override this
	 * to restrict to specific types or type categories.</p>
	 *
	 * @param request the data type being requested for save
	 * @return true if this delegate can save data of the requested type, false otherwise
	 */
	default boolean handlesDataType(final IType request) {
		return true;
	}

	/**
	 * Returns the set of file type identifiers (extensions) supported by this delegate.
	 * 
	 * <p>File types are typically lowercase file extensions without the dot (e.g., "csv",
	 * "json", "shp"). These identifiers are used to match save statements with appropriate
	 * delegates based on the target file's extension.</p>
	 * 
	 * <p>A delegate can support multiple file types by returning a set with multiple elements.</p>
	 *
	 * @return the set of supported file type identifiers
	 */
	Set<String> getFileTypes();

	/**
	 * Returns pairs of synonymous file type identifiers.
	 * 
	 * <p>Some file formats are commonly known by multiple extensions. This method allows
	 * delegates to declare these equivalences, ensuring that save statements work with
	 * any of the synonymous extensions.</p>
	 * 
	 * <p>For example, a text file delegate might return a BiMap containing ("txt", "text")
	 * so that both extensions are recognized as equivalent.</p>
	 * 
	 * <p>The default implementation returns an empty BiMap. Use {@link #EMPTY_SYNONYMS}
	 * for convenience.</p>
	 *
	 * @return a bidirectional map of file type synonyms, or an empty map if none
	 */
	default BiMap<String, String> getSynonyms() { return EMPTY_SYNONYMS; }

}
