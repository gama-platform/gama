/*******************************************************************************************************
 *
 * RecordingHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson;

import java.io.IOException;
import java.util.LinkedList;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

/**
 * Handler that records sequence of calls to be replayed layer.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class RecordingHandler implements ContentHandler {

	/** The actions. */
	LinkedList<Action<?>> actions = new LinkedList<>();

	@Override
	public void startJSON() throws ParseException, IOException {
		actions.add(new Action<>() {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.startJSON();
			}
		});
	}

	@Override
	public boolean startObject() throws ParseException, IOException {
		return actions.add(new Action<>() {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.startObject();
			}
		});
	}

	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		return actions.add(new Action<>(key) {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.startObjectEntry(obj);
			}
		});
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		return actions.add(new Action<>() {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.startArray();
			}
		});
	}

	@Override
	public boolean primitive(final Object obj) throws ParseException, IOException {
		return actions.add(new Action<>(obj) {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.primitive(obj);
			}
		});
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		return actions.add(new Action<>() {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.endArray();
			}
		});
	}

	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		return actions.add(new Action<>() {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.endObjectEntry();
			}
		});
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		return actions.add(new Action<>() {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.endObject();
			}
		});
	}

	@Override
	public void endJSON() throws ParseException, IOException {
		actions.add(new Action<>() {
			@Override
			protected void run(final ContentHandler handler) throws ParseException, IOException {
				handler.endJSON();
			}
		});
	}

	/**
	 * Replay.
	 *
	 * @param handler
	 *            the handler
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void replay(final ContentHandler handler) throws ParseException, IOException {
		while (!actions.isEmpty()) { actions.removeFirst().run(handler); }
	}

	/**
	 * The Class Action.
	 *
	 * @param <T>
	 *            the generic type
	 */
	abstract class Action<T> {

		/** The obj. */
		protected T obj;

		/**
		 * Instantiates a new action.
		 */
		Action() {
			this(null);
		}

		/**
		 * Instantiates a new action.
		 *
		 * @param obj
		 *            the obj
		 */
		Action(final T obj) {
			this.obj = obj;
		}

		/**
		 * Run.
		 *
		 * @param handler
		 *            the handler
		 * @throws ParseException
		 *             the parse exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		protected abstract void run(ContentHandler handler) throws ParseException, IOException;
	}
}
