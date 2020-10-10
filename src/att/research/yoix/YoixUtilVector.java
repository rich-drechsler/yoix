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

class YoixUtilVector extends Vector

    implements YoixConstants

{

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixUtilVector(int capacity) {

	super(Math.max(capacity, 0));
    }

    ///////////////////////////////////
    //
    // YoixUtilVector Methods
    //
    ///////////////////////////////////

    final synchronized boolean
    containsYoixObject(int index) {

	return(getYoixObject(index) != null);
    }


    final synchronized boolean
    containsYoixObject(YoixObject value) {

	return(findYoixObjectIndex(value) >= 0);
    }


    final synchronized Vector
    findYoixObject(YoixObject target) {

	Object  element;
	Vector  result;
	int     n;

	result = new Vector();

	if (target != null) {
	    for (n = 0; n < elementCount; n++) {
		if ((element = elementData[n]) != null) {
		    if (element instanceof YoixObject) {
			if (YoixInterpreter.equalsEQEQ(target, (YoixObject)element))
			    result.addElement(YoixObject.newInt(n));
		    }
		}
	    }
	}

	return(result);
    }


    final synchronized YoixObject
    firstYoixObject() {

	return(getYoixObject(0));
    }


    final synchronized void
    firstYoixObject(YoixObject value) {

	insertYoixObject(0, value);
    }


    final synchronized Vector
    getYoixObject() {

	Object  element;
	Vector  result;
	int     n;

	result = new Vector(elementCount);

	for (n = 0; n < elementCount; n++) {
	    element = elementData[n];
	    result.addElement(element instanceof YoixObject ? element : null);
	}

	return(result);
    }


    final synchronized YoixObject
    getYoixObject(int index) {

	Object  element;

	if (index >= 0 && index < elementCount) {
	    element = elementData[index];
	    if (!(element instanceof YoixObject))
		element = null;
	} else element = null;

	return((YoixObject)element);
    }


    final synchronized int
    getYoixObjectElementCount() {

	Object  element;
	int     count = 0;
	int     n;

	for (n = 0; n < elementCount; n++) {
	    element = elementData[n];
	    if (element instanceof YoixObject)
		count++;
	}

	return(count);
    }


    final synchronized Vector
    getYoixObjectElements() {

	Object  element;
	Vector  result;
	int     n;

	result = new Vector();

	for (n = 0; n < elementCount; n++) {
	    element = elementData[n];
	    if (element instanceof YoixObject)
		result.addElement(element);
	}

	return(result);
    }


    final synchronized int
    getYoixObjectSize() {

	return(size());
    }


    final synchronized int
    insertYoixObject(YoixObject pairs[]) {

	YoixObject  loc;
	YoixObject  value;
	int         count = 0;
	int         n;

	for (n = 0; n < pairs.length - 1; n += 2) {
	    loc = pairs[n];
	    value = pairs[n + 1];
	    if (loc != null && value != null) {
		if (loc.isInteger()) {
		    if (insertYoixObject(loc.intValue(), value))
			count++;
		}
	    }
	}

	return(count);
    }


    final synchronized boolean
    insertYoixObject(int index, YoixObject value) {

	if (index >= 0) {
	    if (index > elementCount)
		setSize(index);
	    insertElementAt(value, index);
	}

	return(index >= 0);
    }


    final synchronized YoixObject
    lastYoixObject() {

	return(getYoixObject(elementCount - 1));
    }


    final synchronized void
    lastYoixObject(YoixObject value) {

	putYoixObject(elementCount, value);
    }


    final synchronized void
    loadYoixObject(YoixObject obj) {

	int  length;
	int  m;
	int  n;

	removeAllElements();

	if (obj != null) {
	    length = obj.length();
	    setSize(obj.sizeof());
	    for (n = obj.offset(), m = 0; n < length; n++, m++) {
		if (obj.defined(n))
		    putYoixObject(m, obj.get(n, false));
	    }
	}
    }


    final synchronized Vector
    putYoixObject(YoixObject pairs[]) {

	YoixObject  loc;
	YoixObject  value;
	YoixObject  prev;
	Vector      result;
	int         n;

	result = new Vector();

	for (n = 0; n < pairs.length - 1; n += 2) {
	    loc = pairs[n];
	    value = pairs[n + 1];
	    if (loc != null && value != null) {
		if (loc.isInteger()) {
		    if ((prev = putYoixObject(loc.intValue(), value)) != null) {
			result.addElement(loc);
			result.addElement(prev);
		    }
		}
	    }
	}

	return(result);
    }


    final synchronized YoixObject
    putYoixObject(int index, YoixObject value) {

	Object  element = null;

	if (index >= 0) {
	    if (index < elementCount) {
		element = elementData[index];
		if (!(element instanceof YoixObject))
		    element = null;
	    } else setSize(index + 1);
	    elementData[index] = value;
	}

	return((YoixObject)element);
    }


    final synchronized int
    removeYoixObject(YoixObject value, boolean preserve) {

	int  count = 0;
	int  n;

	while ((n = findYoixObjectIndex(value)) >= 0) {
	    if (preserve)
		elementData[n] = null;
	    else removeElementAt(n);
	    count++;
	}

	return(count);
    }


    final synchronized YoixObject
    removeYoixObject(int index, boolean preserve) {

	Object  element;

	if (index >= 0 && index < elementCount) {
	    element = elementData[index];
	    if (element instanceof YoixObject) {
		if (preserve)
		    elementData[index] = null;
		else removeElementAt(index);
	    } else element = null;
	} else element = null;

	return((YoixObject)element);
    }


    final synchronized void
    setSizeTo(int size) {

	if (size >= 0 && size != size()) {
	    setSize(size);
	    if (capacity() != size) {
		trimToSize();
		ensureCapacity(size);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private int
    findYoixObjectIndex(YoixObject value) {

	Object  element;
	int     n;

	if (value != null) {
	    for (n = 0; n < elementCount; n++) {
		if ((element = elementData[n]) != null) {
		    if (element instanceof YoixObject) {	// probably unnecessary
			if (YoixInterpreter.equalsEQEQ(value, (YoixObject)element))
			    break;
		    }
		}
	    }
	} else n = -1;

	return(n < elementCount ? n : -1);
    }
}

