package gaml.grammar.transition;

/**
 * A functional interface for GAML source file content transformers.
 *
 * <p>Each implementation encapsulates one specific syntactic transformation
 * (e.g. replacing {@code diffuse var:} with {@code diffuse}) and can be
 * applied in sequence by {@link GamlFileProcessor}.</p>
 *
 * <p>Implementations must be stateless and idempotent: applying the same
 * transformer twice to the same content must produce the same result as
 * applying it once.</p>
 *
 * @see GamlFileProcessor
 */
@FunctionalInterface
public interface IFileTransformer {

	/**
	 * Apply this transformation to the given source content.
	 *
	 * @param content
	 *            the full text content of a GAML source file; never {@code null}
	 * @return the transformed content; never {@code null}. If no change was
	 *         needed, the original {@code content} string must be returned
	 *         unchanged (identity comparison is used by {@link GamlFileProcessor}
	 *         to detect whether a file was modified).
	 */
	String transform(String content);
}
