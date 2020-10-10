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
class YoixModuleCalendar extends YoixModule

{

    static String  $MODULENAME = null;

    //
    // A separate autoloaded module, but in this case it's not much of
    // a performance issue.
    //

    static String  $JAVAUTILCALENDAR = "java.util.Calendar";
    static String  $JAVAUTILGCALENDAR = "java.util.GregorianCalendar";

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "20",                $LIST,      $RORO, "Calendar",
       $JAVAUTILCALENDAR,    new Integer(0),      $READCLASS, $LR__, null,
       $JAVAUTILGCALENDAR,   new Integer(0),      $READCLASS, $LR__, null,
       null,                 null,                $GROWTO,    null,  null,
    };
}

