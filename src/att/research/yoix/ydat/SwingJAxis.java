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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import att.research.yoix.*;

class SwingJAxis extends YoixSwingJTextComponent

    implements Constants,
	       DataAxis,
	       MouseListener,
	       MouseMotionListener,
	       Runnable

{

    //
    // We're off by an annoying pixel in a few cases (see getSliderEnds()
    // for more info). Problem is primarily an inconsistent interpretation
    // of rectangle boundaries that needs to be cleaned up, but it should
    // wait until other problems are fixed.
    //
    // Recently added the painted boolean and used it to try to stop the
    // extra paint that happens the first time the axis is shown. Fix is
    // a kludge that seems to work, but be skeptical if you notice strange
    // behavior.
    //

    private SwingJEventPlot  eventplot = null;

    private boolean  initialized = false;
    private boolean  inverted = false;
    private boolean  painted = false;		// kludge to prevent 2 initial paints??
    private boolean  makingaxis = false;
    private boolean  frozen = false;
    private boolean  thawed = true;
    private int      orientation;		// currently set with anchor

    private double  axisends[] = null;		// current endpoints
    private double  axislimits[] = null;	// original endpoints - limits axisends
    private double  axisscale = 1.0;
    private int     axisspan;
    private int     axiswidth = 0;
    private int     tickwidth = 1;
    private int     plotlinewidth = 1;		// only used by a horizontal axis

    //
    // We manage the slider, no matter what axismodel is in control.
    //

    private Rectangle  slider = null;
    private boolean    sliderenabled = true;
    private double     sliderends[] = null;
    private Color      slidercolor = Color.lightGray;
    private int        sliderhandle;
    private int        slidermodel = 0;

    //
    // All tick and label drawing is now handled by an AxisModel.
    //

    private AxisModel  axismodel = null;

    //
    // Used to keep track of the current operation, if there is one.
    //

    private int  mouse = AVAILABLE;
    private int  mousebutton = 0;

    //
    // To avoid deadlock we currently use a separate thread to handle
    // some of the tasks requested by eventplot. Works, or at least it
    // seems to eliminate deadlock, but it may be overkill - we will
    // investigate later.
    //
    // NOTE - the priority currently must be higher than the internal
    // thread that manages the DataManager queue. Also guess both of
    // them may need to run at a prioirty higher than the thread that
    // manages the event queue? Last conjecture has not been verified
    // and I suspect the first requirement could be dropped if we did
    // a more complete job in this file of passing more work through
    // the internal thread - investigate later.
    //

    private YoixThread  thread = null;
    private boolean     threadenabled = true;
    private Vector      queue = new Vector();
    private int         priority = Thread.MAX_PRIORITY;

    //
    // Queued command identifiers.
    //

    private static final int  COMMAND_SETAXISENDS = 1;
    private static final int  COMMAND_SETENDS = 2;
    private static final int  COMMAND_SETSLIDERENDS = 3;
    private static final int  COMMAND_SETSLIDERMODEL = 4;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    SwingJAxis(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
	setAnchor(YOIX_NORTH);
	addAllListeners();
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


    public synchronized void
    mousePressed(MouseEvent e) {

	Point  p;
	int    modifiers;
	int    button;

	if (mouse == AVAILABLE) {
	    p = getEventLocation(e);
	    if (hitSliderTrack(p)) {
		modifiers = YoixMiscJFC.cookModifiers(e);
		button = modifiers & YOIX_BUTTON_MASK;
		switch (button) {
		    case YOIX_BUTTON1_MASK:
			mouse = ADJUSTING;
			if (orientation == YOIX_HORIZONTAL) {
			    if (p.x <= (2*slider.x + slider.width)/2)
				sliderhandle = slider.x;
			    else sliderhandle = slider.x + slider.width;
			} else {
			    if (p.y <= (2*slider.y + slider.height)/2)
				sliderhandle = slider.y;
			    else sliderhandle = slider.y + slider.height;
			}
			adjustSlider(p);
			break;

		    case YOIX_BUTTON2_MASK:
		    case YOIX_BUTTON3_MASK:
			if (slider.contains(p)) {
			    mouse = GRABBING;
			    if (orientation == YOIX_HORIZONTAL)
				sliderhandle = p.x;
			    else sliderhandle = p.y;
			 }
			 break;
		}
		mousebutton = (mouse != AVAILABLE) ? button : 0;
		if (mouse != AVAILABLE) {
		    addMouseMotionListener(this);
		    sliderUpdate(false);
		}
	    }
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
		if (mouse == ADJUSTING)
		    postItemEvent(YoixMisc.copyIntoArray(sliderends), false, true);
		mouse = AVAILABLE;
	    }
	    if (mouse == AVAILABLE) {
		removeMouseMotionListener(this);
		sliderUpdate(true);
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

	if (mouse != AVAILABLE) {
	    switch (mouse) {
		case ADJUSTING:
		    adjustSlider(getEventLocation(e));
		    break;

		case GRABBING:
		    dragSlider(getEventLocation(e));
		    break;
	    }
	    sliderUpdate(false);
	}
    }


    public synchronized void
    mouseMoved(MouseEvent e) {

	if (ISMAC && (e.getModifiers()&YOIX_CTRL_MASK) != 0)
	    mouseDragged(e);
    }

    ///////////////////////////////////
    //
    // Runnable Methods
    //
    ///////////////////////////////////

    public final void
    run() {

	Object  args[];

	try {
	    while (threadenabled) {
		try {
		    args = null;
		    synchronized(queue) {
			if (queue.size() == 0)
			    queue.wait();
			if (queue.size() > 0) {
			    args = (Object[])queue.elementAt(0);
			    queue.removeElementAt(0);
			}
		    }
		    handleCommand(args);
		}
		catch(InterruptedException e) {}
	    }
	}
	finally {
	    stopThread();
	}
    }

    ///////////////////////////////////
    //
    // DataAxis Methods
    //
    ///////////////////////////////////

    public final YoixObject
    callGenerator(YoixObject funct, YoixObject argv[]) {

	return(call(funct, argv));
    }


    public final double[]
    getAxisEnds() {

	double  ends[] = axisends;

	return(ends != null ? new double[] {ends[0], ends[1]} : null);
    }


    public final double[]
    getAxisLimits() {

	double  limits[] = axislimits;

	return(limits != null ? new double[] {limits[0], limits[1]} : null);
    }


    public final double
    getAxisScale() {

	return(axisscale);
    }


    public final int
    getAxisSpan() {

	return((orientation == YOIX_VERTICAL) ? viewport.height : viewport.width);
    }


    public final SwingJEventPlot
    getEventPlot() {

	return(eventplot);
    }


    public final boolean
    getInverted() {

	return(inverted);
    }


    public final int
    getLineWidth() {

	return(tickwidth);
    }


    public final int
    getOrientation() {

	return(orientation);
    }


    public final int
    getTickLength() {

	return(cellsize.height);
    }


    public final boolean
    hitAxisLimits() {

	return(Arrays.equals(axisends, axislimits));
    }


    public final void
    resetAxis() {

	reset();
    }

    ///////////////////////////////////
    //
    // SwingJAxis Methods
    //
    ///////////////////////////////////

    final synchronized void
    addAllListeners() {

	addMouseListener(this);
    }


    final void
    disableAxis() {

	stopThread();
    }


    protected final synchronized YoixObject
    eventCoordinates(AWTEvent e) {

	YoixObject  obj = null;
	double      coord;
	Point       p;

	if (e instanceof MouseEvent) {
	    if (axisends != null) {
		if (viewport.height > 0 && viewport.width > 0) {
		    p = getEventLocation((MouseEvent)e);
		    if (orientation == YOIX_HORIZONTAL) {
			if (inverted) {
			    if (eventplot.getSpread())
				coord = axisends[1] - (eventplot.getRealX(p.x) - axisends[0]);
			    else coord = axisends[1] - p.x/axisscale;
			} else {
			    if (eventplot.getSpread())
				coord = eventplot.getRealX(p.x);
			    else coord = axisends[0] + p.x/axisscale;
			}
		    } else {
			if (inverted)
			    coord = axisends[0] + p.y/axisscale;
			else coord = axisends[1] - p.y/axisscale;
		    }
		    obj = YoixMake.yoixType(T_POINT);
		    obj.put(N_X, YoixObject.newDouble(coord), false);
		    obj.put(N_Y, YoixObject.newDouble(coord), false);
		}
	    }
	}
	return(obj);
    }


    protected void
    finalize() {

	eventplot = null;
	axismodel = null;
	axisends = null;
	axislimits = null;
	slider = null;
	sliderends = null;
	super.finalize();
    }


    final double
    getAxisScale(int span, double ends[]) {

	double  scale;

	if (orientation == YOIX_HORIZONTAL)
	    scale = Math.max(span - (plotlinewidth + 1), plotlinewidth + 1)/(ends[1] - ends[0]);
	else scale = Math.max(span - tickwidth, tickwidth)/(ends[1] - ends[0]);
	return(scale);
    }


    final boolean
    getFrozen() {

	return(frozen);
    }


    protected final Dimension
    getLayoutSize(String name, Dimension size) {

	YoixObject  lval;
	YoixObject  obj;
	String      labels[];
	String      str;
	int         ticksize;
	int         width;
	int         n;

	if (loadFont()) {
	    synchronized(FONTLOCK) {
		makeAxis(false);
		ticksize = getTickLength() + 2*leading;
		if ((obj = getData().getObject(name)) != null && obj.notNull()) {
		    size = YoixMakeScreen.javaDimension(obj);
		    if (size.width <= 0 || size.height <= 0) {
			labels = generateLabels();
			if (size.width <= 0) {
			    size.width = 0;
			    if (columns <= 0) {
				if (orientation == YOIX_VERTICAL) {
				    if (labels != null) {
					for (n = 0; n < labels.length; n++) {
					    str = labels[n];
					    if ((width = fm.stringWidth(str)) > size.width)
						size.width = width;
					}
				    }
				} else {
				    if (labels != null) {
					for (n = 0; n < labels.length; n++)
					    size.width += fm.stringWidth(labels[n]);
				    }
				}
			    } else size.width = columns*cellsize.width;
			    if (orientation == YOIX_VERTICAL)
				size.width += ticksize;
			    size.width += insets.left + insets.right;
			}
			if (size.height <= 0) {
			    size.height = 0;
			    if (rows <= 0) {
				if (orientation == YOIX_VERTICAL) {
				    if (labels != null)
					size.height = labels.length*cellsize.height;
				}
			    } else size.height = rows*cellsize.height;
			    if (orientation == YOIX_HORIZONTAL)
				size.height += ticksize;
			    size.height += insets.top + insets.bottom;
			}
			if (size.width > 0 && size.height > 0) {
			    lval = YoixObject.newLvalue(getData(), name);
			    if (lval.canWrite())
				lval.put(YoixMakeScreen.yoixDimension(size));
			}
		    }
		} else {
		    if (columns > 0) {
			size.width = columns*cellsize.width + insets.left + insets.right;
			if (orientation == YOIX_VERTICAL)
			    size.width += ticksize;
		    } else if (size.width <= 0) {
			if (orientation == YOIX_VERTICAL)
			    size.width = ticksize + insets.left + insets.right;
		    }
		    if (rows > 0) {
			size.height = rows*cellsize.height + insets.top + insets.bottom;
			if (orientation == YOIX_HORIZONTAL)
			    size.height += ticksize;
		    } else if (size.height <= 0) {
			if (orientation == YOIX_HORIZONTAL)
			    size.height = ticksize + insets.top + insets.bottom;
		    }
		}
	    }
	}
	return(size);
    }


    final synchronized YoixObject
    getPlot() {

	YoixObject  obj;
	DataPlot    plot;

	if ((plot = eventplot) != null) {
	    if ((obj = plot.getContext()) == null)
		obj = YoixObject.newNull();
	} else obj = YoixObject.newNull();

	return(obj);
    }


    final Color
    getSliderColor() {

	Color  color = slidercolor;

	return(color != null ? color : Color.lightGray);
    }


    final double[]
    getSliderEnds() {

	double  ends[] = sliderends;

	return(ends != null ? new double[] {ends[0], ends[1]} : null);
    }


    final synchronized ArrayList
    getSubordinatePlots() {

	DataManager  manager;
	ArrayList    result = null;
	ArrayList    xaxis;
	ArrayList    yaxis;
	DataPlot     plot;
	int          index;
	int          n;

	if (eventplot != null) {
	    if ((manager = eventplot.getDataManager()) != null)
		if ((index = eventplot.getAxisIndex(this)) >= 0) {
		    result = manager.getSubordinatePlots(index);
		    xaxis = (ArrayList)result.get(0);
		    yaxis = (ArrayList)result.get(1);
		    for (n = 0; n < xaxis.size(); n++) {
			if ((plot = (DataPlot)xaxis.get(n)) != null)
			    xaxis.set(n, plot.getContext());
		    }
		    for (n = 0; n < yaxis.size(); n++) {
			if ((plot = (DataPlot)yaxis.get(n)) != null)
			    yaxis.set(n, plot.getContext());
		    }
		}
	}

	return(result);
    }


    final synchronized double[]
    getZoomEnds(int rotation, double lock) {

	double  ends[] = null;

	//
	// This method was added as a replacement for Yoix code that did
	// the endpoint calculations in our YDAT distribution, primarily
	// because the "spread" capabilities that we added to EventPlots
	// complicate the calculations and really don't belong in scripts.
	// 

	if (eventplot != null && axislimits != null) {
	    if ((ends = eventplot.getZoomEnds(this, lock, rotation)) != null) {
		ends[0] = Math.max(ends[0], axislimits[0]);
		ends[1] = Math.min(ends[1], axislimits[1]);
	    }
	}
	return(ends);
    }


    public final void
    paint(Graphics g) {

	super.paintBackground(g);
	paintBackgroundImage(g);
	paintBorder(insets, g);

	if (axisends != null) {
	    g.translate(insets.left, insets.top);
	    paintRect(viewport.x, viewport.y, viewport.width, viewport.height, g);
	    g.translate(-insets.left, -insets.top);
	}
	painted = true;			// painted kludge
    }


    protected final synchronized void
    paintRect(int x, int y, int width, int height, Graphics g) {

	Rectangle  rect;
	Shape      clip;

	if (axisends != null) {
	    clip = g.getClip();
	    g.translate(-viewport.x, -viewport.y);
	    g.clipRect(x, y, width, height);		// recent change
	    rect = g.getClipBounds();
	    repaintAxis(rect.x, rect.y, rect.width, rect.height, g);
	    g.translate(viewport.x, viewport.y);
	    g.setClip(clip);
	}
    }


    final synchronized void
    removeAllListeners() {

	removeMouseListener(this);
	removeMouseMotionListener(this);
    }


    final void
    reset() {

	if (makeAxis(true)) {
	    if (initialized)
		repaint();
	}
    }


    protected final synchronized void
    setAlignment(int alignment) {

    }


    protected final synchronized void
    setAnchor(int anchor) {

	if (this.anchor != anchor) {
	    switch (anchor) {
		case YOIX_NORTH:
		case YOIX_NORTHEAST:
		case YOIX_NORTHWEST:
		case YOIX_TOP:
		default:
		    this.anchor = YOIX_NORTH;
		    orientation = YOIX_HORIZONTAL;
		    break;

		case YOIX_SOUTH:
		case YOIX_SOUTHEAST:
		case YOIX_SOUTHWEST:
		case YOIX_BOTTOM:
		    this.anchor = YOIX_SOUTH;
		    orientation = YOIX_HORIZONTAL;
		    break;

		case YOIX_EAST:
		case YOIX_RIGHT:
		    this.anchor = YOIX_EAST;
		    orientation = YOIX_VERTICAL;
		    break;

		case YOIX_WEST:
		case YOIX_LEFT:
		    this.anchor = YOIX_WEST;
		    orientation = YOIX_VERTICAL;
		    break;
	    }
	    reset();
	}
    }


    final void
    setAxisEnds() {

	setAxisEnds(false);
    }


    final void
    setAxisEnds(boolean saveslider) {

	synchronized(queue) {
	    queue.addElement(
		new Object[] {
		    new Integer(COMMAND_SETAXISENDS),
		    new Boolean(saveslider)
		}
	    );
	    startThread();
	}
    }


    final void
    setAxisEnds(double low, double high) {

	synchronized(queue) {
	    queue.addElement(
		new Object[] {
		    new Integer(COMMAND_SETAXISENDS),
		    new Double(low),
		    new Double(high)
		}
	    );
	    startThread();
	}
    }


    final synchronized void
    setAxisModel(int model) {

	//
	// We currently reject changes after its been officially set once,
	// but permissions will probably prevent the call. The models that
	// have associated cases in the switch statement have been claimed
	// and should not be changed unless you know exactly what you are
	// doing!!!
	//

	if (axismodel == null) {
	    switch (model) {
		case 0:
		case 1:
		    axismodel = new AxisModelDefault(this);
		    break;

		case 2:
		case 3:
		    axismodel = new AxisModelUnixTime(this);
		    break;

		default:
		    axismodel = new AxisModelDefault(this);
		    break;
	    }
	    reset();
	}
    }


    final synchronized void
    setAxisWidth(int width) {

	width = Math.max(0, width);
	if (axiswidth != width) {
	    axiswidth = width;
	    reset();
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
	    makeAxis(true);
	} else super.setBounds(x, y, width, height);
    }


    public final synchronized void
    setEnabled(boolean state) {

	if (state != isEnabled()) {
	    super.setEnabled(state);
	    reset();
	}
    }


    final void
    setEnds(double low, double high, int slidermode) {

	synchronized(queue) {
	    queue.addElement(
		new Object[] {
		    new Integer(COMMAND_SETENDS),
		    new Double(low),
		    new Double(high),
		    new Integer(slidermode)
		}
	    );
	    startThread();
	}
    }


    final synchronized void
    setEventPlot(SwingJEventPlot plot) {

	double  ends[];

	if (eventplot != plot) {
	    eventplot = null;
	    handleSetAxisEnds(false);
	    if (plot != eventplot) {
		eventplot = plot;
		if ((ends = eventplot.getAxisEnds(this)) != null)
		    handleSetAxisEnds(ends[0], ends[1]);
	    }
	}
    }


    final synchronized void
    setFrozen(boolean state) {

	frozen = state;
	if (frozen == false) {
	    if (thawed == false)
		reset();
	}
    }


    final synchronized void
    setInverted(boolean state) {

	if (inverted != state) {
	    inverted = state;
	    reset();
	}
    }


    final synchronized void
    setLabels(YoixObject obj) {

	axismodel.setLabels(obj);
    }


    final synchronized void
    setLineWidth(int width) {

	width = Math.max(1, width);
	if (tickwidth != width) {
	    tickwidth = width;
	    reset();
	}
    }


    final synchronized void
    setPlotLineWidth(int width) {

	width = Math.max(0, width);
	if (plotlinewidth != width) {
	    plotlinewidth = width;
	    reset();
	}
    }


    final synchronized void
    setSliderColor(Color color) {

	if (slidercolor == null || slidercolor.equals(color) == false) {
	    if (color == null)
		slidercolor = Color.lightGray;
	    else slidercolor = color;
	    reset();
	}
    }


    final synchronized void
    setSliderEnabled(boolean state) {

	if (sliderenabled != state) {
	    sliderenabled = state;
	    reset();
	}
    }


    final void
    setSliderEnds(double ends[]) {

	synchronized(queue) {
	    queue.addElement(
		new Object[] {
		    new Integer(COMMAND_SETSLIDERENDS),
		    ends
		}
	    );
	    startThread();
	}
    }


    final void
    setSliderModel(int model) {

	synchronized(queue) {
	    queue.addElement(
		new Object[] {
		    new Integer(COMMAND_SETSLIDERMODEL),
		    new Integer(model)
		}
	    );
	    startThread();
	}
    }


    final synchronized void
    setTicks(YoixObject obj) {

	axismodel.setTicks(obj);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private synchronized void
    adjustSlider(Point p) {

	int  location;
	int  first;
	int  last;
	int  low;
	int  high;

	if (slider != null && sliderends != null) {
	    if (orientation == YOIX_HORIZONTAL) {
		location = p.x;
		first = slider.x;
		last = first + slider.width;
	    } else {
		location = p.y;
		first = slider.y;
		last = first + slider.height;
	    }

	    if (location > last) {
		if (sliderhandle == first)
		    first = last;
		last = location;
		low = sliderhandle;
		high = location;
	    } else if (location < first) {
		if (sliderhandle == last)
		    last = first;
		first = location;
		low = location;
		high = sliderhandle;
	    } else {
		if (sliderhandle == first) {
		    first = location;
		    low = sliderhandle;
		    high = location;
		} else {
		    last = location;
		    low = location;
		    high = sliderhandle;
		}
	    }

	    sliderhandle = location;
	    if (orientation == YOIX_HORIZONTAL) {
		if (inverted) {
		    if (eventplot.getSpread()) {
			sliderends[1] = axisends[1] - (eventplot.getRealX(first) - axisends[0]);
			sliderends[0] = axisends[0] + (axisends[1] - eventplot.getRealX(last));
		    } else {
			sliderends[0] = axisends[0] + (axisspan - (last + 1))/axisscale;
			sliderends[1] = axisends[1] - first/axisscale;
		    }
		} else {
		    if (eventplot.getSpread()) {
			sliderends[0] = eventplot.getRealX(first);
			sliderends[1] = eventplot.getRealX(last);
		    } else {
			sliderends[0] = axisends[0] + first/axisscale;
			sliderends[1] = axisends[1] - (axisspan - (last + 1))/axisscale;
		    }
		}
		slider.x = first;
		slider.width = last - first;
	    } else {
		if (inverted) {
		    sliderends[0] = axisends[0] + first/axisscale;
		    sliderends[1] = axisends[1] - (axisspan - (last + 1))/axisscale;
		} else {
		    sliderends[0] = axisends[0] + (axisspan - (last + 1))/axisscale;
		    sliderends[1] = axisends[1] - first/axisscale;
		}
		slider.y = first;
		slider.height = last - first;
	    }

	    //
	    // Small kludge to keep slider in bounds - tried other obvious
	    // choices, but this worked best.
	    //
	    sliderends[0] = Math.max(sliderends[0], axisends[0]);
	    sliderends[1] = Math.min(sliderends[1], axisends[1]);
	    repaintSlider(low, high);
	}
    }


    private void
    axisUpdate() {

	SwingJEventPlot  plot;
	double           ends[];

	if ((plot = eventplot) != null) {
	    if ((ends = axisends) != null)
		plot.axisUpdate(this, ends[0], ends[1]);
	}
    }


    private synchronized void
    dragSlider(Point p) {

	int  location;
	int  delta;

	if (slider != null) {
	    if (orientation == YOIX_HORIZONTAL) {
		location = p.x;
		delta = location - sliderhandle;
		if (slider.x + slider.width + delta > viewport.x + viewport.width)
		    delta = viewport.x + viewport.width - slider.x - slider.width;
		if (slider.x + delta < viewport.x)
		    delta = viewport.x - slider.x;
		if (delta > 0) {
		    sliderhandle = slider.x + slider.width;
		    p.x = sliderhandle + delta;
		    adjustSlider(p);
		    sliderhandle = slider.x;
		    p.x = sliderhandle + delta;
		    adjustSlider(p);
		} else if (delta < 0) {
		    sliderhandle = slider.x;
		    p.x = sliderhandle + delta;
		    adjustSlider(p);
		    sliderhandle = slider.x + slider.width;
		    p.x = sliderhandle + delta;
		    adjustSlider(p);
		}
		sliderhandle = location;
	    } else {
		location = p.y;
		delta = location - sliderhandle;
		if (slider.y + slider.height + delta > viewport.y + viewport.height)
		    delta = viewport.y + viewport.height - slider.y - slider.height;
		if (slider.y + delta < viewport.y)
		    delta = viewport.y - slider.y;
		if (delta > 0) {
		    sliderhandle = slider.y + slider.height;
		    p.y = sliderhandle + delta;
		    adjustSlider(p);
		    sliderhandle = slider.y;
		    p.y = sliderhandle + delta;
		    adjustSlider(p);
		} else if (delta < 0) {
		    sliderhandle = slider.y;
		    p.y = sliderhandle + delta;
		    adjustSlider(p);
		    sliderhandle = slider.y + slider.height;
		    p.y = sliderhandle + delta;
		    adjustSlider(p);
		}
		sliderhandle = location;
	    }
	}
    }


    private synchronized void
    drawAxisTop(int low, int high, Graphics g) {

	int  width;
	int  height;
	int  x;
	int  y;

	if (axisends != null && axiswidth > 0) {
	    switch (anchor) {
		default:			// YOIX_NORTH
		    height = axiswidth;
		    width = high - low;
		    x = low;
		    y = 0;
		    break;

		case YOIX_SOUTH:
		    height = axiswidth;
		    width = high - low;
		    x = low;
		    y = viewport.height - height;
		    break;

		case YOIX_EAST:
		    height = high - low;
		    width = axiswidth;
		    x = viewport.width - width;
		    y = low;
		    break;

		case YOIX_WEST:
		    height = high - low;
		    width = axiswidth;
		    x = 0;
		    y = low;
		    break;
	    }
	    g.fillRect(x, y, width, height);
	}
    }


    private synchronized void
    drawLabels(int low, int high, Graphics g) {

	axismodel.drawLabels(low, high, g);
    }


    private synchronized void
    drawSlider(int low, int high, Graphics g) {

	Rectangle  rect;
	Color      color;

	if (slider != null && sliderenabled) {
	    if (orientation == YOIX_HORIZONTAL)
		rect = new Rectangle(low, slider.y, high - low, slider.height);
	    else rect = new Rectangle(slider.x, low, slider.width, high - low);
	    if ((rect = slider.intersection(rect)) != null) {
		color = g.getColor();
		g.setColor(slidercolor);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(color);
	    }
	}
    }


    private synchronized void
    drawTicks(int low, int high, Graphics g) {

	axismodel.drawTicks(low, high, g);
    }


    private void
    eraseAxis(int low, int high, Graphics g) {

	Color  color;

	color = g.getColor();
	g.setColor(getBackground());
	if (orientation == YOIX_HORIZONTAL)
	    g.fillRect(low, 0, high - low, viewport.height);
	else g.fillRect(0, low, viewport.width, high - low);
	g.setColor(color);
    }


    private synchronized void
    eraseSlider(int low, int high, Graphics g) {

	Color  color;

	color = g.getColor();
	g.setColor(getBackground());
	if (orientation == YOIX_HORIZONTAL)
	    g.fillRect(low, slider.y, high - low, slider.height);
	else g.fillRect(slider.x, low, slider.width, high - low);
	g.setColor(color);
    }


    private Point
    getEventLocation(MouseEvent e) {

	Point  p;

	p = e.getPoint();
	p.x -= insets.left;
	p.y -= insets.top;
	if (viewport.contains(p) == false) {
	    p.x = Math.max(Math.min(p.x, viewport.x + viewport.width - 1), viewport.x);
	    p.y = Math.max(Math.min(p.y, viewport.y + viewport.height - 1), viewport.y);
	}
	return(p);
    }


    private void
    handleCommand(Object args[]) {

	if (args != null && args.length > 0) {
	    switch (((Integer)args[0]).intValue()) {
		case COMMAND_SETAXISENDS:
		    if (args.length == 3) {
			handleSetAxisEnds(
			    ((Double)args[1]).doubleValue(),
			    ((Double)args[2]).doubleValue()
			);
		    } else handleSetAxisEnds(((Boolean)args[1]).booleanValue());
		    break;

		case COMMAND_SETENDS:
		    handleSetEnds(
			((Double)args[1]).doubleValue(),
			((Double)args[2]).doubleValue(),
			((Integer)args[3]).intValue()
		    );
		    break;

		case COMMAND_SETSLIDERENDS:
		    handleSetSliderEnds((double[])args[1]);
		    break;

		case COMMAND_SETSLIDERMODEL:
		    handleSetSliderModel(((Integer)args[1]).intValue());
		    break;
	    }
	}
    }


    private synchronized void
    handleSetAxisEnds(boolean saveslider) {

	//
	// Moving the postItemEvent() call into makeAxis() is tempting,
	// but there may be cases where you end up in deadlock. If you
	// try be certain to test thoroughly, particularly some of the
	// more complicated examples!!
	//

	if (axisends != null) {
	    axisends = null;
	    axislimits = null;
	    slider = null;
	    sliderends = (saveslider) ? sliderends : null;
	    axismodel.syncToEnds(axisends);
	    reset();
	    if (hasItemListener())
		postItemEvent(YoixMisc.copyIntoArray(getAxisEnds()), true, true);
	}
    }


    private void
    handleSetAxisEnds(double low, double high) {

	boolean  update = false;

	//
	// Moving the postItemEvent() call into makeAxis() is tempting,
	// but there may be cases where you end up in deadlock. If you
	// try be certain to test thoroughly, particularly some of the
	// more complicated examples!!
	//

	if (low < high) {
	    synchronized(this) {
		if (axisends == null || axisends[0] != low || axisends[1] != high) {
		    if (axislimits == null)
			axislimits = new double[] {low, high};
		    //
		    // A recent experiment (6/18/06) now always sets
		    // sliderends, which previously version was only
		    // done if sliderends was null. The old code was
		    //
		    //    if (sliderends == null)
		    //        sliderends = new double[] {low, high};
		    // 
		    // but the new version seems to behave properly.
		    // Still likely are some minor issues related to
		    // the fact that this method call was queued but
		    // the caller might call getSliderEnds() before
		    // we get a chance to run. Eventually should take
		    // a closer look here and in SwingJEventPlot. May
		    // even want to revist the idea of handling these
		    // changes in a separate thread.
		    //
		    // NOTE - we restored the old version (on 7/5/06(),
		    // but didn't address any other related issues. The
		    // right approach may be to make getAxisEnds() and
		    // getSliderEnds() go through the same thread. Not
		    // well thought out yet, so be careful. Code before
		    // this last change always set sliderends[].
		    //
		    if (sliderends == null || slidermodel == 1)
			sliderends = new double[] {low, high};
		    axisends = new double[] {low, high};
		    if (axisends[0] < axislimits[0])
			axisends[0] = axislimits[0];
		    if (axisends[1] > axislimits[1])
			axisends[1] = axislimits[1];

		    if (sliderends[0] < axisends[0]) {
			sliderends[0] = axisends[0];
			if (sliderends[1] < sliderends[0])
			    sliderends[1] = sliderends[0];
			else if (sliderends[1] > axisends[1])
			    sliderends[1] = axisends[1];
		    } else if (sliderends[1] > axisends[1]) {
			sliderends[1] = axisends[1];
			if (sliderends[0] > sliderends[1])
			    sliderends[0] = sliderends[1];
			else if (sliderends[0] < axisends[0])	// can't happen
			    sliderends[0] = axisends[0];
		    }
		    if (hitAxisLimits())
			axismodel.syncToAxisLimits();
		    reset();
		    update = true;
		}
	    }
	    if (update) {
		sliderUpdate(false);
		axisUpdate();
		if (hasItemListener())
		    postItemEvent(YoixMisc.copyIntoArray(getAxisEnds()), true, true);
	    }
	}
    }


    private void
    handleSetEnds(double low, double high, int slidermode) {

	boolean  update = false;

	//
	// Moving the postItemEvent() call into makeAxis() is tempting,
	// but there may be cases where you end up in deadlock. If you
	// try be certain to test thoroughly, particularly some of the
	// more complicated examples!!
	//

	if (low < high) {
	    if (slidermode > 0) {
		synchronized(this) {
		    if (axisends == null || axisends[0] != low || axisends[1] != high) {
			if (axislimits == null)
			    axislimits = new double[] {low, high};
			axisends = new double[] {low, high};
			if (axisends[0] < axislimits[0])
			    axisends[0] = axislimits[0];
			if (axisends[1] > axislimits[1])
			    axisends[1] = axislimits[1];
			switch (slidermode) {
			    case 1:
				sliderends = new double[] {axisends[0], axisends[1]};
				break;

			    case 2:
				//
				// This should really only happen if we're
				// vertical, but for now we'll just assume
				// everyone is behaving properly.
				//
				sliderends = new double[] {sliderends[0], axisends[1]};
				break;
			}
			if (hitAxisLimits())
			    axismodel.syncToAxisLimits();
			reset();
			update = true;
		    }
		}
		if (update) {
		    sliderUpdate(false);
		    axisUpdate();
		    if (hasItemListener())
			postItemEvent(YoixMisc.copyIntoArray(getAxisEnds()), true, true);
		}
	    } else handleSetAxisEnds(low, high);
	}
    }


    private void
    handleSetSliderEnds(double ends[]) {

	Rectangle  oldslider = null;
	double     oldends[];

	//
	// No real idea why we need oldslider. In older versions for
	// some reason we called repaintSlider(oldslider) which didn't
	// give us the right results. Perhaps I was planning on doing
	// a minimal update based on comparing old and current slider,
	// but that is in repaintSlider() right now. Eventually might
	// be worth implementing, but it's a low priority right now.
	//

	if (ends != null) {
	    if (mouse == AVAILABLE) {		// a recent addition
		if (ends.length == 0) {
		    if ((oldends = axisends) != null)
			oldslider = setSliderEnds(oldends[0], oldends[1]);
		} else if (ends.length == 2)
		    oldslider = setSliderEnds(ends[0], ends[1]);
		if (oldslider != null) {
		    repaint();
		    sliderUpdate(false);
		}
	    }
	}
    }


    private void
    handleSetSliderModel(int model) {

	//
	// Only used internally by the associated plot and currently only
	// needed to adjust slider initialization when plots are stacked.
	// We assume, for now anyway, that nothing else needs to be done
	// because the caller is disciplined and only changes slidermodel
	// at approrpiate times (i.e., when the axis is reset by making a
	// to setAxisEnds(false)).
	//

	slidermodel = model;
    }


    private boolean
    hitSliderTrack(Point p) {

	boolean  result;

	//
	// Better than Rectangle.contains because we get a good answer
	// when the slider has zero width or height.
	//

	if (slider != null) {
	    if (orientation == YOIX_HORIZONTAL)
		result = slider.y <= p.y && p.y < slider.y + slider.height;
	    else result = slider.x <= p.x && p.x < slider.x + slider.width;
	} else result = false;

	return(result);
    }


    private synchronized boolean
    makeAxis(boolean force) {

	boolean  repaint = false;

	//
	// Tries to prevent recursion that could happen when NL_TICKS is
	// a function that changes active fields that also call reset().
	// For example, the NL_TICKS function could change the NL_LABELS
	// field, and that could send us into an infinite loop. The tests
	// seem to work, but they're a recent addition (3/1/03).
	//

	if (axisends != null) {
	    if (initialized == false || force) {
		if (makingaxis == false) {
		    if (frozen == false) {
			try {
			    makingaxis = true;
			    repaint = true;
			    axisspan = (orientation == YOIX_VERTICAL) ? viewport.height : viewport.width;
			    axisscale = getAxisScale(axisspan, axisends);
			    makeTicks();
			    makeSlider();
			}
			finally {
			    initialized = true;
			    makingaxis = false;
			    thawed = true;
			}
		    } else thawed = false;
		}
	    }
	}
	return(repaint);
    }


    private synchronized String[]
    generateLabels() {

	return(axismodel.generateLabels());
    }


    private synchronized void
    makeSlider() {

	int  height;
	int  width;
	int  x;
	int  y;

	if (sliderenabled) {
	    switch (anchor) {
		default:			// YOIX_NORTH
		    x = viewport.x;
		    y = viewport.y;
		    width = viewport.width;
		    height = (int)(.50*getTickLength());
		    break;

		case YOIX_SOUTH:
		    x = viewport.x;
		    y = viewport.height - (int)(.50*getTickLength());
		    width = viewport.width;
		    height = (int)(.50*getTickLength());
		    break;

		case YOIX_EAST:
		    x = viewport.width - (int)(.50*getTickLength());
		    y = viewport.y;
		    width = (int)(.50*getTickLength());
		    height = viewport.height;
		    break;

		case YOIX_WEST:
		    x = viewport.x;
		    y = viewport.y;
		    width = (int)(.50*getTickLength());
		    height = viewport.height;
		    break;
	    }
	    if (width > 0 && height > 0) {
		slider = new Rectangle(x, y, width, height);
		setSliderEnds();
	    } else slider = null;
	} else slider = null;
    }


    private synchronized void
    makeTicks() {

	axismodel.makeTicks();
    }


    private double
    pickTickIncrement(double incr) {

	double  delta;
	int     count;

	if (incr <= 0) {
	    if (axislimits != null) {
		if ((delta = axislimits[1] - axislimits[0]) > 0) {
		    if (incr > -1) {
			switch (orientation) {
			    case YOIX_VERTICAL:
				if (viewport.height > 0 && cellsize.height > 0) {
				    count = (viewport.height/cellsize.height + 1)/2;
				    count = Math.max(1, Math.min(count, 10));
				} else count = 10;
				break;

			    case YOIX_HORIZONTAL:
				if (viewport.width > 0 && cellsize.width > 0) {
				    count = (viewport.width/cellsize.width + 1)/2;
				    count = Math.max(1, Math.min(count, 10));
				} else count = 10;
				break;

			    default:
				count = 10;
				break;
			}
		    } else count = -(int)incr;
		    incr = delta/count;
		} else incr = 10;
	    } else incr = 10;
	}
	return(incr);
    }


    private void
    repaintAxis(int x, int y, int width, int height, Graphics g) {

	int  low;
	int  high;

	if (orientation == YOIX_HORIZONTAL) {
	    low = x;
	    high = x + width;
	} else {
	    low = y;
	    high = y + height;
	}

	g.setColor(getForeground());
	g.setFont(getFont());
	eraseAxis(low, high, g);
	drawSlider(low, high, g);
	drawTicks(low, high, g);
	drawAxisTop(low, high, g);
	drawLabels(low, high, g);
    }


    private void
    repaintSlider(int low, int high) {

	Graphics g;

	if ((g = getSavedGraphics()) != null) {
	    g.translate(insets.left, insets.top);
	    g.setColor(getForeground());
	    g.setFont(getFont());
	    eraseSlider(low, high, g);
	    drawSlider(low, high, g);
	    drawTicks(low, high, g);
	    drawAxisTop(low, high, g);
	    g.translate(-insets.left, -insets.top);
	    disposeSavedGraphics(g);
	}
    }


    private synchronized Rectangle
    setSliderEnds() {

	return(sliderends != null ? setSliderEnds(sliderends[0], sliderends[1]) : null);
    }


    private synchronized Rectangle
    setSliderEnds(double low, double high) {

	Rectangle  oldslider = null;
	int        pixelends[];

	//
	// Calculation of integer slider dimension from the endpoints of
	// the slider and axis appears harder than you might expect, but
	// it yields results that are better than the simpler approach.
	//

	if (low <= high) {
	    if (axisends != null && sliderends != null && slider != null) {
		oldslider = new Rectangle(slider);
		sliderends[0] = Math.max(Math.min(low, high), axisends[0]);
	    	sliderends[1] = Math.min(Math.max(low, high), axisends[1]);
	    	if (orientation == YOIX_HORIZONTAL) {
		    if (inverted) {
			if (eventplot.getSpread()) {
			    pixelends = eventplot.getSliderPixelEnds(sliderends[0], sliderends[1]);
			    slider.x = pixelends[0];
			    slider.width = pixelends[1] - pixelends[0] + 1;
			} else {
			    slider.x = (int)(axisscale*(axisends[1] - sliderends[1]));
			    slider.width = (int)(axisspan - slider.x - axisscale*(sliderends[0] - axisends[0]));
			}
		    } else {
			if (eventplot.getSpread()) {
			    pixelends = eventplot.getSliderPixelEnds(sliderends[0], sliderends[1]);
			    slider.x = pixelends[0];
			    slider.width = pixelends[1] - pixelends[0] + 1;
			} else {
			    slider.x = (int)(axisscale*(sliderends[0] - axisends[0]));
			    slider.width = (int)(axisspan - slider.x - axisscale*(axisends[1] - sliderends[1]));
			}
		    }
		} else {
		    if (inverted) {
			slider.y = (int)(axisscale*(sliderends[0] - axisends[0]));
			slider.height = (int)(axisspan - slider.y - axisscale*(axisends[1] - sliderends[1]));
		    } else {
			slider.y = (int)(axisscale*(axisends[1] - sliderends[1]));
			slider.height = (int)(axisspan - slider.y - axisscale*(sliderends[0] - axisends[0]));
		    }
		}
	    }
	}
	return(oldslider);
    }


    private void
    sliderUpdate(boolean released) {

	SwingJEventPlot  plot;
	double           ends[];

	if (sliderenabled) {
	    if ((plot = eventplot) != null) {
		if ((ends = sliderends) != null)
		    plot.sliderUpdate(this, ends[0], ends[1], released);
	    }
	}
    }


    private void
    startThread() {

	synchronized(queue) {
	    if (thread == null) {
		try {
		    threadenabled = true;
		    thread = new YoixThread(this);
		    thread.setPriority(Math.min(priority, Thread.MAX_PRIORITY));
		    thread.start();
		}
		catch(IllegalThreadStateException e) {
		    stopThread();
		}
	    } else queue.notifyAll();
	}
    }


    private void
    stopThread() {

	synchronized(queue) {
	    threadenabled = false;
	    queue.removeAllElements();
	    queue.notifyAll();
	    thread = null;
	}
    }
}

