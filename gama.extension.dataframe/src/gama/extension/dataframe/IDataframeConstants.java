/**
 *
 */
package gama.extension.dataframe;

import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;

/**
 *
 */
public interface IDataframeConstants {

	/** The keyword. */
	String NAME = "dataframe";

	/** The instance. */
	GamaDataFrameType TYPE = (GamaDataFrameType) Types.get(NAME);

	/** The type ID for dataframe (tabular data with named columns) */
	int ID = IType.BEGINNING_OF_CUSTOM_TYPES + 35;

	/** The Constant ID. */
	String CATEGORY = "Dataframe-related operators";

	/** The Constant CONCEPT. */
	String CONCEPT = NAME;

}
