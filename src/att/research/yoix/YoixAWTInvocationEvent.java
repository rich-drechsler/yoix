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
import java.awt.event.*;
import java.util.EventObject;

public
class YoixAWTInvocationEvent extends InvocationEvent

    implements YoixAPI,
	       YoixConstants,
	       YoixConstantsJFC,
	       Runnable

{

    private YoixObject  argv[] = null;
    private YoixObject  context = null;
    private Object      args[] = null;
    private Object      event = null;
    private long        when;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixAWTInvocationEvent() {

	this(YoixObject.newNull(), null, 0L);
    }


    public
    YoixAWTInvocationEvent(YoixObject source) {

	this(source, null, 0L);
    }


    public
    YoixAWTInvocationEvent(YoixObject source, Object event) {

	this(source, event, 0L);
    }


    public
    YoixAWTInvocationEvent(YoixObject source, Object event, long when) {

	super(source, null);
	this.event = event;
	this.when = (when > 0) ? when : System.currentTimeMillis();
	this.runnable = this;
    }


    public
    YoixAWTInvocationEvent(YoixObject source, YoixObject argv[], YoixObject context) {

	super(source, null);
	this.argv = (argv != null) ? argv : new YoixObject[0];
	this.context = context;
	this.when = System.currentTimeMillis();
	this.runnable = this;
    }


    public
    YoixAWTInvocationEvent(Object source, Object args[]) {

	super(source, null);
	this.args = args;
	this.when = System.currentTimeMillis();
	this.runnable = this;
    }

    ///////////////////////////////////
    //
    // Runnable Methods
    //
    ///////////////////////////////////

    public final void
    run() {

	YoixBodyComponent  body;
	YoixObject         yobj;
	Object             obj;

	//
	// The introduction of reflection to call "handleRun" is a recent
	// addition (7/17/06) that provides reasonable framework for other
	// classes that want to use this one to handle invokeLater().
	//

	if ((obj = getSource()) != null) {
	    if (args == null) {
		if (obj instanceof YoixObject) {
		    yobj = (YoixObject)obj;
		    if (yobj.isComponent()) {
			body = (YoixBodyComponent)yobj.body();
			if (event instanceof EventObject)
			    body.handleEventObject((EventObject)event);
			else if (event instanceof YoixObject)
			    body.handleYoixEvent((YoixObject)event);
			else body.handleEventObject(this);
		    } else if (yobj.isCallable())
			call(yobj);
		}
	    } else {
		if (obj instanceof YoixPointerActive)
		    ((YoixPointerActive)obj).handleRun(args);
		else YoixReflect.invoke(obj, "handleRun", new Object[] {args});
	    }
	}
    }

    ///////////////////////////////////
    //
    // YoixAWTInvocationEvent Methods
    //
    ///////////////////////////////////

    public final long
    getWhen() {

	return(when);
    }


    public final String
    toString() {

	//
	// Only defined because super.toString() would sometimes hang when
	// we used Java 1.3.1 on our SGI. We no longer support 1.3.1, but
	// didn't have time to make sure this could really be removed.
	//

	return(getClass().getName());
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixObject
    call(YoixObject function) {

	YoixObject  obj = null;
	YoixError   interrupt_point = null;
	YoixError   error_point = null;

	//
	// Be careful, java.awt.EventDispatchThread.run() can sometimes
	// generates unwanted noise!!
	//

	if (function != null) {
	    if (function.notNull()) {
		try {
		    error_point = VM.pushError();
		    try {
			interrupt_point = VM.pushInterrupt();
			obj = function.call(argv, context).resolve();
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

	return(obj);
    }
}

