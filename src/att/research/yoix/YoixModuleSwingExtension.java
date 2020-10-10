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
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.text.*;

abstract
class YoixModuleSwingExtension extends YoixModuleSwing
{

    //
    // We exceeded the capacity of $module in YoixModuleSwing, so we moved
    // the overflow to this class and just use $INCLUDE in YoixModuleSwing
    // to pull this extension in when the swing module is loaded.
    //

    static String  $MODULENAME = M_SWING_EXTENSION;

    static Object  $module[] = {
    //
    // NAME                       ARG                  COMMAND     MODE   REFERENCE
    // ----                       ---                  -------     ----   ---------
       null,                      "1",                 $LIST,      $RORO, $MODULENAME,

    //
    // Remaining Swing types.
    //

       T_JWINDOW,                 "45",                $DICT,      $L___, T_JWINDOW,
       null,                      "-1",                $GROWTO,    null,  null,
       N_MAJOR,                   $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                   $JWINDOW,            $INTEGER,   $LR__, null,
       N_AUTODISPOSE,             $FALSE,              $INTEGER,   $RW_,  null,
       N_AUTORAISE,               $TRUE,               $INTEGER,   $RW_,  null,
       N_BACKGROUND,              T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,         $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,         T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,              T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                  $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DISPOSE,                 $FALSE,              $INTEGER,   $RW_,  null,
       N_DOUBLEBUFFERED,          T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,             $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                 $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                     T_OBJECT,            $NULL,      $LR__, null,
       N_FIRSTFOCUS,              $TRUE,               $INTEGER,   $RW_,  null,
       N_FOCUSABLE,               T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,              $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,              T_COLOR,             $NULL,      $RW_,  null,
       N_FRONTTOBACK,             $TRUE,               $INTEGER,   $RW_,  null,
       N_FULLSCREEN,              $FALSE,              $INTEGER,   $RW_,  null,
       N_GLASSPANE,               T_OBJECT,            $NULL,      $RW_,  null,
       N_GRAPHICS,                T_GRAPHICS,          $NULL,      $RW_,  null,
       N_LAYOUT,                  T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,           T_BORDERLAYOUT,      $DECLARE,   $RW_,  null,
       N_LOCATION,                $DEFAULTLOCATION,    $POINT,     $RW_,  null,
       N_NEXTCARD,                T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                  $TRUE,               $OBJECT,    $RW_,  null,
       N_PAINT,                   T_CALLABLE,          $NULL,      $RWX,  null,
       N_PARENT,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                   T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_REPAINT,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,            $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                    T_OBJECT,            $NULL,      $LR__, null,
       N_SCREEN,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_SHAPE,                   T_PATH,              $NULL,      $RW_,  null,
       N_SHOWING,                 $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                    $DEFAULTSIZE,        $DIMENSION, $RW_,  null,
       N_TAG,                     T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                   T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,             T_STRING,            $NULL,      $RW_,  null,
       N_UIMKEY,                  T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                 $FALSE,              $INTEGER,   $RW_,  null,

    //
    // Convenient extensions of existing types.
    //

       T_JCHECKBOX,               T_JBUTTON,           $DUP,       $L___, T_JCHECKBOX,
       N_ALIGNMENT,               $YOIX_LEADING,       $INTEGER,   $RW_,  null,
       N_TYPE,                    $CHECKBOXBUTTON,     $INTEGER,   $LR__, null,
       N_UIMKEY,                  T_STRING,            $NULL,      $RW_,  null,
       null,                      T_JBUTTON,           $TYPENAME,  null,  null,

       T_JCHECKBOXMENUITEM,       T_JMENUITEM,         $DUP,       $L___, T_JCHECKBOXMENUITEM,
       N_ALIGNMENT,               $YOIX_LEADING,       $INTEGER,   $RW_,  null,
       N_TYPE,                    $CHECKBOXBUTTON,     $INTEGER,   $LR__, null,
       N_UIMKEY,                  T_STRING,            $NULL,      $RW_,  null,
       null,                      T_JMENUITEM,         $TYPENAME,  null,  null,

       T_JCHECKBOXGROUP,          T_BUTTONGROUP,       $DUP,       $L___, T_JCHECKBOXGROUP,
       N_MODEL,                   "0",                 $INTEGER,   $RW_,  null,
       null,                      T_BUTTONGROUP,       $TYPENAME,  null,  null,

       T_JPASSWORDFIELD,          T_JTEXTFIELD,        $DUP,       $L___, T_JPASSWORDFIELD,
       N_ECHO,                    "42",                $INTEGER,   $RW_,  null,
       N_UIMKEY,                  T_STRING,            $NULL,      $RW_,  null,
       null,                      T_JTEXTFIELD,        $TYPENAME,  null,  null,

       T_JRADIOBUTTON,            T_JBUTTON,           $DUP,       $L___, T_JRADIOBUTTON,
       N_ALIGNMENT,               $YOIX_LEADING,       $INTEGER,   $RW_,  null,
       N_TYPE,                    $RADIOBUTTON,        $INTEGER,   $LR__, null,
       N_UIMKEY,                  T_STRING,            $NULL,      $RW_,  null,
       null,                      T_JBUTTON,           $TYPENAME,  null,  null,

       T_JRADIOBUTTONMENUITEM,    T_JMENUITEM,         $DUP,       $L___, T_JRADIOBUTTONMENUITEM,
       N_ALIGNMENT,               $YOIX_LEADING,       $INTEGER,   $RW_,  null,
       N_TYPE,                    $RADIOBUTTON,        $INTEGER,   $LR__, null,
       N_UIMKEY,                  T_STRING,            $NULL,      $RW_,  null,
       null,                      T_JMENUITEM,         $TYPENAME,  null,  null,

       T_JTOGGLEBUTTON,           T_JBUTTON,           $DUP,       $L___, T_JTOGGLEBUTTON,
       N_TYPE,                    $TOGGLEBUTTON,       $INTEGER,   $LR__, null,
       N_UIMKEY,                  T_STRING,            $NULL,      $RW_,  null,
       null,                      T_JBUTTON,           $TYPENAME,  null,  null,

    //
    // AWT compatibility types. $GET is a recent addition that's OK in
    // some instances, but if you're not 100% certain use $DUP.
    //

       T_JCHECKBOXGROUP_AWT,      T_JCHECKBOXGROUP,    $DUP,       $L___, T_JCHECKBOXGROUP_AWT,

       T_JCHECKBOX_AWT,           T_JCHECKBOX,         $DUP,       $L___, T_JCHECKBOX_AWT,
       null,                      T_JCHECKBOX,         $TYPENAME,  null,  null,

       T_JSCROLLBAR_AWT,          T_JSCROLLBAR,        $GET,       $L___, T_JSCROLLBAR_AWT,
       null,                      T_JSCROLLBAR,        $TYPENAME,  null,  null,

       T_JCHOICE,                 T_JCOMBOBOX,         $DUP,       $L___, T_JCHOICE,
       null,                      T_JCOMBOBOX,         $TYPENAME,  null,  null,
    };

    ///////////////////////////////////
    //
    // YoixModuleSwingExtension Methods
    //
    ///////////////////////////////////
}
