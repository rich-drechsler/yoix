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
interface YoixConstantsError

    extends YoixConstantsErrorName

{

    //
    // Error type strings.
    //

    public static final String  ABORTERROR = "Error";
    public static final String  FATALERROR = "FatalError";
    public static final String  JAVAERROR = "JavaError";
    public static final String  WARNINGMESSAGE = "Warning";

    //
    // Error identification strings.
    //

    public static final String  OFFENDINGARGUMENT = "Argument";
    public static final String  OFFENDINGBUILTIN = "Builtin";
    public static final String  OFFENDINGCLASS = "Class";
    public static final String  OFFENDINGCOUNT = "Count";
    public static final String  OFFENDINGENTRY = "Entry";
    public static final String  OFFENDINGEXPECTED = "Expected";
    public static final String  OFFENDINGFILE = "File";
    public static final String  OFFENDINGFUNCTION = "Function";
    public static final String  OFFENDINGINDEX = "Index";
    public static final String  OFFENDINGINFO = "Info";
    public static final String  OFFENDINGMESSAGE = "Message";
    public static final String  OFFENDINGNAME = "Name";
    public static final String  OFFENDINGREQUIRED = "Required";
    public static final String  OFFENDINGVALUE = "Value";

    //
    // Stack dump prefixes.
    //

    public static final String  PREFIX_JAVASTACKTRACE = "Java Stack Trace:";
    public static final String  PREFIX_YOIXCALLSTACKTRACE = "Yoix Call Stack:";
    public static final String  PREFIX_YOIXSTACKDUMP = "Yoix Stack Dump:";
    public static final String  PREFIX_YOIXSTACKTRACE = "Yoix Stack Trace:";

    //
    // Stack dump models - Yoix stack only.
    //

    public static final int  MODEL_NONE = 0;
    public static final int  MODEL_YOIXCALLSTACKTRACE = 1;
    public static final int  MODEL_YOIXSTACKTRACE = 2;
    public static final int  MODEL_YOIXSTACKDUMP = 3;
}

