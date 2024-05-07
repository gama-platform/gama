/*******************************************************************************************************
 *
 * EnforcementStatement.java, in gama.extension.bdi, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.bdi;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;

/**
 * The Class EnforcementStatement.
 */
@symbol (
		name = EnforcementStatement.ENFORCEMENT,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.BDI })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = true,
				doc = @doc ("the identifier of the enforcement")),
				@facet (
						name = IKeyword.WHEN,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("A boolean value to enforce only with a certain condition")),
				@facet (
						name = EnforcementStatement.NORM,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The norm to enforce")),
				@facet (
						name = EnforcementStatement.OBLIGATION,
						type = PredicateType.id,
						optional = true,
						doc = @doc ("The obligation to enforce")),
				@facet (
						name = EnforcementStatement.LAW,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The law to enforce")),
				@facet (
						name = EnforcementStatement.SANCTION,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The sanction to apply if the norm is violated")),
				@facet (
						name = EnforcementStatement.REWARD,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The positive sanction to apply if the norm has been followed")) })
@doc (
		value = "apply a sanction if the norm specified is violated, or a reward if the norm is applied by the perceived agent",
		examples = {
				@example ("focus var:speed; //where speed is a variable from a species that is being perceived") })

// statement servant à controler les normes pour appliquer des sanctions, sur le modèle du focus
public class EnforcementStatement extends AbstractStatement {

	/** The Constant ENFORCEMENT. */
	public static final String ENFORCEMENT = "enforcement";
	
	/** The Constant NORM. */
	public static final String NORM = "norm";
	
	/** The Constant SANCTION. */
	public static final String SANCTION = "sanction";
	
	/** The Constant REWARD. */
	public static final String REWARD = "reward";
	
	/** The Constant OBLIGATION. */
	public static final String OBLIGATION = "obligation";
	
	/** The Constant LAW. */
	public static final String LAW = "law";

	/** The name expr. */
	final IExpression nameExpr;
	
	/** The when. */
	final IExpression when;
	
	/** The norm. */
	final IExpression norm;
	
	/** The sanction. */
	final IExpression sanction;
	
	/** The reward. */
	final IExpression reward;
	
	/** The obligation. */
	final IExpression obligation;
	
	/** The law. */
	final IExpression law;

	/**
	 * Instantiates a new enforcement statement.
	 *
	 * @param desc the desc
	 */
	public EnforcementStatement(final IDescription desc) {
		super(desc);
		nameExpr = getFacet(IKeyword.NAME);
		when = getFacet(IKeyword.WHEN);
		norm = getFacet(EnforcementStatement.NORM);
		sanction = getFacet(EnforcementStatement.SANCTION);
		reward = getFacet(EnforcementStatement.REWARD);
		obligation = getFacet(EnforcementStatement.OBLIGATION);
		law = getFacet(EnforcementStatement.LAW);
	}

	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		Object retour = null;
		if (when == null || Cast.asBool(scope, when.value(scope))) {
			final IAgent[] stack = scope.getAgentsStack();
			final IAgent mySelfAgent = stack[stack.length - 2];
			IScope scopeMySelf = null;
			if (mySelfAgent != null) {
				scopeMySelf = mySelfAgent.getScope().copy("in EnforcementStatement");
				scopeMySelf.push(mySelfAgent);
			}
			if (norm != null) {
				// getting the norm object from the name
				for (final Norm tempNorm : Utils.getNorms(scope)) {
					if (tempNorm.getName().equals(norm.value(scopeMySelf))) {
						//if the norm has been violated we try to apply the sanction
						//otherwise if the norm is respected we apply a reward 
						final Object toExecute;
						if (tempNorm.getViolated() && sanction != null) {
							toExecute = sanction.value(scopeMySelf);
						}
						else if (tempNorm.getApplied() && reward != null) {
							toExecute = reward.value(scopeMySelf);
						}
						else {
							continue;
						}
						// Both sanctions and rewards are stored in the sanction base
						var firstSanction = Utils.getSanctions(scopeMySelf).stream().filter(sanc -> toExecute.equals(sanc.getName())).findFirst();
						if (firstSanction.isPresent()) {
							// The sanction or reward are executed on the controller agent's context 
							// because they are indirect against a social norm
							retour = firstSanction.get().getSanctionStatement().executeOn(scopeMySelf);	
							break;
						}
					}
				}
			}
			
			if (obligation != null) {
				// on regarde si la base des obligations de l'autre est vide, si non, on regarde s'il a appliqué une
				// norme portant sur l'obligation à vérifiée.
				// Les sanctions et rewards seront ici appliqués dans le cadre de l'agent controlé car directe
				//TODO: last remark is not applied in the code, idk if the problem is the remark or the code
				final MentalState tempObligation = new MentalState("obligation", (Predicate) obligation.value(scope));
				if (Utils.hasObligation(scope, tempObligation)) {
					// si la norme en cours répond à l'obligation , reward, sinon punition.
					for (final Norm testNorm : Utils.getNorms(scope)) {
						if (tempObligation.getPredicate().equals(testNorm.getObligation(scope)) && !testNorm.getSanctioned()) {
							final Object toExecute;
							if (testNorm.getApplied() && reward != null) {
								toExecute = reward.value(scopeMySelf);
							}
							else if (/*testNorm.getViolated() && */ sanction != null) { 
								//commented getViolated according to comment above, but I'm not sure
								//TODO: if it has to be restored then we can refactor with the norm block above
								toExecute = sanction.value(scopeMySelf);
							}
							else {
								continue;
							}
							
							// Both sanctions and rewards are stored in the sanction base
							var firstSanction = Utils.getSanctions(scopeMySelf).stream().filter(sanc -> toExecute.equals(sanc.getName())).findFirst();
							if (firstSanction.isPresent()) {
								// The sanction or reward are executed on the controller agent's context 
								// because they are indirect against a social norm
								retour = firstSanction.get().getSanctionStatement().executeOn(scopeMySelf);	
								break;
							}

							testNorm.sanctioned();						
						}
					}
				}
			}
			// If there's a law we look up for it in the base and check if it's been violated
			if (law != null) {
				double obedienceValue = (double) scope.getAgent().getAttribute("obedience");
				for (final LawStatement tempLaw : Utils.getLaws(scope)) {
					if (tempLaw.getName().equals(law.value(scopeMySelf))) {
						retour = applySanctionOrReward(scope, scopeMySelf, tempLaw, obedienceValue);
						break;
					}
				}
				
			}
		
			GAMA.releaseScope(scopeMySelf);
		}
		return retour;
	}
	
	
	

	private Object applySanctionOrReward(IScope scope, IScope scopeMySelf, LawStatement tempLaw, double obedience) {
		
		
		boolean context = tempLaw.getContextExpression() == null || Cast.asBool(scope, tempLaw.getContextExpression().value(scope));
		boolean givenBeliefRegistered = tempLaw.getBeliefExpression() == null
									|| tempLaw.getBeliefExpression().value(scope) == null
									|| Utils.hasBelief(scope, new MentalState("Belief",(Predicate) tempLaw.getBeliefExpression().value(scope)));
		boolean givenObligationRegistered = tempLaw.getObligationExpression() == null
										|| tempLaw.getObligationExpression().value(scope) == null
										|| Utils.hasObligation(scope, new MentalState("Obligation", (Predicate) tempLaw.getObligationExpression().value(scope)));
		boolean thresholdRespected = tempLaw.getThreshold() == null || tempLaw.getThreshold().value(scope) == null
									|| obedience >= (double) tempLaw.getThreshold().value(scope);
		boolean preconditionsMet = context && givenBeliefRegistered && givenObligationRegistered && thresholdRespected;
		
		if 	(preconditionsMet) {
			
			if (reward != null) {
				for (final Sanction tempReward : Utils.getSanctions(scopeMySelf)) {
					if (tempReward.getName().equals(reward.value(scopeMySelf))) {
						return tempReward.getSanctionStatement().executeOn(scopeMySelf);
					}
				}
			}
		} 
		else if (sanction != null) {
			for (final Sanction tempSanction : Utils.getSanctions(scopeMySelf)) {
				if (tempSanction.getName().equals(sanction.value(scopeMySelf))) {
					return tempSanction.getSanctionStatement().executeOn(scopeMySelf);
				}
			}
		}
		return null;
	}

}
