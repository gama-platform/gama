/*******************************************************************************************************
 *
 * UserGroupFolder.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.workspace.manager.ProjectCategory;
import gama.workspace.manager.WorkspaceModelsManager;

/**
 * A group of user projects, i.e. a plain directory of the workspace (without any {@code .project}) that contains GAMA
 * projects or other groups (see issue #678). It is displayed as a sub-folder of the User folder.
 */
public class UserGroupFolder extends VirtualContent<VirtualContent<?>> {

	/** The location of the directory in the workspace, relative to the workspace root. */
	private final IPath relativePath;

	/** Whether the group is a directory of the workspace that the user can modify (not contributed by a plugin). */
	private final boolean editable;

	/** The sub-groups and the projects of this group. */
	private final List<Object> children = new ArrayList<>();

	/** The icon. */
	private final ImageDescriptor icon;

	/**
	 * Instantiates a new group.
	 *
	 * @param parent
	 *            the User folder or another group
	 * @param relativePath
	 *            the path of the directory relative to the workspace root
	 */
	public UserGroupFolder(final VirtualContent<?> parent, final IPath relativePath) {
		super(parent, relativePath.lastSegment());
		this.relativePath = relativePath;
		final TopLevelFolder top = parent.getTopLevelFolder();
		this.editable = top != null && top.getCategory() == ProjectCategory.USER;
		this.icon = GamaIcon.named(IGamaIcons.FOLDER_GROUP).descriptor();
	}

	/**
	 * Tells whether the user can create, rename, delete or fill this group.
	 *
	 * @return {@code true} for the groups of the User folder
	 */
	public boolean isEditable() { return editable; }

	/**
	 * Organises projects in groups. The projects whose directory is not located in a sub-directory of the workspace
	 * stay at the top.
	 *
	 * @param owner
	 *            the folder that will contain the result
	 * @param projects
	 *            the wrapped projects, in the order they should appear
	 * @param workspaceLocation
	 *            the location of the workspace on disk
	 * @return the groups and the projects to be displayed directly under the owner
	 */
	static List<Object> organise(final VirtualContent<?> owner, final List<IProject> projects,
			final IPath workspaceLocation, final List<WrappedProject> flat, final Map<IPath, UserGroupFolder> known) {
		final List<Object> top = new ArrayList<>();
		// The groups are kept from one call to the next, so that the wrapped projects keep the same parent
		known.values().forEach(g -> g.children.clear());
		final Map<IPath, UserGroupFolder> groups = new LinkedHashMap<>();
		final List<Object> ungrouped = new ArrayList<>();
		for (final IProject p : projects) {
			final IPath rel = ProjectCategory.groupOf(p.getLocation());
			if (rel.isEmpty()) {
				final WrappedProject wp = attach(owner.getManager().wrap(owner, p), owner);
				flat.add(wp);
				ungrouped.add(wp);
				continue;
			}
			UserGroupFolder group = null;
			for (int i = 1; i <= rel.segmentCount(); i++) {
				final IPath current = rel.uptoSegment(i);
				final UserGroupFolder previous = group;
				group = groups.get(current);
				if (group == null) {
					group = known.getOrDefault(current, null);
					if (group == null) { group = new UserGroupFolder(previous == null ? owner : previous, current); }
					groups.put(current, group);
					if (previous == null) {
						top.add(group);
					} else {
						previous.children.add(group);
					}
				}
			}
			final WrappedProject wp = attach(owner.getManager().wrap(group, p), group);
			flat.add(wp);
			group.children.add(wp);
		}
		if (owner.getTopLevelFolder().getCategory() == ProjectCategory.USER) {
			addEmptyGroups(owner, top, groups, known, new java.io.File(workspaceLocation.toOSString()), Path.EMPTY);
		}
		known.clear();
		known.putAll(groups);
		top.addAll(ungrouped);
		return top;
	}

	/**
	 * Wrappers of projects are cached and survive the recreation of the virtual folders: they must be reattached to
	 * the folder or group that now displays them, otherwise their parent is an element that is no longer in the tree
	 * (which prevents revealing them).
	 */
	private static WrappedProject attach(final WrappedResource<?, ?> wrapped, final VirtualContent<?> parent) {
		final WrappedProject wp = (WrappedProject) wrapped;
		if (wp.getParent() != parent) { wp.setParent(parent); }
		return wp;
	}

	/** Adds the directories of the workspace that are not projects and do not contain any project yet. */
	private static void addEmptyGroups(final VirtualContent<?> owner, final List<Object> top,
			final Map<IPath, UserGroupFolder> groups, final Map<IPath, UserGroupFolder> known, final java.io.File dir,
			final IPath rel) {
		if (rel.segmentCount() >= 3) return;
		final java.io.File[] subs = dir.listFiles(f -> f.isDirectory() && !f.getName().startsWith("."));
		if (subs == null) return;
		java.util.Arrays.sort(subs);
		for (final java.io.File sub : subs) {
			final IPath path = rel.append(sub.getName());
			if (rel.isEmpty() && ProjectCategory.isReservedName(sub.getName())) { continue; }
			if (WorkspaceModelsManager.instance.isGamaProject(sub)) { continue; }
			UserGroupFolder group = groups.get(path);
			if (group == null) {
				final UserGroupFolder parent = groups.get(rel);
				group = known.get(path);
				if (group == null) { group = new UserGroupFolder(parent == null ? owner : parent, path); }
				groups.put(path, group);
				if (parent != null) {
					parent.children.add(group);
				} else if (rel.isEmpty()) {
					top.add(group);
				} else {
					continue;
				}
			}
			addEmptyGroups(owner, top, groups, known, sub, path);
		}
	}

	/**
	 * Gets the path of the directory relative to the workspace root.
	 *
	 * @return the relative path
	 */
	public IPath getRelativePath() { return relativePath; }

	/** Returns the directory represented by this group, including groups contributed by plugins. */
	public IPath getLocation() {
		if (editable) return UserGroups.workspaceLocation().append(relativePath);
		final IPath project = getAllProjects().getFirst().getResource().getLocation();
		return project.removeLastSegments(ProjectCategory.groupOf(project).segmentCount() + 1).append(relativePath);
	}

	/**
	 * Gets the projects contained in this group or its sub-groups.
	 *
	 * @return the projects
	 */
	public List<WrappedProject> getAllProjects() {
		final List<WrappedProject> result = new ArrayList<>();
		for (final Object o : children) {
			if (o instanceof WrappedProject p) {
				result.add(p);
			} else if (o instanceof UserGroupFolder g) { result.addAll(g.getAllProjects()); }
		}
		return result;
	}

	@Override
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	public Object[] getNavigatorChildren() { return children.toArray(); }

	@Override
	public ImageDescriptor getImageDescriptor() { return icon; }

	@Override
	public VirtualContentType getType() { return VirtualContentType.VIRTUAL_FOLDER; }

	@Override
	public int findMaxProblemSeverity() {
		int severity = NO_PROBLEM;
		for (final WrappedProject p : getAllProjects()) {
			severity = Math.max(severity, p.findMaxProblemSeverity());
			if (severity == IMarker.SEVERITY_ERROR) { break; }
		}
		return severity;
	}

	@Override
	public String getExplanation() { return "group"; }

	@Override
	public void getSuffix(final StringBuilder sb) {
		final int count = getAllProjects().size();
		sb.append(count).append(" project");
		if (count > 1) { sb.append("s"); }
	}

	@Override
	public ImageDescriptor getOverlay() { return TopLevelFolder.DESCRIPTORS.get(findMaxProblemSeverity()); }

}
