/*******************************************************************************************************
 *
 * GenericFile.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import gama.api.GAMA;
import gama.api.data.factories.GamaEnvelopeFactory;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IList;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.list.GamaListFactory;

/**
 * The Class GenericFile.
 */
public class GenericFile extends GamaFile<IList<String>, String> {

	/**
	 * Instantiates a new generic file.
	 *
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GenericFile(final String pathName) throws GamaRuntimeException {
		super(GAMA.getRuntimeScope(), pathName);
	}

	/**
	 * Instantiates a new generic file.
	 *
	 * @param pathName
	 *            the path name
	 * @param shouldExist
	 *            the should exist
	 */
	public GenericFile(final String pathName, final boolean shouldExist) {
		super(GAMA.getRuntimeScope(), pathName, shouldExist);
	}

	/**
	 * Instantiates a new generic file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GenericFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, false);
	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return GamaEnvelopeFactory.EMPTY;
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		try {
			if (isBinaryFile(scope)) {
				GAMA.reportAndThrowIfNeeded(scope,
						GamaRuntimeException.warning(
								"Problem identifying the contents of " + getFile(scope).getAbsolutePath(), scope),
						false);
				setBuffer(GamaListFactory.getEmptyList());
			} else {
				try (final BufferedReader in = new BufferedReader(new FileReader(getFile(scope)))) {
					final IList<String> allLines = GamaListFactory.create(Types.STRING);
					String str = in.readLine();
					while (str != null) {
						allLines.add(str);
						str = in.readLine();
					}
					setBuffer(allLines);
				}
			}
		} catch (IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}

	}

	/**
	 * Guess whether given file is binary. Just checks for anything under 0x09.
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public boolean isBinaryFile(final IScope scope) throws FileNotFoundException, IOException {
		File f = getFile(scope);
		if (f == null || !f.exists()) return false;
		try (InputStream in = Files.newInputStream(f.toPath())) {
			int ascii = 0;
			int other = 0;
			for (final byte b : in.readNBytes(Math.min(1024, in.available()))) {
				if (b < 0x09) return true;
				if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D || b >= 0x20 && b <= 0x7E) {
					ascii++;
				} else {
					other++;
				}
			}
			return other == 0 ? false : 100 * other / (ascii + other) > 95;
		}
	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {}

}