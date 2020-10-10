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
class YoixBodyDictionaryObject extends YoixBodyDictionary

    implements YoixInterfaceCloneable

{

    //
    // This is the standard implementation of dictionaries that are used
    // to represent the dictionaries declared by Yoix programs and modules
    // loaded by the interpreter.
    //
    // NOTE - we addressed some thread related problems that occasionally
    // showed up on heavily loaded production systems, so some of the low
    // level code in this class has been changed (around 4/1/07). There's
    // a chance some of the old code can be restored, but we probably need
    // to test on a heavily loaded production system first - so be careful
    // if you decide to make changes in this class. Things that you might
    // be able to restore are
    //
    //    1: Replace keymap with a HashMap
    //    2: Restore the unsynchronized version of getSlot()
    //    3: Peek at data[n] rather than using obj.dataAt(n) in the if
    //       test in copy(). This one likely will need lots of testing
    //       and ultimately could need some synchronization elsewhere
    //       in the interpreter.
    //
    // but remember to test thoroughly!!!!
    //

    private YoixObject  data[];
    private Hashtable   keymap;
    private boolean     hashed;
    private String      keys[];
    private int         lastindex = -1;

    //
    // Not used much and probably safest to leave it a Hashtable.
    //

    private Hashtable   modulemap;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyDictionaryObject(int length) {

	this(length, true);
    }


    YoixBodyDictionaryObject(int length, boolean hashed) {

	this.keys = new String[length];
	this.data = new YoixObject[length];
	this.length = length;
	this.hashed = hashed;
	keymap = (hashed || length == 0) ? null : new Hashtable(length);
    }


    YoixBodyDictionaryObject(String keys[], YoixObject values[], Hashtable keymap) {

	//
	// Assumes the caller has built keys, values, and keymap according
	// to our requirements, so this contructor should rarely be used!!
	// It was added on 3/1/11 in an attempt to squeeze a little better
	// performance out of YoixBodyComponent.setLayout() when it needed
	// to update the components dictionary of other containers.
	//

	this.keys = keys;
	this.data = values;
	this.keymap = keymap;
	this.length = values.length;
	this.hashed = (keymap != null);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    length() {

	return(length);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public final synchronized Object
    clone() {

	YoixBodyDictionaryObject  obj;

	try {
	    obj = (YoixBodyDictionaryObject)super.clone();
	    if (obj.data != null) {
		obj.keys = new String[obj.length];
		obj.data = new YoixObject[obj.length];
		System.arraycopy(keys, 0, obj.keys, 0, obj.length);
		System.arraycopy(data, 0, obj.data, 0, obj.length);
	    }
	    if (keymap != null)
		obj.keymap = (Hashtable)keymap.clone();
	    if (modulemap != null)
		obj.modulemap = (Hashtable)modulemap.clone();
	}
	catch(CloneNotSupportedException e) {
	    VM.die(INTERNALERROR);
	    obj = null;
	}

	return(obj);
    }


    public final Object
    copy(HashMap copied) {

	YoixBodyDictionaryObject  obj;
	int                       n;

	//
	// Be very careful if you make changes here. Old versions could
	// trigger some obscure thread related problems that might only
	// show up on heavily loaded systems. The explicit obj.dataAt(n)
	// in the if test is curretly intentional even though might not
	// be necessary and will force some module loading earlier than
	// you might expect (i.e., when typedict is duplicated). It's a
	// change that was in the version that we tested on a production
	// system with lots of users. The old version did
	//
	//	if (obj.data[n] != null)
	//	    ...
	//
	// which postponed module loading, but we probably won't restore
	// the code without thorough testing on a heavily loaded system.
	//
	// NOTE - the old version duplicated and replaced data[] rather
	// than obj.data, which clearly was a mistake that probably was
	// responsible for the thread related problems that we saw.
	// 

	obj = (YoixBodyDictionaryObject)clone();

	for (n = 0; n < obj.length; n++) {
	    if (obj.dataAt(n) != null)
		obj.data[n] = obj.dataAt(n).duplicate(copied);
	}

	return(obj);
    }

    ///////////////////////////////////
    //
    // YoixInterfacePointer Methods
    //
    ///////////////////////////////////

    public final synchronized YoixObject
    cast(YoixObject obj, int index, boolean clone) {

	YoixObject  dest;

	if (canWrite()) {
	    if (inRange(index)) {
		if (keys[index] != null) {
		    if ((dest = dataAt(index)) == null || dest.canWrite()) {
			if ((obj = obj.cast(dest, clone)) == null)
			    VM.abort(TYPECHECK, keys[index]);
		    } else VM.abort(INVALIDACCESS, keys[index]);
		} else VM.abort(UNDEFINED, index);
	    } else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

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


    public final synchronized void
    declare(int index, YoixObject obj, int mode) {

	if (canWrite()) {
	    if (inRange(index)) {
		if (keys[index] != null) {
		    if (dataAt(index) == null) {
			obj.setMode(mode);
			data[index] = obj;
		    } else VM.abort(REDECLARATION, keys[index]);
		} else VM.abort(UNDEFINED, index);
	    } else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);
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

	return(canRead() && inRange(index) && dataAt(index) != null);
    }


    public final boolean
    defined(String name) {

	return(defined(hash(name)));
    }


    public final String
    dump(int index, String indent, String typename) {

	YoixObject  obj;
	String      str;
	int         sorted[];
	int         level;
	int         limit;
	int         size;
	int         n;
	int         m;

	sorted = sortedOrder();
	size = sorted.length;
	level = indent.length()/4;
	limit = VM.getInt(N_DUMPDEPTH);
	indent += "   ";
	str = (typename == null) ? T_DICT : typename;
	str += "[" + size + ":" + index + "]";

	if (canRead()) {
	   if (level < limit) {
		str += NL;
		for (n = 0; n < size; n++) {
		    m = sorted[n];
		    str += indent + ((m == index) ? ">" : " ");
		    if (keys[m] != null) {
			str += keys[m] + "=";
			if ((obj = dataAt(m)) != null) {
			    if (obj.canRead())
				str += obj.dump(indent + " ");
			    else if (obj.canExecute())
				str += "--executeonly--" + NL;
			    else str += "--unreadable--" + NL;
			} else str += "--undefined--" + NL;
		    } else str += "--uninitialized--" + NL;
		}
	    } else str += NL;
	} else str += ":--unreadable--" + NL;

	return(str);
    }


    public final boolean
    executable(int index) {

	return(canExecute() && inRange(index) && dataAt(index) != null && dataAt(index).canExecute());
    }


    public final boolean
    executable(String name) {

	return(executable(hash(name)));
    }


    public final YoixObject
    execute(int index, YoixObject argv[], YoixObject context) {

	YoixObject  obj = null;
	YoixObject  dest;

	if (canExecute()) {
	    if (inRange(index)) {
		if (keys[index] != null) {
		    if ((dest = dataAt(index)) != null) {
			if (dest.canExecute())
			    obj = dest.call(argv, context);
			else VM.abort(INVALIDACCESS, keys[index]);
		    } else VM.abort(UNDEFINED, keys[index]);
		} else VM.abort(UNDEFINED, index);
	    } else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

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

	YoixObject  obj = null;
	YoixObject  dest;

	if (canRead()) {
	    if (inRange(index)) {
		if (keys[index] != null) {
		    if ((dest = dataAt(index)) != null) {
			if (dest.canRead())
			    obj = clone ? (YoixObject)dest.clone() : dest;
			else VM.abort(INVALIDACCESS, keys[index]);
		    } else VM.abort(UNDEFINED, keys[index]);
		} else VM.abort(UNDEFINED, index);
	    } else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final YoixObject
    get(String name, boolean clone) {

	YoixObject  obj = null;
	int         index;

	if ((index = hash(name)) != -1)
	    obj = get(index, clone);	// faster if we did it here??
	else VM.abort(UNDEFINED, name);

	return(obj);
    }


    public final int
    hash(String name) {

	Integer  slot;
	int      index;
	int      size;
	int      entries;
	int      n;

	//
	// Returns the index where the definition of name belongs or -1
	// if it's not defined and there's no room left. lastindex is a
	// recent addition that seems to help, but it's a class variable
	// that can be set by other threads, so be very careful. Even an
	// innocent looking change like,
	//
	//		index = lastindex = n;
	//
	// could cause trouble. The safest approach would write this as
	// two separate assignment statements,
	//
	//		index = n;
	//		lastindex = n;
	//
	// so there would be no chance of obscure thread accidents. There
	// are many other obvious changes - later.
	//

	if ((index = lastindex) == -1 || name.equals(keys[index]) == false) {
	    if ((size = length) > 0) {
		if (keymap == null || (slot = getSlot(name)) == null) {
		    index = -1;
		    n = ((hashed ? name.hashCode() : lastindex+1) & 0x7FFFFFFF) % size;
		    for (entries = size; entries-- > 0; ) {
			if (keys[n] == null || name.equals(keys[n])) {
			    index = n;
			    lastindex = index;
			    break;
			}
			n = (n + 1) % size;
		    }
		} else {
		    index = slot.intValue();
		    lastindex = index;
		}
	    } else index = -1;
	}
	return(index);
    }


    public final String
    name(int index) {

	return(defined(index) ? keys[index] : null);
    }


    public final synchronized YoixObject
    put(int index, YoixObject obj, boolean clone) {

	YoixObject  dest;

	if (canWrite()) {
	    if (inRange(index)) {
		if (keys[index] != null) {
		    if ((dest = dataAt(index)) == null || dest.canWrite()) {
			if ((obj = obj.cast(dest, clone)) != null) {
			    obj.setMode(dest != null
				? dest.mode()
				: RWX|(obj.mode()&ANYMASK)
			    );
			    data[index] = obj;
			} else VM.abort(TYPECHECK, keys[index]);
		    } else VM.abort(INVALIDACCESS, keys[index]);
		} else VM.abort(UNDEFINED, index);
	    } else VM.abort(RANGECHECK, index);
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

	return(defined(index) && dataAt(index).canRead());
    }


    public final boolean
    readable(String name) {

	return(readable(hash(name)));
    }


    public final synchronized int
    reserve(String name) {

	int  index;

	if ((index = hash(name)) != -1 || (growBy(1) && (index = hash(name)) != -1)) {
	    if (keys[index] == null) {
		if (canWrite()) {
		    keys[index] = name;
		    if (keymap != null)
			keymap.put(keys[index], new Integer(index));
		} else VM.abort(INVALIDACCESS);
	    }
	}

	return(index);
    }

    ///////////////////////////////////
    //
    // YoixBodyDictionaryObject Methods
    //
    ///////////////////////////////////

    final synchronized YoixObject
    activateField(String name) {

	YoixObject  value = null;
	int         index;

	//
	// Low level method that should only be used during startup, so
	// don't get any ideas!!
	//

	if ((index = hash(name)) != -1) {
	    if ((value = data[index]) != null) {
		data[index] = null;
		setModuleClass(name, "");	// magic to force VM.load() call
	    }
	}
	return(value);
    }


    final boolean
    existsAt(int index) {

	//
	// Don't use this unless you know exactly why you need it. It's
	// currently only used in one place, and only to prevent types
	// from being automatically loaded as we look through typedict
	// for objects that represent primitive types.
	//

	return(inRange(index) && data[index] != null);
    }


    final synchronized String[]
    getKeys() {

	return(keys != null ? (String[])keys.clone() : null);
    }


    final synchronized YoixObject[]
    getValues() {

	return(data != null ? (YoixObject[])data.clone() : null);
    }


    final String
    moduleNameAt(int index) {

	String  classname = null;

	//
	// Once again, don't use this unless you know exactly why you
	// need it.
	//

	if (inRange(index)) {
	    if (keys[index] != null && modulemap != null) {
		synchronized(this) {
		    classname = (String)modulemap.get(keys[index]);
		}
	    }
	}
	return(classname);
    }


    final String
    nameAt(int index) {

	return(inRange(index) ? keys[index] : null);
    }


    final YoixObject
    peekAt(int index) {

	//
	// Fast but omits most checks, particularly permissions, so it
	// should only be used for low level support when you're trying
	// to optimize performance by eliminating pushAccess/popAccess
	// calls.
	//

	return(inRange(index) ? dataAt(index) : null);
    }


    final YoixObject
    peekAt(String name) {

	//
	// Fast but omits most checks, particularly permissions, so it
	// should only be used for low level support when you're trying
	// to optimize performance by eliminating pushAccess/popAccess
	// calls. For example, typedict lookups are a particularly good
	// example.
	//

	return(peekAt(hash(name)));
    }


    final synchronized void
    setModuleClass(String name, String classname) {

	if (modulemap == null)
	    modulemap = new Hashtable();
	modulemap.put(name, classname);
    }


    final synchronized void
    setModulePackage(String packagename) {

	if (modulemap == null)
	    modulemap = new Hashtable();
	modulemap.put("", packagename);
    }


    final int[]
    sortedOrder() {

	String  nkeys[];

	nkeys = new String[keys.length];
	System.arraycopy(keys, 0, nkeys, 0, nkeys.length);
	return(YoixMiscQsort.qsort(nkeys, 1));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixObject
    dataAt(int index) {

	YoixObject  obj;
	String      list[];
	String      classname;
	String      fieldname;
	String      packagename;
	int         mode;

	if ((obj = data[index]) == null && modulemap != null) {
	    synchronized(this) {
		if ((obj = data[index]) == null) {		// just in case
		    if ((fieldname = keys[index]) != null) {
			if ((classname = (String)modulemap.get(fieldname)) != null) {
			    modulemap.remove(fieldname);
			    if (classname.length() > 0)
				data[index] = YoixModule.get(classname, fieldname);
			    else data[index] = VM.load(fieldname);
			} else {
			    if ((packagename = (String)modulemap.get("")) != null) {
				list = getClassList(packagename, fieldname);
				if ((classname = YoixModule.load(list)) == null) {
				    obj = YoixObject.newDictionary(0);
				    obj.setGrowable(true);
				    obj.setMode(LR__);
				    ((YoixBodyDictionaryObject)obj.body()).setModulePackage(list[0]);
				    data[index] = obj;
				} else data[index] = YoixModule.get(classname, fieldname);
			    }
			}
		    }
		    obj = data[index];
		}
	    }
	}
	return(obj);
    }


    private String[]
    getClassList(String packagename, String fieldname) {

	String  names[];
	char    letters[];

	if (packagename != null && packagename.length() > 0) {
	    if (packagename.startsWith(YOIXPACKAGE)) {
		letters = fieldname.toCharArray();
		letters[0] = Character.toUpperCase(letters[0]);
		names = new String[3];
		names[0] = packagename + "." + fieldname;
		names[1] = packagename + "." + "YoixModule" + new String(letters);
		names[2] = packagename + "." + fieldname + "." + "Module";
	    } else names = new String[] {packagename + "." + fieldname};
	} else names = new String[] {fieldname};

	return(names);
    }


    private synchronized Integer
    getSlot(String name) {

	return((Integer)keymap.get(name));
    }


    private synchronized boolean
    growBy(int increment) {

	YoixObject  ndata[];
	Hashtable   nkeymap;
	String      nkeys[];
	int         nlength;
	int         n;

	if ((nlength = length + increment) > length) {
	    if (canGrowTo(nlength)) {
		nkeys = new String[nlength];
		ndata = new YoixObject[nlength];
		System.arraycopy(keys, 0, nkeys, 0, length);
		System.arraycopy(data, 0, ndata, 0, length);

		if (keymap == null) {
		    nkeymap = new Hashtable(nlength);
		    for (n = 0; n < length; n++) {
			if (keys[n] != null)
			    nkeymap.put(keys[n], new Integer(n));
		    }
		} else nkeymap = keymap;

		keymap = nkeymap;
		data = ndata;
		keys = nkeys;
		length = nlength;
	    }
	}

	return(length >= nlength);
    }
}

