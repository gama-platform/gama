/*******************************************************************************************************
 *
 * ExpressionControl.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import static gama.ui.shared.resources.GamaColors.get;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import gama.core.common.util.StringUtils;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.GAML;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.GamaStringType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.views.toolbar.GamaToolbarFactory;

/**
 * The Class ExpressionControl.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class ExpressionControl implements /* IPopupProvider, */SelectionListener, ModifyListener, FocusListener {

	/** The text. */
	private final Text text;

	/** The editor. */
	private final ExpressionBasedEditor<Object> editor;

	/** The current value. */
	private Object currentValue;

	/** The current exception. */
	protected Exception currentException;

	/** The evaluate expression. */
	final boolean evaluateExpression;

	/** The host agent. */
	private final IAgent hostAgent;

	/** The scope. */
	private final IScope scope;

	/** The expected type. */
	private final IType<?> expectedType;

	/** The tooltip listener. */
	MouseTrackListener tooltipListener = new MouseTrackAdapter() {

		@Override
		public void mouseExit(final MouseEvent arg0) {
			removeTooltip();
		}
	};

	/**
	 * Instantiates a new expression control.
	 *
	 * @param scope
	 *            the scope
	 * @param comp
	 *            the comp
	 * @param ed
	 *            the ed
	 * @param agent
	 *            the agent
	 * @param expectedType
	 *            the expected type
	 * @param controlStyle
	 *            the control style
	 * @param evaluate
	 *            the evaluate
	 */
	public ExpressionControl(final IScope scope, final Composite comp, final ExpressionBasedEditor ed,
			final IAgent agent, final IType<?> expectedType, final int controlStyle, final boolean evaluate) {
		this.scope = scope;
		editor = ed;
		evaluateExpression = evaluate;
		hostAgent = agent;
		this.expectedType = expectedType;
		text = createTextBox(comp, controlStyle);
		text.addModifyListener(this);
		text.addFocusListener(this);
		text.addSelectionListener(this);
		text.addMouseTrackListener(tooltipListener);
		// if (ed != null) { ed.getLabel().getLabel().addMouseTrackListener(tooltipListener); }
	}

	@Override
	public void modifyText(final ModifyEvent event) {
		if (editor != null && editor.internalModification) return;
		modifyValue();
		displayTooltip();
	}

	/**
	 * Display tooltip.
	 */
	protected void displayTooltip() {
		final var s = getPopupText();
		if (s == null || s.isEmpty()) {
			removeTooltip();
		} else {
			final var displayer = GamaToolbarFactory.findTooltipDisplayer(text);
			if (displayer != null) { displayer.displayTooltip(s, null); }
		}
		if (editor != null && currentException != null) { editor.getLabel().signalErrored(); }
	}

	/**
	 * Removes the tooltip.
	 */
	protected void removeTooltip() {
		final var displayer = GamaToolbarFactory.findTooltipDisplayer(text);
		if (displayer != null) { displayer.stopDisplayingTooltips(); }
		if (editor != null) { editor.getLabel().cancelErrored(); }

	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent me) {
		try {
			if (text == null || text.isDisposed()) return;
			modifyValue();
			displayValue(getCurrentValue());
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Compute value.
	 *
	 * @return the object
	 */
	private Object computeValue() {
		try {
			currentException = null;
			var agent = getHostAgent();
			// AD: fix for SWT Issue in Eclipse 4.4
			if (text == null || text.isDisposed()) return null;
			var s = text.getText();
			if (expectedType == Types.STRING && !StringUtils.isGamaString(s)) { s = StringUtils.toGamlString(s); }
			// AD: Fix for Issue 1042
			if (agent != null && (agent.getScope().interrupted() || agent.dead()) && agent instanceof SimulationAgent) {
				agent = agent.getScope().getExperiment();
				if (agent == null) { agent = GAMA.getRuntimeScope().getExperiment(); }
			}
			if (NumberEditor.UNDEFINED_LABEL.equals(s)) {
				setCurrentValue(null);
			} else if (agent == null) {
				if (expectedType == Types.STRING) {
					setCurrentValue(StringUtils.toJavaString(GamaStringType.staticCast(null, s, false)));
				} else {
					setCurrentValue(expectedType.cast(scope, s, null, false));
				}
			} else if (!agent.dead()) {
				// Solves Issue #3104 when the experiment agent dies
				setCurrentValue(evaluateExpression ? GAML.evaluateExpression(s, agent)
						: GAML.compileExpression(s, agent, true));
			}
		} catch (final Exception e) {
			currentException = e;
			return null;
		}
		return getCurrentValue();
	}

	/**
	 * Modify value.
	 */
	public void modifyValue() {
		final var oldValue = getCurrentValue();
		final var value = computeValue();
		if (currentException != null) {
			setCurrentValue(oldValue);
			return;
		}
		if (editor != null) {
			try {

				if (editor.acceptNull && value == null) {
					editor.modifyValue(null);
				} else if (expectedType == Types.STRING) {
					editor.modifyValue(evaluateExpression
							? StringUtils.toJavaString(GamaStringType.staticCast(scope, value, false)) : value);
				} else {
					editor.modifyValue(evaluateExpression ? expectedType.cast(scope, value, null, false) : value);
				}
				editor.updateToolbar();

			} catch (final GamaRuntimeException e) {
				setCurrentValue(oldValue);
				currentException = e;
			}
		}
	}

	/**
	 * Creates the text box.
	 *
	 * @param comp
	 *            the comp
	 * @param controlStyle
	 *            the control style
	 * @return the text
	 */
	protected Text createTextBox(final Composite comp, final int controlStyle) {
		var c = new Composite(comp, SWT.NONE);
		var f = new FillLayout();
		f.marginHeight = 2;
		f.marginWidth = 2;
		c.setLayout(f);
		final var d = new GridData(SWT.FILL, SWT.CENTER, true, false);
		// d.heightHint = 20;
		c.setLayoutData(d);

		c.addListener(SWT.Paint, e -> {
			GC gc = e.gc;
			Rectangle bounds = c.getBounds();
			Color ref = comp.getBackground();
			gc.setBackground(ThemeHelper.isDark() ? get(ref).lighter() : get(ref).darker());
			// gc.setForeground(gc.getBackground());
			gc.fillRoundRectangle(0, 0, bounds.width, bounds.height, 5, 5);
		});
		final var t = new Text(c, controlStyle);
		t.setForeground(GamaColors.getTextColorForBackground(comp.getBackground()).color());

		// force the color, see #2601
		return t;
	}

	@Override
	public void focusGained(final FocusEvent e) {}

	@Override
	public void focusLost(final FocusEvent e) {
		if (e.widget == null || !e.widget.equals(text)) return;
		widgetDefaultSelected(null);
	}

	/**
	 * Gets the control.
	 *
	 * @return the control
	 */
	public Text getControl() { return text; }

	@Override
	public void widgetSelected(final SelectionEvent e) {}

	/**
	 * @see gama.ui.shared.controls.IPopupProvider#getPopupText()
	 */
	public String getPopupText() {
		StringBuilder result = new StringBuilder();
		final var value = getCurrentValue();
		if (currentException != null) {
			result.append(currentException.getMessage());
		} else if (!isOK(value)) {
			result.append("The current value should be of type ").append(expectedType);
		}
		return result.toString();
	}

	/**
	 * Checks if is OK.
	 *
	 * @param value
	 *            the value
	 * @return the boolean
	 */
	private Boolean isOK(final Object value) {
		if (evaluateExpression) return expectedType.canBeTypeOf(scope, value);
		if (value instanceof IExpression)
			return expectedType.isAssignableFrom(((IExpression) value).getGamlType());
		else
			return false;
	}

	/**
	 * Gets the host agent.
	 *
	 * @return the host agent
	 */
	IAgent getHostAgent() { return hostAgent == null ? editor == null ? null : editor.getAgent() : hostAgent; }

	/**
	 * @return the currentValue
	 */
	protected Object getCurrentValue() { return currentValue; }

	/**
	 * @param currentValue
	 *            the currentValue to set
	 */
	protected void setCurrentValue(final Object currentValue) { this.currentValue = currentValue; }

	/**
	 * @param currentValue2
	 */
	public void displayValue(final Object currentValue2) {
		setCurrentValue(evaluateExpression ? expectedType == Types.STRING
				? StringUtils.toJavaString(GamaStringType.staticCast(scope, currentValue2, false))
				: expectedType.cast(scope, currentValue2, null, false) : currentValue2);
		if (text.isDisposed()) return;
		if (expectedType == Types.STRING) {
			text.setText(currentValue == null ? "" : StringUtils.toJavaString(currentValue.toString()));
		} else {
			text.setText(StringUtils.toGaml(currentValue2, false));
		}
	}

}
