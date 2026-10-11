/*******************************************************************************************************
 *
 * GroupActions.java, in gama.ui.navigator.view.actions, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import gama.api.GAMA;
import gama.ui.navigator.view.contents.TopLevelFolder;
import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.navigator.view.contents.UserGroups;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.workspace.manager.ProjectCategory;

/**
 * The actions that create, rename and delete groups of user projects (see issue #678).
 */
public final class GroupActions {

	private GroupActions() {}

	/**
	 * Creates a new group, inside the selected group if any.
	 */
	public static class NewGroup extends Action {

		/** The group in which the new group is created, or null. */
		private final UserGroupFolder parent;

		/**
		 * @param parent
		 *            the selected group, or {@code null} to create the group at the top of the User folder
		 */
		public NewGroup(final UserGroupFolder parent) {
			super("New group folder...");
			this.parent = parent;
			setToolTipText("Creates a folder that can contain several projects");
		}

		@Override
		public void run() {
			final IPath base = parent == null ? org.eclipse.core.runtime.Path.EMPTY : parent.getRelativePath();
			final IInputValidator validator =
					text -> text.isBlank() || text.contains("/") || text.contains("\\") ? "Enter a valid name"
							: UserGroups.validate(base.append(text.trim()).toString());
			final InputDialog dialog = new InputDialog(WorkbenchHelper.getShell(), "New group folder",
					"Name of the new group in the " + ProjectCategory.USER.getLabel() + " folder:", "", validator);
			if (dialog.open() != Window.OK) return;
			UserGroups.create(base.append(dialog.getValue().trim()).toString());
			UserGroups.refreshNavigator();
		}
	}

	/**
	 * Renames a group, which moves all its projects.
	 */
	public static class RenameGroup extends Action {

		/** The group. */
		private final UserGroupFolder group;

		/**
		 * @param group
		 *            the group to rename
		 */
		public RenameGroup(final UserGroupFolder group) {
			super("Rename group folder...");
			this.group = group;
		}

		@Override
		public void run() {
			final IPath parent = group.getRelativePath().removeLastSegments(1);
			final IInputValidator validator =
					text -> text.isBlank() || text.contains("/") || text.contains("\\") ? "Enter a valid name"
							: UserGroups.validate(parent.append(text.trim()).toString());
			final InputDialog dialog = new InputDialog(WorkbenchHelper.getShell(), "Rename group folder",
					"New name of the group:", group.getName(), validator);
			if (dialog.open() != Window.OK) return;
			final String name = dialog.getValue().trim();
			if (name.equals(group.getName())) return;
			try {
				UserGroups.renameGroup(group, parent.append(name).toString());
			} catch (final CoreException e) {
				GAMA.getGui().getDialogFactory().error("The group could not be renamed: " + e.getMessage());
			}
			UserGroups.refreshNavigator();
		}
	}

	/**
	 * Deletes an empty group.
	 */
	public static class DeleteGroup extends Action {

		/** The group. */
		private final UserGroupFolder group;

		/**
		 * @param group
		 *            the group to delete
		 */
		public DeleteGroup(final UserGroupFolder group) {
			super("Delete group folder");
			this.group = group;
		}

		@Override
		public void run() {
			if (!group.getAllProjects().isEmpty()) {
				GAMA.getGui().getDialogFactory().error("This group still contains projects. "
						+ "Move or delete them first.");
				return;
			}
			if (!GAMA.getGui().getDialogFactory().confirm("Delete group folder",
					"Delete the empty group '" + group.getName() + "'?"))
				return;
			UserGroups.deleteIfEmpty(group.getRelativePath());
			UserGroups.refreshNavigator();
		}
	}

	/**
	 * Tells whether groups can be created in a given folder.
	 *
	 * @param o
	 *            the selected element
	 * @return true if it is the User folder
	 */
	public static boolean isUserFolder(final Object o) {
		return o instanceof TopLevelFolder f && f.getCategory() == ProjectCategory.USER;
	}
}
