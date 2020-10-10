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
import java.util.*;
import att.research.yoix.*;

public abstract
class Misc

    implements Constants

{

    //
    // A counter used to generate unique id's when new cells are built. Code
    // that uses this probably should be synchronized, but we're not going to
    // worry about it right now.
    //

    private static int  cellcounter = 0;

    ///////////////////////////////////
    //
    // Misc Methods
    //
    ///////////////////////////////////

    public static void
    addCellToChart(YoixObject cell, YoixObject chartcells, boolean simplecellmodel) {

	YoixObject  element;
	YoixObject  array;
	int         lastcelllayer;
	int         layer;
	int         index = cell.getInt(NL_INDEX, 0);

	if (simplecellmodel == false) {
	    if (chartcells.defined(index) && (element = chartcells.getObject(index)) != null && element.notNull()) {
		element = chartcells.getObject(index);
		if (element.isArray()) {
		    cell.putInt(NL_LAYER, element.sizeof());
		    element.put(element.sizeof(), cell, false);
		} else {
		    element.putInt(NL_LAYER, 0);
		    cell.putInt(NL_LAYER, 1);
		    array = YoixObject.newArray(2, -1);
		    array.put(0, element, false);
		    array.put(1, cell, false);
		    chartcells.put(index, array, false);
		}
		layer = cell.getInt(NL_LAYER, 0);
		lastcelllayer = YoixInterpreter.getInt(NL_LASTCELLLAYER, 0);
		if (layer > lastcelllayer)
		    YoixInterpreter.putInt(NL_LASTCELLLAYER, layer);
	    } else {
		chartcells.put(index, NULL_OBJECT, false);
		chartcells.put(index, cell, false);
	    }
	} else chartcells.put(index, cell, false);
    }


    public static YoixObject
    createChartCell(YoixObject name, double row, double column, double width, double height, YoixObject data, YoixObject etc, int columns, int flags, int labelspercell) {

	YoixObject dict;

	dict = YoixObject.newDictionary(22);
	dict.put(NL_BOUNDS, YoixObject.newRectangle(column, row, width, height), false);
	dict.put(NL_NAME, name, false);
	dict.put(NL_DATA, data, false);
	dict.put(NL_ETC, etc, false);
	dict.put(NL_BACKGROUND, DEFAULT_CELLBACKGROUND, false);
	dict.put(NL_FOREGROUND, DEFAULT_CELLFOREGROUND, false);
	dict.put(NL_SELECTEDFOREGROUND, DEFAULT_SELECTEDFOREGROUND, false);
	dict.put(NL_LABELS, YoixObject.newArray(labelspercell), false);
	dict.put(NL_MARKS, NULL_ARRAY, false);
	dict.put(NL_FLAGS, YoixObject.newInt(flags), false);
	dict.put(NL_INDEX, YoixObject.newInt((int)(((int)row)*columns + column)), false);
	dict.put(NL_LAYER, YoixObject.newInt(-1), false);
	dict.put(NL_SELECTABLE, YoixObject.newInt(0), false);
	dict.put(NL_STATE, YoixObject.newInt(0), false);
	dict.put(NL_NEXTLABEL, YoixObject.newInt(0), false);

	dict.put(NL_NEXT, NULL_DICTIONARY, false);
	dict.put(NL_PREV, NULL_DICTIONARY, false);

	dict.put(NL_HOME, YoixObject.newPoint(column, row), false);

	dict.put(NL_TIP, NULL_STRING, false);
	dict.put(NL_ID, YoixObject.newInt(cellcounter++), false);
	dict.put(NL_ROW, YoixObject.newInt((int)row), false);
	dict.put(NL_COLUMN, YoixObject.newInt((int)column), false);

	return(dict);
    }


    public static ArrayList
    getBadNames() {

	return(getBadNames(VALIDATION_DATA));
    }


    public static synchronized ArrayList
    getBadNames(String data[]) {

	YoixObject  obj;
	ArrayList   badnames;
	String      name;
	String      type;
	int         n;

	badnames = new ArrayList();
	for (n = 0; n < data.length; n += 2) {
	    if ((name = data[n]) != null) {
		if ((obj = YoixInterpreter.getObject(name)) != null) {
		    if ((type = data[n+1]) != null) {
			if (type == T_NUMBER) {
			    if (obj.notNumber())
				badnames.add(name);
			} else if (type == T_CALLABLE) {
			    if (obj.notCallable() && obj.notNull())
				badnames.add(name);
			} else if (type == T_FUNCTION) {
			    if (obj.notFunction() && obj.notNull())
				badnames.add(name);
			} else if (type == T_STRING) {
			    if (obj.notString() && obj.notNull())
				badnames.add(name);
			}
		    }
		} else badnames.add(name);
	    }
	}
	return(badnames);
    }


    public static double
    getColumnAt(double x) {

	YoixObject  chartgrid;
	double      columnwidth;

	chartgrid = YoixInterpreter.getObject(NL_CHARTGRID);
	columnwidth = YoixInterpreter.getDouble(NL_COLUMNWIDTH, 0);
	return((x - chartgrid.getDouble(N_X, 0))/columnwidth);
    }


    public static YoixObject
    getNullArray(int length) {

	YoixObject  obj;
	int         n;

	obj = YoixObject.newArray(length);
	for (n = 0; n < length; n++)
	    obj.put(n, NULL_OBJECT, false);
	return(obj);
    }


    public static int
    indexOfObject(YoixObject source, YoixObject target) {

	int  length;
	int  index = -1;
	int  n;

	if (source != null && target != null) {
	    length = source.length();
	    for (n = source.offset(); n < length; n++) {
		if (source.defined(n)) {
		    if (YoixInterpreter.isEqualEQEQ(source.get(n, false), target)) {
			index = n;
			break;
		    }
		}
	    }
	}
	return(index);
    }


    public static YoixObject
    unrollObject(YoixObject obj) {

	YoixObject  element;
	ArrayList   dest;
	int         length;
	int         n;

	//
	// Essentially a copy of YoixMisc.unrollObject(), which currently isn't
	// public. Probably no good reason why it isn't but didn't want to make
	// the change right now.
	//

	dest = new ArrayList();
	length = obj.length();

	for (n = obj.offset(); n < length; n++) {
	    if ((element = obj.getObject(n)) != null)
		dest.add(element);
	}

	length = dest.size();
	obj = YoixObject.newArray(length);
	for (n = 0; n < length; n++)
	    obj.put(n, (YoixObject)dest.get(n), true);
	return(obj);
    }
}

