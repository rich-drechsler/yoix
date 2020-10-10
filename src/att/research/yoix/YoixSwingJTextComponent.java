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
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public abstract
class YoixSwingJTextComponent extends YoixSwingJCanvas

    implements YoixAPI

{

    protected YoixAWTFontMetrics  fm;
    protected boolean             initialized = false;		// for Swing version

    protected Font  currentfont;
    protected int   alignment = YOIX_LEFT;
    protected int   anchor = YOIX_SOUTH;
    protected int   baseline;
    protected int   leading;
    protected int   spacewidth;
    protected int   columns = 0;
    protected int   rows = 0;

    protected Dimension  totalsize = new Dimension(0, 0);
    protected Dimension  cellsize = new Dimension(0, 0);
    protected Dimension  extent = new Dimension(0, 0);
    protected Rectangle  viewport = new Rectangle(0, 0, 0, 0);
    protected Insets     insets = new Insets(0, 0, 0, 0);
    protected Insets     ipad = new Insets(0, 0, 0, 0);

    //
    // Remembering callback functions helps reduce lookup and checking
    // overhead.
    //

    protected YoixObject  syncviewport = null;
    protected int         synccount = 0;

    //
    // Saving a Graphics object helps scrolling and highlighting, but
    // may tie up a limited system resource and also means we need to
    // carefully restore the origin whenever we translate().
    //

    private Graphics  savedgraphics = null;
    private boolean   savegraphics = false;

    //
    // This is here because we occasionally observed some very obscure
    // hangs when we used JRE 1.3.1 (actually 1.3.1-rc2-b23) on Windows
    // NT (probably happens on any Windows platform) and we were able
    // to get a thread dump by hitting ctrl-BREAK. We could tell that
    // the deadlock happened because Java's event thread and one of our
    // threads were trying to lock the same YoixAWTTextComponent and grab
    // the AWT treelock associated with a YoixAWTTextComponent. Turns out
    // we had some luck reproducing the deadlock, but it was complicated
    // and involved horizontal scrolling, modal dialogs, and posting a
    // query to a server. Although we don't understand exactly how the
    // deadlock happened the dumps did make it clear that removing the
    // synchronization from getLayoutSize() and loadFont() would prevent
    // this particular deadlock. We may investigate some more - later.
    //

    protected final Object  FONTLOCK = new Object();

    //
    // An arbitrary sample string used to guess an average character
    // width. Unfortunately using fm.getMaxAdvance() wasn't the right
    // solution, at least for constant width fonts on all platforms.
    //

    private static String  samplestring = "MNmmnnoopp";

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    protected
    YoixSwingJTextComponent(YoixObject data, YoixBodyComponent parent) {

	super(data, parent);
	initialized = true;
    }

    ///////////////////////////////////
    //
    // YoixAPI Methods
    //
    ///////////////////////////////////

    protected final synchronized void
    disposeSavedGraphics() {

	//
	// Saving graphics objects the way AWT versions did causes
	// problems, so it's currently disabled.
	//
    }


    protected final synchronized void
    disposeSavedGraphics(Graphics g) {

	//
	// Saving graphics objects the way AWT versions did causes
	// problems, so it's currently disabled.
	//

	g.dispose();
    }


    protected final synchronized Graphics
    getSavedGraphics() {

	Graphics  g;

	//
	// Saving graphics objects the way AWT versions did causes
	// problems, so it's currently disabled.
	//

	if ((g = getGraphics()) != null) {
	    g.setClip(insets.left, insets.top, viewport.width, viewport.height);
	    ((Graphics2D)g).setBackground(getBackground());
	}
	return(g);
    }


    protected final synchronized int
    getSyncCount() {

	return(synccount);
    }


    protected final void
    horizontalScrollTo(int x) {

	setOrigin(new Point(x, viewport.y));
    }


    protected final boolean
    loadFont() {

	YoixAWTFontMetrics  metrics;
	Font                font;

	if ((font = getFont()) != null) {
	    if (font.equals(currentfont) == false) {
		if ((metrics = YoixAWTToolkit.getFontMetrics(font)) != null) {
		    synchronized(FONTLOCK) {
			fm = metrics;
			currentfont = font;
			spacewidth = fm.stringWidth(" ");
			baseline = fm.getMaxAscent() + ipad.top;
			leading = fm.getLeading();
			cellsize.width = fm.stringWidth(samplestring)/samplestring.length();
			cellsize.height = baseline + leading + fm.getMaxDescent() + ipad.bottom;
		    }
		} else font = null;
	    }
	}

	return(font != null);
    }


    public void
    paintBackground(Graphics g) {

	YoixMiscJFC.paintBackground(this, g);
    }


    protected void
    paintRect(int x, int y, int width, int height, Graphics g) {

    }


    protected synchronized void
    setAnchor(int anchor) {

	if (this.anchor != anchor) {
	    this.anchor = anchor;
	    reset(false);
	}
    }


    protected synchronized void
    setAlignment(int alignment) {

	if (this.alignment != alignment) {
	    this.alignment = alignment;
	    reset(false);
	}
    }


    protected synchronized void
    setOrigin(Point point) {

	Graphics  g;
	int       height;
	int       width;
	int       dx;
	int       dy;

	//
	// Decided, that it would be best if we explicitly handled the
	// low level scrolling details. All lines are redrawn if too
	// many changed (half the text but it should be tunable) using
	// a version of repaint(). Should be able to use update(g) to
	// avoid some overhead, (e.g., creating a new Graphics context)
	// provided you set an appropriate clipping rectangle.
	//
	// The if statement that checks extent.width and extent.height
	// originally also bracketed the statements that adjust the
	// viewport. That sometimes caused minor scrolling problems in
	// an important application so the viewport initialization was
	// moved outside the extent check on 1/21/05. NOTE - think the
	// 5/2/02 date mentioned below is completely bogus. We suspect
	// it was more like 2/5/04, but that's just a guess.
	//
	// The Thread.yield() call seems to help the painting of Swing
	// components if copyArea() generated a repaint(). It apparently
	// gives that PaintEvent a chance to get on the EventQueue where
	// our isPainting() test will find it. May not be a complete fix,
	// but more complicated approaches, probably using invokeLater(),
	// don't seem worth extra effort because we haven't noticed any
	// painting problems.
	//

	if (point != null && point.equals(viewport.getLocation()) == false) {
	    dx = point.x - viewport.x;
	    dy = point.y - viewport.y;
	    viewport.x = point.x;
	    viewport.y = point.y;
	    if (extent.width > 0 && extent.height > 0) {	// recent addition (5/2/02 - bogus date??)
		if (isPainting() == false) {
		    if ((g = getSavedGraphics()) != null) {
			width = viewport.width;
			height = viewport.height;
			if (Math.abs(dx) < width/2 && Math.abs(dy) < height/2) {
			    g.translate(insets.left, insets.top);
			    if (dx >= 0 && dy >= 0) {
				g.copyArea(dx, dy, width - dx, height - dy, -dx, -dy);
				repaintRect(width - dx, 0, dx, height - dy, g);
				repaintRect(0, height - dy, width, dy, g);
			    } else if (dx >= 0 && dy < 0) {
				g.copyArea(dx, 0, width - dx, height + dy, -dx, -dy);
				repaintRect(0, 0, width, -dy, g);
				repaintRect(width - dx, -dy, dx, height + dy, g);
			    } else if (dx < 0 && dy >= 0) {
				g.copyArea(0, dy, width + dx, height - dy, -dx, -dy);
				repaintRect(0, 0, -dx, height - dy, g);
				repaintRect(0, height - dy, width, dy, g);
			    } else if (dx < 0 && dy < 0) {	// obviously unnecessary test
				g.copyArea(0, 0, width + dx, height + dy, -dx, -dy);
				repaintRect(0, 0, width, -dy, g);
				repaintRect(0, -dy, -dx, height + dy, g);
			    }
			    g.translate(-insets.left, -insets.top);
			} else repaint(insets.left, insets.top, width, height);
			Thread.yield();
			disposeSavedGraphics(g);
		    }
		} else repaintComponent();
		syncViewport();		// recent addition (4/30/04)
	    }
	}
    }


    protected final synchronized void
    syncViewport() {

	YoixObject  funct;
	YoixObject  argv[];
	Runnable    event;

	//
	// Calls a user defined function, so to be safe we build an
	// InvocationEvent for the call and then post it on the AWT
	// event queue (to try to avoid deadlock).
	// 

	funct = syncviewport;		// snapshot - just to be safe

	if (funct != null) {
	    synccount++;
	    if (funct.callable(3)) {
		argv = new YoixObject[] {
		    YoixMakeScreen.yoixRectangle(viewport),
		    YoixMakeScreen.yoixDimension(extent),
		    YoixMakeScreen.yoixDimension(cellsize)
		};
	    } else argv = new YoixObject[] {YoixObject.newInt(synccount)};
	    event = new YoixAWTInvocationEvent(funct, argv, getContext());
	    EventQueue.invokeLater(event);
	}
    }


    protected final void
    verticalScrollTo(int y) {

	setOrigin(new Point(viewport.x, y));
    }

    ///////////////////////////////////
    //
    // YoixSwingJTextComponent Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	Graphics  g;

	if ((g = savedgraphics) != null)
	    g.dispose();
	super.finalize();
    }


    public final int
    getAlignment() {

	return(alignment);
    }


    public final int
    getAnchor() {

	return(anchor);
    }


    public final int
    getBaseline() {

	return(baseline);
    }


    public final int
    getCellHeight() {

	return(cellsize.height);
    }


    public final Dimension
    getCellSize() {

	return(cellsize);
    }


    public final int
    getCellWidth() {

	return(cellsize.width);
    }


    public final int
    getColumns() {

	return(columns);
    }


    public final Dimension
    getExtent() {

	return(extent);
    }


    public final YoixAWTFontMetrics
    getFontMetrics() {

	return(fm);
    }


    public final Insets
    getInsets() {

	return((Insets)(insets != null ? insets : ZEROINSETS).clone());
    }


    public final Insets
    getIpad() {

	return((Insets)(ipad != null ? ipad : ZEROINSETS).clone());
    }


    public final int
    getLeading() {

	return(leading);
    }


    public final Point
    getOrigin() {

	return(viewport.getLocation());
    }


    public final int
    getRows() {

	return(rows);
    }


    String
    getText() {

	return(null);
    }


    public final Rectangle
    getViewport() {

	return(viewport);
    }


    public final boolean
    isDrawable() {

	return(false);
    }


    public final boolean
    isPaintable() {

	return(false);
    }


    public final boolean
    isTileable() {

	return(false);
    }


    void
    reset() {

	repaint();
    }


    void
    reset(boolean sync) {

	//
	// A true argument means something changed that could affect
	// any other components that are grouped with this one. Means
	// we should call manager's reset() if there is one. For now
	// I'll leave the check out, but classes that extend this one,
	// like YoixAWTTableColumn, should override this method.
	//

	reset();
    }


    final synchronized void
    setColumns(int count) {

	columns = count;
    }


    public synchronized void
    setFont(Font font) {

	super.setFont(font);
	if (initialized) {		// Swing version needs this
	    loadFont();
	    reset(true);
	}
    }


    final synchronized void
    setInsets(Insets insets) {

	if (insets != null && insets.equals(this.insets) == false) {
	    this.insets = insets;
	    reset(false);
	}
    }


    final synchronized void
    setIpad(Insets ipad) {

	//
	// Negative values are currently ignored because we noticed a few
	// scrolling problems when there were no restrictions. May try to
	// track it down - much later. Also decided to ignore horizontal
	// padding adjustments until they're actually requested.
	//

	if (ipad != null && ipad.equals(this.ipad) == false) {
	    this.ipad.top = Math.max(ipad.top, 0);
	    this.ipad.bottom = Math.max(ipad.bottom, 0);
	    this.ipad.left = Math.max(ipad.left, 0);
	    this.ipad.right = Math.max(ipad.right, 0);
	    currentfont = null;
	    loadFont();		// recent addition
	    reset(true);
	}
    }


    final synchronized void
    setRows(int count) {

	rows = count;
    }


    final synchronized void
    setSaveGraphics(boolean state) {

	//
	// Saving graphics objects the way AWT versions did causes
	// problems, so it's currently disabled.
	//

	savegraphics = false;
    }


    final synchronized void
    setSyncViewport(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(1) || obj.callable(3))
		syncviewport = obj;
	    else VM.abort(TYPECHECK, N_SYNCVIEWPORT);
	} else syncviewport = null;
    }


    void
    setText(String text) {

    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private boolean
    isPainting() {

	EventQueue  queue = YoixAWTToolkit.getSystemEventQueue();

	return(queue.peekEvent(PaintEvent.UPDATE) != null || queue.peekEvent(PaintEvent.PAINT) != null);
    }


    private void
    repaintComponent() {

	super.repaint();
    }


    private void
    repaintRect(int x, int y, int width, int height, Graphics g) {

	Color  color;

	if (width > 0 && height > 0) {
	    color = g.getColor();
	    g.setColor(getBackground());
	    g.fillRect(x, y, width, height);
	    g.setColor(color);
	    paintRect(x + viewport.x, y + viewport.y, width, height, g);
	}
    }
}

