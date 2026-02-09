/*******************************************************************************************************
 *
 * NowUnitExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import java.time.LocalDateTime;

import gama.api.data.factories.GamaDateFactory;
import gama.api.data.objects.IDate;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;

/**
 * The Class NowUnitExpression.
 */
public class NowUnitExpression extends UnitConstantExpression {

	/**
	 * Instantiates a new now unit expression.
	 *
	 * @param name
	 *            the name
	 * @param doc
	 *            the doc
	 */
	public NowUnitExpression(final String name, final String doc) {
		super(1.0, Types.DATE, name, doc, null);
	}

	@Override
	public IDate _value(final IScope scope) {
		return GamaDateFactory.createFromTemporal(LocalDateTime.now());
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public boolean isAllowedInParameters() { return false; }

}
