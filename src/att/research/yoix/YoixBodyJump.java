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
class YoixBodyJump

    implements YoixConstants,
	       YoixInterfaceBody,
	       YoixInterfaceCloneable

{

    //
    // Used for error handling and implementing break, exit, return,
    // continue and try/catch. Thread-safe because our stacks are,
    // and these only show up on a stack.
    //

    private YoixError  error;
    private int        type;
    private boolean    breakable = false;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixBodyJump(int type, YoixError error) {

	this.error = error;
	this.type = type;
    }


    YoixBodyJump(int type, YoixError error, boolean breakable) {

	this.error = error;
	this.type = type;
	this.breakable = breakable;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceCloneable Methods
    //
    ///////////////////////////////////

    public synchronized Object
    clone() {

	return(VM.die(INTERNALERROR));
    }


    public Object
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

	return("--" + YoixMisc.tokenImage(type).toLowerCase() + "--" + NL);
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
    // YoixBodyJump Methods
    //
    ///////////////////////////////////

    final boolean
    isBreakable() {

	return(breakable);
    }


    final void
    jump() {

	jump(null);
    }


    final void
    jump(YoixObject details) {

	error.setDetails(details);
	throw(error);
    }
}

