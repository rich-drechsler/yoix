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
class YoixModuleSound extends YoixModule

{

    //
    // Nothing much right now, but we anticipate javax.sound support in
    // a future release.
    //

    static String  $MODULENAME = M_SOUND;

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       null,                 "0",                 $LIST,      $RORO, $MODULENAME,

       T_AUDIOCLIP,          "7",                 $DICT,      $L___, T_AUDIOCLIP,
       N_MAJOR,              $AUDIOCLIP,          $INTEGER,   $LR__, null,
       N_MINOR,              "0",                 $INTEGER,   $LR__, null,
       N_DISABLED,           $TRUE,               $INTEGER,   $LR__, null,
       N_NAME,               T_STRING,            $NULL,      $RW_,  null,
       N_LOOP,               T_CALLABLE,          $NULL,      $L__X, null,
       N_PLAY,               T_CALLABLE,          $NULL,      $L__X, null,
       N_STOP,               T_CALLABLE,          $NULL,      $L__X, null,
    };
}

