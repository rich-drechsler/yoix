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
class Guide

    implements Constants

{

    //
    // Custom constants.
    //

    private static final String  NL_CHANNELCOUNT = "ChannelCount";
    private static final String  NL_TOTALMINUTES = "TotalMinutes";
    private static final String  NL_MINUTESPERCELL = "minutespercell";
    private static final String  NL_STARTDAY = "StartDay";
    private static final String  NL_SCHEDULE = "Schedule";
    private static final String  NL_LOADEDCHANNELS = "loadedchannels";
    private static final String  NL_CELLSBYCHANNEL = "cellsbychannel";
    private static final String  NL_CHANNELENDS = "channelends";
    private static final String  NL_TIMESLICES = "timeslices";

    private static final String  NL_COLORBY_TYPE = "COLORBY_TYPE";
    private static final String  NL_COLORBY_MPAA = "COLORBY_MPAA";
    private static final String  NL_COLORBY_MOVIE_TYPE = "COLORBY_MOVIE_TYPE";

    private static final String  NL_EVENT_COLUMNS = "EVENT_COLUMNS";
    private static final String  NL_EVENT_ROW = "EVENT_ROW";
    private static final String  NL_EVENT_DURATION = "EVENT_DURATION";
    private static final String  NL_EVENT_MINUTE = "EVENT_MINUTE";
    private static final String  NL_EVENT_STARTDAY = "EVENT_STARTDAY";
    private static final String  NL_EVENT_FIRSTNAME = "EVENT_FIRSTNAME";
    private static final String  NL_EVENT_LASTNAME = "EVENT_LASTNAME";
    private static final String  NL_EVENT_CHANNEL_NUMBER = "EVENT_CHANNEL_NUMBER";
    private static final String  NL_EVENT_PROGRAM_DATA = "EVENT_PROGRAM_DATA";
    private static final String  NL_EVENT_CELLBYCHANNEL_INDEX = "EVENT_CELLBYCHANNEL_INDEX";

    private static final String  NL_PROGRAM_TITLE = "PROGRAM_TITLE";
    private static final String  NL_PROGRAM_CATEGORY = "PROGRAM_CATEGORY";
    private static final String  NL_PROGRAM_SUBCATEGORY = "PROGRAM_SUBCATEGORY";
    private static final String  NL_PROGRAM_MPAA = "PROGRAM_MPAA";

    ///////////////////////////////////
    //
    // Guide Methods
    //
    ///////////////////////////////////

    public static YoixObject
    getCellBackground(YoixObject cell, YoixObject dict) {

	YoixObject  obj;
	YoixObject  colors;
	YoixObject  color = null;
	YoixObject  data;
	YoixObject  pdata;
	YoixObject  category;
	YoixObject  subcategory;
	YoixObject  mpaa;
	String      text;
	String      name;
	int         event_program_data;
	int         program_category;
	int         program_subcategory;
	int         program_mpaa;
	int         columns;
	int         index;

	if ((colors = dict.getObject(NL_COLORS)) != null) {
	    if ((text = dict.getString(NL_TEXT)) != null) {
		if ((data = cell.getObject(NL_DATA)) != null && data.isArray() && data.sizeof() > 2) {
		    if (text.equals(YoixInterpreter.getString(NL_COLORBY_TYPE))) {
			event_program_data = YoixInterpreter.getInt(NL_EVENT_PROGRAM_DATA, 0);
			program_category = YoixInterpreter.getInt(NL_PROGRAM_CATEGORY, 0);
			program_subcategory = YoixInterpreter.getInt(NL_PROGRAM_SUBCATEGORY, 0);
			if ((pdata = data.getObject(data.offset() + event_program_data)) != null) {
			    category = pdata.getObject(pdata.offset() + program_category);
			    subcategory = pdata.getObject(pdata.offset() + program_subcategory);
			    name = category.stringValue() + ":" + subcategory.stringValue();
			    if (colors.defined(name) == false) {
				name = category.stringValue();
				if (colors.defined(name))
				    color = colors.getObject(name);
				else color = colors.getObject("*");
			    } else color = colors.getObject(name);
			}
		    } else if (text.equals(YoixInterpreter.getString(NL_COLORBY_MOVIE_TYPE))) {
			event_program_data = YoixInterpreter.getInt(NL_EVENT_PROGRAM_DATA, 0);
			program_category = YoixInterpreter.getInt(NL_PROGRAM_CATEGORY, 0);
			program_subcategory = YoixInterpreter.getInt(NL_PROGRAM_SUBCATEGORY, 0);
			if ((pdata = data.getObject(data.offset() + event_program_data)) != null) {
			    category = pdata.getObject(pdata.offset() + program_category);
			    if (category.stringValue().equals("Movie")) {
				subcategory = pdata.getObject(pdata.offset() + program_subcategory);
				name = subcategory.stringValue();
				if (name.length() > 0 && colors.defined(name))
				    color = colors.getObject(name);
				else color = colors.getObject("*");
			    } else color = colors.getObject("");
			}
		    } else if (text.equals(YoixInterpreter.getString(NL_COLORBY_MPAA))) {
			event_program_data = YoixInterpreter.getInt(NL_EVENT_PROGRAM_DATA, 0);
			program_category = YoixInterpreter.getInt(NL_PROGRAM_CATEGORY, 0);
			program_mpaa = YoixInterpreter.getInt(NL_PROGRAM_MPAA, 0);
			if ((pdata = data.getObject(data.offset() + event_program_data)) != null) {
			    category = pdata.getObject(pdata.offset() + program_category);
			    if (category.stringValue().equals("Movie")) {
				mpaa = pdata.getObject(pdata.offset() + program_mpaa);
				name = mpaa.stringValue();
				if (name.length() > 0 && colors.defined(name))
				    color = colors.getObject(name);
				else color = colors.getObject("*");
			    } else color = colors.getObject("");
			}
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

    private static YoixObject
    buildTimeSlices(YoixObject allcells) {

	YoixObject  slice;
	YoixObject  slices;
	YoixObject  cell;
	YoixObject  cells;
	YoixObject  data;
	int         start;
	int         minute;
	int         event_minute;
	int         event_startday;
	int         totalminutes;
	int         length;
	int         index;

	//
	// An implicit assumption in the current implementation is that all
	// channels have some kind of entry in the first time slot. Wouldn't
	// be hard to address if that changes.
	//

	event_minute = YoixInterpreter.getInt(NL_EVENT_MINUTE, 0);
	event_startday = YoixInterpreter.getInt(NL_EVENT_STARTDAY, 0);
	totalminutes = YoixInterpreter.getInt(NL_TOTALMINUTES, 0);

	start = 0;
	slice = newSlice(null, start, totalminutes, YoixMake.javaInt(YoixInterpreter.getString(NL_STARTDAY)));
	cells = slice.getObject("cells");
	slices = YoixObject.newArray(0, -1);
	slices.putObject(slices.sizeof(), slice);

	length = allcells.length();
	for (index = 0; index < length; index++) {
	    if ((cell = allcells.getObject(index)) != null) {
		data = cell.getObject(NL_DATA);
		minute = data.getInt(data.offset() + event_minute, 0);
		if (minute > start) {
		    slice.putInt("end", minute);
		    start = minute;
		    slice = newSlice(cells, start, totalminutes, data.getInt(data.offset() + event_startday, 0));
		    cells = slice.getObject("cells");
		    slices.putObject(slices.sizeof(), slice);
		}
		cells.put(cell.getInt(NL_ROW, 0), cell, false);
	    }
	}
	return(slices);
    }


    private static void
    handleLoadChart() {

	YoixObject  ends;
	YoixObject  chartcells;
	YoixObject  rowends;
	YoixObject  columnends;
	YoixObject  namemap;
	YoixObject  findmap;
	YoixObject  allnames;
	YoixObject  allcells;
	YoixObject  loadedchannels;
	YoixObject  cellsbychannel;
	YoixObject  channelends;
	YoixObject  timeslices;
	YoixObject  schedule;
	YoixObject  data;
	YoixObject  cell;
	YoixObject  eventname;
	YoixObject  screen;
	boolean     simplecellmodel;
	String      firstname;
	String      lastname;
	String      currentchannel;
	String      channel;
	double      totalminutes;
	double      minutespercell;
	double      cellaspect;
	double      minute;
	double      width;
	double      row;
	double      column;
	int         event_columns;
	int         event_row;
	int         event_duration;
	int         event_minute;
	int         event_firstname;
	int         event_lastname;
	int         event_channel_number;
	int         event_program_data;
	int         program_title;
	int         event_cellbychannel_index;
	int         channelcount;
	int         eventcount;
	int         labelspercell;
	int         flags;
	int         rows;
	int         columns;
	int         index;
	int         nextname;
	int         nextcell;
	int         nextchannel;
	int         cellcount;
	int         limit;

	channelcount = YoixInterpreter.getInt(NL_CHANNELCOUNT, 0);
	totalminutes = YoixInterpreter.getDouble(NL_TOTALMINUTES, 0);
	minutespercell = YoixInterpreter.getDouble(NL_MINUTESPERCELL, 0);
	schedule = YoixInterpreter.getObject(NL_SCHEDULE);
	flags = YoixInterpreter.getInt(NL_CELL_VISIBLE, 0);
	labelspercell = YoixInterpreter.getInt(NL_LABELSPERCELL, 0);
	simplecellmodel = YoixInterpreter.getBoolean(NL_SIMPLECELLMODEL, true);

	event_columns = YoixInterpreter.getInt(NL_EVENT_COLUMNS, 0);
	event_row = YoixInterpreter.getInt(NL_EVENT_ROW, 0);
	event_duration = YoixInterpreter.getInt(NL_EVENT_DURATION, 0);
	event_minute = YoixInterpreter.getInt(NL_EVENT_MINUTE, 0);
	event_firstname = YoixInterpreter.getInt(NL_EVENT_FIRSTNAME, 0);
	event_lastname = YoixInterpreter.getInt(NL_EVENT_LASTNAME, 0);
	event_channel_number = YoixInterpreter.getInt(NL_EVENT_CHANNEL_NUMBER, 0);
	event_program_data = YoixInterpreter.getInt(NL_EVENT_PROGRAM_DATA, 0);
	event_cellbychannel_index = YoixInterpreter.getInt(NL_EVENT_CELLBYCHANNEL_INDEX, 0);
	program_title = YoixInterpreter.getInt(NL_PROGRAM_TITLE, 0);

	rows = channelcount;
	columns = (int)(totalminutes/minutespercell);
	eventcount = schedule.sizeof()/event_columns;
	chartcells = Misc.getNullArray(rows*columns);

	screen = YoixInterpreter.getObject(N_VM).getObject(N_SCREEN);
	cellaspect = (10*(screen.getDouble(N_WIDTH, 0) - 2.5*72))/((180/minutespercell)*(screen.getDouble(N_HEIGHT, 0) - 4.5*72));

	namemap = YoixObject.newDictionary(4*eventcount);
	findmap = YoixObject.newDictionary(2*eventcount);
	allnames = YoixObject.newArray(eventcount);
	allcells = YoixObject.newArray(eventcount);
	cellsbychannel = YoixObject.newArray(eventcount);
	nextname = 0;
	nextcell = 0;

	loadedchannels = YoixObject.newArray(channelcount, -1);
	nextchannel = 0;
	cellcount = 0;

	limit = schedule.sizeof() - event_columns;
	for (index = 0; index <= limit; index += event_columns) {
	    row = schedule.getInt(index + event_row, 0);
	    width = schedule.getInt(index + event_duration, 0);
	    minute = schedule.getInt(index + event_minute, 0);
	    if (minute < 0) {
		column = 0;
		width += minute;
	    } else column = minute/minutespercell;
	    if (width + minute > totalminutes)
		width = totalminutes - minute;
	    firstname = schedule.getString(index + event_firstname);
	    lastname = schedule.getString(index + event_lastname);
	    eventname = YoixObject.newString(firstname);
	    cell = Misc.createChartCell(
		eventname,
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
	    data = schedule.getObject(index + event_program_data);
	    cell.put(NL_TIP, data.getObject(data.offset() + program_title), false);
	    namemap.put(firstname, cell, false);
	    namemap.put(lastname, cell, false);
	    findmap.put(firstname, data.getObject(data.offset() + program_title), false);
	    allnames.put(nextname++, eventname, false);
	    allcells.put(nextcell++, cell, false);
	    cellsbychannel.put(schedule.getInt(index + event_cellbychannel_index, 0), cell, false);
	    if (column == 0)
		loadedchannels.putString(nextchannel++, schedule.getString(index + event_channel_number));
	}

	if (nextchannel < channelcount) {
	    //
	    // We do this because qsort currently misbehaves when the array
	    // it's sorting includes slots that haven't been initialized.
	    //
	    loadedchannels = Misc.unrollObject(loadedchannels);
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

	channelends = YoixObject.newDictionary(channelcount);
	ends = null;
	currentchannel = null;

	for (index = 0; index < eventcount; index++) {
	    if ((cell = cellsbychannel.getObject(index)) != null) {
		data = cell.getObject(NL_DATA);
		if ((channel = data.getString(data.offset() + event_channel_number)) != null) {
		    if (channel.equals(currentchannel) == false) {
			ends = YoixObject.newArray(2);
			ends.putInt(0, index);
			ends.putInt(1, index);
			channelends.putObject(channel, ends);
			currentchannel = channel;
		    } else ends.putInt(1, index);
		}
	    }
	}

	timeslices = buildTimeSlices(allcells);

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
	YoixInterpreter.putObject(NL_ALLNAMES, allnames);
	YoixInterpreter.putObject(NL_ALLCELLS, allcells);
	YoixInterpreter.putObject(NL_CELLSBYCHANNEL, cellsbychannel);
	YoixInterpreter.putObject(NL_CHANNELENDS, channelends);
	YoixInterpreter.putObject(NL_LOADEDCHANNELS, loadedchannels);
	YoixInterpreter.putObject(NL_TIMESLICES, timeslices);
	YoixInterpreter.putInt(NL_VALIDATECELLS, false);
	YoixInterpreter.putObject(NL_CHARTCELLS, chartcells);
    }


    private static YoixObject
    newSlice(YoixObject cells, int start, int end, int day) {

	YoixObject  slice = null;

	if (cells == null)
	    cells = YoixObject.newArray(YoixInterpreter.getInt(NL_CHANNELCOUNT, 0));
	else cells = YoixMisc.copyInto(cells, YoixObject.newArray(cells.length()));
	slice = YoixObject.newDictionary(4);
	slice.putObject("cells", cells);
	slice.putInt("start", start);
	slice.putInt("end", end);
	slice.putInt("day", day);
	return(slice);
    }
}

