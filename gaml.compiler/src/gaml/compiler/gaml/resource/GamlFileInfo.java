/*******************************************************************************************************
 *
 * GamlFileInfo.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.resource;

import static gaml.compiler.gaml.resource.GamlResourceServices.getResourceSet;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IKeyword;
import gama.api.gaml.types.GamaFileType;
import gama.api.utils.files.AbstractFileMetaData;
import gama.api.utils.files.IGamlFileInfo;
import gama.dev.DEBUG;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;

/**
 * The Class GamlFileInfo.
 */
public class GamlFileInfo extends AbstractFileMetaData implements IGamlFileInfo {

	static {
		DEBUG.OFF();
	}
	/** The tokenizer for comments and strings. */
	public static final Pattern findCommentsAndStrings =
			Pattern.compile("//.*|(?s)/\\*.*?(?:\\*/|$)|'(?:\\\\.|[^'\\\\])*'|\"(?:\\\\.|[^\"\\\\])*\"");

	/** The find tags. */
	public static final Pattern findTags = Pattern.compile("Tags:\\s*(.*)");

	/** The find pragmas. */
	public static final Pattern findPragmas = Pattern.compile("(?m)^\\s*@([^\\s@]+)");

	/** The find experiments. */
	public static final Pattern findExperiments = Pattern.compile(
			"(?m)^\\s*experiment\\s+(?:'([^']*)'|\"([^\"]*)\"|(\\w+))(?:\\s+type:\\s*(\\w+))?(?:\\s+virtual:\\s*(\\w+))?");
	/** The find species. */
	public static final Pattern findSpecies = Pattern
			.compile("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|\\b(?:species|grid)\\s+([a-zA-Z_][a-zA-Z0-9_]*)");

	/** The batch prefix. */
	public static final String BATCH_PREFIX = "***";

	/** The errors. */
	public static final String ERRORS = "errors detected";

	/** The experiments. */
	private Collection<String> experiments;

	/** The species. */
	private Collection<String> species;

	/** The imports. */
	private Collection<String> imports;

	/** The uses. */
	private Collection<String> uses;

	/** The tags. */
	private Collection<String> tags;

	/** The invalid. */
	public final boolean invalid;

	/** The uri. */
	public URI uri;

	/**
	 * Instantiates a new gaml file info.
	 *
	 * @param file
	 *            the file
	 */
	public GamlFileInfo(final IFile file) {
		super(file);
		invalid = getModificationStamp() == Long.MAX_VALUE;
		createFrom(URI.createPlatformResourceURI(file.getFullPath().toOSString(), true));
	}

	/**
	 * Creates the from.
	 *
	 * @param originalURI
	 *            the original URI
	 */
	public void createFrom(final URI originalURI) {

		Set<String> tags = null;
		Set<String> exps = null;
		Set<String> specs = null;

		for (final URI u : GamlResourceIndexer.directImportsOf(originalURI)) {
			if (imports == null) { imports = new LinkedHashSet<>(); }
			imports.add(u.deresolve(originalURI).toString());
		}

		try (InputStream is = getResourceSet().getURIConverter().createInputStream(originalURI)) {
			// Read the entire file into a single string
			String content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			// 1. Sanitize the string by processing comments and strings first
			Matcher tokenizer = findCommentsAndStrings.matcher(content);
			StringBuilder sanitized = new StringBuilder();

			while (tokenizer.find()) {
				String match = tokenizer.group();

				if (match.startsWith("//") || match.startsWith("/*")) {
					// It's a comment: search for tags inside it
					Matcher tagsMatcher = findTags.matcher(match);
					if (tagsMatcher.find()) {
						String tagList = tagsMatcher.group(1);
						tags = new HashSet<>(asList(
								splitByWholeSeparatorPreserveAllTokens(uncapitalize(deleteWhitespace(tagList)), ",")));
					}
					// Blank out the comment to prevent false positives
					tokenizer.appendReplacement(sanitized, " ");
				} else {
					// It's a string: check if it represents a valid use/import
					if (match.length() > 6) {
						String s = match.substring(1, match.length() - 1);
						final URI u = URI.createFileURI(s);
						final String ext = u.fileExtension();
						if (ext != null && !ext.isBlank() && !"gaml".equals(ext)
								&& GamaFileType.managesExtension(ext)) {
							if (uses == null) { uses = new LinkedHashSet<>(); }
							DEBUG.OUT("===== Considers in uses : " + s);
							uses.add(s);
						}
					}
					// KEEP the string intact so findExperiments can read its name
					tokenizer.appendReplacement(sanitized, Matcher.quoteReplacement(match));
				}
			}
			tokenizer.appendTail(sanitized);
			String cleanText = sanitized.toString();

			// 2. Process Pragmas on the clean text
			boolean processExperiments = true;
			Matcher pragmasMatcher = findPragmas.matcher(cleanText);
			while (pragmasMatcher.find()) {
				String pragma = pragmasMatcher.group(1);
				if (IKeyword.PRAGMA_NO_EXPERIMENT.equals(pragma)) { processExperiments = false; }
			}

			// 3. Process Experiments on the clean text
			if (processExperiments) {
				Matcher experimentsMatcher = findExperiments.matcher(cleanText);
				while (experimentsMatcher.find()) {
					String name = experimentsMatcher.group(1); // single quotes
					if (name == null) { name = experimentsMatcher.group(2); } // double quotes
					if (name == null) { name = experimentsMatcher.group(3); } // no quotes

					String type = experimentsMatcher.group(4);
					String virtual = experimentsMatcher.group(5);

					if (name != null && !IKeyword.TRUE.equals(virtual)) {
						if (exps == null) { exps = new LinkedHashSet<>(); }
						if (IKeyword.BATCH.equals(type)) { name = GamlFileInfo.BATCH_PREFIX + name; }
						exps.add(name);
					}
				}
			}

			// 4. Process Species on the clean text
			Matcher speciesMatcher = findSpecies.matcher(cleanText);
			while (speciesMatcher.find()) {
				String name = speciesMatcher.group(1);
				// If name is null, the engine matched a string to discard it
				if (name != null) {
					if (specs == null) { specs = new LinkedHashSet<>(); }
					specs.add(name);
				}
			}

		} catch (final IOException e1) {
			e1.printStackTrace();
		}

		uri = originalURI;
		this.species = specs;
		DEBUG.OUT("Species built in " + originalURI + ": " + species);
		this.experiments = exps;
		this.tags = tags;
	}

	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	@Override
	public boolean isValid() { return !invalid; }

	/**
	 * Gets the imports.
	 *
	 * @return the imports
	 */
	@Override
	public Collection<String> getImports() { return imports == null ? Collections.EMPTY_LIST : imports; }

	/**
	 * Gets the uses.
	 *
	 * @return the uses
	 */
	@Override
	public Collection<String> getUses() { return uses == null ? Collections.EMPTY_LIST : uses; }

	/**
	 * Gets the tags.
	 *
	 * @return the tags
	 */
	@Override
	public Collection<String> getTags() { return tags == null ? Collections.EMPTY_LIST : tags; }

	/**
	 * Gets the experiments.
	 *
	 * @return the experiments
	 */
	@Override
	public Collection<String> getExperiments() { return experiments == null ? Collections.EMPTY_LIST : experiments; }

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	@Override
	public Collection<String> getSpecies() { return species == null ? Collections.EMPTY_LIST : species; }

	/**
	 * Instantiates a new gaml file info.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public GamlFileInfo(final String propertyString) {
		super(propertyString);
		final String[] values = split(propertyString);
		this.uri = URI.createURI(values[1]);

		this.imports = parseCollection(values[2]);
		this.uses = parseCollection(values[3]);
		this.experiments = parseCollection(values[4]);
		this.species = parseCollection(values[5]);
		this.tags = parseCollection(values[6]);

		this.invalid = "TRUE".equals(values[7]);
		DEBUG.OUT("Species read in " + uri + ": " + species);
	}

	/**
	 * Helper method to safely parse delimited strings into collections.
	 */
	private Collection<String> parseCollection(final String value) {
		if (value == null || value.isEmpty()) return null;
		return asList(splitByWholeSeparatorPreserveAllTokens(value, SUB_DELIMITER));
	}

	@Override
	public String toPropertyString() {
		// Initialize with a capacity large enough to hold a typical serialized string
		final StringBuilder sb = new StringBuilder(256);
		sb.append(super.toPropertyString()).append(DELIMITER); // 0
		sb.append(uri).append(DELIMITER); // 1
		sb.append(imports == null || imports.isEmpty() ? "" : join(imports, SUB_DELIMITER)).append(DELIMITER); // 2
		sb.append(uses == null || uses.isEmpty() ? "" : join(uses, SUB_DELIMITER)).append(DELIMITER); // 3
		sb.append(experiments == null || experiments.isEmpty() ? "" : join(experiments, SUB_DELIMITER))
				.append(DELIMITER); // 4
		sb.append(species == null || species.isEmpty() ? "" : join(species, SUB_DELIMITER)).append(DELIMITER); // 5
		sb.append(tags == null || tags.isEmpty() ? "" : join(tags, SUB_DELIMITER)).append(DELIMITER); // 6
		sb.append(invalid ? "TRUE" : "FALSE").append(DELIMITER); // 7
		return sb.toString();
	}

	/**
	 * Append suffix.
	 *
	 * @param sb
	 *            the sb
	 */
	@Override
	public void appendSuffix(final StringBuilder sb) {
		if (invalid) {
			sb.append(ERRORS);
			return;
		}
		final int expCount = experiments == null ? 0 : experiments.size();
		if (expCount > 0) {
			sb.append(expCount).append(" experiment");
			if (expCount > 1) { sb.append("s"); }
		} else {
			sb.append("no experiment");
		}
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation("GAML model file with " + getSuffix());
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() { return URI.decode(uri.lastSegment()) + " " + tags; }

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	@Override
	public String getTitle() { return URI.decode(uri.lastSegment()); }

	/**
	 * Gets the contextual action.
	 *
	 * @return the contextual action
	 */
	@Override
	public Consumer<IGamlDescription> getContextualAction() { return a -> { GAMA.getGui().openFile(uri); }; }

}