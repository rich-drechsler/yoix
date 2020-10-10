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
import java.awt.geom.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Vector;
import javax.swing.JLayeredPane;
import att.research.yoix.*;

public
class SwingJEventPlot extends SwingDataPlot

    implements MouseWheelListener

{

    //
    // The counter generator is a recent addition (10/28/04) that will
    // be used by a new project. Out first approach tried to use the
    // overlap generator, but it wasn't quite right and we didn't want
    // to make changes to it that could affect existing applications,
    // (even though it clearly could use some work) so we decided to
    // add a new generator. We eventually will take another look at
    // both generators - there's definitely room for improvement!!
    //

    private DataManager  datamanager = null;
    private SweepFilter  sweepfilters[] = null;
    private DataRecord   datarecords[] = null;
    private DataRecord   loadeddata[] = null;
    private DataPlot     partitionedplots[] = null;
    private SwingJAxis   xaxis = null;
    private SwingJAxis   yaxis = null;

    private AffineTransform  plotmatrix = null;
    private AffineTransform  plotinverse = null;
    private StackedRecord    stackedrecords[] = null;
    private StackedRegion    currentregion = null;
    private StackedRegion    stackedregions[] = null;
    private StackedSlice     stackedslices[] = null;
    private EventRecord      eventrecords[] = null;
    private BoundingBox      eventbbox = null;
    private BoundingBox      loadedbbox = null;
    private BoundingBox      plotbbox = null;
    private BoundingBox      stackedbbox = null;
    private Palette          currentpalette = null;
    private double           eventbuckets[] = null;
    private int              eventbucketindices[] = null;
    private int              eventindices[] = null;
    private int              stackedindices[] = null;
    private int              motionlisteners = 0;
    private int              plotheight;
    private int              plotwidth;
    private int              plotpadx;
    private int              plotpady;
    private int              plotminx;
    private int              plotmaxx;
    private int              plotzeroy;

    private boolean  alive = true;
    private boolean  autoready = false;
    private boolean  drawtallest = true;	// originally used in draw()
    private boolean  datasorted = false;
    private boolean  eventsbucketed = false;
    private boolean  eventsstacked = false;
    private boolean  hidepoints = false;
    private boolean  ignorezero = false;
    private boolean  keeptall = false;
    private boolean  reversepalette = false;
    private boolean  symmetric = false;
    private double   eventbucketwidth = 0;
    private double   slidertop = Double.POSITIVE_INFINITY;
    private double   sliderbottom = Double.NEGATIVE_INFINITY;
    private int      bucketseparation;
    private int      laststackfind = 0;
    private int      connect = CONNECT_NONE;
    private int      connectwidth = 1;
    private int      linewidth = 1;
    private int      linethickness = 1;		// plotstyle dependent
    private int      pointsize = 2;
    private int      axiswidth = 0;
    private int      colorchanges = 0;
    private int      plotstyle = 0;
    private int      plotstyleflags = STYLE_ENABLE_MASK;
    private int      model = 0;
    private int      sweepflags = 0;
    private int      tipflags = 0;

    //
    // Recently (10/15/07) added code that spreads the data out uniformily
    // along the xaxis, but it currently only works for simple plots. The
    // spreadmap array is used to map pixels along the horizontal axis to
    // real values that are (or would be) associated with records if they
    // are (or were) plotted at that pixel.
    //

    private boolean  spread = false;
    private double   spreadmap[];

    //
    // Overlap support - synchronization is assumed while buffers are in
    // use.
    //

    private EventRecord  generatedhits[];
    private int          gentype = -1;
    private int          genindices[] = null;

    //
    // Time shading support.
    //

    private YoixObject  unixtime = null;
    private TimeZone    timezone = null;
    private boolean     peakpixels[];
    private double      peakstart = 8;
    private double      peakstop = 17;
    private double      holidays[] = null;
    private double      timeline[] = null;
    private int         peakdays[] = {1, 0, 0, 0, 0, 0, 2};
    private int         timeshading = 0;

    //
    // These are used to customize the text that's displayed by our tips.
    // We eventually could support a callback function that generates the
    // entire tip text, but prefixes and suffixes represents a compromise
    // that's reasonably flexible and efficient (because we cache strings
    // associated with each stackedrecord). A function that generates the
    // entire text would have to be called often - perhaps every time we
    // need to display the tip.
    //

    private YoixTipManager  tipmanager = null;
    private Object          rankprefix = null;
    private Object          ranksuffix = null;
    private Object          tipprefix = null;
    private Object          tipsuffix = null;

    //
    // This is used by the algorithm that tries to do a fast collection
    // of the data records affected when the xaxis slider moves. Setting
    // fastcollector to false disables the fast algorithm.
    //

    private boolean  fastcollector = true;
    private double   sliderdata[] = null;

    //
    // Low level sweep support - currently for selecting, but we may
    // eventually support another way to zoom.
    //

    private int  xcorner[] = {0, 0, 0, 0};
    private int  ycorner[] = {0, 0, 0, 0};

    //
    // The number of pixels needed per day before timeshading details show
    // up. A value less than or equal to 0 disables the threshold.
    //

    private static final int  PIXELSPERDAY = 4;

    //
    // Remembering the JLayeredPane can sometimes be useful because it can
    // be used to get a Graphics object with a clip that protects components
    // that are being displayed in the JLayeredPane.
    // 

    protected JLayeredPane  layeredpane = null;

    //
    // Several BasicStroke objects that are used when we draw lines. Two
    // of them depend on directly connectwidth and are initialized using
    // the getConnectWidth1() and getConnectWidth2() methods.
    // 
    //

    private BasicStroke  connectstroke1 = null;
    private BasicStroke  connectstroke2 = null;

    private static final BasicStroke  OUTLINESTROKE = new BasicStroke(1);

    //
    // For now we explicitly check modifiers against this mask to decide
    // if a mousePressed() operation should adjust the relative position
    // of the tip and the cursor. Eventually may want a general approach
    // something like what's done for GraphPlots.
    // 

    private static final int  TIPPINGMASK = YOIX_CTRL_MASK|YOIX_SHIFT_MASK;

    //
    // Constants that identify actions that need to be carried out in the
    // event thread (via invokeLater() and handleRun()). There's only one
    // right now, so it's overkill, but there eventually could be others.
    //

    private static final int  RUN_DRAWREGION = 1;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    SwingJEventPlot(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
	addMouseWheelListener(this);
	tipmanager = new YoixTipManager(this);
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


    public synchronized void
    mouseExited(MouseEvent e) {

	if (mouse != TIPPING)
	    updateCurrentRegion(null);
    }


    public synchronized void
    mousePressed(MouseEvent e) {

	int  modifiers;
	int  button;

	if (mouse == AVAILABLE && alive) {
	    modifiers = YoixMiscJFC.cookModifiers(e);
	    button = modifiers & YOIX_BUTTON_MASK;
	    if (hasFocus() == false)
		requestFocus();
	    switch (button) {
		case YOIX_BUTTON1_MASK:
		    if ((modifiers & TIPPINGMASK) != TIPPINGMASK) {
			if ((sweepflags & SWEEP_ENABLED) != 0) {
			    mouse = SWEEPING;
			    sweepBegin(getEventLocation(e));
			} else mouse = UNAVAILABLE;
		    } else {
			mouse = TIPPING;
			tipmanager.startShifting();
		    }
		    break;

		case YOIX_BUTTON2_MASK:
		case YOIX_BUTTON3_MASK:
		    mouse = UNAVAILABLE;
		    break;
	    }
	    mousebutton = (mouse != AVAILABLE) ? button : 0;
	    if (mouse != AVAILABLE && mouse != TIPPING)
		updateCurrentRegion(null);
	    updateMouseMotionListener();
	}
    }


    public synchronized void
    mouseReleased(MouseEvent e) {

	int  buttons;

	//
	// The modifiers that we get from Java 1.3.1 and newer versions are
	// different, so we can't just compare mousebutton and buttons. Can
	// change when we no longer support Java 1.3.1.
	//

	if (mouse != AVAILABLE) {
	    buttons = YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK;
	    if ((buttons & mousebutton) != 0) {		// test is for Java 1.3.1
		switch (mouse) {
		    case SWEEPING:
			sweepEnd(e);
			break;

		    case TIPPING:
			break;
		}
		mouse = AVAILABLE;
		mousebutton = 0;
	    }
	    updateCurrentRegion(getEventLocation(e));
	    updateMouseMotionListener();
	}
    }

    ///////////////////////////////////
    //
    // MouseMotionListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseDragged(MouseEvent e) {

	if (mouse == SWEEPING)
	    sweepDragged(getEventLocation(e));
    }


    public synchronized void
    mouseMoved(MouseEvent e) {

	if (mouse == AVAILABLE)
	    updateCurrentRegion(getEventLocation(e));
	else if (ISMAC && (e.getModifiers()&YOIX_CTRL_MASK) != 0)
	    mouseDragged(e);
    }

    ///////////////////////////////////
    //
    // MouseWheelListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseWheelMoved(MouseWheelEvent e) {

	updateCurrentRegion(null);
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
		    datamanager.loadPlot(this);
		layeredpane = YoixMiscJFC.getJLayeredPane(this);
	    } else {
		if (autoready == false) {
		    loadeddata = null;
		    datarecords = null;
		}
		layeredpane = null;
		painted = false;
	    }
	}
    }

    ///////////////////////////////////
    //
    // DataPlot Methods
    //
    ///////////////////////////////////

    public final synchronized void
    appendRecords(DataRecord loaded[], int offset) {

	boolean  allselected;
	int      masks[];
	int      n;

	//
	// This is close, bit it's not quite right yet because we seem
	// to have trouble getting accurate data from various counters.
	// Tried a few things that didn't work and then decided to work
	// on it later - the behavior isn't terrible. Should be able to
	// duplicate problems by deselecting records during a series of
	// appends - keep an eye on the seecalls counter label and you
	// should see the problem.
	//

	if (isReady()) {
	    if (datamanager != null && loaded != null) {
		if (offset >= 0 && offset < loaded.length) {
		    clearPlot(true);
		    loadeddata = loaded;
		    datarecords = loaded;
		    //
		    // This just check to see if some records have been
		    // deselected from whatever's currently loaded. We
		    // may could allselected in afterAppend() to decide
		    // how the plot is supposed to behave.
		    //
		    allselected = true;
		    if ((masks = DataRecord.getSelectMasks(xmask, ymask)) != null) {
			for (n = 0; n < offset; n++) {
			    if (datarecords[n].notSelected(masks)) {
				allselected = false;
				break;
			    }
			}
		    }
		    buildEventRecords();
		    buildStackedRecords();
		    buildPlot();
		    afterAppend(offset, allselected);
		    reset();
		}
	    }
	}
    }


    public final synchronized ArrayList
    collectRecordsAt(Point point, boolean selected) {

	StackedRegion  region;
	StackedRegion  tipregion;
	StackedRecord  stackedrecord;
	DataRecord     record;
	ArrayList      list = null;
	ArrayList      records;
	int            length;
	int            n;

	if (eventsstacked) {
	    point = getEventLocation(point);
	    if ((region = findRegionAt(point)) != null) {
		if ((tipregion = pickTipRegion(point, region)) != null) {
		    if ((stackedrecord = tipregion.getOwner()) != null) {
			if ((records = stackedrecord.getDataRecords(tipregion)) != null) {
			    length = records.size();
			    list = new ArrayList(length);
			    for (n = 0; n < length; n++) {
				record = (DataRecord)records.get(n);
				if (record.isSelected() == selected)
				    list.add(new Integer(record.getIndex()));
			    }
			}
		    }
		}
	    }
	}
	return(list);
    }


    public final synchronized int[]
    getSelectMasks() {

	return(datamanager != null ? DataRecord.getSelectMasks(xmask, ymask) : null);
    }


    public final synchronized String
    getTipTextAt(Point point, int flags, boolean html) {

	StackedRegion  region;
	StackedRegion  tipregion;
	String         text;

	if (eventsstacked) {
	    point = getEventLocation(point);
	    if ((region = findRegionAt(point)) != null) {
		tipregion = pickTipRegion(point, region);
		text = tipregion.getTipText(flags, html);
	    } else text = null;
	} else text = null;
	return(text);
    }


    public final boolean
    isManagedBy(DataManager manager) {

	return(this.datamanager == manager);
    }


    public final synchronized void
    loadRecords(DataRecord loaded[], DataRecord records[], boolean force) {

	boolean  reload;

	if (isReady() || force) {
	    reload = (loaded == loadeddata);
	    clearPlot(loaded != null);
	    if (datamanager != null && records != null && records.length > 0) {
		loadeddata = loaded;
		datarecords = records;
		buildEventRecords();
		buildStackedRecords();
		buildPlot();
	    }
	    afterLoad(reload);
	    reset();
	}
    }


    public final void
    paintRect(Graphics g) {

	Rectangle  rect;

	g.translate(insets.left, insets.top);
	if ((rect = g.getClipBounds()) != null)
	    rect = rect.intersection(viewport);
	else rect = viewport;
	eraseRect(rect.x, rect.y, rect.width, rect.height, g);
	paintRect(rect.x, rect.y, rect.width, rect.height, g);
	g.translate(-insets.left, -insets.top);
    }


    public final synchronized void
    recolorData() {

	Color  defaultcolor;
	int    n;

	if (eventrecords != null) {
	    stackedslices = null;		// to force a rebuild
	    stackedregions = null;
	    colorEvents(++colorchanges);
	    reset();
	    resetStackRegions();
	}
    }


    public final synchronized void
    setDataManager(DataManager manager) {

	disableAxes();

	if (datamanager != manager) {
	    datamanager = manager;
	    xmask = 0;
	    ymask = 0;
	    if (xaxis != null) {
		xaxis.setEventPlot(this);
		if (datamanager != null && xindex >= 0)
		    xmask = datamanager.getPlotMask(xindex, partitionindex);
	    }
	    if (yaxis != null) {
		yaxis.setEventPlot(this);
		if (datamanager != null && yindex >= 0)
		    ymask = datamanager.getPlotMask(yindex, -1);
	    }
	    if (isLoaded())
		loadRecords(loadeddata, datarecords, false);
	}
    }


    public final synchronized void
    setGenerator(Object generator[]) {

	int  index;
	int  n;

	gentype = -1;
	genindices = null;

	if (generator != null && generator.length > 1) {
	    if (generator[0] instanceof Integer) {
		gentype = ((Integer)generator[0]).intValue();
		switch (gentype) {
		    case COUNTER_GENERATOR:
		    case OVERLAP_GENERATOR:
			genindices = new int[generator.length - 1];
			for (n = 1; n < generator.length && genindices != null; n++) {
			    if (generator[n] instanceof Integer)
				index = ((Integer)generator[n]).intValue();
			    else index = -1;
			    if (index < 0) {
				genindices = null;
				break;
			    } else genindices[n-1] = index;
			}
			break;

		   default:
			break;
		}
	    }
	}

	if (genindices == null)
	    gentype = -1;
    }


    public final synchronized void
    setSweepFilter(SweepFilter filter) {

	SweepFilter  tmp[];
	int          n;

	//
	// This preserves the old behavior, which only supported a single
	// SweepFilter.
	//

	tmp = new SweepFilter[MODIFER_OP_COUNT];
	for (n = 0; n < MODIFER_OP_COUNT; n++)
	    tmp[n] = filter;
	sweepfilters = tmp;
    }


    public final synchronized void
    setSweepFilters(SweepFilter filters[]) {

	SweepFilter  tmp[];

	tmp = new SweepFilter[MODIFER_OP_COUNT];
	if (filters != null)
	    System.arraycopy(filters, 0, tmp, 0, Math.min(filters.length, MODIFER_OP_COUNT));
	sweepfilters = tmp;
    }


    public final synchronized void
    setUnixTime(YoixObject obj) {

	if (obj != null && obj.isNull())
	    obj = null;

	if (obj != unixtime) {
	    if (obj == null || obj.callable(1) || obj.isInteger()) {
		unixtime = obj;
		if (loadPeakPixels(true))
		    reset();
	    } else VM.abort(TYPECHECK, NL_UNIXTIME);
	}
    }


    public final synchronized void
    sortRecords(DataRecord records[], DataPlot sorter, DataManager manager) {

	int  n;

	//
	// Probably should be more careful and run additional checks.
	//

	if (this.datamanager == manager) {
	    datasorted = false;
	    eventindices = null;
	    if (sorter == this) {
		if (records != null && eventrecords != null) {
		    if (records.length == eventrecords.length) {
			for (n = 0; n < eventrecords.length; n++) {
			    records[n] = eventrecords[n].datarecord;
			    records[n].setIndex(n);
			}
			datasorted = true;
		    }
		}
	    }
	}
    }


    public final synchronized void
    updatePlot(DataRecord loaded[], HitBuffer hits, int count, DataPlot sorter) {

	Graphics  g;

	//
	// Calling setTipText(null) to take the tip down isn't necessary
	// because it's done again by updateCurrentRegion(null), however
	// it doesn't hurt and matches what's done in SwingJGraphPlot.
	//

	if (count > 0 && isLoaded() && isShowingOnScreen()) {
	    if (loaded == loadeddata && eventrecords != null) {
		if (mouse == SWEEPING)
		    drawSweepBox();
		setTipText(null);	// technically unnecessary
		resetStackRegions(hits, count, isSorted(sorter));
		handleAutoFit();
		if (notGenerated()) {
		    if (count < 100 || count < .95*eventrecords.length) {
			if ((g = getSavedGraphics()) != null) {
			    g.translate(insets.left - viewport.x, insets.top - viewport.y);
			    if (isSorted(sorter) == false) {
				if (isConnected())
				    repaintConnectedPlot(hits.getSortedIndices(count, getEventIndices()), count, g);
				else repaintStandardPlot(hits.getSortedIndices(count, getEventIndices()), count, g);
			    } else {
				if (isConnected())
				    repaintConnectedPlot(hits.getRecordIndices(), count, g);
				else repaintStandardPlot(hits.getRecordIndices(), count, g);
			    }
			    g.translate(-insets.left + viewport.x, -insets.top + viewport.y);
			    disposeSavedGraphics(g);
			} else repaint();
		    } else repaint();
		} else repaintGeneratedPlot(hits, count, sorter);
		if (mouse == SWEEPING)
		    drawSweepBox();
		updateCurrentRegion(null);
		updateCounters(hits, count);
		afterUpdate();
	    }
	}
    }

    ///////////////////////////////////
    //
    // SwingJEventPlot Methods
    //
    ///////////////////////////////////

    public synchronized void
    addActionListener(ActionListener listener) {

	actionlistener = AWTEventMulticaster.add(actionlistener, listener);
    }


    final synchronized void
    axisUpdate(SwingJAxis axis, double low, double high) {

	if (axis != null && plotbbox != null) {
	    if (axis == yaxis || axis == xaxis) {
		if (axis == xaxis) {
		    if (plotbbox.ulx != low || plotbbox.lrx != high) {
			plotbbox.ulx = low;
			plotbbox.lrx = high;
			reset(true);
		    }
		} else {
		    if (plotbbox.uly != low || plotbbox.lry != high) {
			if (anchor != YOIX_CENTER || isGenerated()) {
			    if (model == 1) {
				//
				// Didn't think much about this test, except
				// that we wanted to maintain the existing
				// behavior. Eventually will take another
				// look.
				//
				if (anchor != YOIX_NONE)
				    low = Math.min(0, low);
			    }
			} else {
			    high = Math.max(Math.abs(high), Math.abs(low));
			    low = -high;
			}
			plotbbox.uly = low;
			plotbbox.lry = high;
			reset(true);
		    }
		}
	    }
	}
    }


    protected final synchronized YoixObject
    eventCoordinates(AWTEvent e) {

	YoixObject  obj = null;
	double      coords[];
	Point       p;

	if (e instanceof MouseEvent) {
	    p = getEventLocation((MouseEvent)e);
	    if ((coords = getPlotCoordinates(p.x, p.y)) != null) {
		if (spreadmap != null)
		    coords[0] = spreadmap[Math.max(0, Math.min(p.x, spreadmap.length - 1))];
		obj = YoixMake.yoixType(T_POINT);
		obj.put(N_X, YoixObject.newDouble(coords[0]), false);
		obj.put(N_Y, YoixObject.newDouble(coords[1]), false);
	    }
	}

	return(obj);
    }


    protected void
    finalize() {

	datamanager = null;
	datarecords = null;
	loadeddata = null;
	eventrecords = null;
	eventindices = null;
	eventbucketindices = null;
	eventbbox = null;
	stackedrecords = null;
	stackedindices = null;
	stackedslices = null;
	stackedregions = null;
	currentregion = null;
	stackedbbox = null;
	loadedbbox = null;
	plotbbox = null;
	sweepfilters = null;
	plotmatrix = null;
	plotinverse = null;
	generatedhits = null;
	partitionedplots = null;
	spreadmap = null;
	disableAxes();
	xaxis = null;
	yaxis = null;
	currentpalette = null;
	super.finalize();
    }


    final boolean
    getAlive() {

	return(alive);
    }


    final synchronized double[]
    getAxisEnds(SwingJAxis axis) {

	double  ends[] = null;

	if (axis != null) {
	    if (axis == xaxis || axis == yaxis) {
		if (plotbbox != null) {
		    if (axis == xaxis)
			ends = new double[] {plotbbox.ulx, plotbbox.lrx};
		    else ends = new double[] {plotbbox.uly, plotbbox.lry};
		}
	    }
	}

	return(ends);
    }


    final synchronized int
    getAxisIndex(SwingJAxis axis) {

	int  index = -1;

	if (axis != null) {
	    if (axis == xaxis)
		index = xindex;
	    else if (axis == yaxis)
		index = yindex;
	    else index = -1;
	}

	return(index);
    }


    final int
    getConnect() {

	return(connect);
    }


    final int
    getConnectWidth() {

	return(connectwidth);
    }


    final synchronized YoixObject
    getDataEnds() {

	BoundingBox  bbox;
	YoixObject   obj;

	if ((bbox = getDataBBox()) != null) {
	    if (bbox.count > 0)
		obj = getEnds(bbox);
	    else obj = YoixObject.newDictionary();
	} else obj = YoixObject.newDictionary();

	return(obj);
    }


    final DataManager
    getDataManager() {

	return(datamanager);
    }


    final int
    getLineWidth() {

	return(linewidth);
    }


    final synchronized YoixObject
    getLoadedEnds() {

	YoixObject obj;
	YoixObject ends;
	YoixObject loaded;

	if (eventrecords != null && loadedbbox != null) {
	    obj = getEnds(loadedbbox);
	    if ((loaded = getData().getObject(NL_LOADEDENDS)) != null && loaded.notNull()) {
		if ((ends = loaded.getObject(NL_XAXIS)) != null) {
		    if (ends.notNull() && ends.isArray())
			obj.put(NL_XAXIS, ends, false);
		}
		if ((ends = loaded.getObject(NL_YAXIS)) != null) {
		    if (ends.notNull() && ends.isArray())
			obj.put(NL_YAXIS, ends, false);
		}
	    }
	} else obj = getData().getObject(NL_LOADEDENDS);

	return(obj);
    }


    final synchronized YoixObject
    getPlotEnds() {

	return(getEnds(plotbbox));
    }


    final synchronized YoixObject
    getPlotStyle() {

	YoixObject  obj;
	double      buckets[];

	obj = YoixObject.newArray(2);
	obj.putInt(0, getStyleSettings(plotstyle)[0]);
	if (eventbuckets != null) {
	    if (eventbuckets.length > 1) {
		buckets = new double[eventbuckets.length];
		System.arraycopy(eventbuckets, 0, buckets, 0, buckets.length);
		buckets[1] = eventbucketwidth;
		obj.putObject(1, YoixMisc.copyIntoArray(buckets));
	    } else obj.putInt(1, (int)eventbucketwidth);
	} else obj.putNull(1);

	return(obj);
    }


    final int
    getPlotStyleFlags() {

	return(plotstyleflags);
    }


    final int
    getPointSize() {

	return(pointsize);
    }


    final double
    getRealX(int pixel) {

	double  map[];
	double  x;

	//
	// Currently only used when the plot has been spread out uniformly
	// along the xaxis, so we don't bother returning a decent answer
	// in other situations.
	// 

	if ((map = spreadmap) != null)
	    x = map[Math.max(0, Math.min(pixel + ipad.left - 1, map.length - 1))];
	else x = Double.NaN;

	return(x);
    }


    final YoixObject
    getShadeTimes() {

	YoixObject  obj;
	int         days = 0;
	int         n;

	for (n = 0; n < peakdays.length; n++) {
	    if (peakdays[n] == 0)
		days |= (1 << n);
	}

	obj = YoixObject.newDictionary(4);
	obj.putDouble(NL_PEAKSTART, peakstart);
	obj.putDouble(NL_PEAKSTOP, peakstop);
	obj.putObject(NL_PEAKDAYS, YoixMake.yoixType(T_OBJECT));
	obj.putInt(NL_PEAKDAYS, days);
	obj.put(NL_HOLIDAYS, YoixMisc.copyIntoArray(holidays));
	return(obj);
    }


    final int[]
    getSliderPixelEnds(double left, double right) {

	double  map[];
	int     ends[] = {0, 0};
	int     n;

	//
	// Currently only used when the plot has been spread out uniformly
	// along the xaxis, so we don't bother returning a decent answer
	// in other situations.
	// 

	if ((map = spreadmap) != null) {
	    for (n = 0; n < map.length; n++) {
		if (map[n] <= left)
		    ends[0] = n;
		ends[1] = n;
		if (map[n] >= right)
		    break;
	    }
	    ends[0] = Math.max(0, ends[0] - ipad.left + 1);
	    ends[1] = Math.max(ends[0], ends[1] - ipad.left + linethickness - 1);
	}
	return(ends);
    }


    final boolean
    getSpread() {

	return(spread);
    }


    final boolean
    getStacked() {

	return(eventsstacked);
    }


    final int
    getSweepFlags() {

	return(sweepflags);
    }


    final boolean
    getTipDropped() {

	return(tipmanager.isDropped());
    }


    final boolean
    getTipEnabled() {

	return(tipmanager.isEnabled());
    }


    final Point
    getTipOffset() {

	return(tipmanager.getTipOffset());
    }


    final double[]
    getZoomEnds(SwingJAxis axis, double lock, int amount) {

	BoundingBox  bbox;
	EventRecord  record;
	double       ends[];
	double       fraction;
	double       delta;
	double       span;
	int          length;
	int          leftindex;
	int          rightindex;
	int          lockindex;
	int          total;
	int          count;
	int          sign;
	int          n;

	//
	// Returns endpoints suitable for zooming along axis by amount,
	// which currently always comes from the wheelrotation field of
	// a MouseWheelEvent, while trying to keep the lock point fixed.
	//
	// NOTE - still room for improvement in the branch that handles
	// the calculation when events are "spread".
	//

	if ((ends = getAxisEnds(axis)) != null) {
	    if ((span = ends[1] - ends[0]) > 0) {
		if (axis.getOrientation() == YOIX_HORIZONTAL) {
		    if (spreadmap == null) {
			fraction = (lock - ends[0])/span;
			if (fraction > 0 && fraction <= 0.5) {
			    ends[0] -= amount*(lock - ends[0])/15.0;
			    ends[1] = (lock - ends[0]*(1 - fraction))/fraction;
			} else if (fraction > 0.5 && fraction < 1.0) {
			    ends[1] += amount*(ends[1] - lock)/15.0;
			    ends[0] = (lock - ends[1]*fraction)/(1.0 - fraction);
			} else if (fraction <= 0)
			    ends[1] += amount*span/15.0;
			else ends[0] -= amount*span/15.0;
		    } else {
			//
			// Difficult code that undoubtedly could be
			// improved, but getting decent behavior for
			// all cases isn't trivial, particularly when
			// only a few records are left. We will take
			// another look at this in the near future.
			//
			length = eventrecords.length;
			sign = (amount < 0) ? 1 : -1;
			leftindex = -1;
			rightindex = -1;
			lockindex = 0;
			for (n = 0; n < length; n++) {
			    record = eventrecords[n];
			    if (record.realx >= plotbbox.ulx) {
				if (leftindex < 0) {
				    leftindex = n;
				    rightindex = n;
				}
				if (record.realx <= lock)
				    lockindex = n;
				if (record.realx <= plotbbox.lrx)
				    rightindex = n;
				else break;
			    }
			}
			if (leftindex >= 0) {
			    span = rightindex - leftindex + 1;
			    if (lockindex > leftindex) {
				if (lockindex < rightindex) {
				    if (lock - eventrecords[lockindex].realx < eventrecords[lockindex+1].realx - lock)
					fraction = (lockindex - leftindex + 1)/span;
				    else fraction = (lockindex - leftindex + 2)/span;
				} else fraction = 1.0;
			    } else fraction = 0.0;
			    //
			    // Arrived at by experimenting, so don't take
			    // the total calculation too seriously.
			    //
			    total = Math.min((int)span, Math.abs(amount)*Math.max(1, (int)(0.05*span)));
			    count = (int)Math.round(fraction*total);
			    if (fraction > 0 && fraction < 1.0) {
				leftindex += sign*count;
				rightindex -= sign*(total - count);
			    } else if (fraction <= 0)
				rightindex -= sign*total;
			    else leftindex += sign*total;
			    //
			    // Make sure new indices are in range.
			    //
			    leftindex = Math.max(0, Math.min(leftindex, length - 1));
			    rightindex = Math.max(leftindex, Math.min(rightindex, length - 1));
			    //
			    // Don't use realx from the records when the
			    // two ends land on the same value. otherwise
			    // we'll never get back here and there will be
			    // other issues too (e.g., selecting the sweep
			    // records).
			    // 
			    if (eventrecords[leftindex].realx < eventrecords[rightindex].realx) {
				ends[0] = eventrecords[leftindex].realx;
				ends[1] = eventrecords[rightindex].realx;
			    } else {
				span = ends[1] - ends[0];
				if (lock > plotbbox.ulx && lock < plotbbox.lrx) {
				    ends[0] += 0.25*sign*span;
				    ends[1] -= 0.25*sign*span;
				} else if (lock <= plotbbox.ulx)
				    ends[1] -= 0.5*sign*span;
				else ends[0] += 0.5*sign*span;
			    }
			}
		    }
		} else {
		    delta = -amount*span/15.0;
		    ends[0] += delta;
		    ends[1] -= delta;
		    if (eventsstacked && delta > 0) {
			if ((bbox = getDataBBox()) != null)
			    ends[1] = Math.max(ends[1], bbox.lry);
		    }
		}
	    }
	}
	return(ends);
    }


    public final void
    handleRun(Object args[]) {

	//
	// Should only get here from YoixAWTInvocationEvent.run() because
	// another thread called invokeLater(), which means we're running
	// in the event thread.
	//

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case RUN_DRAWREGION:
		    handleDrawRegion((StackedRegion)args[1]);
		    break;
	    }
	}
    }


    protected final synchronized void
    paintRect(int x, int y, int width, int height, Graphics g) {

	Rectangle  rect;
	Shape      clip;
	int        thickness;
	int        leftedge;
	int        rightedge;
	int        lastselected;
	int        left;
	int        right;

	//
	// Work that has to be done to support connected lines is harder
	// than you might expect. It was added fairly quickly, so there's
	// undoubtedly room for improvement, but test carefully if you
	// make any changes!!
	//
	// NOTE - the extra work that tries to make sure the rectangle
	// that we want to paint really does intersect the viewport is a
	// precaution mostly for Java 1.5.0.
	//

	if (eventrecords != null) {
	    clip = g.getClip();
	    g.translate(-viewport.x, -viewport.y);
	    rect = new Rectangle(x, y, width, height);
	    rect = rect.intersection(viewport);
	    if (rect.isEmpty() == false) {
		g.setClip(rect);
		rect = g.getClipBounds();
		if (isConnected()) {
		    lastselected = -1;
		    thickness = pickThickness();
		    leftedge = rect.x - thickness;
		    rightedge = rect.x + rect.width + thickness;
		    for (left = 0; left < eventrecords.length; left++) {
			if (eventrecords[left].isSelected()) {
			    if (eventrecords[left].x >= leftedge) {
				if (lastselected >= 0)
				    left = lastselected;
				break;
			    } else lastselected = left;
			}
		    }
		    for (right = left + 1; right < eventrecords.length; right++) {
			if (eventrecords[right].x >= rightedge) {
			    if (eventrecords[right].isSelected())
				break;
			}
		    }
		    left = Math.max(left, 0);
		    right = Math.min(right, eventrecords.length - 1);
		    draw(left, eventrecords[right].x + thickness, g);
		} else {
		    leftedge = rect.x - linethickness;
		    for (left = 0; left < eventrecords.length; left++) {
			if (eventrecords[left].x >= leftedge) {
			    draw(left, x + width, g);
			    break;
			}
		    }
		}
	    }
	    g.translate(viewport.x, viewport.y);
	    g.setClip(clip);
	}
    }


    public synchronized void
    removeActionListener(ActionListener listener) {

	actionlistener = AWTEventMulticaster.remove(actionlistener, listener);
    }


    final void
    reset(boolean sync) {

	if (sync) {
	    if (makePlot())
		reset();
	} else reset();
    }


    synchronized void
    setAfterLoad(YoixObject obj) {

	super.setAfterLoad(obj);
    }


    synchronized void
    setAfterUpdate(YoixObject obj) {

	super.setAfterUpdate(obj);
	loadCounters();
    }


    protected final synchronized void
    setAlignment(int alignment) {

    }


    final void
    setAlive(boolean state) {

	alive = state;
    }


    protected final synchronized void
    setAnchor(int anchor) {

	if (this.anchor != anchor) {
	    switch (anchor) {
		case YOIX_NORTH:
		case YOIX_NORTHEAST:
		case YOIX_NORTHWEST:
		case YOIX_TOP:
		    this.anchor = YOIX_NORTH;
		    break;

		case YOIX_SOUTH:
		case YOIX_SOUTHEAST:
		case YOIX_SOUTHWEST:
		case YOIX_BOTTOM:
		    this.anchor = YOIX_SOUTH;
		    break;

		case YOIX_NONE:
		    //
		    // This is new - it means the data can be positive
		    // and negative and zero along the y axis depends
		    // the data that's being displayed. We're not done
		    // implementing everything (e.g., keeptall) related
		    // to YOIX_NONE, but most things behave reasonably
		    // well.
		    //
		    this.anchor = YOIX_NONE;
		    break;

		default:
		    this.anchor = YOIX_CENTER;
		    break;
	    }
	    reset(true);
	}
    }


    final synchronized void
    setAutoReady(boolean value) {

	if (autoready != value) {
	    autoready = value;
	    //
	    // Is there more to do??
	    //
	}
    }


    final synchronized void
    setAxisWidth(int width) {

	width = Math.max(0, width);
	if (axiswidth != width) {
	    axiswidth = width;
	    switch (anchor) {
		case YOIX_CENTER:
		case YOIX_NONE:		// not sure about this??
		    makePlot();
		    reset();
		    break;
	    }
	}
    }


    public final synchronized void
    setBounds(int x, int y, int width, int height) {

	if (totalsize.width != width || totalsize.height != height) {
	    totalsize.width = width;
	    totalsize.height = height;
	    super.setBounds(x, y, width, height);
	    viewport.width = totalsize.width - insets.left - insets.right;
	    viewport.height = totalsize.height - insets.top - insets.bottom;
	    makePlot();
	} else super.setBounds(x, y, width, height);
    }


    final synchronized void
    setConnect(int value) {

	value = (value != CONNECT_LINES) ? CONNECT_NONE : value;
	if (connect != value) {
	    connect = value;
	    switch (plotstyle) {
		case STYLE_POINTS:
		case STYLE_STACKED_POINTS:
		    reset(true);
		    break;
	    }
	}
    }


    final synchronized void
    setConnectWidth(int width) {

	width = Math.max(1, width);
	if (connectwidth != width) {
	    connectwidth = width;
	    connectstroke1 = null;
	    connectstroke2 = null;
	    switch (plotstyle) {
		case STYLE_POINTS:
		case STYLE_STACKED_POINTS:
		    reset(true);
		    break;
	    }
	}
    }


    final synchronized void
    setFrozen(boolean state) {

	frozen = state;
	if (frozen == false) {
	    if (thawed == false)
		reset(true);
	}
    }


    final synchronized void
    setHidePoints(boolean state) {

	if (hidepoints != state) {
	    hidepoints = state;
	    reset();
	}
    }


    final synchronized void
    setIgnoreZero(boolean value) {

	if (value != ignorezero) {
	    ignorezero = value;
	    if (isLoaded())
		loadRecords(loadeddata, datarecords, false);
	}
    }


    final synchronized void
    setKeepTall(boolean keeptall) {

	double  ends[];

	if (this.keeptall != keeptall) {
	    this.keeptall = keeptall;
	    if (yaxis != null) {
		//
		// This may not be 100% correct because setAxisEnds()
		// lets another thread handle things, which includes
		// setting the slider ends. Eventually need to take a
		// closer look.
		//
		if ((ends = yaxis.getSliderEnds()) != null) {
		    sliderbottom = ends[0];
		    slidertop = ends[1];
		    sliderUpdate(yaxis, ends[0], ends[1], false);
		} else {
		    sliderbottom = Double.NEGATIVE_INFINITY;
		    slidertop = Double.POSITIVE_INFINITY;
		}
	    } else {
		sliderbottom = Double.NEGATIVE_INFINITY;
		slidertop = Double.POSITIVE_INFINITY;
	    }
	}
    }


    final synchronized void
    setLineWidth(int width) {

	//
	// In some cases the actual linewidth may not change the plot, so
	// we let setPlotStyle() decide if makePlot() and reset() need to
	// be called.
	//

	width = Math.max(0, width);
	if (linewidth != width) {
	    linewidth = width;
	    setPlotStyle(YoixObject.newInt(plotstyle));
	}
    }


    final synchronized void
    setModel(int model) {

	BoundingBox  bbox;

	model = Math.max(0, Math.min(model, 1));
	if (this.model != model) {
	    this.model = model;
	    switch (model) {
		case 0:
		    this.model = model;
		    break;

		case 1:
		    if (plotbbox != null && plotbbox.uly > 0) {
			bbox = new BoundingBox(plotbbox);
			bbox.uly = 0;
			setPlotEnds(getEnds(bbox));
		    }
		    break;
	    }
	}
    }


    final synchronized void
    setPalette(YoixObject obj) {

	if (obj != null) {
	    currentpalette = obj.notNull() ? (Palette)getBody(obj) : null;
	    colorEvents(++colorchanges);
	    reset();
	} else currentpalette = null;
    }


    final void
    setPlotEnds(YoixObject obj) {

	setPlotEnds(obj, 0);
    }


    final void
    setPlotEnds(YoixObject obj, int slidermode) {

	if (obj != null && obj.notNull() && obj.isDictionary())
	    setEnds(obj, slidermode);
    }


    final synchronized void
    setPlotStyle(YoixObject obj) {

	double  buckets[];
	int     styles[];

	if (obj.isNull() || obj.isInteger() || obj.isArray()) {
	    if (obj.isArray()) {
		styles = getStyleSettings(obj.getInt(0, plotstyle));
		buckets = getBucketSettings(obj.getObject(1));
	    } else if (obj.isNumber()) {
		styles = getStyleSettings(obj.intValue());
		buckets = eventbuckets;
	    } else {
		styles = getStyleSettings(plotstyle);
		buckets = eventbuckets;
	    }
	    buildPlot(styles[0], styles[1], styles[2] != 0, buckets);
	} else VM.abort(TYPECHECK, NL_PLOTSTYLE);
    }


    final synchronized void
    setPlotStyleFlags(int flags) {

	//
	// We currently ignore the change if there's no style bit set in
	// flags. Probably could be changed, but means checking all uses
	// of plotstyle and making sure axes and everyone else behave if
	// the plot is disabled using these flags. Definitely not a big
	// deal - we currently only change these once when the plot is
	// created.
	//

	if ((flags&(STYLE_ENABLE_EVENTS|STYLE_ENABLE_STACKS)) != 0) {
	    if ((flags & plotstyleflags) != plotstyleflags) {
		plotstyleflags = flags;
		setPlotStyle(getPlotStyle());
	    } else plotstyleflags = flags;
	}
    }


    final synchronized void
    setPointSize(int size) {

	//
	// In some cases the actual pointsize may not change the plot, so
	// we let setPlotStyle() decide if makePlot() and reset() need to
	// be called.
	//

	size = Math.max(0, size);
	if (pointsize != size) {
	    pointsize = size;
	    setPlotStyle(YoixObject.newInt(plotstyle));
	}
    }


    final synchronized void
    setRankPrefix(YoixObject obj) {

	Object  generator;

	if (obj.notNull()) {
	    if (obj.isString())
		generator = obj.stringValue();
	    else generator = obj;
	} else generator = null;

	rankprefix = clearCachedTips(rankprefix, generator);
    }


    final synchronized void
    setRankSuffix(YoixObject obj) {

	Object  generator;

	if (obj.notNull()) {
	    if (obj.isString())
		generator = obj.stringValue();
	    else generator = obj;
	} else generator = null;

	ranksuffix = clearCachedTips(ranksuffix, generator);
    }


    final synchronized void
    setReversePalette(boolean state) {

	if (reversepalette != state) {
	    reversepalette = state;
	    if (currentpalette != null)
		currentpalette.setInverted(reversepalette);
	    colorEvents(++colorchanges);
	    reset();
	}
    }


    final synchronized void
    setShadeTimes(YoixObject obj) {

	YoixObject  entry;
	YoixObject  element;
	double      new_holidays[];
	int         new_peakdays[];
	int         length;
	int         days;
	int         day;
	int         n;
	int         i;

	if (obj != null) {
	    new_peakdays = new int[] {1, 0, 0, 0, 0, 0, 2};
	    length = new_peakdays.length;
	    if ((entry = obj.getObject(NL_PEAKDAYS)) != null) {
		if (entry.isInteger()) {
		    days = entry.intValue();
		    entry = YoixObject.newArray(7);
		    for (day = 0; day < length; day++) {
			if ((days & 0x1) != 0)
			    entry.putInt(day, day);
			days >>>= 1;
		    }
		}
		if (entry.isArray()) {
		    if (entry.notNull() && entry.sizeof() > 0) {
			new_peakdays = new int[] {-1, -1, -1, -1, -1, -1, -1};
			for (n = entry.offset(); n < entry.length(); n++) {
			    if ((element = entry.getObject(n)) != null) {
				if (element.isInteger()) {
				    i = element.intValue();
				    if (i >= 0 && i < length)
					new_peakdays[i] = 0;
				    else VM.abort(BADVALUE, NL_PEAKDAYS, n);
				} else VM.abort(TYPECHECK, NL_PEAKDAYS, n);
			    }
			}
			for (n = 0; n < length; n++) {
			    if (new_peakdays[n] < 0) {
				for (i = 1; i < length; i++) {
				    if (new_peakdays[(n+i)%length] == 0) {
					new_peakdays[n] = i;
					break;
				    }
				}
			    }
			}
		    }
		} else VM.abort(TYPECHECK, NL_PEAKDAYS);
	    }

	    new_holidays = null;
	    if ((entry = obj.getObject(NL_HOLIDAYS, null)) != null) {
		if (entry.isArray()) {
		    if (entry.notNull() && entry.sizeof() > 0) {
			new_holidays = new double[entry.sizeof()];
			for (n = entry.offset(), i = 0; n < entry.length(); n++, i++) {
			    if ((element = entry.getObject(n)) != null && element.isNumber()) {
				new_holidays[i] = element.doubleValue();
				if (i%2 == 1) {
				    if (new_holidays[i] < 1000000.0)
					new_holidays[i] += new_holidays[i-1];
				    else if (new_holidays[i] <= new_holidays[i-1])
					VM.abort(BADVALUE, NL_HOLIDAYS, n);
				}
			    } else VM.abort(TYPECHECK, NL_HOLIDAYS, n);
			}
			new_holidays = MiscTime.setHolidays(new_holidays);
		    }
		} else VM.abort(TYPECHECK, NL_HOLIDAYS);
	    }

	    peakstart = obj.getDouble(NL_PEAKSTART, 8);
	    peakstop = obj.getDouble(NL_PEAKSTOP, 17);
	    peakdays = new_peakdays;
	    holidays = new_holidays;
	    if (loadPeakPixels(true))
		reset();
	}
    }


    final void
    setSpread(boolean state) {

	//
	// Brute force right now, but we really only have to reload when
	// the plot has ignorezero set, which typically means it's a tag
	// plot. Will revist later.
	//

	if (spread != state) {
	    spread = state;
	    if (isLoaded())
		loadRecords(loadeddata, datarecords, false);
	}
    }


    final void
    setStacked(boolean state) {

	//
	// Old versions used this, but it's now just a placeholder that may
	// not even be called. Instead you should use plotstyle to pick the
	// appropriate style.
	//

    }


    final void
    setSweepFlags(int flags) {

	sweepflags = flags;
    }


    final synchronized void
    setSymmetric(boolean state) {

	if (state != symmetric) {
	    symmetric = state;
	    if (anchor == YOIX_CENTER)
		reset(true);
	}
    }


    final synchronized void
    setTimeShading(int value) {

	value = (value != 0) ? (value > 0 ? 1 : -1) : 0;

	if (value != timeshading) {
	    timeshading = value;
	    loadPeakPixels(false);
	    reset();
	}
    }


    final void
    setTipDropped(boolean state) {

	tipmanager.setDropped(state);
    }


    final void
    setTipEnabled(boolean state) {

	Point  point;

	tipmanager.setEnabled(state);
	if (state) {
	    if ((point = tipmanager.getCursorLocation()) != null)
		updateCurrentRegion(getEventLocation(point));
	}
    }


    final synchronized void
    setTipFlags(int flags) {

	if (tipflags != flags) {
	    tipflags = flags;
	    updateMouseMotionListener();
	}
    }


    final void
    setTipLockModel(int model) {

	tipmanager.setTipLockModel(model);
    }


    final void
    setTipOffset(Point point) {

	tipmanager.setTipOffset(point);
    }


    final synchronized void
    setTipPrefix(YoixObject obj) {

	Object  generator;

	if (obj.notNull()) {
	    if (obj.isString())
		generator = obj.stringValue();
	    else generator = obj;
	} else generator = null;

	tipprefix = clearCachedTips(tipprefix, generator);
    }


    final synchronized void
    setTipSuffix(YoixObject obj) {

	Object  generator;

	if (obj.notNull()) {
	    if (obj.isString())
		generator = obj.stringValue();
	    else generator = obj;
	} else generator = null;

	tipsuffix = clearCachedTips(tipsuffix, generator);
    }


    final synchronized void
    setXAxis(SwingJAxis axis) {

	if (xaxis != axis) {
	    xaxis = axis;
	    xaxis.setEventPlot(this);
	    xaxis.setPlotLineWidth(linethickness);
	    if (xmask == 0 && xindex >= 0) {
		if (xaxis != null && datamanager != null)
		    xmask = datamanager.getPlotMask(xindex, partitionindex);
	    }
	}
    }


    final synchronized void
    setYAxis(SwingJAxis axis) {

	if (yaxis != axis) {
	    yaxis = axis;
	    yaxis.setEventPlot(this);
	    if (ymask == 0 && yindex >= 0) {
		if (yaxis != null && datamanager != null)
		    ymask = datamanager.getPlotMask(yindex, -1);
	    }
	}
    }


    final synchronized void
    sliderUpdate(SwingJAxis axis, double low, double high, boolean released) {

	HitBuffer  hits;
	int        count = 0;
	int        n;

	if (axis != null && released == false) {
	    if ((axis == xaxis && xmask != 0) || (axis == yaxis && ymask != 0)) {
		if ((hits = getHitBuffer()) != null) {
		    if (axis == xaxis) {
			if (fastcollector)
			    count = collectRecords2(hits, low, high, xindex, xmask);
			else count = collectRecords(hits, low, high, xindex, xmask);
		    } else {
			if (keeptall) {
			    if (eventsstacked) {
				count = collectStackedRecords(hits, low, eventbbox.lry, yindex, ymask);
				repaintTallStacks(sliderbottom, low, slidertop, high);
			    } else {
				count = collectRecords(hits, low, eventbbox.lry, yindex, ymask);
				repaintTallRecords(sliderbottom, low, slidertop, high);
			    }
			} else if (eventsstacked) {
			    count = collectStackedRecords(hits, low, high, yindex, ymask);
			    repaintTallStacks(sliderbottom, low, slidertop, high);
			} else count = collectRecords(hits, low, high, yindex, ymask);
		    }
		    if (datamanager != null) {
			if (count > 0) {
			    datamanager.updateData(loadeddata, hits, count, this);
			    Thread.yield();
			} else releaseHitBuffer(hits);
			if (partitionedplots != null)
			    updatePartitionedPlots(axis, low, high);
		    } else updatePlot(loadeddata, hits, count, this);
		}
	    }
	}
    }


    public final void
    update(Graphics g) {

	paint(g);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildEventBuckets(EventRecord records[], BoundingBox bbox) {

	double  width;
	double  zero;
	double  low;
	double  high;
	double  step;
	int     span;
	int     separation;
	int     count;
	int     n;

	if (eventbuckets != null) {
	    if (records != null && bbox != null) {
		if (eventbuckets.length == 1) {
		    zero = bbox.ulx;
		    width = (bbox.lrx - bbox.ulx)/eventbuckets[0];
		} else {
		    zero = eventbuckets[0];
		    width = eventbuckets[1];
		}
		if (Double.isNaN(width)) {
		    separation = 4*linethickness;
		    if ((span = YoixAWTToolkit.getScreenWidth()/2) > 0)
			width = (bbox.lrx - bbox.ulx)*separation/span;
		    else width = 0;
		}
		if (eventbuckets.length >= 4) {
		    if (eventbuckets.length == 4) {
			low = eventbuckets[2];
			step = eventbuckets[2];
			high = eventbuckets[3];
		    } else {
			low = eventbuckets[2];
			step = eventbuckets[3];
			high = eventbuckets[4];
		    }
		    width = Math.min(low + step*Math.floor((Math.max(width, low) - low)/step), high);
		}
		if (width > 0) {
		    bbox.reset();
		    for (n = 0; n < records.length; n++) {
			records[n].realx = zero + Math.floor((records[n].realx - zero)/width)*width;
			bbox.add(records[n].realx, records[n].realy);
		    }
		    eventbucketwidth = width;
		    eventsbucketed = true;
		}
	    }
	}
    }


    private void
    buildEventBucketIndices() {

	HashMap  map;
	Integer  value;
	Double   key;
	int      indices[];
	int      length;
	int      n;

	if (eventsbucketed && eventrecords != null) {
	    if (eventbucketindices == null) {
		length = eventrecords.length;
		indices = new int[length];
		map = new HashMap();
		for (n = 0; n < length; n++) {
		    key = new Double(eventrecords[n].realx);
		    if ((value = (Integer)map.get(key)) == null) {
			value = new Integer(n);
			map.put(key, value);
		    }
		    indices[n] = value.intValue();
		}
		eventbucketindices = indices;
	    }
	}
    }


    private void
    buildEventIndices() {

	int  length;
	int  n;

	if (eventindices == null) {
	    if (loadeddata != null && eventrecords != null) {
		eventindices = new int[loadeddata.length];
		if ((length = eventrecords.length) != eventindices.length) {
		    for (n = 0; n < eventindices.length; n++)
			eventindices[n] = length;
		}
		for (n = 0; n < length; n++)
		    eventindices[eventrecords[n].datarecord.getIndex()] = n;
	    }
	}
    }


    private synchronized void
    buildEventRecords() {

	EventRecord  records[];
	EventRecord  temp[];
	EventRecord  record;
	BoundingBox  bbox;
	int          count;
	int          length;
	int          n;

	if (eventrecords == null && datarecords != null) {
	    datasorted = false;
	    eventindices = null;
	    eventbucketindices = null;
	    eventbucketwidth = 0;
	    eventsbucketed = false;
	    sliderdata = null;
	    length = datarecords.length;
	    records = new EventRecord[length];
	    bbox = new BoundingBox();
	    if ((ignorezero && !spread) || partitioned) {
		count = 0;
		if (partitioned) {
		    for (n = 0; n < length; n++) {
			if (datarecords[n].getPartition(xindex) == partitionindex)
			    records[count++] = new EventRecord(datarecords[n], bbox);
		    }
		} else {
		    for (n = 0; n < length; n++) {
			record = new EventRecord(datarecords[n], bbox);
			if (record.realy != 0)
			    records[count++] = record;
		    }
		}
		if (count < records.length) {
		    temp = new EventRecord[count];
		    if (count > 0)
			System.arraycopy(records, 0, temp, 0, count);
		    records = temp;
		}
	    } else {
		for (n = length - 1; n >= 0; n--)
		    records[n] = new EventRecord(datarecords[n], bbox);
	    }
	    buildEventBuckets(records, bbox);
	    YoixMiscQsort.sort(records, VL_SORT_TIME);
	    eventrecords = records;
	    eventbbox = bbox;
	    datamanager.sortRecords(this);
	}
    }


    private void
    buildPlot() {

	if (loadCounters() > 0) {
	    setPlotBBox(eventsstacked ? stackedbbox : eventbbox);
	    loadedbbox = new BoundingBox(plotbbox);
	    sliderdata = new double[8];
	    setPartitionedPlots();
	    setLoadedEnds();
	    setAxisEnds(xaxis);
	    setAxisEnds(yaxis);
	    generateValues();
	    makePlot();
	    colorEvents(colorchanges);
	    loadCounters();
	}
    }


    private void
    buildPlot(int style, int thickness, boolean stacked, double buckets[]) {

	BoundingBox  bbox;

	//
	// The yaxis needs special attention when stacked or buckets changes
	// the current settings, because either way the plot usually ends up
	// with a bounding box that has a different yaxis extent.
	//

	if (style != plotstyle || thickness != linethickness || Arrays.equals(buckets, eventbuckets) == false) {
	    plotstyle = style;
	    setLineThickness(thickness);
	    if (Arrays.equals(buckets, eventbuckets) == false) {
		//
		// Changing buckets currently resets both axes, which isn't
		// as friendly as we wanted. Made several tries at improving
		// the xaxis behavior, but none were completely successful.
		// Suspect it can be done, but we probably need to find the
		// records that are currently selected by the xaxis before
		// changing buckets and then reset the xaxis based on those
		// records after the plot is rebuilt. Same idea probably can
		// be applied to improve the yaxis behavior here and in the
		// case when the stacking mode changes - later.
		//
		if (xaxis != null)
		    xaxis.setAxisEnds(false);
		if (yaxis != null)
		    yaxis.setAxisEnds(false);
		eventbuckets = buckets;
		datasorted = false;
		eventrecords = null;
		eventindices = null;
		eventbucketindices = null;
		spreadmap = null;
		buildEventRecords();
		eventsstacked = stacked;
		stackedrecords = null;
		buildStackedRecords();
		buildPlot();
	    } else if (stacked != eventsstacked) {
		//
		// Change in stacking means the yaxis bounds might change, but
		// it shouldn't affect the xaxis, so we save the current bounds
		// of the plot and use it to restore the xaxis bounds a little
		// later.
		//
		bbox = plotbbox;
		if (yaxis != null)
		    yaxis.setAxisEnds(false);
		eventsstacked = stacked;
		stackedrecords = null;
		spreadmap = null;
		buildStackedRecords();
		if (loadCounters() > 0) {
		    setPlotBBox(eventsstacked ? stackedbbox : eventbbox);
		    loadedbbox = new BoundingBox(plotbbox);
		    //
		    // Older version reset sliderdata[] here, but that seemed
		    // wrong and we think occasionally led to inconsistencies
		    // that cleared themselves up quickly but definitely were
		    // annoying.
		    //

		    setPartitionedPlots();

		    //
		    // Skipping the
		    //
		    //     setLoadedEnds();
		    //
		    // call that buildPlot() is an intentional omission.
		    //

		    if (bbox != null) {
			plotbbox.ulx = bbox.ulx;
			plotbbox.lrx = bbox.lrx;
		    }
		    setAxisEnds(xaxis);
		    setAxisEnds(yaxis);
		    generateValues();
		    makePlot();
		    colorEvents(colorchanges);
		    loadCounters();
		}
	    } else makePlot();
	    reset();
	}
    }


    private void
    buildStackedRecords() {

	StackedRecord  records[];
	StackedRecord  temp[];
	StackedRecord  stackedrecord;
	BoundingBox    bbox;
	int            indices[];
	int            count;
	int            length;
	int            m;
	int            n;

	if (stackedrecords == null && eventrecords != null) {
	    if (eventsstacked) {
		length = eventrecords.length;
		indices = new int[length];
		records = new StackedRecord[length];
		bbox = new BoundingBox();
		for (n = 0, count = 0; n < length; count++) {
		    stackedrecord = new StackedRecord(eventrecords, count, n, bbox);
		    records[count] =  stackedrecord;
		    for (; n < stackedrecord.right; n++)
			indices[n] = count;
		}
		if (count < records.length) {
		    temp = new StackedRecord[count];
		    if (count > 0)
			System.arraycopy(records, 0, temp, 0, count);
		    records = temp;
		}
		stackedrecords = records;
		stackedindices = indices;
		stackedbbox = bbox;
	    }
	    if (yaxis != null)
		yaxis.setSliderModel(eventsstacked ? 1 : 0);
	}
    }


    private String
    callTipHelper(YoixObject funct, double x, double y) {

	YoixObject  argv[];
	YoixObject  obj;
	String      text = "";

	//
	// Intentionally returns the empty string (not null) when there's
	// no good answer so we aren't repeatedly called.
	//

	if (funct != null) {
	    if (funct.isCallable()) {
		if (funct.callable(2)) {
		    argv = new YoixObject[] {
			YoixObject.newDouble(x),
			YoixObject.newDouble(y)
		    };
		} else if (funct.callable(1))
		    argv = new YoixObject[] {YoixObject.newDouble(x)};
		else if (funct.callable(0))
		    argv = new YoixObject[0];
		else argv = null;
		if (argv != null) {
		    if ((obj = parent.call(funct, argv)) != null) {
			if (obj.isString())
			    text = obj.stringValue();
		    }
		}
	    }
	}

	return(text);
    }


    private synchronized Object
    clearCachedTips(Object oldgenerator, Object newgenerator) {

	int  n;

	if (stackedrecords != null) {
	    if (newgenerator != oldgenerator) {
		if (newgenerator == null || newgenerator.equals(oldgenerator) == false) {
		    for (n = 0; n < stackedrecords.length; n++) {
			stackedrecords[n].rankprefix = null;
			stackedrecords[n].ranksuffix = null;
			stackedrecords[n].tipprefix = null;
			stackedrecords[n].tipsuffix = null;
		    }
		}
	    }
	}
	return(newgenerator);
    }


    private synchronized void
    clearPlot(boolean saveslider) {

	selectedcount = 0;
	totalcount = 0;
	datarecords = null;
	loadeddata = null;
	eventrecords = null;
	eventindices = null;
	eventbucketindices = null;
	eventbbox = null;
	stackedrecords = null;
	stackedindices = null;
	stackedslices = null;
	stackedregions = null;
	currentregion = null;
	stackedbbox = null;
	loadedbbox = null;
	plotbbox = null;
	plotmatrix = null;
	plotinverse = null;
	generatedhits = null;
	partitionedplots = null;
	spreadmap = null;
	sliderdata = null;
	datasorted = false;
	if (xaxis != null)
	    xaxis.setAxisEnds(saveslider);
	if (yaxis != null)
	    yaxis.setAxisEnds(saveslider);
    }


    private int
    collectGeneratedRecords(HitBuffer hits, int hitcount) {

	EventRecord  record;
	BitSet       collected;
	double       incr;
	double       limit;
	int          genindex;
	int          length;
	int          index;
	int          count;
	int          m;
	int          n;

	//
	// At one time there was a version named collectGeneratedRecords2()
	// that worked a bit faster but made assumptions about the sorting
	// of eventrecords[] relative to datarecords[]. Those assumptions
	// are no longer valid, so it's been deleted (as of 6/26/06). Small
	// chance it could be modified, but we decided it wasn't worth the
	// effort right now.
	//

	count = 0;

	if (eventrecords != null && plotbbox != null && getEventIndices() != null) {
	    length = eventrecords.length;
	    genindex = genindices[0];
	    collected = new BitSet(eventindices[hits.getRecordIndex(hitcount-1)] + 1);
	    switch (gentype) {
		case COUNTER_GENERATOR:
		    for (n = 0; n < hitcount; n++) {
			if ((index = eventindices[hits.getRecordIndex(n)]) < length) {
			    record = eventrecords[index];
			    if (record.selected != record.datarecord.isSelected()) {
				if (record.datarecord.getValue(genindex) != 0) {
				    if (record.height != 0) {		// a late addition
					if (collected.get(index) == false) {
					    generatedhits[count++] = record;
					    collected.set(index);
					}
				    }
				    incr = record.selected ? -1 : 1;
				    limit = record.realx;
				    record.selected = !record.selected;
				    m = eventsbucketed ? eventbucketindices[index] : index + 1;
				    for (; m < length; m++) {
					if (eventrecords[m].realx == limit) {
					    if (eventrecords[m] != record) {
						if (eventrecords[m].datax == record.datax) {
						    if (eventrecords[m].datarecord.getValue(genindex) != 0) {
							if (updateCounterData(eventrecords[m], incr)) {
							    if (collected.get(m) == false) {
								generatedhits[count++] = eventrecords[m];
								collected.set(m);
							    }
							}
						    }
						}
					    }
					} else break;
				    }
				}
			    }
			}
		    }
		    break;

		case OVERLAP_GENERATOR:
		    for (n = 0; n < hitcount; n++) {
			if ((index = eventindices[hits.getRecordIndex(n)]) < length) {
			    record = eventrecords[index];
			    if (record.selected != record.datarecord.isSelected()) {
				if (record.height != 0) {		// a late addition
				    if (collected.get(index) == false) {
					generatedhits[count++] = record;
					collected.set(index);
				    }
				}
				incr = record.selected ? -1 : 1;
				limit = record.datarecord.getValue(genindex);
				record.selected = !record.selected;
				m = eventsbucketed ? eventbucketindices[index] : index + 1;
				for (; m < length; m++) {
				    if (eventrecords[m].realx < limit) {
					if (eventrecords[m] != record) {
					    if (eventrecords[m].datax < limit && eventrecords[m].datax >= record.datax) {
						if (updateOverlapData(eventrecords[m], incr)) {
						    if (collected.get(m) == false) {
							generatedhits[count++] = eventrecords[m];
							collected.set(m);
						    }
						}
					    }
					}
				    } else break;
				}
			    }
			}
		    }
		    break;
	    }
	}

	return(count);
    }


    private int
    collectRecords(HitBuffer hits, double low, double high, int index, int mask) {

	DataRecord  record;
	double      value;
	int         length;
	int         count;
	int         n;

	//
	// This is the original version that works for either axis, but
	// it's quite a bit slower than the faster version that handles
	// xaxis record collection when fastcollector is true. Probably
	// can be improved some, but the best approach if you think you
	// need better yaxis performance, would be to try to extend the
	// fast algorithm (yaxis sorting and saving yxis slider data are
	// the main missing pieces).
	//

	count = 0;
	length = eventrecords.length;

	for (n = 0; n < length; n++) {
	    record = eventrecords[n].datarecord;
	    value = record.getValue(index);
	    if (value < low || value > high) {
		if (record.isSelected(mask)) {
		    if (record.isSelected())
			hits.setRecord(count++, record, false);
		    record.clearSelected(mask);
		}
	    } else {
		if (record.notSelected(mask)) {
		    record.setSelected(mask);
		    if (record.isSelected())
			hits.setRecord(count++, record, true);
		}
	    }
	}

	return(count);
    }


    private int
    collectRecords2(HitBuffer hits, double low, double high, int index, int mask) {

	DataRecord  record;
	boolean     selecting;
	double      top;
	double      value;
	int         lowindex;
	int         highindex;
	int         length;
	int         count;
	int         n;

	//
	// A rather complicated algorithm that collects the records that
	// are affected whenever the x axis slider moves. Performance is
	// almost always better than the standard version, which always
	// works, but this method assumes eventrecords[] properly sorted
	// and that we save the slider endpoints and their corresponding
	// eventrecords[] indices in the sliderdata[] array, which means
	// it currently can't handle the yaxis.
	//
	// The algorithm works by finding the indices of the ends of the
	// new slider (i.e., low and high) in eventrecords[] by starting
	// from the ends of the previous slider. The new end points and
	// their indices are added to sliderdata[] and the array is then
	// sorted (in groups of 2) by one of our quicksort methods. The
	// four sorted end points divide the axis into five regions and
	// and it's trivial collect records from the two regions where
	// somthing may have changed.
	//
	// NOTE - this algorithm now tries to resync when sliderdata[] is
	// null. Uses the brute force algorithm to calculate count and to
	// find the eventrecords[] indices that correspond to low and high.
	// The old algorithm assumed a null sliderdata[] could be filled
	// in using plotbbox, which is no longer an appropriate assumption
	// and perhaps never was 100%. Incidentally, we tried always using
	// the brute force algorithm, but there really can be a noticeable
	// difference between the two methods.
	//

	count = 0;
	length = eventrecords.length;

	if (sliderdata != null) {
	    if (low >= sliderdata[0]) {
		for (lowindex = (int)sliderdata[1]; lowindex < length; lowindex++) {
		    if (eventrecords[lowindex].realx >= low)
			break;
		}
	    } else {
		for (lowindex = (int)sliderdata[1]; lowindex > 0; lowindex--) {
		    if (eventrecords[lowindex-1].realx < low)
			break;
		}
	    }

	    if (high >= sliderdata[2]) {
		for (highindex = (int)sliderdata[3]; highindex < length; highindex++) {
		    if (eventrecords[highindex].realx > high)
			break;
		}
	    } else {
		for (highindex = (int)sliderdata[3]; highindex > 0; highindex--) {
		    if (eventrecords[highindex-1].realx <= high)
			break;
		}
	    }

	    sliderdata[4] = low;
	    sliderdata[5] = lowindex;
	    sliderdata[6] = high;
	    sliderdata[7] = highindex;
	    YoixMiscQsort.sort(sliderdata, 2);

	    selecting = (sliderdata[0] == low);
	    for (n = (int)sliderdata[1]; n < (int)sliderdata[3]; n++) {
		record = eventrecords[n].datarecord;
	    	if (selecting) {
		    if (record.notSelected(mask)) {
			record.setSelected(mask);
			if (record.isSelected())
			    hits.setRecord(count++, record, true);
		    }
		} else {
		    if (record.isSelected(mask)) {
			if (record.isSelected())
			    hits.setRecord(count++, record, false);
			record.clearSelected(mask);
		    }
		}
	    }

	    selecting = (sliderdata[6] == high);
	    for (n = (int)sliderdata[5]; n < (int)sliderdata[7]; n++) {
		record = eventrecords[n].datarecord;
		if (selecting) {
		    if (record.notSelected(mask)) {
			record.setSelected(mask);
			if (record.isSelected())
			    hits.setRecord(count++, record, true);
		    }
		} else {
		    if (record.isSelected(mask)) {
			if (record.isSelected())
			    hits.setRecord(count++, record, false);
			record.clearSelected(mask);
		    }
		}
	    }
	    sliderdata[0] = low;
	    sliderdata[1] = lowindex;
	    sliderdata[2] = high;
	    sliderdata[3] = highindex;
	} else {
	    //
	    // This is the brute force algorithm modified slightly to also
	    // repopulate sliderdata[] when appropriate. Old version assumed
	    // a null sliderdata[] could be initialized using plotbbox, which
	    // is no longer always correct.
	    //

	    lowindex = -1;
	    highindex = -1;

	    for (n = 0; n < length; n++) {
		record = eventrecords[n].datarecord;
	    	value = record.getValue(index);
	    	if (value < low || value > high) {
		    if (record.isSelected(mask)) {
			if (record.isSelected())
			    hits.setRecord(count++, record, false);
			record.clearSelected(mask);
		    }
		    if (value > high && highindex == -1)
			highindex = n;
		} else {
		    if (record.notSelected(mask)) {
			record.setSelected(mask);
			if (record.isSelected())
			    hits.setRecord(count++, record, true);
		    }
		    if (lowindex == -1 && value > low)
			lowindex = n;
		}
	    }

	    if (datasorted) {
		sliderdata = new double[] {
		    low, lowindex >= 0 ? lowindex : 0,
		    high, highindex >= 0 ? highindex : length,
		    0, 0,
		    0, 0,
		};
	    }
	}

	return(count);
    }


    private int
    collectStackedRecords(HitBuffer hits, double low, double high, int index, int mask) {

	StackedRecord  stackedrecord;
	DataRecord     record;
	int            right;
	int            length;
	int            count;
	int            m;
	int            n;

	//
	// The yaxis slider doesn't work particularly well for stacks and
	// there may not be a quick fix. We intentionally compare low and
	// high to maxy and miny instead of realy, because there was some
	// inconsistent behavior when we used realy and we're not sure it
	// can be easily addressed. The result is a yaxis slider that sort
	// of functions, but not all that well and undoubtedly not the way
	// you would want. We may revisit this in a future release.
	//

	count = 0;

	if (stackedrecords != null) {
	    length = stackedrecords.length;
	    for (n = 0; n < length; n++) {
		stackedrecord = stackedrecords[n];
		right = stackedrecord.right;
		if (stackedrecord.maxy < low || stackedrecord.miny > high) {
		    for (m = stackedrecord.left; m < right; m++) {
			record = eventrecords[m].datarecord;
			if (record.isSelected(mask)) {
			    if (record.isSelected())
				 hits.setRecord(count++, record, false);
			    record.clearSelected(mask);
			}
		    }
		} else {
		    for (m = stackedrecord.left; m < right; m++) {
			record = eventrecords[m].datarecord;
			if (record.notSelected(mask)) {
			    record.setSelected(mask);
			    if (record.isSelected())
				hits.setRecord(count++, record, true);
			}
		    }
		}
	    }
	}

	return(count);
    }


    private int
    collectSweep(HitBuffer hits, int count, BoundingBox bbox, BitSet accumulated) {

	StackedRecord  stackedrecord;
	DataRecord     record;
	ArrayList      list;
	double         x;
	double         y;
	int            m;
	int            n;

	//
	// Using HitBuffer is just a convenience - no records have their
	// state changed and collected records aren't sent back to the
	// DataManager.
	//
	// NOTE - looks like older versions weren't using the accumulated
	// BitSet properly. Fixed on 5/24/06.
	//

	if (eventsstacked && stackedrecords != null) {
	    if ((sweepflags & SWEEP_SOLID_STACKS) != 0) {
		for (n = 0; n < stackedrecords.length; n++) {
		    x = stackedrecords[n].realx;
		    y = Math.min(slidertop, stackedrecords[n].realy);
		    if (bbox.contains(x, y)) {
			for (m = stackedrecords[n].left; m < stackedrecords[n].right; m++) {
			    record = eventrecords[m].datarecord;
			    if (record.isSelected()) {
				if (accumulated == null || accumulated.get(record.getID()) == false)
				    hits.setRecord(count++, record);
			    }
			}
		    }
		}
	    } else {
		for (n = 0; n < stackedrecords.length; n++) {
		    if ((list = stackedrecords[n].getCoveredRecords(bbox)) != null) {
			for (m = 0; m < list.size(); m++) {
			    record = (DataRecord)list.get(m);
			    if (record.isSelected()) {
				if (accumulated == null || accumulated.get(record.getID()) == false)
				    hits.setRecord(count++, record);
			    }
			}
		    }
		}
	    }
	} else {
	    for (n = 0; n < eventrecords.length; n++) {
		record = eventrecords[n].datarecord;
		x = eventrecords[n].plotx;
		y = eventrecords[n].realy;
		if (record.isSelected() && bbox.contains(x, y)) {
		    if (accumulated == null || accumulated.get(record.getID()) == false)
			hits.setRecord(count++, record);
		}
	    }
	}

	return(count);
    }


    private synchronized void
    colorEvents(int counter) {

	colorEvents(currentpalette);
	colorRegions();
	colorStacks(counter);
    }


    private synchronized void
    colorEvents(Palette palette) {

	Color  defaultcolor;
	Color  color;
	int    total;
	int    length;
	int    n;

	if (eventrecords != null && plotbbox != null) {
	    defaultcolor = getForegroundColor();
	    length = eventrecords.length;
	    if (palette != null) {
		total = (int)Math.ceil(plotbbox.lry - plotbbox.uly) + 1;
		for (n = 0; n < length; n++) {
		    color = eventrecords[n].datarecord.getColor();
		    if (gentype != COUNTER_GENERATOR) {
			color = palette.selectColor(
			    (int)eventrecords[n].realy,
			    total,
			    color
			);
		    } else color = palette.selectColor((int)eventrecords[n].realy, color);
		    eventrecords[n].color = (color != null) ? color : defaultcolor;
		}
	    } else {
		for (n = 0; n < length; n++) {
		    color = eventrecords[n].datarecord.getColor();
		    eventrecords[n].color = (color != null) ? color : defaultcolor;
		}
	    }
	}
    }


    private synchronized void
    colorRegions() {

	StackedRegion  regions[];
	StackedSlice   slices[];
	DataColorer    coloredby;
	Object         info[];
	int            length;
	int            m;
	int            n;

	if (stackedrecords != null) {
	    if (stackedslices == null && datamanager != null) {
		if ((coloredby = datamanager.getColoredBy()) != null) {
		    if ((info = coloredby.getNamesAndColors()) != null) {
			length = info.length/2 + 1;
			slices = new StackedSlice[length];
			slices[0] = new StackedSlice(0);
			for (n = 1, m = info.length; n < slices.length; n++, m -= 2)
			    slices[n] = new StackedSlice(n, (String)info[m-2], (Color)info[m-1]);
			regions = new StackedRegion[slices.length];
			for (n = 0; n < regions.length; n++)
			    regions[n] = new StackedRegion();
			stackedslices = slices;
			stackedregions = regions;
		    }
		}
	    }
	}
    }


    private synchronized void
    colorStacks(int counter) {

	int  length;
	int  n;

	if (stackedrecords != null) {
	    length = stackedrecords.length;
	    for (n = 0; n < length; n++)
		stackedrecords[n].sortStack(counter);
	}
    }


    private void
    disableAxes() {

	if (xaxis != null)
	    xaxis.disableAxis();
	if (yaxis != null)
	    yaxis.disableAxis();
    }


    private void
    draw(int left, int limit, Graphics g) {

	switch (plotstyle) {
	    case STYLE_BARS:
		drawEventBars(left, limit, g);
		break;

	    case STYLE_POINTS:
		if (connect == CONNECT_LINES)
		    drawEventLines(left, limit, g);
		drawEventPoints(left, limit, g);
		break;

	    case STYLE_POLYGONS:	// currently inaccessible
		drawEventPolygons(left, limit, g);
		break;

	    case STYLE_STACKED_BARS:
		drawStackBars(left, limit, g);
		break;

	    case STYLE_STACKED_POINTS:
		if (connect == CONNECT_LINES)
		    drawStackLines(left, limit, g);
		drawStackPoints(left, limit, g);
		break;

	    case STYLE_STACKED_POLYGONS:
		drawStackPolygons(left, limit, g);
		break;
	}
    }


    private void
    drawEventBars(int left, int limit, Graphics g) {

	EventRecord  record;
	EventRecord  nextrecord;
	boolean      nonnegative;
	double       ends[];
	double       height;
	Color        color;
	int          delta[];
	int          length;
	int          x;
	int          y;

	delta = pickDelta(false);
	g.translate(delta[0], delta[1]);

	ends = getSliderEnds(yaxis);
	length = eventrecords.length;
	color = g.getColor();

	while (left < length) {
	    record = eventrecords[left++];
	    if ((x = record.x) < limit) {
		if (record.isSelected()) {
		    nonnegative = record.nonnegative;
		    for (; left < length; left++) {
			nextrecord = eventrecords[left];
			if (x == nextrecord.x && nonnegative == nextrecord.nonnegative) {
			    if (record.height < nextrecord.height) {
				if (nextrecord.isSelected())
				    record = nextrecord;
			    }
			} else break;
		    }
		    if (keeptall)
			height = Math.min(record.height, getSliderHeight(nonnegative, ends));
		    else height = record.height;
		    switch (anchor) {
			case YOIX_NORTH:
			    if (nonnegative) {
				y = (int)(plotzeroy - plotpady);
				height += plotpady;
			    } else y = (int)(plotzeroy - height);
			    break;

			default:
			    if (anchor == YOIX_CENTER && symmetric) {
				height = Math.ceil(height);
				y = (int)((plotzeroy - axiswidth/2) - height);
				if (height > 0)
				    height = 2*height + axiswidth - 1;
			    } else {
				if (nonnegative) {
				    y = (int)(plotzeroy - height);
				    height += plotpady;
				} else y = plotzeroy;
			    }
			    break;
		    }
		    if (height > 0) {
			if (color != record.color) {	// test is sufficient
			    color = record.color;
			    g.setColor(color);
			}
			g.fillRect(x, y, linethickness, (int)(height + 1));
		    }
		}
	    } else break;
	}

	g.translate(-delta[0], -delta[1]);
    }


    private void
    drawEventLines(int left, int limit, Graphics g) {

	EventRecord  record;
	EventRecord  nextrecord;
	GeneralPath  path;
	boolean      nonnegative;
	double       ends[];
	int          delta[];
	int          heights[];
	int          height;
	int          sliderheight;
	int          count;
	int          length;
	int          x;
	int          y;
	int          n;

	delta = pickDelta(true);
	g.translate(delta[0], delta[1]);

	heights = new int[4];
	ends = getSliderEnds(yaxis);
	length = eventrecords.length;
	path = new GeneralPath();
	count = 0;

	while (left < length) {
	    record = eventrecords[left++];
	    if ((x = record.x) < limit) {
		if (record.isSelected()) {
		    height = (int)record.height;
		    heights[0] = height;	// entry height
		    heights[1] = height;	// min height
		    heights[2] = height;	// max height
		    heights[3] = height;	// exit height
		    nonnegative = record.nonnegative;
		    for (; left < length; left++) {
			nextrecord = eventrecords[left];
			if (x == nextrecord.x && nonnegative == nextrecord.nonnegative) {
			    if (nextrecord.isSelected()) {
				height = (int)nextrecord.height;
				if (height < heights[1])
				    heights[1] = height;
				if (height > heights[2])
				    heights[2] = height;
				heights[3] = height;
			    }
			} else break;
		    }
		    if (keeptall) {
			sliderheight = getSliderHeight(nonnegative, ends);
			for (n = 0; n < heights.length; n++)
			    heights[n] = Math.min(heights[n], sliderheight);
		    }
		    for (n = 0; n < heights.length; n++) {
			if (n == 0 || heights[n-1] != heights[n]) {
			    switch (anchor) {
				case YOIX_NORTH:
				    y = nonnegative ? plotzeroy + heights[n] : plotzeroy - heights[0];
				    break;

				default:
				    y = nonnegative ? plotzeroy - heights[n] : plotzeroy + heights[0];
				    break;
			    }
			    if (count++ == 0)
				path.moveTo(x, y);
			    else path.lineTo(x, y);
			}
		    }
		}
	    } else break;
	}

	if (count > 1) {
	    g.setColor(getConnectColor());
	    ((Graphics2D)g).setStroke(getConnectStroke1());
	    ((Graphics2D)g).draw(path);
	}

	g.translate(-delta[0], -delta[1]);
    }


    private void
    drawEventPoints(int left, int limit, Graphics g) {

	EventRecord  record;
	EventRecord  nextrecord;
	boolean      nonnegative;
	double       ends[];
	Color        color;
	int          delta[];
	int          length;
	int          height;
	int          x;
	int          y;

	if (hidepoints == false) {
	    delta = pickDelta(false);
	    g.translate(delta[0], delta[1]);

	    ends = getSliderEnds(yaxis);
	    length = eventrecords.length;
	    color = g.getColor();

	    while (left < length) {
		record = eventrecords[left++];
		if ((x = record.x) < limit) {
		    if (record.isSelected()) {
			nonnegative = record.nonnegative;
			//
			// The loop is occasionally a big help, but normally only
			// catches a few records. Subtle differences between the
			// test done here and in drawEventBars() isn't a mistake!!
			//
			for (; left < length; left++) {
			    nextrecord = eventrecords[left];
			    if (x == nextrecord.x && nonnegative == nextrecord.nonnegative) {
				if (nextrecord.isSelected()) {
				    if ((int)record.height == (int)nextrecord.height)
					record = nextrecord;
				    else break;
				}
			    } else break;
			}
			if (keeptall)
			    height = Math.min((int)record.height, getSliderHeight(nonnegative, ends));
			else height = (int)record.height;
			switch (anchor) {
			    case YOIX_NORTH:
				y = nonnegative ? plotzeroy + height : plotzeroy - height;
				break;

			    default:
				y = nonnegative ? plotzeroy - height : plotzeroy + height;
				break;
			}
			if (color != record.color) {	// test is sufficient
			    color = record.color;
			    g.setColor(color);
			}
			g.fillRect(x, y, linethickness, linethickness);
		    }
		} else break;
	    }
	    g.translate(-delta[0], -delta[1]);
	}
    }


    private void
    drawEventPolygons(int left, int limit, Graphics g) {

	EventRecord  record;
	EventRecord  nextrecord;
	GeneralPath  path;
	boolean      nonnegative;
	double       ends[];
	int          delta[];
	int          heights[];
	int          height;
	int          sliderheight;
	int          count;
	int          length;
	int          lasty = 0;		// for the compiler
	int          lastx = 0;		// for the compiler
	int          x;
	int          y;
	int          n;

	//
	// This wasn't all that interesting so there's a pretty good chance
	// it's not currently not accessible to users. Decided to leave the
	// code in anyway.
	//

	delta = pickDelta(true);
	g.translate(delta[0], delta[1]);

	heights = new int[4];
	ends = getSliderEnds(yaxis);
	length = eventrecords.length;
	path = new GeneralPath();
	count = 0;

	while (left < length) {
	    record = eventrecords[left++];
	    if ((x = record.x) < limit) {
		if (record.isSelected()) {
		    height = (int)record.height;
		    heights[0] = height;	// entry height
		    heights[1] = height;	// min height
		    heights[2] = height;	// max height
		    heights[3] = height;	// exit height
		    nonnegative = record.nonnegative;
		    for (; left < length; left++) {
			nextrecord = eventrecords[left];
			if (x == nextrecord.x && nonnegative == nextrecord.nonnegative) {
			    if (nextrecord.isSelected()) {
				height = (int)nextrecord.height;
				if (height < heights[1])
				    heights[1] = height;
				if (height > heights[2])
				    heights[2] = height;
				heights[3] = height;
			    }
			} else break;
		    }
		    if (keeptall) {
			sliderheight = getSliderHeight(nonnegative, ends);
			for (n = 0; n < heights.length; n++)
			    heights[n] = Math.min(heights[n], sliderheight);
		    }
		    lastx = x;
		    for (n = 0; n < heights.length; n++) {
			if (n == 0 || heights[n-1] != heights[n]) {
			    switch (anchor) {
				case YOIX_NORTH:
				    y = nonnegative ? plotzeroy + heights[n] : plotzeroy - heights[0];
				    if (count == 0)
					lasty = 0;
				    break;

				default:
				    y = nonnegative ? plotzeroy - heights[n] : plotzeroy + heights[0];
				    if (count == 0)
					lasty = plotzeroy;
				    break;
			    }
			    if (count++ == 0) {
				path.moveTo(lastx, lasty);
				path.lineTo(x, y);
			    } else path.lineTo(x, y);
			}
		    }
		}
	    } else break;
	}

	if (count > 1) {
	    g.setColor(getConnectColor());
	    ((Graphics2D)g).setStroke(getConnectStroke1());
	    path.lineTo(lastx, lasty);
	    path.closePath();
	    ((Graphics2D)g).fill(path);
	    ((Graphics2D)g).draw(path);
	}

	g.translate(-delta[0], -delta[1]);
    }


    private void
    drawRegion(StackedRegion region) {

	if (EventQueue.isDispatchThread() == false) {
	    EventQueue.invokeLater(
		new YoixAWTInvocationEvent(
		    this,
		    new Object[] {new Integer(RUN_DRAWREGION), region}
		)
	    );
	} else handleDrawRegion(region);
    }


    private void
    drawRegionBar(StackedRegion region, Graphics g) {

	int  delta[];

	if (region != null && region.sliceindex >= 0) {
	    if (region.height > 0) {
		delta = pickDelta(false);
		g.translate(delta[0], delta[1]);
		g.setColor(pickRegionColor(region));
		((Graphics2D)g).fill(
		    new Rectangle2D.Float(
			region.x,
			region.y,
			linethickness,
			region.height
		    )
		);
		g.translate(-delta[0], -delta[1]);
	    }
	}
    }


    private void
    drawRegionLine(StackedRegion region, Graphics g) {

	GeneralPath  path;
	boolean      drawing;
	boolean      empty;
	float        height;
	float        x;
	float        y;
	int          index;
	int          delta[];
	int          ends[];
	int          left;
	int          right;
	int          n;

	//
	// The point and line methods for stacked plots must coordinate
	// how they handle x and y coordinates. We decided to use floats
	// even though we explicitly cast coordinates to integers.
	//
	// This is only used when we're highlighting, which is disabled
	// when stacks are too close, so there was no pressing reason to
	// duplicate the drawStackLines() code that builds an efficient
	// path when stacks land on the same pixel. Might change if you
	// start using this method for other purposes.
	//

	if (region != null && (index = region.sliceindex) >= 0) {
	    if ((ends = getSelectedStackEnds()) != null) {
		left = ends[0];
		right = ends[1];
		empty = true;
		drawing = false;
		path = null;
		for (n = left; n <= right; n++) {
		    x = (int)stackedrecords[n].getRegionX();
		    y = (int)stackedrecords[n].getRegionTop(index);
		    height = stackedrecords[n].getRegionHeight(index);
		    if (path == null) {
			path = new GeneralPath();
			path.moveTo(x, y);
			drawing = (height > 0);
		    } else {
			if (drawing || height > 0) {
			    path.lineTo(x, y);
			    empty = false;
			}
			if (height == 0) {
			    path.moveTo(x, y);
			    drawing = false;
			} else drawing = true;
		    }
		}
		if (empty == false) {
		    delta = pickDelta(true);
		    g.translate(delta[0], delta[1]);
		    g.setColor(pickRegionColor(region));
		    ((Graphics2D)g).setStroke(getConnectStroke2());
		    ((Graphics2D)g).draw(path);
		    g.translate(-delta[0], -delta[1]);
		}
	    }
	}
    }


    private void
    drawRegionPoint(StackedRegion region, Graphics g) {

	GeneralPath  path;
	Rectangle2D  rect;
	boolean      empty;
	float        height;
	float        x;
	float        y;
	int          index;
	int          delta[];
	int          ends[];
	int          left;
	int          right;
	int          n;


	//
	// The point and line methods for stacked plots must coordinate
	// how they handle x and y coordinates. We decided to use floats
	// even though we explicitly cast coordinates to integers.
	//
	// When points are connected we already highlight the connecting
	// lines (handled elsewhere), but we also want to highlight every
	// point in the region's slice.
	//

	if (hidepoints == false) {
	    if (region != null && (index = region.sliceindex) >= 0) {
		delta = pickDelta(false);
		g.translate(delta[0], delta[1]);
		g.setColor(pickRegionColor(region));
		path = new GeneralPath();
		rect = new Rectangle2D.Float();
		empty = true;
		if (connect == CONNECT_LINES) {
		    if ((ends = getSelectedStackEnds()) != null) {
			left = ends[0];
			right = ends[1];
			for (n = left; n <= right; n++) {
			    if ((region = stackedrecords[n].getRegion(index)) != null) {
				if (region.height > 0) {
				    rect.setRect(
					(int)region.x,
					(int)region.y,
					linethickness,
					linethickness
				    );
				    path.append(rect, false);
				    empty = false;
				}
			    }
			}
		    }
		} else {
		    if (region.height > 0) {
			rect.setRect(
			    (int)region.x,
			    (int)region.y,
			    linethickness,
			    linethickness
			);
			path.append(rect, false);
			empty = false;
		    }
		}
		if (empty == false)
		    ((Graphics2D)g).fill(path);
		g.translate(-delta[0], -delta[1]);
	    }
	}
    }


    private void
    drawRegionPolygon(StackedRegion region, Graphics g) {

	StackedRecord  stackedrecord;
	GeneralPath    path;
	float          x[];
	float          y0[];
	float          y1[];
	int            firststack;
	int            laststack;
	int            stackcount;
	int            subpathstart;
	int            delta[];
	int            ends[];
	int            index;
	int            m;
	int            n;

	//
	// Drawing done here duplicates the drawStackPolygons() approach,
	// so take a look at the comments in that method for more details.
	// Unfortunatly "spikes" can currently leak into regions above
	// the one we're working on, which can cause some small coloring
	// problems that are more noticeable when the buckets are closely
	// packed.
	// 

	if (region != null && (index = region.sliceindex) >= 0) {
	    if ((ends = getSelectedStackEnds()) != null) {
		stackcount = ends[1] - ends[0] + 1;
		x = new float[stackcount];
		y0 = new float[stackcount];
		y1 = new float[stackcount];

		firststack = -1;
		laststack = -1;
		for (n = ends[0], m = 0; n <= ends[1]; n++, m++) {
		    stackedrecord = stackedrecords[n];
		    x[m] = stackedrecord.getRegionX();
		    y0[m] = stackedrecord.getRegionBottom(index);
		    y1[m] = stackedrecord.getRegionTop(index);
		    if (y0[m] != y1[m]) {
			if (firststack < 0)
			    firststack = m;
			laststack = m;
		    }
		}

		if (firststack >= 0) {
		    path = new GeneralPath();
		    subpathstart = -1;
		    laststack = Math.min(laststack + 1, stackcount - 1);
		    for (n = Math.max(firststack - 1, 0); n <= laststack; n++) {
			if (subpathstart < 0) {
			    if (y0[n] != y1[n] || y0[n+1] != y1[n+1]) {
				path.moveTo(x[n], y0[n]);
				subpathstart = n;
			    }
			} else {
			    path.lineTo(x[n], y0[n]);
			    if (n < laststack && y0[n] == y1[n] && y0[n+1] == y1[n+1]) {
				for (m = n; m >= subpathstart; m--)
				    path.lineTo(x[m], y1[m]);
				path.closePath();
				subpathstart = -1;
			    }
			}
		    }

		    if (subpathstart >= 0) {
			for (m = laststack; m >= subpathstart; m--)
			    path.lineTo(x[m], y1[m]);
			path.closePath();
		    }

		    delta = pickDelta(false);
		    g.translate(delta[0], delta[1]);
		    g.setColor(pickRegionColor(region));
		    ((Graphics2D)g).fill(path);
		    ((Graphics2D)g).draw(path);
		    g.translate(-delta[0], -delta[1]);
		}
	    }
	}
    }


    private void
    drawStackBars(int left, int limit, Graphics g) {

	StackedRecord  stackedrecord;
	StackedRegion  region;
	Rectangle2D    rect;
	Color          nextcolor;
	Color          color;
	int            delta[];
	int            length;
	int            n;

	delta = pickDelta(false);
	g.translate(delta[0], delta[1]);

	length = stackedrecords.length;
	left = (left < stackedindices.length) ? stackedindices[left] : length;
	rect = new Rectangle2D.Float();
	color = g.getColor();

	while (left < length) {
	    stackedrecord = stackedrecords[left++];
	    if (stackedrecord.getX(limit) < limit) {
		if (stackedrecord.isSelected()) {
		    for (region = stackedrecord.getBottomRegion(); region != null; region = region.up) {
			if (region.height > 0) {
			    rect.setRect(
				region.x,
				region.y,
				linethickness,
				region.height
			    );
			    nextcolor = pickRegionColor(region);
			    if ((nextcolor = pickRegionColor(region)) != color) {
				g.setColor(nextcolor);
				color = nextcolor;
			    }
			    ((Graphics2D)g).fill(rect);
			}
		    }
		}
	    } else break;
	}

	g.translate(-delta[0], -delta[1]);
    }


    private void
    drawStackLines(int left, int limit, Graphics g) {

	StackedRecord  stackedrecord;
	StackedRecord  nextrecord;
	GeneralPath    path;
	boolean        drawing;
	boolean        empty;
	boolean        newpath;
	Color          nextcolor;
	Color          color;
	float          tops[];
	float          height;
	float          x;
	float          y;
	int            delta[];
	int            ends[];
	int            right;
	int            length;
	int            k;
	int            m;
	int            n;

	//
	// The point and line methods for stacked plots must coordinate
	// how they handle x and y coordinates. We decided to use floats
	// even though we explicitly cast coordinates to integers. Test
	// carefully if you decide to change things.
	//
	// The code that tries to simplify the path when stacks land on
	// the same pixel resembles what's done in the event version of
	// this method, but the path that's built here isn't a simple,
	// so the code is also more complicated. Was added rather late,
	// our implementation was probably more cautious than necessary.
	//

	delta = pickDelta(true);
	g.translate(delta[0], delta[1]);

	left = (left < stackedindices.length) ? stackedindices[left] : stackedrecords.length;

	if ((ends = getSelectedStackEnds(left, limit)) != null) {
	    ((Graphics2D)g).setStroke(getConnectStroke2());
	    left = ends[0];
	    right = ends[1];
	    tops = new float[4];
	    path = new GeneralPath();
	    empty = true;
	    nextcolor = pickSliceColor(0);
	    length = stackedslices.length;
	    for (n = 0; n < length; n++) {
		drawing = false;
		newpath = true;
		color = nextcolor;
		for (m = left; m <= right; m++) {
		    stackedrecord = stackedrecords[m];
		    x = (int)stackedrecord.getRegionX();
		    y = (int)stackedrecord.getRegionTop(n);
		    height = stackedrecord.getRegionHeight(n);
		    if (drawing && height > 0 && m < right) {
			if (x == (int)stackedrecords[m+1].getRegionX()) {
			    //
			    // This should help performance when lots of
			    // stacks land on the same pixel. Was added
			    // late so we tried not to be too ambitious
			    // about when we end up here.
			    // 
			    tops[0] = y;	// entry top
			    tops[1] = y;	// min top
			    tops[2] = y;	// max top
			    tops[3] = y;	// exit top
			    for (; m < right && drawing; m++) {
				nextrecord = stackedrecords[m+1];
				if (x == (int)nextrecord.getRegionX()) {	// nonnegative check???
				    y = (int)nextrecord.getRegionTop(n);
				    if (y < tops[1])
					tops[1] = y;
				    if (y > tops[2])
					tops[2] = y;
				    tops[3] = y;
				    drawing = (nextrecord.getRegionHeight(n) > 0);
				} else break;
			    }
			    for (k = 0; k < tops.length; k++) {
				if (k == 0 || tops[k-1] != tops[k])
				    path.lineTo(x, tops[k]);
			    }
			} else {
			    path.lineTo(x, y);
			    empty = false;
			}
		    } else {
			//
			// Everything was handled here in the old version
			// of this method, but performance probably would
			// suffer if lots of stacks land on the same pixel.
			//
			if (newpath) {
			    path.moveTo(x, y);
			    newpath = false;
			    drawing = (height > 0);
			} else {
			    if (drawing || height > 0) {
				path.lineTo(x, y);
				empty = false;
			    }
			    if (height == 0) {
				path.moveTo(x, y);
				drawing = false;
			    } else drawing = true;
			}
		    }
		}
		nextcolor = pickSliceColor(n+1);
		if (empty == false) {
		    if (color != nextcolor) {	// test is sufficient
			g.setColor(color);
			((Graphics2D)g).draw(path);
			path.reset();
			empty = true;
		    }
		}
	    }
	}

	g.translate(-delta[0], -delta[1]);
    }


    private void
    drawStackPoints(int left, int limit, Graphics g) {

	StackedRecord  stackedrecord;
	StackedRegion  region;
	Rectangle2D    rect;
	Color          nextcolor;
	Color          color;
	int            delta[];
	int            length;
	int            n;

	//
	// The point and line methods for stacked plots must coordinate
	// how they handle x and y coordinates. We decided to use floats
	// even though we explicitly cast coordinates to integers. Both
	// methods should do the same thing.
	//

	if (hidepoints == false) {
	    delta = pickDelta(false);
	    g.translate(delta[0], delta[1]);

	    length = stackedrecords.length;
	    left = (left < stackedindices.length) ? stackedindices[left] : length;
	    rect = new Rectangle2D.Float();
	    color = g.getColor();

	    while (left < length) {
		stackedrecord = stackedrecords[left++];
		if (stackedrecord.getX(limit) < limit) {
		    if (stackedrecord.isSelected()) {
			for (region = stackedrecord.getBottomRegion(); region != null; region = region.up) {
			    if (region.height > 0) {
				rect.setRect(
				    (int)region.x,
				    (int)region.y,
				    linethickness,
				    linethickness
				);
				nextcolor = pickRegionColor(region);
				if ((nextcolor = pickRegionColor(region)) != color) {
				    g.setColor(nextcolor);
				    color = nextcolor;
				}
				((Graphics2D)g).fill(rect);
			    }
			}
		    }
		} else break;
	    }
	    g.translate(-delta[0], -delta[1]);
	}
    }


    private void
    drawStackPolygons(int left, int limit, Graphics g) {

	StackedRecord  stackedrecord;
	GeneralPath    path;
	Color          color;
	float          x[];
	float          y0[];
	float          y1[];
	int            firststack;
	int            laststack;
	int            stackcount;
	int            subpathstart;
	int            delta[];
	int            ends[];
	int            bottom;
	int            top;
	int            length;
	int            m;
	int            n;

	//
	// Unfortunately simple approaches, like building the polygons as
	// a single connected path, start to break down when buckets are
	// closely packed. Filling the path can miss spikes that show up
	// if the path is filled and drawn, however that means the lines
	// that represent "empty" portions also show up, which leads to
	// misleading plots. So we split slices into subpaths that can
	// be filled and stroked and we erase before drawing anything by
	// subtracting the outline of the entire plot from the clipping
	// path and then clearing that area.
	//
	// This approach is a compromise that's reasonable and results in
	// pretty good performance, however it does introduce some minor
	// "highlighting" problems. We tried quite a few other approaches
	// and every one exhibited its own set of problems when pushed to
	// extremes. This will do for now, but we probably should revisit
	// the implementation at some point in the future.
	//
	// NOTE - we're currently not doing anything to simplify the path
	// when stacks land on the same pixel. Probalby wouldn't be hard
	// to add, but we didn't have time for anymore optimization, at
	// least not right now.
	// 

	delta = pickDelta(false);
	g.translate(delta[0], delta[1]);
	((Graphics2D)g).setStroke(OUTLINESTROKE);

	left = (left < stackedindices.length) ? stackedindices[left] : stackedrecords.length;

	if ((ends = getSelectedStackEnds(left, limit)) != null) {
	    erasePlot(ends[0], ends[1], g);
	    stackcount = ends[1] - ends[0] + 1;
	    x = new float[stackcount];
	    y0 = new float[stackcount];
	    y1 = new float[stackcount];

	    path = new GeneralPath();
	    length = stackedslices.length;
	    for (bottom = 0; bottom < length; bottom = top + 1) {
		color = pickSliceColor(bottom);
		for (top = bottom; top < length - 1; top++) {
		    //
		    // Direct equality test is probably OK here, but didn't
		    // want to take any chances right now.
		    //
		    if (color.equals(pickSliceColor(top+1)) == false)
			break;
		}

		firststack = -1;
		laststack = -1;
		for (n = ends[0], m = 0; n <= ends[1]; n++, m++) {
		    stackedrecord = stackedrecords[n];
		    x[m] = stackedrecord.getRegionX();
		    y0[m] = stackedrecord.getRegionBottom(bottom);
		    y1[m] = stackedrecord.getRegionTop(top);
		    if (y0[m] != y1[m]) {
			if (firststack < 0)
			    firststack = m;
			laststack = m;
		    }
		}

		if (firststack >= 0) {
		    path.reset();
		    subpathstart = -1;
		    laststack = Math.min(laststack + 1, stackcount - 1);
		    for (n = Math.max(firststack - 1, 0); n <= laststack; n++) {
			if (subpathstart < 0) {
			    if (y0[n] != y1[n] || y0[n+1] != y1[n+1]) {
				path.moveTo(x[n], y0[n]);
				subpathstart = n;
			    }
			} else {
			    path.lineTo(x[n], y0[n]);
			    if (n < laststack && y0[n] == y1[n] && y0[n+1] == y1[n+1]) {
				for (m = n; m >= subpathstart; m--)
				    path.lineTo(x[m], y1[m]);
				path.closePath();
				subpathstart = -1;
			    }
			}
		    }

		    if (subpathstart >= 0) {
			for (m = laststack; m >= subpathstart; m--)
			    path.lineTo(x[m], y1[m]);
			path.closePath();
		    }

		    g.setColor(color);
		    ((Graphics2D)g).fill(path);
		    ((Graphics2D)g).draw(path);
		}
	    }
	} else eraseRect(g.getClipBounds(), g);

	g.translate(-delta[0], -delta[1]);
    }


    private void
    drawSweepBox() {

	Graphics  g;

	if (xcorner[0] != xcorner[2] || ycorner[0] != ycorner[2]) {
	    if ((g = YoixMiscJFC.getMaskedGraphics(this)) != null) {
		g.translate(insets.left, insets.top);
		g.setColor(sweepcolor);
		g.setXORMode(getBackground());
		if (timeshading != 0) {
		    drawXORLine(xcorner[0], ycorner[0], xcorner[1], ycorner[1], g);
		    drawXORLine(xcorner[1], ycorner[1], xcorner[2], ycorner[2], g);
		    drawXORLine(xcorner[2], ycorner[2], xcorner[3], ycorner[3], g);
		    drawXORLine(xcorner[3], ycorner[3], xcorner[0], ycorner[0], g);
		} else g.drawPolygon(xcorner, ycorner, 4);
		g.dispose();
	    }
	}
    }


    private void
    drawXORLine(int x0, int y0, int x1, int y1, Graphics g) {

	int  delta;
	int  x;

	if (x0 == x1) {
	    delta = (y1 >= y0) ? 1 : -1;
	    g.setXORMode(getBackgroundColor(x0, y0));
	    g.drawLine(x0, y0, x1, y1 - delta);
	} else {
	    delta = (x1 >= x0) ? 1 : -1;
	    for (; x0 != x1; x0 = x) {
		for (x = x0 + delta; x != x1; x += delta) {
		    if (isPeakPixel(x) != isPeakPixel(x0))
			break;
		}
		g.setXORMode(getBackgroundColor(x0, y0));
		g.drawLine(x0, y0, x - delta, y1);
	    }
	}
	g.setPaintMode();
    }


    private void
    eraseArea(Area area, Graphics g) {

	Rectangle  rect;
	Shape      clip;

	clip = g.getClip();
	rect = area.getBounds();
	g.setClip(area);
	eraseRect(rect.x, rect.y, rect.width, rect.height, g);
	g.setClip(clip);
    }


    private void
    erasePlot(int left, int right, Graphics g) {

	StackedRecord  stackedrecord;
	GeneralPath    path;
	float          x;
	float          y;
	Area           area;
	int            index;
	int            n;

	//
	// Assumes that the stacks between left and right that make up a
	// polygon plot are about to be completely repainted, so we can
	// safely just erase the part of the clipping path that we think
	// won't be repainted. If this method called for any other plot
	// style it just erases the entire clipping area, but there's a
	// chance we'll eventually extend this to other stacked plots.
	//

	if (stackedrecords != null || plotstyle == STYLE_STACKED_POLYGONS) {
	    path = null;
	    index = 0;
	    for (n = left; n <= right; n++) {
		stackedrecord = stackedrecords[n];
		stackedrecord.resetNextRegion();
		x = stackedrecord.getRegionX();
		y = stackedrecord.getRegionBottom(index);
		if (path == null) {
		    path = new GeneralPath();
		    path.moveTo(x, y);
		} else path.lineTo(x, y);
	    }
	    if (path != null) {
		index = stackedslices.length - 1;
		for (n = right; n >= left; n--) {
		    stackedrecord = stackedrecords[n];
		    x = stackedrecord.getRegionX();
		    y = stackedrecord.getRegionTop(index);
		    path.lineTo(x, y);
		    stackedrecord.resetNextRegion();
		}
		path.closePath();
		area = new Area(g.getClip());
		area.subtract(new Area(path));
		eraseArea(area, g);
	    } else eraseRect(g.getClipBounds(), g);
	} else eraseRect(g.getClipBounds(), g);
    }


    private void
    eraseRect(Rectangle rect, Graphics g) {

	if (rect != null)
	    eraseRect(rect.x, rect.y, rect.width, rect.height, g);
    }


    private void
    eraseRect(int x, int y, int width, int height, Graphics g) {

	int  x1;
	int  x2;

	//
	// We only draw the axis when anchor is YOIX_CENTER or YOIX_NONE
	// because the other variations should normally have y == 0 very
	// close to the top or bottom of the plot.
	//

	if (timeshading != 0 && peakpixels != null) {
	    x1 = Math.min(x + width, peakpixels.length);
	    x = Math.max(x, 0);
	    for (; x < x1; x = x2) {
		for (x2 = x+1; x2 < x1; x2++) {
		    if (peakpixels[x2] != peakpixels[x])
			break;
		}
		g.setColor(getBackgroundColor(x, y));
		g.fillRect(x, y, x2 - x, height);
	    }
	} else {
	    g.setColor(getBackgroundColor(x, y));
	    g.fillRect(x, y, width, height);
	}

	if (axiswidth > 0 && plotheight > 0) {
	    switch (anchor) {
		case YOIX_CENTER:
		case YOIX_NONE:
		    g.setColor(getForeground());
		    g.fillRect(
			viewport.x,
			viewport.y + plotzeroy - axiswidth/2,
			viewport.width,
			axiswidth
		    );
		    break;
	    }
	}
    }


    private synchronized StackedRegion
    findRegionAt(Point p) {

	StackedRegion  region = null;

	if (eventsstacked && p != null) {
	    switch (plotstyle) {
		case STYLE_STACKED_BARS:
		    region = findRegionInBars(p);
		    break;

		case STYLE_STACKED_POINTS:
		    if (connect == CONNECT_LINES)
			region = findRegionInPolygons(p, true);
		    else region = findRegionInPoints(p);
		    break;

		case STYLE_STACKED_POLYGONS:
		    region = findRegionInPolygons(p, false);
		    break;
	    }
	}
	return(region);
    }


    private StackedRegion
    findRegionInBars(Point p) {

	StackedRecord  stackedrecord;
	StackedRegion  found = null;
	StackedRegion  region;
	Rectangle2D    rect;
	int            delta[];
	int            length;
	int            n;

	if (stackedrecords != null) {
	    delta = pickDelta(false);
	    p = new Point(p.x - delta[0], p.y - delta[1]);
	    length = stackedrecords.length;
	    for (n = pickHorizontalFindStart(p); n < length; n++) {
		stackedrecord = stackedrecords[n];
		if (stackedrecord.isSelected()) {
		    if ((region = stackedrecord.getBottomRegion()) != null) {
			if (p.x < region.x + linethickness) {
			    if (region.x <= p.x) {
				laststackfind = n;
				if (stackedrecord.realy != 0) {
				    rect = new Rectangle2D.Float();
				    for (; region != null; region = region.up) {
					rect.setRect(
					    region.x,
					    region.y,
					    linethickness,
					    region.height
					);
					if (rect.contains(p)) {
					    found = region;
					    break;
					}
				    }
				}
			    }
			    break;
			}
		    }
		}
	    }
	}
	return(found);
    }


    private StackedRegion
    findRegionInPoints(Point p) {

	StackedRecord  stackedrecord;
	StackedRegion  found = null;
	StackedRegion  region;
	Rectangle2D    rect;
	int            delta[];
	int            length;
	int            n;

	if (hidepoints == false) {
	    if (stackedrecords != null) {
		delta = pickDelta(false);
		p = new Point(p.x - delta[0], p.y - delta[1]);
		length = stackedrecords.length;
		for (n = pickHorizontalFindStart(p); n < length; n++) {
		    stackedrecord = stackedrecords[n];
		    if (stackedrecord.isSelected()) {
			if ((region = stackedrecord.getBottomRegion()) != null) {
			    if (p.x < region.x + linethickness) {
				if (region.x <= p.x) {
				    laststackfind = n;
				    if (stackedrecord.realy != 0) {
					rect = new Rectangle2D.Float();
					for (; region != null; region = region.up) {
					    rect.setRect(
						region.x,
						region.y,
						linethickness,
						region.height
					    );
					    if (rect.contains(p)) {
						rect.setRect(
						    region.x,
						    region.y,
						    linethickness,
						    linethickness
						);
						if (rect.contains(p))
						    found = region;
						break;
					    }
					}
				    }
				}
				break;
			    }
			}
		    }
		}
	    }
	}
	return(found);
    }


    private StackedRegion
    findRegionInPolygons(Point p, boolean lines) {

	StackedRecord  left;
	StackedRecord  right;
	StackedRegion  region = null;
	GeneralPath    path;
	float          leftx;
	float          rightx;
	int            delta[];
	int            first;
	int            last;
	int            m;
	int            n;

	if (stackedrecords != null) {
	    delta = pickDelta(lines);
	    p = new Point(p.x - delta[0], p.y - delta[1]);
	    path = new GeneralPath();
	    for (n = pickHorizontalFindStart(p); n < stackedrecords.length - 1; n++) {
		left = stackedrecords[n];
		right = stackedrecords[n+1];
		leftx = left.getRegionX();
		rightx = right.getRegionX();
		if (p.x < rightx) {
		    if (leftx <= p.x) {
			laststackfind = n;
			if (isPolygonShowingAt(n)) {
			    left.resetNextRegion();
			    right.resetNextRegion();
			    first = Math.min(left.getBottomSliceIndex(), right.getBottomSliceIndex());
			    last = Math.max(left.getTopSliceIndex(), right.getTopSliceIndex());
			    for (m = first; m <= last; m++) {
				if (left.getRegionHeight(m) > 0 || right.getRegionHeight(m) > 0) {
				    path.reset();
				    path.moveTo(leftx, left.getRegionBottom(m));
				    path.lineTo(rightx, right.getRegionBottom(m));
				    path.lineTo(rightx, right.getRegionTop(m));
				    path.lineTo(leftx, left.getRegionTop(m));
				    path.closePath();
				    if (path.contains(p)) {
					region = left.getRegionDescription(m);
					break;
				    }
				}
			    }
			}
		    }
		    break;
		}
	    }
	}
	return(region);
    }


    private void
    handleAutoFit() {

	BoundingBox  bbox;

	//
	// Eventually may be controlled by a variable named autofit, but
	// for the time being we're only interested in the yaxis behavior
	// of stacked were zoomed to display subset but are now asked to
	// dislay data that no longer fits vertically in the space that's
	// available. Not an issue with simple event plots because events
	// that don't fit must also have been deselected by the y axis.
	//
	// NOTE - probably wouldn't be hard to add visual effects here,
	// like using a timer to fit the plot to the new bounds in small
	// steps.
	//

	if (eventsstacked) {
	    if (plotbbox != null && yaxis != null) {
		if ((bbox = getDataBBox()) != null) {
		    //
		    // We're currently only interested in yaxis, so we
		    // forcably set xaxis values in bbox. Might need to
		    // change if autofitting becomes more general.
		    //
		    bbox.ulx = plotbbox.ulx;
		    bbox.lrx = plotbbox.lrx;
		    if (bbox.equals(plotbbox) == false) {
			if (plotbbox.covers(bbox) == false) {
			    //
			    // Undoubtedly not 100% right for all values
			    // of anchor and model, but it's good enough
			    // for now. Will revisit in the near future.
			    //
			    plotbbox.uly = bbox.uly;
			    plotbbox.lry = bbox.lry;
			    setAxisEnds(yaxis);
			    reset(true);
			}
		    }
		}
	    }
	}
    }


    private void
    handleDrawRegion(StackedRegion region) {

	StackedRegion  highlighted;
	Graphics       g;

	//
	// Manages the drawing and highlighting of the current region and
	// is also responsible for updating currentregion.
	//
	// NOTE - this should only be called from the event thread because
	// YoixMiscJFC.getMaskedGraphics() needs the AWTTreeLock.
	//

	if (region != currentregion) {
	    if ((g = YoixMiscJFC.getMaskedGraphics(this, layeredpane)) != null) {
		g.translate(insets.left, insets.top);
		g.clipRect(0, 0, viewport.width, viewport.height);
		switch (plotstyle) {
		    case STYLE_STACKED_BARS:
			if ((highlighted = currentregion) != null) {
			    currentregion = null;
			    drawRegionBar(highlighted, g);
			}
			if ((currentregion = region) != null)
			    drawRegionBar(currentregion, g);
			break;

		    case STYLE_STACKED_POINTS:
			if ((highlighted = currentregion) != null) {
			    currentregion = null;
			    drawRegionPoint(highlighted, g);
			    if (connect == CONNECT_LINES)
				drawRegionLine(highlighted, g);
			}
			if ((currentregion = region) != null) {
			    drawRegionPoint(currentregion, g);
			    if (connect == CONNECT_LINES)
				drawRegionLine(currentregion, g);
			}
			break;

		    case STYLE_STACKED_POLYGONS:
			if (region == null || currentregion == null || region.sliceindex != currentregion.sliceindex) {
			    if ((highlighted = currentregion) != null) {
				currentregion = null;
				drawRegionPolygon(highlighted, g);
			    }
			    if ((currentregion = region) != null)
				drawRegionPolygon(currentregion, g);
			} else currentregion = region;
			break;

		}
		g.dispose();
	    }
	}
    }


    private void
    generateValues() {

	EventRecord  record;
	DataRecord   datarecord;
	double       incr;
	double       limit;
	double       value;
	double       yorigin;
	int          genindex;
	int          m;
	int          n;

	switch (gentype) {
	    case COUNTER_GENERATOR:
		buildEventBucketIndices();
		genindex = genindices[0];
		yorigin = plotbbox.uly;
		for (n = 0; n < eventrecords.length; n++) {
		    record = eventrecords[n];
		    datarecord = record.datarecord;
		    record.realy += yorigin;	// is this really necessary??
		    if (record.selected != datarecord.isSelected()) {
			if ((value = datarecord.getValue(genindex)) != 0) {
			    incr = record.selected ? -1 : 1;
			    limit = record.realx;
			    record.selected = !record.selected;
			    record.realy += incr;
			    m = eventsbucketed ? eventbucketindices[n] : n + 1;
			    for (; m < eventrecords.length; m++) {
				if (eventrecords[m].realx == limit) {
				    if (eventrecords[m] != record) {
					if (eventrecords[m].datax == record.datax) {
					    if (eventrecords[m].datarecord.getValue(genindex) != 0)
						eventrecords[m].realy += incr;
					}
				    }
				} else break;
			    }
			}
		    }
		}
		break;

	    case OVERLAP_GENERATOR:
		buildEventBucketIndices();
		genindex = genindices[0];
		yorigin = plotbbox.uly;
		for (n = 0; n < eventrecords.length; n++) {
		    record = eventrecords[n];
		    datarecord = record.datarecord;
		    record.realy += yorigin;	// is this really necessary??
		    if (record.selected != datarecord.isSelected()) {
			incr = record.selected ? -1 : 1;
			limit = datarecord.getValue(genindex);
			record.selected = !record.selected;
			m = eventsbucketed ? eventbucketindices[n] : n + 1;
			for (; m < eventrecords.length; m++) {
			    if (eventrecords[m].realx < limit) {
				if (eventrecords[m] != record) {
				    if (eventrecords[m].datax < limit && eventrecords[m].datax >= record.datax)
					eventrecords[m].realy += incr;
				}
			    } else break;
			}
		    }
		}
		break;
	}
    }


    private Color
    getBackgroundColor(int x, int y) {

	Color  color;

	if (timeshading != 0 && peakpixels != null) {
	    if (timeshading < 0)
		color = isPeakPixel(x) ? offpeakcolor : getBackground();
	    else color = isPeakPixel(x) ? getBackground() : offpeakcolor;
	} else color = getBackground();

	return(color);
    }


    private double[]
    getBucketSettings(YoixObject obj) {

	double  buckets[] = null;

	if (obj != null && obj.notNull()) {
	    if (obj.isArray()) {
		if (obj.sizeof() == 1) {
		    buckets = new double[] {0, obj.getDouble(0, 0)};
		    if (buckets[1] <= 0)
			buckets = null;
		} else if (obj.sizeof() == 2) {
		    buckets = new double[] {obj.getDouble(0, 0), obj.getDouble(1, 0)};
		    if (buckets[1] <= 0)
			buckets = null;
		} else if (obj.sizeof() == 3 || obj.sizeof() == 4) {
		    buckets = new double[] {
			obj.getDouble(0, 0),
			obj.getDouble(1, 0),				// width
			obj.getDouble(2, 1),				// min width
			obj.getDouble(3, Double.POSITIVE_INFINITY),	// max width
		    };
		} else if (obj.sizeof() > 4) {
		    buckets = new double[] {
			obj.getDouble(0, 0),
			obj.getDouble(1, 0),				// width
			obj.getDouble(2, 0),				// min width
			obj.getDouble(3, 1),				// step
			obj.getDouble(4, Double.POSITIVE_INFINITY)	// max width
		    };
		} else if (obj.sizeof() == 0)
		    buckets = new double[0];
	    } else if (obj.isNumber()) {
		buckets = new double[] {0.0, obj.doubleValue()};
		if (buckets[1] <= 0)
		    buckets = null;
	    }
	}
	return(buckets);
    }


    private BasicStroke
    getConnectStroke1() {

	int  width;

	if (connectstroke1 == null) {
	    width = pickConnectWidth();
	    if (width > 2) {
		connectstroke1 = new BasicStroke(
		    width,
		    BasicStroke.CAP_ROUND,
		    BasicStroke.JOIN_ROUND
		);
	    } else {
		connectstroke1 = new BasicStroke(
		    width,
		    BasicStroke.CAP_BUTT,
		    BasicStroke.JOIN_BEVEL
		);
	    }
	}
	return(connectstroke1);
    }


    private BasicStroke
    getConnectStroke2() {

	int  width;

	if (connectstroke2 == null) {
	    width = Math.max(connectwidth/2, 1);
	    if (width > 2) {
		connectstroke2 = new BasicStroke(
		    width,
		    BasicStroke.CAP_ROUND,
		    BasicStroke.JOIN_ROUND
		);
	    } else {
		connectstroke2 = new BasicStroke(
		    width,
		    BasicStroke.CAP_BUTT,
		    BasicStroke.JOIN_BEVEL
		);
	    }
	}
	return(connectstroke2);
    }


    private synchronized BoundingBox
    getDataBBox() {

	StackedRecord  stackedrecord;
	EventRecord    record;
	BoundingBox    bbox;
	int            length;
	int            n;

	if (eventrecords != null && eventbbox != null) {
	    if (eventsstacked) {
		if (totalcount != selectedcount) {
		    bbox = new BoundingBox();
		    length = stackedrecords.length;
		    for (n = 0; n < length; n++) {
			stackedrecord = stackedrecords[n];
			if (stackedrecord.isSelected()) {
			    if (stackedrecord.isBuilt()) {
				bbox.add(stackedrecord.realx, 0);
				bbox.add(stackedrecord.realx, stackedrecord.realy);
			    }
			}
		    }
		} else bbox = new BoundingBox(stackedbbox);
	    } else {
		if (totalcount != selectedcount) {
		    bbox = new BoundingBox();
		    length = eventrecords.length;
		    for (n = 0; n < length; n++) {
			record = eventrecords[n];
			if (record.isSelected())
			    bbox.add(record.realx, record.realy);
		    }
		} else bbox = new BoundingBox(eventbbox);
	    }
	} else bbox = null;

	return(bbox);
    }


    private YoixObject
    getEnds(BoundingBox bbox) {

	YoixObject  obj;
	YoixObject  ends;

	if (bbox != null) {
	    obj = YoixObject.newDictionary(2);
	    ends = YoixObject.newArray(2);
	    ends.putDouble(0, bbox.ulx);
	    ends.putDouble(1, bbox.lrx);
	    obj.put(NL_XAXIS, ends, false);
	    ends = YoixObject.newArray(2);
	    ends.putDouble(0, bbox.uly);
	    ends.putDouble(1, bbox.lry);
	    obj.put(NL_YAXIS, ends, false);
	} else obj = YoixObject.newDictionary();

	return(obj);
    }


    private int[]
    getEventIndices() {

	if (eventindices == null)
	    buildEventIndices();
	return(eventindices);
    }


    private Point
    getEventLocation(MouseEvent e) {

	return(getEventLocation(e.getPoint()));
    }


    private Point
    getEventLocation(Point p) {

	if (p != null) {
	    p.x = Math.max(Math.min(p.x - insets.left, viewport.x + viewport.width), viewport.x - 1);
	    p.y = Math.max(Math.min(p.y - insets.top, viewport.y + viewport.height), viewport.y - 1);
	}
	return(p);
    }


    private Color
    getForegroundColor() {

	Color  color;

	return((color = getForeground()) != null ? color : Color.white);
    }


    private synchronized HitBuffer
    getHitBuffer() {

	HitBuffer  hits;

	if (eventrecords != null) {
	    if (datamanager != null)
		hits = datamanager.getHitBuffer(loadeddata);
	    else hits = new HitBuffer(eventrecords.length);
	} else hits = null;

	return(hits);
    }


    private double[]
    getPlotCoordinates(int x, int y) {

	Point2D  point;
	double   coords[] = null;

	//
	// Cleaned up some, but it's still a bit confusing. We eventually
	// will improve things.
	//

	if (plotinverse != null) {
	    switch (anchor) {
		case YOIX_CENTER:
		case YOIX_NONE:
		case YOIX_SOUTH:
		default:
		    point = new Point2D.Double(x, plotzeroy - y);
		    break;

		case YOIX_NORTH:
		    point = new Point2D.Double(x, y - plotzeroy);
		    break;
	    }
	    point =  plotinverse.transform(point, null);
	    coords = new double[] {point.getX(), point.getY()};
	}
	return(coords);
    }


    private String
    getRankPrefix(StackedRecord stackedrecord, String text) {

	if (stackedrecord.rankprefix == null) {
	    if (rankprefix instanceof YoixObject) {
		text = callTipHelper(
		    (YoixObject)rankprefix,
		    stackedrecord.realx,
		    stackedrecord.realy
		);
	    } else if (rankprefix instanceof String)
		text = (String)rankprefix;
	    stackedrecord.rankprefix = text;
	}
	return(stackedrecord.rankprefix);
    }


    private String
    getRankSuffix(StackedRecord stackedrecord, String text) {

	if (stackedrecord.ranksuffix == null) {
	    if (ranksuffix instanceof YoixObject) {
		text = callTipHelper(
		    (YoixObject)ranksuffix,
		    stackedrecord.realx,
		    stackedrecord.realy
		);
	    } else if (ranksuffix instanceof String)
		text = (String)ranksuffix;
	    stackedrecord.ranksuffix = text;
	}
	return(stackedrecord.ranksuffix);
    }


    private int[]
    getSelectedStackEnds() {

	return(getSelectedStackEnds(0, Integer.MAX_VALUE));
    }


    private int[]
    getSelectedStackEnds(int start, int limit) {

	int  ends[] = null;
	int  length;
	int  n;

	//
	// Returns an array that contains the indices that correspond to
	// the first and last selected stackedrecords in the bounds that
	// are described by start and limit, or null if there aren't any
	// selected stacks.
	//
	// NOTE - start is the starting index in stackedrecords[], while
	// limit is an xaxis coordinate.
	//
	// NOTE - caller can assume that every selected stack that lies
	// between the first and last indices in the array that's returned
	// is ready to use (i.e., resetNextRegion() has been called). We
	// eventually should see if we can push this task back into the
	// StackedRecord class, which is where it really belongs.
	//

	if (stackedrecords != null) {
	    length = stackedrecords.length;
	    for (n = start; n < length && stackedrecords[n].getX(limit) <= limit; n++) {
		if (stackedrecords[n].isSelected()) {
		    if (ends == null)
			ends = new int[] {n, n};
		    else ends[1] = n;
		    stackedrecords[n].resetNextRegion();
		}
	    }
	}
	return(ends);
    }


    private double[]
    getSliderEnds(SwingJAxis axis) {

	double  ends[];

	if (axis != null)
	    ends = axis.getSliderEnds();
	else ends = null;

	return(ends != null ? ends : new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
    }


    private int
    getSliderHeight(boolean nonnegative, double ends[]) {

	Point2D  point;
	int      height = 0;

	if (plotmatrix != null) {
	    if (nonnegative) {
		if (ends[1] > 0) {
		    point = plotmatrix.transform(new Point2D.Double(0, ends[1]), null);
        	    height = (int)point.getY();
		} else height = 0;
	    } else {
		if (ends[0] < 0) {
		    point = plotmatrix.transform(new Point2D.Double(0, -ends[0]), null);
		    height = (int)point.getY();
		} else height = 0;
	    }
	}
	return(height);
    }


    final int[]
    getStyleSettings(int style) {

	boolean  stackedmode;
	int      thickness;

	//
	// The initial check of style assumes one of STYLE_ENABLE_EVENTS
	// or STYLE_ENABLE_STACKS is set in plotstyleflags. Also probably
	// wouldn't be hard to pick a better better substitute for style
	// when it's not allowed by plotstyleflags, but it's also not very
	// important because plotstyleflags will usually only be set once
	// and should also control the GUI components that users access.
	//
	// NOTE - the small kludge setting thickness for STYLE_POLYGONS is
	// needed to make sure the left end can be properly erased using
	// the axis slider. You might see problems if you decrease it.
	//

	switch (style) {
	    case STYLE_BARS:
	    case STYLE_POINTS:
	    case STYLE_POLYGONS:
		if ((plotstyleflags & STYLE_ENABLE_EVENTS) == 0)
		    style = STYLE_STACKED_BARS;
		break;

	    case STYLE_STACKED_BARS:
	    case STYLE_STACKED_POINTS:
	    case STYLE_STACKED_POLYGONS:
		if ((plotstyleflags & STYLE_ENABLE_STACKS) == 0)
		    style = STYLE_BARS;
		break;

	    default:
		style = (plotstyleflags & STYLE_ENABLE_EVENTS) != 0 ? STYLE_BARS : STYLE_STACKED_BARS;
		break;
	}

	switch (style) {
	    case STYLE_BARS:
		thickness = (linewidth > 0) ? linewidth : 1;
		stackedmode = false;
		break;

	    case STYLE_POINTS:
		thickness = (pointsize > 0) ? pointsize : 1;
		stackedmode = false;
		break;

	    case STYLE_POLYGONS:
		thickness = 1;
		stackedmode = false;
		break;

	    case STYLE_STACKED_BARS:
		thickness = (linewidth > 0) ? linewidth : 1;
		stackedmode = true;
		break;

	    case STYLE_STACKED_POINTS:
		thickness = (pointsize > 0) ? pointsize : 1;
		stackedmode = true;
		break;

	    case STYLE_STACKED_POLYGONS:
		thickness = 1;
		stackedmode = true;
		break;

	    default:			// unnecessary!!
		style = STYLE_BARS;
		thickness = linewidth;
		stackedmode = false;
		break;
	}
	return(new int[] {style, thickness, stackedmode ? 1 : 0});
    }


    private long
    getTimeZoneOffset(double time) {

	long  offset;

	if (timezone == null)
	    timezone = YoixMiscTime.getDefaultTimeZone();

	offset = timezone.getRawOffset();
	if (timezone.inDaylightTime(new Date((long)(1000.0*time)))) {
	    if (timezone.useDaylightTime())
		offset += 3600000;
	}
	return(offset/1000);
    }


    private String
    getTipPrefix(StackedRecord stackedrecord, String text) {

	if (stackedrecord.tipprefix == null) {
	    if (tipprefix instanceof YoixObject) {
		text = callTipHelper(
		    (YoixObject)tipprefix,
		    stackedrecord.realx,
		    stackedrecord.realy
		);
	    } else if (tipprefix instanceof String)
		text = (String)tipprefix;
	    stackedrecord.tipprefix = text;
	}
	return(stackedrecord.tipprefix);
    }


    private String
    getTipSuffix(StackedRecord stackedrecord, String text) {

	if (stackedrecord.tipsuffix == null) {
	    if (tipsuffix instanceof YoixObject) {
		text = callTipHelper(
		    (YoixObject)tipsuffix,
		    stackedrecord.realx,
		    stackedrecord.realy
		);
	    } else if (tipsuffix instanceof String)
		text = (String)tipsuffix;
	    stackedrecord.tipsuffix = text;
	}
	return(stackedrecord.tipsuffix);
    }


    private synchronized boolean
    isConnected() {

	boolean  result;

	switch (plotstyle) {
	    case STYLE_POINTS:
	    case STYLE_STACKED_POINTS:
		result = (connect != CONNECT_NONE);
		break;

	    case STYLE_POLYGONS:
	    case STYLE_STACKED_POLYGONS:
		result = true;
		break;

	    default:
		result = false;
		break;
	}
	return(result);
    }


    private boolean
    isGenerated() {

	return(gentype > 0);
    }


    private synchronized boolean
    isLoaded() {

	return(loadeddata != null && datarecords != null);
    }


    private synchronized boolean
    isLoadable() {

	return(autoready || isShowing());
    }


    private boolean
    isPeakPixel(int x) {

	return((peakpixels != null && x >= 0 && x < peakpixels.length) ? peakpixels[x] : true);
    }


    private boolean
    isPeakTime(double utime) {

	return(MiscTime.isPeakTime(utime - getTimeZoneOffset(utime), peakdays, peakstart, peakstop, holidays));
    }


    private boolean
    isPolygonShowingAt(int n) {

	boolean  result = false;
	int      length;

	//
	// Returns true if the stackedrecord at n is at the left side of
	// a polygon that would actually be drawn. Needs to agree with
	// the actual polygon drawing code, which makes this harder than
	// you might expect.
	//

	if (stackedrecords != null) {
	     if ((length = stackedrecords.length) > 0) {
		if (n >= 0 && n < length - 1) {
		    if (stackedrecords[n+1].isSelected() == false) {
			if (stackedrecords[n].isSelected()) {
			    for (n += 2; n < length; n++) {
				if (stackedrecords[n].isSelected()) {
				    result = true;
				    break;
				}
			    }
			}
		    } else if (stackedrecords[n].isSelected() == false) {
			if (stackedrecords[n+1].isSelected()) {
			    for (n -= 1; n >= 0; n--) {
				if (stackedrecords[n].isSelected()) {
				    result = true;
				    break;
				}
			    }
			}
		    } else result = true;
		}
	    }
	}
	return(result);
    }


    private synchronized boolean
    isReady() {

	return(autoready || isShowing());
    }


    private boolean
    isShowingOnScreen() {

	Rectangle  rect;
	boolean    result;

	if (isShowing()) {
	    if ((rect = getBounds()) != null)
		result = !rect.isEmpty();
	    else result = false;
	} else result = false;
	return(result);
    }


    private boolean
    isSorted(DataPlot sorter) {

	boolean  result;

	//
	// There's plenty of room for improvement in how we handle the
	// sorting. Partitioning tosses records, which disables sorting
	// and can hurt performance. Suspect there's something we can do
	// when ignorezero or partitioning are enabled, but it probably
	// will have to wait a bit.
	//

	if (datarecords.length == eventrecords.length) {
	    if (sorter != this) {
		if (sorter instanceof SwingJEventPlot) {
		    if (((SwingJEventPlot)sorter).datasorted) {
			if (((SwingJEventPlot)sorter).xindex == xindex) {
			    if (((SwingJEventPlot)sorter).eventrecords != null)
				result = (((SwingJEventPlot)sorter).eventrecords.length == datarecords.length);
			    else result = false;
			} else result = false;
		    } else result = false;
		} else result = false;
	    } else result = datasorted;
	} else result = false;

	return(result);
    }


    private synchronized int
    loadCounters() {

	int  n;

	selectedcount = 0;
	totalcount = 0;

	if (datarecords != null) {
	    totalcount = datarecords.length;
	    if (getAfterUpdate() != null) {
		for (n = 0; n < totalcount; n++)
		    selectedcount += datarecords[n].isSelected() ? 1 : -1;
	    }
	}
	return(totalcount);
    }


    private synchronized boolean
    loadPeakPixels(boolean force) {

	boolean  loaded[];
	double   utime0;
	double   utime1;
	double   days;
	double   slope;
	int      n;

	//
	// We're currently using a constant value to decide if timeshading
	// details can be rendered, but there are other obvious approaches.
	// This one has the advantage that timeshading only depends on the
	// number of days being displayed, which means the decision won't
	// change if the user interactively modifies timeshading settings.
	//

	loaded = peakpixels;

	if (peakpixels == null || force) {
	    peakpixels = null;
	    timeline = null;
	    if (timeshading != 0 && unixtime != null && plotbbox != null) {
		if (viewport.width > 0 && plotbbox.lrx > plotbbox.ulx) {
		    utime0 = toUnixTime(plotbbox.ulx, null, null);
		    utime1 = toUnixTime(plotbbox.lrx, null, null);
		    days = (utime1 - utime0)/86400;
		    if (Double.isNaN(days) == false) {
			peakpixels = new boolean[viewport.width];
			if (PIXELSPERDAY <= 0 || days <= plotwidth/PIXELSPERDAY) {
			    if (spreadmap == null) {
				slope = (utime1 - utime0)/plotwidth;
				timeline = new double[] {slope, utime0 - slope*ipad.left};
			    }
			    for (n = 0; n < peakpixels.length; n++) {
				if (isPeakTime(toUnixTime(n, spreadmap, timeline)))
				    peakpixels[n] = true;
			    }
			}
		    }
		}
	    }
	}
	return(loaded != peakpixels);
    }


    private synchronized void
    loadSpreadMap() {

	double  values[] = null;
	double  scale;
	int     length;
	int     left;
	int     i;
	int     j;
	int     n;

	//
	// Builds the spreadmap[] array using the pixels coordinates that
	// have already been assigned to each record. Values assigned to
	// pixels that land between records are assigned using the values
	// stored in the records to the immediate left and right of that
	// pixel.
	//

	if (spread) {
	    if ((length = eventrecords.length) > 1) {
		for (i = 0, j = 1; j < length; j++) {
		    if (eventrecords[i].x < eventrecords[j].x)
			break;
		}
		if (j < length) {
		    values = new double[viewport.width];
		    scale = (eventrecords[j].realx - eventrecords[i].realx)/(eventrecords[j].x - eventrecords[i].x);
		    left = ipad.left;
		    for (n = 0; n < values.length; n++) {
			if (n > eventrecords[j].x && j < length - 1) {
			    for (; i < length - 2; i++) {
				if (eventrecords[i+1].x >= n) {
				    for (j = i+1; j < length - 1; j++) {
					if (eventrecords[j].x > n) {
					    break;
					}
				    }
				    scale = (eventrecords[j].realx - eventrecords[i].realx)/(eventrecords[j].x - eventrecords[i].x);
				    left = eventrecords[i].x;
				    break;
				}
			    }
			}
			values[n] = scale*(n - left) + eventrecords[i].realx;
		    }
		}
	    }
	}
	spreadmap = values;
    }


    private synchronized boolean
    makePlot() {

	EventRecord  record;
	Point2D      src;
	Point2D      dest;
	boolean      made = false;
	double       xorigin;
	double       yorigin;
	double       dx;
	double       dy;
	double       lastrealx;
	int          lastpixel;
	int          thickness;
	int          length;
	int          right;
	int          left;
	int          selected;
	int          count;
	int          x;
	int          n;

	if (eventrecords != null && plotbbox != null) {
	    if (viewport.width > 0 && viewport.height > 0) {
		if (frozen == false) {
		    switch (anchor) {
			default:
			case YOIX_SOUTH:
			    plotpady = ipad.bottom;
			    plotheight = viewport.height - (ipad.top + ipad.bottom);
			    break;

			case YOIX_NORTH:
			    plotpady = ipad.top;
			    plotheight = viewport.height - (ipad.top + ipad.bottom);
			    break;

			case YOIX_CENTER:
			    plotpady = 0;
			    if (symmetric == false) {
				plotheight = viewport.height - (ipad.top + ipad.bottom);
				plotbbox.lry = Math.max(Math.abs(plotbbox.lry), Math.abs(plotbbox.uly));
				plotbbox.uly = -plotbbox.lry;
			    } else plotheight = (viewport.height - (ipad.top + ipad.bottom))/2;
			    break;

			case YOIX_NONE:
			    plotpady = 0;
			    plotheight = viewport.height - (ipad.top + ipad.bottom);
			    break;
		    }
		    plotpadx = ipad.left;
		    plotwidth = Math.max(viewport.width - linethickness - (ipad.left + ipad.right), linethickness);
		    plotheight = Math.max(plotheight, 0);
		    plotminx = plotpadx;
		    plotmaxx = plotminx + plotwidth;
		    plotmatrix = null;
		    plotinverse = null;
		    dx = plotbbox.lrx - plotbbox.ulx;
		    dy = plotbbox.lry - plotbbox.uly;
		    bucketseparation = Integer.MAX_VALUE;
		    lastpixel = plotminx - 1;
		    lastrealx = 0;
		    if (dx > 0 && dy > 0) {
			xorigin = plotbbox.ulx;
			yorigin = 0;
			plotmatrix = new AffineTransform();
			plotmatrix.setToTranslation(ipad.left, 0);
			plotmatrix.scale(plotwidth/dx, plotheight/dy);
			plotmatrix.translate(-xorigin, -yorigin);
			try {
			    plotinverse = plotmatrix.createInverse();
			}
			catch(NoninvertibleTransformException e) {}

			src = new Point2D.Double();
			dest = new Point2D.Double();

			//
			// Calculate the distance in pixels from the top of this
			// component to a horizontal axis though the origin and
			// save it in plotzeroy.
			//

			switch (anchor) {
			    case YOIX_CENTER:
				if (symmetric)
				    plotzeroy = plotheight + ipad.top;
				else plotzeroy = plotheight/2 + ipad.top;
				break;

			    case YOIX_NONE:
			    case YOIX_SOUTH:
			    default:
				plotmatrix.transform(new Point2D.Double(0, plotbbox.lry), dest);
				plotzeroy = (int)dest.getY() + ipad.top;
				break;

			    case YOIX_NORTH:
				plotmatrix.transform(new Point2D.Double(0, plotbbox.uly), dest);
				plotzeroy = -(int)dest.getY() + ipad.top;
				break;
			}

			thickness = pickThickness();
			length = eventrecords.length;
			selected = 0;

			if (spread) {
			    for (n = 0; n < length; n++) {
				record = eventrecords[n];
				if (record.realx >= plotbbox.ulx) {
				    if (record.realx <= plotbbox.lrx)
					selected++;
				    else break;
				}
			    }
			}

			for (n = 0, left = 0, right = 0, count = 0; n < length; n++) {
			    record = eventrecords[n];
			    if (selected > 1) {
				if (record.realx >= plotbbox.ulx && record.realx <= plotbbox.lrx) {
				    if (count++ > 0)
					record.plotx = plotbbox.ulx + dx*((double)(count-1)/(selected-1));
				    else record.plotx = plotbbox.ulx;
				} else record.plotx = Double.NaN;
			    } else record.plotx = record.realx;
			    src.setLocation(record.plotx, record.realy);
			    plotmatrix.transform(src, dest);
			    x = (int)dest.getX();
			    record.x = x;
			    record.height = Math.abs(dest.getY());

			    //
			    // This is new and only used to help us decide
			    // about highlighting when we're drawing stacked
			    // polygons. Definitely a small kludge, probably
			    // insignificant, that should help maintain plot
			    // coloring consistency. Won't be 100%, but good
			    // enough for now. Trying to deal with fact that
			    // when stroking the outline of a polygon slice
			    // can color pixels in the next region that would
			    // be erased if we also painted that region etc.
			    //

			    if (record.realx != lastrealx && x >= plotminx && lastpixel >= plotminx) {
				if (x - (lastpixel + thickness) < bucketseparation)
				    bucketseparation = x - (lastpixel + thickness);
			    }
			    lastrealx = record.realx;
			    lastpixel = x;

			    //
			    // Remember first event to the right that's not touched when
			    // the current event is painted.
			    //
			    record.right = length;
			    while (x - eventrecords[right].x >= thickness && right < n)
				eventrecords[right++].right = n;

			    //
			    // Remember the first event that paints a pixel that's also
			    // painted by this event.
			    //
			    while (x - eventrecords[left].x >= thickness && left <= n)
				left++;
			    record.left = left;
			}
			loadSpreadMap();
			loadPeakPixels(true);
		    }
		    resetStackRegions();
		    updateMouseMotionListener();
		    thawed = true;
		    made = true;
		} else thawed = false;
	    }
	}

	return(made);
    }


    private boolean
    notGenerated() {

	return(!isGenerated());
    }


    private synchronized void
    partitionUpdate(int index, int mask, double low, double high) {

	HitBuffer  hits;
	int        count = 0;

	if (index == xindex || index == yindex) {
	    if ((hits = getHitBuffer()) != null) {
		if (index == xindex) {
		    if (fastcollector)
			count = collectRecords2(hits, low, high, index, mask);
		    else count = collectRecords(hits, low, high, index, mask);
		} else {
		    if (keeptall) {
			if (eventsstacked) {
			    count = collectStackedRecords(hits, low, eventbbox.lry, index, mask);
			    repaintTallStacks(sliderbottom, low, slidertop, high);
			} else {
			    count = collectRecords(hits, low, eventbbox.lry, index, mask);
			    repaintTallRecords(sliderbottom, low, slidertop, high);
			}
		    } else if (eventsstacked) {
			count = collectStackedRecords(hits, low, high, index, mask);
			repaintTallStacks(sliderbottom, low, slidertop, high);
		    } else count = collectRecords(hits, low, high, index, mask);
		}
		if (datamanager != null) {
		    if (count > 0) {
			datamanager.updateData(loadeddata, hits, count, this);
			Thread.yield();
		    } else releaseHitBuffer(hits);
		} else updatePlot(loadeddata, hits, count, this);
	    }
	}
    }


    private int
    pickConnectWidth() {

	int  width;

	switch (plotstyle) {
	    case STYLE_POINTS:
		width = Math.max(connectwidth, 1);
		break;

	    case STYLE_STACKED_POINTS:
		width = Math.max(connectwidth/2, 1);
		break;

	    default:
		width = 0;
		break;
	}
	return(width);
    }


    private int[]
    pickDelta(boolean lines) {

	int  dx = 0;
	int  dy = 0;

	switch (plotstyle) {
	    case STYLE_BARS:
	    case STYLE_STACKED_BARS:
		break;

	    case STYLE_POINTS:
	    case STYLE_STACKED_POINTS:
		if (lines)
		    dx = linethickness/2;
		else dy = -linethickness/2;
		break;

	    case STYLE_POLYGONS:
	    case STYLE_STACKED_POLYGONS:
		dx = linethickness/2;
		break;
	}

	return(new int[] {dx, dy});
    }


    private int
    pickHorizontalFindStart(Point p) {

	int  start = 0;

	if (stackedrecords != null) {
	    if (laststackfind > 0 && laststackfind < stackedrecords.length) {
		start = laststackfind;
		while (p.x < stackedrecords[start].getX(p.x + 1) && start > 0)
		    start--;
	    }
	}
	return(start);
    }


    private Color
    pickRegionColor(StackedRegion region) {

	StackedRegion  current;
	DataColorer    coloredby;
	Color          color;

	color = region.getColor();

	if ((current = currentregion) != null) {
	    if (current.sliceindex == region.sliceindex) {
		if ((coloredby = datamanager.getColoredBy()) != null) {
		    switch (plotstyle) {
			case STYLE_STACKED_BARS:
			    if (region == current)
				color = ((SwingDataColorer)coloredby).pickPressedColor(color);
			    break;

			case STYLE_STACKED_POINTS:
			    //
			    // Need work elsewhere if we want to draw
			    // connecting lines in a different color.
			    //
			    if (region == current || connect == CONNECT_LINES)
				color = ((SwingDataColorer)coloredby).pickPressedColor(color);
			    break;

			case STYLE_STACKED_POLYGONS:
			    color = ((SwingDataColorer)coloredby).pickPressedColor(color);
			    break;
		    }
		}
	    }
	}
	return(color);
    }


    private Color
    pickSliceColor(int index) {

	StackedRegion  current;
	DataColorer    coloredby;
	Color          color;

	//
	// Only returns null when index is out of bounds, otherwise the
	// caller can safely conclude color is not null.
	//

	if (index >= 0 && index < stackedslices.length) {
	    color = stackedslices[index].color;		// will not be null
	    if ((current = currentregion) != null) {
		if (current.sliceindex == index) {
		    if ((coloredby = datamanager.getColoredBy()) != null)
			color = ((SwingDataColorer)coloredby).pickPressedColor(color);
		}
	    }
	} else color = null;

	return(color);
    }


    private synchronized SweepFilter
    pickSweepFilter(int modifiers) {

	SweepFilter  filters[];
	SweepFilter  filter = null;

	if ((filters = sweepfilters) != null) {
	    if ((modifiers & YOIX_CTRL_MASK) == 0) {
		if ((modifiers & YOIX_SHIFT_MASK) != 0)
		    filter = filters[SHIFT_OP];
		else filter = filters[PLAIN_OP];
	    } else {
		if ((modifiers & YOIX_SHIFT_MASK) != 0)
		    filter = filters[CONTROL_SHIFT_OP];
		else filter = filters[CONTROL_OP];
	    }
	}
	return(filter);
    }


    private int
    pickThickness() {

	int  thickness;

	switch (plotstyle) {
	    case STYLE_POINTS:
	    case STYLE_STACKED_POINTS:
		if (connect != CONNECT_NONE)
		    thickness = Math.max(linethickness, pickConnectWidth());
		else thickness = linethickness;
		break;

	    default:
		thickness = linethickness;
		break;
	}
	return(thickness);
    }


    private StackedRegion
    pickTipRegion(Point p, StackedRegion region) {

	StackedRegion  regions[];
	int            index;
	int            x0;
	int            x1;

	if (plotstyle == STYLE_STACKED_POLYGONS || (plotstyle == STYLE_STACKED_POINTS && isConnected())) {
	    if ((index = region.stackindex) < stackedrecords.length - 1) {
		x0 = stackedrecords[index].getX(Integer.MAX_VALUE);
		x1 = stackedrecords[index+1].getX(Integer.MIN_VALUE);
		if (x1 > x0) {
		    if (p.x >= (x0 + x1)/2) {
			//
			// We really shouldn't be bothered with all this
			// low level stuff - fix it tomorrow!!!!!!!
			//
			if (stackedrecords[index+1].isBuilt()) {
			    stackedrecords[index+1].resetNextRegion();
			    region = stackedrecords[index+1].getRegionDescription(region.sliceindex);
			}
		    }
		}
	    }
	}
	return(region);
    }


    private synchronized void
    releaseHitBuffer(HitBuffer hits) {

	if (datamanager != null)
	    datamanager.releaseHitBuffer(hits);
    }


    private void
    repaintConnectedEvents(int indices[], int count, Graphics g) {

	Rectangle  rect;
	int        delta[];
	int        length;
	int        index;
	int        width;
	int        lastleft;
	int        left;
	int        right;
	int        dx0;
	int        dx1;
	int        x0;
	int        x1;
	int        n;

	//
	// Harder than you might expect, but there's probably still room
	// for improvement. Starts by looking for the left and right ends
	// of the line segments that connect to events in indices[], which
	// are the ones that had their state changed. We erase a vertical
	// slice of the viewport that would contain the line segment that
	// would connect the left and right endpoints and any decorations,
	// like bars or squares, that would be placed at those endpoints.
	// After that we have to make sure we redraw all line segements
	// that touch the area that we erased (remember the lines aren't
	// zero width).
	//
	// NOTE - extra work that tries to make sure the vertical "slice"
	// that we want to draw really does intersect the viewport is a
	// precaution mostly for Java 1.5.0. It's also currently needed
	// because the code that gets called to handle "zooming out" via
	// the mouse wheel may not have called makePlot() by the time the
	// datamanager decides to update us.
	//

	length = eventrecords.length;
	lastleft = 0;
	rect = new Rectangle();

	delta = pickDelta(true);
	dx0 = Math.max((pickConnectWidth() + 1)/2 - delta[0], 0);
	dx1 = Math.max(linethickness, (pickConnectWidth() + 1)/2 + delta[0]);

	for (n = 0; n < count; ) {
	    if ((index = indices[n++]) < length) {
		//
		// First find the events on the left and right with line
		// segments connecting to this event. The calculation of
		// right is complicated because we want to update our
		// loop index (i.e., n) so we deal with as many events
		// as possible right now and don't have to revisit them
		// the next time through the loop. The code probably can
		// be simplified some, but the performance gains really
		// are important so be careful making changes.
		//

		for (left = index - 1; left > lastleft; left--) {
		    if (eventrecords[left].isSelected() || eventrecords[left].x < plotminx)
			break;
		}

		for (right = index + 1; right < length; right++) {
		    if (eventrecords[right].isSelected()) {
			//
			// All connections between left and right will be
			// completely redrawn, so the two nested loops try
			// to eliminate duplication. We first adjust n so
			// it's as close to right as possible (probably no
			// movement here) and then we try to push right and
			// n farther to the right so this pass will draw as
			// much as possible and avoid redrawing stuff in the
			// next pass.
			//
			while (n < count) {
			    if (indices[n] >= right) {
				while (right < length - 1 && n < count && right == indices[n]) {
				    if (eventrecords[right+1].isSelected()) {
					n++;
					right++;
				    } else break;
				}
				break;
			    } else n++;
			}
			//
			// The test was added on 8/2/06 - older code just
			// did the break, but this seems to help apparently
			// without breaking anything.
			//
			if (n >= count || right >= length || eventrecords[right].x + dx1 < eventrecords[indices[n]].x)
			    break;
		    }
		}

		//
		// Force left and right into bounds, but if right isn't in
		// bounds we can assume we won't need another loop because
		// we must have looked through eventrecords[] but we didn't
		// a selected record.
		//

		if (left < 0)
		    left = 0;
		if (right >= length) {
		    right = indices[count - 1];
		    n = count;
		}

		x0 = eventrecords[left].x - dx0;
		x1 = eventrecords[right].x + dx1;
		width = x1 - x0;

		//
		// The extra clipping work here is mostly for Java 1.5.0,
		// which pretty much lets us draw anywhere we want.
		//
		rect.setBounds(x0, 0, width, viewport.height);
		rect = rect.intersection(viewport);
		if (rect.isEmpty() == false) {
		    g.setClip(rect);
		    eraseRect(x0, 0, width, viewport.height, g);

		    //
		    // Next two loops make sure we repaint anything that may
		    // have unintentionally been erased. For example, line
		    // segments that end on the left or right events often
		    // are partly erased (just think of a very steep line),
		    // so we need to make sure those parts are restored.
		    //

		    left = eventrecords[left].left - 1;
		    for (index = left; index >= lastleft; index--) {
			if (eventrecords[index].isSelected()) {
			    if (eventrecords[index].x < x0) {
				left = index;
				break;
			    }
			}
		    }

		    right = eventrecords[right].right;
		    for (index = right; index < length; index++) {
			if (eventrecords[index].isSelected()) {
			    if (eventrecords[index].x > x1) {
				right = index;
				break;
			    }
			}
		    }

		    //
		    // Adding 1 is a small kludge to make sure the event drawing
		    // methods really go far enough. Needed because the second
		    // argument to draw is interpreted a screen coordinate and
		    // the painting loops don't paint records at or past that
		    // limit.
		    //

		    left = Math.max(left, 0);
		    right = Math.min(right, length - 1);
		    draw(left, eventrecords[right].x + 1, g);
		    lastleft = left;
		}
	    } else break;
	}
    }


    private void
    repaintConnectedPlot(int indices[], int count, Graphics g) {

	//
	// A plot of isolated events is handled differently than a plot
	// of stacked events.
	//

	if (eventsstacked)
	    repaintConnectedStacks(indices, count, g);
	else repaintConnectedEvents(indices, count, g);
    }


    private void
    repaintConnectedStacks(int indices[], int count, Graphics g) {

	StackedRecord  stackedrecord;
	Rectangle      rect;
	int            delta[];
	int            length;
	int            index;
	int            width;
	int            lastleft;
	int            left;
	int            right;
	int            dx0;
	int            dx1;
	int            x0;
	int            x1;
	int            n;

	//
	// This is the stack version of the method that coordinates all
	// drawing when stacks are supposed to be connected by lines or
	// filled polygons.
	//
	// NOTE - extra work that tries to make sure the vertical "slice"
	// that we want to draw really does intersect the viewport is a
	// precaution mostly for Java 1.5.0. It's also currently needed
	// because the code that gets called to handle "zooming out" via
	// the mouse wheel may not have called makePlot() by the time the
	// datamanager decides to update us.
	//

	length = eventrecords.length;
	lastleft = 0;
	rect = new Rectangle();

	delta = pickDelta(true);
	dx0 = Math.max((pickConnectWidth() + 1)/2 - delta[0], 0);
	dx1 = Math.max(linethickness, (pickConnectWidth() + 1)/2 + delta[0]);

	for (n = 0; n < count; ) {
	    if ((index = indices[n++]) < length) {
		stackedrecord = stackedrecords[stackedindices[index]];
		left = stackedrecord.left - 1;
		right = stackedrecord.right;

		//
		// First find the stacks on the left and right with line
		// segments connecting to stackedrecord. The calculation
		// of right is complicated because we want to update our
		// loop index (i.e., n) so we work on a different stack
		// the next time through the loop. The code probably can
		// be simplified some, but the performance gains really
		// are important so be careful making changes. The inner
		// loops in the calculation of right are probably the
		// best place to look for improvements.
		//

		for (index = left; index > lastleft; index--) {
		    if (eventrecords[index].isSelected() || eventrecords[index].x < plotminx) {
			left = index;
			break;
		    }
		}

		for (; right < length; right++) {
		    if (eventrecords[right].isSelected()) {
			while (n < count) {
			    if (stackedindices[indices[n]] >= stackedindices[right]) {
				while (stackedindices[right] < stackedrecords.length - 1 && n < count) {
				    if (stackedindices[indices[n]] == stackedindices[right]) {
					if (stackedrecords[stackedindices[right] + 1].isSelected()) {
					    right = stackedrecords[stackedindices[right]].right;
					    while (n < count && stackedindices[indices[n]] < stackedindices[right])
						n++;
					} else break;
				    } else break;
				}
				break;
			    } else n++;
			}
			//
			// The test was added on 8/2/06 - older code just
			// did the break, but this seems to help apparently
			// without breaking anything.
			//
			if (n >= count || right >= length || eventrecords[right].x + dx1 < eventrecords[indices[n]].x)
			    break;
		    }
		}


		//
		// Force left and right into bounds, but if right isn't in
		// bounds we can assume we won't need another loop because
		// we must have looked through eventrecords but didn't find
		// a selected record.
		//

		if (left < 0)
		    left = 0;
		if (right >= length) {
		    right = indices[count - 1];
		    n = count;
		}

		x0 = eventrecords[left].x - dx0;
		x1 = eventrecords[right].x + dx1;
		width = x1 - x0;

		//
		// The extra clipping work here is mostly for Java 1.5.0,
		// which pretty much lets us draw anywhere we want.
		//
		rect.setBounds(x0, 0, width, viewport.height);
		rect = rect.intersection(viewport);
		if (rect.isEmpty() == false) {
		    g.setClip(rect);

		    //
		    // The polygon drawing method includes code that handles
		    // erasing so we skip it here. The main contribution of
		    // that code is to avoid having to completely erase the
		    // rectangle whenever possible. The same approach didn't
		    // work particularly well for connected points, but we
		    // only made a quick try so we probably wll try again.
		    // Performance hits for connected lines probably came
		    // from the stroke to outline conversion and the fact
		    // that the resulting clipping area was complicated.
		    //

		    if (plotstyle != STYLE_STACKED_POLYGONS)
			eraseRect(x0, 0, width, viewport.height, g);

		    //
		    // Next two loops make sure we repaint anything that may
		    // have unintentionally been erased. For example, line
		    // segments that end on the left or right stacks often
		    // are partly erased (just think of a very steep line),
		    // so we need to make sure those parts are restored. In
		    // fact we probably should be able to skip this if we're
		    // letting the polygon code handle erasing, but we had a
		    // few small problems when we tried it - maybe later.
		    //

		    left = eventrecords[left].left - 1;
		    for (index = left; index >= lastleft; index--) {
			if (eventrecords[index].isSelected()) {
			    if (eventrecords[index].x < x0) {
				left = index;
				break;
			    }
			}
		    }

		    right = eventrecords[right].right;
		    for (index = right; index < length; index++) {
			if (eventrecords[index].isSelected()) {
			    if (eventrecords[index].x > x1) {
				right = index;
				break;
			    }
			}
		    }

		    //
		    // Adding 1 is a small kludge to make sure the stack drawing
		    // methods really go far enough. Needed because the second
		    // argument to draw is interpreted a screen coordinate and
		    // the painting loops don't paint records at or past that
		    // limit. Best fix probably would be to hand left and right
		    // to the drawing methods. Might be OK for connected stacks,
		    // but maybe not for other styles.
		    //

		    left = Math.max(left, 0);
		    right = Math.min(right, length - 1);
		    draw(left, eventrecords[right].x + 1, g);
		    lastleft = left;
		}
	    } else break;
	}
    }


    private void
    repaintGeneratedPlot(HitBuffer hits, int count, DataPlot sorter) {

	EventRecord  record;
	Rectangle    rect;
	Graphics     g;
	int          left;
	int          height;
	int          x;
	int          n;

	//
	// NOTE - extra work that tries to make sure the vertical "slice"
	// that we want to draw really does intersect the viewport is a
	// precaution mostly for Java 1.5.0. It's also currently needed
	// because the code that gets called to handle "zooming out" via
	// the mouse wheel may not have called makePlot() by the time the
	// datamanager decides to update us.
	//

	if (count > 0) {
	    if (generatedhits == null || generatedhits.length != eventrecords.length)
		generatedhits = new EventRecord[eventrecords.length];
	    if ((count = collectGeneratedRecords(hits, count)) > 0) {
		if ((g = getSavedGraphics()) == null)
		    g = getGraphics();
		g.translate(insets.left - viewport.x, insets.top - viewport.y);
		height = viewport.height;
		rect = new Rectangle();
		for (n = 0; n < count;) {
		    record = generatedhits[n++];
		    x = record.x;
		    for (; n < count; n++) {
			if (generatedhits[n].x != x)
			    break;
		    }

		    //
		    // The extra clipping work here is mostly for Java 1.5.0,
		    // which pretty much lets us draw anywhere we want.
		    //
		    rect.setBounds(x, 0, linethickness, height);
		    rect = rect.intersection(viewport);
		    if (rect.isEmpty() == false) {
			g.setClip(rect);
			eraseRect(x, 0, linethickness, height, g);
			draw(record.left, x + linethickness, g);
		    }
		}
		g.translate(-insets.left + viewport.x, -insets.top + viewport.y);
		disposeSavedGraphics(g);
	    }
	}
    }


    private void
    repaintStandardPlot(int indices[], int count, Graphics g) {

	EventRecord  record;
	EventRecord  neighbor;
	Rectangle    rect;
	boolean      erase;
	int          length;
	int          index;
	int          width;
	int          height;
	int          x;
	int          n;

	//
	// This is a fast version that can handle most plots because the
	// indices array is sorted so we can march through eventrecords[]
	// in the order that makes painting efficient. It's also safe to
	// assume that we're done painting records in this plot as soon
	// as the indices array yields an index that's greater than the
	// length of eventrecords[] (see buildEventIndices()). Note that
	// this should only occur for specialized plots, like tag plots
	// used in seecalls, that deal with a subset of the loaded data.
	//
	// NOTE - extra work that tries to make sure the vertical "slice"
	// that we want to draw really does intersect the viewport is a
	// precaution mostly for Java 1.5.0. It's also currently needed
	// because the code that gets called to handle "zooming out" via
	// the mouse wheel may not have called makePlot() by the time the
	// datamanager decides to update us.
	//

	height = viewport.height;
	length = eventrecords.length;
	rect = new Rectangle();

	for (n = 0; n < count; ) {
	    if ((index = indices[n++]) < length) {
		record = eventrecords[index];
		x = record.x;
		width = linethickness;
		erase = record.notSelected();
		for (; n < count; n++) {
		    if ((index = indices[n]) < length) {
			neighbor = eventrecords[index];
			if (erase == neighbor.notSelected()) {
			    if (neighbor.x > x) {
				if (record.right > index)
				    width = neighbor.x - x;
				break;
			    }
			} else break;
		    } else break;
		}

		//
		// The extra clipping work here is mostly for Java 1.5.0,
		// which pretty much lets us draw anywhere we want.
		//
		rect.setBounds(x, 0, width, height);
		rect = rect.intersection(viewport);
		if (rect.isEmpty() == false) {
		    g.setClip(rect);
		    if (erase || eventsstacked || keeptall)
			eraseRect(x, 0, width, height, g);
		    draw(record.left, x + width, g);
		}
	    } else break;
	}
    }


    private void
    repaintTallRecords(double oldlow, double newlow, double oldhigh, double newhigh) {

	DataRecord  record;
	HitBuffer   hits;
	double      high;
	double      low;
	int         count;
	int         length;
	int         n;

	if (oldlow != newlow || oldhigh != newhigh) {
	    if ((hits = getHitBuffer()) != null) {
		sliderbottom = newlow;
		slidertop = newhigh;
		high = Math.min(oldhigh, newhigh);
		low = Math.max(oldlow, newlow);
		length = eventrecords.length;
		count = 0;
		for (n = 0; n < length; n++) {
		    if (eventrecords[n].realy > high) {
			record = eventrecords[n].datarecord;
			if (record.isSelected())
			    hits.setRecord(count++, record);
		    } else if (eventrecords[n].realy < low) {
			record = eventrecords[n].datarecord;
			if (record.isSelected())
			    hits.setRecord(count++, record);
		    }
		}
		if (count > 0)
		    updatePlot(loadeddata, hits, count, null);
		releaseHitBuffer(hits);
	    }
	}
    }


    private void
    repaintTallStacks(double oldlow, double newlow, double oldhigh, double newhigh) {

	StackedRecord  stackedrecord;
	EventRecord    record;
	HitBuffer      hits;
	double         high;
	double         low;
	int            count;
	int            length;
	int            n;

	if (oldlow != newlow || oldhigh != newhigh) {
	    if ((hits = getHitBuffer()) != null) {
		sliderbottom = newlow;
		slidertop = newhigh;
		high = Math.min(oldhigh, newhigh);
		low = Math.max(oldlow, newlow);
		length = stackedrecords.length;
		count = 0;
		for (n = 0; n < length; n++) {
		    stackedrecord = stackedrecords[n];
		    if ((record = stackedrecord.getFirstSelectedRecord()) != null) {
			if (stackedrecord.nonnegative) {
			    if (stackedrecord.realy > high)
				hits.setRecord(count++, record.datarecord);
			} else {
			    if (stackedrecord.realy < low)
				hits.setRecord(count++, record.datarecord);
			}
		    }
		}
		if (count > 0)
		    updatePlot(loadeddata, hits, count, null);
		releaseHitBuffer(hits);
	    }
	}
    }


    private synchronized void
    resetStackRegions() {

	int  length;
	int  n;

	currentregion = null;
	if (stackedrecords != null) {
	    length = stackedrecords.length;
	    for (n = 0; n < length; n++)
		stackedrecords[n].resetRegions();
	}
    }


    private synchronized void
    resetStackRegions(HitBuffer hits, int count, boolean sorted) {

	StackedRecord  stackedrecord;
	int            indices[];
	int            index;
	int            right;
	int            n;

	currentregion = null;
	if (eventsstacked && stackedrecords != null) {
	    if (sorted == false)
		indices = hits.getSortedIndices(count, getEventIndices());
	    else indices = hits.getRecordIndices();
	    for (n = 0; n < count; ) {
		if ((index = indices[n++]) < stackedindices.length) {
		    stackedrecord = stackedrecords[stackedindices[index]];
		    stackedrecord.removeRegions();
		    if ((right = stackedrecord.right) < stackedindices.length) {
			while (n < count && indices[n] < stackedindices.length && stackedindices[indices[n]] < stackedindices[right])
			    n++;
		    } else break;
		} else break;
	    }
	}
    }


    private void
    setAxisEnds(SwingJAxis axis) {

	setAxisEnds(axis, 0);
    }


    private void
    setAxisEnds(SwingJAxis axis, int slidermode) {

	double  ends[];

	if (axis != null) {
	    if (axis == xaxis || axis == yaxis) {
		if (plotbbox != null) {
		    if (axis == yaxis) {
			if (slidermode == 2) {
			    if (model != 0 && anchor != YOIX_CENTER)
				axis.setEnds(plotbbox.uly, plotbbox.lry, slidermode);
			    else axis.setEnds(plotbbox.uly, plotbbox.lry, 1);
			} else axis.setEnds(plotbbox.uly, plotbbox.lry, slidermode);
			//
			// This can't be 100% correct because setAxisEnds()
			// lets another thread handle things, which includes
			// setting the slider ends. Eventually need to take a
			// closer look.
			//
			if ((ends = axis.getSliderEnds()) != null) {
			    sliderbottom = ends[0];
			    slidertop = ends[1];
			} else {
			    sliderbottom = Double.NEGATIVE_INFINITY;
			    slidertop = Double.POSITIVE_INFINITY;
			}
		    } else if (axis == xaxis)
			axis.setEnds(plotbbox.ulx, plotbbox.lrx, slidermode);
		} else axis.setAxisEnds();
	    }
	}
    }


    private synchronized void
    setEnds(YoixObject obj, int slidermode) {

	YoixObject  ends;
	YoixObject  low;
	YoixObject  high;
	boolean     reset;
	double      ulx;
	double      uly;
	double      lrx;
	double      lry;
	int         n;

	if (plotbbox != null) {
	    reset = false;
	    if ((ends = obj.getObject(NL_XAXIS)) != null) {
		if (ends.notNull() && ends.isArray()) {
		    n = ends.offset();
		    if ((low = ends.getObject(n++)) != null && low.isNumber()) {
			if ((high = ends.getObject(n)) != null && high.isNumber()) {
			    ulx = low.doubleValue();
			    lrx = high.doubleValue();
			    if (lrx < ulx)
				lrx = ulx;
			    if (ulx != plotbbox.ulx || lrx != plotbbox.lrx) {
				plotbbox.ulx = ulx;
				plotbbox.lrx = lrx;
				setAxisEnds(xaxis, slidermode);
				reset = true;
			    }
			}
		    }
		}
	    }
	    if ((ends = obj.getObject(NL_YAXIS)) != null) {
		if (ends.notNull() && ends.isArray()) {
		    n = ends.offset();
		    if ((low = ends.getObject(n++)) != null && low.isNumber()) {
			if ((high = ends.getObject(n)) != null && high.isNumber()) {
			    uly = low.doubleValue();
			    lry = high.doubleValue();
			    if (anchor != YOIX_CENTER || isGenerated()) {
				if (model == 1) {
				    if (anchor == YOIX_NONE) {
					if (uly > 0)
					    uly = 0;
					if (lry < 0)
					    lry = 0;
				    } else uly = Math.min(0, uly);
				}
				if (lry < uly)
				    lry = uly;
			    } else {
				lry = Math.max(Math.abs(lry), Math.abs(uly));
				uly = -lry;
			    }
			    if (uly != plotbbox.uly || lry != plotbbox.lry) {
				plotbbox.uly = uly;
				plotbbox.lry = lry;
				setAxisEnds(yaxis, slidermode);
				reset = true;
			    }
			}
		    }
		}
	    }
	    if (reset)
		reset(true);
	}
    }


    private void
    setLineThickness(int thickness) {

	//
	// Some low level code might implicilty assume that linethickness
	// is always greater than 0, so we enforce it here. Really not an
	// issue that needs to be dealt with elsewhere.
	//

	linethickness = Math.max(thickness, 1);
	if (xaxis != null)
	    xaxis.setPlotLineWidth(linethickness);
    }


    private void
    setLoadedEnds() {

	YoixObject obj;
	YoixObject ends;
	YoixObject loaded;

	//
	// The code that looks through NL_LOADEDENDS was designed to give
	// tag plots precise control over their yaxis, independent of the
	// data that was acutually loaded. The xaxis control is currently
	// not used and none of it is needed for normal plots.
	//

	if (loadedbbox != null) {
	    obj = getEnds(loadedbbox);
	    if ((loaded = getData().getObject(NL_LOADEDENDS)) != null && loaded.notNull()) {
		if ((ends = loaded.getObject(NL_XAXIS)) != null) {
		    if (ends.notNull() && ends.isArray())
			obj.put(NL_XAXIS, ends, false);
		}
		if ((ends = loaded.getObject(NL_YAXIS)) != null) {
		    if (ends.notNull() && ends.isArray())
			obj.put(NL_YAXIS, ends, false);
		}
	    }
	    setPlotEnds(obj);
	}
    }


    private synchronized void
    setPartitionedPlots() {

	partitionedplots = null;

	if (datamanager != null) {
	    if (partitioned && xaxis != null)
		partitionedplots = datamanager.getPartitionedPlots(xindex, partitionindex);
	}
    }


    private synchronized void
    setPlotBBox(BoundingBox bbox) {

	//
	// Currently should only be called from buildPlot(), however some
	// of the code that handles the yaxis based on anchor and model
	// could also be needed elsewhere (e.g., in handleAutoFit()).
	//

	plotbbox = new BoundingBox(bbox);
	plotbbox.ulx = Math.floor(plotbbox.ulx);
	plotbbox.lrx = Math.max(Math.ceil(plotbbox.lrx), plotbbox.ulx + 1);

	switch (anchor) {
	    case YOIX_NONE:
		plotbbox.uly = Math.floor(plotbbox.uly);
		plotbbox.lry = Math.ceil(plotbbox.lry);
		break;

	    default:
		if (anchor != YOIX_CENTER || isGenerated()) {
		    plotbbox.uly = Math.floor(plotbbox.uly);
		    if (model == 1)
			plotbbox.uly = Math.min(0, plotbbox.uly);
		    plotbbox.lry = Math.max(Math.ceil(plotbbox.lry), plotbbox.uly + 1);
		} else {
		    plotbbox.lry = Math.max(Math.abs(plotbbox.lry), Math.abs(plotbbox.uly));
		    plotbbox.uly = -plotbbox.lry;
		}
		break;
	}
    }


    private void
    setTipText(String text) {

	if (tipmanager.isEnabled())
	    tipmanager.setText(text);
    }


    private void
    sweepBegin(Point p) {

	xcorner[0] = p.x;
	ycorner[0] = p.y;
	xcorner[2] = xcorner[0];
	ycorner[2] = ycorner[0];
    }


    private void
    sweepDragged(Point p) {

	drawSweepBox();		// erase the last one

	xcorner[1] = xcorner[0];
	ycorner[1] = p.y;
	xcorner[2] = p.x;
	ycorner[2] = ycorner[1];
	xcorner[3] = xcorner[2];
	ycorner[3] = ycorner[0];

	drawSweepBox();
    }


    private void
    sweepEnd(MouseEvent e) {

	BoundingBox  bbox;
	DataRecord   records[];
	YoixObject   bboxends;
	SweepFilter  filter;
	HitBuffer    hits;
	boolean      hasaction;
	boolean      hasitem;
	BitSet       accumulated;
	double       coords[];
	int          count;
	int          start;

	//
	// Using HitBuffer is just a convenience - no records have their
	// state changed and collected records aren't sent back to the
	// DataManager.
	//

	bbox = new BoundingBox();
	if ((coords = getPlotCoordinates(xcorner[0], ycorner[0])) != null) {
	    bbox.add(coords[0], coords[1]);
	    if ((coords = getPlotCoordinates(xcorner[2], ycorner[2])) != null) {
		filter = pickSweepFilter(e.getModifiers());
		hasaction = hasActionListener();
		hasitem = hasItemListener();
		bbox.add(coords[0], coords[1]);
		bboxends = getEnds(hasitem ? bbox : null);
		if (eventsstacked) {
		    if (bbox.covers(coords[0], slidertop))
			bbox.add(coords[0], stackedbbox.lry);
		    if (bbox.covers(coords[0], sliderbottom))
			bbox.add(coords[0], stackedbbox.uly);
		} else if (keeptall) {
		    if (bbox.covers(coords[0], Math.min(slidertop, plotbbox.lry)))
			bbox.add(coords[0], eventbbox.lry);
		}
		if (filter != null || hasaction || hasitem) {
		    if ((hits = getHitBuffer()) != null) {
			accumulated = new BitSet(0);
			start = (filter != null) ? filter.getAccumulatedRecords(hits, accumulated) : 0;
			if ((count = collectSweep(hits, start, bbox, accumulated)) > start) {
			    if (filter != null || hasaction) {
				records = hits.copyRecords(count);
				if (filter != null)
				    filter.loadRecords(loadeddata, records, false);
				if (hasaction) {
				    postActionEvent(
					datamanager.getSelected(records, count, accumulated),
					e.getModifiers(),
					true
				    );
				}
			    }
			}
			releaseHitBuffer(hits);
			if (hasitem)
			    postItemEvent(bboxends, count > 0, true);
		    }
		}
	    }
	}

	drawSweepBox();		// erase the last one
    }


    private synchronized double
    toUnixTime(double x, double map[], double line[]) {

	YoixObject  obj;
	double      utime = Double.NaN;
	double      delta;

	if (unixtime != null) {
	    if (map != null) {
		//
		// Not certain what should happen when the index is out
		// of bounds, so we just force it in for now.
		//
		if (x < 0)
		    x = map[0];
		else if (x >= map.length)
		    x = map[map.length - 1];
		else x = map[(int)x];
	    }
	    if (line == null) {
		if (unixtime.isNumber()) {
		    utime = x;
		    if ((delta = unixtime.intValue()) > 1 || delta < -1)
			utime += delta;
		} else {
		    obj = call(unixtime, YoixObject.newDouble(x));
		    if (obj.isNumber())
			utime = obj.doubleValue();
		}
	    } else utime = x*line[0] + line[1];
	}
	return(utime);
    }


    private boolean
    updateCounterData(EventRecord record, double incr) {

	boolean  collect;
	double   value;
	Color    color;

	//
	// Updates record and returns true when things changed enough
	// that the record needs to be redrawn. Implies that collected
	// records may not be sorted after collection, so the update
	// can't make any sorting assumptions. Seems like a reasonable
	// approach, particularly when we're managing lots of records.
	//
	// NOTE - there's only a subtle difference between this method
	// and updateOverlapData(), so we eventually should look into
	// combining them. The counter generator is a recent addtion
	// (10/28/04) and we didn't have time to spend on a thorough
	// test of a combined version - later.
	//

	value = record.realy;
	record.updateRecordValue(value + incr);
	collect = (value <= plotbbox.lry) || (record.realy <= plotbbox.lry);

	if (currentpalette != null) {
	    if ((color = currentpalette.selectColor((int)record.realy, null)) != null) {
		if (color != record.color && color.equals(record.color) == false) {
		    record.color = color;
		    collect = true;
		}
	    }
	}

	return(collect);
    }


    private void
    updateCounters(HitBuffer hits, int count) {

	int  n;

	if (getAfterUpdate() != null) {
	    for (n = 0; n < count; n++) {
		if (hits.isSelected(n))
		    selectedcount++;
		else selectedcount--;
	    }
	}
    }


    private synchronized void
    updateCurrentRegion(Point point) {

	StackedRegion  region;
	StackedRegion  tipregion;

	//
	// We currently only support tips and highlighting over stacks.
	// Also notice that we add insets in before tell we tipmanager
	// about the new lock point, which is supposed to account for
	// the translation that's automatically handled by the methods
	// (e.g., updatePlot()) that control drawing. A bit confusing,
	// but necessary if we want the lock point to match what's done
	// by the low level drawing code.
	//

	if (eventsstacked) {
	    if ((region = findRegionAt(point)) != null) {
		if (tipmanager.isEnabled()) {
		    tipregion = pickTipRegion(point, region);
		    tipmanager.setTipLockPoint(
			(int)(tipregion.x + insets.left + 0.5),
			(int)(tipregion.y + insets.top + 0.5)
		    );
		    setTipText(tipregion.getTipText(tipflags, true));
		}
	    } else setTipText(null);
	    if (bucketseparation > 1)
	        drawRegion(region);
	}
    }


    private synchronized void
    updateMouseMotionListener() {

	//
	// Need to make sure we only add ourselves once!!
	//

	if (mouse != AVAILABLE || eventsstacked) {
	    if (motionlisteners == 0) {
		motionlisteners++;
		addMouseMotionListener(this);
	    }
	} else {
	    if (motionlisteners > 0) {
		motionlisteners--;
		removeMouseMotionListener(this);
	    }
	}
    }


    private boolean
    updateOverlapData(EventRecord record, double incr) {

	boolean  collect;
	double   value;
	Color    color;

	//
	// Updates record and returns true when things changed enough
	// that the record needs to be redrawn. Implies that collected
	// records may not be sorted after collection, so the update
	// can't make any sorting assumptions. Seems like a reasonable
	// approach, particularly when we're managing lots of records.
	//
	// NOTE - there's only a subtle difference between this method
	// and updateCounterData(), so we eventually should look into
	// combining them. The overlap generator has been around for
	// a long time and is used by several important applications,
	// so we decided to keep them separate until we had time for
	// a thorough test.
	//

	value = record.realy;
	record.updateRecordValue(value + incr);
	collect = (value <= plotbbox.lry) || (record.realy <= plotbbox.lry);

	if (currentpalette != null) {
	    color = currentpalette.selectColor(
		(int)record.realy,
		(int)Math.ceil(plotbbox.lry - plotbbox.uly) + 1,
		record.datarecord.getColor()
	    );
	    if (color != record.color && color.equals(record.color) == false) {
		record.color = color;
		collect = true;
	    }
	}

	return(collect);
    }


    private void
    updatePartitionedPlots(SwingJAxis axis, double low, double high) {

	SwingJEventPlot  plot;
	int              n;

	if (partitionedplots != null) {
	    if (axis == xaxis) {
		for (n = 0; n < partitionedplots.length; n++) {
		    if (partitionedplots[n] instanceof SwingJEventPlot) {
			plot = (SwingJEventPlot)partitionedplots[n];
			if (plot.xmask == 0 && plot.xindex == xindex)
			    plot.partitionUpdate(xindex, xmask, low, high);
		    }
		}
	    } else if (axis == yaxis) {
		for (n = 0; n < partitionedplots.length; n++) {
		    if (partitionedplots[n] instanceof SwingJEventPlot) {
			plot = (SwingJEventPlot)partitionedplots[n];
			if (plot.ymask == 0 && plot.yindex == yindex)
			    plot.partitionUpdate(yindex, ymask, low, high);
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

    class EventRecord

	implements YoixInterfaceSortable

    {

	DataRecord  datarecord;
	boolean     selected;
	boolean     nonnegative;
	double      realx;
	double      realy;
	double      datax;
	double      plotx;
	double      height;
	Color       color;
	int         index;
	int         x;
	int         left;
	int         right;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	EventRecord(DataRecord record, BoundingBox bbox) {

	    datarecord = record;
	    selected = false;
	    realx = datarecord.getValue(xindex);
	    realy = datarecord.getValue(yindex);
	    index = datarecord.getIndex();
	    datax = realx;
	    nonnegative = (realy >= 0);
	    bbox.add(realx, realy);
	}

	///////////////////////////////////
	//
	// YoixInterfaceSortable Methods
	//
	///////////////////////////////////

	public final int
	compare(YoixInterfaceSortable element, int flag) {

	    EventRecord  record;
	    DataColorer  coloredby;
	    int          result;

	    //
	    // Returns negative if element should precede this record, 0 if
	    // they can be considered equal, and positive if this record is
	    // smaller than element.
	    //
	    // Sorting by realy is primarily used by the stack related code
	    // that needs to separate events have the same x-coordinate into
	    // two groups of positive and nonnegative values. Sorting events
	    // by their decreasing height can mean a little less work when
	    // we draw events as vertical bars.
	    //

	    record = (EventRecord)element;

	    if (realx == record.realx) {
		if (flag != VL_SORT_COLOR) {
		    if (realy != record.realy) {
			if (nonnegative == record.nonnegative) {
			    if (nonnegative)
				result = (realy > record.realy) ? -1 : 1;	// tallest first
			    else result = (realy < record.realy) ? -1 : 1;	// tallest first
			} else result = (realy > record.realy) ? -1 : 1;	// tallest first
		    } else result = datarecord.getID() - record.datarecord.getID();
		} else {
		    if ((coloredby = datamanager.getColoredBy()) != null) {
			if ((result = coloredby.compare(record.datarecord, datarecord)) == 0)
			    result = datarecord.getID() - record.datarecord.getID();
		    } else result = datarecord.getID() - record.datarecord.getID();
		}
	    } else result = (realx > record.realx) ? 1 : -1;

	    return(result);
	}

	///////////////////////////////////
	//
	// EventRecord Methods
	//
	///////////////////////////////////

	final boolean
	isSelected() {

	    return(datarecord.isSelected());
	}


	final boolean
	notSelected() {

	    return(datarecord.notSelected());
	}


	final void
	updateRecordValue(double value) {

	    Point2D  point;

	    realy = value;
	    if (plotmatrix != null) {
		point = plotmatrix.deltaTransform(new Point.Double(0, realy - plotbbox.uly), null);
		height = point.getY();
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class StackedRecord

    {

	StackedRegion  bottomregion;
	StackedRegion  topregion;
	EventRecord    events[];
	boolean        built;
	boolean        nonnegative;
	double         realx;
	double         realy;
	double         miny;
	double         maxy;
	String         rankprefix;
	String         ranksuffix;
	String         tipprefix;
	String         tipsuffix;
	int            stackindex;
	int            lastevent;
	int            changes;
	int            left;
	int            right;

	//
	// Right now nextregion is only used by methods that draw polygons.
	// It's value MUST be officially reset at the start of any polygon
	// operation, which should be synchronized so other threads don't
	// interfere. Requirements are met by our current implementation,
	// but be careful if you make changes or try to expand the use of
	// nextregion!!!
	//

	StackedRegion  nextregion;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	StackedRecord(EventRecord records[], int stackindex, int eventindex, BoundingBox bbox) {

	    double  y;

	    //
	    // We assume here that records[] has been sorted by realx into
	    // groups that have negative and nonnegative y values. In other
	    // words, we can stop collecting records when realx or the sign
	    // of the records changes.
	    //

	    this.stackindex = stackindex;
	    left = eventindex;
	    realx = records[eventindex].realx;
	    miny = records[eventindex].realy;
	    maxy = records[eventindex].realy;
	    nonnegative = records[eventindex].nonnegative;
	    rankprefix = null;
	    changes = -1;
	    lastevent = 0;
	    built = false;

	    for (right = left + 1; right < records.length; right++) {
		if (records[right].realx == realx && records[right].nonnegative == nonnegative) {
		    y = records[right].realy;
		    if (nonnegative) {
			maxy += y;
			if (y < miny)
			    miny = y;
		    } else {
			miny += y;
			if (y > maxy)
			    maxy = y;
		    }
		} else break;
	    }

	    realy = maxy;
	    bbox.add(realx, maxy);
	    bbox.add(realx, miny);

	    events = new EventRecord[right - left];
	    System.arraycopy(records, left, events, 0, events.length);
	}

	///////////////////////////////////
	//
	// StackedRecord Methods
	//
	///////////////////////////////////

	final synchronized StackedRegion
	getBottomRegion() {

	    if (built == false)
		buildRegions();
	    return(bottomregion);
	}


	final synchronized int
	getBottomSliceIndex() {

	    StackedRegion  region;

	    return((region = getBottomRegion()) != null ? region.sliceindex : 0);
	}


	final int
	getCount() {

	    return(events.length);
	}


	final synchronized ArrayList
	getCoveredRecords(BoundingBox bbox) {

	    StackedRegion  region;
	    ArrayList      list;
	    double         y;
	    int            length;
	    int            first;
	    int            n;

	    list = new ArrayList();

	    if (bbox.containsX(realx)) {
		y = 0;
		for (region = bottomregion; region != null; region = region.up) {
		    if (region.eventindex >= 0) {
			y += region.realy;
			if (bbox.containsY(y)) {
			    length = (region != topregion) ? region.up.eventindex : events.length;
			    for (n = region.eventindex; n < length; n++)
				list.add(events[n].datarecord);
			}
		    }
		}
	    }
	    return(list);
	}


	final ArrayList
	getDataRecords(StackedRegion region) {

	    ArrayList  list;
	    int        length;
	    int        n;

	    if (region != null && region.eventindex >= 0) {
		list = new ArrayList();
		length = (region != topregion && region.up != null) ? region.up.eventindex : events.length;
		for (n = region.eventindex; n < length; n++) {
		    if (events[n].isSelected())
			list.add(events[n].datarecord);
		}
	    } else list = null;
	    return(list);
	}


	final EventRecord
	getEventRecord(int index) {

	    return(index >= 0 && index < events.length ? events[index] : null);
	}


	final EventRecord
	getFirstSelectedRecord() {

	    EventRecord  record = null;
	    int          n;

	    for (n = 0; n < events.length; n++) {
		if (events[n].isSelected()) {
		    record = events[n];
		    break;
		}
	    }
	    return(record);
	}


	final synchronized StackedRegion
	getRegion(int index) {

	    StackedRegion  target = null;
	    StackedRegion  region;

	    for (region = nextregion; region != null; region = region.up) {
		if (region.sliceindex == index) {
		    target = region;
		    break;
		}
	    }
	    return(target);
	}


	final synchronized float
	getRegionBottom(int index) {

	    StackedRegion  region;
	    float          y = plotzeroy;

	    //
	    // Currently assumes regions are all properly built, which is
	    // prefectly reasonable considering the fact that this method
	    // should only be called from the polygon or find methods.
	    //
	    // NOTE - probably can eliminate anchor - later.
	    //

	    for (region = nextregion; region != null; region = region.up) {
		if (region == topregion) {
		    if (region.sliceindex >= index)
			y = region.flipped ? region.y : region.y + region.height;
		    else y = region.flipped ? region.y + region.height : region.y;
		    nextregion = region;
		    break;
		} else if (region.sliceindex >= index) {
		    y = region.flipped ? region.y : region.y + region.height;
		    nextregion = region;
		    break;
		}
	    }
	    return(y);
	}


	final synchronized StackedRegion
	getRegionDescription(int index) {

	    StackedRegion  region = null;

	    if (nextregion != null) {
		for (region = nextregion; region != null; region = region.up) {
		    if (region == topregion) {
			if (region.sliceindex != index) {
			    region = new StackedRegion(stackindex, index, region.x, region.flipped);
			    region.y = getRegionBottom(index);
			}
			break;
		    } else if (region.sliceindex >= index) {
			if (region.sliceindex != index) {
			    region = new StackedRegion(stackindex, index, region.x, region.flipped);
			    region.y = getRegionBottom(index);
			}
			break;
		    }
		}
	    } else if (events.length > 0)
		region = new StackedRegion(stackindex, index, getRegionX(), isFlipped());
	    return(region);
	}


	final synchronized float
	getRegionHeight(int index) {

	    StackedRegion  region;
	    float          height = 0;

	    //
	    // Currently assumes regions are all properly built, which is
	    // prefectly reasonable considering the fact that this method
	    // should only be called from the polygon or find methods.
	    //
	    // NOTE - probably can eliminate anchor - later.
	    //

	    for (region = nextregion; region != null; region = region.up) {
		if (region == topregion) {
		    height = (region.sliceindex == index) ? region.height : 0;
		    nextregion = region;
		    break;
		} else if (region.sliceindex >= index) {
		    height = (region.sliceindex == index) ? region.height : 0;
		    nextregion = region;
		    break;
		}
	    }
	    return(height);
	}


	final synchronized float
	getRegionTop(int index) {

	    StackedRegion  region;
	    float          y = plotzeroy;

	    //
	    // Currently assumes regions are all properly built, which is
	    // prefectly reasonable considering the fact that this method
	    // should only be called from the polygon or find methods.
	    //

	    for (region = nextregion; region != null; region = region.up) {
		if (region == topregion) {
		    if (region.sliceindex > index)
			y = region.flipped ? region.y : region.y + region.height;
		    else y = region.flipped ? region.y + region.height : region.y;
		    nextregion = region;
		    break;
		} else if (region.sliceindex == index) {
		    y = region.flipped ? region.y + region.height : region.y;
		    nextregion = region;
		    break;
		} else if (region.sliceindex > index) {
		    y = region.flipped ? region.y : region.y + region.height;
		    nextregion = region;
		    break;
		}
	    }
	    return(y);
	}


	final synchronized float
	getRegionX() {

	    //
	    // Pretty much the same answer as getX() except that it's a
	    // float and NaN is returned if there's no answer. Only used
	    // when drawing polygon plots, and if we ever do return NaN,
	    // which should never happen, we'll end up with messy polygon
	    // plots.
	    //

	    return(events.length > 0 ? events[0].x : Float.NaN);
	}


	final synchronized StackedRegion
	getTopRegion() {

	    if (built == false)
		buildRegions();
	    return(topregion);
	}


	final synchronized int
	getTopSliceIndex() {

	    StackedRegion  region;

	    return((region = getTopRegion()) != null ? region.sliceindex : 0);
	}


	final int
	getX(int fail) {

	    return(events.length > 0 ? events[0].x : fail);
	}


	final boolean
	isBuilt() {

	    if (built == false)
		buildRegions();
	    return(built);
	}


	final boolean
	isFlipped() {

	    return((anchor == YOIX_NORTH) ? nonnegative : !nonnegative);
	}


	final boolean
	isSelected() {

	    boolean  result = false;
	    int      length;
	    int      count;
	    int      n;

	    //
	    // Remembering the index of the selected event that we found
	    // last time should help make this a bit faster. Old version
	    // always started at 0, but we're now using this method more
	    // than we used to, mostly to eliminate unnecessay calls to
	    // buildRegions(), so we decided to work on this method too.
	    //

	    if (events != null) {
		length = events.length;
		for (n = lastevent, count = length; count > 0; count--) {
		    if (events[n].isSelected()) {
			lastevent = n;
			result = true;
			break;
		    }
		    n = (n + 1)%length;
		}
	    }
	    return(result);
	}


	final synchronized void
	removeRegions() {

	    resetRegions();	// eventually eliminate this call!!!
	}


	final synchronized void
	resetNextRegion() {

	    //
	    // Unfortunately we pushed the responsibility for resetting
	    // nextregion (i.e., calling this method) out of this class,
	    // but it probably would be better if we automatically did
	    // it elsewhere (e.g., in getBottomRegion()). That currently
	    // would handle almost everything, but there's at least one
	    // place that isn't covered. We will revisit this next time.
	    //

	    if (built == false)
		buildRegions();
	    nextregion = bottomregion;
	}


	final synchronized void
	resetRegions() {

	    built = false;
	    bottomregion = null;
	    topregion = null;
	    nextregion = null;
	}


	final void
	setY(double y) {

	    realy = y;
	}


	final void
	sortStack(int changes) {

	    //
	    // We use changes to help eliminate unnecessary sorting that
	    // occured whenever the plot switched to stacking mode. It's
	    // a big improvement that doesn't quite catch everything, but
	    // what's missed probably isn't all that common.
	    //

	    if (eventsstacked) {
		if (this.changes != changes) {
		    YoixMiscQsort.sort(events, VL_SORT_COLOR);
		    this.changes = changes;
		}
	    }
	}


	public final String
	toString() {

	    DataColorer  coloredby;
	    String       str = "";
	    int          n;

	    if (events != null) {
		if ((coloredby = datamanager.getColoredBy()) != null) {
		    for (n = 0; n < events.length; n++)
			str += coloredby.getName(events[n].datarecord) + "\n";
		}
	    }
	    return(str);
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	private synchronized void
	buildRegions() {

	    StackedRegion  region;
	    StackedRegion  bottomregion;
	    StackedRegion  topregion;
	    DataColorer    coloredby;
	    EventRecord    record;
	    boolean        flipped;
	    String         recordname;
	    double         recordheight;
	    double         regionheight;
	    double         stackedheight;
	    double         stackrealy;
	    double         stackpady;
	    double         ends[];
	    int            sliderheight;
	    int            eventindex;
	    int            sliceindex;
	    int            length;
	    int            m;
	    int            n;
	    int            x;
	    int            y;

	    //
	    // A really important method that's called a lot when the plot
	    // is stacked.
	    //

	    if (events != null && events.length > 0) {
		if ((coloredby = datamanager.getColoredBy()) != null) {
		    if (stackedslices != null && stackedregions != null) {
			x = events[0].x;
			flipped = isFlipped();
			stackedheight = 0;
			stackrealy = 0;
			stackpady = (plotstyle == STYLE_STACKED_BARS) ? plotpady : 0;
			sliceindex = 1;
			eventindex = 0;

			length = stackedregions.length;
			for (n = 0; n < length; n++)
			    stackedregions[n].reset(stackindex, n, x, flipped);

			ends = getSliderEnds(yaxis);
			sliderheight = getSliderHeight(nonnegative, ends);

			for (; (record = getEventRecord(eventindex)) != null; eventindex++) {
			    recordname = coloredby.getName(record.datarecord);
			    sliceindex = pickRegion(recordname, stackedregions, sliceindex);
			    region = stackedregions[sliceindex];
			    if (region.eventindex < 0)
				region.eventindex = eventindex;
			    region.totaly += record.realy;
			    if (record.isSelected()) {
				region.realy += record.realy;
				stackrealy += record.realy;
				recordheight = record.height;
				if (recordheight + stackedheight >= sliderheight) {
				    if (stackedheight >= 0)
					recordheight = sliderheight - stackedheight;
				}
				switch (anchor) {
				    case YOIX_NORTH:
					if (region.started) {
					    region.height += recordheight;
					} else {
					    if (nonnegative) {
						region.y = (float)(plotzeroy - stackpady + stackedheight);
						region.height += recordheight + stackpady;
					    } else {
						region.y = (float)(plotzeroy - recordheight);
						region.height += recordheight;
					    }
					}
					break;

				    default:
					if (region.started) {
					    if (nonnegative) {
						region.y -= recordheight;
						region.height += recordheight;
					    } else region.height += recordheight;
					} else {
					    if (nonnegative) {
						region.y = (float)(plotzeroy - recordheight - stackedheight);
						region.height = (float)(recordheight + stackpady);
					    } else {
						region.y = (float)(plotzeroy - stackedheight);
						region.height = (float)(recordheight + stackpady);
					    }
					}
					break;
				}
				stackedheight += recordheight;
				stackpady = 0;
				region.started = true;
			    }
			}
			bottomregion = null;
			topregion = null;
			length = stackedregions.length;
			for (n = 0; n < length; n++) {
			    if (stackedregions[n].started) {
				region = (StackedRegion)stackedregions[n].clone();
				if (bottomregion == null) {
				    bottomregion = region;
				    topregion = region;
				    topregion.up = null;
				} else {
				    topregion.up = region;
				    topregion = region;
				    topregion.up = null;
				}
			    }
			}
			this.realy = stackrealy;
	    		this.bottomregion = bottomregion;
	    		this.topregion = topregion;
	    		this.nextregion = bottomregion;
			built = true;
		    }
		}
	    }
	}


	private int
	pickRegion(String name, StackedRegion regions[], int n) {

	    int  index = 0;
	    int  count;

	    if (name != null) {
		for (count = regions.length; count > 0; count--) {
		    if (name.equals(regions[n].getName())) {
			index = n;
			break;
		    }
		    n = (n + 1)%regions.length;
		    if (n == 0)
			n = 1;
		}
	    }
	    return(index);
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class StackedRegion

	implements Cloneable

    {

	//
	// These are organized in linked lists that's are associated with
	// every StackedRecord. At one point the lists were circular, but
	// they now end with a null pointer.
	//

	StackedRegion  up = null;
	boolean        flipped;
	boolean        started = false;
	double         realy = 0;
	double         totaly = 0;		// currently only for tip text
	float          height = 0;
	float          x;
	float          y = plotzeroy;
	int            eventindex = -1;
	int            sliceindex = -1;
	int            stackindex = -1;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	StackedRegion() {

	}


	StackedRegion(int stackindex, int sliceindex, float x, boolean flipped) {

	    this.stackindex = stackindex;
	    this.sliceindex = sliceindex;
	    this.x = x;
	    this.flipped = flipped;
	}

	///////////////////////////////////
	//
	// Cloneable Methods
	//
	///////////////////////////////////

	public final Object
	clone() {

	    StackedRegion  obj = null;

	    try {
		obj = (StackedRegion)super.clone();
		obj.up = null;
	    }
	    catch(CloneNotSupportedException e) {}

	    return(obj);
	}

	///////////////////////////////////
	//
	// StackedRegion Methods
	//
	///////////////////////////////////

	final synchronized Color
	getColor() {

	    return(stackedslices != null ? stackedslices[sliceindex].color : getForeground());
	}


	final synchronized String
	getName() {

	    return(stackedslices != null ? stackedslices[sliceindex].name : "");
	}


	synchronized StackedRecord
	getOwner() {

	    return(stackedrecords != null && stackindex < stackedrecords.length
		? stackedrecords[stackindex]
		: null
	    );
	}


	final String
	getTipText(int flags, boolean html) {

	    StackedRecord  owner;
	    StringBuffer   buf;
	    ArrayList      list;
	    boolean        showvalue;
	    boolean        showrank;
	    boolean        showties;
	    String         name;
	    String         text;
	    int            n;

	    name = getName();
	    if ((text = (sliceindex > 0 || started) ? name : null) != null) {
		buf = new StringBuffer();
		owner = getOwner();
		if (html) {
		    buf.append("<html>&nbsp;");
		    if (owner != null)
			buf.append(getTipPrefix(owner, ""));
		    if (name.indexOf('\n') >= 0) {
			list = YoixMisc.split(name, "\n");
			for (n = 0; n < list.size(); n++) {
			    if (n > 0)
				buf.append("<p>&nbsp;");
			    buf.append(YoixMisc.htmlFromAscii((String)list.get(n)));
			}
		    } else buf.append(YoixMisc.htmlFromAscii(name));
		} else buf.append(name);
		showvalue = ((flags&(TIP_SHOW_VALUE|TIP_SHOW_COUNT)) != 0);
		showrank = ((flags&TIP_SHOW_RANK) != 0);
		showties = ((flags&TIP_SHOW_TIES) != 0);
		if (showvalue || showrank) {
		    if (showvalue) {
			if (name.length() > 0)
			    buf.append(" (");
			else buf.append("(");
			if (realy != 0) {
			    if (realy == (int)realy)
				buf.append((int)realy);
			    else buf.append(YoixMiscPrintf.strfmt("%.1f", realy));
			    buf.append(" of ");
			    if (totaly == (int)totaly)
				buf.append((int)totaly);
			    else buf.append(YoixMiscPrintf.strfmt("%.1f", totaly));
			} else buf.append("0 of 0");
			buf.append(")");
		    }
		    if (showrank)
			addRank(buf, showties);
		}
		if (html) {
		    if (owner != null)
			buf.append(getTipSuffix(owner, ""));
		    buf.append("&nbsp;</html>");
		}
		text = buf.toString();
	    }

	    return(text);
	}


	final void
	reset(int stackindex, int sliceindex, float x, boolean flipped) {

	    this.flipped = flipped;
	    this.started = false;
	    this.realy = 0;
	    this.totaly = 0;
	    this.height = 0;
	    this.x = x;
	    this.y = plotzeroy;
	    this.eventindex = -1;
	    this.sliceindex = sliceindex;
	    this.stackindex = stackindex;
	}


	public final String
	toString() {

	    String  name = getName();
	    Color   color = getColor();

	    return("name=" + name + ", sliceindex=" + sliceindex + ", height=" + height + ", x=" + x + ", y=" + y + ", color=" + color);
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	private synchronized void
	addRank(StringBuffer buf, boolean showties) {

	    StackedRecord  owner;
	    StackedRegion  region;
	    String         prefix;
	    String         suffix;
	    double         values[];
	    double         value;
	    int            sign;
	    int            tied;
	    int            length;
	    int            n;

	    if ((owner = getOwner()) != null) {
		if ((region = owner.getBottomRegion()) != null) {
		    if (realy != 0) {
			sign = owner.nonnegative ? 1 : -1;
			length = stackedslices.length;
			values = new double[length];
			value = sign*realy;
			for (; region != null; region = region.up)
			    values[region.sliceindex] = sign*region.realy;
			YoixMiscQsort.sort(values, 1);
			for (n = length - 1; n >= 0; n--) {
			    if (values[n] == value) {
				buf.append("<br>");
				if ((prefix = getRankPrefix(owner, "Rank: ")) != null && prefix.length() > 0)
				    buf.append(prefix);
				buf.append(values.length - n);
				if (showties) {
				    tied = 0;
				    for (n -= 1; n >= 0 && values[n] == value; n--)
					tied++;
				    if (tied > 0) {
					buf.append(" (tied with ");
					buf.append(tied);
					buf.append(")");
				    }
				}
				if ((suffix = getRankSuffix(owner, "")) != null && suffix.length() > 0)
				    buf.append(suffix);
				break;
			    }
			}
		    } else {
			buf.append("<br>");
			if ((prefix = getRankPrefix(owner, "Rank: ")) != null && prefix.length() > 0)
			    buf.append(prefix);
			buf.append("none");
			if ((suffix = getRankSuffix(owner, "")) != null && suffix.length() > 0)
			    buf.append(suffix);
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

    class StackedSlice

    {

	//
	// This class remembers some common values that are associated
	// with each region in a slice. There's one StackedSlice array
	// (i.e., stackedslices) that's built and initialized whenever
	// something significant happens to the data colorer. This was
	// originally going to be used to manage linked lists for each
	// slice, but they turned out to be unnecessary - at least for
	// now.
	//

	String  name;
	Color   color;
	int     sliceindex;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	StackedSlice(int sliceindex) {

	    this(sliceindex, UNKNOWNNAME, getForeground());
	}


	StackedSlice(int sliceindex, String name, Color color) {

	    this.sliceindex = sliceindex;
	    this.name = (name != null ? name : UNKNOWNNAME);
	    this.color = (color != null ? color : getForeground());
	}
    }
}

