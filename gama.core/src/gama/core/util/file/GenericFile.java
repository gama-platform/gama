/*******************************************************************************************************
 *
 * GenericFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import gama.core.common.geometry.Envelope3D;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.statements.Facets;
import gama.gaml.types.IContainerType;
import gama.gaml.types.Types;

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
	public IContainerType<?> getGamlType() { return Types.FILE; }

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		return Envelope3D.EMPTY;
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
				setBuffer(GamaListFactory.EMPTY_LIST);
			} else {
				try (final BufferedReader in = new BufferedReader(new FileReader(getFile(scope)))) {
					final IList<String> allLines = GamaListFactory.create(Types.STRING);
					String str;
					str = in.readLine();
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
		try (FileInputStream in = new FileInputStream(f)) {
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