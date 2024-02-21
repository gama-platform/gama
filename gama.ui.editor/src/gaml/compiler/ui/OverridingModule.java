/*******************************************************************************************************
 *
 * OverridingModule.java, in gama.ui.shared.modeling, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui;

import org.eclipse.xtext.builder.builderState.IMarkerUpdater;
import org.eclipse.xtext.builder.clustering.ClusteringBuilderState;
import org.eclipse.xtext.builder.resourceloader.IResourceLoader;
import org.eclipse.xtext.builder.resourceloader.ResourceLoaderProviders;
import org.eclipse.xtext.service.AbstractGenericModule;

import com.google.inject.Binder;
import com.google.inject.name.Names;

import gaml.compiler.ui.decorators.GamlMarkerUpdater;

/**
 * The class OverridingModule.
 *
 * @author drogoul
 * @since 11 sept. 2016
 *
 */
public class OverridingModule extends AbstractGenericModule {

	@Override
	public void configure(final Binder binder) {
		super.configure(binder);
		binder.bind(IResourceLoader.class)
				.annotatedWith(Names.named(ClusteringBuilderState.RESOURCELOADER_GLOBAL_INDEX))
				.toProvider(ResourceLoaderProviders.getParallelLoader(8, 8));

		binder.bind(IResourceLoader.class)
				.annotatedWith(Names.named(ClusteringBuilderState.RESOURCELOADER_CROSS_LINKING))
				.toProvider(ResourceLoaderProviders.getParallelLoader(8, 8));

		binder.bind(IMarkerUpdater.class).to(GamlMarkerUpdater.class);
	}

}
