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

class YoixAWTPanel extends Panel

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
    // A Yoix paint function.
    //

    private YoixObject  paint = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    YoixAWTPanel(YoixObject data, YoixBodyComponent parent) {

	this.parent = parent;
	this.data = data;
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

	if ((image = getFilteredImage(backgroundhints)) != null)
	    YoixMiscJFC.paintImage(this, image, backgroundhints, g);
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

	if (isPaintable())
	    paint = obj.notNull() ? obj : null;
    }

    ///////////////////////////////////
    //
    // YoixAWTPanel Methods
    //
    ///////////////////////////////////

    protected void
    finalize() {

	data = null;
	parent = null;
	paint = null;
	backgroundimage = null;
	filteredimage = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final Dimension
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


    public final void
    paint(Graphics g) {

	paintBackgroundImage(g);
	paintCallback(g);
	super.paint(g);			// required for Containers
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
}

