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
class YoixBodyNull extends YoixPointer

    implements YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCallable,
	       YoixInterfaceCloneable,
	       Serializable

{

    private int  type;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyNull(int type) {

	this.type = type;
	this.length = 0;
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

	return(dump(0, "", null));
    }


    public final int
    length() {

	return(0);
    }


    public final String
    toString() {

	return(dump().trim());
    }


    public final int
    type() {

	return(type);
    }

    ///////////////////////////////////
    //
    // YoixInterfacePointer Methods
    //
    ///////////////////////////////////

    public final YoixObject
    cast(YoixObject obj, int index, boolean clone) {

	return(VM.abort(NULLPOINTER, index));
    }


    public final YoixObject
    cast(YoixObject obj, String name, boolean clone) {

	return(VM.abort(NULLPOINTER, name));
    }


    public final boolean
    compound() {

	return(false);
    }


    public final void
    declare(int index, YoixObject obj, int mode) {

	VM.abort(NULLPOINTER, index);
    }


    public final void
    declare(String name, YoixObject obj, int mode) {

	VM.abort(NULLPOINTER, name);
    }


    public final boolean
    defined(int index) {

	return(false);
    }


    public final boolean
    defined(String name) {

	return(false);
    }


    public final String
    dump(int index, String indent, String typename) {

	String  str;

	if (typename == null) {
	    if (type != EMPTY && type != NULL)		// EMPTY should be temporary
		str = "NULL:" + SimpleNode.typeString(type);
	    else str = "NULL";
	} else str = "NULL:" + typename;

	return(str + NL);
    }


    public final boolean
    executable(int index) {

	return(false);
    }


    public final boolean
    executable(String name) {

	return(false);
    }


    public final YoixObject
    execute(int index, YoixObject argv[], YoixObject context) {

	return(VM.abort(NULLPOINTER, index));
    }


    public final YoixObject
    execute(String name, YoixObject argv[], YoixObject context) {

	return(VM.abort(NULLPOINTER, name));
    }


    public final YoixObject
    get(int index, boolean clone) {

	return(VM.abort(NULLPOINTER, index));
    }


    public final YoixObject
    get(String name, boolean clone) {

	return(VM.abort(NULLPOINTER, name));
    }


    public final int
    hash(String name) {

	return(-1);
    }


    public final String
    name(int index) {

	return(defined(index) ? (index + "") : null);
    }


    public final YoixObject
    put(int index, YoixObject obj, boolean clone) {

	return(VM.abort(NULLPOINTER, index));
    }


    public final YoixObject
    put(String name, YoixObject obj, boolean clone) {

	return(VM.abort(NULLPOINTER, name));
    }


    public final boolean
    readable(int index) {

	return(false);
    }


    public final boolean
    readable(String name) {

	return(false);
    }


    public final int
    reserve(String name) {

	VM.abort(NULLPOINTER, name);
	return(-1);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCallable Methods
    //
    ///////////////////////////////////

    public final YoixObject
    call(YoixObject argv[], YoixObject context) {

	return(argv.length == 0 ? YoixObject.newEmpty() : VM.badCall());
    }


    public final boolean
    callable(int argc) {

	return(argc == 0);
    }


    public final boolean
    callable(YoixObject argv[]) {

	return(callable(argv.length));
    }
}

