/*******************************************************************************************************
 *
 * GamlDescriptionUtils.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml;

import org.eclipse.emf.ecore.EAttribute;

/**
 * Utility class to handle getName/setName operations for GamlDefinition objects.
 * This class provides static methods to work around the Xtext generation issue
 * where some GamlDefinition classes are missing proper getName/setName methods.
 * 
 * @author Generated to fix Xtext interface generation bug
 * @since 2025
 */
public class GamlDefinitionUtils {

	
	public static EAttribute getDefinitionNameAttribute(GamlDefinition definition) {
		if(definition instanceof VarDefinition) {
			return GamlPackage.Literals.VAR_DEFINITION__NAME;
		} else if(definition instanceof EquationDefinition) {
			return GamlPackage.Literals.EQUATION_DEFINITION__NAME;
		} else if(definition instanceof UnitFakeDefinition) {
			return GamlPackage.Literals.UNIT_FAKE_DEFINITION__NAME;
		} else if(definition instanceof SkillFakeDefinition) {
			return GamlPackage.Literals.SKILL_FAKE_DEFINITION__NAME;
		} else if(definition instanceof TypeFakeDefinition) {
			return GamlPackage.Literals.TYPE_FAKE_DEFINITION__NAME;
		} else if(definition instanceof ActionFakeDefinition) {
			return GamlPackage.Literals.ACTION_FAKE_DEFINITION__NAME;
		} 
//		else if(definition instanceof EquationFakeDefinition) {
//			return GamlPackage.EQUATION_FAKE_DEFINITION__NAME;
//		} else if(definition instanceof VarFakeDefinition) {
//			return GamlPackage.;
//		}
		return null; // Return null if no matching type found
	}
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private GamlDefinitionUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Gets the name from a GamlDefinition object by dispatching to the appropriate concrete type.
     * 
     * @param definition the GamlDefinition object
     * @return the name string, or null if the object is null or type is not supported
     */
    public static String getName(GamlDefinition definition) {
        if (definition == null) {
            return null;
        }
        
        // Handle VarDefinition (has getName method)
        if (definition instanceof VarDefinition) {
            return ((VarDefinition) definition).getName();
        }
        
        // Handle EquationDefinition (has getName method)
        if (definition instanceof EquationDefinition) {
            return ((EquationDefinition) definition).getName();
        }
        
        // Handle UnitFakeDefinition (has getName method)
        if (definition instanceof UnitFakeDefinition) {
            return ((UnitFakeDefinition) definition).getName();
        }
        
        // Handle SkillFakeDefinition (has getName method)  
        if (definition instanceof SkillFakeDefinition) {
            return ((SkillFakeDefinition) definition).getName();
        }
        
        // Handle TypeFakeDefinition (has getName method)
        if (definition instanceof TypeFakeDefinition) {
            return ((TypeFakeDefinition) definition).getName();
        }
        
        // Handle ActionFakeDefinition (has getName method)
        if (definition instanceof ActionFakeDefinition) {
            return ((ActionFakeDefinition) definition).getName();
        }
        
        // Handle EquationFakeDefinition (has getName method)
        if (definition instanceof EquationFakeDefinition) {
            return ((EquationFakeDefinition) definition).getName();
        }
        
        // Handle VarFakeDefinition (has getName method)
        if (definition instanceof VarFakeDefinition) {
            return ((VarFakeDefinition) definition).getName();
        }
        
        // For classes without getName method (like TypeDefinition, ActionDefinition), use reflection
        try {
            return (String) definition.getClass().getMethod("getName").invoke(definition);
        } catch (Exception e) {
            // If reflection fails, return null
            return null;
        }
    }

    /**
     * Sets the name on a GamlDefinition object by dispatching to the appropriate concrete type.
     * 
     * @param definition the GamlDefinition object
     * @param name the name to set
     * @return true if the name was successfully set, false otherwise
     */
    public static boolean setName(GamlDefinition definition, String name) {
        if (definition == null) {
            return false;
        }
        
        try {
            // Handle VarDefinition (has setName method)
            if (definition instanceof VarDefinition) {
                ((VarDefinition) definition).setName(name);
                return true;
            }
            
            // Handle EquationDefinition (has setName method)
            if (definition instanceof EquationDefinition) {
                ((EquationDefinition) definition).setName(name);
                return true;
            }
            
            // Handle UnitFakeDefinition (has setName method)
            if (definition instanceof UnitFakeDefinition) {
                ((UnitFakeDefinition) definition).setName(name);
                return true;
            }
            
            // Handle SkillFakeDefinition (has setName method)
            if (definition instanceof SkillFakeDefinition) {
                ((SkillFakeDefinition) definition).setName(name);
                return true;
            }
            
            // Handle TypeFakeDefinition (has setName method)
            if (definition instanceof TypeFakeDefinition) {
                ((TypeFakeDefinition) definition).setName(name);
                return true;
            }
            
            // Handle ActionFakeDefinition (has setName method)
            if (definition instanceof ActionFakeDefinition) {
                ((ActionFakeDefinition) definition).setName(name);
                return true;
            }
            
            // Handle EquationFakeDefinition (has setName method)
            if (definition instanceof EquationFakeDefinition) {
                ((EquationFakeDefinition) definition).setName(name);
                return true;
            }
            
            // Handle VarFakeDefinition (has setName method)
            if (definition instanceof VarFakeDefinition) {
                ((VarFakeDefinition) definition).setName(name);
                return true;
            }
            
            // For classes without setName method (like TypeDefinition, ActionDefinition), use reflection
            definition.getClass().getMethod("setName", String.class).invoke(definition, name);
            return true;
            
        } catch (Exception e) {
            // If any operation fails, return false
            return false;
        }
    }

    /**
     * Checks if a GamlDefinition object has a name (i.e., getName() returns non-null and non-empty).
     * 
     * @param definition the GamlDefinition object
     * @return true if the object has a name, false otherwise
     */
    public static boolean hasName(GamlDefinition definition) {
        String name = getName(definition);
        return name != null && !name.trim().isEmpty();
    }

    /**
     * Gets the name from a GamlDefinition object, returning a default value if the name is null or empty.
     * 
     * @param definition the GamlDefinition object
     * @param defaultName the default name to return if no name is found
     * @return the name string, or the default name if no name is found
     */
    public static String getNameOrDefault(GamlDefinition definition, String defaultName) {
        String name = getName(definition);
        return (name != null && !name.trim().isEmpty()) ? name : defaultName;
    }

    /**
     * Gets the simple class name of the GamlDefinition object for debugging purposes.
     * 
     * @param definition the GamlDefinition object
     * @return the simple class name, or "null" if the object is null
     */
    public static String getTypeName(GamlDefinition definition) {
        return definition != null ? definition.getClass().getSimpleName() : "null";
    }
}