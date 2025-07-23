/*******************************************************************************************************
 *
 * DataTypeFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.factories;

import org.eclipse.emf.ecore.EObject;

import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.DataDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.statements.Facets;

/**
 * Factory for creating data_type descriptions and registering them as types in the GAMA type system.
 * This allows user-defined data_type declarations to be recognized as types throughout the model.
 *
 * @author Generated to fix data_type registration issue
 * @since 2025
 */
@SuppressWarnings({ "rawtypes" })
public class DataTypeFactory extends SymbolFactory {

    @Override
    protected TypeDescription buildDescription(final String keyword, final Facets facets, final EObject element,
            final Iterable<IDescription> children, final IDescription sd, final SymbolProto proto) {
        
        // Get the name of the data type from facets
        String typeName = facets.getLabel(IKeyword.NAME);
        
        // Register the data type name as a type in the GAMA type system
        // This is the key step that makes the new type available throughout the model
        if (typeName != null && !typeName.isEmpty()) {
            DescriptionFactory.addSpeciesNameAsType(typeName);
        }
        
        // Create a DataDescription for the data type using your existing DataDescription class
        return new DataDescription(keyword, null, sd, null, children, element, facets, "user.defined");
    }
}