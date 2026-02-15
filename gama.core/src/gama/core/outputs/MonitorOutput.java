/*******************************************************************************************************
 *
 * MonitorOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.data.csv.CsvWriter;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IValue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IExperimentDisplayable;
import gama.api.ui.IGui;
import gama.api.ui.IItemList;
import gama.api.utils.color.GamaColor;
import gama.api.utils.color.GamaColorFactory;
import gama.api.utils.files.FileUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.gaml.operators.Files;

/**
 * The Class MonitorOutput.
 *
 * @author drogoul
 */
@symbol (
		name = IKeyword.MONITOR,
		kind = ISymbolKind.OUTPUT,
		with_sequence = false,
		concept = { IConcept.MONITOR })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.LABEL,
				optional = false,
				doc = @doc ("identifier of the monitor")),
				@facet (
						name = IKeyword.COLOR,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("Indicates the (possibly dynamic) color of this output (default is a light gray)")),
				@facet (
						name = IKeyword.REFRESH,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Indicates the condition under which this output should be refreshed (default is true)")),
				@facet (
						name = IKeyword.VALUE,
						type = IType.NONE,
						optional = false,
						doc = @doc ("expression that will be evaluated to be displayed in the monitor")) },
		omissible = IKeyword.NAME)
@inside (
		symbols = { IKeyword.OUTPUT, IKeyword.PERMANENT })
@doc (
		value = "A monitor allows to follow the value of an arbitrary expression in GAML.",
		usages = { @usage (
				value = "An example of use is:",
				examples = @example (
						value = "monitor \"nb preys\" value: length(prey as list) refresh_every: 5;  ",
						isExecutable = false)) })
public class MonitorOutput extends AbstractValuedDisplayOutput implements IExperimentDisplayable {

	/** The monitor folder. */
	private static String monitorFolder = "monitors";

	/** The color expression. */
	protected IExpression colorExpression = null;

	/** The color. */
	protected IColor color = null;

	/** The constant color. */
	protected IColor constantColor = null;

	/** The history. */
	protected List<Object> history;

	/** The should be initialized. */
	protected boolean shouldBeInitialized;

	/**
	 * Instantiates a new monitor output.
	 *
	 * @param desc
	 *            the desc
	 */
	public MonitorOutput(final IDescription desc) {
		super(desc);
		setColor(getFacet(IKeyword.COLOR));
	}

	/**
	 * @param facet
	 */
	private void setColor(final IExpression facet) {
		colorExpression = facet;
		if (facet != null && facet.isConst()) {
			constantColor = Types.COLOR.cast(null, facet.getConstValue(), null, false);
			return;
		}
		if (colorExpression == null) {
			final ITopLevelAgent sim = GAMA.getSimulation();
			if (sim != null) {
				constantColor = sim.getColor();
			} else {
				constantColor = GamaColorFactory.GRAY;
			}
		}
	}

	/**
	 * Sets the color.
	 *
	 * @param gamaColor
	 *            the new color
	 */
	public void setColor(final GamaColor gamaColor) {
		color = gamaColor;
		constantColor = gamaColor;
		colorExpression = GAML.getExpressionFactory().createConst(gamaColor, Types.COLOR);
	}

	/**
	 * Instantiates a new monitor output.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param expr
	 *            the expr
	 */
	public MonitorOutput(final IScope scope, final String name, final String expr) {
		super(GAML.getDescriptionFactory().create(IKeyword.MONITOR, IKeyword.VALUE, expr == null ? "" : expr,
				IKeyword.NAME, name == null ? expr : name));
		shouldBeInitialized = true;
		setScope(scope.copy("in monitor '" + name + "'"));
		setNewExpressionText(expr);
		if (getScope().init(this).passed()) {
			getScope().getSimulation().addOutput(this);
			setPaused(false);
			open();
		}
	}

	/**
	 * Should be initialized.
	 *
	 * @return true, if successful
	 */
	public boolean shouldBeInitialized() {
		return shouldBeInitialized;
	}

	/**
	 * Should not be initialized.
	 */
	public void shouldNotBeInitialized() {
		shouldBeInitialized = false;
	}

	@Override
	protected boolean shouldOpenView() {
		return !GamaPreferences.Runtime.CORE_MONITOR_PARAMETERS.getValue();
	}

	@Override
	public String getViewId() { return IGui.MONITOR_VIEW_ID; }

	@Override
	public String getId() { return getViewId() + ":" + getName(); }

	@Override
	public boolean step(final IScope scope) {
		try {
			getScope().setCurrentSymbol(this);
			if (getScope().interrupted()) return false;
			if (getValue() != null) {
				try {
					lastValue = getValue().value(getScope());
					if (history != null) { history.add(lastValue); }
				} catch (final GamaRuntimeException e) {
					lastValue = IItemList.ERROR_CODE + e.getMessage();
				}
			} else {
				lastValue = null;
			}
			if (constantColor == null && colorExpression != null) {
				color = GamaColorFactory.createFrom(scope, colorExpression.value(scope));
			}
		} finally {
			scope.setCurrentSymbol(null);
		}
		return true;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	@Override
	public IColor getColor(final IScope scope) {

		return constantColor == null ? color : constantColor;
	}

	@Override
	public boolean isUnique() { return true; }

	@Override
	public String getName() {
		String result = super.getName();
		if (result == null) { result = getExpressionText(); }
		return result;
	}

	@Override
	protected void setValue(final IExpression value) {
		if (history != null) {
			history.clear();
			history = null;
		}
		super.setValue(value);
		if (value != null) {
			final IType<?> t = value.getGamlType();
			if (t.isNumber() || t.isContainer() && t.getContentType().isNumber()) {
				history = GamaListFactory.create(t);
			}
		}
	}

	/**
	 * Save history.
	 */
	public void saveHistory() {
		if (getScope() == null || history == null || history.isEmpty()) return;
		Files.newFolder(getScope(), monitorFolder);
		String file =
				monitorFolder + "/" + "monitor_" + getName() + "_cycle_" + getScope().getClock().getCycle() + ".csv";
		file = FileUtils.constructAbsoluteFilePath(getScope(), file, false);
		try (final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				final CsvWriter w = new CsvWriter(bw)) {
			for (final Object o : history) {
				String[] strings = null;
				if (o instanceof Number) {
					strings = new String[] { o.toString() };
				} else if (o instanceof List) {
					final List<?> l = (List<?>) o;
					strings = new String[l.size()];
					for (int i = 0; i < strings.length; i++) { strings[i] = l.get(i).toString(); }
				}
				w.writeRecord(strings);
			}

		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getTitle() {
		final StringBuilder sb = new StringBuilder(100);
		sb.append(getName()).append(": ");
		final Object v = getLastValue();
		sb.append(v == null ? "nil" : v instanceof IValue i ? i.serializeToGaml(true) : v.toString());
		if (isPaused()) { sb.append(" (paused)"); }
		return sb.toString();

	}

	@Override
	public String getUnitLabel(final IScope scope) {
		return null;
	}

	@Override
	public boolean isDefinedInExperiment() { return false; }

	@Override
	public String getCategory() { return "Monitors"; }

}
