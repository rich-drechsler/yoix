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
import java.io.*;
import java.net.*;
import java.util.*;

class YoixInterruptable

    implements Runnable,
	       YoixConstants,
	       YoixInterfaceKillable

{

    //
    // This class represents an attempt to provide interrupt support for
    // operations, currently only a few IO related methods, that can't
    // seem to be handled any other way. There's a very good description
    // at
    //
    //   http://developer.java.sun.com/developer/bugParade/bugs/4154947.html
    //
    // that you should look at (assuming you belong to the Java Developer
    // Connection) if you're not familiar with the issues. Our approach
    // really consists of two parts, and even though it's not a perfect
    // solution to a difficult problem, we think we've made a reasonably
    // good try a providing interruptable IO.
    //
    // Blocked reads and writes are handled outside this class, because
    // we wanted to avoid thread overhead and when they don't respond to
    // interrupts (e.g., under Windows) they do seem to notice a close() of
    // the underlying stream. This class was added to try to help with
    // other calls (e.g. URLConnection.getInputStream()) that don't seem
    // to respond to interrupts or any other obvious mechanism, but the
    // overhead also means it should be used as a last resort. There are
    // no guarantees with this stuff, except that we will keep trying to
    // improve the interruptable IO provided by Yoix.
    //

    private Object  source;

    private YoixThread  thread;
    private Exception   caught;
    private boolean     running;
    private Object      result;
    private Object      arg;
    private int         operation;

    //
    // Constants that describe the currently supported operations.
    //

    private static final int  ACCEPT = 1;
    private static final int  GETINPUTSTREAM = 2;
    private static final int  GETOUTPUTSTREAM = 3;
    private static final int  RECEIVE = 4;

    //
    // This controls whether we try stop(), which has been deprecated
    // and can sometimes cause other problems, when it looks like the
    // the thread that's handling the interruptable operation is still
    // running. Some platforms (e.g., Linux and 1.4.X) have misbehaved
    // after stop() is called, so we may eventually have to skip it.
    //

    private static final boolean  STOPPABLE = true;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixInterruptable(Object source) {

	this.source = source;
    }

    ///////////////////////////////////
    //
    // Runnable Methods
    //
    ///////////////////////////////////

    public final void
    run() {

	try {
	    switch (operation) {
		case ACCEPT:
		    if (source instanceof ServerSocket)
			result = ((ServerSocket)source).accept();
		    break;

		case GETINPUTSTREAM:
		    if (source instanceof URLConnection)
			result = ((URLConnection)source).getInputStream();
		    break;

		case GETOUTPUTSTREAM:
		    if (source instanceof URLConnection)
			result = ((URLConnection)source).getOutputStream();
		    break;

		case RECEIVE:
		    if (source instanceof DatagramSocket) {
			((DatagramSocket)source).receive((DatagramPacket)arg);
			result = Boolean.TRUE;
		    }
		    break;
	    }
	}
	catch(Exception e) {
	    caught = e;
	}
	finally {
	    try {
		synchronized(this) {
		    notifyAll();
		}
	    }
	    catch(Exception e) {}
	}
    }

    ///////////////////////////////////
    //
    // YoixInterfaceKillable Methods
    //
    ///////////////////////////////////

    public final void
    kill() {

    }

    ///////////////////////////////////
    //
    // YoixInterruptable Methods
    //
    ///////////////////////////////////

    protected final void
    finalize() {

	thread = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final Socket
    accept()

	throws IOException

    {

	try {
	    operation = ACCEPT;
	    caught = null;
	    result = null;
	    startThread();
	}
	finally {
	    if (Thread.currentThread().isInterrupted())
		throw(new InterruptedIOException());
	    else if (caught != null)
		throw(new IOException(caught.getMessage()));
	    else if (result == null)
		throw(new IOException());
	}

	return((Socket)result);
    }


    final InputStream
    getInputStream()

	throws IOException

    {

	try {
	    operation = GETINPUTSTREAM;
	    caught = null;
	    result = null;
	    startThread();
	}
	finally {
	    if (Thread.currentThread().isInterrupted())
		throw(new InterruptedIOException());
	    else if (caught != null)
		throw(new IOException(caught.getMessage()));
	    else if (result == null)
		throw(new IOException());
	}

	return((InputStream)result);
    }


    final OutputStream
    getOutputStream()

	throws IOException

    {

	try {
	    operation = GETOUTPUTSTREAM;
	    caught = null;
	    result = null;
	    startThread();
	}
	finally {
	    if (Thread.currentThread().isInterrupted())
		throw(new InterruptedIOException());
	    else if (caught != null)
		throw(new IOException(caught.getMessage()));
	    else if (result == null)
		throw(new IOException());
	}

	return((OutputStream)result);
    }


    final void
    receive(DatagramPacket p)

	throws IOException

    {

	try {
	    operation = RECEIVE;
	    caught = null;
	    arg = p;
	    result = null;
	    startThread();
	}
	finally {
	    if (Thread.currentThread().isInterrupted())
		throw(new InterruptedIOException());
	    else if (caught != null)
		throw(new IOException(caught.getMessage()));
	    else if (result == null)
		throw(new IOException());
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    startThread() {

	String  message;

	//
	// We realize stop() has been deprecated, but thread was only
	// started to handle the uninterruptable operation, so stop()
	// probably is the only way the operation can be interrupted.
	// In addition, thread is completely isolated from the rest of
	// Yoix, so stop(), if it works, shouldn't corrupt any of the
	// interpreter's data structures.
	//

	if (thread == null) {
	    try {
		thread = new YoixThread(this);
		running = true;
		thread.start();
		try {
		    wait();
		}
		catch(InterruptedException e) {
		    Thread.currentThread().interrupt();
		}
	    }
	    catch(IllegalThreadStateException e) {}
	    finally {
		running = false;
		if (thread != null) {
		    if (thread.isAlive()) {
			if (running && STOPPABLE) {
			    if (VM.bitCheck(N_DEBUG, DEBUG_STOPTHREAD)) {
				message = "stop[" + operation + "] " + thread;
				VM.println(N_STDOUT, "stop[" + operation + "] " + thread);
				VM.saveException(message);
			    }
			    thread.stop();
			}
			if (running) {
			    if (VM.bitCheck(N_DEBUG, DEBUG_STOPTHREAD)) {
				message = "thread " + thread +
				    " handling interruptable operation " + operation +
				    " has" + (STOPPABLE ? " " : " not " + "been stopped");
				VM.println(N_STDOUT, message);
				VM.saveException(message);
			    }
			    if (STOPPABLE)
				thread.stop();
			}
		    }
		    thread = null;
		}
	    }
	} else VM.die(INTERNALERROR);	// careless coding - it should not happen!!
    }
}

