/*******************************************************************************************************
 *
 * ProjectInfo.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.metadata;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.files.AbstractFileMetaData;

/**
 * The Class ProjectInfo.
 */
public class ProjectInfo extends AbstractFileMetaData {

	/** The comment. */
	final String comment;

	/**
	 * Instantiates a new project info.
	 *
	 * @param project
	 *            the project
	 * @throws CoreException
	 *             the core exception
	 */
	public ProjectInfo(final IProject project) throws CoreException {
		super(project);
		final IProjectDescription desc = project.getDescription();
		comment = desc.getComment();
	}

	/**
	 * Instantiates a new project info.
	 *
	 * @param propertiesString
	 *            the properties string
	 */
	public ProjectInfo(final String propertiesString) { // NO_UCD (unused code)
		super(propertiesString);
		final String[] segments = split(propertiesString);
		comment = segments[1];
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		if (comment != null && !comment.isEmpty()) { sb.append(comment); }
	}

	@Override
	public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + comment;
	}

	@Override
	public IGamlDocumentation getDocumentation() { return new GamlConstantDocumentation(comment); }
}