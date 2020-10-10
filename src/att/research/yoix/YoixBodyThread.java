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
import java.util.*;

final
class YoixBodyThread extends YoixPointerActive

    implements YoixInterfaceKillable,
	       Runnable

{

    //
    // Looks harder than you might expect, mostly because we also have
    // to represent threads that were created somewhere else.
    //

    private Thread  thread;
    private Vector  queue = new Vector();

    //
    // The runnable and the command that it's currently running if it's
    // not null. Old versions defined command[] in the run() method, but
    // we moved it here when we added the N_QUEUEONCE builtin.
    //

    private YoixObject  command[] = null;
    private Runnable    runner = null;

    //
    // All running YoixBodyThread and a counter for generating names.
    //

    private static Hashtable  activethreads = new Hashtable();
    private static int        threadnumber = 1;

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_RUN,              $LR_X,       null,
    };

    //
    // Permissions when we're supposed to represent a Java Thread that
    // was created somewhere else.
    //

    private static final Object  permissions2[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_ALIVE,            $LR__,       null,
	N_DAEMON,           $LR__,       null,
	N_NAME,             $LR__,       $LR__,
	N_PERSISTENT,       $L___,       null,
	N_QUEUE,            $L___,       $L___,
	N_QUEUEONCE,        $L___,       $L___,
	N_QUEUESIZE,        $LR__,       $LR__,
	N_RUN,              $L___,       null,
    };

    //
    // Permissions when we're supposed to represent a Java Thread that
    // has an external Runnable supplied
    //

    private static final Object  permissions3[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_DAEMON,           $LR__,       null,
	N_NAME,             $LR__,       $LR__,
	N_PERSISTENT,       $L___,       null,
	N_QUEUE,            $L___,       $L___,
	N_QUEUEONCE,        $L___,       $L___,
	N_QUEUESIZE,        $LR__,       $LR__,
	N_RUN,              $L___,       null,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(15);

    static {
	activefields.put(N_ALIVE, new Integer(V_ALIVE));
	activefields.put(N_DAEMON, new Integer(V_DAEMON));
	activefields.put(N_GROUP, new Integer(V_GROUP));
	activefields.put(N_INTERRUPTED, new Integer(V_INTERRUPTED));
	activefields.put(N_NAME, new Integer(V_NAME));
	activefields.put(N_PERSISTENT, new Integer(V_PERSISTENT));
	activefields.put(N_PRIORITY, new Integer(V_PRIORITY));
	activefields.put(N_QUEUE, new Integer(V_QUEUE));
	activefields.put(N_QUEUEONCE, new Integer(V_QUEUEONCE));
	activefields.put(N_QUEUESIZE, new Integer(V_QUEUESIZE));
	activefields.put(N_RUN, new Integer(V_RUN));
	activefields.put(N_STATE, new Integer(V_STATE));
    }

    private static final String  YOIXTHREADGROUP = "Yoix";
    private static final String  YOIXTHREADPREFIX = "YoixThread-";

    //
    // Take the static out of the next declaration and you may be able
    // notice a small (56 byte) memory leak in some implementations of
    // java.
    //

    private static ThreadGroup  group = new ThreadGroup(YOIXTHREADGROUP);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyThread(YoixObject data) {

	super(data);
	buildThread();
	setFixedSize();
	setPermissions(permissions);
    }


    YoixBodyThread(YoixObject data, Thread thread) {

	super(data);
	this.thread = thread;
	runner = thread;
	setFixedSize();
	setPermissions(permissions2);
    }


    YoixBodyThread(YoixObject data, Runnable runner) {

	super(data);
	try {
	    thread = new Thread(group, runner);
	    activethreads.put(thread, this);
	    setField(N_NAME);
	    setField(N_PRIORITY);
	    setField(N_DAEMON);
	}
	catch(IllegalThreadStateException e) {
	    stopThread();
	}
	this.runner = runner;		// recent change - 12/28/02
	setFixedSize();
	setPermissions(permissions3);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(THREAD);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceKillable Methods
    //
    ///////////////////////////////////

    public final void
    kill() {

	stopThread();		// check that it's the currentThread()??
    }

    ///////////////////////////////////
    //
    // Runnable Methods
    //
    ///////////////////////////////////

    public final void
    run() {

	try {
	    while (data.getBoolean(N_ALIVE, true)) {
		command = null;
		try {
		    synchronized(this) {
			if (queue.size() > 0) {
			    command = (YoixObject[])queue.elementAt(0);
			    queue.removeElementAt(0);
			} else {
			    if (data.getBoolean(N_PERSISTENT)) {
				if (data.getBoolean(N_ALIVE))
				    wait();
			    } else break;
			}
		    }
		    if (command != null) {
			Thread.interrupted();	// just to clear interrupted state!!!
			call(command);
		    }
		}
		catch(InterruptedException e) {}
	    }
	}
	finally {
	    stopThread();
	}
    }

    ///////////////////////////////////
    //
    // YoixBodyThread Methods
    //
    ///////////////////////////////////

    static YoixObject
    activeThread(Thread t) {

	YoixBodyThread  active;

	return((active = (YoixBodyThread)activethreads.get(t)) != null
	    ? YoixObject.newPointer(active)
	    : null
	);
    }


    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_QUEUE:
		obj = builtinQueue(name, argv);
		break;

	    case V_QUEUEONCE:
		obj = builtinQueueOnce(name, argv);
		break;

	    case V_RUN:
		obj = builtinRun(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	thread = null;
	queue = null;
	runner = null;
	super.finalize();
    }


    protected final synchronized YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_ALIVE:
		obj = YoixObject.newInt(thread != null && thread.isAlive());
		break;

	    case V_DAEMON:
		if (thread != null)
		    obj = YoixObject.newInt(thread.isDaemon());
		break;

	    case V_GROUP:
		if (thread != null)
		    obj = YoixObject.newString(thread.getThreadGroup().getName());
		break;

	    case V_INTERRUPTED:
		if (thread != null)
		    obj = YoixObject.newInt(thread.isInterrupted());
		break;

	    case V_NAME:
		if (thread != null)
		    obj = YoixObject.newString(thread.getName());
		break;

	    case V_PRIORITY:
		if (thread != null)
		    obj = YoixObject.newInt(thread.getPriority());
		break;

	    case V_QUEUESIZE:
		obj = YoixObject.newInt(queue.size());
		break;

	    case V_STATE:
		obj = YoixObject.newInt(Thread.currentThread() == thread);
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(thread);
    }


    final Runnable
    getRunner() {

	return(runner != null ? runner : this);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ALIVE:
		    setAlive(obj);
		    break;

		case V_DAEMON:
		    setDaemon(obj);
		    break;

		case V_INTERRUPTED:
		    setInterrupted(obj);
		    break;

		case V_NAME:
		    setName(obj);
		    break;

		case V_PERSISTENT:
		    setPersistent(obj);
		    break;

		case V_PRIORITY:
		    setPriority(obj);
		    break;

		case V_RUN:
		    setRun(obj);
		    break;
	    }
	}

	return(obj);
    }


    static int
    threadCount(boolean all) {

	Enumeration  enm;
	Hashtable    active;
	Object       element;
	int          count = 0;

	if (all == false) {
	    active = (Hashtable)activethreads.clone();		// just to be safe
	    for (enm = active.elements(); enm.hasMoreElements(); ) {
		if ((element = enm.nextElement()) != null) {
		    if (((YoixBodyThread)element).isDaemon() == false)
			count++;
		}
	    }
	} else count = activethreads.size();

	return(count);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildThread() {

	thread = null;
	runner = null;
	setField(N_NAME);
	setField(N_RUN);
    }


    private synchronized YoixObject
    builtinQueue(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  funct;

	//
	// Starting the thread before building the return value should
	// be OK, but only because we own the lock that run() needs to
	// remove objects from the queue.
	//

	if (arg.length > 0) {
	    if (arg[0].isNull()) {
		obj = YoixMake.yoixArray(queue);
		queue.removeAllElements();
	    } else if (arg[0].isCallable() || arg[0].isCallablePointer()) {
		funct = arg[0].isCallable() ? arg[0] : arg[0].get();
		if (funct.callable(arg.length - 1)) {
		    queue.addElement(arg);
		    obj = YoixMake.yoixArray(queue);
		} else VM.badCall(name);
	    } else VM.badArgument(name, 0);
	    startThread(null);
	} else obj = YoixMake.yoixArray(queue);

	return(obj);
    }


    private synchronized YoixObject
    builtinQueueOnce(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  funct;

	//
	// Starting the thread before building the return value should
	// be OK, but only because we own the lock that run() needs to
	// remove objects from the queue.
	//

	if (arg.length > 0) {
	    if (arg[0].isNull()) {
		obj = YoixMake.yoixArray(queue);
		queue.removeAllElements();
	    } else if (arg[0].isCallable() || arg[0].isCallablePointer()) {
		funct = arg[0].isCallable() ? arg[0] : arg[0].get();
		if (funct.callable(arg.length - 1)) {
		    if (notDuplicated(arg)) {
			queue.addElement(arg);
			obj = YoixMake.yoixArray(queue);
		    } else obj = YoixObject.newArray();
		} else VM.badCall(name);
	    } else VM.badArgument(name, 0);
	    startThread(null);
	} else obj = YoixMake.yoixArray(queue);

	return(obj);
    }


    private synchronized YoixObject
    builtinRun(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  run;

	run = data.getObject(N_RUN);
	if (run.isFunction() && run.notNull()) {
	    if (run.callable(arg.length))
		startThread(arg);
	    else VM.badCall(name);
	}
	obj = YoixObject.newEmpty();

	return(obj);
    }


    private static void
    interrupt(Thread t) {

	if (t != null) {
	    t.interrupt();
	    YoixBodyStream.atInterrupt(t);
	}
    }


    private synchronized boolean
    isDaemon() {

	return(thread != null && thread.isDaemon());
    }


    private synchronized String
    nextName() {

	return(YOIXTHREADPREFIX + threadnumber++);
    }


    private synchronized boolean
    notDuplicated(YoixObject arg[]) {

	Enumeration  enm;
	boolean      result;
	Object       element;
	int          length;
	int          n;

	if (YoixInterpreter.equalsEQEQ(arg, command) == false) {
	    result = true;
	    for (enm = queue.elements(); enm.hasMoreElements(); ) {
		if ((element = enm.nextElement()) != null) {
		    if (element instanceof YoixObject[]) {
			if (YoixInterpreter.equalsEQEQ(arg, (YoixObject[])element)) {
			    result = false;
			    break;
			}
		    }
		}
	    }
	} else result = false;

	return(result);
    }


    private synchronized void
    setAlive(YoixObject obj) {

	if (obj != null) {
	    if (runner != null) {
                if (thread != null) {
		    if (obj.booleanValue() == false) {
			interrupt(thread);
			// record that it was interrupted rather than
			// a normal termination at completion
			data.putInt(N_INTERRUPTED, thread.isInterrupted());
			// wrap it up (external Runnables cannot be restarted)
			stopThread();
		    } else {
			data.putInt(N_ALIVE, true);
			thread.start();
		    }
		}
	    } else {
		if (obj.booleanValue() == false)
		    interrupt(thread);
		else startThread(null);
	    }
	}
    }


    private synchronized void
    setDaemon(YoixObject obj) {

	if (obj != null && thread != null) {
	    if (thread.isAlive())
		data.putInt(N_DAEMON, thread.isDaemon());
	    else thread.setDaemon(obj.booleanValue());
	}
    }


    private synchronized void
    setInterrupted(YoixObject obj) {

	YoixObject  funct;

	if (obj != null && thread != null) {
	    if (obj.booleanValue()) {
		interrupt(thread);
		if ((funct = data.getObject(N_AFTERINTERRUPT)) != null) {
		    if (funct.notNull())
			queue.insertElementAt(new YoixObject[] {funct}, 0);
		}
	    }
	}
    }


    private synchronized void
    setName(YoixObject obj) {

	String  name;

	if (obj != null) {
	    if (obj.isNull()) {
		data.putString(N_NAME, nextName());
		obj = data.get(N_NAME);
	    }
	    if (thread != null) {
		name = obj.stringValue();
		if (name.equals(thread.getName()) == false)
		    thread.setName(name);
	    }
	}
    }


    private synchronized void
    setPersistent(YoixObject obj) {

	if (obj != null && thread != null) {
	    if (obj.booleanValue() == false) {
		if (thread.isAlive())
		    notifyAll();
	    }
	}
    }


    private synchronized void
    setPriority(YoixObject obj) {

	int  priority;

	if (obj != null) {
	    priority = Math.max(Math.min(obj.intValue(), Thread.MAX_PRIORITY), Thread.MIN_PRIORITY);
	    if (thread != null) {
		if (thread.getPriority() != priority)
		    thread.setPriority(priority);
	    }
	}
    }


    private synchronized void
    setRun(YoixObject obj) {

	//
	// Decided, at least for now, to explicitly disable N_QUEUE,
	// when the thread defines a run function that's notNull().
	// Permssions currently assigned at the end of the constructor
	// imply this can only happen in the thread's declaration.
	//

	if (obj.isFunction() && obj.callable(0)) {
	    if (thread != null) {
		data.putInt(N_ALIVE, false);
		interrupt(thread);
	    }
	    VM.pushAccess(LR__);
	    data.get(N_QUEUE).setAccess(obj.notNull() ? L___ : L__X);
	    VM.popAccess();
	} else VM.abort(TYPECHECK, N_RUN);
    }


    private synchronized void
    startThread(YoixObject arg[]) {

	YoixObject  run;
	YoixObject  copy[];

	if (thread == null) {
	    try {
		thread = new Thread(group, this);
		activethreads.put(thread, this);
		setField(N_NAME);
		setField(N_PRIORITY);
		setField(N_DAEMON);
		run = data.getObject(N_RUN);
		if (run.notNull()) {
		    if (arg != null && arg.length > 0) {
			copy = new YoixObject[arg.length + 1];
			copy[0] = run;
			System.arraycopy(arg, 0, copy, 1, arg.length);
			arg = copy;
		    } else arg = new YoixObject[] {run};
		    queue.insertElementAt(arg, 0);
		}
		data.putInt(N_ALIVE, true);	// must preceed start()!
		thread.start();
	    }
	    catch(IllegalThreadStateException e) {
		stopThread();
	    }
	} else notifyAll();
    }


    private synchronized void
    stopThread() {

	if (thread != null) {
	    command = null;
	    queue.removeAllElements();
	    activethreads.remove(thread);
	    thread = null;
	    data.putInt(N_ALIVE, false);
	    if (runner == null)
		notifyAll();
	    else runner = null;
	}
    }
}

