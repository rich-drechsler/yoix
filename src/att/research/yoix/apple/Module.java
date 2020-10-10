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

package att.research.yoix.apple;
import java.awt.Point;
import att.research.yoix.*;

public abstract
class Module extends YoixModule

    implements Constants

{

    public static Object appl = null;
    public static Object listener = null;

    static {
	try {
	    appl = YoixReflect.invoke("com.apple.eawt.Application", "getApplication");
	    if (appl != null) {
		listener = new AppleApplicationAdapter();
		if (listener != null) {
		    YoixReflect.invoke(appl, "addApplicationListener", new Object[] { listener }, new Class[] { YoixReflect.getClassForName("com.apple.eawt.ApplicationListener") } );
		} else VM.abort(INTERNALERROR);
	    }
	}
	catch(ExceptionInInitializerError e) {
	    // if there is a problem, e.g., a security issue,
	    // just carry on without it, i.e., appl is null
	}
    }

    public static final String  $MODULENAME = "apple";
    public static final String  $MODULECREATED = "Wed Aug 18 09:08:08 EDT 2006";
    public static final String  $MODULENOTICE = YOIXNOTICE;
    public static final String  $MODULEVERSION = "0.1";

    public static Object  $module[] = {
    //
    // NAME                          ARG                  COMMAND     MODE   REFERENCE
    // ----                          ---                  -------     ----   ---------
       $MODULENAME,                  "9",                 $LIST,      $RORO, $MODULENAME,
       "appleApplication",           "",                  $BUILTIN,   $LR_X, null,
       "appleFindFolder",            "-1",                $BUILTIN,   $LR_X, null,
       "appleGetFileCreator",        "1",                 $BUILTIN,   $LR_X, null,
       "appleGetFileType",           "1",                 $BUILTIN,   $LR_X, null,
       "appleGetResource",           "-1",                $BUILTIN,   $LR_X, null,
       "appleOpenURL",               "1",                 $BUILTIN,   $LR_X, null,
       "appleSetFileCreator",        "2",                 $BUILTIN,   $LR_X, null,
       "appleSetFileType",           "2",                 $BUILTIN,   $LR_X, null,
       "appleSetFileTypeAndCreator", "3",                 $BUILTIN,   $LR_X, null,
    };

    ///////////////////////////////////
    //
    // Module Methods
    //
    ///////////////////////////////////

    public static YoixObject
    appleApplication(YoixObject arg[]) {

	YoixObject  yret = null;
	YoixObject  cl;
	boolean     bv;
	Object      ret;
	String      op;
	String      name;
	String      cmd;

	if (appl != null) {
	    if (arg.length == 0)
		yret = YoixObject.newInt(true);
	    else {
		name = cmd = op = null;
		if (arg[0].isString()) {
		    name = arg[0].stringValue();
		} else VM.badArgument(0);
		if (arg.length == 1) {
		    if (name.equalsIgnoreCase(NL_ABOUT))
			op = "isAboutMenuItemPresent";
		    else if (name.equalsIgnoreCase(NL_MOUSELOCATION))
			op = "getMouseLocationOnScreen";
		    else if (name.equalsIgnoreCase(NL_PREFERENCES))
			op = "isPreferencesMenuItemPresent";
		    else VM.badArgumentValue(0);
		    ret = YoixReflect.invoke(appl, op);
		    if (ret instanceof Boolean)
			yret = YoixObject.newInt(((Boolean)ret).booleanValue());
		    else if (ret instanceof Point)
			yret = YoixMakeScreen.yoixPoint(((Point)ret));
		    else VM.abort(INTERNALERROR); // should never happen
		} else if (arg.length == 2) {
		    if (arg[1].notNull() && arg[1].isString()) {
			cmd = arg[1].stringValue();
			if (cmd.equalsIgnoreCase(NY_ENABLED)) {
			    if (name.equalsIgnoreCase(NL_ABOUT)) {
				op = "getEnabledAboutMenu";
			    } else if (name.equalsIgnoreCase(NL_PREFERENCES)) {
				op = "getEnabledPreferencesMenu";
			    } else VM.badArgumentValue(0);
			} else if (cmd.equalsIgnoreCase(NY_REMOVE)) {
			    if (name.equalsIgnoreCase(NL_ABOUT)) {
				op = "removeAboutMenuItem";
			    } else if (name.equalsIgnoreCase(NL_PREFERENCES)) {
				op = "removePreferencesMenuItem";
			    } else VM.badArgumentValue(0);
			} else VM.badArgumentValue(1);
			ret = YoixReflect.invoke(appl, op);
			if (ret instanceof Boolean)
			    yret = YoixObject.newInt(((Boolean)ret).booleanValue());
		    } else if (arg[1].isInteger()) {
			bv = arg[1].booleanValue();
			if (name.equalsIgnoreCase(NL_ABOUT)) {
			    op = "setEnabledAboutMenu";
			} else if (name.equalsIgnoreCase(NL_PREFERENCES)) {
			    op = "setEnabledPreferencesMenu";
			} else VM.badArgumentValue(0);
			ret = YoixReflect.invoke(appl, op, bv);
			// ret is always null
		    } else if (arg[1].isNull() || arg[1].isCallable()) {
			if (arg[1].callable(1)) {
			    if (arg[1].isNull())
				cl = null;
			    else cl = arg[1];

			    if (name.equalsIgnoreCase(NL_ABOUT)) {
				((AppleApplicationAdapter)listener).setAboutHandler(cl);
			    } else if (name.equalsIgnoreCase(NL_OPENAPPLICATION)) {
				((AppleApplicationAdapter)listener).setOpenApplicationHandler(cl);
			    } else if (name.equalsIgnoreCase(NL_OPENFILE)) {
				((AppleApplicationAdapter)listener).setOpenFileHandler(cl);
			    } else if (name.equalsIgnoreCase(NL_PREFERENCES)) {
				((AppleApplicationAdapter)listener).setPreferencesHandler(cl);
			    } else if (name.equalsIgnoreCase(NL_PRINTFILE)) {
				((AppleApplicationAdapter)listener).setPrintFileHandler(cl);
			    } else if (name.equalsIgnoreCase(NL_QUIT)) {
				((AppleApplicationAdapter)listener).setQuitHandler(cl);
			    } else if (name.equalsIgnoreCase(NL_REOPENAPPLICATION)) {
				((AppleApplicationAdapter)listener).setReOpenApplicationHandler(cl);
			    } else VM.badArgumentValue(0);
			} else VM.badArgumentValue(1);
		    } else VM.badArgument(1);
		} else VM.badCall();
	    }
	} else if (arg.length == 0)
	    yret = YoixObject.newInt(false);

	return(yret == null ? YoixObject.newNull() : yret);
    }


    public static YoixObject
    appleFindFolder(YoixObject arg[]) {

	YoixObject  yret = null;
	Object      ret;
	int         nbr;

	if (appl != null) {
	    if (arg.length == 1) {
		if (arg[0].isInteger()) {
		    ret = YoixReflect.invoke(APPLE_FILEMANAGER, "findFolder", arg[0].intValue());
		    yret = YoixObject.newString((String)ret);
		} else VM.badArgument(0);
	    } else if (arg.length == 2) {
		if (arg[0].isInteger()) {
		    nbr = arg[0].intValue();
		    if (nbr >= (int)(Short.MIN_VALUE) && nbr <= (int)(Short.MAX_VALUE)) {
			if (arg[1].isInteger()) {
			    ret = YoixReflect.invoke(APPLE_FILEMANAGER, "findFolder", new Object[] { new Short((short)nbr), new Integer(arg[1].intValue()) });
			    yret = YoixObject.newString((String)ret);
			} else VM.badArgument(1);
		    } else VM.badArgumentValue(0);
		} else VM.badArgument(0);
	    } else VM.badCall();
	}

	return(yret == null ? YoixObject.newNull() : yret);
    }


    public static YoixObject
    appleGetFileCreator(YoixObject arg[]) {

	YoixObject  yret = null;
	Object      ret;

	if (appl != null) {
	    if (arg[0].isNull())
		VM.badArgumentValue(0);
	    else if (arg[0].isString()) {
		ret = YoixReflect.invoke(APPLE_FILEMANAGER, "getFileCreator", arg[0].stringValue());
		yret = YoixObject.newInt(((Integer)ret).intValue());
	    } else VM.badArgument(0);
	}

	return(yret == null ? YoixObject.newNull() : yret);
    }


    public static YoixObject
    appleGetFileType(YoixObject arg[]) {

	YoixObject  yret = null;
	Object      ret;

	if (appl != null) {
	    if (arg[0].isNull())
		VM.badArgumentValue(0);
	    else if (arg[0].isString()) {
		ret = YoixReflect.invoke(APPLE_FILEMANAGER, "getFileType", arg[0].stringValue());
		yret = YoixObject.newInt(((Integer)ret).intValue());
	    } else VM.badArgument(0);
	}

	return(yret == null ? YoixObject.newNull() : yret);
    }


    public static YoixObject
    appleGetResource(YoixObject arg[]) {

	YoixObject  yret = null;
	Object      ret;

	if (appl != null) {
	    if (arg.length == 1) {
		if (arg[0].isNull())
		    VM.badArgumentValue(0);
		else if (arg[0].isString()) {
		    ret = YoixReflect.invoke(APPLE_FILEMANAGER, "getResource", arg[0].stringValue());
		    yret = YoixObject.newString((String)ret);
		} else VM.badArgument(0);
	    } else if (arg.length == 2) {
		if (arg[0].isNull())
		    VM.badArgumentValue(0);
		else if (arg[0].isString()) {
		    if (arg[1].isNull())
			VM.badArgumentValue(1);
		    else if (arg[1].isString()) {
			ret = YoixReflect.invoke(APPLE_FILEMANAGER, "getResource", new Object[] { arg[0].stringValue(), arg[1].stringValue() });
			yret = YoixObject.newString((String)ret);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else VM.badCall();
	}

	return(yret == null ? YoixObject.newNull() : yret);
    }


    public static YoixObject
    appleOpenURL(YoixObject arg[]) {

	Object  ret;

	if (appl != null) {
	    if (arg[0].isNull())
		VM.badArgumentValue(0);
	    else if (arg[0].isString()) {
		ret = YoixReflect.invoke(APPLE_FILEMANAGER, "openURL", arg[0].stringValue());
	    } else VM.badArgument(0);
	}

	return(null);
    }


    public static YoixObject
    appleSetFileCreator(YoixObject arg[]) {

	Object  ret;

	if (appl != null) {
	    if (arg[0].isNull())
		VM.badArgumentValue(0);
	    else if (arg[0].isString()) {
		if (arg[1].isInteger()) {
		    ret = YoixReflect.invoke(APPLE_FILEMANAGER, "setFileCreator", new Object[] { arg[0].stringValue(), new Integer(arg[1].intValue()) } );
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	}

	return(null);
    }


    public static YoixObject
    appleSetFileType(YoixObject arg[]) {

	YoixObject  yret = null;
	Object      ret;

	if (appl != null) {
	    if (arg[0].isNull())
		VM.badArgumentValue(0);
	    else if (arg[0].isString()) {
		if (arg[1].isInteger()) {
		    ret = YoixReflect.invoke(APPLE_FILEMANAGER, "setFileType", new Object[] { arg[0].stringValue(), new Integer(arg[1].intValue()) } );
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	}

	return(yret == null ? YoixObject.newNull() : yret);
    }


    public static YoixObject
    appleSetFileTypeAndCreator(YoixObject arg[]) {

	YoixObject  yret = null;
	Object      ret;

	if (appl != null) {
	    if (arg[0].isNull())
		VM.badArgumentValue(0);
	    else if (arg[0].isString()) {
		if (arg[1].isInteger()) {
		    if (arg[2].isInteger()) {
			ret = YoixReflect.invoke(APPLE_FILEMANAGER, "setFileTypeAndCreator", new Object[] { arg[0].stringValue(), new Integer(arg[1].intValue()), new Integer(arg[2].intValue()) } );
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	}

	return(yret == null ? YoixObject.newNull() : yret);
    }
}

