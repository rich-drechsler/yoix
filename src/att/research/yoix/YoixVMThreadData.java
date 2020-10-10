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

class YoixVMThreadData

    implements YoixConstants

{

    //
    // Data that needs to be maintained separately for every thread is
    // stored in one of these - there probably will be more!
    //

    YoixBodyBlock  block;
    YoixStack      stack = new YoixStack();
    boolean        handlingerror;
    boolean        interruptable;
    boolean        loadingtype;
    Thread         thread;
    int            access;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixVMThreadData(Thread owner) {

	reset(owner);
    }

    ///////////////////////////////////
    //
    // YoixVMThreadData Methods
    //
    ///////////////////////////////////

    final boolean
    active() {

	return(thread != null);
    }


    final String
    name() {

	return(thread != null ? thread.getName() : null);
    }


    final void
    reset() {

	if (thread instanceof YoixVMShutdownThread)
	    reset(thread);
    }


    final void
    reset(Thread owner) {

	thread = owner;
	handlingerror = false;
	interruptable = false;
	loadingtype = false;
	block = null;
	access = 0;
	stack.reset();
    }


    final String
    status() {

	String  str;

	if (thread != null) {
	    str = name() + ", " + access + ", ";
	    if (thread == Thread.currentThread())
		str += "RUNNING";
	    else str += (thread.isAlive()) ? "WAITING" : "STOPPED";
	} else str = "???, DEAD";	// can this happen??

	return("<" + str + ">");
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

}

