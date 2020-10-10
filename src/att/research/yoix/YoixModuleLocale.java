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
class YoixModuleLocale extends YoixModule

{

    //
    // A separate autoloaded module mostly because building the Locale
    // objects is time-consuming, which affected yoix.util loading. We
    // may consider a more drastic separation of yoix.util in a future
    // release.
    //

    static String  $MODULENAME = null;

    static String  $JAVAUTILLOCALE = "java.util.Locale";

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "10",                $LIST,      $RORO, "Locale",
       $JAVAUTILLOCALE,      Locale.getDefault(), $READCLASS, $LR__, null,
       null,                 null,                $GROWTO,    null,  null,
    };
}

