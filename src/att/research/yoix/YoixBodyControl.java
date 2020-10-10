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
class YoixBodyControl

    implements YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCloneable

{

    //
    // Only used by internal objects, like EMPTY, MARK, and SAVE that
    // usually just show up on the stack.
    //

    private int     type = EMPTY;
    private Object  data;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyControl(int type) {

	this(type, null);
    }


    YoixBodyControl(int type, int data) {

	this(type, new Integer(data));
    }


    YoixBodyControl(int type, Object data) {

	this.type = type;
	this.data = data;
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

	String  str;

	switch (type) {
	    case ACCESS:
		str = "--access:" + intValue() + "--";
		break;

	    case EMPTY:
		str = "--empty--";
		break;

	    case FINALLY:
		str = "--finally--";
		break;

	    case MARK:
		str = "--mark--";
		break;

	    case RESTORE:
		str = "--restore--";
		break;

	    case SAVE:
		str = "--save--";
		break;

	    case TAG:
		str = "--tag--";
		break;

	    default:
		str = "--internal:" + data + "--";
		break;
	}

	return(str + NL);
    }


    public final int
    length() {

	return(0);
    }


    public String
    toString() {

	return(dump().trim());
    }


    public final int
    type() {

	return(type);
    }

    ///////////////////////////////////
    //
    // YoixBodyControl Methods
    //
    ///////////////////////////////////

    final Object
    getManagedObject() {

	return(data);
    }


    final int
    intValue() {

	return(data instanceof Integer ? ((Integer)data).intValue() : 0);
    }
}

