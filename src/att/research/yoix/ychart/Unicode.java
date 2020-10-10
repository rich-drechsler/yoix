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
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import att.research.yoix.*;

public abstract
class Unicode

    implements Constants

{

    //
    // Custom constants.
    //

    private static final String  NL_BADCHAR = "badchar";
    private static final String  NL_BADCHAR_INFO = "badchar_info";
    private static final String  NL_COLORBY_CATEGORY = "COLORBY_CATEGORY";
    private static final String  NL_LOADEDBLOCKS = "loadedblocks";
    private static final String  NL_UNICODEBLOCKS = "UnicodeBlocks";
    private static final String  NL_UNICODEDATA = "UnicodeData";

    ///////////////////////////////////
    //
    // Unicode Methods
    //
    ///////////////////////////////////

    public static YoixObject
    getCellBackground(YoixObject cell, YoixObject dict) {

	YoixObject  obj;
	YoixObject  cellcolors;
	YoixObject  colors;
	YoixObject  color = null;
	YoixObject  data;
	String      text;
	String      key;
	int         columns;
	int         index;

	if ((colors = dict.getObject(NL_COLORS)) != null) {
	    if ((text = dict.getString(NL_TEXT)) != null) {
		if ((data = cell.getObject(NL_DATA)) != null && data.isArray() && data.sizeof() > 2) {
		    if (text.equals(YoixInterpreter.getString(NL_COLORBY_CATEGORY))) {
			if ((obj = data.getObject(data.offset() + 3)) != null) {
			    if (obj.notNull()) {
				if ((obj = obj.getObject(1)) != null && obj.isString())
				    color = colors.getObject(obj.stringValue());
			    } else if ((cellcolors = YoixInterpreter.getObject(NL_CELLCOLORS)) != null) {
				if ((obj = data.getObject(data.offset() + 2)) != null) {
				    if (obj.notNull())
					color = cellcolors.getObject(NL_MEDIUMLIGHTGRAY);
				    else color = cellcolors.getObject(NL_MEDIUMDARKGRAY);
				}
			    }
			}
		    } else if (text.equals(YoixInterpreter.getString(NL_COLORBY_BLOCK))) {
			if ((obj = data.getObject(data.offset() + 2)) != null) {
			    if (obj.notNull()) {
				if (obj.isString())
				    color = colors.getObject(obj.stringValue());
			    } else if ((cellcolors = YoixInterpreter.getObject(NL_CELLCOLORS)) != null) {
				if (obj.notNull())
				    color = cellcolors.getObject(NL_MEDIUMLIGHTGRAY);
				else color = cellcolors.getObject(NL_MEDIUMDARKGRAY);
			    }
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
    loadChart(YoixObject start, YoixObject end, YoixObject blockseparation) {

	boolean result = false;

	if (start.isInteger()) {
	    if (end.isInteger()) {
		if (blockseparation.isInteger()) {
		    handleLoadChart(start.intValue(), end.intValue(), Math.max(0, blockseparation.intValue()));
		    result = true;
		}
	    }
	}
	return(result);
    }


    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static HashMap
    getBadCharMap() {

	YoixObject  obj;
	YoixObject  element;
	HashMap     map = null;
	int         n;

	if ((obj = YoixInterpreter.getObject(NL_BADCHAR)) != null && obj.notNull()) {
	    if (obj.sizeof() > 0) {
		map = new HashMap();
		for (n = 0; n < obj.sizeof(); n += 2) {
		    if ((element = obj.getObject(n)) != null)
			map.put(getHexName(element.intValue()), obj.getString(n+1));
		}
	    }
	}
	return(map);
    }


    private static String
    getHexName(int code) {

	String  name = Integer.toString(code, 16).toUpperCase();

	if (code < 0x0010)
	    name = "000" + name;
	else if (code < 0x0100)
	    name = "00" + name;
	else if (code < 0x1000)
	    name = "0" + name;
	return(name);
    }


    private static void
    handleLoadChart(int start, int end, int blockseparation) {

	YoixObject  obj;
	YoixObject  chartcells;
	YoixObject  rowends;
	YoixObject  columnends;
	YoixObject  namemap;
	YoixObject  findmap;
	YoixObject  allcells;
	YoixObject  allnames;
	YoixObject  loadedblocks;
	YoixObject  blockmap;
	YoixObject  charname;
	YoixObject  data;
	YoixObject  chardata;
	YoixObject  cell;
	YoixObject  blockname;
	YoixObject  unicodeblocks;
	YoixObject  unicodedata;
	YoixObject  key;
	HashMap     badcharmap;
	String      name;
	int         labelspercell;
	int         flags;
	int         blockcount;
	int         blocksize;
	int         blockrows;
	int         blockcolumns;
	int         blockrow;
	int         blockcolumn;
	int         blockstart;
	int         blockend;
	int         blockindex;
	int         codecount;
	int         dataoffset;
	int         rowcount;
	int         columncount;
	int         first;
	int         last;
	int         rows;
	int         columns;
	int         row;
	int         column;
	int         code;
	int         index;
	int         nextname;
	int         nextcell;

	unicodeblocks = YoixInterpreter.getObject(NL_UNICODEBLOCKS);
	unicodedata = YoixInterpreter.getObject(NL_UNICODEDATA);
	flags = YoixInterpreter.getInt(NL_CELL_VISIBLE, 0);
	labelspercell = YoixInterpreter.getInt(NL_LABELSPERCELL, 0);
	blocksize = 256;
	first = (start/blocksize)*blocksize;
	last = ((end + blocksize)/blocksize)*blocksize - 1;
	blockcount = (last - first + blocksize)/blocksize;
	blockcolumns = (int)Math.ceil(Math.sqrt(blockcount));
	blockrows = (blockcount + blockcolumns - 1)/blockcolumns;

	rows = 16*blockrows + (blockrows - 1)*blockseparation;
	columns = 16*blockcolumns + (blockcolumns - 1)*blockseparation;
	chartcells = Misc.getNullArray(rows*columns);

	//
	// NOTE - making namemap bigger than required reduces collisions
	// which can add a seon 1sec to this method. Not 100% sure it's
	// OK - check tomorrow.
	//
	codecount = 256*blockcount;
	namemap = YoixObject.newDictionary(2*codecount);
	findmap = YoixObject.newDictionary(2*codecount);
	allnames = YoixObject.newArray(codecount);
	allcells = YoixObject.newArray(codecount);
	nextname = 0;
	nextcell = 0;

	badcharmap = getBadCharMap();
	data = YoixObject.newArray(4*codecount);
	dataoffset = 0;

	blockstart = 0;
	blockend = last;
	blockname = NULL_STRING;

	for (blockindex = 0; blockindex < unicodeblocks.sizeof(); blockindex += 3) {
	    if ((blockend = unicodeblocks.getInt(blockindex+1, 0)) >= first) {
		blockstart = unicodeblocks.getInt(blockindex, 0);
		blockname = unicodeblocks.getObject(blockindex+2);
		break;
	    }
	}

	loadedblocks = YoixObject.newArray(0, -1);
	blockmap = YoixObject.newDictionary(0, -1);
	recordUnicodeBlock(blockname, Math.max(blockstart, first), blockend, loadedblocks, blockmap);

	for (blockrow = 0, code = first; blockrow < blockrows && code <= end; blockrow++) {
	    for (blockcolumn = 0; blockcolumn < blockcolumns && code <= end; blockcolumn++) {
		row = 16*blockrow + blockrow*blockseparation;
		for (rowcount = 0; rowcount < 16; rowcount++, row++) {
		    column = 16*blockcolumn + blockcolumn*blockseparation;
		    for (columncount = 0; columncount < 16; columncount++, column++, code++) {
			name = getHexName(code);
			if (validateChar(name, row, column, badcharmap)) {
			    if ((chardata = unicodedata.getObject(name)) == null)
				chardata = NULL_ARRAY;
			    charname = YoixObject.newString(name);
			    data.put(dataoffset++, charname, false);
			    data.put(dataoffset++, YoixObject.newString((char)code), false);
			    data.put(dataoffset++, (code >= blockstart && code <= blockend) ? blockname : NULL_STRING, false);
			    data.put(dataoffset++, chardata, false);
			    cell = Misc.createChartCell(charname, row, column, 1, 1, YoixObject.newLvalue(data, dataoffset-4), NULL_OBJECT, columns, flags, labelspercell);
			    chartcells.put(row*columns + column, cell, false);
			    cell.put(NL_TIP, chardata != NULL_ARRAY ? chardata.getObject(0) : charname);
			    namemap.put(name, cell, false);
			    if ((key = chardata.getObject()) != null)
				findmap.put(name, key, false);
			    else findmap.put(name, NULL_STRING, false);
			    allnames.put(nextname++, charname, false);
			    allcells.put(nextcell++, cell, false);
			}
			if (code >= blockend) {
			    blockindex += 3;
			    if (blockindex < unicodeblocks.sizeof() - 3) {
				blockstart = unicodeblocks.getInt(blockindex, 0);
				blockend = unicodeblocks.getInt(blockindex+1, 0);
				blockname = unicodeblocks.getObject(blockindex+2);
				if (code < end)
				    recordUnicodeBlock(blockname, blockstart, blockend, loadedblocks, blockmap);
			    } else {
				blockstart = end;
				blockend = end + 1;
				blockname = NULL_STRING;
			    }
			}
		    }
		}
	    }
	}

	if (nextname < allnames.sizeof()) {	// happens when there are bad characters
	    //
	    // Currently doing this because the default code that search for
	    // patterns may assume all elements in allnames are defined.
	    //
	    allnames = Misc.unrollObject(allnames);
	}

	if (nextcell < allcells.sizeof()) {
	    //
	    // Currently doing this because the default code that search for
	    // patterns may assume all elements in allnames are defined.
	    //
	    allcells = Misc.unrollObject(allcells);
	}

	rowends = Misc.getNullArray(rows);
	obj = YoixObject.newArray(2);
	obj.putInt(0, 0);
	obj.putInt(1, columns - 1);
	for (blockrow = 0; blockrow < blockrows; blockrow++) {
	    row = 16*blockrow + blockrow*blockseparation;
	    for (rowcount = 0; rowcount < 16; rowcount++, row++)
		rowends.put(row, obj, false);
	}

	columnends = Misc.getNullArray(columns);
	obj = YoixObject.newArray(2);
	obj.putInt(0, 0);
	obj.putInt(1, rows - 1);
	for (blockcolumn = 0; blockcolumn < blockcolumns; blockcolumn++) {
	    column = 16*blockcolumn + blockcolumn*blockseparation;
	    for (columncount = 0; columncount < 16; columncount++, column++)
		columnends.put(column, obj, false);
	}

	//
	// Store everything that we just built.
	//

	YoixInterpreter.putInt(NL_ROWS, rows);
	YoixInterpreter.putInt(NL_COLUMNS, columns);
	YoixInterpreter.putInt(NL_CELLCOUNT, codecount);
	YoixInterpreter.putObject(NL_ROWENDS, rowends);
	YoixInterpreter.putObject(NL_COLUMNENDS, columnends);
	YoixInterpreter.putObject(NL_NAMEMAP, namemap);
	YoixInterpreter.putObject(NL_FINDMAP, findmap);
	YoixInterpreter.putObject(NL_ALLCELLS, allcells);
	YoixInterpreter.putObject(NL_ALLNAMES, allnames);
	YoixInterpreter.putObject(NL_LOADEDBLOCKS, loadedblocks);
	YoixInterpreter.putObject(NL_BLOCKMAP, blockmap);
	YoixInterpreter.putInt(NL_VALIDATECELLS, false);
	YoixInterpreter.putObject(NL_CHARTCELLS, chartcells);
    }


    private static void
    recordUnicodeBlock(YoixObject name, int start, int end, YoixObject loaded, YoixObject map) {

	YoixObject  obj;

	loaded.put(loaded.sizeof(), name, false);
	obj = YoixObject.newArray(2);
	obj.putInt(0, start);
	obj.putInt(1, end);
	map.put(name.stringValue(), obj, false);
    }


    private static boolean
    validateChar(String name, int row, int column, HashMap map) {

	YoixObject  obj;
	YoixObject  info;
	boolean     result = true;
	String      message;

	if (map != null && map.containsKey(name)) {
	    result = false;
	    if ((message = (String)map.get(name)) != null && message.length() > 0) {
		if ((obj = YoixInterpreter.getObject(NL_BADCHAR_INFO)) != null && obj.notNull()) {
		    info = YoixObject.newArray(3);
		    info.putInt(0, row);
		    info.putInt(1, column);
		    info.putString(2, message);
		    obj.put(obj.sizeof(), info, false);
		}
	    }
	}
	return(result);
    }
}

