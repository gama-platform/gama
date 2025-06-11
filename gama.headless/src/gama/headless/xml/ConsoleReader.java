/*******************************************************************************************************
 *
 * ConsoleReader.java, in gama.headless, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.headless.xml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import gama.dev.DEBUG;

/**
 * The Class ConsoleReader.
 */
public abstract class ConsoleReader {

	static {
		DEBUG.ON();
	}
	
	/** The end of file. */
	public static final String END_OF_FILE = "</Experiment_plan>";

	/**
	 * Read on console.
	 *
	 * @return the input stream
	 */
	public static InputStream readOnConsole() {
		
		StringBuilder str = new StringBuilder();
		String tmp = "";
		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		final String pp = new File(".").getAbsolutePath();
		DEBUG.OUT("************************** CURRENT PATH **********************************\n"
				+ pp.substring(0, pp.length() - 1)
				+ "\n************************************************************\n");

		do {
			try {
				tmp = br.readLine();
				str.append(tmp);
			} catch (final IOException e) {
				
				e.printStackTrace();
			}
		} while (tmp != null && !tmp.contains(END_OF_FILE));

		return new ByteArrayInputStream(str.toString().getBytes());

	}

}
