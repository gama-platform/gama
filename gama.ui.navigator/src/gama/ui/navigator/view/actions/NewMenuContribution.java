package gama.ui.navigator.view.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.actions.CompoundContributionItem;

import gama.api.GAMA;
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
		items.add(wizardAction("gama.ui.wizards.newFileWizard", "File...", IGamaIcons.NEW_FILE));
		items.add(wizardAction("gama.ui.wizards.newFolderWizard", "Folder...", IGamaIcons.NEW_FOLDER_BLUE));
		return items.toArray(IContributionItem[]::new);
	}

	private IContributionItem wizardAction(final String id, final String label, final String icon) {
		final var action = new Action(label, GamaIcon.named(icon).descriptor()) {

			@Override
			public void run() {
				final var descriptor = WorkbenchHelper.getWorkbench().getNewWizardRegistry().findWizard(id);
				if (descriptor == null) {
					GAMA.getGui().getDialogFactory().error("The " + label + " wizard is not available.");
					return;
				}
				try {
					final IWorkbenchWizard wizard = descriptor.createWizard();
					final var selection = WorkbenchHelper.getSelection();
					wizard.init(WorkbenchHelper.getWorkbench(),
							selection == null ? StructuredSelection.EMPTY : selection);
					new WizardDialog(WorkbenchHelper.getShell(), wizard).open();
				} catch (final CoreException e) {
					GAMA.getGui().getDialogFactory()
							.error("The " + label + " wizard could not be opened: " + e.getMessage());
				}
			}
		};
		return new ActionContributionItem(action);
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
