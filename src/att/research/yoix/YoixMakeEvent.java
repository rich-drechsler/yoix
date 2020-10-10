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
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

public abstract
class YoixMakeEvent

    implements YoixAPI,
	       YoixConstants,
               YoixConstantsJFC,
               YoixConstantsSwing

{

    //
    // These are the methods that we use to translate events back and
    // forth between their Java and Yoix representations. The one that
    // translates from a Java EventObject to a YoixObject is heavily
    // used and very important. The method that translates in the other
    // direction is much less important.
    //

    private static HashMap  eventcache = new HashMap();

    //
    // Stuff that we use to try to improve MouseWheelEvent consistency.
    // Most of this was borrowed from YoixSwingJScrollPane, which has
    // used is for quite some time now.
    //

    private static int THRESHOLD = 7;

    private static Object  wheeleventdata[] = null;
    private static int     nextevent = 0;

    private static long  rolltimes[] = {550, 425};	// trial and error
    private static long  clicktimes[] = {30, 25};	// trial and error
    private static int   threshold;

    static {
	//
	// We eventually may want a more flexible way to set threshold and
	// we also might want a mechanism that give individual components
	// a way to opt-out of the adjustments, but for now we just set to
	// THRESHOLD.
	//
	// Low level mouse wheel scrolling improved in 1.6.0, so we can be
	// more aggressive in how many events we track and consequently how
	// big our adjusted scrollAmount gets.
	//

	if ((threshold = THRESHOLD) > 0) {
	    if (YoixMisc.jvmCompareTo("1.6.0") >= 0)
		wheeleventdata = new Object[5*threshold + 1];
	    else wheeleventdata = new Object[threshold + 5];
	}
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    public static AWTEvent
    javaAWTEvent(YoixObject event, YoixObject target) {

	return(javaAWTEvent(event, target, true));
    }


    public static AWTEvent
    javaAWTEvent(YoixObject event, YoixObject target, boolean wrap) {

	YoixBodyComponent  body;
	EventObject        e = null;
	YoixObject         update;
	Rectangle          rect;
	Component          mousesource;
	Object             source;
	Object             item;
	Object             type;
	Object             value;
	String             desc;
	Point              location;
	URL                url;
	int                id;

	//
	// Main use, even though it's currently called from a few different
	// places, is to create an AWTEvent that EventQueue.postEvent() can
	// work with, so it's not heavily used code. We wrap our answer in
	// a YoixAWTInvocationEvent, which is an AWTEvent, if the event that
	// we would end up with isn't an AWTEvent or wrap is true and target
	// represents a Swing component.
	//

	if (event != null && target != null && target.isComponent()) {
	    body = (YoixBodyComponent)target.body();
	    source = target.getManagedObject();
	    if (source instanceof Component) {
		switch (id = YoixBodyComponent.jfcEventID(event)) {
		    case V_ACTIONPERFORMED:
			e = new ActionEvent(
			    source,
			    id,
			    event.getString(N_COMMAND, ""),
			    event.getInt(N_MODIFIERS, 0)
			);
			break;

		    case V_ADJUSTCHANGED:
			if (!(source instanceof Adjustable))
			    source = body.getAdjustable(event.getInt(N_ORIENTATION, YOIX_VERTICAL));
			if (source instanceof Adjustable) {
			    e = new AdjustmentEvent(
				(Adjustable)source,
				id,
				event.getInt(N_TYPE, YOIX_TRACK),
				event.getInt(N_VALUE, 0)
			    );
			}
			break;

		    case V_CARETUPDATE:		// CaretEvent is abstract
			break;

		    case V_COMPONENTHIDDEN:
		    case V_COMPONENTMOVED:
		    case V_COMPONENTRESIZED:
		    case V_COMPONENTSHOWN:
			e = new ComponentEvent((Component)source, id);
			break;

		    case V_FOCUSGAINED:
		    case V_FOCUSLOST:
			e = new FocusEvent(
			    (Component)source,
			    id,
			    event.getBoolean(N_TEMPORARY)
			);
			break;

		    case V_HYPERLINKACTIVATED:
		    case V_HYPERLINKENTERED:
		    case V_HYPERLINKEXITED:
			if (source instanceof JEditorPane) {
			    if (id == V_HYPERLINKACTIVATED)
				type = HyperlinkEvent.EventType.ACTIVATED;
			    else if (id == V_HYPERLINKENTERED)
				type = HyperlinkEvent.EventType.ENTERED;
			    else type = HyperlinkEvent.EventType.EXITED;
			    desc = event.getString(N_HREF);
			    try {
				url = new URL(desc);
			    }
			    catch(MalformedURLException ex) {
				url = null;
			    }
			    e = new HyperlinkEvent(
				source,
				(HyperlinkEvent.EventType)type,
				url,
				desc
			    );
			}
			break;

		    case V_INVOCATIONACTION:
		    case V_INVOCATIONBROWSE:
		    case V_INVOCATIONCHANGE:
		    case V_INVOCATIONEDIT:
		    case V_INVOCATIONEDITIMPORT:
		    case V_INVOCATIONEDITKEY:
		    case V_INVOCATIONRUN:
		    case V_INVOCATIONSELECTION:
			e = new YoixAWTInvocationEvent(target, event.duplicate(), 0L);
			break;

		    case V_ITEMCHANGED:
			if (source instanceof ItemSelectable) {
			    if ((item = event.getObject(N_ITEM)) == null)
				item = YoixObject.newNull();
			    e = new ItemEvent(
				(ItemSelectable)source,
				id,
				item,
				event.getBoolean(N_STATE) ? ItemEvent.SELECTED : ItemEvent.DESELECTED
			    );
			}
			break;

		    case V_KEYPRESSED:
		    case V_KEYRELEASED:
		    case V_KEYTYPED:
			e = new KeyEvent(
			    (Component)source,
			    id,
			    (long)(1000.0*event.getDouble(N_WHEN, 0)),
			    event.getInt(N_MODIFIERS, 0),
			    event.getInt(N_KEYCODE, 0),
			    (char)event.getInt(N_KEYCHAR, -1)
			);
			break;

		    case V_MOUSECLICKED:
		    case V_MOUSEDRAGGED:
		    case V_MOUSEENTERED:
		    case V_MOUSEEXITED:
		    case V_MOUSEMOVED:
		    case V_MOUSEPRESSED:
		    case V_MOUSERELEASED:
			mousesource = body.getMouseEventSource();	// kludge
			location = body.getMouseEventPoint(event, mousesource);
			e = new MouseEvent(
			    mousesource,
			    id,
			    (long)(1000.0*event.getDouble(N_WHEN, 0)),
			    event.getInt(N_MODIFIERS, 0),
			    location.x,
			    location.y,
			    event.getInt(N_CLICKCOUNT, 1),
			    event.getBoolean(N_POPUPTRIGGER),
			    event.getInt(N_BUTTON, MouseEvent.NOBUTTON)
			);
			break;

		    case V_MOUSEWHEELMOVED:
			mousesource = body.getMouseEventSource();	// kludge
			location = body.getMouseEventPoint(event, mousesource);
			e = new MouseWheelEvent(
			    mousesource,
			    id,
			    (long)(1000.0*event.getDouble(N_WHEN, 0)),
			    event.getInt(N_MODIFIERS, 0),
			    location.x,
			    location.y,
			    event.getInt(N_CLICKCOUNT, 0),
			    event.getBoolean(N_POPUPTRIGGER),
			    event.getInt(N_SCROLLTYPE, YOIX_WHEEL_UNIT_SCROLL),
			    event.getInt(N_SCROLLAMOUNT, 0),
			    event.getInt(N_WHEELROTATION, 0)
			);
			break;

		    case V_PAINTPAINT:
		    case V_PAINTUPDATE:
			update = event.getObject(N_UPDATERECT, null);
			if (update != null && update.notNull())
			    rect = YoixMakeScreen.javaRectangle(update);
		        else rect = new Rectangle(((Component)source).getSize());
			e = new PaintEvent((Component)source, id, rect);
			break;

		    case V_STATECHANGED:
			if (source instanceof AbstractButton)
			    e = new ChangeEvent(source);
			else if (source instanceof JProgressBar)
			    e = new ChangeEvent(source);
			else if (source instanceof JTabbedPane)
			    e = new ChangeEvent(source);
			break;

		    case V_TEXTCHANGED:
			e = new TextEvent(source, id);
			break;

		    case V_VALUECHANGED:
			if (source instanceof JList) {
			    e = new ListSelectionEvent(
				source,
				event.getInt(N_FIRSTINDEX, 0),
				event.getInt(N_LASTINDEX, 0),
				event.getBoolean(N_SEQUENCE, false)
			    );
			} else if (source instanceof JTree) {
			    //
			    // Currently unimplemented, but eventually could
			    // build a TreeSelectionEvent event. We may need
			    // (or want) to reorganize some of the low level
			    // JTree support in YoixBodyComponentSwing.java
			    // before tackling this.
			    //
			}
			break;

		    case V_WINDOWACTIVATED:
		    case V_WINDOWCLOSED:
		    case V_WINDOWCLOSING:
		    case V_WINDOWDEACTIVATED:
		    case V_WINDOWDEICONIFIED:
		    case V_WINDOWICONIFIED:
		    case V_WINDOWOPENED:
			if (source instanceof Window)
			    e = new WindowEvent((Window)source, id);
			else if (source instanceof JInternalFrame)
			    e = new InternalFrameEvent((JInternalFrame)source, javaInternalFrameEventID(id));
			break;
		}
		if (e != null) {
		    if (e instanceof AWTEvent == false || (wrap && source instanceof JComponent)) {
			if (e instanceof InvocationEvent == false)
			    e = new YoixAWTInvocationEvent(target, e);
		    }
		}
	    }
	}
	return((AWTEvent)e);
    }


    public static YoixObject
    yoixEvent(EventObject e, YoixBodyComponent listener) {

	return(yoixEvent(e, YoixBodyComponent.jfcEventID(e), listener));
    }


    public static YoixObject
    yoixEvent(EventObject e, int id, YoixBodyComponent listener) {

	YoixObject  event = null;
	YoixObject  items;
	YoixObject  location;
	InputEvent  trigger;
	TreePath    paths[];
	boolean     state;
	boolean     textpreferable;
	Object      item;
	Object      source;
	Object      value;
	String      command;
	Point       point;
	char        keychar;
	int         clickcount;
	int         eventflags;
	int         wheelrotation;
	int         modifiers;
	int         n;

	//
	// This is an important method that's called whenever we need to
	// convert a Java event to its Yoix representation so it can be
	// handed to a Yoix event handler. The special purpose code that
	// handled InternalFrameEvents has been removed (on 5/17/05), so
	// the caller is now responsible for picking id values that map
	// InternalFrameEvents to WindowEvents.
	//
	// Handling MouseWheelEvents in RootPaneContinaers like a JFrame
	// forced us to introduce a series of MouseEvent kludges to work
	// around an occasional Java StackOverflowError bug. The details
	// are hidden in getMouseEventSource() and getMouseEventPoint()
	// and should be described in YoixBodyComponent.java. We saw the
	// problem using Java 1.4.X and Java 1.5.0, so our kludges will
	// probably be around for a while!!!
	//
	// NOTE - most of the time we don't check to see if the event can
	// be cast to the one we're expecting, which means we assume the
	// caller is well behaved and is handing us event and id arguments
	// that are consistent!!
	//

	if (e != null) {
	    switch (id) {
		case V_ACTIONPERFORMED:
		    event = newEvent(T_ACTIONEVENT, id);
		    event.putInt(N_MODIFIERS, YoixMiscJFC.cookModifiers((ActionEvent)e));
		    event.putString(N_COMMAND, ((ActionEvent)e).getActionCommand());
		    break;

		case V_ADJUSTCHANGED:
		    event = newEvent(T_ADJUSTMENTEVENT, id);
		    event.putInt(N_VALUE, ((AdjustmentEvent)e).getValue());
		    event.putInt(N_TYPE, ((AdjustmentEvent)e).getAdjustmentType());
		    event.putInt(N_ORIENTATION, getOrientation((AdjustmentEvent)e));
		    break;

		case V_CARETUPDATE:
		    event = newEvent(T_CARETEVENT, id);
		    event.putInt(N_DOT, ((CaretEvent)e).getDot());
		    event.putInt(N_MARK, ((CaretEvent)e).getMark());
		    break;

		case V_COMPONENTHIDDEN:
		case V_COMPONENTMOVED:
		case V_COMPONENTRESIZED:
		case V_COMPONENTSHOWN:
		    event = newEvent(T_COMPONENTEVENT, id);
		    break;

		case V_DRAGGESTURERECOGNIZED:
		    event = newDnDEvent(e, id);
		    trigger = ((DragGestureEvent)e).getTriggerEvent();
		    point = getEventPoint(e);
		    location = getLocation(point, event.getObject(N_LOCATION));
		    event.putObject(N_LOCATION, location);
		    event.putObject(N_SCREENLOCATION, getLocationOnScreen(e));
		    event.putObject(N_COORDINATES, getCoordinates(null, listener, point, location));
		    event.putInt(N_DRAGIMAGESUPPORTED, ((DragGestureEvent)e).getDragSource().isDragImageSupported());
		    event.putObject(N_MAXIMUMSIZE, getMaximumSize(1.0/3.0, 1.0/3.0));
		    event.putInt(N_MODIFIERS, YoixMiscJFC.cookModifiers(trigger));
		    event.putInt(N_MODIFIERSDOWN, trigger.getModifiersEx());
		    event.putDouble(N_WHEN, trigger.getWhen()/1000.0);
		    break;

		case V_DRAGDROPEND:
		    event = newDnDEvent(e, id);
		    point = getEventPoint(e);
		    location = getLocation(point, event.getObject(N_LOCATION));
		    event.putObject(N_ACTION, getAction(e));
		    event.putObject(N_LOCATION, location);
		    event.putObject(N_SCREENLOCATION, getLocationOnScreen(e));
		    event.putObject(N_COORDINATES, getCoordinates(null, listener, point, location));
		    event.putInt(N_SUCCEEDED, ((DragSourceDropEvent)e).getDropSuccess());
		    break;

		case V_DRAGENTER:
		case V_DRAGMOUSEMOVED:
		case V_DRAGOVER:
		case V_DROPACTIONCHANGED:
		    event = newDnDEvent(e, id);
		    source = e.getSource();
		    if (source instanceof DropTarget) {
			textpreferable = (((DropTarget)source).getComponent() instanceof JTextComponent);
		    } else textpreferable = false;
		    point = getEventPoint(e);
		    location = getLocation(point, event.getObject(N_LOCATION));
		    event.putObject(N_ACTION, getAction(e));
		    event.putObject(N_LOCATION, location);
		    event.putObject(N_SCREENLOCATION, getLocationOnScreen(e));
		    event.putObject(N_COORDINATES, getCoordinates(null, listener, point, location));
		    if (e instanceof DragSourceDragEvent)
			event.putObject(N_CURSOR, listener != null ? listener.getDragCursor() : null);
		    if (e instanceof DropTargetDragEvent) {
			event.putObject(N_MIMETYPES, YoixMake.yoixMimeTypes(
			    ((DropTargetDragEvent)e).getCurrentDataFlavors()
			));
			//
			// This assumes 1.5.0, so we use reflection.
			//
			event.putObject(N_TRANSFERABLE, YoixDataTransfer.yoixTransferable(
			    (Transferable)YoixReflect.invoke((DropTargetDragEvent)e, "getTransferable"),
			    textpreferable
			));
		    }
		    if (e instanceof DropTargetEvent)
			event.putInt(N_DRAGOWNER, listener.getDragStarted());
		    break;

		case V_DRAGEXIT:
		    event = newDnDEvent(e, id);
		    point = getEventPoint(e);
		    location = getLocation(point, event.getObject(N_LOCATION));
		    event.putObject(N_ACTION, getAction(e));
		    event.putObject(N_LOCATION, location);
		    event.putObject(N_SCREENLOCATION, getLocationOnScreen(e));
		    event.putObject(N_COORDINATES, getCoordinates(null, listener, point, location));
		    if (e instanceof DragSourceEvent)
			event.putObject(N_CURSOR, listener != null ? listener.getDragCursor() : null);
		    break;

		case V_DROP:
		    event = newDnDEvent(e, id);
		    source = e.getSource();
		    if (source instanceof DropTarget) {
			textpreferable = (((DropTarget)source).getComponent() instanceof JTextComponent);
		    } else textpreferable = false;
		    point = getEventPoint(e);
		    location = getLocation(point, event.getObject(N_LOCATION));
		    event.putObject(N_ACTION, getAction(e));
		    event.putObject(N_LOCATION, location);
		    event.putObject(N_SCREENLOCATION, getLocationOnScreen(e));
		    event.putObject(N_COORDINATES, getCoordinates(null, listener, point, location));
		    event.putObject(N_TRANSFERABLE, YoixDataTransfer.yoixTransferable(((DropTargetDropEvent)e).getTransferable(), textpreferable));
		    break;

		case V_FOCUSGAINED:
		case V_FOCUSLOST:
		    event = newEvent(T_FOCUSEVENT, id);
		    event.putInt(N_TEMPORARY, ((FocusEvent)e).isTemporary());
		    break;

		case V_HYPERLINKACTIVATED:
		case V_HYPERLINKENTERED:
		case V_HYPERLINKEXITED:
		    event = newEvent(T_HYPERLINKEVENT, id);
		    event.putString(N_HREF, ((HyperlinkEvent)e).getDescription());
		    break;

		case V_INVOCATIONACTION:
		case V_INVOCATIONBROWSE:
		case V_INVOCATIONCHANGE:
		case V_INVOCATIONEDIT:
		case V_INVOCATIONEDITIMPORT:
		case V_INVOCATIONEDITKEY:
		case V_INVOCATIONRUN:
		case V_INVOCATIONSELECTION:
		    event = newEvent(T_INVOCATIONEVENT, id);
		    break;

		case V_ITEMCHANGED:
		    event = newEvent(T_ITEMEVENT, id);
		    state = (((ItemEvent)e).getStateChange() == ItemEvent.SELECTED);
		    source = ((ItemEvent)e).getSource();
		    item = ((ItemEvent)e).getItem();
		    event.putInt(N_STATE, state);
		    if ((item instanceof YoixObject) == false) {
			if (source instanceof MenuItem) {
			    command = ((MenuItem)source).getActionCommand();
			    if (command == null)
				command = item + "";
			    item = YoixObject.newString(command);
			} else if (source instanceof JMenuItem) {
			    command = ((JMenuItem)source).getActionCommand();
			    if (command == null)
				command = item + "";
			    item = YoixObject.newString(command);
			} else if (item instanceof Integer)
			    item = YoixObject.newInt(((Integer)item).intValue());
			else if (item instanceof Number)
			    item = YoixObject.newDouble(((Number)item).doubleValue());
			else item = YoixObject.newString(item + "");
		    }
		    event.putObject(N_ITEM, (YoixObject)item);
		    break;

		case V_KEYPRESSED:
		case V_KEYRELEASED:
		case V_KEYTYPED:
		    event = newEvent(T_KEYEVENT, id);
		    keychar = ((KeyEvent)e).getKeyChar();
		    event.putInt(N_KEYCHAR, keychar != KeyEvent.CHAR_UNDEFINED ? keychar : -1);
		    event.putInt(N_KEYCODE, ((KeyEvent)e).getKeyCode());
		    event.putString(N_KEYSTRING, keychar != KeyEvent.CHAR_UNDEFINED ? "" + keychar : "");
		    event.putInt(N_MODIFIERS, YoixMiscJFC.cookModifiers((KeyEvent)e));
		    event.putInt(N_MODIFIERSDOWN, ((KeyEvent)e).getModifiersEx());
		    event.putDouble(N_WHEN, ((KeyEvent)e).getWhen()/1000.0);
		    break;

		case V_MOUSECLICKED:
		case V_MOUSEDRAGGED:
		case V_MOUSEENTERED:
		case V_MOUSEEXITED:
		case V_MOUSEMOVED:
		case V_MOUSEPRESSED:
		case V_MOUSERELEASED:
		    event = newEvent(T_MOUSEEVENT, id);
		    modifiers = YoixMiscJFC.cookModifiers((MouseEvent)e);
		    point = listener.getMouseEventPoint((MouseEvent)e);		// kludge
		    location = YoixMakeScreen.yoixPoint(point, event.getObject(N_LOCATION));
		    event.putObject(N_LOCATION, location);
		    event.putObject(N_COORDINATES, getCoordinates((MouseEvent)e, listener, point, location));
		    event.putInt(N_MODIFIERS, modifiers);
		    event.putInt(N_BUTTON, ((MouseEvent)e).getButton());
		    event.putInt(N_MODIFIERSDOWN, ((MouseEvent)e).getModifiersEx());
		    event.putInt(N_PRESSED, YoixMiscJFC.getButtonsPressed((MouseEvent)e));
		    event.putInt(N_POPUPTRIGGER, ((MouseEvent)e).isPopupTrigger());
		    event.putInt(N_CLICKCOUNT, ((MouseEvent)e).getClickCount());
		    event.putDouble(N_WHEN, ((MouseEvent)e).getWhen()/1000.0);
		    break;

		case V_MOUSEWHEELMOVED:
		    event = newEvent(T_MOUSEWHEELEVENT, id);
		    eventflags = listener.getEventFlags();
		    modifiers = YoixMiscJFC.cookModifiers((MouseWheelEvent)e);
		    point = listener.getMouseEventPoint((MouseWheelEvent)e);		// kludge
		    location = YoixMakeScreen.yoixPoint(point, event.getObject(N_LOCATION));
		    wheelrotation = ((MouseWheelEvent)e).getWheelRotation();
		    clickcount = ((MouseWheelEvent)e).getClickCount();

		    //
		    // We saw non-zero clickcounts on Linux systems that seemed
		    // to record the number of mouse wheel clicks, however we
		    // didn't see the same thing on other systems. Instead the
		    // mouse wheel clicks were recorded in wheelrotation and
		    // clickcount was zero. We decided that we needed to try
		    // for consistent behavior, which is why we adjust some of
		    // the MouseWheelEvent values.
		    //
		    if (clickcount > 1) {
			if (Math.abs(wheelrotation) <= 1)
			    wheelrotation *= clickcount;
		    }

		    event.putObject(N_LOCATION, location);
		    event.putObject(N_COORDINATES, getCoordinates((MouseWheelEvent)e, listener, point, location));
		    event.putInt(N_MODIFIERS, modifiers);
		    event.putInt(N_MODIFIERSDOWN, ((MouseWheelEvent)e).getModifiersEx());
		    event.putInt(N_PRESSED, YoixMiscJFC.getButtonsPressed((MouseWheelEvent)e));
		    event.putInt(N_POPUPTRIGGER, ((MouseWheelEvent)e).isPopupTrigger());
		    event.putInt(N_CLICKCOUNT, clickcount);
		    event.putDouble(N_WHEN, ((MouseWheelEvent)e).getWhen()/1000.0);
		    event.putInt(N_SCROLLTYPE, ((MouseWheelEvent)e).getScrollType());
		    event.putInt(N_SCROLLAMOUNT, ((MouseWheelEvent)e).getScrollAmount());
		    event.putInt(N_WHEELROTATION, wheelrotation);
		    event.putInt(N_UNITSTOSCROLL, pickUnitsToScroll((MouseWheelEvent)e, eventflags));
		    event.putDouble(N_WHENNEXT, getWhenNext((MouseWheelEvent)e)/1000.0);
		    break;

		case V_STATECHANGED:
		    event = newEvent(T_CHANGEEVENT, id);
		    break;

		case V_TEXTCHANGED:
		    event = newEvent(T_TEXTEVENT, id);
		    break;

		case V_VALUECHANGED:
		    if (e instanceof ListSelectionEvent) {
			event = newEvent(T_LISTSELECTIONEVENT, id);
			event.putInt(N_FIRSTINDEX, ((ListSelectionEvent)e).getFirstIndex());
			event.putInt(N_LASTINDEX, ((ListSelectionEvent)e).getLastIndex());
			event.putInt(N_SEQUENCE, ((ListSelectionEvent)e).getValueIsAdjusting());
		    } else if (e instanceof TreeSelectionEvent) {
			event = newEvent(T_TREESELECTIONEVENT, id);
			paths = ((TreeSelectionEvent)e).getPaths();
			items = YoixObject.newArray(2*paths.length);
			for (n = 0; n < paths.length; n++) {
			    items.putObject(2*n, YoixMake.yoixJTreeNode(paths[n].getLastPathComponent()));
			    items.putInt(2*n+1, ((TreeSelectionEvent)e).isAddedPath(paths[n]));
			}
			event.putObject(N_ITEMS, items);
		    } else event = YoixObject.newNull(T_EVENT);
		    break;

		case V_WINDOWACTIVATED:
		case V_WINDOWCLOSED:
		case V_WINDOWCLOSING:
		case V_WINDOWDEACTIVATED:
		case V_WINDOWDEICONIFIED:
		case V_WINDOWICONIFIED:
		case V_WINDOWOPENED:
		    event = newEvent(T_WINDOWEVENT, id);
		    break;
	    }
	}
	return(event != null ? event : YoixObject.newNull(T_EVENT));
    }


    public static YoixObject
    yoixEvent(Object e, int id) {

	YoixObject  event = null;
	int         length = -1;
	int         offset = -1;
	int         size = -1;

	if (e != null) {
	    switch (id) {
		case V_TEXTCHANGED:
		    event = newEvent(T_TEXTEVENT, id);
		    if (e instanceof DocumentEvent) {
			if (((DocumentEvent)e).getType() == DocumentEvent.EventType.INSERT) {
			    event.putInt(N_TYPE, YOIX_TEXTINSERT);
			    size = ((DocumentEvent)e).getDocument().getLength();
			    offset = ((DocumentEvent)e).getOffset();
			    length = ((DocumentEvent)e).getLength();
			} else if (((DocumentEvent)e).getType() == DocumentEvent.EventType.REMOVE) {
			    event.putInt(N_TYPE, YOIX_TEXTREMOVE);
			    //
			    // In some cases, found that offset+length > size, so we do:
			    //
			    size = ((DocumentEvent)e).getDocument().getLength();
			    offset = Math.max(size, ((DocumentEvent)e).getOffset());
			    length = Math.min(size-offset, ((DocumentEvent)e).getLength());
			} else VM.abort(INTERNALERROR);		// should never happen

			event.putInt(N_LENGTH, length);
			event.putInt(N_OFFSET, offset);
			event.putInt(N_SIZE, size);
		    }
		    break;
	    }
	}
	return(event != null ? event : YoixObject.newNull(T_EVENT));
    }

    ///////////////////////////////////
    //
    // YoixMakeEvent Methods
    //
    ///////////////////////////////////

    static void
    setConsumed(YoixObject event) {

	setConsumed(event, VM.getInt(N_EVENTFLAGS));
    }


    static void
    setConsumed(YoixObject event, int flags) {

	//
	// Added very quickly (on 6/22/11) so it really needs a close look.
	// Right now the interpretation of bits in flags are undocumented,
	// so that's also something that eventually should be addressed.
	//
	// NOTE - probably only reliable if VM.eventflags is set using the
	// undocumented --eventflags command line option.
	//

	if (event != null && (flags & 0x1E) != 0) {
	    if (event.defined(N_CONSUMED)) {
		switch (YoixBodyComponent.jfcEventID(event)) {
		    case V_MOUSEWHEELMOVED:
			event.putInt(N_CONSUMED, (flags&0x02) != 0);
			break;

		    case V_KEYPRESSED:
			event.putInt(N_CONSUMED, (flags&0x04) != 0);
			break;

		    case V_KEYRELEASED:
			event.putInt(N_CONSUMED, (flags&0x08) != 0);
			break;

		    case V_KEYTYPED:
			event.putInt(N_CONSUMED, (flags&0x10) != 0);
			break;
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private static YoixObject
    getAction(EventObject e) {

	int  action;

	if (e instanceof DragGestureEvent)
	    action = ((DragGestureEvent)e).getDragAction();
	else if (e instanceof DragSourceDragEvent)
	    action = ((DragSourceDragEvent)e).getUserAction();
	else if (e instanceof DragSourceDropEvent)
	    action = ((DragSourceDropEvent)e).getDropAction();
	else if (e instanceof DropTargetDragEvent)
	    action = ((DropTargetDragEvent)e).getDropAction();
	else if (e instanceof DropTargetDropEvent)
	    action = ((DropTargetDropEvent)e).getDropAction();
	else action = DnDConstants.ACTION_NONE;

	switch (action) {
	    case DnDConstants.ACTION_COPY:
		action = YOIX_COPY;
		break;

	    case DnDConstants.ACTION_COPY_OR_MOVE:
		action = YOIX_COPY_OR_MOVE;
		break;

	    case DnDConstants.ACTION_MOVE:
		action = YOIX_MOVE;
		break;

	    case DnDConstants.ACTION_LINK:
		action = YOIX_LINK;
		break;

	    default:
		action = YOIX_NONE;
		break;
	}
	return(YoixObject.newInt(action));
    }


    private static YoixObject
    getCoordinates(AWTEvent e, YoixBodyComponent listener, Point point, YoixObject location) {

	YoixBodyMatrix  matrix;
	YoixObject      coordinates;

        if (listener != null) {
	    if ((coordinates = listener.eventCoordinates(e)) == null) {
		if (point != null) {
		    if ((matrix = listener.getCTMBody()) != null)
			coordinates = YoixMake.yoixPoint(point, matrix);
		    else coordinates = location;
		} else coordinates = YoixObject.newPoint();
	    }
	} else coordinates = location;
	return(coordinates);
    }


    private static Point
    getEventPoint(EventObject e) {

	Object  source;
	Point   corner;
	Point   point;

	//
	// MouseEvents are only included for completeness, but that doesn't
	// mean they should be using this method!!
	//

	if (e instanceof DragSourceEvent) {
	    point = ((DragSourceEvent)e).getLocation();
	    source = e.getSource();
	    if (source instanceof DragSourceContext)
		source = ((DragSourceContext)source).getComponent();
	    if (source instanceof Component) {
		try {
		    corner = ((Component)source).getLocationOnScreen();
		    point = new Point(point);	// don't change event's point
		    point.translate(-corner.x, -corner.y);
		}
		catch(IllegalComponentStateException ex) {}
	    }
	} else if (e instanceof DropTargetDragEvent)
	    point = ((DropTargetDragEvent)e).getLocation();
	else if (e instanceof DropTargetDropEvent)
	    point = ((DropTargetDropEvent)e).getLocation();
	else if (e instanceof DragGestureEvent)
	    point = ((DragGestureEvent)e).getDragOrigin();
	else if (e instanceof MouseEvent)
	    point = ((MouseEvent)e).getPoint();
	else point = (Point)YoixReflect.invoke(e, "getLocation");

	return(point);
    }


    private static YoixObject
    getLocation(Point point, YoixObject location) {

	if (point != null)
	    location = YoixMakeScreen.yoixPoint(point, location);
	else location = YoixObject.newPoint();
	return(location);
    }


    private static YoixObject
    getLocationOnScreen(EventObject e) {

	Object  source;
	Point   corner;
	Point   point;

	if (!(e instanceof DragSourceEvent)) {
	    if (e instanceof DropTargetDragEvent)
		point = ((DropTargetDragEvent)e).getLocation();
	    else if (e instanceof DropTargetDropEvent)
		point = ((DropTargetDropEvent)e).getLocation();
	    else if (e instanceof DragGestureEvent)
		point = ((DragGestureEvent)e).getDragOrigin();
	    else if (e instanceof MouseEvent)
		point = ((MouseEvent)e).getPoint();
	    else point = (Point)YoixReflect.invoke(e, "getLocation");
	    if (point != null) {
		if (e instanceof DropTargetEvent)
		    source = ((DropTargetEvent)e).getDropTargetContext().getComponent();
		else if (e instanceof DragGestureEvent)
		    source = ((DragGestureEvent)e).getComponent();
		else source = e.getSource();
		if (source instanceof Component) {
		    try {
			corner = ((Component)source).getLocationOnScreen();
			point = new Point(point);	// don't change event's point
			point.translate(corner.x, corner.y);
		    }
		    catch(IllegalComponentStateException ex) {}
		}
	    }
	} else point = (Point)YoixReflect.invoke(e, "getLocation");

	return(point != null ? YoixMakeScreen.yoixPoint(point) : null);
    }


    private static YoixObject
    getMaximumSize(double sx, double sy) {

	YoixObject  obj = null;
	Dimension   size;

	if ((size = YoixAWTToolkit.getScreenSize()) != null) {
	    size.width = (int)(sx*size.width);
	    size.height = (int)(sy*size.height);
	    obj = YoixMakeScreen.yoixDimension(size);
	}
	return(obj);
    }


    private static int
    getOrientation(AdjustmentEvent e) {

	Adjustable  adjustable;
	int         orientation;

	if ((adjustable = e.getAdjustable()) != null) {
	    switch (adjustable.getOrientation()) {
		case Adjustable.HORIZONTAL:
		    orientation = YOIX_HORIZONTAL;
		    break;

		case Adjustable.VERTICAL:
		    orientation = YOIX_VERTICAL;
		    break;

		default:
		    orientation = YOIX_NONE;
		    break;
	    }
	} else orientation = YOIX_NONE;

	return(orientation);
    }


    private static double
    getWhenNext(MouseWheelEvent e) {

	EventQueue  queue;
	AWTEvent    next;
	double      time = 0;

	//
	// Right now we only handle MouseWheelEvents and we don't bother
	// comparing sources when we find queued event because there's a
	// good chance they won't match and there's currently no way to
	// ask peekEvent() to look for MouseWheelEvents that belong to a
	// particular source.
	//

	if ((queue = YoixAWTToolkit.getSystemEventQueue()) != null) {
	    if ((next = queue.peekEvent(((InputEvent)e).getID())) != null)
		time = ((MouseWheelEvent)next).getWhen();
	}
	return(time);
    }


    private static int
    javaInternalFrameEventID(int id) {

	switch (id) {
	    case V_WINDOWACTIVATED:
		id = InternalFrameEvent.INTERNAL_FRAME_ACTIVATED;
		break;

	    case V_WINDOWCLOSED:
		id = InternalFrameEvent.INTERNAL_FRAME_CLOSED;
		break;

	    case V_WINDOWCLOSING:
		id = InternalFrameEvent.INTERNAL_FRAME_CLOSING;
		break;

	    case V_WINDOWDEACTIVATED:
		id = InternalFrameEvent.INTERNAL_FRAME_DEACTIVATED;
		break;

	    case V_WINDOWDEICONIFIED:
		id = InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED;
		break;

	    case V_WINDOWICONIFIED:
		id = InternalFrameEvent.INTERNAL_FRAME_ICONIFIED;
		break;

	    case V_WINDOWOPENED:
		id = InternalFrameEvent.INTERNAL_FRAME_OPENED;
		break;
	}
	return(id);
    }


    private static YoixObject
    newDnDEvent(EventObject e, int id) {

	String  name;

	if (e instanceof DragSourceEvent)
	    name = T_DRAGSOURCEEVENT;
	else if (e instanceof DropTargetEvent)
	    name = T_DROPTARGETEVENT;
	else if (e instanceof DragGestureEvent)
	    name = T_DRAGGESTUREEVENT;
	else name = T_DRAGSOURCEEVENT;

	return(newEvent(name, id));
    }


    private static YoixObject
    newEvent(String name, int id) {

	YoixObject  obj;
	String      key;

	//
	// In case you're wondering we actually did try using a static
	// HashMap that we passed to duplicate(), but synchronization
	// and clearing of that HashMap seemed to hurt performance so
	// we suspect that allocating a new HashMap each time probably
	// is best.
	//

	key = name + ":" + id;

	if ((obj = (YoixObject)eventcache.get(key)) == null) {
	    if ((obj = YoixMake.yoixType(name)) != null) {
		obj.putInt(N_ID, id);
		synchronized(eventcache) {
		    eventcache.put(key, obj);
		}
	    } else VM.abort(BADTYPENAME, name);		// should be impossible
	}
	return(obj.duplicate(new HashMap()));
    }


    private static int
    pickUnitsToScroll(MouseWheelEvent e, int flags) {

	long  event[];
	long  lastevent[];
	long  clicktime;
	long  rolltime;
	long  delta;
	int   scrollamount;
	int   rotation;
	int   unitstoscroll;
	int   length;
	int   index;
	int   count;
	int   subtotal;

	//
	// The implementation of this method in release 2.2.0 save event e
	// in an array named wheelevents[] which meant the source stored in
	// e could be referenced for a while after being disposed. It wasn't
	// a permanent memeory leak, but could delay garbage collection of
	// components that processed MouseWheelEvents. The fix changed the
	// wheelevents[] array to the wheeleventdata[] Object array and each
	// element in wheeleventdata[] is a long array that stores the data
	// (rotation direction and time) that our algorthim needs. Since we
	// don't reference the event's source we no longer delay the garbage
	// collection of components that receive MouseWheelEvents. Change
	// was added on 9/19/08.
	//

	scrollamount = e.getScrollAmount();

	if ((flags & 0x01) != 0) {
	    rotation = (e.getWheelRotation() >= 0) ? 1 : -1;
	    if (wheeleventdata != null) {
		if ((length = wheeleventdata.length) > 1) {
		    synchronized(wheeleventdata) {
			index = nextevent;
			wheeleventdata[index] = new long[] {e.getWheelRotation(), e.getWhen()};
			nextevent = (nextevent + 1)%length;
			clicktime = clicktimes[rotation < 0 ? 0 : 1];
			rolltime = rolltimes[rotation < 0 ? 0 : 1];
			lastevent = (long[])wheeleventdata[index];
			subtotal = 0;
			for (count = 0; index != nextevent; count++, subtotal++, index--) {
			    if ((event = (long[])wheeleventdata[index]) != null) {
				if (rotation*event[0] > 0) {
				    delta = lastevent[1] - ((long[])wheeleventdata[index])[1];
				    if (delta > clicktime) {
					if (subtotal >= threshold/2 || count < threshold) {
					    if (delta < rolltime)
						subtotal = 0;
					    else break;
					} else break;
				    }
				    lastevent = event;
				    if (index == 0)
					index = length;
				} else break;
			    } else break;
			}
			if (count <= threshold)
			    scrollamount = 1;
			else scrollamount = count - (threshold - 1);
		    }
		} else scrollamount = 1;
	    }
	} else rotation = e.getWheelRotation();

	return(rotation*scrollamount);
    }
}

