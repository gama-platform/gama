package gama.core.common;

import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IStatusMessage;
import gama.core.runtime.exceptions.GamaRuntimeException;

public class ErrorStatusMessage implements IStatusMessage {

	private final Exception exception;

	public ErrorStatusMessage(final Exception e) {
		this.exception = e;
	}

	@Override
	public String getText() { return exception.getMessage(); }

	@Override
	public int getCode() { return IGui.ERROR; }

	@Override
	public String getIcon() { return "overlays/small.close"; }

	public GamaRuntimeException getException() {
		if (exception instanceof GamaRuntimeException ge) return ge;
		return null;
	}

	@Override
	public StatusMessageType getType() { return StatusMessageType.ERROR; }

}
