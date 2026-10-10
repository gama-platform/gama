/**
 *
 */
package gama.extension.serialize.fst;

/**
 *
 */
public interface FSTSerialisationConstants {

	/** The Constant SPECIAL_COMPATIBILITY_OBJECT_TAG. */
	byte SPECIAL_COMPATIBILITY_OBJECT_TAG = -19; // see issue 52
	/** The Constant ONE_OF. */
	byte ONE_OF = -18;
	/** The Constant BIG_BOOLEAN_FALSE. */
	byte BIG_BOOLEAN_FALSE = -17;
	/** The Constant BIG_BOOLEAN_TRUE. */
	byte BIG_BOOLEAN_TRUE = -16;
	/** The Constant BIG_LONG. */
	byte BIG_LONG = -10;
	/** The Constant BIG_INT. */
	byte BIG_INT = -9;
	/** The Constant DIRECT_ARRAY_OBJECT. */
	byte DIRECT_ARRAY_OBJECT = -8;
	/** The Constant HANDLE. */
	byte HANDLE = -7;
	/** The Constant ENUM. */
	byte ENUM = -6;
	/** The Constant ARRAY. */
	byte ARRAY = -5;
	/** The Constant STRING. */
	byte STRING = -4;
	/** The Constant TYPED. */
	byte TYPED = -3; // var class == object written class
	/** The Constant DIRECT_OBJECT. */
	byte DIRECT_OBJECT = -2;
	/** The Constant NULL. */
	byte NULL = -1;
	/** The Constant OBJECT. */
	byte OBJECT = 0;

}
