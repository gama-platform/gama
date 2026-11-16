package gama.ui.navigator.view;

import org.eclipse.core.expressions.PropertyTester;

import gama.ui.navigator.view.contents.VirtualContent;

/** Shares the virtual content's workspace-action policy with declarative menu contributions. */
public final class WorkspaceActionsTester extends PropertyTester {

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		final boolean supported =
				!(receiver instanceof VirtualContent<?> content) || content.supportsWorkspaceActions();
		return supported == (expectedValue == null || Boolean.parseBoolean(expectedValue.toString()));
	}
}
