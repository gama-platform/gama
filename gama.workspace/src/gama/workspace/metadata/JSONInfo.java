/*******************************************************************************************************
 *
 * JSONInfo.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.metadata;

import gama.core.util.file.GamaFileMetaData;
import gama.gaml.interfaces.IGamlDescription.Doc;
import gama.gaml.interfaces.IGamlDescription.RegularDoc;
import gama.gaml.operators.Strings;

/**
 * The Class JSONInfo.
 */
public class JSONInfo extends GamaFileMetaData {

	/** The item count. */
	private final int itemCount;

	/** The is geo json. */
	private final boolean isGeoJson;

	/** The type. */
	private final String type;

	/**
	 * Instantiates a new JSON info.
	 *
	 * @param modificationStamp
	 *            the modification stamp
	 * @param itemCount
	 *            the item count
	 * @param isGeoJson
	 *            the is geo json
	 * @param type
	 *            the type
	 */
	public JSONInfo(final long modificationStamp, final int itemCount, final boolean isGeoJson, final String type) {
		super(modificationStamp);
		this.itemCount = itemCount;
		this.isGeoJson = isGeoJson;
		this.type = type;
	}

	/**
	 * Instantiates a new JSON info.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public JSONInfo(final String propertyString) {
		super(propertyString);
		final String[] segments = split(propertyString);
		itemCount = Integer.parseInt(segments[1]);
		isGeoJson = Boolean.parseBoolean(segments[2]);
		type = segments.length > 3 ? segments[3] : "Unknown";
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		sb.append(type).append(" [").append(itemCount).append(isGeoJson ? " features" : " items").append("]");
		if (isGeoJson) {
			sb.append(SUFFIX_DEL).append("GeoJSON");
		} else {
			sb.append(SUFFIX_DEL).append("JSON");
		}
	}

	@Override
	public Doc getDocumentation() {
		final RegularDoc sb = new RegularDoc();
		sb.append(isGeoJson ? "GeoJSON File" : "JSON File").append(Strings.LN);
		sb.append("Type: ").append(type).append(Strings.LN);
		sb.append("Contains: ").append(itemCount + "").append(isGeoJson ? " features" : " items").append(Strings.LN);
		return sb;
	}

	@Override
	public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + itemCount + DELIMITER + isGeoJson + DELIMITER + type;
	}

	/**
	 * Gets the item count.
	 *
	 * @return the item count
	 */
	public int getItemCount() { return itemCount; }

	/**
	 * Checks if is geo json.
	 *
	 * @return true, if is geo json
	 */
	public boolean isGeoJson() { return isGeoJson; }

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() { return type; }

}
