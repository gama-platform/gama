/*******************************************************************************************************
 *
 * MtlLoader.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import gama.dev.DEBUG;

/**
 * The Class MtlLoader.
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class MtlLoader {

	/** The Materials. */
	protected ArrayList materials = new ArrayList<>();

	/**
	 * The Class Mtl.
	 */
	public static class Mtl {

		/** The name. */
		public String name;

		/** The mtlNum. */
		public int mtlNum;

		/** The d. */
		public float d = 1f;

		/** The Ka. */
		public float[] Ka = new float[3];

		/** The Kd. */
		public float[] Kd = new float[3];

		/** The Ks. */
		public float[] Ks = new float[3];

		/** The map kd. */
		public String map_Kd;

		/** The map ka. */
		public String map_Ka;

		/** The map d. */
		public String map_d;

	}

	/**
	 * Instantiates a new mtl loader.
	 *
	 * @param ref
	 *            the ref
	 * @param pathtoimages
	 *            the pathtoimages
	 */
	public MtlLoader(final BufferedReader ref, final String pathtoimages) {
		loadobject(ref, pathtoimages);
		cleanup();
	}

	/**
	 * Cleanup.
	 */
	private void cleanup() {}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public int getSize() { return materials.size(); }

	/**
	 * Gets the d.
	 *
	 * @param namepass
	 *            the namepass
	 * @return the d
	 */
	public float getd(final String namepass) {
		final float returnfloat = 1f;
		for (Object material : materials) {
			final Mtl tempmtl = (Mtl) material;
			if (tempmtl.name.matches(namepass)) // returnfloat = tempmtl.d;
				return tempmtl.d;
		}
		return returnfloat;
	}

	/**
	 * Gets the kd.
	 *
	 * @param namepass
	 *            the namepass
	 * @return the kd
	 */
	public float[] getKd(final String namepass) {
		final float[] returnfloat = new float[3];
		for (Object material : materials) {
			final Mtl tempmtl = (Mtl) material;
			if (tempmtl.name.matches(namepass)) // returnfloat = tempmtl.Kd;
				return tempmtl.Kd;
		}
		return returnfloat;
	}

	/**
	 * Gets the map ka.
	 *
	 * @param namepass
	 *            the namepass
	 * @return the map ka
	 */
	public String getMapKa(final String namepass) {
		for (Object material : materials) {
			final Mtl tempmtl = (Mtl) material;
			if (tempmtl.name.matches(namepass)) return tempmtl.map_Ka;
		}
		return null;
	}

	/**
	 * Gets the map kd.
	 *
	 * @param namepass
	 *            the namepass
	 * @return the map kd
	 */
	public String getMapKd(final String namepass) {
		for (Object material : materials) {
			final Mtl tempmtl = (Mtl) material;
			if (tempmtl.name.matches(namepass)) return tempmtl.map_Kd;
		}
		return null;
	}

	/**
	 * Gets the mapd.
	 *
	 * @param namepass
	 *            the namepass
	 * @return the mapd
	 */
	public String getMapd(final String namepass) {
		for (Object material : materials) {
			final Mtl tempmtl = (Mtl) material;
			if (tempmtl.name.matches(namepass)) return tempmtl.map_d;
		}
		return null;
	}

	/**
	 * Loadobject.
	 *
	 * @param br
	 *            the br
	 * @param pathtoimages
	 *            the pathtoimages
	 */
	private void loadobject(final BufferedReader br, final String pathtoimages) {
		int linecounter = 0;
		try {

			String newline;
			boolean firstpass = true;
			Mtl matset = new Mtl();
			int mtlcounter = 0;

			while ((newline = br.readLine()) != null) {
				linecounter++;
				newline = newline.trim();
				if (newline.length() > 0) {
					if (newline.charAt(0) == 'n' && newline.charAt(1) == 'e' && newline.charAt(2) == 'w') {
						if (firstpass) {
							firstpass = false;
						} else {
							materials.add(matset);
							matset = new Mtl();
						}
						final String[] coordstext = newline.split("\\s+");
						matset.name = coordstext[1];
						matset.mtlNum = mtlcounter;
						mtlcounter++;
					} else if (newline.charAt(0) == 'K' && newline.charAt(1) == 'a') {
						final String[] coordstext = newline.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							matset.Ka[i - 1] = Float.parseFloat(coordstext[i]);
						}
					} else if (newline.charAt(0) == 'K' && newline.charAt(1) == 'd') {
						final String[] coordstext = newline.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							matset.Kd[i - 1] = Float.parseFloat(coordstext[i]);
						}
					} else if (newline.charAt(0) == 'K' && newline.charAt(1) == 's') {
						final String[] coordstext = newline.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							matset.Ks[i - 1] = Float.parseFloat(coordstext[i]);
						}
					} else if (newline.charAt(0) == 'd') {
						final String[] coordstext = newline.split("\\s+");
						matset.d = Float.parseFloat(coordstext[1]);
					} else if (newline.contains("map_Ka")) {
						String texture = newline.replace("map_Ka ", "");
						while (texture.startsWith(" ")) { texture = texture.replaceFirst(" ", ""); }
						matset.map_Ka = texture;
					} else if (newline.contains("map_Kd")) {
						String texture = newline.replace("map_Kd ", "");
						while (texture.startsWith(" ")) { texture = texture.replaceFirst(" ", ""); }
						matset.map_Kd = texture;
					} else if (newline.contains("map_d")) {
						String texture = newline.replace("map_d ", "");
						while (texture.startsWith(" ")) { texture = texture.replaceFirst(" ", ""); }
						matset.map_d = texture;
					}
				}
			}
			materials.add(matset);

		} catch (final IOException e) {
			DEBUG.ERR("Failed to read file: " + br.toString());
			e.printStackTrace();
		} catch (final NumberFormatException | StringIndexOutOfBoundsException e) {
			DEBUG.ERR("Malformed MTL (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
		}
	}
}
