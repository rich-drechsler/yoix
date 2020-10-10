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

class YoixVMDisposer

    implements Runnable,
	       YoixConstants

{

    //
    // Thread that we use to dipose windows that won't be interrupted,
    // so we won't have to worry that dispose() failed or that we might
    // get an unwanted Java dump just because a Yoix thread was killed
    // or interrupted. Seems to behave properly, but it was added very
    // quickly so it deserves another a careful look - later.
    //

    private Object  window;
    private int     priority;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixVMDisposer(Object window) {

	this(window, Thread.currentThread().getPriority());
    }


    YoixVMDisposer(Object window, int priority) {

	this.window = window;
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

	dispose(window);
	window = null;
    }

    ///////////////////////////////////
    //
    // YoixVMCleaner Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	window = null;
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
    dispose(Object window) {

	if (VM.bitCheck(N_DEBUG, DEBUG_DISPOSE))
	    VM.println(N_STDOUT, "dispose " + window);

	if (window instanceof Window)
	    ((Window)window).dispose();
	else if (window instanceof YoixInterfaceWindow)
	    ((YoixInterfaceWindow)window).dispose();
    }


    private synchronized void
    startThread() {

	YoixThread  thread;

	thread = new YoixThread(this);
	thread.setPriority(priority);
	thread.setDaemon(true);
	thread.start();
    }
}

