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
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public abstract
class YoixBodyComponent extends YoixPointerActive

    implements YoixAPI,
	       YoixAPIProtected,
	       YoixConstantsJFC,
	       YoixConstantsSwing,
	       YoixConstantsGraphics,
	       YoixInterfaceDnD,
	       YoixInterfaceKillable,
	       YoixInterfaceListener

{

    //
    // Recently added methods that can pick background and foreground colors
    // using UIManager and also try to compensate for JButton's behavior in
    // the "Ocean" theme. There may be other components that need attention
    // and there's a small chance we eventually may want to extend things to
    // font selection. Definitely not convinced about fonts, but if we ever
    // do then we may want (or need) to expand the new N_UIMKEY field. For
    // example it could turn into a dictionary with entries, like "font" or
    // "background", that select the fields should go through the UIMananger
    // lookup (but only when that fields entry in data is NULL). Definitely
    // not urgent and even obvious that it would be useful, at least not yet.
    //

    protected YoixObject  parent;
    protected Object      peer;

    private HashMap  compressevents = null;
    private boolean  iswindow = false;
    private boolean  canshape = false;
    private boolean  packed = false;
    private boolean  firstfocus = false;
    private int      eventmask;

    //
    // Stuff to manage children (screens, processes etc.).
    //

    private Vector  children;
    private Vector  iconified;

    //
    // Drag and drop support - transfertrigger and any related code is only
    // needed on 1.4.X. See the comments in pickTransferTrigger() for more
    // info.
    //

    private DragGestureRecognizer  dragrecognizer = null;
    private YoixDragManager        dragmanager = null;
    private DragSource             dragsource = null;
    private YoixObject             dragcursor = null;
    private boolean                dragstarted = false;
    private boolean                dropstarted = false;

    private MouseEvent             transfertrigger = null;	// 1.4.X kludge

    //
    // We ran into occasional StackOverflowError problem using Java 1.4 and
    // 1.5 when a MouseWheelEvent listener was added to a JFrame, JDialog,
    // or other RootPaneContainers. Using the RootPaneContainer's contentpane
    // eliminated the StackOverflowError but introduced several more problems
    // that we had to address. Redirecting MouseWheelEvents to the contentpane
    // meant all other MouseEvents got bottled up, so we had to redirect all
    // of them. We also had to adjust the locations of the MouseEvents that
    // we redirected to the contentpane, otherwise window decorations would
    // not be properly accounted for. The kludges are in getMouseEventPoint()
    // and getMouseEventSource() and probably will be needed for a long time
    // because the StackOverflowError happened in 1.4 and 1.5.
    //

    private Component  mouseeventsource = null;		// stackoverflow kludge
    private Point      mouseeventoffset = null;		// stackoverflow kludge

    //
    // We use timestamps to decide if a window (and its contents) needs to
    // be updated right before it's made visible because the look and feel
    // changed while the window was invisible. A different approach, which
    // might be better, wouldn't use timestamps but instead would try to
    // decide when the look and feel used to build the window was different
    // from the one currently being used. Probably not hard, but handling
    // everything (e.g., themes etc.) means more testing and experimenting
    // than we can do right now.
    //

    private static long  lookandfeelchange = -1;
    private long         windowupdated = 0;

    //
    // Save a reference to objects that represent visible windows, so they
    // won't accidentally go away and so we can easily update them when the
    // look and feel is changed.
    //

    private static Hashtable  activewindows = new Hashtable();

    //
    // The eventlistener Hashtable maps Yoix event handler names to flags
    // that represent the Java event listeners that we need to enable for
    // a particular component. Flags are combined by the constructor based
    // on the Yoix event handlers that are defined by the Yoix component.
    //
    // The listenermap hashtable gives the reverse mapping; currently only
    // used by a builtin in YoixModuleEvent, so only populate as needed.
    //

    private static HashMap  eventlisteners = new HashMap(40);
    private static HashMap  listenermap = null;

    static final int  ACTIONLISTENER                 = 0x00000001;
    static final int  ADJUSTMENTLISTENER             = 0x00000002;
    static final int  CARETLISTENER                  = 0x00000004;
    static final int  CHANGELISTENER                 = 0x00000008;
    static final int  COMPONENTLISTENER              = 0x00000010;
    static final int  DRAGGESTURELISTENER            = 0x00000020;
    static final int  DROPTARGETLISTENER             = 0x00000040;
    static final int  FOCUSLISTENER                  = 0x00000080;
    static final int  HYPERLINKLISTENER              = 0x00000100;
    static final int  INVOCATIONACTIONLISTENER       = 0x00000200;	// fake
    static final int  INVOCATIONBROWSELISTENER       = 0x00000400;	// fake
    static final int  INVOCATIONEDITLISTENER         = 0x00000800;	// fake
    static final int  INVOCATIONEDITKEYLISTENER      = 0x00001000;	// fake
    static final int  INVOCATIONRUNLISTENER          = 0x00002000;	// fake
    static final int  INVOCATIONSELECTIONLISTENER    = 0x00004000;	// fake
    static final int  ITEMLISTENER                   = 0x00008000;
    static final int  KEYLISTENER                    = 0x00010000;
    static final int  MOUSELISTENER                  = 0x00020000;
    static final int  MOUSEMOTIONLISTENER            = 0x00040000;
    static final int  MOUSEWHEELLISTENER             = 0x00080000;
    static final int  SELECTIONLISTENER              = 0x00100000;
    static final int  TEXTLISTENER                   = 0x00200000;
    static final int  WINDOWLISTENER                 = 0x00400000;

    static final int  INVOCATIONCHANGELISTENER       = 0x00800000;

    static final int  VERIFIERLISTENER               = 0x01000000;      // fake
    static final int  INVOCATIONEDITIMPORTLISTENER   = 0x02000000;	// fake

    static final int  NEXTLISTENER                   = 0x04000000;  	// next free flag

    static {
	eventlisteners.put(N_ACTIONPERFORMED, new Integer(ACTIONLISTENER));
	eventlisteners.put(N_ADJUSTCHANGED, new Integer(ADJUSTMENTLISTENER));
	eventlisteners.put(N_CARETUPDATE, new Integer(CARETLISTENER));
	eventlisteners.put(N_COMPONENTHIDDEN, new Integer(COMPONENTLISTENER));
	eventlisteners.put(N_COMPONENTMOVED, new Integer(COMPONENTLISTENER));
	eventlisteners.put(N_COMPONENTRESIZED, new Integer(COMPONENTLISTENER));
	eventlisteners.put(N_COMPONENTSHOWN, new Integer(COMPONENTLISTENER));
	eventlisteners.put(N_FOCUSGAINED, new Integer(FOCUSLISTENER));
	eventlisteners.put(N_FOCUSLOST, new Integer(FOCUSLISTENER));
	eventlisteners.put(N_HYPERLINKACTIVATED, new Integer(HYPERLINKLISTENER));
	eventlisteners.put(N_HYPERLINKENTERED, new Integer(HYPERLINKLISTENER));
	eventlisteners.put(N_HYPERLINKEXITED, new Integer(HYPERLINKLISTENER));
	eventlisteners.put(N_INVOCATIONACTION, new Integer(INVOCATIONACTIONLISTENER));
	eventlisteners.put(N_INVOCATIONBROWSE, new Integer(INVOCATIONBROWSELISTENER));
	eventlisteners.put(N_INVOCATIONCHANGE, new Integer(INVOCATIONCHANGELISTENER));
	eventlisteners.put(N_INVOCATIONEDIT, new Integer(INVOCATIONEDITLISTENER));
	eventlisteners.put(N_INVOCATIONEDITIMPORT, new Integer(INVOCATIONEDITIMPORTLISTENER));
	eventlisteners.put(N_INVOCATIONEDITKEY, new Integer(INVOCATIONEDITKEYLISTENER));
	eventlisteners.put(N_INVOCATIONRUN, new Integer(INVOCATIONRUNLISTENER));
	eventlisteners.put(N_INVOCATIONSELECTION, new Integer(INVOCATIONSELECTIONLISTENER));
	eventlisteners.put(N_ITEMCHANGED, new Integer(ITEMLISTENER));
	eventlisteners.put(N_KEYPRESSED, new Integer(KEYLISTENER));
	eventlisteners.put(N_KEYRELEASED, new Integer(KEYLISTENER));
	eventlisteners.put(N_KEYTYPED, new Integer(KEYLISTENER));
	eventlisteners.put(N_MOUSECLICKED, new Integer(MOUSELISTENER));
	eventlisteners.put(N_MOUSEDRAGGED, new Integer(MOUSEMOTIONLISTENER));
	eventlisteners.put(N_MOUSEENTERED, new Integer(MOUSELISTENER));
	eventlisteners.put(N_MOUSEEXITED, new Integer(MOUSELISTENER));
	eventlisteners.put(N_MOUSEMOVED, new Integer(MOUSEMOTIONLISTENER));
	eventlisteners.put(N_MOUSEPRESSED, new Integer(MOUSELISTENER));
	eventlisteners.put(N_MOUSERELEASED, new Integer(MOUSELISTENER));
	eventlisteners.put(N_MOUSEWHEELMOVED, new Integer(MOUSEWHEELLISTENER));
	eventlisteners.put(N_STATECHANGED, new Integer(CHANGELISTENER));
	eventlisteners.put(N_TEXTCHANGED, new Integer(TEXTLISTENER));
	eventlisteners.put(N_VALUECHANGED, new Integer(SELECTIONLISTENER));
	eventlisteners.put(N_VERIFIER, new Integer(VERIFIERLISTENER));
	eventlisteners.put(N_WINDOWACTIVATED, new Integer(WINDOWLISTENER));
	eventlisteners.put(N_WINDOWCLOSED, new Integer(WINDOWLISTENER));
	eventlisteners.put(N_WINDOWCLOSING, new Integer(WINDOWLISTENER));
	eventlisteners.put(N_WINDOWDEACTIVATED, new Integer(WINDOWLISTENER));
	eventlisteners.put(N_WINDOWDEICONIFIED, new Integer(WINDOWLISTENER));
	eventlisteners.put(N_WINDOWICONIFIED, new Integer(WINDOWLISTENER));
	eventlisteners.put(N_WINDOWOPENED, new Integer(WINDOWLISTENER));

	eventlisteners.put(N_DRAGGESTURERECOGNIZED, new Integer(DRAGGESTURELISTENER));
	eventlisteners.put(N_DRAGENTER, new Integer(DROPTARGETLISTENER));
	eventlisteners.put(N_DRAGEXIT, new Integer(DROPTARGETLISTENER));
	eventlisteners.put(N_DRAGOVER, new Integer(DROPTARGETLISTENER));
	eventlisteners.put(N_DROP, new Integer(DROPTARGETLISTENER));
	eventlisteners.put(N_DROPACTIONCHANGED, new Integer(DROPTARGETLISTENER));
    }

    //
    // The $constants array is only used to load jfcconstants with the
    // key/value pairs that let us map Yoix constants into appropriate
    // Java constants. Be careful rearranging $constants - a null value
    // is special and means map the key to the last non-null value that
    // preceeded key in the $constants array.
    //

    private static HashMap  jfcconstants;

    private static Object  $constants[] = {
	"BorderAlignment.DEFAULT", new Integer(TitledBorder.LEADING),
	"BorderAlignment." + YOIX_LEADING, null,
	"BorderAlignment." + YOIX_LEFT, new Integer(TitledBorder.LEFT),
	"BorderAlignment." + YOIX_WEST, null,
	"BorderAlignment." + YOIX_CENTER, new Integer(TitledBorder.CENTER),
	"BorderAlignment." + YOIX_RIGHT, new Integer(TitledBorder.RIGHT),
	"BorderAlignment." + YOIX_EAST, null,
	"BorderAlignment." + YOIX_TRAILING, new Integer(TitledBorder.TRAILING),

	"BorderPosition.DEFAULT", new Integer(TitledBorder.TOP),
	"BorderPosition." + YOIX_TOP, null,
	"BorderPosition." + YOIX_NORTH, null,
	"BorderPosition." + YOIX_BOTTOM, new Integer(TitledBorder.BOTTOM),
	"BorderPosition." + YOIX_SOUTH, null,
	"BorderPosition." + YOIX_ABOVE_TOP, new Integer(TitledBorder.ABOVE_TOP),
	"BorderPosition." + YOIX_ABOVE_BOTTOM, new Integer(TitledBorder.ABOVE_BOTTOM),
	"BorderPosition." + YOIX_BELOW_TOP, new Integer(TitledBorder.BELOW_TOP),
	"BorderPosition." + YOIX_BELOW_BOTTOM, new Integer(TitledBorder.BELOW_BOTTOM),

	"BorderLayout.DEFAULT", BorderLayout.CENTER,
	"BorderLayout." + BorderLayout.CENTER, null,
	"BorderLayout." + YOIX_CENTER, null,
	"BorderLayout." + BorderLayout.SOUTH, BorderLayout.SOUTH,
	"BorderLayout." + YOIX_SOUTH, null,
	"BorderLayout." + YOIX_BOTTOM, null,
	"BorderLayout." + BorderLayout.EAST, BorderLayout.EAST,
	"BorderLayout." + YOIX_EAST, null,
	"BorderLayout." + YOIX_RIGHT, null,
	"BorderLayout." + BorderLayout.WEST, BorderLayout.WEST,
	"BorderLayout." + YOIX_WEST, null,
	"BorderLayout." + YOIX_LEFT, null,
	"BorderLayout." + BorderLayout.NORTH, BorderLayout.NORTH,
	"BorderLayout." + YOIX_NORTH, null,
	"BorderLayout." + YOIX_TOP, null,

	"BoxLayout.DEFAULT", new Integer(BoxLayout.X_AXIS),
	"BoxLayout." + YOIX_HORIZONTAL, null,
	"BoxLayout." + YOIX_VERTICAL, new Integer(BoxLayout.Y_AXIS),

	//
	// The "EventID" and "EventHandler" pairs are practically inverses
	// so new events are normally added to both lists. Only difference
	// right now are "EventHandler" entries for InternalFrameEvents.
	//

	"EventID." + N_ACTIONPERFORMED, new Integer(V_ACTIONPERFORMED),
	"EventID." + N_ADJUSTCHANGED, new Integer(V_ADJUSTCHANGED),
	"EventID." + N_CARETUPDATE, new Integer(V_CARETUPDATE),
	"EventID." + N_COMPONENTHIDDEN, new Integer(V_COMPONENTHIDDEN),
	"EventID." + N_COMPONENTMOVED, new Integer(V_COMPONENTMOVED),
	"EventID." + N_COMPONENTRESIZED, new Integer(V_COMPONENTRESIZED),
	"EventID." + N_COMPONENTSHOWN, new Integer(V_COMPONENTSHOWN),
	"EventID." + N_DRAGGESTURERECOGNIZED, new Integer(V_DRAGGESTURERECOGNIZED),
	"EventID." + N_DRAGDROPEND, new Integer(V_DRAGDROPEND),
	"EventID." + N_DRAGENTER, new Integer(V_DRAGENTER),
	"EventID." + N_DRAGEXIT, new Integer(V_DRAGEXIT),
	"EventID." + N_DRAGOVER, new Integer(V_DRAGOVER),
	"EventID." + N_DROP, new Integer(V_DROP),
	"EventID." + N_DROPACTIONCHANGED, new Integer(V_DROPACTIONCHANGED),
	"EventID." + N_FOCUSGAINED, new Integer(V_FOCUSGAINED),
	"EventID." + N_FOCUSLOST, new Integer(V_FOCUSLOST),
	"EventID." + N_HYPERLINKACTIVATED, new Integer(V_HYPERLINKACTIVATED),
	"EventID." + N_HYPERLINKENTERED, new Integer(V_HYPERLINKENTERED),
	"EventID." + N_HYPERLINKEXITED, new Integer(V_HYPERLINKEXITED),
	"EventID." + N_INVOCATIONACTION, new Integer(V_INVOCATIONACTION),
	"EventID." + N_INVOCATIONBROWSE, new Integer(V_INVOCATIONBROWSE),
	"EventID." + N_INVOCATIONCHANGE, new Integer(V_INVOCATIONCHANGE),
	"EventID." + N_INVOCATIONEDIT, new Integer(V_INVOCATIONEDIT),
	"EventID." + N_INVOCATIONEDITIMPORT, new Integer(V_INVOCATIONEDITIMPORT),
	"EventID." + N_INVOCATIONEDITKEY, new Integer(V_INVOCATIONEDITKEY),
	"EventID." + N_INVOCATIONRUN, new Integer(V_INVOCATIONRUN),
	"EventID." + N_INVOCATIONSELECTION, new Integer(V_INVOCATIONSELECTION),
	"EventID." + N_ITEMCHANGED, new Integer(V_ITEMCHANGED),
	"EventID." + N_KEYPRESSED, new Integer(V_KEYPRESSED),
	"EventID." + N_KEYRELEASED, new Integer(V_KEYRELEASED),
	"EventID." + N_KEYTYPED, new Integer(V_KEYTYPED),
	"EventID." + N_MOUSECLICKED, new Integer(V_MOUSECLICKED),
	"EventID." + N_MOUSEDRAGGED, new Integer(V_MOUSEDRAGGED),
	"EventID." + N_MOUSEENTERED, new Integer(V_MOUSEENTERED),
	"EventID." + N_MOUSEEXITED, new Integer(V_MOUSEEXITED),
	"EventID." + N_MOUSEMOVED, new Integer(V_MOUSEMOVED),
	"EventID." + N_MOUSEPRESSED, new Integer(V_MOUSEPRESSED),
	"EventID." + N_MOUSERELEASED, new Integer(V_MOUSERELEASED),
	"EventID." + N_MOUSEWHEELMOVED, new Integer(V_MOUSEWHEELMOVED),
	"EventID." + N_PAINTPAINT, new Integer(V_PAINTPAINT),
	"EventID." + N_PAINTUPDATE, new Integer(V_PAINTUPDATE),
	"EventID." + N_STATECHANGED, new Integer(V_STATECHANGED),
	"EventID." + N_TEXTCHANGED, new Integer(V_TEXTCHANGED),
	"EventID." + N_VALUECHANGED, new Integer(V_VALUECHANGED),
	"EventID." + N_VERIFIER, new Integer(V_VERIFIER),
	"EventID." + N_WINDOWACTIVATED, new Integer(V_WINDOWACTIVATED),
	"EventID." + N_WINDOWCLOSED, new Integer(V_WINDOWCLOSED),
	"EventID." + N_WINDOWCLOSING, new Integer(V_WINDOWCLOSING),
	"EventID." + N_WINDOWDEACTIVATED, new Integer(V_WINDOWDEACTIVATED),
	"EventID." + N_WINDOWDEICONIFIED, new Integer(V_WINDOWDEICONIFIED),
	"EventID." + N_WINDOWICONIFIED, new Integer(V_WINDOWICONIFIED),
	"EventID." + N_WINDOWOPENED, new Integer(V_WINDOWOPENED),

	"EventHandler." + V_ACTIONPERFORMED, N_ACTIONPERFORMED,
	"EventHandler." + V_ADJUSTCHANGED, N_ADJUSTCHANGED,
	"EventHandler." + V_CARETUPDATE, N_CARETUPDATE,
	"EventHandler." + V_COMPONENTHIDDEN, N_COMPONENTHIDDEN,
	"EventHandler." + V_COMPONENTMOVED, N_COMPONENTMOVED,
	"EventHandler." + V_COMPONENTRESIZED, N_COMPONENTRESIZED,
	"EventHandler." + V_COMPONENTSHOWN, N_COMPONENTSHOWN,
	"EventHandler." + V_DRAGGESTURERECOGNIZED, N_DRAGGESTURERECOGNIZED,
	"EventHandler." + V_DRAGDROPEND, N_DRAGDROPEND,
	"EventHandler." + V_DRAGENTER, N_DRAGENTER,
	"EventHandler." + V_DRAGEXIT, N_DRAGEXIT,
	"EventHandler." + V_DRAGOVER, N_DRAGOVER,
	"EventHandler." + V_DROP, N_DROP,
	"EventHandler." + V_DROPACTIONCHANGED, N_DROPACTIONCHANGED,
	"EventHandler." + V_FOCUSGAINED, N_FOCUSGAINED,
	"EventHandler." + V_FOCUSLOST, N_FOCUSLOST,
	"EventHandler." + V_HYPERLINKACTIVATED, N_HYPERLINKACTIVATED,
	"EventHandler." + V_HYPERLINKENTERED, N_HYPERLINKENTERED,
	"EventHandler." + V_HYPERLINKEXITED, N_HYPERLINKEXITED,
	"EventHandler." + V_INVOCATIONACTION, N_INVOCATIONACTION,
	"EventHandler." + V_INVOCATIONBROWSE, N_INVOCATIONBROWSE,
	"EventHandler." + V_INVOCATIONCHANGE, N_INVOCATIONCHANGE,
	"EventHandler." + V_INVOCATIONEDIT, N_INVOCATIONEDIT,
	"EventHandler." + V_INVOCATIONEDITIMPORT, N_INVOCATIONEDITIMPORT,
	"EventHandler." + V_INVOCATIONEDITKEY, N_INVOCATIONEDITKEY,
	"EventHandler." + V_INVOCATIONRUN, N_INVOCATIONRUN,
	"EventHandler." + V_INVOCATIONSELECTION, N_INVOCATIONSELECTION,
	"EventHandler." + V_ITEMCHANGED, N_ITEMCHANGED,
	"EventHandler." + V_KEYPRESSED, N_KEYPRESSED,
	"EventHandler." + V_KEYRELEASED, N_KEYRELEASED,
	"EventHandler." + V_KEYTYPED, N_KEYTYPED,
	"EventHandler." + V_MOUSECLICKED, N_MOUSECLICKED,
	"EventHandler." + V_MOUSEDRAGGED, N_MOUSEDRAGGED,
	"EventHandler." + V_MOUSEENTERED, N_MOUSEENTERED,
	"EventHandler." + V_MOUSEEXITED, N_MOUSEEXITED,
	"EventHandler." + V_MOUSEMOVED, N_MOUSEMOVED,
	"EventHandler." + V_MOUSEPRESSED, N_MOUSEPRESSED,
	"EventHandler." + V_MOUSERELEASED, N_MOUSERELEASED,
	"EventHandler." + V_MOUSEWHEELMOVED, N_MOUSEWHEELMOVED,
	"EventHandler." + V_PAINTPAINT, N_PAINTPAINT,
	"EventHandler." + V_PAINTUPDATE, N_PAINTUPDATE,
	"EventHandler." + V_STATECHANGED, N_STATECHANGED,
	"EventHandler." + V_TEXTCHANGED, N_TEXTCHANGED,
	"EventHandler." + V_VALUECHANGED, N_VALUECHANGED,
	"EventHandler." + V_VERIFIER, N_VERIFIER,
	"EventHandler." + V_WINDOWACTIVATED, N_WINDOWACTIVATED,
	"EventHandler." + V_WINDOWCLOSED, N_WINDOWCLOSED,
	"EventHandler." + V_WINDOWCLOSING, N_WINDOWCLOSING,
	"EventHandler." + V_WINDOWDEACTIVATED, N_WINDOWDEACTIVATED,
	"EventHandler." + V_WINDOWDEICONIFIED, N_WINDOWDEICONIFIED,
	"EventHandler." + V_WINDOWICONIFIED, N_WINDOWICONIFIED,
	"EventHandler." + V_WINDOWOPENED, N_WINDOWOPENED,

	"EventHandler." + InternalFrameEvent.INTERNAL_FRAME_ACTIVATED, N_WINDOWACTIVATED,
	"EventHandler." + InternalFrameEvent.INTERNAL_FRAME_CLOSED, N_WINDOWCLOSED,
	"EventHandler." + InternalFrameEvent.INTERNAL_FRAME_CLOSING, N_WINDOWCLOSING,
	"EventHandler." + InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED, N_WINDOWDEACTIVATED,
	"EventHandler." + InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED, N_WINDOWDEICONIFIED,
	"EventHandler." + InternalFrameEvent.INTERNAL_FRAME_ICONIFIED, N_WINDOWICONIFIED,
	"EventHandler." + InternalFrameEvent.INTERNAL_FRAME_OPENED, N_WINDOWOPENED,

	"FileDialog.DEFAULT", new Integer(FileDialog.LOAD),
	"FileDialog." + YOIX_LOAD, null,
	"FileDialog." + YOIX_OPEN, null,
	"FileDialog." + YOIX_SAVE, new Integer(FileDialog.SAVE),

	"FlowLayout.DEFAULT", new Integer(FlowLayout.CENTER),
	"FlowLayout." + YOIX_CENTER, null,
	"FlowLayout." + YOIX_LEFT, new Integer(FlowLayout.LEFT),
	"FlowLayout." + YOIX_WEST, null,
	"FlowLayout." + YOIX_RIGHT, new Integer(FlowLayout.RIGHT),
	"FlowLayout." + YOIX_EAST, null,
	"FlowLayout." + YOIX_LEADING, new Integer(FlowLayout.LEADING),
	"FlowLayout." + YOIX_TRAILING, new Integer(FlowLayout.TRAILING),

	"GridBagAnchor.DEFAULT", new Integer(GridBagConstraints.CENTER),
	"GridBagAnchor." + YOIX_CENTER, null,
	"GridBagAnchor." + YOIX_EAST, new Integer(GridBagConstraints.EAST),
	"GridBagAnchor." + YOIX_RIGHT, null,
	"GridBagAnchor." + YOIX_NORTH, new Integer(GridBagConstraints.NORTH),
	"GridBagAnchor." + YOIX_TOP, null,
	"GridBagAnchor." + YOIX_NORTHEAST, new Integer(GridBagConstraints.NORTHEAST),
	"GridBagAnchor." + YOIX_TOPRIGHT, null,
	"GridBagAnchor." + YOIX_NORTHWEST, new Integer(GridBagConstraints.NORTHWEST),
	"GridBagAnchor." + YOIX_TOPLEFT, null,
	"GridBagAnchor." + YOIX_SOUTH, new Integer(GridBagConstraints.SOUTH),
	"GridBagAnchor." + YOIX_BOTTOM, null,
	"GridBagAnchor." + YOIX_SOUTHEAST, new Integer(GridBagConstraints.SOUTHEAST),
	"GridBagAnchor." + YOIX_BOTTOMRIGHT, null,
	"GridBagAnchor." + YOIX_SOUTHWEST, new Integer(GridBagConstraints.SOUTHWEST),
	"GridBagAnchor." + YOIX_BOTTOMLEFT, null,
	"GridBagAnchor." + YOIX_WEST, new Integer(GridBagConstraints.WEST),
	"GridBagAnchor." + YOIX_LEFT, null,

	"GridBagFill.DEFAULT", new Integer(GridBagConstraints.NONE),
	"GridBagFill." + YOIX_NONE, null,
	"GridBagFill." + YOIX_BOTH, new Integer(GridBagConstraints.BOTH),
	"GridBagFill." + YOIX_HORIZONTAL, new Integer(GridBagConstraints.HORIZONTAL),
	"GridBagFill." + YOIX_VERTICAL, new Integer(GridBagConstraints.VERTICAL),

	"JFileChooserSelectionMode.1", new Integer(JFileChooser.FILES_ONLY),
	"JFileChooserSelectionMode.2", new Integer(JFileChooser.DIRECTORIES_ONLY),
	"JFileChooserSelectionMode.3", new Integer(JFileChooser.FILES_AND_DIRECTORIES),
	"JFileChooserSelectionMode." + YOIX_FILES_ONLY, new Integer(JFileChooser.FILES_ONLY),
	"JFileChooserSelectionMode." + YOIX_DIRECTORIES_ONLY, new Integer(JFileChooser.DIRECTORIES_ONLY),
	"JFileChooserSelectionMode." + YOIX_FILES_AND_DIRECTORIES, new Integer(JFileChooser.FILES_AND_DIRECTORIES),

	"JFileChooserType.DEFAULT", new Integer(JFileChooser.OPEN_DIALOG),
	"JFileChooserType." + YOIX_LOAD, null,
	"JFileChooserType." + YOIX_OPEN, null,
	"JFileChooserType." + YOIX_SAVE, new Integer(JFileChooser.SAVE_DIALOG),

	"JListModel.DEFAULT", new Integer(ListSelectionModel.SINGLE_SELECTION),
	"JListModel." + YOIX_SINGLE_SELECTION, new Integer(ListSelectionModel.SINGLE_SELECTION),
	"JListModel." + YOIX_SINGLE_INTERVAL_SELECTION, new Integer(ListSelectionModel.SINGLE_INTERVAL_SELECTION),
	"JListModel." + YOIX_MULTIPLE_INTERVAL_SELECTION, new Integer(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION),

	"JOptionPaneMessageType.DEFAULT", new Integer(JOptionPane.PLAIN_MESSAGE),
	"JOptionPaneMessageType." + YOIX_PLAIN_MESSAGE, null,
	"JOptionPaneMessageType." + YOIX_ERROR_MESSAGE, new Integer(JOptionPane.ERROR_MESSAGE),
	"JOptionPaneMessageType." + YOIX_INFORMATION_MESSAGE, new Integer(JOptionPane.INFORMATION_MESSAGE),
	"JOptionPaneMessageType." + YOIX_WARNING_MESSAGE, new Integer(JOptionPane.WARNING_MESSAGE),
	"JOptionPaneMessageType." + YOIX_QUESTION_MESSAGE, new Integer(JOptionPane.QUESTION_MESSAGE),

	"JOptionPaneOptionType.DEFAULT", new Integer(JOptionPane.YES_NO_CANCEL_OPTION),
	"JOptionPaneOptionType." + YOIX_YES_NO_OPTION, new Integer(JOptionPane.YES_NO_OPTION),
	"JOptionPaneOptionType." + YOIX_YES_NO_CANCEL_OPTION, new Integer(JOptionPane.YES_NO_CANCEL_OPTION),
	"JOptionPaneOptionType." + YOIX_OK_CANCEL_OPTION, new Integer(JOptionPane.OK_CANCEL_OPTION),
	"JOptionPaneOptionType." + YOIX_DEFAULT_OPTION, new Integer(JOptionPane.DEFAULT_OPTION),
	"JOptionPaneOptionType." + YOIX_OK_OPTION, null,

	"JProgressBar.DEFAULT", new Integer(JProgressBar.HORIZONTAL),
	"JProgressBar." + YOIX_HORIZONTAL, null,
	"JProgressBar." + YOIX_VERTICAL, new Integer(JProgressBar.VERTICAL),

	"JScrollBar.DEFAULT", new Integer(JScrollBar.VERTICAL),
	"JScrollBar." + YOIX_VERTICAL, null,
	"JScrollBar." + YOIX_HORIZONTAL, new Integer(JScrollBar.HORIZONTAL),

	"JScrollPane.DEFAULT", new Integer(YOIX_CENTER),
	"JScrollPane." + YOIX_CENTER, null,
	"JScrollPane." + YOIX_LOWER_LEFT_CORNER, new Integer(YOIX_LOWER_LEFT_CORNER),
	"JScrollPane." + YOIX_SOUTHWEST, null,
	"JScrollPane." + YOIX_LOWER_RIGHT_CORNER, new Integer(YOIX_LOWER_RIGHT_CORNER),
	"JScrollPane." + YOIX_SOUTHEAST, null,
	"JScrollPane." + YOIX_UPPER_LEFT_CORNER, new Integer(YOIX_UPPER_LEFT_CORNER),
	"JScrollPane." + YOIX_NORTHWEST, null,
	"JScrollPane." + YOIX_UPPER_RIGHT_CORNER, new Integer(YOIX_UPPER_RIGHT_CORNER),
	"JScrollPane." + YOIX_NORTHEAST, null,
	"JScrollPane." + YOIX_TOP, new Integer(YOIX_TOP),
	"JScrollPane." + YOIX_NORTH, null,
	"JScrollPane." + YOIX_LEFT, new Integer(YOIX_LEFT),
	"JScrollPane." + YOIX_WEST, null,
	//
	// These were added on 1/13/11 so scrollbars could be supported in
	// the JScrollPane layout array.
	//
	"JScrollPane." + YOIX_HORIZONTAL, new Integer(YOIX_HORIZONTAL),
	"JScrollPane." + YOIX_VERTICAL, new Integer(YOIX_VERTICAL),

	"JSeparator.DEFAULT", new Integer(JSeparator.HORIZONTAL),
	"JSeparator." + YOIX_HORIZONTAL, null,
	"JSeparator." + YOIX_VERTICAL, new Integer(JSeparator.VERTICAL),

	"JSlider.DEFAULT", new Integer(JSlider.HORIZONTAL),
	"JSlider." + YOIX_HORIZONTAL, null,
	"JSlider." + YOIX_VERTICAL, new Integer(JSlider.VERTICAL),

	"JSplitPane.DEFAULT", new Integer(JSplitPane.HORIZONTAL_SPLIT),
	"JSplitPane." + YOIX_HORIZONTAL, null,
	"JSplitPane." + YOIX_VERTICAL, new Integer(JSplitPane.VERTICAL_SPLIT),

	"JTabbedPaneAlignment.DEFAULT", new Integer(JTabbedPane.BOTTOM),
	"JTabbedPaneAlignment." + YOIX_BOTTOM, null,
	"JTabbedPaneAlignment." + YOIX_SOUTH, null,
	"JTabbedPaneAlignment." + YOIX_CENTER, null,
	"JTabbedPaneAlignment." + YOIX_TOP, new Integer(JTabbedPane.TOP),
	"JTabbedPaneAlignment." + YOIX_NORTH, null,
	"JTabbedPaneAlignment." + YOIX_LEFT, new Integer(JTabbedPane.LEFT),
	"JTabbedPaneAlignment." + YOIX_WEST, null,
	"JTabbedPaneAlignment." + YOIX_RIGHT, new Integer(JTabbedPane.RIGHT),
	"JTabbedPaneAlignment." + YOIX_EAST, null,

	"JTabbedPaneScroll.DEFAULT", new Integer(YOIX_NEVER),
	"JTabbedPaneScroll." + YOIX_NONE, null,
	"JTabbedPaneScroll." + YOIX_ALWAYS, new Integer(YOIX_ALWAYS),
	"JTabbedPaneScroll." + YOIX_AS_NEEDED, null,
	"JTabbedPaneScroll." + YOIX_BOTH, null,

	"JToolBar.DEFAULT", new Integer(JToolBar.HORIZONTAL),
	"JToolBar." + YOIX_HORIZONTAL, null,
	"JToolBar." + YOIX_VERTICAL, new Integer(JToolBar.VERTICAL),

	"JTreeModel.DEFAULT", new Integer(TreeSelectionModel.SINGLE_TREE_SELECTION),
	"JTreeModel." + YOIX_SINGLE_SELECTION, new Integer(TreeSelectionModel.SINGLE_TREE_SELECTION),
	"JTreeModel." + YOIX_SINGLE_INTERVAL_SELECTION, new Integer(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION),
	"JTreeModel." + YOIX_MULTIPLE_INTERVAL_SELECTION, new Integer(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION),

	"Label.DEFAULT", new Integer(Label.LEFT),
	"Label." + YOIX_LEFT, null,
	"Label." + YOIX_WEST, null,
	"Label." + YOIX_TOPLEFT, null,
	"Label." + YOIX_BOTTOMLEFT, null,
	"Label." + YOIX_NORTHWEST, null,
	"Label." + YOIX_SOUTHWEST, null,
	"Label." + YOIX_LEADING, null,
	"Label." + YOIX_CENTER, new Integer(Label.CENTER),
	"Label." + YOIX_TOP, null,
	"Label." + YOIX_BOTTOM, null,
	"Label." + YOIX_NORTH, null,
	"Label." + YOIX_SOUTH, null,
	"Label." + YOIX_RIGHT, new Integer(Label.RIGHT),
	"Label." + YOIX_EAST, null,
	"Label." + YOIX_TOPRIGHT, null,
	"Label." + YOIX_BOTTOMRIGHT, null,
	"Label." + YOIX_NORTHEAST, null,
	"Label." + YOIX_SOUTHEAST, null,
	"Label." + YOIX_TRAILING, null,

	"Scrollbar.DEFAULT", new Integer(Scrollbar.VERTICAL),
	"Scrollbar." + YOIX_VERTICAL, null,
	"Scrollbar." + YOIX_HORIZONTAL, new Integer(Scrollbar.HORIZONTAL),

	"ScrollPane.DEFAULT", new Integer(ScrollPane.SCROLLBARS_ALWAYS),
	"ScrollPane." + YOIX_ALWAYS, null,
	"ScrollPane." + YOIX_AS_NEEDED, new Integer(ScrollPane.SCROLLBARS_AS_NEEDED),
	"ScrollPane." + YOIX_NEVER, new Integer(ScrollPane.SCROLLBARS_NEVER),

	"ScrollPolicy.DEFAULT", new Integer(YOIX_HORIZONTAL_NEVER|YOIX_VERTICAL_NEVER),
	"ScrollPolicy." + YOIX_AS_NEEDED, new Integer(YOIX_HORIZONTAL_AS_NEEDED|YOIX_VERTICAL_AS_NEEDED),
	"ScrollPolicy." + YOIX_ALWAYS, new Integer(YOIX_HORIZONTAL_ALWAYS|YOIX_VERTICAL_ALWAYS),
	"ScrollPolicy." + YOIX_BOTH, new Integer(YOIX_HORIZONTAL_ALWAYS|YOIX_VERTICAL_ALWAYS),
	"ScrollPolicy." + YOIX_NEVER, new Integer(YOIX_HORIZONTAL_NEVER|YOIX_VERTICAL_NEVER),
	"ScrollPolicy." + YOIX_NONE, new Integer(YOIX_HORIZONTAL_NEVER|YOIX_VERTICAL_NEVER),
	"ScrollPolicy." + YOIX_HORIZONTAL, new Integer(YOIX_HORIZONTAL_ALWAYS|YOIX_VERTICAL_NEVER),
	"ScrollPolicy." + YOIX_HORIZONTAL_ALWAYS, new Integer(YOIX_HORIZONTAL_ALWAYS),
	"ScrollPolicy." + YOIX_HORIZONTAL_AS_NEEDED, new Integer(YOIX_HORIZONTAL_AS_NEEDED),
	"ScrollPolicy." + YOIX_HORIZONTAL_NEVER, new Integer(YOIX_HORIZONTAL_NEVER),
	"ScrollPolicy." + YOIX_VERTICAL, new Integer(YOIX_HORIZONTAL_NEVER|YOIX_VERTICAL_ALWAYS),
	"ScrollPolicy." + YOIX_VERTICAL_ALWAYS, new Integer(YOIX_VERTICAL_ALWAYS),
	"ScrollPolicy." + YOIX_VERTICAL_AS_NEEDED, new Integer(YOIX_VERTICAL_AS_NEEDED),
	"ScrollPolicy." + YOIX_VERTICAL_NEVER, new Integer(YOIX_VERTICAL_NEVER),
	"ScrollPolicy." + (YOIX_VERTICAL_ALWAYS|YOIX_HORIZONTAL_ALWAYS), new Integer(YOIX_VERTICAL_ALWAYS|YOIX_HORIZONTAL_ALWAYS),
	"ScrollPolicy." + (YOIX_VERTICAL_ALWAYS|YOIX_HORIZONTAL_AS_NEEDED), new Integer(YOIX_VERTICAL_ALWAYS|YOIX_HORIZONTAL_AS_NEEDED),
	"ScrollPolicy." + (YOIX_VERTICAL_ALWAYS|YOIX_HORIZONTAL_NEVER), new Integer(YOIX_VERTICAL_ALWAYS|YOIX_HORIZONTAL_NEVER),
	"ScrollPolicy." + (YOIX_VERTICAL_AS_NEEDED|YOIX_HORIZONTAL_ALWAYS), new Integer(YOIX_VERTICAL_AS_NEEDED|YOIX_HORIZONTAL_ALWAYS),
	"ScrollPolicy." + (YOIX_VERTICAL_AS_NEEDED|YOIX_HORIZONTAL_AS_NEEDED), new Integer(YOIX_VERTICAL_AS_NEEDED|YOIX_HORIZONTAL_AS_NEEDED),
	"ScrollPolicy." + (YOIX_VERTICAL_AS_NEEDED|YOIX_HORIZONTAL_NEVER), new Integer(YOIX_VERTICAL_AS_NEEDED|YOIX_HORIZONTAL_NEVER),
	"ScrollPolicy." + (YOIX_VERTICAL_NEVER|YOIX_HORIZONTAL_ALWAYS), new Integer(YOIX_VERTICAL_NEVER|YOIX_HORIZONTAL_ALWAYS),
	"ScrollPolicy." + (YOIX_VERTICAL_NEVER|YOIX_HORIZONTAL_AS_NEEDED), new Integer(YOIX_VERTICAL_NEVER|YOIX_HORIZONTAL_AS_NEEDED),
	"ScrollPolicy." + (YOIX_VERTICAL_NEVER|YOIX_HORIZONTAL_NEVER), new Integer(YOIX_VERTICAL_NEVER|YOIX_HORIZONTAL_NEVER),

	"SpringLayout.DEFAULT", SpringLayout.SOUTH,
	"SpringLayout." + SpringLayout.SOUTH, null,
	"SpringLayout." + YOIX_SOUTH, null,
	"SpringLayout." + YOIX_BOTTOM, null,
	"SpringLayout." + SpringLayout.EAST, SpringLayout.EAST,
	"SpringLayout." + YOIX_EAST, null,
	"SpringLayout." + YOIX_RIGHT, null,
	"SpringLayout." + SpringLayout.WEST, SpringLayout.WEST,
	"SpringLayout." + YOIX_WEST, null,
	"SpringLayout." + YOIX_LEFT, null,
	"SpringLayout." + SpringLayout.NORTH, SpringLayout.NORTH,
	"SpringLayout." + YOIX_NORTH, null,
	"SpringLayout." + YOIX_TOP, null,
	//
	// Use reflection because these are not defined until JDK 1.6; the
	// behavior will not be quite right since the needed features are
	// not there prior to 1.6
	//
 	"SpringLayout." + YoixReflect.getDeclaredField(SpringLayout.class, "BASELINE", null, SpringLayout.SOUTH), YoixReflect.getDeclaredField(SpringLayout.class, "BASELINE", null, SpringLayout.SOUTH),
 	"SpringLayout." + YOIX_BASELINE, null,
 	"SpringLayout." + YoixReflect.getDeclaredField(SpringLayout.class, "HORIZONTAL_CENTER", null, SpringLayout.SOUTH), YoixReflect.getDeclaredField(SpringLayout.class, "HORIZONTAL_CENTER", null, SpringLayout.WEST),
 	"SpringLayout." + YOIX_HORIZONTAL_CENTER, null,
 	"SpringLayout." + YoixReflect.getDeclaredField(SpringLayout.class, "VERTICAL_CENTER", null, SpringLayout.SOUTH), YoixReflect.getDeclaredField(SpringLayout.class, "VERTICAL_CENTER", null, SpringLayout.NORTH),
 	"SpringLayout." + YOIX_VERTICAL_CENTER, null,

	"SwingHorizontalAlignment.DEFAULT", new Integer(SwingConstants.CENTER),
	"SwingHorizontalAlignment." + YOIX_CENTER, null,
	"SwingHorizontalAlignment." + YOIX_TOP, null,
	"SwingHorizontalAlignment." + YOIX_BOTTOM, null,
	"SwingHorizontalAlignment." + YOIX_NORTH, null,
	"SwingHorizontalAlignment." + YOIX_SOUTH, null,
	"SwingHorizontalAlignment." + YOIX_RIGHT, new Integer(SwingConstants.RIGHT),
	"SwingHorizontalAlignment." + YOIX_EAST, null,
	"SwingHorizontalAlignment." + YOIX_TOPRIGHT, null,
	"SwingHorizontalAlignment." + YOIX_BOTTOMRIGHT, null,
	"SwingHorizontalAlignment." + YOIX_NORTHEAST, null,
	"SwingHorizontalAlignment." + YOIX_SOUTHEAST, null,
	"SwingHorizontalAlignment." + YOIX_LEFT, new Integer(SwingConstants.LEFT),
	"SwingHorizontalAlignment." + YOIX_WEST, null,
	"SwingHorizontalAlignment." + YOIX_TOPLEFT, null,
	"SwingHorizontalAlignment." + YOIX_BOTTOMLEFT, null,
	"SwingHorizontalAlignment." + YOIX_NORTHWEST, null,
	"SwingHorizontalAlignment." + YOIX_SOUTHWEST, null,
	"SwingHorizontalAlignment." + YOIX_LEADING, new Integer(SwingConstants.LEADING),
	"SwingHorizontalAlignment." + YOIX_TRAILING, new Integer(SwingConstants.TRAILING),

	"SwingVerticalAlignment.DEFAULT", new Integer(SwingConstants.CENTER),
	"SwingVerticalAlignment." + YOIX_CENTER, null,
	"SwingVerticalAlignment." + YOIX_RIGHT, null,
	"SwingVerticalAlignment." + YOIX_EAST, null,
	"SwingVerticalAlignment." + YOIX_LEFT, null,
	"SwingVerticalAlignment." + YOIX_WEST, null,
	"SwingVerticalAlignment." + YOIX_LEADING, null,
	"SwingVerticalAlignment." + YOIX_TRAILING, null,
	"SwingVerticalAlignment." + YOIX_TOP, new Integer(SwingConstants.TOP),
	"SwingVerticalAlignment." + YOIX_NORTH, null,
	"SwingVerticalAlignment." + YOIX_TOPRIGHT, null,
	"SwingVerticalAlignment." + YOIX_TOPLEFT, null,
	"SwingVerticalAlignment." + YOIX_NORTHEAST, null,
	"SwingVerticalAlignment." + YOIX_NORTHWEST, null,
	"SwingVerticalAlignment." + YOIX_BOTTOM, new Integer(SwingConstants.BOTTOM),
	"SwingVerticalAlignment." + YOIX_SOUTH, null,
	"SwingVerticalAlignment." + YOIX_BOTTOMRIGHT, null,
	"SwingVerticalAlignment." + YOIX_BOTTOMLEFT, null,
	"SwingVerticalAlignment." + YOIX_SOUTHEAST, null,
	"SwingVerticalAlignment." + YOIX_SOUTHWEST, null,

	"TextArea.DEFAULT", new Integer(TextArea.SCROLLBARS_NONE),
	"TextArea." + YOIX_NONE, null,
	"TextArea." + YOIX_BOTH, new Integer(TextArea.SCROLLBARS_BOTH),
	"TextArea." + YOIX_HORIZONTAL, new Integer(TextArea.SCROLLBARS_HORIZONTAL_ONLY),
	"TextArea." + YOIX_VERTICAL, new Integer(TextArea.SCROLLBARS_VERTICAL_ONLY),

	"TextCanvasAlignment.DEFAULT", new Integer(YOIX_LEFT),
	"TextCanvasAlignment." + YOIX_LEFT, null,
	"TextCanvasAlignment." + YOIX_WEST, null,
	"TextCanvasAlignment." + YOIX_TOPLEFT, null,
	"TextCanvasAlignment." + YOIX_BOTTOMLEFT, null,
	"TextCanvasAlignment." + YOIX_NORTHWEST, null,
	"TextCanvasAlignment." + YOIX_SOUTHWEST, null,
	"TextCanvasAlignment." + YOIX_LEADING, null,
	"TextCanvasAlignment." + YOIX_CENTER, new Integer(YOIX_CENTER),
	"TextCanvasAlignment." + YOIX_TOP, null,
	"TextCanvasAlignment." + YOIX_BOTTOM, null,
	"TextCanvasAlignment." + YOIX_NORTH, null,
	"TextCanvasAlignment." + YOIX_SOUTH, null,
	"TextCanvasAlignment." + YOIX_RIGHT, new Integer(YOIX_RIGHT),
	"TextCanvasAlignment." + YOIX_EAST, null,
	"TextCanvasAlignment." + YOIX_TOPRIGHT, null,
	"TextCanvasAlignment." + YOIX_BOTTOMRIGHT, null,
	"TextCanvasAlignment." + YOIX_NORTHEAST, null,
	"TextCanvasAlignment." + YOIX_SOUTHEAST, null,
	"TextCanvasAlignment." + YOIX_TRAILING, null,

	"TransferHandler.DEFAULT", new Integer(DnDConstants.ACTION_NONE),
	"TransferHandler." + YOIX_NONE, null,
	"TransferHandler." + YOIX_COPY, new Integer(DnDConstants.ACTION_COPY),
	"TransferHandler." + YOIX_COPY_OR_MOVE, new Integer(DnDConstants.ACTION_COPY_OR_MOVE),
	"TransferHandler." + YOIX_MOVE, new Integer(DnDConstants.ACTION_MOVE),
	"TransferHandler." + YOIX_LINK, new Integer(DnDConstants.ACTION_LINK),
    };

    //
    // Load jfcconstants using key/value pairs defined in $constants.
    //

    static {
	jfcconstants = new HashMap($constants.length/2);
	jfcLoader($constants);
	$constants = null;		// never needed again!!
    }

    //
    // The droprobot is part of a kludge designed to address behavior that
    // we recently noticed in Java 1.5.0, but we haven't had the time to
    // really track it down. The behavior also seems to be fixed in 1.6.0.
    // The way we noticed the problem is by dragging something out of one
    // of our components and dropping it in another (in this case is was a
    // JTable). Immediately after the drop we click to get the focus, and
    // then hit a key (F2) that's supposed to enable or disable "drag and
    // drop" in that component. The cursor is supposed to change to inform
    // the user about the drag and drop state, but it doesn't change until
    // we move the point out of the top-level frame and back in again. The
    // kludge that we implemented was to use droprobot when it's not null
    // to quickly move the cursor out of the frame and back in to the drop
    // point.
    //
    // Bottom line is that this kludge was thrown in rather quickly and we
    // will investigate some more in the near future. It's sufficient for
    // now, but it is a bit ugly and it's hard to believe that there isn't
    // a cleaner fix. Linux seems to be the only platform that needs the
    // kludge, so it's disabled if you're running on Windows or Mac OSX.
    //

    private static Robot    droprobot = null;
    private static boolean  dropcheck = false;

    //
    // New setLayout() code was added on 3/1/11 can be enabled by setting
    // USENEWSETLAYOUT to true. Good enough for now, but there's a small
    // chance we may want finer control.
    //

    private static boolean  USENEWSETLAYOUT = true;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixBodyComponent(YoixObject data) {

	super(data);
	parent = null;
	iswindow = false;
	compressevents = null;
	eventmask = buildEventMask();
    }

    ///////////////////////////////////
    //
    // YoixInterfaceBody Methods
    //
    ///////////////////////////////////

    public final int
    type() {

	return(COMPONENT);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceDnD Methods
    //
    ///////////////////////////////////

    public final void
    dragDropEnd(DragSourceDropEvent e) {

	YoixDragManager  manager;

	try {
	    if (dragstarted)
		handleDnDEvent(N_DRAGDROPEND, V_DRAGDROPEND, e);
	}
	finally {
	    dragstarted = false;
	    setDragCursor(null);
	    if ((manager = dragmanager) != null) {
		manager.stopDrag();
		dragmanager = null;
	    }
	}
    }


    public final void
    dragEnter(DragSourceDragEvent e) {

	YoixObject  obj;

	if (dragstarted) {
	    if ((obj = handleDnDEvent(N_DRAGENTER, V_DRAGENTER, e)) != null)
		setDragCursor(obj.getObject(N_CURSOR), e.getDragSourceContext());
	}
    }


    public final void
    dragEnter(DropTargetDragEvent e) {

	YoixObject  obj;

	dropstarted = false;

	if ((obj = handleDnDEvent(N_DRAGENTER, V_DRAGENTER, e)) != null) {
	    if (obj.isNumber()) {
		if (obj.booleanValue()) {
		    e.acceptDrag(e.getDropAction());
		    dropstarted = true;
		} else e.rejectDrag();
	    } else e.rejectDrag();
	} else e.rejectDrag();

	updateDropTarget(dropstarted, false, e.getLocation());
    }


    public final void
    dragExit(DragSourceEvent e) {

	YoixObject  obj;

	if (dragstarted) {
	    if ((obj = handleDnDEvent(N_DRAGEXIT, V_DRAGEXIT, e)) != null)
		setDragCursor(obj.getObject(N_CURSOR), e.getDragSourceContext());
	}
    }


    public final void
    dragExit(DropTargetEvent e) {

	dropstarted = false;
	handleDnDEvent(N_DRAGEXIT, V_DRAGEXIT, e);
	updateDropTarget(dropstarted, false, null);
    }


    public final void
    dragGestureRecognized(DragGestureEvent e) {

	YoixObject  event;
	YoixObject  transferable;
	Object      comp;
	Object      value;

	comp = this.peer;               // snapshot - just to be safe

	if (isDragGesturePossible()) {
	    if (!(comp instanceof YoixInterfaceDragable) || ((YoixInterfaceDragable)comp).getDragPossible()) {
		if ((event = handleDnDEvent(N_DRAGGESTURERECOGNIZED, V_DRAGGESTURERECOGNIZED, e)) != null) {
		    if ((transferable = event.getObject(N_TRANSFERABLE)) != null) {
			if (transferable.notNull()) {
			    //
			    // Just in case - we've seen 1.4.2 misbehave when
			    // a drop went to some external applications (e.g.,
			    // Mozilla 1.6 on Linux). Java might not realize a
			    // drop happened so it doesn't clean things up and
			    // calls our dragDropEnd() method. We never found
			    // a way to reset Java's drag and drop machinery,
			    // but this call should clean our stuff up (e.g.,
			    // a window used to drag an image around should be
			    // hidden and/or disposed). Problem doesn't seem
			    // to happen on 1.5.
			    // 
			    if (dragstarted)
				dragDropEnd(null);
			    dragmanager = new YoixDragManager(this, event, e, dragsource);
			    dragstarted = dragmanager.getDragStarted();
			}
		    }
		}
	    }
	}
    }


    public final void
    dragMouseMoved(DragSourceDragEvent e) {

	YoixObject  obj;

	//
	// This method must be implemented by any class that claims to be
	// a DragSourceMotionListener, which is an interface that appeared
	// in Java 1.4. We don't claim to implement DragSourceMotionListener
	// even though we no longer support 1.3.1. Instead we get here from
	// the dragMouseMoved() method that's implemented in dragmanager.
	// Probably should take a closer look later on, but YoixDragManager
	// isn't just used to hide the DragSourceMotionListener interface
	// so any changes can easily wait.
	//

	if (dragstarted) {
	    if ((obj = handleDnDEvent(N_DRAGMOUSEMOVED, V_DRAGMOUSEMOVED, e)) != null)
		setDragCursor(obj.getObject(N_CURSOR), e.getDragSourceContext());
	}
    }


    public final void
    dragOver(DragSourceDragEvent e) {

	YoixObject  obj;

	if (dragstarted) {
	    if ((obj = handleDnDEvent(N_DRAGOVER, V_DRAGOVER, e)) != null)
		setDragCursor(obj.getObject(N_CURSOR), e.getDragSourceContext());
	}
    }


    public final void
    dragOver(DropTargetDragEvent e) {

	YoixObject  obj;

	if ((obj = handleDnDEvent(N_DRAGOVER, V_DRAGOVER, e)) != null) {
	    if (obj.isNumber()) {
		if (obj.booleanValue()) {
		    e.acceptDrag(e.getDropAction());
		    dropstarted = true;
		} else {
		    e.rejectDrag();
		    dropstarted = false;
		}
	    }
	}
	updateDropTarget(dropstarted, false, e.getLocation());
    }


    public final void
    drop(DropTargetDropEvent e) {

	YoixObject  obj;
	boolean     accepted;
	Object      source;
	Point       corner;
	Point       point;

	//
	// Trickier than you might expect, because Java seems to insist
	// that you accept the drop before looking at the Transferable.
	// Quite a few of Java's drag-and-drop "bugs" have been fixed
	// in their 5.0 release - we didn't look carefully for this one
	// (or any of the others) because Yoix doesn't currently require 
	// users to run 5.0.
	//

	dropstarted = false;
	e.acceptDrop(e.getDropAction());	// must happen first!!

	if ((obj = handleDnDEvent(N_DROP, V_DROP, e)) != null) {
	    if (obj.isNumber())
		accepted = obj.booleanValue();
	    else accepted = false;
	} else accepted = false;

	e.dropComplete(accepted);
	updateDropTarget(dropstarted, accepted, null);

	if (droprobot != null || !dropcheck) {
	    if (droprobot == null) {
		//
		// we don't do this upfront in a static block because we
		// only want to create the droprobot when we really must
		// otherwise we access the DISPLAY unnecessarily.
		//
		dropcheck = true;
		if (YoixMisc.jvmCompareTo("1.6.0") < 0 && !(ISWIN || ISMAC)) {
		    try {
			droprobot = new Robot();
		    }
		    catch(AWTException x) {}
		}
	    }
	    if (droprobot != null) {
		source = ((DropTargetEvent)e).getDropTargetContext().getComponent();
		if (source instanceof Component) {
		    corner = ((Component)source).getLocationOnScreen();
		    point = new Point(e.getLocation());
		    point.translate(corner.x, corner.y);
		    droprobot.mouseMove(0, 0);
		    droprobot.mouseMove(point.x, point.y);
		}
	    }
	}
    }


    public final void
    dropActionChanged(DragSourceDragEvent e) {

	YoixObject  obj;

	if (dragstarted) {
	    if ((obj = handleDnDEvent(N_DROPACTIONCHANGED, V_DROPACTIONCHANGED, e)) != null)
		setDragCursor(obj.getObject(N_CURSOR), e.getDragSourceContext());
	}
    }


    public final void
    dropActionChanged(DropTargetDragEvent e) {

	YoixObject  obj;

	if ((obj = handleDnDEvent(N_DROPACTIONCHANGED, V_DROPACTIONCHANGED, e)) != null) {
	    if (obj.isNumber()) {
		if (obj.booleanValue()) {
		    e.acceptDrag(e.getDropAction());
		    dropstarted = true;
		} else {
		    e.rejectDrag();
		    dropstarted = false;
		}
	    }
	}
	updateDropTarget(dropstarted, false, e.getLocation());
    }


    public YoixObject
    getDragCursor() {

	return(dragcursor);
    }


    public boolean
    getDragPossible() {

	boolean  result;
	Object   comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof YoixInterfaceDragable)
	    result = ((YoixInterfaceDragable)comp).getDragPossible();
	else result = true;
	return(true);
    }


    public boolean
    getDragStarted() {

	return(dragstarted);
    }


    public boolean
    isDragGesturePossible() {

	YoixObject  funct;
	boolean     result = false;
	Object      comp;

	//
	// Old version first checked to see if dragrecognizer wasn't null,
	// which implicilty assumed listeners have already been added. We
	// tossed the test because this can be called before they're added
	// (see YoixSwingJTextArea.java). Alternative would be to check the
	// DRAGGESTURELISTENER bit in eventmask, but we really don't think
	// it's necessary.
	//

	comp = this.peer;		// snapshot - just to be safe

	if ((funct = data.getObject(N_DRAGGESTURERECOGNIZED)) != null) {
	    if (funct.notNull()) {
		if (!(comp instanceof JComponent) || ((JComponent)comp).getTransferHandler() == null)
		    result = true;
	    }
	}
	return(result);
    }


    public boolean
    isDragPossible() {

	TransferHandler  handler;
	boolean          result;
	Object           comp;

	if ((result = isDragGesturePossible()) == false) {
	    comp = this.peer;		// snapshot - just to be safe
	    if (comp instanceof JComponent) {
		handler = ((JComponent)comp).getTransferHandler();
		result = handler instanceof YoixSwingTransferHandler;
	    }
	}
	return(result);
    }


    public Cursor
    setDragCursor(YoixObject obj) {

	return(setDragCursor(obj, null));
    }


    public synchronized Cursor
    setDragCursor(YoixObject obj, DragSourceContext context) {

	Cursor  cursor = null;

	if (context != null) {
	    if (obj != null) {
		if (obj.bodyEquals(dragcursor) == false) {
		    if (obj.notNull() || dragcursor != null) {
			if (obj.notNull())
			    cursor = YoixMakeScreen.javaCursor(obj);
			else cursor = null;
			context.setCursor(cursor);
		    } else cursor = context.getCursor();
		    dragcursor = obj;
		} else cursor = context.getCursor();
	    } else cursor = context.getCursor();
	} else {
	    if (obj != null) {
		if (obj.notNull())
		    cursor = YoixMakeScreen.javaCursor(obj);
		else cursor = null;
	    } else cursor = null;
	    dragcursor = obj;
	}
	return(cursor);
    }


    public void
    updateDropTarget(boolean dragging, boolean accepted, Point p) {

	Object  comp;
	Point   corner;

	comp = peer;
	if (comp instanceof YoixInterfaceDragable)
	    ((YoixInterfaceDragable)comp).updateDropTarget(dragging, accepted, p);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceKillable Methods
    //
    ///////////////////////////////////

    public final void
    kill() {

	dispose(false);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceListener Methods
    //
    ///////////////////////////////////

    public void
    actionPerformed(ActionEvent e) {

	EventListener  listeners[];
	boolean        handle = true;
	Object         source;
	int            n;

	//
	// NOTE - we added code that tries to skip calling the event handler
	// when the source is a JMenuItem, we're not the source, and source
	// has an ActionListener with a peer that equals source. This should
	// eliminate multiple actionPerformed() calls when the Yoix version
	// of the JMenuItem defined its own actionPerformed() handler. Yoix
	// AWT components don't support Swing style menu declarations that
	// we're concerned with here so they can be skipped. Change was made
	// on 4/17/08. There's a very small chance that existing applications
	// will notice the change, but we still think it's a good change.
	//
	// NOTE - checking to make sure the length of the listeners[] array
	// is 2 (or perhaps more) probably would be sufficient.
	// 

	source = e.getSource();
	if (source instanceof JMenuItem && peer != source) {
	    listeners = ((JMenuItem)source).getListeners(ActionListener.class);
	    for (n = 0; n < listeners.length; n++) {
		if (listeners[n] instanceof YoixBodyComponent) {
		    if (((YoixBodyComponent)listeners[n]).peer == source) {
			handle = false;
			break;
		    }
		}
	    }
	}
	if (handle)
	    handleAWTEvent(N_ACTIONPERFORMED, V_ACTIONPERFORMED, e);
    }


    public void
    adjustmentValueChanged(AdjustmentEvent e) {

	handleAWTEvent(N_ADJUSTCHANGED, V_ADJUSTCHANGED, e);
    }


    public void
    caretUpdate(CaretEvent e) {

	handleEventObject(N_CARETUPDATE, V_CARETUPDATE, e);
    }


    public void
    componentHidden(ComponentEvent e) {

	handleAWTEvent(N_COMPONENTHIDDEN, V_COMPONENTHIDDEN, e);
	handleShowingChange(false);
    }


    public void
    componentMoved(ComponentEvent e) {

	//
	// Apparently you get this instead of componentShown() when you
	// use older versions of Java on some platforms (e.g., 1.1.8 on
	// SGI). Haven't investigated or experimented much and it seems
	// to be fixed in newer versions of Java, so at this point we're
	// not going to bother trying to compensate.
	//
	handleAWTEvent(N_COMPONENTMOVED, V_COMPONENTMOVED, e);
    }


    public void
    componentResized(ComponentEvent e) {

	handleAWTEvent(N_COMPONENTRESIZED, V_COMPONENTRESIZED, e);
    }


    public void
    componentShown(ComponentEvent e) {

	handleAWTEvent(N_COMPONENTSHOWN, V_COMPONENTSHOWN, e);
	handleShowingChange(true);
    }


    public void
    focusGained(FocusEvent e) {

	handleAWTEvent(N_FOCUSGAINED, V_FOCUSGAINED, e);
    }


    public void
    focusLost(FocusEvent e) {

	handleAWTEvent(N_FOCUSLOST, V_FOCUSLOST, e);
    }


    public void
    hyperlinkUpdate(HyperlinkEvent e) {

	handleEventObject(jfcEventHandler(e), jfcEventID(e), e);
    }


    public void
    internalFrameActivated(InternalFrameEvent e) {

	handleAWTEvent(N_WINDOWACTIVATED, V_WINDOWACTIVATED, e);
    }


    public void
    internalFrameClosed(InternalFrameEvent e) {

	handleAWTEvent(N_WINDOWCLOSED, V_WINDOWCLOSED, e);
    }


    public void
    internalFrameClosing(InternalFrameEvent e) {

	YoixObject  handled;

	handleShowingChange(false);
	handled = handleAWTEvent(N_WINDOWCLOSING, V_WINDOWCLOSING, e);
	if (handled == null || (handled.isNumber() && handled.booleanValue() == false)) {
	    setField(N_VISIBLE, YoixObject.newInt(false));
	    dispose(false);
	}
    }


    public void
    internalFrameDeactivated(InternalFrameEvent e) {

	handleAWTEvent(N_WINDOWDEACTIVATED, V_WINDOWDEACTIVATED, e);
    }


    public void
    internalFrameDeiconified(InternalFrameEvent e) {

	handleAWTEvent(N_WINDOWDEICONIFIED, V_WINDOWDEICONIFIED, e);
	childrenIconify(iconified, false);
    }


    public void
    internalFrameIconified(InternalFrameEvent e) {

	handleAWTEvent(N_WINDOWICONIFIED, V_WINDOWICONIFIED, e);
	childrenIconify(children, true);
    }


    public void
    internalFrameOpened(InternalFrameEvent e) {

	handleAWTEvent(N_WINDOWOPENED, V_WINDOWOPENED, e);
    }


    public void
    itemStateChanged(ItemEvent e) {

	EventListener  listeners[];
	boolean        handle = true;
	Object         source;
	int            n;

	//
	// NOTE - we added code that tries to skip calling the event handler
	// when the source is a JMenuItem, we're not the source, and source
	// has an ItemListener with a peer that equals source. This should
	// eliminate multiple itemStateChanged() calls when the Yoix version
	// of the JMenuItem defined its own itemStateChanged() handler. Yoix
	// AWT components don't support Swing style menu declarations that
	// we're concerned with here so they can be skipped. Change was made
	// on 4/17/08. There's a very small chance that existing applications
	// will notice the change, but we still think it's the good change.
	//
	// NOTE - checking to make sure the length of the listeners[] array
	// is 2 (or perhaps more) probably would be sufficient.
	// 

	source = e.getSource();
	if (source instanceof JMenuItem && peer != source) {
	    listeners = ((JMenuItem)source).getListeners(ItemListener.class);
	    for (n = 0; n < listeners.length; n++) {
		if (listeners[n] instanceof YoixBodyComponent) {
		    if (((YoixBodyComponent)listeners[n]).peer == source) {
			handle = false;
			break;
		    }
		}
	    }
	}
	if (handle)
	    handleAWTEvent(N_ITEMCHANGED, V_ITEMCHANGED, e);
    }


    public void
    keyPressed(KeyEvent e) {

	handleAWTEvent(N_KEYPRESSED, V_KEYPRESSED, e);
    }


    public void
    keyReleased(KeyEvent e) {

	handleAWTEvent(N_KEYRELEASED, V_KEYRELEASED, e);
    }


    public void
    keyTyped(KeyEvent e) {

	handleAWTEvent(N_KEYTYPED, V_KEYTYPED, e);
    }


    public void
    mouseClicked(MouseEvent e) {

	handleAWTEvent(N_MOUSECLICKED, V_MOUSECLICKED, e);
    }


    public void
    mouseDragged(MouseEvent e) {

	handleAWTEvent(N_MOUSEDRAGGED, V_MOUSEDRAGGED, e);
    }


    public void
    mouseEntered(MouseEvent e) {

	handleAWTEvent(N_MOUSEENTERED, V_MOUSEENTERED, e);
    }


    public void
    mouseExited(MouseEvent e) {

	handleAWTEvent(N_MOUSEEXITED, V_MOUSEEXITED, e);
    }


    public void
    mouseMoved(MouseEvent e) {

	handleAWTEvent(N_MOUSEMOVED, V_MOUSEMOVED, e);
    }


    public void
    mousePressed(MouseEvent e) {

	transfertrigger = e;
	handleAWTEvent(N_MOUSEPRESSED, V_MOUSEPRESSED, e);
    }


    public void
    mouseReleased(MouseEvent e) {

	transfertrigger = null;
	handleAWTEvent(N_MOUSERELEASED, V_MOUSERELEASED, e);
    }


    public void
    mouseWheelMoved(MouseWheelEvent e) {

	Object  comp;

	//
	// Looks like disabled Swing components generate MouseWheelEvents.
	// Seems wrong and doesn't happen for AWT components, so we added
	// the isEnabledCheck() (8/10/07).
	//

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof Component && ((Component)comp).isEnabled())
	    handleAWTEvent(N_MOUSEWHEELMOVED, V_MOUSEWHEELMOVED, e);
    }


    public void
    stateChanged(ChangeEvent e) {

	handleEventObject(N_STATECHANGED, V_STATECHANGED, e);
    }


    public void
    textValueChanged(TextEvent e) {

	handleAWTEvent(N_TEXTCHANGED, V_TEXTCHANGED, e);
    }


    public void
    valueChanged(ListSelectionEvent e) {

	handleEventObject(N_VALUECHANGED, V_VALUECHANGED, e);
    }


    public void
    valueChanged(TreeSelectionEvent e) {

	handleEventObject(N_VALUECHANGED, V_VALUECHANGED, e);
    }


    public void
    windowActivated(WindowEvent e) {

	handleAWTEvent(N_WINDOWACTIVATED, V_WINDOWACTIVATED, e);
    }


    public void
    windowClosed(WindowEvent e) {

	handleAWTEvent(N_WINDOWCLOSED, V_WINDOWCLOSED, e);
    }


    public void
    windowClosing(WindowEvent e) {

	YoixObject  handled;

	handleShowingChange(false);
	handled = handleAWTEvent(N_WINDOWCLOSING, V_WINDOWCLOSING, e);
	if (handled == null || (handled.isNumber() && handled.booleanValue() == false)) {
	    setField(N_VISIBLE, YoixObject.newInt(false));
	    dispose(false);
	}
	if (VM.canExit() && VM.getExitModel() == 1)
	    VM.exit(VM.getErrorCount() == 0 ? 0 : -1);
    }


    public void
    windowDeactivated(WindowEvent e) {

	handleAWTEvent(N_WINDOWDEACTIVATED, V_WINDOWDEACTIVATED, e);
    }


    public void
    windowDeiconified(WindowEvent e) {

	handleAWTEvent(N_WINDOWDEICONIFIED, V_WINDOWDEICONIFIED, e);
	childrenIconify(iconified, false);
    }


    public void
    windowIconified(WindowEvent e) {

	handleAWTEvent(N_WINDOWICONIFIED, V_WINDOWICONIFIED, e);
	childrenIconify(children, true);
    }


    public void
    windowOpened(WindowEvent e) {

	handleAWTEvent(N_WINDOWOPENED, V_WINDOWOPENED, e);
    }

    ///////////////////////////////////
    //
    // YoixAPIProtected Methods
    //
    ///////////////////////////////////

    protected Object
    buildPeer() {

	return(null);
    }


    protected YoixObject
    eventCoordinates(AWTEvent e) {

	//
	// Custom modules that extend this class can redefine this method
	// when they want to control the value (it should be a Yoix point)
	// that's stored as N_COORDINATES entry in the Yoix event object.
	// Currently used by an important custom module, so don't think it
	// can be tossed.
	//

	return(null);
    }


    protected YoixBodyMatrix
    getCTMBody() {

	YoixObject  graphics;
	YoixObject  ctm;

	if ((graphics = data.getObject(N_GRAPHICS)) != null) {
	    if ((ctm = graphics.getObject(N_CTM)) == null)
		ctm = VM.getDefaultMatrix();
	} else ctm = VM.getDefaultMatrix();
	return((YoixBodyMatrix)ctm.body());
    }


    protected final Object
    getManagedObject() {

	return(peer);
    }

    ///////////////////////////////////
    //
    // YoixBodyComponent Methods
    //
    ///////////////////////////////////

    final synchronized boolean
    activateListener(String name) {

	int  bit = 0;

	//
	// We're synchronized, but other methods that deal with listeners
	// may not be yet - small hole, but probably not a bit deal so we
	// can wait if necessary.
	//

	if (name != null) {
	    if ((bit = getListenerBit(name)) != 0) {
		if ((eventmask&bit) == 0) {
		    if (addListeners(bit) == 0)
			eventmask |= bit;
		}
	    }
	}
	return((eventmask & bit) != 0);
    }


    void
    addAllListeners() {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp != null) {
	    if (comp instanceof YoixAWTCanvas) {
		//
		// A kludge that's needed because we can't access the
		// newEventsOnly boolean in Component.java, but it has
		// to be true before custom events (probably ItemEvent
		// only) generated by YoixAWTCanvas get through. This
		// may not be a general fix - we looked at older Java
		// code before coming up with the kludge. It also only
		// has to happen once, but this method is only called
		// from the constructor so we aren't careful.
		//
		if ((eventmask & (ITEMLISTENER | ACTIONLISTENER)) != 0) {
		    ((Component)comp).addComponentListener(this);	// sets newEventsOnly
		    ((Component)comp).removeComponentListener(this);
		}
	    }
	    eventmask |= ((comp instanceof YoixInterfaceWindow) ? WINDOWLISTENER|COMPONENTLISTENER : 0);
	    addListeners(eventmask);
	}
    }


    int
    addListeners(Object comp, int mask) {

	int  missed;
	int  listener;
	int  bit;

	missed = 0;

	for (bit = 1; mask != 0 && bit != NEXTLISTENER; bit <<= 1) {
	    switch (listener = (mask & bit)) {
		case ADJUSTMENTLISTENER:
		    if (comp instanceof Adjustable)
			((Adjustable)comp).addAdjustmentListener(this);
		    else missed |= listener;
		    break;

		case COMPONENTLISTENER:
		    if (comp instanceof Component)
			((Component)comp).addComponentListener(this);
		    else missed |= listener;
		    break;

		case DRAGGESTURELISTENER:
		    if (comp instanceof Component)
			addDragGestureListener((Component)comp, this);
		    else missed |= listener;
		    break;

		case DROPTARGETLISTENER:
		    if (comp instanceof Component)
			addDropTargetListener((Component)comp, this);
		    else missed |= listener;
		    break;

		case FOCUSLISTENER:
		    if (comp instanceof Component) {
			((Component)comp).setFocusable(true);		// added 7/4/05
			((Component)comp).addFocusListener(this);
		    } else missed |= listener;
		    break;

		case INVOCATIONRUNLISTENER:
		    // all get it automatically
		    break;

		case ITEMLISTENER:
		    if (comp instanceof ItemSelectable)
			((ItemSelectable)comp).addItemListener(this);
		    else missed |= listener;
		    break;

		case KEYLISTENER:
		    if (comp instanceof Component) {
			((Component)comp).setFocusable(true);		// added 7/4/05
			((Component)comp).addKeyListener(this);
		    } else missed |= listener;
		    break;

		case MOUSELISTENER:
		    if (comp instanceof Component)
			getMouseEventSource().addMouseListener(this);
		    else missed |= listener;
		    break;

		case MOUSEMOTIONLISTENER:
		    if (comp instanceof Component)
			getMouseEventSource().addMouseMotionListener(this);
		    else missed |= listener;
		    break;

		case MOUSEWHEELLISTENER:
		    if (comp instanceof Component)
			getMouseEventSource().addMouseWheelListener(this);
		    else missed |= listener;
		    break;

		case WINDOWLISTENER:
		    if (comp instanceof Window)
			((Window)comp).addWindowListener(this);
		    else missed |= listener;
		    break;

	        default:
		    missed |= listener;
		    break;
	    }
	    mask &= ~bit;
	}

	return(missed);
    }


    final void
    addTo(YoixObject child, Object constraint, YoixObject added, int index) {

	YoixBodyComponent  childcomp;
	YoixObject         components;
	Container          pane;
	Object             comp;
	String             tag;
	int                n;

	//
	// NOTE - we now assume the caller has verified that child is not
	// null and passed isComponent() and notNull() tests, which means
	// the caller now decides how to handle the unusual cases.
	//

	comp = this.peer;		// snapshot - just to be safe

	if ((pane = getContainer(comp)) != null) {
	    try {
		specialAddToProcessing(comp, pane, child, constraint, index);
		tag = child.getString(N_TAG);
		if (added.defined(tag) == false) {
		    added.put(tag, child);
		    child.put(N_ROOT, getContext(), true);
		    childcomp = (YoixBodyComponent)child.body();
		    if (childcomp.isContainer()) {
			if ((components = child.getObject(N_COMPONENTS)) != null) {
			    for (n = 0; n < components.length(); n++) {
				if (components.defined(n)) {
				    child = components.getObject(n);
				    if (((YoixBodyComponent)child.body()) != childcomp) {
					tag = child.getString(N_TAG);
					if (added.defined(tag) == false) {
					    added.put(tag, child);
					    child.put(N_ROOT, getContext(), true);
					} else VM.abort(DUPLICATETAG, tag);
				    }
				}
			    }
			}
		    }
		} else VM.abort(DUPLICATETAG, tag);
	    }
	    catch(RuntimeException e) {
		VM.abort(BADVALUE, N_LAYOUT, index);
	    }
	}
    }


    final void
    addToUnconstrained(YoixObject add, YoixObject added, boolean fronttoback) {

	YoixObject  child;
	int         length;
	int         incr;
	int         n;

	length = add.length();
	incr = fronttoback ? 1 : -1;
	n = fronttoback ? 0 : length - 1;

	for (; n >= 0 && n < length; n += incr) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    if (child.isComponent())
			addTo(child, null, added, n);
		    else if ((child = pickLayoutComponent(child)) != null)
			addTo(child, null, added, n);
		    else VM.abort(BADVALUE, N_LAYOUT, n);
		}
	    }
	}
    }


    final void
    appendText(String text) {

	if (text != null)
	    replaceText(Integer.MAX_VALUE, 0, text, true, null);
    }


    final synchronized YoixObject
    builtinGetEnabled(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Object      pattern;
	int         result = -1;

	if (isMenu(comp)) {
	    if (arg.length == 1) {
		if (arg[0].isString() || arg[0].isRegexp()) {
		    if (arg[0].isString())
			pattern = arg[0].stringValue();
		    else pattern = arg[0].getManagedObject();
		    result = YoixMiscMenu.getMenuItemEnabled(comp, pattern);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	    obj = YoixObject.newInt(result);
	}

	return(obj);
    }


    final synchronized YoixObject
    builtinGetState(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Object      pattern;
	int         result = -1;

	if (isMenu(comp)) {
	    if (arg.length == 1) {
		if (arg[0].isString() || arg[0].isRegexp()) {
		    if (arg[0].isString())
			pattern = arg[0].stringValue();
		    else pattern = arg[0].getManagedObject();
		    result = YoixMiscMenu.getMenuItemState(comp, pattern);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	    obj = YoixObject.newInt(result);
	}

	return(obj);
    }


    final YoixObject
    XXXbuiltinRepaint(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj;
	Rectangle   rect;
	Dimension   dimension;
	Point       point;

	//
	// Decided that this release will only support the the 0 argument
	// version of repaint. probably will expan some next release, but
	// probably just to the one argument version. Think about which
	// matrix should be used - VM.getDefaultMatrix() is the implicit
	// choice and seems reasonable, but may be a little inconsistent,
	// at least compared to paint().
	//

	obj = null;

	if (comp instanceof Component) {
	    if (isDrawable()) {
		if (arg.length != 0) {
		    VM.badCall(name);	// unavailalable for now
		    if (arg.length == 1) {
			if (arg[0].isRectangle()) {
			    rect = YoixMakeScreen.javaRectangle(arg[0]);
			    ((Component)comp).repaint(rect.x, rect.y, rect.width, rect.height);
			} else VM.badArgument(name, 0);
		    } else if (arg.length == 4) {
			if (arg[0].isNumber()) {
			    if (arg[1].isNumber()) {
				if (arg[2].isNumber()) {
				    if (arg[3].isNumber()) {
					point = YoixMakeScreen.javaPoint(
					    arg[0].doubleValue(),
					    arg[1].doubleValue()
					);
					dimension = YoixMakeScreen.javaDimension(
					    arg[2].doubleValue(),
					    arg[3].doubleValue()
					);
					((Component)comp).repaint(
					    point.x, point.y,
					    dimension.width, dimension.height
					);
				    } else VM.badArgument(name, 3);
				} else VM.badArgument(name, 2);
			    } else VM.badArgument(name, 1);
			} else VM.badArgument(name, 0);
		    } else VM.badCall(name);
		} else ((Component)comp).repaint();
		obj = YoixObject.newEmpty();
	    }
	}

	return(obj);
    }


    final YoixObject
    builtinRepaint(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj;
	Rectangle   rect;
	Dimension   dimension;
	Point       point;

	//
	// Decided that this release will only support the the 0 argument
	// version of repaint. probably will expand some next release, but
	// probably just to the one argument version. Think about which
	// matrix should be used - VM.getDefaultMatrix() is the implicit
	// choice and seems reasonable, but may be a little inconsistent,
	// at least compared to paint().
	//

	obj = null;

	if (comp instanceof Component) {
	    if (isDrawable()) {
		if (arg.length != 0) {
		    if (arg.length > 1 || arg[0].notNumber()) {
			VM.badCall(name);	// unavailalable for now
			//
			// Also missing code here to support an optional
			// last argument that means use paintImmediately()
			// if possible (i.e., comp is a JComponent).
			//
			if (arg.length == 1 || arg.length == 2) {
			    if (arg[0].isRectangle()) {
				rect = YoixMakeScreen.javaRectangle(arg[0]);
				((Component)comp).repaint(rect.x, rect.y, rect.width, rect.height);
			    } else VM.badArgument(name, 0);
			} else if (arg.length == 4 || arg.length == 5) {
			    if (arg[0].isNumber()) {
				if (arg[1].isNumber()) {
				    if (arg[2].isNumber()) {
					if (arg[3].isNumber()) {
					    point = YoixMakeScreen.javaPoint(
						arg[0].doubleValue(),
						arg[1].doubleValue()
					    );
					    dimension = YoixMakeScreen.javaDimension(
						arg[2].doubleValue(),
						arg[3].doubleValue()
					    );
					    ((Component)comp).repaint(
						point.x, point.y,
						dimension.width, dimension.height
					    );
					} else VM.badArgument(name, 3);
				    } else VM.badArgument(name, 2);
				} else VM.badArgument(name, 1);
			    } else VM.badArgument(name, 0);
			} else VM.badCall(name);
		    } else {
			if (arg[0].booleanValue()) {
			    if (comp instanceof JComponent) {
				((JComponent)comp).paintImmediately(new Rectangle(((JComponent)comp).getSize()));
			    } else ((Component)comp).repaint();
			} else ((Component)comp).repaint();
		    }
		} else ((Component)comp).repaint();
		obj = YoixObject.newEmpty();
	    }
	}

	return(obj);
    }


    final synchronized YoixObject
    builtinSetEnabled(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Object      pattern;
	int         result = -1;

	if (isMenu(comp)) {
	    if (arg.length == 2) {
		if (arg[0].isString() || arg[0].isRegexp()) {
		    if (arg[1].isNumber()) {
			if (arg[0].isString())
			    pattern = arg[0].stringValue();
		        else pattern = arg[0].getManagedObject();
			result = YoixMiscMenu.setMenuItemEnabled(
			    comp,
			    pattern,
			    arg[1].booleanValue()
			);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	    obj = YoixObject.newInt(result);
	}

	return(obj);
    }


    final synchronized YoixObject
    builtinSetState(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Object      pattern;
	int         result = -1;

	if (isMenu(comp)) {
	    if (arg.length == 2) {
		if (arg[0].isString() || arg[0].isRegexp()) {
		    if (arg[1].isNumber()) {
			if (arg[0].isString())
			    pattern = arg[0].stringValue();
			else pattern = arg[0].getManagedObject();
			result = YoixMiscMenu.setMenuItemState(
			    comp,
			    pattern,
			    arg[1].booleanValue()
			);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	    obj = YoixObject.newInt(result);
	}

	return(obj);
    }


    final synchronized boolean
    canTextChange() {

	YoixObject  lval;
	boolean     result = false;

	if (data.canAccessBody(RW_)) {
	    if (data.defined(N_TEXT)) {
		lval = YoixObject.newLvalue(data, N_TEXT);
		if (lval.canAccess(RW_))
		    result = lval.get().isString();
	    }
	}

	return(result);
    }


    final synchronized void
    childrenAdd(Object child) {

	//
	// Code that handles things when children is null was added on
	// 3/24/07 and is supposed to catch situations where the parent
	// is disposed before the child can be added to children. Lots
	// of room for improvement, but right now we're only interested
	// in plugging possible memory leaks that could happen if child
	// assumes its parent will be handling the dispose. We eventually
	// will revisit this and when we do we should take a close look
	// at synchronization of these methods and dispose.
	//
	// NOTE - the old viersion of this method did
	//
	//	if (children != null) {
	//	    if (child != this && children.contains(child) == false)
	//		children.addElement(child);
	//	}
	//
	// which also wasn't completely correct because childrenDispose()
	// and dispose() aren't synchronized.
	//

	if (isWindow()) {
	    try {
		if (child != this && children.contains(child) == false)
		    children.addElement(child);
	    }
	    catch(NullPointerException e) {
		//
		// This can happen if we try to add a child to a window
		// that's already disposed. It currently duplicates code
		// in childrenDispose(), which we eventually will fix.
		//
		if (child != null && child != this) {
		    if (child instanceof YoixBodyComponent)
			((YoixBodyComponent)child).dispose(false);
		    else if (child instanceof YoixBodyProcess)
			((YoixBodyProcess)child).stop();
		    else if (child instanceof YoixAWTTextComponent)	// unnecessary
			((YoixAWTTextComponent)child).disposeSavedGraphics();
		}
	    }
	}
    }


    final void
    childrenDispose() {

	Vector  children;
	Object  child;
	int     length;
	int     n;

	//
	// Now tossing children and iconified. Probably safe, but it
	// was added quickly and without careful thought so it needs
	// a second look - later.
	//

	children = this.children;		// snapshot - probably unnecessary
	this.children = null;
	this.iconified = null;

	if (children != null) {
	    children = (Vector)children.clone();	// clone - just to be safe
	    length = children.size();
	    for (n = 0; n < length; n++) {
		child = children.elementAt(n);
		if (child != null && child != this) {
		    if (child instanceof YoixBodyComponent)
			((YoixBodyComponent)child).dispose(false);
		    else if (child instanceof YoixBodyProcess)
			((YoixBodyProcess)child).stop();
		    else if (child instanceof YoixAWTTextComponent)	// unnecessary
			((YoixAWTTextComponent)child).disposeSavedGraphics();
		}
	    }
	}
    }


    final void
    childrenIconify(Vector targets, boolean state) {

	YoixObject  visible;
	Object      component;
	Object      child;
	Vector      hidden;
	int         length;
	int         n;

	if (targets != null) {
	    targets = (Vector)targets.clone();		// clone - just to be safe
	    length = targets.size();
	    hidden = null;
	    visible = YoixObject.newInt(!state);
	    for (n = 0; n < length; n++) {
		child = targets.elementAt(n);
		if (child != null && child != this) {
		    if (child instanceof YoixBodyComponent) {
			component = ((YoixBodyComponent)child).getManagedObject();
			if (component instanceof YoixInterfaceWindow) {
			    if (!((component instanceof Dialog) && ((Dialog)component).isModal())) {
				if (component instanceof Frame) {
				    if (state != (((Frame)component).getState() == Frame.ICONIFIED)) {
					if (state) {
					    ((Frame)component).setState(Frame.ICONIFIED);
					    if (hidden == null)
						hidden = new Vector();
					    hidden.addElement(child);
					} else ((Frame)component).setState(Frame.NORMAL);
				    }
				} else {
				    if (((YoixInterfaceWindow)component).isVisible() == state) {
					((YoixBodyComponent)child).setField(N_VISIBLE, visible);
					if (state) {
					    if (hidden == null)
						hidden = new Vector();
					    hidden.addElement(child);
					} else ((YoixInterfaceWindow)component).toFront();
				    }
				}
			    }
			}
		    }
		}
	    }
	    if (state) {		// just in case
		if (iconified == null)
		    iconified = hidden;
	    } else iconified = null;
	}
    }


    final synchronized void
    childrenRemove(Object child) {

	if (children != null)
	    children.removeElement(child);
    }


    final void
    childrenSetVisible(boolean state) {

	YoixObject  visible;
	Object      child;
	Vector      children;
	int         length;
	int         n;

	children = this.children;		// snapshot - probably unnecessary

	if (children != null) {
	    children = (Vector)children.clone();	// clone - just to be safe
	    length = children.size();
	    visible = YoixObject.newInt(state);
	    for (n = 0; n < length; n++) {
		child = children.elementAt(n);
		if (child instanceof YoixBodyComponent)
		    ((YoixBodyComponent)child).setField(N_VISIBLE, visible);
	    }
	}
    }


    final void
    childrenStack(boolean tofront) {

	YoixInterfaceWindow  window;
	Object               component;
	Object               child;
	Vector               children;
	int                  length;
	int                  n;

	children = this.children;		// snapshot - probably unnecessary

	if (children != null) {
	    children = (Vector)children.clone();	// clone - just to be safe
	    length = children.size();
	    for (n = 0; n < length; n++) {
		child = children.elementAt(n);
		if (child instanceof YoixBodyComponent) {
		    component = ((YoixBodyComponent)child).getManagedObject();
		    if (component instanceof YoixInterfaceWindow) {
			window = (YoixInterfaceWindow)component;
			if (window.isVisible()) {
			    if (tofront)
				window.toFront();
			    else window.toBack();
			}
		    }
		}
	    }
	}
    }


    final void
    doLayout() {

	doLayout(data.getObject(N_LAYOUT), data.getBoolean(N_FRONTTOBACK, true), false);
    }


    protected void
    finalize() {

	dispose(true);
	super.finalize();
    }


    static YoixBodyComponent
    findActiveWindowBody(Component comp) {

	YoixBodyComponent body = null;
	Enumeration       enm;
	Object            element;
	Window            target;

	if (comp != null) {
	    if ((target = SwingUtilities.getWindowAncestor(comp)) != null) {
		for (enm = activewindows.elements(); enm.hasMoreElements(); ) {
		    if ((element = enm.nextElement()) != null) {
			if (element instanceof YoixBodyComponent) {
			    if (((YoixBodyComponent)element).getManagedObject() == target) {
				body = (YoixBodyComponent)element;
				break;
			    }
			}
		    }
		}
	    }
	}

	return(body);
    }


    final YoixObject
    findClosestValue(String name) {

	YoixBodyComponent  body;
	YoixObject         obj;
	YoixObject         root;
	YoixObject         components;
	YoixObject         child;
	YoixObject         value;
	Container          container;
	Object             comp;
	int                n;

	comp = this.peer;		// snapshot - just to be safe
	obj = null;

	if (comp instanceof Component) {
	    if ((root = data.getObject(N_ROOT)) != null) {
		if ((components = root.getObject(N_COMPONENTS)) != null) {
		    for (n = 0; n < components.length(); n++) {
			if (components.defined(n)) {
			    child = components.getObject(n);
			    if (child.body() != root.body()) {
				body = (YoixBodyComponent)child.body();
				if (body != this) {
				    if ((container = getContainer(child.getManagedObject())) != null) {
					if (container.isAncestorOf((Component)comp)) {
					    value = body.data.getObject(name);
					    //
					    // Extra tests for specific fields
					    // was added on 11/14/06 and should
					    // cover all uses of this method.
					    //
					    if (value != null && value.notNull())
						obj = value;
					    else if (name.equals(N_BACKGROUND))
						obj = YoixMake.yoixColor(container.getBackground());
					    else if (name.equals(N_FOREGROUND))
						obj = YoixMake.yoixColor(container.getForeground());
					    else if (name.equals(N_ENABLED))
						obj = YoixObject.newInt(container.isEnabled());
					    else if (name.equals(N_OPAQUE))
						obj = YoixObject.newInt(container.isOpaque());
					}
				    }
				} else break;
			    }
			}
		    }
		}
		if (obj == null) { // is obj.isNull() reasonable to check here as well?
		    if (root.body() instanceof YoixBodyComponent) {
			body = (YoixBodyComponent)root.body();
			obj = body.data.getObject(name);
		    }
		}
	    }
	}

	return(obj);
    }


    final YoixObject
    getBackground(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixMake.yoixColor(((Component)comp).getBackground());
	return(obj);
    }


    final YoixObject
    getCompressEvents(Object comp, YoixObject obj) {

	HashMap  events;
	Set      keys;

	if (comp instanceof Component) {
	    if ((events = this.compressevents) != null) {
		if ((keys = events.keySet()) != null)
		    obj = YoixMisc.copyIntoArray(keys.toArray(), true);
		else obj = YoixObject.newArray();
	    } else obj = YoixObject.newArray();
	}
	return(obj);
    }


    final YoixObject
    getDecorationStyle(Object comp, YoixObject obj) {

	int  style;

	if (comp instanceof Frame || comp instanceof Dialog) {
	    if (comp instanceof Frame) {
		if (((Frame)comp).isUndecorated()) {
		    style = 0;
		    if (comp instanceof JFrame) {
			if (((JFrame)comp).getRootPane().getWindowDecorationStyle() == JRootPane.FRAME)
			    style = 2;
			else style = 0;
		    } else style = 0;
		} else style = 1;
	    } else {
		if (((Dialog)comp).isUndecorated()) {
		    style = 0;
		    if (comp instanceof JDialog) {
			if (((JDialog)comp).getRootPane().getWindowDecorationStyle() == JRootPane.PLAIN_DIALOG)
			    style = 2;
			style = 0;
		    } style = 0;
		} else style = 1;
	    }
	    obj = YoixObject.newInt(style);
	}

	return(obj);
    }


    final YoixObject
    getEnabled(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixObject.newInt(((Component)comp).isEnabled());
	return(obj);
    }


    final int
    getEventFlags() {

	return(VM.getInt(N_EVENTFLAGS));
    }


    final YoixObject
    getFocusable(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixObject.newInt(((Component)comp).isFocusable());
	return(obj);
    }


    final YoixObject
    getFocusOwner(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixObject.newInt(((Component)comp).isFocusOwner());
	return(obj);
    }


    final YoixObject
    getFont(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixMake.yoixFont(((Component)comp).getFont());
	else if (comp instanceof MenuComponent)
	    obj = YoixMake.yoixFont(((MenuComponent)comp).getFont());
	return(obj);
    }


    final YoixObject
    getForeground(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixMake.yoixColor(((Component)comp).getForeground());
	return(obj);
    }


    final YoixObject
    getFullScreen(Object comp, YoixObject obj) {

	GraphicsDevice  screen;

	if (comp instanceof Window) {
	    if ((screen = getGraphicsDeviceFromScreen()) != null)
		obj = YoixObject.newInt(comp == screen.getFullScreenWindow());
	}
	return(obj);
    }


    final synchronized YoixObject
    getGraphics(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (isDrawable()) {
		if ((obj = data.getObject(N_GRAPHICS)) != null) {
		    if (obj.isNull()) {
			obj = YoixMake.yoixType(T_GRAPHICS);
			setGraphics(comp, obj);
		    }
		}
	    }
	}
	return(obj);
    }


    final GraphicsConfiguration
    getGraphicsConfigurationFromScreen() {

	GraphicsConfiguration  gc = null;
	YoixObject             obj;

	if ((obj = data.getObject(N_SCREEN)) != null) {
	    if (obj.isScreen())
		gc = ((YoixBodyScreen)obj.body()).getGraphicsConfiguration();
	    else if (obj.isNull())
		gc = YoixMisc.getDefaultGraphicsConfiguration();
	}
	return(gc);
    }


    final GraphicsDevice
    getGraphicsDeviceFromScreen() {

	GraphicsDevice  gd = null;
	YoixObject      obj;

	if ((obj = data.getObject(N_SCREEN)) != null) {
	    if (obj.isScreen())
		gd = ((YoixBodyScreen)obj.body()).getScreenDevice();
	    else if (obj.isNull())
		gd = YoixMisc.getDefaultScreenDevice();
	}
	return(gd);
    }


    static int
    getListenerBit(String name) {

	Integer  mask;

	return((mask = (Integer)eventlisteners.get(name)) != null ? mask.intValue() : 0);
    }


    final YoixObject
    getLocation(Object comp, YoixObject obj) {

	GraphicsConfiguration  gc;
	Rectangle              bounds;
	Point                  point;

	if (comp instanceof Component) {
	    point = ((Component)comp).getLocation();
	    if (comp instanceof Window) {
		if ((gc = getGraphicsConfigurationFromScreen()) != null) {
		    bounds = gc.getBounds();
		    point.x -= bounds.x;
		    point.y -= bounds.y;
		}
	    }
	    obj = YoixMakeScreen.yoixPoint(point);
	}
	return(obj);
    }


    final Point
    getMouseEventOffset(Object comp) {

	//
	// Part of the kludge used to eliminate the Java StackOverflowError
	// bug. Probably could be private and synchronized, but neither one
	// seems particularly important - maybe later.
	//

	if (mouseeventoffset == null && mouseeventsource != null) {
	    if (comp instanceof Component) {
		mouseeventoffset = SwingUtilities.convertPoint(
		    mouseeventsource,
		    new Point(),
		    (Component)comp
		);
	    } else mouseeventoffset = new Point();
	}
	return(mouseeventoffset);
    }


    final Point
    getMouseEventPoint(MouseEvent e) {

	Point  offset;
	Point  point;

	//
	// Part of the kludge used to eliminate the Java StackOverflowError
	// bug that sometimes occurred when a RootPaneContainer was also a
	// MouseWheelEvent listener. This part of the kludge is only used
	// by YoixMakeEvent.yoixEvent() to adjust MouseEvent points, when
	// necessary, to compensate for the fact the MouseEvent might come
	// from a component that has a different coordinate system than the
	// RootPaneContainer.
	//

	point = e.getPoint();
	if (peer != mouseeventsource && e.getSource() == mouseeventsource) {
	    if ((offset = getMouseEventOffset(peer)) != null)
		point.translate(offset.x, offset.y);
	}
	return(point);
    }


    final Point
    getMouseEventPoint(YoixObject event, Component source) {

	Point  offset;
	Point  point;

	//
	// Part of the kludge used to eliminate the Java StackOverflowError
	// bug that sometimes occurred when a RootPaneContainer was also a
	// MouseWheelEvent listener. This part of the kludge is only used
	// by YoixMakeEvent.javaAWTEvent() to adjust location that will be
	// stored in the MouseEvent, when necessary, to compensate for the
	// fact the MouseEvent might be going to a component (via postEvent)
	// that has a different coordinate system than the RootPaneContainer.
	//

	point = YoixMakeScreen.javaPoint(event.getObject(N_LOCATION));
	if (peer != mouseeventsource && source == mouseeventsource) {
	    if ((offset = getMouseEventOffset(peer)) != null)
		point.translate(-offset.x, -offset.y);
	}
	return(point);
    }


    final Component
    getMouseEventSource() {

	Component  child;
	Object     comp;
	int        n;

	//
	// Started as a way to compensate for a MouseWheelEvent bug that
	// sometimes resulted in a Java StackOverflowError when a JFrame,
	// JDialog, or any other RootPaneContainer was a MouseWheelEvent
	// listener. Unfortunately redirecting MouseWheelEvents meant all
	// other MouseEvents got bottled up, so we have to handle all of
	// them if we really want to hide the StackOverflowError bug that
	// we think belongs to Java!! Even more annoying was the fact that
	// as soon as we redirected MouseEvents to the contentpane (or any
	// other pane associated with the RootPaneContainer) we were also
	// forced to adjust event locations because events were happening
	// in a coordinate system that usually didn't match the one that
	// was associated with the RootPaneContainer.
	//
	// NOTE - popup menus attached to a JComboBox on a Mac didn't work
	// properly until the ISMAC code was added on 5/7/08.
	//

	if (mouseeventsource == null) {
	    comp = this.peer;		// snapshot - just to be safe
	    if (comp instanceof RootPaneContainer)
		mouseeventsource = ((RootPaneContainer)comp).getContentPane();
	    else mouseeventsource = (Component)comp;
	    if (ISMAC && mouseeventsource instanceof JComboBox) {	// added on 5/7/08
		for (n = 0; n < ((Container)comp).getComponentCount(); n++) {
		    child = ((Container)comp).getComponent(n);
		    if (child instanceof AbstractButton) {
			mouseeventsource = child;
			break;
		    }
		}
	    }
	}
	return(mouseeventsource);
    }


    final YoixObject
    getParent(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceWindow) {
	    if (parent != null)
		obj = parent;
	}
	return(obj);
    }

 
    final YoixObject
    getPreferredSize(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixMakeScreen.yoixDimension(((Component)comp).getPreferredSize());
	return(obj);
    }


    final YoixObject
    getRoot(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (isContainer(comp)) {
		if (obj == null || obj.isNull()) {	// not convinced by this!!
		    if (isWindow())		// Panels are harder - maybe later
			obj = getContext();
		}
	    }
	}
	return(obj);
    }


    final YoixObject
    getScreen(Object comp, YoixObject obj) {

	//
	// Right now this omits JInternalFrame, which also doesn't currently
	// define a screen field. Good enough for now, but it's something we
	// eventually may address.
	//

	if (comp instanceof Window) {
	    if ((obj = getScreenForObject(data.getObject(N_SCREEN), null)) == null)
		obj = VM.getObject(N_SCREEN);
	}
	return(obj);
    }


    final Dimension
    getShapeSize(Object comp) {

	YoixBodyPath  path;
	YoixObject    obj;
	Rectangle     bounds;
	Dimension     size = null;
	Area          area;

	if (canshape && comp instanceof Window) {
	    if ((obj = data.getObject(N_SHAPE)) != null && obj.isPath()) {
		path = (YoixBodyPath)obj.body();
		path.paint(null);
		if ((area = path.getCurrentArea(YOIX_WIND_NON_ZERO)) != null && !area.isEmpty()) {
		    bounds = area.getBounds();
		    size = new Dimension(bounds.x + bounds.width, bounds.y + bounds.height);
		}
	    }
	}
	return(size);
    }


    final YoixObject
    getShowing(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixObject.newInt(((Component)comp).isShowing());
	return(obj);
    }


    YoixObject
    getSize(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (comp instanceof YoixInterfaceWindow) {
		if (isPacked() == false) {
		    syncDispatchThread(ISMAC ? 2 : 0);		// small ISMAC kludge added 6/4/2008
		    ((YoixInterfaceWindow)comp).pack();
		    setPacked(true);
		}
	    }
	    obj = YoixMakeScreen.yoixDimension(((Component)comp).getSize());
	}
	return(obj);
    }


    final YoixObject
    handleAWTEvent(AWTEvent e) {

	return(handleAWTEvent(jfcEventHandler(e), jfcEventID(e), e));
    }


    final YoixObject
    handleEventObject(EventObject e) {

	return(handleEventObject(jfcEventHandler(e), jfcEventID(e), e));
    }


    final YoixObject
    handleYoixEvent(YoixObject event) {

	YoixObject  funct;
	YoixObject  obj = null;
	Object      value;
	String      name;
	int         id;

	if ((id = jfcEventID(event)) != V_INVALIDEVENT) {
	    if (jfcExists("EventHandler", id)) {
		value = jfcObject("EventHandler", id);
		if (value instanceof String) {
		    name = (String)value;
		    if ((funct = data.getObject(name)) != null && funct.notNull())
			obj = call(funct, new YoixObject[] {event});
		}
	    }
	}
	return(obj);
    }


    final boolean
    isDrawable() {

	return(peer instanceof YoixInterfaceDrawable && ((YoixInterfaceDrawable)peer).isDrawable());
    }


    final boolean
    isPacked() {

	return(packed);
    }


    final boolean
    isWindow() {

	return(iswindow);
    }


    static String
    jfcEventHandler(EventObject e) {

	String  name = null;
	Object  value;
	Object  type;
	int     id;

	//
	// Tries to return the name of the Yoix event handler that should
	// handle a Java event. Unfortunately we can't always determine an
	// answer (e.g., who's supposed to handle a DragSourceDragEvent),
	// so in those cases we return null.
	//

	if (e != null) {
	    if (e instanceof AWTEvent == false) {
		if (e instanceof HyperlinkEvent) {
		    type = ((HyperlinkEvent)e).getEventType();
		    if (type == HyperlinkEvent.EventType.ACTIVATED)
			id = V_HYPERLINKACTIVATED;
		    else if (type == HyperlinkEvent.EventType.ENTERED)
			id = V_HYPERLINKENTERED;
		    else if (type == HyperlinkEvent.EventType.EXITED)
			id = V_HYPERLINKEXITED;
		    else id = V_INVALIDEVENT;
		} else if (e instanceof CaretEvent)
		    id = V_CARETUPDATE;
		else if (e instanceof ChangeEvent)
		    id = V_STATECHANGED;
		else if (e instanceof ListSelectionEvent)
		    id = V_VALUECHANGED;
		else if (e instanceof TreeSelectionEvent)
		    id = V_VALUECHANGED;
		else if (e instanceof DragGestureEvent)
		    id = V_DRAGGESTURERECOGNIZED;
		else if (e instanceof DragSourceDropEvent)
		    id = V_DRAGDROPEND;
		else if (e instanceof DropTargetDropEvent)
		    id = V_DROP;
		else if (e instanceof DragSourceEvent && !(e instanceof DragSourceDragEvent))
		    id = V_DRAGEXIT;
		else if (e instanceof DropTargetEvent && !(e instanceof DropTargetDragEvent))
		    id = V_DRAGEXIT;
		else id = V_INVALIDEVENT;
	    } else id = ((AWTEvent)e).getID();
	    if (jfcExists("EventHandler", id)) {
		value = jfcObject("EventHandler", id);
		if (value instanceof String)
		    name = (String)value;
	    }
	}
	return(name);
    }


    static int
    jfcEventID(EventObject e) {

	Object  value;
	String  handler;
	int     id = V_INVALIDEVENT;

	//
	// Recently changed (5/18/05) to just use getID() when the event
	// is an AWTEvent. In previous versions we always looked for the
	// Yoix event handler and then used the "EventID" pairs to map
	// the handler back to an id. That meant we got V_INVALIDEVENT
	// for AWTEvents that Yoix didn't support (ContainerEvent should
	// be the only one) in the old implementation, but now we could
	// get an interger back that may not have code (e.g., a case in
	// a switch statement) to handle it. Don't think the change will
	// cause any problems - there's even a chance we may add support
	// for ContainerEvents.
	//

	if (e != null) {
	    if (e instanceof AWTEvent == false) {
		if ((handler = jfcEventHandler(e)) != null) {
		    if (jfcExists("EventID", handler)) {
			value = jfcObject("EventID", handler);
			if (value instanceof Integer)
			    id = ((Integer)value).intValue();
		    }
		}
	    } else id = ((AWTEvent)e).getID();
	}
	return(id);
    }


    static int
    jfcEventID(YoixObject event) {

	YoixObject  id;
	Object      handler;
	String      name;
	int         value = V_INVALIDEVENT;

	//
	// Recent change (on 5/20/05) remove an isEvent() check, so the
	// only significant test event has to pass is that it defines a
	// field named N_ID that refers to an event that we recognize.
	// This method is not used much, so we doubt anyone will notice
	// the change.
	// 

	if (event != null) {
	    if ((id = event.getObject(N_ID)) != null) {
		if (id.isString()) {
		    name = id.stringValue();
		    if (jfcExists("EventID", name)) {
			handler = jfcObject("EventID", name);
			if (handler instanceof Integer)
			    value = ((Integer)handler).intValue();
		    }
		} else if (id.isInteger())
		    value = id.intValue();
	    }
	}
	return(value);
    }


    static boolean
    jfcExists(String tag, int field) {

	return(jfcconstants.containsKey(tag + "." + field));
    }


    static boolean
    jfcExists(String tag, String field) {

	return(jfcconstants.containsKey(tag + "." + field));
    }


    static int
    jfcInt(String tag, int field) {

	return(((Integer)jfcObject(tag, field)).intValue());
    }


    static int
    jfcInt(String tag, YoixObject dict, String name) {

	return(jfcInt(tag, dict.getInt(name, -1)));
    }


    static int
    jfcInt(String tag, YoixObject field) {

	Object  obj;

	if (field != null) {
	    if (field.isString())
		obj = jfcObject(tag, field.stringValue());
	    else obj = jfcObject(tag, field.intValue());
	} else obj = jfcObject(tag, "");

	return(((Integer)obj).intValue());
    }


    static void
    jfcLoader(Object constants[]) {

	Object  key;
	Object  value = null;
	int     n;

	for (n = 0; n < constants.length; n += 2) {
	    if ((key = constants[n]) != null) {
		if (constants[n + 1] != null)
		    value = constants[n + 1];
		if (value != null)
		    jfcconstants.put(key, value);
	    }
	}
    }


    static Object
    jfcObject(String tag, int field) {

	Object  obj;

	if ((obj = jfcconstants.get(tag + "." + field)) == null) {
	    if ((obj = jfcconstants.get(tag + ".DEFAULT")) == null)
		VM.die(INTERNALERROR);
	}
	return(obj);
    }


    static Object
    jfcObject(String tag, String field) {

	Object  obj;

	if ((obj = jfcconstants.get(tag + "." + field)) == null) {
	    if ((obj = jfcconstants.get(tag + "." + field.toUpperCase())) == null) {
		if ((obj = jfcconstants.get(tag + ".DEFAULT")) == null)
		    VM.die(INTERNALERROR);
	    }
	}
	return(obj);
    }


    static String
    jfcString(String tag, int field) {

	return((String)jfcObject(tag, field));
    }


    static String
    jfcString(String tag, YoixObject dict, String name) {

	return(jfcString(tag, dict.getInt(name, -1)));
    }


    static String[]
    listenerList(int bit) {

	Iterator  keys;
	HashMap   lmap;
	Integer   bitvalue;
	String    newvalues[];
	String    values[];
	String    key;

	if (listenermap == null) {
	    lmap = new HashMap();
	    keys = eventlisteners.keySet().iterator();
	    while (keys.hasNext()) {
		key = (String)keys.next();
		bitvalue = (Integer)eventlisteners.get(key);
		if ((values = (String[])lmap.get(bitvalue)) == null) {
		    newvalues = new String[1];
		    newvalues[0] = key;
		} else {
		    newvalues = new String[values.length + 1];
		    System.arraycopy(values, 0, newvalues, 0, values.length);
		    newvalues[values.length] = key;
		}
		lmap.put(bitvalue, newvalues);
	    }
	    listenermap = lmap;
	} else lmap = listenermap; // snapshot

	return((String[])lmap.get(new Integer(bit)));
    }


    final YoixObject
    pickLayoutComponent(YoixObject obj) {

	return(pickLayoutComponent(obj, null));
    }


    final YoixObject
    pickLayoutComponent(YoixObject obj, Object constraint) {

	YoixObject  component = null;
	int         alignment;

	//
	// Callers implicitly assume a non-null return means it's a valid
	// YoixObject representation of a component!!
	//
	// NOTE - we evenutally will look into expanding this. For example,
	// It's not hard to imagine handling arrays (e.g., string/textfield
	// pairs), but this is sufficient for now.
	//

	if (obj != null) {
	    if (obj.isComponent() == false) {
		if (obj.isString()) {
		    if (constraint instanceof GridBagConstraints) {
			switch (((GridBagConstraints)constraint).anchor) {
			    case GridBagConstraints.EAST:
			    case GridBagConstraints.NORTHEAST:
			    case GridBagConstraints.SOUTHEAST:
				alignment = YOIX_RIGHT;
				break;

			    case GridBagConstraints.WEST:
			    case GridBagConstraints.NORTHWEST:
			    case GridBagConstraints.SOUTHWEST:
				alignment = YOIX_LEFT;
				break;

			    default:
				alignment = YOIX_CENTER;
				break;
			}
		    } else alignment = YOIX_CENTER;

		    if (this instanceof YoixBodyComponentSwing)
			component = YoixObject.newJComponent(VM.getTypeTemplate(T_JLABEL));
		    else component = YoixObject.newComponent(VM.getTypeTemplate(T_LABEL));
		    component.putString(N_TEXT, obj.stringValue());
		    component.putInt(N_ALIGNMENT, alignment);
		} else component = null;
	    } else component = obj;
	} else component = null;

	return(component);
    }


    final Component
    pickLayoutFiller(YoixObject obj, int orientation) {

	return(pickLayoutFiller(obj, orientation, null));
    }


    final Component
    pickLayoutFiller(YoixObject obj, int orientation, double weights[]) {

	Component  filler = null;
	double     width;
	double     height;
	double     weightx = 0;
	double     weighty = 0;
	int        value;
	int        length;

	//
	// This code was in addToBox(), but any layout manager can now
	// use it if they want to create invisible components that can
	// be used as "struts" or "glue".
	//

	if (obj.isNumber()) {
	    //
	    // We rely on the fact that javaDistance() preserves
	    // the sign of its argument!!!
	    //
	    value = YoixMakeScreen.javaDistance(obj.doubleValue());
	    if (orientation == YOIX_VERTICAL) {
		if (value != 0) {
		    if (value > 0)
			filler = Box.createVerticalStrut(value);
		    else filler = Box.createHorizontalStrut(-value);
		} else {
		    filler = Box.createVerticalGlue();
		    weighty = 1.0;
		}
	    } else {
		if (value != 0) {
		    if (value > 0)
			filler = Box.createHorizontalStrut(value);
		    else filler = Box.createVerticalStrut(-value);
		} else {
		    filler = Box.createHorizontalGlue();
		    weightx = 1.0;
		}
	    }
	} else if (obj.isDimension()) {
	    width = obj.getDouble(N_WIDTH, 0);
	    height = obj.getDouble(N_HEIGHT, 0);
	    if (width != 0 || height != 0) {
		if (width == 0) {
		    if ((value = YoixMakeScreen.javaDistance(height)) > 0)
			filler = Box.createVerticalStrut(value);
		    else filler = Box.createHorizontalStrut(-value);
		} else if (height == 0) {
		    if ((value = YoixMakeScreen.javaDistance(width)) > 0)
			filler = Box.createHorizontalStrut(value);
		    else filler = Box.createVerticalStrut(-value);
		} else filler = Box.createRigidArea(YoixMakeScreen.javaDimension(width, height));
	    } else {
		if (orientation == YOIX_VERTICAL) {
		    filler = Box.createVerticalGlue();
		    weighty = 1.0;
		} else {
		    filler = Box.createHorizontalGlue();
		    weightx = 1.0;
		}
	    }
	}
	if (weights != null && weights.length >= 2) {
	    if (weightx != 0 && weights[0] <= 0)
		weights[0] = weightx;
	    if (weighty != 0 && weights[1] <= 0)
		weights[1] = weighty;
	}
	return(filler);
    }


    final Dimension
    pickLayoutSize(YoixObject obj, String name, Dimension size) {

	Dimension  defaultsize;

	//
	// Currently only used by Swing components to set their preferred,
	// minimum, and maximum sizes. AWT components didn't have a way to
	// set those values until Java 1.5, so we'll leve things be until
	// we drop support for Java 1.4.
	//

	if (obj != null && obj.notNull() && name != null) {
	    defaultsize = size;
	    size = YoixMakeScreen.javaDimension(obj);
	    if (size.width <= 0 || size.height <= 0) {
		if (size.width < 0 || (size.width == 0 && name.equals(N_PREFERREDSIZE)))
		    size.width = (defaultsize != null) ? defaultsize.width : 0;
		if (size.height < 0 || (size.height == 0 && name.equals(N_PREFERREDSIZE)))
		    size.height = (defaultsize != null) ? defaultsize.height : 0;
	    }
	}

	return(size);
    }


    final InputEvent
    pickTransferTrigger(InputEvent event) {

	MouseEvent  original;

	//
	// We seem to have some problems in TransferHandler.exportAsDrag()
	// when we use Java 1.4.X and end up in our builtin version of that
	// method (because a script called it). The problem seems to happen
	// because our code builds a Java event from a Yoix representation
	// of an event, and Java's low level drag and drop machinery doesn't
	// approve of the event that we build. Using the original event in
	// YoixBodyTransferHandler.exportAsDrag(), when appropriate, seems
	// to fix the 1.4.X behavior, and right now that's the only reason
	// this method is needed. Java 1.5.0 doesn't seem need to need this
	// kludge, so one day we should be able to toss transfertrigger and
	// this method.
	//

	if ((original = transfertrigger) != null) {
	    if (event instanceof MouseEvent) {
		if (event.getID() == original.getID()) {
		    //
		    // Decided not to compare much, at least for now, but
		    // that could change.
		    //
		    event = original;
		}
	    }
	}
	return(event);
    }


    final synchronized void
    removeAll() {

	YoixBodyComponent  objcomp;
	YoixObject         components;
	YoixObject         container;
	YoixObject         obj;
	Container          pane;
	int                n;
	int                m;

	//
	// NOTE - the code used to update the N_COMPONENTS lists may not
	// be completely thread-safe. Good enough for now, but it needs a
	// careful look - later!! Probably have to coordinate the setting
	// of N_ROOT here and in addTo(). Consider abort() in addTo() when
	// N_ROOT for a Container is not NULL??
	//

	if ((pane = getContainer(this.peer)) != null) {
	    specialRemoveAll(pane);
	    components = data.getObject(N_COMPONENTS);
	    if (components != null && components.notNull()) {
		VM.pushAccess(LRW_);
		data.put(N_COMPONENTS, YoixObject.newDictionary(), false);
		for (n = 0; n < components.length(); n++) {
		    if (components.defined(n)) {
			obj = components.getObject(n);
			if ((objcomp = (YoixBodyComponent)obj.body()) != this) {
			    obj.put(N_ROOT, YoixObject.newNull(), true);
			    if (((YoixBodyComponent)(obj.body())).isContainer()) {
				if ((container = obj.getObject(N_COMPONENTS)) != null) {
				    for (m = 0; m < container.length(); m++) {
					if (container.defined(m)) {
					    if (objcomp != ((YoixBodyComponent)container.getObject(m).body())) {
						container.getObject(m).put(N_ROOT, obj, true);
						n++;
					    }
					}
				    }
				}
			    }
			}
		    }
		}
		VM.popAccess();
	    }
	}
    }


    void
    removeAllListeners() {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp != null)
	    removeListeners(eventmask);
    }


    int
    removeListeners(Object comp, int mask) {

	int  missed;
	int  listener;
	int  bit;

	missed = 0;

	for (bit = 1; mask != 0 && bit != NEXTLISTENER; bit <<= 1) {
	    switch (listener = (mask & bit)) {
		case ADJUSTMENTLISTENER:
		    if (comp instanceof Adjustable)
			((Adjustable)comp).removeAdjustmentListener(this);
		    else missed |= listener;
		    break;

		case COMPONENTLISTENER:
		    if (comp instanceof Component)
			((Component)comp).removeComponentListener(this);
		    else missed |= listener;
		    break;

		case DRAGGESTURELISTENER:
		    if (comp instanceof Component)
			removeDragGestureListener(this);
		    else missed |= listener;
		    break;

		case DROPTARGETLISTENER:
		    if (comp instanceof Component)
			removeDropTargetListener(this);
		    else missed |= listener;
		    break;

		case FOCUSLISTENER:
		    if (comp instanceof Component)
			((Component)comp).removeFocusListener(this);
		    else missed |= listener;
		    break;

		case INVOCATIONRUNLISTENER:
		    // nothing to do
		    break;

		case ITEMLISTENER:
		    if (comp instanceof ItemSelectable)
			((ItemSelectable)comp).removeItemListener(this);
		    else missed |= listener;
		    break;

		case KEYLISTENER:
		    if (comp instanceof Component)
			((Component)comp).removeKeyListener(this);
		    else missed |= listener;
		    break;

		case MOUSELISTENER:
		    if (comp instanceof Component)
			((Component)comp).removeMouseListener(this);
		    else missed |= listener;
		    break;

		case MOUSEMOTIONLISTENER:
		    if (comp instanceof Component)
			((Component)comp).removeMouseMotionListener(this);
		    else missed |= listener;
		    break;

		case MOUSEWHEELLISTENER:
		    if (comp instanceof Component)
			((Component)comp).removeMouseWheelListener(this);
		    else missed |= listener;
		    break;

		case WINDOWLISTENER:
		    if (comp instanceof Window)
			((Window)comp).removeWindowListener(this);
		    else missed |= listener;
		    break;

	        default:
		    missed |= listener;
		    break;
	    }
	    mask &= ~bit;
	}

	return(missed);
    }


    synchronized int
    replaceText(int offset, int length, String str, boolean adjust, ArrayList undo) {

	YoixObject  obj;
	boolean     trim;
	String      dest;
	String      text;
	int         delta = 0;

	if (canTextChange()) {
	    if ((obj = getField(N_TEXT, null)) != null) {
		if (obj.isString()) {
		    trim = data.getBoolean(N_AUTOTRIM, false);
		    dest = obj.stringValue();
		    text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		    delta = text.length() - dest.length();
		    if (dest.equals(text) == false)
			setField(N_TEXT, YoixObject.newString(text));
		}
	    }
	}
	return(delta);
    }


    final void
    requestFirstFocus() {

	YoixBodyComponent  body;
	YoixObject         components;
	YoixObject         container;
	YoixObject         child;
	Object             comp;
	int                length;
	int                n;

	//
	// Relatively new and not thoroughly tested. The goal is to try
	// to start with the focus set to the last Component mentioned
	// in a Window's N_COMPONENTS disctionary that asks for it by
	// setting it's N_REQUESTFOCUS field to TRUE. We currently let
	// setFirstFocus() restrict this to Windows.
	//

	if (firstfocus) {
	    comp = this.peer;		// snapshot - just to be safe
	    if (comp instanceof Component) {
		if (((Component)comp).isVisible()) {
		    if (data.getBoolean(N_REQUESTFOCUS, false)) {
			if (data.getBoolean(N_REQUESTFOCUSENABLED, true))
			    ((Component)comp).requestFocus();
		    }
		    if (isContainer(comp)) {
			components = data.getObject(N_COMPONENTS);
			length = components.length();
			for (n = length - 1; n >= 0; n--) {
			    if (components.defined(n)) {
				child = components.getObject(n);
				if (((YoixBodyComponent)child.body()) != this) {
				    body = (YoixBodyComponent)child.body();
				    comp = child.getManagedObject();
				    if (((Component)comp).isFocusable() && ((Component)comp).isShowing() && ((Component)comp).isEnabled()) {
					if (body.data.getBoolean(N_REQUESTFOCUS, false)) {
					    if (body.data.getBoolean(N_REQUESTFOCUSENABLED, true)) {
						((Component)comp).requestFocus();
						break;
					    }
					}
				    }
				}
			    }
			}
		    }
		    setFirstFocus(false);
		}
	    }
	}
    }


    void
    setBackground(Object comp, YoixObject obj) {

	YoixBodyComponent  body;
	YoixObject         components;
	YoixObject         container;
	YoixObject         child;
	YoixObject         background;
	Color              color;
	Color              childcolor;
	int                m;
	int                n;

	if (comp instanceof Component) {
	    color = pickBackground(obj, null);
	    ((Component)comp).setBackground(color);
	    if (comp instanceof JMenu)
		((JMenu)comp).getPopupMenu().setBackground(color);
	    syncTabProperty((Component)comp, N_BACKGROUND, color);
	    if (isContainer(comp)) {
		components = data.getObject(N_COMPONENTS);
		for (n = 0; n < components.length(); n++) {
		    if (components.defined(n)) {
			child = components.getObject(n);
			if (((YoixBodyComponent)child.body()) != this) {
			    comp = child.getManagedObject();
			    if (comp instanceof Component) {
				body = (YoixBodyComponent)child.body();
				background = body.data.getObject(N_BACKGROUND);
				if (background == null || background.isNull()) {
				    if (obj.isNull())
					childcolor = body.pickBackground(background, null);
				    else childcolor = body.pickBackground(background, color);
				} else childcolor = body.pickBackground(background, null);
				if (childcolor == null)	// just in case
				    childcolor = color;
				((Component)comp).setBackground(childcolor);
				if (comp instanceof JMenu)
				    ((JMenu)comp).getPopupMenu().setBackground(childcolor);
				syncTabProperty((Component)comp, N_BACKGROUND, childcolor);
				if (((YoixBodyComponent)(child.body())).isContainer()) {
				    if (background != null && background.notNull()) {
					container = child.getObject(N_COMPONENTS);
					for (m = 1; m < container.length(); m++)
					    n += container.defined(m) ? 1 : 0;
				    }
				}
			    }
			}
		    }
		}
	    }
	}
    }


    final void
    setBackgroundHints(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceDrawable) {
	    if (((YoixInterfaceDrawable)comp).isTileable())
		((YoixInterfaceDrawable)comp).setBackgroundHints(obj.intValue());
	}
    }


    final void
    setBackgroundImage(Object comp, YoixObject obj) {

	Image  image;

	if (comp instanceof YoixInterfaceDrawable) {
	    if (((YoixInterfaceDrawable)comp).isTileable()) {
		if (obj.isImage() || obj.isString() || obj.isNull()) {
		    image = YoixMake.javaImage(obj);
		    ((YoixInterfaceDrawable)comp).setBackgroundImage(image);
		} else VM.abort(TYPECHECK, N_BACKGROUNDIMAGE);
	    }
	}
    }


    final void
    setCanShape(boolean state) {

	canshape = state;
    }


    final void
    setCompressEvents(Object comp, YoixObject obj) {

	HashMap  events;
	String   name;
	int      n;

	if (comp instanceof Component) {
	    if (obj.notNull()) {
		events = new HashMap();
		if (obj.isArray()) {
		    for (n = 0; n < obj.length(); n++) {
			if ((name = obj.getString(n, null)) != null) {
			    if (jfcExists("EventID", name))
				events.put(name, Boolean.TRUE);
			}
		    }
		} else if (obj.isString()) {
		    name = obj.stringValue();
		    if (jfcExists("EventID", name))
			events.put(name, Boolean.TRUE);
		} else VM.abort(TYPECHECK, N_COMPRESSEVENTS);
		compressevents = (events.size() > 0) ? events : null;
	    } else compressevents = null;
	}
    }


    final void
    setCursor(Object comp, YoixObject obj) {

	YoixBodyComponent  body;
	YoixObject         components;
	YoixObject         container;
	YoixObject         child;
	YoixObject         value;
	boolean            standardcursor;
	Cursor             cursor;
	Cursor             childcursor;
	int                m;
	int                n;

	//
	// Think the try/catch that surround setCursor() calls are only
	// for FileDialogs on Windows that haven't been made visible yet.
	// Is it necessary and/or should we look for a better solution??
	//

	if (comp instanceof Component && !(comp instanceof FileDialog)) {
	    if (obj.isInteger() || obj.isString() || obj.isImage() || obj.isNull()) {
		cursor = YoixMakeScreen.javaCursor(obj, (Component)comp);
		standardcursor = YoixRegistryCursor.isStandardCursor(obj);
		if (cursor == null)	// no longer can happen??
		    cursor = YoixRegistryCursor.getStandardCursor((Component)comp);
		try {
		    ((Component)comp).setCursor(cursor);
		}
		catch(RuntimeException e) {}
		if (isContainer(comp)) {
		    components = data.getObject(N_COMPONENTS);
		    for (n = 0; n < components.length(); n++) {
			if (components.defined(n)) {
			    child = components.getObject(n);
			    if (((YoixBodyComponent)child.body()) != this) {
				comp = child.getManagedObject();
				if (comp instanceof Component) {
				    body = (YoixBodyComponent)child.body();
				    value = body.data.getObject(N_CURSOR);		// likely never null
				    if (YoixRegistryCursor.isStandardCursor(value) || value == null) {
					if (standardcursor)
					    childcursor = YoixRegistryCursor.getCursor(value, comp);
					else childcursor = cursor;
				    } else childcursor = YoixRegistryCursor.getCursor(value, comp);
				    try {
					((Component)comp).setCursor(childcursor);
				    }
				    catch(RuntimeException e) {}
				    if (((YoixBodyComponent)(child.body())).isContainer()) {
					if (YoixRegistryCursor.notStandardCursor(value) && value != null) {
					    container = child.getObject(N_COMPONENTS);
					    for (m = 1; m < container.length(); m++)
						n += container.defined(m) ? 1 : 0;
					}
				    }
				}
			    }
			}
		    }
		}
	    } else VM.abort(TYPECHECK, N_CURSOR);
	}
    }


    final void
    setDecorationStyle(Object comp, YoixObject obj) {

	int  style;

	if (comp instanceof Frame || comp instanceof Dialog) {
	    style = obj.intValue();
	    try {
		if (comp instanceof Frame) {
		    ((Frame)comp).setUndecorated(style != 1);
		    if (comp instanceof JFrame) {
			((JFrame)comp).getRootPane().setWindowDecorationStyle(
			     style > 1 ? JRootPane.FRAME : JRootPane.NONE
			);
		    }
		} else {
		    ((Dialog)comp).setUndecorated(style != 1);
		    if (comp instanceof JDialog) {
			((JDialog)comp).getRootPane().setWindowDecorationStyle(
			    style > 1 ? JRootPane.PLAIN_DIALOG : JRootPane.NONE
			);
		    }
		}
	    }
	    catch(IllegalComponentStateException e) {}
	}
    }


    final void
    setFirstFocus(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceWindow)
	    setFirstFocus(obj.booleanValue());
    }


    final void
    setFocusable(Object comp, YoixObject obj) {

	//
	// Calling Component.setFocusable() changes another variable in the
	// Component class that affects focus traveral and there's no way
	// undo that change. To avoid unintended focus traversal changes we
	// initialize N_FOCUSABLE in components to NULL which means skip the
	// Component.setFocusable() call.
	//

	if (comp instanceof Component) {
	    if (obj.notNull()) {
		if (obj.isNumber())
		    ((Component)comp).setFocusable(obj.booleanValue());
		else VM.abort(TYPECHECK, N_FOCUSABLE);
	    }
	}
    }


    final void
    setFont(Object comp, YoixObject obj) {

	YoixBodyComponent  body;
	YoixObject         components;
	YoixObject         container;
	YoixObject         child;
	Font               font;
	Font               childfont;
	int                m;
	int                n;

	if (comp instanceof Component || comp instanceof MenuComponent) {
	    if (obj.isNull()) {
		if (comp instanceof YoixInterfaceWindow) {
		    obj = VM.getObject(N_FONT);
		    //
		    // A recent addition (11/5/05) designed to make sure
		    // that YoixMakeScreen.javaFont() font adjustments
		    // (e.g., like the ones triggered by +f command line
		    // option) aren't omitted when obj hasn't explicitly
		    // been set.
		    //
		    if (obj.isNull()) {
			if ((font = YoixMakeScreen.javaFont(obj)) != null)
			    obj = YoixMake.yoixFont(font);
		    }
		}
	    }
	    if (obj.notNull()) {
		if (obj.isString() || obj.isFont()) {
		    if ((font = YoixMakeScreen.javaFont(obj)) != null) {
			if (comp instanceof MenuComponent)
			    ((MenuComponent)comp).setFont(font);
			else ((Component)comp).setFont(font);
			if (isContainer(comp)) {
			    components = data.getObject(N_COMPONENTS);
			    for (n = 0; n < components.length(); n++) {
				if (components.defined(n)) {
				    child = components.getObject(n);
				    if (((YoixBodyComponent)child.body()) != this) {
					body = (YoixBodyComponent)child.body();
					comp = child.getManagedObject();
					obj = body.data.getObject(N_FONT);
					if (obj != null && obj.notNull()) {
					    if (((YoixBodyComponent)(child.body())).isContainer()) {
						container = child.getObject(N_COMPONENTS);
						for (m = 1; m < container.length(); m++)
						    n += container.defined(m) ? 1 : 0;
					    } else {
						childfont = YoixMakeScreen.javaFont(obj);
						if (childfont == null)
						    childfont = font;
						((Component)comp).setFont(childfont);
					    }
					} else ((Component)comp).setFont(font);
				    }
				}
			    }
			}
		    }
		} else VM.abort(TYPECHECK, N_FONT);
	    }
	}
    }


    void
    setForeground(Object comp, YoixObject obj) {

	YoixBodyComponent  body;
	YoixObject         components;
	YoixObject         container;
	YoixObject         child;
	YoixObject         foreground;
	Color              color;
	Color              childcolor;
	int                m;
	int                n;

	if (comp instanceof Component) {
	    ////color = YoixMake.javaColor(obj);
	    color = pickForeground(obj, null);
	    ((Component)comp).setForeground(color);
	    syncTabProperty((Component)comp, N_FOREGROUND, color);
	    if (isContainer(comp)) {
		components = data.getObject(N_COMPONENTS);
		for (n = 0; n < components.length(); n++) {
		    if (components.defined(n)) {
			child = components.getObject(n);
			if (((YoixBodyComponent)child.body()) != this) {
			    comp = child.getManagedObject();
			    if (comp instanceof Component) {
				body = (YoixBodyComponent)child.body();
				foreground = body.data.getObject(N_FOREGROUND);
				if (foreground == null || foreground.isNull()) {
				    if (obj.isNull())
					childcolor = body.pickForeground(foreground, null);
				    else childcolor = body.pickForeground(foreground, color);
				} else childcolor = body.pickForeground(foreground, null);
				if (childcolor == null)
				    childcolor = color;
				((Component)comp).setForeground(childcolor);
				syncTabProperty((Component)comp, N_FOREGROUND, childcolor);
				if (((YoixBodyComponent)(child.body())).isContainer()) {
				    if (foreground != null && foreground.notNull()) {
					container = child.getObject(N_COMPONENTS);
					for (m = 1; m < container.length(); m++)
					    n += container.defined(m) ? 1 : 0;
				    }
				}
			    }
			}
		    }
		}
	    }
	}
    }


    final void
    setFrontToBack(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (isContainer(comp))
		doLayout(data.getObject(N_LAYOUT), obj.booleanValue(), true);
	}
    }


    final void
    setFullScreen(Object comp, YoixObject obj) {

	GraphicsDevice  screen;
	boolean         state;

	//
	// This should only be called if the component has been initialized
	// but right now there's now way to check the boolean. We'll add it
	// later - if necessary.
	//
	// NOTE - Java documentation suggests calling
	//
	//     Component.enableInputMethods(false);
	//
	// when full screen windows are created, probably to make it harder to
	// steal keystrokes, but right now we don't do it. Might be something
	// to consider before the next release, but it might also be really
	// inconvenient - they obviously decided not to do it. See
	//
	//     http://java.sun.com/docs/books/tutorial/extra/fullscreen/
	//
	// for more info.
	//

	if (comp instanceof Window) {
	    state = obj.booleanValue();
	    data.putInt(N_FULLSCREEN, state);	// our setVisible() may use this
	    if (state) {
		setField(N_VISIBLE, obj);
		if ((screen = getGraphicsDeviceFromScreen()) != null) {
		    if (comp != screen.getFullScreenWindow())
			screen.setFullScreenWindow((Window)comp);
		}
	    } else {
		if ((screen = getGraphicsDeviceFromScreen()) != null) {
		    if (comp == screen.getFullScreenWindow())
			screen.setFullScreenWindow(null);
		}
	    }
	}
    }


    final synchronized void
    setGraphics(Object comp, YoixObject obj) {

	YoixBodyGraphics  body;

	if (comp instanceof Component) {
	    if (isDrawable()) {
		if (obj.notNull()) {
		    body = (YoixBodyGraphics)obj.body();
		    body.setOwner(getContext());
		    data.put(N_GRAPHICS, obj, false);
		    data.get(N_GRAPHICS).setAccess(LR__);
		}
	    }
	}
    }


    final void
    setLayout(Object comp, YoixObject obj) {

	YoixObject  oldcomponents;
	YoixObject  newcomponents;
	YoixObject  components;
	YoixObject  comps;
	YoixObject  comps2;
	YoixObject  root;
	YoixObject  child;
	YoixObject  container;
	YoixObject  values[];
	String      tag;
	int         offset;
	int         index;
	int         count;

	if (comp instanceof Component) {
	    if (isContainer(comp)) {
		oldcomponents = data.getObject(N_COMPONENTS);
		doLayout(obj, data.getBoolean(N_FRONTTOBACK, true), false);
		newcomponents = data.getObject(N_COMPONENTS);
		//
		// This code handle the case where a new layout is added to a
		// container at least one level removed from the root container
		// in an existing layout hierarchy; without this code the
		// changes do not propagate up and root.components gets out
		// of sync. See Tests/changelayout.yx. Added: 10/9/2008
		//
		// NOTE - the new implementation was added on 3/1/11 and can
		// really help performance when there are lots of containers
		// that need their components dictionaries updated.
		//
		if ((root = data.getObject(N_ROOT)) != null) {
		    if (root.notNull() && root.body() != this) {
			synchronized(root.body()) {		// is this really needed??
			    tag = data.getString(N_TAG);
			    if (USENEWSETLAYOUT) {
				VM.pushAccess(LRW_);
				if ((values = newcomponents.getValues()) != null) {
				    for (index = 0; index < values.length; index++) {
					if (values[index] != null)
					    values[index].put(N_ROOT, root, true);
				    }
				}

				if ((components = root.getObject(N_COMPONENTS)) != null) {
				    if ((index = components.definedAt(tag) - 1) >= 0) {
					if ((values = components.getValues()) != null) {
					    count = oldcomponents.length();
					    for (; index >= 0; index--) {
						if ((child = values[index]) != null) {
						    if ((components = child.getObject(N_COMPONENTS)) != null) {
							if ((offset = components.definedAt(tag)) >= 0) {
							    if ((components = replaceComponents(components, offset, count, newcomponents)) != null)
								child.putObject(N_COMPONENTS, components);
							}
						    }
						}
					    }
					}
				    }
				}
				VM.popAccess();
			    } else {
				if ((comps = root.getObject(N_COMPONENTS)) != null) {
				    index = comps.hash(tag);
				    while (--index >= 0) {
					if ((child = comps.getObject(index)) != null && (comps2 = child.getObject(N_COMPONENTS)) != null) {
					    if (comps2.hash(tag) >= 0) {
						child.put(N_LAYOUT, child.getObject(N_LAYOUT));
						break;
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


    final void
    setLayoutManager(Object comp, YoixObject obj) {

	LayoutManager  layout;
	Container      pane;

	//
	// NOTE - think we should call our own removeAll() before actually
	// changing the Container's layout manager. Probably do it in the
	// synchronized block of code right before setLayout(). If we skip
	// removeAll() we could end up with unexpected Java errors? Easy
	// change, but it needs to be thoroughly tested so I'll wait.
	//

	if ((pane = getContainer(comp)) != null) {
	    if (obj.notNull()) {	// should eliminate FileDialog and ScrollPane
		layout = null;
		switch (obj.getInt(N_TYPE, YOIX_INVALIDLAYOUT)) {
		    case YOIX_BORDERLAYOUT:
			layout = new BorderLayout(
			    YoixMakeScreen.javaDistance(obj.getObject(N_HGAP)),
			    YoixMakeScreen.javaDistance(obj.getObject(N_VGAP))
			);
			break;

		    case YOIX_BOXLAYOUT:
			layout = new BoxLayout(
			    pane,
			    jfcInt("BoxLayout", obj, N_ORIENTATION)
			);
			break;

		    case YOIX_CARDLAYOUT:
			layout = new CardLayout(
			    YoixMakeScreen.javaDistance(obj.getObject(N_HGAP)),
			    YoixMakeScreen.javaDistance(obj.getObject(N_VGAP))
			);
			break;

		    case YOIX_CUSTOMLAYOUT:
			VM.abort(UNIMPLEMENTED, T_CUSTOMLAYOUT);
			break;

		    case YOIX_FLOWLAYOUT:
			layout = new FlowLayout(
			    jfcInt("FlowLayout", obj, N_ALIGNMENT),
			    YoixMakeScreen.javaDistance(obj.getObject(N_HGAP)),
			    YoixMakeScreen.javaDistance(obj.getObject(N_VGAP))
			);
			break;

		    case YOIX_GRIDBAGLAYOUT:
			layout = new GridBagLayout();
			break;

		    case YOIX_GRIDLAYOUT:
			layout = new YoixAWTGridLayout(
			    obj.getInt(N_MODEL, 0),
			    obj.getInt(N_ROWS, 1),
			    obj.getInt(N_COLUMNS, 0),
			    YoixMakeScreen.javaDistance(obj.getObject(N_HGAP)),
			    YoixMakeScreen.javaDistance(obj.getObject(N_VGAP)),
			    obj.getInt(N_ORIENTATION, YOIX_HORIZONTAL),
			    obj.getBoolean(N_USEALL, true)
			);
			break;

		    case YOIX_SPRINGLAYOUT:
			layout = new SpringLayout();
			break;

		    case YOIX_INVALIDLAYOUT:
			if (comp instanceof JScrollPane) {
			    break;
			}
			// else fall through...

		    default:
			VM.die(INTERNALERROR);
			break;
		}

		synchronized(this) {
		    //
		    // Test that skips FileDialog and ScrollPane should be
		    // unnecessary - both should have NULL managers, so we
		    // should never get here!
		    //
		    if (!(pane instanceof FileDialog || pane instanceof ScrollPane))
			pane.setLayout(layout);
		    if (data.getBoolean(N_VALIDATE, false))
			pane.validate();
		}
	    }
	}
    }


    final synchronized void
    setMenuBar(Object comp, YoixObject obj) {

	YoixBodyComponent  body;
	Object             menubar;
	boolean            visible;

	//
	// An AWT MenuBar isn't a Component, which means it really can't
	// tell if it's visible or not, so we use the value currently
	// assigned to N_VISIBLE. Things could easily be improved if we
	// only dealt with Swing because a JMenuBar is a JComponent, so
	// in that case changeMenuBar() always get the value.
	// 

	if (comp instanceof YoixInterfaceMenuBar) {
	    if ((menubar = obj.getManagedObject()) != null) {
		body = (YoixBodyComponent)obj.body();
		visible = body.data.getBoolean(N_VISIBLE);
	    } else visible = false;
	    if (((YoixInterfaceMenuBar)comp).changeMenuBar(menubar, visible, data.getBoolean(N_VALIDATE))) {
		if (obj.notNull())
		    ((YoixBodyComponent)obj.body()).setMenuBarOwner(this);
		updateRoot(data.getObject(N_MENUBAR), null);
		updateRoot(obj, getContext());
	    }
	}
    }


    final void
    setNextCard(Object comp, YoixObject obj) {

	LayoutManager  layout;
	Container      pane;
	String         tag;

	if (isContainer(comp)) {
	    if ((pane = getContainer(comp)) != null) {
		layout = pane.getLayout();
		if (layout instanceof CardLayout) {
		    if (obj.isNumber()) {
			if (obj.intValue() > 0)
			    ((CardLayout)layout).next(pane);
			else if (obj.intValue() < 0)
			    ((CardLayout)layout).previous(pane);
			else if (obj.doubleValue() == Double.NEGATIVE_INFINITY)
			    ((CardLayout)layout).first(pane);
			else if (obj.doubleValue() == Double.POSITIVE_INFINITY)
			    ((CardLayout)layout).last(pane);
		    } else if (obj.isString())
			((CardLayout)layout).show(pane, obj.stringValue());
		    else if ((tag = obj.getString(N_TAG)) != null)
			((CardLayout)layout).show(pane, tag);
		}
	    }
	}
    }


    final void
    setPacked(boolean state) {

	packed = state;
    }


    final void
    setPaint(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (comp instanceof YoixInterfaceDrawable) {
		if (obj.isNull() || obj.callable(1) || obj.callable(0))
		    ((YoixInterfaceDrawable)comp).setPaint(obj);
		else VM.abort(TYPECHECK, N_PAINT);
	    }
	}
    }


    final void
    setParent(Object comp, YoixObject obj) {

	YoixObject  owner;

	//
	// Be careful if you make changes here - another thread could
	// force dispose() to run, so the values assigned to parent and
	// peer can change to null.
	//

	if (comp instanceof YoixInterfaceWindow) {
	    data.put(N_PARENT, YoixObject.newNull());
	    owner = this.parent;		// snapshot - just to be safe
	    if (owner != null) {
		((YoixBodyComponent)owner.body()).childrenRemove(this);
		this.parent = null;
	    }
	    if (obj.isWindow() && obj.notNull()) {
		if (obj.body() != this) {
		    this.parent = obj;
		    ((YoixBodyComponent)obj.body()).childrenAdd(this);
		}
	    }
	}
    }


    final synchronized void
    setPopup(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (isPopupOwner()) {
		if (obj.isPopupMenu() || obj.isNull()) {
		    if (obj.notNull())
			((YoixBodyComponent)obj.body()).setPopupOwner(this);
		    updateRoot(data.getObject(N_POPUP), null);
		    updateRoot(obj, getContext());
		}
	    }
	}
    }


    final void
    setRequestFocus(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (!(comp instanceof FileDialog)) {
		if (obj.booleanValue())
		    ((Component)comp).requestFocus();
		else if (((Component)comp).isFocusOwner())
		    ((Component)comp).transferFocus();
	    }
	}
    }


    final void
    setResizable(Object comp, YoixObject obj) {

	if (comp instanceof Dialog)
	    ((Dialog)comp).setResizable(obj.booleanValue());
	else if (comp instanceof Frame)
	    ((Frame)comp).setResizable(obj.booleanValue());
	else if (comp instanceof JInternalFrame)
	    ((JInternalFrame)comp).setResizable(obj.booleanValue());
    }


    final void
    setScreen(Object comp, YoixObject obj) {

	Object  screens;
	int     index;

	if (comp instanceof Window) {
	    if (obj != null && obj.notNull()) {
		if ((obj = getScreenForObject(obj, null)) != null)
		    data.putObject(N_SCREEN, obj);
		else VM.abort(TYPECHECK, N_SCREEN);
	    }
	}
    }


    final void
    setShape(Object comp, YoixObject obj) {

	YoixBodyPath  path;
	Object        result;
	Area          area;

	//
	// Using reflection to access setWindowShape() because it's not in
	// 1.5 or even all versions of 1.6 and javac also complained about
	// accessing com.sun.awt.AWTUtilities. Eventually may also want to
	// look for setWindowShape() in other classes when the first try
	// fails.
	//

	if (canshape && comp instanceof Window) {
	    if (obj.isPath()) {
		path = (YoixBodyPath)obj.body();
		path.paint(((Component)comp).getBounds());
		if ((area = path.getCurrentArea(YOIX_WIND_NON_ZERO)) != null && area.isEmpty())
		    area = null;
	    } else area = null;
	    result = YoixReflect.invoke(
		"com.sun.awt.AWTUtilities",
		"setWindowShape",
		new Object[] {comp, area},
		new Class[] {Window.class, Shape.class},
		Boolean.FALSE
	    );
	}
    }


    final void
    setTag(Object comp, YoixObject obj) {

	if (obj.isNull()) {
	    if (data.defined(N_TAG)) {
		if ((obj = data.getObject(N_TAG)) == null || obj.isNull() || obj.getAccess() != LR__) {
		    obj = YoixObject.newString("_" + YoixMisc.nextID());
		    obj.setAccessBody(LR__);
		    data.put(N_TAG, obj);
		}
	    }
	}
    }


    final void
    setValidate(Object comp, YoixObject obj) {

	Container  pane;

	if (comp instanceof Component) {
	    if (!(comp instanceof FileDialog)) {
		if ((pane = getContainer(comp)) != null) {
		    if (obj.booleanValue()) {
			pane.invalidate();
			pane.validate();
		    }
		}
	    }
	}
    }


    final void
    setWindow(boolean state) {

	if (iswindow = state)
	    children = new Vector(0);
    }


    final static void
    syncDispatchThread(int pause) {

	if (pause > 0) {
	    try {
		Thread.sleep(pause);
	    }
	    catch(InterruptedException e) {}
	}

	if (EventQueue.isDispatchThread() == false) {
	    try {
		EventQueue.invokeAndWait(new YoixAWTInvocationEvent());
	    }
	    catch(InterruptedException e) {}
	    catch(InvocationTargetException e) {}
	}

	if (pause > 0) {
	    try {
		Thread.sleep(pause);
	    }
	    catch(InterruptedException e) {}
	}
    }


    final void
    syncMenus(String name, Object value) {

	YoixObject  obj;
	Object      body;
	Object      comp;

	comp = peer;		// snapshot - just to be safe

	if (comp instanceof YoixInterfaceWindow) {
	    if (comp instanceof YoixInterfaceMenuBar) {
		if ((obj = data.getObject(N_MENUBAR)) != null && obj.notNull()) {
		    body = obj.body();
		    if (body instanceof YoixBodyComponent)
			((YoixBodyComponent)body).syncMenuProperties(this);
		}
		if ((obj = data.getObject(N_POPUP)) != null && obj.notNull()) {
		    body = obj.body();
		    if (body instanceof YoixBodyComponent)
			((YoixBodyComponent)body).syncMenuProperties(this);
		}
	    }
	}
    }


    final void
    syncTabProperty(Component comp, String name, boolean state) {

	Container  parent = comp.getParent();

	if (parent instanceof YoixSwingJTabbedPane)
	    ((YoixSwingJTabbedPane)parent).syncTabProperty(comp, name, new Boolean(state));
    }


    final void
    syncTabProperty(Component comp, String name, Object value) {

	Container  parent = comp.getParent();

	if (parent instanceof YoixSwingJTabbedPane)
	    ((YoixSwingJTabbedPane)parent).syncTabProperty(comp, name, value);
    }


    static void
    updateRoot(YoixObject child, YoixObject root) {

	YoixBodyComponent  childcomp;
	YoixObject         components;
	int                n;

	//
	// NOTE - this is needed by YoixBodyComponentSwing.doMenuLayout()
	// to force root to be updated in child menus after we store a new
	// value in a menu's items field. The method originally was private
	// but was changed on 2/17/08 to address the root issue that caused
	// some annoying inconsistencies.
	//

	if (child != null) {
	    if (child.isComponent() && child.defined(N_ROOT)) {
		VM.pushAccess(LRW_);
		if (root == null || root.isNull()) {
		    child.put(N_ROOT, YoixObject.newNull());
		    root = child;
		} else child.put(N_ROOT, root, true);
		childcomp = (YoixBodyComponent)child.body();
		if (childcomp.isContainer()) {
		    if ((components = child.getObject(N_COMPONENTS)) != null) {
			for (n = 0; n < components.length(); n++) {
			    if (components.defined(n)) {
				child = components.getObject(n);
				if (((YoixBodyComponent)child.body()) != childcomp)
				    child.put(N_ROOT, root, true);
			    }
			}
		    }
		}
		VM.popAccess();
	    }
	}
    }


    static void
    updateUI() {

	YoixBodyComponent  body;
	Enumeration        enm;
	Hashtable          active;
	Object             element;
	Object             comp;

	//
	// Called to update the UI for all visible windows when the look and
	// feel or its associated theme changes. Windows that aren't visible
	// will be updated right before they're made visible because they'll
	// have a timestamp that's older than lookandfeelchange.
	//
	// NOTE - our old implementation occasionally had trouble with some
	// NullPointerExceptions in low level Java, so it put
	//
	//	locker = new JButton();
	//	synchronized(locker.getTreeLock()) {
	//	    ...
	//	}
	//
	// around the code that's left. Adding threadsafe support to Swing
	// components could introduce a deadlock if this method was called
	// from outside the event thread. To get around the problem we made
	// sure that methods in YoixBodyUIMananger.java that could call us
	// do so from the event thread, so we no longer need the AWT tree
	// lock. Perhaps a somewhat better solution would be to make sure
	// this method only ran in the event thread, however that was an
	// approach that didn't quite fit the mechanism that we currently
	// use (i.e., handleRun()) although it probably wouldn't be hard
	// address. We may investigate later on - this is sufficient for
	// now since YoixBodyUIMananger is the only class that calls us.
	//

	lookandfeelchange = System.currentTimeMillis();

	for (enm = activewindows.elements(); enm.hasMoreElements(); ) {
	    if ((element = enm.nextElement()) != null) {
		if (element instanceof YoixBodyComponent) {
		    body = (YoixBodyComponent)element;
		    comp = body.getManagedObject();
		    if (comp instanceof Component)
			SwingUtilities.updateComponentTreeUI((Component)comp);
		    body.updateUIFields();
		}
	    }
	}
    }


    final void
    updateUIFields() {

	//
	// Called to update the fields that explicilty look at UIMananger
	// values. For example, pickUIBackground() and pickUIForeground()
	// do the dirty work for the N_BACKGROUND and N_FOREGROUND fields.
	// If the set of fields changes (e.g., N_FONT may be added) make
	// sure those changes are reflected here.
	// 

	setField(N_BACKGROUND);
	setField(N_FOREGROUND);
    }


    final void
    validateRoot(Object comp) {

	YoixObject  obj;  
	Object      root;

	if (comp instanceof Component) {
	    ((Component)comp).invalidate();
	    if ((obj = data.getObject(N_ROOT)) != null) {
		root = obj.getManagedObject();
		if (root instanceof Container) {
		    if (((Container)root).isVisible())
			((Container)root).validate();
		}
	    }
	}
    }


    static void
    windowActivate(YoixBodyComponent body, Object comp) {

	if (comp instanceof Window) {
	    if (body.windowupdated <= lookandfeelchange) {
		body.windowupdated = System.currentTimeMillis();
		SwingUtilities.updateComponentTreeUI((Component)comp);
		body.updateUIFields();
	    }
	    activewindows.put(body, body);
	}
    }


    static int
    windowCount() {

	syncDispatchThread(0);
	return(activewindows.size());
    }


    static void
    windowDeactivate(YoixBodyComponent body) {

	activewindows.remove(body);
    }

    ///////////////////////////////////
    //
    // Abstract Methods
    //
    ///////////////////////////////////

    abstract int
    addListeners(int mask);

    abstract void
    dispose(boolean finalizing);

    abstract Adjustable
    getAdjustable(int orientation);

    abstract Container
    getContainer();

    abstract Container
    getContainer(Object arg);

    abstract boolean
    isContainer();

    abstract boolean
    isContainer(Object arg);

    abstract boolean
    isMenu();

    abstract boolean
    isMenu(Object arg);

    abstract boolean
    isPopupMenu();

    abstract boolean
    isPopupOwner();

    abstract int
    removeListeners(int mask);

    abstract void
    setMenuBarOwner(YoixBodyComponent owner);

    abstract void
    setPopupOwner(YoixBodyComponent owner);

    abstract void
    specialAddToProcessing(Object comp, Container pane, YoixObject child, Object constraint, int index);

    abstract boolean
    specialLayout(YoixObject obj, Object comp, Container pane, YoixObject added, boolean fronttoback);

    abstract void
    specialRemoveAll(Container pane);

    abstract void
    syncMenuProperties(YoixBodyComponent owner);

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addDragGestureListener(Component comp, DragGestureListener listener) {

	//
	// There's currently no way to initialize the source actions, so
	// we accept everything, which means we'll start a drag when any
	// combination of Ctrl and Shift are pressed. Eventually may want
	// some more flexibility, however expliciting setting the actions
	// that dragrecognizer accepts in dragGestureRecognized() affects
	// subsequent events. Not too hard to imagine solutions, but they
	// would need thorough testing - maybe later.
	//

	try {
	    dragsource = new DragSource();
	    dragrecognizer = dragsource.createDefaultDragGestureRecognizer(
		comp,
		DnDConstants.ACTION_COPY|DnDConstants.ACTION_MOVE|DnDConstants.ACTION_LINK,
		listener
	    );
	}
	catch(RuntimeException e) {}
    }


    private void
    addDropTargetListener(Component comp, DropTargetListener listener) {

	//
	// We do nothing, for now anyway, if comp has already been assigned
	// a DropTarget. Done mostly to protect the automatic Swing drag and
	// drop handling that may already have been setup. In other words,
	// this is mainly for AWT, because our Swing components will handle
	// everything in setTransferHandler().
	// 

	if (comp.getDropTarget() == null)
	    new DropTarget(comp, listener);
    }


    private void
    addToBorder(YoixObject add, YoixObject added, boolean fronttoback) {

	YoixObject  child;
	YoixObject  where;
	Object      constraint;
	int         length;
	int         incr;
	int         n;

	length = add.length();
	incr = fronttoback ? 2 : -2;
	n = fronttoback ? 0 : length - 2 + (length%2);

	for (; n >= 0 && n < length; n += incr) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    constraint = null;
		    if ((where = add.getObject(n + 1)) != null) {
			if (where.isInteger())
			    constraint = jfcObject("BorderLayout", where.intValue());
			else if (where.isString())
			    constraint = jfcObject("BorderLayout", where.stringValue());
			else VM.abort(BADVALUE, N_LAYOUT, n + 1);
		    }
		    if (child.isComponent())
			addTo(child, constraint, added, n);
		    else if ((child = pickLayoutComponent(child, constraint)) != null)
			addTo(child, constraint, added, n);
		    else VM.abort(BADVALUE, N_LAYOUT, n);
		}
	    }
	}
    }


    private void
    addToBox(YoixObject add, YoixObject added, boolean fronttoback, BoxLayout layout) {

	YoixObject  child;
	YoixObject  manager;
	Component   component;
	int         orientation;
	int         length;
	int         incr;
	int         n;

	manager = data.getObject(N_LAYOUTMANAGER);
	orientation = manager.getInt(N_ORIENTATION, YOIX_HORIZONTAL);

	length = add.length();
	incr = fronttoback ? 1 : -1;
	n = fronttoback ? 0 : length - 1;

	for (; n >= 0 && n < length; n += incr) {
	    if ((child = add.getObject(n)) != null) {
		if (child.isComponent() == false) {
		    if ((component = pickLayoutFiller(child, orientation)) == null) {
			if ((child = pickLayoutComponent(child)) != null)
			    addTo(child, null, added, n);
			else VM.abort(BADVALUE, N_LAYOUT, n);
		    } else getContainer(peer).add(component);
		} else if (child.notNull())
		    addTo(child, null, added, n);
	    }
	}
    }


    private void
    addToCard(YoixObject add, YoixObject added, boolean fronttoback) {

	YoixObject  child;
	int         length;
	int         incr;
	int         n;

	length = add.length();
	incr = fronttoback ? 1 : -1;
	n = fronttoback ? 0 : length - 1;

	for (; n >= 0 && n < length; n += incr) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    if (child.isComponent())
			addTo(child, child.getString(N_TAG), added, n);
		    else if ((child = pickLayoutComponent(child)) != null)
			addTo(child, child.getString(N_TAG), added, n);
		    else VM.abort(BADVALUE, N_LAYOUT, n);
		}
	    }
	}
    }


    private void
    addToCustom(YoixObject add, YoixObject added, boolean fronttoback) {

	VM.abort(UNIMPLEMENTED, T_CUSTOMLAYOUT);
    }


    private void
    addToGridBag(YoixObject add, YoixObject added, boolean fronttoback) {

	GridBagConstraints  constraints[];
	GridBagConstraints  gbc;
	YoixObject          child;
	YoixObject          component;
	YoixObject          constraint;
	YoixObject          manager;
	YoixObject          hgap;
	YoixObject          vgap;
	YoixObject          obj;
	Component           filler;
	Container           pane;
	boolean             newline;
	boolean             leading;
	double              weights[];
	int                 model;
	int                 orientation;
	int                 columns;
	int                 column;
	int                 period;
	int                 length;
	int                 k;
	int                 n;

	//
	// A new version that recycles and updates GridBagConstraints. We
	// haven't tried to implement everything yet, and there are some
	// pretty obvious extensions that could be added. For example, we
	// could add callback function to the Yoix GridBagLayout that gets
	// called when we stumble into constraints that aren't supported,
	// or perhaps is always called (if it's defined) when we stumble
	// into a constrant that's not a GridBagConstraints. Anyway, lots
	// of room for changes here - we just need to think about it some
	// before thowing them in.
	//
	// NOTE - orientations of YOIX_BOTH and YOIX_NONE only differ in
	// the way they handle consecutive fillers that come right before
	// the components that start a new row. Difference is subtle and
	// we suspect the YOIX_BOTH behavior isn't worth documenting, at
	// least not right now.
	//
	// NOTE - this layout manager now ignores fronttoback because the
	// direction that we pick can now have much larger impact on the
	// final results. We probably should also disable fronttoback in
	// other layout mangers, just for consistency. It was introduced
	// long ago to try to deal with printing issues in very old Java
	// versions (probably 1.1.X) and we suspect it's a kludge that's
	// no longer necessary.
	//

	manager = data.getObject(N_LAYOUTMANAGER);
	hgap = manager.getObject(N_HGAP);
	vgap = manager.getObject(N_VGAP);
	model = manager.getInt(N_MODEL, 0);
	columns = manager.getInt(N_COLUMNS, 0);
	orientation = manager.getInt(N_ORIENTATION, YOIX_HORIZONTAL);

	if (columns >= 0 && model != 0) {
	    if (columns > 0) {
		constraints = new GridBagConstraints[columns];
		for (column = 0; column < columns; column++)
		    constraints[column] = new GridBagConstraints();
		constraints[columns - 1].gridwidth = GridBagConstraints.REMAINDER;
	    } else constraints = new GridBagConstraints[] {new GridBagConstraints()};
	} else constraints = null;

	if (YoixMakeScreen.javaDistance(hgap.doubleValue()) <= 0)
	    hgap = null;

	if (YoixMakeScreen.javaDistance(vgap.doubleValue()) <= 0)
	    vgap = null;

	length = add.length();
	leading = false;
	newline = false;
	pane = getContainer(this.peer);

	for (n = 0, column = 0; n < length; n++) {
	    if ((child = add.getObject(n)) != null) {
		if ((constraint = add.getObject(n + 1)) != null) {
		    if ((gbc = pickGridBagConstraints(constraint, constraints, column)) != null) {
			for (n += 1; n < length - 1; n++) {
			    if (constraint != null && (constraint.isInteger() || constraint.isInsets())) {
				if ((obj = add.getObject(n + 1)) != null && (obj.isInteger() || obj.isInsets())) {
				    if (pickGridBagConstraints(obj, gbc) != gbc) {
					gbc = null;
					break;
				    } else constraint = obj;
				} else break;
			    } else break;
			}
		    } else gbc = (constraints != null) ? constraints[column] : null;
		} else gbc = (constraints != null) ? constraints[column] : new GridBagConstraints();

		if (gbc != null) {
		    if ((component = pickLayoutComponent(child, gbc)) != null || child.isNull()) {
			if (component != null)
			    addTo(component, gbc, added, n);
			newline = (gbc.gridwidth == GridBagConstraints.REMAINDER);
			leading = (orientation == YOIX_BOTH) ? newline : false;

			if (constraints != null) {
			    //
			    // We don't save gbc if it came from a Yoix
			    // GridBagConstraints. You use a Dictionary
			    // to change fields, like weightx or weighty,
			    // and have the changes saved. Other fields,
			    // like fill and anchor, can be set by single
			    // integer arguments and in those cases the
			    // changes are also saved.
			    //
			    if (constraint == null || constraint.isGridBagConstraints() == false) {
				if (columns > 0 && column == columns - 1)
				    gbc.gridwidth = GridBagConstraints.REMAINDER;
				else gbc.gridwidth = 1;
				constraints[column] = gbc;
			    }
			    column = newline ? 0 : (column + 1)%constraints.length;
			} else column = newline ? 0 : column + 1;

			if (n < length - 1) {
			    if (newline) {
				if (vgap != null) {
				    if ((filler = pickLayoutFiller(vgap, YOIX_VERTICAL)) != null) {
					gbc = new GridBagConstraints();
					gbc.gridwidth = GridBagConstraints.REMAINDER;
					pane.add(filler, gbc);
				    }
				}
			    } else {
				if (hgap != null) {
				    if ((filler = pickLayoutFiller(hgap, YOIX_HORIZONTAL)) != null) {
					gbc = new GridBagConstraints();
					pane.add(filler, gbc);
				    }
				}
			    }
			}
		    } else {
			if (pane != null) {
			    weights = new double[] {0, 0};
			    if (leading)
				filler = pickLayoutFiller(child, YOIX_VERTICAL, weights);
			    else if (orientation == YOIX_NONE)
				filler = pickLayoutFiller(child, newline ? YOIX_VERTICAL : YOIX_HORIZONTAL, weights);
			    else filler = pickLayoutFiller(child, orientation, weights);
			    if (filler != null) {
				gbc = new GridBagConstraints();
				gbc.weightx = weights[0];
				gbc.weighty = weights[1];
				if (leading || orientation == YOIX_VERTICAL)
				    gbc.gridwidth = GridBagConstraints.REMAINDER;
				else if (newline && orientation == YOIX_NONE)
				    gbc.gridwidth = GridBagConstraints.REMAINDER;
				pane.add(filler, gbc);
			    } else VM.abort(BADVALUE, N_LAYOUT, n);
			}
			leading = false;
		    }
		} else VM.abort(BADVALUE, N_LAYOUT, n + 1);
	    } else {
		//
		// Our inclination here would be to abort, but backward
		// compatibility in at least one application wants this
		// behavior.
		//
		if ((constraint = add.getObject(n + 1)) != null) {
		    if (constraint.isGridBagConstraints())
			n += 1;
		}
	    }
	}
    }


    private void
    addToSpring(YoixObject add, YoixObject added, boolean fronttoback) {

	YoixObject                child;
	YoixObject                manager;
	YoixObject                yobj;
	YoixObject                yobj2;
	YoixObject                objs[]; 
	Component                 anchor;
	Component                 dependent;
	Object                    comp;
	Container                 pane;
	SpringLayout              layout;
	LayoutManager             lm;
	Spring                    hoffset = null;
	Spring                    voffset = null;
	SpringLayout.Constraints  constraints;
	Spring                    colwidth = null;
	Spring                    rowheight = null;
	Spring                    colwidths[];
	Spring                    rowheights[];
	Spring                    hgap;
	Spring                    vgap;
	Spring                    maxcol;
	Spring                    maxrow;
	String                    tag;
	String                    edge_anc;
	String                    edge_dep;
	boolean                   hcompact;
	boolean                   vcompact;
	int                       rows;
	int                       cols;
	int                       row;
	int                       col;
	int                       orientation;
	int                       length;
	int                       incr;
	int                       min;
	int                       pref;
	int                       max;
	int                       cnt;
	int                       n;

	comp = this.peer;		// snapshot - just to be safe

	if ((pane = getContainer(comp)) != null && (lm = pane.getLayout()) != null && lm instanceof SpringLayout) {
	    layout = (SpringLayout)lm;
	    manager = data.getObject(N_LAYOUTMANAGER); // safe to assume SpringLayout

	    //
	    // orientation can take values:
	    //   NONE       = no compaction, neither vertical nor horizontal (i.e., all cells are same size)
	    //   VERTICAL   = compaction in vertical direction (i.e., different row heights)
	    //   HORIZONTAL = compaction in horizontal direction (i.e., different column widths)
	    //   BOTH       = compaction in both directions (i.e., different row heigths and column widths)
	    //
	    orientation = manager.getInt(N_ORIENTATION, YOIX_BOTH);
	    rows = manager.getInt(N_ROWS, 0);
	    cols = manager.getInt(N_COLUMNS, 0);
	    hgap = Spring.constant(YoixMakeScreen.javaDistance(manager.getInt(N_HGAP, 0)));
	    vgap = Spring.constant(YoixMakeScreen.javaDistance(manager.getInt(N_VGAP, 0)));

	    if (orientation == YOIX_HORIZONTAL || orientation == YOIX_BOTH)
		hcompact = true;
	    else hcompact = false;
	    if (orientation == YOIX_VERTICAL || orientation == YOIX_BOTH)
		vcompact = true;
	    else vcompact = false;

	    length = add.length();
	    incr = fronttoback ? 1 : -1;
	    n = fronttoback ? 0 : length - 1;

	    //
	    // First layout pass adds components to pane and stores other objects
	    // for the second pass.
	    //
	    objs = new YoixObject[length];
	    for (cnt = 0; n >= 0 && n < length; n += incr) {
		if ((child = add.getObject(n)) != null) {
		    if (child.notNull() && child.isComponent()) {
			addTo(child, null, added, n);
			objs[n] = null;
			cnt++; // count the components
		    } else objs[n] = child;
		} else objs[n] = null;
	    }

	    //
	    // Applied automated SpringLayout constraints now, so they can be
	    // over-ridden by any explicit ones that might also exists
	    //
	    if (cnt > 0 && (rows > 0 || cols > 0)) {
		if (rows <= 0)
		    rows = (cnt%cols == 0 ? 0 : 1) + cnt/cols;
		else if (cols <= 0)
		    cols = (cnt%rows == 0 ? 0 : 1) + cnt/rows;

		//
		// add fillers if needed
		//
		for (n = rows*cols; cnt < n; cnt++)
		    pane.add(new JLabel());

		//
		// first get sizing info
		//
		maxcol = Spring.constant(0);
		maxrow = Spring.constant(0);
		colwidths = new Spring[cols];
		rowheights = new Spring[rows];
		for (row = 0; row < rows; row++)
		    rowheights[row] = Spring.constant(0);
		for (col = 0; col < cols; col++) {
		    colwidth = Spring.constant(0);
		    for (row = 0; row < rows; row++) {
			constraints = getCellSpringConstraints(pane, layout, row, col, cols, cnt);
			colwidth = Spring.max(colwidth, constraints.getWidth());
			rowheights[row] = Spring.max(rowheights[row], constraints.getHeight());
		    }
		    colwidths[col] = colwidth;
		    maxcol = Spring.max(maxcol, colwidth);
		}
		for (row = 0; row < rows; row++)
		    maxrow = Spring.max(maxrow, rowheights[row]);

		//
		// now add appropriate constraints
		//
		
		hoffset = Spring.constant(0);
		for (col = 0; col < cols; col++) {
		    voffset = Spring.constant(0);
		    for (row = 0; row < rows; row++) {
			constraints = getCellSpringConstraints(pane, layout, row, col, cols, cnt);

			colwidth = hcompact ? colwidths[col] : maxcol;
			rowheight = vcompact ? rowheights[row] : maxrow;

			constraints.setX(hoffset);
			constraints.setWidth(colwidth);

			constraints.setY(voffset);
			constraints.setHeight(rowheight);

			voffset = Spring.sum(voffset, Spring.sum(rowheight, vgap));
		    }
		    hoffset = Spring.sum(hoffset, Spring.sum(colwidth, hgap));
		}

		//
		// constrain pane
		//
		constraints = layout.getConstraints(pane);
		constraints.setConstraint(SpringLayout.EAST, hoffset);
		constraints.setConstraint(SpringLayout.SOUTH, voffset);
	    }

	    //
	    // Second pass applies the constraints.
	    //
	    for (n = 0; n < objs.length; n++) {
		if (objs[n] != null) { // for now assume it is a SpringConstraints object
		    tag = objs[n].getString(N_ANCHORCOMP);
		    child = added.getObject(tag);
		    if (child != null) {
			anchor = (Component)child.getManagedObject();
			if (isContainer(anchor))
			    anchor = getContainer(anchor);
			edge_anc = jfcString("SpringLayout", objs[n], N_ANCHOREDGE);
			tag = objs[n].getString(N_DEPENDCOMP);
			child = added.getObject(tag);
			if (child != null) {
			    dependent = (Component)child.getManagedObject();
			    if (isContainer(dependent))
				dependent = getContainer(dependent);
			    edge_dep = jfcString("SpringLayout", objs[n], N_DEPENDEDGE);
			    yobj = objs[n].getObject(N_SPRING);
			    if (yobj != null && yobj.notNull()) {
				if (yobj.isInteger()) {
				    pref = YoixMakeScreen.javaDistance(yobj.intValue()); // should we check for negative?
				    layout.putConstraint(edge_dep, dependent, pref, edge_anc, anchor);
				} else if (yobj.isString()) {
				    tag = yobj.stringValue();
				    child = added.getObject(tag);
				    if (child != null) {
					comp = child.getManagedObject();
					if (isContainer(comp))
					    comp = getContainer(comp);
					if (SpringLayout.NORTH.equals(edge_anc) || SpringLayout.SOUTH.equals(edge_anc))
					    layout.putConstraint(edge_dep, dependent, Spring.height((Component)comp), edge_anc, anchor);
					else layout.putConstraint(edge_dep, dependent, Spring.width((Component)comp), edge_anc, anchor);
				    } else VM.abort(BADVALUE, N_SPRING);
				} else if (yobj.isArray()) {
				    if (yobj.sizeof() > 0) {
					yobj2 = yobj.getObject(0);
					if (yobj2.isInteger()) {
					    min = pref = max = yobj2.intValue();
					    if (yobj.sizeof() > 1) {
						yobj2 = yobj.getObject(1);
						if (yobj2.isInteger()) {
						    pref = max = yobj2.intValue();
						    if (yobj.sizeof() > 2) {
							yobj2 = yobj.getObject(2);
							if (yobj2.isInteger()) {
							    max = yobj2.intValue();
							} else VM.abort(BADVALUE, N_SPRING, 2);
						    }
						} else VM.abort(BADVALUE, N_SPRING, 1);
					    }
					    layout.putConstraint(edge_dep, dependent, Spring.constant(min, pref, max), edge_anc, anchor); // should we check for negatives?
					} else VM.abort(BADVALUE, N_SPRING, 0);
				    } else VM.abort(BADVALUE, N_SPRING);
				} else VM.abort(TYPECHECK, N_SPRING);
			    } else VM.abort(BADVALUE, N_SPRING);
			} else VM.abort(BADVALUE, N_DEPENDCOMP, tag);
		    } else VM.abort(BADVALUE, N_ANCHORCOMP, tag);
		}
	    }
	}
    }


    private int
    buildEventMask() {

	YoixObject  handler;
	YoixObject  funct;
	YoixObject  argv[];
	Iterator    iterator;
	String      name;
	int         listener;
	int         mask;

	//
	// Older versions didn't enable events when the handler was a
	// NULL function. Changed because there's currently no way to
	// enable or disable events after the contructor runs. Probably
	// easy to do a better job, but this is good enough for now.
	//

	mask = 0;

	for (iterator = eventlisteners.keySet().iterator(); iterator.hasNext(); ) {
	    name = (String)iterator.next();
	    if ((handler = data.getObject(name)) != null) {
		if (handler.isCallable() || handler.isCallablePointer()) {
		    listener = ((Integer)eventlisteners.get(name)).intValue();
		    if (listener == DROPTARGETLISTENER) {
			if ((mask&DROPTARGETLISTENER) == 0) {
			    argv = new YoixObject[] {YoixMake.yoixType(T_DROPTARGETEVENT)};
			    funct = handler.isCallable() ? handler : handler.get();
			    if (funct.callable(argv))		// extra checking
				mask |= listener;
			}
		    } else mask |= listener;
		} else VM.abort(TYPECHECK, name);
	    }
	}
	return(mask);
    }


    private void
    doLayout(YoixObject obj, boolean fronttoback, boolean syncroot) {

	LayoutManager  layout;
	YoixObject     added;
	YoixObject     root;
	Container      pane;
	YoixError      error_point = null;
	Object         comp;

	//
	// Other methods, like setBackground(), now implicitly assume the
	// container itself is the first element in the dictionary, so be
	// careful if you make changes. In particular, loops like
	//
	//    for (m = 1; m < container.length(); m++)
	//        n += container.defined(m) ? 1 : 0;
	//
	// in methods like setBackground() are making the assumption when
	// they start the loop at index 1 instead of index 0. Wouldn't be
	// hard to make those loops smarter so they don't need to make the
	// assumption, but it's not necessary right now - just be aware of
	// the potential problems.
	//
	// NOTE - several methods defined in YoixBodyComponentSwing.java
	// make the same assumption.
	//

	comp = this.peer;		// snapshot - just to be safe

	if ((pane = getContainer(comp)) != null) {
	    synchronized(this) {
		root = data.getObject(N_ROOT);
		removeAll();
		try {
		    error_point = VM.pushError();
		    VM.pushAccess(LRW_);	// must follow pushJump()!!
		    setFirstFocus(true);
		    added = YoixObject.newDictionary(1, -1, false);
		    //
		    // Since setField(N_LAYOUT) precedes setField(N_TAG) an
		    // automatically generated tag, if needed, would not be
		    // set at this point, but no harm in forcing it at this
		    // point. Added: 10/9/2008
		    //
		    setTag((Object)null, data.getObject(N_TAG));
		    added.put(data.getString(N_TAG), getContext(), true);
		    if (obj != null && obj.notNull()) {
			if (specialLayout(obj, comp, pane, added, fronttoback) == false) {
			    layout = pane.getLayout();
			    if (layout instanceof BorderLayout) {
				addToBorder(obj, added, fronttoback);
			    } else if (layout instanceof BoxLayout) {
				addToBox(obj, added, fronttoback, (BoxLayout)layout);
			    } else if (layout instanceof CardLayout) {
				addToCard(obj, added, fronttoback);
				//
				// Recent addition (9/19/10) that makes sure the
				// first "card" gets componentShown() events if
				// it really wants them.
				//
				((CardLayout)layout).first(pane);
			    } else if (layout instanceof FlowLayout) {
				addToUnconstrained(obj, added, fronttoback);
			    } else if (layout instanceof GridBagLayout) {
				addToGridBag(obj, added, fronttoback);
			    } else if (layout instanceof GridLayout) {
				addToUnconstrained(obj, added, fronttoback);
			    } else if (layout instanceof SpringLayout) {
				addToSpring(obj, added, fronttoback);
			    } else addToCustom(obj, added, fronttoback);
			}
		    }
		    added.setGrowable(false);
		    added.setAccessBody(LR__);
		    data.put(N_COMPONENTS, added);
		    requestFirstFocus();
		    VM.popError();	// also resets access
		}
		catch(Error e) {
		    removeAll();
		    if (e != error_point) {
			VM.popError();
			throw(e);
		    } else VM.error(error_point);
		}
		catch(RuntimeException e) {
		    VM.popError();
		    throw(e);
		}
	    }

	    //
	    // Think this is sometimes needed to sync N_COMPONENTS in the
	    // original root window.
	    //
	    if (syncroot && root != null && root.isComponent())
		((YoixBodyComponent)root.body()).doLayout();
	    pane.validate();
	    if (pane.getLayout() == null)
		pane.repaint();
	}
    }


    private SpringLayout.Constraints
    getCellSpringConstraints(Container pane, SpringLayout layout, int row, int col, int cols, int cnt) {

	Component                 comp;
	SpringLayout.Constraints  val;
	int                       cell = row*cols + col;

	if (cell < cnt) {
	    try {
		comp = pane.getComponent(cell);
		val = layout.getConstraints(comp);
	    }
	    catch(Exception e) {	// could only happen in threaded situation
		val = new SpringLayout.Constraints();
	    }
	} else val = new SpringLayout.Constraints();

	return(val);
    }


    private YoixObject
    getScreenForObject(YoixObject obj, YoixObject screen) {

	YoixObject  screens;
	int         index;

	if (obj != null) {
	    if (obj.isNull() || obj.isInteger() || obj.isScreen()) {
		if (obj.isInteger()) {
		    screens = VM.getObject(N_SCREENS);
		    if ((index = obj.intValue()) <= 0)
			screen = VM.getObject(N_SCREEN);
		    else if (index >= screens.length())
			screen = screens.getObject(screens.length() - 1);
		    else screen = screens.getObject(index);
		} else if (obj.isNull())
		    screen = VM.getObject(N_SCREEN);
		else screen = obj;
	    }
	}

	return(screen);
    }


    private YoixObject
    handleAWTEvent(String name, int id, AWTEvent e) {

	EventQueue  queue;
	YoixObject  funct;
	YoixObject  event;
	YoixObject  obj = null;
	AWTEvent    next;
	HashMap     compress;

	//
	// Checks to see if we're supposed to compress events that go by
	// name, and if so then skip this event if it looks like we'll be
	// handling another one shortly. Comparing the source of the next
	// event to our peer should be correct - previous implementations
	// compared the source of event e to the source next event, which
	// was probably sufficient. Test was changed on 5/17/05.
	// 

	if (name != null && e != null) {
	    if ((compress = this.compressevents) != null && compress.containsKey(name) && (queue = YoixAWTToolkit.getSystemEventQueue()) != null) {
		if ((next = queue.peekEvent(e.getID())) != null) {
		    if (next.getSource() != peer)	// recent change
			next = null;
		}
	    } else next = null;
	    if (next == null) {
		if ((funct = data.getObject(name)) != null && funct.notNull()) {
		    if ((event = YoixMakeEvent.yoixEvent(e, id, this)) != null) {
			if (e instanceof InputEvent) {
			    YoixMakeEvent.setConsumed(event);
			    obj = call(funct, new YoixObject[] {event});
			    if (event.defined(N_CONSUMED) && event.getBoolean(N_CONSUMED))
				((InputEvent)e).consume();
			} else obj = call(funct, new YoixObject[] {event});
		    }
		}
	    }
	}
	return(obj);
    }


    private YoixObject
    handleDnDEvent(String name, int id, EventObject e) {

	YoixObject  argv[];
	YoixObject  handler;
	YoixObject  funct;
	YoixObject  event;
	YoixObject  obj = null;
	Object      comp;

	//
	// Most of these event handlers are "overloaded" (i.e., they can
	// be handed a DragSourceEvent or DropTargetEvent) so there's an
	// extra argv[] check that has to be passed before we call funct.
	// Didn't see any reason why other event handling code should be
	// burdoned by extra checking.
	//
	// The caller gets the Yoix representation of the event when e is
	// a DragGestureEvent or a DragSourceEvent, otherwise we return
	// whatever the Yoix event handler returned. The implementation
	// means Yoix DragSourceEvent and DragGestureEvent event handlers
	// store values, like the cursor or transferable, in their event
	// argument, while DropTarget event handlers just return true or
	// false (or no return value) to indicate they accept or reject
	// the "operation".
	//
	// NOTE - the isEnabled() test is new and is supposed to stop all
	// drag and drop events when a component has been disabled. Java
	// apparently lets them through, but we think it's better to stop
	// them all. Change was made on 10/4/06.
	//


	if (name != null && e != null) {
	    comp = this.peer;		// snapshot - just to be safe
	    if (comp instanceof Component && ((Component)comp).isEnabled()) {
		if ((handler = data.getObject(name)) != null && handler.notNull()) {
		    if ((event = YoixMakeEvent.yoixEvent(e, id, this)) != null) {
			argv = new YoixObject[] {event};
			funct = handler.isCallablePointer() ? handler.get() : handler;
			if (funct.callable(argv)) {	// extra checking
			    if ((obj = call(handler, argv)) != null) {
				if (e instanceof DragGestureEvent) {
				    if (obj.notEmpty()) {
					event.putObject(N_TRANSFERABLE, obj);
					obj = event;
				    } else obj = null; 
				} else if (e instanceof DragSourceEvent)
				    obj = event;
				else if (obj.isEmpty())
				    obj = null;
			    }
			}
		    }
		}
	    }
	}
	return(obj);
    }


    private YoixObject
    handleEventObject(String name, int id, EventObject e) {

	YoixObject  funct;
	YoixObject  event;
	YoixObject  obj = null;

	if (name != null && e != null) {
	    if ((funct = data.getObject(name)) != null && funct.notNull()) {
		if ((event = YoixMakeEvent.yoixEvent(e, id, this)) != null)
		    obj = call(funct, new YoixObject[] {event});
	    }
	}
	return(obj);
    }


    private synchronized void
    handleShowingChange(boolean state) {

	YoixObject  components;
	YoixObject  child;
	Object      comp;
	int         n;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof YoixInterfaceWindow) {
	    components = data.getObject(N_COMPONENTS);
	    for (n = 0; n < components.length(); n++) {
		if (components.defined(n)) {
		    child = components.getObject(n);
		    if (((YoixBodyComponent)child.body()) != this) {
			comp = child.getManagedObject();
			if (comp instanceof YoixInterfaceShowing)
			    ((YoixInterfaceShowing)comp).handleShowingChange(state);
		    }
		}
	    }
	}
    }


    private Color
    pickBackground(YoixObject obj, Color color) {

	Object  comp;
	Color   uicolor;

	comp = this.peer;		// snapshot - just to be safe

	if (obj.isNull()) {
	    if ((uicolor = pickUIBackground()) == null) {
		if (comp instanceof TextComponent || comp instanceof JTextComponent) {
		    color = Color.white;
		    data.putColor(N_BACKGROUND, color);
		} else if (comp instanceof List || comp instanceof JList) {
		    color = Color.white;
		    data.putColor(N_BACKGROUND, color);
		} else if (comp instanceof JComboBox) {
		    color = Color.white;
		    data.putColor(N_BACKGROUND, color);
		} else if (ISWIN && comp instanceof Choice) {
		    color = Color.white;
		    data.putColor(N_BACKGROUND, color);
		} else if (color == null)
		    color = YoixMakeScreen.javaBackground(findClosestValue(N_BACKGROUND));
	    } else color = uicolor;
	} else color = YoixMakeScreen.javaBackground(obj);

	return(color);
    }


    private Color
    pickForeground(YoixObject obj, Color color) {

	Object  comp;
	Color   uicolor;

	comp = this.peer;		// snapshot - just to be safe

	if (obj.isNull()) {
	    if ((uicolor = pickUIForeground()) == null) {
		if (comp instanceof TextComponent || comp instanceof JTextComponent) {
		    color = Color.black;
		    data.putColor(N_FOREGROUND, color);
		} else if (color == null)
		    color = YoixMakeScreen.javaForeground(findClosestValue(N_FOREGROUND));
	    } else color = uicolor;
	} else color = YoixMakeScreen.javaForeground(obj);

	return(color);
    }


    private GridBagConstraints
    pickGridBagConstraints(YoixObject constraint, GridBagConstraints gbc) {

	//
	// Should only be called with constraint and gbc that aren't null.
	// Caller probably is comparing the return value to gbc to decide
	// how to proceed, so a null gbc could be trouble.
	//

	return(pickGridBagConstraints(constraint, new GridBagConstraints[] {gbc}, 0));
    }


    private GridBagConstraints
    pickGridBagConstraints(YoixObject constraint, GridBagConstraints constraints[], int index) {

	GridBagConstraints  gbc;
	Dimension           size;

	//
	// A null return means we couldn't build a GridBagConstraints from
	// constraint. The caller should run additional checks to decide if
	// it's supposed to be consumed now or perhaps should be treated as
	// a component or an object, like a string, that might represent a
	// component. In other words, our job is to decide if we know how
	// to turn constraint into a GridBagConstraints using the arguments
	// that we were called with.
	//

	if (constraint != null) {
	    if (constraint.notNull()) {
		if (constraint.isGridBagConstraints()) {
		    gbc = new GridBagConstraints();
		    gbc.anchor = jfcInt("GridBagAnchor", constraint, N_ANCHOR);
		    gbc.fill = jfcInt("GridBagFill", constraint, N_FILL);
		    gbc.gridx = constraint.getInt(N_GRIDX, YOIX_RELATIVE);
		    gbc.gridy = constraint.getInt(N_GRIDY, YOIX_RELATIVE);
		    gbc.gridheight = constraint.getInt(N_GRIDHEIGHT, 1);
		    gbc.gridwidth = constraint.getInt(N_GRIDWIDTH, 1);
		    gbc.weightx = constraint.getDouble(N_WEIGHTX, 0);
		    gbc.weighty = constraint.getDouble(N_WEIGHTY, 0);
		    gbc.insets = YoixMakeScreen.javaInsets(constraint.getObject(N_INSETS));
		    size = YoixMakeScreen.javaDimension(
			constraint.getObject(N_IPADX),
			constraint.getObject(N_IPADY)
		    );
		    gbc.ipadx = size.width;
		    gbc.ipady = size.height;
		} else if (constraints != null) {
		    if (constraint.isInteger()) {
			gbc = constraints[index];
			switch (constraint.intValue()) {
			    case YOIX_BOTH:
			    case YOIX_BOTH_WEIGHTX:
			    case YOIX_BOTH_WEIGHTY:
			    case YOIX_BOTH_WEIGHT_NONE:
				gbc.fill = GridBagConstraints.BOTH;
				break;

			    case YOIX_HORIZONTAL:
			    case YOIX_HORIZONTAL_WEIGHTY:
			    case YOIX_HORIZONTAL_WEIGHT_BOTH:
			    case YOIX_HORIZONTAL_WEIGHT_NONE:
				gbc.fill = GridBagConstraints.HORIZONTAL;
				break;

			    case YOIX_NONE:
			    case YOIX_NONE_WEIGHTX:
			    case YOIX_NONE_WEIGHTY:
			    case YOIX_NONE_WEIGHT_BOTH:
				gbc.fill = GridBagConstraints.NONE;
				break;

			    case YOIX_REMAINDER:
				gbc.gridwidth = GridBagConstraints.REMAINDER;
				break;

			    case YOIX_VERTICAL:
			    case YOIX_VERTICAL_WEIGHTX:
			    case YOIX_VERTICAL_WEIGHT_BOTH:
			    case YOIX_VERTICAL_WEIGHT_NONE:
				gbc.fill = GridBagConstraints.VERTICAL;
				break;

			    default:
				if (jfcExists("GridBagAnchor", constraint.intValue()))
				    gbc.anchor = jfcInt("GridBagAnchor", constraint);
				else gbc = null;
				break;
			}
			switch (constraint.intValue()) {
			    case YOIX_BOTH:
			    case YOIX_HORIZONTAL_WEIGHT_BOTH:
			    case YOIX_NONE_WEIGHT_BOTH:
			    case YOIX_VERTICAL_WEIGHT_BOTH:
				if (gbc.weightx <= 0)
				    gbc.weightx = 1.0;
				if (gbc.weighty <= 0)
				    gbc.weighty = 1.0;
				break;

			    case YOIX_BOTH_WEIGHT_NONE:
			    case YOIX_HORIZONTAL_WEIGHT_NONE:
			    case YOIX_NONE:
			    case YOIX_VERTICAL_WEIGHT_NONE:
				gbc.weightx = 0;
				gbc.weighty = 0;
				break;

			    case YOIX_BOTH_WEIGHTX:
			    case YOIX_HORIZONTAL:
			    case YOIX_VERTICAL_WEIGHTX:
			    case YOIX_NONE_WEIGHTX:
				if (gbc.weightx <= 0)
				    gbc.weightx = 1.0;
				gbc.weighty = 0;
				break;

			    case YOIX_BOTH_WEIGHTY:
			    case YOIX_HORIZONTAL_WEIGHTY:
			    case YOIX_NONE_WEIGHTY:
			    case YOIX_VERTICAL:
				gbc.weightx = 0;
				if (gbc.weighty <= 0)
				    gbc.weighty = 1.0;
				break;
			}
		    } else if (constraint.isInsets()) {
			gbc = constraints[index];
			gbc.insets = YoixMakeScreen.javaInsets(constraint);
		    } else if (constraint.isDictionary()) {
			gbc = constraints[index];
			if (constraint.defined(N_ANCHOR))
			    gbc.anchor = jfcInt("GridBagAnchor", constraint, N_ANCHOR);
			if (constraint.defined(N_FILL))
			    gbc.fill = jfcInt("GridBagFill", constraint, N_FILL);
			if (constraint.defined(N_GRIDX))
			    gbc.gridx = constraint.getInt(N_GRIDX, YOIX_RELATIVE);
			if (constraint.defined(N_GRIDY))
			    gbc.gridy = constraint.getInt(N_GRIDY, YOIX_RELATIVE);
			if (constraint.defined(N_GRIDHEIGHT))
			    gbc.gridheight = constraint.getInt(N_GRIDHEIGHT, 1);
			if (constraint.defined(N_GRIDWIDTH))
			    gbc.gridwidth = constraint.getInt(N_GRIDWIDTH, 1);
			if (constraint.defined(N_WEIGHTX))
			    gbc.weightx = constraint.getDouble(N_WEIGHTX, 0);
			if (constraint.defined(N_WEIGHTY))
			    gbc.weighty = constraint.getDouble(N_WEIGHTY, 0);
			if (constraint.defined(N_INSETS))
			    gbc.insets = YoixMakeScreen.javaInsets(constraint.getObject(N_INSETS));
			if (constraint.defined(N_IPADX))
			    gbc.ipadx = YoixMakeScreen.javaDistance(constraint.getInt(N_IPADX, 0));
			if (constraint.defined(N_IPADY))
			    gbc.ipady = YoixMakeScreen.javaDistance(constraint.getInt(N_IPADY, 0));
		    } else gbc = null;
		} else gbc = null;
	    } else gbc = new GridBagConstraints();
	} else gbc = new GridBagConstraints();

	return(gbc);
    }


    private Color
    pickUIBackground() {

	YoixObject  obj;
	Object      comp;
	String      key;
	Color       color = null;

	//
	// Currently only giving JButton in the "Ocean" theme special treatment,
	// so the code is optimized for that case. Wouldn't be hard to improve
	// if more components or "themes" need attention. For example, we could
	// easily get the key from the last component peer's class name (after
	// tossing a leading 'J').
	//
	// NOTE - a special interface named ColorUIResource is used to wrap most
	// (perhaps all) the default colors managed by UIMananger and there's at
	// least one component, namely a JButton in the "Ocean" theme, that draws
	// its background differently when the background color implements that
	// interface. In other words, if you grab a color from the UIManager and
	// use it as the background color of a JButton in the "Ocean" theme you
	// may be surprised by the result. Anyway, we added a kludge that tries
	// to deal with the JButton by explicitly building a Color object that
	// doesn't implement the ColorUIResource interface. Small chance other
	// components will need some attention??
	//
	// NOTE - added some special treatment for JScrollBars on 1/13/11, but
	// it's not well tested and I decided not to add any code to the branch
	// that handles things when N_UIMKEY is set (only because I don't have
	// the time to really test the changes).
	//

	comp = this.peer;		// snapshot - just to be safe

	if ((obj = data.getObject(N_UIMKEY)) == null || obj.isNull()) {
	    if (comp instanceof JButton) {
		if (YoixMiscJFC.checkLookAndFeel("Metal", "Ocean"))
		    color = UIManager.getColor("Button.background");
	    } else if (comp instanceof JScrollBar) {
		if (YoixMiscJFC.checkLookAndFeel("Metal", "Ocean"))
		    color = UIManager.getColor("ScrollBar.background");
	    }
	} else {
	    key = obj.stringValue();
	    if ((color = UIManager.getColor(key + ".background")) != null) {
		if (comp instanceof JButton) {		// ColorUIResource kludge
		    if (key.equals("Button") == false)
			color = new Color(color.getRGB());
		}
	    }
	}
	return(color);
    }


    private Color
    pickUIForeground() {

	YoixObject  obj;
	String      key = null;

	//
	// Currently don't think any components need special attention.
	//

	if ((obj = data.getObject(N_UIMKEY)) != null && obj.notNull())
	    key = obj.stringValue();
 	return(key != null ? UIManager.getColor(key + "." + N_FOREGROUND) : null);
    }


    private void
    removeDragGestureListener(DragGestureListener listener) {

	if (dragrecognizer != null) {
	    try {
		dragrecognizer.removeDragGestureListener(listener);
		dragrecognizer = null;
	    }
	    catch(IllegalArgumentException e) {}
	}
    }


    private void
    removeDropTargetListener(DropTargetListener listener) {

	DropTarget  droptarget;
	Object      comp = peer;

	if (comp instanceof Component) {
	    if ((droptarget = ((Component)comp).getDropTarget()) != null) {
		try {
		    droptarget.removeDropTargetListener(listener);
		}
		catch(IllegalArgumentException e) {}
	    }
	}
    }


    private final YoixObject
    replaceComponents(YoixObject dest, int offset, int count, YoixObject source) {

	YoixObject  components = null;
	YoixObject  destvalues[];
	YoixObject  srcvalues[];
	YoixObject  values[];
	Hashtable   keymap;
	String      destkeys[];
	String      srckeys[];
	String      keys[];
	String      key;
	int         destlength;
	int         srclength;
	int         length;
	int         index;
	int         m;
	int         n;

	//
	// Added on 3/1/11 to help a new version of setLayout() update other
	// components dictionaries. It uses getValues() and getKeys(), which
	// are two methods that were also added to YoixBodyDictionaryObject
	// on 3/1/11.
	//

	if ((destvalues = dest.getValues()) != null) {
	    if ((destkeys = dest.getKeys()) != null) {
		if ((srcvalues = source.getValues()) != null) {
		    if ((srckeys = source.getKeys()) != null) {
			destlength = destvalues.length;
			srclength = srcvalues.length;
			length = destlength - count + srclength;
			keys = new String[length];
			values = new YoixObject[length];
			keymap = new Hashtable();
			index = 0;
			for (n = 0; n < destlength; n++) {
			    if (n == offset) {
				for (m = 0; m < srclength; m++) {
				    if ((key = srckeys[m]) != null) {
					if (keymap.containsKey(key) == false) {
					    keymap.put(key, new Integer(index));
					    keys[index] = key;
					    values[index] = srcvalues[m];
					    index++;
					} else VM.abort(DUPLICATETAG, key);
				    }
				}
				n += count - 1;
			    } else {
				if ((key = destkeys[n]) != null) {
				    if (keymap.containsKey(key) == false) {
					keymap.put(key, new Integer(index));
					keys[index] = key;
					values[index] = destvalues[n];
					index++;
				    } else VM.abort(DUPLICATETAG, key);
				}
			    }
			}
			components = YoixObject.newDictionary(keys, values, keymap);
		    }
		}
	    }
	}
	return(components);
    }


    private void
    setFirstFocus(boolean state) {

	firstfocus = (peer instanceof YoixInterfaceWindow) ? state : false;
    }
}

