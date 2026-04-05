/*******************************************************************************************************
 *
 * GMLContentDescriber.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

import gama.annotations.constants.IKeyword;

/**
 * {@code GMLContentDescriber} is an Eclipse content describer that distinguishes <em>Geography Markup Language</em>
 * (GML) files — as defined by the Open Geospatial Consortium (OGC) — from other XML-based formats that happen to share
 * the {@code .gml} file extension, most notably <em>Graph Markup Language</em> files used by tools such as yEd or JUNG.
 *
 * <p>
 * The describer reads the full text content of the file and checks whether it starts with the word
 * {@link IKeyword#GRAPH}. Graph Markup Language files begin with a {@code <graphml>} root element, whose tag name
 * starts with that keyword. OGC Geography GML files, by contrast, use root elements such as {@code <FeatureCollection>}
 * or {@code <wfs:FeatureCollection>} and never start with that word.
 * </p>
 *
 * <p>
 * This class is registered as a describer for the {@code gama.gml.file.type} content type in {@code plugin.xml}.
 * Eclipse's content-type framework then only assigns that type to files whose content passes this check, so
 * {@link FileMetaDataProvider#getContentTypeId} correctly returns
 * {@link gama.api.utils.files.IFileMetadataProvider#GML_CT_ID} only for genuine Geography GML files, and never
 * attempts to build geographic metadata for Graph Markup Language files.
 * </p>
 *
 * <h2>Detection logic</h2>
 * <ol>
 * <li>Read the entire file content as a string via the supplied {@link Reader} (the {@link InputStream} overload
 * delegates to the {@link Reader} one).</li>
 * <li>If the content starts with {@link IKeyword#GRAPH} (case-sensitive), return
 * {@link ITextContentDescriber#INVALID} — this is a Graph Markup Language file.</li>
 * <li>Otherwise return {@link ITextContentDescriber#VALID} — treat the file as Geography GML.</li>
 * <li>If any exception occurs during reading, return {@link ITextContentDescriber#INVALID} as a safe fallback.</li>
 * </ol>
 *
 * @author drogoul
 * @since 2026-04-05
 */
public class GMLContentDescriber implements ITextContentDescriber {

	/**
	 * Describes the content of the supplied {@link InputStream} by delegating to
	 * {@link #describe(Reader, IContentDescription)} via an {@link InputStreamReader}.
	 *
	 * <p>
	 * The stream is not closed by this method; the caller retains ownership.
	 * </p>
	 *
	 * @param contents
	 *            the input stream to inspect; must not be {@code null}
	 * @param description
	 *            the content description to populate (may be {@code null})
	 * @return {@link ITextContentDescriber#VALID} if the stream contains OGC Geography GML content,
	 *         {@link ITextContentDescriber#INVALID} if it starts with {@link IKeyword#GRAPH} or any error occurs
	 * @throws IOException
	 *             if an I/O error occurs while reading the stream
	 */
	@Override
	public int describe(final InputStream contents, final IContentDescription description) throws IOException {
		return describe(new InputStreamReader(contents), description);
	}

	/**
	 * Describes the content of the supplied {@link Reader}.
	 *
	 * <p>
	 * Reads the full content of the file and checks whether it starts with the keyword {@link IKeyword#GRAPH}
	 * (case-sensitive). Graph Markup Language files begin with a {@code <graphml>} element whose tag name starts with
	 * that keyword, so they are rejected. All other files — including every variant of OGC Geography GML — are
	 * accepted.
	 * </p>
	 *
	 * <p>
	 * The reader is not closed by this method; the caller retains ownership.
	 * </p>
	 *
	 * @param contents
	 *            the character reader to inspect; must not be {@code null}
	 * @param description
	 *            the content description to populate (may be {@code null})
	 * @return {@link ITextContentDescriber#VALID} if the content does <em>not</em> start with
	 *         {@link IKeyword#GRAPH}, {@link ITextContentDescriber#INVALID} if it does or if any exception
	 *         occurs during reading
	 * @throws IOException
	 *             if an I/O error occurs while reading
	 */
	@Override
	public int describe(final Reader contents, final IContentDescription description) throws IOException {
		try {
			// if the contents begins with "graph" we return INVALID otherwise VALID
			if (contents.readAllAsString().startsWith(IKeyword.GRAPH)) return INVALID;
			return VALID;
		} catch (final Exception e) {
			return INVALID;
		}
	}

	/**
	 * Returns the set of properties that this describer can populate on a {@link IContentDescription}. This
	 * implementation does not populate any properties, so an empty array is returned.
	 *
	 * @return an empty {@link QualifiedName} array
	 */
	@Override
	public QualifiedName[] getSupportedOptions() { return new QualifiedName[0]; }

}
