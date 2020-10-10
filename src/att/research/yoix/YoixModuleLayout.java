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
class YoixModuleLayout extends YoixModule

    implements YoixConstantsJFC

{

    static String  $MODULENAME = null;

    static Integer  $BORDERLAYOUT = new Integer(YOIX_BORDERLAYOUT);
    static Integer  $BOXLAYOUT = new Integer(YOIX_BOXLAYOUT);
    static Integer  $CARDLAYOUT = new Integer(YOIX_CARDLAYOUT);
    static Integer  $CUSTOMLAYOUT = new Integer(YOIX_CUSTOMLAYOUT);
    static Integer  $FLOWLAYOUT = new Integer(YOIX_FLOWLAYOUT);
    static Integer  $GRIDBAGLAYOUT = new Integer(YOIX_GRIDBAGLAYOUT);
    static Integer  $GRIDLAYOUT = new Integer(YOIX_GRIDLAYOUT);
    static Integer  $SPRINGLAYOUT = new Integer(YOIX_SPRINGLAYOUT);

    static Object  $module[] = {
    //
    // NAME                  ARG                  COMMAND     MODE   REFERENCE
    // ----                  ---                  -------     ----   ---------
       T_LAYOUTMANAGER,     T_DICT,               $NULL,      $L___, T_LAYOUTMANAGER,

       T_BORDERLAYOUT,       "3",                 $DICT,      $L___, T_BORDERLAYOUT,
       null,                 T_LAYOUTMANAGER,     $TYPENAME,  null,  null,
       N_TYPE,               $BORDERLAYOUT,       $INTEGER,   $LR__, null,
       N_HGAP,               "0",                 $DOUBLE,    $RW_,  null,
       N_VGAP,               "0",                 $DOUBLE,    $RW_,  null,

       T_BOXLAYOUT,          "2",                 $DICT,      $L___, T_BOXLAYOUT,
       null,                 T_LAYOUTMANAGER,     $TYPENAME,  null,  null,
       N_TYPE,               $BOXLAYOUT,          $INTEGER,   $LR__, null,
       N_ORIENTATION,        $YOIX_HORIZONTAL,    $INTEGER,   $RW_,  null,

       T_CARDLAYOUT,         "3",                 $DICT,      $L___, T_CARDLAYOUT,
       null,                 T_LAYOUTMANAGER,     $TYPENAME,  null,  null,
       N_TYPE,               $CARDLAYOUT,         $INTEGER,   $LR__, null,
       N_HGAP,               "0",                 $DOUBLE,    $RW_,  null,
       N_VGAP,               "0",                 $DOUBLE,    $RW_,  null,

       T_CUSTOMLAYOUT,       "1",                 $DICT,      $L___, T_CUSTOMLAYOUT,
       null,                 "-1",                $GROWTO,    null,  null,
       null,                 T_LAYOUTMANAGER,     $TYPENAME,  null,  null,
       N_TYPE,               $CUSTOMLAYOUT,       $INTEGER,   $LR__, null,

       T_FLOWLAYOUT,         "4",                 $DICT,      $L___, T_FLOWLAYOUT,
       null,                 T_LAYOUTMANAGER,     $TYPENAME,  null,  null,
       N_TYPE,               $FLOWLAYOUT,         $INTEGER,   $LR__, null,
       N_ALIGNMENT,          $YOIX_CENTER,        $INTEGER,   $RW_,  null,
       N_HGAP,               "0",                 $DOUBLE,    $RW_,  null,
       N_VGAP,               "0",                 $DOUBLE,    $RW_,  null,

       T_GRIDBAGCONSTRAINTS, "11",                $DICT,      $L___, T_GRIDBAGCONSTRAINTS,
       N_ANCHOR,             $YOIX_CENTER,        $INTEGER,   $RW_,  null,
       N_FILL,               $YOIX_NONE,          $INTEGER,   $RW_,  null,
       N_GRIDHEIGHT,         "1",                 $INTEGER,   $RW_,  null,
       N_GRIDWIDTH,          "1",                 $INTEGER,   $RW_,  null,
       N_GRIDX,              $YOIX_RELATIVE,      $INTEGER,   $RW_,  null,
       N_GRIDY,              $YOIX_RELATIVE,      $INTEGER,   $RW_,  null,
       N_INSETS,             T_OBJECT,            $NULL,      $RW_,  null,
       N_IPADX,              "0.0",               $DOUBLE,    $RW_,  null,
       N_IPADY,              "0.0",               $DOUBLE,    $RW_,  null,
       N_WEIGHTX,            "0.0",               $DOUBLE,    $RW_,  null,
       N_WEIGHTY,            "0.0",               $DOUBLE,    $RW_,  null,

       T_GRIDBAGLAYOUT,      "6",                 $DICT,      $L___, T_GRIDBAGLAYOUT,
       null,                 T_LAYOUTMANAGER,     $TYPENAME,  null,  null,
       N_TYPE,               $GRIDBAGLAYOUT,      $INTEGER,   $LR__, null,
       N_COLUMNS,            "0",                 $INTEGER,   $RW_,  null,
       N_HGAP,               "0",                 $DOUBLE,    $RW_,  null,
       N_MODEL,              "1",                 $INTEGER,   $RW_,  null,
       N_ORIENTATION,        $YOIX_NONE,          $INTEGER,   $RW_,  null,
       N_VGAP,               "0",                 $DOUBLE,    $RW_,  null,

       T_GRIDLAYOUT,         "8",                 $DICT,      $L___, T_GRIDLAYOUT,
       null,                 T_LAYOUTMANAGER,     $TYPENAME,  null,  null,
       N_TYPE,               $GRIDLAYOUT,         $INTEGER,   $LR__, null,
       N_HGAP,               "0",                 $DOUBLE,    $RW_,  null,
       N_VGAP,               "0",                 $DOUBLE,    $RW_,  null,
       N_ROWS,               "1",                 $INTEGER,   $RW_,  null,
       N_COLUMNS,            "0",                 $INTEGER,   $RW_,  null,
       N_MODEL,              "0",                 $INTEGER,   $RW_,  null,
       N_ORIENTATION,        $YOIX_HORIZONTAL,    $INTEGER,   $RW_,  null,
       N_USEALL,             $TRUE,               $INTEGER,   $RW_,  null,

       T_SPRINGCONSTRAINTS,  "5",                 $DICT,      $L___, T_SPRINGCONSTRAINTS,
       N_ANCHORCOMP,         T_STRING,            $NULL,      $RW_,  null,
       N_ANCHOREDGE,         "-1",                $INTEGER,   $RW_,  null,
       N_DEPENDCOMP,         T_STRING,            $NULL,      $RW_,  null,
       N_DEPENDEDGE,         "-1",                $INTEGER,   $RW_,  null,
       N_SPRING,             T_OBJECT,            $NULL,      $RW_,  null,

       T_SPRINGLAYOUT,       "6",                 $DICT,      $L___, T_SPRINGLAYOUT,
       null,                 T_LAYOUTMANAGER,     $TYPENAME,  null,  null,
       N_TYPE,               $SPRINGLAYOUT,       $INTEGER,   $LR__, null,
       N_HGAP,               "0",                 $DOUBLE,    $RW_,  null,
       N_VGAP,               "0",                 $DOUBLE,    $RW_,  null,
       N_ROWS,               "0",                 $INTEGER,   $RW_,  null,
       N_COLUMNS,            "0",                 $INTEGER,   $RW_,  null,
       N_ORIENTATION,        $YOIX_HORIZONTAL,    $INTEGER,   $RW_,  null,
    };
}

