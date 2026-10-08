/*******************************************************************************************************
 *
 * TutorialNature.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * The Class TutorialNature.
 *
 * <p>
 * Eclipse project nature used to mark GAMA tutorial projects. Projects bearing this nature are automatically placed in
 * the "Tutorials" virtual folder of the GAMA Navigator. A plugin that ships tutorials should place its tutorial
 * sub-projects inside a {@code tutorials/} subdirectory at the root of the plugin bundle; those projects must declare
 * this nature in their {@code .project} file.
 * </p>
 *
 * <p>
 * Nature identifier: {@value gama.workspace.nature.GamaNatures#TUTORIAL_NATURE}
 * </p>
 *
 * @author GAMA Development Team
 * @since 2026-04
 */
public class TutorialNature implements IProjectNature {

	/** The project to which this nature is attached. */
	private IProject project;

	/**
	 * Configures the nature on the project. No-op for tutorial projects.
	 *
	 * @throws CoreException
	 *             if the configuration fails
	 */
	@Override
	public void configure() throws CoreException {}

	/**
	 * Removes the nature configuration from the project. No-op for tutorial projects.
	 *
	 * @throws CoreException
	 *             if the deconfiguration fails
	 */
	@Override
	public void deconfigure() throws CoreException {}

	/**
	 * Returns the project this nature is attached to.
	 *
	 * @return the project, never {@code null} after {@link #setProject(IProject)} has been called
	 */
	@Override
	public IProject getProject() {
		return project;
	}

	/**
	 * Sets the project this nature is attached to.
	 *
	 * @param project
	 *            the project, must not be {@code null}
	 */
	@Override
	public void setProject(final IProject project) {
		this.project = project;
	}

}
