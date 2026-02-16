/*******************************************************************************************************
 *
 * IGamaFile.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.file;

import org.eclipse.emf.common.util.URI;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ITypeProvider;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.ui.displays.IAsset;
import gama.api.utils.geometry.IEnvelopeProvider;

/**
 * Written by drogoul Modified on 14 nov. 2011
 *
 * @todo Description
 *
 * @param <K>
 * @param <V>
 */
@vars ({ @variable (
		name = IKeyword.NAME,
		type = IType.STRING,
		doc = { @doc ("Returns the name of the receiver file") }),
		@variable (
				name = IKeyword.EXTENSION,
				type = IType.STRING,
				doc = { @doc ("Returns the extension of the receiver file") }),
		@variable (
				name = IKeyword.PATH,
				type = IType.STRING,
				doc = { @doc ("Returns the absolute path of the receiver file") }),
		@variable (
				name = IKeyword.EXISTS,
				type = IType.BOOL,
				doc = { @doc ("Returns whether the receiver file exists or not in the filesystem") }),
		@variable (
				name = IKeyword.ISFOLDER,
				type = IType.BOOL,
				doc = { @doc ("Returns whether the receiver file is a folder or not") }),
		@variable (
				name = IKeyword.READABLE,
				type = IType.BOOL,
				doc = { @doc ("Returns true if the contents of the receiver file can be read") }),
		@variable (
				name = IKeyword.WRITABLE,
				type = IType.BOOL,
				doc = { @doc ("Returns true if the contents of the receiver file can be written") }),
		@variable (
				name = IKeyword.ATTRIBUTES,
				type = IType.LIST,
				of = IType.STRING,
				doc = { @doc ("Retrieves the list of 'attributes' present in the receiver files that support this concept (and an empty list for the others). For instance, in a CSV file, the attributes represent the headers of the columns (if any); in a shape file, the attributes provided to the objects, etc.") }),
		@variable (
				name = IKeyword.CONTENTS,
				type = ITypeProvider.WRAPPED,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				index = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the contents of the receiver file in the form of a container") }) })
@SuppressWarnings ({ "rawtypes" })
public interface IGamaFile<C extends IContainer.Modifiable, Contents>
		extends IContainer.Addressable, IContainer.Modifiable, IEnvelopeProvider, IAsset {

	/**
	 * A tagging interface for drawable files
	 */
	interface Drawable {

	}

	/**
	 * The Interface Geometry.
	 */
	interface WithGeometry extends IGamaFile<IList<IShape>, IShape>, Drawable {

		/**
		 * @param scope
		 * @return
		 */
		IShape getGeometry(IScope scope);

	}

	/**
	 * The "temporary output" key. Used to indicate in the scope (see {@link IScope#setData(String, Object)} that the
	 * current file is created for serving as an output file, for saving data
	 */
	String KEY_TEMPORARY_OUTPUT = "key_temporary_output";

	/**
	 * Sets the writable.
	 *
	 * @param scope
	 *            the scope
	 * @param w
	 *            the w
	 */
	void setWritable(IScope scope, final boolean w);

	/**
	 * Sets the contents.
	 *
	 * @param cont
	 *            the new contents
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void setContents(final C cont) throws GamaRuntimeException;

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i gama file
	 */
	@Override
	IGamaFile copy(IScope scope);

	/**
	 * Gets the buffer.
	 *
	 * @return the buffer
	 */
	C getBuffer();

	/**
	 * Exists.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = IKeyword.EXISTS,
			initializer = true)
	Boolean exists(IScope scope);

	/**
	 * Gets the extension.
	 *
	 * @param scope
	 *            the scope
	 * @return the extension
	 */
	@getter (
			value = IKeyword.EXTENSION,
			initializer = true)
	String getExtension(IScope scope);

	/**
	 * Gets the name.
	 *
	 * @param scope
	 *            the scope
	 * @return the name
	 */
	@getter (
			value = IKeyword.NAME,
			initializer = true)
	String getName(IScope scope);

	/**
	 * Gets the path.
	 *
	 * @param scope
	 *            the scope
	 * @return the path
	 */
	@getter (
			value = IKeyword.PATH,
			initializer = true)
	String getPath(IScope scope);

	/**
	 * Gets the contents.
	 *
	 * @param scope
	 *            the scope
	 * @return the contents
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@getter (IKeyword.CONTENTS)
	C getContents(IScope scope) throws GamaRuntimeException;

	/**
	 * Gets the attributes.
	 *
	 * @param scope
	 *            the scope
	 * @return the attributes
	 */
	@getter (IKeyword.ATTRIBUTES)
	/**
	 * Retrieves the list of "attributes" present in files that support this concept (and an empty list for the others).
	 * For instance, in a CSV file, attributes represent the headers of the columns (if any); in a shape file, the
	 * attributes provided to the objects, etc.
	 *
	 * @param scope
	 * @return a list of string or an empty list (never null)
	 */
	IList<String> getAttributes(IScope scope);

	/**
	 * Checks if is folder.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = IKeyword.ISFOLDER,
			initializer = true)
	Boolean isFolder(IScope scope);

	/**
	 * Checks if is readable.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = IKeyword.READABLE,
			initializer = true)
	Boolean isReadable(IScope scope);

	/**
	 * Checks if is writable.
	 *
	 * @param scope
	 *            the scope
	 * @return the boolean
	 */
	@getter (
			value = IKeyword.WRITABLE,
			initializer = true)
	Boolean isWritable(IScope scope);

	/**
	 * Save.
	 *
	 * @param scope
	 *            the scope
	 * @param parameters
	 *            the parameters
	 */
	void save(IScope scope, Facets parameters);

	/**
	 * Gets the original path.
	 *
	 * @return the original path
	 */
	String getOriginalPath();

	/**
	 * Contains key.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	default boolean containsKey(final IScope scope, final Object o) {
		final C contents = getContents(scope);
		return contents != null && contents.contains(scope, o);
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Override
	default String getId() { return this.getOriginalPath(); }

	/**
	 * Gets the URI relative to workspace.
	 *
	 * @return the URI relative to workspace
	 */
	URI getURIRelativeToWorkspace();

	/**
	 * Checks for geo data available.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if successful
	 * @date 15 juil. 2023
	 */
	default boolean hasGeoDataAvailable(final IScope scope) {
		return false;
	}

	/**
	 * Ensure contents is compatible.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param contents
	 *            the contents
	 * @return the i modifiable container
	 * @date 4 nov. 2023
	 */
	default Modifiable ensureContentsIsCompatible(final Modifiable contents) {
		return contents;
	}

	/**
	 * Invalidate contents.
	 */
	void invalidateContents();

}