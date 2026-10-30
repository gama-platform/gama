package gama.ui.export.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import gama.export.ExportedModelInfo;
import gama.export.ProjectArchiveInfo;
import gama.workspace.metadata.FileMetaDataProvider;

public final class ExportedModelPropertyPage extends PropertyPage {

	@Override
	protected Control createContents(final Composite parent) {
		noDefaultAndApplyButton();
		final var content = new Composite(parent, SWT.NONE);
		content.setLayout(new FillLayout());
		final var details = new Text(content, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		final IFile file = Adapters.adapt(getElement(), IFile.class);
		final var metadata = FileMetaDataProvider.getInstance().getMetaData(file, false, true);
		details.setText(switch (metadata) {
			case ExportedModelInfo info -> info.getDescription();
			case ProjectArchiveInfo info -> info.getDescription();
			case null, default -> "Unable to read archive information.";
		});
		return content;
	}
}
