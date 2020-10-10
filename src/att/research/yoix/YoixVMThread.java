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

class YoixVMThread

    implements YoixConstants

{

    //
    // Very important code that's heavily used and responsible for much
    // of the thread-safe behavior of the interpreter. Each thread that
    // runs a yoix program gets its own stack and a copy other important
    // thread data. Still lots of room for improvement, particularly in
    // getThreadData(), but be very careful!!
    //

    private static YoixVMThreadData  threaddata[];
    private static int               last = 0;

    //
    // These constants control how the threaddata[] array grows.
    //

    private static final int  INITIAL_CAPACITY = 3;
    private static final int  CAPACITY_INCREMENT = 5;

    //
    // Code assumes threaddata[] isn't empty, never contains null values,
    // and the values never move.
    //

    static {
	threaddata = new YoixVMThreadData[INITIAL_CAPACITY];
	for (int n = 0; n < threaddata.length; n++)
	    threaddata[n] = new YoixVMThreadData(null);
    }

    ///////////////////////////////////
    //
    // YoixVMThread Methods
    //
    ///////////////////////////////////

    static synchronized int
    activeCount() {

	Thread  thread;
	int     count;
	int     n;

	for (n = count = 0; n < threaddata.length; n++) {
	    if ((thread = threaddata[n].thread) != null && thread.isAlive())
		count++;
	}
	return(count);
    }


    static synchronized String
    dump(String target) {

	return(dump(target, VM.isBooted() ? VM.getInt(N_TRACE) : 0, true));
    }


    static synchronized String
    dump(String target, int model) {

	return(dump(target, model, true));
    }


    static synchronized String
    dump(String target, int model, boolean labeled) {

	String  str = "";
	String  prefix;
	String  name;
	int     n;

	if ((prefix = YoixStack.dumpPrefix(model)) != null) {
	    if (target != null) {
		for (n = 0; n < threaddata.length; n++) {
		    if ((name = threaddata[n].name()) != null) {
			if (target.equals("*") || target.equals(name)) {
			    if (labeled)
				str += prefix + " " + threaddata[n].status() + NL;
			    str += threaddata[n].stack.dump(model);
			}
		    }
		}
	    } else {
		str = getThreadStack().dump(model);
		if (labeled && str != null && str.length() > 0)
		    str = prefix + NL + str;
	    }
	}

	return(str);
    }


    static YoixBodyBlock
    getCurrentBlock() {

	return(getThreadData(Thread.currentThread()).block);
    }


    static int
    getThreadAccess() {

	return(getThreadData(Thread.currentThread()).access);
    }


    static YoixVMThreadData
    getThreadData() {

	return(getThreadData(Thread.currentThread()));
    }


    static YoixStack
    getThreadStack() {

	return(getThreadStack(Thread.currentThread()));
    }


    static boolean
    isHandlingError() {

	return(getThreadData(Thread.currentThread()).handlingerror);
    }


    static boolean
    isInterruptable() {

	return(getThreadData(Thread.currentThread()).interruptable);
    }


    static boolean
    isLoadingType() {

	return(getThreadData(Thread.currentThread()).loadingtype);
    }


    static boolean
    isRunning() {

	return(getThreadStack().getHeight() > 0);
    }


    static void
    reset() {

	//
	// Brute force approach that may only be allowed when we're in the
	// process of shutting down (i.e., thread is a YoixVMShutdownThread).
	//

	getThreadData(Thread.currentThread()).reset();
    }


    static void
    setCurrentBlock(YoixBodyBlock block) {

	getThreadData(Thread.currentThread()).block = block;
    }


    static void
    setHandlingError(boolean state) {

	getThreadData(Thread.currentThread()).handlingerror = state;
    }


    static void
    setInterruptable(boolean state) {

	getThreadData(Thread.currentThread()).interruptable = state;
    }


    static void
    setLoadingType(boolean state) {

	getThreadData(Thread.currentThread()).loadingtype = state;
    }


    static void
    setThreadAccess(int perm) {

	getThreadData(Thread.currentThread()).access = perm;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static YoixVMThreadData
    getThreadData(Thread target) {

	YoixVMThreadData  data = threaddata[last];	// snapshot - just to be safe

	if (data.thread != target)
	    data = pickThreadData(target);
	return(data);
    }


    private static YoixStack
    getThreadStack(Thread target) {

	YoixVMThreadData  data = threaddata[last];	// snapshot - just to be safe

	if (data.thread != target)
	    data = pickThreadData(target);
	return(data.stack);
    }


    private static synchronized int
    growThreadData() {

	YoixVMThreadData  newdata[];
	int               length;
	int               n;

	//
	// We guarantee entries in threaddata[] aren't null, so others don't
	// have to check. Return value is the old length, which is the next
	// available slot in the expanded threaddata[] array.
	//

	length = threaddata.length;
	newdata = new YoixVMThreadData[length + CAPACITY_INCREMENT];
	System.arraycopy(threaddata, 0, newdata, 0, length);
	for (n = length; n < newdata.length; n++)
	    newdata[n] = new YoixVMThreadData(null);
	threaddata = newdata;
	return(length);
    }


    private static synchronized YoixVMThreadData
    pickThreadData(Thread target) {

	int  length;
	int  count;
	int  next;
	int  n;

	//
	// This shouldn't happen all that much, so we often take time to
	// look at (and clean up) all entries. Not hard to imagine some
	// improvements, but properly maintaining the last index should
	// reduce the number of times this method is called.
	//

	length = threaddata.length;
	n = (last + 1)%length;
	next = -1;

	for (count = length; count > 0; count--) {
	    if (threaddata[n].active()) {
		if (target == threaddata[n].thread)
		    return(threaddata[last = n]);
		else if (!threaddata[n].thread.isAlive())
		    threaddata[next = n].reset(null);
	    } else next = n;
	    n = (n + 1)%length;
	}

	if (next < 0)
	    next = growThreadData();
	threaddata[next].reset(target);
	last = next;
	return(threaddata[next]);
    }
}

