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
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.print.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import att.research.yoix.*;

class SwingJGraphPlot extends SwingDataViewer

    implements Constants,
	       MouseWheelListener,
	       Printable

{

    //
    // Written after we assumed Java2D was available, so some of this
    // could serve as a model for older classes in this package that
    // were written without Java2D.
    //
    // NOTE - we reproduced the changes that we made in SwingJHistogram
    // to support multiple "secondary" values even though the YDAT Yoix
    // code needed to support the feature in graphs is incomplete, but
    // even if it were it's not nearly as interesting as histograms. In
    // other words, the Java code should be ready but the Yoix support
    // code mostly missing. Changes in this file were made on 2/7/07.
    //
    // NOTE - Added code on 8/1/07 to do a better job with selection and
    // label colors for graphs that have more than one active field. The
    // changes were in drawNode() and findAllNodes() and similiar changes
    // probably also belong in SwingJHistogram.java. Didn't bother right
    // now, but we will eventually look into it.
    //
    // NOTE - the DATA_MARK code was added an 3/13/08. Was written quickly
    // and plenty of things are missing (e.g., we only support mark nodes,
    // don't accurately account for their sizes, and currently can't move
    // them). Perhaps the best solution, but one that would take lots more
    // work, would be to add marks as a new category, like nodes and edges,
    // that are handled separately and always painted last. Anyway, marks
    // were added for an application that needed them so it was rushed in,
    // but we definitely need to take another look - at the very least we
    // need to completely finish the current implementation!!!
    // 

    private GraphData  graph[];
    private GraphData  graphbackgrounds[];

    //
    // Linked lists that group nodes and edges.
    //

    private GraphData  firstnode;
    private GraphData  lastnode;
    private GraphData  firstedge;
    private GraphData  lastedge;

    private HashMap  namemap = null;
    private HashMap  indexmap = null;
    private HashMap  subdatamap = null;
    private boolean  hidemode = false;
    private boolean  havemarks = false;
    private boolean  havesubdata = false;

    //
    // These are sometimes filled with sorted non-zero values that can
    // be used to select an edge or node color using a Palette, but it
    // should only be used with active graph components.
    //

    private Object  palettecontrol_edges = null;
    private Object  palettecontrol_nodes = null;

    //
    // We probably will cache node/edge relationships in these tables
    // after we've done the work to find them.
    //

    private HashMap  attachededges = null;
    private HashMap  attachednodes = null;

    //
    // A simple cache that remembers the last few scaled node or edge
    // paths that actually contained the cursor. Primarily to improve
    // tooltip performance when paths get complicated or the bounding
    // boxes of paths get big (e.g., all the territories of a country
    // like England would require a large bounding box).
    //
    // NOTE - there's still room for improvement in the performance of
    // tooltips. Suspect that working with paths that are transformed
    // to the screen rather than paths in the data's coordinates might
    // help - later.
    //

    private Object  outlinecache[] = null;
    private int     lastoutlinehit = 0;

    //
    // Java2D drawing support.
    //

    private AffineTransform  graphmatrix = null;
    private AffineTransform  graphinverse = null;
    private AffineTransform  screenmatrix = null;
    private AffineTransform  screeninverse = null;
    private BasicStroke      currentedgestroke = null;
    private BasicStroke      currentnodestroke = null;
    private BasicStroke      currentmarkstroke = null;
    private Rectangle2D      databbox_loaded = null;
    private Rectangle2D      databbox = null;
    private Rectangle2D      graphextent = null;
    private Rectangle2D      eventbbox = new Rectangle.Double();
    private Palette          activepalette = null;
    private Insets           markerpad = null;
    private double           maxnodetotal = 0.0;
    private double           maxedgetotal = 0.0;
    private int              motionlisteners = 0;
    private int              clickradius2 = 0;
    private int              zoomdiameter2 = ZOOMDIAMETER*ZOOMDIAMETER;
    private int              backgroundcount = 0;
    private int              edgecount = 0;
    private int              nodecount = 0;

    private YoixObject  graphlayoutarg = null;
    private boolean     alive = true;
    private boolean     brushing = true;
    private Object      graphlayoutmodel = null;
    private String      separator = " ";
    private double      edgewidths[] = null;
    private double      linewidth = 0;
    private double      selectwidth = 0;
    private double      edgescale = 1.0;
    private double      fontscale = 1.0;
    private double      nodescale = 1.0;
    private double      nodeoutline = 1.0;
    private int         graphlayoutsort[] = new int[] {VL_SORT_TEXT, VL_SORT_TEXT};
    private int         currentorder = VL_SORT_LOAD_ORDER;
    private int         edgeflags = 0;
    private int         fillmodel = 0;
    private int         nodeflags = 0;
    private int         selectflags = 0;
    private int         tipflags = 0;
    private int         zoomdirection = 1;

    private boolean  attachededgeselection = false;

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
    private Object          tipprefix = null;
    private Object          tipsuffix = null;

    //
    // The scaling used to used to fit the loaded data to the current
    // screen is stored in graphscale. The scaling used to draw the
    // graph is obtained by multiplying graphscale and zoomscale.
    //

    private Point2D  lastcenter = null;
    private double   graphscale = 1.0;
    private double   zoomscale = 1.0;
    private double   zoomlimit = ZOOMLIMIT;
    private Font     smallestfont = null;

    //
    // These are used to help keep track of what's in currentedgestroke
    // and currentnodestroke. Dash patterns are hardest - we didn't want
    // to be forced into comparing individual array elements.
    //

    private double  currentedgewidth = 0;
    private double  currentnodewidth = 0;
    private double  currentmarkwidth = 0;
    private double  currentedgepattern[] = null;

    //
    // Support for scrolling, hit detection, and probably more.
    //

    private GraphSlice  xslices[] = null;
    private GraphSlice  yslices[] = null;
    private final int   SLICES = 10;

    //
    // Previous event location - for brushing and transient mode.
    //

    private GraphData  pressedelement = null;
    private boolean    pressedstart;
    private Point2D    grabbedpoint = null;
    private Point      lastpoint = null;

    //
    // Stuff used when we're dragging a node (or maybe an edge).
    //

    private Graphics2D  draggraphics = null;
    private GraphData   dragelement = null;
    private boolean     dragdragged = false;
    private Point2D     dragorigin = null;
    private Point2D     dragpoint = null;
    private Object      dragobjects[] = null;
    private Point       draglastpoint = null;
    private Point       dragoffset = null;
    private int         dragstyle = 0;

    //
    // Edge, node, and zoom control - there probably will be more.
    //

    private static final int  EDGEHIDE = 0x01;
    private static final int  EDGETOGGLE = 0x02;
    private static final int  EDGEHIDEDESELECTED = 0x04;
    private static final int  EDGEERASE = 0x08;		// currently unimplemented
    private static final int  NODEHIDE = 0x01;
    private static final int  NODEHIDEDESELECTED = 0x04;
    private static final int  NODEERASE = 0x08;		// currently unimplemented

    private static final double  ZOOMLIMIT = 10.0;
    private static final double  ZOOMPOINTSIZE = 36.0;
    private static final int     ZOOMDIAMETER = YoixMakeScreen.javaDistance(72.0/4);

    //
    // These are used to classify data records. Currently flags so they
    // can be combined and used to extract particular components from the
    // input data. For example, sometimes we might want nodes and other
    // times we might want nodes and edges. In addition we need control
    // information (e.g., scaling or bounding box info) and passing it
    // in with the other data makes sense.
    //
    // NOTE - the constants used to set tipflags currently assume that
    // TIP_OVER_NODES matches DATA_GRAPH_NODE and TIP_OVER_EDGES matches
    // DATA_GRAPH_EDGE, so be careful if you change them contants.
    //

    static final int  DATA_GRAPH_NODE = 0x01;
    static final int  DATA_GRAPH_EDGE = 0x02;
    static final int  DATA_GRAPH_REFERENCE = 0x04;
    static final int  DATA_GRAPH_REQUIRED = 0x08;
    static final int  DATA_BACKGROUND_NODE = 0x10;
    static final int  DATA_BACKGROUND_EDGE = 0x20;
    static final int  DATA_MARK = 0x40;

    static final int  DATA_EDGE_MASK = DATA_GRAPH_EDGE|DATA_BACKGROUND_EDGE;
    static final int  DATA_NODE_MASK = DATA_GRAPH_NODE|DATA_BACKGROUND_NODE;
    static final int  DATA_GRAPH_MASK = DATA_GRAPH_EDGE|DATA_GRAPH_NODE;
    static final int  DATA_BACKGROUND_MASK = DATA_BACKGROUND_EDGE|DATA_BACKGROUND_NODE;

    //
    // Supposed to control the order that nodes and edges are painted.
    //

    private int  paintorder[] = {DATA_GRAPH_EDGE, DATA_GRAPH_NODE};

    //
    // Low level sweep support - currently only for zooming in graphs,
    // but we may evetually support selection.
    //

    private int  xcorner[] = {0, 0, 0, 0};
    private int  ycorner[] = {0, 0, 0, 0};

    //
    // Slight pause, in milliseconds, between the positioning and final
    // repaint when we're CENTERING an element.
    //

    private static final int  CENTERINGPAUSE = 400;

    //
    // Miscellaneous stuff - mostly for GraphData.
    //

    private static final double  DOTPATTERN[] = new double[0];
    private static final double  DASHPATTERN[] = new double[] {12, 8};

    private static final int  TOKEN_START = 0;
    private static final int  TOKEN_END = 1;

    private static final int  ATTR_DISCARD = 0;
    private static final int  ATTR_COLOR = 1;
    private static final int  ATTR_NUMBER = 2;
    private static final int  ATTR_STRING = 3;
    private static final int  ATTR_POINT = 4;

    //
    // Deadlock might not have been an issue because getMarker() was a
    // static method, but just to be safe things are now more explicit.
    //

    private static final Object  MARKERLOCK = new Object();
    private static int           marker = 0;

    //
    // A definition that won't be used if a font has been assigned to this
    // component via the font field defined in its data dictionary.
    //

    private static final Font  DEFAULTFONT = new Font("TimesRoman", Font.PLAIN, 14);

    //
    // The stroke used to create an area that represents the line segment
    // that connects two consecutive cursor positions.
    //

    private static final BasicStroke  BRUSHSTROKE = new BasicStroke(
	0.01f,
	BasicStroke.CAP_SQUARE,
	BasicStroke.JOIN_MITER
    );

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    SwingJGraphPlot(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
	clearViewer();
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
	    setTipText(null);
    }


    public synchronized void
    mousePressed(MouseEvent e) {

	int  modifiers;
	int  button;
	int  op;

	//
	// Currently no way to get to the SWEEPING state, but that should
	// change. Expect it will work like a sweep in EventPlot, but the
	// DataManager (and maybe others) need low level attention first.
	//

	if (graph != null) {
	    if (mouse == AVAILABLE && alive) {
		lastpoint = null;
		pressedelement = null;
		dragelement = null;
		modifiers = YoixMiscJFC.cookModifiers(e);
		button = modifiers & YOIX_BUTTON_MASK;
		op = getOperation(modifiers);
		if (hasFocus() == false)
		    requestFocus();
		switch (button) {
		    case YOIX_BUTTON1_MASK:
			switch (op) {
			    case VL_OP_BRUSH:
			    case VL_OP_POINT:
			    case VL_OP_SELECT:
				mouse = SELECTING;
				brushing = (op != VL_OP_POINT);
				selectElement(e.getPoint(), true);
				break;

			    case VL_OP_DRAG:
				mouse = DRAGGING;
				dragBegin(e.getPoint(), 0);
				break;

			    case VL_OP_GRAB:
				mouse = GRABBING;
				grabBegin(e.getPoint());
				break;

			    case VL_OP_PRESS:
				mouse = PRESSING;
				pressBegin(e.getPoint());
				break;

			    case VL_OP_SCROLL:
				mouse = SCROLLING;
				scrollBegin(e.getPoint());
				break;

			    case VL_OP_TIP:
				mouse = TIPPING;
				tipmanager.startShifting();
				break;

			    case VL_OP_ZOOM:
				mouse = (zoomdirection >= 0) ? ZOOMING_IN : ZOOMING_OUT;
				sweepBegin(e.getPoint());
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
			    case VL_OP_POINT:
			    case VL_OP_SELECT:
				mouse = DESELECTING;
				brushing = (op != VL_OP_POINT);
				selectElement(e.getPoint(), false);
				break;

			    case VL_OP_DRAG:
				mouse = DRAGGING;
				dragBegin(e.getPoint(), 1);
				break;

			    case VL_OP_GRAB:
			    case VL_OP_SCROLL:
				mouse = CENTERING;
				pressBegin(e.getPoint());
				break;

			    case VL_OP_PRESS:
				mouse = TOGGLING;
				pressBegin(e.getPoint());
				break;

			    case VL_OP_ZOOM:
				mouse = (zoomdirection >= 0) ? ZOOMING_OUT : ZOOMING_IN;
				sweepBegin(e.getPoint());
				break;

			    default:
				mouse = UNAVAILABLE;
				break;
			}
			break;
		}
		mousebutton = (mouse != AVAILABLE) ? button : 0;
		if (mouse != AVAILABLE && mouse != TIPPING)
		    setTipText(null);
		updateMouseMotionListener();
	    }
	}
    }


    public synchronized void
    mouseReleased(MouseEvent e) {

	Point  point;
	int    buttons;

	//
	// The modifiers that we get from Java 1.3.1 and newer versions are
	// different, so we can't just compare mousebutton and buttons. Can
	// change when we no longer support Java 1.3.1.
	//

	if (mouse != AVAILABLE) {
	    buttons = YoixMiscJFC.cookModifiers(e) & YOIX_BUTTON_MASK;
	    if ((buttons & mousebutton) != 0) {		// test is for Java 1.3.1
		switch (mouse) {
		    case CENTERING:
		    case PRESSING:
		    case TOGGLING:
			pressEnd(e);
			break;

		    case DESELECTING:
			postActionEvent(null, e.getModifiers());
			break;

		    case DRAGGING:
			dragEnd(e.getPoint());
			break;

		    case GRABBING:
			grabEnd(e.getPoint());
			break;

		    case SCROLLING:
			scrollEnd(e.getPoint());
			break;

		    case SELECTING:
			postActionEvent(null, e.getModifiers());
			break;

		    case SWEEPING:
		    case ZOOMING_IN:
		    case ZOOMING_OUT:
			sweepEnd(e);
			break;

		    case TIPPING:
			break;
		}
		lastpoint = null;
		pressedelement = null;
		dragelement = null;
		mouse = AVAILABLE;
		mousebutton = 0;
	    }
	    moveTip(e.getPoint());
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

	if (mouse != AVAILABLE && graph != null) {
	    switch (mouse) {
		case CENTERING:
		case PRESSING:
		case TOGGLING:
		    pressDragged(e.getPoint());
		    break;

		case DESELECTING:
		    selectElement(e.getPoint(), false);
		    break;

		case DRAGGING:
		    dragDragged(e.getPoint());
		    break;

		case GRABBING:
		    grabDragged(e.getPoint());
		    break;

		case SCROLLING:
		    scrollDragged(e.getPoint());
		    break;

		case SELECTING:
		    selectElement(e.getPoint(), true);
		    break;

		case SWEEPING:
		case ZOOMING_IN:
		case ZOOMING_OUT:
		    sweepDragged(e.getPoint());
		    break;
	    }
	}
    }


    public synchronized void
    mouseMoved(MouseEvent e) {

	if (graph != null) {
	    if (ISMAC && (e.getModifiers()&YOIX_CTRL_MASK) != 0)
		mouseDragged(e);
	    else moveTip(e.getPoint());
	}
    }

    ///////////////////////////////////
    //
    // MouseWheelListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseWheelMoved(MouseWheelEvent e) {

	setTipText(null);
    }

    ///////////////////////////////////
    //
    // Printable Methods
    //
    ///////////////////////////////////

    public final int
    print(Graphics g, PageFormat pf, int index) {

	AffineTransform  original;
	AffineTransform  matrix;
	double           scale;
	double           width;
	double           height;
	double           angle;
	double           x;
	double           y;
	Color            color;
	Shape            clip;

	//
	// Thrown in quickly, but we could use a general approach that
	// also works with Yoix paint() functions. Not trivial, but it
	// should be possible - main issue is how to make sure we use
	// this Graphics object everywhere.
	//

	original = ((Graphics2D)g).getTransform();
	clip = g.getClip();
	color = g.getColor();

	x = pf.getImageableX();
	y = pf.getImageableY();
	width = pf.getImageableWidth();
	height = pf.getImageableHeight();
	scale = Math.min(width/viewport.width, height/viewport.height);

	if (height/viewport.width > scale && width/viewport.height > scale) {
	    angle = Math.PI/2.0;
	    scale = Math.min(height/viewport.width, width/viewport.height);
	} else angle = 0;

	matrix = (AffineTransform)original.clone();
	matrix.translate(x + width/2, y + height/2);
	matrix.scale(scale, scale);
	matrix.rotate(angle);
	matrix.translate(-viewport.width/2, -viewport.height/2);

	g.setColor(getBackground());
	g.fillRect((int)x, (int)y, (int)width, (int)height);
	g.setColor(getForeground());
	((Graphics2D)g).setTransform(matrix);

	//
	// We really should be able to call paint(g) here, but some
	// low level code Sun doesn't approve during paintBorder(),
	// at least when we run on our SGI. For now just duplicate
	// the acceptable parts of paint() here.
	//

	g.translate(insets.left, insets.top);
	paintRect(viewport.x, viewport.y, viewport.width, viewport.height, g);
	g.translate(-insets.left, -insets.top);

	g.setColor(color);
	g.setClip(clip);
	((Graphics2D)g).setTransform(original);

	return(index == 0 ? PAGE_EXISTS : NO_SUCH_PAGE);
    }

    ///////////////////////////////////
    //
    // DataColorer Methods
    //
    ///////////////////////////////////

    public final synchronized void
    appendRecords(DataRecord loaded[], int offset) {

	DataRecord  record;
	HitBuffer   hits;
	GraphData   element;
	ArrayList   elements;
	ArrayList   backgrounds;
	HashMap     references;
	HashMap     map;
	String      name;
	int         length;
	int         count;
	int         m;
	int         n;

	if (isReady()) {
	    if (datamanager != null && loaded != null) {
		length = loaded.length;
		if (offset >= 0 && offset < length) {
		    loadeddata = loaded;
		    datarecords = loaded;
		    hits = new HitBuffer(length - offset);
		    count = 0;
		    if (sweepfiltering == false) {
			map = new HashMap(viewermap);
			references = new HashMap();
			elements = YoixMisc.copyIntoArrayList(graph);
			backgrounds = YoixMisc.copyIntoArrayList(graphbackgrounds);
			for (n = offset; n < length; n++) {
			    if ((record = datarecords[n]) != null) {
				for (m = 0; m < fieldindices.length; m++) {
				    if ((name = getRecordName(record, m)) != null) {
					if (m < fieldmasks.length) {	// it's active
					    element = loadActiveElement(name, name, n, 1, getRecordValues(record, 1), map, references, elements, backgrounds);
					    if (element != null) {
						if (record.isSelected(fieldmasks[m])) {
						    if (element.selected == false) {
							if (record.isSelected())
							    hits.setRecord(count++, record, false);
							record.clearSelected(fieldmasks[m]);
						    }
						} else element.update(-1, getRecordValues(record, -1), false);
						colormap.put(name, record);
					    }
					} else loadPassiveElement(name, name, n, map, references, elements, backgrounds);
				    }
				}
			    }
			}
			loadReferences(map, references, elements, backgrounds);
			loadSubData(map, elements);
			buildViewer(map, elements, backgrounds);
			sliceGraph();
			syncViewport();
			datamanager.updateData(loadeddata, hits, count, this);
			afterLoad();
			if (isActiveHiddenDataColorer())
			    repaintViewer();
			else reset();
		    }
		}
	    }
	}
    }


    public final synchronized int
    compare(DataRecord record1, DataRecord record2) {

	GraphData  element1;
	GraphData  element2;
	String     name1;
	String     name2;
	int        result;

	//
	// Returns negative if record1 should precede record2, 0 if they
	// can be considered equal, and positive if record2 should precede
	// record1.
	//

	if ((name1 = getRecordName(record1)) != null) {
	    if ((name2 = getRecordName(record2)) != null) {
		if (name1.equals(name2) == false) {
		    if (viewermap != null) {
			if ((element1 = (GraphData)viewermap.get(name1)) != null) {
			    if ((element2 = (GraphData)viewermap.get(name2)) != null)
				result = element1.compare(element2, VL_SORT_TOTAL);
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

	GraphData  element;
	String     name;
	Color      color;

	if (viewermap != null && (name = getRecordName(record)) != null) {
	    if ((element = (GraphData)viewermap.get(name)) != null)
		color = element.color;
	    else color = null;
	} else color = null;

	return(color);
    }


    public final synchronized Color
    getColor(String name) {

	GraphData  element;
	Color      color;

	if (name != null && viewermap != null) {
	    if ((element = (GraphData)viewermap.get(name)) != null)
		color = element.color;
	    else color = null;
	} else color = null;

	return(color);
    }


    public final synchronized String
    getKey(int n) {

	return(n >= 0 && n < totalcount ? graph[n].key : null);
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
		names[n] = graph[n].key;
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
		data[m++] = graph[n].getElementName();
		data[m++] = graph[n].color;
	    }
	}
	return(data);
    }


    public final synchronized String
    getTipText(String name, int flags, boolean html) {

	GraphData  element;
	String     text;

	if (name != null && viewermap != null) {
	    if ((element = (GraphData)viewermap.get(name)) != null)
		text = element.getTipText(flags, html);
	    else text = null;
	} else text = null;
	return(text);
    }


    public final synchronized String
    getTipTextAt(Point point, int flags, boolean html) {

	GraphData  element;
	String     text;
	int        index;

	if ((index = findNextElement(point, eventbbox, DATA_GRAPH_NODE)) < 0)
	    index = findNextElement(point, eventbbox, DATA_GRAPH_EDGE);
	if (index >= 0) {
	    element = graph[index];
	    text = element.getTipText(flags, html);
	} else text = null;
	return(text);
    }


    public final synchronized boolean
    isActiveDataColorer() {

	return(isDataColorer() && currentpalette != null);
    }


    public final synchronized void
    loadColors() {

	int  count;
	int  n;

	//
	// New DataColorer method that does whatever is necessary to make
	// sure elements are properly colored when we're responsible for
	// coloring data records.
	//

	if (graph != null) {
	    if (isDataColorer()) {
		count = 0;
		for (n = 0; n < totalcount; n++) {
		    if (graph[n].color == null) {
			graph[n].repaint = true;
			count++;
		    }
		}
		if (count > 0)
		    repaintViewer(count);
	    }
	}
    }


    public final synchronized void
    loadRecords(DataRecord loaded[], DataRecord records[]) {

	if (loaded != loadeddata || datarecords != records)
	    loadRecords(loaded, records, true);
    }


    public final synchronized void
    loadRecords(DataRecord loaded[], DataRecord records[], boolean force) {

	DataRecord  record;
	YoixObject  root;
	GraphData   element;
	ArrayList   elements;
	ArrayList   backgrounds;
	HashMap     references;
	HashMap     map;
	String      name;
	int         length;
	int         m;
	int         n;

	if (isReady() || force) {
	    clearViewer();
	    if (datamanager != null && records != null && records.length > 0) {
		if (sweepfiltering == false) {
		    length = records.length;
		    loadeddata = loaded;
		    datarecords = records;
		    colormap = new Hashtable();
		    map = new HashMap(length);
		    references = new HashMap();
		    elements = new ArrayList(length);
		    backgrounds = new ArrayList();
		    havemarks = false;
		    for (n = 0; n < length; n++) {
			if ((record = datarecords[n]) != null) {
			    for (m = 0; m < fieldindices.length; m++) {
				if ((name = getRecordName(record, m)) != null) {
				    if (m < fieldmasks.length) {	// it's active
					element = loadActiveElement(name, name, n, 1, getRecordValues(record, 1), map, references, elements, backgrounds);
					if (element != null) {
					    if (record.notSelected()) {
						element.update(-1, getRecordValues(record, -1), false);
						if (record.notSelected(fieldmasks[m]))
						    element.selected = false;
					    }
					    colormap.put(name, record);
					}
				    } else loadPassiveElement(name, name, n, map, references, elements, backgrounds);
				}
			    }
			}
		    }
		    loadReferences(map, references, elements, backgrounds);
		    loadSubData(map, elements);
		    buildViewer(map, elements, backgrounds);
		    sliceGraph();
		    zoomToData(null);
		} else loadSweepFilter(loaded, records);
	    }
	    syncViewport();
	    afterLoad();
	    if (isActiveHiddenDataColorer())
		repaintViewer();
	    else reset();
	}
    }


    public final synchronized void
    setExtent() {

	Rectangle2D  rect;

	if ((rect = graphextent) != null) {
	    extent.width = (int)rect.getWidth();
	    extent.height = (int)rect.getHeight();
	} else {
	    extent.width = 0;
	    extent.height = 0;
	}
    }


    public final synchronized void
    tossLabels() {

	int  n;

	for (n = 0; n < totalcount; n++)
	    graph[n].tossLabel();
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
	ArrayList   elements;
	HashMap     keys;
	Object      value;
	String      name;
	int         length;
	int         total;
	int         size;
	int         m;
	int         n;

	if (graph != null) {
	    if (datamanager != null) {
		if ((elements = findAllElements(point, point, selectflags, !selected)) != null) {
		    length = datarecords.length;
		    size = elements.size();
		    list = new ArrayList(size);
		    keys = new HashMap((int)(1.1*size));
		    total = 0;
		    for (n = 0; n < size; n++) {
			if ((value = elements.get(n)) != null) {
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

	DataRecord  record;
	GraphData   element;
	boolean     repaint;
	boolean     force;
	double      edgetotals[];
	double      nodetotals[];
	double      value;
	Color       color;
	int         nextedge;
	int         nextnode;
	int         count = 0;
	int         length;
	int         type;
	int         n;

	//
	// Now using totalcount to help decide whether graph elements
	// need to be repainted (4/10/03).
	//

	if (graph != null) {
	    force = isActiveHiddenDataColorer();
	    repaint = (isShowing() && painted && totalcount > 0) || force;
	    length = graph.length;
	    if (palette != null) {
		if (palette != activepalette) {
		    edgetotals = new double[edgecount];
		    nodetotals = new double[nodecount];
		    nextedge = 0;
		    nextnode = 0;
		    for (n = 0; n < length; n++) {
			element = graph[n];
			if (element.isother == false) {
			    if (element.color != null || force) {
				element.color = null;
				if (repaint) {
				    element.repaint = true;
				    count++;
				}
			    }
			    if (element.element_bounds != null) {
				value = element.getActiveTotal();
				switch (element.element_type) {
				    case DATA_GRAPH_EDGE:
					if (value > 0)
					    edgetotals[nextedge++] = value;
					break;

				    case DATA_GRAPH_NODE:
					if (value > 0)
					    nodetotals[nextnode++] = value;
					break;
				}
			    }
			} else element.color = othercolor;
		    }
		    palettecontrol_edges = palette.getPaletteControls(edgetotals, nextedge);
		    palettecontrol_nodes = palette.getPaletteControls(nodetotals, nextnode);
		    maxedgetotal = palette.getMaxValue(palettecontrol_edges);
		    maxnodetotal = palette.getMaxValue(palettecontrol_nodes);
		    activepalette = palette;
		}
	    } else {
		for (n = 0; n < length; n++) {
		    element = graph[n];
		    if (element.isother == false) {
			if (element.loaded) {
			    if (coloredby != this) {
				if ((record = (DataRecord)colormap.get(element.key)) != null) {
				    if (coloredby != null)
					color = coloredby.getColor(record);
				    else color = record.getColor();
				} else color = null;
			    } else color = element.element_color;
			} else color = null;
			if (repaint && color != element.color) {
			    if (color == null || !color.equals(element.color)) {
				element.repaint = true;
				count++;
			    }
			}
			element.color = color;
		    } else element.color = othercolor;
		}
	    }
	}

	return(count);
    }


    public final synchronized int
    getCountTotal(int n) {

	return(n >= 0 && n < totalcount ? graph[n].getCountTotal() : 0);
    }


    public final synchronized boolean
    isHighlighted(int n) {

	return(n >= 0 && n < totalcount && graph[n].highlight);
    }


    public final synchronized boolean
    isPressed(int n) {

	return(n >= 0 && n < totalcount && graph[n].pressed);
    }


    public final synchronized boolean
    isSelected(int n) {

	return(n >= 0 && n < totalcount && graph[n].getCountValue() != 0);
    }


    public final synchronized boolean
    isSelected(String name) {

	GraphData  element;
	boolean    result;

	if (viewermap != null) {
	    if ((element = (GraphData)viewermap.get(name)) != null)
		result = (element.getCountValue() != 0);
	    else result = false;
	} else result = false;

	return(result);
    }


    public final synchronized void
    recolorViewer() {

	//
	// Older code called repaintViewer(), but that took really long
	// for big graphs.
	//

	if (totalcount > 0) {
	    if (colorViewerWith(currentpalette) > 0) {
		if (datarecords != null)	// unnecessary test
		    repaintViewer(datarecords.length);
		else repaintViewer();
	    }
	}
    }


    public final synchronized void
    repaintViewer() {

	Rectangle2D  dirty;
	Rectangle2D  cliprect;
	Rectangle2D  bounds;
	Rectangle2D  covered;
	Graphics2D   g;
	GraphData    element;
	GraphData    next;
	GraphData    last;
	GraphData    firstmark;
	GraphData    lastmark;
	boolean      forcedraw;
	Shape        clip;
	Shape        shape;
	Color        fg;
	Color        bg;
	Font         font;
	int          tx;
	int          ty;
	int          n;

	//
	// Uses a linear search, as is done in other methods, but this
	// one should clear the repaint field when it's set. That means
	// optimization, if there is any, probably can only surround the
	// call that actually does the painting.
	//
	// This code currently assumes edges are painted first despite
	// the fact that Yoix scripts can set the order. It shouldn't be
	// too hard to fix (modify the dirty code based on the paintorder
	// index), but it's definitely not a high priority right now.
	//

	if (isShowing() || isDataColorer()) {
	    if (graph != null && firstnode != null) {
		if ((g = (Graphics2D)getSavedGraphics()) != null) {
		    font = g.getFont();
		    tx = insets.left - viewport.x;
		    ty = insets.top - viewport.y;
		    g.translate(tx, ty);
		    g.setFont(deriveFont(font));
		    fg = getForeground();
		    bg = getBackground();
		    clip = g.getClip();
		    cliprect = transformViewportToData(g.getClipBounds());
		    dirty = null;
		    //
		    // Seems like isActiveDataColorer() should be sufficient
		    // but we had some coloring problems. Overkill for now,
		    // but even this isn't 100%. Real problem we're trying
		    // to address is the fact that color assignments happen
		    // in the low level draw methods, so we sometime need to
		    // make sure they run even when it appears unnecessary.
		    //
		    forcedraw = isDataColorer();
		    for (n = 0; n < paintorder.length; n++) {
			switch (paintorder[n]) {
			    case DATA_GRAPH_EDGE:
				g.setClip(new Area(clip));	// ZLW kludge
				for (next = firstedge, last = lastedge, element = null; element != last; ) {
				    element = next;
				    next = element.next;
				    if (element.repaint) {
					element.repaint = false;
					if ((bounds = element.getCurrentBounds()) != null) {
					    if (cliprect.intersects(bounds) || forcedraw) {
						drawEdge(element, fg, bg, g);
						if (dirty == null)
						    dirty = bounds;
						else dirty.add(bounds);
					    }
					    raiseEdge(element);
					}
				    }
				}
				if (dirty != null)
				    dirty.intersect(cliprect, dirty, dirty);
				break;

			    case DATA_GRAPH_NODE:
				g.setClip(new Area(clip));	// ZLW kludge
				for (next = firstnode, last = lastnode, element = null; element != last; ) {
				    element = next;
				    next = element.next;
				    bounds = element.getCurrentBounds();
				    if (element.repaint) {
					element.repaint = false;
					if (bounds != null) {
					    if (cliprect.intersects(bounds) || forcedraw)
						drawNode(element, fg, bg, 1.0, g);
					    raiseNode(element);
					}
				    } else if (dirty != null) {
					if (bounds != null) {
					    if (dirty.intersects(bounds))
						drawNode(element, fg, bg, 1.0, g);
					}
				    }
				}
				if (havemarks) {
				    //
				    // This makes sure mark nodes aren't covered by regular
				    // nodes. The code probably can be simplified and there's
				    // a small chance that the work done initial loop could
				    // be handled by the code that just repainted the nodes.
				    // Perhaps the best solution, but one that would take
				    // considerable work, is to add marks as a new category,
				    // like nodes and edges, that are handled separately and
				    // always painted last.
				    //
				    covered = null;
				    firstmark = null;
				    lastmark = null;
				    for (element = firstnode; element != null; element = element.next) {
					if (element.ismark) {
					    if (firstmark == null)
						firstmark = element;
					    lastmark = element;
					} else {
					    if (firstmark != null) {
						if ((bounds = element.getCurrentBounds()) != null) {
						    if (covered == null)
							covered = bounds;
						    else covered.add(bounds);
						}
					    }
					}
				    }
				    if (covered != null) {
					for (next = firstmark, last = lastmark, element = null; element != last; ) {
					    element = next;
					    next = element.next;
					    if (element.ismark) {
						bounds = element.getCurrentBounds();
						if (bounds != null && covered.intersects(bounds) && cliprect.intersects(bounds))
						    drawNode(element, fg, bg, 1.0, g);
						raiseNode(element);
					    }
					}
				    }
				}
				break;
			}
		    }
		    g.setClip(clip);
		    g.translate(-tx, -ty);
		    g.setFont(font);
		    disposeSavedGraphics(g);
		}
	    }
	}
    }


    public final synchronized void
    repaintViewer(int count) {

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
		    pickElements(items, select, deselect, false);
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
		    pickElements(items, select, deselect, false);
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
		    pickElements(items, select, deselect, true);
		    setSelected(select, deselect);
		}
	    }
	}
    }


    public final synchronized void
    setSelected(HashMap select, HashMap deselect) {

	HitBuffer  hits;
	GraphData  element;
	boolean    needpaint;
	boolean    state;
	String     key;
	int        count;
	int        n;

	if (loadeddata != null && datamanager != null) {
	    needpaint = false;
	    for (n = 0; n < graph.length; n++) {
		element = graph[n];
		if (element.loaded && (element.element_type & selectflags) != 0) {
		    state = select.containsKey(element.key);
		    if (element.selected != state) {
			element.selected = state;
			element.repaint = true;
			needpaint = true;
		    }
		} else if ((key = element.key) != null) {
		    select.remove(key);
		    deselect.remove(key);
		}
	    }
	    if ((hits = datamanager.getHitBuffer(loadeddata)) != null) {
		if ((count = collectRecords(hits, select, deselect)) > 0) {
		    datamanager.updateData(loadeddata, hits, count, this);
		    needpaint = false;
		    Thread.yield();
		} else datamanager.releaseHitBuffer(hits);
	    }
	    if (needpaint)			// unlikely this is still true
		repaintViewer();
	}
    }


    public final synchronized void
    updateViewer(DataRecord loaded[], HitBuffer hits, int count) {

	DataRecord   record;
	GraphData    edges[];
	GraphData    nodes[];
	GraphData    element;
	GraphData    subelement;
	GraphData    edge;
	boolean      append;
	boolean      filtered;
	boolean      postevent;
	boolean      recolored;
	String       name;
	String       secondary;
	int          sign;
	int          m;
	int          n;
	int          i;

	if (count > 0 && isLoaded()) {
	    if (isReady()) {
		if (loaded == loadeddata && graph != null && viewermap != null) {
		    setTipText(null);
		    postevent = hasItemListener();
		    recolored = false;
		    for (n = 0; n < count; n++) {
			record = hits.getRecord(n);
			for (m = 0; m < fieldmasks.length; m++) {
			    if ((name = getRecordName(record, m)) != null) {
				if ((element = (GraphData)viewermap.get(name)) != null) {
				    filtered = element.isFiltered();
				    sign = hits.isSelected(n) ? 1 : -1;
				    element.update(sign, getRecordValues(record, sign), postevent);
				    recolored = true;
				    if (element.subdata != null) {
					for (i = 0; i < element.subdata.length; i++) {
					    subelement = (GraphData)element.subdata[i];
					    subelement.update(sign, getRecordValues(record, sign), postevent);
					}
				    }
				    if (attachededgeselection && element.isNode() && filtered != element.isFiltered()) {
					if ((edges = findAttachedEdges(element)) != null) {
					    filtered = !filtered;
					    for (i = 0; i < edges.length; i++) {
						if ((edge = edges[i]) != null) {
						    if (edge.filtered != filtered) {
							if ((nodes = findAttachedNodes(edge)) != null) {
							    append = filtered || (
								(nodes[0] == null || nodes[0].isFiltered() == false)
								&&
								(nodes[1] == null || nodes[1].isFiltered() == false)
							    );
							} else append = true;
							if (append) {
							    edge.filtered = filtered;
							    edge.repaint = true;
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
		    if (recolored && currentpalette != null) {
			if (datamanager.getColoredBy() == this)
			    datamanager.recolorData(this);
		    }
		    afterUpdate();
		}
	    } else clearViewer();
	}
    }

    ///////////////////////////////////
    //
    // SwingJGraphPlot Methods
    //
    ///////////////////////////////////

    final synchronized int
    findNextElement(Point2D point, Rectangle2D clip, int types) {

	return(findNextElement(point, clip, types, true, true));
    }


    protected void
    finalize() {

	graph = null;
	graphbackgrounds = null;
	super.finalize();
    }


    final boolean
    getAlive() {

	return(alive);
    }


    final Font
    getDefaultFont(boolean tight) {

	Font  font;

	//
	// The preferred way to set the default font is through the font
	// field defined in its data dictionary, which is what getFont()
	// should return. If it's null then we use DEFAULTFONT.
	//

	if ((font = getFont()) == null)
	    font = DEFAULTFONT;
	return(tight ? deriveFont(font) : font);
    }


    final int
    getEdgeCount() {

	return(edgecount);
    }


    final YoixObject
    getEdges() {

	return(YoixMisc.copyIntoArray(getElements(DATA_GRAPH_EDGE)));
    }


    final int[]
    getGraphLayoutSort() {

	//
	// We don't make a copy because Yoix scripts automatically get one
	// anyway, and internally we want to reduce sorting overhead.
	//

	return(graphlayoutsort);
    }


    final synchronized AffineTransform
    getGraphMatrix() {

	return(graphmatrix != null ? new AffineTransform(graphmatrix) : null);
    }


    final synchronized Point2D
    getPosition(String name) {

	GraphData  element;
	Point2D    point;

	if (viewermap != null) {
	    if ((element = (GraphData)viewermap.get(name)) != null)
		point = element.getElementPosition();
	    else point = null;
	} else point = null;

	return(point);
    }


    final double
    getLineWidth() {

	return(linewidth);
    }


    final synchronized YoixObject
    getMoved() {

	YoixObject  obj = YoixObject.newArray(0, -1);
	int         m;
	int         n;

	for (n = 0, m = 0; n < totalcount; n++) {
	    if (graph[n].original_values != null)
		obj.putString(m++, graph[n].key);
	}
	return(obj);
    }


    final int
    getNodeCount() {

	return(nodecount);
    }


    final YoixObject
    getNodes() {

	return(YoixMisc.copyIntoArray(getElements(DATA_GRAPH_NODE)));
    }


    final synchronized int
    getOutlineCacheSize() {

	return(outlinecache != null ? outlinecache.length : 0);
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


    final double
    getZoomLimit() {

	return(zoomlimit);
    }


    final double
    getZoomScale() {

	return(zoomscale);
    }


    final synchronized boolean
    isActiveHiddenDataColorer() {

	return(isShowing() == false && isActiveDataColorer());
    }


    protected final synchronized void
    paintRect(int x, int y, int width, int height, Graphics g) {

	Rectangle2D  drect;
	GraphSlice   sl[];
	GraphData    element;
	Rectangle    cliprect;
	Point2D      point1;
	Point2D      point2;
	boolean      needpaint;
	double       dmn;
	double       dmx;
	Shape        clip;
	Color        color;
	Color        fg;
	Color        bg;
	Font         font;
	int          length;
	int          mark;
	int          m;
	int          n;

	if (totalcount > 0 && graphmatrix != null) {
	    needpaint = false;
	    clip = g.getClip();
	    g.translate(-viewport.x, -viewport.y);
	    g.clipRect(x, y, width, height);	// recent change
	    cliprect = g.getClipBounds();
	    //
	    // Java 1.6.0 would sometimes miss elements (often the right
	    // or bottom boundary of a rectangular node) unless we expand
	    // our rectangle slightly. We suspect it's something in their
	    // Rectangle2D.intersects() method, but we're not certain. In
	    // any case, growing our rectangle slightly, just to be sure,
	    // really isn't expensive and may even be completely correct.
	    //
	    point1 = new Point2D.Double(cliprect.x-1, cliprect.y-1);
	    point2 = new Point2D.Double(cliprect.x+cliprect.width+2, cliprect.y+cliprect.height+2);
	    graphinverse.transform(point1, point1);
	    graphinverse.transform(point2, point2);
	    drect = new Rectangle2D.Double(
		point1.getX(), point1.getY(),
		point2.getX() - point1.getX(), point2.getY() - point1.getY()
	    );
	    if (width < height) {
		sl = xslices;
		dmn = point1.getX();
		dmx = point2.getX();
	    } else {
		sl = yslices;
		dmn = point1.getY();
		dmx = point2.getY();
	    }
	    mark = getMarker();
	    for (m = 0; m < SLICES; m++) {
		if (sl[m].intersects(dmn, dmx)) {
		    length = sl[m].members.length;
		    for (n = 0; n < length; n++) {
			element = graph[sl[m].members[n]];
			if (element.marker != mark) {
			    if (element.intersects(drect)) {
				element.marker = mark;
				needpaint = true;
			    }
			}
		    }
		}
	    }
	    if (needpaint || backgroundcount > 0) {
		color = g.getColor();
		font = g.getFont();
		g.setFont(deriveFont(font));
		g.setClip(new Area(cliprect));		// ZLW kludge
		fg = getForeground();
		bg = getBackground();
		drawBackground(fg, bg, (Graphics2D)g);
		if (needpaint) {
		    for (n = 0; n < paintorder.length; n++) {
			switch(paintorder[n]) {
			    case DATA_GRAPH_EDGE:
				for (element = firstedge; element != null; element = element.next) {
				    if (element.marker == mark)
					drawEdge(element, fg, bg, (Graphics2D)g);
				}
				break;

			    case DATA_GRAPH_NODE:
				for (element = firstnode; element != null; element = element.next) {
				    if (element.marker == mark)
					drawNode(element, fg, bg, 1.0, (Graphics2D)g);
				}
				break;
			}
		    }
		}
		g.setColor(color);
		g.setFont(font);
	    }
	    g.translate(viewport.x, viewport.y);
	    g.setClip(clip);
	}
    }


    final synchronized void
    reset() {

	clearCachedOutlines();
	super.reset();
    }


    final void
    setAlive(boolean state) {

	alive = state;
    }


    final synchronized void
    setAttachedEdgeSelection(YoixObject yobj) {

	GraphData  edges[];
	GraphData  node;
	GraphData  edge;
	boolean    needpaint;
	boolean    filtered;
	boolean    newmode;
	int        n;

	newmode = yobj.booleanValue();
	if (newmode != attachededgeselection) {
	    needpaint = false;
	    if (newmode) {
		//
		// When turning the feature on, go through and deselect any
		// edges attached to a deselected node; this behavior seems
		// most likely to be what users would want -- only other
		// reasonable alternative is to do nothing.
		//
		for (node = firstnode; node != null; node = node.next) {
		    filtered = node.isFiltered();
		    if (!node.selected || filtered) {
			if ((edges = findAttachedEdges(node)) != null) {
			    for (n = 0; n < edges.length; n++) {
				if ((edge = edges[n]) != null) {
				    if (!node.selected && edge.selected) {
					edge.selected = false;
					edge.repaint = true;
					needpaint = true;
				    }
				    if (filtered && !edge.filtered) {
					edge.filtered = true;
					edge.repaint = true;
					needpaint = true;
				    }
				}
			    }
			}
		    }
		}
	    } else {
		for (edge = firstedge; edge != null; edge = edge.next) {
		    if (edge.filtered) {
			edge.filtered = false;
			edge.repaint = true;
			needpaint = true;
		    }
		}
	    }
	    attachededgeselection = newmode;
	    if (needpaint)
		repaintViewer();
	}
    }


    public final void
    setBounds(int x, int y, int width, int height) {

	Point2D  center;

	//
	// NOTE - older versions of this method were synchronized, but
	// that left open the possibility of deadlock because official
	// Java methods (e.g., BorderLayout.layoutContainer()) grab the
	// AWTTreeLock defined in Component.java and can eventually end
	// up getting here when they decide to change the size of this
	// component. Anyway, it looks like eliminating the possibility
	// of deadlock means no synchronization here, and since we still
	// call zoomToData() we really haven't completely eliminate the
	// possibility yet. Removing synchronized from this method is an
	// improvement that we suspect will be sufficient even though
	// there's still a small chance of deadlock.
	//

	center = lastcenter;		// snapshot - just to be safe

	if (totalsize.width != width || totalsize.height != height) {
	    totalsize.width = width;
	    totalsize.height = height;
	}

	super.setBounds(x, y, width, height);
	setViewportSize();
	setDataBBox();
	setExtent();
	eventbbox.setRect(insets.left, insets.right, viewport.width, viewport.height);
	zoomToData(center);		// deadlock may still be possible here??
    }


    final void
    setClickRadius(int radius) {

	clickradius2 = (radius > 0) ? radius*radius : 0;
    }


    final synchronized void
    setEdgeFlags(int flags) {

	if (edgeflags != flags) {
	    edgeflags = flags;
	    hidemode = ((nodeflags&NODEHIDEDESELECTED)!=0 || (edgeflags&EDGEHIDEDESELECTED)!=0);
	    reset();
	}
    }


    final synchronized void
    setEdgeScale(double scale) {

	if (edgescale != scale) {
	    edgescale = Math.max(scale, 0.0);
	    currentedgestroke = null;
	    currentedgepattern = null;
	    currentedgewidth = 0;
	    resetEdgeWidths();
	    reset();
	}
    }


    final synchronized void
    setFillModel(int model) {

	switch (model) {
	    case YOIX_BOTTOM:
	    case YOIX_LEFT:
	    case YOIX_NONE:
	    case YOIX_RIGHT:
	    case YOIX_TOP:
		break;

	    default:
		model = YOIX_NONE;
		break;
	}

	if (fillmodel != model) {
	    fillmodel = model;
	    reset();
	}
    }


    final synchronized void
    setFontScale(double scale) {

	if (fontscale != scale) {
	    fontscale = Math.max(scale, 0.0);
	    reset();
	}
    }


    final void
    setGraphLayoutArg(YoixObject obj) {

	graphlayoutarg = obj;
    }


    final void
    setGraphLayoutModel(int model) {

	graphlayoutmodel = new Integer(model);
    }


    final void
    setGraphLayoutModel(String model) {

	graphlayoutmodel = (model != null) ? new String(model) : null;
    }


    final void
    setGraphLayoutSort(YoixObject obj) {

	YoixObject  element;
	int         tmp[] = new int[] {VL_SORT_TEXT, VL_SORT_TEXT};

	if (obj.notNull()) {
	    if (obj.isArray()) {
		if ((element = obj.getObject(0)) != null && element.isInteger())
		    tmp[0] = element.intValue();
		if ((element = obj.getObject(1)) != null && element.isInteger())
		    tmp[1] = element.intValue();
	    } else if (obj.isInteger())
		tmp[0] = obj.intValue();
	}
	graphlayoutsort = tmp;
    }


    final synchronized void
    setLineWidth(double width) {

	if (linewidth != width) {
	    linewidth = Math.max(width, 0.0);
	    currentedgestroke = null;
	    currentedgepattern = null;
	    currentedgewidth = 0;
	    currentnodestroke = null;
	    currentnodewidth = 0;
	    currentmarkstroke = null;
	    currentmarkwidth = 0;
	    resetEdgeWidths();
	    reset();
	}
    }


    final synchronized void
    setMoved(YoixObject obj) {

	GraphData  element;
	boolean    needpaint = false;
	int        n;

	if (obj.isNull()) {
	    needpaint = false;
	    for (n = 0; n < totalcount; n++) {
		if ((element = graph[n]) != null) {
		    if (element.isNode()) {
			if (element.original_values != null) {
			    element.unextendElement();
			    element.moveNodeHome(null);
			    needpaint = true;
			}
		    }
		}
	    }
	    if (needpaint) {
		sliceGraph();
		reset();
	    }
	}
    }


    final synchronized void
    setNodeFlags(int flags) {

	if (nodeflags != flags) {
	    nodeflags = flags;
	    hidemode = ((nodeflags&NODEHIDEDESELECTED)!=0 || (edgeflags&EDGEHIDEDESELECTED)!=0);
	    reset();
	}
    }


    final synchronized void
    setNodeOutline(double alpha) {

	alpha = Math.max(0.0, Math.min(alpha, 1.0));
	if (nodeoutline != alpha) {
	    nodeoutline = alpha;
	    reset();
	}
    }


    final synchronized void
    setNodeScale(double scale) {

	if (nodescale != scale) {
	    nodescale = Math.max(scale, 0.0);
	    sliceGraph();
	    reset();
	}
    }


    protected final synchronized void
    setOrigin(Point point) {

	Point2D  center;

	super.setOrigin(point);
	screenmatrix.setToTranslation(viewport.x - insets.left, viewport.y - insets.top);
	try {
	    screeninverse = screenmatrix.createInverse();
	}
	catch (NoninvertibleTransformException e) {
	    screeninverse = new AffineTransform();
	}
	if (graphinverse != null) {
	    center = new Point2D.Double(
		viewport.x + viewport.width/2,
		viewport.y + viewport.height/2
	    );
	    lastcenter = graphinverse.transform(center, null);
	} else lastcenter = null;
    }


    final synchronized void
    setOutlineCacheSize(int size) {

	if (size > 0) {
	    if (outlinecache == null || outlinecache.length != size) {
		outlinecache = new Object[size];
		lastoutlinehit = 0;
	    }
	} else outlinecache = null;
    }


    final synchronized void
    setPaintOrder(YoixObject obj) {

	boolean  newvalue;
	boolean  differ = false;
	int      order[] = {DATA_GRAPH_EDGE, DATA_GRAPH_NODE};
	int      tmporder[];
	int      value;
	int      l;
	int      m;
	int      n;

	if (obj != null && obj.notNull()) {
	    for (n = obj.offset(), m = 0; n < obj.length() && m < order.length; n++) {
		value = obj.getInt(n, order[m]);
		switch (value) {
		    case DATA_GRAPH_EDGE:
		    case DATA_GRAPH_NODE:
			break;

		    default:
			continue;
		}
		newvalue = true;
		for (l = 0; l < m; l++) {
		    if (order[l] == value) {
			newvalue = false;
			break;
		    }
		}
		if (newvalue) {
		    order[m++] = value;
		    if (paintorder.length < m || value != paintorder[m-1])
			differ = true;
		}
	    }
	} else {
	    m = order.length;
	    if (paintorder.length == order.length) {
		for (n = 0; n < paintorder.length; n++) {
		    if (paintorder[n] != order[n]) {
			differ = true;
			break;
		    }
		}
	    } else differ = true;
	}
	if (m > 0 && differ) {
	    if (m != order.length) {
		tmporder = new int[m];
		System.arraycopy(order, 0, tmporder, 0, m);
		order = tmporder;
	    }
	    paintorder = order;
	    for (l = -1, m = -1, n = 0; n < paintorder.length; n++) {
		switch (paintorder[n]) {
		    case DATA_GRAPH_EDGE:
			l = n;
			break;

		    case DATA_GRAPH_NODE:
			m = n;
			break;
		}
	    }
	    reset();
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
    setSelectFlags(int flags) {

	selectflags = flags & DATA_GRAPH_MASK;
    }


    final synchronized void
    setSelectWidth(double width) {

	selectwidth = Math.max(width, 0.0);
	resetEdgeWidths();
    }


    final void
    setSeparator(String str) {

	separator = str;
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
		moveTip(point);
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


    final void
    setZoomDirection(int value) {

	zoomdirection = value;
    }


    final synchronized void
    setZoomScale(double value) {

	Point2D  center;

	value = pickZoomScale(value);
	if (value != zoomscale) {
	    center = lastcenter;
	    zoomscale = value;
	    zoomToViewport(center);
	}
    }


    final synchronized void
    zoom(double scale) {

	zoom(scale, new Point2D.Double(viewport.width/2.0, viewport.height/2.0));
    }


    final synchronized void
    zoom(double scale, Point2D lock) {

	AffineTransform  matrix;
	Rectangle2D      rect;
	Point2D          point;
	double           x0;
	double           y0;
	double           x1;
	double           y1;

	if (scale > 0 && zoomscale > 0) {
	    if ((lock = transformScreenToData(lock)) != null) {
		point = transformDataToViewport(lastcenter);
		rect = new Rectangle2D.Double(
		    point.getX() - viewport.width/2.0,
		    point.getY() - viewport.height/2.0,
		    viewport.width,
		    viewport.height
		);
		if ((rect = transformViewportToData(rect)) != null) {
		    scale = pickZoomScale(scale);
		    matrix = new AffineTransform();
		    matrix.translate(lock.getX(), lock.getY());
		    matrix.scale(zoomscale/scale, zoomscale/scale);
		    matrix.translate(-lock.getX(), -lock.getY());

		    x0 = rect.getX();
		    y0 = rect.getY();
		    x1 = rect.getX() + rect.getWidth();
		    y1 = rect.getY() + rect.getHeight();
		    point = matrix.transform(new Point2D.Double(x0, y0), null);
		    x0 = point.getX();
		    y0 = point.getY();
		    point = matrix.transform(new Point2D.Double(x1, y1), null);
		    x1 = point.getX();
		    y1 = point.getY();
		    rect = new Rectangle2D.Double(x0, y0, x1 - x0, y1 - y0);

		    zoomToDataRectangle(rect);
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
    addOtherNode(HashMap map) {

	StringBuffer  sbuf;
	GraphData     element;
	int           width;
	int           height;
	int           x;
	int           y;

	if ((element = (GraphData)map.get(OTHERNAME)) != null) {
	    if (element.getElementBounds() == null) {
		if (databbox_loaded != null) {
		    width = (int)(databbox_loaded.getWidth()/20);
		    height = (int)(databbox_loaded.getHeight()/200);
		    x = (int)(databbox_loaded.getX() + databbox_loaded.getWidth()/2);
		    y = (int)(databbox_loaded.getY() + databbox_loaded.getHeight() + databbox_loaded.getHeight()/50);
		} else {
		    width = 200;
		    height = 20;
		    x = 0;
		    y = 0;
		}
		sbuf = new StringBuffer();
		sbuf.append(OTHERNAME);
		sbuf.append(' ');
		sbuf.append(13);
		sbuf.append(" P 2 ");
		sbuf.append(x);
		sbuf.append(' ');
		sbuf.append(y);
		sbuf.append(' ');
		sbuf.append(width);
		sbuf.append(' ');
		sbuf.append(height);
		sbuf.append(" t ");
		sbuf.append(OTHERNAME.length());
		sbuf.append(" -");
		sbuf.append(OTHERNAME);
		element.separateText(sbuf.toString(), " ");
	    }
	}
    }


    private void
    buildViewer(HashMap map, ArrayList elements, ArrayList backgrounds) {

	GraphData  element;
	int        n;

	addOtherNode(map);
	selectedcount = 0;
	totalcount = elements.size();
	graph = new GraphData[totalcount];
	attachededges = new HashMap();
	attachednodes = new HashMap();
	firstnode = null;
	lastnode = null;
	firstedge = null;
	lastedge = null;
	for (n = 0; n < totalcount; n++) {
	    if ((element = (GraphData)elements.get(n)) != null) {
		selectedcount += element.selected ? 1 : 0;
		switch (element.element_type) {
		    case DATA_GRAPH_NODE:
			if (lastnode != null) {
			    lastnode.next = element;
			    element.prev = lastnode;
			    element.next = null;
			} else {
			    element.prev = null;
			    element.next = null;
			    firstnode = element;
			}
			lastnode = element;
			nodecount++;
			break;

		    case DATA_GRAPH_EDGE:
			if (lastedge != null) {
			    lastedge.next = element;
			    element.prev = lastedge;
			    element.next = null;
			} else {
			    element.prev = null;
			    element.next = null;
			    firstedge = element;
			}
			lastedge = element;
			edgecount++;
			break;
		}
	    }
	    graph[n] = element;
	}
	raiseAllMarks();
	sortGraph(VL_SORT_TOTAL, true);
	viewermap = map;
	colorViewer();
	setDataBBox();
	setExtent();
	setZoomLimit();

	if ((backgroundcount = backgrounds.size()) > 0) {
	    graphbackgrounds = new GraphData[backgroundcount];
	    for (n = 0; n < backgroundcount; n++)
		graphbackgrounds[n] = (GraphData)backgrounds.get(n);
	}
    }


    private String
    callTipHelper(YoixObject funct, GraphData element) {

	YoixObject  argv[];
	YoixObject  obj;
	String      text = "";

	//
	// Intentionally returns the empty string (not null) when there's
	// no good answer so we aren't repeatedly called.
	//
	// Not sure what the argument or arguments should be, so for now
	// we just support the zero argument version. Not a particularly
	// useful function, but we can easily expand things later.
	//

	if (funct != null) {
	    if (funct.isCallable()) {
		if (funct.callable(0))
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


    private void
    clearCachedOutlines() {

	int  n;

	if (outlinecache != null) {
	    for (n = 0; n < outlinecache.length; n++)
		outlinecache[n] = null;
	    lastoutlinehit = 0;
	}
    }


    private synchronized Object
    clearCachedTips(Object oldgenerator, Object newgenerator) {

	int  n;

	if (graph != null) {
	    if (newgenerator != oldgenerator) {
		if (newgenerator == null || newgenerator.equals(oldgenerator) == false) {
		    for (n = 0; n < graph.length; n++) {
			graph[n].tipprefix = null;
			graph[n].tipsuffix = null;
		    }
		}
	    }
	}
	return(newgenerator);
    }


    private void
    clearViewer() {

	int  n;

	totalcount = 0;			// probably should be first
	selectedcount = 0;
	backgroundcount = 0;
	edgecount = 0;
	nodecount = 0;
	palettecontrol_edges = null;
	palettecontrol_nodes = null;
	maxnodetotal = 0.0;
	maxedgetotal = 0.0;
	graph = null;
	graphbackgrounds = null;
	attachededges = null;
	attachednodes = null;
	firstnode = null;
	lastnode = null;
	firstedge = null;
	lastedge = null;
	viewermap = null;
	colormap = null;
	datarecords = null;
	loadeddata = null;
	databbox_loaded = null;
	databbox = null;
	activepalette = null;

	namemap = null;
	indexmap = null;

	graphmatrix = null;
	graphinverse = null;
	graphextent = null;
	currentedgestroke = null;
	currentedgepattern = null;
	currentedgewidth = 0;
	currentnodestroke = null;
	currentnodewidth = 0;
	currentmarkstroke = null;
	currentmarkwidth = 0;

	xslices = null;
	yslices = null;

	screenmatrix = new AffineTransform();
	screeninverse = new AffineTransform();

	graphscale = 1.0;
	zoomscale = 1.0;
	zoomlimit = ZOOMLIMIT;
	smallestfont = null;
	lastcenter = null;

	markerpad = new Insets(0, 0, 0, 0);

	setExtent();
    }


    private Font
    deriveFont(Font font) {

	Rectangle2D  bounds;
	double       width;
	double       height;
	double       scale;
	double       vmscale;
	double       sx;
	double       sy;

	//
	// The adjustments made to sx and sy are a bit kludgy and far from
	// perfect, but seem to help. Previous implementations omitted the
	// sx and sy adjustments, which seemed to result in more frequent
	// occurrences of text labels that were a bit too long for nodes.
	//

	if (font != null || (font = getFont()) != null) {
	    vmscale = VM.getFontScale();
	    if (graphmatrix != null) {
		sx = fontscale*graphmatrix.getScaleX()/vmscale;
		sy = fontscale*graphmatrix.getScaleY()/vmscale;
	    } else {
		sx = 1.0/vmscale;
		sy = 1.0/vmscale;
	    }

	    //
	    // These are the kludges mentioned above that sometimes help
	    // fit labels to nodes when node dimensions and label bounds
	    // have been determined externally by graphviz. Omit them and
	    // you many graphs will still display properly, but some will
	    // have problems with labels that are too big for nodes.
	    //

	    bounds = font.getMaxCharBounds(FONTCONTEXT);
	    width = bounds.getWidth();
	    height = bounds.getHeight();
	    sx = ((int)(sx * Math.floor(width)))/Math.ceil(width);
	    sy = ((int)(sy * Math.floor(height)))/Math.ceil(height);
	    scale = Math.min(.9*sx, .9*sy);

	    font = font.deriveFont(AffineTransform.getScaleInstance(scale, scale));
	}
	return(font);
    }


    private synchronized void
    dragBegin(Point p, int style) {

	GeneralPath  paths[];
	Rectangle2D  dirty = null;
	Rectangle2D  dbounds;
	Rectangle    bounds;
	GraphData    edges[];
	GraphData    nodes[];
	GraphData    element;
	GraphData    edge;
	Point2D      point;
	Shape        shape;
	int          index;
	int          n;

	//
	// Currently only allow nodes, but we could drag edges too.
	//

	if (dragobjects == null && draggraphics == null) {
	    if ((index = findNextElement(p, eventbbox, DATA_GRAPH_NODE)) >= 0) {
		if ((element = graph[index]) != null) {
		    if ((nodeflags & NODEHIDE) == 0 && !element.invisible && !element.hidden && (style == 0 || element.original_values != null) && !element.ismark) {
			dragelement = element;
			dragelement.dragging = true;
			dragorigin = dragelement.getCurrentOrigin(null);
			draggraphics = (Graphics2D)getGraphics();
			draggraphics.setColor(dragcolor);
			draggraphics.setXORMode(getBackground());
			draggraphics.setStroke(new BasicStroke());		// simple, fast stroke. reset?
			if ((edges = findAttachedEdges(dragelement)) != null) {
			    dragobjects = new Object[edges.length + 1];
			    for (n = 0; n < edges.length; n++) {
				if ((edge = edges[n]) != null) {
				    edge.dragging = true;	// so we can hide them temporarily
				    edge.repaint = true;	// since they will be hidden
				    if ((dbounds = edge.getScaledBounds()) != null) {
					if (dirty == null)
					    dirty = dbounds;	// was edge.getCurrentBounds()???
					else dirty.add(dbounds);
				    }
				    if ((nodes = findAttachedNodes(edge)) != null) {
					if (nodes[0] != nodes[1] && nodes[0] != null && nodes[1] != null) {
					    if (nodes[0] == dragelement)
						dragobjects[n+1] = transformDataToScreen(nodes[1].getElementCenter());
					    else dragobjects[n+1] = transformDataToScreen(nodes[0].getElementCenter());
					}
				    }
				}
			    }
			    if (dirty != null) {
				dragelement.repaint = true;
				repaintDataRect(dirty);
			    }
			    draglastpoint = p;
			    dragstyle = style;
			} else dragobjects = new Object[1];
			element = new GraphData(dragelement);		// make a copy
			dragobjects[0] = element;			// save it
			paths = element.element_paths;
			for (n = 0; n < paths.length; n++) {
			    shape = screeninverse.createTransformedShape(graphmatrix.createTransformedShape(paths[n]));
			    if (shape instanceof GeneralPath)	// probably always true
				paths[n] = (GeneralPath)shape;
			    else paths[n] = new GeneralPath(shape);
			}
			element.element_paths = paths;
			element.element_bounds = null;
			element.element_position = null;
			if ((dbounds = element.getElementBounds()) != null) {
			    dragstyle = style;
			    if (dragoffset == null) {
				bounds = dbounds.getBounds();
				dragoffset = new Point(p.x - bounds.x, p.y - bounds.y);
			    }
			    dragelement.repaint = true;
			    pressedstart = dragelement.pressed;
			    if (style != 0) {
				point = graphinverse.transform(screenmatrix.transform(p, null), null);
				if (dragelement.getOriginalBounds().contains(point)) {
				    dbounds = transformDataToScreen(dragelement.getOriginalBounds());
				    p = new Point((int)(dbounds.getX()) + dragoffset.x, (int)(dbounds.getY()) + dragoffset.y);
				}
				dragelement.pressed = !pressedstart;
				dragelement.extendElement();
				sliceGraph();
				repaintViewer();
				dragNodeTo(p);
			    } else {
				dragelement.pressed = !pressedstart;
				repaintViewer();
			    }
			    //
			    // Old version did
			    //
			    //    repaintXORElements(dragobjects, draggraphics);
			    //
			    // here, but it was a bit sloppy and often left
			    // XOR line segements lying around in the node
			    // being moved. Probably not hard to fix, but
			    // just skipping the initial drawing is OK for
			    // now. User gets appropriate feedback as soon
			    // as the mouse is moved.
			    //
			    draglastpoint = p;
			} else {		// unlikely but looks possible
			    if (edges != null) {
				for (n = 0; n < edges.length; n++)
				    edges[n].dragging = false;
			    }
			    draggraphics = null;
			    dragelement = null;
			    dragdragged = false;
			    dragorigin = null;
			    draglastpoint = null;
			    dragpoint = null;
			    dragobjects = null;
			    dragoffset = null;
			    dragstyle = 0;
			}
		    }
		}
	    }
	}
    }


    private synchronized void
    dragDragged(Point p) {

	Rectangle2D  bounds;
	Point2D      point;

	if (dragobjects != null && draggraphics != null) {
	    if (dragdragged)
		repaintXORElements(dragobjects, draggraphics);
	    if (dragstyle != 0) {
		point = graphinverse.transform(screenmatrix.transform(p, null), null);
		if (dragelement.elementContains(point) && dragelement.getOriginalBounds().contains(point)) {
		    bounds = transformDataToScreen(dragelement.getOriginalBounds());
		    p = new Point((int)(bounds.getX()) + dragoffset.x, (int)(bounds.getY()) + dragoffset.y);
		}
	    }
	    dragNodeTo(p);
	    dragdragged = true;
	    repaintXORElements(dragobjects, draggraphics);
	    draglastpoint = p;
	}
    }


    private synchronized void
    dragEnd(Point p) {

	Rectangle2D  dbounds;
	Rectangle    bounds;
	GraphData    edges[];
	GraphData    edge;
	Point2D      origin;
	int          n;

	if (dragobjects != null) {
	    if (draggraphics != null) {
		if (dragdragged)
		    repaintXORElements(dragobjects, draggraphics);
		draggraphics.setPaintMode();
		if (dragelement != null) {
		    dragelement.dragging = false;
		    if ((edges = findAttachedEdges(dragelement)) != null) {
			for (n = 0; n < edges.length; n++) {
			    if ((edge = edges[n]) != null) {
				edge.dragging = false;
				edge.repaint = true;	// just to be sure
			    }
			}
		    }
		    dragelement.pressed = pressedstart;
		    if (dragstyle == 0) {
			p.x -= dragoffset.x;
			p.y -= dragoffset.y;
			origin = graphinverse.transform(screenmatrix.transform(p, null), null);
			dragelement.moveNodeTo(origin, draggraphics);
		    } else {
			dbounds = dragelement.getExtendedBounds();
			origin = graphinverse.transform(screenmatrix.transform(p, null), null);
			dragelement.unextendElement();
			sliceGraph();
			repaintDataRect(dbounds);
			if (dragelement.getOriginalBounds().contains(origin))
			    dragelement.moveNodeHome(draggraphics);
			if (dragobjects.length > 1)
			    repaintViewer();		// for edges
		    }
		}
		draggraphics.dispose();
	    }
	    draggraphics = null;
	    dragelement = null;
	    dragdragged = false;
	    dragorigin = null;
	    draglastpoint = null;
	    dragpoint = null;
	    dragobjects = null;
	    dragoffset = null;
	    dragstyle = 0;
	}
    }


    final void
    dragNodeTo(Point2D origin) {

	AffineTransform  matrix;
	GeneralPath      paths[];
	GeneralPath      path;
	GeneralPath      original;
	GraphData        element;
	Shape            shape;
	double           tx;
	double           ty;
	int              n;

	//
	// Calculations are like moveNodeTo, but understood to be transitory,
	// and everything is already in screen coords
	//

	dragdragged = false;
	if (origin != null && dragobjects != null && (element = (GraphData)dragobjects[0]) != null) {
	    if (draglastpoint != null) {
		tx = origin.getX() - draglastpoint.getX();
		ty = origin.getY() - draglastpoint.getY();
		if (tx != 0 || ty != 0) {
		    matrix = new AffineTransform();
		    matrix.setToTranslation(tx, ty);
		    if ((paths = element.element_paths) != null) {
			original = new GeneralPath();
			for (n = 0; n < paths.length; n++) {
			    if ((path = paths[n]) != null) {
				original.append(path, false);
				shape = path.createTransformedShape(matrix);
				if (shape instanceof GeneralPath)	// probably always true
				    paths[n] = (GeneralPath)shape;
				else paths[n] = new GeneralPath(shape);
			    }
			}
		    }
		    element.element_position = null;
		    element.element_bounds = null;
		}
	    }
	}
    }


    private void
    drawBackground(Color fg, Color bg, Graphics2D g) {

	GraphData  element;
	int        n;

	for (n = 0; n < backgroundcount; n++) {
	    if ((element = graphbackgrounds[n]) != null) {
		switch (element.element_type) {
		    case DATA_BACKGROUND_EDGE:
			drawBackgroundEdge(element, fg, g);
			break;

		    case DATA_BACKGROUND_NODE:
			drawBackgroundNode(element, fg, bg, g);
			break;
		}
	    }
	}
    }


    private void
    drawBackgroundEdge(GraphData element, Color fg, Graphics2D g) {

	Shape  shapes[];

	if (!element.invisible && !element.dragging && !element.hidden()) {
	    if (element.hidden == false) {
		if ((shapes = element.getDrawShapes(graphmatrix, true)) != null) {
		    g.setColor(element.pickEdgeColor(fg, null));
		    g.setStroke(getCurrentEdgeStroke(element.linewidth, element.dasharray));
		    drawEdgeShapes(shapes, g);
		    //
		    // Temporarily skipping the label, if there is one.
		    //
		}
	    } else repaintDataRect(element.getCurrentBounds());
	}
    }


    private void
    drawBackgroundNode(GraphData element, Color fg, Color bg, Graphics2D g) {

	Shape  shapes[];
	Color  interior;
	Color  outline;

	if (!element.invisible && !element.hidden()) {
	    if (element.hidden == false) {
		if ((shapes = element.getDrawShapes(graphmatrix, true)) != null) {
		    if (element.filled)
			interior = element.pickFillColor(bg, null);
		    else interior = bg;
		    outline = element.pickPenColor(fg, null, element.filled);
		    //
		    // The setStroke() call should be unnecessary if we're
		    // not drawing the outline (i.e., the outline color is
		    // null).
		    //
		    g.setStroke(getCurrentStroke(element));
		    drawNodeShapes(shapes, interior, outline, false, false, 1.0, g);
		    //
		    // Changed on 5/25/07 - old code just did
		    //
		    //    g.setColor(element.label_color != null ? element.label_color : bg);
		    //
		    // but using bg seemed wrong, so we switched to fg, but
		    // didn't run any tests.
		    //	
		    if (element.label_color == null) {
			if ((labelflags&LABEL_PICKCOLORNODE) != 0)
			    g.setColor(YoixMiscJFC.pickForeground(fg, bg, interior));
			else g.setColor(fg);
		    } else g.setColor(element.label_color);
		    drawLabel(element, g);
		}
	    } else repaintDataRect(element.getCurrentBounds());
	}
    }


    private void
    drawEdge(GraphData element, Color fg, Color bg, Graphics2D g) {

	double  pattern[];
	double  value;
	double  total;
	Shape   shapes[];
	Color   color;

	if ((edgeflags & EDGEHIDE) == 0 && !element.invisible && !element.dragging && !element.hidden()) {
	    if (element.hidden == false) {
		if ((shapes = element.getDrawShapes(graphmatrix, true)) != null) {
		    pattern = element.dasharray;
		    value = element.getActiveValue();
		    total = element.getActiveTotal();
		    if (element.selected == false || element.filtered == true) {
			color = emptycolor;
			if ((edgeflags & EDGETOGGLE) != 0) {
			    if (shapes[0] != null) {
				g.setColor(bg);
				g.setStroke(getCurrentEdgeStroke(element.linewidth, pattern));
				g.draw(shapes[0]);
				pattern = (pattern == null) ? new double[] {5, 2} : null;
			    }
			}
		    } else {
			color = (value > 0) ? element.pickEdgeColor(fg, currentpalette) : emptycolor;
			if ((edgeflags & EDGETOGGLE) != 0) {
			    if (shapes[0] != null) {
				g.setColor(bg);
				g.setStroke(getCurrentEdgeStroke(element.linewidth, null));
				g.draw(shapes[0]);
			    }
			}
		    }
		    if (element != pressedelement || element.pressed == pressedstart) {
			if (element.highlight)
			    color = pickHighlightColor(color);
			else if (element.pressed)
			    color = pickPressedColor(color);
		    } else color = pickPressingColor(color);
		    g.setColor(color);
		    g.setStroke(getCurrentEdgeStroke(element.linewidth, pattern));
		    drawEdgeShapes(shapes, g);
		}
		if ((labelflags&LABEL_HIDEEDGE) == 0) {
		    if (element.label_color == null)
			g.setColor(fg);
		    else g.setColor(element.label_color);
		    drawLabel(element, g);
		}
	    } else repaintDataRect(element.getCurrentBounds());
	}
    }


    private void
    drawEdgeShapes(Shape shapes[], Graphics2D g) {

	Shape  shape;
	int    n;

	//
	// We assume that edge heads and tails are always designed to be
	// filled and stroked, which may not be 100% correct. Seemed to
	// behave well for the test files that we looked at. If you make
	// changes be sure to test edge scaling. The code that builds the
	// GraphData selection outline will probably also need adjusting.
	//

	for (n = 0; n < shapes.length; n++) {
	    if ((shape = shapes[n]) != null) {
		if (n != 0)
		    g.fill(shape);
		g.draw(shape);
	    }
	}
    }


    private void
    drawLabel(GraphData element, Graphics2D g) {

	Rectangle2D  rect;
	TextLayout   layout;
	Point2D      start;
	double       x;
	double       y;
	Font         labelfont;
	Font         font;
	int          n;

	//
	// Now assumes the font only needs to be changed when the element's
	// font entry is not null, which obviously means g's font must be an
	// appropriately scaled instance of the default font. Changes got us
	// a significant performance improvement and were pretty easy.
	//
	// Some of this is duplicated in getLabelBounds(), so eventually see
	// if we can do something about it.
	//

	if (element.font != null) {
	    font = g.getFont();
	    g.setFont(deriveFont(element.font));
	} else font = null;

	labelfont = g.getFont();

	for (n = 0; (layout = element.getLabelLayout(n, labelfont)) != null; n++) {
	    start = graphmatrix.transform(element.getLabelPoint(n), null);
	    rect = layout.getBounds();
	    switch (element.getLabelJustification(n)) {
		case -1:
		    x = start.getX();
		    y = start.getY();
		    break;

		case 1:
		    x = start.getX() - rect.getWidth() - rect.getX();
		    y = start.getY();
		    break;

		default:
		    x = start.getX() - rect.getWidth()/2.0 - rect.getX();
		    y = start.getY();
		    break;
	    }
	    layout.draw(g, (float)x, (float)y);
	}

	if (font != null)
	    g.setFont(font);
    }


    private void
    drawNode(GraphData element, Color fg, Color bg, double alpha, Graphics2D g) {

	Composite  composite;
	boolean    selected;
	boolean    selected_label;
	boolean    filled;
	boolean    highlight;
	boolean    pressed;
	boolean    plaintext;
	double     value;
	double     total;
	double     fraction;
	Shape      shapes[];
	Color      interior;
	Color      outline;

	//
	// Added selected_label on 8/1/07 and use to pick a branch when we're
	// trying to pick a color for the nodes label. Right now the choice is
	// only based on activefields, which seems reasonable, but we also may
	// want a way control the selection from Yoix scripts. Also think the
	// selection control should match how findAllElements chooses elements.
	//

	if ((nodeflags & NODEHIDE) == 0 && !element.invisible && !element.hidden()) {
	    if (element.hidden == false) {
		if (alpha < 1.0) {
		    composite = g.getComposite();
		    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha));
		} else composite = null;
		if ((shapes = element.getDrawShapes(graphmatrix, true)) != null) {
		    selected = element.notEmpty();
		    selected_label = (activefieldcount > 1) ? selected : element.selected;	// added on 8/1/07
		    value = element.getActiveValue();
		    total = element.getActiveTotal();
		    filled = element.filled;
		    highlight = element.highlight;
		    pressed = element.pressed;
		    plaintext = element.plaintext;
		    outline = fg;
		    interior = bg;
		    g.setStroke(getCurrentStroke(element));
		    if (element != pressedelement || element.pressed == pressedstart) {
			if (selected == false || value == 0 || (value != total && fillmodel != YOIX_NONE)) {
			    if (filled) {
				interior = emptycolor;
				outline = element.pickPenColor(fg, null, filled);
			    } else {
				interior = emptycolor;
				outline = selected ? element.pickPenColor(fg, null, filled) : emptycolor;
			    }
			    drawNodeShapes(shapes, interior, outline, highlight, pressed, 1.0, g);
			}
			if (selected && (value > 0 || value == total)) {
			    fraction = (value < total) ? value/total : 1.0;
			    if (filled)
				interior = element.pickFillColor(bg, currentpalette);
			    else interior = bg;
			    outline = element.pickPenColor(fg, currentpalette, filled);
			    drawNodeShapes(shapes, interior, outline, highlight, pressed, fraction, g);
			}
		    } else {
			interior = pickPressingColor(element.pickFillColor(bg, currentpalette));
			outline = fg;
			drawNodeShapes(shapes, interior, outline, false, false, 1.0, g);
		    }
		    if ((labelflags&LABEL_HIDENODE) == 0) {
			if (selected_label) {
			    if (element.label_color == null) {
				if ((labelflags&LABEL_PICKCOLORNODE) != 0)
				    g.setColor(YoixMiscJFC.pickForeground(fg, bg, interior));
				else g.setColor(fg);
			    } else g.setColor(element.label_color);
			} else g.setColor(bg);
			drawLabel(element, g);
		    }
		} else if (element.label_text != null) {
		    if ((outline = element.color) == null) {
			if ((outline = element.label_color) == null)
			    outline = fg;
		    }
		    if (element.pressed == false) {
			selected = element.selected;
			value = element.getActiveValue();
			total = element.getActiveTotal();
			if (selected == false || value != total) {
			    g.setColor(emptycolor);
			    drawLabel(element, g);
			} else {
			    g.setColor(outline);
			    drawLabel(element, g);
			}
		    } else {
			g.setColor(pickPressedColor(outline));
			drawLabel(element, g);
		    }
		}
		if (composite != null)
		    g.setComposite(composite);
	    } else repaintDataRect(element.getCurrentBounds());
	}
    }


    private void
    drawNodeShapes(Shape shapes[], Color interior, Color outline, boolean highlight, boolean pressed, double fraction, Graphics2D g) {

	Composite  composite;
	Rectangle  bounds;
	Stroke     stroke;
	Shape      shape;
	Shape      clip;
	float      delta;
	int        n;

	for (n = 0; n < shapes.length; n++) {
	    if ((shape = shapes[n]) != null) {
		if (interior != null) {
		    if (highlight)
			g.setColor(pickHighlightColor(interior));
		    else if (pressed)
			g.setColor(pickPressedColor(interior));
		    else g.setColor(interior);
		    if (fraction < 1.0 && fillmodel != YOIX_NONE && shapes.length == 1) {
			clip = g.getClip();
			if (outline != null) {
			    stroke = g.getStroke();
			    if (stroke instanceof BasicStroke)
				delta = 2*((BasicStroke)stroke).getLineWidth();
			    else delta = 0;
			} else delta = 0;
			switch (fillmodel) {
			    case YOIX_BOTTOM:
				bounds = shape.getBounds();
				bounds.y += (int)(delta/2 + (1.0 - fraction)*(bounds.height - delta));
				g.clip(bounds);
				break;

			    case YOIX_LEFT:
				bounds = shape.getBounds();
				bounds.x -= (int)(delta/2 + (1.0 - fraction)*(bounds.width - delta));
				g.clip(bounds);
				break;

			    case YOIX_RIGHT:
				bounds = shape.getBounds();
				bounds.x += (int)(delta/2 + (1.0 - fraction)*(bounds.width - delta));
				g.clip(bounds);
				break;

			    case YOIX_TOP:
				bounds = shape.getBounds();
				bounds.y -= (int)(delta/2 + (1.0 - fraction)*(bounds.height - delta));
				g.clip(bounds);
				break;
			}
			g.fill(shape);
			g.setClip(clip);
		    } else g.fill(shape);
		}
		if (outline != null && nodeoutline != 0.0) {
		    g.setColor(outline);
		    if (nodeoutline != 1.0) {
			composite = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)nodeoutline));
			g.draw(shape);
			g.setComposite(composite);
		    } else g.draw(shape);
		}
	    }
	}
    }


    private void
    drawSweepBox() {

	Graphics  g;
	Point2D   center;
	int       dx;
	int       dy;

	if (xcorner[0] != xcorner[2] || ycorner[0] != ycorner[2]) {
	    if ((g = getGraphics()) != null) {
		if (mouse == ZOOMING_IN || mouse == ZOOMING_OUT) {
		    g.setColor(mouse == ZOOMING_IN ? zoomincolor : zoomoutcolor);
		    g.setXORMode(getBackground());
		    dx = Math.abs(xcorner[2] - xcorner[0]);
		    dy = Math.abs(ycorner[2] - ycorner[0]);
		    g.drawPolygon(xcorner, ycorner, 4);
		    if ((dx*dx + dy*dy) > zoomdiameter2) {
			center = new Point2D.Double(
			    (xcorner[0] + xcorner[2])/2,
			    (ycorner[0] + ycorner[2])/2
			);
			center = screenmatrix.transform(center, null);
			center = graphinverse.transform(center, null);
			if (databbox != null && databbox.contains(center)) {
			    g.drawLine(xcorner[0], ycorner[0], xcorner[2], ycorner[2]);
			    g.drawLine(xcorner[1], ycorner[1], xcorner[3], ycorner[3]);
			}
		    }
		} else {
		    g.setColor(sweepcolor);
		    g.setXORMode(getBackground());
		    g.drawPolygon(xcorner, ycorner, 4);
		}
		g.dispose();
	    }
	}
    }


    private ArrayList
    findAllElements(Point2D point1, Point2D point2, int types, boolean selected) {

	GraphSlice  sl[];
	GraphData   element;
	ArrayList   elements = null;
	Line2D      line;
	double      dmn;
	double      dmx;
	double      x1;
	double      y1;
	double      x2;
	double      y2;
	Area        linearea;
	int         length;
	int         index;
	int         m;
	int         n;

	//
	// Returns an ArrayList containing the indices of the elements
	// that are touched by the line connecting the two points that
	// aren't already in the state specified by selected. Modifies
	// fields in each element to reflect the new state and indicate
	// that it needs to be repainted. Works with the updateData()
	// methods that are defined elsewhere, which should explain the
	// return value.
	//

	if (graph != null && graphmatrix != null) {
	    point1 = graphinverse.transform(
		screenmatrix.transform(point1, null),
		null
	    );
	    point2 = graphinverse.transform(
		screenmatrix.transform(point2, null),
		null
	    );
	    x1 = point1.getX();
	    y1 = point1.getY();
	    x2 = point2.getX();
	    y2 = point2.getY();
	    if (Math.abs(x1 - x2) < Math.abs(y1 - y2)) {
		sl = xslices;
		dmn = Math.min(x1, x2);
		dmx = Math.max(x1, x2);
	    } else {
		sl = yslices;
		dmn = Math.min(y1, y2);
		dmx = Math.max(y1, y2);
	    }
	    line = new Line2D.Double(x1, y1, x2, y2);
	    linearea = new Area(BRUSHSTROKE.createStrokedShape(line));
	    elements = new ArrayList();
	    for (m = 0; m < SLICES; m++) {
		if (sl[m].intersects(dmn, dmx)) {
		    length = sl[m].members.length;
		    for (n = 0; n < length; n++) {
			index = sl[m].members[n];
			element = graph[index];
			if (element.loaded && (element.element_type & types) != 0) {
			    //
			    // The activefieldcount test was added on 8/1/07,
			    // mostly as a precaution, but it's probably not
			    // necessary because the true branch should be
			    // able to handle all cases.
			    //
			    if (activefieldcount > 1) {
				if (selected) {
				    if (element.notFull()) {
					if (element.elementIntersects(line, linearea))
					    elements.add(new Integer(index));
				    }
				} else {
				    if (element.notEmpty()) {
					if (element.elementIntersects(line, linearea))
					    elements.add(new Integer(index));
				    }
				}
			    } else {
				if (element.selected != selected) {
				    if (element.elementIntersects(line, linearea))
					elements.add(new Integer(index));
				}
			    }
			}
		    }
		}
	    }
	}

	return(elements != null && elements.size() > 0 ? elements : null);
    }


    private synchronized GraphData[]
    findAttachedEdges(GraphData node) {

	YoixGraphElement  graphdata;
	BasicStroke       stroke;
	GeneralPath       paths[];
	GeneralPath       path;
	Rectangle2D       bounds;
	GraphData         nodes[];
	GraphData         edges[] = null;
	GraphData         edge;
	ArrayList         list;
	Point2D           ends[];
	boolean           attached;
	String            nodenames[];
	String            edgenames[];
	String            str;
	Area              boundary;
	Area              area;
	int               n;

	//
	// We're synchronized so it's safe to use the list of edges, but we
	// could just as easily use graph array and only check edges.
	//

	if (attachededges != null && node != null && node.isGraphNode()) {
	    if ((edges = (GraphData[])attachededges.get(node)) == null) {
		if (datamanager != null && (graphdata = datamanager.getGraphElement()) != null) {
		    if ((edgenames = graphdata.listAttachedEdgeNames(node.getElementName())) != null) {
			edges = new GraphData[edgenames.length];
			if (edges.length > 0 && namemap == null) {
			    namemap = new HashMap(graph.length);
			    for (n = 0; n < graph.length; n++)
				namemap.put(graph[n].getElementName(), graph[n]);
			}
			for (n = 0; n < edges.length; n++) {
			    edges[n] = (GraphData)namemap.get(edgenames[n]);
			    if (!attachednodes.containsKey(edges[n])) {
				if ((nodenames = graphdata.listEdgeTailHead(edgenames[n])) != null) {
				    nodes = new GraphData[2];
				    nodes[0] = (GraphData)namemap.get(nodenames[0]);
				    nodes[1] = (GraphData)namemap.get(nodenames[1]);
				} else nodes = new GraphData[] { null, null };
				attachednodes.put(edges[n], nodes);
			    }
			}
			attachededges.put(node, edges);
		    }
		} else {
		    if ((bounds = node.getOriginalBounds()) != null) {
			if ((paths = node.getOriginalPaths()) != null) {
			    stroke = new BasicStroke(2*Math.max(node.linewidth, 1));
			    boundary = new Area();
			    for (n = 0; n < paths.length; n++)
				boundary.add(new Area(stroke.createStrokedShape(paths[n])));

			    list = new ArrayList();
			    for (edge = firstedge; edge != null; edge = edge.next) {
				if (bounds.intersects(edge.getOriginalBounds())) {
				    if ((paths = edge.getOriginalPaths()) != null) {
					if ((ends = YoixMiscGeom.getEndPoints(paths[0])) != null) {
					    //
					    // A little confusing because the position
					    // of corresponding values in paths[] and
					    // ends[] don't match because we assumed
					    // edges have heads more often than tails.
					    //
					    attached = false;
					    for (n = 1; n <= 2 && attached == false; n++) {
						if (paths.length > n && (path = paths[n]) != null) {
						    area = new Area(path);
						    area.add(new Area(stroke.createStrokedShape(path)));
						    area.intersect(boundary);
						    attached = (area.isEmpty() == false);
						} else attached = boundary.contains(ends[n%2]);
					    }
					    if (attached) {
						list.add(edge);
						if ((nodes = (GraphData[])attachednodes.get(edge)) == null) {
						    nodes = new GraphData[2];
						    attachednodes.put(edge, nodes);
						}
						nodes[(n+1)%2] = node;
					    }
					}
				    }
				}
			    }
			    edges = new GraphData[list.size()];
			    for (n = 0; n < edges.length; n++)
				edges[n] = (GraphData)list.get(n);
			    attachededges.put(node, edges);
			}
		    }
		}
	    }
	}
	return(edges);
    }


    private synchronized GraphData[]
    findAttachedNodes(GraphData edge) {

	YoixGraphElement  graphdata;
	BasicStroke       stroke;
	GeneralPath       paths[];
	GeneralPath       path;
	Rectangle2D       bounds;
	GraphData         nodes[] = null;
	GraphData         node;
	Point2D           ends[];
	String[]          nodenames;
	boolean           attached;
	Area              boundary;
	Area              areas[];
	Area              area;
	int               counter;
	int               n;

	//
	// We're synchronized so it's safe to use the list of edges, but we
	// could just as easily use graph array and only check edges.
	//

	if (attachednodes != null && edge != null && edge.isGraphEdge()) {
	    if ((nodes = (GraphData[])attachednodes.get(edge)) == null) {
		if (datamanager != null && (graphdata = datamanager.getGraphElement()) != null) {
		    if (namemap == null) {
			namemap = new HashMap(graph.length);
			for (n = 0; n < graph.length; n++)
			    namemap.put(graph[n].getElementName(), graph[n]);
		    }
		    if ((nodenames = graphdata.listEdgeTailHead(edge.getElementName())) != null) {
			nodes = new GraphData[2];
			nodes[0] = (GraphData)namemap.get(nodenames[0]);
			nodes[1] = (GraphData)namemap.get(nodenames[1]);
		    } else nodes = new GraphData[] {null, null};
		} else nodes = new GraphData[] {null, null};
		attachednodes.put(edge, nodes);
	    }
	    if (nodes[0] == null || nodes[1] == null) {
		if ((bounds = edge.getOriginalBounds()) != null) {
		    if ((paths = edge.getOriginalPaths()) != null) {
			if ((ends = YoixMiscGeom.getEndPoints(paths[0])) != null) {
			    stroke = new BasicStroke(2*Math.max(edge.linewidth, 1));
			    areas = new Area[] {null, null};
			    for (n = 1; n <= 2; n++) {
				if (paths.length > n && (path = paths[n]) != null) {
				    if (nodes[n%2] == null) {
					area = new Area(path);
					area.add(new Area(stroke.createStrokedShape(path)));
					areas[n%2] = area;
				    }
				}
			    }

			    counter = 0;
			    for (n = 0; n < 2; n++) {
				if (nodes[n] != null)
				    counter++;
			    }

			    for (node = firstnode; node != null && counter < 2; node = node.next) {
				if (bounds.intersects(node.getOriginalBounds())) {
				    if ((paths = node.getOriginalPaths()) != null) {
					boundary = new Area();
					for (n = 0; n < paths.length; n++)
					    boundary.add(new Area(stroke.createStrokedShape(paths[n])));

					attached = false;
					for (n = 0; n < 2 && attached == false; n++) {
					    if (nodes[n] == null) {
						if (areas[n] != null) {
						    area = new Area(areas[n]);
						    area.intersect(boundary);
						    attached = (area.isEmpty() == false);
						} else attached = boundary.contains(ends[n%2]);
						if (attached) {
						    nodes[n] = node;
						    counter++;
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
	return(nodes);
    }


    private synchronized int
    findNextElement(Point2D point, Rectangle2D clip, int types, boolean selected) {

	return(findNextElement(point, clip, types, selected, false));
    }


    private synchronized int
    findNextElement(Point2D point, Rectangle2D clip, int types, boolean selected, boolean any) {

	GraphData  element;
	int        members[];
	int        index = -1;
	int        length;
	int        distance;
	int        mindist;
	int        n;

	if (graph != null && graphmatrix != null && types != 0) {
	    if (clip == null || clip.contains(point)) {
		point = graphinverse.transform(
		    screenmatrix.transform(point, null),
		    null
		);
		if ((members = pickMembers(point.getX(), point.getY())) != null) {
		    mindist = Integer.MAX_VALUE;
		    length = members.length;
		    for (n = 0; n < length; n++) {
			element = graph[members[n]];
			if ((any || element.loaded) && (element.element_type & types) != 0) {
			    if (any || element.selected != selected) {
				if (element.elementContains(point)) {
				    distance = 0;
				    while ((element = element.next) != null)
					distance++;
				    if (distance < mindist) {
					index = members[n];
					if ((mindist = distance) == 0)
					    break;
				    }
				}
			    }
			}
		    }
		}
	    }
	}

	return(index);
    }


    private synchronized GraphData
    getCachedOutlineElement(Point2D point) {

	GraphData  element = null;
	Object     value;
	Shape      shape;
	int        length;
	int        count;
	int        n;

	//
	// Might be faster if everything was in screen coordinates - will
	// test later using a "high" resolution world map.
	//

	if (outlinecache != null && graphinverse != null) {
	    if ((length = outlinecache.length) > 0) {
		point = graphinverse.transform(
		    screenmatrix.transform(point, null),
		    null
		);
		for (n = lastoutlinehit, count = length; count > 0; n = (n + 1)%length, count--) {
		    value = outlinecache[n];
		    if (value instanceof Object[]) {
			shape = (Shape)((Object[])value)[1];
			if (shape.contains(point)) {
			    element = (GraphData)((Object[])value)[0];
			    break;
			}
		    }
		}
	    }
	}
	return(element);
    }


    private synchronized Shape
    getCachedOutlineShape(GraphData element) {

	Object  value;
	Shape   shape = null;
	int     length;
	int     count;
	int     n;

	if (outlinecache != null) {
	    if ((length = outlinecache.length) > 0) {
		for (n = lastoutlinehit, count = length; count > 0; n = (n + 1)%length, count--) {
		    value = outlinecache[n];
		    if (value instanceof Object[]) {
			if (((Object[])value)[0] == element) {
			    shape = (Shape)((Object[])value)[1];
			    lastoutlinehit = n;
			    break;
			}
		    }
		}
	    }
	}
	return(shape);
    }


    private synchronized BasicStroke
    getCurrentEdgeStroke(double width, double pattern[]) {

	double  coords[];
	float   dasharray[] = null;
	int     linecap = BasicStroke.CAP_SQUARE;
	int     m;
	int     n;

	if (currentedgestroke == null || currentedgewidth != width || currentedgepattern != pattern) {
	    if (graphmatrix != null) {
		width = edgescale*width;
		if (linewidth > 0 && width > 0) {
		    currentedgewidth = width;
		    coords = new double[] {linewidth*width, 0};
		    graphmatrix.deltaTransform(coords, 0, coords, 0, 1);
		    width = Math.sqrt(coords[0]*coords[0] + coords[1]*coords[1]);
		} else currentedgewidth = 0;
		if (pattern != null) {
		    if (pattern.length > 0) {
			coords = new double[2*pattern.length];
			for (n = 0, m = 0; n < pattern.length; n++) {
			    coords[m++] = pattern[n];
			    coords[m++] = 0;
			}
			graphmatrix.deltaTransform(coords, 0, coords, 0, pattern.length);
			dasharray = new float[pattern.length];
			for (n = 0, m = 0; n < coords.length; n += 2)
			    dasharray[m++] = (float)(Math.sqrt(coords[n]*coords[n] + coords[n+1]*coords[n+1]));
		    } else {
			linecap = BasicStroke.CAP_ROUND;
			dasharray = new float[] {1, (float)(2*width)};
		    }
		}
		currentedgepattern = pattern;
		currentedgestroke = new BasicStroke(
		    (float)width,
		    linecap,
		    BasicStroke.JOIN_MITER,
		    10,
		    dasharray,
		    0
		);
	    }
	}
	return(currentedgestroke);
    }


    private synchronized BasicStroke
    getCurrentMarkStroke(double width) {

	if (currentmarkstroke == null || currentmarkwidth != width) {
	    if (graphmatrix != null) {
		currentmarkwidth = width;
		currentmarkstroke = new BasicStroke((float)(linewidth*width), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	    }
	}
	return(currentmarkstroke);
    }


    private synchronized BasicStroke
    getCurrentNodeStroke(double width) {

	double  coords[];

	//
	// Simplified version of getCurrentEdgeStroke() because we decided
	// to ignore dash patterns and we also assume this will only be used
	// to draw outlines around nodes.
	//

	if (currentnodestroke == null || currentnodewidth != width) {
	    if (graphmatrix != null) {
		if (linewidth > 0 && width > 0) {
		    currentnodewidth = width;
		    coords = new double[] {linewidth*width, 0};
		    graphmatrix.deltaTransform(coords, 0, coords, 0, 1);
		    width = Math.sqrt(coords[0]*coords[0] + coords[1]*coords[1]);
		} else currentnodewidth = 0;
		currentnodestroke = new BasicStroke((float)width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	    }
	}
	return(currentnodestroke);
    }


    private synchronized BasicStroke
    getCurrentStroke(GraphData element) {

	BasicStroke  stroke;

	if (element != null) {
	    if ((element.element_type & DATA_NODE_MASK) != 0) {
		if (element.ismark)
		    stroke = getCurrentMarkStroke(element.linewidth);
		else stroke = getCurrentNodeStroke(element.linewidth);
	    } else if ((element.element_type & DATA_EDGE_MASK) != 0)
		stroke = getCurrentEdgeStroke(element.linewidth, element.dasharray);
	    else stroke = null;
	} else stroke = null;
	return(stroke);
    }


    private synchronized ArrayList
    getElements(int types) {

	ArrayList   elements;
	GraphData   element;
	int         n;

	elements = new ArrayList();
	for (n = 0; n < totalcount; n++) {
	    if ((element = graph[n]) != null) {
		if ((element.element_type & types) != 0)
		    elements.add(element.key);
	    }
	}
	return(elements);
    }


    private Integer
    getIndexIntegerFor(GraphData element) {

	HashMap  map;
	int      n;

	if ((map = indexmap) == null) {
	    map = new HashMap(graph.length);
	    for (n = 0; n < graph.length; n++)
		map.put(graph[n], new Integer(n));
	    indexmap = map;
	}
	return((Integer)map.get(element));
    }


    private static int
    getMarker() {

	int  value;

	synchronized(MARKERLOCK) {
	    if (marker == Integer.MAX_VALUE)
		marker = 0;
	    value = marker++;
	}
	return(value);
    }


    private String
    getNextToken(String text) {

	return(getNextToken(text, new int[] {0, 0}, text.length(), separator, separator.length()));
    }


    private static String
    getNextToken(String text, int offsets[], int length, String delim, int skip, int mode, boolean in_label) {

	String token;

	switch (mode) {
	    case -5:
		token = getTextToken(text, offsets, length, delim, skip);
		break;

	    case -6:
		if (in_label)
		    token = "fontcolor";
		else token = "color";
		break;

	    case -7:
		if (in_label)
		    token = "fontcolor";
		else token = "pencolor";
		break;

	    case -8:
		token = "pos";
		break;

	    default:
		token = getNextToken(text, offsets, length, delim, skip);
		break;
	}
	return(token);
    }


    private static String
    getNextToken(String text, int offsets[], int length, String delim, int skip) {

	StringBuffer  buffer;
	String        token;
	int           start;
	int           end;
	int           offset;
	char          newchars[];

	// we only unquote the first field in a record, if needed
	if (offsets[TOKEN_START] == 0 && length > 1 && text.charAt(0) == '"') {
	    buffer = new StringBuffer();
	    start = 0;
	    start++;
	    offset = 1;
	    end = length;
	    while (start < length) {
		if ((end = text.indexOf('"', start)) < start) {
		    end = length;
		    buffer.append(text.substring(start, end));
		    break;
		} else {
		    if ((end+1) < length && text.charAt(end+1) == '"') {
			buffer.append(text.substring(start, end+1));
			start = end + 2;
			offset++;
		    } else {
			buffer.append(text.substring(start, end));
			end++;
			break;
		    }
		}
	    }
	    token = buffer.toString();
	    offsets[TOKEN_END] = end;
	    offsets[TOKEN_START] = skipDelims(text, length, offsets[TOKEN_END], delim, skip);
	} else if ((offsets[TOKEN_END] = tokenEnd(text, length, offsets[TOKEN_START], delim)) >= 0) {
	    token = text.substring(offsets[TOKEN_START], offsets[TOKEN_END]);
	    offsets[TOKEN_START] = skipDelims(text, length, offsets[TOKEN_END], delim, skip);
	} else token = null;

	return(token);
    }


    private static String
    getTextToken(String text, int offsets[], int length, String delim, int skip) {

	String  token;
	int     tlen;

	if ((token = getNextToken(text, offsets, length, delim, skip)) != null) {
	    tlen = YoixMake.javaInt(token, -1);
	    if (tlen >= 0 && (offsets[TOKEN_START]+tlen) < length && text.charAt(offsets[TOKEN_START]) == '-') {
		offsets[TOKEN_START]++;
		offsets[TOKEN_END] = offsets[TOKEN_START] + tlen;
		token = text.substring(offsets[TOKEN_START], offsets[TOKEN_END]);
		offsets[TOKEN_START] = skipDelims(text, length, offsets[TOKEN_END], delim, skip);
	    } else VM.abort(BADVALUE, text);
	} else VM.abort(BADVALUE, text);

	return(token);
    }


    private String
    getTipPrefix(GraphData element, String text) {

	if (element.tipprefix == null) {
	    if (tipprefix instanceof YoixObject) {
		text = callTipHelper(
		    (YoixObject)tipprefix,
		    element
		);
	    } else if (tipprefix instanceof String)
		text = (String)tipprefix;
	    element.tipprefix = text;
	}
	return(element.tipprefix);
    }


    private String
    getTipSuffix(GraphData element, String text) {

	if (element.tipsuffix == null) {
	    if (tipsuffix instanceof YoixObject) {
		text = callTipHelper(
		    (YoixObject)tipsuffix,
		    element
		);
	    } else if (tipsuffix instanceof String)
		text = (String)tipsuffix;
	    element.tipsuffix = text;
	}
	return(element.tipsuffix);
    }


    private void
    grabBegin(Point point) {

	int  index;

	grabbedpoint = screenmatrix.transform(point, null);
	if ((index = findNextElement(point, eventbbox, DATA_GRAPH_NODE)) < 0)
	    index = findNextElement(point, eventbbox, DATA_GRAPH_EDGE);
	if (index >= 0) {
	    pressedelement = graph[index];
	    pressedstart = pressedelement.pressed;
	    pressedelement.repaint = true;
	    pressedelement.pressed = !pressedstart;
	    repaintViewer();
	}
    }


    private void
    grabDragged(Point2D point) {

	double  dx;
	double  dy;
	int     x;
	int     y;

	if (graph != null) {
	    if (grabbedpoint != null) {
		point = screenmatrix.transform(point, null);
		dx = grabbedpoint.getX() - point.getX();
		dy = grabbedpoint.getY() - point.getY();
		x = (int)Math.min(Math.max(viewport.x + dx, 0), extent.width - viewport.width);
		y = (int)Math.min(Math.max(viewport.y + dy, 0), extent.height - viewport.height);
		if (x != viewport.x || y != viewport.y)
		    setOrigin(new Point(x, y));
	    }
	}
    }


    private void
    grabEnd(Point point) {

	if (pressedelement != null) {
	    pressedelement.pressed = pressedstart;
	    pressedelement.repaint = true;
	    pressedelement = null;
	    repaintViewer();
	}
    }


    private synchronized GraphData
    loadActiveElement(String name, String text, int index, int count, double values[], HashMap map, HashMap references, ArrayList elements, ArrayList backgrounds) {

	GraphData  element;

	if ((element = (GraphData)map.get(name)) == null) {
	    element = new GraphData(name, text, index, values.length + 1);
	    if ((element = element.resolveReference(references)) != null) {
		if ((element.element_type & DATA_GRAPH_MASK) != 0) {
		    if (element.reference == false) {
			map.put(name, element);
			elements.add(element);
		    } else {
			element.saveReference(references);
			datarecords[index].setRequired(true);
			element = null;
		    }
		} else if ((element.element_type & DATA_BACKGROUND_MASK) != 0) {
		    backgrounds.add(element);
		    datarecords[index].setRequired(true);
		    element = null;
		} else if (element.isother) {
		    map.put(name, element);
		    elements.add(element);
		} else element = null;
	    }
	}
	if (element != null)
	    element.load(Math.max(1, count), values);
	return(element);
    }


    private synchronized void
    loadPassiveElement(String name, String text, int index, HashMap map, HashMap references, ArrayList elements, ArrayList backgrounds) {

	GraphData  element;

	//
	// Not completely convinced the reference stuff belongs here.
	// Leave it in for now, but it may disappear.
	//

	if ((element = (GraphData)map.get(name)) == null) {
	    element = new GraphData(name, text, index, 2);
	    if ((element = element.resolveReference(references)) != null) {
		if ((element.element_type & DATA_GRAPH_MASK) != 0) {
		    if (element.reference == false) {
			map.put(name, element);
			elements.add(element);
		    } else {
			element.saveReference(references);
			datarecords[index].setRequired(true);
		    }
		} else if ((element.element_type & DATA_BACKGROUND_MASK) != 0) {
		    backgrounds.add(element);
		    datarecords[index].setRequired(true);
		}
	    }
	}
    }


    private synchronized void
    loadReferences(HashMap map, HashMap references, ArrayList elements, ArrayList backgrounds) {

	Collection  values;
	GraphData   element;
	Iterator    iterator;
	Object      value;
	String      name;

	//
	// Eventually let this be controllable...
	//

	if (references.size() > 0) {
	    if ((values = references.values()) != null) {
		for (iterator = values.iterator(); iterator.hasNext(); ) {
		    value = iterator.next();
		    if (value instanceof GraphData) {
			element = (GraphData)value;
			if (element.required) {
			    if ((element.element_type & DATA_GRAPH_MASK) != 0) {
				if ((name = element.getElementName()) != null) {
				    map.put(name, element);
				    elements.add(0, element);
				}
			    } else if ((element.element_type & DATA_BACKGROUND_MASK) != 0)
				backgrounds.add(0, element);
			}
		    }
		}
	    }
	}
    }


    private synchronized void
    loadSubData(HashMap map, ArrayList elements) {

	DataRecord  record;
	GraphData   element;
	GraphData   subelement;
	ArrayList   list;
	HashMap     submap;
	String      text;
	Object      array[];
	double      values[];
	int         index;
	int         count;
	int         m;
	int         n;

	if (havesubdata) {
	    array = elements.toArray();
	    submap = new HashMap(map);
	    subdatamap = new HashMap();
	    for (n = 0; n < array.length; n++) {
		element = (GraphData)array[n];
		if (element.subdata != null) {
		    index = element.id;
		    count = element.getCountTotal();
		    record = datarecords[index];
		    values = getRecordValues(record, 1);
		    for (m = 0; m < element.subdata.length; m++) {
			if (element.subdata[m] instanceof String) {
			    text = (String)element.subdata[m];
			    if ((subelement = (GraphData)submap.get(text)) == null) {
				subelement = new GraphData(element.key, text, index, values.length + 1);
				elements.add(subelement);
				colormap.put(text, record);
				submap.put(text, subelement);
			    } else {
				if ((list = (ArrayList)subdatamap.get(subelement)) == null) {
				    list = new ArrayList();
				    subdatamap.put(subelement, list);
				}
				list.add(element);
			    }
			    element.subdata[m] = subelement;
			    subelement.load(count, values);
			}
		    }
		}
	    }
	}
    }


    private void
    loadSweepFilter(DataRecord loaded[], DataRecord records[]) {

	GraphRecord  graphrecords[];
	GraphRecord  graphrecord;
	DataRecord   record;
	YoixObject   root;
	GraphData    element;
	ArrayList    elements;
	ArrayList    backgrounds;
	HashMap      references;
	HashMap      map;
	String       name;
	String       text;
	int          length;
	int          mask;
	int          count;
	int          m;
	int          n;

	if (sweepfiltering) {
	    if (fieldindices.length > 1 && getActiveFieldCount() == 1) {	// unnecessary test?
		graphrecords = GraphLayout.buildGraphRecords(this, records, graphlayoutmodel, graphlayoutarg);
		mask = getFieldMask();
		length = graphrecords.length;
		loadeddata = loaded;
		datarecords = records;
		colormap = new Hashtable();
		map = new HashMap(length);
		references = new HashMap();
		elements = new ArrayList(length);
		backgrounds = new ArrayList();
		for (n = 0; n < length; n++) {
		    if ((graphrecord = graphrecords[n]) != null) {
			record = graphrecord.getRecord();
			if ((name = getRecordName(record)) != null) {
			    if ((text = graphrecord.getActiveElement()) != null) {
				element = loadActiveElement(name, text, n, 1, getRecordValues(record, 1), map, references, elements, backgrounds);
				if (element != null) {
				    if (record.notSelected()) {
					element.update(-1, getRecordValues(record, -1), false);
					if (record.notSelected(mask))
					    element.selected = false;
				    }
				    colormap.put(name, record);
				}
			    }
			    if ((count = graphrecord.getPassiveCount()) > 0) {
				for (m = 0; m < count; m++) {
				    if ((name = graphrecord.getPassiveName(m)) != null) {
					if ((text = graphrecord.getPassiveElement(m)) != null)
					    loadPassiveElement(name, text, n, map, references, elements, backgrounds);
				    }
				}
			    }
			}
		    }
		}
		loadReferences(map, references, elements, backgrounds);
		loadSubData(map, elements);
		buildViewer(map, elements, backgrounds);
		sliceGraph();
		zoomToData(null);
		if (autoshow && datamanager != null) {
		    if ((root = data.getObject(N_ROOT)) != null && root.notNull())
			root.putInt(N_VISIBLE, true);
		}
	    }
	}
    }


    private void
    moveTip(Point point) {

	GraphData  element;
	String     text;
	int        index;

	if (tipmanager.isEnabled()) {
	    if ((element = getCachedOutlineElement(point)) == null) {
		if ((index = findNextElement(point, eventbbox, tipflags & DATA_GRAPH_NODE)) < 0)
		    index = findNextElement(point, eventbbox, tipflags & DATA_GRAPH_EDGE);
		text = (index >= 0) ? graph[index].getTipText(tipflags, true) : null;
	    } else text = element.getTipText(tipflags, true);
	    setTipText(text);
	}
    }


    private synchronized void
    pickElements(YoixObject list, HashMap select, HashMap deselect, boolean selecting) {

	YoixObject  item;
	DataRecord  record;
	GraphData   element;
	String      name;
	String      key;
	int         index;
	int         length;
	int         n;

	//
	// Numbers now always reference records in loadeddata rather than
	// individual elements in the graph[] array. It's a small change
	// from older implemenations, at least for setSelected(), but we
	// don't think it will affect existing applications.
	//
	// The selecting argument was added on 9/9/05 and is used to make
	// sure we don't arbitrarily add things to the select HashMap when
	// we're selecting, because the low level setSelected() code might
	// not behave well. Eventually may want to investigate, but it's
	// definitely not that important.
	// 
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
			    if ((element = (GraphData)viewermap.get(name)) != null)
				key = element.key;
			}
		    }
		} else if (item.isString())
		    key = item.stringValue();
		if (key != null) {
		    if (deselect.containsKey(key)) {
			select.put(key, Boolean.TRUE);
			deselect.remove(key);
		    } else if (selecting == false) {	// added on 9/9/05
			if (select.containsKey(key) == false)
			    select.put(key, Boolean.TRUE);
		    }
		}
	    }
	}
    }


    private synchronized int[]
    pickMembers(double x, double y) {

	int  members[] = null;
	int  xm[];
	int  ym[];
	int  n;

	if (xslices != null && yslices != null) {
	    xm = null;
	    ym = null;
	    for (n = 0; n < SLICES && (xm == null || ym == null); n++) {
		if (xm == null && xslices[n].contains(x))
		    xm = xslices[n].members;
		if (ym == null && yslices[n].contains(y))
		    ym = yslices[n].members;
	    }
	    if (xm != null || ym != null) {
		if (xm == null)
		    members = ym;
		else if (ym == null)
		    members = xm;
		else members = (xm.length < ym.length) ? xm : ym;
	    }
	}

	return(members);
    }


    private double
    pickZoomScale(double value) {

	//
	// The rounding is a kludge added on 12/15/06 that's supposed to
	// help keep eliminate "jitter" and "wandering" issues when Yoix
	// scripts sync the zoom slider to a value that wasn't set by the
	// slider. Was noticeable when mouse wheel scrolling was anchored
	// to the pointer. Probably no reason when we can't increase the
	// a few more decimal places. Might be nice if the precision was
	// controllable from Yoix scripts - probably overkill right now
	// so we didn't bother yet.
	//

	return(Math.round(10.0*Math.min(Math.max(value, 0.5), zoomlimit))/10.0);
    }


    private void
    pressBegin(Point p) {

	int  index;

	if ((index = findNextElement(p, eventbbox, DATA_GRAPH_NODE)) < 0)
	    index = findNextElement(p, eventbbox, DATA_GRAPH_EDGE);
	if (index >= 0) {
	    switch (mouse) {
		case CENTERING:
		case TOGGLING:
		    pressedelement = graph[index];
		    pressedstart = pressedelement.pressed;
		    pressedelement.repaint = true;
		    pressedelement.pressed = !pressedstart;
		    repaintViewer();
		    break;

		case PRESSING:
		    if (getAfterPressed() != null) {
			pressedelement = graph[index];
			pressedstart = pressedelement.pressed;
			pressedelement.repaint = true;
			pressedelement.pressed = !pressedstart;
			repaintViewer();
		    }
		    break;
	    }
	}
    }


    private void
    pressDragged(Point p) {

	Point2D  point;

	if (pressedelement != null && graphmatrix != null) {
	    point = graphinverse.transform(
		screenmatrix.transform(p, null),
		null
	    );
	    if (pressedelement.elementContains(point)) {
		if (pressedelement.pressed == pressedstart) {
		    pressedelement.repaint = true;
		    pressedelement.pressed = !pressedstart;
		    repaintViewer();
		}
	    } else {
		if (pressedelement.pressed != pressedstart) {
		    pressedelement.repaint = true;
		    pressedelement.pressed = pressedstart;
		    repaintViewer();
		}
	    }
	}
    }


    private void
    pressEnd(MouseEvent e) {

	GraphData  element;
	boolean    pressed;
	Point2D    center;
	Point      point;

	//
	// We do things slightly differently when we're CENTERING because
	// we want to give the user a chance to see the element after we
	// move it. In the normal PRESSING case nothing moves so there's
	// probably no reason to delay the repaint until afterPressed().
	// 

	if ((element = pressedelement) != null) {
	    pressed = element.pressed;
	    switch (mouse) {
		case CENTERING:
		    if (pressed != pressedstart) {
			center = graphmatrix.transform(element.getElementPosition(), null);
			point = new Point(
			    (int)(center.getX() - viewport.width/2),
			    (int)(center.getY() - viewport.height/2)
			);
			setOrigin(point);
			try {
			    Thread.sleep(CENTERINGPAUSE);
			}
			catch(InterruptedException ex) {}
		    }
		    pressedelement = null;
		    element.pressed = pressedstart;
		    element.repaint = true;
		    repaintViewer();
		    break;

		case PRESSING:
		    pressedelement = null;
		    element.pressed = pressedstart;
		    element.repaint = true;
		    repaintViewer();
		    if (pressed != pressedstart)
			afterPressed(element.key, e);
		    break;

		case TOGGLING:
		    pressedelement = null;
		    element.pressed = pressedstart;
		    element.repaint = true;
		    if (pressed != pressedstart) {
			if (element.highlight)
			    element.highlight = false;
			else element.pressed = !element.pressed;
		    }
		    repaintViewer();
		    break;
	    }
	}
    }


    private synchronized void
    putCachedOutline(GraphData element, Shape outline) {

	int  length;

	if (outlinecache != null) {
	    if ((length = outlinecache.length) > 0) {
		lastoutlinehit = (lastoutlinehit + 1)%length;
		outlinecache[lastoutlinehit] = new Object[] {element, outline};
	    }
	}
    }


    private synchronized void
    raiseAllMarks() {

	GraphData  element;
	GraphData  next;
	GraphData  last;

	//
	// This is just an initialization method and since we only support
	// node marks, at least right now, we only go through the node list.
	//
	// NOTE - using each element's prev link to traverse the list would
	// be a little easier, but this way won't change the stacking order
	// of the marks. Definitely not a big deal here because this method
	// is usually only called once before anything is painted.
	//

	if (havemarks) {
	    for (next = firstnode, last = lastnode, element = null; element != last; ) {
		element = next;
		next = element.next;
		if (element.ismark)
		    raiseNode(element);
	    }
	}
    }


    private synchronized void
    raiseEdge(GraphData edge) {

	if (edge != null && edge != lastedge) {
	    if (edge.prev != null)
		edge.prev.next = edge.next;
	    else firstedge = edge.next;
	    edge.next.prev = edge.prev;
	    lastedge.next = edge;
	    edge.prev = lastedge;
	    edge.next = null;
	    lastedge = edge;
	}
    }


    private synchronized void
    raiseElement(GraphData element) {

	if ((element.element_type & DATA_GRAPH_NODE) != 0)
	    raiseNode(element);
	else if ((element.element_type & DATA_GRAPH_EDGE) != 0)
	    raiseEdge(element);
    }


    private synchronized void
    raiseNode(GraphData node) {

	if (node != null && node != lastnode) {
	    if (node.prev != null)
		node.prev.next = node.next;
	    else firstnode = node.next;
	    node.next.prev = node.prev;
	    lastnode.next = node;
	    node.prev = lastnode;
	    node.next = null;
	    lastnode = node;
	}
    }


    private synchronized void
    repaintXORElements(Object elements[], Graphics2D graphics) {

	GeneralPath  paths[];
	GraphData    element;
	Point2D      elem_center;
	Point2D      center;
	int          x;
	int          y;
	int          n;

	if (elements != null && graphics != null && (element = (GraphData)dragobjects[0]) != null) {
	    if ((paths = element.element_paths) != null) {
		elem_center = element.getElementCenter();
		x = (int)elem_center.getX();
		y = (int)elem_center.getY();
		for (n = 0; n < paths.length; n++)
		    graphics.draw(paths[n]);
		for (n = 1; n < elements.length; n++) {
		    if (elements[n] != null) {
			center = (Point2D)elements[n];
			graphics.drawLine(
			    x, y,
			    (int)center.getX(), (int)center.getY()
			);
		    }
		}
	    }
	}
    }


    private synchronized void
    repaintDataRect(Rectangle2D rect) {

	if (rect != null)
	    repaintScreenRect(transformDataToScreen(rect).getBounds());
    }


    private synchronized void
    repaintDataRect(Rectangle rect) {

	if (rect != null)
	    repaintScreenRect(transformDataToScreen(rect.getBounds2D()).getBounds());
    }


    private synchronized void
    repaintDataRect(Rectangle rect, Graphics2D g) {

	if (rect != null)
	    repaintScreenRect(transformDataToScreen(rect.getBounds2D()).getBounds(), g);
    }


    private synchronized void
    repaintScreenRect(Rectangle rect) {

	if (rect != null)
	    repaint(rect.x, rect.y, rect.width, rect.height);
    }


    private void
    repaintScreenRect(Rectangle rect, Graphics2D g) {

	Stroke  stroke;
	Color   color;
	int     linewidth;

	//
	// We grow the rectangle slightly so we can be sure we cover nodes
	// (or edges) that were stroked.
	//

	if (rect != null && g != null) {
	    stroke = g.getStroke();
	    if (stroke instanceof BasicStroke) {
		linewidth = (int)Math.max(Math.ceil(((BasicStroke)stroke).getLineWidth()), 2);
		linewidth += linewidth%2;
	    } else linewidth = 2;
	    rect = new Rectangle(rect);
	    rect.x -= linewidth;
	    rect.y -= linewidth;
	    rect.width += 2*linewidth;
	    rect.height += 2*linewidth;

	    color = g.getColor();
	    g.setColor(getBackground());
	    g.fill(rect);
	    paintRect(viewport.x + rect.x, viewport.y + rect.y, rect.width, rect.height, g);
	    g.setColor(color);
	}
    }


    private synchronized void
    repaintViewportRect(Rectangle rect) {

	if (rect != null)
	    repaintScreenRect(transformViewportToScreen(rect));
    }


    private synchronized void
    resetEdgeWidths() {

	double  coords[];
	double  widths[];

	widths = new double[] {linewidth, selectwidth};

	if (graphinverse != null) {
	    coords = new double[] {widths[1], 0};
	    graphinverse.deltaTransform(coords, 0, coords, 0, 1);
	    widths[1] = Math.sqrt(coords[0]*coords[0] + coords[1]*coords[1]);
	}

	edgewidths = widths;
    }


    private void
    saveSmallestFont(Font font) {

	if (font != null) {
	    if (smallestfont == null || font.getSize() < smallestfont.getSize())
		smallestfont = font;
	}
    }


    private void
    scrollBegin(Point point) {

	lastpoint = point;
    }


    private void
    scrollDragged(Point point) {

	int  dx;
	int  dy;
	int  x;
	int  y;

	if (graph != null) {
	    if (lastpoint != null) {
		dx = (point.x - lastpoint.x)*extent.width/viewport.width;
		dy = (point.y - lastpoint.y)*extent.height/viewport.height;
		x = Math.min(Math.max(viewport.x + dx, 0), extent.width - viewport.width);
		y = Math.min(Math.max(viewport.y + dy, 0), extent.height - viewport.height);
		setOrigin(new Point(x, y));
	    }
	    lastpoint = point;
	}
    }


    private void
    scrollEnd(Point point) {

    }


    private void
    selectElement(Point point, boolean selected) {

	ArrayList  elements;
	ArrayList  sublist;
	GraphData  element;
	GraphData  subelement;
	GraphData  edges[];
	GraphData  nodes[];
	GraphData  edge;
	boolean    append;
	int        length;
	int        index;
	int        n;
	int        m;
	int        i;

	if (graph != null) {
	    lastpoint = (lastpoint == null) ? point : lastpoint;
	    if (brushing) {
		if ((elements = findAllElements(lastpoint, point, selectflags, selected)) != null) {
		    length = elements.size();
		    for (n = 0; n < length; n++) {
			index = ((Integer)elements.get(n)).intValue();
			element = graph[index];
			if (element.selected != selected) {
			    if (attachededgeselection) {
				if (element.isNode()) {
				    element.selected = selected;
				    element.repaint = true;
				    edges = findAttachedEdges(element);
				    for (m = 0; m < edges.length; m++) {
					if ((edge = edges[m]) != null) {
					    if (edge.selected != selected) {
						if ((nodes = findAttachedNodes(edge)) != null) {
						    append = !selected || (
							(nodes[0] == null || nodes[0].selected == true)
							&&
							(nodes[1] == null || nodes[1].selected == true)
						    );
						} else append = true;
						if (append) {
						    edge.selected = selected;
						    edge.repaint = true;
						    length++;
						    elements.add(getIndexIntegerFor(edge));
						}
					    }
					}
				    }
				} else { // element.isEdge()
				    if ((nodes = findAttachedNodes(element)) != null) {
					if (!selected || (
						(nodes[0] == null || nodes[0].selected == true)
						&&
						(nodes[1] == null || nodes[1].selected == true)
					)) {
					    element.selected = selected;
					    element.repaint = true;
					}
				    }
				}
			    } else {
				element.selected = selected;
				element.repaint = true;
			    }
			    if (havesubdata) {
				if ((sublist = (ArrayList)subdatamap.get(element)) != null) {
				    for (m = 0; m < sublist.size(); m++) {
					if ((subelement = (GraphData)sublist.get(m)) != null) {
					    if (subelement.selected != selected) {
						subelement.selected = selected;
						subelement.repaint = true;
						elements.add(getIndexIntegerFor(subelement));
					    }
					}
				    }
				}
			    }
			}
		    }
		    updateData(elements, selected);
		}
	    } else {
		if ((index = findNextElement(point, eventbbox, selectflags, selected)) >= 0) {
		    element = graph[index];
		    if (element.selected != selected) {
			if (attachededgeselection) {
			    elements = new ArrayList();
			    length = 1;
			    elements.add(new Integer(index));
			    if (element.isNode()) {
				edges = findAttachedEdges(element);
				for (m = 0; m < edges.length; m++) {
				    if ((edge = edges[m]) != null) {
					if (edge.selected != selected) {
					    if ((nodes = findAttachedNodes(edge)) != null) {
						append = !selected || (
						    (nodes[0] == null || nodes[0].selected == true)
						    &&
						    (nodes[1] == null || nodes[1].selected == true)
						);
					    } else append = true;
					    if (append) {
						edge.selected = selected;
						edge.repaint = true;
						length++;
						elements.add(getIndexIntegerFor(edge));
					    }
					}
				    }
				}
				element.selected = selected;
				element.repaint = true;
				updateData(elements, selected);
			    } else { // element.isEdge()
				if ((nodes = findAttachedNodes(element)) != null) {
				    append = !selected || (
					(nodes[0] == null || nodes[0].selected == true)
					&&
					(nodes[1] == null || nodes[1].selected == true)
				    );
				} else append = true;
				if (append) {
				    element.selected = selected;
				    element.repaint = true;
				    updateData(index, selected);
				}
			    }
			} else {
			    element.selected = selected;
			    element.repaint = true;
			    updateData(index, selected);
			}
		    }
		}
	    }
	    lastpoint = point;
	}
    }


    private void
    setDataBBox() {

	double  width;
	double  height;
	double  padwidth;
	double  padheight;
	double  dx;
	double  dy;
	double  sx;
	double  sy;
	double  x;
	double  y;

	if ((databbox = databbox_loaded) != null) {
	    width = databbox.getWidth();
	    height = databbox.getHeight();
	    if (width > 0 && height > 0) {
		padwidth = ipad.left + ipad.right + markerpad.left + markerpad.right;
		padheight = ipad.top + ipad.bottom + markerpad.top + markerpad.bottom;
		if (padwidth != 0 || padheight != 0) {
		    sx = (viewport.width - padwidth)/width;
		    sy = (viewport.height - padheight)/height;
		    if (sx > 0 && sy > 0) {
			dx = viewport.width/sx - width;
			dy = viewport.height/sy - height;
			x = databbox.getX();
			y = databbox.getY();
			if (padwidth != 0)
			    x -= dx*((ipad.left + markerpad.left)/padwidth);
			if (padheight != 0)
			    y -= dy*((ipad.top + markerpad.top)/padheight);
			databbox = new Rectangle2D.Double(x, y, width + dx, height + dy);
		    }
		}
	    }
	}
    }


    private synchronized void
    setHighlighted(HashMap select) {

	GraphData  element;
	boolean    needpaint;
	boolean    state;
	int        n;

	if (loadeddata != null && datamanager != null) {
	    needpaint = false;
	    for (n = 0; n < graph.length; n++) {
		element = graph[n];
		state = select.containsKey(element.key);
		if (element.highlight != state) {
		    element.highlight = state;
		    element.repaint = true;
		    needpaint = true;
		}
	    }
	    if (needpaint)
		repaintViewer();
	}
    }


    private synchronized void
    setPressed(HashMap select) {

	GraphData  element;
	boolean    needpaint;
	boolean    state;
	int        n;

	if (loadeddata != null && datamanager != null) {
	    needpaint = false;
	    for (n = 0; n < graph.length; n++) {
		element = graph[n];
		state = select.containsKey(element.key);
		if (element.pressed != state) {
		    element.pressed = state;
		    element.repaint = true;
		    needpaint = true;
		}
	    }
	    if (needpaint)
		repaintViewer();
	}
    }


    private void
    setTipText(String text) {

	if (tipmanager.isEnabled())
	    tipmanager.setText(text);
    }


    private synchronized void
    setZoomLimit() {

	Dimension  screen;
	Toolkit    toolkit;
	double     pointsize;
	double     width;
	double     height;
	double     limit;
	double     scale;
	Font       font;
	int        resolution;

	//
	// Feel free to change the fine tuning of the calculations that
	// we currently use to adjust zoomlimit when we end up with a
	// value that seems too big or too small.
	//
	// NOTE - the markerpad adjustment to the limit is trying to let
	// the user separate markers that are very close in "data space"
	// when this canvas occupies about half the screen. Calculations
	// seem reasonable but definitely aren't meant to be exact!! The
	// addition was made on 3/17/08 as part of the support for fixed
	// size nodes.
	//

	if (databbox != null) {
	    width = databbox.getWidth();
	    height = databbox.getHeight();
	    if (width > 0 && height > 0) {
		toolkit = Toolkit.getDefaultToolkit();
		if ((screen = toolkit.getScreenSize()) != null) {
		    resolution = toolkit.getScreenResolution();
		    screen.width -= resolution;
		    screen.height -= resolution;
		    scale = Math.min(screen.width/width, screen.height/height);
		    if ((font = smallestfont) == null)
	        	font = getFont();
		    pointsize = (font != null) ? font.getSize() : 14.0;
		    limit = Math.ceil(ZOOMPOINTSIZE/(pointsize*scale));
		    if (limit > ZOOMLIMIT) {
			if (limit > 10*ZOOMLIMIT)
			    limit = Math.max(Math.ceil((.75*ZOOMPOINTSIZE)/(pointsize*scale)), 10*ZOOMLIMIT);
		    } else limit = ZOOMLIMIT;
		    if (markerpad.equals(ZEROINSETS) == false)
			limit = Math.max(2*(markerpad.left + markerpad.right)/scale, limit);
		    zoomlimit = limit;
		}
	    }
	}
    }


    private static int
    skipDelims(String text, int length, int end, String delim, int skip) {

	int  offset;

	for (offset = end + skip; offset < length; offset += skip) {
	    if (text.startsWith(delim, offset) == false)
		break;
	}
	return(offset);
    }


    private synchronized void
    sliceGraph() {

	int  n;

	if (graph != null && databbox != null) {
	    xslices = new GraphSlice[SLICES];
	    yslices = new GraphSlice[SLICES];
	    for (n = 0; n < SLICES; n++) {
		xslices[n] = new GraphSlice();
		yslices[n] = new GraphSlice();
	    }
	    sliceGraph(graph, xslices, yslices, databbox);
	}
    }


    private void
    sliceGraph(GraphData grf[], GraphSlice xs[], GraphSlice ys[], Rectangle2D bbox) {

	Rectangle2D  bounds;
	GraphSlice   sl[];
	double       incr;
	double       ofst;
	double       mn;
	double       mx;
	int          len;
	int          i;
	int          j;
	int          k;
	int          l;

	//
	// Using infinity as bounds for the two end slices was introduced
	// when we added support for node and edge scaling.
	//

	if (grf != null && bbox != null) {
	    for (i = 0; i < 2; i++) {
		if (i == 0) {
		    sl = xs;
		    ofst = bbox.getX();
		    incr = bbox.getWidth()/((double)SLICES);
		} else {
		    sl = ys;
		    ofst = bbox.getY();
		    incr = bbox.getHeight()/((double)SLICES);
		}

		for (k = 0; k < SLICES; k++) {
		    sl[k].mbr_hi = sl[k].bnd_lo = (k > 0) ? ofst : Double.NEGATIVE_INFINITY;
		    ofst += incr;
		    sl[k].mbr_lo = sl[k].bnd_hi = (k < SLICES-1) ? ofst : Double.POSITIVE_INFINITY;
		}
		len = grf.length;
		for (k = 0; k < SLICES; k++) {
		    for (j = 0; j < len; j++) {
			if ((bounds = grf[j].getCurrentBounds()) == null)
			    continue;
			if (i == 0) {
			    mn = bounds.getX();
			    if (mn < sl[k].bnd_lo || mn >= sl[k].bnd_hi)
				continue;
			    mx = mn + bounds.getWidth();
			} else {
			    mn = bounds.getY();
			    if (mn < sl[k].bnd_lo || mn >= sl[k].bnd_hi)
				continue;
			    mx = mn + bounds.getHeight();
			}
			if (mn < sl[k].mbr_lo)
			    sl[k].mbr_lo = mn;

			for (l = k; l < SLICES; l++) {
			    if (mx <= sl[l].bnd_lo)
				break;
			    sl[l].add(j);
			    if (l != k)
				sl[l].mbr_lo = sl[l].bnd_lo;
			    if (mx > sl[l].bnd_hi) {
				sl[l].mbr_hi = sl[l].bnd_hi;
				continue;
			    }
			    if (mx > sl[l].mbr_hi)
				sl[l].mbr_hi = mx;
			    break;
			}
		    }
		}
	    }
	}
    }


    private synchronized boolean
    sortGraph(int neworder, boolean force) {

	boolean  result = false;

	if (graph != null) {
	    if ((currentorder != neworder) || force) {
		YoixMiscQsort.sort(graph, neworder);
		colorViewer();
		indexmap = null;
		result = true;
	    }
	}
	currentorder = neworder;
	return(result);
    }


    private void
    sweepBegin(Point p) {

	xcorner[0] = Math.min(Math.max(p.x, insets.left), viewport.width + insets.left - 1);
	ycorner[0] = Math.min(Math.max(p.y, insets.top), viewport.height + insets.top - 1);
	xcorner[2] = xcorner[0];
	ycorner[2] = ycorner[0];
    }


    private void
    sweepDragged(Point p) {

	drawSweepBox();		// erase the last one

	xcorner[1] = xcorner[0];
	ycorner[1] = Math.min(Math.max(p.y, insets.top), viewport.height + insets.top - 1);
	xcorner[2] = Math.min(Math.max(p.x, insets.left), viewport.width + insets.left - 1);
	ycorner[2] = ycorner[1];
	xcorner[3] = xcorner[2];
	ycorner[3] = ycorner[0];

	drawSweepBox();
    }


    private void
    sweepEnd(MouseEvent e) {

	Rectangle2D  rect;
	Point2D      center;
	Point2D      point;
	double       sx;
	double       sy;
	int          modifiers;
	int          dx;
	int          dy;

	drawSweepBox();

	if (databbox != null && graphmatrix != null) {
	    modifiers = YoixMiscJFC.cookModifiers(e);
	    dx = Math.abs(xcorner[2] - xcorner[0]);
	    dy = Math.abs(ycorner[2] - ycorner[0]);
	    if ((dx*dx + dy*dy) > zoomdiameter2) {
		center = new Point2D.Double(
		    (xcorner[0] + xcorner[2])/2,
		    (ycorner[0] + ycorner[2])/2
		);
		center = screenmatrix.transform(center, null);
		center = graphinverse.transform(center, null);
		if (databbox.contains(center)) {
		    if (zoomdirection != 0) {
			if (mouse == ZOOMING_IN) {
			    point = new Point2D.Double(xcorner[0], ycorner[0]);
			    point = screenmatrix.transform(point, null);
			    point = graphinverse.transform(point, null);
			    rect = new Rectangle2D.Double(point.getX(), point.getY(), 0, 0);
			    point = new Point2D.Double(xcorner[2], ycorner[2]);
			    point = screenmatrix.transform(point, null);
			    point = graphinverse.transform(point, null);
			    rect.add(point);
			    zoomToDataRectangle(rect);
			} else if (mouse == ZOOMING_OUT) {
			    sx = ((double)dx)/viewport.width;
			    sy = ((double)dy)/viewport.height;
			    zoomToDataPoint(center, Math.min(sx, sy));
			}
		    } else zoomToData(center);
		    if (mouse == ZOOMING_IN || mouse == ZOOMING_OUT)
			afterSweep(VL_OP_ZOOM);
		}
	    }
	}
    }


    private static int
    tokenEnd(String text, int length, int offset, String delim) {

	int  end;

	if (offset < length) {
	    if ((end = text.indexOf(delim, offset)) < offset)
		end = length;
	} else end = -1;

	return(end);
    }


    private Point2D
    transformDataToScreen(Point2D point) {

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	if (point != null) {
	    if (graphmatrix != null) {
		point = screeninverse.transform(
		    graphmatrix.transform(point, null),
		    null
		);
	    } else point = null;
	}
	return(point);
    }


    private Rectangle2D
    transformDataToScreen(Rectangle2D rect) {

	Point2D  point;
	Point2D  size;

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	if (rect != null) {
	    if (graphmatrix != null) {
		point = new Point2D.Double(rect.getX(), rect.getY());
		size = new Point2D.Double(rect.getWidth(), rect.getHeight());

		point = screeninverse.transform(
		    graphmatrix.transform(point, null),
		    null
		);

		size = screeninverse.deltaTransform(
		    graphmatrix.deltaTransform(size, null),
		    null
		);

		rect = new Rectangle2D.Double(
		    point.getX(),
		    point.getY(),
		    size.getX(),
		    size.getY()
		);
	    }
	}
	return(rect);
    }


    private Point2D
    transformDataToViewport(Point2D point) {

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	return(point != null && graphmatrix != null ? graphmatrix.transform(point, null) : null);
    }


    private Rectangle2D
    transformDataToViewport(Rectangle2D rect) {

	Point2D  point;
	Point2D  size;

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	if (rect != null) {
	    if (graphmatrix != null) {
		point = new Point2D.Double(rect.getX(), rect.getY());
		size = new Point2D.Double(rect.getWidth(), rect.getHeight());
		point = graphmatrix.transform(point, null);
		size = graphmatrix.deltaTransform(size, null);

		rect = new Rectangle2D.Double(
		    point.getX(),
		    point.getY(),
		    size.getX(),
		    size.getY()
		);
	    }
	}
	return(rect);
    }


    private Point2D
    transformScreenToData(Point2D point) {

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	if (point != null) {
	    if (graphinverse != null) {
		point = graphinverse.transform(
		    screenmatrix.transform(point, null),
		    null
		);
	    } else point = null;
	}
	return(point);
    }


    private Point2D
    transformViewportToData(Point2D point) {

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	return(point != null && graphinverse != null ? graphinverse.transform(point, null) : null);
    }


    private Rectangle2D
    transformViewportToData(Rectangle2D rect) {

	Point2D  point;
	Point2D  size;

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	if (rect != null) {
	    if (graphinverse != null) {
		point = new Point2D.Double(rect.getX(), rect.getY());
		size = new Point2D.Double(rect.getWidth(), rect.getHeight());

		point = graphinverse.transform(point, null);
		size = graphinverse.deltaTransform(size, null);

		rect = new Rectangle2D.Double(
		    point.getX(),
		    point.getY(),
		    size.getX(),
		    size.getY()
		);
	    }
	}
	return(rect);
    }


    private Point2D
    transformViewportToScreen(Point2D point) {

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	return(point != null ? screeninverse.transform(point, null) : point);
    }


    private Rectangle
    transformViewportToScreen(Rectangle rect) {

	Point2D  point;

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	if (rect != null) {
	    point = screeninverse.transform(
		new Point2D.Double((double)rect.x, (double)rect.y),
		null
	    );
	    rect = new Rectangle((int)point.getX(), (int)point.getY(), rect.width, rect.height);
	    ////rect.setLocation((int)point.getX(), (int)point.getY());
	}
	return(rect);
    }


    private Point2D
    transformScreenToViewport(Point2D point) {

	//
	// Assumes caller is synchronized so transforms can't change. If
	// it's not true take snapshots or synchronize this method.
	//

	return(point != null ? screenmatrix.transform(point, null) : point);
    }


    private synchronized void
    updateMouseMotionListener() {

	//
	// Need to make sure we only add ourselves once!!
	//

	if (mouse != AVAILABLE || (tipflags & DATA_GRAPH_MASK) != 0) {
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


    private synchronized void
    zoomTo(double x, double y) {

	AffineTransform  matrix;
	AffineTransform  inverse;
	double           scale;
	double           ulx;
	double           uly;
	double           lrx;
	double           lry;

	//
	// Works well, but ignores padding. We will investigate later on.
	//

	if (databbox != null) {
	    if ((scale = zoomscale*graphscale) > 0) {
		matrix = new AffineTransform();
		matrix.translate(viewport.width/2, viewport.height/2);
		matrix.scale(scale, scale);
		matrix.translate(-databbox.getX(), -databbox.getY());
		try {
		    inverse = matrix.createInverse();
		}
		catch(NoninvertibleTransformException e) {
		    inverse = new AffineTransform();
		}

		lrx = scale*databbox.getWidth() + viewport.width;
		lry = scale*databbox.getHeight() + viewport.height;

		ulx = Math.round(scale*(x - databbox.getX()));
		uly = Math.round(scale*(y - databbox.getY()));

		graphmatrix = matrix;
		graphinverse = inverse;
		graphextent = new Rectangle2D.Double(0, 0, lrx, lry);
		viewport.x = (int)ulx;
		viewport.y = (int)uly;

		currentedgestroke = null;
		currentnodestroke = null;
		currentmarkstroke = null;

		setOrigin(new Point((int)ulx, (int)uly));
		setExtent();
		lastcenter = new Point2D.Double(x, y);
		if (havemarks)
		    sliceGraph();
		resetEdgeWidths();
		syncViewport();
		reset();		// really just need repaint()
	    }
	}
    }


    private synchronized void
    zoomToData(Point2D center) {

	double  width;
	double  height;
	double  scale;
	double  sx;
	double  sy;

	if (databbox != null) {
	    width = databbox.getWidth();
	    height = databbox.getHeight();
	    if (width > 0 && height > 0) {
		sx = viewport.width/width;
		sy = viewport.height/height;
		if ((scale = Math.min(sx, sy)) > 0) {
		    graphscale = scale;
		    if (center == null) {
			center = new Point2D.Double(
			    databbox.getX() + databbox.getWidth()/2,
			    databbox.getY() + databbox.getHeight()/2
			);
		    }
		    zoomTo(center.getX(), center.getY());
		}
	    }
	}
    }


    private synchronized void
    zoomToDataPoint(Point2D point, double scale) {

	if (databbox != null && point != null) {
	    if (scale > 0) {
		zoomscale = pickZoomScale(scale*zoomscale);
		zoomTo(point.getX(), point.getY());
	    }
	}
    }


    private synchronized void
    zoomToDataRectangle(Rectangle2D rect) {

	double  width;
	double  height;
	double  scale;
	double  sx;
	double  sy;

	if (databbox != null && rect != null) {
	    width = rect.getWidth();
	    height = rect.getHeight();
	    if (width > 0 && height > 0) {
		sx = viewport.width/width;
		sy = viewport.height/height;
		if ((scale = Math.min(sx, sy)) > 0) {
		    zoomscale = pickZoomScale(scale/graphscale);
		    zoomTo(
			rect.getX() + rect.getWidth()/2,
			rect.getY() + rect.getHeight()/2
		    );
		}
	    }
	}
    }


    private synchronized void
    zoomToViewport(Point2D center) {

	if (databbox != null) {
	    if (viewport.width > 0 && viewport.height > 0) {
		if (center == null) {
		    center = new Point2D.Double(
			viewport.x + viewport.width/2,
			viewport.y + viewport.height/2
		    );
		    center = graphinverse.transform(center, null);
		}
		zoomTo(center.getX(), center.getY());
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class GraphData

	implements YoixInterfaceSortable

    {

	//
	// Recently (3/29/05) decided that we needed separate access to the
	// head and tail of edges so there can now be up to three subpaths
	// in element_paths[]. Only edges currently end up with multiple
	// subpaths arranged in element_paths as [edge, head, tail]. The
	// code that deals with edges currently assumes element_paths[0]
	// is supposed to be stroked while the others are supposed to be
	// filled and stroked. The code that handles nodes uses the filled
	// boolean to decide what to do with subpaths (there's only one
	// right now). Suggests (along with other things) that the way we
	// set and use filled leaves somethings to be desired!! Not a big
	// deal, but it may be something we eventually want to address.
	//

	String  key;
	int     id;

	boolean  selected = true;
	boolean  repaint = false;
	boolean  isother = false;
	boolean  pressed = false;
	boolean  highlight = false;
	boolean  loaded = false;
	String   label = null;
	double   counters[];
	double   totals[];
	Color    color = null;
	Font     font = null;

	//
	// Low level stuff filled when separateText() parses text.
	//

	GeneralPath  element_paths[] = null;
	Rectangle2D  element_bounds = null;
	Point2D      element_position = null;
	Point2D      label_point[] = null;
	Point2D      hotspot = null;
	boolean      filled = false;
	boolean      invisible = false;
	boolean      dragging = false;
	boolean      hidden = false;
	boolean      filtered = false;
	boolean      reference = false;
	boolean      required = false;
	boolean      plaintext = false;
	boolean      ismark = false;
	String       label_text[] = null;
	String       tiptext = null;
	String       tipprefix = null;
	String       tipsuffix = null;
	double       dasharray[] = null;
	Object       original_values[] = null;
	Color        element_color = null;
	Color        label_color = null;
	Color        pen_color = null;
	int          element_type = 0;
	int          label_justification[] = null;
	int          linewidth = 1;

	String       name = null;

	//
	// This is used to avoid double vists.
	//

	int  marker = -1;		// used to avoid double visits

	//
	// Elements are also kept in a doubly linked list that's used
	// to keep printing consistent.
	//

	GraphData  next = null;
	GraphData  prev = null;

	//
	// This probably is only needed by marks, but we're going to keep
	// it general for now.
	// 

	Object  subdata[] = null;

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	GraphData(String key, String text, int id, int slots) {

	    this.key = key;
	    this.id = id;		// increasing integer for sorting
	    this.isother = (key == OTHERNAME);
	    this.counters = new double[slots];
	    this.totals = new double[slots];
	    separateText(text, separator);
	}


	GraphData(GraphData original) {

	    Shape  shapes[];
	    int    n;

	    //
	    // Makes a copy for XOR dragging so we can mess with values
	    // and then discard; consequently, we are not interested in
	    // all values
	    //

	    key = original.key;
	    id = original.id;
	    label = original.label;
	    color = original.color;
	    font = original.font;
	    filled = original.filled;
	    invisible = original.invisible;
	    hidden = original.hidden;
	    filtered = original.filtered;
	    reference = original.reference;
	    required = original.required;
	    tiptext = original.tiptext;
	    element_color = original.element_color;
	    pen_color = original.pen_color;
	    label_color = original.label_color;
	    element_type = original.element_type;
	    linewidth = original.linewidth;
	    name = original.name;

	    // skip: selected, repaint, pressed, highlight, loaded, dragging, maybe others?

	    if (original.element_paths != null) {
		element_paths = new GeneralPath[original.element_paths.length];
		for (n = 0; n < element_paths.length; n++)
		    element_paths[n] = new GeneralPath(original.element_paths[n]);
	    }
	    if (original.element_bounds != null) {
		element_bounds = new Rectangle2D.Double(
		    original.element_bounds.getX(),
		    original.element_bounds.getY(),
		    original.element_bounds.getWidth(),
		    original.element_bounds.getHeight()
		);
	    }
	    if (original.element_position != null) {
		element_position = new Point2D.Double(
		    original.element_position.getX(),
		    original.element_position.getY()
		);
	    }
	}

	///////////////////////////////////
	//
	// YoixInterfaceSortable Methods
	//
	///////////////////////////////////

	public final int
	compare(YoixInterfaceSortable element, int flag) {

	    GraphData  element2;
	    String     name1;
	    String     name2;
	    double     val;
	    int        sign;

	    //
	    // A recent addition that's really only needed to impose some
	    // order on elements when the graph is used to color data and
	    // the associated plot is stacked.
	    //
	    // Not synchronized, which means we assume an earlier method
	    // locked the GraphPlot. Could be problems, primarily in the
	    // cases that do division, if that assumption is wrong.
	    //

	    element2 = (GraphData)element;
	    sign = (flag >= 0) ? 1 : -1;

	    if (isother == false && element2.isother == false) {
		switch (sign*flag) {
		    case VL_SORT_LOAD_ORDER:
		    case VL_SORT_TIME:
			val = -(element2.id - id);	// smaller id's are older
			break;

		    case VL_SORT_TOTAL:
			val = element2.totals[active] - totals[active];
			break;

		    default:
			val = 0;
			break;
		}

		if (val == 0) {
		    if ((name1 = getElementName()) != null) {
			if ((name2 = element2.getElementName()) != null)
			    val = name1.compareTo(name2);
			else val = -1;
		    } else val = (element2.getElementName() != null) ? 1 : 0;
		}
	    } else val = isother ? 1 : -1;

	    return(sign*(val != 0 ? (val > 0 ? 1 : -1) : 0));
	}

	///////////////////////////////////
	//
	// GraphData Methods
	//
	///////////////////////////////////

	final boolean
	elementContains(Point2D point) {

	    Rectangle2D  bounds;
	    boolean      result = false;
	    Shape        outline;

	    if ((bounds = getScaledBounds()) != null) {
		if (bounds.contains(point)) {
		    if ((outline = getCachedOutlineShape(this)) == null || (result = outline.contains(point)) == false) {
			if ((outline = getScaledOutline(false)) != null) {
			    result = outline.contains(point);
			    if (result)
				putCachedOutline(this, outline);
			} else result = false;
		    }
		} else result = false;
	    } else result = false;
	    return(result);
	}


	final boolean
	elementIntersects(Line2D line, Area linearea) {

	    Rectangle2D  bounds;
	    boolean      result;
	    Shape        outline;
	    Area         intersection;

	    if ((bounds = getScaledBounds()) != null) {
		if (line.intersects(bounds)) {
		    if ((outline = getScaledOutline(false)) != null) {
			intersection = new Area(outline);
			intersection.intersect(linearea);
			result = (intersection.isEmpty() == false);
		    } else result = false;
		} else result = false;
	    } else result = false;
	    return(result);
	}


	final void
	extendElement() {

	    GeneralPath  paths[];
	    GeneralPath  opaths[];
	    Object       ovalues[];
	    int          n;

	    if (original_values != null && original_values[4] == null) {
		repaint = true;
		ovalues = new Object[2];
		ovalues[0] = element_bounds.getBounds2D();
		if (element_paths != null) {
		    paths = new GeneralPath[element_paths.length];
		    System.arraycopy(element_paths, 0, paths, 0, paths.length);
		    ovalues[1] = paths;
		} else ovalues[1] = null;
		original_values[4] = ovalues;

		//
		// now extend element values (using copies)
		//
		element_bounds.add((Rectangle2D)original_values[0]);
		opaths = (GeneralPath[])original_values[1];
		if (element_paths != null && opaths != null) {
		    n = element_paths.length > opaths.length ? element_paths.length : opaths.length;
		    paths = new GeneralPath[n];
		    for (n = 0; n < element_paths.length; n++)
			paths[n] = new GeneralPath(element_paths[n]);
		    for (n = 0; n < opaths.length; n++) {
			if (n < element_paths.length)
			    paths[n].append(opaths[n], false);
			else paths[n] = new GeneralPath(opaths[n]);
		    }
		    element_paths = paths;
		} else if (opaths != null) {
		    paths = new GeneralPath[opaths.length];
		    for (n = 0; n < opaths.length; n++)
			paths[n] = new GeneralPath(opaths[n]);
		    element_paths = paths;
		} else if (element_paths != null) {
		    paths = new GeneralPath[element_paths.length];
		    for (n = 0; n < element_paths.length; n++)
			paths[n] = new GeneralPath(element_paths[n]);
		    element_paths = paths;
		}
	    }
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


	final Rectangle2D
	getCurrentBounds() {

	    Rectangle2D  bounds;

	    //
	    // This method always returns a rectangle that the caller can
	    // safely modify.
	    //

	    return((bounds = getScaledBounds()) != null ? bounds.getBounds2D() : null);
	}


	final Rectangle2D
	getCurrentBounds(AffineTransform matrix) {

	    Rectangle2D  bounds;

	    //
	    // This method always returns a rectangle that the caller can
	    // safely modify, even when matrix is null.
	    //

	    if ((bounds = getScaledBounds()) != null) {
		if (matrix != null)
		    bounds = matrix.createTransformedShape(bounds).getBounds2D();
		else bounds = bounds.getBounds2D();
	    }
	    return(bounds);
	}


	final Point2D
	getCurrentOrigin(AffineTransform matrix) {

	    Rectangle2D  bounds;
	    Point2D      point;

	    if ((bounds = getScaledBounds()) != null) {
		point = new Point2D.Double(bounds.getX(), bounds.getY());
		if (matrix != null)
		    point = matrix.transform(point, null);
	    } else point = null;
	    return(point);
	}


	final Shape[]
	getDrawShapes(AffineTransform matrix, boolean scaled) {

	    Shape  shapes[];
	    int    n;

	    //
	    // Should only be called by the methods that really draw the
	    // graph elements. Notice that we return null if plaintext is
	    // true, which means separateText() found a label but didn't
	    // find any drawing instructions for the element.
	    //

	    if (element_paths != null && plaintext == false) {
		if (scaled == false) {
		    shapes = new Shape[element_paths.length];
		    System.arraycopy(element_paths, 0, shapes, 0, element_paths.length);
		} else shapes = getScaledShapes();
		if (matrix != null) {
		    for (n = 0; n < shapes.length; n++)
			shapes[n] = matrix.createTransformedShape(shapes[n]);
		}
	    } else shapes = null;
	    return(shapes);
	}


	final Rectangle2D
	getElementBounds() {

	    Rectangle2D  bounds;
	    Rectangle2D  rect;
	    GeneralPath  path;
	    double       lw;
	    int          n;

	    //
	    // We currently assume that node labels are contained within
	    // nodes (when element_paths[0] exists), so we skip the label
	    // bounds calculation for nodes.
	    //

	    if (element_bounds == null) {
		if (element_paths != null) {
		    bounds = null;
		    for (n = 0; n < element_paths.length; n++) {
			if ((path = element_paths[n]) != null) {
			    if (path.getCurrentPoint() != null) {
				if ((rect = path.getBounds2D()) != null) {
				    if (bounds != null)
					bounds.add(rect);
				    else bounds = rect;
				}
			    }
			}
		    }

		    if (label_text != null) {
			// get label bounds for edges and elements without other bounds
			if (bounds == null || element_type == DATA_GRAPH_EDGE || element_paths[0] == null) {
			    if ((rect = getLabelBounds()) != null) {
				if (bounds != null)
				    bounds.add(rect);
				else bounds = rect;
			    }
			}
		    }

		    if (bounds != null) {
			lw = (linewidth > 0) ? linewidth : 0.5;
			//
			// Doubled the adjustment that's supposed to account
			// for the element's outline on 3/18/08. It's a Small
			// kludge, but the other way would occasionally miss
			// outside edges of outlined nodes.
			//
			bounds.setRect(
			    bounds.getX() - lw,
			    bounds.getY() - lw,
			    bounds.getWidth() + 2*lw,
			    bounds.getHeight() + 2*lw
			);
			element_bounds = bounds;
		    }
		}
	    }

	    return(element_bounds);
	}


	final Point2D
	getElementCenter() {

	    Rectangle2D  bounds;
	    Point2D      point;

	    if (ismark == false) {
		if ((bounds = getElementBounds()) != null) {
		    point = new Point2D.Double(
			bounds.getCenterX(),
			bounds.getCenterY()
		    );
		} else point = null;
	    } else point = new Point2D.Double(hotspot.getX(), hotspot.getY());
	    return(point);
	}


	final String
	getElementName() {

	    return(name);
	}


	final Point2D
	getElementPosition() {

	    return(element_position != null ? (Point2D)element_position.clone() : getElementCenter());
	}


	final Rectangle2D
	getExtendedBounds() {

	    Rectangle2D  bounds;
	    GraphData    edges[];
	    GraphData    edge;
	    int          n;

	    //
	    // This method always returns a rectangle that the caller can
	    // safely modify.
	    //

	    if ((bounds = getScaledBounds()) != null) {
		bounds = bounds.getBounds2D();
		if ((edges = findAttachedEdges(this)) != null) {
		    for (n = 0; n < edges.length; n++) {
			if ((edge = edges[n]) != null)
			    bounds.add(edge.getScaledBounds());
		    }
		}
	    }
	    return(bounds);
	}


	final String
	getLabel(int nbr) {

	    StringBuffer  buf;
	    boolean       showcount;
	    boolean       showvalue;
	    String        prefix;
	    String        lbl;

	    //
	    // Need to think about this some, because doing a good job is
	    // quite a bit harder than you might expect. Problems happen
	    // when labels change, as they should when we show counts or
	    // values. In those cases we need to erase (something) before
	    // drawing a new label, but doing a good (and efficient) job
	    // on that one is harder than you might expect. For now we
	    // skip appending counts to real label, which is OK when we
	    // print node labels and the text that we print fits inside
	    // the node. No guarantee that currently happens, but shorter
	    // labels are less likely to cause problems.
	    //

	    if (nbr < 0)
		nbr = 0;

	    lbl = (nbr == 0) ? label : null;

	    if (lbl == null && (labelflags&LABEL_HIDE) == 0) {
		if (nbr == 0) {
		    if (label_text != null && label_text[nbr] != null) {
			showcount = ((labelflags&LABEL_SHOWCOUNT) != 0);
			showvalue = ((labelflags&LABEL_SHOWVALUE) != 0);
			if (showcount || showvalue)
			    buf = new StringBuffer();
			else buf = new StringBuffer(label_text[nbr]);
			appendExtra(buf, showcount, showvalue);
			lbl = buf.toString();
			label = lbl;
		    }
		} else if (label_text != null && nbr < label_text.length)
		    lbl = label_text[nbr];
	    }
	    return(lbl);
	}


	final Rectangle2D
	getLabelBounds() {

	    return(getLabelBounds(font != null ? font : getFont()));
	}


	final Rectangle2D
	getLabelBounds(Font textfont) {

	    Rectangle2D  bounds = null;
	    Rectangle2D  rect;
	    TextLayout   layout;
	    int          n;

	    //
	    // We may need to calculate that bounds of a label based on a
	    // font that is derived from the font that was assigned to this
	    // element. You might think that scaling bounds that are based
	    // on the assigned font would be sufficient, but unfortunately
	    // that doesn't always seem to be the case and the small errors
	    // that sometimes creep in can affect things when we're dragging
	    // or editing. Incidentally, clipping node labels to nodes can
	    // help eliminate some of the problems, but the performance hit
	    // that we observed wasn't generally acceptable.
	    //

	    if (textfont != null && label_text != null) {
		for (n = label_text.length - 1; n >= 0; n--) {
		    if (label_text[n] != null && label_text[n].length() > 0) {
			layout = new TextLayout(label_text[n], textfont, FONTCONTEXT);
			rect = layout.getBounds();
			switch (label_justification[n]) {
			    case -1:
				rect = new Rectangle2D.Double(
				    label_point[n].getX() + rect.getX(),
				    label_point[n].getY() + rect.getY(),
				    rect.getWidth(),
				    rect.getHeight()
				);
				break;

			    case 1:
				rect = new Rectangle2D.Double(
				    label_point[n].getX() - rect.getWidth(),
				    label_point[n].getY() + rect.getY(),
				    rect.getWidth(),
				    rect.getHeight()
				);
				break;

			    default:
				rect = new Rectangle2D.Double(
				    label_point[n].getX() - rect.getWidth()/2.0,
				    label_point[n].getY() + rect.getY(),
				    rect.getWidth(),
				    rect.getHeight()
				);
				break;
			}
			if (bounds == null)
			    bounds = rect;
			else bounds.add(rect);
		    }
		}
	    }
	    return(bounds);
	}


	final Point2D
	getLabelCenter() {

	    Rectangle2D  bounds;
	    Point2D      point;

	    if ((bounds = getLabelBounds()) != null) {
		point = new Point2D.Double(
		    bounds.getCenterX(),
		    bounds.getCenterY()
		);
	    } else point = null;

	    return(point);
	}


	final int
	getLabelJustification(int n) {

	    return(label_justification != null && n >= 0 && n < label_justification.length
		? label_justification[n]
		: 0
	    );
	}


	final TextLayout
	getLabelLayout(int n, Font textfont) {

	    TextLayout  layout;
	    double      scale;
	    String      text;

	    //
	    // We assume uniform scaling and no rotatation to simplify the
	    // point size check. Easy to change, but the generality really
	    // isn't needed. The lower bound should be greater than 1 (for
	    // a Mac bug), but otherwise the value is optional. Could be
	    // controlled using another GraphPlot field, but that seemed
	    // like overkill.
	    //

	    if ((text = getLabel(n)) != null) {
		scale = textfont.getTransform().getScaleX();
		if (scale*textfont.getSize2D() > 1.25)
		    layout = new TextLayout(text, textfont, FONTCONTEXT);
		else layout = null;
	    } else layout = null;

	    return(layout);
	}


	final Point2D
	getLabelPoint(int n) {

	    return(label_point != null && n >= 0 && n < label_point.length
		? label_point[n]
		: null
	    );
	}


	final Rectangle2D
	getOriginalBounds() {

	    Rectangle2D  bounds;

	    if (original_values != null) {
		if ((bounds = (Rectangle2D)original_values[0]) == null)
		    bounds = element_bounds;
	    } else bounds = element_bounds;

	    return(bounds);
	}


	final GeneralPath[]
	getOriginalPaths() {

	    GeneralPath  paths[];

	    if (original_values != null) {
		if ((paths = (GeneralPath[])original_values[1]) == null)
		    paths = element_paths;
	    } else paths = element_paths;

	    return(paths);
	}


	final Point2D
	getOriginalPosition() {

	    Point2D  point;

	    if (original_values != null) {
		if ((point = (Point2D)original_values[2]) == null)
		    point = element_position;
	    } else point = element_position;

	    return(point);
	}


	final String
	getTipText(int flags, boolean html) {

	    StringBuffer  buf;
	    ArrayList     list;
	    boolean       showcount;
	    boolean       showvalue;
	    String        text;
	    int           n;

	    if ((text = tiptext) == null) {
		if (label_text != null && label_text.length > 0)
		    text = label_text[0];
	    }
	    if (text != null) {
		buf = new StringBuffer();
		if (html) {
		    buf.append("<html>&nbsp;");
		    buf.append(getTipPrefix(this, ""));
		    if (text.indexOf('\n') >= 0) {
			list = YoixMisc.split(text, "\n");
			for (n = 0; n < list.size(); n++) {
			    if (n > 0)
				buf.append("<p>&nbsp;");
			    buf.append(YoixMisc.htmlFromAscii((String)list.get(n)));
			}
		    } else buf.append(YoixMisc.htmlFromAscii(text));
		} else buf.append(text);
		showcount = ((flags&TIP_SHOW_COUNT) != 0) && (getTotalAt(0) != 0);
		showvalue = ((flags&TIP_SHOW_VALUE) != 0) && (getTotalAt(active > 0 ? active : 1) != 0);
		if (showcount || showvalue)
		    appendExtra(buf, showcount, showvalue);
		if (html) {
		    buf.append(getTipSuffix(this, ""));
		    buf.append("&nbsp;</html>");
		}
		text = buf.toString();
	    }
	    return(text);
	}


	final double
	getTotalAt(int n) {

	    return(totals[n]);
	}


	final double
	getValueAt(int n) {

	    return(counters[n]);
	}


	final boolean
	hidden() {

	    boolean  oldvalue = hidden;

	    hidden = (
		hidemode
		&&
		(!selected || filtered || isFiltered())
		&&
		(
		    (isEdge() && (edgeflags & EDGEHIDEDESELECTED) != 0)
		    ||
		    (isNode() && (nodeflags & NODEHIDEDESELECTED) != 0)
		)
	    );
	    return(oldvalue != hidden ? false : hidden);
	}


	final boolean
	intersects(Rectangle2D rect) {

	    Rectangle2D  bounds;

	    return((bounds = getScaledBounds()) != null && bounds.intersects(rect));
	}


	final boolean
	isEdge() {

	    return((element_type & DATA_EDGE_MASK) != 0);
	}


	final boolean
	isFiltered() {

	    return(getActiveValue() == 0 && getActiveValue() != getActiveTotal());
	}


	final boolean
	isGraphEdge() {

	    return((element_type & DATA_GRAPH_EDGE) != 0);
	}


	final boolean
	isGraphNode() {

	    return((element_type & DATA_GRAPH_NODE) != 0);
	}


	final boolean
	isNode() {

	    return((element_type & DATA_NODE_MASK) != 0);
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
	    loaded = true;
	}


	final void
	moveAttachedEdges(AffineTransform matrix, GeneralPath original, Rectangle dirty, boolean going_home) {

	    AffineTransform  transform;
	    Rectangle2D      bounds;
	    GeneralPath      paths[];
	    GeneralPath      path;
	    GraphData        edges[];
	    GraphData        edge;
	    GraphData        nodes[];
	    GraphData        node;
	    Point2D          centers[];
	    Point2D          center;
	    Point2D          intersections[];
	    Point2D          before;
	    Point2D          after;
	    Point2D          average;
	    Line2D           tangents[];
	    Line2D           lines[];
	    Line2D           line;
	    double           dx1;
	    double           dy1;
	    double           dx2;
	    double           dy2;
	    double           alpha;
	    Shape            shape;
	    int              m;
	    int              n;

	    //
	    // Finds the edges attached to this node (answer will be null if
	    // this isn't a node) and moves each edge so it looks like it's
	    // properly attached to this node, which has already been moved.
	    // Hardest part is making sure the head and tail of the edge are
	    // properly adjusted (translated and then rotated) so they look
	    // like they're properly attached to the moved edge.
	    //
	    // NOTE - we pass the original path as an argument and calculate
	    // the original center from that path when necessary. Would it be
	    // better to pass the center as an argument (the caller should be
	    // able to get it without any calculations).
	    //

	    if ((edges = findAttachedEdges(this)) != null) {
		for (n = 0; n < edges.length; n++) {
		    if ((edge = edges[n]) != null) {
			//
			// Find the nodes that are attached to the tail and
			// head of each edge. Note that nodes[0] is attached
			// to the tail and nodes[1] is attached to the head.
			//
			if ((nodes = findAttachedNodes(edge)) != null) {
			    if (nodes[0] != null && nodes[1] != null) {
				if (nodes[0] == this || nodes[1] == this) {
				    //
				    // This saves original stuff and means
				    // we could restore things if we ever
				    // want to. At the very least we need
				    // to save the original paths so that
				    // the findAttached methods will work
				    // properly.
				    //
				    if (going_home) {
					if (nodes[0].original_values == null && nodes[1].original_values == null)
					    edge.original_values = null;
				    } else edge.saveOriginalValues();
				    dirty.add(edge.getScaledBounds().getBounds());
				    if ((paths = edge.element_paths) != null) {
					if (nodes[0] != nodes[1]) {
					    centers = new Point2D[] {
						nodes[0].getElementCenter(),
						nodes[1].getElementCenter(),
					    };
					    lines = new Line2D[] {
						new Line2D.Double(centers[1], centers[0]),
						new Line2D.Double(centers[0], centers[1]),
					    };
					    intersections = new Point2D[] {
						YoixMiscGeom.getFirstIntersection(
						    lines[0],
						    nodes[0].getTargetShape(null, false)
						),
						YoixMiscGeom.getFirstIntersection(
						    lines[1],
						    nodes[1].getTargetShape(null, false)
						)
					    };
					    line = new Line2D.Double(
						intersections[0].getX(), intersections[0].getY(),
						intersections[1].getX(), intersections[1].getY()
					    );
					    //
					    // Note that paths[1] will be the head
					    // (if it's not null) and paths[2] will
					    // be the tail. A bit confusing, but it
					    // actually turns out to be a convenient
					    // match with the way the tangents[] and
					    // nodes[] arrays are arranged.
					    //
					    if (paths.length > 1) {
						//
						// We currently assume edges are connected
						// by straight line segments, so this isn't
						// as general as it should be. We could use
						// YoixMiscGeom.getEndTangents() or something
						// like it to get spline tangents at each end
						// of the edge. Tried, but had some problems
						// when edges disappeared because the nodes
						// at the ends of the edge were moved on top
						// of each other. The current approach seems
						// to behave much better, but it's still not
						// quite 100%.
						//
						bounds = original.getBounds2D();
						center = new Point2D.Double(
						    bounds.getCenterX(), bounds.getCenterY()
						);
						if (nodes[1] == this) {
						    tangents = new Line2D[] {
							new Line2D.Double(center, centers[0]),
							new Line2D.Double(centers[0], center)
						    };
						} else {
						    tangents = new Line2D[] {
							new Line2D.Double(centers[1], center),
							new Line2D.Double(center, centers[1])
						    };
						}
						for (m = 1; m < paths.length; m++) {
						    if ((path = paths[m]) != null) {
							if (nodes[m%2] == this) {
							    before = YoixMiscGeom.getFirstIntersection(
								tangents[m%2],
								original
							    );
							} else {
							    if (nodes[m%2].element_paths[0].getCurrentPoint() != null) {
								before = YoixMiscGeom.getFirstIntersection(
								    tangents[m%2],
								    nodes[m%2].element_paths[0]
								);
							    } else {
								before = YoixMiscGeom.getFirstIntersection(
								    tangents[m%2],
								    nodes[m%2].getElementBounds()
								);
							    }
							}
							after = intersections[m%2];

							//
							// Translate the head or tail from the
							// old to the new intersection point.
							//
							transform = new AffineTransform();
							transform.setToTranslation(
							    after.getX() - before.getX(),
							    after.getY() - before.getY()
							);
							path.transform(transform);

							//
							// Then rotate the head or tail so it's
							// properly aligned with what eventually
							// will be the line that connects the new
							// intersection points.
							//
							dx1 = tangents[m%2].getX2() - tangents[m%2].getX1();
							dy1 = tangents[m%2].getY2() - tangents[m%2].getY1();
							dx2 = after.getX() - intersections[(m+1)%2].getX();
							dy2 = after.getY() - intersections[(m+1)%2].getY();
							alpha = Math.atan2(dy2, dx2) - Math.atan2(dy1, dx1);

							transform.setToRotation(alpha, after.getX(), after.getY());
							path.transform(transform);

							//
							// We try to make sure the new line will
							// end in the new translated and rotated
							// head or tail. Not always needed, but
							// if omitted we pretty much are assuming
							// that transformed head or tail really
							// will cover the calculated intersection
							// points.
							//

							if ((average = YoixMiscGeom.getAveragePoint(path)) != null) {
							    if (m == 1)
								line.setLine(line.getP1(), average);
							    else line.setLine(average, line.getP2());
							}
						    }
						}
					    }

					    path = new GeneralPath();
					    path.append(line, false);
					    edge.element_paths[0] = path;
					} else {	// must be this node
					    //
					    // Both ends must be attached to this node,
					    // so we use matrix to transform the edge.
					    //
					    for (m = 0; m < paths.length; m++) {
						if ((path = paths[m]) != null) {
						    shape = path.createTransformedShape(matrix);
						    if (shape instanceof GeneralPath)	// probably always true
							paths[m] = (GeneralPath)shape;
						    else paths[m] = new GeneralPath(shape);
						}
					    }
					}
				    }
				    edge.moveAttachedLabels(matrix, dirty);
				    edge.element_bounds = null;
				    edge.repaint = true;
				    nodes[1].repaint = true;
				    nodes[0].repaint = true;
				}	// should not happen!!
			    }		// should not happen!!
			}
		    }
		}
	    }
	}


	final void
	moveAttachedLabels(AffineTransform matrix, Rectangle dirty) {

	    Rectangle2D  bounds;
	    GeneralPath  paths[];
	    GraphData    nodes[];
	    Point2D      center;
	    Point2D      current;
	    int          n;

	    if (label_point != null) {
		//
		// First see if this is an edge that's attached to two
		// different nodes. If so we'll move the center of the
		// label to the "average" point in the edges path. The
		// center of the path's bounding box probably would be
		// just as good.
		//
		nodes = findAttachedNodes(this);
		if (nodes != null && nodes[0] != nodes[1]) {	// edge attached to 2 nodes
		    if ((paths = element_paths) != null) {
			if ((center = YoixMiscGeom.getAveragePoint(paths[0])) != null) {
			    current = getLabelCenter();
			    matrix = new AffineTransform();
			    matrix.setToTranslation(
				center.getX() - current.getX(),
				center.getY() - current.getY()
			    );
			}
		    }
		}
		for (n = 0; n < label_point.length; n++)
		    label_point[n] = matrix.transform(label_point[n], null);
	    }
	}


	final void
	moveNodeHome(Graphics2D g) {

	    Rectangle2D  bounds;
	    Point2D      origin;

	    if ((bounds = getOriginalBounds()) != null) {
		bounds = getScaledBounds(bounds);
		origin = new Point2D.Double(bounds.getX(), bounds.getY());
		moveNodeTo(origin, g, true);
	    }
	}


	final void
	moveNodeTo(Point2D origin, Graphics2D g) {

	    moveNodeTo(origin, g, false);
	}


	final void
	moveNodeTo(Point2D origin, Graphics2D g, boolean going_home) {

	    AffineTransform  matrix;
	    GeneralPath      paths[];
	    GeneralPath      path;
	    GeneralPath      original;
	    Rectangle2D      bounds;
	    Rectangle        dirty;
	    double           tx;
	    double           ty;
	    Stroke           stroke;
	    Shape            shape;
	    int              m;
	    int              n;

	    //
	    // This method eventually will also be responsible for adjusting
	    // the edges that are connected to this node. Also need to build
	    // a new node mask - later.
	    //

	    if (origin != null) {
		bounds = getScaledBounds();
		tx = origin.getX() - bounds.getX();
		ty = origin.getY() - bounds.getY();
		if (tx != 0 || ty != 0) {
		    if (going_home)
			original_values = null;
		    else saveOriginalValues();
		    dirty = bounds.getBounds();
		    matrix = new AffineTransform();
		    matrix.setToTranslation(tx, ty);

		    if ((paths = element_paths) != null) {
			original = new GeneralPath();
			for (n = 0; n < paths.length; n++) {
			    if ((path = paths[n]) != null) {
				original.append(path, false);
				shape = path.createTransformedShape(matrix);
				if (shape instanceof GeneralPath)	// probably always true
				    paths[n] = (GeneralPath)shape;
				else paths[n] = new GeneralPath(shape);
			    }
			}
		    } else original = null;

		    if (element_position != null)
			element_position = matrix.transform(element_position, null);

		    element_bounds = null;
		    moveAttachedLabels(matrix, dirty);
		    moveAttachedEdges(matrix, original, dirty, going_home);
		    sliceGraph();
		    repaintDataRect(dirty, g);
		}
	    }
	    repaint = true;
	    repaintViewer();
	}


	final boolean
	notEmpty() {

	    return((int)counters[0] != 0);
	}


	final boolean
	notFull() {

	    return((int)totals[0] != (int)counters[0]);
	}


	final Color
	pickEdgeColor(Color value, Palette palette) {

	    if (palette != null) {
		if (getCountTotal() > 0 || (element_color == null && pen_color == null)) {
		    if ((value = this.color) == null) {
			switch (element_type) {
			    case DATA_GRAPH_EDGE:
				value = palette.selectColor(getActiveValue(), maxedgetotal, palettecontrol_edges, value);
				break;

			    case DATA_GRAPH_NODE:
				value = palette.selectColor(getActiveValue(), maxnodetotal, palettecontrol_nodes, value);
				break;

			    default:
				value = palette.selectColor((int)getActiveValue(), value);
				break;
			}
			this.color = value;
		    }
		} else if (element_color != null)
		    value = element_color;
		else if (pen_color != null)
		    value = pen_color;
	    } else if (color != null)
		value = color;
	    else if (element_color != null)
		 value = element_color;
	    else if (pen_color != null)
		value = pen_color;
	    return(value);
	}


	final Color
	pickFillColor(Color value, Palette palette) {

	    if (isother == false) {
		if (palette != null) {
		    if (getCountTotal() > 0 || element_color == null) {
			if ((value = this.color) == null) {
			    switch (element_type) {
				case DATA_GRAPH_EDGE:
				    value = palette.selectColor(getActiveValue(), maxedgetotal, palettecontrol_edges, value);
				    break;

				case DATA_GRAPH_NODE:
				    value = palette.selectColor(getActiveValue(), maxnodetotal, palettecontrol_nodes, value);
				    break;

				default:
				    value = palette.selectColor((int)getActiveValue(), value);
				    break;
			    }
			    this.color = value;
			}
		    } else if (element_color != null)
			value = element_color;
		} else if (color != null)
		    value = color;
		else if (element_color != null)
		    value = element_color;
	    } else value = othercolor;

	    return(value);
	}


	final Color
	pickPenColor(Color value, Palette palette, boolean filled) {

	    if (filled) {
		if (pen_color != null)
		    value = pen_color;
	    } else if (palette != null) {
		if (getCountTotal() > 0 || pen_color == null) {
		    if ((value = this.color) == null) {
			switch (element_type) {
			    case DATA_GRAPH_EDGE:
				value = palette.selectColor(getActiveValue(), maxedgetotal, palettecontrol_edges, value);
				break;

			    case DATA_GRAPH_NODE:
				value = palette.selectColor(getActiveValue(), maxnodetotal, palettecontrol_nodes, value);
				break;

			    default:
				value = palette.selectColor((int)getActiveValue(), value);
				break;
			}
			this.color = value;
		    }
		} else if (pen_color != null)
		    value = pen_color;
	    } else if (color != null)
		value = color;
	    else if (pen_color != null)
		value = pen_color;

	    return(value);
	}


	final GraphData
	resolveReference(HashMap references) {

	    GraphData  element;
	    String     name;

	    if (element_type == 0) {
		if ((name = getElementName()) != null) {
		    if ((element = (GraphData)references.get(name)) != null) {
			if (element.reference) {
			    element.key = key;
			    element.id = id;
			    element.isother = isother;
			    element.reference = false;
			    references.remove(name);
			}
		    } else element = this;
		} else element = this;
	    } else element = this;
	    return(element);
	}


	final void
	saveReference(HashMap references) {

	    String  name;

	    if ((name = getElementName()) != null)
		references.put(name, this);
	}


	final void
	tossLabel() {

	    label = null;
	}


	public String
	toString() {

	    //
	    // Mostly for debugging - fill stuff in as needed.
	    //

	    return("id=" + id + ", key=" + key);
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
	    if (currentpalette != null && isother == false)
		color = null;
	    repaint = true;
	    endstate = (counters[active] != 0);

	    if (isother)
		selected = endstate;

	    if (startstate != endstate) {
		selectedcount += endstate ? 1 : -1;
		if (postevent)
		    postItemEvent(YoixObject.newString(key), endstate, true);
	    }
	}

	///////////////////////////////////
	//
	// Private Methods
	//
	///////////////////////////////////

	private void
	appendExtra(StringBuffer buf, boolean showcount, boolean showvalue) {

	    if (buf != null) {
		if (showcount || showvalue) {
		    if (buf.length() > 0)
			buf.append(" (");
		    else buf.append("(");
		    if (showvalue) {
			buf.append((int)(getValueAt(active > 0 ? active : 1) + .5));
			if (showcount) {
			    buf.append("|");
			    buf.append((int)getValueAt(0));
			}
		    } else buf.append((int)getValueAt(0));
		    buf.append(" of ");
		    if (showvalue) {
			buf.append((int)(getTotalAt(active > 0 ? active : 1) + .5));
			if (showcount) {
			    buf.append("|");
			    buf.append((int)getTotalAt(0));
			}
		    } else buf.append((int)getTotalAt(0));
		    buf.append(")");
		}
	    }
	}


	private Object
	getAttributeValue(String text, int offsets[], int length, String delim, int skip, int type) {
	    return(getAttributeValue(text, offsets, length, delim, skip, type, null));
	}


	private Object
	getAttributeValue(String text, int offsets[], int length, String delim, int skip, int type, String token) {

	    StringTokenizer  st;
	    Object           value;
	    String           str;
	    String           tok;
	    char             ch;
	    float            flts[];
	    int              nbrs[];
	    int              vs;
	    int              i;

	    nbrs = null;
	    str = null;

	    if (token != null || (token = getNextToken(text, offsets, length, delim, skip)) != null) {
		if ((ch = token.charAt(0)) == 'N' || ch == 'n') {
		    if ((token = getNextToken(text, offsets, length, delim, skip)) != null) {
			vs = YoixMake.javaInt(token, -1);
			nbrs = new int[vs];
			for (i = 0; i < vs; i++) {
			    if ((token = getNextToken(text, offsets, length, delim, skip)) != null)
				nbrs[i] = YoixMake.javaInt(token, -1);
			    else VM.abort(BADVALUE, text);
			}
		    } else VM.abort(BADVALUE, text);
		} else if (ch == 'S' || ch == 's') {
		    str = getTextToken(text, offsets, length, delim, skip);
		} else VM.abort(BADVALUE, text);
	    } else VM.abort(BADVALUE, text);

	    value = null;

	    switch (type) {
		case ATTR_COLOR:
		    if (str != null) {
			if ((value = YoixMisc.javaColorLookup(str)) == null) {
			    if (str.charAt(0) == '#') {
				byte[]  bytearray;
				if ((bytearray = YoixMisc.hexStringToBytes(str.substring(1))) != null) {
				    if (bytearray.length == 3) {
					value = new Color(bytearray[0]&0xFF, bytearray[1]&0xFF, bytearray[2]&0xFF);
				    } else if (bytearray.length == 4) {
					value = new Color(bytearray[0]&0xFF, bytearray[1]&0xFF, bytearray[2]&0xFF, bytearray[3]&0xFF);
				    } else VM.abort(BADVALUE, new String[] { "color value", str });
				} else VM.abort(BADVALUE, new String[] { "color value", str });
			    } else {
				flts = new float[3];
				i = 0;
				st = new StringTokenizer(str, " ,");
				while(st.hasMoreTokens()) {
				    if (i > 2)
					VM.abort(BADVALUE, new String[] { "color value", str });
				    tok = st.nextToken();
				    try {
					flts[i++] = Float.parseFloat(tok);
				    }
				    catch(NumberFormatException nfe) {
					VM.abort(BADVALUE, new String[] { "color value", str });
				    }
				}
				value = Color.getHSBColor(flts[0], flts[1], flts[2]);
			    }
			}
		    } else if (nbrs != null && nbrs.length == 3) {
			for (i = 0; i < 3; i++) {
			    if (nbrs[i] < 0 || nbrs[i] > 255)
				VM.abort(BADVALUE, text);
			}
			value = new Color(nbrs[0], nbrs[1], nbrs[2]);
		    } else VM.abort(BADVALUE, text);
		    break;

		case ATTR_NUMBER:
		    if (nbrs != null && nbrs.length == 1)
			value = new Double(nbrs[0]);
		    else VM.abort(BADVALUE, text);
		    break;

		case ATTR_POINT:
		    if (nbrs != null && nbrs.length == 2)
			value = new Point2D.Double(nbrs[0], nbrs[1]);
		    else VM.abort(BADVALUE, text);
		    break;

		case ATTR_STRING:
		    if (str != null)
			value = str;
		    else VM.abort(BADVALUE, text);
		    break;

		case ATTR_DISCARD:
		    break;

		default:
		    VM.abort(INTERNALERROR, text);
		    break;
	    }

	    return(value);
	}


	private Rectangle2D
	getScaledBounds() {

	    return(getScaledBounds(getElementBounds()));
	}


	private Rectangle2D
	getScaledBounds(Rectangle2D bounds) {

	    BasicStroke  stroke;
	    double       coords[];
	    double       adjustment;
	    double       width;
	    double       height;
	    double       scale;
	    Shape        shape;

	    if (bounds != null) {
		if (ismark == false) {
		    switch (element_type) {
			case DATA_GRAPH_EDGE:
			case DATA_BACKGROUND_EDGE:
			    scale = edgescale;
			    break;

			case DATA_GRAPH_NODE:
			case DATA_BACKGROUND_NODE:
			    scale = nodescale;
			    break;

			default:
			    scale = 1.0;
			    break;
		    }

		    if (scale != 1.0) {
		        adjustment = (scale - 1.0)/(2*scale);
		        width = scale*bounds.getWidth();
		        height = scale*bounds.getHeight();
		        bounds = new Rectangle.Double(
			    bounds.getX() - width*adjustment,
			    bounds.getY() - height*adjustment,
			    width,
			    height
			);
		    }
		} else {
		    if (graphinverse != null) {
			//
			// We currently ignore the bounds argument for marks,
			// which seems strange and isn't really correct, but
			// the width and height values in bounds are probably
			// wrong. A small kludge that only affects the code
			// used to drag nodes around and since marks can't be
			// dragged it's not currently a problem. We'll address
			// it later, most likely by having getElementBounds()
			// call getScaledOutline() so it returns a value that
			// reflects the current graphmatrix.
			// 
			shape = getScaledOutline(false);
			bounds = shape.getBounds2D();

			coords = new double[] {linewidth, 0};
			graphinverse.deltaTransform(coords, 0, coords, 0, 1);
			width = Math.sqrt(coords[0]*coords[0] + coords[1]*coords[1]);

			bounds = new Rectangle2D.Double(
			    bounds.getX() - width,
			    bounds.getY() - width,
			    bounds.getWidth() + 2*width,
			    bounds.getHeight() + 2*width
			);
		    }
		}
	    }
	    return(bounds);
	}


	private GeneralPath
	getScaledEdgePath(int index) {

	    AffineTransform  matrix;
	    GeneralPath      path;
	    Point2D          center;
	    double           scale;
	    double           x;
	    double           y;
	    Shape            shape;

	    //
	    // We current using YoixMiscGeom.getAveragePoint() to select
	    // a center point, but the path's bounding box likely would
	    // be just as good a guess.
	    //

	    if ((path = element_paths[index]) != null) {
		if (index > 0) {	// omit the stroked subpath
		    if (edgescale != 1.0) {
			if ((center = YoixMiscGeom.getAveragePoint(path)) != null) {
			    scale = 1.0 + (edgescale - 1.0)/2.0;
			    x = center.getX();
			    y = center.getY();
			    matrix = new AffineTransform();
			    matrix.translate(x, y);
			    matrix.scale(scale, scale);
			    shape = path.createTransformedShape(matrix);
			    if (shape instanceof GeneralPath)	// probably always true
				path = (GeneralPath)shape;
			    else path = new GeneralPath(shape);
			    matrix.setToTranslation(-scale*x, -scale*y);
			    path.transform(matrix);
			}
		    }
		}
	    }
	    return(path);
	}


	private GeneralPath
	getScaledNodePath(int index) {

	    AffineTransform  matrix;
	    Rectangle2D      bounds;
	    GeneralPath      path;
	    double           scale;
	    double           x;
	    double           y;
	    Shape            shape;

	    if ((path = element_paths[index]) != null) {
		if (nodescale != 1.0 || ismark) {
		    if ((bounds = path.getBounds()) != null) {
			if (ismark) {
			    scale = (graphinverse != null) ? graphinverse.getScaleX() : 1.0;
			    if (hotspot != null) {
				x = hotspot.getX();
				y = hotspot.getY();
			    } else {
				x = bounds.getX() + bounds.getWidth()/2;
				y = bounds.getY() + bounds.getHeight()/2;
			    }
			} else {
			    scale = nodescale;
			    x = bounds.getX() + bounds.getWidth()/2;
			    y = bounds.getY() + bounds.getHeight()/2;
			}
			matrix = new AffineTransform();
			matrix.translate(x, y);
			matrix.scale(scale, scale);
			shape = path.createTransformedShape(matrix);
			if (shape instanceof GeneralPath)	// probably always true
			    path = (GeneralPath)shape;
			else path = new GeneralPath(shape);
			matrix.setToTranslation(-scale*x, -scale*y);
			path.transform(matrix);
		    }
		}
	    }
	    return(path);
	}


	private Shape
	getScaledOutline(boolean accurate) {

	    return(accurate ? getScaledOutlineExact() : getScaledOutlineFast());
	}


	private Shape
	getScaledOutlineExact() {

	    GeneralPath  path;
	    GeneralPath  outline = null;
	    BasicStroke  stroke;
	    Shape        stroked;
	    int          n;

	    //
	    // This version does an exact job accounting for the head and
	    // tail of edges, but the outline is more complicated than the
	    // fast version and that slows intersection testing. We doubt
	    // anyone will ever really care about accurate hit detection
	    // in the head or tail of an edge, so this version may only be
	    // here as a reference.
	    //
	    // Didn't thoroughly investigate the performance differences,
	    // so you may not want to just accept our analysis. Also think
	    // a simple (and temporary) caching mechanism could make a big
	    // difference if the time spent building the outline in this
	    // method is a noticeable part of the problem.
	    //

	    if (element_paths != null) {
		for (n = 0; n < element_paths.length; n++) {
		    if (element_paths[n] != null) {
			switch (element_type) {
			    case DATA_GRAPH_EDGE:
			    case DATA_BACKGROUND_EDGE:
				stroke = new BasicStroke(
				    (float)Math.max(edgescale*linewidth*edgewidths[0], edgewidths[1])
				);
				path = getScaledEdgePath(n);
				stroked = stroke.createStrokedShape(path);
				if (!(stroked instanceof GeneralPath))
				    stroked = new GeneralPath(stroked);
				if (n != 0)
				    ((GeneralPath)stroked).append(path, false);
				path = (GeneralPath)stroked;
				break;

			    case DATA_GRAPH_NODE:
			    case DATA_BACKGROUND_NODE:
				path = getScaledNodePath(n);
				break;

			    default:
				path = null;
				break;
			}
			if (path != null) {
			    if (outline != null)
				outline.append(path, false);
			    else outline = path;
			}
		    }
		}
	    }
	    return(outline);
	}


	private Shape
	getScaledOutlineFast() {

	    GeneralPath  path;
	    GeneralPath  outline = null;
	    BasicStroke  stroke;
	    Shape        stroked;
	    int          n;

	    //
	    // A version that doesn't bother accounting for the outline that
	    // is supposed to be drawn around the head and tail of edges. The
	    // resulting shape is simpler and often results in significantly
	    // faster intersection testing. Reasonable to use if you don't
	    // care about accurate detection if an edge's head or tail.
	    //

	    if (element_paths != null) {
		for (n = 0; n < element_paths.length; n++) {
		    if (element_paths[n] != null) {
			switch (element_type) {
			    case DATA_GRAPH_EDGE:
			    case DATA_BACKGROUND_EDGE:
				if (n == 0) {
				    stroke = new BasicStroke(
					(float)Math.max(edgescale*linewidth*edgewidths[0], edgewidths[1]),
					BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_MITER,
					2	// might help a little
				    );
				    stroked = stroke.createStrokedShape(element_paths[n]);
				    if (stroked instanceof GeneralPath)
					path = (GeneralPath)stroked;
				    else path = new GeneralPath(stroked);
				} else path = getScaledEdgePath(n);
				break;

			    case DATA_GRAPH_NODE:
			    case DATA_BACKGROUND_NODE:
				path = getScaledNodePath(n);
				break;

			    default:
				path = null;
				break;
			}
			if (path != null) {
			    if (outline != null)
				outline.append(path, false);
			    else outline = path;
			}
		    }
		}
	    }
	    return(outline);
	}


	private Shape[]
	getScaledShapes() {

	    Shape  shapes[];
	    int    n;

	    if (element_paths != null) {
		shapes = new Shape[element_paths.length];
		for (n = 0; n < shapes.length; n++) {
		    switch (element_type) {
			case DATA_GRAPH_EDGE:
			case DATA_BACKGROUND_EDGE:
			    if (n == 0)
				shapes[n] = element_paths[n];	// partly bogus!!!
			    else shapes[n] = getScaledEdgePath(n);
			    break;

			case DATA_GRAPH_NODE:
			case DATA_BACKGROUND_NODE:
			    shapes[n] = getScaledNodePath(n);
			    break;
		    }
		}
	    } else shapes = null;
	    return(shapes);
	}


	private Shape
	getTargetShape(AffineTransform matrix, boolean scaled) {

	    GeneralPath  path = null;
	    Rectangle2D  rect;
	    Shape        shapes[];
	    Shape        shape;
	    int          length;
	    int          n;

	    //
	    // Returns a shape that's appropriate non-rendering tasks, like
	    // hit detection, but isn't necessarily right for rendering.
	    //
	    // NOTE - moving edges around and dealing with their heads and
	    // tails is a big reason why we keep the components separate.
	    //

	    if ((shapes = scaled ? getScaledShapes() : element_paths) != null) {
		if ((length = shapes.length) > 0) {
		    for (n = 0; n < length; n++) {
			if ((shape = shapes[n]) != null) {
			    if ((rect = shape.getBounds2D()) != null) {
				if (rect.isEmpty() == false) {
				    if (path == null)
					path = new GeneralPath(shape);	// don't change shape!!
				    else path.append(shape, false);
				}
			    }
			}
		    }
		}
	    }
	    return(matrix != null ? matrix.createTransformedShape(path) : path);
	}


	private synchronized void
	restoreOriginalValues() {

	    //
	    // Currently unused ...
	    //

	    if (original_values != null) {
		element_bounds = (Rectangle2D)original_values[0];
		element_paths = (GeneralPath[])original_values[1];
		original_values = null;
	    }
	}


	private synchronized void
	saveOriginalValues() {

	    GeneralPath  paths[];
	    int          n;

	    //
	    // Eventually expect we'll do more here...
	    //

	    if (original_values == null) {
		original_values = new Object[5];
		original_values[0] = element_bounds.getBounds2D(); // is reference OK?
		if (element_paths != null) {
		    paths = new GeneralPath[element_paths.length];
		    for (n = 0; n < paths.length; n++)
			paths[n] = new GeneralPath(element_paths[n]);
		    original_values[1] = paths;
		}
		original_values[2] = element_position;
		original_values[3] = getLabelCenter();
		original_values[4] = null; // reserved for extending operation
	    }
	}


	private void
	separateText(String text, String delim) {

	    GeneralPath  subpaths[];
	    Rectangle2D  bounds;
	    Rectangle2D  rectangle;
	    Ellipse2D    ellipse;
	    HashSet      set;
	    boolean      dofont;
	    boolean      in_label;
	    Object       obj;
	    String       split[];
	    String       nexttoken;
	    String       token;
	    String       tval;
	    String       fontname;
	    float        coords[];
	    Font         currentfont;
	    int          pathindex;
	    int          fontstyle;
	    int          fontsize;
	    int          offsets[];
	    int          length;
	    int          skip;
	    int          crdsln;
	    int          mode;
	    int          submode;
	    int          spec;
	    int          just;
	    int          na;
	    int          m;
	    int          n;

	    //
	    // Here's a brief and incomplete summary of the drawing directives.
	    // Our goal is to conform with the directives described here:
	    //    http://www.graphviz.org/pub/scm/graphviz2/doc/info/output.html#d:xdot
	    //
	    //	  E xcenter ycenter width height
	    //	    Fill an ellispe
	    //
	    //	  e xcenter ycenter width height
	    //	    Draw an ellipse
	    //
	    //	  P n x1 y1 x2 y2 ... xn yn
	    //	    Fill the polygon connecting the n points, where n
	    //	    must be greater than 2. The first and last points
	    //	    points are automatically connected if necessary.
	    //
	    //	  p n x1 y1 x2 y2 ... xn yn
	    //	    Draw the polygon (the outline) connecting the n points,
	    //	    where n must be greater than 2. The first and last
	    //	    points are automatically connected if necessary, so
	    //	    this isn't quite the same as the L command.
	    //
	    //	  P 2 xcenter yxcenter width height
	    //	    Fill a rectangle (instance of polygon described above)
	    //
	    //	  p 2 xcenter yxcenter width height
	    //	    Draw a rectangle (instance of polygon described above)
	    //
	    //    L n x1 y1 x2 y2 ... xn yn
	    //	    Draw a line connecting the n point
	    //
	    //	  B n x1 y1 x2 y2 x3 y3 ... xn yn
	    //	    Draw a Bezier spline that starts at (x1, y1) ends at
	    //	    (xn, yn) and is described by the remaining points. The
	    //	    value of n must be at least 4 and (n - 1) must also be
	    //	    divisible by 3. If you set the filled attribute (see
	    //	    the "A 1 filled" description below) before the spline
	    //	    and the path will be automatically closed, which means
	    //	    the shape described by the S command will be completely
	    //	    outlined when it's used as a node.
	    //
	    //	  b n x1 y1 x2 y2 x3 y3 ... xn yn
	    //	    Fill a Bezier spline that starts at (x1, y1) ends at
	    //	    (xn, yn) and is described by the remaining points. The
	    //	    value of n must be at least 4 and (n - 1) must also be
	    //	    divisible by 3. If you set the filled attribute (see
	    //	    the "A 1 filled" description below) before the spline
	    //	    and the path will be automatically closed, which means
	    //	    the shape described by the S command will be completely
	    //	    outlined when it's used as a node.
	    //
	    //	  T x y justification width n -[n chars]
	    //	    Place n characters at (x, y) using justification (-1
	    //	    means left justified, 0 means centered, and 1 means
	    //	    right justified) to control the positioning relative
	    //	    to (x, y). The width argument is currently unused but
	    //	    must be supplied (any number works). The n characters
	    //	    of the text that's draw must be introduced by the -
	    //	    character.
	    //
	    //    C n -[n chars]
	    //      Set the fill color. The color value can be a text name
	    //      (e.g., red), or an RGB spec starting with a pound-sign
	    //      and with each color specified by a 2 hex characters
	    //      (e.g., #FF0000) or an RGBA spec (e.g., #FF0000F8) or
	    //      an HSB spec with each value in the range 0 to 1 and
	    //      separated by either a comma or a space character
	    //      (e.g., 0.482 0.714 0.878 or 0.482,0.714,0.878)
	    //
	    //    c n -[n chars]
	    //      Set the line color. The color value can be a text name
	    //      (e.g., red), or an RGB spec starting with a pound-sign
	    //      and with each color specified by a 2 hex characters
	    //      (e.g., #FF0000) or an RGBA spec (e.g., #FF0000F8) or
	    //      an HSB spec with each value in the range 0 to 1 and
	    //      separated by either a comma or a space character
	    //      (e.g., 0.482 0.714 0.878 or 0.482,0.714,0.878)
	    //
	    //    F s n -[n chars]
	    //      Set the font to size s and as specified (e.g., F 12 14 -Helvetica-Bold)
	    //
	    //    S n -[n chars]
	    //      Set the style as specified (e.g., S 15 -setlinewidth(3))
	    //
	    //   ====== below here are not used by graphviz, but we use them ======
	    //
	    //	  < label related commands >
	    //	    The delimiters serve to bracket one or more commands
	    //	    that are supposed to be grouped together and handled
	    //	    as a label. Bracketing a single T command is allowed,
	    //	    and you'll see it in many of our examples, but it's
	    //
	    //    w n
	    //      Switch to subpath n. Currently only used by edges and
	    //      in that case 0 is the main path, 1 is the head, and 2
	    //      is the tail.
	    //
	    //    x n x1 y1 ... xn yn
	    //      Position infomation specified as n 2-D points.
	    //
	    //    n n x1 ... xn
	    //    N n x1 ... xn
	    //      Numeric info specified by n numbers. An upper-case N
	    //      can also signal a numeric value. Only understood within
	    //      an attribute spec (see below).
	    //
	    //    s n -[n chars]
	    //    S n -[n chars]
	    //      String info speficied by n characters. An upper-case S
	    //      can also signal a string value. Only understood within
	    //      an attribute spec (see below).
	    //
	    //    t n -[n chars]
	    //      Tip text.
	    //
	    //    a n n-fields
	    //    A n n-fields
	    //      Attribute information specified with n fields. A one
	    //      field attribute is a flag value, a two field attribute
	    //      is a name-value pair (e.g. a 2 title s 11 -hello world
	    //      or a 2 length n 1 25). An upper-case A can also signal
	    //      an attribute.
	    //
	    // An incomplete list of attribute examples follow:
	    //
	    //	  A 1 bold
	    //	    Text is drawn in a bold font (equivalent to: S 4 -bold)
	    //
	    //	  A 1 italic
	    //	    Text is drawn in an italic font (equivalent to: S 6 -italic)
	    //
	    //	  A 1 dashed
	    //	    Lines and curves are drawn dashed rather than solid
	    //	    (which is the default) or dotted. (equivalent to: S 6 -dashed)
	    //
	    //	  A 1 dotted
	    //	    Lines and curves are drawn dotted rather than solid
	    //	    (which is the default) or dashed. (equivalent to: S 6 -dotted)
	    //
	    //	  A 1 solid
	    //	    Lines and curves are drawn solid (this is the default)
	    //	    rather than dashed or dotted. (equivalent to S 5 -solid)
	    //
	    //	  A 1 filled
	    //	    Unnecessary when you use P or E commands, but it may
	    //	    be useful when a node is described by a spline using
	    //	    the B command. Would also work for the L command, but
	    //	    seems unnecessary because you might as well have used
	    //	    the P command. (equivalent to: S 6 -filled)
	    //
	    //	  A 2 color N 3 red green blue
	    //	    Pick the color specified by the red, green, and blue
	    //	    components, which must be integers in the range 0 to
	    //	    255 inclusive. (equivalent to: C 7 -#FF0088 or c 7 -#FF0088,
	    //      [the color value is just an example])
	    //
	    //	  A 2 fontname s n -[n chars]
	    //	    Changes the font used to draw text from the default,
	    //	    which is Times-Roman to the font named by the n chars
	    //	    that immediately follow the - character. (can be
	    //      subsumed by a "F" reference).
	    //
	    //	  A 2 fontsize N 1 pointsize
	    //	    Changes the size of the font used to draw characters
	    //	    to pointsize (the default is 14, but your graph and
	    //	    all the text are always scaled to fit into the space
	    //	    that's available). (can be subsumed by a "F" reference).
	    //
	    //	  A 2 fontcolor N 3 red green blue
	    //	    Sets the color used to draw text, which is black by
	    //	    default, to the color specified by the red, green,
	    //	    and blue components, which must be integers in the
	    //	    range 0 to 255 inclusive. (can just use a "c" reference)
	    //
	    //	  A 2 setlinewidth N 1 num
	    //	    Lines and curves are drawn num units wide (where 1 is
	    //	    the default). (equivalent to: S 15 -setlinewidth(1))
	    //
	    // We eventually should revisit this code and make it handle badly
	    // formatted input better.
	    //

	    if (text != null && delim != null) {
		element_type = 0;
		element_bounds = null;
		element_color = null;
		pen_color = null;
		label_text = null;
		label_point = null;
		label_color = null;

		in_label = false;

		filled = false;
		invisible = false;
		hidden = false;
		filtered = false;
		linewidth = 1;
		dasharray = null;

		dofont = false;
		offsets = new int[] {0, 0};
		coords = new float[4];		// decided to guarantee 4 slots

		currentfont = getDefaultFont(false);
		fontname = currentfont.getName();
		fontstyle = currentfont.getStyle();
		fontsize = currentfont.getSize();

		subpaths = new GeneralPath[] {new GeneralPath(), null, null, null};
		pathindex = 0;

		rectangle = new Rectangle2D.Float();
		ellipse = new Ellipse2D.Float();

		length = text.length();
		skip = delim.length();
		if (text.indexOf(" \0 ") < 0) {
		    if ((name = getNextToken(text, offsets, length, delim, skip)) != null) {
			if ((token = getNextToken(text, offsets, length, delim, skip)) != null) {
			    element_type = YoixMake.javaInt(token, -1);
			    if ((element_type & (DATA_EDGE_MASK|DATA_NODE_MASK)) != 0) {
				if ((element_type & DATA_MARK) != 0) {
				    ismark = true;
				    havemarks = true;
				    element_type &= ~DATA_MARK;
				}
				if ((element_type & DATA_GRAPH_REFERENCE) != 0) {
				    reference = true;
				    required = (element_type & DATA_GRAPH_REQUIRED) != 0;
				    element_type &= DATA_GRAPH_MASK;
				} else reference = false;
				crdsln = -1;
				tval = null;
				just = 0;
				spec = 0;
				mode = 0;
				while ((token = getNextToken(text, offsets, length, delim, skip, mode, in_label)) != null) {
				    switch (mode) {
					case 0:		// new sequence
					    switch (spec = (int)token.charAt(0)) {
						case '<': // start label portion
						    in_label = true;
						    pathindex = subpaths.length - 1;
						    if (subpaths[pathindex] == null)
							subpaths[pathindex] = new GeneralPath();
						    continue;
    
						case '>': // end label portion
						    in_label = false;
						    pathindex = 0;
						    continue;
    
						case 'w':	// subpath index
						    if ((token = getNextToken(text, offsets, length, delim, skip)) != null) {
							pathindex = Math.max(0, Math.min(YoixMake.javaInt(token, 0), 2));
							if (subpaths[pathindex] == null)
							    subpaths[pathindex] = new GeneralPath();
						    } else VM.abort(BADVALUE, text);
						    mode = 0;
						    continue;
    
						case 'a': // Attribute
						case 'A': // Attribute (backward compatible)
						    mode = -3;
						    break;
    
						case 'F': // Font
						    mode = -4;
						    break;
    
						case 'S': // Style
						    mode = -5;
						    break;
    
						case 'C': // Fillcolor
						    mode = -6;
						    break;
    
						case 'c': // Fillcolor
						    mode = -7;
						    break;
    
						case 'x': // position 
						    mode = -8;
						    break;
    
						case 'b': // Bézier-spline
						    filled = true;
						    mode = -1;
						    break;
    
						case 'B': // Bézier-spline
						case 'L': // polyline
						    filled = false;
						    mode = -1;
						    break;
    
						case 'P': // filled polygon
						    filled = true;
						    mode = -1;
						    break;
    
						case 'p': // unfilled polygon
						    filled = false;
						    mode = -1;
						    break;
    
						case 'E': // filled ellipse - coords has at least 4 slots
						    filled = true;
						    crdsln = 4;
						    mode = 4;
						    break;
    
						case 'e': // unfilled ellipse - coords has at least 4 slots
						    filled = false;
						    crdsln = 4;
						    mode = 4;
						    break;
    
						case 'T': // text - coords has at least 4 slots
						    mode = -2;
						    crdsln = 4;
						    break;
    
						case 't':	// tiptext: t n -[n chars]
						    if ((token = getTextToken(text, offsets, length, delim, skip)) != null)
							tiptext = (token.length()) > 0 ? token : name;
						    else VM.abort(BADVALUE, text);
						    mode = 0;
						    break;
    
						default:
						    VM.abort(BADVALUE, text);
						    break;
					    }
					    break;
    
					case -1: // coord count
					    crdsln = 2*YoixMake.javaInt(token, -1);
					    mode = crdsln;
					    if (mode <= 0)
						VM.abort(BADVALUE, text);
					    if (coords.length < crdsln)
						coords = new float[crdsln];
					    break;
    
					case -2: // text: x y j w n -[n chars]
					    coords[0] = YoixMake.javaFloat(token, -1);
					    if ((token = getNextToken(text, offsets, length, delim, skip)) != null) {
						coords[1] = YoixMake.javaFloat(token, -1);
						if ((token = getNextToken(text, offsets, length, delim, skip)) != null) {
						    just = YoixMake.javaInt(token, -1);
						    if ((token = getNextToken(text, offsets, length, delim, skip)) != null) {
							// note: width not used, so discard
							if ((token = getTextToken(text, offsets, length, delim, skip)) != null) {
							    tval = token;
							} else VM.abort(BADVALUE, text);
						    } else VM.abort(BADVALUE, text);
						} else VM.abort(BADVALUE, text);
					    } else VM.abort(BADVALUE, text);
					    mode = 0;
					    break;
    
					case -3:
					case -4:
					case -5:
					case -6:
					case -7:
					case -8:
					    switch(mode) {
					    case -3:
						na = YoixMake.javaInt(token, -1);
						token = getNextToken(text, offsets, length, delim, skip);
						nexttoken = null;
						break;
					    case -4: // font (F)
						fontsize = (int)(YoixMake.javaDouble(token, -1));
						na = 2;
						token = "fontname";
						nexttoken = "s";
						break;
					    case -5: // style (S)
						na = 1;
						if (token.endsWith(")")) {
						    styleValue(token);
						    token = " ";
						}
						nexttoken = null;
						break;
					    case -6: // color (C)
						na = 2;
						nexttoken = "s";
						break;
					    case -7: // color (c)
						na = 2;
						nexttoken = "s";
						break;
					    case -8: // position (x)
						na = 2;
						nexttoken = "n";
						break;
					    default:
						VM.abort(INTERNALERROR);
						na = 0;
						token = nexttoken = null;
						break;
					    }
					    if (token != null) {
						if (na == 1) {
						    switch (token.charAt(0)) {
							case 'b':
							    if ("bold".equals(token)) {
								fontstyle = Font.BOLD;
								dofont = true;
							    }
							    break;
    
							case 'i':
							    if ("invis".equals(token)) {
								invisible = true;
							    } else if ("italic".equals(token)) {
								fontstyle = Font.ITALIC;
								dofont = true;
							    }
							    break;
    
							case 'd':
							    if ("dashed".equals(token))
								dasharray = DASHPATTERN;
							    else if ("dotted".equals(token))
								dasharray = DOTPATTERN;
							    break;
    
							case 'f':
							    if ("filled".equals(token))
								filled = true;
							    break;
    
							case 's':
							    if ("solid".equals(token))
								dasharray = null;
							    break;
    
							default:
							    break;
						    }
						} else if (na == 2) {
						    switch (token.charAt(0)) {
							case 'c':
							    if ("color".equals(token)) {
								if ((obj = getAttributeValue(text, offsets, length, delim, skip, ATTR_COLOR, nexttoken)) != null) {
								    if (obj instanceof Color)
									element_color = (Color)obj;
								    else VM.abort(BADVALUE, text);
								} else VM.abort(BADVALUE, text);
							    } else getAttributeValue(text, offsets, length, delim, skip, ATTR_DISCARD);
							    break;
    
							case 'f':
							    if ("fontcolor".equals(token)) {
								if ((obj = getAttributeValue(text, offsets, length, delim, skip, ATTR_COLOR, nexttoken)) != null && obj instanceof Color)
								    label_color = (Color)obj;
								else VM.abort(BADVALUE, text);
							    } else if ("fontname".equals(token)) {
								if ((obj = getAttributeValue(text, offsets, length, delim, skip, ATTR_STRING, nexttoken)) != null && obj instanceof String)
								    fontname = (String)obj;
								else VM.abort(BADVALUE, text);
								dofont = true;
							    } else if ("fontsize".equals(token)) {
								if ((obj = getAttributeValue(text, offsets, length, delim, skip, ATTR_NUMBER)) != null && obj instanceof Double)
								    fontsize = ((Double)obj).intValue();
								else VM.abort(BADVALUE, text);
								dofont = true;
							    } else if ("fontstyle".equals(token)) {
								if ((obj = getAttributeValue(text, offsets, length, delim, skip, ATTR_STRING)) != null && obj instanceof String)
								    tval = ((String)obj).toLowerCase();
								else VM.abort(BADVALUE, text);
								if ("italic".equals(tval)) {
								    fontstyle = Font.ITALIC;
								    dofont = true;
								} else if ("bold".equals(tval)) {
								    fontstyle = Font.BOLD;
								    dofont = true;
								} else fontstyle = Font.PLAIN;
							    } else getAttributeValue(text, offsets, length, delim, skip, ATTR_DISCARD);
							    break;
    
							case 'p':
							    if ("pos".equals(token)) {
								if ((obj = getAttributeValue(text, offsets, length, delim, skip, ATTR_POINT, nexttoken)) != null && obj instanceof Point2D)
								    element_position = (Point2D)obj;
								else VM.abort(BADVALUE, text);
							    } else if ("pencolor".equals(token)) {
								if ((obj = getAttributeValue(text, offsets, length, delim, skip, ATTR_COLOR, nexttoken)) != null) {
								    if (obj instanceof Color)
									pen_color = (Color)obj;
								    else VM.abort(BADVALUE, text);
								} else VM.abort(BADVALUE, text);
							    } else getAttributeValue(text, offsets, length, delim, skip, ATTR_DISCARD);
							    break;
    
							case 's':
							    if ("setlinewidth".equals(token)) {
								if ((obj = getAttributeValue(text, offsets, length, delim, skip, ATTR_NUMBER)) != null && obj instanceof Double)
								    linewidth = ((Double)obj).intValue();
								else VM.abort(BADVALUE, text);
							    } else getAttributeValue(text, offsets, length, delim, skip, ATTR_DISCARD);
							    break;
    
							default:
							    getAttributeValue(text, offsets, length, delim, skip, ATTR_DISCARD);
							    break;
						    }
						} else VM.abort(BADVALUE, text);
					    } else VM.abort(BADVALUE, text);
					    mode = 0;
					    break;
    
					default: // coords
					    coords[crdsln - mode] = YoixMake.javaFloat(token, -1);
					    mode--;
					    break;
				    }
    
				    if (mode == 0) {
					switch (spec) {
					    case 'A': // Attribute
					    case 'C':
					    case 'c':
					    case 'F':
					    case 'S':
					    case 't':
					    case 'x':
						// nothing to do
						break;
    
					    case 'b': // Bézier-spline
					    case 'B': // Bézier-spline
						if (crdsln >= 8 && (crdsln - 2)%6 == 0) {
						    subpaths[pathindex].moveTo(coords[0], coords[1]);
						    for (n = 2; n < crdsln; n += 6) {
							subpaths[pathindex].curveTo(
							    coords[n], coords[n+1],
							    coords[n+2], coords[n+3],
							    coords[n+4], coords[n+5]
							);
						    }
						    hotspot = new Point2D.Double(coords[0], coords[1]);
						    //
						    // The new test was added on 3/31/05 to fix code
						    // that was added on 11/10/04 that always closed
						    // the path when filled was true. Problems arose
						    // when edges had a head or tail that was defined
						    // before the edge and that head or tail ended up
						    // setting filled.
						    //
						    if ((element_type & DATA_NODE_MASK) != 0 || pathindex > 0)
							subpaths[pathindex].closePath();
						} else VM.abort(BADVALUE, text);
						break;
    
					    case 'E': // filled ellipse
					    case 'e': // unfilled ellipse
						if (crdsln == 4) {
						    ellipse.setFrame(
							coords[0]-coords[2], coords[1]-coords[3],
							2*coords[2], 2*coords[3]
						    );
						    subpaths[pathindex].append(ellipse, false);
						    hotspot = new Point2D.Double(coords[0], coords[1]);
						} else VM.abort(BADVALUE, text);
						break;
    
					    case 'L': // polyline
						if (crdsln >= 4) {
						    subpaths[pathindex].moveTo(coords[0], coords[1]);
						    for (n = 2; n < crdsln; n += 2)
							subpaths[pathindex].lineTo(coords[n], coords[n+1]);
						    hotspot = new Point2D.Double(coords[0], coords[1]);
						    //
						    // Apparently Java 1.6.0 has some problems when
						    // use simple line segments as decorations in
						    // nodes. Not 100% sure what's going on, but it
						    // definitely looks like a Java bug that shows
						    // up in at least one of the standard graphviz
						    // tests (e.g., test with the multi-field text
						    // is a good example).
						    //
						    // The following version dependent kludge seems
						    // to help, but there's also a chance it's also
						    // platform dependent.
						    //
						    if (element_type == DATA_GRAPH_NODE && crdsln == 4) {
							if (YoixMisc.jvmCompareTo("1.6.0") >= 0)
							    subpaths[pathindex].closePath();
						    }
						} else VM.abort(BADVALUE, text);
						break;
    
					    case 'P': // filled polygon
					    case 'p': // unfilled polygon
						if (crdsln == 4) {
						    rectangle.setRect(
							coords[0]-coords[2], coords[1]-coords[3],
							2*coords[2], 2*coords[3]
						    );
						    subpaths[pathindex].append(rectangle, false);
						    hotspot = new Point2D.Double(coords[0], coords[1]);
						} else if (crdsln > 4) {
						    subpaths[pathindex].moveTo(coords[0], coords[1]);
						    for (n = 2; n < crdsln; n += 2)
							subpaths[pathindex].lineTo(coords[n], coords[n+1]);
						    if (coords[0] != coords[crdsln-2] || coords[1] != coords[crdsln-1])
							subpaths[pathindex].closePath();
						    hotspot = new Point2D.Double(coords[0], coords[1]);
						} else VM.abort(BADVALUE, text);
						break;
    
					    case 'T': // text
						if (label_text == null) {
						    n = 0;
						    label_text = new String[1];
						    label_point = new Point2D[1];
						    label_justification = new int[1];
						} else {
						    n = label_text.length;
						    String   tlabel_text[] = new String[n+1];
						    Point2D  tlabel_point[] = new Point2D[n+1];
						    int tlabel_justification[] = new int[n+1];
						    System.arraycopy(label_text, 0, tlabel_text, 0, n);
						    System.arraycopy(label_point, 0, tlabel_point, 0, n);
						    System.arraycopy(label_justification, 0, tlabel_justification, 0, n);
						    label_text = tlabel_text;
						    label_point = tlabel_point;
						    label_justification = tlabel_justification;
						}
						label_text[n] = tval;
						label_point[n] = new Point2D.Double(coords[0], coords[1]);
						label_justification[n] = just;
						break;
    
					    default:
						VM.abort(BADVALUE, text);
						break;
					}
				    }
				}
				if (dofont) {
				    font = new Font(fontname, fontstyle, fontsize);
				    saveSmallestFont(font);
				}
    
				if (subpaths[2] != null) {
				    element_paths = new GeneralPath[3];
				    element_paths[2] = subpaths[2];
				    element_paths[1] = subpaths[1];
				} else if (subpaths[1] != null) {
				    element_paths = new GeneralPath[2];
				    element_paths[1] = subpaths[1];
				} else element_paths = new GeneralPath[1];
				if (subpaths[0].getCurrentPoint() == null) {
				    if ((rectangle = getLabelBounds()) != null) {
					subpaths[0] = new GeneralPath(getLabelBounds());
					plaintext = true;
				    }
				}
				element_paths[0] = subpaths[0];
			    }
			    if ((bounds = getElementBounds()) != null) {
				if (ismark) {
				    if (markerpad.left < bounds.getWidth()/2) {
					markerpad.left = (int)Math.ceil(bounds.getWidth()/2);
					markerpad.right = markerpad.left;
				    }
				    if (markerpad.top < bounds.getHeight()/2) {
					markerpad.top = (int)Math.ceil(bounds.getHeight()/2);
					markerpad.bottom = markerpad.top;
				    }
					    
				    //
				    // These elements are positioned in data coordinates
				    // but sized in default coordinates (i.e., 72dpi), so
				    // their full contribution to databbox_loaded can't be
				    // determined at this point. It's also likely that we
				    // aren't currently trying to account for the marks
				    // even after all the data is loaded - maybe later.
				    // 
				    //
				    bounds = new Rectangle2D.Double(
					bounds.getX() + bounds.getWidth()/2,
					bounds.getY() + bounds.getHeight()/2,
					0,
					0
				    );
				}
				if (databbox_loaded == null)
				    databbox_loaded = bounds.getBounds2D();	// use a copy!!
				else databbox_loaded.add(bounds);
			    }
			}
		    }
		} else {
		    if ((name = getNextToken(text, offsets, length, delim, skip)) != null) {
			if ((token = getNextToken(text, offsets, length, delim, skip)) != null) {
			    element_type = YoixMake.javaInt(token, -1);
			    if ((element_type & (DATA_EDGE_MASK|DATA_NODE_MASK)) != 0) {
				if ((element_type & DATA_MARK) != 0) {
				    ismark = true;
				    havemarks = true;
				    element_type &= ~DATA_MARK;
				}
				if ((element_type & DATA_GRAPH_REFERENCE) != 0) {
				    reference = true;
				    required = (element_type & DATA_GRAPH_REQUIRED) != 0;
				    element_type &= DATA_GRAPH_MASK;
				} else reference = false;
			    }
			}
			havesubdata = true;
			split = text.split(" \0 ");
			set = new HashSet();
			for (m = 0; m < split.length; m++)
			    set.add(split[m]);
			subdata = set.toArray();
		    }
		}
	    }
	}


	final void
	styleValue(String token) {

	    String  value;
	    int     idx;

	    if (token != null && token.endsWith(")") && (idx = token.indexOf('(')) > 0) {
		value = token.substring(idx+1, token.length() - 1);
		token = token.substring(0, idx);
		switch(token.charAt(0)) {
		    case 's':
			if ("setlinewidth".equals(token)) {
			    try {
				linewidth = Integer.parseInt(value);
			    }
			    catch(NumberFormatException nfe) {
				VM.abort(BADVALUE, new String[] {token});
			    }
			}
			break;
		}
	    } else VM.abort(BADVALUE, new String[] {"style token", token});
	}


	final void
	unextendElement() {

	    GeneralPath  paths[];
	    Object       values[];

	    //
	    // note: match saveOriginalValues as to what is saved
	    //

	    if (original_values != null && original_values[4] != null) {
		repaint = true;
		values = (Object[])original_values[4];
		element_bounds = (Rectangle2D)values[0];
		element_paths = (GeneralPath[])values[1];
		original_values[4] = null;
	    }
	}
    }

    ///////////////////////////////////
    //
    // Inner Class
    //
    ///////////////////////////////////

    class GraphSlice {

	double  bnd_lo;
	double  bnd_hi;
	double  mbr_lo;
	double  mbr_hi;
	int     members[];

	///////////////////////////////////
	//
	// Constructors
	//
	///////////////////////////////////

	GraphSlice() {

	    bnd_lo = bnd_hi = mbr_lo = mbr_hi = 0;
	    members = new int[0];
	}

	///////////////////////////////////
	//
	// GraphData Methods
	//
	///////////////////////////////////

	final void
	add(int sb) {

	    int  newmbrs[] = new int[members.length + 1];

	    System.arraycopy(members, 0, newmbrs, 0, members.length);
	    newmbrs[members.length] = sb;
	    members = newmbrs;
	}


	final boolean
	contains(double val) {

	    return(val >= mbr_lo && val < mbr_hi);
	}


	final boolean
	intersects(double mn, double mx) {

	    return(mx >= mbr_lo && mn < mbr_hi);
	}
    }
}

