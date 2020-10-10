/*
 *  This software may only be used by you under license from AT&T Corp.
 *  ("AT&T").  A copy of AT&T's Source Code Agreement is available at
 *  AT&T's Internet website having the URL:
 *
 *    <http://www.research.att.com/sw/tools/yoix/license/source.html>
 *
 *  If you received this software without first entering into a license
 *  with AT&T, you have an infringing copy of this software and cannot
 *  use it without violating AT&T's intellectual property rights.
 */

package att.research.yoix.jvma;
import java.util.regex.*;

public
interface JVMConstants 

    extends JVMOpcodes

{

    //
    // The first group of constants can be used to control the behavior of
    // the assembler (currenly just the code assembler).
    //

    public static final int  STRICT_CODE_MODEL = 0;
    public static final int  EXPANDED_CODE_MODEL = 1;
    public static final int  DEFAULT_CODE_MODEL = EXPANDED_CODE_MODEL;

    public static final int  MIN_CODE_MODEL = STRICT_CODE_MODEL;
    public static final int  MAX_CODE_MODEL = EXPANDED_CODE_MODEL;

    //
    // Class file definitions.
    //

    public static final int  MAGIC_NUMBER = 0xCAFEBABE;
    public static final int  MINIMUM_MAJOR_VERSION = 45;
    public static final int  MAXIMUM_MAJOR_VERSION = 0xFFFF;
    public static final int  DEFAULT_MAJOR_VERSION = 46;
    public static final int  MINIMUM_MINOR_VERSION = 0;
    public static final int  MAXIMUM_MINOR_VERSION = 0xFFFF;
    public static final int  DEFAULT_MINOR_VERSION = 0;
    public static final int  MINIMUM_ARRAY_DIMENSIONS = 1;
    public static final int  MAXIMUM_ARRAY_DIMENSIONS = 255;

    //
    // Offsets of the items in a Java class file. The first five are absolute
    // positions - all others have to add the size of the tables that precede
    // it to the defined offsets.
    //

    public static final int  CLASSFILE_MAGIC = 0;
    public static final int  CLASSFILE_MINOR_VERSION = 4;
    public static final int  CLASSFILE_MAJOR_VERSION = 6;
    public static final int  CLASSFILE_CONSTANT_POOL_COUNT = 8;
    public static final int  CLASSFILE_CONSTANT_POOL_TABLE = 10;

    public static final int  CLASSFILE_ACCESS_FLAGS = 10;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_THIS_CLASS = 12;			// plus bytes in all preceeding tables
    public static final int  CLASSFILE_SUPER_CLASS = 14;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_INTERFACES_COUNT = 16;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_INTERFACES_TABLE = 18;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_FIELDS_COUNT = 18;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_FIELDS_TABLE = 20;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_METHODS_COUNT = 20;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_METHODS_TABLE = 22;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_ATTRIBUTES_COUNT = 22;		// plus bytes in all preceeding tables
    public static final int  CLASSFILE_ATTRIBUTES_TABLE = 24;		// plus bytes in all preceeding tables

    public static final int  CLASSFILE_MINIMUM_SIZE = 25;		// absolute minimum size

    //
    // Access flag definitions - a few are overloaded (e.g., ACC_SUPER and
    // ACC_SYNCHRONIZED). There are other flags, like 0x1000 (ACC_SYNTHETIC)
    // that seem to be used by compilers but aren't documented in Sun's book
    // about the JVM sepcifications (second edition). We're going to collect
    // all unused bits in the UNDOCUMENTED_MASK and assume, for now anyway,
    // that any of them can always be set.
    //

    public static final int  ACC_PUBLIC = 0x0001;
    public static final int  ACC_PRIVATE = 0x0002;
    public static final int  ACC_PROTECTED = 0x0004;
    public static final int  ACC_STATIC = 0x0008;
    public static final int  ACC_FINAL = 0x0010;
    public static final int  ACC_SUPER = 0x0020;
    public static final int  ACC_SYNCHRONIZED = 0x0020;
    public static final int  ACC_VOLATILE = 0x0040;
    public static final int  ACC_TRANSIENT = 0x0080;
    public static final int  ACC_NATIVE = 0x0100;
    public static final int  ACC_INTERFACE = 0x0200;
    public static final int  ACC_ABSTRACT = 0x0400;
    public static final int  ACC_STRICT = 0x0800;

    public static final int  UNDOCUMENTED_MASK = 0xFFFFF000;		// bits not mentioned in second edition book

    public static final int  CLASS_ACCESS_MASK = ACC_PUBLIC|ACC_FINAL|ACC_SUPER|ACC_INTERFACE|ACC_ABSTRACT|UNDOCUMENTED_MASK;
    public static final int  FIELDS_ACCESS_MASK = ACC_PUBLIC|ACC_PRIVATE|ACC_PROTECTED|ACC_STATIC|ACC_FINAL|ACC_VOLATILE|ACC_TRANSIENT|UNDOCUMENTED_MASK;
    public static final int  METHODS_ACCESS_MASK = ACC_PUBLIC|ACC_PRIVATE|ACC_PROTECTED|ACC_STATIC|ACC_FINAL|ACC_SYNCHRONIZED|ACC_NATIVE|ACC_ABSTRACT|ACC_STRICT|UNDOCUMENTED_MASK;
    public static final int  INNER_CLASS_ACCESS_MASK = ACC_PUBLIC|ACC_PRIVATE|ACC_PROTECTED|ACC_STATIC|ACC_FINAL|ACC_INTERFACE|ACC_ABSTRACT|UNDOCUMENTED_MASK;

    //
    // Access file names.
    //

    public static final String  NAME_PUBLIC = "public";
    public static final String  NAME_PRIVATE = "private";
    public static final String  NAME_PROTECTED = "protected";
    public static final String  NAME_STATIC = "static";
    public static final String  NAME_FINAL = "final";
    public static final String  NAME_SUPER = "super";
    public static final String  NAME_SYNCHRONIZED = "synchronized";
    public static final String  NAME_VOLATILE = "volatile";
    public static final String  NAME_TRANSIENT = "transient";
    public static final String  NAME_NATIVE = "native";
    public static final String  NAME_INTERFACE = "interface";
    public static final String  NAME_ABSTRACT = "abstract";
    public static final String  NAME_STRICT = "strict";

    //
    // Constant pool constants.
    //

    public static final int  CONSTANT_UTF8 = 1;
    public static final int  CONSTANT_INTEGER = 3;
    public static final int  CONSTANT_FLOAT = 4;
    public static final int  CONSTANT_LONG = 5;
    public static final int  CONSTANT_DOUBLE = 6;
    public static final int  CONSTANT_CLASS = 7;
    public static final int  CONSTANT_STRING = 8;
    public static final int  CONSTANT_FIELDREF = 9;
    public static final int  CONSTANT_METHODREF = 10;
    public static final int  CONSTANT_INTERFACEMETHODREF = 11;
    public static final int  CONSTANT_NAMEANDTYPE = 12;

    //
    // Official predefined attribute names.
    //

    public static final String  ATTRIBUTE_CODE = "Code";
    public static final String  ATTRIBUTE_CONSTANT_VALUE = "ConstantValue";
    public static final String  ATTRIBUTE_DEPRECATED = "Deprecated";
    public static final String  ATTRIBUTE_EXCEPTIONS = "Exceptions";
    public static final String  ATTRIBUTE_INNER_CLASSES = "InnerClasses";
    public static final String  ATTRIBUTE_LINE_NUMBER_TABLE = "LineNumberTable";
    public static final String  ATTRIBUTE_LOCAL_VARIABLE_TABLE = "LocalVariableTable";
    public static final String  ATTRIBUTE_SOURCE_FILE = "SourceFile";
    public static final String  ATTRIBUTE_SYNTHETIC = "Synthetic";

    //
    // Base type names, including one for jsr return address, plus void.
    //

    public static final String  BASE_TYPE_NAME_BOOLEAN = "boolean";
    public static final String  BASE_TYPE_NAME_CHAR = "char";
    public static final String  BASE_TYPE_NAME_FLOAT = "float";
    public static final String  BASE_TYPE_NAME_DOUBLE = "double";
    public static final String  BASE_TYPE_NAME_BYTE = "byte";
    public static final String  BASE_TYPE_NAME_SHORT = "short";
    public static final String  BASE_TYPE_NAME_INT = "int";
    public static final String  BASE_TYPE_NAME_LONG = "long";
    public static final String  BASE_TYPE_NAME_RETURNADDRESS = "returnaddress";		// jsr return address

    public static final String  NAME_VOID = "void";

    //
    // Descriptor definitions - sometimes we want char values that can be
    // used as case labels in switch statements, which other times we want
    // strings.
    //

    public static final char  DESCRIPTOR_BYTE = 'B';
    public static final char  DESCRIPTOR_CHAR = 'C';
    public static final char  DESCRIPTOR_DOUBLE = 'D';
    public static final char  DESCRIPTOR_FLOAT = 'F';
    public static final char  DESCRIPTOR_INT = 'I';
    public static final char  DESCRIPTOR_LONG = 'J';
    public static final char  DESCRIPTOR_CLASS = 'L';
    public static final char  DESCRIPTOR_SHORT = 'S';
    public static final char  DESCRIPTOR_BOOLEAN = 'Z';
    public static final char  DESCRIPTOR_ARRAY = '[';
    public static final char  DESCRIPTOR_VOID = 'V';		// method returns only
    public static final char  DESCRIPTOR_RETURNADDRESS = 'R';	// return address (for jsr and ret)

    public static final String  BYTE_DESCRIPTOR = "B";
    public static final String  CHAR_DESCRIPTOR = "C";
    public static final String  DOUBLE_DESCRIPTOR = "D";
    public static final String  FLOAT_DESCRIPTOR = "F";
    public static final String  INT_DESCRIPTOR = "I";
    public static final String  LONG_DESCRIPTOR = "J";
    public static final String  CLASS_DESCRIPTOR = "L";
    public static final String  SHORT_DESCRIPTOR = "S";
    public static final String  BOOLEAN_DESCRIPTOR = "Z";
    public static final String  ARRAY_DESCRIPTOR = "[";
    public static final String  VOID_DESCRIPTOR = "V";
    public static final String  RETURNADDRESS_DESCRIPTOR = "R";

    //
    // Some miscellaneous definitions.
    //

    public static final String  NAME_THIS = "this";
    public static final String  NAME_INIT = "<init>";
    public static final String  NAME_CLASS_INIT = "<clinit>";
    public static final String  NULL_DESCRIPTOR = "Lnull;";
    public static final String  OBJECT_CLASS = "java.lang.Object";
    public static final String  OBJECT_DESCRIPTOR = "Ljava/lang/Object;";
    public static final String  STRING_CLASS = "java.lang.String";
    public static final String  STRING_DESCRIPTOR = "Ljava/lang/String;";
    public static final String  SUPER_DESCRIPTOR = "()V";
    public static final String  THROWABLE_CLASS = "java.lang.Throwable";
    public static final String  THROWABLE_DESCRIPTOR = "Ljava/lang/Throwable;";

    //
    // Type codes used to OP_NEWARRAY to create arrays.
    //

    public static final int  BOOLEAN_ARRAY_TYPE_ID = 4;
    public static final int  CHAR_ARRAY_TYPE_ID = 5;
    public static final int  FLOAT_ARRAY_TYPE_ID = 6;
    public static final int  DOUBLE_ARRAY_TYPE_ID = 7;
    public static final int  BYTE_ARRAY_TYPE_ID = 8;
    public static final int  SHORT_ARRAY_TYPE_ID = 9;
    public static final int  INT_ARRAY_TYPE_ID = 10;
    public static final int  LONG_ARRAY_TYPE_ID = 11;
}

