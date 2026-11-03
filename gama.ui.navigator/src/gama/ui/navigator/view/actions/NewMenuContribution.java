package gama.ui.navigator.view.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.actions.CompoundContributionItem;

import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;

/** The shared, fixed New menu for the workbench and navigator. */
public class NewMenuContribution extends CompoundContributionItem {

	@Override
	protected IContributionItem[] getContributionItems() {
		final var selection = WorkbenchHelper.getSelection();
		final Object selected = selection == null ? null : selection.getFirstElement();
		final UserGroupFolder group = selected instanceof UserGroupFolder folder && folder.isEditable() ? folder : null;
		final List<IContributionItem> items = new ArrayList<>();
		items.add(new ActionContributionItem(new GroupActions.NewProject(group)));
		if (GroupActions.isUserFolder(selected) || group != null) {
			items.add(new ActionContributionItem(new GroupActions.NewGroup(group)));
		}
		items.add(new Separator());
		items.add(wizard("gama.ui.wizards.NewFileWizard", "Model...", IGamaIcons.NEW_MODEL));
		items.add(wizard("gama.ui.wizards.NewTestExperimentWizard", "Test...", IGamaIcons.NEW_MODEL));
		items.add(new Separator());
		items.add(wizard("org.eclipse.ui.wizards.new.file", "File...", IGamaIcons.FILE_ICON));
		items.add(wizard("org.eclipse.ui.wizards.new.folder", "Folder...", IGamaIcons.NEW_FOLDER));
		return items.toArray(IContributionItem[]::new);
	}

	private IContributionItem wizard(final String id, final String label, final String icon) {
		final var parameter = new CommandContributionItemParameter(WorkbenchHelper.getWindow(), id,
				"org.eclipse.ui.newWizard", CommandContributionItem.STYLE_PUSH);
		parameter.parameters = Map.of("newWizardId", id);
		parameter.label = label;
		parameter.icon = GamaIcon.named(icon).descriptor();
		return new CommandContributionItem(parameter);
	}
}
