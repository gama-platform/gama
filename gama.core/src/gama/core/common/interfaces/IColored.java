package gama.core.common.interfaces;

import java.util.List;

import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.core.util.GamaListFactory;
import gama.gaml.types.Types;

public interface IColored {

	GamaColor getColor(IScope scope);

	default List<GamaColor> getColors(final IScope scope) {
		return GamaListFactory.wrap(Types.COLOR, getColor(scope));
	}

}
