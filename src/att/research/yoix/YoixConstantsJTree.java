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

interface YoixConstantsJTree

{

    //
    // Exclusively for JTree related constants that are also loaded
    // automatically into a Yoix dictionary by the Swing module. The
    // load request in YoixModuleSwing.java selects the constants in
    // this file that begin with the YOIX_ prefix, removes it, and 
    // then makes an appropriate definition in a Yoix Dictionary.
    //

    static final int  YOIX_ADD_CHILD =  1;
    static final int  YOIX_ADD_SIBLING_ABOVE =  2;
    static final int  YOIX_ADD_SIBLING_BELOW =  3;
    static final int  YOIX_BREADTH_FIRST =  4;
    static final int  YOIX_COLLAPSE_ALL =  5;
    static final int  YOIX_COLLAPSE_NODE =  6;
    static final int  YOIX_DELETE_NODE =  7;
    static final int  YOIX_DEPTH_FIRST =  8;
    static final int  YOIX_DESELECT_INTERVAL =  9;
    static final int  YOIX_DESELECT_NODE = 10;
    static final int  YOIX_DESELECT_NODES = 11;
    static final int  YOIX_EDIT_CANCEL = 12;
    static final int  YOIX_EDIT_START = 13;
    static final int  YOIX_EDIT_STOP = 14;
    static final int  YOIX_ELEMENT_COUNT = 15;
    static final int  YOIX_EXPAND_ALL = 16;
    static final int  YOIX_EXPAND_NODE = 17;
    static final int  YOIX_GET_EXPANDED_NODES = 18;
    static final int  YOIX_GET_NODE_FOR_ROW = 19;
    static final int  YOIX_GET_PARENT = 20;
    static final int  YOIX_GET_ROW_COUNT = 21;
    static final int  YOIX_GET_ROW_FOR_NODE = 22;
    static final int  YOIX_GET_SELECTED_NODES = 23;
    static final int  YOIX_GET_SELECTED_COUNT = 24;
    static final int  YOIX_GET_SIBLING_ABOVE = 25;
    static final int  YOIX_GET_SIBLING_BELOW = 26;
    static final int  YOIX_MAKE_NODE_VISIBLE = 27;
    static final int  YOIX_NEW_FOR_OLD = 28;
    static final int  YOIX_NODE_COLLAPSED = 29;
    static final int  YOIX_NODE_COLLAPSING = 30;
    static final int  YOIX_NODE_EXPANDED = 31;
    static final int  YOIX_NODE_EXPANDING = 32;
    static final int  YOIX_NODE_HAS_BEEN_EXPANDED = 33;
    static final int  YOIX_NODE_IS_EXPANDED = 34;
    static final int  YOIX_NODE_IS_SELECTED = 35;
    static final int  YOIX_NODE_IS_VISIBLE = 36;
    static final int  YOIX_POSTORDER_TRAVERSAL = 37;
    static final int  YOIX_PREORDER_TRAVERSAL = 38;
    static final int  YOIX_SCROLL_NODE = 39;
    static final int  YOIX_SCROLL_ROW = 40;
    static final int  YOIX_SELECT_ALL = 41;
    static final int  YOIX_SELECT_ALL_VISIBLE = 42;
    static final int  YOIX_SELECT_INTERVAL = 43;
    static final int  YOIX_SELECT_NODE = 44;
    static final int  YOIX_SELECT_NODES = 45;
    static final int  YOIX_SELECT_NONE = 46;
    static final int  YOIX_SELECT_TOGGLE = 47;
    static final int  YOIX_TAGGED_COPY = 48;
    static final int  YOIX_UNTAGGED_COPY = 49;
    static final int  YOIX_UPDATE_COPY = 50;
    static final int  YOIX_UPDATE_TREE = 51;

    static final int  YOIX_NODE_SELECTION_ADDED = 52;
    static final int  YOIX_NODE_SELECTION_REMOVED = 53;
}

