/*******************************************************************************************************
 *
 * GamaFolderFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.files;

import static gama.api.data.factories.GamaListFactory.createWithoutCasting;

import java.io.File;

import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IList;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.GamaFileType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;

/**
 * The Class GamaFolderFile.
 */
public class GamaFolderFile extends GamaFile<IList<String>, String> {

	/**
	 * Instantiates a new gama folder file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GamaFolderFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
		// AD 27/04/13 Let the flags of the file remain the same. Can be turned
		// off and on using the "read" and
		// "write" operators, so no need to decide for a default here
		// setWritable(true);
	}

	/**
	 * Instantiates a new gama folder file.
	 *
	 * @param scope
	 *            the scope
	 * @param pn
	 *            the pn
	 * @param forReading
	 *            the for reading
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GamaFolderFile(final IScope scope, final String pn, final boolean forReading) throws GamaRuntimeException {
		super(scope, pn, forReading);
	}

	@Override
	protected void checkValidity(final IScope scope) throws GamaRuntimeException {
		final File file = getFile(scope);
		if (file == null || !file.exists()) throw GamaRuntimeException.error(
				"The folder " + getFile(scope).getAbsolutePath() + " does not exist. Please use 'new_folder' instead",
				scope);
		if (!getFile(scope).isDirectory())
			throw GamaRuntimeException.error(getFile(scope).getAbsolutePath() + "is not a folder", scope);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return IKeyword.FOLDER + "('" + /* StringUtils.toGamlString(getPath()) */getPath(null) + "')";
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// No attributes to speak of
		return GamaListFactory.create(Types.STRING);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		final String[] list = getFile(scope).list();
		final IList<String> result =
				list == null ? GamaListFactory.getEmptyList() : createWithoutCasting(Types.STRING, list);
		setBuffer(result);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.util.GamaFile#flushBuffer()
	 */
	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		// Nothing to do
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		final IContainer<Integer, String> files = getContents(scope);
		IEnvelope globalEnv = null;
		for (final String s : files.iterable(scope)) {
			final IGamaFile f = GamaFileType.createFile(scope, s, true, null);
			if (f != null) {
				final IEnvelope env = f.computeEnvelope(scope);
				if (globalEnv == null) {
					globalEnv = env;
				} else {
					globalEnv.expandToInclude(env);
				}
			}
		}
		return globalEnv;
	}

}
