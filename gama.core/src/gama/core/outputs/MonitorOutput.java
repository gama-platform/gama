/*******************************************************************************************************
 *
 * MonitorOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.IValue;
import gama.core.common.interfaces.ItemList;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.util.FileUtils;
import gama.core.kernel.experiment.IExperimentDisplayable;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaListFactory;
import gama.core.util.file.csv.CsvWriter;
import gama.gaml.compilation.GAML;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Files;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
	protected GamaColor color = null;

	/** The constant color. */
	protected GamaColor constantColor = null;

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
				constantColor = GamaColor.get(Color.gray);
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
		super(DescriptionFactory.create(IKeyword.MONITOR, IKeyword.VALUE, expr == null ? "" : expr, IKeyword.NAME,
				name == null ? expr : name));
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
					lastValue = ItemList.ERROR_CODE + e.getMessage();
				}
			} else {
				lastValue = null;
			}
			if (constantColor == null && colorExpression != null) {
				color = Cast.asColor(scope, colorExpression.value(scope));
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
	public GamaColor getColor(final IScope scope) {

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
		sb.append(v == null ? "nil" : v instanceof IValue ? ((IValue) v).serializeToGaml(true) : v.toString());
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
