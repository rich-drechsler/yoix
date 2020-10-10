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

class YoixUtilHashtable extends Hashtable

    implements YoixConstants

    //
    // Tricker than YoixUtilVector and probably harder than you might
    // expect because much of the internal machinery in a Hashtable is
    // private. That means we need control of equals() and hashCode(),
    // but we were reluctant (at this point) to implement the methods
    // in YoixObject.java.
    //
    // NOTE - seems like we could add indexing capabilites without too
    // much trouble, but we decided to wait. We will consider adding
    // the required code to this class or introducing a new type, say
    // HashVector, in a future release. The anticipated changes are
    // one of the main reasons why the data objects associated with a
    // Vector and Hashtable currently can't grow.
    //

{

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixUtilHashtable(int capacity) {

	super(Math.max(capacity, 1));		// can't ask for capacity <= 0
    }

    ///////////////////////////////////
    //
    // YoixUtilHashtable Methods
    //
    ///////////////////////////////////

    final synchronized boolean
    containsYoixObjectKey(YoixObject key) {

	return(containsKey(new YoixHashtableObject(key)));
    }


    final synchronized boolean
    containsYoixObjectValue(YoixObject value) {

	return(contains(new YoixHashtableObject(value)));
    }


    final synchronized Vector
    findYoixObject(YoixObject target) {

	Enumeration  enm;
	YoixObject   value;
	Object       key;
	Vector       result;

	result = new Vector();

	if (target != null) {
	    for (enm = keys(); enm.hasMoreElements(); ) {
		key = enm.nextElement();
		if (key instanceof YoixHashtableObject) {
		    key = ((YoixHashtableObject)key).data;
		    if ((value = getYoixObject((YoixObject)key)) != null) {
			if (YoixInterpreter.equalsEQEQ(target, value))
			    result.addElement(key);
		    }
		}
	    }
	}

	return(result);
    }


    final synchronized YoixObject
    getYoixObject(YoixObject key) {

	Object  value;

	if ((value = get(new YoixHashtableObject(key))) != null) {
	    if (value instanceof YoixHashtableObject)	// probably unnecessary
		value = ((YoixHashtableObject)value).data;
	    else value = null;
	}

	return((YoixObject)value);
    }


    final synchronized int
    getYoixObjectCount() {

	return(size());
    }


    final synchronized Vector
    getYoixObjectKeys() {

	Enumeration  enm;
	Object       key;
	Vector       result;

	result = new Vector();

	for (enm = keys(); enm.hasMoreElements(); ) {
	    key = enm.nextElement();
	    if (key instanceof YoixHashtableObject)
		result.addElement(((YoixHashtableObject)key).data);
	}

	return(result);
    }


    final synchronized Vector
    getYoixObjectPairs() {

	Enumeration  enm;
	Object       key;
	Vector       result;

	result = new Vector();

	for (enm = keys(); enm.hasMoreElements(); ) {
	    key = enm.nextElement();
	    if (key instanceof YoixHashtableObject) {
		key = ((YoixHashtableObject)key).data;
		result.addElement(key);
		result.addElement(getYoixObject((YoixObject)key));
	    }
	}

	return(result);
    }


    final synchronized Vector
    getYoixObjectValues() {

	Enumeration  enm;
	Object       value;
	Vector       result;

	result = new Vector();

	for (enm = elements(); enm.hasMoreElements(); ) {
	    value = enm.nextElement();
	    if (value instanceof YoixHashtableObject)
		result.addElement(((YoixHashtableObject)value).data);
	}

	return(result);
    }


    final synchronized void
    loadYoixObject(YoixObject obj) {

	int  length;
	int  n;
	int  m;

	clear();

	if (obj != null) {
	    length = obj.length() - 1;
	    for (n = obj.offset(); n < length; n += 2) {
		m = n + 1;
		if (obj.defined(n) && obj.defined(m))
		    putYoixObject(obj.get(n, false), obj.get(m, false));
	    }
	}
    }


    final synchronized Vector
    putYoixObject(YoixObject pairs[]) {

	YoixObject  key;
	YoixObject  value;
	YoixObject  prev;
	Vector      result;
	int         n;

	result = new Vector();

	for (n = 0; n < pairs.length - 1; n += 2) {
	    key = pairs[n];
	    value = pairs[n+1];
	    if (key != null && value != null) {
		if ((prev = putYoixObject(key, value)) != null) {
		    result.addElement(key);
		    result.addElement(prev);
		}
	    }
	}

	return(result);
    }


    final synchronized YoixObject
    putYoixObject(YoixObject key, YoixObject value) {

	Object  prev;

	if ((prev = put(new YoixHashtableObject(key), new YoixHashtableObject(value))) != null) {
	    if (prev instanceof YoixHashtableObject)	// probably unnecessary
		prev = ((YoixHashtableObject)prev).data;
	    else prev = null;
	}

	return((YoixObject)prev);
    }


    final synchronized YoixObject
    removeYoixObjectKey(YoixObject key) {

	Object  value;

	if ((value = remove(new YoixHashtableObject(key))) != null)
	    value = ((YoixHashtableObject)value).data;
	return((YoixObject)value);
    }


    final synchronized int
    removeYoixObjectValue(YoixObject target) {

	Enumeration  enm;
	YoixObject   value;
	Object       key;
	int          count;

	count = 0;

	if (target != null) {
	    for (enm = keys(); enm.hasMoreElements(); ) {
		key = enm.nextElement();
		if (key instanceof YoixHashtableObject) {
		    if ((value = getYoixObject(((YoixHashtableObject)key).data)) != null) {
			if (YoixInterpreter.equalsEQEQ(target, value)) {
			    remove(key);
			    count++;
			}
		    }
		}
	    }
	}

	return(count);
    }


    final synchronized void
    setSizeTo(int size) {

	Enumeration  enm;
	int          extra;

	if (size >= 0 && size != size()) {
	    if (size != 0) {
		if ((extra = size() - size) > 0) {
		    for (enm = keys(); enm.hasMoreElements() && extra > 0; extra--)
			remove(enm.nextElement());
		}
	    } else clear();
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixHashtableObject {

	YoixObject  data;

	YoixHashtableObject(YoixObject data) {

	    this.data = (data != null) ? data : YoixObject.newNull();
	}

	public final boolean
	equals(Object obj) {

	    boolean  result = false;

	    if (obj instanceof YoixHashtableObject)
		obj = ((YoixHashtableObject)obj).data;
	    if (obj instanceof YoixObject) {
		if (obj != null && data != null)
		    result = YoixInterpreter.equalsEQEQ((YoixObject)obj, data);
	    }

	    return(result);
	}

	public final int
	hashCode() {

	    int  code;

	    switch (data.major()) {
		case CALLABLE:
		    code = data.body().hashCode();
		    break;

		case NUMBER:
		    code = data.intValue();
		    break;

		case POINTER:
		    if (data.isString())
			code = data.stringValue().hashCode();
		    else code = data.body().hashCode();
		    break;

		default:
		    code = (data.major() << 8 | data.minor());
		    break;
	    }

	    return(code);
	}
    }
}

