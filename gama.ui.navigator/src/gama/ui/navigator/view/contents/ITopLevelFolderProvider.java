/*******************************************************************************************************
 *
 * ITopLevelFolderProvider.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import org.eclipse.core.runtime.CoreException;

/**
 * Factory contributed to {@code gama.ui.navigator.topLevelFolders}. Implementations need a public no-argument
 * constructor. Workspace folders are retained across navigator lifetimes; independent folders are disposed when
 * the content provider closes.
 */
@FunctionalInterface
public interface ITopLevelFolderProvider {

	/**
	 * Creates a folder attached to the supplied root. Downstream contributions must return promptly without network
	 * or disk scans and use the non-workspace constructor. The navigator's own declared workspace factories may
	 * initialize their local project children. Do not enumerate the root's folders from a factory.
	 *
	 * @param root the navigator root
	 * @return a folder using the non-workspace {@link TopLevelFolder} constructor
	 * @throws CoreException if the contribution cannot be initialized
	 */
	TopLevelFolder createFolder(NavigatorRoot root) throws CoreException;
}
