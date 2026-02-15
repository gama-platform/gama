/*******************************************************************************************************
 *
 * IStatusMessage.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import org.eclipse.core.runtime.QualifiedName;

import gama.api.data.objects.IColor;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.utils.color.GamaColorFactory;

/**
 *
 */
public interface IStatusMessage {

	/** The wait and task color. */
	IColor WAIT_AND_TASK_COLOR = GamaColorFactory.get(207, 119, 56);

	/** The inform color. */
	IColor INFORM_COLOR = GamaColorFactory.get(102, 114, 126);

	/** The error color. */
	IColor ERROR_COLOR = GamaColorFactory.get(158, 77, 77);

	/** The neutral color. */
	IColor NEUTRAL_COLOR = GamaColorFactory.get(102, 114, 126);

	/** The progress icon. */
	String PROGRESS_ICON = "status/status.progress";

	/** The simulation icon. */
	String SIMULATION_ICON = "status/status.simulation";

	/** The system icon. */
	String SYSTEM_ICON = "status/status.system";

	/** The error icon. */
	String ERROR_ICON = "status/status.error";

	/** The compile icon. */
	String COMPILE_ICON = "status/status.compile";

	/** The compile icon. */
	String VIEW_ICON = "status/status.view";

	/** The compile icon. */
	String MEMORY_ICON = "status/status.memory";

	/** The download icon. */
	String DOWNLOAD_ICON = "status/status.download";

	/** The idle icon. */
	String IDLE_ICON = "overlays/status.clock";

	/** The job key. */
	QualifiedName JOB_KEY = new QualifiedName("status", "property");

	/** The internal job. */
	String INTERNAL_STATUS_REFRESH_JOB = "internal";

	/** The view job. */
	String VIEW_JOB = "view";

	/**
	 * The Enum StatusType.
	 */
	public enum StatusType {

		/** The error. */
		ERROR,
		/** The inform. */
		REGULAR,
		/** The experiment. */
		EXPERIMENT,
		/** The none. */
		NONE;
	}

	/**
	 * @return
	 */
	default GamaRuntimeException exception() {
		return null;
	}

	/**
	 * @return
	 */
	default StatusType type() {
		return StatusType.NONE;
	}

	/**
	 * Checks if is error.
	 *
	 * @return true, if is error
	 */
	default boolean isError() { return false; }

	/**
	 * @return
	 */
	String message();

	/**
	 * @return
	 */
	default Double completion() {
		return null;
	}

	/**
	 * Color.
	 *
	 * @return the gama color
	 */
	default IColor color() {
		return null;
	}

	/**
	 * Icon.
	 *
	 * @return the string
	 */
	default String icon() {
		return PROGRESS_ICON;
	}

	/**
	 * @return
	 */
	default long timeStamp() {
		return System.currentTimeMillis();
	}

}
