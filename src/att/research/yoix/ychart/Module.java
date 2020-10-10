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
import java.awt.geom.*;
import java.util.*;
import att.research.yoix.*;

public abstract
class Module extends YoixModule

    implements Constants

{

    //
    // An experimental module and must be synced with the Yoix code that's
    // used to manage and display charts. Includes some simple validation
    // code, but currently assumes it's called by the Yoix script, but we
    // eventually may force the validation the first time a builtin that
    // really works on the chart is called.
    // 

    public static final String  $MODULENAME = "ychart";
    public static final String  $MODULECREATED = "Tue Nov 25 13:02:20 EST 2008";
    public static final String  $MODULEVERSION = "2.0";

    public static Object  $module[] = {
    //
    // NAME                        ARG                 COMMAND     MODE   REFERENCE
    // ----                        ---                 -------     ----   ---------
       $MODULENAME,                "17",               $LIST,      $RORO, $MODULENAME,

       "checkYchartNames",         "0",                $BUILTIN,   $LR_X, null,
       "colorChartCells",          "1",                $BUILTIN,   $LR_X, null,
       "generateCellLabels",       "0",                $BUILTIN,   $LR_X, null,
       "getCellBackground",        "2",                $BUILTIN,   $LR_X, null,
       "getNextChartCell",         "3",                $BUILTIN,   $LR_X, null,
       "getNextMatchingCell",      "5",                $BUILTIN,   $LR_X, null,
       "getSelectionAsCell",       "-2",               $BUILTIN,   $LR_X, null,
       "isYchartUsable",           "0",                $BUILTIN,   $LR_X, null,
       "loadChart",                "",                 $BUILTIN,   $LR_X, null,
       "newChartCell",             "7",                $BUILTIN,   $LR_X, null,
       "paintChartCells",          "2",                $BUILTIN,   $LR_X, null,
       "paintChartLabels",         "2",                $BUILTIN,   $LR_X, null,
       "paintFloatingLabel",       "6",                $BUILTIN,   $LR_X, null,
       "paintFloatingLabels",      "5",                $BUILTIN,   $LR_X, null,
       "prepareMulticellData",     "0",                $BUILTIN,   $LR_X, null,
       "prepareChartCells",        "0",                $BUILTIN,   $LR_X, null,
       "validateYchartModule",     "0",                $BUILTIN,   $LR_X, null,
    };

    //
    // These are managed by the validateYchartModule() module and are only
    // set once.
    //

    private static boolean  autovalidate = true;
    private static boolean  validated = false;
    private static boolean  valid = true;
    private static boolean  useychart = true;

    ///////////////////////////////////
    //
    // Module Methods
    //
    ///////////////////////////////////

    public static YoixObject
    checkYchartNames(YoixObject arg[]) {

	ArrayList  badnames;

	if ((badnames = Misc.getBadNames()) != null && badnames.size() == 0)
	    badnames = null;
	return(YoixMisc.copyIntoArray(badnames));
    }


    public static YoixObject
    colorChartCells(YoixObject arg[]) {

	boolean  result = false;

	if (arg[0].isArray()) {
	    if (result = isValid())
		handleColorChartCells(arg[0]);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    generateCellLabels(YoixObject arg[]) {

	boolean  result = false;

	if (result = isValid())
	    result = handleGenerateCellLabels();
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    getCellBackground(YoixObject arg[]) {

	YoixObject  color = null;
	Object      background;

	if (arg[0].isDictionary()) {
	    if (arg[1].isDictionary()) {
		if (isValid()) {
		    background = YoixReflect.invoke(YoixInterpreter.getString(NL_SUPPORTCLASS), "getCellBackground", arg);
		    if (background instanceof YoixObject)
			color = (YoixObject)background;
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(color != null && color.isColor() ? color : NULL_COLOR);
    }


    public static YoixObject
    getNextChartCell(YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  lptr;
	int         layer[];

	if (arg[0].isInteger()) {
	    if (arg[1].isIntegerPointer()) {
		if (arg[2].isInteger()) {
		    if (isValid()) {
			lptr = arg[1];
			layer = new int[] {lptr.getInt(lptr.offset(), 0)};
			obj = handleGetNextChartCell(
			    YoixInterpreter.getObject(NL_CHARTCELLS),
			    arg[0].intValue(),
			    layer,
			    arg[2].intValue(),
			    YoixInterpreter.getBoolean(NL_SIMPLECELLMODEL)
			);
			lptr.putInt(lptr.offset(), layer[0]);
		    }
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj != null ? obj : NULL_DICTIONARY);
    }


    public static YoixObject
    getNextMatchingCell(YoixObject arg[]) {

	YoixObject  obj = null;

	if (arg[0].isDictionary()) {
	    if (arg[1].isString() || arg[1].isNull()) {
		if (arg[2].isInteger()) {
		    if (arg[3].isInteger()) {
			if (arg[4].isInteger()) {
			    if (isValid()) {
				if (arg[1].sizeof() > 0) {
				    obj = handleGetNextMatchingCell(
					arg[0],
					arg[1].stringValue(),
					arg[2].intValue(),
					arg[3].booleanValue(),
					arg[4].intValue()
				    );
				}
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(obj != null ? obj : NULL_DICTIONARY);
    }


    public static YoixObject
    getSelectionAsCell(YoixObject arg[]) {

	boolean  result = false;
	Object   loaded;
	int      n;

	//
	// We expect at least 2 arguments. First should be an integer pointer,
	// the remaining args should be strings or null.
	//
	if (arg[0].isIntegerPointer()) {
	    n = 1;
	    while (n < arg.length) {
		if (arg[n].isString() || arg[n].isNull()) {
		    if (++n == arg.length) {
			if (isValid()) {
			    loaded = YoixReflect.invoke(YoixInterpreter.getString(NL_SUPPORTCLASS), "getSelectionAsCell", arg, YoixReflect.REFLECTION_ERROR);
			    if (loaded instanceof Boolean)
				result = ((Boolean)loaded).booleanValue();
			}
		    }
		} else VM.badArgument(n);
	    }
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    isYchartUsable(YoixObject arg[]) {

	ArrayList  badnames;

	//
	// You only get one chance at this, which means don't call it too
	// soon or in the wrong context or the module will be permanently
	// marked invalid.
	//

	if (arg.length == 0) {
	    if (validated == false) {
		badnames = Misc.getBadNames();
		valid = (badnames == null || badnames.size() == 0);
		useychart = YoixInterpreter.getBoolean(NL_USEYCHART, true);
		validated = true;
	    }
	} else VM.badCall();

	return(YoixObject.newInt(valid && useychart));
    }


    public static YoixObject
    loadChart(YoixObject arg[]) {

	boolean  result = false;
	Object   loaded;

	if (isValid()) {
	    loaded = YoixReflect.invoke(YoixInterpreter.getString(NL_SUPPORTCLASS), "loadChart", arg, YoixReflect.REFLECTION_ERROR);
	    if (loaded instanceof Boolean)
		result = ((Boolean)loaded).booleanValue();
	}
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    newChartCell(YoixObject arg[]) {

	YoixObject  cell = NULL_DICTIONARY;
	YoixObject  name;
	double      row;
	double      column;
	double      width;
	double      height;

	if (arg[0].isString() || arg[0].isNull()) {
	    if (arg[1].isNumber()) {
		if (arg[2].isNumber()) {
		    if (arg[3].isNumber()) {
			if (arg[4].isNumber()) {
			    if (isValid()) {
				row = arg[1].doubleValue();
				column = arg[2].doubleValue();
				width = arg[3].doubleValue();
				height = arg[4].doubleValue();
				name = arg[0].notNull() ? arg[0] : YoixObject.newString((int)row + "," + (int)column);
				cell = handleNewChartCell(name, row, column, width, height, arg[5], arg[6]);
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(cell);
    }


    public static YoixObject
    paintChartCells(YoixObject arg[]) {

	Graphics2D  g;
	boolean     result = false;
	double      corners[];  

	if (arg[0].isArray()) {
	    if (arg[1].isGraphics()) {
		if (result = isValid()) {
		    corners = new double[] {
			arg[0].getDouble(0, 0),
			arg[0].getDouble(1, 0),
			arg[0].getDouble(2, 0),
			arg[0].getDouble(3, 0)
		    };
		    handlePaintChartCells(corners, arg[1]);
		}
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    paintChartLabels(YoixObject arg[]) {

	Graphics2D  g;
	boolean     result = false;
	double      corners[];  

	if (arg[0].isRectangle()) {
	    if (arg[1].isGraphics()) {
		if (result = isValid())
		    handlePaintChartLabels(arg[0], arg[1]);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    paintFloatingLabel(YoixObject arg[]) {

	Rectangle2D  bbox_new;
	Rectangle2D  bbox_clean;
	Graphics2D   g;
	boolean      result = false;

	if (arg[0].isDictionary() || arg[0].isNull()) {
	    if (arg[1].isDictionary() || arg[1].isNull()) {
		if (arg[2].isInteger()) {
		    if (arg[3].isRectangle()) {
			if (arg[4].isRectangle() || arg[4].isNull()) {
			    if (arg[5].isGraphics()) {
				if (result = isValid()) {
				    if ((g = YoixMiscGraphics.getGraphics2D(arg[5])) != null) {
					if (arg[0].notNull() && arg[1].notNull()) {
					    bbox_new = new Rectangle2D.Double(
						arg[3].getDouble(N_X, 0),
						arg[3].getDouble(N_Y, 0),
						arg[3].getDouble(N_WIDTH, 0),
						arg[3].getDouble(N_HEIGHT, 0)
					    );
					    if (arg[4].notNull()) {
						bbox_clean = new Rectangle2D.Double(
						    arg[4].getDouble(N_X, 0),
						    arg[4].getDouble(N_Y, 0),
						    arg[4].getDouble(N_WIDTH, 0),
						    arg[4].getDouble(N_HEIGHT, 0)
						);
					    } else bbox_clean = null;
					    result = handlePaintFloatingLabel(arg[0], arg[1], arg[2].booleanValue(), bbox_new, bbox_clean, arg[5], g);
					}
					g.dispose();
				    } else result = false;
				}
			    } else VM.badArgument(5);
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    paintFloatingLabels(YoixObject arg[]) {

	Rectangle2D  bbox_new;
	Rectangle2D  bbox_clean;
	Graphics2D   g;
	boolean      result = false;
	double       corners[];  

	if (arg[0].isArray()) {
	    if (arg[1].isRectangle()) {
		if (arg[2].isRectangle() || arg[2].isNull()) {
		    if (arg[3].isDictionaryPointer()) {
		        if (arg[4].isGraphics()) {
			    if (result = isValid()) {
				if ((g = YoixMiscGraphics.getGraphics2D(arg[4])) != null) {
				    corners = new double[] {
					arg[0].getDouble(0, 0),
					arg[0].getDouble(1, 0),
					arg[0].getDouble(2, 0),
					arg[0].getDouble(3, 0)
				    };
				    bbox_new = new Rectangle2D.Double(
					arg[1].getDouble(N_X, 0),
					arg[1].getDouble(N_Y, 0),
					arg[1].getDouble(N_WIDTH, 0),
					arg[1].getDouble(N_HEIGHT, 0)
				    );
				    if (arg[2].notNull()) {
					bbox_clean = new Rectangle2D.Double(
					    arg[2].getDouble(N_X, 0),
					    arg[2].getDouble(N_Y, 0),
					    arg[2].getDouble(N_WIDTH, 0),
					    arg[2].getDouble(N_HEIGHT, 0)
					);
				    } else bbox_clean = null;
				    result = handlePaintFloatingLabels(corners, bbox_new, bbox_clean, arg[3], arg[4], g);
				    g.dispose();
				} else result = false;
			    }
			} else VM.badArgument(4);
		    } else VM.badArgument(3);
		} else VM.badArgument(2);
	    } else VM.badArgument(1);
	} else VM.badArgument(0);

	return(YoixObject.newInt(result));
    }


    public static YoixObject
    prepareChartCells(YoixObject arg[]) {

	boolean  result = false;

	if (result = isValid())
	    handlePrepareChartCells();
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    prepareMulticellData(YoixObject arg[]) {

	boolean  result = false;

	if (result = isValid())
	    handlePrepareMulticellData();
	return(YoixObject.newInt(result));
    }


    public static YoixObject
    validateYchartModule(YoixObject arg[]) {

	ArrayList  badnames;

	//
	// You only get one chance at this, which means don't call it too
	// soon or in the wrong context or the module will be permanently
	// marked invalid.
	//

	if (arg.length == 0) {
	    if (validated == false) {
		badnames = Misc.getBadNames();
		valid = (badnames == null || badnames.size() == 0);
		validated = true;
	    }
	} else VM.badCall();

	return(YoixObject.newInt(valid));
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static void
    handleColorChartCells(YoixObject data) {

	YoixObject  args[];
	YoixObject  cell;
	YoixObject  allcells;
	YoixObject  cellcolors;
	YoixObject  foreground;
	YoixObject  selectedforeground;
	YoixObject  getcellbackground;
	YoixObject  color;
	Object      background;
	String      supportclass;
	int         length;
	int         index;

	supportclass = YoixInterpreter.getString(NL_SUPPORTCLASS);
	allcells = YoixInterpreter.getObject(NL_ALLCELLS);
	getcellbackground = YoixInterpreter.getLvalue(NL_GETCELLBACKGROUND);
	cellcolors = YoixInterpreter.getObject(NL_CELLCOLORS);

	if ((foreground = cellcolors.getObject(NL_BLACK)) == null)
	    foreground = YoixObject.newColor(Color.black);
	if ((selectedforeground = cellcolors.getObject(NL_WHITE)) == null)
	    selectedforeground = YoixObject.newColor(Color.white);
	args = new YoixObject[2];
	if ((args[1] = data.getObject()) == null)
	    args[1] = NULL_DICTIONARY;
	length = allcells.sizeof();
	for (index = 0; index < length; index++) {
	    if ((cell = allcells.getObject(index)) != null) {
		cell.putObject(NL_FOREGROUND, foreground);
		cell.putObject(NL_SELECTEDFOREGROUND, selectedforeground);
		args[0] = cell;
		if (supportclass != null) {
		    background = YoixReflect.invoke(supportclass, "getCellBackground", args);
		    if (background instanceof YoixObject)
			color = (YoixObject)background;
		    else color = getcellbackground.call(args, null);
		} else color = getcellbackground.call(args, null);
		cell.putObject(NL_BACKGROUND, color);
	    }
	}
    }


    private static boolean
    handleGenerateCellLabels() {

	YoixObject  chartcells;
	YoixObject  cell;
	YoixObject  generator;
	YoixObject  generators;
	YoixObject  getcachedgenerators;
	YoixObject  preloadlabeldata;
	YoixObject  updatechartstatus;
	YoixObject  getstringbounds;
	YoixObject  currentlinebounds;
	YoixObject  linebounds;
	YoixObject  fontbbox;
	YoixObject  cellsize;
	YoixObject  font;
	YoixObject  rowends;
	YoixObject  ends;
	YoixObject  layout;
	YoixObject  label;
	YoixObject  labels;
	YoixObject  linebackground;
	YoixObject  lineforeground;
	YoixObject  funct;
	YoixObject  text;
	YoixObject  textbounds;
	YoixObject  dict;
	YoixObject  args[];
	boolean     simplecellmodel;
	boolean     result = false;
	double      baseline;
	double      cellwidth;
	double      cellinset;
	double      interiorwidth;
	double      width;
	double      x;
	double      y;
	int         layer[];
	int         lastcelllayer;
	int         columns;
	int         nextindex;
	int         lastindex;
	int         index;
	int         length;
	int         step;
	int         row;
	int         n;

	if ((getcachedgenerators = YoixInterpreter.getLvalue(NL_GETCACHEDCELLLABELGENERATORS)) != null) {
	    generators = getcachedgenerators.call(new YoixObject[0], null);
	    if ((preloadlabeldata =  YoixInterpreter.getLvalue(NL_PRELOADCELLLABELS)) != null) {
		chartcells = YoixInterpreter.getObject(NL_CHARTCELLS);
		rowends = YoixInterpreter.getObject(NL_ROWENDS);
		columns = YoixInterpreter.getInt(NL_COLUMNS, -1);
		getstringbounds = YoixInterpreter.getLvalue(NL_GETSTRINGBOUNDS);
		updatechartstatus = YoixInterpreter.getLvalue(NL_UPDATECHARTSTATUS);

		preloadlabeldata.call(new YoixObject[0], null);
		cellsize = YoixInterpreter.getObject(NL_CELLSIZE);
		cellinset = YoixInterpreter.getDouble(NL_CELLINSET, 0);
		simplecellmodel = YoixInterpreter.getBoolean(NL_SIMPLECELLMODEL);
		lastcelllayer = YoixInterpreter.getInt(NL_LASTCELLLAYER, 0);

		args = new YoixObject[1];
		cellwidth = cellsize.getDouble(N_WIDTH, 0);
		interiorwidth = cellwidth - 2*cellinset;
		layer = new int[1];
		length = generators.sizeof();
		for (index = length - 1, step = 1; step <= length; index = (index + 1)%length, step++) {
		    updatechartstatus.call(
			new YoixObject[] {
			    YoixObject.newInt(step > 1), 
			    YoixObject.newString("Building Cell Labels: Step %d of %d"),
			    YoixObject.newInt(step),
			    YoixObject.newInt(length)
			},
			null
		    );
		    if ((generator = generators.getObject(index)) != null && generator.notNull()) {
			if (generator.getBoolean(NL_VISIBLE)) {
			    baseline = generator.getDouble(NL_BASELINE, 0);
			    font = generator.getObject(NL_FONT);
			    fontbbox = generator.getObject(NL_FONTBBOX);
			    currentlinebounds = YoixObject.newRectangle(
				0,
				baseline + fontbbox.getDouble(N_Y, 0),
				interiorwidth,
				fontbbox.getDouble(N_HEIGHT, 0)
			    );
			    for (row = rowends.offset(); row < rowends.sizeof(); row++) {
				if ((ends = rowends.getObject(row)) != null && ends.notNull()) {
				    nextindex = row*columns + ends.getInt(0, 0);
				    lastindex = row*columns + ends.getInt(1, 0);
				    for (; nextindex <= lastindex; nextindex++) {
					for (layer[0] = 0; layer[0] <= lastcelllayer && (cell = handleGetNextChartCell(chartcells, nextindex, layer, 1, simplecellmodel)) != null && cell.notNull(); ) {
					    labels = cell.getObject(NL_LABELS);
					    args[0] = cell;
					    if ((funct = generator.getObject(NL_GETBACKGROUND)) != null && funct.notNull())
						linebackground = funct.call(args, generator);
					    else linebackground = NULL_COLOR;
					    if ((funct = generator.getObject(NL_GETFOREGROUND)) != null && funct.notNull())
						lineforeground = funct.call(args, generator);
					    else lineforeground = DEFAULT_CELLFOREGROUND;
					    width = cell.getObject(NL_BOUNDS).getDouble(N_WIDTH, 0);
					    if (width != cellwidth) {
						width -= 2*cellinset;
						linebounds = YoixObject.newRectangle(
						    currentlinebounds.getDouble(N_X, 0),
						    currentlinebounds.getDouble(N_Y, 0),
						    width,
						    currentlinebounds.getDouble(N_HEIGHT, 0)
						);
					    } else {
						width = interiorwidth;
						linebounds = currentlinebounds;
					    }
					    if ((layout = generator.getObject(NL_LAYOUT)) != null && layout.notNull()) {
						for (n = layout.offset(); n < layout.sizeof(); n++) {
						    if ((label = layout.getObject(n)) != null && label.notNull()) {
							//
							// Unfortunately there's currently no getLvalue()
							// in YoixObject so we have to be careful making
							// the call.
							//
							if ((funct = label.getObject(NL_GETTEXT)) != null && funct.notNull()) {
							    if ((text = funct.call(args, label)) != null && text.notNull()) {
								textbounds = getstringbounds.call(new YoixObject[] {text, font}, null);
								switch (label.getInt(NL_ALIGNMENT, YOIX_CENTER)) {
								    case YOIX_CENTER:
									x = (width - textbounds.getDouble(N_WIDTH, 0))/2 - textbounds.getDouble(N_X, 0);
									break;

								    case YOIX_LEFT:
									x = 0;
									break;

								    case YOIX_RIGHT:
									x = width - textbounds.getDouble(N_WIDTH, 0) - textbounds.getDouble(N_X, 0);
									break;

								     default:
									x = 0;
									break;
								}
								textbounds.putDouble(N_X, textbounds.getDouble(N_X, 0) + x);
								textbounds.putDouble(N_Y, textbounds.getDouble(N_Y, 0) + baseline);
								dict = YoixObject.newDictionary(11);
								dict.put(NL_LINEBOUNDS, linebounds, false);
								dict.put(NL_TEXTBOUNDS, textbounds, false);
								dict.put(NL_TEXT, text, false);
								dict.put(NL_BACKGROUND, linebackground, false);
								dict.put(NL_FOREGROUND, lineforeground, false);
								dict.put(NL_FONT, font, false);
								dict.putInt(NL_SELECTED, generator.getInt(NL_SELECTED, 0));
								dict.putInt(NL_SELECTABLE, label.getBoolean(NL_SELECTABLE));
								dict.putInt(NL_FLOATER, label.getInt(NL_FLOATER, 0));
								dict.putObject(NL_GETTIPTEXT, label.getObject(NL_GETTIPTEXT));
								dict.putObject(NL_POINT, YoixObject.newPoint(x, baseline));
								linebackground = NULL_COLOR;
							    } else dict = NULL_DICTIONARY;
							} else dict = NULL_DICTIONARY;
							labels.put(cell.getInt(NL_NEXTLABEL, 0), dict, false);
							cell.putInt(NL_NEXTLABEL, cell.getInt(NL_NEXTLABEL, 0) + 1);
						    }
						}
					    }
					}
				    }
				}
			    }
			}
		    }
		}
		updatechartstatus.call(
		    new YoixObject[] {
			YoixObject.newInt(true), 
			NULL_STRING
		    },
		    null
		);
		result = true;
	    }
	}

	return(result);
    }


    private static YoixObject
    handleGetNextChartCell(YoixObject chartcells, int index, int layer[], int incr, boolean simplecellmodel) {

	YoixObject  cell = null;
	YoixObject  element;
	YoixObject  obj;
	int         lastcelllayer;
	int         n;

	if (simplecellmodel == false) {
	    lastcelllayer = YoixInterpreter.getInt(NL_LASTCELLLAYER, 0);
	    if (layer[0] <= lastcelllayer) {
		if (lastcelllayer != 0) {
		    if ((element = chartcells.getObject(index)) != null) {
			if (element.isDictionary()) {
			    if (layer[0] == 0) {
			        layer[0] = 1;
				cell = element;
			    }
			} else if (element.isArray()) {
			    if (incr >= 0) {
				for (n = layer[0] + element.offset(); n < element.sizeof(); n++) {
				    if ((obj = element.getObject(n)) != null && obj.isDictionary() && obj.notNull()) {
					cell = obj;
					n++;
					break;
				    }
				}
				layer[0] = n - element.offset();
			    } else {
				for (n = element.sizeof() - 1 - layer[0]; n >= element.offset(); n--) {
				    if ((obj = element.getObject(n)) != null && obj.isDictionary() && obj.notNull()) {
					cell = obj;
					n--;
					break;
				    }
				}
				layer[0] = element.sizeof() - 1 - n;
			    }
			}
		    }
		} else {
		    layer[0] = 1;
		    cell = chartcells.getObject(index);
		}
	    }
	} else {
	    if (layer[0] == 0) {
		layer[0] = 1;
		cell = chartcells.getObject(index);
	    }
	}

	return(cell);
    }


    private static YoixObject
    handleGetNextMatchingCell(YoixObject cell, String pattern, int type, boolean ignorecase, int direction) {

	YoixObject  nextcell;
	YoixObject  allnames;
	YoixObject  findmap;
	YoixObject  getselectablecellnamed;
	YoixObject  element;
	String      name;
	String      key;
	int         length;
	int         checked;
	int         index;
	int         incr;
	int         n;

	nextcell = NULL_DICTIONARY;

	if (pattern != null && pattern.length() > 0) {
	    allnames = YoixInterpreter.getObject(NL_ALLNAMES);
	    findmap = YoixInterpreter.getObject(NL_FINDMAP);
	    getselectablecellnamed = YoixInterpreter.getLvalue(NL_GETSELECTABLECELLNAMED);

	    pattern = ignorecase ? pattern.toUpperCase() : pattern;
	    incr = (direction >= 0) ? 1 : -1;
	    length = allnames.sizeof();
	    index = Misc.indexOfObject(allnames, cell.notNull() ? cell.getObject(NL_NAME) : NULL_STRING);

	    for (n = index + incr, checked = 0; checked < length; n += incr, checked++) {
		if (n >= length)
		    n = 0;
		else if (n < 0)
		    n = length - 1;
		name = allnames.getString(n);		// assumes allnames is full
		if (findmap.notNull()) {
		    if ((element = findmap.getObject(name)) != null) {
			if (element.isString() && element.notNull())
			    key = element.stringValue();
			else key = null;
		    } else key = null;
		} else key = name;
		if (key != null) {
		    if (ignorecase)
			key = key.toUpperCase();
		    if (key.indexOf(pattern) >= 0) {
			nextcell = getselectablecellnamed.call(new YoixObject[] {allnames.getObject(n)}, null);
			break;
		    }
		}
	    }
	}
	return(nextcell);
    }


    private static YoixObject
    handleNewChartCell(YoixObject name, double row, double column, double width, double height, YoixObject data, YoixObject etc) {

	YoixObject  cell;
	YoixObject  chartcells;
	int         columns;
	int         cellcount;
	int         index;

	cellcount = YoixInterpreter.getInt(NL_CELLCOUNT, 0);
	chartcells = YoixInterpreter.getObject(NL_CHARTCELLS);
	columns = YoixInterpreter.getInt(NL_COLUMNS, 0);
	index = (int)(((int)row)*columns + column);
	cell = Misc.createChartCell(
	    name, row, column, width, height, data, etc, columns,
	    YoixInterpreter.getInt(NL_CELL_VISIBLE, 0),
	    YoixInterpreter.getInt(NL_LABELSPERCELL, 0)
	);
	cell.putInt(NL_ID, cellcount++);
	YoixInterpreter.putInt(NL_CELLCOUNT, cellcount);
	chartcells.putObject(index, cell);
	return(cell);
    }


    private static boolean
    handlePaintChartCells(double corners[], YoixObject graphics) {

	Rectangle2D  bbox_new = null;
	YoixObject   chartcells;
	YoixObject   cell;
	YoixObject   bounds;
	YoixObject   linebounds;
	YoixObject   label;
	YoixObject   labels;
	YoixObject   rowends;
	YoixObject   ends;
	YoixObject   font;
	YoixObject   cellsize;
	YoixObject   cellcolors;
	YoixObject   currentfont = null;
	YoixObject   point;
	YoixObject   marks;
	YoixObject   paintcellmarks;
	YoixObject   bbox = null;
	YoixObject   floater_args[];
	YoixObject   mark_args[];
	Graphics2D   g;
	boolean      result = false;
	boolean      showselected;
	boolean      showunselectedlabels;
	boolean      simplecellmodel;
	double       minpointsize;
	double       minbordersize;
	double       minlabelsize;
	double       minselectedsize;
	double       minmarksize;
	double       cellborder;
	double       cellinset;
	double       width;
	double       height;
	double       x;
	double       y;
	double       tx;
	double       ty;
	Color        color;
	Color        currentcolor = null;
	int          layer[];
	int          lastcelllayer;
	int          nextlabel;
	int          cell_selected;
	int          cell_visible;
	int          columns;
	int          index;
	int          last;
	int          row;
	int          n;

	if ((g = YoixMiscGraphics.getGraphics2D(graphics)) != null) {
	    chartcells = YoixInterpreter.getObject(NL_CHARTCELLS);
	    rowends = YoixInterpreter.getObject(NL_ROWENDS);
	    columns = YoixInterpreter.getInt(NL_COLUMNS, -1);

	    cell_selected = YoixInterpreter.getInt(NL_CELL_SELECTED, 0);
	    cell_visible = YoixInterpreter.getInt(NL_CELL_VISIBLE, 0);
	    minpointsize = YoixInterpreter.getDouble(NL_MINPOINTSIZE, 0);
	    minbordersize = YoixInterpreter.getDouble(NL_MINBORDERSIZE, 0);
	    minlabelsize = YoixInterpreter.getDouble(NL_MINLABELSIZE, 0);
	    minselectedsize = YoixInterpreter.getDouble(NL_MINSELECTEDSIZE, 0);
	    minmarksize = YoixInterpreter.getDouble(NL_MINMARKSIZE, 0);

	    cellborder = YoixInterpreter.getDouble(NL_CELLBORDER, 0);
	    cellinset = YoixInterpreter.getDouble(NL_CELLINSET, 0);
	    cellsize = YoixInterpreter.getObject(NL_CELLSIZE);
	    cellcolors = YoixInterpreter.getObject(NL_CELLCOLORS);
	    simplecellmodel = YoixInterpreter.getBoolean(NL_SIMPLECELLMODEL);
	    lastcelllayer = YoixInterpreter.getInt(NL_LASTCELLLAYER, 0);
	    showunselectedlabels = YoixInterpreter.getBoolean(NL_SHOWUNSELECTEDLABELS, true);

	    paintcellmarks = YoixInterpreter.getLvalue(NL_PAINTCELLMARKS);
	    floater_args = new YoixObject[] {null, null, YoixObject.newInt(false), null, NULL_OBJECT, graphics};
	    mark_args = new YoixObject[] {null, graphics};
	    layer = new int[1];

	    for (row = (int)corners[0]; row <= corners[2]; row++) {
		if ((ends = rowends.getObject(row)) != null && ends.notNull()) {
		    index = (int)(row*columns + Math.max(corners[1], ends.getDouble(0, corners[1])));
		    last = (int)(row*columns + Math.min(corners[3], ends.getDouble(1, corners[3])));
		    for (; index <= last; index++) {
			for (layer[0] = 0; layer[0] <= lastcelllayer && (cell = handleGetNextChartCell(chartcells, index, layer, 1, simplecellmodel)) != null && cell.notNull(); ) {
			    if ((cell.getInt(NL_FLAGS, 0) & cell_visible) != 0) {
				showselected = (cell.getInt(NL_FLAGS, 0)&cell_selected) == cell_selected;
				bounds = cell.getObject(NL_BOUNDS);
				x = bounds.getDouble(N_X, 0);
				y = bounds.getDouble(N_Y, 0);
				width = bounds.getDouble(N_WIDTH, 0);
				height = bounds.getDouble(N_HEIGHT, 0);
				if (showselected && height <= minselectedsize) {
				    showselected = false;
				    if ((color = cellcolors.getColor(NL_LIGHTGRAY)) == null)
					color = Color.lightGray;
				} else color = cell.getColor(NL_BACKGROUND);
				if (color != currentcolor) {
				    currentcolor = color;
				    g.setColor(currentcolor);
				}
				if (cellborder >= minbordersize)
				    YoixMiscGraphics.rectButton(graphics, x, y, width, height, cellborder, cell.getInt(NL_STATE, 0), g);
				else YoixMiscGraphics.rectFill(graphics, x, y, width, height, g);
				if (height >= minlabelsize && (showunselectedlabels || showselected)) {
				    tx = x + cellinset;
				    ty = y + cellinset;
				    labels = cell.getObject(NL_LABELS);
				    nextlabel = cell.getInt(NL_NEXTLABEL, 0);
				    for (n = 0; n < nextlabel; n++) {
					if ((label = labels.getObject(n)) != null && label.notNull()) {
					    if (!label.getBoolean(NL_SELECTABLE) || cell.getBoolean(NL_SELECTABLE)) {
						if ((font = label.getObject(NL_FONT)) != null && font.notNull() && font.getDouble(N_POINTSIZE, 0) >= minpointsize) {
						    if (!label.getBoolean(NL_FLOATER)) {
							if ((color = label.getColor(NL_BACKGROUND)) != null) {
							    if (color != currentcolor) {
								currentcolor = color;
								g.setColor(currentcolor);
							    }
							    if ((linebounds = label.getObject(NL_LINEBOUNDS)) != null) {
								YoixMiscGraphics.rectFill(
								    graphics, 
								    linebounds.getDouble(N_X, 0) + tx,
								    linebounds.getDouble(N_Y, 0) + ty,
								    linebounds.getDouble(N_WIDTH, 0),
								    linebounds.getDouble(N_HEIGHT, 0),
								    g
								);
							    }
							}
							if (showselected && label.getBoolean(NL_SELECTED) || label.getBoolean(NL_SELECTABLE))
							    color = cell.getColor(NL_SELECTEDFOREGROUND);
							else color = label.getColor(NL_FOREGROUND);
							if (color != currentcolor) {
							    currentcolor = color;
							    g.setColor(currentcolor);
							}
							//
							// Decided to ignore the currentfont check that's in the
							// Yoix script, at least for now. Code would look like
							//
							//     if (font.bodyEquals(currentfont) == false) {
							//         currentfont = font;
							//         graphics.putObject(N_FONT, currentfont);
							//     }
							//
							// but it probably needs to be carefully tested just to
							// sure.
							//
							graphics.putObject(N_FONT, font);
							point = label.getObject(NL_POINT);
							YoixMiscGraphics.drawString(graphics, label.getString(NL_TEXT), tx + point.getDouble(N_X, 0), ty + point.getDouble(N_Y, 0), g);
						    } else {
							if (bbox_new == null) {
							    bbox = graphics.execute(N_DRAWABLEBBOX, new YoixObject[0], null);
							    bbox_new = new Rectangle2D.Double(
								bbox.getDouble(N_X, 0),
								bbox.getDouble(N_Y, 0),
								bbox.getDouble(N_WIDTH, 0),
								bbox.getDouble(N_HEIGHT, 0)
							    );
							}
							handlePaintFloatingLabel(cell, label, false, bbox_new, null, graphics, g);
						    }
						}
					    }
					}
				    }
				}
				if ((marks = cell.getObject(NL_MARKS)) != null && marks.notNull()) {
				    if (height > minmarksize) {
					mark_args[0] = cell;
					paintcellmarks.call(mark_args, null);
				    }
				}
			    }
			}
		    }
		}
	    }
	    g.dispose();
	    result = true;
	}
	return(result);
    }


    private static boolean
    handlePaintChartLabels(YoixObject rect, YoixObject graphics) {

	Rectangle2D  rect1;
	Rectangle2D  rect2;
	YoixObject   chartlabels;
	YoixObject   dict;
	YoixObject   bounds;
	YoixObject   labels;
	YoixObject   label;
	YoixObject   point;
	YoixObject   font;
	Graphics2D   g;
	boolean      result = false;
	double       minpointsize;
	Color        color;
	Color        currentcolor = null;
	Color        foreground;
	int          m;
	int          n;

	if ((chartlabels = YoixInterpreter.getObject(NL_CHARTLABELS)) != null && chartlabels.sizeof() > 0) {
	    minpointsize = YoixInterpreter.getDouble(NL_MINPOINTSIZE, 0);
	    if ((g = YoixMiscGraphics.getGraphics2D(graphics)) != null) {
		rect1 = new Rectangle2D.Double(
		    rect.getDouble(N_X, 0),
		    rect.getDouble(N_Y, 0),
		    rect.getDouble(N_WIDTH, 0),
		    rect.getDouble(N_HEIGHT, 0)
		);
		foreground = graphics.getObject(N_DRAWABLE).getColor(N_FOREGROUND);
		for (n = chartlabels.offset(); n < chartlabels.sizeof(); n++) {
		    if ((dict = chartlabels.getObject(n)) != null && dict.notNull()) {
			bounds = dict.getObject(NL_BOUNDS);
			rect2 = new Rectangle2D.Double(
			    bounds.getDouble(N_X, 0),
			    bounds.getDouble(N_Y, 0),
			    bounds.getDouble(N_WIDTH, 0),
			    bounds.getDouble(N_HEIGHT, 0)
			);
			if (rect1.intersects(rect2.getX(), rect2.getY(), rect2.getWidth(), rect2.getHeight())) {
			    if ((labels = dict.getObject(NL_LABELS)) != null && dict.notNull()) {
				for (m = labels.offset(); m < labels.sizeof(); m++) {
				    if ((label = labels.getObject(m)) != null && label.notNull()) {
					bounds = label.getObject(NL_BOUNDS);
					if (rect1.intersects(bounds.getDouble(N_X, 0), bounds.getDouble(N_Y, 0), bounds.getDouble(N_WIDTH, 0), bounds.getDouble(N_HEIGHT, 0))) {
					    if ((font = label.getObject(NL_FONT)) != null && font.notNull() && font.getDouble(N_POINTSIZE, 0) >= label.getDouble(NL_MINSCALING, 0)*minpointsize) {
						if ((color = label.getColor(N_FOREGROUND)) == null)
						    color = foreground;
						if (color != currentcolor) {
						    currentcolor = color;
						    g.setColor(currentcolor);
						}
						graphics.putObject(N_FONT, font);
						point = label.getObject(NL_POINT);
						YoixMiscGraphics.drawString(graphics, label.getString(NL_TEXT), point.getDouble(N_X, 0), point.getDouble(N_Y, 0), g);
					    }
					}
				    }
				}
			    }
			}
		    }
		}
		g.dispose();
		result = true;
	    }
	}
	return(result);
    }


    private static boolean
    handlePaintFloatingLabel(YoixObject cell, YoixObject label, boolean erase, Rectangle2D bbox_new, Rectangle2D bbox_clean, YoixObject graphics, Graphics2D g) {

	Rectangle2D  linebounds;
	Rectangle2D  intersection;
	YoixObject   bounds;
	YoixObject   cellbounds;
	YoixObject   textbounds;
	YoixObject   labelpoint;
	YoixObject   movingcell;
	YoixObject   font;
	Point2D      point;
	boolean      showselected;
	boolean      result = true;
	double       minpointsize;
	double       minselectedsize;
	double       cellinset;
	double       cellmargin;
	double       xmax;
	double       tx;
	double       ty;
	Color        color;
	int          cell_selected;
	int          cell_visible;

	if (YoixInterpreter.getBoolean(NL_FLOATINGLABELS)) {
	    //
	    // The caller almost certainly has done the minpointsize test, so
	    // this one should be unnecessary.
	    //
	    minpointsize = YoixInterpreter.getDouble(NL_MINPOINTSIZE, 0);
	    if ((font = label.getObject(NL_FONT)) != null && font.notNull() && font.getDouble(N_POINTSIZE, 0) >= minpointsize) {
		cellinset = YoixInterpreter.getDouble(NL_CELLINSET, 0);
		cellmargin = YoixInterpreter.getDouble(NL_CELLMARGIN, 0);
		cell_selected = YoixInterpreter.getInt(NL_CELL_SELECTED, 0);
		cell_visible = YoixInterpreter.getInt(NL_CELL_VISIBLE, 0);
		movingcell = YoixInterpreter.getObject(NL_MOVINGCELL);
		minselectedsize = YoixInterpreter.getDouble(NL_MINSELECTEDSIZE, 0);

		showselected = (cell.getInt(NL_FLAGS, 0)&cell_selected) == cell_selected;
		cellbounds = cell.getObject(NL_BOUNDS);
		tx = cellbounds.getDouble(N_X, 0) + cellinset;
		ty = cellbounds.getDouble(N_Y, 0) + cellinset;

		bounds = label.getObject(NL_LINEBOUNDS);
		linebounds = new Rectangle2D.Double(
		    bounds.getDouble(N_X, 0) + tx,
		    bounds.getDouble(N_Y, 0) + ty,
		    bounds.getDouble(N_WIDTH, 0),
		    bounds.getDouble(N_HEIGHT, 0)
		);

		if (bbox_new.intersects(linebounds) && (bbox_clean == null || !bbox_clean.contains(linebounds))) {
		    textbounds = label.getObject(NL_TEXTBOUNDS);
		    labelpoint = label.getObject(NL_POINT);
		    xmax = cellbounds.getDouble(N_X, 0) + cellbounds.getDouble(N_WIDTH, 0) - cellinset - textbounds.getDouble(N_WIDTH, 0) - (textbounds.getDouble(N_X, 0) - labelpoint.getDouble(N_X, 0));
		    point = new Point2D.Double(
			labelpoint.getDouble(N_X, 0) + tx,
			labelpoint.getDouble(N_Y, 0) + ty
		    );

		    if (cell.bodyEquals(movingcell) == false) {
			if ((intersection = bbox_new.createIntersection(linebounds)) != null) {
			    if (intersection.isEmpty() == false && intersection.getX() > point.getX())
				point.setLocation(Math.min(xmax - cellmargin, intersection.getX()), point.getY());
			}
		    }

		    if (erase) {
			if (showselected && cellbounds.getDouble(N_HEIGHT, 0) <= minselectedsize)
			    g.setColor(YoixInterpreter.getObject(NL_CELLCOLORS).getColor(NL_LIGHTGRAY));
			else g.setColor(cell.getColor(NL_BACKGROUND));
			YoixMiscGraphics.rectFill(graphics, linebounds.getX(), linebounds.getY(), linebounds.getWidth(), linebounds.getHeight() + 2, g);
		    }
		    if ((color = label.getColor(NL_BACKGROUND)) != null) {
			g.setColor(color);
			YoixMiscGraphics.rectFill(graphics, linebounds.getX(), linebounds.getY(), linebounds.getWidth(), linebounds.getHeight(), g);
		    }
		    if (showselected && label.getBoolean(NL_SELECTED) && cellbounds.getDouble(N_HEIGHT, 0) > minselectedsize || label.getBoolean(NL_SELECTABLE))
			color = cell.getColor(NL_SELECTEDFOREGROUND);
		    else color = label.getColor(NL_FOREGROUND);
		    g.setColor(color);
		    graphics.putObject(N_FONT, font);
		    YoixMiscGraphics.drawString(graphics, label.getString(NL_TEXT), point.getX(), point.getY(), g);
		}
	    }
	}

	return(result);
    }


    private static boolean
    handlePaintFloatingLabels(double corners[], Rectangle2D bbox_new, Rectangle2D bbox_clean, YoixObject lptr, YoixObject graphics, Graphics2D g) {

	Rectangle2D  bbox_cell;
	YoixObject   chartcells;
	YoixObject   cell;
	YoixObject   cellsize;
	YoixObject   font;
	YoixObject   label;
	YoixObject   labels;
	YoixObject   bounds;
	YoixObject   lastcellpainted;
	YoixObject   marks;
	YoixObject   paintcellmarks;
	YoixObject   mark_args[];
	boolean      rowstatus[];
	boolean      showunselectedlabels;
	boolean      simplecellmodel;
	boolean      result = false;
	double       minpointsize;
	double       minlabelsize;
	double       minmarksize;
	int          layer[];
	int          lastcelllayer;
	int          nextlabel;
	int          cell_selected;
	int          cell_visible;
	int          paintedrows;
	int          rowcount;
	int          rows;
	int          columns;
	int          row;
	int          column;
        int          firstrow;
        int          lastrow;
        int          firstcolumn;
        int          lastcolumn;
	int          index;
	int          n;

	if (YoixInterpreter.getBoolean(NL_FLOATINGLABELS)) {
	    lastcellpainted = NULL_DICTIONARY;
	    chartcells = YoixInterpreter.getObject(NL_CHARTCELLS);
	    columns = YoixInterpreter.getInt(NL_COLUMNS, 0);
	    rows = YoixInterpreter.getInt(NL_ROWS, 0);
	    cell_selected = YoixInterpreter.getInt(NL_CELL_SELECTED, 0);
	    cell_visible = YoixInterpreter.getInt(NL_CELL_VISIBLE, 0);
	    cellsize = YoixInterpreter.getObject(NL_CELLSIZE);
	    simplecellmodel = YoixInterpreter.getBoolean(NL_SIMPLECELLMODEL);
	    lastcelllayer = YoixInterpreter.getInt(NL_LASTCELLLAYER, 0);
	    showunselectedlabels = YoixInterpreter.getBoolean(NL_SHOWUNSELECTEDLABELS, true);

	    minpointsize = YoixInterpreter.getDouble(NL_MINPOINTSIZE, 0);
	    minlabelsize = YoixInterpreter.getDouble(NL_MINLABELSIZE, 0);
	    minmarksize = YoixInterpreter.getDouble(NL_MINMARKSIZE, 0);
	    paintcellmarks = YoixInterpreter.getLvalue(NL_PAINTCELLMARKS);
	    mark_args = new YoixObject[] {null, graphics};
	    layer = new int[1];
	    if (cellsize.getDouble(N_HEIGHT, 0) >= minlabelsize) {
		if (bbox_clean != null) {
		    if ((bbox_clean = bbox_new.createIntersection(bbox_clean)) == null || bbox_clean.isEmpty())
			bbox_clean = bbox_new;
		} else bbox_clean = bbox_new;
		if (corners != null) {
		    lastcellpainted = null;
		    firstrow = (int)corners[0];
		    lastrow = (int)corners[2];
		    firstcolumn = (int)corners[1];
		    lastcolumn = (int)Math.max(firstcolumn, Math.max(Misc.getColumnAt(bbox_new.getX()), Misc.getColumnAt(bbox_clean.getX())));

		    paintedrows = 0;
		    rowcount = lastrow - firstrow + 1;
		    rowstatus = new boolean[rowcount];
		    for (n = 0; n < rowcount; n++)
			rowstatus[n] = false;
		    for (column = lastcolumn; column >= firstcolumn && paintedrows < rowcount; column--) {
			for (row = firstrow; row <= lastrow && paintedrows < rowcount; row++) {
			    if ((index = row*columns + column) >= 0 && index < chartcells.sizeof()) {
				for (layer[0] = 0; layer[0] <= lastcelllayer && (cell = handleGetNextChartCell(chartcells, index, layer, 1, simplecellmodel)) != null && cell.notNull(); ) {
				    if (showunselectedlabels || (cell.getInt(NL_FLAGS, 0)&cell_selected) == cell_selected) {
					if ((cell.getInt(NL_FLAGS, 0) & cell_visible) != 0) {
					    bounds = cell.getObject(NL_BOUNDS);
					    bbox_cell = new Rectangle2D.Double(
						bounds.getDouble(N_X, 0),
						bounds.getDouble(N_Y, 0),
						bounds.getDouble(N_WIDTH, 0),
						bounds.getDouble(N_HEIGHT, 0)
					    );
					    if (bbox_new.intersects(bbox_cell) && !bbox_clean.contains(bbox_cell)) {
						labels = cell.getObject(NL_LABELS);
						nextlabel = cell.getInt(NL_NEXTLABEL, 0);
						for (n = 0; n < nextlabel; n++) {
						    if ((label = labels.getObject(n)) != null && label.notNull()) {
							if (label.getBoolean(NL_FLOATER)) {
							    if (!label.getBoolean(NL_SELECTABLE) || cell.getBoolean(NL_SELECTABLE)) {
								if ((font = label.getObject(NL_FONT)) != null && font.notNull() && font.getDouble(N_POINTSIZE, 0) >= minpointsize) {
								    handlePaintFloatingLabel(cell, label, true, bbox_new, bbox_clean, graphics, g);
								    lastcellpainted = cell;
								}
							    }
							}
						    }
						}
						if (rowstatus[row - firstrow] == false) {
						    rowstatus[row - firstrow] = true;
						    paintedrows++;
						}
						if ((marks = cell.getObject(NL_MARKS)) != null && marks.notNull()) {
						    if (bounds.getDouble(N_HEIGHT, 0) > minmarksize) {
							mark_args[0] = cell;
							paintcellmarks.call(mark_args, null);
						    }
						}
					    }
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	    lptr.putObject(lptr.offset(), lastcellpainted);
	}
	return(result);
    }


    private static void
    handlePrepareChartCells() {

	YoixObject  obj;
	YoixObject  cell;
	YoixObject  bounds;
	YoixObject  home;
	YoixObject  chartcells;
	YoixObject  chartbounds;
	YoixObject  cellcolors;
	YoixObject  foreground;
	YoixObject  selectedforeground;
	YoixObject  getcellbackground;
	YoixObject  getcolormenudata;
	YoixObject  pickcolordata;
	YoixObject  data;
	YoixObject  dict;
	YoixObject  color;
	YoixObject  args[];
	boolean     multicellchart;
	boolean     simplecellmodel;
	Object      background;
	String      supportclass;
	double      columnwidth;
	double      rowheight;
	double      xorigin;
	double      yorigin;
	double      width;
	double      height;
	double      x;
	double      y;
	double      x0;
	double      y0;
	double      x1;
	double      y1;
	int         layer[];
	int         lastcelllayer;
	int         cell_multicell;
	int         columns;
	int         length;
	int         index;

	supportclass = YoixInterpreter.getString(NL_SUPPORTCLASS);
	chartcells = YoixInterpreter.getObject(NL_CHARTCELLS);
	chartbounds = YoixInterpreter.getObject(NL_CHARTBOUNDS);
	columns = YoixInterpreter.getInt(NL_COLUMNS, 0);
	columnwidth = YoixInterpreter.getDouble(NL_COLUMNWIDTH, 0);
	rowheight = YoixInterpreter.getDouble(NL_ROWHEIGHT, 0);
	cellcolors = YoixInterpreter.getObject(NL_CELLCOLORS);
	getcellbackground = YoixInterpreter.getLvalue(NL_GETCELLBACKGROUND);
	getcolormenudata = YoixInterpreter.getLvalue(NL_GETCOLORMENUDATA);
	pickcolordata = YoixInterpreter.getLvalue(NL_PICKCOLORDATA);
	simplecellmodel = YoixInterpreter.getBoolean(NL_SIMPLECELLMODEL);
	lastcelllayer = YoixInterpreter.getInt(NL_LASTCELLLAYER, 0);
	cell_multicell = YoixInterpreter.getInt(NL_CELL_MULTICELL, 0);
	multicellchart = false;

	if ((foreground = cellcolors.getObject(NL_BLACK)) == null)
	    foreground = YoixObject.newColor(Color.black);
	if ((selectedforeground = cellcolors.getObject(NL_WHITE)) == null)
	    selectedforeground = YoixObject.newColor(Color.white);

	data = pickcolordata.call(
	    new YoixObject[] {
		getcolormenudata.call(new YoixObject[0], null),
		NULL_OBJECT
	    },
	    null
	);
	args = new YoixObject[] {NULL_DICTIONARY, (dict = data.getObject()) != null ? dict : NULL_DICTIONARY};

	xorigin = chartbounds.getDouble(N_X, 0);
	yorigin = chartbounds.getDouble(N_Y, 0);
	length = chartcells.sizeof();
	layer = new int[1];

	for (index = 0; index < length; index++) {
	    for (layer[0] = 0; layer[0] <= lastcelllayer && (cell = handleGetNextChartCell(chartcells, index, layer, 1, simplecellmodel)) != null && cell.notNull(); ) {
		bounds = cell.getObject(NL_BOUNDS);
		x = bounds.getDouble(N_X, 0);
		y = bounds.getDouble(N_Y, 0);
		width = bounds.getDouble(N_WIDTH, 0);
		height = bounds.getDouble(N_HEIGHT, 0);
		if (((int)x + 1 < x + width) || ((int)y + 1 < y + height)) {
		    cell.putInt(NL_FLAGS, cell.getInt(NL_FLAGS, 0)|cell_multicell);
		    multicellchart = true;
		}
		x0 = xorigin + x*columnwidth;
		y0 = yorigin + y*rowheight;
		x1 = xorigin + (x + width)*columnwidth;
		y1 = yorigin + (y + height)*rowheight;
		bounds.putDouble(N_X, x0);
		bounds.putDouble(N_Y, y0);
		bounds.putDouble(N_WIDTH, x1 - x0);
		bounds.putDouble(N_HEIGHT, y1 - y0);
		home = cell.getObject(NL_HOME);
		home.putDouble(N_X, x0);
		home.putDouble(N_Y, y0);
		cell.putObject(NL_FOREGROUND, foreground);
		cell.putObject(NL_SELECTEDFOREGROUND, selectedforeground);
		args[0] = cell;
		if (supportclass != null) {
			background = YoixReflect.invoke(supportclass, "getCellBackground", args);
			if (background instanceof YoixObject)
			    color = (YoixObject)background;
		    else color = getcellbackground.call(args, null);
		} else color = getcellbackground.call(args, null);
		cell.putObject(NL_BACKGROUND, color);
	    }
	}
	YoixInterpreter.putInt(NL_MULTICELLCHART, multicellchart);
    }


    private static void
    handlePrepareMulticellData() {

	YoixObject  obj;
	YoixObject  cell;
	YoixObject  bounds;
	YoixObject  home;
	YoixObject  chartcells;
	YoixObject  chartbounds;
	YoixObject  cellcolors;
	YoixObject  foreground;
	YoixObject  selectedforeground;
	YoixObject  getcellbackground;
	YoixObject  getcolormenudata;
	YoixObject  pickcolordata;
	YoixObject  data;
	YoixObject  dict;
	YoixObject  color;
	YoixObject  corner;
	YoixObject  args[];
	boolean     simplecellmodel;
	Object      background;
	double      columnwidth;
	double      rowheight;
	double      xorigin;
	double      yorigin;
	double      width;
	double      height;
	double      x;
	double      y;
	double      x0;
	double      y0;
	double      x1;
	double      y1;
	int         layer[];
	int         lastcelllayer;
	int         cell_multicell;
	int         columns;
	int         rows;
	int         row;
	int         column;
	int         length;
	int         index;
	int         n;

	if (YoixInterpreter.getBoolean(NL_MULTICELLCHART)) {
	    chartcells = YoixInterpreter.getObject(NL_CHARTCELLS);
	    chartbounds = YoixInterpreter.getObject(NL_CHARTBOUNDS);
	    columns = YoixInterpreter.getInt(NL_COLUMNS, 0);
	    rows = YoixInterpreter.getInt(NL_ROWS, 0);
	    columnwidth = YoixInterpreter.getDouble(NL_COLUMNWIDTH, 0);
	    rowheight = YoixInterpreter.getDouble(NL_ROWHEIGHT, 0);
	    simplecellmodel = YoixInterpreter.getBoolean(NL_SIMPLECELLMODEL);
	    lastcelllayer = YoixInterpreter.getInt(NL_LASTCELLLAYER, 0);
	    cell_multicell = YoixInterpreter.getInt(NL_CELL_MULTICELL, 0);

	    xorigin = chartbounds.getDouble(N_X, 0);
	    yorigin = chartbounds.getDouble(N_Y, 0);
	    length = chartcells.sizeof();
	    layer = new int[1];
	    data = YoixObject.newArray(length);

	    for (n = 0; n < length; n++) {
		if (data.defined(n) == false)
		    data.putObject(n, NULL_ARRAY);
		for (layer[0] = 0; layer[0] <= lastcelllayer && (cell = handleGetNextChartCell(chartcells, n, layer, 1, simplecellmodel)) != null && cell.notNull(); ) {
		    if ((cell.getInt(NL_FLAGS, 0)&cell_multicell) != 0) {
			bounds = cell.getObject(NL_BOUNDS);
			x1 = bounds.getDouble(N_X, 0) + bounds.getDouble(N_WIDTH, 0);
			y1 = bounds.getDouble(N_Y, 0) + bounds.getDouble(N_HEIGHT, 0);
			for (row = cell.getInt(NL_ROW, rows); row < rows && yorigin + row*rowheight < y1; row++) {
			    for (column = cell.getInt(NL_COLUMN, columns); column < columns && xorigin + column*columnwidth < x1; column++) {
				index = row*columns + column;
				if (cell.getInt(NL_INDEX, -1) != index) {
				    if (data.defined(index)) {
					corner = data.getObject(index);
					if (corner.notNull()) {
					    if (column < corner.getInt(1, 0))
						corner.putInt(1, column);
					} else {
					    corner = YoixObject.newArray(2);
					    corner.putInt(0, cell.getInt(NL_ROW, 0));
					    corner.putInt(1, cell.getInt(NL_COLUMN, 0));
					    data.putObject(index, corner);
					}
				    } else {
					corner = YoixObject.newArray(2);
					corner.putInt(0, cell.getInt(NL_ROW, 0));
					corner.putInt(1, cell.getInt(NL_COLUMN, 0));
					data.putObject(index, corner);
				    }
				}
			    }
			}
		    }
		}
	    }
	    YoixInterpreter.putObject(NL_MULTICELLDATA, data);
	}
    }


    private static boolean
    isValid() {

	ArrayList  badnames;

	if (validated == false && autovalidate) {
	    badnames = Misc.getBadNames();
	    valid = (badnames == null || badnames.size() == 0);
	    useychart = YoixInterpreter.getBoolean(NL_USEYCHART, true);
	    validated = true;
	}
	return(valid && useychart);
    }
}

