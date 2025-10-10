/*******************************************************************************************************
 *
 * GamaServerExperimentJob.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.java_websocket.WebSocket;

import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.ExperimentPlan;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.model.IModelSpecies;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.compilation.GamaCompilationFailedException;
import gama.gaml.compilation.GamlCompilationError;
import gama.gaml.types.Types;
import gama.headless.core.GamaHeadlessException;
import gama.headless.core.RichExperiment;
import gama.headless.core.RichOutput;
import gama.headless.job.ExperimentJob;
import gama.headless.job.ListenedVariable;
import gaml.compiler.gaml.validation.GamlModelBuilder;

/**
 * The Class ExperimentJob.
 */
public class GamaServerExperimentJob extends ExperimentJob {

	/** The socket. */
	public WebSocket socket;

	/** The params. */
	// public GamaJsonList params;

	/** The end cond. */
	// public String endCond = "";

	/** The controller. */
	public GamaServerExperimentController controller;

	/** The play command. */
	// public IMap<String, Object> playCommand;

	/**
	 * Instantiates a new manual experiment job.
	 *
	 * @param sourcePath
	 *            the source path
	 * @param exp
	 *            the exp
	 * @param gamaWebSocketServer
	 *            the gama web socket server
	 * @param sk
	 *            the sk
	 * @param p
	 *            the p
	 * @param console
	 *            the console
	 * @param status
	 *            the status
	 * @param dialog
	 *            the dialog
	 */
	public GamaServerExperimentJob(final String sourcePath, final String exp, final WebSocket sk, final IList p,
			final String end, final boolean console, final boolean status, final boolean dialog,
			final boolean runtime) {
		// (final String sourcePath, final String exp, final long max, final String untilCond,
		// final double s)
		super(sourcePath, exp, 0, "", 0);
		socket = sk;
		// params = p;
		controller = new GamaServerExperimentController(this, p, end, socket, console, status, dialog, runtime);
	}

	@Override
	public void doStep() {
		this.step = simulator.step();
	}

	@Override
	public void doBackStep() {
		this.step = simulator.backStep();
	}

	@Override
	public void load() throws IOException, GamaCompilationFailedException {
		System.setProperty("user.dir", this.sourcePath);
		final List<GamlCompilationError> errors = new ArrayList<>();
		final IModelSpecies mdl = GamlModelBuilder.getDefaultInstance().compile(new File(this.sourcePath), errors, null);
		this.modelName = mdl.getName();
		this.simulator = new RichExperiment(mdl);
	}

	/**
	 * Load and build with json.
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
	public void loadAndBuildWithJson(final IList params, final String endCond)
			throws IOException, GamaCompilationFailedException {
		if (this.simulator == null) { this.load(); }
		this.setup();
		controller.setExperiment(simulator.getModel().getExperiment(experimentName));
		simulator.setup(experimentName, this.seed, params, this);
		simulator.getExperimentPlan().setStopCondition(endCond);
	}

	/**
	 * Inits the param.
	 *
	 * @param p
	 *            the p
	 */
	@SuppressWarnings ("unchecked")
	public void initParam(final IList p) {
		IList params = p;
		if (params != null) {
			final ExperimentPlan curExperiment = (ExperimentPlan) simulator.getExperimentPlan();
			for (var param : params.listValue(null, Types.MAP, false)) {
				IMap<String, Object> m = (IMap<String, Object>) param;
				String type = m.get(IKeyword.TYPE).toString();
				Object v = m.get(IKeyword.VALUE);
				if (IKeyword.INT.equals(type)) { v = Integer.valueOf("" + m.get(IKeyword.VALUE)); }
				if (IKeyword.FLOAT.equals(type)) { v = Double.valueOf("" + m.get(IKeyword.VALUE)); }

				final IParameter.Batch b = curExperiment.getParameterByTitle(m.get(IKeyword.NAME).toString());
				if (b != null) {
					curExperiment.setParameterValueByTitle(curExperiment.getExperimentScope(),
							m.get(IKeyword.NAME).toString(), v);
				} else {
					curExperiment.setParameterValue(curExperiment.getExperimentScope(), m.get(IKeyword.NAME).toString(),
							v);
				}

			}
		}
	}

	@Override
	public void exportVariables() {
		final int size = this.listenedVariables.length;
		if (size == 0) return;
		for (int i = 0; i < size; i++) {
			final ListenedVariable v = this.listenedVariables[i];
			if (this.step % v.getFrameRate() == 0) {
				final RichOutput out = simulator.getRichOutput(v);
				if (out == null || out.getValue() == null) {} else if (out.getValue() instanceof BufferedImage) {
					try (ByteArrayOutputStream out1 = new ByteArrayOutputStream()) {
						BufferedImage bi = (BufferedImage) out.getValue();

						ImageIO.write(bi, "png", out1);

						byte[] array1 = out1.toByteArray();
						byte[] array2 = { (byte) 0 };
						byte[] array3 = { (byte) i };
						byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length + array3.length);
						System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
						System.arraycopy(array3, 0, joinedArray, array1.length + array2.length, array3.length);

						ByteBuffer byteBuffer = ByteBuffer.wrap(joinedArray);
						if (!socket.isClosing() && !socket.isClosed()) { socket.send(byteBuffer); }
						// server.broadcast(byteBuffer);
						byteBuffer.clear();

					} catch (IOException e) {
						e.printStackTrace();
					}
					// v.setValue(writeImageInFile((BufferedImage) out.getValue(), v.getName(), v.getPath()), step,
					// out.getType());
				} else {
					byte[] array1 = (out.getName() + ": " + out.getValue().toString()).getBytes();
					byte[] array2 = { (byte) 1 };
					byte[] array3 = { (byte) i };
					byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length + array3.length);
					System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
					System.arraycopy(array3, 0, joinedArray, array1.length + array2.length, array3.length);

					ByteBuffer byteBuffer = ByteBuffer.wrap(joinedArray);
					if (!socket.isClosing() && !socket.isClosed()) { socket.send(byteBuffer); }
					v.setValue(out.getValue(), out.getType());
				}
			} else {
				v.setValue(null);
			}
		}
		// if (this.outputFile != null) {
		// this.outputFile.writeResultStep(this.step, this.listenedVariables);
		// }

	}

}
