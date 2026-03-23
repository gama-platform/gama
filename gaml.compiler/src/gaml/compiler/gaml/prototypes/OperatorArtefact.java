/*******************************************************************************************************
 *
 * OperatorArtefact.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.prototypes;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import gama.annotations.doc;
import gama.annotations.operator;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.ISymbolKind;
import gama.annotations.support.ITypeProvider;
import gama.api.additions.GamaBundleLoader;
import gama.api.additions.IGamaGetter;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.annotations.depends_on;
import gama.api.annotations.validator;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.validation.IValidator;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;
import gama.api.utils.collections.ICollector;
import gama.dev.DEBUG;

/**
 * Class OperatorArtefact.
 *
 *
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 9 déc. 2023
 */
@SuppressWarnings ({ "rawtypes" })
public class OperatorArtefact extends AbstractArtefact implements IArtefact.Operator {

	/** The Constant EMPTY_DEPS. */
	public static final String[] EMPTY_DEPS = {};

	/** The as. */
	public static OperatorArtefact AS;

	/** The iterator. */
	public final boolean isVarOrField, canBeConst, iterator;

	/** The semantic validator. */
	private IValidator semanticValidator;

	/** The return type. */
	public final IType returnType;

	/** The helper. */
	private final IGamaGetter helper;

	/** The signature. */
	public final Signature signature;

	/** The lazy. */
	public boolean[] lazy;

	/** The key type provider. */
	public final int typeProvider, contentTypeProvider, keyTypeProvider, contentTypeContentTypeProvider;

	/** The expected content type. */
	public final int[] expectedContentType;

	/** The depends on. */
	private String[] depends_on;

	/**
	 * Instantiates a new operator artefact.
	 *
	 * @param name
	 *            the name
	 * @param method
	 *            the method
	 * @param helper
	 *            the helper
	 * @param canBeConst
	 *            the can be const
	 * @param isVarOrField
	 *            the is var or field
	 * @param returnType
	 *            the return type
	 * @param signature
	 *            the signature
	 * @param typeProvider
	 *            the type provider
	 * @param contentTypeProvider
	 *            the content type provider
	 * @param keyTypeProvider
	 *            the key type provider
	 * @param contentTypeContentTypeProvider
	 *            the content type content type provider
	 * @param expectedContentType
	 *            the expected content type
	 * @param plugin
	 *            the plugin
	 */
	public OperatorArtefact(final String name, final AnnotatedElement method, final String constantDoc,
			final IGamaGetter helper, final boolean canBeConst, final boolean isVarOrField, final IType returnType,
			final Signature signature, final int typeProvider, final int contentTypeProvider, final int keyTypeProvider,
			final int contentTypeContentTypeProvider, final int[] expectedContentType, final String plugin) {
		super(name, method, plugin);
		iterator = GAML.isIterator(name);
		if (constantDoc != null) { documentation = new GamlConstantDocumentation(constantDoc); }
		if (IKeyword.AS.equals(name)) { AS = this; }
		this.returnType = returnType;
		this.canBeConst = canBeConst;
		this.isVarOrField = isVarOrField;
		this.helper = helper;
		this.signature = signature;
		this.typeProvider = typeProvider;
		this.contentTypeProvider = contentTypeProvider;
		this.keyTypeProvider = keyTypeProvider;
		this.expectedContentType = expectedContentType;
		this.contentTypeContentTypeProvider = contentTypeContentTypeProvider;
	}

	/**
	 * Instantiates a new operator artefact.
	 *
	 * @param name
	 *            the name
	 * @param method
	 *            the method
	 * @param helper
	 *            the helper
	 * @param canBeConst
	 *            the can be const
	 * @param isVarOrField
	 *            the is var or field
	 * @param returnType
	 *            the return type
	 * @param signature
	 *            the signature
	 * @param typeProvider
	 *            the type provider
	 * @param contentTypeProvider
	 *            the content type provider
	 * @param keyTypeProvider
	 *            the key type provider
	 * @param expectedContentType
	 *            the expected content type
	 */
	public OperatorArtefact(final String name, final AnnotatedElement method, final IGamaGetter helper,
			final boolean canBeConst, final boolean isVarOrField, final int returnType, final Class signature,
			final int typeProvider, final int contentTypeProvider, final int keyTypeProvider,
			final int[] expectedContentType) {
		this(name, method == null ? signature : method, null, helper, canBeConst, isVarOrField, Types.get(returnType),
				new Signature(signature), typeProvider, contentTypeProvider, keyTypeProvider, ITypeProvider.NONE,
				expectedContentType, GamaBundleLoader.CURRENT_PLUGIN_NAME);
	}

	/**
	 * Compute lazyness.
	 *
	 * @param method
	 *            the method
	 * @return the boolean[]
	 */
	@Override
	public boolean[] getLazyness() {
		if (lazy == null) {
			lazy = new boolean[signature.size()];
			if (lazy.length == 0) return lazy;
			if (support instanceof Method m) {
				final Class[] classes = m.getParameterTypes();
				if (classes.length == 0) return lazy;
				int begin = 0;
				if (classes[0] == IScope.class) { begin = 1; }
				for (int i = begin; i < classes.length; i++) {
					if (IExpression.class.isAssignableFrom(classes[i])) { lazy[i - begin] = true; }
				}
			}
		}

		return lazy;
	}

	/**
	 * Instantiates a new operator artefact.
	 *
	 * @param op
	 *            the op
	 * @param gamaType
	 *            the gama type
	 */
	private OperatorArtefact(final OperatorArtefact op, final IType gamaType) {
		this(op.name, op.support, null, op.getHelper(), op.canBeConst, op.isVarOrField, op.returnType,
				new Signature(gamaType), op.typeProvider, op.contentTypeProvider, op.keyTypeProvider,
				op.contentTypeContentTypeProvider, op.expectedContentType, op.plugin);
	}

	@Override
	public String getTitle() {
		if (isVarOrField) return "field " + getName() + " of type " + returnType + ", for values of type "
				+ signature.asPattern(false);
		return "operator " + getName() + "(" + signature.asPattern(false) + "), returns " + documentReturnType();
	}

	/**
	 * Document return type.
	 *
	 * @return the string
	 */
	public String documentReturnType() {
		IType result = returnType;
		if (!result.isContainer()) return result.toString();
		IType keyType = keyTypeProvider > 0 ? Types.get(keyTypeProvider) : Types.NO_TYPE;
		IType contentType = contentTypeProvider > 0 ? Types.get(contentTypeProvider) : Types.NO_TYPE;
		return GamaType.from(result, keyType, contentType).toString();
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		if (!isVarOrField) return super.getDocumentation();
		final vars annot = getJavaBase().getAnnotation(vars.class);
		if (annot != null) {
			final variable[] allVars = annot.value();
			for (final variable v : allVars) {
				if (v.name().equals(getName())) {
					if (v.doc().length > 0) return new GamlConstantDocumentation(v.doc()[0].value());
					break;
				}
			}
		}
		return new GamlConstantDocumentation(getTitle());
	}

	/**
	 * Verify expected types.
	 *
	 * @param context
	 *            the context
	 * @param rightType
	 *            the right type
	 */
	@Override
	public void verifyExpectedTypes(final IDescription context, final IType<?> rightType) {
		if (expectedContentType == null || expectedContentType.length == 0 || context == null) return;
		if (expectedContentType.length == 1 && iterator) {
			final IType<?> expected = Types.get(expectedContentType[0]);
			if (!rightType.isTranslatableInto(expected)) {
				context.warning("Operator " + getName() + " expects an argument of type " + expected,
						IGamlIssue.SHOULD_CAST);
			}
		} else if (signature.isUnary()) {
			for (final int element : expectedContentType) {
				if (rightType.isTranslatableInto(Types.get(element))) return;
			}
			context.error("Operator " + getName() + " expects arguments of type " + rightType, IGamlIssue.WRONG_TYPE);
		}
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName() + "(" + signature.toString() + ")";
	}

	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	@Override
	public String getCategory() {
		if (support == null) return "Other";
		final operator op = support.getAnnotation(operator.class);
		if (op == null) return "Other";
		final String[] strings = op.category();
		if (strings.length > 0) return op.category()[0];
		return "Other";
	}

	/**
	 * Method getKind()
	 *
	 * @see gaml.compiler.gaml.prototypes.AbstractArtefact#getKind()
	 */
	@Override
	public ISymbolKind getKind() { return ISymbolKind.OPERATOR; }

	/**
	 * @return
	 */
	@Override
	public String getPattern(final boolean withVariables) {
		final int size = signature.size();
		final String aName = getName();
		if (size == 1 || size > 2) {
			if (ArtefactRegistry.ARTEFACTS_WITHOUT_PARENTHESES.contains(aName))
				return aName + signature.asPattern(withVariables);
			return aName + "(" + signature.asPattern(withVariables) + ")";
		}
		if (ArtefactRegistry.BINARY_ARTEFACTS_NAMES.contains(aName))
			return signature.get(0).asPattern() + " " + aName + " " + signature.get(1).asPattern();
		return aName + "(" + signature.asPattern(withVariables) + ")";
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		super.collectMetaInformation(meta);
		meta.put(GamlProperties.OPERATORS, name);
	}

	/**
	 * Copy with signature.
	 *
	 * @param gamaType
	 *            the gama type
	 * @return the operator artefact
	 */
	@Override
	public OperatorArtefact copyWithSignature(final IType gamaType) {
		return new OperatorArtefact(this, gamaType);
	}

	@Override
	public void collectUsedVarsOf(final ITypeDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		String[] deps = getDependencies();
		if (deps != null) {
			for (final String s : deps) { if (species.hasAttribute(s)) { result.add(species.getAttribute(s)); } }
		}
	}

	@Override
	public doc getDocAnnotation() {
		doc d = super.getDocAnnotation();
		if (d != null) return d;
		if (support != null && support.isAnnotationPresent(operator.class)) {
			final operator op = support.getAnnotation(operator.class);
			final doc[] docs = op.doc();
			if (docs != null && docs.length > 0) { d = docs[0]; }
		}
		return d;
	}

	/**
	 * Gets the helper.
	 *
	 * @return the helper
	 */
	@Override
	public IGamaGetter getHelper() { return helper; }

	/**
	 * Gets the validator.
	 *
	 * @return the validator
	 */
	public IValidator getValidator() {
		if (semanticValidator == null) {
			if (support != null) {
				final validator val = support.getAnnotation(validator.class);
				try {
					semanticValidator = val == null ? IValidator.NULL : val.value().getConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					DEBUG.ERR("Error in creating the validator for operator " + name + " on method " + support);
				}
			} else {
				semanticValidator = IValidator.NULL;
			}
		}
		return semanticValidator;
	}

	/**
	 * Gets the dependencies.
	 *
	 * @return the dependencies
	 */
	public String[] getDependencies() {
		if (depends_on == null) {
			if (support != null) {
				final depends_on d = support.getAnnotation(depends_on.class);
				depends_on = d == null ? EMPTY_DEPS : d.value();
			} else {
				depends_on = EMPTY_DEPS;
			}
		}
		return depends_on;
	}

	/**
	 * Gets the signature.
	 *
	 * @return the signature
	 */
	@Override
	public Signature getSignature() { return signature; }

	/**
	 * Gets the return type.
	 *
	 * @return the return type
	 */
	@Override
	public IType<?> getReturnType() { return returnType; }

	/**
	 * @return
	 */
	public boolean isVarOrField() { return isVarOrField; }

	@Override
	public boolean canBeConst() {
		return canBeConst;
	}

	@Override
	public boolean isIterator() { return iterator; }

	@Override
	public int getTypeProvider() { return typeProvider; }

	@Override
	public int getContentTypeContentTypeProvider() { return contentTypeContentTypeProvider; }

	@Override
	public int getContentTypeProvider() { return contentTypeProvider; }

	@Override
	public int getKeyTypeProvider() { return keyTypeProvider; }
}
