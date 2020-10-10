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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.*;
import java.util.zip.*;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.TransferHandler;

public
class YoixObject extends SimpleNode

    implements YoixAPI,
	       YoixAPIProtected,
	       YoixConstants,
	       YoixConstantsGraph,
	       YoixInterfaceBody,
	       YoixInterfaceCallable,
	       YoixInterfaceKillable,
	       YoixInterfacePointer,
	       Transferable,
	       Serializable

{

    //
    // A generic object. Everything in a user's program and anything else
    // (control objects) that ends up on our stacks (there's one for each
    // active thread) must be one of these.
    //
    // Recently made functions implement YoixInterfacePointer, and as part
    // of that work we had to change the pointer testing in this file. The
    // main change was to isPointer() (see the comments in that method for
    // more details), but we also replaced most isPointer() calls in this
    // file by appropriate instanceof calls.
    //
    //
    // NOTE - eventually may need more checking in some public methods. For
    // example, there may be problems (probably just a NullPointerException)
    // if put() tries to store null somewhere. Anyway public methods deserve
    // a bit more attention - maybe later.
    //

    private short  flags;	// currently all could fit in a byte
    private int    offset;

    //
    // YoixObjects are created by pseudo-constructors that clone appropriate
    // templates and fill in missing pieces. YoixObjects built by the pseudo
    // constructors guarantee value[0] implements YoixInterfaceBody. Pseudo
    // constructor names start with "new" (e.g., newString()) and most can
    // be found in this file.
    //

    private static YoixObject  callable_template;
    private static YoixObject  control_template;
    private static YoixObject  jump_template;
    private static YoixObject  number_template;
    private static YoixObject  pointer_template;
    private static YoixObject  tag_template;

    static {
	callable_template = new YoixObject(CALLABLE, RWX, 1);
	callable_template.value[0] = null;

	control_template = new YoixObject(CONTROL, L___, 1);
	control_template.value[0] = null;

	jump_template = new YoixObject(JUMP, L___, 1);
	jump_template.value[0] = null;

	number_template = new YoixObject(NUMBER, RW_, 1);
	number_template.value[0] = null;

	pointer_template = new YoixObject(POINTER, RW_, 1);
	pointer_template.value[0] = null;

	tag_template = new YoixObject(TAG, L___, 1);
	tag_template.value[0] = null;
    }

    //
    // Everything works with one mark, empty, and restore object, so build
    // them now rather than cloning control_template.
    //

    private static YoixObject  empty_object;
    private static YoixObject  mark_object;
    private static YoixObject  restore_object;

    static {
	empty_object = (YoixObject)control_template.clone();
	empty_object.value[0] = new YoixBodyControl(EMPTY);

	mark_object = new YoixObject(CONTROL, LR__, 1);
	mark_object.value[0] = new YoixBodyControl(MARK);

	restore_object = new YoixObject(CONTROL, LR__, 1);
	restore_object.value[0] = new YoixBodyControl(RESTORE);
    }

    //
    // Caching null objects helps the loader and should be useful in other
    // places too. We went back to a Hashtable while we were tracking down
    // some obscure thread related problems that initially seemed to point
    // to nullcache problems, however we believe YoixBodyDictionaryObject
    // the culprit. We decided there should be extensive testing before we
    // change back to a HashMap, so it will remain a Hashtable until we get
    // the time to test on a heavily loaded production system. The Hashtable
    // change was made on 3/17/07.
    //

    private static final Hashtable  nullcache = new Hashtable(200);

    static {
        YoixObject  obj;

        nullcache.put(T_ARRAY, YoixObject.newArray());
        nullcache.put(T_CHECKBOXGROUP, YoixObject.newCheckboxGroup());
        nullcache.put(T_DICT, YoixObject.newDictionary());
        nullcache.put(T_FUNCTION, YoixObject.newFunction());
        nullcache.put(T_IMAGE, YoixObject.newImage());
        nullcache.put(T_LOCALE, YoixObject.newLocale());
        nullcache.put(T_STRING, YoixObject.newString());
        nullcache.put(T_STREAM, YoixObject.newStream());
        nullcache.put(T_TIMEZONE, YoixObject.newTimeZone());
        nullcache.put(T_ZIPENTRY, YoixObject.newZipEntry());

        obj = YoixObject.newFunction();
        obj.setModeBits(ANYMINOR);
        nullcache.put(T_CALLABLE, obj);

        obj = YoixObject.newNullPointer(OBJECT, null);
        obj.setModeBits(ANYMAJOR|ANYMINOR);
        nullcache.put(T_OBJECT, obj);

        obj = YoixObject.newNullPointer(POINTER, null);
        obj.setModeBits(ANYMINOR);
        nullcache.put(T_POINTER, obj);
    }

    //
    // This is an experiment caching a few small integers - mostly to see
    // if we notice an improvement in performance and/or memory use. Should
    // be simple to back out if there are problems, which we don't expect.
    //
    // NOTE - numbers can be cached because the internal value stored by
    // YoixBodyNumber.java never changes.
    //

    private static final int         MIN_CACHEDINT = -100;
    private static final int         MAX_CACHEDINT = 100;
    private static final int         INTCACHESIZE = MAX_CACHEDINT - MIN_CACHEDINT + 1;
    private static final YoixObject  intcache[] = new YoixObject[INTCACHESIZE];

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    protected
    YoixObject(YoixPointerActive body) {

	//
	// This was added so custom modules could extend YoixObject, which
	// means they get access to lots of convenient methods, and create
	// their own objects that extend YoixPointerActive. Classes in the
	// att.research.yoix package currently should not use this method!!
	//

	super(body);
	if (body.getClass().getPackage() == getClass().getPackage())
	    this.flags = (short)RW_;
	else VM.abort(INVALIDACCESS);
    }


    protected
    YoixObject(YoixObject template) {

	//
	// Another constructor added for custom modules, but this one
	// intentionally makes lots of assumptions, and should only work
	// when we're loading a new type template. Don't make this more
	// general if you haven't thoroughly considered the implications
	// (mostly permissions issues).
	//

	super((YoixInterfaceBody)template.value[0], (String)template.value[1]);
	if (YoixVMThread.isLoadingType()) {
	    this.flags = (short)RW_;
	    this.value[0] = ((YoixInterfaceCloneable)value[0]).clone();
	} else VM.abort(INVALIDACCESS);
    }


    protected
    YoixObject(String typename) {

	//
	// A constructor added for custom modules that subclasses can call
	// when they want to create an official null object that's also an
	// instance of their class. Definitely not convinced by this!!!
	// 

	super(null, typename);
	constructCachedNull(typename);
    }


    protected
    YoixObject(int minor, int length) {

	//
	// This was added so custom modules can create versions of objects,
	// like arrays or dictionaries, that behave exactly like standard
	// versions but allow them to do things, like change permissions,
	// that they can't do to standard Yoix versions of those objects.
	// Classes in the att.research.yoix package currently should not
	// use this method!!
	//

	super(POINTER, 1);
	this.flags = (short)RW_;
	this.value[0] = (YoixInterfaceBody)newPointer(minor, length, null).value[0];
    }


    private
    YoixObject(int type, int flags, int length) {

	//
	// This is the only constructor used by this class and it's only
	// for creating the collection of static SimpleNode templates that
	// we clone whenever we build official YoixObjects.
	//

	super(type, length);
	this.flags = (short)flags;
    }

    ///////////////////////////////////
    //
    // Transferable Methods
    //
    ///////////////////////////////////

    public Object
    getTransferData(DataFlavor flavor)

	throws UnsupportedFlavorException

    {

	YoixObject  funct;
	YoixObject  context;
	YoixObject  argv[];
	YoixObject  transferable;
	Object      value = null;

	//
	// The code that handles a function or pointer to a function is
	// a recent addition that can be used when you want to transfer
	// different data that depends on flavor. An easy way to use the
	// new capabilities is create a dictionary with the values that
	// you want to transfer out and a function that can be called to
	// pick the appropriate value. This should be done when the drag
	// gesture is recognized and the address of that function (it's
	// also in the dictionary) should be used as the transferable.
	//
	// Eventually may want to look more closely at flavor and maybe
	// decide what to do based on its mime type??
	//

	if (flavor != null) {
	    if (isCallable() || (isCallablePointer() && isDictionary())) {
		if (isCallablePointer() && isDictionary()) {
		    funct = get();
		    context = this;
		} else {
		    funct = this;
		    context = null;
		}
		argv = null;
		if (funct.callable(1)) {
		    if (YoixDataTransfer.isYoixFlavor(flavor))
			argv = new YoixObject[] {newInt(OBJECT)};
		    else if (YoixDataTransfer.isStringFlavor(flavor))
			argv = new YoixObject[] {newInt(STRING)};
		    else if (YoixDataTransfer.isImageFlavor(flavor))
			argv = new YoixObject[] {newInt(IMAGE)};
		} else if (funct.callable(0))
		    argv = new YoixObject[0];
		if (argv != null)
		    transferable = funct.call(argv, this);
		else transferable = null;
	    } else transferable = this;
	    if (transferable != null) {
		if (YoixDataTransfer.isYoixFlavor(flavor) == false) {
		    if (YoixDataTransfer.isStringFlavor(flavor)) {
			if (transferable.isString())
			    value = transferable.stringValue();
			else value = transferable.toString();
		    } else if (YoixDataTransfer.isImageFlavor(flavor)) {
			if (transferable.isImage())
			    value = ((YoixBodyImage)transferable.body()).copyCurrentImage();
			else throw(new UnsupportedFlavorException(flavor));
		    } else throw(new UnsupportedFlavorException(flavor));
		} else value = transferable;
	    }
	}

	return(value);
    }


    public DataFlavor[]
    getTransferDataFlavors() {

	return(YoixDataTransfer.getExportFlavors());
    }


    public boolean
    isDataFlavorSupported(DataFlavor flavor) {

	return(YoixDataTransfer.isFlavorExported(flavor));
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final String
    dump() {

	return(dump(""));
    }


    public final int
    length() {

	return(((YoixInterfaceBody)value[0]).length());
    }


    public final String
    toString() {

	return(dump());
    }


    public final int
    type() {

	return(super.type());
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCallable Methods
    //
    ///////////////////////////////////

    public final YoixObject
    call(YoixObject argv[], YoixObject context) {

	YoixObject  obj = null;

	if (isCallablePointer()) {
	    obj = get().call(argv, this);
	} else if (canExecute()) {
	    if (isCallable())
		obj = ((YoixInterfaceCallable)value[0]).call(argv, context);
	    else VM.abort(TYPECHECK);
	} else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final boolean
    callable(int argc) {

	return(isCallable() && ((YoixInterfaceCallable)value[0]).callable(argc));
    }


    public final boolean
    callable(YoixObject argv[]) {

	return(isCallable() && ((YoixInterfaceCallable)value[0]).callable(argv));
    }

    ///////////////////////////////////
    //
    // YoixInterfaceKillable Methods
    //
    ///////////////////////////////////

    public final void
    kill() {

	if (value[0] instanceof YoixInterfaceKillable)
	    ((YoixInterfaceKillable)value[0]).kill();
    }

    ///////////////////////////////////
    //
    // YoixInterfacePointer Methods
    //
    ///////////////////////////////////

    public final YoixObject
    cast(YoixObject obj, int index, boolean clone) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).cast(obj, index, clone)
	    : VM.abort(TYPECHECK)
	);
    }


    public final YoixObject
    cast(YoixObject obj, String name, boolean clone) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).cast(obj, name, clone)
	    : VM.abort(TYPECHECK)
	);
    }


    public final boolean
    compound() {

	return(value[0] instanceof YoixInterfacePointer && ((YoixInterfacePointer)value[0]).compound());
    }


    public final void
    declare(int index, YoixObject obj, int mode) {

	if (value[0] instanceof YoixInterfacePointer)
	    ((YoixInterfacePointer)value[0]).declare(index, obj, mode);
	else VM.abort(TYPECHECK);
    }


    public final void
    declare(String name, YoixObject obj, int mode) {

	if (value[0] instanceof YoixInterfacePointer)
	    ((YoixInterfacePointer)value[0]).declare(name, obj, mode);
	else VM.abort(TYPECHECK);
    }


    public final boolean
    defined(int index) {

	return(value[0] instanceof YoixInterfacePointer && ((YoixInterfacePointer)value[0]).defined(index));
    }


    public final boolean
    defined(String name) {

	return(value[0] instanceof YoixInterfacePointer && ((YoixInterfacePointer)value[0]).defined(name));
    }


    public final int
    definedAt(String name) {

	return(value[0] instanceof YoixInterfacePointer ? ((YoixInterfacePointer)value[0]).definedAt(name) : -1);
    }


    public final String
    dump(int index, String indent, String typename) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).dump(index, indent, typename)
	    : ((YoixInterfaceBody)value[0]).dump()
	);
    }


    public final boolean
    executable(int index) {

	return(value[0] instanceof YoixInterfacePointer && ((YoixInterfacePointer)value[0]).executable(index));
    }


    public final boolean
    executable(String name) {

	return(value[0] instanceof YoixInterfacePointer && ((YoixInterfacePointer)value[0]).executable(name));
    }


    public final YoixObject
    execute(int index, YoixObject argv[], YoixObject context) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).execute(index, argv, context)
	    : VM.abort(TYPECHECK)
	);
    }


    public final YoixObject
    execute(String name, YoixObject argv[], YoixObject context) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).execute(name, argv, context)
	    : VM.abort(TYPECHECK)
	);
    }


    public final YoixObject
    get(int index, boolean clone) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).get(index, clone)
	    : VM.abort(TYPECHECK)
	);
    }


    public final YoixObject
    get(String name, boolean clone) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).get(name, clone)
	    : VM.abort(TYPECHECK)
	);
    }


    public final int
    hash(String name) {

	return(value[0] instanceof YoixInterfacePointer ? ((YoixInterfacePointer)value[0]).hash(name) : -1);
    }


    public final String
    name(int index) {

	return(value[0] instanceof YoixInterfacePointer ? ((YoixInterfacePointer)value[0]).name(index) : null);
    }


    public final YoixObject
    put(int index, YoixObject obj, boolean clone) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).put(index, obj, clone)
	    : VM.abort(TYPECHECK)
	);
    }


    public final YoixObject
    put(String name, YoixObject obj, boolean clone) {

	return(value[0] instanceof YoixInterfacePointer
	    ? ((YoixInterfacePointer)value[0]).put(name, obj, clone)
	    : VM.abort(TYPECHECK)
	);
    }


    public final boolean
    readable(int index) {

	return(value[0] instanceof YoixInterfacePointer && ((YoixInterfacePointer)value[0]).readable(index));
    }


    public final boolean
    readable(String name) {

	return(value[0] instanceof YoixInterfacePointer && ((YoixInterfacePointer)value[0]).readable(name));
    }


    public final int
    reserve(String name) {

	return(value[0] instanceof YoixInterfacePointer ? ((YoixInterfacePointer)value[0]).reserve(name) : -1);
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public final boolean
    bodyEquals(YoixObject obj) {

	return(obj != null && value[0] == obj.value[0]);
    }


    public final boolean
    booleanValue() {

	//
	// We recently (5/12/09) changed this from
	//
	//     return(intValue() != 0 ? true : false);
	//
	// so it would be consistent with the implementation of the method
	// defined in YoixInterpreter.java, which we believe does a better
	// job handling NaN.
	//

	return(doubleValue() != 0);
    }


    public final boolean
    canExecute() {

	return(canAccess(__X));
    }


    public final boolean
    canRead() {

	return(canAccess(R__));
    }


    public final boolean
    canReadBody() {

	return(canAccessBody(R__));
    }


    public final boolean
    canWrite() {

	return(canAccess(_W_));
    }


    public final boolean
    canWriteBody() {

	return(canAccessBody(_W_));
    }


    public final double
    doubleValue() {

	double  result = 0;

	if (isNumber())
	    result = ((YoixBodyNumber)value[0]).doubleValue();
	else VM.abort(TYPECHECK);

	return(result);
    }


    public final double[]
    dtransform(double dx, double dy) {

	if (isMatrix())
	    return(((YoixBodyMatrix)value[0]).dtransform(dx, dy));
	else VM.abort(TYPECHECK);

	return(null);
    }


    public final boolean
    equals(Object obj) {

	return(obj instanceof YoixObject && YoixInterpreter.equalsEQEQ(this, (YoixObject)obj));
    }


    public final float
    floatValue() {

	float  result = 0;

	if (isNumber())
	    result = ((YoixBodyNumber)value[0]).floatValue();
	else VM.abort(TYPECHECK);

	return(result);
    }


    public final YoixObject
    get() {

	return(get(offset(), false));
    }


    public final YoixObject
    get(boolean clone) {

	return(get(offset(), clone));
    }


    public final YoixObject
    get(String name) {

	return(get(name, false));
    }


    public final int
    getAccess() {

	return(flags&ACCESSMASK);
    }


    public final int
    getAccessBody() {

	return(value[0] instanceof YoixPointer ? ((YoixPointer)value[0]).getAccess() : 0);
    }


    public final AffineTransform
    getAffineTransform() {

	return(isMatrix() ? ((YoixBodyMatrix)body()).getCurrentAffineTransform() : null);
    }


    public final boolean
    getBoolean(int index) {

	return(defined(index) ? get(index, false).intValue() != 0 : false);
    }


    public final boolean
    getBoolean(String name) {

	return(defined(name) ? get(name, false).intValue() != 0 : false);
    }


    public final boolean
    getBoolean(int index, boolean fail) {

	return(defined(index) ? get(index, false).intValue() != 0 : fail);
    }


    public final boolean
    getBoolean(String name, boolean fail) {

	return(defined(name) ? get(name, false).intValue() != 0 : fail);
    }


    public final Color
    getColor(int index) {

	return(YoixMake.javaColor(getObject(index)));
    }


    public final Color
    getColor(String name) {

	return(YoixMake.javaColor(getObject(name)));
    }


    public final Color
    getColor(int index, Color fail) {

	return(YoixMake.javaColor(getObject(index), fail));
    }


    public final Color
    getColor(String name, Color fail) {

	return(YoixMake.javaColor(getObject(name), fail));
    }


    public final Boolean
    getDefinedBoolean(String name) {

	YoixObject  obj;
	Boolean     value = null;

	if ((obj = getObject(name)) != null) {
	    if (obj.isNumber())
		value = obj.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
	}
	return(value);
    }


    public final Certificate
    getDefinedCertificate(String name) {

	Certificate  value = null;
	YoixObject   obj;

	if ((obj = getObject(name)) != null) {
	    if (obj.notNull() && obj.isCertificate()) 
		value = (Certificate)obj.getManagedObject();
	}
	return(value);
    }


    public final Integer
    getDefinedInteger(String name) {

	YoixObject  obj;
	Integer     value = null;

	if ((obj = getObject(name)) != null) {
	    if (obj.isNumber())
		value = new Integer(obj.intValue());
	}
	return(value);
    }


    public final Object
    getDefinedManagedObject(String name) {

	YoixObject  obj;
	Object      value = null;

	if ((obj = getObject(name)) != null) {
	    if (obj.notNull())
		value = obj.getManagedObject();
	}
	return(value);
    }


    public final SecureRandom
    getDefinedSecureRandom(String name) {

	SecureRandom  value = null;
	YoixObject    obj;

	if ((obj = getObject(name)) != null) {
	    if (obj.notNull() && obj.isRandom() && obj.getBoolean(N_SECURE))
		value = (SecureRandom)obj.getManagedObject();
	}
	return(value);
    }


    public final String
    getDefinedString(String name) {

	YoixObject  obj;
	String      value = null;

	if ((obj = getObject(name)) != null) {
	    if (obj.notNull() && obj.isString())
		value = obj.stringValue();
	}
	return(value);
    }


    public final double
    getDouble(int index) {

	return(defined(index) ? get(index, false).doubleValue() : 0.0);
    }


    public final double
    getDouble(int index, double fail) {

	return(defined(index) ? get(index, false).doubleValue() : fail);
    }


    public final double
    getDouble(String name) {

	return(defined(name) ? get(name, false).doubleValue() : 0.0);
    }


    public final double
    getDouble(String name, double fail) {

	return(defined(name) ? get(name, false).doubleValue() : fail);
    }


    public final float
    getFloat(int index, double fail) {

	return(defined(index) ? get(index, false).floatValue() : (float)fail);
    }


    public final float
    getFloat(String name, double fail) {

	return(defined(name) ? get(name, false).floatValue() : (float)fail);
    }


    public final YoixGraphElement
    getGraphElement() {

	//
	// Thrown in quickly for an internal application, but what's really
	// needed is a safe and general way to access it (and other things)
	// from outside this package.
	//

	return(isGraph() ? ((YoixBodyElement)body()).getElement() : null);
    }


    public final int
    getInt(int index) {

	return(defined(index) ? get(index, false).intValue() : 0);
    }


    public final int
    getInt(int index, int fail) {

	return(defined(index) ? get(index, false).intValue() : fail);
    }


    public final int
    getInt(String name) {

	return(defined(name) ? get(name, false).intValue() : 0);
    }


    public final int
    getInt(String name, int fail) {

	return(defined(name) ? get(name, false).intValue() : fail);
    }


    public final Object
    getLock() {

	//
	// Returns a reference to the object that Yoix interpreter uses for
	// synchronization that's explicitly requested by a Yoix script.
	//

	return(value[0] instanceof YoixPointer
	    ? ((YoixPointer)value[0]).getLock()
	    : VM.die(TYPECHECK)
	);
    }


    public final long
    getLong(int index, double fail) {

	return(defined(index) ? get(index, false).longValue() : (long)fail);
    }


    public final long
    getLong(String name, double fail) {

	return(defined(name) ? get(name, false).longValue() : (long)fail);
    }


    public final YoixObject
    getObject() {

	return(getObject(offset()));
    }


    public final YoixObject
    getObject(int index) {

	return(defined(index) ? get(index, false) : null);
    }


    public final YoixObject
    getObject(String name) {

	return(defined(name) ? get(name, false) : null);
    }


    public final YoixObject
    getObject(int index, YoixObject fail) {

	return(defined(index) ? get(index, false) : fail);
    }


    public final YoixObject
    getObject(String name, YoixObject fail) {

	return(defined(name) ? get(name, false) : fail);
    }


    public final YoixObject
    getObject(int index, int fail) {

	return(defined(index) ? get(index, false) : newInt(fail));
    }


    public final YoixObject
    getObject(String name, int fail) {

	return(defined(name) ? get(name, false) : newInt(fail));
    }


    public final YoixObject
    getObject(int index, double fail) {

	return(defined(index) ? get(index, false) : newDouble(fail));
    }


    public final YoixObject
    getObject(String name, double fail) {

	return(defined(name) ? get(name, false) : newDouble(fail));
    }


    public final String
    getString(int index) {

	return(defined(index) ? get(index, false).stringValue() : null);
    }


    public final String
    getString(String name) {

	return(defined(name) ? get(name, false).stringValue() : null);
    }


    public final String
    getString(int index, String fail) {

	return(defined(index) ? get(index, false).stringValue() : fail);
    }


    public final String
    getString(String name, String fail) {

	return(defined(name) ? get(name, false).stringValue() : fail);
    }


    public final double[]
    idtransform(double dx, double dy, double fail[]) {

	if (isMatrix())
	    return(((YoixBodyMatrix)value[0]).idtransform(dx, dy, fail));
	else VM.abort(TYPECHECK);

	return(null);
    }


    public final int
    intValue() {

	int  result = 0;

	if (isNumber())
	    result = ((YoixBodyNumber)value[0]).intValue();
	else if (isControl())
	    result = ((YoixBodyControl)value[0]).intValue();
	else VM.abort(TYPECHECK);

	return(result);
    }


    public final boolean
    isArray() {

	return(type == POINTER && ((YoixInterfaceBody)value[0]).type() == ARRAY);
    }


    public final boolean
    isArrayPointer() {

	return(defined() && get().isArray());
    }


    public final boolean
    isBodyInstanceOf(Class type) {

	return(type == null || value[0] == null ? false : type.isInstance(value[0]));
    }


    public final boolean
    isBuiltin() {

	return(type == CALLABLE && ((YoixInterfaceBody)value[0]).type() == BUILTIN);
    }


    public final boolean
    isCallable() {

	return(value[0] instanceof YoixInterfaceCallable);
    }


    public final boolean
    isCallablePointer() {

	boolean  result = false;

	if (defined()) {
	    VM.pushAccess(R__);
	    result = get().isCallable();
	    VM.popAccess();
	}
	return(result);
    }


    public final boolean
    isColor() {

	return(T_COLOR.equals(getTypename()));
    }


    public final boolean
    isComponent() {

	return(value[0] instanceof YoixBodyComponent);
    }


    public final boolean
    isDictionary() {

	return(type == POINTER && ((YoixInterfaceBody)value[0]).type() == DICTIONARY);
    }


    public final boolean
    isDictionaryPointer() {

	return(defined() && get().isDictionary());
    }


    public final boolean
    isDimension() {

	return(T_DIMENSION.equals(getTypename()));
    }


    public final boolean
    isDouble() {

	return(((YoixInterfaceBody)value[0]).type() == DOUBLE);
    }


    public final boolean
    isDoublePointer() {

	return(defined() && get().isDouble());
    }


    public final boolean
    isEdge() {

	return(value[0] instanceof YoixBodyElement && ((YoixBodyElement)value[0]).isEdge());
    }


    public final boolean
    isEmpty() {

	return(type == CONTROL && ((YoixInterfaceBody)value[0]).type() == EMPTY);
    }


    public final boolean
    isFile() {

	return(value[0] instanceof YoixBodyStream && ((YoixBodyStream)value[0]).isFile());
    }


    public final boolean
    isFont() {

	return(value[0] instanceof YoixBodyFont);
    }


    public final boolean
    isFunction() {

	return(type == CALLABLE && ((YoixInterfaceBody)value[0]).type() == FUNCTION);
    }


    public final boolean
    isFunctionPointer() {

	YoixObject  obj;
	boolean     result = false;

	//
	// Unfortunately this is a little trickier than it should be.
	//

	if (defined()) {
	    VM.pushAccess(R__);
	    obj = get();
	    result = obj.isFunction() && obj.notNull();		// confusing test!!
	    VM.popAccess();
	}
	return(result);
    }


    public final boolean
    isGraph() {

	return(value[0] instanceof YoixBodyElement && ((YoixBodyElement)value[0]).isGraph());
    }


    public final boolean
    isGraphics() {

	return(value[0] instanceof YoixBodyGraphics);
    }


    public final boolean
    isHashtable() {

	return(value[0] instanceof YoixBodyHashtable);
    }


    public final boolean
    isImage() {

	return(value[0] instanceof YoixBodyImage);
    }


    public final boolean
    isInteger() {

	return(type == NUMBER && ((YoixInterfaceBody)value[0]).type() == INTEGER);
    }


    public final boolean
    isIntegerPointer() {

	return(defined() && get().isInteger());
    }


    public final boolean
    isMatrix() {

	return(value[0] instanceof YoixBodyMatrix);
    }


    public final boolean
    isNaN() {

	return(type == NUMBER ? Double.isNaN(doubleValue()) : false);
    }


    public final boolean
    isNode() {

	return(value[0] instanceof YoixBodyElement && ((YoixBodyElement)value[0]).isNode());
    }


    public final boolean
    isNormalNumber() {

	boolean  result;
	double   value;

	if (type == NUMBER) {
	    value = doubleValue();
	    if (Double.isNaN(value) || Double.isInfinite(value))
		result = false;
	    else result = true;
	} else result = false;

	return(result);
    }


    public final boolean
    isNull() {

	return(value[0] instanceof YoixBodyNull);
    }


    public final boolean
    isNumber() {

	return(type == NUMBER);
    }


    public final boolean
    isNumberPointer() {

	return(defined() && get().isNumber());
    }


    public final boolean
    isParseTree() {

	return(value[0] instanceof YoixBodyParseTree);
    }


    public final boolean
    isPoint() {

	return(T_POINT.equals(getTypename()));
    }


    public final boolean
    isPointer() {

	//
	// Old version did
	//
	//     return(type == POINTER);
	//
	// but functions now expose some internal data by implementing the
	// YoixInterfacePointer interface, so we now need a different test.
	// 

	return(value[0] instanceof YoixInterfacePointer);
    }


    public final boolean
    isProcess() {

	return(value[0] instanceof YoixBodyProcess);
    }


    public final boolean
    isRectangle() {

	return(T_RECTANGLE.equals(getTypename()));
    }


    public final boolean
    isServerSocket() {

	return(value[0] instanceof YoixBodyServerSocket);
    }


    public final boolean
    isSocket() {

	return(value[0] instanceof YoixBodySocket);
    }


    public final boolean
    isStream() {

	return(value[0] instanceof YoixBodyStream);
    }


    public final boolean
    isString() {

	return(type == POINTER && ((YoixInterfaceBody)value[0]).type() == STRING);
    }


    public final boolean
    isStringPointer() {

	return(defined() && get().isString());
    }


    public final boolean
    isStringStream() {

	return(value[0] instanceof YoixBodyStream && ((YoixBodyStream)value[0]).isStringStream());
    }


    public final boolean
    isThread() {

	return(value[0] instanceof YoixBodyThread);
    }


    public final boolean
    isTimeZone() {

	return(value[0] instanceof YoixBodyTimeZone);
    }


    public final boolean
    isType(String name) {

	return(name != null && name.equals(getTypename()));
    }


    public final boolean
    isURL() {

	return(value[0] instanceof YoixBodyStream && ((YoixBodyStream)value[0]).isURL());
    }


    public final boolean
    isVector() {

	return(value[0] instanceof YoixBodyVector);
    }


    public final double[]
    itransform(double x, double y, double fail[]) {

	if (isMatrix())
	    return(((YoixBodyMatrix)value[0]).itransform(x, y, fail));
	else VM.abort(TYPECHECK);

	return(null);
    }


    public final long
    longValue() {

	long  result = 0;

	if (isNumber())
	    result = (long)((YoixBodyNumber)value[0]).longValue();
	else VM.abort(TYPECHECK);

	return(result);
    }


    public static YoixObject
    newArray() {

	return(newNullPointer(ARRAY, null));
    }


    public static YoixObject
    newArray(int length) {

	return(length >= 0
	    ? pointer_template.construct(new YoixBodyArray(length))
	    : newNullPointer(ARRAY, null)
	);
    }


    public static YoixObject
    newArray(int length, int limit) {

	YoixObject  obj;

	if (length >= 0) {
	    obj = pointer_template.construct(new YoixBodyArray(length));
	    obj.setGrowable(true);
	    obj.setGrowto(limit);
	} else obj = newNullPointer(ARRAY, null);
	return(obj);
    }


    public static YoixObject
    newBuiltin() {

	return(newBuiltin(null, 0, true));
    }


    public static YoixObject
    newCertificate(java.security.cert.Certificate certificate) {

	return(certificate != null
	    ? pointer_template.construct(new YoixBodyCertificate(VM.getTypeTemplate(T_CERTIFICATE), certificate), T_CERTIFICATE)
	    : newNullPointer(CERTIFICATE, T_CERTIFICATE)
	);
    }


    public static YoixObject
    newColor() {

	return(YoixObject.newNull(T_COLOR));
    }


    public static YoixObject
    newColor(Color color) {

	YoixObject  obj;

	//
	// This duplicates YoixMake.yoixColor(), which probably will be
	// removed in a future release.
	//

	if (color != null) {
	    obj = YoixMake.yoixType(T_COLOR);
	    obj.put(N_RED, YoixObject.newDouble(color.getRed()/255.0), false);
	    obj.put(N_GREEN, YoixObject.newDouble(color.getGreen()/255.0), false);
	    obj.put(N_BLUE, YoixObject.newDouble(color.getBlue()/255.0), false);
	} else obj = YoixObject.newNull(T_COLOR);
	return(obj);
    }


    public static YoixObject
    newColor(String color) {

	return(newColor(new Color(YoixObject.newInt(color).intValue())));
    }


    public static YoixObject
    newDictionary() {

	return(newNullPointer(DICTIONARY, null));
    }


    public static YoixObject
    newDictionary(int length) {

	return(length >= 0
	    ? pointer_template.construct(new YoixBodyDictionaryObject(length))
	    : newNullPointer(DICTIONARY, null)
	);
    }


    public static YoixObject
    newDictionary(int length, int limit) {

	YoixObject  obj;

	if (length >= 0) {
	    obj = pointer_template.construct(new YoixBodyDictionaryObject(length));
	    obj.setGrowable(true);
	    obj.setGrowto(limit);
	} else obj = newNullPointer(DICTIONARY, null);
	return(obj);
    }


    public static YoixObject
    newDouble(double value) {

	return(number_template.construct(new YoixBodyNumber(value)));
    }


    public static YoixObject
    newDouble(Double value) {

	return(number_template.construct(new YoixBodyNumber(value)));
    }


    public static YoixObject
    newDouble(String image) {

	return(newDouble(Double.valueOf(image).doubleValue()));
    }


    public static YoixObject
    newEmpty() {

	return(empty_object);
    }


    public static YoixObject
    newFunction() {

	return(newNullCallable(FUNCTION, T_FUNCTION));
    }


    public static YoixObject
    newImage() {

	return(newNullPointer(IMAGE, T_IMAGE));
    }


    public static YoixObject
    newImage(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyImage(data))
	    : newNullPointer(IMAGE, T_IMAGE)
	);
    }


    public static YoixObject
    newImage(Image image) {

	return(image != null
	    ? pointer_template.construct(new YoixBodyImage(VM.getTypeTemplate(T_IMAGE), image), T_IMAGE)
	    : newNullPointer(IMAGE, T_IMAGE)
	);
    }


    public static YoixObject
    newImage(String source) {

	YoixObject  ival;

	ival = YoixObject.newDictionary(1);
	ival.putString(N_SOURCE, source);
	return(YoixMake.yoixType(T_IMAGE, ival));
    }


    public static YoixObject
    newInt(int value) {

	return(number_template.construct(new YoixBodyNumber(value)));
    }


    public static YoixObject
    newInt(Integer value) {

	return(number_template.construct(new YoixBodyNumber(value)));
    }


    public static YoixObject
    newInt(boolean value) {

	return(newInt(value ? 1 : 0));
    }


    public static YoixObject
    newInt(Boolean value) {

	return(newInt(value.booleanValue() ? 1 : 0));
    }


    public static YoixObject
    newInt(String image) {

	int  radix;
	int  ch;

	if (image.length() > 1 && image.charAt(0) == '0') {
	    if ((ch = image.charAt(1)) == 'x' || ch == 'X') {
		radix = 16;
		image = image.substring(2);
	    } else radix = 8;
	} else radix = 10;

	return(newInt(YoixMake.javaInt(image, radix, 0)));
    }


    public static YoixObject
    newLvalue(YoixObject obj, int index) {

	if (obj.value[0] instanceof YoixInterfacePointer) {
	    obj = (YoixObject)obj.clone();
	    obj.offset = index;
	} else VM.abort(TYPECHECK);

	return(obj);
    }


    public static YoixObject
    newLvalue(YoixObject obj, String name) {

	int  index;

	//
	// Checking the value returned by reserve() is very important
	// for error messages!!
	//

	if (obj.value[0] instanceof YoixInterfacePointer) {
	    if ((index = ((YoixInterfacePointer)obj.value[0]).reserve(name)) != -1) {
		obj = (YoixObject)obj.clone();
		obj.offset = index;
	    } else VM.abort(UNDEFINED, name);
	} else VM.abort(TYPECHECK);

	return(obj);
    }


    public static YoixObject
    newMatrix() {

	return(newNullPointer(MATRIX, T_MATRIX));
    }


    public static YoixObject
    newMatrix(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyMatrix(data))
	    : newNullPointer(MATRIX, T_MATRIX)
	);
    }


    public static YoixObject
    newMatrix(AffineTransform transform) {

	YoixObject  data;

	if (transform != null) {
	    if ((data = VM.getTypeTemplate(T_MATRIX)) != null) {
		data.putDouble(N_SX, transform.getScaleX());
		data.putDouble(N_SY, transform.getScaleY());
		data.putDouble(N_SHX, transform.getShearX());
		data.putDouble(N_SHY, transform.getShearY());
		data.putDouble(N_TX, transform.getTranslateX());
		data.putDouble(N_TY, transform.getTranslateY());
	    } else data = null;
	} else data = null;

	return(newMatrix(data));
    }


    public static YoixObject
    newNull() {

	return(getCachedNull(T_OBJECT, true));
    }


    public static YoixObject
    newNull(String typename) {

	return(getCachedNull(typename, true));
    }


    public static YoixObject
    newNull(YoixObject dest) {

	YoixObject  obj;

	//
	// Sometimes asked to do the ridiculous, like make a NULL object
	// that can be assigned to an integer. In those cases we return
	// null and let the caller (e.g., yoixInstance()) decide what's
	// correct.
	//

	if ((obj = getCachedNull(VM.getTypename(dest), true)) == null)
	    obj = newNull(dest.major(), dest.minor(), VM.getTypename(dest));
	return(obj);
    }


    public static YoixObject
    newNullPointer() {

	return(newNullPointer(POINTER, null));	// could use getCachedNull(T_POINTER, true)
    }


    public static YoixObject
    newNumber(int value) {

	YoixObject  obj;

	if (value >= MIN_CACHEDINT && value <= MAX_CACHEDINT) {
	    if ((obj = intcache[value - MIN_CACHEDINT]) == null) {
		obj = number_template.construct(new YoixBodyNumber(value));
		intcache[value - MIN_CACHEDINT] = obj;
	    }
	    return(obj);
	}

	return(number_template.construct(new YoixBodyNumber(value)));
    }


    public static YoixObject
    newNumber(double value) {

	return(number_template.construct(new YoixBodyNumber(value)));
    }


    public static YoixObject
    newNumber(Number value) {

	return(number_template.construct(new YoixBodyNumber(value)));
    }


    public static YoixObject
    newNumber(String image) {

	YoixObject  obj = null;
	long        num;
	int         radix;
	int         ch;

	if (image.length() > 1 && image.charAt(0) == '0') {
	    if ((ch = image.charAt(1)) == 'x' || ch == 'X') {
		radix = 16;
		image = image.substring(2);
	    } else radix = 8;
	} else radix = 10;

	try {
	    obj = newNumber(Integer.parseInt(image, radix));
	}
	catch(NumberFormatException e) {
	    try {
		num = Long.parseLong(image, radix);
		if ((num >>> 32) == 0)
		    obj = newNumber((int)num);
		else obj = newNumber(num);
	    }
	    catch(NumberFormatException ee) {
		obj = newNumber(radix == 10
		    ? YoixMake.javaDouble(image, Double.NaN)
		    : Double.NaN
		);
	    }
	}

	return(obj);
    }


    public static YoixObject
    newParseTree() {

	return(newNullPointer(PARSETREE, T_PARSETREE));
    }


    public static YoixObject
    newParseTree(int parser) {

	return(newParseTree(parser, null));
    }


    public static YoixObject
    newParseTree(int parser, String parse) {

	YoixObject  obj = null;
	YoixObject  dict;

	if ((dict = VM.getTypeTemplate(T_PARSETREE)) != null) {
	    dict.putInt(N_PARSER, parser);
	    dict.putString(N_PARSE, parse);
	    obj = newParseTree(dict);
	} else obj = newParseTree();

	return(obj);
    }


    public static YoixObject
    newPoint() {

	return(newNull(T_POINT));
    }


    public static YoixObject
    newPoint(Point point) {

	return(point != null
	    ? newPoint(point.getX(), point.getY())
	    : newPoint()
	);
    }


    public static YoixObject
    newPoint(Point2D point) {

	return(point != null
	    ? newPoint(point.getX(), point.getY())
	    : newPoint()
	);
    }


    public static YoixObject
    newPoint(double coords[]) {

	return(newPoint(coords[0], coords[1]));
    }


    public static YoixObject
    newPoint(double x, double y) {

	YoixObject  obj;

	obj = YoixMake.yoixType(T_POINT);
	obj.putDouble(N_X, x);
	obj.putDouble(N_Y, y);
	return(obj);
    }


    public static YoixObject
    newPointer(YoixPointer pointer) {

	return(pointer != null
	    ? pointer_template.construct(pointer, null)
	    : VM.die(INTERNALERROR)
	);
    }


    public static YoixObject
    newRectangle() {

	return(newNull(T_RECTANGLE));
    }


    public static YoixObject
    newRectangle(Rectangle2D rect) {

	return(rect != null
	    ? newRectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight())
	    : newRectangle()
	);
    }


    public static YoixObject
    newRectangle(double x, double y, double width, double height) {

	YoixObject  obj;

	obj = YoixMake.yoixType(T_RECTANGLE);
	obj.putDouble(N_X, x);
	obj.putDouble(N_Y, y);
	obj.putDouble(N_WIDTH, width);
	obj.putDouble(N_HEIGHT, height);
	return(obj);
    }


    public static YoixObject
    newStream() {

	return(newNullPointer(STREAM, T_STREAM));
    }


    public static YoixObject
    newString() {

	return(newNullPointer(STRING, null));
    }


    public static YoixObject
    newString(int length) {

	return(length >= 0
	    ? pointer_template.construct(new YoixBodyString(length))
	    : newNullPointer(STRING, null)
	);
    }


    public static YoixObject
    newString(String value) {

	return(value != null
	    ? pointer_template.construct(new YoixBodyString(value))
	    : newNullPointer(STRING, null)
	);
    }


    public static YoixObject
    newString(StringBuffer sbuf) {

	return(sbuf != null
	    ? pointer_template.construct(new YoixBodyString(sbuf))
	    : newNullPointer(STRING, null)
	);
    }


    public static YoixObject
    newString(char ch) {

	return(pointer_template.construct(new YoixBodyString(new char[] {ch})));
    }


    public static YoixObject
    newThread() {

	return(newNullPointer(THREAD, T_THREAD));
    }


    public final boolean
    notArray() {

	return(!isArray());
    }


    public final boolean
    notCallable() {

	return(!isCallable());
    }


    public final boolean
    notColor() {

	return(!isColor());
    }


    public final boolean
    notDictionary() {

	return(!isDictionary());
    }


    public final boolean
    notEmpty() {

	return(!isEmpty());
    }


    public final boolean
    notFunction() {

	return(!isFunction());
    }


    public final boolean
    notNull() {

	return(!isNull());
    }


    public final boolean
    notNumber() {

	return(!isNumber());
    }


    public final boolean
    notPointer() {

	return(!isPointer());
    }


    public final boolean
    notString() {

	return(!isString());
    }


    public final int
    offset() {

	return(offset);
    }


    public final YoixObject
    put(YoixObject obj) {

	return(put(offset(), obj, false));
    }


    public final YoixObject
    put(String name, YoixObject obj) {

	return(put(name, obj, false));
    }


    public final void
    putColor(int index, Color color) {

	put(index, YoixMake.yoixColor(color), false);
    }


    public final void
    putColor(String name, Color color) {

	put(name, YoixMake.yoixColor(color), false);
    }


    public final void
    putDouble(int index, double value) {

	put(index, newDouble(value), false);
    }


    public final void
    putDouble(String name, double value) {

	put(name, newDouble(value), false);
    }


    public final void
    putInt(int index, boolean value) {

	put(index, newInt(value), false);
    }


    public final void
    putInt(String name, boolean value) {

	put(name, newInt(value), false);
    }


    public final void
    putInt(int index, int value) {

	put(index, newInt(value), false);
    }


    public final void
    putInt(String name, int value) {

	put(name, newInt(value), false);
    }


    public final void
    putNull(int index) {

	put(index, newNull(), false);
    }


    public final void
    putNull(String name) {

	put(name, newNull(), false);
    }


    public final void
    putNull(int index, String typename) {

	put(index, newNull(typename), false);
    }


    public final void
    putNull(String name, String typename) {

	put(name, newNull(typename), false);
    }


    public final void
    putObject(int index, YoixObject obj) {

	put(index, obj != null ? obj : newNull(), false);
    }


    public final void
    putObject(String name, YoixObject obj) {

	put(name, obj != null ? obj : newNull(), false);
    }


    public final void
    putString(int index, String value) {

	put(index, newString(value), false);
    }


    public final void
    putString(String name, String value) {

	put(name, newString(value), false);
    }


    public final YoixObject
    resolve() {

	//
	// Made public for current compiler implementation, but there's no
	// good reason for the change (6/13/09).
	//

	return(((flags&RESOLVE) != 0) ? get(offset(), false) : this);
    }


    public final YoixObject
    resolveClone() {

	//
	// Made public for current compiler implementation, but there's no
	// good reason for the change (6/13/09).
	//

	return(((flags&RESOLVE) != 0) ? get(offset(), true) : (YoixObject)clone());
    }


    public final YoixObject
    setBodyField(String name, YoixObject obj, boolean clone) {

	YoixObject  retobj;

	retobj = put(name, obj, clone);
	if (value[0] instanceof YoixPointerActive)
	    retobj = ((YoixPointerActive)value[0]).setField(name, obj);

	return(retobj);
    }


    public final int
    sizeof() {

	//
	// Don't think we need to synchronize - length never decreases
	// and offset doesn't change. Return value should never be more
	// than what's really available.
	//

	return((offset() >= 0 && offset() < length()) ? length() - offset() : 0);
    }


    public final String
    stringValue() {

	String  str = null;

	if (isNull())
	    str = "";
	else if (isString())
	    str = ((YoixBodyString)value[0]).stringValue(offset());
	else VM.abort(TYPECHECK);

	return(str);
    }


    public final String
    stringValue(boolean tonull) {

	return(tonull ? YoixMake.CString(this) : stringValue());
    }


    public final TimeZone
    timeZoneValue() {

	return(isTimeZone()
	    ? (TimeZone)(((TimeZone)getManagedObject()).clone())
	    : null
	);
    }


    public final char[]
    toCharArray() {

	char  buf[] = null;

	if (isNull())
	    buf = new char[0];
	else if (isString())
	    buf = ((YoixBodyString)value[0]).toCharArray(offset());
	else VM.abort(TYPECHECK);

	return(buf);
    }


    public final char[]
    toCharArray(boolean grab, Object arg) {

	char  buf[] = null;

	//
	// A special version that tries to get direct access to the
	// data buffer that's currently used by the string. There
	// are absolutely no guarantees this will succeed or that
	// the string will continue to use the returned buffer. In
	// addition, the implementation details may change without
	// notice, so applications that rely on this method may not
	// work in new releases of the interpreter.
	//
	// NOTE - arg is unused, but that could change. Its purpose
	// is currently unspecified.
	//

	if (isNull())
	    buf = new char[0];
	else if (isString())
	    buf = ((YoixBodyString)value[0]).toCharArray(grab, arg);
	else VM.abort(TYPECHECK);

	return(buf);
    }


    public final double[]
    transform(double x, double y) {

	if (isMatrix())
	    return(((YoixBodyMatrix)value[0]).transform(x, y));
	else VM.abort(TYPECHECK);

	return(null);
    }


    public final String
    typename() {

	String  name;

	return((name = getTypename()) != null ? name : VM.getTypename(this));
    }

    ///////////////////////////////////
    //
    // YoixAPIProtected Methods
    //
    ///////////////////////////////////

    protected Object
    body() {

	return(value[0]);
    }


    protected YoixObject
    forceCall(YoixObject argv[], YoixObject context) {

	try {
	    VM.pushAccess(L__X);
	    return(call(argv, context));
	}
	finally {
	    VM.popAccess();
	}
    }


    protected YoixObject
    forceExecute(int index, YoixObject argv[], YoixObject context) {

	try {
	    VM.pushAccess(L__X);
	    return(execute(index, argv, context));
	}
	finally {
	    VM.popAccess();
	}
    }


    protected YoixObject
    forceExecute(String name, YoixObject argv[], YoixObject context) {

	try {
	    VM.pushAccess(L__X);
	    return(execute(name, argv, context));
	}
	finally {
	    VM.popAccess();
	}
    }


    protected YoixObject
    forceGetObject(int index) {

	try {
	    VM.pushAccess(LR__);
	    return(getObject(index));
	}
	finally {
	    VM.popAccess();
	}
    }


    protected YoixObject
    forceGetObject(String name) {

	try {
	    VM.pushAccess(LR__);
	    return(getObject(name));
	}
	finally {
	    VM.popAccess();
	}
    }


    protected void
    forcePutObject(int index, YoixObject obj) {

	try {
	    VM.pushAccess(LRW_);	// L_W_ might be sufficient
	    putObject(index, obj);
	}
	finally {
	    VM.popAccess();
	}
    }


    protected void
    forcePutObject(String name, YoixObject obj) {

	try {
	    VM.pushAccess(LRW_);	// L_W_ might be sufficient
	    putObject(name, obj);
	}
	finally {
	    VM.popAccess();
	}
    }


    protected void
    forceSetAccess(int perm) {

	try {
	    VM.pushAccess(LRW_);
	    setAccess(perm);
	}
	finally {
	    VM.popAccess();
	}
    }


    protected void
    forceSetAccessBody(int perm) {

	try {
	    VM.pushAccess(LRW_);
	    setAccessBody(perm);
	}
	finally {
	    VM.popAccess();
	}
    }


    protected void
    forceSetAccessElement(int index, int perm) {

	try {
	    VM.pushAccess(LRW_);
	    setAccessElement(index, perm);
	}
	finally {
	    VM.popAccess();
	}
    }


    protected void
    forceSetAccessElement(String name, int perm) {

	try {
	    VM.pushAccess(LRW_);
	    setAccessElement(name, perm);
	}
	finally {
	    VM.popAccess();
	}
    }


    protected Object
    getManagedObject() {

	if (value[0] instanceof YoixPointerActive)
	    return(((YoixPointerActive)value[0]).getManagedObject());
	else if (value[0] instanceof YoixBodyControl)
	    return(((YoixBodyControl)value[0]).getManagedObject());
	else return(null);
    }


    protected boolean
    inRange() {

	return(value[0] instanceof YoixPointer && ((YoixPointer)value[0]).inRange(offset()));
    }


    protected void
    setAccessBody(int perm) {

	if (value[0] instanceof YoixPointer)
	    ((YoixPointer)value[0]).setAccess(perm);
    }


    protected void
    setAccessElement(int index, int perm) {

	YoixObject  element;

	if (value[0] instanceof YoixPointer) {
	    if ((element = getObject(index)) != null)
		element.setAccess(perm);
	}
    }


    protected void
    setAccessElement(String name, int perm) {

	YoixObject  element;

	if (value[0] instanceof YoixPointer) {
	    if ((element = getObject(name)) != null)
		element.setAccess(perm);
	}
    }


    protected void
    setGrowable(boolean state) {

	if (value[0] instanceof YoixPointer)
	    ((YoixPointer)value[0]).setGrowable(state);
    }


    protected void
    setGrowto(int limit) {

	if (value[0] instanceof YoixPointer)
	    ((YoixPointer)value[0]).setGrowto(limit);
    }

    ///////////////////////////////////
    //
    // YoixObject Methods
    //
    ///////////////////////////////////

    static Object
    body(YoixObject obj, YoixPointerActive owner) {

	Package  target;
	Package  dest;
	String   targetname;
	String   destname;
	Object   result = null;
	Object   body;

	if (obj != null && owner != null) {
	    if ((body = obj.body()) != null) {
		target = body.getClass().getPackage();
		dest = owner.getClass().getPackage();
		if (target != dest) {
		    if (target != null && dest != null) {
			if ((targetname = target.getName()) != null) {
			    destname = dest.getName();
			    if (targetname.equals(destname) || YOIXPACKAGE.equals(destname))
				result = body;
			}
		    }
		} else result = body;
	    }
	}
	return(result);
    }


    final boolean
    canAccess(short perm) {

	return((flags&perm) == (perm&ACCESSMASK) || VM.canAccess(perm));
    }


    final boolean
    canAccessBody(short perm) {

	return(value[0] instanceof YoixPointer && ((YoixPointer)value[0]).canAccess(perm));
    }


    final boolean
    canGrowTo(int length) {

	return(value[0] instanceof YoixPointer && ((YoixPointer)value[0]).canGrowTo(length));
    }


    final boolean
    canResolve() {

	return(isPointer() && (flags&RESOLVE) != 0);
    }


    final boolean
    canUnlock() {

	return((flags&L___) == 0 || VM.canAccess(L___));
    }


    final boolean
    canUnroll() {

	return(isPointer() && (flags&UNROLL) != 0);
    }


    final int
    capacity(int limit) {

	return(value[0] instanceof YoixPointer && offset() >= 0 ? ((YoixPointer)value[0]).getCapacity(limit + offset()) : 0);
    }


    final YoixObject
    cast(YoixObject dest, boolean clone) {

	YoixObject  obj;

	//
	// Heavily used routine (even though there are only a few calls
	// in the source code), so we eventually will speed it up!! The
	// case where nothing needs to be done is the most common.
	//

	obj = this;

	if (dest != null && (dest.flags&ANYMAJOR) == 0) {
	    if (major() == dest.major()) {
		if (minor() != dest.minor()) {
		    switch (major()) {
			case CALLABLE:
			    if ((dest.flags&ANYMINOR) == 0)
				obj = isNull() ? newNull(dest) : null;
			    break;

			case POINTER:
			    //
			    // This is a recent change (12/15/04). Previous
			    // version, which had not changed for a long time,
			    // was grouped with CALLABLE case. Be suspicious
			    // of this change if you notice any strange type
			    // related behavior. Turns out the test when type
			    // names matched needed to eliminate null (for
			    // some reason that probably should eventually be
			    // looked at more carefully). Anyway changed this
			    // to account for null on 12/21/04 and our dataviz
			    // program that complained is now happy.
			    //
			    if ((dest.flags&ANYMINOR) == 0) {
				if (notNull()) {
				    if (dest.getTypename() != obj.getTypename())
					obj = null;
				    else if (dest.getTypename() == null)
					obj = null;
				} else obj = newNull(dest);
			    }
			    break;

			case NUMBER:
			    if ((dest.flags&ANYMINOR) == 0) {
				if (dest.isInteger())
				    obj = newInt(intValue());
				else obj = newDouble(doubleValue());
			    }
			    break;

			case STREAM:
			    if ((dest.flags&ANYMINOR) == 0) {
				if (dest.major() != STREAM)
				    obj = null;
			    }
			    break;

			default:
			    obj = null;
			    break;
		    }
		} else if (dest.getTypename() != obj.getTypename()) {
		    if ((dest.flags&ANYMINOR) == 0)
			obj = isNull() ? newNull(dest) : null;
		}
	    } else obj = isNull() ? newNull(dest) : null;
	}

	return((clone && obj == this) ? (YoixObject)clone() : obj);
    }


    static YoixObject
    cast(YoixObject src, YoixObject dest, boolean clone) {

	YoixObject  obj;

	if ((obj = src.cast(dest, clone)) != null)
	    obj.setMode(dest.mode());
	else VM.abort(TYPECHECK);

	return(obj);
    }


    final boolean
    close() {

	return(isStream() && ((YoixBodyStream)value[0]).close());
    }


    final void
    declare(YoixObject obj) {

	declare(offset(), obj, obj.flags);
    }


    final void
    declare(String name, YoixObject obj) {

	declare(name, obj, obj.flags);
    }


    final boolean
    defined() {

	return(defined(offset()));
    }


    final String
    dump(String indent) {

	return(dump(offset(), indent, getTypename()));
    }


    final YoixObject
    duplicate() {

	return(duplicate(new HashMap()));
    }


    final YoixObject
    duplicate(HashMap copied) {

	YoixObject  obj;
	int         n;

	if ((obj = (YoixObject)copied.get(this)) == null) {
	    obj = (YoixObject)clone();
	    if (obj.notNull()) {
		copied.put(this, obj);
		for (n = 0; n < value.length; n++) {
		    if (obj.value[n] instanceof YoixInterfaceCloneable)
			obj.value[n] = ((YoixInterfaceCloneable)obj.value[n]).copy(copied);
		}
	    }
	}

	return(obj);
    }


    final void
    end() {

	if (value[0] instanceof YoixBodyBlock)
	    ((YoixBodyBlock)value[0]).end();
	else VM.die(INTERNALERROR);
    }


    final YoixObject
    execute(YoixObject argv[]) {

	return(execute(offset(), argv, this));
    }


    final GraphicsConfiguration
    getGraphicsConfiguration() {

	GraphicsConfiguration  gc = null;
	YoixObject             root;
	YoixObject             screen;

	if (isComponent()) {
	    if ((root = getObject(N_ROOT)) != null) {
		if ((screen = root.getObject(N_SCREEN)) != null) {
		    if (screen.isScreen())
			gc = ((YoixBodyScreen)screen.body()).getGraphicsConfiguration();
		}
	    }
	} else if (isScreen())
	    gc = ((YoixBodyScreen)body()).getGraphicsConfiguration();

	return(gc);
    }


    final boolean
    getGrowable() {

	return(value[0] instanceof YoixPointer
	    ? ((YoixPointer)value[0]).getGrowable()
	    : false
	);
    }


    final int
    getGrowto() {

	return(value[0] instanceof YoixPointer
	    ? ((YoixPointer)value[0]).getGrowto()
	    : length()
	);
    }


    final String[]
    getKeys() {

	return(value[0] instanceof YoixBodyDictionaryObject
	    ? ((YoixBodyDictionaryObject)value[0]).getKeys()
	    : null
	);
    }


    final Object
    getManagedDrawable(Object lock) {

	Object  drawable;

	if (isDrawable()) {
	    if (isImage())
		drawable = ((YoixBodyImage)value[0]).getCurrentImage(lock);
	    else drawable = getManagedObject();
	} else drawable = null;

	return(drawable);
    }


    static Object
    getManagedObject(YoixObject obj, YoixPointerActive owner) {

	Package  target;
	Package  dest;
	String   targetname;
	String   destname;
	Object   result = null;
	Object   body;

	if (obj != null && owner != null) {
	    if ((body = obj.body()) != null) {
		target = body.getClass().getPackage();
		dest = owner.getClass().getPackage();
		if (target != dest) {
		    if (target != null && dest != null) {
			if ((targetname = target.getName()) != null) {
			    destname = dest.getName();
			    if (targetname.equals(destname) || YOIXPACKAGE.equals(destname))
				result = obj.getManagedObject();
			}
		    }
		} else result = obj.getManagedObject();
	    }
	}
	return(result);
    }


    final YoixObject[]
    getValues() {

	return(value[0] instanceof YoixBodyDictionaryObject
	    ? ((YoixBodyDictionaryObject)value[0]).getValues()
	    : null
	);
    }


    final double[]
    idtransform(double dx, double dy) {

	return(idtransform(dx, dy, null));
    }


    final void
    incrementLvalue(int incr) {

	//
	// This is for internal use and only, so be careful using it. We
	// currently omit permission checking, but that may change.
	//

	if (isPointer()) {
	    if (canWrite())
		offset += incr;
	    else VM.abort(INVALIDACCESS);
	} else VM.abort(TYPECHECK);
    }


    final int
    indexOf(int ch) {

	return(indexOf(ch, offset()));
    }


    final int
    indexOf(int ch, int from) {

	int  result = -1;

	if (isString()) {
	    if (notNull())
		result = ((YoixBodyString)value[0]).indexOf(ch, from);
	} else VM.abort(TYPECHECK);

	return(result);
    }


    final boolean
    isBorder() {

	return(T_BORDER.equals(getTypename()));
    }


    final boolean
    isBlock() {

	return(type == CONTROL && ((YoixInterfaceBody)value[0]).type() == BLOCK);
    }


    final boolean
    isBreakableError() {

	return(type == JUMP && ((YoixInterfaceBody)value[0]).type() == ERROR && ((YoixBodyJump)value[0]).isBreakable());
    }


    final boolean
    isCalendar() {

	return(value[0] instanceof YoixBodyCalendar);
    }


    final boolean
    isCertificate() {

	return(value[0] instanceof YoixBodyCertificate);
    }


    final boolean
    isCipher() {

	return(value[0] instanceof YoixBodyCipher);
    }


    final boolean
    isClipboard() {

	return(value[0] instanceof YoixBodyClipboard);
    }


    final boolean
    isComponentDrawable() {

	return(isComponent() && ((YoixBodyComponent)value[0]).isDrawable());
    }


    final boolean
    isControl() {

	return(type == CONTROL);
    }


    final boolean
    isDatagramSocket() {

	return(value[0] instanceof YoixBodyDatagramSocket);
    }


    final boolean
    isDrawable() {

	return(isComponentDrawable() || isImage());
    }


    final boolean
    isEmptyString() {

	return(isString() && notNull() && stringValue().length() == 0);
    }


    final boolean
    isFinally() {

	return(type == CONTROL && ((YoixInterfaceBody)value[0]).type() == FINALLY);
    }


    final boolean
    isGraphElement() {

	// i.e., isGraph || isNode || isEdge, not YoixGraphElement

	return(value[0] instanceof YoixBodyElement);
    }


    final boolean
    isGraphObserver() {

	return(value[0] instanceof YoixBodyGraphObserver);
    }


    final boolean
    isGridBagConstraints() {

	return(T_GRIDBAGCONSTRAINTS.equals(getTypename()));
    }


    final boolean
    isGridBagLayout() {

	return(T_GRIDBAGLAYOUT.equals(getTypename()));
    }


    final boolean
    isInsets() {

	return(T_INSETS.equals(getTypename()));
    }


    final boolean
    isJComboBox() {

	return(getManagedObject() instanceof JComboBox);
    }


    final boolean
    isJComponent() {

	return(getManagedObject() instanceof JComponent);
    }


    final boolean
    isJDesktopPane() {

	return(getManagedObject() instanceof JDesktopPane);
    }


    final boolean
    isJInternalFrame() {

	return(getManagedObject() instanceof JInternalFrame);
    }


    final boolean
    isJMenu() {

	return(getManagedObject() instanceof JMenu);
    }


    final boolean
    isJTableColumn() {

	return(T_JTABLECOLUMN.equals(getTypename()));
    }


    final boolean
    isJTextField() {

	return(getManagedObject() instanceof YoixSwingJTextField);
    }


    final boolean
    isJTreeNode() {

	return(T_JTREENODE.equals(getTypename()));
    }


    final boolean
    isKey() {

	return(value[0] instanceof YoixBodyKey);
    }


    final boolean
    isKeyStore() {

	return(value[0] instanceof YoixBodyKeyStore);
    }


    final boolean
    isLocale() {

	return(value[0] instanceof YoixBodyLocale);
    }


    final boolean
    isMenu() {

	return(T_MENU.equals(getTypename()));
    }


    final boolean
    isMulticastSocket() {

	return(value[0] instanceof YoixBodyMulticastSocket);
    }


    final boolean
    isOption() {

	return(value[0] instanceof YoixBodyOption);
    }


    final boolean
    isPath() {

	return(value[0] instanceof YoixBodyPath);
    }


    final boolean
    isPopupMenu() {

	return(isComponent() && ((YoixBodyComponent)value[0]).isPopupMenu());
    }


    final boolean
    isRandom() {

	return(value[0] instanceof YoixBodyRandom);
    }


    final boolean
    isRegexp() {

	return(value[0] instanceof YoixBodyRegexp);
    }


    final boolean
    isScreen() {

	return(value[0] instanceof YoixBodyScreen);
    }


    final boolean
    isSecurityManager() {

	return(value[0] instanceof YoixBodySecurityManager);
    }


    final boolean
    isSubexp() {

	return(value[0] instanceof YoixBodySubexp);
    }


    final boolean
    isTag() {

	return(type == TAG);
    }


    final boolean
    isTransferHandler() {

	return(value[0] instanceof YoixBodyTransferHandler);
    }


    final boolean
    isWindow() {

	return(getManagedObject() instanceof YoixInterfaceWindow);
    }


    final boolean
    isZipEntry() {

	return(value[0] instanceof YoixBodyZipEntry);
    }


    final void
    jump() {

	if (value[0] instanceof YoixBodyJump)
	    ((YoixBodyJump)value[0]).jump();
	else VM.die(INTERNALERROR);
    }


    final void
    jump(YoixObject details) {

	if (value[0] instanceof YoixBodyJump)
	    ((YoixBodyJump)value[0]).jump(details);
	else VM.die(INTERNALERROR);
    }


    final int
    major() {

	return(type());
    }


    final int
    minor() {

	return(((YoixInterfaceBody)value[0]).type());
    }


    final int
    mode() {

	return(flags&MODEMASK);
    }


    final String
    name() {

	return(name(offset()));
    }


    static YoixObject
    newAccess(int access) {

	return(control_template.construct(new YoixBodyControl(ACCESS, access)));
    }


    static YoixObject
    newArray(YoixObject data[]) {

	return(data != null
	    ? pointer_template.construct(new YoixBodyArray(data))
	    : newNullPointer(ARRAY, null)
	);
    }

    static YoixObject
    newArrayBlock(int length) {

	//
	// Eventually may extend YoixBodyArray with an implementation that
	// some of the overhead that's associated with normal arrays. Will
	// only be done if we decide to look for more optimization.
	//

	return(pointer_template.construct(new YoixBodyArray(length)));
    }


    static YoixObject
    newAudioClip() {

	return(newNullPointer(AUDIOCLIP, T_AUDIOCLIP));
    }


    static YoixObject
    newAudioClip(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyAudioClip(data))
	    : newNullPointer(AUDIOCLIP, T_AUDIOCLIP)
	);
    }


    static YoixObject
    newBlockDvalue(YoixObject obj, int index) {

	obj = (YoixObject)obj.clone();
	obj.offset = index;
	return(obj);
    }


    static YoixObject
    newBlockLvalue(YoixObject obj, int index) {

	//
	// Version that creates an lvalue for use by the block code, which
	// means we can omit checks and always set the RESOLVE flag.
	//

	obj = (YoixObject)obj.clone();
	obj.offset = index;
	obj.flags |= RESOLVE;
	return(obj);
    }


    static YoixObject
    newBreakableError(YoixError error) {

	return(jump_template.construct(new YoixBodyJump(ERROR, error, true)));
    }


    static YoixObject
    newBuiltin(String name, int argc, boolean varargs) {

	return(name != null
	    ? callable_template.construct(new YoixBodyBuiltin(name, argc, varargs))
	    : newNullCallable(BUILTIN, T_BUILTIN)
	);
    }


    static YoixObject
    newBuiltin(String name, int argc, boolean varargs, Object extraargs[]) {

	return(name != null
	    ? callable_template.construct(new YoixBodyBuiltin(name, argc, varargs, extraargs))
	    : newNullCallable(BUILTIN, T_BUILTIN)
	);
    }


    static YoixObject
    newCalendar() {

	return(newNullPointer(CALENDAR, T_CALENDAR));
    }


    static YoixObject
    newCalendar(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyCalendar(data))
	    : newNullPointer(CALENDAR, T_CALENDAR)
	);
    }


    static YoixObject
    newCalendar(Calendar calendar) {

	return(newPointer(new YoixBodyCalendar(VM.getTypeTemplate(T_CALENDAR), calendar)));
    }


    static YoixObject
    newCertificate() {

	return(newNullPointer(CERTIFICATE, T_CERTIFICATE));
    }


    static YoixObject
    newCertificate(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyCertificate(data))
	    : newNullPointer(CERTIFICATE, T_CERTIFICATE)
	);
    }


    static YoixObject
    newChar(String image) {

	return(newInt(YoixMake.javaCharacter(image)));
    }


    static YoixObject
    newCheckboxGroup() {

	return(newNullPointer(COMPONENT, T_CHECKBOXGROUP));
    }


    static YoixObject
    newCipher() {

	return(newNullPointer(CIPHER, T_CIPHER));
    }


    static YoixObject
    newCipher(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyCipher(data))
	    : newNullPointer(CIPHER, T_CIPHER)
	);
    }


    static YoixObject
    newClipboard() {

	return(newNullPointer(CLIPBOARD, T_CLIPBOARD));
    }


    static YoixObject
    newClipboard(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyClipboard(data))
	    : newNullPointer(CLIPBOARD, T_CLIPBOARD)
	);
    }


    static YoixObject
    newClipboard(Clipboard clipboard) {

	return(clipboard != null
	    ? newPointer(new YoixBodyClipboard(VM.getTypeTemplate(T_CLIPBOARD), clipboard))
	    : newNullPointer(CLIPBOARD, T_CLIPBOARD)
	);
    }


    static YoixObject
    newCompiler() {

	return(newNullPointer(COMPILER, T_COMPILER));
    }


    static YoixObject
    newCompiler(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyCompiler(data))
	    : newNullPointer(COMPILER, T_COMPILER)
	);
    }


    static YoixObject
    newComponent() {

	return(newNullPointer(COMPONENT, null));
    }


    static YoixObject
    newComponent(String typename) {

	return(newNullPointer(COMPONENT, typename));
    }


    static YoixObject
    newComponent(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyComponentAWT(data))
	    : newNullPointer(COMPONENT, null)
	);
    }


    static YoixObject
    newCookieManager() {

	return(newNullPointer(COOKIEMANAGER, T_COOKIEMANAGER));
    }


    static YoixObject
    newCookieManager(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyCookieManager(data))
	    : newNullPointer(COOKIEMANAGER, T_COOKIEMANAGER)
	);
    }


    static YoixObject
    newDatagramSocket() {

	return(newNullPointer(DATAGRAMSOCKET, T_DATAGRAMSOCKET));
    }


    static YoixObject
    newDatagramSocket(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyDatagramSocket(data))
	    : newNullPointer(DATAGRAMSOCKET, T_DATAGRAMSOCKET)
	);
    }


    static YoixObject
    newDictionary(int length, boolean hashed) {

	return(length >= 0
	    ? pointer_template.construct(new YoixBodyDictionaryObject(length, hashed))
	    : newNullPointer(DICTIONARY, null)
	);
    }


    static YoixObject
    newDictionary(int length, int limit, boolean hashed) {

	YoixObject  obj;

	if (length >= 0) {
	    obj = pointer_template.construct(new YoixBodyDictionaryObject(length, hashed));
	    obj.setGrowable(true);
	    obj.setGrowto(limit);
	} else obj = newNullPointer(DICTIONARY, null);

	return(obj);
    }


    static YoixObject
    newDictionary(YoixObject names, YoixObject values) {

	return(names != null && values != null
	    ? pointer_template.construct(new YoixBodyDictionaryThis(names, values))
	    : newNullPointer(DICTIONARY, null)
	);
    }


    static YoixObject
    newDictionary(String keys[], YoixObject values[], Hashtable keymap) {

	YoixObject  obj = null;

	//
	// Added on 3/1/11 to support YoixBodyComponent.setLayout() changes.
	// Assumes keys, values, and keymap all meet YoixBodyDictionaryObject
	// requirements, so probably shouldn't be used often!!!
	//

	if (keys != null && values != null) {
	    if (keys.length == values.length)
		obj = pointer_template.construct(new YoixBodyDictionaryObject(keys, values, keymap));
	    else VM.abort(INTERNALERROR);
	} else VM.abort(INTERNALERROR);

	return(obj);
    }


    static YoixObject
    newDimension() {

	return(newNull(T_DIMENSION));
    }


    static YoixObject
    newDimension(Dimension dimension) {

	return(dimension != null
	    ? newDimension(dimension.width, dimension.height)
	    : newDimension()
	);
    }


    static YoixObject
    newDimension(double coords[]) {

	return(newDimension(coords[0], coords[1]));
    }


    static YoixObject
    newDimension(double width, double height) {

	YoixObject  obj;

	obj = YoixMake.yoixType(T_DIMENSION);
	obj.putDouble(N_WIDTH, width);
	obj.putDouble(N_HEIGHT, height);
	return(obj);
    }


    static YoixObject
    newElement() {

	return(newNullPointer(ELEMENT, T_ELEMENT));
    }


    static YoixObject
    newElement(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyElement(data))
	    : newNullPointer(ELEMENT, T_ELEMENT)
	);
    }


    static YoixObject
    newElement(YoixBodyElement body) {

	return(body != null
	    ? newPointer(body)
	    : newNullPointer(ELEMENT, T_ELEMENT)
	);
    }


    static YoixObject
    newElement(YoixGraphElement elem) {

	YoixBodyElement  body = null;
	YoixObject       data;

	if (elem != null && (body = elem.getWrapper()) == null) {
	    if (elem.ofType(GRAPH_NODE))
		data = VM.getTypeTemplate(T_NODE);
	    else if (elem.ofType(GRAPH_EDGE))
		data = VM.getTypeTemplate(T_EDGE);
	    else data = VM.getTypeTemplate(T_GRAPH);
	    data.putString(N_NAME, elem.getName());
	    data.putInt(N_FLAGS, elem.getFlags());
	    body = new YoixBodyElement(data, elem);
	}
	return(elem == null ? newElement() : newElement(body));
    }


    static YoixObject
    newFinally(SimpleNode stmt) {

	return(control_template.construct(new YoixBodyControl(FINALLY, stmt)));
    }


    static YoixObject
    newFont() {

	return(newNullPointer(FONT, T_FONT));
    }


    static YoixObject
    newFont(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyFont(data))
	    : newNullPointer(FONT, T_FONT)
	);
    }


    static YoixObject
    newFont(YoixObject data, YoixInterfaceFont font) {

	return(data != null
	    ? pointer_template.construct(new YoixBodyFont(data, font), null)
	    : VM.die(INTERNALERROR)
	);
    }


    static YoixObject
    newForEachBlock(String name, YoixObject value) {

	YoixObject  names;
	YoixObject  values;

	names = YoixObject.newDictionary(1);
	values = YoixObject.newArray(1);
	names.putInt(name, 0);
	values.putObject(0, value);
	values.setAccessBody(LR__);
	return(control_template.construct(new YoixBodyBlock(names, values, null, false, false)));
    }


    static YoixObject
    newFunction(YoixObject names, YoixObject values, SimpleNode tree) {

	return(newFunction(names, values, tree, true));
    }


    static YoixObject
    newFunction(YoixObject names, YoixObject values, SimpleNode tree, boolean varargs) {

	return(tree != null
	    ? callable_template.construct(new YoixBodyFunction(names, values, tree, varargs))
	    : newNullCallable(FUNCTION, T_FUNCTION)
	);
    }


    static YoixObject
    newGlobalBlock(YoixObject names) {

	return(control_template.construct(new YoixBodyBlock(names, names, null, true, true)));
    }


    static YoixObject
    newGlobalBlock(boolean autocreate) {

	return(control_template.construct(new YoixBodyBlock(autocreate ? newDictionary(0, -1) : newDictionary(0), autocreate)));
    }


    static YoixObject
    newGraphics() {

	return(newNullPointer(GRAPHICS, T_GRAPHICS));
    }


    static YoixObject
    newGraphics(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyGraphics(data))
	    : newNullPointer(GRAPHICS, T_GRAPHICS)
	);
    }


    static YoixObject
    newGraphObserver() {

	return(newNullPointer(GRAPHOBSERVER, T_GRAPHOBSERVER));
    }


    static YoixObject
    newGraphObserver(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyGraphObserver(data))
	    : newNullPointer(GRAPHOBSERVER, T_GRAPHOBSERVER)
	);
    }


    static YoixObject
    newHashtable() {

	return(newNullPointer(HASHTABLE, T_HASHTABLE));
    }


    static YoixObject
    newHashtable(int size) {

	YoixObject  data;

	data = VM.getTypeTemplate(T_HASHTABLE);
	data.putInt(N_SIZE, size);
	return(newPointer(new YoixBodyHashtable(data)));
    }


    static YoixObject
    newHashtable(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyHashtable(data))
	    : newNullPointer(HASHTABLE, T_HASHTABLE)
	);
    }


    static YoixObject
    newInsets() {

	return(newNull(T_INSETS));
    }


    static YoixObject
    newInsets(Insets insets) {

	return(newInsets(insets.top, insets.left, insets.bottom, insets.right));
    }


    static YoixObject
    newInsets(double top, double left, double bottom, double right) {

	YoixObject  obj;

	obj = YoixMake.yoixType(T_INSETS);
	obj.putDouble(N_TOP, top);
	obj.putDouble(N_LEFT, left);
	obj.putDouble(N_BOTTOM, bottom);
	obj.putDouble(N_RIGHT, right);
	return(obj);
    }


    static YoixObject
    newJComponent() {

	return(newNullPointer(COMPONENT, null));
    }


    static YoixObject
    newJComponent(String typename) {

	return(newNullPointer(COMPONENT, typename));
    }


    static YoixObject
    newJComponent(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyComponentSwing(data))
	    : newNullPointer(COMPONENT, null)
	);
    }


    static YoixObject
    newJump(int type, YoixError error) {

	return(jump_template.construct(new YoixBodyJump(type, error)));
    }


    static YoixObject
    newKey() {

	return(newNullPointer(KEY, T_KEY));
    }


    static YoixObject
    newKey(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyKey(data))
	    : newNullPointer(KEY, T_KEY)
	);
    }


    static YoixObject
    newKeyStore() {

	return(newNullPointer(KEYSTORE, T_KEYSTORE));
    }


    static YoixObject
    newKeyStore(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyKeyStore(data))
	    : newNullPointer(KEYSTORE, T_KEYSTORE)
	);
    }


    static YoixObject
    newLocale() {

	return(newNullPointer(LOCALE, T_LOCALE));
    }


    static YoixObject
    newLocale(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyLocale(data))
	    : newNullPointer(LOCALE, T_LOCALE)
	);
    }


    static YoixObject
    newLocalBlock(YoixObject names, boolean isthis) {

	YoixObject  values;

	values = newArrayBlock(names.length());
	return(control_template.construct(new YoixBodyBlock(names, values, null, false, isthis)));
    }


    static YoixObject
    newLocalBlock(YoixObject names, YoixObject values, YoixObject tags, boolean isthis) {

	//
	// We probably don't need to check for null values, but we'll leave
	// it in for now - just in case.
	//

	return(values != null
	    ? control_template.construct(new YoixBodyBlock(names, values, tags, false, isthis))
	    : newLocalBlock(names, isthis)
	);
    }


    static YoixObject
    newLocale(Locale locale) {

	return(newPointer(new YoixBodyLocale(VM.getTypeTemplate(T_LOCALE), locale)));
    }


    static YoixObject
    newLvalue(YoixObject obj, int index, boolean resolve) {

	//
	// Duplicates earlier version, but caller can skip setResolve().
	// Added for YoixBodyBlock.newLvalue(). Eventually should check
	// all uses of newLvalue() to see if we should always set the
	// flag in the other routines and toss this one.
	//

	if (obj.value[0] instanceof YoixInterfacePointer) {
	    obj = (YoixObject)obj.clone();
	    obj.offset = index;
	    if (resolve)
		obj.flags |= RESOLVE;
	    else obj.flags &= ~RESOLVE;
	} else VM.abort(TYPECHECK);

	return(obj);
    }


    static YoixObject
    newLvalue(YoixObject obj, String name, boolean resolve) {

	int  index;

	//
	// Duplicates earlier version, but caller can skip setResolve().
	// Added for YoixBodyBlock.newLvalue(). Eventually should check
	// all uses of newLvalue() to see if we should always set the
	// flag in the other routines and toss this one.
	//

	if (obj.value[0] instanceof YoixInterfacePointer) {
	    if ((index = ((YoixInterfacePointer)obj.value[0]).reserve(name)) != -1) {
		obj = (YoixObject)obj.clone();
		obj.offset = index;
		if (resolve)
		    obj.flags |= RESOLVE;
		else obj.flags &= ~RESOLVE;
	    } else VM.abort(UNDEFINED, name);
	} else VM.abort(TYPECHECK);

	return(obj);
    }


    static YoixObject
    newMark() {

	return(mark_object);
    }


    static YoixObject
    newMulticastSocket() {

	return(newNullPointer(MULTICASTSOCKET, T_MULTICASTSOCKET));
    }


    static YoixObject
    newMulticastSocket(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyMulticastSocket(data))
	    : newNullPointer(MULTICASTSOCKET, T_MULTICASTSOCKET)
	);
    }


    static YoixObject
    newNull(int major, int minor, String typename) {

	YoixObject  obj;

	switch (major) {
	    case CALLABLE:
		obj = callable_template.construct(new YoixBodyNull(minor), typename);
		break;

	    case POINTER:
		obj = pointer_template.construct(new YoixBodyNull(minor), typename);
		break;

	    default:
		obj = null;	// cast() needs this - old versions died here
		break;
	}
	return(obj);
    }


    static YoixObject
    newOption() {

	return(newNullPointer(OPTION, T_OPTION));
    }


    static YoixObject
    newOption(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyOption(data))
	    : newNullPointer(OPTION, T_OPTION)
	);
    }


    static YoixObject
    newParseTree(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyParseTree(data))
	    : newNullPointer(PARSETREE, T_PARSETREE)
	);
    }


    static YoixObject
    newPath() {

	return(newNullPointer(PATH, T_PATH));
    }


    static YoixObject
    newPath(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyPath(data))
	    : newNullPointer(PATH, T_PATH)
	);
    }


    static YoixObject
    newPointer(int id, int length, YoixObject data, String classname) {

	YoixObject  obj = null;
	YoixError   error_point = null;
	Throwable   t;
	Method      method;
	Class       owner;

	//
	// Recently modified to first look for a method named newObject()
	// and if that fails look for newPointer(), which is the method
	// name that most older applications will define. Done primarily
	// so custom modules can extend YoixObject, which should make
	// other things much easier. The newPointer() method defined by
	// old custom modules probably should be renamed.
	//

	if (classname != null) {
	    try {
		owner = Class.forName(classname);
		try {
		    method = owner.getMethod(
			"newObject",
			new Class[] {Integer.TYPE, Integer.TYPE, YoixObject.class}
		    );
		}
		catch(NoSuchMethodException e) {
		    //
		    // Fallback for older custom modules - don't delete
		    // this code, but renaming the newPointer() methods
		    // in old modules will eliminate this step.
		    //
		    method = owner.getMethod(
			"newPointer",
			new Class[] {Integer.TYPE, Integer.TYPE, YoixObject.class}
		    );
		}
		try {
		    error_point = VM.pushError();
		    obj = (YoixObject)method.invoke(
			null,
			new Object[] {new Integer(id), new Integer(length), data}
		    );
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
		catch(IllegalAccessException e) {}
	    }
	    catch(RuntimeException e) {}
	    catch(ClassNotFoundException e) {}
	    catch(NoSuchMethodException e) {}
	} else obj = newPointer(id, length, data);

	return(obj);
    }


    static YoixObject
    newProcess() {

	return(newNullPointer(PROCESS, T_PROCESS));
    }


    static YoixObject
    newProcess(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyProcess(data))
	    : newNullPointer(PROCESS, T_PROCESS)
	);
    }


    static YoixObject
    newRandom() {

	return(newNullPointer(RANDOM, T_RANDOM));
    }


    static YoixObject
    newRandom(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyRandom(data))
	    : newNullPointer(RANDOM, T_RANDOM)
	);
    }


    static YoixObject
    newRegexp() {

	return(newNullPointer(REGEXP, T_REGEXP));
    }


    static YoixObject
    newRegexp(String value) {

	YoixObject  data = null;

	if (value != null) {
	    data = VM.getTypeTemplate(T_REGEXP);
	    data.putString(N_PATTERN, YoixMake.javaRegexString(value));
	}
	return(newRegexp(data));
    }


    static YoixObject
    newRegexp(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyRegexp(data))
	    : newNullPointer(REGEXP, T_REGEXP)
	);
    }


    static YoixObject
    newRestore() {

	return(restore_object);
    }


    static YoixObject
    newRestrictedBlock(YoixObject dict) {

	return(control_template.construct(new YoixBodyBlock(dict)));
    }


    static YoixObject
    newScreen() {

	return(newNullPointer(SCREEN, T_SCREEN));
    }


    static YoixObject
    newScreen(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyScreen(data))
	    : newNullPointer(SCREEN, T_SCREEN)
	);
    }


    static YoixObject
    newScreen(GraphicsDevice screen, YoixObject dict) {

	return(newPointer(new YoixBodyScreen(VM.getTypeTemplate(T_SCREEN), screen, dict)));
    }


    static YoixObject
    newSecurityManager() {

	return(newNullPointer(SECURITYMANAGER, T_SECURITYMANAGER));
    }


    static YoixObject
    newSecurityManager(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodySecurityManager(data))
	    : newNullPointer(SECURITYMANAGER, T_SECURITYMANAGER)
	);
    }


    static YoixObject
    newServerSocket() {

	return(newNullPointer(SERVERSOCKET, T_SERVERSOCKET));
    }


    static YoixObject
    newServerSocket(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyServerSocket(data))
	    : newNullPointer(SERVERSOCKET, T_SERVERSOCKET)
	);
    }


    static YoixObject
    newSocket() {

	return(newNullPointer(SOCKET, T_SOCKET));
    }


    static YoixObject
    newSocket(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodySocket(data))
	    : newNullPointer(SOCKET, T_SOCKET)
	);
    }


    static YoixObject
    newSocket(YoixObject data, Socket socket) {

	return(socket != null
	    ? newPointer(new YoixBodySocket(data, socket))
	    : newSocket(data)
	);

    }


    static YoixObject
    newStream(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyStream(data))
	    : newNullPointer(STREAM, T_STREAM)
	);
    }


    static YoixObject
    newStream(YoixObject data, InputStream stream) {

	return(data != null
	    ? pointer_template.construct(new YoixBodyStream(data, stream), null)
	    : VM.die(INTERNALERROR)
	);
    }


    static YoixObject
    newStream(YoixObject data, OutputStream stream) {

	return(data != null
	    ? pointer_template.construct(new YoixBodyStream(data, stream), null)
	    : VM.die(INTERNALERROR)
	);
    }


    static YoixObject
    newStream(YoixObject path, int mode) {

	YoixObject  data;

	switch (YoixMisc.guessStreamType(path.stringValue())) {
	    case FILE:
		data = VM.getTypeTemplate(T_FILE);
		break;

	    case STRINGSTREAM:
		data = VM.getTypeTemplate(T_STRINGSTREAM);
		break;

	    case URL:
		data = VM.getTypeTemplate(T_URL);
		break;

	    default:
		data = VM.abort(UNIMPLEMENTED);
		break;
	}

	data.put(N_NAME, path);
	data.putInt(N_MODE, mode);
	return(newStream(data));
    }


    static YoixObject
    newString(char data[]) {

	//
	// A recent addition (11/8/04) that uses a new YoixBodyString
	// constructor that doesn't make a copy of data, so be a little
	// careful using it!! Was added primarly to help eliminate the
	// overhead of making an extra copy of large files that we just
	// read into memory using readStream().
	//

	return(data != null
	    ? pointer_template.construct(new YoixBodyString(data))
	    : newNullPointer(STRING, null)
	);
    }


    static YoixObject
    newString(String value, boolean escapes, boolean delimiters) {

	if (value != null) {
	    if (delimiters || escapes) {
		if (delimiters) {
		    if (value.startsWith("\""))
			value = YoixMake.javaString(value, escapes, delimiters);
		    else if (value.startsWith("#"))
			value = YoixMake.javaRegexString(value);
		    else if (value.startsWith("0x"))
			value = YoixMake.javaHexString(value);
		    else if (value.startsWith("@<<") && value.endsWith(">>@")) // must be before next one
			value = YoixMake.javaString(value.substring(3, value.length()-3), escapes, false);
		    else if (value.startsWith("@<") && value.endsWith(">@")) // must be after previous one
			value = YoixMake.javaString(value.substring(2, value.length()-2), escapes, false);
		    else value = YoixMake.javaString(value, escapes, delimiters);
		} else value = YoixMake.javaString(value, escapes, delimiters);
	    }
	}

	return(newString(value));
    }


    static YoixObject
    newStringConstant(String value) {

	YoixObject  obj;

	obj = newString(value);
	obj.setAccess(LR__);
	if (obj.notNull())
	    obj.setAccessBody(LR__);

	return(obj);
    }


    static YoixObject
    newSubexp() {

	return(newNullPointer(SUBEXP, T_SUBEXP));
    }


    static YoixObject
    newSubexp(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodySubexp(data))
	    : newNullPointer(SUBEXP, T_SUBEXP)
	);
    }


    static YoixObject
    newTag(int line, int column, String source) {

	return(tag_template.construct(new YoixBodyTag(line, column, source)));
    }


    static YoixObject
    newThread(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyThread(data))
	    : newNullPointer(THREAD, T_THREAD)
	);
    }


    static YoixObject
    newThread(Thread thread) {

	return(newPointer(new YoixBodyThread(VM.getTypeTemplate(T_THREAD), thread)));
    }


    static YoixObject
    newTimeZone() {

	return(newNullPointer(TIMEZONE, T_TIMEZONE));
    }


    static YoixObject
    newTimeZone(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyTimeZone(data))
	    : newNullPointer(TIMEZONE, T_TIMEZONE)
	);
    }


    static YoixObject
    newTimeZone(TimeZone timezone) {

	return(newPointer(new YoixBodyTimeZone(VM.getTypeTemplate(T_TIMEZONE), timezone)));
    }


    static YoixObject
    newTransferHandler() {

	return(newNullPointer(TRANSFERHANDLER, T_TRANSFERHANDLER));
    }


    static YoixObject
    newTransferHandler(String property) {

	YoixObject  data;

	data = VM.getTypeTemplate(T_TRANSFERHANDLER);
	data.putString(N_PROPERTY, property);
	return(newPointer(new YoixBodyTransferHandler(data)));
    }


    static YoixObject
    newTransferHandler(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyTransferHandler(data))
	    : newNullPointer(TRANSFERHANDLER, T_TRANSFERHANDLER)
	);
    }


    static YoixObject
    newTransferHandler(TransferHandler handler) {

	return(handler != null
	    ? newPointer(new YoixBodyTransferHandler(VM.getTypeTemplate(T_TRANSFERHANDLER), handler))
	    : newNullPointer(TRANSFERHANDLER, T_TRANSFERHANDLER)
	);
    }


    static YoixObject
    newUIManager() {

	return(newNullPointer(UIMANAGER, T_UIMANAGER));
    }


    static YoixObject
    newUIManager(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyUIManager(data))
	    : newNullPointer(UIMANAGER, T_UIMANAGER)
	);
    }


    static YoixObject
    newVector() {

	return(newNullPointer(VECTOR, T_VECTOR));
    }


    static YoixObject
    newVector(int size) {

	YoixObject  data;

	data = VM.getTypeTemplate(T_VECTOR);
	data.putInt(N_SIZE, size);
	return(newPointer(new YoixBodyVector(data)));
    }


    static YoixObject
    newVector(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyVector(data))
	    : newNullPointer(VECTOR, T_VECTOR)
	);
    }


    static YoixObject
    newZipEntry() {

	return(newNullPointer(ZIPENTRY, T_ZIPENTRY));
    }


    static YoixObject
    newZipEntry(YoixObject data) {

	return(data != null
	    ? newPointer(new YoixBodyZipEntry(data))
	    : newNullPointer(ZIPENTRY, T_ZIPENTRY)
	);
    }


    static YoixObject
    newZipEntry(ZipEntry zipentry) {

        return(zipentry != null
	    ? newZipEntry(null, zipentry)
	    : newNullPointer(ZIPENTRY, T_ZIPENTRY)
	);
    }


    static YoixObject
    newZipEntry(YoixObject data, ZipEntry zipentry) {

        YoixObject  obj;

        if (data != null)
            obj = newPointer(new YoixBodyZipEntry(data, zipentry));
        else if (zipentry != null) {
            data = VM.getTypeTemplate(T_ZIPENTRY);
            obj = newPointer(new YoixBodyZipEntry(data, zipentry));
        } else obj = newNullPointer(ZIPENTRY, T_ZIPENTRY);

        return(obj);
    }


    final boolean
    notInsets() {

	return(!isInsets());
    }


    final boolean
    notNullPointer() {

	//
	// True if this is a pointer that's not null, which means it's
	// false if you test numbers etc. Name is misleading - perhaps
	// it should change??
	//

	return(isPointer() && notNull());
    }


    final boolean
    open() {

	return(isStream() && ((YoixBodyStream)value[0]).open());
    }


    final void
    overlay(String src) {

	if (src != null)
	    overlay(src, offset(), src.length());
    }


    final void
    overlay(String src, int start, int length) {

	if (isString()) {
	    if (notNull())
		((YoixBodyString)value[0]).overlay(src, start, length);
	    else VM.abort(RANGECHECK);
	} else VM.abort(TYPECHECK);
    }


    final void
    setAccess(int perm) {

	if (canUnlock())
	    flags = (short)((flags&(~ACCESSMASK)) | (perm&ACCESSMASK));
	else if ((perm&ACCESSMASK) != (flags&ACCESSMASK))
	    VM.abort(INVALIDACCESS);
    }


    final void
    setMode(int mode) {

	flags = (short)(mode&MODEMASK);
    }


    final void
    setModeBits(int mode) {

	flags |= (short)(mode&MODEMASK);
    }


    final void
    setResolve(boolean state) {

	if (isPointer()) {
	    if (state)
		flags |= RESOLVE;
	    else flags &= ~RESOLVE;
	} else VM.abort(TYPECHECK);
    }


    final void
    setUnroll(boolean state) {

	if (isPointer()) {
	    if (state)
		flags |= UNROLL;
	    else flags &= ~UNROLL;
	} else VM.abort(TYPECHECK);
    }


    final int[]
    sortedOrder() {

	return(value[0] instanceof YoixBodyDictionary
	    ? ((YoixBodyDictionary)value[0]).sortedOrder()
	    : null
	);
    }


    final YoixBodyStream
    streamValue() {

	YoixBodyStream  stream = null;

	if (isNull())
	    stream = null;
	else if (isStream())
	    stream = (YoixBodyStream)value[0];
	else VM.abort(TYPECHECK);

	return(stream);
    }


    final boolean
    writable(String name) {

	YoixObject  obj;
	boolean     result;

	//
	// Added quickly and without much thought. Currently only used
	// in a few places, so adding it to YoixInterfacePointer seems
	// like overkill.
	//

	if (result = canWriteBody()) {
	    if (defined(name)) {
		VM.pushAccess(R__);
		result = get(name, false).canWrite();
		VM.popAccess();
	    } else result = (hash(name) >= 0 || canGrowTo(length() + 1));
	}

	return(result);
    }


    final int
    write(String str) {

	if (isStream())
	    return(((YoixBodyStream)value[0]).write(str));
	else VM.abort(TYPECHECK);

	return(YOIX_EOF);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    constructCachedNull(String typename) {

	YoixObject  obj;

	//
	// This should only be used by the protected constructor that
	// subclasses can call when they want to create a null object
	// that the interpreter will like but that's also an instance
	// of the subclass. Not completely cinvinced by this.
	//

	if (value[0] == null && typename != null && getTypename() == typename) {
	    if (VM.isYoixTypename(typename)) {
		if ((obj = (YoixObject)nullcache.get(typename)) == null) {
		    obj = YoixMake.yoixType(typename);
		    if ((obj = newNull(obj.major(), obj.minor(), typename)) != null) {
			value[0] = obj.value[0];
			flags = obj.flags;
			nullcache.put(typename, this);
		    } else VM.abort(INVALIDACCESS);
		} else value[0] = obj.value[0];
	    } else VM.abort(BADTYPENAME);
	} else VM.abort(INVALIDACCESS);
    }


    private static YoixObject
    getCachedNull(String typename, boolean create) {

	YoixObject  obj;

	//
	// Prior to 6/17/02 the second argument in the newNull() call
	// was,
	//
	//	obj.getInt(N_MAJOR, obj.minor())
	//
	// but that looked like a mistake and really didn't work well
	// with our new Swing implementation. Not 100% certain, which
	// is why these comments are here, but the change seems OK.
	//

	if ((obj = (YoixObject)nullcache.get(typename)) == null && create) {
	    if (VM.isYoixTypename(typename)) {
		obj = YoixMake.yoixType(typename);
		if ((obj = newNull(obj.major(), obj.minor(), typename)) != null)
		    nullcache.put(typename, obj);
	    }
	}
	return(obj != null ? (YoixObject)obj.clone() : obj);
    }


    private static YoixObject
    newNullCallable(int minor, String typename) {

	return(callable_template.construct(new YoixBodyNull(minor)));
    }


    private static YoixObject
    newNullPointer(int minor, String typename) {

	return(pointer_template.construct(new YoixBodyNull(minor), typename));
    }


    private static YoixObject
    newPointer(int id, int length, YoixObject data) {

	YoixObject  obj;

	switch (id) {
	    case ARRAY:
		obj = newArray(length);
		break;

	    case AUDIOCLIP:
		obj = newAudioClip(data);
		break;

	    case CALENDAR:
		obj = newCalendar(data);
		break;

	    case CERTIFICATE:
		obj = newCertificate(data);
		break;

	    case CIPHER:
		obj = newCipher(data);
		break;

	    case CLIPBOARD:
		obj = newClipboard(data);
		break;

	    case COMPILER:
		obj = newCompiler(data);
		break;

	    case COMPONENT:
		obj = newComponent(data);
		break;

	    case COOKIEMANAGER:
		obj = newCookieManager(data);
		break;

	    case DATAGRAMSOCKET:
		obj = newDatagramSocket(data);
		break;

	    case DICTIONARY:
		obj = newDictionary(length);
		break;

	    case ELEMENT:
		obj = newElement(data);
		break;

	    case FONT:
		obj = newFont(data);
		break;

	    case GRAPHICS:
		obj = newGraphics(data);
		break;

	    case GRAPHOBSERVER:
		obj = newGraphObserver(data);
		break;

	    case HASHTABLE:
		obj = newHashtable(data);
		break;

	    case IMAGE:
		obj = newImage(data);
		break;

	    case JCOMPONENT:
		obj = newJComponent(data);
		break;

	    case KEY:
		obj = newKey(data);
		break;

	    case KEYSTORE:
		obj = newKeyStore(data);
		break;

	    case LOCALE:
		obj = newLocale(data);
		break;

	    case MATRIX:
		obj = newMatrix(data);
		break;

	    case MULTICASTSOCKET:
		obj = newMulticastSocket(data);
		break;

	    case OPTION:
		obj = newOption(data);
		break;

	    case PARSETREE:
		obj = newParseTree(data);
		break;

	    case PATH:
		obj = newPath(data);
		break;

	    case PROCESS:
		obj = newProcess(data);
		break;

	    case RANDOM:
		obj = newRandom(data);
		break;

	    case REGEXP:
		obj = newRegexp(data);
		break;

	    case SCREEN:
		obj = newScreen(data);
		break;

	    case SECURITYMANAGER:
		obj = newSecurityManager(data);
		break;

	    case SERVERSOCKET:
		obj = newServerSocket(data);
		break;

	    case SOCKET:
		obj = newSocket(data);
		break;

	    case STREAM:
		obj = newStream(data);
		break;

	    case STRING:
		obj = newString(length);
		break;

	    case SUBEXP:
		obj = newSubexp(data);
		break;

	    case THREAD:
		obj = newThread(data);
		break;

	    case TIMEZONE:
		obj = newTimeZone(data);
		break;

	    case TRANSFERHANDLER:
		obj = newTransferHandler(data);
		break;

	    case UIMANAGER:
		obj = newUIManager(data);
		break;

	    case VECTOR:
		obj = newVector(data);
		break;

	    case ZIPENTRY:
		obj = newZipEntry(data);
		break;

	    default:		// sure looks like a kludge??
		obj = newArray(length);
		break;
	}

	return(obj);
    }
}

