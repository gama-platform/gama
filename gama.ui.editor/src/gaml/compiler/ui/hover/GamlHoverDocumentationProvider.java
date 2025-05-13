/*******************************************************************************************************
 *
 * GamlHoverDocumentationProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.hover;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import com.google.inject.Inject;

import gama.core.common.interfaces.IDisplayCreator.DisplayDescription;
import gama.core.common.interfaces.IDocManager;
import gama.core.common.interfaces.IExperimentAgentCreator.ExperimentAgentDescription;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.util.FileUtils;
import gama.core.runtime.GAMA;
import gama.core.util.file.IGamaFileMetaData;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.kernel.GamaMetaModel;
import gama.gaml.compilation.kernel.GamaSkillRegistry;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.expressions.units.UnitConstantExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.interfaces.IGamlDescription;
import gama.gaml.operators.Strings;
import gama.gaml.statements.DoStatement;
import gama.gaml.types.Types;
import gaml.compiler.gaml.ActionRef;
import gaml.compiler.gaml.ArgumentPair;
import gaml.compiler.gaml.Array;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.ExpressionList;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Function;
import gaml.compiler.gaml.Import;
import gaml.compiler.gaml.Parameter;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Display;
import gaml.compiler.gaml.S_Do;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.UnitFakeDefinition;
import gaml.compiler.gaml.UnitName;
import gaml.compiler.gaml.VarDefinition;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.gaml.speciesOrGridDisplayStatement;
import gaml.compiler.gaml.resource.GamlResourceServices;
import gaml.compiler.gaml.util.GamlSwitch;
import gaml.compiler.ui.editor.GamlHyperlinkDetector;

/**
 * The class GamlHoverDocumentationProvider.
 *
 * @author drogoul
 * @since 30 déc. 2023
 *
 */
public class GamlHoverDocumentationProvider extends GamlSwitch<IGamlDescription> {

	/** The detector. */
	@Inject protected GamlHyperlinkDetector detector;

	/** The documenter. */
	private final IDocManager documenter = GamlResourceServices.getResourceDocumenter();

	/**
	 * The Doc.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 30 déc. 2023
	 */
	record Result(String title, String doc) implements IGamlDescription {

		/**
		 * Gets the documentation.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the documentation
		 * @date 30 déc. 2023
		 */
		@Override
		public Doc getDocumentation() { return new ConstantDoc(doc); }

		/**
		 * Gets the title.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the title
		 * @date 30 déc. 2023
		 */
		@Override
		public String getTitle() { return title; }
	}

	/**
	 * Gets the documentation attached to an EObject or null if no doc can be found. Relies first on polymorphism
	 * through the use of {@link GamlSwitch} and then on specific methods
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return the doc
	 * @date 30 déc. 2023
	 */
	IGamlDescription getDoc(final EObject o) {
		if (o == null) return null;
		int id = o.eClass().getClassifierID();
		IGamlDescription result = doSwitch(id, o);
		if (result == null) {
			if (o instanceof VariableRef vr) {
				result = specialCaseVariableRef(vr);
			} else if (o instanceof TypeRef type) { result = specialCaseTypeRef(type); }
		}
		return result;
	}

	@Override
	public IGamlDescription caseImport(final Import imp) {
		String uri = imp.getImportURI();
		uri = uri.substring(uri.lastIndexOf('/') + 1);
		final String model = imp.getName() != null ? "micro-model" : "model";
		String title = "Import of the " + model + " defined in <i>" + uri + "</i>";
		String doc = "ctrl-click or cmd-click on the path to open this model in a new editor";
		return new Result(title, doc);
	}

	@Override
	public IGamlDescription caseS_Global(final S_Global global) {
		EObject model = global.eContainer().eContainer();
		String title = "Global section of <i>" + getDoc(model).getTitle() + "</i>";
		String doc = "";
		return new Result(title, doc);
	}

	@Override
	public IGamlDescription caseStringLiteral(final StringLiteral string) {
		final URI iu = detector.getURI(string);
		if (iu != null) {
			String doc = "";
			IFile file;
			if (FileUtils.isFileExistingInWorkspace(iu)) {
				file = FileUtils.getWorkspaceFile(iu);
				final IGamaFileMetaData data = GAMA.getGui().getMetaDataProvider().getMetaData(file, false, true);
				if (data == null) {
					final String ext = file.getFileExtension();
					doc = "This " + ext + " file has no metadata associated with it";
				} else {
					String s = data.getDocumentation().toString();
					if (s != null) { doc = s.replace(Strings.LN, "<br/>"); }
				}
			} else { // absolute file
				file = FileUtils.createLinkToExternalFile(string.getOp(), string.eResource().getURI());
				if (file == null) {
					doc = "This file is outside the workspace and cannot be found.";
				} else {
					final IGamaFileMetaData data = GAMA.getGui().getMetaDataProvider().getMetaData(file, false, true);
					if (data == null) {
						final String ext = file.getFileExtension();
						doc = "This external " + ext + " file has no metadata associated with it";
					} else {
						String s = data.getDocumentation().toString();
						if (s != null) { doc = s.replace(Strings.LN, "<br/>"); }
					}
				}
			}
			if (file != null) return new Result("File " + file.getFullPath(), doc);
		}
		return null;
	}

	@Override
	public IGamlDescription caseTypeRef(final TypeRef type) {
		// final Statement s = EGaml.getInstance().getStatement(type);
		// if (s instanceof S_Definition sd && sd.getTkey() == type) {
		// final IGamlDescription gd = documenter.getGamlDocumentation(s);
		// if (gd != null) return gd;
		// }
		// Case for the type of displays
		final Statement s = EGaml.getInstance().getSurroundingStatement(type);
		String name = EGaml.getInstance().getKeyOf(type);
		if (s instanceof S_Display) {
			DisplayDescription dc = IGui.DISPLAYS.get(name);
			if (dc != null) return dc;
			return new IGamlDescription() {

				@Override
				public String getTitle() { return "Unknown type of display " + name; }

				@Override
				public Doc getDocumentation() {
					return new ConstantDoc(name
							+ " is not a registered display type. Please visit <a href=\"https://gama-platform.org/wiki/Displays\"> https://gama-platform.org/wiki/Displays</a> for more information.");
				}
			};
		}
		if (s instanceof S_Experiment) {
			ExperimentAgentDescription ead =
					(ExperimentAgentDescription) GamaMetaModel.INSTANCE.getExperimentCreator(name);
			if (ead != null) return ead;
			return new IGamlDescription() {

				@Override
				public String getTitle() { return "Unknown type of experiment " + name; }

				@Override
				public Doc getDocumentation() {
					return new ConstantDoc(name
							+ " is not a registered experiment type. Please visit <a=href=\"https://gama-platform.org/wiki/DefiningGUIExperiment#types-of-experiments\">https://gama-platform.org/wiki/DefiningGUIExperiment#types-of-experiments</a> for more information.");
				}

			};
		}
		if (IKeyword.METHOD.equals(name)) return new IGamlDescription() {

			@Override
			public String getTitle() { return "Definition of the exploration method to use in this experiment "; }

			@Override
			public Doc getDocumentation() {
				return new ConstantDoc(
						"The facets that can be defined to specify the exploration are specific to each method. Please visit <a href=\"https://gama-platform.org/wiki/ExplorationMethods\">https://gama-platform.org/wiki/ExplorationMethods</a> for more information.");
			}

		};
		if (IKeyword.CHART.equals(EGaml.getInstance().getKeyOf(s))) return new IGamlDescription() {

			@Override
			public String getTitle() { return "The type (" + name + ") of charts to draw"; }

			@Override
			public Doc getDocumentation() {
				return new ConstantDoc(
						"Several types of charts are available (pie, series, histogram, xy...). Please visit <a href=\"https://gama-platform.org/wiki/DefiningCharts\">https://gama-platform.org/wiki/DefiningCharts </a> for more information.");
			}

		};
		return null;
	}

	@Override
	public IGamlDescription caseFacet(final Facet facet) {
		// CASE do run_thread interval: 2#s;
		if (facet.eContainer() instanceof S_Do sdo && sdo.getExpr() instanceof VariableRef vr) {
			String key = EGaml.getInstance().getKeyOf(facet);
			if (!DoStatement.DO_FACETS.contains(key)) {
				String title = "Argument " + key + " of action " + EGaml.getInstance().getNameOfRef(sdo.getExpr());
				IGamlDescription action = documenter.getGamlDocumentation(vr);
				String doc = action == null ? "" : action.getDocumentation().get(key).toString();
				return new Result(title, doc);
			}
		}
		String facetName = facet.getKey();
		if (facetName.endsWith(":")) { facetName = facetName.substring(0, facetName.length() - 1); }
		final EObject cont = facet.eContainer();
		String key = EGaml.getInstance().getKeyOf(cont);
		if (cont instanceof speciesOrGridDisplayStatement ds) {
			String layerName = ds.getKey();
			if (IKeyword.SPECIES.equals(layerName)) {
				key = IKeyword.SPECIES_LAYER;
			} else {
				key = IKeyword.GRID_LAYER;
			}
		} else if (cont instanceof S_Definition sd && IKeyword.METHOD.equals(key)) { key = sd.getName(); }
		final SymbolProto p = DescriptionFactory.getProto(key, null);
		if (p != null) return p.getPossibleFacets().get(facetName);
		return null;
	}

	@Override
	public IGamlDescription caseArgumentPair(final ArgumentPair pair) {
		if (pair.eContainer() instanceof ExpressionList el && el.eContainer() instanceof Array array
				&& array.eContainer() instanceof Facet facet) {
			// CASE do run_thread with: [interval::2#s];
			if (facet.eContainer() instanceof S_Do sdo && sdo.getExpr() instanceof VariableRef vr) {
				String key = pair.getOp();
				if (!DoStatement.DO_FACETS.contains(key)) {
					String title = "Argument " + key + " of action " + EGaml.getInstance().getNameOfRef(vr);
					IGamlDescription action = documenter.getGamlDocumentation(vr);
					String doc = action == null ? "" : action.getDocumentation().get(key).toString();
					return new Result(title, doc);
				}
			} else
			// CASE create xxx with: [var::yyy]
			if (facet.eContainer() instanceof Statement sdo && IKeyword.CREATE.equals(sdo.getKey())) {
				String key = pair.getOp();
				IGamlDescription species = documenter.getGamlDocumentation(sdo.getExpr());
				if (species != null) {
					String title = "Attribute " + key + " defined in " + species.getTitle();
					String doc = species.getDocumentation().get(key).toString();
					return new Result(title, doc);
				}

			}
		}
		return null;
	}

	@Override
	public IGamlDescription caseVariableRef(final VariableRef var) {
		// CASE do run_thread with: (interval::2#s);
		if (var.eContainer() instanceof Parameter pair && pair.eContainer() instanceof ExpressionList el
				&& el.eContainer() instanceof Facet facet && facet.eContainer() instanceof S_Do sdo
				&& sdo.getExpr() instanceof VariableRef v) {
			String key = EGaml.getInstance().getKeyOf(pair);
			if (!DoStatement.DO_FACETS.contains(key)) {
				String title = "Argument " + key + " of action " + EGaml.getInstance().getNameOfRef(sdo.getExpr());
				IGamlDescription action = documenter.getGamlDocumentation(v);
				String doc = action == null ? "" : action.getDocumentation().get(key).toString();
				return new Result(title, doc);
			}
		}
		// CASE do run_thread (interval: 2#s); unknown aa <- self.run_thread (interval: 2#s); aa <- run_thread
		// (interval: 2#s);
		if (var.eContainer() instanceof Parameter param && param.eContainer() instanceof ExpressionList el
				&& el.eContainer() instanceof Function function && function.getLeft() instanceof ActionRef ar) {
			final IGamlDescription description = documenter.getGamlDocumentation(function);
			if (description != null) {
				VarDefinition vd = var.getRef();
				String title = "Argument " + vd.getName() + " of action "
						+ EGaml.getInstance().getNameOfRef(function.getLeft());
				String doc = description.getDocumentation().get(vd.getName()).toString();
				return new Result(title, doc);
			}
		}
		// Case of species xxx skills: [skill]
		if (var.eContainer() instanceof ExpressionList el && el.eContainer() instanceof Array array
				&& array.eContainer() instanceof Facet facet && facet.getKey().startsWith(IKeyword.SKILLS)) {
			VarDefinition vd = var.getRef();
			String name = vd.getName();
			SkillDescription skill = GamaSkillRegistry.INSTANCE.get(name);
			if (skill != null) return skill;
		}
		// case of style: in chart
		if (var.eContainer() instanceof Facet f && IKeyword.STYLE.equals(EGaml.getInstance().getKeyOf(f))
				&& f.eContainer() instanceof Statement s && IKeyword.CHART.equals(EGaml.getInstance().getKeyOf(s)))
			return getDoc(f);
		return null;
	}

	@Override
	public IGamlDescription caseFunction(final Function function) {
		final ActionRef ref = function.getLeft() instanceof ActionRef ? (ActionRef) function.getLeft() : null;
		if (ref != null && ref.getRef() instanceof S_Definition def && def.getBlock() != null) {
			IGamlDescription doc = getDoc(def);
			if (doc != null) return doc;
		}
		return null;
	}

	@Override
	public IGamlDescription caseUnitName(final UnitName un) {
		final UnitFakeDefinition fake = un.getRef();
		if (fake != null) {
			final UnitConstantExpression unit = GAML.UNITS.get(fake.getName());
			if (unit != null) return unit;
		}
		return null;
	}

	@Override
	public IGamlDescription defaultCase(final EObject o) {
		return documenter.getGamlDocumentation(o);
	}

	/**
	 * Last chance to build a documentation in case we have a reference to a variable which is not documented itself
	 * (like in create xxx with: [var: yyy])
	 *
	 * @param vr
	 * @return
	 */
	private IGamlDescription specialCaseVariableRef(final VariableRef vr) {
		VarDefinition vd = vr.getRef();
		return documenter.getGamlDocumentation(vd);
	}

	/**
	 * If the type is not correctly documented, it can mean it masks a statement
	 *
	 * @param type
	 * @return
	 */
	private IGamlDescription specialCaseTypeRef(final TypeRef type) {
		String name = EGaml.getInstance().getKeyOf(type);
		// Can happen with statements that "look like" var declarations and which are not treated specially in the
		// grammar
		if (DescriptionFactory.isStatementProto(name)) return DescriptionFactory.getStatementProto(name);
		if (Types.hasType(name)) return Types.get(name);
		return null;
	}

}
