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
import java.awt.geom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

public
class YoixBodyComponentAWT extends YoixBodyComponent

    implements YoixAPI,
	       YoixAPIProtected

{

    private YoixBodyComponent  menubarowner = null;
    private YoixBodyComponent  popupowner = null;
    private boolean            disposable = false;
    private Frame              awtparent = null;	// faked for dialog or window

    //
    // Translator support
    //

    private Hashtable  translator = null;
    private String     labels[] = null;
    private String     mappings[] = null;

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
    // The activefields HashMap translates the field names in data
    // that are active (i.e. they trigger action on get() and put())
    // to integers that can be used to quickly select the appropriate
    // action in a switch statement.
    //

    private static HashMap  activefields = new HashMap(110);

    static {
	activefields.put(N_AFTERLOAD, new Integer(V_AFTERLOAD));
	activefields.put(N_ALIGNMENT, new Integer(V_ALIGNMENT));
	activefields.put(N_ANCHOR, new Integer(V_ANCHOR));
	activefields.put(N_BACKGROUND, new Integer(V_BACKGROUND));
	activefields.put(N_BACKGROUNDHINTS, new Integer(V_BACKGROUNDHINTS));
	activefields.put(N_BACKGROUNDIMAGE, new Integer(V_BACKGROUNDIMAGE));
	activefields.put(N_BLOCKINCREMENT, new Integer(V_BLOCKINCREMENT));
	activefields.put(N_BORDER, new Integer(V_BORDER));
	activefields.put(N_BORDERCOLOR, new Integer(V_BORDERCOLOR));
	activefields.put(N_CARET, new Integer(V_CARET));
	activefields.put(N_CELLSIZE, new Integer(V_CELLSIZE));
	activefields.put(N_CLICKRADIUS, new Integer(V_CLICKRADIUS));
	activefields.put(N_COLUMNS, new Integer(V_COLUMNS));
	activefields.put(N_COMMAND, new Integer(V_COMMAND));
	activefields.put(N_COMPRESSEVENTS, new Integer(V_COMPRESSEVENTS));
	activefields.put(N_CURSOR, new Integer(V_CURSOR));
	activefields.put(N_DECORATIONSTYLE, new Integer(V_DECORATIONSTYLE));
	activefields.put(N_DIRECTORY, new Integer(V_DIRECTORY));
	activefields.put(N_DISPLAYED, new Integer(V_DISPLAYED));
	activefields.put(N_DISPOSE, new Integer(V_DISPOSE));
	activefields.put(N_ECHO, new Integer(V_ECHO));
	activefields.put(N_EDIT, new Integer(V_EDIT));
	activefields.put(N_ENABLED, new Integer(V_ENABLED));
	activefields.put(N_EXTENT, new Integer(V_EXTENT));
	activefields.put(N_FILE, new Integer(V_FILE));
	activefields.put(N_FILTERS, new Integer(V_FILTERS));
	activefields.put(N_FIRSTFOCUS, new Integer(V_FIRSTFOCUS));
	activefields.put(N_FOCUSABLE, new Integer(V_FOCUSABLE));
	activefields.put(N_FOCUSOWNER, new Integer(V_FOCUSOWNER));
	activefields.put(N_FONT, new Integer(V_FONT));
	activefields.put(N_FOREGROUND, new Integer(V_FOREGROUND));
	activefields.put(N_FRONTTOBACK, new Integer(V_FRONTTOBACK));
	activefields.put(N_FULLSCREEN, new Integer(V_FULLSCREEN));
	activefields.put(N_GETENABLED, new Integer(V_GETENABLED));
	activefields.put(N_GETSTATE, new Integer(V_GETSTATE));
	activefields.put(N_GRAPHICS, new Integer(V_GRAPHICS));
	activefields.put(N_GROUP, new Integer(V_GROUP));
	activefields.put(N_HIGHLIGHTED, new Integer(V_HIGHLIGHTED));
	activefields.put(N_HTML, new Integer(V_HTML));
	activefields.put(N_ICONIFIED, new Integer(V_ICONIFIED));
	activefields.put(N_INDEX, new Integer(V_INDEX));
	activefields.put(N_INPUTFILTER, new Integer(V_INPUTFILTER));
	activefields.put(N_INSETS, new Integer(V_INSETS));
	activefields.put(N_IPAD, new Integer(V_IPAD));
	activefields.put(N_ITEMS, new Integer(V_ITEMS));
	activefields.put(N_LABELS, new Integer(V_LABELS));
	activefields.put(N_LAYOUT, new Integer(V_LAYOUT));
	activefields.put(N_LAYOUTMANAGER, new Integer(V_LAYOUTMANAGER));
	activefields.put(N_LOCATION, new Integer(V_LOCATION));
	activefields.put(N_MAPPINGS, new Integer(V_MAPPINGS));
	activefields.put(N_MAXIMIZED, new Integer(V_MAXIMIZED));
	activefields.put(N_MAXIMUM, new Integer(V_MAXIMUM));
	activefields.put(N_MENUBAR, new Integer(V_MENUBAR));
	activefields.put(N_MINIMUM, new Integer(V_MINIMUM));
	activefields.put(N_MODAL, new Integer(V_MODAL));
	activefields.put(N_MODE, new Integer(V_MODE));
	activefields.put(N_MULTIPLEMODE, new Integer(V_MULTIPLEMODE));
	activefields.put(N_NEXTCARD, new Integer(V_NEXTCARD));
	activefields.put(N_ORIENTATION, new Integer(V_ORIENTATION));
	activefields.put(N_ORIGIN, new Integer(V_ORIGIN));
	activefields.put(N_OUTPUTFILTER, new Integer(V_OUTPUTFILTER));
	activefields.put(N_PAINT, new Integer(V_PAINT));
	activefields.put(N_PARENT, new Integer(V_PARENT));
	activefields.put(N_POPUP, new Integer(V_POPUP));
	activefields.put(N_PREFERREDSIZE, new Integer(V_PREFERREDSIZE));
	activefields.put(N_PROMPT, new Integer(V_PROMPT));
	activefields.put(N_REPAINT, new Integer(V_REPAINT));
	activefields.put(N_REQUESTFOCUS, new Integer(V_REQUESTFOCUS));
	activefields.put(N_RESIZABLE, new Integer(V_RESIZABLE));
	activefields.put(N_ROOT, new Integer(V_ROOT));
	activefields.put(N_ROWPROPERTIES, new Integer(V_ROWPROPERTIES));
	activefields.put(N_ROWS, new Integer(V_ROWS));
	activefields.put(N_SAVEGRAPHICS, new Integer(V_SAVEGRAPHICS));
	activefields.put(N_SAVELINES, new Integer(V_SAVELINES));
	activefields.put(N_SCREEN, new Integer(V_SCREEN));
	activefields.put(N_SELECTED, new Integer(V_SELECTED));
	activefields.put(N_SELECTEDENDS, new Integer(V_SELECTEDENDS));
	activefields.put(N_SETENABLED, new Integer(V_SETENABLED));
	activefields.put(N_SETSTATE, new Integer(V_SETSTATE));
	activefields.put(N_SETVALUES, new Integer(V_SETVALUES));
	activefields.put(N_SHAPE, new Integer(V_SHAPE));
	activefields.put(N_SHOWING, new Integer(V_SHOWING));
	activefields.put(N_SIZE, new Integer(V_SIZE));
	activefields.put(N_STATE, new Integer(V_STATE));
	activefields.put(N_SYNCCOUNT, new Integer(V_SYNCCOUNT));
	activefields.put(N_SYNCVIEWPORT, new Integer(V_SYNCVIEWPORT));
	activefields.put(N_TAG, new Integer(V_TAG));
	activefields.put(N_TEXT, new Integer(V_TEXT));
	activefields.put(N_TEXTMODE, new Integer(V_TEXTMODE));
	activefields.put(N_TEXTWRAP, new Integer(V_TEXTWRAP));
	activefields.put(N_TITLE, new Integer(V_TITLE));
	activefields.put(N_UNITINCREMENT, new Integer(V_UNITINCREMENT));
	activefields.put(N_VALIDATE, new Integer(V_VALIDATE));
	activefields.put(N_VALUE, new Integer(V_VALUE));
	activefields.put(N_VIEWPORT, new Integer(V_VIEWPORT));
	activefields.put(N_VISIBLE, new Integer(V_VISIBLE));
	activefields.put(N_VISIBLEAMOUNT, new Integer(V_VISIBLEAMOUNT));
	activefields.put(N_VISITCOLOR, new Integer(V_VISITCOLOR));
    }

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixBodyComponentAWT(YoixObject data) {

	//
	// We now try to add event listeners before setting fields that
	// might trigger calls to Yoix event handlers. May not be 100%,
	// but it's an improvement.
	//

	super(data);
	translator = null;
	buildComponent();
	addAllListeners();
	setFixedSize();
	setPermissions(permissions);

	//
	// These were recently moved out of buildComponent().
	//
	setField(N_REQUESTFOCUS);
	setField(N_FOCUSABLE);
	setField(N_VISIBLE);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceListener Methods
    //
    ///////////////////////////////////

    public void
    itemStateChanged(ItemEvent e) {

	Object  source;

	//
	// N_MENUFLAGS is an integer that can be used, among other things,
	// to exercise control over the handling of ItemEvents generated
	// when checkboxes are grouped together in a menu.
	//

	if ((data.getInt(N_MENUFLAGS, VM.getInt(N_MENUFLAGS)) & 0x01) == 0x01) {
	    //
	    // Omit DESELECTED when menu items are grouped.
	    //
	    source = e.getSource();
	    if (source instanceof YoixAWTCheckboxMenuItem) {
		if (((YoixAWTCheckboxMenuItem)source).getGroup() != null) {
		    if (e.getStateChange() == ItemEvent.DESELECTED)
			e = null;
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

	YoixObject  obj;
	Object      comp;

	comp = this.peer;		// snapshot - just to be safe

	switch (activeField(name, activefields)) {
	    case V_GETENABLED:
		obj = builtinGetEnabled(comp, name, argv);
		break;

	    case V_GETSTATE:
		obj = builtinGetState(comp, name, argv);
		break;

	    case V_HTML:
		obj = builtinHTML(comp, name, argv);
		break;

	    case V_REPAINT:
		obj = builtinRepaint(comp, name, argv);
		break;

	    case V_SETENABLED:
		obj = builtinSetEnabled(comp, name, argv);
		break;

	    case V_SETSTATE:
		obj = builtinSetState(comp, name, argv);
		break;

	    case V_SETVALUES:
		obj = builtinSetValues(comp, name, argv);
		break;

	    case V_VALUE:
		obj = builtinValue(comp, name, argv);
		break;

	    default:
		obj = null;
		break;
	}

	return(obj);
    }


    protected YoixObject
    getField(String name, YoixObject obj) {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	switch (activeField(name, activefields)) {
	    case V_BACKGROUND:
		obj = getBackground(comp, obj);
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

	    case V_CELLSIZE:
		obj = getCellSize(comp, obj);
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

	    case V_DISPLAYED:
		obj = getDisplayed(comp, obj);
		break;

	    case V_DISPOSE:
		obj = getDispose(comp, obj);	// currently OK if comp is null
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

	    case V_FULLSCREEN:
		obj = getFullScreen(comp, obj);
		break;

	    case V_GRAPHICS:
		obj = getGraphics(comp, obj);
		break;

	    case V_HIGHLIGHTED:
		obj = getHighlighted(comp, obj);
		break;

	    case V_ICONIFIED:
		obj = getIconified(comp, obj);
		break;

	    case V_INDEX:
		obj = getIndex(comp, obj);
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

	    case V_LOCATION:
		obj = getLocation(comp, obj);
		break;

	    case V_MAPPINGS:
		obj = getMappings(comp, obj);
		break;

	    case V_MAXIMIZED:
		obj = getMaximized(comp, obj);
		break;

	    case V_MAXIMUM:
		obj = getMaximum(comp, obj);
		break;

	    case V_MINIMUM:
		obj = getMinimum(comp, obj);
		break;

	    case V_MODAL:
		obj = getModal(comp, obj);
		break;

	    case V_ORIGIN:
		obj = getOrigin(comp, obj);
		break;

	    case V_OUTPUTFILTER:
		obj = getOutputFilter(comp, obj);
		break;

	    case V_PARENT:
		obj = getParent(comp, obj);
		break;

	    case V_PREFERREDSIZE:
		obj = getPreferredSize(comp, obj);
		break;

	    case V_ROOT:
		obj = getRoot(comp, obj);
		break;

	    case V_ROWS:
		obj = getRows(comp, obj);
		break;

	    case V_SCREEN:
		obj = getScreen(comp, obj);
		break;

	    case V_SCROLLPOSITION:
		obj = getScrollPosition(comp, obj);
		break;

	    case V_SELECTED:
		obj = getSelected(comp, obj);
		break;

	    case V_SELECTEDENDS:
		obj = getSelectedEnds(comp, obj);
		break;

	    case V_SHOWING:
		obj = getShowing(comp, obj);
		break;

	    case V_SIZE:
		obj = getSize(comp, obj);
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

	    case V_UNITINCREMENT:
		obj = getUnitIncrement(comp, obj);
		break;

	    case V_VALUE:
		obj = getValue(comp, obj);
		break;

	    case V_VIEWPORT:
		obj = getViewport(comp, obj);
		break;

	    case V_VISIBLE:
		obj = getVisible(comp, obj);
		break;

	    case V_VISIBLEAMOUNT:
		obj = getVisibleAmount(comp, obj);
		break;

	    case V_VISITCOLOR:
		obj = getVisitColor(comp, obj);
		break;
	}

	return(obj);
    }


    protected YoixObject
    setField(String name, YoixObject obj) {

	Object  comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp != null && obj != null) {
	    switch (activeField(name, activefields)) {
		case V_AFTERLOAD:
		    setAfterLoad(comp, obj);
		    break;

		case V_ALIGNMENT:
		    setAlignment(comp, obj);
		    break;

		case V_ANCHOR:
		    setAnchor(comp, obj);
		    break;

		case V_BACKGROUND:
		    setBackground(comp, obj);
		    break;

		case V_BACKGROUNDHINTS:
		    setBackgroundHints(comp, obj);
		    break;

		case V_BACKGROUNDIMAGE:
		    setBackgroundImage(comp, obj);
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

		case V_CARET:
		    setCaret(comp, obj);
		    break;

		case V_CLICKRADIUS:
		    setClickRadius(comp, obj);
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

		case V_CURSOR:
		    setCursor(comp, obj);
		    break;

		case V_DECORATIONSTYLE:
		    setDecorationStyle(comp, obj);
		    break;

		case V_DIRECTORY:
		    setDirectory(comp, obj);
		    break;

		case V_DISPOSE:
		    setDispose(comp, obj);
		    break;

		case V_ECHO:
		    setEcho(comp, obj);
		    break;

		case V_EDIT:
		    setEdit(comp, obj);
		    break;

		case V_ENABLED:
		    setEnabled(comp, obj);
		    break;

		case V_FILE:
		    setFile(comp, obj);
		    break;

		case V_FILTERS:
		    setFilters(comp, obj);
		    break;

		case V_FIRSTFOCUS:
		    setFirstFocus(comp, obj);
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

		case V_GRAPHICS:
		    setGraphics(comp, obj);
		    break;

		case V_GROUP:
		    setGroup(comp, obj);
		    break;

		case V_HIGHLIGHTED:
		    setHighlighted(comp, obj);
		    break;

		case V_ICONIFIED:
		    setIconified(comp, obj);
		    break;

		case V_INPUTFILTER:
		    setInputFilter(comp, obj);
		    break;

		case V_INSETS:
		    setInsets(comp, obj);
		    break;

		case V_IPAD:
		    setIpad(comp, obj);
		    break;

		case V_INDEX:
		    setIndex(comp, obj);
		    break;

		case V_ITEMS:
		    setItems(comp, obj);
		    break;

		case V_LABELS:
		    setLabels(comp, obj);
		    break;

		case V_LAYOUT:
		    setLayout(comp, obj);
		    break;

		case V_LAYOUTMANAGER:
		    setLayoutManager(comp, obj);
		    break;

		case V_LOCATION:
		    setLocation(comp, obj);
		    break;

		case V_MAPPINGS:
		    setMappings(comp, obj);
		    break;

		case V_MAXIMIZED:
		    setMaximized(comp, obj);
		    break;

		case V_MAXIMUM:
		    setMaximum(comp, obj);
		    break;

		case V_MENUBAR:
		    setMenuBar(comp, obj);
		    break;

		case V_MINIMUM:
		    setMinimum(comp, obj);
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

		case V_ORIENTATION:
		    setOrientation(comp, obj);
		    break;

		case V_ORIGIN:
		    setOrigin(comp, obj);
		    break;

		case V_OUTPUTFILTER:
		    setOutputFilter(comp, obj);
		    break;

		case V_PAINT:
		    setPaint(comp, obj);
		    break;

		case V_PARENT:
		    setParent(comp, obj);
		    break;

		case V_POPUP:
		    setPopup(comp, obj);
		    break;

		case V_PREFERREDSIZE:
		    setPreferredSize(comp, obj);
		    break;

		case V_PROMPT:
		    setPrompt(comp, obj);
		    break;

		case V_REQUESTFOCUS:
		    setRequestFocus(comp, obj);
		    break;

		case V_RESIZABLE:
		    setResizable(comp, obj);
		    break;

		case V_ROWPROPERTIES:
		    setRowProperties(comp, obj);
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

		case V_SCROLLPOSITION:
		    setScrollPosition(comp, obj);
		    break;

		case V_SELECTED:
		    setSelected(comp, obj);
		    break;

		case V_SELECTEDENDS:
		    setSelectedEnds(comp, obj);
		    break;

		case V_SHAPE:
		    setShape(comp, obj);
		    break;

		case V_SIZE:
		    setSize(comp, obj);
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

		case V_TEXTWRAP:
		    setTextWrap(comp, obj);
		    break;

		case V_TITLE:
		    setTitle(comp, obj);
		    break;

		case V_UNITINCREMENT:
		    setUnitIncrement(comp, obj);
		    break;

		case V_VALIDATE:
		    setValidate(comp, obj);
		    break;

		case V_VALUE:
		    setValue(comp, obj);
		    break;

		case V_VISIBLE:
		    setVisible(comp, obj);
		    break;

		case V_VISIBLEAMOUNT:
		    setVisibleAmount(comp, obj);
		    break;

		case V_VISITCOLOR:
		    setVisitColor(comp, obj);
		    break;
	    }
	}

	return(obj);
    }

    ///////////////////////////////////
    //
    // YoixBodyComponentAWT Methods
    //
    ///////////////////////////////////

    final int
    addListeners(int mask) {

	Object  comp;
	int     missed;
	int     listener;
	int     bit;

	comp = peer;     		// snapshot - just to be safe
	missed = 0;

	for (bit = 1; mask != 0 && bit != NEXTLISTENER; bit <<= 1) {
	    switch (listener = (mask & bit)) {
		case ACTIONLISTENER:
		    if (comp instanceof Button)
			((Button)comp).addActionListener(this);
		    else if (comp instanceof List)
			((List)comp).addActionListener(this);
		    else if (comp instanceof TextField)
			((TextField)comp).addActionListener(this);
		    else if (comp instanceof YoixAWTCanvas)
			((YoixAWTCanvas)comp).addActionListener(this);
		    else missed |= listener;
		    break;

		case ADJUSTMENTLISTENER:
		    if (comp instanceof ScrollPane)
			addAdjustmentListener((ScrollPane)comp);
		    else missed |= listener;

		case TEXTLISTENER:
		    if (comp instanceof TextComponent)
			((TextComponent)comp).addTextListener(this);
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
	// The comments that describe the 3/24/07 code changes were copied
	// form the Swing version of this method. We didn't test much but
	// all of this really needs a close look once things settle down.
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
		//
		if (comp instanceof Frame)
		    YoixMiscMenu.removeListener(((Frame)comp).getMenuBar(), this);
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
	    } else if (comp instanceof MenuComponent) {
		if (comp instanceof MenuBar)
		    YoixMiscMenu.dispose((MenuBar)comp);
		else if (comp instanceof PopupMenu)
		    YoixMiscMenu.dispose((PopupMenu)comp);
	    }

	    if (awtparent != null)
		YoixMiscJFC.dispose(awtparent);

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
	
	    if (data.defined(N_MENUBAR))
		data.putObject(N_MENUBAR, null);
	    if (data.defined(N_POPUP))
		data.putObject(N_POPUP, null);

	    this.peer = null;
	    this.awtparent = null;
	    this.parent = null;
	    windowDeactivate(this);
	}
    }


    protected void
    finalize() {

	menubarowner = null;
	popupowner = null;
	translator = null;
	super.finalize();
    }


    final Adjustable
    getAdjustable(int orientation) {

	Adjustable  adjustable;
	Object      comp;

	comp = this.peer;		// snapshot - just to be safe
	if (comp instanceof ScrollPane) {
	    if (orientation == YOIX_HORIZONTAL)
		adjustable = ((ScrollPane)comp).getHAdjustable();
	    else adjustable = ((ScrollPane)comp).getVAdjustable();
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

	if (isContainer(arg))
	    container = (Container)arg;
	return(container);
    }


    final boolean
    isContainer() {

	return(isContainer(this.peer));
    }


    final boolean
    isContainer(Object arg) {

	return(arg instanceof Container);
    }


    final boolean
    isMenu() {

	return(isMenu(peer));
    }


    final boolean
    isMenu(Object arg) {

	return(arg instanceof MenuBar || arg instanceof PopupMenu);
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
	int     missed;
	int     listener;
	int     bit;

	comp = this.peer;		// snapshot - just to be safe
	missed = 0;

	for (bit = 1; mask != 0 && bit != NEXTLISTENER; bit <<= 1) {
	    switch (listener = (mask & bit)) {
		case ACTIONLISTENER:
		    if (comp instanceof Button)
			((Button)comp).removeActionListener(this);
		    else if (comp instanceof List)
			((List)comp).removeActionListener(this);
		    else if (comp instanceof TextField)
			((TextField)comp).removeActionListener(this);
		    else if (comp instanceof YoixAWTCanvas)
			((YoixAWTCanvas)comp).removeActionListener(this);
		    else missed |= listener;
		    break;

		case ADJUSTMENTLISTENER:
		    if (comp instanceof ScrollPane)
			removeAdjustmentListener((ScrollPane)comp);
		    else missed |= listener;
		    break;

		case TEXTLISTENER:
		    if (comp instanceof TextComponent)
			((TextComponent)comp).removeTextListener(this);
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

	boolean  trim;
	Object   comp;
	String   dest;
	String   text;
	int      delta = 0;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof Component && canTextChange()) {
	    trim = data.getBoolean(N_AUTOTRIM);
	    if (!(comp instanceof YoixAWTTextArea)) {
		if (comp instanceof TextField) {
		    str = (str != null) ? str : "";
		    dest = ((TextField)comp).getText();
		    offset = Math.max(0, Math.min(offset, dest.length()));
		    if (trim == false)
			str = YoixMisc.trim(str, "", "\n");
		    text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		    delta = text.length() - dest.length();
		    ((TextField)comp).setText(text);
		    if (adjust)
			((TextField)comp).setCaretPosition(offset + delta);
		} else if (comp instanceof YoixAWTTextComponent) {
		    dest = ((YoixAWTTextComponent)comp).getText();
		    text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		    delta = text.length() - dest.length();
		    ((YoixAWTTextComponent)comp).setText(text);
		} else if (comp instanceof Label) {
		    dest = ((Label)comp).getText();
		    text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		    delta = text.length() - dest.length();
		    ((Label)comp).setText(text);
		    ((Label)comp).invalidate();	// other components too??
		} else if (comp instanceof Button) {
		    dest = ((Button)comp).getLabel();
		    text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		    delta = text.length() - dest.length();
		    ((Button)comp).setLabel(text);
		} else if (comp instanceof Checkbox) {
		    dest = ((Checkbox)comp).getLabel();
		    text = YoixMisc.replaceString(dest, offset, length, str, trim, undo);
		    delta = text.length() - dest.length();
		    ((Checkbox)comp).setLabel(text);
		} else delta = super.replaceText(offset, length, str, adjust, undo);
	    } else delta = ((YoixAWTTextArea)comp).replaceText(offset, length, str, trim, adjust, undo);
	}

	return(delta);
    }


    final void
    setBackground(Object comp, YoixObject obj) {

	MenuBar  menubar;

	super.setBackground(comp, obj);
	if (comp instanceof Frame) {
	    if ((menubar = ((Frame)comp).getMenuBar()) != null) {
		((Frame)comp).remove(menubar);
		((Frame)comp).setMenuBar(menubar);
	    }
	}
    }


    final void
    setForeground(Object comp, YoixObject obj) {

	MenuBar  menubar;

	super.setForeground(comp, obj);
	if (comp instanceof Frame) {
	    if ((menubar = ((Frame)comp).getMenuBar()) != null) {
		((Frame)comp).remove(menubar);
		((Frame)comp).setMenuBar(menubar);
	    }
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

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof PopupMenu) {
	    try {
		if (popupowner != null && popupowner != owner) {
		    YoixMiscMenu.removeListener((PopupMenu)comp, popupowner);
		    if ((component = popupowner.getManagedObject()) != null) {
			if (component instanceof Component)
			    ((Component)component).remove((PopupMenu)comp);
			else VM.die(INTERNALERROR);
		    }
		    popupowner = null;
		}

		if (popupowner != owner) {
		    if ((component = owner.getManagedObject()) != null) {
			if (component instanceof Component) {
			    popupowner = owner;
			    YoixMiscMenu.addListener((PopupMenu)comp, popupowner);
			    ((Component)component).add((PopupMenu)comp);
			} else VM.abort(TYPECHECK, N_POPUP);
		    }
		}

		syncMenuProperties(owner);
		showPopupMenu((PopupMenu)comp);
	    }
	    catch(RuntimeException e) {}
	}
    }


    final void
    specialAddToProcessing(Object comp, Container pane, YoixObject child, Object constraint, int index) {

	Container  parent;
	Object     component;

	component = child.getManagedObject();
	if (component instanceof YoixAWTTableManager) {
	    parent = ((YoixAWTTableManager)component).getParent();
	    if (parent != null)
		component = parent;
	} else if (component instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)component).disposeSavedGraphics();
	pane.add((Component)component, constraint);
    }


    final boolean
    specialLayout(YoixObject obj, Object comp, Container pane, YoixObject added, boolean fronttoback) {

	boolean  special = false;

	if (comp instanceof ScrollPane) {
	    special = true;
	    addToScroller(obj, added, !fronttoback);
	}
	return(special);
    }


    final void
    specialRemoveAll(Container pane) {

	pane.removeAll();
    }


    final void
    syncMenuProperties(YoixBodyComponent owner) {

	YoixObject  value;
	Object      component;
	Object      comp;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof MenuBar || comp instanceof PopupMenu) {
	    if (owner != null) {
		if ((component = owner.getManagedObject()) != null) {
		    if (component instanceof Component) {
			if ((value = data.getObject(N_FONT)) == null || value.isNull())
			    YoixMiscMenu.setMenuProperty(comp, N_FONT, ((Component)component).getFont());
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
    addAdjustmentListener(ScrollPane scroller) {

	Adjustable  adjustable;

	if (scroller != null) {
	    if ((adjustable = scroller.getHAdjustable()) != null)
		adjustable.addAdjustmentListener(this);
	    if ((adjustable = scroller.getVAdjustable()) != null)
		adjustable.addAdjustmentListener(this);
	}
    }


    private void
    addItems(YoixObject obj, int type) {

	Container  prnt;
	Container  top;
	Dimension  sz;
	Object     comp;
	String     values[];
	int        n;

	comp = this.peer;		// snapshot - just to be safe

	if (comp instanceof Component && obj != null) {
	    if (comp instanceof Choice) {
		if ((values = buildTranslator(obj, type)) != null) {
		    if (((Choice)comp).getItemCount() > 0)
			((Choice)comp).removeAll();
		    for (n = 0; n < values.length; n++)
			((Choice)comp).addItem(values[n]);
		    ((Choice)comp).invalidate();	// other components too??
		}
	    } else if (comp instanceof List) {
		if ((values = buildTranslator(obj, type)) != null) {
		    if (((List)comp).getItemCount() > 0)
			((List)comp).removeAll();
		    for (n = 0; n < values.length; n++)
			((List)comp).add(values[n]);
		    //
		    // This is trying to cure some very obscure List sizing
		    // problems. Was a late addition to 1.0 that seems to
		    // help, but just in case it can be disabled by setting
		    // bit DEBUG_AWTLIST in VM.debug.
		    //
		    if (values.length > 0) {
			if (VM.bitCheck(N_DEBUG, DEBUG_AWTLIST) == false) {
			    if ((prnt = ((Component)comp).getParent()) != null) {
				top = null;
				while (prnt != null) {
				    top = prnt;
				    prnt.invalidate();
				    prnt = prnt.getParent();
				}
				if ((sz = ((List)comp).getSize()) != null)
				    ((List)comp).setSize(sz.width - 1, sz.height);
				top.validate();
			    }
			}
		    }
		}
	    }
	}
    }


    private void
    addToScroller(YoixObject add, YoixObject added, boolean fronttoback) {

	addToUnconstrained(add, added, fronttoback);
    }


    private void
    buildComponent() {

	YoixObject  obj;
	boolean     canshape = false;

	//
	// Should be thread-safe because it's only called once from the
	// constructor. Will need fixing if that ever changes!!
	//

	if ((peer = buildPeer()) == null) {
	    switch (getMinor()) {
		case BUTTON:
		    peer = new Button();
		    setField(N_TEXT);
		    setField(N_COMMAND);
		    break;

		case CANVAS:
		    peer = new YoixAWTCanvas(data, this);
		    setField(N_GRAPHICS);
		    setField(N_STATE);
		    setField(N_BORDERCOLOR);
		    setField(N_BORDER);
		    break;

		case CHECKBOX:
		    peer = new YoixAWTCheckbox(data, this);
		    setField(N_GROUP);
		    setField(N_TEXT);
		    setField(N_STATE);
		    setField(N_COMMAND);
		    break;

		case CHECKBOXGROUP:
		    peer = new CheckboxGroup();
		    break;

		case CHOICE:
		    peer = new Choice();
		    setField(N_ITEMS);
		    if ((obj = data.getObject(N_ITEMS)) != null && obj.isNull()) {
			setField(N_LABELS);
			setField(N_MAPPINGS);
		    }
		    setField(N_SELECTED);
		    break;

		case DIALOG:
		    peer = newDialog(data.getObject(N_PARENT), false);
		    canshape = true;
		    setField(N_GRAPHICS);
		    setField(N_TITLE);
		    setField(N_MODAL);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_PARENT);
		    setField(N_RESIZABLE);
		    setField(N_DECORATIONSTYLE);
		    setField(N_FIRSTFOCUS);
		    setToConstant(N_MODAL);
		    break;

		case FILEDIALOG:
		    peer = newDialog(data.getObject(N_PARENT), true);
		    setField(N_MODE);
		    setField(N_FILE);
		    setField(N_DIRECTORY);
		    setField(N_FILTERS);
		    setField(N_PARENT);
		    break;

		case FRAME:
		    peer = new YoixAWTFrame(data, this, getGraphicsConfigurationFromScreen());
		    canshape = true;
		    setField(N_GRAPHICS);
		    setField(N_TITLE);
		    setField(N_MENUBAR);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_PARENT);
		    setField(N_RESIZABLE);
		    setField(N_ICONIFIED);
		    setField(N_MAXIMIZED);
		    setField(N_DECORATIONSTYLE);
		    setField(N_FIRSTFOCUS);
		    break;

		case LABEL:
		    peer = new Label();
		    setField(N_TEXT);
		    setField(N_ALIGNMENT);
		    break;

		case LIST:
		    peer = new YoixAWTList(data, this, data.getInt(N_ROWS, 1));
		    setField(N_MULTIPLEMODE);
		    setField(N_ITEMS);
		    if ((obj = data.getObject(N_ITEMS)) != null && obj.isNull()) {
			setField(N_LABELS);
			setField(N_MAPPINGS);
		    }
		    setField(N_SELECTED);
		    setField(N_INDEX);
		    setToConstant(N_SCROLL);
		    break;

		case MENUBAR:
		    peer = YoixMiscMenu.buildMenuBar(data.getObject(N_ITEMS));
		    break;

		case PANEL:
		    peer = new YoixAWTPanel(data, this);
		    setField(N_GRAPHICS);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    break;

		case POPUPMENU:
		    peer = YoixMiscMenu.buildPopupMenu(
			data.getObject(N_ITEMS),
			data.getObject(N_TEXT)
		    );
		    break;

		case SCROLLBAR:
		    peer = new Scrollbar();
		    setField(N_MAXIMUM);
		    setField(N_MINIMUM);
		    setField(N_VISIBLEAMOUNT);
		    setField(N_BLOCKINCREMENT);
		    setField(N_UNITINCREMENT);
		    setField(N_ORIENTATION);
		    setField(N_VALUE);
		    break;

		case SCROLLPANE:
		    peer = new ScrollPane(jfcInt("ScrollPane", data, N_SCROLL));
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setToConstant(N_SCROLL);
		    break;

		case TABLECOLUMN:
		    peer = new YoixAWTTableColumn(data, this);
		    setField(N_ALIGNMENT);
		    setField(N_INDEX);
		    setField(N_INSETS);
		    setField(N_IPAD);
		    setField(N_STATE);
		    setField(N_SAVEGRAPHICS);
		    setField(N_BORDERCOLOR);
		    setField(N_VISITCOLOR);
		    setField(N_BORDER);
		    setField(N_COLUMNS);
		    setField(N_ROWS);
		    setField(N_ROWPROPERTIES);
		    break;

		case TABLEMANAGER:
		    peer = new YoixAWTTableManager(data, this);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_SAVEGRAPHICS);
		    setField(N_CLICKRADIUS);
		    setField(N_INDEX);
		    setField(N_INPUTFILTER);
		    setField(N_OUTPUTFILTER);
		    setField(N_ROWPROPERTIES);
		    setField(N_TEXT);
		    break;

		case TEXTAREA:
		    peer = new YoixAWTTextArea(
			data,
			this,
			data.getInt(N_ROWS, 5),
			data.getInt(N_COLUMNS, 80),
			jfcInt("TextArea", data, N_SCROLL)
		    );
		    setField(N_TEXT);
		    setField(N_CARET);
		    setField(N_SELECTEDENDS);
		    setField(N_EDIT);
		    break;

		case TEXTCANVAS:
		    peer = new YoixAWTTextCanvas(data, this);
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

		case TEXTFIELD:
		    peer = new YoixAWTTextField(data, this);
		    setField(N_COLUMNS);
		    setField(N_TEXT);
		    setField(N_CARET);
		    setField(N_SELECTEDENDS);
		    setField(N_EDIT);
		    setField(N_ECHO);
		    break;

		case TEXTTERM:
		    peer = new YoixAWTTextTerm(data, this);
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

		case WINDOW:
		    peer = newWindow(data.getObject(N_PARENT));
		    canshape = true;
		    setField(N_GRAPHICS);
		    setField(N_LAYOUTMANAGER);
		    setField(N_LAYOUT);
		    setField(N_PARENT);
		    setField(N_FIRSTFOCUS);
		    break;

		default:
		    VM.abort(UNIMPLEMENTED);
		    break;
	    }
	}

	setWindow(peer instanceof YoixInterfaceWindow);
	setCanShape(canshape);

	setField(N_AFTERLOAD);
	setField(N_SYNCVIEWPORT);
	setField(N_ENABLED);
	setField(N_TAG);
	setField(N_FOREGROUND);
	setField(N_BACKGROUND);
	setField(N_BACKGROUNDHINTS);
	setField(N_BACKGROUNDIMAGE);
	setField(N_PAINT);
	setField(N_FONT);
	setField(N_POPUP);
	setField(N_SIZE);		// right before visible - menubar kludge
	setField(N_SHAPE);
	setField(N_LOCATION);		// moved after N_SIZE on 3/15/00
	setField(N_CURSOR);
	setField(N_COMPRESSEVENTS);

	disposable = true;
    }


    private synchronized String[]
    buildTranslator(YoixObject items, int type) {

	YoixObject  obj;
	Hashtable   table;
	String      values[];
	String      mvalues[];
	int         length;
	int         m;
	int         n;
	int         offset;

	//
	// Currently using one Hashtable organized so retrieving items
	// from the component (List or Choice) is efficient.
	//

	values = null;
	mvalues = null;

	switch (type) {
	    case V_ITEMS:
		length = items.length();
		offset = items.offset();
		table = new Hashtable();
		values = new String[(length - offset + 1)/2];
		mvalues = new String[values.length];
		for (m = 0, n = offset; n < length; m++, n += 2) {
		    values[m] = items.get(n, false).stringValue();
		    obj = items.get(n + 1, false);
		    if (obj.notNull()) {
			mvalues[m] = obj.stringValue();
			table.put(values[m], mvalues[m]);
		    } else mvalues[m] = null;
		}
		translator = table;
		labels = values;
		mappings = mvalues;
		break;

	    case V_LABELS:
		length = items.length();
		offset = items.offset();
		table = new Hashtable();
		values = new String[length - offset];
		mvalues = new String[length - offset];
		for (m = 0, n = offset; n < length; m++, n++) {
		    values[m] = items.get(n, false).stringValue();
		    mvalues[m] = null;
		}
		translator = table;
		labels = values;
		mappings = mvalues;
		break;

	    case V_MAPPINGS:
		if (items.isNull()) {
		    if (labels != null) {
			length = labels.length;
			mvalues = new String[length];
			table = new Hashtable();
			for (n = 0; n < length; n++)
			    mvalues[n] = null;
			translator = table;
			mappings = mvalues;
		    }
		} else if (labels != null && labels.length == items.sizeof()) {
		    length = items.length();
		    offset = items.offset();
		    mvalues = new String[length-offset];
		    table = new Hashtable();
		    for (m = 0, n = offset; n < length; m++, n++) {
			obj = items.get(n, false);
			if (obj.notNull()) {
			    mvalues[m] = obj.stringValue();
			    table.put(labels[m], mvalues[m]);
			} else mvalues[m] = null;
		    }
		    translator = table;
		    mappings = mvalues;
		} else if (labels == null)
		    VM.abort(BADVALUE, new String[] {"null labels"});
		else VM.abort(BADVALUE, new String[] {"length mismatch"});
		break;
	}

	return(values);
    }


    private synchronized YoixObject
    builtinHTML(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj;
	Object      objs[] = {null, null, null, null};
	String      strs[];
	String      str;
	String      result;
	int         ints[];
	int         strcnt = 0;
	int         intcnt = 0;
	int         len;
	int         off;
	int         pos;
	int         i;

	obj = null;

	if (comp instanceof YoixAWTTableManager) {
	    if (arg.length >= 0 && arg.length <= 4) {
		for (i = 0; i < arg.length; i++) {
		    if (arg[i].isNull() || arg[i].isString()) {
			if (strcnt < 2)
			    objs[strcnt++] = arg[i].stringValue();
			else VM.badArgument(name, i);
		    } else if (arg[i].isInteger()) {
			if (intcnt < 2)
			    objs[2+intcnt++] = new Integer(arg[i].intValue());
			else VM.badArgument(name, i);
		    } else if (arg[i].isArray()) {
			if ((len = arg[i].sizeof()) > 0) {
			    off = arg[i].offset();
			    obj = arg[i].get(off, false);
			    if (obj.isNull() || obj.isString()) {
				if (strcnt < 2) {
				    strs = new String[len];
				    len = arg[i].length();
				    pos = 0;
				    while (off < len) {
					obj = arg[i].get(off++, false);
					if (obj.isNull() || obj.isString())
					    strs[pos++] = obj.stringValue();
					else VM.badArgument(name, i);
				    }
				    objs[strcnt++] = strs;
				} else VM.badArgument(name, i);
			    } else if (obj.isInteger()) {
				if (intcnt < 2) {
				    ints = new int[len];
				    len = arg[i].length();
				    pos = 0;
				    while (off < len) {
					obj = arg[i].get(off++, false);
					if (obj.isInteger())
					    ints[pos++] = obj.intValue();
					else VM.badArgument(name, i);
				    }
				    objs[2+intcnt++] = ints;
				} else VM.badArgument(name, i);
			    } else VM.badArgument(name, i);
			} else VM.badArgument(name, i);
		    } else VM.badArgument(name, i);
		}
		result = ((YoixAWTTableManager)comp).getHTML(objs);
		obj = YoixObject.newString(result);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinSetValues(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj = null;

	if (comp instanceof Scrollbar) {
	    if (arg.length == 4) {
		if (arg[0].isNumber()) {
		    if (arg[1].isNumber()) {
			if (arg[2].isNumber()) {
			    if (arg[3].isNumber()) {
				((Scrollbar)comp).setMaximum(arg[3].intValue());
				((Scrollbar)comp).setMinimum(arg[2].intValue());
				((Scrollbar)comp).setVisibleAmount(arg[1].intValue());
				((Scrollbar)comp).setValue(arg[0].intValue());
				obj = YoixObject.newEmpty();
			    } else VM.badArgument(name, 3);
			} else VM.badArgument(name, 2);
		    } else VM.badArgument(name, 1);
		} else VM.badArgument(name, 0);
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private synchronized YoixObject
    builtinValue(Object comp, String name, YoixObject arg[]) {

	YoixObject  obj;
	int         idx;

	obj = null;

	if (comp instanceof YoixAWTTableColumn) {
	    if (arg.length == 0 || arg.length == 1) {
		idx = -1;
		if (arg.length == 1) {
		    if (arg[0].isInteger())
			idx = arg[0].intValue();
		    else VM.badArgument(name, 0);
		} else idx = ((YoixAWTTableColumn)comp).getIndexItem();
		obj = YoixObject.newString(((YoixAWTTableColumn)comp).getCell(idx));
	    } else VM.badCall(name);
	}

	return(obj);
    }


    private YoixObject
    getBlockIncrement(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    obj = YoixObject.newInt(((Scrollbar)comp).getBlockIncrement());
	return(obj);
    }


    private YoixObject
    getBorder(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTCanvas)
	    obj = YoixMakeScreen.yoixInsets(((YoixAWTCanvas)comp).getBorderInsets());
	return(obj);
    }


    private YoixObject
    getBorderColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTCanvas)
	    obj = YoixMake.yoixColor(((YoixAWTCanvas)comp).getBorderColor());
	return(obj);
    }


    private YoixObject
    getCaret(Object comp, YoixObject obj) {

	if (comp instanceof TextComponent)
	    obj = YoixObject.newInt(((TextComponent)comp).getCaretPosition());
	return(obj);
    }


    private YoixObject
    getCellSize(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    obj = YoixMakeScreen.yoixDimension(((YoixAWTTextComponent)comp).getCellSize());
	else if (comp instanceof YoixAWTTableManager)
	    obj = YoixMakeScreen.yoixDimension(((YoixAWTTableManager)comp).getCellSize());
	return(obj);
    }


    private YoixObject
    getColumns(Object comp, YoixObject obj) {

	if (comp instanceof TextArea)
	    obj = YoixObject.newInt(((TextArea)comp).getColumns());
	else if (comp instanceof TextField)
	    obj = YoixObject.newInt(((TextField)comp).getColumns());
	else if (comp instanceof YoixAWTTextComponent)
	    obj = YoixObject.newInt(((YoixAWTTextComponent)comp).getColumns());
	return(obj);
    }


    private YoixObject
    getDirectory(Object comp, YoixObject obj) {

	String  directory;
	String  file;

	if (comp instanceof FileDialog) {
	    file = ((FileDialog)comp).getFile();
	    if (file != null) {
		directory = ((FileDialog)comp).getDirectory();
		if (data.getBoolean(N_TOYOIXPATH, true))
		    directory = YoixMisc.toYoixPath(directory);
		obj = YoixObject.newString(directory);
	    } else obj = data.get(N_DIRECTORY, true);
	}

	return(obj);
    }


    private YoixObject
    getDisplayed(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    obj = YoixObject.newString(((YoixAWTTableManager)comp).getDisplayedText());
	return(obj);
    }


    private YoixObject
    getDispose(Object comp, YoixObject obj) {

	if (isWindow())
	    obj = YoixObject.newInt(comp == null);
	return(obj);
    }


    private YoixObject
    getEcho(Object comp, YoixObject obj) {

	if (comp instanceof TextField)
	    obj = YoixObject.newInt(((TextField)comp).getEchoChar());
	return(obj);
    }


    private YoixObject
    getEdit(Object comp, YoixObject obj) {

	if (comp instanceof TextComponent)
	    obj = YoixObject.newInt(((TextComponent)comp).isEditable());
	else if (comp instanceof YoixAWTTextTerm)
	    obj = YoixObject.newInt(((YoixAWTTextTerm)comp).getEditable());
	return(obj);
    }


    private YoixObject
    getExtent(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    obj = YoixMakeScreen.yoixDimension(((YoixAWTTextComponent)comp).getExtent());
	else if (comp instanceof YoixAWTTableManager)
	    obj = YoixMakeScreen.yoixDimension(((YoixAWTTableManager)comp).getExtent());
	return(obj);
    }


    private YoixObject
    getFile(Object comp, YoixObject obj) {

	String  absolute;
	String  file;
	String  pwd;

	if (comp instanceof FileDialog) {
	    file = ((FileDialog)comp).getFile();
	    if (file != null && file.length() > 0) {
		if (data.getBoolean(N_TOYOIXPATH, true)) {
		    pwd = YoixMisc.toYoixPath(".");
		    absolute = YoixMisc.toYoixPath(file);
		    if (absolute.startsWith(pwd))
			file = absolute.substring(pwd.length());
		}
	    }
	    obj = YoixObject.newString(file);
	}

	return(obj);
    }


    private YoixObject
    getHighlighted(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    obj = YoixObject.newString(((YoixAWTTableManager)comp).getHighlightedItem());
	else if (comp instanceof YoixAWTTableColumn)
	    obj = YoixObject.newString(((YoixAWTTableColumn)comp).getHighlightedItem());
	return(obj);
    }


    private YoixObject
    getIconified(Object comp, YoixObject obj) {

	if (comp instanceof Frame)
	    obj = YoixObject.newInt(((Frame)comp).getExtendedState() & Frame.ICONIFIED);
	return(obj);
    }


    private YoixObject
    getIndex(Object comp, YoixObject obj) {

	int  idx[];
	int  n;

	if (comp instanceof YoixAWTTableManager) {
	    idx = ((YoixAWTTableManager)comp).getIndexItem();
	    obj = YoixObject.newArray(idx.length);
	    for (n = 0; n < idx.length; n++)
		obj.put(n, YoixObject.newInt(idx[n]), false);
	} else if (comp instanceof YoixAWTTableColumn)
	    obj = YoixObject.newInt(((YoixAWTTableColumn)comp).getIndexItem());
	else if (comp instanceof YoixAWTList)
	    obj = YoixObject.newInt(((YoixAWTList)comp).getVisibleIndex());

	return(obj);
    }


    private YoixObject
    getInputFilter(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    obj = ((YoixAWTTableManager)comp).getInputFilter();
	return(obj);
    }


    private YoixObject
    getInsets(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    obj = YoixMakeScreen.yoixInsets(((YoixAWTTextComponent)comp).getInsets());
	return(obj);
    }


    private YoixObject
    getIpad(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    obj = YoixMakeScreen.yoixInsets(((YoixAWTTextComponent)comp).getIpad());
	return(obj);
    }


    private synchronized YoixObject
    getItems(Object comp, YoixObject obj) {

	int  length;
	int  m;
	int  n;

	if (comp instanceof Choice || comp instanceof List) {
	    if (labels != null && mappings != null) {
		length = Math.min(labels.length, mappings.length);
		obj = YoixObject.newArray(2*length);
		for (m = 0, n = 0; n < length; n++) {
		    obj.put(m++, YoixObject.newString(labels[n]), false);
		    obj.put(m++, YoixObject.newString(mappings[n]), false);
		}
	    } else obj = YoixObject.newArray(0);
	}
	return(obj);
    }


    private synchronized YoixObject
    getLabels(Object comp, YoixObject obj) {

	int  n;

	if (comp instanceof Choice || comp instanceof List) {
	    if (labels != null) {
		obj = YoixObject.newArray(labels.length);
		for (n = 0; n < labels.length; n++)
		    obj.put(n, YoixObject.newString(labels[n]), false);
	    } else obj = YoixObject.newArray(0);
	}
	return(obj);
    }


    private synchronized YoixObject
    getMappings(Object comp, YoixObject obj) {

	int  n;

	if (comp instanceof Choice || comp instanceof List) {
	    if (mappings != null) {
		obj = YoixObject.newArray(mappings.length);
		for (n = 0; n < mappings.length; n++) {
		    if (mappings[n] == null && labels != null && n < labels.length)
			obj.put(n, YoixObject.newString(labels[n]), false);
		    else obj.put(n, YoixObject.newString(mappings[n]), false);
		}
	    } else obj = YoixObject.newArray(0);
	}
	return(obj);
    }


    private YoixObject
    getMaximized(Object comp, YoixObject obj) {

	if (comp instanceof Frame)
	    obj = YoixObject.newInt(((Frame)comp).getExtendedState() & Frame.MAXIMIZED_BOTH);
	return(obj);
    }


    private YoixObject
    getMaximum(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    obj = YoixObject.newInt(((Scrollbar)comp).getMaximum());
	return(obj);
    }


    private YoixObject
    getMinimum(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    obj = YoixObject.newInt(((Scrollbar)comp).getMinimum());
	return(obj);
    }


    private YoixObject
    getModal(Object comp, YoixObject obj) {

	if (comp instanceof Dialog)
	    obj = YoixObject.newInt(((Dialog)comp).isModal());
	return(obj);
    }


    private YoixObject
    getOrigin(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    obj = YoixMakeScreen.yoixPoint(((YoixAWTTextComponent)comp).getOrigin());
	else if (comp instanceof YoixAWTTableManager)
	    obj = YoixMakeScreen.yoixPoint(((YoixAWTTableManager)comp).getOrigin());
	return(obj);
    }


    private YoixObject
    getOutputFilter(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    obj = ((YoixAWTTableManager)comp).getOutputFilter();
	return(obj);
    }


    private YoixObject
    getRows(Object comp, YoixObject obj) {

	if (comp instanceof TextArea)
	    obj = YoixObject.newInt(((TextArea)comp).getRows());
	else if (comp instanceof YoixAWTTextComponent)
	    obj = YoixObject.newInt(((YoixAWTTextComponent)comp).getRows());
	return(obj);
    }


    private YoixObject
    getScrollPosition(Object comp, YoixObject obj) {

	if (comp instanceof ScrollPane)
	    obj = YoixMakeScreen.yoixPoint(((ScrollPane)comp).getScrollPosition());
	return(obj);
    }


    private YoixObject
    getSelected(Object comp, YoixObject obj) {

	YoixBodyComponent  body;
	YoixAWTCheckbox    box;
	String             selected[];
	String             value;
	int                n;

	if (comp instanceof CheckboxGroup) {
	    obj = YoixObject.newNull();
	    if ((box = (YoixAWTCheckbox)((CheckboxGroup)comp).getSelectedCheckbox()) != null) {
		if ((body = box.getBody()) != null) {
		    switch (data.getInt(N_MODEL, 0)) {
			 case 0:
			    obj = YoixObject.newString(box.getActionCommand());
			    break;

			default:
			    obj = body.getContext();
			    break;
		    }
		}
	    }
	} else if (comp instanceof Choice) {
	    value = translateFrom(((Choice)comp).getSelectedItem());
	    obj = YoixObject.newString(value);
	} else if (comp instanceof List) {
	    selected = ((List)comp).getSelectedItems();
	    obj = YoixObject.newArray(selected.length);
	    for (n = 0; n < selected.length; n++) {
		value = translateFrom(selected[n]);
		obj.put(n, YoixObject.newString(value), false);
	    }
	} else if (comp instanceof TextComponent) {
	    value = ((TextComponent)comp).getSelectedText();
	    obj = YoixObject.newString(value);
	} else if (comp instanceof YoixAWTTableManager) {
	    value = ((YoixAWTTableManager)comp).getSelectedItem();
	    obj = YoixObject.newString(value);
	} else if (comp instanceof YoixAWTTableColumn) {
	    value = ((YoixAWTTableColumn)comp).getSelectedItem();
	    obj = YoixObject.newString(value);
	}

	return(obj);
    }


    private YoixObject
    getSelectedEnds(Object comp, YoixObject obj) {

	int  start;
	int  end;

	if (comp instanceof TextComponent) {
	    start = ((TextComponent)comp).getSelectionStart();
	    end = ((TextComponent)comp).getSelectionEnd();
	    obj = YoixObject.newArray(2);
	    obj.putInt(0, start);
	    obj.putInt(1, end);
	}

	return(obj);
    }


    private YoixObject
    getState(Object comp, YoixObject obj) {

	if (comp instanceof Checkbox)
	    obj = YoixObject.newInt(((Checkbox)comp).getState());
	else if (comp instanceof YoixAWTCanvas)
	    obj = YoixObject.newInt(((YoixAWTCanvas)comp).getState());
	else if (comp instanceof Frame)
	    obj = YoixObject.newInt(((Frame)comp).getExtendedState());
	return(obj);
    }


    private YoixObject
    getSyncCount(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    obj = YoixObject.newInt(((YoixAWTTextComponent)comp).getSyncCount());
	return(obj);
    }


    private YoixObject
    getText(Object comp, YoixObject obj) {

	String  str;

	if (comp instanceof Label)
	    str = ((Label)comp).getText();
	else if (comp instanceof TextComponent)
	    str = ((TextComponent)comp).getText();
	else if (comp instanceof Button)
	    str = ((Button)comp).getLabel();
	else if (comp instanceof Checkbox)
	    str = ((Checkbox)comp).getLabel();
	else if (comp instanceof YoixAWTTextComponent)
	    str = ((YoixAWTTextComponent)comp).getText();
	else if (comp instanceof YoixAWTTableManager)
	    str = ((YoixAWTTableManager)comp).getText();
	else str = null;

	if (str != null) {
	    if (data.getBoolean(N_AUTOTRIM))
		str = str.trim();
	    obj = YoixObject.newString(str);
	}

	return(obj);
    }


    private YoixObject
    getUnitIncrement(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    obj = YoixObject.newInt(((Scrollbar)comp).getUnitIncrement());
	return(obj);
    }


    private YoixObject
    getValue(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    obj = YoixObject.newInt(((Scrollbar)comp).getValue());
	return(obj);
    }


    private YoixObject
    getViewport(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    obj = YoixMakeScreen.yoixRectangle(((YoixAWTTextComponent)comp).getViewport());
	else if (comp instanceof YoixAWTTableManager)
	    obj = YoixMakeScreen.yoixRectangle(((YoixAWTTableManager)comp).getViewport());
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

	if (comp instanceof Scrollbar)
	    obj = YoixObject.newInt(((Scrollbar)comp).getVisibleAmount());
	return(obj);
    }


    private YoixObject
    getVisitColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableColumn)
	    obj = YoixMake.yoixColor(((YoixAWTTableColumn)comp).getVisitColor());
	return(obj);
    }


    private synchronized boolean
    isDisposable() {

	boolean  result = disposable;

	disposable = false;
	return(result);
    }


    private boolean
    isPopupMenu(Object comp) {

	return(comp instanceof PopupMenu);
    }


    private boolean
    isPopupOwner(Object comp) {

	//
	// No longer recall exactly why FileDialog is not allowed,
	// but undoubtedly there is (or was) a problem. Undoubtedly
	// needs a closer look - later. A better approach might be
	// to initialize a boolean based of the type represented
	// by this object and simply return that boolean.
	//

	return(comp instanceof Component && !(comp instanceof FileDialog));
    }


    private Dialog
    newDialog(YoixObject obj, boolean filedialog) {

	GraphicsConfiguration  gc;
	Object                 frame;

	gc = getGraphicsConfigurationFromScreen();

	if (obj.notNull()) {
	    if ((frame = obj.getManagedObject()) != null) {
		if (!(frame instanceof Frame))
		    frame = awtparent = new Frame();
	    } else VM.abort(TYPECHECK, N_PARENT);
	} else frame = awtparent = new Frame();

	return(filedialog
	    ? new YoixAWTFileDialog((Frame)frame)
	    : (Dialog)(new YoixAWTDialog(data, this, (Frame)frame, gc))
	);
    }


    private Window
    newWindow(YoixObject obj) {

	GraphicsConfiguration  gc;
	Window                 window = null;
	Object                 parent;

	gc = getGraphicsConfigurationFromScreen();

	if (obj.notNull()) {
	    if ((parent = obj.getManagedObject()) != null) {
		if (parent instanceof Frame)
		    window = (Window)(new YoixAWTWindow(data, this, (Frame)parent, gc));
		else if (parent instanceof Window)
		    window = (Window)(new YoixAWTWindow(data, this, (Window)parent, gc));
		else window = (Window)(new YoixAWTWindow(data, this, (Frame)null, gc));
	    } else VM.abort(TYPECHECK, N_PARENT);
	} else window = (Window)(new YoixAWTWindow(data, this, gc));

	return(window);
    }


    private void
    removeAdjustmentListener(ScrollPane scroller) {

	Adjustable  adjustable;

	if (scroller != null) {
	    if ((adjustable = scroller.getHAdjustable()) != null)
		adjustable.removeAdjustmentListener(this);
	    if ((adjustable = scroller.getVAdjustable()) != null)
		adjustable.removeAdjustmentListener(this);
	}
    }


    private void
    setAfterLoad(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    ((YoixAWTTableManager)comp).setAfterLoad(obj);
	else if (comp instanceof YoixAWTTableColumn)
	    ((YoixAWTTableColumn)comp).setAfterLoad(obj);
    }


    private void
    setAlignment(Object comp, YoixObject obj) {

	if (comp instanceof Label)
	    ((Label)comp).setAlignment(jfcInt("Label", obj.intValue()));
	else if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setAlignment(jfcInt("TextCanvasAlignment", obj.intValue()));
    }


    private void
    setAnchor(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setAnchor(obj.intValue());
    }


    private void
    setBlockIncrement(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    ((Scrollbar)comp).setBlockIncrement(Math.max(obj.intValue(), 1));
    }


    private void
    setBorder(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTCanvas)
	    ((YoixAWTCanvas)comp).setBorderInsets(YoixMakeScreen.javaInsets(obj));
    }


    private void
    setBorderColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTCanvas)
	    ((YoixAWTCanvas)comp).setBorderColor(YoixMake.javaColor(obj));
    }


    private void
    setCaret(Object comp, YoixObject obj) {

	if (comp instanceof TextComponent) {
	    try {
		((TextComponent)comp).setCaretPosition(Math.max(obj.intValue(), 0));
	    }
	    catch(RuntimeException e) {}
	}
    }


    private void
    setClickRadius(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    ((YoixAWTTableManager)comp).setClickRadius(YoixMakeScreen.javaDistance(obj));
    }


    private void
    setColumns(Object comp, YoixObject obj) {

	if (comp instanceof TextArea)
	    ((TextArea)comp).setColumns(Math.max(obj.intValue(), 0));
	else if (comp instanceof TextField)
	    ((TextField)comp).setColumns(Math.max(obj.intValue(), 0));
	else if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setColumns(Math.max(obj.intValue(), 0));
    }


    private void
    setCommand(Object comp, YoixObject obj) {

	if (comp instanceof Button) {
	    if (obj.isNull())
		((Button)comp).setActionCommand(null); // resets to default
	    else ((Button)comp).setActionCommand(obj.stringValue());
	} else if (comp instanceof YoixAWTCheckbox)
	    ((YoixAWTCheckbox)comp).setActionCommand(obj.stringValue());
    }


    private void
    setDirectory(Object comp, YoixObject obj) {

	if (comp instanceof FileDialog)
	    ((FileDialog)comp).setDirectory(YoixMisc.toLocalPath(obj.stringValue()));
    }


    private void
    setDispose(Object comp, YoixObject obj) {

	if (isWindow()) {
	    if (obj.booleanValue())
		dispose(false);
	}
    }


    private void
    setEcho(Object comp, YoixObject obj) {

	if (comp instanceof TextField)
	    ((TextField)comp).setEchoChar((char)obj.intValue());
    }


    private void
    setEdit(Object comp, YoixObject obj) {

	if (comp instanceof TextComponent)
	    ((TextComponent)comp).setEditable(obj.booleanValue());
	else if (comp instanceof YoixAWTTextTerm)
	    ((YoixAWTTextTerm)comp).setEditable(obj.booleanValue());
    }


    private void
    setEnabled(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    ((Component)comp).setEnabled(obj.booleanValue());
    }


    private void
    setFile(Object comp, YoixObject obj) {

	//
	// We have occasionally seen NullPointerExceptions using Java 1.5.0
	// on at least one platform. The try/catch eliminates the noise and
	// the FileDialog seems to behave properly.
	//

	if (comp instanceof FileDialog) {
	    try {
	        ((FileDialog)comp).setFile(YoixMisc.toLocalPath(obj.stringValue()));
	    }
	    catch(NullPointerException e) {}
	}
    }


    private void
    setFilters(Object comp, YoixObject obj) {

	FilenameFilter  filters;
	YoixRERegexp    re[];
	YoixObject      elem;
	int             length;
	int             n;

	if (comp instanceof YoixAWTFileDialog) {
	    filters = null;
	    if (obj.notNull() && ((length = obj.length()) > 0)) {
		re = new YoixRERegexp[length];
		for (n = obj.offset(); n < length; n ++) {
		    if ((elem = obj.getObject(n)) != null && elem.notNull()) {
			if (elem.isString() || elem.isRegexp()) {
			    re[n] = null;
			    if (elem.isString())
				re[n] = new YoixRERegexp(elem.stringValue(), SHELL_PATTERN);
			    else if (elem.isRegexp())
				re[n] = (YoixRERegexp)((YoixBodyRegexp)elem.body()).getManagedObject();
			    else VM.abort(BADVALUE, N_FILTERS, n);
			} else VM.abort(BADVALUE, N_FILTERS, n);
		    } else VM.abort(UNDEFINED, N_FILTERS, n);
		}
		filters = new YoixFilenameFilter(re);
	    }
	    ((YoixAWTFileDialog)comp).setFilenameFilter(filters);
	}
    }


    private void
    setGroup(Object comp, YoixObject obj) {

	Object  group;

	if (comp instanceof Checkbox) {
	    if (obj != null) {
		if (obj.notNull()) {
		    if (obj.isComponent()) {
			group = obj.getManagedObject();
			if (group instanceof CheckboxGroup)
			    ((Checkbox)comp).setCheckboxGroup((CheckboxGroup)group);
			else VM.abort(TYPECHECK, N_GROUP);
		    } else VM.abort(TYPECHECK, N_GROUP);
		} else ((Checkbox)comp).setCheckboxGroup(null);
	    }
	}
    }


    private void
    setHighlighted(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    ((YoixAWTTableManager)comp).setHighlighted(obj.intValue());
	else if (comp instanceof YoixAWTTableColumn)
	    ((YoixAWTTableColumn)comp).setHighlighted(obj.intValue());
    }


    private void
    setIconified(Object comp, YoixObject obj) {

	int  state;

	if (comp instanceof Frame) {
	    state = ((Frame)comp).getExtendedState();
	    if (obj.booleanValue())
		state |= Frame.ICONIFIED;
	    else state &= ~Frame.ICONIFIED;
	    ((Frame)comp).setExtendedState(state);
	}
    }


    private void
    setIndex(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager) {
	    if (obj.isInteger())
		((YoixAWTTableManager)comp).setIndexItem(obj.intValue());
	    else if (obj.notNull())
		VM.abort(TYPECHECK, N_INDEX);
	} else if (comp instanceof YoixAWTTableColumn)
	    ((YoixAWTTableColumn)comp).setIndexItem(obj.intValue());
	else if (comp instanceof YoixAWTList)
	    ((YoixAWTList)comp).makeVisible(obj.intValue());
    }


    private void
    setInputFilter(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    ((YoixAWTTableManager)comp).setInputFilter(obj);
    }


    private void
    setInsets(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setInsets(YoixMakeScreen.javaInsets(obj));
    }


    private void
    setIpad(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setIpad(YoixMakeScreen.javaInsets(obj));
    }


    private void
    setItems(Object comp, YoixObject obj) {

	if (comp instanceof MenuComponent) {
	    if (comp instanceof MenuBar)
		YoixMiscMenu.buildMenuBar(obj, (MenuBar)comp, true);
	    else if (comp instanceof PopupMenu)
		YoixMiscMenu.buildPopupMenu(obj, (PopupMenu)comp, true);
	} else if (comp instanceof Choice || comp instanceof List)
	    addItems(obj, V_ITEMS);
    }


    private void
    setLabels(Object comp, YoixObject obj) {

	if (comp instanceof Choice || comp instanceof List)
	    addItems(obj, V_LABELS);
    }


    private void
    setLocation(Object comp, YoixObject obj) {

	GraphicsConfiguration  gc;
	Rectangle              bounds;
	Point                  point;

	//
	// Think hard before accepting other components - it probably
	// only make sense for custom layout managers, which are not
	// currently implemented.
	//

	if (comp instanceof Component) {
	    if (comp instanceof YoixInterfaceWindow) {
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
		}
	    }
	}
    }


    private void
    setMappings(Object comp, YoixObject obj) {

	if (comp instanceof Choice || comp instanceof List)
	    addItems(obj, V_MAPPINGS);
    }


    private void
    setMaximized(Object comp, YoixObject obj) {

	int  state;

	if (comp instanceof Frame) {
	    state = ((Frame)comp).getExtendedState();
	    if (obj.booleanValue())
		state |= Frame.MAXIMIZED_BOTH;
	    else state &= ~Frame.MAXIMIZED_BOTH;
	    ((Frame)comp).setExtendedState(state);
	}
    }


    private void
    setMaximum(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    ((Scrollbar)comp).setMaximum(obj.intValue());
    }


    private void
    setMinimum(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    ((Scrollbar)comp).setMinimum(obj.intValue());
    }


    private void
    setModal(Object comp, YoixObject obj) {

	if (comp instanceof Dialog)		// should we skip FileDialogs??
	    ((Dialog)comp).setModal(obj.booleanValue());
    }


    private void
    setMode(Object comp, YoixObject obj) {

	if (comp instanceof FileDialog)
	    ((FileDialog)comp).setMode(jfcInt("FileDialog", obj.intValue()));
    }


    private void
    setMultipleMode(Object comp, YoixObject obj) {

	boolean  multiple;
	int      selected[];
	int      n;

	if (comp instanceof List) {
	    if ((multiple = obj.booleanValue()) == false) {
		selected = ((List)comp).getSelectedIndexes();
		for (n = 1; n < selected.length; n++)
		    ((List)comp).deselect(selected[n]);
	    }
	    ((List)comp).setMultipleMode(multiple);
	}
    }


    private void
    setOrientation(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    ((Scrollbar)comp).setOrientation(jfcInt("Scrollbar", obj.intValue()));
    }


    private void
    setOrigin(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setOrigin(YoixMakeScreen.javaPoint(obj));
	else if (comp instanceof YoixAWTTableManager)
	    ((YoixAWTTableManager)comp).setOrigin(YoixMakeScreen.javaPoint(obj));
    }


    private void
    setOutputFilter(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    ((YoixAWTTableManager)comp).setOutputFilter(obj);
    }


    private void
    setPreferredSize(Object comp, YoixObject obj) {

	if (comp instanceof Component)
	    validateRoot((Component)comp);
    }


    private void
    setPrompt(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextTerm)
	    ((YoixAWTTextTerm)comp).setPrompt(obj.stringValue());
    }


    private void
    setRowProperties(Object comp, YoixObject obj) {

	if (comp instanceof Component) {
	    data.put(N_ROWPROPERTIES, YoixObject.newArray(), false);
	    if (comp instanceof YoixAWTTableManager)
		((YoixAWTTableManager)comp).setRowProperties(obj);
	    else if (comp instanceof YoixAWTTableColumn)
		((YoixAWTTableColumn)comp).setRowProperties(obj);
	}
    }


    private void
    setRows(Object comp, YoixObject obj) {

	if (comp instanceof TextArea)
	    ((TextArea)comp).setRows(Math.max(obj.intValue(), 0));
	else if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setRows(Math.max(obj.intValue(), 0));
    }


    private void
    setSaveGraphics(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    ((YoixAWTTableManager)comp).setSaveGraphics(obj.booleanValue());
	else if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setSaveGraphics(obj.booleanValue());
    }


    private void
    setSaveLines(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextTerm)
	    ((YoixAWTTextTerm)comp).setSaveLines(obj.intValue());
    }


    private void
    setScrollPosition(Object comp, YoixObject obj) {

	if (comp instanceof ScrollPane) {
	    if (obj.notNull())
		((ScrollPane)comp).setScrollPosition(YoixMakeScreen.javaPoint(obj));
	}
    }


    private void
    setSelected(Object comp, YoixObject obj) {

	YoixObject  item;
	Hashtable   indexmap;
	Object      box;
	Object      value;
	String      items[];
	String      key;
	String      replacement;
	String      newtext;
	String      oldtext;
	int         selected[];
	int         index;
	int         start;
	int         end;
	int         n;

	//
	// Much of this, particularly the List code, is harder than you
	// should expect, but AWT is partly to blame. For example, JDK
	// 1.1.X implementations of Choice.select() want an upper bound
	// check, but List.select() is forgiving!!
	//
	// NOTE - bracketed everything with a try/catch because there's
	// so much going on here and so many chances for real trouble in
	// a mult-threaded application. Good enough for now, but needs a
	// closer look - later. Also decided not to touch the code inside
	// the try/catch for now, even though some of the checks are now
	// unnecessary.
	//

	try {
	    if (comp instanceof CheckboxGroup) {
		if (obj.notNull()) {
		    if (obj.isComponent()) {
			box = obj.getManagedObject();
			if (box instanceof Checkbox)
			    ((CheckboxGroup)comp).setSelectedCheckbox((Checkbox)box);
			else VM.abort(TYPECHECK, N_SELECTED);
		    } else VM.abort(TYPECHECK, N_SELECTED);
		} else ((CheckboxGroup)comp).setSelectedCheckbox(null);
	    } else if (comp instanceof Choice) {
		if (obj.isNumber()) {
		    if ((index = obj.intValue()) < ((Choice)comp).getItemCount())
			((Choice)comp).select(index);
		} else if (obj.isString()) {
		    if ((key = translateTo(obj.stringValue())) != null)
			((Choice)comp).select(key);
		} else if (obj.notNull())
		    VM.abort(TYPECHECK, N_SELECTED);
	    } else if (comp instanceof List) {
		items = ((List)comp).getItems();
		selected = ((List)comp).getSelectedIndexes();
		indexmap = new Hashtable();
		for (n = 0; n < items.length; n++)
		    indexmap.put(items[n], new Integer(n));
		for (n = 0; n < selected.length; n++)
		    ((List)comp).deselect(selected[n]);
		if (obj.isArray()) {
		    index = -1;
		    for (n = 0; n < obj.length(); n++) {
			if (obj.defined(n)) {
			    item = obj.get(n, false);
			    if (item.isString()) {
				index = -1;
				if ((key = translateTo(item.stringValue())) != null) {
				    if ((value = indexmap.get(key)) != null)
					index = ((Integer)value).intValue();
				}
			    } else index = item.intValue();
			    if (index >= 0)
				((List)comp).select(index);
			}
		    }
		    if (index >= 0)
			((List)comp).makeVisible(index);
		} else if (obj.isString()) {
		    if ((key = translateTo(obj.stringValue())) != null) {
			if ((value = indexmap.get(key)) != null) {
			    index = ((Integer)value).intValue();
			    ((List)comp).select(index);
			    ((List)comp).makeVisible(index);
			}
		    }
		} else if (obj.isNumber()) {
		    index = obj.intValue();
		    ((List)comp).select(index);
		    ((List)comp).makeVisible(index);
		} else if (obj.notNull())
		    VM.abort(TYPECHECK, N_SELECTED);
	    } else if (comp instanceof TextComponent) {
		if (obj.isString()) {
		    replacement = obj.stringValue();
		    start = ((TextComponent)comp).getSelectionStart();
		    end = ((TextComponent)comp).getSelectionEnd();
		    if (comp instanceof TextArea) {
			((TextArea)comp).replaceRange(replacement, start, end);
		    } else {
			oldtext = ((TextComponent)comp).getText();
			n = oldtext.length();
			newtext = oldtext.substring(0, start);
			newtext += replacement;
			if (n >= end)
			    newtext += oldtext.substring(end);
			((TextComponent)comp).setText(newtext);
		    }
		    end = start + replacement.length();
		    ((TextComponent)comp).setCaretPosition(end);
		} else VM.abort(TYPECHECK, N_SELECTED);
	    } else if (comp instanceof YoixAWTTableColumn) {
		if (obj.isString())
		    ((YoixAWTTableColumn)comp).setSelectedItem(obj.stringValue());
		else if (obj.notNull())
		    VM.abort(TYPECHECK, N_SELECTED);
	    }
	}
	catch(RuntimeException e) {}
    }


    private void
    setSelectedEnds(Object comp, YoixObject obj) {

	YoixObject  item;
	int         limit;
	int         start;
	int         end;
	int         n;

	if (comp instanceof TextComponent) {
	    if (obj.notNull()) {
		limit = ((TextComponent)comp).getText().length();
		n = obj.offset();
		if ((item = obj.getObject(n++)) != null && item.isNumber())
		    start = Math.min(Math.max(item.intValue(), 0), limit);
		else start = 0;
		if ((item = obj.getObject(n++)) != null && item.isNumber())
		    end = Math.min(Math.max(item.intValue(), start), limit);
		else end = limit;
	    } else {
		start = Math.max(data.getInt(N_CARET, 0), 0);
		end = start;
	    }
	    try {
		((TextComponent)comp).select(start, end);
		((TextComponent)comp).setCaretPosition(end);
	    }
	    catch(RuntimeException e) {}		// a precaution
	}
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
		    } else ((YoixInterfaceWindow)comp).pack();	// changed on 3/22/05
		}
	    } else if (comp instanceof YoixAWTCanvas && !(comp instanceof YoixAWTTableColumn)) {
		if (obj.notNull()) {
		    size = YoixMakeScreen.javaDimension(obj);
		    ((Component)comp).setSize(size);
		}
	    }
	}
    }


    private void
    setState(Object comp, YoixObject obj) {

	if (comp instanceof Checkbox)
	    ((Checkbox)comp).setState(obj.booleanValue());
	else if (comp instanceof YoixAWTCanvas)
	    ((YoixAWTCanvas)comp).setState(obj.intValue());
	else if (comp instanceof Frame)
	    ((Frame)comp).setExtendedState(obj.intValue());
    }


    private void
    setSyncViewport(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableManager)
	    ((YoixAWTTableManager)comp).setSyncViewport(obj);
	else if (comp instanceof YoixAWTTextComponent)
	    ((YoixAWTTextComponent)comp).setSyncViewport(obj);
    }


    private void
    setText(Object comp, YoixObject obj) {

	YoixObject  item;
	boolean     trim;
	String      str;
	int         n;

	//
	// Added invalidate() call when Label text is changed on 2/18/01.
	// Suspect the same should be done for other components too.
	//

	if (comp instanceof Component) {
	    data.put(N_TEXT, YoixObject.newString(), false);	// can now always toss
	    trim = data.getBoolean(N_AUTOTRIM);
	    if (comp instanceof YoixAWTTableManager) {
		if (obj.isString() || obj.isNull())
		    ((YoixAWTTableManager)comp).setText(obj.stringValue());
		else VM.abort(TYPECHECK, N_TEXT);
	    } else if (comp instanceof Label) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((Label)comp).setText(str);
		((Label)comp).invalidate();	// other components too??
	    } else if (comp instanceof TextArea) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((TextArea)comp).setText(str);
	    } else if (comp instanceof TextField) {
		str = trim ? obj.stringValue().trim() : YoixMisc.trim(obj.stringValue(), "", "\n");
		((TextField)comp).setText(str);
	    } else if (comp instanceof YoixAWTTextComponent) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((YoixAWTTextComponent)comp).setText(str);
	    } else if (comp instanceof Button) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((Button)comp).setLabel(str);
	    } else if (comp instanceof Checkbox) {
		str = trim ? obj.stringValue().trim() : obj.stringValue();
		((Checkbox)comp).setLabel(str);
	    }
	}
    }


    private void
    setTextMode(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextCanvas)
	    ((YoixAWTTextCanvas)comp).setTextMode(obj.intValue());
    }


    private void
    setTextWrap(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTextCanvas)
	    ((YoixAWTTextCanvas)comp).setTextWrap(obj.booleanValue());
    }


    private void
    setTitle(Object comp, YoixObject obj) {

	String  title;

	//
	// Resetting a title seems to make it disappear under some window
	// managers (e.g., fvwm) on some Unix systems. Never had a problem
	// on Windows and didn't bother to really track it down, except to
	// note that hiding and then showing the Frame made it appear with
	// the new title. The kludge that we added should be harmless and
	// only helps by skipping the setTitle() call when nothing would
	// change, which obviously isn't much of a fix!!
	//

	if (comp instanceof Component) {
	    if (comp instanceof Dialog) {
		if (!(comp instanceof FileDialog))
		    ((Dialog)comp).setTitle(obj.stringValue());
	    } else if (comp instanceof Frame) {
		title = obj.stringValue();
		if (title == null || title.equals(((Frame)comp).getTitle()) == false)
		    ((Frame)comp).setTitle(title);
	    }
	}
    }


    private void
    setUnitIncrement(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    ((Scrollbar)comp).setUnitIncrement(Math.max(obj.intValue(), 1));
    }


    private void
    setValue(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    ((Scrollbar)comp).setValue(obj.intValue());
    }


    private void
    setVisible(Object comp, YoixObject obj) {

	GraphicsDevice  screen;
	boolean         state;

	if (comp instanceof Component) {
	    state = obj.booleanValue();
	    if (comp instanceof YoixInterfaceWindow) {
		if (state != ((Component)comp).isVisible()) {
		    if (state) {
			windowActivate(this, comp);
			if (comp instanceof FileDialog) {
			    setField(N_DIRECTORY);	// JDK bug fix
			    setField(N_FILE);		// unnecessary??
			}
			if (isPacked() == false) {
			    ((YoixInterfaceWindow)comp).pack();
			    setPacked(true);
			}
		    } else windowDeactivate(this);

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
		    ((Component)comp).setVisible(state);
		    requestFirstFocus();
		    if (comp instanceof FileDialog && state) {
			state = ((Component)comp).isVisible();
			if (state == false)
			    windowDeactivate(this);
		    }
		    if (state == false) {
			if (data.getBoolean(N_AUTODISPOSE, false) == false)
			    childrenSetVisible(state);
			else dispose(false);
		    } else childrenSetVisible(state);
		}
		if (comp instanceof Frame) {
		    if (state && data.getBoolean(N_AUTODEICONIFY, false))
			((Frame)comp).setState(Frame.NORMAL);
		}
		//
		// Old versions did this in an else clause, which seemed
		// wrong, so we changed it on 11/28/04.
		//
		if (state && data.getBoolean(N_AUTORAISE, false)) {
		    if (!((comp instanceof Dialog) && ((Dialog)comp).isModal()))
			((YoixInterfaceWindow)comp).toFront();
		}
	    } else ((Component)comp).setVisible(state);
	} else if (comp instanceof PopupMenu) {
	    if (obj.booleanValue())
		showPopupMenu((PopupMenu)comp);
	} else if (comp instanceof MenuBar)
	    showMenuBar((MenuBar)comp, obj.booleanValue());
    }


    private void
    setVisibleAmount(Object comp, YoixObject obj) {

	if (comp instanceof Scrollbar)
	    ((Scrollbar)comp).setVisibleAmount(Math.max(obj.intValue(), 1));
    }


    private void
    setVisitColor(Object comp, YoixObject obj) {

	if (comp instanceof YoixAWTTableColumn)
	    ((YoixAWTTableColumn)comp).setVisitColor(YoixMake.javaColor(obj));
    }


    private void
    showMenuBar(MenuBar menubar, boolean visible) {

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
    showPopupMenu(PopupMenu popupmenu) {

	Object  owner;
	Point   loc;

	if (popupowner != null) {
	    if ((owner = popupowner.getManagedObject()) != null) {
		if (owner instanceof Component) {
		    if (((Component)owner).isShowing()) {
			loc = YoixMakeScreen.javaPoint(data.getObject(N_LOCATION));
			popupmenu.show((Component)owner, loc.x, loc.y);
		    }
		}
	    }
	}
    }


    private String
    translateFrom(String str) {

	Hashtable  translator;
	String     value;

	translator = this.translator;		// snapshot - probably unnecessary

	if (translator != null && str != null) {
	    if ((value = (String)translator.get(str)) != null)
		str = value;
	}

	return(str);
    }


    private String
    translateTo(String str) {

	Enumeration  enm;
	Hashtable    translator;
	String       key;

	//
	// Looks through the translator table for a string and returns
	// the associated value if found or the orignal string if not.
	// There's currently only one translator, so we decided this
	// lookup could be costly - lots of solutions if turns out to
	// be an issue.
	//

	translator = this.translator;		// snapshot - probably unnecessary

	if (translator != null && str != null) {
	    for (enm = translator.keys(); enm.hasMoreElements(); ) {
		key = (String)enm.nextElement();
		if (translator.get(key).equals(str)) {
		    str = key;
		    break;
		}
	    }
	}

	return(str);
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixFilenameFilter implements FilenameFilter {

	private YoixRERegexp  re[];

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixFilenameFilter(YoixRERegexp re[]) {

	    this.re = re;
	}

	///////////////////////////////////
	//
	// FilenameFilter Interface
	//
	///////////////////////////////////

	public final boolean
	accept(File dir, String filename) {

	    boolean  result = false;
	    int      n;

	    if (filename != null) {
		if (re != null) {
		    for (n = 0; n < re.length; n++) {
			if (re[n] != null && re[n].exec(filename, null)) {
			    result = true;
			    break;
			}
		    }
		} else result = true;
	    }
	    return(result);
	}
    }
}

