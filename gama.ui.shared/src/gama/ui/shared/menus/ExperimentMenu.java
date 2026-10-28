package gama.ui.shared.menus;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import gama.annotations.constants.IKeyword;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.resources.IGamaIcons;

/** Experiment choices shared by editors and standalone launchers. */
public final class ExperimentMenu {

	public record Entry(String name, String label, String type) {}

	private ExperimentMenu() {}

	public static Menu create(final Control owner, final List<Entry> experiments, final Consumer<String> select) {
		final Menu menu = new Menu(owner.getShell(), SWT.POP_UP);
		for (final Entry experiment : experiments) {
			GamaMenu.action(menu, experiment.label(), event -> select.accept(experiment.name()),
					menuIcon(experiment.type())).setData("exp", experiment.name());
		}
		owner.addDisposeListener(event -> menu.dispose());
		return menu;
	}

	public static void open(final Menu menu, final Control owner) {
		menu.setLocation(owner.toDisplay(0, owner.getSize().y));
		menu.setVisible(true);
	}

	public static String buttonIcon(final String type) {
		return IKeyword.BATCH.equals(type) ? IGamaIcons.BUTTON_BATCH
				: IKeyword.RECORD.equals(type) ? IGamaIcons.BUTTON_BACK : IGamaIcons.BUTTON_GUI;
	}

	private static String menuIcon(final String type) {
		if (ThemeHelper.isDark()) return buttonIcon(type);
		return IKeyword.BATCH.equals(type) ? IGamaIcons.MENU_BATCH
				: IKeyword.RECORD.equals(type) ? IGamaIcons.MENU_BACK : IGamaIcons.MENU_GUI;
	}
}
