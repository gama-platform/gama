/*******************************************************************************************************
 *
 * GamaPropertyFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.GamaFile;
import gama.api.utils.list.GamaListFactory;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.support.IConcept;

/**
 * The Class GamaPropertyFile.
 */
@file (
		name = "property",
		extensions = { "properties" },
		buffer_type = IType.MAP,
		buffer_content = IType.STRING,
		buffer_index = IType.STRING,
		concept = { IConcept.FILE },
		doc = @doc ("Represents property files"))
public class GamaPropertyFile extends GamaFile<IMap<String, String>, String> {

	/**
	 * Instantiates a new gama property file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a property file (.properties)",
			examples = { @example (
					value = "file f <-property_file(\"file.properties\");",
					isExecutable = false) })
	public GamaPropertyFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Instantiates a new gama property file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param buffer
	 *            the buffer
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to store a map in a property file (it does not save it - just store it in memory)",
			examples = { @example (
					value = "file f <-property_file(\"file.properties\", map([\"param1\"::1.0,\"param3\"::10.0 ]));",
					isExecutable = false) })
	public GamaPropertyFile(final IScope scope, final String pathName, final IMap<String, String> buffer)
			throws GamaRuntimeException {
		super(scope, pathName, buffer);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.util.GamaFile#fillBuffer()
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		final Properties p = new Properties();
		final IMap m = GamaMapFactory.create(Types.STRING, Types.STRING);
		try (FileReader f = new FileReader(getFile(scope))) {
			p.load(f);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
		m.putAll(p);
		setBuffer(m);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// TODO return the keys of the map as "attributes"
		return GamaListFactory.getEmptyList();
	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		final Properties p = new Properties();
		if (getBuffer() != null && !getBuffer().isEmpty()) { getBuffer().forEach((a, b) -> { p.setProperty(a, b); }); }
		try (FileWriter fw = new FileWriter(getFile(scope))) {
			p.store(fw, null);
		} catch (final IOException e) {}

	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		// TODO Probably possible to get some information there
		return null;
	}

}
