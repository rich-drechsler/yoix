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
import java.text.*;
import java.util.*;
import java.util.zip.*;
import att.research.yoix.jvma.*;

final
class YoixBodyCompiler extends YoixPointerActive

    implements YoixCompilerConstants,
	       YoixInterfaceKillable,
	       Runnable

{

    //
    // We use a separate thread when the user wants the compiler to run in
    // the background (via the compileFunctionLater() builtin).
    //

    private Thread  thread;
    private Vector  queue;

    //
    // Local storage from several fields that are defined in data.
    //

    private boolean  addtags;
    private int      debug;
    private int      localcopymodel;

    //
    // A counter for generating thread names.
    //

    private static int  threadnumber = 1;

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(15);

    static {
	activefields.put(N_ADDTAGS, new Integer(V_ADDTAGS));
	activefields.put(N_ALIVE, new Integer(V_ALIVE));
	activefields.put(N_COMPILEFUNCTION, new Integer(V_COMPILEFUNCTION));
	activefields.put(N_COMPILEFUNCTIONLATER, new Integer(V_COMPILEFUNCTIONLATER));
	activefields.put(N_COMPILESCRIPT, new Integer(V_COMPILESCRIPT));
	activefields.put(N_DEBUG, new Integer(V_DEBUG));
	activefields.put(N_LOCALCOPYMODEL, new Integer(V_LOCALCOPYMODEL));
	activefields.put(N_PRIORITY, new Integer(V_PRIORITY));
	activefields.put(N_TIMESTAMP, new Integer(V_TIMESTAMP));
	activefields.put(N_VERSION, new Integer(V_VERSION));
    }

    private static final String  YOIXCOMPILER_THREADGROUP = "YoixCompiler";
    private static final String  YOIXCOMPILER_THREADPREFIX = "YoixCompilerThread-";

    //
    // Take the static out of the next declaration and you may be able
    // notice a small (56 byte) memory leak in some implementations of
    // java.
    //

    private static ThreadGroup  group = new ThreadGroup(YOIXCOMPILER_THREADGROUP);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyCompiler(YoixObject data) {

	super(data, true);	// tells super we're cloneable!!
	buildCompiler();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public final synchronized Object
    clone() {

	YoixBodyCompiler  obj;

	obj = (YoixBodyCompiler)super.clone();
	obj.buildCompiler();

	return(obj);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(COMPILER);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceKillable Methods
    //
    ///////////////////////////////////

    public final void
    kill() {

	stopThread();
    }

    ///////////////////////////////////
    //
    // Runnable Methods
    //
    ///////////////////////////////////

    public final void
    run() {

	YoixBodyFunction  function;
	YoixObject        arg;

	try {
	    while (data.getBoolean(N_ALIVE, true)) {
		arg = null;
		synchronized(this) {
		    if (queue.size() > 0) {
			arg = (YoixObject)queue.elementAt(0);
			queue.removeElementAt(0);
		    } else break;
		}
		if (arg != null) {
		    Thread.interrupted();	// just to clear interrupted state!!!
		    //
		    // Functions only right now, but we eventually may accept
		    // other kinds of arguments.
		    //
		    if (arg.isFunction())
			compileFunction(arg);
		}
	    }
	}
	finally {
	    stopThread();
	}
    }

    ///////////////////////////////////
    //
    // YoixBodyCompiler Methods
    //
    ///////////////////////////////////

    protected final YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj;

	switch (activeField(name, activefields)) {
	    case V_COMPILEFUNCTION:
		obj = builtinCompileFunction(name, argv);
		break;

	    case V_COMPILEFUNCTIONLATER:
		obj = builtinCompileFunctionLater(name, argv);
		break;

	    case V_COMPILESCRIPT:
		obj = builtinCompileScript(name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected final void
    finalize() {

	thread = null;
	queue = null;
	super.finalize();
    }


    final boolean
    getAddTags() {

	return(addtags);
    }


    final synchronized boolean
    getAlive() {

	return(thread != null ? thread.isAlive() : false);
    }


    final int
    getDebug() {

	return(debug);
    }


    protected final synchronized YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_ADDTAGS:
		obj = YoixObject.newInt(addtags);
		break;

	    case V_ALIVE:
		obj = YoixObject.newInt(getAlive());
		break;

	    case V_DEBUG:
		obj = YoixObject.newInt(debug);
		break;

	    case V_LOCALCOPYMODEL:
		obj = YoixObject.newInt(localcopymodel);
		break;

	    case V_PRIORITY:
		obj = YoixObject.newInt(getPriority());
		break;

	    case V_TIMESTAMP:
		obj = YoixObject.newString(COMPILER_TIMESTAMP);
		break;

	    case V_VERSION:
		obj = YoixObject.newString(COMPILER_VERSION);
		break;
	}

	return(obj);
    }


    final int
    getLocalCopyModel() {

	return(localcopymodel);
    }


    protected final Object
    getManagedObject() {

	return(null);
    }


    final synchronized int
    getPriority() {

	int  priority;

	if (thread == null)
	    priority = Math.max(Math.min(data.getInt(N_PRIORITY), Thread.MAX_PRIORITY), Thread.MIN_PRIORITY);
	else priority = thread.getPriority();

	return(priority);
    }


    final void
    setAddTags(boolean state) {

	addtags = state;
    }


    private synchronized void
    setAlive(boolean state) {

	if (state == false) {
	    if (thread != null)
		thread.interrupt();
	} else startThread();
    }


    final void
    setDebug(int value) {

	debug = value;
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ADDTAGS:
		    setAddTags(obj.booleanValue());
		    break;

		case V_ALIVE:
		    setAlive(obj.booleanValue());
		    break;

		case V_DEBUG:
		    setDebug(obj.intValue());
		    break;

		case V_LOCALCOPYMODEL:
		    setLocalCopyModel(obj.intValue());
		    break;

		case V_PRIORITY:
		    setPriority(obj.intValue());
		    break;
	    }
	}

	return(obj);
    }


    final void
    setLocalCopyModel(int value) {

	localcopymodel = value;
    }


    synchronized void
    setPriority(int priority) {

	if (thread != null) {
	    priority = Math.max(Math.min(priority, Thread.MAX_PRIORITY), Thread.MIN_PRIORITY);
	    if (thread.getPriority() != priority)
		thread.setPriority(priority);
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildCompiler() {

	queue = new Vector();
	thread = null;
	setField(N_ADDTAGS);
	setField(N_DEBUG);
	setField(N_LOCALCOPYMODEL);
    }


    private synchronized YoixObject
    builtinCompileFunction(String name, YoixObject arg[]) {

	YoixBodyFunction  function;
	int               count = 0;
	int               n;

	if (arg.length > 0) {
	    for (n = 0; n < arg.length; n++) {
		if (arg[n].notFunction())
		    VM.badArgument(name, n);
	    }
	    for (n = 0; n < arg.length; n++) {
		if (compileFunction(arg[n]))
		    count++;
	    }
	}

	return(YoixObject.newInt(count));
    }


    private synchronized YoixObject
    builtinCompileFunctionLater(String name, YoixObject arg[]) {

	YoixObject  obj = null;
	int         n;

	//
	// Starting the thread before building the return value should
	// be OK, but only because we own the lock that run() needs to
	// remove objects from the queue.
	//

	if (arg.length > 0) {
	    for (n = 0; n < arg.length; n++) {
		if (arg[n].notNull() && arg[n].notFunction())
		    VM.badArgument(name, n);
	    }
	    for (n = 0; n < arg.length; n++) {
		if (arg[n].isFunction())
		    queue.addElement(arg[n]);
		else queue.removeAllElements();
	    }
	    obj = YoixMake.yoixArray(queue);
	    startThread();
	} else obj = YoixMake.yoixArray(queue);

	return(obj);
    }


    private synchronized YoixObject
    builtinCompileScript(String name, YoixObject arg[]) {

	ByteArrayOutputStream  bstream;
	YoixSimpleDateFormat   sdf;
	ZipOutputStream        zstream;
	YoixCompiler           compiler;
	JVMClassFile           classfile;
	SimpleNode             nodes[];
	YoixObject             objects[];
	boolean                storescript;
	String                 zipped = null;
	String                 script;
	String                 classname;
	String                 source;
	String                 dest;
	byte                   bytes[];
	Date                   date;

	//
	// Compiles a Yoix script and stores the pieces the interpreter will
	// need to execute the script in a zip file that's returned to the
	// caller as a Yoix string.
	//
	// NOTE - error handling can use some work!!
	//

	if (arg.length == 1 || arg.length == 2 || arg.length == 3) {
	    if (arg[0].isString() || arg[0].isNull()) {
		if (arg.length <= 1 || arg[1].isString() || arg[1].isInteger()) {
		    if (arg.length <= 2 || arg[1].isInteger()) {
			source = arg[0].stringValue();
			storescript = false;
			dest = null;
			if (arg.length > 1) {
			    if (arg[1].isString())
				dest = arg[1].stringValue();
			    else storescript = arg[1].booleanValue();
			}
			if (arg.length > 2)
			    storescript = arg[2].booleanValue();
			date = new Date();
			sdf = new YoixSimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			compiler = new YoixCompiler();
			compiler.setAddTags(addtags);
			compiler.setDebug(debug);
			compiler.setLocalCopyModel(localcopymodel);
			if ((script = compiler.compileScript(source)) != null) {
			    try {
				if ((classfile = (new JVMAssembler()).assembleClass(script)) != null) {
				    if ((bytes = classfile.getBytes()) != null) {
					bstream = new ByteArrayOutputStream();
					zstream = new ZipOutputStream(bstream);
					try {
					    zstream.putNextEntry(new ZipEntry(N_CLASS));
					    zstream.write(bytes);
					    zstream.closeEntry();

					    zstream.putNextEntry(new ZipEntry(N_CLASSNAME));
					    zstream.write(classfile.getClassName().getBytes("ISO-8859-1"));
					    zstream.write('\n');
					    zstream.closeEntry();

					    zstream.putNextEntry(new ZipEntry(N_COMPILER));
					    zstream.write(compiler.toString().getBytes("ISO-8859-1"));
					    zstream.write('\n');
					    zstream.closeEntry();

					    zstream.putNextEntry(new ZipEntry(N_NODES));
					    if ((nodes = compiler.getNodeReferences()) != null)
						zstream.write(YoixMisc.objectToBytes(nodes));
					    zstream.closeEntry();

					    zstream.putNextEntry(new ZipEntry(N_OBJECTS));
					    if ((objects = compiler.getObjectReferences()) != null)
						zstream.write(YoixMisc.objectToBytes(objects));
					    zstream.closeEntry();

					    if (storescript) {
						if ((bytes = YoixMisc.readFile(source)) != null) {
						    zstream.putNextEntry(new ZipEntry(N_SCRIPT));
						    zstream.write(YoixMisc.readFile(source));
						    zstream.closeEntry();
						}
					    }

					    zstream.putNextEntry(new ZipEntry(N_SCRIPTNAME));
					    zstream.write(source.getBytes("ISO-8859-1"));
					    zstream.write('\n');
					    zstream.closeEntry();

					    zstream.putNextEntry(new ZipEntry(N_TIMESTAMP));
					    zstream.write(sdf.format(date).getBytes("ISO-8859-1"));
					    zstream.write('\n');
					    zstream.closeEntry();

					    zstream.close();

					    if ((bytes = bstream.toByteArray()) != null) {
						if (dest != null)
						    YoixMisc.writeFile(dest, bytes);
						zipped = new String(bytes, "ISO-8859-1");
					    }
					}
					catch(Exception e) {}
				    }
				}
			    }
			    catch(JVMAssemblerError e) {
				if (compiler.isDebugBitSet(DEBUG_COMPILER_ERRORS)) {
				    System.err.println(e.getMessage());
				    System.err.println(e.getDetails());
				    System.err.println(YoixMisc.javaTrace(e));
				}
				VM.abort(INTERNALERROR, e);
			    }
			}
		    } else VM.badArgument(name, 2);
		} else VM.badArgument(name, 1);
	    } else VM.badArgument(name, 0);
	}

	return(YoixObject.newString(zipped));
    }


    private synchronized boolean
    compileFunction(YoixObject obj) {

	YoixBodyFunction  function;
	YoixCompiler      compiler;
	boolean           result = false;

	if (obj != null) {
	    if (obj.isFunction()) {
		compiler = new YoixCompiler();
		compiler.setAddTags(addtags);
		compiler.setDebug(debug);
		compiler.setLocalCopyModel(localcopymodel);
		function = (YoixBodyFunction)obj.body();
		result = function.compile(compiler);
	    }
	}

	return(result);
    }


    private synchronized String
    getNextThreadName() {

	return(YOIXCOMPILER_THREADPREFIX + threadnumber++);
    }


    private synchronized void
    startThread() {

	if (thread == null) {
	    try {
		thread = new Thread(group, this, getNextThreadName());
		thread.setDaemon(true);
		setField(N_PRIORITY);
		data.putInt(N_ALIVE, true);	// must preceed start()!
		thread.start();
	    }
	    catch(IllegalThreadStateException e) {
		stopThread();
	    }
	} else notifyAll();
    }


    private synchronized void
    stopThread() {

	if (thread != null) {
	    queue.removeAllElements();
	    thread = null;
	    data.putInt(N_ALIVE, false);
	    notifyAll();
	}
    }
}

