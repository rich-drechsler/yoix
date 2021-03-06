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
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

class YoixSwingJWindow extends JWindow

    implements YoixConstants,
	       YoixInterfaceDrawable,
	       YoixInterfaceWindow

{

    private YoixBodyComponent  parent;
    private YoixObject         data;
    private boolean            disposed = false;	// prevents dispose loops

    //
    // We're essentially assuming none of these change once created by
    // the constructor.
    //

    private YoixSwingJLayeredPane  layeredpane;
    private JRootPane              rootpane;
    private Container              contentpane;
    private Component              firstglasspane;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJWindow(YoixObject data, YoixBodyComponent parent, GraphicsConfiguration gc) {

	this(data, parent, new YoixAWTOwnerFrame(data), gc);
    }


    YoixSwingJWindow(YoixObject data, YoixBodyComponent parent, Frame frame, GraphicsConfiguration gc) {

	super(frame, gc);
	buildWindow(data, parent);
    }


    YoixSwingJWindow(YoixObject data, YoixBodyComponent parent, Window window, GraphicsConfiguration gc) {

	super(window, gc);
	buildWindow(data, parent);
    }

    ///////////////////////////////////
    //
    // YoixInterfaceDrawable Methods
    //
    ///////////////////////////////////

    public final Graphics
    getPaintGraphics() {

	Rectangle  rect;
	Graphics   g;
	Point      offset;
	Area       area;
	Area       mask;

	if ((g = layeredpane.getPaintGraphics()) == null) {
	    if ((g = super.getGraphics()) != null) {
		if (contentpane != null) {
		    offset = getOffset();
		    if ((mask = YoixMiscJFC.getComponentMask(contentpane, offset)) != null) {
			rect = new Rectangle(offset, contentpane.getSize());
			area = new Area(rect);
			area.subtract(mask);
			g.setClip(area);
		    }
		}
	    }
	}

	return(g);
    }


    public final boolean
    isDrawable() {

	return(true);
    }


    public final boolean
    isPaintable() {

	return(true);
    }


    public final boolean
    isTileable() {

	return(true);
    }


    public final void
    paintBackground(Graphics g) {

	layeredpane.paintBackground(getOffset(), g);
    }


    public final void
    paintBackgroundImage(Graphics g) {

	layeredpane.paintBackgroundImage(getOffset(), g);
    }


    public final synchronized void
    setBackgroundHints(int hints) {

	layeredpane.setBackgroundHints(hints);
    }


    public final synchronized void
    setBackgroundImage(Image image) {

	layeredpane.setBackgroundImage(image);
    }


    public final synchronized void
    setPaint(YoixObject obj) {

	layeredpane.setPaint(obj);
    }

    ///////////////////////////////////
    //
    // YoixSwingJWindow Methods
    //
    ///////////////////////////////////

    public final void
    dispose() {

	Object  owner;

	//
	// Harder than you might expect - omitting the disposed check
	// results in a stack overflow because of how Java's Window
	// class now implements dispose(). If you change this method
	// make sure there's no memory leak when Java runs.
	//

	if (disposed == false) {
	    disposed = true;
	    owner = getOwner();
	    super.dispose();
	    if (layeredpane != null)
		layeredpane.dispose();
	    if (owner instanceof YoixAWTOwnerFrame)
		((YoixAWTOwnerFrame)owner).dispose();
	}
    }


    protected void
    finalize() {

	data = null;
	parent = null;
	rootpane = null;
	contentpane = null;
	firstglasspane = null;
	layeredpane = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public final Dimension
    getMaximumSize() {

	return(getLayoutSize(N_MAXIMUMSIZE, super.getMaximumSize()));
    }


    public final Dimension
    getMinimumSize() {

	return(getLayoutSize(N_MINIMUMSIZE, super.getMinimumSize()));
    }


    public final Dimension
    getPreferredSize() {

	return(getLayoutSize(N_PREFERREDSIZE, super.getPreferredSize()));
    }


    public final synchronized boolean
    isDoubleBuffered() {

	return(rootpane.isDoubleBuffered());
    }


    public final void
    setBackground(Color color) {

	//
	// Setting rootpane background probably makes it unecessary to do
	// anything to contentpane, but test thoroughly before removing
	// the contentpane code.
	//

	super.setBackground(color);
	if (contentpane != null)
	    contentpane.setBackground(color);
	if (rootpane != null)
	    rootpane.setBackground(color);
    }


    final void
    setBorder(Border border) {

	if (contentpane instanceof JComponent)
	    ((JComponent)contentpane).setBorder(border);
    }


    final synchronized void
    setDoubleBuffered(boolean state) {

	//
	// We had problems with transparent YoixSwingJFileChoosers
	// when not double buffering, and we didn't spend lots of
	// time investigating or looking at many other components,
	// but you should be aware that disabling double buffering
	// and opaque could cause problems.
	//

	rootpane.setDoubleBuffered(state);
	layeredpane.setDoubleBuffered(state);
	if (contentpane instanceof JComponent)
	    ((JComponent)contentpane).setDoubleBuffered(state);
    }


    public final void
    setGlassPane(Component glass) {

	if (glass == null)
	    glass = firstglasspane;
	if (glass != getGlassPane())
	    super.setGlassPane(glass);
    }


    final synchronized void
    setToolTipText(String text) {

	layeredpane.setToolTipText(text);
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    buildWindow(YoixObject data, YoixBodyComponent parent) {

	//
	// We currently must restore or reset contentpane after changing
	// layeredpane, but it's really seems like a job setLayeredPane()
	// should be handling for everyone!!
	//

	this.data = data;
	this.parent = parent;
	rootpane = getRootPane();
	firstglasspane = getGlassPane();
	contentpane = getContentPane();
	layeredpane = new YoixSwingJLayeredPane(data, parent, this);
	setLayeredPane(layeredpane);
	setContentPane(contentpane);
	if (layeredpane instanceof YoixSwingJLayeredPane)
	    ((YoixSwingJLayeredPane)layeredpane).setOpaque(true);
	if (contentpane instanceof JComponent)
	    ((JComponent)contentpane).setOpaque(false);
    }


    private Dimension
    getLayoutSize(String name, Dimension size) {

	YoixObject  obj;
	Dimension   defaultsize;

	if ((obj = data.getObject(name)) != null && obj.notNull()) {
	    defaultsize = size;
	    size = YoixMakeScreen.javaDimension(obj);
	    if (size.width <= 0 || size.height <= 0) {
		if (size.width < 0 || (size.width == 0 && name.equals(N_PREFERREDSIZE)))
		    size.width = (defaultsize != null) ? defaultsize.width : 0;
		if (size.height < 0 || (size.height == 0 && name.equals(N_PREFERREDSIZE)))
		    size.height = (defaultsize != null) ? defaultsize.height : 0;
	    }
	}

	return(size);
    }


    private Point
    getOffset() {

	Point  offset;
	Point  corner;

	offset = rootpane.getLocation();
	if (contentpane != null) {
	    corner = contentpane.getLocation();
	    offset.translate(corner.x, corner.y); 
	}
	return(offset);
    }
}

