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
import java.util.*;

final
class YoixBodyBuiltin

    implements YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCallable,
	       YoixInterfaceCloneable

{

    private boolean  varargs;
    private Object   extraargs[];
    private Method   method;
    private String   fullname;
    private String   methodname;
    private int      argc;

    //
    // Almost all Java methods that implement Yoix builtins take an
    // array of YoixObjects as their only argument and STANDARDARGS
    // describes the formal parameters to Class.getMethod(). A few
    // special builtins take an extra Object argument which is what
    // the EXTRAARGS array is used for.
    //

    private static final Class  STANDARDARGS[] = {(new YoixObject[1]).getClass()};
    private static final Class  EXTRAARGS[] = {STANDARDARGS[0], (new Object[1]).getClass()};

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyBuiltin(String fullname, int argc, boolean varargs) {

	this.fullname = fullname;
	this.method = null;
	this.argc = Math.abs(argc);
	this.varargs = varargs;
	this.extraargs = null;
    }


    YoixBodyBuiltin(String fullname, int argc, boolean varargs, Object extraargs[]) {

	this.fullname = fullname;
	this.method = null;
	this.argc = Math.abs(argc);
	this.varargs = varargs;
	this.extraargs = extraargs;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public final synchronized Object
    clone() {

	Object  obj;

	try {
	    obj = super.clone();
	}
	catch(CloneNotSupportedException e) {
	    obj = VM.die(INTERNALERROR);
	}

	return(obj);
    }


    public final Object
    copy(HashMap copied) {

	return(clone());
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final String
    dump() {

	String  str;
	int     n;

	str = fullname + "(";

	for (n = 1; n <= argc; n++)
	    str += (n == 1 ? "" : ", ") + "arg" + n;
	if (varargs)
	    str += (n == 1 ? "..." : ", ...");

	return(str + ")" + NL);
    }


    public final int
    length() {

	return(argc);
    }


    public final String
    toString() {

	return(dump().trim());
    }


    public final int
    type() {

	return(BUILTIN);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCallable Methods
    //
    ///////////////////////////////////

    public final YoixObject
    call(YoixObject argv[], YoixObject context) {

	YoixObject  result = null;
	YoixError   error_point = null;
	Throwable   t;

	//
	// Proper error handling probably means a more work than you
	// might expect. Still room for improvement!!
	//

	if (method == null)
	    load();

	if (argv.length == argc || (varargs && argv.length > argc)) {
	    try {
		error_point = VM.pushError(OFFENDINGBUILTIN, methodname);
		if (extraargs == null)
		    result = (YoixObject)method.invoke(null, new Object[] {argv});
		else result = (YoixObject)method.invoke(null, new Object[] {argv, extraargs});
		VM.popError();
	    }
	    catch(InvocationTargetException e) {
		VM.caughtException(e);
		if ((t = e.getTargetException()) != error_point) {
		    if (t instanceof YoixError)
			throw((YoixError)t);
		    else {
			VM.popError();
			if (t instanceof SecurityException)
			    throw((SecurityException)t);
			else if (t instanceof ThreadDeath)
			    throw((ThreadDeath)t);
			VM.abort(t);
		    }
		} else VM.jumpToError(error_point.getDetails());
	    }
	    catch(Exception e) {
		if (e instanceof SecurityException)
		    throw((SecurityException)e);
		VM.die(INTERNALERROR);	// should never get here
	    }
	} else VM.badCall(methodname);

	return(result != null ? result : YoixObject.newEmpty());
    }


    public final boolean
    callable(int argc) {

	return(argc == this.argc || (varargs && argc > this.argc));
    }


    public final boolean
    callable(YoixObject argv[]) {

	return(callable(argv.length));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    load() {

	Class  owner;
	int    dot;

	if (method == null) {
	    dot = fullname.lastIndexOf('.');
	    try {
		owner = Class.forName(fullname.substring(0, dot));
		methodname = fullname.substring(dot + 1);
		method = owner.getMethod(
		    methodname,
		    extraargs == null ? STANDARDARGS : EXTRAARGS
		);
	    }
	    catch(RuntimeException e) {
		VM.caughtException(e);
		VM.abort(UNDEFINEDBUILTIN, fullname);
	    }
	    catch(ClassNotFoundException e) {
		VM.caughtException(e);
		VM.abort(UNDEFINEDCLASS, fullname.substring(0, dot));
	    }
	    catch(NoSuchMethodException e) {
		VM.caughtException(e);
		VM.abort(UNDEFINEDBUILTIN, fullname);
	    }
	}
    }
}

