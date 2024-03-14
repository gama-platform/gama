/*******************************************************************************************************
 *
 * ExperimentJob.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.job;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.model.IModel;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.COUNTER;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.descriptions.ExperimentDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.operators.Cast;
import gama.headless.common.Display2D;
import gama.headless.common.Globals;
import gama.headless.core.GamaHeadlessException;
import gama.headless.core.IRichExperiment;
import gama.headless.core.RichExperiment;
import gama.headless.core.RichOutput;
import gama.headless.xml.Writer;
import gama.headless.xml.XmlTAG;
import gaml.compiler.gaml.validation.GamlModelBuilder;

/**
 * The Class ExperimentJob.
 */
public class ExperimentJob implements IExperimentJob {

	static {
		DEBUG.ON();
	}

	/**
	 * The Enum OutputType.
	 */
	public enum OutputType {

		/** The output. */
		OUTPUT,
		/** The experiment attribute. */
		EXPERIMENT_ATTRIBUTE,
		/** The simulation attribute. */
		SIMULATION_ATTRIBUTE
	}

	/**
	 * Variable listeners
	 */
	protected ListenedVariable[] listenedVariables;

	/** The parameters. */
	protected List<Parameter> parameters;

	/** The outputs. */
	protected List<Output> outputs;

	/** The output file. */
	protected Writer outputFile;

	/** The source path. */
	protected String sourcePath;

	/** The experiment name. */
	public String experimentName;

	/** The model name. */
	protected String modelName;

	/** The seed. */
	public double seed;
	/**
	 * current step
	 */
	public long step;

	/**
	 * id of current experiment
	 */
	private String experimentID;

	/** The final step. */
	public long finalStep;

	/** The until cond. */
	protected String untilCond;

	/** The end condition. */
	// public IExpression endCondition;

	/**
	 * simulator to be loaded
	 */
	public volatile IRichExperiment simulator;

	/**
	 * Instantiates a new experiment job.
	 *
	 * @param clone
	 *            the clone
	 */
	public ExperimentJob(final ExperimentJob clone) {
		untilCond = "";
		this.experimentID = clone.experimentID != null ? clone.experimentID : "" + ExperimentJob.generateID();
		this.sourcePath = clone.sourcePath;
		this.finalStep = clone.finalStep;
		this.experimentName = clone.experimentName;
		this.modelName = clone.modelName;
		this.parameters = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.listenedVariables = clone.listenedVariables;
		this.step = clone.step;
		this.seed = clone.seed;
		for (final Parameter p : clone.parameters) { this.addParameter(new Parameter(p)); }
		for (final Output o : clone.outputs) { this.addOutput(new Output(o)); }

	}

	/**
	 * Gets the source path.
	 *
	 * @return the source path
	 */
	public String getSourcePath() { return sourcePath; }

	/**
	 * Generate ID.
	 *
	 * @return the long
	 */
	private static long generateID() {
		return COUNTER.GET_UNIQUE();
	}

	/**
	 * Sets the buffered writer.
	 *
	 * @param w
	 *            the new buffered writer
	 */
	public void setBufferedWriter(final Writer w) { this.outputFile = w; }

	@Override
	public void addParameter(final Parameter p) {
		this.parameters.add(p);
	}

	@Override
	public void addOutput(final Output p) {
		p.setId("" + outputs.size());
		this.outputs.add(p);
	}

	/**
	 * Instantiates a new experiment job.
	 *
	 * @param sourcePath
	 *            the source path
	 * @param exp
	 *            the exp
	 * @param max
	 *            the max
	 * @param untilCond
	 *            the until cond
	 * @param s
	 *            the s
	 */
	public ExperimentJob(final String sourcePath, final String exp, final long max, final String untilCond,
			final double s) {
		this(sourcePath, Long.toString(ExperimentJob.generateID()), exp, max, untilCond, s);
	}

	/**
	 * Instantiates a new experiment job.
	 *
	 * @param sourcePath
	 *            the source path
	 * @param expId
	 *            the exp id
	 * @param exp
	 *            the exp
	 * @param max
	 *            the max
	 * @param untilCond
	 *            the until cond
	 * @param s
	 *            the s
	 */
	public ExperimentJob(final String sourcePath, final String expId, final String exp, final long max,
			final String untilCond, final double s) {
		parameters = new ArrayList<>();
		outputs = new ArrayList<>();
		this.experimentID = expId;
		this.sourcePath = sourcePath;
		this.finalStep = max;
		this.untilCond = untilCond;
		this.experimentName = exp;
		this.seed = s;
		this.modelName = null;

	}

	@Override
	public void loadAndBuild() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			IOException, GamaHeadlessException {

		this.load();
		this.listenedVariables = new ListenedVariable[outputs.size()];

		for (final Parameter temp : parameters) {
			if (temp.getName() == null || "".equals(temp.getName())) {
				this.simulator.setParameter(temp.getVar(), temp.getValue());
			} else {
				this.simulator.setParameter(temp.getName(), temp.getValue());
			}
		}
		this.setup();
		simulator.setup(experimentName, this.seed);
		for (int i = 0; i < outputs.size(); i++) {
			final Output temp = outputs.get(i);
			this.listenedVariables[i] = new ListenedVariable(temp.getName(), temp.getWidth(), temp.getHeight(),
					temp.getFrameRate(), simulator.getTypeOf(temp.getName()), temp.getOutputPath());
		}
		simulator.getExperimentPlan().setStopCondition(untilCond);

		// // Initialize the enCondition
		// if (untilCond == null || "".equals(untilCond)) {
		// endCondition = IExpressionFactory.FALSE_EXPR;
		// } else {
		// endCondition = GAML.getExpressionFactory().createExpr(untilCond, simulator.getModel().getDescription());
		// // endCondition = GAML.compileExpression(untilCond, simulator.getSimulation(), true);
		// }

	}

	/**
	 * Load.
	 *
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GamaHeadlessException
	 *             the gama headless exception
	 */
	public void load() throws IOException, GamaHeadlessException {
		System.setProperty("user.dir", this.sourcePath);
		final List<GamlCompilationError> errors = new ArrayList<>();
		final IModel mdl = GamlModelBuilder.getDefaultInstance().compile(new File(this.sourcePath), errors, null);
		this.modelName = mdl.getName();
		this.simulator = new RichExperiment(mdl);
	}

	/**
	 * Setup.
	 */
	public void setup() {
		this.step = 0;

	}

	@Override
	public void playAndDispose() {
		DEBUG.TIMER("GAMA", "Simulation running ", "for: ", () -> {
			play();
			dispose();
		});
	}

	@Override
	public void play() {
		if (this.outputFile != null) { this.outputFile.writeSimulationHeader(this); }
		// DEBUG.LOG("Simulation is running...", false);
		final long affDelay = finalStep < 100 ? 1 : finalStep / 100;

		try {
			int step = 0;
			// Added because the simulation may be null in case we deal with a batch experiment
			while (finalStep >= 0 ? step < finalStep : true) {
				if (step % affDelay == 0) { DEBUG.LOG(".", false); }
				if (simulator.isInterrupted()) { break; }
				final SimulationAgent sim = simulator.getSimulation();
				final ExperimentAgent exp = simulator.getExperimentPlan().getAgent();
				final IScope scope = sim == null ? exp.getScope() : sim.getScope();
				if (Cast.asBool(scope, exp.getStopCondition().value(scope))) { break; }
				doStep();
				step++;
			}
		} catch (final GamaRuntimeException e) {
			DEBUG.ERR("\n The simulation has stopped before the end due to the following exception: ", e);
		}
	}

	@Override
	public void dispose() {
		if (this.simulator != null) {
			this.simulator.dispose();
			this.simulator = null;
		}
		if (this.outputFile != null) {
			this.outputFile.close();
			this.outputFile = null;
		}
	}

	@Override
	public void doStep() {
		this.step = simulator.step();
		this.exportVariables();
	}

	@Override
	public void doBackStep() {
		this.step = simulator.backStep();
		// this.exportVariables(); ?
	}

	@Override
	public String getExperimentID() { return experimentID; }

	/**
	 * Sets the id of current experiment.
	 *
	 * @param experimentID
	 *            the new id of current experiment
	 */
	public void setExperimentID(final String experimentID) { this.experimentID = experimentID; }

	/**
	 * Gets the variable listeners.
	 *
	 * @return the variable listeners
	 */
	public ListenedVariable[] getListenedVariables() { return listenedVariables; }

	/**
	 * Export variables.
	 */
	protected void exportVariables() {
		final int size = this.listenedVariables.length;
		if (size == 0) return;
		for (int i = 0; i < size; i++) {
			final ListenedVariable v = this.listenedVariables[i];
			if (this.step % v.getFrameRate() == 0) {
				final RichOutput out = simulator.getRichOutput(v);
				if (out == null || out.getValue() == null) {} else if (out.getValue() instanceof BufferedImage) {
					v.setValue(writeImageInFile((BufferedImage) out.getValue(), v.getName(), v.getPath()), out.getType());
				} else {
					v.setValue(out.getValue(), out.getType());
				}
			} else {
				v.setValue(null);
			}
		}
		if (this.outputFile != null) { this.outputFile.writeResultStep(this.step, this.listenedVariables); }

	}

	@Override
	public long getStep() { return step; }

	/**
	 * Write image in file.
	 *
	 * @param img
	 *            the img
	 * @param name
	 *            the name
	 * @param outputPath
	 *            the output path
	 * @return the display 2 D
	 */
	protected Display2D writeImageInFile(final BufferedImage img, final String name, final String outputPath) {
		final String fileName = name + this.getExperimentID() + "-" + step + ".png";
		String fileFullName = Globals.IMAGES_PATH + "/" + fileName;
		if (!"".equals(outputPath)  && outputPath != null) {
			// a specific output path has been specified with the "output_path"
			// keyword in the xml
			fileFullName = outputPath + "-" + step + ".png";
			// check if the folder exists, create a new one if it does not
			final File tmp = new File(fileFullName);
			tmp.getParentFile().mkdirs();
		}
		try {
			ImageIO.write(img, "png", new File(fileFullName));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new Display2D(name + this.getExperimentID() + "-" + step + ".png");
	}

	@Override
	public void setSeed(final double s) { this.seed = s; }

	@Override
	public double getSeed() { return this.seed; }

	@Override
	public Element asXMLDocument(final Document doc) {
		final Element simulation = doc.createElement(XmlTAG.SIMULATION_TAG);

		final Attr attr = doc.createAttribute(XmlTAG.ID_TAG);
		attr.setValue(this.experimentID);
		simulation.setAttributeNode(attr);

		final Attr attr3 = doc.createAttribute(XmlTAG.SOURCE_PATH_TAG);
		attr3.setValue(this.sourcePath);
		simulation.setAttributeNode(attr3);

		final Attr attr2 = doc.createAttribute(XmlTAG.FINAL_STEP_TAG);
		attr2.setValue(Long.toString(this.finalStep));
		simulation.setAttributeNode(attr2);

		final Attr attr5 = doc.createAttribute(XmlTAG.SEED_TAG);
		attr5.setValue(Float.toString((float) this.seed));
		simulation.setAttributeNode(attr5);

		final Attr attr4 = doc.createAttribute(XmlTAG.EXPERIMENT_NAME_TAG);
		attr4.setValue(this.experimentName);
		simulation.setAttributeNode(attr4);

		final Element parameters = doc.createElement(XmlTAG.PARAMETERS_TAG);
		simulation.appendChild(parameters);

		for (final Parameter p : this.parameters) {
			final Element aparameter = doc.createElement(XmlTAG.PARAMETER_TAG);
			parameters.appendChild(aparameter);

			final Attr ap1 = doc.createAttribute(XmlTAG.NAME_TAG);
			ap1.setValue(p.getName());
			aparameter.setAttributeNode(ap1);

			final Attr ap2 = doc.createAttribute(XmlTAG.VAR_TAG);
			ap2.setValue(p.getVar());
			aparameter.setAttributeNode(ap2);

			final Attr ap3 = doc.createAttribute(XmlTAG.TYPE_TAG);
			ap3.setValue(p.getType().toString());
			aparameter.setAttributeNode(ap3);

			final Attr ap4 = doc.createAttribute(XmlTAG.VALUE_TAG);
			ap4.setValue(p.getValue().toString());
			aparameter.setAttributeNode(ap4);
		}

		final Element outputs = doc.createElement(XmlTAG.OUTPUTS_TAG);
		simulation.appendChild(outputs);

		for (final Output o : this.outputs) {
			final Element aOutput = doc.createElement(XmlTAG.OUTPUT_TAG);
			outputs.appendChild(aOutput);

			final Attr o3 = doc.createAttribute(XmlTAG.ID_TAG);
			o3.setValue(o.getId());
			aOutput.setAttributeNode(o3);

			final Attr o1 = doc.createAttribute(XmlTAG.NAME_TAG);
			o1.setValue(o.getName());
			aOutput.setAttributeNode(o1);

			final Attr o2 = doc.createAttribute(XmlTAG.FRAMERATE_TAG);
			o2.setValue(Integer.toString(o.getFrameRate()));
			aOutput.setAttributeNode(o2);
		}
		return simulation;
	}

	/**
	 * Load and build job.
	 *
	 * @param expD
	 *            the exp D
	 * @param path
	 *            the path
	 * @param model
	 *            the model
	 * @return the experiment job
	 */
	public static ExperimentJob loadAndBuildJob(final ExperimentDescription expD, final String path,
			final IModel model) {
		final String expName = expD.getName();
		final IExpressionDescription seedDescription = expD.getFacet(IKeyword.SEED);
		double mseed = 0.0;
		if (seedDescription != null) { mseed = Double.parseDouble(seedDescription.getExpression().literalValue()); }
		final IDescription d = expD.getChildWithKeyword(IKeyword.OUTPUT);
		final ExperimentJob expJob =
				new ExperimentJob(path, Long.toString(ExperimentJob.generateID()), expName, 0, "", mseed);

		if (d != null) {
			final Iterable<IDescription> monitors = d.getChildrenWithKeyword(IKeyword.MONITOR);
			for (final IDescription moni : monitors) { expJob.addOutput(Output.loadAndBuildOutput(moni)); }

			final Iterable<IDescription> displays = d.getChildrenWithKeyword(IKeyword.DISPLAY);
			for (final IDescription disp : displays) {
				if (disp.getFacetExpr(IKeyword.VIRTUAL) != IExpressionFactory.TRUE_EXPR) {
					expJob.addOutput(Output.loadAndBuildOutput(disp));
				}
			}
		}

		final Iterable<IDescription> parameters = expD.getChildrenWithKeyword(IKeyword.PARAMETER);
		for (final IDescription para : parameters) {
			expJob.addParameter(Parameter.loadAndBuildParameter(para, model));
		}

		return expJob;
	}

	@Override
	public String getExperimentName() {

		return this.experimentName;
	}

	/**
	 * Gets the parameter.
	 *
	 * @param name
	 *            the name
	 * @return the parameter
	 */
	private Parameter getParameter(final String name) {
		for (final Parameter p : parameters) { if (p.getName().equals(name)) return p; }
		return null;
	}

	@Override
	public List<Parameter> getParameters() { return this.parameters; }

	/**
	 * Gets the output.
	 *
	 * @param name
	 *            the name
	 * @return the output
	 */
	private Output getOutput(final String name) {
		for (final Output p : outputs) { if (p.getName().equals(name)) return p; }
		return null;
	}

	@Override
	public List<Output> getOutputs() { return this.outputs; }

	@Override
	public void setParameterValueOf(final String name, final Object val) {
		this.getParameter(name).setValue(val);
	}

	@Override
	public void removeOutputWithName(final String name) {
		this.outputs.remove(this.getOutput(name));
	}

	@Override
	public void setOutputFrameRate(final String name, final int frameRate) {
		this.getOutput(name).setFrameRate(frameRate);
	}

	@Override
	public List<String> getOutputNames() {
		final List<String> res = new ArrayList<>();
		for (final Output o : outputs) { res.add(o.getName()); }
		return res;
	}

	/**
	 * Gets the final step.
	 *
	 * @return the final step
	 */
	public long getFinalStep() { return finalStep; }

	@Override
	public void setFinalStep(final long finalStep) { this.finalStep = finalStep; }

	@Override
	public String getModelName() { return this.modelName; }

}
