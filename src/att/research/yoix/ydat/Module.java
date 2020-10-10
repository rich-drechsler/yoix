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

package att.research.yoix.ydat;
import java.awt.Insets;
import java.util.*;
import javax.swing.JLayeredPane;
import javax.swing.JTable;
import att.research.yoix.*;

public abstract
class Module extends YoixModule

    implements Constants

{

    //
    // Data manager support, primarily designed to work with the data
    // visualization classes (e.g., Histograms) that are implemented in
    // in other modules. We've also included at least one builtin that
    // can build a datamanager's datafield entry by reading a specially
    // designed table, which currently is an array with seven columns
    // that describe the fields in the dictionaries that end up in the
    // datafields array. Probably just a first step, so you probably
    // can expect more table reading builtins.
    //

    public static final String  $MODULENAME = "ydat";
    public static final String  $MODULECREATED = "Mon Apr 27 08:11:48 EDT 2009";
    public static final String  $MODULENOTICE = YOIXNOTICE;
    public static final String  $MODULEVERSION = "37.0";

    static final Integer  $DATAMANAGER = new Integer(DATAMANAGER);
    static final Integer  $JAXIS = new Integer(JAXIS);
    static final Integer  $JDATATABLE = new Integer(JDATATABLE);
    static final Integer  $JEVENTPLOT = new Integer(JEVENTPLOT);
    static final Integer  $JGRAPHPLOT = new Integer(JGRAPHPLOT);
    static final Integer  $JHISTOGRAM = new Integer(JHISTOGRAM);
    static final Integer  $PALETTE = new Integer(PALETTE);

    static final Integer  $SWEEPGRAPH_MARKERS = new Integer(SWEEPGRAPH_MARKERS);
    static final Integer  $SWEEPGRAPH_WIDTHRATIO = new Integer(SWEEPGRAPH_WIDTHRATIO);

    static final Integer  $RESIZEMODE = new Integer(JTable.AUTO_RESIZE_OFF);

    static Integer  $DEFAULT_LAYER = JLayeredPane.DEFAULT_LAYER;
    static Integer  $SINGLESELECTION = new Integer(YOIX_SINGLE_SELECTION);

    //
    // DataGenerator constants
    //

    static final int  DATA_ARRAY = 1;
    static final int  DATA_CALL = 2;
    static final int  DATA_COUNTER = 3;
    static final int  DATA_DICTIONARY = 4;
    static final int  DATA_DOUBLE = 5;
    static final int  DATA_INTEGER = 6;
    static final int  DATA_ONE = 7;
    static final int  DATA_PARTITION = 8;
    static final int  DATA_STRING = 9;
    static final int  DATA_TABLE = 10;
    static final int  DATA_TABLE_NEW = 11;
    static final int  DATA_TABLECOLUMN = 12;
    static final int  DATA_TABLECOLUMN_NEW = 13;
    static final int  DATA_UID = 14;
    static final int  DATA_ZERO = 15;

    static Integer  $DATA_ARRAY = new Integer(DATA_ARRAY);
    static Integer  $DATA_CALL = new Integer(DATA_CALL);
    static Integer  $DATA_COUNTER = new Integer(DATA_COUNTER);
    static Integer  $DATA_DICTIONARY = new Integer(DATA_DICTIONARY);
    static Integer  $DATA_DOUBLE = new Integer(DATA_DOUBLE);
    static Integer  $DATA_INTEGER = new Integer(DATA_INTEGER);
    static Integer  $DATA_ONE = new Integer(DATA_ONE);
    static Integer  $DATA_PARTITION = new Integer(DATA_PARTITION);
    static Integer  $DATA_STRING = new Integer(DATA_STRING);
    static Integer  $DATA_TABLE = new Integer(DATA_TABLE);
    static Integer  $DATA_TABLE_NEW = new Integer(DATA_TABLE_NEW);
    static Integer  $DATA_TABLECOLUMN = new Integer(DATA_TABLECOLUMN);
    static Integer  $DATA_TABLECOLUMN_NEW = new Integer(DATA_TABLECOLUMN_NEW);
    static Integer  $DATA_UID = new Integer(DATA_UID);
    static Integer  $DATA_ZERO = new Integer(DATA_ZERO);

    static Integer  $OVERLAP_GENERATOR = new Integer(OVERLAP_GENERATOR);

    static final int  BUILTIN_ADJUSTTIME = 1;
    static final int  BUILTIN_ATOH = 2;
    static final int  BUILTIN_BTOA = 3;
    static final int  BUILTIN_BTOI = 4;
    static final int  BUILTIN_CONSTANT = 5;
    static final int  BUILTIN_CONTAINS = 6;
    static final int  BUILTIN_DATE = 7;
    static final int  BUILTIN_ENDSWITH = 8;
    static final int  BUILTIN_EQUALS = 9;
    static final int  BUILTIN_GETFILEEXTENSION = 10;
    static final int  BUILTIN_GETINDEX = 11;
    static final int  BUILTIN_GETKEY = 12;
    static final int  BUILTIN_GETQUERYSTRING = 13;
    static final int  BUILTIN_GETSEARCHQUERY = 14;
    static final int  BUILTIN_GETVALUE = 15;
    static final int  BUILTIN_HTOA = 16;
    static final int  BUILTIN_HTOI = 17;
    static final int  BUILTIN_LENGTH = 18;
    static final int  BUILTIN_MATCH = 19;
    static final int  BUILTIN_MERCATORTOYDAT = 20;
    static final int  BUILTIN_PARSEDATE = 21;
    static final int  BUILTIN_PARSETIMER = 22;
    static final int  BUILTIN_PRINTF = 23;
    static final int  BUILTIN_RANDOM = 24;
    static final int  BUILTIN_REPLACE = 25;
    static final int  BUILTIN_SELECT = 26;
    static final int  BUILTIN_SIZEOF = 27;
    static final int  BUILTIN_STARTSWITH = 28;
    static final int  BUILTIN_STRFMT = 29;
    static final int  BUILTIN_SUBSTRING = 30;
    static final int  BUILTIN_TIMERFORMAT = 31;
    static final int  BUILTIN_TOLOWERCASE = 32;
    static final int  BUILTIN_TOUPPERCASE = 33;
    static final int  BUILTIN_TRIMQUERYSTRING = 34;
    static final int  BUILTIN_URLDECODE = 35;
    static final int  BUILTIN_URLENCODE = 36;

    static Integer  $BUILTIN_ADJUSTTIME = new Integer(BUILTIN_ADJUSTTIME);
    static Integer  $BUILTIN_ATOH = new Integer(BUILTIN_ATOH);
    static Integer  $BUILTIN_BTOA = new Integer(BUILTIN_BTOA);
    static Integer  $BUILTIN_BTOI = new Integer(BUILTIN_BTOI);
    static Integer  $BUILTIN_CONSTANT = new Integer(BUILTIN_CONSTANT);
    static Integer  $BUILTIN_CONTAINS = new Integer(BUILTIN_CONTAINS);
    static Integer  $BUILTIN_DATE = new Integer(BUILTIN_DATE);
    static Integer  $BUILTIN_ENDSWITH = new Integer(BUILTIN_ENDSWITH);
    static Integer  $BUILTIN_EQUALS = new Integer(BUILTIN_EQUALS);
    static Integer  $BUILTIN_GETFILEEXTENSION = new Integer(BUILTIN_GETFILEEXTENSION);
    static Integer  $BUILTIN_GETINDEX = new Integer(BUILTIN_GETINDEX);
    static Integer  $BUILTIN_GETKEY = new Integer(BUILTIN_GETKEY);
    static Integer  $BUILTIN_GETQUERYSTRING = new Integer(BUILTIN_GETQUERYSTRING);
    static Integer  $BUILTIN_GETSEARCHQUERY = new Integer(BUILTIN_GETSEARCHQUERY);
    static Integer  $BUILTIN_GETVALUE = new Integer(BUILTIN_GETVALUE);
    static Integer  $BUILTIN_HTOA = new Integer(BUILTIN_HTOA);
    static Integer  $BUILTIN_HTOI = new Integer(BUILTIN_HTOI);
    static Integer  $BUILTIN_LENGTH = new Integer(BUILTIN_LENGTH);
    static Integer  $BUILTIN_MATCH = new Integer(BUILTIN_MATCH);
    static Integer  $BUILTIN_MERCATORTOYDAT = new Integer(BUILTIN_MERCATORTOYDAT);
    static Integer  $BUILTIN_PARSEDATE = new Integer(BUILTIN_PARSEDATE);
    static Integer  $BUILTIN_PARSETIMER = new Integer(BUILTIN_PARSETIMER);
    static Integer  $BUILTIN_PRINTF = new Integer(BUILTIN_PRINTF);
    static Integer  $BUILTIN_RANDOM = new Integer(BUILTIN_RANDOM);
    static Integer  $BUILTIN_REPLACE = new Integer(BUILTIN_REPLACE);
    static Integer  $BUILTIN_SELECT = new Integer(BUILTIN_SELECT);
    static Integer  $BUILTIN_SIZEOF = new Integer(BUILTIN_SIZEOF);
    static Integer  $BUILTIN_STARTSWITH = new Integer(BUILTIN_STARTSWITH);
    static Integer  $BUILTIN_STRFMT = new Integer(BUILTIN_STRFMT);
    static Integer  $BUILTIN_SUBSTRING = new Integer(BUILTIN_SUBSTRING);
    static Integer  $BUILTIN_TIMERFORMAT = new Integer(BUILTIN_TIMERFORMAT);
    static Integer  $BUILTIN_TOLOWERCASE = new Integer(BUILTIN_TOLOWERCASE);
    static Integer  $BUILTIN_TOUPPERCASE = new Integer(BUILTIN_TOUPPERCASE);
    static Integer  $BUILTIN_TRIMQUERYSTRING = new Integer(BUILTIN_TRIMQUERYSTRING);
    static Integer  $BUILTIN_URLDECODE = new Integer(BUILTIN_URLDECODE);
    static Integer  $BUILTIN_URLENCODE = new Integer(BUILTIN_URLENCODE);

    //
    // Sorting constants
    //

    static Integer  $VL_SORT_COLOR = new Integer(VL_SORT_COLOR);
    static Integer  $VL_SORT_DIVERSITY = new Integer(VL_SORT_DIVERSITY);
    static Integer  $VL_SORT_DIVERSITY2 = new Integer(VL_SORT_DIVERSITY2);
    static Integer  $VL_SORT_IP = new Integer(VL_SORT_IP);
    static Integer  $VL_SORT_LOAD_ORDER = new Integer(VL_SORT_LOAD_ORDER);
    static Integer  $VL_SORT_NUMBER = new Integer(VL_SORT_NUMBER);
    static Integer  $VL_SORT_NUMERIC = new Integer(VL_SORT_NUMERIC);
    static Integer  $VL_SORT_OCTET = new Integer(VL_SORT_OCTET);
    static Integer  $VL_SORT_PRESSED = new Integer(VL_SORT_PRESSED);
    static Integer  $VL_SORT_SELECTED = new Integer(VL_SORT_SELECTED);
    static Integer  $VL_SORT_SELECTED2 = new Integer(VL_SORT_SELECTED2);
    static Integer  $VL_SORT_TEXT = new Integer(VL_SORT_TEXT);
    static Integer  $VL_SORT_TIME = new Integer(VL_SORT_TIME);
    static Integer  $VL_SORT_TOTAL = new Integer(VL_SORT_TOTAL);
    static Integer  $VL_SORT_TRANSLATOR = new Integer(VL_SORT_TRANSLATOR);

    //
    // Operation constants
    //

    static Integer  $VL_OP_BRUSH = new Integer(VL_OP_BRUSH);
    static Integer  $VL_OP_DRAG = new Integer(VL_OP_DRAG);
    static Integer  $VL_OP_DRAW = new Integer(VL_OP_DRAW);
    static Integer  $VL_OP_EDIT = new Integer(VL_OP_EDIT);
    static Integer  $VL_OP_GRAB = new Integer(VL_OP_GRAB);
    static Integer  $VL_OP_NONE = new Integer(VL_OP_NONE);
    static Integer  $VL_OP_PAN = new Integer(VL_OP_PAN);
    static Integer  $VL_OP_POINT = new Integer(VL_OP_POINT);
    static Integer  $VL_OP_PRESS = new Integer(VL_OP_PRESS);
    static Integer  $VL_OP_SCROLL = new Integer(VL_OP_SCROLL);
    static Integer  $VL_OP_SELECT = new Integer(VL_OP_SELECT);
    static Integer  $VL_OP_TIP = new Integer(VL_OP_TIP);
    static Integer  $VL_OP_ZOOM = new Integer(VL_OP_ZOOM);

    //
    // GraphPlot constants
    //

    static Integer  $DATA_GRAPH_NODE = new Integer(SwingJGraphPlot.DATA_GRAPH_NODE);
    static Integer  $DATA_GRAPH_EDGE = new Integer(SwingJGraphPlot.DATA_GRAPH_EDGE);
    static Integer  $DATA_BACKGROUND_EDGE = new Integer(SwingJGraphPlot.DATA_BACKGROUND_EDGE);
    static Integer  $DATA_BACKGROUND_NODE = new Integer(SwingJGraphPlot.DATA_BACKGROUND_NODE);

    static Integer  $DATA_GRAPH_MASK = new Integer(SwingJGraphPlot.DATA_GRAPH_MASK);
    static Integer  $DATA_BACKGROUND_MASK = new Integer(SwingJGraphPlot.DATA_BACKGROUND_MASK);

    //
    // Style and connect constants for plots.
    //

    static Integer  $STYLE_BARS = new Integer(STYLE_BARS);
    static Integer  $STYLE_POINTS = new Integer(STYLE_POINTS);
    static Integer  $STYLE_POLYGONS = new Integer(STYLE_POLYGONS);
    static Integer  $STYLE_STACKED_BARS = new Integer(STYLE_STACKED_BARS);
    static Integer  $STYLE_STACKED_POINTS = new Integer(STYLE_STACKED_POINTS);
    static Integer  $STYLE_STACKED_POLYGONS = new Integer(STYLE_STACKED_POLYGONS);

    static Integer  $STYLE_ENABLE_EVENTS = new Integer(STYLE_ENABLE_EVENTS);
    static Integer  $STYLE_ENABLE_STACKS = new Integer(STYLE_ENABLE_STACKS);
    static Integer  $STYLE_ENABLE_MASK = new Integer(STYLE_ENABLE_MASK);

    static Integer  $CONNECT_LINES = new Integer(CONNECT_LINES);
    static Integer  $CONNECT_NONE = new Integer(CONNECT_NONE);

    //
    // Sweep constants.
    //

    static Integer  $SWEEP_ENABLED = new Integer(SWEEP_ENABLED);
    static Integer  $SWEEP_SOLID_STACKS = new Integer(SWEEP_SOLID_STACKS);

    //
    // Label flags.
    //

    static Integer  $LABEL_SHOWCOUNT = new Integer(LABEL_SHOWCOUNT);
    static Integer  $LABEL_SHOWVALUE = new Integer(LABEL_SHOWVALUE);
    static Integer  $LABEL_SHOWDIVERSITY = new Integer(LABEL_SHOWDIVERSITY);
    static Integer  $LABEL_HIDE = new Integer(LABEL_HIDE);
    static Integer  $LABEL_HIDENODE = new Integer(LABEL_HIDENODE);
    static Integer  $LABEL_HIDEEDGE = new Integer(LABEL_HIDEEDGE);
    static Integer  $LABEL_PICKCOLORNODE = new Integer(LABEL_PICKCOLORNODE);
    static Integer  $LABEL_SHOWCOUNTPCNT = new Integer(LABEL_SHOWCOUNTPCNT);
    static Integer  $LABEL_SHOWVALUEPCNT = new Integer(LABEL_SHOWVALUEPCNT);

    //
    // Tip constants.
    //

    static Integer  $TIP_OVER_EDGES = new Integer(TIP_OVER_EDGES);
    static Integer  $TIP_OVER_EVENTS = new Integer(TIP_OVER_EVENTS);
    static Integer  $TIP_OVER_NODES = new Integer(TIP_OVER_NODES);
    static Integer  $TIP_OVER_STACKS = new Integer(TIP_OVER_STACKS);
    static Integer  $TIP_OVER_MASK = new Integer(TIP_OVER_MASK);
    static Integer  $TIP_SHOW_COUNT = new Integer(TIP_SHOW_COUNT);
    static Integer  $TIP_SHOW_COUNTPCNT = new Integer(TIP_SHOW_COUNTPCNT);
    static Integer  $TIP_SHOW_RANK = new Integer(TIP_SHOW_RANK);
    static Integer  $TIP_SHOW_TIES = new Integer(TIP_SHOW_TIES);
    static Integer  $TIP_SHOW_VALUE = new Integer(TIP_SHOW_VALUE);
    static Integer  $TIP_SHOW_VALUEPCNT = new Integer(TIP_SHOW_VALUEPCNT);

    //
    // Temporary definitions - just for backward compatibility and  they
    // all probably can be removed when GFMS is updated.
    //

    static Integer  $STYLE_PIXEL_LINES = new Integer(STYLE_BARS);
    static Integer  $STYLE_PIXEL_POINTS = new Integer(STYLE_POINTS);
    static Integer  $STYLE_PIXEL_STACKS = new Integer(STYLE_BARS);
    static Integer  $STYLE_NORMAL_LINES = new Integer(STYLE_BARS);
    static Integer  $STYLE_NORMAL_POINTS = new Integer(STYLE_POINTS);
    static Integer  $STYLE_NORMAL_STACKS = new Integer(STYLE_BARS);
    static Integer  $STYLE_MEDIUM_LINES = new Integer(STYLE_BARS);
    static Integer  $STYLE_MEDIUM_POINTS = new Integer(STYLE_POINTS);
    static Integer  $STYLE_MEDIUM_STACKS = new Integer(STYLE_BARS);
    static Integer  $STYLE_LARGE_LINES = new Integer(STYLE_BARS);
    static Integer  $STYLE_LARGE_POINTS = new Integer(STYLE_POINTS);
    static Integer  $STYLE_LARGE_STACKS = new Integer(STYLE_BARS);
    static Integer  $STYLE_HUGE_LINES = new Integer(STYLE_BARS);
    static Integer  $STYLE_HUGE_POINTS = new Integer(STYLE_POINTS);
    static Integer  $STYLE_HUGE_STACKS = new Integer(STYLE_BARS);

    static String   NL_OP_ACTION = "OP_ACTION";
    static Integer  $VL_OP_ACTION = new Integer(-1);

    //
    // Miscellaneous stuff
    //

    static Integer  $STANDARDCURSOR = new Integer(V_STANDARD_CURSOR);
    static Insets   $HISTOGRAMIPAD = new Insets(0, 3, 0, 3);
    static String   $ZEROSIZE = "ZERO_SIZE";

    public static Object  $module[] = {
    //
    // NAME                        ARG                        COMMAND     MODE   REFERENCE
    // ----                        ---                        -------     ----   ---------
       $MODULENAME,                "141",                     $LIST,      $RORO, $MODULENAME,
       "DATA_ARRAY",               $DATA_ARRAY,               $INTEGER,   $LR__, null,
       "DATA_CALL",                $DATA_CALL,                $INTEGER,   $LR__, null,
       "DATA_COUNTER",             $DATA_COUNTER,             $INTEGER,   $LR__, null,
       "DATA_DICTIONARY",          $DATA_DICTIONARY,          $INTEGER,   $LR__, null,
       "DATA_DOUBLE",              $DATA_DOUBLE,              $INTEGER,   $LR__, null,
       "DATA_INTEGER",             $DATA_INTEGER,             $INTEGER,   $LR__, null,
       "DATA_ONE",                 $DATA_ONE,                 $INTEGER,   $LR__, null,
       "DATA_PARTITION",           $DATA_PARTITION,           $INTEGER,   $LR__, null,
       "DATA_STRING",              $DATA_STRING,              $INTEGER,   $LR__, null,
       "DATA_TABLE",               $DATA_TABLE,               $INTEGER,   $LR__, null,
       "DATA_TABLE_NEW",           $DATA_TABLE_NEW,           $INTEGER,   $LR__, null,
       "DATA_TABLECOLUMN",         $DATA_TABLECOLUMN,         $INTEGER,   $LR__, null,
       "DATA_TABLECOLUMN_NEW",     $DATA_TABLECOLUMN_NEW,     $INTEGER,   $LR__, null,
       "DATA_UID",                 $DATA_UID,                 $INTEGER,   $LR__, null,
       "DATA_ZERO",                $DATA_ZERO,                $INTEGER,   $LR__, null,
       "BUILTIN_ADJUSTTIME",       $BUILTIN_ADJUSTTIME,       $INTEGER,   $LR__, null,
       "BUILTIN_ATOH",             $BUILTIN_ATOH,             $INTEGER,   $LR__, null,
       "BUILTIN_BTOA",             $BUILTIN_BTOA,             $INTEGER,   $LR__, null,
       "BUILTIN_BTOI",             $BUILTIN_BTOI,             $INTEGER,   $LR__, null,
       "BUILTIN_CONSTANT",         $BUILTIN_CONSTANT,         $INTEGER,   $LR__, null,
       "BUILTIN_CONTAINS",         $BUILTIN_CONTAINS,         $INTEGER,   $LR__, null,
       "BUILTIN_DATE",             $BUILTIN_DATE,             $INTEGER,   $LR__, null,
       "BUILTIN_ENDSWITH",         $BUILTIN_ENDSWITH,         $INTEGER,   $LR__, null,
       "BUILTIN_EQUALS",           $BUILTIN_EQUALS,           $INTEGER,   $LR__, null,
       "BUILTIN_GETFILEEXTENSION", $BUILTIN_GETFILEEXTENSION, $INTEGER,   $LR__, null,
       "BUILTIN_GETINDEX",         $BUILTIN_GETINDEX,         $INTEGER,   $LR__, null,
       "BUILTIN_GETKEY",           $BUILTIN_GETKEY,           $INTEGER,   $LR__, null,
       "BUILTIN_GETQUERYSTRING",   $BUILTIN_GETQUERYSTRING,   $INTEGER,   $LR__, null,
       "BUILTIN_GETSEARCHQUERY",   $BUILTIN_GETSEARCHQUERY,   $INTEGER,   $LR__, null,
       "BUILTIN_GETVALUE",         $BUILTIN_GETVALUE,         $INTEGER,   $LR__, null,
       "BUILTIN_HTOA",             $BUILTIN_HTOA,             $INTEGER,   $LR__, null,
       "BUILTIN_HTOI",             $BUILTIN_HTOI,             $INTEGER,   $LR__, null,
       "BUILTIN_LENGTH",           $BUILTIN_LENGTH,           $INTEGER,   $LR__, null,
       "BUILTIN_MATCH",            $BUILTIN_MATCH,            $INTEGER,   $LR__, null,
       "BUILTIN_MERCATORTOYDAT",   $BUILTIN_MERCATORTOYDAT,   $INTEGER,   $LR__, null,
       "BUILTIN_PARSEDATE",        $BUILTIN_PARSEDATE,        $INTEGER,   $LR__, null,
       "BUILTIN_PARSETIMER",       $BUILTIN_PARSETIMER,       $INTEGER,   $LR__, null,
       "BUILTIN_PRINTF",           $BUILTIN_PRINTF,           $INTEGER,   $LR__, null,
       "BUILTIN_RANDOM",           $BUILTIN_RANDOM,           $INTEGER,   $LR__, null,
       "BUILTIN_REPLACE",          $BUILTIN_REPLACE,          $INTEGER,   $LR__, null,
       "BUILTIN_SELECT",           $BUILTIN_SELECT,           $INTEGER,   $LR__, null,
       "BUILTIN_SIZEOF",           $BUILTIN_SIZEOF,           $INTEGER,   $LR__, null,
       "BUILTIN_STARTSWITH",       $BUILTIN_STARTSWITH,       $INTEGER,   $LR__, null,
       "BUILTIN_STRFMT",           $BUILTIN_STRFMT,           $INTEGER,   $LR__, null,
       "BUILTIN_SUBSTRING",        $BUILTIN_SUBSTRING,        $INTEGER,   $LR__, null,
       "BUILTIN_TIMERFORMAT",      $BUILTIN_TIMERFORMAT,      $INTEGER,   $LR__, null,
       "BUILTIN_TOLOWERCASE",      $BUILTIN_TOLOWERCASE,      $INTEGER,   $LR__, null,
       "BUILTIN_TOUPPERCASE",      $BUILTIN_TOUPPERCASE,      $INTEGER,   $LR__, null,
       "BUILTIN_TRIMQUERYSTRING",  $BUILTIN_TRIMQUERYSTRING,  $INTEGER,   $LR__, null,
       "BUILTIN_URLDECODE",        $BUILTIN_URLDECODE,        $INTEGER,   $LR__, null,
       "BUILTIN_URLENCODE",        $BUILTIN_URLENCODE,        $INTEGER,   $LR__, null,
       "OVERLAP_GENERATOR",        $OVERLAP_GENERATOR,        $INTEGER,   $LR__, null,

       "DATA_BACKGROUND_EDGE",     $DATA_BACKGROUND_EDGE,     $INTEGER,   $LR__, null,
       "DATA_BACKGROUND_MASK",     $DATA_BACKGROUND_MASK,     $INTEGER,   $LR__, null,
       "DATA_BACKGROUND_NODE",     $DATA_BACKGROUND_NODE,     $INTEGER,   $LR__, null,
       "DATA_GRAPH_EDGE",          $DATA_GRAPH_EDGE,          $INTEGER,   $LR__, null,
       "DATA_GRAPH_MASK",          $DATA_GRAPH_MASK,          $INTEGER,   $LR__, null,
       "DATA_GRAPH_NODE",          $DATA_GRAPH_NODE,          $INTEGER,   $LR__, null,

       "SWEEPGRAPH_MARKERS",       $SWEEPGRAPH_MARKERS,       $INTEGER,   $LR__, null,
       "SWEEPGRAPH_WIDTHRATIO",    $SWEEPGRAPH_WIDTHRATIO,    $INTEGER,   $LR__, null,

       NL_SORT_COLOR,              $VL_SORT_COLOR,            $INTEGER,   $LR__, null,
       NL_SORT_DIVERSITY,          $VL_SORT_DIVERSITY,        $INTEGER,   $LR__, null,
       NL_SORT_DIVERSITY2,         $VL_SORT_DIVERSITY2,       $INTEGER,   $LR__, null,
       NL_SORT_IP,                 $VL_SORT_IP,               $INTEGER,   $LR__, null,
       NL_SORT_LOAD_ORDER,         $VL_SORT_LOAD_ORDER,       $INTEGER,   $LR__, null,
       NL_SORT_NUMBER,             $VL_SORT_NUMBER,           $INTEGER,   $LR__, null,
       NL_SORT_NUMERIC,            $VL_SORT_NUMERIC,          $INTEGER,   $LR__, null,
       NL_SORT_OCTET,              $VL_SORT_OCTET,            $INTEGER,   $LR__, null,
       NL_SORT_PRESSED,            $VL_SORT_PRESSED,          $INTEGER,   $LR__, null,
       NL_SORT_SELECTED,           $VL_SORT_SELECTED,         $INTEGER,   $LR__, null,
       NL_SORT_SELECTED2,          $VL_SORT_SELECTED2,        $INTEGER,   $LR__, null,
       NL_SORT_TEXT,               $VL_SORT_TEXT,             $INTEGER,   $LR__, null,
       NL_SORT_TIME,               $VL_SORT_TIME,             $INTEGER,   $LR__, null,
       NL_SORT_TOTAL,              $VL_SORT_TOTAL,            $INTEGER,   $LR__, null,
       NL_SORT_TRANSLATOR,         $VL_SORT_TRANSLATOR,       $INTEGER,   $LR__, null,

       NL_OP_BRUSH,                $VL_OP_BRUSH,              $INTEGER,   $LR__, null,
       NL_OP_DRAG,                 $VL_OP_DRAG,               $INTEGER,   $LR__, null,
       NL_OP_DRAW,                 $VL_OP_DRAW,               $INTEGER,   $LR__, null,
       NL_OP_EDIT,                 $VL_OP_EDIT,               $INTEGER,   $LR__, null,
       NL_OP_GRAB,                 $VL_OP_GRAB,               $INTEGER,   $LR__, null,
       NL_OP_NONE,                 $VL_OP_NONE,               $INTEGER,   $LR__, null,
       NL_OP_PAN,                  $VL_OP_PAN,                $INTEGER,   $LR__, null,
       NL_OP_POINT,                $VL_OP_POINT,              $INTEGER,   $LR__, null,
       NL_OP_PRESS,                $VL_OP_PRESS,              $INTEGER,   $LR__, null,
       NL_OP_SCROLL,               $VL_OP_SCROLL,             $INTEGER,   $LR__, null,
       NL_OP_SELECT,               $VL_OP_SELECT,             $INTEGER,   $LR__, null,
       NL_OP_TIP,                  $VL_OP_TIP,                $INTEGER,   $LR__, null,
       NL_OP_ZOOM,                 $VL_OP_ZOOM,               $INTEGER,   $LR__, null,

       "STYLE_BARS",               $STYLE_BARS,               $INTEGER,   $LR__, null,
       "STYLE_POINTS",             $STYLE_POINTS,             $INTEGER,   $LR__, null,
       "STYLE_POLYGONS",           $STYLE_POLYGONS,           $INTEGER,   $LR__, null,
       "STYLE_STACKED_BARS",       $STYLE_STACKED_BARS,       $INTEGER,   $LR__, null,
       "STYLE_STACKED_POINTS",     $STYLE_STACKED_POINTS,     $INTEGER,   $LR__, null,
       "STYLE_STACKED_POLYGONS",   $STYLE_STACKED_POLYGONS,   $INTEGER,   $LR__, null,

       "STYLE_ENABLE_EVENTS",      $STYLE_ENABLE_EVENTS,      $INTEGER,   $LR__, null,
       "STYLE_ENABLE_STACKS",      $STYLE_ENABLE_STACKS,      $INTEGER,   $LR__, null,
       "STYLE_ENABLE_MASK",        $STYLE_ENABLE_MASK,        $INTEGER,   $LR__, null,

       "CONNECT_LINES",            $CONNECT_LINES,            $INTEGER,   $LR__, null,
       "CONNECT_NONE",             $CONNECT_NONE,             $INTEGER,   $LR__, null,

       "SWEEP_ENABLED",            $SWEEP_ENABLED,            $INTEGER,   $LR__, null,
       "SWEEP_SOLID_STACKS",       $SWEEP_SOLID_STACKS,       $INTEGER,   $LR__, null,

       "LABEL_SHOWCOUNT",          $LABEL_SHOWCOUNT,          $INTEGER,   $LR__, null,
       "LABEL_SHOWVALUE",          $LABEL_SHOWVALUE,          $INTEGER,   $LR__, null,
       "LABEL_SHOWDIVERSITY",      $LABEL_SHOWDIVERSITY,      $INTEGER,   $LR__, null,
       "LABEL_HIDE",               $LABEL_HIDE,               $INTEGER,   $LR__, null,
       "LABEL_HIDENODE",           $LABEL_HIDENODE,           $INTEGER,   $LR__, null,
       "LABEL_HIDEEDGE",           $LABEL_HIDEEDGE,           $INTEGER,   $LR__, null,
       "LABEL_PICKCOLORNODE",      $LABEL_PICKCOLORNODE,      $INTEGER,   $LR__, null,
       "LABEL_SHOWCOUNTPCNT",      $LABEL_SHOWCOUNTPCNT,      $INTEGER,   $LR__, null,
       "LABEL_SHOWVALUEPCNT",      $LABEL_SHOWVALUEPCNT,      $INTEGER,   $LR__, null,

       "TIP_OVER_EDGES",           $TIP_OVER_EDGES,           $INTEGER,   $LR__, null,
       "TIP_OVER_EVENTS",          $TIP_OVER_EVENTS,          $INTEGER,   $LR__, null,
       "TIP_OVER_MASK",            $TIP_OVER_MASK,            $INTEGER,   $LR__, null,
       "TIP_OVER_NODES",           $TIP_OVER_NODES,           $INTEGER,   $LR__, null,
       "TIP_OVER_STACKS",          $TIP_OVER_STACKS,          $INTEGER,   $LR__, null,
       "TIP_SHOW_COUNT",           $TIP_SHOW_COUNT,           $INTEGER,   $LR__, null,
       "TIP_SHOW_COUNTPCNT",       $TIP_SHOW_COUNTPCNT,       $INTEGER,   $LR__, null,
       "TIP_SHOW_RANK",            $TIP_SHOW_RANK,            $INTEGER,   $LR__, null,
       "TIP_SHOW_TIES",            $TIP_SHOW_TIES,            $INTEGER,   $LR__, null,
       "TIP_SHOW_VALUE",           $TIP_SHOW_VALUE,           $INTEGER,   $LR__, null,
       "TIP_SHOW_VALUEPCNT",       $TIP_SHOW_VALUEPCNT,       $INTEGER,   $LR__, null,

    //
    // These constants are for backward compatibility and can be removed
    // when GFMS is updated.
    //

       NL_OP_ACTION,               $VL_OP_ACTION,             $INTEGER,   $LR__, null,
       "STYLE_PIXEL_LINES",        $STYLE_PIXEL_LINES,        $INTEGER,   $LR__, null,
       "STYLE_PIXEL_POINTS",       $STYLE_PIXEL_POINTS,       $INTEGER,   $LR__, null,
       "STYLE_PIXEL_STACKS",       $STYLE_PIXEL_STACKS,       $INTEGER,   $LR__, null,
       "STYLE_NORMAL_LINES",       $STYLE_NORMAL_LINES,       $INTEGER,   $LR__, null,
       "STYLE_NORMAL_POINTS",      $STYLE_NORMAL_POINTS,      $INTEGER,   $LR__, null,
       "STYLE_NORMAL_STACKS",      $STYLE_NORMAL_STACKS,      $INTEGER,   $LR__, null,
       "STYLE_MEDIUM_LINES",       $STYLE_MEDIUM_LINES,       $INTEGER,   $LR__, null,
       "STYLE_MEDIUM_POINTS",      $STYLE_MEDIUM_POINTS,      $INTEGER,   $LR__, null,
       "STYLE_MEDIUM_STACKS",      $STYLE_MEDIUM_STACKS,      $INTEGER,   $LR__, null,
       "STYLE_LARGE_LINES",        $STYLE_LARGE_LINES,        $INTEGER,   $LR__, null,
       "STYLE_LARGE_POINTS",       $STYLE_LARGE_POINTS,       $INTEGER,   $LR__, null,
       "STYLE_LARGE_STACKS",       $STYLE_LARGE_STACKS,       $INTEGER,   $LR__, null,
       "STYLE_HUGE_LINES",         $STYLE_HUGE_LINES,         $INTEGER,   $LR__, null,
       "STYLE_HUGE_POINTS",        $STYLE_HUGE_POINTS,        $INTEGER,   $LR__, null,
       "STYLE_HUGE_STACKS",        $STYLE_HUGE_STACKS,        $INTEGER,   $LR__, null,

       "buildDataFields",          "-3",                      $BUILTIN,   $LR_X, null,
       "dotPlot",                  "-2",                      $BUILTIN,   $LR_X, null,
       "graphPlotText",            "-1",                      $BUILTIN,   $LR_X, null,
       "mercatorToYDAT",           "-4",                      $BUILTIN,   $LR_X, null,

    //
    // Temporary objects - assumes the required types exist in typedict.
    //

       null,                       T_DIMENSION,               $DECLARE,   $RW_,  $ZEROSIZE,
       N_HEIGHT,                   "0.0",                     $DOUBLE,    $RW_,  null,
       N_WIDTH,                    "0.0",                     $DOUBLE,    $RW_,  null,

    //
    // Type templates - actual typedefs follow.
    //

       T_PALETTE,                  "13",                      $DICT,      $L___, T_PALETTE,
       null,                       "-1",                      $GROWTO,    null,  null,
       NY_CLASSNAME,               null,                      $CLASS,     $RORO, null,
       NY_MAJOR,                   $PALETTE,                  $INTEGER,   $LR__, null,
       NY_MINOR,                   "0",                       $INTEGER,   $LR__, null,
       NL_AFTERSELECT,             T_CALLABLE,                $NULL,      $RWX,  null,
       NL_BRIGHTNESS,              "1.0",                     $DOUBLE,    $RW_,  null,
       NL_CANRANK,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NL_COLORS,                  T_ARRAY,                   $NULL,      $RW_,  null,
       NL_HUE,                     "0.0",                     $DOUBLE,    $RW_,  null,
       NL_INVERTED,                $FALSE,                    $INTEGER,   $RW_,  null,
       NL_MODEL,                   "2",                       $INTEGER,   $RW_,  null,
       NL_SATURATION,              "1.0",                     $DOUBLE,    $RW_,  null,
       NL_SELECT,                  T_CALLABLE,                $NULL,      $L__X, null,
       NL_SEQUENCE,                $FALSE,                    $INTEGER,   $RW_,  null,
       null,                       null,                      $TYPEDEF,   null,  null,

       T_AXIS,                     "48",                      $DICT,      $L___, T_AXIS,
       null,                       "-1",                      $GROWTO,    null,  null,
       NY_CLASSNAME,               null,                      $CLASS,     $RORO, null,
       NY_MAJOR,                   $JCOMPONENT,               $INTEGER,   $LR__, null,
       NY_MINOR,                   $JAXIS,                    $INTEGER,   $LR__, null,
       NY_ANCHOR,                  $YOIX_NORTH,               $INTEGER,   $RW_,  null,
       NL_AXISENDS,                T_ARRAY,                   $NULL,      $RW_,  null,
       NL_AXISLIMITS,              T_ARRAY,                   $NULL,      $LR__, null,
       NL_AXISMODEL,               "0",                       $INTEGER,   $RW_,  null,
       NL_AXISWIDTH,               "0",                       $DOUBLE,    $RW_,  null,
       NY_BACKGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_BORDER,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_BORDERCOLOR,             T_COLOR,                   $NULL,      $RW_,  null,
       NY_CELLSIZE,                T_DIMENSION,               $DECLARE,   $LR__, null,
       NY_COLUMNS,                 "1",                       $INTEGER,   $RW_,  null,
       NY_CURSOR,                  $STANDARDCURSOR,           $INTEGER,   $RW_,  null,
       NY_ENABLED,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NY_EXTENT,                  T_DIMENSION,               $DECLARE,   $LR__, null,
       NY_ETC,                     T_OBJECT,                  $NULL,      $LR__, null,
       NY_FONT,                    T_OBJECT,                  $NULL,      $RW_,  null,
       NY_FOREGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NL_FROZEN,                  $FALSE,                    $INTEGER,   $RW_,  null,
       NL_GETZOOMENDS,             T_CALLABLE,                $NULL,      $L__X, null,
       NY_INSETS,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NL_INVERTED,                $FALSE,                    $INTEGER,   $RW_,  null,
       NL_LABELS,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NL_LINEWIDTH,               "1",                       $DOUBLE,    $RW_,  null,
       NY_MAXIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NY_MINIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NL_ORIENTATION,             $YOIX_HORIZONTAL,          $INTEGER,   $LR__, null,
       NY_ORIGIN,                  T_POINT,                   $DECLARE,   $RW_,  null,
       NL_PLOT,                    T_OBJECT,                  $NULL,      $LR__, null,
       NY_POPUP,                   T_POPUPMENU,               $NULL,      $RW_,  null,
       NY_PREFERREDSIZE,           T_DIMENSION,               $NULL,      $RW_,  null,
       NY_REQUESTFOCUS,            $FALSE,                    $INTEGER,   $RW_,  null,
       NY_ROOT,                    T_OBJECT,                  $NULL,      $LR__, null,
       NY_ROWS,                    "1",                       $INTEGER,   $RW_,  null,
       NY_SAVEGRAPHICS,            $TRUE,                     $INTEGER,   $RW_,  null,
       NY_SHOWING,                 $FALSE,                    $INTEGER,   $LR__, null,
       NY_SIZE,                    T_DIMENSION,               $NULL,      $RW_,  null,
       NL_SLIDERCOLOR,             T_COLOR,                   $NULL,      $RW_,  null,
       NL_SLIDERENABLED,           $TRUE,                     $INTEGER,   $RW_,  null,
       NL_SLIDERENDS,              T_ARRAY,                   $NULL,      $RW_,  null,
       NY_STATE,                   $FALSE,                    $INTEGER,   $RW_,  null,
       NL_SUBORDINATEPLOTS,        T_CALLABLE,                $NULL,      $L__X, null,
       NY_TAG,                     T_STRING,                  $NULL,      $RW_,  null,
       NL_TEXT,                    T_STRING,                  $NULL,      $LR__, null,
       NL_TICKS,                   T_OBJECT,                  $NULL,      $RW_,  null,
       NY_VIEWPORT,                T_RECTANGLE,               $DECLARE,   $LR__, null,
       NY_VISIBLE,                 $TRUE,                     $INTEGER,   $RW_,  null,
       null,                       null,                      $TYPEDEF,   null,  null,

       T_HISTOGRAM,                "80",                      $DICT,      $L___, T_HISTOGRAM,
       null,                       "-1",                      $GROWTO,    null,  null,
       NY_CLASSNAME,               null,                      $CLASS,     $RORO, null,
       NY_MAJOR,                   $JCOMPONENT,               $INTEGER,   $LR__, null,
       NY_MINOR,                   $JHISTOGRAM,               $INTEGER,   $LR__, null,
       NL_ACCUMULATE,              $FALSE,                    $INTEGER,   $RW_,  null,
       NL_ACTIVE,                  "0",                       $INTEGER,   $RW_,  null,
       NL_ACTIVEFIELDCOUNT,        "1",                       $INTEGER,   $RW_,  null,
       NL_AFTERLOAD,               T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERPRESSED,            T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERSWEEP,              T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERUPDATE,             T_CALLABLE,                $NULL,      $RWX,  null,
       NY_ALIGNMENT,               $YOIX_RIGHT,               $INTEGER,   $RW_,  null,
       NL_ALIVE,                   $TRUE,                     $INTEGER,   $RW_,  null,
       NY_ANCHOR,                  $YOIX_LEFT,                $INTEGER,   $RW_,  null,
       NL_AUTOREADY,               $FALSE,                    $INTEGER,   $RW_,  null,
       NL_AUTOSCROLL,              $TRUE,                     $INTEGER,   $RW_,  null,
       NL_AUTOSHOW,                $FALSE,                    $INTEGER,   $RW_,  null,
       NY_BACKGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NL_BARSPACE,                "0.5",                     $DOUBLE,    $RW_,  null,
       NY_BORDER,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_BORDERCOLOR,             T_COLOR,                   $NULL,      $RW_,  null,
       NY_CELLSIZE,                T_DIMENSION,               $DECLARE,   $LR__, null,
       NL_CLEAR,                   T_CALLABLE,                $NULL,      $L__X, null,
       NL_COLLECTRECORDSAT,        T_CALLABLE,                $NULL,      $L__X, null,
       NY_COLUMNS,                 "0",                       $INTEGER,   $RW_,  null,
       NY_CURSOR,                  $STANDARDCURSOR,           $INTEGER,   $RW_,  null,
       NL_DATAMANAGER,             T_OBJECT,                  $NULL,      $LR__, null,
       NL_EMPTYCOLOR,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_ENABLED,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NY_ETC,                     T_OBJECT,                  $NULL,      $LR__, null,
       NY_EXTENT,                  T_DIMENSION,               $DECLARE,   $LR__, null,
       NL_FIELDINDEX,              "-1",                      $INTEGER,   $LR__, null,
       NL_FIELDINDICES,            T_ARRAY,                   $NULL,      $LR__, null,
       NY_FINDNEXTMATCH,           T_CALLABLE,                $NULL,      $L__X, null,
       NY_FONT,                    T_OBJECT,                  $NULL,      $RW_,  null,
       NY_FOREGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NL_FROZEN,                  $FALSE,                    $INTEGER,   $RW_,  null,
       NL_GETTIPTEXTAT,            T_CALLABLE,                $NULL,      $L__X, null,
       NL_HIDEUNLABELED,           $FALSE,                    $INTEGER,   $RW_,  null,
       NL_HIGHLIGHTCOLOR,          T_COLOR,                   $NULL,      $RW_,  null,
       NL_HIGHLIGHTED,             T_OBJECT,                  $NULL,      $RW_,  null,
       NY_INSETS,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_IPAD,                    $HISTOGRAMIPAD,            $OBJECT,    $RW_,  null,
       NL_KEYS,                    T_ARRAY,                   $NULL,      $LR__, null,
       NL_LABELFLAGS,              "0",                       $INTEGER,   $RW_,  null,
       NL_LABELS,                  T_ARRAY,                   $NULL,      $LR__, null,
       NL_LOADRECORDS,             T_CALLABLE,                $NULL,      $L__X, null,
       NY_MAXIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NY_MINIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NL_OPERATIONS,              T_ARRAY,                   $NULL,      $RW_,  null,
       NY_ORIGIN,                  T_POINT,                   $DECLARE,   $RW_,  null,
       NL_OTHERCOLOR,              T_COLOR,                   $NULL,      $RW_,  null,
       NL_PALETTE,                 T_PALETTE,                 $NULL,      $RW_,  null,
       NL_PERSISTENT,              $TRUE,                     $INTEGER,   $RW_,  null,
       NY_POPUP,                   T_POPUPMENU,               $NULL,      $RW_,  null,
       NY_PREFERREDSIZE,           $ZEROSIZE,                 $GET,       $RW_,  null,
       NL_PRESSED,                 T_OBJECT,                  $NULL,      $RW_,  null,
       NL_PRESSEDCOLOR,            T_COLOR,                   $NULL,      $RW_,  null,
       NL_PRESSINGCOLOR,           T_COLOR,                   $NULL,      $RW_,  null,
       NL_RECOLORED,               $FALSE,                    $INTEGER,   $RW_,  null,
       NY_REQUESTFOCUS,            $FALSE,                    $INTEGER,   $RW_,  null,
       NL_REVERSEPALETTE,          $FALSE,                    $INTEGER,   $RW_,  null,
       NY_ROOT,                    T_OBJECT,                  $NULL,      $LR__, null,
       NY_ROWS,                    "0",                       $INTEGER,   $RW_,  null,
       NY_SAVEGRAPHICS,            $TRUE,                     $INTEGER,   $RW_,  null,
       NL_SELECTED,                T_OBJECT,                  $NULL,      $RW_,  null,
       NL_SETALL,                  T_CALLABLE,                $NULL,      $L__X, null,
       NY_SHOWING,                 $FALSE,                    $INTEGER,   $LR__, null,
       NY_SIZE,                    T_DIMENSION,               $NULL,      $RW_,  null,
       NL_SORTBY,                  $VL_SORT_TEXT,             $INTEGER,   $RW_,  null,
       NL_SORTDEFAULT,             $VL_SORT_TEXT,             $INTEGER,   $RW_,  null,
       NL_STACKED,                 $FALSE,                    $INTEGER,   $RW_,  null,
       NY_STATE,                   $FALSE,                    $INTEGER,   $RW_,  null,
       NY_SYNCCOUNT,               "0",                       $INTEGER,   $LR__, null,
       NY_SYNCVIEWPORT,            T_CALLABLE,                $NULL,      $RWX,  null,
       NY_TAG,                     T_STRING,                  $NULL,      $RW_,  null,
       NL_TEXT,                    T_STRING,                  $NULL,      $RW_,  null,
       NL_TRANSIENTMODE,           $FALSE,                    $INTEGER,   $RW_,  null,
       NL_TRANSLATOR,              T_DICT,                    $NULL,      $RW_,  null,
       NY_VIEWPORT,                T_RECTANGLE,               $DECLARE,   $LR__, null,
       NY_VISIBLE,                 $TRUE,                     $INTEGER,   $RW_,  null,
       null,                       null,                      $TYPEDEF,   null,  null,

    //
    // Note - NL_SYMMETRIC is initialized to $TRUE for backward compatibility
    // in tag plots, which really were the only centered plots that we ever
    // used.
    //

       T_EVENTPLOT,                "73",                      $DICT,      $L___, T_EVENTPLOT,
       null,                       "-1",                      $GROWTO,    null,  null,
       NY_CLASSNAME,               null,                      $CLASS,     $RORO, null,
       NY_MAJOR,                   $JCOMPONENT,               $INTEGER,   $LR__, null,
       NY_MINOR,                   $JEVENTPLOT,               $INTEGER,   $LR__, null,
       NL_AFTERAPPEND,             T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERLOAD,               T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERSWEEP,              T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERUPDATE,             T_CALLABLE,                $NULL,      $RWX,  null,
       NL_ALIVE,                   $TRUE,                     $INTEGER,   $RW_,  null,
       NY_ANCHOR,                  $YOIX_SOUTH,               $INTEGER,   $RW_,  null,
       NL_AUTOREADY,               $FALSE,                    $INTEGER,   $RW_,  null,
       NL_AXISWIDTH,               "0",                       $DOUBLE,    $RW_,  null,
       NY_BACKGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_BORDER,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_BORDERCOLOR,             T_COLOR,                   $NULL,      $RW_,  null,
       NY_CELLSIZE,                T_DIMENSION,               $DECLARE,   $LR__, null,
       NL_COLLECTRECORDSAT,        T_CALLABLE,                $NULL,      $L__X, null,
       NL_CONNECT,                 $FALSE,                    $INTEGER,   $RW_,  null,
       NL_CONNECTCOLOR,            T_COLOR,                   $NULL,      $RW_,  null,
       NL_CONNECTWIDTH,            "1",                       $DOUBLE,    $RW_,  null,
       NY_CURSOR,                  $STANDARDCURSOR,           $INTEGER,   $RW_,  null,
       NL_DATAENDS,                T_DICT,                    $NULL,      $LR__, null,
       NL_DATAMANAGER,             T_OBJECT,                  $NULL,      $LR__, null,
       NY_ENABLED,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NY_EXTENT,                  T_DIMENSION,               $DECLARE,   $LR__, null,
       NY_ETC,                     T_OBJECT,                  $NULL,      $LR__, null,
       NY_FONT,                    T_OBJECT,                  $NULL,      $RW_,  null,
       NY_FOREGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NL_FROZEN,                  $FALSE,                    $INTEGER,   $RW_,  null,
       NL_GETTIPTEXTAT,            T_CALLABLE,                $NULL,      $L__X, null,
       NL_HIDEPOINTS,              $FALSE,                    $INTEGER,   $RW_,  null,
       NL_IGNOREZERO,              $FALSE,                    $INTEGER,   $RW_,  null,
       NY_INSETS,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_IPAD,                    T_OBJECT,                  $NULL,      $RW_,  null,
       NL_KEEPTALL,                $FALSE,                    $INTEGER,   $RW_,  null,
       NL_LINEWIDTH,               "1",                       $DOUBLE,    $RW_,  null,
       NL_LOADEDENDS,              T_DICT,                    $NULL,      $RW_,  null,
       NY_MAXIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NY_MINIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NL_MODEL,                   "0",                       $INTEGER,   $RW_,  null,
       NL_OFFPEAKCOLOR,            T_COLOR,                   $NULL,      $RW_,  null,
       NY_ORIGIN,                  T_POINT,                   $DECLARE,   $RW_,  null,
       NL_PALETTE,                 T_PALETTE,                 $NULL,      $RW_,  null,
       NL_PLOTENDS,                T_DICT,                    $NULL,      $RW_,  null,
       NL_PLOTSTYLE,               $STYLE_BARS,               $OBJECT,    $RW_,  null,
       NL_PLOTSTYLEFLAGS,          $STYLE_ENABLE_MASK,        $INTEGER,   $RW_,  null,
       NL_POINTSIZE,               "2",                       $DOUBLE,    $RW_,  null,
       NY_POPUP,                   T_POPUPMENU,               $NULL,      $RW_,  null,
       NY_PREFERREDSIZE,           T_DIMENSION,               $NULL,      $RW_,  null,
       NL_RANKPREFIX,              T_OBJECT,                  $NULL,      $RWX,  null,
       NL_RANKSUFFIX,              T_OBJECT,                  $NULL,      $RWX,  null,
       NY_REQUESTFOCUS,            $FALSE,                    $INTEGER,   $RW_,  null,
       NL_REVERSEPALETTE,          $FALSE,                    $INTEGER,   $RW_,  null,
       NY_ROOT,                    T_OBJECT,                  $NULL,      $LR__, null,
       NY_SAVEGRAPHICS,            $TRUE,                     $INTEGER,   $RW_,  null,
       NL_SETPLOTENDS,             T_CALLABLE,                $NULL,      $L__X, null,
       NL_SHADETIMES,              T_DICT,                    $NULL,      $RW_,  null,
       NY_SHOWING,                 $FALSE,                    $INTEGER,   $LR__, null,
       NY_SIZE,                    T_DIMENSION,               $NULL,      $RW_,  null,
       NL_SPREAD,                  $FALSE,                    $INTEGER,   $RW_,  null,
       NL_STACKED,                 $FALSE,                    $INTEGER,   $RW_,  null,
       NY_STATE,                   $FALSE,                    $INTEGER,   $RW_,  null,
       NL_SWEEPCOLOR,              T_COLOR,                   $NULL,      $RW_,  null,
       NL_SWEEPFLAGS,              $SWEEP_ENABLED,            $INTEGER,   $RW_,  null,
       NL_SYMMETRIC,               $TRUE,                     $INTEGER,   $RW_,  null,
       NY_TAG,                     T_STRING,                  $NULL,      $RW_,  null,
       NL_TEXT,                    T_STRING,                  $NULL,      $LR__, null,
       NL_TIMESHADING,             "0",                       $INTEGER,   $RW_,  null,
       NL_TIPDROPPED,              $FALSE,                    $INTEGER,   $RW_,  null,
       NL_TIPENABLED,              $FALSE,                    $INTEGER,   $RW_,  null,
       NL_TIPFLAGS,                "0",                       $INTEGER,   $RW_,  null,
       NL_TIPLOCKMODEL,            $YOIX_NONE,                $INTEGER,   $RW_,  null,
       NL_TIPOFFSET,               T_POINT,                   $NULL,      $RW_,  null,
       NL_TIPPREFIX,               T_OBJECT,                  $NULL,      $RWX,  null,
       NL_TIPSUFFIX,               T_OBJECT,                  $NULL,      $RWX,  null,
       NL_UNIXTIME,                T_OBJECT,                  $NULL,      $RW_,  null,
       NY_VIEWPORT,                T_RECTANGLE,               $DECLARE,   $LR__, null,
       NY_VISIBLE,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NL_XAXIS,                   T_AXIS,                    $NULL,      $RW_,  null,
       NL_YAXIS,                   T_AXIS,                    $NULL,      $RW_,  null,
       null,                       null,                      $TYPEDEF,   null,  null,

    //
    // NL_EDGECOUNT and NL_NODECOUNT are new fields that technically should
    // not be writable, but this way initializers can create them and that
    // should help with backward compatibility issues.
    //

       T_GRAPHPLOT,                "106",                     $DICT,      $L___, T_GRAPHPLOT,
       null,                       "-1",                      $GROWTO,    null,  null,
       NY_CLASSNAME,               null,                      $CLASS,     $RORO, null,
       NY_MAJOR,                   $JCOMPONENT,               $INTEGER,   $LR__, null,
       NY_MINOR,                   $JGRAPHPLOT,               $INTEGER,   $LR__, null,
       NL_ACCUMULATE,              $FALSE,                    $INTEGER,   $RW_,  null,
       NL_ACTIVE,                  "0",                       $INTEGER,   $RW_,  null,
       NL_ACTIVEFIELDCOUNT,        "1",                       $INTEGER,   $RW_,  null,
       NL_AFTERLOAD,               T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERPRESSED,            T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERSWEEP,              T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERUPDATE,             T_CALLABLE,                $NULL,      $RWX,  null,
       NL_ALIVE,                   $TRUE,                     $INTEGER,   $RW_,  null,
       NL_ATTACHEDEDGESELECTION,   $FALSE,                    $INTEGER,   $RW_,  null,
       NL_AUTOREADY,               $FALSE,                    $INTEGER,   $RW_,  null,
       NL_AUTOSHOW,                $FALSE,                    $INTEGER,   $RW_,  null,
       NY_BACKGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_BORDER,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_BORDERCOLOR,             T_COLOR,                   $NULL,      $RW_,  null,
       NY_CELLSIZE,                T_DIMENSION,               $DECLARE,   $LR__, null,
       NL_CLEAR,                   T_CALLABLE,                $NULL,      $L__X, null,
       NL_CLICKRADIUS,             "2",                       $DOUBLE,    $RW_,  null,
       NL_COLLECTRECORDSAT,        T_CALLABLE,                $NULL,      $L__X, null,
       NL_COLOREDBY,               T_OBJECT,                  $NULL,      $RW_,  null,
       NY_CURSOR,                  $STANDARDCURSOR,           $INTEGER,   $RW_,  null,
       NL_DATAMANAGER,             T_OBJECT,                  $NULL,      $LR__, null,
       NL_DRAGCOLOR,               T_COLOR,                   $NULL,      $RW_,  null,
       NL_EDGECOUNT,               "-1",                      $INTEGER,   $RW_,  null,
       NL_EDGEFLAGS,               "0",                       $INTEGER,   $RW_,  null,
       NL_EDGES,                   T_ARRAY,                   $NULL,      $LR__, null,
       NL_EDGESCALE,               "1.0",                     $DOUBLE,    $RW_,  null,
       NL_EMPTYCOLOR,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_ENABLED,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NY_ETC,                     T_OBJECT,                  $NULL,      $LR__, null,
       NY_EXTENT,                  T_DIMENSION,               $DECLARE,   $LR__, null,
       NL_FIELDINDEX,              "-1",                      $INTEGER,   $LR__, null,
       NL_FIELDINDICES,            T_ARRAY,                   $NULL,      $LR__, null,
       NL_FILLMODEL,               $YOIX_NONE,                $INTEGER,   $RW_,  null,
       NY_FONT,                    T_OBJECT,                  $NULL,      $RW_,  null,
       NL_FONTSCALE,               "1.0",                     $DOUBLE,    $RW_,  null,
       NY_FOREGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NL_FROZEN,                  $FALSE,                    $INTEGER,   $RW_,  null,
       NL_GETPOSITION,             T_CALLABLE,                $NULL,      $RWX,  null,
       NL_GETTIPTEXTAT,            T_CALLABLE,                $NULL,      $L__X, null,
       NL_GRAPHLAYOUTARG,          T_OBJECT,                  $NULL,      $RW_,  null,
       NL_GRAPHLAYOUTMODEL,        T_OBJECT,                  $NULL,      $RW_,  null,
       NL_GRAPHLAYOUTSORT,         T_OBJECT,                  $NULL,      $RW_,  null,
       NL_GRAPHMATRIX,             T_MATRIX,                  $NULL,      $LR__, null,
       NL_HIGHLIGHTCOLOR,          T_COLOR,                   $NULL,      $RW_,  null,
       NL_HIGHLIGHTED,             T_OBJECT,                  $NULL,      $RW_,  null,
       NY_INSETS,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_IPAD,                    T_OBJECT,                  $NULL,      $RW_,  null,
       NL_KEYS,                    T_ARRAY,                   $NULL,      $LR__, null,
       NL_LABELFLAGS,              "0",                       $INTEGER,   $RW_,  null,
       NL_LINEWIDTH,               "1",                       $DOUBLE,    $RW_,  null,
       NL_LOADRECORDS,             T_CALLABLE,                $NULL,      $L__X, null,
       NY_MAXIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NY_MINIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NL_MOVED,                   T_OBJECT,                  $NULL,      $RW_,  null,
       NL_NODECOUNT,               "-1",                      $INTEGER,   $RW_,  null,
       NL_NODEFLAGS,               "0",                       $INTEGER,   $RW_,  null,
       NL_NODEOUTLINE,             "1.0",                     $DOUBLE,    $RW_,  null,
       NL_NODES,                   T_ARRAY,                   $NULL,      $LR__, null,
       NL_NODESCALE,               "1.0",                     $DOUBLE,    $RW_,  null,
       NL_OTHERCOLOR,              T_COLOR,                   $NULL,      $RW_,  null,
       NL_OPERATIONS,              T_ARRAY,                   $NULL,      $RW_,  null,
       NY_ORIGIN,                  T_POINT,                   $DECLARE,   $RW_,  null,
       NL_OUTLINECACHESIZE,        "1",                       $INTEGER,   $RW_,  null,
       NL_PAINTORDER,              T_ARRAY,                   $NULL,      $RW_,  null,
       NL_PALETTE,                 T_PALETTE,                 $NULL,      $RW_,  null,
       NL_PERSISTENT,              $TRUE,                     $INTEGER,   $RW_,  null,
       NY_POPUP,                   T_POPUPMENU,               $NULL,      $RW_,  null,
       NY_PREFERREDSIZE,           T_DIMENSION,               $NULL,      $RW_,  null,
       NL_PRESSED,                 T_OBJECT,                  $NULL,      $RW_,  null,
       NL_PRESSEDCOLOR,            T_COLOR,                   $NULL,      $RW_,  null,
       NL_PRESSINGCOLOR,           T_COLOR,                   $NULL,      $RW_,  null,
       NL_PRIMARYFIELD,            "0",                       $INTEGER,   $RW_,  null,
       NL_RECOLORED,               $FALSE,                    $INTEGER,   $RW_,  null,
       NY_REQUESTFOCUS,            $FALSE,                    $INTEGER,   $RW_,  null,
       NL_REVERSEPALETTE,          $FALSE,                    $INTEGER,   $RW_,  null,
       NY_ROOT,                    T_OBJECT,                  $NULL,      $LR__, null,
       NY_SAVEGRAPHICS,            $TRUE,                     $INTEGER,   $RW_,  null,
       NL_SELECTED,                T_OBJECT,                  $NULL,      $RW_,  null,
       NL_SELECTFLAGS,             $DATA_GRAPH_MASK,          $INTEGER,   $RW_,  null,
       NL_SELECTWIDTH,             "5",                       $DOUBLE,    $RW_,  null,
       NL_SEPARATOR,               " ",                       $STRING,    $RW_,  null,
       NL_SETALL,                  T_CALLABLE,                $NULL,      $L__X, null,
       NY_SHOWING,                 $FALSE,                    $INTEGER,   $LR__, null,
       NY_SIZE,                    T_DIMENSION,               $NULL,      $RW_,  null,
       NY_STATE,                   $FALSE,                    $INTEGER,   $RW_,  null,
       NL_SWEEPCOLOR,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_SYNCCOUNT,               "0",                       $INTEGER,   $LR__, null,
       NY_SYNCVIEWPORT,            T_CALLABLE,                $NULL,      $RWX,  null,
       NY_TAG,                     T_STRING,                  $NULL,      $RW_,  null,
       NL_TEXT,                    T_STRING,                  $NULL,      $LR__, null,
       NL_TIPDROPPED,              $FALSE,                    $INTEGER,   $RW_,  null,
       NL_TIPENABLED,              $FALSE,                    $INTEGER,   $RW_,  null,
       NL_TIPFLAGS,                "0",                       $INTEGER,   $RW_,  null,
       NL_TIPLOCKMODEL,            $YOIX_NONE,                $INTEGER,   $RW_,  null,
       NL_TIPOFFSET,               T_POINT,                   $NULL,      $RW_,  null,
       NL_TIPPREFIX,               T_OBJECT,                  $NULL,      $RWX,  null,
       NL_TIPSUFFIX,               T_OBJECT,                  $NULL,      $RWX,  null,
       NY_VIEWPORT,                T_RECTANGLE,               $DECLARE,   $LR__, null,
       NY_VISIBLE,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NL_ZOOM,                    T_CALLABLE,                $NULL,      $L__X, null,
       NL_ZOOMDIRECTION,           "1",                       $INTEGER,   $RW_,  null,
       NL_ZOOMINCOLOR,             T_COLOR,                   $NULL,      $RW_,  null,
       NL_ZOOMLIMIT,               "1.0",                     $DOUBLE,    $LR__, null,
       NL_ZOOMOUTCOLOR,            T_COLOR,                   $NULL,      $RW_,  null,
       NL_ZOOMSCALE,               "1.0",                     $DOUBLE,    $RW_,  null,
       null,                       null,                      $TYPEDEF,   null,  null,

       T_DATATABLE,                "75",                      $DICT,      $L___, T_DATATABLE,
       null,                       "-1",                      $GROWTO,    null,  null,
       NY_CLASSNAME,               null,                      $CLASS,     $RORO, null,
       NY_MAJOR,                   $JCOMPONENT,               $INTEGER,   $LR__, null,
       NY_MINOR,                   $JDATATABLE,               $INTEGER,   $LR__, null,
       NL_ACCUMULATE,              $FALSE,                    $INTEGER,   $RW_,  null,
       NY_ACTION,                  T_CALLABLE,                $NULL,      $L__X, null,
       NL_ACTIVE,                  "0",                       $INTEGER,   $RW_,  null,
       NL_ACTIVEFIELDCOUNT,        "1",                       $INTEGER,   $RW_,  null,
       NL_AFTERLOAD,               T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERPRESSED,            T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERSWEEP,              T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERUPDATE,             T_CALLABLE,                $NULL,      $RWX,  null,
       NL_ALIVE,                   $TRUE,                     $INTEGER,   $RW_,  null,
       NY_ALTALIGNMENT,            "-1",                      $INTEGER,   $RW_,  null,
       NY_ALTBACKGROUND,           T_OBJECT,                  $NULL,      $RW_,  null,
       NY_ALTFONT,                 T_OBJECT,                  $NULL,      $RW_,  null,
       NY_ALTFOREGROUND,           T_OBJECT,                  $NULL,      $RW_,  null,
       NY_ALTGRIDCOLOR,            T_COLOR,                   $NULL,      $RW_,  null,
       NY_ALTTOOLTIPTEXT,          T_OBJECT,                  $NULL,      $RW_,  null,
       NL_ANCHOR,                  $YOIX_LEFT,                $INTEGER,   $RW_,  null,
       NL_AUTOREADY,               $FALSE,                    $INTEGER,   $RW_,  null,
       NL_AUTOSCROLL,              $TRUE,                     $INTEGER,   $RW_,  null,
       NY_BACKGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_BORDER,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_BORDERCOLOR,             T_COLOR,                   $NULL,      $RW_,  null,
       NY_CELLCOLORS,              T_ARRAY,                   $NULL,      $RW_,  null,
       NL_CLEAR,                   T_CALLABLE,                $NULL,      $L__X, null,
       NY_CLICKCOUNT,              "1",                       $INTEGER,   $RW_,  null,
       NY_COLUMNS,                 T_ARRAY,                   $NULL,      $RW_,  null,
       NY_CURSOR,                  $STANDARDCURSOR,           $INTEGER,   $RW_,  null,
       NL_DATAMANAGER,             T_OBJECT,                  $NULL,      $LR__, null,
       NY_DOUBLEBUFFERED,          T_OBJECT,                  $NULL,      $RW_,  null,
       NY_DRAGENABLED,             $FALSE,                    $INTEGER,   $RW_,  null,
       NY_EDIT,                    T_OBJECT,                  $NULL,      $RW_,  null,
       NY_EDITBACKGROUND,          T_COLOR,                   $NULL,      $RW_,  null,
       NY_EDITFOREGROUND,          T_COLOR,                   $NULL,      $RW_,  null,
       NL_EMPTYCOLOR,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_ENABLED,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NY_EXTENT,                  T_DIMENSION,               $DECLARE,   $LR__, null,
       NY_ETC,                     T_OBJECT,                  $NULL,      $RW_,  null,
       NL_FIELDINDEX,              "-1",                      $INTEGER,   $LR__, null,
       NL_FIELDINDICES,            T_ARRAY,                   $NULL,      $LR__, null,
       NY_FINDNEXTMATCH,           T_CALLABLE,                $NULL,      $L__X, null,
       NY_FOCUSOWNER,              $FALSE,                    $INTEGER,   $LR__, null,
       NY_FONT,                    T_OBJECT,                  $NULL,      $RW_,  null,
       NY_FOREGROUND,              T_COLOR,                   $NULL,      $RW_,  null,
       NY_GRIDCOLOR,               T_COLOR,                   $NULL,      $RW_,  null,
       NY_GRIDSIZE,                T_DIMENSION,               $NULL,      $RW_,  null,
       NY_HEADERS,                 T_OBJECT,                  $NULL,      $RW_,  null,
       NY_HEADERICONS,             T_OBJECT,                  $NULL,      $RW_,  null,
       NL_HIGHLIGHTCOLOR,          T_COLOR,                   $NULL,      $RW_,  null,
       NL_HIGHLIGHTED,             T_OBJECT,                  $NULL,      $RW_,  null,
       NY_INPUTFILTER,             T_OBJECT,                  $NULL,      $RW_,  null,
       NL_KEYS,                    T_ARRAY,                   $NULL,      $LR__, null,
       NY_LAYER,                   $DEFAULT_LAYER,            $INTEGER,   $RW_,  null,
       NL_LOADRECORDS,             T_CALLABLE,                $NULL,      $L__X, null,
       NY_LOCATION,                T_POINT,                   $NULL,      $RW_,  null,
       NY_MAXIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NY_MINIMUMSIZE,             T_DIMENSION,               $NULL,      $RW_,  null,
       NY_MODELTOVIEW,             T_CALLABLE,                $NULL,      $L__X, null,
       NY_MULTIPLEMODE,            $SINGLESELECTION,          $INTEGER,   $RW_,  null,
       NY_NEXTFOCUS,               T_OBJECT,                  $NULL,      $RW_,  null,
       NY_OPAQUE,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NL_OPERATIONS,              T_ARRAY,                   $NULL,      $RW_,  null,
       NY_ORIGIN,                  T_POINT,                   $DECLARE,   $RW_,  null,
       NY_OUTPUTFILTER,            T_OBJECT,                  $NULL,      $RW_,  null,
       NL_PALETTE,                 T_PALETTE,                 $NULL,      $RW_,  null,
       NL_PERSISTENT,              $TRUE,                     $INTEGER,   $RW_,  null,
       NY_POPUP,                   T_POPUPMENU,               $NULL,      $RW_,  null,
       NY_PREFERREDSIZE,           $ZEROSIZE,                 $GET,       $RW_,  null,
       NL_PRESSED,                 T_OBJECT,                  $NULL,      $RW_,  null,
       NL_PRESSEDCOLOR,            T_COLOR,                   $NULL,      $RW_,  null,
       NL_PRESSINGCOLOR,           T_COLOR,                   $NULL,      $RW_,  null,
       NL_RECOLORED,               $FALSE,                    $INTEGER,   $RW_,  null,
       NY_REORDER,                 $FALSE,                    $INTEGER,   $RW_,  null,
       NY_REQUESTFOCUS,            $FALSE,                    $INTEGER,   $RW_,  null,
       NY_REQUESTFOCUSENABLED,     $TRUE,                     $INTEGER,   $RW_,  null,
       NY_RESIZE,                  $FALSE,                    $INTEGER,   $RW_,  null,
       NY_RESIZEMODE,              $RESIZEMODE,               $INTEGER,   $RW_,  null,
       NY_ROOT,                    T_OBJECT,                  $NULL,      $LR__, null,
       NY_ROWHEIGHTADJUSTMENT,     "0.0",                     $DOUBLE,    $RW_,  null,
       NY_ROWS,                    "-1",                      $INTEGER,   $RW_,  null,
       NY_SCROLL,                  $YOIX_NEVER,               $INTEGER,   $RW_,  null,
       NY_SELECTIONBACKGROUND,     T_COLOR,                   $NULL,      $RW_,  null,
       NY_SELECTIONFOREGROUND,     T_COLOR,                   $NULL,      $RW_,  null,
       NL_SELECTED,                T_OBJECT,                  $NULL,      $RW_,  null,
       NL_SETALL,                  T_CALLABLE,                $NULL,      $L__X, null,
       NY_SHOWING,                 $FALSE,                    $INTEGER,   $LR__, null,
       NY_SIZE,                    T_DIMENSION,               $NULL,      $RW_,  null,
       NY_TAG,                     T_STRING,                  $NULL,      $RW_,  null,
       NY_TEXT,                    T_STRING,                  $NULL,      $RW_,  null,
       NY_TOOLTIP,                 T_OBJECT,                  $NULL,      $RW_,  null,
       NY_TOOLTIPS,                $FALSE,                    $INTEGER,   $RW_,  null,
       NY_TOOLTIPTEXT,             T_OBJECT,                  $NULL,      $RW_,  null,
       NY_TRANSFERHANDLER,         $NULL,                     $OBJECT,    $RW_,  null,
       NL_TRANSIENTMODE,           $FALSE,                    $INTEGER,   $RW_,  null,
       NY_TYPES,                   T_ARRAY,                   $NULL,      $RW_,  null,
       NY_UIMKEY,                  T_STRING,                  $NULL,      $RW_,  null,
       NY_USEEDITHIGHLIGHT,        $TRUE,                     $INTEGER,   $RW_,  null,
       NY_VALIDATOR,               T_CALLABLE,                $NULL,      $RWX,  null,
       NY_VALUES,                  T_OBJECT,                  $NULL,      $RW_,  null,
       NY_VIEWPORT,                T_RECTANGLE,               $DECLARE,   $LR__, null,
       NY_VIEWROWCOUNT,            "0",                       $INTEGER,   $LR__, null,
       NY_VIEWTOMODEL,             T_CALLABLE,                $NULL,      $L__X, null,
       NY_VISIBLE,                 $TRUE,                     $INTEGER,   $RW_,  null,
       NY_VISIBLEWIDTH,            "0",                       $INTEGER,   $LR__, null,
       NY_WIDTH,                   "0",                       $INTEGER,   $RW_,  null,
       null,                       null,                      $TYPEDEF,   null,  null,

    //
    // The NL_DATAVIEWERS field has been intentionally omitted in order
    // to simplify the code needed to support the old and new mechanisms
    // that are used to add graphs to the collection of managed objects.
    // The kludge is in DataManager.java and it lets graphs in via the
    // datafilters array, but only if the dataviewers field is undefined.
    // We eventually hope to remove the kludge and add a NL_DATAVIEWERS
    // to this template, but existing applications that use graphs need
    // to be updated first.
    //
    // The NL_PLOTFILTERS is a kludge that's only here because we need to
    // support and old, but important, internal application. It should be
    // removed from this file and from DataManager.java as soon as that
    // application is updated!!
    // 

       T_DATAMANAGER,              "35",                      $DICT,      $L___, T_DATAMANAGER,
       null,                       "-1",                      $GROWTO,    null,  null,
       NY_CLASSNAME,               null,                      $CLASS,     $RORO, null,
       NY_MAJOR,                   $DATAMANAGER,              $INTEGER,   $LR__, null,
       NY_MINOR,                   "0",                       $INTEGER,   $LR__, null,
       NL_AFTERCOLOREDBY,          T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERLOAD,               T_CALLABLE,                $NULL,      $RWX,  null,
       NL_AFTERUPDATE,             T_CALLABLE,                $NULL,      $RWX,  null,
       NL_APPENDTEXT,              T_CALLABLE,                $NULL,      $L__X, null,
       NL_COLOREDBY,               T_OBJECT,                  $NULL,      $RW_,  null,
       NL_COUNTERS,                T_DICT,                    $NULL,      $LR__, null,
       NL_DATAFIELDS,              T_ARRAY,                   $NULL,      $RW_,  null,
       NL_DATAFILTERS,             T_ARRAY,                   $NULL,      $RW_,  null,
       NL_DATAPLOTS,               T_ARRAY,                   $NULL,      $RW_,  null,
       NL_DATATABLES,              T_ARRAY,                   $NULL,      $RW_,  null,
       NL_DISPOSE,                 $FALSE,                    $INTEGER,   $RW_,  null,
       NY_ETC,                     T_OBJECT,                  $NULL,      $RW_,  null,
       NL_GETFIELDS,               T_CALLABLE,                $NULL,      $L__X, null,
       NL_GETINDEX,                T_CALLABLE,                $NULL,      $L__X, null,
       NL_GETINDICES,              T_CALLABLE,                $NULL,      $L__X, null,
       NL_GETSTATE,                T_CALLABLE,                $NULL,      $L__X, null,
       NL_GETVALUES,               T_CALLABLE,                $NULL,      $L__X, null,
       NL_GRAPHDATA,               T_ELEMENT,                 $NULL,      $RW_,  null,
       NL_INPUTCOMMENT,            T_STRING,                  $NULL,      $RW_,  null,
       NL_INPUTFILTER,             T_OBJECT,                  $NULL,      $RW_,  null,
       NL_INTERN,                  $FALSE,                    $INTEGER,   $RW_,  null,
       NL_LOADEDCOUNT,             "0",                       $INTEGER,   $LR__, null,
       NL_MONITOR,                 T_THREAD,                  $NULL,      $RW_,  null,
       NL_PLOTFILTERS,             T_ARRAY,                   $NULL,      $RW_,  null,
       NL_SELECTED,                T_STRING,                  $NULL,      $LR__, null,
       NL_SELECTEDCOUNT,           "0",                       $INTEGER,   $LR__, null,
       NL_SORTEDBY,                T_OBJECT,                  $NULL,      $RW_,  null,
       NL_SWEEPFILTERS,            T_ARRAY,                   $NULL,      $RW_,  null,
       NL_TEXT,                    T_STRING,                  $NULL,      $RW_,  null,
       NL_TOTALS,                  T_DICT,                    $NULL,      $LR__, null,
       NL_TRIMFIELDS,              $FALSE,                    $INTEGER,   $RW_,  null,
       NL_UNSELECTED,              T_STRING,                  $NULL,      $LR__, null,
       null,                       null,                      $TYPEDEF,   null,  null,

    //
    // Swing implementations of the data visualization types.
    //
    // NOTE - original version used $DUP to set NY_PREFERREDSIZE, but doing
    // $DUP twice seemed to cause an obscure bug in YoixModule.readTable()
    // that we haven't tracked down yet. Changing to $GET seems to resolve
    // the problem, but the $DUP behavior should eventually be investigated
    // (something very strange is going on in readTable() that seems to be
    // related to the target variable).
    //

       T_JAXIS,                    T_AXIS,                    $GET,       $L___, T_JAXIS,
       null,                       T_AXIS,                    $TYPEDEF,   null,  null,

       T_JDATATABLE,               T_DATATABLE,               $GET,       $L___, T_JDATATABLE,
       null,                       T_DATATABLE,               $TYPEDEF,   null,  null,

       T_JHISTOGRAM,               T_HISTOGRAM,               $GET,       $L___, T_JHISTOGRAM,
       null,                       T_HISTOGRAM,               $TYPEDEF,   null,  null,

       T_JEVENTPLOT,               T_EVENTPLOT,               $GET,       $L___, T_JEVENTPLOT,
       null,                       T_EVENTPLOT,               $TYPEDEF,   null,  null,

       T_JGRAPHPLOT,               T_GRAPHPLOT,               $GET,       $L___, T_JGRAPHPLOT,
       null,                       T_GRAPHPLOT,               $TYPEDEF,   null,  null,
    };

    ///////////////////////////////////
    //
    // Module Methods
    //
    ///////////////////////////////////

    public static YoixObject
    buildDataFields(YoixObject arg[]) {

	YoixObject  source;
	YoixObject  translators;
	YoixObject  context;
	YoixObject  desc;
	ArrayList   table = null;
	String      tag;
	int         type;
	int         columns;
	int         length;
	int         n;

	//
	// Decided to return null if columns doesn't match what we expect
	// (currently 7), so the caller can decide to parse the table or
	// issue a warning. A column mismatch undoubtedly means we need
	// to update this builtin because the format has changed. Other
	// errors are currently not dealt with gracefully, but probably
	// should also trigger a null return - later.
	// 

	if (arg.length == 3 || arg.length == 4) {
	    if (arg[0].isArray() || arg[0].isNull()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isDictionary() || arg[2].isNull()) {
			if (arg.length == 3 || arg[3].isDictionary() || arg[3].isNull()) {
			    if (arg[0].sizeof() > 0) {
				if ((columns = arg[1].intValue()) == 7) {	// should we abort??
				    source = arg[0];
				    translators = arg[2].notNull() ? arg[2] : null;
				    context = (arg.length == 4 && arg[3].notNull()) ? arg[3] : null;
				    table = new ArrayList(source.sizeof());
				    length = source.length() - columns + 1;
				    for (n = source.offset(); n < length; n += columns) {
					tag = source.getString(n);
					type = source.getInt(n+1, DATA_STRING);
					desc = YoixObject.newDictionary(7);
					desc.putString(NL_TAG, tag);
					desc.putInt(NL_TYPE, type);
					desc.put(NL_INDEX, source.get(n+2, true), false);
					desc.put(NL_ACCUMULATE, source.get(n+3, true), false);
					desc.put(NL_UNIXTIME, source.get(n+4, true), false);
					desc.put(
					    NL_TRANSLATOR,
					    newTranslator(source.getString(n+5), translators),
					    false
					);
					desc.put(
					    NL_GENERATOR,
					    newTableGenerator(source.getObject(n+6), type, translators),
					    false
					);
					if (context != null)
					    context.put(tag, desc, false);
					table.add(desc);
				    }
				}
			    }
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(YoixMisc.copyIntoGrowableArray(table, false, table));
    }


    public static YoixObject
    dotPlot(YoixObject arg[]) {

	YoixObject  obj;
	String      text = null;
	String      attrs[] = null;
	char        delim = '|';
	int         offset;
	int         n;

	if (arg[0].isBodyInstanceOf(DataManager.class)) {
	    if (arg[1].isString() || arg[1].isGraph()) {
		if (arg.length >= 3) {
		    if (arg[2].notNull() && arg[2].isString() && arg[2].sizeof() == 1) {
			delim = arg[2].stringValue().charAt(0);
		    } else if (arg[2].notNull())
			VM.badArgument(2);
		    if (arg.length == 4 && arg[3].notNull()) {
			if (arg[3].isArray()) {
			    attrs = new String[arg[3].sizeof()];
			    offset = arg[3].offset();
			    for (n = 0; n < attrs.length; n++) {
				if ((obj = arg[3].getObject(n + offset)) != null) {
				    if (obj.isString() && obj.notNull())
					attrs[n] = obj.stringValue();
				    else VM.badArgument(3);
				} else VM.badArgument(3);
			    }
			} else VM.badArgument(3);
		    }
		}
		text = YoixMiscGraph.graphdata(arg[1], delim, attrs);
		if (text != null) {
		    arg[0].setBodyField(NL_TEXT, YoixObject.newString(text), false);
		} else VM.badArgument(1);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newEmpty());
    }


    public static YoixObject
    graphPlotText(YoixObject arg[]) {

	String  text = null;
	char    delim = '|';

	//
	// Pretty much duplicates the YoixModuleGraph.dotGraphToText()
	// builtin, which is the one we now recommend that you use. In
	// fact there's a chance this one will disappear, so don't use
	// it in new applications. The reasons for the change was so
	// we could write a server based Yoix script that could parse
	// graphs but that didn't need a custom module that might also
	// be licensed.
	//

	if (arg.length <= 4) {
	    if (arg[0].isGraph() || arg[0].isString()) {
		if (arg[0].notNull()) {
		    if (arg.length <= 1 || arg[1].isString() || arg[1].isNull()) {
			if (arg.length <= 2 || arg[2].isArray() || arg[2].isDictionary() || arg[2].isNull()) {
			    if (arg.length <= 3 || arg[3].isInteger()) {
				if (arg.length > 1 && arg[1].sizeof() > 0)
				    delim = arg[1].stringValue().charAt(0);
				text = YoixMiscGraph.graphdata(
				    arg[0],
				    delim,
				    arg.length > 2 ? arg[2] : null,
				    arg.length > 3 ? arg[3].booleanValue() : false
				);
			    } else VM.badArgument(3);
			} else VM.badArgument(2);
		    } else VM.badArgument(1);
		}
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(text == null ? YoixObject.newString() : YoixObject.newString(text));
    }


    public static YoixObject
    mercatorToYDAT(YoixObject arg[]) {

	String  text = null;

	if (arg.length >= 4 || arg.length <= 10) {
	    if (arg[0].isString()) {
		if (arg[1].isNumber()) {
		    if (arg[2].isNumber()) {
			if (arg[3].isNumber()) {
			    if (arg[4].isNumber()) {
				if (arg.length <= 5 || arg[5].isString() || arg[5].isNull()) {
				    if (arg.length <= 6 || arg[6].isNumber()) {
					if (arg.length <= 7 || arg[7].isNumber()) {
					    if (arg.length <= 8 || arg[8].isNumber()) {
						if (arg.length <= 9 || arg[9].isMatrix() || arg[9].isNull()) {
						    text = Misc.mercatorToYDAT(
							arg[0].stringValue(),
							arg[1].intValue(),
							arg[2].intValue(),
							arg[3].intValue(),
							arg.length > 4 ? arg[4].intValue() : 2,
							arg.length > 5 && arg[5].notNull() ? arg[5].stringValue() : null,
							arg.length > 6 ? arg[6].doubleValue() : 0,
							arg.length > 7 ? arg[7].doubleValue() : 0,
							arg.length > 8 ? arg[8].booleanValue() : true,
							arg.length > 9 && arg[9].isMatrix() ? arg[9] : null
						    );
						} else VM.badArgument(9);
					    } else VM.badArgument(8);
					} else VM.badArgument(7);
				    } else VM.badArgument(6);
				} else VM.badArgument(5);
			    } else VM.badArgument(4);
			} else VM.badArgument(3);
		    } else VM.badArgument(2);
		} else VM.badArgument(1);
	    } else VM.badArgument(0);
	} else VM.badCall();

	return(text == null ? YoixObject.newString() : YoixObject.newString(text));
    }


    public static YoixObject
    newPointer(int id, int length, YoixObject data) {

	YoixObject  obj = null;

	switch (id) {
	    case DATAMANAGER:
		obj = YoixObject.newPointer(new DataManager(data));
		break;

	    case JCOMPONENT:
		obj = YoixObject.newPointer(new BodyComponentSwing(data));
		break;

	    case PALETTE:
		obj = YoixObject.newPointer(new Palette(data));
		break;
	}
	return(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static YoixObject
    newTableGenerator(YoixObject generator, int type, YoixObject translators) {

	YoixObject  translator;
	ArrayList   table = null;
	int         columns = 5;
	int         length;
	int         n;

	if (generator != null && generator.notNull()) {
	    switch (type) {
		case DATA_TABLE:
		    table = new ArrayList(generator.length());
		    length = generator.length() - columns + 1;
		    for (n = 0; n < length; n += columns) {
			table.add(generator.getObject(n+0));
			table.add(generator.getObject(n+1));
			table.add(generator.getObject(n+2));
			table.add(generator.getObject(n+3));
			if ((translator = generator.getObject(n+4)) != null) {
			    if (translator.isString())
				translator = newTranslator(translator.stringValue(), translators);
			} else translator = YoixObject.newDictionary();
			table.add(translator);
		    }
		    generator = YoixMisc.copyIntoArray(table, false, table);
		    break;

		case DATA_TABLE_NEW:
		    table = new ArrayList(generator.length());
		    length = generator.length() - columns + 1;
		    for (n = 0; n < length; n += columns) {
			table.add(generator.getObject(n+0));
			table.add(generator.getObject(n+1));
			table.add(generator.getObject(n+2));
			table.add(generator.getObject(n+3));
			table.add(generator.getObject(n+4));
		    }
		    generator = YoixMisc.copyIntoArray(table, false, table);
		    break;
	    }
	}

	return(generator);
    }


    private static YoixObject
    newTranslator(String name, YoixObject translators) {

	YoixObject  translator = null;

	if (translators != null && name != null)
	    translator = translators.getObject(name);
	return(translator == null ? YoixObject.newDictionary() : translator);
    }
}

