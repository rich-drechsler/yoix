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
class YoixPointerActive extends YoixPointer

    implements YoixAPI,
	       YoixAPIProtected,
	       YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCloneable

{

    //
    // Class that's extended by objects that claim to be pointers with
    // active fields that can trigger special actions on read, write,
    // or execute.
    //
    // The constructors set length to data.length(), which is exactly
    // right if data doesn't grow, but if it does the implementation of
    // inRange() in YoixPointer should work. A safer approach would be
    // to set length to -1, which tells inRange() to use the exact size
    // of data in every test. Unfortunately, YoixBodyStream may think
    // it knows all this, so be careful making changes.
    //

    YoixObject  data;		// never reset this to null!!!

    //
    // Subclasses in other packages will somtimes want need to provide
    // their own implementation of getContext(), which means they may
    // need to check context.
    //

    protected YoixObject  context;

    //
    // Decided to extract major an minor from data, so changes to the
    // values stored in data (once the constructor is finished) won't
    // affect performace. Means subclasses (and anyone else) should
    // use getMajor() and getMinor().
    //

    private int  major = YOIX_EOF;
    private int  minor = YOIX_EOF;

    //
    // In past releases no class that extended this one could be cloned
    // and the clone() method defined here called Die() if it was ever
    // called. In most cases there are low level reasons why cloning is
    // not allowed (e.g., we can't figure out what it means to clone an
    // object that's managing a Java Component like a Button). However,
    // there's now a class (e.g., YoixBodyMatrix) that extends this one
    // that needs to support cloning, so what happens in clone() is now
    // controlled by the cloneable boolean, which should rarely be set
    // to true.
    //

    private boolean  cloneable = false;

    //
    // Constants occasionally used in subclasses to set permissions on
    // some of the fields in the data dictionary, usually right before
    // the constructor is finished.
    //

    protected static final Integer  $LR__ = new Integer(LR__);
    protected static final Integer  $L_W_ = new Integer(L_W_);
    protected static final Integer  $L__X = new Integer(L__X);
    protected static final Integer  $LRW_ = new Integer(LRW_);
    protected static final Integer  $LR_X = new Integer(LR_X);
    protected static final Integer  $L___ = new Integer(L___);

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    protected
    YoixPointerActive(YoixObject data) {

	this(data, false);
    }


    protected
    YoixPointerActive(YoixObject data, boolean cloneable) {

	this.data = data;
	this.length = data.length();	// means data can grow but not shrink!!
	this.cloneable = cloneable;

	//
	// Assumes that N_MAJOR and N_MINOR, if they exist, are numbers,
	// which is sufficent for now. Means user modules that get here
	// must also obey the rule otherwise the constructor will fail!!
	//
	major = data.getInt(N_MAJOR, YOIX_EOF);
	minor = data.getInt(N_MINOR, YOIX_EOF);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public synchronized Object
    clone() {

	YoixPointerActive  obj;

	if (cloneable) {
	    try {
		obj = (YoixPointerActive)super.clone();
		obj.data = obj.data.duplicate();
		obj.context = null;
	    }
	    catch(CloneNotSupportedException e) {
		VM.die(INTERNALERROR);
		obj = null;
	    }
	} else {
	    VM.abort(INVALIDCLONE);
	    obj = null;
	}

	return(obj);
    }


    public Object
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

	return(dump(0, "", null));
    }


    public final int
    length() {

	return(data.length());
    }


    public final String
    toString() {

	return(dump());
    }

    ///////////////////////////////////
    //
    // YoixInterfacePointer Methods
    //
    ///////////////////////////////////

    public final YoixObject
    cast(YoixObject obj, int index, boolean clone) {

	if (canWrite())
	    obj = data.cast(obj, index, clone);
	else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final YoixObject
    cast(YoixObject obj, String name, boolean clone) {

	int  index;

	if ((index = hash(name)) != -1)
	    obj = cast(obj, index, clone);
	else VM.abort(DICTFULL, name);

	return(obj);
    }


    public final boolean
    compound() {

	return(true);
    }


    public final void
    declare(int index, YoixObject obj, int mode) {

	if (canWrite())
	    data.declare(index, obj, mode);
	else VM.abort(INVALIDACCESS);
    }


    public final void
    declare(String name, YoixObject obj, int mode) {

	int  index;

	if ((index = reserve(name)) != -1)
	    declare(index, obj, mode);
	else VM.abort(DICTFULL, name);
    }


    public final boolean
    defined(int index) {

	return(canRead() && data.defined(index));
    }


    public final boolean
    defined(String name) {

	return(defined(hash(name)));
    }


    public final String
    dump(int index, String indent, String typename) {

	String  str;
	String  name;
	int     sorted[];
	int     level;
	int     limit;
	int     size;
	int     n;
	int     m;

	sorted = ((YoixBodyDictionary)data.body()).sortedOrder();
	size = sorted.length;
	level = indent.length()/4;
	limit = VM.getInt(N_DUMPDEPTH);
	indent += "   ";
	str = (typename == null) ? getTypename() : typename;
	str += "[" + size + ":" + index + "]";

	if (canRead()) {
	   if (level < limit) {
		str += NL;
		for (n = 0; n < size; n++) {
		    m = sorted[n];
		    str += indent + ((m == index) ? ">" : " ");
		    if ((name = data.name(m)) != null) {
			str += name + "=";
			if (data.readable(m)) {
			    if (sideEffects(name))
				str += data.get(m, false).dump(indent + " ");
			    else str += get(m, false).dump(indent + " ");
			} else if (data.executable(m))
			    str += "--executeonly--" + NL;
			else str += "--unreadable--" + NL;
		    } else str += "--uninitialized--" + NL;
		}
	    } else str += NL;
	} else str += ":--unreadable--" + NL;

	return(str);
    }


    public final boolean
    executable(int index) {

	return(canExecute() && data.executable(index));
    }


    public final boolean
    executable(String name) {

	return(executable(hash(name)));
    }


    public final YoixObject
    execute(int index, YoixObject argv[], YoixObject context) {

	YoixObject  obj;

	if (executable(index)) {
	    if ((obj = executeField(data.name(index), argv)) == null)
		obj = data.execute(index, argv, context);
	} else obj = data.execute(index, argv, context);	// only for the error!!

	return(obj);
    }


    public final YoixObject
    execute(String name, YoixObject argv[], YoixObject context) {

	YoixObject  obj = null;
	int         index;

	if ((index = hash(name)) != -1)
	    obj = execute(index, argv, context);
	else VM.abort(UNDEFINED, name);

	return(obj);
    }


    public final YoixObject
    get(int index, boolean clone) {

	YoixObject  obj;

	if (readable(index)) {
	    if ((obj = getField(data.name(index), null)) == null)
		obj = data.get(index, clone);
	} else obj = data.get(index, clone);	// only for the error!!

	return(obj);
    }


    public final YoixObject
    get(String name, boolean clone) {

	YoixObject  obj = null;
	int         index;

	if ((index = hash(name)) != -1)
	    obj = get(index, clone);
	else VM.abort(UNDEFINED, name);

	return(obj);
    }


    public final int
    hash(String name) {

	return(data.hash(name));
    }


    public final String
    name(int index) {

	return(data.name(index));
    }


    public final YoixObject
    put(int index, YoixObject obj, boolean clone) {

	YoixObject  tmp;

	//
	// A change on 8/22/01 delays the put() until after setField()
	// returns, mostly so data isn't changed if setField() detects
	// an error. Implementation obviously introduced some overhead
	// that we probably can eliminate if necessary. Performance in
	// all the tests we've run so has not decreased noticeably.
	//

	if (canWrite()) {
	    obj = cast(obj, index, clone);
	    if ((tmp = setField(data.name(index), obj)) != null)
		obj = tmp;
	    data.put(index, obj, false);
	} else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final YoixObject
    put(String name, YoixObject obj, boolean clone) {

	int  index;

	if ((index = reserve(name)) != -1)
	    obj = put(index, obj, clone);
	else VM.abort(DICTFULL, name);

	return(obj);
    }


    public final boolean
    readable(int index) {

	return(canRead() && data.readable(index));
    }


    public final boolean
    readable(String name) {

	return(readable(hash(name)));
    }


    public final int
    reserve(String name) {

	int  index;

	//
	// Old versions checked to see if hash() was -1, but that seemed
	// wrong. Changed the test on 12/17/05, but this code is rarely
	// used (e.g., a named block with growable data or uninitialized
	// slots) so it's unlikely any applications will be affected!!
	//

	if ((index = hash(name)) != -1 && canWrite())
	    index = data.reserve(name);
	return(index);
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final YoixObject
    call(YoixObject args[]) {

	YoixObject  obj = null;
	YoixObject  function;
	YoixObject  argv[];

	if (args.length > 0) {
	    function = args[0];
	    argv = new YoixObject[args.length - 1];
	    System.arraycopy(args, 1, argv, 0, argv.length);
	    obj = call(function, argv, getContext());
	}

	return(obj);
    }


    public final YoixObject
    call(String name) {

	return(call(data.getObject(name), new YoixObject[0], getContext()));
    }


    public final YoixObject
    call(String name, YoixObject arg) {

	return(call(data.getObject(name), new YoixObject[] {arg}, getContext()));
    }


    public final YoixObject
    call(String name, YoixObject argv[]) {

	return(call(data.getObject(name), argv, getContext()));
    }


    public final YoixObject
    call(YoixObject function, YoixObject arg) {

	return(call(function, new YoixObject[] {arg}, getContext()));
    }


    public final YoixObject
    call(YoixObject function, YoixObject argv[]) {

	return(call(function, argv, getContext()));
    }


    public final Object
    getBody(String name) {

	return(YoixObject.body(data.getObject(name), this));
    }


    public final Object
    getBody(YoixObject obj) {

	return(YoixObject.body(obj, this));
    }


    public YoixObject
    getContext() {

	if (context == null)
	    context = YoixObject.newPointer(this);
	return(context);
    }


    public final YoixObject
    getData() {

	return(data);
    }


    public final int
    getMajor() {

	return(major);
    }


    public final Object
    getManagedObject(String name) {

	return(YoixObject.getManagedObject(data.getObject(name), this));
    }


    public final Object
    getManagedObject(YoixObject obj) {

	return(YoixObject.getManagedObject(obj, this));
    }


    public final int
    getMinor() {

	return(minor);
    }


    public final String
    getTypename() {

	return(data.getTypename());
    }


    public final boolean
    isCloneable() {

	return(cloneable);
    }

    ///////////////////////////////////
    //
    // YoixAPIProtected Methods
    //
    ///////////////////////////////////

    protected final int
    activeField(String name, Map table) {

	Object  id;

	return((id = table.get(name)) != null ? ((Integer)id).intValue() : -1);
    }


    protected static void
    atExit() {

    }

    protected YoixObject
    call(YoixObject function, YoixObject argv[], YoixObject context) {

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


    protected YoixObject
    executeField(String name, YoixObject argv[]) {

	return(null);
    }


    protected void
    finalize() {

	context = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    protected YoixObject
    getField(String name, YoixObject obj) {

	return(obj);
    }


    protected Object
    getManagedObject() {

	return(VM.abort(UNIMPLEMENTED));
    }


    protected void
    handleRun(Object args[]) {

    }


    protected void
    initializer() {

	YoixObject  obj;

	if ((obj = data.getObject(N_INITIALIZER)) != null) {
	    if (obj.callable(0))
		call(obj, new YoixObject[0], getContext());
	}
    }


    protected final void
    resetPermissions(Object permissions[], int columns) {

	YoixObject  obj;
	String      name;
	Integer     perm;
	int         n;

	if (columns == 5) {
	    VM.pushAccess(LR__);
	    for (n = 0; n < permissions.length; n += columns) {
		if ((name = (String)permissions[n]) != null) {
		    if (data.defined(name)) {
			obj = data.get(name, false);
			if ((perm = (Integer)permissions[n+3]) != null)
			    obj.setAccess(perm.intValue());
			if ((perm = (Integer)permissions[n+4]) != null)
			    obj.setAccessBody(perm.intValue());
		    }
		}
	    }
	    VM.popAccess();
	} else VM.die(INTERNALERROR);
    }


    protected YoixObject
    setField(String name) {

	return(setField(name, data.getObject(name)));
    }


    protected YoixObject
    setField(String name, YoixObject obj) {

	return(obj);
    }


    protected final void
    setFixedSize() {

	data.setGrowable(false);
    }


    protected final void
    setPermissions(Object permissions[]) {

	setPermissions(permissions, 3);
    }


    protected final void
    setPermissions(Object permissions[], int columns) {

	YoixObject  obj;
	String      name;
	Integer     perm;
	int         n;

	if (columns == 3 || columns == 5) {
	    VM.pushAccess(LR__);
	    for (n = 0; n < permissions.length; n += columns) {
		if ((name = (String)permissions[n]) != null) {
		    if (data.defined(name)) {
			obj = data.get(name, false);
			if ((perm = (Integer)permissions[n+1]) != null) {
			    if (columns == 5)
				permissions[n+3] = new Integer(obj.getAccess());
			    obj.setAccess(perm.intValue());
			} else if (columns == 5)
			    permissions[n+3] = null;
			if ((perm = (Integer)permissions[n+2]) != null) {
			    if (columns == 5)
				permissions[n+4] = new Integer(obj.getAccessBody());
			    obj.setAccessBody(perm.intValue());
			} else if (columns == 5)
			    permissions[n+4] = null;
		    }
		}
	    }
	    VM.popAccess();
	} else VM.die(INTERNALERROR);
    }


    protected final void
    setToConstant(String name) {

	YoixObject  obj;

	if ((obj = data.get(name)) != null) {
	    if (obj.canWrite() || obj.canUnlock())
		obj.setAccess(LR__);
	}
    }


    protected boolean
    sideEffects(String name) {

	return(false);
    }

    ///////////////////////////////////
    //
    // YoixPointerActive Methods
    //
    ///////////////////////////////////

    synchronized void
    changeData(YoixObject data) {

	//
	// Don't use this unless you really know what you're doing.
	// Right now reopening streams is the only time its used,
	// and we suspect there may never be another use.
	//

	VM.pushAccess(LRW_);
	this.data = data;
	this.length = data.length();
	major = data.getInt(N_MAJOR, YOIX_EOF);
	minor = data.getInt(N_MINOR, YOIX_EOF);
	VM.popAccess();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////
}

