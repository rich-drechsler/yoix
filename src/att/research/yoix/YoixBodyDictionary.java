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

abstract
class YoixBodyDictionary extends YoixPointer

    implements YoixConstants,
	       YoixInterfaceBody

{

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final String
    dump() {

	return(dump(0, "", null));
    }


    public final String
    toString() {

	return(dump());
    }


    public final int
    type() {

	return(DICTIONARY);
    }

    ///////////////////////////////////
    //
    // Abstract Methods
    //
    ///////////////////////////////////

    abstract int[]
    sortedOrder();
}

