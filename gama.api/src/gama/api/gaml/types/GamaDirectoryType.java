/*******************************************************************************************************
 *
 * GamaDirectoryType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.types.file.GamaFolderFile;

/**
 * Represents the GAML directory (folder) type.
 * <p>
 * This is a specialized file type specifically for directories/folders. Directory objects contain
 * the list of file names within the folder. They are container types with STRING content (file names)
 * indexed by integers.
 * </p>
 * 
 * @author taillandier
 * @since GAMA 2021
 * @see GamaFileType
 * @see GamaFolderFile
 */
@type (
		name = IKeyword.DIRECTORY,
		id = IType.DIRECTORY,
		wraps = { GamaFolderFile.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.FILE },
		doc = @doc ("specific type for directories (folders). Contains the list of file names"))
public class GamaDirectoryType extends GamaFileType {

	/**
	 * Constructs a new GamaDirectoryType.
	 * 
	 * @param typesManager the types manager for type resolution
	 */
	public GamaDirectoryType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Gets the content type for directories.
	 * <p>
	 * Directories contain strings (file names).
	 * </p>
	 * 
	 * @return the STRING type
	 */
	@Override
	public IType<?> getContentType() { return Types.STRING; }

	/**
	 * Gets the key type for directories.
	 * <p>
	 * Directory contents are indexed by integers.
	 * </p>
	 * 
	 * @return the INT type
	 */
	@Override
	public IType<?> getKeyType() { return Types.INT; }

}