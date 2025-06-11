/*******************************************************************************************************
 *
 * HeadlessApplication.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.runtime;

import static gama.headless.runtime.GamaHeadlessWebSocketServer.startForSecureHeadless;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.w3c.dom.Document;

import com.google.inject.Injector;

import gama.core.common.GamlFileExtension;
import gama.core.common.preferences.GamaPreferences;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.model.IModel;
import gama.core.runtime.GAMA;
import gama.core.runtime.NullGuiHandler;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.runtime.server.IGamaServer;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamaCompilationFailedException;
import gama.gaml.compilation.GamlCompilationError;
import gama.headless.batch.ModelLibraryRunner;
import gama.headless.batch.ModelLibraryTester;
import gama.headless.batch.ModelLibraryValidator;
import gama.headless.batch.documentation.ModelLibraryGenerator;
import gama.headless.common.Globals;
import gama.headless.common.HeadLessErrors;
import gama.headless.core.GamaHeadlessException;
import gama.headless.job.ExperimentJob;
import gama.headless.job.IExperimentJob;
import gama.headless.script.ExperimentationPlanFactory;
import gama.headless.server.GamaServerGUIHandler;
import gama.headless.xml.ConsoleReader;
import gama.headless.xml.Reader;
import gama.headless.xml.XMLWriter;
import gaml.compiler.GamlStandaloneSetup;
import gaml.compiler.gaml.validation.GamlModelBuilder;

/**
 * The Class Application.
 */
public class HeadlessApplication implements IApplication {

	/** The injector. */
	static Injector INJECTOR;

	/**
	 * Gets the injector.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the injector
	 * @date 15 oct. 2023
	 */
	public static Injector getInjector() { return configureInjector(); }

	/**
	 * Configure injector.
	 *
	 * @return the injector
	 */
	private static Injector configureInjector() {
		if (INJECTOR != null) {
			return INJECTOR;
		}
		DEBUG.LOG("GAMA configuring and loading...");
		System.setProperty("java.awt.headless", "true");
		GAMA.setHeadLessMode(isServer);
		GAMA.setHeadlessGui(isServer ? new GamaServerGUIHandler() : new NullGuiHandler());
		try {
			// We initialize XText and Gaml.
			INJECTOR = GamlStandaloneSetup.doSetup();
		} catch (final Exception e1) {
			throw GamaRuntimeException.create(e1, GAMA.getRuntimeScope());
		}
		// SEED HACK // WARNING AD : Why ?
		GamaPreferences.External.CORE_SEED_DEFINED.set(true);
		GamaPreferences.External.CORE_SEED.set(1.0);
		// SEED HACK
		return INJECTOR;
	}

	/** The Constant HELP_PARAMETER. */
	final public static String HELP_PARAMETER = "-help";

	/** The Constant GAMA_VERSION. */
	final public static String GAMA_VERSION = "-version";

	/** The Constant CONSOLE_PARAMETER. */
	final public static String CONSOLE_PARAMETER = "-c";

	/** The Constant VERBOSE_PARAMETER. */
	final public static String VERBOSE_PARAMETER = "-v";

	/** The Constant THREAD_PARAMETER. */
	final public static String THREAD_PARAMETER = "-hpc";

	/** The Constant THREAD_PARAMETER. */
	final public static String STEPS_PARAMETER = "-steps";

	/** The Constant PING_INTERVAL. */
	final public static String PING_INTERVAL = "-ping_interval";

	/** The Constant SOCKET_PARAMETER. */
	final public static String SOCKET_PARAMETER = "-socket";

	/** The Constant SECURE_SSL_SOCKET_PARAMETER. */
	final public static String SSOCKET_PARAMETER = "-ssocket";

	/** The Constant SECURE_SSL_SOCKET_PARAMETER_JKSPATH. */
	final public static String SSOCKET_PARAMETER_JKSPATH = "-jks";

	/** The Constant SECURE_SSL_SOCKET_PARAMETER_SPWD. */
	final public static String SSOCKET_PARAMETER_SPWD = "-spwd";

	/** The Constant SECURE_SSL_SOCKET_PARAMETER_KPWD. */
	final public static String SSOCKET_PARAMETER_KPWD = "-kpwd";

	/** The Constant TUNNELING_PARAMETER. */
	final public static String TUNNELING_PARAMETER = "-p";

	/** The Constant VALIDATE_LIBRARY_PARAMETER. */
	final public static String VALIDATE_LIBRARY_PARAMETER = "-validate";

	/** The Constant TEST_LIBRARY_PARAMETER. */
	final public static String TEST_LIBRARY_PARAMETER = "-test";

	/** The Constant BUILD_XML_PARAMETER. */
	final public static String BUILD_XML_PARAMETER = "-xml";

	/** The Constant CHECK_MODEL_PARAMETER. */
	final public static String CHECK_MODEL_PARAMETER = "-check";

	/** The Constant RUN_LIBRARY_PARAMETER. */
	final public static String RUN_LIBRARY_PARAMETER = "-runLibrary";

	/** The Constant BATCH_PARAMETER. */
	// -> Code still exist, but not documented nor use
	final public static String BATCH_PARAMETER = "-batch";

	/** The Constant GAML_PARAMETER. */
	final public static String GAML_PARAMETER = "-gaml";

	/** The Constant WRITE_XMI. */
	final public static String WRITE_XMI = "-write-xmi";

	/** The socket. */
	private int socket = -1;

	/** The ping interval. */
	// the interval between each ping sent by the server, -1 to deactivate this behaviour
	private int ping = IGamaServer.DEFAULT_PING_INTERVAL;

	/** The console mode. */
	private boolean consoleMode = false;

	/** The tunneling mode. */
	private boolean tunnelingMode = false;

	/** The verbose. */
	private boolean verbose = false;

	/** The processor queue. */
	private final SimulationRuntime processorQueue = new SimulationRuntime();

	/** The is server. */
	private static boolean isServer = false;

	/**
	 * Show version.
	 */
	private static void showVersion() {
		DEBUG.ON();
		DEBUG.LOG("Welcome to Gama-platform.org version " + GAMA.VERSION + "\n");
		DEBUG.OFF();
	}

	/**
	 * Show help.
	 */
	private static void showHelp() {
		showVersion();
		DEBUG.ON();
		DEBUG.LOG("sh ./gama-headless.sh [Options]\n" + "\nList of available options:" + "\n\t=== Headless Options ==="
				+ "\n\t\t-m [mem]                      -- allocate memory (ex 2048m)" 
				+ "\n\t\t-ws [./path/to/ws]            -- manually set a workspace" + "\n\t\t" + CONSOLE_PARAMETER
				+ "                            -- start the console to write xml parameter file" + "\n\t\t"
				+ VERBOSE_PARAMETER + "                            -- verbose mode" + "\n\t\t" + THREAD_PARAMETER
				+ " [core]                   -- set the number of core available for experimentation" + "\n\t\t"
				+ TUNNELING_PARAMETER
				+ "                            -- start pipeline to interact with another framework" + "\n\t\t"
				+ PING_INTERVAL + " [pingInterval] "
				+ "-- when in server mode (socket parameter set), defines in milliseconds the time "
				+ "between each ping packet sent to clients to keep alive the connection. "
				+ "The default value is 10000, set to -1 to deactivate this behaviour." + "\n\t=== Infos ===" + "\n\t\t"
				+ HELP_PARAMETER + "                         -- get the help of the command line" + "\n\t\t"
				+ GAMA_VERSION + "                      -- get the the version of gama" + "\n\t=== Library Runner ==="
				+ "\n\t\t" + VALIDATE_LIBRARY_PARAMETER
				+ "                     -- invokes GAMA to validate models present in built-in library and plugins"
				+ "\n\t\t" + TEST_LIBRARY_PARAMETER
				+ "                         -- invokes GAMA to execute the tests present in built-in library and plugins and display their results"
				+ "\n\t=== GAMA Headless Runner ===" + "\n\t\t" + SOCKET_PARAMETER
				+ " [socketPort]          -- starts socket pipeline to interact with another framework" + "\n\t\t"
				+ BATCH_PARAMETER + " [experimentName] [modelFile.gaml]"
				+ "\n\t\t                              -- Run batch experiment in headless mode"
				// + "\n\t\t" + GAML_PARAMETER + " [experimentName] [modelFile.gaml]"
				// + "\n\t\t -- Run single gaml experiment in headless mode"
				+ "\n\t\t" + BUILD_XML_PARAMETER + " [experimentName] [modelFile.gaml] [xmlOutputFile.xml]"
				+ "\n\t\t                              -- build an xml parameter file from a model"
				+ "\n\t\t[xmlHeadlessFile.xml] [outputDirectory]"
				+ "\n\t\t                              -- default usage of GAMA headless");
		// + "\n\t\t" + WRITE_XMI + " -- write scope provider resource files to disk");
		DEBUG.OFF();
	}

	/**
	 * Check parameters.
	 *
	 * @param args
	 *            the args
	 * @param apply
	 *            the apply
	 * @return true, if successful
	 */
	private boolean checkParameters(final List<String> args) {

		int size = args.size();
		boolean mustContainInFile = true;
		boolean mustContainOutFolder = true;

		// Parameters flag
		// ========================
		if (args.contains(VERBOSE_PARAMETER)) {
			size = size - 1;
			this.verbose = true;
			DEBUG.ON();
			DEBUG.LOG("Log active", true);
		}

		if (args.contains(CONSOLE_PARAMETER)) {
			size = size - 1;
			mustContainInFile = false;
			this.consoleMode = true;
		}
		if (args.contains(TUNNELING_PARAMETER)) {
			size = size - 1;
			mustContainOutFolder = false;
			this.tunnelingMode = true;
		}
		if (args.contains(SOCKET_PARAMETER)) {
			size = size - 2;
			mustContainOutFolder = mustContainInFile = false;
			this.socket = Integer.parseInt(after(args, SOCKET_PARAMETER));
			isServer = true;
		}
		if (args.contains(SSOCKET_PARAMETER)) {
			size = size - 2;
			mustContainOutFolder = mustContainInFile = false;
			this.socket = Integer.parseInt(after(args, SSOCKET_PARAMETER));
			isServer = true;
		}
		if (args.contains(PING_INTERVAL)) {
			size = size - 2;
			this.ping = Integer.parseInt(after(args, PING_INTERVAL));
		}
		if (args.contains(THREAD_PARAMETER)) {
			size = size - 2;
			processorQueue.setNumberOfThreads(Integer.parseInt(after(args, THREAD_PARAMETER)));
		}

		// Commands
		// ========================
		if (args.contains(WRITE_XMI) || args.contains(GAMA_VERSION) || args.contains(HELP_PARAMETER)
				|| args.contains(VALIDATE_LIBRARY_PARAMETER) || args.contains(TEST_LIBRARY_PARAMETER)) {
			size = size - 1;
			mustContainOutFolder = mustContainInFile = false;
		}
		if (args.contains(BATCH_PARAMETER)) {
			size = size - 3;
			mustContainOutFolder = false;
		}
		if (args.contains(BUILD_XML_PARAMETER)) {
			size = size - 4;
			mustContainInFile = mustContainOutFolder = false;
		}

		if (args.contains(GAML_PARAMETER)) { size = size - 2; }

		// Runner verification
		// ========================
		if (mustContainInFile && mustContainOutFolder && size < 2) {
			return showError(HeadLessErrors.INPUT_NOT_DEFINED, null);
		}
		if (!mustContainInFile && mustContainOutFolder && size < 1) {
			return showError(HeadLessErrors.OUTPUT_NOT_DEFINED, null);
		}

		// In/out files
		// ========================
		if (mustContainOutFolder) {
			// Check and create output folder
			Globals.OUTPUT_PATH = args.get(args.size() - 1);
			final File output = new File(Globals.OUTPUT_PATH);
			if (!output.exists() && !output.mkdir()) {
				return showError(HeadLessErrors.PERMISSION_ERROR, Globals.OUTPUT_PATH);
			}
			// Check and create output image folder
			Globals.IMAGES_PATH = Globals.OUTPUT_PATH + "/snapshot";
			final File images = new File(Globals.IMAGES_PATH);
			if (!images.exists() && !images.mkdir()) {
				return showError(HeadLessErrors.PERMISSION_ERROR, Globals.IMAGES_PATH);
			}
		}
		if (mustContainInFile) {
			final int inIndex = args.size() - (mustContainOutFolder ? 2 : 1);
			final File input = new File(args.get(inIndex));
			if (!input.exists()) {
				return showError(HeadLessErrors.NOT_EXIST_FILE_ERROR, args.get(inIndex));
			}
		}
		return true;
	}

	/**
	 * Show error.
	 *
	 * @param errorCode
	 *            the error code
	 * @param path
	 *            the path
	 * @return true, if successful
	 */
	private static boolean showError(final int errorCode, final String path) {
		DEBUG.ON();
		DEBUG.ERR(HeadLessErrors.getError(errorCode, path));
		DEBUG.OFF();

		return false;
	}

	/**
	 * Start.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param context
	 *            the context
	 * @return the object
	 * @throws Exception
	 *             the exception
	 * @date 17 oct. 2023
	 */
	@SuppressWarnings ({ "unchecked" })
	@Override
	public Object start(final IApplicationContext context) throws Exception {

		final Map<String, String[]> mm = context.getArguments();
		final List<String> args = Arrays.asList(mm.get("application.args"));

		// Check and apply parameters
		if (!checkParameters(args)) { System.exit(-1); }
		// ========================
		// No GAMA run
		// ========================
		boolean shouldExit = false;

		if (args.contains(GAMA_VERSION)) {
			showVersion();
			shouldExit = true;
		} else if (args.contains(HELP_PARAMETER)) {
			showHelp();
			shouldExit = true;
		}

		if (shouldExit) { System.exit(0); }

		// ========================
		// With GAMA run
		// ========================
		configureInjector();

		DEBUG.OFF();

		// Debug runner
		if (args.contains(VALIDATE_LIBRARY_PARAMETER)) {
			return ModelLibraryValidator.getInstance().start();
		}
		if (args.contains(TEST_LIBRARY_PARAMETER)) {
			return ModelLibraryTester.getInstance().start();
		}
		if (args.contains(RUN_LIBRARY_PARAMETER)) {
			return ModelLibraryRunner.getInstance().start();
		}
		if (args.contains(CHECK_MODEL_PARAMETER)) {
			ModelLibraryGenerator.start(this, args);
		} else if (args.contains(BATCH_PARAMETER)) {
			runBatchSimulation(args.get(args.size() - 2), args.get(args.size() - 1));
		} else if (args.contains(GAML_PARAMETER)) {
			runGamlSimulation(args);
		} else if (args.contains(BUILD_XML_PARAMETER)) {
			buildXML(args);
		} else if (args.contains(SOCKET_PARAMETER)) {
			GamaHeadlessWebSocketServer.startForHeadless(socket, processorQueue, ping);
		} else if (args.contains(SSOCKET_PARAMETER)) {
			final String jks = args.contains(SSOCKET_PARAMETER_JKSPATH) ? after(args, SSOCKET_PARAMETER_JKSPATH) : "";
			final String spwd = args.contains(SSOCKET_PARAMETER_SPWD) ? after(args, SSOCKET_PARAMETER_SPWD) : "";
			final String kpwd = args.contains(SSOCKET_PARAMETER_KPWD) ? after(args, SSOCKET_PARAMETER_KPWD) : "";
			GAMA.setServer(startForSecureHeadless(socket, processorQueue, true, jks, spwd, kpwd, ping));
		} else {
			runSimulation(args);
		}

		return null;
	}

	/**
	 * After.
	 *
	 * @param args
	 *            the args
	 * @param arg
	 *            the arg
	 * @return the string
	 */
	public String after(final List<String> args, final String arg) {
		if (args == null || args.size() < 2) {
			return null;
		}
		for (int i = 0; i < args.size() - 1; i++) { if (args.get(i).equals(arg)) {
			return args.get(i + 1);
		} }
		return null;
	}

	/**
	 * Builds the XML.
	 *
	 * @param arg
	 *            the arg
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws TransformerException
	 *             the transformer exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GamaHeadlessException
	 *             the gama headless exception
	 */
	public void buildXML(final List<String> arg)
			throws ParserConfigurationException, TransformerException, IOException, GamaHeadlessException {
		if (arg.size() < 3) {
			DEBUG.ON();
			DEBUG.ERR("Check your parameters!");
			showHelp();
			return;
		}

		// -xml [exp] [gaml] [xml]
		final String argExperimentName = arg.get(arg.size() - 3);
		final String argGamlFile = arg.get(arg.size() - 2);
		final String argXMLFile = arg.get(arg.size() - 1);

		final List<IExperimentJob> jb = ExperimentationPlanFactory.buildExperiment(argGamlFile);
		final ArrayList<IExperimentJob> selectedJob = new ArrayList<>();
		for (final IExperimentJob j : jb) {
			if (j.getExperimentName().equals(argExperimentName)) {
				selectedJob.add(j);
				break;
			}
		}

		if (selectedJob.size() == 0) {
			DEBUG.ON();
			DEBUG.ERR("""

					=== ERROR ===\

						GAMA is about to generate an empty XML file.\

						If you want to run a Batch experiment, please check the "-batch" flag.""");
			System.exit(-1);
		} else {
			final Document dd = ExperimentationPlanFactory.buildXmlDocument(selectedJob);
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(dd);
			final File output = new File(argXMLFile);
			final StreamResult result = new StreamResult(output);
			transformer.transform(source, result);
			DEBUG.OFF();
			DEBUG.LOG("Parameter file saved at: " + output.getAbsolutePath());
		}
	}

	/**
	 * Builds the XML for model library.
	 *
	 * @param modelPaths
	 *            the model paths
	 * @param outputPath
	 *            the output path
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws TransformerException
	 *             the transformer exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GamaHeadlessException
	 *             the gama headless exception
	 */
	public void buildXMLForModelLibrary(final ArrayList<File> modelPaths, final String outputPath)
			throws ParserConfigurationException, TransformerException, IOException, GamaHeadlessException {
		// "arg[]" are the paths to the different models
		final ArrayList<IExperimentJob> selectedJob = new ArrayList<>();
		for (final File modelFile : modelPaths) {
			final List<IExperimentJob> jb = ExperimentationPlanFactory.buildExperiment(modelFile.getAbsolutePath());
			selectedJob.addAll(jb);
		}

		final Document dd = ExperimentationPlanFactory.buildXmlDocumentForModelLibrary(selectedJob);
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();
		final DOMSource source = new DOMSource(dd);
		final File output = new File(outputPath);
		output.createNewFile();
		final StreamResult result = new StreamResult(output);
		transformer.transform(source, result);
		DEBUG.ON();
		DEBUG.LOG("Parameter file saved at: " + output.getAbsolutePath());
	}

	/**
	 * Run XML for model library.
	 *
	 * @param xmlPath
	 *            the xml path
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws InterruptedException
	 */
	public void runXMLForModelLibrary(final String xmlPath) throws FileNotFoundException, InterruptedException {
		runXML(new Reader(xmlPath));
	}

	/**
	 * Run simulation.
	 *
	 * @param args
	 *            the args
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public void runSimulation(final List<String> args) throws FileNotFoundException, InterruptedException {
		if (this.verbose && !this.tunnelingMode) { DEBUG.FORCE_ON(); }
		runXML(consoleMode ? new Reader(ConsoleReader.readOnConsole()) : new Reader(args.get(args.size() - 2)));
		System.exit(0);
	}

	/**
	 * Run XML.
	 *
	 * @param in
	 *            the in
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private void runXML(final Reader in) throws InterruptedException {
		in.parseXmlFile();
		this.buildAndRunSimulation(in.getSimulation());
		in.dispose();
		processorQueue.shutdown();
		while (!processorQueue.awaitTermination(100, TimeUnit.MILLISECONDS)) {}
	}

	/**
	 * Builds the and run simulation.
	 *
	 * @param sims
	 *            the sims
	 */
	public void buildAndRunSimulation(final Collection<ExperimentJob> sims) {
		for (ExperimentJob sim : sims) {
			try {
				XMLWriter ou = null;
				if (tunnelingMode) {
					ou = new XMLWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
				} else {
					ou = new XMLWriter(
							Globals.OUTPUT_PATH + "/" + Globals.OUTPUT_FILENAME + sim.getExperimentID() + ".xml");
				}
				sim.setBufferedWriter(ou);
				processorQueue.execute(sim);
			} catch (final Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	/**
	 * Stop.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 17 oct. 2023
	 */
	@Override
	public void stop() {}

	/*
	 * New runner implementations
	 */

	/**
	 * Auto launch batch experiment in headless mode from a gaml file
	 *
	 * @param experimentName
	 * @param pathToModel
	 */
	public void runBatchSimulation(final String experimentName, final String pathToModel) {
		assertIsAModelFile(pathToModel);
		final Injector injector = getInjector();
		final GamlModelBuilder builder = new GamlModelBuilder(injector);

		final List<GamlCompilationError> errors = new ArrayList<>();
		URI uri;
		try {
			uri = URI.createFileURI(pathToModel);
		} catch (Exception e) {
			uri = URI.createURI(pathToModel);
		}
		final IModel mdl = builder.compile(uri, errors);

		if (mdl == null) {
			DEBUG.LOG(
					"GAMA couldn't compile your input file. Please verify that the input file path is correct and ensure that there are no errors in the GAML model.");
			System.exit(1);
			return;
		}

		GamaExecutorService.CONCURRENCY_SIMULATIONS.set(true);
		GamaExecutorService.THREADS_NUMBER.set(processorQueue.getCorePoolSize());

		final IExperimentPlan expPlan = mdl.getExperiment(experimentName);
		assertIsExperiment(experimentName, expPlan);
		expPlan.setHeadless(true);
		expPlan.open();
		GAMA.getControllers().add(expPlan.getController());
		expPlan.getController().processStart(false);

		System.exit(0);
	}

	/**
	 * Auto launch gui experiment in headless mode from a gaml file
	 *
	 * @param experimentName
	 * @param pathToModel
	 * @throws InterruptedException
	 */
	public void runGamlSimulation(final List<String> args)
			throws IOException, GamaCompilationFailedException, InterruptedException {

		final String argExperimentName = args.get(args.size() - 3);
		final String argGamlFile = args.get(args.size() - 2);
		final String argOutDir = args.get(args.size() - 1);
		final Integer numberOfSteps =
				args.contains(STEPS_PARAMETER) ? Integer.parseInt(after(args, STEPS_PARAMETER)) : null;
		final Integer numberOfCores =
				args.contains(THREAD_PARAMETER) ? Integer.parseInt(after(args, THREAD_PARAMETER)) : null;

		assertIsAModelFile(argGamlFile);

		final List<IExperimentJob> jb =
				ExperimentationPlanFactory.buildExperiment(argGamlFile, numberOfSteps, numberOfCores);

		ExperimentJob selectedJob = null;
		for (final IExperimentJob j : jb) {
			if (j.getExperimentName().equals(argExperimentName)) {
				selectedJob = (ExperimentJob) j;
				break;
			}
		}
		if (selectedJob == null) {
			return;
		}
		Globals.OUTPUT_PATH = argOutDir;

		selectedJob.setBufferedWriter(new XMLWriter(Globals.OUTPUT_PATH + "/" + Globals.OUTPUT_FILENAME + ".xml"));
		processorQueue.setNumberOfThreads(numberOfCores != null ? numberOfCores : SimulationRuntime.DEFAULT_NB_THREADS);
		processorQueue.execute(selectedJob);
		processorQueue.shutdown();
		while (!processorQueue.awaitTermination(100, TimeUnit.MILLISECONDS)) {}
		System.exit(0);
	}

	/**
	 * Assert is A model file.
	 *
	 * @param pathToModel
	 *            the path to model
	 */
	private void assertIsAModelFile(final String pathToModel) {
		if (!GamlFileExtension.isGaml(pathToModel)) {
			DEBUG.LOG("The file " + pathToModel + " is not a GAML model or experiment file.");
			System.exit(-1);
		}
	}

	/**
	 * Assert is experiment.
	 *
	 * @param experimentName
	 *            the experiment name
	 * @param expPlan
	 *            the exp plan
	 */
	private void assertIsExperiment(final String experimentName, final IExperimentPlan expPlan) {
		if (expPlan == null) {
			DEBUG.LOG("Experiment " + experimentName + " does not exist. Verify its name.");
			System.exit(-1);
		}
	}

}
