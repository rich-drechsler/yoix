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
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

abstract
class YoixModuleSystem extends YoixModule

{

    static String  $MODULENAME = M_SYSTEM;

    private static final int  R_OK = 0x04;
    private static final int  W_OK = 0x02;
    private static final int  X_OK = 0x01;
    private static final int  F_OK = 0x00;

    //
    // Had trouble with user.timezone in recent versions of Java, but
    // TimeZone.getDefault().getID() seems reliable. We compensate in
    // getProperties(), getProperty(), and USERTIMEZONE, which are all
    // available in yoix.system.
    //

    private static final String  UTZ = (USERTIMEZONE != null && USERTIMEZONE.length() > 0) ? USERTIMEZONE : YoixMiscTime.getDefaultTimeZone().getID();

    //
    // We use GROWABLE in setPermissions() and only care that it doesn't
    // conflict with the EXECUTE, WRITE, READ, and LOCK flags.
    //

    private static final short  GROWABLE = ~LRWX;

    static Object  $module[] = {
    //
    // NAME                         ARG                  COMMAND     MODE   REFERENCE
    // ----                         ---                  -------     ----   ---------
       null,                        "84",                $LIST,      $RORO, $MODULENAME,
       "FILESEPARATOR",             FILESEP,             $STRING,    $RORO, null,
       "F_OK",                      new Integer(F_OK),   $INTEGER,   $LR__, null,
       "ISMAC",                     new Boolean(ISMAC),  $INTEGER,   $LR__, null,
       "ISUNIX",                    new Boolean(ISUNIX), $INTEGER,   $LR__, null,
       "ISWIN",                     new Boolean(ISWIN),  $INTEGER,   $LR__, null,
       "LINESEPARATOR",             NL,                  $STRING,    $RORO, null,
       "OSARCH",                    OSARCH,              $STRING,    $RORO, null,
       "OSNAME",                    OSNAME,              $STRING,    $RORO, null,
       "OSVERSION",                 OSVERSION,           $STRING,    $RORO, null,
       "PATHSEPARATOR",             PATHSEP,             $STRING,    $RORO, null,
       "R_OK",                      new Integer(R_OK),   $INTEGER,   $LR__, null,
       "USERDIR",                   USERDIR,             $STRING,    $RORO, null,
       "USERHOME",                  USERHOME,            $STRING,    $RORO, null,
       "USERNAME",                  USERNAME,            $STRING,    $RORO, null,
       "USERTIMEZONE",              UTZ,                 $STRING,    $RORO, null,
       "W_OK",                      new Integer(W_OK),   $INTEGER,   $LR__, null,
       "X_OK",                      new Integer(X_OK),   $INTEGER,   $LR__, null,

       "access",                    "2",                 $BUILTIN,   $LR_X, null,
       "addShutdownHook",           "-1",                $BUILTIN,   $LR_X, null,
       "appendClasspath",           "1",                 $BUILTIN,   $LR_X, null,
       "checkAccept",               "2",                 $BUILTIN,   $LR_X, null,
       "checkConnect",              "2",                 $BUILTIN,   $LR_X, null,
       "checkCreateRobot",          "0",                 $BUILTIN,   $LR_X, null,
       "checkDelete",               "1",                 $BUILTIN,   $LR_X, null,
       "checkExec",                 "1",                 $BUILTIN,   $LR_X, null,
       "checkExit",                 "",                  $BUILTIN,   $LR_X, null,
       "checkListen",               "1",                 $BUILTIN,   $LR_X, null,
       "checkMulticast",            "1",                 $BUILTIN,   $LR_X, null,
       "checkPropertiesAccess",     "0",                 $BUILTIN,   $LR_X, null,
       "checkRead",                 "1",                 $BUILTIN,   $LR_X, null,
       "checkReadDisplayPixels",    "0",                 $BUILTIN,   $LR_X, null,
       "checkReadEnvironment",      "1",                 $BUILTIN,   $LR_X, null,
       "checkReadProperty",         "1",                 $BUILTIN,   $LR_X, null,
       "checkSystemClipboardAccess","0",                 $BUILTIN,   $LR_X, null,
       "checkWrite",                "1",                 $BUILTIN,   $LR_X, null,
       "checkWriteProperty",        "1",                 $BUILTIN,   $LR_X, null,
       "currentTimeMillis",         "0",                 $BUILTIN,   $LR_X, null,
       "directoryListing",          "-1",                $BUILTIN,   $LR_X, null,
       "exec",                      "-1",                $BUILTIN,   $LR_X, null,
       "fileModified",              "1",                 $BUILTIN,   $LR_X, null,
       "fileSize",                  "1",                 $BUILTIN,   $LR_X, null,
       "freeMemory",                "0",                 $BUILTIN,   $LR_X, null,
       "gc",                        "0",                 $BUILTIN,   $LR_X, null,
       "getenv",                    "",                  $BUILTIN,   $LR_X, null,
       "getErrorCount",             "0",                 $BUILTIN,   $LR_X, null,
       "getErrorLimit",             "0",                 $BUILTIN,   $LR_X, null,
       "getModuleCreated",          "1",                 $BUILTIN,   $LR_X, null,
       "getModuleNotice",           "1",                 $BUILTIN,   $LR_X, null,
       "getModuleVersion",          "1",                 $BUILTIN,   $LR_X, null,
       "getProperties",             "",                  $BUILTIN,   $LR_X, null,
       "getProperty",               "-1",                $BUILTIN,   $LR_X, null,
       "getResource",               "1",                 $BUILTIN,   $LR_X, null,
       "getSavedExceptionCount",    "0",                 $BUILTIN,   $LR_X, null,
       "getSavedExceptions",        "0",                 $BUILTIN,   $LR_X, null,
       "getSystemClipboard",        "0",                 $BUILTIN,   $LR_X, null,
       "hideSystemSplashScreen",    "0",                 $BUILTIN,   $LR_X, null,
       "isDirectoryPath",           "1",                 $BUILTIN,   $LR_X, null,
       "isFilePath",                "1",                 $BUILTIN,   $LR_X, null,
       "isGrowable",                "-1",                $BUILTIN,   $LR_X, null,
       "isShutdownThread",          "0",                 $BUILTIN,   $LR_X, null,
       "localPath",                 "1",                 $BUILTIN,   $LR_X, null,
       "mkdir",                     "1",                 $BUILTIN,   $LR_X, null,
       "mkdirs",                    "1",                 $BUILTIN,   $LR_X, null,
       "nanoTime",                  "0",                 $BUILTIN,   $LR_X, null,
       "realPath",                  "1",                 $BUILTIN,   $LR_X, null,
       "rename",                    "2",                 $BUILTIN,   $LR_X, null,
       "rmdir",                     "1",                 $BUILTIN,   $LR_X, null,
       "runFinalization",           "0",                 $BUILTIN,   $LR_X, null,
       "setErrorCount",             "1",                 $BUILTIN,   $LR_X, null,
       "setErrorLimit",             "1",                 $BUILTIN,   $LR_X, null,
       "setPermissions",            "-2",                $BUILTIN,   $LR_X, null,
       "setProperty",               "2",                 $BUILTIN,   $LR_X, null,
       "getSavedExceptionLimit",    "1",                 $BUILTIN,   $LR_X, null,
       "setSecurityChecker",        "2",                 $BUILTIN,   $LR_X, null,
       "setSecurityManager",        "1",                 $BUILTIN,   $LR_X, null,
       "showSystemSplashScreen",    "",                  $BUILTIN,   $LR_X, null,
       "stat",                      "-1",                $BUILTIN,   $LR_X, null,
       "time",                      "",                  $BUILTIN,   $LR_X, null,
       "totalMemory",               "0",                 $BUILTIN,   $LR_X, null,
       "traceInstructions",         "1",                 $BUILTIN,   $LR_X, null,
       "traceMethodCalls",          "1",                 $BUILTIN,   $LR_X, null,
       "unlink",                    "1",                 $BUILTIN,   $LR_X, null,
       "waitFor",                   "1",                 $BUILTIN,   $LR_X, null,
       "yoixPath",                  "1",                 $BUILTIN,   $LR_X, null,

       T_CLIPBOARD,                 "6",                 $DICT,      $L___, T_CLIPBOARD,
       null,                        "-1",                $GROWTO,    null,  null,
       N_MAJOR,                     $CLIPBOARD,          $INTEGER,   $LR__, null,
       N_MINOR,                     "0",                 $INTEGER,   $LR__, null,
       N_CONTENTS,                  T_OBJECT,            $NULL,      $LR__, null,
       N_NAME,                      T_STRING,            $NULL,      $RW_,  null,
       N_SETCONTENTS,               T_CALLABLE,          $NULL,      $L__X, null,
       N_OWNER,                     T_OBJECT,            $NULL,      $LR__, null,

       T_PROCESS,                   "12",                $DICT,      $L___, T_PROCESS,
       N_MAJOR,                     $PROCESS,            $INTEGER,   $LR__, null,
       N_MINOR,                     "0",                 $INTEGER,   $LR__, null,
       N_ALIVE,                     $FALSE,              $INTEGER,   $RW_,  null,
       N_COMMAND,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_DIRECTORY,                 T_STRING,            $NULL,      $RW_,  null,
       N_ENVP,                      T_ARRAY,             $NULL,      $RW_,  null,
       N_ERROR,                     T_STREAM,            $NULL,      $LR__, null,
       N_INPUT,                     T_STREAM,            $NULL,      $LR__, null,
       N_OUTPUT,                    T_STREAM,            $NULL,      $LR__, null,
       N_PARENT,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_PERSISTENT,                $FALSE,              $INTEGER,   $RW_,  null,
       N_EXITVALUE,                 $NAN,                $NUMBER,    $LR__, null,

       T_SECURITYMANAGER,           "26",                $DICT,      $L___, T_SECURITYMANAGER,
       N_MAJOR,                     $SECURITYMANAGER,    $INTEGER,   $LR__, null,
       N_MINOR,                     "0",                 $INTEGER,   $LR__, null,
       N_CHECKACCEPT,               T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKCONNECT,              T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKCREATEROBOT,          T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKDELETE,               T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKEXEC,                 T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKEXIT,                 T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKLISTEN,               T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKMULTICAST,            T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKPROPERTIESACCESS,     T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKREAD,                 T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKREADDISPLAYPIXELS,    T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKREADENVIRONMENT,      T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKREADPROPERTY,         T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKSYSTEMCLIPBOARDACCESS,T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKWRITE,                T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKWRITEPROPERTY,        T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKYOIXADDPROVIDER,      T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKYOIXEVAL,             T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKYOIXEXECUTE,          T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKYOIXINCLUDE,          T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKYOIXMODULE,           T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKYOIXOPEN,             T_CALLABLE,          $NULL,      $RWX,  null,
       N_CHECKYOIXREMOVEPROVIDER,   T_CALLABLE,          $NULL,      $RWX,  null,
       N_INCHECK,                   $FALSE,              $INTEGER,   $LR__, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleSystem Methods
    //
    ///////////////////////////////////

    public static YoixObject
    access(YoixObject arg[]) {

	boolean  result = false;
	Object   executable;
	String   path;
	File     file;
	int      mode;

	//
	// Java 1.6 introduced File.canExecute(), but we're supporting
	// 1.5 too so we have to use reflection. The old implementation
	// of this builtin just checked readability when asked if a file
	// is executable, so that's what we continue to do.
	//

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[1].isInteger()) {
		if (arg[0].notNull()) {
		    path = arg[0].stringValue();
		    if (path.startsWith("file:")) {
			try {
			    file = new File(new URI(path));
			}
			catch(Exception e) {
			    file = new File(path);
			}
		    } else file = new File(path);
		    mode = arg[1].intValue();
		    result = true;
		    if (mode == 0 && file.exists() == false)
			result = false;
		    if (result && (mode&R_OK) != 0 && file.canRead() == false)
			result = false;
		    if (result && (mode&W_OK) != 0 && file.canWrite() == false)
			result = false;
		    if (result && (mode&X_OK) != 0) {
			//
			// The reflection will work if we're using 1.6,
			// otherwise behave they way previous versions
			// did and check the file's readablilty.
			//
			executable = YoixReflect.invoke(file, "canExecute", null, null);
			if (executable instanceof Boolean) {
			    if (((Boolean)executable).booleanValue() == false)
				result = false;
			} else if (executable == null && file.canRead() == false)
			    result = false;
		    }
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    addShutdownHook(YoixObject arg[]) {

	YoixObject  funct;
	YoixObject  argv[];
	YoixObject  context;
	boolean     result = false;
	int         argc;

	if (arg.length > 0) {
	    if (arg[0].isCallable() || arg[0].isCallablePointer()) {
		argc = arg.length - 1;
		if (arg[0].isCallable()) {
		    funct = arg[0];
		    context = null;
		} else {
		    funct = arg[0].get();
		    context = arg[0].compound() ? arg[0] : null;
		}
		if (funct.callable(argc)) {
		    argv = new YoixObject[argc];
		    System.arraycopy(arg, 1, argv, 0, argc);
		    result = YoixVMShutdownThread.addShutdownHook(funct, argv, context);
		} else VM.badCall();
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    appendClasspath(YoixObject arg[]) {

	ClassLoader  loader;
	String       path;
	File         file;
	URL          url;

	if (arg[0].notNull() && arg[0].isString()) {
	    //
	    // Probably still want some security manager checking?
	    //
	    loader = ClassLoader.getSystemClassLoader();
	    if (loader instanceof YoixClassLoader) {
		try {
		    path = arg[0].stringValue();
		    file = new File(path);
		    if (file.isFile() || file.isDirectory())
			url = file.toURL();
		    else url = new URL(path);
		    ((YoixClassLoader)loader).addURL(url);
		}
		catch(MalformedURLException e) {
		    VM.badArgumentValue(0);
		}
	    } else VM.abort(UNSETVALUE, new String[] {"java.system.class.loader not properly set at JVM invocation"});
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    checkAccept(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[1].isNumber()) {
		if ((sm = System.getSecurityManager()) != null) {
		    try {
			sm.checkAccept(arg[0].stringValue(), arg[1].intValue());
		    }
		    catch(SecurityException e) {
			result = false;
		    }
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkConnect(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[1].isNumber()) {
		if ((sm = System.getSecurityManager()) != null) {
		    try {
			sm.checkConnect(arg[0].stringValue(), arg[1].intValue());
		    }
		    catch(SecurityException e) {
			result = false;
		    }
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkCreateRobot(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if ((sm = System.getSecurityManager()) != null) {
	    try {
		sm.checkPermission(new AWTPermission("createRobot"));
	    }
	    catch(SecurityException e) {
		result = false;
	    }
	}

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkDelete(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkDelete(arg[0].stringValue());
		}
		catch(SecurityException e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkExec(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkExec(arg[0].stringValue());
		}
		catch(SecurityException e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkExit(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg.length == 0 || arg.length == 1) {
	    if (arg.length == 0 || arg[0].isNumber()) {
		if ((sm = System.getSecurityManager()) != null) {
		    try {
			sm.checkExit(arg.length == 1 ? arg[0].intValue() : 0);
		    }
		    catch(SecurityException e) {
			result = false;
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkListen(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isNumber()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkListen(arg[0].intValue());
		}
		catch(SecurityException e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkMulticast(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkMulticast(InetAddress.getByName(arg[0].stringValue()));
		}
		catch(SecurityException e) {
		    result = false;
		}
		catch(UnknownHostException e) {}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkPropertiesAccess(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if ((sm = System.getSecurityManager()) != null) {
	    try {
		sm.checkPropertiesAccess();
	    }
	    catch(SecurityException e) {
		result = false;
	    }
	}

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkRead(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkRead(arg[0].stringValue());
		}
		catch(SecurityException e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkReadDisplayPixels(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if ((sm = System.getSecurityManager()) != null) {
	    try {
		sm.checkPermission(new AWTPermission("readDisplayPixels"));
	    }
	    catch(SecurityException e) {
		result = false;
	    }
	}

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkReadEnvironment(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkPermission(new RuntimePermission("getenv." + arg[0].stringValue()));
		}
		catch(SecurityException e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkReadProperty(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkPropertyAccess(arg[0].stringValue());
		}
		catch(SecurityException e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkSystemClipboardAccess(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if ((sm = System.getSecurityManager()) != null) {
	    try {
		sm.checkSystemClipboardAccess();
	    }
	    catch(SecurityException e) {
		result = false;
	    }
	}

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkWrite(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkWrite(arg[0].stringValue());
		}
		catch(SecurityException e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    checkWriteProperty(YoixObject arg[]) {

	SecurityManager  sm;
	boolean          result = true;

	if (arg[0].isString() || arg[0].isNull()) {
	    if ((sm = System.getSecurityManager()) != null) {
		try {
		    sm.checkPermission(new PropertyPermission(arg[0].stringValue(), "write"));
		}
		catch(SecurityException e) {
		    result = false;
		}
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    currentTimeMillis(YoixObject arg[]) {

	return(YoixObject.newNumber(System.currentTimeMillis()));
    }


    public static YoixObject
    exec(YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  yobj;
	YoixObject  data;
	YoixObject  envp;
	File        fdir;
	int         n;
	int         len;

	if (arg.length >= 1) {
	    if (arg[0].isString() || arg[0].isArray()) {
		if (arg[0].notNull() && (len = arg[0].length()) > 0) {
		    data = VM.getTypeTemplate(T_PROCESS);
		    if (arg[0].isArray()) {
			for (n = 0; n < len; n++) {
			    yobj = arg[0].get(n, false);
			    if (!(yobj.isString() && yobj.notNull()))
				VM.badArgument(0, new String[] {"Array index", "" + n});
			}
		    }
		    data.put(N_COMMAND, arg[0]);
		    if (arg.length > 1) {
			if (arg[1].isNull() || arg[1].isArray()) {
			    if (arg[1].notNull() && arg[1].isArray() && (len = arg[1].length()) > 0) {
				envp = arg[1];
				for (n = 0; n < len; n++) {
				    yobj = envp.get(n, false);
				    if (!(yobj.isString() && yobj.notNull()))
					VM.badArgument(1, new String[] {"Array index", "" + n});
				}
				data.put(N_ENVP, envp);
			    }
			    if (arg.length == 3) {
				if (arg[2].notNull()) {
				    if (arg[2].isString()) {
					fdir = new File(arg[2].stringValue());
					if (fdir.isDirectory())
					    data.put(N_DIRECTORY, arg[2]);
					else VM.badArgumentValue(2);
				    } else VM.badArgument(2);
				}
			    } else VM.badCall();
			} else {
			    envp = YoixObject.newArray(arg.length - 1);
			    for (n = 1; n < arg.length; n++) {
				if (arg[n].isString() && arg[n].notNull())
				    envp.put(n - 1, arg[n], false);
				else VM.badArgument(n);
			    }
			    data.put(N_ENVP, envp);
			}
		    }
		    data.putInt(N_ALIVE, true);
		    obj = YoixObject.newProcess(data);
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return((obj != null && ((YoixBodyProcess)(obj.body())).active())
	    ? obj
	    : YoixObject.newProcess()
	);
    }


    public static YoixObject
    fileModified(YoixObject arg[]) {

	double  result = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    try {
		result = (double)((new File(arg[0].stringValue())).lastModified());
	    }
	    catch(RuntimeException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newDouble(result/1000.0));
    }


    public static YoixObject
    fileSize(YoixObject arg[]) {

	double  result = 0;

	if (arg[0].isString() && arg[0].notNull()) {
	    result = YoixMisc.fileSize(arg[0].stringValue());
	} else VM.badArgument(0);

	return(YoixObject.newDouble(result));
    }


    public static YoixObject
    directoryListing(YoixObject arg[]) {

	YoixObject  listing = null;
	String      result[] = null;
	File        directory = null;
	int         n;

	if (arg.length == 1 /* || arg.length == 2 */) {
	    if (arg[0].isString() && arg[0].notNull()) {
		directory = new File(arg[0].stringValue());
		if (directory.isDirectory() && directory.canRead()) {
		    if (arg.length == 2) {
			if (arg[1].isString() && arg[1].notNull()) {
			    // TODO: use YoixFilenameFilter here and
			    // use as: directory.list(yoixFilenameFilter);
			} else VM.badArgument(1);
		    } else {
			try {
			    result = directory.list();
			}
			catch(RuntimeException e) {}
		    }
		    if (result != null && result.length > 0) {
			listing = YoixObject.newArray(result.length);
			for (n = 0; n < result.length; n++)
			    listing.putString(n, result[n]);
		    }
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(listing != null ? listing : YoixObject.newArray());
    }


    public static YoixObject
    freeMemory(YoixObject arg[]) {

	return(YoixObject.newNumber(RUNTIME.freeMemory()));
    }


    public static YoixObject
    gc(YoixObject arg[]) {

	System.gc();
	return(null);
    }


    public static YoixObject
    getenv(YoixObject arg[]) {

	YoixObject  obj = null;
	Iterator    keys;
	String      name;
	String      value;
	Map         env;

	//
	// Java 1.4 throws an Exception if we try to use System.getenv(),
	// so we catch and ignore it.
	//

	if (arg.length <= 1) {
	    if (arg.length == 0 || arg[0].isNull()) {
		try {
		    env = System.getenv();
		    obj = YoixObject.newDictionary(env.size(), -1);		// let user to grow it
		    keys = env.keySet().iterator();
		    while (keys.hasNext()) {
			if ((name = (String)keys.next()) == null)
			    name = "";
			if ((value = (String)env.get(name)) == null)
			    value = "";
			obj.putString(name, value);
		    }
		}
		catch(Exception e) {}
	    } else if (arg[0].notNull() && arg[0].isString())
		try {
		    obj = YoixObject.newString(System.getenv(arg[0].stringValue()));
		}
		catch(Exception e) {}
	    else VM.badArgument(0);
	} else VM.badCall();

	return(obj == null ? YoixObject.newNull() : obj);
    }


    public static YoixObject
    getErrorCount(YoixObject arg[]) {

	return(YoixObject.newInt(VM.getErrorCount()));
    }


    public static YoixObject
    getErrorLimit(YoixObject arg[]) {

	return(YoixObject.newInt(VM.getErrorLimit()));
    }


    public static YoixObject
    getModuleCreated(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString())
	    value = YoixModule.created(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    getModuleNotice(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString())
	    value = YoixModule.notice(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    getModuleVersion(YoixObject arg[]) {

	String  value = null;

	if (arg[0].isString())
	    value = YoixModule.version(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(value));
    }


    public static YoixObject
    getProperties(YoixObject arg[]) {

	Enumeration  enm;
	YoixObject   obj = null;
	Properties   prop;
	Object       key;
	String       prefix;
	String       value;

	if (arg.length == 0 || arg.length == 1) {
	    prop = System.getProperties();
	    if (arg.length == 1) {
		if (arg[0].isString()) {
		    prefix = arg[0].stringValue();
		    prop = (Properties)prop.clone();	// required!!
		    for (enm = prop.keys(); enm.hasMoreElements(); ) {
			key = enm.nextElement();
			if (key.toString().startsWith(prefix) == false)
			    prop.remove(key);
		    }
		} else VM.badArgument(0);
	    }

	    //
	    // Provide a default for user.timezone, if needed
	    //
	    if ((value = prop.getProperty("user.timezone")) == null || value.length() == 0)
		prop.put("user.timezone", UTZ);
	    obj = YoixMisc.copyIntoDictionary(prop);
	} else VM.badCall();

	return(obj);
    }


    public static YoixObject
    getProperty(YoixObject arg[]) {

	YoixObject  result = null;
	YoixObject  yobj;
	String      key;
	String      value = null;
	int         len;
	int         m;
	int         n;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isString()) {
		if (arg.length == 1 || arg[1].isString()) {
		    key = arg[0].stringValue();
		    if ((value = System.getProperty(key)) == null) {
			if (arg.length == 2 && arg[1].notNull())
			    value = arg[1].stringValue();
		    }

		    //
		    // Provide a default for user.timezone, if needed
		    //
		    if (key.equals("user.timezone") && (value == null || value.length() == 0))
			value = UTZ;

		    result = YoixObject.newString(value);
		} else VM.badArgument(1);
	    } else if (arg[0].isArray()) {
		if (arg.length == 1) {
		    if ((len = arg[0].sizeof()) > 0) {
			result = YoixObject.newDictionary(len);
			len = arg[0].length();
			for (m = arg[0].offset(), n = 0; m < len; m++, n++) {
			    yobj = arg[0].get(m, false);
			    if (yobj.isString()) {
				key = yobj.stringValue();
				value = System.getProperty(key);
				//
				// Provide a default for user.timezone, if needed
				//
				if (key.equals("user.timezone") && (value == null || value.length() == 0))
				    value = UTZ;
				result.put(key, YoixObject.newString(value));
			    } else VM.badArgumentValue(0, n);
			}
		    } else VM.badArgumentValue(0);
		} else VM.badCall();
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(result == null ? YoixObject.newNull() : result);
    }


    public static YoixObject
    getResource(YoixObject arg[]) {

	ClassLoader  loader;
	String       resource = null;
	String       name;
	URL          url;

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[0].notNull())
		resource = YoixMisc.getResource(arg[0].stringValue());
	} else VM.badArgument(0);

	return(YoixObject.newString(resource));
    }


    public static YoixObject
    getSavedExceptionCount(YoixObject arg[]) {

	return(YoixObject.newInt(VM.getSavedExceptionCount()));
    }


    public static YoixObject
    getSavedExceptions(YoixObject arg[]) {

	YoixSimpleDateFormat  sdf;
	StringBuffer          sbuf = null;
	Object                info[];
	Object                element;
	Vector                exceptions;
	int                   length;
	int                   n;

	if ((exceptions = VM.getSavedExceptions()) != null) {
	    sbuf = new StringBuffer();
	    sdf = new YoixSimpleDateFormat(UNIX_DATE_FORMAT);
	    sdf.setTimeZone(YoixMiscTime.getDefaultTimeZone());
	    sdf.setLenient(true);
	    length = exceptions.size();
	    for (n = 0; n < length; n++) {
		element = exceptions.elementAt(n);
		if (element instanceof Object[]) {
		    info = (Object[])element;
		    if (info.length >= 4) {
			sbuf.append("[");
		 	sbuf.append(sdf.format(new Date(((Long)info[0]).longValue())));
			sbuf.append("] ");
			sbuf.append(((Thread)info[1]).getName());
			sbuf.append(": ");
			sbuf.append(YoixMisc.javaTrace((Throwable)info[2]));
			sbuf.append((String)info[3]);
			sbuf.append("\n");
		    }
		}
	    }
	}

	return(YoixObject.newString(sbuf));
    }


    public static YoixObject
    getSystemClipboard(YoixObject arg[]) {

	YoixObject  obj;

	if ((obj = VM.getSystemClipboard()) == null)
	    obj = YoixObject.newClipboard();
	return(obj);
    }


    public static YoixObject
    hideSystemSplashScreen(YoixObject arg[]) {

	YoixSplashScreen.hideSystemSplashScreen();
	return(null);
    }


    public static YoixObject
    isDirectoryPath(YoixObject arg[]) {

	boolean  result = false;
	String   path;
	File     file;

	if (arg[0].isString() && arg[0].notNull()) {
	    path = arg[0].stringValue();
	    if (path.startsWith("file:")) {
		try {
		    file = new File(new URI(path));
		}
		catch(Exception e) {
		    file = new File(path);
		}
	    } else file = new File(path);
	    result = file.isDirectory();
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isFilePath(YoixObject arg[]) {

	boolean  result = false;
	String   path;
	File     file;

	if (arg[0].isString() && arg[0].notNull()) {
	    path = arg[0].stringValue();
	    if (path.startsWith("file:")) {
		try {
		    file = new File(new URI(path));
		}
		catch(Exception e) {
		    file = new File(path);
		}
	    } else file = new File(path);
	    result = file.isFile();
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isGrowable(YoixObject arg[]) {

	boolean  result = false;
	int      incr;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1 || arg[1].isInteger()) {
		incr = (arg.length > 1) ? arg[1].intValue() : 1;
		if (incr >= 0)
		    result = arg[0].canGrowTo(arg[0].length() + incr);
		else VM.badArgumentValue(1);
	    } else VM.badArgument(1);
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isShutdownThread(YoixObject arg[]) {

	boolean  result = false;

	if (VM.isShutdown())
	    result = Thread.currentThread() instanceof YoixVMShutdownThread;
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    localPath(YoixObject arg[]) {

	String  path = null;

	if (arg[0].isString() && arg[0].notNull() && arg[0].length() > 0)
	    path = YoixMisc.toLocalPath(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(path));
    }


    public static YoixObject
    mkdir(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isString() && arg[0].notNull()) {
	    try {
		result = (new File(arg[0].stringValue())).mkdir();
	    }
	    catch(RuntimeException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    mkdirs(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isString() && arg[0].notNull()) {
	    try {
		result = (new File(arg[0].stringValue())).mkdirs();
	    }
	    catch(RuntimeException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    nanoTime(YoixObject arg[]) {

	return(YoixObject.newNumber(YoixMiscTime.getNanoTime()));
    }


    public static YoixObject
    realPath(YoixObject arg[]) {

	String  path = null;

	if (arg[0].isString() && arg[0].notNull() && arg[0].length() > 0) {
	    path = YoixMisc.toRealPath(arg[0].stringValue());
	} else VM.badArgument(0);

	return(YoixObject.newString(path));
    }


    public static YoixObject
    rename(YoixObject arg[]) {

	boolean  result = false;
	File     from;
	File     to;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isString() && arg[1].notNull()) {
		try {
		    from = new File(arg[0].stringValue());
		    to = new File(arg[1].stringValue());
		    result = from.renameTo(to);
		}
		catch(RuntimeException e) {}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    rmdir(YoixObject arg[]) {

	boolean  result = false;
	File     file;

	if (arg[0].isString() && arg[0].notNull()) {
	    try {
		file = new File(arg[0].stringValue());
		result = file.isDirectory() && file.delete();
	    }
	    catch(RuntimeException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    runFinalization(YoixObject arg[]) {

	System.runFinalization();
	return(null);
    }


    public static YoixObject
    setErrorCount(YoixObject arg[]) {

	int  value = 0;

	if (arg[0].isInteger())
	    value = VM.setErrorCount(arg[0].intValue());
	else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    setErrorLimit(YoixObject arg[]) {

	int  value = 0;

	if (arg[0].isInteger())
	    value = VM.setErrorLimit(arg[0].intValue());
	else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    setPermissions(YoixObject arg[]) {

	YoixObject  yobj;
	boolean     recurse = false;
	boolean     negate = false;
	short       addPerms = 0;
	short       delPerms = 0;
	short       perm = 0;
	char        chars[] = null;
	int         idx = 1;
	int         n;

	//
	// Not finished or documented, so it could change or disappear,
	// so you probably shouldn't use it until it's documented.
	//

	if (arg[0].isInteger() || (arg[0].notNull() && arg[0].isString())) {
	    if (arg[0].isInteger()) {
		idx = 2;
		recurse = arg[0].booleanValue();
		if (arg[1].notNull() && arg[1].isString())
		    chars = arg[1].stringValue().toCharArray();
		else VM.badArgument(1);
	    } else{
		chars = arg[0].stringValue().toCharArray();
		if (arg[1].isInteger()) {
		    idx = 2;
		    recurse = arg[1].booleanValue();
		}
	    }
	    if (arg.length > idx) {
		for (n = 0; n < chars.length; n++) {
		    if (chars[n] == '+')
			negate = false;
		    else if (chars[n] == '-')
			negate = true;
		    else VM.abort(BADVALUE, new String(chars), n);
		    if (++n < chars.length) {
			switch(chars[n]) {
			    case 'g':
				perm = GROWABLE;
				break;

			    case 'r': // READ TODO maybe
			    case 'w': // WRITE TODO maybe
			    case 'x': // EXECUTE TODO maybe
			    case 'l': // LOCK TODO maybe
			    default:
				VM.abort(BADVALUE, new String(chars), n);
				break;
			}
			if (negate)
			    delPerms |= perm;
			else addPerms |= perm;
		    } else VM.abort(BADVALUE, new String(chars), n);
		}
		// remove change or adding/deleting same permission
		perm = (short)(addPerms & delPerms);
		addPerms ^= perm;
		delPerms ^= perm;
		while (idx < arg.length) {
		    yobj = arg[idx++];
		    if (yobj.notNull() && yobj.isPointer())
			recurseSetPermissions(yobj, addPerms, delPerms, recurse);
		    else VM.badArgument(idx-1);
		}
	    } else VM.badCall();
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    setProperty(YoixObject arg[]) {

	String  key;
	String  value;

	if (arg[0].notNull() && arg[0].isString()) {
	    if (arg[1].isString()) {
		key = arg[0].stringValue();
		if (key.length() > 0) {
		    value = arg[1].stringValue();
		    System.setProperty(key, value);
		} else VM.badArgumentValue(0);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    setSavedExceptionCount(YoixObject arg[]) {

	if (arg[0].isNumber())
	    VM.setSavedExceptionLimit(arg[0].intValue());
	else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    setSecurityChecker(YoixObject arg[]) {

	boolean result = false;

	if (arg[0].isString() && arg[0].notNull()) {
	    if (arg[1].isFunction() && arg[1].notNull())
		result = VM.setSecurityChecker(arg[0].stringValue(), arg[1]);
	    else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    setSecurityManager(YoixObject arg[]) {

	boolean result = false;

	if (arg[0].isSecurityManager() || arg[0].isNull())
	    result = VM.setSecurityManager(arg[0]);
	else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    showSystemSplashScreen(YoixObject arg[]) {

	//
	// If the fourth argument is true closing the window used to display
	// the splash screen will cause the application to exit.
	//

	if (arg.length <= 4) {
	    if (arg.length <= 0 || arg[0].isString() || arg[0].isNull()) {
		if (arg.length <= 3 || arg[3].isNumber()) {
		    YoixSplashScreen.showSystemSplashScreen(
			arg.length > 0 ? arg[0] : null,
			arg.length > 1 ? arg[1] : null,
			arg.length > 2 ? arg[2] : null,
			arg.length > 3 ? arg[3].booleanValue() : false
		    );
		} else VM.badArgument(3);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    stat(YoixObject arg[]) {

	YoixObject  dict = null;
	YoixObject  obj;
	boolean     exists = false;
	String      path;
	File        file = null;
	int         mode = 0;

	if (arg[0].isString() && arg[0].notNull() && arg[0].length() > 0) {
	    if (arg.length == 2) {
		if (arg[1].isDictionary() && arg[1].notNull())
		    dict = arg[1];
		else VM.badArgument(1);
	    } else dict = YoixObject.newDictionary(9);

	    try {
		file = new File(path = arg[0].stringValue());
	    }

	    catch(RuntimeException e) {
		file = null;
		path = null;
	    }

	    if (file != null) {
		VM.pushAccess(LRW_);
		exists = file.exists();
		if (exists) {
		    if (file.canRead())
			mode += READ;
		    if (file.canWrite())
			mode += WRITE;

		    dict.put(N_EXISTS, YoixObject.newInt(1), false);
		    dict.put(N_MODE, YoixObject.newInt(mode), false);
		    dict.put(N_SIZE, YoixObject.newDouble(file.length()), false);
		    dict.put(N_MTIME, YoixObject.newDouble((file.lastModified())/1000.0), false);
		    dict.put(N_ISFILE, YoixObject.newInt(file.isFile()), false);
		    dict.put(N_ISDIRECTORY, YoixObject.newInt(file.isDirectory()), false);
		} else {
		    dict.put(N_EXISTS, YoixObject.newInt(0), false);
		    dict.put(N_MODE, YoixObject.newInt(0), false);
		    dict.put(N_SIZE, YoixObject.newDouble(0), false);
		    dict.put(N_MTIME, YoixObject.newDouble(0), false);
		    dict.put(N_ISFILE, YoixObject.newInt(0), false);
		    dict.put(N_ISDIRECTORY, YoixObject.newInt(0), false);
		}

		dict.put(N_YOIXPATH, YoixObject.newString(YoixMisc.toYoixPath(path)), false);
		dict.put(N_LOCALPATH, YoixObject.newString(YoixMisc.toLocalPath(path)), false);

		try {
		    path = file.getCanonicalPath();
		    if (File.separatorChar != '/')
			path = path.replace(File.separatorChar, '/');
		    dict.put(N_REALPATH, YoixObject.newString(path), false);
		}
		catch(IOException e) {
		    dict.put(N_REALPATH, YoixObject.newString(), false);
		    VM.caughtException(e);
		}

		VM.popAccess();
	    }
	} else VM.badArgument(0);

	return(file == null ? YoixObject.newDictionary() : dict);
    }


    public static YoixObject
    time(YoixObject arg[]) {

	YoixObject  obj = null;
	double      secs;

	if (arg.length <= 1) {
	    secs = System.currentTimeMillis()/1000.0;
	    if (arg.length == 1) {
		if (arg[0].isNull() || arg[0].isNumberPointer()) {
		    if (arg[0].isIntegerPointer())
			arg[0].put(YoixObject.newInt((int)secs));
		    else if (arg[0].isDoublePointer())
			arg[0].put(YoixObject.newDouble(secs));
		} else VM.badArgument(0);
	    }
	    obj = YoixObject.newDouble(secs);
	} else VM.badCall();

	return(obj);
    }


    public static YoixObject
    totalMemory(YoixObject arg[]) {

	return(YoixObject.newNumber(RUNTIME.totalMemory()));
    }


    public static YoixObject
    traceInstructions(YoixObject arg[]) {

	if (arg[0].isInteger())
	    RUNTIME.traceInstructions(arg[0].booleanValue());
	else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    traceMethodCalls(YoixObject arg[]) {

	if (arg[0].isInteger())
	    RUNTIME.traceMethodCalls(arg[0].booleanValue());
	else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    unlink(YoixObject arg[]) {

	boolean  result = false;
	File     file;

	if (arg[0].isString() && arg[0].notNull()) {
	    try {
		file = new File(arg[0].stringValue());
		result = !file.isDirectory() && file.delete();
	    }
	    catch(RuntimeException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(result ? 0 : -1));
    }


    public static YoixObject
    waitFor(YoixObject arg[]) {

	Process  process;

	if (arg[0].isProcess() && arg[0].notNull()) {
	    if ((process = (Process)arg[0].getManagedObject()) != null) {
		try {
		    process.waitFor();
		}
		catch(InterruptedException e) {
		    VM.caughtException(e);
		}
	    }
	} else VM.badArgument(0);

	return(null);
    }


    public static YoixObject
    yoixPath(YoixObject arg[]) {

	String  path = null;

	if (arg[0].isString() && arg[0].notNull() && arg[0].length() > 0)
	    path = YoixMisc.toYoixPath(arg[0].stringValue());
	else VM.badArgument(0);

	return(YoixObject.newString(path));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    recurseSetPermissions(YoixObject yobj, short addPerms, short delPerms, boolean recurse) {

	YoixObject  yobj2;
	int         n;

	// for now just allow Dictionary or Array
	if (yobj.isDictionary() || yobj.isArray()) {
	    if ((addPerms&GROWABLE) == GROWABLE) {
		yobj.setGrowable(true);
	    } else if ((delPerms&GROWABLE) == GROWABLE) {
		yobj.setGrowable(false);
	    }
	    if (recurse) {
		for (n = yobj.offset(); n < yobj.length(); n++) {
		    yobj2 = yobj.getObject(n);
		    recurseSetPermissions(yobj2, addPerms, delPerms, recurse);
		}
	    }
	}
    }
}

