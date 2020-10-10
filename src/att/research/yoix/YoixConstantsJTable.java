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
import java.awt.event.*;
import javax.swing.JTable;

interface YoixConstantsJTable

{

    //
    // Exclusively for JTable related constants that are also loaded
    // automatically into a Yoix dictionary by the Swing module. The
    // load request in YoixModuleSwing.java selects the constants in
    // this file that begin with the YOIX_ prefix, removes it, and 
    // then makes an appropriate definition in a yoix dictionary.
    // N.B. Be sure to change the $module Object array size in
    // YoixModuleSwing.java for each item you add to this list.
    //

    static final int  YOIX_ADD_CELL_SELECTION = 100;
    static final int  YOIX_ADD_COLUMN_SELECTION = 101;
    static final int  YOIX_ADD_ROW_SELECTION = 102;
    static final int  YOIX_APPEND_ROWS = 103;
    static final int  YOIX_CELL_SELECTION = 104;
    static final int  YOIX_CLEAR_SELECTION = 105;
    static final int  YOIX_COLUMN_SELECTION = 106;
    static final int  YOIX_DELETE_ROWS = 107;
    static final int  YOIX_FIND_CELL_AT = 108;
    static final int  YOIX_GET_CELL_SELECTION = 109;
    static final int  YOIX_GET_COLUMN = 110;
    static final int  YOIX_GET_COLUMN_DATA_INDEX = 111;
    static final int  YOIX_GET_COLUMN_FIELD = 112;
    static final int  YOIX_GET_COLUMN_RECT = 113;
    static final int  YOIX_GET_COLUMN_SELECTION = 114;
    static final int  YOIX_GET_COLUMN_VIEW_INDEX = 115;
    static final int  YOIX_GET_EDITOR_BOX = 116;
    static final int  YOIX_GET_FIELD = 117;
    static final int  YOIX_GET_FIND_MARKER = 118;
    static final int  YOIX_GET_ROW = 119;
    static final int  YOIX_GET_ROW_DATA_INDEX = 120;
    static final int  YOIX_GET_ROW_RECT = 121;
    static final int  YOIX_GET_ROW_SELECTION = 122;
    static final int  YOIX_GET_ROW_VIEW_INDEX = 123;
    static final int  YOIX_GET_TYPE_FIELD = 124;
    static final int  YOIX_GET_VISIBLE_RECT = 125;
    static final int  YOIX_INSERT_ROWS = 126;
    static final int  YOIX_IS_CELL_SELECTED = 127;
    static final int  YOIX_IS_COLUMN_SELECTED = 128;
    static final int  YOIX_IS_ROW_SELECTED = 129;
    static final int  YOIX_MAKE_CELL_VISIBLE = 130;
    static final int  YOIX_MOVE_COLUMN = 131;
    static final int  YOIX_REPLACE_ROWS = 132;
    static final int  YOIX_RESET_VIEW = 133;
    static final int  YOIX_ROW_SELECTION = 134;
    static final int  YOIX_ROW_VISIBILITY = 135;
    static final int  YOIX_SET_CELL_SELECTION = 136;
    static final int  YOIX_SET_COLUMN = 137;
    static final int  YOIX_SET_COLUMN_FIELD = 138;
    static final int  YOIX_SET_COLUMN_SELECTION = 139;
    static final int  YOIX_SET_EDITOR_BOX = 140;
    static final int  YOIX_SET_FIELD = 141;
    static final int  YOIX_SET_FIND_MARKER = 142;
    static final int  YOIX_SET_ROW_SELECTION = 143;
    static final int  YOIX_SET_TYPE_FIELD = 144;
    static final int  YOIX_SYNCROWVIEWS = 145;
    static final int  YOIX_TABLE_JOIN = 146;
    static final int  YOIX_TABLE_JOIN_RAW = 147;
    static final int  YOIX_TABLE_RESORT = 148;
    static final int  YOIX_TABLE_SORT = 149;

    //
    // Recent editing additions that probably only apply to JTables.
    //

    static final int  YOIX_EDIT_GET_CELL = 150;
    static final int  YOIX_GET_SELECTED_CELL = 151;

    //
    // These are recent additions that probably should also be supported
    // by our JTree code, so the values will need to be synchronized.
    //

    static final int  YOIX_EDIT_GET_FIELD = 161;
    static final int  YOIX_EDIT_SET_FIELD = 162;
    static final int  YOIX_EDIT_SET_BACKGROUND = 163;
    static final int  YOIX_EDIT_SET_FOREGROUND = 164;

    //
    // These are also defined in YoixConstantsJTree.java and the two
    // sets of defintions currently must match!!! Really ugly and is
    // something we must deal with - later!!!!!!
    //

    static final int  YOIX_EDIT_CANCEL            = 12;
    static final int  YOIX_EDIT_START             = 13;
    static final int  YOIX_EDIT_STOP              = 14;

    //
    // These probably can be eliminated - later.
    //

    static final String  N_YOIX_SET_FIELD = "SET_FIELD";	// unfortunate - later

    //
    // JTable column type flags (remember: these are used in YoixMiscQSort, too!)
    //

    static final int  YOIX_BOOLEAN_TYPE = 1;
    static final int  YOIX_DATE_TYPE = 2;
    static final int  YOIX_DOUBLE_TYPE = 3;
    static final int  YOIX_HISTOGRAM_TYPE = 4;
    static final int  YOIX_ICON_TYPE = 5;
    static final int  YOIX_INTEGER_TYPE = 6;
    static final int  YOIX_MONEY_TYPE = 7;
    static final int  YOIX_OBJECT_TYPE = 8;
    static final int  YOIX_PERCENT_TYPE = 9;
    static final int  YOIX_STRING_TYPE = 10;
    static final int  YOIX_TEXT_TYPE = 11;
    static final int  YOIX_TIMER_TYPE = 12;

    //
    // JTable resize mode constants
    //

    static final int  YOIX_AUTO_RESIZE_OFF = JTable.AUTO_RESIZE_OFF;
    static final int  YOIX_AUTO_RESIZE_NEXT_COLUMN = JTable.AUTO_RESIZE_NEXT_COLUMN;
    static final int  YOIX_AUTO_RESIZE_SUBSEQUENT_COLUMNS = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS;
    static final int  YOIX_AUTO_RESIZE_LAST_COLUMN = JTable.AUTO_RESIZE_LAST_COLUMN;
    static final int  YOIX_AUTO_RESIZE_ALL_COLUMNS = JTable.AUTO_RESIZE_ALL_COLUMNS;
}
