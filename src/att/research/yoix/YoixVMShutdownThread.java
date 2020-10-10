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
class YoixVMShutdownThread extends YoixThread

    implements YoixConstants,
	       Runnable

{

    //
    // A single instance of this class will be registered with the JVM
    // as our official "shutdown hook".
    // 

    private static boolean  installed = false;
    private static Vector   queue = new Vector();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    private
    YoixVMShutdownThread() {

	setDaemon(true);
	setPriority(Thread.MAX_PRIORITY);	// just in case
    }

    ///////////////////////////////////
    //
    // Runnable Methods
    //
    ///////////////////////////////////

    public final void
    run() {

	YoixObject  function;
	YoixObject  argv[];
	YoixObject  context;
	YoixError   error_point = null;
	YoixError   interrupt_point = null;
	Vector      hooks;
	Object      hook[];
	int         status;

	//
	// A single instance of this class will be registered with the JVM
	// as our official "shutdown hook". According to documentation of
	// the JVM shutdown process (i.e., javadoc comments) this is the
	// first step in a two step process. The second step handles the
	// running uninvoked finalizers if runFinalizersOnExit() was used.
	// The Yoix interpreter doesn't use runFinalizersOnExit() and we
	// probably never will, so as things stand right now we can call
	// halt() (indirectly via VM.exit()) when we're done here. This
	// approach will have to change if we use runFinalizersOnExit(),
	// but the changes (here and in YoixVM.java) aren't difficult.
	//

	status = VM.setShutdown(-1);

	try {
	    hooks = (Vector)queue.clone();
	    while (hooks.size() > 0) {
		YoixVMThread.reset();		// clear our stack using brute force!!
		hook = (Object[])hooks.elementAt(0);
		hooks.removeElementAt(0);
		if (hook != null) {
		    if (hook.length == 3) {
			if (hook[0] instanceof YoixObject && hook[1] instanceof YoixObject[]) {
			    if ((function = (YoixObject)hook[0]) != null) {
				if (function.notNull()) {
				    argv = (YoixObject[])hook[1];
				    context = (YoixObject)hook[2];
				    try {
					error_point = VM.pushError();
					try {
					    interrupt_point = VM.pushInterrupt();
					    function.call(argv, context);
					}
					catch(YoixError e) {
					    if (e != interrupt_point)
						throw(e);
					}
					VM.popError();
				    }
				    catch(YoixError e) {
					if (e != error_point)
					    throw(e);
					else VM.error(error_point);
				    }
				    catch(SecurityException e) {
					VM.error(e);
					VM.popError();
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	catch (Throwable t) {}
	finally {
	    VM.exit(status);
	}
    }

    ///////////////////////////////////
    //
    // YoixVMShutdownThread Methods
    //
    ///////////////////////////////////

    static boolean
    addShutdownHook(YoixObject function, YoixObject argv[], YoixObject context) {

	boolean  result = false;

	if (VM.notRestricted()) {
	    if (installed) {
		if (function != null && queue != null) {
		    if (function.notNull()) {
			if (function.callable(argv.length)) {
			    queue.addElement(new Object[] {function, argv, context});
			    result = true;
			}
		    }
		}
	    }
	} else VM.abort(RESTRICTEDACCESS);

	return(result);
    }


    static void
    installShutdownThread() {

	synchronized(queue) {
	    if (installed == false) {
		//
		// Catching SecurityException prevents problems when we're running
		// as an untrusted application under javaws.
		//
		try {
		    RUNTIME.addShutdownHook(new YoixVMShutdownThread());
		    installed = true;
		}
		catch(IllegalStateException e) {}
		catch(SecurityException e) {}
	    }
	}
    }
}

