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
import java.awt.geom.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

public
class YoixBodyComponentSwing extends YoixBodyComponent

    implements YoixAPI,
	       YoixAPIProtected,
	       YoixConstantsJTree,
	       YoixConstantsSwing

{

    private YoixBodyComponent  menubarowner = null;
    private YoixBodyComponent  popupowner = null;
    private Dimension          layoutsize = null;
    private boolean            disposable = false;
    private boolean            initialized = false;

    protected JScrollPane  peerscroller = null;

    //
    // Used when we simulate textValueChanged events for Swing components
    // even though Java really only supports them for AWT components.
    //

    private YoixTextValueChangedListener  ytvcl = null;

    //
    // Used with JPopupMenu components
    //
    
    private YoixPopupMenuListener  popupmenulistener = null;

    //
    // Values that can be used to restore special default values when we
    // store a null in certain fields.
    //

    private TransferHandler  nulltransferhandler = null;
    private Border           nullborder = null;

    //
    // This can be used to control the thread-safe behavior of the Swing
    //

    private static boolean  threadsafe = true;
    private static boolean  threadsafe_locked = false;

    //
    // Unfortunately Swing menus can hang around longer than they really
    // should which also sometimes delays garbage collection on lots of
    // other object. The culprit seems to be a static reference in Sun's
    // AppContext that goes through the MenuKeyboardHelper that's defined
    // in the BasicPopupMenuUI class. It doesn't look like there's an easy
    // way for user code to clear that reference, but attaching the popup
    // menu to a JFrame and quickly toggling its visibility seems to do
    // do it. This isn't an ideal solution, so we're going to investigate
    // a bit more and perhaps make it optional. Anwywya there's a bunch
    // more to do.
    // 

    private static JFrame  menuframe = new JFrame();

    //
    // An array used to set permissions on some of the fields that
    // users should only be able to set once.
    //

    private static final Object  permissions[] = {
     //
     // FIELD               OBJECT       BODY
     // -----               ------       ----
	N_COMPONENTS,       $LR__,       $LR__,
	N_SCREEN,           $LR__,       null,		// added on 10/18/11
	N_TAG,              $LR__,       null,
    };

    //
    // ButtonGroup dummy button used when deselecting all group buttons.
    // This was static in older versions, which could occasionally cause
    // memory management problems. Recently (12/4/04) removed the static,
    // but probably still need to test more.
    //

    private JButton bg_dummy = null;		// this was static!!

    //
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(225);

    static {
	activefields.put(N_ACCELERATOR, new Integer(V_ACCELERATOR));
	activefields.put(N_ACTION, new Integer(V_ACTION));
	activefields.put(N_ADJUSTING, new Integer(V_ADJUSTING));
	activefields.put(N_AFTERPAN, new Integer(V_AFTERPAN));
	activefields.put(N_AFTERSELECT, new Integer(V_AFTERSELECT));
	activefields.put(N_AFTERZOOM, new Integer(V_AFTERZOOM));
	activefields.put(N_ALIGNMENT, new Integer(V_ALIGNMENT));
	activefields.put(N_ALLOWEDIT, new Integer(V_ALLOWEDIT));
	activefields.put(N_ALTALIGNMENT, new Integer(V_ALTALIGNMENT));
	activefields.put(N_ALTBACKGROUND, new Integer(V_ALTBACKGROUND));
	activefields.put(N_ALTFONT, new Integer(V_ALTFONT));
	activefields.put(N_ALTFOREGROUND, new Integer(V_ALTFOREGROUND));
	activefields.put(N_ALTGRIDCOLOR, new Integer(V_ALTGRIDCOLOR));
	activefields.put(N_ALTTOOLTIPTEXT, new Integer(V_ALTTOOLTIPTEXT));
	activefields.put(N_ANCHOR, new Integer(V_ANCHOR));
	activefields.put(N_APPROVEBUTTONMNEMONIC, new Integer(V_APPROVEBUTTONMNEMONIC));
	activefields.put(N_APPROVEBUTTONTEXT, new Integer(V_APPROVEBUTTONTEXT));
	activefields.put(N_APPROVEBUTTONTOOLTIPTEXT, new Integer(V_APPROVEBUTTONTOOLTIPTEXT));
 	activefields.put(N_ARMED, new Integer(V_ARMED));
 	activefields.put(N_ATTRIBUTES, new Integer(V_ATTRIBUTES)); // used in YoixSwingJTable
	activefields.put(N_BACKGROUND, new Integer(V_BACKGROUND));
	activefields.put(N_BACKGROUNDHINTS, new Integer(V_BACKGROUNDHINTS));
	activefields.put(N_BACKGROUNDIMAGE, new Integer(V_BACKGROUNDIMAGE));
	activefields.put(N_BASE, new Integer(V_BASE));
	activefields.put(N_BLOCKINCREMENT, new Integer(V_BLOCKINCREMENT));
	activefields.put(N_BORDER, new Integer(V_BORDER));
	activefields.put(N_BORDERCOLOR, new Integer(V_BORDERCOLOR));
	activefields.put(N_BUTTONS, new Integer(V_BUTTONS));
	activefields.put(N_CARET, new Integer(V_CARET));
	activefields.put(N_CARETALPHA, new Integer(V_CARETALPHA));
	activefields.put(N_CARETCOLOR, new Integer(V_CARETCOLOR));
	activefields.put(N_CARETMODEL, new Integer(V_CARETMODEL));
	activefields.put(N_CARETOWNERCOLOR, new Integer(V_CARETOWNERCOLOR));
	activefields.put(N_CELLCOLORS, new Integer(V_CELLCOLORS));
	activefields.put(N_CELLSIZE, new Integer(V_CELLSIZE));
	activefields.put(N_CLICK, new Integer(V_CLICK));
	activefields.put(N_CLICKCOUNT, new Integer(V_CLICKCOUNT));
	activefields.put(N_CLOSABLE, new Integer(V_CLOSABLE));
	activefields.put(N_CLOSED, new Integer(V_CLOSED));
	activefields.put(N_CLOSEDICON, new Integer(V_CLOSEDICON));
	activefields.put(N_COLOR, new Integer(V_COLOR));
	activefields.put(N_COLUMNS, new Integer(V_COLUMNS));
	activefields.put(N_COMMAND, new Integer(V_COMMAND));
	activefields.put(N_COMPRESSEVENTS, new Integer(V_COMPRESSEVENTS));
	activefields.put(N_CONTINUOUSLAYOUT, new Integer(V_CONTINUOUSLAYOUT));
	activefields.put(N_CURSOR, new Integer(V_CURSOR));
	activefields.put(N_DECORATIONSTYLE, new Integer(V_DECORATIONSTYLE));
	activefields.put(N_DESKTOP, new Integer(V_DESKTOP));
	activefields.put(N_DIRECTORY, new Integer(V_DIRECTORY));
	activefields.put(N_DISPOSE, new Integer(V_DISPOSE));
	activefields.put(N_DIVIDERLOCATION, new Integer(V_DIVIDERLOCATION));
	activefields.put(N_DIVIDERLOCKED, new Integer(V_DIVIDERLOCKED));
	activefields.put(N_DIVIDERSIZE, new Integer(V_DIVIDERSIZE));
	activefields.put(N_DOUBLEBUFFERED, new Integer(V_DOUBLEBUFFERED));
	activefields.put(N_DRAGENABLED, new Integer(V_DRAGENABLED));
	activefields.put(N_ECHO, new Integer(V_ECHO));
	activefields.put(N_EDIT, new Integer(V_EDIT));
	activefields.put(N_EDITBACKGROUND, new Integer(V_EDITBACKGROUND));
	activefields.put(N_EDITFOREGROUND, new Integer(V_EDITFOREGROUND));
	activefields.put(N_ENABLED, new Integer(V_ENABLED));
	activefields.put(N_EXPANDSSELECTEDNODES, new Integer(V_EXPANDSSELECTEDNODES));
	activefields.put(N_EXTENT, new Integer(V_EXTENT));
	activefields.put(N_ETC, new Integer(V_ETC));
	activefields.put(N_FILE, new Integer(V_FILE));
	activefields.put(N_FILESELECTIONMODE, new Integer(V_FILESELECTIONMODE));
	activefields.put(N_FILTER, new Integer(V_FILTER));
	activefields.put(N_FILTERS, new Integer(V_FILTERS));
	activefields.put(N_FINDNEXTMATCH, new Integer(V_FINDNEXTMATCH));
	activefields.put(N_FIRSTFOCUS, new Integer(V_FIRSTFOCUS));
	activefields.put(N_FLOATABLE, new Integer(V_FLOATABLE));
	activefields.put(N_FOCUSABLE, new Integer(V_FOCUSABLE));
	activefields.put(N_FOCUSOWNER, new Integer(V_FOCUSOWNER));
	activefields.put(N_FONT, new Integer(V_FONT));
	activefields.put(N_FOREGROUND, new Integer(V_FOREGROUND));
	activefields.put(N_FRAMES, new Integer(V_FRAMES));
	activefields.put(N_FRONTTOBACK, new Integer(V_FRONTTOBACK));
	activefields.put(N_FULLSCREEN, new Integer(V_FULLSCREEN));
	activefields.put(N_GETENABLED, new Integer(V_GETENABLED));
	activefields.put(N_GETSTATE, new Integer(V_GETSTATE));
	activefields.put(N_GLASSPANE, new Integer(V_GLASSPANE));
	activefields.put(N_GRIDCOLOR, new Integer(V_GRIDCOLOR));
	activefields.put(N_GRIDSIZE, new Integer(V_GRIDSIZE));
	activefields.put(N_GRAPHICS, new Integer(V_GRAPHICS));
	activefields.put(N_GROUP, new Integer(V_GROUP));
 	activefields.put(N_HEADER, new Integer(V_HEADER)); // used in YoixSwingJTable
	activefields.put(N_HEADERS, new Integer(V_HEADERS));
	activefields.put(N_HEADERICONS, new Integer(V_HEADERICONS));
	activefields.put(N_HIDDENFILES, new Integer(V_HIDDENFILES));
	activefields.put(N_HIGHLIGHTFLAGS, new Integer(V_HIGHLIGHTFLAGS));
	activefields.put(N_ICON, new Integer(V_ICON));
	activefields.put(N_ICONIFIABLE, new Integer(V_ICONIFIABLE));
	activefields.put(N_ICONIFIED, new Integer(V_ICONIFIED));
	activefields.put(N_ICONS, new Integer(V_ICONS));
	activefields.put(N_INDETERMINATE, new Integer(V_INDETERMINATE));
	activefields.put(N_INDEX, new Integer(V_INDEX));
	activefields.put(N_INPUTFILTER, new Integer(V_INPUTFILTER));
	activefields.put(N_INSETS, new Integer(V_INSETS));
	activefields.put(N_INVERTED, new Integer(V_INVERTED));
	activefields.put(N_IPAD, new Integer(V_IPAD));
	activefields.put(N_ITEMARRAY, new Integer(V_ITEMARRAY));
	activefields.put(N_ITEM, new Integer(V_ITEM));
	activefields.put(N_ITEMS, new Integer(V_ITEMS));
	activefields.put(N_KEEPHIDDEN, new Integer(V_KEEPHIDDEN));
	activefields.put(N_LABELS, new Integer(V_LABELS));
	activefields.put(N_LAYER, new Integer(V_LAYER));
	activefields.put(N_LAYOUT, new Integer(V_LAYOUT));
	activefields.put(N_LAYOUTMANAGER, new Integer(V_LAYOUTMANAGER));
	activefields.put(N_LEAFICON, new Integer(V_LEAFICON));
	activefields.put(N_LOCATION, new Integer(V_LOCATION));
	activefields.put(N_MAJORTICKSPACING, new Integer(V_MAJORTICKSPACING));
	activefields.put(N_MAXIMIZABLE, new Integer(V_MAXIMIZABLE));
	activefields.put(N_MAXIMIZED, new Integer(V_MAXIMIZED));
	activefields.put(N_MAXIMUM, new Integer(V_MAXIMUM));
	activefields.put(N_MAXIMUMSIZE, new Integer(V_MAXIMUMSIZE));
	activefields.put(N_MAPPINGS, new Integer(V_MAPPINGS));
	activefields.put(N_MENUBAR, new Integer(V_MENUBAR));
	activefields.put(N_MINIMUM, new Integer(V_MINIMUM));
	activefields.put(N_MINIMUMSIZE, new Integer(V_MINIMUMSIZE));
	activefields.put(N_MINORTICKSPACING, new Integer(V_MINORTICKSPACING));
	activefields.put(N_MNEMONIC, new Integer(V_MNEMONIC));
	activefields.put(N_MODAL, new Integer(V_MODAL));
	activefields.put(N_MODE, new Integer(V_MODE));
	activefields.put(N_MODELTOVIEW, new Integer(V_MODELTOVIEW));
	activefields.put(N_MULTIPLEMODE, new Integer(V_MULTIPLEMODE));
	activefields.put(N_NEXTCARD, new Integer(V_NEXTCARD));
	activefields.put(N_NEXTFOCUS, new Integer(V_NEXTFOCUS));
	activefields.put(N_ONETOUCHEXPANDABLE, new Integer(V_ONETOUCHEXPANDABLE));
	activefields.put(N_OPAQUE, new Integer(V_OPAQUE));
	activefields.put(N_OPENICON, new Integer(V_OPENICON));
	activefields.put(N_ORIENTATION, new Integer(V_ORIENTATION));
	activefields.put(N_ORIGIN, new Integer(V_ORIGIN));
	activefields.put(N_OUTPUTFILTER, new Integer(V_OUTPUTFILTER));
	activefields.put(N_PAGE, new Integer(V_PAGE));
	activefields.put(N_PAINT, new Integer(V_PAINT));
	activefields.put(N_PAINTLABELS, new Integer(V_PAINTLABELS));
	activefields.put(N_PAINTTICKS, new Integer(V_PAINTTICKS));
	activefields.put(N_PAINTTRACK, new Integer(V_PAINTTRACK));
	activefields.put(N_PANANDZOOM, new Integer(V_PANANDZOOM));
	activefields.put(N_PARENT, new Integer(V_PARENT));
	activefields.put(N_PERCENTCOMPLETE, new Integer(V_PERCENTCOMPLETE));
	activefields.put(N_POPUP, new Integer(V_POPUP));
	activefields.put(N_PREFERREDSIZE, new Integer(V_PREFERREDSIZE));
	activefields.put(N_PRESSED, new Integer(V_PRESSED));
	activefields.put(N_PROTOTYPEVALUE, new Integer(V_PROTOTYPEVALUE));
	activefields.put(N_PROMPT, new Integer(V_PROMPT));
	activefields.put(N_QUIET, new Integer(V_QUIET));
	activefields.put(N_REORDER, new Integer(V_REORDER));
	activefields.put(N_REPAINT, new Integer(V_REPAINT));
	activefields.put(N_REQUESTFOCUS, new Integer(V_REQUESTFOCUS));
	activefields.put(N_REQUESTFOCUSENABLED, new Integer(V_REQUESTFOCUSENABLED));
	activefields.put(N_RESET, new Integer(V_RESET));
	activefields.put(N_RESIZABLE, new Integer(V_RESIZABLE));
	activefields.put(N_RESIZE, new Integer(V_RESIZE));
	activefields.put(N_RESIZEMODE, new Integer(V_RESIZEMODE));
	activefields.put(N_RESIZEWEIGHT, new Integer(V_RESIZEWEIGHT));
	activefields.put(N_ROLLOVER, new Integer(V_ROLLOVER));
	activefields.put(N_ROLLOVERENABLED, new Integer(V_ROLLOVERENABLED));
	activefields.put(N_ROOT, new Integer(V_ROOT));
	activefields.put(N_ROOTHANDLE, new Integer(V_ROOTHANDLE));
	activefields.put(N_ROWHEIGHTADJUSTMENT, new Integer(V_ROWHEIGHTADJUSTMENT));
	activefields.put(N_ROWS, new Integer(V_ROWS));
	activefields.put(N_SAVEGRAPHICS, new Integer(V_SAVEGRAPHICS));
	activefields.put(N_SAVELINES, new Integer(V_SAVELINES));
	activefields.put(N_SCREEN, new Integer(V_SCREEN));
	activefields.put(N_SCROLL, new Integer(V_SCROLL));
	activefields.put(N_SCROLLSONEXPAND, new Integer(V_SCROLLSONEXPAND));
	activefields.put(N_SELECTED, new Integer(V_SELECTED));
	activefields.put(N_SELECTEDENDS, new Integer(V_SELECTEDENDS));
	activefields.put(N_SELECTEDINDEX, new Integer(V_SELECTEDINDEX));
	activefields.put(N_SELECTEDLABEL, new Integer(V_SELECTEDLABEL));
	activefields.put(N_SELECTIONBACKGROUND, new Integer(V_SELECTIONBACKGROUND));
	activefields.put(N_SELECTIONFOREGROUND, new Integer(V_SELECTIONFOREGROUND));
	activefields.put(N_SETENABLED, new Integer(V_SETENABLED));
	activefields.put(N_SETINCREMENT, new Integer(V_SETINCREMENT));
	activefields.put(N_SETSTATE, new Integer(V_SETSTATE));
	activefields.put(N_SETVALUES, new Integer(V_SETVALUES));
	activefields.put(N_SHAPE, new Integer(V_SHAPE));
	activefields.put(N_SHOWING, new Integer(V_SHOWING));
	activefields.put(N_SIZE, new Integer(V_SIZE));
	activefields.put(N_SIZECONTROL, new Integer(V_SIZECONTROL));
	activefields.put(N_SNAPTOTICKS, new Integer(V_SNAPTOTICKS));
	activefields.put(N_STATE, new Integer(V_STATE));
	activefields.put(N_SUBTEXT, new Integer(V_SUBTEXT));
	activefields.put(N_SYNCCOUNT, new Integer(V_SYNCCOUNT));
	activefields.put(N_SYNCVIEWPORT, new Integer(V_SYNCVIEWPORT));
	activefields.put(N_TAG, new Integer(V_TAG));
	activefields.put(N_TEXT, new Integer(V_TEXT));
	activefields.put(N_TEXTMODE, new Integer(V_TEXTMODE));
	activefields.put(N_TEXTPOSITION, new Integer(V_TEXTPOSITION));
	activefields.put(N_TEXTWRAP, new Integer(V_TEXTWRAP));
	activefields.put(N_TITLE, new Integer(V_TITLE));
	activefields.put(N_TOOLTIP, new Integer(V_TOOLTIP));
	activefields.put(N_TOOLTIPS, new Integer(V_TOOLTIPS));
	activefields.put(N_TOOLTIPTEXT, new Integer(V_TOOLTIPTEXT));
	activefields.put(N_TOP, new Integer(V_TOP));
	activefields.put(N_TRACKFOCUS, new Integer(V_TRACKFOCUS));
	activefields.put(N_TRANSFERHANDLER, new Integer(V_TRANSFERHANDLER));
	activefields.put(N_TYPE, new Integer(V_TYPE)); // used in YoixSwingJTable
	activefields.put(N_TYPES, new Integer(V_TYPES));
	activefields.put(N_UNITINCREMENT, new Integer(V_UNITINCREMENT));
	activefields.put(N_USEEDITHIGHLIGHT, new Integer(V_USEEDITHIGHLIGHT));
	activefields.put(N_VALIDATE, new Integer(V_VALIDATE));
	activefields.put(N_VALIDATOR, new Integer(V_VALIDATOR));
	activefields.put(N_VALUE, new Integer(V_VALUE));
	activefields.put(N_VALUES, new Integer(V_VALUES));
	activefields.put(N_VIEWPORT, new Integer(V_VIEWPORT));
	activefields.put(N_VIEWROWCOUNT, new Integer(V_VIEWROWCOUNT));
	activefields.put(N_VIEWTOMODEL, new Integer(V_VIEWTOMODEL));
	activefields.put(N_VISIBLE, new Integer(V_VISIBLE));
	activefields.put(N_VISIBLEAMOUNT, new Integer(V_VISIBLEAMOUNT));
	activefields.put(N_VISIBLEWIDTH, new Integer(V_VISIBLEWIDTH));
	activefields.put(N_WIDTH, new Integer(V_WIDTH));
	activefields.put(N_ZOOM, new Integer(V_ZOOM));
    }

    //
    // Mac deadlock kludge flag - we eventually should investigate the
    // places in the code that seem to need this!!
    //

    private static boolean  MAC_DEADLOCK_KLUDGE = false;

    //
    // Icon and Tree node support.
    //

    private Color defaultSelectionBackground = null;
    private Color defaultSelectionForeground = null;

    private static Color javaDefaultSelectionBackground;
    private static Color javaDefaultSelectionForeground;

    static {
	javaDefaultSelectionBackground = UIManager.getColor("Tree.selectionBackground");
	javaDefaultSelectionForeground = UIManager.getColor("Tree.selectionForeground");
    }

    //
    // Constants that identify actions that need to be carried out in the
    // event thread (via invokeLater() and handleRun()). The first group
    // are the really important ones that are used to ensure thread-safe
    // behavior. The others have been around for quite a while and there's
    // a small chance they're no longer needed, at least when we're running
    // in thread-safe mode.  We will investigate later on.
    //

    private static final int  RUN_EXECUTEFIELD = 1;
    private static final int  RUN_GETFIELD = 2;
    private static final int  RUN_SETFIELD = 3;
    private static final int  RUN_PACK = 4;
    private static final int  RUN_SETVISIBLE = 5;

    private static final int  RUN_JLISTINDEX = 6;
    private static final int  RUN_JPOPUPQUIET = 7;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixBodyComponentSwing(YoixObject data) {

	//
	// We now try to add event listeners before setting fields that
	// might trigger calls to Yoix event handlers. May not be 100%,
	// but it's an improvement.
	//

	super(data);
	buildComponent();
	addAllListeners();
	setFixedSize();
	setPermissions(permissions);

	//
	// These were recently moved out of buildComponent().
	//
	setField(N_REQUESTFOCUS);
	setField(N_NEXTFOCUS);
	setField(N_FOCUSABLE);
	setField(N_VISIBLE);

	//
	// This was moved on 3/16/2010 because of an obscure deadlock that
	// happened when running in threadsafe mode. It prevents setField()
	// from pushing work into the even thread before the constructor is
	// completely finished.
	// 

	initialized = true;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceListener Methods
    //
    ///////////////////////////////////

    public void
    itemStateChanged(ItemEvent e) {

	ButtonModel  model;
	Object       source;

	//
	// N_MENUFLAGS is an integer that can be used, among other things,
	// to exercise control over the handling of ItemEvents generated
	// when checkbox or radio buttons are grouped together in a menu.
	//

	if ((data.getInt(N_MENUFLAGS, VM.getInt(N_MENUFLAGS)) & 0x01) == 0x01) {
	    //
	    // Omit DESELECTED when menu items are grouped.
	    //
	    source = e.getSource();
	    if (source instanceof JCheckBoxMenuItem || source instanceof JRadioButtonMenuItem) {
		model = ((AbstractButton)source).getModel();
		if (model instanceof DefaultButtonModel) {
		    if (((DefaultButtonModel)model).getGroup() != null) {
			if (e.getStateChange() == ItemEvent.DESELECTED)
			    e = null;
		    }
		}
	    }
	}

	if (e != null)
	    super.itemStateChanged(e);
    }

    ///////////////////////////////////
    //
    // YoixAPIProtected Methods
    //
    ///////////////////////////////////

    protected YoixObject
    executeField(String name, YoixObject argv[]) {

	YoixObject  obj = null;
	Object      result[];
	Object      comp;
	Object      lock;

	if (threadsafe && EventQueue.isDispatchThread() == false && initialized && VM.isShutdown() == false) {
	    comp = this.peer;		// snapshot - just to be safe
	    try {
		result = new Object[] {null};
		lock = new Object();
		synchronized(lock) {
		    EventQueue.invokeLater(
			new YoixAWTInvocationEvent(
			    this,
			    new Object[] {new Integer(RUN_EXECUTEFIELD), name, argv, result, lock}
			)
		    );
		    lock.wait();
		    if (result[0] instanceof Throwable) {
			if (result[0] instanceof YoixError)
			    VM.abort(((YoixError)result[0]).getDetails());
			else VM.abort((Throwable)result[0]);
		    } else obj = (YoixObject)result[0];
		}
	    }
	    catch(InterruptedException e) {}
	} else obj = handleExecuteField(name, argv);

	return(obj);
    }


    protected YoixObject
    getField(String name, YoixObject obj) {

	Object  result[];
	Object  comp;
	Object  lock;

	if (threadsafe && EventQueue.isDispatchThread() == false && initialized && VM.isShutdown() == false) {
	    comp = this.peer;		// snapshot - just to be safe
	    try {
		result = new Object[] {null};
		lock = new Object();
		synchronized(lock) {
		    EventQueue.invokeLater(
			new YoixAWTInvocationEvent(
			    this,
			    new Object[] {new Integer(RUN_GETFIELD), name, obj, result, lock}
			)
		    );
		    lock.wait();
		    if (result[0] instanceof Throwable) {
			if (result[0] instanceof YoixError)
			    VM.abort(((YoixError)result[0]).getDetails());
			else VM.abort((Throwable)result[0]);
		    } else obj = (YoixObject)result[0];
		}
	    }
	    catch(InterruptedException e) {}
	} else obj = handleGetField(name, obj);

	return(obj);
    }


    public final Rectangle
    getViewRect() {

	JScrollPane  scroller;
	JViewport    viewport;
	Rectangle    rect;

	scroller = (peer instanceof JScrollPane) ? (JScrollPane)peer : peerscroller;
	if (scroller != null) {
	    if ((viewport = scroller.getViewport()) != null)
		rect = viewport.getViewRect();
	    else rect = new Rectangle();
	} else rect = new Rectangle();

	return(rect);
    }


    public final Dimension
    getViewSize() {

	JScrollPane  scroller;
	JViewport    viewport;
	Dimension    size;

	scroller = (peer instanceof JScrollPane) ? (JScrollPane)peer : peerscroller;
	if (scroller != null) {
	    if ((viewport = scroller.getViewport()) != null)
		size = viewport.getViewSize();
	    else size = new Dimension();
	} else size = new Dimension();

	return(size);
    }


    protected YoixObject
    setField(String name, YoixObject obj) {

	boolean  later;
	Object   result[];
	Object   comp;
	Object   lock;

	//
	// Modal dialogs need special attention if we're about to show one,
	// so deciding whether or not to use invokeLater() is harder than
	// you might initially expect. Obviously modal dialogs can be shown
	// from the dispatch thread, but there are situations where it won't
	// always work properly (e.g., the blocking won't happen).
	//
	// Right now handleSetField() always returns the YoixObject that we
	// hand it, so most of the time we don't have to wait for an answer.
	// However aborts are always possible so we really do need to wait
	// if we care about accurate error messages.
	//

	if (threadsafe && EventQueue.isDispatchThread() == false && initialized && VM.isShutdown() == false) {
	    comp = this.peer;		// snapshot - just to be safe
	    if (isModalDialog(comp)) {
		if (name.equals(N_VISIBLE) && obj.isNumber() && obj.booleanValue())
		    later = false;
		else later = true;
	    } else later = true;
	    if (later) {
		try {
		    result = new Object[] {null};
		    lock = new Object();
		    synchronized(lock) {
			EventQueue.invokeLater(
			    new YoixAWTInvocationEvent(
				this,
				new Object[] {new Integer(RUN_SETFIELD), name, obj, result, lock}
			    )
			);
			lock.wait();
			if (result[0] instanceof Throwable) {
			    if (result[0] instanceof YoixError)
				VM.abort(((YoixError)result[0]).getDetails());
			    else VM.abort((Throwable)result[0]);
			} else obj = (YoixObject)result[0];
		    }
		}
		catch(InterruptedException e) {}
	    } else handleSetField(name, obj);
	} else handleSetField(name, obj);

	return(obj);
    }

    ///////////////////////////////////
    //
    // YoixBodyComponentSwing Methods
    //
    ///////////////////////////////////

    final int
    addListeners(int mask) {

	Object  comp;
	Object  editor;
	int     missed;
	int     listener;
	int     bit;

	comp = this.peer;		// snapshot - just to be safe
	missed = 0;

	for (bit = 1; mask != 0 && bit != NEXTLISTENER; bit <<= 1) {
	    switch (listener = (mask & bit)) {
		case ACTIONLISTENER:
		    if (comp instanceof AbstractButton)
			((AbstractButton)comp).addActionListener(this);
		    else if (comp instanceof JTextField)
			((JTextField)comp).addActionListener(this);
		    else if (comp instanceof YoixSwingJCanvas)
			((YoixSwingJCanvas)comp).addActionListener(this);
		    else if (comp instanceof YoixSwingJComboBox)
			((YoixSwingJComboBox)comp).addActionListener(this);
		    else if (comp instanceof YoixSwingJFileChooser)
			((YoixSwingJFileChooser)comp).addActionListener(this);
		    else missed |= listener;
		    break;

		case ADJUSTMENTLISTENER:
		    if (comp instanceof JScrollPane)
			addAdjustmentListener((JScrollPane)comp);
		    else if (peerscroller != null)
			addAdjustmentListener(peerscroller);
		    else missed |= listener;
		    break;

		case CARETLISTENER:
		    if (comp instanceof JTextComponent)
			((JTextComponent)comp).addCaretListener(this);
		    else missed |= listener;
		    break;

		case CHANGELISTENER:
		    if (comp instanceof AbstractButton)
			((AbstractButton)comp).addChangeListener(this);
		    else if (comp instanceof JProgressBar)
			((JProgressBar)comp).addChangeListener(this);
		    else if (comp instanceof YoixSwingJSlider)
			((YoixSwingJSlider)comp).addChangeListener(this);
		    else if (comp instanceof JTabbedPane)
			((JTabbedPane)comp).addChangeListener(this);
		    else if (comp instanceof YoixSwingJColorChooser)
			((YoixSwingJColorChooser)comp).addChangeListener(this);
		    else missed |= listener;
		    break;

	        case FOCUSLISTENER:
		    if ((editor = getJComboBoxTextEditor(comp)) != null) {
			if (((JComboBox)comp).isEditable()) {
			    ((JComboBox)comp).setFocusable(true);
			    ((JTextComponent)editor).setFocusable(true);
			    ((JTextComponent)editor).addFocusListener(this);
			} else {
			    ((JTextComponent)editor).setFocusable(true);
			    missed |= listener; // want YoixBodyComponent to handle this one
			}
		    } else missed |= listener;
		    break;

		case HYPERLINKLISTENER:
		    if (comp instanceof JEditorPane)
			((JEditorPane)comp).addHyperlinkListener(this);
		    else missed |= listener;
		    break;

		case INVOCATIONACTIONLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setTableHeader(true);
		    else missed |= listener;
		    break;

		case INVOCATIONBROWSELISTENER:
		    if (comp instanceof YoixSwingJTree) {
			((YoixSwingJTree)comp).addTreeExpansionListener((YoixSwingJTree)comp);
			((YoixSwingJTree)comp).addTreeWillExpandListener((YoixSwingJTree)comp);
		    } else missed |= listener;
		    break;

		case INVOCATIONCHANGELISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setChangeListenerMode(true);
		    else missed |= listener;
		    break;

		case INVOCATIONEDITLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setEditListenerMode(true);
		    else missed |= listener;
		    break;

		case INVOCATIONEDITIMPORTLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setEditImportListenerMode(true);
		    else missed |= listener;
		    break;

		case INVOCATIONEDITKEYLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setEditKeyListenerMode(true);
		    else missed |= listener;
		    break;

		case INVOCATIONSELECTIONLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).addMouseListener((YoixSwingJTable)comp);
		    else missed |= listener;
		    break;

		case SELECTIONLISTENER:
		    if (comp instanceof JList)
			((JList)comp).addListSelectionListener(this);
		    else if (comp instanceof YoixSwingJTree)
			((YoixSwingJTree)comp).addTreeSelectionListener(this);
		    else missed |= listener;
		    break;

		case TEXTLISTENER:
		    if (comp instanceof JTextComponent) {
			if (ytvcl != null)
			    ((JTextComponent)comp).getDocument().removeDocumentListener(ytvcl);
			((JTextComponent)comp).getDocument().addDocumentListener(ytvcl = new YoixTextValueChangedListener(this, (JTextComponent)comp));
		    } else if (comp instanceof JComboBox && (editor = ((JComboBox)comp).getEditor()) != null && (editor = ((ComboBoxEditor)editor).getEditorComponent()) instanceof JTextComponent) {
			if (ytvcl != null)
			    ((JTextComponent)editor).getDocument().removeDocumentListener(ytvcl);
			((JTextComponent)editor).getDocument().addDocumentListener(ytvcl = new YoixTextValueChangedListener(this, (JTextComponent)editor));
		    } else missed |= listener;
		    break;

		case VERIFIERLISTENER:
		    if (comp instanceof JComponent)
			((JComponent)comp).setInputVerifier(new YoixInputVerifier(getContext()));
		    else missed |= listener;
		    break;

		case WINDOWLISTENER:
		    if (comp instanceof JInternalFrame)
			((JInternalFrame)comp).addInternalFrameListener(this);
		    else missed |= listener;
		    break;

		default:
		    missed |= listener;
		    break;
	    }
	    mask &= ~bit;
	}

	return(super.addListeners(comp, missed));
    }


    final void
    dispose(boolean finalizing) {

	YoixObject  owner;
	Rectangle   bounds;
	Object      comp;

	//
	// Added some code on 3/24/07 to try to help with a memory leak
	// that we saw in a large application. We eventually need to take
	// a close look at them and the original code. Also should look
	// at the AWT version of this method.
	//

	if (isDisposable()) {
	    comp = this.peer;		// snapshot - probably unnecessary
	    owner = this.parent;	// snapshot - just to be safe
	    if (comp instanceof Component)	// a recent addition
		((Component)comp).setVisible(false);
	    //
	    // A change (3/24/07) to try to help with a memory leak that
	    // we saw in one large application. Not sure about this right
	    // now so it needs to be checked.
	    //
	    if (comp instanceof YoixInterfaceMenuBar)
		((YoixInterfaceMenuBar)comp).changeMenuBar(null, false, false);
	    if (comp instanceof Container) {
		//
		// Old version just did this, which probably now isn't
		// necessary. Going to leave it in just to be safe, but
		// when we get time to test we probably will remove it.
		// In fact this isn't complete because a Swing dialog
		// can have a menubar.
		//
		if (comp instanceof JFrame)
		    YoixMiscMenu.removeListener(((JFrame)comp).getJMenuBar(), this);
		if (comp instanceof YoixInterfaceWindow) {
		    if (finalizing == false) {
			bounds = ((Component)comp).getBounds();
			if (data.writable(N_LOCATION))
			    data.put(N_LOCATION, YoixMakeScreen.yoixPoint(bounds.getLocation()));
			if (data.writable(N_SIZE))
			    data.put(N_SIZE, YoixMakeScreen.yoixDimension(bounds.getSize()));
		    }
		    YoixMiscJFC.dispose(comp);
		}
	    }

	    if (owner != null)
		((YoixBodyComponent)owner.body()).childrenRemove(this);

	    childrenDispose();
	    VM.removeClipboards(getContext());	// not completely convinced - added 5/12/05

	    //
	    // Clearing the menubar and popup fields is a change that was
	    // added on 3/24/07. It's mostly a precaution that probably
	    // isn't really needed, but it also doesn't hurt, so we'll
	    // leave it in for the time being. Doubt this had any part
	    // in the memory management issues that we were tried to fix
	    // on 3/24/07.
	    //

	    disposeAllMenus();
/****
	    if (data.defined(N_MENUBAR))
		data.putObject(N_MENUBAR, null);
	    if (data.defined(N_POPUP))
		data.putObject(N_POPUP, null);
****/

	    this.peer = null;
	    this.parent = null;
	    windowDeactivate(this);
	}
    }


    protected void
    finalize() {

	menubarowner = null;
	popupowner = null;
	peerscroller = null;
	if(peer instanceof JPopupMenu && popupmenulistener != null) {
	    ((JPopupMenu)peer).removePopupMenuListener(popupmenulistener);
	    popupmenulistener = null;
	}
	super.finalize();
    }


    final Adjustable
    getAdjustable(int orientation) {

	JScrollPane  scroller;
	Adjustable   adjustable;
	Object       comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof JScrollPane) {
	    if (orientation == YOIX_HORIZONTAL)
		adjustable = ((JScrollPane)comp).getHorizontalScrollBar();
	    else adjustable = ((JScrollPane)comp).getVerticalScrollBar();
	} else if ((scroller = peerscroller) != null) {
	    if (orientation == YOIX_HORIZONTAL)
		adjustable = scroller.getHorizontalScrollBar();
	    else adjustable = scroller.getVerticalScrollBar();
	} else adjustable = null;

	return(adjustable);
    }


    final Container
    getContainer() {

	return(getContainer(this.peer));
    }


    final Container
    getContainer(Object arg) {

	Container  container = null;

	if (isContainer(arg)) {
	    if (arg instanceof RootPaneContainer)
		container = ((RootPaneContainer)arg).getContentPane();
	    else if (arg instanceof JScrollPane)
		container = ((JScrollPane)arg).getViewport();
	    else if (arg instanceof Container)		// unnecessary test??
		container = (Container)arg;
	}
	return(container);
    }


    final int
    getFieldCode(String name) {

	return(activeField(name, activefields));
    }


    public final JScrollPane
    getPeerScroller() {

	return(peerscroller);
    }


    final Dimension
    getScrollerSize() {

	JScrollPane  scroller;
	Dimension    size = null;

	if ((scroller = peerscroller) != null)
	    size = scroller.getSize();
	return(size);
    }


    final YoixObject
    getSize(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = super.getSize(comp, obj);
	else if (comp instanceof ButtonGroup)
	    obj = YoixObject.newInt(((ButtonGroup)comp).getButtonCount() - 1); // subtract the dummy button
	return(obj);
    }


    final static synchronized boolean
    getThreadSafe() {

	return(threadsafe);
    }


    final Dimension
    getViewportSize(boolean adjusted) {

	JScrollPane  scroller;
	Dimension    size = null;

	if ((scroller = peerscroller) != null) {
	    if (scroller instanceof YoixSwingJScrollPane) {	// should always be true
		if (adjusted)
		    size = ((YoixSwingJScrollPane)scroller).getAdjustedViewportSize();
		else size = scroller.getViewport().getSize();
	    } else size = scroller.getViewport().getSize();
	}
	return(size);
    }


    public final void
    handleRun(Object args[]) {

	YoixError  error_point;

	//
	// The RUN_JLISTINDEX and RUN_JPOPUPQUIET may not be necessary when
	// we're running in thread-safe mode. They were the first two cases
	// this method handled and they were here long before the thread-safe
	// cases.
	//

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case RUN_EXECUTEFIELD:
		    synchronized(args[4]) {
			error_point = VM.pushError();
			try {
			    ((Object[])args[3])[0] = handleExecuteField((String)args[1], (YoixObject[])args[2]);
			    VM.popError();
			}
			catch(Throwable t) {
			    if (t != error_point)
				VM.popError();
			    ((Object[])args[3])[0] = t;
			}
			finally {
			    args[4].notify();
			}
		    }
		    break;

		case RUN_GETFIELD:
		    synchronized(args[4]) {
			error_point = VM.pushError();
			try {
			    ((Object[])args[3])[0] = handleGetField((String)args[1], (YoixObject)args[2]);
			    VM.popError();
			}
			catch(Throwable t) {
			    if (t != error_point)
				VM.popError();
			    ((Object[])args[3])[0] = t;
			}
			finally {
			    args[4].notify();
			}
		    }
		    break;

		case RUN_JLISTINDEX:
		    handleJListIndex((JList)args[1], (YoixObject)args[2]);
		    break;

		case RUN_JPOPUPQUIET:
		    YoixMiscMenu.removeListener((JPopupMenu)args[1], (YoixBodyComponent)args[2]);
		    break;

		case RUN_PACK:
		    synchronized(args[1]) {
			try {
			    handlePack();
			}
			finally {
			    args[1].notify();
			}
		    }
		    break;

		case RUN_SETFIELD:
		    //
		    // Right now setField() always returns the YoixObject that
		    // we hand it, so for the most part the caller shouldn't
		    // have to wait around for the answer. However aborts are
		    // always possible so we really should wait around to make
		    // make sure the operation completed successfully. Also, if
		    // we don't wait error handling code can misbehave.
		    // 
		    synchronized(args[4]) {
			error_point = VM.pushError();
			try {
			    ((Object[])args[3])[0] = handleSetField((String)args[1], (YoixObject)args[2]);
			    VM.popError();
			}
			catch(Throwable t) {
			    if (t != error_point)
				VM.popError();
			    ((Object[])args[3])[0] = t;
			}
			finally {
			    args[4].notify();
			}
		    }
		    break;

		case RUN_SETVISIBLE:
		    synchronized(args[2]) {
			try {
			    handleSetVisible(((Boolean)args[1]).booleanValue());
			}
			finally {
			    args[2].notify();
			}
		    }
		    break;
	    }
	}
    }


    final boolean
    isContainer() {

	return(isContainer(this.peer));
    }


    final boolean
    isContainer(Object arg) {

	boolean  result;

	if ((arg instanceof YoixSwingJCanvas) == false) {
	    if (arg instanceof JComponent) {
		result = arg instanceof JPanel ||
		    arg instanceof JScrollPane ||
		    arg instanceof JSplitPane ||
		    arg instanceof JTabbedPane ||
		    arg instanceof JToolBar ||
		    arg instanceof JDesktopPane ||
		    arg instanceof JLayeredPane ||
		    arg instanceof JMenuBar ||
		    arg instanceof JPopupMenu ||
		    arg instanceof JMenu;
	    } else result = arg instanceof Container;
	} else result = false;

	return(result);
    }


    final boolean
    isMenu() {

	return(isMenu(peer));
    }


    final boolean
    isMenu(Object arg) {

	return(arg instanceof JMenuBar || arg instanceof JPopupMenu || arg instanceof JMenu);
    }


    final boolean
    isPopupMenu() {

	return(isPopupMenu(peer));
    }


    final boolean
    isPopupOwner() {

	return(isPopupOwner(peer));
    }


    final int
    removeListeners(int mask) {

	Object  comp;
	Object  editor;
	int     missed;
	int     listener;
	int     bit;


	comp = this.peer;		// snapshot - just to be safe
	missed = 0;

	for (bit = 1; mask != 0 && bit != NEXTLISTENER; bit <<= 1) {
	    switch (listener = (mask & bit)) {
		case ACTIONLISTENER:
		    if (comp instanceof AbstractButton)
			((AbstractButton)comp).removeActionListener(this);
		    else if (comp instanceof JTextField)
			((JTextField)comp).removeActionListener(this);
		    else if (comp instanceof YoixSwingJCanvas)
			((YoixSwingJCanvas)comp).removeActionListener(this);
		    else if (comp instanceof YoixSwingJComboBox)
			((YoixSwingJComboBox)comp).removeActionListener(this);
		    else if (comp instanceof YoixSwingJFileChooser)
			((YoixSwingJFileChooser)comp).removeActionListener(this);
		    else missed |= listener;
		    break;

		case ADJUSTMENTLISTENER:
		    if (comp instanceof JScrollPane)
			removeAdjustmentListener((JScrollPane)comp);
		    else if (peerscroller != null)
			removeAdjustmentListener(peerscroller);
		    else missed |= listener;
		    break;

		case CARETLISTENER:
		    if (comp instanceof JTextComponent)
			((JTextComponent)comp).removeCaretListener(this);
		    else missed |= listener;
		    break;

		case CHANGELISTENER:
		    if (comp instanceof AbstractButton)
			((AbstractButton)comp).removeChangeListener(this);
		    else if (comp instanceof JProgressBar)
			((JProgressBar)comp).removeChangeListener(this);
		    else if (comp instanceof YoixSwingJSlider)
			((YoixSwingJSlider)comp).removeChangeListener(this);
		    else if (comp instanceof JTabbedPane)
			((JTabbedPane)comp).removeChangeListener(this);
		    else if (comp instanceof YoixSwingJColorChooser)
			((YoixSwingJColorChooser)comp).removeChangeListener(this);
		    else missed |= listener;
		    break;

	        case FOCUSLISTENER:
		    if ((editor = getJComboBoxTextEditor(comp)) != null)
			((JTextComponent)editor).removeFocusListener(this);
		    else missed |= listener;
		    break;

		case HYPERLINKLISTENER:
		    if (comp instanceof JEditorPane)
			((JEditorPane)comp).removeHyperlinkListener(this);
		    else missed |= listener;
		    break;

		case INVOCATIONACTIONLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setTableHeader(false);
		    else missed |= listener;
		    break;

		case INVOCATIONBROWSELISTENER:
		    if (comp instanceof YoixSwingJTree) {
			((YoixSwingJTree)comp).removeTreeExpansionListener((YoixSwingJTree)comp);
			((YoixSwingJTree)comp).removeTreeWillExpandListener((YoixSwingJTree)comp);
		    } else missed |= listener;
		    break;

		case INVOCATIONCHANGELISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setChangeListenerMode(false);
		    else missed |= listener;
		    break;

		case INVOCATIONEDITLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setEditListenerMode(false);
		    else missed |= listener;
		    break;

		case INVOCATIONEDITIMPORTLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setEditImportListenerMode(false);
		    else missed |= listener;
		    break;

		case INVOCATIONEDITKEYLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).setEditKeyListenerMode(false);
		    else missed |= listener;
		    break;

		case INVOCATIONSELECTIONLISTENER:
		    if (comp instanceof YoixSwingJTable)
			((YoixSwingJTable)comp).removeMouseListener((YoixSwingJTable)comp);
		    else missed |= listener;
		    break;

		case SELECTIONLISTENER:
		    if (comp instanceof JList)
			((JList)comp).removeListSelectionListener(this);
		    else if (comp instanceof YoixSwingJTree)
			((YoixSwingJTree)comp).removeTreeSelectionListener(this);
		    else missed |= listener;
		    break;

		case TEXTLISTENER:
		    if (comp instanceof JTextComponent) {
			if (ytvcl != null) {
			    ((JTextComponent)comp).getDocument().removeDocumentListener(ytvcl);
			    ytvcl = null;
			}
		    } else if (comp instanceof JComboBox && (editor = ((JComboBox)comp).getEditor()) != null && (editor = ((ComboBoxEditor)editor).getEditorComponent()) instanceof JTextComponent) {
			if (ytvcl != null) {
			    ((JTextComponent)editor).getDocument().removeDocumentListener(ytvcl);
			    ytvcl = null;
			}
		    } else missed |= listener;
		    break;

		case VERIFIERLISTENER:
		    if (comp instanceof JComponent)
			((JComponent)comp).setInputVerifier(null);
		    else missed |= listener;
		    break;

		case WINDOWLISTENER:
		    if (comp instanceof JInternalFrame)
			((JInternalFrame)comp).removeInternalFrameListener(this);
		    else missed |= listener;
		    break;

		default:
		    missed |= listener;
		    break;
	    }
	    mask &= ~bit;
	}

	return(super.removeListeners(comp, missed));
    }


    final synchronized int
    replaceText(int offset, int length, String str, boolean adjust, ArrayList undo) {

	Highlighter  highlighter;
	Document     doc;
	boolean      trim;
	String       dest;
	String       text;
	Object       comp;
	int          delta = 0;
	int          len;

	//
	// Punted, for now, on replacing at an arbitrary offset in a
	// JEditorPane. We currently can only append to the end of the
	// text that's displayed by the JEditorPane. Builtins that can
	// end up here are defined in YoixModuleJFC.java, and there's
	// a chance they're running argument checks that prevent some
	// calls to this method, so check the builtins after you make
	// JEditorPane related changes in this method.
	//

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof Component && canTextChange()) {
	    trim = data.getBoolean(N_AUTOTRIM);
	    if (comp instanceof JTextComponent) {
		highlighter = ((JTextComponent)comp).getHighlighter();
		if (!(highlighter instanceof YoixSwingHighlighter)) {
		    //
		    // Follows closely the YoixMisc.replaceText method,
		    // but uses the Document object to perform the
		    // manipulations.
		    //
		    doc = ((JTextComponent)comp).getDocument();
		    len = doc.getLength();
		    str = (str != null) ? str : "";
		    offset = Math.max(0, Math.min(offset, len));
		    length = Math.max(0, length);
		    trim = data.getBoolean(N_AUTOTRIM, false);
		    if (trim) {
			if (offset == 0)
			    str = YoixMisc.trimWhiteSpace(str, true, false);
			else if (offset == len)
			    str = YoixMisc.trimWhiteSpace(str, false, true);
		    }
		    if (undo != null) {
			undo.add(new Integer(offset));
			undo.add(new Integer(str.length()));
			try {
			    undo.add(doc.getText(offset, length));
			}
			catch(BadLocationException e) {
			    VM.abort(e);		// should never happen
			}
		    }
		    if (length == 0) {
			if (str.length() > 0) {
			    try {
				doc.insertString(offset, str, null);
			    }
			    catch(BadLocationException e) {
				VM.abort(e);		// should never happen
			    }
			}
		    } else {
			try {
			    doc.remove(offset, length);
			    doc.insertString(offset, str, null);
			}
			catch(BadLocationException e) {
			    VM.abort(e);		// should never happen
			}
		    }
		    delta = doc.getLength() - len;
		} else {
		    //
		    // Probably can apply the above approach to the highlighter
		    // case, but for now we will just leave it be.
		    //
		    delta = ((YoixSwingHighlighter)highlighter).replaceText(offset, length, str, adjust, undo);
		}
	    } else if (comp instanceof JLabel) {
		dest = ((JLabel)comp).getText();
		text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		delta = text.length() - dest.length();
		//
		// JLabel kludge - make sure text isn't null or empty
		//
		((JLabel)comp).setText(text != null && text.length() > 0 ? text : " ");
	    } else if (comp instanceof AbstractButton) {
		dest = ((AbstractButton)comp).getText();
		text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		delta = text.length() - dest.length();
		((AbstractButton)comp).setText(text);
	    } else if (comp instanceof YoixSwingJTextComponent) {
		dest = ((YoixSwingJTextComponent)comp).getText();
		text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		delta = text.length() - dest.length();
		((YoixSwingJTextComponent)comp).setText(text);
	    } else delta = super.replaceText(offset, length, str, adjust, undo);
	}

	return(delta);
    }


    final void
    setForeground(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    super.setForeground(comp, obj);
	    syncTabProperty((Component)comp, N_FOREGROUND, ((Component)comp).getForeground());
	}
    }


    final synchronized void
    setMenuBarOwner(YoixBodyComponent owner) {

	//
	// Listeners should be added and removed by changeMenuBar(), which
	// YoixInterfaceMenuBar objects must implement.
	//

	menubarowner = owner;
	syncMenuProperties(owner);
    }


    final synchronized void
    setPopupOwner(YoixBodyComponent owner) {

	Object  component;
	Object  comp;

	//
	// In older versions this method was responsible for adding and
	// removing listeners for the popupmenu, but that job was moved
	// in version 2.1.4 (around 1/23/07).
	//

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof JPopupMenu) {
	    if (popupowner != null && popupowner != owner)
		popupowner = null;
	    if (owner != null) {
 		if (popupowner != owner) {
 		    if ((component = owner.getManagedObject()) != null) {
 			if (component instanceof Component)
			    popupowner = owner;
 			else VM.abort(TYPECHECK, N_POPUP);
 		    }
 		}
		syncMenuProperties(owner);
		showPopupMenu((JPopupMenu)comp);
	    }
	}
    }


    final static synchronized boolean
    setThreadSafe(boolean state, boolean locked) {

	if (threadsafe_locked == false) {
	    threadsafe = state;
	    threadsafe_locked = locked;
	}
	return(threadsafe);
    }


    final void
    specialAddToProcessing(Object comp, Container pane, YoixObject child, Object constraint, int index) {

	Component  component = null;
	Object     body;

	//
	// Caller should be catching RuntimeExceptions, so we can be a bit
	// careless.
	//

	if ((body = child.body()) != null) {
	    if (body instanceof YoixBodyComponentSwing)
		component = ((YoixBodyComponentSwing)body).peerscroller;
	}

	if (component == null)
	    component = (Component)child.getManagedObject();

	if (component instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)component).disposeSavedGraphics();

	if (comp instanceof JScrollPane) {
	    switch (constraint != null ? ((Integer)constraint).intValue() : YOIX_CENTER) {
		case YOIX_LOWER_LEFT_CORNER:
		    ((JScrollPane)comp).setCorner(JScrollPane.LOWER_LEFT_CORNER, component);
		    break;

		case YOIX_LOWER_RIGHT_CORNER:
		    ((JScrollPane)comp).setCorner(JScrollPane.LOWER_RIGHT_CORNER, component);
		    break;

		case YOIX_UPPER_LEFT_CORNER:
		    ((JScrollPane)comp).setCorner(JScrollPane.UPPER_LEFT_CORNER, component);
		    break;

		case YOIX_UPPER_RIGHT_CORNER:
		    ((JScrollPane)comp).setCorner(JScrollPane.UPPER_RIGHT_CORNER, component);
		    break;

		case YOIX_LEFT:
		    ((JScrollPane)comp).setRowHeaderView(component);
		    break;

		case YOIX_TOP:
		    ((JScrollPane)comp).setColumnHeaderView(component);
		    break;

		case YOIX_HORIZONTAL:
		    if (component instanceof YoixSwingJScrollBar)
			((YoixSwingJScrollBar)component).setScrollPane((JScrollPane)comp, JScrollBar.HORIZONTAL);
		    else VM.abort(BADVALUE, N_LAYOUT, index);
		    break;

		case YOIX_VERTICAL:
		    if (component instanceof YoixSwingJScrollBar)
			((YoixSwingJScrollBar)component).setScrollPane((JScrollPane)comp, JScrollBar.VERTICAL);
		    else VM.abort(BADVALUE, N_LAYOUT, index);
		    break;

		case YOIX_CENTER:
		default:
		    ((JScrollPane)comp).getViewport().setView(component);
		    if (comp instanceof YoixSwingJScrollPane)
			((YoixSwingJScrollPane)comp).inheritSizeControl(child.getObject(N_SIZECONTROL));
		    break;
	    }
	} else if (comp instanceof JSplitPane) {
	    if (constraint == JSplitPane.LEFT)
		((JSplitPane)comp).setLeftComponent(component);
	    else ((JSplitPane)comp).setRightComponent(component);
	} else pane.add(component, constraint);
    }


    final boolean
    specialLayout(YoixObject obj, Object comp, Container pane, YoixObject added, boolean fronttoback) {

	boolean  handled = true;

	if (comp instanceof JDesktopPane)	// test before JLayeredPane
	    addToJDesktopPane(obj, added, getContext());
	else if (comp instanceof JLayeredPane)
	    addToJLayeredPane(obj, added);
	else if (comp instanceof JScrollPane)
	    addToJScrollPane(obj, added);
	else if (comp instanceof JSplitPane)
	    addToJSplitPane(obj, added);
	else if (comp instanceof JTabbedPane)
	    addToJTabbedPane(obj, added, (JTabbedPane)comp);
	else if (comp instanceof JToolBar)
	    addToJToolBar(obj, added, (JToolBar)comp);
	else if (comp instanceof YoixSwingJFileDialog)
	    addToJFileDialog(obj, added, fronttoback, (YoixSwingJFileDialog)comp);
	else handled = false;

	return(handled);
    }


    final void
    specialRemoveAll(Container pane) {

	if (pane instanceof JSplitPane) {
	    ((JSplitPane)pane).setLeftComponent(null);
	    ((JSplitPane)pane).setRightComponent(null);
	} else pane.removeAll();
    }


    final void
    syncMenuProperties(YoixBodyComponent owner) {

	YoixObject  value;
	Object      component;
	Object      comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof JMenuBar || comp instanceof JPopupMenu) {
	    if (owner != null) {
		if ((component = owner.getManagedObject()) != null) {
		    if (component instanceof Component) {
			if ((value = data.getObject(N_BACKGROUND)) == null || value.isNull())
			    syncMenuProperty(comp, N_BACKGROUND, ((Component)component).getBackground());
			if ((value = data.getObject(N_FOREGROUND)) == null || value.isNull())
			    syncMenuProperty(comp, N_FOREGROUND, ((Component)component).getForeground());
			if ((value = data.getObject(N_FONT)) == null || value.isNull())
			    syncMenuProperty(comp, N_FONT, ((Component)component).getFont());
			if (component instanceof JComponent) {
			    if ((value = data.getObject(N_OPAQUE)) == null || value.isNull())
				syncMenuProperty(comp, N_OPAQUE, new Boolean(((JComponent)component).isOpaque()));
			}
		    }
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addAdjustmentListener(JScrollPane scroller) {

	JScrollBar  scrollbar;

	if (scroller != null) {
	    if ((scrollbar = scroller.getHorizontalScrollBar()) != null)
		scrollbar.addAdjustmentListener(this);
	    if ((scrollbar = scroller.getVerticalScrollBar()) != null)
		scrollbar.addAdjustmentListener(this);
	}
    }


    private void
    addToJDesktopPane(YoixObject add, YoixObject added, YoixObject desktop) {

	YoixBodyComponent  body;
	YoixObject         child;
	YoixObject         location;
	YoixObject         size;
	Component          component;
	Rectangle          bounds;
	int                layer;
	int                length;
	int                n;

	bounds = null;
	length = add.length();

	for (n = 0; n < length; n++) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    if (child.isComponent() || (child = pickLayoutComponent(child)) != null) {
			body = (YoixBodyComponent)child.body();
			component = (Component)child.getManagedObject();
			layer = pickLayer(child.getObject(N_LAYER));
			if (component instanceof YoixSwingJInternalFrame)
			    child.put(N_DESKTOP, desktop, false);
			else addTo(child, new Integer(layer), added, n);
			if ((size = body.data.getObject(N_SIZE)) != null) {
			    if (size.notNull())
				component.setSize(YoixMakeScreen.javaDimension(size));
			    else component.setSize(component.getPreferredSize());
			} else component.setSize(component.getPreferredSize());
			if ((location = child.getObject(N_LOCATION)) != null) {
			    if (location.notNull())
				component.setLocation(YoixMakeScreen.javaPoint(location));
			}
			if (bounds == null)
			    bounds = new Rectangle();
			bounds = bounds.union(component.getBounds());
		    } else VM.abort(BADVALUE, N_LAYOUT, n);
		}
	    }
	}
	layoutsize = (bounds != null) ? bounds.getSize() : null;
    }


    private void
    addToJFileDialog(YoixObject add, YoixObject added, boolean fronttoback, YoixSwingJFileDialog container) {

	YoixObject  child;

	if ((child = container.getFileChooser()) != null)
	    addTo(child, BorderLayout.CENTER, added, 0);
    }


    private void
    addToJLayeredPane(YoixObject add, YoixObject added) {

	YoixBodyComponent  body;
	YoixObject         child;
	YoixObject         location;
	YoixObject         size;
	Component          component;
	Rectangle          bounds;
	int                layer;
	int                length;
	int                n;

	bounds = null;
	length = add.length();

	for (n = 0; n < length; n++) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    if (child.isComponent() || (child = pickLayoutComponent(child)) != null) {
			body = (YoixBodyComponent)child.body();
			component = (Component)child.getManagedObject();
			layer = pickLayer(child.getObject(N_LAYER));
			addTo(child, new Integer(layer), added, n);
			if ((size = body.data.getObject(N_SIZE)) != null) {
			    if (size.notNull())
				component.setSize(YoixMakeScreen.javaDimension(size));
			    else component.setSize(component.getPreferredSize());
			} else component.setSize(component.getPreferredSize());
			if ((location = child.getObject(N_LOCATION)) != null) {
			    if (location.notNull())
				component.setLocation(YoixMakeScreen.javaPoint(location));
			}
			if (bounds == null)
			    bounds = new Rectangle();
			bounds = bounds.union(component.getBounds());
		    } else VM.abort(BADVALUE, N_LAYOUT, n);
		}
	    }
	}
	layoutsize = (bounds != null) ? bounds.getSize() : null;
    }


    private void
    addToJScrollPane(YoixObject add, YoixObject added) {

	YoixObject  child;
	YoixObject  where;
	Object      constraint;
	int         length;
	int         n;

	length = add.length();

	for (n = 0; n < length; n += 2) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    constraint = null;
		    if ((where = add.getObject(n + 1)) != null) {
			if (where.isInteger())
			    constraint = jfcObject("JScrollPane", where.intValue());
			else if (where.isString())
			    constraint = jfcObject("JScrollPane", where.stringValue());
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
    addToJSplitPane(YoixObject add, YoixObject added) {

	YoixObject  child;
	Object      constraint;
	int         length;
	int         n;

	length = Math.min(add.length(), 2);

	for (n = 0; n < length; n++) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    constraint = (n == 0) ? JSplitPane.LEFT : JSplitPane.RIGHT;
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
    addToJTabbedPane(YoixObject add, YoixObject added, JTabbedPane container) {

	YoixObject  child;
	YoixObject  icon;
	YoixObject  title;
	YoixObject  tooltip;
	Component   component;
	int         index;
	int         length;
	int         n;

	length = add.length();

	for (n = 0; n < length; n++) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    if (child.isComponent() || (child = pickLayoutComponent(child)) != null) {
			component = (Component)child.getManagedObject();
			addTo(child, null, added, n);
			if ((index = container.indexOfComponent(component)) >= 0) {
			    if ((title = child.getObject(N_TITLE)) != null) {
				if (title.notNull()) {
				    if (title.isString())
					container.setTitleAt(index, title.stringValue());
				}
			    }
			    if ((icon = child.getObject(N_ICON)) != null) {
				if (icon.notNull()) {
				    if (icon.isImage())
					container.setIconAt(index, YoixMake.javaIcon(icon));
				}
			    }
			    container.setBackgroundAt(index, component.getBackground());
			    container.setForegroundAt(index, component.getForeground());
			    container.setEnabledAt(index, component.isEnabled());
			}
		    } else VM.abort(BADVALUE, N_LAYOUT, n);
		}
	    }
	}
    }


    private void
    addToJToolBar(YoixObject add, YoixObject added, JToolBar container) {

	YoixObject  child;
	Dimension   size;
	int         length;
	int         n;

	length = add.length();

	for (n = 0; n < length; n++) {
	    if ((child = add.getObject(n)) != null) {
		if (child.notNull()) {
		    if (child.isNumber() || child.isDimension()) {
			size = null;
			if (child.isNumber()) {
			    switch (container.getOrientation()) {
				case SwingConstants.HORIZONTAL:
				    size = new Dimension(YoixMakeScreen.javaDistance(child.doubleValue()), 0);
				    break;

				case SwingConstants.VERTICAL:
				    size = new Dimension(0, YoixMakeScreen.javaDistance(child.doubleValue()));
				    break;
			    }
			} else size = YoixMakeScreen.javaDimension(child);
			if (size != null) {
			    if (size.width > 0 || size.height > 0)
				container.addSeparator(size);
			    else container.addSeparator(null);
			}
		    } else if (child.isComponent())
			addTo(child, null, added, n);
		    else if ((child = pickLayoutComponent(child)) != null)
			addTo(child, null, added, n);
		    else VM.abort(BADVALUE, N_LAYOUT, n);
		}
	    }
	}
    }


    private void
    addToMenu(Container menu, YoixObject root, YoixObject list, YoixObject added) {

	YoixObject  elem;
	YoixObject  components;
	YoixObject  ybg;
	boolean     ismenubar;
	boolean     ispopupmenu;
	Object      elempeer;
	String      tag;
	int         length;
	int         i;
	int         n;

	length = list.length();
	if (list.notNull() && length > 0) {
	    ispopupmenu = (menu instanceof JPopupMenu);
	    ismenubar = (menu instanceof JMenuBar);
	    for (i = 0; i < length; i++) {
		elem = list.get(i, false);
		if (ismenubar && elem.isInteger() && elem.intValue() == YOIX_RIGHT)
		    ((JMenuBar)menu).add(Box.createHorizontalGlue());
		else if (elem.isNull() && !ismenubar) {
		    if (ispopupmenu)
			((JPopupMenu)menu).add(new JSeparator(JSeparator.HORIZONTAL));
		    else ((JMenu)menu).add(new JSeparator(JSeparator.HORIZONTAL));
		} else if (elem.isString() && !ismenubar) {
		    if (elem.stringValue().equals("-")) {
			if (ispopupmenu)
			    ((JPopupMenu)menu).add(new JSeparator(JSeparator.HORIZONTAL));
			else ((JMenu)menu).add(new JSeparator(JSeparator.HORIZONTAL));
		    } else VM.abort(BADMENUITEM, i);
		} else if (elem.isComponent()) {
		    elem.put(N_ROOT, root, true);
		    tag = elem.getString(N_TAG);
		    if (added.defined(tag))
			VM.abort(DUPLICATETAG, tag);
		    else added.put(tag, elem);
		    elempeer = elem.getManagedObject();
		    if (elempeer instanceof JMenu) {
			menu.add((JMenu)elempeer);
			if ((components = elem.getObject(N_COMPONENTS)) != null) {
			    for (n = 0; n < components.length(); n++) {
				if (components.defined(n)) {
				    elem = components.getObject(n);
				    tag = elem.getString(N_TAG);
				    if (added.defined(tag))
					VM.abort(DUPLICATETAG, tag);
				    else added.put(tag, elem);
				    elem.put(N_ROOT, root, true);
				}
			    }
			}
		    } else if (elempeer instanceof JCheckBoxMenuItem && !ismenubar) {
			menu.add((JCheckBoxMenuItem)elempeer);
			if ((ybg = elem.getObject(N_GROUP)) != null && ybg.notNull())
			    buttonGroupAdd(ybg, elem);
		    } else if (elempeer instanceof JRadioButtonMenuItem && !ismenubar) {
			menu.add((JRadioButtonMenuItem)elempeer);
			if ((ybg = elem.getObject(N_GROUP)) != null && ybg.notNull())
			    buttonGroupAdd(ybg, elem);
		    } else if (elempeer instanceof JMenuItem && !ismenubar) {
			menu.add((JMenuItem)elempeer);
		    } else VM.abort(BADMENUITEM, i);
		} else VM.abort(BADMENUITEM, i);
	    }
	}
    }


    private void
    buildComponent() {

	YoixObject  obj;
	boolean     canshape = false;

	//
	// We currently call setRequestFocusEnabled(false) right after
	// creating peerscroller in an attempt to prevent the transfer
	// of the focus to peerscroller. Partly successful, but only
	// when the scrollbars aren't visible. Not sure what the right
	// approach is, but doing more probably means we would have to
	// create our own JScrollPane class - look into it later.
	//

	if ((peer = buildPeer()) == null) {
	    switch (getMinor()) {
		case BUTTONGROUP:
		    peer = new ButtonGroup();
		    if (bg_dummy == null)
			bg_dummy = new JButton();
		    ((ButtonGroup)peer).add(bg_dummy);
		    setField(N_SELECTED); // needed?? wanted??
		    break;

		case JBUTTON:
		    switch (data.getInt(N_TYPE, YOIX_STANDARD_BUTTON)) {
			case YOIX_CHECKBOX_BUTTON:
			    peer = new JCheckBox();
			    break;

			case YOIX_RADIO_BUTTON:
			    peer = new JRadioButton();
			    break;

			case YOIX_TOGGLE_BUTTON:
			    peer = new JToggleButton();
			    break;

			default:
			    peer = new JButton();
			    data.putInt(N_TYPE, YOIX_STANDARD_BUTTON);
			    break;
		    }
		    setToConstant(N_TYPE);
		    setField(N_TEXT);
		    setField(N_ICON);
		    if (data.getObject(N_ICONS).notNull())
			setField(N_ICONS);
		    setField(N_ALIGNMENT);
		    setField(N_TEXTPOSITION);
		    setField(N_ARMED);
		    setField(N_COMMAND);
		    setField(N_MNEMONIC);
		    setField(N_PRESSED);
		    setField(N_ROLLOVERENABLED);
		    setField(N_ROLLOVER);
		    setField(N_GROUP);
		    if (data.getObject(N_SELECTED).notNull())
			setField(N_SELECTED);
		    else setField(N_STATE);
		    setField(N_INSETS);
		    break;

		case JCANVAS:
		    peer = new YoixSwingJCanvas(data, this);
		    setField(N_GRAPHICS);
		    setField(N_STATE);
		    setField(N_BORDERCOLOR);
		    setField(N_PANANDZOOM);
		    setField(N_ORIGIN);
		    setField(N_AFTERPAN);
		    setField(N_AFTERZOOM);
		    break;

		case JCOLORCHOOSER:
		    peer = new YoixSwingJColorChooser(data, this);
		    setField(N_COLOR);
		    break;

		case JCOMBOBOX:
		    peer = new YoixSwingJComboBox(data, this);
		    setField(N_COMMAND);
		    setField(N_EDIT);
		    setField(N_ITEMS);
		    if ((obj = data.getObject(N_ITEMS)) != null && obj.isNull()) {
			setField(N_LABELS);
			setField(N_MAPPINGS);
		    }
		    setField(N_TEXT);
		    setField(N_SELECTED);
		    setField(N_SELECTEDENDS);
		    setField(N_ROWS);
		    break;

		case JDESKTOPPANE:
		    peer = new YoixSwingJDesktopPane(data, this);
		    ////setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    break;

		case JDIALOG:
		    peer = newJDialog(data.getObject(N_PARENT), false);
		    canshape = true;
		    setField(N_SCREEN);
		    setField(N_MODAL);
		    setField(N_GRAPHICS);
		    setField(N_TITLE);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_PARENT);
		    setField(N_GLASSPANE);
		    setField(N_RESIZABLE);
		    setField(N_DECORATIONSTYLE);
		    setField(N_FIRSTFOCUS);
		    setToConstant(N_MODAL);
		    break;

		case JFILECHOOSER:
		    peer = new YoixSwingJFileChooser(data, this);
		    setField(N_DIRECTORY);		// should happen first!
		    setField(N_MODE);
		    setField(N_MULTIPLEMODE);
		    setField(N_HIDDENFILES);
		    setField(N_FILESELECTIONMODE);
		    setField(N_FILTERS);
		    setField(N_FILTER);
		    setField(N_APPROVEBUTTONMNEMONIC);
		    setField(N_APPROVEBUTTONTEXT);
		    setField(N_APPROVEBUTTONTOOLTIPTEXT);
		    setField(N_FILE);
		    setField(N_BUTTONS);
		    break;

		case JFILEDIALOG:
		    peer = newJDialog(data.getObject(N_PARENT), true);
		    setField(N_SCREEN);
		    setField(N_MODAL);
		    setField(N_GRAPHICS);
		    setField(N_TITLE);
		    ////setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_PARENT);
		    setField(N_GLASSPANE);
		    setField(N_RESIZABLE);
		    setField(N_DIRECTORY);
		    setField(N_MODE);
		    setField(N_MULTIPLEMODE);
		    setField(N_HIDDENFILES);
		    setField(N_FILESELECTIONMODE);
		    setField(N_FILTERS);
		    setField(N_FILTER);
		    setField(N_APPROVEBUTTONMNEMONIC);
		    setField(N_APPROVEBUTTONTEXT);
		    setField(N_APPROVEBUTTONTOOLTIPTEXT);
		    setField(N_FILE);
		    setToConstant(N_MODAL);

		    //
		    // We ran into some deadlock problems that only happened with
		    // our JFileDialog on Apple's JVM and we think the problem was
		    // caused by Apple's AquaDirectoryModel. The following kludge
		    // seems to eliminate the deadlock, provided it happens between
		    // the setField(N_LAYOUT) and setField(N_SIZE) calls. Deadlock
		    // was still present in 1.5.0_7 at last test (1/23/2007).
		    //

		    if (ISMAC)
			syncDispatchThread(2);
		    break;

		case JFRAME:
		    peer = new YoixSwingJFrame(data, this, getGraphicsConfigurationFromScreen());
		    canshape = true;
		    setField(N_SCREEN);
		    setField(N_GRAPHICS);
		    setField(N_TITLE);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_PARENT);
		    setField(N_GLASSPANE);
		    setField(N_RESIZABLE);
		    setField(N_ICONIFIED);
		    setField(N_MAXIMIZED);
		    setField(N_DECORATIONSTYLE);
		    setField(N_FIRSTFOCUS);
		    break;

		case JINTERNALFRAME:
		    peer = new YoixSwingJInternalFrame(data, this);
		    setField(N_GRAPHICS);
		    setField(N_TITLE);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_DESKTOP);
		    setField(N_PARENT);
		    setField(N_ICON);
		    setField(N_GLASSPANE);
		    setField(N_CLOSABLE);
		    setField(N_CLOSED);
		    setField(N_ICONIFIABLE);
		    setField(N_ICONIFIED);
		    setField(N_MAXIMIZABLE);
		    setField(N_MAXIMIZED);
		    setField(N_RESIZABLE);
		    setField(N_FIRSTFOCUS);
		    break;

		case JLABEL:
		    peer = new JLabel();
		    setField(N_ALIGNMENT);
		    setField(N_ALTALIGNMENT);
		    setField(N_TEXTPOSITION);
		    setField(N_TEXT);
		    setField(N_ICON);
		    break;

		case JLAYEREDPANE:
		    peer = new YoixSwingJLayeredPane(data, this);
		    ////setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    break;

		case JLIST:
		    peer = new YoixSwingJList(new DefaultListModel());
		    if (data.getInt(N_SCROLL, 0) > 0) {
			peerscroller = new YoixSwingJScrollPane((JList)peer);
			((JComponent)peerscroller).setRequestFocusEnabled(false);
			setField(N_SCROLL);
		    } else setToConstant(N_SCROLL);
		    setField(N_SIZECONTROL);
		    setField(N_MULTIPLEMODE);
		    setField(N_ITEMS);
		    if (data.getObject(N_ITEMS).isNull()) {
			setField(N_LABELS);
			setField(N_MAPPINGS);
		    }
		    setField(N_SELECTED);
		    setField(N_INDEX);
		    setField(N_ROWS);
		    setField(N_COLUMNS);
		    break;

		case JMENU:
		    peer = new JMenu();
		    setField(N_ALIGNMENT);
		    setField(N_TEXT);
		    setField(N_ICON);
		    if (data.getObject(N_ICONS).notNull())
			setField(N_ICONS);
		    setField(N_ITEMS);
		    setField(N_ITEMARRAY);	// deprecated
		    setField(N_MNEMONIC);
		    break;

		case JMENUBAR:
		    peer = new JMenuBar();
		    setField(N_INSETS);
		    setField(N_ITEMS);
		    setField(N_ITEMARRAY);	// deprecated
		    break;

		case JMENUITEM:
		    switch (data.getInt(N_TYPE, YOIX_STANDARD_BUTTON)) {
			case YOIX_CHECKBOX_BUTTON:
			    peer = new JCheckBoxMenuItem();
			    break;

			case YOIX_RADIO_BUTTON:
			    peer = new JRadioButtonMenuItem();
			    break;

			default:
			    peer = new JMenuItem();
			    data.putInt(N_TYPE, YOIX_STANDARD_BUTTON);
			    break;
		    }
		    setToConstant(N_TYPE);
		    setField(N_TEXT);
		    setField(N_ICON);
		    if (data.getObject(N_ICONS).notNull())
			setField(N_ICONS);
		    setField(N_ALIGNMENT);
		    setField(N_TEXTPOSITION);
		    setField(N_ARMED);
		    setField(N_COMMAND);
		    setField(N_GROUP);
		    setField(N_ACCELERATOR);
		    setField(N_MNEMONIC);
		    setField(N_PRESSED);
		    setField(N_ROLLOVERENABLED);
		    setField(N_ROLLOVER);
		    if (data.getObject(N_SELECTED).notNull())
			setField(N_SELECTED);
		    else setField(N_STATE);
		    break;

		case JPANEL:
		    peer = new YoixSwingJPanel(data, this);
		    setField(N_GRAPHICS);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    break;

		case JPOPUPMENU:
		    peer = new JPopupMenu();
		    popupmenulistener = new YoixPopupMenuListener(this);
		    ((JPopupMenu)peer).addPopupMenuListener(popupmenulistener);
		    setField(N_TEXT);
		    setField(N_ITEMS);
		    setField(N_ITEMARRAY);	// deprecated
		    break;

		case JPROGRESSBAR:
		    peer = new JProgressBar();
		    ((JProgressBar)peer).setBorderPainted(true);
		    setField(N_MAXIMUM);
		    setField(N_MINIMUM);
		    setField(N_INDETERMINATE);
		    setField(N_ORIENTATION);
		    setField(N_VALUE);
		    setField(N_TEXT);
		    break;

		case JSCROLLBAR:
		    peer = new YoixSwingJScrollBar(data);
		    setField(N_MAXIMUM);
		    setField(N_MINIMUM);
		    setField(N_VISIBLEAMOUNT);
		    setField(N_BLOCKINCREMENT);
		    setField(N_UNITINCREMENT);
		    setField(N_ORIENTATION);
		    setField(N_VALUE);
		    break;

		case JSCROLLPANE:
		    peer = new YoixSwingJScrollPane();
		    setField(N_SCROLL);
		    setField(N_SIZECONTROL);
		    setField(N_LAYOUT);
		    break;

		case JSEPARATOR:
		    peer = new JSeparator();
		    setField(N_ORIENTATION);
		    break;

		case JSLIDER:
		    peer = new YoixSwingJSlider();
		    setField(N_ADJUSTING);
		    setField(N_MAXIMUM);
		    setField(N_MINIMUM);
		    setField(N_EXTENT);
		    setField(N_VALUE);
		    setField(N_ORIENTATION);
		    setField(N_INVERTED);
		    setField(N_LABELS);
		    setField(N_MAJORTICKSPACING);
		    setField(N_MINORTICKSPACING);
		    setField(N_PAINTLABELS);
		    setField(N_PAINTTICKS);
		    setField(N_PAINTTRACK);
		    setField(N_SNAPTOTICKS);
		    setField(N_UNITINCREMENT);
		    break;

		case JSPLITPANE:
		    peer = new YoixSwingJSplitPane();
		    setField(N_KEEPHIDDEN);
		    setField(N_ORIENTATION);
		    setField(N_LAYOUT);
		    setField(N_CONTINUOUSLAYOUT);
		    setField(N_RESIZEWEIGHT);
		    setField(N_DIVIDERLOCATION);
		    setField(N_DIVIDERLOCKED);
		    setField(N_DIVIDERSIZE);
		    setField(N_ONETOUCHEXPANDABLE);
		    break;

		case JTABBEDPANE:
		    peer = new YoixSwingJTabbedPane(data, this);
		    setField(N_SCROLL);
		    setField(N_SIZECONTROL);
		    setField(N_ALIGNMENT);
		    setField(N_LAYOUT);
		    setField(N_SELECTED);
		    setField(N_TRACKFOCUS);
		    break;

		case JTABLE:
		    peer = new YoixSwingJTable(data, this);
		    //
		    // always put a table in a scrollpane so that the header
		    // shows up -- if scrolling is not desired by user,
		    // then they can use NEVER as a scrolling option
		    //
		    peerscroller = new YoixSwingJScrollPane((YoixSwingJTable)peer);
		    ((JComponent)peerscroller).setRequestFocusEnabled(false);
		    setField(N_QUIET);
		    setField(N_INPUTFILTER);
		    setField(N_OUTPUTFILTER);
		    if (data.get(N_COLUMNS, false).notNull())
			setField(N_COLUMNS);
		    if (data.get(N_HEADERS, false).notNull())
			setField(N_HEADERS);
		    if (data.get(N_TYPES, false).notNull())
			setField(N_TYPES);
		    if (data.get(N_TEXT, false).notNull())
			setField(N_TEXT);
		    if (data.get(N_VALUES, false).notNull())
			setField(N_VALUES);
		    setField(N_ALTTOOLTIPTEXT);
		    setField(N_CLICKCOUNT);
		    setField(N_ALTALIGNMENT);
		    setField(N_ALTFONT);
		    setField(N_ALTBACKGROUND);
		    setField(N_ALTFOREGROUND);
		    setField(N_ALTGRIDCOLOR);
		    setField(N_SELECTIONBACKGROUND);
		    setField(N_SELECTIONFOREGROUND);
		    setField(N_CELLCOLORS);
		    setField(N_EDIT);
		    setField(N_EDITBACKGROUND);
		    setField(N_EDITFOREGROUND);
		    setField(N_GRIDCOLOR);
		    setField(N_GRIDSIZE);
		    setField(N_MULTIPLEMODE);
		    setField(N_REORDER);
		    setField(N_RESIZE);
		    setField(N_RESIZEMODE);
		    setField(N_ROWHEIGHTADJUSTMENT);
		    setField(N_ROWS);
		    setField(N_SCROLL);
		    setField(N_SIZECONTROL);
		    setField(N_USEEDITHIGHLIGHT);
		    setField(N_VALIDATOR);
		    setField(N_AFTERSELECT);
		    setField(N_ALLOWEDIT);
		    setField(N_ORIGIN);
		    break;

		case JTEXTAREA:
		    peer = new YoixSwingJTextArea(data, this);
		    if (data.getInt(N_SCROLL, 0) > 0) {
			peerscroller = new YoixSwingJScrollPane((JTextArea)peer);
			((JComponent)peerscroller).setRequestFocusEnabled(false);
			setField(N_SCROLL);
		    } else setToConstant(N_SCROLL);
		    setField(N_SIZECONTROL);
		    ((JTextComponent)peer).setHighlighter(new YoixSwingHighlighter());
		    setField(N_HIGHLIGHTFLAGS);
		    setField(N_SELECTIONBACKGROUND);
		    setField(N_SELECTIONFOREGROUND);
		    setField(N_COLUMNS);
		    setField(N_ROWS);
		    setField(N_TEXTWRAP);
		    setField(N_TEXT);
		    setField(N_CARETMODEL);
		    setField(N_CARET);
		    setField(N_SELECTEDENDS);
		    setField(N_EDIT);
		    setField(N_INSETS);
		    break;

		case JTEXTCANVAS:
		    peer = new YoixSwingJTextCanvas(data, this);
		    setField(N_TEXTMODE);
		    setField(N_TEXTWRAP);
		    setField(N_ALIGNMENT);
		    setField(N_INSETS);
		    setField(N_IPAD);
		    setField(N_STATE);
		    setField(N_SAVEGRAPHICS);
		    setField(N_BORDERCOLOR);
		    setField(N_BORDER);
		    setField(N_COLUMNS);
		    setField(N_ROWS);
		    setField(N_TEXT);
		    break;

		case JTEXTFIELD:
		    if (data.getInt(N_ECHO, 0) == 0) {
			peer = new YoixSwingJTextField(data, this);
			setToConstant(N_ECHO);
		    } else {
			peer = new JPasswordField();
			setField(N_ECHO);
		    }
		    ((JTextComponent)peer).setHighlighter(new YoixSwingHighlighter());
		    setField(N_HIGHLIGHTFLAGS);
		    setField(N_SELECTIONBACKGROUND);
		    setField(N_SELECTIONFOREGROUND);
		    setField(N_COLUMNS);
		    setField(N_TEXT);
		    setField(N_ALIGNMENT);
		    setField(N_CARETMODEL);
		    setField(N_CARET);
		    setField(N_COMMAND);
		    setField(N_SELECTEDENDS);
		    setField(N_EDIT);
		    setField(N_INSETS);
		    break;

		case JTEXTPANE:
		    //
		    // The N_TEXT field is now set below because it has
		    // to follow the font, foreground, and background if
		    // we're in HTML mode. N_PAGE field is also set there.
		    //
		    peer = new YoixSwingJTextPane(data, this);
		    if (data.getInt(N_SCROLL, 0) > 0) {
			peerscroller = new YoixSwingJScrollPane((YoixSwingJTextPane)peer);
			((JComponent)peerscroller).setRequestFocusEnabled(false);
			setField(N_SCROLL);
		    } else setToConstant(N_SCROLL);
		    setField(N_SIZECONTROL);
		    ((JTextComponent)peer).setHighlighter(new YoixSwingHighlighter());
		    setField(N_HIGHLIGHTFLAGS);
		    setField(N_SELECTIONBACKGROUND);
		    setField(N_SELECTIONFOREGROUND);
		    setField(N_MODE);
		    setField(N_CARETMODEL);
		    setField(N_CARET);
		    setField(N_SELECTEDENDS);
		    setField(N_EDIT);
		    setField(N_BASE);
		    setField(N_INSETS);
		    setField(N_ALIGNMENT);
		    if (ISMAC && data.getInt(N_MODE, 0) != 0)
			MAC_DEADLOCK_KLUDGE = true;
		    break;

		case JTEXTTERM:
		    peer = new YoixSwingJTextTerm(data, this);
		    setField(N_ALIGNMENT);
		    setField(N_INSETS);
		    setField(N_IPAD);
		    setField(N_STATE);
		    setField(N_SAVEGRAPHICS);
		    setField(N_BORDERCOLOR);
		    setField(N_BORDER);
		    setField(N_COLUMNS);
		    setField(N_ROWS);
		    setField(N_SAVELINES);
		    setField(N_PROMPT);
		    setField(N_TEXT);
		    setField(N_EDIT);
		    break;

		case JTOOLBAR:
		    peer = new JToolBar();
		    setField(N_ORIENTATION);
		    setField(N_LAYOUT);
		    setField(N_FLOATABLE);
		    setField(N_INSETS);
		    break;

		case JTREE:
		    peer = new YoixSwingJTree(data, this);
		    setField(N_MULTIPLEMODE);
		    setField(N_CLOSEDICON);
		    setField(N_LEAFICON);
		    setField(N_OPENICON);
		    setField(N_EDIT);
		    setField(N_ROOTHANDLE);
		    setField(N_TOP);
		    setField(N_BORDERCOLOR);
		    setField(N_SELECTIONBACKGROUND);
		    setField(N_SELECTIONFOREGROUND);
		    setField(N_EXPANDSSELECTEDNODES);
		    setField(N_SCROLLSONEXPAND);
		    break;

		case JWINDOW:
		    peer = newJWindow(data.getObject(N_PARENT));
		    canshape = true;
		    setField(N_SCREEN);
		    setField(N_GRAPHICS);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_PARENT);
		    setField(N_GLASSPANE);
		    setField(N_FIRSTFOCUS);
		    break;

		default:
		    VM.abort(UNIMPLEMENTED);
		    break;
	    }
	}

	setWindow(peer instanceof YoixInterfaceWindow);
	setCanShape(canshape);

	if (peer instanceof JComponent) {
	    nulltransferhandler = ((JComponent)peer).getTransferHandler();
	    nullborder = ((JComponent)peer).getBorder();
	}

	//
	// Order currently matches AWT version, which had some strict
	// requirements that may may be unnecessary in Swing.
	//

	setField(N_DOUBLEBUFFERED);
	setField(N_SYNCVIEWPORT);
	setField(N_ENABLED);
	setField(N_TAG);
	setField(N_OPAQUE);
	setField(N_FOREGROUND);
	setField(N_BACKGROUND);
	setField(N_BACKGROUNDHINTS);
	setField(N_BACKGROUNDIMAGE);
	setField(N_FONT);
	if (MAC_DEADLOCK_KLUDGE)
	    syncDispatchThread(0);
	setField(N_BORDER);		// should follow N_[BACK|FORE]GROUND and N_FONT
	setField(N_PAINT);
	setField(N_PREFERREDSIZE);
	setField(N_MINIMUMSIZE);
	setField(N_MAXIMUMSIZE);

	//
	// Handle anything that can inherit properties, like the font
	// or background, but also needs to be done before making the
	// component visible. Incidentally, we ran into a deadlock on
	// Linux when menubar was attached after visible was set.
	//

	if (isWindow())
	    setField(N_MENUBAR);
	setField(N_POPUP);

	setField(N_SIZE);
	setField(N_SHAPE);
	setField(N_LOCATION);
	setField(N_CURSOR);
	setField(N_REQUESTFOCUSENABLED);
	setField(N_COMPRESSEVENTS);
	setField(N_TOOLTIPTEXT);

	//
	// if TOOLTIPTEXT was null, but TOOLTIPS is true (as might be the
	// case for a JTable with a HISTOGRAM_TYPE column, we want to run
	// TOOLTIPS after TOOLTIPTEXT, but if TOOLTIPTEXT is not null, but
	// TOOLTIPS is false, we would not want to run it (in most cases).
	//

	if (!(peer instanceof JComponent) || ((JComponent)peer).getToolTipText() == null)
	    setField(N_TOOLTIPS);

	setField(N_DRAGENABLED);
	setField(N_TRANSFERHANDLER);

	if (peer instanceof YoixSwingJTextPane) {
	    //
	    // Has to be done after font, background, and foreground when
	    // the JTextPane is in HTML mode, otherwise it probably could
	    // be moved back.
	    //
	    setField(N_TEXT);
	    setField(N_PAGE);
	}

	if (peer instanceof JComboBox || peer instanceof JList)
	    setField(N_PROTOTYPEVALUE);

	disposable = true;

	//
	// Moved the line
	//
	//     initialized = true;
	//
	// to the end of the constructor on 3/16/2010 to address an obscure
	// deadlock issue.
	//
    }


    private synchronized YoixSwingLabelItem[]
    buildLabelItems(ListModel listmodel, YoixObject items, int type) {

	YoixSwingLabelItem  values[];
	YoixObject          yobj;
	boolean             notnull;
	int                 length;
	int                 offset;
	int                 grp;
	int                 m;
	int                 n;

	//
	// The lines labeled "BUG FIX" under the V_ITEMS and V_LABELS cases
	// should also be able to replace the removeAllElements() calls. We
	// decided to keep them for now, but they probably will be removed
	// in a future release. See bug27.yx for an example of the behavior
	// that's supposed to be fixed.
	//

	values = null;

	switch (type) {
	    case V_ITEMS:
		if (items != null && items.notNull() && (length = items.length()) > 0) {
		    offset = items.offset();
		    yobj = items.get(offset, false);
		    if (yobj.notNull() && yobj.isString())
			grp = 2;
		    else grp = 3;
		    values = new YoixSwingLabelItem[(length - offset + 1)/grp];
		    for (m = 0, n = offset; n < length; m++, n += grp) {
			values[m] = new YoixSwingLabelItem();
			yobj = items.get(n, false);
			notnull = yobj.notNull();
			if (grp == 3 && (!notnull || yobj.isImage()))
			    values[m].setIcon(YoixMake.javaIcon(yobj));
			else if (grp == 2 && notnull && yobj.isString())
			    values[m].setText(yobj.stringValue());
			else if (notnull)
			    VM.abort(BADVALUE, N_ITEMS, n);
			yobj = items.get(n + 1, false);
			notnull = yobj.notNull();
			if (grp == 3 && notnull && yobj.isString())
			    values[m].setText(yobj.stringValue());
			else if (grp == 2 && notnull && yobj.isString())
			    values[m].setMapping(yobj.stringValue());
			else if (grp != 2 || notnull)
			    VM.abort(BADVALUE, N_ITEMS, n + 1);
			if (grp == 3) {
			    yobj = items.get(n + 2, false);
			    notnull = yobj.notNull();
			    if (notnull && yobj.isString())
				values[m].setMapping(yobj.stringValue());
			    else if (notnull)
				VM.abort(BADVALUE, N_ITEMS, n + 2);
			}
		    }
		} else if (listmodel instanceof DefaultListModel)
		    ((DefaultListModel)listmodel).removeAllElements();
		else if (listmodel instanceof DefaultComboBoxModel)
		    ((DefaultComboBoxModel)listmodel).removeAllElements();
		else values = new YoixSwingLabelItem[0];	// BUG FIX
		break;

	    case V_LABELS:
		if (items != null && items.notNull() && (length = items.length()) > 0) {
		    offset = items.offset();
		    values = new YoixSwingLabelItem[length - offset];
		    for (m = 0, n = offset; n < length; m++, n++) {
			yobj = items.get(n, false);
			if (yobj.notNull() && yobj.isString())
			    values[m] = new YoixSwingLabelItem(yobj.stringValue());
			else VM.abort(BADVALUE, N_LABELS, n);
		    }
		} else if (listmodel instanceof DefaultListModel)
		    ((DefaultListModel)listmodel).removeAllElements();
		else if (listmodel instanceof DefaultComboBoxModel)
		    ((DefaultComboBoxModel)listmodel).removeAllElements();
		else values = new YoixSwingLabelItem[0];	// BUG FIX
		break;

	    case V_MAPPINGS:
		values = getLabelItems(listmodel);
		if (items != null && items.notNull() && items.length() > 0) {
		    if (values != null && values.length == items.sizeof()) {
			length = items.length();
			offset = items.offset();
			for (m = 0, n = offset; n < length; m++, n++) {
			    yobj = items.get(n, false);
			    notnull = yobj.notNull();
			    if (notnull && yobj.isString())
				values[m].setMapping(yobj.stringValue());
			    else if (notnull)
				VM.abort(BADVALUE, N_MAPPINGS, n);
			}
		    } else if (values == null)
			VM.abort(BADVALUE, new String[] {"null labels"});
		    else VM.abort(BADVALUE, new String[] {"length mismatch"});
		} else if (values != null && (length = values.length) > 0) {
		    for (m = 0; m < length; m++)
			values[m].setMapping(null);
		}
		break;
	}

	return(values);
    }


    private synchronized YoixObject
    builtinAction(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (comp instanceof YoixSwingJTable)
	    obj = ((YoixSwingJTable)comp).builtinAction(name, arg);
	else if (comp instanceof YoixSwingJTree)
	    obj = ((YoixSwingJTree)comp).builtinAction(name, arg);
	return(obj);
    }


    private synchronized YoixObject
    builtinClick(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	double      seconds;

	if (comp instanceof AbstractButton) {
	    if (arg.length == 0 || arg.length == 1) {
		if (arg.length < 1 || arg[0].isNumber()) {
		    seconds = (arg.length > 0) ? arg[0].doubleValue() : -1;
		    if (seconds > 0)
			((AbstractButton)comp).doClick((int)(1000.0*seconds));
		    else if (seconds < 0)
			((AbstractButton)comp).doClick();
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	    obj = YoixObject.newEmpty();
	}
	return(obj);
    }


    private synchronized YoixObject
    builtinFindNextMatch(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	int         pattern;
	boolean     ignorecase;
	boolean     bycols;
	boolean     forward;
	boolean     visible;
	String      key;

	if (comp instanceof YoixSwingJTable) {
	    if (arg.length >= 1 && arg.length <= 6) {
		if (arg[0].isString()) {
		    if (arg.length < 2 || arg[1].isInteger()) {
			if (arg.length < 3 || arg[2].isInteger()) {
			    if (arg.length < 4 || arg[3].isInteger()) {
				if (arg.length < 5 || arg[4].isInteger()) {
				    if (arg.length < 6 || arg[5].isInteger()) {
					pattern = (arg.length > 1) ? arg[1].intValue() : 0;
					ignorecase = (arg.length > 2) ? arg[2].booleanValue() : true;
					bycols = (arg.length > 3) ? arg[3].booleanValue() : false;
					forward = (arg.length > 4) ? arg[4].booleanValue() : true;
					visible = (arg.length > 5) ? arg[5].booleanValue() : true;
					key = ((YoixSwingJTable)comp).findNextMatch(
					    arg[0].stringValue(),
					    pattern,
					    ignorecase,
					    bycols,
					    forward,
					    visible
					);
					obj = YoixObject.newString(key);
				    } else VM.badArgument(name, 5);
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else if (arg.length == 0)
		((YoixSwingJTable)comp).findClearSelection();
	    else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinItem(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (comp instanceof YoixSwingJTree)
	    obj = ((YoixSwingJTree)comp).builtinItem(name, arg);
	return(obj);
    }


    private synchronized YoixObject
    builtinModelToView(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	Rectangle   rect = null;
	String      mode;
	Point       point;
	int         offset;

	if (comp instanceof JTextComponent) {
	    if (arg.length == 1) {
		if (arg[0].isNumber()) {
		    offset = Math.max(arg[0].intValue(), 0);
		    try {
			rect = ((JTextComponent)comp).modelToView(offset);
		    }
		    catch(BadLocationException e) {}
		    point = (rect != null) ? rect.getLocation() : new Point(0, 0);
		    obj = YoixMakeScreen.yoixPoint(point);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	} else if (comp instanceof YoixSwingJTable) {
	    if (arg.length == 2) {
		if (arg[0].isNumber()) {
		    offset = Math.max(arg[0].intValue(), 0);
		    if (arg[1].notNull() && arg[1].isString()) {
			mode = arg[1].stringValue();
			if (mode.length() > 0) {
			    mode = mode.substring(0,1).toLowerCase();
			    if (mode.equals("r")) {
				offset = Math.min(offset, ((YoixSwingJTable)comp).getRowCount() - 1);
				obj = YoixObject.newInt(((YoixSwingJTable)comp).yoixConvertRowIndexToView(offset));
			    } else if (mode.equals("c")) {
				offset = Math.min(offset, ((YoixSwingJTable)comp).getColumnCount() - 1);
				obj = YoixObject.newInt(((YoixSwingJTable)comp).convertColumnIndexToView(offset));
			    } else VM.abort(BADVALUE, name, 1);
			} else VM.abort(BADVALUE, name, 1);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinSetIncrement(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	JScrollBar  jsb;
	int         orientation;
	int         incrtype;

	if (comp instanceof JScrollPane) {
	    if (arg.length == 3) {
		if (arg[0].isNumber()) {
		    orientation = arg[0].intValue();
		    if (orientation == YOIX_HORIZONTAL || orientation == YOIX_VERTICAL) {
			if (arg[1].isNumber()) {
			    incrtype = arg[1].intValue();
			    if (incrtype == YOIX_UNITINCREMENT || incrtype == YOIX_UNIT_INCREMENT || incrtype == YOIX_BLOCKINCREMENT || incrtype == YOIX_BLOCK_INCREMENT) {
				if (arg[2].isNumber()) {
				    jsb = orientation == YOIX_HORIZONTAL ? ((JScrollPane)comp).getHorizontalScrollBar() : ((JScrollPane)comp).getVerticalScrollBar();
				    if (incrtype == YOIX_UNITINCREMENT || incrtype == YOIX_UNIT_INCREMENT) {
					jsb.setUnitIncrement(
					    Math.max(YoixMakeScreen.javaDistance(arg[2].doubleValue()), 1)
					);
				    } else {
					jsb.setBlockIncrement(
					    Math.max(YoixMakeScreen.javaDistance(arg[2].doubleValue()), 1)
					);
				    }
				    obj = YoixObject.newEmpty();
				} else VM.badArgument(name, 2);
			    } else VM.badArgumentValue(name, 1);
			} else VM.badArgument(name, 1);
		    } else VM.badArgumentValue(name, 0);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinSetValues(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (comp instanceof JScrollBar) {
	    if (arg.length == 4) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				((JScrollBar)comp).setValues(
				    arg[0].intValue(),		// value
				    arg[1].intValue(),		// extent
				    arg[2].intValue(),		// min
				    arg[3].intValue()		// max
				);
				obj = YoixObject.newEmpty();
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	} else if (comp instanceof YoixSwingJSlider) {
	    if (arg.length >= 4 && arg.length <= 5) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				if (arg.length == 4) {
				    ((DefaultBoundedRangeModel)(((YoixSwingJSlider)comp).getModel())).setRangeProperties(
					arg[0].intValue(),	// value
					arg[1].intValue(),	// extent
					arg[2].intValue(),	// min
					arg[3].intValue(),	// max
					false			// adjusting
				    );
				    obj = YoixObject.newEmpty();
				} else if (arg[4].isNumber()) {
				    ((DefaultBoundedRangeModel)(((YoixSwingJSlider)comp).getModel())).setRangeProperties(
					arg[0].intValue(),	// value
					arg[1].intValue(),	// extent
					arg[2].intValue(),	// min
					arg[3].intValue(),	// max
					arg[4].booleanValue()	// adjusting
				    );
				    obj = YoixObject.newEmpty();
				} else VM.badArgument(name, 4);
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinSubtext(Object comp, String name, YoixObject arg[]) {

	Document  doc = null;
	String    subtext = null;
	int       offset = 0;
	int       length = Integer.MAX_VALUE;
	int       len;

	if (arg.length >= 0 && arg.length <= 2) {
	    if (arg.length > 0) {
		if (arg[0].isInteger())
		    offset = arg[0].intValue();
		else VM.badArgument(name, 0);
		if (arg.length > 1) {
		    if (arg[1].isInteger())
			length = arg[1].intValue();
		    else VM.badArgument(name, 1);
		}
	    }
	    if (comp instanceof JTextComponent) {
		doc = ((JTextComponent)comp).getDocument();
		len = doc.getLength();
		offset = Math.max(Math.min(offset, len), 0);
		length = Math.max(Math.min(length, len-offset), 0);
		try {
		    subtext = doc.getText(offset, length);
		}
		catch(BadLocationException e) {
		    VM.abort(e);		// should never happen
		}
	    } else if (comp instanceof YoixSwingJTextComponent) {
		subtext = ((YoixSwingJTextComponent)comp).getText();
		if (subtext != null && (len = subtext.length()) > 0) {
		    offset = Math.max(Math.min(offset, len), 0);
		    length = Math.max(Math.min(length, len-offset), 0);
		    subtext = subtext.substring(offset, offset+length);
		}
	    }

	    if (subtext != null && data.getBoolean(N_AUTOTRIM))
		subtext = subtext.trim();
	} else VM.badCall(name);

	return(subtext == null ? YoixObject.newString() : YoixObject.newString(subtext));
    }


    private synchronized YoixObject
    builtinViewToModel(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	String      mode;
	Point       point = null;
	int         offset;

	if (comp instanceof JTextComponent) {
	    if (arg.length == 1 || arg.length == 2) {
		if (arg.length == 1) {
		    if (arg[0].isPoint())
			point = YoixMakeScreen.javaPoint(arg[0]);
		    else VM.badArgument(name, 0);
		} else {
		    if (arg[0].isNumber()) {
			if (arg[1].isNumber())
			    point = new Point(arg[0].intValue(), arg[1].intValue());
			else VM.badArgument(name, 1);
		    } else VM.badArgument(name, 0);
		}
		obj = YoixObject.newInt(((JTextComponent)comp).viewToModel(point));
	    } else VM.badCall(name);
	} else if (comp instanceof YoixSwingJTable) {
	    if (arg.length == 2) {
		if (arg[0].isNumber()) {
		    offset = Math.max(arg[0].intValue(), 0);
		    if (arg[1].notNull() && arg[1].isString()) {
			mode = arg[1].stringValue();
			if (mode.length() > 0) {
			    mode = mode.substring(0,1).toLowerCase();
			    if (mode.equals("r")) {
				offset = Math.min(offset, ((YoixSwingJTable)comp).getRowCount() - 1);
				obj = YoixObject.newInt(((YoixSwingJTable)comp).yoixConvertRowIndexToModel(offset));
			    } else if (mode.equals("c")) {
				offset = Math.min(offset, ((YoixSwingJTable)comp).getColumnCount() - 1);
				obj = YoixObject.newInt(((YoixSwingJTable)comp).convertColumnIndexToModel(offset));
			    } else VM.abort(BADVALUE, name, 1);
			} else VM.abort(BADVALUE, name, 1);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinZoom(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;
	YoixObject  lock;
	double      scaling;

	if (comp instanceof YoixSwingJCanvas) {
	    if (arg.length == 1 || arg.length == 2) {
		if (arg[0].isNumber()) {
		    if ((scaling = arg[0].doubleValue()) != 0) {
			if (arg.length == 1 || arg[1].isPoint() || arg[1].isNull()) {
			    lock = (arg.length > 1) ? arg[1] : null;
			    ((YoixSwingJCanvas)comp).zoom(scaling, lock);
			    obj = YoixObject.newEmpty();
			} else VM.badArgument(name, 1);
		    } else VM.badArgumentValue(name, 0);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private YoixObject
    buttonGroupAdd(YoixObject ybg, YoixObject yab) {

	YoixObject  items;
	YoixObject  element;
	boolean     exists;
	Object      manobj;
	int         len;
	int         n;

	if (yab.notNull()) {
	    if (ybg == null || ybg.isNull())
		ybg = YoixObject.newJComponent(VM.getTypeTemplate(T_BUTTONGROUP));
	    synchronized(ybg.getManagedObject()) {
		items = ybg.getObject(N_ITEMS);
		if (items.isNull()) {
		    items = YoixObject.newArray(0);
		    VM.pushAccess(RW_);
		    ybg.put(N_ITEMS, items, false);
		    VM.popAccess();
		}

		manobj = yab.getManagedObject();
		exists = false;
		len = items.length();
		for (n = 0; n < len; n++) {
		    if ((element = items.getObject(n)) != null) {
			if (manobj == element.getManagedObject()) {
			    exists = true;
			    break;
			}
		    }
		}

		if (!exists) {
		    items.setGrowable(true);
		    VM.pushAccess(RW_);
		    items.put(items.length(), yab, false);
		    VM.popAccess();
		    items.setGrowable(false);
		    ((ButtonGroup)(ybg.getManagedObject())).add((AbstractButton)(yab.getManagedObject()));
		}
	    }
	}

	return(ybg);
    }


    private void
    buttonGroupRemove(YoixObject ybg, YoixObject yab) {

	YoixObject  items;
	YoixObject  element;
	YoixObject  new_items;
	Object      manobj;
	int         index;
	int         len;
	int         n;

	//
	// Added the VM.pushAccess(RW_) right before writing items, which
	// duplicates what's done by addGroupRemove() and is needed if we
	// want to prevent an invalidaccess error. Changed on 5/31/07.
	//

	if (ybg.notNull()) {
	    manobj = (yab != null) ? yab.getManagedObject() : null;
	    synchronized(ybg.getManagedObject()) {
		items = ybg.getObject(N_ITEMS);
		if (items.notNull()) {
		    index = -1;
		    len = items.length();
		    for (n = 0; n < len; n++) {
			if ((element = items.getObject(n)) != null) {
			    if (manobj == element.getManagedObject()) {
				index = n;
				break;
			    }
			}
		    }

		    if (index >= 0) {
			new_items = YoixObject.newArray(len - 1);
			new_items.setGrowable(true);
			for (n = 0; n < len; n++) {
			    if (n != index) {
				if ((element = items.getObject(n)) != null)
				    new_items.putObject(new_items.length(), element);
			    }
			}
			new_items.setAccessBody(LR__);
			new_items.setGrowable(false);
			VM.pushAccess(RW_);
			ybg.put(N_ITEMS, new_items, false);
			VM.popAccess();
			((ButtonGroup)(ybg.getManagedObject())).remove((AbstractButton)(yab.getManagedObject()));
		    }
		}
	    }
	}
    }


    private void
    disposeAllMenus() {

	YoixObject  obj;
	Object      menu;
	int         flags;

	flags = data.getInt(N_MENUFLAGS, VM.getInt(N_MENUFLAGS));
	if (data.defined(N_MENUBAR)) {
	    if ((flags & 0x02) == 0x02) {
		if ((obj = data.getObject(N_MENUBAR)) != null) {
		    menu = obj.getManagedObject();
		    if (menu instanceof JMenuBar)
			disposeJMenuBar((JMenuBar)menu);
		}
	    }
	    if ((flags & 0x04) == 0x04)
		data.putObject(N_MENUBAR, null);
	}

	if (data.defined(N_POPUP)) {
	    if ((flags & 0x02) == 0x02) {
		if ((obj = data.getObject(N_POPUP)) != null) {
		    menu = obj.getManagedObject();
		    if (menu instanceof JPopupMenu)
			disposeJPopupMenu((JPopupMenu)menu);
		}
	    }
	    if ((flags & 0x04) == 0x04)
		data.putObject(N_POPUP, null);
	}
    }


    private void
    disposeJMenu(JMenu menu) {

	JMenuItem  item;
	JPopupMenu popup;
	int        count;
	int        n;

	count = menu.getItemCount();
	for (n = 0; n < count; n++) {
	    if ((item = menu.getItem(n)) != null) {
		if (item instanceof JMenu)
		    disposeJMenu((JMenu)item);
	    }
	}
	if ((popup = menu.getPopupMenu()) != null) {
	    popup.setInvoker(menuframe);
	    MenuSelectionManager.defaultManager().setSelectedPath(new MenuElement[] {popup});
	    MenuSelectionManager.defaultManager().clearSelectedPath();
	    popup.setInvoker(null);
	}
    }


    private void
    disposeJMenuBar(JMenuBar menubar) {

	JMenu  menu;
	int    count;
	int    n;

	count = menubar.getMenuCount();
	for (n = 0; n < count; n++) {
	    if ((menu = menubar.getMenu(n)) != null)
		disposeJMenu(menu);
	}
    }


    private void
    disposeJPopupMenu(JPopupMenu popup) {

	Component  comp;
	int        count;
	int        n;

	count = popup.getComponentCount();
	for (n = 0; n < count; n++) {
	    if ((comp = popup.getComponent(n)) != null) {
		if (comp instanceof JMenu)
		    disposeJMenu((JMenu)comp);
	    }
	}

	popup.setInvoker(menuframe);
	MenuSelectionManager.defaultManager().setSelectedPath(new MenuElement[] {popup});
	MenuSelectionManager.defaultManager().clearSelectedPath();
	popup.setInvoker(null);
    }


    private void
    doMenuLayout() {

	doMenuLayout(data.getObject(N_ITEMS));
    }


    private synchronized void
    doMenuLayout(YoixObject obj) {

	YoixObject  added;
	YoixError   error_point = null;
	Object      comp;

	comp = this.peer;		// snapshot - just to be safe

	if (obj != null) {
	    removeAll();
	    if (obj.notNull()) {
		try {
		    error_point = VM.pushError();
		    VM.pushAccess(LRW_);
		    added = YoixObject.newDictionary(0, -1, false);
		    addToMenu((Container)comp, getContext(), obj, added);
		    added.setGrowable(false);
		    added.setAccessBody(LR__);
		    data.put(N_COMPONENTS, added);
		    VM.popAccess();
		    VM.popError();
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
	    // Think these were needed to update menus properly??
	    //
	    ((JComponent)comp).validate();

	    //
	    // The updateRoot() call is part of the KLUDGE mentioned in
	    // comments in YoixBodyComponent.updateRoot() that was done
	    // on 2/17/08. The ROOT field in child menus wasn't being
	    // updated when a new value was stored in items and that
	    // led to a inconsistent behavior!!! Need to take a close
	    // look at this before the next release!!!!!!!!
	    //

	    updateRoot(getContext(), getContext().getObject(N_ROOT));

	    setField(N_BACKGROUND);
	    setField(N_CURSOR);
	    setField(N_FONT);
	    setField(N_FOREGROUND);
	}
    }


    private YoixObject
    getAccelerator(Object comp, YoixObject obj) {

	KeyStroke  keystroke;

	if (comp instanceof JMenuItem) {
	    if ((keystroke = ((JMenuItem)comp).getAccelerator()) != null)
		obj = YoixObject.newString(keystroke.toString());
	    else obj = YoixObject.newString();
	}
	return(obj);
    }


    private YoixObject
    getAdjusting(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    obj = YoixObject.newInt(((DefaultBoundedRangeModel)(((YoixSwingJSlider)comp).getModel())).getValueIsAdjusting());
	return(obj);
    }


    private YoixObject
    getAlignment(Object comp, YoixObject obj) {

	int  alignment;

	if (comp instanceof YoixSwingJTextPane) {
	    switch(((YoixSwingJTextPane)comp).getHorizontalAlignment()) {
		case SwingConstants.LEFT:
		case SwingConstants.LEADING:
		default:
		    alignment = YOIX_LEFT;
		    break;

		case SwingConstants.CENTER:
		    alignment = YOIX_CENTER;
		    break;

		case SwingConstants.RIGHT:
		case SwingConstants.TRAILING:
		    alignment = YOIX_RIGHT;
		    break;
	    }
	    obj = YoixObject.newInt(alignment);
	}
	return(obj);
    }


    private YoixObject
    getApproveButtonMnemonic(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceFileChooser)
	    obj = YoixObject.newInt(((YoixInterfaceFileChooser)comp).getApproveButtonMnemonic());
	return(obj);
    }


    private YoixObject
    getApproveButtonText(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceFileChooser)
	    obj = YoixObject.newString(((YoixInterfaceFileChooser)comp).getApproveButtonText());
	return(obj);
    }


    private YoixObject
    getApproveButtonToolTipText(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceFileChooser)
	    obj = YoixObject.newString(((YoixInterfaceFileChooser)comp).getApproveButtonToolTipText());
	return(obj);
    }


    private YoixObject
    getArmed(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    obj = YoixObject.newInt(((AbstractButton)comp).getModel().isArmed());
	return(obj);
    }


    private YoixObject
    getBase(Object comp, YoixObject obj) {

	Document  doc;
	URL       base;

	if (comp instanceof JTextComponent) {
	    doc = ((JTextComponent)comp).getDocument();
	    if (doc instanceof HTMLDocument) {
		if ((base = ((HTMLDocument)doc).getBase()) != null)
		    obj = YoixObject.newString(base.toString());
	    }
	}
	return(obj);
    }


    private YoixObject
    getBlockIncrement(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    obj = YoixObject.newInt(((JScrollBar)comp).getBlockIncrement());
	return(obj);
    }


    private YoixObject
    getBorder(Object comp, YoixObject obj) {

	//
	// This was added quickly for an existing application and primiarly
	// is trying to reproduce YoixAWTCanvas behavior. Probably needs a
	// closer look - later.
	// 

	if (comp instanceof YoixSwingJCanvas) {
	    if (obj == null || obj.isBorder() == false)
		obj = YoixMakeScreen.yoixInsets(((YoixSwingJCanvas)comp).getBorderInsets());
	}
	return(obj);
    }


    private YoixObject
    getBorderColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJCanvas)
	    obj = YoixMake.yoixColor(((YoixSwingJCanvas)comp).getBorderColor());
	return(obj);
    }


    private YoixObject
    getCaret(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	int          pos = -1;

	//
	// Decided to always ask the JTextComponent for the position
	// if it's an editable component, but it's a precaution that
	// probably could be handled differently - later.
	//

	if (comp instanceof JTextComponent) {
	    if (((JTextComponent)comp).isEditable() == false) {
		highlighter = ((JTextComponent)comp).getHighlighter();
		if (highlighter instanceof YoixSwingHighlighter)
		    pos = ((YoixSwingHighlighter)highlighter).getCaretPosition();
		else pos = ((JTextComponent)comp).getCaretPosition();
	    } else pos = ((JTextComponent)comp).getCaretPosition();
	    obj = YoixObject.newInt(pos);
	}
	return(obj);
    }


    private YoixObject
    getCaretColor(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	Color        color;

	if (comp instanceof JTextComponent) {
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (highlighter instanceof YoixSwingHighlighter)
		color = ((YoixSwingHighlighter)highlighter).getCaretColor();
	    else color = ((JTextComponent)comp).getCaretColor();
	    obj = YoixMake.yoixColor(color);
	}
	return(obj);
    }


    private YoixObject
    getCaretOwnerColor(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	Color        color;

	if (comp instanceof JTextComponent) {
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (highlighter instanceof YoixSwingHighlighter)
		color = ((YoixSwingHighlighter)highlighter).getCaretOwnerColor();
	    else color = null;
	    obj = YoixMake.yoixColor(color);
	}
	return(obj);
    }


    private YoixObject
    getCellSize(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixMakeScreen.yoixDimension(((YoixSwingJTextComponent)comp).getCellSize());
	return(obj);
    }


    private YoixObject
    getClosable(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    obj = YoixObject.newInt(((JInternalFrame)comp).isClosable());
	return(obj);
    }


    private YoixObject
    getClosed(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    obj = YoixObject.newInt(((JInternalFrame)comp).isClosed());
	return(obj);
    }


    private YoixObject
    getColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJColorChooser)
	    obj = YoixMake.yoixColor(((YoixSwingJColorChooser)comp).getColor());
	return(obj);
    }


    private YoixObject
    getColumns(Object comp, YoixObject obj) {

	if (comp instanceof JTextField)
	    obj = YoixObject.newInt(((JTextField)comp).getColumns());
	else if (comp instanceof YoixSwingJTable)
	    obj = ((YoixSwingJTable)comp).getColumns();
	else if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixObject.newInt(((YoixSwingJTextComponent)comp).getColumns());
	else if (comp instanceof YoixSwingJList)
	    obj = YoixObject.newInt(((YoixSwingJList)comp).getVisibleColumnCount());
	return(obj);
    }


    private YoixObject
    getDirectory(Object comp, YoixObject obj) {

	String  path;
	File    dir;

	if (comp instanceof YoixInterfaceFileChooser) {
	    if ((dir = ((YoixInterfaceFileChooser)comp).getCurrentDirectory()) != null) {
		path = dir.getPath();
		if (data.getInt(N_MODEL, 1) == 0) {	// attempt at AWT compatibility
		    if (path.endsWith(dir.separator) == false)
			path += dir.separator;
		}
		if (data.getBoolean(N_TOYOIXPATH))
		    path = YoixMisc.toYoixPath(path);
	    } else path = null;
	    obj = YoixObject.newString(path);
	}
	return(obj);
    }


    private YoixObject
    getDispose(Object comp, YoixObject obj) {

	if (isWindow())
	    obj = YoixObject.newInt(comp == null);
	return(obj);
    }


    private YoixObject
    getDividerLocation(Object comp, YoixObject obj) {

	Insets  insets;
	double  location;
	double  value;
	double  size;
	double  denom;

	//
	// NOTE - this was changed on 5/8/08 to return a value between 0.0
	// and 1.0 inclusive, which should be interpreted as a fraction of
	// the space occupied by the top/left component. Was done because
	// its compatible with setDividerLocation() and provides an easier
	// way for either component to determine if its showing. We also
	// added a getDividerLocation() method to YoixSwingJSplitPane that
	// gets the answer from the UI when true is passed as an argument.
	//

	if (comp instanceof JSplitPane) {
	    insets = ((JSplitPane)comp).getInsets();
	    if (comp instanceof YoixSwingJSplitPane)		// should always be true
		location = ((YoixSwingJSplitPane)comp).getDividerLocation(true);
	    else location = ((JSplitPane)comp).getDividerLocation();
	    size = ((JSplitPane)comp).getDividerSize();
	    if (((JSplitPane)comp).getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
		if ((denom = ((JSplitPane)comp).getWidth() - (size + insets.left + insets.right)) > 0)
		    value = (location - insets.left)/denom;
		else value = 1.0;
	    } else {
		if ((denom = ((JSplitPane)comp).getHeight() - (size + insets.left + insets.right)) > 0)
		    value = (location - insets.top)/denom;
		else value = 1.0;
	    }
	    //
	    // Explicitly restricting the answer to the appropriate range
	    // is a precaution that will rarely (if ever) be needed.
	    //
	    obj = YoixObject.newDouble(Math.max(0.0, Math.min(value, 1.0)));
	}
	return(obj);
    }


    private YoixObject
    getDividerLocked(Object comp, YoixObject obj) {

	if (comp instanceof JSplitPane) {
	    if (comp instanceof YoixSwingJSplitPane)
		obj = YoixObject.newInt(((YoixSwingJSplitPane)comp).getDividerLocked());
	    else obj = YoixObject.newInt(false);
	}
	return(obj);
    }


    private YoixObject
    getDividerSize(Object comp, YoixObject obj) {

	double  value;
	double  loc[];

	if (comp instanceof JSplitPane) {
	    value = ((JSplitPane)comp).getDividerSize();
	    if ((loc = VM.getDefaultMatrix().idtransform(value, value, null)) == null)
		loc = new double[] {-1, -1};
	    if (((JSplitPane)comp).getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
		value = loc[0];
	    else value = loc[1];
	    obj = YoixObject.newDouble(value);
	}

	return(obj);
    }


    private YoixObject
    getDoubleBuffered(Object comp, YoixObject obj) {

	if (comp instanceof JComponent)
	    obj = YoixObject.newInt(((JComponent)comp).isDoubleBuffered());
	else if (comp instanceof YoixSwingJDialog)
	    obj = YoixObject.newInt(((YoixSwingJDialog)comp).isDoubleBuffered());
	else if (comp instanceof YoixSwingJFrame)
	    obj = YoixObject.newInt(((YoixSwingJFrame)comp).isDoubleBuffered());
	else if (comp instanceof YoixSwingJWindow)
	    obj = YoixObject.newInt(((YoixSwingJWindow)comp).isDoubleBuffered());
	return(obj);
    }


    private YoixObject
    getDragEnabled(Object comp, YoixObject obj) {

	Object  enabled;

	if (comp instanceof Component) {
	    if (comp instanceof JComponent) {
		enabled = YoixReflect.invoke(comp, "getDragEnabled");
		if (enabled instanceof Boolean)
		    obj = YoixObject.newInt((Boolean)enabled);
		else obj = YoixObject.newInt(false);
	    } else obj = YoixObject.newInt(false);
	}
	return(obj);
    }


    private YoixObject
    getEcho(Object comp, YoixObject obj) {

	if (comp instanceof JPasswordField)
	    obj = YoixObject.newInt(((JPasswordField)comp).getEchoChar());
	return(obj);
    }


    private YoixObject
    getEdit(Object comp, YoixObject obj) {

	if (comp instanceof JTextComponent)
	    obj = YoixObject.newInt(((JTextComponent)comp).isEditable());
	else if (comp instanceof YoixSwingJTextTerm)
	    obj = YoixObject.newInt(((YoixSwingJTextTerm)comp).getEditable());
	return(obj);
    }


    private YoixObject
    getExtent(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = YoixMakeScreen.yoixDimension(getViewSize());
	else if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixMakeScreen.yoixDimension(((YoixSwingJTextComponent)comp).getExtent());
	else if (comp instanceof YoixSwingJScrollPane)
	    obj = YoixMakeScreen.yoixDimension(getViewSize());
	else if (comp instanceof YoixSwingJSlider)
	    obj = YoixObject.newInt(((YoixSwingJSlider)comp).getExtent());  // extent is an int in this case
	return(obj);
    }


    private YoixObject
    getFile(Object comp, YoixObject obj) {

	boolean  toyoix;
	String   path;
	File     files[];
	File     file;
	int      n;

	if (comp instanceof YoixInterfaceFileChooser) {
	    toyoix = data.getBoolean(N_TOYOIXPATH);
	    if (((YoixInterfaceFileChooser)comp).isMultiSelectionEnabled()) {
		if ((files = ((YoixInterfaceFileChooser)comp).getSelectedFiles()) != null) {
		    obj = YoixObject.newArray(files.length);
		    for (n = 0; n < files.length; n++) {
			path = files[n].getPath();
			if (toyoix)
			    path = YoixMisc.toYoixPath(path);
			obj.putString(n, path);
		    }
		} else obj = YoixObject.newArray();
	    } else {
		if ((file = ((YoixInterfaceFileChooser)comp).getSelectedFile()) != null) {
		    if (data.getInt(N_MODEL, 1) == 0)	// attempt at AWT compatilbily
			path = file.getName();
		    else path = file.getPath();
		    if (toyoix)
			path = YoixMisc.toYoixPath(path);
		} else path = null;
		obj = YoixObject.newString(path);
	    }
	}
	return(obj);
    }


    private YoixObject
    getFileSelectionMode(Object comp, YoixObject obj) {

	int  mode;

	if (comp instanceof YoixInterfaceFileChooser) {
	    mode = 0;
	    if (((YoixInterfaceFileChooser)comp).isFileSelectionEnabled())
		mode |= 0x01;
	    if (((YoixInterfaceFileChooser)comp).isDirectorySelectionEnabled())
		mode |= 0x02;
	    obj = YoixObject.newInt(mode);
	}
	return(obj);
    }


    private YoixObject
    getFilter(Object comp, YoixObject obj) {

	FileFilter  filter;
	String      description = null;

	if (comp instanceof YoixInterfaceFileChooser) {
	    if ((filter = ((YoixInterfaceFileChooser)comp).getFileFilter()) != null)
		description = filter.getDescription();
	}
	return(YoixObject.newString(description));
    }


    private YoixObject
    getFrames(Object comp, YoixObject obj) {

	JInternalFrame  frames[];
	YoixObject      frame;
	String          tag;
	int             length;
	int             n;

	if (comp instanceof JDesktopPane) {
	    if ((frames = ((JDesktopPane)comp).getAllFrames()) != null) {
		length = frames.length;
		obj = YoixObject.newDictionary(length);
		for (n = 0; n < length; n++) {
		    if (frames[n] instanceof YoixSwingJInternalFrame) {
			if ((frame = ((YoixSwingJInternalFrame)frames[n]).getContext()) != null) {
			    if ((tag = frame.getString(N_TAG)) != null)
				obj.put(tag, frame, true);
			}
		    }
		}
	    } else obj = YoixObject.newDictionary();
	}
	return(obj);
    }


    private YoixObject
    getGridSize(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = YoixMakeScreen.yoixDimension(((YoixSwingJTable)comp).getIntercellSpacing());

	return(obj);
    }


    private YoixObject
    getHeaders(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = ((YoixSwingJTable)comp).getHeaders();
	return(obj);
    }


    private YoixObject
    getHeaderIcons(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = ((YoixSwingJTable)comp).getHeaderIcons();
	return(obj);
    }


    private YoixObject
    getHiddenFiles(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceFileChooser)
	    obj = YoixObject.newInt(!((YoixInterfaceFileChooser)comp).isFileHidingEnabled());
	return(obj);
    }


    private YoixObject
    getIconifiable(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    obj = YoixObject.newInt(((JInternalFrame)comp).isIconifiable());
	return(obj);
    }


    private YoixObject
    getIconified(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    obj = YoixObject.newInt(((JInternalFrame)comp).isIcon());
	else if (comp instanceof JFrame)
	    obj = YoixObject.newInt(((JFrame)comp).getExtendedState() & JFrame.ICONIFIED);
	return(obj);
    }


    private YoixObject
    getIcons(Object comp, YoixObject obj) {

	AbstractButton  ab;
	YoixObject      yobj;
	String          desc;
	String          type;
	Image           img;

	if (comp instanceof AbstractButton) {
	    ab = (AbstractButton)comp;
	    obj = YoixObject.newDictionary(7);
	    obj.put(N_DEFAULTICON, YoixMake.yoixIcon(ab.getIcon()), false);
	    obj.put(N_DISABLEDICON, YoixMake.yoixIcon(ab.getDisabledIcon()), false);
	    obj.put(N_DISABLEDSELECTEDICON, YoixMake.yoixIcon(ab.getDisabledSelectedIcon()), false);
	    obj.put(N_PRESSEDICON, YoixMake.yoixIcon(ab.getPressedIcon()), false);
	    obj.put(N_ROLLOVERICON, YoixMake.yoixIcon(ab.getRolloverIcon()), false);
	    obj.put(N_ROLLOVERSELECTEDICON, YoixMake.yoixIcon(ab.getRolloverSelectedIcon()), false);
	    obj.put(N_SELECTEDICON, YoixMake.yoixIcon(ab.getSelectedIcon()), false);
	}
	return(obj);
    }


    private YoixObject
    getIndeterminate(Object comp, YoixObject obj) {

	if (comp instanceof JProgressBar)
	    obj = YoixObject.newInt(((JProgressBar)comp).isIndeterminate());
	return(obj);
    }


    private YoixObject
    getInputFilter(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = ((YoixSwingJTable)comp).getInputFilter();
	return(obj);
    }


    private YoixObject
    getInsets(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixMakeScreen.yoixInsets(((YoixSwingJTextComponent)comp).getInsets());
	else if (comp instanceof JButton)
	    obj = YoixMakeScreen.yoixInsets(((JButton)comp).getMargin());
	return(obj);
    }


    private YoixObject
    getIpad(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixMakeScreen.yoixInsets(((YoixSwingJTextComponent)comp).getIpad());
	return(obj);
    }


    private synchronized YoixObject
    getItems(Object comp, YoixObject obj) {

	YoixSwingLabelItem  labelitems[];
	int                 length;
	int                 grp = 2;
	int                 m;
	int                 n;

	if (comp instanceof JList || comp instanceof YoixSwingJComboBox) {
	    if (comp instanceof JList)
		labelitems = getLabelItems(((JList)comp).getModel());
	    else labelitems = getLabelItems(((YoixSwingJComboBox)comp).getModel());
	    if (labelitems != null) {
		length = labelitems.length;
		for (n = 0; n < length; n++) {
		    if (labelitems[n].getIcon() != null) {
			grp = 3;
			break;
		    }
		}
		obj = YoixObject.newArray(grp*length);
		for (m = 0, n = 0; n < length; n++) {
		    if (grp == 3)
			obj.put(m++, YoixMake.yoixIcon(labelitems[n].getIcon()), false);
		    obj.put(m++, YoixObject.newString(labelitems[n].getText()), false);
		    obj.put(m++, YoixObject.newString(labelitems[n].getMapping()), false);
		}
	    } else obj = YoixObject.newArray(0);
	}
	return(obj);
    }


    private JTextComponent
    getJComboBoxTextEditor(Object comp) {

	ComboBoxEditor  editor;
	Object          texteditor = null;

	if (comp instanceof JComboBox) {
	    if ((editor = ((JComboBox)comp).getEditor()) != null)
		texteditor = editor.getEditorComponent();
	}
	return(texteditor instanceof JTextComponent ? (JTextComponent)texteditor : null);
    }


    private synchronized int
    getLabelItemIndex(ListModel listmodel, String label) {

	YoixSwingLabelItem  labelitems[];
	int                 length;
	int                 index = -1;
	int                 n;

	if (label != null) {
	    length = listmodel.getSize();
	    for (n = 0; n < length; n++) {
		if (label.equals(((YoixSwingLabelItem)listmodel.getElementAt(n)).getValue())) {
		    index = n;
		    break;
		}
	    }
	    if (index < 0) {
		for (n = 0; n < length; n++) {
		    if (label.equals(((YoixSwingLabelItem)listmodel.getElementAt(n)).getText())) {
			index = n;
			break;
		    }
		}
	    }
	}
	return(index);
    }


    private synchronized YoixSwingLabelItem[]
    getLabelItems(ListModel listmodel) {

	YoixSwingLabelItem  labelitems[];
	int                 n;

	labelitems = new YoixSwingLabelItem[listmodel.getSize()];
	for (n = 0; n < labelitems.length; n++)
	    labelitems[n] = (YoixSwingLabelItem)(listmodel.getElementAt(n));
	return(labelitems);
    }


    private synchronized YoixObject
    getLabels(Object comp, YoixObject obj) {

	YoixSwingLabelItem  labelitems[];
	Enumeration         enm;
	Dictionary          table;
	Object              key;
	Object              value;
	int                 length;
	int                 n;

	if (comp instanceof JList || comp instanceof YoixSwingJComboBox) {
	    if (comp instanceof JList)
		labelitems = getLabelItems(((JList)comp).getModel());
	    else labelitems = getLabelItems(((YoixSwingJComboBox)comp).getModel());
	    if (labelitems != null) {
		obj = YoixObject.newArray(length = labelitems.length);
		for (n = 0; n < length; n++)
		    obj.put(n, YoixObject.newString(labelitems[n].getText()), false);
	    } else obj = YoixObject.newArray(0);
	} else if (comp instanceof YoixSwingJSlider) {
	    table = ((YoixSwingJSlider)comp).getLabelTable();
	    if (table != null) {
		obj = YoixObject.newArray(2*(table.size()));
		n = 0;
		for (enm = table.keys(), n = 0; enm.hasMoreElements(); ) {
		    key = enm.nextElement();
		    if (key instanceof Integer) {
			obj.putInt(n++, ((Integer)key).intValue());
			value = table.get(key);
			if (value instanceof JLabel) {
			    obj.putString(n++, ((JLabel)value).getText());
			} else VM.abort(INTERNALERROR);
		    } else VM.abort(INTERNALERROR);
		}
	    } else obj = YoixObject.newArray(0);
	}
	return(obj);
    }


    private YoixObject
    getLayer(Object comp, YoixObject obj) {

	Container  parent;
	int        layer;

	if (comp instanceof Component) {
	    parent = ((Component)comp).getParent();
	    if (parent instanceof JLayeredPane) {
		layer = ((JLayeredPane)parent).getLayer((Component)comp);
		obj = YoixObject.newInt(layer);
	    }
	}
	return(obj);
    }


    private synchronized YoixObject
    getMappings(Object comp, YoixObject obj) {

	YoixSwingLabelItem  labelitems[];
	int                 length;
	int                 n;

	if (comp instanceof JList || comp instanceof YoixSwingJComboBox) {
	    if (comp instanceof JList)
		labelitems = getLabelItems(((JList)comp).getModel());
	    else labelitems = getLabelItems(((YoixSwingJComboBox)comp).getModel());
	    if (labelitems != null) {
		obj = YoixObject.newArray(length = labelitems.length);
		for (n = 0; n < length; n++)
		    obj.put(n, YoixObject.newString(labelitems[n].getValue()), false);
	    } else obj = YoixObject.newArray(0);
	}
	return(obj);
    }


    private YoixObject
    getMaximizable(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    obj = YoixObject.newInt(((JInternalFrame)comp).isMaximizable());
	return(obj);
    }


    private YoixObject
    getMaximized(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    obj = YoixObject.newInt(((JInternalFrame)comp).isMaximum());
	else if (comp instanceof JFrame)
	    obj = YoixObject.newInt(((JFrame)comp).getExtendedState() & JFrame.MAXIMIZED_BOTH);
	return(obj);
    }


    private YoixObject
    getMaximum(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    obj = YoixObject.newInt(((JScrollBar)comp).getMaximum());
	else if (comp instanceof JProgressBar)
	    obj = YoixObject.newInt(((JProgressBar)comp).getMaximum());
	else if (comp instanceof YoixSwingJSlider)
	    obj = YoixObject.newInt(((YoixSwingJSlider)comp).getMaximum());
	return(obj);
    }


    private YoixObject
    getMaximumSize(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixMakeScreen.yoixDimension(((Component)comp).getMaximumSize());
	return(obj);
    }


    private YoixObject
    getModal(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJDialog)
	    obj = YoixObject.newInt(((YoixSwingJDialog)comp).isModal());
	return(obj);
    }


    private YoixObject
    getMinimum(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    obj = YoixObject.newInt(((JScrollBar)comp).getMinimum());
	else if (comp instanceof JProgressBar)
	    obj = YoixObject.newInt(((JProgressBar)comp).getMinimum());
	else if (comp instanceof YoixSwingJSlider)
	    obj = YoixObject.newInt(((YoixSwingJSlider)comp).getMinimum());
	return(obj);
    }


    private YoixObject
    getMinimumSize(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixMakeScreen.yoixDimension(((Component)comp).getMinimumSize());
	return(obj);
    }


    private YoixObject
    getOpaque(Object comp, YoixObject obj) {

	if (comp instanceof JComponent)
	    obj = YoixObject.newInt(((JComponent)comp).isOpaque());
	return(obj);
    }


    private YoixObject
    getOrigin(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = YoixMakeScreen.yoixPoint(getViewRect().getLocation());
	else if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixMakeScreen.yoixPoint(((YoixSwingJTextComponent)comp).getOrigin());
	else if (comp instanceof YoixSwingJScrollPane)
	    obj = YoixMakeScreen.yoixPoint(getViewRect().getLocation());
	else if (comp instanceof YoixSwingJCanvas) {
	    obj = YoixObject.newPoint(((YoixSwingJCanvas)comp).getOrigin2D());
	    //////obj = YoixObject.newPoint((Point2D)((YoixSwingJCanvas)comp).getOrigin());
	}
	return(obj);
    }


    private YoixObject
    getOutputFilter(Object comp, YoixObject obj) {
 
 	if (comp instanceof YoixSwingJTable)
 	    obj = ((YoixSwingJTable)comp).getOutputFilter();
 	return(obj);
    }
 

    private YoixObject
    getPage(Object comp, YoixObject obj) {

	URL  url;

	if (comp instanceof JTextPane) {
	    if ((url = ((JTextPane)comp).getPage()) != null)
		obj = YoixObject.newString(url.toString());
	    else obj = YoixObject.newString();
	}
	return(obj);
    }
 

    private YoixObject
    getPercentComplete(Object comp, YoixObject obj) {

	if (comp instanceof JProgressBar)
	    obj = YoixObject.newDouble(100*((JProgressBar)comp).getPercentComplete());
	return(obj);
    }


    private YoixObject
    getPressed(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    obj = YoixObject.newInt(((AbstractButton)comp).getModel().isPressed());
	return(obj);
    }


    private YoixObject
    getReset(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJColorChooser)
	    obj = YoixMake.yoixColor(((YoixSwingJColorChooser)comp).getReset());
	return(obj);
    }


    private YoixObject
    getRollover(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    obj = YoixObject.newInt(((AbstractButton)comp).getModel().isRollover());
	return(obj);
    }


    private YoixObject
    getRowHeightAdjustment(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = YoixObject.newDouble(YoixMakeScreen.yoixDistance(((YoixSwingJTable)comp).getRowHeightAdjustment()));

	return(obj);
    }


    private YoixObject
    getRows(Object comp, YoixObject obj) {

	if (comp instanceof JTextArea)
	    obj = YoixObject.newInt(((JTextArea)comp).getRows());
	else if (comp instanceof JList)
	    obj = YoixObject.newInt(((JList)comp).getVisibleRowCount());
	else if (comp instanceof YoixSwingJTable)
	    obj = YoixObject.newInt(((YoixSwingJTable)comp).getRowCount());
	else if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixObject.newInt(((YoixSwingJTextComponent)comp).getRows());
	else if (comp instanceof YoixSwingJComboBox)
	    obj = YoixObject.newInt(((YoixSwingJComboBox)comp).getRows());
	return(obj);
    }


    private YoixObject
    getScroll(Object comp, YoixObject obj) {

	int  hscroll;
	int  vscroll;
	int  value;

	if (comp instanceof JScrollPane) {
	    hscroll = ((JScrollPane)comp).getHorizontalScrollBarPolicy();
	    vscroll = ((JScrollPane)comp).getVerticalScrollBarPolicy();
	    if (hscroll == JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) {
		if (vscroll == JScrollPane.VERTICAL_SCROLLBAR_ALWAYS)
		    value = YOIX_ALWAYS;
		else if (vscroll == JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
		    value = YOIX_HORIZONTAL_ALWAYS|YOIX_VERTICAL_AS_NEEDED;
		else value = YOIX_HORIZONTAL;
	    } else if (hscroll == JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
		if (vscroll == JScrollPane.VERTICAL_SCROLLBAR_ALWAYS)
		    value = YOIX_HORIZONTAL_AS_NEEDED|YOIX_VERTICAL_ALWAYS;
		else if (vscroll == JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
		    value = YOIX_AS_NEEDED;
		else value = YOIX_HORIZONTAL_AS_NEEDED|YOIX_VERTICAL_NEVER;
	    } else {
		if (vscroll == JScrollPane.VERTICAL_SCROLLBAR_ALWAYS)
		    value = YOIX_VERTICAL;
		else if (vscroll == JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
		    value = YOIX_HORIZONTAL_NEVER|YOIX_VERTICAL_AS_NEEDED;
		else value = YOIX_NEVER;
	    }
	    obj = YoixObject.newInt(value);
	}
	return(obj);
    }


    private YoixObject
    getSelected(Object comp, YoixObject obj) {

	YoixObject  items;
	YoixObject  element;
	YoixObject  layout;
	Object      selection[];
	Object      selected;
	Object      managed;
	String      value;
	String      str;
	int         length;
	int         n;

	if (comp instanceof AbstractButton)
	    obj = YoixObject.newInt(((AbstractButton)comp).isSelected());
	else if (comp instanceof JTextComponent) {
	    value = ((JTextComponent)comp).getSelectedText();
	    obj = YoixObject.newString(value);
	} else if (comp instanceof JList) {
	    // need to handle non-string object, too
	    if ((selection = ((JList)comp).getSelectedValues()) != null) {
		obj = YoixObject.newArray(selection.length);
		for (n = 0; n < selection.length; n++) {
		    value = ((YoixSwingLabelItem)selection[n]).getValue();
		    obj.put(n, YoixObject.newString(value), false);
		}
	    } else obj = YoixObject.newArray();
	} else if (comp instanceof YoixSwingJComboBox) {
	    // need to handle non-string object, too
	    if ((selected = ((YoixSwingJComboBox)comp).getSelectedItem()) != null) {
		if (selected instanceof YoixSwingLabelItem)
		    value = ((YoixSwingLabelItem)selected).getValue();
		else if (selected instanceof String)
		    value = (String)selected;
		else value = null;
		if (value != null && data.getBoolean(N_AUTOTRIM))
		    value = value.trim();
		obj = YoixObject.newString(value);
	    } else obj = YoixObject.newString();
	} else if (comp instanceof JTabbedPane) {
	    obj = YoixObject.newNull();
	    if ((selected = ((JTabbedPane)comp).getSelectedComponent()) != null) {
		if ((layout = data.getObject(N_LAYOUT)) != null) {
		    length = layout.length();
		    for (n = layout.offset(); n < length; n++) {
			if ((element = layout.getObject(n)) != null) {
			    if (element.getManagedObject() == selected) {
				obj = element;
				break;
			    }
			}
		    }
		}
	    }
	} else if (comp instanceof JDesktopPane) {
	    if ((selected = ((JDesktopPane)comp).getSelectedFrame()) != null) {
		if (selected instanceof YoixSwingJInternalFrame) {
		    if ((obj = ((YoixSwingJInternalFrame)selected).getContext()) == null)
			YoixObject.newNull();
		} else obj = YoixObject.newNull();
	    } else obj = YoixObject.newNull();
	} else if (comp instanceof ButtonGroup) {
	    obj = YoixObject.newNull();
	    if ((selected = ((ButtonGroup)comp).getSelection()) != null) {
		if ((items = data.getObject(N_ITEMS)) != null) {
		    length = items.length();
		    for (n = items.offset(); n < length; n++) {
			if ((element = items.getObject(n)) != null) {
			    managed = element.getManagedObject();
			    if (managed instanceof AbstractButton) {
				if (((AbstractButton)managed).getModel() == selected) {
				    switch (data.getInt(N_MODEL, 0)) {
					case 0:	// AWT CheckboxGroup model
					    value = ((AbstractButton)managed).getActionCommand();
					    obj = YoixObject.newString(value);
					    break;

					default:
					    obj = element;
					    break;
				    }
				    break;
				}
			    }
			}
		    }
		}
	    }
	}

	return(obj);
    }


    private YoixObject
    getSelectedEnds(Object comp, YoixObject obj) {

	Object  editor = comp;

	if (editor instanceof JTextComponent || (editor = getJComboBoxTextEditor(comp)) != null) {
	    obj = YoixObject.newArray(2);
	    obj.putInt(0, ((JTextComponent)editor).getSelectionStart());
	    obj.putInt(1, ((JTextComponent)editor).getSelectionEnd());
	}
	return(obj);
    }


    private YoixObject
    getSelectedIndex(Object comp, YoixObject obj) {

	if (comp instanceof JList)
	    obj = YoixObject.newInt(((JList)comp).getSelectedIndex());
	else if (comp instanceof YoixSwingJComboBox)
	    obj = YoixObject.newInt(((YoixSwingJComboBox)comp).getSelectedIndex());
	return(obj);
    }


    private YoixObject
    getSelectedLabel(Object comp, YoixObject obj) {

	YoixObject  items;
	YoixObject  element;
	YoixObject  layout;
	Object      selection[];
	Object      selected;
	Object      managed;
	String      value;
	String      str;
	int         length;
	int         n;

	if (comp instanceof JList) {
	    // need to handle non-string object, too
	    if ((selection = ((JList)comp).getSelectedValues()) != null) {
		obj = YoixObject.newArray(selection.length);
		for (n = 0; n < selection.length; n++) {
		    value = ((YoixSwingLabelItem)selection[n]).getText();
		    obj.put(n, YoixObject.newString(value), false);
		}
	    } else obj = YoixObject.newArray();
	} else if (comp instanceof YoixSwingJComboBox) {
	    // need to handle non-string object, too
	    if ((selected = ((YoixSwingJComboBox)comp).getSelectedItem()) != null) {
		if (selected instanceof YoixSwingLabelItem)
		    value = ((YoixSwingLabelItem)selected).getText();
		else if (selected instanceof String)
		    value = (String)selected;
		else value = null;
		if (value != null && data.getBoolean(N_AUTOTRIM))
		    value = value.trim();
		obj = YoixObject.newString(value);
	    } else obj = YoixObject.newString();
	}

	return(obj);
    }


    private YoixObject
    getSelectionBackground(Object comp, YoixObject obj) {

	if (comp instanceof JTextComponent)
	    obj = YoixMake.yoixColor(((JTextComponent)comp).getSelectionColor());
	else if (comp instanceof YoixSwingJTable)
	    obj = YoixMake.yoixColor(((YoixSwingJTable)comp).getSelectionBackground());
	else if (comp instanceof YoixSwingJTree)
	    obj = YoixMake.yoixColor(((YoixSwingJTree)comp).getSelectionBackground());
	return(obj);
    }


    private YoixObject
    getSelectionForeground(Object comp, YoixObject obj) {

	if (comp instanceof JTextComponent)
	    obj = YoixMake.yoixColor(((JTextComponent)comp).getSelectedTextColor());
	else if (comp instanceof YoixSwingJTable)
	    obj = YoixMake.yoixColor(((YoixSwingJTable)comp).getSelectionForeground());
	else if (comp instanceof YoixSwingJTree)
	    obj = YoixMake.yoixColor(((YoixSwingJTree)comp).getSelectionForeground());
	return(obj);
    }


    private YoixObject
    getSizeControl(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJScrollPane)
	    obj = YoixObject.newInt(((YoixSwingJScrollPane)comp).getSizeControl());
	else if (comp instanceof YoixSwingJTabbedPane)
	    obj = YoixObject.newInt(((YoixSwingJTabbedPane)comp).getSizeControl());
	return(obj);
    }


    private YoixObject
    getState(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    obj = YoixObject.newInt(((AbstractButton)comp).isSelected());
	else if (comp instanceof YoixSwingJCanvas)
	    obj = YoixObject.newInt(((YoixSwingJCanvas)comp).getState());
	return(obj);
    }


    private YoixObject
    getSyncCount(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixObject.newInt(((YoixSwingJTextComponent)comp).getSyncCount());
	return(obj);
    }


    private YoixObject
    getText(Object comp, YoixObject obj) {

	Object  editor;
	String  str;

	//
	// JPasswordField has a bunch of seemingly unnecessary overhead,
	// particularly in the context of the Yoix interpreter, so just
	// use JTextComponent.getText(), even though it is deprecated
	// for JPasswordField.
	//

	if (comp instanceof AbstractButton)
	    str = ((AbstractButton)comp).getText();
	else if (comp instanceof JPopupMenu)
	    str = ((JPopupMenu)comp).getLabel();
	else if (comp instanceof JLabel)
	    str = ((JLabel)comp).getText();
	else if (comp instanceof JProgressBar)
	    str = ((JProgressBar)comp).getString();
	else if (comp instanceof YoixSwingJTable) {
	    str = null;		// we'll set obj directly
	    obj = ((YoixSwingJTable)comp).getText();
	} else if (comp instanceof JTextComponent)
	    str = ((JTextComponent)comp).getText();
	else if (comp instanceof YoixSwingJTextComponent)
	    str = ((YoixSwingJTextComponent)comp).getText();
	else if (comp instanceof JComboBox && (editor = ((JComboBox)comp).getEditor()) != null && (editor = ((ComboBoxEditor)editor).getEditorComponent()) instanceof JTextComponent)
	    str = ((JTextComponent)editor).getText();
	else str = null;

	if (str != null) {
	    if (data.getBoolean(N_AUTOTRIM))
		str = str.trim();
	    obj = YoixObject.newString(str);
	}

	return(obj);
    }


    private YoixObject
    getTextWrap(Object comp, YoixObject obj) {

	if (comp instanceof JTextArea) {
	    if (((JTextArea)comp).getLineWrap()) {
		if (((JTextArea)comp).getWrapStyleWord())
		    obj = YoixObject.newInt(1);
		else obj = YoixObject.newInt(-1);
	    } else obj = YoixObject.newInt(0);
	}
	return(obj);
    }


    private YoixObject
    getTrackFocus(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTabbedPane)
	    obj = YoixObject.newInt(((YoixSwingJTabbedPane)comp).getTrackFocus());
	return(obj);
    }


    private YoixObject
    getTransferHandler(Object comp, YoixObject obj) {

	TransferHandler  handler;

	if (comp instanceof JComponent) {
	    if ((handler = ((JComponent)comp).getTransferHandler()) != null) {
		if ((obj = data.getObject(N_TRANSFERHANDLER)) != null) {
		    if (obj.isTransferHandler() == false)
			obj = YoixObject.newTransferHandler(handler);
		} else obj = YoixObject.newNull();
	    } else obj = YoixObject.newNull();
	}
	return(obj);
    }


    private YoixObject
    getTypes(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = ((YoixSwingJTable)comp).yoixTypes();
	return(obj);
    }


    private YoixObject
    getUnitIncrement(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    obj = YoixObject.newInt(((JScrollBar)comp).getUnitIncrement());
	else if (comp instanceof YoixSwingJSlider)
	    obj = YoixObject.newInt(((YoixSwingJSlider)comp).getUnitIncrement());
	return(obj);
    }


    private YoixObject
    getValue(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    obj = YoixObject.newInt(((JScrollBar)comp).getValue());
	else if (comp instanceof JProgressBar)
	    obj = YoixObject.newInt(((JProgressBar)comp).getValue());
	else if (comp instanceof YoixSwingJSlider)
	    obj = YoixObject.newInt(((YoixSwingJSlider)comp).getValue());
	return(obj);
    }


    private YoixObject
    getValues(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = ((YoixSwingJTable)comp).getValues();
	return(obj);
    }


    private YoixObject
    getViewport(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = YoixMakeScreen.yoixRectangle(getViewRect());
	else if (comp instanceof YoixSwingJTextComponent)
	    obj = YoixMakeScreen.yoixRectangle(((YoixSwingJTextComponent)comp).getViewport());
	else if (comp instanceof YoixSwingJScrollPane)
	    obj = YoixMakeScreen.yoixRectangle(getViewRect());
	return(obj);
    }


    private YoixObject
    getViewRowCount(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = YoixObject.newInt(((YoixSwingJTable)comp).getViewableRowCount());
	return(obj);
    }


    private YoixObject
    getVisible(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    obj = YoixObject.newInt(((Component)comp).isVisible());
	return(obj);
    }


    private YoixObject
    getVisibleAmount(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    obj = YoixObject.newInt(((JScrollBar)comp).getVisibleAmount());
	return(obj);
    }


    private YoixObject
    getVisibleWidth(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = YoixObject.newInt(((YoixSwingJTable)comp).getVisibleColumnCount());
	return(obj);
    }


    private YoixObject
    getWidth(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    obj = YoixObject.newInt(((YoixSwingJTable)comp).getColumnCount());
	return(obj);
    }


    private YoixObject
    handleExecuteField(String name, YoixObject argv[]) {

	YoixObject  obj;
	Object      comp;

	comp = this.peer;		// snapshot - just to be safe

	switch (activeField(name, activefields)) {
	    case V_ACTION:
		obj =  builtinAction(comp, name, argv);
		break;

	    case V_CLICK:
		obj = builtinClick(comp, name, argv);
		break;

	    case V_FINDNEXTMATCH:
		obj = builtinFindNextMatch(comp, name, argv);
		break;

	    case V_GETENABLED:
		obj = builtinGetEnabled(comp, name, argv);
		break;

	    case V_GETSTATE:
		obj = builtinGetState(comp, name, argv);
		break;

	    case V_ITEM:
		obj = builtinItem(comp, name, argv);
		break;

	    case V_MODELTOVIEW:
		obj = builtinModelToView(comp, name, argv);
		break;

	    case V_REPAINT:
		obj = builtinRepaint(comp, name, argv);
		break;

	    case V_SETENABLED:
		obj = builtinSetEnabled(comp, name, argv);
		break;

	    case V_SETINCREMENT:
		obj = builtinSetIncrement(comp, name, argv);
		break;

	    case V_SETSTATE:
		obj = builtinSetState(comp, name, argv);
		break;

	    case V_SETVALUES:
		obj = builtinSetValues(comp, name, argv);
		break;

	    case V_SUBTEXT:
		obj = builtinSubtext(comp, name, argv);
		break;

	    case V_VIEWTOMODEL:
		obj = builtinViewToModel(comp, name, argv);
		break;

	    case V_ZOOM:
		obj = builtinZoom(comp, name, argv);
		break;

	    default:
		obj = null;
		break;
	}
	return(obj);
    }


    private YoixObject
    handleGetField(String name, YoixObject obj) {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	switch (activeField(name, activefields)) {
	    case V_ACCELERATOR:
		obj = getAccelerator(comp, obj);
		break;

	    case V_ADJUSTING:
		obj = getAdjusting(comp, obj);
		break;

	    case V_ALIGNMENT:
		obj = getAlignment(comp, obj);
		break;

	    case V_APPROVEBUTTONMNEMONIC:
		obj = getApproveButtonMnemonic(comp, obj);
		break;

	    case V_APPROVEBUTTONTEXT:
		obj = getApproveButtonText(comp, obj);
		break;

	    case V_APPROVEBUTTONTOOLTIPTEXT:
		obj = getApproveButtonToolTipText(comp, obj);
		break;

	    case V_ARMED:
		obj = getArmed(comp, obj);
		break;

	    case V_BACKGROUND:
		obj = getBackground(comp, obj);
		break;

	    case V_BASE:
		obj = getBase(comp, obj);
		break;

	    case V_BLOCKINCREMENT:
		obj = getBlockIncrement(comp, obj);
		break;

	    case V_BORDER:
		obj = getBorder(comp, obj);
		break;

	    case V_BORDERCOLOR:
		obj = getBorderColor(comp, obj);
		break;

	    case V_CARET:
		obj = getCaret(comp, obj);
		break;

	    case V_CARETCOLOR:
		obj = getCaretColor(comp, obj);
		break;

	    case V_CARETOWNERCOLOR:
		obj = getCaretOwnerColor(comp, obj);
		break;

	    case V_CELLSIZE:
		obj = getCellSize(comp, obj);
		break;

	    case V_CLOSABLE:
		obj = getClosable(comp, obj);
		break;

	    case V_CLOSED:
		obj = getClosed(comp, obj);
		break;

	    case V_COLOR:
		obj = getColor(comp, obj);
		break;

	    case V_COLUMNS:
		obj = getColumns(comp, obj);
		break;

	    case V_COMPRESSEVENTS:
		obj = getCompressEvents(comp, obj);
		break;

	    case V_DECORATIONSTYLE:
		obj = getDecorationStyle(comp, obj);
		break;

	    case V_DIRECTORY:
		obj = getDirectory(comp, obj);
		break;

	    case V_DIVIDERLOCATION:
		obj = getDividerLocation(comp, obj);
		break;

	    case V_DIVIDERLOCKED:
		obj = getDividerLocked(comp, obj);
		break;

	    case V_DIVIDERSIZE:
		obj = getDividerSize(comp, obj);
		break;

	    case V_DISPOSE:
		obj = getDispose(comp, obj);	// currently OK if comp is null
		break;

	    case V_DOUBLEBUFFERED:
		obj = getDoubleBuffered(comp, obj);
		break;

	    case V_DRAGENABLED:
		obj = getDragEnabled(comp, obj);
		break;

	    case V_ECHO:
		obj = getEcho(comp, obj);
		break;

	    case V_EDIT:
		obj = getEdit(comp, obj);
		break;

	    case V_ENABLED:
		obj = getEnabled(comp, obj);
		break;

	    case V_EXTENT:
		obj = getExtent(comp, obj);
		break;

	    case V_FILE:
		obj = getFile(comp, obj);
		break;

	    case V_FILESELECTIONMODE:
		obj = getFileSelectionMode(comp, obj);
		break;

	    case V_FILTER:
		obj = getFilter(comp, obj);
		break;

	    case V_FOCUSABLE:
		obj = getFocusable(comp, obj);
		break;

	    case V_FOCUSOWNER:
		obj = getFocusOwner(comp, obj);
		break;

	    case V_FONT:
		obj = getFont(comp, obj);
		break;

	    case V_FOREGROUND:
		obj = getForeground(comp, obj);
		break;

	    case V_FRAMES:
		obj = getFrames(comp, obj);
		break;

	    case V_FULLSCREEN:
		obj = getFullScreen(comp, obj);
		break;

	    case V_GRIDSIZE:
		obj = getGridSize(comp, obj);
		break;

	    case V_GRAPHICS:
		obj = getGraphics(comp, obj);
		break;

	    case V_HEADERS:
		obj = getHeaders(comp, obj);
		break;

	    case V_HEADERICONS:
		obj = getHeaderIcons(comp, obj);
		break;

	    case V_HIDDENFILES:
		obj = getHiddenFiles(comp, obj);
		break;

	    case V_ICONIFIABLE:
		obj = getIconifiable(comp, obj);
		break;

	    case V_ICONIFIED:
		obj = getIconified(comp, obj);
		break;

	    case V_ICONS:
		obj = getIcons(comp, obj);
		break;

	    case V_INDETERMINATE:
		obj = getIndeterminate(comp, obj);
		break;

	    case V_INPUTFILTER:
		obj = getInputFilter(comp, obj);
		break;

	    case V_INSETS:
		obj = getInsets(comp, obj);
		break;

	    case V_IPAD:
		obj = getIpad(comp, obj);
		break;

	    case V_ITEMS:
		obj = getItems(comp, obj);
		break;

	    case V_LABELS:
		obj = getLabels(comp, obj);
		break;

	    case V_LAYER:
		obj = getLayer(comp, obj);
		break;

	    case V_LOCATION:
		obj = getLocation(comp, obj);
		break;

	    case V_MAPPINGS:
		obj = getMappings(comp, obj);
		break;

	    case V_MAXIMIZABLE:
		obj = getMaximizable(comp, obj);
		break;

	    case V_MAXIMIZED:
		obj = getMaximized(comp, obj);
		break;

	    case V_MAXIMUM:
		obj = getMaximum(comp, obj);
		break;

	    case V_MAXIMUMSIZE:
		obj = getMaximumSize(peerscroller != null ? peerscroller : comp, obj);
		break;

	    case V_MINIMUM:
		obj = getMinimum(comp, obj);
		break;

	    case V_MINIMUMSIZE:
		obj = getMinimumSize(peerscroller != null ? peerscroller : comp, obj);
		break;

	    case V_MODAL:
		obj = getModal(comp, obj);
		break;

	    case V_OPAQUE:
		obj = getOpaque(comp, obj);
		break;

	    case V_ORIGIN:
		obj = getOrigin(comp, obj);
		break;

	    case V_OUTPUTFILTER:
 		obj = getOutputFilter(comp, obj);
 		break;

	    case V_PAGE:
		obj = getPage(comp, obj);
		break;

	    case V_PARENT:
		obj = getParent(comp, obj);
		break;

	    case V_PERCENTCOMPLETE:
		obj = getPercentComplete(comp, obj);
		break;

	    case V_PREFERREDSIZE:
		obj = getPreferredSize(peerscroller != null ? peerscroller : comp, obj);
		break;

	    case V_PRESSED:
		obj = getPressed(comp, obj);
		break;

	    case V_RESET:
		obj = getReset(comp, obj);
		break;

	    case V_ROLLOVER:
		obj = getRollover(comp, obj);
		break;

	    case V_ROOT:
		obj = getRoot(comp, obj);
		break;

	    case V_ROWHEIGHTADJUSTMENT:
		obj = getRowHeightAdjustment(comp, obj);
		break;

	    case V_ROWS:
		obj = getRows(comp, obj);
		break;

	    case V_SCREEN:
		obj = getScreen(comp, obj);
		break;

	    case V_SCROLL:
		obj = getScroll(peerscroller != null ? peerscroller : comp, obj);
		break;

	    case V_SELECTED:
		obj = getSelected(comp, obj);
		break;

	    case V_SELECTEDENDS:
		obj = getSelectedEnds(comp, obj);
		break;

	    case V_SELECTEDINDEX:
		obj = getSelectedIndex(comp, obj);
		break;

	    case V_SELECTEDLABEL:
		obj = getSelectedLabel(comp, obj);
		break;

	    case V_SELECTIONBACKGROUND:
		obj = getSelectionBackground(comp, obj);
		break;

	    case V_SELECTIONFOREGROUND:
		obj = getSelectionForeground(comp, obj);
		break;

	    case V_SHOWING:
		obj = getShowing(comp, obj);
		break;

	    case V_SIZE:
		obj = getSize(comp, obj);
		break;

	    case V_SIZECONTROL:
		obj = getSizeControl(peerscroller != null ? peerscroller : comp, obj);
		break;

	    case V_STATE:
		obj = getState(comp, obj);
		break;

	    case V_SYNCCOUNT:
		obj = getSyncCount(comp, obj);
		break;

	    case V_TEXT:
		obj = getText(comp, obj);
		break;

	    case V_TEXTWRAP:
		obj = getTextWrap(comp, obj);
		break;

	    case V_TRACKFOCUS:
		obj = getTrackFocus(comp, obj);
		break;

	    case V_TRANSFERHANDLER:
		obj = getTransferHandler(comp, obj);
		break;

	    case V_TYPES:
		obj = getTypes(comp, obj);
		break;

	    case V_UNITINCREMENT:
		obj = getUnitIncrement(comp, obj);
		break;

	    case V_VALUE:
		obj = getValue(comp, obj);
		break;

	    case V_VALUES:
		obj = getValues(comp, obj);
		break;

	    case V_VIEWPORT:
		obj = getViewport(comp, obj);
		break;

	    case V_VIEWROWCOUNT:
		obj = getViewRowCount(comp, obj);
		break;

	    case V_VISIBLE:
		//obj = getVisible(comp, obj);
		obj = getVisible(peerscroller != null ? peerscroller : comp, obj);
		break;

	    case V_VISIBLEWIDTH:
		obj = getVisibleWidth(comp, obj);
		break;

	    case V_VISIBLEAMOUNT:
		obj = getVisibleAmount(comp, obj);
		break;

	    case V_WIDTH:
		obj = getWidth(comp, obj);
		break;
	}

	return(obj);
    }


    private void
    handleJListIndex(Object comp, YoixObject obj) {

	int  index;
	int  size;
	int  first;
	int  last;

	if ((size = ((JList)comp).getModel().getSize()) > 0) {
	    if ((index = obj.intValue()) < 0)
		index = 0;
	    if (index >= size)
		index = size - 1;
	    ((JList)comp).ensureIndexIsVisible(index);
	}
    }


    private void
    handlePack() {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof YoixInterfaceWindow)
	    ((YoixInterfaceWindow)comp).pack();
    }


    private YoixObject
    handleSetField(String name, YoixObject obj) {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp != null && obj != null) {
	    switch (activeField(name, activefields)) {
		case V_ACCELERATOR:
		    setAccelerator(comp, obj);
		    break;

		case V_ADJUSTING:
		    setAdjusting(comp, obj);
		    break;

		case V_AFTERPAN:
		    setAfterPan(comp, obj);
		    break;

		case V_AFTERSELECT:
		    setAfterSelect(comp, obj);
		    break;

		case V_AFTERZOOM:
		    setAfterZoom(comp, obj);
		    break;

		case V_ALIGNMENT:
		    setAlignment(comp, obj);
		    break;

		case V_ALLOWEDIT:
		    setAllowEdit(comp, obj);
		    break;

		case V_ALTALIGNMENT:
		    setAltAlignment(comp, obj);
		    break;

		case V_ALTBACKGROUND:
		    setAltBackground(comp, obj);
		    break;

		case V_ALTFONT:
		    setAltFont(comp, obj);
		    break;

		case V_ALTFOREGROUND:
		    setAltForeground(comp, obj);
		    break;

		case V_ALTGRIDCOLOR:
		    setAltGridColor(comp, obj);
		    break;

		case V_ALTTOOLTIPTEXT:
		    setAltToolTipText(comp, obj);
		    break;

		case V_ANCHOR:
		    setAnchor(comp, obj);
		    break;

		case V_APPROVEBUTTONMNEMONIC:
		    setApproveButtonMnemonic(comp, obj);
		    break;

		case V_APPROVEBUTTONTEXT:
		    setApproveButtonText(comp, obj);
		    break;

		case V_APPROVEBUTTONTOOLTIPTEXT:
		    setApproveButtonToolTipText(comp, obj);
		    break;

		case V_ARMED:
		    setArmed(comp, obj);
		    break;

		case V_BACKGROUND:
		    setBackground(comp, peerscroller, obj);
		    break;

		case V_BACKGROUNDHINTS:
		    setBackgroundHints(comp, obj);
		    break;

		case V_BACKGROUNDIMAGE:
		    setBackgroundImage(comp, obj);
		    break;

		case V_BASE:
		    setBase(comp, obj);
		    break;

		case V_BLOCKINCREMENT:
		    setBlockIncrement(comp, obj);
		    break;

		case V_BORDER:
		    setBorder(comp, obj);
		    break;

		case V_BORDERCOLOR:
		    setBorderColor(comp, obj);
		    break;

		case V_BUTTONS:
		    setButtons(comp, obj);
		    break;

		case V_CARET:
		    setCaret(comp, obj);
		    break;

		case V_CARETALPHA:
		    setCaretAlpha(comp, obj);
		    break;

		case V_CARETCOLOR:
		    setCaretColor(comp, obj);
		    break;

		case V_CARETMODEL:
		    setCaretModel(comp, obj);
		    break;

		case V_CARETOWNERCOLOR:
		    setCaretOwnerColor(comp, obj);
		    break;

		case V_CELLCOLORS:
		    setCellColors(comp, obj);
		    break;

		case V_CLICKCOUNT:
		    setClickCount(comp, obj);
		    break;

		case V_CLOSABLE:
		    setClosable(comp, obj);
		    break;

		case V_CLOSED:
		    setClosed(comp, obj);
		    break;

		case V_CLOSEDICON:
		    setClosedIcon(comp, obj);
		    break;

		case V_COLOR:
		    setColor(comp, obj);
		    break;

		case V_COLUMNS:
		    setColumns(comp, obj);
		    break;

		case V_COMMAND:
		    setCommand(comp, obj);
		    break;

		case V_COMPRESSEVENTS:
		    setCompressEvents(comp, obj);
		    break;

		case V_CONTINUOUSLAYOUT:
		    setContinuousLayout(comp, obj);
		    break;

		case V_CURSOR:
		    setCursor(comp, obj);
		    break;

		case V_DECORATIONSTYLE:
		    setDecorationStyle(comp, obj);
		    break;

		case V_DESKTOP:
		    setDesktop(comp, obj);
		    break;

		case V_DIRECTORY:
		    setDirectory(comp, obj);
		    break;

		case V_DISPOSE:
		    setDispose(comp, obj);
		    break;

		case V_DIVIDERLOCATION:
		    setDividerLocation(comp, obj);
		    break;

		case V_DIVIDERLOCKED:
		    setDividerLocked(comp, obj);
		    break;

		case V_DIVIDERSIZE:
		    setDividerSize(comp, obj);
		    break;

		case V_DOUBLEBUFFERED:
		    setDoubleBuffered(comp, obj);
		    break;

		case V_DRAGENABLED:
		    setDragEnabled(comp, obj);
		    break;

		case V_ECHO:
		    setEcho(comp, obj);
		    break;

		case V_EDIT:
		    setEdit(comp, obj);
		    break;

		case V_EDITBACKGROUND:
		    setEditBackground(comp, obj);
		    break;

		case V_EDITFOREGROUND:
		    setEditForeground(comp, obj);
		    break;

		case V_ENABLED:
		    setEnabled(comp, obj);
		    break;

		case V_EXPANDSSELECTEDNODES:
		    setExpandsSelectedNodes(comp, obj);
		    break;

		case V_EXTENT:
		    setExtent(comp, obj);
		    break;

		case V_FILE:
		    setFile(comp, obj);
		    break;

		case V_FILESELECTIONMODE:
		    setFileSelectionMode(comp, obj);
		    break;

		case V_FILTER:
		    setFilter(comp, obj);
		    break;

		case V_FILTERS:
		    setFilters(comp, obj);
		    break;

		case V_FIRSTFOCUS:
		    setFirstFocus(comp, obj);
		    break;

		case V_FLOATABLE:
		    setFloatable(comp, obj);
		    break;

		case V_FOCUSABLE:
		    setFocusable(comp, obj);
		    break;

		case V_FONT:
		    setFont(comp, obj);
		    break;

		case V_FOREGROUND:
		    setForeground(comp, obj);
		    break;

		case V_FRONTTOBACK:
		    setFrontToBack(comp, obj);
		    break;

		case V_FULLSCREEN:
		    setFullScreen(comp, obj);
		    break;

		case V_GLASSPANE:
		    setGlassPane(comp, obj);
		    break;

		case V_GRIDCOLOR:
		    setGridColor(comp, obj);
		    break;

		case V_GRIDSIZE:
		    setGridSize(comp, obj);
		    break;

		case V_GROUP:
		    setGroup(comp, obj);
		    break;

		case V_GRAPHICS:
		    setGraphics(comp, obj);
		    break;

		case V_HEADERS:
		    setHeaders(comp, obj);
		    break;

		case V_HEADERICONS:
		    setHeaderIcons(comp, obj);
		    break;

		case V_HIDDENFILES:
		    setHiddenFiles(comp, obj);
		    break;

		case V_HIGHLIGHTFLAGS:
		    setHighlightFlags(comp, obj);
		    break;

		case V_ICON:
		    setIcon(comp, obj);
		    break;

		case V_ICONIFIABLE:
		    setIconifiable(comp, obj);
		    break;

		case V_ICONIFIED:
		    setIconified(comp, obj);
		    break;

		case V_ICONS:
		    setIcons(comp, obj);
		    break;

		case V_INDETERMINATE:
		    setIndeterminate(comp, obj);
		    break;

		case V_INDEX:
		    setIndex(comp, obj);
		    break;

		case V_INPUTFILTER:
		    setInputFilter(comp, obj);
		    break;

		case V_INSETS:
		    setInsets(comp, obj);
		    break;

		case V_INVERTED:
		    setInverted(comp, obj);
		    break;

		case V_IPAD:
		    setIpad(comp, obj);
		    break;

		case V_ITEMARRAY:		// deprecated
		    setItemArray(comp, obj);
		    break;

		case V_ITEMS:
		    setItems(comp, obj);
		    break;

		case V_KEEPHIDDEN:
		    setKeepHidden(comp, obj);
		    break;

		case V_LABELS:
		    setLabels(comp, obj);
		    break;

		case V_LAYER:
		    setLayer(comp, obj);
		    break;

		case V_LAYOUT:
		    setLayout(comp, obj);
		    break;

		case V_LAYOUTMANAGER:
		    setLayoutManager(comp, obj);
		    break;

		case V_LEAFICON:
		    setLeafIcon(comp, obj);
		    break;

		case V_LOCATION:
		    setLocation(comp, obj);
		    break;

		case V_MAJORTICKSPACING:
		    setMajorTickSpacing(comp, obj);
		    break;

		case V_MAPPINGS:
		    setMappings(comp, obj);
		    break;

		case V_MAXIMIZABLE:
		    setMaximizable(comp, obj);
		    break;

		case V_MAXIMIZED:
		    setMaximized(comp, obj);
		    break;

		case V_MAXIMUM:
		    setMaximum(comp, obj);
		    break;

		case V_MAXIMUMSIZE:
		    setMaximumSize(peerscroller != null ? peerscroller : comp, obj);
		    break;

		case V_MENUBAR:
		    setMenuBar(comp, obj);
		    break;

		case V_MINIMUM:
		    setMinimum(comp, obj);
		    break;

		case V_MINIMUMSIZE:
		    setMinimumSize(peerscroller != null ? peerscroller : comp, obj);
		    break;

		case V_MINORTICKSPACING:
		    setMinorTickSpacing(comp, obj);
		    break;

		case V_MNEMONIC:
		    setMnemonic(comp, obj);
		    break;

		case V_MODAL:
		    setModal(comp, obj);
		    break;

		case V_MODE:
		    setMode(comp, obj);
		    break;

		case V_MULTIPLEMODE:
		    setMultipleMode(comp, obj);
		    break;

		case V_NEXTCARD:
		    setNextCard(comp, obj);
		    break;

		case V_NEXTFOCUS:
		    setNextFocus(comp, obj);
		    break;

		case V_ONETOUCHEXPANDABLE:
		    setOneTouchExpandable(comp, obj);
		    break;

		case V_OPAQUE:
		    setOpaque(comp, obj);
		    break;

		case V_OPENICON:
		    setOpenIcon(comp, obj);
		    break;

		case V_ORIENTATION:
		    setOrientation(comp, obj);
		    break;

		case V_ORIGIN:
		    setOrigin(comp, obj);
		    break;

		case V_OUTPUTFILTER:
		    setOutputFilter(comp, obj);
		    break;

		case V_PAGE:
		    setPage(comp, obj);
		    break;

		case V_PAINT:
		    setPaint(comp, obj);
		    break;

		case V_PAINTLABELS:
		    setPaintLabels(comp, obj);
		    break;

		case V_PAINTTICKS:
		    setPaintTicks(comp, obj);
		    break;

		case V_PAINTTRACK:
		    setPaintTrack(comp, obj);
		    break;

		case V_PANANDZOOM:
		    setPanAndZoom(comp, obj);
		    break;

		case V_PARENT:
		    setParent(comp, obj);
		    break;

		case V_POPUP:
		    setPopup(comp, obj);
		    break;

		case V_PREFERREDSIZE:
		    setPreferredSize(peerscroller != null ? peerscroller : comp, obj);
		    break;

		case V_PRESSED:
		    setPressed(comp, obj);
		    break;

		case V_PROTOTYPEVALUE:
		    setPrototypeValue(comp, obj);
		    break;

		case V_PROMPT:
		    setPrompt(comp, obj);
		    break;

		case V_QUIET:
		    setQuiet(comp, obj);
		    break;

		case V_REORDER:
		    setReorder(comp, obj);
		    break;

		case V_REQUESTFOCUS:
		    setRequestFocus(comp, obj);
		    break;

		case V_REQUESTFOCUSENABLED:
		    setRequestFocusEnabled(comp, obj);
		    break;

		case V_RESET:
		    setReset(comp, obj);
		    break;

		case V_RESIZABLE:
		    setResizable(comp, obj);
		    break;

		case V_RESIZE:
		    setResize(comp, obj);
		    break;

		case V_RESIZEMODE:
		    setResizeMode(comp, obj);
		    break;

		case V_RESIZEWEIGHT:
		    setResizeWeight(comp, obj);
		    break;

		case V_ROLLOVER:
		    setRollover(comp, obj);
		    break;

		case V_ROLLOVERENABLED:
		    setRolloverEnabled(comp, obj);
		    break;

		case V_ROOTHANDLE:
		    setRoothandle(comp, obj);
		    break;

		case V_ROWHEIGHTADJUSTMENT:
		    setRowHeightAdjustment(comp, obj);
		    break;

		case V_ROWS:
		    setRows(comp, obj);
		    break;

		case V_SAVEGRAPHICS:
		    setSaveGraphics(comp, obj);
		    break;

		case V_SAVELINES:
		    setSaveLines(comp, obj);
		    break;

		case V_SCREEN:
		    setScreen(comp, obj);
		    break;

		case V_SCROLL:
		    setScroll(peerscroller != null ? peerscroller : comp, obj);
		    break;

		case V_SCROLLSONEXPAND:
		    setScrollsOnExpand(comp, obj);
		    break;

		case V_SELECTED:
		    setSelected(comp, obj);
		    break;

		case V_SELECTEDENDS:
		    setSelectedEnds(comp, obj);
		    break;

		case V_SELECTIONBACKGROUND:
		    setSelectionBackground(comp, obj);
		    break;

		case V_SELECTIONFOREGROUND:
		    setSelectionForeground(comp, obj);
		    break;

		case V_SHAPE:
		    setShape(comp, obj);
		    break;

		case V_SIZE:
		    setSize(comp, obj);
		    break;

		case V_SIZECONTROL:
		    setSizeControl(peerscroller != null ? peerscroller : comp, obj);
		    break;

		case V_SNAPTOTICKS:
		    setSnapToTicks(comp, obj);
		    break;

		case V_STATE:
		    setState(comp, obj);
		    break;

		case V_SYNCVIEWPORT:
		    setSyncViewport(comp, obj);
		    break;

		case V_TAG:
		    setTag(comp, obj);
		    break;

		case V_TEXT:
		    setText(comp, obj);
		    break;

		case V_TEXTMODE:
		    setTextMode(comp, obj);
		    break;

		case V_TEXTPOSITION:
		    setTextPosition(comp, obj);
		    break;

		case V_TEXTWRAP:
		    setTextWrap(comp, obj);
		    break;

		case V_TITLE:
		    setTitle(comp, obj);
		    break;

		case V_TOOLTIPS:
		    setTooltips(comp, obj);
		    break;

		case V_TOOLTIPTEXT:
		case V_TOOLTIP:		// obsolete - for backward compatibility
		    setToolTipText(comp, obj);
		    break;

		case V_TOP:
		    setTop(comp, obj);
		    break;

		case V_TRACKFOCUS:
		    setTrackFocus(comp, obj);
		    break;

		case V_TRANSFERHANDLER:
		    setTransferHandler(comp, obj);
		    break;

		case V_TYPES:
		    setTypes(comp, obj);
		    break;

		case V_UNITINCREMENT:
		    setUnitIncrement(comp, obj);
		    break;

		case V_USEEDITHIGHLIGHT:
		    setUseEditHighlight(comp, obj);
		    break;

		case V_VALIDATE:
		    setValidate(comp, obj);
		    break;

		case V_VALIDATOR:
		    setValidator(comp, obj);
		    break;

		case V_VALUE:
		    setValue(comp, obj);
		    break;

		case V_VALUES:
		    setValues(comp, obj);
		    break;

		case V_VISIBLE:
		    //setVisible(comp, obj);
		    setVisible(peerscroller != null ? peerscroller : comp, obj);
		    break;

		case V_VISIBLEAMOUNT:
		    setVisibleAmount(comp, obj);
		    break;
	    }
	}

	return(obj);
    }


    private void
    handleSetVisible(boolean state) {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof Component) {
	    if (peerscroller != null)
		peerscroller.setVisible(state);
	    ((Component)comp).setVisible(state);
	}

    }


    private synchronized boolean
    isDisposable() {

	boolean  result = disposable;

	disposable = false;
	return(result);
    }


    private boolean
    isModalDialog(Object comp) {

	return(comp instanceof Dialog && ((Dialog)comp).isModal());
    }


    private boolean
    isPopupMenu(Object comp) {

	return(comp instanceof JPopupMenu);
    }


    private boolean
    isPopupOwner(Object comp) {

	//
	// Currently accept all Components except for obvious exceptions,
	// like another JPopupMenu. Actually, we haven't tested to see
	// what happens if popupmenus are allowed??
	//

	return(comp instanceof Component && !(comp instanceof JPopupMenu || comp instanceof PopupMenu));
    }


    private JDialog
    newJDialog(YoixObject obj, boolean filedialog) {

	GraphicsConfiguration  gc;
	JDialog                dialog = null;
	Object                 parent;

	//
	// Old version complained with a TYPECHECK error if obj.notNull()
	// was true and parent was null. Changed was made on 11/3/03, but
	// decided not to simplify the code even more because we're very
	// close to a release.
	//

	gc = getGraphicsConfigurationFromScreen();

	if (obj.notNull()) {
	    if ((parent = obj.getManagedObject()) != null) {
		if (parent instanceof Frame) {
		    if (filedialog)
			dialog = (JDialog)(new YoixSwingJFileDialog(data, this, (Frame)parent, gc));
		    else dialog = (JDialog)(new YoixSwingJDialog(data, this, (Frame)parent, gc));
		} else if (parent instanceof Dialog) {
		    if (filedialog)
			dialog = (JDialog)(new YoixSwingJFileDialog(data, this, (Dialog)parent, gc));
		    else dialog = (JDialog)(new YoixSwingJDialog(data, this, (Dialog)parent, gc));
		} else {
		    if (filedialog)
			dialog = (JDialog)(new YoixSwingJFileDialog(data, this, (Frame)null, gc));
		    else dialog = (JDialog)(new YoixSwingJDialog(data, this, (Frame)null, gc));
		}
	    } else {
		if (filedialog)
		    dialog = (JDialog)(new YoixSwingJFileDialog(data, this, (Frame)null, gc));
		else dialog = (JDialog)(new YoixSwingJDialog(data, this, (Frame)null, gc));
	    }
	} else {
	    if (filedialog)
		dialog = (JDialog)(new YoixSwingJFileDialog(data, this, (Frame)null, gc));
	    else dialog = (JDialog)(new YoixSwingJDialog(data, this, (Frame)null, gc));
	}

	return(dialog);
    }


    private JWindow
    newJWindow(YoixObject obj) {

	GraphicsConfiguration  gc;
	JWindow                window = null;
	Object                 parent;

	//
	// Old version complained with a TYPECHECK error if obj.notNull()
	// was true and parent was null. Changed was made on 11/3/03, but
	// decided not to simplify the code even more because we're very
	// close to a release.
	//

	gc = getGraphicsConfigurationFromScreen();

	if (obj.notNull()) {
	    if ((parent = obj.getManagedObject()) != null) {
		if (parent instanceof Frame)
		    window = (JWindow)(new YoixSwingJWindow(data, this, (Frame)parent, gc));
		else if (parent instanceof Window)
		    window = (JWindow)(new YoixSwingJWindow(data, this, (Window)parent, gc));
		else window = (JWindow)(new YoixSwingJWindow(data, this, (Frame)null, gc));
	    } else window = (JWindow)(new YoixSwingJWindow(data, this, gc));
	} else window = (JWindow)(new YoixSwingJWindow(data, this, gc));

	return(window);
    }


    private void
    pack() {

	Object  comp;
	Object  lock;

	//
	// Calling pack() outside the event thread can cause problems because
	// it calls validate(), which in turn grabs the window's AWTTreeLock.
	// 

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof YoixInterfaceWindow) {
	    if (EventQueue.isDispatchThread() == false) {
		try {
		    lock = new Object();
		    synchronized(lock) {
			EventQueue.invokeLater(
			    new YoixAWTInvocationEvent(
				this,
				new Object[] {new Integer(RUN_PACK), lock}
			    )
			);
			lock.wait();
		    }
		}
		catch(InterruptedException e) {}
	    } else handlePack();
	}
    }


    private boolean
    pickEnabled(YoixObject obj) {

	boolean  state;
	Object   comp;

	comp = this.peer;		// snapshot - just to be safe

	if (obj.isNull()) {
	    if ((obj = findClosestValue(N_ENABLED)) == null || obj.isNull())
		state = true;
	    else state = obj.booleanValue();
	} else state = obj.booleanValue();

	return(state);
    }


    private int
    pickLayer(YoixObject obj) {

	int  layer;
	int  min;
	int  max;

	if (obj != null && obj.notNull()) {
	    if (obj.isNumber()) {
		min = JLayeredPane.FRAME_CONTENT_LAYER.intValue();
		max = JLayeredPane.DRAG_LAYER.intValue() - 1;
		layer = Math.max(min, Math.min(obj.intValue(), max));
	    } else layer = JLayeredPane.DEFAULT_LAYER.intValue();
	} else layer = JLayeredPane.DEFAULT_LAYER.intValue();

	return(layer);
    }


    private boolean
    pickOpaque(YoixObject obj) {

	boolean  state;
	Object   comp;

	//
	// We recently (10/17/06) added special treatment for JTabbedPanes
	// because the
	//
	//    TabbedPane.tabAreaBackground
	//
	// UIManager property apparently is the only way we can control the
	// color of that part of a JTabbedPane if it's not opaque, but it's
	// an approach that affects all JTabbedPanes. Anyway, picking false
	// as the setting for JTabbedPanes that have NULL assigned to their
	// opaque field seems reasonable and means that tabAreaBackground
	// part of the JTabbedPane looks better.
	//

	comp = this.peer;		// snapshot - just to be safe

	if (obj.isNull()) {
	    if ((obj = findClosestValue(N_OPAQUE)) == null || obj.isNull()) {
		if (isContainer(comp)) {
		    if (comp instanceof JPanel)
			state = false;
		    else if (comp instanceof JTabbedPane)	// added on 10/17/06
			state = false;
		    else state = true;
		} else state = true;
	    } else state = obj.booleanValue();
	} else state = obj.booleanValue();

	return(state);
    }


    private void
    removeAdjustmentListener(JScrollPane scroller) {

	JScrollBar  scrollbar;

	if (scroller != null) {
	    if ((scrollbar = scroller.getHorizontalScrollBar()) != null)
		scrollbar.removeAdjustmentListener(this);
	    if ((scrollbar = scroller.getVerticalScrollBar()) != null)
		scrollbar.removeAdjustmentListener(this);
	}
    }


    private void
    setAccelerator(Object comp, YoixObject obj) {

	KeyStroke  keystroke = null;

	//
	// A string value should fit the description provided in comments
	// that preceed Java's
	//
	//	KeyStroke.getKeyStroke(String s);
	//
	// method. A value that's an int is treated as a Unicode charater.
	//

	if (comp instanceof JMenuItem) {
	    if (obj.isString() || obj.isNull()) {
		if (obj.notNull()) {
		    try {
			keystroke = KeyStroke.getKeyStroke(obj.stringValue());
		    }
		    catch(IllegalArgumentException e) {
			keystroke = null;
		    }
		} else keystroke = null;
	    } else if (obj.isInteger())
		keystroke = KeyStroke.getKeyStroke((char)obj.intValue());
	    else VM.abort(TYPECHECK, N_ACCELERATOR);
	    ((JMenuItem)comp).setAccelerator(keystroke);
	}
    }


    private void
    setAdjusting(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    ((DefaultBoundedRangeModel)(((YoixSwingJSlider)comp).getModel())).setValueIsAdjusting(obj.booleanValue());
    }


    private void
    setAfterPan(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJCanvas)
	    ((YoixSwingJCanvas)comp).setAfterPan(obj);
    }


    private void
    setAfterSelect(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAfterSelect(obj);
    }


    private void
    setAfterZoom(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJCanvas)
	    ((YoixSwingJCanvas)comp).setAfterZoom(obj);
    }


    private void
    setAlignment(Object comp, YoixObject obj) {

	//
	// Eventually consider setting vertical alignment for components,
	// like JLabel and AbstractButton, that support it. Decided not
	// to rush the change into this release. Also, menuitems can be
	// screwy (e.g., if menu has checkboxes in it, then width may not
	// be adjusted to subtract the width reserved for the checkbox
	// portion).
	//

	if (comp instanceof AbstractButton)
	    ((AbstractButton)comp).setHorizontalAlignment(jfcInt("SwingHorizontalAlignment", obj.intValue()));
	else if (comp instanceof YoixSwingJComboBox)
	    ((YoixSwingJComboBox)comp).setHorizontalAlignment(jfcInt("SwingHorizontalAlignment", obj.intValue()));
	else if (comp instanceof JLabel)
	    ((JLabel)comp).setHorizontalAlignment(jfcInt("SwingHorizontalAlignment", obj.intValue()));
	else if (comp instanceof JTextField)
	    ((JTextField)comp).setHorizontalAlignment(jfcInt("SwingHorizontalAlignment", obj.intValue()));
	else if (comp instanceof YoixSwingJTextPane)
	    ((YoixSwingJTextPane)comp).setHorizontalAlignment(jfcInt("SwingHorizontalAlignment", obj.intValue()));
	else if (comp instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)comp).setAlignment(jfcInt("TextCanvasAlignment", obj.intValue()));
	else if (comp instanceof YoixSwingJTabbedPane)
	    ((YoixSwingJTabbedPane)comp).setAlignment(jfcInt("JTabbedPaneAlignment", obj.intValue()));
    }


    private void
    setAllowEdit(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAllowEdit(obj);
    }


    private void
    setAltAlignment(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAltAlignment(obj);
	else if (comp instanceof JLabel)
	    ((JLabel)comp).setVerticalAlignment(jfcInt("SwingVerticalAlignment", obj.intValue()));
    }


    private void
    setAltBackground(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAltColor(obj, true);
    }


    private void
    setAltFont(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAltFont(obj);
    }


    private void
    setAltForeground(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAltColor(obj, false);
    }


    private void
    setAltGridColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAltGridColor(obj);
    }


    private void
    setAltToolTipText(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAltToolTipText(obj);
    }


    private void
    setAnchor(Object comp, YoixObject obj) {

	//
	// This didn't compile everywhere - scrollToReference() was
	// protected until 1.4.1, so we added a YoixSwingJTextPane
	// class that extends JTextPane. Only needed until we pick
	// 1.4.1 (or later) as the oldest supported release.
	//

	if (comp instanceof YoixSwingJTextPane)
	    ((YoixSwingJTextPane)comp).setAnchor(obj.stringValue());
	else if (comp instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)comp).setAnchor(obj.intValue());
    }


    private void
    setApproveButtonMnemonic(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceFileChooser)
	    ((YoixInterfaceFileChooser)comp).setApproveButtonMnemonic(obj.intValue());
    }


    private void
    setApproveButtonText(Object comp, YoixObject obj) {

	String  text;

	if (comp instanceof YoixInterfaceFileChooser) {
	    if (obj.notNull()) {
		text = obj.stringValue();
		if (text.length() == 0)
		    text = null;
	    } else text = null;
	    ((YoixInterfaceFileChooser)comp).setApproveButtonText(text);
	}
    }


    private void
    setApproveButtonToolTipText(Object comp, YoixObject obj) {

	String  text;

	if (comp instanceof YoixInterfaceFileChooser) {
	    if (obj.notNull()) {
		text = obj.stringValue();
		if (text.length() == 0)
		    text = null;
	    } else text = null;
	    ((YoixInterfaceFileChooser)comp).setApproveButtonToolTipText(text);
	}
    }


    private void
    setArmed(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    ((AbstractButton)comp).getModel().setArmed(obj.booleanValue());
    }


    private void
    setBackground(Object comp, JScrollPane scroller, YoixObject obj) {

	Color  color;

	if (comp instanceof Component) {
	    super.setBackground(comp, obj);
	    color = ((Component)comp).getBackground();
	    if (scroller != null)
		scroller.getViewport().setBackground(color);
	    syncTabProperty((Component)comp, N_BACKGROUND, color);
	}
    }


    private void
    setBase(Object comp, YoixObject obj) {

	Document  doc;
	URL       url;

	if (comp instanceof JTextComponent) {
	    doc = ((JTextComponent)comp).getDocument();
	    if (doc instanceof HTMLDocument) {
		if (obj.notNull()) {
		    try {
			url = new URL(obj.stringValue());
		    }
		    catch(MalformedURLException e) {
			url = null;
		    }
		} else url = null;
		((HTMLDocument)doc).setBase(url);
	    }
	}
    }


    private void
    setBlockIncrement(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJScrollBar)
	    ((YoixSwingJScrollBar)comp).setBlockIncrement(obj.intValue());
	else if (comp instanceof JScrollBar)
	    ((JScrollBar)comp).setBlockIncrement(Math.max(obj.intValue(), 1));
    }


    private void
    setBorder(Object comp, YoixObject obj) {

	//
	// Recently (5/30/05) made some changes here that were accompanied
	// by changes to YoixMakeScreen.javaBorder(). Be suspicious of the
	// new code if any borders misbehave.
	//

	if (comp instanceof YoixSwingJCanvas) {
	    if (obj.isNumber() || obj.isInsets() || obj.isNull()) {
		((YoixSwingJCanvas)comp).setBorderInsets(YoixMakeScreen.javaInsets(obj));
		if (nullborder != null)
		    ((YoixSwingJCanvas)comp).setBorder(nullborder);
	    } else ((YoixSwingJCanvas)comp).setBorder(YoixMakeScreen.javaBorder(obj, null, (JComponent)comp));
	} else if (comp instanceof JComponent)
	    ((JComponent)comp).setBorder(YoixMakeScreen.javaBorder(obj, nullborder, (JComponent)comp));
	else if (comp instanceof YoixSwingJDialog)
	    ((YoixSwingJDialog)comp).setBorder(YoixMakeScreen.javaBorder(obj));
	else if (comp instanceof YoixSwingJFrame)
	    ((YoixSwingJFrame)comp).setBorder(YoixMakeScreen.javaBorder(obj));
	else if (comp instanceof YoixSwingJWindow)
	    ((YoixSwingJWindow)comp).setBorder(YoixMakeScreen.javaBorder(obj));
    }


    private void
    setBorderColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setBorderColor(obj);
	else if (comp instanceof YoixSwingJCanvas)
	    ((YoixSwingJCanvas)comp).setBorderColor(YoixMake.javaColor(obj));
    }


    private void
    setButtons(Object comp, YoixObject obj) {
	if (comp instanceof JFileChooser) {
	    ((JFileChooser)comp).setControlButtonsAreShown(obj.booleanValue());
	    ((JFileChooser)comp).validate();
	}
    }


    private void
    setCaret(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	boolean      editable;
	int          pos;

	//
	// Decided to always let the JTextComponent set the position
	// if it's an editable component, but it's a precaution that
	// probably could be handled differently - later.
	//

	if (comp instanceof JTextComponent) {
	    pos = obj.intValue();
	    editable = ((JTextComponent)comp).isEditable();
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (editable || highlighter instanceof YoixSwingHighlighter == false) {
		try {
		    ((JTextComponent)comp).setCaretPosition(pos < 0 ? 0 : pos);
		}
		catch(RuntimeException e) {
		    try {
			pos = ((JTextComponent)comp).getText().length();
			((JTextComponent)comp).setCaretPosition(pos);
		    }
		    catch(RuntimeException ee) {}
		}
	    } else ((YoixSwingHighlighter)highlighter).setCaretPosition(pos);
	}
    }


    private synchronized void
    setCaretAlpha(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	double       alpha;
	Color        caretcolor;
	Color        color;

	if (comp instanceof JTextComponent) {
	    alpha = obj.doubleValue();
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (highlighter instanceof YoixSwingHighlighter)
		caretcolor = ((YoixSwingHighlighter)highlighter).getCaretColor();
	    else caretcolor = ((JTextComponent)comp).getCaretColor();
	    if (caretcolor != null) {
		color = new Color(
		    (float)(caretcolor.getRed()/255.0),
		    (float)(caretcolor.getGreen()/255.0),
		    (float)(caretcolor.getBlue()/255.0),
		    (float)Math.max(0.0, Math.min(1.0, alpha))
		);
	    } else color = new Color(0, 0, 0, (float)alpha);	// wrong!!
	    if (color.equals(caretcolor) == false) {
		if (highlighter instanceof YoixSwingHighlighter)
		    ((YoixSwingHighlighter)highlighter).setCaretColor(color);
		else ((JTextComponent)comp).setCaretColor(color);
	    }
	}
    }


    private synchronized void
    setCaretColor(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	double       alpha;
	Color        currentcolor;
	Color        color;

	if (comp instanceof JTextComponent) {
	    alpha = data.getDouble(N_CARETALPHA, 1.0);
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (highlighter instanceof YoixSwingHighlighter) {
		currentcolor = ((YoixSwingHighlighter)highlighter).getCaretColor();
		if (obj.notNull())
		    color = YoixMake.javaColor(obj);
		else color = ((YoixSwingHighlighter)highlighter).getDefaultCaretColor();
	    } else {
		currentcolor = ((JTextComponent)comp).getCaretColor();
		color = obj.notNull() ? YoixMake.javaColor(obj) : Color.black;
	    }
	    color = new Color(
		(float)(color.getRed()/255.0),
		(float)(color.getGreen()/255.0),
		(float)(color.getBlue()/255.0),
		(float)Math.max(0.0, Math.min(1.0, alpha))
	    );
	    if (color.equals(currentcolor) == false) {
		if (highlighter instanceof YoixSwingHighlighter)
		    ((YoixSwingHighlighter)highlighter).setCaretColor(color);
		else ((JTextComponent)comp).setCaretColor(color);
	    }
	}
    }


    private synchronized void
    setCaretModel(Object comp, YoixObject obj) {

	if (comp instanceof JTextComponent) {
	    if (comp instanceof YoixSwingJTextArea)
		((YoixSwingJTextArea)comp).setCaretModel(obj.intValue());
	    else if (comp instanceof YoixSwingJTextField)
		((YoixSwingJTextField)comp).setCaretModel(obj.intValue());
	    else if (comp instanceof YoixSwingJTextPane)
		((YoixSwingJTextPane)comp).setCaretModel(obj.intValue());
	}
    }


    private synchronized void
    setCaretOwnerColor(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	Color        currentcolor;
	Color        color;

	if (comp instanceof JTextComponent) {
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (highlighter instanceof YoixSwingHighlighter) {
		currentcolor = ((YoixSwingHighlighter)highlighter).getCaretOwnerColor();
		if (obj.notNull()) {
		    color = YoixMake.javaColor(obj);
		    if (color.equals(currentcolor) == false)
			((YoixSwingHighlighter)highlighter).setCaretOwnerColor(color);
		} else if (currentcolor != null)
		    ((YoixSwingHighlighter)highlighter).setCaretOwnerColor(null);
	    }
	}
    }


    private void
    setCellColors(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setCellColors(obj);
    }


    private void
    setClickCount(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setClickCount(obj.intValue());
    }


    private void
    setClosable(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    ((JInternalFrame)comp).setClosable(obj.booleanValue());
    }


    private void
    setClosed(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame) {
	    try {
		((JInternalFrame)comp).setClosed(obj.booleanValue());
	    }
	    catch(PropertyVetoException e) {}
	}
    }


    private void
    setClosedIcon(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setClosedIcon(obj);
    }


    private void
    setColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJColorChooser)
	    ((YoixSwingJColorChooser)comp).setColor(YoixMake.javaColor(obj));
    }


    private void
    setColumns(Object comp, YoixObject obj) {

	if (comp instanceof JTextField)
	    ((JTextField)comp).setColumns(Math.max(obj.intValue(), 0));
	else if (comp instanceof JTextArea)
	    ((JTextArea)comp).setColumns(Math.max(obj.intValue(), 0));
	else if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setColumns(obj);
	else if (comp instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)comp).setColumns(Math.max(obj.intValue(), 0));
	else if (comp instanceof YoixSwingJList)
	    ((YoixSwingJList)comp).setVisibleColumnCount(Math.max(obj.intValue(), 0));
    }


    private void
    setCommand(Object comp, YoixObject obj) {

	YoixObject  yobj;
	String      cmds[];
	int         idx;
	int         jdx;
	int         len;

	//
	// We accept a JCheckBox even though an action listener is not
	// currently allowed because getSelected() may use the value
	// when a JCheckBox is in a ButtonGroup.
	//

	if (comp instanceof AbstractButton) {
	    if (obj.isNull())
		((AbstractButton)comp).setActionCommand(null);	// resets to default
	    else ((AbstractButton)comp).setActionCommand(obj.stringValue());
	} else if (comp instanceof JTextField) {
	    if (obj.isNull())
		((JTextField)comp).setActionCommand(null);	// resets to default
	    else ((JTextField)comp).setActionCommand(obj.stringValue());
	} else if (comp instanceof YoixSwingJComboBox) {
	    if (obj.isNull())
		((YoixSwingJComboBox)comp).setActionCommands(null, null);	// resets to default
	    else if (obj.isString())
		((YoixSwingJComboBox)comp).setActionCommand(obj.stringValue());
	    else if (obj.isArray()) {
		if (obj.sizeof() < 3) {
		    len = obj.length();
		    cmds = ((YoixSwingJComboBox)comp).getActionCommands();
		    for (idx = obj.offset(), jdx = 0; idx < len; idx++, jdx++) {
			yobj = obj.get(idx, false);
			if (yobj.isNull())
			    cmds[jdx] = null;
			else if (yobj.isString())
			    cmds[jdx] = yobj.stringValue();
			else VM.abort(BADVALUE, N_COMMAND, idx);
		    }
		    ((YoixSwingJComboBox)comp).setActionCommands(cmds);
		} else VM.abort(BADVALUE, N_COMMAND);
	    } else VM.abort(TYPECHECK, N_COMMAND);
	}
    }


    private void
    setContinuousLayout(Object comp, YoixObject obj) {

	if (comp instanceof JSplitPane)
	    ((JSplitPane)comp).setContinuousLayout(obj.booleanValue());
    }


    private void
    setDesktop(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJInternalFrame) {
	    if (obj.isNull() || obj.isJDesktopPane()) {
		((YoixSwingJInternalFrame)comp).setDesktop(
		    (JDesktopPane)obj.getManagedObject(),
		    pickLayer(data.getObject(N_LAYER))
		);
	    } else VM.abort(TYPECHECK, N_DESKTOP);
	}
    }


    private void
    setDirectory(Object comp, YoixObject obj) {

	File  dir;

	if (comp instanceof YoixInterfaceFileChooser) {
	    if (obj.notNull()) {
		dir = new File(YoixMisc.toLocalPath(obj.stringValue()));
		((YoixInterfaceFileChooser)comp).setCurrentDirectory(dir);
	    } else if (initialized) 
		((YoixInterfaceFileChooser)comp).setCurrentDirectory(null);
	}
    }


    private void
    setDispose(Object comp, YoixObject obj) {

	if (isWindow()) {
	    if (obj.booleanValue())
		dispose(false);
	}
    }


    private void
    setDividerLocation(Object comp, YoixObject obj) {

	double  value;

	//
	// We now take a number between 0 and 1 inclusive as a fraction
	// of the available distance, negative values mean use the top
	// left components preferred size, and anything else is assumed
	// to be distance (in the default coordinate systems). The only
	// value that we have trouble with is 1 (i.e., exactly 1 point)
	// which just doesn't seem like a big deal. We still support the
	// String mechanism, but it's probably no longer docuemented.
	//

	if (comp instanceof JSplitPane) {
	    if (obj.notNull()) {
		if (obj.isNumber()) {
		    if ((value = obj.doubleValue()) >= 0) {
			if (value > 1) {
			    value = YoixMakeScreen.javaDistance(value);
			    ((JSplitPane)comp).setDividerLocation((int)value);
			} else ((JSplitPane)comp).setDividerLocation(value);
		    } else ((JSplitPane)comp).setDividerLocation(-1);
		} else if (obj.isString()) {
		    //
		    // Still supported, for now anyway, but it probably
		    // is no longer documented.
		    //
		    value = YoixMake.javaDouble(obj.stringValue(), 0)/100.0;
		    if (value < 0)
			value = 0;
		    else if (value > 1)
			value = 1;
		    ((JSplitPane)comp).setDividerLocation(value);
		} else VM.abort(TYPECHECK, N_DIVIDERLOCATION);
	    } else ((JSplitPane)comp).setDividerLocation(-1);
	}
    }


    private void
    setDividerLocked(Object comp, YoixObject obj) {

	if (comp instanceof JSplitPane) {
	    if (comp instanceof YoixSwingJSplitPane)
		((YoixSwingJSplitPane)comp).setDividerLocked(obj.booleanValue());
	}
    }


    private void
    setDividerSize(Object comp, YoixObject obj) {

	double  value;
	Point   point;
	int     size;

	if (comp instanceof JSplitPane) {
	    value = obj.doubleValue();
	    if (value >= 0) {
		point = YoixMakeScreen.javaPoint(value, value);
		if (((JSplitPane)comp).getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
		    size = point.x;
		else size = point.y;
		((JSplitPane)comp).setDividerSize(size);
	    }
	}
    }


    private void
    setDoubleBuffered(Object comp, YoixObject obj) {

	//
	// Currently makes no changes if obj is NULL, which means each
	// component normally gets their default setting.
	//

	if ((obj = VM.getDoubleBuffered(obj)) != null) {
	    if (obj.notNull()) {
		if (comp instanceof YoixSwingJDialog)
		    ((YoixSwingJDialog)comp).setDoubleBuffered(obj.booleanValue());
		else if (comp instanceof YoixSwingJFrame)
		    ((YoixSwingJFrame)comp).setDoubleBuffered(obj.booleanValue());
		else if (comp instanceof YoixSwingJWindow)
		    ((YoixSwingJWindow)comp).setDoubleBuffered(obj.booleanValue());
		else if (comp instanceof YoixSwingJPanel)
		    ((YoixSwingJPanel)comp).setDoubleBuffered(obj.booleanValue());
		else if (comp instanceof JComponent)
		    ((JComponent)comp).setDoubleBuffered(obj.booleanValue());
	    }
	}
    }


    private void
    setDragEnabled(Object comp, YoixObject obj) {

	if (comp instanceof JComponent)
	    YoixReflect.invoke(comp, "setDragEnabled", obj.booleanValue());
    }


    private void
    setEcho(Object comp, YoixObject obj) {

	if (comp instanceof JPasswordField) {
	    ((JPasswordField)comp).setEchoChar((char)obj.intValue());
	    ((JPasswordField)comp).repaint();	// needed to update display
	}
    }


    private void
    setEdit(Object comp, YoixObject obj) {

	FocusListener  listeners[];
	boolean        editable;
	Object         editor;
	int            n;

	if (comp instanceof JTextComponent)
	    ((JTextComponent)comp).setEditable(obj.booleanValue());
	else if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setEditable(obj.booleanValue());
	else if (comp instanceof JComboBox) {
	    editor = getJComboBoxTextEditor(comp);
	    editable = ((JComboBox)comp).isEditable();
	    ((JComboBox)comp).setEditable(obj.booleanValue());
	    if (editor != null && editable != ((JComboBox)comp).isEditable()) {
		if (editable) {
		    listeners = ((JTextComponent)editor).getFocusListeners();
		    for (n = 0; n < listeners.length; n++) {
			if (listeners[n] == this) {
			    ((JTextComponent)editor).removeFocusListener(this);
			    ((JComboBox)comp).addFocusListener(this);
			    break;
			}
		    }
		} else {
		    listeners = ((JComboBox)comp).getFocusListeners();
		    for (n = 0; n < listeners.length; n++) {
			if (listeners[n] == this) {
			    ((JComboBox)comp).removeFocusListener(this);
			    ((JTextComponent)editor).addFocusListener(this);
			    break;
			}
		    }
		}
	    }
	} else if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setEditInfo(obj);
	else if (comp instanceof YoixSwingJTextTerm)
	    ((YoixSwingJTextTerm)comp).setEditable(obj.booleanValue());
    }


    private void
    setEditBackground(Object comp, YoixObject obj) {

	Color        color;

	if (comp instanceof YoixSwingJTable) {
	    if (obj.isNull())
		color = null;
	    else color = YoixMake.javaColor(obj);
	    ((YoixSwingJTable)comp).setEditBackground(color);
	}
    }


    private void
    setEditForeground(Object comp, YoixObject obj) {

	Color        color;

	if (comp instanceof YoixSwingJTable) {
	    if (obj.isNull())
		color = null;
	    else color = YoixMake.javaColor(obj);
	    ((YoixSwingJTable)comp).setEditForeground(color);
	}
    }


    private void
    setEnabled(Object comp, YoixObject obj) {

	YoixBodyComponent  body;
	JScrollPane        scroller;
	YoixObject         components;
	YoixObject         container;
	YoixObject         child;
	boolean            state;
	int                m;
	int                n;

	//
	// Code that handles peerscroller was added to make sure scrolling
	// is also disabled, which really depends on the setEnabled method
	// defined in YoixSwingJScrollPane (8/10/07).
	//

	if (comp instanceof Component) {
	    if (obj.isNumber() || obj.isNull()) {
		state = pickEnabled(obj);
		((Component)comp).setEnabled(state);
		if ((scroller = peerscroller) != null)
		    scroller.setEnabled(state);
		syncTabProperty((Component)comp, N_ENABLED, state);
		if (isContainer(comp) || comp instanceof YoixInterfaceWindow) {
		    components = data.getObject(N_COMPONENTS);
		    for (n = 0; n < components.length(); n++) {
			if (components.defined(n)) {
			    child = components.getObject(n);
			    if (((YoixBodyComponent)child.body()) != this) {
				comp = child.getManagedObject();
				if (comp instanceof Component) {
				    body = (YoixBodyComponent)child.body();
				    obj = body.data.getObject(N_ENABLED);
				    if (obj != null && obj.notNull()) {
					if (((YoixBodyComponent)(child.body())).isContainer()) {
					    container = child.getObject(N_COMPONENTS);
					    for (m = 1; m < container.length(); m++)
						n += container.defined(m) ? 1 : 0;
					}
				    } else ((Component)comp).setEnabled(state);
				}
			    }
			}
		    }
		}
	    } else VM.abort(TYPECHECK, N_ENABLED);
	}
    }


    private void
    setExpandsSelectedNodes(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setExpandsSelectedPaths(obj.booleanValue());
    }


    private void
    setExtent(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setExtent(obj.intValue());
    }


    private void
    setFile(Object comp, YoixObject obj) {

	boolean  mode;
	String   names[];
	File     files[];
	File     file;
	int      n;

	//
	// The initialized test pretty much assumes buildComponent() sets
	// the directory before the file. Probably should add an identical
	// test to setDirectory(), but it's not that important and would mean
	// lots of special case testing, so we'll leave things be for now.
	//

	if (comp instanceof YoixInterfaceFileChooser) {
	    mode = ((YoixInterfaceFileChooser)comp).isMultiSelectionEnabled();
	    if (obj.isNull() || (mode && obj.isArray()) || obj.isString()) {
		if (obj.notNull()) {
		    if (mode) {
			if (obj.isString()) {
			    files = new File[1];
			    files[0] = new File(YoixMisc.toLocalPath(obj.stringValue()));
			} else {
			    names = YoixMake.javaStringArray(obj);
			    files = new File[names.length];
			    for (n = 0; n < names.length; n++) {
				if (names[n] != null)
				    files[n] = new File(YoixMisc.toLocalPath(names[n]));
				else files[n] = null;
			    }
			}
			((YoixInterfaceFileChooser)comp).setSelectedFiles(files);
		    } else {
			file = new File(YoixMisc.toLocalPath(obj.stringValue()));
			((YoixInterfaceFileChooser)comp).setSelectedFile(file);
		    }
		} else if (initialized) {
		    ((YoixInterfaceFileChooser)comp).setSelectedFile(null);
		    // to clear selection
		    ((YoixInterfaceFileChooser)comp).setMultiSelectionEnabled(!mode);
		    ((YoixInterfaceFileChooser)comp).setMultiSelectionEnabled(mode);
		}
	    } else VM.abort(BADVALUE, N_FILE);
	}
    }


    private void
    setFileSelectionMode(Object comp, YoixObject obj) {

	int  mode;

	if (comp instanceof YoixInterfaceFileChooser) {
	    mode = obj.intValue()&0x03;
	    if (jfcExists("JFileChooserSelectionMode", mode))
		((YoixInterfaceFileChooser)comp).setFileSelectionMode(jfcInt("JFileChooserSelectionMode", mode));
	}
    }


    private void
    setFilter(Object comp, YoixObject obj) {

	FileFilter  filters[];
	String      description;
	int         n;

	if (comp instanceof YoixInterfaceFileChooser) {
	    if ((description = obj.stringValue()) != null) {
		if ((filters = ((YoixInterfaceFileChooser)comp).getChoosableFileFilters()) != null) {
		    for (n = 0; n < filters.length; n++) {
			if (description.equals(filters[n].getDescription())) {
			    ((YoixInterfaceFileChooser)comp).setFileFilter(filters[n]);
			    break;
			}
		    }
		}
	    }
	}
    }


    private void
    setFilters(Object comp, YoixObject obj) {

	YoixRERegexp  re;
	YoixObject    elem;
	boolean       allfilter;
	String        description;
	int           length;
	int           n;

	//
	// We explicitly remove the "all" filter so that it will always
	// be the last entry in the filter list.
	//

	if (comp instanceof YoixInterfaceFileChooser) {
	    ((YoixInterfaceFileChooser)comp).resetChoosableFileFilters();
	    ((YoixInterfaceFileChooser)comp).setAcceptAllFileFilterUsed(false);
	    if (obj.notNull()) {
		allfilter = false;
		length = obj.length();
		for (n = obj.offset(); n < length; n += 2) {
		    if ((elem = obj.getObject(n)) != null) {
			if (elem.isNull()) {
			    if ((n+1) == length)
				allfilter = true;
			    else if ((elem = obj.getObject(n+1)) != null) {
				if (elem.isNull())
				    allfilter = true;
				else VM.abort(BADVALUE, N_FILTERS, n+1);
			    } else VM.abort(UNDEFINED, N_FILTERS, n+1);
			} else if (elem.isString()) {
			    description = elem.stringValue();
			    if ((elem = obj.getObject(n+1)) != null) {
				if (elem.notNull()) {
				    re = null;
				    if (elem.isString())
					re = new YoixRERegexp(elem.stringValue(), SHELL_PATTERN);
				    else if (elem.isRegexp())
					re = (YoixRERegexp)((YoixBodyRegexp)elem.body()).getManagedObject();
				    else VM.abort(BADVALUE, N_FILTERS, n+1);
				    ((YoixInterfaceFileChooser)comp).addChoosableFileFilter(
					new YoixFileFilter(description, re)
				    );
				} else VM.abort(BADVALUE, N_FILTERS, n+1);
			    } else VM.abort(UNDEFINED, N_FILTERS, n+1);
			} else VM.abort(BADVALUE, N_FILTERS, n);
		    } else VM.abort(UNDEFINED, N_FILTERS, n);
		}
	    } else allfilter = true;
	    if (allfilter)
		((YoixInterfaceFileChooser)comp).setAcceptAllFileFilterUsed(true);
	}
    }


    private void
    setFloatable(Object comp, YoixObject obj) {

	if (comp instanceof JToolBar)
	    ((JToolBar)comp).setFloatable(obj.booleanValue());
    }


    private void
    setGlassPane(Object comp, YoixObject obj) {

	YoixObject  opaque;
	Object      body;
	Object      glasspane;

	if (comp instanceof YoixInterfaceWindow) {
	    if (obj.isComponent() || obj.isNull()) {
		glasspane = obj.getManagedObject();
		if (glasspane instanceof JComponent || glasspane == null) {
		    body = obj.body();
		    if (body instanceof YoixBodyComponentSwing) {
			opaque = ((YoixBodyComponentSwing)body).data.getObject(N_OPAQUE);
			if (opaque == null || opaque.isNull())
			    ((YoixBodyComponentSwing)body).setOpaque(false);
		    }
		    ((YoixInterfaceWindow)comp).setGlassPane((Component)glasspane);
		} else VM.abort(TYPECHECK, N_GLASSPANE);
	    } else VM.abort(TYPECHECK, N_GLASSPANE);
	}
    }


    private void
    setGridColor(Object comp, YoixObject obj) {

	Color  gcolor;

	if (comp instanceof YoixSwingJTable) {
	    if (obj.isNull())
		gcolor = null;
	    else gcolor = YoixMake.javaColor(obj);
	    ((YoixSwingJTable)comp).setGridColor(gcolor);
	}
    }


    private void
    setGridSize(Object comp, YoixObject obj) {

	Dimension  size;

	if (comp instanceof YoixSwingJTable) {
	    if (obj.isNull())
		size = null;
	    else size = YoixMakeScreen.javaDimension(obj);
	    ((YoixSwingJTable)comp).setIntercellSpacing(size);
	}
    }


    private void
    setGroup(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton) {
	    buttonGroupRemove(data.getObject(N_GROUP), getContext());
	    if (obj.notNull())
		buttonGroupAdd(obj, getContext());
	}
    }


    private void
    setHeaders(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setHeaders(obj);
    }


    private void
    setHeaderIcons(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setHeaderIcons(obj);
    }


    private void
    setHiddenFiles(Object comp, YoixObject obj) {

	if (comp instanceof YoixInterfaceFileChooser)
	    ((YoixInterfaceFileChooser)comp).setFileHidingEnabled(!obj.booleanValue());
    }


    private void
    setHighlightFlags(Object comp, YoixObject obj) {

	Highlighter  highlighter;

	if (comp instanceof JTextComponent) {
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (highlighter instanceof YoixSwingHighlighter)
		((YoixSwingHighlighter)highlighter).setFlags(obj.intValue());
	}
    }


    private void
    setIcon(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (comp instanceof AbstractButton)
		((AbstractButton)comp).setIcon(YoixMake.javaIcon(obj));
	    else if (comp instanceof JLabel)
		((JLabel)comp).setIcon(YoixMake.javaIcon(obj));
	    else if (comp instanceof JInternalFrame)
		((JInternalFrame)comp).setFrameIcon(YoixMake.javaIcon(obj));
	    syncTabProperty((Component)comp, N_ICON, obj);
	}
    }


    private void
    setIconifiable(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    ((JInternalFrame)comp).setIconifiable(obj.booleanValue());
    }


    private void
    setIconified(Object comp, YoixObject obj) {

	int  state;

	if (comp instanceof JInternalFrame) {
	    try {
		((JInternalFrame)comp).setIcon(obj.booleanValue());
	    }
	    catch(PropertyVetoException e) {}
	} else if (comp instanceof JFrame) {
	    state = ((JFrame)comp).getExtendedState();
	    if (obj.booleanValue())
		state |= JFrame.ICONIFIED;
	    else state &= ~JFrame.ICONIFIED;
	    ((JFrame)comp).setExtendedState(state);
	}
    }


    private void
    setIcons(Object comp, YoixObject obj) {

	AbstractButton  ab;
	YoixObject      yobj;

	if (comp instanceof AbstractButton) {
	    ab = (AbstractButton)comp;
	    if (obj.notNull() && obj.length() > 0) {
		if ((yobj = obj.getObject(N_DEFAULTICON, null)) != null) {
		    if (yobj.isImage() || yobj.isNull())
			ab.setIcon(YoixMake.javaIcon(yobj));
		    else VM.abort(TYPECHECK, N_DEFAULTICON);
		}
		if ((yobj = obj.getObject(N_DISABLEDICON, null)) != null) {
		    if (yobj.isImage() || yobj.isNull())
			ab.setDisabledIcon(YoixMake.javaIcon(yobj));
		    else VM.abort(TYPECHECK, N_DISABLEDICON);
		}
		if ((yobj = obj.getObject(N_DISABLEDSELECTEDICON, null)) != null) {
		    if (yobj.isImage() || yobj.isNull())
			ab.setDisabledSelectedIcon(YoixMake.javaIcon(yobj));
		    else VM.abort(TYPECHECK, N_DISABLEDSELECTEDICON);
		}
		if ((yobj = obj.getObject(N_PRESSEDICON, null)) != null) {
		    if (yobj.isImage() || yobj.isNull())
			ab.setPressedIcon(YoixMake.javaIcon(yobj));
		    else VM.abort(TYPECHECK, N_PRESSEDICON);
		}
		if ((yobj = obj.getObject(N_ROLLOVERICON, null)) != null) {
		    if (yobj.isImage() || yobj.isNull())
			ab.setRolloverIcon(YoixMake.javaIcon(yobj));
		    else VM.abort(TYPECHECK, N_ROLLOVERICON);
		}
		if ((yobj = obj.getObject(N_ROLLOVERSELECTEDICON, null)) != null) {
		    if (yobj.isImage() || yobj.isNull())
			ab.setRolloverSelectedIcon(YoixMake.javaIcon(yobj));
		    else VM.abort(TYPECHECK, N_ROLLOVERSELECTEDICON);
		}
		if ((yobj = obj.getObject(N_SELECTEDICON, null)) != null) {
		    if (yobj.isImage() || yobj.isNull())
			ab.setSelectedIcon(YoixMake.javaIcon(yobj));
		    else VM.abort(TYPECHECK, N_SELECTEDICON);
		}
	    } else if (obj.isNull()) {
		ab.setSelectedIcon(null);
		ab.setRolloverSelectedIcon(null);
		ab.setDisabledSelectedIcon(null);
		ab.setDisabledIcon(null);
		ab.setRolloverIcon(null);
		ab.setPressedIcon(null);
		ab.setIcon(null);
	    }
	}
    }


    private void
    setIndeterminate(Object comp, YoixObject obj) {

	if (comp instanceof JProgressBar)
	    ((JProgressBar)comp).setIndeterminate(obj.booleanValue());
    }


    private void
    setIndex(Object comp, YoixObject obj) {

	if (comp instanceof JList) {
	    if (EventQueue.isDispatchThread() == false) {
		EventQueue.invokeLater(
		    new YoixAWTInvocationEvent(
			this,
			new Object[] { new Integer(RUN_JLISTINDEX), comp, obj }
		    )
		);
	    } else handleJListIndex(comp, obj);
	}
    }


    private void
    setInputFilter(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setInputFilter(obj);
    }


    private void
    setInsets(Object comp, YoixObject obj) {

	Insets  insets;

	//
	// Java throws a NullPointerException if a JMenuBar's margin
	// is set to null after after being set to non-null, however
	// YoixMakeScreen.javaInsets() should not return null.
	//

	if (comp instanceof JComponent) {
	    insets = YoixMakeScreen.javaInsets(obj);
	    if (comp instanceof JMenuBar)
		((JMenuBar)comp).setMargin(insets);
	    else if (comp instanceof JTextComponent)
		((JTextComponent)comp).setMargin(insets);
	    else if (comp instanceof JToolBar)
		((JToolBar)comp).setMargin(insets);
	    else if (comp instanceof YoixSwingJTextComponent)
		((YoixSwingJTextComponent)comp).setInsets(insets);
	    else if (comp instanceof JButton)
		((JButton)comp).setMargin(obj.notNull() ? insets : null);
	}
    }


    private void
    setInverted(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setInverted(obj.booleanValue());
    }


    private void
    setIpad(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)comp).setIpad(YoixMakeScreen.javaInsets(obj));
    }


    private void
    setItemArray(Object comp, YoixObject obj) {

	//
	// Building menus using the itemarray field is not recommended
	// and even though it's currently supported it may not be in
	// future releases. The approach that's recommended is to build
	// a T_MENU and assign it to the items field.
	//

	if (isMenu(comp) && obj != null) {
	    obj = YoixMake.yoixType(T_MENU, obj);
	    data.put(N_ITEMS, obj, false);
	    setItems(comp, obj);
	}
    }


    private synchronized void
    setItems(Object comp, YoixObject obj) {

	YoixSwingLabelItem  values[];

	if (comp instanceof JList) {
	    if ((values = buildLabelItems(((JList)comp).getModel(), obj, V_ITEMS)) != null)
		((JList)comp).setListData(values);
	} else if (comp instanceof YoixSwingJComboBox) {
	    if ((values = buildLabelItems(((YoixSwingJComboBox)comp).getModel(), obj, V_ITEMS)) != null)
		((YoixSwingJComboBox)comp).setListData(values);
	} else if (isMenu(comp)) {
	    if (obj.isMenu())
		obj = YoixMiscMenu.buildSwingMenuArray(obj);
	    if (obj.isArray() || obj.isNull())
		doMenuLayout(obj);
	    else VM.abort(TYPECHECK, N_ITEMS);
	}
    }


    private synchronized void
    setKeepHidden(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSplitPane)
	    ((YoixSwingJSplitPane)comp).setKeepHidden(obj.booleanValue());
    }


    private void
    setLabels(Object comp, YoixObject obj) {

	YoixSwingLabelItem  values[];
	YoixObject          element;
	Hashtable           table;
	int                 offset;
	int                 size;
	int                 itmp;
	int                 n;

	if (comp instanceof JList) {
	    if ((values = buildLabelItems(((JList)comp).getModel(), obj, V_LABELS)) != null)
		((JList)comp).setListData(values);
	} else if (comp instanceof YoixSwingJComboBox) {
	    if ((values = buildLabelItems(((YoixSwingJComboBox)comp).getModel(), obj, V_LABELS)) != null)
		((YoixSwingJComboBox)comp).setListData(values);
	} else if (comp instanceof YoixSwingJSlider) {
	    if ((size = obj.sizeof()) > 0) {
		offset = obj.offset();
		element = obj.getObject(offset);
		if (size == 1) {
		    offset++;
		    if (element.isInteger()) {
			table = ((YoixSwingJSlider)comp).createStandardLabels(element.intValue());
			((YoixSwingJSlider)comp).setLabelTable(table);
		    } else VM.abort(BADVALUE, N_LABELS, offset-1);
		} else if (size == 2) {
		    offset++;
		    if (element.isInteger()) {
			itmp = element.intValue();
			element = obj.getObject(offset++);
			if (element.isInteger()) {
			    table = ((YoixSwingJSlider)comp).createStandardLabels(itmp, element.intValue());
			    ((YoixSwingJSlider)comp).setLabelTable(table);
			} else if (element.isString()) {
			    table = new Hashtable();
			    table.put(new Integer(itmp), new JLabel(element.stringValue()));
			    ((YoixSwingJSlider)comp).setLabelTable(table);
			    // TODO: should add JLabel as a possible value
			} else VM.abort(BADVALUE, N_LABELS, offset-1);
		    } else VM.abort(BADVALUE, N_LABELS, offset-1);
		} else if (size%2 == 0) {
		    table = new Hashtable(size/2);
		    for (n = 0; n < size; n += 2) {
			element = obj.getObject(offset+n);
			if (element.isInteger()) {
			    itmp = element.intValue();
			    element = obj.getObject(offset+n+1);
			    if (element.isString()) {
				table.put(new Integer(itmp), new JLabel(element.stringValue()));
				// TODO: should add JLabel as a possible value
			    } else VM.abort(BADVALUE, N_LABELS, offset+n+1);
			} else VM.abort(BADVALUE, N_LABELS, offset+n);
		    }
		    ((YoixSwingJSlider)comp).setLabelTable(table);
		} else VM.abort(BADVALUE, N_LABELS);
	    } else ((YoixSwingJSlider)comp).setLabelTable(null);
	}
    }


    private void
    setLayer(Object comp, YoixObject obj) {

	Container  parent;
	int        layer;
	int        max;
	int        min;

	if (comp instanceof Component) {
	    parent = ((Component)comp).getParent();
	    if (parent instanceof JLayeredPane) {
		layer = pickLayer(obj);
		if (layer != ((JLayeredPane)parent).getLayer((Component)comp))
		    ((JLayeredPane)parent).setLayer((Component)comp, layer, 0);
	    }
	}
    }


    private void
    setLeafIcon(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setLeafIcon(obj);
    }


    private void
    setLocation(Object comp, YoixObject obj) {

	GraphicsConfiguration  gc;
	Rectangle              bounds;
	Point                  point;

	if (comp instanceof Component) {
	    if (obj.notNull()) {
		point = YoixMakeScreen.javaPoint(obj);
		if (comp instanceof Window) {
		    if ((gc = getGraphicsConfigurationFromScreen()) != null) {
			bounds = gc.getBounds();
			point.x += bounds.x;
			point.y += bounds.y;
		    }
		}
		((Component)comp).setLocation(point);
		if (!(comp instanceof YoixInterfaceWindow)) {
		    if (comp instanceof JComponent)
			((JComponent)comp).revalidate();
		}
	    }
	}
    }


    private void
    setMajorTickSpacing(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setMajorTickSpacing(obj.intValue());
    }


    private void
    setMappings(Object comp, YoixObject obj) {

	YoixSwingLabelItem  values[];

	if (comp instanceof JList) {
	    if ((values = buildLabelItems(((JList)comp).getModel(), obj, V_MAPPINGS)) != null)
		((JList)comp).setListData(values);
	} else if (comp instanceof YoixSwingJComboBox) {
	    if ((values = buildLabelItems(((YoixSwingJComboBox)comp).getModel(), obj, V_MAPPINGS)) != null)
		((YoixSwingJComboBox)comp).setListData(values);
	}
    }


    private void
    setMaximizable(Object comp, YoixObject obj) {

	if (comp instanceof JInternalFrame)
	    ((JInternalFrame)comp).setMaximizable(obj.booleanValue());
    }


    private void
    setMaximized(Object comp, YoixObject obj) {

	int  state;

	if (comp instanceof JInternalFrame) {
	    try {
		((JInternalFrame)comp).setMaximum(obj.booleanValue());
	    }
	    catch(PropertyVetoException e) {}
	} else if (comp instanceof JFrame) {
	    state = ((JFrame)comp).getExtendedState();
	    if (obj.booleanValue())
		state |= JFrame.MAXIMIZED_BOTH;
	    else state &= ~JFrame.MAXIMIZED_BOTH;
	    ((JFrame)comp).setExtendedState(state);
	}
    }


    private void
    setMaximum(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    ((JScrollBar)comp).setMaximum(obj.intValue());
	else if (comp instanceof JProgressBar)
	    ((JProgressBar)comp).setMaximum(obj.intValue());
	else if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setMaximum(obj.intValue());
    }


    private void
    setMaximumSize(Object comp, YoixObject obj) {

	Dimension  size;
	JViewport  jv;

	if (comp instanceof JComponent) {
	    if (obj.notNull()) {
		if (comp instanceof JScrollPane && (jv = ((JScrollPane)comp).getViewport()) != null) {
		    size = pickLayoutSize(obj, N_MAXIMUMSIZE, jv.getMaximumSize());
		    jv.setMaximumSize(size);
		} else {
		    size = pickLayoutSize(obj, N_MAXIMUMSIZE, ((JComponent)comp).getMaximumSize());
		    ((JComponent)comp).setMaximumSize(size);
		}
		validateRoot(comp);
	    } else if (initialized) {
		((JComponent)comp).setMaximumSize(null);
		validateRoot(comp);
	    }
	}
    }


    private void
    setMinimum(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    ((JScrollBar)comp).setMinimum(obj.intValue());
	else if (comp instanceof JProgressBar)
	    ((JProgressBar)comp).setMinimum(obj.intValue());
	else if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setMinimum(obj.intValue());
    }


    private void
    setMinimumSize(Object comp, YoixObject obj) {

	Dimension  size;
	JViewport  jv;

	if (comp instanceof JComponent) {
	    if (obj.notNull()) {
		if (comp instanceof JScrollPane && (jv = ((JScrollPane)comp).getViewport()) != null) {
		    size = pickLayoutSize(obj, N_MINIMUMSIZE, jv.getMinimumSize());
		    jv.setMinimumSize(size);
		} else {
		    size = pickLayoutSize(obj, N_MINIMUMSIZE, ((JComponent)comp).getMinimumSize());
		    ((JComponent)comp).setMinimumSize(size);
		}
		validateRoot(comp);
	    } else if (initialized) {
		((JComponent)comp).setMinimumSize(null);
		validateRoot(comp);
	    }
	}
    }


    private void
    setMinorTickSpacing(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setMinorTickSpacing(obj.intValue());
    }


    private void
    setMnemonic(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    ((AbstractButton)comp).setMnemonic(obj.intValue());
    }


    private void
    setModal(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJDialog)
	    ((YoixSwingJDialog)comp).setModal(obj.booleanValue());
    }


    private void
    setMode(Object comp, YoixObject obj) {

	StyledEditorKit  editor;
	int              mode;
	int              oldmode;

	if (comp instanceof JTextPane) {
	    oldmode = data.getInt(N_MODE, 0);
	    if ((mode = obj.intValue()) == 0) {
		editor = new StyledEditorKit();
	    } else if (mode < 0) {
		mode = -1;
		editor = new javax.swing.text.rtf.RTFEditorKit();
	    } else {
		mode = 1;
		editor = new YoixHTMLEditorKit();
	    }
	    if (oldmode != mode || !initialized) {
		data.putInt(N_MODE, mode);
		((JTextPane)comp).setEditorKit(editor);
	    }
	} else if (comp instanceof YoixInterfaceFileChooser)
	    ((YoixInterfaceFileChooser)comp).setDialogType(jfcInt("JFileChooserType", obj.intValue()));
    }


    private void
    setMultipleMode(Object comp, YoixObject obj) {

	int  multiple;

	if (comp instanceof YoixInterfaceFileChooser)
	    ((YoixInterfaceFileChooser)comp).setMultiSelectionEnabled(obj.booleanValue());
	else if (comp instanceof JList) {
	    switch (multiple = obj.intValue()) {
		case YOIX_MULTIPLE_INTERVAL_SELECTION:
		    break;

		case YOIX_SINGLE_INTERVAL_SELECTION:
		case YOIX_SINGLE_SELECTION:
		    ((JList)comp).clearSelection();
		    break;

		default:
		    multiple = YOIX_SINGLE_SELECTION;
		    break;
	    }
	    ((JList)comp).getSelectionModel().setSelectionMode(((Integer)jfcObject("JListModel", multiple)).intValue());
	} else if (comp instanceof YoixSwingJTree) {
	    switch (multiple = obj.intValue()) {
		case YOIX_MULTIPLE_INTERVAL_SELECTION:
		    break;

		case YOIX_SINGLE_INTERVAL_SELECTION:
		case YOIX_SINGLE_SELECTION:
		    ((YoixSwingJTree)comp).clearSelection();
		    break;

		default:
		    multiple = YOIX_SINGLE_SELECTION;
		    break;
	    }
	    ((YoixSwingJTree)comp).getSelectionModel().setSelectionMode(((Integer)jfcObject("JTreeModel", multiple)).intValue());
	} else if (comp instanceof YoixSwingJTable) {
	    ((YoixSwingJTable)comp).setSelectionMode(((Integer)jfcObject("JListModel", obj.intValue())).intValue()); // clearSelection() always occurs already
	}
    }


    private void
    setNextFocus(Object comp, YoixObject obj) {

	YoixObject  components;
	YoixObject  root;
	Object      component;
	String      tag;

	if (comp instanceof JComponent) {
	    if (obj.isString()) {
		if ((root = data.getObject(N_ROOT)) != null) {
		    if ((components = root.getObject(N_COMPONENTS)) != null) {
			if ((obj = components.getObject(obj.stringValue())) == null)
			    obj = YoixObject.newNull();
		    } else obj = YoixObject.newNull();
		} else obj = YoixObject.newNull();
		if (obj.isNull())
		    data.putNull(N_NEXTFOCUS);
	    }
	    if (obj.isComponent()) {
		component = obj.getManagedObject();
		if (component instanceof Component)
		    ((JComponent)comp).setNextFocusableComponent((Component)component);
		else VM.abort(TYPECHECK, N_NEXTFOCUS);
	    } else if (obj.isNull())
		((JComponent)comp).setNextFocusableComponent(null);
	    else VM.abort(TYPECHECK, N_NEXTFOCUS);
	}
    }


    private void
    setOneTouchExpandable(Object comp, YoixObject obj) {

	if (comp instanceof JSplitPane)
	    ((JSplitPane)comp).setOneTouchExpandable(obj.booleanValue());
    }


    private void
    setOpaque(Object comp, YoixObject obj) {

	YoixBodyComponent  body;
	YoixObject         components;
	YoixObject         container;
	YoixObject         child;
	YoixObject         menubar;
	boolean            state;
	boolean            repaint;
	Object             source;
	int                m;
	int                n;

	if (comp instanceof JComponent || comp instanceof YoixInterfaceWindow) {
	    if (obj.isNumber() || obj.isNull()) {
		source = comp;
		state = pickOpaque(obj);
		setOpaque(state);
		if (isContainer(comp) || comp instanceof YoixInterfaceWindow) {
		    repaint = true;
		    //
		    // JMenuBar support was added on 1/17/06. Reasonable
		    // behavior, but the code is a little tricky and it
		    // hasn't been thoroughly tested.
		    //
		    if (comp instanceof JFrame || comp instanceof JDialog || comp instanceof JInternalFrame) {
			if ((menubar = data.getObject(N_MENUBAR)) != null && menubar.isComponent()) {
			    comp = menubar.getManagedObject();
			    if (comp instanceof JMenuBar) {
				body = (YoixBodyComponent)menubar.body();
				obj = body.data.getObject(N_OPAQUE);
				//
				// The setOpaque() calls are tricky.
				//
				if (obj != null && obj.notNull())
				    ((YoixBodyComponentSwing)body).setOpaque(comp, obj);
				else ((YoixBodyComponentSwing)body).setOpaque(comp, YoixObject.newInt(state));
			    }
			}
		    }
		    components = data.getObject(N_COMPONENTS);
		    for (n = 0; n < components.length(); n++) {
			if (components.defined(n)) {
			    child = components.getObject(n);
			    if (((YoixBodyComponent)child.body()) != this) {
				comp = child.getManagedObject();
				if (comp instanceof JComponent) {
				    body = (YoixBodyComponent)child.body();
				    obj = body.data.getObject(N_OPAQUE);
				    if (obj != null && obj.notNull()) {
					if (((YoixBodyComponent)(child.body())).isContainer()) {
					    container = child.getObject(N_COMPONENTS);
					    for (m = 1; m < container.length(); m++)
						n += container.defined(m) ? 1 : 0;
					} else if (body instanceof YoixBodyComponentSwing)
					    ((YoixBodyComponentSwing)body).setOpaque(obj.booleanValue());
				    } else {
					//
					// A recent addition (10/17/06) that
					// seems to result in decent behavior.
					// JTabbedPanes need special attention
					// because the
					//
					//    TabbedPane.tabAreaBackground
					//
					// UIManager property apparently is
					// the only way to control the color
					// of that part of a JTabbedPane if
					// it's not transparent.
					//
					if (comp instanceof JTabbedPane)
					    ((JComponent)comp).setOpaque(false);
					else ((JComponent)comp).setOpaque(state);
				    }
				}
			    }
			}
		    }
		    if (repaint)
			((Component)source).repaint();
		}
	    } else VM.abort(TYPECHECK, N_OPAQUE);
	}
    }


    private void
    setOpaque(boolean state) {

	Object  comp;

	comp = this.peer;	// snapshot - just to be safe

	if (comp instanceof JComponent) {
	    ((JComponent)comp).setOpaque(state);
	    setOpaque(peerscroller, state, data.getInt(N_OPAQUEFLAGS, 0));
	} else if (comp instanceof YoixSwingJFrame)
	    ((YoixSwingJFrame)comp).setOpaque(state);
	else if (comp instanceof YoixSwingJDialog)
	    ((YoixSwingJDialog)comp).setOpaque(state);
    }


    private void
    setOpaque(Container container, boolean state, int flags) {

	Component  components[];
	Component  component;
	int        n;

	if (container != null) {
	    components = container.getComponents();
	    for (n = 0; n < components.length; n++) {
		component = components[n];
		if (component instanceof JTextComponent) {
		    if ((flags&0x01) != 0)
			component = null;
		}
		if (component instanceof JScrollBar) {
		    if ((flags&0x02) != 0)
			component = null;
		}
		if (component instanceof JComponent)
		    ((JComponent)component).setOpaque(state);
		if (component instanceof JPanel)
		    setOpaque((JPanel)component, state, flags);
	    }
	    if (container instanceof JComponent)
		((JComponent)container).setOpaque(state);
	}
    }


    private void
    setOpenIcon(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setOpenIcon(obj);
    }


    private void
    setOrientation(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    ((JScrollBar)comp).setOrientation(jfcInt("JScrollBar", obj.intValue()));
	else if (comp instanceof JProgressBar)
	    ((JProgressBar)comp).setOrientation(jfcInt("JProgressBar", obj.intValue()));
	else if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setOrientation(jfcInt("JSlider", obj.intValue()));
	else if (comp instanceof JSplitPane)
	    ((JSplitPane)comp).setOrientation(jfcInt("JSplitPane", obj.intValue()));
	else if (comp instanceof JToolBar)
	    ((JToolBar)comp).setOrientation(jfcInt("JToolBar", obj.intValue()));
	else if (comp instanceof JSeparator)
	    ((JSeparator)comp).setOrientation(jfcInt("JSlider", obj.intValue()));
    }


    private void
    setOrigin(Object comp, YoixObject obj) {

	JScrollPane  scroller;
	JViewport    viewport;

	if (comp instanceof YoixSwingJTable) {
	    if ((scroller = peerscroller) != null) {
		if ((viewport = scroller.getViewport()) != null)
		    viewport.setViewPosition(YoixMakeScreen.javaPoint(obj));
	    }
	} else if (comp instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)comp).setOrigin(YoixMakeScreen.javaPoint(obj));
	else if (comp instanceof YoixSwingJCanvas)
	    ((YoixSwingJCanvas)comp).setOrigin(obj);
	else if (comp instanceof YoixSwingJScrollPane) {
	    if ((viewport = ((JScrollPane)comp).getViewport()) != null)
		viewport.setViewPosition(YoixMakeScreen.javaPoint(obj));
	}
    }


    private void
    setOutputFilter(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setOutputFilter(obj);
    }


    private void
    setPage(Object comp, YoixObject obj) {

	if (comp instanceof JTextPane) {
	    if (obj.notNull()) {
		try {
		    ((JTextPane)comp).setPage(obj.stringValue());
		}
		catch(Exception e) {
		    //
		    // Older versions called VM.abort() here, but seems like
		    // displaying an error message in the JTextPane is better
		    // behavior and much more useful to the user. The message
		    // undoubtedly could use some formatting - maybe later.
		    //
		    VM.caughtException(e, true);
		    setText(comp, YoixObject.newString("cannot load URL: " + obj.stringValue()));
		}
	    } else if (initialized)
		setText(comp, obj);
	}
    }


    private void
    setPaintLabels(Object comp, YoixObject obj) {

	Dictionary  table;

	if (comp instanceof YoixSwingJSlider) {
	    if ((table = ((YoixSwingJSlider)comp).getLabelTable()) != null && table.size() > 0)
		((YoixSwingJSlider)comp).setPaintLabels(obj.booleanValue());
	}
    }


    private void
    setPaintTicks(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setPaintTicks(obj.booleanValue());
    }


    private void
    setPaintTrack(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setPaintTrack(obj.booleanValue());
    }


    private void
    setPanAndZoom(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJCanvas)
	    ((YoixSwingJCanvas)comp).setPanAndZoom(obj);
    }


    private void
    setPreferredSize(Object comp, YoixObject obj) {

	Dimension  size;
	JViewport  jv;

	if (comp instanceof JComponent) {
	    if (obj.notNull()) {
		if (comp instanceof JScrollPane && (jv = ((JScrollPane)comp).getViewport()) != null) {
		    size = pickLayoutSize(obj, N_PREFERREDSIZE, jv.getPreferredSize());
		    jv.setPreferredSize(size);
		} else {
		    size = pickLayoutSize(obj, N_PREFERREDSIZE, ((JComponent)comp).getPreferredSize());
		    ((JComponent)comp).setPreferredSize(size);
		}
		validateRoot(comp);
	    } else if (initialized) {
		((JComponent)comp).setPreferredSize(null);
		validateRoot(comp);
	    }
	}
    }


    private void
    setPrompt(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextTerm)
	    ((YoixSwingJTextTerm)comp).setPrompt(obj.stringValue());
    }


    private void
    setPressed(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    ((AbstractButton)comp).getModel().setPressed(obj.booleanValue());
    }


    private void
    setPrototypeValue(Object comp, YoixObject obj) {

	Object  value;
	char    charset[] = {'M', 'p', 'a', 'b', 'c', 'i', 'm', 'n', 'o'};
	char    letters[];
	int     n;

	if (comp instanceof JComboBox || comp instanceof JList) {
	    if (obj.isNull()) {
		value = null;
	    } else if (obj.isString()) {
		value = obj.stringValue();
	    } else if (obj.isImage()) {
		value = YoixMake.javaIcon(obj);
	    } else if (obj.isInteger() && obj.intValue() > 0) {
		//
		// Recent (1/8/11) addition. Not at all convinced by this,
		// so right now (9/27/11) it's undocumented and it may be
		// removed in future releases.
		//
		letters = new char[obj.intValue()];
		for (n = 0; n < letters.length; n++)
		    letters[n] = charset[n%charset.length];
		value = new String(letters);
	    } else value = null;
	    if (comp instanceof JComboBox)
		((JComboBox)comp).setPrototypeDisplayValue(value);
	    else ((JList)comp).setPrototypeCellValue(value);
	}
    }


    private void
    setQuiet(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setQuiet(obj.booleanValue());
    }


    private void
    setReorder(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setReorder(obj.booleanValue());
    }


    private void
    setRequestFocusEnabled(Object comp, YoixObject obj) {

	if (comp instanceof JComponent)
	    ((JComponent)comp).setRequestFocusEnabled(obj.booleanValue());
    }


    private void
    setReset(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJColorChooser)
	    ((YoixSwingJColorChooser)comp).setReset(YoixMake.javaColor(obj));
    }


    private void
    setResize(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setResize(obj.booleanValue());
    }


    private void
    setResizeMode(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setAutoResizeMode(obj.intValue());
    }


    private void
    setResizeWeight(Object comp, YoixObject obj) {

	if (comp instanceof JSplitPane)
	    ((JSplitPane)comp).setResizeWeight(Math.max(0, Math.min(obj.doubleValue(), 1)));
    }


    private void
    setRollover(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    ((AbstractButton)comp).getModel().setRollover(obj.booleanValue());
    }


    private void
    setRolloverEnabled(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    ((AbstractButton)comp).setRolloverEnabled(obj.booleanValue());
    }


    private void
    setRoothandle(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setShowsRootHandles(obj.booleanValue());
    }


    private void
    setRowHeightAdjustment(Object comp, YoixObject obj) {

	double  val = obj.doubleValue();

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setRowHeightAdjustment(val < 0 ? -1 : YoixMakeScreen.javaDistance(val));
    }


    private void
    setRows(Object comp, YoixObject obj) {

	if (comp instanceof JTextArea)
	    ((JTextArea)comp).setRows(Math.max(obj.intValue(), 0));
	else if (comp instanceof JList)
	    ((JList)comp).setVisibleRowCount(Math.max(obj.intValue(), 0));
	else if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setRows(obj.intValue());
	else if (comp instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)comp).setRows(Math.max(obj.intValue(), 0));
	else if (comp instanceof YoixSwingJComboBox)
	    ((YoixSwingJComboBox)comp).setRows(Math.max(obj.intValue(), 0));
    }


    private void
    setSaveGraphics(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)comp).setSaveGraphics(obj.booleanValue());
    }


    private void
    setSaveLines(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextTerm)
	    ((YoixSwingJTextTerm)comp).setSaveLines(obj.intValue());
    }


    private void
    setScroll(Object comp, YoixObject obj) {

	JScrollPane  pane;
	int          scroll;

	if (comp instanceof JScrollPane) {
	    pane = (JScrollPane)comp;
	    scroll = jfcInt("ScrollPolicy", obj.intValue());
	    if ((scroll&YOIX_HORIZONTAL_ALWAYS) == YOIX_HORIZONTAL_ALWAYS)
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	    else if ((scroll&YOIX_HORIZONTAL_AS_NEEDED) == YOIX_HORIZONTAL_AS_NEEDED)
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    else pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	    if ((scroll&YOIX_VERTICAL_ALWAYS) == YOIX_VERTICAL_ALWAYS)
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    else if ((scroll&YOIX_VERTICAL_AS_NEEDED) == YOIX_VERTICAL_AS_NEEDED)
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    else pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
	} else if (comp instanceof YoixSwingJTabbedPane)
	    ((YoixSwingJTabbedPane)comp).setScrollPolicy(jfcInt("JTabbedPaneScroll", obj.intValue()));
    }


    private void
    setScrollsOnExpand(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setScrollsOnExpand(obj.booleanValue());
    }


    private void
    setSelected(Object comp, YoixObject obj) {

	YoixSwingLabelItem  labelitems[];
	YoixObject          items;
	YoixObject          item;
	YoixObject          yobj;
	YoixObject          components;
	Hashtable           indexmap;
	boolean             exists;
	Object              oldvalue;
	Object              newvalue;
	Object              value;
	String              replacement;
	String              oldtext;
	String              newtext;
	String              key;
	String              tag;
	int                 lastindex;
	int                 index;
	int                 count;
	int                 start;
	int                 len;
	int                 end;
	int                 i;
	int                 m;
	int                 n;

	if (comp instanceof AbstractButton) {
	    if (obj.notNull()) {
		if (obj.isInteger())
		    ((AbstractButton)comp).setSelected(obj.booleanValue());
		else VM.abort(TYPECHECK, N_SELECTED);
	    }
	} else if (comp instanceof JTextComponent) {
	    if (obj.isString()) {
		replacement = obj.stringValue();
		start = ((JTextComponent)comp).getSelectionStart();
		end = ((JTextComponent)comp).getSelectionEnd();
		if (comp instanceof JTextArea) {
		    ((JTextArea)comp).replaceRange(replacement, start, end);
		} else {
		    oldtext = ((JTextComponent)comp).getText();
		    n = oldtext.length();
		    newtext = oldtext.substring(0, start);
		    newtext += replacement;
		    if (n >= end)
			newtext += oldtext.substring(end);
		    ((JTextComponent)comp).setText(newtext);
		}
		end = start + replacement.length();
		((JTextComponent)comp).setCaretPosition(end);
	    } else VM.abort(TYPECHECK, N_SELECTED);
	} else if (comp instanceof JList) {
	    if (obj.notNull()) {
		indexmap = null;
		if (obj.isArray()) {
		    lastindex = index = -1;
		    for (n = obj.offset(); n < obj.length(); n++) {
			if (obj.defined(n)) {
			    item = obj.get(n, false);
			    if (item.isNull())
				continue;
			    else if (item.isString()) {
				index = -1;
				if (indexmap == null) {
				    if (n+1 == obj.length()) {
					index = getLabelItemIndex(((JList)comp).getModel(), item.stringValue());
				    } else {
					labelitems = getLabelItems(((JList)comp).getModel());
					indexmap = new Hashtable();
					for (m = 0; m < labelitems.length; m++)
					    indexmap.put(labelitems[m].getText(), new Integer(m));
					for (m = 0; m < labelitems.length; m++)
					    indexmap.put(labelitems[m].getValue(), new Integer(m));
				    }
				}
				if (indexmap != null && (value = indexmap.get(item.stringValue())) != null)
				    index = ((Integer)value).intValue();
			    } else if (item.isNumber())
				index = item.intValue();
			    else if (item.isArray()) {
				if (item.sizeof() == 2) {
				    m = item.offset();
				    yobj = item.get(m, false);
				    if (yobj.isNumber())
					index = yobj.intValue();
				    else VM.abort(BADVALUE, N_SELECTED, n, 0);
				    yobj = item.get(m+1, false);
				    if (yobj.isNumber())
					lastindex = yobj.intValue();
				    else VM.abort(BADVALUE, N_SELECTED, n, 1);
				} else VM.abort(BADVALUE, N_SELECTED, n);
			    } else VM.abort(TYPECHECK, N_SELECTED + "[" +  n + "]");
			    if (index >= 0) {
				if (lastindex < 0)
				    ((JList)comp).addSelectionInterval(index, index);
				else {
				    ((JList)comp).addSelectionInterval(index, lastindex);
				    lastindex = -1;
				}
			    }
			}
		    }
		    if (index >= 0)
			((JList)comp).ensureIndexIsVisible(index);
		} else if (obj.isString()) {
		    index = getLabelItemIndex(((JList)comp).getModel(), obj.stringValue());
		    if (index >= 0) {
			((JList)comp).addSelectionInterval(index, index);
			((JList)comp).ensureIndexIsVisible(index);
		    }
		} else if (obj.isNumber()) {
		    index = obj.intValue();
		    if (index >= 0) {
			((JList)comp).addSelectionInterval(index, index);
			((JList)comp).ensureIndexIsVisible(index);
		    }
		} else if (obj.notNull())
		    VM.abort(TYPECHECK, N_SELECTED);
	    } else ((JList)comp).clearSelection();
	} else if (comp instanceof YoixSwingJComboBox) {
	    if (obj.notNull()) {
		count = ((YoixSwingJComboBox)comp).getItemCount();
		if (obj.isString()) {
		    newtext = obj.stringValue();
		    index = getLabelItemIndex(((YoixSwingJComboBox)comp).getModel(), newtext);
		    if (index >= 0 && index < count)
			((YoixSwingJComboBox)comp).setSelectedIndex(index);
		    else if (((YoixSwingJComboBox)comp).isEditable()) {
			((YoixSwingJComboBox)comp).getEditor().setItem(newtext);
			((YoixSwingJComboBox)comp).getModel().setSelectedItem(newtext);
		    } else ((YoixSwingJComboBox)comp).setSelectedItem(null);
		} else if (obj.isNumber()) {
		    index = obj.intValue();
		    if (index >= 0 && index < count)
			((YoixSwingJComboBox)comp).setSelectedIndex(index);
		    else ((YoixSwingJComboBox)comp).setSelectedItem(null);
		} else VM.abort(TYPECHECK, N_SELECTED);
	    } else ((YoixSwingJComboBox)comp).setSelectedItem(null);
	} else if (comp instanceof ButtonGroup) {
	    oldvalue = ((ButtonGroup)comp).getSelection();
	    newvalue = null;
	    if (obj.notNull()) {
		if (obj.isString()) {
		    newvalue = obj.stringValue();
		    if (oldvalue != null && newvalue.equals(((ButtonModel)oldvalue).getActionCommand()))
			oldvalue = newvalue;
		} else if (obj.isComponent())
		    newvalue = ((AbstractButton)(obj.getManagedObject())).getModel();
	    }
	    if (oldvalue != newvalue) {
		if (newvalue != null) {
		    items = data.getObject(N_ITEMS);
		    if (items.notNull()) {
			exists = false;
			len = items.length();
			for (i = 0; i < len; i++) {
			    value = items.get(i, false).getManagedObject();
			    oldvalue = ((AbstractButton)value).getModel();
			    if (
				newvalue == oldvalue
				||
				(newvalue instanceof String && ((ButtonModel)oldvalue).getActionCommand().equals(newvalue))
			    ) {
				newvalue = value;
				exists = true;
				break;
			    }
			}
			if (exists) {
			    ((ButtonGroup)comp).setSelected(((AbstractButton)newvalue).getModel(), true);
			    //
			    // repaint needed (on Linux, at least) otherwise
			    // button will appear selected only after the
			    // first time the cursor enters it
			    //
			    ((AbstractButton)newvalue).repaint();
			} else ((ButtonGroup)comp).setSelected(bg_dummy.getModel(), true);
		    }
		} else ((ButtonGroup)comp).setSelected(bg_dummy.getModel(), true);
	    }
	} else if (comp instanceof YoixSwingJTabbedPane) {
	    if (obj.isString()) {
		components = data.getObject(N_COMPONENTS);
		if ((obj = components.getObject(obj.stringValue())) != null) {
		    if (obj.isComponent())
			((YoixSwingJTabbedPane)comp).setSelected(obj);
		}
	    } else if (obj.isNull() || obj.isComponent() || obj.isNumber())
		((YoixSwingJTabbedPane)comp).setSelected(obj);
	    else VM.abort(TYPECHECK, N_SELECTED);
	} else if (comp instanceof JDesktopPane) {
	    if (obj.isNull() || obj.isJInternalFrame())
		((JDesktopPane)comp).setSelectedFrame((JInternalFrame)obj.getManagedObject());
	    else VM.abort(TYPECHECK, N_SELECTED);
	}
    }


    private void
    setSelectedEnds(Object comp, YoixObject obj) {

	YoixObject  item;
	Object      editor = comp;
	String      text;
	int         start;
	int         end;
	int         length;
	int         n;

	if (editor instanceof JTextComponent || (editor = getJComboBoxTextEditor(comp)) != null) {
	    if (obj.notNull()) {
		n = obj.offset();
		text = ((JTextComponent)editor).getText();
		if (text != null) {
		    length = text.length();
		    if ((item = obj.getObject(n++)) != null && item.isNumber())
			start = Math.min(Math.max(item.intValue(), 0), length);
		    else start = 0;
		    if ((item = obj.getObject(n++)) != null && item.isNumber())
			end = Math.min(Math.max(item.intValue(), start), length);
		    else end = length;
		} else {
		    start = 0;
		    end = 0;
		}
	    } else {
		start = Math.max(data.getInt(N_CARET, 0), 0);
		end = start;
	    }
	    try {
		// note: caret position is properly set at end position
		((JTextComponent)editor).select(start, end);
	    }
	    catch(RuntimeException e) {}		// a precaution
	}
    }


    private void
    setSelectionBackground(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	Color        color;
	int          start;
	int          end;

	if (comp instanceof JTextComponent) {
	    color = YoixMake.javaColor(obj, javaDefaultSelectionBackground);
	    ((JTextComponent)comp).setSelectionColor(color);
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (!(highlighter instanceof YoixSwingHighlighter)) {
		start = ((JTextComponent)comp).getSelectionStart();
		end = ((JTextComponent)comp).getSelectionEnd();
		if (start < end)
		    ((JTextComponent)comp).select(start, end);
	    } else ((YoixSwingHighlighter)highlighter).repaintHighlights();
	} else if (comp instanceof YoixSwingJTable) {
	    color = defaultSelectionBackground;
	    defaultSelectionBackground = YoixMake.javaColor(obj, UIManager.getColor("Table.selectionBackground"));
	    if (!defaultSelectionBackground.equals(color))
		((YoixSwingJTable)comp).setSelectionBackground(defaultSelectionBackground);
	} else if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setSelectionBackground(obj);
    }


    private void
    setSelectionForeground(Object comp, YoixObject obj) {

	Highlighter  highlighter;
	Color        color;
	int          start;
	int          end;

	if (comp instanceof JTextComponent) {
	    color = YoixMake.javaColor(obj, javaDefaultSelectionForeground);
	    ((JTextComponent)comp).setSelectedTextColor(color);
	    highlighter = ((JTextComponent)comp).getHighlighter();
	    if (!(highlighter instanceof YoixSwingHighlighter)) {
		start = ((JTextComponent)comp).getSelectionStart();
		end = ((JTextComponent)comp).getSelectionEnd();
		if (start < end)
		    ((JTextComponent)comp).select(start, end);
	    } else ((YoixSwingHighlighter)highlighter).repaintHighlights();
	} else if (comp instanceof YoixSwingJTable) {
	    color = defaultSelectionForeground;
	    defaultSelectionForeground = YoixMake.javaColor(obj, UIManager.getColor("Table.selectionForeground"));
	    if (!defaultSelectionForeground.equals(color))
		((YoixSwingJTable)comp).setSelectionForeground(defaultSelectionForeground);
	} else if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setSelectionForeground(obj);
    }


    private void
    setSize(Object comp, YoixObject obj) {

	Dimension  size;

	//
	// Changed on 3/22/05 so pack() is always called when obj is null.
	// Previous version did
	//
	//	} else if (isPacked())
	//	    ((YoixInterfaceWindow)comp).pack();
	//
	// but the testing caused an obsurce scrollbar related problem in
	// one screen in an important internal application. Change should
	// be thoroughly tested!!!
	//
	// NOTE - preceding the pack() call with syncDispatchThread(0) is
	// a change that was added on 4/27/07 and was done to make sure
	// any revalidate() calls that may have been queued actually get
	// a chance to do their thing before we pack the window. This fix
	// addressed a sizing problem that we sometimes saw when a screen
	// that contained a JTextArea with zero for its rows and columns
	// was built outside the event thread.
	// 

	if (comp instanceof Component) {
	    if (comp instanceof YoixInterfaceWindow) {
		if (obj.notNull()) {
		    if ((size = YoixMakeScreen.javaDimension(obj)) != null) {
			((Component)comp).setSize(size);
			setPacked(true);
		    }
		} else {
		    if ((size = getShapeSize(comp)) != null) {
			((Component)comp).setSize(size);
			setPacked(true);
		    } else {
			syncDispatchThread(ISMAC ? 2 : 0);	// small ISMAC kludge added 6/4/2008
			pack();
		    }
		}
	    } else if (comp instanceof YoixSwingJTable) {
		if (obj.notNull()) {
		    size = YoixMakeScreen.javaDimension(obj);
		    ((YoixSwingJTable)comp).setPreferredScrollableViewportSize(size);
		} else {
		    size = ((YoixSwingJTable)comp).getPreferredSize();
		    if (data.getInt(N_ROWS, -1) >= 0)
			size.height = ((YoixSwingJTable)comp).getPreferredScrollableViewportSize().height;
		    ((YoixSwingJTable)comp).setPreferredScrollableViewportSize(size);
		}
		((YoixSwingJTable)comp).revalidate();
		// users will also need to set the frame size to NULL to see a change, but
		// we cannot presume to do it here.
	    } else if (comp instanceof JComponent) {
		if (obj.notNull()) {
		    size = YoixMakeScreen.javaDimension(obj);
		    if (YoixMisc.notEqual(size, ((Component)comp).getSize())) {
			((Component)comp).setSize(size);
			((JComponent)comp).revalidate();
		    }
		} else size = layoutsize;
		syncPreferredSize(peerscroller != null ? peerscroller : (JComponent)comp, size);
	    }
	}
    }


    private void
    setSizeControl(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJScrollPane)
	    ((YoixSwingJScrollPane)comp).setSizeControl(obj.intValue());
	else if (comp instanceof YoixSwingJTabbedPane)
	    ((YoixSwingJTabbedPane)comp).setSizeControl(obj.intValue());
    }


    private void
    setSnapToTicks(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setSnapToTicks(obj.booleanValue());
    }


    private void
    setState(Object comp, YoixObject obj) {

	if (comp instanceof AbstractButton)
	    ((AbstractButton)comp).setSelected(obj.booleanValue());
	else if (comp instanceof YoixSwingJCanvas)
	    ((YoixSwingJCanvas)comp).setState(obj.intValue());
    }


    private void
    setSyncViewport(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextComponent)
	    ((YoixSwingJTextComponent)comp).setSyncViewport(obj);
    }


    private void
    setText(Object comp, YoixObject obj) {

	boolean  trim;
	Object   editor;
	String   str;
	int      n;

	//
	// Apparently a JLabel displaying null text or the empty string
	// doesn't think it needs to show up, so we decided to call the
	// JLabel's setText() method with " " as the string. Means we
	// can update the text displayed by the JLabel without worrying
	// about it disappearing.
	//

	if (comp instanceof Component) {
	    data.put(N_TEXT, YoixObject.newString(), false);
	    trim = data.getBoolean(N_AUTOTRIM);
	    if (comp instanceof AbstractButton) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((AbstractButton)comp).setText(str);
	    } else if (comp instanceof JPopupMenu) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((JPopupMenu)comp).setLabel(str);
	    } else if (comp instanceof JLabel) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		//
		// JLabel kludge - make sure text isn't null or empty
		//
		((JLabel)comp).setText(str != null && str.length() > 0 ? str : " ");
	    } else if (comp instanceof JTextComponent) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((JTextComponent)comp).setText(str);
	    } else if (comp instanceof JComboBox && (editor = ((JComboBox)comp).getEditor()) != null && (editor = ((ComboBoxEditor)editor).getEditorComponent()) instanceof JTextComponent) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((JTextComponent)editor).setText(str);
	    } else if (comp instanceof YoixSwingJTextComponent) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((YoixSwingJTextComponent)comp).setText(str);
	    } else if (comp instanceof YoixSwingJTable) {
		((YoixSwingJTable)comp).setValues(obj);
	    } else if (comp instanceof JProgressBar) {
		if (obj.isNull()) {
		    if (((JProgressBar)comp).isStringPainted())
			((JProgressBar)comp).setStringPainted(false);
		} else {
		    str = trim ? obj.stringValue().trim() : obj.stringValue();
		    ((JProgressBar)comp).setString(str);
		    if (!((JProgressBar)comp).isStringPainted())
			((JProgressBar)comp).setStringPainted(true);
		}
	    }
	}
    }


    private void
    setTextMode(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTextCanvas)
	    ((YoixSwingJTextCanvas)comp).setTextMode(obj.intValue());
    }


    private void
    setTextPosition(Object comp, YoixObject obj) {

	//
	// Eventually consider setting vertical alignment for components,
	// like JLabel and AbstractButton, that support it. Decided not
	// to rush the change into this release. Also, menuitems can be
	// screwy (e.g., if menu has checkboxes in it, then width may not
	// be adjusted to subtract the width reserved for the checkbox
	// portion).
	//

	if (comp instanceof AbstractButton)
	    ((AbstractButton)comp).setHorizontalTextPosition(jfcInt("SwingHorizontalAlignment", obj.intValue()));
	else if (comp instanceof JLabel)
	    ((JLabel)comp).setHorizontalTextPosition(jfcInt("SwingHorizontalAlignment", obj.intValue()));
    }


    private void
    setTextWrap(Object comp, YoixObject obj) {

	boolean  linewrap;
	boolean  wordwrap;
	int      textwrap;

	if (comp instanceof JTextArea) {
	    textwrap = obj.intValue();
	    if (textwrap > 0) {
		((JTextArea)comp).setLineWrap(true);
		((JTextArea)comp).setWrapStyleWord(true);
	    } else if (textwrap < 0) {
		((JTextArea)comp).setLineWrap(true);
		((JTextArea)comp).setWrapStyleWord(false);
	    } else ((JTextArea)comp).setLineWrap(false);
	} else if (comp instanceof YoixSwingJTextCanvas)
	    ((YoixSwingJTextCanvas)comp).setTextWrap(obj.booleanValue());
    }


    private void
    setTitle(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    if (comp instanceof JFrame)
		((JFrame)comp).setTitle(obj.stringValue());
	    else if (comp instanceof JDialog)
		((JDialog)comp).setTitle(obj.stringValue());
	    else if (comp instanceof YoixSwingJInternalFrame)
		((YoixSwingJInternalFrame)comp).setTitle(obj.stringValue());
	    syncTabProperty((Component)comp, N_TITLE, obj.stringValue());
	}
    }


    private void
    setToolTipText(Object comp, YoixObject obj) {

	//
	// YoixSwingJTable currently accepts an array or string, mostly for
	// backward compatibility, but other components only take a string.
	// Decent chance our test files are the only place an array is used,
	// but we decided to leave the code in for now but it shouldn't be
	// be documented!!!
	//

	if (comp instanceof Component) {
	    if (comp instanceof YoixSwingJTable || comp instanceof YoixSwingJTree) {
		//
		// These currently are special and our low level Java code
		// interprets a null tooltip text to mean all tooltips are
		// disabled. Unfortunately registering or unregistering the
		// JTree or JTable with the ToolTipManager (in setTooltips())
		// didn't completely enable or disable tooltips. We probably
		// will revisit this (and setTooltips()) in a future release.
		//
		// NOTE - currently nothing special to do for YoixSwingJTree,
		// but that probably will change.
		//
		if (comp instanceof YoixSwingJTable)
		    ((YoixSwingJTable)comp).setToolTipText(obj);
 	    } else if (comp instanceof JComponent)
		((JComponent)comp).setToolTipText(obj.notNull() ? obj.stringValue() : null);
	    else if (comp instanceof YoixSwingJDialog)
		((YoixSwingJDialog)comp).setToolTipText(obj.notNull() ? obj.stringValue() : null);
	    else if (comp instanceof YoixSwingJFrame)
		((YoixSwingJFrame)comp).setToolTipText(obj.notNull() ? obj.stringValue() : null);
	    else if (comp instanceof YoixSwingJWindow)
		((YoixSwingJWindow)comp).setToolTipText(obj.notNull() ? obj.stringValue() : null);
	    syncTabProperty((Component)comp, N_TOOLTIPTEXT, obj.isString() && obj.notNull() ? obj.stringValue() : null);
	}
    }


    private void
    setTooltips(Object comp, YoixObject obj) {

	//
	// Seems like we should just be able to register or unregister
	// comp with the ToolTipManager, but that didn't really work, so
	// we hide enabled and disabled info in the tooltip text. Works
	// but seems a little strange.
	//

	if (comp instanceof YoixSwingJTree || comp instanceof YoixSwingJTable) {
	    if (obj.booleanValue()) {
		// just need a non-null value to turn them on;
		// renderers handle the actual tooltip text
		((JComponent)comp).setToolTipText("");
	    } else ((JComponent)comp).setToolTipText(null);
	}
    }


    private synchronized void
    setTop(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTree)
	    ((YoixSwingJTree)comp).setTop(obj);
    }


    private void
    setTrackFocus(Object comp, YoixObject obj) {

	YoixObject  layout;
	YoixObject  tab;
	YoixObject  components;
	YoixObject  component;
	int         m;
	int         n;

	if (comp instanceof YoixSwingJTabbedPane) {
	    if (obj.booleanValue()) {
		((YoixSwingJTabbedPane)comp).setTrackFocus(true);
		if ((layout = data.getObject(N_LAYOUT)) != null) {
		    for (n = 0; n < layout.length(); n++) {
			if ((tab = layout.getObject(n)) != null) {
			    if ((components = tab.getObject(N_COMPONENTS)) != null) {
				for (m = components.length() - 1; m >= 1; m--) {
				    if ((obj = components.getObject(m)) != null) {
					if (obj.getBoolean(N_REQUESTFOCUS)) {
					    try {
						((YoixSwingJTabbedPane)comp).setFirstFocus(
						    (Component)tab.getManagedObject(),
						    (Component)obj.getManagedObject()
						);
					    }
					    catch(RuntimeException e) {}
					    break;
					}
				    }
				}
			    }
			}
		    }
		}
	    } else ((YoixSwingJTabbedPane)comp).setTrackFocus(false);
	}
    }


    private void
    setTransferHandler(Object comp, YoixObject obj) {

	TransferHandler  handler = null;
	DropTarget       droptarget;
	String           property;

	//
	// Needed to be a little tricky with the way N_TRANSFERHANDLER is
	// declared in YoixModuleSwing.java and the type checking that we
	// do in this method. We tried making N_TRANSFERHANDLER an empty
	// string in YoixModuleSwing.java, but that caused initialization
	// problems (strange rangecheck errors that we understand). To get
	// around the problems we made N_TRANSFERHANDLER start as an int,
	// which we also let through if we're not initialized yet. Feels
	// like a kludge, but it's probably something we're just going to
	// have to live with.
	//

	if (comp instanceof JComponent) {
	    if (obj.notNull()) {
		if (obj.isString()) {
		    property = obj.stringValue();
		    if (property.length() > 0) {
			obj = YoixObject.newTransferHandler(property);
			handler = new YoixSwingTransferHandler(obj, getContext());
		    } else handler = nulltransferhandler;
		} else if (obj.isTransferHandler())
		    handler = new YoixSwingTransferHandler(obj, getContext());
		else if (obj.isInteger() && initialized == false)
		    handler = nulltransferhandler;
		else VM.abort(TYPECHECK, N_TRANSFERHANDLER);
	    } else handler = null;

	    if ((droptarget = ((JComponent)comp).getDropTarget()) != null) {
		try {
		    droptarget.removeDropTargetListener(this);
		    droptarget.setComponent(null);
		}
		catch(IllegalArgumentException e) {}
	    }

	    //
	    // This seems to be necessary if we ever expect to
	    // completely restore DropTarget behavior that Swing
	    // provides for some components, like a JTextArea.
	    //

	    ((JComponent)comp).setTransferHandler(null);

	    if (handler == null) {
		//
		// Reproduces what one of the DropTarget() constructors
		// does behind the scene, but using that constructor and
		// then tossing the result seemed confusing (at best).
		//
		try {
		    droptarget = new DropTarget();
		    droptarget.addDropTargetListener(this);
		    droptarget.setComponent((JComponent)comp);
		    droptarget.setActive(true);
		}
		catch (TooManyListenersException e) {}
	    } else ((JComponent)comp).setTransferHandler(handler);

	    if (comp instanceof YoixSwingJTextArea)
		((YoixSwingJTextArea)comp).syncCaretToModel();
	    else if (comp instanceof YoixSwingJTextField)
		((YoixSwingJTextField)comp).syncCaretToModel();
	    else if (comp instanceof YoixSwingJTextPane)
		((YoixSwingJTextPane)comp).syncCaretToModel();
	}
    }


    private void
    setTypes(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setTypes(obj);
    }


    private void
    setUnitIncrement(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJScrollBar)
	    ((YoixSwingJScrollBar)comp).setUnitIncrement(obj.intValue());
	else if (comp instanceof JScrollBar)
	    ((JScrollBar)comp).setUnitIncrement(Math.max(obj.intValue(), 1));
	else if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setUnitIncrement(Math.max(obj.intValue(), 1));
    }


    private void
    setUseEditHighlight(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setUseEditHighlight(obj.booleanValue());
    }


    private void
    setValidator(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setValidator(obj);
    }


    private void
    setValue(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    ((JScrollBar)comp).setValue(obj.intValue());
	else if (comp instanceof JProgressBar)
	    ((JProgressBar)comp).setValue(obj.intValue());
	else if (comp instanceof YoixSwingJSlider)
	    ((YoixSwingJSlider)comp).setValue(obj.intValue());
    }


    private void
    setValues(Object comp, YoixObject obj) {

	if (comp instanceof YoixSwingJTable)
	    ((YoixSwingJTable)comp).setValues(obj);
    }


    private void
    setVisible(boolean state) {

	boolean  later;
	Object   comp;
	Object   lock;

	//
	// Modal dialogs need special attention if we're about to show one,
	// so deciding whether or not to use invokeLater() is harder than
	// you might initially expect. Obviously modal dialogs can be shown
	// from the dispatch thread, but there are situations where it won't
	// always work properly (e.g., the blocking won't happen).
	//

	if (EventQueue.isDispatchThread() == false) {
	    comp = this.peer;		// snapshot - just to be safe
	    if (isModalDialog(comp))
		later = !state;
	    else later = true;
	    if (later) {
		try {
		    lock = new Object();
		    synchronized(lock) {
			EventQueue.invokeLater(
			    new YoixAWTInvocationEvent(
				this,
				new Object[] {new Integer(RUN_SETVISIBLE), state ? Boolean.TRUE : Boolean.FALSE, lock}
			    )
			);
			lock.wait();
		    }
		}
		catch(InterruptedException e) {}
	    } else handleSetVisible(state);
	} else handleSetVisible(state);
    }


    private void
    setVisible(Object comp, YoixObject obj) {

	GraphicsDevice  screen;
	boolean         state;

	//
	// NOTE - preceding the pack() call with syncDispatchThread(0) is
	// a change that was added on 4/27/07 and was done to make sure
	// any revalidate() calls that may have been queued actually get
	// a chance to do their thing before we pack the window. This fix
	// addressed a sizing problem that we sometimes saw when a screen
	// that contained a JTextArea with zero for its rows and columns
	// was built outside the event thread.
	// 

	if (comp instanceof Component) {
	    state = obj.booleanValue();
	    if (comp instanceof YoixInterfaceWindow) {
		if (state != ((Component)comp).isVisible()) {
		    if (state) {
			windowActivate(this, comp);
			if (isPacked() == false) {
			    syncDispatchThread(0);
			    pack();
			    setPacked(true);
			}
		    } else windowDeactivate(this);
		    //
		    // We ran into some deadlock problems that only happened with
		    // a JTextPane in HTML mode (eventtest3.yx) using Apple's JVM
		    // on a intel Mac Pro and the only line traced back to what
		    // we were doing was the set visible call. So, do this trick
		    // again. Deadlock first noticed with 1.5.0_7 on 1/23/2007.
		    //
		    if (MAC_DEADLOCK_KLUDGE)
			syncDispatchThread(0);

		    //
		    // This is primarily needed when windows have their fullscreen
		    // field set to true in their initializer. Handling it in the
		    // method responsible for the fullscreen field wouldn't work
		    // because GraphicsDevice.setFullScreenWindow() makes sure its
		    // window argument is visible, but we count on this method to
		    // handle important initialization that would be skipped if we
		    // got here (the first time through) and the window already was
		    // visible. This probably could be moved up a few lines - later.
		    // 
		    if (state) {
			if (comp instanceof Window) {
			    if (data.getBoolean(N_FULLSCREEN)) {
				if ((screen = getGraphicsDeviceFromScreen()) != null)
				    screen.setFullScreenWindow((Window)comp);
			    }
			}
		    }
		    /////////((Component)comp).setVisible(state);
		    setVisible(state);
		    requestFirstFocus();
		    if (state == false) {
			if (data.getBoolean(N_AUTODISPOSE, false) == false)
			    childrenSetVisible(state);
			else dispose(false);
		    } else childrenSetVisible(state);
		}
		if (comp instanceof JFrame) {
		    if (state && data.getBoolean(N_AUTODEICONIFY, false))
			((JFrame)comp).setState(JFrame.NORMAL);
		}
		//
		// Old versions did this in an else clause, which seemed
		// wrong, so we changed it on 11/28/04.
		//
		if (state && data.getBoolean(N_AUTORAISE, false)) {
		    if (!((comp instanceof Dialog) && ((Dialog)comp).isModal()))
			((YoixInterfaceWindow)comp).toFront();
		}
	    } else if (comp instanceof JMenuBar) {
		if (state != ((Component)comp).isVisible()) {
		    //////////((Component)comp).setVisible(state);
		    setVisible(state);
		    showMenuBar((JMenuBar)comp, state);
		}
	    } else if (comp instanceof JPopupMenu) {
		if (state)
		    showPopupMenu((JPopupMenu)comp);
		//////////else ((Component)comp).setVisible(state);
		else setVisible(state);
	    //////////} else ((Component)comp).setVisible(state);
	    } else setVisible(state);
	}
    }


    private void
    setVisibleAmount(Object comp, YoixObject obj) {

	if (comp instanceof JScrollBar)
	    ((JScrollBar)comp).setVisibleAmount(Math.max(obj.intValue(), 1));
    }


    private void
    showMenuBar(JMenuBar menubar, boolean visible) {

	Object  owner;

	if (menubarowner != null) {
	    if ((owner = menubarowner.getManagedObject()) != null) {
		if (owner instanceof YoixInterfaceMenuBar) {
		    ((YoixInterfaceMenuBar)owner).changeMenuBar(
			menubar,
			visible,
			menubarowner.data.getBoolean(N_VALIDATE)
		    );
		}
	    }
	}
    }


    private void
    showPopupMenu(JPopupMenu popupmenu) {

	Object  owner;
	Point   loc;

	if (popupowner != null) {
	    if ((owner = popupowner.getManagedObject()) != null) {
		if (owner instanceof Component) {
		    if (((Component)owner).isShowing()) {
			loc = YoixMakeScreen.javaPoint(data.getObject(N_LOCATION));
			YoixMiscMenu.addListener(popupmenu, popupowner);
			popupmenu.show((Component)owner, loc.x, loc.y);
		    }
		}
	    }
	}
    }


    private void
    syncMenuProperty(Object comp, String name, Object value) {

	YoixBodyComponent  body;
	YoixObject         components;
	YoixObject         container;
	YoixObject         child;
	YoixObject         obj;
	int                m;
	int                n;

	if (comp instanceof JMenuBar || comp instanceof JPopupMenu) {
	    YoixMiscMenu.setNewProperty(comp, name, value);
	    if ((components = data.getObject(N_COMPONENTS)) != null) {
		for (n = 0; n < components.length(); n++) {
		    if (components.defined(n)) {
			child = components.getObject(n);
			//
			// Menu components do not contain the root, so no
			// need to have a test for it.
			//
			comp = child.getManagedObject();
			if (comp instanceof JComponent) {
			    body = (YoixBodyComponent)child.body();
			    obj = body.data.getObject(name);
			    if (obj != null && obj.notNull()) {
				if (((YoixBodyComponent)(child.body())).isContainer()) {
				    container = child.getObject(N_COMPONENTS);
				    //
				    // This loop starts at 0 instead of 1
				    // because the components dictionary
				    // doesn't contain the root.
				    //
				    for (m = 0; m < container.length(); m++)
					n += container.defined(m) ? 1 : 0;
				}
			    } else YoixMiscMenu.setNewProperty(comp, name, value);
			}
		    }
		}
	    }
	}
    }


    private void
    syncPreferredSize(JComponent comp, Dimension size) {

	YoixObject  obj;

	//
	// This did a bit more in older versions, but we've cleaned much
	// our preferredsize code up (here and elsewhere). Changes were
	// added rather late, so be suspicious of them if you notice any
	// sizing problems.
	//
	// Checking N_MINIMUMSIZE when size is null is a recent addition
	// that doesn't seem unreasonable, but it's primarily for an older
	// application that occasionally set minimumsize in JPanels. Turns
	// out that minimumsize (and maximumsize) are not documented even
	// though they're active fields that are currently defined in most
	// of the Yoix Swing components (see YoixModuleSwing.java). Bottom
	// line is that Yoix scripts should not use them and we may change
	// their implementation, or remove them, in future releases.
	//

	if ((obj = data.getObject(N_PREFERREDSIZE)) == null || obj.isNull()) {
	    if (size == null) {		// old application kludge
		if ((obj = data.getObject(N_MINIMUMSIZE)) != null && obj.notNull())
		    size = ((JComponent)comp).getMinimumSize();
	    }
	    if (YoixMisc.notEqual(size, ((JComponent)comp).getPreferredSize())) {
		((JComponent)comp).setPreferredSize(size);
		validateRoot(comp);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixFileFilter extends FileFilter {

	private YoixRERegexp  re;
	private String        desc;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixFileFilter(String desc, YoixRERegexp re) {

	    this.desc = desc;
	    this.re = re;
	}

	///////////////////////////////////
	//
	// YoixFileFilter Methods
	//
	///////////////////////////////////

	public final boolean
	accept(File f) {

	    return(f == null ? false : f.isDirectory() || re.exec(f.getName(), null));
	}


	public final String
	getDescription() {

	    return(desc);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixHTMLEditorKit extends HTMLEditorKit {

	//
	// Needed to make sure only a pure button 1 triggers a link
	// activation.
	//

	LinkMouseAdapter  lma = null;
	MouseListener     ml = null;

	///////////////////////////////////
	//
	// YoixHTMLEditorKit Methods
	//
	///////////////////////////////////

	public final void
	install(JEditorPane c) {

	    EventListener  mls[];
	    int            i;

	    super.install(c);
	    mls = c.getListeners(MouseListener.class);
	    for (i = 0; i < mls.length; i++) {
		if (mls[i] instanceof HTMLEditorKit.LinkController) {
		    ml = (MouseListener)(mls[i]);
		    c.removeMouseListener(ml);
		    break;
		}
	    }
	    c.addMouseListener(lma = new LinkMouseAdapter());
	}


	public final void
	deinstall(JEditorPane c) {

	    c.removeMouseListener(lma);
	    super.deinstall(c);
	    ml = null;
	    lma = null;
	}


	///////////////////////////////////
	//
	// Inner Class
	//
	///////////////////////////////////

	class LinkMouseAdapter extends MouseAdapter {

	    public final void
	    mouseClicked(MouseEvent e) {

		int  modifiers;

		if (ml != null) {
		    modifiers = e.getModifiers();
		    if ((modifiers&YOIX_BUTTON1_MASK) != 0) {
			if ((modifiers&(YOIX_BUTTON2_MASK|YOIX_BUTTON3_MASK)) == 0) {
			    if ((modifiers&YOIX_KEY_MASK) == 0)
				ml.mouseClicked(e);
			}
		    }
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixTextValueChangedListener implements DocumentListener {

	YoixBodyComponentSwing  body;
	JTextComponent          comp;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixTextValueChangedListener(YoixBodyComponentSwing body, JTextComponent comp) {

	    this.body = body;
	    this.comp = comp;
	}

	///////////////////////////////////
	//
	// DocumentListener Methods
	//
	///////////////////////////////////

	public void
	changedUpdate(DocumentEvent e) {

	}


	public void
	insertUpdate(DocumentEvent e) {

	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    YoixObject.newPointer(body),
		    YoixMakeEvent.yoixEvent(e, V_TEXTCHANGED)
		)
	    );
	}


	public void
	removeUpdate(DocumentEvent e) {

	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    YoixObject.newPointer(body),
		    YoixMakeEvent.yoixEvent(e, V_TEXTCHANGED)
		)
	    );
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixPopupMenuListener implements PopupMenuListener {

	YoixBodyComponentSwing  yoixpeer;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixPopupMenuListener(YoixBodyComponentSwing yp) {
	    yoixpeer = yp;
	}

	///////////////////////////////////
	//
	// PopupMenuListener Methods
	//
	///////////////////////////////////

	public void
	popupMenuCanceled(PopupMenuEvent e) {

	}


	public void
	popupMenuWillBecomeInvisible(PopupMenuEvent e) {

	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    yoixpeer,
		    new Object[] {new Integer(RUN_JPOPUPQUIET), e.getSource(), popupowner}
		)
	    );
	}


	public void
	popupMenuWillBecomeVisible(PopupMenuEvent e) {

	}


	protected void
	finalize() {

	    yoixpeer = null;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixInputVerifier extends InputVerifier {

	YoixObject  owner;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixInputVerifier(YoixObject context) {
	    super();
	    owner = context;
	}

	///////////////////////////////////
	//
	// InputVerifier Methods
	//
	///////////////////////////////////

	public boolean
	verify(JComponent input) {

	    YoixObject  verifier;
	    YoixObject  obj;
	    YoixError   error_point = null;
	    boolean     result = true;

	    if (owner.getManagedObject() == input) {
		if ((verifier = data.getObject(N_VERIFIER, null)) != null && verifier.notNull() && verifier.isFunction() && verifier.length() == 0) {
		    try {
			error_point = VM.pushError();
			obj = call(verifier, new YoixObject[] {}, data);
			if (obj != null && obj.notNull() && obj.isInteger())
			    result = obj.booleanValue();
			VM.popError();
		    }
		    catch(YoixError e) {
			if (e != error_point)
			    throw(e);
			else VM.error(error_point);
		    }
		}
	    }
	    return(result);
	}
    }
}

