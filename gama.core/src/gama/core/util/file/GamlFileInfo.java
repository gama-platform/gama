/*******************************************************************************************************
 *
 * GamlFileInfo.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;

import gama.core.common.GamlFileExtension;
import gama.core.common.util.FileUtils;
import gama.core.runtime.GAMA;
import gama.gaml.interfaces.IGamlDescription;

/**
 * The Class GamlFileInfo.
 */
public class GamlFileInfo extends GamaFileMetaData implements IGamlDescription {

	/**
	 * Gets the all models.
	 *
	 * @return the all models
	 */
	public static Iterable<GamlFileInfo> getAllModels() {
		List<GamlFileInfo> infos = new ArrayList<>();
		try {
			processContainer(FileUtils.ROOT, infos);
		} catch (CoreException e) {}
		return infos;
	}

	/**
	 * Process container.
	 *
	 * @param container
	 *            the container
	 * @throws CoreException
	 *             the core exception
	 */
	static void processContainer(final IContainer container, final List<GamlFileInfo> list) throws CoreException {
		IResource[] members = container.members();
		IFileMetaDataProvider provider = GAMA.getGui().getMetaDataProvider();
		for (IResource member : members) {
			if (member instanceof IContainer) {
				processContainer((IContainer) member, list);
			} else if (member instanceof IFile && GamlFileExtension.isGaml(member.getName())) {
				GamlFileInfo data = (GamlFileInfo) provider.getMetaData(member, true, true);
				// in case the data is not compatible anymore
				if (data.uri == null || data.uri.isEmpty() || data.getName() == null) { provider.refreshAllMetaData(); }
				list.add((GamlFileInfo) provider.getMetaData(member, true, true));
			}
		}
	}

	/** The batch prefix. */
	public static final String BATCH_PREFIX = "***";

	/** The errors. */
	public static final String ERRORS = "errors detected";

	/** The experiments. */
	private final Collection<String> experiments;

	/** The imports. */
	private final Collection<String> imports;

	/** The uses. */
	private final Collection<String> uses;

	/** The tags. */
	private final Collection<String> tags;

	/** The invalid. */
	public final boolean invalid;

	/** The uri. */
	public final URI uri;

	/**
	 * Instantiates a new gaml file info.
	 *
	 * @param stamp
	 *            the stamp
	 * @param imports
	 *            the imports
	 * @param uses
	 *            the uses
	 * @param exps
	 *            the exps
	 * @param tags
	 *            the tags
	 */
	public GamlFileInfo(final URI fileURI, final long stamp, final Collection<String> imports,
			final Collection<String> uses, final Collection<String> exps, final Collection<String> tags) {
		super(stamp);
		uri = fileURI;
		invalid = stamp == Long.MAX_VALUE;
		this.imports = imports;
		this.uses = uses;
		this.experiments = exps;
		this.tags = tags;
	}

	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	public boolean isValid() { return !invalid; }

	/**
	 * Gets the imports.
	 *
	 * @return the imports
	 */
	public Collection<String> getImports() { return imports == null ? Collections.EMPTY_LIST : imports; }

	/**
	 * Gets the uses.
	 *
	 * @return the uses
	 */
	public Collection<String> getUses() { return uses == null ? Collections.EMPTY_LIST : uses; }

	/**
	 * Gets the tags.
	 *
	 * @return the tags
	 */
	public Collection<String> getTags() { return tags == null ? Collections.EMPTY_LIST : tags; }

	/**
	 * Gets the experiments.
	 *
	 * @return the experiments
	 */
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
	 * Method getSuffix()
	 *
	 * @see gama.core.util.file.GamaFileMetaInformation#getSuffix()
	 */
	@Override
	public String getSuffix() {
		if (invalid) return ERRORS;
		final int expCount = experiments == null ? 0 : experiments.size();
		if (expCount > 0) return "" + (expCount == 1 ? "1 experiment" : expCount + " experiments");
		return "no experiment";
	}

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
	public Doc getDocumentation() { return new ConstantDoc("GAML model file with " + getSuffix()); }

	@Override
	public String getName() { return URI.decode(uri.lastSegment()) + " " + tags; }

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