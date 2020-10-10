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

package att.research.yoix;

abstract
class YoixModuleParser extends YoixModule

{

    static String  $MODULENAME = M_PARSER;

    static String  $YOIXPARSERCONSTANTS = YOIXPACKAGE + ".YoixParserConstants";
    static String  $XMLPARSERCONSTANTS = YOIXPACKAGE + ".XMLParserConstants";
    static String  $DOTPARSERCONSTANTS = YOIXPACKAGE + ".DOTParserConstants";
    static String  $PATPARSERCONSTANTS = YOIXPACKAGE + ".PatternParserConstants";

    static Integer  $PARSER_DOT = new Integer(PARSER_DOT);
    static Integer  $PARSER_DTD = new Integer(PARSER_DTD);
    static Integer  $PARSER_NONE = new Integer(PARSER_NONE);
    static Integer  $PARSER_PATTERN = new Integer(PARSER_PATTERN);
    static Integer  $PARSER_PATTERN_AND = new Integer(PARSER_PATTERN_AND);
    static Integer  $PARSER_PATTERN_OR = new Integer(PARSER_PATTERN_OR);
    static Integer  $PARSER_PATTERN_XOR = new Integer(PARSER_PATTERN_XOR);
    static Integer  $PARSER_XML = new Integer(PARSER_XML);
    static Integer  $PARSER_YOIX = new Integer(PARSER_YOIX);

    static Integer  $ROOT_TREE = new Integer(ROOT_TREE);
    static Integer  $CHILDREN = new Integer(CHILDREN);
    static Integer  $DESCENDANTS = new Integer(DESCENDANTS);

    static Integer  $CASE_INSENSITIVE = new Integer(CASE_INSENSITIVE);
    static Integer  $DOT_PATTERN = new Integer(DOT_PATTERN);
    static Integer  $SHELL_PATTERN = new Integer(SHELL_PATTERN);
    static Integer  $SINGLE_BYTE = new Integer(SINGLE_BYTE);
    static Integer  $TEXT_PATTERN = new Integer(TEXT_PATTERN);
    static Integer  $REFLAGS_MASK = new Integer(REFLAGS_MASK);

    static Integer  $TOSS_WS_CHARDATA = new Integer(YoixMiscXML.TOSS_WS_CHARDATA);
    static Integer  $TRIM_FRONT_CHARDATA_WS = new Integer(YoixMiscXML.TRIM_FRONT_CHARDATA_WS);
    static Integer  $TRIM_BACK_CHARDATA_WS = new Integer(YoixMiscXML.TRIM_BACK_CHARDATA_WS);
    static Integer  $TOSS_ATTRIBUTES = new Integer(YoixMiscXML.TOSS_ATTRIBUTES);
    static Integer  $TOSS_WS_CDATA = new Integer(YoixMiscXML.TOSS_WS_CDATA);
    static Integer  $TRIM_FRONT_CDATA_WS = new Integer(YoixMiscXML.TRIM_FRONT_CDATA_WS);
    static Integer  $TRIM_BACK_CDATA_WS = new Integer(YoixMiscXML.TRIM_BACK_CDATA_WS);
    static Integer  $OMIT_TEXT_CONSOLIDATION = new Integer(YoixMiscXML.OMIT_TEXT_CONSOLIDATION);

    static Integer  $OMIT_NULL_ELEMENTS = new Integer(YoixMiscXML.OMIT_NULL_ELEMENTS);
    static Integer  $ADD_XML_DECLARATION = new Integer(YoixMiscXML.ADD_XML_DECLARATION);
    static Integer  $ACCEPT_ALL_NAMES = new Integer(YoixMiscXML.ACCEPT_ALL_NAMES);
    static Integer  $USE_EMPTY_ELEMENT_TAG = new Integer(YoixMiscXML.USE_EMPTY_ELEMENT_TAG);

    static Object  $module[] = {
    //
    // NAME                       ARG                       COMMAND     MODE   REFERENCE
    // ----                       ---                       -------     ----   ---------
       null,                      "41",                     $LIST,      $RORO, $MODULENAME,
       "CHILDREN",                $CHILDREN,                $INTEGER,   $LR__, null,
       "DESCENDANTS",             $DESCENDANTS,             $INTEGER,   $LR__, null,
       "PARSER_DOT",              $PARSER_DOT,              $INTEGER,   $LR__, null,
       "PARSER_DTD",              $PARSER_DTD,              $INTEGER,   $LR__, null,
       "PARSER_NONE",             $PARSER_NONE,             $INTEGER,   $LR__, null,
       "PARSER_PATTERN",          $PARSER_PATTERN,          $INTEGER,   $LR__, null,
       "PARSER_PATTERN_AND",      $PARSER_PATTERN_AND,      $INTEGER,   $LR__, null,
       "PARSER_PATTERN_OR",       $PARSER_PATTERN_OR,       $INTEGER,   $LR__, null,
       "PARSER_PATTERN_XOR",      $PARSER_PATTERN_XOR,      $INTEGER,   $LR__, null,
       "PARSER_XML",              $PARSER_XML,              $INTEGER,   $LR__, null,
       "PARSER_YOIX",             $PARSER_YOIX,             $INTEGER,   $LR__, null,
       "ROOT_TREE",               $ROOT_TREE,               $INTEGER,   $LR__, null,

       "CASE_INSENSITIVE",        $CASE_INSENSITIVE,        $INTEGER,   $LR__, null,
       "DOT_PATTERN",             $DOT_PATTERN,             $INTEGER,   $LR__, null,
       "SHELL_PATTERN",           $SHELL_PATTERN,           $INTEGER,   $LR__, null,
       "SINGLE_BYTE",             $SINGLE_BYTE,             $INTEGER,   $LR__, null,
       "TEXT_PATTERN",            $TEXT_PATTERN,            $INTEGER,   $LR__, null,

       "OMIT_TEXT_CONSOLIDATION", $OMIT_TEXT_CONSOLIDATION, $INTEGER,   $LR__, null,
       "TOSS_ATTRIBUTES",         $TOSS_ATTRIBUTES,         $INTEGER,   $LR__, null,
       "TOSS_WS_CHARDATA",        $TOSS_WS_CHARDATA,        $INTEGER,   $LR__, null,
       "TOSS_WS_CDATA",           $TOSS_WS_CDATA,           $INTEGER,   $LR__, null,
       "TRIM_BACK_CDATA_WS",      $TRIM_BACK_CDATA_WS,      $INTEGER,   $LR__, null,
       "TRIM_BACK_CHARDATA_WS",   $TRIM_BACK_CHARDATA_WS,   $INTEGER,   $LR__, null,
       "TRIM_FRONT_CDATA_WS",     $TRIM_FRONT_CDATA_WS,     $INTEGER,   $LR__, null,
       "TRIM_FRONT_CHARDATA_WS",  $TRIM_FRONT_CHARDATA_WS,  $INTEGER,   $LR__, null,

       "ACCEPT_ALL_NAMES",        $ACCEPT_ALL_NAMES,        $INTEGER,   $LR__, null,
       "ADD_XML_DECLARATION",     $ADD_XML_DECLARATION,     $INTEGER,   $LR__, null,
       "OMIT_NULL_ELEMENTS",      $OMIT_NULL_ELEMENTS,      $INTEGER,   $LR__, null,
       "USE_EMPTY_ELEMENT_TAG",   $USE_EMPTY_ELEMENT_TAG,   $INTEGER,   $LR__, null,

       "tokenAssociativity",      "-1",                     $BUILTIN,   $LR_X, null,
       "tokenImage",              "-1",                     $BUILTIN,   $LR_X, null,
       "tokenPrecedence",         "1",                      $BUILTIN,   $LR_X, null,
       "tokenValue",              "-1",                     $BUILTIN,   $LR_X, null,
       "xmlAdd",                  "-2",                     $BUILTIN,   $LR_X, null,
       "xmlGet",                  "-2",                     $BUILTIN,   $LR_X, null,
       "xmlToYoix",               "-1",                     $BUILTIN,   $LR_X, null,
       "yoixToXML",               "-1",                     $BUILTIN,   $LR_X, null,

       "DOTConstants",            "20",                     $DICT,      $RORO, $_THIS,
       null,                      "-1",                     $GROWTO,    null,  null,
       $DOTPARSERCONSTANTS,       "_\t_",                   $READCLASS, $LR__, null,
       N_EOF,                     $YOIX_EOF,                $INTEGER,   $LR__, null,
       null,                      null,                     $GROWTO,    null,  null,
       null,                      $MODULENAME,              $PUT,       null,  null,

       "PatternConstants",        "20",                     $DICT,      $RORO, $_THIS,
       null,                      "-1",                     $GROWTO,    null,  null,
       $PATPARSERCONSTANTS,       null,                     $READCLASS, $LR__, null,
       N_EOF,                     $YOIX_EOF,                $INTEGER,   $LR__, null,
       null,                      null,                     $GROWTO,    null,  null,
       null,                      $MODULENAME,              $PUT,       null,  null,

       "XMLConstants",            "53",                     $DICT,      $RORO, $_THIS,
       null,                      "-1",                     $GROWTO,    null,  null,
       $XMLPARSERCONSTANTS,       "_\t_",                   $READCLASS, $LR__, null,
       N_EOF,                     $YOIX_EOF,                $INTEGER,   $LR__, null,
       null,                      null,                     $GROWTO,    null,  null,
       null,                      $MODULENAME,              $PUT,       null,  null,

       "YoixConstants",           "271",                    $DICT,      $RORO, $_THIS,
       null,                      "-1",                     $GROWTO,    null,  null,
       $YOIXPARSERCONSTANTS,      null,                     $READCLASS, $LR__, null,
       N_EOF,                     $YOIX_EOF,                $INTEGER,   $LR__, null,
       null,                      null,                     $GROWTO,    null,  null,
       null,                      $MODULENAME,              $PUT,       null,  null,

       T_PARSETREE,               "16",                     $DICT,      $L___, T_PARSETREE,
       N_MAJOR,                   $PARSETREE,               $INTEGER,   $LR__, null,
       N_MINOR,                   $STATEMENT,               $INTEGER,   $LR__, null,
       N_ADDTAGS,                 $FALSE,                   $INTEGER,   $RW_,  null,
       N_CHILD,                   T_CALLABLE,               $NULL,      $L__X, null,
       N_DEPTH,                   "0",                      $INTEGER,   $LR__, null,
       N_ERRORDICT,               T_DICT,                   $NULL,      $LRW_, null,
       N_LENGTH,                  "0",                      $INTEGER,   $LR__, null,
       N_MATCH,                   T_CALLABLE,               $NULL,      $L__X, null,
       N_PARENT,                  T_PARSETREE,              $NULL,      $LR__, null,
       N_PARSE,                   T_STRING,                 $NULL,      $RW_,  null,
       N_PARSER,                  $PARSER_YOIX,             $INTEGER,   $RW_,  null,
       N_POSITION,                "0",                      $INTEGER,   $LR__, null,
       N_TREE,                    T_STRING,                 $NULL,      $LR__, null,
       N_TYPE,                    "0",                      $INTEGER,   $LR__, null,
       N_VALUE,                   T_OBJECT,                 $NULL,      $LR__, null,
       N_WALK,                    T_CALLABLE,               $NULL,      $L__X, null,
    };

    //
    // Default argument values for xmlToYoix() builtin.
    //

    private static int  DEFAULT_XML_FLAGS = YoixMiscXML.TOSS_WS_CHARDATA|YoixMiscXML.TOSS_ATTRIBUTES;
    private static int  DEFAULT_XML_MODEL = 1;          // dictionaries or arrays

    ///////////////////////////////////
    //
    // YoixModuleParser Methods
    //
    ///////////////////////////////////

    public static YoixObject
    tokenAssociativity(YoixObject arg[]) {

	int  value = -1;

	//
	// The return value has changed from earlier releases, but we
	// doubt there were any production programs using this builtin.
	// The optional extra argruments are also new and probably not
	// documented.
	//

	if (arg.length == 1 || arg.length == 4) {
	    if (arg[0].isInteger()) {
		if (arg.length < 2 || arg[1].isInteger()) {
		    if (arg.length < 3 || arg[2].isInteger()) {
			if (arg.length < 4 || arg[3].isInteger()) {
			    value = YoixParser.getAssociativity(
				arg[0].intValue(),
				arg.length > 1 ? arg[1].intValue() : -1,
				arg.length > 2 ? arg[2].intValue() : 0,
				arg.length > 3 ? arg[3].intValue() : 1
			    );
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    tokenImage(YoixObject arg[]) {

	String  value = null;
	int     parser = PARSER_YOIX;
	int     p;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isInteger()) {
		if (arg.length == 2) {
		    if (arg[1].isInteger()) {
			if ((p = arg[1].intValue()) == PARSER_XML || p == PARSER_DTD)
			    parser = PARSER_XML;
		    } else VM.badArgument(1);
		}
		value = YoixMisc.tokenImage(arg[0].intValue(), parser);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    tokenPrecedence(YoixObject arg[]) {

	int  value = -1;

	if (arg[0].isInteger())
	    value = YoixParser.getPrecedence(arg[0].intValue());
	else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    tokenValue(YoixObject arg[]) {

	String  tok;
	int     value = -1;
	int     parser = PARSER_YOIX;
	int     p;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString()) {
		if (arg.length == 2) {
		    if (arg[1].isInteger()) {
			if ((p = arg[1].intValue()) == PARSER_XML || p == PARSER_DTD)
			    parser = PARSER_XML;
		    } else VM.badArgument(1);
		}
		value = YoixMisc.tokenValue(arg[0].stringValue(), parser);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    xmlAdd(YoixObject arg[]) {

	YoixObject  obj = null;
	boolean     result = false;
	String      key;

	if (arg.length == 3) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg[1].isDictionary() || arg[1].isArray() || arg[1].isString() || arg[1].isNull()) {
		    if (arg[2].isDictionary() || arg[2].isArray() || arg[2].isString() || arg[2].isNull()) {
			if (arg[0].notNull()) {
			    if (arg[2].isArray() || arg[2].isNull()) {
				key = arg[0].stringValue().trim();
				if (key.length() > 0)
				    obj = YoixMiscXML.xmlAdd(key, arg[1], arg[2]);
			    } else VM.abort(UNIMPLEMENTED);	// maybe later...
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    xmlGet(YoixObject arg[]) {

	YoixObject  obj = null;
	boolean     extract;
	String      key;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg[1].isDictionary() || arg[1].isArray() || arg[1].isString() || arg[1].isNull()) {
		    if (arg.length == 2 || arg[2].isInteger()) {
			if (arg[0].notNull()) {
			    if (arg[1].notNull()) {
				key = arg[0].stringValue();
				extract = (arg.length > 2) ? arg[2].booleanValue() : true;
				if (arg[1].isDictionary() || arg[1].isArray())
				    obj = YoixMiscXML.xmlGet(key, arg[1], extract);
				else if (arg[1].isString())
				    VM.abort(UNIMPLEMENTED);
			    }
			}
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    xmlToYoix(YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  parser;
	int         model;
	int         flags;
	int         argn;

	if (arg.length >= 1 && arg.length <= 4) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg.length == 1 || arg[1].isNumber() || arg[1].isParseTree() || arg[1].isNull()) {
		    if (arg.length != 1) {
			if (arg[1].notNumber()) {
			    if (arg[1].isParseTree()) {
				parser = arg[1];
				parser.putInt(N_PARSER, PARSER_XML);	// make sure
			    } else parser = YoixObject.newParseTree(PARSER_XML);
			    argn = 2;
			} else {
			    parser = YoixObject.newParseTree(PARSER_XML);
			    argn = 1;
			}
		    } else {
			parser = YoixObject.newParseTree(PARSER_XML);
			argn = 1;
		    }
		    if (argn >= arg.length || arg[argn].isNumber()) {
			model = (argn < arg.length) ? arg[argn].intValue() : DEFAULT_XML_MODEL;
			argn++;
			if (argn >= arg.length || arg[argn].isNumber()) {
			    flags = (argn < arg.length) ? arg[argn].intValue() : DEFAULT_XML_FLAGS;
			    argn++;
			    if (arg[0].notNull()) {
				parser.putInt(N_PARSER, PARSER_XML);	// make sure
				parser.putObject(N_PARSE, arg[0]);
				obj = YoixMiscXML.xmlToYoix((SimpleNode)parser.getManagedObject(), model, flags);
			    }
			} else VM.badArgument(argn);
		    } else VM.badArgument(argn);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj != null ? obj : YoixObject.newNull());
    }


    public static YoixObject
    yoixToXML(YoixObject arg[]) {

	String  xml = null;
	String  indent;
	int     flags;

	//
	// A missing or NULL second argument means no extra whitespace is
	// added, while the empty string (i.e., "") as a second argument
	// means only newlines will be added.
	// 

	if (arg.length >= 1 && arg.length <= 3) {
	    if (arg[0].compound() || arg[0].isArray() || arg[0].isNull() || arg[0].isString()) {
		if (arg.length <= 1 || arg[1].isNumber()) {
		    if (arg.length <= 2 || arg[2].isString() || arg[2].isNull()) {
			if (arg[0].notNull()) {
			    flags = (arg.length > 1) ? arg[1].intValue() : 0;
			    if (arg.length > 2 && arg[2].isString())
				indent = arg[2].stringValue();
			    else indent = null;
			    xml = YoixMiscXML.yoixToXML(arg[0], flags, indent);
			}
		    } else VM.badArgument(1);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newString(xml));
    }
}

