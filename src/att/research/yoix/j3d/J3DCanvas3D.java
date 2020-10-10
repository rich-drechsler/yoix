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

package att.research.yoix.j3d;
import java.awt.*;
import java.awt.GraphicsConfiguration;
import java.awt.event.*;
import java.awt.image.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.*;
import att.research.yoix.*;

public
class J3DCanvas3D extends Canvas3D

    implements Constants,
	       MouseListener,
	       MouseMotionListener,
	       YoixInterfaceDrawable

{

    //
    // This started with YoixAWTCanva.java and at one point it included
    // the standard background image code, but there will be better ways
    // to handle backgrounds so the code has now been deleted. Yoix paint
    // functions are still supported and right now they're called after
    // the 3D painting.
    //

    BodyComponentAWT  parent;
    YoixObject        data;

    //
    // These proabably will help...
    //

    private BodyVirtualUniverse  virtualuniverse = null;

    //
    // Background image and hints - these no longer work and probably will
    // be removed.
    //

    private Image  backgroundimage = null;
    private int    backgroundhints;

    //
    // A Yoix paint function.
    //

    private YoixObject  paint = null;

    //
    // Java3D specific callbacks.
    //

    private YoixObject  postrender = null;
    private YoixObject  postswap = null;
    private YoixObject  prerender = null;
    private YoixObject  renderfield = null;

    ///////////////////////////////////
    //
    // Constructors
    //
    ///////////////////////////////////

    protected
    J3DCanvas3D(YoixObject data, BodyComponentAWT parent) {

	super(SimpleUniverse.getPreferredConfiguration(), data.getObject(NL_OFFSCREENBUFFER).notNull());
	this.data = data;
	this.parent = parent;
    }

    ///////////////////////////////////
    //
    // MouseListener Methods
    //
    ///////////////////////////////////

    public void
    mouseClicked(MouseEvent e) {

    }


    public synchronized void
    mouseEntered(MouseEvent e) {

    }


    public synchronized void
    mouseExited(MouseEvent e) {

    }


    public synchronized void
    mousePressed(MouseEvent e) {

    }


    public synchronized void
    mouseReleased(MouseEvent e) {

    }

    ///////////////////////////////////
    //
    // MouseMotionListener Methods
    //
    ///////////////////////////////////

    public synchronized void
    mouseDragged(MouseEvent e) {

    }


    public void
    mouseMoved(MouseEvent e) {

    }

    ///////////////////////////////////
    //
    // YoixInterfaceDrawable Methods
    //
    ///////////////////////////////////

    public Graphics
    getPaintGraphics() {

	Graphics  g;

	//
	// We never had any luck (on Linux) using super.getGraphics2D(),
	// even when we tried flush() and we currently have no idea what
	// the problem is. Using super.getGraphics() occasionally worked
	// even though we suspect it's undoubtedly not correct. The real
	// solution is try to get super.getGraphics2D() to work and if we
	// can't then consider removing the paint() support.
	// 

	////return(super.getGraphics());	// super.getGraphics2D()???
	return(super.getGraphics2D());	// super.getGraphics2D()???
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


    public final void
    paintBackground(Graphics g) {

	//
	// Recent additions to YoixInterfaceDrawable, so something has to
	// be defined here. Just an empty method so the the compiler won't
	// complain, but may eventually do something??
	//

    }


    public final void
    paintBackgroundImage(Graphics g) {

	//
	// Recent additions to YoixInterfaceDrawable, so something has to
	// be defined here. Just an empty method so the the compiler won't
	// complain, but may eventually do something??
	//

    }


    public final synchronized void
    setBackgroundHints(int hints) {

	backgroundhints = hints;
    }


    public final synchronized void
    setBackgroundImage(Image image) {

	backgroundimage = image;
    }


    public synchronized void
    setPaint(YoixObject obj) {

	if (isPaintable())
	    paint = obj.notNull() ? obj : null;
    }

    ///////////////////////////////////
    //
    // J3DCanvas3D Methods
    //
    ///////////////////////////////////

    public final void
    addNotify() {

	//
	// We eventually may want to essentially disable this if we're
	// writing to an offscreen buffer. Think removeNotify() should
	// also be called if we set the offscreen image.
	//

	super.addNotify();
    }


    protected void
    finalize() {

	data = null;
	parent = null;
	virtualuniverse = null;
	backgroundimage = null;
	paint = null;
	postrender = null;
	postswap = null;
	prerender = null;
	renderfield = null;
	try {
	    super.finalize();
	}
	catch(Throwable t) {}
    }


    final boolean
    getAlive() {

	return(isRendererRunning());
    }


    final boolean
    getDoubleBuffered() {

	return(getDoubleBufferEnable());
    }


    public final Dimension
    getMaximumSize() {

	return(getLayoutSize(NY_MAXIMUMSIZE, super.getMaximumSize()));
    }


    public final Dimension
    getMinimumSize() {

	return(getLayoutSize(NY_MINIMUMSIZE, super.getMinimumSize()));
    }


    public final Image
    getOffScreenImage() {

	ImageComponent2D  buffer;
	Image             image = null;

	//
	// The renderOffScreenBuffer() call seems to be the trouble maker
	// on Linux.
	//

	if (isOffScreen()) {
	    renderOffScreenBuffer();
	    waitForOffScreenRendering();
	    if ((buffer = getOffScreenBuffer()) != null)
		image = buffer.getImage();
	}
	return(image);
    }


    public final Dimension
    getPreferredSize() {

	return(getLayoutSize(NY_PREFERREDSIZE, super.getPreferredSize()));
    }


    final BodyVirtualUniverse
    getUniverse() {

	return(virtualuniverse);
    }


    public final void
    paint(Graphics g) {

	//
	// This doesn't work properly, and we've tried lots of different
	// approaches. Probably had the best results (which weren't all
	// that great) when paintCallback() was called from an official
	// callback method, like postwap(). Eventually will investigate
	// and remove paint support if we can't make it work properly.
	// See getPaintGraphics() comments for more info.
	//

	paintCallback(g);
	super.paint(g);
	flush(false);
    }


    public final void
    postRender() {

	YoixObject  funct;

	if ((funct = postrender) != null)
	    parent.call(funct, new YoixObject[0]);
	flush(false);
    }


    public final void
    postSwap() {

	YoixObject  funct;

	if ((funct = postswap) != null)
	    parent.call(funct, new YoixObject[0]);
	flush(false);
    }


    public final void
    preRender() {

	YoixObject  funct;

	if ((funct = prerender) != null)
	    parent.call(funct, new YoixObject[0]);
	flush(false);
    }


    public final void
    renderField(int field) {

	YoixObject  funct;
	YoixObject  argv[];

	if ((funct = renderfield) != null) {
	    switch (field) {
		case FIELD_LEFT:
		    argv = new YoixObject[] {YoixObject.newInt(-1)};
		    break;

		case FIELD_RIGHT:
		    argv = new YoixObject[] {YoixObject.newInt(1)};
		    break;

		default:
		    argv = new YoixObject[] {YoixObject.newInt(0)};
		    break;
	    }
	    parent.call(funct, argv);
	}
	flush(false);
    }


    final synchronized void
    setAlive(boolean state) {

	if (state)
	    startRenderer();
	else stopRenderer();
    }


    final synchronized void
    setDoubleBuffered(boolean state) {

	setDoubleBufferEnable(state);
    }


    final synchronized void
    setOffScreenImage(YoixObject obj) {

	ImageComponent2D  buffer;
	Screen3D          screen;
	Image             image;
	int               width;
	int               height;

	//
	// This probably needs some work. Recovering the offscreen buffer
	// seems to kill X on Linux, which makes it really hard to debug
	// (it's the renderOffScreenBuffer() in getOffScreenImage()). The
	// behavior on Macs is better, but the recovered image is black,
	// which could be because the ImageComponent2D constructor should
	// be setting things like yUp. Suspect we'll debug later on a Mac.
	//

	if ((image = YoixMake.javaImage(obj)) != null) {
	    if (image instanceof BufferedImage) {
		if ((width = ((BufferedImage)image).getWidth()) > 0) {
		    if ((height = ((BufferedImage)image).getHeight()) > 0) {
			screen = getScreen3D();
			screen.setSize(width, height);
			screen.setPhysicalScreenWidth(width*0.0254/90.0);
			screen.setPhysicalScreenHeight(height*0.0254/90.0);
			setOffScreenBuffer(Make.javaImageComponent2D(obj));
		    }
		}
	    }
	}
    }


    final synchronized void
    setPostRender(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0))
		postrender = obj;
	    else VM.abort(TYPECHECK, NL_POSTRENDER);
	} else postrender = null;
    }


    final synchronized void
    setPostSwap(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0))
		postswap = obj;
	    else VM.abort(TYPECHECK, NL_POSTSWAP);
	} else postswap = null;
    }


    final synchronized void
    setPreRender(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(0))
		prerender = obj;
	    else VM.abort(TYPECHECK, NL_PRERENDER);
	} else prerender = null;
    }


    final synchronized void
    setRenderField(YoixObject obj) {

	if (obj.notNull()) {
	    if (obj.callable(1))
		renderfield = obj;
	    else VM.abort(TYPECHECK, NL_RENDERFIELD);
	} else renderfield = null;
    }


    final void
    setUniverse(BodyVirtualUniverse universe) {

	virtualuniverse = universe;
    }


    final void
    setView(YoixObject obj) {

	Object  body;

	if (obj.notNull()) {
	    if (obj instanceof J3DObject) {
		body = ((J3DObject)obj).body();
		if (body instanceof BodyVirtualUniverse)
		    ((BodyVirtualUniverse)body).setCanvas(this);
		else if (body instanceof BodyViewPlatform)
		    ((BodyViewPlatform)body).setCanvas(this);
		else VM.abort(TYPECHECK, NL_VIEW);
	    } else VM.abort(TYPECHECK, NL_VIEW);
	}
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////

    private void
    flush(boolean wait) {

	//
	// Currently does nothing because we rather easily were able to
	// cause a deadlock. Happened when paint() called this method and
	// we called getGraphics2D(). Chance this caused some of the other
	// bad behavior that seemed to be caused by postRender() and the
	// other callbacks - we will eventually investigate.
	//
    }


    private Dimension
    getLayoutSize(String name, Dimension size) {

	YoixObject  obj;
	Dimension   defaultsize;

	//
	// Classes that extend this one can override this method when
	// they need more control over layout sizing.
	//

	if ((obj = data.getObject(name)) != null && obj.notNull()) {
	    defaultsize = size;
	    size = YoixMakeScreen.javaDimension(obj);
	    if (size.width <= 0 || size.height <= 0) {
		if (size.width <= 0)
		    size.width = (defaultsize != null) ? defaultsize.width : 0;
		if (size.height <= 0)
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
		else clip = YoixMake.yoixBBox(bounds, data);
		parent.call(funct, new YoixObject[] {clip});
	    } else parent.call(funct, new YoixObject[0]);
	}
    }
}

