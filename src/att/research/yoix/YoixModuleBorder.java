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
class YoixModuleBorder extends YoixModule

    implements YoixConstantsJFC

{

    //
    // Swing border types. We let a Border grow, primarily to leave room
    // for an entry named width that was supported in older releases and
    // used in at least one imporant application. New applications should
    // not rely on width because support for it may be removed in future
    // releases - look in YoixMakeScreen.java for the low level Java code.
    //

    static String  $MODULENAME = null;

    static Integer  $BEVELBORDER = new Integer(YOIX_BEVELED);
    static Integer  $EMPTYBORDER = new Integer(YOIX_EMPTY);
    static Integer  $ETCHEDBORDER = new Integer(YOIX_ETCHED);
    static Integer  $LINEBORDER = new Integer(YOIX_LINED);
    static Integer  $MATTEBORDER = new Integer(YOIX_MATTE);
    static Integer  $SOFTBEVELBORDER = new Integer(YOIX_SOFTBEVELED);

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       T_BORDER,             "15",                $DICT,      $L___, T_BORDER,
       null,                 "-1",                $GROWTO,    null,  null,
       N_ALIGNMENT,          $YOIX_LEFT,          $INTEGER,   $RW_,  null,
       N_BACKGROUND,         T_COLOR,             $NULL,      $RW_,  null,
       N_CHILD,              T_BORDER,            $NULL,      $RW_,  null,
       N_FONT,               T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,         T_COLOR,             $NULL,      $RW_,  null,
       N_HIGHLIGHT,          T_COLOR,             $NULL,      $RW_,  null,
       N_ICON,               T_IMAGE,             $NULL,      $RW_,  null,
       N_INSETS,             T_OBJECT,            $NULL,      $RW_,  null,
       N_POSITION,           $YOIX_TOP,           $INTEGER,   $RW_,  null,
       N_RAISED,             $FALSE,              $INTEGER,   $RW_,  null,
       N_ROUNDED,            $FALSE,              $INTEGER,   $RW_,  null,
       N_SHADOW,             T_COLOR,             $NULL,      $RW_,  null,
       N_THICKNESS,          "1.0",               $DOUBLE,    $RW_,  null,
       N_TITLE,              T_STRING,            $NULL,      $RW_,  null,
       N_TYPE,               $ETCHEDBORDER,       $INTEGER,   $RW_,  null,

       T_BEVELBORDER,        T_BORDER,            $DUP,       $L___, T_BEVELBORDER,
       N_TYPE,               $BEVELBORDER,        $INTEGER,   $LR__, null,
       null,                 T_BORDER,            $TYPENAME,  null,  null,

       T_EMPTYBORDER,        T_BORDER,            $DUP,       $L___, T_EMPTYBORDER,
       N_TYPE,               $EMPTYBORDER,        $INTEGER,   $LR__, null,
       null,                 T_BORDER,            $TYPENAME,  null,  null,

       T_ETCHEDBORDER,       T_BORDER,            $DUP,       $L___, T_ETCHEDBORDER,
       N_TYPE,               $ETCHEDBORDER,       $INTEGER,   $LR__, null,
       null,                 T_BORDER,            $TYPENAME,  null,  null,

       T_LINEBORDER,         T_BORDER,            $DUP,       $L___, T_LINEBORDER,
       N_TYPE,               $LINEBORDER,         $INTEGER,   $LR__, null,
       null,                 T_BORDER,            $TYPENAME,  null,  null,

       T_MATTEBORDER,        T_BORDER,            $DUP,       $L___, T_MATTEBORDER,
       N_TYPE,               $MATTEBORDER,        $INTEGER,   $LR__, null,
       null,                 T_BORDER,            $TYPENAME,  null,  null,

       T_SOFTBEVELBORDER,    T_BORDER,            $DUP,       $L___, T_SOFTBEVELBORDER,
       N_TYPE,               $SOFTBEVELBORDER,    $INTEGER,   $LR__, null,
       null,                 T_BORDER,            $TYPENAME,  null,  null,
    };
}

