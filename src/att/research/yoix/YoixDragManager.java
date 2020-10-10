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
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

class YoixDragManager

    implements DragSourceMotionListener,
	       YoixConstants

{

    //
    // This class started as a place to hide the DragSourceMotionListener,
    // which first appeared in Java 1.4, but we ended up handling lots of
    // special code that's used to customize Java's drag and drop behavior.
    // 

    private YoixBodyComponent  owner = null;
    private DragGestureEvent   trigger = null;
    private DragSource         source = null;
    private YoixObject         event = null;

    //
    // Low level info about the drag and drop operation, including a window
    // that can be dragged around with the cursor. Seems to work reasonably
    // well, as long as the drag window isn't positioned over the cursor's
    // hotspot, which means you should avoid using YOIX_CENTER as an anchor
    // if you're interested in dropping and you're using a drag window.
    //

    private BufferedImage  dragimage = null;
    private YoixObject     yoixwindow = null;
    private InputEvent     draginputevent = null;
    private Rectangle      dragbounds = null;
    private boolean        dragstarted = false;
    private Window         dragwindow = null;
    private Point          dragpoint = null;
    private Point          dragoffset = null;
    private int            draganchor = YOIX_NONE;
    private int            dragmodel = 0;
    private int            dragpadding = 0;
    private int            dragradius2 = 2;
    private int            dragcounter = 0;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixDragManager(YoixBodyComponent owner, YoixObject event, DragGestureEvent trigger, DragSource source) {

	this.owner = owner;
	this.event = event;
	this.source = source;
	this.trigger = trigger;
	buildDragManager();
	addAllListeners();
	startDrag();
    }

    ///////////////////////////////////
    //
    // DragSourceMotionListener Methods
    //
    ///////////////////////////////////

    public final void
    dragMouseMoved(DragSourceDragEvent e) {

	if (shouldDragWindowMove(e))
	    moveDragWindow(e);
	owner.dragMouseMoved(e);
    }

    ///////////////////////////////////
    //
    // YoixDragManager Methods
    //
    ///////////////////////////////////

    public final boolean
    getDragStarted() {

	return(dragstarted);
    }


    protected void
    finalize() {

	stopDrag();
	owner = null;
	event = null;
	trigger = null;
	source = null;
	yoixwindow = null;
	dragwindow = null;
    }


    public final void
    stopDrag() {

	removeAllListeners();
	closeDragWindow();
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addAllListeners() {

	source.addDragSourceMotionListener(this);
    }


    private void
    buildDragManager() {

	BufferedImage  image;
	YoixObject     visual;
	YoixObject     layout;
	YoixObject     label;
	YoixObject     maximumsize;
	Dimension      currentsize;
	Dimension      size;
	boolean        usewindow;
	String         text;
	Object         comp;
	Font           font;

	if ((visual = event.getObject(N_VISUAL)) != null) {
	    if (visual.notNull()) {
		maximumsize = event.getObject(N_MAXIMUMSIZE);
		if (visual.isImage()) {
		    usewindow = (event.getBoolean(N_DRAGIMAGESUPPORTED) == false);
		    if (usewindow || source.isDragImageSupported() == false) {
			yoixwindow = newDragWindow(visual, null, visual.getObject(N_SIZE));
			dragwindow = (Window)yoixwindow.getManagedObject();
		    } else {
			yoixwindow = null;
			dragwindow = null;
			dragimage = ((YoixBodyImage)visual.body()).copyCurrentImage();
		    }
		    maximumsize = null;
		} else if (visual.isWindow()) {
		    yoixwindow = visual;
		    dragwindow = (Window)yoixwindow.getManagedObject();
		    maximumsize = null;
		} else if (visual.isString()) {
		    //
		    // Made small style changes here to match what's done
		    // by the YoixTipManager.
		    //
		    label = YoixMake.yoixType(T_JLABEL);
		    label.putString(N_TEXT, visual.stringValue());
		    label.putColor(N_BACKGROUND, UIManager.getColor("ToolTip.background"));
		    if ((font = UIManager.getFont("ToolTip.font")) != null)
			label.putObject(N_FONT, YoixMake.yoixFont(font));
		    if ((comp = label.getManagedObject()) != null) {
			//
			// Make sure we get a 1 pixel border - again done
			// to duplicate YoixTipManager.
			//
			if (comp instanceof JComponent)
			    ((JComponent)comp).setBorder(new EmptyBorder(1, 1, 1, 1));
		    }
		    layout = YoixObject.newArray(1);
		    layout.putObject(0, label);
		    yoixwindow = newDragWindow(null, layout, null);
		    dragwindow = (Window)yoixwindow.getManagedObject();
		} else if (visual.isColor()) {
		    yoixwindow = newDragWindow(visual, null, YoixObject.newDimension(72.0/2, 72.0/4));
		    dragwindow = (Window)yoixwindow.getManagedObject();
		}
		if (dragwindow != null) {
		    if (maximumsize != null && maximumsize.notNull()) {
			if ((size = YoixMakeScreen.javaDimension(maximumsize)) != null) {
			    currentsize = dragwindow.getSize();
			    if (currentsize.width < size.width || size.width <= 0)
				size.width = currentsize.width;
			    if (currentsize.height < size.height || size.height <= 0)
				size.height = currentsize.height;
			    if (size.equals(currentsize) == false)
				dragwindow.setSize(size);
			}
		    }
		}
		//
		// Must set draganchor and dragpadding before getDragOffset()
		// is called.
		//
		draginputevent = trigger.getTriggerEvent();
		draganchor = event.getInt(N_ANCHOR, YOIX_SOUTH);
		dragpadding = YoixMakeScreen.javaDistance(event.getDouble(N_PADDING, 0));
		dragoffset = getDragOffset();
		dragbounds = getDragBounds();
	    }
	}
    }


    private void
    closeDragWindow() {

	//
	// We now assume that the autodispose field is set to true whenever
	// we create a drag window, so "officially" hiding the window should
	// trigger a dispose. Done because there's a small chance we may let
	// users access the Yoix version of the drag window that we create,
	// and using autodispose to trigger the dispose will let users have
	// the final say.
	//

	if (dragwindow != null) {
	    if (yoixwindow != null && yoixwindow.notNull())
		yoixwindow.putInt(N_VISIBLE, false);
	    else dragwindow.setVisible(false);
	}
    }


    private Rectangle
    getDragBounds() {

	Rectangle  bounds = null;
	Dimension  size;
	Object     comp;
	Point      origin;
	Point      point;

	//
	// Creates a "padded" rectangle that describes the screen area that
	// the dragwindow initially claims, even if it's not made visible
	// right away.
	//

	if (dragwindow != null && trigger != null) {
	    if (draginputevent instanceof MouseEvent) {
		comp = draginputevent.getSource();
		if (comp instanceof Component) {
		    origin = ((Component)comp).getLocationOnScreen();
		    point = ((MouseEvent)draginputevent).getPoint();
		    size = dragwindow.getSize();
		    bounds = new Rectangle(
			origin.x + point.x + dragoffset.x - dragpadding,
			origin.y + point.y + dragoffset.y - dragpadding,
			size.width + 2*dragpadding,
			size.height + 2*dragpadding
		    );
		}
	    }
	}
	return(bounds);
    }


    private Point
    getDragOffset() {

	Point  offset;

	if (dragwindow != null)
	    offset = getDragOffset(dragwindow.getSize());
	else if (dragimage != null)
	    offset = getDragOffset(new Dimension(dragimage.getWidth(), dragimage.getHeight()));
	else offset = null;
	return(offset);
    }


    private Point
    getDragOffset(Dimension size) {

	YoixObject  obj;
	boolean     aligned;
	Point       offset;
	int         dx;
	int         dy;

	//
	// Even though we currently support it, centering the visual doesn't
	// make much sense if we're dragging a window around because nobody
	// else will get a get a chance to accept the drag!!
	// 

	if (size != null) {
	    if ((obj = event.getObject(N_OFFSET)) != null && obj.notNull()) {
		offset = YoixMakeScreen.javaPoint(obj);
		aligned = false;
	    } else {
		offset = new Point(0, 0);
		aligned = true;
	    }
	    switch (draganchor) {
		case YOIX_BOTTOM:
		case YOIX_SOUTH:
		    dx = aligned ? -size.width/2 : 0;
		    dy = dragpadding;
		    break;

		case YOIX_TOP:
		case YOIX_NORTH:
		    dx = aligned ? -size.width/2 : 0;
		    dy = -size.height - dragpadding;
		    break;

		case YOIX_RIGHT:
		case YOIX_EAST:
		    dx = dragpadding;
		    dy = aligned ? -size.height/2 : 0;
		    break;

		case YOIX_LEFT:
		case YOIX_WEST:
		    dx = -size.width - dragpadding;
		    dy = aligned ? -size.height/2 : 0;
		    break;

		case YOIX_BOTTOMLEFT:
		case YOIX_SOUTHWEST:
		    dx = -size.width - dragpadding;
		    dy = dragpadding;
		    break;

		case YOIX_BOTTOMRIGHT:
		case YOIX_SOUTHEAST:
		    dx = dragpadding;
		    dy = dragpadding;
		    break;

		case YOIX_TOPLEFT:
		case YOIX_NORTHWEST:
		    dx = -size.width - dragpadding;
		    dy = -size.height - dragpadding;
		    break;

		case YOIX_TOPRIGHT:
		case YOIX_NORTHEAST:
		    dx = dragpadding;
		    dy = -size.height - dragpadding;
		    break;

		case YOIX_CENTER:	// often doesn't make much sense
		    dx = -size.width/2;
		    dy = -size.height/2;
		    break;

		case YOIX_NONE:
		default:
		    dx = 0;
		    dy = 0;
		    if (dragwindow != null && aligned == false) {
			dragmodel = 1;
			dragpadding = (int)Math.max(dragpadding, 2*(Math.sqrt(dragradius2) + 1));
		    } else dragmodel = 0;
		    break;
	    }
	    offset = new Point(offset.x + dx, offset.y + dy);
	} else offset = null;

	return(offset);
    }


    private void
    moveDragWindow(DragSourceDragEvent e) {

	Point  point;

	if (dragwindow != null && e != null) {
	    point = e.getLocation();
	    dragwindow.setLocation(point.x + dragoffset.x, point.y + dragoffset.y);
	    if (dragwindow.isVisible() == false)
		dragwindow.setVisible(true);
	    dragwindow.toFront();		// unnecessary??
	    if ((++dragcounter%5) == 0)
		Thread.yield();
	    dragpoint = point;
	}
    }


    private YoixObject
    newDragWindow(YoixObject background, YoixObject layout, YoixObject size) {

	YoixObject  ival;
	YoixObject  parent;

	ival = YoixObject.newDictionary(4, -1);

	ival.putInt(N_AUTODISPOSE, true);
	if (background != null) {
	    if (background.isImage())
		ival.putObject(N_BACKGROUNDIMAGE, background);
	    else if (background.isColor())
		ival.putObject(N_BACKGROUND, background);
	}
	ival.putObject(N_LAYOUT, layout);
	ival.putObject(N_SIZE, size);
	ival.putObject(N_PARENT, owner.getContext().getObject(N_ROOT));

	return(YoixMake.yoixType(T_JWINDOW, ival));
    }


    private void
    removeAllListeners() {

	source.removeDragSourceMotionListener(this);
    }


    private boolean
    shouldDragWindowMove(DragSourceDragEvent e) {

	boolean  result;
	Point2D  intersection;
	Object   source;
	Point    point;
	Point    origin;
	Point    corner;

	if (dragwindow != null && e != null) {
	    point = e.getLocation();
	    if (dragmodel == 1 && dragbounds != null) {
		if (result = !dragbounds.contains(point)) {
		    if (draginputevent instanceof MouseEvent) {
			origin = new Point(
			    dragbounds.x + dragpadding,
			    dragbounds.y + dragpadding
			);
			if (dragpoint == null) {
			    dragpoint = ((MouseEvent)draginputevent).getPoint();
			    source = draginputevent.getSource();
			    if (source instanceof Component) {	// unnecessary test?
				corner = ((Component)source).getLocationOnScreen();
				dragpoint.translate(corner.x, corner.y);
			    }
			}
			intersection = YoixMiscGeom.getFirstIntersection(
			    new Line2D.Double(point, dragpoint),
			    dragbounds,
			    point
			);
			if (intersection != null) {
			    dragoffset = new Point(
				origin.x - (int)intersection.getX(),
				origin.y - (int)intersection.getY()
			    );
			}
		    }
		    dragmodel = 0;	// so we don't do this again
		    dragbounds = null;
		} else dragpoint = point;
	    } else {
		if (dragpoint != null)
		    result = dragpoint.distanceSq(point) > dragradius2;
		else result = true;
	    }
	} else result = false;

	return(result);
    }


    private void
    startDrag() {

	YoixObject  transferable;
	Cursor      cursor;
	Image       image;
	Point       offset;

	if ((transferable = event.getObject(N_TRANSFERABLE)) != null) {
	    if (transferable.notNull()) {
		cursor = owner.setDragCursor(event.getObject(N_CURSOR));
		if ((image = dragimage) != null) {
		    if ((offset = dragoffset) == null)	// won't happen
			offset = new Point(0, 0);
		} else {
		    //
		    // The Java implementation on the only platform that
		    // we've seen support a dragimage seems to draw an
		    // empty outline of the entire component when there's
		    // no image. A tiny transparent image changes Java's
		    // behavior.
		    //
		    image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		    offset = new Point(0, 0);
		}
		try {
		    trigger.startDrag(cursor, image, offset, transferable, owner);
		    dragstarted = true;
		}
		catch(InvalidDnDOperationException e) {}
		finally {
		    if (dragstarted == false) {
			//
			// handle any cleanup...
			//
			stopDrag();
		    }
		}
	    }
	}
    }
}

