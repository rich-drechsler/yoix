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
class YoixBodyDictionaryThis extends YoixBodyDictionary

    implements YoixInterfaceCloneable

{

    //
    // A new class (added 8/24/05) that expands our handling of "this". Our
    // previous implementations of "this" only worked when the block used a
    // dictionary to store names and values. It was a simplification that
    // let us build lvalues when the condition was met, but didn't let us
    // handle the usual case where a block used a separate dictionary and
    // array to manage local storage.
    //
    // Incidentally, we can safely assume that
    //
    //		names.length() <= values.length()
    //
    // which is why we use values in length(). In fact the only time when
    // the sizes will be different is for the block that's associated with
    // the arguments of a varargs function.
    // 

    private YoixObject  names;
    private YoixObject  values;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyDictionaryThis(YoixObject names, YoixObject values) {

	this.names = names;
	this.values = values;
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

    public final int
    length() {

	return(values.length());
    }

    ///////////////////////////////////
    //
    // YoixInterfacePointer Methods
    //
    ///////////////////////////////////

    public final YoixObject
    cast(YoixObject obj, int index, boolean clone) {

	return(values.cast(obj, index, clone));
    }

    
    public final YoixObject
    cast(YoixObject obj, String name, boolean clone) {

	int      index;

	if ((index = names.definedAt(name)) != -1) {
	    if (values.isArray())
		index = names.get(index, false).intValue();
	    obj = cast(obj, index, clone);
	} else VM.abort(UNDEFINED, name);
	return(obj);
    }


    public final boolean
    compound() {

	return(true);
    }


    public final void
    declare(int index, YoixObject obj, int mode) {

	//
	// We don't allow declarations in dictionaires that are used to
	// represent block storage. Seems reasonable so it probably won't
	// be changed.
	//

	VM.abort(DICTFULL, index);
    }


    public final void
    declare(String name, YoixObject obj, int mode) {

	//
	// We don't allow declarations in dictionaires that are used to
	// represent block storage. Seems reasonable so it probably won't
	// be changed.
	//

	VM.abort(DICTFULL, name);
    }


    public final boolean
    defined(int index) {

	return(values.defined(index));
    }


    public final boolean
    defined(String name) {

	boolean  result = false;
	int      index;

	if ((index = names.definedAt(name)) != -1) {
	    if (values.isArray())
		index = names.get(index, false).intValue();
	    result = defined(index);
	}
	return(result);
    }


    public final String
    dump(int index, String indent, String typename) {
    
	YoixObject  obj;
	String      str;
	String      key;
	int         sorted[];
	int         level;
	int         limit;
	int         size;
	int         n;
	int         m;

	if (values.isArray()) {
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
			if ((key = names.name(m)) != null) {
			    str += key + "=";
			    if ((obj = values.getObject(names.get(m, false).intValue())) != null) {
				if (obj.canRead())
				    str += obj.dump(indent + " ");
				else if (obj.canExecute())
				    str += "--executeonly--" + NL;
				else str += "--unreadable--" + NL;
			    } else str += "--undefined--" + NL;
			} else str += "--uninitialized--" + NL;
		    }
		    //
		    // A block associated with the arguments of a varargs
		    // may have left-over values that haven't been printed
		    // yet. We dump them like an array (a little confusing)
		    // assuming they occupy the remaining slots in values.
		    //
		    size = values.length();
		    for (; n < size; n++) {
			str += indent + ((n == index) ? ">" : " ");
			if ((obj = values.getObject(n)) != null) {
			    if (obj.canRead())
				str += obj.dump(indent + " ");
			    else if (obj.canExecute())
				str += "--executeonly--" + NL;
			    else str += "--unreadable--" + NL;
			} str += "--uninitialized--" + NL;
		    }
		} else str += NL;
	    } else str += ":--unreadable--" + NL;
	} else str = values.dump(index, indent, typename);
	return(str);
    }


    public final boolean
    executable(int index) {

	return(values.executable(index));
    }


    public final boolean
    executable(String name) {

	boolean  result = false;
	int      index;

	if ((index = names.definedAt(name)) != -1) {
	    if (values.isArray())
		index = names.get(index, false).intValue();
	    result = executable(index);
	}
	return(result);
    }


    public final YoixObject
    execute(int index, YoixObject argv[], YoixObject context) {

	return(values.execute(index, argv, context));
    }


    public final YoixObject
    execute(String name, YoixObject argv[], YoixObject context) {

	YoixObject  obj = null;
	int         index;

	if ((index = names.definedAt(name)) != -1) {
	    if (values.isArray())
		index = names.get(index, false).intValue();
	    obj = execute(index, argv, context);
	} else VM.abort(UNDEFINED, name);
	return(obj);
    }


    public final YoixObject
    get(int index, boolean clone) {

	return(values.get(index, clone));
    }


    public final YoixObject
    get(String name, boolean clone) {

	YoixObject  obj = null;

	if ((obj = names.get(name, clone)) != null) {
	    if (values.isArray())
		obj = values.get(obj.intValue(), clone);
	}
	return(obj);
    }


    public final int
    hash(String name) {

	int  index;

	if ((index = names.hash(name)) != -1) {
	    if (values.isArray())
		index = names.get(index, false).intValue();
	}
	return(index);
    }


    public final String
    name(int index) {

	String  name = null;
	int     n;

	if (values.isArray()) {
	    for (n = 0; n < names.length(); n++) {
		if (names.defined(n)) {
		    if (names.get(n, false).intValue() == index) {
			name = names.name(n);
			break;
		    }
		}
	    }
	} else name = values.name(index);
	return(name);
    }


    public final YoixObject
    put(int index, YoixObject obj, boolean clone) {

	if (values.defined(index))
	    obj = values.put(index, obj, clone);
	else VM.abort(UNDEFINED, index);
	return(obj);
    }


    public final YoixObject
    put(String name, YoixObject obj, boolean clone) {

	int  index;

	if ((index = names.definedAt(name)) != -1) {
	    if (values.isArray())
		index = names.get(index, false).intValue();
	    obj = put(index, obj, clone);
	} else VM.abort(UNDEFINED, name);
	return(obj);
    }


    public final boolean
    readable(int index) {

	return(values.readable(index));
    }


    public final boolean
    readable(String name) {

	boolean  result = false;
	int      index;

	if ((index = names.definedAt(name)) != -1) {
	    if (values.isArray())
		index = names.get(index, false).intValue();
	    result = readable(index);
	}
	return(result);
    }


    public final int
    reserve(String name) {

	int  index;

	if ((index = names.definedAt(name)) != -1) {
	    if (values.isArray())
		index = names.get(index, false).intValue();
	}
	return(index);
    }

    ///////////////////////////////////
    //
    // YoixBodyDictionary Methods
    //
    ///////////////////////////////////

    final int[]
    sortedOrder() {

	return(names.sortedOrder());
    }
}

