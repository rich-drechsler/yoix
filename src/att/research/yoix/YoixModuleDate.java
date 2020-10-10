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
class YoixModuleDate extends YoixModule

{

    static String  $MODULENAME = null;

    //
    // A separate autoloaded module, but in this case it's not much of
    // a performance issue.
    //

    static String  $JAVATEXTDATEFORMAT = "java.text.DateFormat";

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "10",                $LIST,      $RORO, "DateFormat",
       "DATETIME",           $DATETIME,           $INTEGER,   $LR__, null,
       "TIME",               $TIME,               $INTEGER,   $LR__, null,
       $JAVATEXTDATEFORMAT,  new Integer(0),      $READCLASS, $LR__, null,
       null,                 null,                $GROWTO,    null,  null,
    };
}

