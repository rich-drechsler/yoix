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
interface JVMPatterns {

    //
    // Regular expression patterns that are primarily used by methods that
    // build class files from the tokens extracted from a string.
    //
    // NOTE - in a few places (e.g., the POOL_INDEX patterns) we initially
    // tried using (?:...) to prevent capturing unwanted characters, but it
    // didn't seem to work. We eventually will revisit, but for now you may
    // notice some inconsistencies with parens in the PATTERN definitions.
    //
    // NOTE - the "internal rules" for when parens are needed or not is very
    // confusing and eventually must be cleaned up. For example, differences
    // between various CONSTRUCTOR and METHOD related definitions highlights
    // the issue and if trying to make them similiar breaks one of the other.
    // Took some trial and error to get things working, but this definitiely
    // needs attention - in fact the whole scanning business has gotten very
    // complicated and makes it look using a JavaCC to clean it up would be
    // worth a try (later). For now this is very ugly but sufficient.
    //
    // NOTE - the addition of REGEX_HEX_STRING (on 10/3/09) and the current
    // definitions of the regular expression strings used to match numbers
    // (e.g., REGEX_INTEGER, REGEX_FLOAT) means REGEX_HEX_STRING sometimes
    // has to be checked before numbers are matched!!
    //
    // NOTE - take a look at Java bug 5050507 if you run into stack overflow
    // errors. Alternation is expensive and usually is the culprit. In one
    // case (e.g., REGEX_STRING) we added a trailing '+' to the pattern as
    // a way to disable backtracking when we got a stack overflow on a long
    // string literal. The (?>X) grouping also disables backtracking an we
    // could have used it as well. Bottom line is that some of the patterns
    // that we use may need tuning if you encounter stack overflow errors.
    //

    public static final String  CLASS_QUALIFIERS = "(public|final|super|interface|abstract|deprecated)";
    public static final String  FIELD_QUALIFIERS = "(public|private|protected|static|final|volatile|transient|synthetic|deprecated)";
    public static final String  METHOD_QUALIFIERS = "(public|private|protected|static|final|synchronized|native|abstract|strictfp|synthetic|deprecated)";
    public static final String  CONSTRUCTOR_QUALIFIERS = "public|private|protected";

    public static final String  REGEX_LWS = "^\\s*";
    public static final String  REGEX_TWS = "(\\s|$)";

    public static final String  REGEX_ACCESS_CLASS = "(public|final|super|interface|abstract)(\\s+(public|final|super|interface|abstract))*";
    public static final String  REGEX_ACCESS_FIELD = "(public|private|protected|static|final|volatile|transient)(\\s+(public|private|protected|static|final|volatile|transient))*";
    public static final String  REGEX_ARRAY_DESCRIPTOR = "(\\[)+[$_\\p{L}][$_\\p{L}\\p{N}]*([./][$_\\p{L}][$_\\p{L}\\p{N}]*)*";
    public static final String  REGEX_ARRAY_TYPE = "[$_\\p{L}][$_\\p{L}\\p{N}]*([./][$_\\p{L}][$_\\p{L}\\p{N}]*)*(\\[\\s*\\])+";
    public static final String  REGEX_ASSIGN = "=(?!=)";
    public static final String  REGEX_BASE_TYPE = "byte|char|double|float|int|long|short|boolean";
    public static final String  REGEX_BRANCH_LABEL = "([$_\\p{L}][_\\p{L}\\p{N}]*)\\s*:";
    public static final String  REGEX_CATCH = "catch";
    public static final String  REGEX_CHAR = "'([^'\\\\]|\\\\[^x]|\\\\x[0-9a-fA-F]+)'";
    public static final String  REGEX_CLASS = "class";
    public static final String  REGEX_CLASS_PRAGMAS = "interface|major_version|minor_version|sourcefile|deprecated";
    public static final String  REGEX_CLASS_NAME = "[$_\\p{L}][$_\\p{L}\\p{N}]*([.][$_\\p{L}][$_\\p{L}\\p{N}]*)*";
    public static final String  REGEX_CLASS_TYPE = "[$_\\p{L}][$_\\p{L}\\p{N}]*([./][$_\\p{L}][$_\\p{L}\\p{N}]*)*";
    public static final String  REGEX_CLOSE_BRACE = "\\}";
    public static final String  REGEX_CLOSE_PAREN = "\\)";
    public static final String  REGEX_CODE_PRAGMAS = "model|max_stack";
    public static final String  REGEX_COMMA = ",";
    public static final String  REGEX_EXTENDS = "extends";
    public static final String  REGEX_EXTERN = "extern";
    public static final String  REGEX_FLOAT = "[+-]?(\\d+[.]\\d*|[.]\\d+)([eE][+-]?\\d+)?";
    public static final String  REGEX_FLOAT_QUALIFIED = "[+-]?(\\d+[.]\\d*|[.]\\d+)([eE][+-]?\\d+)?[dDFf]?";
    public static final String  REGEX_HEX_STRING = "0x\"[0-9a-fA-F\\s]*\"";
    public static final String  REGEX_INTEGER = "[+-]?\\d+";
    public static final String  REGEX_INTEGER_QUALIFIED = "[+-]?\\d+[dDfFLl]?";
    public static final String  REGEX_INSTANCE_INIT = "<init>";
    public static final String  REGEX_NAME = "[$_\\p{L}][$_\\p{L}\\p{N}]*";
    public static final String  REGEX_NULL = "null";
    public static final String  REGEX_OPEN_BRACE = "\\{";
    public static final String  REGEX_OPEN_PAREN = "\\(";
    public static final String  REGEX_POOL_INDEX = "#(\\d+)";
    public static final String  REGEX_PRAGMA = "pragma";
    public static final String  REGEX_QUALIFIED_NAME = "[$_\\p{L}][$_\\p{L}\\p{N}]*([.][$_\\p{L}][$_\\p{L}\\p{N}]*)*";
    public static final String  REGEX_STRING = "\"([^\"\\\\]|\\\\[^x]|\\\\x[0-9a-fA-F]+)*+\"";
    public static final String  REGEX_SWITCH_LABEL = "([+-]?\\d+|default)\\s*:";
    public static final String  REGEX_THIS = "this";
    public static final String  REGEX_TRY = "try";
    public static final String  REGEX_TYPE_NAME = "[$_\\p{L}][$_\\p{L}\\p{N}]*(?:[./][$_\\p{L}][$_\\p{L}\\p{N}]*)*(?:\\[\\s*\\])*";
    public static final String  REGEX_WHITESPACE_OR_COMMENT = "\\s*($|;.*$|//.*$)";
    public static final String  REGEX_WORD = "\\w+";

    public static final String  REGEX_CLASS_QUALIFIERS = "(" + CLASS_QUALIFIERS + "\\s+(" + CLASS_QUALIFIERS + "\\s+)*)*";

    public static final String  REGEX_FIELD_QUALIFIERS = "(" + FIELD_QUALIFIERS + "\\s+(" + FIELD_QUALIFIERS + "\\s+)*)*";
    public static final String  REGEX_FIELD_INITIALIZER = REGEX_ASSIGN + "\\s*((" + REGEX_HEX_STRING + ")|(" + REGEX_FLOAT + ")|(" + REGEX_INTEGER + ")|(" + REGEX_STRING + ")|(" + REGEX_CHAR + ")|(" + REGEX_OPEN_BRACE + "))";
    public static final String  REGEX_FIELD_TAIL = "(" + REGEX_ASSIGN + ")|(" + REGEX_WHITESPACE_OR_COMMENT + ")";

    public static final String  REGEX_METHOD_QUALIFIERS = "(" + METHOD_QUALIFIERS + "\\s+(" + METHOD_QUALIFIERS + "\\s+)*)*";
    public static final String  REGEX_METHOD_TYPE = REGEX_TYPE_NAME;
    public static final String  REGEX_METHOD_NAME = "(" + REGEX_NAME + "|" + REGEX_INSTANCE_INIT + ")";
    public static final String  REGEX_METHOD_PARAMETER = "(" + REGEX_TYPE_NAME + ")\\s+(" + REGEX_NAME + ")";
    public static final String  REGEX_METHOD_PARAMETERS = "\\(\\s*(((" + REGEX_TYPE_NAME + ")\\s+(" + REGEX_NAME + "))(\\s*,\\s*((" + REGEX_TYPE_NAME + ")\\s+(" + REGEX_NAME + ")))*)*\\s*\\)";

    public static final String  REGEX_CONSTRUCTOR_QUALIFIERS = "(" + CONSTRUCTOR_QUALIFIERS + "\\s+(" + CONSTRUCTOR_QUALIFIERS + "\\s+)*)*";
    public static final String  REGEX_CONSTRUCTOR_NAME = REGEX_NAME + "|" + REGEX_INSTANCE_INIT;

    public static final String  REGEX_EXTERNAL_FIELD_QUALIFIERS = "(" + FIELD_QUALIFIERS + "\\s+(" + FIELD_QUALIFIERS + "\\s+)*)*";
    public static final String  REGEX_EXTERNAL_FIELD_TYPE = REGEX_TYPE_NAME;
    public static final String  REGEX_EXTERNAL_FIELD_NAME = REGEX_QUALIFIED_NAME;

    public static final String  REGEX_EXTERNAL_METHOD_QUALIFIERS = "(" + METHOD_QUALIFIERS + "\\s+(" + METHOD_QUALIFIERS + "\\s+)*)*";
    public static final String  REGEX_EXTERNAL_METHOD_TYPE = REGEX_TYPE_NAME;
    public static final String  REGEX_EXTERNAL_METHOD_NAME = REGEX_QUALIFIED_NAME;
    public static final String  REGEX_EXTERNAL_METHOD_PARAMETER = "(" + REGEX_TYPE_NAME + ")(\\s+" + REGEX_NAME + ")?";
    public static final String  REGEX_EXTERNAL_METHOD_PARAMETERS = "\\(\\s*((" + REGEX_EXTERNAL_METHOD_PARAMETER + ")(\\s*,\\s*(" + REGEX_EXTERNAL_METHOD_PARAMETER + "))*)*\\s*\\)";

    public static final String  REGEX_EXTERNAL_CONSTRUCTOR_QUALIFIERS = "(" + CONSTRUCTOR_QUALIFIERS + "\\s+(" + CONSTRUCTOR_QUALIFIERS + "\\s+)*)*";
    public static final String  REGEX_EXTERNAL_CONSTRUCTOR_NAME = REGEX_QUALIFIED_NAME;

    public static final Pattern  PATTERN_ACCESS_CLASS = Pattern.compile(REGEX_LWS + "(" + REGEX_ACCESS_CLASS + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_ACCESS_FIELD = Pattern.compile(REGEX_LWS + "(" + REGEX_ACCESS_FIELD + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_ARRAY_DESCRIPTOR = Pattern.compile(REGEX_LWS + "(" + REGEX_ARRAY_DESCRIPTOR + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_ARRAY_TYPE = Pattern.compile(REGEX_LWS + "(" + REGEX_ARRAY_TYPE + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_ASSIGN = Pattern.compile(REGEX_LWS + "(" + REGEX_ASSIGN + ")");
    public static final Pattern  PATTERN_BASE_TYPE = Pattern.compile(REGEX_LWS + "(" + REGEX_BASE_TYPE + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_BRANCH_LABEL = Pattern.compile(REGEX_LWS + REGEX_BRANCH_LABEL + REGEX_TWS);
    public static final Pattern  PATTERN_CATCH = Pattern.compile(REGEX_LWS + "(" + REGEX_CATCH + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_CHAR = Pattern.compile(REGEX_LWS + "(" + REGEX_CHAR + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_CLASS = Pattern.compile(REGEX_LWS + "(" + REGEX_CLASS + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_CLASS_PRAGMAS = Pattern.compile(REGEX_LWS + "(" + REGEX_CLASS_PRAGMAS + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_CLASS_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_CLASS_NAME + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_CLASS_TYPE = Pattern.compile(REGEX_LWS + "(" + REGEX_CLASS_TYPE + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_CLOSE_BRACE = Pattern.compile(REGEX_LWS + "(" + REGEX_CLOSE_BRACE + ")");
    public static final Pattern  PATTERN_CLOSE_PAREN = Pattern.compile(REGEX_LWS + "(" + REGEX_CLOSE_PAREN + ")");
    public static final Pattern  PATTERN_CODE_PRAGMAS = Pattern.compile(REGEX_LWS + "(" + REGEX_CODE_PRAGMAS + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_COMMA = Pattern.compile(REGEX_LWS + "(" + REGEX_COMMA + ")");
    public static final Pattern  PATTERN_EXTENDS = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTENDS + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_EXTERN = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERN + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_FLOAT = Pattern.compile(REGEX_LWS + "(" + REGEX_FLOAT + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_FLOAT_QUALIFIED = Pattern.compile(REGEX_LWS + "(" + REGEX_FLOAT_QUALIFIED + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_HEX_STRING = Pattern.compile(REGEX_LWS + "(" + REGEX_HEX_STRING + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_INTEGER = Pattern.compile(REGEX_LWS + "(" + REGEX_INTEGER + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_INTEGER_QUALIFIED = Pattern.compile(REGEX_LWS + "(" + REGEX_INTEGER_QUALIFIED + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_NAME + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_NULL = Pattern.compile(REGEX_LWS + "(" + REGEX_NULL + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_OPEN_BRACE = Pattern.compile(REGEX_LWS + "(" + REGEX_OPEN_BRACE + ")");
    public static final Pattern  PATTERN_OPEN_PAREN = Pattern.compile(REGEX_LWS + "(" + REGEX_OPEN_PAREN + ")");
    public static final Pattern  PATTERN_POOL_INDEX = Pattern.compile(REGEX_LWS + REGEX_POOL_INDEX + REGEX_TWS);
    public static final Pattern  PATTERN_PRAGMA = Pattern.compile(REGEX_LWS + "(" + REGEX_PRAGMA + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_QUALIFIED_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_QUALIFIED_NAME + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_STRING = Pattern.compile(REGEX_LWS + "(" + REGEX_STRING + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_SWITCH_LABEL = Pattern.compile(REGEX_LWS + REGEX_SWITCH_LABEL);
    public static final Pattern  PATTERN_TRY = Pattern.compile(REGEX_LWS + "(" + REGEX_TRY + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_THIS = Pattern.compile(REGEX_LWS + "(" + REGEX_THIS + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_TYPE_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_TYPE_NAME + ")" + REGEX_TWS);
    public static final Pattern  PATTERN_WHITESPACE_OR_COMMENT = Pattern.compile(REGEX_LWS + REGEX_WHITESPACE_OR_COMMENT);
    public static final Pattern  PATTERN_WORD = Pattern.compile(REGEX_LWS + "(" + REGEX_WORD + ")" + REGEX_TWS);

    public static final Pattern  PATTERN_CLASS_QUALIFIERS = Pattern.compile(REGEX_LWS + "(" + REGEX_CLASS_QUALIFIERS + ")");
    public static final Pattern  PATTERN_CLASS_DECLARATION = Pattern.compile("((" + PATTERN_CLASS_QUALIFIERS + ")(" + REGEX_CLASS + ")\\s+(" + REGEX_QUALIFIED_NAME + ")(\\s+(" + REGEX_EXTENDS + ")\\s+(" + REGEX_TYPE_NAME + "))?\\s*(" + REGEX_OPEN_BRACE + "))" + REGEX_WHITESPACE_OR_COMMENT);

    public static final Pattern  PATTERN_FIELD_QUALIFIERS = Pattern.compile(REGEX_LWS + "(" + REGEX_FIELD_QUALIFIERS + ")");
    public static final Pattern  PATTERN_FIELD_TYPE = Pattern.compile(REGEX_LWS + "(" + REGEX_TYPE_NAME + ")\\s+");
    public static final Pattern  PATTERN_FIELD_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_NAME + ")\\s*");
    public static final Pattern  PATTERN_FIELD_INITIALIZER = Pattern.compile(REGEX_LWS + REGEX_FIELD_INITIALIZER + "\\s*");

    public static final Pattern  PATTERN_METHOD_QUALIFIERS = Pattern.compile(REGEX_LWS + "(" + REGEX_METHOD_QUALIFIERS + ")");
    public static final Pattern  PATTERN_METHOD_TYPE = Pattern.compile(REGEX_LWS + "(" + REGEX_METHOD_TYPE + ")");
    public static final Pattern  PATTERN_METHOD_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_METHOD_NAME + ")");
    public static final Pattern  PATTERN_METHOD_PARAMETER = Pattern.compile(REGEX_LWS + REGEX_METHOD_PARAMETER);
    public static final Pattern  PATTERN_METHOD_PARAMETERS = Pattern.compile(REGEX_LWS + REGEX_METHOD_PARAMETERS + REGEX_TWS);

    public static final Pattern  PATTERN_CONSTRUCTOR_QUALIFIERS = Pattern.compile(REGEX_LWS + "(" + REGEX_CONSTRUCTOR_QUALIFIERS + ")");
    public static final Pattern  PATTERN_CONSTRUCTOR_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_CONSTRUCTOR_NAME + ")");

    public static final Pattern  PATTERN_LOCAL_VARIABLE_TYPE = Pattern.compile(REGEX_LWS + "(" + REGEX_TYPE_NAME + ")\\s+");
    public static final Pattern  PATTERN_LOCAL_VARIABLE_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_NAME + ")\\s*");

    public static final Pattern  PATTERN_EXTERNAL_FIELD_QUALIFIERS = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERNAL_FIELD_QUALIFIERS + ")");
    public static final Pattern  PATTERN_EXTERNAL_FIELD_TYPE = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERNAL_FIELD_TYPE + ")\\s+");
    public static final Pattern  PATTERN_EXTERNAL_FIELD_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERNAL_FIELD_NAME + ")\\s*");

    public static final Pattern  PATTERN_EXTERNAL_METHOD_QUALIFIERS = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERNAL_METHOD_QUALIFIERS + ")");
    public static final Pattern  PATTERN_EXTERNAL_METHOD_TYPE = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERNAL_METHOD_TYPE + ")\\s+");
    public static final Pattern  PATTERN_EXTERNAL_METHOD_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERNAL_METHOD_NAME + ")");
    public static final Pattern  PATTERN_EXTERNAL_METHOD_PARAMETER = Pattern.compile(REGEX_LWS + REGEX_EXTERNAL_METHOD_PARAMETER);
    public static final Pattern  PATTERN_EXTERNAL_METHOD_PARAMETERS = Pattern.compile(REGEX_LWS + REGEX_EXTERNAL_METHOD_PARAMETERS + REGEX_TWS);

    public static final Pattern  PATTERN_EXTERNAL_CONSTRUCTOR_QUALIFIERS = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERNAL_CONSTRUCTOR_QUALIFIERS + ")");
    public static final Pattern  PATTERN_EXTERNAL_CONSTRUCTOR_NAME = Pattern.compile(REGEX_LWS + "(" + REGEX_EXTERNAL_CONSTRUCTOR_NAME + ")");

    public static final Pattern  PATTERN_METHOD_REFERENCE = Pattern.compile("((" + REGEX_EXTERNAL_METHOD_NAME + ")\\s*(" + REGEX_EXTERNAL_METHOD_PARAMETERS + "))\\s*");

    public static final Pattern  PATTERN_START_TRY = Pattern.compile(REGEX_LWS + REGEX_TRY + "\\s*" + REGEX_OPEN_BRACE);
    public static final Pattern  PATTERN_START_CATCH = Pattern.compile(REGEX_LWS + REGEX_CLOSE_BRACE + "\\s*" +  REGEX_CATCH);
}

