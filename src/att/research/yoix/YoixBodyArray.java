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
class YoixBodyArray extends YoixPointer

    implements YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCloneable

{

    //
    // This class is also used for local block storage, but in that case
    // much checking overhead probably could be eliminated with careful
    // programming. This is good enough for now, but we eventually will
    // work on our block and array code.
    //

    private YoixObject  data[];

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyArray(int length) {

	this.data = new YoixObject[length];
	this.length = length;
    }


    YoixBodyArray(YoixObject data[]) {

	this.length = (data != null) ? data.length : 0;
	this.data = new YoixObject[this.length];
	if (data != null)
	    System.arraycopy(data, 0, this.data, 0, this.length);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public final synchronized Object
    clone() {

	YoixBodyArray  obj;

	try {
	    obj = (YoixBodyArray)super.clone();
	    if (obj.data != null) {
		obj.data = new YoixObject[obj.length];
		System.arraycopy(data, 0, obj.data, 0, obj.length);
	    }
	}
	catch(CloneNotSupportedException e) {
	    VM.die(INTERNALERROR);
	    obj = null;
	}

	return(obj);
    }


    public final Object
    copy(HashMap copied) {

	YoixBodyArray  obj;
	int            n;

	obj = (YoixBodyArray)clone();

	for (n = 0; n < obj.length; n++) {
	    if (obj.data[n] != null)
		obj.data[n] = obj.data[n].duplicate(copied);
	}

	return(obj);
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

	return(length);
    }


    public final String
    toString() {

	return(dump());
    }


    public final int
    type() {

	return(ARRAY);
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
	    if (inRange(index) || (growTo(index + 1) && inRange(index))) {
		if ((dest = data[index]) == null || dest.canWrite()) {
		    if ((obj = obj.cast(dest, clone)) == null)
			abort(TYPECHECK, index);
		} else abort(INVALIDACCESS, index);
	    } else abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final YoixObject
    cast(YoixObject obj, String name, boolean clone) {

	return(cast(obj, hash(name), clone));
    }


    public final boolean
    compound() {

	return(false);
    }


    public final synchronized void
    declare(int index, YoixObject obj, int mode) {

	if (canWrite()) {
	    if (inRange(index) || (growTo(index + 1) && inRange(index))) {
		if (data[index] == null) {
		    obj.setMode(mode);
		    data[index] = obj;
		} else abort(REDECLARATION, index);
	    } else abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);
    }


    public final void
    declare(String name, YoixObject obj, int mode) {

	declare(reserve(name), obj, mode);
    }


    public final boolean
    defined(int index) {

	return(canRead() && inRange(index) && data[index] != null);
    }


    public final boolean
    defined(String name) {

	return(defined(hash(name)));
    }


    public final String
    dump(int index, String indent, String typename) {

	YoixObject  obj;
	String      str;
	int         level;
	int         limit;
	int         size;
	int         n;

	size = length;
	limit = VM.getInt(N_DUMPDEPTH);
	level = indent.length()/4;
	indent += "   ";
	str = (typename == null) ? T_ARRAY : typename;
	str += "[" + size + ":" + index + "]";

	if (canRead()) {
	   if (level < limit) {
		str += NL;
		for (n = 0; n < size; n++) {
		    str += indent + ((n == index) ? ">" : " ");
		    if ((obj = data[n]) != null) {
			if (obj.canRead())
			    str += obj.dump(indent + " ");
			else if (obj.canExecute())
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

	return(canExecute() && inRange(index) && data[index] != null && data[index].canExecute());
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
		if ((dest = data[index]) != null) {
		    if (dest.canExecute())
			obj = dest.call(argv, context);
		    else abort(INVALIDACCESS, index);
		} else abort(UNDEFINED, index);
	    } else abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final YoixObject
    execute(String name, YoixObject argv[], YoixObject context) {

	return(execute(hash(name), argv, context));
    }


    public final YoixObject
    get(int index, boolean clone) {

	YoixObject  obj = null;
	YoixObject  dest;

	if (canRead()) {
	    if (inRange(index)) {
		if ((dest = data[index]) != null) {
		    if (dest.canRead())
			obj = clone ? (YoixObject)dest.clone() : dest;
		    else abort(INVALIDACCESS, index);
		} else abort(UNDEFINED, index);
	    } else abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final YoixObject
    get(String name, boolean clone) {

	return(get(hash(name), clone));
    }


    public final int
    hash(String name) {

	int  index = YoixMake.javaInt(name);

	return(inRange(index) ? index : -1);
    }


    public final String
    name(int index) {

	return(defined(index) ? (index + "") : null);
    }


    public final synchronized YoixObject
    put(int index, YoixObject obj, boolean clone) {

	YoixObject  dest;

	if (canWrite()) {
	    if (inRange(index) || (growTo(index + 1) && inRange(index))) {
		if ((dest = data[index]) == null || dest.canWrite()) {
		    if ((obj = obj.cast(dest, clone)) != null) {
			obj.setMode(dest != null
			    ? dest.mode()
			    : RWX|(obj.mode()&ANYMASK)
			);
			data[index] = obj;
		    } else abort(TYPECHECK, index);
		} else abort(INVALIDACCESS, index);
	    } else abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final YoixObject
    put(String name, YoixObject obj, boolean clone) {

	return(put(reserve(name), obj, clone));
    }


    public final boolean
    readable(int index) {

	return(defined(index) && data[index].canRead());
    }


    public final boolean
    readable(String name) {

	return(readable(hash(name)));
    }


    public final int
    reserve(String name) {

	int  index;

	if ((index = hash(name)) == -1) {
	    if ((index = YoixMake.javaInt(name)) >= 0) {
		if (growTo(index + 1) == false)
		    index = -1;
	    }
	}

	return(index);
    }

    ///////////////////////////////////
    //
    // YoixBodyArray Methods
    //
    ///////////////////////////////////

    final synchronized boolean
    growTo(int nlength) {

	YoixObject  ndata[];

	//
	// Was private, but YoixBodyFuntion.call() wanted a way to grow
	// the argument array (when necessary) so storeArgument() could
	// safely skip that job.
	//

	if (nlength > length) {
	    if (canGrowTo(nlength)) {
		ndata = new YoixObject[nlength];
		System.arraycopy(data, 0, ndata, 0, length);
		data = ndata;
		length = nlength;
	    }
	}

	return(length >= nlength);
    }


    final YoixObject
    storeArgument(YoixObject obj, int index) {

	YoixObject  dest;

	//
	// A stripped down version of put() that should only be used to
	// store function arguments in the array that holds their values.
	// A null return means something went wrong and the caller (i.e.,
	// YoixBodyFuntion.call()) is supposed to issue an error message.
	// Overriding the aborts done in put() so we could issue improved
	// error messages was the initial justification for this method.
	// We then decided we could safely eliminate most error checking,
	// which helped function call performance.
	//

	if ((dest = data[index]) != null) {
	    if ((obj = obj.cast(dest, false)) != null) {
		obj.setMode(dest.mode());
		data[index] = obj;
	    }
	} else {
	    obj.setMode(RWX|(obj.mode()&ANYMASK));
	    data[index] = obj;
	}
	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    abort(String error, int index) {

	String  name;

	//
	// This should improve error messages when this array is used
	// to store local variables in a block (e.g., arguments in a
	// function call).
	//

	if ((name = YoixBodyBlock.getBlockName(this, index)) != null)
	    VM.abort(error, name);
	else VM.abort(error, index);
    }
}

