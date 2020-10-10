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
import java.util.Vector;
import att.research.yoix.*;

public abstract
class SwingDataColorer extends YoixSwingJTextComponent

    implements Constants,
	       ComponentListener,
	       DataColorer,
	       MouseListener,
	       MouseMotionListener,
	       YoixInterfaceShowing

{

    //
    // Although not currently required, we have included the painted kludge
    // that was recently added to several other specialized components. The
    // fix, in the other components, eliminates an extra paint that happens
    // the first time the component is shown. Currently not an issue, but
    // decided to add the kludge anyway.
    // 

    boolean  painted = false;
    boolean  intransient = false;
    boolean  frozen = false;
    Color    dragcolor = DRAGCOLOR;
    Color    emptycolor = EMPTYCOLOR;
    Color    highlightcolor = HIGHLIGHTCOLOR;
    Color    othercolor = OTHERCOLOR;
    Color    pressedcolor = PRESSEDCOLOR;
    Color    pressingcolor = PRESSINGCOLOR;
    Color    sweepcolor = SWEEPCOLOR;
    Color    zoomincolor = ZOOMINCOLOR;
    Color    zoomoutcolor = ZOOMOUTCOLOR;
    int      labelflags = 0;
    int      selectedcount = 0;
    int      totalcount = 0;

    //
    // Used to keep track of the current operation, if there is one.
    //

    int  mouse = AVAILABLE;
    int  mousebutton = 0;

    //
    // Remembering callback functions helps reduce lookup and checking
    // overhead.
    //

    private YoixObject  afterload = null;
    private YoixObject  afterpressed = null;
    private YoixObject  aftersweep = null;
    private YoixObject  afterupdate = null;

    //
    // Arrays used to map event modifiers into actions.
    //

    private static final int  DEFAULT_OPERATIONS[] = {VL_OP_SELECT, VL_OP_ZOOM, VL_OP_PRESS, VL_OP_TIP};
    private int               operations[] = (int[])DEFAULT_OPERATIONS.clone();

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    SwingDataColorer(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
	addAllListeners();
	this.parent = parent;
    }

    ///////////////////////////////////
    //
    // ComponentListener Methods
    //
    ///////////////////////////////////

    public void
    componentHidden(ComponentEvent e) {

	handleShowingChange(false);
    }


    public void
    componentMoved(ComponentEvent e) {

    }


    public void
    componentResized(ComponentEvent e) {

    }


    public void
    componentShown(ComponentEvent e) {

	handleShowingChange(true);
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

    }


    public void
    mouseReleased(MouseEvent e) {

    }

    ///////////////////////////////////
    //
    // MouseMotionListener Methods
    //
    ///////////////////////////////////

    public void
    mouseDragged(MouseEvent e) {

    }


    public void
    mouseMoved(MouseEvent e) {

    }

    ///////////////////////////////////
    //
    // SwingDataColorer Methods
    //
    ///////////////////////////////////

    final synchronized void
    addAllListeners() {

	addComponentListener(this);
	addMouseListener(this);
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
    afterSweep(int operation) {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = aftersweep) != null) {
	    if (funct.callable(1))
		argv = new YoixObject[] {YoixObject.newInt(operation)};
	    else argv = new YoixObject[0];
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

	super.finalize();
    }


    final YoixObject
    getAfterPressed() {

	return(afterpressed);
    }


    final synchronized Color
    getDragColor() {

	return(dragcolor != null ? dragcolor : DRAGCOLOR);
    }


    final synchronized Color
    getEmptyColor() {

	return(emptycolor != null ? emptycolor : EMPTYCOLOR);
    }


    final boolean
    getFrozen() {

	return(frozen);
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


    final synchronized int
    getOperation(int modifiers) {

	int  op;

	if ((modifiers & YOIX_CTRL_MASK) == 0) {
	    if ((modifiers & YOIX_SHIFT_MASK) != 0)
		op = operations[SHIFT_OP];
	    else op = operations[PLAIN_OP];
	} else {
	    if ((modifiers & YOIX_SHIFT_MASK) != 0)
		op = operations[CONTROL_SHIFT_OP];
	    else op = operations[CONTROL_OP];
	}

	return(op);
    }


    final synchronized YoixObject
    getOperations() {

	return(YoixMisc.copyIntoArray(operations));
    }


    final synchronized Color
    getOtherColor() {

	return(othercolor != null ? othercolor : OTHERCOLOR);
    }


    final synchronized Color
    getPressedColor() {

	return(pressedcolor != null ? pressedcolor : PRESSEDCOLOR);
    }


    final synchronized Color
    getPressingColor() {

	return(pressingcolor != null ? pressingcolor : PRESSINGCOLOR);
    }


    final synchronized Color
    getSweepColor() {

	return(sweepcolor != null ? sweepcolor : SWEEPCOLOR);
    }


    final synchronized Color
    getZoomInColor() {

	return(zoomincolor != null ? zoomincolor : ZOOMINCOLOR);
    }


    final synchronized Color
    getZoomOutColor() {

	return(zoomoutcolor != null ? zoomoutcolor : ZOOMOUTCOLOR);
    }


    public void
    paint(Graphics g) {

	super.paintBackground(g);
	paintBackgroundImage(g);
	paintBorder(insets, g);
	paintRect(g);
	painted = true;			// painted kludge
    }


    protected void
    paintRect(Graphics g) {

	Font  font;

	//
	// A brute force implementation that always works, but might paint
	// more than needed. Classes that extend this one can provide their
	// own implementation when there's a more efficient approach.
	// 

	g.translate(insets.left, insets.top);
	font = g.getFont();
	g.setFont(getFont());
	paintRect(viewport.x, viewport.y, viewport.width, viewport.height, g);
	g.setFont(font);
	g.translate(-insets.left, -insets.top);
    }


    final Color
    pickHighlightColor(Color color) {

	return(highlightcolor == null ? pickAdjustedColor(color, 0.2) : highlightcolor);
    }


    final Color
    pickPressedColor(Color color) {

	return(pressedcolor == null ? pickAdjustedColor(color, -0.2) : pressedcolor);
    }


    final Color
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


    final synchronized void
    removeAllListeners() {

	removeComponentListener(this);
	removeMouseListener(this);
	removeMouseMotionListener(this);
    }


    void
    reset() {

	setViewportSize();
	setExtent();
	repaint();
    }


    final void
    reset(boolean sync) {

	reset();
	if (sync && totalcount > 0)	// another recent addition
	    syncViewport();
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
    setAfterSweep(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		aftersweep = obj;
	    else VM.abort(TYPECHECK, NL_AFTERSWEEP);
	} else aftersweep = null;
    }


    final synchronized void
    setAfterUpdate(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		afterupdate = obj;
	    else VM.abort(TYPECHECK, NL_AFTERUPDATE);
	} else afterupdate = null;
    }


    final synchronized void
    setDragColor(Color color) {

	dragcolor = (color != null) ? color : DRAGCOLOR;
    }


    final synchronized void
    setEmptyColor(Color color) {

	color = (color != null) ? color : EMPTYCOLOR;

	if (emptycolor == null || emptycolor.equals(color) == false) {
	    emptycolor = color;
	    reset();
	}
    }


    synchronized void
    setFrozen(boolean state) {

	frozen = state;
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
    setLabelFlags(int flags) {

	if (labelflags != flags) {
	    labelflags = flags;
	    tossLabels();
	    reset();
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


    synchronized void
    setOtherColor(Color color) {

	othercolor = (color != null) ? color : OTHERCOLOR;
    }


    synchronized void
    setPressedColor(Color color) {

	pressedcolor = (color != null) ? color : PRESSEDCOLOR;
    }


    synchronized void
    setPressingColor(Color color) {

	pressingcolor = (color != null) ? color : PRESSINGCOLOR;
    }


    final synchronized void
    setSweepColor(Color color) {

	sweepcolor = (color != null) ? color : SWEEPCOLOR;
    }


    synchronized void
    setViewportSize() {

	viewport.width = totalsize.width - insets.left - insets.right;
	viewport.height = totalsize.height - insets.top - insets.bottom;
    }


    final synchronized void
    setZoomInColor(Color color) {

	zoomincolor = (color != null) ? color : ZOOMINCOLOR;
    }


    final synchronized void
    setZoomOutColor(Color color) {

	zoomoutcolor = (color != null) ? color : ZOOMOUTCOLOR;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private Color
    pickAdjustedColor(Color color, double adjust) {

	return(Misc.pickAdjustedColor(
	    color,
	    adjust,
	    new Color[] {
		getEmptyColor(),
		getOtherColor(),
		getBackground(),
		getForeground(),
	    }
	));
    }
}

