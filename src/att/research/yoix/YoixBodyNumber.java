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
class YoixBodyNumber

    implements YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCloneable,
	       Serializable

{

    //
    // Not hard to imagine different, and undoubtedly more efficient,
    // implementations. We will eventually experiment, but be careful
    // because YoixObject.java may be caching numbers, which in turn
    // probably assumes that data doesn't change.
    //

    private Number  data;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyNumber(int value) {

	this(new Integer(value));
    }


    YoixBodyNumber(double value) {

	this(new Double(value));
    }


    YoixBodyNumber(Number value) {

	this.data = value;
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

	return(data + NL);
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

	return(((data instanceof Double) ? DOUBLE : INTEGER));
    }

    ///////////////////////////////////
    //
    // YoixBodyNumber Methods
    //
    ///////////////////////////////////

    public final double
    doubleValue() {

	return(data.doubleValue());
    }


    public final float
    floatValue() {

	return(data.floatValue());
    }


    public final int
    intValue() {

	return(data.intValue());
    }


    public final long
    longValue() {

	return(data.longValue());
    }
}

