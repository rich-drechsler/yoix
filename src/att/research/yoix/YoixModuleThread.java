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
class YoixModuleThread extends YoixModule

{

    static String  $MODULENAME = M_THREAD;

    static Integer  $MAX_PRIORITY = new Integer(Thread.MAX_PRIORITY);
    static Integer  $MIN_PRIORITY = new Integer(Thread.MIN_PRIORITY);
    static Integer  $NORM_PRIORITY = new Integer(Thread.NORM_PRIORITY);
    static Integer  $YOIXCALLSTACKTRACE = new Integer(MODEL_YOIXCALLSTACKTRACE);
    static Integer  $YOIXSTACKDUMP = new Integer(MODEL_YOIXSTACKDUMP);
    static Integer  $YOIXSTACKTRACE = new Integer(MODEL_YOIXSTACKTRACE);

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "16",                $LIST,      $RORO, $MODULENAME,
       "MAX_PRIORITY",       $MAX_PRIORITY,       $INTEGER,   $LR__, null,
       "MIN_PRIORITY",       $MIN_PRIORITY,       $INTEGER,   $LR__, null,
       "NORM_PRIORITY",      $NORM_PRIORITY,      $INTEGER,   $LR__, null,
       "YOIXCALLSTACKTRACE", $YOIXCALLSTACKTRACE, $INTEGER,   $LR__, null,
       "YOIXSTACKDUMP",      $YOIXSTACKDUMP,      $INTEGER,   $LR__, null,
       "YOIXSTACKTRACE",     $YOIXSTACKTRACE,     $INTEGER,   $LR__, null,
       "activeCount",        "",                  $BUILTIN,   $LR_X, null,
       "currentThread",      "0",                 $BUILTIN,   $LR_X, null,
       "dumpJavaStack",      "0",                 $BUILTIN,   $LR_X, null,
       "dumpStack",          "",                  $BUILTIN,   $LR_X, null,
       "dumpYoixStack",      "",                  $BUILTIN,   $LR_X, null,
       "enumerate",          "",                  $BUILTIN,   $LR_X, null,
       "notifyAll",          "1",                 $BUILTIN,   $LR_X, null,
       "sleep",              "1",                 $BUILTIN,   $LR_X, null,
       "wait",               "-1",                $BUILTIN,   $LR_X, null,
       "yield",              "0",                 $BUILTIN,   $LR_X, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleThread Methods
    //
    ///////////////////////////////////

    public static YoixObject
    activeCount(YoixObject arg[]) {

	Thread  list[];
	String  name;
	int     count = 0;
	int     n;

	if (arg.length <= 1) {
	    if (arg.length == 0 || arg[0].isString()) {
		if (arg.length == 1) {
		    name = arg[0].stringValue();
		    list = getActiveThreads();
		    for (n = 0; n < list.length; n++) {
			if (name.equals(list[n].getThreadGroup().getName())) {
			    count = list[n].getThreadGroup().activeCount();
			    break;
			}
		    }
		} else count = Thread.activeCount();
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(count));
    }


    public static YoixObject
    currentThread(YoixObject arg[]) {

	return(YoixMake.yoixThread(Thread.currentThread()));
    }


    public static YoixObject
    dumpJavaStack(YoixObject arg[]) {

	VM.print(N_STDERR, YoixMisc.javaTrace(new Throwable(PREFIX_JAVASTACKTRACE)));
	return(null);
    }


    public static YoixObject
    dumpStack(YoixObject arg[]) {

	String  thread;
	int     model;

	if (arg.length <= 2) {
	    thread = null;
	    model = MODEL_YOIXSTACKTRACE;
	    if (arg.length == 1) {
		if (arg[0].isString() || arg[0].isNull())
		    thread = arg[0].notNull() ? arg[0].stringValue() : null;
		else if (arg[0].isNumber())
		    model = arg[0].intValue();
		else VM.badArgument(0);
	    } else if (arg.length == 2) {
		if (arg[0].isString() || arg[0].isNull()) {
		    if (arg[1].isNumber()) {
			thread = arg[0].notNull() ? arg[0].stringValue() : null;
			model = arg[1].intValue();
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    }
	    VM.print(N_STDERR, YoixVMThread.dump(thread, model));
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    dumpYoixStack(YoixObject arg[]) {

	String  thread;

	if (arg.length <= 1) {
	    thread = null;
	    if (arg.length == 1) {
		if (arg[0].isString() || arg[0].isNull())
		    thread = arg[0].notNull() ? arg[0].stringValue() : null;
		else VM.badArgument(0);
	    }
	    VM.print(N_STDERR, YoixVMThread.dump(thread, MODEL_YOIXSTACKDUMP));
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    enumerate(YoixObject arg[]) {

	YoixObject  obj = null;
	Thread      list[];
	String      name;
	int         count;
	int         m;
	int         n;

	if (arg.length <= 1) {
	    if (arg.length == 0 || arg[0].isString()) {
		list = getActiveThreads();
		if (arg.length == 1)
		    name = arg[0].stringValue();
		else name = null;
		if (name != null) {
		    for (n = 0, count = 0; n < list.length; n++) {
			if (name.equals(list[n].getThreadGroup().getName()))
			    count++;
		    }
		} else count = list.length;
		obj = YoixObject.newArray(count > 0 ? count : -1);
		for (n = m = 0; n < list.length && m < count; n++) {
		    if (name == null || name.equals(list[n].getThreadGroup().getName()))
			obj.put(m++, YoixMake.yoixThread(list[n]), false);
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(obj);
    }


    public static YoixObject
    notifyAll(YoixObject arg[]) {

	int  value = -1;

	if (arg[0].isPointer()) {
	    try {
		arg[0].getLock().notifyAll();
		value = 0;
	    }
	    catch(IllegalMonitorStateException e) {}
	} else VM.badArgument(0);

	return(YoixObject.newInt(value));
    }


    public static YoixObject
    sleep(YoixObject arg[]) {

	double  value = 0;
	long    msecs;
	long    start;

	if (arg[0].isNumber()) {
	    if ((msecs = (long)(arg[0].doubleValue()*1000)) > 0) {
		start = System.currentTimeMillis();
		try {
		    Thread.sleep(msecs);
		}
		catch(InterruptedException e) {
		    VM.caughtException(e);
		}
		value = (start + msecs - System.currentTimeMillis())/1000.0;
	    }
	} else VM.badArgument(0);

	return(YoixObject.newNumber(value > 0 ? value : 0.0));
    }


    public static YoixObject
    wait(YoixObject arg[]) {

	double  value = 0;
	long    start;
	long    msecs;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isPointer()) {
		msecs = 0;
		if (arg.length == 2) {
		    if (arg[1].isNumber())
			msecs = (long)(arg[1].doubleValue()*1000);
		    else VM.badArgument(1);
		}

		if (msecs >= 0) {
		    start = System.currentTimeMillis();
		    try {
			arg[0].getLock().wait(msecs);	// see statementSynchronized()
		    }
		    catch(InterruptedException e) {
			VM.caughtException(e);
		    }
		    catch(IllegalMonitorStateException e) {
			value = -1;
		    }
		    finally {
			if (value == 0) {
			    value = Math.max(
				(start + msecs - System.currentTimeMillis())/1000.0,
				0
			    );
			}
		    }
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newNumber(value));
    }


    public static YoixObject
    yield(YoixObject arg[]) {

	Thread.yield();
	return(null);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static Thread[]
    getActiveThreads() {

	ThreadGroup  group;
	String       name;
	Thread       list[];
	Thread       nlist[];
	int          count;
	int          length;

	group = Thread.currentThread().getThreadGroup();
	while (group.getParent() != null)
	    group = group.getParent();

	for (count = group.activeCount(); ; count += 10) {
	    list = new Thread[count + 1];
	    if ((length = group.enumerate(list, true)) < list.length)
		break;
	}

	nlist = new Thread[length];
	System.arraycopy(list, 0, nlist, 0, nlist.length);
	return(nlist);
    }
}

