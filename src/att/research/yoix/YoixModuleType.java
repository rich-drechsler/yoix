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
class YoixModuleType extends YoixModule

{

    static String  $MODULENAME = M_TYPE;

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "33",                $LIST,      $RORO, $MODULENAME,
       "isArray",            "1",                 $BUILTIN,   $LR_X, null,
       "isBuiltin",          "-1",                $BUILTIN,   $LR_X, null,
       "isCallable",         "-1",                $BUILTIN,   $LR_X, null,
       "isComponent",        "1",                 $BUILTIN,   $LR_X, null,
       "isDatagramSocket",   "1",                 $BUILTIN,   $LR_X, null,
       "isDictionary",       "1",                 $BUILTIN,   $LR_X, null,
       "isDouble",           "1",                 $BUILTIN,   $LR_X, null,
       "isEdge",             "1",                 $BUILTIN,   $LR_X, null,
       "isEvent",            "1",                 $BUILTIN,   $LR_X, null,
       "isFile",             "1",                 $BUILTIN,   $LR_X, null,
       "isFont",             "1",                 $BUILTIN,   $LR_X, null,
       "isFunction",         "-1",                $BUILTIN,   $LR_X, null,
       "isGraph",            "1",                 $BUILTIN,   $LR_X, null,
       "isHashtable",        "1",                 $BUILTIN,   $LR_X, null,
       "isImage",            "1",                 $BUILTIN,   $LR_X, null,
       "isInt",              "1",                 $BUILTIN,   $LR_X, null,
       "isLayoutManager",    "1",                 $BUILTIN,   $LR_X, null,
       "isMatrix",           "1",                 $BUILTIN,   $LR_X, null,
       "isNode",             "1",                 $BUILTIN,   $LR_X, null,
       "isNumber",           "1",                 $BUILTIN,   $LR_X, null,
       "isOption",           "1",                 $BUILTIN,   $LR_X, null,
       "isParseTree",        "1",                 $BUILTIN,   $LR_X, null,
       "isPointer",          "1",                 $BUILTIN,   $LR_X, null,
       "isProcess",          "1",                 $BUILTIN,   $LR_X, null,
       "isSecurityManager",  "1",                 $BUILTIN,   $LR_X, null,
       "isServerSocket",     "1",                 $BUILTIN,   $LR_X, null,
       "isSocket",           "1",                 $BUILTIN,   $LR_X, null,
       "isStream",           "1",                 $BUILTIN,   $LR_X, null,
       "isString",           "1",                 $BUILTIN,   $LR_X, null,
       "isStringStream",     "1",                 $BUILTIN,   $LR_X, null,
       "isThread",           "1",                 $BUILTIN,   $LR_X, null,
       "isURL",              "1",                 $BUILTIN,   $LR_X, null,
       "isVector",           "1",                 $BUILTIN,   $LR_X, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleType Methods
    //
    ///////////////////////////////////

    public static YoixObject
    isArray(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isArray()));
    }


    public static YoixObject
    isBuiltin(YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1 || arg[1].isInteger()) {
		result = arg[0].isBuiltin();
		if (result && arg.length == 2)
		    result = arg[0].callable(arg[1].intValue());
	    } else VM.badArgument(1);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isCallable(YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1 || arg[1].isInteger()) {
		result = arg[0].isBuiltin() || arg[0].isFunction();
		if (result && arg.length == 2)
		    result = arg[0].callable(arg[1].intValue());
	    } else VM.badArgument(1);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isComponent(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isComponent()));
    }


    public static YoixObject
    isDatagramSocket(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isDatagramSocket()));
    }


    public static YoixObject
    isDictionary(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isDictionary()));
    }


    public static YoixObject
    isDouble(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isDouble()));
    }


    public static YoixObject
    isEdge(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isEdge()));
    }


    public static YoixObject
    isEvent(YoixObject arg[]) {

	return(YoixObject.newInt(T_EVENT.equals(arg[0].getTypename())));
    }


    public static YoixObject
    isFile(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isFile()));
    }


    public static YoixObject
    isFont(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isFont()));
    }


    public static YoixObject
    isFunction(YoixObject arg[]) {

	boolean  result = false;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1 || arg[1].isInteger()) {
		result = arg[0].isFunction();
		if (result && arg.length == 2)
		    result = arg[0].callable(arg[1].intValue());
	    } else VM.badArgument(1);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isGraph(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isGraph()));
    }


    public static YoixObject
    isHashtable(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isHashtable()));
    }


    public static YoixObject
    isImage(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isImage()));
    }


    public static YoixObject
    isInt(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isInteger()));
    }


    public static YoixObject
    isLayoutManager(YoixObject arg[]) {

	return(YoixObject.newInt(T_LAYOUTMANAGER.equals(arg[0].getTypename())));
    }


    public static YoixObject
    isMatrix(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isMatrix()));
    }


    public static YoixObject
    isNode(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isNode()));
    }


    public static YoixObject
    isNumber(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isNumber()));
    }


    public static YoixObject
    isOption(YoixObject arg[]) {

	return(YoixObject.newInt(T_OPTION.equals(arg[0].getTypename())));
    }


    public static YoixObject
    isParseTree(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isParseTree()));
    }


    public static YoixObject
    isPointer(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isPointer()));
    }


    public static YoixObject
    isProcess(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isProcess()));
    }


    public static YoixObject
    isSecurityManager(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isSecurityManager()));
    }


    public static YoixObject
    isServerSocket(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isServerSocket()));
    }


    public static YoixObject
    isSocket(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isSocket()));
    }


    public static YoixObject
    isStream(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isStream()));
    }


    public static YoixObject
    isString(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isString()));
    }


    public static YoixObject
    isStringStream(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isStringStream()));
    }


    public static YoixObject
    isThread(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isThread()));
    }


    public static YoixObject
    isURL(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isURL()));
    }


    public static YoixObject
    isVector(YoixObject arg[]) {

	return(YoixObject.newInt(arg[0].isVector()));
    }
}

