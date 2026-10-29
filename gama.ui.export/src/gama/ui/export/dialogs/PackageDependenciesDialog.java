package gama.ui.export.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import gama.export.SimulationPackageExporter.Dependency;

/** Shows automatically packaged non-standard plugins and exact host requirements. */
public final class PackageDependenciesDialog extends TitleAreaDialog {

	private final List<Dependency> dependencies;
	private Table table;
	private Label detail;

	public PackageDependenciesDialog(final Shell shell, final List<Dependency> dependencies) {
		super(shell);
		this.dependencies = List.copyOf(dependencies);
		setHelpAvailable(false);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Additional plugins");
		setMessage("These extra Java plugins are included automatically. Unsupported plugins prevent package export.");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final var area = (Composite) super.createDialogArea(parent);
		final var content = new Composite(area, SWT.NONE);
		content.setLayout(new GridLayout());
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table = new Table(content, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		final var data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 700;
		data.heightHint = 280;
		table.setLayoutData(data);
		for (final String title : List.of("Plugin", "Exact version", "Provided by")) {
			final var column = new TableColumn(table, SWT.LEFT);
			column.setText(title);
			column.setWidth(title.equals("Plugin") ? 350 : 165);
		}
		for (final Dependency dependency : dependencies) {
			final var item = new TableItem(table, SWT.NONE);
			item.setData(dependency);
			item.setChecked(dependency.automatic());
			item.setGrayed(true);
			item.setText(new String[] { dependency.id(), dependency.version(),
					item.getChecked() ? "Package (automatic)" : dependency.standard() ? "Standard GAMA" : "Unsupported extra" });
		}
		detail = new Label(content, SWT.WRAP);
		detail.setText("Unsupported extra dependencies prevent package export; build/install Java plugin JARs or use application export. "
				+ "A package cannot replace an already installed plugin, even at the same version.");
		final var detailData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		detailData.widthHint = 700;
		detailData.heightHint = 65;
		detail.setLayoutData(detailData);
		table.addListener(SWT.Selection, event -> {
			if (!(event.item instanceof TableItem item)) return;
			final var dependency = (Dependency) item.getData();
			item.setChecked(dependency.automatic());
			detail.setText(dependency.reason() + (dependency.file() == null ? "" : "\n" + dependency.file()));
		});
		return area;
	}

	@Override
	protected boolean isResizable() { return true; }
}
