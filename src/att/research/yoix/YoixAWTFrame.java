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
import java.util.*;

class YoixAWTFrame extends Frame

    implements ComponentListener,
	       YoixConstants,
	       YoixConstantsImage,
	       YoixInterfaceDrawable,
	       YoixInterfaceMenuBar,
	       YoixInterfaceWindow

{

    private YoixBodyComponent  parent;
    private YoixObject         data;

    private Image  backgroundimage = null;
    private Image  filteredimage = null;
    private int    backgroundhints = 0;

    //
    // A Yoix paint function.
    //

    private YoixObject  paint = null;
    private boolean     listening = false;

    //
    // These sometimes helped deal with obscure MenuBar related bugs
    // that only showed up in JDK115 and JDK116 IRIX implementations.
    // Currently disabled, because usekludge is false and it can't be
    // changed.
    //

    private boolean  applied = false;
    private MenuBar  menubar = null;

    private static boolean  usekludge = false;

    //
    // An offset, calculated elsewhere, that accounts for decorations. 
    //

    private static Point  windowoffset = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTFrame(YoixObject data, YoixBodyComponent parent, GraphicsConfiguration gc) {

	super(gc);
	this.parent = parent;
	this.data = data;
    }

    ///////////////////////////////////
    //
    // ComponentListener Methods
    //
    ///////////////////////////////////

    public final void
    componentHidden(ComponentEvent e) {

    }


    public final void
    componentMoved(ComponentEvent e) {

    }


    public final void
    componentResized(ComponentEvent e) {

	//
	// This seems to be needed now because paint() may only be called
	// when at least one dimension increases and even when it's called
	// it may only be to paint the "added" rectangles. Problems happen
	// when there's a Yoix paint function that decides what to do based
	// on the size of this window, so we repaint() everything whenever
	// the size changes and there's a Yoix paint function that we need
	// to call. Older versions of Java behaved differently didn't need
	// this kludge.
	//

	if (paint != null)
	    repaint();
    }


    public final void
    componentShown(ComponentEvent e) {

    }

    ///////////////////////////////////
    //
    // YoixInterfaceDrawable Methods
    //
    ///////////////////////////////////

    public final Graphics
    getPaintGraphics() {

	return(super.getGraphics());
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

	YoixMiscJFC.paintBackground(this, g);
    }


    public final void
    paintBackgroundImage(Graphics g) {

	Image  image;

	if ((image = getFilteredImage(backgroundhints)) != null) {
	    if (windowoffset == null)
		windowoffset = YoixMiscJFC.getWindowOffset(this);
	    YoixMiscJFC.paintImage(this, image, windowoffset, backgroundhints, g);
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

	if (isPaintable()) {
	    if (obj.notNull()) {
		paint = obj;
		addListeners();
	    } else paint = null;
	}
    }

    ///////////////////////////////////
    //
    // YoixInterfaceMenuBar Methods
    //
    ///////////////////////////////////

    public final synchronized boolean
    changeMenuBar(Object newbar, boolean visible, boolean validate) {

	MenuBar  oldbar;
	boolean  result = false;

	//
	// An AWT MenuBar isn't a Component and really can't tell if it's
	// visible or not. We use the visible argument to help get around
	// that limitation, which means we handed the responsibility back
	// to the caller. A Swing JMenuBar is a JComponent, so we could
	// eliminate the argument if we didn't care about AWT.
	//

	if (newbar != null) {
	    if (newbar instanceof MenuBar) {
		oldbar = getMenuBar();
		if (oldbar != newbar) {
		    if (oldbar != null) {
			YoixMiscMenu.removeListener(oldbar, parent);
			remove(oldbar);
		    }
		    if (visible) {
			YoixMiscMenu.addListener((MenuBar)newbar, parent);
			setMenuBar((MenuBar)newbar);
		    }
		    result = true;
		} else if (visible == false)
		    result = changeMenuBar(null, visible, validate);
	    }
	} else {
	    if ((oldbar = getMenuBar()) != null) {
		YoixMiscMenu.removeListener(oldbar, parent);
		remove(oldbar);
		result = true;
	    }
	}

	if (result && validate)
	    validate();
	return(result);
    }
 
    ///////////////////////////////////
    //
    // YoixInterfaceWindow Methods
    //
    ///////////////////////////////////

    public final void
    setGlassPane(Component pane) {

    }

    ///////////////////////////////////
    //
    // YoixAWTFrame Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	data = null;
	parent = null;
	paint = null;
	backgroundimage = null;
	filteredimage = null;
	removeComponentListener(this);		// should be unnecessary
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


    public final void
    paint(Graphics g) {

	paintBackgroundImage(g);
	paintCallback(g);
	super.paint(g);			// required for Containers
    }


    public final void
    setBounds(int x, int y, int width, int height) {

	if (usekludge) {
	    synchronized(this) {
		if (applied == false) {
		    if (menubar != null) {
			super.setMenuBar(menubar);
			menubar = null;
		    }
		    applied = true;
		}
	    }
	}
	super.setBounds(x, y, width, height);
    }


    public final void
    setFont(Font font) {

	MenuBar  menubar;

	super.setFont(font);
	syncMenus(N_FONT, font);
    }


    public final void
    setMenuBar(MenuBar mb) {

	if (usekludge) {
	    synchronized(this) {
		if (applied == false)
		    menubar = mb;
		else super.setMenuBar(mb);
	    }
	} else super.setMenuBar(mb);
    }


    public final void
    setVisible(boolean state) {

	//
	// This seems to solve obscure cursor related problems that we
	// observed in Frames using JDK 1.1.6 on an SGI. Didn't track
	// it down or try to reproduce the problem anywhere else.
	//

	super.setVisible(state);
	super.setCursor(getCursor());		// the cursor kludge
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    addListeners() {

	if (listening == false) {
	    listening = true;
	    addComponentListener(this);
	}
    }


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


    private void
    paintCallback(Graphics g) {

	YoixObject  funct;
	YoixObject  clip;
	Rectangle   bounds;

	if ((funct = paint) != null && funct.notNull()) {
	    if (funct.callable(1)) {
		bounds = g.getClipBounds();
		if (bounds == null || getSize().equals(bounds.getSize()))
		    clip = YoixObject.newNull();
		else clip = YoixMake.yoixBBox(bounds, getCTMBody());
		parent.call(funct, new YoixObject[] {clip});
	    } else parent.call(funct, new YoixObject[0]);
	}
    }


    private void
    syncMenus(String name, Object value) {

	if (parent != null)
	    parent.syncMenus(name, value);
    }
}

