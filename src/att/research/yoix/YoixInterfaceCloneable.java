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
import java.util.HashMap;

public
interface YoixInterfaceCloneable extends Cloneable {

    //
    // Methods that must be implemented by classes that claim they can
    // copy themselves. Extends Cloneable so clone() is included, but
    // it's explicitly mentioned here to make sure the complier knows
    // there's a public implementation of clone().
    //

    Object  clone();
    Object  copy(HashMap copied);
}

