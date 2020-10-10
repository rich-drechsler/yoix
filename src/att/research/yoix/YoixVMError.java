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

public abstract
class YoixVMError

    implements YoixAPI,
	       YoixConstants

{

    //
    // These are the error handling methods that are supported by the Yoix
    // virtual machine. This class is indirectly extended by YoixVM.java
    // and was separated from it primarily to reduce the clutter from all
    // the new methods. We tried to improve the Yoix error handling and
    // reporting, which means you may notice lots of changes. Three new
    // methods, namely badCall(), badArgument(), and badArgumentValue()
    // are used to report errors that occur when a builtin (or function)
    // is called with the wrong number of arguments, when an argument is
    // the wrong type, or when an argument has a value that's not allowed.
    // We also added new variations of abort(), warn(), and die() that
    // let you supply an array of strings (which should be organized in
    // pairs) that can be used when you need to add extra information to
    // the error message.
    //

    private static boolean  dead = false;
    private static boolean  lockedlimit = false;
    private static int      errorcount = 0;
    private static int      errorlimit = 0;

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final YoixObject
    abort(String error) {

	return(handleAbort(error, null, null, null));
    }


    public final YoixObject
    abort(String error, String extra[]) {

	return(handleAbort(error, null, null, extra));
    }


    public final YoixObject
    abort(String error, int index) {

	return(handleAbort(error, OFFENDINGINDEX, index + "", null));
    }


    public final YoixObject
    abort(String error, int index, String extra[]) {

	return(handleAbort(error, OFFENDINGINDEX, index + "", extra));
    }


    public final YoixObject
    abort(String error, String name) {

	return(handleAbort(error, OFFENDINGNAME, name, null));
    }


    public final YoixObject
    abort(String error, String name, String extra[]) {

	return(handleAbort(error, OFFENDINGNAME, name, extra));
    }


    public final YoixObject
    abort(String error, String name, String field) {

	return(handleAbort(error, OFFENDINGENTRY, name + "." + field, null));
    }


    public final YoixObject
    abort(String error, String name, String field, String extra[]) {

	return(handleAbort(error, OFFENDINGENTRY, name + "." + field, extra));
    }


    public final YoixObject
    abort(String error, String name, int index) {

	return(handleAbort(error, OFFENDINGENTRY, name + "[" + index + "]", null));
    }


    public final YoixObject
    abort(String error, String name, int index, String extra[]) {

	return(handleAbort(error, OFFENDINGENTRY, name + "[" + index + "]", extra));
    }


    public final YoixObject
    abort(String error, String name, int index, String field) {

	return(handleAbort(error, OFFENDINGENTRY, name + "[" + index + "]." + field, null));
    }


    public final YoixObject
    abort(String error, String name, int index, String field, String extra[]) {

	return(handleAbort(error, OFFENDINGENTRY, name + "[" + index + "]." + field, extra));
    }


    public final YoixObject
    abort(String error, String name, int index1, int index2) {

	return(handleAbort(error, OFFENDINGENTRY, name + "[" + index1 + "][" + index2 + "]", null));
    }


    public final YoixObject
    abort(String error, String name, int index1, int index2, String extra[]) {

	return(handleAbort(error, OFFENDINGENTRY, name + "[" + index1 + "][" + index2 + "]", extra));
    }


    public final YoixObject
    abort(String error, ArrayList args, Throwable t) {

	return(handleAbort(error, args, t));
    }


    public final YoixObject
    abort(String error, String args[], Throwable t) {

	return(handleAbort(error, args, null));
    }


    public final YoixObject
    badArgument(int argn) {

	return(handleCallableAbort(BADARGUMENT, OFFENDINGBUILTIN, null, argn+1, null));
    }


    public final YoixObject
    badArgument(int argn, String extra[]) {

	return(handleCallableAbort(BADARGUMENT, OFFENDINGBUILTIN, null, argn+1, extra));
    }


    public final YoixObject
    badArgument(String name, int argn) {

	return(handleCallableAbort(BADARGUMENT, OFFENDINGBUILTIN, name, argn+1, null));
    }


    public final YoixObject
    badArgument(String name, int argn, String extra[]) {

	return(handleCallableAbort(BADARGUMENT, OFFENDINGBUILTIN, name, argn+1, extra));
    }


    public final YoixObject
    badArgument(String tag, String name, int argument) {

	//
	// Function call errors automatically end up here with tag set to
	// OFFENDINGFUNCTION (see YoixBodyFunction.java). That also means
	// we can safely assume the argument index already identifies the
	// offending argument so this method doesn't increment.
	//

	return(handleCallableAbort(BADARGUMENT, tag, name, argument, null));
    }


    public final YoixObject
    badArgumentValue(int argn) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, null, argn+1, null));
    }


    public final YoixObject
    badArgumentValue(int argn, String extra[]) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, null, argn+1, extra));
    }


    public final YoixObject
    badArgumentValue(int argn, String field) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, null, "arg" + (argn+1) + "." + field, null));
    }


    public final YoixObject
    badArgumentValue(int argn, String field, String extra[]) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, null, "arg" + (argn+1) + "." + field, extra));
    }


    public final YoixObject
    badArgumentValue(int argn, int index) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, null, "arg" + (argn+1) + "[" + index + "]", null));
    }


    public final YoixObject
    badArgumentValue(int argn, int index, String extra[]) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, null, "arg" + (argn+1) + "[" + index + "]", extra));
    }


    public final YoixObject
    badArgumentValue(String name, int argn) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, name, argn+1, null));
    }


    public final YoixObject
    badArgumentValue(String name, int argn, String extra[]) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, name, argn+1, extra));
    }


    public final YoixObject
    badArgumentValue(String name, int argn, String field) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, name, "arg" + (argn+1) + "." + field, null));
    }


    public final YoixObject
    badArgumentValue(String name, int argn, String field, String extra[]) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, name, "arg" + (argn+1) + "." + field, extra));
    }


    public final YoixObject
    badArgumentValue(String name, int argn, int index) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, name, "arg" + (argn+1) + "[" + index + "]", null));
    }


    public final YoixObject
    badArgumentValue(String name, int argn, int index, String extra[]) {

	return(handleCallableAbort(BADVALUE, OFFENDINGBUILTIN, name, "arg" + (argn+1) + "[" + index + "]", extra));
    }


    public final YoixObject
    badArgumentValue(String tag, String name) {

	return(handleCallableAbort(BADVALUE, tag, name, null, null));
    }


    public final YoixObject
    badArgumentValue(String tag, String name, String extra[]) {

	return(handleCallableAbort(BADVALUE, tag, name, null, extra));
    }


    public final YoixObject
    badCall() {

	return(handleCallableAbort(BADCALL, OFFENDINGBUILTIN, null, null, null));
    }


    public final YoixObject
    badCall(String extra[]) {

	return(handleCallableAbort(BADCALL, OFFENDINGBUILTIN, null, null, extra));
    }


    public final YoixObject
    badCall(String name) {

	return(handleCallableAbort(BADCALL, OFFENDINGBUILTIN, name, null, null));
    }


    public final YoixObject
    badCall(String name, String extra[]) {

	return(handleCallableAbort(BADCALL, OFFENDINGBUILTIN, name, null, extra));
    }


    public final YoixObject
    badCall(String tag, String name) {

	//
	// Function call errors automatically end up here with tag set to
	// OFFENDINGFUNCTION (see YoixBodyFunction.java).
	//

	return(handleCallableAbort(BADCALL, tag, name, null, null));
    }


    public final YoixObject
    die(String error) {

	return(handleDie(error, null, null, null));
    }


    public final YoixObject
    die(String error, String extra[]) {

	return(handleDie(error, null, null, extra));
    }


    public final YoixObject
    die(String error, int index) {

	return(handleDie(error, OFFENDINGINDEX, index + "", null));
    }


    public final YoixObject
    die(String error, int index, String extra[]) {

	return(handleDie(error, OFFENDINGINDEX, index + "", extra));
    }


    public final YoixObject
    die(String error, String name) {

	return(handleDie(error, OFFENDINGNAME, name, null));
    }


    public final YoixObject
    die(String error, String name, String extra[]) {

	return(handleDie(error, OFFENDINGNAME, name, extra));
    }


    public final YoixObject
    warn(String error) {

	return(handleWarn(error, null, null, null));
    }


    public final YoixObject
    warn(String error, String extra[]) {

	return(handleWarn(error, null, null, extra));
    }


    public final YoixObject
    warn(String error, int index) {

	return(handleWarn(error, OFFENDINGINDEX, index + "", null));
    }


    public final YoixObject
    warn(String error, int index, String extra[]) {

	return(handleWarn(error, OFFENDINGINDEX, index + "", extra));
    }


    public final YoixObject
    warn(String error, String name) {

	return(handleWarn(error, OFFENDINGNAME, name, null));
    }


    public final YoixObject
    warn(String error, String name, String extra[]) {

	return(handleWarn(error, OFFENDINGNAME, name, extra));
    }


    public final YoixObject
    warn(String error, ArrayList args, Throwable t) {

	return(handleWarn(error, args, t));
    }


    public final YoixObject
    warn(String error, String args[], Throwable t) {

	return(handleWarn(error, args, null));
    }

    ///////////////////////////////////
    //
    // YoixVMError Methods
    //
    ///////////////////////////////////

    final YoixObject
    abort(YoixObject details) {

	String  error;
	String  args[];

	//
	// A new and somewhat oscure abort() method that's currently only
	// used when we want to transfer information about an error that
	// happened in one thread to another thread that really should be
	// handling the error. Right now it's only used by swing methods
	// when we're running in threadsafe mode.
	//

	error = details.getString(N_NAME);
	args = YoixMake.javaStringArray(details.getObject(N_ARGS, YoixObject.newNull()));
	return(handleAbort(error, args, null));
    }


    final YoixObject
    abort(Throwable t) {

	return(abort(EXCEPTION, t));
    }


    final YoixObject
    abort(String error, Throwable t) {

	String  args[];
	String  message;

	VM.recordException(t);

	if ((message = t.getMessage()) != null)
	    args = new String[] {OFFENDINGMESSAGE, "\"" + message + "\""};
	else args = new String[] {OFFENDINGNAME, t.toString()};

	return(handleAbort(error, args, t));
    }


    final YoixObject
    die(Throwable t) {

	String  args[];
	String  message;

	if ((message = t.getMessage()) != null)
	    args = new String[] {OFFENDINGMESSAGE, "\"" + message + "\""};
	else args = new String[] {OFFENDINGNAME, t.toString()};

	return(handleDie(EXCEPTION, args, t, true));
    }


    final YoixObject
    die(String error, String args[], Throwable t) {

	return(handleDie(error, args, t, true));
    }


    final void
    error(YoixError e) {

	handleError(e.getDetails());
    }


    final void
    error(SecurityException e) {

	String  message;

	if ((message = e.getMessage()) != null && message.length() > 0)
	    handleError(YoixError.recordDetails(SECURITYCHECK, new String[] {message}, e));
	else handleError(YoixError.recordDetails(SECURITYCHECK, e));
    }


    final void
    error(UnsupportedOperationException e) {

	handleError(YoixError.recordDetails(UNSUPPORTEDOPERATION, e));
    }


    final int
    getErrorCount() {

	return(errorcount);
    }


    final int
    getErrorLimit() {

	return(errorlimit);
    }


    final synchronized int
    setErrorCount(int count) {

	int  previous = errorcount;

	errorcount = Math.max(0, count);
	return(previous);
    }


    final synchronized int
    setErrorLimit(int limit) {

	int  previous = errorlimit;

	if (lockedlimit == false)
	    errorlimit = Math.max(0, limit);
	return(previous);
    }


    final synchronized int
    setErrorLimit(String str, boolean locked) {

	int  previous = errorlimit;
	int  limit = 0;

	try {
	    limit = Integer.parseInt(str, 10);
	}
	catch(NumberFormatException e) {}

	if (lockedlimit == false) {
	    setErrorLimit(limit);
	    lockedlimit = locked;
	}
	return(previous);
    }


    final YoixObject
    warn(Throwable t) {

	String  args[];
	String  message;

	if ((message = t.getMessage()) != null)
	    args = new String[] {OFFENDINGMESSAGE, "\"" + message + "\""};
	else args = new String[] {OFFENDINGNAME, t.toString()};

	return(handleWarn(EXCEPTION, args, t));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixObject
    handleAbort(String error, ArrayList args, Throwable t) {

	return(handleAbort(error, YoixMake.javaStringArray(args), t));
    }


    private YoixObject
    handleAbort(String error, String args[], Throwable t) {

	YoixObject  details;
	boolean     trace = true;
	String      message;

	//
	// All aborts eventually end up here. If args isn't null it should
	// consist of pairs of strings that are added into the final error
	// message by YoixError.recordDetails() (actually the real work is
	// done by YoixError.formatMessage()). The first string in the pair
	// is normally a one word tag (e.g., OFFENDINGNAME) that's added to
	// the error message, followed by a colon, a space, and then second
	// string in the pair, which is supposed to provide info about the
	// error that's related to the tag. The dictionary that's returned
	// by YoixError.recordDetails() includes the error message along
	// with other useful information (e.g., stack traces) can be dumped
	// when we magically (via a throw() generated by jumpToError() that
	// is caught elsewhere) get to handleError().
	//

	if (YoixVMThread.isHandlingError() == false) {
	    try {
		if (VM.isBooted() || YoixVMThread.isRunning()) {
		    YoixVMThread.setHandlingError(true);
		    details = YoixError.recordDetails(error, args, t);
		    message = details.getString(N_MESSAGE, "");
		    if (message.length() > 0)
			VM.saveException(message);
		    VM.saveException(t, true);
		    VM.jumpToError(details);
		}
		trace = VM.isBooting();
	    }
	    finally {
		YoixVMThread.setHandlingError(false);
	    }
	} else error = ERRORLOOP;

	return(handleDie(error, args, null, trace));
    }


    private YoixObject
    handleAbort(String error, String tag, String name, String extra[]) {

	ArrayList  args = new ArrayList();
	int        n;

	if (tag != null && name != null) {
	    args.add(tag);
	    args.add(name);
	}
	if (extra != null) {
	    for (n = 0; n < extra.length - 1; n += 2) {
		args.add(extra[n]);	// usually a one word tag - no colon
		args.add(extra[n+1]);	// extra info related to that tag
	    }
	    if (n < extra.length) {
		args.add(OFFENDINGINFO);
		args.add(extra[n]);
	    }
	}
	return(handleAbort(error, YoixMake.javaStringArray(args), null));
    }


    private YoixObject
    handleCallableAbort(String error, String tag, String name, int argument, String extra[]) {

	return(handleCallableAbort(error, tag, name, new Integer(argument), extra));
    }


    private YoixObject
    handleCallableAbort(String error, String tag, String name, Object arg, String extra[]) {

	ArrayList  args = new ArrayList();
	int        n;

	if (tag != null && name != null) {
	    args.add(tag);
	    args.add(name);
	}
	if (arg != null) {
	    if (arg instanceof Integer) {
		if (((Integer)arg).intValue() > 0) {
		    args.add(OFFENDINGARGUMENT);
		    args.add(((Integer)arg).toString());
		}
	    } else if (arg instanceof String) {
		args.add(OFFENDINGENTRY);
		args.add(arg);
	    }
	}
	if (extra != null) {
	    for (n = 0; n < extra.length - 1; n += 2) {
		args.add(extra[n]);	// usually a one word tag - no colon
		args.add(extra[n+1]);	// extra info related to that tag
	    }
	    if (n < extra.length) {
		args.add(OFFENDINGINFO);
		args.add(extra[n]);
	    }
	}
	return(handleAbort(error, YoixMake.javaStringArray(args), null));
    }


    private YoixObject
    handleDie(String error, String args[], Throwable t, boolean trace) {

	YoixObject  details;

	if (dead == false) {
	    dead = true;
	    YoixVMThread.getThreadStack().reset();	// protection from earlier stackunderflow
	    if (error != null) {
		details = YoixError.recordDetails(FATALERROR, error, args, t);
		VM.println(N_STDERR, details.getString(N_MESSAGE, ""));
	    }

	    if (trace) {
		if (t == null)
		    t = new Throwable(PREFIX_JAVASTACKTRACE);
		VM.print(N_STDERR, YoixMisc.javaTrace(t));
	    }
	}

	VM.exit(1);
	return(null);
    }


    private YoixObject
    handleDie(String error, String tag, String name, String extra[]) {

	ArrayList  args = new ArrayList();
	int        n;

	if (tag != null && name != null) {
	    args.add(tag);
	    args.add(name);
	}
	if (extra != null) {
	    for (n = 0; n < extra.length - 1; n += 2) {
		args.add(extra[n]);	// usually a one word tag - no colon
		args.add(extra[n+1]);	// extra info related to that tag
	    }
	    if (n < extra.length) {
		args.add(OFFENDINGINFO);
		args.add(extra[n]);
	    }
	}
	return(handleDie(error, YoixMake.javaStringArray(args), null, true));
    }


    private void
    handleError(YoixObject details) {

	YoixObject  dict;
	String      message;
	int         limit;
	int         count;

	//
	// This is the last stop for an abort, but how we get here probably
	// is not obvious. An earlier abort() call got into handleAbort(),
	// and the jumpToError() in handleAbort() popped the stack down to
	// an error object, recorded error information in that error object,
	// and then threw the error that was caught by the appropriate Java
	// method (e.g., YoixBodyBuiltin.call()). Anyway, that method takes
	// care of required cleanup and then calls error() (which is defined
	// above) before it returns, and that's how we end up here with all
	// the information about the error.
	//

	if (details.isDictionary()) {
	    message = details.getString(N_MESSAGE, "");
	    if (message.length() > 0)
		VM.println(N_STDERR, message);
	    if (VM.getInt(N_TRACE) != 0)
		VM.print(N_STDERR, details.getString(N_STACKTRACE, ""));
	    if (VM.bitCheck(N_DEBUG, DEBUG_JAVATRACE))
		VM.print(N_STDERR, details.getString(N_JAVATRACE, ""));
	    count = ++errorcount;
	    limit = errorlimit;
	    if (VM.isShutdown() == false) {
		if (limit > 0 && count >= limit) {
		    handleDie(
			LIMITCHECK,
			new String[] {"error limit of " + count + " has been reached"},
			null, false
		    );
		}
	    }
	    if ((dict = YoixBodyBlock.getErrordict()) != null)
		YoixMisc.copyInto(details, dict);
	} else die(INTERNALERROR);
    }


    private YoixObject
    handleWarn(String error, ArrayList args, Throwable t) {

	return(handleWarn(error, YoixMake.javaStringArray(args), t));
    }


    private YoixObject
    handleWarn(String error, String args[], Throwable t) {

	YoixObject  details;

	details = YoixError.recordDetails(WARNINGMESSAGE, error, args, t);
	if (VM.isBooted()) {		// is this necessary??
	    VM.println(N_STDERR, details.getString(N_MESSAGE, ""));
	    if (VM.getInt(N_TRACE) != 0)
		VM.print(N_STDERR, details.getString(N_STACKTRACE, ""));
	    if (VM.bitCheck(N_DEBUG, DEBUG_JAVATRACE))
		VM.print(N_STDERR, details.getString(N_JAVATRACE, ""));
	} else System.err.println(details.getString(N_MESSAGE, ""));

	return(null);
    }


    private YoixObject
    handleWarn(String error, String tag, String name, String extra[]) {

	ArrayList  args = new ArrayList();
	int        n;

	if (tag != null && name != null) {
	    args.add(tag);
	    args.add(name);
	}
	if (extra != null) {
	    for (n = 0; n < extra.length - 1; n += 2) {
		args.add(extra[n]);	// usually a one word tag - no colon
		args.add(extra[n+1]);	// extra info related to that tag
	    }
	    if (n < extra.length) {
		args.add(OFFENDINGINFO);
		args.add(extra[n]);
	    }
	}
	return(handleWarn(error, YoixMake.javaStringArray(args), null));
    }
}

