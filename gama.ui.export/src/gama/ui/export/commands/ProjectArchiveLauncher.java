package gama.ui.export.commands;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;

public final class ProjectArchiveLauncher implements IEditorLauncher {
	@Override public void open(final IPath file) { ImportProject.open(file.toFile().toPath(), ""); }
}
