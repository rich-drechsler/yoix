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
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import att.research.yoix.*;

public
interface Constants

    extends YoixConstants

{

    //
    // Default colors and a special label.
    //

    static final Color  CONNECTCOLOR = Color.orange;
    static final Color  DRAGCOLOR = Color.blue;
    static final Color  EMPTYCOLOR = Color.gray;
    static final Color  HIGHLIGHTCOLOR = null;
    static final Color  OFFPEAKCOLOR = new Color(135, 135, 135);
    static final Color  OTHERCOLOR = Color.lightGray;
    static final Color  PRESSEDCOLOR = null;
    static final Color  PRESSINGCOLOR = null;
    static final Color  SWEEPCOLOR = Color.green;
    static final Color  ZOOMINCOLOR = Color.green;
    static final Color  ZOOMOUTCOLOR = Color.red;

    static final String  OTHERNAME = "OTHER...";
    static final String  UNKNOWNNAME = "UNKNOWN";

    //
    // Timer delays for our autoscroller that are adjusted in a way
    // that depends on how how far the mouse has been moved from the
    // viewport.
    //

    static final int  DEFAULT_INITIAL_DELAY = 350;
    static final int  DEFAULT_REPEAT_DELAY = 200;
    static final int  MINIMUM_REPEAT_DELAY = 20;

    //
    // A FontRenderContext that can be used when we need to measure text.
    //

    static final FontRenderContext  FONTCONTEXT = new FontRenderContext(
	new AffineTransform(),
	false,
	true
    );

    //
    // Default constants that control the contents of automatically
    // generated labels. These were recently (5/25/07) made available
    // as constants to yoix scripts, but old scripts (primarily config
    // files) may use the numbers so don't change the values unless you
    // don't care about backward compatibility.
    // 

    static final int  LABEL_SHOWCOUNT     = 0x001;
    static final int  LABEL_SHOWVALUE     = 0x002;
    static final int  LABEL_SHOWDIVERSITY = 0x004;
    static final int  LABEL_HIDE          = 0x008;
    static final int  LABEL_HIDENODE      = 0x010;
    static final int  LABEL_HIDEEDGE      = 0x020;
    static final int  LABEL_PICKCOLORNODE = 0x040;
    static final int  LABEL_SHOWCOUNTPCNT = 0x080;
    static final int  LABEL_SHOWVALUEPCNT = 0x100;

    //
    // Constants that control how the individual events are drawn in an
    // event plot.
    //

    static final int  STYLE_BARS = 1;
    static final int  STYLE_POINTS = 2;
    static final int  STYLE_POLYGONS = 3;
    static final int  STYLE_STACKED_BARS = 4;
    static final int  STYLE_STACKED_POINTS = 5;
    static final int  STYLE_STACKED_POLYGONS = 6;

    //
    // Flags that exercise low level control over plot style and bucketing.
    //

    static final int  STYLE_ENABLE_EVENTS = 0x01;
    static final int  STYLE_ENABLE_STACKS = 0x02;
    static final int  STYLE_ENABLE_MASK = 0x03;

    //
    // Constants that determine what happens to neighboring events in
    // an even plot.
    //

    static final int  CONNECT_NONE = 0;
    static final int  CONNECT_LINES = 1;

    //
    // A few flags that control sweeping - there probably will be more.
    //

    static final int  SWEEP_ENABLED = 0x01;
    static final int  SWEEP_SOLID_STACKS = 0x02;

    //
    // Some flags that control tips - don't change them without checking
    // other files. For example, TIP_OVER_NODES and TIP_OVER_EDGES must
    // match DATA_GRAPH_NODE and DATA_GRAPH_EDGE.
    //

    static final int  TIP_OVER_NODES = 0x01;	// must currently match DATA_GRAPH_NODE!!
    static final int  TIP_OVER_EDGES = 0x02;	// must currently match DATA_GRAPH_EDGE!!
    static final int  TIP_OVER_STACKS = 0x01;	// not matched to a value
    static final int  TIP_OVER_EVENTS = 0x02;	// not matched to a value
    static final int  TIP_OVER_MASK = 0x03;

    static final int  TIP_SHOW_COUNT = 0x04;
    static final int  TIP_SHOW_VALUE = 0x08;
    static final int  TIP_SHOW_RANK = 0x10;
    static final int  TIP_SHOW_TIES = 0x20;
    static final int  TIP_SHOW_COUNTPCNT = 0x40;
    static final int  TIP_SHOW_VALUEPCNT = 0x80;

    //
    // Mouse event related stuff - some states may not be implemented.
    // Acutal values shouldn't matter, but to be safe we don't change
    // UNAVAILABLE and AVAILABLE when new values are added.
    //

    static final int  UNAVAILABLE = -1;
    static final int  AVAILABLE = 0;

    static final int  ADJUSTING = 1;
    static final int  CENTERING = 2;
    static final int  CLICKING = 3;
    static final int  DESELECTING = 4;
    static final int  DRAGGING = 5;
    static final int  EDITING = 6;
    static final int  GRABBING = 7;
    static final int  PRESSING = 8;
    static final int  SCROLLING = 9;
    static final int  SELECTING = 10;
    static final int  SWEEPING = 11;
    static final int  TIPPING = 12;
    static final int  TOGGLING = 13;
    static final int  ZOOMING_IN = 14;
    static final int  ZOOMING_OUT = 15;

    //
    // Constants that select special internal data generators that can
    // generate new data from the original source data. Old code only
    // supported an overlap generator, but we recently (10/28/04) added
    // a counter generator, which behaves something like overlap, but
    // there are some subtle differences that were important for the
    // project that needed it. The implementation of both "generators"
    // probably shouldn't change much, although there's clearly plenty
    // of room for improvement!!
    //

    static final int  OVERLAP_GENERATOR = 1;
    static final int  COUNTER_GENERATOR = 2;

    //
    // Local type names - no reason to make a copy, because the Yoix
    // interpreter won't allow typedict collisions.
    // 

    static final String  T_AXIS = "Axis";
    static final String  T_DATAMANAGER = "DataManager";
    static final String  T_DATATABLE = "DataTable";
    static final String  T_EVENTPLOT = "EventPlot";
    static final String  T_GRAPHPLOT = "GraphPlot";
    static final String  T_HISTOGRAM = "Histogram";
    static final String  T_JAXIS = "JAxis";
    static final String  T_JDATATABLE = "JDataTable";
    static final String  T_JEVENTPLOT = "JEventPlot";
    static final String  T_JGRAPHPLOT = "JGraphPlot";
    static final String  T_JHISTOGRAM = "JHistogram";
    static final String  T_PALETTE = "Palette";

    //
    // The numbers assigned to these constants are not important except
    // that they must be unique and shouldn't equal values assigned to
    // the automatically generated constants, like JCOMPONENT, that are
    // used in YoixModule.newPointer.
    //

    static final int  DATAMANAGER = LASTTOKEN + 1;
    static final int  JAXIS = LASTTOKEN + 2;
    static final int  JDATATABLE = LASTTOKEN + 3;
    static final int  JEVENTPLOT = LASTTOKEN + 4;
    static final int  JGRAPHPLOT = LASTTOKEN + 5;
    static final int  JHISTOGRAM = LASTTOKEN + 6;
    static final int  PALETTE = LASTTOKEN + 7;

    //
    // Use in GraphLayout
    //

    static final int  SWEEPGRAPH_MARKERS = 1;
    static final int  SWEEPGRAPH_WIDTHRATIO = 2;

    //
    // A copy of some official Yoix field names.
    //

    static final String  NY_ACTION = N_ACTION;
    static final String  NY_ALIGNMENT = N_ALIGNMENT;
    static final String  NY_ALTALIGNMENT = N_ALTALIGNMENT;
    static final String  NY_ALTBACKGROUND = N_ALTBACKGROUND;
    static final String  NY_ALTFOREGROUND = N_ALTFOREGROUND;
    static final String  NY_ALTFONT = N_ALTFONT;
    static final String  NY_ALTGRIDCOLOR = N_ALTGRIDCOLOR;
    static final String  NY_ALTTOOLTIPTEXT = N_ALTTOOLTIPTEXT;
    static final String  NY_ANCHOR = N_ANCHOR;
    static final String  NY_BACKGROUND = N_BACKGROUND;
    static final String  NY_BORDER = N_BORDER;
    static final String  NY_BORDERCOLOR = N_BORDERCOLOR;
    static final String  NY_BOTTOM = N_BOTTOM;
    static final String  NY_CELLCOLORS = N_CELLCOLORS;
    static final String  NY_CELLSIZE = N_CELLSIZE;
    static final String  NY_CLASSNAME = N_CLASSNAME;
    static final String  NY_CLICKCOUNT = N_CLICKCOUNT;
    static final String  NY_COLUMNS = N_COLUMNS;
    static final String  NY_CURSOR = N_CURSOR;
    static final String  NY_DOUBLEBUFFERED = N_DOUBLEBUFFERED;
    static final String  NY_DRAGENABLED = N_DRAGENABLED;
    static final String  NY_EDIT = N_EDIT;
    static final String  NY_EDITBACKGROUND = N_EDITBACKGROUND;
    static final String  NY_EDITFOREGROUND = N_EDITFOREGROUND;
    static final String  NY_ENABLED = N_ENABLED;
    static final String  NY_ETC = N_ETC;
    static final String  NY_EXTENT = N_EXTENT;
    static final String  NY_FINDNEXTMATCH = N_FINDNEXTMATCH;
    static final String  NY_FONT = N_FONT;
    static final String  NY_FOCUSOWNER = N_FOCUSOWNER;
    static final String  NY_FOREGROUND = N_FOREGROUND;
    static final String  NY_GRIDCOLOR = N_GRIDCOLOR;
    static final String  NY_GRIDSIZE = N_GRIDSIZE;
    static final String  NY_HEADERS = N_HEADERS;
    static final String  NY_HEADERICONS = N_HEADERICONS;
    static final String  NY_INPUTFILTER = N_INPUTFILTER;
    static final String  NY_INSETS = N_INSETS;
    static final String  NY_INTERRUPTED = N_INTERRUPTED;
    static final String  NY_IPAD = N_IPAD;
    static final String  NY_LEFT = N_LEFT;
    static final String  NY_LAYER = N_LAYER;
    static final String  NY_LOCATION = N_LOCATION;
    static final String  NY_MAJOR = N_MAJOR;
    static final String  NY_MAXIMUMSIZE = N_MAXIMUMSIZE;
    static final String  NY_MINIMUMSIZE = N_MINIMUMSIZE;
    static final String  NY_MINOR = N_MINOR;
    static final String  NY_MODELTOVIEW = N_MODELTOVIEW;
    static final String  NY_MULTIPLEMODE = N_MULTIPLEMODE;
    static final String  NY_NEXTFOCUS = N_NEXTFOCUS;
    static final String  NY_OPAQUE = N_OPAQUE;
    static final String  NY_ORIGIN = N_ORIGIN;
    static final String  NY_OUTPUTFILTER = N_OUTPUTFILTER;
    static final String  NY_POPUP = N_POPUP;
    static final String  NY_PREFERREDSIZE = N_PREFERREDSIZE;
    static final String  NY_REORDER = N_REORDER;
    static final String  NY_REQUESTFOCUS = N_REQUESTFOCUS;
    static final String  NY_REQUESTFOCUSENABLED = N_REQUESTFOCUSENABLED;
    static final String  NY_RESIZE = N_RESIZE;
    static final String  NY_RESIZEMODE = N_RESIZEMODE;
    static final String  NY_RIGHT = N_RIGHT;
    static final String  NY_ROOT = N_ROOT;
    static final String  NY_ROWHEIGHTADJUSTMENT = N_ROWHEIGHTADJUSTMENT;
    static final String  NY_ROWS = N_ROWS;
    static final String  NY_RUN = N_RUN;
    static final String  NY_SAVEGRAPHICS = N_SAVEGRAPHICS;
    static final String  NY_SCROLL = N_SCROLL;
    static final String  NY_SELECTIONBACKGROUND = N_SELECTIONBACKGROUND;
    static final String  NY_SELECTIONFOREGROUND = N_SELECTIONFOREGROUND;
    static final String  NY_SHOWING = N_SHOWING;
    static final String  NY_SIZE = N_SIZE;
    static final String  NY_STATE = N_STATE;
    static final String  NY_SYNCCOUNT = N_SYNCCOUNT;
    static final String  NY_SYNCVIEWPORT = N_SYNCVIEWPORT;
    static final String  NY_TAG = N_TAG;
    static final String  NY_TEXT = N_TEXT;
    static final String  NY_TOOLTIP = N_TOOLTIP;
    static final String  NY_TOOLTIPS = N_TOOLTIPS;
    static final String  NY_TOOLTIPTEXT = N_TOOLTIPTEXT;
    static final String  NY_TOP = N_TOP;
    static final String  NY_TRANSFERHANDLER = N_TRANSFERHANDLER;
    static final String  NY_TYPES = N_TYPES;
    static final String  NY_UIMKEY = N_UIMKEY;
    static final String  NY_USEEDITHIGHLIGHT = N_USEEDITHIGHLIGHT;
    static final String  NY_VALIDATOR = N_VALIDATOR;
    static final String  NY_VALUES = N_VALUES;
    static final String  NY_VIEWPORT = N_VIEWPORT;
    static final String  NY_VIEWROWCOUNT = N_VIEWROWCOUNT;
    static final String  NY_VIEWTOMODEL = N_VIEWTOMODEL;
    static final String  NY_VISIBLE = N_VISIBLE;
    static final String  NY_VISIBLEWIDTH = N_VISIBLEWIDTH;
    static final String  NY_WIDTH = N_WIDTH;
    static final String  NY_X = N_X;
    static final String  NY_Y = N_Y;

    //
    // Local field names and associated values.
    //

    static final String  NL_ACCUMULATE = "accumulate";
    static final String  NL_ACTIVE = "active";
    static final String  NL_ACTIVEFIELDCOUNT = "activefieldcount";
    static final String  NL_AFTERAPPEND = "afterAppend";
    static final String  NL_AFTERCOLOREDBY = "afterColoredBy";
    static final String  NL_AFTERLOAD = "afterLoad";
    static final String  NL_AFTERPRESSED = "afterPressed";
    static final String  NL_AFTERSELECT = "afterSelect";
    static final String  NL_AFTERSWEEP = "afterSweep";
    static final String  NL_AFTERUPDATE = "afterUpdate";
    static final String  NL_ALIVE = "alive";
    static final String  NL_ANCHOR = NY_ANCHOR;
    static final String  NL_APPENDTEXT = "appendText";
    static final String  NL_ATTACHEDEDGESELECTION = "attachededgeselection";
    static final String  NL_AUTOREADY = "autoready";
    static final String  NL_AUTOSCROLL = "autoscroll";
    static final String  NL_AUTOSHOW = "autoshow";
    static final String  NL_AXISENDS = "axisends";
    static final String  NL_AXISLIMITS = "axislimits";
    static final String  NL_AXISMODEL = "axismodel";
    static final String  NL_AXISWIDTH = "axiswidth";
    static final String  NL_BARSPACE = "barspace";
    static final String  NL_BRIGHTNESS = "brightness";
    static final String  NL_CLEAR = "clear";
    static final String  NL_CLICKRADIUS = "clickradius";
    static final String  NL_COLLECTRECORDSAT = "collectRecordsAt";
    static final String  NL_COLOREDBY = "coloredby";
    static final String  NL_COLORS = "colors";
    static final String  NL_CONNECT = "connect";
    static final String  NL_CONNECTCOLOR = "connectcolor";
    static final String  NL_CONNECTWIDTH = "connectwidth";
    static final String  NL_COUNTERS = "counters";
    static final String  NL_DATAENDS = "dataends";
    static final String  NL_DATAFIELDS = "datafields";
    static final String  NL_DATAFILTERS = "datafilters";
    static final String  NL_DATAMANAGER = "datamanager";
    static final String  NL_DATAPLOTS = "dataplots";
    static final String  NL_DATATABLES = "datatables";
    static final String  NL_DATAVIEWERS = "dataviewers";
    static final String  NL_DISPOSE = "dispose";
    static final String  NL_DIVERSITY = "diversity";
    static final String  NL_DRAGCOLOR = "dragcolor";
    static final String  NL_EDGECOUNT = "edgecount";
    static final String  NL_EDGEFLAGS = "edgeflags";
    static final String  NL_EDGES = "edges";
    static final String  NL_EDGESCALE = "edgescale";
    static final String  NL_EMPTYCOLOR = "emptycolor";
    static final String  NL_ENABLED = "enabled";
    static final String  NL_FIELD = "field";
    static final String  NL_FIELDINDEX = "fieldindex";
    static final String  NL_FIELDINDICES = "fieldindices";
    static final String  NL_FILLMODEL = "fillmodel";
    static final String  NL_FILTER = "filter";
    static final String  NL_FONTSCALE = "fontscale";
    static final String  NL_FROZEN = "frozen";
    static final String  NL_GENERATOR = "generator";
    static final String  NL_GETFIELDS = "getFields";
    static final String  NL_GETINDEX = "getIndex";
    static final String  NL_GETINDICES = "getIndices";
    static final String  NL_GETPOSITION = "getPosition";
    static final String  NL_GETSTATE = "getState";
    static final String  NL_GETTIPTEXTAT = "getTipTextAt";
    static final String  NL_GETVALUES = "getValues";
    static final String  NL_GRAPHDATA = "graphdata";
    static final String  NL_GRAPHLAYOUTARG = "graphlayoutarg";
    static final String  NL_GRAPHLAYOUTMODEL = "graphlayoutmodel";
    static final String  NL_GRAPHLAYOUTSORT = "graphlayoutsort";
    static final String  NL_GRAPHMATRIX = "graphmatrix";
    static final String  NL_HIDEPOINTS = "hidepoints";
    static final String  NL_HIDEUNLABELED = "hideunlabeled";
    static final String  NL_HIGHLIGHTCOLOR = "highlightcolor";
    static final String  NL_HIGHLIGHTED = "highlighted";
    static final String  NL_HOLIDAYS = "holidays";
    static final String  NL_HUE = "hue";
    static final String  NL_IGNOREZERO = "ignorezero";
    static final String  NL_INDEX = "index";
    static final String  NL_INPUTCOMMENT = "inputcomment";
    static final String  NL_INPUTFILTER = "inputfilter";
    static final String  NL_INTERN = "intern";
    static final String  NL_INVERTED = "inverted";
    static final String  NL_KEEPTALL = "keeptall";
    static final String  NL_KEYS = "keys";
    static final String  NL_LABELFLAGS = "labelflags";
    static final String  NL_LABELS = "labels";
    static final String  NL_LINEWIDTH = "linewidth";
    static final String  NL_LOADEDCOUNT = "loadedcount";
    static final String  NL_LOADEDENDS = "loadedends";
    static final String  NL_LOADRECORDS = "loadRecords";
    static final String  NL_MODEL = "model";
    static final String  NL_MONITOR = "monitor";
    static final String  NL_MOVED = "moved";
    static final String  NL_NODECOUNT = "nodecount";
    static final String  NL_NODEFLAGS = "nodeflags";
    static final String  NL_NODEOUTLINE = "nodeoutline";
    static final String  NL_NODES = "nodes";
    static final String  NL_NODESCALE = "nodescale";
    static final String  NL_OFFPEAKCOLOR = "offpeakcolor";
    static final String  NL_OPERATIONS = "operations";
    static final String  NL_ORIENTATION = "orientation";
    static final String  NL_OTHERCOLOR = "othercolor";
    static final String  NL_OUTLINECACHESIZE = "outlinecachesize";
    static final String  NL_OUTPUTFILTER = "outputfilter";
    static final String  NL_PAINTORDER = "paintorder";
    static final String  NL_PALETTE = "palette";
    static final String  NL_PEAKDAYS = "peakdays";
    static final String  NL_PEAKSTART = "peakstart";
    static final String  NL_PEAKSTOP = "peakstop";
    static final String  NL_PERSISTENT = "persistent";
    static final String  NL_PLOT = "plot";
    static final String  NL_PLOTENDS = "plotends";
    static final String  NL_PLOTFILTERS = "plotfilters";
    static final String  NL_PLOTSTYLE = "plotstyle";
    static final String  NL_PLOTSTYLEFLAGS = "plotstyleflags";
    static final String  NL_POINTSIZE = "pointsize";
    static final String  NL_PRESSED = "pressed";
    static final String  NL_PRESSEDCOLOR = "pressedcolor";
    static final String  NL_PRESSINGCOLOR = "pressingcolor";
    static final String  NL_PRIMARYFIELD = "primaryfield";
    static final String  NL_RANKPREFIX = "rankprefix";
    static final String  NL_RANKSUFFIX = "ranksuffix";
    static final String  NL_RECOLORED = "recolored";
    static final String  NL_REVERSEPALETTE = "reversepalette";
    static final String  NL_SATURATION = "saturation";
    static final String  NL_SELECT = "select";
    static final String  NL_SELECTED = "selected";
    static final String  NL_SELECTEDCOUNT = "selectedcount";
    static final String  NL_SELECTFLAGS = "selectflags";
    static final String  NL_SELECTWIDTH = "selectwidth";
    static final String  NL_SEPARATOR = "separator";
    static final String  NL_SEQUENCE = "sequence";
    static final String  NL_SETALL = "setAll";
    static final String  NL_SETPLOTENDS = "setPlotEnds";
    static final String  NL_SHADETIMES = "shadetimes";
    static final String  NL_SLIDERCOLOR = "slidercolor";
    static final String  NL_SLIDERENABLED = "sliderenabled";
    static final String  NL_SLIDERENDS = "sliderends";
    static final String  NL_SORTBY = "sortby";
    static final String  NL_SORTDEFAULT = "sortdefault";
    static final String  NL_SORTEDBY = "sortedby";
    static final String  NL_STACKED = "stacked";
    static final String  NL_SUBORDINATEPLOTS = "subordinatePlots";
    static final String  NL_SWEEPCOLOR = "sweepcolor";
    static final String  NL_SWEEPFILTERS = "sweepfilters";
    static final String  NL_SWEEPFLAGS = "sweepflags";
    static final String  NL_SYMMETRIC = "symmetric";
    static final String  NL_TAG = "tag";
    static final String  NL_TEXT = "text";
    static final String  NL_TEXTMODE = "textmode";
    static final String  NL_TICKS = "ticks";
    static final String  NL_TIMESHADING = "timeshading";
    static final String  NL_TIPDROPPED = "tipdropped";
    static final String  NL_TIPENABLED = "tipenabled";
    static final String  NL_TIPFLAGS = "tipflags";
    static final String  NL_TIPLOCKMODEL = "tiplockmodel";
    static final String  NL_TIPOFFSET = "tipoffset";
    static final String  NL_TIPPREFIX = "tipprefix";
    static final String  NL_TIPSUFFIX = "tipsuffix";
    static final String  NL_TOTALS = "totals";
    static final String  NL_TRANSIENTMODE = "transientmode";
    static final String  NL_TRANSLATOR = "translator";
    static final String  NL_TYPE = "type";
    static final String  NL_UNIXTIME = "unixtime";
    static final String  NL_UNSELECTED = "unselected";
    static final String  NL_VALUE = "value";
    static final String  NL_WIDTH = "width";
    static final String  NL_XAXIS = "xaxis";
    static final String  NL_YAXIS = "yaxis";
    static final String  NL_ZOOM = "zoom";
    static final String  NL_ZOOMDIRECTION = "zoomdirection";
    static final String  NL_ZOOMINCOLOR = "zoomincolor";
    static final String  NL_ZOOMLIMIT = "zoomlimit";
    static final String  NL_ZOOMOUTCOLOR = "zoomoutcolor";
    static final String  NL_ZOOMSCALE = "zoomscale";

    static final String  NL_SPREAD = "spread";
    static final String  NL_GETZOOMENDS = "getZoomEnds";
    static final String  NL_TRIMFIELDS = "trimfields";
    static final String  NL_CANRANK = "canrank";

    //
    // Unique value associated with each local field name.
    //

    static final int  VL_ACCUMULATE = 1;
    static final int  VL_ACTIVE = 2;
    static final int  VL_ACTIVEFIELDCOUNT = 3;
    static final int  VL_AFTERAPPEND = 4;
    static final int  VL_AFTERCOLOREDBY = 5;
    static final int  VL_AFTERLOAD = 6;
    static final int  VL_AFTERPRESSED = 7;
    static final int  VL_AFTERSELECT = 8;
    static final int  VL_AFTERSWEEP = 9;
    static final int  VL_AFTERUPDATE = 10;
    static final int  VL_ALIVE = 11;
    static final int  VL_ANCHOR = 12;
    static final int  VL_APPENDTEXT = 13;
    static final int  VL_ATTACHEDEDGESELECTION = 14;
    static final int  VL_AUTOREADY = 15;
    static final int  VL_AUTOSCROLL = 16;
    static final int  VL_AUTOSHOW = 17;
    static final int  VL_AXISENDS = 18;
    static final int  VL_AXISLIMITS = 19;
    static final int  VL_AXISMODEL = 20;
    static final int  VL_AXISWIDTH = 21;
    static final int  VL_BARSPACE = 22;
    static final int  VL_BRIGHTNESS = 23;
    static final int  VL_CLEAR = 24;
    static final int  VL_CLICKRADIUS = 25;
    static final int  VL_COLLECTRECORDSAT = 26;
    static final int  VL_COLOREDBY = 27;
    static final int  VL_COLORS = 28;
    static final int  VL_CONNECT = 29;
    static final int  VL_CONNECTCOLOR = 30;
    static final int  VL_CONNECTWIDTH = 31;
    static final int  VL_COUNTERS = 32;
    static final int  VL_DATAENDS = 33;
    static final int  VL_DATAFIELDS = 34;
    static final int  VL_DATAFILTERS = 35;
    static final int  VL_DATAMANAGER = 36;
    static final int  VL_DATAPLOTS = 37;
    static final int  VL_DATATABLES = 38;
    static final int  VL_DATAVIEWERS = 39;
    static final int  VL_DISPOSE = 40;
    static final int  VL_DIVERSITY = 41;
    static final int  VL_DRAGCOLOR = 42;
    static final int  VL_EDGECOUNT = 43;
    static final int  VL_EDGEFLAGS = 44;
    static final int  VL_EDGES = 45;
    static final int  VL_EDGESCALE = 46;
    static final int  VL_EMPTYCOLOR = 47;
    static final int  VL_ENABLED = 48;
    static final int  VL_FIELD = 49;
    static final int  VL_FIELDINDEX = 50;
    static final int  VL_FIELDINDICES = 51;
    static final int  VL_FILLMODEL = 52;
    static final int  VL_FILTER = 53;
    static final int  VL_FINDNEXTMATCH = 54;
    static final int  VL_FONTSCALE = 55;
    static final int  VL_FROZEN = 56;
    static final int  VL_GENERATOR = 57;
    static final int  VL_GETFIELDS = 58;
    static final int  VL_GETINDEX = 59;
    static final int  VL_GETINDICES = 60;
    static final int  VL_GETPOSITION = 61;
    static final int  VL_GETSTATE = 62;
    static final int  VL_GETTIPTEXTAT = 63;
    static final int  VL_GETVALUES = 64;
    static final int  VL_GRAPHDATA = 65;
    static final int  VL_GRAPHLAYOUTARG = 66;
    static final int  VL_GRAPHLAYOUTMODEL = 67;
    static final int  VL_GRAPHLAYOUTSORT = 68;
    static final int  VL_GRAPHMATRIX = 69;
    static final int  VL_HIDEPOINTS = 70;
    static final int  VL_HIDEUNLABELED = 71;
    static final int  VL_HIGHLIGHTCOLOR = 72;
    static final int  VL_HIGHLIGHTED = 73;
    static final int  VL_HOLIDAYS = 74;
    static final int  VL_HUE = 75;
    static final int  VL_IGNOREZERO = 76;
    static final int  VL_INDEX = 77;
    static final int  VL_INPUTCOMMENT = 78;
    static final int  VL_INPUTFILTER = 79;
    static final int  VL_INTERN = 80;
    static final int  VL_INVERTED = 81;
    static final int  VL_KEEPTALL = 82;
    static final int  VL_KEYS = 83;
    static final int  VL_LABELFLAGS = 84;
    static final int  VL_LABELS = 85;
    static final int  VL_LINEWIDTH = 86;
    static final int  VL_LOADEDCOUNT = 87;
    static final int  VL_LOADEDENDS = 88;
    static final int  VL_LOADRECORDS = 89;
    static final int  VL_MODEL = 90;
    static final int  VL_MONITOR = 91;
    static final int  VL_MOVED = 92;
    static final int  VL_NODECOUNT = 93;
    static final int  VL_NODEFLAGS = 94;
    static final int  VL_NODEOUTLINE = 95;
    static final int  VL_NODES = 96;
    static final int  VL_NODESCALE = 97;
    static final int  VL_OFFPEAKCOLOR = 98;
    static final int  VL_OPERATIONS = 99;
    static final int  VL_ORIENTATION = 100;
    static final int  VL_OTHERCOLOR = 101;
    static final int  VL_OUTLINECACHESIZE = 102;
    static final int  VL_OUTPUTFILTER = 103;
    static final int  VL_PAINTORDER = 104;
    static final int  VL_PALETTE = 105;
    static final int  VL_PEAKDAYS = 106;
    static final int  VL_PEAKSTART = 107;
    static final int  VL_PEAKSTOP = 108;
    static final int  VL_PERSISTENT = 109;
    static final int  VL_PLOT = 110;
    static final int  VL_PLOTENDS = 111;
    static final int  VL_PLOTFILTERS = 112;
    static final int  VL_PLOTSTYLE = 113;
    static final int  VL_PLOTSTYLEFLAGS = 114;
    static final int  VL_POINTSIZE = 115;
    static final int  VL_PRESSED = 116;
    static final int  VL_PRESSEDCOLOR = 117;
    static final int  VL_PRESSINGCOLOR = 118;
    static final int  VL_PRIMARYFIELD = 119;
    static final int  VL_RANKPREFIX = 120;
    static final int  VL_RANKSUFFIX = 121;
    static final int  VL_RECOLORED = 122;
    static final int  VL_REVERSEPALETTE = 123;
    static final int  VL_SATURATION = 124;
    static final int  VL_SELECT = 125;
    static final int  VL_SELECTED = 126;
    static final int  VL_SELECTEDCOUNT = 127;
    static final int  VL_SELECTFLAGS = 128;
    static final int  VL_SELECTWIDTH = 129;
    static final int  VL_SEPARATOR = 130;
    static final int  VL_SEQUENCE = 131;
    static final int  VL_SETALL = 132;
    static final int  VL_SETPLOTENDS = 133;
    static final int  VL_SHADETIMES = 134;
    static final int  VL_SLIDERCOLOR = 135;
    static final int  VL_SLIDERENABLED = 136;
    static final int  VL_SLIDERENDS = 137;
    static final int  VL_SORTBY = 138;
    static final int  VL_SORTDEFAULT = 139;
    static final int  VL_SORTEDBY = 140;
    static final int  VL_STACKED = 141;
    static final int  VL_SUBORDINATEPLOTS = 142;
    static final int  VL_SWEEPCOLOR = 143;
    static final int  VL_SWEEPFILTERS = 144;
    static final int  VL_SWEEPFLAGS = 145;
    static final int  VL_SYMMETRIC = 146;
    static final int  VL_TAG = 147;
    static final int  VL_TEXT = 148;
    static final int  VL_TEXTMODE = 149;
    static final int  VL_TICKS = 150;
    static final int  VL_TIMESHADING = 151;
    static final int  VL_TIPDROPPED = 152;
    static final int  VL_TIPENABLED = 153;
    static final int  VL_TIPFLAGS = 154;
    static final int  VL_TIPLOCKMODEL = 155;
    static final int  VL_TIPOFFSET = 156;
    static final int  VL_TIPPREFIX = 157;
    static final int  VL_TIPSUFFIX = 158;
    static final int  VL_TOTALS = 159;
    static final int  VL_TRANSIENTMODE = 160;
    static final int  VL_TRANSLATOR = 161;
    static final int  VL_TYPE = 162;
    static final int  VL_UNIXTIME = 163;
    static final int  VL_UNSELECTED = 164;
    static final int  VL_VALUE = 165;
    static final int  VL_WIDTH = 166;
    static final int  VL_XAXIS = 167;
    static final int  VL_YAXIS = 168;
    static final int  VL_ZOOM = 169;
    static final int  VL_ZOOMDIRECTION = 170;
    static final int  VL_ZOOMINCOLOR = 171;
    static final int  VL_ZOOMLIMIT = 172;
    static final int  VL_ZOOMOUTCOLOR = 173;
    static final int  VL_ZOOMSCALE = 174;

    static final int  VL_SPREAD = 175;
    static final int  VL_GETZOOMENDS = 176;
    static final int  VL_TRIMFIELDS = 177;
    static final int  VL_CANRANK = 178;

    //
    // Sorting constants and a few synonmys.
    //

    static final String  NL_SORT_COLOR = "SORTBY_COLOR";
    static final String  NL_SORT_DIVERSITY = "SORTBY_DIVERSITY";
    static final String  NL_SORT_DIVERSITY2 = "SORTBY_DIVERSITY_PERCENT";
    static final String  NL_SORT_LOAD_ORDER = "SORTBY_LOAD_ORDER";
    static final String  NL_SORT_NUMERIC = "SORTBY_NUMERIC";
    static final String  NL_SORT_OCTET = "SORTBY_OCTET";
    static final String  NL_SORT_PRESSED = "SORTBY_PRESSED";
    static final String  NL_SORT_SELECTED = "SORTBY_SELECTED";
    static final String  NL_SORT_SELECTED2 = "SORTBY_SELECTED_PERCENT";
    static final String  NL_SORT_TEXT = "SORTBY_TEXT";
    static final String  NL_SORT_TIME = "SORTBY_TIME";
    static final String  NL_SORT_TOTAL = "SORTBY_TOTAL";
    static final String  NL_SORT_TRANSLATOR = "SORTBY_TRANSLATOR";

    static final int  VL_SORT_COLOR = 1;
    static final int  VL_SORT_DIVERSITY = 2;
    static final int  VL_SORT_DIVERSITY2 = 3;
    static final int  VL_SORT_LOAD_ORDER = 4;
    static final int  VL_SORT_NUMERIC = 5;
    static final int  VL_SORT_OCTET = 6;
    static final int  VL_SORT_PRESSED = 7;
    static final int  VL_SORT_SELECTED = 8;
    static final int  VL_SORT_SELECTED2 = 9;
    static final int  VL_SORT_TEXT = 10;
    static final int  VL_SORT_TIME = 11;
    static final int  VL_SORT_TOTAL = 12;
    static final int  VL_SORT_TRANSLATOR = 13;

    static final String  NL_SORT_IP = "SORTBY_IP";
    static final String  NL_SORT_NUMBER = "SORTBY_NUMBER";

    static final int  VL_SORT_IP = VL_SORT_OCTET;
    static final int  VL_SORT_NUMBER = VL_SORT_NUMERIC;

    //
    // Operation constants.
    //

    static final String  NL_OP_BRUSH = "OP_BRUSH";
    static final String  NL_OP_DRAG = "OP_DRAG";
    static final String  NL_OP_DRAW = "OP_DRAW";
    static final String  NL_OP_EDIT = "OP_EDIT";
    static final String  NL_OP_GRAB = "OP_GRAB";
    static final String  NL_OP_POINT = "OP_POINT";
    static final String  NL_OP_PRESS = "OP_PRESS";
    static final String  NL_OP_NONE = "OP_NONE";
    static final String  NL_OP_SCROLL = "OP_SCROLL";
    static final String  NL_OP_SELECT = "OP_SELECT";
    static final String  NL_OP_TIP = "OP_TIP";
    static final String  NL_OP_ZOOM = "OP_ZOOM";

    static final int  VL_OP_BRUSH = 1;
    static final int  VL_OP_DRAG = 2;
    static final int  VL_OP_DRAW = 3;
    static final int  VL_OP_EDIT = 4;
    static final int  VL_OP_GRAB = 5;
    static final int  VL_OP_POINT = 6;
    static final int  VL_OP_PRESS = 7;
    static final int  VL_OP_NONE = 8;
    static final int  VL_OP_SCROLL = 9;
    static final int  VL_OP_SELECT = 10;
    static final int  VL_OP_TIP = 11;
    static final int  VL_OP_ZOOM = 12;

    static final String  NL_OP_PAN = "OP_PAN";		// same as OP_GRAB
    static final int     VL_OP_PAN = VL_OP_GRAB;

    //
    // The first group of constants are used as indices into an array that
    // has at least MODIFER_OP_COUNT slots, so make sure the definitions of
    // MODIFER_OP_COUNT and the indices are consistent!!!!
    // 

    static final int  PLAIN_OP = 0;
    static final int  SHIFT_OP = 1;
    static final int  CONTROL_OP = 2;
    static final int  CONTROL_SHIFT_OP = 3;

    static final int  MODIFER_OP_COUNT = 4;
}

