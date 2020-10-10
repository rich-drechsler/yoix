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
import javax.swing.event.*;

interface YoixConstantsJFC

    extends YoixParserConstants

{

    static final Insets  ZEROINSETS = new Insets(0, 0, 0, 0);

    //
    // These were moved here from YoixConstants.java to prevent annoying
    // "focus grabs" that that happened on Macs whenever the interpreter
    // started.
    //

    static final Color  DEFAULT_BACKGROUND = Color.lightGray;
    static final Color  DEFAULT_FOREGROUND = Color.black;

    //
    // STANDARD_CURSOR means each component pick an appropriate default.
    // The value must not collide with AWT cursors, but there's currently
    // no checking!!!
    //

    static final String  N_STANDARD_CURSOR = "STANDARD_CURSOR";
    static final int     V_STANDARD_CURSOR = Math.min(Cursor.CUSTOM_CURSOR, 0) - 1;

    //
    // Event handler names and values.
    //

    static final int  LASTAWTEVENT = AWTEvent.RESERVED_ID_MAX;

    static final String  N_ACTIONPERFORMED = "actionPerformed";
    static final String  N_ADJUSTCHANGED = "adjustmentValueChanged";
    static final String  N_CARETUPDATE = "caretUpdate";
    static final String  N_COMPONENTHIDDEN = "componentHidden";
    static final String  N_COMPONENTMOVED = "componentMoved";
    static final String  N_COMPONENTRESIZED = "componentResized";
    static final String  N_COMPONENTSHOWN = "componentShown";
    static final String  N_DRAGGESTURERECOGNIZED = "dragGestureRecognized";
    static final String  N_DRAGDROPEND = "dragDropEnd";
    static final String  N_DRAGENTER = "dragEnter";
    static final String  N_DRAGEXIT = "dragExit";
    static final String  N_DRAGMOUSEMOVED = "dragMouseMoved";
    static final String  N_DRAGOVER = "dragOver";
    static final String  N_DROP = "drop";
    static final String  N_DROPACTIONCHANGED = "dropActionChanged";
    static final String  N_FOCUSGAINED = "focusGained";
    static final String  N_FOCUSLOST = "focusLost";
    static final String  N_HYPERLINKACTIVATED = "hyperlinkActivated";
    static final String  N_HYPERLINKENTERED = "hyperlinkEntered";
    static final String  N_HYPERLINKEXITED = "hyperlinkExited";
    static final String  N_INVOCATIONACTION = "invocationAction";
    static final String  N_INVOCATIONBROWSE = "invocationBrowse";
    static final String  N_INVOCATIONCHANGE = "invocationChange";
    static final String  N_INVOCATIONEDIT = "invocationEdit";
    static final String  N_INVOCATIONEDITIMPORT = "invocationEditImport";
    static final String  N_INVOCATIONEDITKEY = "invocationEditKey";
    static final String  N_INVOCATIONRUN = "invocationRun";
    static final String  N_INVOCATIONSELECTION = "invocationSelection";
    static final String  N_ITEMCHANGED = "itemStateChanged";
    static final String  N_KEYPRESSED = "keyPressed";
    static final String  N_KEYRELEASED = "keyReleased";
    static final String  N_KEYTYPED = "keyTyped";
    static final String  N_MOUSECLICKED = "mouseClicked";
    static final String  N_MOUSEDRAGGED = "mouseDragged";
    static final String  N_MOUSEENTERED = "mouseEntered";
    static final String  N_MOUSEEXITED = "mouseExited";
    static final String  N_MOUSEMOVED = "mouseMoved";
    static final String  N_MOUSEPRESSED = "mousePressed";
    static final String  N_MOUSERELEASED = "mouseReleased";
    static final String  N_MOUSEWHEELMOVED = "mouseWheelMoved";
    static final String  N_PAINTPAINT = "paint";
    static final String  N_PAINTUPDATE = "update";
    static final String  N_STATECHANGED = "stateChanged";
    static final String  N_TEXTCHANGED = "textValueChanged";
    static final String  N_VALUECHANGED = "valueChanged";
    static final String  N_VERIFIER = "verifier";
    static final String  N_WINDOWACTIVATED = "windowActivated";
    static final String  N_WINDOWCLOSED = "windowClosed";
    static final String  N_WINDOWCLOSING = "windowClosing";
    static final String  N_WINDOWDEACTIVATED = "windowDeactivated";
    static final String  N_WINDOWDEICONIFIED = "windowDeiconified";
    static final String  N_WINDOWICONIFIED = "windowIconified";
    static final String  N_WINDOWOPENED = "windowOpened";

    static final int  V_ACTIONPERFORMED = ActionEvent.ACTION_PERFORMED;
    static final int  V_ADJUSTCHANGED = AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED;
    static final int  V_CARETUPDATE = LASTAWTEVENT + 1;
    static final int  V_COMPONENTHIDDEN = ComponentEvent.COMPONENT_HIDDEN;
    static final int  V_COMPONENTMOVED = ComponentEvent.COMPONENT_MOVED;
    static final int  V_COMPONENTRESIZED = ComponentEvent.COMPONENT_RESIZED;
    static final int  V_COMPONENTSHOWN = ComponentEvent.COMPONENT_SHOWN;
    static final int  V_DRAGGESTURERECOGNIZED = LASTAWTEVENT + 2;
    static final int  V_DRAGDROPEND = LASTAWTEVENT + 3;
    static final int  V_DRAGENTER = LASTAWTEVENT + 4;
    static final int  V_DRAGEXIT = LASTAWTEVENT + 5;
    static final int  V_DRAGMOUSEMOVED = LASTAWTEVENT + 6;
    static final int  V_DRAGOVER = LASTAWTEVENT + 7;
    static final int  V_DROP = LASTAWTEVENT + 8;
    static final int  V_DROPACTIONCHANGED = LASTAWTEVENT + 9;
    static final int  V_FOCUSGAINED = FocusEvent.FOCUS_GAINED;
    static final int  V_FOCUSLOST = FocusEvent.FOCUS_LOST;
    static final int  V_HYPERLINKACTIVATED = LASTAWTEVENT + 10;
    static final int  V_HYPERLINKENTERED = LASTAWTEVENT + 11;
    static final int  V_HYPERLINKEXITED = LASTAWTEVENT + 12;
    static final int  V_INVOCATIONACTION = LASTAWTEVENT + 13;
    static final int  V_INVOCATIONBROWSE = LASTAWTEVENT + 14;
    static final int  V_INVOCATIONCHANGE = LASTAWTEVENT + 15;
    static final int  V_INVOCATIONEDIT = LASTAWTEVENT + 16;
    static final int  V_INVOCATIONEDITIMPORT = LASTAWTEVENT + 17;
    static final int  V_INVOCATIONEDITKEY = LASTAWTEVENT + 18;
    static final int  V_INVOCATIONRUN = InvocationEvent.INVOCATION_DEFAULT;
    static final int  V_INVOCATIONSELECTION = LASTAWTEVENT + 19;
    static final int  V_ITEMCHANGED = ItemEvent.ITEM_STATE_CHANGED;
    static final int  V_KEYPRESSED = KeyEvent.KEY_PRESSED;
    static final int  V_KEYRELEASED = KeyEvent.KEY_RELEASED;
    static final int  V_KEYTYPED = KeyEvent.KEY_TYPED;
    static final int  V_MOUSECLICKED = MouseEvent.MOUSE_CLICKED;
    static final int  V_MOUSEDRAGGED = MouseEvent.MOUSE_DRAGGED;
    static final int  V_MOUSEENTERED = MouseEvent.MOUSE_ENTERED;
    static final int  V_MOUSEEXITED = MouseEvent.MOUSE_EXITED;
    static final int  V_MOUSEMOVED = MouseEvent.MOUSE_MOVED;
    static final int  V_MOUSEPRESSED = MouseEvent.MOUSE_PRESSED;
    static final int  V_MOUSERELEASED = MouseEvent.MOUSE_RELEASED;
    static final int  V_MOUSEWHEELMOVED = MouseEvent.MOUSE_WHEEL;
    static final int  V_PAINTPAINT = PaintEvent.PAINT;
    static final int  V_PAINTUPDATE = PaintEvent.UPDATE;
    static final int  V_STATECHANGED = LASTAWTEVENT + 20;
    static final int  V_TEXTCHANGED = TextEvent.TEXT_VALUE_CHANGED;
    static final int  V_VALUECHANGED = LASTAWTEVENT + 21;
    static final int  V_VERIFIER = LASTAWTEVENT + 22;
    static final int  V_WINDOWACTIVATED = WindowEvent.WINDOW_ACTIVATED;
    static final int  V_WINDOWCLOSED = WindowEvent.WINDOW_CLOSED;
    static final int  V_WINDOWCLOSING = WindowEvent.WINDOW_CLOSING;
    static final int  V_WINDOWDEACTIVATED = WindowEvent.WINDOW_DEACTIVATED;
    static final int  V_WINDOWDEICONIFIED = WindowEvent.WINDOW_DEICONIFIED;
    static final int  V_WINDOWICONIFIED = WindowEvent.WINDOW_ICONIFIED;
    static final int  V_WINDOWOPENED = WindowEvent.WINDOW_OPENED;

    static final int  V_INVALIDEVENT = -1;

    //
    // Event modifier masks.
    //

    static final int  YOIX_SHIFT_MASK = InputEvent.SHIFT_MASK;
    static final int  YOIX_SHIFT_DOWN_MASK = InputEvent.SHIFT_DOWN_MASK;
    static final int  YOIX_CTRL_MASK = InputEvent.CTRL_MASK;
    static final int  YOIX_CTRL_DOWN_MASK = InputEvent.CTRL_DOWN_MASK;
    static final int  YOIX_META_MASK = InputEvent.META_MASK;
    static final int  YOIX_META_DOWN_MASK = InputEvent.META_DOWN_MASK;
    static final int  YOIX_ALT_MASK = InputEvent.ALT_MASK;
    static final int  YOIX_ALT_DOWN_MASK = InputEvent.ALT_DOWN_MASK;
    static final int  YOIX_ALT_GRAPH_MASK = InputEvent.ALT_GRAPH_MASK;
    static final int  YOIX_ALT_GRAPH_DOWN_MASK = InputEvent.ALT_GRAPH_DOWN_MASK;
    static final int  YOIX_BUTTON1_MASK = InputEvent.BUTTON1_MASK;
    static final int  YOIX_BUTTON1_DOWN_MASK = InputEvent.BUTTON1_DOWN_MASK;
    static final int  YOIX_BUTTON2_MASK = InputEvent.BUTTON2_MASK;
    static final int  YOIX_BUTTON2_DOWN_MASK = InputEvent.BUTTON2_DOWN_MASK;
    static final int  YOIX_BUTTON3_MASK = InputEvent.BUTTON3_MASK;
    static final int  YOIX_BUTTON3_DOWN_MASK = InputEvent.BUTTON3_DOWN_MASK;

    static final int  YOIX_BUTTON_MASK = YOIX_BUTTON1_MASK|YOIX_BUTTON2_MASK|YOIX_BUTTON3_MASK;
    static final int  YOIX_BUTTON_DOWN_MASK = YOIX_BUTTON1_DOWN_MASK|YOIX_BUTTON2_DOWN_MASK|YOIX_BUTTON3_DOWN_MASK;
    static final int  YOIX_KEY_MASK = YOIX_SHIFT_MASK|YOIX_CTRL_MASK|YOIX_META_MASK|YOIX_ALT_MASK|YOIX_ALT_GRAPH_MASK;
    static final int  YOIX_KEY_DOWN_MASK = YOIX_SHIFT_DOWN_MASK|YOIX_CTRL_DOWN_MASK|YOIX_META_DOWN_MASK|YOIX_ALT_DOWN_MASK|YOIX_ALT_GRAPH_DOWN_MASK;

    //
    // AdjustEvent types
    //

    static final int  YOIX_BLOCK_DECREMENT = AdjustmentEvent.BLOCK_DECREMENT;
    static final int  YOIX_BLOCK_INCREMENT = AdjustmentEvent.BLOCK_INCREMENT;
    static final int  YOIX_TRACK = AdjustmentEvent.TRACK;
    static final int  YOIX_UNIT_DECREMENT = AdjustmentEvent.UNIT_DECREMENT;
    static final int  YOIX_UNIT_INCREMENT = AdjustmentEvent.UNIT_INCREMENT;

    //
    // MouseWheelEvent types
    //

    static final int  YOIX_WHEEL_BLOCK_SCROLL = MouseWheelEvent.WHEEL_BLOCK_SCROLL;
    static final int  YOIX_WHEEL_UNIT_SCROLL = MouseWheelEvent.WHEEL_UNIT_SCROLL;

    //
    // TextEvent types - really only for Swing, but our documentation and
    // event initialization may imply they're more general.
    //

    static final int YOIX_TEXTCHANGE = 0; 
    static final int YOIX_TEXTINSERT = 1;
    static final int YOIX_TEXTREMOVE = 2;

    //
    // Layout names and values.
    //

    static final int  YOIX_INVALIDLAYOUT = -1;
    static final int  YOIX_BORDERLAYOUT = 0;
    static final int  YOIX_BOXLAYOUT = 1;
    static final int  YOIX_CARDLAYOUT = 2;
    static final int  YOIX_CUSTOMLAYOUT = 3;
    static final int  YOIX_FLOWLAYOUT = 4;
    static final int  YOIX_GRIDBAGLAYOUT = 5;
    static final int  YOIX_GRIDLAYOUT = 6;
    static final int  YOIX_SPRINGLAYOUT = 7;

    //
    // For backward compatibility reasons, these constants should have
    // these particular values.
    //

    static final int  YOIX_FILES_ONLY = 1;
    static final int  YOIX_DIRECTORIES_ONLY = 2;
    static final int  YOIX_FILES_AND_DIRECTORIES = 3;

    //
    // Miscellaneous constants that also end up being used in Yoix programs.
    // We assigned a single fixed value to these constants, so that's what
    // users see, and let our code map that value into the number that Java
    // wants. For example, Java defines a NORTH constant in its BorderLayout
    // and GridBagConstraints classes, but the values may not match. Values
    // assigned to most are optional positive integers, but YOIX_RELATIVE
    // and YOIX_REMAINDER are obvious exceptions.
    //

    static final int  YOIX_ABOVE_BOTTOM = 1;
    static final int  YOIX_ABOVE_TOP = 2;
    static final int  YOIX_ALWAYS = 3;
    static final int  YOIX_AS_NEEDED = 4;
    static final int  YOIX_AUTOMATIC = 5;
    static final int  YOIX_BELOW_BOTTOM = 6;
    static final int  YOIX_BELOW_TOP = 7;
    static final int  YOIX_BLOCKINCREMENT = 8;
    static final int  YOIX_BOTH = 9;
    static final int  YOIX_BOTH_WEIGHTX = 10;
    static final int  YOIX_BOTH_WEIGHTY = 11;
    static final int  YOIX_BOTH_WEIGHT_NONE = 12;
    static final int  YOIX_BOTTOM = 13;
    static final int  YOIX_BOTTOMLEFT = 14;
    static final int  YOIX_BOTTOMRIGHT = 15;
    static final int  YOIX_CENTER = 16;
    static final int  YOIX_COPY = 17;
    static final int  YOIX_COPY_OR_MOVE = 18;
    static final int  YOIX_DEFAULT_OPTION = 19;
    static final int  YOIX_EAST = 20;
    static final int  YOIX_ERROR_MESSAGE = 21;
    static final int  YOIX_HORIZONTAL = 22;
    static final int  YOIX_HORIZONTAL_WEIGHTY = 23;
    static final int  YOIX_HORIZONTAL_WEIGHT_BOTH = 24;
    static final int  YOIX_HORIZONTAL_WEIGHT_NONE = 25;
    static final int  YOIX_INFORMATION_MESSAGE = 26;
    static final int  YOIX_LEADING = 27;
    static final int  YOIX_LEFT = 28;
    static final int  YOIX_LINEMODE = 29;
    static final int  YOIX_LINK = 30;
    static final int  YOIX_LOWER_LEFT_CORNER = 31;
    static final int  YOIX_LOWER_RIGHT_CORNER = 32;
    static final int  YOIX_LOAD = SAVE + 1;
    static final int  YOIX_MOVE = 33;
    static final int  YOIX_NEVER = 34;
    static final int  YOIX_NONE = 35;
    static final int  YOIX_NONE_WEIGHTX = 36;
    static final int  YOIX_NONE_WEIGHTY = 37;
    static final int  YOIX_NONE_WEIGHT_BOTH = 38;
    static final int  YOIX_NORTH = 39;
    static final int  YOIX_NORTHEAST = 40;
    static final int  YOIX_NORTHWEST = 41;
    static final int  YOIX_OK_OPTION = 42;
    static final int  YOIX_OK_CANCEL_OPTION = 43;
    static final int  YOIX_OPEN = SAVE + 1;	// same as YOIX_LOAD
    static final int  YOIX_PLAIN_MESSAGE = 44;
    static final int  YOIX_QUESTION_MESSAGE = 45;
    static final int  YOIX_RELATIVE = GridBagConstraints.RELATIVE;
    static final int  YOIX_REMAINDER = GridBagConstraints.REMAINDER;
    static final int  YOIX_RIGHT = 46;
    static final int  YOIX_SAVE = SAVE;
    static final int  YOIX_SOUTH = 47;
    static final int  YOIX_SOUTHEAST = 48;
    static final int  YOIX_SOUTHWEST = 49;
    static final int  YOIX_TOP = 50;
    static final int  YOIX_TOPLEFT = 51;
    static final int  YOIX_TOPRIGHT = 52;
    static final int  YOIX_TRAILING = 53;
    static final int  YOIX_UNITINCREMENT = 54;
    static final int  YOIX_UPPER_LEFT_CORNER = 55;
    static final int  YOIX_UPPER_RIGHT_CORNER = 56;
    static final int  YOIX_VERTICAL = 57;
    static final int  YOIX_VERTICAL_WEIGHTX = 58;
    static final int  YOIX_VERTICAL_WEIGHT_BOTH = 59;
    static final int  YOIX_VERTICAL_WEIGHT_NONE = 60;
    static final int  YOIX_WARNING_MESSAGE = 61;
    static final int  YOIX_WEST = 62;
    static final int  YOIX_WORDMODE = 63;
    static final int  YOIX_YES_NO_OPTION = 64;
    static final int  YOIX_YES_NO_CANCEL_OPTION = 65;

    static final int  YOIX_BASELINE = 66;
    static final int  YOIX_HORIZONTAL_CENTER = 67;
    static final int  YOIX_VERTICAL_CENTER = 68;
}

