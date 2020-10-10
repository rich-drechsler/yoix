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

package att.research.yoix.ychart;
import java.awt.*;
import att.research.yoix.*;

public
interface Constants

    extends YoixConstants

{

    //
    // We keep our own cache of some NULL YoixObjects, which means we can
    // eliminate method calls to create them. Done because the only reason
    // for this module is to improve the overall performane on big charts
    // so eliminating the method calls should help a little.
    //

    public static final YoixObject  NULL_ARRAY = YoixObject.newArray();
    public static final YoixObject  NULL_COLOR = YoixObject.newColor();
    public static final YoixObject  NULL_DICTIONARY = YoixObject.newDictionary();
    public static final YoixObject  NULL_OBJECT = YoixObject.newNull();
    public static final YoixObject  NULL_STRING = YoixObject.newString();

    //
    // These really don't help much, mostly because the foreground colors
    // that we're supposed to use should be defined in the NL_CELLCOLORS
    // dictionary, which might not be available when this file is loaded.
    //

    public static final YoixObject  DEFAULT_CELLBACKGROUND = YoixObject.newColor(Color.lightGray);
    public static final YoixObject  DEFAULT_CELLFOREGROUND = YoixObject.newColor(Color.black);
    public static final YoixObject  DEFAULT_SELECTEDFOREGROUND = YoixObject.newColor(Color.white);

    //
    // These are the names of variables that are assumed to be available by
    // the builtins that work with various charts. Some are variable names
    // that should be found by methods, like YoixInterpreter.getObject(),
    // that look through the current scope, while others are the names of
    // fields in dictionaries (e.g., NL_BLACK, NL_WHITE, and NL_LIGHTGRAY
    // are fields in the NL_CELLCOLORS dictionary).
    //

    public static final String  NL_ALIGNMENT = "alignment";
    public static final String  NL_ALLCELLS = "allcells";
    public static final String  NL_ALLNAMES = "allnames";
    public static final String  NL_ARROWSELECTOR = "arrowselector";
    public static final String  NL_BASELINE = "baseline";
    public static final String  NL_BLACK = "BLACK";
    public static final String  NL_BLOCKMAP = "blockmap";
    public static final String  NL_CELLASPECT = "cellaspect";
    public static final String  NL_CELLBORDER = "cellborder";
    public static final String  NL_CELLCOLORS = "CellColors";
    public static final String  NL_CELLCOUNT = "cellcount";
    public static final String  NL_CELLINSET = "cellinset";
    public static final String  NL_CELLMARGIN = "cellmargin";
    public static final String  NL_CELLSIZE = "cellsize";
    public static final String  NL_CELL_FITLER_STARTBIT = "CELL_FITLER_STARTBIT";
    public static final String  NL_CELL_MULTICELL = "CELL_MULTICELL";
    public static final String  NL_CELL_SELECTED = "CELL_SELECTED";
    public static final String  NL_CELL_VISIBLE = "CELL_VISIBLE";
    public static final String  NL_CHARTBOUNDS = "chartbounds";
    public static final String  NL_CHARTCELLS = "chartcells";
    public static final String  NL_CHARTGRID = "chartgrid";
    public static final String  NL_CHARTLABELS = "chartlabels";
    public static final String  NL_CHARTMODEL = "chartmodel";
    public static final String  NL_COLORBY_BLOCK = "COLORBY_BLOCK";
    public static final String  NL_COLORBY_CATEGORY = "COLORBY_CATEGORY";
    public static final String  NL_COLORBY_CHECKERBOARD = "COLORBY_CHECKERBOARD";
    public static final String  NL_COLORS = "colors";
    public static final String  NL_COLUMNENDS = "columnends";
    public static final String  NL_COLUMNS = "columns";
    public static final String  NL_COLUMNWIDTH = "columnwidth";
    public static final String  NL_DARKGRAY = "DARKGRAY";
    public static final String  NL_FINDMAP = "findmap";
    public static final String  NL_FILTERCOUNT = "filtercount";
    public static final String  NL_FILTERFLAGMAP = "filterflagmap";
    public static final String  NL_FLOATER = "floater";
    public static final String  NL_FLOATINGLABELS = "floatinglabels";
    public static final String  NL_FONT = "font";
    public static final String  NL_FONTBBOX = "fontbbox";
    public static final String  NL_GETBACKGROUND = "GetBackground";
    public static final String  NL_GETCACHEDCELLLABELGENERATORS = "GetCachedCellLabelGenerators";
    public static final String  NL_GETCELLBACKGROUND = "GetCellBackground";
    public static final String  NL_GETCOLORMENUDATA = "GetColorMenuData";
    public static final String  NL_GETFOREGROUND = "GetForeground";
    public static final String  NL_GETSELECTABLECELLNAMED = "GetSelectableCellNamed";
    public static final String  NL_GETSTRINGBOUNDS = "GetStringBounds";
    public static final String  NL_GETTEXT = "GetText";
    public static final String  NL_GETTIPTEXT = "GetTipText";
    public static final String  NL_GRAY = "GRAY";
    public static final String  NL_HOME = "home";
    public static final String  NL_LABELSPERCELL = "labelspercell";
    public static final String  NL_LASTCELLLAYER = "lastcelllayer";
    public static final String  NL_LAYER = "layer";
    public static final String  NL_LAYOUT = "layout";
    public static final String  NL_LIGHTGRAY = "LIGHTGRAY";
    public static final String  NL_LINEBOUNDS = "linebounds";
    public static final String  NL_MEDIUMDARKGRAY = "MEDIUMDARKGRAY";
    public static final String  NL_MEDIUMLIGHTGRAY = "MEDIUMLIGHTGRAY";
    public static final String  NL_MINBORDERSIZE = "minbordersize";
    public static final String  NL_MINLABELSIZE = "minlabelsize";
    public static final String  NL_MINMARKSIZE = "minmarksize";
    public static final String  NL_MINPOINTSIZE = "minpointsize";
    public static final String  NL_MINSCALING = "minscaling";
    public static final String  NL_MINSELECTEDSIZE = "minselectedsize";
    public static final String  NL_MOVEDCELLS = "movedcells";
    public static final String  NL_MOVINGCELL = "movingcell";
    public static final String  NL_MULTICELLCHART = "multicellchart";
    public static final String  NL_MULTICELLDATA = "multicelldata";
    public static final String  NL_NAMEMAP = "namemap";
    public static final String  NL_NEXTFILTERFLAG = "nextfilterflag";
    public static final String  NL_PAINTCELLMARKS = "PaintCellMarks";
    public static final String  NL_PICKCOLORDATA = "PickColorData";
    public static final String  NL_POINT = "point";
    public static final String  NL_PRELOADCELLLABELS = "PreloadCellLabelData";
    public static final String  NL_ROWENDS = "rowends";
    public static final String  NL_ROWHEIGHT = "rowheight";
    public static final String  NL_ROWS = "rows";
    public static final String  NL_SELECTED = "selected";
    public static final String  NL_SHOWUNSELECTEDLABELS = "showunselectedlabels";
    public static final String  NL_SIMPLECELLMODEL = "simplecellmodel";
    public static final String  NL_SUPPORTCLASS = "supportclass";
    public static final String  NL_TAN = "TAN";
    public static final String  NL_TEXT = "text";
    public static final String  NL_TEXTBOUNDS = "textbounds";
    public static final String  NL_UNKNOWN = "UNKNOWN";
    public static final String  NL_UPDATECHARTSTATUS = "UpdateChartStatus";
    public static final String  NL_USEYCHART = "USEYCHART";
    public static final String  NL_VALIDATECELLS = "validatecells";
    public static final String  NL_VISIBLE = "visible";
    public static final String  NL_WHITE = "WHITE";

    //
    // These fields required in each chart cell that we build and we also
    // assume they exist in every call that we access. Right now there's
    // no checking of these fields, so the module and Yoix code can be out
    // of sync and we won't be warned.
    //

    public static final String  NL_BACKGROUND = "background";
    public static final String  NL_BOUNDS = "bounds";
    public static final String  NL_COLUMN = "column";
    public static final String  NL_DATA = "data";
    public static final String  NL_ETC = "etc";
    public static final String  NL_FLAGS = "flags";
    public static final String  NL_FOREGROUND = "foreground";
    public static final String  NL_ID = "id";
    public static final String  NL_INDEX = "index";
    public static final String  NL_LABELS = "labels";
    public static final String  NL_MARKS = "marks";
    public static final String  NL_NAME = "name";
    public static final String  NL_NEXT = "next";
    public static final String  NL_NEXTLABEL = "nextlabel";
    public static final String  NL_PREV = "prev";
    public static final String  NL_ROW = "row";
    public static final String  NL_SELECTABLE = "selectable";
    public static final String  NL_SELECTEDFOREGROUND = "selectedforeground";
    public static final String  NL_STATE = "state";
    public static final String  NL_TIP = "tip";

    //
    // The VALIDATION_DATA[] array is used by the methods that try to make
    // sure the module and Yoix code that uses it are in sync (or at least
    // reasonably so). Checking is far from perfect, but is sufficient for
    // now.
    //
    // NOTE - this list definitely isn't complete and there's a bunch more
    // checking that could be done.
    //

    final String  VALIDATION_DATA[] = {
	NL_ALLCELLS,                     T_OBJECT,
	NL_ALLNAMES,                     T_OBJECT,
	NL_CELLASPECT,                   T_NUMBER,
	NL_CELLBORDER,                   T_NUMBER,
	NL_CELLCOLORS,                   T_OBJECT,
	NL_CELLCOUNT,                    T_NUMBER,
	NL_CELLINSET,                    T_NUMBER,
	NL_CELLMARGIN,                   T_NUMBER,
	NL_CELLSIZE,                     T_OBJECT,
	NL_CELL_FITLER_STARTBIT,         T_NUMBER,
	NL_CELL_MULTICELL,               T_NUMBER,
	NL_CELL_SELECTED,                T_NUMBER,
	NL_CELL_VISIBLE,                 T_NUMBER,
	NL_CHARTBOUNDS,                  T_OBJECT,
	NL_CHARTCELLS,                   T_OBJECT,
	NL_CHARTGRID,                    T_OBJECT,
	NL_COLUMNENDS,                   T_OBJECT,
	NL_COLUMNS,                      T_NUMBER,
	NL_COLUMNWIDTH,                  T_NUMBER,
	NL_FINDMAP,                      T_OBJECT,
	NL_FLOATINGLABELS,               T_NUMBER,
	NL_GETCACHEDCELLLABELGENERATORS, T_CALLABLE,
	NL_GETCELLBACKGROUND,            T_CALLABLE,
	NL_GETCOLORMENUDATA,             T_CALLABLE,
	NL_GETSELECTABLECELLNAMED,       T_CALLABLE,
	NL_GETSTRINGBOUNDS,              T_CALLABLE,
	NL_LABELSPERCELL,                T_NUMBER,
	NL_LASTCELLLAYER,                T_NUMBER,
	NL_MINBORDERSIZE,                T_NUMBER,
	NL_MINLABELSIZE,                 T_NUMBER,
	NL_MINMARKSIZE,                  T_NUMBER,
	NL_MINPOINTSIZE,                 T_NUMBER,
	NL_MINSELECTEDSIZE,              T_NUMBER,
	NL_MOVEDCELLS,                   T_OBJECT,
	NL_MULTICELLCHART,		 T_NUMBER,
	NL_MULTICELLDATA,		 T_OBJECT,
	NL_NAMEMAP,                      T_OBJECT,
	NL_PAINTCELLMARKS,               T_CALLABLE,
	NL_PICKCOLORDATA,                T_CALLABLE,
	NL_PRELOADCELLLABELS,            T_CALLABLE,
	NL_ROWENDS,                      T_OBJECT,
	NL_ROWHEIGHT,                    T_NUMBER,
	NL_ROWS,                         T_NUMBER,
	NL_SIMPLECELLMODEL,              T_NUMBER,
	NL_UPDATECHARTSTATUS,            T_CALLABLE,
	NL_VALIDATECELLS,                T_NUMBER,
    };
}

