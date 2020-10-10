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
import att.research.yoix.*;

public abstract
class SwingDataPlot extends YoixSwingJTextComponent

    implements Constants,
	       ComponentListener,
	       DataPlot,
	       MouseListener,
	       MouseMotionListener,
	       YoixInterfaceShowing

{

    //
    // Recently added the painted boolean and used it to try to stop the
    // extra paint that happens the first time the plot is shown. Fix is
    // a kludge that seems to work, but be skeptical if you notice strange
    // behavior.
    //

    boolean  painted = false;
    boolean  partitioned = false;
    boolean  frozen = false;
    boolean  thawed = true;
    Color    connectcolor = CONNECTCOLOR;
    Color    offpeakcolor = OFFPEAKCOLOR;
    Color    sweepcolor = SWEEPCOLOR;
    int      xmask;
    int      ymask;
    int      xindex = -1;
    int      yindex = -1;
    int      partitionindex = -1;
    int      selectedcount;
    int      totalcount;

    //
    // Used to keep track of the current operation, if there is one.
    //

    int  mouse = AVAILABLE;
    int  mousebutton = 0;

    //
    // Remembering callback functions helps reduce lookup and checking
    // overhead.
    //

    private YoixObject  afterappend = null;
    private YoixObject  afterload = null;
    private YoixObject  aftersweep = null;
    private YoixObject  afterupdate = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    SwingDataPlot(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
	addAllListeners();
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
    // DataPlot Methods
    //
    ///////////////////////////////////

    public final int
    getPartitionIndex() {

	return(partitionindex);
    }


    public final int
    getXIndex() {

	return(xindex);
    }


    public final int
    getXMask() {

	return(xmask);
    }


    public final int
    getYIndex() {

	return(yindex);
    }


    public final int
    getYMask() {

	return(ymask);
    }


    synchronized void
    setFrozen(boolean state) {

	frozen = state;
    }


    public void
    setGenerator(Object generator[]) {

    }


    public final synchronized void
    setPartitionIndex(int index) {

	partitionindex = Math.max(index, -1);
	partitioned = (partitionindex >= 0);
    }


    public void
    setUnixTime(YoixObject obj) {

    }


    public final synchronized void
    setXIndex(int index) {

	xindex = Math.max(index, -1);
    }


    public final synchronized void
    setYIndex(int index) {

	yindex = Math.max(index, -1);
    }

    ///////////////////////////////////
    //
    // SwingDataPlot Methods
    //
    ///////////////////////////////////

    final synchronized void
    addAllListeners() {

	addComponentListener(this);
	addMouseListener(this);
    }


    final synchronized void
    afterAppend(int offset, boolean allselected) {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = afterappend) != null) {
	    if (funct.callable(3)) {
		argv = new YoixObject[] {
		    YoixObject.newInt(totalcount),
		    YoixObject.newInt(offset),
		    YoixObject.newInt(allselected)
		};
	    } else if (funct.callable(2)) {
		argv = new YoixObject[] {
		    YoixObject.newInt(totalcount),
		    YoixObject.newInt(offset),
		};
	    } else argv = new YoixObject[] {YoixObject.newInt(totalcount)};
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }


    final synchronized void
    afterLoad(boolean reload) {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	if ((funct = afterload) != null) {
	    if (funct.callable(2)) {
		argv = new YoixObject[] {
		    YoixObject.newInt(totalcount),
		    YoixObject.newInt(reload)
		};
	    } else if (funct.callable(1))
		argv = new YoixObject[] {YoixObject.newInt(totalcount)};
	    else argv = new YoixObject[0];
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
    getAfterUpdate() {

	return(afterupdate);
    }


    final synchronized Color
    getConnectColor() {

	return(connectcolor != null ? connectcolor : CONNECTCOLOR);
    }


    final boolean
    getFrozen() {

	return(frozen);
    }


    final synchronized Color
    getOffPeakColor() {

	return(offpeakcolor != null ? offpeakcolor : OFFPEAKCOLOR);
    }


    final synchronized Color
    getSweepColor() {

	return(sweepcolor != null ? sweepcolor : SWEEPCOLOR);
    }


    public final void
    paint(Graphics g) {

	super.paintBackground(g);	// Swing version currently needs this
	paintBackgroundImage(g);
	paintBorder(insets, g);
	paintRect(g);
	painted = true;			// painted kludge
    }


    final synchronized void
    removeAllListeners() {

	removeComponentListener(this);
	removeMouseListener(this);
	removeMouseMotionListener(this);
    }


    final void
    reset() {

	if (frozen == false)
	    repaint();
    }


    synchronized void
    setAfterAppend(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(1) || obj.callable(2) || obj.callable(3))
		afterappend = obj;
	    else VM.abort(TYPECHECK, NL_AFTERAPPEND);
	} else afterload = null;
    }


    synchronized void
    setAfterLoad(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1) || obj.callable(2))
		afterload = obj;
	    else VM.abort(TYPECHECK, NL_AFTERLOAD);
	} else afterload = null;
    }


    final synchronized void
    setAfterSweep(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		aftersweep = obj;
	    else VM.abort(TYPECHECK, NL_AFTERSWEEP);
	} else aftersweep = null;
    }


    synchronized void
    setAfterUpdate(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0) || obj.callable(1))
		afterupdate = obj;
	    else VM.abort(TYPECHECK, NL_AFTERUPDATE);
	} else afterupdate = null;
    }


    final synchronized void
    setConnectColor(Color color) {

	color = (color != null) ? color : CONNECTCOLOR;

	if (connectcolor == null || connectcolor.equals(color) == false) {
	    connectcolor = color;
	    reset();
	}
    }


    final synchronized void
    setOffPeakColor(Color color) {

	color = (color != null) ? color : OFFPEAKCOLOR;

	if (offpeakcolor == null || offpeakcolor.equals(color) == false) {
	    offpeakcolor = color;
	    reset();
	}
    }


    final synchronized void
    setSweepColor(Color color) {

	sweepcolor = (color != null) ? color : SWEEPCOLOR;
    }
}

