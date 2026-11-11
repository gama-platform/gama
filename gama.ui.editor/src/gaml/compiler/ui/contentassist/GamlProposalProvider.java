/*******************************************************************************************************
 *
 * GamlProposalProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.contentassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.IImageHelper;
import org.eclipse.xtext.ui.editor.contentassist.ConfigurableCompletionProposal;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControl;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.html.HtmlEscapers;
import com.google.inject.Inject;

import gama.annotations.constants.IKeyword;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.gaml.GAML;
import gama.api.gaml.types.Types;
import gama.ui.shared.access.GamlDocStyleHelper;
import gama.ui.shared.utils.WebHelper;
import gaml.compiler.EGaml;
import gaml.compiler.gaml.ArgumentDefinition;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.S_Callable;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_Loop;
import gaml.compiler.gaml.S_Reflex;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.StandaloneExperiment;
import gaml.compiler.gaml.Statement;
import gaml.compiler.scoping.BuiltinGlobalScopeProvider;
import gaml.compiler.scoping.GamlScopeProvider;
import gaml.compiler.services.GamlGrammarAccess;
import gaml.compiler.ui.labeling.GamlLabelProvider;

/**
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on how to customize content assistant
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlProposalProvider extends AbstractGamlProposalProvider {

	private static final int VARIABLE_PRIORITY = 3000;
	private static final int ACTION_PRIORITY = 2000;
	private static final int OTHER_PRIORITY = 1200;
	private static final int DECLARATION_PRIORITY = 1000;
	private static final int OPERATOR_PRIORITY = 900;
	private static final int STATEMENT_PRIORITY = 800;

	/**
	 * The Class GamlProposalCreator.
	 */
	class GamlProposalCreator extends DefaultProposalCreator {

		/** The context. */
		ContentAssistContext context;

		/**
		 * @param contentAssistContext
		 * @param ruleName
		 * @param qualifiedNameConverter
		 */
		public GamlProposalCreator(final ContentAssistContext contentAssistContext, final String ruleName,
				final IQualifiedNameConverter qualifiedNameConverter) {
			super(contentAssistContext, ruleName, qualifiedNameConverter);
			context = contentAssistContext;
		}

		@Override
		public ICompletionProposal apply(final IEObjectDescription candidate) {

			final ConfigurableCompletionProposal cp = (ConfigurableCompletionProposal) super.apply(candidate);
			boolean isOperator = false;
			if (cp != null) {
				final String type = candidate.getUserData(IKeyword.TYPE);
				if (type != null) {
					cp.setDisplayString(cp.getDisplayString().concat(" (Built-in " + type + ") "));
					isOperator = "operator".equals(type) || "unit".equals(type) || IKeyword.TYPE.equals(type);
				}
				final String kind = proposalKind(candidate, defaultProposalKind(candidate));
				final String origin = proposalOrigin(candidate,
						EcoreUtil.resolve(candidate.getEObjectOrProxy(), context.getResource()));
				if (origin != null) cp.setDisplayString(cp.getDisplayString() + " - " + origin);
				cp.setImage(proposalImage(candidate, kind, context));
				cp.setPriority(proposalPriority(kind));
				setProposalDocumentation(cp, candidate, kind, context);
			}

			if (".".equals(context.getPrefix())) {
				if (isOperator) return null;
				if (cp != null && cp.getPriority() > 500) { cp.setPriority(200); }
			}
			return cp;
		}

	}

	/**
	 * The Class GamlCompletionProposal.
	 */
	static class GamlCompletionProposal extends ConfigurableCompletionProposal {

		private Supplier<String> documentation;

		/**
		 * @param replacementString
		 * @param replacementOffset
		 * @param replacementLength
		 * @param cursorPosition
		 * @param image
		 * @param displayString
		 * @param contextInformation
		 * @param additionalProposalInfo
		 */
		public GamlCompletionProposal(final String replacementString, final int replacementOffset,
				final int replacementLength, final int cursorPosition, final Image image,
				final StyledString displayString, final IContextInformation contextInformation,
				final String additionalProposalInfo) {
			super(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
					contextInformation, additionalProposalInfo);
		}

		@Override
		public IInformationControlCreator getInformationControlCreator() {
			return parent -> createDocumentationControl(parent, false);
		}

		private IInformationControl createDocumentationControl(final Shell parent, final boolean resizable) {
			if (!BrowserInformationControl.isAvailable(parent)) return new DefaultInformationControl(parent, resizable);
			final var control = new XtextBrowserInformationControl(parent, "org.eclipse.jdt.ui.javadocfont", resizable) {
				@Override
				public IInformationControlCreator getInformationPresenterControlCreator() {
					return shell -> createDocumentationControl(shell, true);
				}
			};
			control.addLocationListener(new LocationAdapter() {
				@Override
				public void changing(final LocationEvent event) {
					if (event.location.startsWith("about:blank")) return;
					event.doit = false;
					if (event.location.startsWith("https://") || event.location.startsWith("http://")) {
						WebHelper.openPage(event.location);
					}
				}
			});
			return control;
		}

		@Override
		public String getAdditionalProposalInfo() {
			return documentation == null ? super.getAdditionalProposalInfo() : documentation.get();
		}

		@Override
		public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
			return monitor != null && monitor.isCanceled() ? null : getAdditionalProposalInfo();
		}

	}

	// private DefaultProposalCreator creator;

	/**
	 * The Class BuiltInProposal.
	 */
	static class BuiltInProposal {

		/** The name. */
		String name;

		/** The title. */
		StyledString title;

		/** The image. */
		Image image;

		/** The documentation. */
		String documentation;

		final String kind;

		/**
		 * Instantiates a new built in proposal.
		 *
		 * @param name
		 *            the name
		 * @param title
		 *            the title
		 * @param image
		 *            the image
		 * @param kind
		 *            the proposal category
		 */
		public BuiltInProposal(final String name, final StyledString title, final Image image, final String kind) {
			this.name = name;
			this.title = title;
			this.image = image;
			this.kind = kind;
		}

		/**
		 * @param documentation
		 */
		public void setDoc(final String documentation) { this.documentation = documentation; }
	}

	@Override
	protected String getDisplayString(final EObject element, final String q, final String shortName) {
		String qualifiedNameAsString = q;
		if (qualifiedNameAsString == null) { qualifiedNameAsString = shortName; }
		if (qualifiedNameAsString == null) {
			if (element == null) return null;
			qualifiedNameAsString = provider.getText(element);
		}
		return qualifiedNameAsString;
	}

	/** The Constant proposals. */
	static final List<BuiltInProposal> proposals = new ArrayList<>();

	/** The Constant facets. */
	static final Set<String> fields = new HashSet(), vars = new HashSet(), actions = new HashSet(),
			types = new HashSet(), skills = new HashSet(), constants = new HashSet(), units = new HashSet(),
			statements = new HashSet(), facets = new HashSet();

	/** The provider. */
	@Inject GamlLabelProvider provider;

	@Inject private IImageHelper imageHelper;
	@Inject private IEObjectDocumentationProvider documentationProvider;
	//
	// @Inject
	// private GamlJavaValidator validator;

	/** The ga. */
	@Inject private GamlGrammarAccess ga;

	@Inject private GamlScopeProvider scopeProvider;

	@Override
	public void createProposals(final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
		// Disabling for comments (see Issue 786)
		final EObject grammarElement = context.getCurrentNode().getGrammarElement();
		if (grammarElement == ga.getML_COMMENTRule() || grammarElement == ga.getSL_COMMENTRule()) return;

		final GamlCompletionContext completion = GamlCompletionContext.at(context);
		if (completion.memberAccess()) {
			addNameProposals(scopeProvider.getMemberCompletionScope(completion.model(), completion.receiver(),
					completion.receiverName(), GamlPackage.Literals.VARIABLE_REF__REF, context.getOffset()),
					false, completion, context, acceptor);
			addNameProposals(scopeProvider.getMemberCompletionScope(completion.model(), completion.receiver(),
					completion.receiverName(), GamlPackage.Literals.ACTION_REF__REF, context.getOffset()),
					true, completion, context, acceptor);
			return;
		}
		if (atStatementStart(completion, context)) {
			addNameProposals(scopeProvider.getCompletionScope(completion.model(),
					GamlPackage.Literals.VARIABLE_REF__REF, context.getOffset()), false, completion, context, acceptor);
			addNameProposals(scopeProvider.getCompletionScope(completion.model(),
					GamlPackage.Literals.ACTION_REF__REF, context.getOffset()), true, completion, context, acceptor);
		}
		super.createProposals(context, acceptor);
		addBuiltInElements(context, acceptor);
	}

	private boolean atStatementStart(final GamlCompletionContext completion, final ContentAssistContext context) {
		String previous = null;
		for (final var leaf : context.getRootNode().getLeafNodes()) {
			if (leaf.getEndOffset() > completion.replacementOffset()) break;
			if (!leaf.isHidden()) previous = leaf.getText();
		}
		if (!"{".equals(previous) && !";".equals(previous) && !"}".equals(previous)) return false;
		for (EObject current = completion.model(); current != null; current = current.eContainer()) {
			if (current instanceof Block block) {
				final EObject owner = block.eContainer();
				return owner instanceof Statement && !(owner instanceof S_Species)
						&& !(owner instanceof S_Global) && !(owner instanceof S_Experiment);
			}
		}
		return false;
	}

	@Override
	public void completeVariableRef_Ref(final EObject model, final Assignment assignment,
			final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
		final GamlCompletionContext completion = GamlCompletionContext.at(context);
		addNameProposals(scopeProvider.getCompletionScope(completion.model(),
				GamlPackage.Literals.VARIABLE_REF__REF, context.getOffset()),
				false, completion, context, acceptor);
	}

	@Override
	public void completeActionRef_Ref(final EObject model, final Assignment assignment, final ContentAssistContext context,
			final ICompletionProposalAcceptor acceptor) {
		final GamlCompletionContext completion = GamlCompletionContext.at(context);
		addNameProposals(scopeProvider.getCompletionScope(completion.model(),
				GamlPackage.Literals.ACTION_REF__REF, context.getOffset()),
				true, completion, context, acceptor);
	}

	private void addNameProposals(final IScope scope, final boolean action, final GamlCompletionContext completion,
			final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
		final Set<String> names = new HashSet<>();
		for (final IEObjectDescription candidate : scope.getAllElements()) {
			if (!acceptor.canAcceptMoreProposals()) return;
			final String name = candidate.getName().toString();
			if (!names.add(name) || !context.getMatcher().isCandidateMatchingPrefix(name, completion.prefix())) continue;
			final String kind = proposalKind(candidate,
					action ? IKeyword.ACTION : defaultProposalKind(candidate));
			final EObject definition = EcoreUtil.resolve(candidate.getEObjectOrProxy(), context.getResource());
			final String label = proposalLabel(kind, definition);
			final String type = proposalType(candidate, definition);
			final StyledString display = new StyledString(name);
			if (type != null && !type.isBlank()) display.append(" : " + type, StyledString.DECORATIONS_STYLER);
			display.append(" (" + label + ")", StyledString.QUALIFIER_STYLER);
			final String origin = proposalOrigin(candidate, definition);
			if (origin != null) display.append(" - " + origin, StyledString.QUALIFIER_STYLER);
			final ConfigurableCompletionProposal proposal = doCreateProposal(name,
					display,
					proposalImage(candidate, kind, context),
					completion.replacementOffset(), completion.replacementLength());
			proposal.setMatcher(context.getMatcher());
			proposal.setReplaceContextLength(completion.prefix().length());
			proposal.setPriority(proposalPriority(kind));
			setProposalDocumentation(proposal, candidate, kind, context);
			acceptor.accept(proposal);
		}
	}

	private String proposalLabel(final String kind, final EObject definition) {
		return switch (kind) {
			case "variable", "field" -> {
				if (definition instanceof ArgumentDefinition) yield "Argument";
				if (definition instanceof S_Loop || definition instanceof S_Definition
						&& definition.eContainer() instanceof Block block
						&& !(block.eContainer() instanceof S_Species) && !(block.eContainer() instanceof S_Global)
						&& !(block.eContainer() instanceof S_Experiment)
						&& !(block.eContainer() instanceof StandaloneExperiment)) yield "Local variable";
				yield "Attribute";
			}
			case IKeyword.ACTION -> "Action";
			case IKeyword.TYPE -> "Type";
			case "constant" -> "Constant";
			case "operator" -> "Operator";
			case IKeyword.SKILL -> "Skill";
			default -> "Other";
		};
	}

	private String proposalType(final IEObjectDescription candidate, final EObject definition) {
		final String builtin = candidate.getUserData(BuiltinGlobalScopeProvider.MEMBER_TYPE);
		if (builtin != null) return builtin;
		if (definition instanceof ArgumentDefinition argument) return sourceText(argument.getType());
		if (definition instanceof S_Definition declaration) {
			final var facets = EGaml.getInstance().getFacetsMapOf(declaration);
			final Facet type = facets.get(IKeyword.TYPE);
			if (type != null && type.getExpr() != null) return sourceText(type.getExpr());
			if (declaration.getTkey() != null) return sourceText(declaration.getTkey());
			final Facet returns = facets.get(IKeyword.RETURNS);
			if (returns != null) return returns.getName();
			if (declaration instanceof S_Callable) return "void";
		}
		return null;
	}

	private String sourceText(final EObject object) {
		if (object == null) return null;
		final var node = NodeModelUtils.findActualNodeFor(object);
		return node == null ? null : NodeModelUtils.getTokenText(node);
	}

	private String proposalOrigin(final IEObjectDescription candidate, final EObject definition) {
		final String builtin = candidate.getUserData(BuiltinGlobalScopeProvider.MEMBER_ORIGIN);
		if (builtin != null && !builtin.isBlank()) return builtin;
		final List<String> owners = new ArrayList<>();
		for (EObject owner = definition.eContainer(); owner != null; owner = owner.eContainer()) {
			if (owner instanceof S_Callable callable) {
				owners.add("action " + callable.getName());
			} else if (owner instanceof S_Species species) {
				owners.add(EGaml.getInstance().getKeyOf(species) + " " + species.getName());
			} else if (owner instanceof S_Global) {
				owners.add("global");
			} else if (owner instanceof S_Experiment experiment) {
				owners.add("experiment " + experiment.getName());
			} else if (owner instanceof StandaloneExperiment experiment) {
				owners.add("experiment " + experiment.getName());
			} else if (owner instanceof S_Reflex behavior) {
				final String name = behavior.getName();
				owners.add(EGaml.getInstance().getKeyOf(behavior)
						+ (name == null || name.isBlank() ? "" : " " + name));
			} else if (owner instanceof Statement statement
					&& IKeyword.ASPECT.equals(EGaml.getInstance().getKeyOf(statement))) {
				final String name = sourceText(statement.getExpr());
				owners.add("aspect" + (name == null || name.isBlank() ? "" : " " + name));
			} else if (owner instanceof Model model && owners.isEmpty()) {
				owners.add("model " + model.getName());
			}
		}
		return owners.isEmpty() ? null : String.join(" in ", owners);
	}

	private void setProposalDocumentation(final ConfigurableCompletionProposal proposal,
			final IEObjectDescription candidate, final String kind, final ContentAssistContext context) {
		if (proposal instanceof GamlCompletionProposal completion) {
			completion.documentation = Suppliers.memoize(() -> {
				if ("operator".equals(kind)) return operatorDocumentation(candidate.getName().toString());
				final EObject definition = EcoreUtil.resolve(candidate.getEObjectOrProxy(), context.getResource());
				final String type = proposalType(candidate, definition);
				String signature = candidate.getUserData(BuiltinGlobalScopeProvider.MEMBER_SIGNATURE);
				if (signature == null) {
					signature = (type == null ? "" : type + " ") + candidate.getName();
					if (definition instanceof S_Callable callable) {
						final List<String> arguments = new ArrayList<>();
						for (final ArgumentDefinition argument : callable.getArgs()) {
							arguments.add(sourceText(argument.getType()) + " " + argument.getName());
						}
						signature += "(" + String.join(", ", arguments) + ")";
					}
				}
				final String title = proposalLabel(kind, definition) + " "
						+ HtmlEscapers.htmlEscaper().escape(signature);
				final String comment = NodeModelUtils.findActualNodeFor(definition) == null ? null
						: documentationProvider.getDocumentation(definition);
				String body = candidate.getUserData("doc");
				final String origin = proposalOrigin(candidate, definition);
				if (origin != null) {
					body = "<p>Defined in <code>" + HtmlEscapers.htmlEscaper().escape(origin) + "</code></p>"
							+ (body == null ? "" : body);
				}
				if (definition.eResource() != null && !(definition instanceof Model)) {
					final String location = definition.eResource().getURI().lastSegment();
					if (location != null) {
						body = (body == null ? "" : body) + "<p>Declared in <code>"
								+ HtmlEscapers.htmlEscaper().escape(location) + "</code></p>";
					}
				}
				return "<html><head><style>" + GamlDocStyleHelper.getDocCss() + "</style></head><body>"
						+ GamlDocStyleHelper.wrapWithCard(title, comment, body) + "</body></html>";
			});
		}
	}

	private String operatorDocumentation(final String name) {
		final StringBuilder body = new StringBuilder();
		for (final IArtefact.Operator operator : GAML.getOperatorsNamed(name).values()) {
			String documentation = operator.getDocumentation().toString();
			final String plugin = operator.getDefiningPlugin();
			if (plugin != null && !plugin.isBlank()) {
				documentation = "<p>Defined in plug-in <code>" + HtmlEscapers.htmlEscaper().escape(plugin)
						+ "</code></p>" + documentation;
			}
			body.append(GamlDocStyleHelper.wrapWithCard(
					HtmlEscapers.htmlEscaper().escape(operator.getTitle()), null, documentation));
		}
		return "<html><head><style>" + GamlDocStyleHelper.getDocCss() + "</style></head><body>" + body + "</body></html>";
	}

	private String defaultProposalKind(final IEObjectDescription candidate) {
		if (GamlPackage.Literals.ACTION_DEFINITION.isSuperTypeOf(candidate.getEClass())) return IKeyword.ACTION;
		if (GamlPackage.Literals.VAR_DEFINITION.isSuperTypeOf(candidate.getEClass())) return "variable";
		if (GamlPackage.Literals.TYPE_DEFINITION.isSuperTypeOf(candidate.getEClass())) return IKeyword.TYPE;
		if (GamlPackage.Literals.SKILL_FAKE_DEFINITION.isSuperTypeOf(candidate.getEClass())) return IKeyword.SKILL;
		if (GamlPackage.Literals.UNIT_FAKE_DEFINITION.isSuperTypeOf(candidate.getEClass())) return "unit";
		return "other";
	}

	private String proposalKind(final IEObjectDescription candidate, final String fallback) {
		final String kind = candidate.getUserData(IKeyword.TYPE);
		return kind == null ? fallback : kind;
	}

	private int proposalPriority(final String kind) {
		return switch (kind) {
			case "variable", "field" -> VARIABLE_PRIORITY;
			case IKeyword.ACTION -> ACTION_PRIORITY;
			case "declaration" -> DECLARATION_PRIORITY;
			case "operator" -> OPERATOR_PRIORITY;
			case "statement" -> STATEMENT_PRIORITY;
			default -> OTHER_PRIORITY;
		};
	}

	private Image proposalImage(final IEObjectDescription candidate, final String kind,
			final ContentAssistContext context) {
		final EObject definition = EcoreUtil.resolve(candidate.getEObjectOrProxy(), context.getResource());
		if (definition instanceof Statement) return provider.getImage(definition);
		if (definition instanceof ArgumentDefinition argument) {
			return imageHelper.getImage(provider.typeImage(EGaml.getInstance().getKeyOf(argument.getType())));
		}
		final String type = proposalType(candidate, definition);
		if (type != null) return imageHelper.getImage(provider.memberImage(type, IKeyword.ACTION.equals(kind)));
		final String key = switch (kind) {
			case IKeyword.TYPE -> candidate.getName().toString();
			case "variable", "field", "unit", "other" -> IKeyword.VAR;
			case "constant" -> IKeyword.CONST;
			case "operator" -> IKeyword.ACTION;
			default -> kind;
		};
		return imageHelper.getImage(provider.typeImage(key));
	}

	/**
	 * @see org.eclipse.xtext.ui.editor.contentassist.AbstractContentProposalProvider#doCreateProposal(java.lang.String,
	 *      org.eclipse.jface.viewers.StyledString, org.eclipse.swt.graphics.Image, int,
	 *      org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext)
	 */
	@Override
	protected ConfigurableCompletionProposal doCreateProposal(final String proposal, final StyledString displayString,
			final Image image, final int priority, final ContentAssistContext context) {
		return super.doCreateProposal(proposal, displayString, image, priority, context);
	}

	/**
	 * @param context
	 * @param acceptor
	 *
	 *            TODO Filter the proposals (passing an argument ?) depending on the context in the dispatcher (see
	 *            commented methods below). TODO Build this list at once instead of recomputing it everytime (might be
	 *            done in a dedicated data structure somewhere) and separate it by types (vars, units, etc.)
	 */
	private void addBuiltInElements(final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
		if (proposals.isEmpty()) {
			// for ( String t : Types.getTypeNames() ) {
			// types.add(t);
			// Image image = imageHelper.getImage(provider.typeImage(t));
			// if ( image == null ) {
			// image = image =
			// imageHelper.getImage(provider.typeImage("gaml_facet.png"));
			// }
			// BuiltInProposal cp = new BuiltInProposal(t, new StyledString(t +
			// " (Built-in type)"), image);
			// proposals.add(cp);
			// }

			// for ( String t : AbstractGamlAdditions.CONSTANTS ) {
			// constants.add(t);
			// BuiltInProposal cp = new BuiltInProposal(t, new StyledString(t +
			// " (Built-in constant)"), null);
			// proposals.add(cp);
			// }
			// for ( String t : IUnits.UNITS.keySet() ) {
			// units.add(t);
			// BuiltInProposal cp = new BuiltInProposal(t, new StyledString(t +
			// " (Built-in unit)"), null);
			// proposals.add(cp);
			// }
			for (final String name : GAML.getOperatorsNames()) {
				proposals.add(new BuiltInProposal(name, new StyledString(name + " (Operator)"),
						imageHelper.getImage(provider.typeImage(IKeyword.ACTION)), "operator"));
			}
			for (final String t : ArtefactRegistry.getStatementArtefactNames()) {
				final IArtefact s = ArtefactRegistry.getArtefact(t, null);
				statements.add(t);
				final String title = " (Statement)";
				final BuiltInProposal cp = new BuiltInProposal(t, new StyledString(t + title),
						imageHelper.getImage(provider.typeImage(t)), "statement");
				proposals.add(cp);
				cp.setDoc(s instanceof IArtefact.Symbol symbol
						? symbol.getDocumentation(t).toString() : s.getDocumentation().toString());
			}

			for (final String t : Types.getTypeNames()) {
				final IArtefact s = ArtefactRegistry.getVarArtefact(t, null);
				statements.add(t);
				final String title = " (Declaration)";
				final BuiltInProposal cp = new BuiltInProposal(t, new StyledString(t + title),
						imageHelper.getImage(provider.typeImage(t)), "declaration");
				proposals.add(cp);
				cp.setDoc(s.getDocumentation().toString());

			}
			// for ( String t : AbstractGamlAdditions.getAllSkills() ) {
			// skills.add(t);
			// BuiltInProposal cp = new BuiltInProposal(t, new StyledString(t +
			// ": (Built-in skill)"), skillImage);
			// proposals.add(cp);
			// }

		}
		for (final BuiltInProposal bi : proposals) {
			final ICompletionProposal cp = createCompletionProposal(bi.name, bi.title, bi.image,
					proposalPriority(bi.kind), context.getPrefix(), context);
			if (cp == null) {
				// scope.getGui().debug("GamlProposalProvider.addBuiltInElements
				// null for " + t);
			} else {
				((ConfigurableCompletionProposal) cp).setPriority(proposalPriority(bi.kind));
				if ("operator".equals(bi.kind) && cp instanceof GamlCompletionProposal completion) {
					completion.documentation = Suppliers.memoize(() -> operatorDocumentation(bi.name));
				}
				if (bi.documentation != null) {
					((ConfigurableCompletionProposal) cp).setAdditionalProposalInfo(bi.documentation);
				}
				acceptor.accept(cp);
			}
		}

	}

	@Override
	public void completeClassicFacet_Key(final EObject model, final Assignment assignment,
			final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
		super.completeClassicFacet_Key(model, assignment, context, acceptor);
		proposeFacets(model, context, acceptor);
	}

	@Override
	public void completeDefinitionFacet_Key(final EObject model, final Assignment assignment,
			final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
		super.completeDefinitionFacet_Key(model, assignment, context, acceptor);
		proposeFacets(model, context, acceptor);
	}

	private void proposeFacets(final EObject model, final ContentAssistContext context,
			final ICompletionProposalAcceptor acceptor) {
		final Statement statement = EGaml.getInstance().getSurroundingStatement(model);
		if (statement == null) return;
		String key = EGaml.getInstance().getKeyOf(statement);
		final Statement parent = EGaml.getInstance().getSurroundingStatement(statement.eContainer());
		if (parent instanceof gaml.compiler.gaml.S_Display) {
			if (IKeyword.SPECIES.equals(key)) key = IKeyword.SPECIES_LAYER;
			else if (IKeyword.GRID.equals(key)) key = IKeyword.GRID_LAYER;
		}
		final String keyword = key;
		final IArtefact.Symbol symbol = ArtefactRegistry.getArtefact(keyword, null);
		if (symbol == null) return;
		final Set<String> present = new HashSet<>();
		for (final Facet facet : statement.getFacets()) {
			if (facet != model) {
				String name = EGaml.getInstance().getKeyOf(facet);
				if ("<-".equals(name)) name = IKeyword.INIT;
				else if ("->".equals(name)) name = IKeyword.FUNCTION;
				present.add(name);
			}
		}
		symbol.getPossibleFacets(keyword).forEach((name, facet) -> {
			if (facet.isInternal() || present.contains(name)
					|| facet.getExcludes().stream().anyMatch(present::contains)
					|| present.stream().map(symbol.getPossibleFacets()::get)
							.anyMatch(peer -> peer != null && peer.getExcludes().contains(name))) return;
			final ICompletionProposal proposal = createCompletionProposal(name + ": ",
					new StyledString(name + " (Facet)"), null, 1100, context.getPrefix(), context);
			if (proposal instanceof ConfigurableCompletionProposal configurable) {
				configurable.setAdditionalProposalInfo(facet.getDocumentation().toString());
				acceptor.accept(proposal);
			}
		});
	}

	/**
	 * @see org.eclipse.xtext.ui.editor.contentassist.AbstractContentProposalProvider#doCreateProposal(java.lang.String,
	 *      org.eclipse.jface.viewers.StyledString, org.eclipse.swt.graphics.Image, int, int)
	 */
	@Override
	protected ConfigurableCompletionProposal doCreateProposal(final String proposal, final StyledString displayString,
			final Image image, final int replacementOffset, final int replacementLength) {
		return new GamlCompletionProposal(proposal, replacementOffset, replacementLength, proposal.length(), image,
				displayString, null, null);
	}

	@Override
	protected Function<IEObjectDescription, ICompletionProposal> getProposalFactory(final String ruleName,
			final ContentAssistContext contentAssistContext) {
		return new GamlProposalCreator(contentAssistContext, ruleName, getQualifiedNameConverter());
	}

	@Override
	protected boolean isValidProposal(final String proposal, final String prefix, final ContentAssistContext context) {
		if (".".equals(prefix))
			return !types.contains(proposal) && !units.contains(proposal) && !constants.contains(proposal)
					&& !skills.contains(proposal) && isValidProposal(proposal, "", context);
		return super.isValidProposal(proposal, prefix, context);
	}

}
