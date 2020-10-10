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
class YoixBodyGraphObserver extends YoixPointerActive

    implements YoixConstantsGraph

{

    YoixGraphElement  graph;
    YoixBodyThread    thread;

    int manager_type = -1;

    // for layout
    Random  rng = null;
    double  dvalues[] = null;
    int     ivalues[] = null;
    // end for layout

    private YoixObject  args[] = null;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(10);

    static {
	activefields.put(N_GRAPH, new Integer(V_GRAPH));
	activefields.put(N_LAYOUTMANAGER, new Integer(V_LAYOUTMANAGER));
	activefields.put(N_SIZE, new Integer(V_SIZE));
	activefields.put(N_TEXT, new Integer(V_TEXT));
	activefields.put(N_UPDATE, new Integer(V_UPDATE));
	activefields.put(N_WAIT, new Integer(V_WAIT));
	activefields.put(N_WALK, new Integer(V_WALK));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyGraphObserver(YoixObject data) {

	super(data);
	buildObserver(data.getObject(N_GRAPH, null));
	setFixedSize();
    }


    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(GRAPHOBSERVER);
    }

    ///////////////////////////////////
    //
    // YoixBodyGraphObserver Methods
    //
    ///////////////////////////////////

    YoixGraphElement
    getGraph() {

	return(graph);
    }


    final void
    layoutCallback(YoixGraphElement grf, boolean interrupted, YoixObject args[]) {

	// TODO: set display cursor back to previous value

	data.putInt(N_INTERRUPTED, interrupted);

	if (args != null) {
	    if (graph != grf)
		data.put(N_GRAPH, YoixObject.newPointer(grf.getWrapper()));
	    call(args); // ignore return for now - maybe forever
	    if (graph != grf)
		data.put(N_GRAPH, YoixObject.newPointer(graph.getWrapper()));
	}

    }


    protected final YoixObject
    executeField(String name, YoixObject args[]) {

	YoixObject        obj = null;
	YoixGraphElement  graph = this.graph;
	YoixBodyThread    bthrd;

	if (graph != null) {
	    switch (activeField(name, activefields)) {
		case V_LAYOUTMANAGER:
		    obj = builtinLayoutManager(name, args);
		    break;

		case V_TEXT:
		    obj = builtinText(name, args);
		    break;

		case V_UPDATE:
		    obj = builtinUpdate(name, args);
		    break;

		case V_WAIT:
		    obj = builtinWait(name, args);
		    break;

		default:
		    obj = null;
		    break;
	    }
	} else obj = null;		// not quite!!

	return(obj);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_GRAPH:
		    if (thread == null) {
			data.put(N_GRAPH, obj, false);
			buildObserver(obj);
		    } else VM.abort(ACCESSCONFLICT);
		    break;

	    }
	}

	return(obj);
    }


    final void
    update(YoixGraphElement graph, Vector info_packet) {

	Object  info[];
	int     len = info_packet.size();

// 	System.err.println("update for graph: " + graph.getName());
// 	for(int i=0; i<len; i++) {
// 	    info = (Object[])(info_packet.elementAt(i));
// 	    System.err.print("\t"+((YoixGraphElement)info[0]).getName()+"\t"+((Integer)info[1]).intValue());
// 	    for(int j=2; j<info.length; j++) {
// 		System.err.print("\t"+info[j]);
// 	    }
// 	    System.err.println();
// 	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildObserver(YoixObject grf) {

	YoixGraphElement  new_graph = null;
	YoixGraphElement  graph = this.graph;

	if (grf != null && grf.notNull()) {
	    if (grf.isGraph()) {
		if (((YoixBodyElement)grf.body()).hasElement())
		    new_graph = ((YoixBodyElement)grf.body()).getElement();
	    } else VM.abort(BADVALUE, N_GRAPH);
	}


	if (new_graph == graph)
	    return;

	// should we synchronize the whole method?
	synchronized(this) {
	    if(thread != null) {
		// interrupt current layout
		thread.setField(N_ALIVE, YoixObject.newInt(false));
	    }
	}

	if (graph != null) {
	    graph.notifier.deleteObserver(this);

	    // TODO: queue removal to display observers

	    graph = null;
	}

	if (new_graph != null) {
	    graph = new_graph;
	    graph.notifier.addObserver(this);

	    // TODO: queue addition to display observers
	}

	this.graph = graph;

	// TODO: notify display observers
    }


    private synchronized YoixObject
    builtinText(String name, YoixObject arg[]) {

	YoixObject  result = null;
	String      indent = null;
	int         format;
	int         mode;
	int         depth;

	if (thread == null) {
	    if (arg.length <= 4) {
		format = XML_TEXTUAL;
		mode = -1;
		depth = -1;
		indent = null;
		if (arg.length > 0) {
		    if (arg[0].isInteger()) {
			if (arg[0].intValue() == DOT_TEXTUAL)
			    format = DOT_TEXTUAL;
			else format = XML_TEXTUAL;
			if (arg.length > 1) {
			    if (arg[1].isInteger())
				mode = arg[1].intValue();
			    else VM.badArgument(name, 1);
			    if (arg.length > 2) {
				if (arg[2].isInteger())
				    depth = arg[2].intValue();
				else VM.badArgument(name, 2);
				if (arg.length > 3) {
				    if (arg[3].notNull() && arg[3].isString())
					indent = arg[3].stringValue();
				    else VM.badArgument(name, 3);
				}
			    }
			}
		    } else VM.badArgument(name, 0);
		}
		if (graph != null)
		    result = YoixObject.newString(graph.toString(format, mode, depth, indent, this));
	    } else VM.badCall(name);

	} else VM.abort(ACCESSCONFLICT);

	return(result == null ? YoixObject.newString() : result);
    }


    private YoixObject
    builtinUpdate(String name, YoixObject arg[]) {

	YoixBodyThread  bthrd;
	YoixObject      args[] = null;
	YoixObject      obj;
	boolean         waitForIt = false;

	synchronized(this) {
	    if (thread == null) {
		if (arg.length > 0 && arg[0].notNull()) {
		    if (arg[0].isInteger()) {
			waitForIt = arg[0].booleanValue();
			if (arg.length > 1 && arg[1].notNull()) {
			    if (arg[1].isCallable() && arg[1].callable(arg.length - 2)) {
				args = new YoixObject[arg.length-1];
				for (int i=1; i<arg.length; i++) {
				    args[i-1] = arg[i];
				}
			    } else VM.badArgument(name, 1);
			}
		    } else if (arg[0].isCallable() && arg[0].callable(arg.length - 1)) {
			args = arg;
		    } else VM.badArgument(name, 0);
		}

		if ((thread = graph.prepLayout(this)) != null)
		    data.putInt(N_INTERRUPTED, false);

	    } else VM.abort(ACCESSCONFLICT, name);

	    bthrd = thread;
	}

	if (bthrd != null) {
	    // TODO: set display cursor to wait cursor

	    graph.runLayout(bthrd, waitForIt, args);
	    obj = YoixObject.newPointer(bthrd);
	} else obj = YoixObject.newThread();

	return(obj);
    }


    private YoixObject
    builtinWait(String name, YoixObject arg[]) {

	YoixBodyThread  bthrd;
	YoixObject      obj;

	synchronized(this) {
	    if (thread != null) {
		if (arg.length != 0)
		    VM.badCall(name);
	    }

	    bthrd = thread;
	}

	if (bthrd != null) {
	    graph.waitForLayout(bthrd);
	    obj = YoixObject.newPointer(bthrd);
	} else obj = YoixObject.newThread();

	return(obj);
    }


    private synchronized YoixObject
    builtinLayoutManager(String name, YoixObject arg[]) {

	int  manager_type;

	if (thread == null) {
	    if (arg.length > 0) {
		if (arg[0].isInteger()) {
		    manager_type = arg[0].intValue();
		    switch(manager_type) {
			case LAYOUT_FORCE_BOUND:
			    this.manager_type = manager_type;
			    // args are:
			    // type, xoff, yoff, wid, ht[, iters[, temp0]]
			    if (dvalues == null) {
				rng = new Random();
				// xoff, yoff, wid, ht, wid^2, ht^2, T0
				dvalues = new double[7];
				// numIters
				ivalues = new int[1];
			    }

			    // for debug: rng = new Random(110354);
			    ivalues[0] = 50;

			    if (arg.length >= 5 && arg.length <= 7) {
				for (int i=1; i<5; i++) {
				    if (arg[i].isNumber()) {
					dvalues[i-1] = arg[i].doubleValue();
				    } else VM.badArgumentValue(name, i);
				}
			    } else VM.badCall(name);

			    dvalues[4] = dvalues[2] * dvalues[2];
			    dvalues[5] = dvalues[3] * dvalues[3];
			    dvalues[6] = dvalues[2] / 10.0;

			    if (arg.length > 5) {
				if (arg[5].isInteger()) {
				    ivalues[0] = arg[5].intValue();
				} else VM.badArgumentValue(name, 5);
				if (arg.length > 6) {
				    if (arg[6].isNumber()) {
					dvalues[5] = arg[6].doubleValue();
				    } else VM.badArgumentValue(name, 6);
				}
			    }

			    graph.addObserverData(this, manager_type);
			    break;

			case LAYOUT_FORCE:
			default:
			    VM.badArgumentValue(name, 0);
			    break;
		    }
		} else VM.badArgument(name, 1);
	    } else VM.badCall(name);
	} else VM.abort(ACCESSCONFLICT);

	return(YoixObject.newNull());
    }
}

