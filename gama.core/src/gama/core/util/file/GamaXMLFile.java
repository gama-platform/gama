/*******************************************************************************************************
 *
 * GamaXMLFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GamaFile;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.geometry.IEnvelope;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.support.IConcept;

/**
 * Class GamaXMLFile. TODO: Everything ! What kind of buffer should be returned from here ? The current implementation
 * does not make any sense at all.
 *
 * @author drogoul
 * @since 9 janv. 2014
 *
 */
@file (
		name = IKeyword.XML,
		extensions = "xml",
		buffer_type = IType.MAP,
		concept = { IConcept.FILE, IConcept.XML },
		doc = @doc ("Represents XML files. The internal representation is a list of strings"))
public class GamaXMLFile extends GamaFile<IMap<String, String>, String> {

	/**
	 * @param scope
	 * @param pathName
	 * @throws GamaRuntimeException
	 */
	@doc (
			value = "This file constructor allows to read a xml file",
			examples = { @example (
					value = "file f <-xml_file(\"file.xml\");",
					isExecutable = false) })
	public GamaXMLFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/**
	 * Gets the root tag.
	 *
	 * @param scope
	 *            the scope
	 * @return the root tag
	 */
	public String getRootTag(final IScope scope) {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = factory.newDocumentBuilder();
			final Document doc = db.parse(new File(this.getPath(scope)));
			return doc.getFirstChild().getNodeName();
		} catch (final ParserConfigurationException | SAXException | IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// TODO depends on the contents...
		return GamaListFactory.create(Types.STRING);
	}

	/**
	 * Method computeEnvelope()
	 *
	 * @see gama.api.types.file.IGamaFile#computeEnvelope(gama.api.runtime.scope.IScope)
	 */
	@Override
	public IEnvelope computeEnvelope(final IScope scope) {
		return null;
	}

	/**
	 * Method fillBuffer()
	 *
	 * @see gama.api.types.file.GamaFile#fillBuffer(gama.api.runtime.scope.IScope)
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		try (final BufferedReader in = new BufferedReader(new FileReader(getFile(scope)))) {
			final IMap<String, String> allLines = GamaMapFactory.create(Types.STRING, Types.STRING);
			String str = in.readLine();
			while (str != null) {
				allLines.put(str, str + "\n");
				str = in.readLine();
			}
			setBuffer(allLines);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

}
