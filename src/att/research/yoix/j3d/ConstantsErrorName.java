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

package att.research.yoix.j3d;

public
interface ConstantsErrorName {

    //
    // Standard error names - separate so they're easy to load into
    // a dictionary at boot time.
    //

    public static final String  BADTRANSFORM = "badtransform";
    public static final String  BADFILECONTENT = "badfilecontent";
    public static final String  BADFILEFORMAT = "badfileformat";
    public static final String  CAPABILITYNOTSET = "capabilitynotset";
    public static final String  MULTIPLEPARENT = "multipleparent";
/*
    public static final String  RESTRICTEDACCESS = "restrictedacccess";
*/
    public static final String  SCENEERROR = "sceneerror";
}

