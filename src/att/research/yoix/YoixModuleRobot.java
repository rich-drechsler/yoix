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

abstract
class YoixModuleRobot extends YoixModule

    implements YoixConstants

{

    static String  $MODULENAME = M_ROBOT;

    static Object  $module[] = {
    //
    // NAME                  ARG                    COMMAND     MODE   REFERENCE
    // ----                  ---                    -------     ----   ---------
       null,                 "12",                  $LIST,      $RORO, $MODULENAME,

       "robotAutoDelay",     "",                    $BUILTIN,   $LR_X, null,
       "robotAutoWait",      "",                    $BUILTIN,   $LR_X, null,
       "robotCheck",         "0",                   $BUILTIN,   $LR_X, null,
       "robotDelay",         "1",                   $BUILTIN,   $LR_X, null,
       "robotKeyPress",      "1",                   $BUILTIN,   $LR_X, null,
       "robotKeyRelease",    "1",                   $BUILTIN,   $LR_X, null,
       "robotMouseMove",     "-1",                  $BUILTIN,   $LR_X, null,
       "robotMousePress",    "1",                   $BUILTIN,   $LR_X, null,
       "robotMouseRelease",  "1",                   $BUILTIN,   $LR_X, null,
       "robotMouseWheel",    "1",                   $BUILTIN,   $LR_X, null,
       "robotPixelColor",    "-1",                  $BUILTIN,   $LR_X, null,
       "robotWaitForIdle",   "0",                   $BUILTIN,   $LR_X, null,
    };

    private static Robot  modulerobot = null;

    ///////////////////////////////////
    //
    // YoixModuleRobot Methods
    //
    ///////////////////////////////////

    public static YoixObject
    robotAutoDelay(YoixObject arg[]) {

	YoixObject  retval = null;
	double      seconds;
	long        lms;
	int         ms;

	if (arg.length >= 0 && arg.length <= 1) {
	    if (arg.length == 0) {
		retval = YoixObject.newDouble(((double)getRobot().getAutoDelay())/1000.0);
	    } else {
		if (arg[0].notNull() && arg[0].isNumber()) {
		    lms = Math.round(arg[0].doubleValue() * 1000.0);
		    if (lms >= 0L && lms <= 60000L) {
			getRobot().setAutoDelay((int)lms);
		    } else VM.badArgumentValue(0);
		} else VM.badArgument(0);
	    }
	} else VM.badCall();

	return(retval == null ? YoixObject.newEmpty() : retval);
    }


    public static YoixObject
    robotAutoWait(YoixObject arg[]) {

	YoixObject  retval = null;

	if (arg.length >= 0 && arg.length <= 1) {
	    if (arg.length == 0) {
		retval = YoixObject.newInt(getRobot().isAutoWaitForIdle());
	    } else {
		if (arg[0].notNull() && arg[0].isInteger()) {
		    getRobot().setAutoWaitForIdle(arg[0].booleanValue());
		} else VM.badArgument(0);
	    }
	} else VM.badCall();

	return(retval == null ? YoixObject.newEmpty() : retval);
    }


    public static YoixObject
    robotCheck(YoixObject arg[]) {

	return(YoixObject.newInt(getRobot(true) != null));
    }


    public static YoixObject
    robotDelay(YoixObject arg[]) {

	long  lms;

	if (arg[0].notNull() && arg[0].isNumber()) {
	    lms = Math.round(arg[0].doubleValue() * 1000.0);
	    if (lms >= 0L && lms <= 60000L)
		getRobot().delay((int)lms);
	    else VM.badArgumentValue(0);
	} else VM.badArgument(0);

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    robotKeyPress(YoixObject arg[]) {

	int  keycode;

	if (arg[0].notNull() && arg[0].isInteger()) {
	    keycode = arg[0].intValue();
	    try {
		getRobot().keyPress(keycode);
	    }
	    catch(IllegalArgumentException e) {
		VM.badArgumentValue(0);
	    }
	} else VM.badArgument(0);

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    robotKeyRelease(YoixObject arg[]) {

	int  keycode;

	if (arg[0].notNull() && arg[0].isInteger()) {
	    keycode = arg[0].intValue();
	    try {
		getRobot().keyRelease(keycode);
	    }
	    catch(IllegalArgumentException e) {
		VM.badArgumentValue(0);
	    }
	} else VM.badArgument(0);

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    robotMouseMove(YoixObject arg[]) {

	Point  pt = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint() || arg[0].isNull())
		    pt = YoixMakeScreen.javaPoint(arg[0]);
		else VM.badArgument(0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber())
			pt = YoixMakeScreen.javaPoint(arg[0].doubleValue(), arg[1].doubleValue());
		    else VM.badArgument(1);
		} else VM.badArgument(0);
	    }
	    getRobot().mouseMove(pt.x, pt.y);
	} else VM.badCall();

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    robotMousePress(YoixObject arg[]) {

	int  buttons;

	if (arg[0].notNull() && arg[0].isInteger()) {
	    buttons = arg[0].intValue();
	    try {
		getRobot().mousePress(buttons);
	    }
	    catch(IllegalArgumentException e) {
		VM.badArgumentValue(0);
	    }
	} else VM.badArgument(0);

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    robotMouseRelease(YoixObject arg[]) {

	int  buttons;

	if (arg[0].notNull() && arg[0].isInteger()) {
	    buttons = arg[0].intValue();
	    try {
		getRobot().mouseRelease(buttons);
	    }
	    catch(IllegalArgumentException e) {
		VM.badArgumentValue(0);
	    }
	} else VM.badArgument(0);

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    robotMouseWheel(YoixObject arg[]) {

	if (arg[0].notNull() && arg[0].isInteger())
	    getRobot().mouseWheel(arg[0].intValue());
	else VM.badArgument(0);

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    robotPixelColor(YoixObject arg[]) {

	YoixObject  retval = null;
	Point       pt = null;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg.length == 1) {
		if (arg[0].isPoint() || arg[0].isNull())
		    pt = YoixMakeScreen.javaPoint(arg[0]);
		else VM.badArgument(0);
	    } else {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber())
			pt = YoixMakeScreen.javaPoint(arg[0].doubleValue(), arg[1].doubleValue());
		    else VM.badArgument(1);
		} else VM.badArgument(0);
	    }
	    retval = YoixObject.newColor(getRobot().getPixelColor(pt.x, pt.y));
	} else VM.badCall();

	return(retval != null ? retval : YoixObject.newColor());
    }


    public static YoixObject
    robotWaitForIdle(YoixObject arg[]) {

	try {
	    getRobot().waitForIdle();
	}
	catch(IllegalThreadStateException e) {
	    VM.abort(RESTRICTEDACCESS);
	}

	return(YoixObject.newEmpty());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static synchronized Robot
    getRobot() {

	return(getRobot(false));
    }


    private static synchronized Robot
    getRobot(boolean checking) {

	SecurityManager  sm;
	Robot            robot = null;

	try {
	    if (modulerobot != null) {
		if ((sm = System.getSecurityManager()) != null)
		    sm.checkPermission(new AWTPermission("createRobot"));
	    } else modulerobot = new Robot();
	    robot = modulerobot;
	}
	catch(AWTException e) {
	    if (checking == false)
		VM.abort(UNSUPPORTEDOPERATION);
	}
	return(robot);
    }
}

