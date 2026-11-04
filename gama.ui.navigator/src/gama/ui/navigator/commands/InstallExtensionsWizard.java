/*******************************************************************************************************
 *
 * InstallExtensionsWizard.java, in gama.ui.navigator, is part of the source code of the GAMA modeling
 * and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProvisioningPlan;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

import gama.ui.navigator.commands.InstallExtension.Extension;

/**
 * A categorized checklist and a single review for an entire plugin installation.
 */
final class InstallExtensionsWizard extends Wizard {

	private final SelectionPage selection = new SelectionPage();
	private final ReviewPage review = new ReviewPage();
	private InstallOperation operation;
	private boolean resolving;

	InstallExtensionsWizard(final Extension initial) {
		setWindowTitle("Install GAMA plugins");
		setNeedsProgressMonitor(true);
		if (initial != null) selection.selected.add(initial.featureId());
	}

	@Override
	public void addPages() {
		addPage(selection);
		addPage(review);
	}

	@Override
	public IWizardPage getNextPage(final IWizardPage page) {
		if (page != selection) return null;
		operation = null;
		selection.setErrorMessage(null);
		final List<Extension> chosen = selection.chosen();
		if (chosen.isEmpty()) return null;
		Throwable failure = null;
		resolving = true;
		try {
			getContainer().run(true, true, monitor -> {
				try {
					operation = InstallExtension.resolve(chosen, monitor);
				} catch (final ProvisionException e) {
					throw new InvocationTargetException(e);
				}
			});
			return review;
		} catch (final InterruptedException e) {
			operation = null;
		} catch (final InvocationTargetException e) {
			operation = null;
			failure = e.getCause();
		} finally {
			resolving = false;
		}
		selection.reload();
		if (failure != null) showFailure(selection, failure);
		return null;
	}

	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == review && operation != null && review.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		if (!canFinish()) return false;
		try {
			InstallExtension.schedule(operation, review.trustAll.getSelection());
			return true;
		} catch (final ProvisionException e) {
			showFailure(review, e);
			operation = null;
			getContainer().updateButtons();
			return false;
		}
	}

	@Override
	public void dispose() {
		InstallExtensionsContribution.removeListener(selection.listener);
		super.dispose();
	}

	private static void showFailure(final WizardPage page, final Throwable error) {
		if (error instanceof OperationCanceledException) return;
		final IStatus status = error instanceof CoreException exception ? exception.getStatus()
				: new Status(IStatus.ERROR, InstallExtension.PLUGIN_ID, "Unable to prepare plugin installation.", error);
		if (status.matches(IStatus.CANCEL)) return;
		StatusManager.getManager().handle(status, StatusManager.LOG);
		page.setErrorMessage(status.getMessage());
	}

	private record Category(String label, List<Extension> extensions) {}

	private record Plugin(Category category, Extension extension) {}

	private final class SelectionPage extends WizardPage {

		private final Set<String> selected = new HashSet<>();
		private final Runnable listener = this::reload;
		private List<Extension> available = List.of();
		private Map<String, Version> installed = Map.of();
		private CheckboxTreeViewer tree;
		private Button refresh;
		private Label status;

		SelectionPage() {
			super("plugins");
			setTitle("Choose plugins");
			setDescription("Select plugins or whole categories, then click Next to review one combined installation.");
			setPageComplete(false);
		}

		@Override
		public void createControl(final Composite parent) {
			final Composite area = new Composite(parent, SWT.NONE);
			area.setLayout(new GridLayout());
			tree = new CheckboxTreeViewer(area, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
			tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			tree.getTree().setHeaderVisible(true);
			tree.setContentProvider(new ITreeContentProvider() {
				@Override
				public Object[] getElements(final Object input) { return ((List<?>) input).toArray(); }

				@Override
				public Object[] getChildren(final Object element) {
					return element instanceof Category category ? category.extensions().stream()
							.map(e -> new Plugin(category, e)).toArray() : new Object[0];
				}

				@Override
				public Object getParent(final Object element) {
					return element instanceof Plugin plugin ? plugin.category() : null;
				}

				@Override
				public boolean hasChildren(final Object element) { return element instanceof Category; }
			});
			addColumn("Plugin", 430, 0);
			addColumn("Status", 110, 1);
			addColumn("Available version", 170, 2);
			tree.setCheckStateProvider(new ICheckStateProvider() {
				@Override
				public boolean isChecked(final Object element) {
					return element instanceof Plugin plugin ? selected.contains(plugin.extension().featureId())
							: ((Category) element).extensions().stream().anyMatch(e -> selected.contains(e.featureId()));
				}

				@Override
				public boolean isGrayed(final Object element) {
					if (element instanceof Plugin plugin) return current(plugin.extension());
					final var candidates = ((Category) element).extensions().stream().filter(e -> !current(e)).toList();
					final long count = candidates.stream().filter(e -> selected.contains(e.featureId())).count();
					return candidates.isEmpty() || count > 0 && count < candidates.size();
				}
			});
			tree.addCheckStateListener(event -> {
				final List<Extension> affected = event.getElement() instanceof Category category
						? category.extensions() : List.of(((Plugin) event.getElement()).extension());
				for (final Extension extension : affected) {
					if (event.getChecked() && !current(extension)) {
						selected.add(extension.featureId());
					} else {
						selected.remove(extension.featureId());
					}
				}
				selectionChanged();
			});
			final Composite buttons = new Composite(area, SWT.NONE);
			buttons.setLayout(new GridLayout(3, false));
			addButton(buttons, "Select all", () -> {
				available.stream().filter(e -> !current(e)).forEach(e -> selected.add(e.featureId()));
				selectionChanged();
			});
			addButton(buttons, "Deselect all", () -> {
				selected.clear();
				selectionChanged();
			});
			refresh = addButton(buttons, "Refresh list",
					() -> InstallExtensionsContribution.refresh(area.getDisplay()));
			status = new Label(area, SWT.WRAP);
			status.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			setControl(area);
			InstallExtensionsContribution.addListener(listener);
			InstallExtensionsContribution.ensureLoaded(area.getDisplay());
			reload();
		}

		private void addColumn(final String title, final int width, final int index) {
			final TreeViewerColumn column = new TreeViewerColumn(tree, SWT.NONE);
			column.getColumn().setText(title);
			column.getColumn().setWidth(width);
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(final Object element) {
					if (element instanceof Category category) return index == 0 ? category.label() : "";
					final Extension extension = ((Plugin) element).extension();
					return switch (index) {
						case 0 -> extension.label();
						case 1 -> current(extension) ? "Installed"
								: installed.containsKey(extension.featureId()) ? "Update" : "New";
						default -> extension.version().toString();
					};
				}

				@Override
				public Color getForeground(final Object element) {
					return element instanceof Plugin plugin && current(plugin.extension())
							? tree.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY) : null;
				}
			});
		}

		private void reload() {
			if (tree == null || tree.getControl().isDisposed()) return;
			if (resolving) return;
			if (getContainer().getCurrentPage() != null && getContainer().getCurrentPage() != this) return;
			available = InstallExtensionsContribution.availableExtensions();
			installed = InstallExtensionsContribution.installedVersions();
			selected.retainAll(available.stream().filter(e -> !current(e)).map(Extension::featureId).toList());
			final Map<String, List<Extension>> groups = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			available.stream().sorted(Comparator.comparing(Extension::label, String.CASE_INSENSITIVE_ORDER))
					.forEach(e -> e.categories().forEach(c -> groups.computeIfAbsent(c, _ -> new ArrayList<>()).add(e)));
			tree.setInput(groups.entrySet().stream().map(e -> new Category(e.getKey(), List.copyOf(e.getValue()))).toList());
			tree.expandAll();
			refresh.setEnabled(!InstallExtensionsContribution.isLoading());
			selectionChanged();
		}

		private boolean current(final Extension extension) {
			final Version version = installed.get(extension.featureId());
			return version != null && version.compareTo(extension.version()) >= 0;
		}

		private List<Extension> chosen() {
			return available.stream().filter(e -> selected.contains(e.featureId())).toList();
		}

		private void selectionChanged() {
			operation = null;
			setErrorMessage(null);
			tree.refresh();
			final boolean loading = InstallExtensionsContribution.isLoading();
			status.setText(selected.size() + " selected out of " + available.size() + " available plugins."
					+ (loading ? " Loading update sites..." : ""));
			status.getParent().layout();
			if (InstallExtensionsContribution.hasFailed()) {
				setMessage("Some update sites could not be read. You can install listed plugins, or refresh to retry.",
						IMessageProvider.WARNING);
			} else {
				setMessage(available.isEmpty() && !loading ? "No plugins published. Refresh to try again." : null);
			}
			setPageComplete(!selected.isEmpty());
		}

		@Override
		public boolean canFlipToNextPage() { return isPageComplete(); }

		@Override
		public void setVisible(final boolean visible) {
			super.setVisible(visible);
			if (visible) reload();
		}
	}

	private final class ReviewPage extends WizardPage {

		private Text details;
		private Button trustAll;

		ReviewPage() {
			super("review");
			setTitle("Review plugin installation");
			setDescription("Dependencies are included automatically. Click Finish to install in the background.");
		}

		@Override
		public void createControl(final Composite parent) {
			final Composite area = new Composite(parent, SWT.NONE);
			area.setLayout(new GridLayout());
			details = new Text(area, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			trustAll = new Button(area, SWT.CHECK);
			trustAll.setText("Trust all plugin publishers, including unsigned plugins");
			trustAll.setSelection(true);
			final Label notice = new Label(area, SWT.WRAP);
			notice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			notice.setText("Finishing automatically accepts the licenses of the selected plugins and their dependencies.\n"
					+ "Trusting all publishers skips signature trust prompts for this installation only. "
					+ "Plugins run with your permissions. Install only software from sources you trust.");
			setControl(area);
		}

		@Override
		public void setVisible(final boolean visible) {
			if (visible && operation != null) {
				setErrorMessage(null);
				final StringBuilder summary = new StringBuilder("Selected plugins:\n");
				selection.chosen().stream().sorted(Comparator.comparing(Extension::label, String.CASE_INSENSITIVE_ORDER))
						.forEach(e -> summary.append("  ").append(e.label()).append("  ").append(e.version()).append('\n'));
				appendPlan(summary, operation.getProvisioningPlan());
				final IStatus result = operation.getResolutionResult();
				if (!result.isOK()) {
					summary.append("\nResolver details:\n").append(operation.getResolutionDetails()).append('\n');
				}
				details.setText(summary.toString());
				setMessage(result.matches(IStatus.WARNING) ? result.getMessage() : null, IMessageProvider.WARNING);
			}
			super.setVisible(visible);
		}

		@Override
		public boolean canFlipToNextPage() { return false; }

		private void appendPlan(final StringBuilder summary, final IProvisioningPlan plan) {
			summary.append("\nInstall or update (including dependencies):\n");
			appendUnits(summary, plan.getAdditions().query(QueryUtil.ALL_UNITS, null).toSet().stream().toList());
			final var removals = plan.getRemovals().query(QueryUtil.ALL_UNITS, null).toSet();
			if (!removals.isEmpty()) {
				summary.append("\nRemove or replace:\n");
				appendUnits(summary, List.copyOf(removals));
			}
			if (plan.getInstallerPlan() != null) {
				summary.append("\nInstaller changes:\n");
				appendPlan(summary, plan.getInstallerPlan());
			}
		}

		private void appendUnits(final StringBuilder summary, final List<IInstallableUnit> units) {
			units.stream().sorted(Comparator.comparing(IInstallableUnit::getId))
					.forEach(u -> summary.append("  ").append(u.getId()).append("  ").append(u.getVersion()).append('\n'));
		}
	}

	private static Button addButton(final Composite parent, final String label, final Runnable action) {
		final Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.addListener(SWT.Selection, _ -> action.run());
		return button;
	}
}
