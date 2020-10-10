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
class YoixModuleCtype extends YoixModule

{

    static String  $MODULENAME = M_CTYPE;

    //
    // C-style ASCII character testing module.
    //

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "15",                $LIST,      $RORO, $MODULENAME,
       "isalnum",            "1",                 $BUILTIN,   $LR_X, null,
       "isalpha",            "1",                 $BUILTIN,   $LR_X, null,
       "isascii",            "1",                 $BUILTIN,   $LR_X, null,
       "iscntrl",            "1",                 $BUILTIN,   $LR_X, null,
       "isdigit",            "1",                 $BUILTIN,   $LR_X, null,
       "isgraph",            "1",                 $BUILTIN,   $LR_X, null,
       "islower",            "1",                 $BUILTIN,   $LR_X, null,
       "isoctal",            "1",                 $BUILTIN,   $LR_X, null,
       "isprint",            "1",                 $BUILTIN,   $LR_X, null,
       "ispunct",            "1",                 $BUILTIN,   $LR_X, null,
       "isspace",            "1",                 $BUILTIN,   $LR_X, null,
       "isupper",            "1",                 $BUILTIN,   $LR_X, null,
       "isxdigit",           "1",                 $BUILTIN,   $LR_X, null,
       "tolower",            "1",                 $BUILTIN,   $LR_X, null,
       "toupper",            "1",                 $BUILTIN,   $LR_X, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleCtype Methods
    //
    ///////////////////////////////////

    public static YoixObject
    isalnum(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isalnum(ch)));
    }


    public static YoixObject
    isalpha(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isalpha(ch)));
    }


    public static YoixObject
    isascii(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isascii(ch)));
    }


    public static YoixObject
    iscntrl(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.iscntrl(ch)));
    }


    public static YoixObject
    isdigit(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isdigit(ch)));
    }


    public static YoixObject
    isgraph(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isgraph(ch)));
    }


    public static YoixObject
    islower(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.islower(ch)));
    }


    public static YoixObject
    isoctal(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isoctal(ch)));
    }


    public static YoixObject
    isprint(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isprint(ch)));
    }


    public static YoixObject
    ispunct(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.ispunct(ch)));
    }


    public static YoixObject
    isspace(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isspace(ch)));
    }


    public static YoixObject
    isupper(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isupper(ch)));
    }


    public static YoixObject
    isxdigit(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.isxdigit(ch)));
    }


    public static YoixObject
    tolower(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.tolower(ch)));
    }


    public static YoixObject
    toupper(YoixObject arg[]) {

	int	ch = -1;

	if (arg[0].isInteger())
	    ch = arg[0].intValue();
	else VM.badArgument(0);

	return(YoixObject.newInt(YoixMiscCtype.toupper(ch)));
    }
}

