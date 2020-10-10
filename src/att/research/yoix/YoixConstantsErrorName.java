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

public
interface YoixConstantsErrorName {

    //
    // Standard error names - separate so they're easy to load into
    // a dictionary at boot time.
    //

    public static final String  ACCESSCONFLICT = "accessconflict";
    public static final String  BADARGUMENT = "badargument";
    public static final String  BADCALL = "badcall";
    public static final String  BADDECLARATION = "baddeclaration";
    public static final String  BADENCODING = "badencoding";
    public static final String  BADFIELDNAME = "badfieldname";
    public static final String  BADFONT = "badfont";
    public static final String  BADGLOBALBLOCK = "badglobalblock";
    public static final String  BADIMPORT = "badimport";
    public static final String  BADMATRIX = "badmatrix";
    public static final String  BADMENUITEM = "badmenuitem";
    public static final String  BADOPERAND = "badoperand";
    public static final String  BADRESTRICTEDBLOCK = "badrestrictedblock";
    public static final String  BADTYPENAME = "badtypename";
    public static final String  BADURL = "badurl";
    public static final String  BADVALUE = "badvalue";
    public static final String  BINDINGERROR = "bindingerror";
    public static final String  BLOCKFULL = "blockfull";
    public static final String  COMPILERERROR = "compilererror";
    public static final String  DICTFULL = "dictfull";
    public static final String  DUPLICATECASE = "duplicatecase";
    public static final String  DUPLICATETAG = "duplicatetag";
    public static final String  ERRORLOOP = "errorloop";
    public static final String  EXCEPTION = "exception";
    public static final String  HTMLERROR = "htmlerror";
    public static final String  ILLEGALJUMP = "illegaljump";
    public static final String  INTERNALERROR = "internalerror";
    public static final String  INVALIDACCESS = "invalidaccess";
    public static final String  INVALIDASSIGNMENT = "invalidassignment";
    public static final String  INVALIDCLASS = "invalidclass";
    public static final String  INVALIDCLONE = "invalidclone";
    public static final String  INVALIDFIELD = "invalidfield";
    public static final String  INVALIDSCRIPT = "invalidscript";
    public static final String  INVALIDSUPERCLASS = "invalidsuperclass";
    public static final String  LIMITCHECK = "limitcheck";
    public static final String  LOADERERROR = "loadererror";
    public static final String  MISSINGVALUE = "missingvalue";
    public static final String  MODULESIZEERROR = "modulesizeerror";
    public static final String  NAMECLASH = "nameclash";
    public static final String  NOCURRENTPOINT = "nocurrentpoint";
    public static final String  NULLPOINTER = "nullpointer";
    public static final String  OPTIONERROR = "optionerror";
    public static final String  RANGECHECK = "rangecheck";
    public static final String  REDECLARATION = "redeclaration";
    public static final String  REGEXPBADINITIALIZER = "regexpbadinitializer";
    public static final String  REGEXPSTACKUNDERFLOW = "regexpstackunderflow";
    public static final String  REGEXPSYNTAXERROR = "regexpsyntaxerror";
    public static final String  REGEXPUNDEFINED = "regexpundefined";
    public static final String  RESTRICTEDACCESS = "restrictedaccess";
    public static final String  SCANNERERROR = "scannererror";
    public static final String  SECURITYCHECK = "securitycheck";
    public static final String  SECURITYOPTION = "securityoption";
    public static final String  STACKUNDERFLOW = "stackunderflow";
    public static final String  SYNTAXERROR = "syntaxerror";
    public static final String  TYPECHECK = "typecheck";
    public static final String  UNCLOSEDCOMMENT = "unclosedcomment";
    public static final String  UNCLOSEDSTRING = "unclosedstring";
    public static final String  UNDEFINED = "undefined";
    public static final String  UNDEFINEDBUILTIN = "undefinedbuiltin";
    public static final String  UNDEFINEDCLASS = "undefinedclass";
    public static final String  UNDEFINEDRESULT = "undefinedresult";
    public static final String  UNIMPLEMENTED = "unimplemented";
    public static final String  UNREADABLEFILE = "unreadablefile";
    public static final String  UNSETVALUE = "unsetvalue";
    public static final String  UNSUPPORTEDOPERATION = "unsupportedoperation";
    public static final String  UNWRITABLEFILE = "unwritablefile";

    //
    // Used to mark areas that don't qork properly and need further
    // invesitgation. Should only be a temporary addition!!!
    //

    public static final String  INVESTIGATING = "investigating";
}

