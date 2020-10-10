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
import att.research.yoix.*;

public abstract
class Schedule

    implements Constants

{

    //
    // Custom constants that currently are the names of variables defined
    // in the Yoix script that we occasionally need. Not all of them are
    // currently used.
    //

    private static final String  NL_LOCATIONCOUNT = "LocationCount";
    private static final String  NL_MAXDAY = "MAXDAY";
    private static final String  NL_MINDAY = "MINDAY";
    private static final String  NL_DAYSPAN = "DAYSPAN";
    private static final String  NL_MINUTESPERCELL = "minutespercell";
    private static final String  NL_SCHEDULE = "Schedule";
    private static final String  NL_TOTALMINUTES = "TotalMinutes";
    private static final String  NL_HALFMAP = "halfmap";

    private static final String  NL_COLORBY_COLLEGE = "COLORBY_COLLEGE";
    private static final String  NL_COLORBY_DAY = "COLORBY_DAY";
    private static final String  NL_COLORBY_DEPARTMENT = "COLORBY_DEPARTMENT";
    private static final String  NL_COLORBY_INSTRUCTOR = "COLORBY_INSTRUCTOR";

    private static final String  NL_LOCATION_COLUMNS = "LOCATION_COLUMNS";
    private static final String  NL_LOCATION_NAME = "LOCATION_NAME";
    private static final String  NL_LOCATION_DESCRIPTION = "LOCATION_DESCRIPTION";
    private static final String  NL_LOCATION_COLLEGE = "LOCATION_COLLEGE";
    private static final String  NL_LOCATION_ROOM = "LOCATION_ROOM";

    private static final String  NL_COURSE_COLUMNS = "COURSE_COLUMNS";
    private static final String  NL_COURSE_URL = "COURSE_URL";
    private static final String  NL_COURSE_ID = "COURSE_ID";
    private static final String  NL_COURSE_NAME = "COURSE_NAME";
    private static final String  NL_COURSE_INSTRUCTOR = "COURSE_INSTRUCTOR";
    private static final String  NL_COURSE_MISC = "COURSE_MISC";
    private static final String  NL_COURSE_SCHEDULE = "COURSE_SCHEDULE";
    private static final String  NL_COURSE_LOCATION = "COURSE_LOCATION";
    private static final String  NL_COURSE_COLLEGE = "COURSE_COLLEGE";
    private static final String  NL_COURSE_DEPARTMENT = "COURSE_DEPARTMENT";

    private static final String  NL_CLASS_COLUMNS = "CLASS_COLUMNS";
    private static final String  NL_CLASS_FIRSTNAME = "CLASS_FIRSTNAME";
    private static final String  NL_CLASS_LASTNAME = "CLASS_LASTNAME";
    private static final String  NL_CLASS_ROW = "CLASS_ROW";
    private static final String  NL_CLASS_MINUTE = "CLASS_MINUTE";
    private static final String  NL_CLASS_DURATION = "CLASS_DURATION";
    private static final String  NL_CLASS_START = "CLASS_START";
    private static final String  NL_CLASS_STOP = "CLASS_STOP";
    private static final String  NL_CLASS_WEEKDAY = "CLASS_WEEKDAY";
    private static final String  NL_CLASS_DAYSLOT = "CLASS_DAYSLOT";
    private static final String  NL_CLASS_TIMESLOT = "CLASS_TIMESLOT";
    private static final String  NL_CLASS_LOCATION_DATA = "CLASS_LOCATION_DATA";
    private static final String  NL_CLASS_COURSE_DATA = "CLASS_COURSE_DATA";

    private static final String  NL_LOADEDDAYS = "loadeddays";
    private static final String  NL_LOADEDLOCATIONS = "loadedlocations";
    private static final String  NL_LOADEDTIMES = "loadedtimes";

    private static final String  NL_GET_NEXT_DAY = "GET_NEXT_DAY";
    private static final String  NL_GET_NEXT_LOCATION = "GET_NEXT_LOCATION";
    private static final String  NL_GET_NEXT_TIME = "GET_NEXT_TIME";
    private static final String  NL_DIRECTION = "direction";
    private static final String  NL_FLAGS = "flags";
    private static final String  NL_MODE = "mode";
    private static final String  NL_QUICKLOOK = "quicklook";

    ///////////////////////////////////
    //
    // Schedule Methods
    //
    ///////////////////////////////////

    public static YoixObject
    getCellBackground(YoixObject cell, YoixObject dict) {

	YoixObject  colors;
	YoixObject  color = null;
	YoixObject  data;
	YoixObject  course_data;
	String      text;
	String      key;
	int         class_course_data;
	int         columns;
	int         index;

	if ((colors = dict.getObject(NL_COLORS)) != null) {
	    if ((text = dict.getString(NL_TEXT)) != null) {
		if ((data = cell.getObject(NL_DATA)) != null && data.isArray() && data.sizeof() > 2) {
		    class_course_data = YoixInterpreter.getInt(NL_CLASS_COURSE_DATA, 0);
		    course_data = data.getObject(data.offset() + class_course_data).getObject(0);
		    if (text.equals(YoixInterpreter.getString(NL_COLORBY_COLLEGE))) {
			index = YoixInterpreter.getInt(NL_COURSE_COLLEGE, 0);
			key = course_data.getString(course_data.offset() + index);
			if (colors.defined(key))
			    color = colors.getObject(key);
		    } else if (text.equals(YoixInterpreter.getString(NL_COLORBY_DAY))) {
			index = YoixInterpreter.getInt(NL_CLASS_WEEKDAY, 0);
			key = data.getString(data.offset() + index);
			if (colors.defined(key))
			    color = colors.getObject(key);
		    } else if (text.equals(YoixInterpreter.getString(NL_COLORBY_DEPARTMENT))) {
			index = YoixInterpreter.getInt(NL_COURSE_DEPARTMENT, 0);
			key = course_data.getString(course_data.offset() + index);
			if (colors.defined(key))
			    color = colors.getObject(key);
		    } else if (text.equals(YoixInterpreter.getString(NL_COLORBY_INSTRUCTOR))) {
			index = YoixInterpreter.getInt(NL_COURSE_INSTRUCTOR, 0);
			key = course_data.getString(course_data.offset() + index);
			if (colors.defined(key))
			    color = colors.getObject(key);
		    } else if (text.equals(YoixInterpreter.getString(NL_COLORBY_CHECKERBOARD))) {
			if (colors.isArray() && colors.sizeof() >= 2) {
			    if ((columns = YoixInterpreter.getInt(NL_COLUMNS, 0)) > 0) {
				if ((index = cell.getInt(NL_INDEX, 0)) >= 0) {
				    if ((color = colors.getObject(((index/columns)%2 + index%columns)%colors.sizeof())) != null) {
					if (color.notColor())
					    color = null;
				    }
				}
			    }
			}
		    } else if (colors.isColor())
			color = colors;
		}
	    }
	}
	return(color == null || color.notColor() ? null : color);
    }


    public static boolean
    getSelectionAsCell(YoixObject iptr, YoixObject ylocation, YoixObject yday, YoixObject ytime) {

	boolean result = false;

	result = handleGetSelectionAsCell(iptr, ylocation, yday, ytime);
	return(result);
    }


    public static boolean
    loadChart() {

	boolean result = false;

	handleLoadChart();
	result = true;
	return(result);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static int
    checkDept(int index, YoixObject map, String dept) {

	YoixObject  cell;
	YoixObject  data;
	YoixObject  course_data;
	YoixObject  info;
	boolean     found = false;
	int         class_course_data;
	int         course_department;
	int         length;
	int         n;

	if (index >= 0 && dept != null) {
	    if ((cell = map.getObject(index)) != null && cell.notNull()) {
		if ((data = cell.getObject(NL_DATA)) != null && data.isArray() && data.sizeof() > 2) {
		    course_department = YoixInterpreter.getInt(NL_COURSE_DEPARTMENT, 0);
		    class_course_data = YoixInterpreter.getInt(NL_CLASS_COURSE_DATA, 0);
		    course_data = data.getObject(data.offset() + class_course_data).getObject(0);
		    length = course_data.length();
		    for (n = course_data.offset(); n < length; n++) {
			if ((info = course_data.getObject()) != null) {
			    if (dept.equals(info.getString(info.offset() + course_department))) {
				found = true;
				break;
			    }
			}
		    }
		}
	    }
	}

	return(found ? index : -1);
    }


    private static boolean
    handleGetSelectionAsCell(YoixObject iptr, YoixObject ylocation, YoixObject yday, YoixObject ytime) {
	YoixObject cell;
	String     location = null;
	String     day = null;
	String     time = null;
	String     name;
	boolean    result = false;
	int[]      selection;
	int        index = -1;
	int        flags;
	int        l0, ls, lf, lr;
	int        m0, ms, mf, mr;
	int        n0, ns, nf, nr;
	boolean    firstpass = true;

	YoixObject loadedlocations = YoixInterpreter.getObject(NL_LOADEDLOCATIONS);
	YoixObject loadeddays = YoixInterpreter.getObject(NL_LOADEDDAYS);
	YoixObject loadedtimes = YoixInterpreter.getObject(NL_LOADEDTIMES);
	int        CELL_VISIBLE = YoixInterpreter.getInt(NL_CELL_VISIBLE, -1);
	int        GET_NEXT_LOCATION = YoixInterpreter.getInt(NL_GET_NEXT_LOCATION, -1);
	int        GET_NEXT_DAY = YoixInterpreter.getInt(NL_GET_NEXT_DAY, -1);
	int        GET_NEXT_TIME = YoixInterpreter.getInt(NL_GET_NEXT_TIME, -1);
	int        direction = YoixInterpreter.getInt(NL_DIRECTION, 0);
	int        mode = YoixInterpreter.getInt(NL_MODE, GET_NEXT_TIME);
	boolean    quicklook = YoixInterpreter.getBoolean(NL_QUICKLOOK, true);
	YoixObject map = direction == 0 ? YoixInterpreter.getObject(NL_NAMEMAP) : YoixInterpreter.getObject(NL_HALFMAP);

	location = ylocation.stringValue();
	day = yday.stringValue();
	time = ytime.stringValue();

	if (
	    map.isDictionary() && map.sizeof() > 0 &&
	    loadedlocations.isArray() && (ls = loadedlocations.sizeof()) > 0 &&
	    loadeddays.isArray() && (ms = loadeddays.sizeof()) > 0 &&
	    loadedtimes.isArray() && (ns = loadedtimes.sizeof()) > 0 &&
	    CELL_VISIBLE >= 0 &&
	    GET_NEXT_LOCATION >= 0 &&
	    GET_NEXT_DAY >= 0 &&
	    GET_NEXT_TIME >= 0
	    ) {
	    result = true;

	    if (location.length() > 0 && day.length() > 0 && time.length() > 0) {
		if (quicklook) {
		    name = location + "|" + day + "|" + time;
		    index = map.defined(index = map.hash(name)) ? index : -1;
		    if (index >= 0 && (cell = map.get(index, false)) != null && cell.notNull()) {
			flags = cell.getInt(NL_FLAGS, 0);
			if ((flags&CELL_VISIBLE) != CELL_VISIBLE)
			    index = -1;
		    }
		}

		if (index < 0) {
		    lf = lr = indexOfString(loadedlocations, location);
		    if (lf < 0)
			lf = lr = 0;
		    mf = mr = indexOfString(loadeddays, day);
		    if (mf < 0)
			mf = mr = 0;
		    nf = nr = indexOfString(loadedtimes, time);
		    if (nf < 0)
			nf = nr = 0;
		    if (mode == GET_NEXT_TIME) {
			while (index < 0) {
			    if (++nf >= ns) {
				nf = 0;
				if (++mf >= ms) {
				    mf = 0;
				    if (++lf >= ls) {
					lf = 0;
				    }
				}
			    }
			    if (--nr < 0) {
				nr = ns - 1;
				if (--mr < 0) {
				    mr = ms - 1;
				    if (--lr < 0) {
					lr = ls - 1;
				    }
				}
			    }
			    if (firstpass)
				firstpass = false;
			    else if (lf == lr && mf == mr && nf == nr)
				break;
			    if (direction >= 0 && index < 0) {
				name = loadedlocations.getString(lf) + "|" + loadeddays.getString(mf) + "|" + loadedtimes.getString(nf);
				index = map.defined(index = map.hash(name)) ? index : -1;
				if (index >= 0 && (cell = map.get(index, false)) != null && cell.notNull()) {
				    flags = cell.getInt(NL_FLAGS, 0);
				    if ((flags&CELL_VISIBLE) != CELL_VISIBLE)
					index = -1;
				}
			    }
			    if (direction <= 0 && index < 0) {
				name = loadedlocations.getString(lr) + "|" + loadeddays.getString(mr) + "|" + loadedtimes.getString(nr);
				index = map.defined(index = map.hash(name)) ? index : -1;
				if (index >= 0 && (cell = map.get(index, false)) != null && cell.notNull()) {
				    flags = cell.getInt(NL_FLAGS, 0);
				    if ((flags&CELL_VISIBLE) != CELL_VISIBLE)
					index = -1;
				}
			    }
			}
		    } else if (mode == GET_NEXT_LOCATION) {
			while (index < 0) {
			    if (++lf >= ls) {
				lf = 0;
				if (++nf >= ns) {
				    nf = 0;
				    if (++mf >= ms) {
					mf = 0;
				    }
				}
			    }
			    if (--lr < 0) {
				lr = ls - 1;
				if (--nr < 0) {
				    nr = ns - 1;
				    if (--mr < 0) {
					mr = ms - 1;
				    }
				}
			    }
			    if (firstpass)
				firstpass = false;
			    else if (lf == lr && mf == mr && nf == nr)
				break;
			    if (direction >= 0 && index < 0) {
				name = loadedlocations.getString(lf) + "|" + loadeddays.getString(mf) + "|" + loadedtimes.getString(nf);
				index = map.defined(index = map.hash(name)) ? index : -1;
				if (index >= 0 && (cell = map.get(index, false)) != null && cell.notNull()) {
				    flags = cell.getInt(NL_FLAGS, 0);
				    if ((flags&CELL_VISIBLE) != CELL_VISIBLE)
					index = -1;
				}
			    }
			    if (direction <= 0 && index < 0) {
				name = loadedlocations.getString(lr) + "|" + loadeddays.getString(mr) + "|" + loadedtimes.getString(nr);
				index = map.defined(index = map.hash(name)) ? index : -1;
				if (index >= 0 && (cell = map.get(index, false)) != null && cell.notNull()) {
				    flags = cell.getInt(NL_FLAGS, 0);
				    if ((flags&CELL_VISIBLE) != CELL_VISIBLE)
					index = -1;
				}
			    }
			}
		    } else if (mode == GET_NEXT_DAY) {
			while (index < 0) {
			    if (++mf >= ms) {
				mf = 0;
				if (++nf >= ns) {
				    nf = 0;
				    if (++lf >= ls) {
					lf = 0;
				    }
				}
			    }
			    if (--mr < 0) {
				mr = ms - 1;
				if (--nr < 0) {
				    nr = ns - 1;
				    if (--lr < 0) {
					lr = ls - 1;
				    }
				}
			    }
			    if (firstpass)
				firstpass = false;
			    else if (lf == lr && mf == mr && nf == nr)
				break;
			    if (direction >= 0 && index < 0) {
				name = loadedlocations.getString(lf) + "|" + loadeddays.getString(mf) + "|" + loadedtimes.getString(nf);
				index = map.defined(index = map.hash(name)) ? index : -1;
				if (index >= 0 && (cell = map.get(index, false)) != null && cell.notNull()) {
				    flags = cell.getInt(NL_FLAGS, 0);
				    if ((flags&CELL_VISIBLE) != CELL_VISIBLE)
					index = -1;
				}
			    }
			    if (direction <= 0 && index < 0) {
				name = loadedlocations.getString(lr) + "|" + loadeddays.getString(mr) + "|" + loadedtimes.getString(nr);
				index = map.defined(index = map.hash(name)) ? index : -1;
				if (index >= 0 && (cell = map.get(index, false)) != null && cell.notNull()) {
				    flags = cell.getInt(NL_FLAGS, 0);
				    if ((flags&CELL_VISIBLE) != CELL_VISIBLE)
					index = -1;
				}
			    }
			}
		    }
		}
	    }
	    iptr.put(YoixObject.newInt(index));
	}

	return(result);
    }

    private static void
    handleLoadChart() {

	YoixObject  ends;
	YoixObject  chartcells;
	YoixObject  rowends;
	YoixObject  columnends;
	YoixObject  namemap;
	YoixObject  findmap;
	YoixObject  halfmap;
	YoixObject  allnames;
	YoixObject  allcells;
	YoixObject  schedule;
	YoixObject  course_data;
	YoixObject  location_data;
	YoixObject  cell;
	YoixObject  cellname;
	YoixObject  screen;
	boolean     simplecellmodel;
	String      firstname;
	String      lastname;
	String      str;
	double      totalminutes;
	double      minutespercell;
	double      cellaspect;
	double      minute;
	double      width;
	double      row;
	double      column;
	int         locationcount;
	int         classcount;
	int         minday;
	int         maxday;
	int         dayspan;
	int         location_columns;
	int         location_name;
	int         location_description;
	int         location_college;
	int         location_room;
	int         course_columns;
	int         course_url;
	int         course_id;
	int         course_name;
	int         course_instructor;
	int         course_misc;
	int         course_schedule;
	int         course_location;
	int         course_college;
	int         course_department;
	int         class_columns;
	int         class_firstname;
	int         class_lastname;
	int         class_row;
	int         class_minute;
	int         class_duration;
	int         class_start;
	int         class_stop;
	int         class_weekday;
	int         class_dayslot;
	int         class_timeslot;
	int         class_location_data;
	int         class_course_data;
	int         labelspercell;
	int         flags;
	int         rows;
	int         columns;
	int         index;
	int         nextname;
	int         nextcell;
	int         cellcount;

	totalminutes = YoixInterpreter.getDouble(NL_TOTALMINUTES, 0);
	minutespercell = YoixInterpreter.getDouble(NL_MINUTESPERCELL, 0);
	schedule = YoixInterpreter.getObject(NL_SCHEDULE);
	flags = YoixInterpreter.getInt(NL_CELL_VISIBLE, 0);
	labelspercell = YoixInterpreter.getInt(NL_LABELSPERCELL, 0);
	simplecellmodel = YoixInterpreter.getBoolean(NL_SIMPLECELLMODEL, true);
	locationcount = YoixInterpreter.getInt(NL_LOCATIONCOUNT, 0);
	minday = YoixInterpreter.getInt(NL_MINDAY, 0);
	maxday = YoixInterpreter.getInt(NL_MAXDAY, 0);
	dayspan = YoixInterpreter.getInt(NL_DAYSPAN, 0);

	location_columns = YoixInterpreter.getInt(NL_LOCATION_COLUMNS, 0);
	location_name = YoixInterpreter.getInt(NL_LOCATION_NAME, 0);
	location_description = YoixInterpreter.getInt(NL_LOCATION_DESCRIPTION, 0);
	location_college = YoixInterpreter.getInt(NL_LOCATION_COLLEGE, 0);
	location_room = YoixInterpreter.getInt(NL_LOCATION_ROOM, 0);

	course_columns = YoixInterpreter.getInt(NL_COURSE_COLUMNS, 0);
	course_url = YoixInterpreter.getInt(NL_COURSE_URL, 0);
	course_id = YoixInterpreter.getInt(NL_COURSE_ID, 0);
	course_name = YoixInterpreter.getInt(NL_COURSE_NAME, 0);
	course_instructor = YoixInterpreter.getInt(NL_COURSE_INSTRUCTOR, 0);
	course_misc = YoixInterpreter.getInt(NL_COURSE_MISC, 0);
	course_schedule = YoixInterpreter.getInt(NL_COURSE_SCHEDULE, 0);
	course_location = YoixInterpreter.getInt(NL_COURSE_LOCATION, 0);
	course_college = YoixInterpreter.getInt(NL_COURSE_COLLEGE, 0);
	course_department = YoixInterpreter.getInt(NL_COURSE_DEPARTMENT, 0);

	class_columns = YoixInterpreter.getInt(NL_CLASS_COLUMNS, 0);
	class_firstname = YoixInterpreter.getInt(NL_CLASS_FIRSTNAME, 0);
	class_lastname = YoixInterpreter.getInt(NL_CLASS_LASTNAME, 0);
	class_row = YoixInterpreter.getInt(NL_CLASS_ROW, 0);
	class_minute = YoixInterpreter.getInt(NL_CLASS_MINUTE, 0);
	class_duration = YoixInterpreter.getInt(NL_CLASS_DURATION, 0);
	class_start = YoixInterpreter.getInt(NL_CLASS_START, 0);
	class_stop = YoixInterpreter.getInt(NL_CLASS_STOP, 0);
	class_weekday = YoixInterpreter.getInt(NL_CLASS_WEEKDAY, 0);
	class_dayslot = YoixInterpreter.getInt(NL_CLASS_DAYSLOT, 0);
	class_timeslot = YoixInterpreter.getInt(NL_CLASS_TIMESLOT, 0);
	class_location_data = YoixInterpreter.getInt(NL_CLASS_LOCATION_DATA, 0);
	class_course_data = YoixInterpreter.getInt(NL_CLASS_COURSE_DATA, 0);

	rows = locationcount;
	columns = (int)(((maxday - minday + 1)*dayspan)/minutespercell);
	classcount = schedule.sizeof()/class_columns;
	chartcells = Misc.getNullArray(rows*columns);

	screen = YoixInterpreter.getObject(N_VM).getObject(N_SCREEN);
	cellaspect = (10*(screen.getDouble(N_WIDTH, 0) - 2.5*72))/((180/minutespercell)*(screen.getDouble(N_HEIGHT, 0) - 4.5*72));

	namemap = YoixObject.newDictionary(4*classcount);
	findmap = YoixObject.newDictionary(2*classcount);
	halfmap = YoixObject.newDictionary(2*classcount);
	allnames = YoixObject.newArray(classcount);
	allcells = YoixObject.newArray(classcount);
	nextname = 0;
	nextcell = 0;

	cellcount = 0;

	for (index = 0; index <= schedule.sizeof() - class_columns; index += class_columns) {
	    row = schedule.getInt(index + class_row, 0);
	    width = schedule.getInt(index + class_duration, 0);
	    minute = schedule.getInt(index + class_timeslot, 0);
	    if (minute < 0) {
		column = 0;
		width += minute;
	    } else column = minute/minutespercell;
	    if (width + minute > totalminutes)
		width = totalminutes - minute;

	    firstname = schedule.getString(index + class_firstname);
	    lastname = schedule.getString(index + class_lastname);
	    cellname = YoixObject.newString(firstname);
	    cell = Misc.createChartCell(
		cellname,
		row, column,
		width/minutespercell, 1.0, 
		YoixObject.newLvalue(schedule, index),
		NULL_OBJECT,
		columns,
		flags,
		labelspercell
	    );
	    cellcount++;
	    Misc.addCellToChart(cell, chartcells, simplecellmodel);

	    course_data = schedule.getObject(index + class_course_data);
	    cell.putString(NL_TIP, uniqMergeArrayInfo(course_data, course_name, "; "));

	    location_data = schedule.getObject(index + class_location_data);

	    namemap.put(firstname, cell, false);
	    namemap.put(lastname, cell, false);
	    halfmap.put(firstname, cell, false);

	    str = uniqMergeArrayInfo(course_data, course_name, "|") + "+" +
		uniqMergeArrayInfo(course_data, course_instructor, "|") + "+" +
		schedule.getString(index + class_weekday) + "+" +
		schedule.getString(index + class_start) + "-" +
		schedule.getString(index + class_stop) + "+" +
		uniqMergeArrayInfo(course_data, course_id, "|") + "+" +
		uniqMergeArrayInfo(course_data, course_department, "|") + "+" +
		location_data.getString(location_data.offset() + location_college) + "+" +
		location_data.getString(location_data.offset() + location_name);
	    findmap.putString(firstname, str);
	    allnames.put(nextname++, cellname, false);
	    allcells.put(nextcell++, cell, false);
	}

	rowends = Misc.getNullArray(rows);
	ends = YoixObject.newArray(2);
	ends.putInt(0, 0);
	ends.putInt(1, columns - 1);
	for (index = 0; index < rows; index++)
	    rowends.putObject(index, ends);

	columnends = Misc.getNullArray(columns);
	ends = YoixObject.newArray(2);
	ends.putInt(0, 0);
	ends.putInt(1, rows - 1);
	for (index = 0; index < columns; index++)
	    columnends.putObject(index, ends);

	//
	// Store everything that we just built.
	//

	YoixInterpreter.putInt(NL_ROWS, rows);
	YoixInterpreter.putInt(NL_COLUMNS, columns);
	YoixInterpreter.putInt(NL_CELLCOUNT, cellcount);
	YoixInterpreter.putDouble(NL_CELLASPECT, cellaspect);
	YoixInterpreter.putObject(NL_ROWENDS, rowends);
	YoixInterpreter.putObject(NL_COLUMNENDS, columnends);
	YoixInterpreter.putObject(NL_NAMEMAP, namemap);
	YoixInterpreter.putObject(NL_FINDMAP, findmap);
	YoixInterpreter.putObject(NL_HALFMAP, halfmap);
	YoixInterpreter.putObject(NL_ALLNAMES, allnames);
	YoixInterpreter.putObject(NL_ALLCELLS, allcells);
	YoixInterpreter.putInt(NL_VALIDATECELLS, false);
	YoixInterpreter.putObject(NL_CHARTCELLS, chartcells);
    }

    private static int
    indexOfString(YoixObject source, String target) {

	String     str;
	int        index = -1;
	int        length = source.length();

	for (int n = source.offset(); n < length; n++) {
	    if ((str = source.getString(n)) != null) {
		if (str.equals(target)) {
		    index = n;
		    break;
		}
	    }
	}

	return(index);
    }

    private static String
    uniqMergeArrayInfo(YoixObject array, int index, String delim) {
	YoixObject   obj;
	YoixObject   obj2;
	StringBuffer sb;
	String[]     strs;
	boolean      repeat;
	int          m;
	int          n;
	int          sz = array.sizeof();
	int          off = array.offset();
	int          total = 0;
	int          extra;

	if (delim == null)
	    delim = "";

	extra = delim.length();

	strs = new String[sz];

	for (n = 0; n < sz; n++) {
	    obj = array.getObject(n);
	    obj2 = obj.getObject(obj.offset() + index);
	    strs[n] = obj2.isString() ? obj2.stringValue() : obj2.toString();
	}

	java.util.Arrays.sort(strs);

	for (n = 1; n < sz; n++) {
	    if (strs[n] != null && strs[n].equals(strs[n-1]))
		strs[n-1] = null;
	}

	for (m = 0, n = 0; n < sz; n++) {
	    if (strs[n] != null) 
		total += strs[n].length() + (m++ > 0 ? extra : 0);
	}

	sb = new StringBuffer(total);
	for (m = 0, n = 0; n < sz; n++) {
	    if (strs[n] != null) {
		if (m++ > 0 && extra > 0)
		    sb.append(delim);
		sb.append(strs[n]);
	    }
	}
	return(sb.toString());
    }
}

