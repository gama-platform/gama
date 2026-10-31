/*******************************************************************************************************
 *
 * ServerConsoleView.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.views.console;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolItem;

import gama.api.GAMA;
import gama.api.ui.ConsoleEvent;
import gama.api.ui.IConsoleSink;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.server.GamaServerConsole;
import gama.api.utils.server.GamaServerConsole.State;
import gama.ui.application.server.GamaGuiWebSocketServer;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaToolbar2;

/** A local control panel and console for the desktop WebSocket server, independent of experiments. */
public class ServerConsoleView extends ConsoleView implements IConsoleSink {

	private static final DateTimeFormatter TIME =
			DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

	private final GamaServerConsole serverConsole = GamaGuiWebSocketServer.getGuiServerConsole();
	private final AtomicBoolean updatePending = new AtomicBoolean();
	private volatile boolean disposed;
	private boolean busy;
	private boolean showRequests = true;
	private long lastSequence;
	private int configuredPort;
	private Spinner port;
	private Button applyPort, clients;
	private ToolItem start, stop, restart;

	public ServerConsoleView() {
		super("GAMA Server Console");
	}

	@Override
	public void createToolItems(final GamaToolbar2 tb) {
		start = tb.button("toolbar/server.start", null, "Start the server",
				e -> control(GamaGuiWebSocketServer::startServer), SWT.RIGHT);
		stop = tb.button("toolbar/server.stop", null, "Stop the server and disconnect its clients",
				e -> control(GamaGuiWebSocketServer::stopServer), SWT.RIGHT);
		restart = tb.button("toolbar/server.restart", null, "Restart the server and disconnect its clients",
				e -> control(GamaGuiWebSocketServer::restartServer), SWT.RIGHT);
		super.createToolItems(tb);
	}

	@Override
	public void ownCreatePartControl(final Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		final Composite settings = new Composite(parent, SWT.NONE);
		settings.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		settings.setLayout(new GridLayout(4, false));
		new Label(settings, SWT.NONE).setText("Port");
		port = new Spinner(settings, SWT.BORDER);
		port.setMinimum(1);
		port.setMaximum(65535);
		port.setSelection(GamaPreferences.Runtime.CORE_SERVER_PORT.getValue());
		applyPort = new Button(settings, SWT.PUSH);
		applyPort.setText("Apply");
		applyPort.setToolTipText("Save this port and restart the server if it is active");
		applyPort.addListener(SWT.Selection, e -> {
			GamaPreferences.setNewPreferences(Map.of(GamaPreferences.Runtime.CORE_SERVER_PORT.getKey(),
					port.getSelection()));
			requestUpdate();
		});
		clients = new Button(settings, SWT.PUSH);
		clients.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		clients.addListener(SWT.Selection, e -> showClients());

		final Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contents.setLayout(new org.eclipse.swt.layout.FillLayout());
		super.ownCreatePartControl(contents);
		serverConsole.addConsoleSink(this);
		refresh();
	}

	@Override
	public void accept(final ConsoleEvent event) {
		requestUpdate();
	}

	private void requestUpdate() {
		if (disposed || !updatePending.compareAndSet(false, true)) return;
		WorkbenchHelper.asyncRun(() -> {
			if (!disposed && port != null && !port.isDisposed()) {
				port.getDisplay().timerExec(100, this::refresh);
			}
		});
	}

	private void refresh() {
		updatePending.set(false);
		if (disposed || port == null || port.isDisposed()) return;
		final GamaServerConsole.Snapshot snapshot = serverConsole.snapshot();
		final boolean active = snapshot.state() == State.RUNNING || snapshot.state() == State.STARTING;
		final boolean transitioning = busy || snapshot.state() == State.STARTING || snapshot.state() == State.STOPPING;
		clients.setText("Clients (" + snapshot.clients().size() + ")...");
		clients.setEnabled(!snapshot.clients().isEmpty());
		clients.setToolTipText(clientDetails(snapshot));
		start.setEnabled(!transitioning && !active);
		stop.setEnabled(!transitioning && GAMA.getServer() instanceof GamaGuiWebSocketServer);
		restart.setEnabled(!transitioning && active);
		applyPort.setEnabled(!transitioning);
		port.setEnabled(!transitioning);
		final int preferencePort = GamaPreferences.Runtime.CORE_SERVER_PORT.getValue();
		if (configuredPort != preferencePort) {
			configuredPort = preferencePort;
			port.setSelection(preferencePort);
		}
		if (!snapshot.entries().isEmpty() && lastSequence < snapshot.entries().getFirst().sequence() - 1) {
			append("(... older server messages discarded)\n", null, IGamaColors.GRAY);
		}
		for (final GamaServerConsole.Entry entry : snapshot.entries()) {
			if (entry.sequence() <= lastSequence) continue;
			final ConsoleEvent event = entry.event();
			if (showRequests || event.level() != ConsoleEvent.Level.DEBUG) {
				append("[" + TIME.format(event.timestamp()) + "] [" + event.level() + "] [" + event.source()
						+ "] " + event.message() + "\n", null, switch (event.level()) {
							case ERROR -> IGamaColors.ERROR;
							case WARNING -> IGamaColors.WARNING;
							case DEBUG -> IGamaColors.BLUE;
							case INFO -> IGamaColors.BLACK;
						});
			}
		}
		lastSequence = snapshot.sequence();
		port.getParent().layout();
	}

	private void control(final Supplier<CompletableFuture<Void>> command) {
		busy = true;
		refresh();
		command.get().whenComplete((result, error) -> WorkbenchHelper.asyncRun(() -> {
			if (disposed) return;
			busy = false;
			refresh();
		}));
	}

	@Override
	protected void fillAdditionalMenuItems(final Menu menu) {
		GamaMenu.separate(menu);
		GamaMenu.separate(menu, "Server options");
		final GamaServerConsole.Snapshot snapshot = serverConsole.snapshot();
		final boolean transitioning = busy || snapshot.state() == State.STARTING || snapshot.state() == State.STOPPING;
		final MenuItem startup = new MenuItem(menu, SWT.CHECK);
		startup.setText("Enable at startup");
		startup.setSelection(GamaPreferences.Runtime.CORE_SERVER_MODE.getValue());
		startup.setEnabled(!transitioning);
		startup.addListener(SWT.Selection, e -> {
			GamaPreferences.setNewPreferences(Map.of(GamaPreferences.Runtime.CORE_SERVER_MODE.getKey(),
					startup.getSelection()));
			requestUpdate();
		});
		final MenuItem requests = new MenuItem(menu, SWT.CHECK);
		requests.setText("Show client requests");
		requests.setSelection(showRequests);
		requests.addListener(SWT.Selection, e -> {
			showRequests = requests.getSelection();
			super.reset();
			lastSequence = 0;
			refresh();
		});
		final MenuItem logging = new MenuItem(menu, SWT.CHECK);
		logging.setText("Log all exchanges to file");
		logging.setSelection(snapshot.state() == State.RUNNING ? snapshot.exchangeLogging()
				: GamaPreferences.Runtime.CORE_SERVER_LOG.getValue());
		logging.setEnabled(!transitioning);
		logging.addListener(SWT.Selection, e -> {
			final boolean enabled = logging.getSelection();
			control(() -> GamaGuiWebSocketServer.setServerLogging(enabled).thenRun(() ->
					WorkbenchHelper.asyncRun(() -> GamaPreferences.setNewPreferences(
							Map.of(GamaPreferences.Runtime.CORE_SERVER_LOG.getKey(), enabled)))));
		});
		final Path file = snapshot.logFile();
		final MenuItem openLog = new MenuItem(menu, SWT.PUSH);
		openLog.setText("Open exchange log");
		openLog.setEnabled(file != null);
		openLog.addListener(SWT.Selection, e -> {
			if (!Program.launch(file.toString())) {
				GAMA.getGui().getDialogFactory().error("Unable to open server exchange log: " + file);
			}
		});
	}

	private void showClients() {
		MessageDialog.openInformation(getSite().getShell(), "Connected server clients",
				clientDetails(serverConsole.snapshot()));
	}

	private static String clientDetails(final GamaServerConsole.Snapshot snapshot) {
		return snapshot.clients().isEmpty() ? "No connected clients"
				: snapshot.clients().entrySet().stream()
						.map(client -> client.getKey() + " : " + client.getValue()).collect(Collectors.joining("\n"));
	}

	@Override
	public synchronized void reset() {
		super.reset();
		lastSequence = serverConsole.snapshot().sequence();
	}

	@Override
	public void widgetDisposed(final DisposeEvent event) {
		disposed = true;
		serverConsole.removeConsoleSink(this);
		super.widgetDisposed(event);
	}
}
