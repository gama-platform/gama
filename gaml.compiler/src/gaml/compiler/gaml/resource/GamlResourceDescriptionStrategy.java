/*******************************************************************************************************
 *
 * GamlResourceDescriptionStrategy.java, in gaml.compiler.gaml, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.gaml.resource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.util.IAcceptor;

import gaml.compiler.gaml.ActionArguments;
import gaml.compiler.gaml.ArgumentDefinition;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.Statement;

/**
 * The class GamlResourceDescriptionManager.
 *
 * @author drogoul
 * @since 19 avr. 2012
 *
 */
public class GamlResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {

	/**
	 * Creates the description.
	 *
	 * @param o the o
	 * @param acceptor the acceptor
	 */
	private void createDescription(final EObject o, final IAcceptor<IEObjectDescription> acceptor) {
		final QualifiedName qn = getQualifiedNameProvider().getFullyQualifiedName(o);
		if (qn != null) {
			acceptor.accept(EObjectDescription.create(qn, o));
		}
	}

	@Override
	public boolean createEObjectDescriptions(final EObject o, final IAcceptor<IEObjectDescription> acceptor) {
		if (o instanceof ActionArguments || o instanceof Block || o instanceof Model) { return true; }
		if (o instanceof Statement) {
			createDescription(o, acceptor);
			return true;
		}
		if (o instanceof ArgumentDefinition) {
			createDescription(o, acceptor);
		}
		return false;
	}

	@Override
	protected boolean isResolvedAndExternal(final EObject from, final EObject to) {
		if (to == null) { return false; }
		if (!to.eIsProxy()) {
			final Resource toR = to.eResource();
			return toR != null && toR != from.eResource();
		}
		return !getLazyURIEncoder().isCrossLinkFragment(from.eResource(),
				((InternalEObject) to).eProxyURI().fragment());
	}

}
