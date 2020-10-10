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

abstract
class YoixModuleError extends YoixModule

{

    static String  $MODULENAME = M_ERROR;

    static String  $YOIXCONSTANTSERRORNAME = YOIXPACKAGE + ".YoixConstantsErrorName";

    static Object  $module[] = {
    //
    // NAME                     ARG               COMMAND     MODE   REFERENCE
    // ----                     ---               -------     ----   ---------
       null,                    "67",             $LIST,      $RORO, $MODULENAME,
       $YOIXCONSTANTSERRORNAME, null,             $READCLASS, $LR__, null,
    };
}

