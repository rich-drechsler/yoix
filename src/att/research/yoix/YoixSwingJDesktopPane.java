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

class YoixSwingJDesktopPane extends JDesktopPane

    implements YoixConstants,
	       YoixConstantsImage,
	       YoixInterfaceDrawable

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private Image  backgroundimage = null;
    private Image  filteredimage = null;
    private int    backgroundhints = 0;

    //
    // Support for user defined paint functions.
    //

    private YoixObject  paint = null;
    private Graphics    paintgraphics = null;
    private Thread      paintthread = null;

    //
    // Looks like javax.swing.JDesktopPane.isOpaque() always returns true
    // (at least through Java version 1.4.2), so we maintain the current
    // opaque setting.
    // 

    private boolean  opaque = true;
    private boolean  protectbackground = false;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJDesktopPane(YoixObject data, YoixBodyComponent parent) {

	this.data = data;
	this.parent = parent;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceDrawable Methods
    //
    ///////////////////////////////////

    public final Graphics
    getPaintGraphics() {

	Graphics  g;
	Point     origin;

	if ((g = paintgraphics) != null) {
	    if (Thread.currentThread() == paintthread)
		g = g.create();
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

	//
	// A recent change (6/17/05) now always clears the background if
	// backgroundimage exists. Older versions didn't bother clearing
	// it if there was a backgroundimage that also painted the entire
	// background. The check was only for efficiency, but transparent
	// images caused problems.
	//

	if (backgroundimage != null || opaque)
	    YoixMiscJFC.paintBackground(this, g);
    }


    public final void
    paintBackgroundImage(Graphics g) {

	Image  image;

	if (backgroundimage != null) {
	    if ((image = getFilteredImage(backgroundhints)) != null)
		YoixMiscJFC.paintImage(this, image, backgroundhints, g);
	}
    }


    public final synchronized void
    setBackgroundHints(int hints) {

	if (this.backgroundhints != hints) {
	    this.backgroundhints = hints;
	    this.filteredimage = null;
	    repaint();
	}
    }


    public final synchronized void
    setBackgroundImage(Image image) {

	if (this.backgroundimage != image) {
	    this.backgroundimage = image;
	    this.filteredimage = null;
	    repaint();
	}
    }


    public final synchronized void
    setPaint(YoixObject obj) {

	paint = obj.notNull() ? obj : null;
    }

    ///////////////////////////////////
    //
    // YoixSwingJDesktopPane Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	Graphics  g;

	data = null;
	parent = null;
	backgroundimage = null;
	filteredimage = null;
	paint = null;
	paintthread = null;
	if ((g = paintgraphics) != null) {
	    g.dispose();
	    paintgraphics = null;
	}
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    public final boolean
    isOpaque() {

	//
	// Return value is faked when we want to prevent super.paint()
	// from repainting our background - needed when there's a Yoix
	// paint() function or a background image (see paint() below
	// and javax.swing.JLayeredPane.paint() for more details).
	//

	return(protectbackground ? false : opaque);
    }


    public final void
    paint(Graphics g) {

	String  msg;

	//
	// The way we use protectbackground is very tricky so be careful
	// if you make changes here. Setting it to false is how we have
	// super.paint() to leave our background alone (you may want to
	// look at JLayeredPane.paint() and our version of isOpaque() to
	// see how it works).
	//
	// NOTE - old versions set protectbackground to true before the
	// paintBackground() call, but we recently moved it. See comments
	// in YoixSwingJLayeredPane.java for more details.
	//

	if (backgroundimage != null || paint != null) {
	    paintBackground(g);
	    protectbackground = true;		// moved on 2/28/06
	    paintBackgroundImage(g);
	    paintCallback(g);
	} else protectbackground = false;

	try {
	    super.paint(g);
	}
	catch(RuntimeException e) {
	    //
	    // This seems to help on Mac OSX where we sometimes can get
	    // an exception that looks like
	    //
	    //    apple.awt.EventQueueExceptionHandler Caught Throwable : java.lang.ArrayIndexOutOfBoundsException: No such child: ...
	    //
	    // and appears to be an Apple Java bug. Not 100% sure who's
	    // to blame, but this elimintes unwanted noise.
	    // 

	    msg = e.getMessage();
	    if ((msg = e.getMessage()) == null || !msg.startsWith("No such child:"))
		throw(e);
	}
	finally {
	    protectbackground = false;
	}
    }


    public final void
    setOpaque(boolean state) {

	opaque = state;
	super.setOpaque(opaque);		// unnecessary
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private YoixBodyMatrix
    getCTMBody() {

	YoixObject  graphics;
	YoixObject  mtx;

	if ((graphics = data.getObject(N_GRAPHICS)) != null) {
	    if ((mtx = graphics.getObject(N_CTM)) == null)
		mtx = VM.getDefaultMatrix();
	} else mtx = VM.getDefaultMatrix();
	return((YoixBodyMatrix)mtx.body());
    }


    private Image
    getFilteredImage(int hints) {

	Dimension  size;
	Image      image;
	int        height;
	int        width;

	//
	// Old versions always synchronized, but as a precaution we decided
	// the most common case (no background image) shouldn't bother. We
	// wanted to avoid any chance of deadlock, despite the fact that we
	// had no evidence the old implementation caused problems.
	//

	if (backgroundimage != null) {
	    synchronized(this) {
		image = (filteredimage != null) ? filteredimage : backgroundimage;
		if (image != null) {
		    size = getSize();
		    height = image.getHeight(null);
		    width = image.getWidth(null);
		    if ((hints & YOIX_SCALE_TILE) == 0) {
			if ((hints & YOIX_SCALE_NONE) == 0) {
			    if (size.height != height || size.width != width) {
				image = IMAGEOBSERVER.scaleImage(
				    image,
				    size.width,
				    size.height,
				    hints,
				    null
				);
			    }
			}
		    }
		    filteredimage = image;
		}
	    }
	} else image = null;

	return(image);
    }


    private void
    paintCallback(Graphics g) {

	YoixObject  funct;
	YoixObject  argv[];
	Rectangle   bounds;
	Point       origin;

	if ((funct = paint) != null && funct.notNull()) {
	    if (funct.callable(1)) {
		bounds = g.getClipBounds();
		if (bounds != null && !getSize().equals(bounds.getSize()))
		    argv = new YoixObject[] {YoixMake.yoixBBox(bounds, getCTMBody())};
		else argv = new YoixObject[] {YoixObject.newNull()};
	    } else argv = new YoixObject[0];
	    try {
		paintgraphics = g;
		paintthread = Thread.currentThread();
		parent.call(funct, argv);
	    }
	    finally {
		paintthread = null;
		paintgraphics = null;
	    }
	}
    }
}

