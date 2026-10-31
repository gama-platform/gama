/*******************************************************************************************************
 *
 * GamlLabelProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.labeling;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import gama.annotations.constants.IKeyword;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.ast.IGamlLabelProvider;
import gama.api.compilation.ast.ISyntacticElement;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.gaml.operators.Strings;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gaml.compiler.EGaml;
import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Import;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.StandaloneExperiment;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.VarDefinition;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.ui.outline.GamlOutlineTreeProvider;

/**
 * Provides labels for a EObjects.
 *
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#labelProvider
 */
@Singleton

public class GamlLabelProvider extends DefaultEObjectLabelProvider implements IGamlLabelProvider {

	private static final Map<String, String> ICON_ALIASES = Map.ofEntries(
			Map.entry("include", "imports"), Map.entry("var", "unknown"), Map.entry("const", "unknown"),
			Map.entry(Types.SPECIES_VAR, "species"), Map.entry("color", "rgb"), Map.entry("picture", "image"),
			Map.entry("BDIPlan", "plan"), Map.entry("Norm", "norm"), Map.entry("Sanction", "sanction"),
			Map.entry("type", "object"));

	/** The name provider. */
	@Inject private IQualifiedNameProvider nameProvider;

	/**
	 * Instantiates a new gaml label provider.
	 *
	 * @param delegate
	 *            the delegate
	 */
	@Inject
	public GamlLabelProvider(final AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	/**
	 * Image.
	 *
	 * @param ele
	 *            the ele
	 * @return the string
	 */
	String image(final Model ele) {
		return "_model.png";
	}

	/**
	 * Text.
	 *
	 * @param ele
	 *            the ele
	 * @return the string
	 */
	// Import
	String text(final Import ele) {
		String display = ele.getImportURI();
		final int index = display.lastIndexOf('/');
		if (index >= 0) { display = display.substring(index + 1); }
		return "import " + display;
	}

	/**
	 * Text.
	 *
	 * @param e
	 *            the e
	 * @return the string
	 */
	String text(final StandaloneExperiment e) {
		return "Experiment " + e.getName();
	}

	/**
	 * Text.
	 *
	 * @param ele
	 *            the ele
	 * @return the string
	 */
	String text(final EObject ele) {
		String text;
		String key = EGaml.getInstance().getKeyOf(ele);
		if (key == null) { key = ""; }
		text = key;
		key = null;
		if (ele instanceof Statement) {
			if (IKeyword.PARAMETER.equals(text)) return parameterText((Statement) ele);
			if (GamlOutlineTreeProvider.isAttribute((Statement) ele)) return attributeText((S_Definition) ele);
			if (GamlOutlineTreeProvider.isAction((Statement) ele)) return actionText((Statement) ele);
			String name = EGaml.getInstance().getNameOf(ele);
			if (name == null) {
				final Expression expr = ((Statement) ele).getExpr();
				if (expr != null) { name = EGaml.getInstance().getKeyOf(expr); }
			}
			if (name == null) {
				QualifiedName qn = null;
				try {
					qn = nameProvider.getFullyQualifiedName(ele);
					name = qn == null ? null : qn.toString();
				} catch (final IllegalArgumentException e) {
					name = null;
				}

			}
			text += " " + (name == null ? "" : name);
		}
		// } else {
		// text = key + " " + qn.toString();
		// }
		return StringUtils.capitalize(text);
	}

	/**
	 * @param ele
	 * @return
	 */
	private String attributeText(final S_Definition ele) {
		String type = EGaml.getInstance().getKeyOf(ele);
		String key = IKeyword.CONST.equals(type) ? type : null;
		final Map<String, Facet> map = EGaml.getInstance().getFacetsMapOf(ele);
		if (ele.getBlock() != null /* && ele.getBlock().getFunction() != null */) {
			key = "function";
		} else if (map.containsKey(IKeyword.FUNCTION) || map.containsKey("->")) { key = "function"; }
		if (IKeyword.VAR.equals(type) || IKeyword.CONST.equals(type)) {
			final Facet f = map.get(IKeyword.TYPE);
			if (f != null) { type = EGaml.getInstance().getKeyOf(f.getExpr()); }
		}
		String name = EGaml.getInstance().getNameOf(ele);
		if (name == null) {
			final Expression expr = ele.getExpr();
			if (expr != null) { name = EGaml.getInstance().getKeyOf(expr); }
		}
		if (name == null) {
			QualifiedName qn = null;
			try {
				qn = nameProvider.getFullyQualifiedName(ele);
				name = qn == null ? null : qn.toString();
			} catch (final IllegalArgumentException e) {
				name = null;
			}

		}
		String first = "Attribute ";
		if (type.contains("file")) { type = "file"; }
		if (Types.get(type) == Types.NO_TYPE) { first = Strings.capitalize(null, type) + " "; }

		return first + (name == null ? "" : name) + " (" + type + ") " + (key == null ? "" : "(" + key + ") ");

	}

	/**
	 * @param ele
	 * @return
	 */
	private static String actionText(final Statement ele) {
		final String type = EGaml.getInstance().getKeyOf(ele);
		final String name = EGaml.getInstance().getNameOf(ele);
		return "Action " + name + " " + (IKeyword.ACTION.equals(type) ? "" : " (" + type + ")");
	}

	/**
	 * Text.
	 *
	 * @param obj
	 *            the obj
	 * @return the string
	 */
	String text(final Model obj) {
		return "Model " + obj.getName();
	}

	/**
	 * Parameter text.
	 *
	 * @param p
	 *            the p
	 * @return the string
	 */
	protected String parameterText(final Statement p) {
		String type = null;
		String var = null;
		final Map<String, Facet> map = EGaml.getInstance().getFacetsMapOf(p);
		Facet f = map.get(IKeyword.VAR);
		if (f != null) {
			final Expression vr = f.getExpr();
			if (vr instanceof VariableRef) {
				final VarDefinition vd = ((VariableRef) vr).getRef();
				if (vd instanceof S_Definition) {
					type = EGaml.getInstance().getKeyOf(vd);
					var = EGaml.getInstance().getNameOf(vd);
				}
			}
		}
		String name = null;
		f = map.get(IKeyword.NAME);
		if (f == null) {
			final Expression e = p.getExpr();
			if (e instanceof StringLiteral) { name = ((StringLiteral) e).getOp(); }
		} else {
			final Expression e = f.getExpr();
			if (e instanceof StringLiteral) { name = ((StringLiteral) e).getOp(); }
		}
		return "Parameter " + "\"" + name + "\""
				+ (var == null ? "" : " (" + var + ")" + (type == null ? "" : " (" + type + ")"));
	}

	/**
	 * Image.
	 *
	 * @param ele
	 *            the ele
	 * @return the string
	 */
	String image(final Import ele) {
		return "_imports.png";
	}

	/**
	 * Image.
	 *
	 * @param ele
	 *            the ele
	 * @return the string
	 */
	String image(final S_Experiment ele) {
		final Map<String, Facet> facets = EGaml.getInstance().getFacetsMapOf(ele);
		Facet type = facets.get(IKeyword.TYPE);
		if (type == null) return "_gui.png";
		return typeImage(EGaml.getInstance().toString(type.getExpr()));
	}

	/**
	 * Image.
	 *
	 * @param ele
	 *            the ele
	 * @return the string
	 */
	String image(final StandaloneExperiment ele) {
		final Map<String, Facet> facets = EGaml.getInstance().getFacetsMapOf(ele);
		Facet type = facets.get(IKeyword.TYPE);
		if (type == null) return "_gui.png";
		return typeImage(EGaml.getInstance().toString(type.getExpr()));
	}

	/**
	 * Image.
	 *
	 * @param ele
	 *            the ele
	 * @return the string
	 */
	// Statement : keyword.value
	public String image(final Statement ele) {
		final String kw = EGaml.getInstance().getKeyOf(ele);
		if (GamlOutlineTreeProvider.isAction(ele)) {
			final String type = definitionTypeImage((S_Definition) ele);
			return "_action.png".equals(type) || kw == null ? "_action.png" : decorate(type, "_badge_action");
		}
		if (IKeyword.PARAMETER.equals(kw)) return parameterImage(ele);
		if (GamlOutlineTreeProvider.isAttribute(ele)) {
			final S_Definition definition = (S_Definition) ele;
			final Map<String, Facet> facets = EGaml.getInstance().getFacetsMapOf(ele);
			String icon = definitionTypeImage(definition);
			if (ele.getBlock() != null || facets.containsKey(IKeyword.FUNCTION) || facets.containsKey("->")) {
				icon = decorate(icon, "_badge_function");
			} else if (facets.containsKey(IKeyword.UPDATE)) {
				icon = decorate(icon, "_badge_update");
			}
			final Facet constant = facets.get(IKeyword.CONST);
			if (IKeyword.CONST.equals(kw) || constant != null
					&& "true".equals(EGaml.getInstance().toString(constant.getExpr()))) {
				icon = decorate(icon, "_badge_const");
			}
			return icon;
		}
		return typeImage(kw);
	}

	private String definitionTypeImage(final S_Definition definition) {
		final Facet facet = EGaml.getInstance().getFacetsMapOf(definition).get(IKeyword.TYPE);
		final Expression type = facet == null ? definition.getTkey() : facet.getExpr();
		final String key = type == null ? EGaml.getInstance().getKeyOf(definition)
				: EGaml.getInstance().getKeyOf(type);
		if (type instanceof TypeRef ref && ref.getRef() instanceof S_Species species && !hasIcon(key)) {
			return switch (EGaml.getInstance().getKeyOf(species)) {
				case IKeyword.CLASS -> "_object.png";
				case IKeyword.SKILL -> "_skill.png";
				case null -> "_missing.png";
				default -> "_agent.png";
			};
		}
		return typeImage(key);
	}

	private static String decorate(final String icon, final String badge) {
		return icon.substring(0, icon.length() - ".png".length()) + "+" + badge + ".png";
	}

	/**
	 * Parameter image.
	 *
	 * @param p
	 *            the p
	 * @return the string
	 */
	protected String parameterImage(final Statement p) {
		final Facet f = EGaml.getInstance().getFacetsMapOf(p).get(IKeyword.VAR);
		if (f != null) {
			final Expression vr = f.getExpr();
			if (vr instanceof VariableRef) {
				final VarDefinition vd = ((VariableRef) vr).getRef();
				if (vd instanceof S_Definition definition) {
					return decorate(definitionTypeImage(definition), "_badge_parameter");
				}
			}
		}
		final Facet type = EGaml.getInstance().getFacetsMapOf(p).get(IKeyword.TYPE);
		return type == null ? "_parameter.png"
				: decorate(typeImage(EGaml.getInstance().getKeyOf(type.getExpr())), "_badge_parameter");
	}

	/**
	 * Type image.
	 *
	 * @param string
	 *            the string
	 * @return the string
	 */
	public String typeImage(final String string) {
		if (string == null || string.isBlank()) return "_missing.png";
		String key = string.trim();
		if (key.length() > 1 && (key.startsWith("\"") && key.endsWith("\"")
				|| key.startsWith("'") && key.endsWith("'"))) {
			key = key.substring(1, key.length() - 1);
		}
		final int generic = key.indexOf('<');
		if (generic >= 0) { key = key.substring(0, generic).trim(); }
		key = ICON_ALIASES.getOrDefault(key, key);
		if (hasIcon(key)) return "_" + key + ".png";
		if (key.endsWith("_file")) return "_file.png";
		final IType<?> type = Types.get(key);
		if (type != Types.NO_TYPE) {
			if (type.isNumber()) return "_float.png";
			if (type.isAgentType()) return "_agent.png";
			if (type.isSkillType()) return "_skill.png";
			if (type.isContainer()) return "_container.png";
			return "_object.png";
		}
		final var symbol = ArtefactRegistry.getStatementArtefact(key);
		if (symbol == null) return "_missing.png";
		return "_" + switch (symbol.getKind()) {
			case MODEL -> "model";
			case SPECIES -> "species";
			case CLASS -> "class";
			case BEHAVIOR -> "behavior";
			case PARAMETER -> "parameter";
			case OUTPUT -> "output";
			case LAYER -> "layer";
			case SKILL -> "skill";
			case BATCH_METHOD, BATCH_SECTION -> "method";
			case EXPERIMENT -> "gui";
			case NUMBER -> "float";
			case REGULAR -> "object";
			default -> "statement";
		} + ".png";
	}

	private static boolean hasIcon(final String key) {
		return key != null && GamaIcon.exist(IGamaIcons.GAML_PATH + "_" + key);
	}

	/**
	 * @see gama.api.compilation.ast.IGamlLabelProvider#getText(gama.api.compilation.ast.ISyntacticElement)
	 */
	@Override
	public String getText(final ISyntacticElement element) {
		return this.getText(element.getElement());
	}

	@Override
	public ImageDescriptor getImageDescriptor(final ISyntacticElement element) {
		return this.getImageDescriptor(element.getElement());
	}

	@Override
	public OutlineGroup getOutlineGroup(final ISyntacticElement element, final boolean inExperiment) {
		if (element == null || !(element.getElement() instanceof Statement s)) return null;
		return GamlOutlineTreeProvider.groupOf(s, inExperiment);
	}

	@Override
	public ImageDescriptor convertToImageDescriptor(final Object obj) {
		return super.convertToImageDescriptor(obj);
	}

}
