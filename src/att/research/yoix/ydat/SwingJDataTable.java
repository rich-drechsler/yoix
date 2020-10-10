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

package att.research.yoix.ydat;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.plaf.UIResource;
import javax.swing.table.*;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.basic.BasicTableUI;
import att.research.yoix.*;

class SwingJDataTable extends YoixSwingJTable

    implements ActionListener,
	       ChangeListener,
	       ComponentListener,
	       Constants,
	       DataTable,
	       ItemSelectable,		// available in YoixSwingJTextComponent
	       MouseListener,
	       MouseMotionListener,
	       MouseWheelListener,
	       SweepFilter,
	       YoixInterfaceShowing

{

    private YoixBodyComponentSwing  parent;
    private YoixObject              data;	// currently unused

    //
    // This class was slowly transformed from our SwingJHistogram class
    // into a new class that eventually will extend a JTable and use it
    // to dislay data. Main goal was to replace the special Histogram we
    // used as a "plot filter", but there may very well be other uses.
    //

    private DataManager  datamanager = null;
    private DataRecord   datarecords[];
    private DataRecord   loadeddata[];
    private TableData    datatable[];
    private Hashtable    colormap;
    private HashMap      tablemap;
    private Palette      currentpalette = null;

    private boolean  fastlookups[] = null;
    private int      fieldmasks[] = null;
    private int      selectmasks[] = null;
    private int      fieldindices[] = null;
    private int      valueindices[] = null;
    private int      partitionindices[] = null;
    private int      primaryfield = 0;
    private int      activefieldcount = 0;
    private int      active = 0;

    private int      visiblerowcount = -1;

    private boolean  accumulate = false;
    private boolean  alive = true;
    private boolean  autoready = false;
    private boolean  autoshow = false;
    private boolean  listening = false;
    private boolean  painted = false;
    private boolean  intransient = false;
    private boolean  transientmode = false;
    private boolean  sweepfiltering = false;
    private Color    emptycolor = EMPTYCOLOR;
    private Color    highlightcolor = HIGHLIGHTCOLOR;
    private Color    pressedcolor = PRESSEDCOLOR;
    private Color    pressingcolor = PRESSINGCOLOR;
    private Color    sweepcolor = SWEEPCOLOR;
    private int      selectedcount = 0;
    private int      totalcount = 0;

    private int      addlength;	// used by synchronized load/append records routines as a convenience

    private Dimension  cellsize = null;
    private Rectangle  viewport = null;
    private int        anchor = YOIX_RIGHT;

    private GridBagConstraints  spacerconstraints = null;
    private JScrollPane         peerscroller = null;
    private JScrollBar          scroller = null;
    private boolean             configuringpane = false;
    private JPanel              spacerpanel = null;
    private JPanel              headerpanel = null;
    private int                 lastheight = -1;

    //
    // Used when drawing the bars.
    //

    private double  maxvalue = 0;
    //private int     maxbar = 0;

    //
    // Mouse event support.
    //

    private boolean  pressedstart;
    private Timer    autoscroller = null;
    private Point    mouseposition = null;
    private int      mouse = AVAILABLE;
    private int      mousebutton = 0;
    private int      toprow = 0;		// smallest touched row
    private int      bottomrow = 0;		// largest touched row plus 1
    private int      pressedrow = -1;

    //
    // Remembering callback functions helps reduce lookup and checking
    // overhead.
    //

    private YoixObject  afterload = null;
    private YoixObject  afterpressed = null;
    private YoixObject  afterupdate = null;

    //
    // Listeners for some custom events.
    //

    private ActionListener  actionlistener = null;
    private ItemListener    itemlistener = null;

    //
    // Arrays used to map event modifiers into actions.
    //

    private static final int  DEFAULT_OPERATIONS[] = {VL_OP_SELECT, VL_OP_ZOOM, VL_OP_PRESS};
    private int               operations[] = (int[])DEFAULT_OPERATIONS.clone();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    SwingJDataTable(YoixObject data, YoixBodyComponentSwing parent) {

	super(data, parent);
	this.parent = parent;
	this.data = data;
	TableUI ui = new DataTableUI();
	setUI(ui);
	((DataTableUI)ui).disableListeners();
	setDefaultRenderer(YoixJTableHistogram.class, new JTableHistogramRenderer());
	setActionMap(null);	// so our menu accelerators work properly
    }

    ///////////////////////////////////
    //
    // ActionListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    actionPerformed(ActionEvent e) {

	boolean  selecting;
	int      first;
	int      last;
	int      row;

	if (autoscroller != null && mouseposition != null) {
	    if (e.getSource() == autoscroller && autoscroller.isRunning()) {
		viewport = parent.getViewRect();
		if (mouseposition.y <= viewport.y || mouseposition.y >= (viewport.y + viewport.height)) {
		    switch (mouse) {
			case DESELECTING:
			case SELECTING:
			    selecting = (mouse == SELECTING);
			    if (mouseposition.y <= viewport.y) {
				first = getFirstRow() - 1;
				last = getLastRow() - 1;
				row = first;
				if (row > 0)
				    mouseposition.y -= cellsize.height;
			    } else {
				first = getFirstRow() + 1;
				last = getLastRow() + 1;
				row = last;
				if (mouseposition.y < (last - 1)*cellsize.height + viewport.height)
				    mouseposition.y += cellsize.height;
			    }
			    if (first >= 0 && first < totalcount) {
				setAutoScrollerDelay();
				verticalScrollTo(first*cellsize.height);
				if (row >= first && row <= last)
				    selectRow(row, selecting, false);
				else if (intransient)
				    selectRow(row, selecting, true);
				else selectRow(Math.min(last, Math.max(first, row)), selecting, false);
			    }
			    break;
		    }
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // ChangeListener Methods
    //
    ///////////////////////////////////

    public void
    stateChanged(ChangeEvent e) {

	Dimension  size;
	Object     src = e.getSource();

	if (src instanceof JViewport) {
	    size = ((JViewport)src).getExtentSize();
	    if (size.height != lastheight) {
		configureEnclosingScrollPane();
		lastheight = size.height;
	    }
	}
    }

    ///////////////////////////////////
    //
    // ComponentListener Methods
    //
    ///////////////////////////////////

    public void
    componentHidden(ComponentEvent e) {

	if (e.getSource() instanceof JTable)
	    handleShowingChange(false);
    }


    public void
    componentMoved(ComponentEvent e) {

    }


    public void
    componentResized(ComponentEvent e) {

	configureEnclosingScrollPane();
    }


    public void
    componentShown(ComponentEvent e) {

	if (e.getSource() instanceof JTable)
	    handleShowingChange(true);
    }

    ///////////////////////////////////
    //
    // ItemSelectable Methods
    //
    ///////////////////////////////////

    public final synchronized void
    addItemListener(ItemListener listener) {

        itemlistener = AWTEventMulticaster.add(itemlistener, listener);
    }


    public synchronized Object[]
    getSelectedObjects() {

	return(null);
    }


    public final synchronized void
    removeItemListener(ItemListener listener) {

	itemlistener = AWTEventMulticaster.remove(itemlistener, listener);
    }

    ///////////////////////////////////
    //
    // MouseListener Methods
    //
    ///////////////////////////////////

    public void
    mouseClicked(MouseEvent e) {

    }


    public synchronized void
    mouseEntered(MouseEvent e) {

	if (!(e.getComponent() instanceof JTableHeader))
	    stopAutoScroller();
    }


    public synchronized void
    mouseExited(MouseEvent e) {

	Point p;

	if (mouse != AVAILABLE) {
	    switch (mouse) {
		case SELECTING:
		case DESELECTING:
		    viewport = parent.getViewRect();
		    p = e.getPoint();
		    if (e.getSource() instanceof JViewport)
			p.y += ((JViewport)e.getSource()).getViewPosition().y;
		    if (p.y <= viewport.y || p.y >= (viewport.y + viewport.height))
			startAutoScroller();
		    break;
	    }
	}
    }


    public synchronized void
    mousePressed(MouseEvent e) {

	int  modifiers;
	int  button;
	int  op;

	if (e.getComponent() instanceof JTableHeader)
	    super.mousePressed(e);
	else if (mouse == AVAILABLE && datatable != null && alive) {
	    visiblerowcount = getVisibleRowCount();
	    mouseposition = e.getPoint();
	    if (e.getSource() instanceof JViewport)
		mouseposition.y += ((JViewport)e.getSource()).getViewPosition().y;
	    toprow = 0;
	    bottomrow = 0;
	    pressedrow = -1;
	    intransient = transientmode;
	    modifiers = YoixMiscJFC.cookModifiers(e);
	    button = modifiers & YOIX_BUTTON_MASK;
	    op = getOperation(modifiers);
	    switch (button) {
		case YOIX_BUTTON1_MASK:
		    if (hasFocus() == false)
			requestFocus();
		    switch (op) {
			case VL_OP_BRUSH:
			case VL_OP_SELECT:
			    mouse = SELECTING;
			    selectRow(findViewRowAt(mouseposition), true, false);
			    break;

			case VL_OP_PRESS:
			    mouse = PRESSING;
			    pressBegin(mouseposition);
			    break;

			default:
			    mouse = UNAVAILABLE;
			    break;
		    }
		    break;

		case YOIX_BUTTON2_MASK:
		    mouse = GRABBING;
		    grabBegin(mouseposition);
		    break;

		case YOIX_BUTTON3_MASK:
		    switch (op) {
			case VL_OP_BRUSH:
			case VL_OP_SELECT:
			    mouse = DESELECTING;
			    selectRow(findViewRowAt(mouseposition), false, false);
			    break;

			case VL_OP_PRESS:
			    mouse = TOGGLING;
			    pressBegin(mouseposition);
			    break;

			default:
			    mouse = UNAVAILABLE;
			    break;
		    }
		    break;
	    }
	    mousebutton = (mouse != AVAILABLE) ? button : 0;
	    if (mouse != AVAILABLE) {
		addMouseMotionListener(this);
		peerscroller.getViewport().addMouseMotionListener(this);
	    }
	}
    }


    public synchronized void
    mouseReleased(MouseEvent e) {

	boolean  selecting;
	int      buttons;
	int      first;
	int      last;
	int      row;

	//
	// The modifiers that we get from Java 1.3.1 and newer versions are
	// different, so we can't just compare mousebutton and buttons. Can
	// change when we no longer support Java 1.3.1.
	//

	if (mouse != AVAILABLE) {
	    mouseposition = e.getPoint();
	    if (e.getSource() instanceof JViewport)
		mouseposition.y += ((JViewport)e.getSource()).getViewPosition().y;
	    buttons = YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK;
	    if ((buttons & mousebutton) != 0) {		// test is for Java 1.3.1
		if (datatable != null) {
		    switch (mouse) {
			case DESELECTING:
			case SELECTING:
			    selecting = (mouse == SELECTING);
			    first = getFirstRow();
			    last = getLastRow();
			    row = findViewRowAt(mouseposition);
			    if ((row >= first && row <= last) || intransient)
				selectRow(row, selecting, true);
			    postActionEvent(null, e.getModifiers());
			    break;

			case GRABBING:
			    grabEnd(mouseposition);
			    break;

			case PRESSING:
			case TOGGLING:
			    pressEnd(e);
			    break;
		    }
		}
		toprow = 0;
		bottomrow = 0;
		pressedrow = -1;
		intransient = false;
		mouseposition = null;
		mouse = AVAILABLE;
		mousebutton = 0;
	    }
	    if (mouse == AVAILABLE) {
		removeMouseMotionListener(this);
		peerscroller.getViewport().removeMouseMotionListener(this);
		stopAutoScroller();
	    }
	    visiblerowcount = -1;
	} else if (e.getComponent() instanceof JTableHeader)
	    super.mouseReleased(e);
    }

    ///////////////////////////////////
    //
    // MouseMotionListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseDragged(MouseEvent e) {

	boolean  selecting;
	int      first;
	int      last;
	int      row;

	if (mouse != AVAILABLE && datatable != null) {
	    mouseposition = e.getPoint();
	    if (e.getSource() instanceof JViewport)
		mouseposition.y += ((JViewport)e.getSource()).getViewPosition().y;
	    switch (mouse) {
		case DESELECTING:
		case SELECTING:
		    selecting = (mouse == SELECTING);
		    first = getFirstRow();
		    last = getLastRow();
		    row = findViewRowAt(mouseposition);
		    if (row >= first && row <= last)
			selectRow(row, selecting, false);
		    else if (intransient)
			selectRow(row, selecting, true);
		    else selectRow(Math.min(last, Math.max(first, row)), selecting, false);
		    break;

		case GRABBING:
		    grabDragged(mouseposition);
		    break;

		case PRESSING:
		case TOGGLING:
		    pressDragged(mouseposition);
		    break;
	    }
	} else if (e.getComponent() instanceof JTableHeader)
	    super.mouseDragged(e);
    }


    public synchronized void
    mouseMoved(MouseEvent e) {

	if (datatable != null) {
	    if (ISMAC && (e.getModifiers()&YOIX_CTRL_MASK) != 0)
		mouseDragged(e);
	}
    }

    ///////////////////////////////////
    //
    // MouseWheelListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseWheelMoved(MouseWheelEvent e) {

	EventQueue  queue;

	if ((queue = YoixAWTToolkit.getSystemEventQueue()) != null) {
	    queue.postEvent(
		new MouseWheelEvent(
		    scroller,
		    e.getID(),
		    e.getWhen(),
		    e.getModifiers(),
		    e.getX(),
		    e.getY(),
		    e.getClickCount(),
		    e.isPopupTrigger(),
		    e.getScrollType(),
		    e.getScrollAmount(),
		    e.getWheelRotation()
		)
	    );
	}
    }

    ///////////////////////////////////
    //
    // YoixInterfaceShowing Methods
    //
    ///////////////////////////////////

    public final synchronized void
    handleShowingChange(boolean state) {

	if (datamanager != null) {
	    if (state) {
		if (isLoadable() && isLoaded() == false)
		    datamanager.loadTable(this);
	    } else {
		if (autoready == false && sweepfiltering == false) {
		    loadeddata = null;
		    datarecords = null;
		}
		painted = false;
	    }
	}
    }

    ///////////////////////////////////
    //
    // DataTable Methods
    //
    ///////////////////////////////////

    public final synchronized int
    colorTableWith(Palette palette) {

	DataRecord  record;
	TableData   bin;
	boolean     repaint;
	String      name;
	Color       color;
	Color       defaultcolor;
	int         count = 0;
	int         length;
	int         n;

	if (datatable != null) {
	    defaultcolor = getForeground();
	    repaint = (isShowing() && painted && totalcount > 0);
	    length = datatable.length;
	    for (n = 0; n < length; n++) {
		bin = datatable[n];
		name = bin.key;
		if (palette == null) {
		    if ((record = (DataRecord)colormap.get(name)) != null) {
			if ((color = record.getColor()) == null)
			    color = defaultcolor;
		    } else color = defaultcolor;
		} else {
		    if ((color = palette.selectColor(n, length, null)) == null) {
			if ((record = (DataRecord)colormap.get(name)) != null)
			    if ((color = record.getColor()) == null)
				color = defaultcolor;
		    }
		}
		if (repaint && color != bin.color) {
		    if (color == null || !color.equals(bin.color)) {
			bin.repaint = true;
			count++;
		    }
		}
		bin.color = color;
	    }
	}
	return(count);
    }


    public final synchronized int
    getActiveFieldCount() {

	return(datamanager != null ? activefieldcount : 0);
    }


    public final YoixObject
    getContext() {

	//
	// This wasn't needed when we extended YoixSwingJTextComponent,
	// but it's not available in YoixSwingJTable or YoixSwingJPanel
	// so you may want to take a cloer look.
	//

	return(parent != null ? parent.getContext() : null);
    }


    public final synchronized int
    getCountTotal(int row) {

	return(row >= 0 && row < totalcount ? datatable[row].getCountTotal() : 0);
    }


    public final DataManager
    getDataManager() {

	return(datamanager);
    }


    public final synchronized YoixObject
    getHighlighted() {

	YoixObject  obj;
	ArrayList   buffer;
	int         length;
	int         n;

	if (totalcount > 0) {
	    buffer = new ArrayList();
	    for (n = 0; n < totalcount; n++) {
		if (isHighlighted(n))
		    buffer.add(YoixObject.newString(getKey(n)));
	    }
	    obj = YoixMisc.copyIntoArray(buffer, true, buffer);
	} else obj = null;

	return(obj != null ? obj : YoixObject.newArray());
    }


    public final synchronized String
    getKey(int row) {

	return(row >= 0 && row < totalcount ? datatable[row].key : null);
    }


    public final synchronized boolean
    isHighlighted(int row) {

	return(row >= 0 && row < totalcount && datatable[row].highlight);
    }


    public final synchronized boolean
    isPressed(int row) {

	return(row >= 0 && row < totalcount && datatable[row].pressed);
    }


    public final synchronized boolean
    isSelected(int row) {

	return(row >= 0 && row < totalcount && datatable[row].getCountValue() != 0);
    }


    public final synchronized boolean
    isSelected(String name) {

	TableData  bin;
	boolean    result;

	if (tablemap != null) {
	    if ((bin = (TableData)tablemap.get(name)) != null)
		result = (bin.getCountValue() != 0);
	    else result = false;
	} else result = false;

	return(result);
    }


    public final synchronized void
    loadRecords(DataRecord loaded[], DataRecord records[]) {

	if (loaded != loadeddata || datarecords != records)
	    loadRecords(loaded, records, true);
    }


    public final synchronized void
    recolorTable() {

	//
	// Currently only called by the datamanager and only for datatables
	// that are directly associated with plots, which means bar colors
	// are selected from the data rather than the palette, if there is
	// one. Will only result in a performance hit for other datatables,
	// but it's probably not a penalty we want to pay, so be careful
	// making changes here.
	//

	if (sweepfiltering) {
	    if (datamanager != null) {
		//
		// Old version did
		//
		//	loadRecords(loadeddata, datarecords, false);
		//
		// but this is more efficient and should work.
		//
		if (colorTableWith(currentpalette) > 0) {
		    if (datarecords != null)		// unnecessary test
			repaintTable(datarecords.length);
		    else repaintTable();
		}
	    }
	}
    }


    public final synchronized void
    repaintTable() {

	Color  bcolors[];
	Color  fcolors[];
	Color  back;
	Color  fore;
	int    length;
	int    first;
	int    last;
	int    n;

	if (isShowing())
	    fireTableDataChanged();
    }


    public final synchronized void
    repaintTable(int count) {

	if (count > 0)
	    repaintTable();
    }


    public final synchronized void
    setHighlighted(YoixObject obj) {

	YoixObject  items;
	HashMap     select;
	HashMap     deselect;

	if (loadeddata != null && tablemap != null) {
	    if (obj.notNull()) {
		if (obj.isArray() || obj.isNumber() || obj.isString()) {
		    if (obj.isArray() == false) {
			items = YoixObject.newArray(1);
			items.put(0, obj, false);
		    } else items = obj;
		    select = new HashMap();
		    deselect = (HashMap)tablemap.clone();
		    pickBins(items, select, deselect);
		    if (select.size() > 0)
			setHighlighted(select);
		}
	    } else setHighlighted(new HashMap());
	}
    }


    public final synchronized void
    setPressed(YoixObject obj) {

	YoixObject  items;
	HashMap     select;
	HashMap     deselect;

	if (loadeddata != null && tablemap != null) {
	    if (obj.notNull()) {
		if (obj.isArray() || obj.isNumber() || obj.isString()) {
		    if (obj.isArray() == false) {
			items = YoixObject.newArray(1);
			items.put(0, obj, false);
		    } else items = obj;
		    select = new HashMap();
		    deselect = (HashMap)tablemap.clone();
		    pickBins(items, select, deselect);
		    if (select.size() > 0)
			setPressed(select);
		}
	    } else setPressed(new HashMap());
	}
    }


    public final synchronized void
    setSelected(YoixObject obj) {

	YoixObject  items;
	HashMap     select;
	HashMap     deselect;

	if (tablemap != null) {
	    if (obj.notNull()) {
		if (obj.isArray() || obj.isNumber() || obj.isString()) {
		    if (obj.isArray() == false) {
			items = YoixObject.newArray(1);
			items.put(0, obj, false);
		    } else items = obj;
		    select = new HashMap();
		    deselect = (HashMap)tablemap.clone();
		    pickBins(items, select, deselect);
		    setSelected(select, deselect);
		}
	    }
	}
    }


    public final synchronized void
    setSelected(HashMap select, HashMap deselect) {

	HitBuffer  hits;
	boolean    needpaint;
	boolean    state;
	int        count;
	int        n;

	if (loadeddata != null && datamanager != null) {
	    if ((hits = datamanager.getHitBuffer(loadeddata)) != null) {
		needpaint = false;
		for (n = 0; n < datatable.length; n++) {
		    state = select.containsKey(datatable[n].key);
		    if (datatable[n].selected != state) {
			datatable[n].selected = state;
			datatable[n].repaint = true;
			needpaint = true;
		    }
		}
		if ((count = collectRecords(hits, select, deselect)) > 0) {
		    datamanager.updateData(loadeddata, hits, count, this);
		    Thread.yield();
		} else {
		    datamanager.releaseHitBuffer(hits);
		    if (needpaint)
			repaintTable();
		}
	    }
	}
    }


    public final synchronized void
    updateTable(DataRecord loaded[], HitBuffer hits, int count) {

	DataRecord  record;
	TableData   bin;
	boolean     postevent;
	String      name;
	String      secondary;
	int         sign;
	int         m;
	int         n;

	if (count > 0 && isLoaded()) {
	    if (isReady()) {
		if (loaded == loadeddata && datatable != null && tablemap != null) {
		    postevent = hasItemListener();
		    for (n = 0; n < count; n++) {
			record = hits.getRecord(n);
			for (m = 0; m < activefieldcount; m++) {
			    if ((name = getRecordName(record, m)) != null) {
				if ((bin = (TableData)tablemap.get(name)) != null) {
				    sign = hits.isSelected(n) ? 1 : -1;
				    bin.update(sign, getRecordValues(record, sign), postevent);
				}
			    }
			}
		    }
		    afterUpdate();
		}
	    } else clearTable();
	}
    }

    ///////////////////////////////////
    //
    // SweepFilter Methods
    //
    ///////////////////////////////////

    public final synchronized void
    appendRecords(DataRecord loaded[], int offset) {

	StringBuffer  sb;
	DataRecord    record;
	YoixObject    args[];
	TableData     element;
	HitBuffer     hits;
	ArrayList     elements;
	ArrayList     rows;
	ArrayList     colors;
	HashMap       map;
	String        sa[];
	String        name;
	String        secondary;
	int           length;
	int           count;
	int           m;
	int           n;

	//
	// Seems like we probably should deal with colormap here too. The
	// code came from SwingJHistogram, and we probably didn't consider
	// appending to a Histogram that didn't have a palette (i.e. we
	// probably just assumed it was a sweepfilter).
	//

	if (isReady()) {
	    if (loaded != null && fieldindices != null && fieldmasks != null) {
		length = loaded.length;
		if (offset >= 0 && offset < length) {
		    loadeddata = loaded;
		    hits = new HitBuffer(length - offset);
		    count = 0;
		    if (sweepfiltering == false) {
			datarecords = loaded;
			map = new HashMap(tablemap);
			elements = YoixMisc.copyIntoArrayList(datatable);
			rows = new ArrayList(length - offset);
			colors = new ArrayList(2*(length - offset));
			addlength = 0;
			for (n = offset; n < length; n++) {
			    if ((record = datarecords[n]) != null) {
				for (m = 0; m < fieldindices.length; m++) {
				    if ((name = getRecordName(record, m)) != null) {
					if (m < fieldmasks.length) {	// what happens here??
					    element = loadData(name, 1, getRecordValues(record, 1), n, map, elements, rows, colors);
					    if (element != null) {
						if (record.isSelected(fieldmasks[m])) {
						    if (element.selected == false) {
							if (record.isSelected())
							    hits.setRecord(count++, record, false);
							record.clearSelected(fieldmasks[m]);
						    }
						} else element.update(-1, getRecordValues(record, -1), false);
					    }
					}
				    }
				}
			    }
			}
			sb = new StringBuffer(addlength);
			sa = (String[])rows.toArray(new String[]{});
			for (n=0; n<sa.length; n++) {
			    sb.append(sa[n]);
			    sb.append('\n');
			}
			args =  new YoixObject[] {
			    YoixObject.newInt(YOIX_APPEND_ROWS),
			    YoixObject.newString(sb.toString()),
			};
			callActionBuiltin(args);
			addRowColors(getForeground(), getBackground());
			if (cellsize == null)
			    cellsize = getCellRect(0,0,true).getSize();
			buildTable(map, elements);
			if (datamanager != null) {
			    datamanager.updateData(loadeddata, hits, count, this);
			    afterLoad();
			}
			reset();
		    }
		}
	    }
	}
    }


    public final synchronized void
    clear() {

	if (sweepfiltering && datamanager != null) {
	    if (isLoaded())
		datamanager.clearTable(this);
	}
    }


    public final synchronized void
    clear(boolean selected) {

	DataRecord  loaded[];
	DataRecord  record;
	HitBuffer   hits[];
	int         counts[];
	int         masks[];
	int         length;
	int         count;
	int         n;

	//
	// Harder than you might expect when clearing unselected records
	// because when we're done none of those records can be deselected
	// by the masks associated with this filter.
	//

	if (sweepfiltering && datamanager != null) {
	    if (isLoaded()) {
		if ((masks = getSelectMasks()) != null) {
		    hits = new HitBuffer[2];
		    if ((hits[0] = datamanager.getHitBuffer(loadeddata)) != null) {
			if (selected || (hits[1] = datamanager.getHitBuffer(loadeddata)) != null) {
			    counts = new int[] {0, 0};
			    length = datarecords.length;
			    for (n = 0; n < length; n++) {
				record = datarecords[n];
				if (selected == false) {
				    if (record.notSelected(masks)) {
					record.setSelected(masks);
					if (record.isSelected())
					    hits[1].setRecord(counts[1]++, record, true);
				    } else if (record.isSelected())
					hits[0].setRecord(counts[0]++, record);
				} else if (record.notSelected())
				    hits[0].setRecord(counts[0]++, record);
			    }
			    //
			    // Remember loadeddata because it may end up null
			    // in loadRecords() but datamanager.updateData()
			    // checks value before doing anything. Moving the
			    // loadRecords() call also works, but doing the
			    // load first seems safer.
			    //
			    loaded = loadeddata;	// set to null in clearTable()
			    loadRecords(loaded, hits[0].copyRecords(counts[0]), false);
			    if (hits[1] != null) {
				if (counts[1] > 0) {
				    datamanager.updateData(loaded, hits[1], counts[1], this);
				    Thread.yield();
				} else datamanager.releaseHitBuffer(hits[1]);
			    }
			}
			datamanager.releaseHitBuffer(hits[0]);
		    }
		}
	    }
	}
    }


    public final synchronized boolean
    getAccumulate() {

	return(accumulate && sweepfiltering);
    }


    public final int
    getAccumulatedRecords(HitBuffer hits, BitSet accumulated) {

	return(getAccumulatedRecords(hits, accumulated, getAccumulate()));
    }


    public final synchronized int[]
    getFieldIndices() {

	return(datamanager != null ? fieldindices : null);
    }


    public final synchronized int[]
    getFieldMasks() {

	return(datamanager != null ? fieldmasks : null);
    }


    public final synchronized int[]
    getSelectMasks() {

	return(datamanager != null ? selectmasks : null);
    }


    public final boolean
    isManagedBy(DataManager manager) {

	return(this.datamanager == manager);
    }


    public final synchronized boolean
    isSweepFilter(DataPlot plot) {

	return(sweepfiltering && plot.isManagedBy(datamanager));
    }


    public final synchronized void
    loadRecords(YoixObject records, boolean accumulating) {

	DataRecord  loaded[];
	DataRecord  record;
	YoixObject  element;
	HitBuffer   hits;
	BitSet      accumulated;
	int         length;
	int         index;
	int         count;
	int         start;
	int         n;

	//
	// Loads the DataRecords records identified by the integers indices
	// stored in the records array. This is only designed to be called
	// by the loadRecords() builtin that's supposed to handle actions,
	// like drag and drop, that may be used to load sweep filters.
	//

	if (sweepfiltering && datamanager != null) {
	    if ((loaded = datamanager.getDataRecords()) != null) {
		if ((hits = datamanager.getHitBuffer(loaded)) != null) {
		    if (records.isArray() && records.sizeof() > 0) {
			accumulated = new BitSet(0);
			length = records.length();
			start = getAccumulatedRecords(hits, accumulated, accumulating);
			count = start;
			for (n = records.offset(); n < length; n++) {
			    if ((element = records.getObject(n)) != null) {
				if (element.isInteger()) {
				    index = element.intValue();
				    if (index >= 0 && index < loaded.length) {
					record = loaded[index];
					if (accumulated.get(record.getID()) == false)
					    hits.setRecord(count++, record);
				    }
				}
			    }
			}
			if (count > start)
			    loadRecords(loaded, hits.copyRecords(count), false);
		    }
		}
		datamanager.releaseHitBuffer(hits);
	    }
	}
    }


    public final synchronized void
    loadRecords(DataRecord loaded[], DataRecord records[], boolean force) {

	StringBuffer  sb;
	DataRecord    record;
	YoixObject    args[];
	YoixObject    root;
	TableData     element;
	ArrayList     elements;
	ArrayList     rows;
	ArrayList     colors;
	HashMap       map;
	String        sa[];
	String        name;
	String        secondary;
	int           cols[];
	int           length;
	int           m;
	int           n;

	if (isReady() || force) {
	    cols = getStates();
	    clearTable();
	    setStates(cols);
	    if (records != null && records.length > 0 && fieldindices != null && fieldmasks != null) {
		length = records.length;
		loadeddata = loaded;
		datarecords = records;
		colormap = new Hashtable();
		map = new HashMap();
		elements = new ArrayList();
		rows = new ArrayList(length);
		colors = new ArrayList(2*length);
		addlength = 0;
		for (n = 0; n < length; n++) {
		    if ((record = datarecords[n]) != null) {
			for (m = 0; m < fieldindices.length; m++) {
			    if ((name = getRecordName(record, m)) != null) {
				if (m < fieldmasks.length) {	// what happens here??
				    element = loadData(name, 1, getRecordValues(record, 1), n, map, elements, rows, colors);
				    if (element != null) {
					if (record.notSelected()) {
					    element.update(-1, getRecordValues(record, -1), false);
					    if (record.notSelected(fieldmasks[m]))
						element.selected = false;
					}
					//
					// This code changed from Histogram version,
					// but there's a chance the currentpalette
					// test should also be done for Histograms.
					// Also - for some reason we we don't seem
					// to deal with colormap in appendRecords()
					// but it seems like we should (here and
					// in other classes like SwingJHistogram).
					// Didn't have time to make the change and
					// run tests - later.
					//
					if (sweepfiltering || currentpalette == null)
					    colormap.put(name, record);
				    }
				}
			    }
			}
		    }
		}
		sb = new StringBuffer(addlength);
		sa = (String[])rows.toArray(new String[]{});
		for (n=0; n<sa.length; n++) {
		    sb.append(sa[n]);
		    sb.append('\n');
		}
		args =  new YoixObject[] {
		    YoixObject.newInt(YOIX_APPEND_ROWS),
		    YoixObject.newString(sb.toString()),
		};
		callActionBuiltin(args);
		addRowColors(getForeground(), getBackground());
		if (cellsize == null)
		    cellsize = getCellRect(0,0,true).getSize();
		buildTable(map, elements);
		if (autoshow && sweepfiltering && datamanager != null) {
		    if ((root = data.getObject(N_ROOT)) != null && root.notNull())
			root.putInt(N_VISIBLE, true);
		}
	    }
	    if (datamanager != null)
		afterLoad();
	    reset();
	}
    }


    public final void
    recolorSweepFilter() {

	recolorTable();
    }


    public final void
    recordsSorted(DataRecord records[]) {

    }


    public final void
    repaintSweepFilter() {

	repaintTable();
    }


    public final void
    repaintSweepFilter(int count) {

	repaintTable(count);
    }


    public final synchronized void
    setDataManager(DataManager manager, int index, int mask) {

	setDataManager(manager, new int[] {index}, new int[] {mask}, new int[] {-1}, null);
    }


    public final synchronized void
    setDataManager(DataManager manager, int indices[], int masks[], int values[], int partitions[]) {

	//
	// Eventually add more checking. For example, make sure masks
	// is smaller than indices and that partitions and indices are
	// the same size? For now we assume the caller is disciplined,
	// which is OK because calls only come from DataManager.java.
	//
	// NOTE - the call to syncActive() is a recent addition (2/7/07)
	// that is supposed to make sure active stays in bounds. Doubt
	// it's really needed but if it did change active there could be
	// some inconsistency between our display and the checkbox menu
	// items that users can access.
	//

	if (datamanager != manager || manager == null) {
	    datamanager = manager;
	    fieldindices = indices;
	    fieldmasks = masks;
	    valueindices = values;
	    selectmasks = DataRecord.getSelectMasks(fieldmasks, false);
	    partitionindices = partitions;
	    setFastLookups();
	    setActiveFieldCount();
	    syncActive();
	    if (isLoaded())
		loadRecords(loadeddata, datarecords, false);
	}
    }


    public final synchronized void
    setSweepFiltering(boolean value) {

	if (sweepfiltering != value) {
	    sweepfiltering = value;
	    if (loadeddata != null)
		clear();
	}
	setFastLookups();
    }


    public final void
    updateSweepFilter(DataRecord loaded[], HitBuffer hits, int count) {

	updateTable(loaded, hits, count);
    }

    ///////////////////////////////////
    //
    // SwingJDataTable Methods
    //
    ///////////////////////////////////

    public final synchronized void
    addActionListener(ActionListener listener) {

	actionlistener = AWTEventMulticaster.add(actionlistener, listener);
    }


    final synchronized void
    afterLoad() {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = afterload) != null) {
	    if (funct.callable(1))
		argv = new YoixObject[] {YoixObject.newInt(totalcount)};
	    else argv = new YoixObject[0];
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }


    final synchronized void
    afterPressed(String key, MouseEvent mouseevent) {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = afterpressed) != null) {
	    if (funct.callable(2)) {
		argv = new YoixObject[] {
		    YoixObject.newString(key),
		    YoixMakeEvent.yoixEvent(
			mouseevent,
			mouseevent.getID(),
			parent
		    )
		};
	    } else argv = new YoixObject[] {YoixObject.newString(key)};
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }


    final synchronized void
    afterUpdate() {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = afterupdate) != null) {
	    if (funct.callable(1))
		argv = new YoixObject[] {YoixObject.newInt(selectedcount)};
	    else argv = new YoixObject[0];
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }


    protected void
    finalize() {

	stopAutoScroller();
	autoscroller = null;
	data = null;
	parent = null;
	datatable = null;
	super.finalize();
    }


    final void
    forceDataLoad() {

	if (isLoaded() == false && sweepfiltering == false) {
	    if (datamanager != null)
		datamanager.forceDataLoad(this);
	}
    }


    final int
    getActive() {

	return(active);
    }


    final YoixObject
    getAfterPressed() {

	return(afterpressed);
    }


    final boolean
    getAlive() {

	return(alive);
    }


    final boolean
    getAutoScroll() {

	return(autoscroller != null);
    }


    final synchronized Color
    getEmptyColor() {

	return(emptycolor != null ? emptycolor : EMPTYCOLOR);
    }


    final synchronized int
    getFieldIndex() {

	return(getFieldIndex(primaryfield));
    }


    final synchronized int
    getFieldIndex(int n) {

	return(datamanager != null && fieldindices != null && (n >= 0 && n < fieldindices.length) ? fieldindices[n] : -1);
    }


    final synchronized Color
    getHighlightColor() {

	return(highlightcolor != null ? highlightcolor : HIGHLIGHTCOLOR);
    }


    final synchronized YoixObject
    getKeys() {

	YoixObject  obj;
	int         n;

	if (totalcount > 0) {
	    obj = YoixObject.newArray(totalcount);
	    for (n = 0; n < totalcount; n++)
		obj.putString(n, getKey(n));
	} else obj = YoixObject.newArray();

	return(obj);
    }


    final synchronized YoixObject
    getOperations() {

	return(YoixMisc.copyIntoArray(operations));
    }


    final synchronized YoixObject
    getPressed() {

	YoixObject  obj;
	ArrayList   buffer;
	int         length;
	int         n;

	if (totalcount > 0) {
	    buffer = new ArrayList();
	    for (n = 0; n < totalcount; n++) {
		if (isPressed(n))
		    buffer.add(YoixObject.newString(getKey(n)));
	    }
	    obj = YoixMisc.copyIntoArray(buffer, true, buffer);
	} else obj = null;

	return(obj != null ? obj : YoixObject.newArray());
    }


    final synchronized Color
    getPressedColor() {

	return(pressedcolor != null ? pressedcolor : PRESSEDCOLOR);
    }


    final synchronized Color
    getPressingColor() {

	return(pressingcolor != null ? pressingcolor : PRESSINGCOLOR);
    }


    final String
    getRecordName(DataRecord record) {

	return(getRecordName(record, primaryfield));
    }


    final String
    getRecordName(DataRecord record, int n) {

	String  name;

	if (fieldindices != null && n >= 0 && n < fieldindices.length)
	    name = record.getField(fieldindices[n]);
	else name = null;

	return(name);
    }


    final double[]
    getRecordValues(DataRecord record, int sign) {

	return(getRecordValues(record, valueindices, sign));
    }


    final double[]
    getRecordValues(DataRecord record, int indices[], int sign) {

	double  values[];
	int     length;
	int     n;

	if (indices != null) {
	    length = indices.length;
	    values = new double[length];
	    for (n = 0; n < length; n++)
		values[n] = sign*record.getValue(indices[n]);
	} else values = null;
	return(values);
    }


    final synchronized YoixObject
    getSelected() {

	YoixObject  obj;
	ArrayList   buffer;
	int         n;

	if (totalcount > 0) {
	    buffer = new ArrayList();
	    for (n = 0; n < totalcount; n++) {
		if (isSelected(n))
		    buffer.add(YoixObject.newString(getKey(n)));
	    }
	    obj = YoixMisc.copyIntoArray(buffer, true, buffer);
	} else obj = null;

	return(obj != null ? obj : YoixObject.newArray());
    }


    final synchronized Color
    getSweepColor() {

	return(sweepcolor != null ? sweepcolor : SWEEPCOLOR);
    }


    final synchronized boolean
    isClear() {

	return(loadeddata == null || datarecords == null);
    }


    final synchronized boolean
    isLoaded() {

	return(loadeddata != null && datarecords != null);
    }


    final synchronized boolean
    isLoadable() {

	return(sweepfiltering == false && (autoready || isShowing()));
    }


    final synchronized boolean
    isReady() {

	return(autoready || sweepfiltering || isShowing());
    }


    final String
    localFindNextMatch(String string, int pattern, boolean ignorecase, boolean bycols, boolean forward) {

	return(findNextMatch(string, pattern, ignorecase, bycols, forward));
    }

    final void
    localFindClearSelection() {

	findClearSelection();
    }


    public void
    paint(Graphics g) {

	Color  bcolors[];
	Color  fcolors[];
	int    length;
	int    first;
	int    last;
	int    n;

	if (datatable != null && (length = datatable.length) > 0) {
	    bcolors = getCellBackgrounds();
	    fcolors = getCellForegrounds();
	    first = getFirstRow();
	    last = Math.min(getLastRow(), length - 1);
	    for (n = first; n <= last; n++)
		setupRow(n, bcolors, fcolors);
	}

	super.paint(g);
	painted = true;			// painted kludge
    }


    protected final void
    processEvent(AWTEvent e) {

	ActionListener  actionlistener;
	ItemListener    itemlistener;

	if (e instanceof ItemEvent) {
	    itemlistener = this.itemlistener;		// snapshot - just to be safe
	    if (itemlistener != null) {
		itemlistener.itemStateChanged(
		    new ItemEvent(
			(ItemSelectable)e.getSource(),
			ItemEvent.ITEM_STATE_CHANGED,
			((ItemEvent)e).getItem(),
			((ItemEvent)e).getStateChange()
		    )
		);
	    }
	} else if (e instanceof ActionEvent) {
	    actionlistener = this.actionlistener;	// snapshot - just to be safe
	    if (actionlistener != null) {
		actionlistener.actionPerformed(
		    new ActionEvent(
			e.getSource(),
			ActionEvent.ACTION_PERFORMED,
			((ActionEvent)e).getActionCommand(),
			((ActionEvent)e).getModifiers()
		    )
		);
	    }
	} else super.processEvent(e);
    }


    public final synchronized void
    removeActionListener(ActionListener listener) {

	actionlistener = AWTEventMulticaster.remove(actionlistener, listener);
    }


    final void
    reset() {

	resortTable();
	repaint();
	getTableHeader().repaint();

    }


    final synchronized void
    setAccumulate(boolean accumulate) {

	this.accumulate = accumulate;
    }


    final synchronized void
    setActive(int active) {

	if (active >= 0 && active <= (valueindices != null ? valueindices.length : 0)) {
	    if (this.active != active) {
		this.active = active;
		loadRecords(loadeddata, datarecords, false);
	    }
	}
    }


    final synchronized void
    setAfterLoad(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		afterload = obj;
	    else VM.abort(TYPECHECK, NL_AFTERLOAD);
	} else afterload = null;
    }


    final synchronized void
    setAfterPressed(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(1) || obj.callable(2))
		afterpressed = obj;
	    else VM.abort(TYPECHECK, NL_AFTERPRESSED);
	} else afterpressed = null;
    }


    final synchronized void
    setAfterUpdate(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		afterupdate = obj;
	    else VM.abort(TYPECHECK, NL_AFTERUPDATE);
	} else afterupdate = null;
    }


    final void
    setAlive(boolean state) {

	alive = state;
    }


    final synchronized void
    setAll(boolean state) {

	HashMap  select;
	HashMap  deselect;

	if (tablemap != null) {
	    if (state) {
		deselect = new HashMap();
		select = (HashMap)tablemap.clone();
	    } else {
		deselect = (HashMap)tablemap.clone();
		select = new HashMap();
	    }
	    setSelected(select, deselect);
	}
    }


    final synchronized void
    setAll(boolean state, YoixObject obj) {

	YoixObject  element;
	HashMap     select;
	HashMap     deselect;
	HashMap     target;
	String      key;
	Object      value;
	int         n;

	//
	// A recent addition (9/9/05) that only changes the state of the
	// elements that are named in obj, which currently should be an
	// array of strings that identify the target elements. Looks much
	// than it probably should because setSelected() currently seems
	// to want HashMap arguments that cover the data that's loaded.
	//

	if (tablemap != null && obj != null) {
	    target = new HashMap();
	    if (obj.isArray()) {
		target = new HashMap();
		for (n = obj.offset(); n < obj.length(); n++) {
		    if ((element = obj.getObject(n)) != null) {
			if (element.isString()) {
			    key = element.stringValue();
			    if (tablemap.containsKey(key))
				target.put(key, Boolean.TRUE);
			}
		    }
		}
	    }

	    if (target.size() > 0) {
		deselect = new HashMap();
		select = new HashMap();
		for (n = 0; n < totalcount; n++) {
		    key = getKey(n);
		    if ((value = tablemap.get(key)) != null) {
			if (state) {
			    if (isSelected(n) || target.containsKey(key))
				select.put(key, value);
			    else deselect.put(key, value);
			} else {
			    if (isSelected(n) == false || target.containsKey(key))
				deselect.put(key, value);
			    else select.put(key, value);
			}
		    }
		}
		setSelected(select, deselect);
	    }
	}
    }


    protected final synchronized void
    setAnchor(int anchor) {

	if (this.anchor != anchor) {
	    switch (anchor) {
		default:
		case YOIX_LEFT:
		case YOIX_WEST:
		    this.anchor = YOIX_LEFT;
		    break;

		case YOIX_CENTER:
		    this.anchor = YOIX_CENTER;
		    break;

		case YOIX_RIGHT:
		case YOIX_EAST:
		    this.anchor = YOIX_RIGHT;
		    break;
	    }
	    reset();
	}
    }


    final synchronized void
    setAutoReady(boolean value) {

	//
	// This is a kludge: want to set listeners on the peerscroller, but
	// we cannot access it in the constructor, so we do it here, which
	// is called just after the peerscroller is created.
	//

	peerscroller = parent.getPeerScroller();
	scroller = peerscroller.getVerticalScrollBar();
	addAllListeners();
	new DropTarget(peerscroller, parent);

	// now back to our regularly scheduled method...

	if (autoready != value) {
	    autoready = value;
	    //
	    // Is there more to do??
	    //
	}
    }


    final synchronized void
    setAutoScroll(boolean state) {

	if (state) {
	    if (autoscroller == null)
		autoscroller = new Timer(DEFAULT_INITIAL_DELAY, null);
	} else {
	    stopAutoScroller();
	    autoscroller = null;
	}
    }


    final void
    setAutoShow(boolean state) {

	autoshow = state;
    }


    final synchronized void
    setEmptyColor(Color color) {

	color = (color != null) ? color : EMPTYCOLOR;

	if (emptycolor == null || emptycolor.equals(color) == false) {
	    emptycolor = color;
	    reset();
	}
    }


    final synchronized void
    setHighlightColor(Color color) {

	color = (color != null) ? color : HIGHLIGHTCOLOR;

	if (color != highlightcolor) {
	    if (highlightcolor == null || highlightcolor.equals(color) == false) {
		highlightcolor = color;
		reset();
	    }
	}
    }


    final synchronized void
    setOperations(YoixObject obj) {

	int  m;
	int  n;

	operations = new int[DEFAULT_OPERATIONS.length];

	for (m = 0, n = obj.offset(); m < operations.length; m++, n++)
	    operations[m] = obj.getInt(n, DEFAULT_OPERATIONS[m]);
    }


    final synchronized void
    setPalette(YoixObject obj) {

	Palette  palette;

	if (obj != null && obj.notNull() && parent != null)
	    palette = (Palette)parent.getBody(obj);
	else palette = null;

	if (palette != currentpalette) {
	    currentpalette = palette;
	    colorTableWith(currentpalette);
	    reset();
	}
    }


    final synchronized void
    setPressedColor(Color color) {

	pressedcolor = (color != null) ? color : PRESSEDCOLOR;
    }


    final synchronized void
    setPressingColor(Color color) {

	pressingcolor = (color != null) ? color : PRESSINGCOLOR;
    }


    final synchronized void
    setPrimaryField(int field) {

	if (primaryfield != field) {
	    primaryfield = field;
	    reset();			// unnecessary???
	}
    }


    final synchronized void
    setSweepColor(Color color) {

	sweepcolor = (color != null) ? color : SWEEPCOLOR;
    }


    final synchronized void
    setTransientMode(boolean state) {

	transientmode = state;
    }


    final synchronized void
    updateData(int number, boolean selected) {

	HitBuffer  hits;
	int        count;

	if (totalcount > 0) {
	    if (datamanager != null) {
		if (datarecords != null && loadeddata != null) {
		    if (activefieldcount > 0) {
			if ((hits = datamanager.getHitBuffer(loadeddata)) != null) {
			    count = collectRecords(hits, number, selected);
			    if (count > 0) {
				datamanager.updateData(loadeddata, hits, count, this);
				Thread.yield();
			    } else {
				datamanager.releaseHitBuffer(hits);
				repaintTable();
			    }
			}
		    }
		}
	    }
	}
    }


    final synchronized void
    updateData(ArrayList touched, boolean selected) {

	HitBuffer  hits;
	int        count;
	int        size;

	if (touched != null) {
	    size = touched.size();
	    if (totalcount > 0 && size > 0) {
		if (datamanager != null) {
		    if (size > 1) {
			if (datarecords != null && loadeddata != null) {
			    if (activefieldcount > 0) {
				if ((hits = datamanager.getHitBuffer(loadeddata)) != null) {
				    count = collectRecords(hits, touched, selected);
				    if (count > 0) {
					datamanager.updateData(loadeddata, hits, count, this);
					Thread.yield();
				    } else {
					datamanager.releaseHitBuffer(hits);
					repaintTable();
				    }
				}
			    }
			}
		    } else updateData(((Integer)touched.get(0)).intValue(), selected);
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    addAllListeners() {

	//
	// Make sure we don't do this too often!!
	//

	if (listening == false) {
	    addComponentListener(this);
	    addMouseListener(this);
	    addMouseWheelListener(this);

	    peerscroller.getViewport().addMouseListener(this);
	    peerscroller.getViewport().addMouseWheelListener(this);
	    listening = true;
	}
    }


    private void
    buildTable(HashMap map, ArrayList elements) {

	TableData  element;
	double     value;
	int        n;

	selectedcount = 0;
	totalcount = elements.size();
	datatable = new TableData[totalcount];
	//maxbar = 0;
	maxvalue = 0;
	for (n = 0; n < totalcount; n++) {
	    if ((element = (TableData)elements.get(n)) != null) {
		selectedcount += element.selected ? 1 : 0;
		if ((value = element.getActiveTotal()) > maxvalue)
		    maxvalue = value;
	    }
	    datatable[n] = element;
	}
	tablemap = map;
	colorTableWith(currentpalette);
    }


    private void
    clearTable() {

	totalcount = 0;			// probably should be first
	selectedcount = 0;
	maxvalue = 0;
	//maxbar = 0;
	datatable = null;
	tablemap = null;
	colormap = null;
	cellsize = null;
	datarecords = null;
	loadeddata = null;
	setValues(YoixObject.newString(""));
    }


    private synchronized int
    collectRecords(HitBuffer hits, int number, boolean selected) {

	DataRecord  record;
	String      key;
	String      name;
	int         masks[];
	int         length;
	int         total;
	int         count = 0;
	int         m;
	int         n;

	//
	// This is the original brute force algorithm. It should always
	// work, but you may be able to implement faster version.
	//

	if ((masks = getSelectMasks()) != null) {
	    if (activefieldcount > 0) {
		length = datarecords.length;
		key = getKey(number);
		total = (key != null) ? getCountTotal(number) : length;
		for (n = 0; n < length && total > 0; n++) {
		    record = datarecords[n];
		    for (m = 0; m < activefieldcount; m++) {
			if ((name = getRecordName(record, m)) != null) {
			    if (name.equals(key) || key == null) {
				total--;
				if (selected) {
				    if (record.notSelected(masks)) {
					record.setSelected(masks);
					if (record.isSelected())
					    hits.setRecord(count++, record, true);
				    }
				} else {
				    if (record.isSelected(masks)) {
					if (record.isSelected())
					    hits.setRecord(count++, record, false);
					record.clearSelected(masks);
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	return(count);
    }


    private synchronized int
    collectRecords(HitBuffer hits, HashMap select, HashMap deselect) {

	DataRecord  record;
	String      name;
	int         masks[];
	int         length;
	int         count = 0;
	int         index;
	int         m;
	int         n;

	if ((masks = getSelectMasks()) != null) {
	    if (activefieldcount > 0) {
		length = datarecords.length;
		for (n = 0; n < length; n++) {
		    record = datarecords[n];
		    for (m = 0; m < activefieldcount; m++) {
			if ((name = getRecordName(record, m)) != null) {
			    if (select != null && select.containsKey(name)) {
				if (record.notSelected(masks)) {
				    record.setSelected(masks);
				    if (record.isSelected())
					hits.setRecord(count++, record, true);
				}
			    } else if (deselect != null && deselect.containsKey(name)) {
				if (record.isSelected(masks)) {
				    if (record.isSelected())
					hits.setRecord(count++, record, false);
				    record.clearSelected(masks);
				}
			    }
			}
		    }
		}
	    }
	}
	return(count);
    }


    private synchronized int
    collectRecords(HitBuffer hits, ArrayList touched, boolean selected) {

	DataRecord  record;
	HashMap     keys;
	Object      value;
	String      name;
	int         masks[];
	int         length;
	int         total;
	int         count = 0;
	int         size;
	int         m;
	int         n;

	//
	// Collects records when more than one bin is touched, so we only
	// have to request one update from datamanager. Initially done to
	// conserve hit buffers, but we noticed a speed improvement too.
	// There's still room for improvement - maybe later.
	//

	if ((masks = getSelectMasks()) != null) {
	    if (activefieldcount > 0) {
		length = datarecords.length;
		size = touched.size();
		keys = new HashMap((int)(1.1*size));
		total = 0;
		for (n = 0; n < size; n++) {
		    if ((value = touched.get(n)) != null) {
			if ((m = ((Integer)value).intValue()) >= 0) {
			    keys.put(getKey(m), Boolean.TRUE);
			    total += getCountTotal(m);
			}
		    }
		}
		for (n = 0; n < length && total > 0; n++) {
		    record = datarecords[n];
		    for (m = 0; m < activefieldcount; m++) {
			if ((name = getRecordName(record, m)) != null) {
			    if (keys.containsKey(name)) {
				total--;
				if (selected) {
				    if (record.notSelected(masks)) {
					record.setSelected(masks);
					if (record.isSelected())
					    hits.setRecord(count++, record, true);
				    }
				} else {
				    if (record.isSelected(masks)) {
					if (record.isSelected())
					    hits.setRecord(count++, record, false);
					record.clearSelected(masks);
				    }
				}
			    }

			}
		    }
		}
	    }
	}
	return(count);
    }


    protected synchronized void
    configureEnclosingScrollPane() {

	JScrollBar  vsb;
	JPanel      jp;
	Border      border;
	int         rows;
	int         cellht;
	int         viewht;

	if (peerscroller != null) {
	    if (!configuringpane) {
		configuringpane = true;
		rows = getRowCount();
		if (cellsize == null || rows <= 1) {
		    peerscroller.setViewportView(this);
		    if (spacerpanel != null) {
			peerscroller.getViewport().removeChangeListener(this);
			spacerpanel.removeComponentListener(this);
			spacerpanel.removeAll();
			spacerconstraints = null;
			spacerpanel = null;
			headerpanel.removeAll();
			headerpanel = null;
		    }
		    peerscroller.setColumnHeaderView(getTableHeader());
		} else {
		    if (spacerpanel == null) {
			peerscroller.getViewport().addChangeListener(this);
			spacerpanel = new JPanel(new GridBagLayout());
			spacerpanel.setOpaque(false);
			spacerpanel.addComponentListener(this);
			headerpanel = new JPanel(new GridBagLayout());
			headerpanel.setOpaque(false);
			spacerconstraints = new GridBagConstraints();
			spacerconstraints.anchor = GridBagConstraints.NORTHWEST;
		    } else {
			spacerpanel.removeAll();
			headerpanel.removeAll();
		    }
		    spacerconstraints.weightx = 1;
		    spacerconstraints.gridx = 0;
		    spacerconstraints.gridy = 0;
		    spacerconstraints.fill = GridBagConstraints.NONE;
		    viewport = parent.getViewRect();
		    if ((viewht = viewport.height) > (cellht = cellsize.height))
			spacerconstraints.insets = new Insets(0,0,viewht-cellht,0);
		    else spacerconstraints.insets = new Insets(0,0,0,0);
		    spacerpanel.add(this, spacerconstraints);
		    spacerconstraints.insets = new Insets(0,0,0,0);
		    headerpanel.add(getTableHeader(), spacerconstraints);
		    if (viewport.width > this.getWidth()) {
			// add a panel as slack to keep the table from moving
			spacerconstraints.weightx = 1;
			spacerconstraints.gridx = 1;
			spacerconstraints.gridy = 0;
			spacerconstraints.fill = GridBagConstraints.HORIZONTAL;
			spacerpanel.add(jp = new JPanel(), spacerconstraints);
			jp.setOpaque(false);
			headerpanel.add(jp = new JPanel(), spacerconstraints);
			jp.setOpaque(false);
		    }
		    peerscroller.setViewportView(spacerpanel);
		    peerscroller.setColumnHeaderView(headerpanel);
		    vsb = peerscroller.getVerticalScrollBar();
		    if (cellht <= 0)
			cellht = 1;
		    vsb.setUnitIncrement(cellht);
		    if (viewht > (2 * cellht))
			vsb.setBlockIncrement((((int)(viewht / cellht)) - 1) * cellht);
		    else vsb.setBlockIncrement(cellht);
		    lastheight = peerscroller.getViewport().getExtentSize().height;
		}
		border = peerscroller.getBorder();
		if (border == null || border instanceof UIResource) {
		    peerscroller.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		}
		configuringpane = false;
	    }
	}
    }


    private void
    drawBar(int row, Graphics g, Rectangle rect) {

	TableData  table[];
	TableData  bin;
	boolean    selected;
	boolean    flipfill;
	double     value;
	double     total;
	Color      color;
	int        barwidth;
	int        barheight;
	int        width;
	int        x;
	int        y;
	int        maxbar;

	//
	// We removed the synchronization from this method on 7/3/07 that
	// was added to try to prevent using a null datatable. Done because
	// we saw a few deadlocks that were caused the synchronization. We
	// now use a snapshot of datatable which should stop both problems.
	//

	if ((table = datatable) != null) {
	    if (row >= 0 && row < totalcount) {
		bin = table[row];
		selected = isSelected(row);	// changed on 9/2/04
		value = bin.getActiveValue();
		total = bin.getActiveTotal();

		y = rect.y;
		maxbar = rect.width;
		barwidth = maxbar;
		barheight = rect.height;

		switch (anchor) {
		    case YOIX_LEFT:
		    default:
			flipfill = false;
			barwidth = (int)(barwidth*total/maxvalue);
			x = 0;
			break;

		    case YOIX_RIGHT:
			flipfill = true;
			barwidth = (int)(barwidth*total/maxvalue);
			x = maxbar - barwidth;
			break;
		}
		if (selected == false || value != total) {
		    g.setColor(emptycolor);
		    g.fillRect(x, y, barwidth, barheight);
		}
		if (selected && (value > 0 || value == total)) {
		    g.setColor(bin.color);
		    if (value < total) {
			width = (int)((barwidth*value)/total);
			if (flipfill)
			    g.fillRect(x + barwidth - width, y, width, barheight);
			else g.fillRect(x, y, width, barheight);
		    } else g.fillRect(x, y, barwidth, barheight);
		}
	    }
	}
    }


    private synchronized int
    findViewRowAt(Point point) {

	return(cellsize.height > 0 ? point.y/cellsize.height : 0);
    }


    private synchronized int
    findViewRowAt(Point point, int fail) {

	int  index;

	if ((index = findViewRowAt(point)) < 0 || index >= datatable.length)
	    index = fail;
	return(index);
    }


    private synchronized int
    getAccumulatedRecords(HitBuffer hits, BitSet accumulated, boolean accumulating) {

	int  masks[];
	int  count;
	int  n;

	//
	// Using HitBuffer is just a convenience - no records have their
	// state changed and collected records aren't sent back to the
	// DataManager.
	//

	count = 0;

	if (datarecords != null && loadeddata != null) {
	    masks = getSelectMasks();
	    for (n = 0; n < datarecords.length; n++) {
		if (accumulating || datarecords[n].notSelected(masks)) {
		    hits.setRecord(count++, datarecords[n]);
		    if (accumulated != null)
			accumulated.set(datarecords[n].getID());
		}
	    }
	}
	return(count);
    }


    private synchronized int
    getFirstRow() {

	viewport = parent.getViewRect();
	return(cellsize.height > 0 ? viewport.y/cellsize.height : 0);
    }


    private synchronized int
    getHighlightedRow(int row, int incr) {

	int  count;
	int  n;

	if (incr != 0) {
	    for (n = row, count = totalcount; count > 0; count--, n += incr) {
		if (n < 0 || n >= totalcount)
		    n = (n < 0) ? totalcount - 1 : 0;
		if (isHighlighted(n)) {
		    row = n;
		    break;
		}
	    }
	}
	return(row);
    }


    private synchronized int
    getLastCompleteRow() {

	viewport = parent.getViewRect();
	return(cellsize != null && cellsize.height > 0 ? (viewport.y + viewport.height - cellsize.height + 1)/cellsize.height : -1);
    }


    private synchronized int
    getLastRow() {

	viewport = parent.getViewRect();
	return(cellsize != null && cellsize.height > 0 ? (viewport.y + viewport.height)/cellsize.height : -1);
    }


    private synchronized int
    getOperation(int modifiers) {

	int  op;

	if ((modifiers & YOIX_CTRL_MASK) == 0) {
	    if ((modifiers & YOIX_SHIFT_MASK) != 0)
		op = operations[SHIFT_OP];
	    else op = operations[PLAIN_OP];
	} else op = operations[CONTROL_OP];

	return(op);
    }


    private synchronized int
    getRow(int y, boolean restrict) {

	int  num;

	return(restrict
	    ? (cellsize.height > 0 && (num = y/cellsize.height) >= 0 && num < datatable.length ? num : -1)
	    : (cellsize.height > 0 ? y/cellsize.height : -1)
	);
    }


    private void
    grabBegin(Point p) {

	TableData  bin;
	int        row;

	if ((row = findViewRowAt(p, -1)) >= 0) {
	    pressedrow = row;
	    bin = datatable[yoixConvertRowIndexToModel(pressedrow)];
	    pressedstart = bin.pressed;
	    bin.repaint = true;
	    bin.pressed = !pressedstart;
	    repaintTable();
	}
    }


    private void
    grabDragged(Point point) {

	int  first;
	int  delta;
	int  row;

	if (pressedrow != -1) {
	    first = getFirstRow();
	    if ((row = getRow(point.y, true)) >= 0) {
		delta = pressedrow - row;
		row = Math.max(0, Math.min(first + delta, datatable.length - 1));
		if (row != first)
		    verticalScrollTo(row*cellsize.height);
	    }
	}
    }


    private void
    grabEnd(Point point) {

	TableData  bin;

	if (pressedrow != -1) {
	    bin = datatable[yoixConvertRowIndexToModel(pressedrow)];
	    bin.pressed = pressedstart;
	    bin.repaint = true;
	    pressedrow = -1;
	    repaintTable();
	}
    }


    private boolean
    hasActionListener() {

	YoixObject  obj;
	boolean     result;

	if (actionlistener != null) {
	    obj = data.getObject(N_ACTIONPERFORMED);
	    result = (obj != null && obj.notNull() && obj.isCallable());
	} else result = false;

	return(result);
    }


    private boolean
    hasItemListener() {

	YoixObject  obj;
	boolean     result;

	if (itemlistener != null) {
	    obj = data.getObject(N_ITEMCHANGED);
	    result = (obj != null && obj.notNull() && obj.isCallable());
	} else result = false;

	return(result);
    }


    private synchronized TableData
    loadData(String name, int count, double values[], int id, HashMap map, ArrayList elements, ArrayList rows, ArrayList colors) {

	StringBuffer  sb;
	YoixObject    args[];
	YoixObject    yobj;
	DataRecord    record;
	TableData     bin;
	String        fldname;
	char          sep = '|';
	int           m;

	//
	// We assume the caller guarantees name isn't null.
	//

	if ((bin = (TableData)map.get(name)) == null) {
	    bin = new TableData(name, id, values.length + 1);
	    map.put(name, bin);
	    elements.add(bin);
	}

	//
	// Threw this in quickly and also initialized inputfilter in the
	// init.yx script - probably not 100%.
	//
	if ((yobj = data.getObject(N_INPUTFILTER)) != null) {
	    if (yobj.isString() && yobj.sizeof() > 0)
		sep = yobj.stringValue().charAt(0);
	}

	sb = new StringBuffer(name);
	if ((record = datarecords[id]) != null) {
	    for (m = 1; m < fieldindices.length; m++) {
		sb.append(sep);		// delimiter from yx file is found where?
		if ((fldname = getRecordName(record, m)) != null)
		    sb.append(fldname);
	    }
	}
	bin.load(Math.max(1, count), values);
	addlength += 1 + sb.length();
	rows.add(sb.toString());
	colors.add(getForeground());
	colors.add(getBackground());
	return(bin);
    }


    private Color
    pickAdjustedColor(Color color, double adjust) {

	return(Misc.pickAdjustedColor(
	    color,
	    adjust,
	    new Color[] {
		getEmptyColor(),
		getBackground(),
		getForeground(),
	    }
	));
    }


    private synchronized void
    pickBins(YoixObject list, HashMap select, HashMap deselect) {

	YoixObject  item;
	DataRecord  record;
	TableData   bin;
	String      name;
	String      key;
	int         index;
	int         length;
	int         n;

	//
	// Numbers now always reference records in loadeddata rather than
	// individual bins in the datatable[] array. It's a small change
	// from older implemenations, at least for setSelected(), but we
	// don't think it will affect existing applications.
	//

	length = list.length();
	for (n = list.offset(); n < length; n++) {
	    if ((item = list.getObject(n)) != null) {
		key = null;
		if (item.isNumber()) {
		    index = item.intValue();
		    if (index >= 0 && index < loadeddata.length) {
			record = loadeddata[index];
			if ((name = getRecordName(record)) != null) {
			    if ((bin = (TableData)tablemap.get(name)) != null)
				key = bin.key;
			}
		    }
		} else if (item.isString())
		    key = item.stringValue();
		if (key != null) {
		    if (deselect.containsKey(key)) {
			select.put(key, Boolean.TRUE);
			deselect.remove(key);
		    }
		}
	    }
	}
    }


    private Color
    pickHighlightColor(Color color) {

	return(highlightcolor == null ? pickAdjustedColor(color, 0.2) : highlightcolor);
    }


    private Color
    pickPressedColor(Color color) {

	return(pressedcolor == null ? pickAdjustedColor(color, -0.2) : pressedcolor);
    }


    private Color
    pickPressingColor(Color color) {

	if (pressingcolor == null) {
	    if (pressedcolor == null) {
		if (highlightcolor == null)
		    color = pickAdjustedColor(color, -0.3);
		else color = pickAdjustedColor(highlightcolor, -0.2);
	    } else color = pickAdjustedColor(pressedcolor, -0.2);
	} else color = pressingcolor;
	return(color);
    }


    private void
    postActionEvent(String command, int modifiers) {

	EventQueue  queue;

	//
	// Calling hasActionListener() won't be a burden here because the
	// only call comes from mouseReleased().
	//

	if (hasActionListener()) {
	    if ((queue = YoixAWTToolkit.getSystemEventQueue()) != null) {
		queue.postEvent(
		    new ActionEvent(
			this,
			AWTEvent.RESERVED_ID_MAX + 1,
			command,
			modifiers
		    )
		);
	    }
	}
    }


    private void
    postItemEvent(YoixObject item, boolean selected) {

	EventQueue  queue;

	//
	// Implicitly assumes the caller has checked hasItemListener(),
	// which is a small change from the YoixSwingJCanvas version.
	// Omitting hasItemListener() test each time TableData.update()
	// is called is for efficiency, because it could happen a lot.
	//

	if ((queue = YoixAWTToolkit.getSystemEventQueue()) != null) {
	    queue.postEvent(
		new ItemEvent(
		    this,
		    AWTEvent.RESERVED_ID_MAX + 1,
		    item,
		    selected ? ItemEvent.SELECTED : ItemEvent.DESELECTED
		)
	    );
	}
    }


    private void
    pressBegin(Point p) {

	TableData  bin;
	int        row;

	if ((row = findViewRowAt(p, -1)) >= 0) {
	    bin = datatable[yoixConvertRowIndexToModel(row)];
	    switch (mouse) {
		case PRESSING:
		    if (getAfterPressed() != null) {
			pressedrow = row;
			pressedstart = bin.pressed;
			bin.repaint = true;
			bin.pressed = !pressedstart;
			repaintTable();
		    }
		    break;

		case TOGGLING:
		    pressedrow = row;
		    pressedstart = bin.pressed;
		    bin.repaint = true;
		    bin.pressed = !pressedstart;
		    repaintTable();
		    break;
	    }
	}
    }


    private void
    pressDragged(Point p) {

	TableData  bin;

	if (pressedrow != -1) {
	    bin = datatable[yoixConvertRowIndexToModel(pressedrow)];
	    if (pressedrow == findViewRowAt(p, -1)) {
		if (bin.pressed == pressedstart) {
		    bin.repaint = true;
		    bin.pressed = !pressedstart;
		    repaintTable();
		}
	    } else {
		if (bin.pressed != pressedstart) {
		    bin.repaint = true;
		    bin.pressed = pressedstart;
		    repaintTable();
		}
	    }
	}
    }


    private void
    pressEnd(MouseEvent e) {

	TableData  bin;
	boolean    pressed;

	if (pressedrow != -1) {
	    bin = datatable[yoixConvertRowIndexToModel(pressedrow)];
	    pressedrow = -1;
	    switch (mouse) {
		case PRESSING:
		    pressed = bin.pressed;
		    bin.pressed = pressedstart;
		    bin.repaint = true;
		    repaintTable();
		    if (pressed != pressedstart)
			afterPressed(bin.key, e);
		    break;

		case TOGGLING:
		    pressed = bin.pressed;
		    bin.pressed = pressedstart;
		    bin.repaint = true;
		    if (pressed != pressedstart) {
			if (bin.highlight)
			    bin.highlight = false;
			else bin.pressed = !bin.pressed;
		    }
		    repaintTable();
		    break;
	    }
	}
    }


    private synchronized void
    removeAllListeners() {

	if (listening) {
	    removeComponentListener(this);
	    removeMouseListener(this);
	    removeMouseWheelListener(this);

	    removeMouseMotionListener(this);
	    if (peerscroller != null)
		peerscroller.getViewport().removeChangeListener(this);

	    listening = false;
	}
    }


    private synchronized void
    repaintVisibleRow(int row, boolean forceposition) {
	if (true) return;

	int  first;
	int  last;

	if (isShowing()) {
	    if (datatable != null) {
		if (row >= 0 && row < datatable.length) {
		    first = getFirstRow();
		    last = getLastCompleteRow();
		    if (row < first || row > last || forceposition) {
			verticalScrollTo(row*cellsize.height);
			reset();	// sometimes needed to erase old hightlight
		    }
		}
		repaintTable();
	    }
	}
    }


    private void
    selectRow(int row, boolean selected, boolean released) {

	ArrayList  touched;
	int        first;
	int        last;
	int        delta;
	int        n;
	int        nn;
	int        maxrow;

	if (datatable != null && datamanager != null) {
	    // actually visiblerowcount is most likely always valid
	    maxrow = (visiblerowcount < 0) ? datatable.length : visiblerowcount;
	    if (intransient == false) {
		if (row >= -1 && row <= datatable.length) {
		    if (row < toprow || row >= bottomrow) {
			if (toprow == bottomrow) {
			    first = row;
			    last = row + 1;
			    toprow = first;
			    bottomrow = last;
			    delta = 1;
			} else if (row < toprow) {
			    first = toprow - 1;
			    last = row - 1;
			    toprow = first;
			    delta = -1;
			} else {
			    first = bottomrow;
			    last = row + 1;
			    bottomrow = last;
			    delta = 1;
			}
			touched = new ArrayList();
			for (n = first; n != last; n += delta) {
			    if (n >= 0 && n < maxrow) {
				nn = yoixConvertRowIndexToModel(n);
				if (datatable[nn].selected != selected) {
				    datatable[nn].selected = selected;
				    datatable[nn].repaint = true;
				    touched.add(new Integer(nn));
				}
			    }
			}
			updateData(touched, selected);
		    }
		}
	    } else {
		if (row >= -1 && row <= maxrow) {
		    if (released || row < toprow || row >= bottomrow) {
			if (toprow != bottomrow) {
			    nn = yoixConvertRowIndexToModel(toprow);
			    datatable[nn].selected = !selected;
			    datatable[nn].repaint = true;
			    updateData(nn, !selected);
			    toprow = 0;
			    bottomrow = 0;
			}
			if (released == false && row >= 0 && row < maxrow) {
			    nn = yoixConvertRowIndexToModel(row);
			    if (datatable[nn].selected != selected) {
				datatable[nn].selected = selected;
				datatable[nn].repaint = true;
				toprow = row;
				bottomrow = row + 1;
				updateData(nn, selected);
			    }
			}
		    }
		}
	    }
	}
    }


    private synchronized void
    setActiveFieldCount() {

	if (fieldindices != null) {
	    if (fieldmasks != null)
		activefieldcount = Math.min(fieldindices.length, fieldmasks.length);
	    else activefieldcount = 0;
	} else activefieldcount = 0;
    }


    private synchronized void
    setAutoScrollerDelay() {

	double  factor;
	int     delay;
	int     distance;

	if (autoscroller != null && mouseposition != null) {
	    viewport = parent.getViewRect();
	    if (mouseposition.y <= viewport.y || mouseposition.y >= (viewport.y + viewport.height)) {
		if (mouseposition.y <= viewport.y)
		    distance = viewport.y - mouseposition.y;
		else distance = mouseposition.y - viewport.height - viewport.y;
		if (distance > cellsize.height) {
		    factor = Math.max(1.0 - (distance - cellsize.height)/(1.0*cellsize.height), 0.0);
		    delay = (int)(factor*DEFAULT_REPEAT_DELAY);
		} else delay = DEFAULT_REPEAT_DELAY;
	    } else delay = DEFAULT_REPEAT_DELAY;
	    autoscroller.setInitialDelay(DEFAULT_INITIAL_DELAY);
	    autoscroller.setDelay(Math.max(delay, MINIMUM_REPEAT_DELAY));
	}
    }


    private synchronized void
    setFastLookups() {

	int  n;

	if (partitionindices != null && sweepfiltering == false) {
	    fastlookups = new boolean[partitionindices.length];
	    for (n = 0; n < fastlookups.length; n++)
		fastlookups[n] = (partitionindices[n] < 0);
	} else fastlookups = null;
    }


    private synchronized void
    setHighlighted(int row) {

	HashMap  select;
	String   key;

	select = new HashMap();
	if ((key = getKey(row)) != null)
	    select.put(key, Boolean.TRUE);
	setHighlighted(select);
    }


    private synchronized void
    setHighlighted(HashMap select) {

	TableData  bin;
	boolean    needpaint;
	boolean    state;
	int        row;
	int        n;

	if (loadeddata != null && datamanager != null) {
	    needpaint = false;
	    row = -1;
	    for (n = 0; n < datatable.length; n++) {
		bin = datatable[n];
		state = select.containsKey(bin.key);
		if (bin.highlight != state) {
		    bin.highlight = state;
		    bin.repaint = true;
		    needpaint = true;
		}
		if (row < 0 && bin.highlight)
		    row = n;
	    }
	    if (needpaint || row >= 0)
		repaintVisibleRow(row, false);
	}
    }


    private synchronized void
    setPressed(HashMap select) {

	TableData  bin;
	boolean    needpaint;
	boolean    state;
	int        n;

	if (loadeddata != null && datamanager != null) {
	    needpaint = false;
	    for (n = 0; n < datatable.length; n++) {
		bin = datatable[n];
		state = select.containsKey(bin.key);
		if (bin.pressed != state) {
		    bin.pressed = state;
		    bin.repaint = true;
		    needpaint = true;
		}
	    }
	    if (needpaint)
		repaintTable();
	}
    }


    private void
    setupRow(int vrow, Color bcolors[], Color fcolors[]) {

	TableData  bin;
	Color      color;
	int        row;

	row = yoixConvertRowIndexToModel(vrow);
	bin = datatable[row];

	if (bin.pressed || bin.highlight || vrow == pressedrow) {
	    if (vrow != pressedrow || bin.pressed == pressedstart) {
		if (bin.highlight)
		    color = pickHighlightColor(bin.color);
		else if (bin.pressed)
		    color = pickPressedColor(bin.color);
		else color = getBackground();
	    } else color = pickPressingColor(bin.color);
	    bcolors[vrow] = color;

	    if (vrow != pressedrow || bin.pressed == pressedstart) {
		if (bin.highlight)
		    color = bin.selected ? getBackground() : emptycolor;
		else color = bin.selected ? getForeground() : emptycolor;
	    } else color = bin.selected ? getForeground() : emptycolor;
	} else {
	    bcolors[vrow] = getBackground();
	    color = bin.selected ? getForeground() : emptycolor;
	}
	fcolors[vrow] = color;
    }


    private synchronized void
    startAutoScroller() {

	if (autoscroller != null) {
	    setAutoScrollerDelay();
	    autoscroller.addActionListener(this);
	    autoscroller.start();
	}
    }


    private synchronized void
    stopAutoScroller() {

	if (autoscroller != null) {
	    autoscroller.stop();
	    autoscroller.removeActionListener(this);
	}
    }


    private synchronized void
    syncActive() {

	if (valueindices == null || active < 0 || active > valueindices.length)
	    active = 0;
    }


    private void
    verticalScrollTo(int newy) {

	viewport = parent.getViewRect();
	viewport.y = newy;
	scrollRectToVisible(viewport);
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class TableData {

	//
	// A stripped down version of HistogramData that's still overkill
	// but might be useful if we ever decide to implement a variation
	// of Histograms using a JTable. If it looks like that won't ever
	// happen then we should toss most (if not all) of this class.
	//

	String  key;
	int     id;

	boolean  selected = true;
	boolean  repaint = false;
	boolean  pressed = false;
	boolean  highlight = false;
	double   counters[];
	double   totals[];
	Color    color = Color.white;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	TableData(String key, int id, int slots) {

	    this.key = key;		// should match record name
	    this.id = id;		// increasing integer for ordering
	    this.counters = new double[slots];
	    this.totals = new double[slots];
	}

	///////////////////////////////////
	//
	// TableData Methods
	//
	///////////////////////////////////

	final double
	getActiveTotal() {

	    return(totals[active]);
	}


	final double
	getActiveValue() {

	    return(counters[active]);
	}


	final int
	getCountTotal() {

	    return((int)totals[0]);
	}


	final int
	getCountValue() {

	    return((int)counters[0]);
	}


	final double
	getTotalAt(int n) {

	    return(totals[n]);
	}


	final double
	getValueAt(int n) {

	    return(counters[n]);
	}


	final void
	load(int count, double values[]) {

	    int  m;
	    int  n;

	    counters[0] += count;
	    totals[0] += count;
	    for (n = 0, m = 1; n < values.length; n++, m++) {
		counters[m] += values[n];
		totals[m] += values[n];
	    }
	}


	final void
	update(int count, double values[], boolean postevent) {

	    boolean  startstate;
	    boolean  endstate;
	    int      m;
	    int      n;

	    //
	    // May need better checking??
	    //

	    startstate = (counters[active] != 0);

	    counters[0] = Math.max(0, Math.min(totals[0], counters[0] + count));
	    for (n = 0, m = 1; n < values.length; n++, m++) {
		if (counters[0] != 0)
		    counters[m] += values[n];
		else counters[m] = 0;
	    }
	    repaint = true;
	    endstate = (counters[active] != 0);

	    if (startstate != endstate) {
		selectedcount += endstate ? 1 : -1;
		if (postevent)
		    postItemEvent(YoixObject.newString(key), endstate);
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class JTableHistogramRenderer extends YoixJTableHistogramRenderer {

	protected void
	paintComponent(Graphics g) {

	    Dimension   size = getSize();
	    Rectangle   rect = new Rectangle(0, 0, size.width, size.height);
	    Color       orig;
	    int         modrow;
	    int         row = getRow();

	    orig = g.getColor();
	    g.setColor(getBackground());
	    g.fillRect(rect.x, rect.y, rect.width, rect.height);
	    modrow = yoixConvertRowIndexToModel(row);
	    drawBar(modrow, g, rect);
	    g.setColor(orig);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class DataTableUI extends BasicTableUI {

	DataTableUI() {

	    super();
	}

	public void
	disableListeners() {

	    uninstallListeners();
	}
    }
}

