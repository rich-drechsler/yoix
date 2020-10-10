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
import java.io.*;

abstract
class YoixModuleVM extends YoixModule

{

    static String  $MODULENAME = null;

    static Integer  $FLUSHWRITES = new Integer(YoixConstantsStream.FLUSHWRITES);
    static String   $JVMENCODING = YoixConverter.getJVMEncoding();
    static Integer  $NORM_PRIORITY = new Integer(Thread.NORM_PRIORITY);
    static Integer  $LOW_PRIORITY = new Integer((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY)/2);
    static String   $ISO88591 = YoixConverter.getISO88591Encoding();
    static Integer  $ZIPPED_DEFAULT = new Integer(EXECUTE_ZIPPED_DEFAULT);

    //
    // Types listed in RESTRICTED_TYPES[] are automatically availible in
    // every restricted block. We intentionally omitted important types,
    // like T_FILE and T_THREAD, and don't think they should be added to
    // the list!!
    //

    static final String RESTRICTED_TYPES[] = {
	T_ARRAY,
	T_BUILTIN,
	T_CALLABLE,
	T_DICT,
	T_DOUBLE,
	T_FUNCTION,
	T_INT,
	T_NUMBER,
	T_OBJECT,
	T_POINTER,
	T_STREAM,
	T_STRING,

	T_COLOR,
	T_DIMENSION,
	T_INSETS,
	T_MENU,
	T_POINT,
	T_RECTANGLE,
	T_MATRIX,
    };

    //
    // The values assigned to the entries in $init[] can be changed by
    // YoixModule.tuner() and that table, if it exists, is read before
    // $module[]. Currently only used to initialize values that can be
    // set by property files or command line options.
    //

    static Object  $init[] = {
    //
    // NAME       ARG              COMMAND     MODE   REFERENCE
    // ----       ---              -------     ----   ---------
       null,      $FALSE,          $INTEGER,   $RW_,  N_ACCEPTCERTIFICATES,
       null,      $TRUE,           $INTEGER,   $RW_,  N_ADDTAGS,
       null,      $FALSE,          $INTEGER,   $RW_,  N_APPLET,
       null,      $TRUE,           $INTEGER,   $RW_,  N_AUTOIMPORT,
       null,      $TRUE,           $INTEGER,   $RW_,  N_BIND,
       null,      "0",             $INTEGER,   $RW_,  N_BUTTONMODEL,
       null,      $TRUE,           $INTEGER,   $RW_,  N_CREATE,
       null,      "0",             $INTEGER,   $RW_,  N_DEBUG,
       null,      $FALSE,          $INTEGER,   $RW_,  N_DEBUGGING,
       null,      "0",             $DOUBLE,    $LR__, N_DIAGONAL,
       null,      T_STRING,        $NULL,      $LR__, N_DEFAULTFONT,
       null,      "1",             $INTEGER,   $RW_,  N_DUMPDEPTH,
       null,      $ISO88591,       $STRING,    $RW_,  N_STREAMENCODING,
       null,      $JVMENCODING,    $STRING,    $RW_,  N_PARSERENCODING,
       null,      "1",             $INTEGER,   $RW_,  N_EVENTFLAGS,
       null,      "1",             $INTEGER,   $RW_,  N_EXITMODEL,
       null,      $FALSE,          $INTEGER,   $RW_,  N_FIXFONTS,
       null,      $FALSE,          $INTEGER,   $RW_,  N_FIXFONTMETRICS,
       null,      "0",             $INTEGER,   $RW_,  N_FLAGS,
       null,      "1.0",           $DOUBLE,    $RW_,  N_FONTMAGNIFICATION,
       null,      "1",             $INTEGER,   $RW_,  N_MENUFLAGS,
       null,      T_STRING,        $NULL,      $LR__, N_SECURITYOPTIONS,
       null,      TMPDIR,          $STRING,    $RW_,  N_TMPDIR,
       null,      "0",             $INTEGER,   $RW_,  N_TRACE,
       null,      $ZIPPED_DEFAULT, $INTEGER,   $RW_,  N_ZIPPED,
    };

    static Object  $module[] = {
    //
    // NAME                   ARG                  COMMAND     MODE   REFERENCE
    // ----                   ---                  -------     ----   ---------
       null,                  "17",                $DICT,      $RORO, N_RESERVED,
       null,                  "-1",                $GROWTO,    null,  null,
       N_NULL,                T_POINTER,           $NULL,      $LR__, null,
       N_NULL2,               T_POINTER,           $NULL,      $LR__, null,
       N_TRUE,                $TRUE,               $INTEGER,   $LR__, null,
       N_TRUE2,               $TRUE,               $INTEGER,   $LR__, null,
       N_FALSE,               $FALSE,              $INTEGER,   $LR__, null,
       N_FALSE2,              $FALSE,              $INTEGER,   $LR__, null,
       N_EOF,                 $YOIX_EOF,           $INTEGER,   $LR__, null,
       N_STDIN,               T_STREAM,            $NULL,      $LR__, null,
       N_STDOUT,              T_STREAM,            $NULL,      $LR__, null,
       N_STDERR,              T_STREAM,            $NULL,      $LR__, null,
       N_ABORT,               "-1",                $BUILTIN,   $LR_X, null,
       N_DEFINED,             "-1",                $BUILTIN,   $LR_X, null,
       N_EVAL,                "-1",                $BUILTIN,   $LR_X, null,
       N_EXECUTE,             "-1",                $BUILTIN,   $LR_X, null,
       N_TOSTRING,            "-1",                $BUILTIN,   $LR_X, null,
       N_UNROLL,              "-1",                $BUILTIN,   $LR_X, null,

    //
    // Type definitions - loaded early because some of them (e.g., T_COLOR)
    // are needed later on.
    //

       null,                  "172",               $LIST,      $RORO, N_TYPEDICT,
       T_ARRAY,               $ARRAY,              $INTEGER,   $L___, null,
       T_BUILTIN,             $BUILTIN,            $INTEGER,   $L___, null,
       T_CALLABLE,            $CALLABLE,           $INTEGER,   $L___, null,
       T_DICT,                $DICT,               $INTEGER,   $L___, null,
       T_DOUBLE,              $DOUBLE,             $INTEGER,   $L___, null,
       T_ELEMENT,             $ELEMENT,            $INTEGER,   $L___, null,
       T_FUNCTION,            $FUNCTION,           $INTEGER,   $L___, null,
       T_INT,                 $INTEGER,            $INTEGER,   $L___, null,
       T_NUMBER,              $NUMBER,             $INTEGER,   $L___, null,
       T_OBJECT,              $OBJECT,             $INTEGER,   $L___, null,
       T_POINTER,             $POINTER,            $INTEGER,   $L___, null,
       T_STREAM,              $STREAM,             $INTEGER,   $L___, null,
       T_STRING,              $STRING,             $INTEGER,   $L___, null,

       T_FILE,                "22",                $DICT,      $L___, T_FILE,
       null,                  T_STREAM,            $TYPENAME,  null,  null,
       N_MAJOR,               $STREAM,             $INTEGER,   $LR__, null,
       N_MINOR,               $FILE,               $INTEGER,   $LR__, null,
       N_MODE,                "r",                 $OBJECT,    $RW_,  null,
       N_NAME,                T_STRING,            $NULL,      $RW_,  null,
       N_AUTOREADY,           $FALSE,              $INTEGER,   $RW_,  null,
       N_BUFSIZE,             $BUFSIZ,             $INTEGER,   $RW_,  null,
       N_CALLBACK,            T_CALLABLE,          $NULL,      $L__X, null,
       N_CHECKSUM,            "0",                 $DOUBLE,    $RW_,  null,
       N_CIPHER,              T_OBJECT,            $NULL,      $RW_,  null,
       N_ENCODING,            T_STRING,            $NULL,      $RW_,  null,
       N_FILTERS,             "0",                 $INTEGER,   $RW_,  null,
       N_FLUSHMODE,           $FLUSHWRITES,        $INTEGER,   $RW_,  null,
       N_FULLNAME,            T_STRING,            $NULL,      $LR__, null,
       N_INTERRUPTED,         "0",                 $INTEGER,   $RW_,  null,
       N_MARKSUPPORTED,       $FALSE,              $INTEGER,   $LR__, null,
       N_NEXTBUF,             "",                  $STRING,    $RW_,  null,
       N_NEXTCHAR,            "-1",                $INTEGER,   $RW_,  null,
       N_NEXTENTRY,           T_ZIPENTRY,          $NULL,      $RW_,  null,
       N_NEXTLINE,            "",                  $STRING,    $RW_,  null,
       N_OPEN,                $FALSE,              $INTEGER,   $RW_,  null,
       N_READY,               "-1",                $INTEGER,   $LR__, null,
       N_SIZE,                "-1",                $DOUBLE,    $LR__, null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

       T_COLOR,               "3",                 $DICT,      $L___, $_THIS,
       N_RED,                 "0",                 $NUMBER,    $RW_,  null,
       N_GREEN,               "0",                 $NUMBER,    $RW_,  null,
       N_BLUE,                "0",                 $NUMBER,    $RW_,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

       T_DIMENSION,           "2",                 $DICT,      $L___, $_THIS,
       N_HEIGHT,              "0.0",               $DOUBLE,    $RW_,  null,
       N_WIDTH,               "0.0",               $DOUBLE,    $RW_,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

       T_INSETS,              "4",                 $DICT,      $L___, $_THIS,
       N_BOTTOM,              "0.0",               $DOUBLE,    $RW_,  null,
       N_LEFT,                "0.0",               $DOUBLE,    $RW_,  null,
       N_RIGHT,               "0.0",               $DOUBLE,    $RW_,  null,
       N_TOP,                 "0.0",               $DOUBLE,    $RW_,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

       T_MENU,                "0",                 $ARRAY,     $L___, $_THIS,
       null,                  "-1",                $GROWTO,    null,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

       T_POINT,               "2",                 $DICT,      $L___, $_THIS,
       N_X,                   "0.0",               $DOUBLE,    $RW_,  null,
       N_Y,                   "0.0",               $DOUBLE,    $RW_,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

       T_RECTANGLE,           "4",                 $DICT,      $L___, $_THIS,
       N_X,                   "0.0",               $DOUBLE,    $RW_,  null,
       N_Y,                   "0.0",               $DOUBLE,    $RW_,  null,
       N_HEIGHT,              "0.0",               $DOUBLE,    $RW_,  null,
       N_WIDTH,               "0.0",               $DOUBLE,    $RW_,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

       T_MATRIX,              "25",                $DICT,      $L___, $_THIS,
       N_MAJOR,               $MATRIX,             $INTEGER,   $LR__, null,
       N_MINOR,               "0",                 $INTEGER,   $LR__, null,
       N_CONCAT,              T_CALLABLE,          $NULL,      $L__X, null,
       N_CONCATMATRIX,        T_CALLABLE,          $NULL,      $L__X, null,
       N_CURRENTMATRIX,       T_CALLABLE,          $NULL,      $L__X, null,
       N_DIVIDEMATRIX,        T_CALLABLE,          $NULL,      $L__X, null,
       N_DTRANSFORM,          T_CALLABLE,          $NULL,      $L__X, null,
       N_IDENTMATRIX,         T_CALLABLE,          $NULL,      $L__X, null,
       N_IDTRANSFORM,         T_CALLABLE,          $NULL,      $L__X, null,
       N_INITMATRIX,          T_CALLABLE,          $NULL,      $L__X, null,
       N_INVERTMATRIX,        T_CALLABLE,          $NULL,      $L__X, null,
       N_ITRANSFORM,          T_CALLABLE,          $NULL,      $L__X, null,
       N_MAPTOPIXEL,          T_CALLABLE,          $NULL,      $L__X, null,
       N_ROTATE,              T_CALLABLE,          $NULL,      $L__X, null,
       N_SCALE,               T_CALLABLE,          $NULL,      $L__X, null,
       N_SETMATRIX,           T_CALLABLE,          $NULL,      $L__X, null,
       N_SHEAR,               T_CALLABLE,          $NULL,      $L__X, null,
       N_SHX,                 "0.0",               $DOUBLE,    $RW_,  null,
       N_SHY,                 "0.0",               $DOUBLE,    $RW_,  null,
       N_SX,                  "1.0",               $DOUBLE,    $RW_,  null,
       N_SY,                  "1.0",               $DOUBLE,    $RW_,  null,
       N_TRANSFORM,           T_CALLABLE,          $NULL,      $L__X, null,
       N_TRANSLATE,           T_CALLABLE,          $NULL,      $L__X, null,
       N_TX,                  "0.0",               $DOUBLE,    $RW_,  null,
       N_TY,                  "0.0",               $DOUBLE,    $RW_,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

       T_SCREEN,              "21",                $DICT,      $L___, $_THIS,
       N_MAJOR,               $SCREEN,             $INTEGER,   $LR__, null,
       N_MINOR,               "0",                 $INTEGER,   $LR__, null,
       N_BACKGROUND,          T_COLOR,             $NULL,      $LRW_, null,
       N_BOUNDS,              T_RECTANGLE,         $NULL,      $LR__, null,
       N_DEFAULTMATRIX,       "0",                 $MATRIX,    $RORO, null,
       N_DIAGONAL,            N_DIAGONAL,          $DUP,       $RW_,  null,
       N_DOUBLEBUFFERED,      T_OBJECT,            $NULL,      $LRW_, null,
       N_FONT,                N_DEFAULTFONT,       $DUP,       $LRW_, null,
       N_FOREGROUND,          T_COLOR,             $NULL,      $LRW_, null,
       N_FULLSCREENSUPPORTED, $TRUE,               $INTEGER,   $LR__, null,
       N_HEADLESS,            $TRUE,               $INTEGER,   $LR__, null,
       N_HEIGHT,              "0",                 $DOUBLE,    $RW_,  null,
       N_ID,                  T_STRING,            $NULL,      $RW_,  null,
       N_INDEX,               "0",                 $INTEGER,   $RW_,  null,
       N_INSETS,              T_INSETS,            $NULL,      $LR__, null,
       N_PIXELHEIGHT,         "0",                 $DOUBLE,    $RW_,  null,
       N_PIXELWIDTH,          "0",                 $DOUBLE,    $RW_,  null,
       N_RESOLUTION,          "72",                $DOUBLE,    $LR__, null,
       N_UIMANAGER,           T_OBJECT,            $NULL,      $LR__, null,
       N_VIRTUALBOUNDS,       T_RECTANGLE,         $NULL,      $LR__, null,
       N_WIDTH,               "0",                 $DOUBLE,    $RW_,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

    //
    // This really belongs here - security manager checking, if there
    // is one installed, can cause deadlock if we prompt with a dialog
    // and the Thread type still needs to be loaded.
    //

       T_THREAD,              "15",                $DICT,      $L___, $_THIS,
       null,                  "-1",                $GROWTO,    null,  null,
       N_MAJOR,               $THREAD,             $INTEGER,   $LR__, null,
       N_MINOR,               "0",                 $INTEGER,   $LR__, null,
       N_ALIVE,               $FALSE,              $INTEGER,   $RW_,  null,
       N_DAEMON,              $FALSE,              $INTEGER,   $RW_,  null,
       N_GROUP,               T_STRING,            $NULL,      $LR__, null,
       N_INTERRUPTED,         $FALSE,              $INTEGER,   $RW_,  null,
       N_NAME,                T_STRING,            $NULL,      $RW_,  null,
       N_PERSISTENT,          $FALSE,              $INTEGER,   $RW_,  null,
       N_PRIORITY,            $NORM_PRIORITY,      $INTEGER,   $RW_,  null,
       N_STATE,               $FALSE,              $INTEGER,   $LR__, null,
       N_QUEUE,               T_CALLABLE,          $NULL,      $L__X, null,
       N_QUEUEONCE,           T_CALLABLE,          $NULL,      $L__X, null,
       N_QUEUESIZE,           "0",                 $INTEGER,   $LR__, null,
       N_RUN,                 T_FUNCTION,          $NULL,      $RWX,  null,
       N_AFTERINTERRUPT,      T_FUNCTION,          $NULL,      $RWX,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

    //
    // This is also belongs here because the N_SCREEN dictionary now has
    // an N_UIMANAGER field.
    //

       T_UIMANAGER,           "12",                $DICT,      $L___, $_THIS,
       null,                  "-1",                $GROWTO,    null,  null,
       N_MAJOR,               $UIMANAGER,          $INTEGER,   $LR__, null,
       N_MINOR,               "0",                 $INTEGER,   $LR__, null,
       N_CONTAINS,            T_CALLABLE,          $NULL,      $L__X, null,
       N_CROSSPLATFORMNAME,   T_STRING,            $NULL,      $LR__, null,
       N_GET,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_LOOKANDFEEL,         T_STRING,            $NULL,      $RW_,  null,
       N_LOOKANDFEELNAMES,    T_ARRAY,             $NULL,      $LR__, null,
       N_NATIVENAME,          T_STRING,            $NULL,      $LR__, null,
       N_PROPERTIES,          T_DICT,              $NULL,      $RW_,  null,
       N_PUT,                 T_CALLABLE,          $NULL,      $L__X, null,
       N_RESET,               T_CALLABLE,          $NULL,      $L__X, null,
       N_THEME,               T_STRING,            $NULL,      $RW_,  null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

    //
    // A new type that's probably not used much, but we add a Compiler to VM,
    // so it belongs here.
    //

       T_COMPILER,            "12",                $DICT,      $L___, $_THIS,
       null,                  "-1",                $GROWTO,    null,  null,
       N_MAJOR,               $COMPILER,           $INTEGER,   $LR__, null,
       N_MINOR,               "0",                 $INTEGER,   $LR__, null,
       N_ADDTAGS,             $FALSE,              $INTEGER,   $RW_,  null,
       N_ALIVE,               $FALSE,              $INTEGER,   $RW_,  null,
       N_COMPILEFUNCTION,     T_CALLABLE,          $NULL,      $L__X, null,
       N_COMPILEFUNCTIONLATER,T_CALLABLE,          $NULL,      $L__X, null,
       N_COMPILESCRIPT,       T_CALLABLE,          $NULL,      $L__X, null,
       N_DEBUG,               "0",                 $INTEGER,   $RW_,  null,
       N_LOCALCOPYMODEL,      "1",                 $INTEGER,   $RW_,  null,
       N_PRIORITY,            $LOW_PRIORITY,       $INTEGER,   $RW_,  null,
       N_TIMESTAMP,           T_STRING,            $NULL,      $LR__, null,
       N_VERSION,             T_STRING,            $NULL,      $LR__, null,
       null,                  N_TYPEDICT,          $PUT,       null,  null,

    //
    // Loadable types
    //

       null,                  N_TYPEDICT,          $RESTART,   null,  null,
       T_LAYOUTMANAGER,       "YoixModuleLayout",  $MODULE,    null,  null,
       T_BORDERLAYOUT,        "YoixModuleLayout",  $MODULE,    null,  null,
       T_BOXLAYOUT,           "YoixModuleLayout",  $MODULE,    null,  null,
       T_CARDLAYOUT,          "YoixModuleLayout",  $MODULE,    null,  null,
       T_CUSTOMLAYOUT,        "YoixModuleLayout",  $MODULE,    null,  null,
       T_FLOWLAYOUT,          "YoixModuleLayout",  $MODULE,    null,  null,
       T_GRIDBAGCONSTRAINTS,  "YoixModuleLayout",  $MODULE,    null,  null,
       T_GRIDBAGLAYOUT,       "YoixModuleLayout",  $MODULE,    null,  null,
       T_GRIDLAYOUT,          "YoixModuleLayout",  $MODULE,    null,  null,
       T_SPRINGLAYOUT,        "YoixModuleLayout",  $MODULE,    null,  null,
       T_SPRINGCONSTRAINTS,   "YoixModuleLayout",  $MODULE,    null,  null,
       T_BUTTON,              "YoixModuleAWT",     $MODULE,    null,  null,
       T_CANVAS,              "YoixModuleAWT",     $MODULE,    null,  null,
       T_CHECKBOX,            "YoixModuleAWT",     $MODULE,    null,  null,
       T_CHECKBOXGROUP,       "YoixModuleAWT",     $MODULE,    null,  null,
       T_CHECKBOXGROUP_SWING, "YoixModuleAWT",     $MODULE,    null,  null,
       T_CHECKBOX_SWING,      "YoixModuleAWT",     $MODULE,    null,  null,
       T_CHOICE,              "YoixModuleAWT",     $MODULE,    null,  null,
       T_DIALOG,              "YoixModuleAWT",     $MODULE,    null,  null,
       T_FILEDIALOG,          "YoixModuleAWT",     $MODULE,    null,  null,
       T_FRAME,               "YoixModuleAWT",     $MODULE,    null,  null,
       T_LABEL,               "YoixModuleAWT",     $MODULE,    null,  null,
       T_LIST,                "YoixModuleAWT",     $MODULE,    null,  null,
       T_MENUBAR,             "YoixModuleAWT",     $MODULE,    null,  null,
       T_PANEL,               "YoixModuleAWT",     $MODULE,    null,  null,
       T_POPUPMENU,           "YoixModuleAWT",     $MODULE,    null,  null,
       T_ROWPROPERTIES,       "YoixModuleAWT",     $MODULE,    null,  null,
       T_SCROLLBAR,           "YoixModuleAWT",     $MODULE,    null,  null,
       T_SCROLLBAR_SWING,     "YoixModuleAWT",     $MODULE,    null,  null,
       T_SCROLLPANE,          "YoixModuleAWT",     $MODULE,    null,  null,
       T_TABLECOLUMN,         "YoixModuleAWT",     $MODULE,    null,  null,
       T_TABLEMANAGER,        "YoixModuleAWT",     $MODULE,    null,  null,
       T_TEXTAREA,            "YoixModuleAWT",     $MODULE,    null,  null,
       T_TEXTCANVAS,          "YoixModuleAWT",     $MODULE,    null,  null,
       T_TEXTFIELD,           "YoixModuleAWT",     $MODULE,    null,  null,
       T_TEXTTERM,            "YoixModuleAWT",     $MODULE,    null,  null,
       T_WINDOW,              "YoixModuleAWT",     $MODULE,    null,  null,

       T_BUTTONGROUP,         "YoixModuleSwing",   $MODULE,    null,  null,
       T_JBUTTON,             "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCANVAS,             "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCHECKBOX,           "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCHECKBOXMENUITEM,   "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCHECKBOXGROUP,      "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCHECKBOXGROUP_AWT,  "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCHECKBOX_AWT,       "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCHOICE,             "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCOLORCHOOSER,       "YoixModuleSwing",   $MODULE,    null,  null,
       T_JCOMBOBOX,           "YoixModuleSwing",   $MODULE,    null,  null,
       T_JDESKTOPPANE,        "YoixModuleSwing",   $MODULE,    null,  null,
       T_JDIALOG,             "YoixModuleSwing",   $MODULE,    null,  null,
       T_JFILECHOOSER,        "YoixModuleSwing",   $MODULE,    null,  null,
       T_JFILEDIALOG,         "YoixModuleSwing",   $MODULE,    null,  null,
       T_JFRAME,              "YoixModuleSwing",   $MODULE,    null,  null,
       T_JINTERNALFRAME,      "YoixModuleSwing",   $MODULE,    null,  null,
       T_JLABEL,              "YoixModuleSwing",   $MODULE,    null,  null,
       T_JLAYEREDPANE,        "YoixModuleSwing",   $MODULE,    null,  null,
       T_JLIST,               "YoixModuleSwing",   $MODULE,    null,  null,
       T_JMENU,               "YoixModuleSwing",   $MODULE,    null,  null,
       T_JMENUBAR,            "YoixModuleSwing",   $MODULE,    null,  null,
       T_JMENUITEM,           "YoixModuleSwing",   $MODULE,    null,  null,
       T_JPANEL,              "YoixModuleSwing",   $MODULE,    null,  null,
       T_JPASSWORDFIELD,      "YoixModuleSwing",   $MODULE,    null,  null,
       T_JPOPUPMENU,          "YoixModuleSwing",   $MODULE,    null,  null,
       T_JPROGRESSBAR,        "YoixModuleSwing",   $MODULE,    null,  null,
       T_JRADIOBUTTON,        "YoixModuleSwing",   $MODULE,    null,  null,
       T_JRADIOBUTTONMENUITEM,"YoixModuleSwing",   $MODULE,    null,  null,
       T_JSCROLLBAR,          "YoixModuleSwing",   $MODULE,    null,  null,
       T_JSCROLLBAR_AWT,      "YoixModuleSwing",   $MODULE,    null,  null,
       T_JSCROLLPANE,         "YoixModuleSwing",   $MODULE,    null,  null,
       T_JSEPARATOR,          "YoixModuleSwing",   $MODULE,    null,  null,
       T_JSLIDER,             "YoixModuleSwing",   $MODULE,    null,  null,
//       T_JSPINNER,            "YoixModuleSwing",   $MODULE,    null,  null,
       T_JSPLITPANE,          "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTABBEDPANE,         "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTABLE,              "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTABLECOLUMN,        "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTEXTAREA,           "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTEXTCANVAS,         "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTEXTFIELD,          "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTEXTPANE,           "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTEXTTERM,           "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTOGGLEBUTTON,       "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTOOLBAR,            "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTREE,               "YoixModuleSwing",   $MODULE,    null,  null,
       T_JTREENODE,           "YoixModuleSwing",   $MODULE,    null,  null,
       T_JWINDOW,             "YoixModuleSwing",   $MODULE,    null,  null,
       T_TRANSFERHANDLER,     "YoixModuleSwing",   $MODULE,    null,  null,

       T_EVENT,               "YoixModuleEvent",   $MODULE,    null,  null,
       T_ACTIONEVENT,         "YoixModuleEvent",   $MODULE,    null,  null,
       T_ADJUSTMENTEVENT,     "YoixModuleEvent",   $MODULE,    null,  null,
       T_CARETEVENT,          "YoixModuleEvent",   $MODULE,    null,  null,
       T_CHANGEEVENT,         "YoixModuleEvent",   $MODULE,    null,  null,
       T_COMPONENTEVENT,      "YoixModuleEvent",   $MODULE,    null,  null,
       T_DRAGGESTUREEVENT,    "YoixModuleEvent",   $MODULE,    null,  null,
       T_DRAGSOURCEEVENT,     "YoixModuleEvent",   $MODULE,    null,  null,
       T_DROPTARGETEVENT,     "YoixModuleEvent",   $MODULE,    null,  null,
       T_FOCUSEVENT,          "YoixModuleEvent",   $MODULE,    null,  null,
       T_HYPERLINKEVENT,      "YoixModuleEvent",   $MODULE,    null,  null,
       T_INVOCATIONEVENT,     "YoixModuleEvent",   $MODULE,    null,  null,
       T_ITEMEVENT,           "YoixModuleEvent",   $MODULE,    null,  null,
       T_KEYEVENT,            "YoixModuleEvent",   $MODULE,    null,  null,
       T_LISTSELECTIONEVENT,  "YoixModuleEvent",   $MODULE,    null,  null,
       T_MOUSEEVENT,          "YoixModuleEvent",   $MODULE,    null,  null,
       T_MOUSEWHEELEVENT,     "YoixModuleEvent",   $MODULE,    null,  null,
       T_PAINTEVENT,          "YoixModuleEvent",   $MODULE,    null,  null,
       T_TEXTEVENT,           "YoixModuleEvent",   $MODULE,    null,  null,
       T_TREESELECTIONEVENT,  "YoixModuleEvent",   $MODULE,    null,  null,
       T_WINDOWEVENT,         "YoixModuleEvent",   $MODULE,    null,  null,

       T_BEVELBORDER,         "YoixModuleBorder",  $MODULE,    null,  null,
       T_BORDER,              "YoixModuleBorder",  $MODULE,    null,  null,
       T_EMPTYBORDER,         "YoixModuleBorder",  $MODULE,    null,  null,
       T_ETCHEDBORDER,        "YoixModuleBorder",  $MODULE,    null,  null,
       T_LINEBORDER,          "YoixModuleBorder",  $MODULE,    null,  null,
       T_MATTEBORDER,         "YoixModuleBorder",  $MODULE,    null,  null,
       T_SOFTBEVELBORDER,     "YoixModuleBorder",  $MODULE,    null,  null,

       T_AUDIOCLIP,           "YoixModuleSound",   $MODULE,    null,  null,
       T_FONT,                "YoixModuleGraphics",$MODULE,    null,  null,
       T_GRAPHICS,            "YoixModuleGraphics",$MODULE,    null,  null,
       T_PATH,                "YoixModuleGraphics",$MODULE,    null,  null,
       T_IMAGE,               "YoixModuleImage",   $MODULE,    null,  null,
       T_REGEXP,              "YoixModuleRE",      $MODULE,    null,  null,
       T_SUBEXP,              "YoixModuleRE",      $MODULE,    null,  null,
       T_OPTION,              "YoixModuleUtil",    $MODULE,    null,  null,
       T_LOCALE,              "YoixModuleUtil",    $MODULE,    null,  null,
       T_TIMEZONE,            "YoixModuleUtil",    $MODULE,    null,  null,
       T_CALENDAR,            "YoixModuleUtil",    $MODULE,    null,  null,
       T_CERTIFICATE,         "YoixModuleSecure",  $MODULE,    null,  null,
       T_CIPHER,              "YoixModuleSecure",  $MODULE,    null,  null,
       T_KEY,                 "YoixModuleSecure",  $MODULE,    null,  null,
       T_KEYSTORE,            "YoixModuleSecure",  $MODULE,    null,  null,
       T_CLIPBOARD,           "YoixModuleSystem",  $MODULE,    null,  null,
       T_PROCESS,             "YoixModuleSystem",  $MODULE,    null,  null,
       T_SECURITYMANAGER,     "YoixModuleSystem",  $MODULE,    null,  null,
       T_RANDOM,              "YoixModuleMath",    $MODULE,    null,  null,
       T_HASHTABLE,           "YoixModuleMisc",    $MODULE,    null,  null,
       T_VECTOR,              "YoixModuleMisc",    $MODULE,    null,  null,
       T_PARSETREE,           "YoixModuleParser",  $MODULE,    null,  null,

       T_ZIPENTRY,            "YoixModuleStream",  $MODULE,    null,  null,
       T_STRINGSTREAM,        "YoixModuleStream",  $MODULE,    null,  null,
       T_URL,                 "YoixModuleStream",  $MODULE,    null,  null,

       T_COOKIEMANAGER,       "YoixModuleNet",     $MODULE,    null,  null,
       T_DATAGRAMSOCKET,      "YoixModuleNet",     $MODULE,    null,  null,
       T_MULTICASTSOCKET,     "YoixModuleNet",     $MODULE,    null,  null,
       T_SERVERSOCKET,        "YoixModuleNet",     $MODULE,    null,  null,
       T_SOCKET,              "YoixModuleNet",     $MODULE,    null,  null,

       T_EDGE,                "YoixModuleGraph",   $MODULE,    null,  null,
       T_GRAPH,               "YoixModuleGraph",   $MODULE,    null,  null,
       T_GRAPHOBSERVER,       "YoixModuleGraph",   $MODULE,    null,  null,
       T_NODE,                "YoixModuleGraph",   $MODULE,    null,  null,

    //
    // Everything else needed to start the interpreter. The assignment
    // of a package name to the N_YOIX dictionary currently must happen
    // after the creation of M_MODULE, which explains why it's last.
    //
    // NOTE - ordering of modules in N_YOIX now controls the search and
    // load order triggered by the processing of the new
    //
    //		import yoix.*.*;
    //
    // statement. No ordering can optimize all scripts, which suggests
    // we should come up with some other mechanism - later.
    //

       null,                  "25",                 $LIST,      $RWRO, N_YOIX,
       M_SWING,               "YoixModuleSwing",    $MODULE,    null,  null,	// covers M_AWT
       M_SYSTEM,              "YoixModuleSystem",   $MODULE,    null,  null,
       M_IO,                  "YoixModuleIO",       $MODULE,    null,  null,
       M_STDIO,               "YoixModuleStdio",    $MODULE,    null,  null,
       M_STRING,              "YoixModuleString",   $MODULE,    null,  null,
       M_CTYPE,               "YoixModuleCtype",    $MODULE,    null,  null,
       M_MATH,                "YoixModuleMath",     $MODULE,    null,  null,
       M_THREAD,              "YoixModuleThread",   $MODULE,    null,  null,
       M_UTIL,                "YoixModuleUtil",     $MODULE,    null,  null,
       M_RE,                  "YoixModuleRE",       $MODULE,    null,  null,
       M_NET,                 "YoixModuleNet",      $MODULE,    null,  null,
       M_GRAPH,               "YoixModuleGraph",    $MODULE,    null,  null,
       M_SECURE,              "YoixModuleSecure",   $MODULE,    null,  null,
       M_IMAGE,               "YoixModuleImage",    $MODULE,    null,  null,
       M_GRAPHICS,            "YoixModuleGraphics", $MODULE,    null,  null,
       M_FACTORIAL,           "YoixModuleFactorial",$MODULE,    null,  null,	// new in 4.0

       M_TYPE,                "YoixModuleType",     $MODULE,    null,  null,	// deprecated???
       M_PARSER,              "YoixModuleParser",   $MODULE,    null,  null,	// big/not used much
       M_XCOLOR,              "YoixModuleXColor",   $MODULE,    null,  null,	// big/not used much
       M_ERROR,               "YoixModuleError",    $MODULE,    null,  null,	// rarely used
       M_WINDOWS,             "YoixModuleWindows",  $MODULE,    null,  null,	// rarely used
       M_EVENT,               "YoixModuleEvent",    $MODULE,    null,  null,	// rarely used
       M_ROBOT,               "YoixModuleRobot",    $MODULE,    null,  null,	// rarely used
       M_AWT,                 "YoixModuleAWT",      $MODULE,    null,  null,	// covered by M_SWING
       M_SOUND,               "YoixModuleSound",    $MODULE,    null,  null,	// currently empty

    //
    // The next two sections are tricky - they add magic tokens to the
    // M_MODULE and N_YOIX dictionaries that can trigger module loading
    // when a lookup fails (see YoixBodyDictionary.java). The $PACKAGE
    // magic obviously must be the last thing done to the dictionary,
    // and in this case only N_YOIX is harder than you might expect.
    //

       M_MODULE,              "0",                 $DICT,      $LRW_, $_THIS,
       null,                  "-1",                $GROWTO,    null,  null,
       null,                  "",                  $PACKAGE,   null,  null,
       null,                  N_YOIX,              $PUT,       null,  null,

       N_YOIX,                N_YOIX,              $RESTART,   null,  null,
       null,                  YOIXPACKAGE,         $PACKAGE,   null,  null,
       null,                  N_RESERVED,          $PUT,       null,  null,

       null,                  "32",                $LIST,      $RWRO, N_VM,
       null,                  null,                $GROWTO,    null,  null,
       N_YOIXCREATED,         YOIXCREATED,         $STRING,    $RORO, null,
       N_YOIXNOTICE,          YOIXNOTICE,          $STRING,    $RORO, null,
       N_YOIXVERSION,         YOIXVERSION,         $STRING,    $RORO, null,
       N_YOIXPACKAGE,         YOIXPACKAGE,         $STRING,    $RORO, null,
       N_YOIXSERIALNUMBER,    YOIXSERIALNUMBER,    $STRING,    $RORO, null,
       N_ACCEPTCERTIFICATES,  N_ACCEPTCERTIFICATES,$DUP,       $LR__, null,
       N_ADDTAGS,             N_ADDTAGS,           $DUP,       $LRW_, null,
       N_APPLET,              N_APPLET,            $DUP,       $LR__, null,
       N_AUTOIMPORT,          N_AUTOIMPORT,        $DUP,       $LR__, null,
       N_BUTTONMODEL,         N_BUTTONMODEL,       $DUP,       $LRW_, null,
       N_BIND,                N_BIND,              $DUP,       $LRW_, null,
       N_COMPILER,            T_COMPILER,          $DECLARE,   $LR__, null,
       N_CREATE,              N_CREATE,            $DUP,       $LRW_, null,
       N_DEBUG,               N_DEBUG,             $DUP,       $LRW_, null,
       N_DEBUGGING,           N_DEBUGGING,         $DUP,       $LR__, null,
       N_DUMPDEPTH,           N_DUMPDEPTH,         $DUP,       $LRW_, null,
       N_EVENTFLAGS,          N_EVENTFLAGS,        $DUP,       $LRW_, null,
       N_EXITMODEL,           N_EXITMODEL,         $DUP,       $LRW_, null,
       N_FIXFONTMETRICS,      N_FIXFONTMETRICS,    $DUP,       $LRW_, null,
       N_FIXFONTS,            N_FIXFONTS,          $DUP,       $LR__, null,
       N_FLAGS,               N_FLAGS,             $DUP,       $LRW_, null,
       N_FONTMAGNIFICATION,   N_FONTMAGNIFICATION, $DUP,       $LRW_, null,
       N_MENUFLAGS,           N_MENUFLAGS,         $DUP,       $LRW_, null,
       N_SCREENS,             "1",                 $ARRAY,     $RORO, null,
       N_SECURESUFFIX,        YOIXSECURESUFFIX,    $STRING,    $RORO, null,
       N_SECURITYOPTIONS,     N_SECURITYOPTIONS,   $DUP,       $RORO, null,
       N_STARTTIME,           YOIXSTARTTIME,       $DOUBLE,    $LR__, null,
       N_TMPDIR,              N_TMPDIR,            $DUP,       $RORO, null,
       N_TRACE,               N_TRACE,             $DUP,       $LRW_, null,
       N_ZIPPED,              N_ZIPPED,            $DUP,       $LR__, null,

       N_ENCODING,            "4",                 $LIST,      $RWRO, $_THIS,
       N_JVM,                 $JVMENCODING,        $STRING,    $RORO, null,
       N_PARSER,              N_PARSERENCODING,    $DUP,       $RW_,  null,
       N_STREAM,              N_STREAMENCODING,    $DUP,       $RW_,  null,
       N_VM,                  N_PARSERENCODING,    $DUP,       $RORO, null,
       null,                  N_VM,                $PUT,       null,  null,

       N_SCREEN,              "17",                $LIST,      $RWRO, $_THIS,
       null,                  null,                $GROWTO,    null,  null,
       N_BACKGROUND,          T_COLOR,             $NULL,      $LRW_, null,
       N_BOUNDS,              T_RECTANGLE,         $NULL,      $LRW_, null,
       N_DEFAULTMATRIX,       "0",                 $MATRIX,    $RORO, null,
       N_DIAGONAL,            N_DIAGONAL,          $DUP,       $LR__, null,
       N_DOUBLEBUFFERED,      T_OBJECT,            $NULL,      $LRW_, null,
       N_FONT,                N_DEFAULTFONT,       $DUP,       $LRW_, null,
       N_FOREGROUND,          T_COLOR,             $NULL,      $LRW_, null,
       N_HEADLESS,            $TRUE,               $INTEGER,   $LR__, null,
       N_HEIGHT,              "0",                 $DOUBLE,    $LR__, null,
       N_ID,                  T_STRING,            $NULL,      $LR__, null,
       N_INDEX,               "0",                 $INTEGER,   $LR__, null,
       N_INSETS,              T_INSETS,            $NULL,      $LRW_, null,
       N_PIXELHEIGHT,         "0",                 $DOUBLE,    $LR__, null,
       N_PIXELWIDTH,          "0",                 $DOUBLE,    $LR__, null,
       N_RESOLUTION,          "72",                $DOUBLE,    $LR__, null,
       N_UIMANAGER,           T_OBJECT,            $NULL,      $LR__, null,
       N_WIDTH,               "0",                 $DOUBLE,    $LR__, null,
       null,                  N_VM,                $PUT,       null,  null,

       null,                  T_FILE,              $DUP,       $RW_,  N_STDERR,
       N_NAME,                NAME_STDERR,         $STRING,    $RORO, null,
       N_FULLNAME,            T_STRING,            $NULL,      $LR__, null,
       N_MODE,                "w",                 $STRING,    $RORO, null,
       N_BUFSIZE,             $BUFSIZ,             $INTEGER,   $LR__, null,
       N_FLUSHMODE,           $FLUSHWRITES,        $INTEGER,   $LR__, null,
       N_OPEN,                $TRUE,               $INTEGER,   $RW_,  null,

       null,                  T_FILE,              $DUP,       $RW_,  N_STDIN,
       N_NAME,                NAME_STDIN,          $STRING,    $RORO, null,
       N_FULLNAME,            T_STRING,            $NULL,      $LR__, null,
       N_MODE,                "r",                 $STRING,    $RORO, null,
       N_OPEN,                $TRUE,               $INTEGER,   $RW_,  null,

       null,                  T_FILE,              $DUP,       $RW_,  N_STDOUT,
       N_NAME,                NAME_STDOUT,         $STRING,    $RORO, null,
       N_FULLNAME,            T_STRING,            $NULL,      $LR__, null,
       N_MODE,                "w",                 $STRING,    $RORO, null,
       N_FLUSHMODE,           $FLUSHWRITES,        $INTEGER,   $LR__, null,
       N_OPEN,                $TRUE,               $INTEGER,   $RW_,  null,

       null,                  "8",                 $DICT,      $LRW_, N_ERRORDICT,
       N_ARGS,                T_ARRAY,             $NULL,      $LRW_, null,
       N_EXCEPTION,           T_STRING,            $NULL,      $LRW_, null,
       N_JAVATRACE,           T_STRING,            $NULL,      $LRW_, null,
       N_MESSAGE,             T_STRING,            $NULL,      $LRW_, null,
       N_NAME,                T_STRING,            $NULL,      $LRW_, null,
       N_STACKTRACE,          T_STRING,            $NULL,      $LRW_, null,
       N_TIMESTAMP,           "0",                 $DOUBLE,    $LRW_, null,
       N_TYPE,                T_STRING,            $NULL,      $LRW_, null,
    };

    ///////////////////////////////////
    //
    // YoixModuleVM Methods
    //
    ///////////////////////////////////

    public static YoixObject
    abort(YoixObject arg[]) {

	String  args[];
	int     n;

	if (arg.length > 0) {
	    if (arg[0].isString()) {
		if (arg.length > 1) {
		    args = new String[arg.length - 1];
		    for (n = 1; n < arg.length; n++) {
			if (arg[n].isString())
			    args[n - 1] = arg[n].stringValue();
			else VM.badArgument(n);
		    }
		} else args = null;
		VM.abort(arg[0].stringValue(), args, new Throwable());
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(null);
    }


    public static YoixObject
    defined(YoixObject arg[]) {

	boolean  result = false;

	//
	// NOTE - looks like the two argument version with an integer as
	// as the first argument forgot to add the target's offset to the
	// integer. I made the change on 5/13/08, but this really needs
	// to be carefully tested!!!!
	//

	if (arg.length == 1 || arg.length == 2) {
	    if (arg[0].notNull()) {
		if (arg[0].isString() || arg[0].isInteger()) {
		    if (arg.length == 2) {
			if (arg[1].isPointer()) {
			    if (arg[0].isString())
				result = arg[1].defined(arg[0].stringValue());
			    else result = arg[1].defined(arg[1].offset() + arg[0].intValue());		// offset added on 5/13/08
			} else result = false;
		    } else {
			if (arg[0].isString())
			    result = YoixBodyBlock.isDefined(arg[0].stringValue());
			else VM.badArgument(0);
		    }
		} else if (arg[0].isPointer()) {
		    if (arg.length == 1)
			result = arg[0].defined(arg[0].offset());
		    else VM.badCall();
		} else VM.badArgument(0);
	    } else result = false;
	} else VM.badCall();

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    eval(YoixObject arg[]) {

	DataInputStream  input;
	YoixBodyStream   stream;
	YoixObject       value = null;
	String           name;

	if (arg.length == 1 || arg.length == 2) {
	    if ((arg[0].isStream() || arg[0].isString()) && arg[0].notNull()) {
		if (arg.length == 1 || arg[1].isString() || arg[1].isNull()) {
		    if (arg[0].isString() || arg[0].isStringStream())
			name = (arg.length > 1 && arg[1].notNull()) ? arg[1].stringValue() : "--string--";
		    else name = arg[0].getString(N_NAME);
		    if (arg[0].isStream()) {
			stream = arg[0].streamValue();
			if ((input = stream.accessDataInputStream()) != null) {
			    try {
				value = Yoix.evalStream(YoixMisc.getParserReader(input), name);
			    }
			    finally {
				stream.releaseDataInputStream();
			    }
			} else VM.badArgument(0);
		    } else if (arg[0].isString())
			value = Yoix.evalString(arg[0].stringValue(), name);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(value);
    }


    public static YoixObject
    execute(YoixObject arg[]) {

	YoixBodyStream   stream;
	InputStream      input;
	YoixObject       array;
	YoixObject       value = null;
	String           name;

	if ((arg[0].isStream() || arg[0].isString()) && arg[0].notNull()) {
	    if (arg.length == 1 || arg[1].isString() || arg[1].isNull()) {
		if (arg[0].isString() || arg[0].isStringStream())
		    name = (arg.length > 1 && arg[1].notNull()) ? arg[1].stringValue() : "--string--";
		else name = arg[0].getString(N_NAME);
		if (arg.length > 1) {
		    if (arg[1].isNull())	// kludge - NULL Object isn't a String
			arg[1] = YoixObject.newString();
		    array = YoixMake.yoixArray(arg, 1);
		    if (arg[1].isNull())
			array.put(0, YoixObject.newString(name), false);
		} else {
		    array = YoixObject.newArray(1);
		    array.put(0, YoixObject.newString(name), false);
		}
		if (arg[0].isStream()) {
		    stream = arg[0].streamValue();
		    if ((input = stream.accessDataInputStream()) != null) {
			try {
			    if (input.markSupported() == false)
				input = new BufferedInputStream(input);
			    value = Yoix.executeStream(input, name, array, null, true);
			}
			finally {
			    stream.releaseDataInputStream();
			}
		    } else VM.badArgument(0);
		} else value = Yoix.executeString(arg[0].stringValue(), name, array, true);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(value);
    }


    public static YoixObject
    toString(YoixObject arg[]) {

	YoixObject  lval;
	YoixObject  level;
	String      value = null;

	if (arg.length <= 2) {
	    if (arg.length == 1 || arg[1].isInteger()) {
		level = YoixObject.newInt(arg.length == 2 ? arg[1].intValue() : 1);
		if (arg[0].isString() == false) {
		    if (arg[0].isPointer()) {
			VM.pushMark();
			VM.pushRestore(N_DUMPDEPTH, level);
			value = arg[0].toString().trim();
			VM.popMark();
		    } else value = arg[0].toString().trim();
		} else value = arg[0].stringValue(false);
	    } else VM.badArgument(1);
	} else VM.badCall();

	return(YoixObject.newString(value));
    }


    public static YoixObject
    unroll(YoixObject arg[]) {

	YoixObject  dest = null;
	YoixObject  src;
	int         length;
	int         n;
	int         m;

	if (arg.length <= 2) {
	    if (arg[0].isPointer()) {
		src = arg[0];
		if (arg.length == 1) {
		    if (src.notNull()) {
			length = src.length();
			if (src.compound())
			    dest = YoixObject.newArray(2*src.sizeof());
			else dest = YoixObject.newArray(src.sizeof());
			YoixMisc.unrollInto(src, dest);
			dest.setUnroll(true);
		    } else dest = src;
		} else {
		    if (arg[1].isPointer()) {
			dest = arg[1];
			YoixMisc.unrollInto(src, dest);
			dest.setUnroll(true);
		    } else VM.badArgument(1);
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(dest);
    }
}

