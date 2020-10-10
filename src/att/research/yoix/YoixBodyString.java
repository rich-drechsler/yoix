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
class YoixBodyString extends YoixPointer

    implements YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCloneable

{

    private char  data[];

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyString(int length) {

	this.data = new char[length];
	this.length = length;
    }


    YoixBodyString(String value) {

	this.data = value.toCharArray();
	this.length = data.length;
    }


    YoixBodyString(StringBuffer sbuf) {

	this.length = sbuf.length();
	this.data = new char[this.length];
	sbuf.getChars(0, this.length, this.data, 0);
    }


    YoixBodyString(char data[]) {

	this.data = data;
	this.length = data.length;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public final synchronized Object
    clone() {

	YoixBodyString  obj;

	try {
	    obj = (YoixBodyString)super.clone();
	    if (obj.data != null) {
		obj.data = new char[obj.length];
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

	return(length);
    }


    public final String
    toString() {

	return(dump().trim());
    }


    public final int
    type() {

	return(STRING);
    }

    ///////////////////////////////////
    //
    // YoixInterfacePointer Methods
    //
    ///////////////////////////////////

    public final synchronized YoixObject
    cast(YoixObject obj, int index, boolean clone) {

	if (canWrite()) {
	    if (inRange(index) || (growTo(index + 1) && inRange(index))) {
		if (obj.isNumber()) {
		    if (obj.isInteger()) {
			if (clone)
			    obj = (YoixObject)obj.clone();
		    } else obj = YoixObject.newInt(obj.intValue());
		} else VM.abort(TYPECHECK, index);
	    } else VM.abort(RANGECHECK, index);
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


    public final void
    declare(int index, YoixObject obj, int mode) {

	if (canWrite()) {
	    if (inRange(index))
		VM.abort(REDECLARATION, index);
	    else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);
    }


    public final void
    declare(String name, YoixObject obj, int mode) {

	declare(reserve(name), obj, mode);
    }


    public final boolean
    defined(int index) {

	return(canRead() && inRange(index) && data[index] != '\0');
    }


    public final boolean
    defined(String name) {

	return(defined(hash(name)));
    }


    public final String
    dump(int index, String indent, String typename) {

	StringBuffer  buf;
	int           size;
	int           n;

	size = length;
	buf = new StringBuffer(size + 5);

	if (canRead()) {
	    if (index <= 0)
		buf.append('^');
	    for (n = (index < 0) ? index : 0; n < 0; n++)
		buf.append('.');
	    buf.append('"');
	    for (; n < index && n < size; n++)
		buf.append(YoixMake.javaString(data[n]));
	    if (n > 0 && n < size)
		buf.append("\"^\"");
	    for (; n < size; n++)
		buf.append(YoixMake.javaString(data[n]));
	    buf.append('"');
	    for (; n < index; n++)
		buf.append('.');
	    if (index > 0 && index >= size)
		buf.append('^');
	} else buf.append("--unreadable--");

	return(buf.toString() + NL);
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

	if (canExecute()) {
	    if (inRange(index)) {
		VM.abort(INVALIDACCESS, index);
	    } else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(null);
    }


    public final YoixObject
    execute(String name, YoixObject argv[], YoixObject context) {

	return(execute(hash(name), argv, context));
    }


    public final YoixObject
    get(int index, boolean clone) {

	YoixObject  obj = null;

	if (canRead()) {
	    if (inRange(index))
		obj = YoixObject.newInt((int)data[index]);
	    else VM.abort(RANGECHECK, index);
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

	if (canWrite()) {
	    if (inRange(index) || (growTo(index + 1) && inRange(index))) {
		if (obj.isNumber()) {
		    data[index] = (char)obj.intValue();
		    obj = YoixObject.newInt((int)data[index]);
		} else VM.abort(TYPECHECK, index);
	    } else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(obj);
    }


    public final YoixObject
    put(String name, YoixObject obj, boolean clone) {

	return(put(reserve(name), obj, clone));
    }


    public final boolean
    readable(int index) {

	return(defined(index));
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
    // YoixBodyString Methods
    //
    ///////////////////////////////////

    final synchronized void
    append(String src) {

	int  start;

	for (start = 0; start < length && data[start] != 0; start++);
	overlay(src, start, src.length());
    }


    final synchronized void
    append(char ch) {

	int  index;

	if (canWrite()) {
	    for (index = 0; index < length && data[index] != 0; index++);
	    if (inRange(index) || (growTo(index + 1) && inRange(index)))
		data[index] = ch;
	    else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);
    }


    final synchronized String
    cstringValue(int index) {

	StringBuffer  buf = null;
	String        str = null;
	int           size;
	int           i;

	if (canRead()) {
	    if (inRange(index)) {
		size = length;
		buf = new StringBuffer(size + 5);
		for (i = index; i < size; i++)
		    buf.append(YoixMake.javaString(data[i]));
		str = buf.toString();
	    } else if (index == 0 && length == 0)
		str = "";
	    else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(str);
    }


    final synchronized int
    indexOf(int ch, int from) {

	int  index = -1;
	int  n;

	if (canRead()) {
	    if (inRange(from)) {
		for (n = Math.max(from, 0); n < length; n++) {
		    if (data[n] == ch) {
			index = n - from;
			break;
		    }
		}
	    }
	} else VM.abort(INVALIDACCESS);

	return(index);
    }


    final synchronized void
    overlay(String src, int start, int count) {

	int  index;
	int  n;

	if (canWrite()) {
	    if (start >= 0) {
		if (count > 0) {
		    index = start + count - 1;
		    if (inRange(index) || (growTo(index + 1) && inRange(index))) {
			if (src != null) {
			    count = Math.min(count, src.length());
			    System.arraycopy(src.toCharArray(), 0, data, start, count);
			} else count = 0;
			for (n = start + count; n <= index; n++)
			    data[n] = '\0';
		    } else VM.abort(RANGECHECK, index);
		}
	    } else VM.abort(RANGECHECK, start);
	} else VM.abort(INVALIDACCESS);
    }


    final synchronized String
    stringValue(int index) {

	String  str = null;

	if (canRead()) {
	    if (inRange(index))
		str = new String(data, index, length - index);
	    else if (index == 0 && length == 0)
		str = "";
	    else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(str);
    }


    final synchronized char[]
    toCharArray(int index) {

	char  buf[] = null;

	if (canRead()) {
	    if (inRange(index)) {
		buf = new char[length - index];
		System.arraycopy(data, index, buf, 0, length - index);
	    } else if (index == 0 && length == 0)
		buf = new char[0];
	    else VM.abort(RANGECHECK, index);
	} else VM.abort(INVALIDACCESS);

	return(buf);
    }


    final synchronized char[]
    toCharArray(boolean grab, Object arg) {

	char  buf[] = null;

	if (grab) {
	    if (canRead() && canWrite())
		buf = data;
	} else buf = toCharArray(0);

	return(buf);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized boolean
    growTo(int nlength) {

	char  ndata[];

	if (nlength > length) {
	    if (canGrowTo(nlength)) {
		ndata = new char[nlength];
		System.arraycopy(data, 0, ndata, 0, length);
		data = ndata;
		length = nlength;
	    }
	}

	return(length >= nlength);
    }
}

