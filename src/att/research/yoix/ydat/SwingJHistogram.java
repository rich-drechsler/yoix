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
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.Timer;
import att.research.yoix.*;

class SwingJHistogram extends SwingDataViewer

    implements ActionListener

{

    //
    // We removed the specialized version of collectRecords() that used
    // the bindata[] array to try and improve performance, but it might
    // be something we eventually want to restore. Tossed the code while
    // adding support for Guy's "set-value" histograms, mostly because
    // we wanted to simplify things as much as possible.
    //
    // This was written long before we assumed Java2D was available, but
    // that's now changed so this code could be improved - later.
    //
    // NOTE - the activefieldcount changes added to SwingJGraphPlot.java
    // on 8/1/07 eventually should be included here. See the comments at
    // the start of SwingJGraphPlot.java for more info.
    //

    private HistogramData  histogram[];
    private YoixObject     translator;
    private Hashtable      labelmap;
    private HashMap        binmap;
    private Palette        sortingpalette = null;

    private boolean  alive = true;
    private boolean  hideunlabeled = true;
    private boolean  stackstate = false;
    private double   barspace = 0;
    private double   maxvalue = 0;
    private int      maxbar = 0;
    private int      currentorder = VL_SORT_LOAD_ORDER;
    private int      sortdefault = VL_SORT_LOAD_ORDER;

    //
    // Forces an exact width calculation based on the loaded text when
    // set to true. Overhead was quite noticeable when tables had lots
    // bins. None of our existing applications need exact widths, so
    // it's disabled by default. Also, there's currently no way users
    // can change this setting, but that should be easy to change.
    //

    private boolean  measuretext = false;

    //
    // Mouse event related stuff.
    //

    private boolean  pressedstart;
    private Timer    autoscroller = null;
    private Point    mouseposition = null;
    private int      toprow = 0;	// smallest touched row
    private int      bottomrow = 0;	// largest touched row plus 1
    private int      pressedrow = -1;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    SwingJHistogram(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
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
	int      n;

	if (autoscroller != null && mouseposition != null) {
	    if (e.getSource() == autoscroller && autoscroller.isRunning()) {
		if (mouseposition.y < 0 || mouseposition.y > viewport.height) {
		    switch (mouse) {
			case DESELECTING:
			case SELECTING:
			    selecting = (mouse == SELECTING);
			    if (mouseposition.y < 0) {
				first = getFirstRow() - 1;
				last = getLastRow() - 1;
				n = first;
			    } else {
				first = getFirstRow() + 1;
				last = getLastRow() + 1;
				n = last;
			    }
			    if (first >= 0 && first < totalcount) {
				setAutoScrollerDelay();
				verticalScrollTo(first*cellsize.height);
				if (n >= first && n <= last)
				    selectRow(n, selecting, false);
				else if (intransient)
				    selectRow(n, selecting, true);
				else selectRow(Math.min(last, Math.max(first, n)), selecting, false);
			    }
			    break;
		    }
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // MouseListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseEntered(MouseEvent e) {

	stopAutoScroller();
    }


    public synchronized void
    mouseExited(MouseEvent e) {

	if (mouse != AVAILABLE) {
	    switch (mouse) {
		case SELECTING:
		case DESELECTING:
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

	if (mouse == AVAILABLE && histogram != null && alive) {
	    mouseposition = e.getPoint();
	    toprow = 0;
	    bottomrow = 0;
	    pressedrow = -1;
	    intransient = transientmode;
	    modifiers = YoixMiscJFC.cookModifiers(e);
	    button = modifiers & YOIX_BUTTON_MASK;
	    op = getOperation(modifiers);
	    if (hasFocus() == false)
		requestFocus();
	    switch (button) {
		case YOIX_BUTTON1_MASK:
		    switch (op) {
			case VL_OP_BRUSH:
			case VL_OP_SELECT:
			    mouse = SELECTING;
			    selectRow(findRowAt(mouseposition), true, false);
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
		    grabBegin(e.getPoint());
		    break;

		case YOIX_BUTTON3_MASK:
		    switch (op) {
			case VL_OP_BRUSH:
			case VL_OP_SELECT:
			    mouse = DESELECTING;
			    selectRow(findRowAt(mouseposition), false, false);
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
	    if (mouse != AVAILABLE)
		addMouseMotionListener(this);
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
	    buttons = YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK;
	    if ((buttons & mousebutton) != 0) {		// test is for Java 1.3.1
		if (histogram != null) {
		    switch (mouse) {
			case DESELECTING:
			case SELECTING:
			    selecting = (mouse == SELECTING);
			    first = getFirstRow();
			    last = getLastRow();
			    row = findRowAt(mouseposition);
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
		stopAutoScroller();
	    }
	}
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

	if (mouse != AVAILABLE && histogram != null) {
	    mouseposition = e.getPoint();
	    switch (mouse) {
		case DESELECTING:
		case SELECTING:
		    selecting = (mouse == SELECTING);
		    first = getFirstRow();
		    last = getLastRow();
		    row = findRowAt(mouseposition);
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
	}
    }


    public synchronized void
    mouseMoved(MouseEvent e) {

	if (histogram != null) {
	    if (ISMAC && (e.getModifiers()&YOIX_CTRL_MASK) != 0)
		mouseDragged(e);
	}
    }

    ///////////////////////////////////
    //
    // DataColorer Methods
    //
    ///////////////////////////////////

    public final synchronized void
    appendRecords(DataRecord loaded[], int offset) {

	HistogramData  element;
	DataRecord     record;
	HitBuffer      hits;
	ArrayList      elements;
	HashMap        map;
	String         name;
	String         secondary;
	int            length;
	int            count;
	int            m;
	int            n;

	if (isReady()) {
	    if (loaded != null && fieldindices != null && fieldmasks != null) {
		length = loaded.length;
		if (offset >= 0 && offset < length) {
		    loadeddata = loaded;
		    hits = new HitBuffer(length - offset);
		    count = 0;
		    if (sweepfiltering == false) {
			datarecords = loaded;
			map = new HashMap(viewermap);
			elements = YoixMisc.copyIntoArrayList(histogram);
			binmap = null;
			for (n = offset; n < length; n++) {
			    if ((record = datarecords[n]) != null) {
				for (m = 0; m < fieldindices.length; m++) {
				    if ((name = getRecordName(record, m)) != null) {
					if (m < fieldmasks.length) {	// what happens here??
					    element = loadData(name, 1, getRecordValues(record, 1), n, map, elements);
					    if (element != null) {
						if (record.isSelected(fieldmasks[m])) {
						    if (element.selected == false) {
							if (record.isSelected())
							    hits.setRecord(count++, record, false);
							record.clearSelected(fieldmasks[m]);
						    }
						} else element.update(-1, getRecordValues(record, -1), false);
						if (diversityindex >= 0) {	// now required
						    if ((secondary = record.getField(diversityindex)) != null)
							element.addDiversity(name, secondary);
						}
					    }
					}
				    }
				}
			    }
			}
			buildViewer(map, elements);
			syncViewport();
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


    public final synchronized int
    compare(DataRecord record1, DataRecord record2) {

	HistogramData  bin1;
	HistogramData  bin2;
	String         name1;
	String         name2;
	int            result;

	//
	// Returns negative if record1 should precede record2, 0 if they
	// can be considered equal, and positive if record2 should precede
	// record1.
	//

	if ((name1 = getRecordName(record1)) != null) {
	    if ((name2 = getRecordName(record2)) != null) {
		if (name1.equals(name2) == false) {
		    if (viewermap != null) {
			if ((bin1 = (HistogramData)viewermap.get(name1)) != null) {
			    if ((bin2 = (HistogramData)viewermap.get(name2)) != null)
				result = bin1.compare(bin2, currentorder);
			    else result = -1;
			} else result = (viewermap.get(name2) != null) ? 1 : 0;
		    } else result = name1.compareTo(name2);
		} else result = 0;
	    } else result = -1;
	} else result = (getRecordName(record1) != null) ? 1 : 0;

	return(result);
    }


    public final Color
    getColor(DataRecord record) {

	HistogramData  bin;
	String         name;
	Color          color;

	if (viewermap != null && (name = getRecordName(record)) != null) {
	    if ((bin = (HistogramData)viewermap.get(name)) != null)
		color = bin.color;
	    else color = null;
	} else color = null;

	return(color);
    }


    public final synchronized Color
    getColor(String name) {

	HistogramData  bin;
	Color          color;

	if (name != null && viewermap != null) {
	    if ((bin = (HistogramData)viewermap.get(name)) != null)
		color = bin.color;
	    else color = null;
	} else color = null;

	return(color);
    }


    public final synchronized String
    getKey(int row) {

	return(row >= 0 && row < totalcount ? histogram[row].text : null);
    }


    public final String
    getName(DataRecord record) {

	return(getRecordName(record));
    }


    public final synchronized String[]
    getNames() {

	String  names[] = null;
	int     n;

	if (totalcount > 0) {
	    names = new String[totalcount];
	    for (n = 0; n < totalcount; n++)
		names[n] = histogram[n].text;
	}
	return(names);
    }


    public final synchronized Object[]
    getNamesAndColors() {

	Object  data[] = null;
	int     m;
	int     n;

	if (totalcount > 0) {
	    data = new Object[2*totalcount];
	    for (n = 0, m = 0; n < totalcount; n++) {
		data[m++] = histogram[n].text;
		data[m++] = histogram[n].color;
	    }
	}
	return(data);
    }


    public final synchronized String
    getTipText(String name, int flags, boolean html) {

	HistogramData  bin;
	String         text;

	if (name != null && viewermap != null) {
	    if ((bin = (HistogramData)viewermap.get(name)) != null)
		text = bin.getTipText(flags, html);
	    else text = null;
	} else text = null;
	return(text);
    }


    public final synchronized String
    getTipTextAt(Point point, int flags, boolean html) {

	HistogramData  bin;
	String         text;
	int            row;

	if ((row = findRowAt(point)) >= 0 && row < totalcount) {
	    bin = histogram[row];
	    text = bin.getTipText(flags, html);
	} else text = null;
	return(text);
    }


    public final synchronized void
    loadRecords(DataRecord loaded[], DataRecord records[]) {

	if (loaded != loadeddata || datarecords != records)
	    loadRecords(loaded, records, true);
    }


    public final synchronized void
    loadRecords(DataRecord loaded[], DataRecord records[], boolean force) {

	HistogramData  element;
	DataRecord     record;
	YoixObject     root;
	ArrayList      elements;
	HashMap        map;
	String         name;
	String         secondary;
	int            length;
	int            m;
	int            n;

	if (isReady() || force) {
	    clearViewer();
	    if (records != null && records.length > 0 && fieldindices != null && fieldmasks != null) {
		length = records.length;
		loadeddata = loaded;
		datarecords = records;
		colormap = new Hashtable();
		diversitymap = new Hashtable();
		map = new HashMap();
		elements = new ArrayList();
		labelmap = new Hashtable();
		for (n = 0; n < length; n++) {
		    if ((record = datarecords[n]) != null) {
			for (m = 0; m < fieldindices.length; m++) {
			    if ((name = getRecordName(record, m)) != null) {
				if (m < fieldmasks.length) {	// what happens here??
				    element = loadData(name, 1, getRecordValues(record, 1), n, map, elements);
				    if (element != null) {
					if (record.notSelected()) {
					    element.update(-1, getRecordValues(record, -1), false);
					    if (record.notSelected(fieldmasks[m]))
						element.selected = false;
					}
					if (sweepfiltering) {
					    colormap.put(name, record);
					    labelmap.put(name, record);
					}
					if (diversityindex >= 0) {	// now required
					    if ((secondary = record.getField(diversityindex)) != null)
						element.addDiversity(name, secondary);
					}
				    }
				}
			    }
			}
		    }
		}
		buildViewer(map, elements);
		setOrigin(new Point(0, 0));
		if (autoshow && sweepfiltering && datamanager != null) {
		    if ((root = data.getObject(N_ROOT)) != null && root.notNull())
			root.putInt(N_VISIBLE, true);
		}
	    }
	    syncViewport();
	    if (datamanager != null)
		afterLoad();
	    reset();
	}
    }


    public final void
    setExtent() {

	HistogramData  tokens[];
	int            width;
	int            n;

	tokens = histogram;			// snapshot - just to be safe
	extent.width = 0;
	extent.height = 0;

	if (tokens != null) {
	    if (loadFont()) {
		setViewportSize();
		if (measuretext) {
		    synchronized(FONTLOCK) {
			for (n = 0; n < tokens.length; n++) {
			    if ((width = fm.stringWidth(tokens[n].text)) > extent.width)
				extent.width = width;
			}
		    }
		} else extent.width = totalsize.width;
		extent.height = cellsize.height*tokens.length;
	    }
	}
    }


    public final synchronized void
    tossLabels() {

	int  n;

	for (n = 0; n < totalcount; n++)
	    histogram[n].tossLabel();
    }

    ///////////////////////////////////
    //
    // DataViewer Methods
    //
    ///////////////////////////////////

    public final synchronized ArrayList
    collectRecordsAt(Point point, boolean selected) {

	DataRecord  record;
	ArrayList   list = null;
	String      key;
	String      name;
	int         length;
	int         total;
	int         row;
	int         m;
	int         n;

	if (totalcount > 0) {
	    if (datamanager != null) {
		if ((row = findRowAt(point)) >= 0 && row < totalcount) {
		    length = datarecords.length;
		    key = getKey(row);
		    total = (key != null) ? getCountTotal(row) : length;
		    list = new ArrayList(total);
		    for (n = 0; n < length && total > 0; n++) {
			record = datarecords[n];
			for (m = 0; m < activefieldcount; m++) {
			    if ((name = getRecordName(record, m)) != null) {
				if (name.equals(key) || key == null) {
				    if (record.isSelected() == selected)
					list.add(new Integer(record.getIndex()));
				}
			    }
			}
		    }
		}
	    }
	}
	return(list != null && list.size() > 0 ? list : null);
    }


    public final synchronized int
    colorViewerWith(Palette palette) {

	HistogramData  bin;
	DataRecord     record;
	boolean        repaint;
	String         name;
	Color          color;
	Color          defaultcolor;
	int            count = 0;
	int            length;
	int            n;

	//
	// Now using totalcount to help decide whether bins need to
	// be repainted (4/10/03).
	//

	if (histogram != null) {
	    defaultcolor = getForeground();
	    repaint = (isShowing() && painted && totalcount > 0);
	    length = histogram.length;
	    for (n = 0; n < length; n++) {
		bin = histogram[n];
		name = bin.text;
		if (bin.isother == false) {
		    if (palette == null) {
			if ((record = (DataRecord)colormap.get(name)) != null) {
			    if ((color = record.getColor()) == null)
				color = defaultcolor;
			} else color = defaultcolor;
		    } else {
			if ((color = palette.selectColor(n, length, null)) == null) {
			    if ((record = (DataRecord)colormap.get(name)) != null) {
				if ((color = record.getColor()) == null)
				    color = defaultcolor;
			    }
			}
		    }
		} else color = othercolor;
		if (stacked || stackstate || (repaint && color != bin.color)) {
		    if (stacked || stackstate || color == null || !color.equals(bin.color)) {
			bin.repaint = true;
			count++;
		    }
		}
		bin.color = color;
	    }
	    stackstate = stacked;
	}
	return(count);
    }


    public final synchronized int
    getCountTotal(int row) {

	return(row >= 0 && row < totalcount ? histogram[row].getCountTotal() : 0);
    }


    public final synchronized boolean
    isHighlighted(int row) {

	return(row >= 0 && row < totalcount && histogram[row].highlight);
    }


    public final synchronized boolean
    isPressed(int row) {

	return(row >= 0 && row < totalcount && histogram[row].pressed);
    }


    public final synchronized boolean
    isSelected(int row) {

	return(row >= 0 && row < totalcount && histogram[row].getCountValue() != 0);
    }


    public final synchronized boolean
    isSelected(String name) {

	HistogramData  bin;
	boolean        result;

	if (viewermap != null) {
	    if ((bin = (HistogramData)viewermap.get(name)) != null)
		result = (bin.getCountValue() != 0);
	    else result = false;
	} else result = false;

	return(result);
    }


    public final synchronized void
    recolorViewer() {

	//
	// Currently only called by the datamanager and only for histograms
	// that are directly associated with plots, which means bar colors
	// are selected from the data rather than the palette, if there is
	// one. Will only result in a performance hit for other histograms,
	// but it's probably not a penalty we want to pay, so be careful
	// making changes here.
	//

	if (sweepfiltering || stacked || stackstate) {
	    if (datamanager != null) {
		//
		// Old version did
		//
		//	loadRecords(loadeddata, datarecords, false);
		//
		// but this is more efficient and should work.
		//
		if (colorViewerWith(currentpalette) > 0 || stackstate || stacked) {
		    if (stacked)
			buildBinMap();
		    if (datarecords != null)		// unnecessary test
			repaintViewer(datarecords.length);
		    else repaintViewer();
		}
	    }
	}
    }


    public final void
    recordsSorted(DataRecord records[]) {

    }


    public final synchronized void
    repaintViewer() {

	StringBuffer  buf;
	Graphics      g;
	Font          font;
	int           length;
	int           first;
	int           last;
	int           tx;
	int           ty;
	int           n;

	if (isShowing()) {
	    if (histogram != null && histogram.length > 0) {
		if ((g = getSavedGraphics()) != null) {
		    font = g.getFont();
		    g.setFont(getFont());
		    tx = insets.left - viewport.x;
		    ty = insets.top - viewport.y;
		    g.translate(tx, ty);
		    length = histogram.length;
		    first = getFirstRow();
		    last = Math.min(getLastRow(), histogram.length - 1);
		    buf = new StringBuffer();
		    for (n = first; n <= last; n++) {
			if (histogram[n].repaint) {
			    histogram[n].repaint = false;
			    repaintBin(n, buf, g);
			}
		    }
		    g.translate(-tx, -ty);
		    g.setFont(font);
		    disposeSavedGraphics(g);
		}
	    }
	}
    }


    public final synchronized void
    repaintViewer(int count) {

	if (count > 0)
	    repaintViewer();
    }


    public final synchronized void
    setHighlighted(YoixObject obj) {

	YoixObject  items;
	HashMap     select;
	HashMap     deselect;

	if (loadeddata != null && viewermap != null) {
	    if (obj.notNull()) {
		if (obj.isArray() || obj.isNumber() || obj.isString()) {
		    if (obj.isArray() == false) {
			items = YoixObject.newArray(1);
			items.put(0, obj, false);
		    } else items = obj;
		    select = new HashMap();
		    deselect = (HashMap)viewermap.clone();
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

	if (loadeddata != null && viewermap != null) {
	    if (obj.notNull()) {
		if (obj.isArray() || obj.isNumber() || obj.isString()) {
		    if (obj.isArray() == false) {
			items = YoixObject.newArray(1);
			items.put(0, obj, false);
		    } else items = obj;
		    select = new HashMap();
		    deselect = (HashMap)viewermap.clone();
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

	if (viewermap != null) {
	    if (obj.notNull()) {
		if (obj.isArray() || obj.isNumber() || obj.isString()) {
		    if (obj.isArray() == false) {
			items = YoixObject.newArray(1);
			items.put(0, obj, false);
		    } else items = obj;
		    select = new HashMap();
		    deselect = (HashMap)viewermap.clone();
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
		for (n = 0; n < histogram.length; n++) {
		    state = select.containsKey(histogram[n].text);
		    if (histogram[n].selected != state) {
			histogram[n].selected = state;
			histogram[n].repaint = true;
			needpaint = true;
		    }
		}
		if ((count = collectRecords(hits, select, deselect)) > 0) {
		    datamanager.updateData(loadeddata, hits, count, this);
		    Thread.yield();
		} else {
		    datamanager.releaseHitBuffer(hits);
		    if (needpaint)
			repaintViewer();
		}
	    }
	}
    }


    public final synchronized void
    updateViewer(DataRecord loaded[], HitBuffer hits, int count) {

	HistogramData  bin;
	DataRecord     record;
	boolean        postevent;
	String         name;
	String         secondary;
	int            sign;
	int            m;
	int            n;

	if (count > 0 && isLoaded()) {
	    if (isReady()) {
		if (loaded == loadeddata && histogram != null && viewermap != null) {
		    postevent = hasItemListener();
		    for (n = 0; n < count; n++) {
			record = hits.getRecord(n);
			for (m = 0; m < activefieldcount; m++) {
			    if ((name = getRecordName(record, m)) != null) {
				if ((bin = (HistogramData)viewermap.get(name)) != null) {
				    sign = hits.isSelected(n) ? 1 : -1;
				    bin.update(sign, getRecordValues(record, sign), postevent);
				    if (diversityindex >= 0) {		// now required
					if ((secondary = record.getField(diversityindex)) != null)
					    bin.updateDiversity(name, secondary, sign);
				    }
				}
			    }
			}
		    }
		    afterUpdate();
		}
	    } else clearViewer();
	}
    }

    ///////////////////////////////////
    //
    // SwingJHistogram Methods
    //
    ///////////////////////////////////

    protected final synchronized YoixObject
    eventCoordinates(AWTEvent e) {

	YoixObject  obj = null;
	int         row;

	if (e instanceof MouseEvent) {
	    if (histogram != null) {
		row = Math.max(-1, Math.min(getRow(((MouseEvent)e).getY()), histogram.length));
		obj = YoixMake.yoixType(T_POINT);
		obj.put(N_X, YoixObject.newDouble(row), false);
		obj.put(N_Y, YoixObject.newDouble(row), false);
	    }
	}

	return(obj);
    }


    protected void
    finalize() {

	stopAutoScroller();
	autoscroller = null;
	histogram = null;
	labelmap = null;
	binmap = null;
	sortingpalette = null;
	super.finalize();
    }


    final String
    findNextMatch(String string, boolean pattern, boolean ignorecase, int direction) {

	YoixRERegexp  re = null;
	StringBuffer  buf;
	String        label;
	int           index = -1;
	int           incr;
	int           count;
	int           row;

	if (string != null && string.length() > 0) {
	    buf = new StringBuffer();
	    if (pattern) {
		re = new YoixRERegexp(
		    string,
		    SHELL_PATTERN|(ignorecase ? CASE_INSENSITIVE|SINGLE_BYTE : SINGLE_BYTE)
		);
	    } else if (ignorecase)
		string = string.toLowerCase();

	    incr = (direction >= 0) ? 1 : -1;
	    row = getHighlightedRow(getFirstRow(), incr);

	    if (incr > 0)
		row += isHighlighted(row) ? incr : 0;
	    else row += incr;

	    for (count = totalcount; count > 0 && index < 0; count--, row += incr) {
		if (row < 0 || row >= totalcount)
		    row = (row < 0) ? totalcount - 1 : 0;
		if ((label = histogram[row].getLabel(buf)) != null) {
		    if (re == null) {
			if (ignorecase)
			    label = label.toLowerCase();
			if (label.indexOf(string) >= 0)
			    index = row;
		    } else if (re.exec(label, null))
			index = row;
		}
	    }
	}
	setHighlighted(index);
	return(getKey(index));
    }


    final boolean
    getAlive() {

	return(alive);
    }


    final boolean
    getAutoScroll() {

	return(autoscroller != null);
    }


    final synchronized YoixObject
    getLabels() {

	StringBuffer  buf;
	YoixObject    obj;
	int           n;

	if (totalcount > 0) {
	    obj = YoixObject.newArray(totalcount);
	    buf = new StringBuffer();
	    for (n = 0; n < totalcount; n++)
		obj.putString(n, histogram[n].getLabel(buf));
	} else obj = YoixObject.newArray();

	return(obj);
    }


    protected final Dimension
    getLayoutSize(String name, Dimension size) {

	HistogramData  tokens[];
	YoixObject     lval;
	YoixObject     obj;
	String         str;
	int            width;
	int            n;

	if (loadFont()) {
	    forceDataLoad();
	    setViewportSize();
	    tokens = histogram;
	    synchronized(FONTLOCK) {
		if ((obj = getData().getObject(name)) != null && obj.notNull()) {
		    size = YoixMakeScreen.javaDimension(obj);
		    if (size.width <= 0 || size.height <= 0) {
			if (size.width <= 0) {
			    if (columns <= 0) {
				if (tokens != null) {
				    for (n = 0; n < tokens.length; n++) {
					str = tokens[n].text;
					if ((width = fm.stringWidth(str)) > size.width)
					    size.width = width;
				    }
				    size.width += insets.left + insets.right;
				}
			    } else size.width = columns*cellsize.width + insets.left + insets.right;
			}
			if (size.height <= 0) {
			    if (rows <= 0) {
				if (tokens != null) {
				    size.height = (tokens.length + 1)*cellsize.height;
				    size.height += insets.top + insets.bottom;
				}
			    } else size.height = rows*cellsize.height + insets.top + insets.bottom;
			}
			if (size.width > 0 && size.height > 0) {
			    lval = YoixObject.newLvalue(getData(), name);
			    if (lval.canWrite())
				lval.put(YoixMakeScreen.yoixDimension(size));
			}
		    }
		} else {
		    //
		    // Eventually be smarter here and have the answer depend
		    // on name (i.e., preferredsize, minimumsize). Probably
		    // can wait because name is almost always preferredsize.
		    //
		    if (columns > 0)
			size.width = columns*cellsize.width + insets.left + insets.right;
		    if (rows <= 0) {
			if (size.height <= 0) {
			    if (tokens != null) {
				size.height = (tokens.length + 1)*cellsize.height;
				size.height += insets.top + insets.bottom;
			    }
			}
		    } else size.height = rows*cellsize.height + insets.top + insets.bottom;
		}
	    }
	}

	return(size);
    }


    final boolean
    getStacked() {

	return(stacked);
    }


    protected final synchronized void
    paintRect(int x, int y, int width, int height, Graphics g) {

	StringBuffer  buf;
	Rectangle     rect;
	Shape         clip;
	Font          font;
	int           last;
	int           n;

	if (histogram != null) {
	    clip = g.getClip();
	    font = g.getFont();
	    g.setFont(getFont());
	    g.translate(-viewport.x, -viewport.y);
	    g.clipRect(x, y, width, height);		// recent change
	    rect = g.getClipBounds();
	    last = Math.min((rect.y + rect.height)/cellsize.height, histogram.length - 1);
	    buf = new StringBuffer();
	    for (n = Math.max(y/cellsize.height, 0); n <= last; n++)
		repaintBin(n, buf, g);
	    g.translate(viewport.x, viewport.y);
	    g.setFont(font);
	    g.setClip(clip);
	}
    }


    protected final synchronized void
    setAlignment(int alignment) {

	if (this.alignment != alignment) {
	    switch (alignment) {
		case YOIX_RIGHT:
		case YOIX_EAST:
		    this.alignment = YOIX_RIGHT;
		    break;

		default:
		case YOIX_LEFT:
		case YOIX_WEST:
		    this.alignment = YOIX_LEFT;
		    break;
	    }
	    reset(false);
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
	    reset(false);
	}
    }


    final void
    setAlive(boolean state) {

	alive = state;
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


    final synchronized void
    setBarSpace(double value) {

	value = Math.max(value, 0);

	if (barspace != value) {
	    barspace = value;
	    reset();
	}
    }


    public synchronized void
    setBounds(int x, int y, int width, int height) {

	if (totalsize.width != width || totalsize.height != height) {
	    totalsize.width = width;
	    totalsize.height = height;
	}

	super.setBounds(x, y, width, height);
	setViewportSize();
	setExtent();		// recent addition
	syncViewport();
    }


    final synchronized void
    setHideUnlabeled(boolean state) {

	if (hideunlabeled != state) {
	    hideunlabeled = state;
	    loadRecords(loadeddata, datarecords, false);
	}
    }


    final synchronized void
    setRecolored(boolean recolored) {

	if (loadeddata != null && datarecords != null && datamanager != null) {
	    if (recolored)
		datamanager.recolorViewer(this, getFieldIndex(), OTHERNAME);
	    else datamanager.uncolorViewer(this, getFieldIndex());
	}
    }


    final synchronized void
    setSortBy(int neworder) {

	boolean  force;

	switch (neworder) {
	    case VL_SORT_COLOR:
	    case VL_SORT_PRESSED:
	    case VL_SORT_SELECTED:
	    case VL_SORT_SELECTED2:
		force = true;
		break;

	    default:
		force = false;
		break;
	}

	if (sortHistogram(neworder, force)) {
	    reset();
	    if (datamanager != null)
		datamanager.recolorData(this);
	}
    }


    final synchronized void
    setSortDefault(int order) {

	sortdefault = order;
    }


    final synchronized void
    setStacked(boolean mode) {

	if (mode != stacked) {
	    stacked = mode;
	    recolorViewer();
	}
    }


    final synchronized void
    setText(String text) {

	DataRecord  records[];
	ArrayList   list;
	int         length;
	int         n;

	//
	// The datamanager, if there is one, has control so we only allow
	// changes via the text field when datamanager is null. No errors
	// either, which is probably OK, because this is currently always
	// called when the histogram is created - may eventually change.
	// We toss the trailing newline, if there is one, because we want
	// to be compatible with getText() and that should make split()
	// behave properly.
	//

	if (datamanager == null && text != null) {
	    if (isLoaded() || text.length() > 0) {
		if (text.endsWith("\n"))
		    text = text.substring(0, text.length() - 1);
		list = YoixMisc.split(text, "\n");
		length = list.size();
		records = new DataRecord[length];
		for (n = 0; n < length; n++)
		    records[n] = new DataRecord(new Object[] {list.get(n)}, n);
		clearViewer();
		setDataManager(0);
		loadRecords(records, records, true);
	    }
	}
    }


    final synchronized void
    setTranslator(YoixObject obj) {

	translator = (obj != null && obj.notNull()) ? obj : null;
    }


    final synchronized void
    setViewportSize() {

	super.setViewportSize();
	maxbar = (int)(barspace > 1 ? barspace : barspace*viewport.width);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    buildBinMap() {

	StackedRecord  records[];
	DataRecord     record;
	ArrayList      list;
	HashMap        map;
	String         name;
	int            m;
	int            n;

	//
	// Builds a HashMap that maps bin names to an array of DataRecords
	// that are associated with each bin. Currently only used when we
	// need to draw stacked bars, so that's the only time it's built.
	//

	if (datarecords != null && histogram != null) {
	    if (stacked) {
		map = new HashMap();
		for (n = 0; n < datarecords.length; n++) {
		    record = datarecords[n];
		    for (m = 0; m < fieldindices.length; m++) {
			if ((name = getRecordName(record, m)) != null) {
			    if (m < fieldmasks.length) {	// what happens here??
				if ((list = (ArrayList)map.get(name)) == null) {
				    list = new ArrayList();
				    map.put(name, list);
				}
				list.add(new StackedRecord(record));
			    }
			}
		    }
		}
		if (datamanager != null)
		    sortingpalette = datamanager.getCurrentPalette();
		else sortingpalette = null;
		for (n = 0; n < histogram.length; n++) {
		    name = histogram[n].text;
		    if ((list = (ArrayList)map.get(name)) != null) {
			records = new StackedRecord[list.size()];
			for (m = 0; m < records.length; m++)
			    records[m] = (StackedRecord)list.get(m);
			YoixMiscQsort.sort(records, VL_SORT_COLOR);
			map.put(name, records);
		    }
		}
		sortingpalette = null;
		binmap = map;
	    }
	}
    }


    private void
    buildViewer(HashMap map, ArrayList elements) {

	HistogramData  element;
	double         value;
	int            neworder;
	int            n;

	selectedcount = 0;
	totalcount = elements.size();
	histogram = new HistogramData[totalcount];
	maxbar = 0;
	maxvalue = 0;
	for (n = 0; n < totalcount; n++) {
	    if ((element = (HistogramData)elements.get(n)) != null) {
		selectedcount += element.selected ? 1 : 0;
		if ((value = element.getActiveTotal()) > maxvalue)
		    maxvalue = value;
	    }
	    histogram[n] = element;
	}
	viewermap = map;
	neworder = currentorder;
	currentorder = VL_SORT_LOAD_ORDER;
	if (sortHistogram(neworder, viewermap.containsKey(OTHERNAME)) == false)
	    colorViewer();
	setExtent();
	buildBinMap();
    }


    private void
    clearViewer() {

	totalcount = 0;			// probably should be first
	selectedcount = 0;
	maxvalue = 0;
	maxbar = 0;
	histogram = null;
	viewermap = null;
	colormap = null;
	datarecords = null;
	loadeddata = null;
	diversitymap = null;
	labelmap = null;
	binmap = null;
	setExtent();
    }


    private void
    drawBar(int row, Graphics g) {

	HistogramData  bin;
	boolean        selected;
	boolean        flipfill;
	double         value;
	double         total;
	Color          color;
	int            barwidth;
	int            barheight;
	int            width;
	int            padx;
	int            x;
	int            y;

	bin = histogram[row];
	selected = isSelected(row);	// changed on 9/2/04
	flipfill = false;
	value = bin.getActiveValue();
	total = bin.getActiveTotal();
	y = row*cellsize.height + ipad.top;

	barwidth = maxbar - (ipad.left + ipad.right);
	barheight = cellsize.height - (ipad.top + ipad.bottom + 1);

	switch (anchor) {
	    case YOIX_LEFT:
	    default:
		padx = ipad.left;
		switch (alignment) {
		    case YOIX_LEFT:
			barwidth = (int)(barwidth*total/maxvalue);
			x = maxbar;
			break;

		    case YOIX_RIGHT:
		    default:
			barwidth = (int)(barwidth*total/maxvalue);
			x = 0;
			break;
		}
		break;

	    case YOIX_RIGHT:
		flipfill = true;
		padx = ipad.right;
		switch (alignment) {
		    case YOIX_LEFT:
		    default:
			barwidth = (int)(barwidth*total/maxvalue);
			x = viewport.width - barwidth - padx;
			break;

		    case YOIX_RIGHT:
			barwidth = (int)(barwidth*total/maxvalue);
			x = maxbar - barwidth - padx;
			break;
		}
		break;
	}
	if (selected == false || value != total) {
	    g.setColor(emptycolor);
	    g.fillRect(x, y, barwidth + padx, barheight);
	}
	if (selected && (value > 0 || value == total)) {
	    g.setColor(bin.color);
	    if (value < total) {
		width = (int)((barwidth*value)/total) + padx;
		if (flipfill)
		    g.fillRect(x + barwidth - width + padx, y, width, barheight);
		else g.fillRect(x, y, width, barheight);
	    } else g.fillRect(x, y, barwidth + padx, barheight);
	}
    }


    private void
    drawLabel(int row, StringBuffer buf, Graphics g) {

	HistogramData  bin;
	String         label;
	Color          color;
	int            separation;
	int            width;
	int            x;
	int            y;

	//
	// The separation of the bar and label needs improvement - later.
	//

	bin = histogram[row];

	if ((label = bin.getLabel(buf)) != null) {
	    separation = cellsize.width/2;
	    if ((width = fm.stringWidth(label)) > 0) {
		switch (anchor) {
		    case YOIX_LEFT:
		    default:
			switch (alignment) {
			    case YOIX_LEFT:
				x = maxbar - (separation + width);
				break;

			    case YOIX_RIGHT:
			    default:
				x = maxbar + separation;
				break;
			}
			break;

		    case YOIX_RIGHT:
			switch (alignment) {
			    case YOIX_LEFT:
			    default:
				x = maxbar - (separation + width);
				break;

			    case YOIX_RIGHT:
				x = maxbar + separation;
				break;
			}
			break;
		}
		y = row*cellsize.height;
		if (bin.pressed || bin.highlight || row == pressedrow) {
		    drawLabelBackground(row, separation, g);
		    if (row != pressedrow || bin.pressed == pressedstart) {
			if (bin.highlight)
			    color = bin.selected ? getBackground() : emptycolor;
			else color = bin.selected ? getForeground() : emptycolor;
		    } else color = bin.selected ? getForeground() : emptycolor;
		} else color = bin.selected ? getForeground() : emptycolor;
		g.setColor(color);
		g.drawString(label, x, y + baseline + 1);
	    }
	}
    }


    private void
    drawLabelBackground(int row, int separation, Graphics g) {

	HistogramData  bin;
	Color          color;
	int            width;
	int            height;
	int            x;
	int            y;

	//
	// Really only needed when the bin is being pressed or already has
	// its pressed or highlight flags set to true.
	//

	bin = histogram[row];
	switch (anchor) {
	    case YOIX_LEFT:
	    default:
		switch (alignment) {
		    case YOIX_LEFT:
			x = 0;
			width = maxbar - separation;
			break;

		    case YOIX_RIGHT:
		    default:
			x = maxbar + separation;
			width = viewport.x + viewport.width - x;
			break;
		}
		break;

	    case YOIX_RIGHT:
		switch (alignment) {
		    case YOIX_LEFT:
		    default:
			x = 0;
			width = maxbar - separation;
			break;

		    case YOIX_RIGHT:
			x = maxbar + separation;
			width = viewport.x + viewport.width - x;
			break;
		}
		break;
	}
	y = row*cellsize.height + ipad.top;
	height = cellsize.height - (ipad.top + ipad.bottom + 1);
	if (row != pressedrow || bin.pressed == pressedstart) {
	    if (bin.highlight)
		color = pickHighlightColor(bin.color);
	    else if (bin.pressed)
		color = pickPressedColor(bin.color);
	    else color = getBackground();
	} else color = pickPressingColor(bin.color);
	g.setColor(color);
	g.fillRect(x, y, width, height);
    }


    private void
    drawStackedBar(int row, Graphics g) {

	StackedRecord  records[];
	StackedRecord  stackedrecord;
	HistogramData  bin;
	DataRecord     record;
	boolean        selected;
	boolean        flipfill;
	double         delta;
	double         value;
	double         total;
	double         width;
	String         name;
	Color          color;
	int            barwidth;
	int            barheight;
	int            padx;
	int            x;
	int            y;
	int            m;
	int            n;

	//
	// The way padx is used here is currently different than the way
	// we use it in drawBar(), where bars are only drawn in one color.
	// We suspect the differences could be eliminated by copying the
	// the approach that we use here to drawBar(), however we didn't
	// have the time for thorough testing so the change will have to
	// to wait even though the changes look pretty simple.
	//

	bin = histogram[row];
	selected = isSelected(row);	// changed on 9/2/04
	flipfill = false;
	value = bin.getActiveValue();
	total = bin.getActiveTotal();
	y = row*cellsize.height + ipad.top;

	barwidth = maxbar - (ipad.left + ipad.right);
	barheight = cellsize.height - (ipad.top + ipad.bottom + 1);

	switch (anchor) {
	    case YOIX_LEFT:
	    default:
		padx = ipad.left;
		switch (alignment) {
		    case YOIX_LEFT:
			barwidth = (int)(barwidth*total/maxvalue) + padx;
			x = maxbar;
			break;

		    case YOIX_RIGHT:
		    default:
			barwidth = (int)(barwidth*total/maxvalue) + padx;
			x = 0;
			break;
		}
		break;

	    case YOIX_RIGHT:
		flipfill = true;
		padx = ipad.right;
		switch (alignment) {
		    case YOIX_LEFT:
		    default:
			barwidth = (int)(barwidth*total/maxvalue) + padx;
			x = viewport.width - barwidth;
			break;

		    case YOIX_RIGHT:
			barwidth = (int)(barwidth*total/maxvalue) + padx;
			x = maxbar - barwidth;
			break;
		}
		break;
	}

	if (selected == false || value != total) {
	    g.setColor(emptycolor);
	    g.fillRect(x, y, barwidth, barheight);
	}
	if (selected && (value > 0 || value == total)) {
	    name = bin.text;
	    color = null;
	    delta = 0;
	    if (value != total)		// probably should check for 0
		width = ((barwidth*value)/total);
	    else width = barwidth;
	    if ((records = (StackedRecord[])binmap.get(name)) != null) {
		for (n = 0; n < records.length; n++) {
		    record = records[n].getDataRecord();
		    if (record.isSelected()) {
			if (color != null && color.equals(record.getColor()) == false) {
			    g.setColor(color);
			    if (flipfill)
				g.fillRect(x + barwidth - (int)(width + .5), y, (int)(width + .5), barheight);
			    else g.fillRect(x, y, (int)(width + .5), barheight);
			    if (delta != 0 && total != 0)
				width -= (barwidth*delta)/total;
			    delta = 0;
			}
			for (m = 0; m < fieldindices.length; m++) {
			    if (name.equals(getRecordName(record, m))) {
				if (m < fieldmasks.length) {	// what happens here??
				    delta += getActiveValue(record);
				    color = record.getColor();
				}
				break;
			    }
			}
		    }
		}
		if (width > 0 && color != null) {
		    g.setColor(color);
		    if (flipfill)
			g.fillRect(x + barwidth - (int)(width + .5), y, (int)(width + .5), barheight);
		    else g.fillRect(x, y, (int)(width + .5), barheight);
		}
	    }
	}
    }


    private synchronized int
    findRowAt(Point point) {

	return(Math.max(-1, Math.min(getRow((int)point.getY()), histogram.length)));
    }


    private synchronized int
    findRowAt(Point point, int fail) {

	int  index;

	if ((index = findRowAt(point)) < 0 || index >= histogram.length)
	    index = fail;
	return(index);
    }


    private String
    getLabelPrefix(String name) {

	Object  value;

	//
	// The special sweepfiltering code lets us postpone the evaluation
	// of fields that are only occasionally needed. Done mostly because
	// we noticed delays when all sweepfilter labels (which can be long)
	// were built when the histogram was loaded. This way means we only
	// build labels for the visible bins.
	//

	if (sweepfiltering && name != null) {
	    if ((value = labelmap.get(name)) != null) {
		if (value instanceof DataRecord) {
		    value = ((DataRecord)value).getField(getFieldIndex());
		    if (value instanceof String) {
			labelmap.put(name, value);
			name = (String)value;
		    } else labelmap.remove(name);
		} else if (value instanceof String)
		    name = (String)value;
		else labelmap.remove(name);
	    }
	}
	return(name);
    }


    private synchronized int
    getFirstRow() {

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

	return(cellsize.height > 0 ? (viewport.y + viewport.height - cellsize.height + 1)/cellsize.height : -1);
    }


    private synchronized int
    getLastRow() {

	return(cellsize.height > 0 ? (viewport.y + viewport.height)/cellsize.height : -1);
    }


    private synchronized int
    getRow(int y) {

	int  num;

	return(cellsize.height > 0 && (num = (viewport.y - insets.top + y)) >= 0
	    ? num/cellsize.height
	    : -1
	);
    }


    private void
    grabBegin(Point p) {

	HistogramData  bin;
	int            row;

	if ((row = findRowAt(p, -1)) >= 0) {
	    pressedrow = row;
	    bin = histogram[pressedrow];
	    pressedstart = bin.pressed;
	    bin.repaint = true;
	    bin.pressed = !pressedstart;
	    repaintViewer();
	}
    }


    private void
    grabDragged(Point point) {

	int  first;
	int  delta;
	int  row;

	if (pressedrow != -1) {
	    first = getFirstRow();
	    delta = pressedrow - getRow(point.y);
	    row = Math.max(0, Math.min(first + delta, histogram.length - 1));
	    if (row != first)
		verticalScrollTo(row*cellsize.height);
	}
    }


    private void
    grabEnd(Point point) {

	HistogramData  bin;

	if (pressedrow != -1) {
	    bin = histogram[pressedrow];
	    bin.pressed = pressedstart;
	    bin.repaint = true;
	    pressedrow = -1;
	    repaintViewer();
	}
    }


    private synchronized HistogramData
    loadData(String name, int count, double values[], int id, HashMap map, ArrayList elements) {

	HistogramData  bin;

	//
	// We assume the caller guarantees name isn't null.
	//

	if (hideunlabeled == false || name.length() > 0) {
	    if ((bin = (HistogramData)map.get(name)) == null) {
		bin = new HistogramData(name, id, values.length + 1);
		map.put(name, bin);
		elements.add(bin);
	    }
	    bin.load(Math.max(1, count), values);
	} else bin = null;
	return(bin);
    }


    private synchronized void
    pickBins(YoixObject list, HashMap select, HashMap deselect) {

	HistogramData  bin;
	YoixObject     item;
	DataRecord     record;
	String         name;
	String         key;
	int            index;
	int            length;
	int            n;

	//
	// Numbers now always reference records in loadeddata rather than
	// individual bins in the histogram[] array. It's a small change
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
			    if ((bin = (HistogramData)viewermap.get(name)) != null)
				key = bin.text;
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


    private void
    pressBegin(Point p) {

	HistogramData  bin;
	int            row;

	if ((row = findRowAt(p, -1)) >= 0) {
	    bin = histogram[row];
	    switch (mouse) {
		case PRESSING:
		    if (getAfterPressed() != null) {
			pressedrow = row;
			pressedstart = bin.pressed;
			bin.repaint = true;
			bin.pressed = !pressedstart;
			repaintViewer();
		    }
		    break;

		case TOGGLING:
		    pressedrow = row;
		    pressedstart = bin.pressed;
		    bin.repaint = true;
		    bin.pressed = !pressedstart;
		    repaintViewer();
		    break;
	    }
	}
    }


    private void
    pressDragged(Point p) {

	HistogramData  bin;

	if (pressedrow != -1) {
	    bin = histogram[pressedrow];
	    if (pressedrow == findRowAt(p, -1)) {
		if (bin.pressed == pressedstart) {
		    bin.repaint = true;
		    bin.pressed = !pressedstart;
		    repaintViewer();
		}
	    } else {
		if (bin.pressed != pressedstart) {
		    bin.repaint = true;
		    bin.pressed = pressedstart;
		    repaintViewer();
		}
	    }
	}
    }


    private void
    pressEnd(MouseEvent e) {

	HistogramData  bin;
	boolean        pressed;

	if (pressedrow != -1) {
	    bin = histogram[pressedrow];
	    pressedrow = -1;
	    switch (mouse) {
		case PRESSING:
		    pressed = bin.pressed;
		    bin.pressed = pressedstart;
		    bin.repaint = true;
		    repaintViewer();
		    if (pressed != pressedstart)
			afterPressed(bin.text, e);
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
		    repaintViewer();
		    break;
	    }
	}
    }


    private void
    repaintBin(int row, StringBuffer buf, Graphics g) {

	Color  color;

	//
	// Implicitly assumes the caller is properly synchronized, so be
	// careful!! Also suspect we might improve the performance if we
	// drew all bars were drawn before the labels because we could
	// reduce graphics state color changes with a few simple checks
	// in drawLabel() and drawBar(). No idea if this would make the
	// viusual display more distracting than it current is and also
	// don't know if our Swing implementation would be helped. May
	// investigate - later.
	//

	if (histogram != null) {
	    if (row >= 0 && row < histogram.length) {
		color = g.getColor();
		g.setColor(getBackground());
		g.fillRect(viewport.x, row*cellsize.height, viewport.width, cellsize.height);
		if (stacked)
		    drawStackedBar(row, g);
		else drawBar(row, g);
		drawLabel(row, buf, g);
		g.setColor(color);
	    }
	}
    }


    private synchronized void
    repaintHistogram(int row, boolean forceposition) {

	int  first;
	int  last;

	if (isShowing()) {
	    if (histogram != null) {
		if (row >= 0 && row < histogram.length) {
		    first = getFirstRow();
		    last = getLastCompleteRow();
		    if (row < first || row > last || forceposition) {
			verticalScrollTo(row*cellsize.height);
			reset();	// sometimes needed to erase old hightlight
		    }
		}
		repaintViewer();
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

	if (histogram != null && datamanager != null) {
	    if (intransient == false) {
		if (row >= -1 && row <= histogram.length) {
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
			    if (n >= 0 && n < histogram.length) {
				if (histogram[n].selected != selected) {
				    histogram[n].selected = selected;
				    histogram[n].repaint = true;
				    touched.add(new Integer(n));
				}
			    }
			}
			updateData(touched, selected);
		    }
		}
	    } else {
		if (row >= -1 && row <= histogram.length) {
		    if (released || row < toprow || row >= bottomrow) {
			if (toprow != bottomrow) {
			    histogram[toprow].selected = !selected;
			    histogram[toprow].repaint = true;
			    updateData(toprow, !selected);
			    toprow = 0;
			    bottomrow = 0;
			}
			if (released == false && row >= 0 && row < histogram.length) {
			    if (histogram[row].selected != selected) {
				histogram[row].selected = selected;
				histogram[row].repaint = true;
				toprow = row;
				bottomrow = row + 1;
				updateData(row, selected);
			    }
			}
		    }
		}
	    }
	}
    }


    private synchronized void
    setAutoScrollerDelay() {

	double  factor;
	int     delay;
	int     distance;

	if (autoscroller != null && mouseposition != null) {
	    if (mouseposition.y < 0 || mouseposition.y > viewport.height) {
		if (mouseposition.y < 0)
		    distance = -mouseposition.y;
		else distance = mouseposition.y - viewport.height;
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

	HistogramData  bin;
	boolean        needpaint;
	boolean        state;
	int            row;
	int            n;

	if (loadeddata != null && datamanager != null) {
	    needpaint = false;
	    row = -1;
	    for (n = 0; n < histogram.length; n++) {
		bin = histogram[n];
		state = select.containsKey(bin.text);
		if (bin.highlight != state) {
		    bin.highlight = state;
		    bin.repaint = true;
		    needpaint = true;
		}
		if (row < 0 && bin.highlight)
		    row = n;
	    }
	    if (needpaint || row >= 0)
		repaintHistogram(row, false);
	}
    }


    private synchronized void
    setPressed(HashMap select) {

	HistogramData  bin;
	boolean        needpaint;
	boolean        state;
	int            n;

	if (loadeddata != null && datamanager != null) {
	    needpaint = false;
	    for (n = 0; n < histogram.length; n++) {
		bin = histogram[n];
		state = select.containsKey(bin.text);
		if (bin.pressed != state) {
		    bin.pressed = state;
		    bin.repaint = true;
		    needpaint = true;
		}
	    }
	    if (needpaint)
		repaintViewer();
	}
    }


    private synchronized boolean
    sortHistogram(int neworder, boolean force) {

	boolean  result = false;

	if (histogram != null) {
	    if ((currentorder != neworder) || force) {
		if (datamanager != null)
		    sortingpalette = datamanager.getCurrentPalette();
		else sortingpalette = null;
		YoixMiscQsort.sort(histogram, neworder);
		sortingpalette = null;
		colorViewer();
		result = true;
	    }
	}
	currentorder = neworder;
	return(result);
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

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class HistogramData

	implements YoixInterfaceSortable

    {

	String  text;
	int     id;

	boolean  selected = true;
	boolean  repaint = false;
	boolean  isother = false;
	boolean  pressed = false;
	boolean  highlight = false;
	String   prefix = null;
	String   label = null;
	double   counters[];
	double   totals[];
	Color    color = Color.white;
	int      diversity = 0;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	HistogramData(String text, int id, int slots) {

	    this.text = text;
	    this.id = id;		// increasing integer for sorting
	    this.isother = (text == OTHERNAME);
	    this.counters = new double[slots];
	    this.totals = new double[slots];
	}

	///////////////////////////////////
	//
	// YoixInterfaceSortable Methods
	//
	///////////////////////////////////

	public final int
	compare(YoixInterfaceSortable element, int flag) {

	    HistogramData  bin;
	    double         val;
	    int            sign;

	    //
	    // Not synchronized, which means we assume an earlier method
	    // locked the Histogram. Could be problems, primarily in the
	    // cases that do division, if that assumption is wrong.
	    //

	    bin = (HistogramData)element;
	    sign = (flag >= 0) ? 1 : -1;

	    if (isother == false && bin.isother == false) {
		switch (sign*flag) {
		    case VL_SORT_COLOR:
			if (sortingpalette != null)
			    val = sortingpalette.getIndex(color) - sortingpalette.getIndex(bin.color);
			else val = 0;
			break;

		    case VL_SORT_DIVERSITY:
			if ((val = bin.diversity - diversity) == 0) {
			    if ((val = bin.counters[active] - counters[active]) == 0)
				val = bin.totals[active] - totals[active];
			}
			break;

		    case VL_SORT_DIVERSITY2:
			val = (diversity != 0 && bin.diversity != 0)
			    ? bin.counters[active]/bin.diversity - counters[active]/diversity
			    : 0;
			if (val == 0) {
			    if ((val = bin.diversity - diversity) == 0) {
				if ((val = bin.counters[active] - counters[active]) == 0)
				    val = bin.totals[active] - totals[active];
			    }
			}
			break;

		    case VL_SORT_OCTET:
			val = Misc.compareOctets(text, bin.text);
			break;

		    case VL_SORT_PRESSED:
			val = (pressed != bin.pressed) ? (pressed ? -1 : 1) : 0;
			break;

		    case VL_SORT_NUMERIC:
			val = YoixMake.javaDouble(text, Double.NaN) - YoixMake.javaDouble(bin.text, Double.NaN);
			if (Double.isNaN(val)) {
			    if (Double.isNaN(YoixMake.javaDouble(text, Double.NaN))) {
				if (Double.isNaN(YoixMake.javaDouble(bin.text, Double.NaN)))
				    val = 0;
				else val = 1;
			    } else val = -1;
			}
			break;

		    case VL_SORT_SELECTED:
			if ((val = bin.counters[active] - counters[active]) == 0)
			    val = bin.totals[active] - totals[active];
			break;

		    case VL_SORT_SELECTED2:
			val = (totals[active] != 0 && bin.totals[active] != 0)
			    ? bin.counters[active]/bin.totals[active] - counters[active]/totals[active]
			    : 0;
			if (val == 0)		// value only check is sufficient
			    val = bin.counters[active] - counters[active];
			break;

		    case VL_SORT_TEXT:
			val = text.compareTo(bin.text);
			break;

		    case VL_SORT_LOAD_ORDER:
		    case VL_SORT_TIME:
			val = -(bin.id - id);	// smaller id's are older
			break;

		    case VL_SORT_TOTAL:
			val = bin.totals[active] - totals[active];
			break;

		    case VL_SORT_TRANSLATOR:
			if (translator != null)
			    val = translator.getString(text, text).compareTo(translator.getString(bin.text, bin.text));
			else val = 0;
			break;

		    default:
			val = 0;
			break;
		}
		if (val == 0) {
		    if (sortdefault != flag && sortdefault != VL_SORT_TEXT)
			val = compare(bin, sortdefault);
		}
	    } else val = isother ? 1 : -1;

	    return(sign*(val == 0 ? text.compareTo(bin.text) : (val > 0 ? 1 : -1)));
	}

	///////////////////////////////////
	//
	// HistogramData Methods
	//
	///////////////////////////////////

	final void
	addDiversity(String primary, String secondary) {

	    String  key;
	    int     counter[];

	    key = primary + "\u0001" + secondary;

	    if ((counter = (int[])diversitymap.get(key)) == null) {
		counter = new int[1];
		diversitymap.put(key, counter);
		diversity++;
	    }
	    counter[0]++;
	}


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


	final int
	getDiversity() {

	    return(diversity);
	}


	final String
	getLabel(StringBuffer buf) {

	    boolean  showcount;
	    boolean  showcountpcnt;
	    boolean  showvalue;
	    boolean  showvaluepcnt;
	    boolean  showdiversity;

	    if (label == null && (labelflags&LABEL_HIDE) == 0) {
		if (prefix != null || (prefix = getLabelPrefix(text)) != null) {
		    showcount = ((labelflags&LABEL_SHOWCOUNT) != 0);
		    showcountpcnt = ((labelflags&LABEL_SHOWCOUNTPCNT) != 0);
		    showvalue = ((labelflags&LABEL_SHOWVALUE) != 0);
		    showvaluepcnt = ((labelflags&LABEL_SHOWVALUEPCNT) != 0);
		    showdiversity = (diversityindex >= 0 && ((labelflags&LABEL_SHOWDIVERSITY) != 0));
		    if (buf != null) {
			buf.setLength(0);
			buf.append(prefix);
		    } else buf = new StringBuffer(prefix);
		    appendExtra(buf, showcount, showcountpcnt, showvalue, showvaluepcnt, showdiversity);
		    label = buf.toString();
		}
	    }
	    return(label);
	}


	final String
	getTipText(int flags, boolean html) {

	    StringBuffer  buf;
	    boolean       showcount;
	    boolean       showcountpcnt;
	    boolean       showvalue;
	    boolean       showvaluepcnt;
	    boolean       showdiversity;
	    String        tiptext;

	    if (prefix != null || (prefix = getLabelPrefix(text)) != null) {
		showcount = ((flags&TIP_SHOW_COUNT) != 0) && (getTotalAt(0) != 0);
		showcountpcnt = ((flags&TIP_SHOW_COUNTPCNT) != 0) && (getTotalAt(0) != 0);
		showvalue = ((flags&TIP_SHOW_VALUE) != 0) && (getTotalAt(active > 0 ? active : 1) != 0);
		showvaluepcnt = ((flags&TIP_SHOW_VALUEPCNT) != 0) && (getTotalAt(active > 0 ? active : 1) != 0);
		showdiversity = false;
		if (showcount || showvalue || showdiversity) {
		    buf = new StringBuffer();
		    if (html) {
			buf.append("<html>&nbsp;");
			buf.append(YoixMisc.htmlFromAscii(prefix));
		    } else buf.append(prefix);
		    appendExtra(buf, showcount, showcountpcnt, showvalue, showvaluepcnt, showdiversity);
		    if (html)
			buf.append("&nbsp;</html>");
		    tiptext = buf.toString();
		} else tiptext = html ? YoixMisc.htmlFromAscii(prefix) : prefix;
	    } else tiptext = null;
	    return(tiptext);
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
	    label = null;
	}


	final void
	setDiversity(int value) {

	    diversity = Math.max(0, value);
	}


	final void
	tossLabel() {

	    label = null;
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
	    label = null;
	    repaint = true;
	    endstate = (counters[active] != 0);

	    if (isother)
		selected = endstate;

	    if (startstate != endstate) {
		selectedcount += endstate ? 1 : -1;
		if (postevent)
		    postItemEvent(YoixObject.newString(text), endstate, true);
	    }
	}


	final void
        updateDiversity(String primary, String secondary, int incr) {

	    String  key;
	    int     counter[];

	    key = primary + "\u0001" + secondary;

	    if ((counter = (int[])diversitymap.get(key)) != null) {
		counter[0] += incr;
		if (counter[0] == 0 && incr < 0) {
		    if (diversity > 0)
			diversity--;
		} else if (counter[0] == 1 && incr > 0)
		    diversity++;
	    }
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	private void
	appendExtra(StringBuffer buf, boolean showcount, boolean showcountpcnt, boolean showvalue, boolean showvaluepcnt, boolean showdiversity) {

	    if (buf != null) {
		if (showcount || showcountpcnt || showvalue || showvaluepcnt) {
		    if (buf.length() > 0)
			buf.append(" (");
		    else buf.append("(");
		    if (showvalue || showvaluepcnt) {
			if (showvalue) {
			    buf.append((int)(getValueAt(active > 0 ? active : 1) + .5));
			    if (showvaluepcnt)
				buf.append(":");
			}
			if (showvaluepcnt) {
			    buf.append((int)(100.0 * getValueAt(active > 0 ? active : 1)/getTotalAt(active > 0 ? active : 1) + .5));
			    buf.append("%");
			}
			if (showcount || showcountpcnt) {
			    buf.append("|");
			    if (showcount) {
				buf.append((int)getValueAt(0));
				if (showcountpcnt)
				    buf.append(":");
			    }
			    if (showcountpcnt) {
				buf.append((int)(100.0 * getValueAt(0)/getTotalAt(0) + .5));
				buf.append("%");
			    }
			    buf.append(")");
			}
		    } else {
			if (showcount) {
			    buf.append((int)getValueAt(0));
			    if (showcountpcnt)
				buf.append(":");
			}
			if (showcountpcnt) {
			    buf.append((int)(100.0 * getValueAt(0)/getTotalAt(0) + .5));
			    buf.append("%");
			}
		    }
		    if (showdiversity) {
			buf.append("/");
			buf.append(diversity);
		    }
		    if (showcount || showvalue) {
			buf.append(" of ");
			if (showvalue) {
			    if (showcount || showcountpcnt)
				buf.append("(");
			    buf.append((int)(getTotalAt(active > 0 ? active : 1) + .5));
			    if (showcount) {
				buf.append("|");
				buf.append((int)getTotalAt(0));
			    }
			} else buf.append((int)getTotalAt(0));
		    }
		    buf.append(")");
		}
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class StackedRecord

	implements YoixInterfaceSortable

    {

	//
	// A wrapper for individual DataRecords that can be grouped into
	// bins and sorted by color whenever we're dealing with Histograms
	// that are stacked.
	//

	DataRecord  datarecord;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	StackedRecord(DataRecord record) {

	    this.datarecord = record;
	}

	///////////////////////////////////
	//
	// YoixInterfaceSortable Methods
	//
	///////////////////////////////////

	public final int
	compare(YoixInterfaceSortable element, int flag) {

	    Color  color1;
	    Color  color2;
	    int    result;

	    color1 = getColor();
	    color2 = ((StackedRecord)element).getColor();

	    if (color1.equals(color2) == false) {
		if (sortingpalette != null)
		    result = sortingpalette.getIndex(color1) - sortingpalette.getIndex(color2);
		else result = color1.getRGB() - color2.getRGB();
	    } else result = 0;

	    return(result);
	}

	///////////////////////////////////
	//
	// StackedRecord Methods
	//
	///////////////////////////////////

	final DataRecord
	getDataRecord() {

	    return(datarecord);
	}


	final Color
	getColor() {

	    return(datarecord.getColor());
	}
    }
}

