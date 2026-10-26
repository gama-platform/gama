package gama.ui.shared.parameters;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.types.GamaFloatType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.core.experiment.parameters.InputParameter;

/** SWT regression check. On macOS, run with -XstartOnFirstThread. */
public final class EditorToolbarLayoutTest {

	private static final class Editor extends SliderEditor.Float {
		final int[] toolItems;

		Editor(final IParameter parameter, final int[] toolItems) {
			super(agent(), parameter, null);
			this.toolItems = toolItems;
		}

		@Override
		protected Double retrieveValueOfParameter(final boolean retrieveVarValue) { return 7200d; }

		@Override
		protected int[] getToolItems() { return toolItems; }

	}

	private static IAgent agent() {
		final IScope scope = (IScope) Proxy.newProxyInstance(IScope.class.getClassLoader(), new Class<?>[] { IScope.class },
				(p, method, args) -> {
					if (method.getName().equals("copy")) return p;
					throw new UnsupportedOperationException(method.getName());
				});
		return (IAgent) Proxy.newProxyInstance(IAgent.class.getClassLoader(), new Class<?>[] { IAgent.class },
				(p, method, args) -> {
					if (method.getName().equals("getScope")) return scope;
					throw new UnsupportedOperationException(method.getName());
				});
	}

	private static Rectangle[] bounds(final Composite group) {
		return Arrays.stream(group.getChildren()).flatMap(c -> c instanceof Composite k
				? java.util.stream.Stream.concat(java.util.stream.Stream.of(c.getBounds()), Arrays.stream(bounds(k)))
				: java.util.stream.Stream.of(c.getBounds())).toArray(Rectangle[]::new);
	}

	public static void main(final String[] args) {
		try {
			Types.cache(Types.addRegularType("float", new GamaFloatType(Types.getBuiltInTypeManager()), "gama.api"));
			// Text-only buttons: icons need a running workbench
			EditorToolbar.commands[Editor.REVERT] = gama.ui.shared.views.toolbar.GamaCommand.build(null, "R", "", null);
			EditorToolbar.commands[Editor.DEFINE] = gama.ui.shared.views.toolbar.GamaCommand.build(null, "D", "", null);
			final Display display = new Display();
			try {
				final Shell shell = new Shell(display);
				shell.setLayout(new GridLayout());
				final EditorsGroup group = new EditorsGroup(shell);
				EditorToolbar<Double> toolbar = null;
				Editor firstEditor = null;
				EditorToolbar<Double> plainToolbar = null;
				Editor plainEditor = null;
				for (int row = 0; row < 2; row++) {
					final Label label = new Label(group, SWT.RIGHT);
					label.setText("Parameter " + row);
					label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
					final Composite composite = new Composite(group, SWT.NONE);
					composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					composite.setLayout(new GridLayout(2, false));
					final Text text = new Text(composite, SWT.BORDER);
					text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
					final int index = row;
					final Editor editor = new Editor(new InputParameter("Duration", 7200d, Types.FLOAT) {
						@Override
						public Comparable getMinValue(final IScope scope) { return 3600d; }

						@Override
						public Comparable getMaxValue(final IScope scope) { return 36000d; }

						@Override
						public Comparable getStepValue(final IScope scope) { return 0.25d; }

						@Override
						public DisplayUnit getDisplayUnit(final IScope scope) {
							return index == 0 ? new DisplayUnit("h", 3600d) : null;
						}
					}, row == 0 ? new int[] { Editor.VALUE, Editor.REVERT, Editor.DEFINE } : new int[] { Editor.VALUE });
					editor.parent = group;
					final EditorToolbar<Double> next = new EditorToolbar<>(editor, composite);
					next.updateValue(editor.formatValue());
					if (row == 0) {
						toolbar = next;
						firstEditor = editor;
					} else {
						plainToolbar = next;
						plainEditor = editor;
					}
				}
				shell.setSize(1000, 250);
				shell.layout(true, true);
				// Run the layouts deferred by requestLayout()
				while (display.readAndDispatch()) {}
				shell.layout(true, true);
				final Rectangle[] original = bounds(group);
				final Label first = toolbar.getItem(Editor.VALUE), second = plainToolbar.getItem(Editor.VALUE);
				if (display.map(first, null, 0, 0).x != display.map(second, null, 0, 0).x)
					throw new AssertionError("Value columns are not aligned");
				if (first.getSize().x == 0) throw new AssertionError("Value column is not laid out");
				if (first.getForeground().equals(display.getSystemColor(SWT.COLOR_GRAY)))
					throw new AssertionError("Slider values must keep the regular text color");
				if (display.map(first, null, 0, 0).x < display.map(firstEditor.parent, null, 0, 0).x
						+ firstEditor.parent.getSize().x / 2)
					throw new AssertionError("Value column is not next to the buttons side");
				for (final double value : new double[] { 20121.75d, 35114.25d, 3600d, 36000d }) {
					firstEditor.currentValue = value;
					toolbar.updateValue(firstEditor.formatValue());
					plainEditor.currentValue = value;
					plainToolbar.updateValue(plainEditor.formatValue());
					if (!Arrays.equals(original, bounds(group)))
						throw new AssertionError("Value updates moved editors: " + value);
					group.layout(true, true);
					if (!Arrays.equals(original, bounds(group)))
						throw new AssertionError("Value updates changed preferred widths: " + value);
					final Label valueLabel = toolbar.getItem(Editor.VALUE);
					final GC gc = new GC(valueLabel);
					try {
						if (gc.textExtent(valueLabel.getText()).x > valueLabel.getSize().x)
							throw new AssertionError("Reserved value space clips " + valueLabel.getText());
					} finally {
						gc.dispose();
					}
				}
				shell.dispose();
			} finally {
				display.dispose();
			}
		} catch (final Throwable failure) {
			failure.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
