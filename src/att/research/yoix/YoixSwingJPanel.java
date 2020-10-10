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

class YoixSwingJPanel extends JPanel

    implements YoixConstants,
	       YoixConstantsImage,
	       YoixInterfaceDrawable

{

    //
    // NOTE - the redefinition of getPreferredSize() (others too) seems
    // to cause an pretty insignifant performance glitch if a Yoix script
    // changes preferred size a few times. The second change seems like
    // it doesn't affect the size of the component (unless the frame is
    // resized), but after that size changes track preferredsize changes.
    // Was able to make the behavior go away when getLayoutSize() just
    // returned the original size. At this point we don't want to make
    // changes that undoubtedly need lots of testing, particularly when
    // it's not a serious issue - we will investigate more later.
    //

    protected YoixBodyComponent  parent;
    protected YoixObject         data;

    protected Image  backgroundimage = null;
    protected Image  filteredimage = null;
    protected int    backgroundhints = 0;

    //
    // Support for user defined paint functions.
    //

    private YoixObject  paint = null;
    private Graphics    paintgraphics = null;
    private Thread      paintthread = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixSwingJPanel(YoixObject data, YoixBodyComponent parent) {

	this.parent = parent;
	this.data = data;
    }

    ///////////////////////////////////
    //
    // YoixInterfaceDrawable Methods
    //
    ///////////////////////////////////

    public Graphics
    getPaintGraphics() {

	Component  components[];
	Graphics   g;
	Shape      clip;
	Area       area;
	Area       mask;

	if ((g = paintgraphics) == null || Thread.currentThread() != paintthread) {
	    if ((g = super.getGraphics()) != null) {
		if ((mask = YoixMiscJFC.getComponentMask(this)) != null) {
		    if ((clip = g.getClip()) != null)
			area = new Area(clip);
		    else area = new Area(new Rectangle(getSize()));
		    area.subtract(mask);
		    g.setClip(area);
		}
	    }
	} else g = g.create();
	return(g);
    }


    public boolean
    isDrawable() {

	return(true);
    }


    public boolean
    isPaintable() {

	return(true);
    }


    public boolean
    isTileable() {

	return(true);
    }


    public void
    paintBackground(Graphics g) {

	//
	// A recent change (6/17/05) now always clears the background if
	// backgroundimage exists. Older versions didn't bother clearing
	// it if there was a backgroundimage that also painted the entire
	// background. The check was only for efficiency, but transparent
	// images caused problems.
	//

	if (backgroundimage != null || isOpaque())
	    YoixMiscJFC.paintBackground(this, g);
    }


    public void
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


    public synchronized void
    setPaint(YoixObject obj) {

	if (isPaintable())
	    paint = obj.notNull() ? obj : null;
    }

    ///////////////////////////////////
    //
    // YoixSwingJPanel Methods
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


    protected Image
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


    protected Dimension
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


    public void
    paint(Graphics g) {

	paintBackground(g);
	paintBackgroundImage(g);
	paintCallback(g);
	paintChildren(g);
	paintBorder(g);
    }


    protected final void
    paintCallback(Graphics g) {

	YoixObject  funct;
	YoixObject  argv[];
	Rectangle   bounds;

	if ((funct = paint) != null && funct.notNull()) {
	    if (funct.callable(1)) {
		bounds = g.getClipBounds();
		if (bounds == null || getSize().equals(bounds.getSize()))
		    argv = new YoixObject[] {YoixObject.newNull()};
	        else argv = new YoixObject[] {YoixMake.yoixBBox(bounds, getCTMBody())};
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
}

