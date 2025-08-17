/*******************************************************************************************************
 *
 * GamlProposalProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.contentassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.ConfigurableCompletionProposal;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;

import com.google.common.base.Function;
import com.google.inject.Inject;

import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.factories.DescriptionFactory;
import gaml.compiler.services.GamlGrammarAccess;
import gaml.compiler.ui.labeling.GamlLabelProvider;

/**
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on how to customize content assistant
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlProposalProvider extends AbstractGamlProposalProvider {

	// private static Set<String> typeList;
	// private static GamlProperties allowedFacets;
	// private static Image rgbImage =
	// ImageDescriptor.createFromFile(GamlProposalProvider.class,
	// "/icons/_rgb.png")
	// .createImage();
	// private static Image facetImage =
	// ImageDescriptor.createFromFile(GamlProposalProvider.class,
	// "/icons/_facet.png")
	/** The type image. */
	// .createImage();
	static Image typeImage =
			ImageDescriptor.createFromFile(GamlProposalProvider.class, "/icons/_type.png").createImage();

	/** The var image. */
	static Image varImage = ImageDescriptor.createFromFile(GamlProposalProvider.class, "/icons/_var.png").createImage();

	/** The action image. */
	static Image actionImage =
			ImageDescriptor.createFromFile(GamlProposalProvider.class, "/icons/_action.png").createImage();

	// private static Image skillImage =
	// ImageDescriptor.createFromFile(GamlProposalProvider.class,
	// "/icons/_skills.png")
	// .createImage();

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
			String doc = candidate.getUserData("doc");
			final String title = candidate.getUserData("title");
			if (doc == null) { doc = "Not documented yet"; }
			if (cp != null) {
				cp.setAdditionalProposalInfo("<b>" + title + "</b><p/><p>" + doc + "</p>");

				final String type = candidate.getUserData(IKeyword.TYPE);
				if (type != null) {
					cp.setDisplayString(cp.getDisplayString().concat(" (Built-in " + type + ") "));
					switch (type) {
						case "operator":
							isOperator = true;
							cp.setImage(actionImage);
							break;
						case "variable":
						case "field":
							cp.setImage(varImage);
							break;
						case IKeyword.ACTION:
							cp.setImage(actionImage);
							break;
						case "unit":
							isOperator = true;
							cp.setImage(null);
							break;
						case IKeyword.TYPE:
							isOperator = true;
							cp.setImage(typeImage);
							break;
						case null:
						default:
							break;
					}
					cp.setPriority(1000);
				}
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
			return parent -> new DefaultInformationControl(parent, true);
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

		/**
		 * Instantiates a new built in proposal.
		 *
		 * @param name
		 *            the name
		 * @param title
		 *            the title
		 * @param image
		 *            the image
		 */
		public BuiltInProposal(final String name, final StyledString title, final Image image) {
			this.name = name;
			this.title = title;
			this.image = image;
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

	// @Inject
	// private IImageHelper imageHelper;
	//
	// @Inject
	// private GamlJavaValidator validator;

	/** The ga. */
	@Inject private GamlGrammarAccess ga;

	@Override
	public void createProposals(final ContentAssistContext context, final ICompletionProposalAcceptor acceptor) {
		// Disabling for comments (see Issue 786)
		final EObject grammarElement = context.getCurrentNode().getGrammarElement();
		if (grammarElement == ga.getML_COMMENTRule() || grammarElement == ga.getSL_COMMENTRule()) return;
		//
		addBuiltInElements(context, acceptor);
		super.createProposals(context, acceptor);

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
			for (final String t : DescriptionFactory.getStatementProtoNames()) {
				final SymbolProto s = DescriptionFactory.getProto(t, null);
				statements.add(t);
				final String title = " (Statement)";
				final BuiltInProposal cp = new BuiltInProposal(t, new StyledString(t + title), null);
				proposals.add(cp);
				cp.setDoc(s.getDocumentation().toString());
			}

			for (final String t : DescriptionFactory.getVarProtoNames()) {
				final SymbolProto s = DescriptionFactory.getVarProto(t, null);
				statements.add(t);
				final String title = " (Declaration)";
				final BuiltInProposal cp = new BuiltInProposal(t, new StyledString(t + title), null);
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
			final ICompletionProposal cp =
					createCompletionProposal(bi.name, bi.title, bi.image, 1000, context.getPrefix(), context);
			if (cp == null) {
				// scope.getGui().debug("GamlProposalProvider.addBuiltInElements
				// null for " + t);
			} else {
				if (bi.documentation != null) {
					((ConfigurableCompletionProposal) cp).setAdditionalProposalInfo(bi.documentation);
				}
				acceptor.accept(cp);
			}
		}

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
