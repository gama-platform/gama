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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

	/** The find tags. */
	public static final Pattern findTags = Pattern.compile("Tags:\\s*(.*)");

	/** The find pragmas. */
	public static final Pattern findPragmas = Pattern.compile("^\\s*@([^\\s@]+)");

	/** The find strings. */
	public static final Pattern findStrings = Pattern.compile("('([^']*)')|(\"([^\"]*)\")");

	/** The find experiments. */
	public static final Pattern findExperiments = Pattern.compile(
			"^\\s*experiment\\s+(?:'([^']*)'|\"([^\"]*)\"|(\\w+))(?:\\s+type:\\s*(\\w+))?(?:\\s+virtual:\\s*(\\w+))?");

	/** The batch prefix. */
	public static final String BATCH_PREFIX = "***";

	/** The errors. */
	public static final String ERRORS = "errors detected";

	/** The experiments. */
	private Collection<String> experiments;

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
	 * @param stamp
	 *            the stamp
	 */
	public void createFrom(final URI originalURI) {

		Set<String> tags = null;
		Set<String> exps = null;

		for (final URI u : GamlResourceIndexer.directImportsOf(originalURI)) {
			if (imports == null) { imports = new LinkedHashSet<>(); }
			imports.add(u.deresolve(originalURI).toString());
		}

		try (InputStreamReader isr =
				new InputStreamReader(getResourceSet().getURIConverter().createInputStream(originalURI));
				BufferedReader reader = new BufferedReader(isr)) {

			String input;
			boolean processExperiments = true;
			while ((input = reader.readLine()) != null) {
				Matcher tagsMatcher = findTags.matcher(input);
				if (tagsMatcher.find()) {
					String tagList = tagsMatcher.group(1);
					tags = new HashSet<>(asList(
							splitByWholeSeparatorPreserveAllTokens(uncapitalize(deleteWhitespace(tagList)), ",")));
				}
				Matcher pragmasMatcher = findPragmas.matcher(input);
				if (pragmasMatcher.find()) {
					String pragma = pragmasMatcher.group(1);
					// DEBUG.OUT("Pragma found = " + pragma);
					if (IKeyword.PRAGMA_NO_EXPERIMENT.equals(pragma)) { processExperiments = false; }
				}
				Matcher stringsMatcher = findStrings.matcher(input);
				while (stringsMatcher.find()) {
					String s = stringsMatcher.group();
					// DEBUG.OUT("Strings found = " + s);
					if (s.length() > 6) {
						s = s.substring(1, s.length() - 1);
						final URI u = URI.createFileURI(s);
						final String ext = u.fileExtension();
						if (ext != null && !ext.isBlank() && !"gaml".equals(ext)
								&& GamaFileType.managesExtension(ext)) {
							if (uses == null) { uses = new LinkedHashSet<>(); }
							DEBUG.OUT("===== Considers in uses : " + s);
							uses.add(s);
						}
					}
				}
				if (processExperiments) {
					Matcher experimentsMatcher = findExperiments.matcher(input);
					if (experimentsMatcher.find()) {
						String name = experimentsMatcher.group(1); // single quotes
						if (name == null) {
							name = experimentsMatcher.group(2); // double quotes
						}
						if (name == null) {
							name = experimentsMatcher.group(3); // no quotes
						}
						String type = experimentsMatcher.group(4);
						String virtual = experimentsMatcher.group(5);
						if (name != null && !IKeyword.TRUE.equals(virtual)) {
							// DEBUG.OUT("Experiment " + name + " type " + type + " virtual " + virtual);
							if (exps == null) { exps = new LinkedHashSet<>(); }
							if (IKeyword.BATCH.equals(type)) { name = GamlFileInfo.BATCH_PREFIX + name; }
							exps.add(name);
						}
					}
				}
			}

		} catch (final IOException e1) {
			e1.printStackTrace();
		}

		uri = originalURI;

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
	 * Instantiates a new gaml file info.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public GamlFileInfo(final String propertyString) {
		super(propertyString);
		final String[] values = split(propertyString);
		this.uri = URI.createURI(values[1]);
		final List<String> declaredImports = asList(splitByWholeSeparatorPreserveAllTokens(values[2], SUB_DELIMITER));
		this.imports = declaredImports == null || declaredImports.isEmpty() || declaredImports.contains(null) ? null
				: declaredImports;
		final List<String> declaredUses = asList(splitByWholeSeparatorPreserveAllTokens(values[3], SUB_DELIMITER));
		this.uses = declaredUses == null || declaredUses.isEmpty() || declaredUses.contains(null) ? null : declaredUses;
		final List<String> exps = asList(splitByWholeSeparatorPreserveAllTokens(values[4], SUB_DELIMITER));
		this.experiments = exps == null || exps.isEmpty() || exps.contains(null) ? null : exps;
		final List<String> declaredTags = asList(splitByWholeSeparatorPreserveAllTokens(values[5], SUB_DELIMITER));
		this.tags = declaredTags == null || declaredTags.isEmpty() || declaredTags.contains(null) ? null : declaredTags;
		invalid = "TRUE".equals(values[6]);
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

	/**
	 * To property string.
	 *
	 * @return the string
	 */
	@Override
	public String toPropertyString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toPropertyString()).append(DELIMITER); // 0
		sb.append(uri).append(DELIMITER); // 1
		sb.append(imports == null ? "" : join(SUB_DELIMITER, imports)).append(DELIMITER); // 2
		sb.append(uses == null ? "" : join(SUB_DELIMITER, uses)).append(DELIMITER); // 3
		sb.append(experiments == null ? "" : join(SUB_DELIMITER, experiments)).append(DELIMITER); // 4
		sb.append(tags == null ? "" : join(SUB_DELIMITER, tags)).append(DELIMITER); // 5
		sb.append(invalid ? "TRUE" : "FALSE").append(DELIMITER); // 6
		return sb.toString();

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