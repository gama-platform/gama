/*******************************************************************************************************
 *
 * GamaJsonFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.support.IConcept;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFile;
import gama.api.types.misc.IRuntimeContainer;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.json.IJson;
import gama.core.util.json.ParseException;

/**
 * The Class GamaJsonFile.
 */
@file (
		name = "json",
		extensions = { "json" },
		buffer_type = IType.MAP,
		buffer_index = IType.STRING,
		concept = { IConcept.FILE })
@doc ("Reads a JSON file into a map<string, unknown>. Either a direct map of the object denoted in the JSON file, or a map with only one key ('"
		+ IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL
		+ "') containing the list in the JSON file. All data structures (JSON object and JSON array) are properly converted into GAMA structures (map and list) recursively, or into direct GAMA objects when they sport the required tags. ")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class GamaJsonFile extends GamaFile<IMap<String, Object>, Object> {

	/**
	 * Instantiates a new gama json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a json file",
			examples = { @example (
					value = "file f <-json_file(\"file.json\");",
					isExecutable = false) })

	public GamaJsonFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Instantiates a new gama json file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param container
	 *            the container
	 */
	@doc (
			value = "This constructor allows to  store a map in a json file (it does not save it). The file can then be saved later using the `save` statement",
			examples = { @example (
					value = "file f <-json_file(\"file.json\", map([\"var1\"::1.0, \"var2\"::3.0]));",
					isExecutable = false) })

	public GamaJsonFile(final IScope scope, final String pathName, final IMap<String, Object> container) {
		super(scope, pathName, container);
	}

	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return null;
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		try (FileReader reader = new FileReader(getFile(scope))) {
			final IMap<String, Object> map;
			final Object o = GAMA.getJsonEncoder().parse(reader).toGamlValue(scope);
			if (o instanceof IMap) {
				map = (IMap<String, Object>) o;
			} else {
				map = GamaMapFactory.create();
				map.put(IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL, o);
			}
			setBuffer(map);
		} catch (final IOException | ParseException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	@Override
	protected String getHttpContentType() { return "application/json"; }

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		final IMap<String, Object> map = getBuffer();
		Object toSave = map;
		if (map.size() == 1 && map.containsKey(IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL)) {
			toSave = map.get(IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL);
		}
		final File file = getFile(scope);
		if (file.exists()) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning(file.getName() + " already exists", scope),
					false);
		}
		try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
			writer.write(GAMA.getJsonEncoder().valueOf(toSave).toPrettyPrint());
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	@Override
	public IRuntimeContainer.Modifiable ensureContentsIsCompatible(final IRuntimeContainer.Modifiable contents) {
		if (contents instanceof IMap) return contents;
		IMap map = GamaMapFactory.create();
		map.put(IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL, contents);
		return map;
	}

}
