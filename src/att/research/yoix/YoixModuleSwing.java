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
class YoixModuleSwing extends YoixModule

    implements YoixConstantsJTable,
	       YoixConstantsSwing,
	       YoixConstantsImage

{

    static String  $MODULENAME = M_SWING;

    static String  $YOIXCONSTANTSJFC = YOIXPACKAGE + ".YoixConstantsJFC";
    static String  $YOIXCONSTANTSJTABLE = YOIXPACKAGE + ".YoixConstantsJTable";
    static String  $YOIXCONSTANTSJTREE = YOIXPACKAGE + ".YoixConstantsJTree";
    static String  $YOIXCONSTANTSSWING = YOIXPACKAGE + ".YoixConstantsSwing";

    //
    // Default constants
    //

    static Dimension  $DEFAULTSIZE = new Dimension(576, 432);
    static Integer    $BUTTON1MASK = new Integer(YOIX_BUTTON1_MASK);
    static Integer    $CHECKBOXBUTTON = new Integer(YOIX_CHECKBOX_BUTTON);
    static Integer    $COLUMNTYPE = new Integer(YOIX_STRING_TYPE);
    static Integer    $COPY = new Integer(YOIX_COPY);
    static Integer    $DEFAULTICON = new Integer(YOIX_DEFAULT_ICON);
    static Integer    $NULLTRANSFERHANDLER = new Integer(0); 
    static Integer    $RADIOBUTTON = new Integer(YOIX_RADIO_BUTTON);
    static Integer    $RESIZEMODE = new Integer(YOIX_AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    static Integer    $SINGLESELECTION = new Integer(YOIX_SINGLE_SELECTION);
    static Integer    $STANDARDBUTTON = new Integer(YOIX_STANDARD_BUTTON);
    static Integer    $STANDARDCURSOR = new Integer(V_STANDARD_CURSOR);
    static Integer    $TILEHINT = new Integer(YOIX_SCALE_TILE);
    static Integer    $TOGGLEBUTTON = new Integer(YOIX_TOGGLE_BUTTON);
    static Double     $MEDIUMINSETS = new Double(2.0);
    static Point      $DEFAULTLOCATION = new Point(0, 0);

    //
    // Standard layers
    //

    static Integer  $DEFAULT_LAYER = JLayeredPane.DEFAULT_LAYER;
    static Integer  $POPUP_LAYER = JLayeredPane.POPUP_LAYER;
    static Integer  $FRAME_CONTENT_LAYER = JLayeredPane.FRAME_CONTENT_LAYER;

    //
    // UIMKey constants
    //

    static String  $BUTTON_UIMKEY = YoixMisc.jvmCompareTo("1.5") >= 0 ? "Button" : null;

    //
    // Magic numbers that are unpacked by YoixSwingJScrollPane and used to
    // control the behavior of the getMinimumSize() and getPreferredSize()
    // methods in that class. Bits 0 to 3 set the minimum size model, bits
    // 4 to 7 set the preferred size model, bits 8 to 11 are special flags,
    // and the remaining are currently undocumented and should be zero. A
    // value that's zero means Java's default answers will be used for the
    // minimum and preferred sizes. Model 1 means values are calculated by
    // Yoix interpreter and optionally adjusted based on the special flags.
    // Model 2 means both dimensions will be zero, which obviously doesn't
    // make sense for the preferred size, but occasionally can be a useful
    // minimum size.
    // 

    static Integer  $JLIST_FLAGS = new Integer(0x111);
    static Integer  $JSCROLLPANE_FLAGS = new Integer(0x411);
    static Integer  $JTABLE_FLAGS = new Integer(0x011);
    static Integer  $JTABBEDPANE_FLAGS = new Integer(0x000);
    static Integer  $JTEXTAREA_FLAGS = new Integer(0x011);
    static Integer  $JTEXTPANE_FLAGS = new Integer(0x011);

    static Object  $module[] = {
    //
    // NAME                          ARG                  COMMAND     MODE   REFERENCE
    // ----                          ---                  -------     ----   ---------
       null,                         "319",               $LIST,      $RORO, $MODULENAME,
       $YOIXCONSTANTSJFC,            "YOIX_\tYOIX_",      $READCLASS, $LR__, null,
       $YOIXCONSTANTSSWING,          "YOIX_\tYOIX_",      $READCLASS, $LR__, null,

       "addColor",                   "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "addCursor",                  "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "addEventHandler",            "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "addListener",                "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "appendText",                 "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "beep",                       "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "deleteText",                 "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "distance",                   "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getBestCursorSize",          "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getBrighterColor",           "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getCMYKColor",               "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getColorName",               "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getDarkerColor",             "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getFirstFocusComponent",     "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getFocusComponentAfter",     "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getFocusComponentBefore",    "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getFocusComponents",         "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getFocusOwner",              "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getFontList",                "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getHSBColor",                "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getHSBComponents",           "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getMaximumCursorColors",     "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getLastFocusComponent",      "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getLocationInRoot",          "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getLocationOnScreen",        "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getRGBColor",                "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getSaturationAdjustedColor", "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getScreenInsets",            "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getScreenResolution",        "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "getScreenSize",              "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "insertText",                 "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "invokeLater",                "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "isDispatchThread",           "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "isFullScreenSupported",      "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "postEvent",                  "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "printAll",                   "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "removeEventHandler",         "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "removeListener",             "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "replaceText",                "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "toBack",                     "YoixModuleJFC",     $MODULE,    $LR_X, null,
       "toFront",                    "YoixModuleJFC",     $MODULE,    $LR_X, null,

       "getHighlights",              "-1",                $BUILTIN,   $LR_X, null,
       "getScrollerSize",            "1",                 $BUILTIN,   $LR_X, null,
       "getSwingThreadSafe",         "0",                 $BUILTIN,   $LR_X, null,
       "getViewportSize",            "-1",                $BUILTIN,   $LR_X, null,
       "setHighlights",              "-2",                $BUILTIN,   $LR_X, null,
       "setSwingThreadSafe",         "-1",                $BUILTIN,   $LR_X, null,
       "showConfirmDialog",          "-2",                $BUILTIN,   $LR_X, null,
       "showInputDialog",            "-2",                $BUILTIN,   $LR_X, null,
       "showMessageDialog",          "-2",                $BUILTIN,   $LR_X, null,

       "Color",                      "YoixModuleJFC",     $MODULE,    $RORO, null,
       "Cursor",                     "YoixModuleJFC",     $MODULE,    $RORO, null,
       "KeyCode",                    "YoixModuleJFC",     $MODULE,    $RORO, null,
       "SystemColor",                "YoixModuleJFC",     $MODULE,    $RORO, null,

    //
    // Eventually consider importing JTree and JTable constants into
    // their own dictionaries. Means at least one of our applications
    // will break, but it should be easy to fix.
    //

       $YOIXCONSTANTSJTABLE,         "YOIX_\tYOIX_",      $READCLASS, $LR__, null,
       $YOIXCONSTANTSJTREE,          "YOIX_\tYOIX_",      $READCLASS, $LR__, null,

    //
    // Transfer handlers
    //

       T_TRANSFERHANDLER,            "12",                $DICT,      $L___, T_TRANSFERHANDLER,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $TRANSFERHANDLER,    $INTEGER,   $LR__, null,
       N_MINOR,                      "0",                 $INTEGER,   $LR__, null,
       N_ACTION,                     $COPY,               $INTEGER,   $RW_,  null,
       N_CANIMPORT,                  T_FUNCTION,          $NULL,      $RWX,  null,
       N_CREATETRANSFERABLE,         T_FUNCTION,          $NULL,      $RWX,  null,
       N_EXPORTASDRAG,               T_CALLABLE,          $NULL,      $L__X, null,
       N_EXPORTDONE,                 T_FUNCTION,          $NULL,      $RWX,  null,
       N_EXPORTTOCLIPBOARD,          T_CALLABLE,          $NULL,      $L__X, null,
       N_GETSOURCEACTIONS,           T_FUNCTION,          $NULL,      $RWX,  null,
       N_GETVISUALREPRESENTATION,    T_FUNCTION,          $NULL,      $RWX,  null,
       N_IMPORTDATA,                 T_FUNCTION,          $NULL,      $RWX,  null,
       N_PROPERTY,                   T_STRING,            $NULL,      $RW_,  null,

    //
    // Remaining Swing types.
    //

       T_BUTTONGROUP,                "7",                 $DICT,      $L___, T_BUTTONGROUP,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $BUTTONGROUP,        $INTEGER,   $LR__, null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_ITEMS,                      T_ARRAY,             $NULL,      $LR__, null,
       N_MODEL,                      "1",                 $INTEGER,   $RW_,  null,
       N_SIZE,                       "0",                 $INTEGER,   $LR__, null,
       N_SELECTED,                   T_OBJECT,            $NULL,      $RW_,  null,

       T_JTABLECOLUMN,               "37",                $DICT,      $L___, T_JTABLECOLUMN,
       null,                         "-1",                $GROWTO,    null,  null,
       N_ALIGNMENT,                  "-1",                $INTEGER,   $RW_,  null,
       N_ALTALIGNMENT,               "-1",                $INTEGER,   $RW_,  null,
       N_ALTBACKGROUND,              T_OBJECT,            $NULL,      $RW_,  null,
       N_ALTFONT,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ALTFOREGROUND,              T_OBJECT,            $NULL,      $RW_,  null,
       N_ALTTOOLTIPTEXT,             T_OBJECT,            $NULL,      $RW_,  null,
       N_ATTRIBUTES,                 T_DICT,              $NULL,      $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_CELLCOLORS,                 T_ARRAY,             $NULL,      $RW_,  null,
       N_CELLEDITOR,                 T_OBJECT,            $NULL,      $RW_,  null,
       N_DISABLEDBACKGROUND,         T_COLOR,             $NULL,      $RW_,  null,
       N_DISABLEDFOREGROUND,         T_COLOR,             $NULL,      $RW_,  null,
       N_EDIT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_EDITBACKGROUND,             T_COLOR,             $NULL,      $RW_,  null,
       N_EDITFOREGROUND,             T_COLOR,             $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $RW_,  null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_HEADER,                     T_STRING,            $NULL,      $RW_,  null,
       N_HEADERICONS,                T_OBJECT,            $NULL,      $RW_,  null,
       N_PICKSORTOBJECT,             T_FUNCTION,          $NULL,      $RWX,  null,
       N_PICKTABLEOBJECT,            T_FUNCTION,          $NULL,      $RWX,  null,
       N_SELECTIONBACKGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SELECTIONFOREGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SELECTIONMARK,              T_OBJECT,            $NULL,      $RW_,  null,
       N_STATE,                      "0",                 $INTEGER,   $LR__, null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       // N_TOOLTIP for backwards compatibility
       N_TOOLTIP,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_OBJECT,            $NULL,      $RW_,  null,
       N_TYPE,                       $COLUMNTYPE,         $INTEGER,   $RW_,  null,
       N_VALUE,                      "-1",                $INTEGER,   $LR__, null,
       N_VALUES,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_VIEW,                       "-1",                $INTEGER,   $LR__, null,
       N_VIEWS,                      T_ARRAY,             $NULL,      $LR__, null,
       N_VISIBLE,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_WIDTH,                      T_OBJECT,            $NULL,      $RW_,  null,

       T_JTREENODE,                  "16",                $DICT,      $L___, T_JTREENODE,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDERCOLOR,                T_COLOR,             $NULL,      $RW_,  null,
       N_CHILDREN,                   T_ARRAY,             $NULL,      $RW_,  null,
       N_CLOSEDICON,                 $DEFAULTICON,        $OBJECT,    $RW_,  null,
       N_CONTENT,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_LEAFICON,                   $DEFAULTICON,        $OBJECT,    $RW_,  null,
       N_OPENICON,                   $DEFAULTICON,        $OBJECT,    $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SELECTIONBACKGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SELECTIONFOREGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIP,                    T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,

       T_JMENU,                      "43",                $DICT,      $L___, T_JMENU,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JMENU,              $INTEGER,   $LR__, null,
       N_ALIGNMENT,                  $YOIX_LEFT,          $INTEGER,   $RW_,  null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CLICK,                      T_CALLABLE,          $NULL,      $L__X, null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GETENABLED,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_GETSTATE,                   T_CALLABLE,          $NULL,      $L__X, null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_ICONS,                      T_DICT,              $NULL,      $RW_,  null,
       N_ITEMS,                      T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MNEMONIC,                   "0",                 $INTEGER,   $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SETENABLED,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_SETSTATE,                   T_CALLABLE,          $NULL,      $L__X, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JPOPUPMENU,                 "38",                $DICT,      $L___, T_JPOPUPMENU,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JPOPUPMENU,         $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GETENABLED,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_GETSTATE,                   T_CALLABLE,          $NULL,      $L__X, null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_ICONS,                      T_DICT,              $NULL,      $RW_,  null,
       N_ITEMS,                      T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $POPUP_LAYER,        $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $DECLARE,   $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SETENABLED,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_SETSTATE,                   T_CALLABLE,          $NULL,      $L__X, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $FALSE,              $INTEGER,   $RW_,  null,

       T_JMENUBAR,                   "37",                $DICT,      $L___, T_JMENUBAR,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JMENUBAR,           $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GETENABLED,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_GETSTATE,                   T_CALLABLE,          $NULL,      $L__X, null,
       N_INSETS,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ITEMS,                      T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $FRAME_CONTENT_LAYER,$INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SETENABLED,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_SETSTATE,                   T_CALLABLE,          $NULL,      $L__X, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JMENUITEM,                  "48",                $DICT,      $L___, T_JMENUITEM,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JMENUITEM,          $INTEGER,   $LR__, null,
       N_ACCELERATOR,                T_OBJECT,            $NULL,      $RW_,  null,
       N_ALIGNMENT,                  $YOIX_LEFT,          $INTEGER,   $RW_,  null,
       N_ARMED,                      "0",                 $INTEGER,   $RW_,  null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CLICK,                      T_CALLABLE,          $NULL,      $L__X, null,
       N_COMMAND,                    T_STRING,            $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GROUP,                      T_BUTTONGROUP,       $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_ICONS,                      T_DICT,              $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MNEMONIC,                   "0",                 $INTEGER,   $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_PRESSED,                    "0",                 $INTEGER,   $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROLLOVER,                   "0",                 $INTEGER,   $RW_,  null,
       N_ROLLOVERENABLED,            $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SELECTED,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_STATE,                      $FALSE,              $INTEGER,   $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TEXTPOSITION,               $YOIX_TRAILING,      $INTEGER,   $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_TYPE,                       $STANDARDBUTTON,     $INTEGER,   $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

    //
    // Recently added N_STATE to T_JBUTTON, but we still support N_SELECTED
    // because that's what Yoix scripts were using. N_SELECTED now starts
    // as NULL, which means YoixBodyComponentSwing.java can tell if it was
    // initialized. Also shouldn't remove N_SELECTED because older scripts
    // could use N_SELECTED without initializing it. N_STATE is preferred,
    // but reading or writing either one accomplishes the same thing.
    //

       T_JBUTTON,                    "49",                $DICT,      $L___, T_JBUTTON,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JBUTTON,            $INTEGER,   $LR__, null,
       N_ALIGNMENT,                  $YOIX_CENTER,        $INTEGER,   $RW_,  null,
       N_ARMED,                      "0",                 $INTEGER,   $RW_,  null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CLICK,                      T_CALLABLE,          $NULL,      $L__X, null,
       N_COMMAND,                    T_STRING,            $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GROUP,                      T_BUTTONGROUP,       $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_ICONS,                      T_DICT,              $NULL,      $RW_,  null,
       N_INSETS,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MNEMONIC,                   "0",                 $INTEGER,   $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_PRESSED,                    "0",                 $INTEGER,   $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROLLOVER,                   "0",                 $INTEGER,   $RW_,  null,
       N_ROLLOVERENABLED,            $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SELECTED,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_STATE,                      $FALSE,              $INTEGER,   $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TEXTPOSITION,               $YOIX_TRAILING,      $INTEGER,   $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_TYPE,                       $STANDARDBUTTON,     $INTEGER,   $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JCANVAS,                    "45",                $DICT,      $L___, T_JCANVAS,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JCANVAS,            $INTEGER,   $LR__, null,
       N_AFTERPAN,                   T_CALLABLE,          $NULL,      $RWX,  null,
       N_AFTERZOOM,                  T_CALLABLE,          $NULL,      $RWX,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,            $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,            T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDERCOLOR,                T_COLOR,             $NULL,      $RW_,  null,
       N_BUTTONMASK,                 $BUTTON1MASK,        $INTEGER,   $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GRAPHICS,                   T_GRAPHICS,          $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIGIN,                     T_POINT,             $DECLARE,   $RW_,  null,
       N_PAINT,                      T_CALLABLE,          $NULL,      $RWX,  null,
       N_PANANDZOOM,                 "0",                 $OBJECT,    $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REPAINT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_STATE,                      "0",                 $INTEGER,   $RW_,  null,
       N_STICKY,                     $FALSE,              $INTEGER,   $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,
       N_ZOOM,                       T_CALLABLE,          $NULL,      $L__X, null,

       T_JCOLORCHOOSER,              "34",                $DICT,      $L___, T_JCOLORCHOOSER,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JCOLORCHOOSER,      $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COLOR,                      T_COLOR,             $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUEFLAGS,                "1",                 $INTEGER,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_RESET,                      T_COLOR,             $NULL,      $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JCOMBOBOX,                  "46",                $DICT,      $L___, T_JCOMBOBOX,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JCOMBOBOX,          $INTEGER,   $LR__, null,
       N_ALIGNMENT,                  $YOIX_LEFT,          $INTEGER,   $RW_,  null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMMAND,                    T_ARRAY,             $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_EDIT,                       $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSACTION,                $TRUE,               $INTEGER,   $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_ITEMS,                      T_ARRAY,             $NULL,      $RW_,  null,
       N_LABELS,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAPPINGS,                   T_ARRAY,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_PROTOTYPEVALUE,             T_OBJECT,            $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_ROWS,                       "0",                 $INTEGER,   $RW_,  null,
       N_SELECTED,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_SELECTEDINDEX,              "0",                 $INTEGER,   $LR__, null,
       N_SELECTEDENDS,               T_ARRAY,             $NULL,      $RW_,  null,
       N_SELECTEDLABEL,              T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JDESKTOPPANE,               "44",                $DICT,      $L___, T_JDESKTOPPANE,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JDESKTOPPANE,       $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,            $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,            T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_FRAMES,                     T_DICT,              $NULL,      $LR__, null,
       N_GRAPHICS,                   T_GRAPHICS,          $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_OBJECT,            $NULL,      $LR__, null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_PAINT,                      T_CALLABLE,          $NULL,      $RWX,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REPAINT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SELECTED,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JDIALOG,                    "49",                $DICT,      $L___, T_JDIALOG,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JDIALOG,            $INTEGER,   $LR__, null,
       N_AUTODISPOSE,                $FALSE,              $INTEGER,   $RW_,  null,
       N_AUTORAISE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,            $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,            T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DECORATIONSTYLE,            "1",                 $INTEGER,   $RW_,  null,
       N_DISPOSE,                    $FALSE,              $INTEGER,   $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FIRSTFOCUS,                 $TRUE,               $INTEGER,   $RW_,  null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_FRONTTOBACK,                $TRUE,               $INTEGER,   $RW_,  null,
       N_FULLSCREEN,                 $FALSE,              $INTEGER,   $RW_,  null,
       N_GLASSPANE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_GRAPHICS,                   T_GRAPHICS,          $NULL,      $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_BORDERLAYOUT,      $DECLARE,   $RW_,  null,
       N_LOCATION,                   $DEFAULTLOCATION,    $POINT,     $RW_,  null,
       N_MENUBAR,                    T_JMENUBAR,          $NULL,      $RW_,  null,
       N_MODAL,                      $FALSE,              $INTEGER,   $RW_,  null,
       N_NEXTCARD,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     $TRUE,               $OBJECT,    $RW_,  null,
       N_PAINT,                      T_CALLABLE,          $NULL,      $RWX,  null,
       N_PARENT,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_REPAINT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_RESIZABLE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SCREEN,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_SHAPE,                      T_PATH,              $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       $DEFAULTSIZE,        $DIMENSION, $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $FALSE,              $INTEGER,   $RW_,  null,

       T_JFILECHOOSER,               "46",                $DICT,      $L___, T_JFILECHOOSER,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JFILECHOOSER,       $INTEGER,   $LR__, null,
       N_APPROVEBUTTONMNEMONIC,      "0",                 $INTEGER,   $RW_,  null,
       N_APPROVEBUTTONTEXT,          T_STRING,            $NULL,      $RW_,  null,
       N_APPROVEBUTTONTOOLTIPTEXT,   T_STRING,            $NULL,      $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_BUTTONS,                    $TRUE,               $INTEGER,   $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DIRECTORY,                  T_STRING,            $NULL,      $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FILE,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FILESELECTIONMODE,          "3",                 $INTEGER,   $RW_,  null,
       N_FILTER,                     T_STRING,            $NULL,      $RW_,  null,
       N_FILTERS,                    T_ARRAY,             $NULL,      $RW_,  null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_HIDDENFILES,                $FALSE,              $INTEGER,   $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MODE,                       $YOIX_LOAD,          $INTEGER,   $RW_,  null,
       N_MODEL,                      "1",                 $INTEGER,   $RW_,  null,
       N_MULTIPLEMODE,               $FALSE,              $INTEGER,   $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUEFLAGS,                "1",                 $INTEGER,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TOYOIXPATH,                 $FALSE,              $INTEGER,   $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JFILEDIALOG,                "57",                $DICT,      $L___, T_JFILEDIALOG,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JFILEDIALOG,        $INTEGER,   $LR__, null,
       N_APPROVEBUTTONMNEMONIC,      "0",                 $INTEGER,   $RW_,  null,
       N_APPROVEBUTTONTEXT,          T_STRING,            $NULL,      $RW_,  null,
       N_APPROVEBUTTONTOOLTIPTEXT,   T_STRING,            $NULL,      $RW_,  null,
       N_AUTODISPOSE,                $FALSE,              $INTEGER,   $RW_,  null,
       N_AUTORAISE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,            $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,            T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DIRECTORY,                  T_STRING,            $NULL,      $RW_,  null,
       N_DISPOSE,                    $FALSE,              $INTEGER,   $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FILE,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FILESELECTIONMODE,          "3",                 $INTEGER,   $RW_,  null,
       N_FILTER,                     T_STRING,            $NULL,      $RW_,  null,
       N_FILTERS,                    T_ARRAY,             $NULL,      $RW_,  null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GLASSPANE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_GRAPHICS,                   T_GRAPHICS,          $NULL,      $RW_,  null,
       N_HIDDENFILES,                $FALSE,              $INTEGER,   $RW_,  null,
       N_LAYOUT,                     "0",                 $ARRAY,     $LR__, null,
       N_LAYOUTMANAGER,              T_BORDERLAYOUT,      $DECLARE,   $LR__, null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MENUBAR,                    T_JMENUBAR,          $NULL,      $RW_,  null,
       N_MODAL,                      $TRUE,               $INTEGER,   $RW_,  null,
       N_MODE,                       $YOIX_LOAD,          $INTEGER,   $RW_,  null,
       N_MODEL,                      "1",                 $INTEGER,   $RW_,  null,
       N_MULTIPLEMODE,               $FALSE,              $INTEGER,   $RW_,  null,
       N_OPAQUE,                     $FALSE,              $OBJECT,    $RW_,  null,
       N_OPAQUEFLAGS,                "1",                 $INTEGER,   $RW_,  null,
       N_PAINT,                      T_CALLABLE,          $NULL,      $RWX,  null,
       N_PARENT,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_REPAINT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_RESIZABLE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SCREEN,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TOYOIXPATH,                 $FALSE,              $INTEGER,   $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $FALSE,              $INTEGER,   $RW_,  null,

       T_JFRAME,                     "51",                $DICT,      $L___, T_JFRAME,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JFRAME,             $INTEGER,   $LR__, null,
       N_AUTODEICONIFY,              $TRUE,               $INTEGER,   $RW_,  null,
       N_AUTODISPOSE,                $FALSE,              $INTEGER,   $RW_,  null,
       N_AUTORAISE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,            $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,            T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DECORATIONSTYLE,            "1",                 $INTEGER,   $RW_,  null,
       N_DISPOSE,                    $FALSE,              $INTEGER,   $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FIRSTFOCUS,                 $TRUE,               $INTEGER,   $RW_,  null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_FRONTTOBACK,                $TRUE,               $INTEGER,   $RW_,  null,
       N_FULLSCREEN,                 $FALSE,              $INTEGER,   $RW_,  null,
       N_GLASSPANE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_GRAPHICS,                   T_GRAPHICS,          $NULL,      $RW_,  null,
       N_ICONIFIED,                  $FALSE,              $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_BORDERLAYOUT,      $DECLARE,   $RW_,  null,
       N_LOCATION,                   $DEFAULTLOCATION,    $POINT,     $RW_,  null,
       N_MAXIMIZED,                  $FALSE,              $INTEGER,   $RW_,  null,
       N_MENUBAR,                    T_JMENUBAR,          $NULL,      $RW_,  null,
       N_NEXTCARD,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     $TRUE,               $OBJECT,    $RW_,  null,
       N_PAINT,                      T_CALLABLE,          $NULL,      $RWX,  null,
       N_PARENT,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_REPAINT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_RESIZABLE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SCREEN,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_SHAPE,                      T_PATH,              $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       $DEFAULTSIZE,        $DIMENSION, $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $FALSE,              $INTEGER,   $RW_,  null,

       T_JINTERNALFRAME,             "54",                $DICT,      $L___, T_JINTERNALFRAME,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JINTERNALFRAME,     $INTEGER,   $LR__, null,
       N_AUTODISPOSE,                $FALSE,              $INTEGER,   $RW_,  null,
       N_AUTORAISE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,            $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,            T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CLOSABLE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_CLOSED,                     $FALSE,              $INTEGER,   $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DESKTOP,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_DISPOSE,                    $FALSE,              $INTEGER,   $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    $TRUE,               $OBJECT,    $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FIRSTFOCUS,                 $TRUE,               $INTEGER,   $RW_,  null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_FRONTTOBACK,                $TRUE,               $INTEGER,   $RW_,  null,
       N_GLASSPANE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_GRAPHICS,                   T_GRAPHICS,          $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_ICONIFIABLE,                $TRUE,               $INTEGER,   $RW_,  null,
       N_ICONIFIED,                  $FALSE,              $INTEGER,   $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_BORDERLAYOUT,      $DECLARE,   $RW_,  null,
       N_LOCATION,                   $DEFAULTLOCATION,    $POINT,     $RW_,  null,
       N_MAXIMIZABLE,                $TRUE,               $INTEGER,   $RW_,  null,
       N_MAXIMIZED,                  $FALSE,              $INTEGER,   $RW_,  null,
       N_MENUBAR,                    T_JMENUBAR,          $NULL,      $RW_,  null,
       N_NEXTCARD,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     $TRUE,               $OBJECT,    $RW_,  null,
       N_PAINT,                      T_CALLABLE,          $NULL,      $RWX,  null,
       N_PARENT,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_REPAINT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_RESIZABLE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       $DEFAULTSIZE,        $DIMENSION, $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $FALSE,              $INTEGER,   $RW_,  null,

       T_JLABEL,                     "37",                $DICT,      $L___, T_JLABEL,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JLABEL,             $INTEGER,   $LR__, null,
       N_ALIGNMENT,                  $YOIX_CENTER,        $INTEGER,   $RW_,  null,
       N_ALTALIGNMENT,               $YOIX_CENTER,        $INTEGER,   $RW_,  null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TEXTPOSITION,               $YOIX_TRAILING,      $INTEGER,   $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JLAYEREDPANE,               "42",                $DICT,      $L___, T_JLAYEREDPANE,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JLAYEREDPANE,       $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,            $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,            T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GRAPHICS,                   T_GRAPHICS,          $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_OBJECT,            $NULL,      $LR__, null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_PAINT,                      T_CALLABLE,          $NULL,      $RWX,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REPAINT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JLIST,                      "44",                $DICT,      $L___, T_JLIST,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JLIST,              $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COLUMNS,                    "0",                 $INTEGER,   $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_INDEX,                      "-1",                $INTEGER,   $RW_,  null,
       N_ITEMS,                      T_ARRAY,             $NULL,      $RW_,  null,
       N_LABELS,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAPPINGS,                   T_ARRAY,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MULTIPLEMODE,               $SINGLESELECTION,    $INTEGER,   $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_PROTOTYPEVALUE,             T_OBJECT,            $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_ROWS,                       "1",                 $INTEGER,   $RW_,  null,
       N_SCROLL,                     $YOIX_AS_NEEDED,     $INTEGER,   $RW_,  null,
       N_SELECTED,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_SELECTEDINDEX,              "0",                 $INTEGER,   $LR__, null,
       N_SELECTEDLABEL,              T_OBJECT,            $NULL,      $LR__,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_SIZECONTROL,                $JLIST_FLAGS,        $INTEGER,   $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JPANEL,                     "44",                $DICT,      $L___, T_JPANEL,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JPANEL,             $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BACKGROUNDHINTS,            $TILEHINT,           $INTEGER,   $RW_,  null,
       N_BACKGROUNDIMAGE,            T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_FRONTTOBACK,                $TRUE,               $INTEGER,   $RW_,  null,
       N_GRAPHICS,                   T_GRAPHICS,          $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_FLOWLAYOUT,        $DECLARE,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTCARD,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_PAINT,                      T_CALLABLE,          $NULL,      $RWX,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REPAINT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JPROGRESSBAR,               "38",                $DICT,      $L___, T_JPROGRESSBAR,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JPROGRESSBAR,       $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_INDETERMINATE,              $FALSE,              $INTEGER,   $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUM,                    "100",               $INTEGER,   $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUM,                    "0",                 $INTEGER,   $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIENTATION,                $YOIX_HORIZONTAL,    $INTEGER,   $RW_,  null,
       N_PERCENTCOMPLETE,            "0",                 $DOUBLE,    $LR__, null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALUE,                      "0",                 $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JSCROLLBAR,                 "39",                $DICT,      $L___, T_JSCROLLBAR,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JSCROLLBAR,         $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BLOCKINCREMENT,             "10",                $INTEGER,   $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPRESSEVENTS,             N_ADJUSTCHANGED,     $OBJECT,    $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUM,                    "100",               $INTEGER,   $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUM,                    "0",                 $INTEGER,   $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIENTATION,                $YOIX_VERTICAL,      $INTEGER,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SETVALUES,                  T_CALLABLE,          $NULL,      $L__X, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_UNITINCREMENT,              "1",                 $INTEGER,   $RW_,  null,
       N_VALUE,                      "0",                 $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLEAMOUNT,              "10",                $INTEGER,   $RW_,  null,

       T_JSCROLLPANE,                "43",                $DICT,      $L___, T_JSCROLLPANE,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JSCROLLPANE,        $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_EXTENT,                     T_DIMENSION,         $DECLARE,   $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_OBJECT,            $NULL,      $LR__, null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIGIN,                     T_POINT,             $DECLARE,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SCROLL,                     $YOIX_AS_NEEDED,     $INTEGER,   $RW_,  null,
       N_SETINCREMENT,               T_CALLABLE,          $NULL,      $L__X, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_SIZECONTROL,                $JSCROLLPANE_FLAGS,  $INTEGER,   $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VIEWPORT,                   T_RECTANGLE,         $DECLARE,   $LR__, null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JSEPARATOR,                 "32",                $DICT,      $L___, T_JSEPARATOR,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JSEPARATOR,         $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIENTATION,                $YOIX_HORIZONTAL,    $INTEGER,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JSLIDER,                    "46",                $DICT,      $L___, T_JSLIDER,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JSLIDER,            $INTEGER,   $LR__, null,
       N_ADJUSTING,                  $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_EXTENT,                     "0",                 $INTEGER,   $RW_,  null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_INVERTED,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_LABELS,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAJORTICKSPACING,           "10",                $INTEGER,   $RW_,  null,
       N_MAXIMUM,                    "100",               $INTEGER,   $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUM,                    "0",                 $INTEGER,   $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINORTICKSPACING,           "5",                 $INTEGER,   $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIENTATION,                $YOIX_VERTICAL,      $INTEGER,   $RW_,  null,
       N_PAINTLABELS,                $FALSE,              $INTEGER,   $RW_,  null,
       N_PAINTTICKS,                 $FALSE,              $INTEGER,   $RW_,  null,
       N_PAINTTRACK,                 $TRUE,               $INTEGER,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SETVALUES,                  T_CALLABLE,          $NULL,      $L__X, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_SNAPTOTICKS,                $FALSE,              $INTEGER,   $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_UNITINCREMENT,              "1",                 $INTEGER,   $RW_,  null,
       N_VALUE,                      "0",                 $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JSPLITPANE,                 "45",                $DICT,      $L___, T_JSPLITPANE,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JSPLITPANE,         $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CONTINUOUSLAYOUT,           $FALSE,              $INTEGER,   $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DIVIDERLOCATION,            T_OBJECT,            $NULL,      $RW_,  null,
       N_DIVIDERLOCKED,              $FALSE,              $INTEGER,   $RW_,  null,
       N_DIVIDERSIZE,                "-1",                $INTEGER,   $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_KEEPHIDDEN,                 $TRUE,               $INTEGER,   $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_OBJECT,            $NULL,      $LR__, null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_ONETOUCHEXPANDABLE,         $FALSE,              $INTEGER,   $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIENTATION,                $YOIX_HORIZONTAL,    $INTEGER,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $FALSE,              $INTEGER,   $RW_,  null,
       N_RESIZEWEIGHT,               "0.0",               $DOUBLE,    $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JTABBEDPANE,                "42",                $DICT,      $L___, T_JTABBEDPANE,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTABBEDPANE,        $INTEGER,   $LR__, null,
       N_ALIGNMENT,                  $YOIX_TOP,           $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_ICON,                       T_IMAGE,             $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_OBJECT,            $NULL,      $LR__, null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SCROLL,                     $YOIX_AS_NEEDED,     $INTEGER,   $RW_,  null,
       N_SELECTED,                   T_OBJECT,            $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_SIZECONTROL,                $JTABBEDPANE_FLAGS,  $INTEGER,   $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TITLE,                      T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRACKFOCUS,                 $FALSE,              $INTEGER,   $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JTABLE,                     "80",                $DICT,      $L___, T_JTABLE,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTABLE,             $INTEGER,   $LR__, null,
       N_ACTION,                     T_CALLABLE,          $NULL,      $L__X, null,
       N_AFTERSELECT,                T_CALLABLE,          $NULL,      $RWX,  null,
       N_ALLOWEDIT,                  T_CALLABLE,          $NULL,      $RWX,  null,
       N_ALTALIGNMENT,               "-1",                $INTEGER,   $RW_,  null,
       N_ALTBACKGROUND,              T_OBJECT,            $NULL,      $RW_,  null,
       N_ALTFONT,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ALTFOREGROUND,              T_OBJECT,            $NULL,      $RW_,  null,
       N_ALTGRIDCOLOR,               T_COLOR,             $NULL,      $RW_,  null,
       N_ALTTOOLTIPTEXT,             T_OBJECT,            $NULL,      $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDERCOLOR,                T_COLOR,             $NULL,      $RW_,  null,
       N_CELLCOLORS,                 T_ARRAY,             $NULL,      $RW_,  null,
       N_CLICKCOUNT,                 "1",                 $INTEGER,   $RW_,  null,
       N_COLUMNS,                    T_ARRAY,             $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_EDIT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_EDITBACKGROUND,             T_COLOR,             $NULL,      $RW_,  null,
       N_EDITFOREGROUND,             T_COLOR,             $NULL,      $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $RW_,  null,
       N_EXTENT,                     T_DIMENSION,         $DECLARE,   $LR__, null,
       N_FINDNEXTMATCH,              T_CALLABLE,          $NULL,      $L__X, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_GRIDCOLOR,                  T_COLOR,             $NULL,      $RW_,  null,
       N_GRIDSIZE,                   T_DIMENSION,         $NULL,      $RW_,  null,
       N_HEADERS,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_HEADERICONS,                T_OBJECT,            $NULL,      $RW_,  null,
       N_INPUTFILTER,                T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MODELTOVIEW,                T_CALLABLE,          $NULL,      $L__X, null,
       N_MULTIPLEMODE,               $SINGLESELECTION,    $INTEGER,   $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIGIN,                     T_POINT,             $DECLARE,   $RW_,  null,
       N_OUTPUTFILTER,               T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REORDER,                    $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_RESIZE,                     $FALSE,              $INTEGER,   $RW_,  null,
       N_RESIZEMODE,                 $RESIZEMODE,         $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_ROWHEIGHTADJUSTMENT,        "0.0",               $DOUBLE,    $RW_,  null,
       N_ROWS,                       "-1",                $INTEGER,   $RW_,  null,
       N_SCROLL,                     $YOIX_NEVER,         $INTEGER,   $RW_,  null,
       N_SELECTIONBACKGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SELECTIONFOREGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_SIZECONTROL,                $JTABLE_FLAGS,       $INTEGER,   $RW_,  null,
       N_SORTMAP,                    T_ARRAY,             $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIP,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_TOOLTIPS,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_TOOLTIPTEXT,                T_OBJECT,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_TYPES,                      T_ARRAY,             $NULL,      $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_USEEDITHIGHLIGHT,           $TRUE,               $INTEGER,   $RW_,  null,
       N_VALIDATOR,                  T_CALLABLE,          $NULL,      $RWX,  null,
       N_VALUES,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_VIEWPORT,                   T_RECTANGLE,         $DECLARE,   $LR__, null,
       N_VIEWROWCOUNT,               "0",                 $INTEGER,   $LR__, null,
       N_VIEWTOMODEL,                T_CALLABLE,          $NULL,      $L__X, null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLEWIDTH,               "0",                 $INTEGER,   $LR__, null,
       N_WIDTH,                      "0",                 $INTEGER,   $LR__, null,

       T_JTEXTAREA,                  "54",                $DICT,      $L___, T_JTEXTAREA,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTEXTAREA,          $INTEGER,   $LR__, null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CARET,                      "0",                 $INTEGER,   $RW_,  null,
       N_CARETALPHA,                 "0",                 $DOUBLE,    $RW_,  null,
       N_CARETCOLOR,                 T_COLOR,             $NULL,      $RW_,  null,
       N_CARETMODEL,                 "0",                 $INTEGER,   $RW_,  null,
       N_CARETOWNERCOLOR,            T_COLOR,             $NULL,      $RW_,  null,
       N_COLUMNS,                    "80",                $INTEGER,   $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_EDIT,                       $TRUE,               $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_HIGHLIGHTFLAGS,             "0",                 $INTEGER,   $RW_,  null,
       N_INSETS,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MODELTOVIEW,                T_CALLABLE,          $NULL,      $L__X, null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUEFLAGS,                "0",                 $INTEGER,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_ROWS,                       "5",                 $INTEGER,   $RW_,  null,
       N_SCROLL,                     $YOIX_AS_NEEDED,     $INTEGER,   $RW_,  null,
       N_SELECTED,                   T_STRING,            $NULL,      $RW_,  null,
       N_SELECTEDENDS,               T_ARRAY,             $NULL,      $RW_,  null,
       N_SELECTIONBACKGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SELECTIONFOREGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_SIZECONTROL,                $JTEXTAREA_FLAGS,    $INTEGER,   $RW_,  null,
       N_SUBTEXT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TEXTWRAP,                   "0",                 $INTEGER,   $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VIEWTOMODEL,                T_CALLABLE,          $NULL,      $L__X, null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JTEXTCANVAS,                "52",                $DICT,      $L___, T_JTEXTCANVAS,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTEXTCANVAS,        $INTEGER,   $LR__, null,
       N_ALIGNMENT,                  $YOIX_LEFT,          $INTEGER,   $RW_,  null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDERCOLOR,                T_COLOR,             $NULL,      $RW_,  null,
       N_BUTTONMASK,                 $BUTTON1MASK,        $INTEGER,   $RW_,  null,
       N_CELLSIZE,                   T_DIMENSION,         $DECLARE,   $LR__, null,
       N_COLUMNS,                    "0",                 $INTEGER,   $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_EXTENT,                     T_DIMENSION,         $DECLARE,   $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_INSETS,                     $MEDIUMINSETS,       $OBJECT,    $RW_,  null,
       N_IPAD,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIGIN,                     T_POINT,             $DECLARE,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_ROWS,                       "0",                 $INTEGER,   $RW_,  null,
       N_SAVEGRAPHICS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_STATE,                      $FALSE,              $INTEGER,   $RW_,  null,
       N_STICKY,                     $FALSE,              $INTEGER,   $RW_,  null,
       N_SUBTEXT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_SYNCCOUNT,                  "0",                 $INTEGER,   $LR__, null,
       N_SYNCVIEWPORT,               T_CALLABLE,          $NULL,      $RWX,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TEXTMODE,                   $YOIX_WORDMODE,      $INTEGER,   $RW_,  null,
       N_TEXTWRAP,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VIEWPORT,                   T_RECTANGLE,         $DECLARE,   $LR__, null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JTEXTFIELD,                 "51",                $DICT,      $L___, T_JTEXTFIELD,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTEXTFIELD,         $INTEGER,   $LR__, null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CARET,                      "0",                 $INTEGER,   $RW_,  null,
       N_CARETALPHA,                 "0",                 $DOUBLE,    $RW_,  null,
       N_CARETCOLOR,                 T_COLOR,             $NULL,      $RW_,  null,
       N_CARETMODEL,                 "0",                 $INTEGER,   $RW_,  null,
       N_CARETOWNERCOLOR,            T_COLOR,             $NULL,      $RW_,  null,
       N_COLUMNS,                    "10",                $INTEGER,   $RW_,  null,
       N_COMMAND,                    T_STRING,            $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ECHO,                       "0",                 $INTEGER,   $RW_,  null,
       N_EDIT,                       $TRUE,               $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_HIGHLIGHTFLAGS,             "0",                 $INTEGER,   $RW_,  null,
       N_INSETS,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MODELTOVIEW,                T_CALLABLE,          $NULL,      $L__X, null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SELECTED,                   T_STRING,            $NULL,      $RW_,  null,
       N_SELECTEDENDS,               T_ARRAY,             $NULL,      $RW_,  null,
       N_SELECTIONBACKGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SELECTIONFOREGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_SUBTEXT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VIEWTOMODEL,                T_CALLABLE,          $NULL,      $L__X, null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JTEXTPANE,                  "56",                $DICT,      $L___, T_JTEXTPANE,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTEXTPANE,          $INTEGER,   $LR__, null,
       N_ANCHOR,                     T_STRING,            $NULL,      $RW_,  null,
       N_ALIGNMENT,                  $YOIX_LEFT,          $INTEGER,   $RW_,  null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BASE,                       T_STRING,            $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_CARET,                      "0",                 $INTEGER,   $RW_,  null,
       N_CARETALPHA,                 "0",                 $DOUBLE,    $RW_,  null,
       N_CARETCOLOR,                 T_COLOR,             $NULL,      $RW_,  null,
       N_CARETMODEL,                 "0",                 $INTEGER,   $RW_,  null,
       N_CARETOWNERCOLOR,            T_COLOR,             $NULL,      $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_EDIT,                       $TRUE,               $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_HIGHLIGHTFLAGS,             "0",                 $INTEGER,   $RW_,  null,
       N_INSETS,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MODE,                       "0",                 $INTEGER,   $RW_,  null,
       N_MODELTOVIEW,                T_CALLABLE,          $NULL,      $L__X, null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUEFLAGS,                "0",                 $INTEGER,   $RW_,  null,
       N_PAGE,                       T_STRING,            $NULL,      $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SCROLL,                     $YOIX_AS_NEEDED,     $INTEGER,   $RW_,  null,
       N_SELECTED,                   T_STRING,            $NULL,      $RW_,  null,
       N_SELECTEDENDS,               T_ARRAY,             $NULL,      $RW_,  null,
       N_SELECTIONBACKGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SELECTIONFOREGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_SIZECONTROL,                $JTEXTPANE_FLAGS,    $INTEGER,   $RW_,  null,
       N_SUBTEXT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VIEWTOMODEL,                T_CALLABLE,          $NULL,      $L__X, null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JTEXTTERM,                  "52",                $DICT,      $L___, T_JTEXTTERM,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTEXTTERM,          $INTEGER,   $LR__, null,
       N_AFTERNEWLINE,               T_FUNCTION,          $NULL,      $RWX,  null,
       N_ALIGNMENT,                  $YOIX_LEFT,          $INTEGER,   $RW_,  null,
       N_AUTOTRIM,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDERCOLOR,                T_COLOR,             $NULL,      $RW_,  null,
       N_CELLSIZE,                   T_DIMENSION,         $DECLARE,   $LR__, null,
       N_COLUMNS,                    "0",                 $INTEGER,   $RW_,  null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_EDIT,                       $TRUE,               $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_EXTENT,                     T_DIMENSION,         $DECLARE,   $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_INSETS,                     $MEDIUMINSETS,       $OBJECT,    $RW_,  null,
       N_IPAD,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIGIN,                     T_POINT,             $DECLARE,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_PROMPT,                     T_STRING,            $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_ROWS,                       "0",                 $INTEGER,   $RW_,  null,
       N_SAVEGRAPHICS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_SAVELINES,                  "0",                 $INTEGER,   $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_STATE,                      $FALSE,              $INTEGER,   $RW_,  null,
       N_SUBTEXT,                    T_CALLABLE,          $NULL,      $L__X, null,
       N_SYNCCOUNT,                  "0",                 $INTEGER,   $LR__, null,
       N_SYNCVIEWPORT,               T_CALLABLE,          $NULL,      $RWX,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TEXT,                       T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VIEWPORT,                   T_RECTANGLE,         $DECLARE,   $LR__, null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JTOOLBAR,                   "38",                $DICT,      $L___, T_JTOOLBAR,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTOOLBAR,           $INTEGER,   $LR__, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FLOATABLE,                  $TRUE,               $INTEGER,   $RW_,  null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_INSETS,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LAYOUT,                     T_ARRAY,             $NULL,      $RW_,  null,
       N_LAYOUTMANAGER,              T_OBJECT,            $NULL,      $LR__, null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_ORIENTATION,                $YOIX_HORIZONTAL,    $INTEGER,   $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $FALSE,              $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPTEXT,                T_STRING,            $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VALIDATE,                   $TRUE,               $INTEGER,   $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

       T_JTREE,                      "47",                $DICT,      $L___, T_JTREE,
       null,                         "-1",                $GROWTO,    null,  null,
       N_MAJOR,                      $JCOMPONENT,         $INTEGER,   $LR__, null,
       N_MINOR,                      $JTREE,              $INTEGER,   $LR__, null,
       N_ACTION,                     T_CALLABLE,          $NULL,      $L__X, null,
       N_BACKGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_BORDER,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_BORDERCOLOR,                T_COLOR,             $NULL,      $RW_,  null,
       N_CLOSEDICON,                 $DEFAULTICON,        $OBJECT,    $RW_,  null,
       N_COMPONENTS,                 T_DICT,              $NULL,      $LR__, null,
       N_CURSOR,                     $STANDARDCURSOR,     $OBJECT,    $RW_,  null,
       N_DOUBLEBUFFERED,             T_OBJECT,            $NULL,      $RW_,  null,
       N_DRAGENABLED,                $FALSE,              $INTEGER,   $RW_,  null,
       N_EDIT,                       $FALSE,              $INTEGER,   $RW_,  null,
       N_ENABLED,                    T_OBJECT,            $NULL,      $RW_,  null,
       N_EXPANDSSELECTEDNODES,       $FALSE,              $INTEGER,   $RW_,  null,
       N_ETC,                        T_OBJECT,            $NULL,      $LR__, null,
       N_FOCUSABLE,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_FOCUSOWNER,                 $FALSE,              $INTEGER,   $LR__, null,
       N_FONT,                       T_OBJECT,            $NULL,      $RW_,  null,
       N_FOREGROUND,                 T_COLOR,             $NULL,      $RW_,  null,
       N_ITEM,                       T_CALLABLE,          $NULL,      $L__X, null,
       N_LAYER,                      $DEFAULT_LAYER,      $INTEGER,   $RW_,  null,
       N_LEAFICON,                   $DEFAULTICON,        $OBJECT,    $RW_,  null,
       N_LOCATION,                   T_POINT,             $NULL,      $RW_,  null,
       N_MAXIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MINIMUMSIZE,                T_DIMENSION,         $NULL,      $RW_,  null,
       N_MULTIPLEMODE,               $SINGLESELECTION,    $INTEGER,   $RW_,  null,
       N_NEXTFOCUS,                  T_OBJECT,            $NULL,      $RW_,  null,
       N_OPAQUE,                     T_OBJECT,            $NULL,      $RW_,  null,
       N_OPENICON,                   $DEFAULTICON,        $OBJECT,    $RW_,  null,
       N_POPUP,                      T_JPOPUPMENU,        $NULL,      $RW_,  null,
       N_PREFERREDSIZE,              T_DIMENSION,         $NULL,      $RW_,  null,
       N_REQUESTFOCUS,               $FALSE,              $INTEGER,   $RW_,  null,
       N_REQUESTFOCUSENABLED,        $TRUE,               $INTEGER,   $RW_,  null,
       N_ROOT,                       T_OBJECT,            $NULL,      $LR__, null,
       N_ROOTHANDLE,                 $TRUE,               $INTEGER,   $RW_,  null,
       N_SCROLLSONEXPAND,            $TRUE,               $INTEGER,   $RW_,  null,
       N_SELECTIONBACKGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SELECTIONFOREGROUND,        T_COLOR,             $NULL,      $RW_,  null,
       N_SHOWING,                    $FALSE,              $INTEGER,   $LR__, null,
       N_SIZE,                       T_DIMENSION,         $NULL,      $RW_,  null,
       N_TAG,                        T_STRING,            $NULL,      $RW_,  null,
       N_TOOLTIPS,                   $FALSE,              $INTEGER,   $RW_,  null,
       N_TOOLTIPTEXT,                T_OBJECT,            $NULL,      $LR__, null,
       N_TOP,                        T_JTREENODE,         $NULL,      $RW_,  null,
       N_TRANSFERHANDLER,            $NULLTRANSFERHANDLER,$OBJECT,    $RW_,  null,
       N_UIMKEY,                     T_STRING,            $NULL,      $RW_,  null,
       N_VISIBLE,                    $TRUE,               $INTEGER,   $RW_,  null,

    //
    // Definition of T_JWINDOW has moved into YoixModuleSwingExtension.java.
    //

    //
    // Java methods can't be more than 65535 bytes. If $module gets too
    // big for the method that's responsible for static initialization
    // you will have to move part of the table to a new class file that
    // extends YoixModuleSwing and pull the rest of the definitions in
    // using the $INCLUDE directive that's supported by YoixModule.java.
    // For example, move part of $module to YoixModuleSwingExtension.java
    // and add something like,
    //
    //   YOIXPACKAGE + ".YoixModuleSwingExtension", null, $INCLUDE, null, null,
    //
    // to this $module table and you'll include the $module table that's
    // defined in YoixModuleSwingExtension.java.
    //

       YOIXPACKAGE + ".YoixModuleSwingExtension",null, $INCLUDE,   null,  null,
    };

    ///////////////////////////////////
    //
    // YoixModuleSwing Methods
    //
    ///////////////////////////////////

    public static YoixObject
    getHighlights(YoixObject arg[]) {

	Highlighter  highlighter;
	Object       highlights[] = null;
	Object       comp;
	int          fields;
	int          flags;

	if (arg.length == 1 || arg.length == 2 || arg.length == 3) {
	    if (arg[0].isComponent()) {
		comp = arg[0].getManagedObject();
		if (comp instanceof JTextComponent) {
		    if (arg.length < 2 || arg[1].isInteger()) {
			if (arg.length < 3 || arg[2].isInteger()) {
			    fields = (arg.length > 1) ? arg[1].intValue() : 2;
			    flags = (arg.length > 2) ? arg[2].intValue() : 0;
			    highlighter = ((JTextComponent)comp).getHighlighter();
			    if (highlighter instanceof YoixSwingHighlighter)
				highlights = ((YoixSwingHighlighter)highlighter).getHighlights(fields, flags);
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixMisc.copyIntoArray(highlights));
    }


    public static YoixObject
    getScrollerSize(YoixObject arg[]) {

	Dimension  size = null;
	Object     body;

	if (arg[0].isComponent()) {
	    body = arg[0].body();
	    if (body instanceof YoixBodyComponentSwing)
		size = ((YoixBodyComponentSwing)body).getScrollerSize();
	} else VM.badArgument(0);

	return(size != null ? YoixMakeScreen.yoixDimension(size) : YoixObject.newDimension());
    }


    public static YoixObject
    getSwingThreadSafe(YoixObject arg[]) {

	return(YoixObject.newInt(YoixBodyComponentSwing.getThreadSafe()));
    }


    public static YoixObject
    getViewportSize(YoixObject arg[]) {

	Dimension  size = null;
	boolean    adjusted;
	Object     body;

	//
	// Decided not to document the optional adjusted argument because
	// looks like we get the same answer with or without it using all
	// allowed versions of Java.
	//

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isComponent()) {
		if (arg.length == 1 || arg[1].isInteger()) {
		    body = arg[0].body();
		    if (body instanceof YoixBodyComponentSwing) {
			adjusted = (arg.length == 1) ? false : arg[1].booleanValue();
			size = ((YoixBodyComponentSwing)body).getViewportSize(adjusted);
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(size != null ? YoixMakeScreen.yoixDimension(size) : YoixObject.newDimension());
    }


    public static YoixObject
    setHighlights(YoixObject arg[]) {

	Highlighter  highlighter;
	YoixObject   item;
	YoixObject   obj;
	ArrayList    list;
	Object       comp;
	Object       ends[];
	Color        color;
	int          fields;
	int          start;
	int          end;
	int          length;
	int          limit;
	int          n;

	if (arg.length == 2 || arg.length == 3) {
	    if (arg[0].isComponent()) {
		comp = arg[0].getManagedObject();
		if (comp instanceof JTextComponent) {
		    if (arg[1].isArray() || arg[1].isNull()) {
			if (arg.length == 2 || arg[2].isInteger()) {
			    fields = (arg.length == 3) ? arg[2].intValue() : 2;
			    if (fields >= 2 && fields <= 4) {
				highlighter = ((JTextComponent)comp).getHighlighter();
				if (highlighter instanceof YoixSwingHighlighter) {
				    obj = arg[1];
				    limit = ((JTextComponent)comp).getText().length();
				    start = 0;
				    end = limit;
				    list = new ArrayList(obj.sizeof());
				    length = obj.length();
				    for (n = obj.offset(); n < length; n += fields) {
					//
					// Existence test is a recent addition that
					// helps in one application.
					//
					if (obj.defined(n) && obj.defined(n+1)) {
					    start = Math.min(Math.max(obj.getInt(n, 0), 0), limit);
					    end = Math.min(Math.max(obj.getInt(n+1, 0), start), limit);
					    if (fields > 2) {
						if ((item = obj.getObject(n+2)) != null) {
						    if (item.isNull() || item.isColor())
							color = YoixMake.javaColor(item);
						    else color = null;
						} else color = null;
					    } else color = null;

					    list.add(new Integer(start));
					    list.add(new Integer(end));
					    list.add(color);
					    list.add(new Integer(fields > 3 ? obj.getInt(n+3, 0) : 0));
					}
				    }
				    ends = list.toArray();
				    ((YoixSwingHighlighter)highlighter).setHighlights(ends, 4);
				}
			    } else VM.badArgument(2);
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		} else VM.badArgument(0);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    setSwingThreadSafe(YoixObject arg[]) {

	boolean  state = true;
	boolean  lock = false;

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].isNumber()) {
		if (arg.length == 1 || arg[1].isNumber()) {
		    state = YoixBodyComponentSwing.setThreadSafe(
			arg[0].booleanValue(), 
			arg.length == 1 ? false : arg[1].booleanValue()
		    );
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newInt(state));
    }


    public static YoixObject
    showConfirmDialog(YoixObject arg[]) {

	YoixObject  obj;
	Component   comp = null;
	String      options[] = null;
	String      message;
	String      title = UIManager.getString("OptionPane.titleText");
	Icon        icon = null;
	int         optiontype = JOptionPane.YES_NO_CANCEL_OPTION;
	int         messagetype = JOptionPane.QUESTION_MESSAGE;
	int         ret_val = -1;
	int         default_option = -1;
	int         len;
	int         n;

	if (arg.length >= 2 && arg.length <= 5) {
	    if (arg[0].isNull() || arg[0].isComponent()) {
		if (arg[0].notNull())
		    comp = (Component)arg[0].getManagedObject();
		if (arg[1].notNull() && arg[1].isString()) {
		    message = arg[1].stringValue();
		    if (arg.length > 2) {
			if (arg[2].isNull() || arg[2].isString()) {
			    if (arg[2].notNull())
				title = arg[2].stringValue();
			    if (arg.length > 3) {
				if (arg[3].isInteger() || arg[3].isArray() || arg[3].isString() || arg[3].isNull()) {
				    if (arg[3].sizeof() > 0) {
					if (arg[3].isArray()) {
					    len = arg[3].length();
					    options = new String[len];
					    default_option = arg[3].offset();
					    for (n = 0; n < len; n++) {
						obj = arg[3].get(n, false);
						if (obj.notNull() && obj.isString())
						    options[n] = obj.stringValue();
						else VM.badArgumentValue(3, n);
					    }
					} else if (arg[3].isString()) {
					    options = new String[] {arg[3].stringValue()};
					    default_option = 0;
					}
				    } else if (arg[3].isInteger())
					optiontype = YoixBodyComponent.jfcInt("JOptionPaneOptionType", arg[3].intValue());
				    if (arg.length == 5) {
					if (arg[4].isNull() || arg[4].isImage()) {
					    if (arg[4].notNull())
						icon = YoixMake.javaIcon(arg[4]);
					} else if (arg[4].isInteger())
					    messagetype = YoixBodyComponent.jfcInt("JOptionPaneMessageType", arg[4].intValue());
					else VM.badArgument(4);
				    }
				} else VM.badArgument(3);
			    }
			} else VM.badArgument(2);
		    }
		    if (options == null) {
			if (comp instanceof JLayeredPane || comp instanceof JInternalFrame) {
			    ret_val = JOptionPane.showInternalConfirmDialog(
				comp,
				message,
				title,
				optiontype,
				messagetype,
				icon
			    );
			} else {
			    ret_val = JOptionPane.showConfirmDialog(
				comp,
				message,
				title,
				optiontype,
				messagetype,
				icon
			    );
			}
		    } else {
			if (comp instanceof JLayeredPane || comp instanceof JInternalFrame) {
			    ret_val = JOptionPane.showInternalOptionDialog(
				comp,
				message,
				title,
				optiontype,
				messagetype,
				icon,
				options,
				options[default_option]
			    );
			} else {
			    ret_val = JOptionPane.showOptionDialog(
				comp,
				message,
				title,
				optiontype,
				messagetype,
				icon,
				options,
				options[default_option]
			    );
			}
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	if (options == null) {
	    switch (ret_val) {
		case JOptionPane.CANCEL_OPTION:
		    ret_val = (optiontype == JOptionPane.OK_CANCEL_OPTION) ? 1 : 2;
		    break;

		case JOptionPane.NO_OPTION:
		    ret_val = 1;
		    break;

		case JOptionPane.YES_OPTION:
		    ret_val = 0;
		    break;

		default:
		    if (ret_val == JOptionPane.OK_OPTION)	// just in case
			ret_val = 0;
		    else ret_val = -1;
		    break;
	    }
	}

	return(YoixObject.newInt(ret_val));
    }


    public static YoixObject
    showInputDialog(YoixObject arg[]) {

	YoixObject  obj;
	Component   comp = null;
	String      selections[] = null;
	String      message;
	String      title = UIManager.getString( "OptionPane.inputDialogTitle");
	Object      ret_val = null;
	Icon        icon = null;
	int         messagetype = JOptionPane.QUESTION_MESSAGE;
	int         default_selection = -1;
	int         len;
	int         n;

	if (arg.length >= 2 && arg.length <= 5) {
	    if (arg[0].isNull() || arg[0].isComponent()) {
		if (arg[0].notNull())
		    comp = (Component)arg[0].getManagedObject();
		if (arg[1].notNull() && arg[1].isString()) {
		    message = arg[1].stringValue();
		    if (arg.length > 2) {
			if (arg[2].isNull() || arg[2].isString()) {
			    if (arg[2].notNull())
				title = arg[2].stringValue();
			    if (arg.length > 3) {
				if (arg[3].isArray() || arg[3].isString() || arg[3].isNull()) {
				    if (arg[3].sizeof() > 0) {
					if (arg[3].isArray()) {
					    len = arg[3].length();
					    selections = new String[len];
					    default_selection = arg[3].offset();
					    for (n = 0; n < len; n++) {
						obj = arg[3].get(n, false);
						if (obj.notNull() && obj.isString())
						    selections[n] = obj.stringValue();
						else VM.badArgumentValue(3, n);
					    }
					} else if (arg[3].isString()) {
					    selections = new String[] {arg[3].stringValue()};
					    default_selection = 0;
					}
				    }
				    if (arg.length == 5) {
					if (arg[4].isNull() || arg[4].isImage()) {
					    if (arg[4].notNull())
						icon = YoixMake.javaIcon(arg[4]);
					} else if (arg[4].isInteger())
					    messagetype = YoixBodyComponent.jfcInt("JOptionPaneMessageType", arg[4].intValue());
					else VM.badArgument(4);
				    }
				} else VM.badArgument(3);
			    }
			} else VM.badArgument(2);
		    }
		    if (comp instanceof JLayeredPane || comp instanceof JInternalFrame) {
			ret_val = JOptionPane.showInternalInputDialog(
			    comp,
			    message,
			    title,
			    messagetype,
			    icon,
			    selections,
			    selections == null ? null : selections[default_selection]
			);
		    } else {
			ret_val = JOptionPane.showInputDialog(
			    comp,
			    message,
			    title,
			    messagetype,
			    icon,
			    selections,
			    selections == null ? null : selections[default_selection]
			);
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(ret_val == null ? YoixObject.newNull() : YoixObject.newString(ret_val.toString()));
    }


    public static YoixObject
    showMessageDialog(YoixObject arg[]) {

	Component  comp = null;
	String     message;
	String     title = UIManager.getString("OptionPane.messageDialogTitle");
	Icon       icon = null;
	int        messagetype = JOptionPane.INFORMATION_MESSAGE;

	if (arg.length >= 2 && arg.length <= 4) {
	    if (arg[0].isNull() || arg[0].isComponent()) {
		if (arg[0].notNull())
		    comp = (Component)arg[0].getManagedObject();
		if (arg[1].notNull() && arg[1].isString()) {
		    message = arg[1].stringValue();
		    if (arg.length > 2) {
			if (arg[2].isNull() || arg[2].isString()) {
			    if (arg[2].notNull())
				title = arg[2].stringValue();
			    if (arg.length == 4) {
				if (arg[3].isNull() || arg[3].isImage()) {
				    if (arg[3].notNull())
					icon = YoixMake.javaIcon(arg[3]);
				} else if (arg[3].isInteger())
				    messagetype = YoixBodyComponent.jfcInt("JOptionPaneMessageType", arg[3].intValue());
				else VM.badArgument(3);
			    }
			} else VM.badArgument(2);
		    }
		    if (comp instanceof JLayeredPane || comp instanceof JInternalFrame) {
			JOptionPane.showInternalMessageDialog(
			    comp,
			    message,
			    title,
			    messagetype,
			    icon
			);
		    } else {
			JOptionPane.showMessageDialog(
			    comp,
			    message,
			    title,
			    messagetype,
			    icon
			);
		    }
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixObject.newEmpty());
    }
}

