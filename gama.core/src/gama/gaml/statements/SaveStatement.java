/*******************************************************************************************************
 *
 * SaveStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.delegates.ISaveDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.FlushBufferException;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaFileType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.IGamaFile;
import gama.api.types.misc.IContainer;
import gama.api.utils.files.BufferingUtils;
import gama.api.utils.files.BufferingUtils.BufferingStrategies;
import gama.api.utils.files.FileUtils;
import gama.api.utils.files.SaveOptions;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.gaml.statements.SaveStatement.SaveValidator;

/**
 * The Class SaveStatement.
 */

/**
 * The Class SaveStatement.
 */
@symbol (
		name = IKeyword.SAVE,
		kind = ISymbolKind.SINGLE_STATEMENT,
		concept = { IConcept.FILE, IConcept.SAVE_FILE },
		with_sequence = true, // necessary to allow declaring the attributes facet as remote itself
		// with_args = true,
		remote_context = true)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.ACTION })
@facets (
		value = { @facet (
				name = IKeyword.FORMAT,
				type = { IType.STRING },
				optional = true,
				doc = @doc (
						value = "a string representing the format of the output file (e.g. \"shp\", \"asc\", \"geotiff\", \"png\", \"text\", \"csv\"). If the file extension is non ambiguous in facet 'to:', this format does not need to be specified. However, in many cases, it can be useful to do it (for instance, when saving a string to a .pgw file, it is always better to clearly indicate that the expected format is 'text'). ")),
				@facet (
						name = IKeyword.DATA,
						type = IType.NONE,
						optional = true,
						doc = @doc ("the data that will be saved to the file or the file itself to save when data is used in its simplest form")),
				@facet (
						name = IKeyword.REWRITE,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("a boolean expression specifying whether to erase the file if it exists or append data at the end of it. Only applicable to \"text\" or \"csv\" files. Default is true")),
				@facet (
						name = IKeyword.HEADER,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("an expression that evaluates to a boolean, specifying whether the save will write a header if the file does not exist")),
				@facet (
						name = IKeyword.TO,
						type = IType.STRING,
						optional = true,
						doc = @doc ("an expression that evaluates to an string, the path to the file, or directly to a file")),
				@facet (
						name = "crs",
						type = IType.NONE,
						optional = true,
						doc = @doc ("the name of the projection, e.g. crs:\"EPSG:4326\" or its EPSG id, e.g. crs:4326. Here a list of the CRS codes (and EPSG id): http://spatialreference.org")),
				@facet (
						name = IKeyword.ATTRIBUTES,
						type = { IType.MAP, IType.LIST },
						remote_context = true,
						optional = true,
						doc = @doc (
								value = "Allows to specify the attributes of a shape file or GeoJson file where agents are saved. Can be expressed as a list of string or as a literal map. When expressed as a list, each value should represent the name of an attribute of the shape or agent. The keys of the map are the names of the attributes that will be present in the file, the values are whatever expressions neeeded to define their value. ")),
				@facet (
						name = IKeyword.BUFFERING,
						type = { IType.STRING },
						optional = true,
						doc = @doc (
								value = "Allows to specify a buffering strategy to write the file. Accepted values are `"
										+ BufferingUtils.PER_CYCLE_BUFFERING + "` and `"
										+ BufferingUtils.PER_SIMULATION_BUFFERING + "`, `" + BufferingUtils.NO_BUFFERING
										+ "`. " + "In the case of `" + BufferingUtils.PER_CYCLE_BUFFERING + "` or `"
										+ BufferingUtils.PER_SIMULATION_BUFFERING
										+ "`, all the write operations in the simulation which used these values would be "
										+ "executed all at once at the end of the cycle or simulation while keeping the initial order. In case of '"
										+ BufferingUtils.PER_AGENT
										+ "' all operations will be released when the agent is killed (or the simulation ends). Those strategies can be used to optimise a "
										+ "simulation's execution time on models that extensively write in files. "
										+ "The `" + BufferingUtils.NO_BUFFERING
										+ "` (which is the system's default) will directly write into the file.")), },
		omissible = IKeyword.DATA)
@doc (
		value = "Allows to save data in a file.",
		usages = { @usage (
				value = "Its simple syntax is:",
				examples = { @example (
						value = "save data to: output_file format: a_file_format;",
						isExecutable = false) }),
				@usage (
						value = "To save data in a text file:",
						examples = { @example (
								value = "save (string(cycle) + \"->\"  + name + \":\" + location) to: \"save_data.txt\" format: \"text\";") }),
				@usage (
						value = "To save the values of some attributes of the current agent in csv file:",
						examples = { @example (
								value = "save [name, location, host] to: \"save_data.csv\" format: \"csv\";") }),
				@usage (
						value = "To save the values of all attributes of all the agents of a species into a csv (with optional attributes):",
						examples = { @example (
								value = "save species_of(self) to: \"save_csvfile.csv\" format: \"csv\" header: false;") }),
				@usage (
						value = "To save the geometries of all the agents of a species into a shapefile (with optional attributes):",
						examples = { @example (
								value = "save species_of(self) to: \"save_shapefile.shp\" format: \"shp\" attributes: ['nameAgent'::name, 'locationAgent'::location] crs: \"EPSG:4326\";") }),
				@usage (
						value = "To save the grid_value attributes of all the cells of a grid into an ESRI ASCII Raster file:",
						examples = { @example (
								value = "save grid to: \"save_grid.asc\" format: \"asc\";") }),
				@usage (
						value = "To save the grid_value attributes of all the cells of a grid into geotiff:",
						examples = { @example (
								value = "save grid to: \"save_grid.tif\" format: \"geotiff\";") }),
				@usage (
						value = "To save the grid_value attributes of all the cells of a grid into png (with a worldfile):",
						examples = { @example (
								value = "save grid to: \"save_grid.png\" format: \"image\";") }),
				@usage (
						value = "The save statement can be use in an init block, a reflex, an action or in a user command. Do not use it in experiments.") })
@validator (SaveValidator.class)
@SuppressWarnings ({ "rawtypes" })
public class SaveStatement extends AbstractStatementSequence {

	/** The Constant NON_SAVEABLE_ATTRIBUTE_NAMES. */
	public static final Set<String> NON_SAVEABLE_ATTRIBUTE_NAMES =
			Set.of(IKeyword.PEERS, IKeyword.LOCATION, IKeyword.HOST, IKeyword.AGENTS, IKeyword.MEMBERS, IKeyword.SHAPE);

	/** The Constant EPSG_LABEL. */
	private static final String EPSG_LABEL = "EPSG:";

	/**
	 * @param createExecutableExtension
	 */
	/**
	 * The Class SaveValidator.
	 */
	public static class SaveValidator implements IDescriptionValidator<IStatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IStatementDescription desc) {

			final IExpression att = desc.getFacetExpr(ATTRIBUTES);
			final IExpressionDescription type = desc.getFacet(FORMAT);
			final IExpression bufferingStrategy = desc.getFacetExpr(IKeyword.BUFFERING);

			if (type != null) { desc.setFacetExprDescription(FORMAT, type); }
			final IExpression format = type == null ? null : type.getExpression();

			final IExpression data = desc.getFacetExpr(DATA);
			if (data == null) return;
			final IType<?> dataType = data.getGamlType();
			final IExpression to = desc.getFacetExpr(TO);

			boolean isAFile = Types.FILE.isAssignableFrom(dataType);
			String ext = null;
			if (to != null && to.isConst()) { ext = com.google.common.io.Files.getFileExtension(to.literalValue()); }
			if (isAFile && to != null) {
				desc.warning("The destination will not be taking into account when saving an already existing file",
						IGamlIssue.UNMATCHED_OPERANDS);
			}
			if (isAFile && format != null) {
				desc.warning("The file format will not be taken into account when saving an already existing file ",
						IGamlIssue.CONFLICTING_FACETS, FORMAT);
			}

			if (!isAFile && to == null) {
				desc.error("No file specified", IGamlIssue.MISSING_FACET);
				return;
			}

			if (!isAFile && format == null && to != null && ext != null
					&& !GamaAdditionRegistry.containsSaveDelegate(ext)) {
				if (dataType != Types.STRING && dataType != Types.INT && dataType != Types.FLOAT) {
					desc.error("Unknown file extension. Accepted formats are: "
							+ GamaAdditionRegistry.getSaveFileFormats(), IGamlIssue.UNKNOWN_ARGUMENT, TO);
					return;
				}
				desc.warning("Unknown file format, will default to 'text'. Accepted formats are: "
						+ GamaAdditionRegistry.getSaveFileFormats(), IGamlIssue.UNKNOWN_ARGUMENT, TO);
			}

			if (!isAFile && format == null && to != null) {
				desc.info(
						"'save' will use the extension of the file to determine its format. If you are unsure about this, please specify the format of the file using the 'format:' facet",
						IGamlIssue.UNKNOWN_ARGUMENT);
			}

			if (!isAFile && format != null && to != null) {
				String id = format.literalValue();
				// maybe it can represent a string ?
				if (!GamaAdditionRegistry.containsSaveDelegate(id) && format.getGamlType() != Types.STRING) {
					desc.error(
							"Unknown file format. Accepted formats are: " + GamaAdditionRegistry.getSaveFileFormats(),
							IGamlIssue.UNKNOWN_ARGUMENT, FORMAT);
					return;
				}
				if (ext != null && !id.equals(ext) && !GamaAdditionRegistry.areFormatSynonyms(ext, id)) {
					desc.info("The extension of the file and the format differ. Make sure they are compatible",
							IGamlIssue.CONFLICTING_FACETS);
				}

			}

			if (bufferingStrategy != null
					&& !BufferingUtils.BUFFERING_STRATEGIES.contains(bufferingStrategy.literalValue())) {
				desc.error("The value for buffering must be '" + BufferingUtils.NO_BUFFERING + "', '"
						+ BufferingUtils.PER_CYCLE_BUFFERING + "', '" + BufferingUtils.PER_AGENT + "'" + "' or '"
						+ BufferingUtils.PER_SIMULATION_BUFFERING + "'.", IGamlIssue.WRONG_TYPE);
			}

			// Starting from here we validate the attributes, other validations must be done before
			if (att == null) return;

			final boolean isMap = att instanceof IExpression.Map;
			if (!isMap && !att.getGamlType().isTranslatableInto(Types.LIST.of(Types.STRING))) {
				desc.error("attributes must be expressed as a map<string, unknown> or as a list<string>",
						IGamlIssue.WRONG_TYPE, ATTRIBUTES);
				return;
			}
			if (isMap && att.getGamlType().getKeyType() != Types.STRING) {
				desc.error(
						"The type of the keys of the attributes map must be string. These will be used for naming the attributes in the file",
						IGamlIssue.WRONG_TYPE, ATTRIBUTES);
				return;
			}

			if (ext != null && format == null && !"shp".equals(ext) && !"json".equals(ext) && !"geojson".equals(ext)
					|| format != null && !"shp".equals(format.literalValue())
							&& !"geojson".equals(format.literalValue()) && !"json".equals(format.literalValue())) {
				desc.warning("Attributes can only be defined for shape, geojson or json files", IGamlIssue.WRONG_TYPE,
						ATTRIBUTES);
			}

			/** The t. */
			final IType<?> t = dataType.getContentType();

			/** The species. */
			final ISpeciesDescription species = t.getSpecies();

			if (species == null && isMap) {
				desc.error("Attributes of geometries can only be specified with a list of attribute names",
						IGamlIssue.UNKNOWN_FACET, ATTRIBUTES);
			}
			// Error deactivated for fixing #2982.
			// desc.error("Attributes can only be saved for agents", IGamlIssue.UNKNOWN_FACET,
			// att == null ? WITH : ATTRIBUTES);

		}

	}

	/** The attributes facet. */
	private final IExpression attributesFacet;

	/** The item. */
	private final IExpression item;

	/** The file. */
	private final IExpression file;

	/** The format. */
	private final IExpression format;

	/** The rewrite expr. */
	private final IExpression rewriteExpr;

	/** The buffering strategy. */
	private final IExpression bufferingStrategy;

	/**
	 * Instantiates a new save statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public SaveStatement(final IDescription desc) {
		super(desc);
		item = desc.getFacetExpr(IKeyword.DATA);
		file = getFacet(IKeyword.TO);
		format = getFacet(IKeyword.FORMAT);
		rewriteExpr = getFacet(IKeyword.REWRITE);
		attributesFacet = getFacet(IKeyword.ATTRIBUTES);
		bufferingStrategy = getFacet(IKeyword.BUFFERING);
	}

	/**
	 * Should overwrite.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	private boolean shouldOverwrite(final IScope scope) {
		if (rewriteExpr == null) return true;
		return Cast.asBool(scope, rewriteExpr.value(scope));
	}

	/**
	 * In case the save statement is called with a file object, calls the save method from this object
	 *
	 * @param scope
	 * @return
	 */
	protected Object saveFile(final IScope scope) {
		if (!Types.FILE.isAssignableFrom(item.getGamlType())) return null;
		final IGamaFile theFile = (IGamaFile) item.value(scope);
		if (theFile != null) {
			// Passes directly the facets of the statement, like crs, etc.
			theFile.save(scope, description.getFacets());
		}
		return theFile;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		// if item is null, there's nothing to write
		if (item == null || item.value(scope) == null) return null;

		// First case: we have no destination file, so it means the item is a file;
		if (file == null) return saveFile(scope);

		final String fileName = Cast.asString(scope, file.value(scope));
		final String filePath = FileUtils.constructAbsoluteFilePath(scope, fileName, false);
		if (filePath == null || "".equals(filePath)) return null;
		final File fileToSave = new File(filePath);
		String typeExp = getLiteral(IKeyword.FORMAT);
		// Second case: a filename is indicated but not the type. In that case,
		// we try to build a new GamaFile from it and save it
		if (typeExp == null) {
			final Object contents = item.value(scope);
			if (contents instanceof IContainer.Modifiable mc) {
				try {
					// We set a temporary flag to the scope, which should be readable by the GamaFile and indicate that
					// the file is created "for saving" (and not reading). Otherwise it might create an exception if the
					// file does not exist already (see #3684)
					scope.setData(IGamaFile.KEY_TEMPORARY_OUTPUT, true);
					final IGamaFile f = GamaFileType.createFile(scope, fileName, false, mc);
					f.save(scope, description.getFacets());
					return f;
				} catch (FlushBufferException e) {
					// Nothing to do : the corresponding GamaFile does not implement flushBuffer
					// Not really clean but well... see #3684. We silently log the error and continue with the format
					DEBUG.OUT(e.getMessage());
				} finally {
					// We remove the temporary flag
					scope.setData(IGamaFile.KEY_TEMPORARY_OUTPUT, null);
				}
			}
			typeExp = com.google.common.io.Files.getFileExtension(fileName);
		}

		// We may have the case of a string (instead of a literal)
		if (typeExp != null && !GamaAdditionRegistry.containsSaveDelegate(typeExp) && format != null
				&& format.getGamlType() == Types.STRING) {
			typeExp = Cast.asString(scope, format.value(scope));
			if (!GamaAdditionRegistry.containsSaveDelegate(typeExp)) { typeExp = null; }
		}

		// get the buffering strategy
		BufferingStrategies strategy = BufferingUtils.stringToBufferingStrategies(scope,
				GamaPreferences.Experimental.DEFAULT_SAVE_BUFFERING_STRATEGY.value(scope));
		if (bufferingStrategy != null) {
			strategy = BufferingUtils.stringToBufferingStrategies(scope, (String) bufferingStrategy.value(scope));
		}

		try {
			Files.createDirectories(fileToSave.toPath().getParent());
			boolean exists = fileToSave.exists() || BufferingUtils.getInstance().isFileWaitingToBeWritten(fileToSave);
			final boolean rewrite = shouldOverwrite(scope);

			IExpression header = getFacet(IKeyword.HEADER);
			final boolean addHeader = !exists && (header == null || Cast.asBool(scope, header.value(scope)));
			final String type = (typeExp != null ? typeExp : "text").trim().toLowerCase();
			String code = null;
			IExpression crsCode = getFacet("crs");
			if (crsCode != null) {
				final IType tt = crsCode.getGamlType();
				if (tt.id() == IType.INT || tt.id() == IType.FLOAT) {
					code = EPSG_LABEL + Cast.asInt(scope, crsCode.value(scope));
				} else if (tt.id() == IType.STRING) { code = (String) crsCode.value(scope); }
			}
			//
			IType itemType = item.getGamlType();
			ISaveDelegate delegate = GamaAdditionRegistry.getSaveDelegate(type, itemType);
			if (delegate != null) {
				var saveOptions = new SaveOptions(code, addHeader, type, attributesFacet, strategy, rewrite);
				delegate.save(scope, item, fileToSave, saveOptions);
				return Cast.asString(scope, file.value(scope));
			}
			throw GamaRuntimeException.error("Format not recognized: " + type, scope);
		} catch (final GamaRuntimeException e) {
			throw e;
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

}
