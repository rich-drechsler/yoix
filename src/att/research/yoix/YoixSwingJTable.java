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
import java.awt.datatransfer.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public
class YoixSwingJTable extends JTable

    implements MouseListener,
	       MouseMotionListener,
	       YoixAPI,
	       YoixConstants,
	       YoixConstantsJTable,
	       YoixConstantsSwing

{
    //
    // An extension to JTable mainly to consolidate inner classes and
    // MouseListener stuff here rather than in YoixBodyComponentSwing.
    //
    // TODO: get array abort message and make the subscript consistent
    // (currently sometimes it is absolute offset value and  sometimes
    // relative offset (i.e. offset()+n versus n).
    //
    // NOTE: static javaXXX() and yoixXXX() methods may be candidates
    // for inclusion in YoixMake.java, but if they're moved the aborts
    // should not be included, which means some arguments lists should
    // change. We'll leave them in here for now...
    //

    public static BevelBorder lowered = new BevelBorder(BevelBorder.LOWERED);
    public static BevelBorder raised  = new BevelBorder(BevelBorder.RAISED);

    private final static int CURRENCY_INSTANCE = 1;
    private final static int NUMBER_INSTANCE = 2;
    private final static int PERCENT_INSTANCE = 3;

    private final static Date MIN_DATE_VALUE = new Date(Long.MIN_VALUE);
    private final static Date MAX_DATE_VALUE = new Date(Long.MAX_VALUE);

    private Point                 presspoint = null;
    private YoixSwingTableColumn  presscolumn = null;

    private int pressedColumn = -1;
    private int pressedRow = -1;
    private boolean resized = false;
    private boolean resizable = false;

    private boolean useedithighlight = true;

    private boolean quiet = false;

    private YoixRERegexp matchRE = null;
    private String       matchMatrix[][] = null;
    private boolean      matchByCols = true;

    private boolean changeListenerMode = false;
    private boolean editListenerMode = false;
    private boolean editImportListenerMode = false;
    private boolean editKeyListenerMode = false;

    private Color      gridColor = null;
    private Color      altGridColor = null;
    private Dimension  defaultIntercellSpacing;

    private int visibleRows = 0;

    private int findRow = -1;
    private int findColumn = -1;
    private int foundRow = -1;
    private int foundColumn = -1;
    private boolean findmode = false;

    private boolean single_selection_mode = true; // set correctly later
    private int[] lastselection = null;

    private String  lastmatch = null;
    private boolean lastbycols = false;
    private boolean lastignorecase = false;
    private int     lastpattern = -999;

    protected static int typeValues[] = {
	YOIX_BOOLEAN_TYPE,
	YOIX_DATE_TYPE,
	YOIX_DOUBLE_TYPE,
	YOIX_HISTOGRAM_TYPE,
	YOIX_ICON_TYPE,
	YOIX_INTEGER_TYPE,
	YOIX_MONEY_TYPE,
	YOIX_OBJECT_TYPE,
	YOIX_PERCENT_TYPE,
	YOIX_STRING_TYPE,
	YOIX_TEXT_TYPE,
	YOIX_TIMER_TYPE
    };

    //
    // IMPORTANT: do not change the ordering of the elements in these
    // arrays without changing setColumn
    //

    private String  boolAttrs[] = {
	"decimalSeparatorAlwaysShown",
	"groupingUsed",
	"parseIntegerOnly",
	"zeroNotShown"
    };

    private String  intAttrs[] = {
	"groupingSize",
	"maximumFractionDigits",
	"maximumIntegerDigits",
	"minimumFractionDigits",
	"minimumIntegerDigits",
	"multiplier"
    };

    private String  nbrAttrs[] = {
	"overflow",
	"underflow"
    };

    private String  strAttrs[] = {
	"format",
	"negativePrefix",
	"negativeSuffix",
	"positivePrefix",
	"positiveSuffix",
	"lowSubstitute",
	"highSubstitute",
	"inputFormat",
	"timeZone",
	"inputTimeZone",
	"locale",
	"inputLocale"
    };

    //
    // Useful references
    //

    private YoixBodyComponentSwing  parent;
    private YoixObject              data;

    private Hashtable               tags = null;
    private YoixObject              validator = null;
    private YoixObject              allowedit = null;
    private YoixObject              afterselect = null;
    private ArrayList               selectionmarks = null;


    //
    // Mouse event related stuff - some states may not be implemented.
    //

    private static final int  AVAILABLE = 0;
    private static final int  PRESSED = 1;

    private int  mouse = AVAILABLE;

    private static String  INPUT_DELIMITER = "|";
    private static String  OUTPUT_DELIMITER = "|";
    private static String  RECORD_DELIMITER = "\n";

    private TableCellRenderer defaultHeaderRenderer = null;
    private YoixJTableModel  yjtm; // a convenience (vs. casting getModel())

    private int MINIMUM_ROW_HEIGHT;
    private int ROW_HEIGHT_EDITING_ADJUSTMENT = 4; // not worth getting UIBorder info, etc.
    private int rowHeightAdjustment = 0;

    private Color editforeground = null;
    private Color editbackground = null;

    //
    // Several things seem to be broken in BasicTableHeaderUI.java. We've
    // dealt directly with cursor problems in earlier releases, but there
    // also seems to be a problem with scrolling when columns are dragged
    // around. We now handle scrolling in columnMoved(), but if it's fixed
    // in Java and support for 1.4/1.5 is no longer needed, then be sure
    // to remove it.
    //

    private static boolean  DISABLE_SCROLLING = (YoixMisc.jvmCompareTo("1.6.0") >= 0);

    private boolean  disable_scrolling = false;

    //
    // Constants that identify actions that need to be carried out in the
    // event thread (via invokeLater() and handleRun()). There's only one
    // right now, so it's overkill, but there undoubtedly will be others.
    //

    private static final int  RUN_SCROLLTOVISIBLE = 1;
    private static final int  RUN_EDITORCARET = 2;
    private static final int  RUN_AFTERSELECT = 3;
    private static final int  RUN_UPDATESELECTIONMARKS = 4;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    public
    YoixSwingJTable(YoixObject data, YoixBodyComponentSwing parent) {

	super();

	// Cannot use this property because it triggers a stopCellEditing, when
	// perhaps you wanted to click on a cancelCellEditing button
	// putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

	MINIMUM_ROW_HEIGHT = getRawRowHeight();

	this.parent = parent;
	this.data = data;

	setModel(yjtm = new YoixJTableModel());
	setAutoCreateColumnsFromModel(false);
	// setAutoCreateRowSorter(false); // the default (and not in 1.5)
	setDefaultHeaderRenderer();

	setTableHeader(false);

	setDefaultRenderer(Boolean.class, new YoixJTableBooleanRenderer());
	setDefaultRenderer(Date.class, new YoixJTableDateRenderer());
	setDefaultRenderer(Double.class, new YoixJTableDoubleRenderer());
	setDefaultRenderer(YoixJTableHistogram.class, new YoixJTableHistogramRenderer());
	setDefaultRenderer(YoixJTableIcon.class, new YoixJTableIconRenderer());
	setDefaultRenderer(Integer.class, new YoixJTableIntegerRenderer());
	setDefaultRenderer(YoixJTableMoney.class, new YoixJTableMoneyRenderer());
	setDefaultRenderer(YoixJTableObject.class, new YoixJTableObjectRenderer());
	setDefaultRenderer(YoixJTablePercent.class, new YoixJTablePercentRenderer());
	setDefaultRenderer(String.class, new YoixJTableStringRenderer());
	setDefaultRenderer(YoixJTableText.class, new YoixJTableTextRenderer());
	setDefaultRenderer(YoixJTableTimer.class, new YoixJTableTimerRenderer());

	setDefaultEditor(Boolean.class, new YoixJTableBooleanEditor());
	setDefaultEditor(Date.class, new YoixJTableDateEditor());
	setDefaultEditor(Double.class, new YoixJTableDoubleEditor());
	setDefaultEditor(YoixJTableHistogram.class, new YoixJTableHistogramEditor());
	setDefaultEditor(YoixJTableIcon.class, new YoixJTableIconEditor());
	setDefaultEditor(Integer.class, new YoixJTableIntegerEditor());
	setDefaultEditor(YoixJTableMoney.class, new YoixJTableMoneyEditor());
	setDefaultEditor(YoixJTableObject.class, new YoixJTableObjectEditor());
	setDefaultEditor(YoixJTablePercent.class, new YoixJTablePercentEditor());
	setDefaultEditor(String.class, new YoixJTableStringEditor());
	setDefaultEditor(YoixJTableText.class, new YoixJTableTextEditor());
	setDefaultEditor(YoixJTableTimer.class, new YoixJTableTimerEditor());

	defaultIntercellSpacing = getIntercellSpacing();

	addMouseListener(new YoixJTableFindMarker());
    }

    ///////////////////////////////////
    //
    // MouseListener Methods
    //
    ///////////////////////////////////

    public void
    mouseClicked(MouseEvent e) {

    }


    public void
    mouseEntered(MouseEvent e) {

    }


    public void
    mouseExited(MouseEvent e) {

    }


    public void
    mousePressed(MouseEvent e) {

	YoixSwingTableColumn  column;
	TableColumnModel      columnModel;
	Component             comp = e.getComponent();
	Rectangle             hrect;
	int                   viewColumn;

	if (comp instanceof JTableHeader || comp instanceof JTable) {
	    presspoint = e.getPoint();
	    columnModel = getColumnModel();
	    viewColumn = columnModel.getColumnIndexAtX(e.getX());
	    pressedColumn = viewColumn;
	    pressedRow = -1;

	    if (comp instanceof JTableHeader) {
		resizable = ((JTableHeader)comp).getResizingAllowed();
		resized = false;
		if (viewColumn >= 0) {
		    hrect = ((JTableHeader)comp).getHeaderRect(viewColumn);
		    column = (YoixSwingTableColumn)(columnModel.getColumn(viewColumn));
		    column.setLowered(true);
		    getTableHeader().repaint(hrect);
		    presscolumn = column;
		}
	    } else pressedRow = rowAtPoint(e.getPoint());
	}
    }


    public void
    mouseReleased(MouseEvent e) {
	YoixSwingTableColumn  column;
	TableColumnModel      columnModel;
	Enumeration           enm;
	EventQueue            queue;
	YoixObject            obj;
	Component             comp = e.getComponent();
	Rectangle             hrect;
	Point                 pressed = presspoint;
	YoixSwingTableColumn  columned = presscolumn;
	AWTEvent              event;
	boolean               clicked;
	Point                 point;
	int                   viewColumn;
	int                   viewRow;
	int                   rowSnap;
	int                   colSnap;

	if (comp instanceof JTableHeader || comp instanceof JTable) {
	    presspoint = null;
	    presscolumn = null;
	    if (comp instanceof JTable || (pressed != null && YoixMisc.distance2(pressed, e.getPoint()) < 1.414214)) {
		if (comp instanceof JTableHeader)
		    disable_scrolling = false;
		colSnap = pressedColumn;
		rowSnap = pressedRow;
		pressedColumn = -1;
		pressedRow = -1;
		viewColumn = columnAtPoint(point = e.getPoint());
		viewRow = rowAtPoint(point);

		if (comp instanceof JTableHeader) {
		    resizable = false;
		    clicked = (viewColumn >= 0 && viewColumn == colSnap && !resized);
		    columnModel = getColumnModel();

		    if (colSnap >= 0) {
			if (clicked) {
			    hrect = getTableHeader().getHeaderRect(colSnap);
			    column = (YoixSwingTableColumn)(columnModel.getColumn(colSnap));
			    column.setLowered(false);
			    getTableHeader().repaint(hrect);
			}
		    }
		} else {
		    clicked = (viewColumn >= 0 && viewColumn == colSnap &&
			       viewRow >= 0 && viewRow == rowSnap);
		}

		if (clicked) {
		    obj = YoixMake.yoixType(T_INVOCATIONEVENT);
		    obj.put(N_MODIFIERS, YoixObject.newInt(YoixMiscJFC.cookModifiers(e)));
		    obj.put(N_PRESSED, YoixObject.newInt(YoixMiscJFC.getButtonsPressed(e)));
		    obj.put(N_LOCATION, YoixMakeScreen.yoixPoint(point));
		    obj.put(N_COORDINATES, obj.get(N_LOCATION, false));
		    obj.put(N_POPUPTRIGGER, YoixObject.newInt(e.isPopupTrigger()));

		    if (comp instanceof JTableHeader) {
			obj.putInt(N_ID, V_INVOCATIONACTION);

			obj.putInt("viewColumn", viewColumn);
			viewColumn = yoixConvertColumnIndexToModel(viewColumn);
			obj.putInt("valuesColumn", viewColumn);

			event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
		    } else {
			obj.putInt(N_ID, V_INVOCATIONSELECTION);

			//viewColumn = columnAtPoint(point);
			obj.putInt("viewColumn", viewColumn);
			if (viewColumn >= 0)
			    viewColumn = yoixConvertColumnIndexToModel(viewColumn);
			obj.putInt("valuesColumn", viewColumn);

			obj.putInt("viewRow", viewRow);
			if (viewRow >= 0)
			    viewRow = yoixConvertRowIndexToModel(viewRow);
			obj.putInt("valuesRow", viewRow);

			event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
		    }
		    if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
			queue.postEvent(event);
		}
	    } else if (columned != null && columned.getLowered()) {
		columned.setLowered(false);
		hrect = getTableHeader().getHeaderRect(yoixConvertColumnIndexToView(columned.getModelIndex()));
		getTableHeader().repaint(hrect);
	    }
	}
    }

    //
    // We put our own here mainly because we'd like the ALT modifier key
    // to start the editor and be processed so that the invocationEditKey reaches
    // the Yoix level even in those cases. Note: SHIFT and CTRL are used for
    // navigation through table (e.g., SHIFT-TAB) so still ignore those for
    // starting the editor.
    //

    protected boolean
    processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {

	YoixSwingTableColumn  tblcol;
	TableColumnModel      tcm;
	KeyListener           kls[];
	Component             comp;
	Rectangle             rect;
	KeyEvent              event;
	boolean               retValue;
	int                   count;
	int                   code;
	int                   row;
	int                   col;

	retValue = super.processKeyBinding(ks, e, condition, pressed);

	if (!retValue) {
	    if (condition == WHEN_ANCESTOR_OF_FOCUSED_COMPONENT && isFocusOwner() &&
	    !Boolean.FALSE.equals((Boolean)getClientProperty("JTable.autoStartsEdit"))) {
		comp = getEditorComponent();
		if (comp == null) {
		    // Only attempt to install the editor on a KEY_PRESSED,
		    if (e == null || e.getID() != KeyEvent.KEY_PRESSED) {
			return(false);
		    }
		    // ignore shift for starting editor
		    if ((code = e.getKeyCode()) == KeyEvent.VK_SHIFT || code == KeyEvent.VK_CONTROL)
			return(false);
		    // Try to install the editor
		    row = getSelectionModel().getLeadSelectionIndex();
		    col = getColumnModel().getSelectionModel().getLeadSelectionIndex();
		    if (row != -1 && col != -1 && !isEditing()) {
			if (!editCellAt(row, col, e))
			    return(false);
		    }
		    comp = getEditorComponent();
		    if (comp == null)
			return(false);
		}
	    } else return(false);
	} else if ((comp = getEditorComponent()) == null) {
	    if (pressed) {
		//
		// This code, which was added on 11/3/10, is supposed to make
		// sure the focus doesn't end up in a cell that's not visible.
		// Limiting the loop based on the count is just a precaution
		// that's supposed to make sure we don't loop forever, even
		// though it's hard to imagine how that could happen. Calling
		// super.processKeyBinding() here also gives us the chance to
		// easily avoid any possible infinite loop.
		//
		// NOTE - always initializing count to
		//
		//     getColumnCount()*getRowCount() - 1;
		//
		// probably wouldn't be unreasonable.
		//
		tcm = getColumnModel();
		code = e.getKeyCode();
		if (code == KeyEvent.VK_TAB || code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT)
		    count = getColumnCount() - 1;
		else if (code == KeyEvent.VK_ENTER)
		    count = getColumnCount()*getRowCount() - getRowCount();
		else count = getColumnCount()*getRowCount() - 1;	// just in case
		for (count = count; count > 0; count--) {
		    col = tcm.getSelectionModel().getLeadSelectionIndex();
		    tblcol = (YoixSwingTableColumn)tcm.getColumn(col);
		    if (tblcol.getWidth() > 0) {	// columns with zero width are hidden
			if ((row = getSelectionModel().getLeadSelectionIndex()) >= 0) {
			    if ((rect = getCellRect(row, col, true)) != null)
				scrollRectToVisible(rect);
			}
			break;
		    } else super.processKeyBinding(ks, e, condition, pressed);
		}
	    }
	    return(false);
	}

	if (e.getSource() == this) {
	    switch(e.getID()) {
	    case KeyEvent.KEY_PRESSED:
		kls = comp.getKeyListeners();
		for (int n = 0; n < kls.length; n++) {
		    if (kls[n] instanceof YoixJTableCellEditor) {
			event = new KeyEvent(comp, KeyEvent.KEY_PRESSED, e.getWhen(), e.getModifiers(), e.getKeyCode());
			((YoixJTableCellEditor)kls[n]).keyPressed(event);
			//if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
			//queue.postEvent(event);
		    }
		}
		break;
	    case KeyEvent.KEY_RELEASED:
		kls = comp.getKeyListeners();
		for (int n = 0; n < kls.length; n++) {
		    if (kls[n] instanceof YoixJTableCellEditor) {
			event = new KeyEvent(comp, KeyEvent.KEY_RELEASED, e.getWhen(), e.getModifiers(), e.getKeyCode());
			((YoixJTableCellEditor)kls[n]).keyReleased(event);
		    }
		}
		break;
	    case KeyEvent.KEY_TYPED:
		kls = comp.getKeyListeners();
		for (int n = 0; n < kls.length; n++) {
		    if (kls[n] instanceof YoixJTableCellEditor) {
			event = new KeyEvent(comp, KeyEvent.KEY_TYPED, e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar());
			((YoixJTableCellEditor)kls[n]).keyTyped(event);
		    }
		}
		break;
	    }
	}

	return(retValue);
    }

    ///////////////////////////////////
    //
    // MouseMotionListener Methods
    //
    ///////////////////////////////////

    public void
    mouseDragged(MouseEvent e) {

	if (e.getComponent() instanceof JTableHeader) {
	    if (DISABLE_SCROLLING)
		disable_scrolling = true;
	    if (resizable && !resized)
		resized = (getTableHeader().getResizingColumn() != null);
	}
    }


    public void
    mouseMoved(MouseEvent e) {

    }

    ///////////////////////////////////
    //
    // TableModelListener Methods
    //
    ///////////////////////////////////

    public void
    tableChanged(TableModelEvent e) {

	lastmatch = null;
	lastpattern = -999;
	findHighlight(-1, -1, false, false);
	try {
	    super.tableChanged(e);
	}
	catch(RuntimeException ex) {}
    }

    ///////////////////////////////////
    //
    // TableColumnModelListener Methods
    //
    ///////////////////////////////////

    public void
    columnAdded(TableColumnModelEvent e) {

	YoixObject  obj;
	EventQueue  queue;
	AWTEvent    event;

	lastmatch = null;
	lastpattern = -999;
	findHighlight(-1, -1, false, false);
	super.columnAdded(e);

	if (changeListenerMode) {
	    obj = YoixMake.yoixType(T_INVOCATIONEVENT);
	    obj.putInt(N_ID, V_INVOCATIONCHANGE);
		
	    obj.putString("change", "add");
	    obj.putInt("viewColumn", e.getToIndex());
	    obj.putInt("valuesColumn", yoixConvertColumnIndexToModel(e.getToIndex()));
	    event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
	    if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
		queue.postEvent(event);
	}
    }


    public void
    columnMoved(TableColumnModelEvent e) {
	YoixObject    obj;
	EventQueue    queue;
	AWTEvent      event;
	JTableHeader  header;
	TableColumn   tc;
	Rectangle     vis;
	Rectangle     cellBounds;

	//
	// NOTE - DefaultTableColumnModel.moveColumn() posts an event even
	// if the column hasn't moved, so we're called even if the column's
	// header was just pressed. If we're editing super.columnMoved(e)
	// calls removeEditor(), which essentially cancels the edit.
	//

	lastmatch = null;
	lastpattern = -999;
	findHighlight(-1, -1, false, false);
	super.columnMoved(e);

	//
	// Now scroll, if necessary.
	//

	if ((header = getTableHeader()) != null) {
	    if ((tc = header.getDraggedColumn()) != null) {
		vis = getVisibleRect();
		cellBounds = getCellRect(0, yoixConvertColumnIndexToView(tc.getModelIndex()), true);
		vis.x = cellBounds.x;
		vis.width = cellBounds.width;
		super.scrollRectToVisible(vis);
	    }
	}

	if (e.getFromIndex() != e.getToIndex()) {
	    if (changeListenerMode) {
		obj = YoixMake.yoixType(T_INVOCATIONEVENT);
		obj.putInt(N_ID, V_INVOCATIONCHANGE);
		
		obj.putString("change", "drag");
		obj.putInt("fromViewColumn", e.getFromIndex());
		obj.putInt("toViewColumn", e.getToIndex());
		obj.putInt("valuesColumn", yoixConvertColumnIndexToModel(e.getToIndex()));
		event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
		if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
		    queue.postEvent(event);
	    }
	}
    }


    public void
    columnRemoved(TableColumnModelEvent e) {

	YoixObject  obj;
	EventQueue  queue;
	AWTEvent    event;
	int         idx;

	lastmatch = null;
	lastpattern = -999;
	findHighlight(-1, -1, false, false);
	super.columnRemoved(e);

	if (changeListenerMode) {
	    obj = YoixMake.yoixType(T_INVOCATIONEVENT);
	    obj.putInt(N_ID, V_INVOCATIONCHANGE);
		
	    obj.putString("change", "remove");
	    obj.putInt("viewColumn", e.getFromIndex());
	    idx = e.getFromIndex();
	    if (idx >= 0 && idx < getColumnCount())
		obj.putInt("valuesColumn", convertColumnIndexToModel(e.getFromIndex()));
	    else obj.putInt("valuesColumn", -1);
	    event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
	    if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
		queue.postEvent(event);
	}
    }

    ///////////////////////////////////
    //
    // YoixSwingJTable Methods
    //
    ///////////////////////////////////

    protected final void
    addRowColors(Color fore, Color back) {

	Color  colors[];
	Color  newcolors[];
	int    cnt = getRowCount();
	int    n;

	newcolors = new Color[cnt];

	colors = yjtm.cellForegrounds;
	if (colors == null)
	    n = 0;
	else n = colors.length;
	if (cnt >= n && n > 0)
	    System.arraycopy(colors, 0, newcolors, 0, n);
	else n = 0;
	for (; n < cnt; n++)
	    newcolors[n] = fore;
	yjtm.cellForegrounds = newcolors;

	newcolors = new Color[cnt];

	colors = yjtm.cellBackgrounds;
	if (colors == null)
	    n = 0;
	else n = colors.length;
	if (cnt >= n && n > 0)
	    System.arraycopy(colors, 0, newcolors, 0, n);
	else n = 0;
	for (; n < cnt; n++)
	    newcolors[n] = back;
	yjtm.cellBackgrounds = newcolors;
    }


    final int
    adjustAlignment(int value) {

	return(parent.jfcInt("SwingHorizontalAlignment", value));
    }


    final synchronized YoixObject
    builtinAction(String name, YoixObject arg[]) {

	YoixJTableCellRenderer  renderer = null;
	YoixSwingTableColumn    tblcol;
	YoixSwingJComboBox      editor;
	TableColumnModel        tcm;
	YoixObject              result = null;
	YoixObject              yobj = null;
	YoixObject              yobj2;
	Boolean                 booleans[];
	boolean                 dataview = false;
	boolean                 valmode = false;
	boolean                 altmode = false;
	boolean                 setmode = false;
	boolean                 syncmode = true;
	boolean                 success = false;
	boolean                 useview = false;
	boolean                 range = false;
	boolean                 badvalue = false;
	Dimension               size = null;
	Point                   pt1;
	Point                   pt2;
	Rectangle               rect1;
	Rectangle               rect2;
	Component               cmpnt;
	Object                  selected;
	Object                  object;
	Object                  objarr[] = null;
	Object                  newvals[][] = null;
	String                  text;
	String                  option;
	String                  tips[];
	Font                    font = null;
	Color                   colors[];
	Color                   colors2[];
	Color                   colors3[];
	Color                   colors4[];
	Color                   color;
	Class                   typeclass;
	int                     ridxs[];
	int                     mode = -1;
	int                     selections[];
	int                     rows[];
	int                     cols[];
	int                     iarray[];
	int                     subaction = -1;
	int                     action = -1;
	int                     idx = -1;
	int                     midx = -1;
	int                     vidx = -1;
	int                     value = -1;
	int                     val = -1;
	int                     cidx = -1;
	int                     ridx = -1;
	int                     count;
	int                     ccnt;
	int                     rcnt;
	int                     len;
	int                     off;
	int                     caret;
	int                     sz;
	int                     m;
	int                     n;

	if (arg.length >= 1 && arg.length <= 7) {
	    if (arg[0].isInteger()) {
		action = arg[0].intValue();

		switch (action) {
		case YOIX_CLEAR_SELECTION:
		    if (arg.length == 1)
			clearSelection();
		    else VM.badCall(name);
		    break;

		case YOIX_EDIT_CANCEL:
		    if (arg.length == 1)
			yjtm.cancelEditing();
		    else VM.badCall(name);
		    break;

		case YOIX_EDIT_STOP:
		    if (arg.length == 1)
			yjtm.stopEditing();
		    else VM.badCall(name);
		    break;

		case YOIX_EDIT_START:
		    if (arg.length == 3 || arg.length == 4) {
			useview = false;
			if (arg.length == 4) {
			    if (arg[3].isInteger())
				useview = arg[3].booleanValue();
			    else VM.badArgument(name, 1);
			}
			if (arg[1].isInteger()) {
			    if (useview)
				ridx = arg[1].intValue();
			    else ridx = yoixConvertRowIndexToView(arg[1].intValue());
			} else {
			    VM.badArgument(name, 1);
			    ridx = -1; // for compiler
			}
			if (arg[2].isInteger()) {
			    if (useview)
				cidx = arg[2].intValue();
			    else cidx = yoixConvertColumnIndexToView(arg[2].intValue());
			} else {
			    VM.badArgument(name, 2);
			    cidx = -1; // for compiler
			}
			if (editCellAt(ridx, cidx)) {
			    Component comp = getEditorComponent();
			    MouseEvent event;
			    EventQueue queue;
 			    if (comp != null) {
 				changeSelection(ridx, cidx, false, false);
 				comp.requestFocusInWindow();
				event = new MouseEvent(this, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), MouseEvent.BUTTON1_MASK, comp.getX()+5, comp.getY()+5, 1, false);
				if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
				    queue.postEvent(event);
				if (comp instanceof JTextField) {
				    EventQueue.invokeLater(
					new YoixAWTInvocationEvent(
					    this,
					    new Object[] {new Integer(RUN_EDITORCARET), comp}
					    )
					);
				}
			    }
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_MAKE_CELL_VISIBLE:
		    if (arg.length == 3 || arg.length == 4) {
			useview = false;
			if (arg.length == 4) {
			    if (arg[3].isInteger())
				useview = arg[3].booleanValue();
			    else VM.badArgument(name, 1);
			}
			if (arg[1].isInteger()) {
			    if (useview)
				ridx = arg[1].intValue();
			    else ridx = yoixConvertRowIndexToView(arg[1].intValue());
			} else {
			    VM.badArgument(name, 1);
			    ridx = -1; // for compiler
			}
			if (arg[2].isInteger()) {
			    if (useview)
				cidx = arg[2].intValue();
			    else cidx = yoixConvertColumnIndexToView(arg[2].intValue());
			} else {
			    VM.badArgument(name, 2);
			    cidx = -1; // for compiler
			}
			Rectangle rect = getCellRect(ridx, cidx, false);
			if (rect != null)
			    scrollRectToVisible(rect);
		    } else VM.badCall(name);
		    break;

		case YOIX_MOVE_COLUMN:
		    if (arg.length == 3) {
			if (arg[1].isInteger()) {
			    if (arg[2].isInteger()) {
				idx = arg[1].intValue();
				midx = arg[2].intValue();
				ccnt = getColumnCount();
				if (ccnt > 1) {
				    if (idx < 0)
					idx = 0;
				    else if (idx >= ccnt)
					idx = ccnt - 1;
				    if (midx < 0)
					midx = 0;
				    else if (midx >= ccnt)
					midx = ccnt - 1;
				    if (idx != midx) {
					tcm = getColumnModel();
					tcm.moveColumn(idx, midx);
				    }
				}
			    } else VM.badArgument(name, 2);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_APPEND_ROWS:
		    mode = 2;
		    // fall through to...
		case YOIX_INSERT_ROWS:
		    if (mode < 0)
			mode = 0;
		    // fall through to...
		case YOIX_REPLACE_ROWS:
		    if (mode < 0)
			mode = 1;
		    // first arg is String or Array, only one that is required
		    // second arg, if present, is reference row number; if not present,
		    //     and adding, put at end; if inserting, put at beginning; if
		    //     replacing, it is an error
		    // third arg, if present,  and doing a replacement, then it is either the ending row number or an array of to be replaced row numbers
		    // last arg (thrid or fourth depending on if replacement mode, if present, boolean to sync rows (true by default)

		    if (((mode != 1 && arg.length >= (2)) || arg.length >= 3) &&
			((mode == 1 && arg.length <= 5) || arg.length <= 4)) {
			if (arg[1].notNull()) {
			    if (arg[1].isString())
				newvals = yjtm.processText(arg[1].stringValue(), getColumnCount());
			    else if (arg[1].isArray()) {
				if (arg[1].length() > 0) {
				    yobj = arg[1].get(0, false);
				    if (!yobj.isArray()) {
					yobj = YoixObject.newArray(1);
					yobj.put(0, arg[1], false);
					arg[1] = yobj;
				    }
				}
				newvals = yjtm.processValues(arg[1], getColumnCount());
			    } else VM.badArgument(name, 1);
			} else newvals = null;
			if (mode == 0)
			    ridx = 0;
			else ridx = getRowCount();
			ridxs = null;
			if (arg.length >= 3) {
			    if (arg[2].isInteger()) {
				ridx = arg[2].intValue();
				if (arg.length >= 4) {
				    if (mode == 1) {
					if (arg[3].isInteger() || arg[3].isNull() || arg[3].isArray()) {
					    if (arg[3].isInteger()) {
						midx = arg[3].intValue();
						if (midx < ridx) {
						    m = ridx;
						    ridx = midx;
						    midx = m;
						}
						if (ridx < 0)
						    ridx = 0;
						else if (ridx >= getRowCount())
						    mode = 2;
						if (midx < 0)
						    mode = 0;
						else if (midx >= getRowCount())
						    midx = getRowCount() - 1;
						if (mode == 1) {
						    ridxs = new int[midx - ridx + 1];
						    for (m = ridx, n = 0; m <= midx; m++, n++)
							ridxs[n] = m;
						}
					    } else if (arg[3].notNull()) {
						ridxs = new int[arg[3].sizeof()];
						for (m = arg[3].offset(), n = 0; n < ridxs.length; m++, n++) {
						    if ((yobj = arg[3].get(m, false)).isInteger()) {
							ridxs[n] = yobj.intValue();
						    } else VM.badArgumentValue(name, 3, n);
						}
					    }
					    if (arg.length >= 5) {
						if (arg[4].isInteger()) {
						    syncmode = arg[4].booleanValue();
						} else VM.badArgument(name, 4);
					    }
					} else VM.badArgument(name, 3);
				    } else{
					if (arg[3].isInteger()) {
					    syncmode = arg[3].booleanValue();
					    if (arg.length >= 5)
						VM.badCall(name);
					} else VM.badArgument(name, 3);
				    }
				}
			    } else VM.badArgument(name, 2);
			}
			yjtm.placeValues(mode, newvals, ridx, ridxs, syncmode);
		    } else VM.badCall(name);
		    break;

		case YOIX_DELETE_ROWS:
		    // first arg is starting view row
		    // second arg, if present, is ending view row (otherwise just one row)
		    // third arg, if present, boolean to sync rows (true by default)
		    // OR
		    // first arg is array of view rows
		    // second arg, if present, boolean to sync rows (true by default)

		    if (arg.length >= 2 && arg.length <= 4) {
			if (arg[1].isInteger()) {
			    ridx = arg[1].intValue();
			    midx = ridx;
			    if (arg.length >= 3) {
				if (arg[2].isInteger()) {
				    midx = arg[2].intValue();
				    if (arg.length >= 4) {
					if (arg[3].isInteger()) {
					    syncmode = arg[3].booleanValue();
					} else VM.badArgument(name, 3);
				    }
				} else VM.badArgument(name, 2);
			    }
			    yjtm.deleteRows(ridx, midx, syncmode);
			} else if (arg[1].notNull() && arg[1].isArray()) {
			    if (arg.length >= 3) {
				if (arg[2].isInteger()) {
				    syncmode = arg[2].booleanValue();
				} else VM.badArgument(name, 2);
			    } else if (arg.length >= 4)
				VM.badArgument(name, 3);
			    else syncmode = true;
			    rows = new int[arg[1].sizeof()];
			    idx = arg[1].offset();
			    for (int r = 0; r < rows.length; r++) {
				yobj = arg[1].getObject(r+idx);
				if (yobj.isInteger()) {
				    rows[r] = yobj.intValue();
				} else VM.badArgumentValue(name, 1, r);
			    }
			    yjtm.deleteRows(rows, syncmode);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_COLUMN_DATA_INDEX:
		    valmode = true;
		    // fall through to...
		case YOIX_GET_COLUMN_VIEW_INDEX:
		    if (arg.length == 2) {
			if (arg[1].isInteger()) {
			    if (valmode)
				cidx = yoixConvertColumnIndexToModel(arg[1].intValue());
			    else cidx = yoixConvertColumnIndexToView(arg[1].intValue());
			    result = YoixObject.newInt(cidx);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_ROW_DATA_INDEX:
		    valmode = true;
		    // fall through to...
		case YOIX_GET_ROW_VIEW_INDEX:
		    if (arg.length == 2) {
			if (arg[1].isInteger()) {
			    if (valmode)
				ridx = yoixConvertRowIndexToModel(arg[1].intValue());
			    else ridx = yoixConvertRowIndexToView(arg[1].intValue());
			    result = YoixObject.newInt(ridx);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_COLUMN_RECT:
		    if (arg.length >= 2 && arg.length <= 3) {
			if (arg[1].isInteger()) {
			    if (arg.length < 3 || arg[2].isInteger()) {
				cidx = arg[1].intValue();
				rect1 = getCellRect(0,cidx,true);
				rect2 = getVisibleRect();
				if (arg.length == 3 && arg[2].isInteger()) {
				    pt1 = new Point(rect1.x, rect2.y);
				    SwingUtilities.convertPointToScreen(pt1, this);
				    rect1.x = pt1.x;
				    rect1.y = pt1.y;
				}
				rect1.height = rect2.height;
				result = YoixMakeScreen.yoixRectangle(rect1);
			    } else VM.badArgument(name, 2);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_ROW_RECT:
		    if (arg.length >= 2 && arg.length <= 3) {
			if (arg[1].isInteger()) {
			    if (arg.length < 3 || arg[2].isInteger()) {
				ridx = arg[1].intValue();
				rect1 = getCellRect(ridx,0,true);
				rect2 = getVisibleRect();
				if (arg.length == 3 && arg[2].booleanValue()) {
				    pt1 = new Point(rect2.x, rect1.y);
				    SwingUtilities.convertPointToScreen(pt1, this);
				    rect1.x = pt1.x;
				    rect1.y = pt1.y;
				}
				rect1.width = rect2.width;
				result = YoixMakeScreen.yoixRectangle(rect1);
			    } else VM.badArgument(name, 2);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_VISIBLE_RECT:
		    if (arg.length == 1) {
			result = YoixMakeScreen.yoixRectangle(getVisibleRect());
		    } else VM.badCall(name);
		    break;

		case YOIX_FIND_CELL_AT:
		    if (arg.length == 2) {
			if (arg[1].isPoint()) {
			    pt1 = YoixMakeScreen.javaPoint(arg[1]);
			    ridx = rowAtPoint(pt1);
			    cidx = columnAtPoint(pt1);
			    result = YoixObject.newDictionary(4);
			    result.putInt("viewRow", ridx);
			    result.putInt("viewColumn", cidx);
			    if (ridx >= 0)
				ridx = yoixConvertRowIndexToModel(ridx);
			    if (cidx >= 0)
				cidx = yoixConvertColumnIndexToModel(cidx);
			    result.putInt("valuesRow", ridx);
			    result.putInt("valuesColumn", cidx);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;


		case YOIX_GET_ROW:
		    // valmode/altmode is implemented, apparently so allow 4 args
		    if (arg.length >= 2 && arg.length <= 4) {
			if (arg[1].isInteger()) {
			    ridx = arg[1].intValue();
			} else {
			    VM.badArgument(name, 1);
			    ridx = -1; // for compiler
			}
			valmode = false; // i.e., no view mode
			altmode = false; // i.e., not formatted
			if (arg.length >= 3) {
			    if (arg[2].isInteger())
				valmode = arg[2].booleanValue();
			    else VM.badArgument(name, 2);
			    if (arg.length == 4) {
				if (arg[3].isInteger())
				    altmode = arg[3].booleanValue();
				else VM.badArgument(name, 3);
			    }
			}
			if ((option = yjtm.getRow(ridx, valmode, altmode)) != null)
			    result = YoixObject.newString(option);
			else result = null;
		    } else VM.badCall(name);
		    break;

		case YOIX_RESET_VIEW:
		    if (arg.length == 1) {
			midx = 0;
			ccnt = getColumnCount();
			dataview = false;
			while(!dataview) {
			    dataview = true;
			    for (cidx = 0; cidx < ccnt; cidx++) {
				if ((idx = yoixConvertColumnIndexToView(cidx)) != cidx) {
				    dataview = false;
				    tcm = getColumnModel();
				    tcm.moveColumn(idx, cidx);
				    midx++;
				    break;
				}
			    }
			}
			yjtm.syncRowViews(true, true);
		    } else VM.badCall(name);
		    break;

		case YOIX_SYNCROWVIEWS:
		    if (arg.length >= 1 && arg.length <= 2) {
			dataview = false;
			if (arg.length == 2) {
			    if (arg[1].isInteger()) {
				dataview = arg[1].booleanValue();
			    }
			}
			yjtm.syncRowViews(dataview, true);
		    } else VM.badCall(name);
		    break;


		case YOIX_IS_CELL_SELECTED:
		    valmode = true;
		    // fall through to...
		case YOIX_IS_COLUMN_SELECTED:
		    altmode = true;
		    // fall through to...
		case YOIX_IS_ROW_SELECTED:
		    if (arg.length == 2 || arg.length == 3) {
			setmode = false;
			if (!valmode && arg.length == 2) {
			    if (arg[1].isInteger())
				idx = arg[1].intValue();
			    else VM.badArgument(name, 1);
			    if (altmode) {
				if (idx >= 0 && idx < getColumnCount()) {
				    setmode = isColumnSelected(idx);
				}
			    } else {
				if (idx >= 0 && idx < getRowCount()) {
				    setmode = isRowSelected(idx);
				}
			    }
			} else if (valmode && arg.length == 3) {
			    if (arg[1].isInteger())
				ridx = arg[1].intValue();
			    else VM.badArgument(name, 1);
			    if (arg[2].isInteger())
				cidx = arg[2].intValue();
			    else VM.badArgument(name, 2);
			    if (ridx >= 0 && ridx < getRowCount() &&
				cidx >= 0 && cidx < getColumnCount()) {
				setmode = isCellSelected(ridx, cidx);
			    }
			} else VM.badCall(name);
			result = YoixObject.newInt(setmode);
		    } else VM.badCall(name);
		    break;

		case YOIX_CELL_SELECTION:
		    valmode = true;
		    // fall through to...
		case YOIX_COLUMN_SELECTION:
		    altmode = true;
		    // fall through to...
		case YOIX_ROW_SELECTION:
		    if (arg.length <= 2) {
			if (arg.length == 2) {
			    if (arg[1].isInteger())
				setmode = arg[1].booleanValue();
			    else VM.badArgument(name, 1);
			    clearSelection();
			    if (valmode)
				setCellSelectionEnabled(setmode);
			    else if (altmode)
				setColumnSelectionAllowed(setmode);
			    else setRowSelectionAllowed(setmode);
			} else {
			    if (valmode)
				result = YoixObject.newInt(getCellSelectionEnabled());
			    else if (altmode)
				result = YoixObject.newInt(getColumnSelectionAllowed());
			    else result = YoixObject.newInt(getRowSelectionAllowed());
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_CELL_SELECTION:
		    if (arg.length == 1) {
			boolean[][] cellshot = yjtm.cellected;

			result = YoixObject.newArray(0, -1);
			if (cellshot != null && cellshot.length > 0) {
			    idx = 0;
			    for (m = 0; m < cellshot.length; m++) {
				for (n = 0; n < cellshot[0].length; n++) {
				    if (cellshot[m][n]) {
					ridx = yoixConvertRowIndexToView(m);
					cidx = yoixConvertColumnIndexToView(n);
					yobj = YoixObject.newDictionary(4);
					yobj.putInt("valuesRow", m);
					yobj.putInt("valuesColumn", n);
					yobj.putInt("viewRow", ridx);
					yobj.putInt("viewColumn", cidx);
					result.put(idx++, yobj, false);
				    }
				}
			    }
			}
			result.setGrowto(result.length());
			result.setGrowable(false);
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_ROW_SELECTION:
		    altmode = true;
		    // fall through to...
		case YOIX_GET_COLUMN_SELECTION:
		    if (arg.length == 1) {
			if (altmode)
			    selections = getSelectedRows(false);
			else selections = getSelectedColumns(false);
			result = YoixObject.newArray(selections.length);
			for (m = 0; m < selections.length; m++) {
			    if (altmode) {
				ridx = yoixConvertRowIndexToView(selections[m]);
				yobj = YoixObject.newDictionary(2);
				yobj.putInt("valuesRow", selections[m]);
				yobj.putInt("viewRow", ridx);
			    } else {
				cidx = yoixConvertColumnIndexToView(selections[m]);
				yobj = YoixObject.newDictionary(2);
				yobj.putInt("valuesColumn", selections[m]);
				yobj.putInt("viewColumn", cidx);
			    }
			    result.put(m, yobj, false);
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_ADD_COLUMN_SELECTION:
		    altmode = true;
		    // fall through to...
		case YOIX_SET_COLUMN_SELECTION:
		    if (arg.length <= 4) {
			// val: 0=>off, 1=>on, 2=>toggle
			val = 1;
			midx = cidx = -1;
			if (arg.length > 1) {
			    idx = 1;
			    if (arg[idx].isInteger()) {
				cidx = midx = arg[idx].intValue();
				if (midx < 0)
				    cidx = midx = 0;
				idx++;
				if (arg.length > idx) {
				    if (arg[idx].isInteger()) {
					cidx = arg[idx].intValue();
					idx++;
				    }
				}
			    }
			    if (arg.length > idx) {
				if (arg[idx].notNull() && arg[idx].isString()) {
				    option = arg[idx].stringValue().toLowerCase();
				    idx++;
				    if ("off".equals(option))
					val = 0;
				    else if ("on".equals(option))
					val = 1;
				    else if ("toggle".equals(option))
					val = 2;
				    else VM.abort(BADVALUE, name, idx);
				    if (arg.length > idx)
					VM.badCall(name);
				} else VM.badArgumentValue(name, idx);
			    }
			}
			if (getColumnSelectionAllowed()) {
			    ccnt = getColumnCount();
			    if (single_selection_mode) {
				if (val != 0 && (midx == -1 || midx != cidx))
				    VM.badCall(name);
				selections = getSelectedColumns(true);
				if (selections.length > 0 && selections[0] != midx)
				    clearSelection();
			    } else {
				if (!altmode)
				    clearSelection();
				if (midx == -1) {
				    midx = 0;
				    cidx = ccnt;
				}
			    }
			    if (midx >= ccnt)
				midx = (ccnt - 1);
			    if (midx < 0)
				midx = 0;
			    if (cidx >= ccnt)
				cidx = (ccnt - 1);
			    if (cidx < 0)
				cidx = 0;

			    if (midx > cidx) {
				idx = midx;
				midx = cidx;
				cidx = idx;
			    }
			    if (val == 0)
				removeColumnSelectionInterval(midx,cidx);
			    else if (val == 1)
				addColumnSelectionInterval(midx,cidx);
			    else toggleColumnSelectionInterval(midx,cidx);
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_ADD_ROW_SELECTION:
		    altmode = true;
		    // fall through to...
		case YOIX_SET_ROW_SELECTION:
		    if (arg.length <= 4) {
			// val: 0=>off, 1=>on, 2=>toggle
			val = 1;
			midx = ridx = -1;
			if (arg.length > 1) {
			    idx = 1;
			    if (arg[idx].isInteger()) {
				ridx = midx = arg[idx].intValue();
				if (midx < 0)
				    ridx = midx = 0;
				idx++;
				if (arg.length > idx) {
				    if (arg[idx].isInteger()) {
					ridx = arg[idx].intValue();
					idx++;
				    }
				}
			    }
			    if (arg.length > idx) {
				if (arg[idx].notNull() && arg[idx].isString()) {
				    option = arg[idx].stringValue().toLowerCase();
				    idx++;
				    if ("off".equals(option))
					val = 0;
				    else if ("on".equals(option))
					val = 1;
				    else if ("toggle".equals(option))
					val = 2;
				    else VM.abort(BADVALUE, name, idx);
				    if (arg.length > idx)
					VM.badCall(name);
				} else VM.badArgumentValue(name, idx);
			    }
			}
			if (getRowSelectionAllowed()) {
			    if ((rcnt = getRowCount()) > 0) {
				if (single_selection_mode) {
				    if (val != 0 && (midx == -1 || midx != ridx))
					VM.badCall(name);
				    selections = getSelectedRows(true);
				    if (selections.length > 0 && selections[0] != midx) {
					clearSelection();
				    }
				} else {
				    if (!altmode)
					clearSelection();
				    if (midx == -1) {
					midx = 0;
					ridx = rcnt - 1;
				    }
				}
				if (midx >= rcnt)
				    midx = (rcnt - 1);
				if (midx < 0)
				    midx = 0;
				if (ridx >= rcnt)
				    ridx = (rcnt - 1);
				if (ridx < 0)
				    ridx = 0;

				if (midx > ridx) {
				    idx = midx;
				    midx = ridx;
				    ridx = idx;
				}
				if (val == 0)
				    removeRowSelectionInterval(midx,ridx);
				else if (val == 1)
				    addRowSelectionInterval(midx,ridx);
				else toggleRowSelectionInterval(midx,ridx);
			    }
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_ADD_CELL_SELECTION:
		    altmode = true;
		    // fall through to...
		case YOIX_SET_CELL_SELECTION:
		    if (arg.length <= 6) {
			// val: 0=>off, 1=>on, 2=>toggle
			val = 1;
			rows = null;
			cols = null;
			range = false;
			if (arg.length > 1) {
			    idx = 1;
			    if (arg[idx].notNull() && arg[idx].isArray()) {
				sz = arg[idx].sizeof();
				if ((sz%2) != 0)
				    VM.abort(BADVALUE, name, idx);
				len = arg[idx].length();
				sz /= 2;
				rows = new int[sz];
				cols = new int[sz];
				for (m = 0, n = arg[idx].offset(); n < len; n+=2) {
				    yobj = arg[idx].get(n, false);
				    if (yobj.isInteger())
					rows[m] = yobj.intValue();
				    else VM.abort(BADVALUE, name, idx);
				    yobj = arg[idx].get(n+1, false);
				    if (yobj.isInteger())
					cols[m++] = yobj.intValue();
				    else VM.abort(BADVALUE, name, idx);
				}
			    } else if (arg[idx].isInteger()) {
				ridx = arg[idx].intValue();
				idx++;
				if (arg.length > idx) {
				    if (arg[idx].isInteger()) {
					cidx = arg[idx].intValue();
					idx++;
				    } else VM.badArgument(name, idx);
				    if (arg.length > idx && arg[idx].isInteger()) {
					rows = new int[2];
					cols = new int[2];
					rows[0] = ridx;
					cols[0] = cidx;
					ridx = arg[idx].intValue();
					idx++;
					if (arg.length > idx) {
					    if (arg[idx].isInteger()) {
						cidx = arg[idx].intValue();
						idx++;
					    } else VM.badArgument(name, idx);
					    rows[1] = ridx;
					    cols[1] = cidx;
					    range = true;
					} else VM.badCall(name);
				    } else {
					rows = new int[1];
					cols = new int[1];
					rows[0] = ridx;
					cols[0] = cidx;
				    }
				} else VM.badCall(name);
			    }
			    if (arg.length > idx) {
				if (arg[idx].notNull() && arg[idx].isString()) {
				    option = arg[idx].stringValue().toLowerCase();
				    idx++;
				    if ("off".equals(option))
					val = 0;
				    else if ("on".equals(option))
					val = 1;
				    else if ("toggle".equals(option))
					val = 2;
				    else VM.abort(BADVALUE, name, idx);
				    if (arg.length > idx)
					VM.badCall(name);
				} else VM.badArgument(name, idx);
			    }
			}
			if (yjtm != null)
			    yjtm.cellSelection(val, altmode, range, rows, cols);
		    } else VM.badCall(name);
		    break;


		case YOIX_SET_EDITOR_BOX:
		    setmode = true;
		    // fall through to...
		case YOIX_GET_EDITOR_BOX:
		    if ((setmode && arg.length == 3) || (!setmode && arg.length == 2)) {
			if (arg[1].notNull() && arg[1].isInteger()) {
			    midx = arg[1].intValue();
			    if ((ccnt = getColumnCount()) > 0 && midx >= 0 && midx < ccnt) {
				vidx = yoixConvertColumnIndexToView(midx);
				tcm = getColumnModel();
				tblcol = (YoixSwingTableColumn)(tcm.getColumn(vidx));
				if (setmode) {
				    if (arg[2].isNull() || arg[2].isJComboBox())
					tblcol.setEditor(this, arg[2], yjtm.getType(midx));
				    else VM.badArgument(name, 2);
				} else {
				    if ((result = tblcol.getEditor()) != null) {
					if (result.isJComboBox() == false)
					    result = null;
				    }
				}
			    } else VM.badArgumentValue(name, 1);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_FIND_MARKER:
		    if (arg.length == 1) {
			if (foundRow < 0 || foundColumn < 0)
			    result = YoixObject.newDictionary();
			else {
			    ridx = yoixConvertRowIndexToModel(foundRow);
			    cidx = yoixConvertColumnIndexToModel(foundColumn);
			    result = YoixObject.newDictionary(4);
			    result.putInt("valuesRow", ridx);
			    result.putInt("valuesColumn", cidx);
			    result.putInt("viewRow", foundRow);
			    result.putInt("viewColumn", foundColumn);
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_SET_FIND_MARKER:
		    if (arg.length == 3 || arg.length == 4) {
			if (arg[1].notNull() && arg[1].isInteger()) {
			    if (arg[2].notNull() && arg[2].isInteger()) {
				ridx = arg[1].intValue();
				cidx = arg[2].intValue();
				if (ridx < 0 || ridx >= getRowCount())
				    ridx = -1;
				if (cidx < 0 || cidx >= getColumnCount())
				    cidx = -1;
				if (arg.length == 4) {
				    if (arg[3].notNull() && arg[3].isInteger()) {
					dataview = arg[3].booleanValue();
				    } else VM.badArgument(name, 3);
				} else dataview = true;
				findHighlight(ridx, cidx, true, dataview);
			    } else VM.badArgument(name, 2);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;


		case YOIX_TABLE_RESORT:
		    if (arg.length == 1)
			yjtm.resortTable();
		    else VM.badCall(name);
		    break;

		case YOIX_TABLE_JOIN:
		case YOIX_TABLE_JOIN_RAW:		// probably undocumented
		    if (arg.length >= 1 && arg.length <= 7) {
			int     iarr[] = null;
			int     lastarg = arg.length;
			// initialize these for the compiler, but they are set below
			String  outdel = null;
			String  recdel = null;
			int     convert  = 0;
			int     rowcol = 0;
			boolean textbool = false;
			boolean viewbool = false;
			boolean csv = false;
			boolean raw = (action == YOIX_TABLE_JOIN_RAW);

			if (lastarg == 7 && arg[--lastarg].notNull()) {
			    if (arg[lastarg].isString()) {
				outdel = arg[lastarg].stringValue();
				recdel = yjtm.getRecordDelimiter();
				csv = ",".equals(outdel);
			    } else if (arg[lastarg].isArray()) {
				if ((sz = arg[lastarg].sizeof()) == 0) {
				    outdel = yjtm.getOutputDelimiter();
				    recdel = yjtm.getRecordDelimiter();
				} else if (sz == 1) {
				    recdel = yjtm.getRecordDelimiter();
				    yobj = arg[lastarg].get(arg[lastarg].offset(), false);
				    if (yobj.isNull())
					outdel = yjtm.getOutputDelimiter();
				    else if (yobj.isString()) {
					outdel = yobj.stringValue();
					csv = ",".equals(outdel);
				    } else VM.badArgument(name, lastarg);
				} else if (sz == 2) {
				    yobj = arg[lastarg].get((idx = arg[lastarg].offset()), false);
				    if (yobj.isNull())
					outdel = yjtm.getOutputDelimiter();
				    else if (yobj.isString()) {
					outdel = yobj.stringValue();
					csv = ",".equals(outdel);
				    } else VM.badArgument(name, lastarg);
				    yobj = arg[lastarg].get(idx+1, false);
				    if (yobj.isNull())
					recdel = yjtm.getRecordDelimiter();
				    else if (yobj.isString())
					recdel = yobj.stringValue();
				    else VM.badArgument(name, lastarg);
				} else VM.badArgument(name, lastarg);
			    } else VM.badArgument(name, lastarg);
			} else {
			    outdel = yjtm.getOutputDelimiter();
			    recdel = yjtm.getRecordDelimiter();
			}

			if (lastarg == 6) {
			    if (arg[--lastarg].isInteger()) {
				convert = arg[lastarg].intValue();
			    } else VM.badArgument(name, lastarg);
			} else convert = 0;

			if (lastarg == 5) {
			    if (arg[--lastarg].isInteger()) {
				rowcol = arg[lastarg].intValue();
			    } else VM.badArgument(name, lastarg);
			} else rowcol = 0;

			if (lastarg == 4) {
			    if (arg[--lastarg].isInteger()) {
				textbool = arg[lastarg].booleanValue();
			    } else VM.badArgument(name, lastarg);
			} else textbool = true;

			if (lastarg == 3) {
			    if (arg[--lastarg].isInteger()) {
				viewbool = arg[lastarg].booleanValue();
			    } else VM.badArgument(name, lastarg);
			} else viewbool = true;

			// at this point lastarg must be 1 or 2

			if (lastarg == 1 || arg[1].isNull()) {
			    iarr = new int[yjtm.getColumnCount()];
			    len = iarr.length;
			    for (n = 0; n < len; n++) {
				if (viewbool)
				    iarr[n] = n;
				else iarr[n] = yoixConvertColumnIndexToView(n);
			    }
			} else if (arg[1].isInteger()) {
			    iarr = new int[1];
			    idx = arg[1].intValue();
			    if (idx >= 0 && idx < yjtm.getColumnCount()) {
				if (viewbool)
				    iarr[0] = idx;
				else iarr[0] = yoixConvertColumnIndexToView(idx);
			    } else VM.badArgumentValue(name, 1, new String[] {"column index " + idx + " is invalid"});
			} else if (arg[1].isArray()) {
			    m = arg[1].offset();
			    len = arg[1].length();
			    if ((len-m) > 0) {
				iarr = new int[len-m];
				for (n = 0; m < len; m++, n++) {
				    yobj = arg[1].get(m, false);
				    if (yobj.isInteger()) {
					idx = yobj.intValue();
					if (idx >= 0 && idx < yjtm.getColumnCount()) {
					    if (viewbool)
						iarr[n] = idx;
					    else iarr[n] = yoixConvertColumnIndexToView(idx);
					} else VM.badArgumentValue(name, 1, n, new String[] {"column index " + idx + " is invalid"});
				    } else VM.badArgumentValue(name, 1, n);
				}
			    } else VM.badArgumentValue(name, 1);
			} else VM.badArgument(name, 1);
			if (textbool || rowcol >= 0)
			    objarr = new Object[iarr.length];
			else yobj = YoixObject.newArray(iarr.length);
			len = raw ? yjtm.getRawRowCount() : yjtm.getRowCount();
			for (n = 0; n < iarr.length; n++) {
			    object = yjtm.getColumnArray((YoixSwingTableColumn)(getColumnModel().getColumn(iarr[n])), viewbool, textbool || (convert != 0), (convert > 0), csv, raw);
			    if (textbool || (convert != 0) || rowcol >= 0) {
				if (object == null)
				    sz = 0;
				else {
				    objarr[n] = object;
				    if (textbool || (convert != 0))
					sz = ((String[])object).length;
				    else sz = ((YoixObject)object).length();
				    if (sz < len)
					len = sz;
				}
			    } else yobj.put(n, object == null ? YoixObject.newNull() : (YoixObject)object, false);
			}
			if (textbool) {
			    StringBuffer txtbuf = new StringBuffer(0);
			    if (rowcol > 0) {
				yobj = YoixObject.newArray(len);
				synchronized(txtbuf) {
				    for (n = 0; n < len; n++) {
					txtbuf.setLength(0);
					for (m = 0; m < objarr.length; m++) {
					    if (m > 0)
						txtbuf.append(outdel);
					    txtbuf.append(((String[])objarr[m])[n]);
					}
					yobj.put(n, YoixObject.newString(txtbuf.toString()), false);
				    }
				    result = yobj;
				}
			    } else if (rowcol < 0) {
				yobj = YoixObject.newArray(objarr.length);
				synchronized(txtbuf) {
				    for (m = 0; m < objarr.length; m++) {
					txtbuf.setLength(0);
					for (n = 0; n < len; n++) {
					    if (n > 0)
						txtbuf.append(recdel);
					    txtbuf.append(((String[])objarr[m])[n]);
					}
					yobj.put(m, YoixObject.newString(txtbuf.toString()), false);
				    }
				    result = yobj;
				}
			    } else {
				synchronized(txtbuf) {
				    for (n = 0; n < len; n++) {
					for (m = 0; m < objarr.length; m++) {
					    if (m > 0)
						txtbuf.append(outdel);
					    txtbuf.append(((String[])objarr[m])[n]);
					}
					txtbuf.append(recdel);
				    }
				    result = YoixObject.newString(txtbuf.toString());
				}
			    }
			} else if (convert != 0) {
			    yobj = YoixObject.newArray(len);
			    for (n = 0; n < len; n++) {
				yobj2 = YoixObject.newArray(objarr.length);
				for (m = 0; m < objarr.length; m++) {
				    yobj2.putString(m, ((String[])objarr[m])[n]);
				}
				yobj.put(n, yobj2, false);
			    }
			    result = yobj;
			} else if (rowcol > 0) {
			    yobj = YoixObject.newArray(len);
			    for (n = 0; n < len; n++) {
				yobj2 = YoixObject.newArray(objarr.length);
				for (m = 0; m < objarr.length; m++)
				    yobj2.put(m, ((YoixObject)objarr[m]).get(n, false), false);
				yobj.put(n, yobj2, false);
			    }
			    result = yobj;
			} else if (rowcol < 0) {
			    result = yobj;
			} else {
			    yobj = YoixObject.newArray(len * objarr.length);
			    for (n = 0, idx = 0; n < len; n++) {
				for (m = 0; m < objarr.length; m++)
				    yobj.put(idx++, ((YoixObject)objarr[m]).get(n, false), false);
			    }
			    result = yobj;
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_TABLE_SORT:
		    if (arg.length <= 2) {
			if (arg.length == 1 || arg[1].isNull())
			    yjtm.resetSort();
			else if (arg[1].isInteger()) {
			    iarray = new int[1];
			    idx = arg[1].intValue();
			    if (idx < 0) {
				if (-idx > yjtm.getColumnCount())
				    iarray[0] = 0;
				else iarray[0] = idx; //-((-idx - 1) + 1);
			    } else if (idx > 0) {
				if (idx > yjtm.getColumnCount())
				    iarray[0] = 0;
				else iarray[0] = idx; //(idx - 1) + 1;
			    } else iarray[0] = 0;
			    if (iarray[0] != 0)
				yjtm.sortTable(iarray);
			} else if (arg[1].isArray()) {
			    iarray = new int[arg[1].sizeof()];
			    for (m = 0, n = arg[1].offset(); m < iarray.length; m++, n++) {
				yobj = arg[1].get(n, false);
				if (yobj.isInteger()) {
				    idx = yobj.intValue();
				    if (idx < 0) {
					if (-idx > yjtm.getColumnCount())
					    iarray[m] = 0;
					else iarray[m] = idx; //-((-idx - 1) + 1);
				    } else if (idx > 0) {
					if (idx > yjtm.getColumnCount())
					    iarray[m] = 0;
					else iarray[m] = idx; //(idx - 1) + 1;
				    } else iarray[m] = 0;
				} else VM.badArgumentValue(name, 1, m);
			    }
			    yjtm.sortTable(iarray);
			} else VM.badArgument(name, 1);
			getTableHeader().repaint();
			yjtm.fireTableChanged(new TableModelEvent(yjtm));
		    } else VM.badCall(name);
		    break;

		case YOIX_SET_COLUMN:
		    // keep set/get_column readonly for now
		    VM.abort(INVALIDACCESS, name, new String[] { "SET_COLUMN" });
		    setmode = true;
		    // fall through to...
		case YOIX_GET_COLUMN:
		    if (arg.length == 2) {
			if (arg[1].isInteger()) {
			    idx = arg[1].intValue();
			    if (idx >= 0 && idx < yjtm.getColumnCount()) {
				midx = yoixConvertColumnIndexToView(idx);
				result = yjtm.getColumn((YoixSwingTableColumn)(getColumnModel().getColumn(midx)));
			    } else VM.badArgumentValue(name, 1, new String[] {"column index " + idx + " is invalid"});
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_SET_COLUMN_FIELD:
		    setmode = true;
		    // fall through to...
		case YOIX_GET_COLUMN_FIELD:
		    if (arg.length >= 3) {
			if (arg[1].isInteger())
			    midx = arg[1].intValue();
			else if (arg[1].notNull() && arg[1].isString()) {
			    if ((object = tags.get(arg[1].stringValue())) != null)
				midx = ((Integer)object).intValue();
			    else VM.badArgumentValue(name, 1);
			} else VM.badArgument(name, 1);
			if (midx >= 0 && midx < yjtm.getColumnCount()) {
			    vidx = yoixConvertColumnIndexToView(midx);
			    if (arg[2].notNull() && arg[2].isString() && (subaction = parent.getFieldCode(arg[2].stringValue())) != -1) {
				if ((setmode && arg.length == 4) || (!setmode && (arg.length == 3 || arg.length == 4 || arg.length == 5))) {
				    tcm = getColumnModel();
				    tblcol = (YoixSwingTableColumn)(tcm.getColumn(vidx));
				    if ((renderer = (YoixJTableCellRenderer)(tblcol.getCellRenderer())) == null)
					renderer = (YoixJTableCellRenderer)(getDefaultRenderer(getColumnClass(vidx)));
				    switch (subaction) {
				    case V_ALTALIGNMENT:
					altmode = true;
					// fall through to...
				    case V_ALIGNMENT:
					if (setmode) {
					    if (arg[3].isInteger()) {
						    if ((val = arg[3].intValue()) < 0) {
							    val = -1;
						    value = -1;
						} else value = adjustAlignment(val);
						if (altmode) {
						    idx = tblcol.getHeaderAlignment(yjtm.getHeaderAlignment(-1));
						    if (idx != val) {
							tblcol.setHeaderAlignments(val, value);
							yjtm.fireTableChanged(new TableModelEvent(yjtm));
						    }
						} else {
						    idx = tblcol.getAlignment(renderer.getDefaultAlignment());
						    if (idx != val) {
							tblcol.setAlignments(val, value);
							yjtm.fireTableChanged(new TableModelEvent(yjtm));
						    }
						}
					    } else VM.badArgument(name, 3);
					} else if (arg.length == 3) {
					    if (altmode)
						result = YoixObject.newInt(tblcol.getHeaderAlignment(yjtm.getHeaderAlignment(YOIX_CENTER)));
					    else result = YoixObject.newInt(tblcol.getAlignment(renderer.getDefaultAlignment()));
					} else VM.badCall(name);
					break;

				    case V_ALTBACKGROUND:
					altmode = true;
					// fall through to...
				    case V_ALTFOREGROUND:
					if (setmode) {
					    colors = javaColorArray(arg[3], name, ":SET_COLUMN_FIELD", altmode ? ":"+N_ALTBACKGROUND : ":"+N_ALTFOREGROUND);
					    if (altmode)
						tblcol.setHeaderBackgrounds(colors);
					    else tblcol.setHeaderForegrounds(colors);
					    java.awt.Rectangle hrect = getTableHeader().getHeaderRect(vidx);
					    getTableHeader().repaint(hrect);
					} else if (arg.length == 3) {
					    if (altmode)
						colors = tblcol.getHeaderBackgrounds(yjtm.headerBackgrounds);
					    else colors = tblcol.getHeaderForegrounds(yjtm.headerForegrounds);
					    result = yoixColorArray(colors);
					} else VM.badCall(name);
					break;

				    case V_ALTTOOLTIPTEXT:
					if (setmode) {
					    tblcol.setHeaderTip(arg[3].stringValue());
					} else if (arg.length == 3) {
					    result = YoixObject.newString(tblcol.getHeaderTip(null));
					} else VM.badCall(name);
					break;

				    case V_ALTFONT:
					altmode = true;
					// fall through to...
				    case V_FONT:
					if (setmode) {
					    font = null;
					    if (arg[3].isString() || arg[3].isFont())
						font = YoixMakeScreen.javaFont(arg[3]);
					    else VM.badArgument(name, 3);
					    if (altmode) {
						tblcol.setHeaderFont(font);
						java.awt.Rectangle hrect = getTableHeader().getHeaderRect(vidx);
						getTableHeader().repaint(hrect);
					    } else {
						tblcol.setFont(font);
						recomputeRowHeight();
						yjtm.fireTableChanged(new TableModelEvent(yjtm));
					    }
					} else if (arg.length == 3) {
					    if (altmode)
						font = tblcol.getHeaderFont(yjtm.getHeaderFont(getFont()));
					    else font = tblcol.getFont(getFont());
					    result = YoixMake.yoixFont(font);
					} else VM.badCall(name);
					break;

				    case V_ETC:
					if (setmode) {
					    tblcol.setEtc(arg[3]);
					    yjtm.fireTableChanged(new TableModelEvent(yjtm));
					} else if (arg.length == 3) {
					    result = tblcol.getEtc();
					} else VM.badCall(name);
					break;

				    case V_ATTRIBUTES:
					renderer = getColumnRenderer(tblcol, true);
					if (renderer == null) {
					    VM.abort(INTERNALERROR, name, new String[] {(setmode?"SET_COLUMN_FIELD:":"GET_COLUMN_FIELD:") + N_ATTRIBUTES + ": could not get renderer for column " + midx});
					}
					if (setmode) {
					    if (arg[3].isDictionary()) {
						if (arg[3].notNull()) {

						    for (n = 0; n < boolAttrs.length; n++) {
							if ((yobj2 = arg[3].getObject(boolAttrs[n])) != null || (yobj2 = arg[3].getObject(boolAttrs[n].toLowerCase())) != null) {
							    if (yobj2.isInteger()) {
								// these must be coordinated with boolAttrs
								switch (n) {
								case 0:
								    renderer.setDecimalSeparatorAlwaysShown(yobj2.booleanValue());
								    break;
								case 1:
								    renderer.setGroupingUsed(yobj2.booleanValue());
								    break;
								case 2:
								    renderer.setParseIntegerOnly(yobj2.booleanValue());
								    break;
								case 3:
								    renderer.setZeroNotShown(yobj2.booleanValue());
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized boolean attribute:" + boolAttrs[n]});
								}
							    } else {
								VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": non-boolean value for boolean attribute:" + boolAttrs[n]});
							    }
							}
						    }

						    for (n = 0; n < intAttrs.length; n++) {
							if ((yobj2 = arg[3].getObject(intAttrs[n])) != null || (yobj2 = arg[3].getObject(intAttrs[n].toLowerCase())) != null) {
							    if (yobj2.isInteger()) {
								// these must be coordinated with intAttrs
								switch (n) {
								case 0:
								    renderer.setGroupingSize(yobj2.intValue());
								    break;
								case 1:
								    renderer.setMaximumFractionDigits(yobj2.intValue());
								    break;
								case 2:
								    renderer.setMaximumIntegerDigits(yobj2.intValue());
								    break;
								case 3:
								    renderer.setMinimumFractionDigits(yobj2.intValue());
								    break;
								case 4:
								    renderer.setMinimumIntegerDigits(yobj2.intValue());
								    break;
								case 5:
								    renderer.setMultiplier(yobj2.intValue());
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized integer attribute:" + intAttrs[n]});
								}
							    } else {
								VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": non-integer value for integer attribute:" + intAttrs[n]});
							    }
							}
						    }

						    for (n = 0; n < nbrAttrs.length; n++) {
							if ((yobj2 = arg[3].getObject(nbrAttrs[n])) != null || (yobj2 = arg[3].getObject(nbrAttrs[n].toLowerCase())) != null) {
							    if (yobj2.isNumber()) {
								// these must be coordinated with nbrAttrs
								switch (n) {
								case 0:
								    renderer.setOverflow(yobj2.doubleValue());
								    break;
								case 1:
								    renderer.setUnderflow(yobj2.doubleValue());
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized number attribute:" + nbrAttrs[n]});
								}
							    } else {
								VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": non-number value for number attribute:" + nbrAttrs[n]});
							    }
							}
						    }

						    for (n = 0; n < strAttrs.length; n++) {
							if ((yobj2 = arg[3].getObject(strAttrs[n])) != null || (yobj2 = arg[3].getObject(strAttrs[n].toLowerCase())) != null) {
							    if (yobj2.notNull() && yobj2.isString()) {
								// these must be coordinated with strAttrs
								switch (n) {
								case 0:
								    renderer.setFormat(yobj2.stringValue());
								    break;
								case 1:
								    renderer.setNegativePrefix(yobj2.stringValue());
								    break;
								case 2:
								    renderer.setNegativeSuffix(yobj2.stringValue());
								    break;
								case 3:
								    renderer.setPositivePrefix(yobj2.stringValue());
								    break;
								case 4:
								    renderer.setPositiveSuffix(yobj2.stringValue());
								    break;
								case 5:
								    renderer.setLowSubstitute(new String[] { yobj2.stringValue() });
								    break;
								case 6:
								    renderer.setHighSubstitute(new String[] { yobj2.stringValue() });
								    break;
								case 7:
								    renderer.setInputFormat(yobj2.stringValue());
								    break;
								case 8:
								    if (renderer instanceof YoixJTableDateRenderer)
									((YoixJTableDateRenderer)renderer).setTimeZone(yobj2.stringValue());
								    break;
								case 9:
								    if (renderer instanceof YoixJTableDateRenderer)
									((YoixJTableDateRenderer)renderer).setInputTimeZone(yobj2.stringValue());
								    break;
								case 10:
								    renderer.setRendererLocale(yobj2.stringValue());
								    break;
								case 11:
								    renderer.setInputLocale(yobj2.stringValue());
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized string attribute:" + strAttrs[n]});
								}
							    } else if (yobj2.notNull() && yobj2.isArray()) {
								switch (n) {
								case 5:
								    renderer.setLowSubstitute(YoixMake.javaStringArray(yobj2));
								    break;
								case 6:
								    renderer.setHighSubstitute(YoixMake.javaStringArray(yobj2));
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized string array attribute:" + strAttrs[n]});
								}
							    } else {
								VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": null or non-string value for string attribute:" + strAttrs[n]});
							    }
							}
						    }
						}
					    } else VM.badArgument(name, 3);
					} else if (arg.length == 3) {
					    yobj = YoixObject.newDictionary(boolAttrs.length + intAttrs.length + nbrAttrs.length + strAttrs.length);

					    for (n = 0; n < boolAttrs.length; n++) {
						switch (n) {
						case 0:
						    yobj.putInt(boolAttrs[n], renderer.getDecimalSeparatorAlwaysShown());
						    break;
						case 1:
						    yobj.putInt(boolAttrs[n], renderer.getGroupingUsed());
						    break;
						case 2:
						    yobj.putInt(boolAttrs[n], renderer.getParseIntegerOnly());
						    break;
						case 3:
						    yobj.putInt(boolAttrs[n], renderer.getZeroNotShown());
						    break;
						default:
						    VM.abort(INTERNALERROR, name, new String[] {"GET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized boolean attribute:" + boolAttrs[n]});
						}
					    }
					    for (n = 0; n < intAttrs.length; n++) {
						switch (n) {
						case 0:
						    yobj.putInt(intAttrs[n], renderer.getGroupingSize());
						    break;
						case 1:
						    yobj.putInt(intAttrs[n], renderer.getMaximumFractionDigits());
						    break;
						case 2:
						    yobj.putInt(intAttrs[n], renderer.getMaximumIntegerDigits());
						    break;
						case 3:
						    yobj.putInt(intAttrs[n], renderer.getMinimumFractionDigits());
						    break;
						case 4:
						    yobj.putInt(intAttrs[n], renderer.getMinimumIntegerDigits());
						    break;
						case 5:
						    yobj.putInt(intAttrs[n], renderer.getMultiplier());
						    break;
						default:
						    VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized integer attribute:" + intAttrs[n]});
						}
					    }
					    for (n = 0; n < nbrAttrs.length; n++) {
						switch (n) {
						case 0:
						    yobj.putDouble(nbrAttrs[n], renderer.getOverflow());
						    break;
						case 1:
						    yobj.putDouble(nbrAttrs[n], renderer.getUnderflow());
						    break;
						default:
						    VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized number attribute:" + nbrAttrs[n]});
						}
					    }
					    for (n = 0; n < strAttrs.length; n++) {
						switch (n) {
						case 0:
						    yobj.putString(strAttrs[n], renderer.getFormat());
						    break;
						case 1:
						    yobj.putString(strAttrs[n], renderer.getNegativePrefix());
						    break;
						case 2:
						    yobj.putString(strAttrs[n], renderer.getNegativeSuffix());
						    break;
						case 3:
						    yobj.putString(strAttrs[n], renderer.getPositivePrefix());
						    break;
						case 4:
						    yobj.putString(strAttrs[n], renderer.getPositiveSuffix());
						    break;
						case 5:
						    yobj.putObject(strAttrs[n], yoixStringArray(renderer.getLowSubstitute(), true));
						    break;
						case 6:
						    yobj.putObject(strAttrs[n], yoixStringArray(renderer.getHighSubstitute(), true));
						    break;
						case 7:
						    yobj.putString(strAttrs[n], renderer.getInputFormat());
						    break;
						case 8:
						    if (renderer instanceof YoixJTableDateRenderer)
							yobj.putString(strAttrs[n], ((YoixJTableDateRenderer)renderer).getTimeZone().getID());
						    break;
						case 9:
						    if (renderer instanceof YoixJTableDateRenderer)
							yobj.putString(strAttrs[n], ((YoixJTableDateRenderer)renderer).getInputTimeZone().getID());
						    break;
						case 10:
						    yobj.putString(strAttrs[n], renderer.getRendererLocale());
						    break;
						case 11:
						    yobj.putString(strAttrs[n], renderer.getInputLocale());
						    break;
						default:
						    VM.abort(INTERNALERROR, name, new String[] {"SET_COLUMN_FIELD:" + N_ATTRIBUTES + ": unrecognized string attribute:" + strAttrs[n]});
						}
					    }
					    result = yobj;
					} else VM.badCall(name);
					break;

					// should add CELLEDITOR set/get tblcol.getEditor (probably just get whatever valid
					// value was passwd to tblcol.setEditor)

				    case V_BACKGROUND:
					altmode = true;
					// fall through to...
				    case V_FOREGROUND:
					if (setmode) {
					    if (arg[3].isNull())
						color = null;
					    else if (arg[3].isColor())
						color = YoixMake.javaColor(arg[3]);
					    else {
						VM.badArgument(name, 3);
						color = null; // for compiler
					    }
					    if (altmode)
						tblcol.setBackground(color);
					    else tblcol.setForeground(color);
					    repaint();
					} else if (arg.length == 3) {
					    if (altmode)
						color = tblcol.getBackground(getBackground());
					    else color = tblcol.getForeground(getForeground());
					    if (color != null)
						result = YoixMake.yoixColor(color);
					} else VM.badCall(name);
					break;

				    case V_CELLCOLORS:
					if (setmode) {
					    if (arg[3].isNull()) {
						colors = null;
						colors2 = null;
						colors3 = null;
						colors4 = null;
					    } else if (arg[3].isArray()) {
						if ((sz = arg[3].sizeof()) <= 4) {
						    colors = null;
						    colors2 = null;
						    colors3 = null;
						    colors4 = null;
						    len = arg[3].length();
						    for (n = arg[3].offset(); n <len; n++) {
							yobj = arg[3].get(n, false);
							switch (n) {
							    case 0:
								colors = javaColorArray(yobj, name, ":SET_COLUMN_FIELD", ":"+N_CELLCOLORS + "[" + n + "]");
								break;

							    case 1:
								colors2 = javaColorArray(yobj, name, ":SET_COLUMN_FIELD", ":"+N_CELLCOLORS + "[" + n + "]");
								break;

							    case 2:
								colors3 = javaColorArray(yobj, name, ":SET_COLUMN_FIELD", ":"+N_CELLCOLORS + "[" + n + "]");
								break;

							    case 3:
								colors4 = javaColorArray(yobj, name, ":SET_COLUMN_FIELD", ":"+N_CELLCOLORS + "[" + n + "]");
								break;
							}
						    }
						} else {
						    VM.badArgumentValue(name, 3, new String[] { NL, "\tReason: too many array components" });
						    colors = null; // for compiler
						    colors2 = null;
						    colors3 = null;
						    colors4 = null;
						}
					    } else {
						VM.badArgument(name, 3);
						colors = null; // for compiler
						colors2 = null;
						colors3 = null;
						colors4 = null;
					    }

					    tblcol.setCellBackgrounds(colors);
					    tblcol.setCellForegrounds(colors2);
					    tblcol.setCellSelectionBackgrounds(colors3);
					    tblcol.setCellSelectionForegrounds(colors4);
					    repaint();
					} else if (arg.length == 3) {
					    colors = tblcol.getCellBackgrounds(null);
					    colors2 = tblcol.getCellForegrounds(null);
					    colors3 = tblcol.getCellSelectionBackgrounds();
					    colors4 = tblcol.getCellSelectionForegrounds();

					    if ((colors != null && colors.length > 0) || (colors2 != null && colors2.length > 0) || (colors3 != null && colors3.length > 0) || (colors4 != null && colors4.length > 0)) {
						yobj = YoixObject.newArray(2, 4);
						yobj.putObject(0, yoixColorArray(colors));
						yobj.putObject(1, yoixColorArray(colors2));
						if ((colors3 != null && colors3.length > 0) || (colors4 != null && colors4.length > 0)) {
						    yobj.putObject(2, yoixColorArray(colors3));
						    yobj.putObject(3, yoixColorArray(colors4));
						}
						yobj.setGrowable(false);
						result = yobj;
					    }
					} else VM.badCall(name);
					break;

				    case V_DISABLEDBACKGROUND:
					altmode = true;
					// fall through to...
				    case V_DISABLEDFOREGROUND:
					if (setmode) {
					    if (arg[3].isNull())
						color = null;
					    else if (arg[3].isColor())
						color = YoixMake.javaColor(arg[3]);
					    else {
						VM.badArgument(name, 3);
						color = null; // for compiler
					    }
					    if (altmode)
						tblcol.setDisabledBackground(color);
					    else tblcol.setDisabledForeground(color);
					    repaint();
					} else if (arg.length == 3) {
					    if (altmode)
						color = tblcol.getDisabledBackground(null);
					    else color = tblcol.getDisabledForeground(null);
					    if (color != null)
						result = YoixMake.yoixColor(color);
					} else VM.badCall(name);
					break;

				    case V_EDIT:
					if (setmode) {
					    if (arg[3].isInteger())
						object = arg[3].booleanValue() ? Boolean.TRUE : Boolean.FALSE;
					    else object = javaBooleanArray(arg[3], name, ":SET_COLUMN_FIELD", ":"+ N_EDIT);
					    yjtm.cancelEditing();
					    tblcol.setEditInfo(object);
					    repaint();
					} else if (arg.length == 3) {
					    result = tblcol.yoixEditInfo();
					} else VM.badCall(name);
					break;

				    case V_EDITBACKGROUND:
					altmode = true;
					// fall through to...
				    case V_EDITFOREGROUND:
					if (setmode) {
					    if (arg[3].isNull())
						color = null;
					    else if (arg[3].isColor())
						color = YoixMake.javaColor(arg[3]);
					    else {
						VM.badArgument(name, 3);
						color = null; // for compiler
					    }
					    DefaultCellEditor dce = (DefaultCellEditor)(tblcol.getCellEditor());
					    if (dce == null)
						dce = (DefaultCellEditor)(getDefaultEditor(getTypeClass(yjtm.getType(midx))));
					    Component dcc = dce.getComponent();
					    if (altmode) {
						tblcol.setEditBackground(color);
						dcc.setBackground(color);
					    } else {
						tblcol.setEditForeground(color);
						dcc.setForeground(color);
					    }
					} else if (arg.length == 3) {
					    if (altmode)
						color = tblcol.getEditBackground(getEditBackground(null));
					    else color = tblcol.getEditForeground(getEditForeground(null));
					    if (color != null)
						result = YoixMake.yoixColor(color);
					} else VM.badCall(name);
					break;

				    case V_HEADER:
					if (setmode) {
					    if (arg[3].notNull() && arg[3].isString()) {
						// columnNames size should be correct
						yjtm.columnNames[midx] = arg[3].stringValue();
						tblcol.setHeaderValue(yjtm.columnNames[midx]);
						getTableHeader().repaint(getTableHeader().getHeaderRect(vidx));
					    } else VM.badArgument(name, 4, new String[] { "SET_COLUMN_FIELD:" + N_HEADER });
					} else if (arg.length == 3) {
					    result = (object = tblcol.getHeaderValue()) == null ? YoixObject.newNull() : YoixObject.newString(object.toString());
					} else VM.badCall(name);
					break;

				    case V_HEADERICONS:
					if (setmode) {
					    Icon[][] icons = yjtm.headerIcons;
					    if (icons == null || icons.length <= getColumnCount()) {
						Icon[][] itmp;
						if (icons == null) {
						    idx = 0;
						    itmp = new Icon[getColumnCount()][];
						} else {
						    itmp = new Icon[getColumnCount()][];
						    if (icons.length == 1) {
							for (idx=0; idx<itmp.length; idx++)
							    itmp[idx] = icons[0];
						    } else {
							for (idx=0; idx<icons.length; idx++)
							    itmp[idx] = icons[idx];
						    }
						}
						for (; idx<itmp.length; idx++)
						    itmp[idx] = null;
						icons = itmp;
					    }
					    icons[midx] = javaIconArray(arg[3], name, ":SET_COLUMN_FIELD", ":"+N_HEADERICONS);
					    yjtm.headerIcons = icons;
					    tblcol.setHeaderIcons(yjtm.headerIcons[midx]);
					    java.awt.Rectangle hrect = getTableHeader().getHeaderRect(vidx);
					    getTableHeader().repaint(hrect);
					} else if (arg.length == 3) {
					    result = yoixIconArray(tblcol.getHeaderIcons());
					} else VM.badCall(name);
					break;

				    case V_SELECTIONBACKGROUND:
					altmode = true;
					// fall through to...
				    case V_SELECTIONFOREGROUND:
					if (setmode) {
					    if (arg[3].isNull())
						color = null;
					    else if (arg[3].isColor())
						color = YoixMake.javaColor(arg[3]);
					    else {
						VM.badArgument(name, 3);
						color = null; // for compiler
					    }
					    if (altmode)
						tblcol.setSelectionBackground(color);
					    else tblcol.setSelectionForeground(color);
					    repaint();
					} else if (arg.length == 3) {
					    if (altmode)
						color = tblcol.getSelectionBackground(null);
					    else color = tblcol.getSelectionForeground(null);
					    if (color != null)
						result = YoixMake.yoixColor(color);
					} else VM.badCall(name);
					break;

				    case V_STATE:
					if (setmode) {
					    VM.abort(INVALIDACCESS, name, new String[] {"SET_COLUMN_FIELD:" + N_STATE});
					} else if (arg.length == 3) {
					    result = YoixObject.newInt(yjtm.getState(midx));
					} else VM.badCall(name);
					break;

				    case V_TAG:
					if (setmode) {
					    if (arg[3].notNull() && arg[3].isString()) {
						tags.remove(tblcol.getTag());
						tblcol.setTag(arg[3].stringValue());
						tags.put(tblcol.getTag(), new Integer(midx));
					    } else VM.abort(BADVALUE, name, new String[] {"SET_COLUMN_FIELD:" + N_TAG});
					} else if (arg.length == 3) {
					    result = YoixObject.newString(tblcol.getTag());
					} else VM.badCall(name);
					break;

				    case V_TEXT:
					if (setmode) {
					    if (arg[3].notNull() && arg[3].isString()) {
						yjtm.addColumnText(arg[3].stringValue(), midx, yjtm.getType(midx), name + ":SET_COLUMN_FIELD:" + N_TEXT + "[" + midx + "]");
					    } else VM.abort(BADVALUE, name, 3, new String[] {"SET_COLUMN_FIELD:" + N_TEXT});
					} else {
					    useview = false; // i.e., view row order
					    altmode = false; // i.e., formatted
					    if (arg.length > 3) {
						if (arg[3].isInteger())
						    useview = arg[3].booleanValue();
						else VM.abort(BADVALUE, name, 3, new String[] {"GET_COLUMN_FIELD:" + N_TEXT});
						if (arg.length > 4) {
						    if (arg[4].isInteger())
							altmode = arg[4].booleanValue();
						    else VM.abort(BADVALUE, name, 4, new String[] {"GET_COLUMN_FIELD:" + N_TEXT});
						}
					    }
					    result = yjtm.columnText(midx, useview, altmode);
					}
					break;

				    case V_TOOLTIP:
				    case V_TOOLTIPTEXT:
					if (setmode) {
					    tips = javaStringArray(arg[3], name, ":SET_COLUMN_FIELD", ":"+ N_TOOLTIPTEXT);
					    tblcol.setTipText(tips);
					} else if (arg.length == 3) {
					    result = yoixStringArray(tblcol.getToolTips());
					} else VM.badCall(name);
					break;

				    case V_TYPE:
					if (setmode) {
					    // keep type readonly for now
					    VM.abort(INVALIDACCESS, name, new String[] { "SET_COLUMN_FIELD: " + N_TYPE });
					} else if (arg.length == 3) {
					    result = YoixObject.newInt(yjtm.getType(midx));
					} else VM.badCall(name);
					break;

				    case V_VALUES:
					if (setmode) {
					    if (arg[3].notNull() && arg[3].isArray()) {
						yjtm.addColumnValues(arg[3], midx, yjtm.getType(midx), name + ":SET_COLUMN_FIELD:" + N_TEXT + "[" + midx + "]");
					    } else VM.abort(BADVALUE, name, 3, new String[] {"SET_COLUMN_FIELD:" + N_VALUES});
					} else if (arg.length < 5) {
					    if (arg.length == 4) {
						if (arg[3].isInteger())
						    useview = arg[3].booleanValue();
						else VM.abort(BADVALUE, name, 3, new String[] {"GET_COLUMN_FIELD:" + N_TEXT});
					    } else useview = false;
					    result = yjtm.columnValues(midx, useview);
					} else VM.badCall(name);
					break;

				    case V_VIEWS:
					if (setmode) {
					    VM.abort(INVALIDACCESS, name, new String[] { "SET_COLUMN_FIELD: " + N_VIEWS });
					} else if (arg.length < 5) {
					    if (arg.length == 4) {
						if (arg[3].isInteger())
						    useview = arg[3].booleanValue();
						else VM.abort(BADVALUE, name, 3, new String[] {"GET_COLUMN_FIELD:" + N_TEXT});
					    } else useview = false;
					    yobj = yjtm.columnValues(midx, useview);
					    if (yobj.notNull()) {
						len = getRowCount(); // this is the visible row count, of course
						result = YoixObject.newArray(len);
						for (idx = 0; idx < len; idx++) {
						    result.putObject(idx, yobj.getObject(yoixConvertRowIndexToModel(idx)));
						}
					    }
					} else VM.badCall(name);
					break;

				    case V_VALUE:
					if (setmode) {
					    VM.abort(INVALIDACCESS, name, new String[] {"SET_COLUMN_FIELD:" + N_VALUE});
					} else if (arg.length == 3) {
					    result = YoixObject.newInt(midx);
					} else VM.badCall(name);
					break;

				    case V_VIEW:
					if (setmode) {
					    VM.abort(INVALIDACCESS, name, new String[] {"SET_COLUMN_FIELD:" + N_VIEW});
					} else if (arg.length == 3) {
					    result = YoixObject.newInt(vidx);
					} else VM.badCall(name);
					break;

				    case V_VISIBLE:
					if (setmode) {
					    if (arg[3].isNumber()) {
						tblcol.setVisible(arg[3].booleanValue());
					    } else VM.abort(BADVALUE, name, 3, new String[] {"SET_COLUMN_FIELD:" + N_VISIBLE});
					} else if (arg.length == 3) {
					    result = YoixObject.newInt(tblcol.isVisible());
					} else VM.badCall(name);
					break;

				    case V_WIDTH:
					if (setmode) {
					    badvalue = true;
					    if (arg[3].isNull())
						badvalue = false;
					    else if (arg[3].isNumber()) {
						badvalue = false;
						size = YoixMakeScreen.javaDimension(arg[3], arg[3]);
						value = size.width;
						if (value >= 0)
						    tblcol.setPreferredWidth(value);
					    } else if (arg[3].notNull() && arg[3].isDictionary()) {
						if ((yobj = arg[3].getObject(N_WIDTH)) != null && yobj.isNumber()) {
						    badvalue = false;
						    size = YoixMakeScreen.javaDimension(yobj, yobj);
						    value = size.width;
						    if (value >= 0)
							tblcol.setPreferredWidth(value);
						}
						if ((yobj = arg[3].getObject(N_MINIMUM)) != null && yobj.isNumber()) {
						    badvalue = false;
						    size = YoixMakeScreen.javaDimension(yobj, yobj);
						    value = size.width;
						    if (value >= 0)
							tblcol.setMinWidth(value);
						}
						if ((yobj = arg[3].getObject(N_MAXIMUM)) != null && yobj.isNumber()) {
						    badvalue = false;
						    size = YoixMakeScreen.javaDimension(yobj, yobj);
						    value = size.width;
						    if (value >= 0)
							tblcol.setMaxWidth(value);
						}
					    }
					    //
					    // Added on 6/2/07 to make sure column width
					    // changes made after the table's size is set
					    // initially by YoixBodyComponentSwing.java
					    // actually accomplish something. It's needed
					    // because a JTable is Scrollable and we always
					    // put it in a JScrollPane and that means the
					    // table's getPreferredScrollableViewportSize()
					    // method is used by the JScrollPane's layout
					    // manager when it picks a size for the table.
					    // Probably still need to be thoroughly tested.
					    //
					    if (arg[3].notNull() && badvalue == false) {
						if ((size = getPreferredSize()) != null) {
						    value = size.width;
						    if ((size = getPreferredScrollableViewportSize()) != null) {
							size.width = value;
							setPreferredScrollableViewportSize(size);
						    }
						}
					    }
					} else if (arg.length == 3) {
					    yobj = YoixMakeScreen.yoixDimension(tblcol.getWidth(), 0);
					    result = yobj.get(N_WIDTH, false);
					} else VM.badCall(name);
					break;

				    default:
					VM.badArgumentValue(name, 2);
					break;
				    }
				    if (setmode) {
					repaint();
				    }
				} else VM.badCall(name);
			    } else VM.badArgument(name, 2);
			} else VM.badArgumentValue(name, 1, new String[] {"column index " + midx + " is invalid"});
		    } else VM.badCall(name);
		    break;

		case YOIX_SET_FIELD:
		    setmode = true;
		    // fall through to...
		case YOIX_GET_FIELD:
		    if (arg.length >= 3 && arg.length <= 5) {
			if (arg[1].isInteger())
			    ridx = arg[1].intValue();
			else {
			    VM.badArgument(name, 1);
			    ridx = -1; // for compiler
			}
			if (arg[2].isInteger())
			    cidx = arg[2].intValue();
			else {
			    VM.badArgument(name, 2);
			    cidx = -1; // for compiler
			}
			if (setmode) {
			    if (arg.length > 3) {
				yobj = arg[3];
				if (arg.length == 4 || arg[4].isNull()) {
				    if (yobj.isString())
					valmode = false;
				    else valmode = true;
				} else if (arg[4].isString()) {
				    if (N_TEXT.equalsIgnoreCase(arg[4].stringValue().trim()))
					valmode = false;
				    else if (N_VALUE.equalsIgnoreCase(arg[4].stringValue().trim()))
					valmode = true;
				    else VM.badArgumentValue(name, 4);
				} else VM.badArgument(name, 4);
				object = null;
				if (valmode) {
				    success = false;
				    switch (yjtm.getType(cidx)) {
				    case YOIX_BOOLEAN_TYPE:
					if (yobj.isInteger()) {
					    object = yobj.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
					    success = true;
					}
					break;
				    case YOIX_DATE_TYPE:
					if (yobj.isNumber()) {
					    object = new Date((long)(1000.0*yobj.doubleValue()));
					    success = true;
					}
					break;
				    case YOIX_DOUBLE_TYPE:
					if (yobj.isNumber()) {
					    object = new Double(yobj.doubleValue());
					    success = true;
					}
					break;
				    case YOIX_HISTOGRAM_TYPE:
					if (yobj.isNumber()) {
					    object = new YoixJTableHistogram(yobj.doubleValue());
					    success = true;
					}
					break;
				    case YOIX_ICON_TYPE:
					if (yobj.notNull()) {
					    if (yobj.isImage()) {
						Image   image;
						String  source;
						String  desc;
						if ((image = YoixMake.javaImage(yobj)) != null) {
						    desc = yobj.get(N_DESCRIPTION, false).stringValue();
						    if ((yobj2=yobj.get(N_SOURCE, false)).notNull() && yobj2.isString())
							source = yobj2.stringValue();
						    else if (desc != null)
							source = "<" + desc + ">";
						    else source = "<internal>";
						    object = new YoixJTableIcon(image, source, desc);
						} else object = new YoixJTableIcon("<unavailable>");
						success = true;
						
					    } else if (yobj.isString()) {
						object = new YoixJTableIcon(yobj.stringValue());
						success = true;
					    }
					}
					break;
				    case YOIX_INTEGER_TYPE:
					if (yobj.isNumber()) {
					    object = new Integer(yobj.intValue());
					    success = true;
					}
					break;
				    case YOIX_MONEY_TYPE:
					if (yobj.isNumber()) {
					    object = new YoixJTableMoney(yobj.doubleValue());
					    success = true;
					}
					break;
				    case YOIX_OBJECT_TYPE:
					if (yobj.isString()) {
					    object = new YoixJTableObject(yobj.stringValue());
					    success = true;
					}
					break;
				    case YOIX_PERCENT_TYPE:
					if (yobj.isNumber()) {
					    object = new YoixJTablePercent(yobj.doubleValue());
					    success = true;
					}
					break;
				    case YOIX_STRING_TYPE:
					if (yobj.isString()) {
					    object = new String(yobj.stringValue());
					    success = true;
					}
					break;
				    case YOIX_TEXT_TYPE:
					if (yobj.isString()) {
					    object = new YoixJTableText(yobj.stringValue());
					    success = true;
					}
					break;
				    case YOIX_TIMER_TYPE:
					if (yobj.isNumber()) {
					    object = new YoixJTableTimer(yobj.doubleValue());
					    success = true;
					}
					break;
				    default:
					VM.abort(INTERNALERROR);
					break;
				    }
				} else if (yobj.isNull())
				    success = true;
				else if (yobj.isString()) {
				    object = yjtm.processField(yobj.stringValue(), yjtm.getType(cidx), cidx, name + ": " + N_YOIX_SET_FIELD);
				    success = true;
				} else VM.badArgument(name, 3);
				if (success) {
				    if (allowEdit(ridx, cidx, false))
					yjtm.checkAndSetValueAt(ridx, cidx, object, true);
				} else VM.badArgumentValue(name, 3);
			    } else VM.badCall(name);
			} else {
			    if (arg.length != 5) {
				altmode = false;
				if (arg.length == 3 || arg[3].isNull()) 
				    valmode = false;
				else if (arg[3].isString()) {
				    if (N_TEXT.equalsIgnoreCase(arg[3].stringValue().trim()))
					valmode = false;
				    else if (N_VIEW.equalsIgnoreCase(arg[3].stringValue().trim())) {
					valmode = false;
					altmode = true;
				    } else if (N_VALUE.equalsIgnoreCase(arg[3].stringValue().trim()))
					valmode = true;
				    else VM.badArgumentValue(name, 3);
				} else VM.badArgument(name, 3);
				if (valmode)
				    result = yjtm.yoixObjectForTypedValue(yjtm.getType(cidx), yjtm.getRawValueAt(ridx, cidx), false);
				else {
				    midx = yoixConvertColumnIndexToView(cidx);
				    tcm = getColumnModel();
				    tblcol = (YoixSwingTableColumn)(tcm.getColumn(midx));
				    renderer = getColumnRenderer(tblcol, false);
				    result = YoixObject.newString(renderer.stringValue(yjtm.getRawValueAt(ridx, cidx), altmode));
				}
			    } else VM.badCall(name);
			}
		    } else VM.badCall(name);
		    break;


		case YOIX_SET_TYPE_FIELD:
		    setmode = true;
		    // fall through to...
		case YOIX_GET_TYPE_FIELD:
		    if (arg.length >= 3) {
			if (arg[1].isInteger())
			    idx = arg[1].intValue();
			else VM.badArgument(name, 1);
			if ((typeclass = getTypeClass(idx)) != null) {
			    if (arg[2].notNull() && arg[2].isString() && (subaction = parent.getFieldCode(arg[2].stringValue())) != -1) {
				if ((setmode && arg.length == 4) || (!setmode && arg.length == 3)) {
				    renderer = (YoixJTableCellRenderer)getDefaultRenderer(typeclass);
				    switch (subaction) {
				    case V_ALIGNMENT:
					if (setmode) {
					    if (arg[3].isInteger()) {
						if ((val = arg[3].intValue()) < 0) {
						    val = -1;
						    value = -1;
						} else value = adjustAlignment(val);
						idx = renderer.getDefaultHorizontalAlignment();
						renderer.setDefaultHorizontalAlignment(value);
						value = renderer.getDefaultHorizontalAlignment();
						if (idx != value) {
						    yjtm.fireTableChanged(new TableModelEvent(yjtm));
						}
					    } else VM.badArgument(name, 3);
					} else result = YoixObject.newInt(renderer.getDefaultAlignment());
					break;

				    case V_ATTRIBUTES:
					if (setmode) {
					    if (arg[3].isDictionary()) {
						if (arg[3].notNull()) {
						    for (n = 0; n < boolAttrs.length; n++) {
							if ((yobj2 = arg[3].getObject(boolAttrs[n])) != null || (yobj2 = arg[3].getObject(boolAttrs[n].toLowerCase())) != null) {
							    if (yobj2.isInteger()) {
								// these must be coordinated with boolAttrs
								switch (n) {
								case 0:
								    renderer.setDecimalSeparatorAlwaysShown(yobj2.booleanValue());
								    break;
								case 1:
								    renderer.setGroupingUsed(yobj2.booleanValue());
								    break;
								case 2:
								    renderer.setParseIntegerOnly(yobj2.booleanValue());
								    break;
								case 3:
								    renderer.setZeroNotShown(yobj2.booleanValue());
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized boolean attribute:" + boolAttrs[n]});
								}
							    } else {
								VM.abort(BADVALUE, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": non-boolean value for boolean attribute:" + boolAttrs[n]});
							    }
							}
						    }

						    for (n = 0; n < intAttrs.length; n++) {
							if ((yobj2 = arg[3].getObject(intAttrs[n])) != null || (yobj2 = arg[3].getObject(intAttrs[n].toLowerCase())) != null) {
							    //
							    // This code aborted if the integer was less than zero,
							    // but that didn't agree with SET_COLUMN_FIELD, so we
							    // decided to be more forgiving here. Looks like all the
							    // methods called here handle negative numbers, as do the
							    // corresponding methods in DecimalFormat or NumberFormat.
							    //
							    if (yobj2.isInteger()) {
								m = yobj2.intValue();
								// these must be coordinated with intAttrs
								switch (n) {
								case 0:
								    renderer.setGroupingSize(m);
								    break;
								case 1:
								    renderer.setMaximumFractionDigits(m);
								    break;
								case 2:
								    renderer.setMaximumIntegerDigits(m);
								    break;
								case 3:
								    renderer.setMinimumFractionDigits(m);
								    break;
								case 4:
								    renderer.setMinimumIntegerDigits(m);
								    break;
								case 5:
								    renderer.setMultiplier(m);
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized integer attribute:" + intAttrs[n]});
								}
							    } else {
								VM.abort(BADVALUE, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": non-integer value for integer attribute:" + intAttrs[n]});
							    }
							}
						    }

						    for (n = 0; n < nbrAttrs.length; n++) {
							if ((yobj2 = arg[3].getObject(nbrAttrs[n])) != null || (yobj2 = arg[3].getObject(nbrAttrs[n].toLowerCase())) != null) {
							    if (yobj2.isNumber()) {
								// these must be coordinated with nbrAttrs
								switch (n) {
								case 0:
								    renderer.setOverflow(yobj2.doubleValue());
								    break;
								case 1:
								    renderer.setUnderflow(yobj2.doubleValue());
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized number attribute:" + nbrAttrs[n]});
								}
							    } else {
								VM.abort(BADVALUE, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": non-number value for number attribute:" + nbrAttrs[n]});
							    }
							}
						    }

						    for (n = 0; n < strAttrs.length; n++) {
							if ((yobj2 = arg[3].getObject(strAttrs[n])) != null || (yobj2 = arg[3].getObject(strAttrs[n].toLowerCase())) != null) {
							    if (yobj2.notNull() && yobj2.isString()) {
								// these must be coordinated with strAttrs
								switch (n) {
								case 0:
								    renderer.setFormat(yobj2.stringValue());
								    break;
								case 1:
								    renderer.setNegativePrefix(yobj2.stringValue());
								    break;
								case 2:
								    renderer.setNegativeSuffix(yobj2.stringValue());
								    break;
								case 3:
								    renderer.setPositivePrefix(yobj2.stringValue());
								    break;
								case 4:
								    renderer.setPositiveSuffix(yobj2.stringValue());
								    break;
								case 5:
								    renderer.setLowSubstitute(new String[] { yobj2.stringValue() });
								    break;
								case 6:
								    renderer.setHighSubstitute(new String[] { yobj2.stringValue() });
								    break;
								case 7:
								    renderer.setInputFormat(yobj2.stringValue());
								    break;
								case 8:
								    if (renderer instanceof YoixJTableDateRenderer)
									((YoixJTableDateRenderer)renderer).setTimeZone(yobj2.stringValue());
								    break;
								case 9:
								    if (renderer instanceof YoixJTableDateRenderer)
									((YoixJTableDateRenderer)renderer).setInputTimeZone(yobj2.stringValue());
								    break;
								case 10:
								    renderer.setRendererLocale(yobj2.stringValue());
								    break;
								case 11:
								    renderer.setInputLocale(yobj2.stringValue());
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized string attribute:" + strAttrs[n]});
								}
							    } else if (yobj2.notNull() && yobj2.isArray()) {
								switch (n) {
								case 5:
								    renderer.setLowSubstitute(YoixMake.javaStringArray(yobj2));
								    break;
								case 6:
								    renderer.setHighSubstitute(YoixMake.javaStringArray(yobj2));
								    break;
								default:
								    VM.abort(INTERNALERROR, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized string array attribute:" + strAttrs[n]});
								}
							    } else {
								VM.abort(BADVALUE, name, new String[] {"SET_TYPE_FIELD:" + N_ATTRIBUTES + ": null or non-string value for string attribute:" + strAttrs[n]});
							    }
							}
						    }
						}
					    } else VM.badArgument(name, 3);
					    repaint();
					} else {
					    yobj = YoixObject.newDictionary(boolAttrs.length + intAttrs.length + nbrAttrs.length + strAttrs.length);

					    for (n = 0; n < boolAttrs.length; n++) {
						switch (n) {
						case 0:
						    yobj.putInt(boolAttrs[n], renderer.getDecimalSeparatorAlwaysShown());
						    break;
						case 1:
						    yobj.putInt(boolAttrs[n], renderer.getGroupingUsed());
						    break;
						case 2:
						    yobj.putInt(boolAttrs[n], renderer.getParseIntegerOnly());
						    break;
						case 3:
						    yobj.putInt(boolAttrs[n], renderer.getZeroNotShown());
						    break;
						default:
						    VM.abort(INTERNALERROR, name, new String[] {"GET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized boolean attribute:" + boolAttrs[n]});
						}
					    }
					    for (n = 0; n < intAttrs.length; n++) {
						switch (n) {
						case 0:
						    yobj.putInt(intAttrs[n], renderer.getGroupingSize());
						    break;
						case 1:
						    yobj.putInt(intAttrs[n], renderer.getMaximumFractionDigits());
						    break;
						case 2:
						    yobj.putInt(intAttrs[n], renderer.getMaximumIntegerDigits());
						    break;
						case 3:
						    yobj.putInt(intAttrs[n], renderer.getMinimumFractionDigits());
						    break;
						case 4:
						    yobj.putInt(intAttrs[n], renderer.getMinimumIntegerDigits());
						    break;
						case 5:
						    yobj.putInt(intAttrs[n], renderer.getMultiplier());
						    break;
						default:
						    VM.abort(INTERNALERROR, name, new String[] {"GET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized integer attribute:" + intAttrs[n]});
						}
					    }
					    for (n = 0; n < nbrAttrs.length; n++) {
						switch (n) {
						case 0:
						    yobj.putDouble(nbrAttrs[n], renderer.getOverflow());
						    break;
						case 1:
						    yobj.putDouble(nbrAttrs[n], renderer.getUnderflow());
						    break;
						default:
						    VM.abort(INTERNALERROR, name, new String[] {"GET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized number attribute:" + nbrAttrs[n]});
						}
					    }
					    for (n = 0; n < strAttrs.length; n++) {
						switch (n) {
						case 0:
						    yobj.putString(strAttrs[n], renderer.getFormat());
						    break;
						case 1:
						    yobj.putString(strAttrs[n], renderer.getNegativePrefix());
						    break;
						case 2:
						    yobj.putString(strAttrs[n], renderer.getNegativeSuffix());
						    break;
						case 3:
						    yobj.putString(strAttrs[n], renderer.getPositivePrefix());
						    break;
						case 4:
						    yobj.putString(strAttrs[n], renderer.getPositiveSuffix());
						    break;
						case 5:
						    yobj.putObject(strAttrs[n], yoixStringArray(renderer.getLowSubstitute(), true));
						    break;
						case 6:
						    yobj.putObject(strAttrs[n], yoixStringArray(renderer.getHighSubstitute(), true));
						    break;
						case 7:
						    yobj.putString(strAttrs[n], renderer.getInputFormat());
						    break;
						case 8:
						    if (renderer instanceof YoixJTableDateRenderer)
							yobj.putString(strAttrs[n], ((YoixJTableDateRenderer)renderer).getTimeZone().getID());
						    break;
						case 9:
						    if (renderer instanceof YoixJTableDateRenderer)
							yobj.putString(strAttrs[n], ((YoixJTableDateRenderer)renderer).getInputTimeZone().getID());
						    break;
						case 10:
						    yobj.putString(strAttrs[n], renderer.getRendererLocale());
						    break;
						case 11:
						    yobj.putString(strAttrs[n], renderer.getInputLocale());
						    break;
						default:
						    VM.abort(INTERNALERROR, name, new String[] {"GET_TYPE_FIELD:" + N_ATTRIBUTES + ": unrecognized string attribute:" + strAttrs[n]});
						}
					    }
					    result = yobj;
					}
					break;

					// someday we may want to add:
					// V_ALTBACKGROUND:
					// V_ALTFOREGROUND:
					// V_CELLCOLORS:
					// V_SELECTIONBACKGROUND:
					// V_SELECTIONFOREGROUND:
					// which would require changes to
					// the renderer classes
				    default:
					VM.badArgumentValue(name, 2);
					break;
				    }
				} else VM.badCall(name);
			    } else VM.badArgument(name, 2);
			} else VM.badArgumentValue(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_ROW_VISIBILITY:
		    // usage: int(1/0)[, row_index_or_array[, "viewwise"/"valuewise"]]
		    //        array_of_bool_values[, "viewwise"/"valuewise"]
		    //        "toggle"[, row_index_or_array[, "viewwise"/"valuewise"]]
		    if (arg.length >= 2 && arg.length <=4 ) {
			int[]     vissnap = yjtm.rowinvis;
			int[]     rowsnap = yjtm.row2model;
			int[]     ivalues = null;
			boolean   turnon = false;
			boolean   toggle = false;

			if (rowsnap != null) {
			    rcnt = vissnap == null ? rowsnap.length : vissnap.length;

			    if (arg[1].notNull() && arg[1].isArray()) {
				if (arg.length <=3) {
				    if ((sz = arg[1].sizeof()) > 0) {
					if (arg.length == 3) {
					    if (arg[2].isNull())
						valmode = true;
					    else if (arg[2].isString()) {
						if (arg[2].stringValue().toLowerCase().startsWith("view"))
						    valmode = false;
						else valmode = true;
					    } else VM.badArgument(name, 2);
					} else valmode = true;
					len = arg[1].length();
					count = rowsnap.length;
					if (vissnap == null) 
					    vissnap = yjtm.getReverseRow2ModelMap();
					for (m = 0, n = arg[1].offset(); n < len; m++, n++) {
					    yobj = arg[1].get(n, false);
					    if (yobj.isInteger()) {
						if (m < count) {
						    idx = valmode ? m : rowsnap[m];
						    if (yobj.booleanValue()) {
							if (vissnap[idx] >= rcnt)
							    vissnap[idx] -= rcnt;
							//if (vissnap[idx] < rcnt)
							    //vissnap[idx] += rcnt;
						    } else {
							if (vissnap[idx] < rcnt)
							    vissnap[idx] += rcnt;
							//if (vissnap[idx] >= rcnt)
							    //vissnap[idx] -= rcnt;
						    }
						} else VM.abort(RANGECHECK, "Builtin: " + name + "; Argument: 2; Element: ", m);
					    } else VM.badArgumentValue(name, 1, m);
					}
					yjtm.rowinvis = vissnap;
					yjtm.setRow2ModelVisibility(false);
					yjtm.fireTableChanged(new TableModelEvent(yjtm));
				    } else VM.badArgumentValue(name, 1);
				} else VM.badCall(name);
			    } else {
				if (arg[1].isInteger()) {
				    turnon = arg[1].booleanValue();
				} else if (arg[1].notNull() && arg[1].isString()) {
				    if (arg[1].stringValue().equalsIgnoreCase("toggle"))
					toggle = true;
				    else VM.badArgumentValue(name, 1);
				} else VM.badArgument(name, 1);

				if (arg.length >= 3) {
				    if (arg.length == 4) {
					if (arg[3].isNull())
					    valmode = true;
					else if (arg[3].isString()) {
					    if (arg[3].stringValue().toLowerCase().startsWith("view"))
						valmode = false;
					    else valmode = true;
					} else VM.badArgument(name, 3);
				    } else valmode = true;
				    if (arg[2].isInteger()) {
					if ((ridx = arg[2].intValue()) >= 0 && ridx < rcnt) {
					    ivalues = new int[1];
					    ivalues[0] = valmode ? ridx : yoixConvertRowIndexToModel(ridx);
					} else VM.badArgumentValue(name, 2);
				    } else if (arg[2].notNull() && arg[2].isArray()) {
					if ((sz = arg[2].sizeof()) > 0) {
					    len = arg[2].length();
					    off = arg[2].offset();
					    ivalues = new int[sz];
					    for (m = 0, n = off; n < len; m++, n++) {
						yobj = arg[2].get(n, false);
						if (yobj.isInteger()) {
						    if ((ridx = yobj.intValue()) >= 0 && ridx < rcnt) {
							ivalues[m] = valmode ? ridx : yoixConvertRowIndexToModel(ridx);
						    } else VM.badArgumentValue(name, 2, m);
						} else VM.badArgumentValue(name, 2, m);
					    }
					} else VM.badArgumentValue(name, 2);
				    } else VM.badArgument(name, 2);
				    if (vissnap == null) 
					vissnap = yjtm.getReverseRow2ModelMap();
				    len = ivalues.length;
				    if (toggle) {
					for (m=0; m<len; m++)
					    if (vissnap[ivalues[m]] < rcnt)
						vissnap[ivalues[m]] += rcnt;
					    else vissnap[ivalues[m]] -= rcnt;
				    } else if (turnon) {
					for (m=0; m<len; m++)
					    if (vissnap[ivalues[m]] >= rcnt)
						vissnap[ivalues[m]] -= rcnt;
				    } else {
					for (m=0; m<len; m++)
					    if (vissnap[ivalues[m]] < rcnt)
						vissnap[ivalues[m]] += rcnt;
				    }
				    yjtm.rowinvis = vissnap;
				    yjtm.setRow2ModelVisibility(false);
				    yjtm.fireTableChanged(new TableModelEvent(yjtm));
				} else {
				    if (vissnap == null)
					vissnap = yjtm.getReverseRow2ModelMap();
				    if (toggle) {
					for (n=0; n<rcnt; n++) {
					    if (vissnap[n] < rcnt)
						vissnap[n] += rcnt;
					    else vissnap[n] -= rcnt;
					}
				    } else if (turnon) {
					for (n=0; n<rcnt; n++) {
					    if (vissnap[n] >= rcnt)
						vissnap[n] -= rcnt;
					}
				    } else {
					for (n=0; n<rcnt; n++) {
					    if (vissnap[n] < rcnt)
						vissnap[n] += rcnt;
					}
				    }
				    yjtm.rowinvis = vissnap;
				    yjtm.setRow2ModelVisibility(false);
				    yjtm.fireTableChanged(new TableModelEvent(yjtm));
				}
			    }
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_EDIT_GET_CELL:
		    if (arg.length == 1) {
			if ((foundRow = getEditingRow()) >= 0) {
			    if ((foundColumn = getEditingColumn()) >= 0) {
				ridx = yoixConvertRowIndexToModel(foundRow);
				cidx = yoixConvertColumnIndexToModel(foundColumn);
				result = YoixObject.newDictionary(4);
				result.putInt("valuesRow", ridx);
				result.putInt("valuesColumn", cidx);
				result.putInt("viewRow", foundRow);
				result.putInt("viewColumn", foundColumn);
			    } else result = YoixObject.newDictionary();
			} else result = YoixObject.newDictionary();
		    } else VM.badCall(name);
		    break;

		case YOIX_EDIT_GET_FIELD:
		    if (arg.length == 1) {
			if (isEditing()) {
			    if ((cmpnt = getEditorComponent()) != null) {
				if (cmpnt instanceof JComboBox) {
				    if ((selected = ((JComboBox)cmpnt).getSelectedItem()) != null) {
					if (selected instanceof String)
					    result = YoixObject.newString((String)selected);
					else if (selected instanceof YoixSwingLabelItem)	// probably impossible
					    result = YoixObject.newString(((YoixSwingLabelItem)selected).getValue());
				    }
				} else if (cmpnt instanceof JTextField)
				    result = YoixObject.newString(((JTextField)cmpnt).getText());
				else if (cmpnt instanceof JCheckBox)
				    result = YoixObject.newString(((JCheckBox)cmpnt).isSelected() ? "true" : "false");
			    }
			}
		    } else VM.badCall(name);
		    break;

		case YOIX_EDIT_SET_BACKGROUND:
		    if (arg.length == 2) {
			yobj = arg[1];
			if (yobj.isColor() || yobj.isNull()) {
			    if (isEditing()) {
				if ((cmpnt = getEditorComponent()) != null) {
				    if (yobj.isColor())
					cmpnt.setBackground(YoixMake.javaColor(yobj));
				    else cmpnt.setBackground(getBackground());
				}
			    }
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_EDIT_SET_FIELD:
		    if (arg.length == 2 || arg.length == 3) {
			if (arg[1].isString() || arg[1].isNull() || arg[1].isNumber()) {
			    if (arg.length == 2 || arg[2].isInteger()) {
				yobj = arg[1];
				if (isEditing()) {
				    if ((cmpnt = getEditorComponent()) != null) {
					if (cmpnt instanceof JComboBox) {
					    if (yobj.isNull()) {
						((JComboBox)cmpnt).setSelectedItem(null);
					    } else if (yobj.isString()) {
						((JComboBox)cmpnt).setSelectedItem(yobj.stringValue());
					    } else if (yobj.isNumber()) {
						count = ((JComboBox)cmpnt).getItemCount();
						idx = yobj.intValue();
						if (idx >= 0 && idx < count)
						    ((JComboBox)cmpnt).setSelectedIndex(idx);
						else ((JComboBox)cmpnt).setSelectedItem(null);
					    }
					} else if (cmpnt instanceof JTextField) {
					    text = (yobj.isString() || yobj.isNull()) ? yobj.stringValue() : yobj.toString().trim();
					    if (arg.length == 3) {
						if ((caret = arg[2].intValue()) < 0) {
						    if (caret < -1)
							caret = YoixMisc.pickCaretPosition(((JTextField)cmpnt).getText(), text);
						    else caret = ((JTextField)cmpnt).getCaretPosition();
						}
					    } else caret = -1;
					    ((JTextField)cmpnt).setText(text);
					    if (caret >= 0)
						((JTextField)cmpnt).setCaretPosition(caret);
					} else if (cmpnt instanceof JCheckBox) {
					    if (yobj.isNull()) {
						((JCheckBox)cmpnt).setSelected(false);
					    } else if (yobj.isString()) {
						text = yobj.stringValue().trim();
						((JCheckBox)cmpnt).setSelected("1".equals(text) || "yes".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text));
					    } else if (yobj.isNumber()) {
						((JCheckBox)cmpnt).setSelected(yobj.intValue() != 0);
					    }
					}
				    }
				}
			    } else VM.badArgument(name, 2);
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_EDIT_SET_FOREGROUND:
		    if (arg.length == 2) {
			yobj = arg[1];
			if (yobj.isColor() || yobj.isNull()) {
			    if (isEditing()) {
				if ((cmpnt = getEditorComponent()) != null) {
				    if (yobj.isColor())
					cmpnt.setForeground(YoixMake.javaColor(yobj));
				    else cmpnt.setForeground(getForeground());
				}
			    }
			} else VM.badArgument(name, 1);
		    } else VM.badCall(name);
		    break;

		case YOIX_GET_SELECTED_CELL:
		    if (arg.length == 1) {
			ridx = getSelectionModel().getLeadSelectionIndex();
			cidx = getColumnModel().getSelectionModel().getLeadSelectionIndex();
			if (ridx >= 0 && cidx >= 0) {
			    result = YoixObject.newDictionary(4);
			    result.putInt("viewRow", ridx);
			    result.putInt("viewColumn", cidx);
			    result.putInt("valuesRow", yoixConvertRowIndexToModel(ridx));
			    result.putInt("valuesColumn", yoixConvertColumnIndexToModel(cidx));
			} else result = YoixObject.newDictionary();
		    } else VM.badCall(name);
		    break;

		default:
		    VM.badArgumentValue(name, 0);
		    break;
		}
	    } else VM.badArgument(name, 0);
	} else VM.badCall(name);

	return(result == null ? YoixObject.newNull() : result);
    }


    protected final void
    callActionBuiltin(YoixObject args[]) {

	YoixError  error_point = null;

	if (args != null) {
	    try {
		error_point = VM.pushError();
		builtinAction(N_ACTION, args);
		VM.popError();
	    }
	    catch(YoixError e) {
		if (e != error_point)
		    throw(e);
		else VM.error(error_point);
	    }
	}
    }


    public final void
    columnMarginChanged(ChangeEvent e) {

	YoixObject    obj;
	EventQueue    queue;
	AWTEvent      event;
	JTableHeader  header;
	TableColumn   resize;

	//
	// Don't bother removing editor -- doesn't seem necessary and
	// confuses user as this can be invoked if focus is temporarily
	// shifted during editing (removing editor in that case is not
	// expected by user).
	//

	//
	// Though we setPreferredWidth of a column here, it doesn't seem like it is
	// necessary to worry about adjusting the scrollable viewport size as we do
	// elsewhere since we trigger a resizeAndRepaint which should handle that implicitly
	//

	if ((header = getTableHeader()) != null) {
	    if ((resize = header.getResizingColumn()) != null && autoResizeMode == AUTO_RESIZE_OFF)
		resize.setPreferredWidth(resize.getWidth());

	    if (changeListenerMode) {
		obj = YoixMake.yoixType(T_INVOCATIONEVENT);
		obj.putInt(N_ID, V_INVOCATIONCHANGE);
		
		obj.putString("change", "resize");
		if (resize != null) {
		    obj.putInt("viewColumn", resize.getModelIndex());
		    obj.putInt("valuesColumn", yoixConvertColumnIndexToModel(resize.getModelIndex()));
		    obj.putDouble("width", YoixMakeScreen.yoixDistance(resize.getWidth()));
		} else {
		    obj.putInt("viewColumn", -1);
		    obj.putInt("valuesColumn", -1);
		    obj.putDouble("width", -1);
		}
		event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
		if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
		    queue.postEvent(event);
	    }
	}
	resizeAndRepaint();
    }


    public final int
    yoixConvertColumnIndexToModel(int idx) {

	//
	// Aborting here seems very questionable. Does anyone depend on it?
	// If not then why not just return -1?
	//

	if (idx < 0 || idx >= getColumnCount())
	    VM.abort(RANGECHECK);

	return(convertColumnIndexToModel(idx));
    }


    public final int
    yoixConvertColumnIndexToView(int idx) {

	if (idx < 0 || idx >= getColumnCount())
	    VM.abort(RANGECHECK);

	return(convertColumnIndexToView(idx));
    }


    public final int
    yoixConvertRowIndexToModel(int idx) {

	return(yjtm.getRowIndex(idx));
    }


    public final int
    yoixConvertRowIndexToView(int idx) {

	int  snapshot[] = yjtm.row2model;
	int  len;
	int  n;

	if (snapshot != null) {
	    len = snapshot.length;
	    for (n = 0; n < len; n++) {
		if (snapshot[n] == idx) {
		    idx = n;
		    break;
		}
	    }
	}
	return(idx);
    }


    final YoixJTableCellEditor
    createCellEditor(YoixSwingJTextField tf) {

	return(tf != null ? new YoixJTableStringEditor(tf) : null);
    }


    final YoixJTableCellEditor
    createComboBoxEditor(int type, YoixSwingJComboBox cb) {

	YoixJTableCellEditor  editor = null;

	if (cb != null) {
	    switch (type) {
	    case YOIX_BOOLEAN_TYPE:
		editor = new YoixJTableBooleanEditor(cb);
		break;

	    case YOIX_DATE_TYPE:
		editor = new YoixJTableDateEditor(cb);
		break;

	    case YOIX_DOUBLE_TYPE:
		editor = new YoixJTableDoubleEditor(cb);
		break;

	    case YOIX_HISTOGRAM_TYPE:
		editor = new YoixJTableHistogramEditor(cb);
		break;

	    case YOIX_ICON_TYPE:
		editor = new YoixJTableIconEditor(cb);
		break;

	    case YOIX_INTEGER_TYPE:
		editor = new YoixJTableIntegerEditor(cb);
		break;

	    case YOIX_MONEY_TYPE:
		editor = new YoixJTableMoneyEditor(cb);
		break;

	    case YOIX_OBJECT_TYPE:
		editor = new YoixJTableStringEditor(cb);
		break;

	    case YOIX_PERCENT_TYPE:
		editor = new YoixJTablePercentEditor(cb);
		break;

	    case YOIX_STRING_TYPE:
		editor = new YoixJTableStringEditor(cb);
		break;

	    case YOIX_TEXT_TYPE:
		editor = new YoixJTableTextEditor(cb);
		break;

	    case YOIX_TIMER_TYPE:
		editor = new YoixJTableTimerEditor(cb);
		break;

	    default:
		VM.abort(INTERNALERROR);
		break;
	    }
	}

	return(editor);
    }


    public final void
    createDefaultColumnsFromModel() {

	YoixSwingTableColumn  column;
	TableColumnModel      tcm;
	TableModel            tm;
	int                   typesnap[] = yjtm == null ? null : yjtm.types;
	int                   typelen = typesnap == null ? 0 : typesnap.length;
	int                   ccnt;
	int                   i;

	if ((tm = getModel()) != null) {
	    tcm = getColumnModel();
	    while (tcm.getColumnCount() > 0)
		tcm.removeColumn(tcm.getColumn(0));

	    ccnt = tm.getColumnCount();
	    if (tags == null)
		tags = new Hashtable(ccnt);
	    else tags.clear();

	    for (i = 0; i < ccnt; i++) {
		column = new YoixSwingTableColumn(i, i < typelen ? typesnap[i] : YOIX_STRING_TYPE);
		addColumn(column);
		tags.put(column.getTag(), new Integer(i));
	    }
	}
    }


    protected void
    finalize() {

	data = null;
	parent = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    protected int[]
    matchPattern(String pattern, int[] startIdx, int flags, boolean forward, boolean bycols) {

	YoixRERegexp  re;
	int           matchIdx[] = null;
	int           result[] = null;
	int           i;
	int           j;

	if (pattern != null || matchRE != null) {
	    if (pattern != null) {
		matchRE = null;
		matchMatrix = null;

		if (pattern.length() > 0) {
		    matchRE = new YoixRERegexp(pattern, flags & (SINGLE_BYTE|SHELL_PATTERN|CASE_INSENSITIVE|TEXT_PATTERN));
		    matchMatrix = getStringMatrix(true, bycols);
		    matchByCols = bycols;
		} else pattern = null;
	    }
	    if (matchRE != null && matchMatrix != null) {
		if (startIdx != null && startIdx.length == 2) {
		    if (matchByCols)
			matchIdx = new int[]{ startIdx[1], startIdx[0] };
		    else matchIdx = new int[]{ startIdx[0], startIdx[1] };
		}
		if (matchIdx == null) {
		    if (forward)
			matchIdx = new int[] { 0, 0 };
		    else if (matchMatrix != null)
			matchIdx = new int[]{ matchMatrix.length - 1, matchMatrix[matchMatrix.length-1].length - 1 };
		}
		if (matchRE != null && matchMatrix != null) {
		    if (forward) {
			if (pattern == null)
			    j = matchIdx[1] + 1;
			else j = matchIdx[1];
			if (j < 0)
			    j = 0;
			for (i = matchIdx[0]; i < matchMatrix.length; i++) {
			    if (matchMatrix[i] != null) {
				for (; j < matchMatrix[i].length; j++) {
				    if (matchRE.exec(matchMatrix[i][j], null)) {
					if (matchByCols) {
					    result = new int[] { j, i };
					} else {
					    result = new int[] { i, j };
					}
					matchIdx = new int[] { i, j };
					return(result); // easier to just return from here
				    }
				}
			    }
			    j = 0;
			}
		    } else {
			if (pattern == null)
			    j = matchIdx[1] - 1;
			else j = matchIdx[1];
			for (i = matchIdx[0]; i >= 0; i--) {
			    for (; j >= 0; j--) {
				if (matchRE.exec(matchMatrix[i][j], null)) {
				    if (matchByCols) {
					result = new int[] { j, i };
				    } else {
					result = new int[] { i, j };
				    }
				    matchIdx = new int[] { i, j };
				    return(result); // easier to just return from here
				}
			    }
			    if (i > 0)
				j = matchMatrix[i-1].length - 1;
			}
		    }
		    matchIdx = null;
		    result = new int[] { -1, -1 }; // indicates reached end
		}
	    }
	}

	return(result);
    }

    protected String
    matchString(int row, int col) {

	String result = null;
	int    tmp;

	if (matchMatrix != null) {
	    if (matchByCols) {
		tmp = col;
		col = row;
		row = tmp;
	    }
	    if (row >= 0 && col >= 0) {
		if (row < matchMatrix.length && col < matchMatrix[row].length)
		    result = matchMatrix[row][col];
	    }
	}

	return(result);
    }


    protected void
    findClearSelection() {

	findHighlight(-1, -1, true, false);
    }


    private void
    findHighlight(int row, int column) {

	findHighlight(row, column, true, true);
    }


    private synchronized void
    findHighlight(int row, int column, boolean repaint, boolean find) {

	Rectangle  rect;

	foundRow = row;
	foundColumn = column;
	if (row >= 0 && column >= 0) {
	    rect = getCellRect(row, column, false);
	    if (rect != null && find)
		scrollRectToVisible(rect);
	}
	if (repaint)
	    repaint();
    }


    protected String
    findNextMatch(String string, int pattern, boolean ignorecase, boolean bycols, boolean forward) {
	return(findNextMatch(string, pattern, ignorecase, bycols, forward, true));
    }


    protected String
    findNextMatch(String string, int pattern, boolean ignorecase, boolean bycols, boolean forward, boolean visible) {

	boolean  repeat = false;
	String   result = null;
	int      flags = SINGLE_BYTE;
	int      idx[] = null;
	int      fnd[] = new int[] {foundRow, foundColumn};	// acts as snapshot, too

	if (string == null || string.equals(lastmatch)) {
	    string = null;
	    repeat = true;
	} else lastmatch = string;

	if (pattern < 0) {
	    flags |= SHELL_PATTERN;
	    pattern = -1;
	} else if (pattern == 0)
	    flags |= TEXT_PATTERN;
	else pattern = 1;

	if (pattern != lastpattern) {
	    lastpattern = pattern;
	    if (repeat)
		string = lastmatch;	// force recompile
	}

	if (ignorecase != lastignorecase) {
	    lastignorecase = ignorecase;
	    if (repeat)
		string = lastmatch;	// force recompile
	}

	if (ignorecase)
	    flags |= CASE_INSENSITIVE;

	if (idx == null && fnd[0] >=0 && fnd[1] >= 0)
	    idx = fnd;

	if (bycols != lastbycols) {
	    // want to pick up where we left off, if using same pattern
	    if (string == null) {
		if (idx != null) {
		    if (bycols)
			idx = new int[] { idx[0] + (forward ? 1 : -1), idx[1] };
		    else idx = new int[] { idx[0], idx[1] + (forward ? 1 : -1) };
		}
	    }
	    string = lastmatch; // force recompile
	    lastbycols = bycols;
	}

	idx = matchPattern(string, idx, flags, forward, bycols);

	if (idx != null && idx[0] >= 0) {
	    result = matchString(idx[0], idx[1]);
	    findHighlight(idx[0], idx[1], true, visible);
	} else {
	    findHighlight(-1, -1);
	    lastmatch = null;
	}

	return(result);
    }


    public void
    fireTableDataChanged() {

	yjtm.fireTableDataChanged();
    }


    protected final Color[]
    getCellBackgrounds() {

	return(yjtm.cellBackgrounds);
    }


    protected final Color[]
    getCellForegrounds() {

	return(yjtm.cellForegrounds);
    }


    public Color
    getEditBackground(Color dflt) {

	return(editbackground == null ? dflt : editbackground);
    }


    public Color
    getEditForeground(Color dflt) {

	return(editforeground == null ? dflt : editforeground);
    }


    public Color
    getGridColor() {

	return(getGridColor(getForeground()));
    }


    public Color
    getGridColor(Color dflt) {

	Color  color = gridColor;

	if (color == null)
	    color = dflt;
	return(color);
    }


    final YoixJTableCellRenderer
    getColumnRenderer(int cidx) {
	return(getColumnRenderer(cidx, false, false));
    }


    final synchronized YoixJTableCellRenderer
    getColumnRenderer(int cidx, boolean valmode, boolean for_editing) {
	YoixJTableCellRenderer  renderer = null;
	YoixSwingTableColumn    tblcol;
	TableColumnModel        tcm;

	if ((tcm = getColumnModel()) != null && cidx >= 0 && cidx < tcm.getColumnCount()) {
	    if ((tblcol = (YoixSwingTableColumn)(tcm.getColumn(valmode ? yoixConvertColumnIndexToView(cidx) : cidx))) != null) {
		renderer = getColumnRenderer(tblcol, for_editing);
	    }
	}

	return(renderer);
    }


    final synchronized YoixJTableCellRenderer
    getColumnRenderer(YoixSwingTableColumn tblcol, boolean for_editing) {

	YoixJTableCellRenderer  renderer = null;

	//
	// In this context, for_editing means that the intention is that
	// renderer values will be written/read (set/get) rather than
	// just read (get).
	//

	if ((renderer = (YoixJTableCellRenderer)(tblcol.getCellRenderer())) == null) {
	    renderer = (YoixJTableCellRenderer)(getDefaultRenderer(yjtm.getColumnClass(tblcol.getModelIndex())));
	    if (for_editing) {
		renderer = renderer.makeCopy();
		tblcol.setCellRenderer((TableCellRenderer)renderer);
	    }
	}

	return(renderer);
    }


    final YoixObject
    getColumns() {

	return(yjtm.getColumns());
    }


    final TableCellRenderer
    getDefaultHeaderRenderer() {

	return(defaultHeaderRenderer);
    }


    public final boolean
    getEditListenerMode() {

	return(editListenerMode);
    }


    public final boolean
    getEditKeyListenerMode() {

	return(editKeyListenerMode);
    }


    final YoixObject
    getHeaders() {

	return(yjtm.getHeaders());
    }


    final YoixObject
    getHeaderIcons() {

	return(yjtm.getHeaderIcons());
    }


    final YoixObject
    getInputFilter() {

	return(yjtm.getInputFilter());
    }


    public final Dimension
    getMinimumSize() {

	Dimension  size;

	size = super.getMinimumSize();
	if (isMinimumSizeSet() == false) {
	    size.width = getPreferredSize().width;
	    if (visibleRows > getRowCount())
		size.height = visibleRows*getRowHeight();
	}
	return(size);
    }


    final YoixObject
    getOutputFilter() {

	YoixObject  yobj = YoixObject.newArray(2);

	yobj.putString(0, yjtm.getOutputDelimiter());
	yobj.putString(1, yjtm.getRecordDelimiter());

	return(yobj);
    }


    final int
    getRawRowHeight() {
	return(super.getRowHeight());
    }


    final public int
    getRowHeight() {
	return(super.getRowHeight() + getRowHeightAdjustment());
    }


    final int
    getRowHeightAdjustment() {
	return(rowHeightAdjustment);
    }


    final int[]
    getSortMap() {

	YoixObject obj;
	YoixObject element;
	int        sortmap[] = null;
	int        index;
	int        ccnt;
	int        n;

	//
	// Added on 7/15/10, initially so date strings displayed in a
	// STRING_TYPE column could be sorted using a hidden unixtime
	// column.
	//

	if ((ccnt = getColumnModel().getColumnCount()) > 0) {
	    if ((obj = data.getObject(N_SORTMAP)) != null) {
		if (obj.sizeof() > 0) {
		    sortmap = new int[ccnt];
		    for (n = 0; n < ccnt; n++) {
			sortmap[n] = n;
			if (obj.defined(n)) {
			    if ((element = obj.getObject(n)) != null && element.isNumber()) {
				if ((index = element.intValue()) >= 0 && index < ccnt)
				    sortmap[n] = index;
			    }
			}
		    }
		}
	    }
	}
	return(sortmap);
    }

    protected final int[]
    getStates() {
	return(yjtm.states);
    }


    protected final String[][]
    getStringMatrix(boolean useview, boolean bycols) {

	return(yjtm.getStringMatrix(useview, bycols));
    }


    final YoixObject
    getText() {
	return(builtinAction(N_TEXT, new YoixObject[] {
		    YoixObject.newInt(YOIX_TABLE_JOIN_RAW),
		    YoixObject.newNull(),
		    YoixObject.newInt(0),
		    YoixObject.newInt(1),
		    YoixObject.newInt(0)
		}));
    }


    final YoixObject
    getValues() {
	return(builtinAction(N_VALUES, new YoixObject[] {
		    YoixObject.newInt(YOIX_TABLE_JOIN_RAW),
		    YoixObject.newNull(),
		    YoixObject.newInt(0),
		    YoixObject.newInt(0),
		    YoixObject.newInt(1)
		}));
    }

    final int
    getViewableRowCount() {

	JScrollPane  jsp;
	JViewport    jvp;
	Point        vpt;
	Dimension    ext;
	int          rc;
	int          rw0, rwN;
	int          ht = 0;

	if (parent != null && (jsp = parent.getPeerScroller()) != null) {
	    if ((jvp = jsp.getViewport()) != null) {
		if ((rc = getVisibleRowCount()) > 0) {
		    vpt = jvp.getViewPosition();
		    ext = jvp.getExtentSize();
		    if ((rw0 = rowAtPoint(vpt)) < 0)
			rw0 = 0;
		    if ((rwN = rowAtPoint(new Point(vpt.x, vpt.y + ext.height - 1))) < 0)
			rwN = rc - 1;
		    if ((ht = (1 + rwN - rw0)) > rc)
			ht = rc;
		}
	    }
	}

	return(ht);
    }


    final boolean
    getUseEditHighlight() {
	return(useedithighlight);
    }


    final int
    getVisibleColumnCount() {

	JTableHeader          jth;
	Rectangle             rect;
	YoixSwingTableColumn  tblcol;
	int                   col;
	int                   cols;
	int                   count = 0;

	jth = getTableHeader();
	cols = getColumnCount();
	for (col=0; col<cols; col++) {
	    try {
		tblcol = (YoixSwingTableColumn)(getColumnModel().getColumn(col));
	    }
	    catch(Throwable t) {
		tblcol = null;
	    }
	    if (tblcol != null) {
		if (tblcol.getWidth() > 0)
		    count++;
	    } else if (jth != null && (rect = jth.getHeaderRect(col)) != null && rect.width > 0)
		count++;
	}

	return(count);
    }


    final protected int
    getVisibleRowCount() {
	return(yjtm.getVisibleRowCount());
    }


    public final void
    handleRun(Object args[]) {

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case RUN_SCROLLTOVISIBLE:
		    handleScrollRectToVisible((Rectangle)args[1]);
		    break;

		case RUN_EDITORCARET:
		    ((JTextField)args[1]).setCaretPosition(((JTextField)args[1]).getText().length());
		    break;

		case RUN_AFTERSELECT:
		    parent.call((YoixObject)args[1], (YoixObject[])args[2]);
		    break;

		case RUN_UPDATESELECTIONMARKS:
		    handleUpdateSelectionMarks();
		    break;
	    }
	}
    }


    final YoixObject
    yoixTypes() {

	return(yjtm.getTypes());
    }


    static Boolean[]
    javaBooleanArray(YoixObject ystr, String name1, String name2, String name3) {

	YoixObject  yobj;
	Boolean     booleans[] = null;
	int         len;
	int         sz;
	int         m;
	int         n;

	if (ystr.isNull()) {
	    booleans = null;
	} else if (ystr.isInteger()) {
	    booleans = new Boolean[] {ystr.booleanValue() ? Boolean.TRUE : Boolean.FALSE};
	} else if (ystr.isArray()) {
	    if ((sz = ystr.sizeof()) > 0) {
		len = ystr.length();
		booleans = new Boolean[sz];
		for (m = 0, n = ystr.offset(); n < len; m++, n++) {
		    yobj = ystr.get(n, false);
		    if (yobj.isNull())
			booleans[m] = null;
		    else if (yobj.isInteger()) {
			booleans[m] = yobj.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
		    } else VM.abort(BADVALUE, new String[] {name1 + (name3 == null ? name2 : (name2 + name3)) + "[" + m + "]"});
		}
	    } else booleans = null;
	} else VM.abort(BADVALUE, new String[] {name1 + (name3 == null ? name2 : (name2 + name3))});

	return(booleans);
    }


    static Color[]
    javaColorArray(YoixObject ycol, String name1, String name2, String name3) {

	YoixObject  yobj;
	Color       colors[] = null;
	int         len;
	int         sz;
	int         m;
	int         n;

	if (ycol.isNull()) {
	    colors = null;
	} else if (ycol.isColor()) {
	    colors = new Color[] { YoixMake.javaColor(ycol) };
	} else if (ycol.isArray()) {
	    if ((sz = ycol.sizeof()) > 0) {
		colors = new Color[sz];
		len = ycol.length();
		for (m = 0, n = ycol.offset(); n < len; m++, n++) {
		    yobj = ycol.get(n, false);
		    if (yobj.isNull())
			colors[m] = null;
		    else if (yobj.isColor()) {
			colors[m] = YoixMake.javaColor(yobj);
		    } else VM.abort(BADVALUE, new String[] {name1 + (name3 == null ? name2 : (name2 + name3)) + "[" + m + "]"});
		}
	    } else colors = null;
	} else VM.abort(BADVALUE, new String[] {name1 + (name3 == null ? name2 : (name2 + name3))});

	return(colors);
    }


    static Icon[]
    javaIconArray(YoixObject yicon, String name1, String name2, String name3) {

	YoixObject  yobj;
	Icon        icons[] = null;
	int         len;
	int         sz;
	int         m;
	int         n;

	if (yicon.isNull()) {
	    icons = null;
	} else if (yicon.isImage()) {
	    icons = new Icon[] { YoixMake.javaIcon(yicon) };
	} else if (yicon.isArray()) {
	    if ((sz = yicon.sizeof()) > 0) {
		icons = new Icon[sz];
		len = yicon.length();
		for (m = 0, n = yicon.offset(); n < len; m++, n++) {
		    yobj = yicon.get(n, false);
		    if (yobj.notNull() && yobj.isImage()) {
			icons[m] = YoixMake.javaIcon(yobj);
		    } else VM.abort(BADVALUE, new String[] {name1 + (name3 == null ? name2 : (name2 + name3)) + "[" + m + "]"});
		}
	    } else icons = null;
	} else VM.abort(BADVALUE, new String[] {name1 + (name3 == null ? name2 : (name2 + name3))});

	return(icons);
    }


    static String[]
    javaStringArray(YoixObject ystr, String name1, String name2, String name3) {

	YoixObject  yobj;
	String      strings[] = null;
	int         len;
	int         sz;
	int         m;
	int         n;

	if (ystr.isNull()) {
	    strings = null;
	} else if (ystr.isString()) {
	    strings = new String[] { ystr.stringValue().trim() };
	} else if (ystr.isArray()) {
	    if ((sz = ystr.sizeof()) > 0) {
		len = ystr.length();
		strings = new String[sz];
		for (m = 0, n = ystr.offset(); n < len; m++, n++) {
		    yobj = ystr.get(n, false);
		    if (yobj.isNull())
			strings[m] = null;
		    else if (yobj.isString()) {
			strings[m] = yobj.stringValue().trim();
		    } else VM.abort(BADVALUE, new String[] {name1 + (name3 == null ? name2 : (name2 + name3)) + "[" + m + "]"});
		}
	    } else strings = null;
	} else VM.abort(BADVALUE, new String[] {name1 + (name3 == null ? name2 : (name2 + name3))});

	return(strings);
    }


    public static Object
    pickSortObject(Object obj) {

	return(obj instanceof YoixJTableObject ? ((YoixJTableObject)obj).pickSortObject() : obj);
    }


    final void
    setAfterSelect(YoixObject obj) {

        if (obj != null && obj.notNull()) {
            if (obj.callable(0) || obj.callable(2))
                afterselect = obj;
            else VM.abort(BADVALUE, N_AFTERSELECT);
        } else afterselect = null;
    }


    final void
    setAllowEdit(YoixObject obj) {

        if (obj != null && obj.notNull()) {
            if (obj.callable(3) || obj.callable(2) || obj.callable(1) || obj.callable(0))
                allowedit = obj;
            else VM.abort(BADVALUE, N_ALLOWEDIT);
        } else allowedit = null;
    }


    final void
    setAltColor(YoixObject obj, boolean background) {

	yjtm.setAltColor(obj, background);
    }


    final void
    setAltGridColor(YoixObject obj) {

	yjtm.setAltGridColor(obj);
    }


    final void
    setAltToolTipText(YoixObject obj) {

	yjtm.setHeaderTips(obj);
    }


    final void
    setAltAlignment(YoixObject obj) {

	yjtm.setAltAlignment(obj);
    }


    final void
    setAltFont(YoixObject obj) {

	yjtm.setAltFont(obj);
    }


    final void
    setCellColors(YoixObject obj) {

	yjtm.setCellColors(obj);
    }


    public final void
    setCellEditor(TableCellEditor editor) {

	TransferHandler  handler;
	Component        comp;
	Caret            caret;

	//
	// This method was added (on 10/17/10) to try to compensate for some
	// annoying behavior that left the caret hidden when an editable cell
	// was entered by keyboard traversal. Bit of a kludge, but it really
	// looks like Java is causing the annoying behavior.
	//
	// The TransferHandler kludge was added because cell editors usually
	// are JComponents that don't come from Yoix objects, so there's no
	// way to trigger a call to a Yoix function after data is imported
	// into the editor by DnD or from the Clipboard. Seemed inconsistent
	// because invocationEditKey() is called when the user types in the
	// editor, but unfortunately that doesn't cover all editor changes.
	//

	if (editor instanceof DefaultCellEditor) {
	    comp = ((DefaultCellEditor)editor).getComponent();
	    if (comp instanceof JComponent) {
		if (comp instanceof JTextField) {
		    if ((caret = ((JTextField)comp).getCaret()) != null)
			caret.setVisible(true);
		}
		if (editImportListenerMode) {
		    if ((handler = ((JComponent)comp).getTransferHandler()) != null) {
			if (!(handler instanceof YoixEditorTransferHandler))
			    ((JComponent)comp).setTransferHandler(new YoixEditorTransferHandler((JComponent)comp));
		    }
		}
		//
		// Right now it looks like a JComboBox is the only component
		// that needs to explicitly request the focus. Probably won't
		// hurt if you decide to do it for all components.
		//
		if (comp instanceof JComboBox)
		    comp.requestFocus();
	    }
	}
	super.setCellEditor(editor);
    }


    final void
    setClickCount(int count) {

	TableCellEditor       tblced;
	Class                 typeclass;
	int                   ccnt;
	int                   cidx;

	yjtm.cancelEditing();

	if (count < 1)
	    count = 1;
	for (cidx = 0; cidx < typeValues.length; cidx++) {
	    if ((typeclass = getTypeClass(typeValues[cidx])) != null) {
		tblced = getDefaultEditor(typeclass);
		if (tblced != null && tblced instanceof YoixJTableCellEditor)
		    ((YoixJTableCellEditor)tblced).setClickCountToStart(count);
	    }
	}
	if ((ccnt = getColumnModel().getColumnCount()) > 0) {
	    for (cidx = 0; cidx < ccnt; cidx++) {
		tblced = getCellEditor(0, cidx);
		if (tblced != null && tblced instanceof YoixJTableCellEditor)
		    ((YoixJTableCellEditor)tblced).setClickCountToStart(count);
	    }
	}
    }


    final void
    setColumns(YoixObject obj) {

	yjtm.setColumns(obj);
    }


    public final void
    setCursor(Cursor cursor) {

	JTableHeader  jth;
	JScrollPane   scroller;
	Cursor        headercursor;

	//
	// Logic buried in BasicTableHeaderUI.java thinks it knows how to
	// handle cursors, but unfortunately it doesn't work particularly
	// well when cursors are explicitly set (like we should be doing
	// here). Anyway the BasicTableHeaderUI cursor support is broken
	// so for now we just try not to upset the broken logic. This was
	// broken in Java 1.5.0, but may eventually get fixed. If it does
	// we probably should make this a version dependent kludge.
	// 
	// NOTE - changing the cursor in our peerscroller is a change that
	// that seems to improve things when scripts want to do something
	// like set a wait cursor and the JTable is empty. Was added on
	// 2/11/07.
	// 

	if ((jth = getTableHeader()) != null) {
	    if ((headercursor = jth.getCursor()) != null) {
		super.setCursor(cursor);
		jth.setCursor(headercursor);
	    } else super.setCursor(cursor);
	} else super.setCursor(cursor);

	if ((scroller = parent.getPeerScroller()) != null)	// 2/11/07 change
	    scroller.setCursor(cursor);
    }


    final void
    setDefaultHeaderRenderer() {

	JTableHeader  jth;

	if ((jth = getTableHeader()) != null)
	    defaultHeaderRenderer = jth.getDefaultRenderer();
	else defaultHeaderRenderer = null;
    }


    final void
    setEditInfo(YoixObject obj) {

	YoixObject  yobj;
	YoixObject  yobjs[];
	Boolean     array[];
	Boolean     matrix[][];
	Boolean     info;
	Object      results = null;
	int         maxl;
	int         len;
	int         n;
	int         m;
	int         l;

	yjtm.cancelEditing();

	if (obj != null) {
	    if (obj.isNull()) {
		results = Boolean.FALSE;
	    } else if (obj.isInteger()) {
		results = obj.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
	    } else if (obj.isArray()) {
		yobjs = new YoixObject[obj.sizeof()];
		len = obj.length();
		maxl = 0;
		for (m = 0, n = obj.offset(); n < len; m++, n++) {
		    yobj = obj.get(n, false);
		    if (yobj.isNull()) {
			// nothing
		    } else if (yobj.isInteger()) {
			if (maxl < 1)
			    maxl = 1;
		    } else if (yobj.isArray()) {
			l = yobj.sizeof();
			if (maxl < l)
			    maxl = l;
		    } else VM.abort(BADVALUE, N_EDIT, m);
		    yobjs[m] = yobj;
		}
		if (maxl > 0) {
		    if (maxl == 1) {
			array = new Boolean[m];
			for (n = 0; n < m; n++) {
			    if (yobjs[n].isNull())
				array[n] = null;
			    else {
				if (yobjs[n].isInteger()) {
				    info = yobjs[n].booleanValue() ? Boolean.TRUE : Boolean.FALSE;
				} else { // isArray of length 1
				    yobj = yobjs[n].get(0, false);
				    if (yobj.isNull())
					info = null;
				    else if (yobj.isInteger())
					info = yobj.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
				    else {
					VM.abort(BADVALUE, N_EDIT, n, 0);
					info = null; // for compiler
				    }
				}
				array[n] = info;
			    }
			}
			results = array;
		    } else {
			matrix = new Boolean[len][maxl];
			for (n = 0; n < len; n++) {
			    array = javaBooleanArray(yobjs[n], N_EDIT, "["+n+"]", null);
			    if (array == null || (l = array.length) == 0) {
				for (m = 0; m < maxl; m++)
				    matrix[n][m] = null;
			    } else {
				for (m = 0; m < l; m++) {
				    info = array[m];
				    matrix[n][m] = array[m];
				}
				for (; m < maxl; m++)
				    matrix[n][m] = null;
			    }
			}
			results = matrix;
		    }
		}
	    } else VM.abort(BADVALUE, N_EDIT);
	}

	yjtm.setEditInfo(results);
    }


    public void
    setEditBackground(Color clr) {

	editbackground = clr;
    }


    public void
    setEditForeground(Color clr) {

	editforeground = clr;
    }


    public final void
    setChangeListenerMode(boolean value) {

	changeListenerMode = value;
    }


    public final void
    setEditListenerMode(boolean value) {

	editListenerMode = value;
    }


    public final void
    setEditImportListenerMode(boolean value) {

	editImportListenerMode = value;
    }


    public final void
    setEditKeyListenerMode(boolean value) {

	editKeyListenerMode = value;
    }


    public final void
    setForeground(Color color) {
	super.setForeground(color);
	if (gridColor == null)
	    super.setGridColor(color);
	getTableHeader().repaint();
    }


    public final void
    setGridColor(Color gcolor) {

	gridColor = gcolor;
	if (gcolor == null)
	    gcolor = getForeground();
	super.setGridColor(gcolor);
	getTableHeader().repaint();
    }


    final void
    setHeaders(YoixObject obj) {

	yjtm.setHeaders(obj);
    }


    final void
    setHeaderIcons(YoixObject obj) {

	yjtm.setHeaderIcons(obj);
    }


    final void
    setInputFilter(YoixObject obj) {

	Object  table[];
	String  delimiter;
	int     length;
	int     n;

	table = null;
	delimiter = yjtm.getInputDelimiter();
	if (obj.isNull())
	    delimiter = INPUT_DELIMITER;
	else if (obj.isArray()) {
	    if ((length = obj.length()) > 0 && length%3 == 0) {
		table = new Object[length];
		for (n = 0; n < length; n += 3) {
		    table[n] = obj.get(n, false).stringValue();
		    table[n + 1] = new Integer(obj.get(n + 1, false).intValue());
		    table[n + 2] = obj.get(n + 2, false).stringValue();
		}
	    } else VM.abort(TYPECHECK, N_INPUTFILTER);
	} else if (obj.isString())
	    delimiter = obj.stringValue();
	else VM.abort(TYPECHECK, N_INPUTFILTER);
	yjtm.setInputFilter(table, delimiter);
    }


    public final void
    setIntercellSpacing(Dimension spacing) {

	if (spacing == null)
	    spacing = defaultIntercellSpacing;
	super.setIntercellSpacing(spacing);
    }


    final void
    setOutputFilter(YoixObject obj) {

	YoixObject  yobj;
	int         sz;
	int         of;

	if (obj.isNull()) {
	    yjtm.setOutputFilter(OUTPUT_DELIMITER);
	    yjtm.setRecordFilter(RECORD_DELIMITER);
	} else if (obj.isString()) {
	    yjtm.setOutputFilter(obj.stringValue());
	} else if (obj.isArray()) {
	    if ((sz = obj.sizeof()) == 0) {
		yjtm.setOutputFilter(OUTPUT_DELIMITER);
		yjtm.setRecordFilter(RECORD_DELIMITER);
	    } else if (sz == 1) {
		yobj = obj.get(obj.offset(), false);
		if (yobj.isNull())
		    yjtm.setOutputFilter(OUTPUT_DELIMITER);
		else if (yobj.isString())
		    yjtm.setOutputFilter(yobj.stringValue());
		else VM.abort(TYPECHECK, N_OUTPUTFILTER, 0);
	    } else if (sz == 2) {
		yobj = obj.get((of = obj.offset()), false);
		if (yobj.isNull())
		    yjtm.setOutputFilter(OUTPUT_DELIMITER);
		else if (yobj.isString())
		    yjtm.setOutputFilter(yobj.stringValue());
		else VM.abort(TYPECHECK, N_OUTPUTFILTER, 0);
		yobj = obj.get(of+1, false);
		if (yobj.isNull())
		    yjtm.setRecordFilter(RECORD_DELIMITER);
		else if (yobj.isString())
		    yjtm.setRecordFilter(yobj.stringValue());
		else VM.abort(TYPECHECK, N_OUTPUTFILTER, 1);
	    } else VM.abort(RANGECHECK, N_OUTPUTFILTER);
	} else VM.abort(TYPECHECK, N_OUTPUTFILTER);
    }


    final void
    setQuiet(boolean value) {

	quiet = value;
    }


    final void
    setReorder(boolean value) {

	JTableHeader  jth;

	if ((jth = getTableHeader()) != null)
	    jth.setReorderingAllowed(value);
    }


    final void
    setResize(boolean value) {

	JTableHeader  jth;

	if ((jth = getTableHeader()) != null)
	    jth.setResizingAllowed(value);
    }

    final void
    setRowHeightAdjustment(int adjustment) {
	rowHeightAdjustment = (adjustment < 0) ? ROW_HEIGHT_EDITING_ADJUSTMENT : adjustment;
	recomputeRowHeight();
	if (yjtm != null) {
	    yjtm.fireTableStructureChanged();
	    getTableHeader().resizeAndRepaint();
	}
    }

    public void
    setUseEditHighlight(boolean value) {
	useedithighlight = value;
    }

    public void
    setFont(Font font) {
	if (font != getFont()) { // bonded
	    super.setFont(font);
	    recomputeRowHeight();
	    if (yjtm != null) {
		yjtm.fireTableStructureChanged();
		getTableHeader().resizeAndRepaint();
	    }
	}
    }

    public void
    resortTable() {
	yjtm.resortTable();
    }

    public void
    revalidate() {
	recomputeRowHeight();
	super.revalidate();
    }

    public void
    recomputeRowHeight() {

	YoixSwingTableColumn  tblcol;
	YoixAWTFontMetrics    fm;
	TableColumnModel      tcm;
	Font                  ft;
	int                   n;
	int                   ht;
	int                   maxht = MINIMUM_ROW_HEIGHT;
	int                   kludgecnt;
	int                   kludgecols;
	boolean               kludgeloop;
	TableColumnModel      kludgetcm;

	if ((ft = getFont()) != null) {
	    fm = YoixAWTToolkit.getFontMetrics(ft);
	    ht = fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent() + getRowMargin();
	    if (ht > maxht)
		maxht = ht;
	    if ((tcm = getColumnModel()) != null) {
		//
		// whether we use an Enumeration here or work with
		// getColumnCount() & getColumn() we occasionally
		// get an ArrayIndexOutOfBoundsException apparently
		// because the table column count is fluctuating in
		// another thread so we put in some kludges to work
		// around the problem
		//
		for (n = 0; n < (kludgecols = tcm.getColumnCount()); n++) {
		    kludgecnt = 0;
		    kludgeloop = true;
		    tblcol = null;
		    while (kludgeloop && kludgecnt <= kludgecols) {
			try {
			    tblcol = (YoixSwingTableColumn)(tcm.getColumn(n));
			    kludgeloop = false;
			}
			catch (ArrayIndexOutOfBoundsException e) {
			    try { Thread.sleep(10L); } catch (Exception ee) {}
			    kludgecnt++;
			}
		    }
		    if (tblcol != null) {
			if ((ft = tblcol.getFont(null)) != null) {
			    fm = YoixAWTToolkit.getFontMetrics(ft);
			    ht = fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent() + getRowMargin();
			    if (ht > maxht)
				maxht = ht;
			}
		    }
		}
	    }
	    if (maxht != getRawRowHeight()) {
		setRowHeight(maxht);
		if (visibleRows > 0) {
		    ht = visibleRows;
		    visibleRows = 0;
		    setRows(ht);
		}
	    }
	}
    }


    public void
    scrollRectToVisible(Rectangle rect) {

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_SCROLLTOVISIBLE), rect}
		)
	    );
	} else handleScrollRectToVisible(rect);
    }


    final void
    setRows(int rowcount) {

	JTableHeader  jth;
	StringBuffer  sb;
	Dimension     viewsize;
	int           height;
	int           m;
	int           n;

        if (visibleRows != rowcount) {
            visibleRows = rowcount;

            height = rowcount * getRowHeight();

            if (height >= 0) {
                viewsize = getPreferredScrollableViewportSize();
                if (viewsize.height != height) {
                    viewsize.height = height;
                    setPreferredScrollableViewportSize(viewsize);
		    invalidate();
                }
            }
        }
    }


    public void
    setSelectionMode(int mode) {
        single_selection_mode = (mode == ListSelectionModel.SINGLE_SELECTION);
        super.setSelectionMode(mode);
    }


    protected final void
    setStates(int[] cols) {

	yjtm.states = cols;
    }


    final void
    setTableHeader(boolean activate) {

        JTableHeader  jth;

        if ((jth = getTableHeader()) != null) {
            if (activate) {
                // make headers into buttons
                DefaultTableCellRenderer headrend = new DefaultTableCellRenderer() {
		    public Component getTableCellRendererComponent(JTable table, Object value,
								   boolean isSelected, boolean hasFocus, int row, int column) {

			TableColumnModel      columnModel;
			YoixSwingTableColumn  tblcol;
			YoixJTableModel       yjtm;
			Color                 col;
			int                   state;
			String                tip = null;


			if (table != null) {

			    yjtm = (YoixJTableModel)(((YoixSwingJTable)table).getModel());
			    columnModel = table.getColumnModel();

			    if (column < columnModel.getColumnCount()) {
				tblcol = (YoixSwingTableColumn)(columnModel.getColumn(column));

				setHorizontalAlignment(tblcol.getHeaderHorizontalAlignment(yjtm.getHeaderHorizontalAlignment(SwingConstants.CENTER)));
				setFont(tblcol.getHeaderFont(yjtm.getHeaderFont(table.getFont())));

				state = yjtm.getState(tblcol.getModelIndex());
				if (state > 1 || state < -1) {
				    col = tblcol.getHeaderBackground(state, yjtm.getHeaderBackground(state, table.getBackground()));
				    col = yjtm.adjustColorState(col, state);
				    setBackground(col);
				    setForeground(YoixMisc.getForegroundColor(col, tblcol.getHeaderForeground(state, yjtm.getHeaderForeground(state, table.getForeground()))));
				} else {
				    setBackground(tblcol.getHeaderBackground(state, yjtm.getHeaderBackground(state, table.getBackground())));
				    setForeground(tblcol.getHeaderForeground(state, yjtm.getHeaderForeground(state, table.getForeground())));
				}
				if (tblcol.getLowered()) {
				    setBorder(lowered);
				} else {
				    setBorder(raised);
				}

				setIcon(tblcol.getHeaderIcon(state, yjtm.getHeaderIcon(state, column)));

				tip = tblcol.getHeaderTip(yjtm.getHeaderTip(column, value == null ? null : value.toString()));
			    } else {
				//
				// not sure if we can get into this situation,
				// but if there is no tblcol for this column,
				// then it seems reasonable to do the following:
				//
				state = 0;
				setHorizontalAlignment(yjtm.getHeaderHorizontalAlignment(SwingConstants.CENTER));
				setFont(yjtm.getHeaderFont(table.getFont()));

				setBackground(yjtm.getHeaderBackground(state, table.getBackground()));
				setForeground(yjtm.getHeaderForeground(state, table.getForeground()));
				setBorder(raised);

				setIcon(yjtm.getHeaderIcon(state, column));

				tip = yjtm.getHeaderTip(column, value == null ? null : value.toString());
			    }
			}

			setText((value == null) ? "" : value.toString());

			if (tip != null && tip.length() > 0 && table.getToolTipText() != null) {
			    this.setToolTipText(tip);
			} else this.setToolTipText(null);

			return(this);
		    }

		};
                jth.setDefaultRenderer(headrend);
                jth.addMouseListener(this);
                jth.addMouseMotionListener(this);
            } else {
                DefaultTableCellRenderer headrend = new DefaultTableCellRenderer() {
		    public Component getTableCellRendererComponent(JTable table, Object value,
								   boolean isSelected, boolean hasFocus, int row, int column) {

			TableColumnModel      columnModel;
			YoixSwingTableColumn  tblcol;
			YoixJTableModel       yjtm;
			Color                 col;
			String                tip = null;


			if (table != null) {

			    yjtm = (YoixJTableModel)(((YoixSwingJTable)table).getModel());
			    columnModel = table.getColumnModel();

			    if (column < columnModel.getColumnCount()) {
				tblcol = (YoixSwingTableColumn)(columnModel.getColumn(column));
				setHorizontalAlignment(tblcol.getHeaderHorizontalAlignment(yjtm.getHeaderHorizontalAlignment(SwingConstants.CENTER)));
				setFont(tblcol.getHeaderFont(yjtm.getHeaderFont(table.getFont())));
				setBackground(tblcol.getHeaderBackground(0, yjtm.getHeaderBackground(0, table.getBackground())));
				setForeground(tblcol.getHeaderForeground(0, yjtm.getHeaderForeground(0, table.getForeground())));

				setIcon(tblcol.getHeaderIcon(0, yjtm.getHeaderIcon(0, column)));

				tip = tblcol.getHeaderTip(yjtm.getHeaderTip(column, value == null ? null : value.toString()));
			    } else {
				setHorizontalAlignment(yjtm.getHeaderHorizontalAlignment(SwingConstants.CENTER));
				setFont(yjtm.getHeaderFont(table.getFont()));
				setBackground(yjtm.getHeaderBackground(0, table.getBackground()));
				setForeground(yjtm.getHeaderForeground(0, table.getForeground()));

				setIcon(yjtm.getHeaderIcon(0, column));

				tip = yjtm.getHeaderTip(column, value == null ? null : value.toString());
			    }
			    if (getColumnName(0) == null)
				setBorder(new MatteBorder(0, 0, 1, 1, yjtm.getHeaderGridColor(getGridColor(getForeground()))));
			    else setBorder(new MatteBorder(1, 0, 1, 1, yjtm.getHeaderGridColor(getGridColor(getForeground()))));

			}

			setText((value == null) ? "" : value.toString());

			if (tip != null && tip.length() > 0 && table.getToolTipText() != null) {
			    this.setToolTipText(tip);
			} else this.setToolTipText(null);

			return(this);
		    }

		};
                jth.setDefaultRenderer(headrend);
		if (DISABLE_SCROLLING) {
		    jth.addMouseListener(this);
		    jth.addMouseMotionListener(this);
		} else {
		    jth.removeMouseListener(this);
		    jth.removeMouseMotionListener(this);
		}
            }
        }
    }


    final void
    setToolTipText(YoixObject obj) {

        YoixObject  yobj;
        YoixObject  yobjs[];
        String      array[];
        String      matrix[][];
        Object      tips = null;
        String      tip;
        int         maxl;
        int         len;
        int         n;
        int         m;
        int         l;

        if (obj != null && obj.notNull()) {
            if (obj.isString()) {
                tip = obj.stringValue().trim();
                if (tip.length() > 0)
                    tips = tip;
            } else if (obj.isArray()) {
                yobjs = new YoixObject[obj.sizeof()];
                len = obj.length();
                maxl = 0;
                for (m = 0, n = obj.offset(); n < len; m++, n++) {
                    yobj = obj.get(n, false);
                    if (yobj.isNull()) {
                        // nothing
                    } else if (yobj.isString()) {
                        if (maxl < 1)
                            maxl = 1;
                    } else if (yobj.isArray()) {
                        l = yobj.sizeof();
                        if (maxl < l)
                            maxl = l;
                    } else VM.abort(BADVALUE, N_TOOLTIPTEXT, m);
                    yobjs[m] = yobj;
                }
                if (maxl > 0) {
                    if (maxl == 1) {
                        array = new String[len];
                        for (n = 0; n < len; n++) {
                            if (yobjs[n].isNull())
                                array[n] = null;
                            else {
                                if (yobjs[n].isString()) {
                                    tip = yobjs[n].stringValue().trim();
                                } else { // isArray of length 1
                                    yobj = yobjs[n].get(0, false);
                                    if (yobj.isNull())
                                        tip = null;
                                    else if (yobj.isString())
                                        tip = yobj.stringValue().trim();
                                    else {
                                        VM.abort(BADVALUE, N_TOOLTIPTEXT, n, 0);
                                        tip = null; // for compiler
                                    }
                                }
                                if (tip == null || tip.length() == 0)
                                    array[n] = null;
                                else
                                    array[n] = tip;
                            }
                        }
                        tips = array;
                    } else {
                        matrix = new String[len][maxl];
                        for (n = 0; n < len; n++) {
                            array = javaStringArray(yobjs[n], N_TOOLTIPTEXT, "["+n+"]", null);
                            if (array == null || (l = array.length) == 0) {
                                for (m = 0; m < maxl; m++)
                                    matrix[n][m] = null;
                            } else {
                                for (m = 0; m < l; m++) {
                                    tip = array[m];
                                    if (tip == null || tip.length() == 0)
                                        tip = null;
                                    matrix[n][m] = tip;
                                }
                                for (; m < maxl; m++)
                                    matrix[n][m] = null;
                            }
                        }
                        tips = matrix;
                    }
                }
            } else VM.abort(BADVALUE, N_TOOLTIPTEXT);
        }

        if (tips == null)
            super.setToolTipText(null);
        else super.setToolTipText("");

        yjtm.setTipText(tips);
    }


    final void
    setTypes(YoixObject obj) {

        yjtm.setTypes(obj);
    }


    final void
    setValidator(YoixObject obj) {

        if (obj != null && obj.notNull()) {
            if (obj.callable(5))
                validator = obj;
            else VM.abort(BADVALUE, N_VALIDATOR);
        } else validator = null;
    }


    protected final void
    setValues(YoixObject obj) {

        yjtm.setValues(obj);
    }


    public final void
    sortTable(int columns[]) {
	yjtm.sortTable(columns);
    }


    static YoixObject
    yoixBooleanArray(Boolean bools[]) {

        YoixObject  yobj;
        int         len;
        int         n;

        if (bools == null || (len = bools.length) == 0) {
            yobj = null;
        } else {
            yobj = YoixObject.newArray(len);
            for (n = 0; n < len; n++) {
                if (bools[n] == null)
                    yobj.put(n, YoixObject.newNull(), false);
                else yobj.put(n, YoixObject.newInt(bools[n].booleanValue()), false);
            }
        }
        return(yobj);
    }


    static YoixObject
    yoixColorArray(Color cols[]) {

        YoixObject  yobj;
        int         len;
        int         n;

        if (cols == null || (len = cols.length) == 0) {
            yobj = null;
        } else {
            yobj = YoixObject.newArray(len);
            for (n = 0; n < len; n++)
                yobj.putColor(n, cols[n]);
        }
        return(yobj);
    }


    static YoixObject
    yoixIconArray(Icon icons[]) {

        YoixObject  yobj;
        int         len;
        int         n;

        if (icons == null || (len = icons.length) == 0) {
            yobj = null;
        } else {
            yobj = YoixObject.newArray(len);
            for (n = 0; n < len; n++)
                yobj.putObject(n, YoixMake.yoixIcon(icons[n]));
        }
        return(yobj);
    }


    static YoixObject
    yoixStringArray(String strs[]) {

	return(yoixStringArray(strs, false));
    }


    static YoixObject
    yoixStringArray(String strs[], boolean allowstring) {

        YoixObject  yobj;
        int         len;
        int         n;

        if (strs == null || (len = strs.length) == 0) {
            yobj = null;
	} else if (allowstring && len == 1) {
            yobj = YoixObject.newString(strs[0]);
        } else {
            yobj = YoixObject.newArray(len);
            for (n = 0; n < len; n++)
                yobj.put(n, YoixObject.newString(strs[n]), false);
        }
        return(yobj);
    }

    ///////////////////////////////////
    //
    // methods needed for proper cell selection
    //
    ///////////////////////////////////

    public void
    changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {

        changeSelection(rowIndex, columnIndex, toggle, extend, true);
    }


    public void
    changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend, boolean viewmode) {
        int retval = 0;

        if (yjtm != null) {
	    retval = yjtm.changeSelection(rowIndex, columnIndex, toggle, extend, viewmode);

	    if ((retval&0x01) == 0x01) {
		if (viewmode)
		    super.changeSelection(rowIndex, columnIndex, toggle, extend);
		else
		    super.changeSelection(yoixConvertRowIndexToView(rowIndex), yoixConvertColumnIndexToView(columnIndex), toggle, extend);
	    }

            if ((retval&0x02) == 0x02) {
                repaint();
		updateSelectionMarks();
		afterSelect(rowIndex, columnIndex, viewmode);
            }
        }
    }


    public boolean
    editCellAt(int row, int column) {

	return(allowEdit(row, column, true) && super.editCellAt(row, column));
    }


    public boolean
    editCellAt(int row, int column, EventObject event) {

	return(allowEdit(row, column, true, event) && super.editCellAt(row, column, event));
    }


    public boolean
    isCellEditable(int row, int column) {

	return(allowEdit(row, column, true, null) && super.isCellEditable(row, column));
    }


    public boolean
    isCellSelected(int row, int column) {

        boolean  cellshot[][] = yjtm.cellected;
        boolean  selected = false;
        int      rln;
        int      cln;
        int      n;

	//
	// This method can be called while an event is being processed and
	// in that case we've seen a negative column number. Unfortunately
	// yoixConvertColumnIndexToModel() currently aborts when it gets a
	// negative column number which probably isn't the best thing to do
	// when we're handling a perfectly legitimate event, so instead we
	// check column and if it's negative we just return false.
	//
	// NOTE - to observe the behavior build a small table with headers,
	// select a row, press one of the headers (you probably should hook
	// standard sorting code up to the columns), type something (e.g.,
	// hit return), and if the header had the focus you could end up
	// here while processing a key event and the column you get might
	// be -1.
	//

	if (column >= 0) {
	    row = yoixConvertRowIndexToModel(row);
	    column = yoixConvertColumnIndexToModel(column);
	    if (cellshot != null && (rln = cellshot.length) > 0 && (cln = cellshot[0].length) > 0) {
		if ((row >= 0 && getRowSelectionAllowed()) || (column >= 0 && getColumnSelectionAllowed())) {
		    if (row >= 0 && column >= 0) {
			if (row < rln && column < cln && cellshot[row][column])
			    selected = true;
		    } else if (row >= 0) {
			if (row < rln) {
			    selected = true;
			    for (n = 0; n < cln; n++) {
				if (!cellshot[row][n]) {
				    selected = false;
				    break;
				}
			    }
			}
		    } else if (column >= 0) {
			if (column < cln) {
			    selected = true;
			    for (n = 0; n < rln; n++) {
				if (!cellshot[n][column]) {
				    selected = false;
				    break;
				}
			    }
			}
		    }
		}
	    }
	}

        return(selected);
    }

    public int[]
    getSelectedRows() {
        return(getSelectedRows(true));
    }

    public int[]
    getSelectedRows(boolean viewmode) {
        boolean[][] cellshot = yjtm.cellected;
        int rowsz = 0;
        int rowcnt = 0;
        int m;
        int[] result = null;

        if (getRowSelectionAllowed() && cellshot != null && (rowsz = cellshot.length) > 0) {
            for (m = 0; m < rowsz; m++) {
                if (cellshot[m][0])
                    rowcnt++;
            }
            result = new int[rowcnt];
            rowcnt = 0;
            for (m = 0; m < rowsz; m++) {
                if (cellshot[m][0])
                    result[rowcnt++] = viewmode ? yoixConvertRowIndexToView(m) : m;
            }
        } else result = new int[0];

        return(result);
    }

    public int[]
    getSelectedColumns() {
        return(getSelectedColumns(true));
    }

    public int[]
    getSelectedColumns(boolean viewmode) {
        boolean[][] cellshot = yjtm.cellected;
        int colsz = 0;
        int colcnt = 0;
        int m;
        int[] result = null;

        if (getColumnSelectionAllowed() && cellshot != null && (colsz = cellshot.length) > 0) {
            for (m = 0; m < colsz; m++) {
                if (cellshot[m][0])
                    colcnt++;
            }
            result = new int[colcnt];
            colcnt = 0;
            for (m = 0; m < colsz; m++) {
                if (cellshot[m][0])
                    result[colcnt++] = viewmode ? yoixConvertColumnIndexToView(m) : m;
            }
        } else result = new int[0];

        return(result);
    }

    public void
    addColumnSelectionInterval(int index0, int index1) {
        Rectangle rect = yjtm.setColumnSelectionInterval(index0, index1, 1);
        if (rect != null) {
            index0 = yoixConvertColumnIndexToView(index0);
            index1 = yoixConvertColumnIndexToView(index1);
            super.addColumnSelectionInterval(index0, index1);
            repaint(rect);
        }
    }

    public void
    removeColumnSelectionInterval(int index0, int index1) {
        Rectangle rect = yjtm.setColumnSelectionInterval(index0, index1, -1);
        if (rect != null) {
            index0 = yoixConvertColumnIndexToView(index0);
            index1 = yoixConvertColumnIndexToView(index1);
            super.removeColumnSelectionInterval(index0, index1);
            repaint(rect);
        }
    }

    public void
    toggleColumnSelectionInterval(int index0, int index1) {
        Rectangle rect = yjtm.setColumnSelectionInterval(index0, index1, 0);
        if (rect != null) {
            repaint(rect);
        }
    }

    public void
    addRowSelectionInterval(int index0, int index1) {
        Rectangle rect = yjtm.setRowSelectionInterval(index0, index1, 1);
        if (rect != null) {
            super.addRowSelectionInterval(index0, index1);
            repaint(rect);
        }
    }

    public void
    removeRowSelectionInterval(int index0, int index1) {
        Rectangle rect = yjtm.setRowSelectionInterval(index0, index1, -1);
        if (rect != null) {
            super.removeRowSelectionInterval(index0, index1);
            repaint(rect);
        }
    }

    public void
    toggleRowSelectionInterval(int index0, int index1) {
        Rectangle rect = yjtm.setRowSelectionInterval(index0, index1, 0);
        if (rect != null) {
            repaint(rect);
        }
    }


    public void
    clearSelection() {
        if (yjtm != null) {
	    lastselection = null;
	    findClearSelection();
            yjtm.setAll(false);
            super.clearSelection();
            repaint();
        }
    }


    public void
    selectAll() {
        if (yjtm != null) {
            yjtm.setAll(true);
            super.selectAll();
            repaint();
        }
    }

    public void
    toggleAll() {
        yjtm.toggleAll();
        repaint();
    }

    public void
    valueChanged(ListSelectionEvent e) {
        // over-ride superclass method and do nothing
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    afterSelect(int row, int column, boolean viewmode) {

	YoixObject  funct;
	YoixObject  argv[];

	if ((funct = afterselect) != null) {
	    if (viewmode) {
		row = yoixConvertRowIndexToModel(row);
		if (column >= 0 && column < getColumnCount())
		    column = yoixConvertColumnIndexToModel(column);
		else column = -1;
	    }
	    if (funct.callable(2)) {
		argv = new YoixObject[] {
		    YoixObject.newInt(row),
		    YoixObject.newInt(column)
		};
	    } else argv = new YoixObject[0];
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_AFTERSELECT), funct, argv}
		)
	    );
	}
    }


    private boolean
    allowEdit(int row, int column, boolean viewmode) {

	return(allowEdit(row, column, viewmode, null));
    }


    private boolean
    allowEdit(int row, int column, boolean viewmode, EventObject event) {

	YoixObject  funct;
	YoixObject  argv[];
	YoixObject  obj;
	boolean     result = true;

	if ((funct = allowedit) != null) {
	    if (viewmode) {
		row = yoixConvertRowIndexToModel(row);
		if (column >= 0 && column < getColumnCount())
		    column = yoixConvertColumnIndexToModel(column);
		else column = -1;
	    }
	    if (funct.callable(3)) {
		argv = new YoixObject[] {
		    YoixObject.newInt(row),
		    YoixObject.newInt(column),
		    YoixMakeEvent.yoixEvent(event, parent)
		};
	    } else if (funct.callable(2)) {
		argv = new YoixObject[] {
		    YoixObject.newInt(row),
		    YoixObject.newInt(column)
		};
	    } else if (funct.callable(1))
		argv = new YoixObject[] {YoixMakeEvent.yoixEvent(event, parent)};
	    else argv = new YoixObject[0];
	    if ((obj = parent.call(funct, argv)) != null) {
		if (obj.isNumber())
		    result = obj.booleanValue();
	    }
	}
	
	return(result);
    }


    private Class
    getTypeClass(int type) {

        Class  result;

        switch (type) {
	case YOIX_BOOLEAN_TYPE:
	    result = Boolean.class;
	    break;

	case YOIX_DATE_TYPE:
	    result = Date.class;
	    break;

	case YOIX_DOUBLE_TYPE:
	    result = Double.class;
	    break;

	case YOIX_HISTOGRAM_TYPE:
	    result = YoixJTableHistogram.class;
	    break;

	case YOIX_ICON_TYPE:
	    result = YoixJTableIcon.class;
	    break;

	case YOIX_INTEGER_TYPE:
	    result = Integer.class;
	    break;

	case YOIX_MONEY_TYPE:
	    result = YoixJTableMoney.class;
	    break;

	case YOIX_OBJECT_TYPE:
	    result = YoixJTableObject.class;
	    break;

	case YOIX_PERCENT_TYPE:
	    result = YoixJTablePercent.class;
	    break;

	case YOIX_STRING_TYPE:
	    result = String.class;
	    break;

	case YOIX_TEXT_TYPE:
	    result = YoixJTableText.class;
	    break;

	case YOIX_TIMER_TYPE:
	    result = YoixJTableTimer.class;
	    break;

	default:
	    result = null;
	    break;
        }

        return(result);
    }


    private void
    handleScrollRectToVisible(Rectangle rect) {

	if (disable_scrolling == false)
	    super.scrollRectToVisible(rect);
    }


    private void
    handleUpdateSelectionMarks() {

	String  mark;
	int     oldselections[];
	int     newselections[];
	int     size;
	int     rowcount;
	int     row;
	int     column;
	int     index;
	int     n;

	if (selectionmarks != null) {
	    if ((size = selectionmarks.size()) > 0) {
		if ((rowcount = yjtm.getRawRowCount()) > 0) {
		    oldselections = (int[])selectionmarks.get(0);
		    newselections = getSelectedRows();
		    if (Arrays.equals(oldselections, newselections) == false) {
			for (index = 1; index < size; index += 2) {
			    if ((column = ((Integer)selectionmarks.get(index)).intValue()) >= 0) {
				for (row = 0; row < rowcount; row++)
				    setValueAt(null, row, column);
				if (newselections != null && newselections.length > 0) {
				    if ((mark = (String)selectionmarks.get(index+1)) != null && mark.length() > 0) {
					for (n = 0; n < newselections.length; n++) {
					    if ((row = newselections[n]) >= 0)
						yjtm.setValueAt(mark, row, column);
					}
				    }
				}
			    }
			}
			selectionmarks.set(0, newselections);
		    }
		}
	    }
	}
    }


    private boolean
    isCellValid(int row, int column, int type, Object value, Object oldvalue) {

	YoixObject  obj;
	YoixObject  function;
	boolean     result;

	if ((function = validator) != null) {
	    obj = parent.call(
		function,
		new YoixObject[] {
		    YoixObject.newInt(row),
		    YoixObject.newInt(column),
		    YoixObject.newInt(type),
		    value != null ? yjtm.yoixObjectForTypedValue(type, value) : YoixObject.newNull(),
		    oldvalue != null ? yjtm.yoixObjectForTypedValue(type, oldvalue) : YoixObject.newNull()
		}
	    );
	    result = !(obj != null && obj.notNull() && obj.isInteger() && !obj.booleanValue());
	} else result = true;

	return(result);
    }


    private void
    updateSelectionMarks() {

	if (selectionmarks != null) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_UPDATESELECTIONMARKS)}
		)
	    );
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableFindMarker extends MouseAdapter

    {

	public void
	mousePressed(MouseEvent e) {

	    Point  point;

	    if (e.getModifiersEx() == (YOIX_BUTTON1_DOWN_MASK|YOIX_CTRL_DOWN_MASK|YOIX_SHIFT_DOWN_MASK)) {
		findmode = true;
		point = e.getPoint();
		findColumn = YoixSwingJTable.this.columnAtPoint(point);
		findRow = YoixSwingJTable.this.rowAtPoint(point);
	    } else findmode = false;
	}


	public void
	mouseReleased(MouseEvent e) {

	    Point  point;
	    int    row;
	    int    col;

	    if (findmode) {
		if (e.getModifiersEx() == (YOIX_CTRL_DOWN_MASK|YOIX_SHIFT_DOWN_MASK)) {
		    point = e.getPoint();
		    col = YoixSwingJTable.this.columnAtPoint(point);
		    row = YoixSwingJTable.this.rowAtPoint(point);
		    if (row == findRow && col == findColumn) {
			if (row == foundRow && col == foundColumn)
			    findHighlight(-1, -1);
			else findHighlight(row, col);
		    }
		}
		findRow = -1;
		findColumn = -1;
		findmode = false;
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableModel extends AbstractTableModel
    {

        Border   noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        Color    colormatrix[][][] = null;
        Object   tooltips = null;
        int      row2model[] = new int[0];
	int      rowinvis[] = null;
        boolean  defaultColumnNames = false;
        String   columnNames[] = null;
        int      states[] = new int[0];
        int      types[] = null;
        Object   values[][] = null;
        boolean  cellected[][] = null;
        int      headerAlignment = -1;
        int      headerHorizontalAlignment = -1;
        Font     headerFont = null;
        Color    headerBackgrounds[] = null;
        Color    headerForegrounds[] = null;
        Color    headerGridColor = null;
	Icon     headerIcons[][] = null;
	String   headerTips[] = null;
        boolean  isHeaderBackgroundDark[] = null;
        Color    cellBackgrounds[] = null;
        Color    cellForegrounds[] = null;
        Object   inputfilter[] = null;
        String   input_delimiter = INPUT_DELIMITER;
        String   output_delimiter = OUTPUT_DELIMITER;
        String   record_delimiter = RECORD_DELIMITER;

        Object   editinfo = null;

        YoixSimpleDateFormat  sdf;
        String                timer_format;

        ///////////////////////////////////
        //
        // Constructors
        //
        ///////////////////////////////////

        YoixJTableModel() {

            super();
            sdf = new YoixSimpleDateFormat(UNIX_DATE_FORMAT);
	    sdf.setTimeZone(YoixMiscTime.getDefaultTimeZone());
            sdf.setLenient(true);
            timer_format = TIMER_FORMAT;
        }

        ///////////////////////////////////
        //
        // TableModel Methods
        //
        ///////////////////////////////////

        public final Class
        getColumnClass(int col) {

            Class  result = String.class;
            int    snapshot[] = types;

            if (snapshot != null && col >= 0 && col < snapshot.length)
                if ((result = getTypeClass(snapshot[col])) == null)
                    VM.abort(INTERNALERROR);
            return(result);
        }


        public final int
        getColumnCount() {

            Object            valsnap[][] = values;
            String            colsnap[] = columnNames;
            Icon              icnsnap[][] = headerIcons;
            int               typesnap[] = types;
            int               count = 0;

            if (icnsnap == null && (defaultColumnNames || colsnap == null)) {
                if (valsnap != null && valsnap.length > 0)
                    count = valsnap[0].length;
		else if (typesnap != null)
                    count = typesnap.length;
            } else {
		if (typesnap != null)
		    count = typesnap.length;
		if (colsnap != null)
		    count = colsnap.length;
                if (valsnap != null && valsnap.length > 0 && valsnap[0].length > count)
                    count = valsnap[0].length;
		if (icnsnap != null && icnsnap.length > count && icnsnap.length != 1)
		    count = icnsnap.length;
	    }

            return(count);
        }


        public final String
        getColumnName(int col) {

            String  snapshot[] = columnNames;

            return(defaultColumnNames ? super.getColumnName(col) : (snapshot == null || col < 0 ? null : (col >= snapshot.length || snapshot[col] == null ? super.getColumnName(col) : snapshot[col])));
        }


        public final int
        getRowCount() {

            //return(row2model == null ? 0 : row2model.length);
            return(getVisibleRowCount());
        }

	private int
	getRawRowCount() {
            return(row2model == null ? 0 : row2model.length);
	}

	private int
	getVisibleRowCount() {
	    int vissnap[] = rowinvis;
	    int cnt = 0;
	    int len;

	    if (vissnap != null && (len = vissnap.length) > 0) {
		for (int n = 0; n < len; n++) {
		    if (vissnap[n] < len)
			cnt++;
		}
	    } else cnt = getRawRowCount();

	    return(cnt);
	}

	String[][]
	getStringMatrix(boolean useview, boolean bycols) {

	    String  matrix[][] = null;
	    String  altmatrix[][] = null;
	    String  tmp;
	    int     rows;
	    int     cols;
	    int     colidx[];
	    int     n;
	    int     m;
	    int     v;

	    rows = getRowCount();
	    cols = getColumnCount();
	    if (cols > 0) {
		colidx = new int[cols];
		for (n=0; n<cols; n++)
		    colidx[n] = useview ? n : yoixConvertColumnIndexToModel(n);
		matrix = new String[cols][rows];
		for (n = 0; n < cols; n++) {
		    matrix[n] = (String[])getColumnArray((YoixSwingTableColumn)(getColumnModel().getColumn(colidx[n])), useview, true, true, false, false);
		}

		if (!bycols) {
		    altmatrix = new String[rows][cols];
		    for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
			    altmatrix[j][i] = matrix[i][j];
			}
		    }
		    matrix = altmatrix;
		}
	    }

	    return(matrix);
	}


        public final Object
        getValueAt(int row, int col) {

            return(getRawValueAt(getRowIndex(row), col));
        }


        public final boolean
        isCellEditable(int row, int col) {

            YoixSwingTableColumn  tblcol;
            Boolean               rowedit;
            boolean               ans = false;
            Boolean               array[];
            Boolean               matrix[][];
            Object                snapshot;

            col = yoixConvertColumnIndexToView(col);

            if (row >= 0 && row < getRowCount() && col >= 0 && col < getColumnModel().getColumnCount()) {
                tblcol = (YoixSwingTableColumn)(getColumnModel().getColumn(col));
                snapshot = editinfo;

                if ((rowedit = tblcol.getRowEditableBoolean(yoixConvertRowIndexToModel(row))) != null)
                    ans = rowedit.booleanValue();
                else if (snapshot != null) {
                    if (snapshot instanceof Boolean)
                        ans = ((Boolean)snapshot).booleanValue();
                    else if (snapshot instanceof Boolean[]) {
                        array = (Boolean[])snapshot;
                        if (row < array.length && array[row] != null)
                            ans = array[row].booleanValue();
                    } else if (snapshot instanceof Boolean[][]) {
                        matrix = (Boolean[][])snapshot;
                        if (row < matrix.length && matrix[row] != null && col < matrix[row].length && matrix[row][col] != null)
                            ans = matrix[row][col].booleanValue();
                    } else VM.abort(INTERNALERROR);
                }
            }

            return(ans);
        }


        public final void
        setValueAt(Object value, int row, int col) {

            checkAndSetValueAt(getRowIndex(row), col, value, false);
        }

        ///////////////////////////////////
        //
        // YoixJTableModel Methods
        //
        ///////////////////////////////////

        final Color
        adjustColorState(Color color, int state) {

            int  snapshot[] = states;
            int  levels = 0;
            int  i;

            if (state != 0) {
                for (i = 0; i < snapshot.length; i++)
                    if (snapshot[i] != 0 && Math.abs(snapshot[i]) < Math.abs(state))
                        levels++;

                if (levels > 0) {
                    if (YoixMisc.isBright(color)) {
                        for (i = 0; i < levels; i++)
                            color = color.darker();
                    } else {
                        for (i = 0; i < levels; i++)
                            color = color.brighter();
                    }
                }
            }

            return(color);
        }


        final synchronized void
        cancelEditing() {

            YoixSwingJTable       jtable = YoixSwingJTable.this;
            TableCellEditor       tblced;

	    if ((tblced = jtable.getCellEditor()) != null) {
		if (tblced != null && tblced instanceof YoixJTableCellEditor) {
		    ((YoixJTableCellEditor)tblced).cancelCellEditing();
		    // just in case
		    if (jtable.getCellEditor() != null)
			removeEditor();
		}
	    }
        }


        synchronized void
        cellSelection(int mode, boolean altmode, boolean range, int[] rows, int[] cols) {

            boolean  rowSelection = getRowSelectionAllowed();
            boolean  colSelection = getColumnSelectionAllowed();
            int      m, n, ridx, cidx;
            int      minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = -1, maxy = -1;

            if (cellected != null && cellected.length > 0 && (colSelection || rowSelection)) {
                if (!altmode || YoixSwingJTable.this.single_selection_mode)
                    clearSelection();
                if (range) {
                    if (rows[0] > rows[1]) {
                        m = rows[0];
                        rows[0] = rows[1];
                        rows[1] = m;
                    }
                    if (cols[0] > cols[1]) {
                        m = cols[0];
                        cols[0] = cols[1];
                        cols[1] = m;
                    }
                    rows[0] = rowSelection ? Math.max(0,rows[0]) : 0;
                    cols[0] = colSelection ? Math.max(0,cols[0]) : 0;
                    rows[1] = rowSelection ? Math.min(cellected.length - 1,rows[1]) : cellected.length - 1;
                    cols[1] = colSelection ? Math.min(cellected[0].length - 1,cols[1]) : cellected[0].length - 1;
		    // position anchor and lead
                    YoixSwingJTable.this.changeSelection(rows[0], cols[0], true, true, true);
                    if (mode == 2) {
                        boolean selected = YoixSwingJTable.this.isCellSelected(rows[1], cols[1]);
                        YoixSwingJTable.this.changeSelection(rows[1], cols[1], false, true, true);
                        if (!selected)
                            YoixSwingJTable.this.changeSelection(rows[1], cols[1], true, false, true);
                    } else YoixSwingJTable.this.changeSelection(rows[1], cols[1], false, true, true);

		    // set selection values
                    if (mode == 0) {
                        for (m = rows[0]; m <= rows[1]; m++) {
                            for (n = cols[0]; n <= cols[1]; n++) {
                                ridx = YoixSwingJTable.this.yoixConvertRowIndexToModel(m);
                                cidx = YoixSwingJTable.this.yoixConvertColumnIndexToModel(n);
                                if (ridx < minx)
                                    minx = ridx;
                                if (ridx > maxx)
                                    maxx = ridx;
                                if (cidx < miny)
                                    miny = cidx;
                                if (cidx > maxy)
                                    maxy = cidx;
                                cellected[ridx][cidx] = false;
                            }
                        }
                    } else if (mode == 1) {
                        for (m = rows[0]; m <= rows[1]; m++) {
                            for (n = cols[0]; n <= cols[1]; n++) {
                                ridx = YoixSwingJTable.this.yoixConvertRowIndexToModel(m);
                                cidx = YoixSwingJTable.this.yoixConvertColumnIndexToModel(n);
                                if (ridx < minx)
                                    minx = ridx;
                                if (ridx > maxx)
                                    maxx = ridx;
                                if (cidx < miny)
                                    miny = cidx;
                                if (cidx > maxy)
                                    maxy = cidx;
                                cellected[ridx][cidx] = true;
                            }
                        }
                    } else {
                        for (m = rows[0]; m <= rows[1]; m++) {
                            for (n = cols[0]; n <= cols[1]; n++) {
                                ridx = YoixSwingJTable.this.yoixConvertRowIndexToModel(m);
                                cidx = YoixSwingJTable.this.yoixConvertColumnIndexToModel(n);
                                if (ridx < minx)
                                    minx = ridx;
                                if (ridx > maxx)
                                    maxx = ridx;
                                if (cidx < miny)
                                    miny = cidx;
                                if (cidx > maxy)
                                    maxy = cidx;
                                cellected[ridx][cidx] = !cellected[ridx][cidx];
                            }
                        }
                    }
                    Rectangle firstCell = YoixSwingJTable.this.getCellRect(minx, miny, false);
                    Rectangle lastCell = YoixSwingJTable.this.getCellRect(maxx, maxy, false);
                    repaint(firstCell.union(lastCell));
                } else if (rows != null) {
                    for (m = 0; m < rows.length; m++) {
                        if (YoixSwingJTable.this.isCellSelected(rows[m], cols[m])) {
                            if (mode != 1)
                                YoixSwingJTable.this.changeSelection(rows[m], cols[m], true, false, true);
                        } else {
                            if (mode != 0)
                                YoixSwingJTable.this.changeSelection(rows[m], cols[m], true, false, true);
                        }
                    }
                } else {
                    if (mode == 0) {
                        if (altmode) // avoid duplicate clear
                            YoixSwingJTable.this.clearSelection();
                    } else if (mode == 1)
                        YoixSwingJTable.this.selectAll();
                    else YoixSwingJTable.this.toggleAll();
                }
            }
        }


        final synchronized void
        changeDateFormat(String format) {

            sdf = new YoixSimpleDateFormat(format == null ? UNIX_DATE_FORMAT : format);
	    sdf.setTimeZone(YoixMiscTime.getDefaultTimeZone());
            sdf.setLenient(true);
        }


        final synchronized int
        changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend, boolean viewmode) {
            boolean cleared = false;
            boolean selected = false;
	    boolean single = YoixSwingJTable.this.single_selection_mode;
            boolean rowSelection = getRowSelectionAllowed();
            boolean colSelection = getColumnSelectionAllowed();
            int     retval = 0;
	    int     row0, row1, col0, col1;
	    int     rmdx, cmdx;
	    int     rvdx, cvdx;
            int     m, n, rowsz, colsz;
	    int     mc, nc;

	    if (viewmode) {
		rmdx = yoixConvertRowIndexToModel(rowIndex);
		cmdx = yoixConvertColumnIndexToModel(columnIndex);
		rvdx = rowIndex;
		cvdx = columnIndex;
	    } else {
		rmdx = rowIndex;
		cmdx = columnIndex;
		rvdx = yoixConvertRowIndexToView(rowIndex);
		cvdx = yoixConvertColumnIndexToView(columnIndex);
	    }

            if (!rowSelection && !colSelection)
                clearSelection();
            else if (cellected != null && rmdx >= 0 && cmdx >= 0 && rmdx < (rowsz = cellected.length) && cmdx < (colsz = cellected[0].length)) {

                selected = cellected[rmdx][cmdx];

		if (!selected && single) {
		    // clear
		    lastselection = null;
		    for (m = 0; m < rowsz; m++) {
			for (n = 0; n < colsz; n++) {
			    cellected[m][n] = false;
			}
		    }
		    retval |= 0x02;
		    cleared = true;
		}

		// extension under toggle doesn't register well with expected mouse/key
		// combination, so we force it if last selection was a toggle
		if (extend && lastselection != null && lastselection[5] == 1)
		    toggle = true;

                if (toggle) {
                    if (!extend) {
                        if (rowSelection && colSelection) {
                            cellected[rmdx][cmdx] =
                                !cellected[rmdx][cmdx];
                        } else if (rowSelection) {
                            for (m = 0; m < colsz; m++) {
                                cellected[rmdx][m] =
                                    !cellected[rmdx][m];
                            }
                        } else {
                            for (m = 0; m < rowsz; m++) {
                                cellected[m][cmdx] =
                                    !cellected[m][cmdx];
                            }
                        }
                        retval |= 0x02;
                    } else {
                        if (rowSelection && colSelection) {
			    if (lastselection != null && lastselection[4] == 1 && lastselection[0] < rowsz && lastselection[1] < colsz) {
				if (rvdx < lastselection[2]) {
				    row0 = rvdx;
				    row1 = lastselection[2];
				} else {
				    row1 = rvdx;
				    row0 = lastselection[2];
				}
				if (cvdx < lastselection[3]) {
				    col0 = cvdx;
				    col1 = lastselection[3];
				} else {
				    col1 = cvdx;
				    col0 = lastselection[3];
				}
				for (m = row0; m <= row1; m++) {
				    mc = yoixConvertRowIndexToModel(m);
				    for (n = col0; n <= col1; n++) {
					nc = yoixConvertColumnIndexToModel(n);
					cellected[mc][nc] = !cellected[mc][nc];
				    }
				}
				// reset initial cell back (most likely intent when extending)
				cellected[lastselection[0]][lastselection[1]] =
				    !cellected[lastselection[0]][lastselection[1]];
			    } else cellected[rmdx][cmdx] = !cellected[rmdx][cmdx];
                        } else if (rowSelection) {
			    if (lastselection != null && lastselection[4] == 1 && lastselection[0] < rowsz) {
				if (rvdx < lastselection[2]) {
				    row0 = rvdx;
				    row1 = lastselection[2];
				} else {
				    row1 = rvdx;
				    row0 = lastselection[2];
				}
				for (m = row0; m <= row1; m++) {
				    mc = yoixConvertRowIndexToModel(m);
				    for (n = 0; n < colsz; n++) {
					cellected[mc][n] = !cellected[mc][n];
				    }
				}
			    } else {
				for (n = 0; n < colsz; n++) {
				    cellected[rmdx][n] = !cellected[rmdx][n];
				}
			    }
                        } else {
			    if (lastselection != null && lastselection[4] == 1 && lastselection[1] < colsz) {
				if (cvdx < lastselection[3]) {
				    col0 = cvdx;
				    col1 = lastselection[3];
				} else {
				    col1 = cvdx;
				    col0 = lastselection[3];
				}
				for (n = col0; n <= col1; n++) {
				    nc = yoixConvertColumnIndexToModel(n);
				    for (m = 0; m < rowsz; m++) {
					cellected[m][nc] = !cellected[m][nc];
				    }
				}
			    } else {
				for (m = 0; m < rowsz; m++) {
				    cellected[m][cmdx] = !cellected[m][cmdx];
				}
			    }
                        }
                        retval |= 0x02;
		    }
                } else {
                    if (!cleared && !selected && !extend && !(extend && !single)) {
			if (selected)
			    selected = false;
                        // clear
		 	lastselection = null;
                        for (m = 0; m < rowsz; m++) {
                            for (n = 0; n < colsz; n++) {
                                cellected[m][n] = false;
                            }
                        }
                        retval |= 0x02;
                    }
                    if (!selected) {
                        if (rowSelection && colSelection) {
			    if (extend && lastselection != null && lastselection[4] == 1 && lastselection[0] < rowsz && lastselection[1] < colsz) {
				if (rvdx < lastselection[2]) {
				    row0 = rvdx;
				    row1 = lastselection[2];
				} else {
				    row1 = rvdx;
				    row0 = lastselection[2];
				}
				if (cvdx < lastselection[3]) {
				    col0 = cvdx;
				    col1 = lastselection[3];
				} else {
				    col1 = cvdx;
				    col0 = lastselection[3];
				}
				for (m = row0; m <= row1; m++) {
				    mc = yoixConvertRowIndexToModel(m);
				    for (n = col0; n <= col1; n++) {
					nc = yoixConvertColumnIndexToModel(n);
					cellected[mc][nc] = true;
				    }
				}
			    } else cellected[rmdx][cmdx] = true;
                        } else if (rowSelection) {
			    if (extend && lastselection != null && lastselection[4] == 1 && lastselection[0] < rowsz) {
				if (rvdx < lastselection[2]) {
				    row0 = rvdx;
				    row1 = lastselection[2];
				} else {
				    row1 = rvdx;
				    row0 = lastselection[2];
				}
				for (m = row0; m <= row1; m++) {
				    mc = yoixConvertRowIndexToModel(m);
				    for (n = 0; n < colsz; n++) {
					cellected[mc][n] = true;
				    }
				}
			    } else {
				for (n = 0; n < colsz; n++) {
				    nc = yoixConvertColumnIndexToModel(n);
				    cellected[rmdx][nc] = true;
				}
			    }
                        } else {
			    if (extend && lastselection != null && lastselection[4] == 1 && lastselection[1] < colsz) {
				if (cvdx < lastselection[3]) {
				    col0 = cvdx;
				    col1 = lastselection[3];
				} else {
				    col1 = cvdx;
				    col0 = lastselection[3];
				}
				for (n = col0; n <= col1; n++) {
				    nc = yoixConvertColumnIndexToModel(n);
				    for (m = 0; m < rowsz; m++) {
					cellected[m][nc] = true;
				    }
				}
			    } else {
				for (m = 0; m < rowsz; m++) {
				    cellected[m][cmdx] = true;
				}
			    }
                        }
                        retval |= 0x02;
                    }
                }
                retval |= 0x01;

		if (lastselection == null || !extend)
		    lastselection = new int[] { rmdx, cmdx, rvdx, cvdx, cellected[rmdx][cmdx] ? 1 : 0, toggle ? 1 : 0 };
            }

            return(retval);
        }


        final void
        changeTimerFormat(String format) {

            synchronized(timer_format) {
                timer_format = ((format == null) ? TIMER_FORMAT : format);
            }
        }


        final synchronized void
        checkAndSetValueAt(int row, int col, Object value, boolean programset) {

            YoixJTableCellRenderer  renderer;
            YoixSwingTableColumn    tblcol;
	    EventQueue              queue;
            YoixObject              obj;
            AWTEvent                event;
            Object                  newvalues[][] = null;
            Object                  oldvalue = null;
            int                     rowcount;
            int                     colcount;
            int                     orow = -1;
            int                     ocol;
            int                     nrow;
            int                     ncol;
            int                     type;
            int                     i;
            int                     j;

            rowcount = (values == null ? 0 : values.length);
            colcount = (rowcount == 0 ? 0 : values[0].length);

            if (row >= 0 && row < rowcount && col >= 0 && col < colcount) {
                checkTypeForColumnValue(type = getType(col), value, col, "setting value");
                oldvalue = values[row][col];
		if (oldvalue != value && (value == null || !value.equals(oldvalue))) {
                    values[row][col] = value;
		    //
		    // The following test was added on 10/24/10 because this method
		    // can be called with an empty string (i.e., "") as value and a
		    // null as oldvalue. Not completely convinced by the change, but
		    // the inconsistent calls to an invocationEdit() event handler
		    // triggered because "" and null were considered different by
		    // the old code really seemed wrong!!
		    //
		    if (!(value instanceof String && value.equals("") && oldvalue == null)) {
                	fireTableCellUpdated(row, col);
                	resizeAndRepaint();
                	if (!programset && editListenerMode) {
			    obj = YoixMake.yoixType(T_INVOCATIONEVENT);

			    obj.putInt(N_ID, V_INVOCATIONEDIT);

			    obj.putInt("valuesColumn", col);
			    obj.putInt("viewColumn", ocol = yoixConvertColumnIndexToView(col));

			    obj.putInt("valuesRow", row);
			    obj.putInt("viewRow", yoixConvertRowIndexToView(row));

			    tblcol = (YoixSwingTableColumn)(getColumnModel().getColumn(ocol));
			    renderer = getColumnRenderer(tblcol, false);
			    obj.putString(N_TEXT, renderer.stringValue(value, false));
			    obj.putString("old" + N_TEXT, renderer.stringValue(oldvalue, false));
			    obj.putString(N_VIEW, renderer.stringValue(value, true));
			    obj.putString("old" + N_VIEW, renderer.stringValue(oldvalue, true));
			    obj.put(N_VALUE, yoixObjectForTypedValue(type, value), false);
			    obj.put("old" + N_VALUE, yoixObjectForTypedValue(type, oldvalue), false);

			    event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
			    if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
				queue.postEvent(event);
			}
		    }
                }
            }
        }


        final void
        checkTypeForColumnValue(int type, Object value, int colidx, String errname) {

            boolean  valid = false;
            String   expected;

            switch (type) {
	    case YOIX_BOOLEAN_TYPE:
		valid = value instanceof Boolean;
		expected = "BOOLEAN_TYPE";
		break;

	    case YOIX_DATE_TYPE:
		valid = value instanceof Date;
		expected = "DATE_TYPE";
		break;

	    case YOIX_DOUBLE_TYPE:
		valid = value instanceof Double;
		expected = "DOUBLE_TYPE";
		break;

	    case YOIX_HISTOGRAM_TYPE:
		valid = value instanceof YoixJTableHistogram;
		expected = "HISTOGRAM_TYPE";
		break;

	    case YOIX_ICON_TYPE:
		valid = value instanceof YoixJTableIcon;
		expected = "ICON_TYPE";
		break;

	    case YOIX_INTEGER_TYPE:
		valid = value instanceof Integer;
		expected = "INTEGER_TYPE";
		break;

	    case YOIX_MONEY_TYPE:
		valid = value instanceof YoixJTableMoney;
		expected = "MONEY_TYPE";
		break;

	    case YOIX_OBJECT_TYPE:
		valid = value instanceof YoixJTableObject;
		expected = "OBJECT_TYPE";
		break;

	    case YOIX_PERCENT_TYPE:
		valid = value instanceof YoixJTablePercent;
		expected = "PERCENT_TYPE";
		break;

	    case YOIX_STRING_TYPE:
		valid = value instanceof String;
		expected = "STRING_TYPE";
		break;

	    case YOIX_TEXT_TYPE:
		valid = value instanceof YoixJTableText;
		expected = "TEXT_TYPE";
		break;

	    case YOIX_TIMER_TYPE:
		valid = value instanceof YoixJTableTimer;
		expected = "TIMER_TYPE";
		break;

	    default:
		VM.abort(BADVALUE, errname, new String[] {"unrecognized type (" + type + ") for column values"});
		expected = null; // for compiler
		break;
            }
            // when value is null, we are just checking that type is a
            // valid value (i.e., the default case above)
            if (!valid && value != null) {
                VM.abort(TYPECHECK, errname, new String[] {"Expected type " + expected + " for column values in " + colidx + " (view column " + yoixConvertColumnIndexToView(colidx) + ")"});
	    }
        }


        final Color[][][]
        getColorMatrix() {

            return(colormatrix);
        }


        final YoixObject
        getColumn(YoixSwingTableColumn tblcol) {

            YoixJTableCellRenderer  renderer;
            YoixSwingJTable         jtable = YoixSwingJTable.this;
            StringBuffer            txtbuf;
            YoixObject              ycol = null;
            YoixObject              valrows;
            YoixObject              vurows;
            YoixObject              yobj;
            YoixObject              yobj2;
            boolean                 has_attrs;
            String                  strings[];
            Object                  object;
            Object                  raw_object;
            Color                   colors[];
            Color                   colors2[];
            Color                   colors3[];
            Color                   colors4[];
            Color                   color;
            Font                    font;
            int                     type;
            int                     rcnt;
            int                     idx;
            int                     midx;
            int                     ridx;
            int                     len;
            int                     n;

            idx = tblcol.getModelIndex();

            if (idx >= 0 && idx < getColumnCount()) {
                rcnt = getRowCount();
                ycol = YoixMake.yoixType(T_JTABLECOLUMN);
                midx = jtable.yoixConvertColumnIndexToView(idx);
                if ((renderer = (YoixJTableCellRenderer)(tblcol.getCellRenderer())) == null)
                    renderer = (YoixJTableCellRenderer)(jtable.getDefaultRenderer(jtable.getColumnClass(midx)));
                ycol.putInt(N_ALIGNMENT, tblcol.getAlignment(renderer.getDefaultAlignment()));
                ycol.putInt(N_ALTALIGNMENT, tblcol.getHeaderAlignment(getHeaderAlignment(YOIX_CENTER)));
                if ((font = tblcol.getHeaderFont(getHeaderFont(jtable.getFont()))) != null)
                    ycol.put(N_ALTFONT, YoixMake.yoixFont(font));
                if ((yobj = yoixColorArray(tblcol.getHeaderBackgrounds(headerBackgrounds))) != null)
                    ycol.put(N_ALTBACKGROUND, yobj);
                if ((yobj = yoixColorArray(tblcol.getHeaderForegrounds(headerForegrounds))) != null)
                    ycol.put(N_ALTFOREGROUND, yobj);
                if ((yobj = yoixIconArray(tblcol.getHeaderIcons())) != null)
                    ycol.put(N_HEADERICONS, yobj);
                if ((yobj = YoixObject.newString(tblcol.getHeaderTip(getHeaderTip(idx,null)))) != null)
                    ycol.put(N_ALTTOOLTIPTEXT, yobj);

                has_attrs = false;
                yobj = YoixObject.newDictionary(0,-1);
                // NumberFormat
                if (renderer.getMaximumIntegerDigits() >= 0) {
                    has_attrs = true;
                    yobj.putInt("groupingUsed", renderer.getGroupingUsed());
                    yobj.putInt("maximumFractionDigits", renderer.getMaximumFractionDigits());
                    yobj.putInt("maximumIntegerDigits", renderer.getMaximumIntegerDigits());
                    yobj.putInt("minimumFractionDigits", renderer.getMinimumFractionDigits());
                    yobj.putInt("minimumIntegerDigits", renderer.getMinimumIntegerDigits());
                    yobj.putInt("parseIntegerOnly", renderer.getParseIntegerOnly());

                    yobj.putInt("zeroNotShown", renderer.getZeroNotShown());
                    yobj.putDouble("overflow", renderer.getOverflow());
                    yobj.putDouble("underflow", renderer.getUnderflow());
                    // DecimalFormat
                    if (renderer.getGroupingSize() >= 0) {
                        yobj.putInt("decimalSeparatorAlwaysShown", renderer.getDecimalSeparatorAlwaysShown());
                        yobj.putInt("groupingSize", renderer.getGroupingSize());
                        yobj.putInt("multiplier", renderer.getMultiplier());
                        yobj.putString("negativePrefix", renderer.getNegativePrefix());
                        yobj.putString("negativeSuffix", renderer.getNegativeSuffix());
                        yobj.putString("positivePrefix", renderer.getPositivePrefix());
                        yobj.putString("positiveSuffix", renderer.getPositiveSuffix());
                    }
                }
		if (renderer.getHighSubstitute() != null) {
		    has_attrs = true;
		    yobj.putObject("highSubstitute", yoixStringArray(renderer.getHighSubstitute(), true));
		}
		if (renderer.getLowSubstitute() != null) {
		    has_attrs = true;
		    yobj.putObject("lowSubstitute", yoixStringArray(renderer.getLowSubstitute(), true));
		}
                if (renderer.getFormat() != null) {
                    has_attrs = true;
                    yobj.putString("format", renderer.getFormat());
                }
                if (renderer.getInputFormat() != null) {
                    has_attrs = true;
                    yobj.putString("inputFormat", renderer.getInputFormat());
                }
		if (renderer instanceof YoixJTableDateRenderer && ((YoixJTableDateRenderer)renderer).getTimeZone() != null) {
                    has_attrs = true;
		    yobj.putString("timeZone", ((YoixJTableDateRenderer)renderer).getTimeZone().getID());
                }
		if (renderer instanceof YoixJTableDateRenderer && ((YoixJTableDateRenderer)renderer).getInputTimeZone() != null) {
                    has_attrs = true;
                    yobj.putString("inputTimeZone", ((YoixJTableDateRenderer)renderer).getInputTimeZone().getID());
                }
		if (renderer.getRendererLocale() != null) {
                    has_attrs = true;
		    yobj.putString("locale", renderer.getRendererLocale());
                }
		if (renderer.getInputLocale() != null) {
                    has_attrs = true;
                    yobj.putString("inputLocale", renderer.getInputLocale());
                }
                if (has_attrs) {
                    ycol.put(N_ATTRIBUTES, yobj);
                }

		// should add CELLEDITOR tblcol.getEditor (probably just get whatever valid
		// value was passed to tblcol.setEditor)

                colors = tblcol.getCellBackgrounds(cellBackgrounds);
                colors2 = tblcol.getCellForegrounds(cellForegrounds);
                colors3 = tblcol.getCellSelectionBackgrounds();
                colors4 = tblcol.getCellSelectionForegrounds();
                if (colors != null || colors2 != null || colors3 != null || colors4 != null) {
		    yobj = YoixObject.newArray(2, 4);
		    yobj.putObject(0, yoixColorArray(colors));
		    yobj.putObject(1, yoixColorArray(colors2));
		    if ((colors3 != null && colors3.length > 0) || (colors4 != null && colors4.length > 0)) {
			yobj.putObject(2, yoixColorArray(colors3));
			yobj.putObject(3, yoixColorArray(colors4));
		    }
		    yobj.setGrowable(false);
		    ycol.put(N_CELLCOLORS, yobj);
                }
                if ((yobj = tblcol.yoixEditInfo()) != null)
                    ycol.put(N_EDIT, yobj);
                if ((object = tblcol.getHeaderValue()) != null)
                    ycol.putString(N_HEADER, object.toString());
                if ((color = tblcol.getBackground(getBackground())) != null)
                    ycol.putColor(N_BACKGROUND, color);
                if ((color = tblcol.getForeground(getForeground())) != null)
                    ycol.putColor(N_FOREGROUND, color);
                if ((color = tblcol.getEditBackground(jtable.getEditBackground(null))) != null)
                    ycol.putColor(N_EDITBACKGROUND, color);
                if ((color = tblcol.getEditForeground(jtable.getEditForeground(null))) != null)
                    ycol.putColor(N_EDITFOREGROUND, color);
                if ((color = tblcol.getSelectionBackground(jtable.getSelectionBackground())) != null)
                    ycol.putColor(N_SELECTIONBACKGROUND, color);
                if ((color = tblcol.getSelectionForeground(jtable.getSelectionForeground())) != null)
                    ycol.putColor(N_SELECTIONFOREGROUND, color);
                if ((color = tblcol.getDisabledBackground(null)) != null)
                    ycol.putColor(N_DISABLEDBACKGROUND, color);
                if ((color = tblcol.getDisabledForeground(null)) != null)
                    ycol.putColor(N_DISABLEDFOREGROUND, color);
                VM.pushAccess(LRW_);
                ycol.putInt(N_STATE, getState(idx));
                VM.popAccess();
                ycol.putString(N_TAG, tblcol.getTag());
                if ((yobj = yoixStringArray(tblcol.getToolTips())) != null)
                    ycol.put(N_TOOLTIPTEXT, yobj, false);
                ycol.putInt(N_TYPE, type = getType(idx));
                VM.pushAccess(LRW_);
                ycol.putInt(N_VALUE, idx);
                VM.popAccess();
                if (rcnt > 0) {
                    valrows = YoixObject.newArray(rcnt);
                    vurows = YoixObject.newArray(rcnt);
                    ycol.put(N_VALUES, valrows, false);
                    VM.pushAccess(LRW_);
                    ycol.put(N_VIEWS, vurows, false);
                    VM.popAccess();
                    txtbuf = new StringBuffer();
                    synchronized(txtbuf) {
                        switch (type) {
                        case YOIX_BOOLEAN_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                object = getValueAt(ridx,idx);
                                raw_object = getRawValueAt(ridx,idx);
                                vurows.putInt(ridx, object == null ? 0 : (((Boolean)object).booleanValue() ? 1 : 0));
                                valrows.putInt(ridx, raw_object == null ? 0 : (((Boolean)raw_object).booleanValue() ? 1 : 0));
                                txtbuf.append(renderer.stringValue(raw_object, false));
                                txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_DATE_TYPE:
                            synchronized(this) {
                                for (ridx = 0; ridx < rcnt; ridx++) {
                                    object = getValueAt(ridx,idx);
                                    raw_object = getRawValueAt(ridx,idx);
                                    vurows.putString(ridx, object == null ? "" : sdf.format((Date)object));
                                    valrows.putString(ridx, raw_object == null ? "" : sdf.format((Date)raw_object));
                                    txtbuf.append(renderer.stringValue(raw_object, false));
                                    txtbuf.append(record_delimiter);
                                }
                            }
                            break;
                        case YOIX_DOUBLE_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                object = getValueAt(ridx,idx);
                                raw_object = getRawValueAt(ridx,idx);
                                vurows.putDouble(ridx, object == null ? Double.NaN : ((Double)object).doubleValue());
                                valrows.putDouble(ridx, raw_object == null ? Double.NaN : ((Double)raw_object).doubleValue());
                                txtbuf.append(renderer.stringValue(raw_object, false));
                                txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_HISTOGRAM_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                object = getValueAt(ridx,idx);
                                raw_object = getRawValueAt(ridx,idx);
                                vurows.putDouble(ridx, object == null ? Double.NaN : ((YoixJTableHistogram)object).doubleValue());
                                valrows.putDouble(ridx, raw_object == null ? Double.NaN : ((YoixJTableHistogram)raw_object).doubleValue());
                                txtbuf.append(renderer.stringValue(raw_object, false));
                                txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_ICON_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                object = getValueAt(ridx,idx);
                                raw_object = getRawValueAt(ridx,idx);
                                if (object == null) {
                                    vurows.put(ridx, YoixObject.newImage(), false);
                                } else if (((YoixJTableIcon)(object)).getImage() == null) {
                                    vurows.putString(ridx, ((YoixJTableIcon)object).toString());
                                } else {
                                    vurows.put( ridx, YoixMake.yoixIcon((YoixJTableIcon)object), false);
                                }
                                if (raw_object == null) {
                                    valrows.put(ridx, YoixObject.newImage(), false);
                                } else if (((YoixJTableIcon)(raw_object)).getImage() == null) {
                                    valrows.putString(ridx, ((YoixJTableIcon)raw_object).toString());
                                } else {
                                    valrows.put(ridx, YoixMake.yoixIcon((YoixJTableIcon)raw_object), false);
                                }
                                txtbuf.append(renderer.stringValue(raw_object, false));
                                txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_INTEGER_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                object = getValueAt(ridx,idx);
                                raw_object = getRawValueAt(ridx,idx);
                                vurows.putInt(ridx, object == null ? Integer.MIN_VALUE : ((Integer)object).intValue());
                                valrows.putInt(ridx, raw_object == null ? Integer.MIN_VALUE : ((Integer)raw_object).intValue());
                                txtbuf.append(renderer.stringValue(raw_object, false));
                                txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_MONEY_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                object = getValueAt(ridx,idx);
                                raw_object = getRawValueAt(ridx,idx);
                                vurows.putDouble(ridx, object == null ? Double.NaN : ((YoixJTableMoney)object).doubleValue());
                                valrows.putDouble(ridx, raw_object == null ? Double.NaN : ((YoixJTableMoney)raw_object).doubleValue());
                                txtbuf.append(renderer.stringValue(raw_object, false));
                                txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_OBJECT_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                object = getValueAt(ridx,idx);
                                raw_object = getRawValueAt(ridx,idx);
                                vurows.putString(ridx, object == null ? "" : ((YoixJTableObject)object).stringValue());
                                valrows.putString(ridx, raw_object == null  ? "" : ((YoixJTableObject)raw_object).stringValue());
                                txtbuf.append(renderer.stringValue(raw_object, false));
                                txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_PERCENT_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                object = getValueAt(ridx,idx);
                                raw_object = getRawValueAt(ridx,idx);
                                vurows.putDouble(ridx, object == null ? Double.NaN : ((YoixJTablePercent)object).doubleValue());
                                valrows.putDouble(ridx, raw_object == null ? Double.NaN : ((YoixJTablePercent)raw_object).doubleValue());
                                txtbuf.append(renderer.stringValue(raw_object, false));
                                txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_STRING_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
				object = getValueAt(ridx,idx);
				raw_object = getRawValueAt(ridx,idx);
				vurows.putString(ridx, (String)object);
				valrows.putString(ridx, (String)raw_object);
				txtbuf.append(renderer.stringValue(raw_object, false));
				txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_TEXT_TYPE:
                            for (ridx = 0; ridx < rcnt; ridx++) {
				object = getValueAt(ridx,idx);
				raw_object = getRawValueAt(ridx,idx);
				vurows.putString(ridx, object == null ? "" : ((YoixJTableText)object).toString());
				valrows.putString(ridx, raw_object == null ? "" : ((YoixJTableText)raw_object).toString());
				txtbuf.append(renderer.stringValue(raw_object, false));
				txtbuf.append(record_delimiter);
                            }
                            break;
                        case YOIX_TIMER_TYPE:
                            synchronized(timer_format) {
                                for (ridx = 0; ridx < rcnt; ridx++) {
                                    object = getValueAt(ridx,idx);
                                    raw_object = getRawValueAt(ridx,idx);
                                    vurows.putString(ridx, object == null ? "" : ((YoixJTableTimer)object).toString(timer_format));
                                    valrows.putString(ridx, raw_object == null ? "" : ((YoixJTableTimer)raw_object).toString(timer_format));
                                    txtbuf.append(renderer.stringValue(raw_object, false));
                                    txtbuf.append(record_delimiter);
                                }
                            }
                            break;
                        default:
                            VM.abort(INTERNALERROR);
                        }
                        ycol.putString(N_TEXT, txtbuf.toString());
                    }
                }
                VM.pushAccess(LRW_);
                ycol.putInt(N_VIEW, midx);
                VM.popAccess();
		ycol.putInt(N_VISIBLE, tblcol.isVisible());
                yobj = YoixMakeScreen.yoixDimension(tblcol.getWidth(), 0);
                ycol.put(N_WIDTH, yobj.get(N_WIDTH, false), false);
		ycol.putObject(N_PICKSORTOBJECT, tblcol.getPickSortObject());
		ycol.putObject(N_PICKTABLEOBJECT, tblcol.getPickTableObject());
            }

            return(ycol == null ? YoixObject.newNull(T_JTABLECOLUMN) : ycol);
        }


        final synchronized void
        addColumnText(String text, int index, int type, String errname) {
            StringTokenizer  st = new StringTokenizer(text, record_delimiter);
            Object           object;
            Object[][]       newvals;
            int              rcnt = getRowCount();
            int              tcnt = st.countTokens();
	    int              ccnt;
            int              col;
            int              row;

	    if (rcnt < tcnt) {
		ccnt = getColumnCount();
		if (ccnt > 0) {
		    newvals = new Object[tcnt][ccnt];
		    synchronized(this) {
			for (row = 0; row < rcnt; row++)
			    for (col = 0; col < ccnt; col++)
				newvals[row][col] = values[row][col];
			setValues(newvals);
		    }
		    rcnt = tcnt;
		}
	    }

	    row = 0;
            while (st.hasMoreTokens() && row < rcnt) {
                object = processField(st.nextToken(), type, index, errname);
                values[row++][index] = object;
            }
            for (; row < rcnt; row++)
                values[row][index] = null;
            if (!resortTable())
                fireTableChanged(new TableModelEvent(yjtm));
        }


        final synchronized void
        addColumnValues(YoixObject data, int index, int type, String errname) {
            Object           objs[];
            int              rcnt = getRowCount();
            int              row = 0;

            objs = processColumn(data, index, type, errname);

            if (objs != null) {
                for (row = 0; row < objs.length && row < rcnt; row++)
                    values[row][index] = objs[row];
            }
            for (; row < rcnt; row++)
                values[row][index] = null;
            if (!resortTable())
                fireTableChanged(new TableModelEvent(yjtm));
        }


        final YoixObject
        columnText(int idx, boolean viewmode, boolean formatted) {

            YoixJTableCellRenderer  renderer = getColumnRenderer(idx, true, false);
            StringBuffer             txtbuf = new StringBuffer();
            Object                   object;
            int                      rowsnap[] = row2model;
            int                      rcnt = getRowCount();
            int                      ridx;

            synchronized(txtbuf) {
                switch (renderer.getRendererType()) {
                case YOIX_BOOLEAN_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_DATE_TYPE:
                    synchronized(this) {
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                            txtbuf.append(renderer.stringValue(object, formatted));
                            txtbuf.append(record_delimiter);
                        }
                    }
                    break;
                case YOIX_DOUBLE_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_HISTOGRAM_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_ICON_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_INTEGER_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_MONEY_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_OBJECT_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_PERCENT_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_STRING_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_TEXT_TYPE:
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                        txtbuf.append(renderer.stringValue(object, formatted));
                        txtbuf.append(record_delimiter);
                    }
                    break;
                case YOIX_TIMER_TYPE:
                    synchronized(timer_format) {
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
                            txtbuf.append(renderer.stringValue(object, formatted));
                            txtbuf.append(record_delimiter);
                        }
                    }
                    break;
                default:
                    VM.abort(INTERNALERROR);
                }
            }
            return(YoixObject.newString(txtbuf.toString()));
        }


        final YoixObject
        columnValues(int idx, boolean viewmode) {

            YoixObject  result = null;
            YoixObject  nullobj = YoixObject.newNull();
            Object      object;
            int         rowsnap[] = row2model;
            int         rcnt = getRowCount();
            int         ridx;

	    //
	    // we use nullobj to pre-condition array elements so that they can
	    // accept any object type that the user may want to shove in there
	    // later (it isn't done for our benefit here and this routine would
	    // work fine without it)
	    //

            result = YoixObject.newArray(rcnt);
            switch (yjtm.getType(idx)) {
            case YOIX_BOOLEAN_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    result.putInt(ridx, object == null ? 0 : (((Boolean)object).booleanValue() ? 1 : 0));
                }
                break;
            case YOIX_DATE_TYPE:
                synchronized(this) {
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
			result.putObject(ridx, nullobj);
                        result.putString(ridx, object == null ? "" : sdf.format((Date)object));
                    }
                }
                break;
            case YOIX_DOUBLE_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    result.putDouble(ridx, object == null ? Double.NaN : ((Double)object).doubleValue());
                }
                break;
            case YOIX_HISTOGRAM_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    result.putDouble(ridx, object == null ? Double.NaN : ((YoixJTableHistogram)object).doubleValue());
                }
                break;
            case YOIX_ICON_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    if (object == null) {
                        result.put(ridx, YoixObject.newImage(), false);
                    } else if (((YoixJTableIcon)(object)).getImage() == null) {
                        result.putString(ridx, ((YoixJTableIcon)object).toString());
                    } else {
                        result.put(ridx, YoixMake.yoixIcon((YoixJTableIcon)object), false);
                    }
                }
                break;
            case YOIX_INTEGER_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    result.putInt(ridx, object == null ? Integer.MIN_VALUE : ((Integer)object).intValue());
                }
                break;
            case YOIX_MONEY_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    result.putDouble(ridx, object == null ? Double.NaN : ((YoixJTableMoney)object).doubleValue());
                }
                break;
            case YOIX_OBJECT_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putString(ridx, object == null ? "" : ((YoixJTableObject)object).stringValue());
                }
                break;
            case YOIX_PERCENT_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    result.putDouble(ridx, object == null ? Double.NaN : ((YoixJTablePercent)object).doubleValue());
                }
                break;
            case YOIX_STRING_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    result.putString(ridx, (String)object);
                }
                break;
            case YOIX_TEXT_TYPE:
                for (ridx = 0; ridx < rcnt; ridx++) {
                    object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
		    result.putObject(ridx, nullobj);
                    result.putString(ridx, ((YoixJTableText)object).toString());
                }
                break;
            case YOIX_TIMER_TYPE:
                synchronized(timer_format) {
                    for (ridx = 0; ridx < rcnt; ridx++) {
                        object = getRawValueAt(viewmode ? rowsnap[ridx] : ridx, idx);
			result.putObject(ridx, nullobj);
                        result.putString(ridx, object == null ? null : ((YoixJTableTimer)object).toString(timer_format));
                    }
                }
                break;
            default:
                VM.abort(INTERNALERROR);
            }
            return(result == null ? nullobj : result);
        }


        final synchronized void
        deleteRows(int rw1, int rw2, boolean sync) {

            Object  newvals[][];
            Object  valsnap[][] = values;
	    int     newinvis[] = null;
	    int     vissnap[] = rowinvis;
            int     rowsnap[] = row2model;
            int     rowmap[] = null;
            int     holes[] = null;
            int     rowcount = -1;
            int     oldrc;
            int     oldcc;
            int     slots;
            int     m;
            int     n;
            int     mm;
            int     nn;
            int     ii;

            oldrc = (rowsnap == null ? 0 : rowsnap.length);
            oldcc = getColumnCount();

            if (oldrc > 0) {
                if (rw1 > rw2) {
                    m = rw1;
                    rw1 = rw2;
                    rw2 = m;
                }

                if (rw1 < 0)
                    rw1 = 0;
                if (rw1 >= oldrc)
                    rw1 = oldrc - 1;
                if (rw2 < 0)
                    rw2 = 0;
                if (rw2 >= oldrc)
                    rw2 = oldrc - 1;

                slots = rw2 - rw1 + 1;
                rowcount = oldrc - slots;
                rowmap = new int[rowcount];
                newvals = new Object[rowcount][oldcc];
		if (vissnap != null && rowcount > 0)
		    newinvis = new int[rowcount];
                if (sync) {
                    for (n = 0, nn = 0; nn < rowsnap.length; nn++) {
                        if (nn < rw1 || nn > rw2) {
                            rowmap[n] = n;
			    if (newinvis != null)
				newinvis[n] = vissnap[rowsnap[nn]];
                            newvals[n] = valsnap[rowsnap[nn]];
                            n++;
                        }
                    }
                } else {
                    holes = new int[slots];
                    for (m = 0, mm = 0, n = 0, nn = 0; nn < rowsnap.length; nn++) {
                        if (nn < rw1 || nn > rw2) {
                            rowmap[n++] = rowsnap[nn];
                        } else {
			    holes[m++] = rowsnap[nn];
			    valsnap[rowsnap[nn]] = null;
                        }
                    }
                    for (m = 0, n = 0, nn = 0; nn < rowsnap.length; nn++) {
                        if (valsnap[m] == null) {
                            m++;
                            continue;
                        }
                        for (ii = 0, mm = 0; mm < slots; mm++) {
                            if (rowmap[n] > holes[mm])
                                ii++;
                        }
                        rowmap[n] -= ii;
			if (newinvis != null)
			    newinvis[n] = vissnap[m];
                        newvals[n++] = valsnap[m++];
                    }
                }

		rowinvis = newinvis;
                values = newvals;
                cellected = null;
                if (values.length > 0) {
                    if (values[0].length > 0)
                        cellected = new boolean[values.length][values[0].length];
                }
                row2model = rowmap;
		setRow2ModelVisibility(false);

                if (!resortTable())
                    fireTableDataChanged();
            }
        }


	public void
	fireTableDataChanged() {

	    updateSelectionMarks();
	    super.fireTableDataChanged();
	}


        final synchronized void
        deleteRows(int[] rows, boolean sync) {
	    int[] srows;
	    int vrow;

	    if (rows != null && rows.length > 0) {
 		srows = YoixMiscQsort.qsort(rows, 1);
 		for (int n = rows.length - 1; n >= 0; n--) {
 		    vrow = rows[n];
 		    deleteRows(vrow, vrow, false);
 		}
		if (sync)
		    syncRowViews(false, false);
		//if (!resortTable()) // not really needed in this case
		fireTableDataChanged();
	    }
        }


        final YoixObject
        getColumns() {

            YoixSwingTableColumn  tblcol;
            TableColumnModel      tcm;
            YoixSwingJTable       jtable = YoixSwingJTable.this;
            Enumeration           enm;
            YoixObject            array = null;
            int                   ccnt;
            int                   cidx;

            if ((ccnt = getColumnCount()) > 0) {
                tcm = jtable.getColumnModel();
                // in case thihgs changed while we were enumerating
                // grow the array as needed
                array = YoixObject.newArray(0);
                array.setGrowable(true);
                enm = tcm.getColumns();
                cidx = 0;
                while (enm.hasMoreElements()) {
                    tblcol = (YoixSwingTableColumn)(enm.nextElement());
                    //for (cidx = 0; cidx < ccnt; cidx++)
                    // maybe we should use an enumeration here (getColumns)
                    // may need to use:
                    //   tblcol = tcm.getColumn(yoixConvertColumnIndexToView(cidx));
                    //tblcol = tcm.getColumn(cidx);
                    array.put(yoixConvertColumnIndexToModel(cidx++), getColumn(tblcol), false);
                }
                array.setGrowable(false);
            }
            return(array == null ? YoixObject.newNull(T_ARRAY) : array);
        }


        final Object
        getColumnArray(YoixSwingTableColumn tblcol, boolean view, boolean text, boolean formatted, boolean csv, boolean raw) {

            YoixJTableCellRenderer  renderer;
            YoixSwingJTable         jtable = YoixSwingJTable.this;
            Object[]                string_array = null;
            YoixObject              yoix_array = null;
	    Pattern                 dq;
            Object                  object;
            int                     rcnt;
            int                     idx;
            int                     midx;
            int                     ridx;

            idx = tblcol.getModelIndex();

	    if (csv) {
		text = true;
		dq = Pattern.compile("\"");
	    } else dq = null;

            if (idx >= 0 && idx < getColumnCount()) {
		rcnt = raw ? getRawRowCount() : getRowCount();
                midx = jtable.yoixConvertColumnIndexToView(idx);
                if ((renderer = (YoixJTableCellRenderer)(tblcol.getCellRenderer())) == null)
                    renderer = (YoixJTableCellRenderer)(jtable.getDefaultRenderer(jtable.getColumnClass(midx)));
                if (rcnt > 0) {
                    if (text) {
                        string_array = new String[rcnt];
                        yoix_array = null;
                    } else {
                        yoix_array = YoixObject.newArray(rcnt);
                        string_array = null;
                    }
                    switch (getType(idx)) {
                    case YOIX_BOOLEAN_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
                            } else {
                                yoix_array.putInt(ridx, object == null ? 0 : (((Boolean)object).booleanValue() ? 1 : 0));
                            }
                        }
                        break;
                    case YOIX_DATE_TYPE:
                        synchronized(this) {
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                if (view) {
                                    object = getValueAt(ridx,idx);
                                } else {
                                    object = getRawValueAt(ridx,idx);
                                }
                                if (text) {
                                    string_array[ridx] = renderer.stringValue(object, formatted);
				    if (csv) {
					if (((String)string_array[ridx]).indexOf('"') >= 0)
					    string_array[ridx] = dq.matcher((String)string_array[ridx]).replaceAll("\"\"");
					string_array[ridx] = "\"" + string_array[ridx] + "\"";
				    }
                                } else {
                                    //yoix_array.putString(ridx, object == null ? "" : sdf.format((Date)object));
				    yoix_array.putDouble(ridx, object == null ? Double.NaN : (double)(((Date)object).getTime())/1000.0);
                                }
                            }
                        }
                        break;
                    case YOIX_DOUBLE_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv && formatted)
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
                            } else {
                                yoix_array.putDouble(ridx, object == null ? Double.NaN : ((Double)object).doubleValue());
                            }
                        }
                        break;
                    case YOIX_HISTOGRAM_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv && formatted)
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
                            } else {
                                yoix_array.putDouble(ridx, object == null ? Double.NaN : ((YoixJTableHistogram)object).doubleValue());
                            }
                        }
                        break;
                    case YOIX_ICON_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv) {
				    if (((String)string_array[ridx]).indexOf('"') >= 0)
					string_array[ridx] = dq.matcher((String)string_array[ridx]).replaceAll("\"\"");
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
				}
                            } else {
                                if (object == null) {
                                    yoix_array.put(ridx, YoixObject.newImage(), false);
                                } else if (((YoixJTableIcon)(object)).getImage() == null) {
                                    yoix_array.putString(ridx, ((YoixJTableIcon)object).toString());
                                } else {
                                    yoix_array.put(ridx, YoixMake.yoixIcon((YoixJTableIcon)object), false);
                                }
                            }
                        }
                        break;
                    case YOIX_INTEGER_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv && formatted)
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
                            } else {
                                yoix_array.putInt(ridx, object == null ? Integer.MIN_VALUE : ((Integer)object).intValue());
                            }
                        }
                        break;
                    case YOIX_MONEY_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv && formatted)
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
                            } else {
                                yoix_array.putDouble(ridx, object == null ? Double.NaN : ((YoixJTableMoney)object).doubleValue());
                            }
                        }
                        break;
                    case YOIX_OBJECT_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv) {
				    if (((String)string_array[ridx]).indexOf('"') >= 0)
					string_array[ridx] = dq.matcher((String)string_array[ridx]).replaceAll("\"\"");
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
				}
                            } else {
				yoix_array.putString(ridx, object == null ? "" : ((YoixJTableObject)object).stringValue());
                            }
                        }
                        break;
                    case YOIX_PERCENT_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv && formatted)
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
                            } else {
                                yoix_array.putDouble(ridx, object == null ? Double.NaN : ((YoixJTablePercent)object).doubleValue());
                            }
                        }
                        break;
                    case YOIX_STRING_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv) {
				    if (((String)string_array[ridx]).indexOf('"') >= 0)
					string_array[ridx] = dq.matcher((String)string_array[ridx]).replaceAll("\"\"");
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
				}
                            } else {
                                yoix_array.putString(ridx, (String)object);
                            }
                        }
                        break;
                    case YOIX_TEXT_TYPE:
                        for (ridx = 0; ridx < rcnt; ridx++) {
                            if (view) {
                                object = getValueAt(ridx,idx);
                            } else {
                                object = getRawValueAt(ridx,idx);
                            }
                            if (text) {
                                string_array[ridx] = renderer.stringValue(object, formatted);
				if (csv) {
				    if (((String)string_array[ridx]).indexOf('"') >= 0)
					string_array[ridx] = dq.matcher((String)string_array[ridx]).replaceAll("\"\"");
				    string_array[ridx] = "\"" + string_array[ridx] + "\"";
				}
                            } else {
                                yoix_array.putString(ridx, ((YoixJTableText)object).toString());
                            }
                        }
                        break;
                    case YOIX_TIMER_TYPE:
                        synchronized(timer_format) {
                            for (ridx = 0; ridx < rcnt; ridx++) {
                                if (view) {
                                    object = getValueAt(ridx,idx);
                                } else {
                                    object = getRawValueAt(ridx,idx);
                                }
                                if (text) {
                                    string_array[ridx] = renderer.stringValue(object, formatted);
				    if (csv) {
					if (((String)string_array[ridx]).indexOf('"') >= 0)
					    string_array[ridx] = dq.matcher((String)string_array[ridx]).replaceAll("\"\"");
					string_array[ridx] = "\"" + string_array[ridx] + "\"";
				    }
                                } else {
                                    yoix_array.putString(ridx, object == null ? null : ((YoixJTableTimer)object).toString(timer_format));
                                }
                            }
                        }
                        break;
                    default:
                        VM.abort(INTERNALERROR);
                    }
                }
            }

            return(text ? (Object)(string_array) : (Object)(yoix_array));
        }


        final YoixObject
        getHeaders() {
            YoixObject  retval;
            String      colsnap[] = columnNames;
            int         n;

            if (colsnap == null) {
                retval = YoixObject.newNull();
            } else {
                retval = YoixObject.newArray(colsnap.length);
                for (n = 0; n < colsnap.length; n++) {
                    if (colsnap[n] == null)
                        retval.put(n, YoixObject.newString(), false);
                    else
                        retval.put(n, YoixObject.newString(colsnap[n]), false);
                }
            }

            return(retval);
        }


        final YoixObject
        getHeaderIcons() {
            YoixObject  retval;
            Icon        icnsnap[][] = headerIcons;
            int         n;

            if (icnsnap == null) {
                retval = YoixObject.newNull();
            } else {
                retval = YoixObject.newArray(icnsnap.length);
                for (n = 0; n < icnsnap.length; n++) {
                    if (icnsnap[n] == null)
                        retval.put(n, YoixObject.newNull(), false);
                    else if (icnsnap[n].length == 1)
                        retval.put(n, YoixMake.yoixIcon(icnsnap[n][0]), false);
                    else retval.put(n, yoixIconArray(icnsnap[n]), false);
                }
            }

            return(retval);
        }


        final String
        getInputDelimiter() {

            return(input_delimiter);
        }


        final YoixObject
        getInputFilter() {

            YoixObject  obj;
            int         n;

            if (inputfilter != null) {
                obj = YoixObject.newArray(inputfilter.length);
                for (n = 0; n < inputfilter.length; n += 3) {
                    obj.putString(n, (String)inputfilter[n]);
                    obj.putInt(n + 1, ((Integer)inputfilter[n + 1]).intValue());
                    obj.putString(n + 2, (String)inputfilter[n + 2]);
                }
            } else obj = YoixObject.newString(input_delimiter);

            return(obj);
        }


        final String
        getOutputDelimiter() {

            return(output_delimiter);
        }


        final String
        getRecordDelimiter() {

            return(record_delimiter);
        }


        final String
        getRow(int ridx, boolean viewmode, boolean formatted) {

            StringBuffer  rowbuf = new StringBuffer();
            boolean       initial = true;
            Object        valsnap[][] = values;
            Object        object;
            String        colsnap[] = columnNames;
            String        value;
            int           typsnap[] = types;
            int           rowsnap[] = row2model;
            int           colcount = 0;
            int           collen = 0;
            int           rowcount = 0;
            int           i;
            int           j;
            int           ii;
            int           jj;

            if (defaultColumnNames || colsnap == null) {
                if (valsnap != null && valsnap.length > 0)
                    colcount = valsnap[0].length;
            } else colcount = colsnap.length;

            if (rowsnap != null) {
                if ((rowcount = rowsnap.length) > 0 && valsnap != null)
                    collen = valsnap[0].length;
            }

            synchronized(rowbuf) {
                if ((i = ridx) < rowcount && i >= 0) {
                    for (j = 0; j < colcount; j++) {
                        if (initial)
                            initial = false;
                        else rowbuf.append(output_delimiter);
                        if (j < collen) {
                            if (viewmode) {
                                ii = yoixConvertRowIndexToModel(i);
                                jj = yoixConvertColumnIndexToModel(j);
                            } else {
                                ii = i;
                                jj = j;
                            }

                            object = valsnap[ii][jj];
                            if (formatted)
                                rowbuf.append(getColumnRenderer(j).stringValue(object, false));
                            else {
                                switch (typsnap[jj]) {
                                case YOIX_BOOLEAN_TYPE:
                                    rowbuf.append(object == null ? 0 : (((Boolean)object).booleanValue() ? 1 : 0));
                                    break;
                                case YOIX_DATE_TYPE:
                                    synchronized(this) {
                                        rowbuf.append(object == null ? "" : sdf.format((Date)object));
                                    }
                                    break;
                                case YOIX_DOUBLE_TYPE:
                                    rowbuf.append(object == null ? Double.NaN : ((Double)object).doubleValue());
                                    break;
                                case YOIX_HISTOGRAM_TYPE:
                                    rowbuf.append(object == null ? Double.NaN : ((YoixJTableHistogram)object).doubleValue());
                                    break;
                                case YOIX_ICON_TYPE:
                                    rowbuf.append(object == null ? "" : ((YoixJTableIcon)object).toString());
                                    break;
                                case YOIX_INTEGER_TYPE:
                                    rowbuf.append(object == null ? Integer.MIN_VALUE : ((Integer)object).intValue());
                                    break;
                                case YOIX_MONEY_TYPE:
                                    rowbuf.append(object == null ? Double.NaN : ((YoixJTableMoney)object).doubleValue());
                                    break;
                                case YOIX_OBJECT_TYPE:
				    rowbuf.append(object == null ? "" : ((YoixJTableObject)object).stringValue());
                                    break;
                                case YOIX_PERCENT_TYPE:
                                    rowbuf.append(object == null ? Double.NaN : ((YoixJTablePercent)object).doubleValue());
                                    break;
                                case YOIX_STRING_TYPE:
				    if (object != null)
					rowbuf.append((String)object);
                                    break;
                                case YOIX_TEXT_TYPE:
				    if (object != null)
					rowbuf.append(((YoixJTableText)object).toString());
                                    break;
                                case YOIX_TIMER_TYPE:
                                    synchronized(timer_format) {
                                        rowbuf.append(object == null ? null : ((YoixJTableTimer)object).toString(timer_format));
                                    }
                                    break;
                                default:
                                    VM.abort(INTERNALERROR);
                                }
                            }
                        }
                    }
                }
                value = rowbuf.toString();
            }
            return(value);
        }


        final YoixObject
        getTypes() {

            YoixObject  yobj;
            int         typesnap[] = types;
            int         len;
            int         n;

            if (typesnap == null || typesnap.length == 0)
                yobj = YoixObject.newArray();
            else {
                yobj = YoixObject.newArray(len = typesnap.length);
                for (n = 0; n < len; n++)
                    yobj.put(n, YoixObject.newInt(typesnap[n]), false);
            }

            return(yobj);
        }


        final int
        getState(int idx) {

            int  snapshot[] = states;

            return((snapshot != null && idx >= 0 && idx < snapshot.length) ? snapshot[idx] : 0);
        }


        final String
        getTimerFormat() {

            return(timer_format);
        }


        final int
        getHeaderAlignment(int dflt) {

            return(headerAlignment < 0 ? dflt : headerAlignment);
        }


        final int
        getHeaderHorizontalAlignment(int dflt) {

            return(headerHorizontalAlignment < 0 ? dflt : headerHorizontalAlignment);
        }


        final Color
        getHeaderBackground(int state, Color dflt) {

            Color  bkgd;

            if (headerBackgrounds == null || headerBackgrounds.length == 0)
                bkgd = dflt;
            else if (headerBackgrounds.length < 3)
                bkgd = headerBackgrounds[0];
            else if (state < 0)
                bkgd = headerBackgrounds[1];
            else if (state > 0)
                bkgd = headerBackgrounds[2];
            else bkgd = headerBackgrounds[0];

            return(bkgd);
        }


        final Font
        getHeaderFont(Font dflt) {

            return(headerFont == null ? dflt : headerFont);
        }


        final Color
        getHeaderForeground(int state, Color dflt) {

            Color  frgd;

            if (headerForegrounds == null || headerForegrounds.length == 0)
                frgd = dflt;
            else if (headerForegrounds.length < 3)
                frgd = headerForegrounds[0];
            else if (state < 0)
                frgd = headerForegrounds[1];
            else if (state > 0)
                frgd = headerForegrounds[2];
            else frgd = headerForegrounds[0];

            return(frgd);
        }


        final Color
        getHeaderGridColor(Color dflt) {

            return(headerGridColor == null ? dflt : headerGridColor);
        }


        final Icon
        getHeaderIcon(int state, int col) {

	    Icon[][]  snapshot = headerIcons;
            Icon  hicon;
	    int   n;

	    if (col < 0)
		col = 0;

            if (snapshot == null || snapshot.length == 0)
                hicon = null;
            else if (snapshot.length <= col) {
		n = snapshot.length - 1;
		if (snapshot[n].length < 3)
		    hicon = snapshot[n][0];
		else if (state < 0)
		    hicon = snapshot[n][1];
		else if (state > 0)
		    hicon = snapshot[n][2];
		else hicon = snapshot[n][0];
	    } else {
		if (snapshot[col].length < 3)
		    hicon = snapshot[col][0];
		else if (state < 0)
		    hicon = snapshot[col][1];
		else if (state > 0)
		    hicon = snapshot[col][2];
		else hicon = snapshot[col][0];
	    }

            return(hicon);
        }


//         final Icon[]
//         getHeaderIcons(int col) {

// 	    Icon[][]  snapshot = headerIcons;
//             Icon[]    hicon;

// 	    if (col < 0)
// 		col = 0;

//             if (snapshot == null || snapshot.length == 0)
//                 hicon = null;
//             else if (snapshot.length == 1)
// 		hicon = snapshot[0];
// 	    else if (snapshot.length <= col)
//                 hicon = null;
// 	    else hicon = snapshot[col];

//             return(hicon);
//         }


        final String
        getHeaderTip(int col, String dflt) {

	    String  snapshot[] = headerTips;
	    String  htip;

            if (snapshot == null || snapshot.length == 0)
                htip = dflt;
            else if (snapshot.length <= col)
                htip = snapshot[snapshot.length - 1];
            else htip = snapshot[col];

            return(htip);
        }


        final Object
        getRawValueAt(int row, int col) {

            Object  snapshot[][] = values;
            int     rlen;

            rlen = (snapshot != null) ? snapshot.length : 0;
            return(rlen > 0 && row >= 0 && row < rlen && col >= 0 && col < snapshot[0].length ? snapshot[row][col] : null);
        }


        final int
        getRowIndex(int idx) {

            int  snapshot[] = row2model;

            if (snapshot != null && idx >= 0 && idx < snapshot.length)
		idx = snapshot[idx];
	    else idx = -1;

	    return(idx);
	}


        final String
        getTipText(int row, int column) {

            Object  snapshot = tooltips;
            String  tip = null;
            String  array[];
            String  matrix[][];

            if (snapshot != null) {
                if (snapshot instanceof String)
                    tip = (String)snapshot;
                else if (snapshot instanceof String[]) {
                    array = (String[])snapshot;
                    if (row >= 0 && row < array.length)
                        tip = array[row];
                } else if (snapshot instanceof String[][]) {
                    matrix = (String[][])snapshot;
                    if (row >= 0 && row < matrix.length && column >= 0 && column < matrix[row].length)
                        tip = matrix[row][column];
                } else VM.abort(INTERNALERROR);
            }
            return(tip);
        }


        final int
        getType(int idx) {

            int  snapshot[] = types;

            return(snapshot != null && idx >= 0 && idx < snapshot.length ? snapshot[idx] : YOIX_STRING_TYPE);
        }


        final boolean
        isSorted() {

            boolean  sorted = false;
            int      snapshot[] = states;
            int      i;

            for (i = 0; i < snapshot.length; i++) {
                if (snapshot[i] != 0) {
                    sorted = true;
                    break;
                }
            }
            return(sorted);
        }


        final synchronized void
        placeValues(int mode, Object placevals[][], int rins, int ridx[], boolean sync) {

            YoixSwingTableColumn  tblcol;
            TableColumnModel      tcm;
            String                tag;
            Object                newvals[][];
            Object                valsnap[][] = values;
            String                colsnap[] = columnNames;
	    int                   vissnap[] = rowinvis;
	    int                   newinvis[] = null;
            int                   typesnap[] = types;
            int                   typeinfo[] = null;
            int                   rowsnap[] = row2model;
            int                   rowmap[] = null;
            int                   holes[] = null;
            int                   typelen;
            int                   rowcount = -1;
            int                   colcount = -1;
            int                   oldrc;
            int                   oldcc;
            int                   valcc = 0;
            int                   m;
            int                   n;
            int                   nn;

	    if (rowsnap == null)
		rowsnap = new int[0];

            oldrc = rowsnap.length;
            oldcc = getColumnCount();

            if (placevals != null && (rowcount = placevals.length) > 0) {
                rowcount = placevals.length;
                valcc = placevals[0].length;

                if (defaultColumnNames || columnNames == null) {
                    if (valsnap == null || valsnap[0] == null)
                        colcount = valcc;
                    else colcount = valsnap[0].length;
                } else colcount = oldcc;
            } else {
                rowcount = 0;
                colcount = oldcc;
            }


            if (mode == 1) {
                nn = 0;
                if (ridx != null) {
                    holes = new int[rowsnap.length];
                    for (n=0; n<holes.length; n++)
                        holes[n] = 0;
                    for (n=0; n<ridx.length; n++) {
                        if (ridx[n] >= 0 && ridx[n] < oldrc) {
                            nn++;
                            holes[rowsnap[ridx[n]]] = -1;
                        }
                    }
                }
                if (nn == 0)
                    mode = 2;
                else oldrc -= nn;
            }

            if (rowcount > 0 || mode == 1) {
                rowmap = new int[rowcount + oldrc];
		if (vissnap != null && rowmap.length > 0)
		    newinvis = new int[rowcount + oldrc];

                switch (mode) {
                case 2: // add
                    rins++;
                    // fall through to...
                case 0: // insert
                    if (rins < 0)
                        rins = 0;
                    else if (rins >= oldrc)
                        rins = oldrc;
                    n = 0;
                    nn = 0;
                    while (n < rowmap.length || nn < rowsnap.length) {
                        if (n == rins) {
                            for (m = 0; m < rowcount; m++)
                                rowmap[n++] = oldrc + m;
                        }
                        if (nn < rowsnap.length)
                            rowmap[n++] = rowsnap[nn++];
                    }
                    newvals = new Object[rowmap.length][colcount];
                    if (oldrc > 0) {
                        System.arraycopy(valsnap, 0, newvals, 0, oldrc);
			if (newinvis != null) {
			    System.arraycopy(vissnap, 0, newinvis, 0, oldrc);
			    //
			    // Have to adjust values assigned to invisible rows
			    // because we're adding one or more rows. Added rows
			    // are initialized below by setRow2ModelVisibility()
			    // since we now call it with a true argument.
			    // 
			    for (n = 0; n < oldrc; n++) {
				if (newinvis[n] >= oldrc)
				    newinvis[n] += rowcount;
			    }
			}
		    }
                    if (rowcount > 0)
                        System.arraycopy(placevals, 0, newvals, oldrc, rowcount);
                    break;
                case 1: // replace
                    newvals = new Object[rowmap.length][colcount];
                    for (n = 0, nn = 0; n < rowsnap.length; n++) {
                        if (holes[rowsnap[n]] == 0) {
			    if (newinvis != null)
				newinvis[nn] = vissnap[rowsnap[n]];
                            newvals[nn++] = valsnap[rowsnap[n]];
			}
                    }
                    for (n = 0, nn = 0; n < rowsnap.length; n++) {
                        if (holes[n] < 0)
                            nn++;
                        else holes[n] = nn;
                    }
                    n = 0;
                    nn = 0;
                    while (n < rowmap.length || nn < rowsnap.length) {
                        if (nn > rins || nn >= rowsnap.length) {
                            for (m = 0; m < rowcount; m++)
                                rowmap[n++] = oldrc + m;
                            rins = Integer.MAX_VALUE;
                        }
                        if (nn < rowsnap.length) {
                            if (holes[rowsnap[nn]] >= 0 && n < rowmap.length)
                                rowmap[n++] = rowsnap[nn] - holes[rowsnap[nn]];
                            nn++;
                        }
                    }
                    if (rowcount > 0)
                        System.arraycopy(placevals, 0, newvals, oldrc, rowcount);
                    break;
                default:
                    VM.abort(INTERNALERROR);
                    newvals = null; // for compiler
                }

                typelen = 0;
                if (typesnap == null || (typelen = typesnap.length) < valcc) {
                    typeinfo = new int[valcc];
                    for (n = 0; n < valcc; n++) {
                        if (n < typelen)
                            typeinfo[n] = typesnap[n];
                        else typeinfo[n] = YOIX_STRING_TYPE;
                    }
                } else typeinfo = typesnap;
		typelen = typeinfo == null ? 0 : typeinfo.length;

                if (valcc > 0) {
                    for (n = 0; n < valcc; n++) {
                        checkTypeForColumnValue(typeinfo[n], newvals[0][n], n, N_VALUES);
                    }
                }

                values = newvals;
		rowinvis = newinvis;
                cellected = null;
                if (values.length > 0) {
                    if (values[0].length > 0)
                        cellected = new boolean[values.length][values[0].length];
                }
                types = typeinfo;
                row2model = rowmap;
		setRow2ModelVisibility(true);	// true means rowinvis and row2model will be synced

		tcm = getColumnModel();
		oldcc = tcm.getColumnCount();
		colcount = getColumnCount();
                if (oldcc != colcount) {
                    if (oldcc < colcount) {
                        for (; oldcc < colcount; oldcc++) {
                            tblcol = new YoixSwingTableColumn(oldcc, oldcc < typelen ? typeinfo[oldcc] : YOIX_STRING_TYPE);
                            tags.put(tblcol.getTag(), new Integer(oldcc));
                            tblcol.setHeaderValue(getColumnName(oldcc));
                            tcm.addColumn(tblcol);
                        }
                    } else {
                        while (oldcc > colcount) {
                            tblcol = (YoixSwingTableColumn)tcm.getColumn(--oldcc);
                            tags.remove(tblcol.getTag());
                            tags.remove("_"+oldcc);
                            tcm.removeColumn(tblcol);
                        }
                        if (colcount < typeinfo.length) {
                            int[] ntypes = new int[colcount];
                            System.arraycopy(typeinfo,0,ntypes,0,colcount);
                            types = ntypes;
                        }
                    }
                    if (sync)
                        syncRowViews(false, false);
                    resetSort();
                    fireTableStructureChanged();
                } else {
                    if (sync)
                        syncRowViews(false, false);
                    if (!resortTable())
                        fireTableDataChanged();
                }
            }
        }


        final void
        resetRows(int len) {

	    int  newrows[];
	    int  n;

	    if (len < 0)
		len = 0;

	    newrows = new int[len];

	    for (n = 0; n < len; n++)
		newrows[n] = n;

	    row2model = newrows;
	}


	final void
	resetSort() {

	    resetRows(getRawRowCount());
	    resetStates(getColumnCount());
	    setRow2ModelVisibility(true);
	}


	final void
	resetStates(int len) {

	    int  newstates[];
	    int  n;

	    if (len < 0)
		len = 0;

	    newstates = new int[len];
	    for (n = 0; n < len; n++)
		newstates[n] = 0;
	    states = newstates;
	    updateSelectionMarks();
	    getTableHeader().repaint();
	}


	final synchronized void
	resetTable() {

	    TableColumnModel  tcm = getColumnModel();

	    tags.clear();

	    if (tcm != null) {
		while (tcm.getColumnCount() > 0) {
		    tcm.removeColumn(tcm.getColumn(0));
		}
	    }

	    columnNames = null;
	    types = null;
	    states = new int[0];
	    tooltips = null;
	    row2model = new int[0];
	    rowinvis = null;
	    values = null;
	    editinfo = null;
	    selectionmarks = null;
	}


	final boolean
	resortTable() {

	    JTableHeader  header;
	    boolean       resort = false;
	    int           columns[];
	    int           len;
	    int           ccnt;
	    int           m;
	    int           n;
	    int           s;

	    cancelEditing();
	    ccnt = getColumnCount();

	    len = 0;
	    n = states.length;
	    for (m = 0; m < n; m++) {
		if (states[m] != 0)
		    len++;
	    }

	    if (len > 0) {
		columns = new int[len];
		for (m = 0; m < len; m++)
		    columns[m] = 0;

		for (m = 0, n = 0; m < ccnt; m++) {
		    s = states[m];
		    if (s == 0)
			continue;
		    resort = true;
		    if (s < 0) {
			s = -s;
			n = s - 1;
			if (n >= 0 && n < len)
			    columns[n] = -(m+1);
		    } else {
			n = s - 1;
			if (n >= 0 && n < len)
			    columns[n] = (m+1);
		    }
		}
		if (resort) {
		    sortTable(columns);
		    yjtm.fireTableChanged(new TableModelEvent(yjtm));
		}
	    }

	    return(resort);
	}


	final synchronized void
	setAll(boolean value) {

	    int  rowsz;
	    int  colsz;
	    int  m;
	    int  n;

	    if (cellected != null && (rowsz = cellected.length) > 0) {
		colsz = cellected[0].length;
		for (m = 0; m < rowsz; m++) {
		    for (n = 0; n < colsz; n++)
			cellected[m][n] = value;
		}
	    }
	}


	final void
	setAltColor(YoixObject obj, boolean background) {

	    YoixObject  yobj;
	    Color       colors[] = null;
	    int         len;
	    int         m;
	    int         n;

	    if (obj.notNull()) {
		if (obj.isColor()) {
		    colors = new Color[] { YoixMake.javaColor(obj, null) };
		} else if (obj.isArray()) {
		    colors = new Color[obj.sizeof()];
		    len = obj.length();
		    for (m = 0, n = obj.offset(); n < len; m++, n++) {
			yobj = obj.get(n, false);
			if (yobj.notNull() && yobj.isColor()) {
			    colors[m] = YoixMake.javaColor(yobj, null);
			} else VM.abort(BADVALUE, background ? N_ALTBACKGROUND : N_ALTFOREGROUND, n);

		    }
		} else VM.abort(TYPECHECK, background ? N_ALTBACKGROUND : N_ALTFOREGROUND);
	    }

	    if (background)
		headerBackgrounds = colors;
	    else headerForegrounds = colors;
	    getTableHeader().repaint();
	}


	final void
	setAltAlignment(YoixObject obj) {

	    int align = obj.intValue();

	    if (align != headerAlignment) {
		if (align < 0) {
		    headerAlignment = -1;
		    headerHorizontalAlignment = -1;
		} else headerHorizontalAlignment = adjustAlignment(headerAlignment = align);

		getTableHeader().repaint();
	    }
	}


	final void
	setAltFont(YoixObject obj) {

	    Font  font = null;

	    if (obj.notNull()) {
		if (obj.isString() || obj.isFont())
		    font = YoixMakeScreen.javaFont(obj);
		else VM.abort(TYPECHECK, N_ALTFONT);
	    }
	    headerFont = font;
	    getTableHeader().resizeAndRepaint();
	}


	final void
	setAltGridColor(YoixObject obj) {

	    headerGridColor = obj.isNull() ? null : YoixMake.javaColor(obj);
	    getTableHeader().repaint();
	}


	final void
	setCellColors(YoixObject obj) {

	    YoixObject  yobj;
	    YoixObject  ycol;
	    Color       backcolors[] = null;
	    Color       forecolors[] = null;
	    Color       colors[];
	    int         idx;
	    int         val;
	    int         len;
	    int         off;
	    int         sizeof;
	    int         length;
	    int         offset;

	    if (obj.notNull() && (val = obj.sizeof()) > 0) {
		if (val < 3) {
		    len = obj.length();
		    for (off = val = obj.offset(); off < len; off++) {
			colors = null;
			yobj = obj.get(off, false);
			if (yobj.notNull()) {
			    if (yobj.isColor()) {
				colors = new Color[] { YoixMake.javaColor(yobj, null) };
			    } else if (yobj.isArray()) {
				if ((sizeof = yobj.sizeof()) > 0) {
				    colors = new Color[sizeof];
				    length = yobj.length();
				    for (idx = 0, offset = yobj.offset(); offset < length; idx++, offset++) {
					ycol = yobj.get(offset, false);
					if (ycol.notNull() && ycol.isColor()) {
					    colors[idx] = YoixMake.javaColor(ycol, null);
					} else VM.abort(BADVALUE, N_CELLCOLORS, off, offset);
				    }
				}
			    } else VM.abort(BADVALUE, N_CELLCOLORS, off);
			}
			if (off == val)
			    backcolors = colors;
			else
			    forecolors = colors;
		    }
		} else VM.abort(BADVALUE, N_CELLCOLORS);
	    }

	    cellBackgrounds = backcolors;
	    cellForegrounds = forecolors;

	    fireTableStructureChanged();
	}


	final void
	setColumn(YoixObject obj, YoixSwingTableColumn tblcol,  int column, int ccnt,  ArrayList datavalues, String errname) {

            YoixJTableCellRenderer  renderer = null;
	    YoixSwingJTable         jtable = YoixSwingJTable.this;
	    YoixObject              yobj;
	    YoixObject              yobj2;
	    Boolean                 booleans[];
	    String                  tips[];
	    Icon                    icons[];
	    Color                   color;
	    Color                   colors[];
	    Color                   colors2[];
	    Color                   colors3[];
	    Color                   colors4[];
	    Dimension               size;
	    boolean                 badvalue;
	    int                     val;
	    int                     value;
	    int                     type;
	    int                     idx;
	    int                     sz;
	    int                     len;
	    int                     off;
	    int                     i;
	    int                     j;
	    int                     m;
	    int                     n;

	    if ((val = obj.getInt(N_ALTALIGNMENT, -1)) < 0) {
		val = -1;
		value = -1;
	    } else value = adjustAlignment(val);
	    idx = tblcol.getHeaderAlignment(getHeaderAlignment(-1));
	    if (idx != val)
		tblcol.setHeaderAlignments(val, value);

	    yobj = obj.get(N_ALTFONT, false);
	    if (yobj.notNull() && (yobj.isString() || yobj.isFont())) {
		tblcol.setHeaderFont(YoixMakeScreen.javaFont(yobj));
		getTableHeader().resizeAndRepaint();
	    }

	    yobj = obj.get(N_ALTBACKGROUND, false);
	    if (yobj.notNull()) {
		colors = javaColorArray(yobj, errname, ":" + N_ALTBACKGROUND, null);
		tblcol.setHeaderBackgrounds(colors);
	    }

	    yobj = obj.get(N_ALTFOREGROUND, false);
	    if (yobj.notNull()) {
		colors = javaColorArray(yobj, errname, ":" + N_ALTFOREGROUND, null);
		tblcol.setHeaderForegrounds(colors);
	    }

	    yobj = obj.get(N_CELLCOLORS, false);
	    if (yobj.notNull()) {
		if (yobj.isArray()) {
		    if ((sz = yobj.sizeof()) <= 4) {
			colors = null;
			colors2 = null;
			colors3 = null;
			colors4 = null;
			len = yobj.length();
			for (n = yobj.offset(); n <len; n++) {
			    yobj2 = yobj.get(n, false);
			    switch (n) {
				case 0:
				    colors = javaColorArray(yobj2, errname, ":" + N_CELLCOLORS + "[" + n + "]", null);
				    break;

				case 1:
				    colors2 = javaColorArray(yobj2, errname, ":" + N_CELLCOLORS + "[" + n + "]", null);
				    break;

				case 2:
				    colors3 = javaColorArray(yobj2, errname, ":" + N_CELLCOLORS + "[" + n + "]", null);
				    break;

				case 3:
				    colors4 = javaColorArray(yobj2, errname, ":" + N_CELLCOLORS + "[" + n + "]", null);
				    break;
			    }
			}
		    } else {
			VM.abort(BADVALUE, errname, N_CELLCOLORS);
			colors = null; // for compiler
			colors2 = null;
			colors3 = null;
			colors4 = null;
		    }
		} else {
		    VM.abort(BADVALUE, errname, N_CELLCOLORS);
		    colors = null; // for compiler
		    colors2 = null;
		    colors3 = null;
		    colors4 = null;
		}

		tblcol.setCellBackgrounds(colors);
		tblcol.setCellForegrounds(colors2);
		tblcol.setCellSelectionBackgrounds(colors3);
		tblcol.setCellSelectionForegrounds(colors4);
	    }

	    yobj = obj.get(N_EDIT, false);
	    if (yobj.notNull()) {
		if (yobj.isInteger())
		    tblcol.setEditInfo(yobj.booleanValue() ? Boolean.TRUE : Boolean.FALSE);
		else {
		    booleans = javaBooleanArray(yobj, errname, ":"+N_EDIT, null);
		    tblcol.setEditInfo(booleans);
		}
	    } else tblcol.setEditInfo(null);

	    yobj = obj.get(N_ETC, false);
	    tblcol.setEtc(yobj);

	    yobj = obj.get(N_FONT, false);
	    if (yobj.notNull() && (yobj.isString() || yobj.isFont())) {
		tblcol.setFont(YoixMakeScreen.javaFont(yobj));
		recomputeRowHeight();
	    }

	    yobj = obj.get(N_HEADER, false);
	    if (yobj.notNull()) {
		if (columnNames == null || columnNames.length != ccnt) {
		    if (column != 0)
			VM.abort(BADVALUE, errname, new String[] {"cannot mix null and non-null in " + N_HEADER + "[" + column + "]"});
		    columnNames = new String[ccnt];
		    defaultColumnNames = false;
		}
		columnNames[column] = yobj.stringValue();
		tblcol.setHeaderValue(columnNames[column]);
	    }

	    yobj = obj.get(N_HEADERICONS, false);
	    if (yobj.notNull()) {
		if (headerIcons == null || headerIcons.length != ccnt) {
		    if (column != 0)
			VM.abort(BADVALUE, errname, new String[] {"cannot mix null and non-null in " + N_HEADERICONS + "[" + column + "]"});
		    headerIcons = new Icon[ccnt][];
		}
		headerIcons[column] = javaIconArray(yobj, errname, ":" + N_HEADERICONS, null);
		tblcol.setHeaderIcons(headerIcons[column]);
	    }

	    yobj = obj.get(N_BACKGROUND, false);
	    if (yobj.notNull()) {
		color = YoixMake.javaColor(yobj);
		tblcol.setBackground(color);
	    }

	    yobj = obj.get(N_FOREGROUND, false);
	    if (yobj.notNull()) {
		color = YoixMake.javaColor(yobj);
		tblcol.setForeground(color);
	    }

	    yobj = obj.get(N_EDITBACKGROUND, false);
	    if (yobj.notNull()) {
		color = YoixMake.javaColor(yobj);
		tblcol.setEditBackground(color);
	    }

	    yobj = obj.get(N_EDITFOREGROUND, false);
	    if (yobj.notNull()) {
		color = YoixMake.javaColor(yobj);
		tblcol.setEditForeground(color);
	    }

	    yobj = obj.get(N_SELECTIONBACKGROUND, false);
	    if (yobj.notNull()) {
		color = YoixMake.javaColor(yobj);
		tblcol.setSelectionBackground(color);
	    }

	    yobj = obj.get(N_SELECTIONFOREGROUND, false);
	    if (yobj.notNull()) {
		color = YoixMake.javaColor(yobj);
		tblcol.setSelectionForeground(color);
	    }

	    yobj = obj.get(N_DISABLEDBACKGROUND, false);
	    if (yobj.notNull()) {
		color = YoixMake.javaColor(yobj);
		tblcol.setDisabledBackground(color);
	    }

	    yobj = obj.get(N_DISABLEDFOREGROUND, false);
	    if (yobj.notNull()) {
		color = YoixMake.javaColor(yobj);
		tblcol.setDisabledForeground(color);
	    }

	    tblcol.setEditor(jtable, obj.get(N_CELLEDITOR, false));

	    yobj = obj.get(N_TAG, false);
	    if (yobj.notNull()) {
		tags.remove(tblcol.getTag());
		tblcol.setTag(yobj.stringValue());
		tags.put(tblcol.getTag(), new Integer(column));
	    }

	    yobj = obj.get(N_TEXT, false);
	    if (yobj.notNull()) {
		if (column > 0 && datavalues.size() == 0)
		    VM.abort(BADVALUE, errname, new String[] {"cannot mix null and non-null in " +  N_TEXT + "[" + column + "]"});
		processColumnText(yobj.stringValue(), column, datavalues);
	    } else if (datavalues.size() > 0) {
		VM.abort(BADVALUE, errname, new String[] {"cannot mix null and non-null in " +  N_TEXT + "[" + column + "]"});
	    }

	    yobj = obj.get(N_TOOLTIPTEXT, false);
	    if (yobj.notNull()) {
		tips = javaStringArray(yobj, errname, ":" + N_TOOLTIPTEXT, null);
		tblcol.setTipText(tips);
	    } else {
		yobj = obj.get(N_TOOLTIP, false);
		if (yobj.notNull()) {
		    tips = javaStringArray(yobj, errname, ":" + N_TOOLTIPTEXT, null);
		    tblcol.setTipText(tips);
		}
	    }

	    yobj = obj.get(N_ALTTOOLTIPTEXT, false);
	    if (yobj.notNull()) {
		tblcol.setHeaderTip(yobj.stringValue());
	    }

	    yobj = obj.get(N_TYPE, false);
	    type = yobj.intValue();
	    checkTypeForColumnValue(type, null, column, errname + ": " + N_TYPE);
	    if (type >= 0) {
		if (types == null || types.length != ccnt) {
		    if (column != 0)
			VM.abort(BADVALUE, errname, new String[] {"cannot mix null and non-null in " + N_HEADER + "[" + column + "]"});
		    types = new int[ccnt];
		}
		types[column] = type;
		tblcol.setType(type);
	    }

	    yobj = obj.get(N_ATTRIBUTES, false);
	    if (yobj.notNull()) {
		renderer = getColumnRenderer(tblcol, true);
		if (renderer == null) {
		    VM.abort(INTERNALERROR, new String[] {errname + ": " + N_ATTRIBUTES + " - could not get renderer for column " + column});
		}

		for (n = 0; n < boolAttrs.length; n++) {
		    if ((yobj2 = yobj.getObject(boolAttrs[n])) != null || (yobj2 = yobj.getObject(boolAttrs[n].toLowerCase())) != null) {
			if (yobj2.isInteger()) {
			    // these must be coordinated with boolAttrs
			    switch (n) {
			    case 0:
				renderer.setDecimalSeparatorAlwaysShown(yobj2.booleanValue());
				break;
			    case 1:
				renderer.setGroupingUsed(yobj2.booleanValue());
				break;
			    case 2:
				renderer.setParseIntegerOnly(yobj2.booleanValue());
				break;
			    case 3:
				renderer.setZeroNotShown(yobj2.booleanValue());
				break;
			    default:
				VM.abort(BADVALUE, errname, new String[] {"unrecognized attribute " + N_ATTRIBUTES + "[" + boolAttrs[n] + "]"});
			    }
			} else {
			    VM.abort(BADVALUE, errname, new String[] {N_ATTRIBUTES + "[" + boolAttrs[n] + "] must be a boolean"});
			}
		    }
		}

		for (n = 0; n < intAttrs.length; n++) {
		    if ((yobj2 = yobj.getObject(intAttrs[n])) != null || (yobj2 = yobj.getObject(intAttrs[n].toLowerCase())) != null) {
			//
			// This code aborted if the integer was less than zero,
			// but that didn't agree with SET_COLUMN_FIELD, so we
			// decided to be more forgiving here. Looks like all the
			// methods called here handle negative numbers, as do the
			// corresponding methods in DecimalFormat or NumberFormat.
			//
			// Actually turned out to be an issue when we tried to
			// build a new table from the columns read from another
			// table. maximumFractionDigits was set to 0 in one of
			// the columns (it was a percent type column) in original
			// table and that triggered a very unfriendly abort here.
			// Not being able to use the attributes that came from an
			// existing table just seemed wrong, which is why we made
			// the change (7/31/10) here and elsewhere.
			//
			if (yobj2.isInteger()) {
			    m = yobj2.intValue();
			    // these must be coordinated with intAttrs
			    switch (n) {
			    case 0:
				renderer.setGroupingSize(m);
				break;
			    case 1:
				renderer.setMaximumFractionDigits(m);
				break;
			    case 2:
				renderer.setMaximumIntegerDigits(m);
				break;
			    case 3:
				renderer.setMinimumFractionDigits(m);
				break;
			    case 4:
				renderer.setMinimumIntegerDigits(m);
				break;
			    case 5:
				renderer.setMultiplier(m);
				break;
			    default:
				VM.abort(BADVALUE, errname, new String[] {"unrecognized attribute " + N_ATTRIBUTES + "[" + intAttrs[n] + "]"});
			    }
			} else {
			    VM.abort(BADVALUE, errname, new String[] {N_ATTRIBUTES + "[" + intAttrs[n] + "] must be an integer"});
			}
		    }
		}

		for (n = 0; n < nbrAttrs.length; n++) {
		    if ((yobj2 = yobj.getObject(nbrAttrs[n])) != null || (yobj2 = yobj.getObject(nbrAttrs[n].toLowerCase())) != null) {
			if (yobj2.isNumber()) {
			    // these must be coordinated with nbrAttrs
			    switch (n) {
			    case 0:
				renderer.setOverflow(yobj2.doubleValue());
				break;
			    case 1:
				renderer.setUnderflow(yobj2.doubleValue());
				break;
			    default:
				VM.abort(BADVALUE, errname, new String[] {"unrecognized attribute " + N_ATTRIBUTES + "[" + nbrAttrs[n] + "]"});
			    }
			} else {
			    VM.abort(BADVALUE, errname, new String[] {N_ATTRIBUTES + "[" + nbrAttrs[n] + "] must be a number"});
			}
		    }
		}


		for (n = 0; n < strAttrs.length; n++) {
		    if ((yobj2 = yobj.getObject(strAttrs[n])) != null || (yobj2 = yobj.getObject(strAttrs[n].toLowerCase())) != null) {
			if (yobj2.notNull() && yobj2.isString()) {
			    // these must be coordinated with strAttrs
			    switch (n) {
			    case 0:
				renderer.setFormat(yobj2.stringValue());
				break;
			    case 1:
				renderer.setNegativePrefix(yobj2.stringValue());
				break;
			    case 2:
				renderer.setNegativeSuffix(yobj2.stringValue());
				break;
			    case 3:
				renderer.setPositivePrefix(yobj2.stringValue());
				break;
			    case 4:
				renderer.setPositiveSuffix(yobj2.stringValue());
				break;
			    case 5:
				renderer.setLowSubstitute(new String[] { yobj2.stringValue() });
				break;
			    case 6:
				renderer.setHighSubstitute(new String[] { yobj2.stringValue() });
				break;
			    case 7:
				renderer.setInputFormat(yobj2.stringValue());
				break;
			    case 8:
				if (renderer instanceof YoixJTableDateRenderer)
				    ((YoixJTableDateRenderer)renderer).setTimeZone(yobj2.stringValue());
				break;
			    case 9:
				if (renderer instanceof YoixJTableDateRenderer)
				    ((YoixJTableDateRenderer)renderer).setInputTimeZone(yobj2.stringValue());
				break;
			    case 10:
				renderer.setRendererLocale(yobj2.stringValue());
				break;
			    case 11:
				renderer.setInputLocale(yobj2.stringValue());
				break;
			    default:
				VM.abort(BADVALUE, errname, new String[] {"unrecognized attribute " + N_ATTRIBUTES + "[" + strAttrs[n] + "]"});
			    }
			} else if (yobj2.notNull() && yobj2.isArray()) {
			    switch (n) {
			    case 5:
				renderer.setLowSubstitute(YoixMake.javaStringArray(yobj2));
				break;
			    case 6:
				renderer.setHighSubstitute(YoixMake.javaStringArray(yobj2));
				break;
			    default:
				VM.abort(BADVALUE, errname, new String[] {"unrecognized array attribute " + N_ATTRIBUTES + "[" + strAttrs[n] + "]"});
			    }
			} else {
			    VM.abort(BADVALUE, errname, new String[] {N_ATTRIBUTES + "[" + strAttrs[n] + "] must be a non-null string"});
			}
		    }
		}
	    }

	    if (renderer == null && (renderer = (YoixJTableCellRenderer)(tblcol.getCellRenderer())) == null)
		renderer = (YoixJTableCellRenderer)(jtable.getDefaultRenderer(jtable.getTypeClass(type < 0 ? YOIX_STRING_TYPE : type)));

	    if ((val = obj.getInt(N_ALIGNMENT, -1)) < 0) {
		val = -1;
		value = -1;
	    } else value = adjustAlignment(val);
	    idx = tblcol.getAlignment(renderer.getDefaultAlignment());
	    if (idx != val)
		tblcol.setAlignments(val, value);

	    // TODO: values

	    yobj = obj.get(N_VISIBLE, false);
	    if (yobj.notNull()) {
		tblcol.setVisible(yobj.booleanValue());
	    }

	    //
	    // Though we setPreferredWidth of a column here, it doesn't seem like it is
	    // necessary to worry about adjusting the scrollable viewport size as we do
	    // elsewhere since when we add this column it should happen implicitly
	    //

	    yobj = obj.get(N_WIDTH, false);
	    badvalue = true;
	    if (yobj.isNull())
		badvalue = false;
	    else if (yobj.isNumber()) {
		badvalue = false;
		size = YoixMakeScreen.javaDimension(yobj, yobj);
		value = size.width;
		if (value >= 0)
		    tblcol.setPreferredWidth(value);
	    } else if (yobj.notNull() && yobj.isDictionary()) {
		if ((yobj2 = yobj.getObject(N_WIDTH)) != null && yobj2.isNumber()) {
		    badvalue = false;
		    size = YoixMakeScreen.javaDimension(yobj2, yobj2);
		    value = size.width;
		    if (value >= 0)
			tblcol.setPreferredWidth(value);
		}
		if ((yobj2 = yobj.getObject(N_MINIMUM)) != null && yobj2.isNumber()) {
		    badvalue = false;
		    size = YoixMakeScreen.javaDimension(yobj2, yobj2);
		    value = size.width;
		    if (value >= 0)
			tblcol.setMinWidth(value);
		}
		if ((yobj2 = yobj.getObject(N_MAXIMUM)) != null && yobj2.isNumber()) {
		    badvalue = false;
		    size = YoixMakeScreen.javaDimension(yobj2, yobj2);
		    value = size.width;
		    if (value >= 0)
			tblcol.setMaxWidth(value);
		}
	    }

	    if (badvalue)
		VM.abort(BADVALUE, errname, N_WIDTH);

	    yobj = obj.get(N_PICKSORTOBJECT, false);
	    if (yobj.isNull() || yobj.callable(1))
		tblcol.setPickSortObject(yobj);
	    else VM.abort(BADVALUE, errname, N_PICKSORTOBJECT);

	    yobj = obj.get(N_PICKTABLEOBJECT, false);
	    if (yobj.isNull() || yobj.callable(1))
		tblcol.setPickTableObject(yobj);
	    else VM.abort(BADVALUE, errname, N_PICKTABLEOBJECT);

	    yobj = obj.get(N_SELECTIONMARK, false);
	    if (yobj.notNull()) {
		//
		// For now we only support string values and we also decided
		// accepts all strings (zero length or all whitespace). This
		// stuff was added rather quickly. so there's probably lots
		// of room for improvement here and/or in code that supports
		// selectionmarks.
		//
		if (yobj.isString()) {
		    if (selectionmarks == null) {
			selectionmarks = new ArrayList();
			//
			// First entry is a copy of the row selections that
			// handleUpdateSelectionMarks() used the last it was
			// called. At this point could there be selections??
			// If so they should be stored in the first slot.
			//
			selectionmarks.add(null);
		    }
		    selectionmarks.add(new Integer(column));
		    selectionmarks.add(yobj.stringValue());
		    //
		    // A column that's used to mark selected rows shouldn't
		    // be editable, so we enforce it here.
		    //
		    tblcol.setEditInfo(Boolean.FALSE);
		} else VM.abort(BADVALUE, errname, N_SELECTIONMARK);
	    }
	}


	final synchronized void
	setColumns(YoixObject obj) {

	    YoixSwingTableColumn  tblcol;
	    YoixSwingJTable       jtable = YoixSwingJTable.this;
	    TableColumnModel      tcm;
	    YoixObject            yobj;
	    ArrayList             datavalues;
	    String                tag;
	    int                   typesnap[] = types;
	    int                   typelen = typesnap == null ? 0 : typesnap.length;
	    int                   len;
	    int                   off;
	    int                   ccnt;
	    int                   i;
	    int                   j;

	    resetTable();
	    if (obj.notNull()) {
		tcm = jtable.getColumnModel();
		len = obj.length();
		off = obj.offset();
		ccnt = len - off;
		datavalues = new ArrayList();
		for (i = off, j = 0; i < len; i++, j++) {
		    yobj = obj.get(i, false);
		    if (yobj.isJTableColumn()) {
			tblcol = new YoixSwingTableColumn(j, j < typelen ? typesnap[j] : YOIX_STRING_TYPE);
			tags.put(tblcol.getTag(), new Integer(j));
			setColumn(yobj, tblcol, j, ccnt, datavalues, N_COLUMNS);
			tcm.addColumn(tblcol);
		    } else VM.abort(BADVALUE, N_COLUMNS, j);
		}
		if ((ccnt = datavalues.size()) > 0) {
		    String[][]  strvals = new String[len=(((String[])(datavalues.get(0))).length)][ccnt];
		    String[]    colvals;
		    for (j = 0; j < ccnt; j++) {
			colvals = (String[])(datavalues.get(j));
			for (i = 0; i < len; i++) {
			    strvals[i][j] = colvals[i];
			}
		    }
		    values = processStringValues(strvals, len, ccnt, N_COLUMNS);
		    cellected = null;
		    if (values.length > 0) {
			if (values[0].length > 0) {
			    cellected = new boolean[values.length][values[0].length];
			}
		    }

		    states = new int[ccnt];
		    for (i = 0; i < ccnt; i++)
			states[i] = 0;

		    row2model = new int[len];
		    for (i = 0; i < len; i++)
			row2model[i] = i;
		    rowinvis = null;
		}
	    }

	    fireTableStructureChanged();

	}


	final synchronized Rectangle
	setColumnSelectionInterval(int col0, int col1, int mode) {

	    Rectangle  rect = null;
	    boolean    value = false;
	    boolean    toggle = false;
	    int        rowsz;
	    int        m, n;
	    int        mm, mn;
	    int        minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = -1, maxy = -1;

	    if (mode == 0)
		toggle = true;
	    else if (mode < 0)
		value = false;
	    else value = true;

	    if (cellected != null && col0 >= 0 && col1 >= 0 && (rowsz = cellected.length) > 0 && col0 < cellected[0].length && col1 < cellected[0].length) {
		for (m = 0; m < rowsz; m++) {
		    for (n = col0; n <= col1; n++) {
			mm = yoixConvertRowIndexToModel(m);
			mn = yoixConvertColumnIndexToModel(n);
			if (toggle || cellected[mm][mn] != value) {
			    if (toggle)
				cellected[mm][mn] = !cellected[mm][mn];
			    else cellected[mm][mn] = value;
			    if (m < minx)
				minx = m;
			    if (m > maxx)
				maxx = m;
			    if (n < miny)
				miny = n;
			    if (n > maxy)
				maxy = n;
			}
		    }
		}
		if (maxx >= 0) {
		    Rectangle firstCell = getCellRect(minx, miny, false);
		    Rectangle lastCell = getCellRect(maxx, maxy, false);
		    rect = firstCell.union(lastCell);
		}
	    }
	    return(rect);
	}


	final void
	setEditInfo(Object info) {

	    editinfo = info;
	    repaint();
	}


	final void
	setHeaders(YoixObject obj) {

	    TableColumnModel      tcm;
	    YoixSwingTableColumn  tblcol;
	    YoixObject            yobj;
	    boolean               useDefault = false;
	    Object                vals[][];
	    String                hdrs[] = null;
	    String                tag;
	    int                   len;
	    int                   off;
	    int                   i, j;
	    int                   oldcc = getColumnCount();
	    int                   colcount;
	    int                   typesnap[] = types;
	    int                   typelen = typesnap == null ? 0 : typesnap.length;

	    if (obj.notNull()) {
		if (obj.isInteger())
		    useDefault = obj.booleanValue();
		else if (obj.isString()) {
		    hdrs = separateHeader(obj.stringValue());
		} else if (obj.isArray()) {
		    len = obj.length();
		    off = obj.offset();
		    if (len - off > 0) {
			hdrs = new String[len-off];
			for (i = off, j = 0; i < len; i++) {
			    yobj = obj.get(i, false);
			    if (yobj.isNull())
				hdrs[j++] = null;
			    else if (yobj.isString())
				hdrs[j++] = yobj.stringValue();
			    else VM.abort(BADVALUE, N_HEADERS, i);
			}
		    }
		} else VM.abort(TYPECHECK, N_HEADERS);
	    }

	    if (useDefault || hdrs == null) {
		if ((vals = values) != null &&  vals.length > 0)
		    colcount = vals[0].length;
		else colcount = 0;
		columnNames = hdrs;
	    } else {
		columnNames = hdrs;
		colcount = getColumnCount();
	    }

	    defaultColumnNames = useDefault;

	    // create columns as needed
	    tcm = getColumnModel();
	    oldcc = tcm.getColumnCount();
	    if (oldcc < colcount) {
		for (i = 0; i < oldcc; i++) {
		    tblcol = (YoixSwingTableColumn)tcm.getColumn(i);
		    tblcol.setHeaderValue(getColumnName(i));
		}
		for (i = oldcc; i < colcount; i++) {
		    tblcol = new YoixSwingTableColumn(i, i < typelen ? typesnap[i] : YOIX_STRING_TYPE);
		    tags.put(tblcol.getTag(), new Integer(i));
		    tblcol.setHeaderValue(getColumnName(i));
		    tcm.addColumn(tblcol);
		}
		fireTableStructureChanged();
	    } else {
		useDefault = (oldcc == colcount);
		for (i = 0; i < colcount; i++) {
		    tblcol = (YoixSwingTableColumn)tcm.getColumn(i);
		    tblcol.setHeaderValue(getColumnName(i));
		}
		while (oldcc > colcount) {
		    tblcol = (YoixSwingTableColumn)tcm.getColumn(--oldcc);
		    tags.remove(tblcol.getTag());
		    tags.remove("_"+oldcc);
		    tcm.removeColumn(tblcol);
		}
		if (!useDefault) {
		    if (typesnap != null) {
			int ntypes[] = new int[colcount];
			System.arraycopy(typesnap,0,ntypes,0,colcount);
			types = ntypes;
		    }
		    fireTableStructureChanged();
		}
	    }
	    getTableHeader().resizeAndRepaint();
	}


	final void
	setHeaderIcons(YoixObject obj) {

	    TableColumnModel      tcm;
	    YoixSwingTableColumn  tblcol;
	    YoixObject            yobj;
	    YoixObject            yobj2;
	    Icon                  icons[][] = null;
	    int                   len, len2;
	    int                   m, m2;
	    int                   n, n2;

	    int                   i, j;
	    int                   oldcc;
	    int                   colcount;
	    int                   typesnap[] = types;
	    int                   typelen = typesnap == null ? 0 : typesnap.length;

	    if (obj.notNull()) {
		if (obj.isImage()) {
		    icons = new Icon[1][1];
		    icons[0][0] = YoixMake.javaIcon(obj);
		} else if (obj.isArray()) {
		    if (obj.sizeof() > 0) {
			if (obj.sizeof() > 1 || obj.get(obj.offset(), false).notNull()) {
			    icons = new Icon[obj.sizeof()][];
			    len = obj.length();
			    for (m = 0, n = obj.offset(); n < len; m++, n++) {
				yobj = obj.get(n, false);
				if (yobj.notNull()) {
				    if (yobj.isImage()) {
					icons[m] = new Icon[] { YoixMake.javaIcon(yobj) };
				    } else if (yobj.isArray()) {
					if (yobj.sizeof() > 0) {
					    if (yobj.sizeof() > 2) {
						len2 = 3;
						icons[m] = new Icon[3];
					    } else {
						len2 = 1;
						icons[m] = new Icon[1];
					    }
					    for (m2 = 0, n2 = yobj.offset(); m2 < len2; m2++, n2++) {
						yobj2 = yobj.get(n2, false);
						if (yobj2.notNull()) {
						    if (yobj2.isImage()) {
							icons[m][m2] = YoixMake.javaIcon(yobj2);
						    } else VM.abort(TYPECHECK, N_HEADERICONS, m, m2);
						} else icons[m][m2] = null;
					    }
					} else icons[m] = new Icon[] { null };
				    } else VM.abort(TYPECHECK, N_HEADERICONS, m);
				} else icons[m] = new Icon[] { null };
			    }
			}
		    }
		} else VM.abort(TYPECHECK, N_HEADERICONS);
	    }

	    headerIcons = icons;

	    colcount = getColumnCount();

	    // create columns as needed
	    tcm = getColumnModel();
	    oldcc = tcm.getColumnCount();
	    if (oldcc < colcount) {
		//for (i = 0; i < oldcc; i++) {
		    //tblcol = (YoixSwingTableColumn)tcm.getColumn(i);
		    //tblcol.setHeaderIcons(getHeaderIcons(i));
		//}
		for (i = oldcc; i < colcount; i++) {
		    tblcol = new YoixSwingTableColumn(i, i < typelen ? typesnap[i] : YOIX_STRING_TYPE);
		    tags.put(tblcol.getTag(), new Integer(i));
		    //tblcol.setHeaderIcons(getHeaderIcons(i));
		    tcm.addColumn(tblcol);
		}
		fireTableStructureChanged();
	    } else {
		//for (i = 0; i < colcount; i++) {
		    //tblcol = (YoixSwingTableColumn)tcm.getColumn(i);
		    //tblcol.setHeaderIcons(getHeaderIcons(i));
		//}
		while (oldcc > colcount) {
		    tblcol = (YoixSwingTableColumn)tcm.getColumn(--oldcc);
		    tags.remove(tblcol.getTag());
		    tags.remove("_"+oldcc);
		    tcm.removeColumn(tblcol);
		}
		if (typesnap != null && oldcc > colcount) {
		    int ntypes[] = new int[colcount];
		    System.arraycopy(typesnap,0,ntypes,0,colcount);
		    types = ntypes;
		}
		fireTableStructureChanged();
	    }
	    getTableHeader().resizeAndRepaint();
	}


	final void
	setHeaderTips(YoixObject obj) {

	    String      tips[] = null;

	    if (obj.notNull())
		tips = javaStringArray(obj, N_ALTTOOLTIPTEXT, "", null);

	    headerTips = tips;
	}


	final void
	setInputFilter(Object[] table, String delim) {

	    inputfilter = table;
	    input_delimiter = delim;
	}


	final void
	setOutputFilter(String delim) {

	    output_delimiter = delim;
	}


	final void
	setRecordFilter(String delim) {

	    record_delimiter = delim;
	}


	final synchronized Rectangle
	setRowSelectionInterval(int row0, int row1, int mode) {
	    Rectangle rect = null;
	    int colsz;
	    int m, n;
	    int mm, mn;
	    int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE, maxx = -1, maxy = -1;
	    boolean value = false, toggle = false;

	    if (mode == 0)
		toggle = true;
	    else if (mode < 0)
		value = false;
	    else value = true;

	    if (cellected != null && row0 >= 0 && row1 >= 0 && row0 < cellected.length && row1 < cellected.length) {
		colsz = cellected[0].length;
		for (m = row0; m <= row1; m++) {
		    mm = yoixConvertRowIndexToModel(m);
		    for (n = 0; n < colsz; n++) {
			mn = yoixConvertColumnIndexToModel(n);
			if (toggle || cellected[mm][mn] != value) {
			    if (toggle)
				cellected[mm][mn] = !cellected[mm][mn];
			    else cellected[mm][mn] = value;
			    if (m < minx)
				minx = m;
			    if (m > maxx)
				maxx = m;
			    if (n < miny)
				miny = n;
			    if (n > maxy)
				maxy = n;
			}
		    }
		}
		if (maxx >= 0) {
		    Rectangle firstCell = getCellRect(minx, miny, false);
		    Rectangle lastCell = getCellRect(maxx, maxy, false);
		    rect = firstCell.union(lastCell);
		}
	    }
	    return(rect);
	}


	final void
	setTipText(Object obj) {

	    tooltips = obj;
	}


	final synchronized void
	setTypes(YoixObject obj) {

	    YoixSwingTableColumn  tblcol;
	    TableColumnModel      tcm;
	    YoixObject            yobj;
	    Object                vals[][];
	    Object                sample[];
	    int                   typs[] = null;
	    int                   typelen;
	    int                   type = -1;
	    int                   len;
	    int                   off;
	    int                   i;
	    int                   j;

	    if (obj.notNull()) {
		len = obj.length();
		off = obj.offset();
		typelen = len - off;
		vals = values; // snapshot
		if (vals != null && vals.length > 0) {
		    sample = vals[0];
		    if (sample.length > typelen)
			typelen = sample.length;
		} else sample = null;
		typs = new int[typelen];
		for (i = off, j = 0; j < typelen; i++, j++) {
		    if (i < len) {
			yobj = obj.get(i, false);
			if (yobj.isInteger()) {
			    type = yobj.intValue();
			} else VM.abort(BADVALUE, N_TYPES, j);
		    } else type = YOIX_STRING_TYPE;
		    checkTypeForColumnValue(type, sample == null ? null : sample[j], j, N_TYPES);
		    typs[j] = type;
		    if ((tcm = getColumnModel()) != null && tcm.getColumnCount() > j)
			if ((tblcol = (YoixSwingTableColumn)tcm.getColumn(j)) != null)
			    tblcol.setType(type);
		}
	    }
	    types = typs;
	}


	final synchronized void
	syncRowViews(boolean dataview, boolean refresh)  {

	    Object  valsnap[][] = values;
	    Object  newvals[][];
	    int     vissnap[] = rowinvis;
	    int     newinvis[] = null;
	    int     rowsnap[] = row2model;
	    int     ccnt;
	    int     m;
	    int     n;

	    ccnt = getColumnCount();

	    if (rowsnap != null && rowsnap.length > 0 && ccnt > 0 && valsnap != null) {
		if (dataview) {
		    for (m = 0; m < rowsnap.length; m++)
			rowsnap[m] = m;
		    row2model = rowsnap;
		} else {
		    newvals = new Object[rowsnap.length][ccnt];
		    if (vissnap != null)
			newinvis = new int[rowsnap.length];
		    for (m = 0; m < rowsnap.length; m++) {
			n = rowsnap[m];
			newvals[m] = valsnap[n];
			if (newinvis != null)
			    newinvis[m] = vissnap[n];
		    }
		    for (m = 0; m < rowsnap.length; m++) {
			valsnap[m] = newvals[m];
			rowsnap[m] = m;
		    }
		    values = valsnap;
		    row2model = rowsnap;
		    rowinvis = newinvis;
		    // TODO: need a setRow2ModelVisibility here or something else?
		}
		if (refresh) {
		    if (!resortTable())
			fireTableDataChanged();
		}
	    }
	}


	final synchronized void
	setValues(Object object) {

	    YoixObject            obj;
	    YoixSwingTableColumn  tblcol;
	    TableColumnModel      tcm;
	    Object                newvals[][] = null;
	    String                tag;
	    int                   typesnap[] = types;
	    int                   typeinfo[] = null;
	    int                   rowmap[] = null;
	    int                   typelen;
	    int                   rowcount = -1;
	    int                   colcount = -1;
	    int                   oldcc = getColumnCount();
	    int                   n;
	    int                   valcc = 0;

	    if (object instanceof YoixObject) {
		obj = (YoixObject)object;
		if (obj.notNull()) {
		    if (obj.isString()) {
			newvals = processText(obj.stringValue(), -1);
		    } else if (obj.isArray()) {
			newvals = processValues(obj, -1);
		    }
		}
	    } else if (object instanceof Object[][]) {
		newvals = (Object[][])object;
	    } else {
		VM.abort(INTERNALERROR);
	    }

	    parent.getPeerScroller().getViewport().setViewPosition(new Point(0,0));

	    if (newvals == null) {
		values = null;
		row2model = new int[0];
		rowinvis = null;
		types = typesnap;
		resetSort();
		fireTableDataChanged();
	    } else {
		if (newvals.length == 0) {
		    rowcount = 0;
		    colcount = oldcc;
		} else {
		    rowcount = newvals.length;
		    valcc = newvals[0].length;
		    if (defaultColumnNames || columnNames == null)
			colcount = valcc;
		    else colcount = oldcc;
		}

		rowmap = new int[rowcount];
		for (n = 0; n < rowcount; n++)
		    rowmap[n] = n;

		typelen = 0;
		if (typesnap == null || (typelen = typesnap.length) < valcc) {
		    typeinfo = new int[valcc];
		    for (n = 0; n < valcc; n++) {
			if (n < typelen)
			    typeinfo[n] = typesnap[n];
			else typeinfo[n] = YOIX_STRING_TYPE;
		    }
		} else typeinfo = typesnap;
		typelen = typeinfo == null ? 0 : typeinfo.length;

		if (valcc > 0) {
		    for (n = 0; n < valcc; n++) {
			checkTypeForColumnValue(typeinfo[n], newvals[0][n], n, N_VALUES);
		    }
		}

                if (newvals != null && newvals.length != 0 && newvals[0].length < oldcc) {
                    int nvc = newvals[0].length;
                    Object[][] tmpvals = new Object[newvals.length][oldcc];
                    for (int r = 0; r < newvals.length; r++)
                        System.arraycopy(newvals[r], 0, tmpvals[r], 0, nvc);

                    newvals = tmpvals;
                }


		values = newvals;
		cellected = null;
		if (values.length > 0) {
		    if (values[0].length > 0)
			cellected = new boolean[values.length][values[0].length];
		}
		types = typeinfo;
		row2model = rowmap;
		rowinvis = null;

		tcm = getColumnModel();
		oldcc = tcm.getColumnCount();
		colcount = getColumnCount();
		if (oldcc != colcount) {
		    if (oldcc < colcount) {
			for (; oldcc < colcount; oldcc++) {
			    tblcol = new YoixSwingTableColumn(oldcc, oldcc < typelen ? typeinfo[oldcc] : YOIX_STRING_TYPE);
			    tags.put(tblcol.getTag(), new Integer(oldcc));
			    tblcol.setHeaderValue(getColumnName(oldcc));
			    tcm.addColumn(tblcol);
			}
		    } else {
			while (oldcc > colcount) {
			    tblcol = (YoixSwingTableColumn)tcm.getColumn(--oldcc);
			    tags.remove(tblcol.getTag());
			    tags.remove("_"+oldcc);
			    tcm.removeColumn(tblcol);
			}
			if (colcount < typeinfo.length) {
			    int[] ntypes = new int[colcount];
			    System.arraycopy(typeinfo,0,ntypes,0,colcount);
			    types = ntypes;
			}
		    }
		    resetSort();
		    fireTableStructureChanged();
		} else {
		    if (!resortTable())
			fireTableDataChanged();
		}
	    }
	}


	final void
	sortTable(int columns[]) {

	    int      len;
	    int      ccnt;
	    int      m;
	    int      n;
	    int      s;
	    int      sortmap[];
	    int      index;
	    boolean  proceed = true;

	    cancelEditing();
	    len = columns == null ? 0 : columns.length;
	    ccnt = getColumnCount();
	    sortmap = getSortMap();

	    resetStates(ccnt);

	    if (values != null && values.length > 0) {
		if (values[0].length < ccnt)
		    ccnt = values[0].length;
	    } else ccnt = 0;

	    for (n = 0; n < len; n++) {
		m = columns[n];
		if (m < 0) {
		    s = -(n+1);
		    m = -m;
		    m--;
		} else if (m > 0) {
		    s = n+1;
		    m--;
		} else s = 0;

		if (m >= 0 && m < ccnt)
		    states[m] = s;
		else columns[n] = 0;
	    }

	    //
	    // Added on 7/15/10, initially so date strings displayed in a
	    // STRING_TYPE column could be sorted using a hidden unixtime
	    // column. In this instance we wanted to display date strings
	    // with mix of timezones, which can't be handled by DATE_TYPE
	    // columns, but we also wanted them to be sorted according to
	    // their unixtime values, which were stored in a hidden column.
	    //

	    if ((sortmap = getSortMap()) != null) {
		for (n = 0; n < len; n++) {
		    if ((m = columns[n]) != 0) {
			if ((index = (m < 0 ? -m : m) - 1) < sortmap.length)
			    columns[n] = (sortmap[index] + 1) * (m < 0 ? -1 : 1);
		    }
		}
	    }

	    // values or columns == null just means row2model will be null;
	    // types == null means it will be unsorted
	    row2model = YoixMiscQsort.qsort(values, types, columns, 1);
	    setRow2ModelVisibility(true);
	}

	private int[]
	getReverseRow2ModelMap() {
	    int  rowsnap[] = row2model;
	    int  revmap[];
	    int  n;
	    int  len;

	    len = rowsnap.length;
	    revmap = new int[len];
	    for (n = 0; n < len; n++)
		revmap[rowsnap[n]] = n;

	    return(revmap);
	}


	private void
	setRow2ModelVisibility(boolean postsort) {
	    boolean  sorted;
	    int      vissnap[] = rowinvis;
	    int      rowsnap[] = row2model;
	    int      row2new[];
	    int      vissort[];
	    int      len;
	    int      m, n;

	    if (vissnap != null && (len = vissnap.length) > 0) {
		if (len == rowsnap.length) {
		    if (postsort) {
			vissort = new int[len]; // mis-named in this case
			for (n = 0; n < len; n++) {
			    m = rowsnap[n];
			    vissort[m] = vissnap[m] < len ? n : (len+n);
			}
			rowinvis = vissnap = vissort;
		    }

		    vissort = new int[len];
		    System.arraycopy(vissnap, 0, vissort, 0, len);
		    row2new = YoixMiscQsort.qsort(vissort, 1);
		    if (vissort[len-1] < len)
			rowinvis = null;
		    row2model = row2new;
		} else VM.abort(INTERNALERROR);
	    } else rowinvis = null;
	}


	final synchronized boolean
	stopEditing() {

	    YoixSwingJTable  jtable = YoixSwingJTable.this;
	    TableCellEditor  tblced;
	    boolean          stopped = true;

	    //
	    // Now returns a boolean so the caller can figure out when the
	    // stop request succeeded. Needed because we could get into an
	    // infinite loop of validator() calls when it returned false.
	    // Could happen when YoixJTableCellEditor.keyPressed() decides
	    // to call postEvent() after calling this method. The behavior
	    // was not hard to duplicate.
	    //

	    if ((tblced = jtable.getCellEditor()) != null) {
		if (tblced != null && tblced instanceof YoixJTableCellEditor)
		    stopped = ((YoixJTableCellEditor)tblced).stopCellEditing();
	    }

	    return(stopped);
	}


	final synchronized void
	toggleAll() {

	    int  rowsz;
	    int  colsz;
	    int  m;
	    int  n;

	    if (cellected != null && (rowsz = cellected.length) > 0) {
		colsz = cellected[0].length;
		for (m = 0; m < rowsz; m++) {
		    for (n = 0; n < colsz; n++)
			cellected[m][n] = !cellected[m][n];
		}
	    }
	}


	final YoixObject
	yoixObjectForTypedValue(int type, Object value) {

	    return(yoixObjectForTypedValue(type, value, true));
	}


	final YoixObject
	yoixObjectForTypedValue(int type, Object value, boolean stringy) {

	    YoixObject  yobj = null;

	    switch (type) {
	    case YOIX_BOOLEAN_TYPE:
		yobj = YoixObject.newInt(value == null ? 0 : (((Boolean)value).booleanValue() ? 1 : 0));
		break;
	    case YOIX_DATE_TYPE:
		if (stringy)
		    yobj = YoixObject.newString(value == null ? "" : sdf.format((Date)value));
		else
		    yobj = YoixObject.newDouble(value == null ? Double.NaN : ((double)((Date)value).getTime())/1000.0);
		break;
	    case YOIX_DOUBLE_TYPE:
		yobj = YoixObject.newDouble(value == null ? Double.NaN : ((Double)value).doubleValue());
		break;
	    case YOIX_HISTOGRAM_TYPE:
		yobj = YoixObject.newDouble(value == null ? Double.NaN : ((YoixJTableHistogram)value).doubleValue());
		break;
	    case YOIX_ICON_TYPE:
		if (stringy) {
		    if (value == null)
			yobj = YoixObject.newString();
		    else yobj = YoixObject.newString(((YoixJTableIcon)value).toString());
		} else {
		    if (value == null)
			yobj = YoixObject.newImage();
		    else yobj = YoixMake.yoixIcon((YoixJTableIcon)value);
		}
		break;
	    case YOIX_INTEGER_TYPE:
		yobj = YoixObject.newInt(value == null ? Integer.MIN_VALUE : ((Integer)value).intValue());
		break;
	    case YOIX_MONEY_TYPE:
		yobj = YoixObject.newDouble(value == null ? Double.NaN : ((YoixJTableMoney)value).doubleValue());
		break;
	    case YOIX_OBJECT_TYPE:
		yobj = YoixObject.newString(value == null ? "" : ((YoixJTableObject)value).stringValue());
		break;
	    case YOIX_PERCENT_TYPE:
		yobj = YoixObject.newDouble(value == null ? Double.NaN : ((YoixJTablePercent)value).doubleValue());
		break;
	    case YOIX_STRING_TYPE:
		yobj = YoixObject.newString((String)value);
		break;
	    case YOIX_TEXT_TYPE:
		yobj = YoixObject.newString(((YoixJTableText)value).toString());
		break;
	    case YOIX_TIMER_TYPE:
		if (stringy)
		    yobj = YoixObject.newString(value == null ? null : ((YoixJTableTimer)value).toString(timer_format));
		else
		    yobj = YoixObject.newDouble(value == null ? Double.NaN : ((YoixJTableTimer)value).doubleValue());
		break;
	    default:
		VM.abort(INTERNALERROR);
	    }

	    return(yobj);
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	private void
	processColumnText(String text, int column, ArrayList array) {

	    BufferedReader  reader = null;
	    String          vals[];
	    String          tmpvals[];
	    String          line;
	    int             increment;
	    int             rcnt;
	    int             rows;

	    rows = increment = 100;
	    rcnt = 0;
	    vals = new String[rows];

	    try {
		reader = new BufferedReader(new StringReader(text));
		while ((line = reader.readLine()) != null) {
		    if (rcnt >= rows) {
			rows += increment;
			tmpvals = new String[rows];
			System.arraycopy(vals, 0, tmpvals, 0, rcnt);
			vals = tmpvals;
		    }
		    vals[rcnt++] = line;
		}
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	    catch(RuntimeException e) {
		VM.caughtException(e);
	    }
	    finally {
		try {
		    reader.close();
		}
		catch(IOException e) {
		    VM.caughtException(e);
		}
	    }

	    if (rcnt < rows) {
		tmpvals = new String[rcnt];
		System.arraycopy(vals, 0, tmpvals, 0, rcnt);
		vals = tmpvals;
	    }

	    array.add(vals);
	}


	private Object
	processField(String field, int type, int cidx, String errname) {

	    YoixJTableCellRenderer  renderer;
	    YoixSwingTableColumn    tblcol;
	    TableColumnModel        tcm;
	    YoixSimpleDateFormat    csdf;
	    Object                  obj = null;
	    String                  trimmed;

	    switch (type) {
	    case YOIX_BOOLEAN_TYPE:
		if (field != null) {
		    trimmed = field.trim();
		    if ("1".equals(trimmed) || "yes".equalsIgnoreCase(trimmed) || "true".equalsIgnoreCase(trimmed))
			obj = Boolean.TRUE;
		    else obj = Boolean.FALSE;
		} else obj = Boolean.FALSE;
		break;

	    case YOIX_DATE_TYPE:
		synchronized(this) {
		    try {
			obj = new Double(field);
			if (obj != null && ((Double)obj).isNaN())
			    obj = null;
			else obj = new Date(1000 * ((Double)obj).longValue());
		    }
		    catch(NumberFormatException nfe) {
			if (field == null || field.trim().length() == 0 || field.trim().equals("NaN")) {
			    obj = null;
			} else {
			    renderer = getColumnRenderer(cidx, true, false);
			    synchronized(this) {
				try {
				    if (
					renderer != null &&
					renderer instanceof YoixJTableDateRenderer &&
					(csdf = ((YoixJTableDateRenderer)renderer).getSimpleDateInputFormat()) != null
					) {
					obj = csdf.parse(field);
				    } else {
					throw new java.text.ParseException("no renderer YoixSimpleDateFormat", 0);
				    }
				}
				catch(java.text.ParseException pe) {
				    try {
					obj = sdf.parse(field);
				    }
				    catch(java.text.ParseException pe2) {
					if (renderer == null || (obj = renderer.getSubstitute(field)) == null)
					    VM.abort(BADVALUE, errname, new String[] {field});
				    }
				}
			    }
			}
		    }
		}
		break;

	    case YOIX_DOUBLE_TYPE:
		renderer = getColumnRenderer(cidx, true, false);
		try {
		    obj = renderer.getNumberRenderer().getNumberInputFormat().parse(field);
		    if (obj instanceof Long)
			obj = new Double(((Long)obj).doubleValue());
		}
		catch(java.text.ParseException pe) {
		    try {
			obj = Double.valueOf(field);
		    }
		    catch(NumberFormatException nfe) {
			if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
			    if (field == null || field.trim().length() == 0)
				obj = null;
			    else VM.abort(BADVALUE, errname, new String[] {field});
			}
		    }
		}
		break;

	    case YOIX_HISTOGRAM_TYPE:
		try {
		    obj = new YoixJTableHistogram(field);
		}
		catch(NumberFormatException nfe) {
		    VM.abort(BADVALUE, errname, new String[] {field});
		}
		break;

	    case YOIX_ICON_TYPE:
		obj = new YoixJTableIcon(field);
		break;

	    case YOIX_INTEGER_TYPE:
		renderer = getColumnRenderer(cidx, true, false);
		try {
		    if (renderer != null) {
			obj = renderer.getNumberRenderer().getNumberInputFormat().parse(field);
			if (obj instanceof Long)
			    obj = new Integer(((Long)obj).intValue());
		    } else throw new java.text.ParseException("no renderer", 0);
		}
		catch(java.text.ParseException pe) {
		    try {
			obj = Integer.valueOf(field);
		    }
		    catch(NumberFormatException nfe) {
			if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
			    if (field == null || field.trim().length() == 0)
				obj = null;
			    else VM.abort(BADVALUE, errname, new String[] {field});
			}
		    }
		}
		break;

	    case YOIX_MONEY_TYPE:
		renderer = getColumnRenderer(cidx, true, false);
		try {
		    obj = new YoixJTableMoney(field, renderer.getNumberRenderer().getNumberInputFormat());
		}
		catch(java.text.ParseException pe) {
		    if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
			if (field == null || field.trim().length() == 0)
			    obj = null;
			else VM.abort(BADVALUE, errname, new String[] {field});
		    }
		}
		break;

	    case YOIX_OBJECT_TYPE:
		if ((tcm = getColumnModel()) != null) {
		    if ((tblcol = (YoixSwingTableColumn)tcm.getColumn(cidx)) != null) {
			obj = new YoixJTableObject(
			    field,
			    tblcol.getPickTableObject(),
			    tblcol.getPickSortObject()
			    );
		    } else obj = new YoixJTableObject(field);
		} else obj = new YoixJTableObject(field);
		break;

	    case YOIX_PERCENT_TYPE:
		renderer = getColumnRenderer(cidx, true, false);
		try {
		    obj = new YoixJTablePercent(field, renderer.getNumberRenderer().getNumberInputFormat());
		}
		catch(java.text.ParseException pe) {
		    if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
			if (field == null || field.trim().length() == 0)
			    obj = null;
			else VM.abort(BADVALUE, errname, new String[] {field});
		    }
		}
		break;

	    case YOIX_STRING_TYPE:
		obj = field;
		break;

	    case YOIX_TEXT_TYPE:
		renderer = getColumnRenderer(cidx, true, false);
		obj = new YoixJTableText(field, renderer.getFormat());
		break;

	    case YOIX_TIMER_TYPE:
		renderer = getColumnRenderer(cidx, true, false);
		try {
		    obj = new YoixJTableTimer(field, renderer);
		}
		catch(NumberFormatException nfe) {
		    if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
			if (field == null || field.trim().length() == 0)
			    obj = null;
			else VM.abort(BADVALUE, errname, new String[] {field});
		    }
		}
		break;

	    default:
		VM.abort(TYPECHECK, errname, new String[] {field});
		break;
	    }
	    return(obj);
	}


	private Object[][]
	processStringValues(String strvals[][], int rcnt, int ccnt, String errname) {

	    Object  objects[][];
	    Object  obj;
	    String  field;
	    int     typesnap[] = types;
	    int     typlen;
	    int     i;
	    int     j;

	    objects = new Object[rcnt][ccnt];
	    typlen = (typesnap == null) ? -1 : typesnap.length;

	    for (j = 0; j < ccnt; j++) {
		for (i = 0; i < rcnt; i++) {
		    field = strvals[i][j];
		    obj = null;
		    if (j < typlen) {
			obj = processField(field, typesnap[j], j, errname + ": " + N_VALUES + "["+i+"]["+j+"]");
			objects[i][j] = obj;
		    } else objects[i][j] = field;
		}
	    }
	    return(objects);
	}


	private Object[][]
	processText(String text, int padsize) {

	    BufferedReader  reader = null;
	    ArrayList       rows = new ArrayList();
	    String          line;
	    String          nmsnap[] = columnNames;
	    Object          newvalues[][] = values;
	    Object          vals[];
	    boolean         padding = false;
	    int             ccnt = -1;
	    int             rcnt;
	    int             pd;
	    int             rw;

	    try {
		reader = new BufferedReader(new StringReader(text));
		if (inputfilter == null) {
		    if (input_delimiter.length() == 1)
			ccnt = separateText(reader, input_delimiter.charAt(0), rows);
		    else ccnt = separateText(reader, input_delimiter, rows);
		} else ccnt = separateText(reader, inputfilter, rows);
	    }
	    catch(IOException e) {
		VM.caughtException(e);
	    }
	    catch(RuntimeException e) {
		VM.caughtException(e);
	    }
	    finally {
		try {
		    reader.close();
		}
		catch(IOException e) {
		    VM.caughtException(e);
		}
	    }

	    if (ccnt >= 0) {
		if (padsize > 0 && ccnt < padsize) {
		    padding = true;
		    newvalues = new Object[rcnt = rows.size()][padsize];
		} else newvalues = new Object[rcnt = rows.size()][ccnt];
		for (rw = 0; rw < rcnt; rw++) {
		    vals = (Object[])(rows.get(rw));
		    if (vals.length < ccnt) {
			if (quiet) {
			    System.arraycopy(vals, 0, newvalues[rw], 0, vals.length);
			    for (pd=vals.length; pd<ccnt; pd++)
				newvalues[rw][pd] = null;
			} else VM.abort(BADVALUE, N_VALUES, rw, new String[] {vals.length + " != " + ccnt});
		    } else System.arraycopy(vals, 0, newvalues[rw], 0, ccnt);
		    if (padding) {
			for (pd = ccnt; pd < padsize; pd++)
			    newvalues[rw][pd] = null;
		    }
		}
	    } else newvalues = null;

	    return(newvalues);
	}


	private Object[]
	processColumn(YoixObject yarr, int index, int type, String errname) {

	    YoixJTableCellRenderer  renderer;
	    YoixSimpleDateFormat    csdf;
	    YoixObject              yobj;
	    YoixObject              yobj2;
	    Object                  result[] = null;
	    Object                  object = null;
	    String                  errtype = null;
	    String                  field;
	    int                     aof;
	    int                     asz;
	    int                     aps;

	    // assumes we already text for isArray()
	    aof = yarr.offset();
	    asz = yarr.sizeof();

	    result = new Object[asz];
	    for (aps = 0; aps < asz; aps++, aof++) {
		yobj = yarr.get(aof, false);
		switch (type) {
		case YOIX_BOOLEAN_TYPE:
		    if (yobj.isInteger()) {
			object = yobj.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
		    } else if (yobj.isString()) {
			if (yobj.notNull()) {
			    field = yobj.stringValue().trim();
			    if ("1".equals(field) ||
				"yes".equalsIgnoreCase(field) ||
				"true".equalsIgnoreCase(field))
				object = Boolean.TRUE;
			    else
				object = Boolean.FALSE;
			} else object = Boolean.FALSE;
		    } else errtype = "BOOLEAN_TYPE";
		    break;
		case YOIX_DATE_TYPE:
		    if (yobj.isNumber()) {
			object = new Date((long)(1000.0*yobj.doubleValue()));
		    } else if (yobj.isString()) {
			field = yobj.stringValue();
			renderer = getColumnRenderer(index, true, false);
			try {
			    if (
				renderer != null &&
				renderer instanceof YoixJTableDateRenderer &&
				(csdf = ((YoixJTableDateRenderer)renderer).getSimpleDateInputFormat()) != null
				) {
				object = csdf.parse(field);
			    } else {
				throw new java.text.ParseException("no renderer YoixSimpleDateFormat", 0);
			    }
			}
			catch(java.text.ParseException pe) {
			    try {
				synchronized(this) {
				    object = sdf.parse(field);
				}
			    }
			    catch(java.text.ParseException pe2) {
				if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
				    if (field != null && field.trim().length() != 0)
					errtype = "DATE_TYPE";
				}
			    }
			}
		    } else errtype = "DATE_TYPE";
		    break;
		case YOIX_DOUBLE_TYPE:
		    if (yobj.isNumber()) {
			object = new Double(yobj.doubleValue());
		    } else if (yobj.isString() && yobj.notNull()) {
			field = yobj.stringValue();
			renderer = getColumnRenderer(index, true, false);
			try {
			    if (renderer != null) {
				object = renderer.getNumberRenderer().getNumberInputFormat().parse(field);
				if (object instanceof Long)
				    object = new Double(((Long)object).doubleValue());
			    } else throw new java.text.ParseException("no renderer", 0);
			}
			catch(java.text.ParseException pe) {
			    try {
				object = Double.valueOf(field);
			    }
			    catch(NumberFormatException nfe) {
				if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
				    if (field != null && field.trim().length() != 0)
					errtype = "DOUBLE_TYPE";
				}
			    }
			}
		    } else errtype = "DOUBLE_TYPE";
		    break;
		case YOIX_HISTOGRAM_TYPE:
		    if (yobj.isNumber()) {
			object = new YoixJTableHistogram(yobj.doubleValue());
		    } else if (yobj.isString() && yobj.notNull()) {
			try {
			    object = new YoixJTableHistogram(yobj.stringValue());
			}
			catch(NumberFormatException nfe) {
			    errtype = "HISTOGRAM_TYPE";
			}
		    } else errtype = "HISTOGRAM_TYPE";
		    break;
		case YOIX_ICON_TYPE:
		    if (yobj.isImage() && yobj.notNull()) {
			Image   image;
			String  source;
			String  desc;
			if ((image = YoixMake.javaImage(yobj)) != null) {
			    desc = yobj.get(N_DESCRIPTION, false).stringValue();
			    if ((yobj2=yobj.get(N_SOURCE, false)).notNull() && yobj2.isString())
				source = yobj2.stringValue();
			    else if (desc != null)
				source = "<" + desc + ">";
			    else source = "<internal>";
			    object = new YoixJTableIcon(image, source, desc);
			} else object = new YoixJTableIcon("<unavailable>");
		    } else if (yobj.isString() && yobj.notNull()) {
			object = new YoixJTableIcon(yobj.stringValue());
		    } else errtype = "ICON_TYPE";
		    break;
		case YOIX_INTEGER_TYPE:
		    if (yobj.isNumber()) {
			object = new Integer(yobj.intValue());
		    } else if (yobj.isString() && yobj.notNull()) {
			field = yobj.stringValue();
			renderer = getColumnRenderer(index, true, false);
			try {
			    if (renderer != null) {
				object = renderer.getNumberRenderer().getNumberInputFormat().parse(field);
				if (object instanceof Long)
				    object = new Integer(((Long)object).intValue());
			    } else throw new java.text.ParseException("no renderer", 0);
			}
			catch(java.text.ParseException pe) {
			    try {
				object = Integer.valueOf(field);
			    }
			    catch(NumberFormatException nfe) {
				if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
				    if (field != null && field.trim().length() != 0)
					errtype = "INTEGER_TYPE";
				}
			    }
			}
		    } else errtype = "INTEGER_TYPE";
		    break;
		case YOIX_MONEY_TYPE:
		    if (yobj.isNumber()) {
			object = new YoixJTableMoney(yobj.doubleValue());
		    } else if (yobj.isString() && yobj.notNull()) {
			field = yobj.stringValue();
			renderer = getColumnRenderer(index, true, false);
			try {
			    object = new YoixJTableMoney(field, renderer.getNumberRenderer().getNumberInputFormat());
			}
			catch(java.text.ParseException pe) {
			    if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
				if (field != null && field.trim().length() != 0)
				    errtype = "MONEY_TYPE";
			    }
			}
		    } else errtype = "MONEY_TYPE";
		    break;
		case YOIX_OBJECT_TYPE:
		    if (yobj.isString()) {
			object = new YoixJTableObject(yobj.stringValue());
		    } else errtype = "OBJECT_TYPE";
		    break;
		case YOIX_PERCENT_TYPE:
		    if (yobj.isNumber()) {
			object = new YoixJTablePercent(yobj.doubleValue());
		    } else if (yobj.isString() && yobj.notNull()) {
			field = yobj.stringValue();
			renderer = getColumnRenderer(index, true, false);
			try {
			    object = new YoixJTablePercent(field, renderer.getNumberRenderer().getNumberInputFormat());
			}
			catch(java.text.ParseException pe) {
			    if (field != null && field.trim().length() != 0)
				errtype = "PERCENT_TYPE";
			}
		    } else errtype = "PERCENT_TYPE";
		    break;
		case YOIX_STRING_TYPE:
		    if (yobj.isString()) {
			object = new String(yobj.stringValue());
		    } else errtype = "STRING_TYPE";
		    break;
		case YOIX_TEXT_TYPE:
		    if (yobj.isString()) {
			object = new YoixJTableText(yobj.stringValue());
		    } else errtype = "TEXT_TYPE";
		    break;
		case YOIX_TIMER_TYPE:
		    if (yobj.isNumber()) {
			object = new YoixJTableTimer(yobj.doubleValue());
		    } else if (yobj.isString() && yobj.notNull()) {
			field = yobj.stringValue();
			renderer = getColumnRenderer(index, true, false);
			try {
			    object = new YoixJTableTimer(field, renderer);
			}
			catch(NumberFormatException nfe) {
			    if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
				if (field != null && field.trim().length() != 0)
				    errtype = "TIMER_TYPE";
			    }
			}
		    } else errtype = "TIMER_TYPE";
		    break;
		default:
		    VM.abort(INTERNALERROR);
		}
		if (errtype != null)
		    VM.abort(TYPECHECK, errname, aps, new String[] { "expected valid " + errtype });
		result[aps] = object;
	    }
	    return(result);
	}


	private Object[][]
	processValues(YoixObject yarr, int padsize) {

	    YoixJTableCellRenderer  renderer;
	    YoixSimpleDateFormat    csdf;
	    YoixObject              yobj;
	    YoixObject              yobj2;
	    YoixObject              yobj3;
	    Object                  result[][] = null;
	    Object                  object = null;
	    String                  errtype = null;
	    String                  field;
	    String                  nmsnap[] = columnNames;
	    int                     typesnap[] = types;
	    int                     typlen;
	    int                     aof;
	    int                     asz;
	    int                     aps;
	    int                     yof;
	    int                     ysz;
	    int                     yps;

	    typlen = (typesnap == null) ? -1 : typesnap.length;

	    // assumes we already text for isArray()
	    aof = yarr.offset();
	    asz = yarr.sizeof();

	    for (aps = 0; aps < asz; aps++, aof++) {
		yobj = yarr.get(aof, false);
		if (yobj.isArray()) {
		    yof = yobj.offset();
		    ysz = yobj.sizeof();
		    if (aps == 0) {
			if (padsize > 0 && ysz < padsize)
			    result = new Object[asz][padsize];
			else result = new Object[asz][ysz];
		    }
		    for (yps = 0; yps < ysz; yps++, yof++) {
			yobj2 = yobj.get(yof, false);

			if (yps < typlen) {
			    switch (typesnap[yps]) {
			    case YOIX_BOOLEAN_TYPE:
				if (yobj2.isInteger()) {
				    object = yobj2.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
				} else if (yobj2.isString()) {
				    if (yobj2.notNull()) {
					field = yobj2.stringValue().trim();
					if ("1".equals(field) ||
					    "yes".equalsIgnoreCase(field) ||
					    "true".equalsIgnoreCase(field))
					    object = Boolean.TRUE;
					else
					    object = Boolean.FALSE;
				    } else object = Boolean.FALSE;
				} else errtype = "BOOLEAN_TYPE";
				break;
			    case YOIX_DATE_TYPE:
				if (yobj2.isNumber()) {
				    object = new Date((long)(1000.0*yobj2.doubleValue()));
				} else if (yobj2.isString()) {
				    field = yobj2.stringValue();
				    renderer = getColumnRenderer(yps, true, false);
				    synchronized(this) {
					try {
					    if (
						renderer != null &&
						renderer instanceof YoixJTableDateRenderer &&
						(csdf = ((YoixJTableDateRenderer)renderer).getSimpleDateInputFormat()) != null
						) {
						object = csdf.parse(field);
					    } else {
						throw new java.text.ParseException("no renderer YoixSimpleDateFormat", 0);
					    }
					}
					catch(java.text.ParseException pe) {
					    try {
						object = sdf.parse(field);
					    }
					    catch(java.text.ParseException ex) {
						if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
						    if (field != null && field.trim().length() != 0)
							errtype = "DATE_TYPE";
						}
					    }
					}
				    }
				} else errtype = "DATE_TYPE";
				break;
			    case YOIX_DOUBLE_TYPE:
				if (yobj2.isNumber()) {
				    object = new Double(yobj2.doubleValue());
				} else if (yobj2.isString() && yobj2.notNull()) {
				    field = yobj2.stringValue();
				    renderer = getColumnRenderer(yps, true, false);
				    try {
					if (renderer != null) {
					    object = renderer.getNumberRenderer().getNumberInputFormat().parse(field);
					    if (object instanceof Long)
						object = new Double(((Long)object).doubleValue());
					} else throw new java.text.ParseException("no renderer", 0);
				    }
				    catch(java.text.ParseException pe) {
					try {
					    object = Double.valueOf(field);
					}
					catch(NumberFormatException nfe) {
					    if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
						if (field != null && field.trim().length() != 0)
						    errtype = "DOUBLE_TYPE";
					    }
					}
				    }
				} else errtype = "DOUBLE_TYPE";
				break;
			    case YOIX_HISTOGRAM_TYPE:
				if (yobj2.isNumber()) {
				    object = new YoixJTableHistogram(yobj2.doubleValue());
				} else if (yobj2.isString() && yobj2.notNull()) {
				    try {
					object = new YoixJTableHistogram(yobj2.stringValue());
				    }
				    catch(NumberFormatException nfe) {
					errtype = "HISTOGRAM_TYPE";
				    }
				} else errtype = "HISTOGRAM_TYPE";
				break;
			    case YOIX_ICON_TYPE:
				if (yobj2.isImage() && yobj2.notNull()) {
				    Image   image;
				    String  source;
				    String  desc;
				    if ((image = YoixMake.javaImage(yobj2)) != null) {
					desc = yobj2.get(N_DESCRIPTION, false).stringValue();
					if ((yobj3=yobj2.get(N_SOURCE, false)).notNull() && yobj3.isString())
					    source = yobj3.stringValue();
					else if (desc != null)
					    source = "<" + desc + ">";
					else source = "<internal>";
					object = new YoixJTableIcon(image, source, desc);
				    } else object = new YoixJTableIcon("<unavailable>");
				} else if (yobj2.isString() && yobj2.notNull()) {
				    object = new YoixJTableIcon(yobj2.stringValue());
				} else errtype = "ICON_TYPE";
				break;
			    case YOIX_INTEGER_TYPE:
				if (yobj2.isNumber()) {
				    object = new Integer(yobj2.intValue());
				} else if (yobj2.isString() && yobj2.notNull()) {
				    field = yobj2.stringValue();
				    renderer = getColumnRenderer(yps, true, false);
				    try {
					if (renderer != null) {
					    object = renderer.getNumberRenderer().getNumberInputFormat().parse(field);
					    if (object instanceof Long)
						object = new Integer(((Long)object).intValue());
					} else throw new java.text.ParseException("no renderer", 0);
				    }
				    catch(java.text.ParseException pe) {
					try {
					    object = Integer.valueOf(field);
					}
					catch(NumberFormatException nfe) {
					    if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
						if (field != null && field.trim().length() != 0)
						    errtype = "INTEGER_TYPE";
					    }
					}
				    }
				} else errtype = "INTEGER_TYPE";
				break;
			    case YOIX_MONEY_TYPE:
				if (yobj2.isNumber()) {
				    object = new YoixJTableMoney(yobj2.doubleValue());
				} else if (yobj2.isString() && yobj2.notNull()) {
				    field = yobj2.stringValue();
				    renderer = getColumnRenderer(yps, true, false);
				    try {
					object = new YoixJTableMoney(field, renderer.getNumberRenderer().getNumberInputFormat());
				    }
				    catch(java.text.ParseException pe) {
					if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
					    if (field != null && field.trim().length() != 0)
						errtype = "MONEY_TYPE";
					}
				    }
				} else errtype = "MONEY_TYPE";
				break;
			    case YOIX_OBJECT_TYPE:
				if (yobj2.isString()) {
				    object = new YoixJTableObject(yobj2.stringValue());
				} else errtype = "OBJECT_TYPE";
				break;
			    case YOIX_PERCENT_TYPE:
				if (yobj2.isNumber()) {
				    object = new YoixJTablePercent(yobj2.doubleValue());
				} else if (yobj2.isString() && yobj2.notNull()) {
				    field = yobj2.stringValue();
				    renderer = getColumnRenderer(yps, true, false);
				    try {
					object = new YoixJTablePercent(field, renderer.getNumberRenderer().getNumberInputFormat());
				    }
				    catch(java.text.ParseException pe) {
					if (field != null && field.trim().length() != 0)
					    errtype = "PERCENT_TYPE";
				    }
				} else errtype = "PERCENT_TYPE";
				break;
			    case YOIX_STRING_TYPE:
				if (yobj2.isString()) {
				    object = new String(yobj2.stringValue());
				} else errtype = "STRING_TYPE";
				break;
			    case YOIX_TEXT_TYPE:
				if (yobj2.isString()) {
				    object = new YoixJTableText(yobj2.stringValue());
				} else errtype = "TEXT_TYPE";
				break;
			    case YOIX_TIMER_TYPE:
				if (yobj2.isNumber()) {
				    object = new YoixJTableTimer(yobj2.doubleValue());
				} else if (yobj2.isString() && yobj2.notNull()) {
				    field = yobj2.stringValue();
				    renderer = getColumnRenderer(yps, true, false);
				    try {
					object = new YoixJTableTimer(field, renderer);
				    }
				    catch(NumberFormatException nfe) {
					if (renderer == null || (object = renderer.getSubstitute(field)) == null) {
					    if (field != null && field.trim().length() != 0)
						errtype = "TIMER_TYPE";
					}
				    }
				} else errtype = "TIMER_TYPE";
				break;
			    default:
				VM.abort(INTERNALERROR);
			    }
			} else if (yobj2.isString()) {
			    object = new String(yobj.stringValue());
			} else errtype = "STRING_TYPE";
			if (errtype != null)
			    VM.abort(TYPECHECK, N_VALUES, aps, yps, new String[] { "expected valid " + errtype });
			result[aps][yps] = object;
		    }
		    if (padsize > 0) {
			for (; yps<padsize; yps++)
			    result[aps][yps] = null;
		    }
		} else VM.abort(TYPECHECK, N_VALUES, aps, new String[] { "Array expected" });
	    }
	    return(result);
	}


	private int
	separateText(BufferedReader reader, char delim, ArrayList rows)

	    throws IOException

	{

	    ArrayList  buffer;
	    String     field;
	    String     line;
	    int        row;
	    int        length;
	    int        typlen;
	    int        first;
	    int        next;
	    int        count;
	    int        ccnt;

	    buffer = new ArrayList();
	    ccnt = -1;
	    typlen = (types == null) ? -1 : types.length;

	    while ((line = reader.readLine()) != null) {
		length = line.length();
		buffer.clear();
		row = rows.size();
		for (count = 0, next = 0; next <= length; count++, next++) {
		    first = next;
		    if ((next = line.indexOf(delim, first)) < 0) {
			field = line.substring(first);
			next = length;
		    } else field = line.substring(first, next);

		    if (count < typlen)
			buffer.add(getColumnObject(field, row, count));
		    else buffer.add(field);
		}

		if (ccnt < count)
		    ccnt = count;
		rows.add(buffer.toArray());
	    }
	    return(ccnt);
	}


	private int
	separateText(BufferedReader reader, String delims, ArrayList rows)

	    throws IOException

	{

	    ArrayList  buffer;
	    String     field;
	    String     line;
	    int        row;
	    int        length;
	    int        typlen;
	    int        first;
	    int        next;
	    int        count;
	    int        ccnt;
	    int        dlen;

	    buffer = new ArrayList();
	    ccnt = -1;
	    typlen = (types == null) ? -1 : types.length;

	    if (delims == null || (dlen = delims.length()) == 0)
		dlen = 1;

	    while ((line = reader.readLine()) != null) {
		length = line.length();
		buffer.clear();
		row = rows.size();
		for (count = 0, next = 0; next <= length; count++, next+=dlen) {
		    first = next;
		    if ((next = line.indexOf(delims, first)) < 0) {
			field = line.substring(first);
			next = length;
		    } else field = line.substring(first, next);

		    if (count < typlen)
			buffer.add(getColumnObject(field, row, count));
		    else buffer.add(field);
		}

		if (ccnt < count)
		    ccnt = count;
		rows.add(buffer.toArray());
	    }
	    return(ccnt);
	}


	//
	// this inputfilter model is kludgey, but retained for backward compatibility
	//
	private int
	separateText(BufferedReader reader, Object inputfilter[], ArrayList rows)

	    throws IOException

	{

	    ArrayList  buffer;
	    String     line;
	    String     prefix;
	    String     replace;
	    String     field;
	    int        ccnt;
	    int        count;
	    int        index;
	    int        row;
	    int        rowidx;
	    int        typlen;
	    int        m;
	    int        n;
	    boolean    newrow;

	    buffer = new ArrayList();
	    ccnt = -1;
	    typlen = (types == null) ? -1 : types.length;
	    row = rows.size();
	    newrow = false;

	    // rowidx marks end of row (as does an empty line)
	    // note: rowidx will only work when the associated prefix is not skipped
	    //       in the input data
	    // Moreoever, it should also be last in the inputfilter otherwise
	    // otherwise a record's data will cross table rows.
	    rowidx = 0;
	    for (n = 0; (n+2) < inputfilter.length; n += 3) {
		index = ((Integer)inputfilter[n + 1]).intValue();
		if (index > rowidx)
		    rowidx = index;
	    }
	    rowidx--;

	    while ((line = reader.readLine()) != null) {
		if (line.length() > 0) {
		    for (n = 0; (n+2) < inputfilter.length; n += 3) {
			if ((prefix = (String)inputfilter[n]) != null) {
			    if (line.startsWith(prefix)) {
				index = ((Integer)inputfilter[n + 1]).intValue();
				if (index > 0) {
				    if (index > buffer.size())
					buffer.ensureCapacity(index);
				    replace = (String)inputfilter[n + 2];
				    field = line.substring(prefix.length());
				    if (replace != null) {
					for (m = 0; m < replace.length() - 1; m += 2) {
					    field = field.replace(
						replace.charAt(m),
						replace.charAt(m+1)
						);
					}
				    }

				    index--;

				    for (n = buffer.size(); n <= index; n++)
					buffer.add(n, null);
				    if (index < typlen)
					buffer.set(index, getColumnObject(field, row, index));
				    else buffer.set(index, field);

				    if (index == rowidx)
					newrow = true;
				}
				break;
			    }
			}
		    }
		} else newrow = true;

		if (newrow) {
		    newrow = false;
		    count = buffer.size();
		    if (ccnt < count)
			ccnt = count;
		    if (count > 0)
			rows.add(buffer.toArray());
		    buffer.clear();
		    row = rows.size();
		}
	    }

	    count = buffer.size();

	    if (count > 0) {
		if (ccnt < count)
		    ccnt = count;
		rows.add(buffer.toArray());
	    }

	    return(ccnt);
	}


	private Object
	getColumnObject(String field, int row, int column) {

	    YoixJTableCellRenderer  renderer;
	    YoixSwingTableColumn    tblcol;
	    TableColumnModel        tcm;
	    YoixSimpleDateFormat    csdf;
	    String                  trimmed;
	    Object                  obj = null;

	    if (types != null && column < types.length) {
		switch (types[column]) {
		case YOIX_BOOLEAN_TYPE:
		    if (field != null) {
			trimmed = field.trim();
			if ("1".equals(trimmed) || "yes".equalsIgnoreCase(trimmed) || "true".equalsIgnoreCase(trimmed))
			    obj = Boolean.TRUE;
			else obj = Boolean.FALSE;
		    } else obj = Boolean.FALSE;
		    break;

		case YOIX_DATE_TYPE:
		    synchronized(this) {
			try {
			    obj = new Double(field);
			    if (obj != null && ((Double)obj).isNaN())
				obj = null;
			    else obj = new Date(1000 * ((Double)obj).longValue());
			}
			catch(NumberFormatException nfe) {
			    if (field == null || field.trim().length() == 0 || field.trim().equals("NaN"))
				obj = null;
			    else {
				renderer = getColumnRenderer(column, true, false);
				try {
				    if (
					renderer != null &&
					renderer instanceof YoixJTableDateRenderer &&
					(csdf = ((YoixJTableDateRenderer)renderer).getSimpleDateInputFormat()) != null
					) {
					obj = csdf.parse(field);
				    } else {
					throw new java.text.ParseException("no renderer YoixSimpleDateFormat", 0);
				    }
				}
				catch(java.text.ParseException pe) {
				    csdf = sdf;
				    try {
					obj = csdf.parse(field);
				    }
				    catch(java.text.ParseException pe2) {
					if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
					    if (quiet || field == null || field.trim().length() == 0)
						obj = null;
					    else VM.abort(BADVALUE, N_VALUES, row, column, new String[] {field});
					}
				    }
				}
			    }
			}
		    }
		    break;

		case YOIX_DOUBLE_TYPE:
		    renderer = getColumnRenderer(column, true, false);
		    try {
			obj = renderer.getNumberRenderer().getNumberInputFormat().parse(field);
			if (obj instanceof Long)
			    obj = new Double(((Long)obj).doubleValue());
		    }
		    catch(java.text.ParseException pe) {
			try {
			    obj = Double.valueOf(field);
			}
			catch(NumberFormatException nfe) {
			    if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
				if (quiet || field == null || field.trim().length() == 0)
				    obj = null;
				else VM.abort(BADVALUE, N_VALUES, row, column, new String[] {field});
			    }
			}
		    }
		    break;

		case YOIX_HISTOGRAM_TYPE:
		    try {
			obj = new YoixJTableHistogram(field);
		    }
		    catch(NumberFormatException nfe) {
			if (quiet || field == null || field.trim().length() == 0)
			    obj = null;
			else VM.abort(BADVALUE, N_VALUES, row, column, new String[] {field});
		    }
		    break;

		case YOIX_ICON_TYPE:
		    obj = new YoixJTableIcon(field);
		    break;

		case YOIX_INTEGER_TYPE:
		    renderer = getColumnRenderer(column, true, false);
		    try {
			if (renderer != null) {
			    obj = renderer.getNumberRenderer().getNumberInputFormat().parse(field);
			    if (obj instanceof Long)
				obj = new Integer(((Long)obj).intValue());
			} else throw new java.text.ParseException("no renderer", 0);
		    }
		    catch(java.text.ParseException pe) {
			try {
			    obj = Integer.valueOf(field);
			}
			catch(NumberFormatException nfe) {
			    if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
				if (quiet || field == null || field.trim().length() == 0)
				    obj = null;
				else VM.abort(BADVALUE, N_VALUES, row, column, new String[] {field});
			    }
			}
		    }
		    break;

		case YOIX_MONEY_TYPE:
		    renderer = getColumnRenderer(column, true, false);
		    try {
			obj = new YoixJTableMoney(field, renderer.getNumberRenderer().getNumberInputFormat());
		    }
		    catch(java.text.ParseException pe) {
			if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
			    if (quiet || field == null || field.trim().length() == 0)
				obj = null;
			    else VM.abort(BADVALUE, N_VALUES, row, column, new String[] {field});
			}
		    }
		    break;

		case YOIX_OBJECT_TYPE:
		    if ((tcm = getColumnModel()) != null) {
			if ((tblcol = (YoixSwingTableColumn)tcm.getColumn(column)) != null) {
			    obj = new YoixJTableObject(
				field,
				tblcol.getPickTableObject(),
				tblcol.getPickSortObject()
				);
			} else obj = new YoixJTableObject(field);
		    } else obj = new YoixJTableObject(field);
		    break;

		case YOIX_PERCENT_TYPE:
		    renderer = getColumnRenderer(column, true, false);
		    try {
			obj = new YoixJTablePercent(field, renderer.getNumberRenderer().getNumberInputFormat());
		    }
		    catch(java.text.ParseException pe) {
			if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
			    if (quiet || field == null || field.trim().length() == 0)
				obj = null;
			    else VM.abort(BADVALUE, N_VALUES, row, column, new String[] {field});
			}
		    }
		    break;

		case YOIX_STRING_TYPE:
		    obj = field;
		    break;

		case YOIX_TEXT_TYPE:
		    renderer = getColumnRenderer(column, true, false);
		    obj = new YoixJTableText(field, renderer.getFormat());
		    break;

		case YOIX_TIMER_TYPE:
		    renderer = getColumnRenderer(column, true, false);
		    try {
			obj = new YoixJTableTimer(field, renderer);
		    }
		    catch(NumberFormatException nfe) {
			if (renderer == null || (obj = renderer.getSubstitute(field)) == null) {
			    if (quiet || field == null || field.trim().length() == 0)
				obj = null;
			    else VM.abort(BADVALUE, N_VALUES, row, column, new String[] {field});
			}
		    }
		    break;

		default:
		    VM.abort(TYPECHECK, N_VALUES, row, column, new String[] {field});
		    break;
		}
	    }
	    return(obj);
	}


	private String[]
	separateHeader(String text) {

	    StringTokenizer  st;
	    String[]         headers = null;
	    int              n;

	    if (
		inputfilter == null &&
		text != null && text.length() > 0 &&
		input_delimiter != null && input_delimiter.length() == 1
		) {
		st = new StringTokenizer(text, input_delimiter, false);
		headers = new String[st.countTokens()];
		n = 0;
		while (st.hasMoreTokens())
		    headers[n++] = st.nextToken();
	    }

	    return(headers);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    protected class YoixJTableHistogram extends Number
	implements NaN
    {

	private YoixObject  yoixvalue = null;
	private double      value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableHistogram() {

	    value = 0;
	}


	public
	YoixJTableHistogram(double value) {

	    this.value = value;
	}


	public
	YoixJTableHistogram(String str)

	    throws NumberFormatException

	{

	    value = Double.valueOf(str).doubleValue();
	}

	///////////////////////////////////
	//
	// NaN interface methods
	//
	///////////////////////////////////

	public final boolean
	isNaN() {

	    return("NaN".equals(""+value));
	}

	///////////////////////////////////
	//
	// YoixJTableHistogram Methods
	//
	///////////////////////////////////

	public final double
	doubleValue() {

	    return(value);
	}


	public final boolean
	equals(Object obj) {

	    return((obj instanceof YoixJTableHistogram)
		   && (Double.doubleToLongBits(((YoixJTableHistogram)obj).value) ==
		       Double.doubleToLongBits(value))
		);
	}


	public final float
	floatValue() {

	    return((float)value);
	}


	public final int
	intValue() {

	    return((int)value);
	}


	public final  long
	longValue() {

	    return((long)value);
	}


	public final String
	toString() {

	    return("" + value);
	}


	public final YoixObject
	yoixValue() {

	    if (yoixvalue == null)
		yoixvalue = YoixObject.newDouble(doubleValue());
	    return(yoixvalue);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableMoney extends Number
	implements NaN
    {

	private YoixObject   yoixvalue = null;
	private double       value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableMoney() {

	    value = 0;
	}


	public
	YoixJTableMoney(double value) {

	    this.value = value;
	}


	public
	YoixJTableMoney(String str, NumberFormat nf)

	    throws java.text.ParseException

	{
	    try {
		if (nf != null)
		    value = (nf.parse(str)).doubleValue();
		else throw new java.text.ParseException("no NumberFormat", 0);
	    }
	    catch(java.text.ParseException pe) {
		try {
		    value = Double.valueOf(str).doubleValue();
		}
		catch(NumberFormatException nfe) {
		    throw new java.text.ParseException("number format: " + nfe.getMessage(), 0);
		}
	    }
	}

	///////////////////////////////////
	//
	// NaN interface methods
	//
	///////////////////////////////////

	public final boolean
	isNaN() {

	    return("NaN".equals(""+value));
	}

	///////////////////////////////////
	//
	// YoixJTableMoney Methods
	//
	///////////////////////////////////

	public final double
	doubleValue() {

	    return(value);
	}


	public final boolean
	equals(Object obj) {

	    return((obj instanceof YoixJTableMoney)
		   && (Double.doubleToLongBits(((YoixJTableMoney)obj).value) ==
		       Double.doubleToLongBits(value))
		);
	}


	public final float
	floatValue() {

	    return((float)value);
	}


	public final int
	intValue() {

	    return((int)value);
	}


	public final  long
	longValue() {

	    return((long)value);
	}


	public final String
	toString() {

	    return("" + value);
	}


	public final YoixObject
	yoixValue() {

	    if (yoixvalue == null)
		yoixvalue = YoixObject.newDouble(doubleValue());
	    return(yoixvalue);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableObject

    {

	//
	// Basically just a string that's displayed in the table along with
	// an arbitrary Object that's used when we sort. Name of the class
	// is a little deceptive, because the function, if there is one, is
	// only applied once by the constructor and currently isn't saved
	// here.
	// 

	private YoixObject  yoixvalue = null;
	private Object      sortobject;
	private String      value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableObject(String field) {

	    this(field, null, null);
	}

	public
	YoixJTableObject(String field, YoixObject picktable, YoixObject picksort) {

	    YoixObject  obj;

	    value = (field != null) ? field : "";

	    if (picktable != null && picktable.callable(1)) {
		if ((obj = YoixMisc.call(picktable, YoixObject.newString(value), parent.getContext())) != null) {
		    if (obj.notNull()) {
			if (obj.isString() == false)
			    value = obj.toString().trim();
			else value = obj.stringValue();
		    } else value = "";
		}
	    }

	    if (picksort != null && picksort.callable(1)) {
		if ((obj = YoixMisc.call(picksort, YoixObject.newString(value), parent.getContext())) != null)
		    sortobject = YoixMake.javaObject(obj);
		else sortobject = value;
	    } else sortobject = value;
	}

	///////////////////////////////////
	//
	// YoixJTableObject Methods
	//
	///////////////////////////////////

	final Object
	pickSortObject() {

	    return(sortobject);
	}


	final String
	stringValue() {

	    return(value);
	}


	public final String
	toString() {

	    return(value);
	}


	public final YoixObject
	yoixValue() {

	    if (yoixvalue == null)
		yoixvalue = YoixObject.newString(value);
	    return(yoixvalue);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTablePercent extends Number
	implements NaN
    {

	private YoixObject  yoixvalue = null;
	private double      value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTablePercent() {

	    value = 0;
	}


	public
	YoixJTablePercent(double value) {

	    this.value = value;
	}


	public
	YoixJTablePercent(String str, NumberFormat nf)

	    throws java.text.ParseException

	{

	    if (str != null) {
		str = str.trim();
		try {
		    if (nf != null)
			value = (nf.parse(str)).doubleValue();
		    else throw new java.text.ParseException("no NumberFormat", 0);
		}
		catch(java.text.ParseException pe) {
		    try {
			if (str.endsWith("%"))
			    value = Double.valueOf(str.substring(0, str.length()-1)).doubleValue() / 100;
			else value = Double.valueOf(str).doubleValue();
		    }
		    catch(NumberFormatException nfe) {
			throw new java.text.ParseException("number format: " + nfe.getMessage(), 0);
		    }
		}
	    } else value = 0;
	}

	///////////////////////////////////
	//
	// NaN interface methods
	//
	///////////////////////////////////

	public final boolean
	isNaN() {

	    return("NaN".equals(""+value));
	}

	///////////////////////////////////
	//
	// YoixJTablePercent Methods
	//
	///////////////////////////////////

	public final double
	doubleValue() {

	    return(value);
	}


	public final boolean
	equals(Object obj) {

	    return((obj instanceof YoixJTablePercent)
		   && (Double.doubleToLongBits(((YoixJTablePercent)obj).value) ==
		       Double.doubleToLongBits(value))
		);
	}


	public final float
	floatValue() {

	    return((float)value);
	}


	public final int
	intValue() {

	    return((int)value);
	}


	public final long
	longValue() {

	    return((long)value);
	}


	public final String
	toString() {

	    return("" + value);
	}


	public final YoixObject
	yoixValue() {

	    if (yoixvalue == null)
		yoixvalue = YoixObject.newDouble(doubleValue());
	    return(yoixvalue);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableText
    {

	public final static int PLAIN_TYPE = 1;
	public final static int HTML_TYPE = 2;
	public final static int RTF_TYPE = 3;

	private YoixObject  yoixvalue = null;
	private String      value;
	private String      sortvalue = null;
	private String      format;
	private int         type;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableText() {

	    this(null, "plain");
	}


	public
	YoixJTableText(String value) {

	    this(value, "plain");
	}


	public
	YoixJTableText(String value, String format)
	{

	    if (value == null)
		value = sortvalue = "";
	    this.value = value;
	    if (format != null && format.toLowerCase().startsWith("h")) {
		format = "html";
		type = HTML_TYPE;
	    } else if (format != null && format.toLowerCase().startsWith("r")) {
		format = "rtf";
		type = RTF_TYPE;
	    } else {
		format = "plain";
		type = PLAIN_TYPE;
	    }
	    this.format = format;
	}

	///////////////////////////////////
	//
	// YoixJTableText Methods
	//
	///////////////////////////////////

	public final String
	getFormat() {

	    return(format);
	}

	public final String
	stringValue() {

	    return(value);
	}


	public final boolean
	equals(Object obj) {

	    return(obj instanceof YoixJTableText
		   && format.equals(((YoixJTableText)obj).getFormat())
		   && (value == ((YoixJTableText)obj).value ||
		       (value != null && value.equals(((YoixJTableText)obj).value)))
		);
	}


	public final String
	toSortString() {

	    StyledEditorKit  kit;
	    Document         doc;
	    StringReader     sr;

	    if (sortvalue == null) {
		// expensive, but we only need to do it once
		switch(type) {
		case HTML_TYPE:
		    kit = new HTMLEditorKit();
		    doc = kit.createDefaultDocument();
		    sr = new StringReader(value);
		    try {
			kit.read(sr, doc, 0);
			sortvalue = doc.getText(0, doc.getLength()).trim();
		    }
		    catch(IOException e) {
			VM.abort(EXCEPTION, "setText:" + e.getMessage()); // should never happen
		    }
		    catch(BadLocationException e) {
			VM.abort(EXCEPTION, "setText:" + e.getMessage()); // should never happen
		    }
		    break;
		case RTF_TYPE:
		    kit = new javax.swing.text.rtf.RTFEditorKit();
		    doc = kit.createDefaultDocument();
		    sr = new StringReader(value);
		    try {
			kit.read(sr, doc, 0);
			sortvalue = doc.getText(0, doc.getLength()).trim();
		    }
		    catch(IOException e) {
			VM.abort(EXCEPTION, "setText:" + e.getMessage()); // should never happen
		    }
		    catch(BadLocationException e) {
			VM.abort(EXCEPTION, "setText:" + e.getMessage()); // should never happen
		    }
		    break;
		case PLAIN_TYPE:
		default:
		    sortvalue = value.trim();
		}
	    }
	    return(sortvalue);
	}


	public final String
	toString() {

	    return(value);
	}


	public final YoixObject
	yoixValue() {

	    if (yoixvalue == null)
		yoixvalue = YoixObject.newString(value);
	    return(yoixvalue);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableTimer extends Number
	implements NaN
    {

	private double  value;
	private String  format = TIMER_FORMAT;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableTimer() {

	    value = 0;
	}


	public
	YoixJTableTimer(double value) {

	    this.value = value;
	}

	public
	YoixJTableTimer(String str, YoixJTableCellRenderer renderer)

	    throws NumberFormatException

	{
	    if (
		str == null
		|| str.trim().length() == 0
		|| (renderer != null && renderer.getSubstitute(str) != null)
	    ) {
		throw new NumberFormatException();
	    } else {
		try {
		    value = Double.valueOf(str).doubleValue();
		}
		catch(NumberFormatException nfe) {
		    value = YoixMiscTime.parseTimer(str);
		}
	    }
	}

	///////////////////////////////////
	//
	// NaN interface methods
	//
	///////////////////////////////////

	public final boolean
	isNaN() {

	    return("NaN".equals(""+value));
	}

	///////////////////////////////////
	//
	// YoixJTableTimer Methods
	//
	///////////////////////////////////

	public final double
	doubleValue() {

	    return(value);
	}


	public final boolean
	equals(Object obj) {

	    return((obj instanceof YoixJTableTimer)
		   && (Double.doubleToLongBits(((YoixJTableTimer)obj).value) ==
		       Double.doubleToLongBits(value))
		);
	}


	public final float
	floatValue() {

	    return((float)value);
	}


	public final String
	getFormat() {

	    return(format);
	}


	public final int
	intValue() {

	    return((int)value);
	}


	public final long
	longValue() {

	    return((long)value);
	}


	public final String
	toString() {

	    return(toString(this.format));
	}


	public final String
	toString(String format) {

	    return(YoixMiscTime.timerFormat(format, value));
	}


	public final void
	setFormat(String format) {

	    this.format = format;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    public interface NaN {
	public boolean isNaN();
    }

    protected interface YoixJTableCellRenderer {

	public YoixJTableCellRenderer makeCopy();

	public int getDefaultAlignment();
	public int getDefaultHorizontalAlignment();
	public void resetDefaultHorizontalAlignment();
	public void setHorizontalAlignment(int alignment);
	public void setDefaultHorizontalAlignment(int alignment);
	public String stringValue(Object value, boolean formatted);

	public int getRendererType();

	// formatting related
	public boolean getDecimalSeparatorAlwaysShown();
	public String getFormat();
	public int getGroupingSize();
	public boolean getGroupingUsed();
	public String getInputLocale();
	public String getRendererLocale();
	public int getMaximumFractionDigits();
	public int getMaximumIntegerDigits();
	public int getMinimumFractionDigits();
	public int getMinimumIntegerDigits();
	public int getMultiplier();
	public double getOverflow();
	public double getUnderflow();
	public String getNegativePrefix();
	public String getNegativeSuffix();
	public NumberFormat getNumberInputFormat() throws java.text.ParseException;
	public YoixJTableNumberRenderer getNumberRenderer() throws java.text.ParseException;
	public boolean getParseIntegerOnly();
	public String getPositivePrefix();
	public String getPositiveSuffix();
	public String[] getLowSubstitute();
	public String[] getHighSubstitute();
	public Object getSubstitute(String value);
	public String getReverseSubstitute(Object value);
	public boolean getZeroNotShown();
	public String getInputFormat();
	public void setDecimalSeparatorAlwaysShown(boolean value);
	public void setFormat(String format);
	public void setGroupingSize(int value);
	public void setGroupingUsed(boolean grouping);
	public void setInputLocale(String locale);
	public void setRendererLocale(String locale);
	public void setMaximumFractionDigits(int maxfracdig);
	public void setMaximumIntegerDigits(int maxintdig);
	public void setMinimumFractionDigits(int maxfracdig);
	public void setMinimumIntegerDigits(int minintdig);
	public void setMultiplier(int value);
	public void setOverflow(double value);
	public void setUnderflow(double value);
	public void setNegativePrefix(String value);
	public void setNegativeSuffix(String value);
	public void setParseIntegerOnly(boolean intonly);
	public void setPositivePrefix(String value);
	public void setPositiveSuffix(String value);
	public void setLowSubstitute(String[] value);
	public void setHighSubstitute(String[] value);
	public void setZeroNotShown(boolean nozero);
	public void setInputFormat(String value);
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    abstract
    class YoixJTableBaseRenderer extends DefaultTableCellRenderer

	implements YoixJTableCellRenderer

    {

	int  ALIGNMENT = SwingConstants.LEFT;

	double overflow = Double.MAX_VALUE;
	double underflow = -Double.MAX_VALUE;
	String losubsti[] = null;
	String hisubsti[] = null;
	HashMap substimap = null;
	HashMap substirev = null;
	boolean  nozero = false;
	String format = null;
	String inputformat = null;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableBaseRenderer() {

	    super();
	}

	///////////////////////////////////
	//
	// YoixJTableBaseRenderer Methods
	//
	///////////////////////////////////

	public boolean
	getDecimalSeparatorAlwaysShown() {

	    return(false);
	}


	public final int
	getDefaultAlignment() {

	    int  align;

	    switch (ALIGNMENT) {
	    case SwingConstants.RIGHT:
		align = YOIX_RIGHT;
		break;

	    case SwingConstants.CENTER:
		align = YOIX_CENTER;
		break;

	    case SwingConstants.LEFT:
		align = YOIX_LEFT;
		break;

	    default:
		VM.abort(INTERNALERROR);
		align = -1;
		break;
	    }
	    return(align);
	}


	public final int
	getDefaultHorizontalAlignment() {

	    return(ALIGNMENT);
	}

	public String
	getFormat() {

	    return(format);
	}


	public String
	getInputFormat() {

	    return(inputformat);
	}


	public int
	getGroupingSize() {

	    return(-1);
	}


	public boolean
	getGroupingUsed() {

	    return(false);
	}


	public int
	getMaximumFractionDigits() {

	    return(-1);
	}


	public int
	getMaximumIntegerDigits() {

	    return(-1);
	}


	public int
	getMinimumFractionDigits() {

	    return(-1);
	}


	public int
	getMinimumIntegerDigits() {

	    return(-1);
	}


	public NumberFormat
	getNumberInputFormat()
	    throws java.text.ParseException
	{

	    if (true)
		throw new java.text.ParseException("no NumberFormat available", -1);
	    return(null);
	}


	public YoixJTableNumberRenderer
	getNumberRenderer()
	    throws java.text.ParseException
	{

	    if (true)
		throw new java.text.ParseException("not a YoixJTableNumberRenderer", -1);
	    return(null);
	}


	public int
	getMultiplier() {

	    return(-1);
	}


	public double
	getOverflow() {

	    return(overflow);
	}


	public double
	getUnderflow() {

	    return(underflow);
	}


	public String[]
	getHighSubstitute() {

	    return(hisubsti);
	}


	public String[]
	getLowSubstitute() {

	    return(losubsti);
	}


	public Object
	getSubstitute(String value) {

	    Object  sub = null;

	    if (value != null && substimap != null)
		sub = substimap.get(value);

	    return(sub);
	}

	public String
	getReverseSubstitute(Object value) {

	    String rev = null;

	    if (value != null && substirev != null)
		rev = (String)substirev.get(value);

	    return (rev);
	}


	public String
	getNegativePrefix() {

	    return(null);
	}


	public String
	getNegativeSuffix() {

	    return(null);
	}


	public boolean
	getZeroNotShown() {

	    return(nozero);
	}


	public boolean
	getParseIntegerOnly() {

	    return(false);
	}


	public String
	getPositivePrefix() {

	    return(null);
	}


	public String
	getPositiveSuffix() {

	    return(null);
	}


	public int
	getRendererType() {
	    return(YOIX_STRING_TYPE);
	}


	public final Component
	getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

	    YoixSwingTableColumn  tblcol;
	    TableColumnModel      columnModel;
	    YoixJTableModel       yjtm;
	    Boolean               rowedit;
	    String                tip;
	    Color                 fore = null;
	    Color                 back = null;
	    Color                 colormatrix[][][];
	    Color                 cellfore[];
	    Color                 cellback[];
	    Color                 cellselectionfore[];
	    Color                 cellselectionback[];
	    Color                 color;
	    int                   state;
	    int                   idx = -1;
	    int                   midx;
	    int                   ridx;
	    boolean               isFound = (row == foundRow && column == foundColumn);

	    columnModel = table.getColumnModel();
	    tblcol = (YoixSwingTableColumn)(columnModel.getColumn(column));
	    yjtm = (YoixJTableModel)(((YoixSwingJTable)table).getModel());

	    midx = tblcol.getModelIndex();
	    ridx = yjtm.getRowIndex(row);

	    setFont(tblcol.getFont(table.getFont()));

	    setHorizontalAlignment(tblcol.getHorizontalAlignment(getDefaultHorizontalAlignment()));

	    if (hasFocus) {
		setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		if (((YoixSwingJTable)table).getUseEditHighlight() && table.isCellEditable(row, column)) {
		    //
		    // The changes that use cellfore and cellback were added
		    // on 10/17/10 - previous version always used UIManager
		    // colors.
		    //
		    if ((cellfore = tblcol.getCellForegrounds(null)) != null && cellfore.length > 0)
			fore = cellfore[ridx % cellfore.length];
		    if (fore == null)
			fore = UIManager.getColor("Table.focusCellForeground");
		    if ((cellback = tblcol.getCellBackgrounds(null)) != null && cellback.length > 0)
			back = cellback[ridx % cellback.length];
		    if (back == null)
			back = UIManager.getColor("Table.focusCellBackground");
		}
	    } else setBorder(tblcol.getBorder(yjtm.noFocusBorder));

	    if (fore == null && ridx >= 0) {// fore == null ==> back == null
		if (isSelected) {
		    if ((cellselectionfore = tblcol.getCellSelectionForegrounds()) != null && cellselectionfore.length > 0)
			fore = cellselectionfore[ridx % cellselectionfore.length];
		    else if (fore == null)
			fore = tblcol.getSelectionForeground(table.getSelectionForeground());
		    if ((cellselectionback = tblcol.getCellSelectionBackgrounds()) != null && cellselectionback.length > 0)
			back = cellselectionback[ridx % cellselectionback.length];
		    else if (back == null)
			back = tblcol.getSelectionBackground(table.getSelectionBackground());
		} else if ((colormatrix = yjtm.getColorMatrix()) != null) {
		    // colormatrix already is arranged by appropriate row index
		    back = colormatrix[row][midx][0];
		    fore = colormatrix[row][midx][1];
		}

		if (fore == null) {
		    cellfore = tblcol.getCellForegrounds(yjtm.cellForegrounds);

		    while (fore == null) {
			if (cellfore != null && cellfore.length > 1) {
			    if (cellfore == null || cellfore.length == 0)
				cellfore = new Color[] {table.getForeground()};
			    if (this instanceof YoixJTableHistogramRenderer || cellfore != yjtm.cellForegrounds)
				fore = cellfore[ridx % cellfore.length];
			    else fore = cellfore[row % cellfore.length];
			    if (fore == null) {
				if (cellfore != yjtm.cellForegrounds) {
				    cellfore = yjtm.cellForegrounds;
				} else fore = table.getForeground();
			    }
			} else {
			    //
			    // Changed on 10/25/10 - see comments in the code
			    // that handles back for more details.
			    //
			    if (cellfore != null && cellfore.length == 1)
				fore = cellfore[0];
			    if (fore == null)
				fore = table.getForeground();
			    break;
			}
		    }
		}

		if (back == null) {
		    cellback = tblcol.getCellBackgrounds(yjtm.cellBackgrounds);

		    while (back == null) {
			if (cellback != null && cellback.length > 1) {
			    if (cellback == null || cellback.length == 0)
				cellback = new Color[] {table.getBackground()};
			    if (this instanceof YoixJTableHistogramRenderer || cellback != yjtm.cellBackgrounds) {
				back = cellback[ridx % cellback.length];
			    } else back = cellback[row % cellback.length];
			    if (back == null) {
				if (cellback != yjtm.cellBackgrounds) {
				    cellback = yjtm.cellBackgrounds;
				} else back = table.getBackground();
			    }
			} else {
			    //
			    // Changed the logic here a bit on 10/25/10 because
			    // the code got stuck in an infinite loop. Wasn't
			    // able to trigger it again, but the problem really
			    // did happen and it was because cellback[0] was
			    // null. Decided to to force a break because there's
			    // a chance Component.getBackground() could return
			    // null, and if it did we'd be stuck again. In fact,
			    // that's probably how the loop that I encountered
			    // happened. Code was changed on 10/25/10. Made a
			    // similiar change in the block of code used to set
			    // fore (above).
			    //
			    if (cellback != null && cellback.length == 1)
				back = cellback[0];
			    if (back == null)
				back = table.getBackground();
			    break;
			}
		    }
		}
	    }

	    if (!isCellEditable(row, column)) {
		if ((color = tblcol.getDisabledForeground(null)) != null)
		    fore = color;
		if ((color = tblcol.getDisabledBackground(null)) != null)
		    back = color;
	    }

	    if (isFound) {
		setForeground(back);
		setBackground(fore);
	    } else {
		setForeground(fore);
		setBackground(back);
	    }

	    setValue(value);
	    if (this instanceof YoixJTableHistogramRenderer) {
		((YoixJTableHistogramRenderer)this).setRowColumn(row, column);
		((YoixJTableHistogramRenderer)this).setMaximumValue(tblcol.getEtc());
	    }

	    tip = tblcol.getTipText(ridx, midx, yjtm.getTipText(ridx, midx));
	    if (tip != null && tip.length() > 0 && table.getToolTipText() != null) {
		this.setToolTipText(tip);
	    } else if (this instanceof YoixJTableHistogramRenderer) {
		tip = "Value: " + ((YoixJTableHistogramRenderer)this).value;
		this.setToolTipText(tip);
	    } else this.setToolTipText(null);

	    
	    return(this);
	}


	public void
	resetDefaultHorizontalAlignment() {

	    ALIGNMENT = SwingConstants.LEFT;
	}


	public void
	setDecimalSeparatorAlwaysShown(boolean value) {

	}


	public final void
	setDefaultHorizontalAlignment(int alignment) {

	    switch (alignment) {
	    case SwingConstants.RIGHT:
	    case SwingConstants.CENTER:
	    case SwingConstants.LEFT:
		ALIGNMENT = alignment;
		break;

	    default:
		resetDefaultHorizontalAlignment();
		break;
	    }
	}


	public void
	setFormat(String value) {

	    format = value;
	}


	public void
	setInputFormat(String value) {

	    inputformat = value;
	}


	public void
	setGroupingSize(int value) {

	}


	public void
	setGroupingUsed(boolean value) {

	}


	public void
	setMaximumFractionDigits(int value) {

	}


	public void
	setMaximumIntegerDigits(int value) {

	}


	public void
	setMinimumFractionDigits(int value) {

	}


	public void
	setMinimumIntegerDigits(int value) {

	}


	public void
	setMultiplier(int value) {

	}


	public void
	setOverflow(double value) {

	    overflow = value;
	}


	public void
	setUnderflow(double value) {

	    underflow = value;
	}


	public void
	setNegativePrefix(String value) {

	}


	public void
	setNegativeSuffix(String value) {

	}


	public void
	setZeroNotShown(boolean value) {

	    nozero = value;
	}


	public void
	setParseIntegerOnly(boolean value) {

	}


	public void
	setPositivePrefix(String value) {

	}


	public void
	setPositiveSuffix(String value) {

	}


	public void
	setValue(Object value) {

	}


	public String
	stringValue(Object value, boolean formatted) {

	    return(value == null ? "" : value.toString());
	}


	public void setLowSubstitute(String[] value) {};
	public void setHighSubstitute(String[] value) {};

	public String getRendererLocale() { return(null); };
	public void   setRendererLocale(String locale) {};
	
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    abstract
    class YoixJTableNumberRenderer extends YoixJTableBaseRenderer {

	protected NumberFormat  nf;
	protected NumberFormat  inf;
	private   boolean       infdupe = true;
	private Locale          locale;
	private Locale          inputlocale;
	private int             instance;
	protected boolean       compact = false;
	protected boolean       integral = false;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableNumberRenderer() {

	    this(NUMBER_INSTANCE, Locale.getDefault());
	}


	public
	YoixJTableNumberRenderer(Locale locale) {

	    this(NUMBER_INSTANCE, locale);
	}


	public
	YoixJTableNumberRenderer(int instance) {

	    this(instance, Locale.getDefault());
	}


	public
	YoixJTableNumberRenderer(int instance, Locale locale) {

	    super();

	    this.instance = instance;
	    this.locale = locale;
	    this.inputlocale = locale;
	    resetDefaultHorizontalAlignment();
	    setFormatter(instance, locale, inputlocale);
	}

	///////////////////////////////////
	//
	// YoixJTableNumberRenderer Methods
	//
	///////////////////////////////////

	public final boolean
	getDecimalSeparatorAlwaysShown() {

	    return((nf instanceof DecimalFormat) ? ((DecimalFormat)nf).isDecimalSeparatorAlwaysShown() : false);
	}


	public final String
	getFormat() {

	    return((nf instanceof DecimalFormat) ? ((DecimalFormat)nf).toPattern() : "");
	}


	public final int
	getGroupingSize() {

	    return((nf instanceof DecimalFormat) ? ((DecimalFormat)nf).getGroupingSize() : -1);
	}


	public final boolean
	getGroupingUsed() {

	    return(nf.isGroupingUsed());
	}


	public final String
	getInputLocale() {

	    Locale loc = inputlocale;

	    String  variant = loc.getVariant();

	    return(loc.getLanguage() + "_" + loc.getCountry() + (variant.length() == 0 ? "" : "_" + variant));
	}


	public final String
	getRendererLocale() {

	    Locale loc = locale;

	    String  variant = loc.getVariant();

	    return(loc.getLanguage() + "_" + loc.getCountry() + (variant.length() == 0 ? "" : "_" + variant));
	}


	public final int
	getMaximumFractionDigits() {

	    return(nf.getMaximumFractionDigits());
	}


	public final int
	getMaximumIntegerDigits() {

	    return(nf.getMaximumIntegerDigits());
	}


	public final int
	getMinimumFractionDigits() {

	    return(nf.getMinimumIntegerDigits());
	}


	public final int
	getMinimumIntegerDigits() {

	    return(nf.getMinimumIntegerDigits());
	}


	public final int
	getMultiplier() {

	    return((nf instanceof DecimalFormat) ? ((DecimalFormat)nf).getMultiplier() : -1);
	}


	public final String
	getNegativePrefix() {

	    return((nf instanceof DecimalFormat) ? ((DecimalFormat)nf).getNegativePrefix() : null);
	}


	public final String
	getNegativeSuffix() {

	    return((nf instanceof DecimalFormat) ? ((DecimalFormat)nf).getNegativeSuffix() : null);
	}

	public NumberFormat
	getNumberInputFormat()
	    throws java.text.ParseException // some do
	{

	    return(inf);
	}


	public YoixJTableNumberRenderer
	getNumberRenderer()
	    throws java.text.ParseException // some do
	{

	    return(this);
	}


	public final boolean
	getParseIntegerOnly() {

	    return(nf.isParseIntegerOnly());
	}


	public final String
	getPositivePrefix() {

	    return((nf instanceof DecimalFormat) ? ((DecimalFormat)nf).getPositivePrefix() : null);
	}


	public final String
	getPositiveSuffix() {

	    return((nf instanceof DecimalFormat) ? ((DecimalFormat)nf).getPositiveSuffix() : null);
	}


	public final int
	getType() {

	    return(instance);
	}


	public final YoixJTableCellRenderer
	makeCopy() {

	    YoixJTableNumberRenderer  renderer;

	    if (this instanceof YoixJTableDoubleRenderer)
		renderer = new YoixJTableDoubleRenderer();
	    else if (this instanceof YoixJTableHistogramRenderer)
		renderer = new YoixJTableHistogramRenderer();
	    else if (this instanceof YoixJTableIntegerRenderer)
		renderer = new YoixJTableIntegerRenderer();
	    else if (this instanceof YoixJTableMoneyRenderer)
		renderer = new YoixJTableMoneyRenderer();
	    else if (this instanceof YoixJTablePercentRenderer)
		renderer = new YoixJTablePercentRenderer();
	    else {
		VM.abort(INTERNALERROR);
		renderer = null; // to mollify compiler
	    }

	    renderer.setDefaultHorizontalAlignment(getDefaultHorizontalAlignment());
	    renderer.setGroupingUsed(getGroupingUsed());
	    renderer.setMaximumFractionDigits(getMaximumFractionDigits());
	    renderer.setMaximumIntegerDigits(getMaximumIntegerDigits());
	    renderer.setMinimumIntegerDigits(getMinimumIntegerDigits());
	    renderer.setMinimumIntegerDigits(getMinimumIntegerDigits());
	    renderer.setParseIntegerOnly(getParseIntegerOnly());

	    renderer.setDecimalSeparatorAlwaysShown(getDecimalSeparatorAlwaysShown());
	    renderer.setFormat(getFormat());
	    renderer.setInputFormat(getInputFormat());
	    renderer.setGroupingSize(getGroupingSize());
	    renderer.setMultiplier(getMultiplier());
	    renderer.setNegativePrefix(getNegativePrefix());
	    renderer.setNegativeSuffix(getNegativeSuffix());
	    renderer.setPositivePrefix(getPositivePrefix());
	    renderer.setPositiveSuffix(getPositiveSuffix());

	    renderer.setLowSubstitute(getLowSubstitute());
	    renderer.setHighSubstitute(getHighSubstitute());
	    renderer.setOverflow(getOverflow());
	    renderer.setUnderflow(getUnderflow());

	    return(renderer);
	}


	public final void
	resetDefaultHorizontalAlignment() {

	    ALIGNMENT = SwingConstants.RIGHT;
	}


	public final void
	setDecimalSeparatorAlwaysShown(boolean value) {

	    if (nf instanceof DecimalFormat) {
		((DecimalFormat)nf).setDecimalSeparatorAlwaysShown(value);
		((DecimalFormat)inf).setDecimalSeparatorAlwaysShown(value);
	    }
	}


	public final void
	setFormat(String format) {


	    if (format != null && format.equalsIgnoreCase("c")) {
		compact = true;
		integral = false;
	    } else if (format != null && format.equalsIgnoreCase("i")) {
		compact = true;
		integral = true;
	    } else if (nf instanceof DecimalFormat) {
		try {
		    // Java doesn't seem to synchronize parse and applyPattern, which
		    // means we probably should - no time now, next release
		    // (applies to all NumberFormat/YoixSimpleDateFormat situations here)
		    ((DecimalFormat)nf).applyPattern(format);
		    if (infdupe)
			((DecimalFormat)inf).applyPattern(format);
		    compact = false;
		}
		catch(IllegalArgumentException iae) {
		    VM.abort(BADVALUE, new String[] {"number column format " + format + " is invalid"});
		}
	    }
	}


	public final void
	setInputFormat(String format) {

	    if (format != null && !format.equals(((DecimalFormat)nf).toPattern())) {
		if (format != null && !format.equalsIgnoreCase("c") && !format.equalsIgnoreCase("i")) {
		    try {
			((DecimalFormat)inf).applyPattern(format);
			infdupe = false;
		    }
		    catch(IllegalArgumentException iae) {
			VM.abort(BADVALUE, new String[] {"number column format " + format + " is invalid"});
		    }
		}
	    }
	}


	public final void
	setGroupingSize(int value) {

	    if (nf instanceof DecimalFormat) {
		((DecimalFormat)nf).setGroupingSize(value);
		((DecimalFormat)inf).setGroupingSize(value);
	    }
	}


	public final void
	setGroupingUsed(boolean value) {

	    nf.setGroupingUsed(value);
	    inf.setGroupingUsed(value);
	}


	public final synchronized void
	setInputLocale(String tag) {

	    Locale   loc;
	    String[] parts;

	    if (tag == null || tag.trim().length() == 0)
		loc = Locale.getDefault();
	    else {
		parts = tag.split("_",3);
		if (parts.length == 1)
		    loc = new Locale(parts[0]);
		else if (parts.length == 2)
		    loc = new Locale(parts[0], parts[1]);
		else loc = new Locale(parts[0], parts[1], parts[2]);
	    }
	    setFormatter(instance, locale, inputlocale = loc);
	}


	public final synchronized void
	setRendererLocale(String tag) {

	    Locale   loc;
	    String[] parts;

	    if (tag == null || tag.trim().length() == 0)
		loc = Locale.getDefault();
	    else {
		parts = tag.split("_",3);
		if (parts.length == 1)
		    loc = new Locale(parts[0]);
		else if (parts.length == 2)
		    loc = new Locale(parts[0], parts[1]);
		else loc = new Locale(parts[0], parts[1], parts[2]);
	    }
	    setFormatter(instance, locale = loc, inputlocale);
	}


	public final void
	setMaximumFractionDigits(int value) {

	    nf.setMaximumFractionDigits(value);
	    inf.setMaximumFractionDigits(value);
	}


	public final void
	setMaximumIntegerDigits(int value) {

	    nf.setMaximumIntegerDigits(value);
	    inf.setMaximumIntegerDigits(value);
	}


	public final void
	setMinimumFractionDigits(int value) {

	    nf.setMinimumFractionDigits(value);
	    inf.setMinimumFractionDigits(value);
	}


	public final void
	setMinimumIntegerDigits(int value) {

	    nf.setMinimumIntegerDigits(value);
	    inf.setMinimumIntegerDigits(value);
	}


	public final void
	setMultiplier(int value) {

	    if (nf instanceof DecimalFormat) {
		((DecimalFormat)nf).setMultiplier(value);
		((DecimalFormat)inf).setMultiplier(value);
	    }
	}


	public final void
	setNegativePrefix(String value) {

	    if (nf instanceof DecimalFormat) {
		((DecimalFormat)nf).setNegativePrefix(value == null ? "" : value);
		((DecimalFormat)inf).setNegativePrefix(value == null ? "" : value);
	    }
	}


	public final void
	setNegativeSuffix(String value) {

	    if (nf instanceof DecimalFormat) {
		((DecimalFormat)nf).setNegativeSuffix(value == null ? "" : value);
		((DecimalFormat)inf).setNegativeSuffix(value == null ? "" : value);
	    }
	}


	public final void
	setParseIntegerOnly(boolean value) {

	    nf.setParseIntegerOnly(value);
	    inf.setParseIntegerOnly(value);
	}


	public final void
	setPositivePrefix(String value) {

	    if (nf instanceof DecimalFormat) {
		((DecimalFormat)nf).setPositivePrefix(value == null ? "" : value);
		((DecimalFormat)inf).setPositivePrefix(value == null ? "" : value);
	    }
	}


	public final void
	setPositiveSuffix(String value) {

	    if (nf instanceof DecimalFormat) {
		((DecimalFormat)nf).setPositiveSuffix(value == null ? "" : value);
		((DecimalFormat)inf).setPositiveSuffix(value == null ? "" : value);
	    }
	}


	public final void
	setType(int instance) {

	    this.instance = instance;
	    setFormatter(instance, locale, inputlocale);
	}

	public String
	stringValue(Object value, boolean formatted) {

	    String preview;

	    return(((preview = stringPreview(value)) != null) ? preview : formatted ? nf.format(((Number)value).doubleValue()) : value.toString());
	}

	///////////////////////////////////
	//
	// Protected Methods
	//
	///////////////////////////////////

	protected String
	compactString(double val, boolean integral) {

	    if (integral && Math.abs(val) < 1.0) {
		if (val < 0)
		    return("-0");
		else if (val > 0)
		    return("+0");
		else return("0");
	    } else return(compactString(val));
	}

	protected String
	compactString(long val) {
	    String  output;
	    boolean negative;
	    int     nbr;

	    if (val < 0) {
		negative = true;
		val = -val;
	    } else negative = false;

	    if (val >= 1000000000000000000L) {
		nbr = (int)(val / 1000000000000000000L);
		output = nbr + "E"; // exa
	    } else if (val >= 1000000000000000L) {
		nbr = (int)(val / 1000000000000000L);
		output = nbr + "P"; // peta
	    } else if (val >= 1000000000000L) {
		nbr = (int)(val / 1000000000000L);
		output = nbr + "T"; // tera
	    } else if (val >= 1000000000L) {
		nbr = (int)(val / 1000000000L);
		output = nbr + "G"; // giga
	    } else if (val >= 1000000L) {
		nbr = (int)(val / 1000000L);
		output = nbr + "M"; // mega
	    } else if (val >= 1000L) {
		nbr = (int)(val / 1000L);
		output = nbr + "k"; // kilo
	    } else output = val + ""; // units

	    if (negative)
		output = "-" + output;

	    return(output);
	}

	protected String
	compactString(double val) {
	    String  output;
	    boolean negative;
	    int     nbr;

	    if (val < 0) {
		negative = true;
		val = -val;
	    } else negative = false;

	    if (val >= 1.0e27)
		output = nf.format(Math.floor(val/1.0e24)) + "Y";
	    else if (val >= 1.0e24) {
		nbr = (int)(val / 1.0e24);
		output = nbr + "Y"; // yotta
	    } else if (val >= 1.0e21) {
		nbr = (int)(val / 1.0e21);
		output = nbr + "Z"; // zetta
	    } else if (val >= 1.0e18) {
		nbr = (int)(val / 1.0e18);
		output = nbr + "E"; // exa
	    } else if (val >= 1.0e15) {
		nbr = (int)(val / 1.0e15);
		output = nbr + "P"; // peta
	    } else if (val >= 1.0e12) {
		nbr = (int)(val / 1.0e12);
		output = nbr + "T"; // tera
	    } else if (val >= 1.0e9) {
		nbr = (int)(val / 1.0e9);
		output = nbr + "G"; // giga
	    } else if (val >= 1.0e6) {
		nbr = (int)(val / 1.0e6);
		output = nbr + "M"; // mega
	    } else if (val >= 1.0e3) {
		nbr = (int)(val / 1.0e3);
		output = nbr + "k"; // kilo
	    } else if (val >= 1.0) {
		nbr = (int)val;
		output = nbr + ""; // units
	    } else if (val >= 1.0e-3) {
		nbr = (int)(val / 1.0e-3);
		output = nbr + "m"; // milli
	    } else if (val >= 1.0e-6) {
		nbr = (int)(val / 1.0e-6);
		output = nbr + "u"; // micro
	    } else if (val >= 1.0e-9) {
		nbr = (int)(val / 1.0e-9);
		output = nbr + "n"; // nano
	    } else if (val >= 1.0e-12) {
		nbr = (int)(val / 1.0e-12);
		output = nbr + "p"; // pico
	    } else if (val >= 1.0e-15) {
		nbr = (int)(val / 1.0e-15);
		output = nbr + "f"; // femto
	    } else if (val >= 1.0e-18) {
		nbr = (int)(val / 1.0e-18);
		output = nbr + "a"; // atto
	    } else if (val >= 1.0e-21) {
		nbr = (int)(val / 1.0e-21);
		output = nbr + "z"; // zepto
	    } else if (val >= 1.0e-24) {
		nbr = (int)(val / 1.0e-24);
		output = nbr + "y"; // yocto
	    } else if (val > 0)
		output = "+0";
	    else output = "0";

	    if (negative)
		output = "-" + output; // allow -0

	    return(output);
	}

	protected String
	stringPreview(Object value) {

	    return((value == null || !(value instanceof Number) || (value instanceof Double && ((Double)value).isNaN())) ? "" : getReverseSubstitute(value));
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	private synchronized void
	setFormatter(int instance, Locale locale, Locale inputlocale) {

	    String nfpat = (nf == null) ? null : ((DecimalFormat)nf).toPattern();
	    String infpat = (inf == null) ? null : ((DecimalFormat)inf).toPattern();
	    int    nfmult = (nf == null) ? 1 : ((DecimalFormat)nf).getMultiplier();
	    int    infmult = (inf == null) ? 1 : ((DecimalFormat)inf).getMultiplier();

	    //
	    // Unfortunately some subtle issues can creep in here if nf isn't
	    // null and the original format included special characters (e.g.,
	    // %) because they can trigger side effects in nf (e.g., setting
	    // the multiplier to 100 in the case of %). The pattern that we
	    // get back from nf.toPattern() surrounds special characters in
	    // single quotes and those quotes prevent the side effects from
	    // happening when we use that pattern in nf.applyPattern().
	    //
	    // Saw this happen in a column that was displaying percents. The
	    // column's original format was #,##0%, but toPattern() returned
	    // #,##0'%' and the quotes surrounding the % meant the multiplier
	    // wouldn't be set to 100 after applyPattern() and that meant the
	    // percents were displayed in the column. Annoying bug that took
	    // a while to understand. The fix tries to restore the multipliers
	    // that were defined in nf and inf after applyPattern() is called.
	    //
	    // NOTE - fix is sufficient for now (7/31/10), but there's chance
	    // other "side effects" will need attention. Only way to be sure
	    // is to take a close look at Java's DecimalFormat.applyPattern()
	    // method.
	    //

	    if (inputlocale == null)
		inputlocale = locale;

	    switch (instance) {
	    case CURRENCY_INSTANCE:
		nf = NumberFormat.getCurrencyInstance(locale);
		inf = NumberFormat.getCurrencyInstance(inputlocale);
		break;

	    case NUMBER_INSTANCE:
		nf = NumberFormat.getNumberInstance(locale);
		inf = NumberFormat.getNumberInstance(inputlocale);
		break;

	    case PERCENT_INSTANCE:
		nf = NumberFormat.getPercentInstance(locale);
		inf = NumberFormat.getPercentInstance(inputlocale);
		break;

	    default:
		nf = NumberFormat.getInstance(locale);
		inf = NumberFormat.getInstance(inputlocale);
		break;
	    }

	    if (nfpat != null) {
		((DecimalFormat)nf).applyPattern(nfpat);
		((DecimalFormat)nf).setMultiplier(nfmult);
	    }
	    if (infpat != null) {
		((DecimalFormat)inf).applyPattern(infpat);
		((DecimalFormat)inf).setMultiplier(infmult);
	    } else if (nfpat != null && inf != null) {
		((DecimalFormat)inf).applyPattern(nfpat);
		((DecimalFormat)inf).setMultiplier(nfmult);
	    }

	    if (!locale.equals(inputlocale))
		infdupe = false;
	    if (!((DecimalFormat)nf).toPattern().equals(((DecimalFormat)inf).toPattern()))
		infdupe = false;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableBooleanRenderer extends JCheckBox

	implements TableCellRenderer,
	YoixJTableCellRenderer

    {

	int  ALIGNMENT = SwingConstants.CENTER;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableBooleanRenderer() {

	    super();
	    resetDefaultHorizontalAlignment();
	    setBorderPainted(true);		// so we can tell when it has focus
	}

	///////////////////////////////////
	//
	// YoixJTableBooleanRenderer Methods
	//
	///////////////////////////////////


	public final boolean
	getDecimalSeparatorAlwaysShown() {

	    return(false);
	}


	public final int
	getDefaultAlignment() {

	    int  align;

	    switch (ALIGNMENT) {
	    case SwingConstants.RIGHT:
		align = YOIX_RIGHT;
		break;

	    case SwingConstants.CENTER:
		align = YOIX_CENTER;
		break;

	    case SwingConstants.LEFT:
		align = YOIX_LEFT;
		break;

	    default:
		VM.abort(INTERNALERROR);
		align = -1;
		break;
	    }
	    return(align);
	}


	public final int
	getDefaultHorizontalAlignment() {

	    return(ALIGNMENT);
	}


	public final String
	getFormat() {

	    return(null);
	}


	public final String
	getInputFormat() {

	    return(null);
	}


	public final int
	getGroupingSize() {

	    return(-1);
	}


	public final boolean
	getGroupingUsed() {

	    return(false);
	}


	public final int
	getMaximumFractionDigits() {

	    return(-1);
	}


	public final int
	getMaximumIntegerDigits() {

	    return(-1);
	}


	public final int
	getMinimumFractionDigits() {

	    return(-1);
	}


	public final int
	getMinimumIntegerDigits() {

	    return(-1);
	}


	public final int
	getMultiplier() {

	    return(-1);
	}


	public final String
	getNegativePrefix() {

	    return(null);
	}


	public final String
	getNegativeSuffix() {

	    return(null);
	}


	public final NumberFormat
	getNumberInputFormat()
	    throws java.text.ParseException
	{

	    if (true)
		throw new java.text.ParseException("no NumberFormat available", -1);
	    return(null);
	}


	public YoixJTableNumberRenderer
	getNumberRenderer()
	    throws java.text.ParseException
	{

	    if (true)
		throw new java.text.ParseException("not a YoixJTableNumberRenderer", -1);
	    return(null);
	}


	public final boolean
	getZeroNotShown() {

	    return(false);
	}


	public final boolean
	getParseIntegerOnly() {

	    return(false);
	}


	public final String
	getPositivePrefix() {

	    return(null);
	}


	public final String
	getPositiveSuffix() {

	    return(null);
	}


	public int
	getRendererType() {
	    return(YOIX_BOOLEAN_TYPE);
	}


	public final Component
	getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

	    YoixSwingTableColumn  tblcol;
	    TableColumnModel      columnModel;
	    YoixJTableModel       yjtm;
	    Boolean               rowedit;
	    Color                 fore = null;
	    Color                 back = null;
	    Color                 colormatrix[][][];
	    Color                 cellfore[];
	    Color                 cellback[];
	    Color                 cellselectionfore[];
	    Color                 cellselectionback[];
	    Color                 color;
	    int                   state;
	    int                   idx = -1;
	    int                   midx;
	    int                   ridx;
	    boolean               isFound = (row == foundRow && column == foundColumn);

	    columnModel = table.getColumnModel();
	    tblcol = (YoixSwingTableColumn)(columnModel.getColumn(column));
	    yjtm = (YoixJTableModel)(((YoixSwingJTable)table).getModel());

	    midx = tblcol.getModelIndex();
	    ridx = yjtm.getRowIndex(row);

	    setFont(tblcol.getFont(table.getFont()));
	    setHorizontalAlignment(tblcol.getHorizontalAlignment(getDefaultHorizontalAlignment()));

	    if (hasFocus) {
		setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		if (((YoixSwingJTable)table).getUseEditHighlight() && table.isCellEditable(row, column)) {
		    //
		    // The changes that use cellfore and cellback were added
		    // on 10/28/10 - previous version always used UIManager
		    // colors.
		    //
		    if ((cellfore = tblcol.getCellForegrounds(null)) != null && cellfore.length > 0)
			fore = cellfore[ridx % cellfore.length];
		    if (fore == null)
			fore = UIManager.getColor("Table.focusCellForeground");
		    if ((cellback = tblcol.getCellBackgrounds(null)) != null && cellback.length > 0)
			back = cellback[ridx % cellback.length];
		    if (back == null)
			back = UIManager.getColor("Table.focusCellBackground");
		}
	    } else setBorder(tblcol.getBorder(yjtm.noFocusBorder));

	    if (fore == null && ridx >= 0) { // ==> back == null, too
		if (isSelected) {
		    if ((cellselectionfore = tblcol.getCellSelectionForegrounds()) != null && cellselectionfore.length > 0)
			fore = cellselectionfore[ridx % cellselectionfore.length];
		    else if (fore == null)
			fore = tblcol.getSelectionForeground(table.getSelectionForeground());
		    if ((cellselectionback = tblcol.getCellSelectionBackgrounds()) != null && cellselectionback.length > 0)
			back = cellselectionback[ridx % cellselectionback.length];
		    else if (back == null)
			back = tblcol.getSelectionBackground(table.getSelectionBackground());
		} else if ((colormatrix = yjtm.getColorMatrix()) != null) {
		    // colormatrix already is arranged by appropriate row index
		    back = colormatrix[row][midx][0];
		    fore = colormatrix[row][midx][1];
		}

		if (fore == null) {
		    cellfore = tblcol.getCellForegrounds(yjtm.cellForegrounds);

		    while (fore == null) {
			if (cellfore != null && cellfore.length > 1) {
			    if (cellfore == null || cellfore.length == 0)
				cellfore = new Color[] {table.getForeground()};
			    if (cellfore != yjtm.cellForegrounds)
				fore = cellfore[ridx % cellfore.length];
			    else fore = cellfore[row % cellfore.length];
			    if (fore == null) {
				if (cellfore != yjtm.cellForegrounds) {
				    cellfore = yjtm.cellForegrounds;
				} else fore = table.getForeground();
			    }
			} else {
			    //
			    // This code was changed on 10/27/10 - see comments
			    // in YoixJTableBaseRenderer for more details.
			    //
			    if (cellfore != null && cellfore.length == 1)
				fore = cellfore[0];
			    if (fore == null)
				fore = table.getForeground();
			    break;
			}
		    }
		}
		if (back == null) {
		    cellback = tblcol.getCellBackgrounds(yjtm.cellBackgrounds);

		    while (back == null) {
			if (cellback != null && cellback.length > 1) {
			    if (cellback == null || cellback.length == 0)
				cellback = new Color[] {table.getBackground()};
			    if (cellback != yjtm.cellBackgrounds)
				back = cellback[ridx % cellback.length];
			    else back = cellback[row % cellback.length];
			    if (back == null) {
				if (cellback != yjtm.cellBackgrounds) {
				    cellback = yjtm.cellBackgrounds;
				} else back = table.getBackground();
			    }
			} else {
			    //
			    // This code was changed on 10/27/10 - see comments
			    // in YoixJTableBaseRenderer for more details.
			    //
			    if (cellback != null && cellback.length == 1)
				back = cellback[0];
			    if (back == null)
				back = table.getBackground();
			    break;
			}
		    }
		}
	    }

	    if (!isCellEditable(row, column)) {
		if ((color = tblcol.getDisabledForeground(null)) != null)
		    fore = color;
		if ((color = tblcol.getDisabledBackground(null)) != null)
		    back = color;
	    }

	    if (isFound) {
		setForeground(back);
		setBackground(fore);
	    } else {
		setForeground(fore);
		setBackground(back);
	    }

	    setSelected(value != null && ((Boolean)value).booleanValue());
	    return(this);
	}


	public final YoixJTableCellRenderer
	makeCopy() {

	    YoixJTableBooleanRenderer  renderer = new YoixJTableBooleanRenderer();

	    renderer.setDefaultHorizontalAlignment(getDefaultHorizontalAlignment());
	    return(renderer);
	}


	public final void
	resetDefaultHorizontalAlignment() {

	    ALIGNMENT = SwingConstants.CENTER;
	}


	public final void
	setDecimalSeparatorAlwaysShown(boolean value) {

	}


	public final void
	setDefaultHorizontalAlignment(int alignment) {

	    switch (alignment) {
	    case SwingConstants.RIGHT:
	    case SwingConstants.CENTER:
	    case SwingConstants.LEFT:
		ALIGNMENT = alignment;
		break;

	    default:
		resetDefaultHorizontalAlignment();
		break;
	    }
	}


	public final void
	setFormat(String format) {

	}


	public final void
	setInputFormat(String format) {

	}


	public final void
	setGroupingSize(int value) {

	}


	public final void
	setGroupingUsed(boolean value) {

	}


	public final void
	setMaximumFractionDigits(int value) {

	}


	public final void
	setMaximumIntegerDigits(int value) {

	}


	public final void
	setMinimumFractionDigits(int value) {

	}


	public final void
	setMinimumIntegerDigits(int value) {

	}


	public final void
	setMultiplier(int value) {

	}


	public final void
	setNegativePrefix(String value) {

	}


	public final void
	setNegativeSuffix(String value) {

	}


	public final void
	setZeroNotShown(boolean value) {

	}


	public final void
	setParseIntegerOnly(boolean value) {

	}


	public final void
	setPositivePrefix(String value) {

	}


	public final void
	setPositiveSuffix(String value) {

	}


	public final String
	stringValue(Object value, boolean formatted) {

	    return(value != null && ((Boolean)value).booleanValue() ? "true" : "false");
	}

	public String[] getLowSubstitute() { return(null); };
	public String[] getHighSubstitute() { return(null); };
	public Object getSubstitute(String value) { return(null); };
	public String getReverseSubstitute(Object value) { return(null); };
	public double getOverflow() { return(Double.MAX_VALUE); };
	public double getUnderflow() { return(-Double.MAX_VALUE); };
	public void setLowSubstitute(String[] value) {};
	public void setHighSubstitute(String[] value) {};
	public void setOverflow(double value) {};
	public void setUnderflow(double value) {};

	public String getRendererLocale() { return(null); };
	public void   setRendererLocale(String locale) {};
	public String getInputLocale() { return(null); };
	public void   setInputLocale(String locale) {};
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableDateRenderer extends YoixJTableBaseRenderer {

	private YoixSimpleDateFormat  sdf;
	private YoixSimpleDateFormat  isdf = null;
	private Locale                locale;
	private Locale                inputlocale;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableDateRenderer() {

	    this(Locale.getDefault());
	}

	public
	YoixJTableDateRenderer(Locale locale) {

	    super();

	    this.locale = locale;
	    this.inputlocale = locale;
	    resetDefaultHorizontalAlignment();
	    try {
		sdf = new YoixSimpleDateFormat(UNIX_DATE_FORMAT, locale);
		sdf.setTimeZone(YoixMiscTime.getDefaultTimeZone());
		sdf.setLenient(true);
	    }
	    catch(IllegalArgumentException iae) {
		VM.abort(INTERNALERROR);
	    }
	}

	///////////////////////////////////
	//
	// YoixJTableDateRenderer Methods
	//
	///////////////////////////////////

	public final String
	getFormat() {

	    YoixSimpleDateFormat  snap = sdf;

	    return(snap.toPattern());
	}

	public final String
	getInputFormat() {

	    YoixSimpleDateFormat  snap = (isdf == null ? sdf : isdf);

	    return(snap.toPattern());
	}


	public final String
	getInputLocale() {

	    Locale loc = inputlocale;

	    String  variant = loc.getVariant();

	    return(loc.getLanguage() + "_" + loc.getCountry() + (variant.length() == 0 ? "" : "_" + variant));
	}


	public final String
	getRendererLocale() {

	    Locale loc = locale;

	    String  variant = loc.getVariant();

	    return(loc.getLanguage() + "_" + loc.getCountry() + (variant.length() == 0 ? "" : "_" + variant));
	}


	public int
	getRendererType() {
	    return(YOIX_DATE_TYPE);
	}


	final YoixSimpleDateFormat
	getSimpleDateFormat() {

	    return(sdf);
	}


	final YoixSimpleDateFormat
	getSimpleDateInputFormat() {

	    return(isdf == null ? sdf : isdf);
	}


	final TimeZone
	getInputTimeZone() {

	    return((isdf == null ? sdf : isdf).getTimeZone());
	}


	final TimeZone
	getTimeZone() {

	    return(sdf.getTimeZone());
	}


	public final YoixJTableCellRenderer
	makeCopy() {

	    YoixJTableDateRenderer  renderer = new YoixJTableDateRenderer();

	    renderer.setDefaultHorizontalAlignment(getDefaultHorizontalAlignment());
	    renderer.setFormat(getFormat());
	    renderer.setRendererLocale(getRendererLocale());
	    renderer.setTimeZone(getTimeZone().getID());
	    if (isdf != null) {
		renderer.setInputFormat(getInputFormat());
		renderer.setInputLocale(getInputLocale());
		renderer.setInputTimeZone(getInputTimeZone().getID());
	    }

	    renderer.setLowSubstitute(getLowSubstitute());
	    renderer.setHighSubstitute(getHighSubstitute());

	    return(renderer);
	}


	public final void
	resetDefaultHorizontalAlignment() {

	    ALIGNMENT = SwingConstants.LEFT;
	}


	public final void
	setFormat(String format) {

	    YoixSimpleDateFormat  snap = sdf;

	    try {
		snap.applyPattern(format);
	    }
	    catch(IllegalArgumentException iae) {
		VM.abort(BADVALUE, new String[] {"date column format " + format + " is invalid"});
	    }
	}


	public final void
	setInputFormat(String format) {

	    YoixSimpleDateFormat  snap;

	    if (isdf == null) {
		synchronized(sdf) {
		    if (isdf == null) {
			try {
			    isdf = new YoixSimpleDateFormat(sdf.toPattern(), locale);
			    isdf.setTimeZone(sdf.getTimeZone());
			    isdf.setLenient(true);
			}
			catch(IllegalArgumentException iae) {
			    VM.abort(INTERNALERROR);
			}
		    }
		}
	    }

	    snap = isdf;

	    try {
		snap.applyPattern(format);
	    }
	    catch(IllegalArgumentException iae) {
		VM.abort(BADVALUE, new String[] {"date column input format " + format + " is invalid"});
	    }
	}


	public final synchronized void
	setInputLocale(String tag) {

	    String[]              parts;
	    YoixSimpleDateFormat  snap = isdf == null ? sdf : isdf;
	    Locale                loc;
	    TimeZone              tz;

	    if (tag == null || tag.trim().length() == 0)
		loc = Locale.getDefault();
	    else {
		parts = tag.split("_",3);
		if (parts.length == 1)
		    loc = new Locale(parts[0]);
		else if (parts.length == 2)
		    loc = new Locale(parts[0], parts[1]);
		else loc = new Locale(parts[0], parts[1], parts[2]);
	    }

	    if (!loc.equals(inputlocale)) {
		tz = snap.getTimeZone();
		snap = new YoixSimpleDateFormat(snap.toPattern(), loc);
		snap.setTimeZone(tz);
		snap.setLenient(true);
		inputlocale = loc;
		isdf = snap;
	    }
	}


	public final synchronized void
	setRendererLocale(String tag) {

	    String[]              parts;
	    YoixSimpleDateFormat  snap = sdf;
	    Locale                loc;
	    TimeZone              tz;

	    if (tag == null || tag.trim().length() == 0)
		loc = Locale.getDefault();
	    else {
		parts = tag.split("_",3);
		if (parts.length == 1)
		    loc = new Locale(parts[0]);
		else if (parts.length == 2)
		    loc = new Locale(parts[0], parts[1]);
		else loc = new Locale(parts[0], parts[1], parts[2]);
	    }
	    if (!loc.equals(locale)) {
		tz = snap.getTimeZone();
		snap = new YoixSimpleDateFormat(snap.toPattern(), locale = loc);
		snap.setTimeZone(tz);
		snap.setLenient(true);
		sdf = snap;
	    }
	}

	public final void
	setTimeZone(String tz) {

	    YoixSimpleDateFormat  snap = sdf;

	    snap.setTimeZone(TimeZone.getTimeZone(tz));
	}


	public final void
	setInputTimeZone(String tz) {

	    YoixSimpleDateFormat  snap;

	    if (isdf == null) {
		synchronized(sdf) {
		    if (isdf == null) {
			try {
			    isdf = new YoixSimpleDateFormat(sdf.toPattern(), locale);
			    isdf.setTimeZone(sdf.getTimeZone());
			    isdf.setLenient(true);
			}
			catch(IllegalArgumentException iae) {
			    VM.abort(INTERNALERROR);
			}
		    }
		}
	    }

	    snap = isdf;

	    snap.setTimeZone(TimeZone.getTimeZone(tz));
	}


	// special case used in makeCopy, so isdf is not null
	private final void
	setInputTimeZone(TimeZone tz) {

	    YoixSimpleDateFormat  snap = isdf;

	    snap.setTimeZone(tz);
	}


	public final void
	setValue(Object value) {

	    YoixSimpleDateFormat  snap = sdf;
	    String                preview = stringPreview(value);

	    if (preview == null) {
		if (nozero && ((Date)value).getTime() == 0L)
		    preview = "";
		else preview = snap.format((Date)value);
	    }

	    setText(preview);
	}

	public final String
	stringValue(Object value, boolean formatted) {

	    YoixSimpleDateFormat  snap = sdf;
	    String                preview = stringPreview(value);

	    return(preview != null ? preview : snap.format((Date)value));
	}

	private final String
	stringPreview(Object value) {

	    Date  subval;

	    return((value == null || !(value instanceof Date)) ? "" : getReverseSubstitute(value));
	}


	public void
	setHighSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    Date     value;
	    long     m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0L, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new Date(Long.MAX_VALUE - m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    hisubsti = substi;
	}


	public void
	setLowSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    Date     value;
	    long     m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0L, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new Date(Long.MIN_VALUE + m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    losubsti = substi;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableDoubleRenderer extends YoixJTableNumberRenderer {

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableDoubleRenderer() {

	    super(NUMBER_INSTANCE);
	}

	public
	YoixJTableDoubleRenderer(int type) {

	    super(type);
	}

	///////////////////////////////////
	//
	// YoixJTableDoubleRenderer Methods
	//
	///////////////////////////////////

	public int
	getRendererType() {
	    return(YOIX_DOUBLE_TYPE);
	}


	public void
	setValue(Object value) {

	    double  val;
	    String  preview = stringPreview(value);

	    if (preview == null) {
		val = ((Number)value).doubleValue();

		if (nozero && val == 0)
		    preview = "";
		else if (val > overflow) {
		    preview = nf.format(overflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0 && overflow >= 0)
			preview = "-" + preview;
		} else if (val < underflow) {
		    preview = nf.format(underflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0 && underflow >= 0)
			preview = "-" + preview;
		} else if (compact)
		    preview = compactString(val, integral);
		else preview = nf.format(val);
	    }

	    setText(preview);
	}

	public String
	stringValue(Object value, boolean formatted) {

	    String preview;

	    return(((preview = stringPreview(value)) != null) ? preview : formatted ? (compact ? compactString(((Number)value).doubleValue(), integral) : nf.format(((Number)value).doubleValue())) : value.toString());
	}

	protected final String
	stringPreview(Object value) {

	    return((value == null || !(value instanceof Number) || (value instanceof Double && ((Double)value).isNaN())) ? "" : getReverseSubstitute(value));
	}


	public void
	setHighSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    Double   value;
	    double   m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new Double(Double.MAX_VALUE - m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    hisubsti = substi;
	}


	public void
	setLowSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    Double   value;
	    double   m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new Double(-Double.MAX_VALUE + m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    losubsti = substi;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    // TODO: make all these inner classes protected so other modules can extend them;
    //       right now, this particular one is needed for the data module
    protected class YoixJTableHistogramRenderer extends YoixJTableNumberRenderer {

	private double value;
	private double maxvalue;
	private int row = -1;
	private int column = -1;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableHistogramRenderer() {

	    super(NUMBER_INSTANCE);
	}

	///////////////////////////////////
	//
	// YoixJTableHistogramRenderer Methods
	//
	///////////////////////////////////

	public final int
	getColumn() {
	    return(column);
	}


	public int
	getRendererType() {
	    return(YOIX_HISTOGRAM_TYPE);
	}


	public final int
	getRow() {
	    return(row);
	}


	public final void
	setMaximumValue(YoixObject value) {

	    this.maxvalue = (value == null || value.isNull() || !(value.isNumber())) ? 0 : value.doubleValue();
	}


	public final void
	setRowColumn(int row, int column) {

	    this.row = row;
	    this.column = column;
	}


	public final void
	setValue(Object value) {

	    this.value = (value == null || !(value instanceof Number)) ? 0 : ((Number)value).doubleValue();
	}


	public final String
	stringValue(Object value, boolean formatted) {

	    return((value == null || !(value instanceof Number)) ? "" : formatted ?  nf.format(((Number)value).doubleValue()) : value.toString());
	}

	///////////////////////////////////
	//
	// JComponent Methods
	//
	///////////////////////////////////

	protected void
	paintComponent(Graphics g) {
	    Graphics2D  g2d = (Graphics2D) g;
	    Dimension   size = getSize();
	    Rectangle   rect = new Rectangle(0, 0, size.width, size.height);
	    int         width;
	    Color       orig;

	    super.paintComponent(g);

	    width = rect.width;

	    if (maxvalue > value)
		rect.width = (int)Math.round(width * (value/maxvalue));

	    switch (getHorizontalAlignment()) {
	    case SwingConstants.RIGHT:
		rect.x = width - rect.width;
		break;
	    case SwingConstants.CENTER:
		rect.x = (int)Math.round((width - rect.width) / 2);
		break;
	    case SwingConstants.LEFT:
	    default:
		break;
	    }

	    orig = g.getColor();
	    g.setColor(getForeground());
	    g.fillRect(rect.x, rect.y, rect.width, rect.height);
	    g.setColor(orig);
	}
	
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableIconRenderer extends YoixJTableBaseRenderer {

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableIconRenderer() {

	    super();
	    resetDefaultHorizontalAlignment();
	}

	///////////////////////////////////
	//
	// YoixJTableIconRenderer Methods
	//
	///////////////////////////////////

	public int
	getRendererType() {
	    return(YOIX_ICON_TYPE);
	}


	public final YoixJTableCellRenderer
	makeCopy() {

	    YoixJTableIconRenderer  renderer = new YoixJTableIconRenderer();

	    renderer.setDefaultHorizontalAlignment(getDefaultHorizontalAlignment());
	    return(renderer);
	}


	public final void
	resetDefaultHorizontalAlignment() {

	    ALIGNMENT = SwingConstants.CENTER;
	}


	public final void
	setValue(Object value) {

	    if (value == null || !(value instanceof YoixJTableIcon)) {
		setIcon(null);
		setText("");
	    } else if (((YoixJTableIcon)value).isComplete()) {
		setIcon((Icon)value);
		setText("");
	    } else {
		setIcon(null);
		setText(value.toString());
	    }
	}


	public final String
	stringValue(Object value, boolean formatted) {

	    if (value == null || !(value instanceof YoixJTableIcon))
		value = "";
	    return(value.toString());
	}

	public String getRendererLocale() { return(null); };
	public void   setRendererLocale(String locale) {};
	public String getInputLocale() { return(null); };
	public void   setInputLocale(String locale) {};
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableIntegerRenderer extends YoixJTableNumberRenderer {

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableIntegerRenderer() {

	    super(NUMBER_INSTANCE);
	}

	///////////////////////////////////
	//
	// YoixJTableIntegerRenderer Methods
	//
	///////////////////////////////////

	public int
	getRendererType() {
	    return(YOIX_INTEGER_TYPE);
	}


	public final void
	setValue(Object value) {

	    long    val;
	    String  preview = stringPreview(value);

	    if (preview == null) {
		val = ((Number)value).longValue();

		if (nozero && val == 0L)
		    preview = "";
		else if (val > (long)overflow) {
		    preview = nf.format((long)overflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0L && overflow >= 0)
			preview = "-" + preview;
		} else if (val < (long)underflow) {
		    preview = nf.format((long)underflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0L && underflow >= 0)
			preview = "-" + preview;
		} else if (compact)
		    preview = compactString(val);
		else preview = nf.format(val);
	    }

	    setText(preview);
	}


	public final String
	stringValue(Object value, boolean formatted) {

	    String preview;

	    return(((preview = stringPreview(value)) != null) ? preview : formatted ? (compact ? compactString(((Number)value).longValue()) : nf.format(((Number)value).longValue())) : value.toString());
	}

	protected final String
	stringPreview(Object value) {

	    return((value == null || !(value instanceof Number)) ? "" : getReverseSubstitute(value));
	}


	public void
	setHighSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    Integer  value;
	    int      m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new Integer(Integer.MAX_VALUE - m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    hisubsti = substi;
	}


	public void
	setLowSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    Integer  value;
	    int      m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new Integer(Integer.MIN_VALUE + m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    losubsti = substi;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableMoneyRenderer extends YoixJTableNumberRenderer {

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableMoneyRenderer() {

	    super(CURRENCY_INSTANCE);
	}

	///////////////////////////////////
	//
	// YoixJTableMoneyRenderer Methods
	//
	///////////////////////////////////

	public int
	getRendererType() {
	    return(YOIX_MONEY_TYPE);
	}


	public final void
	setValue(Object value) {

	    double  val;
	    String  preview = stringPreview(value);

	    if (preview == null) {
		val = ((Number)value).doubleValue();

		if (nozero && val == 0)
		    preview = "";
		else if (val > overflow) {
		    preview = nf.format(overflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0 && overflow >= 0)
			preview = "-" + preview;
		} else if (val < underflow) {
		    preview = nf.format(underflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0 && underflow >= 0)
			preview = "-" + preview;
		} else preview = nf.format(val);
	    }

	    setText(preview);
	}

	public final String
	stringValue(Object value, boolean formatted) {

	    String preview;

	    return(((preview = stringPreview(value)) != null) ? preview : formatted ? nf.format(((Number)value).doubleValue()) : value.toString());
	}


	public void
	setHighSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    YoixJTableMoney   value;
	    double   m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new YoixJTableMoney(Double.MAX_VALUE - m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    hisubsti = substi;
	}


	public void
	setLowSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    YoixJTableMoney   value;
	    double   m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new YoixJTableMoney(-Double.MAX_VALUE + m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    losubsti = substi;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableObjectRenderer extends YoixJTableBaseRenderer {

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableObjectRenderer() {

	    super();
	}

	///////////////////////////////////
	//
	// YoixJTableObjectRenderer Methods
	//
	///////////////////////////////////

	public final YoixJTableCellRenderer
	makeCopy() {

	    YoixJTableObjectRenderer  renderer = new YoixJTableObjectRenderer();

	    renderer.setDefaultHorizontalAlignment(getDefaultHorizontalAlignment());
	    return(renderer);
	}


	public final void
	setValue(Object value) {

	    setText((value == null || !(value instanceof YoixJTableObject)) ? "" : ((YoixJTableObject)value).stringValue());
	}


	public final String
	stringValue(Object value, boolean formatted) {

	    return((value == null || !(value instanceof YoixJTableObject)) ? "" : ((YoixJTableObject)value).stringValue());
	}

	public String getRendererLocale() { return(null); };
	public void   setRendererLocale(String locale) {};
	public String getInputLocale() { return(null); };
	public void   setInputLocale(String locale) {};
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTablePercentRenderer extends YoixJTableNumberRenderer {

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTablePercentRenderer() {

	    super(PERCENT_INSTANCE);
	}

	///////////////////////////////////
	//
	// YoixJTablePercentRenderer Methods
	//
	///////////////////////////////////

	public int
	getRendererType() {
	    return(YOIX_PERCENT_TYPE);
	}


	public final void
	setValue(Object value) {

	    double  val;
	    String  preview = stringPreview(value);

	    if (preview == null) {
		val = ((Number)value).doubleValue();

		if (nozero && val == 0)
		    preview = "";
		else if (val > overflow) {
		    preview = nf.format(overflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0 && overflow >= 0)
			preview = "-" + preview;
		} else if (val < underflow) {
		    preview = nf.format(underflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0 && underflow >= 0)
			preview = "-" + preview;
		} else preview = nf.format(val);
	    }

	    setText(preview);
	}


	public void
	setHighSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    YoixJTablePercent   value;
	    double   m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new YoixJTablePercent(Double.MAX_VALUE - m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    hisubsti = substi;
	}


	public void
	setLowSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    YoixJTablePercent   value;
	    double   m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new YoixJTablePercent(-Double.MAX_VALUE + m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    losubsti = substi;
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableStringRenderer extends YoixJTableBaseRenderer {

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableStringRenderer() {

	    super();
	}

	///////////////////////////////////
	//
	// YoixJTableStringRenderer Methods
	//
	///////////////////////////////////

	public final YoixJTableCellRenderer
	makeCopy() {

	    YoixJTableStringRenderer  renderer = new YoixJTableStringRenderer();

	    renderer.setDefaultHorizontalAlignment(getDefaultHorizontalAlignment());
	    return(renderer);
	}


	public final void
	setValue(Object value) {

	    setText((value == null || !(value instanceof String)) ? "" : (String)value);
	}


	public final String
	stringValue(Object value, boolean formatted) {

	    return((value == null || !(value instanceof String)) ? "" : (String)value);
	}

	public String getRendererLocale() { return(null); };
	public void   setRendererLocale(String locale) {};
	public String getInputLocale() { return(null); };
	public void   setInputLocale(String locale) {};
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableTextRenderer extends YoixSwingJTextPane

	implements TableCellRenderer,
	YoixJTableCellRenderer

    {

	String  format = "plain";
	String  inputformat = "^"; // replacement substring for NL
	int     ALIGNMENT = SwingConstants.LEFT;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableTextRenderer() {

	    super();
	    resetDefaultHorizontalAlignment();
	}

	///////////////////////////////////
	//
	// YoixJTableTextRenderer Methods
	//
	///////////////////////////////////


	public final boolean
	getDecimalSeparatorAlwaysShown() {

	    return(false);
	}


	public final int
	getDefaultAlignment() {

	    int  align;

	    switch (ALIGNMENT) {
	    case SwingConstants.RIGHT:
		align = YOIX_RIGHT;
		break;

	    case SwingConstants.CENTER:
		align = YOIX_CENTER;
		break;

	    case SwingConstants.LEFT:
		align = YOIX_LEFT;
		break;

	    default:
		VM.abort(INTERNALERROR);
		align = -1;
		break;
	    }
	    return(align);
	}


	public final int
	getDefaultHorizontalAlignment() {

	    return(ALIGNMENT);
	}


	public final String
	getFormat() {

	    EditorKit editor = getEditorKit();
	    String    format;

	    if (editor instanceof HTMLEditorKit)
		format = "html";
	    else if (editor instanceof javax.swing.text.rtf.RTFEditorKit)
		format = "rtf";
	    else format = "plain";

	    return(format);
	}


	public final String
	getInputFormat() {

	    return(inputformat);
	}


	public final int
	getGroupingSize() {

	    return(-1);
	}


	public final boolean
	getGroupingUsed() {

	    return(false);
	}


	public final int
	getMaximumFractionDigits() {

	    return(-1);
	}


	public final int
	getMaximumIntegerDigits() {

	    return(-1);
	}


	public final int
	getMinimumFractionDigits() {

	    return(-1);
	}


	public final int
	getMinimumIntegerDigits() {

	    return(-1);
	}


	public final int
	getMultiplier() {

	    return(-1);
	}


	public final String
	getNegativePrefix() {

	    return(null);
	}


	public final String
	getNegativeSuffix() {

	    return(null);
	}


	public final NumberFormat
	getNumberInputFormat()
	    throws java.text.ParseException
	{

	    if (true)
		throw new java.text.ParseException("no NumberFormat available", -1);
	    return(null);
	}


	public YoixJTableNumberRenderer
	getNumberRenderer()
	    throws java.text.ParseException
	{

	    if (true)
		throw new java.text.ParseException("not a YoixJTableNumberRenderer", -1);
	    return(null);
	}


	public final boolean
	getZeroNotShown() {

	    return(false);
	}


	public final boolean
	getParseIntegerOnly() {

	    return(false);
	}


	public final String
	getPositivePrefix() {

	    return(null);
	}


	public final String
	getPositiveSuffix() {

	    return(null);
	}


	public int
	getRendererType() {
	    return(YOIX_TEXT_TYPE);
	}


	public final Component
	getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

	    YoixSwingJTable       ytable = (YoixSwingJTable)table;
	    YoixSwingTableColumn  tblcol;
	    YoixSwingTableColumn  tmpcol;
	    TableColumnModel      columnModel;
	    YoixJTableModel       yjtm;
	    String                tip;
	    Integer               rowint;
	    Insets                insets;
	    Enumeration           columns;
	    Color                 fore = null;
	    Color                 back = null;
	    Color                 colormatrix[][][];
	    Color                 cellfore[];
	    Color                 cellback[];
	    Color                 cellselectionfore[];
	    Color                 cellselectionback[];
	    Color                 color;
	    Border                border;
	    Boolean               rowedit;
	    int                   state;
	    int                   height;
	    int                   maxht;
	    int                   rowht;
	    int                   idx = -1;
	    int                   midx;
	    int                   ridx;
	    boolean               isFound = (row == foundRow && column == foundColumn);

	    columnModel = table.getColumnModel();
	    tblcol = (YoixSwingTableColumn)(columnModel.getColumn(column));
	    yjtm = (YoixJTableModel)(ytable.getModel());

	    midx = tblcol.getModelIndex();
	    ridx = yjtm.getRowIndex(row);

	    setFont(tblcol.getFont(table.getFont()));

	    setHorizontalAlignment(tblcol.getHorizontalAlignment(getDefaultHorizontalAlignment()));

	    if (hasFocus) {
		border = UIManager.getBorder("Table.focusCellHighlightBorder");
		if (((YoixSwingJTable)table).getUseEditHighlight() && table.isCellEditable(row, column)) {
		    //
		    // The changes that use cellfore and cellback were added
		    // on 10/28/10 - previous version always used UIManager
		    // colors.
		    //
		    if ((cellfore = tblcol.getCellForegrounds(null)) != null && cellfore.length > 0)
			fore = cellfore[ridx % cellfore.length];
		    if (fore == null)
			fore = UIManager.getColor("Table.focusCellForeground");
		    if ((cellback = tblcol.getCellBackgrounds(null)) != null && cellback.length > 0)
			back = cellback[ridx % cellback.length];
		    if (back == null)
			back = UIManager.getColor("Table.focusCellBackground");
		}
	    } else border = tblcol.getBorder(yjtm.noFocusBorder);

	    if ((height = ytable.getRowHeightAdjustment()) > 0)
		setBorder(new CompoundBorder(new EmptyBorder(height/2, 0, 0, 0), border));
	    else setBorder(border);

	    if (fore == null && ridx >= 0) {// fore == null ==> back == null
		if (isSelected) {
		    if ((cellselectionfore = tblcol.getCellSelectionForegrounds()) != null && cellselectionfore.length > 0)
			fore = cellselectionfore[ridx % cellselectionfore.length];
		    else if (fore == null)
			fore = tblcol.getSelectionForeground(table.getSelectionForeground());
		    if ((cellselectionback = tblcol.getCellSelectionBackgrounds()) != null && cellselectionback.length > 0)
			back = cellselectionback[ridx % cellselectionback.length];
		    else if (back == null)
			back = tblcol.getSelectionBackground(table.getSelectionBackground());
		} else if ((colormatrix = yjtm.getColorMatrix()) != null) {
		    // colormatrix already is arranged by appropriate row index
		    back = colormatrix[row][midx][0];
		    fore = colormatrix[row][midx][1];
		}

		if (fore == null) {
		    cellfore = tblcol.getCellForegrounds(yjtm.cellForegrounds);

		    while (fore == null) {
			if (cellfore != null && cellfore.length > 1) {
			    if (cellfore == null || cellfore.length == 0)
				cellfore = new Color[] {table.getForeground()};
			    if (cellfore != yjtm.cellForegrounds)
				fore = cellfore[ridx % cellfore.length];
			    else fore = cellfore[row % cellfore.length];
			    if (fore == null) {
				if (cellfore != yjtm.cellForegrounds) {
				    cellfore = yjtm.cellForegrounds;
				} else fore = table.getForeground();
			    }
			} else {
			    //
			    // This code was changed on 10/27/10 - see comments
			    // in YoixJTableBaseRenderer for more details.
			    //
			    if (cellfore != null && cellfore.length == 1)
				fore = cellfore[0];
			    if (fore == null)
				fore = table.getForeground();
			    break;
			}
		    }
		}

		if (back == null) {
		    cellback = tblcol.getCellBackgrounds(yjtm.cellBackgrounds);

		    while (back == null) {
			if (cellback != null && cellback.length > 1) {
			    if (cellback == null || cellback.length == 0)
				cellback = new Color[] {table.getBackground()};
			    if (cellback != yjtm.cellBackgrounds)
				back = cellback[ridx % cellback.length];
			    else back = cellback[row % cellback.length];
			    if (back == null) {
				if (cellback != yjtm.cellBackgrounds) {
				    cellback = yjtm.cellBackgrounds;
				} else back = table.getBackground();
			    }
			} else {
			    //
			    // This code was changed on 10/27/10 - see comments
			    // in YoixJTableBaseRenderer for more details.
			    //
			    if (cellback != null && cellback.length == 1)
				back = cellback[0];
			    if (back == null)
				back = table.getBackground();
			    break;
			}
		    }
		}
	    }

	    if (!isCellEditable(row, column)) {
		if ((color = tblcol.getDisabledForeground(null)) != null)
		    fore = color;
		if ((color = tblcol.getDisabledBackground(null)) != null)
		    back = color;
	    }

	    if (isFound) {
		setForeground(back);
		setBackground(fore);
	    } else {
		setForeground(fore);
		setBackground(back);
	    }

	    //if (row == 0) System.err.println("midx="+midx+";type="+YOIX_TEXT_TYPE);
	    rowht = ytable.getRowHeight(row);
	    //if (row == 0) System.err.println("rowht="+rowht);
	    maxht = ytable.getRowHeight();
	    //if (row == 0) System.err.println("maxht0="+maxht);
	    columns = columnModel.getColumns();
	    rowint = new Integer(ridx);
	    while (columns.hasMoreElements()) {
		tmpcol = (YoixSwingTableColumn)(columns.nextElement());
		//if (row == 0) System.err.println("col="+tmpcol.getModelIndex()+";type="+tmpcol.getType());
		if (tmpcol.getModelIndex() != midx && tmpcol.getType() == YOIX_TEXT_TYPE) {
		    height = tmpcol.getRowHeightInfo(rowint);
		    //if (row == 0) System.err.println("height="+height);
		    if (height > maxht)
			maxht = height;
		}
	    }
	    //if (row == 0) System.err.println("maxht1="+maxht);

	    setValue(value);
	    insets = getBorder().getBorderInsets(this);
	    setSize(tblcol.getWidth() - insets.left - insets.right + 1, getPreferredSize().width * getPreferredSize().height);
	    height = getPreferredSize().height;
	    //if (row == 0) System.err.println("ht0="+height);
	    if (height < ytable.getRowHeight())
		height = ytable.getRowHeight();
	    //if (row == 0) System.err.println("ht1="+height);
	    tblcol.setRowHeightInfo(rowint, new Integer(height));
	    if (height > maxht)
		maxht = height;
	    //if (row == 0) System.err.println("maxht2="+maxht);
	    if (height > rowht) {
		table.setRowHeight(row, height);
		//if (row == 0) System.err.println("set0="+height);
	    } else if (maxht < rowht) {
		table.setRowHeight(row, maxht);
		//if (row == 0) System.err.println("set1="+maxht);
	    }

	    tip = tblcol.getTipText(ridx, midx, yjtm.getTipText(ridx, midx));
	    if (tip != null && tip.length() > 0 && table.getToolTipText() != null) {
		this.setToolTipText(tip);
	    } else this.setToolTipText(null);

	    
	    return(this);
	}


	public final YoixJTableCellRenderer
	makeCopy() {

	    YoixJTableTextRenderer  renderer = new YoixJTableTextRenderer();

	    renderer.setDefaultHorizontalAlignment(getDefaultHorizontalAlignment());
	    return(renderer);
	}


	public final void
	resetDefaultHorizontalAlignment() {

	    ALIGNMENT = SwingConstants.LEFT;
	}


	public final void
	setDecimalSeparatorAlwaysShown(boolean value) {

	}


	public final void
	setDefaultHorizontalAlignment(int alignment) {

	    switch (alignment) {
	    case SwingConstants.RIGHT:
	    case SwingConstants.CENTER:
	    case SwingConstants.LEFT:
		ALIGNMENT = alignment;
		break;

	    default:
		resetDefaultHorizontalAlignment();
		break;
	    }
	}



	public final void
	setFormat(String format) {

	    StyledEditorKit  editor;

	    if (format == null)
		format = "plain";
	    else format = format.trim().toLowerCase();
	    if (format.equals("rtf"))
		editor = new javax.swing.text.rtf.RTFEditorKit();
	    else if (format.equals("html"))
		editor = new HTMLEditorKit();
	    else editor = new StyledEditorKit();
	    setEditorKit(editor);
	}


	public final void
	setInputFormat(String format) {

	    inputformat = format;
	}


	public final void
	setGroupingSize(int value) {

	}


	public final void
	setGroupingUsed(boolean value) {

	}


	public final void
	setMaximumFractionDigits(int value) {

	}


	public final void
	setMaximumIntegerDigits(int value) {

	}


	public final void
	setMinimumFractionDigits(int value) {

	}


	public final void
	setMinimumIntegerDigits(int value) {

	}


	public final void
	setMultiplier(int value) {

	}


	public final void
	setNegativePrefix(String value) {

	}


	public final void
	setNegativeSuffix(String value) {

	}


	public final void
	setZeroNotShown(boolean value) {

	}


	public final void
	setParseIntegerOnly(boolean value) {

	}


	public final void
	setPositivePrefix(String value) {

	}


	public final void
	setPositiveSuffix(String value) {

	}


	public final void
	setValue(Object value) {

	    setText(value == null ? "" : value.toString().replace(getInputFormat(),NL));
	}


	public final String
	stringValue(Object value, boolean formatted) {

	    return(value == null ? "" : value.toString().replaceAll("[\r\n]+",getInputFormat()));
	}

	public String[] getLowSubstitute() { return(null); };
	public String[] getHighSubstitute() { return(null); };
	public Object getSubstitute(String value) { return(null); };
	public String getReverseSubstitute(Object value) { return(null); };
	public double getOverflow() { return(Double.MAX_VALUE); };
	public double getUnderflow() { return(-Double.MAX_VALUE); };
	public void setLowSubstitute(String[] value) {};
	public void setHighSubstitute(String[] value) {};
	public void setOverflow(double value) {};
	public void setUnderflow(double value) {};

	public String getRendererLocale() { return(null); };
	public void   setRendererLocale(String locale) {};
	public String getInputLocale() { return(null); };
	public void   setInputLocale(String locale) {};
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableTimerRenderer extends YoixJTableBaseRenderer {

	private String  format;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableTimerRenderer() {

	    super();
	    resetDefaultHorizontalAlignment();
	    format = TIMER_FORMAT;
	}

	///////////////////////////////////
	//
	// YoixJTableTimerRenderer Methods
	//
	///////////////////////////////////

	public int
	getRendererType() {
	    return(YOIX_TIMER_TYPE);
	}


	public final String
	getFormat() {

	    return(format);
	}


	public final YoixJTableCellRenderer
	makeCopy() {

	    YoixJTableTimerRenderer  renderer = new YoixJTableTimerRenderer();

	    renderer.setDefaultHorizontalAlignment(getDefaultHorizontalAlignment());
	    renderer.setFormat(getFormat());
	    renderer.setLowSubstitute(getLowSubstitute());
	    renderer.setHighSubstitute(getHighSubstitute());
	    renderer.setOverflow(getOverflow());
	    renderer.setUnderflow(getUnderflow());
	    return(renderer);
	}


	public final void
	resetDefaultHorizontalAlignment() {

	    ALIGNMENT = SwingConstants.RIGHT;
	}


	public final void
	setFormat(String format) {

	    this.format = format;
	}


	public final void
	setValue(Object value) {

	    double  val;
	    String  preview = stringPreview(value);

	    if (preview == null) {
		val = ((YoixJTableTimer)value).doubleValue();

		if (nozero && val == 0)
		    preview = "";
		else if (val > overflow) {
		    preview = YoixMiscTime.timerFormat(format, overflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0 && overflow >= 0)
			preview = "-" + preview;
		} else if (val < underflow) {
		    preview = YoixMiscTime.timerFormat(format, underflow);
		    preview = preview.replaceAll("[0-9]", "*");
		    if (val < 0 && underflow >= 0)
			preview = "-" + preview;
		} else preview = YoixMiscTime.timerFormat(format, val);
	    }

	    setText(preview);
	}

	public final String
	stringValue(Object value, boolean formatted) {

	    String preview;

	    return(((preview = stringPreview(value)) != null) ? preview : YoixMiscTime.timerFormat(format, ((YoixJTableTimer)value).doubleValue()));
	}

	private final String
	stringPreview(Object value) {

	    return((value == null || !(value instanceof YoixJTableTimer) || (value instanceof YoixJTableTimer && ((YoixJTableTimer)value).isNaN())) ? "" : getReverseSubstitute(value));
	}


	public void
	setHighSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    YoixJTableTimer   value;
	    double   m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new YoixJTableTimer(Double.MAX_VALUE - m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    hisubsti = substi;
	}


	public void
	setLowSubstitute(String[] substi) {

	    HashMap  submap = substimap;
	    HashMap  subrev = substirev;
	    YoixJTableTimer   value;
	    double   m;
	    int      n;

	    if (substi != null) {
		if (submap == null) {
		    submap = new HashMap(substi.length);
		    subrev = new HashMap(substi.length);
		}
		for (m = 0, n = 0; n < substi.length; n++) {
		    if (substi[n] != null) {
			if (!submap.containsKey(substi[n])) {
			    value = new YoixJTableTimer(-Double.MAX_VALUE + m++);
			    submap.put(substi[n], value);
			    subrev.put(value,     substi[n]);
			}
		    }
		}
		
	    }
	    substimap = submap;
	    substirev = subrev;
	    losubsti = substi;
	}

	public String getRendererLocale() { return(null); };
	public void   setRendererLocale(String locale) {};
	public String getInputLocale() { return(null); };
	public void   setInputLocale(String locale) {};
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableCellEditor extends DefaultCellEditor implements KeyListener {

	boolean  doValidation = true;
	Object   oldvalue = null;
	int      comptype;
	int      rowval;
	int      colval;

	final static int JCOMPONENT = 0;
	final static int JTEXTFIELD = 1;
	final static int JTEXTPANE  = 2;
	final static int JCHECKBOX  = 3;
	final static int JCOMBOBOX  = 4;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	public
	YoixJTableCellEditor(JCheckBox comp) {

	    super(comp);
	    setup(comp);
	}


	public
	YoixJTableCellEditor(JComboBox comp) {

	    super(comp);
	    setup(comp);
	}


	public
	YoixJTableCellEditor(JTextField comp) {

	    super(comp);
	    setup(comp);
	}


	public
	YoixJTableCellEditor(JTextPane comp) {

	    super(new JTextField()); // use JTextField, we fix things in setup
	    setup(comp);
	}

	///////////////////////////////////
	//
	// YoixJTableCellEditor Methods
	//
	///////////////////////////////////

	public Component
	getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {

	    YoixSwingJTable        table = (YoixSwingJTable)tbl;
	    YoixSwingTableColumn   tblcol;
	    YoixJTableCellRenderer renderer;
	    TableColumnModel       columnModel;
	    YoixJTableModel        yjtm;
	    int                    midx;

	    columnModel = table.getColumnModel();
	    tblcol = (YoixSwingTableColumn)(columnModel.getColumn(column));
	    yjtm = (YoixJTableModel)(((YoixSwingJTable)table).getModel());
	    midx = tblcol.getModelIndex();

	    editorComponent.setFont(tblcol.getFont(table.getFont()));

	    setErrorColor(Color.black);
	    setRowColVal(row, column, value);
	    //((JComponent)getComponent()).setBorder(new LineBorder(Color.black));

	    renderer = (YoixJTableCellRenderer)table.getCellRenderer(row, column);
	    setHorizontalAlignment(tblcol.getHorizontalAlignment(renderer.getDefaultHorizontalAlignment()));

	    delegate.setValue(value);


	    editorComponent.setForeground(tblcol.getEditForeground(table.getEditForeground(editorComponent.getForeground())));
	    editorComponent.setBackground(tblcol.getEditBackground(table.getEditBackground(editorComponent.getBackground())));
	    return(editorComponent);
	}


	final void
	setErrorColor(Color color) {

	    switch (comptype) {
	    case JTEXTFIELD:
		getComponent().setForeground(color);
		break;

	    case JTEXTPANE:
		getComponent().setForeground(color);
		break;

	    case JCHECKBOX:
		getComponent().setBackground(color);
		break;

	    case JCOMBOBOX:
		((YoixSwingJComboBox)getComponent()).setEditorForeground(color);
		LookAndFeel.uninstallBorder(((YoixSwingJComboBox)getComponent()));
		break;

	    default:
		VM.abort(INTERNALERROR);
	    }
	}


	final void
	setHorizontalAlignment(int alignment) {

	    switch (comptype) {
	    case JCHECKBOX:
		((JCheckBox)editorComponent).setHorizontalAlignment(alignment);
		break;

	    case JCOMBOBOX:
		((YoixSwingJComboBox)editorComponent).setHorizontalAlignment(alignment);
		break;

	    case JTEXTFIELD:
		((JTextField)editorComponent).setHorizontalAlignment(alignment);
		break;

	    case JTEXTPANE:
		((YoixSwingJTextPane)(((JScrollPane)editorComponent).getViewport().getView())).setHorizontalAlignment(alignment);
		break;
	    }
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	private void
	setRowColVal(int row, int col, Object val) {

	    rowval = row;
	    colval = col;
	    oldvalue = val;
	}


	private void
	setup(JComponent comp) {

	    setClickCountToStart(data.getInt(N_CLICKCOUNT, 1));

	    if (comp instanceof JTextField) {
		comptype = JTEXTFIELD;
		//((JTextField)comp).removeActionListener(delegate);
		//((JTextField)comp).addActionListener(this);
	    } else if (comp instanceof YoixSwingJTextPane) {
		comptype = JTEXTPANE;
	    } else if (comp instanceof JCheckBox) {
		comptype = JCHECKBOX;
		//((JCheckBox)comp).removeActionListener(delegate);
		//((JCheckBox)comp).addActionListener(this);
	    } else if (comp instanceof YoixSwingJComboBox) {
		comptype = JCOMBOBOX;
		//((JComboBox)comp).removeActionListener(delegate);
		//((JComboBox)comp).addActionListener(this);
	    } else VM.abort(INTERNALERROR);

	    comp.addKeyListener(this);
	}

	///////////////////////////////////
	//
	// KeyListener
	//
	///////////////////////////////////

	public void
	keyPressed(KeyEvent e) {

	    EventQueue   queue;
	    YoixObject   obj;
	    AWTEvent     event;
	    Point        point;
	    char         keychar;		// was int which messed up N_KEYSTRING
	    int          viewColumn;
	    int          viewRow;

	    if (editKeyListenerMode) {
		viewColumn = columnAtPoint(point = ((Component)e.getSource()).getLocation());
		viewRow = rowAtPoint(point);

		keychar = ((KeyEvent)e).getKeyChar();

		obj = YoixMake.yoixType(T_INVOCATIONEVENT);

		obj.putInt(N_ID, V_INVOCATIONEDITKEY);

		obj.putInt("viewColumn", viewColumn);
		viewColumn = yoixConvertColumnIndexToModel(viewColumn);
		obj.putInt("valuesColumn", viewColumn);

		obj.putInt("viewRow", viewRow);
		viewRow = yoixConvertRowIndexToModel(viewRow);
		obj.putInt("valuesRow", viewRow);

		obj.putInt(N_KEYEVENT, V_KEYPRESSED);
		obj.putInt(N_KEYCHAR, keychar != KeyEvent.CHAR_UNDEFINED ? keychar : -1);
		obj.putInt(N_KEYCODE, ((KeyEvent)e).getKeyCode());
		obj.putString(N_KEYSTRING, keychar != KeyEvent.CHAR_UNDEFINED ? "" + keychar : "");
		obj.putInt(N_MODIFIERS, YoixMiscJFC.cookModifiers((KeyEvent)e));
		obj.putInt(N_MODIFIERSDOWN, ((KeyEvent)e).getModifiersEx());
		obj.putDouble(N_WHEN, ((KeyEvent)e).getWhen()/1000.0);

		event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
		if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
		    queue.postEvent(event);

	    }

	    //
	    // The following provides similar behavior to VK_TAB (but vertically) when editing
	    // (As with VK_TAB, this will not affect TEXT_TYPE cells).
	    //
	    // NOTE - posting the event without knowing whether the editing was successfully
	    // stopped can lead to an infinite loop when we're editing and there's a validator
	    // function that returns false. Changed yjtm.stopEditing so it returns a boolean
	    // that indicates whether the request to stop editing was successful or not.
	    //
	    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		if (YoixSwingJTable.this.yjtm.stopEditing()) {
		    event = new KeyEvent(YoixSwingJTable.this, e.getID(), System.currentTimeMillis(), e.getModifiers(), e.getKeyCode(), e.getKeyChar());
		    if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
			queue.postEvent(event);
		}
	    }
	}


	public void
	keyReleased(KeyEvent e) {

	    EventQueue   queue;
	    YoixObject   obj;
	    AWTEvent     event;
	    Point        point;
	    char         keychar;		// was int which messed up N_KEYSTRING
	    int          viewColumn;
	    int          viewRow;

	    if (editKeyListenerMode) {
		viewColumn = columnAtPoint(point = ((Component)e.getSource()).getLocation());
		viewRow = rowAtPoint(point);

		keychar = ((KeyEvent)e).getKeyChar();

		obj = YoixMake.yoixType(T_INVOCATIONEVENT);

		obj.putInt(N_ID, V_INVOCATIONEDITKEY);

		obj.putInt("viewColumn", viewColumn);
		viewColumn = yoixConvertColumnIndexToModel(viewColumn);
		obj.putInt("valuesColumn", viewColumn);

		obj.putInt("viewRow", viewRow);
		viewRow = yoixConvertRowIndexToModel(viewRow);
		obj.putInt("valuesRow", viewRow);

		obj.putInt(N_KEYEVENT, V_KEYRELEASED);
		obj.putInt(N_KEYCHAR, keychar != KeyEvent.CHAR_UNDEFINED ? keychar : -1);
		obj.putInt(N_KEYCODE, ((KeyEvent)e).getKeyCode());
		obj.putString(N_KEYSTRING, keychar != KeyEvent.CHAR_UNDEFINED ? "" + keychar : "");
		obj.putInt(N_MODIFIERS, YoixMiscJFC.cookModifiers((KeyEvent)e));
		obj.putInt(N_MODIFIERSDOWN, ((KeyEvent)e).getModifiersEx());
		obj.putDouble(N_WHEN, ((KeyEvent)e).getWhen()/1000.0);

		event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
		if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
		    queue.postEvent(event);
	    }
	}


	public void
	keyTyped(KeyEvent e) {

	    EventQueue   queue;
	    YoixObject   obj;
	    AWTEvent     event;
	    Point        point;
	    char         keychar;		// was int which messed up N_KEYSTRING
	    int          viewColumn;
	    int          viewRow;

	    if (editKeyListenerMode) {
		viewColumn = columnAtPoint(point = ((Component)e.getSource()).getLocation());
		viewRow = rowAtPoint(point);

		keychar = ((KeyEvent)e).getKeyChar();

		obj = YoixMake.yoixType(T_INVOCATIONEVENT);

		obj.putInt(N_ID, V_INVOCATIONEDITKEY);

		obj.putInt("viewColumn", viewColumn);
		viewColumn = yoixConvertColumnIndexToModel(viewColumn);
		obj.putInt("valuesColumn", viewColumn);

		obj.putInt("viewRow", viewRow);
		viewRow = yoixConvertRowIndexToModel(viewRow);
		obj.putInt("valuesRow", viewRow);

		obj.putInt(N_KEYEVENT, V_KEYTYPED);
		obj.putInt(N_KEYCHAR, keychar != KeyEvent.CHAR_UNDEFINED ? keychar : -1);
		obj.putInt(N_KEYCODE, ((KeyEvent)e).getKeyCode());
		obj.putString(N_KEYSTRING, keychar != KeyEvent.CHAR_UNDEFINED ? "" + keychar : "");
		obj.putInt(N_MODIFIERS, YoixMiscJFC.cookModifiers((KeyEvent)e));
		obj.putInt(N_MODIFIERSDOWN, ((KeyEvent)e).getModifiersEx());
		obj.putDouble(N_WHEN, ((KeyEvent)e).getWhen()/1000.0);

		event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
		if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
		    queue.postEvent(event);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableBooleanEditor extends YoixJTableCellEditor {

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableBooleanEditor() {
	    super(new JCheckBox());

	    final JCheckBox jCheckBox = (JCheckBox)getComponent();

	    delegate = new EditorDelegate() {
		public void setValue(Object value) {
		    String trimmed;
		    boolean selected = false;
		    if (value instanceof Boolean) {
			selected = ((Boolean)value).booleanValue();
		    }
		    else if (value instanceof String) {
			if (value != null) {
			    trimmed = ((String)value).trim();
			    selected = "true".equalsIgnoreCase(trimmed) ||
				"yes".equalsIgnoreCase(trimmed) ||
				"1".equals(trimmed);
			}
		    }
		    this.value = selected ? Boolean.TRUE : Boolean.FALSE;
		    jCheckBox.setSelected(selected);
		}

		public Object getCellEditorValue() {
		    return(jCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE);
		}
	    };
	}

	YoixJTableBooleanEditor(YoixSwingJComboBox jcb) {

	    this(); // ignore combo box request
	}

	///////////////////////////////////
	//
	// YoixJTableBooleanEditor Methods
	//
	///////////////////////////////////

	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;
	    Object      value;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    value = new Boolean(editval);

	    if (isCellValid(rowval, colval, YOIX_BOOLEAN_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableDateEditor extends YoixJTableCellEditor {

	private YoixSimpleDateFormat  sdf;
	Object                        value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableDateEditor() {

	    super(new JTextField());
	}


	YoixJTableDateEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableDateEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    TableCellRenderer rend = table.getCellRenderer(row, column);

	    // can assume the following
	    sdf = ((YoixJTableDateRenderer)rend).getSimpleDateFormat();
	    this.value = null;

	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    if (sdf != null) {
		if ("".equals(editval) == false) {
		    try {
			// sdf is set in getTableCellEditorComponent
			value = sdf.parse(editval);
		    }
		    catch(java.text.ParseException pe) {
			//((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
			setErrorColor(Color.red);
			return(false);
		    }
		} else value = null;

		if (isCellValid(rowval, colval, YOIX_DATE_TYPE, value, oldvalue) == false) {
		    setErrorColor(Color.red);
		    return(false);
		}
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableDoubleEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableDoubleEditor() {

	    super(new JTextField());
	}


	YoixJTableDoubleEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableDoubleEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    if ("".equals(editval) == false) {
		try {
		    value = Double.valueOf(editval);
		}
		catch(NumberFormatException nfe) {
		    //((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
		    setErrorColor(Color.red);
		    return(false);
		}
	    } else value = null;

	    if (isCellValid(rowval, colval, YOIX_DOUBLE_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableObjectEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableObjectEditor() {

	    super(new JTextField());
	}


	YoixJTableObjectEditor(YoixSwingJTextField jtf) {

	    super(jtf);
	}


	YoixJTableObjectEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableObjectEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    value = editval;

	    if (isCellValid(rowval, colval, YOIX_OBJECT_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableHistogramEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableHistogramEditor() {

	    super(new JTextField());
	}


	YoixJTableHistogramEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableHistogramEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    if ("".equals(editval) == false) {
		try {
		    value = new YoixJTableHistogram(editval);
		}
		catch(NumberFormatException nfe) {
		    //((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
		    setErrorColor(Color.red);
		    return(false);
		}
	    } else value = null;

	    if (isCellValid(rowval, colval, YOIX_HISTOGRAM_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableIconEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableIconEditor() {

	    super(new JTextField());
	}


	YoixJTableIconEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableIconEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    value = ("".equals(editval) == false) ? new YoixJTableIcon(editval) : null;

	    if (isCellValid(rowval, colval, YOIX_ICON_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableIntegerEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableIntegerEditor() {

	    super(new JTextField());
	}


	YoixJTableIntegerEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableIntegerEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    if ("".equals(editval) == false) {
		try {
		    value = Integer.valueOf(editval);
		}
		catch(NumberFormatException nfe) {
		    //((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
		    setErrorColor(Color.red);
		    return(false);
		}
	    } else value = null;

	    if (isCellValid(rowval, colval, YOIX_INTEGER_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableMoneyEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableMoneyEditor() {

	    super(new JTextField());
	}


	YoixJTableMoneyEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableMoneyEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    if ("".equals(editval) == false) {
		try {
		    value = new YoixJTableMoney(editval, null);
		}
		catch(java.text.ParseException pe) {
		    //((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
		    setErrorColor(Color.red);
		    return(false);
		}
	    } else value = null;

	    if (isCellValid(rowval, colval, YOIX_MONEY_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTablePercentEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTablePercentEditor() {

	    super(new JTextField());
	}


	YoixJTablePercentEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTablePercentEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    if ("".equals(editval) == false) {
		try {
		    value = new YoixJTablePercent(editval, null);
		}
		catch(java.text.ParseException pe) {
		    //((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
		    setErrorColor(Color.red);
		    return(false);
		}
	    } else value = null;

	    if (isCellValid(rowval, colval, YOIX_PERCENT_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableStringEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableStringEditor() {

	    super(new JTextField());
	}


	YoixJTableStringEditor(YoixSwingJTextField jtf) {

	    super(jtf);
	}


	YoixJTableStringEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableStringEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    value = editval;

	    if (isCellValid(rowval, colval, YOIX_STRING_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableTextEditor extends YoixJTableCellEditor
    {

	Object  value;
	String  format;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableTextEditor() {

	    this(new YoixSwingJTextPane());
	}


	YoixJTableTextEditor(YoixSwingJTextPane jtp) {

	    super(jtp);

	    EditorKit    editor;
	    JScrollPane  scroller;

	    if (editorComponent instanceof JTextField) // should be the case
		((JTextField)editorComponent).removeActionListener((ActionListener)delegate);
	    editor = jtp.getEditorKit();
	    if (editor instanceof HTMLEditorKit)
		format = "html";
	    else if (editor instanceof javax.swing.text.rtf.RTFEditorKit)
		format = "rtf";
	    else format = "plain";

	    final YoixSwingJTextPane pane = (YoixSwingJTextPane)jtp; 

	    delegate = new EditorDelegate() {
		public void setValue(Object value) {
		    pane.setText(value == null ? "" : value.toString());
		}

		public Object getCellEditorValue() {
		    return (pane.getText());
		}
	    };

	    
	    scroller = new JScrollPane(pane);
	    scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	    scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    editorComponent = scroller;

	}


	YoixJTableTextEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}


	///////////////////////////////////
	//
	// YoixJTableTextEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    YoixJTableCellRenderer renderer;

	    this.value = null;
	    renderer = getColumnRenderer(column);
	    return(super.getTableCellEditorComponent(table, value.toString().replace(renderer.getInputFormat(),NL), isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixJTableCellRenderer renderer;
	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    renderer = getColumnRenderer(colval);
	    editval = editval.replaceAll("[\r\n]+",renderer.getInputFormat());

	    value = new YoixJTableText(editval, format);

	    if (isCellValid(rowval, colval, YOIX_TEXT_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}

	///////////////////////////////////
	//
	// KeyListener methods
	//
	///////////////////////////////////

	public void
	keyPressed(KeyEvent e) {

	    EventQueue   queue;

	    if (e.getKeyCode() == KeyEvent.VK_F2) {
		stopCellEditing();
		// create tab event (with current modifiers) to shift focus to appropriate cell
		e = new KeyEvent(YoixSwingJTable.this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), e.getModifiers(), KeyEvent.VK_TAB);
		if ((queue = YoixAWTToolkit.getSystemEventQueue()) != null)
		    queue.postEvent(e);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableTimerEditor extends YoixJTableCellEditor {

	Object  value;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableTimerEditor() {

	    super(new JTextField());
	}


	YoixJTableTimerEditor(YoixSwingJComboBox jcb) {

	    super(jcb);
	}

	///////////////////////////////////
	//
	// YoixJTableTimerEditor Methods
	//
	///////////////////////////////////

	public final Object
	getCellEditorValue() {

	    return(value);
	}


	public final Component
	getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

	    this.value = null;
	    return(super.getTableCellEditorComponent(table, value, isSelected, row, column));
	}


	public final boolean
	stopCellEditing() {

	    YoixJTableCellRenderer renderer;
	    YoixObject  yobj;
	    String      editval;
	    Object      rawval;

	    rawval = super.getCellEditorValue();

	    if (rawval == null)
		editval = "";
	    else if (rawval instanceof String)
		editval = (String)rawval;
	    else if (rawval instanceof YoixSwingLabelItem)
		editval = ((YoixSwingLabelItem)rawval).getValue();
	    else editval = rawval.toString();

	    if ("".equals(editval) == false) {
		renderer = getColumnRenderer(colval);
		try {
		    value = new YoixJTableTimer(editval, renderer);
		}
		catch(NumberFormatException nfe) {
		    if (renderer == null || (value = (Double)renderer.getSubstitute(editval)) == null) {
			if (editval == null || editval.trim().length() == 0)
			    value = null;
			else {
			    //((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
			    setErrorColor(Color.red);
			    return(false);
			}
		    }
		}
	    } else value = null;

	    if (isCellValid(rowval, colval, YOIX_TIMER_TYPE, value, oldvalue) == false) {
		setErrorColor(Color.red);
		return(false);
	    }

	    return(super.stopCellEditing());
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class YoixJTableIcon extends ImageIcon {

	private  String  source = null;
	private  boolean  complete = false;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixJTableIcon(String source) {

	    super();
	    setup(source);
	}

	YoixJTableIcon(Image image, String source, String description) {

	    super(image, description);
	    this.source = source;
	    this.complete = true;
	}

	///////////////////////////////////
	//
	// YoixJTableIcon Methods
	//
	///////////////////////////////////

	final boolean
	isComplete() {

	    return(complete);
	}


	public final String
	toString() {

	    return(source);
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	private void
	setup(String source) {

	    Image  image;
	    File   file;
	    URL    url;

	    this.source = source;
	    file = new File(source);
	    if (file.canRead()) {
		image = Toolkit.getDefaultToolkit().getImage(source);
		setDescription(YoixMisc.toYoixPath(source));
	    } else {
		try {
		    image = Toolkit.getDefaultToolkit().getImage(new URL(source));
		    setDescription(YoixMisc.toYoixURL(source));
		}
		catch(MalformedURLException mue) {
		    setDescription(source);
		    image = null;
		}
	    }

	    if (image != null) {
		setImage(image);
		if ((getImageLoadStatus()&MediaTracker.COMPLETE) == MediaTracker.COMPLETE)
		    complete = true;
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    public class YoixEditorTransferHandler extends TransferHandler {

	//
	// A kludge that's designed let us call a Yoix function after data
	// is imported into a Swing component that's being used as a cell
	// editor and the component didn't come from a Yoix Swing component.
	// Without something like this any changes to an editor made by DnD
	// or the Clipboard couldn't be noticed by Yoix scripts, which was
	// inconsistent when you realize that invocationEditKey() provides
	// a mechanism that Yoix script can use to notice many changes!!
	//
	// NOTE - if all cell editors came from Yoix components we might not
	// need this kludge. In fact in that case there's a chance that we
	// could even eliminate event handlers like invocationEditKey().
	//
	// NOTE - unfortunately protected methods, like createTransferable(),
	// can't be handled by this mechanism, but it's probably not a big
	// deal because we're really just interested in imported data.
	//

	private TransferHandler  transferhandler;
	private JComponent       editor = null;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	YoixEditorTransferHandler(JComponent editor) {

	    super();
	    this.editor = editor;
	    this.transferhandler = editor.getTransferHandler();
	}

	///////////////////////////////////
	//
	// YoixEditorTransferHandler Methods
	//
	///////////////////////////////////

	public boolean
	canImport(JComponent comp, DataFlavor[] transferFlavors) {

	    return(transferhandler.canImport(editor, transferFlavors));
	}


	public void
	exportAsDrag(JComponent comp, InputEvent e, int action) {

	    transferhandler.exportAsDrag(comp, e, action);
	}


	public void
	exportToClipboard(JComponent comp, Clipboard clip, int action) {

	    transferhandler.exportToClipboard(comp, clip, action);
	}


	public int
	getSourceActions(JComponent c) {

	    return(transferhandler.getSourceActions(c));
	}


	public Icon
	getVisualRepresentation(Transferable t) {

	    return(transferhandler.getVisualRepresentation(t));
	}


	public boolean
	importData(JComponent comp, Transferable t) {

	    YoixObject  obj;
	    EventQueue  queue;
	    AWTEvent    event;
	    boolean     imported;
	    Object      selected;
	    String      text;
	    int         row;
	    int         column;

	    if (imported = transferhandler.importData(comp, t)) {
		//
		// Eventually will want to do more here...
		//
		if (editImportListenerMode) {
		    if ((row = getEditingRow()) >= 0) {
			if ((column = getEditingColumn()) >= 0) {
			    obj = YoixMake.yoixType(T_INVOCATIONEVENT);
			    obj.putInt(N_ID, V_INVOCATIONEDITIMPORT);
			    obj.putInt("viewRow", row);
			    obj.putInt("valuesRow", yoixConvertRowIndexToModel(row));
			    obj.putInt("viewColumn", column);
			    obj.putInt("valuesColumn", yoixConvertColumnIndexToModel(column));

			    text = null;
			    if (editor instanceof JComboBox) {
				if ((selected = ((JComboBox)editor).getSelectedItem()) != null) {
				    if (selected instanceof String)
					text = (String)selected;
				    else if (selected instanceof YoixSwingLabelItem)	// probably impossible
					text = ((YoixSwingLabelItem)selected).getValue();
				}
			    } else if (editor instanceof JTextField)
				text = ((JTextField)editor).getText();
			    else if (editor instanceof JCheckBox)
				text = ((JCheckBox)editor).isSelected() ? "true" : "false";
			    obj.putString(N_TEXT, text);
			    obj.putObject(N_TRANSFERABLE, YoixDataTransfer.yoixTransferable(t, true));

			    event = YoixMakeEvent.javaAWTEvent(obj, parent.getContext());
			    if (event != null && (queue = YoixAWTToolkit.getSystemEventQueue()) != null)
				queue.postEvent(event);
			}
		    }
		}
		comp.requestFocus();
	    }
	    return(imported);
	}
    }
}
