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
import java.lang.reflect.*;

class YoixVMCleaner

    implements Runnable,
	       YoixConstants

{

    private String  classname;
    private int     priority;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixVMCleaner(String classname) {

	this(classname, Thread.MIN_PRIORITY);
    }


    YoixVMCleaner(String classname, int priority) {

	this.classname = classname;
	this.priority = priority;
	startThread();
    }

    ///////////////////////////////////
    //
    // Runnable Methods
    //
    ///////////////////////////////////

    public final void
    run() {

	cleanup(classname);
    }

    ///////////////////////////////////
    //
    // YoixVMCleaner Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    cleanup(String classname) {

	boolean  debug;
	String   name;
	Field    fields[];
	Class    c;
	int      n;

	//
	// The booted check is a recent addition that probably should
	// not be required, but native threads out SGI sometimes started
	// this thread too early. Only saw it once or twice, so it's not
	// a big deal.
	//

	if (debug = (VM.isBooted() && VM.bitCheck(N_DEBUG, DEBUG_VMCLEANER)))
	    VM.println(N_STDOUT, "clean " + classname);

	try {
	    c = Class.forName(classname);
	    fields = c.getDeclaredFields();
	    try {
		for (n = 0; n < fields.length; n++) {
		    name = fields[n].getName();
		    if (name.charAt(0) == '$') {
			fields[n].set(c, null);
			if (debug)
			    VM.println(N_STDOUT, "  field " + name + " set to null");
		    }
		}
	    }
	    catch(Exception e) {}
	}
	catch(ClassNotFoundException e) {}
    }


    private synchronized void
    startThread() {

	YoixThread  thread;

	thread = new YoixThread(this);
	thread.setPriority(priority);
	thread.start();
    }
}

