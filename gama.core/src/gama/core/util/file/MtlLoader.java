/*******************************************************************************************************
 *
 * MtlLoader.java, in gama.opengl, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
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
	public ArrayList Materials = new ArrayList<>();

	/**
	 * The Class mtl.
	 */
	public record mtl (
		
		/** The name. */
		String name,
		
		/** The mtlnum. */
		int mtlnum,
		
		/** The d. */
		float d,
		
		/** The Ka. */
		float[] Ka,
		
		/** The Kd. */
		float[] Kd,
		
		/** The Ks. */
		float[] Ks,
		
		/** The map kd. */
		String map_Kd,
		
		/** The map ka. */
		String map_Ka,
		
		/** The map d. */
		String map_d

	){
		public mtl{
			d = 1f;
			Ka = new float[3];
			Kd = new float[3];
			Ks = new float[3];
		}

	}

	/**
	 * Instantiates a new mtl loader.
	 *
	 * @param ref the ref
	 * @param pathtoimages the pathtoimages
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
	public int getSize() {
		return Materials.size();
	}

	/**
	 * Gets the d.
	 *
	 * @param namepass the namepass
	 * @return the d
	 */
	public float getd(final String namepass) {
		final float returnfloat = 1f;
		for (int i = 0; i < Materials.size(); i++) {
			final mtl tempmtl = (mtl) Materials.get(i);
			if (tempmtl.name.matches(namepass)) {
				// returnfloat = tempmtl.d;
				return tempmtl.d;
			}
		}
		return returnfloat;
	}

	/**
	 * Gets the kd.
	 *
	 * @param namepass the namepass
	 * @return the kd
	 */
	public float[] getKd(final String namepass) {
		final float[] returnfloat = new float[3];
		for (int i = 0; i < Materials.size(); i++) {
			final mtl tempmtl = (mtl) Materials.get(i);
			if (tempmtl.name.matches(namepass)) {
				// returnfloat = tempmtl.Kd;
				return tempmtl.Kd;
			}
		}
		return returnfloat;
	}

	/**
	 * Gets the map ka.
	 *
	 * @param namepass the namepass
	 * @return the map ka
	 */
	public String getMapKa(final String namepass) {
		for (int i = 0; i < Materials.size(); i++) {
			final mtl tempmtl = (mtl) Materials.get(i);
			if (tempmtl.name.matches(namepass)) { return tempmtl.map_Ka; }
		}
		return null;
	}

	/**
	 * Gets the map kd.
	 *
	 * @param namepass the namepass
	 * @return the map kd
	 */
	public String getMapKd(final String namepass) {
		for (int i = 0; i < Materials.size(); i++) {
			final mtl tempmtl = (mtl) Materials.get(i);
			if (tempmtl.name.matches(namepass)) { return tempmtl.map_Kd; }
		}
		return null;
	}

	/**
	 * Gets the mapd.
	 *
	 * @param namepass the namepass
	 * @return the mapd
	 */
	public String getMapd(final String namepass) {
		for (int i = 0; i < Materials.size(); i++) {
			final mtl tempmtl = (mtl) Materials.get(i);
			if (tempmtl.name.matches(namepass)) { return tempmtl.map_d; }
		}
		return null;
	}

	/**
	 * Loadobject.
	 *
	 * @param br the br
	 * @param pathtoimages the pathtoimages
	 */
	private void loadobject(final BufferedReader br, final String pathtoimages) {
		int linecounter = 0;
		try {

			String newline;
			boolean firstpass = true;
			
			// variables for the mtl records
			String name = null;
			int mtlCounter = 0;
			int mtlnum = 0;
			float[] Ka = new float[3];
			float[] Kd = new float[3]; 
			float[] Ks = new float[3];
			float d = 1f;
			String map_Ka = null; 
			String map_Kd = null;
			String map_d  = null;

			while ((newline = br.readLine()) != null) {
				linecounter++;
				newline = newline.trim();
				if (newline.length() > 0) {
					if (newline.charAt(0) == 'n' && newline.charAt(1) == 'e' && newline.charAt(2) == 'w') {
						if (firstpass) {
							firstpass = false;
						} else {
							Materials.add(new mtl(name, mtlnum, d, Ka, Kd, Ks, map_Ka, map_Kd, map_d));
							
							//reinit record variables
							name = null;
							Ka = new float[3];
							Kd = new float[3]; 
							Ks = new float[3];
							d = 1f;
							map_Ka = null; 
							map_Kd = null;
							map_d = null;
						}
						String[] coordstext = new String[2];
						coordstext = newline.split("\\s+");
						name = coordstext[1];
						mtlnum = mtlCounter;
						mtlCounter++;
					} else if (newline.charAt(0) == 'K' && newline.charAt(1) == 'a') {
						final float[] coords = new float[3];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							coords[i - 1] = Float.valueOf(coordstext[i]).floatValue();
						}
						Ka = coords;
					} else if (newline.charAt(0) == 'K' && newline.charAt(1) == 'd') {
						final float[] coords = new float[3];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							coords[i - 1] = Float.valueOf(coordstext[i]).floatValue();
						}
						Kd = coords;
					} else if (newline.charAt(0) == 'K' && newline.charAt(1) == 's') {
						final float[] coords = new float[3];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1; i < coordstext.length; i++) {
							coords[i - 1] = Float.valueOf(coordstext[i]).floatValue();
						}
						Ks = coords;
					} else if (newline.charAt(0) == 'd') {
						final String[] coordstext = newline.split("\\s+");
						d = Float.valueOf(coordstext[1]).floatValue();
					} else if (newline.contains("map_Ka")) {
						String texture = newline.replace("map_Ka ", "");
						while (texture.startsWith(" ")) {
							texture = texture.replaceFirst(" ", "");
						}
						map_Ka = texture;
					} else if (newline.contains("map_Kd")) {
						String texture = newline.replace("map_Kd ", "");
						while (texture.startsWith(" ")) {
							texture = texture.replaceFirst(" ", "");
						}
						map_Kd = texture;
					} else if (newline.contains("map_d")) {
						String texture = newline.replace("map_d ", "");
						while (texture.startsWith(" ")) {
							texture = texture.replaceFirst(" ", "");
						}
						map_d = texture;
					}
				}
			}
			Materials.add(new mtl(name, mtlnum, d, Ka, Kd, Ks, map_Ka, map_Kd, map_d));

		} catch (final IOException e) {
			DEBUG.ERR("Failed to read file: " + br.toString());
			e.printStackTrace();
		} catch (final NumberFormatException e) {
			DEBUG.ERR("Malformed MTL (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
		} catch (final StringIndexOutOfBoundsException e) {
			DEBUG.ERR("Malformed MTL (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
		}
	}
}
