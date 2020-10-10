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
import java.util.*;

final
class YoixBodyProcess extends YoixPointerActive

    implements YoixInterfaceKillable

{

    private Process     process;
    private YoixObject  parent;

    //
    // Remember processes we started, so we can clean up on a graceful
    // exit. May be unnecessary, but I seem to remember having problems
    // in a older program, but probably just on PCs.
    //

    private static Hashtable  activeprocesses = new Hashtable();

    //
    // An array used to set permissions on some of the fields that users
    // should only be able to set once.
    //

    private Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_COMMAND,          null,        $LR__,
	N_DIRECTORY,        null,        $LR__,
	N_ENVP,             null,        $LR__,
    };

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(8);

    static {
	activefields.put(N_ALIVE, new Integer(V_ALIVE));
	activefields.put(N_COMMAND, new Integer(V_COMMAND));
	activefields.put(N_DIRECTORY, new Integer(V_DIRECTORY));
	activefields.put(N_ENVP, new Integer(V_ENVP));
	activefields.put(N_EXITVALUE, new Integer(V_EXITVALUE));
	activefields.put(N_PARENT, new Integer(V_PARENT));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyProcess(YoixObject data) {

	super(data);
	buildProcess();
	setFixedSize();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(PROCESS);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceKillable Methods
    //
    ///////////////////////////////////

    public final void
    kill() {

	stopProcess();
    }

    ///////////////////////////////////
    //
    // YoixBodyProcess Methods
    //
    ///////////////////////////////////

    final boolean
    active() {

	return(process != null);
    }


    protected static void
    atExit() {

	YoixBodyProcess  proc;
	Enumeration      keys;
	Hashtable        active;

	//
	// May be unnecessary, but I seem to remember having problems
	// in an older program (probably just on PCs), so check before
	// removing. Decided to destroy processes here instead of using
	// stopProcess(), so we shouldn't have to worry about deadlock.
	//

	active = (Hashtable)activeprocesses.clone();

	for (keys = active.keys(); keys.hasMoreElements(); ) {
	    try {
		proc = (YoixBodyProcess)keys.nextElement();
		if (proc.data.getBoolean(N_PERSISTENT) == false)
		    proc.process.destroy();
	    }
	    catch(RuntimeException e) {}
	}
    }


    protected final void
    finalize() {

	stopProcess();
	super.finalize();
    }


    protected final YoixObject
    getField(String name, YoixObject obj) {

	switch (activeField(name, activefields)) {
	    case V_ALIVE:
		obj = getAlive(obj);
		break;

	    case V_EXITVALUE:
		obj = getExitvalue(obj);
		break;
	}

	return(obj);
    }


    protected final Object
    getManagedObject() {

	return(process);
    }


    protected final YoixObject
    setField(String name, YoixObject obj) {

	if (obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ALIVE:
		    setAlive(obj);
		    break;

		case V_COMMAND:
		    setCommand(obj);
		    break;

		case V_DIRECTORY:
		    setDirectory(obj);
		    break;

		case V_ENVP:
		    setEnvp(obj);
		    break;

		case V_PARENT:
		    setParent(obj);
		    break;
	    }
	}

	return(obj);
    }


    final void
    stop() {

	//
	// So a parent window can stop a child process without having
	// to remember the field name - don't use it for anything else!!
	//

	stopProcess();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    attachStream(Object stream, String name) {

	YoixObject  dict;

	dict = VM.getTypeTemplate(T_FILE);
	dict.putString(N_NAME, "--process:" + name + "--");
	dict.putString(N_FULLNAME, "");
	dict.putString(N_MODE, name.equals(N_INPUT) ? "w" : "r");
	dict.putInt(N_OPEN, true);

	if (stream instanceof InputStream)
	    data.put(name, YoixObject.newStream(dict, (InputStream)stream));
	else data.put(name, YoixObject.newStream(dict, (OutputStream)stream));
    }


    private void
    buildProcess() {

	process = null;
	setField(N_COMMAND);
	setField(N_DIRECTORY);
	setField(N_ENVP);
	setField(N_ALIVE);
	setField(N_PARENT);
    }


    private synchronized YoixObject
    getAlive(YoixObject obj) {

	boolean  alive = false;

	if (process != null) {
	    try {
		process.exitValue();
	    }
	    catch(IllegalThreadStateException e) {
		alive = true;
	    }
	}

	return(YoixObject.newInt(alive));
    }


    private synchronized YoixObject
    getExitvalue(YoixObject obj) {

	int  value;

	if (process != null) {
	    try {
		if ((value = process.exitValue()) >= 256)
		    value >>= 8;
		obj = YoixObject.newInt(value);
	    }
	    catch(IllegalThreadStateException e) {}
	}

	return(obj);
    }


    private synchronized void
    setAlive(YoixObject obj) {

	if (obj.booleanValue()) {
	    if (process != null) {
		try {
		    process.exitValue();
		    stopProcess();
		}
		catch(IllegalThreadStateException e) {}
	    }
	    startProcess();
	} else stopProcess();
    }


    private synchronized void
    setCommand(YoixObject obj) {

	stopProcess();

	if (obj.notNull()) {
	    data.put(N_COMMAND, YoixObject.newNull());
	    if (obj.isString() || obj.isArray()) {
		//
		// Eventually run more checks, particularly for arrays.
		// Might also be a good place for security checks.
		//
		data.put(N_COMMAND, obj.duplicate());
	    } else VM.abort(TYPECHECK, N_COMMAND);
	}
    }


    private synchronized void
    setDirectory(YoixObject obj) {

	File  dir;

	stopProcess();

	if (obj.notNull()) {
	    data.put(N_DIRECTORY, YoixObject.newNull());
	    if (obj.notNull() && obj.length() > 0) {
		dir = new File(obj.stringValue());
		if (dir.isDirectory())
		    data.put(N_DIRECTORY, obj.duplicate());
		else VM.abort(BADVALUE, N_DIRECTORY);
	    }
	}
    }


    private synchronized void
    setEnvp(YoixObject obj) {

	stopProcess();

	if (obj.notNull()) {
	    //
	    // Probably a good place for security checks if we decide
	    // they're needed - later??
	    //
	}
    }


    private void
    setParent(YoixObject obj) {

	YoixObject  owner;

	if (obj != null) {
	    data.put(N_PARENT, YoixObject.newNull());
	    owner = this.parent;		// snapshot - just to be safe
	    if (owner != null) {
		((YoixBodyComponent)owner.body()).childrenRemove(this);
		parent = null;
	    }
	    if (obj.isWindow() && obj.notNull()) {
		parent = obj;
		((YoixBodyComponent)obj.body()).childrenAdd(this);
	    }
	}
    }


    private synchronized void
    startProcess() {

	YoixObject  cmd;
	YoixObject  env;
	YoixObject  dir;
	String      argv[];
	String      envp[];
	File        fdir;
	int         n;

	if (process == null) {
	    cmd = data.getObject(N_COMMAND);
	    if (cmd.notNull() && cmd.length() > 0) {
		VM.pushAccess(LRW_);
		try {
		    dir = data.getObject(N_DIRECTORY);
		    if (dir.notNull() && dir.length() > 0) {
			// assume existence of directory was checked when assigned
			fdir =  new File(dir.stringValue());
		    } else fdir = null;

		    env = data.getObject(N_ENVP);
		    if (env.notNull()) {
			envp =  new String[env.length()];
			for (n = 0; n < envp.length; n++)
			    envp[n] = env.getString(n);
		    } else envp = null;

		    if (cmd.isArray()) {
			argv = new String[cmd.length()];
			for (n = 0; n < argv.length; n++)
			    argv[n] = cmd.getString(n);
			process = RUNTIME.exec(argv, envp, fdir);
		    } else process = RUNTIME.exec(cmd.stringValue(), envp, fdir);

		    activeprocesses.put(this, this);
		    attachStream(process.getOutputStream(), N_INPUT);
		    attachStream(process.getInputStream(), N_OUTPUT);
		    attachStream(process.getErrorStream(), N_ERROR);
		    setPermissions(permissions);
		}
		catch(IOException e) {
		    process = null;
		    VM.caughtException(e, true);
		}

		VM.popAccess();
	    }
	}
    }


    private synchronized void
    stopProcess() {

	YoixObject input;
	YoixObject output;
	YoixObject error;

	//
	// Changed the order that things are done here slightly and also
	// now make sure files stored in data dictionaries are set to NULL
	// before the process is officially stopped. Old version stopped
	// the process, closed all three files, and finally replaced their
	// data entries with NULL. It often worked, but if Yoix code read
	// stdin first and then checked stderr it could end up trying to
	// read from the error stream before we were completely done here.
	// This approach seems safer and behaves well in the test that had
	// trouble with the old order. Change was made on 9/15/07.
	//
	// NOTE - builtins, like readStream() currently complain if their
	// stream argument is NULL, but it might would be worthwhile if
	// readStream() and others simply returned NULL when handed a NULL
	// argument - later.
	// 

	if (process != null) {
	    VM.pushAccess(LRW_);
	    input =  data.getObject(N_INPUT);
	    output = data.getObject(N_OUTPUT);
	    error = data.getObject(N_ERROR);
	    data.put(N_INPUT, YoixObject.newStream());
	    data.put(N_OUTPUT, YoixObject.newStream());
	    data.put(N_ERROR, YoixObject.newStream());
	    process.destroy();
	    process = null;
	    input.close();
	    error.close();
	    output.close();
	    activeprocesses.remove(this);
	    VM.popAccess();
	}
    }
}

